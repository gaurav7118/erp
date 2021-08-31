
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

package com.krawler.spring.accounting.vendorpayment;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.FileUploadHandler;
import com.krawler.esp.handlers.StorageHandler;

import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.creditnote.accCreditNoteController;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteService;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.goodsreceipt.AccGoodsReceiptServiceDAO;
import com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants.COUNT;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants.DATA;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptCMN;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptControllerCMN;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.handler.CommonEnglishNumberToWords;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceHandler;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceController;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.jasperreports.*;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.receipt.AccReceiptServiceDAO;
import com.krawler.spring.accounting.receipt.accReceiptControllerCMN;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderController;
import com.krawler.spring.accounting.tax.accTaxDAO;
import static com.krawler.spring.accounting.vendorpayment.accVendorPaymentController.getPaymentMap;
import com.krawler.spring.accounting.vendorpayment.service.AccVendorPaymentModuleService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFuctionality.AccExportReportsServiceDAO;
import com.krawler.spring.exportFuctionality.ExportRecordHandler;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.mainaccounting.service.AccMainAccountingService;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.velocity.app.VelocityEngine;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
/**
 *
 * @author krawler
 */
public class accVendorPaymentControllerCMN extends MultiActionController implements MessageSourceAware{
    private accVendorPaymentDAO accVendorPaymentDAO;
    private AccExportReportsServiceDAO accExportReportsServiceDAOobj;
    private AccExportReportsServiceDAO accExportOtherReportsServiceDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accTaxDAO accTaxObj;
    private accGoodsReceiptCMN accGoodsReceiptCMN;
    private String successView;
    private MessageSource messageSource;
    private accAccountDAO accAccountDAOobj;
    private CustomDesignDAO customDesignDAOObj;
    private VelocityEngine velocityEngine;
    private authHandlerDAO authHandlerDAOObj;
    private AccMainAccountingService accMainAccountingService;
    private accCurrencyDAO accCurrencyDAOobj;
    private CommonEnglishNumberToWords EnglishNumberToWordsOjb = new CommonEnglishNumberToWords();
    private AccCommonTablesDAO accCommonTablesDAO;
    private accBankReconciliationDAO accBankReconciliationObj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private AccVendorPaymentServiceDAO accVendorPaymentServiceDAOobj;
    private accReceiptDAO accReceiptDAOobj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private AccInvoiceServiceDAO accInvoiceServiceDAOObj;
    private AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private exportMPXDAOImpl exportDaoObj;
    private accCreditNoteService accCreditNoteService;
    private AccReceiptServiceDAO accReceiptServiceDAOobj;
    private AccVendorPaymentModuleService accVendorPaymentModuleServiceObj;   

    /**
     * @param accVendorPaymentModuleServiceObj the
     * accVendorPaymentModuleServiceObj to set
     */
    public void setAccVendorPaymentModuleServiceObj(AccVendorPaymentModuleService accVendorPaymentModuleServiceObj) {
        this.accVendorPaymentModuleServiceObj = accVendorPaymentModuleServiceObj;
    }
    
    public void setAccInvoiceServiceDAOObj(AccInvoiceServiceDAO accInvoiceServiceDAOObj) {
        this.accInvoiceServiceDAOObj = accInvoiceServiceDAOObj;
    }

    public void setAccGoodsReceiptServiceDAOObj(AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj) {
        this.accGoodsReceiptServiceDAOObj = accGoodsReceiptServiceDAOObj;
    }
    
    
    @Override
    public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}
    public void setaccExportReportsServiceDAO(AccExportReportsServiceDAO accExportReportsServiceDAOobj) {
        this.accExportReportsServiceDAOobj = accExportReportsServiceDAOobj;
    }
    public void setaccExportOtherReportsServiceDAO(AccExportReportsServiceDAO accExportReportsServiceDAOobj) {
        this.accExportOtherReportsServiceDAOobj = accExportReportsServiceDAOobj;
    }
    public void setAccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentDAO) {
        this.accVendorPaymentDAO = accVendorPaymentDAO;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCMN) {
        this.accGoodsReceiptCMN = accGoodsReceiptCMN;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }   
    public void setcustomDesignDAO(CustomDesignDAO customDesignDAOObj) {
        this.customDesignDAOObj = customDesignDAOObj;
    }
    public void setvelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }
    public void setAccMainAccountingService(AccMainAccountingService accMainAccountingService) {
        this.accMainAccountingService = accMainAccountingService;
    }
    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {  //Neeraj
        this.accCurrencyDAOobj = accCurrencyobj;
    }
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    public void setaccVendorPaymentServiceDAO(AccVendorPaymentServiceDAO accVendorPaymentServiceDAOobj) {
        this.accVendorPaymentServiceDAOobj = accVendorPaymentServiceDAOobj;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }
      public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }
    public void setKwlCommonTablesDAOObj(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    
    public void setaccCreditNoteService(accCreditNoteService accCreditNoteService) {
        this.accCreditNoteService = accCreditNoteService;
    }
    public void setAccReceiptServiceDAO(AccReceiptServiceDAO accReceiptServiceDAOobj) {
        this.accReceiptServiceDAOobj = accReceiptServiceDAOobj;
    }
    public static HashMap<String, Object> getPaymentMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("start", request.getParameter("start"));
        requestParams.put("limit", request.getParameter("limit"));
        requestParams.put("ss", request.getParameter("ss"));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        requestParams.put(Constants.REQ_startdate, request.getParameter("stdate"));
        requestParams.put(Constants.REQ_enddate, request.getParameter("enddate"));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(InvoiceConstants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        requestParams.put(Constants.isRepeatedPaymentFlag, request.getParameter(Constants.isRepeatedPaymentFlag));
        if (request.getParameter("dir") != null && !StringUtil.isNullOrEmpty(request.getParameter("dir"))
                && request.getParameter("sort") != null && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
            requestParams.put("dir", request.getParameter("dir"));
            requestParams.put("sort", request.getParameter("sort"));
        }
        if(!StringUtil.isNullOrEmpty(request.getParameter(Constants.requestModuleId))){
            requestParams.put(Constants.requestModuleId, Integer.parseInt(request.getParameter(Constants.requestModuleId)));
        }
        return requestParams;
    }
    
    public ModelAndView getPayments(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            int permCode = sessionHandlerImpl.getPerms(request, Constants.VENDOR_PERMCODE);
            paramJobj.put(Constants.permCode, permCode);
            jobj= accVendorPaymentServiceDAOobj.getPaymentsJSON(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getPaymentRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        try{
            jobj = getPaymentRows(request, true);
            issuccess = true;
            msg = messageSource.getMessage("acc.main.rec", null, RequestContextUtils.getLocale(request));   //"Records fetched successfully";
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getPaymentRowsNew(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        try{
            jobj = getPaymentRowsNew(request, true);
            issuccess = true;
            msg = messageSource.getMessage("acc.main.rec", null, RequestContextUtils.getLocale(request));   //"Records fetched successfully";
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
       
    public JSONObject getPaymentRows(HttpServletRequest request, boolean flag) throws SessionExpiredException, ServiceException{
    JSONObject jobj = new JSONObject();
    try{
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        requestParams.put("dateformat", authHandler.getDateOnlyFormat());
        if(!StringUtil.isNullOrEmpty(request.getParameter("bills"))){
            requestParams.put("bills", request.getParameterValues("bills"));
        }
        requestParams.put("isReceiptEdit", request.getParameter("isReceiptEdit"));
        requestParams.put("isForReport" ,"report".equals(request.getParameter("dtype")) ? true : false );
        requestParams.put("currencyfilterfortrans", (request.getParameter("currencyfilterfortrans") == null)? "" : request.getParameter("currencyfilterfortrans"));
        JSONArray DataJArr = new JSONArray();
        DataJArr = getPaymentRowsJson(requestParams);
        jobj.put(Constants.RES_data, DataJArr.length()>0?DataJArr:"");
    }
    catch (JSONException ex) {
        throw ServiceException.FAILURE("getPaymentRows : " + ex.getMessage(), ex);
//            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
    }
    return jobj;
}
     public JSONObject getPaymentRowsNew(HttpServletRequest request, boolean flag) throws SessionExpiredException, ServiceException{
        JSONObject jobj = new JSONObject();
        try{
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("dateformat", authHandler.getDateOnlyFormat());
            requestParams.put("userdateformat", authHandler.getUserDateFormatterWithoutTimeZone(request));
            if(!StringUtil.isNullOrEmpty(request.getParameter("bills"))){
                requestParams.put("bills", request.getParameter("bills").toString().split(","));
            }
            if(request.getParameter(Constants.isExport)!=null){
                requestParams.put(Constants.isExport, request.getParameter(Constants.isExport));
            }
            if(request.getParameter(Constants.isForReport)!=null){
                requestParams.put(Constants.isForReport, request.getParameter(Constants.isForReport));
            }
            requestParams.put("isReceiptEdit", request.getParameter("isReceiptEdit"));
            requestParams.put("currencyfilterfortrans", (request.getParameter("currencyfilterfortrans") == null)? "" : request.getParameter("currencyfilterfortrans"));
            JSONArray DataJArr = new JSONArray();
            DataJArr = accVendorPaymentServiceDAOobj.getPaymentDetailJsonNew(requestParams);
            jobj.put(Constants.RES_data, DataJArr.length()>0?DataJArr:"");
        }
        catch (JSONException ex) {
            throw ServiceException.FAILURE("getPaymentRows : " + ex.getMessage(), ex);
        }
        return jobj;
    }
    public JSONArray getPaymentRowsJson(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray JArr = new JSONArray();
        try {
            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            String companyid = (String) requestParams.get("companyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            double taxPercent=0;
            DateFormat df = (DateFormat) requestParams.get("dateformat");
            String[] payment = null ;
                if(requestParams.containsKey("bills")){
                    payment =(String[]) requestParams.get("bills");
                }
            boolean isVendorPaymentEdit = Boolean.parseBoolean((String)requestParams.get("isReceiptEdit"));
            boolean isForReport = false;
            if(requestParams.containsKey("isForReport")){
                isForReport =(Boolean)requestParams.get("isForReport");
            }
           int i = 0;
            HashMap<String, Object> pRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("payment.ID");
            order_by.add("srno");
            order_type.add("asc");
            pRequestParams.put("filter_names", filter_names);
            pRequestParams.put("filter_params", filter_params);
            pRequestParams.put("order_by", order_by);
            pRequestParams.put("order_type", order_type);

            JSONArray jArr = new JSONArray();
            while (payment != null && i < payment.length) {
//                Payment re = (Payment) session.get(Payment.class, payment[i]);
                KwlReturnObject presult = accountingHandlerDAOobj.getObject(Payment.class.getName(), payment[i]);
                Payment re = (Payment) presult.getEntityList().get(0);
//                Iterator itr = re.getRows().iterator();
                filter_params.clear();
                filter_params.add(re.getID());
                KwlReturnObject grdresult = accVendorPaymentDAO.getPaymentDetails(pRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();
                HashMap<String, Object> fieldrequestParams = new HashMap();
                
                
                HashMap<String, String> customFieldMap = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(re.getCompany().getCompanyID(), Constants.Acc_Make_Payment_ModuleId));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
            
                while (itr.hasNext()) {
                    PaymentDetail row = (PaymentDetail) itr.next();
                    
                    Date grCreationDate = null;
                    double grAmount = 0d;
                    if(row.getGoodsReceipt().isNormalInvoice()){
//                        grCreationDate = row.getGoodsReceipt().getJournalEntry().getEntryDate();
                        grAmount = isVendorPaymentEdit?row.getGoodsReceipt().getVendorEntry().getAmount():row.getAmount();
                    }else{// for opening balance inoices
                        grAmount = isVendorPaymentEdit?row.getGoodsReceipt().getOriginalOpeningBalanceAmount():row.getAmount();
                    }
                    grCreationDate = row.getGoodsReceipt().getCreationDate();
                    
                    JSONObject obj = new JSONObject();
                    obj.put("billid", isVendorPaymentEdit?row.getGoodsReceipt().getID():re.getID());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("transectionno", row.getGoodsReceipt().getGoodsReceiptNumber());
                    obj.put("transectionid", row.getGoodsReceipt().getID());
                    obj.put("amount",  grAmount);
                    obj.put("accountid",  (isVendorPaymentEdit?row.getGoodsReceipt().getAccount().getID():""));
                    if(row.getGoodsReceipt()!=null){
                        obj.put( "currencyidtransaction",row.getGoodsReceipt().getCurrency()==null?(row.getPayment().getCurrency()==null?currency.getCurrencyID():row.getPayment().getCurrency().getCurrencyID()):row.getGoodsReceipt().getCurrency().getCurrencyID());
                        obj.put("currencysymbol", row.getGoodsReceipt().getCurrency()==null?(row.getPayment().getCurrency()==null?currency.getSymbol():row.getPayment().getCurrency().getSymbol()):row.getGoodsReceipt().getCurrency().getSymbol());
                        obj.put("currencysymboltransaction", row.getGoodsReceipt().getCurrency()==null?(row.getPayment().getCurrency()==null?currency.getSymbol():row.getPayment().getCurrency().getSymbol()):row.getGoodsReceipt().getCurrency().getSymbol());
                    }else{
                        obj.put("currencyidtransaction", (row.getPayment().getCurrency()==null?currency.getCurrencyID():row.getPayment().getCurrency().getCurrencyID()));
                        obj.put("currencysymbol", (row.getPayment().getCurrency()==null?currency.getSymbol():row.getPayment().getCurrency().getSymbol()));
                        obj.put("currencysymboltransaction", (row.getPayment().getCurrency()==null?currency.getSymbol():row.getPayment().getCurrency().getSymbol()));
                    }
                    obj.put("duedate", df.format(row.getGoodsReceipt().getDueDate()));
                    obj.put("creationdate", df.format(grCreationDate));
                    JArr.put(obj);
                    if (row.getGoodsReceipt().getTax() != null) {
//                        KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getGoodsReceipt().getJournalEntry().getEntryDate(), row.getGoodsReceipt().getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getGoodsReceipt().getCreationDate(), row.getGoodsReceipt().getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("taxpercent", taxPercent);
                    obj.put("discount", row.getGoodsReceipt().getDiscount() == null ? 0 : row.getGoodsReceipt().getDiscount().getDiscountValue());
                    obj.put("payment", row.getGoodsReceipt().getID());
                    obj.put("gstCurrencyRate", row.getGstCurrencyRate());
                    double rowAmount=(authHandler.round(row.getAmount(), companyid));
                    if(isVendorPaymentEdit){
                        obj.put("amountpaid", rowAmount);
                        if(row.getFromCurrency()!=null&&row.getToCurrency()!=null){
                            obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());
                            obj.put("amountpaidincurrency", authHandler.round(rowAmount/row.getExchangeRateForTransaction(), companyid));
                        }else{
                            double amount = rowAmount;
                            String fromcurrencyid = (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID());
                            String tocurrencyid = (row.getGoodsReceipt().getCurrency()==null?(row.getPayment().getCurrency()==null?currency.getCurrencyID():row.getPayment().getCurrency().getCurrencyID()):row.getGoodsReceipt().getCurrency().getCurrencyID());
                            double exchangeRate= row.getGoodsReceipt().isIsOpeningBalenceInvoice()? row.getGoodsReceipt().getExternalCurrencyRate(): row.getGoodsReceipt().getJournalEntry().getExternalCurrencyRate();
//                            Date tranDate=row.getGoodsReceipt().isIsOpeningBalenceInvoice()?row.getGoodsReceipt().getCreationDate():row.getGoodsReceipt().getJournalEntry().getEntryDate();
                            Date tranDate=row.getGoodsReceipt().getCreationDate();
                            KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid, tranDate,exchangeRate);
                            amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            obj.put("exchangeratefortransaction", amount/rowAmount);
                            obj.put("amountpaidincurrency", amount);
                        }
                    }else{
                        if(row.getFromCurrency()!=null&&row.getToCurrency()!=null){
                            obj.put("amountpaid", rowAmount/row.getExchangeRateForTransaction());
                        }else{
                            double amount = rowAmount;
                            String fromcurrencyid = (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID());
                            String tocurrencyid = (row.getGoodsReceipt().getCurrency()==null?(row.getPayment().getCurrency()==null?currency.getCurrencyID():row.getPayment().getCurrency().getCurrencyID()):row.getGoodsReceipt().getCurrency().getCurrencyID());
                            double exchangeRate= row.getGoodsReceipt().isIsOpeningBalenceInvoice()? row.getGoodsReceipt().getExternalCurrencyRate(): row.getGoodsReceipt().getJournalEntry().getExternalCurrencyRate();
//                            Date tranDate=row.getGoodsReceipt().isIsOpeningBalenceInvoice()?row.getGoodsReceipt().getCreationDate():row.getGoodsReceipt().getJournalEntry().getEntryDate();
                            Date tranDate=row.getGoodsReceipt().getCreationDate();
                            KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid,tranDate,exchangeRate);
                            amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            obj.put("amountpaid", amount);
                        }
                    }
                    requestParams.put("isVendorPaymentEdit",isVendorPaymentEdit);
                    
                    
                    double amountdue=0.0, totalamount = 0.0,amountDueOriginal=0.0;
                    if(row.getGoodsReceipt().isNormalInvoice()){
                        List ll;
                        if(row.getGoodsReceipt().isIsExpenseType()){
                            ll= accGoodsReceiptCMN.getExpGRAmountDue(requestParams,row.getGoodsReceipt());
                            amountDueOriginal=(ll.isEmpty()?0:(Double)ll.get(4));
                        }
                        else{
                            if (Constants.InvoiceAmountDueFlag) {
                                ll = accGoodsReceiptCMN.getInvoiceDiscountAmountInfo(requestParams, row.getGoodsReceipt());
                                amountDueOriginal=(ll.isEmpty()?0:(Double)ll.get(5));
                            } else {
                                ll= accGoodsReceiptCMN.getGRAmountDue(requestParams,row.getGoodsReceipt());
                                amountDueOriginal=(ll.isEmpty()?0:(Double)ll.get(5));
                            }
                        }
                        amountdue=(ll.isEmpty()?0:(Double)ll.get(1));
                        totalamount = row.getGoodsReceipt().getVendorEntry().getAmount();
                    }else{
                        amountdue = row.getGoodsReceipt().getOpeningBalanceAmountDue()*(row.getExchangeRateForTransaction()==0?1:row.getExchangeRateForTransaction());
                        amountDueOriginal=row.getGoodsReceipt().getOpeningBalanceAmountDue();
                        totalamount = row.getGoodsReceipt().getOriginalOpeningBalanceAmount();
                    }
                     amountdue = authHandler.round(amountdue, companyid);
                     amountDueOriginal=authHandler.round(amountDueOriginal, companyid);
                     totalamount = authHandler.round(totalamount, companyid);
                    
                    if(row.getFromCurrency()!=null&&row.getToCurrency()!=null){
                            obj.put("amountduenonnegative", (isVendorPaymentEdit?(authHandler.round(amountDueOriginal*row.getExchangeRateForTransaction(), companyid)+rowAmount):amountDueOriginal));
                    }else{
                            obj.put("amountduenonnegative", (isVendorPaymentEdit?amountdue+obj.optDouble("amountpaid",0):amountdue));
                    }
                    obj.put("amountDueOriginal", (isVendorPaymentEdit?amountDueOriginal+obj.optDouble("amountpaidincurrency",0):amountDueOriginal));
                    obj.put("amountDueOriginalSaved", (isVendorPaymentEdit?amountDueOriginal+obj.optDouble("amountpaidincurrency",0):amountDueOriginal));
                    obj.put("totalamount", totalamount);

                    // ## Get Custom Field Data 
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                    ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
                    Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                    Detailfilter_params.add(row.getID());
                    invDetailRequestParams.put("filter_names", Detailfilter_names);
                    invDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresult = accVendorPaymentDAO.getVendorPaymentCustomData(invDetailRequestParams);
                    if (idcustresult.getEntityList().size() > 0) {
                        AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateFromDB=null;
                        for (Entry<String, Object> varEntry : variableMap.entrySet()) {
                            String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                            String valueForReport = "";
                            if (customFieldMap.containsKey(varEntry.getKey()) && isForReport && coldata != null) {
                                try {
                                    String[] valueData = coldata.split(",");
                                    for (String value : valueData) {
                                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                                        FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                        if (fieldComboData != null) {
                                            valueForReport += fieldComboData.getValue() + ",";
                                        }
                                    }
                                    if (valueForReport.length() > 1) {
                                        valueForReport = valueForReport.substring(0, valueForReport.length() - 1);
                                    }
                                    obj.put(varEntry.getKey(), valueForReport);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                                } catch (Exception ex) {
                                    obj.put(varEntry.getKey(), coldata);
                                }
                            } else if (customDateFieldMap.containsKey(varEntry.getKey()) && isForReport) {
                                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                                try {
                                    dateFromDB = defaultDateFormat.parse(coldata);
                                    coldata = df2.format(dateFromDB);

                                } catch (Exception e) {
                                }
                                obj.put(varEntry.getKey(), coldata);
                            } else {
                                obj.put(varEntry.getKey(), coldata != null ? coldata : "");
                            }
                        }
                    }
                    jArr.put(obj);
                }
                i++;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentController.getPaymentRowsJson : "+ex.getMessage(), ex);
        }
        return JArr;       
    }   
    
    public ModelAndView getPaymentsLinkedWithNCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject returnObj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            List ll = getPaymentsLinkedWithNCreditNote(request);

            JSONArray DataJArr = (JSONArray) ll.get(0);
            boolean isNoteLinkedWithPayment = (Boolean) ll.get(1);

            returnObj.put(Constants.RES_data, DataJArr);
            returnObj.put("isNoteLinkedWithPayment", isNoteLinkedWithPayment);
            issuccess = true;
            msg = messageSource.getMessage("acc.main.rec", null, RequestContextUtils.getLocale(request));   //"Records fetched successfully";
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                returnObj.put(Constants.RES_success, issuccess);
                returnObj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", returnObj.toString());
    }

    public List getPaymentsLinkedWithNCreditNote(HttpServletRequest request) {
        List returlList = new ArrayList();
        JSONArray returnArray = new JSONArray();
        try {
            String noteId = request.getParameter("noteId");
            boolean isNoteLinkedWithPayment = false;

            KwlReturnObject result = accVendorPaymentDAO.getPaymentIdLinkedWithNote(noteId);
            List list = result.getEntityList();
            Iterator it = list.iterator();
            while (it.hasNext()) {
                isNoteLinkedWithPayment = true;
                JSONObject jobj = new JSONObject();
                Payment payment = null;
                String paymentId = (String) it.next();
                if (!StringUtil.isNullOrEmpty(paymentId)) {
                    KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentId);
                    if (!paymentResult.getEntityList().isEmpty()) {
                        payment = (Payment) paymentResult.getEntityList().get(0);
                    }
                }
                if (payment != null) {
                    jobj.put("paymentNumber", payment.getPaymentNumber());
                    jobj.put("paymentJe", payment.getJournalEntry());
                }
                returnArray.put(jobj);
            }
            returlList.add(returnArray);
            returlList.add(isNoteLinkedWithPayment);

        } catch (JSONException ex) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returlList;
    }
    
   
  public void exportSingleMakePayment(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException, JSONException {
        try {
            JSONObject requestObj = StringUtil.convertRequestToJsonObject(request);
            HashMap<String, Object>otherconfigrequestParams = new HashMap();
            String SOID = requestObj.optString("bills");
            String fileName = requestObj.optString("filename");
            String companyid = requestObj.optString(Constants.companyKey);
            int moduleid = requestObj.optInt(Constants.moduleid, 0);
            JSONArray lineItemsArr=new JSONArray();
           
             KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), SOID);
             Payment payment = (Payment) objItr.getEntityList().get(0);
             
             String jID=payment.getJournalEntry().getID();
              AccCustomData  accCustomData = null;
            String recordids = requestObj.optString("recordids");
            ArrayList<String> SOIDList = CustomDesignHandler.getSelectedBillIDs(recordids);        
              KwlReturnObject custumObjresult = null;
             if (!StringUtil.isNullOrEmpty(jID)) {
                try {
                 custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jID);
                } catch (Exception e) {
                }
                accCustomData = (AccJECustomData) custumObjresult.getEntityList().get(0);
            }
            
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId,1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
            replaceFieldMap = new HashMap<String, String>();      
    
            /*
                * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
                */
            fieldrequestParams.clear();
            HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId, 1, 0));
            HashMap<String, Integer> DimensionFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

            fieldrequestParams.clear();
            HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId, 1, 1));
            HashMap<String, Integer> LineLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, customfieldFieldMap);
            
            HashMap<String, JSONArray>  itemDataMakePayment = new HashMap<String, JSONArray>();
            
            HashMap<String, Object> paramMap = new HashMap();
            paramMap.put(Constants.fieldMap, FieldMap);
            paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
            paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
            paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
            
            for(int count=0 ; count < SOIDList.size() ; count++ ){
                if (Constants.isNewPaymentStructure) {
                   lineItemsArr = accVendorPaymentServiceDAOobj.getMPDetailsItemJSONNew(requestObj, SOIDList.get(count), paramMap);
                } else {
                   lineItemsArr = accVendorPaymentServiceDAOobj.getMPDetailsItemJSON(requestObj, companyid, SOIDList.get(count), FieldMap, replaceFieldMap);//previous code
                }
                itemDataMakePayment.put(SOIDList.get(count), lineItemsArr); 
  
                // Below Function called to update print flag for MP Report
                accCommonTablesDAO.updatePrintFlag(moduleid, SOIDList.get(count), companyid);
            }
            
            otherconfigrequestParams.put(Constants.moduleid, moduleid);
            String invoicePostText="";//goodsReceiptOrder.getPostText()==null?"":goodsReceiptOrder.getPostText()
            ExportRecordHandler.exportSingleGeneric(request, response,itemDataMakePayment,accCustomData,customDesignDAOObj,accCommonTablesDAO,accAccountDAOobj, accountingHandlerDAOobj,velocityEngine,invoicePostText,otherconfigrequestParams,accInvoiceServiceDAOObj,accGoodsReceiptServiceDAOObj);
        } catch (JSONException ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void exportPettyCashVoucher(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        try {
            List list = accExportReportsServiceDAOobj.exportPaymentVoucher(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRPdfExporterParameter.JASPER_PRINT_LIST, list);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
 public void exportSenwanTecPaymentVoucher(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        try {
            List list = accExportReportsServiceDAOobj.exportSenwanTecPaymentVoucher(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRPdfExporterParameter.JASPER_PRINT_LIST, list);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }
 
    public void exportFerrateGroupPaymentVoucher(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        try {
            List list = accExportReportsServiceDAOobj.exportFerrateGroupPaymentVoucher(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRPdfExporterParameter.JASPER_PRINT_LIST, list);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void exportLSHPaymentVoucher(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        try {
            List list = accExportReportsServiceDAOobj.exportLSHPaymentVoucher(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRPdfExporterParameter.JASPER_PRINT_LIST, list);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public void exportTIDPaymentVoucher(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        try {
            List list = accExportReportsServiceDAOobj.exportTIDPaymentVoucher(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRPdfExporterParameter.JASPER_PRINT_LIST, list);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
     public void exportDefaultPaymentVoucher(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        try {
            List list = accExportOtherReportsServiceDAOobj.exportDefaultPaymentVoucher(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRPdfExporterParameter.JASPER_PRINT_LIST, list);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    } 
  
    public ModelAndView SatsPaymentVoucher(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {

        Map<String,Object> voucherMap=new HashMap<String, Object>();
//        String view = "satsPaymentVoucher";
        String mainReport = "/SATSPaymentVoucher.jrxml";
        Date currentDate=null;
        try{
            currentDate=authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(Calendar.getInstance().getTime()));
        }catch(ParseException pe){
            currentDate = Calendar.getInstance().getTime();
        }
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); 
        String currentDateStr=dateFormat.format(currentDate);
        String paymentId = request.getParameter("bills");
        int moduleid = Integer.parseInt(request.getParameter(Constants.moduleid));
        int mode = Integer.parseInt(request.getParameter("mode"));
        String companyid = AccountingManager.getCompanyidFromRequest(request);

        KwlReturnObject objItr = null;
        Payment payment = null;
        BillingPayment billingPayment = null;

               try{
        KwlReturnObject result = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
        CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) result.getEntityList().get(0);
        KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) cmpresult.getEntityList().get(0);
        java.util.Date entryDate = null;
        double total = 0;
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();

        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

        String currencyid = "";
            String receiptNumber = "";
         String paidTo = "";
         Date journalEntryDate = new Date();//mode!=StaticValues.AUTONUM_BILLINGRECEIPT?rc.getJournalEntry().getEntryDate():rc1.getJournalEntry().getEntryDate()
            PayDetail PayDetail = null;
         String memo = "";
         String AccountName = "";
         boolean ismanycrdb = false;
         int receiptType = 0;
         String bankCharges="";
         double bankChargesAmount=0.0;
         String vendorName="";
         String vendorCode="";
         String vendorAddress="";
         String vendorPhone="";
         String vendorFax="";
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();

         String netinword="";
         String cust = request.getParameter("customer");
         String accname = request.getParameter("accname");
         String address = request.getParameter("address");
         double amount = Double.parseDouble(request.getParameter("amount"));
         
         
         boolean iscontraentryflag = false;
         iscontraentryflag = Boolean.parseBoolean(request.getParameter("contraentryflag")); 
        DateFormat df = (DateFormat) requestParams.get("df");
//            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn,"customfield"));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid,0,0));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        JournalEntry entry=null;

         
        double advanceAmount=0;
        if(!StringUtil.isNullOrEmpty(request.getParameter("advanceAmount")))
        advanceAmount = Double.parseDouble(request.getParameter("advanceAmount"));
        boolean advanceFlag=false;
        if(!StringUtil.isNullOrEmpty(request.getParameter("advanceFlag")))
            advanceFlag = Boolean.parseBoolean(request.getParameter("advanceFlag"));
         List listPaymentDetails=null;
         DateFormat dFormat=new SimpleDateFormat("dd/MM/yyyy");
        if (mode==StaticValues.AUTONUM_PAYMENT) {
                objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentId);
                payment = (Payment) objItr.getEntityList().get(0);
            currencyid = (payment.getCurrency() == null) ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID();
                receiptNumber = payment.getPaymentNumber();
            paidTo = payment.getPaidTo()!=null?payment.getPaidTo().getValue():"";
            journalEntryDate = payment.getJournalEntry().getEntryDate();
                PayDetail = payment.getPayDetail();
            memo = payment.getMemo();
                Company com = payment.getCompany();
            ismanycrdb = payment.isIsmanydbcr();
            receiptType = payment.getReceipttype();
            bankChargesAmount=payment.getBankChargesAmount();
            entry=payment.getJournalEntry();
                filter_names.add("payment.ID");
                filter_params.add(payment.getID());
                rRequestParams.put("filter_names", filter_names);
                rRequestParams.put("filter_params", filter_params);
                KwlReturnObject pdoresult = accVendorPaymentDAO.getPaymentDetails(rRequestParams);
                listPaymentDetails = pdoresult.getEntityList();
           if(!listPaymentDetails.isEmpty()){
              PaymentDetail paymentDetail=(PaymentDetail)listPaymentDetails.get(0);
              Vendor vendor=paymentDetail.getGoodsReceipt().getVendor();
              if(vendor!=null){
                  vendorName=vendor.getName();
                  vendorCode=vendor.getAcccode();

                        //refer ticket ERP-10777
                  //vendorAddress=accVendorHandler.getVendorAddress(vendor,true);
                        //params to send to get billing address
                        HashMap<String, Object> addressParams = new HashMap<String, Object>();
                        addressParams.put("companyid", companyid);
                        addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                        addressParams.put("isBillingAddress", true);    //true to get billing address
                        addressParams.put("vendorid", vendor.getID());
                  vendorAddress=accountingHandlerDAOobj.getVendorAddress(addressParams);
                  
//                  vendorPhone=vendor.getContactNumber();
//                  vendorFax=vendor.getFax();
                    }
                }
            
            
            }
        
            SATSPaymentVoucher paymentVoucher=new SATSPaymentVoucher(); 
            ArrayList <SATSPaymentVoucher> paymentVoucherList=new ArrayList<SATSPaymentVoucher>();
//            paymentVoucher.setName(company.getCompanyName()!=null?company.getCompanyName():"");
    //         statementOfAccounts.setAddress(company.getAddress()!=null?company.getAddress():"");
//            paymentVoucher.setEmail(company.getEmailID()!=null?company.getEmailID():"");
//            paymentVoucher.setFax(company.getFaxNumber()!=null?company.getFaxNumber():"");
//            paymentVoucher.setPhone(company.getPhoneNumber()!=null?company.getPhoneNumber():"");
//            paymentVoucher.setGstRegNo(companyAccountPreferences.getGstNumber()!=null?companyAccountPreferences.getGstNumber():"");
//            paymentVoucher.setUem(companyAccountPreferences.getCompanyUEN()!=null?companyAccountPreferences.getCompanyUEN():"");
//            paymentVoucher.setDate(currentDateStr);
            paymentVoucher.setDocumentno(receiptNumber);
//            String paymentAccount=PayDetail!=null?PayDetail.getPaymentMethod().getMethodName():"";
            String paymentAccount="";
            if(PayDetail!=null&&PayDetail.getPaymentMethod()!=null&&PayDetail.getPaymentMethod().getAccount()!=null&&PayDetail.getPaymentMethod().getAccount().getGroup()!=null)        
                paymentAccount=PayDetail.getPaymentMethod().getAccount().getGroup().getName();
            if(!StringUtil.isNullOrEmpty(paymentAccount)){
                if(!paymentAccount.equals("Cash")){
                    Cheque cheque=PayDetail.getCheque();
                    if(PayDetail!=null&&cheque!=null){
                        String bankName=cheque.getBankName();
                        String checkNo=cheque.getChequeNo();
                        paymentVoucher.setType(PayDetail.getPaymentMethod().getMethodName()!=null?PayDetail.getPaymentMethod().getMethodName():"");
                        paymentVoucher.setChequeno(checkNo!=null?checkNo:"");
                        paymentVoucher.setBankname(bankName!=null?bankName:"");
            }
//                    paymentVoucher.setBankCharges("& Bank Charges");
//                    paymentVoucher.setBankChargesAmount(authHandler.formattedAmount(bankChargesAmount)+" as per details given below");
                }else{
                    paymentVoucher.setType("Cash in Hand");
                }
            }else{
                paymentVoucher.setType("Cash in Hand");
            }
          
               
//            paymentVoucher.setMemo(memo);
//            paymentVoucher.setPreparedBy("");
//            paymentVoucher.setAddress(company.getAddress());
            paymentVoucher.setName(vendorName!=null?vendorName:"");
//            paymentVoucher.setVendorCode(vendorCode!=null?vendorCode:"");
            paymentVoucher.setAddress(vendorAddress!=null?vendorAddress.replaceAll("\n", "<br>") :"");
//            paymentVoucher.setCustomerPhone(vendorPhone!=null?vendorPhone:"");
//            paymentVoucher.setCustomerFax(vendorFax!=null?vendorFax:"");
//            paymentVoucher.setCustomerOrVendor("Vendor");
//            paymentVoucher.setPaidToName("Paid To");
//            paymentVoucher.setPaidTo(paidTo);
            getSatsPaymentVoucherTableData(request,voucherMap,paymentVoucher);
            paymentVoucherList.add(paymentVoucher);
            voucherMap.put("format", "pdf");
            String logo = StorageHandler.GetDocStorePath() + companyid + "_template" + FileUploadHandler.getCompanyImageExt();
            voucherMap.put("logo", logo);
            voucherMap.put("datasource", new JRBeanCollectionDataSource(paymentVoucherList));

            InputStream inputStream = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + mainReport);
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(paymentVoucherList);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, voucherMap, beanColDataSource);
            response.setHeader("Content-Disposition", "attachment;filename=" + "TrailBalance_v1.pdf");

            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
         }
    
        catch(Exception ex) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("",voucherMap);
    }
    
    public void getSatsPaymentVoucherTableData(HttpServletRequest request, Map<String, Object> voucherMap, SATSPaymentVoucher paymentVoucher) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        ArrayList<SATSPaymentVoucherTable> satsPaymentVoucherTableList = new ArrayList<SATSPaymentVoucherTable>();
        List<String> billsArr = new ArrayList<String>();
        String companyid = sessionHandlerImpl.getCompanyid(request);
        Payment payment = null;
        String paymentId = request.getParameter("bills");
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        KwlReturnObject objItr = null;
        HashMap<String, Object> rRequestParams = new HashMap<String, Object>();

        double advanceAmount = 0;
        if (!StringUtil.isNullOrEmpty(request.getParameter("advanceAmount"))) {
            advanceAmount = Double.parseDouble(request.getParameter("advanceAmount"));
        }
        boolean advanceFlag = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("advanceFlag"))) {
            advanceFlag = Boolean.parseBoolean(request.getParameter("advanceFlag"));
        }
        int mode = Integer.parseInt(request.getParameter("mode"));
        DateFormat dFormat = new SimpleDateFormat("dd/MM/yyyy");
        if (mode == StaticValues.AUTONUM_PAYMENT) {
            objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentId);
            payment = (Payment) objItr.getEntityList().get(0);
            billsArr.add(paymentId);
            if (advanceFlag) {
                billsArr.add(payment.getAdvanceid().getID());
            }
            double totalAmount = 0.0;
            for (String billId : billsArr) {
                filter_names = new ArrayList();
                filter_params = new ArrayList();
                filter_names.add("payment.ID");
                filter_params.add(billId);
                rRequestParams.put("filter_names", filter_names);
                rRequestParams.put("filter_params", filter_params);
                KwlReturnObject pdoresult = accVendorPaymentDAO.getPaymentDetails(rRequestParams);
                List<PaymentDetail> list1 = pdoresult.getEntityList();
                Iterator pdoRow = list1.iterator();
                if (pdoRow != null && list1.size() > 0) {
                    for (PaymentDetail pdo : list1) {
                        if (billId.equals(pdo.getPayment().getID())) {
                            GoodsReceipt goodsReceipt = pdo.getGoodsReceipt();
                            SATSPaymentVoucherTable paymentVoucherSubReport = new SATSPaymentVoucherTable();
                            paymentVoucherSubReport.setInvoiceno(goodsReceipt.getGoodsReceiptNumber());
                            paymentVoucherSubReport.setDate(goodsReceipt.getCreationDate() != null ? dFormat.format(goodsReceipt.getCreationDate()) : "");
                            paymentVoucherSubReport.setAmount(goodsReceipt.getCurrency().getSymbol() + " " + authHandler.formattedAmount(pdo.getAmount(), companyid) + "");
                            totalAmount += pdo.getAmount();
                            paymentVoucherSubReport.setTotalamount(goodsReceipt.getCurrency().getSymbol() + " " + authHandler.formattedAmount(totalAmount, companyid) + "");
                            satsPaymentVoucherTableList.add(paymentVoucherSubReport);

                        }
                    }
                } else if (advanceFlag && billsArr.get(1) != null && billsArr.get(1).equals(billId)) {
                    SATSPaymentVoucherTable paymentVoucherTable = new SATSPaymentVoucherTable();
                    paymentVoucherTable.setInvoiceno("Advance Amount");
                    paymentVoucherTable.setAmount(payment.getCurrency().getSymbol() + " " + authHandler.formattedAmount(advanceAmount, companyid));
                    paymentVoucherTable.setDate("_");
                    totalAmount += advanceAmount;
                    paymentVoucherTable.setTotalamount(payment.getCurrency().getSymbol() + " " + authHandler.formattedAmount(totalAmount, companyid) + "");
                    satsPaymentVoucherTableList.add(paymentVoucherTable);
                }
            }

        }
        voucherMap.put("SatsPaymentVoucherTable", new JRBeanCollectionDataSource(satsPaymentVoucherTableList));
    }

    public ModelAndView getAdvanceVendorPaymentForRefunds(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        List<AdvanceDetail> advanceDetailList = new ArrayList<AdvanceDetail>();
        boolean issuccess = false;
        String msg = "";
        String companyid = "";
        try {
            companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat();
            HashMap<String, Object> requestParams = getPaymentMap(request);
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            String accid = request.getParameter("accid") != null ? request.getParameter("accid") : "";
            requestParams.put("accid", accid);
            String currencyFilterForTrans="";
            KWLCurrency currencyFilter = null;
            currencyFilterForTrans = (request.getParameter("currencyfilterfortrans") == null) ? "" : request.getParameter("currencyfilterfortrans");
            if (!StringUtil.isNullOrEmpty(currencyFilterForTrans)) {
                KwlReturnObject currencyFilterResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                currencyFilter = (KWLCurrency) currencyFilterResult.getEntityList().get(0);
            }

            if(!StringUtil.isNullOrEmpty(request.getParameter("isReceipt"))){
                requestParams.put("isReceipt", true);     // based on 'isReceipt' present or not, condition to fetch only payment currency records will get append
            }
            if (!StringUtil.isNullOrEmpty(currencyFilterForTrans)) {
                requestParams.put("currencyfilterfortrans", currencyFilterForTrans); // currencyid to fetch only records with this currencyid
            }
            requestParams.put(Constants.start, start);
            requestParams.put(Constants.limit, limit);
            // isRefundLinking flag is used while linking advance payment to refund
            boolean isRefundLinking = (!StringUtil.isNullOrEmpty(request.getParameter("isRefundLinking"))) ? Boolean.parseBoolean(request.getParameter("isRefundLinking")) : false;
            KwlReturnObject result = null;
            result = accVendorPaymentDAO.getPaymentAdvanceAmountDueDetails(requestParams);
            advanceDetailList = result.getEntityList();
            for (AdvanceDetail advanceDetail : advanceDetailList) {
                JSONObject obj = new JSONObject();
                Payment pm = advanceDetail.getPayment();
                String currencyid = pm.getCurrency().getCurrencyID();
                Vendor vendor = pm.getVendor();
                if (vendor != null) {
                    obj.put("accountid", vendor.getID());
                    obj.put("accountnames", vendor.getName());
                    obj.put("accname",  vendor.getAccount()==null?"":vendor.getAccount().getName());
                    obj.put("acccode",  vendor.getAccount()==null?"":vendor.getAccount().getAcccode());
                } 
                
                obj.put("billno", pm.getPaymentNumber());
                obj.put("billid", advanceDetail.getId());
                obj.put("date", df.format(pm.getCreationDate()));
                obj.put("documentno", pm.getPaymentNumber());
                
                double amountdue = advanceDetail.getAmountDue();
                double amountDueOriginal = advanceDetail.getAmountDue();
                double externalCurrencyRate = pm.getExternalCurrencyRate();
                obj.put("totalamount", advanceDetail.getAmount());
                obj.put("amountDueOriginal", advanceDetail.getAmountDue());
                obj.put("amountDueOriginalSaved", advanceDetail.getAmountDue());
                obj.put("amount", advanceDetail.getAmount());
                obj.put("currencyid", currencyid);
                obj.put("currencysymbol", pm.getCurrency().getSymbol());
                obj.put("currencyidtransaction", currencyid);
                obj.put("currencysymboltransaction", pm.getCurrency().getSymbol());
                if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyFilterForTrans, pm.getCreationDate(), externalCurrencyRate);
                    amountdue = (Double) bAmt.getEntityList().get(0);
                    obj.put("currencyidpayment", currencyFilterForTrans);
                    obj.put("currencysymbolpayment", (currencyFilter == null ? pm.getCurrency().getSymbol() : currencyFilter.getSymbol()));
                }
                obj.put("amountdue", authHandler.round(amountdue, companyid));
                obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                // add keys needed for refund linking
                if (isRefundLinking) {
                    obj.put("documentid", advanceDetail.getId());
                    obj.put("documentType", Constants.AdvancePayment);
                    obj.put("type", "Advance Payment");
                    obj.put("externalcurrencyrate", pm.getExternalCurrencyRate());
                    obj.put("currencyidpayment", currencyFilterForTrans);
                    obj.put("currencysymbolpayment", (currencyFilter == null ? pm.getCurrency().getSymbol() : currencyFilter.getSymbol()));
                }

                //Get Custom Field Data 
                accVendorPaymentServiceDAOobj.getAdvancePaymentCustomData(requestParams, advanceDetail, obj);
                jArr.put(obj);
            }
            jobj.put(Constants.RES_data, jArr);
            jobj.put(Constants.RES_count, advanceDetailList.size());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getLinkedInvoicesAgainstAdvance(HttpServletRequest request , HttpServletResponse response){
        JSONObject jObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean isSuccess= false;
        String msg="";
        try{
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("dateformat", authHandler.getDateOnlyFormat());
            if (!StringUtil.isNullOrEmpty(request.getParameter("bills"))) {
                requestParams.put("bills", request.getParameterValues("bills"));
            }

            jArr = getLinkedInvoicesAgainstAdvanceJSON(requestParams);
            isSuccess =true;
            jObj.put(Constants.RES_data, jArr);
            
        } catch(SessionExpiredException e){
            msg = e.getMessage();
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception e){
            msg = e.getMessage();
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                jObj.put(Constants.RES_success, isSuccess);
                jObj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
}
        }
        return new ModelAndView("jsonView","model",jObj.toString());
    }
    
    public JSONArray getLinkedInvoicesAgainstAdvanceJSON(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray jArr = new JSONArray();
        String currencyId = (String) requestParams.get(Constants.globalCurrencyKey);
        String companyid = (String) requestParams.get("companyid");
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyId);
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        String[] paymentid = null;
        DateFormat df = (DateFormat) requestParams.get("dateformat");
        try {
            if (requestParams.containsKey("bills")) {
                paymentid = (String[]) requestParams.get("bills");
            }
            for (int index = 0; index < paymentid.length; index++) {
                KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid[index]);
                Payment payment = (Payment) paymentResult.getEntityList().get(0);
                Set<LinkDetailPayment> linkDetailPayment = payment.getLinkDetailPayments();
                for (LinkDetailPayment LDP : linkDetailPayment) {
                    JSONObject jObj = new JSONObject();
                    Date grCreationDate = null;
                    grCreationDate = LDP.getGoodsReceipt().getCreationDate();
                    double amountdueInInvoiceCurrency = 0.0;
                    if (LDP.getGoodsReceipt().isNormalInvoice()) {
                        List ll;
                        if (LDP.getGoodsReceipt().isIsExpenseType()) {
                            ll = accGoodsReceiptCMN.getExpGRAmountDue(requestParams, LDP.getGoodsReceipt());
                            amountdueInInvoiceCurrency = (ll.isEmpty() ? 0 : (Double) ll.get(4));
                        } else {
                            if (Constants.InvoiceAmountDueFlag) {
                                ll = accGoodsReceiptCMN.getInvoiceDiscountAmountInfo(requestParams, LDP.getGoodsReceipt());
                                amountdueInInvoiceCurrency = (ll.isEmpty() ? 0 : (Double) ll.get(5));
                            } else {
                                ll = accGoodsReceiptCMN.getGRAmountDue(requestParams, LDP.getGoodsReceipt());
                                amountdueInInvoiceCurrency = (ll.isEmpty() ? 0 : (Double) ll.get(5));
                            }
                        }
                    } else {
                        amountdueInInvoiceCurrency = LDP.getGoodsReceipt().getOpeningBalanceAmountDue() * (LDP.getExchangeRateForTransaction() == 0 ? 1 : LDP.getExchangeRateForTransaction());
                    }

                    amountdueInInvoiceCurrency += LDP.getAmountInGrCurrency();
                    amountdueInInvoiceCurrency = authHandler.round(amountdueInInvoiceCurrency, companyid);
                    jObj.put("billid", LDP.getGoodsReceipt().getID());
                    jObj.put("linkdetailid", LDP.getID());
                    jObj.put("billno", LDP.getGoodsReceipt().getGoodsReceiptNumber());
                    jObj.put("transectionno", LDP.getGoodsReceipt().getGoodsReceiptNumber());
                    jObj.put("invoicedate", df.format(grCreationDate));
                    jObj.put("amountDueOriginal", amountdueInInvoiceCurrency);
                    jObj.put("exchangeratefortransaction", LDP.getExchangeRateForTransaction());
                    double amountdueInPaymentCurrency = authHandler.round(amountdueInInvoiceCurrency * LDP.getExchangeRateForTransaction(), companyid);
                    jObj.put("amountdue", amountdueInPaymentCurrency);
                    jObj.put("invamount", LDP.getAmount());
                    jObj.put("currencysymbol", LDP.getGoodsReceipt().getCurrency() == null ? (LDP.getPayment().getCurrency() == null ? currency.getSymbol() : LDP.getPayment().getCurrency().getSymbol()) : LDP.getGoodsReceipt().getCurrency().getSymbol());
                    jObj.put("currencysymbolpayment", LDP.getPayment().getCurrency().getSymbol());
                    jArr.put(jObj);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return jArr;
    }
    
    public ModelAndView getRepeatePaymentDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String parentPaymentId[]=null;
            KwlReturnObject details=null;
            JSONArray JArr = new JSONArray();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            if (request.getParameter("bills") != null) {
                parentPaymentId = (String[]) request.getParameter("bills").split(",");
            }else{
                parentPaymentId = (String[]) request.getParameter("parentPaymentId").split(",");
            }
            for (int i = 0; i < parentPaymentId.length; i++) {

                requestParams.put("parentPaymentId", parentPaymentId[i]);
                details = accVendorPaymentDAO.getRepeatePaymentDetails(requestParams);
                List detailsList = details.getEntityList();
                Iterator itr = detailsList.iterator();

                while (itr.hasNext()) {
                    Payment repeatedPayment = (Payment) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("PaymentId", repeatedPayment.getID());
                    obj.put("paymentNo", repeatedPayment.getPaymentNumber());
                    /*
                     * If Repeated payment is not generate for record then set
                     * isPaymentGenarated as true
                     */
                    obj.put("parentPaymentId", parentPaymentId[i]);
                    obj.put("isPaymentGenarated", true);
                    JArr.put(obj);
                }
                /*
                 * If Repeated payment is not generate for record then set
                 * isPaymentGenarated as false
                 */
                if(detailsList.isEmpty()){
                    JSONObject obj = new JSONObject();
                    obj.put("parentPaymentId", parentPaymentId[i]);
                    obj.put("isPaymentGenarated", false);
                    JArr.put(obj);
                }
                    
            }
            jobj.put(Constants.RES_data, JArr);
            jobj.put(Constants.RES_count, details.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
        public ModelAndView getCNLinkedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getCNLinkedInTransaction(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "ACCVendorPaymentCMN.getCNLinkedInTransaction:" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "ACCVendorPaymentCMN.getCNLinkedInTransaction:" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getCNLinkedInTransaction(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String noteId = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat();
            String cnType= request.getParameter("cntype");

            if (!StringUtil.isNullOrEmpty(noteId)) {
                KwlReturnObject cnresult;
            if(!cnType.equalsIgnoreCase("5")){          //malaysian Demo ERP-27284 / ERP-28249
                cnresult = accVendorPaymentDAO.getPaymentIdLinkedWithNote(noteId);;
                List listc = cnresult.getEntityList();
                Iterator itr1 = listc.iterator();
                while (itr1.hasNext()) {
                    String orderid = (String) itr1.next();

                    JSONObject obj = new JSONObject();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), orderid);
                    Payment payment = (Payment) objItr.getEntityList().get(0);
                    String jeNumbers = (payment.isIsOpeningBalencePayment()) ? "" : payment.getJournalEntry().getEntryNumber();
                    if (payment.getJournalEntryForBankCharges() != null) {
                        jeNumbers += "<br>" + payment.getJournalEntryForBankCharges().getEntryNumber();
                    }
                    if (payment.getJournalEntryForBankInterest() != null) {
                        jeNumbers += "<br>" + payment.getJournalEntryForBankInterest().getEntryNumber();
                    }
                    obj.put("billid", payment.getID());
                    obj.put("deleted", payment.isDeleted());
                    obj.put("companyid", payment.getCompany().getCompanyID());
                    obj.put("companyname", payment.getCompany().getCompanyName());
                    obj.put("withoutinventory", "");
                    obj.put("transactionNo", payment.getPaymentNumber());   //Payment no
                    obj.put("date", df.format(payment.getCreationDate()));  //date of delivery order
                    obj.put("linkingdate", df.format(payment.getCreationDate()));  //date of delivery order
                    obj.put("journalEntryNo", jeNumbers);  //journal entry no
                    obj.put("mergedCategoryData", "Payment Voucher");  //type of data

                    if (payment.getCustomer() != null) {
                        String customerid = payment.getCustomer();
                        KwlReturnObject objItr2 = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerid);
                        Customer customer = (Customer) objItr2.getEntityList().get(0);
                        obj.put("personname", customer.getName());
                        obj.put("personid", customer.getID());
                    } else {
                        Vendor vid = payment.getVendor();
                        obj.put("personname", vid.getName());
                        obj.put("personid", vid.getID());
                    }
                    Vendor vendor = payment.getVendor();
                    if (vendor != null) {
                        obj.put("address", vendor.getAddress());
                        obj.put("personemail", vendor.getEmail());
                    } else {
                        obj.put("address", "");
                        obj.put("personemail", "");
                    }
                    obj.put("ischequeprinted", payment.isChequeprinted());
                    obj.put("billid", payment.getID());
                    obj.put("companyid", payment.getCompany().getCompanyID());
                    obj.put("companyname", payment.getCompany().getCompanyName());
                    String jeNumber = payment.getJournalEntry().getEntryNumber();
                    if (payment.getJournalEntryForBankCharges() != null) {
                        jeNumber += "," + payment.getJournalEntryForBankCharges().getEntryNumber();
                    }
                    if (payment.getJournalEntryForBankInterest() != null) {
                        jeNumber += "," + payment.getJournalEntryForBankInterest().getEntryNumber();
                    }

                    if (payment.getLinkDetailPayments() != null && !payment.getLinkDetailPayments().isEmpty()) {
                        Set<LinkDetailPayment> linkedDetailPayList = payment.getLinkDetailPayments();
                        for (LinkDetailPayment ldprow : linkedDetailPayList) {
                            if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                                KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                                JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                                jeNumber += "," + linkedJEObject.getEntryNumber();
                            }
                        }
                    }
                    obj.put("entryno", jeNumber);
                    obj.put("journalentryid", payment.getJournalEntry().getID());
                    obj.put("isadvancepayment", payment.isIsadvancepayment());
                    obj.put("ismanydbcr", payment.isIsmanydbcr());
                    obj.put("isprinted", payment.isPrinted());
                    obj.put("bankCharges", payment.getBankChargesAmount());
                    obj.put("bankChargesCmb", payment.getBankChargesAccount() != null ? payment.getBankChargesAccount().getID() : "");
                    obj.put("bankInterest", payment.getBankInterestAmount());
                    obj.put("bankInterestCmb", payment.getBankInterestAccount() != null ? payment.getBankInterestAccount().getID() : "");
                    obj.put("paidToCmb", payment.getPaidTo() == null ? "" : payment.getPaidTo().getID());
                    obj.put("paidto", payment.getPaidTo() != null ? payment.getPaidTo().getValue() : "");  //to show the paid to option in grid
                    obj.put(Constants.SEQUENCEFORMATID, payment.getSeqformat() == null ? "" : payment.getSeqformat().getID());
                    boolean advanceUsed = false;
                    obj.put("invoiceadvcndntype", payment.getInvoiceAdvCndnType());
                    obj.put("cndnAndInvoiceId", !StringUtil.isNullOrEmpty(payment.getCndnAndInvoiceId()) ? payment.getCndnAndInvoiceId() : "");
                    obj.put("advanceUsed", advanceUsed);
                    obj.put("advanceid", (payment.getAdvanceid() != null && !payment.getAdvanceid().isDeleted()) ? payment.getAdvanceid().getID() : "");
                    obj.put("advanceamount", payment.getAdvanceamount());
                    obj.put("receipttype", payment.getReceipttype());
                    obj.put("billno", payment.getPaymentNumber());
                    obj.put("billdate", df.format(payment.getCreationDate()));
                    jArr.put(obj);

                }
            }else{
                    cnresult = accReceiptDAOobj.getPaymentIdLinkedWithNote(noteId);     //To display linking information for malaysian country ERP-27284 / ERP-28249
                    List listc = cnresult.getEntityList();
                    Iterator itr1 = listc.iterator();
                    while (itr1.hasNext()) {
                        String orderid = (String) itr1.next();

                        JSONObject obj = new JSONObject();
                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), orderid);
                        Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                        String jeNumbers = (receipt.isIsOpeningBalenceReceipt()) ? "" : receipt.getJournalEntry().getEntryNumber();
                        if (receipt.getJournalEntryForBankCharges() != null) {
                            jeNumbers += "<br>" + receipt.getJournalEntryForBankCharges().getEntryNumber();
                        }
                        if (receipt.getJournalEntryForBankInterest() != null) {
                            jeNumbers += "<br>" + receipt.getJournalEntryForBankInterest().getEntryNumber();
                        }
                        obj.put("billid", receipt.getID());
                        obj.put("deleted", receipt.isDeleted());
                        obj.put("companyid", receipt.getCompany().getCompanyID());
                        obj.put("companyname", receipt.getCompany().getCompanyName());
                        obj.put("withoutinventory", "");
                        obj.put("transactionNo", receipt.getReceiptNumber());   //receipt no
                        obj.put("date", df.format(receipt.getCreationDate()));  //date of delivery order
                        obj.put("linkingdate", df.format(receipt.getCreationDate()));  //date of delivery order
                        obj.put("journalEntryNo", jeNumbers);  //journal entry no
                        obj.put("mergedCategoryData", "Receipt Voucher");  //type of data

                        if (receipt.getCustomer() != null) {
                            Customer customer = receipt.getCustomer();
                            obj.put("personname", customer.getName());
                            obj.put("personid", customer.getID());
                        } else {
                            KwlReturnObject venresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                            Vendor vid = (Vendor) venresult.getEntityList().get(0);
                            obj.put("personname", vid.getName());
                            obj.put("personid", vid.getID());
                        }
                        KwlReturnObject venresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor()!=null?receipt.getVendor():"");
                        Vendor vendor = (Vendor) venresult.getEntityList().get(0);
                        if (vendor != null) {
                            obj.put("address", vendor.getAddress());
                            obj.put("personemail", vendor.getEmail());
                        } else {
                            obj.put("address", "");
                            obj.put("personemail", "");
                        }
                        obj.put("ischequeprinted", receipt.isPrinted());
                        obj.put("billid", receipt.getID());
                        obj.put("companyid", receipt.getCompany().getCompanyID());
                        obj.put("companyname", receipt.getCompany().getCompanyName());
                        String jeNumber = receipt.getJournalEntry().getEntryNumber();
                        if (receipt.getJournalEntryForBankCharges() != null) {
                            jeNumber += "," + receipt.getJournalEntryForBankCharges().getEntryNumber();
                        }
                        if (receipt.getJournalEntryForBankInterest() != null) {
                            jeNumber += "," + receipt.getJournalEntryForBankInterest().getEntryNumber();
                        }

                        if (receipt.getLinkDetailReceipts() != null && !receipt.getLinkDetailReceipts().isEmpty()) {
                            Set<LinkDetailReceipt> linkedDetailRecList = receipt.getLinkDetailReceipts();
                            for (LinkDetailReceipt ldprow : linkedDetailRecList) {
                                if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                                    KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                                    JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                                    jeNumber += "," + linkedJEObject.getEntryNumber();
                                }
                            }
                        }
                        obj.put("entryno", jeNumber);
                        obj.put("journalentryid", receipt.getJournalEntry().getID());
                        obj.put("isadvancepayment", receipt.isIsadvancepayment());
                        obj.put("ismanydbcr", receipt.isIsmanydbcr());
                        obj.put("isprinted", receipt.isPrinted());
                        obj.put("bankCharges", receipt.getBankChargesAmount());
                        obj.put("bankChargesCmb", receipt.getBankChargesAccount() != null ? receipt.getBankChargesAccount().getID() : "");
                        obj.put("bankInterest", receipt.getBankInterestAmount());
                        obj.put("bankInterestCmb", receipt.getBankInterestAccount() != null ? receipt.getBankInterestAccount().getID() : "");
                        obj.put("paidToCmb", receipt.getReceivedFrom() == null ? "" : receipt.getReceivedFrom().getID());
                        obj.put("paidto", receipt.getReceivedFrom() != null ? receipt.getReceivedFrom().getValue() : "");  //to show the paid to option in grid
                        obj.put(Constants.SEQUENCEFORMATID, receipt.getSeqformat() == null ? "" : receipt.getSeqformat().getID());
                        boolean advanceUsed = false;
                        obj.put("invoiceadvcndntype", receipt.getInvoiceAdvCndnType());
                        obj.put("cndnAndInvoiceId", !StringUtil.isNullOrEmpty(receipt.getCndnAndInvoiceId()) ? receipt.getCndnAndInvoiceId() : "");
                        obj.put("advanceUsed", advanceUsed);
                        obj.put("advanceid", (receipt.getAdvanceid() != null && !receipt.getAdvanceid().isDeleted()) ? receipt.getAdvanceid().getID() : "");
                        obj.put("advanceamount", receipt.getAdvanceamount());
                        obj.put("receipttype", receipt.getReceipttype());
                        obj.put("billno", receipt.getReceiptNumber());
                        obj.put("billdate", df.format(receipt.getCreationDate()));
                        jArr.put(obj);

                    }
                }
                cnresult = accVendorPaymentDAO.getAdvancePaymentIdLinkedWithNote(noteId);
                List listAdvance = cnresult.getEntityList();
                 for (int index = 0; index < listAdvance.size(); index++) {
                    Object[] linkedMPObj = (Object[]) listAdvance.get(index);
                    String orderid = (String) linkedMPObj[0];
                    Date linkdate = (Date)linkedMPObj[1];

                    JSONObject obj = new JSONObject();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), orderid);
                    Payment payment = (Payment) objItr.getEntityList().get(0);
                    String jeNumbers = (payment.isIsOpeningBalencePayment()) ? "" : payment.getJournalEntry().getEntryNumber();
                    if (payment.getJournalEntryForBankCharges() != null) {
                        jeNumbers += "<br>" + payment.getJournalEntryForBankCharges().getEntryNumber();
                    }
                    if (payment.getJournalEntryForBankInterest() != null) {
                        jeNumbers += "<br>" + payment.getJournalEntryForBankInterest().getEntryNumber();
                    }
                    obj.put("billid", payment.getID());
                    obj.put("companyid", payment.getCompany().getCompanyID());
                    obj.put("companyname", payment.getCompany().getCompanyName());
                    obj.put("withoutinventory", "");
                    obj.put("transactionNo", payment.getPaymentNumber());   //Payment no
                    obj.put("date", df.format(payment.getCreationDate()));  //date of delivery order
                    obj.put("linkingdate", df.format(linkdate)); 
                    obj.put("journalEntryNo", jeNumbers);  //journal entry no
                    obj.put("mergedCategoryData", "Payment Voucher");  //type of data

                    if (payment.getCustomer() != null) {
                        String customerid = payment.getCustomer();
                        KwlReturnObject objItr2 = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerid);
                        Customer customer = (Customer) objItr2.getEntityList().get(0);
                        obj.put("personname", customer.getName());
                        obj.put("personid", customer.getID());
                    } else {
                        Vendor vid = payment.getVendor();
                        obj.put("personname", vid.getName());
                        obj.put("personid", vid.getID());
                    }
                    Vendor vendor = payment.getVendor();
                    if (vendor != null) {
                        obj.put("address", vendor.getAddress());
                        obj.put("personemail", vendor.getEmail());
                    } else {
                        obj.put("address", "");
                        obj.put("personemail", "");
                    }
                    obj.put("ischequeprinted", payment.isChequeprinted());
                    obj.put("billid", payment.getID());
                    obj.put("companyid", payment.getCompany().getCompanyID());
                    obj.put("companyname", payment.getCompany().getCompanyName());
                    String jeNumber = payment.getJournalEntry().getEntryNumber();
                    if (payment.getJournalEntryForBankCharges() != null) {
                        jeNumber += "," + payment.getJournalEntryForBankCharges().getEntryNumber();
                    }
                    if (payment.getJournalEntryForBankInterest() != null) {
                        jeNumber += "," + payment.getJournalEntryForBankInterest().getEntryNumber();
                    }

                    if (payment.getLinkDetailPayments() != null && !payment.getLinkDetailPayments().isEmpty()) {
                        Set<LinkDetailPayment> linkedDetailPayList = payment.getLinkDetailPayments();
                        for (LinkDetailPayment ldprow : linkedDetailPayList) {
                            if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                                KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                                JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                                jeNumber += "," + linkedJEObject.getEntryNumber();
                            }
                        }
                    }
                    obj.put("entryno", jeNumber);
                    obj.put("journalentryid", payment.getJournalEntry().getID());
                    obj.put("isadvancepayment", payment.isIsadvancepayment());
                    obj.put("ismanydbcr", payment.isIsmanydbcr());
                    obj.put("isprinted", payment.isPrinted());
                    obj.put("bankCharges", payment.getBankChargesAmount());
                    obj.put("bankChargesCmb", payment.getBankChargesAccount() != null ? payment.getBankChargesAccount().getID() : "");
                    obj.put("bankInterest", payment.getBankInterestAmount());
                    obj.put("bankInterestCmb", payment.getBankInterestAccount() != null ? payment.getBankInterestAccount().getID() : "");
                    obj.put("paidToCmb", payment.getPaidTo() == null ? "" : payment.getPaidTo().getID());
                    obj.put("paidto", payment.getPaidTo() != null ? payment.getPaidTo().getValue() : "");  //to show the paid to option in grid
                    obj.put(Constants.SEQUENCEFORMATID, payment.getSeqformat() == null ? "" : payment.getSeqformat().getID());
                    boolean advanceUsed = false;
                    obj.put("invoiceadvcndntype", payment.getInvoiceAdvCndnType());
                    obj.put("cndnAndInvoiceId", !StringUtil.isNullOrEmpty(payment.getCndnAndInvoiceId()) ? payment.getCndnAndInvoiceId() : "");
                    obj.put("advanceUsed", advanceUsed);
                    obj.put("advanceid", (payment.getAdvanceid() != null && !payment.getAdvanceid().isDeleted()) ? payment.getAdvanceid().getID() : "");
                    obj.put("advanceamount", payment.getAdvanceamount());
                    obj.put("receipttype", payment.getReceipttype());
                    obj.put("billno", payment.getPaymentNumber());
                    obj.put("billdate", df.format(payment.getCreationDate()));
                    jArr.put(obj);

                }
                 KwlReturnObject siresult =null;
                 if (cnType.equals("5")) {
                    siresult=accInvoiceDAOobj.getSalesInvoiceLinkedWithDebitNote(noteId, companyid);
                } else if (cnType.equals("" + Constants.CreditNoteForOvercharge)) {
                    siresult=accInvoiceDAOobj.getInvoiceLinkedWithOverchargeCreditNote(noteId, companyid);
                } else {
                    siresult=accInvoiceDAOobj.getSalesInvoiceLinkedWithNote(noteId, companyid);
                }
                List listsi = siresult.getEntityList();
                for (Object listsi1 : listsi) {
                    Object[] linkedSIObj = (Object[]) listsi1;
                    String orderinvid = (String) linkedSIObj[0];
                    JSONObject invobj = new JSONObject();
                    KwlReturnObject invobjItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), orderinvid);
                    Invoice invoice = (Invoice) invobjItr.getEntityList().get(0);
                    Customer customer = invoice.getCustomer();
                    invobj.put("billid", invoice.getID());
                    invobj.put("companyid", invoice.getCompany().getCompanyID());
                    invobj.put("withoutinventory", "");
                    invobj.put("customername", customer.getName());
                    invobj.put("transactionNo", invoice.getInvoiceNumber());   //delivery order no
                    invobj.put("duedate", invoice.getDueDate() != null ? df.format(invoice.getDueDate()) : "");
                    if(cnType.equals("" + Constants.CreditNoteForOvercharge)){
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("ID", noteId);
                        Date linkdate = (Date) kwlCommonTablesDAOObj.getRequestedObjectFields(CreditNote.class, new String[]{"creationDate"}, paramsMap);
                        invobj.put("linkingdate", df.format(linkdate));
                    } else {
                        Date linkdate = (Date)linkedSIObj[1];
                        invobj.put("linkingdate", df.format(linkdate)); 
                    }
                    if (invoice.getJournalEntry() != null) {
                        invobj.put("date", df.format(invoice.getCreationDate()));  //date of invoice
                        invobj.put("journalEntryId", invoice.getJournalEntry().getID());
                        invobj.put("journalEntryNo", invoice.getJournalEntry().getEntryNumber());  //journal entry no
                    }else {
                        Date tempdate = new Date(invoice.getCreatedon());
                        invobj.put("date", df.format(tempdate));  //date of invoice
                    }
                    if (invoice.isIsconsignment()) {
                        invobj.put("mergedCategoryData", "Consignment Customer Invoice");  //type of data
                    } else if (invoice.isFixedAssetInvoice()) {
                        invobj.put("mergedCategoryData", "Fixed Asset Disposal Invoice");  //type of data
                    } else if (invoice.isFixedAssetLeaseInvoice()) {
                        invobj.put("mergedCategoryData", "Lease Invoice");  //type of data
                    } else {
                        invobj.put("mergedCategoryData", "Customer Invoice");  //type of data
                    }
                    invobj.put("isOpeningBalanceInvoice", invoice.isIsOpeningBalenceInvoice());
                    invobj.put("isOpeningBalanceTransaction", invoice.isIsOpeningBalenceInvoice());
                    HashMap requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
                    JournalEntry je = invoice.getJournalEntry();
                    List<String> idsList = new ArrayList<String>();
                    idsList.add(invoice.getID());
                    String invid = invoice.getID();
                    Map<String, JournalEntryDetail> invoiceCustomerEntryMap = accInvoiceDAOobj.getInvoiceCustomerEntryList(idsList);
                    JournalEntryDetail d = invoiceCustomerEntryMap.get(invid);
                    Account account =null;
                    if (!invoice.isIsOpeningBalenceInvoice()) {
                        account = d.getAccount();
                    }
                    KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
                    KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
                    String currencyid = (invoice.getCurrency() == null ? currency.getCurrencyID() : invoice.getCurrency().getCurrencyID());
                    String currencyFilterForTrans = "";
                    if (requestParams.containsKey("currencyfilterfortrans") && requestParams.containsKey("isReceipt")) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                        KwlReturnObject currencyFilterResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        KWLCurrency currencyFilter = (KWLCurrency) currencyFilterResult.getEntityList().get(0);
                        invobj.put("currencyidpayment", currencyFilterForTrans);
                        invobj.put("currencysymbolpayment", (currencyFilter == null ? currency.getSymbol() : currencyFilter.getSymbol()));
                    }
                    Date invoiceCreationDate = invoice.getCreationDate();
                    double currencyToBaseRate = accCurrencyDAOobj.getCurrencyToBaseRate(requestParams, currencyid, invoiceCreationDate);
                    invobj.put("personid",invoice.isIsOpeningBalenceInvoice() ?"": customer == null ? account.getID() : customer.getID());
                    invobj.put("personemail", customer == null ? "" : customer.getEmail());
                    invobj.put("aliasname", customer == null ? "" : customer.getAliasname());
                    invobj.put("accid",invoice.isIsOpeningBalenceInvoice() ?"": account.getID());
                    invobj.put("accountid", invoice.getAccount() == null ? "" : invoice.getAccount().getID());
                    invobj.put("billno", invoice.getInvoiceNumber());
                    invobj.put("transectionno", invoice.getInvoiceNumber());
                    invobj.put("currencyid", currencyid);
                    invobj.put("currencysymbol", (invoice.getCurrency() == null ? currency.getSymbol() : invoice.getCurrency().getSymbol()));
                    invobj.put("currencyidtransaction", currencyid);
                    invobj.put("currencysymboltransaction", (invoice.getCurrency() == null ? currency.getSymbol() : invoice.getCurrency().getSymbol()));
                    invobj.put("companyaddress", invoice.getCompany().getAddress());
                    invobj.put("companyname", invoice.getCompany().getCompanyName());
                    invobj.put("oldcurrencyrate", currencyToBaseRate * 1.0);
                    invobj.put("billto", invoice.getBillTo());
                    invobj.put("shipto", invoice.getShipTo());
                    invobj.put("journalentryid", invoice.isIsOpeningBalenceInvoice() ?"":je.getID());
                    invobj.put("porefno", invoice.getPoRefNumber());
                    invobj.put("externalcurrencyrate",invoice.isIsOpeningBalenceInvoice() ?"": je.getExternalCurrencyRate());
                    invobj.put("entryno",invoice.isIsOpeningBalenceInvoice() ?"": je.getEntryNumber());
                    invobj.put("date", invoice.getCreationDate() == null ? "" : df.format(invoice.getCreationDate()));
                    invobj.put("creationdate",invoice.isIsOpeningBalenceInvoice() ?"": df.format(invoice.getCreationDate()));
                    invobj.put("invoicedate",invoice.isIsOpeningBalenceInvoice() ?"": df.format(invoice.getCreationDate()));
                    invobj.put("shipdate", invoice.getShipDate() == null ? "" : df.format(invoice.getShipDate()));
                    invobj.put("duedate", df.format(invoice.getDueDate()));
                    invobj.put("personname",invoice.isIsOpeningBalenceInvoice() ?"": customer == null ? account.getName() : customer.getName());
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
                    invobj.put("costcenterid", invoice.isIsOpeningBalenceInvoice() ?"":je.getCostcenter() == null ? "" : je.getCostcenter().getID());
                    invobj.put("costcenterName",invoice.isIsOpeningBalenceInvoice() ?"": je.getCostcenter() == null ? "" : je.getCostcenter().getName());
                    invobj.put("shipvia", invoice.getShipvia() == null ? "" : invoice.getShipvia());
                    invobj.put("fob", invoice.getFob() == null ? "" : invoice.getFob());
                    invobj.put("isClaimedInvoice", (invoice.getBadDebtType() == 1 || invoice.getBadDebtType() == 2));// for Malasian Company
                    //                    invobj.put("invoicedate", invoice.isIsOpeningBalenceInvoice() ? (invoice.getCreationDate() == null ? "" : df.format(invoice.getCreationDate())) : df.format(je.getEntryDate()));
                    invobj.put("invoicedate", invoice.getCreationDate() == null ? "" : df.format(invoice.getCreationDate()));
                    BillingShippingAddresses addresses = invoice.getBillingShippingAddresses();
                    AccountingAddressManager.getTransactionAddressJSON(invobj, addresses, false);
                    invobj.put("personname", customer.getName());
                    if (invoice.getModifiedby() != null) {
                        invobj.put("lasteditedby", StringUtil.getFullName(invoice.getModifiedby()));
                    }
                    /* Putting Amout & Amount in Base Currency in Json if invoice is opening*/
                    Double externalCurrencyRate = 0d;
                    Double invoiceOriginalAmount = 0d;
                    if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                        externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                        invoiceOriginalAmount = invoice.getOriginalOpeningBalanceAmount();
                    }
                    double amountinbase = invoiceOriginalAmount;
                    if (invoice.isIsOpeningBalenceInvoice() && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, invoiceOriginalAmount, currencyid, invoiceCreationDate, externalCurrencyRate);
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
                    /* Code for dispalying tax amount of SI in view mode from CN linking report*/
                    boolean includeprotax = false;
                    Set<InvoiceDetail> invoiceDetails = invoice.getRows();
                    for (InvoiceDetail invoiceDetail : invoiceDetails) {
                        if (invoiceDetail.getTax() != null) {
                            includeprotax = true;
                            break;
                            
                        }
                        
                    }invobj.put("includeprotax", includeprotax);
                    jArr.put(invobj);
                }
                siresult = accInvoiceDAOobj.getDebitNoteLinkedWithCreditNote(noteId, companyid);
                List dnlist = siresult.getEntityList();
                for (int index = 0; index < dnlist.size(); index++) {
                    Object[] linkedDNObj = (Object[]) dnlist.get(index);
                    String debitnoteid = (String) linkedDNObj[0];
                    Date linkdate = (Date)linkedDNObj[1];
                    JSONObject dnobj = new JSONObject();
                    KwlReturnObject invobjItr = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), debitnoteid);
                    DebitNote debitNote = (DebitNote) invobjItr.getEntityList().get(0);
                    Customer customer = debitNote.getCustomer() != null?debitNote.getCustomer():null;
                    dnobj.put("billid", debitNote.getID());
                    dnobj.put("noteid", debitNote.getID());
                    dnobj.put("companyid", debitNote.getCompany().getCompanyID());
                    dnobj.put("withoutinventory", "");
                    dnobj.put("customername", customer != null ?customer.getName():null);
                    dnobj.put("transactionNo", debitNote.getDebitNoteNumber()); 
                    if (debitNote.getJournalEntry() != null) {
                        dnobj.put("date", df.format(debitNote.getCreationDate()));  //date of debitNote
                        dnobj.put("journalEntryId", debitNote.getJournalEntry().getID());
                        dnobj.put("journalEntryNo", debitNote.getJournalEntry().getEntryNumber());  //journal entry no
                    }else {
                        Date tempdate = new Date(debitNote.getCreatedon());
                        dnobj.put("date", df.format(tempdate));  //date of debitNote
                    }
                    dnobj.put("linkingdate", df.format(linkdate));
                    dnobj.put("mergedCategoryData", "Debit Note");  //type of data
                    dnobj.put("isOpeningBalanceInvoice", debitNote.isIsOpeningBalenceDN());
                    dnobj.put("isOpeningBalanceTransaction", debitNote.isIsOpeningBalenceDN());
                    HashMap requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
                    JournalEntry je = debitNote.getJournalEntry();
                    List<String> idsList = new ArrayList<>();
                    idsList.add(debitNote.getID());
                    JournalEntryDetail d = debitNote.getVendorEntry();
                    Account account=null;
                    if(d!=null){
                         account = d.getAccount();
                    }
                    KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
                    KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
                    String currencyid = (debitNote.getCurrency() == null ? currency.getCurrencyID() : debitNote.getCurrency().getCurrencyID());
                    String currencyFilterForTrans = "";
                    if (requestParams.containsKey("currencyfilterfortrans") && requestParams.containsKey("isReceipt")) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                        KwlReturnObject currencyFilterResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        KWLCurrency currencyFilter = (KWLCurrency) currencyFilterResult.getEntityList().get(0);
                        dnobj.put("currencyidpayment", currencyFilterForTrans);
                        dnobj.put("currencysymbolpayment", (currencyFilter == null ? currency.getSymbol() : currencyFilter.getSymbol()));
                    }
                     Date invoiceCreationDate = debitNote.getCreationDate();
                    double currencyToBaseRate = accCurrencyDAOobj.getCurrencyToBaseRate(requestParams, currencyid, invoiceCreationDate);
                    dnobj.put("personid", customer == null ?(account != null ? account.getID():"") : customer.getID());
                    dnobj.put("personemail", customer == null ? "" : customer.getEmail());
                    dnobj.put("aliasname", customer == null ? "" : customer.getAliasname());
                    dnobj.put("accid", account !=null?account.getID():"");
                    dnobj.put("accountid", debitNote.getAccount() == null ? "" : debitNote.getAccount().getID());
                    dnobj.put("billno", debitNote.getDebitNoteNumber());
                    dnobj.put("noteno", debitNote.getDebitNoteNumber());
                    dnobj.put("cntype", debitNote.getDntype());
                    dnobj.put(Constants.SEQUENCEFORMATID, debitNote.getSeqformat() != null ? debitNote.getSeqformat().getID() : "");
                    dnobj.put("includingGST", debitNote.isIncludingGST());
                    dnobj.put("transectionno", debitNote.getDebitNoteNumber());
                    dnobj.put("currencyid", currencyid);
                    dnobj.put("currencysymbol", (debitNote.getCurrency() == null ? currency.getSymbol() : debitNote.getCurrency().getSymbol()));
                    dnobj.put("currencyidtransaction", currencyid);
                    dnobj.put("currencysymboltransaction", (debitNote.getCurrency() == null ? currency.getSymbol() : debitNote.getCurrency().getSymbol()));
                    dnobj.put("companyaddress", debitNote.getCompany().getAddress());
                    dnobj.put("companyname", debitNote.getCompany().getCompanyName());
                    dnobj.put("oldcurrencyrate", currencyToBaseRate * 1.0);
                    if (!debitNote.isIsOpeningBalenceDN()) {
                        dnobj.put("journalentryid", je.getID());
                        dnobj.put("externalcurrencyrate", je.getExternalCurrencyRate());
                        dnobj.put("entryno", je.getEntryNumber());
                        dnobj.put("costcenterid", je.getCostcenter() == null ? "" : je.getCostcenter().getID());
                        dnobj.put("costcenterName", je.getCostcenter() == null ? "" : je.getCostcenter().getName());
                    }
                    dnobj.put("date", df.format(debitNote.getCreationDate()));
                    dnobj.put("creationdate", df.format(debitNote.getCreationDate()));
                    dnobj.put("invoicedate", df.format(debitNote.getCreationDate()));
                    dnobj.put("costcenterid", debitNote.getCostcenter()!=null ? debitNote.getCostcenter().getID():"");
                    dnobj.put("personname", customer == null ? (account !=null ?account.getName():"") : customer.getName());
                    dnobj.put("memo", debitNote.getMemo());
                    dnobj.put("termname", customer == null ? "" : customer.getCreditTerm().getTermname());
                    dnobj.put("termid", customer == null ? "" : customer.getCreditTerm().getID());
                    dnobj.put("deleted", debitNote.isDeleted());
                    BillingShippingAddresses addresses = debitNote.getBillingShippingAddresses();
                    AccountingAddressManager.getTransactionAddressJSON(dnobj, addresses, false);
                    dnobj.put("personname", customer == null ? "" :customer.getName());
                      if (debitNote.getModifiedby() != null) {
                        dnobj.put("lasteditedby", StringUtil.getFullName(debitNote.getModifiedby()));
                    }
                    jArr.put(dnobj);
                }
                
                DateFormat userdf = (DateFormat) authHandler.getUserDateFormatter(request);
                KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), AccountingManager.getGlobalCurrencyidFromRequest(request));
                KWLCurrency currency = (KWLCurrency) curresult1.getEntityList().get(0);
                HashMap requestparams = new HashMap();
                requestparams.put("noteId", noteId);
                requestparams.put("companyid", companyid);

                /*
                 * Getting Sales Return linked in Credit Note
                 */
                KwlReturnObject result = accInvoiceDAOobj.getSalesReturnLinkedInCreditNote(requestparams);
                List invoiceList = result.getEntityList();
                if (invoiceList != null && invoiceList.size() > 0) {

                    jArr = accVendorPaymentServiceDAOobj.getSalesReturnJson(jArr, invoiceList, currency, userdf, companyid);
                }

                jobj.put(Constants.RES_count, jArr.length());
                jobj.put(Constants.RES_data, jArr);

            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }

    public ModelAndView getAllPurchaseInvoicesAndCreditNotesForPayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        boolean maxLimitReached = false;
        try {
            JSONArray DataJArr = new JSONArray();
            String paymentOptionString = request.getParameter("paymentOption");

            int paymentOption = !StringUtil.isNullOrEmpty(paymentOptionString) ? Integer.parseInt(paymentOptionString) : 0;
            if (paymentOption == 1) {//1 :- Make payment to Vendor
                DataJArr = getGoodsReceiptsForPayment(request, DataJArr); //get purchase Invoices
            } else if (paymentOption == 2) {// 2 Make Payment to customer
                getAdvanceCustomerPaymentForRefunds(request, DataJArr);//get refund/deposit payments
            }
            if (DataJArr.length() > Constants.MaxLimitOFDocumentsInReceivePayment) {
                maxLimitReached = true;
            } else if (DataJArr.length() < Constants.MaxLimitOFDocumentsInReceivePayment) {
                maxLimitReached = false;
            }
            if (!maxLimitReached) {
                getCreditNoteMergedForPayment(request, DataJArr);//get credit Notes
                if(DataJArr.length() > Constants.MaxLimitOFDocumentsInReceivePayment){
                 maxLimitReached = true;
                }
            }
            if (maxLimitReached) {
                jobj.put("maxLimitReached", maxLimitReached);
            } else {
                jobj.put("maxLimitReached", maxLimitReached);
            }

            jobj.put(Constants.RES_data, DataJArr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getGoodsReceiptsForPayment(HttpServletRequest request, JSONArray DataJArr) throws ServiceException {
        try {
            HashMap<String, Object> requestParams = accGoodsReceiptControllerCMN.getGoodsReceiptMap(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("rcmApplicable"))) {
                requestParams.put("gtaapplicable", Boolean.parseBoolean(request.getParameter("rcmApplicable")));
            }
            requestParams.put("getRecordBasedOnJEDate",true);          //sending getRecordBasedOnJEDate = true because only those invoice should be fetched whose JE posting date is greater then linking date
            KwlReturnObject result = accGoodsReceiptobj.getGoodsReceipts(requestParams);
            boolean isEdit = request.getParameter("isEdit") == null ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            requestParams.put("isEdit", isEdit);
            HashSet invoicesList = new HashSet();
            if (isEdit && !StringUtil.isNullOrEmpty(request.getParameter("billId").toString())) {
                KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Payment.class.getName(), request.getParameter("billId").toString());
                Payment payment = (Payment) paymentResult.getEntityList().get(0);
                Set<PaymentDetail> paymentDetails = payment.getRows();
                for (PaymentDetail paymentDetail : paymentDetails) {
                    invoicesList.add(paymentDetail.getGoodsReceipt().getID());
                }
            }
            DataJArr = getGoodsReceiptsJsonForPayment(requestParams, result.getEntityList(), invoicesList);//get normal purchase invoices 
            getOpeningBalanceInvoicesJsonArray(request, DataJArr);//get opening balance invoices
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentControllerCMN.getGoodsReceiptsForPayment : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }

    public JSONArray getGoodsReceiptsJsonForPayment(HashMap<String, Object> request, List<GoodsReceipt> list, HashSet invoicesList) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String companyid = (String) request.get(GoodsReceiptCMNConstants.COMPANYID);
            String basecurrencyid = (String) request.get(GoodsReceiptCMNConstants.GCURRENCYID);
            DateFormat df = (DateFormat) request.get(GoodsReceiptCMNConstants.DATEFORMAT);
            List ll = null;
            KwlReturnObject company = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company companyObj = null;
            if(company.getEntityList()!=null){
                companyObj = (Company) company.getEntityList().get(0);
            }
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            String cashAccount = preferences.getCashAccount().getID();
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            String currencyFilterForTrans = request.get("currencyfilterfortrans") != null ? (String) request.get("currencyfilterfortrans") : "";
            KwlReturnObject currencyFilterResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
            KWLCurrency currencyFilter = (KWLCurrency) currencyFilterResult.getEntityList().get(0);
            
            List<String> idsList = new ArrayList<String>();
            for (GoodsReceipt gReceipt : list) {
                idsList.add(gReceipt.getID());
            }
            Map<String, JournalEntry> GoodsReceiptJEMap = accGoodsReceiptobj.getGRInvoiceJEList(idsList);
            if (list != null && !list.isEmpty()) {
                for (GoodsReceipt gReceipt : list) {
                    if (!Boolean.parseBoolean(request.get("isEdit").toString()) || Boolean.parseBoolean(request.get("isEdit").toString()) && !(invoicesList.contains(gReceipt.getID()))) {

                        JournalEntry je = GoodsReceiptJEMap.get(gReceipt.getID());
                        JournalEntryDetail d = gReceipt.getVendorEntry();
//                        Date invoiceDate = je.getEntryDate();
                        Date invoiceDate = gReceipt.getCreationDate();
                        Date invoiceDueDate = gReceipt.getDueDate();
                        String currencyid = (gReceipt.getCurrency() == null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());
                        Account account = d.getAccount();
                        double amountdue = 0, amountDueOriginal = 0;
                        JSONObject obj = new JSONObject();
                        if (gReceipt.isIsExpenseType()) {
                            ll = accGoodsReceiptCMN.getExpGRAmountDue(request, gReceipt);
                           amountdue = gReceipt.getInvoiceamountdue();
                            if (amountdue != 0 && Math.abs(amountdue) >= 0.000001) {
                                amountDueOriginal = amountdue;
                                if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
//                                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(request, amountdue, currencyid, currencyFilterForTrans, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(request, amountdue, currencyid, currencyFilterForTrans, gReceipt.getCreationDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                                    amountdue = (Double) bAmt.getEntityList().get(0);
                                }
                            } else {
                                amountdue = 0;
                            }
//                            amountdue = (Double) ll.get(1);
//                            amountDueOriginal = (Double) ll.get(4);
                            obj.put(GoodsReceiptCMNConstants.AMOUNT, (Double) ll.get(0));//for expense invoice   
                        } else {
                            ll = accGoodsReceiptCMN.getInvoiceDiscountAmountInfo(request, gReceipt);
                            amountdue = (Double) ll.get(1);
                            amountDueOriginal = (Double) ll.get(5);
                            obj.put(GoodsReceiptCMNConstants.AMOUNT, d.getAmount()); //actual invoice amount
                        }
                        amountdue = authHandler.round(amountdue, companyid);
                        obj.put("documentid", gReceipt.getID());
                        obj.put("type", "Invoice");
                        obj.put("documentno", gReceipt.getGoodsReceiptNumber());
                        obj.put("documentType", 2);//for purchase invoice
                        obj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyid);
                        obj.put("currencyidtransaction", currencyid);
                        obj.put("currencysymboltransaction", (gReceipt.getCurrency() == null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol()));
                        obj.put("accountid", gReceipt.getAccount() == null ? "" : gReceipt.getAccount().getID());
                        obj.put(GoodsReceiptCMNConstants.ACCOUNTNAMES, (gReceipt.getAccount() == null) ? "" : gReceipt.getAccount().getName());
//                        obj.put(GoodsReceiptCMNConstants.DATE, df.format(je.getEntryDate()));
                        obj.put(GoodsReceiptCMNConstants.DATE, df.format(gReceipt.getCreationDate()));
                        obj.put("claimedDate", gReceipt.getDebtClaimedDate() == null ? "" : df.format(gReceipt.getDebtClaimedDate()));
                        obj.put("isClaimedInvoice", (gReceipt.getBadDebtType() == 1 || gReceipt.getBadDebtType() == 2));// for Malasian Company
                        if (account.getID().equals(cashAccount)) {
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, 0);
                            obj.put("amountDueOriginal", 0);
                            obj.put("amountDueOriginalSaved", 0);
                        } else {
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, authHandler.round(amountdue, companyid));
                            obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                            obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                            obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                        }
                        obj.put("currencysymbolpayment", (currencyFilter == null ? currency.getSymbol() : currencyFilter.getSymbol()));
                        obj.put(Constants.SUPPLIERINVOICENO, gReceipt.getSupplierInvoiceNo() != null ? gReceipt.getSupplierInvoiceNo() : "");//added to fetch data in dn link window
            
                        JSONObject jObj = null;
                        double discountValue = 0.0;
                        int applicableDays = -1;
                        boolean discountType = false;
                        if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                            jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                            if (jObj.has(Constants.DISCOUNT_ON_PAYMENT_TERMS) && jObj.get(Constants.DISCOUNT_ON_PAYMENT_TERMS) != null && jObj.optBoolean(Constants.DISCOUNT_ON_PAYMENT_TERMS, false)) {
                                Term term = gReceipt.getTermid();
                                if (term != null && term.getDiscountName() != null) {
                                    DiscountMaster discountMaster = term.getDiscountName();
                                    discountValue = discountMaster.getValue();
                                    discountType = discountMaster.isDiscounttype();
                                    applicableDays = term.getApplicableDays();
                                }
                            }
                        }
                        DateFormat genericDF = authHandler.getGlobalDateFormat();
                        obj.put("discountvalue", discountValue);
                        obj.put("discounttype", discountType ? Integer.parseInt(Constants.DISCOUNT_MASTER_TYPE_PERCENTAGE) : Integer.parseInt(Constants.DISCOUNT_MASTER_TYPE_FLAT));
                        obj.put("applicabledays", applicableDays);
                        obj.put("grcreationdate", genericDF.format(invoiceDate));
                        obj.put("invoiceduedate", genericDF.format(invoiceDueDate));
                        /*
                         * Get Goods Receipt Custom Data For Payment
                         */
                        accGoodsReceiptServiceDAOObj.getGoodsReceiptCustomDataForPayment(request, obj, gReceipt, je);
                        if (companyObj != null && Integer.toString(Constants.indian_country_id).equals(companyObj.getCountry().getID())) { // only for indian company
                            obj.put("invType", (gReceipt.getExpenserows().size() > 0) ? "0" : "1"); // 0 = expence Type & 1 = inventory Type
                        }else{
                            obj.put("invType", "0");// default 0 for hide product column - only use for showing column
                        }
                        jArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentControllerCMN.getGoodsReceiptsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public void getOpeningBalanceInvoicesJsonArray(HttpServletRequest request, JSONArray DataJArr) throws ServiceException {
        try {
            HashMap requestParams = accGoodsReceiptControllerCMN.getGoodsReceiptMap(request);
            DateFormat df = (DateFormat) requestParams.get(GoodsReceiptCMNConstants.DATEFORMAT);
            String currencyid = (String) requestParams.get(GoodsReceiptCMNConstants.GCURRENCYID);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String accountId = request.getParameter("accid");
            if (!StringUtil.isNullOrEmpty(accountId)) {
                requestParams.put(GoodsReceiptCMNConstants.VENDORID, accountId);
            }
            List ll = null;
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            boolean onlyAmountDue = requestParams.get(GoodsReceiptCMNConstants.ONLYAMOUNTDUE) != null;
            KWLCurrency currencyFilter=null;
            requestParams.put("excludeNormalInv", true);
            KwlReturnObject result = accGoodsReceiptobj.getOpeningBalanceInvoices(requestParams);
            List list = result.getEntityList();
            if (list != null) {
                for (Object gReceiptObj : list) {
                    String grId = gReceiptObj.toString();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grId);
                    GoodsReceipt gReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                    currencyid = (gReceipt.getCurrency() == null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());

                    Date invoiceCreationDate = gReceipt.getCreationDate();
                    double externalCurrencyRate = gReceipt.getExchangeRateForOpeningTransaction();
                    double amountdue = 0;
                    double grAmount = gReceipt.getOriginalOpeningBalanceAmount();
                    if (gReceipt.isIsExpenseType()) {
                        ll = accGoodsReceiptCMN.getExpGRAmountDue(requestParams, gReceipt);
                        amountdue = (Double) ll.get(1);
                    } else {
                        if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                            ll = new ArrayList();
                            ll.add(gReceipt.getOriginalOpeningBalanceAmount());
                            ll.add(gReceipt.getOpeningBalanceAmountDue());
                            ll.add("");
                            ll.add(false);
                            ll.add(0.0);
                        } 
                        amountdue = (Double) ll.get(1);
                    }
                    if (onlyAmountDue && authHandler.round(amountdue, companyid) == 0 ) {//remove //belongsTo1099&&gReceipt.isIsExpenseType()\\ in case of viewing all accounts. [PS]
                        continue;
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("documentid", gReceipt.getID());
                    obj.put("type", "Invoice");
                    obj.put("documentno", gReceipt.getGoodsReceiptNumber());
                    obj.put("documentType", 2);//for purchase invoice
                    obj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyid);
                    obj.put("currencyidtransaction", currencyid);
                    obj.put("currencysymboltransaction", (gReceipt.getCurrency() == null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol()));
                    obj.put("accountid", gReceipt.getAccount() != null ? gReceipt.getAccount().getID() : "");
                    obj.put(GoodsReceiptCMNConstants.ACCOUNTNAMES, (gReceipt.getAccount() == null) ? "" : gReceipt.getAccount().getName());
                    obj.put(GoodsReceiptCMNConstants.DATE, df.format(invoiceCreationDate));

                    obj.put(GoodsReceiptCMNConstants.AMOUNT, grAmount); //actual invoice amount
                    String currencyFilterForTrans = "";
                    if (requestParams.containsKey("currencyfilterfortrans")) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                         KwlReturnObject currencyFilterResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        currencyFilter = (KWLCurrency) currencyFilterResult.getEntityList().get(0);
                    }
                    double amountDueOriginal = amountdue;
                    if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                        KwlReturnObject bAmtCurrencyFilter = null;
                        if (gReceipt.isIsOpeningBalenceInvoice() && gReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, currencyid, currencyFilterForTrans, invoiceCreationDate, externalCurrencyRate);
                        } else {
                            bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyFilterForTrans, invoiceCreationDate, externalCurrencyRate);
                        }
                        amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                    }
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, authHandler.round(amountdue, companyid));
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                    obj.put("currencysymbolpayment", (currencyFilter == null ? currency.getSymbol() : currencyFilter.getSymbol()));
                    DataJArr.put(obj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentControllerCMN.getOpeningBalanceInvoicesJsonArray : " + ex.getMessage(), ex);
        }
    }
    /*
     * TO get credit notes for making payment against vendor/customer
     */
    public JSONArray getCreditNoteMergedForPayment(HttpServletRequest request, JSONArray DataJArr) throws ServiceException {

        try {
            HashMap<String, Object> requestParams = accCreditNoteController.getCreditNoteMap(request);
            String[] companyids = sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = sessionHandlerImpl.getCurrencyID(request);
            boolean isEdit = request.getParameter("isEdit") == null ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            requestParams.put("isEdit", isEdit);
            HashSet cnList = new HashSet();
            KwlReturnObject result = null;
            String companyid = "";
            boolean onlyAmountDue = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("onlyAmountDue"))) {
                onlyAmountDue = Boolean.parseBoolean(request.getParameter("onlyAmountDue"));
            }
            requestParams.put("onlyAmountDue", onlyAmountDue);
            
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute(Constants.globalCurrencyKey, gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
                boolean isNoteForPayment = false;
                boolean isVendor = false;
                if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteForPayment"))) {
                    isNoteForPayment = Boolean.parseBoolean(request.getParameter("isNoteForPayment"));
                    isVendor = Boolean.parseBoolean(request.getParameter("isVendor"));
                }
                requestParams.put("isNoteForPayment", isNoteForPayment);
                requestParams.put("isNewUI", true);
                result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                if (isEdit && !StringUtil.isNullOrEmpty(request.getParameter("billId").toString())) {
                    KwlReturnObject cndnResult = accCreditNoteDAOobj.getVendorCnPayment(request.getParameter("billId"));
                    for (Object cnObj : cndnResult.getEntityList()) {
                        Object[] objects = (Object[]) cnObj;
                        String cnnoteid = objects[0] != null ? (String) objects[1] : "";
                        cnList.add(cnnoteid);
                    }
                }
                getCreditNoteMergedJsonForPayment(request, result.getEntityList(), DataJArr, cnList, isEdit);
                int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
                /*
                 removed  isNoteForPayment   flag while fetching opening CN/DN to solve   ERP-14948
                    opening CN/DN does not load in MP/RP when Document currency and Payment method currency is different
                 */
                if (cntype == 10 || ( !isVendor)) {// cntype=10 is just for help. value 10 have no any sense.
                    result = accCreditNoteDAOobj.getOpeningBalanceCNs(requestParams);
                    requestParams.put("cntype", 10);
                    getOpeningCreditNotesJson(requestParams, result.getEntityList(), DataJArr);
                } else if (cntype == 11 || ( isVendor)) {// cntype=11 is just for help. value 11 have no any sense.
                    result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(requestParams);
                    requestParams.put("cntype", 11);
                    getOpeningCreditNotesJson(requestParams, result.getEntityList(), DataJArr);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentControllerCMN.getCreditNoteMergedForPayment : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }

    public JSONArray getCreditNoteMergedJsonForPayment(HttpServletRequest request, List list, JSONArray jArr, HashSet cnList, boolean isEdit) throws ServiceException {
        try {
            HashMap<String, Object> requestParams = accCreditNoteController.getCreditNoteMap(request);
            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            String transactionCurrencyId = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for (Object objArr : list) {
                Object[] row = (Object[]) objArr;
                if (!isEdit || (isEdit && !cnList.contains((String) row[1]))) {   // here, (String)row[1] refers to credit note id
                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                    JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);
                    JSONObject obj = new JSONObject();
                    resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) row[1]);
                    CreditNote creditMemo = (CreditNote) resultObject.getEntityList().get(0);
                    JournalEntry je = creditMemo.getJournalEntry();
                    Date creditNoteDate = null;
                    double externalCurrencyRate = 0d;
                    transactionCurrencyId = (creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getCurrencyID());
                    obj.put("currencyidtransaction", transactionCurrencyId);
                    obj.put("currencysymboltransaction", (creditMemo.getCurrency() == null ? currency.getSymbol() : creditMemo.getCurrency().getSymbol()));

                    String currencyFilterForTrans = "";
                    boolean isNoteForPayment = false;
                    if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteForPayment"))) {
                        isNoteForPayment = Boolean.parseBoolean(request.getParameter("isNoteForPayment"));
                    }
                    if (requestParams.containsKey("currencyfilterfortrans") && isNoteForPayment) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                        KwlReturnObject curresultpayment = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        KWLCurrency currencyPayment = (KWLCurrency) curresultpayment.getEntityList().get(0);
                        obj.put("currencysymbol", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                        obj.put("currencyid", (currencyPayment == null ? currency.getCurrencyID() : currencyPayment.getCurrencyID()));
                        obj.put("currencysymbolpayment", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                    } else {
                        obj.put("currencysymbol", (creditMemo.getCurrency() == null ? currency.getSymbol() : creditMemo.getCurrency().getSymbol()));
                        obj.put("currencyid", (creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getCurrencyID()));
                    }
                    double amountdue = creditMemo.isOtherwise() ? creditMemo.getCnamountdue() : 0;
                    double amountDueOriginal = creditMemo.isOtherwise() ? creditMemo.getCnamountdue() : 0;
                    if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !currencyFilterForTrans.equals(transactionCurrencyId)) {
//                        creditNoteDate = je.getEntryDate();
                        creditNoteDate = creditMemo.getCreationDate();
                        externalCurrencyRate = je.getExternalCurrencyRate();
                        KwlReturnObject bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
                        amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                    }
                    obj.put("documentid", creditMemo.getID());
                    obj.put("type", "Credit Note");
                    obj.put("documentno", creditMemo.getCreditNoteNumber());
                    obj.put("documentType", 3);//for credit note
                    obj.put("amount", creditMemo.isOtherwise() ? creditMemo.getCnamount() : details.getAmount());
                    obj.put("amountdue", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
//                    obj.put("date", df.format(je.getEntryDate()));
                    obj.put("date", df.format(creditMemo.getCreationDate()));
                    obj.put("accountid", creditMemo.getAccount() == null ? "" : creditMemo.getAccount().getID());
                    obj.put("accountnames", creditMemo.getAccount() == null ? "" : creditMemo.getAccount().getName());

                    /*
                     * Get global custom data for payment
                     */
                    accCreditNoteService.getCreditNoteCustomDataForPayment(requestParams, obj, creditMemo, je);
                    if (requestParams.containsKey("isReceipt") && obj.optDouble("amountdue", 0.0) != 0) {
                        jArr.put(obj);
                    } else if (!requestParams.containsKey("isReceipt")) {
                        jArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentControllerCMN.getCreditNoteMergedJsonForPayment : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public JSONArray getOpeningCreditNotesJson(HashMap<String, Object> requestParams, List list, JSONArray JArr) throws ServiceException {
        try {
            DateFormat df = (DateFormat) requestParams.get("df");
            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            String companyid = (String) requestParams.get("companyid");
            int cnType = (Integer) requestParams.get("cntype");
            boolean isNoteForPayment = false;
            if (requestParams.containsKey("isNoteForPayment")) {
                isNoteForPayment = (Boolean) requestParams.get("isNoteForPayment");
            }

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            if (list != null && !list.isEmpty()) {
                for (Object creditNoteObj : list) {
                    CreditNote cn = (CreditNote) creditNoteObj;
                    JSONObject obj = new JSONObject();
                    Date creditNoteDate = null;
                    double externalCurrencyRate = 0d;
                    creditNoteDate = cn.getCreationDate();
                    externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                    String transactionCurrencyId = (cn.getCurrency() == null ? currency.getCurrencyID() : cn.getCurrency().getCurrencyID());
                    obj.put("currencyidtransaction", transactionCurrencyId);
                    obj.put("currencysymboltransaction", (cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol()));
                    String currencyFilterForTrans = "";
                    if (requestParams.containsKey("currencyfilterfortrans")) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                    }
                    if (requestParams.containsKey("currencyfilterfortrans") && isNoteForPayment) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                        KwlReturnObject curresultpayment = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        KWLCurrency currencyPayment = (KWLCurrency) curresultpayment.getEntityList().get(0);
                        obj.put("currencysymbol", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                        obj.put("currencyid", (currencyPayment == null ? currency.getCurrencyID() : currencyPayment.getCurrencyID()));
                        obj.put("currencysymbolpayment", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                    } else {
                        obj.put("currencysymbol", (cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol()));
                        obj.put("currencyid", (cn.getCurrency() == null ? currency.getCurrencyID() : cn.getCurrency().getCurrencyID()));
                    }
                    double amountdue = cn.isOtherwise() ? cn.getCnamountdue() : 0;
                    double amountDueOriginal = cn.isOtherwise() ? cn.getCnamountdue() : 0;
                    if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !currencyFilterForTrans.equals(transactionCurrencyId)) {
                        KwlReturnObject bAmtCurrencyFilter = null;
                        if (cn.isIsOpeningBalenceCN() && cn.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            requestParams.put("isRevalue",true);
                            bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
                            amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                        } else {
                            bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
                            amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                        }
                    }
                    obj.put("documentid", cn.getID());
                    obj.put("type", "Credit Note");
                    obj.put("documentno", cn.getCreditNoteNumber());
                    obj.put("documentType", 3);//3 for credit note
                    obj.put("amount", cn.getCnamount());
                    obj.put("date", df.format(cn.getCreationDate()));
                    obj.put("amountdue", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("accountid", cn.getAccount() == null ? "" : cn.getAccount().getID());
                    obj.put("accountnames", cn.getAccount() == null ? "" : cn.getAccount().getName());
                    JArr.put(obj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentControllerCMN.getOpeningCreditNotesJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }
   
    
     public JSONArray getAdvanceCustomerPaymentForRefunds(HttpServletRequest request, JSONArray jArr) throws ServiceException {
        try {
            List<ReceiptAdvanceDetail> advanceDetailList = new ArrayList<ReceiptAdvanceDetail>();
            DateFormat df = authHandler.getDateOnlyFormat();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            HashMap<String, Object> requestParams = accReceiptControllerCMN.getReceiptRequestMapJSON(paramJobj);
            
            requestParams.put("accid", request.getParameter("accid") != null ? request.getParameter("accid") : "");
            String currencyFilterForTrans = "";
            currencyFilterForTrans = (request.getParameter("currencyfilterfortrans") == null) ? "" : request.getParameter("currencyfilterfortrans");
            KwlReturnObject result = accReceiptDAOobj.getReceiptAdvanceAmountDueDetails(requestParams);
            advanceDetailList = result.getEntityList();
            for (ReceiptAdvanceDetail advanceDetail : advanceDetailList) {
                JSONObject obj = new JSONObject();
                Receipt re = advanceDetail.getReceipt();
                String currencyid = re.getCurrency().getCurrencyID();
                if (re.getCustomer() != null) {
                    obj.put("accountid", re.getCustomer().getID());
                    obj.put("accountnames", re.getCustomer().getName());
                }
                obj.put("documentid", advanceDetail.getId());
                obj.put("documentno", re.getReceiptNumber());
                obj.put("documentType", 1); //1 for advance payment type. 
                obj.put("date", df.format(re.getCreationDate()));
                double amountdue = advanceDetail.getAmountDue();
                double amountDueOriginal = advanceDetail.getAmountDue();
                double externalCurrencyRate = re.getExternalCurrencyRate();
                obj.put("amountDueOriginal", advanceDetail.getAmountDue());
                obj.put("amountDueOriginalSaved", advanceDetail.getAmountDue());
                obj.put("amount", advanceDetail.getAmount());
                obj.put("currencyid", currencyid);
                obj.put("currencyidtransaction", currencyid);
                obj.put("currencysymboltransaction", re.getCurrency().getSymbol());
                if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyFilterForTrans, re.getCreationDate(), externalCurrencyRate);
                    amountdue = (Double) bAmt.getEntityList().get(0);
                }
                obj.put("amountdue", authHandler.round(amountdue, companyid));
                obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                
                //Get Custom Field Data for Refund/Deposit
                accReceiptServiceDAOobj.getAdvanceReceiptCustomData(requestParams, advanceDetail, obj);
                jArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentControllerCMN.getAdvanceCustomerPaymentForRefunds : " + ex.getMessage(), ex);
        }
        return jArr;
    }
     
    public ModelAndView getDocumentsForLinkingWithAdvancePayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getDocumentsForLinkingWithAdvancePayment(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accVendorPaymentControllerCMN.getDocumentsForLinkingWithAdvancePayment:" + ex.getMessage();
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getDocumentsForLinkingWithAdvancePayment(HttpServletRequest request) {
        JSONObject returnObject = new JSONObject();
        try {
            JSONArray DataArray = new JSONArray();
            DataArray = getGoodsReceiptsForPayment(request, DataArray);
            DataArray = getCreditNoteMergedForPayment(request, DataArray);
            returnObject.put(Constants.RES_data, DataArray);
            returnObject.put(Constants.RES_count, DataArray.length());
        } catch (Exception ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }

        return returnObject;
    }

    public ModelAndView getLinkedDocumentsAgainstAdvance(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean isSuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("dateformat", authHandler.getDateFormatter(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("bills"))) {
                requestParams.put("bills", request.getParameterValues("bills"));
            }

            jArr = getLinkedDocumentsAgainstAdvanceJSON(requestParams);
            isSuccess = true;
            jObj.put(Constants.RES_data, jArr);

        } catch (SessionExpiredException e) {
            msg = e.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception e) {
            msg = e.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                jObj.put(Constants.RES_success, isSuccess);
                jObj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }

    public JSONArray getLinkedDocumentsAgainstAdvanceJSON(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray jArray = new JSONArray();
        try {
            jArray = getLinkedInvoicesAgainstAdvance(requestParams, jArray);
            jArray = getLinkedCreditNotesAgainstAdvance(requestParams, jArray);
        } catch (Exception e) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return jArray;
    }

    public JSONArray getLinkedCreditNotesAgainstAdvance(HashMap<String, Object> requestParams, JSONArray jArray) throws ServiceException {
        String companyid = (String) requestParams.get("companyid");
        String currencyId = (String) requestParams.get(Constants.globalCurrencyKey);
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyId);
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        String[] paymentid = null;
        DateFormat df = (DateFormat) requestParams.get("dateformat");
        try {
            if (requestParams.containsKey("bills")) {
                paymentid = (String[]) requestParams.get("bills");
            }
            for (int index = 0; index < paymentid.length; index++) {
                KwlReturnObject receiptResult = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid[index]);
                Payment payment = (Payment) receiptResult.getEntityList().get(0);
                Set<Invoice> CnSet = new HashSet<Invoice>();
                Set<LinkDetailPaymentToCreditNote> linkDetailReceipt = payment.getLinkDetailPaymentToCreditNote();
                for (LinkDetailPaymentToCreditNote LDR : linkDetailReceipt) {
                    if (!CnSet.contains(LDR.getCreditnote())) {
                        CreditNote Cn = LDR.getCreditnote();
                        JSONObject jObj = new JSONObject();
                        Date cnCreationDate = null;
                        cnCreationDate = Cn.getCreationDate();
                        double amountdueInDnCurrency = 0.0;
                        if (Cn.isNormalCN()) {
                            amountdueInDnCurrency = Cn.getCnamountdue();
                        } else {
                            amountdueInDnCurrency = Cn.getOpeningBalanceAmountDue() * (LDR.getExchangeRateForTransaction() == 0 ? 1 : LDR.getExchangeRateForTransaction());
                        }
                        amountdueInDnCurrency += LDR.getAmountInCNCurrency();
                        amountdueInDnCurrency = authHandler.round(amountdueInDnCurrency, companyid);
                        jObj.put("billid", Cn.getID());
                        jObj.put("type", "Credit Note");
                        jObj.put("linkdetailid", LDR.getID());
                        jObj.put("documentno", LDR.getCreditnote().getCreditNoteNumber());
                        jObj.put("date", df.format(cnCreationDate));
                        jObj.put("amountDueOriginal", amountdueInDnCurrency);
                        jObj.put("exchangeratefortransaction", LDR.getExchangeRateForTransaction());
                        double amountdueInPaymentCurrency = authHandler.round(amountdueInDnCurrency * LDR.getExchangeRateForTransaction(), companyid);
                        jObj.put("amountdue", amountdueInPaymentCurrency);
                        jObj.put("linkamount", LDR.getAmount());
                        jObj.put("currencysymbol", Cn.getCurrency() == null ? (LDR.getPayment().getCurrency() == null ? currency.getSymbol() : LDR.getPayment().getCurrency().getSymbol()) : Cn.getCurrency().getSymbol());
                        jObj.put("currencysymboltransaction", Cn.getCurrency() == null ? (LDR.getPayment().getCurrency() == null ? currency.getSymbol() : LDR.getPayment().getCurrency().getSymbol()) : Cn.getCurrency().getSymbol());
                        jObj.put("currencysymbolpayment", LDR.getPayment().getCurrency().getSymbol());
                        jArray.put(jObj);

                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return jArray;
    }

    public JSONArray getLinkedInvoicesAgainstAdvance(HashMap<String, Object> requestParams, JSONArray jArray) throws ServiceException {
        String currencyId = (String) requestParams.get(Constants.globalCurrencyKey);
        String companyid = (String) requestParams.get("companyid");
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyId);
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        String[] paymentid = null;
        DateFormat df = (DateFormat) requestParams.get("dateformat");
        try {
            if (requestParams.containsKey("bills")) {
                paymentid = (String[]) requestParams.get("bills");
            }
            for (int index = 0; index < paymentid.length; index++) {
                KwlReturnObject receiptResult = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid[index]);
                Payment payment = (Payment) receiptResult.getEntityList().get(0);
                Set<Invoice> invoiceSet = new HashSet<Invoice>();
                Set<LinkDetailPayment> LinkDetailPayment = payment.getLinkDetailPayments();
                for (LinkDetailPayment LDR : LinkDetailPayment) {
                    if (!invoiceSet.contains(LDR.getGoodsReceipt())) {
                        JSONObject jObj = new JSONObject();
                        Date invoiceCreationDate = null;
                        GoodsReceipt GR = LDR.getGoodsReceipt();
                        Date invoicelinkingDate = null;
                        invoicelinkingDate =  LDR.getPaymentLinkDate();
                        invoiceCreationDate = GR.getCreationDate();
                        double amountdueInInvoiceCurrency = 0.0;
                        if (GR.isNormalInvoice()) {
                            List ll;
                            if (GR.isIsExpenseType()) {
                                ll = accGoodsReceiptCMN.getExpGRAmountDue(requestParams, GR);
                                amountdueInInvoiceCurrency = (ll.isEmpty() ? 0 : (Double) ll.get(4));
                            } else {
                                if (Constants.InvoiceAmountDueFlag) {
                                    ll = accGoodsReceiptCMN.getInvoiceDiscountAmountInfo(requestParams, GR);
                                    amountdueInInvoiceCurrency = (ll.isEmpty() ? 0 : (Double) ll.get(5));
                                } else {
                                    ll = accGoodsReceiptCMN.getGRAmountDue(requestParams, GR);
                                    amountdueInInvoiceCurrency = (ll.isEmpty() ? 0 : (Double) ll.get(5));
                                }
                            }
                        } else {
                            amountdueInInvoiceCurrency = GR.getOpeningBalanceAmountDue() * (LDR.getExchangeRateForTransaction() == 0 ? 1 : LDR.getExchangeRateForTransaction());
                        }
                        amountdueInInvoiceCurrency += LDR.getAmountInGrCurrency();
                        amountdueInInvoiceCurrency = authHandler.round(amountdueInInvoiceCurrency, companyid);
                        jObj.put("billid", GR.getID());
                        jObj.put("type", "Invoice");
                        jObj.put("linkdetailid", LDR.getID());
                        jObj.put("documentno", GR.getGoodsReceiptNumber());
                        jObj.put("transectionno", GR.getGoodsReceiptNumber());
                        jObj.put("amountDueOriginal", amountdueInInvoiceCurrency);
                        jObj.put("date", df.format(invoiceCreationDate));
                        jObj.put("invoicelinkingdate", df.format(invoicelinkingDate));
                        jObj.put("exchangeratefortransaction", LDR.getExchangeRateForTransaction());
                        double amountdueInPaymentCurrency = authHandler.round(amountdueInInvoiceCurrency * LDR.getExchangeRateForTransaction(), companyid);
                        jObj.put("amountdue", amountdueInPaymentCurrency);
                        jObj.put("linkamount", LDR.getAmount());
                        jObj.put("currencysymbol", GR.getCurrency() == null ? (GR.getCurrency() == null ? currency.getSymbol() : GR.getCurrency().getSymbol()) : GR.getCurrency().getSymbol());
                        jObj.put("currencysymbolpayment", payment.getCurrency().getSymbol());
                        jObj.put("currencysymboltransaction", GR.getCurrency().getSymbol());
                        jArray.put(jObj);

                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return jArray;
    }
    
    public ModelAndView getDocumentsForLinkingWithDebitNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
//            jobj = getDocumentsForLinkingWithDebitNoteJSON(request);
            jobj = accVendorPaymentServiceDAOobj.getDocumentsForLinkingWithDebitNoteJSON(paramJobj);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accVendorPaymentControllerCMN.getDocumentsForLinkingWithDebitNoteJSON:" + ex.getMessage();
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
        public JSONObject getDocumentsForLinkingWithDebitNoteJSON(HttpServletRequest request) {
        JSONObject returnObject = new JSONObject();
        try {
             JSONArray DataArray = new JSONArray();
            DataArray = getGoodsReceiptsForPayment(request, DataArray);
            DataArray = getCreditNoteMergedForPayment(request, DataArray);
            returnObject.put(Constants.RES_data, DataArray);
            returnObject.put(Constants.RES_count, DataArray.length());
        } catch (Exception e) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnObject;
    }

    /**
     * Description : Below Method is used to fetch TDS Amount of Advances & all Advance Payment IDs made against given Vendor.
     * @param <HttpServletRequest request> used to get request parameters 
     * @param <HttpServletResponse response> used to response for the request
     * @return
     */    
    public ModelAndView getTDSAmountAgainstAdvanceDocument(HttpServletRequest request, HttpServletResponse response) {
        //To fetch TDS Amount of Advances & all Advance Payment IDs made against given Vendor.
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean isSuccess = false;
        String msg = "";
        String pid = "";
        try {
            String goodsReceiptID = "";
            boolean isEdit = false;
            Date billDate = null;//To add filter according to bill/transaction date of purchase invoice.
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("vendorid", request.getParameter("vendorid"));
            if (!StringUtil.isNullOrEmpty((String) request.getParameter("isEdit"))) {
                isEdit = Boolean.parseBoolean(request.getParameter("isEdit"));
            }
            if (!StringUtil.isNullOrEmpty((String) request.getParameter("billdate"))) {
                DateFormat df = authHandler.getDateOnlyFormat();
                billDate = df.parse((String) request.getParameter("billdate"));
                requestParams.put("billDate", billDate);
            }
            if (!StringUtil.isNullOrEmpty((String) request.getParameter("goodsReceiptID"))) {
                goodsReceiptID = (String) request.getParameter("goodsReceiptID");
            }
            //In Edit case, to get used Advance Payment IDs (For TDS) if any.
            if (isEdit && !StringUtil.isNullOrEmpty(goodsReceiptID)) {
                requestParams.put("isEdit", isEdit);
                requestParams.put("goodsReceiptID", goodsReceiptID);
            }
            DataJArr = accVendorPaymentServiceDAOobj.getAdvanceDetailsAgainstVendor(requestParams);
            jobj.put(DATA, DataJArr.length() > 0 ? DataJArr : "" );
            jobj.put( "totalCount", DataJArr.length());
            isSuccess = true;
        } catch (Exception e) {
            msg = e.getMessage();
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }finally {
            try {
                jobj.put(Constants.RES_success, isSuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    /**
     * Description : Below Method is used to Delete TDS Master Rates.
     * @param request used to get request parameters
     * @param response used to response for the request
     * @return 
     */
    public ModelAndView deleteTDSMasterRates(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            Locale locale = RequestContextUtils.getLocale(request);
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(Constants.RES_data, request.getParameter(Constants.RES_data));
            requestParams.put("locale", RequestContextUtils.getLocale(request));
            int deleteCnt = accVendorPaymentServiceDAOobj.deleteTDSMasterRates(requestParams);
            if (deleteCnt > 0) {
                msg = messageSource.getMessage("acc.commo.Allselectedrecord(s)havebeendeletedsuccessfully", null, locale);
            } else {
                msg = messageSource.getMessage("acc.vendorpaymentcmn.TDSMasterRateUsedInTransaction", null, locale);
            }
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, "accVendorPaymentControllerCMN.deleteTDSMasterRates", ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (Exception ex) {
                Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, "accVendorPaymentControllerCMN.deleteTDSMasterRates", ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    /**
     * Description : Below Method is used to Export TDS Rates.
     * @param request used to get request parameters
     * @param response used to response for the request
     * @return
     */
    public ModelAndView exportTDSMasterRates(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArr = new JSONArray();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.REQ_startdate, authHandler.getDateOnlyFormat().parse(request.getParameter(Constants.REQ_startdate)));
            requestParams.put(Constants.REQ_enddate, authHandler.getDateOnlyFormat().parse(request.getParameter(Constants.REQ_enddate)));
            requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            requestParams.put("companyid", companyid);
            jArr = accVendorPaymentServiceDAOobj.getTDSMasterRates(requestParams);
            jobj.put(Constants.data, jArr);
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException | ParseException | ServiceException | JSONException | IOException ex) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(Constants.jsonView_ex, Constants.model, jobj.toString());
    }
    
    /**
     * Description : Below Method is used to fetch TDS Rates.
     *
     * @param request used to get request parameters
     * @param response used to response for the request
     * @return
     */
    public ModelAndView getTDSMasterRates(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean isSuccess = false;
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Date startDate = authHandler.getDateOnlyFormat().parse(request.getParameter(Constants.REQ_startdate));
            Date endDate = authHandler.getDateOnlyFormat().parse(request.getParameter(Constants.REQ_enddate));
            String start = (String) request.getParameter("start");
            String limit = (String) request.getParameter("limit");
            DateFormat df = authHandler.getDateOnlyFormat();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.REQ_startdate, startDate);
            requestParams.put(Constants.REQ_enddate, endDate);
            requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            requestParams.put("companyid", companyid);
            jArr = accVendorPaymentServiceDAOobj.getTDSMasterRates(requestParams);
            JSONArray pagedJson = jArr;      // pagedJson is applied to get work paging properly.
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put(Constants.data, pagedJson);
            jobj.put(Constants.RES_TOTALCOUNT, jArr.length());
            isSuccess = true;
        } catch (Exception e) {
            msg = e.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                jobj.put(Constants.RES_success, isSuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public void TDSRates(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (!StringUtil.isNullOrEmpty(request.getParameter("type"))) {
                String FileHeaderName = "", FileName = "";
                if (request.getParameter("type").equalsIgnoreCase("Resident")) {
                    FileHeaderName = " TDS Rates For Resident FY 15 16 & 16-17 Other Than Salary.xls";
                    FileName = "TDSRatesforResidentFY1516_1617OtherthanSalary.xls";
                } else if (request.getParameter("type").equalsIgnoreCase("NonResident")) {
                    FileHeaderName = " TDS Rates For Non Residents FY 15 16 & 16-17 Other Than Salary.xls";
                    FileName = "TDSRatesNonResidentsFY1516_1617Otherthansalary.xls";
                }
                response.setHeader("Content-Disposition", "attachment; filename=\"" + FileHeaderName);
                response.setContentType("application/octet-stream");
                File outFile = new File(request.getSession().getServletContext().getRealPath("IndiaCompliance") + "/StaticReports/" + FileName);
                FileInputStream fin = new FileInputStream(outFile);
                byte fileContent[] = new byte[(int) outFile.length()];
                fin.read(fileContent);
                response.getOutputStream().write(fileContent);
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }
        } catch (Exception e) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public ModelAndView getTDSCalculationDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String deducteeType = request.getParameter("deducteetype");
            int residentialstatus = !StringUtil.isNullOrEmpty(request.getParameter("residentialstatus")) ? Integer.parseInt(request.getParameter("residentialstatus")) : 0;
            String natureOfPayment = request.getParameter("natureofPayment");
            String vendorID = request.getParameter("vendorID");
            double amount = !StringUtil.isNullOrEmpty(request.getParameter("amount")) ? Double.parseDouble(request.getParameter("amount")) : 0.0;
            boolean isBasicExemptionExc=false;
            boolean isIngoreExemptLimit = true;
            String billdate = "";
            double additionalLineTDSAssesableAmount=0.0;
            if(!StringUtil.isNullOrEmpty(request.getParameter("additionalAmount"))){
                additionalLineTDSAssesableAmount=Double.parseDouble(request.getParameter("additionalAmount"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("isIngoreExemptLimit"))){
                isIngoreExemptLimit = Boolean.parseBoolean(request.getParameter("isIngoreExemptLimit"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("billdate"))) {
                billdate =request.getParameter("billdate");
            }
            if(request.getParameter("isBasicExemptionExc") != null){
                isBasicExemptionExc=Boolean.valueOf(request.getParameter("isBasicExemptionExc"));
            }
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            
            // Get Financial Year Start Date and End Date
            KwlReturnObject companyAccprefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) companyAccprefresult.getEntityList().get(0);
            
            HashMap<String,Date> hmFinancialsdate = authHandler.getFinancialsDates(companyAccountPreferences.getFinancialYearFrom(),sdf.parse(billdate));
            Date financialYearStartDate = hmFinancialsdate.get("financialstartdate");
            Date financialYearEndDate = hmFinancialsdate.get("financialenddate");
            
            KwlReturnObject vendorresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorID);
            Vendor vendor = (Vendor) vendorresult.getEntityList().get(0);
            boolean isTDSApplicable = false;
            if(!StringUtil.isNullOrEmpty(request.getParameter("isTDSApplicable"))){ // TDS is applicable or not at time of creation transaction
                isTDSApplicable = Boolean.parseBoolean(request.getParameter("isTDSApplicable"));
            }else{
                isTDSApplicable = vendor.isIsTDSapplicableonvendor();
            }
//            if (extraCompanyPreferences.isTDSapplicable() && vendor.isIsTDSapplicableonvendor()) {
            if (extraCompanyPreferences.isTDSapplicable() && isTDSApplicable) {
                //Proceed only if TDS is applicable at Company Level as well as at respective Vendor Level.
                
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                if (!StringUtil.isNullOrEmpty(natureOfPayment) && !StringUtil.isNullOrEmpty(deducteeType) && !StringUtil.isNullOrEmpty(billdate) && !StringUtil.isNullOrEmpty(vendorID)) {
                    requestParams.put("activeFromDate", sdf.format(financialYearStartDate));
                    requestParams.put("activeToDate", sdf.format(financialYearEndDate)); 
                    requestParams.put("natureofPayment", natureOfPayment);
                    requestParams.put("deducteeType", deducteeType);
                    requestParams.put("residentialstatus", residentialstatus);
                    requestParams.put("amount", amount);
                    requestParams.put("vendorID", vendorID);
                    requestParams.put("isBasicExemptionExc", isBasicExemptionExc);
                    requestParams.put("isIngoreExemptLimit", isIngoreExemptLimit);
                    requestParams.put("billdate", billdate);
                    requestParams.put("additionalLineTDSAssesableAmount", additionalLineTDSAssesableAmount);
                    jobj = getTDSCalculationDetailsJobj(requestParams);
                    issuccess = true;
                } else if (StringUtil.isNullOrEmpty(natureOfPayment)) {
                    msg = messageSource.getMessage("acc.vendorpaymentcontrollercmn.NatureOfPaymentNotSet", null, RequestContextUtils.getLocale(request));   //"Nature Of Payment is not set for selected Vendor, so cannot apply TDS.";
                } else if (StringUtil.isNullOrEmpty(deducteeType)) {
                    msg = messageSource.getMessage("acc.vendorpaymentcontrollercmn.deducteeTypeNotSet", null, RequestContextUtils.getLocale(request));   //"Deductee Type is not set for selected Vendor, so cannot apply TDS.";
                } else {
                    msg = messageSource.getMessage("acc.vendorpaymentcontrollercmn.failedtoapplyTDS", null, RequestContextUtils.getLocale(request));   //"Failed to apply TDS.";
                }
            } else {
                msg = messageSource.getMessage("acc.vendorpaymentcontrollercmn.TDSIsNotAppliedForSelectedVendor", null, RequestContextUtils.getLocale(request));   //"Failed to apply TDS.";
            }
        } catch (AccountingException ex) {
            msg = "" + ex.getMessage();
        } catch (Exception e) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public JSONObject getTDSCalculationDetailsJobj(HashMap<String, Object> requestParams) throws AccountingException, ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        double rate = 0.0;
        double amount = 0.0;
        double exemptLimit = 0.0;
        double totalamount = 0.0;
        String id = "";
        String deducteeTypeName = "";
        amount = (Double) requestParams.get("amount");
        String vendorID = (String) requestParams.get("vendorID");
        boolean isBasicExemptionExc = (Boolean) requestParams.get("isBasicExemptionExc");
        boolean isIngoreExemptLimit = (boolean)requestParams.get("isIngoreExemptLimit");
        String companyid = (String) requestParams.get("companyid");
        KwlReturnObject vendorresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorID);
        Vendor vendor = (Vendor) vendorresult.getEntityList().get(0);
//        KwlReturnObject kjobjAmount = accVendorPaymentDAO.getTotalAmountofVendorpayment(requestParams);
//        List listobjAmount = kjobjAmount.getEntityList();
//        if (listobjAmount.size() > 0) {
//            if (listobjAmount.get(0) != null) {
//                totalamount = (Double) listobjAmount.get(0);
//                totalamount = amount + totalamount;
//                requestParams.put("totalamount", totalamount);
//            } else {
//                requestParams.put("totalamount", amount);
//            }
//        } else {
          requestParams.put("totalamount", amount);
//        }
        if (requestParams.containsKey("deducteeType")) {
            MasterItem masterItem = null;
            if (!StringUtil.isNullOrEmpty((String) requestParams.get("deducteeType"))) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), (String) requestParams.get("deducteeType"));
                masterItem = (MasterItem) result.getEntityList().get(0);
                 if (masterItem != null) {
                    deducteeTypeName = masterItem.getValue();
                }
            }
        }
        KwlReturnObject kjobj = accCommonTablesDAO.getTDSRate(requestParams);
        List listobj = kjobj.getEntityList();
        amount = Double.parseDouble(requestParams.get("amount").toString());
        if (listobj.size() > 0) {
            Object[] row = (Object[]) listobj.get(0);
            if (row != null) {
                exemptLimit = Double.parseDouble(row[2].toString());
            }
        } else {
            jobj.put("success", false);
            throw new AccountingException("TDS rule is not created for the given Nature of Payment.");
        }
        
        if (deducteeTypeName.equalsIgnoreCase(IndiaComplianceConstants.DEDUCTEETYPE_UNKNOWN)) {
            jobj.put(Constants.RES_success, true);
            rate = 0.0;
            id = "";
        } else if (vendor != null && vendor.getNonLowerDedutionApplicable().equals(IndiaComplianceConstants.NonLowerDedutionApplicable)) {//Check Whether Non Deduction or Lower Deduction Applicable
            if (vendor.getDeductionReason().equals(IndiaComplianceConstants.Reason_Non_Deduction_or_Lower_Deduction)) {//If Reason is Lower Deduction or Non Deduction then Rate will be Lower Rate Fetched.
                rate = vendor.getLowerRate();
            } else if (vendor.getDeductionReason().equals(IndiaComplianceConstants.Reason_Non_Deduction_Declaration) || vendor.getDeductionReason().equals(IndiaComplianceConstants.Reason_Deduction_Transporter)) {
                //Else if Reason is Non Deduction then Rate will be Zero.
                rate = 0;
            } else if (vendor.getDeductionReason().equals(IndiaComplianceConstants.Reason_Basic_Exemption_Reached)) {
                // Check If Vendor is select Basic Exemption then get Normal rate 
                amount = (Double) requestParams.get("amount");
                if (listobj.size() > 0) {
                    Object[] row = (Object[]) listobj.get(0);
                    if (row != null) {
                        if (isBasicExemptionExc) {
                            rate = Double.parseDouble(row[0].toString());
                        } else {
                            rate = 0;
                        }
                        id = row[1].toString();
                    }
                    jobj.put(Constants.RES_success, true);
                } else {//TDS Rate not present for selected Vendor's Combination.(eg. NOP,Residentioal status, Deductee type.)
                    jobj.put(Constants.RES_success, false);
                    throw new AccountingException("TDS Rate is not set for the given Nature of Payment.");
                }
            }
            jobj.put(Constants.RES_success, true);
        } else if (vendor != null && !StringUtil.isNullOrEmpty(vendor.getPanStatus()) && StringUtil.isNullOrEmpty(vendor.getPANnumber()) && vendor.getResidentialstatus() == IndiaComplianceConstants.ResidentialStatus_Resident) {
            rate = vendor.getHigherTDSRate();
            jobj.put(Constants.RES_success, true);
        } else {
            amount = (Double) requestParams.get("amount");
            if (listobj.size() > 0) {
                Object[] row = (Object[]) listobj.get(0);
                if (row != null) {
                    rate = Double.parseDouble(row[0].toString());
                    id = row[1].toString();
                }
                jobj.put(Constants.RES_success, true);
            } else {//TDS Rate not present for selected Vendor's Combination.(eg. NOP,Residentioal status, Deductee type.)
                jobj.put(Constants.RES_success, false);
                throw new AccountingException("Selected Vendor's Combination have no TDS Master Rate Present.");
            }
        }
        double tdsamount = 0;
        if (isIngoreExemptLimit) {
            tdsamount = (amount * rate) / 100;
            jobj.put("tdsrate", rate);
            jobj.put("isExemptLimitCrossed", true);
        } else {
            double additionalLineTDSAssesableAmount = (double)requestParams.get("additionalLineTDSAssesableAmount");
            JSONObject jobjAmount = accCommonTablesDAO.totalTDSAssessableAmountForExemptLimit(requestParams);
            double totalAssesableAmount = jobjAmount.getDouble("totalTDSAssesableAmount");
            double totalTDSamountdeducted = jobjAmount.getDouble("totalTDSamountdeducted");
            boolean tdsjemeppeing_deducted = jobjAmount.getBoolean("deductedWithMapping");
            double assesebleAmount = (totalAssesableAmount+additionalLineTDSAssesableAmount) + amount;
            if (totalTDSamountdeducted > 0 || assesebleAmount > exemptLimit || isIngoreExemptLimit || tdsjemeppeing_deducted) { // Limit crossed , Do not  check exempt Limit for advance Payment
                tdsamount = (amount * rate) / 100;
                jobj.put("tdsrate", rate);
                jobj.put("isExemptLimitCrossed", true);
            } else { // Limit not corssed
                jobj.put("tdsrate", 0.0);
                jobj.put("isExemptLimitCrossed", false);
            }
        }
        jobj.put("ruleid", id);
        jobj.put("tdsmasterrateruleid", id);
        jobj.put("tdsamount", Math.ceil(tdsamount));
        /*
         * ERP-30152 Add Json Basic Exemption respective TDS Rate
         */
        if (!StringUtil.isNullOrEmpty(id)) {
            int objId=Integer.parseInt(id);
            KwlReturnObject tdsrateresult = accountingHandlerDAOobj.getObject(TDSRate.class.getName(), objId);
            TDSRate tdsrateObj = (TDSRate) tdsrateresult.getEntityList().get(0);
            jobj.put("basicexemptionpertransaction", authHandler.round(tdsrateObj.getBasicexemptionpertransaction(), companyid));
            jobj.put("basicexemptionperannum", authHandler.round(tdsrateObj.getBasicexemptionperannum(), companyid));
        }

        return jobj;
    }
    
    /**
     * Description : Controller for getting linkedIn transactions for Refund
     * Payment
     *
     * @param request
     * @param response
     * @return ModelAndView
     */
    public ModelAndView getLinkedDocumentsAgainstRefundPayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean isSuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("dateformat", authHandler.getDateOnlyFormat());
            if (!StringUtil.isNullOrEmpty(request.getParameter("bills"))) {
                requestParams.put("bills", request.getParameterValues("bills"));
            }
            jArr = getLinkedDocumentsAgainstRefundPaymentJSON(requestParams);
            isSuccess = true;
            jObj.put(Constants.RES_data, jArr);

        } catch (SessionExpiredException e) {
            msg = e.getMessage();
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception e) {
            msg = e.getMessage();
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                jObj.put(Constants.RES_success, isSuccess);
                jObj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }
    
    /**
     * Description : Below Method is used to get Advance Payment linked to Refund Payment
     *
     * @param <requestParams> This Map is used to get gcurrencyid, dateformat, bills
     * @return JSONArray
     */
    public JSONArray getLinkedDocumentsAgainstRefundPaymentJSON(Map<String, Object> requestParams) throws ServiceException {
        JSONArray jArray = new JSONArray();
        try {
            String companyid = (String) requestParams.get("companyid");
            String currencyId = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyId);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("dateformat");
            String[] paymentid = null;
            if (requestParams.containsKey("bills")) {
                paymentid = (String[]) requestParams.get("bills");
            }

            for (int index = 0; index < paymentid.length; index++) {
                KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid[index]);
                Payment payment = (Payment) paymentResult.getEntityList().get(0);

                Set<LinkDetailPaymentToAdvancePayment> linkDetailReceipt = payment.getLinkDetailPaymentsToAdvancePayment();
                for (LinkDetailPaymentToAdvancePayment LDR : linkDetailReceipt) {
                    Receipt receipt = LDR.getReceipt();
                    JSONObject jObj = new JSONObject();

                    double amountdueInAdvPaymentCurrency = 0.0;
//                    Date creationDate = receipt.getJournalEntry().getEntryDate();
                    Date creationDate = receipt.getCreationDate();

                    Set<ReceiptAdvanceDetail> advDetails = receipt.getReceiptAdvanceDetails();
                    for (ReceiptAdvanceDetail advDetail : advDetails) {
                        amountdueInAdvPaymentCurrency += advDetail.getAmountDue();
                    }

                    amountdueInAdvPaymentCurrency += LDR.getAmountInPaymentCurrency();
                    amountdueInAdvPaymentCurrency = authHandler.round(amountdueInAdvPaymentCurrency, companyid);
                    jObj.put("billid", receipt.getID());
                    jObj.put("type", "Advance Payment");
                    jObj.put("linkdetailid", LDR.getID());
                    jObj.put("documentno", receipt.getReceiptNumber());
                    jObj.put("date", df.format(creationDate));
                    jObj.put("amountDueOriginal", amountdueInAdvPaymentCurrency);
                    jObj.put("exchangeratefortransaction", LDR.getExchangeRateForTransaction());
                    double amountdueInPaymentCurrency = authHandler.round(amountdueInAdvPaymentCurrency * LDR.getExchangeRateForTransaction(), companyid);
                    jObj.put("amountdue", amountdueInPaymentCurrency);
                    jObj.put("linkamount", LDR.getAmount());
                    jObj.put("currencysymbol", receipt.getCurrency() == null ? (LDR.getPayment().getCurrency() == null ? currency.getSymbol() : LDR.getPayment().getCurrency().getSymbol()) : receipt.getCurrency().getSymbol());
                    jObj.put("currencysymbolpayment", LDR.getPayment().getCurrency().getSymbol());
                    jArray.put(jObj);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return jArray;
    }
    
    public ModelAndView exportPayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();      
        List<JSONObject> list = new ArrayList<JSONObject>();
        List<JSONObject> tempList = new ArrayList<JSONObject>();
        int limitValue = 0, startValue = 0, dataCount = 0;
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = getPaymentMap(request);                         
            if (!StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
                requestParams.put("ss", StringUtil.DecodeText(request.getParameter("ss")));
            }
            /*
             When check(Drop Down) to include child accounts is disabled then includeExcludeChildCombobox flag will be set as TRUE to include child accounts
             
              includeExcludeChildCombobox, if All = Include all child accounts while fetching parent account data
              includeExcludeChildCombobox, if TRUE = Include all child accounts while fetching parent account data
              includeExcludeChildCombobox, if FALSE = Exclude child acounts while fetching parent account data
             
            */
            boolean includeExcludeChildCmb;
            if (request.getParameter("includeExcludeChildCmb") != null && request.getParameter("includeExcludeChildCmb").toString().equals("All")) {
                includeExcludeChildCmb = true;
            } else {
                includeExcludeChildCmb = request.getParameter("includeExcludeChildCmb") != null ? Boolean.parseBoolean(request.getParameter("includeExcludeChildCmb")) : false;
            }
            /*
             
             fetch payment report value from request or set defalut value false
             
             */
            boolean isPaymentReport = (request.getParameter("isPaymentReport") != null) ? Boolean.parseBoolean(request.getParameter("isPaymentReport").toString()) : false;
            /*   
            
            get Vendor id 
            
            */
            String Vendorid = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("custVendorID"))) {
                Vendorid = StringUtil.decodeString((String) request.getParameter("custVendorID"));
            }
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            boolean contraentryflag = request.getParameter("contraentryflag") != null;
            boolean isAdvancePayment = request.getParameter("advancePayment") != null;
            boolean isAdvanceToCustomer = request.getParameter("advanceToCustomer") != null;
            boolean isPostDatedCheque = request.getParameter("isPostDatedCheque") != null;
            boolean isDishonouredCheque = request.getParameter("isDishonouredCheque") != null;
            boolean isGlcode = request.getParameter("isGlcode") != null;
            boolean exportPtw = request.getParameter("exportPtw") != null;
//          boolean ispendingApproval = request.getParameter("ispendingAproval") != null?request.getParameter("ispendingAproval")!=""?true:false:false;            
            boolean ispendingApproval=false; 
            if (!StringUtil.isNullOrEmpty(request.getParameter("ispendingAproval"))) {
                ispendingApproval =  Boolean.parseBoolean(request.getParameter("ispendingAproval"));
            }          
            String fileType = request.getParameter("filetype");
            String paymentWindowType = "";
            requestParams.put("isPaymentReport", isPaymentReport);
            requestParams.put("includeExcludeChildCmb",includeExcludeChildCmb);    
            requestParams.put("custVendorID", Vendorid);
            requestParams.put("contraentryflag", contraentryflag);
            requestParams.put("isadvancepayment", isAdvancePayment);
            requestParams.put("isadvancetocustomer", isAdvanceToCustomer);
            requestParams.put("isPostDatedCheque", isPostDatedCheque);
            requestParams.put("isDishonouredCheque", isDishonouredCheque);
            requestParams.put("isGlcode", isGlcode);
            requestParams.put("fileType", fileType);
            requestParams.put("isExport", true);
            requestParams.put("ispendingAproval",ispendingApproval);
            
            boolean onlyOpeningBalanceTransactionsFlag = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("onlyOpeningBalanceTransactionsFlag"))) {
                onlyOpeningBalanceTransactionsFlag = Boolean.parseBoolean(request.getParameter("onlyOpeningBalanceTransactionsFlag"));
            }
            
            boolean allAdvPayment = request.getParameter("allAdvPayment") != null;
            boolean unUtilizedAdvPayment = request.getParameter("unUtilizedAdvPayment") != null;
            boolean partiallyUtilizedAdvPayment = request.getParameter("partiallyUtilizedAdvPayment") != null;
            boolean fullyUtilizedAdvPayment = request.getParameter("fullyUtilizedAdvPayment") != null;
            boolean nonorpartiallyUtilizedAdvPayment = request.getParameter("nonorpartiallyUtilizedAdvPayment") != null;

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
            if (!StringUtil.isNullOrEmpty(request.getParameter("nonorpartiallyUtilizedAdvPayment"))) {
                nonorpartiallyUtilizedAdvPayment = Boolean.parseBoolean(request.getParameter("nonorpartiallyUtilizedAdvPayment"));
            }

            requestParams.put("allAdvPayment", allAdvPayment);
            requestParams.put("unUtilizedAdvPayment", unUtilizedAdvPayment);
            requestParams.put("partiallyUtilizedAdvPayment", partiallyUtilizedAdvPayment);
            requestParams.put("fullyUtilizedAdvPayment", fullyUtilizedAdvPayment);
            requestParams.put("nonorpartiallyUtilizedAdvPayment", nonorpartiallyUtilizedAdvPayment);
            
            
                if (!StringUtil.isNullOrEmpty(request.getParameter("paymentWindowType"))) {
                    requestParams.put("paymentWindowType", Integer.parseInt(request.getParameter("paymentWindowType")));
                }
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            boolean eliminateflag = consolidateFlag;
            requestParams.put(Constants.start, "");
            requestParams.put(Constants.limit, "");
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(),  sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            if (extraCompanyPreferences != null && extraCompanyPreferences.isEnablesalespersonAgentFlow()) {
                int permCode = sessionHandlerImpl.getPerms(request, Constants.VENDOR_PERMCODE);
                if (!((permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE is true then user has permission to view all vendors documents,so at that time there is need to filter record according to user&agent. 
                     */
                    String userId = sessionHandlerImpl.getUserid(request);
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraCompanyPreferences.isEnablesalespersonAgentFlow());
                }
            }
            KwlReturnObject result = null;
            KwlReturnObject billingResult = null;
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                request.setAttribute("isExport", true);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);

                tempList.clear();
                if (onlyOpeningBalanceTransactionsFlag) {    //To export opening balance records (To Print Data in PDF, CSV format)
                    result = accVendorPaymentDAO.getAllOpeningBalancePayments(requestParams);
                    JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
//                    tempList = accVendorPaymentModuleServiceObj.getOpeningBalanceReceiptJsonForReport(request, result.getEntityList(), tempList);
                    tempList = accVendorPaymentModuleServiceObj.getOpeningBalanceReceiptJsonForReport(paramJobj, result.getEntityList(), tempList);
                } else {
                    result = accVendorPaymentDAO.getPayments(requestParams);
                    tempList = accVendorPaymentModuleServiceObj.getPaymentsJson(requestParams, result.getEntityList(), tempList);
                }
                list.addAll(tempList); 
            }
         
            limitValue = list.size();
            startValue = 0;
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
            if (request.getParameter("type") != null && request.getParameter("type").equals("detailedXls")) {
                jArr = getDetailExcelJsonMakePayment(request, response, requestParams, jArr);
            }
            jobj.put("data", jArr);
            jobj.put("count", list.size());
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }        
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
     /*
     * Payment Export Detail View
     */
    public JSONArray getDetailExcelJsonMakePayment(HttpServletRequest request, HttpServletResponse response, HashMap<String, Object> requestParams, JSONArray DataJArr) throws JSONException, SessionExpiredException, ServiceException, SessionExpiredException, SessionExpiredException {
        JSONArray tempArray = new JSONArray();
        for (int rec = 0; rec < DataJArr.length(); rec++) {
            JSONObject rowjobj = new JSONObject();
            rowjobj = DataJArr.getJSONObject(rec);
            String billid = rowjobj.optString("billid", "");   //Payment ID 
            request.setAttribute("billid", billid);
            request.setAttribute("isExport", true);
            request.setAttribute("isForReport", true);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap paramsFromRequest = StringUtil.convertRequestToMapObject(request);
            paramsFromRequest.put("companyid", companyid);
            String dateFormat=paramsFromRequest.get(Constants.userdateformat)!=null?paramsFromRequest.get(Constants.userdateformat).toString():"";
            if (!StringUtil.isNullOrEmpty(dateFormat)) {
                DateFormat df = new SimpleDateFormat(dateFormat);
                paramsFromRequest.remove(Constants.userdateformat);
                paramsFromRequest.put(Constants.userdateformat,df);
            }
            paramsFromRequest.put("dateformat", authHandler.getOnlyDateFormat(request));
            
            JSONArray dataarrayobj = accVendorPaymentServiceDAOobj.getPaymentDetailJsonNew(paramsFromRequest);
            
            JSONArray DataRowsArr = dataarrayobj;//dataarrayobj.getJSONArray("data");
            rowjobj.put("type", "");
            tempArray.put(rowjobj);
            for (int dataRow = 0; dataRow < DataRowsArr.length(); dataRow++) {
                int accDetailCount = 1;
                int accDebitCount = 1;
                int accAdvanceCount = 1;
                int accFundCount = 1;
                JSONObject tempjobj = new JSONObject();
                for (int row = 0; row < DataRowsArr.getJSONObject(dataRow).getJSONArray("typedata").length(); row++) {
                    tempjobj = DataRowsArr.getJSONObject(dataRow).getJSONArray("typedata").getJSONObject(row);
                    exportDaoObj.editJsonKeyForExcelFile(tempjobj, Constants.Acc_Make_Payment_ModuleId);
                    if (tempjobj.has("type") && !StringUtil.isNullOrEmpty(tempjobj.optString("type", ""))) {
                        int type = tempjobj.optInt("type");
                        boolean isrefund = tempjobj.optBoolean("isrefund");
                        if (type == 4) {
                            tempjobj.put("srnoforaccount", accDetailCount++);
                        } else if (type == 3) {
                            tempjobj.put("srnofordebitnote", accDebitCount++);

                        } else if (type == 1) {
                            if (!isrefund) {
                                tempjobj.put("srnoforadvance", accAdvanceCount++);
                            } else {
                                tempjobj.put("srnoforrefund", accFundCount++);
                            }
                        }
                    } else {
                        tempjobj.put("amountinbase", "");
                    }
                    tempArray.put(tempjobj);
                }
            }
        }
        return tempArray;
    }
}
