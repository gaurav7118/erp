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
package com.krawler.spring.accounting.receipt;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.CsvReader;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceController;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.payment.accPaymentController;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.payment.accPaymentService;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.NoSuchMessageException;
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
public class accReceiptController extends MultiActionController implements MessageSourceAware {

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
    private EnglishNumberToWords EnglishNumberToWordsOjb = new EnglishNumberToWords();
    private AccCommonTablesDAO accCommonTablesDAO;
    private accPaymentService paymentService;
    private accMasterItemsDAO accMasterItemsDAO;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
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
    
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    
    public void setaccPaymentService(accPaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    public void setAccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAO) {
        this.accMasterItemsDAO = accMasterItemsDAO;
    }
    
    /**
     * Description : Get Day End Collection Details
     * @param <request> used to get default company setup parameters
     * @param <response> used to send response
     * @return :JSONObject
     */
    
     public ModelAndView getDayEndcollectionDetails(HttpServletRequest request, HttpServletResponse response) {

        boolean issuccess = false;
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        String msg = "";
        List ckeckList = new ArrayList();
        List checkTotal = new ArrayList();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            jobj = getDayEndcollectionDetailsJson(request, false);
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
     
     /**
     * Description : Get Day End Collection Details Json
     * @param <request> used to get default company setup parameters
     * @param <export> Check for Export Functionality
     * @return :JSONObject
     */
    public JSONObject getDayEndcollectionDetailsJson(HttpServletRequest request, boolean export) throws AccountingException, IOException, SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        String msg = "";
        List ckeckList = new ArrayList();
        List customCol = new ArrayList();
        List checkGst = new ArrayList();
        List checkTotalWithoutCate = new ArrayList();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        JSONObject jobj1 = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        JSONObject commData = new JSONObject();
        HashMap<String, Double> mapProductCate = new HashMap<String, Double>();
        int count = 0;
        int cnt = 0;

        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateFormatter(request);
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            requestParams.put(Constants.df, df);
            requestParams.put("companyid", companyId);
            String startDate = (String) request.getParameter("startDate");
            String endDate = (String) request.getParameter("endDate");
            String start = "";
            String limit = "";
            int reportId = 0;
            String newcustomerid = request.getParameter("newcustomerid");
            String paymentMethodId = request.getParameter("paymentMethodId");

            if (!StringUtil.isNullOrEmpty(request.getParameter("reportId"))) {
                reportId = Integer.parseInt(request.getParameter("reportId"));
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
                requestParams.put("ss", request.getParameter("ss"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.start)) && !StringUtil.isNullOrEmpty(request.getParameter(Constants.limit))) {
                start = request.getParameter(Constants.start);
                limit = request.getParameter(Constants.limit);
            }
            requestParams.put(Constants.start, start);
            requestParams.put(Constants.limit, limit);
            requestParams.put("reportId", reportId);
            requestParams.put("paymentMethodId", paymentMethodId);

            if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                requestParams.put("searchJson", request.getParameter("searchJson"));
                requestParams.put("moduleid", request.getParameter("moduleid"));
                requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
            }

            requestParams.put(Constants.REQ_startdate, startDate);
            requestParams.put(Constants.REQ_enddate, endDate);

            requestParams.put(Constants.REQ_startdate, startDate);
            requestParams.put(Constants.REQ_enddate, endDate);
            requestParams.put("newcustomerid", newcustomerid);
            // Column Model
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "customerName");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "customerCode");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "receiptNo");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "transactionDate");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "payinref");
            jarrRecords.put(jobjTemp);
           
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currencysymbol");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "totalAmount");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountinbase");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.cust.name", null, RequestContextUtils.getLocale(request)));//Customer Name
            jobjTemp.put("dataIndex", "customerName");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.common.customer.code", null, RequestContextUtils.getLocale(request)));//Customer Code
            jobjTemp.put("dataIndex", "customerCode");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.common.CashSales/ReceiptNo", null, RequestContextUtils.getLocale(request)));//Cash Sales/Receipt No
            jobjTemp.put("dataIndex", "receiptNo");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.field.TransactionDate", null, RequestContextUtils.getLocale(request)));//Transaction Date
            jobjTemp.put("dataIndex", "transactionDate");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
//            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.common.PayInRef", null, RequestContextUtils.getLocale(request)));//Pay In Ref
            jobjTemp.put("dataIndex", "payinref");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.cust.currency", null, RequestContextUtils.getLocale(request)));//Currency
            jobjTemp.put("dataIndex", "currencysymbol");
            jobjTemp.put("hidden", true);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
//            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.common.amount", null, RequestContextUtils.getLocale(request)));//Amount
            jobjTemp.put("dataIndex", "totalAmount");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
            jobjTemp.put("summaryRenderer", "function(){\n"
                + "            return '<div class=\"grid-summary-common\">'+WtfGlobal.getLocaleText(\"acc.common.total\")+'</div>'\n"
                + "        }");
            jarrColumns.put(jobjTemp);


            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.common.totalAmount.InBaseCurrency", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "amountinbase");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("summaryType", "sum");
//            jobjTemp.put("renderer", "WtfGlobal.returnValRenderer");
//            jobjTemp.put("summaryType", "sum");
//             jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
             jobjTemp.put("summaryRenderer", "function(value, m, rec) {\n"
                + "                if (value != 0) {\n"
                + "                    var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)\n"
                + "                    return retVal;\n"
                + "                } else {\n"
                + "                    return '';\n"
                + "                }\n"
                + "            }");
            jarrColumns.put(jobjTemp);
            
            /*
             * Get Data of Cash sales 
             */
            KwlReturnObject cashSales = accReceiptDAOobj.getCashSalesFOrDayEndCollection(requestParams);
            List cashSalesDetails = cashSales.getEntityList();
            
            /*
             * Get Data of Receive Payment 
             */
            KwlReturnObject receipt = accReceiptDAOobj.getReceiptsFOrDayEndCollection(requestParams);
            List receiptDetails = receipt.getEntityList();
            /*
             * Json of Cash Sales
             */
            DataJArr = getCashSalesFOrDayEndCollectionJson(request, cashSalesDetails, DataJArr, jarrRecords, jarrColumns,customCol);
             /*
             * Json of Receive Payment
             */
            DataJArr = getReceiptsFOrDayEndCollectionJson(request, receiptDetails, DataJArr, jarrRecords, jarrColumns,customCol);
            /*
             * To Compare Customer Code
             */
            DataJArr = sortJsonArrayOnTransectiondate(DataJArr, "ASC");
            DataJArr = sortJsonArrayOnCustomer(DataJArr, "ASC");
            JSONArray pagedJson = DataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(DataJArr, Integer.parseInt(start), Integer.parseInt(limit));
            }

            commData.put("success", true);
            commData.put("coldata", pagedJson);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", DataJArr.length());
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

            jobj1.put("valid", true);
            if (export) {
                jobj1.put("data", DataJArr);
            } else {
                jobj1.put("data", commData);
            }

        } catch (Exception ex) {
            //   msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jobj1;
    }
    
    /**
     * Description : Get cash sales for day end collection Details Json
     * @param <request> used to get default company setup parameters
     * @param <cashSalesDetails> List of Cash sales details 
     * @param <DataJArr> used to return all data of day end collection report 
     * @param <jarrRecords> used to store records 
     * @param <jarrColumns> used to store column name 
     * @return :JSONObject
     */
    
    public JSONArray getCashSalesFOrDayEndCollectionJson(HttpServletRequest request, List cashSalesDetails, JSONArray DataJArr, JSONArray jarrRecords, JSONArray jarrColumns,List customCol) throws SessionExpiredException, ServiceException {
        try {
            Iterator it = cashSalesDetails.iterator();
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String jeId = "";
            int reportId = 0;
            JSONObject custJobj = new JSONObject();
            JSONObject jobjTemp = new JSONObject();
            if (!StringUtil.isNullOrEmpty(request.getParameter("reportId"))) {
                reportId = Integer.parseInt(request.getParameter("reportId"));
            }

            KwlReturnObject currencylist = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) currencylist.getEntityList().get(0);
            HashMap hashMap = new HashMap();
            List customFieldList = new ArrayList();
            hashMap.put("companyId", companyId);
            hashMap.put("reportId", reportId);
            KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            while (it.hasNext()) {
                Object oj = (Object) it.next();
                String invid = oj.toString();
//                String id=(String) it.next();
                KwlReturnObject inv = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                Invoice invoice = (Invoice) inv.getEntityList().get(0);
                jeId = invoice.getJournalEntry().getID();
                JSONObject newJobj = new JSONObject();
                HashMap<String, String> customFieldMap = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap = new HashMap<String, String>();

                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Cash_Sales_ModuleId));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                String customFieldMapValues = "";
                KwlReturnObject custumObjresult = null;
                Map<String, Object> variableMap = new HashMap<String, Object>();
                custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
                replaceFieldMap = new HashMap<String, String>();
                if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                    AccJECustomData jeDetailCustom = (AccJECustomData) custumObjresult.getEntityList().get(0);
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put("companyid", companyId);
                        params.put("isExport", true);
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, custJobj, params);
                    }
                }
                /*
                 Add Customer Custom data
                 */
                HashMap<String, Object> customerParams = new HashMap();
                customerParams.put("companyId", companyId);
                customerParams.put("customerId", invoice.getCustomer().getID());
                putCustomerCustomData(customerParams, custJobj,request);

                for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
                    String column = "Custom_" + customizeReportMapping.getDataIndex();
                    String dataIndex = customizeReportMapping.getDataIndex();
                    String header = customizeReportMapping.getDataHeader();
                    if (customizeReportMapping.getModuleId() == 25) {
                        dataIndex = dataIndex + "Customer";
                        header = header + " (Customer)";
                        column = "CustomerCustom_" + customizeReportMapping.getDataIndex();
                    }
                    if (custJobj.has(column)) {
                        customFieldMapValues = custJobj.getString(column);
                        if (!customCol.contains(dataIndex)) {
                            jobjTemp = new JSONObject();
                            jobjTemp.put("name", dataIndex);
                            jarrRecords.put(jobjTemp);

                            jobjTemp = new JSONObject();
                            jobjTemp.put("header", header);
                            jobjTemp.put("dataIndex", dataIndex);
                            jobjTemp.put("width", 150);
                            jobjTemp.put("pdfwidth", 150);
                            jobjTemp.put("custom", "true");
                            jarrColumns.put(jobjTemp);

                            customCol.add(dataIndex);
                            
                        }
                        newJobj.put(dataIndex, customFieldMapValues);
                    } else {
                        if (!customCol.contains(dataIndex)) {
                            jobjTemp = new JSONObject();
                            jobjTemp.put("name", dataIndex);
                            jarrRecords.put(jobjTemp);

                            jobjTemp = new JSONObject();
                            jobjTemp.put("header", header);
                            jobjTemp.put("dataIndex", dataIndex);
                            jobjTemp.put("width", 150);
                            jobjTemp.put("pdfwidth", 150);
                            jobjTemp.put("custom", "true");
                            jarrColumns.put(jobjTemp);
                            customCol.add(dataIndex);
                        }
                    }
                }
              
                newJobj.put("customerName", invoice.getCustomer().getName());
                newJobj.put("customerCode", invoice.getCustomer().getAcccode());
                newJobj.put("receiptNo", invoice.getInvoiceNumber());
//                newJobj.put("transactionDate", invoice.getJournalEntry().getEntryDate());
                newJobj.put("transactionDate", invoice.getCreationDate());
                /*
                 * To check If cash sale is type  bank then report will  show Cheque No under Pay in Ref column
                 */
                if (invoice.getPayDetail() != null && invoice.getPayDetail().getCheque() != null && !StringUtil.isNullOrEmpty(invoice.getPayDetail().getCheque().getChequeNo())) {
                    newJobj.put("payinref", invoice.getPayDetail().getCheque().getChequeNo());
                } else {
                    newJobj.put("payinref", invoice.getPayDetail().getPaymentMethod().getMethodName());
                }
                newJobj.put("currencysymbol", invoice.getCurrency() != null ? invoice.getCurrency().getCurrencyCode() : currency.getSymbol());
                newJobj.put("currencyid", invoice.getCurrency() != null ? invoice.getCurrency().getCurrencyID() : currency.getCurrencyID());
                newJobj.put("totalAmount", invoice.getInvoiceamount());
                newJobj.put("amountinbase", invoice.getInvoiceamountinbase());
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getReceiptJson : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getReceiptJson : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    /**
     * 
     * @param customerParams
     * @param custJobj= Put Custom data for Customer fields
     * @throws ServiceException
     * @throws JSONException 
     */
    public void putCustomerCustomData(HashMap<String, Object> customerParams, JSONObject custJobj,HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException {
        String companyId = "";
        String customerId = "";
        if (customerParams.containsKey("companyId")) {
            companyId = customerParams.get("companyId").toString();
        }
        if (customerParams.containsKey("customerId")) {
            customerId = customerParams.get("customerId").toString();
        }
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Customer_ModuleId));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        String customFieldMapValues = "";
        KwlReturnObject custumObjresult = null;
        Map<String, Object> variableMap = new HashMap<String, Object>();
        custumObjresult = accountingHandlerDAOobj.getObject(CustomerCustomData.class.getName(), customerId);
        replaceFieldMap = new HashMap<String, String>();
        if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
            CustomerCustomData jeDetailCustom = (CustomerCustomData) custumObjresult.getEntityList().get(0);
            if (jeDetailCustom != null) {
                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                JSONObject params = new JSONObject();
                params.put("companyid", companyId);
                params.put("isExport", true);
                JSONObject jSONObject = new JSONObject();
                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                    String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                    String colValue = "";
                    if (customFieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                        try {
                            String[] valueData = coldata.split(",");
                            for (String value : valueData) {
                                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                                FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                if (fieldComboData != null) {
                                    colValue += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                }
                            }
                            if (colValue.length() > 1) {
                                colValue = colValue.substring(0, colValue.length() - 1);
                            }
                            custJobj.put("Customer" + varEntry.getKey(), colValue);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                        } catch (Exception ex) {
                            custJobj.put("Customer" + varEntry.getKey(), coldata);
                        }
                    } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                        DateFormat df = authHandler.getOnlyDateFormat(request);
                        DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateFromDB=null;
                        try {
                            dateFromDB = defaultDateFormat.parse(coldata);
                            coldata = df.format(dateFromDB);

                        } catch (Exception e) {
                        }
                        custJobj.put("Customer" + varEntry.getKey(), coldata);
                    } else {
                        custJobj.put("Customer" + varEntry.getKey(), coldata != null ? coldata : "");
                    }
                }
            }
        }
    }
    
    /**
     * Description : To get Custom column for Customer
     * @param <newJobj> used to store record 
     * @param <cashSalesDetails> List of Cash sales details 
     * @param <hashMap> to send company id,customer id etc
     * @return :void
     */

    public void getCustomerdata(JSONObject newJobj, HashMap<String, Object> hashMap) throws ServiceException, JSONException {
        String companyId = "";
        String customerId = "";
        if (hashMap.containsKey("companyId")) {
            companyId = hashMap.get("companyId").toString();
        }
        if (hashMap.containsKey("customerId")) {
            customerId = hashMap.get("customerId").toString();
        }
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();

        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Customer_ModuleId));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
        FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        String customFieldMapValues = "";
        KwlReturnObject custumObjresult = null;
        Map<String, Object> variableMap = new HashMap<String, Object>();
        custumObjresult = accountingHandlerDAOobj.getObject(CustomerCustomData.class.getName(), customerId);
        replaceFieldMap = new HashMap<String, String>();
        if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
            CustomerCustomData jeDetailCustom = (CustomerCustomData) custumObjresult.getEntityList().get(0);
            if (jeDetailCustom != null) {
                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                JSONObject params = new JSONObject();
                params.put("companyid", companyId);
                params.put("isExport", true);
                fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
//                        DataJArr.put(newJobj);
//                        flag = 1;
            }
        }
    }

     /**
     * Description : Get cash Receive Payment for day end collection Details Json
     * @param <request> used to get default company setup parameters
     * @param <cashSalesDetails> List of Cash sales details 
     * @param <DataJArr> used to return all data of day end collection report 
     * @param <jarrRecords> used to store records 
     * @param <jarrColumns> used to store column name 
     * @return :JSONArray
     */
    public JSONArray getReceiptsFOrDayEndCollectionJson(HttpServletRequest request, List receiptDetails, JSONArray DataJArr, JSONArray jarrRecords, JSONArray jarrColumns,List customCol) throws SessionExpiredException, ServiceException {
        try {
            Iterator it = receiptDetails.iterator();
            JSONObject jobjTemp = new JSONObject();
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String jeId = "";

            int reportId = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("reportId"))) {
                reportId = Integer.parseInt(request.getParameter("reportId"));
            }
            KwlReturnObject currencylist = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) currencylist.getEntityList().get(0);
            HashMap hashMap = new HashMap();
             List customFieldList = new ArrayList();
            hashMap.put("companyId", companyId);
            hashMap.put("reportId", reportId);
            KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            while (it.hasNext()) {
                Object oj = (Object) it.next();
                String receiptId = oj.toString();
                JSONObject custJobj = new JSONObject();
//                String id=(String) it.next();
                KwlReturnObject rec = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptId);
                Receipt receipt = (Receipt) rec.getEntityList().get(0);
                JSONObject newJobj = new JSONObject();
                jeId = receipt.getJournalEntry().getID();
                HashMap<String, String> customFieldMap = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap = new HashMap<String, String>();

                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Receive_Payment_ModuleId));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                String customFieldMapValues = "";
                KwlReturnObject custumObjresult = null;
                Map<String, Object> variableMap = new HashMap<String, Object>();
                custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
                replaceFieldMap = new HashMap<String, String>();
                if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                    AccJECustomData jeDetailCustom = (AccJECustomData) custumObjresult.getEntityList().get(0);
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put("companyid", companyId);
                        params.put("isExport", true);
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, custJobj, params);
                    }
                }
                 /*
                 Add Customer Custom data
                 */
                HashMap<String, Object> customerParams = new HashMap();
                customerParams.put("companyId", companyId);
                customerParams.put("customerId", receipt.getCustomer().getID());
                putCustomerCustomData(customerParams, custJobj,request);

                for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
                    String column = "Custom_" + customizeReportMapping.getDataIndex();
                    String dataIndex = customizeReportMapping.getDataIndex();
                    String header = customizeReportMapping.getDataHeader();
                    if (customizeReportMapping.getModuleId() == 25) {
                        dataIndex = dataIndex + "Customer";
                        header = header + " (Customer)";
                        column = "CustomerCustom_" + customizeReportMapping.getDataIndex();
                    }
                    if (custJobj.has(column)) {
                        customFieldMapValues = custJobj.getString(column);
                        if (!customCol.contains(dataIndex)) {
                            jobjTemp = new JSONObject();
                            jobjTemp.put("name", dataIndex);
                            jarrRecords.put(jobjTemp);

                            jobjTemp = new JSONObject();
                            jobjTemp.put("header", header);
                            jobjTemp.put("dataIndex", dataIndex);
                            jobjTemp.put("width", 150);
                            jobjTemp.put("pdfwidth", 150);
                            jobjTemp.put("custom", "true");
                            jarrColumns.put(jobjTemp);

                            customCol.add(dataIndex);
                        }
                        newJobj.put(dataIndex, customFieldMapValues);
                    } else {
                        if (!customCol.contains(dataIndex)) {
                            jobjTemp = new JSONObject();
                            jobjTemp.put("name", dataIndex);
                            jarrRecords.put(jobjTemp);

                            jobjTemp = new JSONObject();
                            jobjTemp.put("header", header);
                            jobjTemp.put("dataIndex", dataIndex);
                            jobjTemp.put("width", 150);
                            jobjTemp.put("pdfwidth", 150);
                            jobjTemp.put("custom", "true");
                            jarrColumns.put(jobjTemp);
                            customCol.add(dataIndex);
                        }
                    }
                }
                newJobj.put("customerName", receipt.getCustomer().getName());
                newJobj.put("customerCode", receipt.getCustomer().getAcccode());
                newJobj.put("receiptNo", receipt.getReceiptNumber());
//                newJobj.put("transactionDate", receipt.getJournalEntry().getEntryDate());
                newJobj.put("transactionDate", receipt.getCreationDate());
                /*
                 * To check If receipt is type  bank then report will  show Cheque No under Pay in Ref column
                 */
                 if (receipt.getPayDetail() != null && receipt.getPayDetail().getCheque() != null && !StringUtil.isNullOrEmpty(receipt.getPayDetail().getCheque().getChequeNo())) {
                    newJobj.put("payinref", receipt.getPayDetail().getCheque().getChequeNo());
                } else {
                    newJobj.put("payinref", receipt.getPayDetail().getPaymentMethod().getMethodName());
                }
                newJobj.put("currencysymbol", receipt.getCurrency() != null ? receipt.getCurrency().getCurrencyCode() : currency.getSymbol());
                newJobj.put("currencyid", receipt.getCurrency() != null ? receipt.getCurrency().getCurrencyID() : currency.getCurrencyID());
                newJobj.put("totalAmount", receipt.getDepositAmount());
                newJobj.put("amountinbase", receipt.getDepositamountinbase());
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getReceiptJson : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getReceiptJson : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    /**
     * Description : Export CSV,PDF,XLS,Print
     * @param <request> used to get default company setup parameters
     * @param <response> used to send response
     * @return :JSONObject
     */
    public ModelAndView exportDayEndcollectionDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            boolean export = true;
            jobj = getDayEndcollectionDetailsJson(request, export);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    /**
     * Description : To compare the Customer code in and return to json array
     * @param <array> used to get default company setup parameters
     * @param <Order_Dir> used to direction ex.ascending
     * @return :JSONArray
     */
    
    public JSONArray sortJsonArrayOnCustomer(JSONArray array, String Order_Dir) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            if (Order_Dir.equals("ASC")) {
                Collections.sort(jsons, new Comparator<JSONObject>() {

                    @Override
                    public int compare(JSONObject ja, JSONObject jb) {
                        String customerCodeA = "", customerCodeB = "";
                        try {
                            customerCodeA = ja.optString("customerCode", "0");
                            customerCodeB = jb.optString("customerCode", "0");
                        } catch (Exception ex) {
                            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        return customerCodeA.compareTo(customerCodeB);

                    }
                });
            }

        } catch (JSONException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new JSONArray(jsons);
    }
    
    /**
     * Description : To compare the Transection date in and return to json array
     * @param <array> used to get default company setup parameters
     * @param <Order_Dir> used to direction ex.ascending
     * @return :JSONArray
     */
    
    public JSONArray sortJsonArrayOnTransectiondate(JSONArray array, String Order_Dir) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            if (Order_Dir.equals("ASC")) {
                Collections.sort(jsons, new Comparator<JSONObject>() {

                    @Override
                    public int compare(JSONObject ja, JSONObject jb) {
                        String transactionDateA = "", transactionDateB = "";
                        try {
                            transactionDateA = ja.optString("transactionDate", "0");
                            transactionDateB = jb.optString("transactionDate", "0");
                        } catch (Exception ex) {
                            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        return transactionDateA.compareTo(transactionDateB);

                    }
                });
            }

        } catch (JSONException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new JSONArray(jsons);
    }
    
    
    
    public ModelAndView saveReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        boolean isChequePrint = false;
        String msg = "";
        String paymentid = "";
        String billno = "";
        String advanceamount = "";
        int receipttype = -1;
        String amountpayment = "", accountaddress = "", accountName = "", JENumBer = "";;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveReceipt(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] id = (String[]) li.get(0);
            paymentid = (String) li.get(4);
            String[] amountnew = (String[]) li.get(1);
            amountpayment = amountnew[1].intern();
            billno = (String) li.get(5);
            advanceamount = (String) li.get(6);
            accountaddress = (String) li.get(7);
            accountName = (String) li.get(8);
            if (li.get(9) != null) {
                JENumBer = (String) li.get(9);
            }
            receipttype = (Integer) li.get(10);
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
            if (!StringUtil.isNullOrEmpty(request.getParameter("isChequePrint"))) {
                isChequePrint = Boolean.parseBoolean(request.getParameter("isChequePrint"));
            }
            if (isChequePrint) {
                Date creationDate = new Date(request.getParameter("creationdate"));
                String date = DATE_FORMAT.format(creationDate);
                String[] amount = (String[]) li.get(1);
                String[] amount1 = (String[]) li.get(2);
                String[] accName = (String[]) li.get(3);
                jobjDetails.put(amount[0], amount[1]);
                jobjDetails.put(amount1[0], amount1[1]);
                jobjDetails.put(accName[0], accName[1]);
                jobjDetails.put("date", date);
                jArr.put(jobjDetails);
            }
            issuccess = true;
            msg = messageSource.getMessage("acc.receipt.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + ",</b>" + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + JENumBer + "</b>";   //"Receipt has been saved successfully";
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteJEArray(id[0], companyid);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteChequeOrCard(id[1], companyid);
            txnManager.commit(status);
            if (request.getParameter("isEdit") != null && Boolean.parseBoolean(request.getParameter("isEdit"))) {
                deleteReceiptForEdit(request, response);
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
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
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView updatePaymentReceiptTransactionDetailsInJE(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jobj = new JSONObject();
        int jeupdatedcount = 0;
        boolean issuccess = false;
        try {
            HashMap<String, Object> tempParams = new HashMap<String, Object>();
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = (String) request.getParameter("subdomain");
                subdomainArray = subdomain.split(",");
            }
            KwlReturnObject company = accCompanyPreferencesObj.getCompanyList(subdomainArray);
            Iterator ctr = company.getEntityList().iterator();
            while (ctr.hasNext()) {
                String companyid = ctr.next().toString();
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("companyid", companyid);
                KwlReturnObject result = accReceiptDAOobj.getPaymentReceiptsForJE(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    Receipt receipt = (Receipt) itr.next();
                    boolean isUpdated = false;
                    tempParams = new HashMap<String, Object>();
                    tempParams.put("transactionID", receipt.getID());
                    tempParams.put("moduleID", Constants.Acc_Receive_Payment_ModuleId);
                    if (receipt.getJournalEntry() != null) {
                        tempParams.put("journalEntry", receipt.getJournalEntry());
                        isUpdated = accJournalEntryobj.updateJEDetails(tempParams);
                        if (isUpdated) {
                            jeupdatedcount++;
                        }
                    }
                    if (receipt.getJournalEntryForBankCharges() != null) { // Bank Charges JE
                        tempParams.put("journalEntry", receipt.getJournalEntryForBankCharges());
                        isUpdated = accJournalEntryobj.updateJEDetails(tempParams);
                        if (isUpdated) {
                            jeupdatedcount++;
                        }
                    }
                    if (receipt.getJournalEntryForBankInterest() != null) { // Bank Interest JE
                        tempParams.put("journalEntry", receipt.getJournalEntryForBankInterest());
                        isUpdated = accJournalEntryobj.updateJEDetails(tempParams);
                        if (isUpdated) {
                            jeupdatedcount++;
                        }
                    }
                    if (receipt.getDisHonouredChequeJe() != null) { // Dishonoured Cheque JE
                        tempParams.put("journalEntry", receipt.getDisHonouredChequeJe());
                        isUpdated = accJournalEntryobj.updateJEDetails(tempParams);
                    }
                    if (isUpdated) {
                        jeupdatedcount++;
                    }
                }
            }
            txnManager.commit(status);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            txnManager.rollback(status);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("Updated JE Records ", jeupdatedcount);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /*
     * Method to save Opening Balance Receipts For customer.
     */
    public ModelAndView saveOpeningBalanceReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        boolean isEdit = false;
        boolean isAccountingExe = false;
        String receiptNumber = null;
        String companyid = "";
        try {
            receiptNumber = request.getParameter("number");
            String receiptId = request.getParameter("transactionId");
            companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cncount = null;
            if (StringUtil.isNullOrEmpty(receiptId)) {
                /*
                 * Checks duplicate number while creating new record
                 */
                cncount = accReceiptDAOobj.getReceiptFromBillNo(receiptNumber, companyid);
                if (cncount.getRecordTotalCount() > 0) {
                    isAccountingExe = true;
                    throw new AccountingException(messageSource.getMessage("acc.payment.receivepaymentno", null, RequestContextUtils.getLocale(request)) + receiptNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                /*
                 * code for checking wheather entered number can be generated by sequence format or not
                 */
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Receive_Payment_ModuleId, receiptNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + receiptNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.pleaseentersomeothernumber", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }else{
                isEdit = true;
            }
            synchronized (this) {
                /*
                 * Checks duplicate number for simultaneous transactions
                 */
                status = txnManager.getTransaction(def);
                KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(receiptNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);//Get entry from temporary table
                if (resultInv.getRecordTotalCount() > 0) {
                    isAccountingExe = true;
                    throw new AccountingException(messageSource.getMessage("acc.payment.selectedReceivePamentNo", null, RequestContextUtils.getLocale(request)) + receiptNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, RequestContextUtils.getLocale(request)));
                } else {
                    accCommonTablesDAO.insertTransactionInTemp(receiptNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);//Insert entry in temporary table
                }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
            List li = saveOpeningBalanceReceipt(request);
            boolean isEditInv = false;
            String succMsg = messageSource.getMessage("acc.field.saved", null, RequestContextUtils.getLocale(request));
            if (!li.isEmpty()) {
                receiptNumber = li.get(0).toString();
                isEditInv = (Boolean) li.get(1);
            }
            if (isEditInv) {
                succMsg = messageSource.getMessage("acc.field.updated", null, RequestContextUtils.getLocale(request));
            }
            issuccess = true;
            msg = messageSource.getMessage("acc.field.Receipt", null, RequestContextUtils.getLocale(request)) + " " + receiptNumber + " " + messageSource.getMessage("acc.field.hasbeen", null, RequestContextUtils.getLocale(request)) + " " + succMsg + " " + messageSource.getMessage("acc.field.successfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            accCommonTablesDAO.deleteTransactionInTemp(receiptNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);//Delete entry in temporary table
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(receiptNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(receiptNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(receiptNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArr);
                jobj.put("accException", isAccountingExe);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView importOpeningBalanceReceipts(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", companyid);
            boolean typeXLSFile = (request.getParameter("typeXLSFile") != null) ? Boolean.parseBoolean(request.getParameter("typeXLSFile")) : false;
            String doAction = request.getParameter("do");
            
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);      
            
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());
            requestParams.put("importMethod", typeXLSFile ? "xls" : "csv");
            requestParams.put("currencyId", companyid);
            requestParams.put("moduleName", "Opening Receipt");
            requestParams.put("moduleid", Constants.Acc_opening_Receipt);
            requestParams.put("bookbeginning", preferences.getBookBeginningFrom());
            
            if (doAction.compareToIgnoreCase("import") == 0) {
                JSONObject datajobj = new JSONObject();
                JSONObject resjson = new JSONObject(request.getParameter("resjson").toString());
                JSONArray resjsonJArray = resjson.getJSONArray("root");

                String filename = request.getParameter("filename");
                datajobj.put("filename", filename);

                String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
                File filepath = new File(destinationDirectory + "/" + filename);
                datajobj.put("FilePath", filepath);

                datajobj.put("resjson", resjsonJArray);

                jobj = importOeningTransactionsRecords(request, datajobj);
            }else if (doAction.compareToIgnoreCase("validateData") == 0) {
                jobj = importHandler.validateFileData(requestParams);
            }
           issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void validateHeaders(JSONArray validateJArray, HttpServletRequest request) throws AccountingException, ServiceException {
        try {

            List<String> list = new ArrayList<String>();
            list.add("Transaction Number");
            list.add("Transaction Date");
            list.add("Amount");
//            list.add("Due Date");
            list.add("Customer Code");
//            list.add("Exchange Rate");
            list.add("Currency");



            List<String> fileHeaderList = new ArrayList<String>();

            for (int i = 0; i < validateJArray.length(); i++) {
                String header = validateJArray.getJSONObject(i).getString("header").trim();
                fileHeaderList.add(header);
            }

            // iterating for manadatory columns

            for (String manadatoryField : list) {
                if (!fileHeaderList.contains(manadatoryField)) {
                    throw new AccountingException(manadatoryField + messageSource.getMessage("acc.field.columnisnotavailabeinfile", null, RequestContextUtils.getLocale(request)));
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public List saveOpeningBalanceReceipt(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException, UnsupportedEncodingException {
        List returnList = new ArrayList();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            KwlReturnObject result;
            boolean isEditInvoice = false;
            String auditMsg = "", auditID = "", memo="";
            auditMsg = "added";
            auditID = AuditAction.OPENING_BALANCE_CREATED;
            // Fetching request parameters

            String receiptNumber = request.getParameter("number");
            String transactionDateStr = request.getParameter("billdate");
            String chequeDateStr = request.getParameter("chequeDate");
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            String transactionAmountStr = request.getParameter("transactionAmount");
            String receiptId = request.getParameter("transactionId");
            String chequeNumber = request.getParameter("chequenumber");
            String drawnOn = request.getParameter("drawnon");
            String customerId = request.getParameter("accountId");
            boolean conversionRateFromCurrencyToBase = true;
            if (request.getParameter("CurrencyToBaseExchangeRate") != null) {
                conversionRateFromCurrencyToBase = Boolean.parseBoolean(request.getParameter("CurrencyToBaseExchangeRate"));
            }
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));

            double exchangeRateForOpeningTransaction = 1;
            if (!StringUtil.isNullOrEmpty(request.getParameter("exchangeRateForOpeningTransaction"))) {
                exchangeRateForOpeningTransaction = Double.parseDouble(request.getParameter("exchangeRateForOpeningTransaction"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("memo"))) {
                memo = request.getParameter("memo").toString();
            }
            
            String accountId = "";

            if (!StringUtil.isNullOrEmpty(customerId)) {
                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerId);
                Customer customer = (Customer) custresult.getEntityList().get(0);
                accountId = customer.getAccount().getID();
            }

            Date transactionDate = df.parse(df.format(new Date()));
            Date chequeDate = df.parse(df.format(new Date()));

            if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
                transactionDate = df.parse(transactionDateStr);
            }

            if (!StringUtil.isNullOrEmpty(chequeDateStr)) {
                chequeDate = df.parse(chequeDateStr);
            }

            double transactionAmount = 0d;
            if (!StringUtil.isNullOrEmpty(transactionAmountStr)) {
                transactionAmount = Double.parseDouble(transactionAmountStr);
            }

            // creating receipt data

            HashMap receipthm = new HashMap();

            if (StringUtil.isNullOrEmpty(receiptId)) {// as user can not chnaged entered number in edit so we have not cheked duplicate entry in edit. if this logic change we need to change here as well
                //code for checking duplicate entry
//                result = accReceiptDAOobj.getReceiptFromBillNo(receiptNumber, companyid);
//                int count = result.getRecordTotalCount();
//                if (count > 0) {
//                    throw new AccountingException(messageSource.getMessage("acc.field.Receiptnumber", null, RequestContextUtils.getLocale(request)) + " " + receiptNumber + " " + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
//                }
                receipthm.put("entrynumber", receiptNumber);
                receipthm.put("autogenerated", false);
                
                
            }


            if (!StringUtil.isNullOrEmpty(receiptId)) {
                isEditInvoice = true;
                auditMsg = "updated";
                auditID = AuditAction.OPENING_BALANCE_UPDATED;
                boolean isPaymentUsedInOtherTransactions = isPaymentUsedInOtherTransactions(receiptId, companyid);

                if (isPaymentUsedInOtherTransactions) {
                    throw new AccountingException(messageSource.getMessage("acc.nee.73", null, RequestContextUtils.getLocale(request)));
                }

                receipthm.put("receiptid", receiptId);
            }


            receipthm.put("depositamount", transactionAmount);//
            receipthm.put("currencyid", currencyid);//
            receipthm.put("externalCurrencyRate", externalCurrencyRate);//
            receipthm.put("memo", memo);//
            receipthm.put("companyid", companyid);//
            receipthm.put("chequeNumber", chequeNumber);//
            receipthm.put("drawnOn", drawnOn);//
            receipthm.put("creationDate", transactionDate);//
            receipthm.put("chequeDate", chequeDate);//
            receipthm.put("customerId", customerId);//
            receipthm.put("accountId", accountId);//
            receipthm.put("isOpeningBalenceReceipt", true);//
            receipthm.put("normalReceipt", false);//
            receipthm.put("openingBalanceAmountDue", transactionAmount);//
            receipthm.put("isadvancepayment", true);
            receipthm.put("contraentry", false);
            receipthm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);
            receipthm.put("conversionRateFromCurrencyToBase", conversionRateFromCurrencyToBase);
            // Store Receipt amount in base currency
            if (conversionRateFromCurrencyToBase) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                receipthm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
                receipthm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
            } else {
                receipthm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount/exchangeRateForOpeningTransaction, companyid));
                receipthm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount/exchangeRateForOpeningTransaction, companyid));
            }
            
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();

            receipthm.put("createdby", createdby);
            receipthm.put("modifiedby", modifiedby);
            receipthm.put("createdon", createdon);
            receipthm.put("updatedon", updatedon);
            
            
            String oldChequeNo = "";
            if (!StringUtil.isNullOrEmpty(receiptId)) {// for edit case
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptId);
                Receipt receipt = (Receipt) receiptObj.getEntityList().get(0);
                
                if (receipt.getPayDetail() != null) {
                    if (receipt.getPayDetail().getCheque() != null) {
                        oldChequeNo = receipt.getPayDetail().getCheque().getChequeNo();
                    }
                }
            }

            boolean bankReconsilationEntry = false;
            String payDetailID = null;
            Map<String, Object> bankReconsilationMap = new HashMap<String, Object>();

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            
            KwlReturnObject extcapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extcapresult.getEntityList().get(0);

            result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("paymentMethodID"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);
            Account depositTo = payMethod.getAccount();
            HashMap pdetailhm = new HashMap();
            pdetailhm.put("paymethodid", payMethod.getID());
            pdetailhm.put("companyid", companyid);
            JSONObject obj = null  ;
            if(!StringUtil.isNullOrEmpty(request.getParameter("paydetail"))){
                obj = new JSONObject(request.getParameter("paydetail"));
            }
            Date startDate = preferences.getFinancialYearFrom();
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            startCal.add(Calendar.YEAR, 1);
            startCal.add(Calendar.DAY_OF_YEAR, -1);
            Date endDate = startCal.getTime();

            if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH) {
                if (payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                    bankReconsilationEntry = obj.getString("paymentstatus") != null ? obj.getString("paymentstatus").equals("Cleared") : false;

                    BigInteger nextSeqNumber = new BigInteger("0");
                    boolean checkForNextSequenceNumberAlso = true;
                    boolean isChequeNumberInString = false;
                    if (extraCompanyPreferences != null && extraCompanyPreferences.isShowAutoGeneratedChequeNumber()) {
                        try {// USER can enter String values also in such case exception will come

                            nextSeqNumber = new BigInteger(obj.getString("chequenumber"));
                            // cheque whether Cheque Number exist or not if already exist then don't let it save
                        } catch (Exception ex) {
                            checkForNextSequenceNumberAlso = false;
                            isChequeNumberInString = true;
                        }
                    } else {
                        checkForNextSequenceNumberAlso = false;
                    }

                    boolean isChequeNumberAvailable = false;

                    boolean isEditCaseButChqNoChanged = false;
                    if (!StringUtil.isNullOrEmpty(obj.optString("chequenumber")) && extraCompanyPreferences != null && extraCompanyPreferences.isShowAutoGeneratedChequeNumber()) {
                        try {// OLD CHQ NO. can be String value also in such case exception will come

                            HashMap chequeNohm = new HashMap();
                            chequeNohm.put("companyId", companyid);
                            chequeNohm.put("sequenceNumber", nextSeqNumber);
                            chequeNohm.put("checkForNextSequenceNumberAlso", checkForNextSequenceNumberAlso);
                            chequeNohm.put("nextChequeNumber", obj.optString("chequenumber"));
                            chequeNohm.put("bankAccountId", payMethod.getAccount().getID());
                            isChequeNumberAvailable = paymentService.isChequeNumberAvailable(chequeNohm);

                            BigInteger oldChqNoIntValue = new BigInteger("0");
                            if (!StringUtil.isNullOrEmpty(oldChequeNo)) {
                                oldChqNoIntValue = new BigInteger(oldChequeNo);
                            }


                            if (!oldChqNoIntValue.equals(nextSeqNumber)) {
                                isEditCaseButChqNoChanged = true;
                            }

                            if (isChequeNumberInString) {
                                if (!oldChequeNo.equals(obj.optString("chequenumber"))) {
                                    isEditCaseButChqNoChanged = true;
                                }
                            }

                        } catch (Exception ex) {
                            if (!oldChequeNo.equals(obj.optString("chequenumber"))) {
                                isEditCaseButChqNoChanged = true;
                            }
                        }
                    } else {
                        if (!oldChequeNo.equals(obj.optString("chequenumber"))) {
                            isEditCaseButChqNoChanged = true;
                        }
                    }


//                    if (!StringUtil.isNullOrEmpty(obj.optString("chequenumber")) && isChequeNumberAvailable && isEditCaseButChqNoChanged) {
//                        throw new AccountingException("Cheque Number : <b>" + obj.getString("chequenumber") + "</b> is already exist, Please enter different one");
//                    }

                    HashMap chequehm = new HashMap();
                    chequehm.put("chequeno", obj.optString("chequenumber"));
                    chequehm.put("companyId", companyid);
                    chequehm.put("createdFrom", 2);
                    chequehm.put("bankAccount", (payMethod.getAccount() != null) ? payMethod.getAccount().getID() : "");
                    chequehm.put("description", StringUtil.DecodeText(obj.optString("description")));
                    chequehm.put("bankname",StringUtil.DecodeText(obj.optString("paymentthrough","")) );
                    chequehm.put("duedate", df.parse(obj.getString("postdate")));
                    chequehm.put("bankmasteritemid", obj.optString("paymentthroughid",""));
                    KwlReturnObject cqresult = accPaymentDAOobj.addCheque(chequehm);
                    Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
                    pdetailhm.put("chequeid", cheque.getID());
                } else if (payMethod.getDetailType() == PaymentMethod.TYPE_CARD) {
                    HashMap cardhm = new HashMap();
                    cardhm.put("cardno", obj.getString("CardNo"));
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
            if (!StringUtil.isNullOrEmpty(receiptId) && !StringUtil.isNullOrEmpty(payDetailID)) {
                pdetailhm.put("paydetailid", payDetailID);
            }
            pdresult = accPaymentDAOobj.addPayDetail(pdetailhm);
            PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);
            receipthm.put("paydetailsid", pdetail.getID());
            
            result = accReceiptDAOobj.saveReceipt(receipthm);

            Receipt receipt = (Receipt) result.getEntityList().get(0);
            returnList.add(receipt.getReceiptNumber());
            returnList.add(isEditInvoice);
            
            if (bankReconsilationEntry) {

                String bankAccountId = payMethod.getAccount().getID();
                Date clearanceDate = df.parse(obj.getString("clearancedate"));

                bankReconsilationMap.put("isOpeningPayment", true);
                bankReconsilationMap.put("bankAccountId", bankAccountId);
                bankReconsilationMap.put("startDate", startDate); //Financial Year Start Date
                bankReconsilationMap.put("endDate", endDate); //Financial Year End Date
                bankReconsilationMap.put("clearanceDate", clearanceDate);
                bankReconsilationMap.put("endingAmount", 0.0);
                bankReconsilationMap.put("companyId", companyid);
                bankReconsilationMap.put("clearingamount", receipt.getDepositAmount());
                bankReconsilationMap.put("currencyid", currencyid);
                bankReconsilationMap.put("details", "");
                bankReconsilationMap.put("receipt", receipt);
                bankReconsilationMap.put("ismultidebit", true);
                bankReconsilationMap.put("createdby", sessionHandlerImpl.getUserid(request));
                bankReconsilationMap.put("checkCount", 0);      //As the discussion with Mayur B. and Sagar A. sir MP relates to check count
                bankReconsilationMap.put("depositeCount", 1);

                HashMap<String, Object> globalParams = AccountingManager.getGlobalParams(request);
                saveBankReconsilation(bankReconsilationMap, globalParams);
                auditTrailObj.insertAuditLog(AuditAction.BANK_RECONCILIATION_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " has reconciled " + receipt.getReceiptNumber(), request, companyid);
            }
              
            String customfield = request.getParameter("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_OpeningBalanceReceipt_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceReceiptid);
                customrequestParams.put("modulerecid", receipt.getID());
                customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath",Constants.Acc_OpeningBalanceReceipt_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    receipthm.put("receiptid", receipt.getID());
                    receipthm.put("openingBalanceReceiptCustomData", receipt.getID());
                    result = accReceiptDAOobj.saveReceipt(receipthm);
                }
            }
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has  " + auditMsg + " an Opening Balance Receipt " + receiptNumber, request, receiptNumber);
        } catch (JSONException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnList;
    }

    private boolean isPaymentUsedInOtherTransactions(String paymentId, String companyId) throws ServiceException {
        boolean isPaymentUsedInOtherTransactions = false;

        KwlReturnObject result;
        if (!StringUtil.isNullOrEmpty(paymentId)) {
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentId);
            Receipt receipt = (Receipt) objItr.getEntityList().get(0);

            if (receipt != null) {
                Set<ReceiptDetail> receiptDetailSet = receipt.getRows();
                if (receiptDetailSet != null) {
                    Iterator itr = receiptDetailSet.iterator();
                    while (itr.hasNext()) {
                        ReceiptDetail row = (ReceiptDetail) itr.next();
                        if (row.getInvoice() != null) {
                            isPaymentUsedInOtherTransactions = true;
                        }
                    }
                }
            }

        }
        return isPaymentUsedInOtherTransactions;
    }

    public HashMap getCurrencyMap(boolean isCurrencyCode) throws ServiceException {
        HashMap currencyMap = new HashMap();
        KwlReturnObject returnObject = accCurrencyobj.getCurrencies(currencyMap);
        List currencyList = returnObject.getEntityList();

        if (currencyList != null && !currencyList.isEmpty()) {
            Iterator iterator = currencyList.iterator();
            while (iterator.hasNext()) {
                KWLCurrency currency = (KWLCurrency) iterator.next();
                if(isCurrencyCode){
                    currencyMap.put(currency.getCurrencyCode(), currency.getCurrencyID());
                }else{
                currencyMap.put(currency.getName(), currency.getCurrencyID());
                }
            }
        }
        return currencyMap;
    }

    private String getCurrencyId(String currencyName, HashMap currencyMap) {
        String currencyId = "";
        if (currencyMap != null && currencyMap.containsKey(currencyName)) {
            currencyId = currencyMap.get(currencyName).toString();
        }
        return currencyId;
    }

    private Customer getCustomerByCode(String customerCode, String companyID, HttpServletRequest request) throws AccountingException {
        Customer customer = null;
        try {
            if (!StringUtil.isNullOrEmpty(customerCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accCustomerDAOObj.getCustomerByCode(customerCode, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    customer = (Customer) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException(messageSource.getMessage("acc.field.SystemFailurewhilefetchingcustomer", null, RequestContextUtils.getLocale(request)));
        }
        return customer;
    }

    public JSONObject importOeningTransactionsRecords(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
        CsvReader csvReader=null;
        int total = 0, failed = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String gcurrencyId = sessionHandlerImpl.getCurrencyID(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("filename");
        String masterPreference = request.getParameter("masterPreference");
        String delimiterType = request.getParameter("delimiterType");
        HashMap<String, FieldParams> customFieldParamMap = new HashMap<String, FieldParams>();
        KwlReturnObject resultObj = null;
        String customfield = "";

        JSONObject returnObj = new JSONObject();

        try {
            String dateFormat = "yyyy-MM-dd", dateFormatId = request.getParameter("dateFormat");
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {

                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);
                if (kdf != null) {
                    dateFormat = kdf.getJavaForm();
                }
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            DateFormat datef=authHandler.getDateOnlyFormat();
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            Boolean isCurrencyCode=extrareferences.isCurrencyCode();
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);
            String record = "";
            int cnt = 0;

            double externalCurrencyRate = 0d;//StringUtil.getDouble(request.getParameter("externalcurrencyrate"));

            StringBuilder failedRecords = new StringBuilder();

            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            Map<String, JSONObject> configMap = new HashMap<>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
            List headArrayList = new ArrayList();

            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                headArrayList.add(jSONObject.get("csvheader"));
                columnConfig.put(jSONObject.getString("columnname"), jSONObject.getInt("csvindex"));
                configMap.put(jSONObject.getString("columnname"), jSONObject);
            }
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            HashMap currencyMap = getCurrencyMap(isCurrencyCode);
            Set transactionNumberSet = new HashSet();

            while (csvReader.readRecord()) {
                String failureMsg = "";
                String[] recarr = csvReader.getValues();
                if (cnt == 0) {//Putting Header in failure File
                    failedRecords.append(createCSVrecord(recarr) + "\"Error Message\"");//failedRecords.append("\"Row No.\","+createCSVrecord(fileData)+"\"Error Message\""); 
                } else {
                    try {
                        String accountId = "";
                        String customerId = "";
                        
                        /*1. Invoice Number*/
                        String invoiceNumber="";
                        if (columnConfig.containsKey("ReceiptNumber")) {
                            invoiceNumber = recarr[(Integer) columnConfig.get("ReceiptNumber")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(invoiceNumber)) {
                                throw new AccountingException("Empty data found in Transaction Number, cannot set empty data for Transaction Number.");
                            } else if (!transactionNumberSet.add(invoiceNumber)) {// this method retur true when added or false when already exit record not get added
                                throw new AccountingException("Duplicate Transaction Number '" + invoiceNumber + "' in file.");
                            } else {
                                KwlReturnObject result = accReceiptDAOobj.getReceiptFromBillNo(invoiceNumber, companyid);
                                int count = result.getRecordTotalCount();
                                if (count > 0) {
                                    throw new AccountingException(messageSource.getMessage("acc.field.Receiptnumber", null, RequestContextUtils.getLocale(request)) + invoiceNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                                }
                            }
                            
                            JSONObject configObj = configMap.get("ReceiptNumber");
                            int maxLength = configObj.optInt("maxLength", 0);
                            String validationType = configObj.optString("validatetype");
                            if ("string".equalsIgnoreCase(validationType) && !StringUtil.isNullOrEmpty(invoiceNumber) && invoiceNumber.length() > maxLength) {
                                if (masterPreference.equalsIgnoreCase("0")) {
                                    failureMsg += "Data length greater than " + maxLength + " for column Transaction Number.";
                                } else {// for other two cases need to trim data upto max length
                                    invoiceNumber = invoiceNumber.substring(0, maxLength);
                                }
                            }
                        } else {
                            throw new AccountingException(messageSource.getMessage("acc.field.TransactionNumberisnotavailable", null, RequestContextUtils.getLocale(request)));
                        }    

                        /*2. Customer Code*/
                        if (columnConfig.containsKey("CustomerCode")) {
                            String customerCode = recarr[(Integer) columnConfig.get("CustomerCode")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(customerCode)) {
                                Customer customer = getCustomerByCode(customerCode, companyid,request);
                                if (customer != null) {
                                    accountId = customer.getAccount().getID();
                                    customerId = customer.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("0")) { //Skip Record
                                        failureMsg += "Customer Code entry not found in master list for Customer Code dropdown.";
                                    } else if (masterPreference.equalsIgnoreCase("1")) {
                                        failureMsg += "Customer Code entry not found in master list for Customer Code dropdown, cannot set empty data for Customer Code.";
                                    } else if (masterPreference.equalsIgnoreCase("2")) {
                                        failureMsg += "Customer Code entry not present in Customer list, Please create new Customer entry for Customer Code as it requires some other details.";
                                    }
                                }
                            }
                        }
                        
                        /*3. Customer Name
                         *if customerID is empty it menas customer is not found for given code. so need to serch data on name
                         */
                        if (StringUtil.isNullOrEmpty(customerId)) {
                            if (columnConfig.containsKey("CustomerName")) {
                                String customerName = recarr[(Integer) columnConfig.get("CustomerName")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(customerName)) {
                                    Customer customer=null;
                                    KwlReturnObject retObj = accCustomerDAOObj.getCustomerByName(customerName, companyid);
                                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                        customer = (Customer) retObj.getEntityList().get(0);
                                    }
                                    if (customer != null) {
                                        accountId = customer.getAccount().getID();
                                        customerId = customer.getID();
                                    } else {
                                        failureMsg+=messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, RequestContextUtils.getLocale(request));
                                    }
                                } else {
                                    failureMsg+=messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, RequestContextUtils.getLocale(request));
                                }
                            } else {
                                failureMsg+=messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, RequestContextUtils.getLocale(request));
                            }
                        }
                        
                        /*4. Creation Date*/
                        String transactionDateStr = "";
                        Date transactionDate = null, bookbeginningdate = null;
                        if (columnConfig.containsKey("CreationDate")) {
                            transactionDateStr = recarr[(Integer) columnConfig.get("CreationDate")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
                                try {
                                    // In UI we are not allowing user to give transaction date  on or after book beginning date
                                    // below code is for the same purpose
                                    transactionDate = df.parse(transactionDateStr);
                                    transactionDate = CompanyPreferencesCMN.removeTimefromDate(transactionDate);
                                    bookbeginningdate = CompanyPreferencesCMN.removeTimefromDate(preferences.getBookBeginningFrom());
                                    if (transactionDate.after(bookbeginningdate) || transactionDate.equals(bookbeginningdate)) {
                                        failureMsg+=messageSource.getMessage("acc.transactiondate.beforebbdate", null, RequestContextUtils.getLocale(request));
                                    }
                                    CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, transactionDate, true);
                                } catch (ParseException ex) {
                                    failureMsg+="Incorrect date format for Transaction Date, Please specify values in " + dateFormat + " format.";
                                } catch (Exception ex){
                                    failureMsg+=ex.getMessage();
                                }
                            } else {
                                failureMsg+=messageSource.getMessage("acc.field.TransactionDateisnotavailable", null, RequestContextUtils.getLocale(request));
                            }
                        } else {
                            failureMsg+=messageSource.getMessage("acc.field.TransactionDateisnotavailable", null, RequestContextUtils.getLocale(request));
                        }
                        
                        /*5. Invoice Currency */
                        String currencyId = "";
                        if (isCurrencyCode?columnConfig.containsKey("currencyCode"):columnConfig.containsKey("Currency")) {
                            String currencyStr = recarr[isCurrencyCode?(Integer) columnConfig.get("currencyCode"):(Integer) columnConfig.get("Currency")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(currencyStr)) {
                                failureMsg +="Empty data found in Currency, cannot set empty data for Currency.";
                            } else {
                                currencyId = getCurrencyId(currencyStr, currencyMap);
                                if (StringUtil.isNullOrEmpty(currencyId)) {
                                    if (masterPreference.equalsIgnoreCase("0")) {// most restricted case
                                        failureMsg += "Currency entry not found in master list for Currency dropdown.";
                                    } else if (masterPreference.equalsIgnoreCase("1")) {// add empty value if data invalid for non mandatory
                                        failureMsg += "Currency entry not found in master list for Currency dropdown, cannot set empty data for Currency.";
                                    } else if (masterPreference.equalsIgnoreCase("2")) {//add new (most lanient)
                                        failureMsg += "Currency entry not present in Currency list, Please create new Currency entry for "+currencyStr+" as it requires some other details.";
                                    }
                                }
                            }
                        } else {
                             failureMsg +=messageSource.getMessage("acc.field.Currencyisnotavailable", null, RequestContextUtils.getLocale(request));
                        } 
                        
                        /*6. Amount*/
                        double transactionAmount = 0d;
                        String transactionAmountStr = "";
                        if (columnConfig.containsKey("Amount")) {
                            transactionAmountStr = recarr[(Integer) columnConfig.get("Amount")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(transactionAmountStr)) {
                                failureMsg += "Empty data found in Amount, cannot set empty data for Amount.";
                            } else {
                                try {
                                    transactionAmount = Double.parseDouble(transactionAmountStr);
                                    if (transactionAmount <= 0) {
                                        failureMsg += "Amount can not be zero or negative.";
                                    }
                                } catch (NumberFormatException ex) {
                                    failureMsg += "Incorrect numeric value for Amount, Please ensure that value type of Amount matches with the Amount.";
                                }
                            }
                        } else {
                            failureMsg += messageSource.getMessage("acc.field.TransactionAmountisnotavailable", null, RequestContextUtils.getLocale(request));
                        }

                        
                        /*7. Exchange Rate */
                        String exchangeRateForOpeningTransactionStr = "";
                        if (columnConfig.containsKey("ExchangeRateForOpeningTransaction")) {
                            exchangeRateForOpeningTransactionStr = recarr[(Integer) columnConfig.get("ExchangeRateForOpeningTransaction")].replaceAll("\"", "").trim();
                        } 
                        
                        double exchangeRateForOpeningTransaction = 1;
                        if (!StringUtil.isNullOrEmpty(exchangeRateForOpeningTransactionStr)) {
                            try {
                                exchangeRateForOpeningTransaction = Double.parseDouble(exchangeRateForOpeningTransactionStr);
                                if (exchangeRateForOpeningTransaction <= 0) {
                                    failureMsg +=messageSource.getMessage("acc.field.ExchangeRateCannotbezeroornegative", null, RequestContextUtils.getLocale(request));
                                }
                            } catch (NumberFormatException ex) {
                                failureMsg += "Incorrect numeric value for Exchange Rate, Please ensure that value type of Exchange Rate matches with the Exchange Rate.";
                            }
                        } else {
                            Map<String, Object> currMap = new HashMap<String, Object>();
                            Date finYrStartDate = preferences.getFinancialYearFrom();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(finYrStartDate);
                            cal.add(Calendar.DATE, -1);

                            Date applyDate = cal.getTime();
                            String adate=datef.format(applyDate);
                            try{
                                applyDate=datef.parse(adate);
                            }catch(ParseException ex){
                                applyDate=cal.getTime();
                            }
                            currMap.put("applydate", applyDate);
                            currMap.put("gcurrencyid", gcurrencyId);
                            currMap.put("companyid", companyid);
                            KwlReturnObject retObj = accCurrencyobj.getExcDetailID(currMap, currencyId, applyDate, null);
                            if (retObj != null) {
                                List li = retObj.getEntityList();
                                if (!li.isEmpty()) {
                                    Iterator itr = li.iterator();
                                    ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                                    if (erd != null) {
                                        exchangeRateForOpeningTransaction = erd.getExchangeRate();
                                    }
                                }
                            }
                        }
                        
                       
                        /*
                         * 8. Payment Method
                         */
                        PaymentMethod payMethod = null;
                        if (columnConfig.containsKey("PaymentMethod")) {
                            String paymentMethodStr = recarr[(Integer) columnConfig.get("PaymentMethod")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(paymentMethodStr)) {
                                failureMsg += "Empty data found in Payment Method, cannot set empty data for Payment Method.";
                            } else {
                                KwlReturnObject retObj = accMasterItemsDAO.getPaymentMethodIdFromName(paymentMethodStr, companyid);
                                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                    payMethod = (PaymentMethod) retObj.getEntityList().get(0);
                                }
                                if (payMethod == null) {
                                    failureMsg += "Payment Method entry not present in Currency list, Please create new Payment Method entry for "+paymentMethodStr+" as it requires some other details.";
                                }

                            }
                        } else {
                            failureMsg += "Payment Method is not available";
                        }
                        
                        
                        /*9. Cheque Number*/
                        String chequeNumber = "";
                        if (columnConfig.containsKey("ChequeNo")) {
                            chequeNumber = recarr[(Integer) columnConfig.get("ChequeNo")].replaceAll("\"", "").trim();
                            
                            JSONObject configObj = configMap.get("ChequeNo");
                            int maxLength = configObj.optInt("maxLength", 0);
                            String validationType = configObj.optString("validatetype");
                            if ("string".equalsIgnoreCase(validationType) && !StringUtil.isNullOrEmpty(chequeNumber) && chequeNumber.length() > maxLength) {
                                if (masterPreference.equalsIgnoreCase("0")) {
                                    failureMsg += "Data length greater than " + maxLength + " for column Cheque Number.";
                                } else {// for other two cases need to trim data upto max length
                                    chequeNumber = chequeNumber.substring(0, maxLength);
                                }
                            }
                        }
                        
                        /*10. Bank Name*/
                        String bankName = "";
                        String bankNameMasterItemID = "";
                        if (columnConfig.containsKey("BankName")) {
                            bankName = recarr[(Integer) columnConfig.get("BankName")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(bankName)) {
                                KwlReturnObject returnObject = importDao.getBankNameMasterItemName(companyid, bankName);
                                if (returnObject.getEntityList().isEmpty()) {
                                    failureMsg += "Incorrect Bank Name type value for Bank Name. Please add new Bank Name as \"" + bankName + "\" with other details.";
                                } else {
                                    MasterItem masterItem = (MasterItem) returnObject.getEntityList().get(0);
                                    bankNameMasterItemID = masterItem.getID();
                                }
                            } else {
                                if(payMethod.getDetailType()==Constants.bank_detail_type){
                                    failureMsg +="Empty data found in Bank Name, cannot set empty data for Bank Name if Payment Method is selected as Bank.";
                                }
                            }
                        }
                        
                        /*11. Cheque Date*/
                        Date chequeDate = null;
                        if (columnConfig.containsKey("DueDate")) {
                            String chequeDateStr = recarr[(Integer) columnConfig.get("DueDate")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(chequeDateStr)) {
                                try {
                                    chequeDate = df.parse(chequeDateStr);
                                } catch (ParseException ex) {
                                    if (masterPreference.equals("1")) {//add empty case or default case
                                        chequeDate = preferences.getBookBeginningFrom();// In UI default is book begining date so here taking book beging date as default date
                                    } else {
                                        failureMsg += "Incorrect date format for Cheque Date, Please specify values in " + dateFormat + " format.";
                                    }
                                }
                            }
                        }
                        
                        /*
                         * 12. Payment Status
                         */
                        boolean cleared = false;
                        if (columnConfig.containsKey("PaymentStatus")) {
                            String paymentStatusStr = recarr[(Integer) columnConfig.get("PaymentStatus")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(paymentStatusStr)) {
                                cleared = false;
                            } else {
                                if (!(paymentStatusStr.equalsIgnoreCase("Uncleared") || paymentStatusStr.equalsIgnoreCase("Cleared"))) {
                                    failureMsg += "Incorrect Payment Status type value for Payment Status. It should be either Cleared or Uncleared.";
                                }
                                if (paymentStatusStr.equalsIgnoreCase("Cleared")) {
                                    cleared = true;
                                }
                            }
                        } else {
                            failureMsg += "Payment Status is not available";
                        }
                        
                         
                        /*
                         * 13. Clearence Date
                         */
                        Date clearenceDate = null;
                        if (columnConfig.containsKey("ClearenceDate")) {
                            if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH && cleared) {// when payment type is other than cash and payment sttus is clear then only need of clerance date. So its validation
                                String clearenceDateStr = recarr[(Integer) columnConfig.get("ClearenceDate")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(clearenceDateStr)) {
                                    try {
                                        clearenceDate = df.parse(clearenceDateStr);
                                        if (chequeDate.compareTo(clearenceDate) > 0) {
                                            failureMsg += "Clearence date should be greter than Cheque date.";
                                        }
                                    } catch (ParseException ex) {
                                        failureMsg += "Incorrect date format for Clearence Date, Please specify values in " + dateFormat + " format.";
                                    }
                                } else {
                                    failureMsg += "You have entered the Payment Status as Cleared. So you cannot set empty data for Clearence Date.";
                                }
                            }
                        }

                        /*14. ReferenceNumber */
                        String referenceNumber = "";
                        if (columnConfig.containsKey("Description")) {
                            referenceNumber = recarr[(Integer) columnConfig.get("Description")].replaceAll("\"", "").trim();
                        }

                        /*15. Memo*/
                        String memo="";
                        if (columnConfig.containsKey("Memo")) {
                            memo = recarr[(Integer) columnConfig.get("Memo")].replaceAll("\"", "").trim();
                            
                            JSONObject configObj = configMap.get("Memo");
                            int maxLength = configObj.optInt("maxLength", 0);
                            String validationType = configObj.optString("validatetype");
                            if ("string".equalsIgnoreCase(validationType) && !StringUtil.isNullOrEmpty(memo) && memo.length() > maxLength) {
                                if (masterPreference.equalsIgnoreCase("0")) {
                                    failureMsg += "Data length greater than " + maxLength + " for column Memo.";
                                } else {// for other two cases need to trim data upto max length
                                    memo = memo.substring(0, maxLength);
                                }
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(failureMsg)) {
                            throw new AccountingException(failureMsg);
                        }
                        
                        
                        if (customFieldParamMap.isEmpty()) {
                            for (int K = 0; K < headArrayList.size(); K++) {
                                HashMap<String, Object> requestParamsCF = new HashMap<String, Object>();
                                requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                requestParamsCF.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId, headArrayList.get(K)));
                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParamsCF); // get custom field for module
                                FieldParams params = null;
                                if (fieldParamsResult.getRecordTotalCount() > 0) {
                                    params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                    customFieldParamMap.put(headArrayList.get(K).toString(), params);
                                }
                            }
                        }
                        
                        HashMap receipthm = new HashMap();
                        receipthm.put("entrynumber", invoiceNumber);
                        receipthm.put("autogenerated", false);


                        receipthm.put("depositamount", transactionAmount);//
                        receipthm.put("currencyid", currencyId);//
                        receipthm.put("externalCurrencyRate", externalCurrencyRate);//
                        receipthm.put("memo", memo);//
                        receipthm.put("companyid", companyid);//
                        receipthm.put("creationDate", transactionDate);//
                        receipthm.put("customerId", customerId);//
                        receipthm.put("accountId", accountId);//
                        receipthm.put("isOpeningBalenceReceipt", true);//
                        receipthm.put("normalReceipt", false);//
                        receipthm.put("openingBalanceAmountDue", transactionAmount);//
                        receipthm.put("isadvancepayment", true);
                        receipthm.put("contraentry", false);
                        receipthm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);
                        receipthm.put("conversionRateFromCurrencyToBase", true);
                        // Store Receipt amount in base currency
                        receipthm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
                        receipthm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
                        String createdby = sessionHandlerImpl.getUserid(request);
                        String modifiedby = sessionHandlerImpl.getUserid(request);
                        long createdon = System.currentTimeMillis();
                        long updatedon = System.currentTimeMillis();

                        receipthm.put("createdby", createdby);
                        receipthm.put("modifiedby", modifiedby);
                        receipthm.put("createdon", createdon);
                        receipthm.put("updatedon", updatedon);
                        
                        HashMap pdetailhm = new HashMap();
                        pdetailhm.put("paymethodid", payMethod.getID());
                        pdetailhm.put("companyid", companyid);

                        if (payMethod.getDetailType()!= PaymentMethod.TYPE_CASH) {
                            if (payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                                HashMap chequehm = new HashMap();
                                chequehm.put("chequeno", chequeNumber);
                                chequehm.put("companyId", companyid);
                                chequehm.put("createdFrom", 2);
                                chequehm.put("bankAccount", (payMethod.getAccount() != null) ? payMethod.getAccount().getID() : "");
                                chequehm.put("description", referenceNumber);
                                chequehm.put("bankname", bankName);
                                chequehm.put("duedate",chequeDate);
                                chequehm.put("bankmasteritemid", bankNameMasterItemID);
                                KwlReturnObject cqresult = accPaymentDAOobj.addCheque(chequehm);
                                Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
                                pdetailhm.put("chequeid", cheque.getID());
                            } 
                        }
                        KwlReturnObject pdresult = null;

                        pdresult = accPaymentDAOobj.addPayDetail(pdetailhm);
                        PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);

                        receipthm.put("paydetailsid", pdetail.getID());
                        KwlReturnObject result = accReceiptDAOobj.saveReceipt(receipthm);
                        Receipt receipt = (Receipt) result.getEntityList().get(0);
                        
                        // For creating custom field array
                        JSONArray customJArr = fieldDataManagercntrl.getCustomFieldForOeningTransactionsRecords(headArrayList, customFieldParamMap, recarr,columnConfig ,request);
                        customfield = customJArr.toString();

                        if (!StringUtil.isNullOrEmpty(customfield)) {
                            JSONArray jcustomarray = new JSONArray(customfield);
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_OpeningBalanceReceipt_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceReceiptid);
                            customrequestParams.put("modulerecid", receipt.getID());
                            customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            customrequestParams.put("customdataclasspath", Constants.Acc_OpeningBalanceReceipt_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                receipthm.put("receiptid", receipt.getID());
                                receipthm.put("openingBalanceReceiptCustomData", receipt.getID());
                                result = accReceiptDAOobj.saveReceipt(receipthm);
                            }
                        }
                        if (cleared) {
                            Date startDate = preferences.getFinancialYearFrom();
                            Calendar startCal = Calendar.getInstance();
                            startCal.setTime(startDate);
                            startCal.add(Calendar.YEAR, 1);
                            startCal.add(Calendar.DAY_OF_YEAR, -1);
                            Date endDate = startCal.getTime();
                            String bankAccountId = payMethod.getAccount().getID();
                            
                            Map<String, Object> bankReconsilationMap = new HashMap<>();
                            bankReconsilationMap.put("isOpeningPayment", true);
                            bankReconsilationMap.put("bankAccountId", bankAccountId);
                            bankReconsilationMap.put("startDate", startDate); //Financial Year Start Date
                            bankReconsilationMap.put("endDate", endDate); //Financial Year End Date
                            bankReconsilationMap.put("clearanceDate", clearenceDate);
                            bankReconsilationMap.put("endingAmount", 0.0);
                            bankReconsilationMap.put("companyId", companyid);
                            bankReconsilationMap.put("clearingamount", receipt.getDepositAmount());
                            bankReconsilationMap.put("currencyid", currencyId);
                            bankReconsilationMap.put("details", "");
                            bankReconsilationMap.put("receipt", receipt);
                            bankReconsilationMap.put("ismultidebit", true);
                            bankReconsilationMap.put("createdby", sessionHandlerImpl.getUserid(request));
                            bankReconsilationMap.put("checkCount", 0);      //As the discussion with Mayur B. and Sagar A. sir MP relates to check count
                            bankReconsilationMap.put("depositeCount", 1);

                            HashMap<String, Object> globalParams = AccountingManager.getGlobalParams(request);
                            saveBankReconsilation(bankReconsilationMap, globalParams);
                            auditTrailObj.insertAuditLog(AuditAction.BANK_RECONCILIATION_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " has reconciled " + receipt.getReceiptNumber(), request, companyid);
                        }

                    } catch (Exception ex) {
                        failed++;
                        String errorMsg = ex.getMessage();
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
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
//                issuccess = false;
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
            } else if (success == total) {
                msg = messageSource.getMessage("acc.rem.168", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request)) + success + messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + messageSource.getMessage("acc.field.successfully", null, RequestContextUtils.getLocale(request));
                msg += (failed == 0 ? "." : messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request)) + failed + messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
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

            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException(messageSource.getMessage("acc.import.msg9", null, RequestContextUtils.getLocale(request)));
        } finally {
            fileInputStream.close();
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
                logDataMap.put("Module", Constants.Acc_Receive_Payment_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }

    public static String getActualFileName(String storageName) {
        String ext = storageName.substring(storageName.lastIndexOf("."));
        String actualName = storageName.substring(0, storageName.lastIndexOf("_"));
        actualName = actualName + ext;
        return actualName;
    }

    public String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 0; i < listArray.length; i++) {    //Discard columns id at index 0 and isvalid,invalidColumns, validationlog at last 3 indexes.
//            String s = (listArray[i]==null)?"":listArray[i].toString();
            rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString().replaceAll("\"", "")) + "\",";
        }
        return rec;
    }

    public void createFailureFiles(String filename, StringBuilder failedRecords, String ext) {
        String destinationDirectory;
        try {
            destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";

            if (!StringUtil.isNullOrEmpty(filename.substring(filename.lastIndexOf(".")))) {
                ext = filename.substring(filename.lastIndexOf("."));
            }

//            if (StringUtil.isNullOrEmpty(ext)) {
//                ext = filename.substring(filename.lastIndexOf("."));
//            }
            filename = filename.substring(0, filename.lastIndexOf("."));

            java.io.FileOutputStream failurefileOut = new java.io.FileOutputStream(destinationDirectory + "/" + filename + ImportLog.failureTag + ext);
            failurefileOut.write(failedRecords.toString().getBytes());
            failurefileOut.flush();
            failurefileOut.close();
        } catch (Exception ex) {
            System.out.println("\nError file write [success/failed] " + ex);
        }
    }

    public List saveReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        KwlReturnObject result=null;
        Receipt receipt = null;
        String oldjeid = null;
        String Cardid = null;
        String netinword = "";
        double amount = 0;
        List ll = new ArrayList();
        GoodsReceipt greceipt = null;
        Customer cust = null;
        Vendor vend = null;
        try {
            Account dipositTo = null;
            double amountDiff = 0;
            boolean rateDecreased = false;
            String sequenceformat = request.getParameter("sequenceformat");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String entryNumber = request.getParameter("no");
            String customfield = request.getParameter("customfield");
            String receiptid = request.getParameter("billid");
            boolean otherwise = ((request.getParameter("otherwise") != null) ? Boolean.parseBoolean(request.getParameter("otherwise")) : false);
            String methodid = request.getParameter("pmtmethod");        
            String advancePaymentIdForCnDn =request.getParameter("advancePaymentIdForCnDn");
            String mainPaymentForCNDNId =request.getParameter("mainPaymentForCNDNId");
            String invoiceadvcndntype =request.getParameter("invoiceadvcndntype");
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);            
            boolean isMultiDebit = StringUtil.getBoolean(request.getParameter("ismultidebit"));
            boolean isAdvancePayment = StringUtil.getBoolean(request.getParameter("isadvpayment"));
            boolean isadvanceFromVendor = StringUtil.getBoolean(request.getParameter("isadvanceFromVendor"));
            boolean isCNDN = StringUtil.getBoolean(request.getParameter("isCNDN"));
            boolean isAgainstDN = StringUtil.getBoolean(request.getParameter("isAgainstDN"));
            boolean ignoreDuplicateChk = StringUtil.getBoolean(request.getParameter("ignoreDuplicateChk"));
            int receiptType = StringUtil.getInteger(request.getParameter("receipttype"));
            int actualReceiptType = StringUtil.getInteger(request.getParameter("actualReceiptType") != null ? request.getParameter("actualReceiptType") : "0");
            boolean isReceiptPaymentEdit = (Boolean.parseBoolean((String) request.getParameter("isReceiptEdit")));
            String drAccDetails = request.getParameter("detail");
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
            HashMap receipthm = new HashMap();
            receipthm.put("isadvancepayment", isAdvancePayment);
            receipthm.put("isadvanceFromVendor", isadvanceFromVendor);
            
            int advanctype=request.getParameter("advanceAmountType") != null ? Integer.parseInt(request.getParameter("advanceAmountType")) : 0;
            
            if (isAdvancePayment && advanctype == 1) {// for Local Sales for Malasian company
                KwlReturnObject ObjReturnObject = accAccountDAOobj.getTaxFromCode(companyid, Constants.MALAYSIAN_GST_SR_TAX_CODE);
                Tax tax = (Tax) ObjReturnObject.getEntityList().get(0);

//                HashMap<String, Object> taxMap = new HashMap<String, Object>();
//
//                taxMap.put("taxAccountId", account.getID());
//
//                KwlReturnObject taxObj = accTaxObj.getTax(taxMap);
//                Tax tax = (Tax) taxObj.getEntityList().get(0);
                
                receipthm.put("tax", tax.getID());
            }
            
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            receipthm.put("advanceamounttype", !StringUtil.isNullOrEmpty((String)request.getParameter("advanceAmountType")) ? Integer.parseInt(request.getParameter("advanceAmountType")) : 0);
            receipthm.put("isLinkedToClaimedInvoice", !StringUtil.isNullOrEmpty(request.getParameter("isLinkedToClaimedInvoice")) ? Boolean.parseBoolean(request.getParameter("isLinkedToClaimedInvoice")):false);
            
            if(!StringUtil.isNullOrEmpty(advancePaymentIdForCnDn)){
                receipthm.put("advancePaymentIdForCnDn", advancePaymentIdForCnDn);
            }
            if(!StringUtil.isNullOrEmpty(mainPaymentForCNDNId)){
                receipthm.put("mainPaymentForCNDNId", mainPaymentForCNDNId);
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("isEdit"))){
                receipthm.put("isEdit", Boolean.parseBoolean(request.getParameter("isEdit")));
            }
            if(!StringUtil.isNullOrEmpty(invoiceadvcndntype)&&(actualReceiptType==0||actualReceiptType==1)){
                receipthm.put("invoiceadvcndntype", Integer.parseInt(invoiceadvcndntype));
            }
            HashMap<Integer,String> paymentHashMap=new HashMap<Integer, String>();
            if(!StringUtil.isNullOrEmpty(request.getParameter("datainvoiceadvcndn"))){
                JSONArray jSONArray=new JSONArray(request.getParameter("datainvoiceadvcndn"));
                String paymentid = "";
                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONObject jObject = jSONArray.getJSONObject(i);
                    paymentid = StringUtil.DecodeText(jObject.optString("paymentID"));
                    int invoiceadvcndntypejson=!StringUtil.isNullOrEmpty(jObject.getString("invoiceadvcndntype"))?Integer.parseInt(jObject.getString("invoiceadvcndntype")):0;
                    paymentHashMap.put(invoiceadvcndntypejson, paymentid);
                }
                receipthm.put("paymentHashMap", paymentHashMap);
            }
            boolean ismanydbcr = StringUtil.getBoolean(request.getParameter("ismanydbcr"));
            receipthm.put("ismanydbcr", ismanydbcr);
            receipthm.put("receipttype", receiptType);
            receipthm.put("actualReceiptType", actualReceiptType);
            double bankCharges = 0;
            double bankInterest = 0;
            String accountId = request.getParameter("accid");
            String bankChargesAccid = request.getParameter("bankChargesCmb");
            String bankInterestAccid = request.getParameter("bankInterestCmb");
            boolean onlyAdvance = StringUtil.getBoolean(request.getParameter("onlyAdvance"));
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            String person = "";
            if (receiptType == 1) {
                person = " Against Customer Invoice ";
            }

            StringBuffer billno = new StringBuffer();
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(request.getParameter("bankCharges"));
                receipthm.put("bankCharges", bankCharges);
                receipthm.put("bankChargesCmb", bankChargesAccid);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(request.getParameter("bankInterest"));
                receipthm.put("bankInterest", bankInterest);
                receipthm.put("bankInterestCmb", bankInterestAccid);
            }
            String paidToid = request.getParameter("paidToCmb");
            if (!StringUtil.isNullOrEmpty(request.getParameter("paidToCmb")) && !StringUtil.isNullOrEmpty(paidToid)) {
                receipthm.put("paidToCmb", paidToid);
            }
            if (receiptType == 6) {
                receipthm.put("vendor", request.getParameter("accid"));
            } else {
                boolean isCustomer = false;
                KwlReturnObject custObj = accountingHandlerDAOobj.getObject(Customer.class.getName(), request.getParameter("accid"));
                if (custObj.getEntityList().get(0) != null) {
                    cust = (Customer) custObj.getEntityList().get(0);
                }
                if (cust != null) {
                    isCustomer = true;
                }

                boolean isVendor = false;
                KwlReturnObject vendObj = accountingHandlerDAOobj.getObject(Vendor.class.getName(), request.getParameter("accid"));
                if (vendObj.getEntityList().get(0) != null) {
                    vend = (Vendor) vendObj.getEntityList().get(0);
                }
                if (vend != null) {
                    isVendor = true;
                }

                if (isCustomer) {
                    receipthm.put("customerId", request.getParameter("accid"));
                } else if (isVendor) {
                    receipthm.put("vendor", request.getParameter("accid"));
                }
            }

            boolean bankReconsilationEntry = false;
            boolean bankPayment = false;
            Date clearanceDate = null, startDate = null, endDate = null;
            Date creationDate = df.parse(request.getParameter("creationdate"));
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
                String dateafterPrefix = "";
                String dateSuffix = "";
                String prevseqnumber = "";

                if (!StringUtil.isNullOrEmpty(receiptid)) {  // for edit case
                    String advanceId = receipt.getAdvanceid() != null ? receipt.getAdvanceid().getID() : "";
                        result = accReceiptDAOobj.getDuplicateForNormalReceipt(entryNumber, companyid, receiptid, advanceId,receipt);
                    int count = result.getRecordTotalCount();
                    if (count > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
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
                }else if(paymentHashMap.containsKey(3)&&!paymentHashMap.containsKey(1)){
                    String cndnId=paymentHashMap.get(3);
                    KwlReturnObject cndnresult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), cndnId);
                    Receipt receiptCnDn=null;
                    int count=0;
                    if (!cndnresult.getEntityList().isEmpty() && cndnresult.getEntityList().get(0) != null) {
                        receiptCnDn = (Receipt) cndnresult.getEntityList().get(0);
                        result = accReceiptDAOobj.getDuplicateForNormalReceipt(entryNumber, companyid, receiptid, cndnId, receiptCnDn);
                        count = result.getRecordTotalCount();
                    }
                    if (count > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    } else if(receiptCnDn!=null && receiptCnDn.getSeqformat()!=null){                             
                            prevSeqFormat=receiptCnDn.getSeqformat();
                            prevseqnumber=receiptCnDn.getSeqnumber()+"";
                            receipthm.put(Constants.SEQFORMAT, prevSeqFormat.getID());
                            receipthm.put(Constants.SEQNUMBER, prevseqnumber);
                            receipthm.put(Constants.DATEPREFIX, receiptCnDn.getDatePreffixValue());
                            receipthm.put(Constants.DATEAFTERPREFIX, receiptCnDn.getDateAfterPreffixValue());
                            receipthm.put(Constants.DATESUFFIX, receiptCnDn.getDateSuffixValue());
                            nextAutoNumber = entryNumber;                        
                    }
                 } else if (!ignoreDuplicateChk && (actualReceiptType != 0 || (actualReceiptType == 0 && isAdvancePayment))) {//true when advance created along with payment against invoice
                    if (actualReceiptType == 0 && isAdvancePayment && request.getParameter("data") != null) {
                        JSONArray jSONArray = new JSONArray(request.getParameter("data"));
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
                                editAdvance = true;
                            }
                        result = accReceiptDAOobj.getDuplicateForNormalReceipt(entryNumber, companyid, advReceiptId,"",advreceipt);
                        }
                    } else {
                        result = accReceiptDAOobj.getReceiptFromBillNo(entryNumber, companyid);
                    }
                    int count = result.getRecordTotalCount();
                    if (count > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                }
                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                    List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Receive_Payment_ModuleId, entryNumber, companyid);
                    if (!resultList.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                        String formatName = (String) resultList.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                }

                if (!sequenceformat.equals("NA") && prevSeqFormat == null && !ignoreDuplicateChk) { //to generate sequence number
                    boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                    if (seqformat_oldflag) {
                        nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat);
                    } else {
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat, seqformat_oldflag, creationDate);
                        nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                        datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        receipthm.put(Constants.SEQFORMAT, sequenceformat);
                        receipthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                        receipthm.put(Constants.DATEPREFIX, datePrefix);
                        receipthm.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
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

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);


            receipthm.put("currencyid", currencyid);
            receipthm.put("externalCurrencyRate", externalCurrencyRate);
            KwlReturnObject payresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) payresult.getEntityList().get(0);

            dipositTo = payMethod.getAccount();
            HashMap pdetailhm = new HashMap();
            pdetailhm.put("paymethodid", payMethod.getID());
            pdetailhm.put("companyid", company.getCompanyID());
            if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH) {

                JSONObject obj = new JSONObject(request.getParameter("paydetail"));
                if (payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                    bankPayment = true;
                    bankReconsilationEntry = obj.getString("paymentStatus") != null ? obj.getString("paymentStatus").equals("Cleared") : false;
                    if (bankReconsilationEntry) {
                        bankAccountId = request.getParameter("bankaccid");
                        startDate = df.parse(request.getParameter("startdate"));
                        endDate = df.parse(request.getParameter("enddate"));
                        clearanceDate = df.parse(obj.getString("clearanceDate"));
                        bankReconsilationMap.put("bankAccountId", bankAccountId);
                        bankReconsilationMap.put("startDate", startDate);
                        bankReconsilationMap.put("endDate", endDate);
                        bankReconsilationMap.put("clearanceDate", clearanceDate);
                        bankReconsilationMap.put("endingAmount", 0.0);
                        bankReconsilationMap.put("companyId", companyid);
                    }
                    HashMap chequehm = new HashMap();
                    chequehm.put("chequeno", obj.optString("chequeno"));
                    chequehm.put("description",StringUtil.DecodeText(obj.optString("description")) );
                    chequehm.put("bankname",StringUtil.DecodeText(obj.optString("bankname")));
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

            receipthm.put("memo", request.getParameter("memo"));
            receipthm.put("companyid", company.getCompanyID());

            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
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
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, creationDate);
                    jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
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
            jeDataMap.put("entrydate", creationDate);
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", request.getParameter("memo"));
            jeDataMap.put("currencyid", currencyid);
            HashSet jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            String detail = request.getParameter("detail");
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
                    double amountDiffforInv = oldReceiptRowsAmount(request, jArray, currencyid, externalCurrencyRate);
                    rateDecreased = false;
                    if (amountDiffforInv < 0) {
                        rateDecreased = true;
                    }
                    amount += jobj.getDouble("payment");
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", jobj.getDouble("payment") - amountDiffforInv);
                    jedjson.put("accountid", jobj.get("accountid"));
                    jedjson.put("gstCurrencyRate",jobj.optDouble("gstCurrencyRate",0.0));
                    if(jobj.optDouble("gstCurrencyRate",0.0)!=0.0)
                        jeDataMap.put("gstCurrencyRate", jobj.optDouble("gstCurrencyRate",0.0));
                    jedjson.put("forexGainLoss",-1*amountDiffforInv);
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
                    if ((!jobj.getString("billno").equalsIgnoreCase("undefined")) && (!jobj.getString("billno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("billno") + ",");
                    }
                }

                amountDiff = oldReceiptRowsAmount(request, jArr, currencyid, externalCurrencyRate);

                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, RequestContextUtils.getLocale(request)));
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
                amount = Double.parseDouble(request.getParameter("amount"));
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
                    int advanceamounttype=request.getParameter("advanceAmountType") != null ? Integer.parseInt(request.getParameter("advanceAmountType")) : 0;
                    if(isAdvancePayment && advanceamounttype > 0 && company.getCountry().getID().equals("137")){ //if advance and Malaysia Country
                        double totalAmount=Double.parseDouble(jobj.getString("dramount"));
                        double typeAmountTax=0;
                        String accountID="";
                        if(advanceamounttype ==1){//for Local Tax
                             KwlReturnObject ObjReturnObject = accAccountDAOobj.getAccountFromName(companyid, "GST(SR)");
                             Account account =(Account)ObjReturnObject.getEntityList().get(0);
                             
                             KwlReturnObject taxReturnObject = accTaxObj.getTaxFromAccount(account.getID(),companyid);
                             Tax tax = (Tax) taxReturnObject.getEntityList().get(0);
                             
                             KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, df.parse(request.getParameter("creationdate")), tax.getID());
                             double percentage = (Double) perresult.getEntityList().get(0);
                             
                             if(account!=null)accountID=account.getID();
                              typeAmountTax=(totalAmount*percentage)/(100+percentage);
                              typeAmountTax=authHandler.roundUnitPrice(typeAmountTax, companyid);
                        }else if(advanceamounttype ==2){//For Export Tax
                            
                            KwlReturnObject ObjReturnObject = accAccountDAOobj.getAccountFromName(companyid, "GST(ZRE)");
                            Account account =(Account)ObjReturnObject.getEntityList().get(0);
                            if(account!=null)accountID=account.getID();
                            typeAmountTax=0;
                            typeAmountTax=authHandler.roundUnitPrice(typeAmountTax, companyid);
                        }
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount",(totalAmount-typeAmountTax));
                        jedjson.put("accountid", jobj.getString("accountid"));
                        jedjson.put("debit", isdebit);//false);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description",URLEncoder.encode(jobj.getString("description"),StaticValues.ENCODING));
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JEdetailID = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(JEdetailID);
                        
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", typeAmountTax);
                        jedjson.put("accountid", !StringUtil.isNullOrEmpty(accountID)?accountID:jobj.getString("accountid"));
                        jedjson.put("debit", isdebit);//false);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description", URLEncoder.encode(jobj.getString("description"),StaticValues.ENCODING));
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JEdetailID = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(JEdetailID);
                        
                    }else{//Normal Flow
                        
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", Double.parseDouble(jobj.getString("dramount")));
                        jedjson.put("accountid", jobj.getString("accountid"));
                        jedjson.put("debit", isdebit);//false);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description", URLEncoder.encode(jobj.getString("description"),StaticValues.ENCODING));
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JEdetailID = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(JEdetailID);
                        
                    }
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
                            receiptdetailotherwise.put("description",URLEncoder.encode(jobj.getString("description"),StaticValues.ENCODING));
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
                            jedjson.put("description", URLEncoder.encode(jobj.getString("description"),StaticValues.ENCODING));
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
                            receiptdetailotherwise.put("description",URLEncoder.encode(jobj.getString("description"),StaticValues.ENCODING));
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
                HashMap<String, Object> globalParams = AccountingManager.getGlobalParams(request);
                saveBankReconsilation(bankReconsilationMap, globalParams);
            }
            if (bankPayment && !bankReconsilationEntry && !StringUtil.isNullOrEmpty(oldjeid)) {
                bankReconsilationMap.put("oldjeid", oldjeid);
                bankReconsilationMap.put("companyId", companyid);
                deleteBankReconcilation(bankReconsilationMap);
            }

            if (isAdvancePayment && !StringUtil.isNullOrEmpty(request.getParameter("mainpaymentid"))) {//Link advance payments id with main payment id
                receipthm.clear();
                receipthm.put("receiptid", request.getParameter("mainpaymentid"));
                receipthm.put("advanceid", receipt.getID());
                receipthm.put("advanceamount", request.getParameter("advanceamt") != null ? Double.parseDouble(request.getParameter("advanceamt")) : 0);
//                receipthm.put("advanceamounttype", !StringUtil.isNullOrEmpty((String)request.getParameter("advanceAmountType")) ? Integer.parseInt(request.getParameter("advanceAmountType")) : 0);
                result = accReceiptDAOobj.saveReceipt(receipthm);
            }

            if (isCNDN) {
                String AccDetailsarrStr = request.getParameter("detailForCNDN");
                JSONArray drAccArr = new JSONArray(AccDetailsarrStr);
                
                 if (!isAgainstDN) {
                     String paymentId=receipt.getID();
                     KwlReturnObject cnhistoryresult = accReceiptDAOobj.getCustomerDnPaymenyHistory("", 0.0, 0.0, paymentId);
                        List<DebitNotePaymentDetails> dnHistoryList=cnhistoryresult.getEntityList();
                         for (DebitNotePaymentDetails dnpd:dnHistoryList) {  
                            String dnnoteid = dnpd.getDebitnote().getID()!=null?dnpd.getDebitnote().getID():"";
                            Double dnpaidamount=dnpd.getAmountPaid();
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
                    person=" Against debit note ";
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

            netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(amount)), currency,countryLanguageId);
            String action = "made";
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(request.getParameter("isCopyReceipt")) ? false : Boolean.parseBoolean(request.getParameter("isCopyReceipt"));
            if (isEdit == true && isCopy == false) {
                action = "updated";
            }
            if (billno.length() > 0) {
                billno.deleteCharAt(billno.length() - 1);
            }
            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User "+sessionHandlerImpl.getUserFullName(request) +" has "+action+" a receipt "+receipt.getReceiptNumber()+person+billno , request, receipt.getID());
            if (jArr.length() > 0 && !isMultiDebit) {
                double finalAmountReval = 0;
                String basecurrency = sessionHandlerImpl.getCurrencyID(request);
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
                    GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    GlobalParams.put("gcurrencyid", basecurrency);
                    GlobalParams.put("dateformat", authHandler.getDateOnlyFormat(request));
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
                    invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
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
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveReceipt : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveReceipt : " + ex.getMessage(), ex);
        }
        ll.add(new String[]{oldjeid, Cardid});
        KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), request.getParameter("accid"));
        Account account = (Account) accresult.getEntityList().get(0);
        String accountName = "";
        String accountaddress = "";
        String customerName = "";
        String accountid = request.getParameter("accid");
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

    public String PostJEFORReevaluation(HttpServletRequest request, double finalAmountReval, String companyid, CompanyAccountPreferences preferences, String basecurrency) {
        String jeid = "";
        try {
            String jeentryNumber = "";
            String jeSeqFormatId = "";
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            boolean jeautogenflag = false;
            DateFormat df = authHandler.getDateOnlyFormat(request);
            /**
             * added Link Date to Realised JE. while link Advanced Payment to
             * Reevaluated Invoice.
             */
            String creationDate = !StringUtil.isNullObject(request.getParameter("linkingdate")) ? request.getParameter("linkingdate") : request.getParameter("creationdate");
            Date entryDate = StringUtil.isNullOrEmpty(creationDate) ? new Date() : df.parse(creationDate);
            synchronized (this) {
                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                JEFormatParams.put("companyid", companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
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
            jeDataMapReval.put("entrydate", entryDate);
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
            setDimensionForRevalJEDetail(lineleveldimensions, jed);
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
            setDimensionForRevalJEDetail(lineleveldimensions, jed);
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
    
    /**
     * Description :This method is used to save Dimension For Reval JEDetail
     */
    public void setDimensionForRevalJEDetail(String lineleveldimensions, JournalEntryDetail jed) {
        try {
            if (!StringUtil.isNullOrEmpty(lineleveldimensions)) {
                JSONArray jcustomarray = new JSONArray(lineleveldimensions);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", jed.getID());
                customrequestParams.put("recdetailId", jed.getID());
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", jed.getCompany().getCompanyID());
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    //jed.setAccJEDetailCustomData((AccJEDetailCustomData) hibernateTemplate.get(AccJEDetailCustomData.class, jed.getID()));
                    JSONObject jedjsonreval = new JSONObject();
                    jedjsonreval.put("accjedetailcustomdata", jed.getID());
                    jedjsonreval.put("jedid", jed.getID());
                    accJournalEntryobj.updateJournalEntryDetails(jedjsonreval);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ModelAndView saveContraReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String JENumBer = "";
        String billno = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveContraReceipt(request);
            if (li.get(1) != null) {
                billno = li.get(1).toString();
            }
            if (li.get(2) != null) {
                JENumBer = li.get(2).toString();
            }
            issuccess = true;
            msg = messageSource.getMessage("acc.contra.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + ",</b>" + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + JENumBer + "</b>";   //"Receipt has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveContraReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        KwlReturnObject result;
        Receipt receipt = null;
        String oldjeid = null;
        String Cardid = null;
        List ll = new ArrayList();
        try {
            String maininvoiceid = request.getParameter("maininvoiceid");//Goods Receipt
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), maininvoiceid);
            String sequenceformat = request.getParameter("sequenceformat");
            GoodsReceipt greceipt = (GoodsReceipt) cmpresult.getEntityList().get(0);
            Account dipositTo = greceipt.getVendor().getAccount();


            double amount = 0;
            double amountDiff = 0;
//            boolean rateDecreased = false;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String entryNumber = request.getParameter("no");
            String receiptid = request.getParameter("billid");
//            boolean otherwise = ((request.getParameter("otherwise")!=null)?Boolean.parseBoolean(request.getParameter("otherwise")):false);
//            String methodid =request.getParameter("pmtmethod");
//            request.getSession().setAttribute("methodid", methodid);
            boolean isMultiDebit = StringUtil.getBoolean(request.getParameter("ismultidebit"));
            String drAccDetails = request.getParameter("detail");
            String jeid = null;
            boolean jeautogenflag = false;
//            String payDetailID=null;
            String jeentryNumber = null;
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            HashMap receipthm = new HashMap();
            String nextAutoNumber = "";
            Date creationDate = df.parse(request.getParameter("creationdate"));

            synchronized (this) {
                result = accReceiptDAOobj.getReceiptFromBillNo(entryNumber, companyid);
                int count = result.getRecordTotalCount();
                if (count > 0) {
                    if (sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Receiptnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                }
                boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));

                String nextAutoNoInt = "";
                String datePrefix = "";
                String dateafterPrefix = "";
                String dateSuffix = "";
                if (seqformat_oldflag) {
                    nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat);
                } else {
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat, seqformat_oldflag, creationDate);
                    nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    
                    receipthm.put(Constants.SEQFORMAT, sequenceformat);
                    receipthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                    receipthm.put(Constants.DATEPREFIX, datePrefix);
                    receipthm.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                    receipthm.put(Constants.DATESUFFIX, dateSuffix);
                }
                if (!sequenceformat.equals("NA")) {
                    entryNumber = nextAutoNumber;
                }
            }
            receipthm.put("entrynumber", entryNumber);

            receipthm.put("autogenerated", nextAutoNumber.equals(entryNumber));


            cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

//            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);


            receipthm.put("currencyid", currencyid);
            receipthm.put("externalCurrencyRate", externalCurrencyRate);
//            KwlReturnObject payresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
//            PaymentMethod payMethod = (PaymentMethod) payresult.getEntityList().get(0);

//            dipositTo = payMethod.getAccount();
//            HashMap pdetailhm = new HashMap();
//            pdetailhm.put("paymethodid", payMethod.getID());
//            pdetailhm.put("companyid", company.getCompanyID());
//            
//            KwlReturnObject pdresult=null;
//            if (!StringUtil.isNullOrEmpty(receiptid)&&!StringUtil.isNullOrEmpty(payDetailID))
//                pdetailhm.put("paydetailid", payDetailID);
//                pdresult = accPaymentDAOobj.addPayDetail(pdetailhm);
//            PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);
//            receipthm.put("paydetailsid", pdetail.getID());

            receipthm.put("memo", request.getParameter("memo"));
            receipthm.put("companyid", company.getCompanyID());
            
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put("companyid", companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false,creationDate);
                    jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
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
            jeDataMap.put("entrydate", creationDate);
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", "Contra Entry " + request.getParameter("memo"));
            jeDataMap.put("currencyid", currencyid);
            HashSet jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            String detail = request.getParameter("detail");
            JSONArray jArr = new JSONArray();
            if (!StringUtil.isNullOrEmpty(detail)) {
                jArr = new JSONArray(detail);
            }
            if (jArr.length() > 0 && !isMultiDebit) {
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    amount += jobj.getDouble("payment");
                }

//                amountDiff = oldReceiptRowsAmount(request, jArr, currencyid, externalCurrencyRate);
//
//                if (preferences.getForeignexchange() == null) {
//                    throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, RequestContextUtils.getLocale(request)));
//                }
//                if (amountDiff != 0 && preferences.getForeignexchange() != null) {
//                    if(amountDiff < 0) {
//                        rateDecreased = true;
//                    }
//                    JSONObject jedjson = new JSONObject();
//                    jedjson.put("srno", jedetails.size()+1);
//                    jedjson.put("companyid", companyid);
//                    jedjson.put("amount", rateDecreased?(-1*amountDiff):amountDiff);
//                    jedjson.put("accountid", preferences.getForeignexchange().getID());
//                    jedjson.put("debit", rateDecreased?false:true);
//                    jedjson.put("jeid", jeid);
//                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                    jedetails.add(jed);
//                }

            } else {
                amount = Double.parseDouble(request.getParameter("amount"));
            }
            JSONObject jedjson = null;
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed = null;
            if (isMultiDebit) {
                JSONArray drAccArr = new JSONArray(drAccDetails);
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", Double.parseDouble(jobj.getString("dramount")));
                    jedjson.put("accountid", jobj.getString("accountid"));
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    jedjson.put("description", jobj.getString("description"));
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            } else {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", amount + amountDiff);
                jedjson.put("accountid", request.getParameter("accid"));
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }

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
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            receipthm.put("journalentryid", journalEntry.getID());
            receipthm.put("contraentry", true);
            if (!StringUtil.isNullOrEmpty(receiptid)) {
                receipthm.put("receiptid", receipt.getID());
            }

            result = accReceiptDAOobj.saveReceipt(receipthm);
            receipt = (Receipt) result.getEntityList().get(0);
            receipthm.put("receiptid", receipt.getID());

            HashSet receiptDetails = new HashSet();
            if (!isMultiDebit)//To do - need to save vendor invoice no as well
            {
                receiptDetails = saveReceiptRows(receipt, company, jArr, greceipt);
            }
            receipthm.put("receiptdetails", receiptDetails);

            result = accReceiptDAOobj.saveReceipt(receipthm);
            receipt = (Receipt) result.getEntityList().get(0);

            //Insert new entries in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            //To do - make audit entry
//            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User "+sessionHandlerImpl.getUserFullName(request) +" created new contra entry for Invoice No.", request, receipt.getID());

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveReceipt : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveReceipt : " + ex.getMessage(), ex);
        }
        ll.add(new String[]{oldjeid, Cardid});
        ll.add(receipt.getReceiptNumber());
        ll.add(receipt.getJournalEntry().getEntryNumber());
        return (ArrayList) ll;
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
                soFar = tensNames[number % 10] + soFar;
                number /= 10;
            }
            if (number == 0) {
                return " And " + soFar + " " + val;
            }
            return " And " + numNames[number] + " " + val + soFar;
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
            tradThousand = convertLessThanOneThousand(thousands);
            result = result + tradThousand;
            String paises;
            switch (fractions) {
                case 0:
                    paises = "";
                    break;
                default:
                    paises = convertLessOne(fractions, currency);
            }
            result = result + paises; //to be done later
            result = result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
            if (isNegative) {
                result = "Minus " + result;
            }
//            result = result.substring(0, 1).toUpperCase() + result.substring(1).toLowerCase(); // Make first letter of operand capital.
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
    
        private void deleteBankReconcilationOfOpeningBalances(Map<String, Object> requestParams) throws ServiceException {
        if (requestParams.containsKey("transactionID")) {
            String reconsilationID = "";
            String unReconsilationID = "";
            String transactionID = (String) requestParams.get("transactionID");
            String companyid = (String) requestParams.get("companyid");
            
            //Deleting  BankReconciliationDetail
            KwlReturnObject reconsiledDetails = accBankReconciliationObj.getBRWithoutJE(transactionID, companyid, Constants.Acc_Receive_Payment_ModuleId);
            if (reconsiledDetails.getRecordTotalCount() > 0) {
                List<BankReconciliationDetail> brd = reconsiledDetails.getEntityList();
                for (BankReconciliationDetail reconciliation : brd) {
                    accBankReconciliationObj.permenantDeleteBankReconciliationDetail(reconciliation.getID(), companyid);
                    reconsilationID = reconciliation.getBankReconciliation().getID();
                }
            }

            //Deleting  BankUnreconciliationDetail
            KwlReturnObject unReconsiledDetails = accBankReconciliationObj.getBankUnReconsiledWithoutJE(transactionID, companyid, Constants.Acc_Receive_Payment_ModuleId);
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
        }
    }

    private void saveBankReconsilation(Map<String, Object> requestParams, Map<String, Object> globalParams) throws ServiceException, JSONException, UnsupportedEncodingException {
        HashMap<String, Object> brMap = new HashMap<String, Object>();
        KwlReturnObject crresult = accCurrencyobj.getCurrencyToBaseAmount(globalParams, (Double) requestParams.get("clearingamount"), (String) requestParams.get("currencyid"), (Date) requestParams.get("clearanceDate"), 0);
        double clearingAmount = (Double) crresult.getEntityList().get(0);
        boolean isOpeningPayment = false;
        HashSet hs = new HashSet();
        String billid = "";
        String jeid = "";
        double amount = 0;
        if (requestParams.containsKey("isOpeningPayment")) {
            isOpeningPayment = Boolean.parseBoolean(requestParams.get("isOpeningPayment").toString());
        }

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
        Receipt receipt = (Receipt) requestParams.get("receipt");
        String accountName = "";
        int moduleID = 0;
        if (!isOpeningPayment) {

            JournalEntry entry = receipt.getJournalEntry();
            Set details = entry.getDetails();
            Iterator iter = details.iterator();
            while (iter.hasNext()) {
                JournalEntryDetail d = (JournalEntryDetail) iter.next();
                if (d.isDebit()) {
                    continue;
                }
                accountName += d.getAccount().getName() + ", ";
            }

            //Calculate the Amount.
            JSONArray jArr = (JSONArray) requestParams.get("details");
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.optDouble("enteramount", 0) != 0) {
                    KwlReturnObject crresult1 = accCurrencyobj.getCurrencyToBaseAmount(globalParams, jobj.getDouble("enteramount"), jobj.getString("currencyid"), (Date) requestParams.get("clearanceDate"), 0);
                    double amt = (Double) crresult1.getEntityList().get(0);
                    if (jobj.optBoolean("debit", false)) {
                        amount -= amt;
                    } else {
                        amount += amt;
                    }
                }
            }
            jeid = entry.getID();
            billid = null;
            moduleID = Constants.Acc_GENERAL_LEDGER_ModuleId;
        } else {
            jeid = null;
            billid = receipt.getID();
            amount = clearingAmount;
            moduleID = Constants.Acc_Receive_Payment_ModuleId;
            accountName = "";
        }
        accountName = accountName.substring(0, Math.max(0, accountName.length() - 2));

        HashMap<String, Object> brdMap = new HashMap<>();
        brdMap.put("companyid", (String) requestParams.get("companyId"));
        brdMap.put("amount", amount);
        brdMap.put("jeid", jeid);
        brdMap.put("accountname", accountName);
        brdMap.put("debit", true);
        brdMap.put("brid", brid);
        brdMap.put("transactionID",billid);
        brdMap.put("moduleID", moduleID);
        brdMap.put("isOpeningTransaction",isOpeningPayment);
        KwlReturnObject brdresult = accBankReconciliationObj.addBankReconciliationDetail(brdMap);
        BankReconciliationDetail brd = (BankReconciliationDetail) brdresult.getEntityList().get(0);
        hs.add(brd);
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
                KwlReturnObject jedresult1 = accJournalEntryobj.deleteJECustomData(oldjeid);
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

    private double oldReceiptRowsAmount(HttpServletRequest request, JSONArray jArr, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException {
        double amount = 0, actualAmount = 0;
        KwlReturnObject result;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            Date creationDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("creationdate"));
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
                invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
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
                result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
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

        } catch (ParseException ex) {
            throw ServiceException.FAILURE("oldReceiptRowsAmount : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("oldReceiptRowsAmount : " + ex.getMessage(), ex);
        }
        return (actualAmount);
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
            rd.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate",0.0));
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

    /*
     * Update invoice due amount when payment is being made against that
     * invoice.
     */
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
            invjson.put(Constants.invoiceamountdueinbase, invoice.getInvoiceamountdue() - baseAmountReceivedForInvoice);
            accInvoiceDAOObj.updateInvoice(invjson, null);
        }
    }

    /*
     * Update receipt due amount when payment is being made.
     */
    public void updateReceiptAmountDue(Receipt receipt, Company company, double amountReceived, double baseAmountReceivedConverted) throws JSONException, ServiceException {
        if (receipt != null) {
            double receiptAmountDue = receipt.getOpeningBalanceAmountDue();
            receiptAmountDue -= amountReceived;
            HashMap receipthm = new HashMap();
            receipthm.put("openingBalanceAmountDue", receiptAmountDue);
            if(receipt.isIsOpeningBalenceReceipt()) {
                receipthm.put(Constants.openingBalanceBaseAmountDue, receipt.getOpeningBalanceBaseAmountDue() - baseAmountReceivedConverted);
            }
            receipthm.put("receiptid", receipt.getID());
            receipthm.put("currencyid", receipt.getCurrency().getCurrencyID());
            receipthm.put("companyid", company.getCompanyID());
            accReceiptDAOobj.saveReceipt(receipthm);
        }
    }

    public ModelAndView getOpeningBalanceReceipts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
            String customerId = request.getParameter("custVenId");
            requestParams.put("customerid", customerId);
            // get opening balance receipts created from opening balance button.
            KwlReturnObject result = accReceiptDAOobj.getOpeningBalanceReceipts(requestParams);
            List<Receipt> list = result.getEntityList();
            getOpeningBalanceReceiptJson(request, list, DataJArr);

//            // getting normal receipts of past year which has been converted into opening balance receipts.
//            result = accReceiptDAOobj.getOpeningBalanceNormalReceipts(requestParams);
//            list = result.getEntityList();
//            getOpeningBalanceReceiptJson(request, list, DataJArr);

            int count = result.getRecordTotalCount();
            JSONArray pagedJson = DataJArr;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }

            jobj.put("data", pagedJson);
            jobj.put("count", count);
            issuccess = true;
        } catch (JSONException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void getOpeningBalanceReceiptJson(HttpServletRequest request, List<Receipt> list, JSONArray dataArray) {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
//            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            SimpleDateFormat sdf = (SimpleDateFormat) authHandler.getGlobalDateFormat();
            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
            if (list != null && !list.isEmpty()) {
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    Receipt receipt = (Receipt) it.next();

                    Date receiptCreationDate = null;
                    Double receiptAmount = 0d;

                    receiptCreationDate = receipt.getCreationDate();
                    receiptAmount = receipt.getDepositAmount();

                    double exchangeRateForOtherCurrency = receipt.getExchangeRateForOpeningTransaction();
                    boolean isopeningBalanceRecceipt = receipt.isIsOpeningBalenceReceipt();

                    JSONObject receiptJson = new JSONObject();
                    receiptJson.put("methodid", receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod() == null ? "" : receipt.getPayDetail().getPaymentMethod().getID());
                    receiptJson.put("detailtype", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getDetailType()));
                    if (receipt.getPayDetail() != null) {
                        try {
                            receiptJson.put("expirydate", (receipt.getPayDetail().getCard() == null ? "" : df.format(receipt.getPayDetail().getCard().getExpiryDate())));
                        } catch (IllegalArgumentException ae) {
                            receiptJson.put("expirydate", "");
                        }
                        try {
                            receiptJson.put("dueDate", (receipt.getPayDetail().getCheque() == null ? "" : df.format(receipt.getPayDetail().getCheque().getDueDate())));
                        } catch (IllegalArgumentException ae) {
                            receiptJson.put("dueDate", "");
                        }
                        receiptJson.put("refdetail", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getDescription()) : receipt.getPayDetail().getCard().getCardType()));
                        receiptJson.put("refno", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getChequeNo()) : receipt.getPayDetail().getCard().getRefNo()));
                        receiptJson.put("refname", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : (receipt.getPayDetail().getCheque().getBankMasterItem() == null ? receipt.getPayDetail().getCheque().getBankName() : receipt.getPayDetail().getCheque().getBankMasterItem().getID())) : receipt.getPayDetail().getCard().getCardHolder()));
                        if (receipt.getPayDetail().getCard() != null) {
                            receiptJson.put("refcardno", receipt.getPayDetail().getCard().getCardNo());
                        }
                        receiptJson.put("clearanceDate", "");
                        receiptJson.put("paymentStatus", false);
                        
                        KwlReturnObject clearanceDate = accBankReconciliationObj.getBRWithoutJE(receipt.getID(), receipt.getCompany().getCompanyID(), Constants.Acc_Receive_Payment_ModuleId);
                        if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
                            BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
                            if (brd.getBankReconciliation().getClearanceDate() != null) {
                                receiptJson.put("clearanceDate", df.format(brd.getBankReconciliation().getClearanceDate()));
                                receiptJson.put("paymentStatus", true);
                            }
                        }
                    }
                    
                    receiptJson.put("transactionId", receipt.getID());
                    receiptJson.put("exchangeRateForOtherCurrency", exchangeRateForOtherCurrency);
                    receiptJson.put("isCurrencyToBaseExchangeRate", receipt.isConversionRateFromCurrencyToBase());
                    receiptJson.put("isNormalTransaction", receipt.isNormalReceipt());
                    receiptJson.put("transactionNo", receipt.getReceiptNumber());
                    receiptJson.put("transactionAmount", authHandler.formattedAmount(receiptAmount, companyid));
                    receiptJson.put("transactionDate", df.format(receiptCreationDate));
                    receiptJson.put("currencysymbol", (receipt.getCurrency() == null ? "" : receipt.getCurrency().getSymbol()));
                    receiptJson.put("currencyid", (receipt.getCurrency() == null ? "" : receipt.getCurrency().getCurrencyID()));
                    receiptJson.put("transactionAmountDue", authHandler.formattedAmount(receipt.getOpeningBalanceAmountDue(), companyid));
                    receiptJson.put("chequeNumber", receipt.getChequeNumber());
                    receiptJson.put("drawnOn", receipt.getDrawnOn());
                    receiptJson.put("chequeDate", receipt.getChequeDate() != null ? df.format(receipt.getChequeDate()) : "");
                    receiptJson.put("isWrittenOff", receipt.isIsWrittenOff());
                    receiptJson.put("memo", (!StringUtil.isNullOrEmpty(receipt.getMemo()) ? receipt.getMemo():""));
                    if((receipt.getLinkDetailReceipts()!=null&&!receipt.getLinkDetailReceipts().isEmpty()) || (receipt.getLinkDetailReceiptsToDebitNote()!=null && !receipt.getLinkDetailReceiptsToDebitNote().isEmpty())){
                        receiptJson.put("isPaymentLinked", true);
                    }else{
                        receiptJson.put("isPaymentLinked", false);
                    }
                    double transactionAmountInBase = 0d;
                    if (Constants.OpeningBalanceBaseAmountFlag) {
                        transactionAmountInBase = receipt.getOriginalOpeningBalanceBaseAmount();
                    } else {
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceRecceipt && receipt.isConversionRateFromCurrencyToBase()) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, receiptAmount, receipt.getCurrency().getCurrencyID(), receipt.getCreationDate(), exchangeRateForOtherCurrency);
                        } else {
                            bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, receiptAmount, receipt.getCurrency().getCurrencyID(), receipt.getCreationDate(), exchangeRateForOtherCurrency);
                        }

                        transactionAmountInBase = (Double) bAmt.getEntityList().get(0);
                    }
                    receiptJson.put("transactionAmountInBase", authHandler.formattedAmount(transactionAmountInBase, companyid));
                    int isReval=0; 
//                   if(invoiceReport){ 
                        KwlReturnObject brdAmt = accInvoiceDAOObj.getRevalFlag(receipt.getID());
                        List reval = brdAmt.getEntityList();
                        if(!reval.isEmpty() && (Long)reval.get(0) >0){
                            isReval=1;
                        }
//                   }
                    receiptJson.put("isreval", isReval);
                    dataArray.put(receiptJson);
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

    public ModelAndView getReceipts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        List<JSONObject> list = new ArrayList<JSONObject>();
        List<JSONObject> tempList = new ArrayList<JSONObject>();
        int limitValue = 0, startValue = 0, dataCount = 0;
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            boolean contraentryflag = request.getParameter("contraentryflag") != null;
            boolean isAdvancePayment = request.getParameter("advancePayment") != null;
            boolean isAdvanceFromVendor = request.getParameter("advanceFromVendor") != null;
            boolean isPostDatedCheque = request.getParameter("isPostDatedCheque") != null;
            boolean isDishonouredCheque = request.getParameter("isDishonouredCheque") != null;
            boolean isGlcode = request.getParameter("isGlcode") != null;
            String billid = request.getParameter("billid") != null ? request.getParameter("billid") : "";
            boolean onlyOpeningBalanceTransactionsFlag = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("onlyOpeningBalanceTransactionsFlag"))) {
                onlyOpeningBalanceTransactionsFlag = Boolean.parseBoolean(request.getParameter("onlyOpeningBalanceTransactionsFlag"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("recordType"))) {
                requestParams.put("receipttype", request.getParameter("recordType"));
            }
            requestParams.put("contraentryflag", contraentryflag);
            requestParams.put("isadvancepayment", isAdvancePayment);
            requestParams.put("isadvancefromvendor", isAdvanceFromVendor);
            requestParams.put("isPostDatedCheque", isPostDatedCheque);
            requestParams.put("isDishonouredCheque", isDishonouredCheque);
            requestParams.put("isGlcode", isGlcode);
            requestParams.put("billid", billid);
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            boolean eliminateflag = consolidateFlag;
//            if(consolidateFlag) {
            requestParams.put(Constants.start, "");
            requestParams.put(Constants.limit, "");
//            }
            KwlReturnObject result = null;
            KwlReturnObject openingBalanceReceiptsResult = null;
            KwlReturnObject billingResult = null;
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);

                tempList.clear();
                if (onlyOpeningBalanceTransactionsFlag) {
                    // getting opening balance receipts
                    openingBalanceReceiptsResult = accReceiptDAOobj.getAllOpeningBalanceReceipts(requestParams);
                    tempList = getOpeningBalanceReceiptJsonForReport(request, openingBalanceReceiptsResult.getEntityList(), tempList);
                } else {
                    result = accReceiptDAOobj.getReceipts(requestParams);
                    tempList = getReceiptJson(request, result.getEntityList(), tempList);

                    billingResult = accReceiptDAOobj.getBillingReceipts(requestParams);
                    tempList = getBillingReceiptJson(request, billingResult.getEntityList(), tempList);
                }


                Collections.sort(tempList, Collections.reverseOrder(new ReceiptDateComparator()));
                list.addAll(tempList);
            }

            if (!StringUtil.isNullOrEmpty(limit) && !StringUtil.isNullOrEmpty(start)) {
                limitValue = Integer.parseInt(limit);
                startValue = Integer.parseInt(start);
            } else {
                limitValue = list.size();
                startValue = 0;
            }
            Iterator iterator = list.iterator();
            for (int i = 0; i < list.size(); i++) {
                if (i >= startValue && dataCount < limitValue) {
                    JSONObject jSONObject = (JSONObject) iterator.next();
                    jArr.put(jSONObject);
                    dataCount++;
                } else {
                    iterator.next();
                }
                if (dataCount == limitValue) {
                    break;
                }
            }
            jobj.put("data", jArr);
            jobj.put("count", list.size());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE("CustomerManager.getReciepts", ex);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public static HashMap<String, Object> getReceiptRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("ss", request.getParameter("ss"));
        requestParams.put("start", request.getParameter("start"));
        requestParams.put("limit", request.getParameter("limit"));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        requestParams.put(Constants.REQ_startdate, request.getParameter("stdate"));
        requestParams.put(Constants.REQ_enddate, request.getParameter("enddate"));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(InvoiceConstants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        return requestParams;
    }

    public static HashMap<String, Object> getReceiptRequestMapJson(JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
        requestParams.put("ss", paramJobj.optString("ss",null));
        requestParams.put("start", paramJobj.optString("start",null));
        requestParams.put("limit", paramJobj.optString("limit",null));
        requestParams.put("deleted", paramJobj.optString("deleted",null));
        requestParams.put("nondeleted", paramJobj.optString("nondeleted",null));
        requestParams.put(Constants.REQ_startdate, paramJobj.optString("stdate",null));
        requestParams.put(Constants.REQ_enddate, paramJobj.optString("enddate",null));
        requestParams.put(Constants.Acc_Search_Json, paramJobj.optString(Constants.Acc_Search_Json,null));
        requestParams.put(Constants.Filter_Criteria, paramJobj.optString(InvoiceConstants.Filter_Criteria,null));
        requestParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));
        return requestParams;
    }
        
    public List<JSONObject> getOpeningBalanceReceiptJsonForReport(HttpServletRequest request, List list, List<JSONObject> jsonObjectlist) {
        List<JSONObject> returnList = new ArrayList<JSONObject>();
        try {
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String fileType = StringUtil.isNullOrEmpty(request.getParameter("filetype"))?"":request.getParameter("filetype");
            String companyid = sessionHandlerImpl.getCompanyid(request);
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
                obj.put("billdate", authHandler.getDateOnlyFormat(request).format(receipt.getCreationDate()));
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
                obj.put("detailtype", "");
                obj.put("paymentmethod", "");
                String jeNumber = "";
                String jeIds = "";
                if(receipt.getLinkDetailReceipts() != null && !receipt.getLinkDetailReceipts().isEmpty()) {
                    Set<LinkDetailReceipt> linkedDetailReceiptList = receipt.getLinkDetailReceipts();
                    for(LinkDetailReceipt ldprow : linkedDetailReceiptList) {
                        if(!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                            jeIds += "," + ldprow.getLinkedGainLossJE();
                            KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                            JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                            jeNumber += "," + linkedJEObject.getEntryNumber();
                        }
                    }
                }
                if(!StringUtil.isNullOrEmpty(jeIds)) {
                    jeIds = jeIds.substring(0, jeIds.length()-1);
                }
                if(!StringUtil.isNullOrEmpty(jeNumber)) {
                    jeNumber = jeNumber.substring(0, jeNumber.length()-1);
                }
                obj.put("entryno", jeNumber);
                obj.put("journalentryid",jeIds);
                double linkedAmountDue=receipt.getDepositAmount();
                if (!receipt.getRows().isEmpty()) {
                    linkedAmountDue=getReceiptAmountDue(receipt);
                        obj.put("otherwise",false);
                        obj.put("isLinked",true);
                } else {
                    obj.put("otherwise", true);
                }
                obj.put("paymentamountdue",authHandler.formattedAmount(linkedAmountDue, companyid));
                obj.put("paymentamountdueinbase", authHandler.formattedAmount(receipt.getOpeningBalanceBaseAmountDue(), companyid));
                if (StringUtil.equal(fileType, "print")||StringUtil.equal(fileType, "pdf") || StringUtil.equal(fileType, "csv")|| StringUtil.equal(fileType, "xls")) {//code under if condition only for print case. no need to execute for other cases.
                    String usedDocumentNumbers = getDocumentNumbersUsedInReceipt(receipt);
                    obj.put("useddocumentnumber", usedDocumentNumbers);
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

    public List<JSONObject> getReceiptJson(HttpServletRequest request, List list, List<JSONObject> jsonObjectlist) throws SessionExpiredException, ServiceException {
        //JSONObject jobj=new JSONObject();
        //JSONArray jArr=new JSONArray();        
        try {
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            HashMap<String, Object> requestParams = new HashMap();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = request.getParameter("gcurrencyid");
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", currencyid);
            String fileType = StringUtil.isNullOrEmpty(request.getParameter("filetype"))?"":request.getParameter("filetype");
            boolean isExport = false;
           if(request.getAttribute("isExport")!=null){
               isExport = (boolean) request.getAttribute("isExport");
           }
//            DecimalFormat f = new DecimalFormat("##.00");     
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
             int permCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
            KwlReturnObject extraPrefesult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) extraPrefesult.getEntityList().get(0);
            String userId = sessionHandlerImpl.getUserid(request);
            
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Receive_Payment_ModuleId,1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            HashMap<String, Object> fieldrequestParamsGlobalLevel = new HashMap();
            HashMap<String, String> customFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMapGlobalLevel = new HashMap<String, String>();
            fieldrequestParamsGlobalLevel.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParamsGlobalLevel.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId));
            HashMap<String, String> replaceFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, Integer> FieldMapGlobalLevel = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParamsGlobalLevel, replaceFieldMapGlobalLevel, customFieldMapGlobalLevel, customDateFieldMapGlobalLevel);

            Iterator itr = list.iterator();          
            while (itr.hasNext()) {
                JSONArray jArr1 = new JSONArray();
                Object[] row = (Object[]) itr.next();
                Receipt receipt = (Receipt) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
                obj.put("withoutinventory", false);
                Vendor vendor = null;
                if (receipt.getVendor() != null) {
                    KwlReturnObject vendResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                    vendor = (Vendor) vendResult.getEntityList().get(0);
                }
//                Customer customer = (Customer) session.get(Customer.class, acc.getID());
//                if (customer != null) {
//                    obj.put("address", customer.getBillingAddress());
//                } else {
//                    Vendor vendor = (Vendor) session.get(Vendor.class, acc.getID());
//                    if (vendor != null) {
//                        obj.put("address", vendor.getAddress());
//                    }
//                }
                String address = "";
//                KwlReturnObject cresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), acc.getID());
                Customer customer = receipt.getCustomer();//(Customer) cresult.getEntityList().get(0);
                 boolean customesDocumentincludeflag = true;
                boolean customersDocumentDonotIncludeFlag = false;
                if (!((permCode & Constants.CUSTOMER_VIEWALL_PERMCODE)==Constants.CUSTOMER_VIEWALL_PERMCODE) && customer != null && extraPref!=null  && extraPref.isEnablesalespersonAgentFlow()  && customer.isIsCusotmerAvailableOnlyToSalespersons() && !StringUtil.isNullOrEmpty(userId)) {
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
                if (customersDocumentDonotIncludeFlag && customesDocumentincludeflag) {
                    continue;
                }
                
                obj.put("personemail", customer == null ? "" : customer.getEmail());
                if (customer != null) {
                    address = customer.getBillingAddress();
                } else {

                    address = "";
                }
                obj.put("address", address);

                obj.put("billid", receipt.getID());
                obj.put("isOpeningBalanceTransaction", receipt.isIsOpeningBalenceReceipt());
                obj.put("isNormalTransaction", receipt.isNormalReceipt());
                obj.put("companyid", receipt.getCompany().getCompanyID());
                obj.put("companyname", receipt.getCompany().getCompanyName());
                String jeNumber=receipt.getJournalEntry().getEntryNumber();
                if(receipt.getJournalEntryForBankCharges()!=null){
                    jeNumber+=","+receipt.getJournalEntryForBankCharges().getEntryNumber();                    
                }
                if (receipt.getJournalEntryForBankInterest() != null) {
                    jeNumber += "," + receipt.getJournalEntryForBankInterest().getEntryNumber();                    
                }         
                if(receipt.getLinkDetailReceipts() != null && !receipt.getLinkDetailReceipts().isEmpty()) {
                    Set<LinkDetailReceipt> linkedDetailReceiptList = receipt.getLinkDetailReceipts();
                    for(LinkDetailReceipt ldprow : linkedDetailReceiptList) {
                        if(!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {                            
                            KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                            JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                            jeNumber += "," + linkedJEObject.getEntryNumber();
                        }
                    }
                }
                obj.put("entryno", jeNumber);
                obj.put("journalentryid", receipt.getJournalEntry().getID());
                obj.put("personid", (customer != null) ? customer.getID() : acc.getID());
//                obj.put("customervendorname", (customer!=null)? customer.getName() : (vendor!=null)? vendor.getName():"");
                obj.put("billno", receipt.getReceiptNumber());
                obj.put("isadvancepayment", receipt.isIsadvancepayment());
                obj.put("isadvancefromvendor", receipt.isIsadvancefromvendor());
                obj.put("ismanydbcr", receipt.isIsmanydbcr());
                obj.put("isprinted", receipt.isPrinted());
                obj.put("bankCharges", receipt.getBankChargesAmount());
                obj.put("bankChargesCmb", receipt.getBankChargesAccount() != null ? receipt.getBankChargesAccount().getID() : "");
                obj.put("bankInterest", receipt.getBankInterestAmount());
                obj.put("bankInterestCmb", receipt.getBankInterestAccount() != null ? receipt.getBankInterestAccount().getID() : "");
                obj.put("paidToCmb", receipt.getReceivedFrom() == null ? "" : receipt.getReceivedFrom().getID());
                obj.put("paidto", receipt.getReceivedFrom() != null ? receipt.getReceivedFrom().getValue() : "");  //to show recived from option in grid
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
                
                Receipt receiptObject=null;
                if(receipt.getInvoiceAdvCndnType()==2||receipt.getInvoiceAdvCndnType()==1){
                    receiptObject=accReceiptDAOobj.getReceiptObject(receipt);
                    if(receiptObject!=null)
                        obj.put("cndnid",receiptObject.getID());
                }else if(receipt.getInvoiceAdvCndnType()==3){
                    obj.put("cndnid",receipt.getID());
                }
                obj.put("invoiceadvcndntype",receipt.getInvoiceAdvCndnType());
                obj.put("cndnAndInvoiceId",!StringUtil.isNullOrEmpty(receipt.getCndnAndInvoiceId())?receipt.getCndnAndInvoiceId():"");
                obj.put("advanceUsed", advanceUsed);
                obj.put("advanceid", (receipt.getAdvanceid() != null && !receipt.getAdvanceid().isDeleted()) ? receipt.getAdvanceid().getID() : "");
                obj.put("advanceamount", receipt.getAdvanceamount());
                obj.put("advanceamounttype", receipt.getAdvanceamounttype());
//                obj.put("billdate", authHandler.getDateOnlyFormat(request).format(receipt.getJournalEntry().getEntryDate()));//receiptdate
                obj.put("billdate", authHandler.getDateOnlyFormat(request).format(receipt.getCreationDate()));//receiptdate
                Iterator itrRow = receipt.getRows().iterator();
                rRequestParams.clear();
                filter_names.clear();
                filter_params.clear();
                filter_names.add("receipt.ID");
                filter_params.add(receipt.getID());
                rRequestParams.put("filter_names", filter_names);
                rRequestParams.put("filter_params", filter_params);
                KwlReturnObject pdoresult = accReceiptDAOobj.getReceiptDetailOtherwise(rRequestParams);
                List<ReceiptDetailOtherwise> list1=pdoresult.getEntityList();
                Iterator pdoRow=list1.iterator(); 
                
                double amount=0,totaltaxamount=0,linkedAmountDue=0,amountDueInBase=0;//linkedAmountDue=receipt.getDepositAmount();;
                obj.put("disableOtherwiseLinking", true);
                if (receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty() && receipt.getCustomer() != null) {
                    for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                        linkedAmountDue = advanceDetail.getAmountDue();
//                        KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, linkedAmountDue, receipt.getCurrency().getCurrencyID(), receipt.getJournalEntry().getEntryDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                        KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, linkedAmountDue, receipt.getCurrency().getCurrencyID(), receipt.getCreationDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                        amountDueInBase = (Double) bAmt.getEntityList().get(0);
                        if (linkedAmountDue <= 0) {
                            obj.put("disableOtherwiseLinking", true);
                        } else {
                            obj.put("disableOtherwiseLinking", false);
                        }
                        totaltaxamount+=advanceDetail.getTaxamount();
                    }
                }
                
                if(!receipt.getRows().isEmpty()) {
                    amount=receipt.getDepositAmount();
//                    linkedAmountDue=getReceiptAmountDue(receipt);//payment amount due calculation 
                    if(pdoRow!=null && list1.size()>0){   
                        for (ReceiptDetailOtherwise receiptDetailOtherwise : list1) {
                            if (receipt.getID().equals(receiptDetailOtherwise.getReceipt().getID())) {
                                double taxamount = 0;
                                obj.put("isLinked", true);
                                if (receiptDetailOtherwise.getTax() != null) {
                                    taxamount = receiptDetailOtherwise.getTaxamount();
                                    totaltaxamount += taxamount;
                                }
                            }
                        }
                    }
                     Receipt mainReceipt=receipt.getAdvanceid();
                    if(receipt.isIsadvancepayment()){
                        obj.put("isLinked",true);//disabling edit for advance once linked  
                    }else if(!receipt.isIsadvancepayment()&&mainReceipt!=null&&mainReceipt.getRows()!=null&&!mainReceipt.getRows().isEmpty()){
                        obj.put("isLinked",true);//disabling the edit functionality for invoice payament entries in which advance is also created and that advance has been used against some other invoices.
                        obj.put("linkedadvanceMsgFlag",true);
                    }
                    obj.put("otherwise",false);
                }else if (pdoRow != null && list1.size() > 0) {
                    for (ReceiptDetailOtherwise receiptDetailOtherwise : list1) {
                        if (receipt.getID().equals(receiptDetailOtherwise.getReceipt().getID())) {
                            double taxamount = 0;
                            if (receiptDetailOtherwise.getTax() != null) {
                                taxamount = receiptDetailOtherwise.getTaxamount();
                                totaltaxamount += taxamount;
                            }
                            JSONObject obj1 = new JSONObject();
                            obj1.put("debitaccid", receiptDetailOtherwise.getAccount().getID());
                            obj1.put("debitamt", receiptDetailOtherwise.getAmount());
                            obj1.put("isdebit", receiptDetailOtherwise.isIsdebit());
                            obj1.put("desc", receiptDetailOtherwise.getDescription() != null ? receiptDetailOtherwise.getDescription() : "");
                            obj1.put("prtaxid", receiptDetailOtherwise.getTax() != null ? receiptDetailOtherwise.getTax().getID() : "");
                            obj1.put("taxamount", taxamount);
                            obj1.put("curamount", (receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount()));
                       //     if (receipt.isIsmanydbcr()) {                 // handle manycndn in new payment structure for against GL case only 
                            amount=receipt.getDepositAmount();
//                            if (receiptDetailOtherwise.isIsdebit()) {
//                                    amount -= Double.parseDouble(authHandler.formattedAmount((receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount())));
//                                } else {
//                                    amount += Double.parseDouble(authHandler.formattedAmount((receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount())));
//                                }
//                            } else {
//                                amount += Double.parseDouble(authHandler.formattedAmount((receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount())));
//                            }
                            // ## Get Custom Field Data 
                            Map<String, Object> variableMap = new HashMap<String, Object>();
                            HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                            ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                            Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                            Detailfilter_params.add(receiptDetailOtherwise.getID());
                            invDetailRequestParams.put("filter_names", Detailfilter_names);
                            invDetailRequestParams.put("filter_params", Detailfilter_params);
                            KwlReturnObject idcustresult = accReceiptDAOobj.getReciptPaymentCustomData(invDetailRequestParams);
                            if (idcustresult.getEntityList().size() > 0) {
                                AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                    String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                                    if (customFieldMapGlobalLevel.containsKey(varEntry.getKey())) {
//                                        boolean isExport = (request.getAttribute("isExport") == null) ? false : true;

                                        String value = "";
                                        String Ids[] = coldata.split(",");
                                        for (int i = 0; i < Ids.length; i++) {
                                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
                                            FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                            if (fieldComboData != null) {
                                                if (fieldComboData.getField().getFieldtype() == 12 && !isExport) {
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
                                    } else if (customDateFieldMap.containsKey(varEntry.getKey()) && isExport ) {
                                        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                                        DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                                        Date dateFromDB=null;
                                        try {
                                            dateFromDB = defaultDateFormat.parse(coldata);
                                            coldata = df2.format(dateFromDB);

                                        } catch (Exception e) {
                                        }                                        
                                            obj1.put(varEntry.getKey(), coldata);
                                    } else {
                                        if (!StringUtil.isNullOrEmpty(coldata)) {
                                            obj1.put(varEntry.getKey(), coldata);
                                        }
                                    }
                                }
                            }
                            jArr1.put(obj1);
                        }
                    }

                    obj.put("otherwise", true);
                    obj.put("disableOtherwiseLinking", true);//to disable linking for payment GL
                    obj.put("detailsjarr", jArr1);
                } else {
//                    itrRow=receipt.getJournalEntry().getDetails().iterator();
//                    amount+=((JournalEntryDetail)itrRow.next()).getAmount();
//                    obj.put("otherwise",true);

                    itrRow = receipt.getJournalEntry().getDetails().iterator();
                    while (itrRow.hasNext()) {
                        JournalEntryDetail jed = ((JournalEntryDetail) itrRow.next());
                        if (jed.isDebit()) {
                            if (receipt.getDeposittoJEDetail() != null) {
                                amount = receipt.getDeposittoJEDetail().getAmount();
                            } else {
                                amount = jed.getAmount();
                            }
                        } else {
                            JSONObject obj1 = new JSONObject();
                            obj1.put("debitaccid", jed.getAccount().getID());
                            obj1.put("isdebit", jed.isDebit());
                            obj1.put("debitamt", jed.getAmount());
                            obj1.put("curamount", jed.getAmount());
                            obj1.put("desc", jed.getDescription() != null ? jed.getDescription() : "");
                            jArr1.put(obj1);
                        }
                    }
                    // In Receipt made against CN/DN, amountdue must be zero 
                    if(receipt.getInvoiceAdvCndnType()==3 || receipt.getReceipttype()==7) { //Against CN/DN
                        linkedAmountDue = 0;
                        obj.put("disableOtherwiseLinking", true);
                    }
                    obj.put("otherwise", true);
                    obj.put("detailsjarr", jArr1);
                }

                KwlReturnObject result = accReceiptDAOobj.getReceiptCustomerNames(receipt.getCompany().getCompanyID(), receipt.getID());
                List cNameList = result.getEntityList();
                Iterator cNamesItr = cNameList.iterator();
                String customerNames = "";
                while (cNamesItr.hasNext()) {
                    String tempName = URLEncoder.encode((String) cNamesItr.next(), "UTF-8");
                    customerNames += tempName;
                    customerNames += ",";
                }
                customerNames = customerNames.substring(0, Math.max(0, customerNames.length() - 1));
                obj.put("receipttype", receipt.getReceipttype());
                amount = authHandler.round(amount, companyid);
                totaltaxamount = authHandler.round(totaltaxamount, companyid); 
                obj.put("amount", authHandler.formattedAmount(amount, companyid));  
                obj.put("paymentamountdue",authHandler.formattedAmount(linkedAmountDue, companyid));
                obj.put("paymentamountdueinbase", authHandler.formattedAmount(amountDueInBase, companyid));
                obj.put("totaltaxamount", authHandler.formattedAmount(totaltaxamount, companyid));  
                obj.put("amountBeforeTax", authHandler.formattedAmount((amount-totaltaxamount), companyid));
                String reccurrencyid = (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID());
//                KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amount, reccurrencyid, receipt.getJournalEntry().getEntryDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amount, reccurrencyid, receipt.getCreationDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                double amountinbase = (Double) bAmt.getEntityList().get(0);
                obj.put("amountinbase", authHandler.formattedAmount(amountinbase, companyid));
                
                obj.put("personname", StringUtil.DecodeText((vendor==null&&customer==null) ? customerNames : ((customer != null) ? customer.getName() : (vendor != null) ? vendor.getName() : "")));
                obj.put("memo", receipt.getMemo());
                obj.put("deleted", receipt.isDeleted());
                obj.put("currencysymbol", (receipt.getCurrency() == null ? currency.getSymbol() : receipt.getCurrency().getSymbol()));
                obj.put("currencycode", (receipt.getCurrency() == null ? currency.getCurrencyCode() : receipt.getCurrency().getCurrencyCode()));
                obj.put("externalcurrencyrate", receipt.getExternalCurrencyRate());
                obj.put("currencyid", (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID()));
                obj.put("methodid", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getID()));
                obj.put("detailtype", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getDetailType()));
                obj.put("paymentmethod", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getMethodName()));
                obj.put("paymentwindowtype", receipt.getPaymentWindowType());
                if (receipt.getPayDetail() != null) {
                    try {
                        obj.put("expirydate", (receipt.getPayDetail().getCard() == null ? "" : authHandler.getDateOnlyFormat(request).format(receipt.getPayDetail().getCard().getExpiryDate())));
                        obj.put("refcardno", (receipt.getPayDetail().getCard() == null ? "" : (receipt.getPayDetail().getCard().getCardNo() == null ? "" : receipt.getPayDetail().getCard().getCardNo())));
                    } catch (Exception ae) {
                        obj.put("expirydate", "");
                    }
                    obj.put("refdetail", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getDescription()) : receipt.getPayDetail().getCard().getCardType()));
                    obj.put("refno", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getChequeNo()) : receipt.getPayDetail().getCard().getRefNo()));
                    obj.put("chequedescription", ((receipt.getPayDetail() == null || receipt.getPayDetail().getCheque() == null) ? "" : (receipt.getPayDetail().getCheque().getDescription() != null ? receipt.getPayDetail().getCheque().getDescription() : "")));
                    obj.put("chequenumber", ((receipt.getPayDetail() == null || receipt.getPayDetail().getCheque() == null) ? "" : receipt.getPayDetail().getCheque().getChequeNo()));
                    obj.put("bankname", ((receipt.getPayDetail() == null || receipt.getPayDetail().getCheque() == null) ? "" : receipt.getPayDetail().getCheque().getBankName()));
                    obj.put("refname", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : (receipt.getPayDetail().getCheque().getBankMasterItem() == null ? receipt.getPayDetail().getCheque().getBankName() : receipt.getPayDetail().getCheque().getBankMasterItem().getID())) : receipt.getPayDetail().getCard().getCardHolder()));
                    obj.put("clearanceDate", "");
                    obj.put("paymentStatus", false);
                    KwlReturnObject clearanceDate = accBankReconciliationObj.getBRfromJE(receipt.getJournalEntry().getID(), receipt.getCompany().getCompanyID(), false);
                    if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
                        BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
                        if (brd.getBankReconciliation().getClearanceDate() != null) {
                            obj.put("clearanceDate", authHandler.getDateOnlyFormat(request).format(brd.getBankReconciliation().getClearanceDate()));
                            obj.put("paymentStatus", true);
                        }
                    }
                }
                if (StringUtil.equal(fileType, "print") ||StringUtil.equal(fileType, "pdf") || StringUtil.equal(fileType, "csv")|| StringUtil.equal(fileType, "xls")) {//code under if condition only for print case. no need to execute for other cases.
                    String usedDocumentNumbers = getDocumentNumbersUsedInReceipt(receipt);
                    obj.put("useddocumentnumber", usedDocumentNumbers);
                }
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
//                            boolean isExport = (request.getAttribute("isExport") == null) ? false : true;
                            String value = "";
                            String Ids[] = coldata.split(",");
                            for (int i = 0; i < Ids.length; i++) {
                                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
                                FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                if (fieldComboData != null) {
                                    if (fieldComboData.getField().getFieldtype() == 12 && !isExport) {
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
                        } else if (customDateFieldMapGlobalLevel.containsKey(varEntry.getKey()) && isExport) {
                            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                dateFromDB = defaultDateFormat.parse(coldata);
                                coldata = sdf.format(dateFromDB);
                            } catch (Exception e) {
                            }
                            
                            obj.put(varEntry.getKey(),coldata);
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
    
    //claculete receipt Amount Due
    public double getReceiptAmountDue(Receipt receipt)  {
                Iterator itrRow=receipt.getRows().iterator();
                double amount=0,totaltaxamount=0,linkedAmountDue=receipt.getDepositAmount();;
                if(!receipt.getRows().isEmpty()) {
                    while(itrRow.hasNext()){
                        amount+=((ReceiptDetail)itrRow.next()).getAmount();
                    }
                    linkedAmountDue=receipt.getDepositAmount()-amount;
                }
        return linkedAmountDue;
    }

    public ModelAndView saveBillingReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        boolean isChequePrint = false;

        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveBillingReceipt(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] id = (String[]) li.get(0);
            issuccess = true;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isChequePrint"))) {
                isChequePrint = Boolean.parseBoolean(request.getParameter("isChequePrint"));
            }
            if (isChequePrint) {
                SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
                Date creationDate = new Date(request.getParameter("creationdate"));
                String date = DATE_FORMAT.format(creationDate);
                String[] amount = (String[]) li.get(1);
                String[] amount1 = (String[]) li.get(2);
                String[] accName = (String[]) li.get(3);
                jobjDetails.put(amount[0], amount[1]);
                jobjDetails.put(amount1[0], amount1[1]);
                jobjDetails.put(accName[0], accName[1]);
                jobjDetails.put("date", date);
                jArr.put(jobjDetails);
            }
            msg = messageSource.getMessage("acc.receipt.save", null, RequestContextUtils.getLocale(request));   //"Receipt has been saved successfully";
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteJEArray(id[0], companyid);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteChequeOrCard(id[1], companyid);
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArr);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveBillingReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        KwlReturnObject result;
        BillingReceipt receipt = null;
        String oldjeid = null;
        String Cardid = null;
        double amount = 0;
        String netinword = "";
        BillingGoodsReceipt bgreceipt = null;//Set for contra entry
        List ll = new ArrayList();
        try {
            Account dipositTo = null;

            double amountDiff = 0;
            boolean rateDecreased = false;
            String sequenceformat = request.getParameter("sequenceformat");
            String customfield = request.getParameter("customfield");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double externalExchangeRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String entryNumber = request.getParameter("no");
            boolean isMultiDebit = StringUtil.getBoolean(request.getParameter("ismultidebit"));
            boolean isAdvancePayment = StringUtil.getBoolean(request.getParameter("isadvpayment"));
            String receiptid = request.getParameter("billid");
            String methodid = request.getParameter("pmtmethod");
            boolean otherwise = ((request.getParameter("otherwise") != null) ? Boolean.parseBoolean(request.getParameter("otherwise")) : false);
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);
            String drAccDetails = request.getParameter("detail");
            int receiptType = StringUtil.getInteger(request.getParameter("receipttype"));
            String jeid = null;
            String payDetailID = null;
            String jeentryNumber = null;
            boolean jeautogenflag = false;
            boolean bankReconsilationEntry = false, bankPayment = false;
            Date clearanceDate = null, startDate = null, endDate = null;
            String bankAccountId = "";
            Map<String, Object> bankReconsilationMap = new HashMap<String, Object>();
            boolean isadvanceFromVendor = StringUtil.getBoolean(request.getParameter("isadvanceFromVendor"));
            HashMap receipthm = new HashMap();
            boolean ismanydbcr = StringUtil.getBoolean(request.getParameter("ismanydbcr"));
            receipthm.put("isadvanceFromVendor", isadvanceFromVendor);
            receipthm.put("ismanydbcr", ismanydbcr);
            receipthm.put("receipttype", receiptType);
            if (receiptType == 6) {
                receipthm.put("vendor", request.getParameter("accid"));
            }
            double bankCharges = 0;
            double bankInterest = 0;
            String bankChargesAccid = request.getParameter("bankChargesCmb");
            String bankInterestAccid = request.getParameter("bankInterestCmb");
            boolean onlyAdvance = StringUtil.getBoolean(request.getParameter("onlyAdvance"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(request.getParameter("bankCharges"));
                receipthm.put("bankCharges", bankCharges);
                receipthm.put("bankChargesCmb", bankChargesAccid);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(request.getParameter("bankInterest"));
                receipthm.put("bankInterest", bankInterest);
                receipthm.put("bankInterestCmb", bankInterestAccid);
            }
            String paidToid = request.getParameter("paidToCmb");
            if (!StringUtil.isNullOrEmpty(request.getParameter("paidToCmb")) && !StringUtil.isNullOrEmpty(paidToid)) {
                receipthm.put("paidToCmb", paidToid);
            }

            if (!StringUtil.isNullOrEmpty(receiptid)) {
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(BillingReceipt.class.getName(), receiptid);
                receipt = (BillingReceipt) receiptObj.getEntityList().get(0);
                jeentryNumber = receipt.getJournalEntry().getEntryNumber();
                oldjeid = receipt.getJournalEntry().getID();
                jeautogenflag = receipt.getJournalEntry().isAutoGenerated();
                if (receipt.getPayDetail() != null) {
                    payDetailID = receipt.getPayDetail().getID();
                    if (receipt.getPayDetail().getCard() != null) {
                        Cardid = receipt.getPayDetail().getCard().getID();
                    }
                    if (receipt.getPayDetail().getCheque() != null) {
                        Cardid = receipt.getPayDetail().getCheque().getID();
                    }
                }
                result = accReceiptDAOobj.deleteBillingReceiptDetails(receiptid, companyid);
                result = accReceiptDAOobj.deleteBillingReceiptDetailsOtherwise(receiptid);
                if (receipt != null && receipt.getRevalJeId() != null) {
                    result = accJournalEntryobj.deleteJEDtails(receipt.getRevalJeId(), companyid);
                    result = accJournalEntryobj.deleteJE(receipt.getRevalJeId(), companyid);
                }

                receipthm.put("deposittojedetailid", null);
                receipthm.put("depositamount", 0.0);
                //Delete old entries and insert new entries again from optimized table in edit case.
                accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
            } else {
                result = accReceiptDAOobj.getBillingReceiptFromBillNo(entryNumber, companyid);
                int count = result.getRecordTotalCount();
                if (count > 0) {
                    throw new AccountingException(messageSource.getMessage("acc.field.Receiptnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                String nextAutoNumber = "";
                String nextAutoNoInt = "";
                String datePrefix = "";
                String dateafterPrefix = "";
                String dateSuffix = "";
                if (seqformat_oldflag) {
                    nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGRECEIPT, sequenceformat);
                } else {
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_BILLINGRECEIPT, sequenceformat, seqformat_oldflag, new Date());
                    nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    
                    receipthm.put(Constants.SEQFORMAT, sequenceformat);
                    receipthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                    receipthm.put(Constants.DATEPREFIX, datePrefix);
                    receipthm.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                    receipthm.put(Constants.DATESUFFIX, dateSuffix);
                }
                receipthm.put("entrynumber", entryNumber);
                receipthm.put("autogenerated", nextAutoNumber.equals(entryNumber));
            }

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            receipthm.put("currencyid", currencyid);
            receipthm.put("externalCurrencyRate", externalExchangeRate);
            KwlReturnObject payresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) payresult.getEntityList().get(0);

            dipositTo = payMethod.getAccount();
            HashMap pdetailhm = new HashMap();
            pdetailhm.put("paymethodid", payMethod.getID());
            pdetailhm.put("companyid", company.getCompanyID());
            if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH) {
                JSONObject obj = new JSONObject(request.getParameter("paydetail"));
                if (payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                    bankPayment = true;
                    bankReconsilationEntry = obj.getString("paymentStatus") != null ? obj.getString("paymentStatus").equals("Cleared") : false;
                    if (bankReconsilationEntry) {
                        bankAccountId = request.getParameter("bankaccid");
                        startDate = df.parse(request.getParameter("startdate"));
                        endDate = df.parse(request.getParameter("enddate"));
                        clearanceDate = df.parse(obj.getString("clearanceDate"));
                        bankReconsilationMap.put("bankAccountId", bankAccountId);
                        bankReconsilationMap.put("startDate", startDate);
                        bankReconsilationMap.put("endDate", endDate);
                        bankReconsilationMap.put("clearanceDate", clearanceDate);
                        bankReconsilationMap.put("endingAmount", 0.0);
                        bankReconsilationMap.put("companyId", companyid);
                    }
                    HashMap chequehm = new HashMap();
                    chequehm.put("chequeno", obj.getString("chequeno"));
                    chequehm.put("description", StringUtil.DecodeText(obj.getString("description")));
                    chequehm.put("duedate", df.parse(obj.getString("payDate")));
                    chequehm.put("bankname",StringUtil.DecodeText(obj.getString("bankname")));
                    chequehm.put("bankmasteritemid", obj.getString("bankmasteritemid"));
                    Map<String, Object> seqchequehm = new HashMap<>();
                    obj.put(Constants.companyKey, companyid);
                    String chequesequenceformat =  obj.optString("sequenceformat");
                    String nextChequeNumber="";
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
            receipthm.put("memo", request.getParameter("memo"));
            receipthm.put("companyid", company.getCompanyID());

            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
                jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
                jeautogenflag = true;
            }
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", df.parse(request.getParameter("creationdate")));
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", request.getParameter("memo"));
            jeDataMap.put("currencyid", currencyid);
            HashSet jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            String detail = request.getParameter("detail");
            JSONArray jArr = new JSONArray();
            if (!StringUtil.isNullOrEmpty(detail)) {
                jArr = new JSONArray(detail);
            }
            if (jArr.length() > 0 && !isMultiDebit) {
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    amount += jobj.getDouble("payment");
                }
                amountDiff = oldBillingReceiptRowsAmount(request, jArr, currencyid, externalExchangeRate);

                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null) {
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jedjson.put("accountid", preferences.getForeignexchange().getID());
                    jedjson.put("debit", rateDecreased ? false : true);
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            } else {
                amount = Double.parseDouble(request.getParameter("amount"));
            }
            JSONObject jedjson = null;
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed = null;
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
                    jedjson.put("accountid", jobj.getString("accountid"));
                    jedjson.put("debit", isdebit);//false);
                    jedjson.put("jeid", jeid);
                    jedjson.put("description", jobj.getString("description"));
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                    double rowtaxamount = 0;

                    if (receiptType == 2 || receiptType == 9) {
                        String rowtaxid = jobj.getString("prtaxid");
                        //taxExist=StringUtil.isNullOrEmpty(rowtaxid)?false:true;
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
                            result = accReceiptDAOobj.saveBillingReceiptDetailOtherwise(receiptdetailotherwise);
                            receiptdetailotherwise.clear();
                            BillingReceiptDetailOtherwise receiptDetailOtherwise = null;
                            receiptDetailOtherwise = (BillingReceiptDetailOtherwise) result.getEntityList().get(0);
                            receiptOtherwiseList.add(receiptDetailOtherwise.getID());
                        } else {
                            rowtaxamount = Double.parseDouble(jobj.getString("curamount")) - Double.parseDouble(jobj.getString("dramount"));
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

                            receiptdetailotherwise.put("amount", Double.parseDouble(jobj.getString("dramount")));
                            receiptdetailotherwise.put("isdebit", isdebit);
                            receiptdetailotherwise.put("taxjedid", jed.getID());
                            receiptdetailotherwise.put("tax", rowtax.getID());
                            receiptdetailotherwise.put("accountid", jobj.getString("accountid"));
                            receiptdetailotherwise.put("taxamount", rowtaxamount);
                            receiptdetailotherwise.put("description", jobj.getString("description"));
                            result = accReceiptDAOobj.saveBillingReceiptDetailOtherwise(receiptdetailotherwise);
                            receiptdetailotherwise.clear();
                            BillingReceiptDetailOtherwise receiptDetailOtherwise = null;
                            receiptDetailOtherwise = (BillingReceiptDetailOtherwise) result.getEntityList().get(0);
                            receiptOtherwiseList.add(receiptDetailOtherwise.getID());
                        }

                    }

                }
            } else {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", amount + amountDiff);
                jedjson.put("accountid", request.getParameter("accid"));
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
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
            jeDataMap.put("externalCurrencyRate", externalExchangeRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            receipthm.put("journalentryid", journalEntry.getID());

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

            if (!StringUtil.isNullOrEmpty(receiptid)) {
                receipthm.put("billingreceiptid", receipt.getID());
            }
            result = accReceiptDAOobj.saveBillingReceipt(receipthm);
            receipt = (BillingReceipt) result.getEntityList().get(0);
            receipthm.put("billingreceiptid", receipt.getID());

            HashSet receiptDetails = new HashSet();
            if (!isMultiDebit) {
                receiptDetails = saveBillingReceiptRows(receipt, company, jArr, bgreceipt);
            }
            receipthm.put("receiptdetails", receiptDetails);

            result = accReceiptDAOobj.saveBillingReceipt(receipthm);
            receipt = (BillingReceipt) result.getEntityList().get(0);
            if (receiptType == 2 || receiptType == 9) {//otherwise case and GL Code Case 
                for (int i = 0; i < receiptOtherwiseList.size(); i++) {
                    receiptdetailotherwise.put("billingreceipt", receipt.getID());
                    receiptdetailotherwise.put("receiptotherwise", receiptOtherwiseList.get(i));
                    result = accReceiptDAOobj.saveBillingReceiptDetailOtherwise(receiptdetailotherwise);
                    receiptdetailotherwise.clear();
                }
            }
            if (bankReconsilationEntry) {
                bankReconsilationMap.put("clearingamount", amount);
                bankReconsilationMap.put("currencyid", currencyid);
                bankReconsilationMap.put("details", jArr);
                bankReconsilationMap.put("ismultidebit", isMultiDebit);
                bankReconsilationMap.put("breceipt", receipt);
                if (!StringUtil.isNullOrEmpty(oldjeid)) {
                    bankReconsilationMap.put("oldjeid", oldjeid);
                }
                HashMap<String, Object> globalParams = AccountingManager.getGlobalParams(request);
                saveBankReconsilation(bankReconsilationMap, globalParams);
            }
            if (bankPayment && !bankReconsilationEntry && !StringUtil.isNullOrEmpty(oldjeid)) {
                bankReconsilationMap.put("oldjeid", oldjeid);
                bankReconsilationMap.put("companyId", companyid);
                deleteBankReconcilation(bankReconsilationMap);
            }

            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(amount)), currency,countryLanguageId);
            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " created new receipt ", request, receipt.getID());
            if (jArr.length() > 0 && !isMultiDebit) {
                double finalAmountReval = 0;
                String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    double ratio = 0;
                    double amountReval = 0;
                    double amountdue = jobj.getDouble("payment");
                    HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
                    GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    GlobalParams.put("gcurrencyid", basecurrency);
                    GlobalParams.put("dateformat", authHandler.getDateOnlyFormat(request));
                    Date creationDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("creationdate"));
                    result = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), jobj.getString("billid"));
                    BillingInvoice invoice = (BillingInvoice) result.getEntityList().get(0);
                    double exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                    invoiceId.put("invoiceid", invoice.getID());
                    invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (revalueationHistory != null) {
                        exchangeRate = revalueationHistory.getEvalrate();
                    }

                    result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
                    currency = (KWLCurrency) result.getEntityList().get(0);
                    String currid = currency.getCurrencyID();
                    if (invoice.getCurrency() != null) {
                        currid = invoice.getCurrency().getCurrencyID();
                    }
                    //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
                    KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    double oldrate = (Double) bAmt.getEntityList().get(0);
                    //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRate);
                    double newrate = (Double) bAmt.getEntityList().get(0);
                    ratio = oldrate - newrate;
                    amountReval = amountdue * ratio;
                    finalAmountReval = finalAmountReval + amountReval;
                }
                if (finalAmountReval != 0) {
                    String revaljeid = PostJEFORReevaluation(request, finalAmountReval, companyid, preferences, basecurrency);
                    receipthm.clear();
                    receipthm.put("billingreceiptid", receipt.getID());
                    receipthm.put("revalJeId", revaljeid);
                    result = accReceiptDAOobj.saveBillingReceipt(receipthm);

                }
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveBillingReceipt : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveBillingReceipt : " + ex.getMessage(), ex);
        }
        KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), request.getParameter("accid"));
        Account account = (Account) accresult.getEntityList().get(0);
        String accountName = "";
        if (account != null) {
            accountName = account.getName();
        }
        ll.add(new String[]{oldjeid, Cardid});
        ll.add(new String[]{"amount", String.valueOf(amount)});
        ll.add(new String[]{"amountinword", netinword});
        ll.add(new String[]{"accountName", accountName});
        return (ArrayList) ll;
    }

    public ModelAndView saveContraBillingReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        boolean isChequePrint = false;

        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveContraBillingReceipt(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] id = (String[]) li.get(0);
            issuccess = true;
            msg = messageSource.getMessage("acc.contra.save", null, RequestContextUtils.getLocale(request));   //"Receipt has been saved successfully";
            txnManager.commit(status);
//            status = txnManager.getTransaction(def);
//            deleteJEArray(id[0],companyid);
//            txnManager.commit(status);
//            status = txnManager.getTransaction(def);
//            deleteChequeOrCard(id[1],companyid);
//            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArr);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveContraBillingReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        KwlReturnObject result;
        BillingReceipt receipt = null;
        String oldjeid = null;
        String Cardid = null;
        double amount = 0;
        String netinword = "";
        List ll = new ArrayList();
        try {
            String maininvoiceid = request.getParameter("maininvoiceid");//Billing Goods Receipt
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(BillingGoodsReceipt.class.getName(), maininvoiceid);
            BillingGoodsReceipt bgreceipt = (BillingGoodsReceipt) cmpresult.getEntityList().get(0);
            Account dipositTo = bgreceipt.getVendor().getAccount();

            double amountDiff = 0;
            boolean rateDecreased = false;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String sequenceformat = request.getParameter("sequenceformat");
            double externalExchangeRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String entryNumber = request.getParameter("no");
            boolean isMultiDebit = StringUtil.getBoolean(request.getParameter("ismultidebit"));
            String receiptid = request.getParameter("billid");
//            String methodid =request.getParameter("pmtmethod");            
//            request.getSession().setAttribute("methodid", methodid); 
            String drAccDetails = request.getParameter("detail");
            String jeid = null;
            String jeentryNumber = null;
            boolean jeautogenflag = false;

            HashMap receipthm = new HashMap();

            result = accReceiptDAOobj.getBillingReceiptFromBillNo(entryNumber, companyid);
            int count = result.getRecordTotalCount();
            if (count > 0) {
                throw new AccountingException(messageSource.getMessage("acc.field.Receiptnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
            }
            boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
            String nextAutoNumber = "";
            String nextAutoNoInt = "";
            String datePrefix = "";
            String dateafterPrefix = "";
            String dateSuffix = "";

            if (seqformat_oldflag) {
                nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGRECEIPT, sequenceformat);
            } else {
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_BILLINGRECEIPT, sequenceformat, seqformat_oldflag, new Date());
                nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                
                receipthm.put(Constants.SEQFORMAT, sequenceformat);
                receipthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                receipthm.put(Constants.DATEPREFIX, datePrefix);
                receipthm.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                receipthm.put(Constants.DATESUFFIX, dateSuffix);
            }
            receipthm.put("entrynumber", entryNumber);
            receipthm.put("autogenerated", nextAutoNumber.equals(entryNumber));

            cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            receipthm.put("currencyid", currencyid);
            receipthm.put("externalCurrencyRate", externalExchangeRate);
//            KwlReturnObject payresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
//            PaymentMethod payMethod = (PaymentMethod) payresult.getEntityList().get(0);
//
//            dipositTo = payMethod.getAccount();
//            HashMap pdetailhm = new HashMap();
//            pdetailhm.put("paymethodid", payMethod.getID());
//            pdetailhm.put("companyid", company.getCompanyID());
//            
//            KwlReturnObject pdresult=null;
//            if (!StringUtil.isNullOrEmpty(receiptid)&&!StringUtil.isNullOrEmpty(payDetailID))
//                pdetailhm.put("paydetailid", payDetailID);
//            pdresult = accPaymentDAOobj.addPayDetail(pdetailhm);            
//            PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);
//            receipthm.put("paydetailsid", pdetail.getID());
            receipthm.put("memo", request.getParameter("memo"));
            receipthm.put("companyid", company.getCompanyID());

            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
                jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
                jeautogenflag = true;
            }
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", df.parse(request.getParameter("creationdate")));
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", " Contra Entry : " + request.getParameter("memo"));
            jeDataMap.put("currencyid", currencyid);
            HashSet jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            String detail = request.getParameter("detail");
            JSONArray jArr = new JSONArray();
            if (!StringUtil.isNullOrEmpty(detail)) {
                jArr = new JSONArray(detail);
            }
            if (jArr.length() > 0 && !isMultiDebit) {
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    amount += jobj.getDouble("payment");
                }
//                amountDiff = oldBillingReceiptRowsAmount(request, jArr, currencyid,externalExchangeRate);
//
//                if (preferences.getForeignexchange() == null) {
//                    throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, RequestContextUtils.getLocale(request)));
//                }
//                if (amountDiff != 0 && preferences.getForeignexchange() != null) {
//                    if(amountDiff < 0) {
//                        rateDecreased = true;
//                    }
//                    JSONObject jedjson = new JSONObject();
//                    jedjson.put("srno", jedetails.size()+1);
//                    jedjson.put("companyid", companyid);
//                    jedjson.put("amount", rateDecreased?(-1*amountDiff):amountDiff);
//                    jedjson.put("accountid", preferences.getForeignexchange().getID());
//                    jedjson.put("debit", rateDecreased?false:true);
//                    jedjson.put("jeid", jeid);
//                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                    jedetails.add(jed);
//                }
            } else {
                amount = Double.parseDouble(request.getParameter("amount"));
            }
            JSONObject jedjson = null;
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed = null;
            if (isMultiDebit) {
                JSONArray drAccArr = new JSONArray(drAccDetails);
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", Double.parseDouble(jobj.getString("dramount")));
                    jedjson.put("accountid", jobj.getString("accountid"));
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    jedjson.put("description", jobj.getString("description"));
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            } else {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", amount + amountDiff);
                jedjson.put("accountid", request.getParameter("accid"));
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }

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

            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalExchangeRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            receipthm.put("journalentryid", journalEntry.getID());
            receipthm.put("contraentry", true);
            if (!StringUtil.isNullOrEmpty(receiptid)) {
                receipthm.put("billingreceiptid", receipt.getID());
            }
            result = accReceiptDAOobj.saveBillingReceipt(receipthm);
            receipt = (BillingReceipt) result.getEntityList().get(0);
            receipthm.put("billingreceiptid", receipt.getID());

            HashSet receiptDetails = new HashSet();
            if (!isMultiDebit) {
                receiptDetails = saveBillingReceiptRows(receipt, company, jArr, bgreceipt);
            }
            receipthm.put("receiptdetails", receiptDetails);

            result = accReceiptDAOobj.saveBillingReceipt(receipthm);
            receipt = (BillingReceipt) result.getEntityList().get(0);

            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            //To do - need to make audit entry
//            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User "+sessionHandlerImpl.getUserFullName(request) +" created new receipt ", request, receipt.getID());       
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveBillingReceipt : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveBillingReceipt : " + ex.getMessage(), ex);
        }
        KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), request.getParameter("accid"));
        Account account = (Account) accresult.getEntityList().get(0);
        String accountName = "";
        if (account != null) {
            accountName = account.getName();
        }
        ll.add(new String[]{oldjeid, Cardid});
        ll.add(new String[]{"amount", String.valueOf(amount)});
        ll.add(new String[]{"amountinword", netinword});
        ll.add(new String[]{"accountName", accountName});
        return (ArrayList) ll;
    }

    private HashSet saveBillingReceiptRows(BillingReceipt receipt, Company company, JSONArray jArr, BillingGoodsReceipt bgreceipt) throws JSONException, ServiceException {
        HashSet details = new HashSet();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            BillingReceiptDetail rd = new BillingReceiptDetail();
            rd.setSrno(i + 1);
            rd.setAmount(jobj.getDouble("payment"));
            rd.setCompany(company);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), jobj.getString("billid"));
            rd.setBillingInvoice((BillingInvoice) result.getEntityList().get(0));
            rd.setBillingReceipt(receipt);
            if (bgreceipt != null) {
                rd.setBillingGoodsReceipt(bgreceipt);
            }
            details.add(rd);
        }
        return details;
    }

    private double oldBillingReceiptRowsAmount(HttpServletRequest request, JSONArray jArr, String currencyid, double externalExchangeRate) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        double ratio = 0;
        double amount = 0;
        KwlReturnObject result;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
//            BillingInvoice invoice = (BillingInvoice) session.get(BillingInvoice.class, jobj.getString("billid"));
            result = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), jobj.getString("billid"));
            BillingInvoice invoice = (BillingInvoice) result.getEntityList().get(0);
            double exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
            HashMap<String, Object> invoiceId = new HashMap<String, Object>();
            invoiceId.put("invoiceid", invoice.getID());
            invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
            //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
            result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (history != null) {
                exchangeRate = history.getEvalrate();
            }
//            KWLCurrency currency = (KWLCurrency) session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (invoice.getCurrency() != null) {
                currid = invoice.getCurrency().getCurrencyID();
            }

//            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, invoice.getJournalEntry().getEntryDate());
            result = accCurrencyobj.getCurrencyToBaseAmount(requestParams, 1.0, currid, invoice.getJournalEntry().getEntryDate(), exchangeRate);
            double oldrate = (Double) result.getEntityList().get(0);
//            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
            result = accCurrencyobj.getCurrencyToBaseAmount(requestParams, 1.0, currid, authHandler.getDateOnlyFormat(request).parse(request.getParameter("creationdate")), externalExchangeRate);
            double newrate = (Double) result.getEntityList().get(0);

            ratio = oldrate - newrate;
            Double recinvamount = jobj.getDouble("payment");
            amount += recinvamount * ratio;
        }
//        amount = CompanyHandler.getBaseToCurrencyAmount(session, request, amount, currencyid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
        result = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, authHandler.getDateOnlyFormat(request).parse(request.getParameter("creationdate")), externalExchangeRate);
        amount = (Double) result.getEntityList().get(0);
        return (amount);
    }

//    public ModelAndView getBillingReceipts(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj=new JSONObject();
//        boolean issuccess = false;
//        String msg = "";
//		try {
//            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
//            KwlReturnObject result = accReceiptDAOobj.getBillingReceipts(requestParams);
//            jobj = getBillingReceiptJson(request, result.getEntityList());
//            jobj.put("count", result.getRecordTotalCount());
//            issuccess = true;
//        } catch (Exception ex) {
//            msg = ""+ex.getMessage();
//            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try{
//                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
//            } catch (JSONException ex) {
//                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView("jsonView", "model", jobj.toString());
//    }
    public List<JSONObject> getBillingReceiptJson(HttpServletRequest request, List list, List<JSONObject> jsonObjectlist) throws SessionExpiredException, ServiceException, UnsupportedEncodingException {
        //JSONObject jobj=new JSONObject();
        //JSONArray jArr=new JSONArray();
        try {
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                BillingReceipt receipt = (BillingReceipt) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
                JSONArray jArr1 = new JSONArray();
                obj.put("withoutinventory", true);
                obj.put("billid", receipt.getID());
                obj.put("companyid", receipt.getCompany().getCompanyID());
                obj.put("companyname", receipt.getCompany().getCompanyName());
                obj.put("entryno", receipt.getJournalEntry().getEntryNumber());
                obj.put("journalentryid", receipt.getJournalEntry().getID());
                KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), acc.getID());
                Customer customer = (Customer) customerresult.getEntityList().get(0);
                obj.put("personemail", customer != null ? customer.getEmail() : "");
                obj.put("personid", acc.getID());
                obj.put("billno", receipt.getBillingReceiptNumber());
                obj.put("ismanydbcr", receipt.isIsmanydbcr());
                obj.put("bankCharges", receipt.getBankChargesAmount());
                obj.put("bankChargesCmb", receipt.getBankChargesAccount() != null ? receipt.getBankChargesAccount().getID() : "");
                obj.put("bankInterest", receipt.getBankInterestAmount());
                obj.put("bankInterestCmb", receipt.getBankInterestAccount() != null ? receipt.getBankInterestAccount().getID() : "");
                obj.put("paidToCmb", receipt.getReceivedFrom() == null ? "" : receipt.getReceivedFrom().getID());
                obj.put("billdate", authHandler.getDateOnlyFormat(request).format(receipt.getJournalEntry().getEntryDate()));//receiptdate
                obj.put("currencysymbol", (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getSymbol()));
                obj.put("externalcurrencyrate", receipt.getExternalCurrencyRate());
                Iterator itrRow = receipt.getRows().iterator();
                rRequestParams.clear();
                filter_names.clear();
                filter_params.clear();
                filter_names.add("billingReceipt.ID");
                filter_params.add(receipt.getID());
                rRequestParams.put("filter_names", filter_names);
                rRequestParams.put("filter_params", filter_params);
                KwlReturnObject pdoresult = accReceiptDAOobj.getBillingReceiptDetailOtherwise(rRequestParams);
                List<BillingReceiptDetailOtherwise> list1 = pdoresult.getEntityList();
                Iterator pdoRow = list1.iterator();

                double amount = 0;
                if (!receipt.getRows().isEmpty()) {
                    while (itrRow.hasNext()) {
                        amount += ((BillingReceiptDetail) itrRow.next()).getAmount();
                    }
                    obj.put("otherwise", false);
                } else if (pdoRow != null && list1.size() > 0) {
                    for (BillingReceiptDetailOtherwise receiptDetailOtherwise : list1) {
                        if (receipt.getID().equals(receiptDetailOtherwise.getBillingReceipt().getID())) {
                            JSONObject obj1 = new JSONObject();
                            obj1.put("debitaccid", receiptDetailOtherwise.getAccount().getID());
                            obj1.put("debitamt", receiptDetailOtherwise.getAmount());
                            obj1.put("isdebit", receiptDetailOtherwise.isIsdebit());
                            obj1.put("desc", receiptDetailOtherwise.getDescription() != null ? receiptDetailOtherwise.getDescription() : "");
                            obj1.put("prtaxid", receiptDetailOtherwise.getTax() != null ? receiptDetailOtherwise.getTax().getID() : "");
                            obj1.put("curamount", (receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount()));
                            if (receipt.isIsmanydbcr()) {
                                if (receiptDetailOtherwise.isIsdebit()) {
                                    amount -= (receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount());
                                } else {
                                    amount += (receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount());
                                }
                            } else {
                                amount += (receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount());
                            }
                            jArr1.put(obj1);
                        }
                    }

                    obj.put("otherwise", true);
                    obj.put("detailsjarr", jArr1);
                } else {
//                    itrRow=receipt.getJournalEntry().getDetails().iterator();
//                    amount+=((JournalEntryDetail)itrRow.next()).getAmount();
//                    obj.put("otherwise",true);                   
                    itrRow = receipt.getJournalEntry().getDetails().iterator();
                    while (itrRow.hasNext()) {
                        JournalEntryDetail jed = ((JournalEntryDetail) itrRow.next());
                        if (jed.isDebit()) {
                            if (receipt.getDeposittoJEDetail() != null) {
                                amount = receipt.getDeposittoJEDetail().getAmount();
                            } else {
                                amount = jed.getAmount();
                            }
                        } else {
                            JSONObject obj1 = new JSONObject();
                            obj1.put("debitaccid", jed.getAccount().getID());
                            obj1.put("isdebit", jed.isDebit());
                            obj1.put("debitamt", jed.getAmount());
                            obj1.put("curamount", jed.getAmount());
                            obj1.put("desc", jed.getDescription() != null ? jed.getDescription() : "");
                            jArr1.put(obj1);
                        }
                    }
                    obj.put("otherwise", true);
                    obj.put("detailsjarr", jArr1);
                }

                KwlReturnObject result = accReceiptDAOobj.getBillingReceiptCustomerNames(receipt.getCompany().getCompanyID(), receipt.getID());
                List cNameList = result.getEntityList();
                Iterator cNamesItr = cNameList.iterator();
                String customerNames = "";
                while (cNamesItr.hasNext()) {
                    String tempName = URLEncoder.encode((String) cNamesItr.next(), "UTF-8");
                    customerNames += tempName;
                    customerNames += ",";
                }
                customerNames = customerNames.substring(0, Math.max(0, customerNames.length() - 1));
                obj.put("amount", authHandler.formattedAmount(amount, companyid));
                obj.put("personname", customerNames);
                obj.put("receipttype", "");
                obj.put("memo", receipt.getMemo());
                obj.put("deleted", receipt.isDeleted());
                obj.put("paymentmethod", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getMethodName()));
                obj.put("amount", amount);
                obj.put("currencysymbol", (receipt.getCurrency() == null ? currency.getSymbol() : receipt.getCurrency().getSymbol()));
                obj.put("currencyid", (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID()));
                obj.put("methodid", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getID()));
                obj.put("detailtype", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getDetailType()));
                if (receipt.getPayDetail() != null) {
                    try {
                        obj.put("expirydate", (receipt.getPayDetail().getCard() == null ? "" : authHandler.getDateOnlyFormat(request).format(receipt.getPayDetail().getCard().getExpiryDate())));
                        obj.put("refcardno", (receipt.getPayDetail().getCard() == null ? "" : (receipt.getPayDetail().getCard().getCardNo() == null ? "" : receipt.getPayDetail().getCard().getCardNo())));
                    } catch (IllegalArgumentException ae) {
                        obj.put("expirydate", "");
                    }
                    obj.put("refdetail", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getDescription()) : receipt.getPayDetail().getCard().getCardType()));
                    obj.put("refno", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getChequeNo()) : receipt.getPayDetail().getCard().getRefNo()));
                    obj.put("refname", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : (receipt.getPayDetail().getCheque().getBankMasterItem() == null ? receipt.getPayDetail().getCheque().getBankName() : receipt.getPayDetail().getCheque().getBankMasterItem().getID())) : receipt.getPayDetail().getCard().getCardHolder()));
                }
                obj.put("clearanceDate", "");
                obj.put("paymentStatus", false);
                if (receipt.getPayDetail() != null) {
                    KwlReturnObject clearanceDate = accBankReconciliationObj.getBRfromJE(receipt.getJournalEntry().getID(), receipt.getCompany().getCompanyID(), false);
                    if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
                        BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
                        if (brd.getBankReconciliation().getClearanceDate() != null) {
                            obj.put("clearanceDate", authHandler.getDateOnlyFormat(request).format(brd.getBankReconciliation().getClearanceDate()));
                            obj.put("paymentStatus", true);
                        }
                    }
                }
                jsonObjectlist.add(obj);
            }
            // jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getBillingReceiptJson : " + ex.getMessage(), ex);
        }
        return jsonObjectlist;
    }

    public ModelAndView getBillingReceiptRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getBillingReceiptRowsJSON(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private JSONObject getBillingReceiptRowsJSON(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
//                KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            boolean isReceiptEdit = Boolean.parseBoolean(request.getParameter("isReceiptEdit"));
            String[] billingreceipt = request.getParameterValues("bills");
            int i = 0;
            double taxPercent = 0;
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("billingReceipt.ID");
            order_by.add("srno");
            order_type.add("asc");
            rRequestParams.put("filter_names", filter_names);
            rRequestParams.put("filter_params", filter_params);
            rRequestParams.put("order_by", order_by);
            rRequestParams.put("order_type", order_type);

            JSONArray jArr = new JSONArray();
            while (billingreceipt != null && i < billingreceipt.length) {
//                    BillingReceipt re=(BillingReceipt)session.get(BillingReceipt.class, billingreceipt[i]);
                KwlReturnObject result = accountingHandlerDAOobj.getObject(BillingReceipt.class.getName(), billingreceipt[i]);
                BillingReceipt re = (BillingReceipt) result.getEntityList().get(0);
//                Iterator itr=re.getRows().iterator();
                filter_params.clear();
                filter_params.add(re.getID());
                KwlReturnObject grdresult = accReceiptDAOobj.getBillingReceiptDetails(rRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();

                while (itr.hasNext()) {
                    BillingReceiptDetail row = (BillingReceiptDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", isReceiptEdit ? row.getBillingInvoice().getID() : re.getID());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("currencysymbol", (row.getBillingReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getBillingReceipt().getCurrency().getSymbol()));
                    obj.put("transectionno", row.getBillingInvoice().getBillingInvoiceNumber());
                    obj.put("transectionid", row.getBillingInvoice().getID());
                    obj.put("amount", (isReceiptEdit ? row.getBillingInvoice().getCustomerEntry().getAmount() : row.getAmount()));
                    obj.put("amountpaid", row.getAmount());
                    obj.put("duedate", authHandler.getDateOnlyFormat(request).format(row.getBillingInvoice().getDueDate()));
                    obj.put("creationdate", authHandler.getDateOnlyFormat(request).format(row.getBillingInvoice().getJournalEntry().getEntryDate()));
                    double totalamount = row.getBillingInvoice().getCustomerEntry().getAmount();
                    obj.put("totalamount", totalamount);

                    KwlReturnObject amtrs = accReceiptDAOobj.getBillingReceiptAmountFromInvoice(row.getBillingInvoice().getID());
                    double ramount = amtrs.getEntityList().size() > 0 ? (Double) amtrs.getEntityList().get(0) : 0;
                    double amountdue = totalamount - ramount;
                    obj.put("amountduenonnegative", (isReceiptEdit ? amountdue + row.getAmount() : amountdue));
                    if (row.getBillingInvoice().getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getBillingInvoice().getJournalEntry().getEntryDate(), row.getBillingInvoice().getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("taxpercent", taxPercent);
                    obj.put("discount", row.getBillingInvoice().getDiscount() == null ? 0 : row.getBillingInvoice().getDiscount().getDiscountValue());
                    obj.put("payment", row.getBillingInvoice().getID());
                    obj.put("totalamount", row.getBillingInvoice().getCustomerEntry().getAmount());
                    jArr.put(obj);
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getBillingReceiptRowsJSON : " + ex.getMessage(), ex);
        }
        return jobj;
    }

//    public ModelAndView deleteBillingReceipt(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj = new JSONObject();
//        boolean issuccess = false;
//        String msg = "";
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("BR_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = txnManager.getTransaction(def);
//        try{
//            String receiptsJson = request.getParameter("data");
//            String companyid = sessionHandlerImpl.getCompanyid(request);
//            int no = deleteBillingReceipt(receiptsJson, companyid);
//            txnManager.commit(status);
//            issuccess = true;
//            msg = messageSource.getMessage("acc.receipt.billdel", null, RequestContextUtils.getLocale(request));   //"Billing Receipt(s) has been deleted successfully";
//        } catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
//            msg = ex.getMessage();
//            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            txnManager.rollback(status);
//            msg = ""+ex.getMessage();
//        } finally {
//            try {
//                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
//            } catch (JSONException ex) {
//                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView("jsonView", "model", jobj.toString());
//    }
//
//    public int deleteBillingReceipt(String receiptsJson, String companyid) throws AccountingException, ServiceException {
//        String msg = "";
//        int numRows = 0;
//        try{
//            JSONArray jArr = new JSONArray(receiptsJson);
//
//            String receiptid;
//            KwlReturnObject result;
//            for (int i = 0; i < jArr.length(); i++) {
//                JSONObject jobj = jArr.getJSONObject(i);
//                receiptid = StringUtil.DecodeText(jobj.optString("billid"));
//                String jeid1 = StringUtil.DecodeText(jobj.optString("journalentryid"));
//                //Delete Billing receipt details
////                result = accReceiptDAOobj.deleteBillingReceiptDetails(receiptid, companyid);
//                //Delete Billing Receipt
//                result = accReceiptDAOobj.deleteBillingReceiptEntry(receiptid, companyid);
//
//                HashMap<String,Object> requestParams = new HashMap<String, Object>();
//                requestParams.put("oldjeid", jeid1);
//                requestParams.put("companyId", companyid);
//                deleteBankReconcilation(requestParams);
//
////                jeid = StringUtil.DecodeText(jobj.optString("journalentryid"));
//                //Delete Journal Entry and Details
////                result = accJournalEntryobj.deleteJEDtails(jeid, companyid);
//                //Delete Journal Entry Details
//
//                result = accReceiptDAOobj.getJEFromBR(receiptid, companyid);
//                      List list = result.getEntityList();
//                      Iterator itr = list.iterator();
//                      while(itr.hasNext()) {
//                          String jeid = (String) itr.next();
//                          result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
//                      }
//
////              query = "update JournalEntry je set je.deleted=true where je.ID in(select p.journalEntry.ID from BillingReceipt p where p.ID in("+qMarks +") and p.company.companyID=je.company.companyID) and je.company.companyID=?";
//
//                numRows++;
//            }
////            issuccess = true;
////            msg = "Billing Receipt(s) has been deleted successfully";
//        } catch (UnsupportedEncodingException ex) {
//            throw ServiceException.FAILURE("Can't extract the records. <br>Encoding not supported", ex);
//        } catch (ServiceException ex) {
//            msg = "Selected record(s) is currently used in the transaction(s).";
//            throw new AccountingException(msg);
//           // Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (JSONException ex) {
//            msg = ex.getMessage();
//            throw new AccountingException(msg);
//           // Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return numRows;
//
//    }
    public ModelAndView exportReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        List<JSONObject> list = new ArrayList<JSONObject>();
        List<JSONObject> tempList = new ArrayList<JSONObject>();
        int limitValue = 0, startValue = 0, dataCount = 0;
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            boolean contraentryflag = request.getParameter("contraentryflag") != null;
            boolean isAdvancePayment = request.getParameter("advancePayment") != null;
            boolean isAdvanceFromVendor = request.getParameter("advanceFromVendor") != null;
            boolean isPostDatedCheque = request.getParameter("isPostDatedCheque") != null;
            boolean isDishonouredCheque = request.getParameter("isDishonouredCheque") != null;
            boolean isGlcode = request.getParameter("isGlcode") != null;
            boolean exportPtw = request.getParameter("exportPtw") != null;
            String paymentWindowType="";
            requestParams.put("contraentryflag", contraentryflag);
            requestParams.put("isadvancepayment", isAdvancePayment);
            requestParams.put("isadvancefromvendor", isAdvanceFromVendor);
            requestParams.put("isPostDatedCheque", isPostDatedCheque);
            requestParams.put("isDishonouredCheque", isDishonouredCheque);
            requestParams.put("isGlcode", isGlcode);
            boolean onlyOpeningBalanceTransactionsFlag = false;
            
            boolean allAdvPayment = request.getParameter("allAdvPayment") != null;
            boolean unUtilizedAdvPayment = request.getParameter("unUtilizedAdvPayment") != null;
            boolean partiallyUtilizedAdvPayment = request.getParameter("partiallyUtilizedAdvPayment") != null;
            boolean fullyUtilizedAdvPayment = request.getParameter("fullyUtilizedAdvPayment") != null;
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("allAdvPayment"))) {
                allAdvPayment = Boolean.parseBoolean(request.getParameter("allAdvPayment"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("unUtilizedAdvPayment"))) {
                unUtilizedAdvPayment = Boolean.parseBoolean(request.getParameter("unUtilizedAdvPayment"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("partiallyUtilizedAdvPayment"))) {
                partiallyUtilizedAdvPayment = Boolean.parseBoolean(request.getParameter("partiallyUtilizedAdvPayment"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("fullyUtilizedAdvPayment"))) {
                fullyUtilizedAdvPayment = Boolean.parseBoolean(request.getParameter("fullyUtilizedAdvPayment"));
            }
            
            requestParams.put("allAdvPayment", allAdvPayment);
            requestParams.put("unUtilizedAdvPayment", unUtilizedAdvPayment);
            requestParams.put("partiallyUtilizedAdvPayment", partiallyUtilizedAdvPayment);
            requestParams.put("fullyUtilizedAdvPayment", fullyUtilizedAdvPayment);
            
            request.setAttribute("isExport", true);
            if (!StringUtil.isNullOrEmpty(request.getParameter("onlyOpeningBalanceTransactionsFlag"))) {
                onlyOpeningBalanceTransactionsFlag = Boolean.parseBoolean(request.getParameter("onlyOpeningBalanceTransactionsFlag"));
            }
            if (exportPtw) {
                if(!StringUtil.isNullOrEmpty(request.getParameter("paymentWindowType"))) {
                    requestParams.put("paymentWindowType", Integer.parseInt(request.getParameter("paymentWindowType")));
                }
            }
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            boolean eliminateflag = consolidateFlag;
//            if(consolidateFlag) {
            requestParams.put(Constants.start, "");
            requestParams.put(Constants.limit, "");
//            }
            KwlReturnObject result = null;
            KwlReturnObject billingResult = null;
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);

                tempList.clear();
                if(onlyOpeningBalanceTransactionsFlag){         //To export opening balance records (To Print Data in PDF, CSV format)
                    result = accReceiptDAOobj.getAllOpeningBalanceReceipts(requestParams);
                    tempList = getOpeningBalanceReceiptJsonForReport(request, result.getEntityList(), tempList);
                }else{
                    result = accReceiptDAOobj.getReceipts(requestParams);
                    tempList = getReceiptJson(request, result.getEntityList(), tempList);
//                    billingResult = accReceiptDAOobj.getBillingReceipts(requestParams);
//                    tempList = getBillingReceiptJson(request, billingResult.getEntityList(), tempList);
                }
                Collections.sort(tempList, Collections.reverseOrder(new ReceiptDateComparator()));
                list.addAll(tempList);
            }
//            if(!StringUtil.isNullOrEmpty(limit) && !StringUtil.isNullOrEmpty(start)){
//                limitValue=Integer.parseInt(limit);
//                startValue=Integer.parseInt(start);
//            }
//            else{
            limitValue = list.size();
            startValue = 0;
//            }
            Iterator iterator = list.iterator();
            for (int i = 0; i < list.size(); i++) {
                if (i >= startValue && dataCount < limitValue) {
                    JSONObject jSONObject = (JSONObject) iterator.next();
                    jArr.put(jSONObject);
                    dataCount++;
                }
                if (dataCount == limitValue) {
                    break;
                }
            }
            jobj.put("data", jArr);
            jobj.put("count", list.size());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String startDate = request.getParameter("stdate");
                String endDate = request.getParameter("enddate");
                if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                    startDate = authHandler.getDateOnlyFormat(request).format(authHandler.getDateOnlyFormat(request).parse(startDate));
                    endDate = authHandler.getDateOnlyFormat(request).format(authHandler.getDateOnlyFormat(request).parse(endDate));
                    jobj.put("isFromToDateRequired", true);
                    jobj.put("stdate", startDate);
                    jobj.put("enddate", endDate);
                } else {
                    String GenerateDate = authHandler.getDateOnlyFormat(request).format(new Date());
                    jobj.put("GenerateDate", GenerateDate);
                }
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    //    public ModelAndView exportBillingReceipt(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj = new JSONObject();
//        String view = "jsonView_ex";
//        try{
//            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
//            KwlReturnObject result = accReceiptDAOobj.getBillingReceipts(requestParams);
//            jobj = getBillingReceiptJson(request, result.getEntityList());
//            String fileType = request.getParameter("filetype");
//            if (StringUtil.equal(fileType, "print")) {
//                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
//                jobj.put("GenerateDate", GenerateDate);
//                view = "jsonView-empty";
//            }
//            exportDaoObj.processRequest(request, response, jobj);
//        } catch (SessionExpiredException ex) {
//            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch(Exception ex) {
//            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return new ModelAndView(view, "model", jobj.toString());
//    }
    private class ReceiptDateComparator implements Comparator<JSONObject> {

        @Override
        public int compare(JSONObject o1, JSONObject o2) {  //sort data on date
            int result = 0;
            try {
                Date date1 = new Date(o1.getString("billdate"));
                Date date2 = new Date(o2.getString("billdate"));

                if (date1.getTime() > date2.getTime()) {
                    result = 1;
                } else if (date1.getTime() < date2.getTime()) {
                    result = -1;
                } else {
                    result = 0;
                }
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return result;
        }
    }

    public ModelAndView linkReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = linkReceipt(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.PaymentinformationhasbeenLinkedtoCustomerInvoicesuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List linkReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        GoodsReceipt greceipt = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String paymentid =request.getParameter("paymentid");
            String invoiceids[] = request.getParameter("invoiceids").split(",");
            String paymentno =request.getParameter("paymentno");
            String invoicenos =request.getParameter("invoicenos");
            double amount = 0;

            String amounts[] = request.getParameter("amounts").split(",");    
//            KwlReturnObject grresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
//            Invoice gr = (Invoice) grresult.getEntityList().get(0);

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentid);
            Receipt payment = (Receipt) receiptObj.getEntityList().get(0);

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            JournalEntry je = null;
            String jeid = "";
            if (payment.isNormalReceipt()) {
                je = payment.getJournalEntry();
                jeid = je.getID();
            }

            JournalEntryDetail updatejed = new JournalEntryDetail();
            boolean isopeningBalanceRecceipt = payment.isIsOpeningBalenceReceipt();
            double eternalCurrencyRate = 0d;
            if (!payment.isNormalReceipt() && payment.isIsOpeningBalenceReceipt()) {
                eternalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
            }
            //Commented the code becoz now only advance payment can be linked and in that we dont need to delete the je and create it again
            //Delete entry from optimized table
//            if (payment.isNormalReceipt()) {
//                accJournalEntryobj.deleteOnEditAccountJEs_optimized(jeid);
//                eternalCurrencyRate = je.getExternalCurrencyRate();
//
//                Iterator itrRow = je.getDetails().iterator();
//
//                while (itrRow.hasNext()) {
//                    JournalEntryDetail jed = ((JournalEntryDetail) itrRow.next());
////                if(payment.isIsmanydbcr()) {
////                    if (jed.isDebit()) {
////                        amount -= jed.getAmount();
////                        updatejed = jed;
////                    } else {
////                        amount += jed.getAmount();
////                        accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
////                    }
////                } else {
//                    if (jed.isDebit()) {
//                        amount = jed.getAmount();
//                        updatejed = jed;
//                    } else {
//                        accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
//                    }
////                }
//                }
//            }
            JSONArray jArr = new JSONArray();
            String customerId="";
            String accountId="";
            String linkedInvoiceids="";
            String linkedInvoicenos="";
            Iterator itrRow = payment.getRows().iterator();
           Map<String,Double> paymentRowsHashMap=new HashMap<String, Double>();
            double receiptAmount=0.0;
            if (!payment.getRows().isEmpty()) {//deleting linked data if the Payment is parctially linked
                    while (itrRow.hasNext()) {
                        ReceiptDetail receiptDetail=((ReceiptDetail) itrRow.next());
                        paymentRowsHashMap.put(receiptDetail.getInvoice().getID(), receiptDetail.getAmount());
                        receiptAmount+=receiptDetail.getAmount();
                    }
                accReceiptDAOobj.deleteReceiptDetailsAndUpdateAmountDue(payment.getID(),payment.getCompany().getCompanyID());
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, company.getCompanyID());
                requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                double externalCurrencyRate = 1d;
                boolean isopeningBalanceRP = payment.isIsOpeningBalenceReceipt();
                Date rpCreationDate = null;
                rpCreationDate = payment.getCreationDate();
                if (!payment.isNormalReceipt() && payment.isIsOpeningBalenceReceipt()) {
                    externalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
                } else {
//                    rpCreationDate = payment.getJournalEntry().getEntryDate();
                    externalCurrencyRate = payment.getJournalEntry().getExternalCurrencyRate();
                }
                String fromcurrencyid = payment.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceRP && payment.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, receiptAmount, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, receiptAmount, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                }
                double openingbalanceBaseAmountDue = (Double) bAmt.getEntityList().get(0);
            
                payment.setOpeningBalanceAmountDue(payment.getOpeningBalanceAmountDue()+receiptAmount);
                payment.setOpeningBalanceBaseAmountDue(payment.getOpeningBalanceBaseAmountDue()+openingbalanceBaseAmountDue);
           }
            for (int k = 0; k < invoiceids.length; k++) {//creating a hash map with payment and their linked invoice
                if (StringUtil.isNullOrEmpty(invoiceids[k])) {
                    continue;
                }
                double usedcnamount = 0d;
                if (!StringUtil.isNullOrEmpty(amounts[k])) {
                    usedcnamount = Double.parseDouble((String) amounts[k]);
                } else {
                    usedcnamount = 0;
                }
                if (usedcnamount == 0) {
                    continue;
                }
                KwlReturnObject invoiceresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceids[k]);
                Invoice invoice = (Invoice) invoiceresult.getEntityList().get(0);
                if (linkedInvoiceids.equals("") && invoice != null) {
                    linkedInvoiceids += invoice.getID();
                    linkedInvoicenos += invoice.getInvoiceNumber();
                } else if (invoice != null) {
                    linkedInvoiceids += "," + invoice.getID();
                    linkedInvoicenos += "," + invoice.getInvoiceNumber();
                }
                customerId = invoice.getCustomer().getID();
                accountId = invoice.getCustomer().getAccount().getID();

                JSONObject jobj = new JSONObject();

                if (paymentRowsHashMap.containsKey(invoiceids[k])) {
                    double actualAmount = paymentRowsHashMap.get(invoiceids[k]);
                    jobj.put("payment", Double.parseDouble(amounts[k]) + actualAmount);
                    paymentRowsHashMap.remove(invoiceids[k]);
                } else {
                    jobj.put("payment", amounts[k]);
                }

                jobj.put("billid", invoiceids[k]);
                jobj.put("isopeningBalanceRecceipt", isopeningBalanceRecceipt);
                jobj.put("isConversionRateFromCurrencyToBase", payment.isConversionRateFromCurrencyToBase());
                jArr.put(jobj);
            }

            for (Entry<String, Double> entry : paymentRowsHashMap.entrySet()) {//creating json for saving linked data
                JSONObject jobj = new JSONObject();
                String key = entry.getKey();
                Double value = entry.getValue();
                jobj.put("payment", value);
                jobj.put("billid", key);
                jobj.put("isopeningBalancePayment", isopeningBalanceRecceipt);
                jobj.put("isConversionRateFromCurrencyToBase", payment.isConversionRateFromCurrencyToBase());
                jArr.put(jobj);
            }
            HashMap<String, Object> paymenthm = new HashMap<String, Object>();
            HashSet payDetails = saveReceiptRows(payment, company, jArr, greceipt);
            paymenthm.put("receiptid", payment.getID());
            paymenthm.put("receiptdetails", payDetails);
            paymenthm.put("customerId", customerId);
            accReceiptDAOobj.saveReceipt(paymenthm);

//Commented the code becoz now only advance payment can be linked and in that we dont need to delete the je and create it again
//            JournalEntryDetail updatejed1 = null;
//            if (payment.isNormalReceipt()) {
//                JSONObject jedjson = new JSONObject();
//                jedjson.put("companyid", companyid);
//                jedjson.put("srno", 2);
//                jedjson.put("amount", amount);
//                jedjson.put("accountid", accountId);
//                jedjson.put("debit", false);
//                jedjson.put("jeid", jeid);
//                KwlReturnObject kjed = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                updatejed1 = (JournalEntryDetail) kjed.getEntityList().get(0);
//            }
//
//            if (jArr.length() > 0) {
//                boolean rateDecreased = false;
//                amount = 0;
//                JSONObject jobj = new JSONObject();
//                for (int i = 0; i < jArr.length(); i++) {
//                    jobj = jArr.getJSONObject(i);
//                    amount += jobj.getDouble("payment");
//                }
//                double amountDiff = oldReceiptRowsAmount(request, jArr, payment.getCurrency().getCurrencyID(), eternalCurrencyRate);
//                if (preferences.getForeignexchange() == null) {
//                    throw new AccountingException(messageSource.getMessage("acc.pay.forex", null, RequestContextUtils.getLocale(request)));
//                }
//                if (amountDiff != 0 && preferences.getForeignexchange() != null && payment.isNormalReceipt()) {
//                    if (amountDiff < 0) {
//                        rateDecreased = true;
//                    }
//                    JSONObject jParam = new JSONObject();
//                    jParam.put("srno", 3);
//                    jParam.put("companyid", companyid);
//                    jParam.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
//                    jParam.put("accountid", preferences.getForeignexchange().getID());
//                    jParam.put("debit", rateDecreased ? false : true);
//                    jParam.put("jeid", jeid);
//                    accJournalEntryobj.addJournalEntryDetails(jParam);
//
//                    jParam = new JSONObject();
//                    jParam.put("jedid", updatejed1.getID());
//                    jParam.put("amount", rateDecreased ? (updatejed.getAmount() + amountDiff) : updatejed1.getAmount() + amountDiff);
//                    accJournalEntryobj.updateJournalEntryDetails(jParam);
//                }
//            }
//
//            //Insert new entries again in optimized table.
//            if (payment.isNormalReceipt()) {
//                accJournalEntryobj.saveAccountJEs_optimized(jeid);
//            }
           auditTrailObj.insertAuditLog(AuditAction.LINKEDRECEIPT, "User " + sessionHandlerImpl.getUserFullName(request) + " has linked Receipt"+paymentno+" to "+linkedInvoicenos, request,linkedInvoiceids); 
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ModelAndView linkBillingReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = linkBillingReceipt(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.PaymentinformationhasbeenLinkedtoCustomerInvoicesuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List linkBillingReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        BillingGoodsReceipt bgreceipt = null;//Set only for contra entry
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String paymentid = request.getParameter("paymentid");
            String invoiceid = request.getParameter("invoiceid");
            double amount = 0;

            KwlReturnObject grresult = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), invoiceid);
            BillingInvoice gr = (BillingInvoice) grresult.getEntityList().get(0);

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(BillingReceipt.class.getName(), paymentid);
            BillingReceipt payment = (BillingReceipt) receiptObj.getEntityList().get(0);


            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            JournalEntry je = payment.getJournalEntry();
            String jeid = je.getID();

            //Delete entry from optimized table
            accJournalEntryobj.deleteOnEditAccountJEs_optimized(jeid);

            Iterator itrRow = je.getDetails().iterator();
            JournalEntryDetail updatejed = new JournalEntryDetail();
            while (itrRow.hasNext()) {
                JournalEntryDetail jed = ((JournalEntryDetail) itrRow.next());
//                if(payment.isIsmanydbcr()) {
//                    if (jed.isDebit()) {
//                        amount -= jed.getAmount();
//                        updatejed = jed;
//                    } else {
//                        amount += jed.getAmount();
//                        accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
//                    }
//                } else {
                if (jed.isDebit()) {
                    amount = jed.getAmount();
                    updatejed = jed;
                } else {
                    accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
                }
            }
            JSONArray jArr = new JSONArray();
            JSONObject jobj = new JSONObject();
            jobj.put("payment", amount);
            jobj.put("billid", invoiceid);
            jArr.put(jobj);

            HashMap<String, Object> paymenthm = new HashMap<String, Object>();
            HashSet payDetails = saveBillingReceiptRows(payment, company, jArr, bgreceipt);
            paymenthm.put("billingreceiptid", payment.getID());
            paymenthm.put("receiptdetails", payDetails);
            accReceiptDAOobj.saveBillingReceipt(paymenthm);

            JSONObject jedjson = new JSONObject();
            jedjson.put("companyid", companyid);
            jedjson.put("srno", 2);
            jedjson.put("amount", amount);
            jedjson.put("accountid", gr.getCustomer().getID());
            jedjson.put("debit", false);
            jedjson.put("jeid", jeid);
            KwlReturnObject kjed = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail updatejed1 = (JournalEntryDetail) kjed.getEntityList().get(0);

            if (jArr.length() > 0) {
                boolean rateDecreased = false;
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) {
                    jobj = jArr.getJSONObject(i);
                    amount += jobj.getDouble("payment");
                }
                double amountDiff = oldBillingReceiptRowsAmount(request, jArr, payment.getCurrency().getCurrencyID(), je.getExternalCurrencyRate());
                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.pay.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null) {
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    //amountDiff = amount / amountDiff;
                    JSONObject jParam = new JSONObject();
                    jParam.put("srno", 3);
                    jParam.put("companyid", companyid);
                    jParam.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jParam.put("accountid", preferences.getForeignexchange().getID());
                    jParam.put("debit", rateDecreased ? false : true);
                    jParam.put("jeid", jeid);
                    accJournalEntryobj.addJournalEntryDetails(jParam);
                    //jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    //jedetails.add(jed);

                    jParam = new JSONObject();
                    jParam.put("jedid", updatejed1.getID());
                    jParam.put("amount", rateDecreased ? (updatejed.getAmount() + amountDiff) : (updatejed1.getAmount() + amountDiff));
                    accJournalEntryobj.updateJournalEntryDetails(jParam);

                }
            }

            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ModelAndView deleteReceiptMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteReceiptMerged(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.receipt.del", null, RequestContextUtils.getLocale(request));   //"Receipt(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteReceiptMerged(HttpServletRequest request) throws AccountingException, SessionExpiredException, ServiceException {
        try {
            String receiptsJson = request.getParameter("data");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray(receiptsJson);
            KwlReturnObject result1;
            String receiptid = "", jeid, receiptno = "";
            KwlReturnObject result;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                receiptid = StringUtil.DecodeText(jobj.optString("billid"));
                jeid = StringUtil.DecodeText(jobj.optString("journalentryid"));
                receiptno = jobj.getString("billno");
                boolean withoutinventory = Boolean.parseBoolean(jobj.getString("withoutinventory"));
                if (withoutinventory) {

                    //Delete Billing receipt details
//                result = accReceiptDAOobj.deleteBillingReceiptDetails(receiptid, companyid);
                    //Delete Billing Receipt
                    result = accReceiptDAOobj.deleteBillingReceiptEntry(receiptid, companyid);

//                jeid = StringUtil.DecodeText(jobj.optString("journalentryid"));
                    //Delete Journal Entry and Details
//                result = accJournalEntryobj.deleteJEDtails(jeid, companyid);
                    //Delete Journal Entry Details

                    result = accReceiptDAOobj.getJEFromBR(receiptid, companyid);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        String jeId = (String) itr.next();
                        result = accJournalEntryobj.deleteJournalEntry(jeId, companyid);
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteAccountJEs_optimized(jeId);
                    }
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(BillingReceipt.class.getName(), receiptid);
                    BillingReceipt receipt = (BillingReceipt) objItr.getEntityList().get(0);
                    if (receipt != null && receipt.getRevalJeId() != null) {
                        result = accJournalEntryobj.deleteJournalEntry(receipt.getRevalJeId(), companyid);
                    }
//              query = "update JournalEntry je set je.deleted=true where je.ID in(select p.journalEntry.ID from BillingReceipt p where p.ID in("+qMarks +") and p.company.companyID=je.company.companyID) and je.company.companyID=?";
                } else {

                    /*
                     * //Delete Billing receipt details result =
                     * accReceiptDAOobj.deleteReceiptDetails(receiptid,
                     * companyid); //Delete Billing Receipt result =
                     * accReceiptDAOobj.deleteReceipt(receiptid, companyid);
                     *
                     * jeid = jobj.getString("journalentryid"); //Delete Journal
                     * Entry and Details result =
                     * accJournalEntryobj.deleteJEDtails(jeid, companyid);
                     * //Delete Journal Entry Details result =
                     * accJournalEntryobj.deleteJE(jeid, companyid);
                     */

//                query = "update Receipt set deleted=true where ID in("+qMarks +") and company.companyID=?";
//                HibernateUtil.executeUpdate(session, query, params.toArray());
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                    Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                    if (receipt != null) {
                        updateOpeningBalance(receipt, companyid);
                    }
                    result = accReceiptDAOobj.deleteReceiptEntry(receiptid, companyid);

                    //                query = "update JournalEntry je set je.deleted=true where je.ID in(select r.journalEntry.ID from Receipt r where r.ID in("+qMarks +") and r.company.companyID=je.company.companyID) and je.company.companyID=?";
//                HibernateUtil.executeUpdate(session, query, params.toArray());
                    result = accReceiptDAOobj.getJEFromReceipt(receiptid);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        String jeId = (String) itr.next();
                        result = accJournalEntryobj.deleteJournalEntry(jeId, companyid);
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteAccountJEs_optimized(jeId);
                    }
                    if (receipt != null && receipt.getRevalJeId() != null) {
                        result = accJournalEntryobj.deleteJournalEntry(receipt.getRevalJeId(), companyid);
                    }

                }

                result1 = accReceiptDAOobj.getreceipthistory(receiptid);
                List ls = result1.getEntityList();
                Iterator<Object[]> itr1 = ls.iterator();
                while (itr1.hasNext()) {
                    Object[] row = (Object[]) itr1.next();
                    String dnid = row[0].toString();
                    Double amount = Double.parseDouble(row[1].toString());
                    KwlReturnObject dnidresult = accReceiptDAOobj.updateDnUpAmount(dnid, amount);
                }

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("oldjeid", jeid);
                requestParams.put("companyId", companyid);
                deleteBankReconcilation(requestParams);

            }
            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a Receipt " + receiptno, request, receiptid);
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

    public ModelAndView deleteReceiptForEdit(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean isAdvancePayment=StringUtil.getBoolean(request.getParameter("isadvpayment"));
            boolean advanceAmountFlag=StringUtil.getBoolean(request.getParameter("advanceAmountFlag"));
            boolean invoiceAmountFlag=StringUtil.getBoolean(request.getParameter("invoiceAmountFlag"));
            boolean cndnAmountFlag=StringUtil.getBoolean(request.getParameter("cndnAmountFlag"));         
            JSONArray jArr=new JSONArray();
            HashMap<Integer,String> paymentHashMap=new HashMap<Integer, String>();
            int invoiceadvcndntype=0;
            if(request.getParameter("datainvoiceadvcndn")!=null){
                JSONArray jSONArray=new JSONArray(request.getParameter("datainvoiceadvcndn"));
                String paymentid = "", paymentno = "",paymentIds="";
                KwlReturnObject result, resultMain;
                
                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONObject jObject = jSONArray.getJSONObject(i);
                    paymentid = StringUtil.DecodeText(jObject.optString("paymentID"));
                    int invoiceadvcndntypejson=!StringUtil.isNullOrEmpty(jObject.getString("invoiceadvcndntype"))?Integer.parseInt(jObject.getString("invoiceadvcndntype")):0;
                    paymentHashMap.put(invoiceadvcndntypejson, paymentid);
                }
                invoiceadvcndntype=!StringUtil.isNullOrEmpty(request.getParameter("invoiceadvcndntype"))?Integer.parseInt(request.getParameter("invoiceadvcndntype")):0;             
            }

//            if(request.getParameter("mainData")!=null){
//                jArr=new JSONArray(request.getParameter("mainData"));
//                String receipid = "", paymentno = "";
//                KwlReturnObject result, resultMain;
//                for (int i = 0; i < jArr.length(); i++) {
//                    JSONObject jObject = jArr.getJSONObject(i);
//                    receipid = StringUtil.DecodeText(jObject.optString("billid"));
//                    resultMain = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receipid);
//                    if (!resultMain.getEntityList().isEmpty()&&resultMain.getEntityList().get(0)!=null) {
//                        Receipt receipt = (Receipt) resultMain.getEntityList().get(0);
//                        receipt.setAdvanceid(null);
//                        accReceiptDAOobj.saveReceiptObject(receipt);
//                    }
//                }
//                txnManager.commit(status);
//                status = txnManager.getTransaction(def);
//            }
//            deleteReceiptPermanent(request);
            
            if (request.getParameter("datainvoiceadvcndn") != null) {
                if (paymentHashMap.containsKey(2) && !paymentHashMap.containsKey(3) && !paymentHashMap.containsKey(1)) {
                    deleteReceiptPermanent(request);
                } 
//                else if (invoiceadvcndntype==3&&paymentHashMap.containsKey(3) && !paymentHashMap.containsKey(1) && paymentHashMap.containsKey(2)) {
//                    deleteReceiptPermanent(request, paymentHashMap.get(2));
//                } 
                else {
                    if (advanceAmountFlag && paymentHashMap.containsKey(2)) {
                        deleteReceiptPermanent(request, paymentHashMap.get(2));
                    }
                    if (invoiceAmountFlag && paymentHashMap.containsKey(1)) {
                        deleteReceiptPermanent(request, paymentHashMap.get(1));
                    }
                    if (cndnAmountFlag && paymentHashMap.containsKey(3)) {
                        deleteReceiptPermanent(request, paymentHashMap.get(3));
                    }
                }
            } else {
                deleteReceiptPermanent(request);
            }
            
            
            
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.receipt.del", null, RequestContextUtils.getLocale(request));   //"Receipt(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //function for deletion of recive payment receipt
    public ModelAndView deleteReceiptPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteReceiptPermanent(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.receipt.del", null, RequestContextUtils.getLocale(request));   //"Receipt(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + (ex.getMessage() != null ? ex.getMessage() : ex.getCause().getMessage());
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteReceiptPermanent(HttpServletRequest request) throws AccountingException, SessionExpiredException, ServiceException, ParseException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String receiptid = "", receiptno = "";
            KwlReturnObject result;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                receiptid = StringUtil.DecodeText(jobj.optString("billid"));
                receiptno = jobj.getString("billno");
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                if (receipt != null && receipt.getRevalJeId() != null) {
                    result = accJournalEntryobj.deleteJEDtails(receipt.getRevalJeId(), companyid);// 2 For realised JE
                    result = accJournalEntryobj.deleteJE(receipt.getRevalJeId(), companyid);
                }
                if (receipt != null) {
                    updateOpeningBalance(receipt, companyid);
                }
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("receiptid", receiptid);
                requestParams.put("companyid", companyid);
                requestParams.put("receiptno", receiptno);
                if (!StringUtil.isNullOrEmpty(receiptid)) {
                    DateFormat dateFormatForLock = authHandler.getDateOnlyFormat(request);
                    Date entryDateForLock = null;
                    if (jobj.has("billdate")) {
                        entryDateForLock = dateFormatForLock.parse(jobj.getString("billdate"));
                    }
                    if (entryDateForLock != null) {
                        requestParams.put("entrydate", entryDateForLock);
                        requestParams.put("df", dateFormatForLock);
                    }
                    accReceiptDAOobj.deleteReceiptPermanent(requestParams);
                    auditTrailObj.insertAuditLog(AuditAction.RECEIPT_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a Receipt permanently " + receiptno, request, receiptid);

                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }
public void deleteReceiptPermanent(HttpServletRequest request,String receiptId) throws AccountingException, SessionExpiredException, ServiceException, ParseException {
        try{
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String receiptid = "", receiptno = "";
            KwlReturnObject result;

                receiptid = receiptId;
               KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                     Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                     if(receipt!=null && receipt.getRevalJeId()!=null){
                         result = accJournalEntryobj.deleteJEDtails(receipt.getRevalJeId(), companyid);// 2 For realised JE
                         result = accJournalEntryobj.deleteJE(receipt.getRevalJeId(), companyid);
                     }  
                     if(receipt!=null){
                         updateOpeningBalance(receipt,companyid);
                     }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
             requestParams.put("receiptid",receiptid);
             requestParams.put("companyid",companyid);
             if (!StringUtil.isNullOrEmpty(receiptid)) {   
                 DateFormat dateFormatForLock = authHandler.getDateOnlyFormat(request);
                 Date entryDateForLock = null;
                 if (receipt!=null&&receipt.getCreationDate()!=null) {
//                     entryDateForLock = dateFormatForLock.parse(jobj.getString("billdate"));
                     entryDateForLock = receipt.getCreationDate();
                 }
                 if (entryDateForLock != null) {
                     requestParams.put("entrydate", entryDateForLock);
                     requestParams.put("df", dateFormatForLock);
                 }
                 accReceiptDAOobj.deleteReceiptPermanent(requestParams);                        
                 auditTrailObj.insertAuditLog(AuditAction.RECEIPT_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a Receipt permanently "  +receiptno , request, receiptid);
          
            }
        
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

    /*
     * Update invoice due amount when payment is being deleted.
     */
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
                        double amountPaid = row.getAmount();
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
                        KwlReturnObject bDiscountAmt = null;
                        if (isopeningBalanceRP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountPaid, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                            bDiscountAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, discountAmount, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                            bDiscountAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, discountAmount, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                        }
                        
                        double openingbalanceBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                        discountAmount = (Double) bDiscountAmt.getEntityList().get(0);

                        double invoiceAmountDue = invoice.getOpeningBalanceAmountDue()+discountAmount;
                        double invoiceBaseAmountDue = invoice.getOpeningBalanceBaseAmountDue() + openingbalanceBaseAmountDue+discountAmount;
//                        invoiceAmountDue += amountPaid;
                        if (row.getExchangeRateForTransaction() != 0) {
                            invoiceAmountDue += amountPaid / row.getExchangeRateForTransaction();
                        } else {
                            invoiceAmountDue += amountPaid;
                        }
                        JSONObject invjson = new JSONObject();
                        invjson.put("invoiceid", invoice.getID());
                        invjson.put("companyid", companyId);
                        invjson.put("openingBalanceAmountDue", (invoiceAmountDue));
                        invjson.put(Constants.openingBalanceBaseAmountDue, invoiceBaseAmountDue);
                        accInvoiceDAOObj.updateInvoice(invjson, null);
                    } else if (row.getInvoice().isNormalInvoice()){
                        double amountPaid = row.getAmount();
                        Invoice invoice = row.getInvoice();

                        double invoiceAmountDue = invoice.getInvoiceamountdue() + discountAmount;
                        
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

    public ModelAndView deleteOpeningReceiptPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteOpeningReceiptPermanent(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.receipt.del", null, RequestContextUtils.getLocale(request));   //"Receipt(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteOpeningReceiptPermanent(HttpServletRequest request) throws AccountingException, SessionExpiredException, ServiceException {

        String companyid = sessionHandlerImpl.getCompanyid(request);
        String receiptid[] = request.getParameterValues("billidArray");
        String invoiceno[] = request.getParameterValues("invoicenoArray");
        Map<String, Object> requestMap = AccountingManager.getGlobalParams(request);
        HashMap<String,Object> reconcileMap = new HashMap<>();
        for (int count = 0; count < receiptid.length; count++) {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("receiptid", receiptid[count]);
            requestParams.put("companyid", companyid);
            try {
                if (!StringUtil.isNullOrEmpty(receiptid[count])) {

                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid[count]);
                    Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                    if (receipt != null) {
                        updateOpeningBalance(receipt, companyid);

                    }
                    /**
                     * Checking whether the record being deleted is reconciled
                     * if yes showing not allowing user to delete the record and
                     * displaying "You can not delete <document number> as it is
                     * reconciled.If you want to delete, please un-reconcile
                     * it".ERP-33261.
                     */
                    reconcileMap.put("isOpeningDocument", true);
                    reconcileMap.put("transactionId", receipt != null ? receipt.getID() : "");
                    reconcileMap.put(Constants.companyKey, companyid);
                    boolean isReconciledFlag = accBankReconciliationObj.isRecordReconciled(reconcileMap);
                    if (isReconciledFlag) {          
                        String receiptNumber = receipt.getReceiptNumber();
                        throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null, RequestContextUtils.getLocale(request)) + " " + "<b>" + receiptNumber + "</b>" + " " + messageSource.getMessage("acc.reconcilation.asitisreconciled", null, RequestContextUtils.getLocale(request)));
                    }
                    if (receipt.getLinkDetailReceipts()!=null&&!receipt.getLinkDetailReceipts().isEmpty()) {
                        accReceiptDAOobj.deleteLinkReceiptsDetailsAndUpdateAmountDue(requestMap,receipt.getID(),companyid,false);
                    }
                    if(receipt.getLinkDetailReceiptsToDebitNote() != null && !receipt.getLinkDetailReceiptsToDebitNote().isEmpty()){
                        accReceiptDAOobj.deleteLinkReceiptsDetailsToDebitNoteAndUpdateAmountDue(receipt.getID(),companyid,false);
                    }
                    //Delete Unrealised JE for Opening Receipt JE 
                    accJournalEntryobj.permanentDeleteJournalEntryDetailReval(receiptid[count], companyid);
                    accJournalEntryobj.permanentDeleteJournalEntryReval(receiptid[count], companyid);
                    /**
                     * Deleting Reevaluation History of the receipt being
                     * deleted.
                     */
                    JSONObject historyDeleteParams = new JSONObject();
                    historyDeleteParams.put("documentId", receiptid[count]);
                    historyDeleteParams.put("companyID", companyid);
                    accJournalEntryobj.deleteRevaluationHistory(historyDeleteParams);
                    accReceiptDAOobj.deleteReceiptPermanent(requestParams);
                    requestParams.put("transactionID", receiptid[count]);
                    deleteBankReconcilationOfOpeningBalances(requestParams);
                    auditTrailObj.insertAuditLog(AuditAction.OPENING_BALANCE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted an Opening Balance Receipt Permanently " + invoiceno[count], request, receiptid[count]);
                }


            } catch(AccountingException accEx){
                throw accEx;
            }catch (Exception ex) {
                throw new AccountingException(messageSource.getMessage("acc.pay1.excp1", null, RequestContextUtils.getLocale(request)));
            }
        }
    }

    public ModelAndView updatePrint(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = null;
            String recordids = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("recordids"))) {
                recordids = request.getParameter("recordids");
            }
            ArrayList<String> SOIDList = CustomDesignHandler.getSelectedBillIDs(recordids);
            for (int cnt = 0; cnt < SOIDList.size(); cnt++) {
                boolean withInventory = Boolean.parseBoolean(request.getParameter("withInv"));
                HashMap<String, Object> hm = new HashMap<String, Object>();
                hm.put("receiptid", SOIDList.get(cnt));
                hm.put("isprinted", request.getParameter("isprinted"));
                if (!StringUtil.isNullOrEmpty(SOIDList.get(cnt))) {
                    result = accReceiptDAOobj.saveReceipt(hm);
                }
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    } 
     
     public ModelAndView getInvoiceAdvanceCNDN(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
            String msg="";
            boolean issuccess = false;

//            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//            def.setName("VP_Tx");
//            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//            TransactionStatus status = txnManager.getTransaction(def);
            try {
                String companyid = sessionHandlerImpl.getCompanyid(request);
                JSONArray jArr=new JSONArray();
                HashMap<String,String> paymentHashMap=new HashMap<String, String>();
                if(request.getParameter("selectedData")!=null){
                    jArr=new JSONArray(request.getParameter("selectedData"));
                    if(jArr.length()>0){
                        JSONObject jSONObject=jArr.getJSONObject(0);
                         if(!StringUtil.isNullOrEmpty(jSONObject.optString("billid",""))){
                            paymentHashMap.put("paymentId", jSONObject.getString("billid"));
                            paymentHashMap.put("invoiceadvcndntype", jSONObject.getString("invoiceadvcndntype"));
                             KwlReturnObject result = accReceiptDAOobj.getInvoiceAdvPaymentList(paymentHashMap);
                             List paymentList=result.getEntityList();
                              Iterator iter = paymentList.iterator();
                              JSONArray jrr=new JSONArray();
                              while (iter.hasNext()) {  
                                    JSONObject jSONObj=new JSONObject();
                                    Receipt receipt = (Receipt) iter.next();
                                    jSONObj.put("paymentID",receipt.getID());
                                    jSONObj.put("invoiceadvcndntype",receipt.getInvoiceAdvCndnType());
                                    jrr.put(jSONObj);
                            }              
                            jobj.put("data",jrr);
                            jobj.put("count",paymentList.size());
                         }
                    }
                    
                }
//                txnManager.commit(status);
                issuccess = true;
                msg = messageSource.getMessage("acc.pay.del", null, RequestContextUtils.getLocale(request));  //"Payment(s) has been deleted successfully";
            } catch (SessionExpiredException ex) {
//                txnManager.rollback(status);
                msg = ex.getMessage();
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
//                txnManager.rollback(status);
                msg = ""+ex.getMessage();
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            } finally{
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return new ModelAndView("jsonView", "model", jobj.toString());
    }
 
}
