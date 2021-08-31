
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

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;

import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.debitnote.accDebitNoteController;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteService;
import com.krawler.spring.accounting.goodsreceipt.AccGoodsReceiptServiceDAO;
import com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptControllerCMN;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.handler.CommonEnglishNumberToWords;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.accounting.invoice.*;
import com.krawler.spring.accounting.jasperreports.*;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.reports.AccScriptController;
import com.krawler.spring.accounting.salesorder.AccSalesOrderServiceDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendorpayment.AccVendorPaymentServiceDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentControllerCMN;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
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
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.*;
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
public class accReceiptControllerCMN extends MultiActionController implements MessageSourceAware{
    private accReceiptDAO accReceiptDAOobj;
    private AccReceiptServiceDAO accReceiptServiceDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private AccExportReportsServiceDAO accExportReportsServiceDAOobj;
    private accVendorPaymentDAO accVendorPaymentDAO;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accTaxDAO accTaxObj;
    private accInvoiceCMN accInvoiceCommon;
    private String successView;
    private accAccountDAO accAccountDAOobj;
    private CustomDesignDAO customDesignDAOObj;
    private VelocityEngine velocityEngine;
    private MessageSource messageSource;
    private authHandlerDAO authHandlerDAOObj;
    private accJournalEntryDAO accJournalEntryobj;
    private AccMainAccountingService accMainAccountingService;
    private CommonEnglishNumberToWords EnglishNumberToWordsOjb = new CommonEnglishNumberToWords();
    private accCurrencyDAO accCurrencyDAOobj;
    private AccCommonTablesDAO accCommonTablesDAO; 
    private accBankReconciliationDAO accBankReconciliationObj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accDebitNoteDAO accDebitNoteobj;
    private AccInvoiceServiceDAO accInvoiceServiceDAOObj;
    private AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accDebitNoteService accDebitNoteService;
    private AccVendorPaymentServiceDAO accVendorPaymentServiceDAOobj;
    private AccSalesOrderServiceDAO accSalesOrderServiceDAOobj;

    public void setAccSalesOrderServiceDAOobj(AccSalesOrderServiceDAO accSalesOrderServiceDAOobj) {
        this.accSalesOrderServiceDAOobj = accSalesOrderServiceDAOobj;
    }
    
    public void setAccInvoiceServiceDAOObj(AccInvoiceServiceDAO accInvoiceServiceDAOObj) {
        this.accInvoiceServiceDAOObj = accInvoiceServiceDAOObj;
    }

    public void setAccGoodsReceiptServiceDAOObj(AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj) {
        this.accGoodsReceiptServiceDAOObj = accGoodsReceiptServiceDAOObj;
    }
    
    
     
     public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    } 
    public void setaccExportReportsServiceDAO(AccExportReportsServiceDAO accExportReportsServiceDAOobj) {
        this.accExportReportsServiceDAOobj = accExportReportsServiceDAOobj;
    }
    public void setAccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentDAO) {
        this.accVendorPaymentDAO = accVendorPaymentDAO;
    }
    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
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
    public void setAccMainAccountingService(AccMainAccountingService accMainAccountingService) {
        this.accMainAccountingService = accMainAccountingService;
    }
    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
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
    public void setMessageSource(MessageSource msg) {
            this.messageSource = msg;
    }
    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
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
    
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setAccReceiptServiceDAO(AccReceiptServiceDAO accReceiptServiceDAOobj) {
        this.accReceiptServiceDAOobj = accReceiptServiceDAOobj;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    
    public void setaccDebitNoteService(accDebitNoteService accDebitNoteService) {
        this.accDebitNoteService = accDebitNoteService;
    }
    
    public void setaccVendorPaymentServiceDAO(AccVendorPaymentServiceDAO accVendorPaymentServiceDAOobj) {
        this.accVendorPaymentServiceDAOobj = accVendorPaymentServiceDAOobj;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public ModelAndView getReceipts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        List<JSONObject> list = new ArrayList<JSONObject>();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            int permCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
            paramJobj.put("permCode", permCode);
            KwlReturnObject result =  accReceiptServiceDAOobj.getReceiptList(paramJobj);
            list = result.getEntityList();
            int count = result.getRecordTotalCount();
            for (JSONObject jSONObject : list) {
                jArr.put(jSONObject);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
            issuccess = true;
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView exportF1ReceiptReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        List<JSONObject> tempList = new ArrayList<JSONObject>();
        Map<String, Object> voucherMap = new HashMap<String, Object>();

        int templateflag = Integer.parseInt(request.getParameter("templateflag"));
        int mode = Integer.parseInt(request.getParameter("mode"));
        boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            
            int permCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
            paramJobj.put("permCode", permCode);
            
            KwlReturnObject result =  accReceiptServiceDAOobj.getReceiptList(paramJobj);
            tempList = result.getEntityList();
            ArrayList<ReceiptReportJasper> pojoList = new ArrayList<ReceiptReportJasper>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            KwlReturnObject compAccPrefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) compAccPrefResult.getEntityList().get(0);
            double amountinbasecurr =0;
            for (JSONObject arrlist : tempList) {
                String tempBillid = arrlist.optString("billid", "");
                String billno = arrlist.optString("billno", "");
                String billdateinUserFormat = arrlist.optString("billdateinUserFormat", "");
                String personcode = arrlist.optString("personcode", "");
                String personname = arrlist.optString("personname", "");
                String paymentmethod = arrlist.optString("paymentmethod", "");
                String chequenumber = arrlist.optString("chequenumber", "");
                String amount = arrlist.optString("currencycode", "") + " " + authHandler.formattedCommaSeparatedAmount(Double.parseDouble(arrlist.optString("amount", "0")), companyid);
                String amountinbase = company.getCurrency().getCurrencyCode() + " " + authHandler.formattedCommaSeparatedAmount(Double.parseDouble(arrlist.optString("amountinbase", "0")), companyid);
                amountinbasecurr += Double.parseDouble(arrlist.optString("amountinbase", "0"));
                int paymentwindowtype = (Integer) arrlist.get("paymentwindowtype");

                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                request.setAttribute("bills", tempBillid);
                
                paramJobj.put("companyid", companyid);
                paramJobj.put("gcurrencyid", gcurrencyid);
                paramJobj.put("bills", tempBillid);
                
                JSONObject tempobj = accReceiptServiceDAOobj.getReceiptRowsJSONNew(paramJobj);
                JSONArray DataJArr = tempobj.getJSONArray("data");
                for (int i = 0; i < DataJArr.length(); i++) {
                    jobj = DataJArr.getJSONObject(i);
                    int detailtype = (Integer) jobj.get("type");
                    if (detailtype == Constants.PaymentAgainstInvoice) {
                        JSONArray arr = jobj.getJSONArray("typedata");
                        for (int j = 0; j < arr.length(); j++) {
                            ReceiptReportJasper pojo = new ReceiptReportJasper();
                            JSONObject data = arr.getJSONObject(j);
                            String invDesc = "";
                            if (templateflag == Constants.F1Recreation_templateflag || templateflag == Constants.F1RecreationLeasing_templateflag) {
//                                invDesc = data.optString("description", "").equals("") ? "" : data.optString("description", "");
                                invDesc = data.optString("transectionno", "");
                            } else {
                                invDesc = "Against Invoice# " + data.getString("transectionno") + " dated " + data.getString("creationdateinuserformat");
                                if (data.getString("totalamount").equals(data.getString("amountpaid"))) {
                                    invDesc += " (Full)";
                                } else {
                                    invDesc += " (Part)";
                                }
                            }

                            pojo.setDate(billdateinUserFormat);
                            pojo.setVoucherno(billno);
                            pojo.setCode(personcode);
                            pojo.setName(personname);
                            pojo.setPaymentmethod(paymentmethod);
                            pojo.setChequenumber(chequenumber);
                            pojo.setDocnumber(invDesc); //enteramount- in payment currency
                            pojo.setAmount(authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("enteramount", "0.0")), companyid)); //enteramount- in payment currency
                            double baseamount = 0;
                            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(paramJobj);
                            if (arrlist.getString("currencyid") != null) {
                                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, Double.parseDouble(data.optString("enteramount", "0.0")), arrlist.getString("currencyid"), authHandler.getDateOnlyFormat(request).parse(arrlist.optString("billdate")), 0);
                                baseamount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            }
                            pojo.setAmountinbase(authHandler.formattedCommaSeparatedAmount(baseamount, companyid)); 
                            pojo.setTotalamount(amount);
                            pojo.setTotalamountinbase(amountinbase);
                            pojo.setTotalamountinbasecurr(company.getCurrency().getCurrencyCode() + " " + authHandler.formattedCommaSeparatedAmount(amountinbasecurr, companyid));
                            pojoList.add(pojo);
                        }
                    } else if (detailtype == Constants.GLPayment) {
                        JSONArray arr = jobj.getJSONArray("typedata");
                        for (int j = 0; j < arr.length(); j++) {
                            ReceiptReportJasper pojo = new ReceiptReportJasper();
                            JSONObject data = arr.getJSONObject(j);
                            String taxname = "";
                            String taxpercent = "";
                            String invDesc = "";
                            if (templateflag == Constants.F1Recreation_templateflag || templateflag == Constants.F1RecreationLeasing_templateflag) {
                                invDesc = data.optString("description", "").equals("") ? "" : data.optString("description", "");
                            } else {
                                invDesc = "Against GL Account " + (data.optString("accountcode", "").equals("") ? "" : data.optString("accountcode", "") + " - ") + data.getString("accountname");
                            }
                            if (!data.optString("taxamount", "0.0").equals("0.0")) {
                                taxpercent = data.optString("taxpercent", "");
                                taxname = (data.optString("taxcode", "").equals("") ? "" : data.optString("taxcode", "") + " - ") + data.optString("taxname", "") + (taxpercent.equals("") ? "" : " - (" + taxpercent + "%)");

                                pojo.setDate(billdateinUserFormat);
                                pojo.setVoucherno(billno);
                                pojo.setCode(personcode);
                                pojo.setName(personname);
                                pojo.setPaymentmethod(paymentmethod);
                                pojo.setChequenumber(chequenumber);
                                pojo.setDocnumber(invDesc); //enteramount- in payment currency
                                pojo.setAmount(authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("totalamount", "0.0")), companyid)); //enteramount- in payment currency
                                double baseamount = 0;
                                HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                                if (arrlist.getString("currencyid") != null) {
                                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, Double.parseDouble(data.optString("totalamount", "0.0")), arrlist.getString("currencyid"), authHandler.getDateOnlyFormat(request).parse(arrlist.optString("billdate")), 0);
                                    baseamount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                                }
                                pojo.setAmountinbase(authHandler.formattedCommaSeparatedAmount(baseamount, companyid));
                                pojo.setTotalamount(amount);
                                pojo.setTotalamountinbase(amountinbase);
                                pojo.setTotalamountinbasecurr(company.getCurrency().getCurrencyCode() + " " + authHandler.formattedCommaSeparatedAmount(amountinbasecurr, companyid));
                                pojoList.add(pojo);

                                pojo = new ReceiptReportJasper();
                                pojo.setDate(billdateinUserFormat);
                                pojo.setVoucherno(billno);
                                pojo.setCode(personcode);
                                pojo.setName(personname);
                                pojo.setPaymentmethod(paymentmethod);
                                pojo.setChequenumber(chequenumber);
                                pojo.setDocnumber("&nbsp&nbsp&nbsp&nbsp " + taxname); //enteramount- in payment currency
                                pojo.setAmount(authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("taxamount", "0.0")), companyid)); //enteramount- in payment currency
                                baseamount = 0;
                                requestParams = AccountingManager.getGlobalParams(request);
                                if (arrlist.getString("currencyid") != null) {
                                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, Double.parseDouble(data.optString("taxamount", "0.0")), arrlist.getString("currencyid"), authHandler.getDateOnlyFormat(request).parse(arrlist.optString("billdate")), 0);
                                    baseamount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                                }
                                pojo.setAmountinbase(authHandler.formattedCommaSeparatedAmount(baseamount, companyid));
                                pojo.setTotalamount(amount);
                                pojo.setTotalamountinbase(amountinbase);
                                pojo.setTotalamountinbasecurr(company.getCurrency().getCurrencyCode() + " " + authHandler.formattedCommaSeparatedAmount(amountinbasecurr, companyid));
                                pojoList.add(pojo);
                            } else {
                                pojo.setDate(billdateinUserFormat);
                                pojo.setVoucherno(billno);
                                pojo.setCode(personcode);
                                pojo.setName(personname);
                                pojo.setPaymentmethod(paymentmethod);
                                pojo.setChequenumber(chequenumber);
                                pojo.setDocnumber(invDesc); //enteramount- in payment currency
                                pojo.setAmount(authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("totalamount", "0.0")), companyid)); //enteramount- in payment currency
                                double baseamount = 0;
                                HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                                if (arrlist.getString("currencyid") != null) {
                                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, Double.parseDouble(data.optString("totalamount", "0.0")), arrlist.getString("currencyid"), authHandler.getDateOnlyFormat(request).parse(arrlist.optString("billdate")), 0);
                                    baseamount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                                }
                                pojo.setAmountinbase(authHandler.formattedCommaSeparatedAmount(baseamount, companyid));
                                pojo.setTotalamount(amount);
                                pojo.setTotalamountinbase(amountinbase);
                                pojo.setTotalamountinbasecurr(company.getCurrency().getCurrencyCode() + " " + authHandler.formattedCommaSeparatedAmount(amountinbasecurr, companyid));
                                pojoList.add(pojo);
                            }
                        }
                    } else if (detailtype == Constants.PaymentAgainstCNDN) {
                        JSONArray arr = jobj.getJSONArray("typedata");
                        for (int j = 0; j < arr.length(); j++) {
                            ReceiptReportJasper pojo = new ReceiptReportJasper();
                            JSONObject data = arr.getJSONObject(j);
                            String invDesc = mode == StaticValues.AUTONUM_PAYMENT ? "Against Credit Note# " : "Against Debit Note# ";
                            invDesc += data.getString("transectionno") + " dated " + data.getString("creationdate");
                            if (data.getString("totalamount").equals(data.getString("cnpaidamount"))) {
                                invDesc += " (Full)";
                            }

                            pojo.setDate(billdateinUserFormat);
                            pojo.setVoucherno(billno);
                            pojo.setCode(personcode);
                            pojo.setName(personname);
                            pojo.setPaymentmethod(paymentmethod);
                            pojo.setChequenumber(chequenumber);
                            pojo.setDocnumber(invDesc); //enteramount- in payment currency
                            pojo.setAmount(authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("enteramount", "0.0")), companyid)); //enteramount- in payment currency
                            double baseamount = 0;
                            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                            if (arrlist.getString("currencyid") != null) {
                                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, Double.parseDouble(data.optString("enteramount", "0.0")), arrlist.getString("currencyid"), authHandler.getDateOnlyFormat(request).parse(arrlist.optString("billdate")), 0);
                                baseamount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            }
                            pojo.setAmountinbase(authHandler.formattedCommaSeparatedAmount(baseamount, companyid));
                            pojo.setTotalamount(amount);
                            pojo.setTotalamountinbase(amountinbase);
                            pojo.setTotalamountinbasecurr(company.getCurrency().getCurrencyCode() + " " + authHandler.formattedCommaSeparatedAmount(amountinbasecurr, companyid));
                            pojoList.add(pojo);
                        }
                    } else if (detailtype == Constants.AdvancePayment) {
                        JSONArray arr = jobj.getJSONArray("typedata");
                        for (int j = 0; j < arr.length(); j++) {
                            ReceiptReportJasper pojo = new ReceiptReportJasper();
                            JSONObject data = arr.getJSONObject(j);
                            String invDesc = "Advance Amount ";
                            if (data.optBoolean("isrefund", false)) {
                                if ((mode == StaticValues.AUTONUM_PAYMENT && paymentwindowtype == Constants.Make_Payment_to_Vendor) || (mode == StaticValues.AUTONUM_RECEIPT && paymentwindowtype == Constants.Receive_Payment_from_Vendor)) {
                                    invDesc = "Refund/ Deposit Amount against Payment #" + data.optString("transectionno", "");
                                } else if ((mode == StaticValues.AUTONUM_PAYMENT && paymentwindowtype == Constants.Make_Payment_to_Customer) || (mode == StaticValues.AUTONUM_RECEIPT && paymentwindowtype == Constants.Receive_Payment_from_Customer)) {
                                    invDesc = "Refund/ Deposit Amount against Receipt #" + data.optString("transectionno", "");
                                }
                            }
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

                            pojo.setDate(billdateinUserFormat);
                            pojo.setVoucherno(billno);
                            pojo.setCode(personcode);
                            pojo.setName(personname);
                            pojo.setPaymentmethod(paymentmethod);
                            pojo.setChequenumber(chequenumber);
                            pojo.setDocnumber(invDesc); //enteramount- in payment currency
                            pojo.setAmount(authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("paidamount", "0.0")), companyid)); //enteramount- in payment currency
                            double baseamount = 0;
                            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                            if (arrlist.getString("currencyid") != null) {
                                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, Double.parseDouble(data.optString("paidamount", "0.0")), arrlist.getString("currencyid"), authHandler.getDateOnlyFormat(request).parse(arrlist.optString("billdate")), 0);
                                baseamount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            }
                            pojo.setAmountinbase(authHandler.formattedCommaSeparatedAmount(baseamount, companyid));
                            pojo.setTotalamount(amount);
                            pojo.setTotalamountinbase(amountinbase);
                            pojo.setTotalamountinbasecurr(company.getCurrency().getCurrencyCode() + " " + authHandler.formattedCommaSeparatedAmount(amountinbasecurr, companyid));
                            pojoList.add(pojo);
                        }
                    } else if (detailtype == Constants.AdvanceLinkedWithInvoicePayment) {
                        JSONArray arr = jobj.getJSONArray("typedata");
                        for (int j = 0; j < arr.length(); j++) {
                            ReceiptReportJasper pojo = new ReceiptReportJasper();
                            JSONObject data = arr.getJSONObject(j);
                            String invDesc = "&nbsp&nbsp&nbsp&nbsp Adjusted Against Invoice# " + data.getString("transectionno") + " dated " + data.getString("creationdateinuserformat");
                            if (data.getString("totalamount").equals(data.getString("amountpaid"))) {
                                invDesc += " (Full)";
                            }

                            pojo.setDate(billdateinUserFormat);
                            pojo.setVoucherno(billno);
                            pojo.setCode(personcode);
                            pojo.setName(personname);
                            pojo.setPaymentmethod(paymentmethod);
                            pojo.setChequenumber(chequenumber);
                            pojo.setDocnumber(invDesc); //enteramount- in payment currency
                            pojo.setAmount(authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("enteramount", "0.0")), companyid)); //enteramount- in payment currency
                            double baseamount = 0;
                            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                            if (arrlist.getString("currencyid") != null) {
                                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, Double.parseDouble(data.optString("enteramount", "0.0")), arrlist.getString("currencyid"), authHandler.getDateOnlyFormat(request).parse(arrlist.optString("billdate")), 0);
                                baseamount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            }
                            pojo.setAmountinbase(authHandler.formattedCommaSeparatedAmount(baseamount, companyid));
                            pojo.setTotalamount(amount);
                            pojo.setTotalamountinbase(amountinbase);
                            pojo.setTotalamountinbasecurr(company.getCurrency().getCurrencyCode() + " " + authHandler.formattedCommaSeparatedAmount(amountinbasecurr, companyid));
                            pojoList.add(pojo);
                        }
                    }
                }
            }
            
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat ed = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss a");
            String endDate1 = df.format(ed.parse(request.getParameter("enddate")));
            String startDate1 = df.format(authHandler.getConstantDateFormatter(request).parse(request.getParameter("stdate")));
            String date1 = " " + startDate1 + " To " + endDate1;
            voucherMap.put("BankBookDateRange", date1);

            String fiscalstart = df.format(companyAccountPreferences.getFinancialYearFrom());
            Calendar c1 = Calendar.getInstance();
            c1.setTime(df.parse(fiscalstart));
            c1.add(Calendar.YEAR, 1); // number of years to add
            c1.add(Calendar.DATE, -1);
            String fiscalend = df.format(c1.getTime());
            DateFormat time = new SimpleDateFormat("HH:mm:ss a");
            Date currentDate=new Date();
            voucherMap.put("currentTime", time.format(currentDate));
            voucherMap.put("currentDate", df.format(currentDate));
            String accPeriod = "<b>Accounting Period</b><br>" + fiscalstart + " - " + fiscalend;
            voucherMap.put("CompanyAccountingPeriod", accPeriod);

            voucherMap.put("CompanyName", company.getCompanyName());
            voucherMap.put("CompanyAdd", AccountingAddressManager.getCompanyDefaultBillingAddress(companyid, accountingHandlerDAOobj));
            voucherMap.put("title", "RECEIPT REGISTER");
            
            response.setHeader("Content-Disposition", "attachment;filename=" + "ReceiptRegister.pdf");

            InputStream inputStream = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + "/F1RecreationReceiptReport.jrxml");
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(pojoList);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, voucherMap, beanColDataSource);
           
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exporter.exportReport();

        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("", "", jobj.toString());
    }

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
                Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
            return result;
        }
    }

    public ModelAndView exportReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        List<JSONObject> list = new ArrayList<JSONObject>();
        List<JSONObject> tempList = new ArrayList<JSONObject>();
        int limitValue = 0, startValue = 0, dataCount = 0;
        String view = "jsonView_ex";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            HashMap<String, Object> requestParams = getReceiptRequestMapJSON(paramJobj);
            if (requestParams.containsKey("ss") && !StringUtil.isNullOrEmpty((String)requestParams.get("ss"))) {
                requestParams.put("ss", StringUtil.DecodeText(requestParams.get("ss").toString()));
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
            
            get Customer id 
            
            */
            String Customerid = "";
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("custVendorID"))) {
                Customerid = StringUtil.decodeString((String) paramJobj.optString("custVendorID"));
            }
            boolean contraentryflag = request.getParameter("contraentryflag") != null;
            boolean isAdvancePayment = request.getParameter("advancePayment") != null;
            boolean isAdvanceFromVendor = request.getParameter("advanceFromVendor") != null;
            boolean isPostDatedCheque = request.getParameter("isPostDatedCheque") != null;
            boolean isDishonouredCheque = request.getParameter("isDishonouredCheque") != null;
            boolean isGlcode = request.getParameter("isGlcode") != null;
//            boolean ispendingApproval = request.getParameter("ispendingAproval") != null?request.getParameter("ispendingAproval")!=""?true:false:false;            
            boolean ispendingApproval=false; 
            if (!StringUtil.isNullOrEmpty(request.getParameter("ispendingAproval"))) {
                ispendingApproval =  Boolean.parseBoolean(request.getParameter("ispendingAproval"));
            }
            String selectedIds = request.getParameter("billid") != null ? request.getParameter("billid") : "";
            requestParams.put("billid", selectedIds);
            requestParams.put("contraentryflag", contraentryflag);
            requestParams.put("isadvancepayment", isAdvancePayment);
            requestParams.put("isadvancefromvendor", isAdvanceFromVendor);
            requestParams.put("isPostDatedCheque", isPostDatedCheque);
            requestParams.put("isDishonouredCheque", isDishonouredCheque);
            requestParams.put("isGlcode", isGlcode);
            requestParams.put("ispendingAproval",ispendingApproval);
            requestParams.put("includeExcludeChildCmb", includeExcludeChildCmb);
            requestParams.put("isPaymentReport",isPaymentReport);
            requestParams.put("custVendorID",Customerid);
            
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
            if (!StringUtil.isNullOrEmpty(request.getParameter("paymentWindowType"))) {
                requestParams.put("paymentWindowType", Integer.parseInt(paramJobj.optString("paymentWindowType")));
            }

            requestParams.put("allAdvPayment", allAdvPayment);
            requestParams.put("unUtilizedAdvPayment", unUtilizedAdvPayment);
            requestParams.put("partiallyUtilizedAdvPayment", partiallyUtilizedAdvPayment);
            requestParams.put("fullyUtilizedAdvPayment", fullyUtilizedAdvPayment);
            requestParams.put("nonorpartiallyUtilizedAdvPayment", nonorpartiallyUtilizedAdvPayment);
            
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            requestParams.put(Constants.start, "");
            requestParams.put(Constants.limit, "");
            KwlReturnObject result = null;
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                request.setAttribute("isExport", true);
                paramJobj.put("companyid", companyid);
                paramJobj.put("gcurrencyid", gcurrencyid);
                paramJobj.put("isExport", true);
                
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);

                tempList.clear();
                if (onlyOpeningBalanceTransactionsFlag) {         //To export opening balance records (To Print Data in PDF, CSV format)
                    result = accReceiptDAOobj.getAllOpeningBalanceReceipts(requestParams);
                    
//                    tempList = accReceiptServiceDAOobj.getOpeningBalanceReceiptJsonForReport(request, result.getEntityList(), tempList);
                    tempList = accReceiptServiceDAOobj.getOpeningBalanceReceiptJsonForReport(paramJobj, result.getEntityList(), tempList);
                } else {
                    result = accReceiptDAOobj.getReceipts(requestParams);
//                    tempList = accReceiptServiceDAOobj.getReceiptJson(request, result.getEntityList(), tempList);
                    int permCode=sessionHandlerImpl.getPerms(request,Constants.CUSTOMER_PERMCODE);
                    paramJobj.put("permCode", permCode);
                    tempList = accReceiptServiceDAOobj.getReceiptJson(paramJobj, result.getEntityList(), tempList);
                }
                Collections.sort(tempList, Collections.reverseOrder(new ReceiptDateComparator()));
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
                jArr = getDetailExcelJsonPaymentReceived(request, response, requestParams, jArr);
            }
            jobj.put("data", jArr);
            jobj.put("count", list.size());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    /*
     * Payment Export Detail View
     */
    public JSONArray getDetailExcelJsonPaymentReceived(HttpServletRequest request, HttpServletResponse response, HashMap<String, Object> requestParams, JSONArray DataJArr) throws JSONException, SessionExpiredException, ServiceException, SessionExpiredException, SessionExpiredException {
        JSONArray tempArray = new JSONArray();
        for (int rec = 0; rec < DataJArr.length(); rec++) {
            JSONObject rowjobj = new JSONObject();
            rowjobj = DataJArr.getJSONObject(rec);
            String billid = rowjobj.optString("billid", "");   //Invoice ID 
            request.setAttribute("billid", billid);
            request.setAttribute("isExport", true);
            request.setAttribute("isForReport", true);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("companyid", companyid);
            JSONObject dataarrayobj = accReceiptServiceDAOobj.getReceiptRowsJSONNew(paramJobj);
            
            JSONArray DataRowsArr = dataarrayobj.getJSONArray("data");
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
                    exportDaoObj.editJsonKeyForExcelFile(tempjobj, Constants.Acc_Receive_Payment_ModuleId);
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


    public ModelAndView getReceiptRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        boolean issuccess = false;
        String msg = "";
		try {
            jobj = getReceiptRowsJSON(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try{
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getReceiptRowsNew(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        boolean issuccess = false;
        String msg = "";
		try {
                    String billId=request.getParameter("bills");
                    request.setAttribute("bills", billId);
                    String companyid = sessionHandlerImpl.getCompanyid(request);
                    JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
                    paramJobj.put("companyid", companyid);
                    jobj = accReceiptServiceDAOobj.getReceiptRowsJSONNew(paramJobj);
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try{
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public static HashMap<String, Object> getReceiptRequestMapJSON(JSONObject paramJobj) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(paramJobj);
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
        return requestParams;
    }
    
    private JSONObject getReceiptRowsJSON(HttpServletRequest request) throws SessionExpiredException, ServiceException {
    JSONObject jobj=new JSONObject();
    try {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.optString(Constants.globalCurrencyKey));
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        double taxPercent=0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        DateFormat df = authHandler.getDateOnlyFormat();
        String[] receipt = null;
        if(!StringUtil.isNullOrEmpty(request.getParameter("bills"))){
            receipt =request.getParameterValues("bills");
        }
        boolean isForReport = false;
        String dtype =  paramJobj.optString("dtype",null);
        if (!StringUtil.isNullOrEmpty(dtype) && dtype.equals("report")) {
            isForReport = true;
        }
        boolean isReceiptEdit = Boolean.parseBoolean(paramJobj.optString("isReceiptEdit"));
        int i = 0;
        
        HashMap requestParams = getReceiptRequestMapJSON(paramJobj);
        requestParams.put("currencyfilterfortrans", (paramJobj.optString("currencyfilterfortrans",null) == null)? "" : paramJobj.optString("currencyfilterfortrans"));
        HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        filter_names.add("receipt.ID");
        order_by.add("srno");
        order_type.add("asc");
        rRequestParams.put("filter_names", filter_names);
        rRequestParams.put("filter_params", filter_params);
        rRequestParams.put("order_by", order_by);
        rRequestParams.put("order_type", order_type);

        JSONArray jArr = new JSONArray();
        while (receipt != null && i < receipt.length) {
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receipt[i]);
            Receipt re = (Receipt) result.getEntityList().get(0);
            filter_params.clear();
            filter_params.add(re.getID());
            KwlReturnObject grdresult = accReceiptDAOobj.getReceiptDetails(rRequestParams);
            Iterator itr = grdresult.getEntityList().iterator();

            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(re.getCompany().getCompanyID(), Constants.Acc_Receive_Payment_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
            
            while (itr.hasNext()) {
                    ReceiptDetail row = (ReceiptDetail) itr.next();
                    
                    Date invoiceCreationDate = null;
                    double invoiceAmount = 0d;
                    double externalCurrencyRate = 0d;
                    
                    boolean isopeningBalanceInvoice = row.getInvoice().isIsOpeningBalenceInvoice();
                    double rowAmount=(authHandler.round(row.getAmount(), companyid));
                    if(row.getInvoice().isNormalInvoice()){
//                        invoiceCreationDate = row.getInvoice().getJournalEntry().getEntryDate();
                        invoiceAmount = isReceiptEdit?row.getInvoice().getCustomerEntry().getAmount():rowAmount;
                        externalCurrencyRate = row.getInvoice().getJournalEntry().getExternalCurrencyRate();
                    }else{// opening balance invoice creation date
                        invoiceAmount = isReceiptEdit?row.getInvoice().getOriginalOpeningBalanceAmount():rowAmount;
                        externalCurrencyRate = row.getInvoice().getExchangeRateForOpeningTransaction();
                    }
                    invoiceCreationDate = row.getInvoice().getCreationDate();
                    
                    JSONObject obj = new JSONObject();
                    obj.put("billid", isReceiptEdit?row.getInvoice().getID():re.getID());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("currencysymbol", row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol());
                    obj.put("transectionno", row.getInvoice().getInvoiceNumber());
                    obj.put("transectionid", row.getInvoice().getID());
                    obj.put("amount",  invoiceAmount);
                    obj.put("accountid",  (isReceiptEdit?row.getInvoice().getAccount().getID():""));
                    if(row.getInvoice()!=null){
                    obj.put( "currencyidtransaction",row.getInvoice().getCurrency()==null?(row.getReceipt().getCurrency()==null?currency.getCurrencyID():row.getReceipt().getCurrency().getCurrencyID()):row.getInvoice().getCurrency().getCurrencyID());
                        obj.put("currencysymbol", row.getInvoice().getCurrency()==null?(row.getReceipt().getCurrency()==null?currency.getSymbol():row.getReceipt().getCurrency().getSymbol()):row.getInvoice().getCurrency().getSymbol());
                        obj.put("currencysymboltransaction", row.getInvoice().getCurrency()==null?(row.getReceipt().getCurrency()==null?currency.getSymbol():row.getReceipt().getCurrency().getSymbol()):row.getInvoice().getCurrency().getSymbol());
                    }else{
                        obj.put("currencyidtransaction", (row.getReceipt().getCurrency()==null?currency.getCurrencyID():row.getReceipt().getCurrency().getCurrencyID()));
                        obj.put("currencysymbol", (row.getReceipt().getCurrency()==null?currency.getSymbol():row.getReceipt().getCurrency().getSymbol()));
                        obj.put("currencysymboltransaction", (row.getReceipt().getCurrency()==null?currency.getSymbol():row.getReceipt().getCurrency().getSymbol()));
                    }
                    obj.put("duedate", df.format(row.getInvoice().getDueDate()));
                    obj.put("creationdate", df.format(invoiceCreationDate));
                    if (row.getInvoice().getTax() != null) {
//                        KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getInvoice().getJournalEntry().getEntryDate(), row.getInvoice().getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getInvoice().getCreationDate(), row.getInvoice().getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("taxpercent", taxPercent);
                    obj.put("discount", row.getInvoice().getDiscount() == null ? 0 : row.getInvoice().getDiscount().getDiscountValue());
                    obj.put("payment", row.getInvoice().getID());
                    obj.put("gstCurrencyRate", row.getGstCurrencyRate());
                    if(isReceiptEdit){
                        obj.put("amountpaid", rowAmount);
                        if(row.getFromCurrency()!=null&&row.getToCurrency()!=null){
                            obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());
                            obj.put("amountpaidincurrency", authHandler.round(rowAmount/row.getExchangeRateForTransaction(), companyid));
                        }else{
                            double amount = rowAmount;
                            KwlReturnObject bAmt = null;
                            String fromcurrencyid = (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID());
                            String tocurrencyid = (row.getInvoice().getCurrency()==null?(row.getInvoice().getCurrency()==null?currency.getCurrencyID():row.getReceipt().getCurrency().getCurrencyID()):row.getInvoice().getCurrency().getCurrencyID());
                            if (isopeningBalanceInvoice && row.getInvoice().isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(requestParams, amount, fromcurrencyid, tocurrencyid, invoiceCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid, invoiceCreationDate, externalCurrencyRate);
                            }
                            amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            obj.put("amountpaidincurrency", amount);
                            obj.put("exchangeratefortransaction", amount/row.getAmount());
                        }
                    }else{
                        if(row.getFromCurrency()!=null&&row.getToCurrency()!=null){
                            obj.put("amountpaid", authHandler.round(rowAmount/row.getExchangeRateForTransaction(), companyid));
                        }else{
                            double amount = rowAmount;
                            String fromcurrencyid = (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID());
                            String tocurrencyid = (row.getInvoice().getCurrency()==null?(row.getInvoice().getCurrency()==null?currency.getCurrencyID():row.getReceipt().getCurrency().getCurrencyID()):row.getInvoice().getCurrency().getCurrencyID());
                            KwlReturnObject bAmt = null;
                            if (isopeningBalanceInvoice && row.getInvoice().isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(requestParams, amount, fromcurrencyid, tocurrencyid, invoiceCreationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid, invoiceCreationDate, externalCurrencyRate);
                            }
                            amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            obj.put("amountpaid", amount);
                        }
                    }
                    double amountdue = 0.0;
                    double amountDueOriginal = 0.0;
                    double totalAmount = 0.0;
                    if(row.getInvoice().isNormalInvoice()){
                        amountdue= accInvoiceCommon.getAmountDue(requestParams,row.getInvoice());
                        requestParams.put("amountDueOriginalFlag", true);
                        amountDueOriginal= accInvoiceCommon.getAmountDue(requestParams,row.getInvoice());
                        requestParams.remove("amountDueOriginalFlag");
                        totalAmount = row.getInvoice().getCustomerEntry().getAmount();
                    }else{
                        amountdue = row.getInvoice().getOpeningBalanceAmountDue()*(row.getExchangeRateForTransaction()==0?1:row.getExchangeRateForTransaction());
                        amountDueOriginal=row.getInvoice().getOpeningBalanceAmountDue();
                        totalAmount = row.getInvoice().getOriginalOpeningBalanceAmount();
                    }
                    
                     amountdue = authHandler.round(amountdue, companyid);
                     amountDueOriginal=authHandler.round(amountDueOriginal, companyid);
                     totalAmount = authHandler.round(totalAmount, companyid);
                    
                    if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                        obj.put("amountduenonnegative", (isReceiptEdit ? (amountDueOriginal*row.getExchangeRateForTransaction())+rowAmount : amountDueOriginal));
                    } else {
                        obj.put("amountduenonnegative", (isReceiptEdit ? amountdue +  obj.optDouble("amountpaid",0) : amountdue));
                    }
                    obj.put("amountDueOriginal", (isReceiptEdit?amountDueOriginal+obj.optDouble("amountpaidincurrency",0):amountDueOriginal));
                    obj.put("amountDueOriginalSaved", (isReceiptEdit?amountDueOriginal+obj.optDouble("amountpaidincurrency",0):amountDueOriginal));
                    
                    
                    obj.put("totalamount", totalAmount);
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
                   if (idcustresult.getEntityList().size() > 0) {
                       AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                       AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                       DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateFromDB=null;
                       for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
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
               jobj.put("data", jArr);
           }
       } catch (JSONException ex) {
           throw ServiceException.FAILURE("getReceiptRowsJSON : " + ex.getMessage(), ex);
       }
        return jobj;
    }

    public ModelAndView getPaymentsLinkedWithDebitNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject returnObj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            List ll = getPaymentsLinkedWithNCreditNote(request);

            JSONArray DataJArr = (JSONArray) ll.get(0);
            boolean isNoteLinkedWithPayment = (Boolean) ll.get(1);

            returnObj.put("data", DataJArr);
            returnObj.put("isNoteLinkedWithPayment", isNoteLinkedWithPayment);
            issuccess = true;
            msg = messageSource.getMessage("acc.main.rec", null, RequestContextUtils.getLocale(request));   //"Records fetched successfully";
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
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

            KwlReturnObject result = accReceiptDAOobj.getPaymentIdLinkedWithNote(noteId);
            List list = result.getEntityList();
            Iterator it = list.iterator();
            while (it.hasNext()) {
                isNoteLinkedWithPayment = true;
                JSONObject jobj = new JSONObject();
                Receipt receipt = null;
                String paymentId = (String) it.next();
                if (!StringUtil.isNullOrEmpty(paymentId)) {
                    KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentId);
                    if (!paymentResult.getEntityList().isEmpty()) {
                        receipt = (Receipt) paymentResult.getEntityList().get(0);
                    }
                }
                if (receipt != null) {
                    jobj.put("paymentNumber", receipt.getReceiptNumber());
                    jobj.put("paymentJe", receipt.getJournalEntry().getEntryNumber());
                }
                returnArray.put(jobj);
            }
            returlList.add(returnArray);
            returlList.add(isNoteLinkedWithPayment);

        } catch (JSONException ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returlList;
    }
   
    public void exportSingleReceivePayment(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException, JSONException {
        try {
            JSONObject requestObj = StringUtil.convertRequestToJsonObject(request);
            HashMap<String, Object> otherconfigrequestParams = new HashMap();
            String SOID = requestObj.optString("bills");
            String companyid = requestObj.optString(Constants.companyKey);
            int moduleid = requestObj.optInt(Constants.moduleid, 0);
            JSONArray lineItemsArr = new JSONArray();

            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), SOID);
            Receipt receipt = (Receipt) objItr.getEntityList().get(0);
            String jID= receipt.getJournalEntry().getID();
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
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId,1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
            replaceFieldMap = new HashMap<String, String>();

            /*
             * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
             */
            fieldrequestParams.clear();
            HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId, 1, 0));
            HashMap<String, Integer> DimensionFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

            fieldrequestParams.clear();
            HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId, 1, 1));
            HashMap<String, Integer> LineLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

            HashMap<String, JSONArray>  itemDataReceivePayment = new HashMap<String, JSONArray>();
            
            HashMap<String, Object> paramMap = new HashMap();
            paramMap.put(Constants.fieldMap, FieldMap);
            paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
            paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
            paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
            
            for(int count=0 ; count < SOIDList.size() ; count++ ){
                if (Constants.isNewPaymentStructure) {
                    lineItemsArr = accReceiptServiceDAOobj.getRPDetailsItemJSONNew(requestObj, SOIDList.get(count), paramMap);
                } else {
                    lineItemsArr = getRPDetailsItemJSON(requestObj, SOIDList.get(count), paramMap);//previous code
                }
                itemDataReceivePayment.put(SOIDList.get(count), lineItemsArr);

                // Below Function called to update print flag for RP Report
                accCommonTablesDAO.updatePrintFlag(moduleid, SOIDList.get(count), companyid);
            }

            otherconfigrequestParams.put(Constants.moduleid, moduleid);
            String invoicePostText="";//goodsReceiptOrder.getPostText()==null?"":goodsReceiptOrder.getPostText()
            ExportRecordHandler.exportSingleGeneric(request, response,itemDataReceivePayment,accCustomData,customDesignDAOObj,accCommonTablesDAO,accAccountDAOobj,accountingHandlerDAOobj,velocityEngine,invoicePostText,otherconfigrequestParams,accInvoiceServiceDAOObj,accGoodsReceiptServiceDAOObj);
        } catch (JSONException ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
      
    public JSONArray getRPDetailsItemJSON(JSONObject requestObj, String SOID, HashMap<String, Object> paramMap) throws SessionExpiredException, ServiceException, JSONException {
        JSONArray jArr = new JSONArray();
        try {
            Receipt receipt = null;
            KwlReturnObject result1;
            PaymentMethod payMethod = null;
            PayDetail PayDetail = null;
            ReceiptDetail row = null;
            PdfTemplateConfig config = null;
            BankReconciliation bankreconciliation = null;
            JSONObject summaryData = new JSONObject();
            JSONArray accJSONArr = new JSONArray();
            JSONArray invJSONArr = new JSONArray();
            JSONArray debitJSONArr = new JSONArray();
            JSONArray VenJSONArr = new JSONArray();
            JSONArray dbCustomJSONArr = new JSONArray();
            
            HashMap<String, Integer> FieldMap = (HashMap<String, Integer>) paramMap.get(Constants.fieldMap);
            HashMap<String, String> replaceFieldMap = (HashMap<String, String>) paramMap.get(Constants.replaceFieldMap);
            String companyid = requestObj.optString(Constants.companyKey);

            List list = null;
            KwlReturnObject result;

            boolean ismanycrdb = false;
            String customergoodsreceiptno = "", invoicetax = "", paymentmethod = "", paymentaccount = "", bankchequeno = "", chequebankname = "", invdesc = "", gridtaxname = "", gridaccountdescription = "";
            String receiptNumber = "", AccountName = "", memo = "", invoiceNos = "", invoicedates = "", invduedates = "", invtax = "", invdiscount = "";
            String invoriginalamount = "", inventerpayment = "", invexchagerates = "", invoriginalamountdue = "", invamountdue = "";
            String gridaccountname = "";

            //3rd option Debit Note
            String dnnumber = "", dnamount = "", dnamountdue = "", dnenterpayment = "";

            //4rth option grid values-GL Code
            String gridtype = "", gridaccname = "", gridaccamount = "", gridaccdesc = "", gridacctax = "",gridacctaxamount="", gridaccwithtax = "";
                //Common appendtext
            String commonAppendtext="";
            String commonEnterPayment="";

            int count=1,rowcnt = 0;
            int receiptType = 0;
            double advanceAmount = 0;
            double invoicediscount = 0, invoiceamount = 0, gridtamountwithtax = 0, gridtaxamount = 0, invoiceamountdue = 0;
            double total = 0, amountdues = 0, enterpayment = 0,taxtotalgst=0,subtotal=0;
            java.util.Date entryDate = null, grduedate = null, grdate = null, chequecleardate = null;
            Date journalEntryDate = new Date();
            String invexchangerate ="",DOref="",SOref="",QouteRef="";//mode!=StaticValues.AUTONUM_BILLINGRECEIPT?rc.getJournalEntry().getEntryDate():rc1.getJournalEntry().getEntryDate()

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(requestObj);
            HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
            int moduleid = requestObj.optInt(Constants.moduleid);
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), SOID);
            receipt = (Receipt) objItr.getEntityList().get(0);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), requestObj.optString(Constants.globalCurrencyKey));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, moduleid);
            if (templateConfig.getEntityList().size() > 0) {//getCompanyPostText
             config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            DateFormat df = authHandler.getDateOnlyFormat();
            SimpleDateFormat sdf = new SimpleDateFormat(CustomDesignerConstants.DateFormat_RemovingTime);

            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
             countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }

            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
            rRequestParams.put("filter_names", filter_names);
            rRequestParams.put("filter_params", filter_params);

            paymentmethod = receipt.getPayDetail().getPaymentMethod().getMethodName();
            paymentaccount = receipt.getPayDetail() != null ? receipt.getPayDetail().getPaymentMethod().getAccount().getName() : "";

            receiptType = receipt.getReceipttype();
            //checking condition for all the option
            Iterator itr = receipt.getRows().iterator();
            
            //document currency
            if (receipt != null && receipt.getCurrency() != null && !StringUtil.isNullOrEmpty(receipt.getCurrency().getCurrencyID())) {
                summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, receipt.getCurrency().getCurrencyID());
            }
            
            double jeExternalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
            double revExchangeRate = 1.0;
            if (jeExternalCurrencyRate != 0.0) {
                revExchangeRate = 1 / jeExternalCurrencyRate;
            }
             //get the custom field of lineitem                                        
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(receipt.getCompany().getCompanyID(), Constants.Acc_Receive_Payment_ModuleId));
            replaceFieldMap = new HashMap<String, String>();
            FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);

            if (receiptType == 1) {
             //To iterate the linked vendor invoice no
             while (itr.hasNext()) {
                 row = (ReceiptDetail) itr.next();
                 JSONObject obj = new JSONObject();
                 List ll = null;
                 double invoiceReturnedAmt = 0d;
                 double originalamount = 0d;
                 double externalCurrencyRate = 0d, exchangeratefortransaction = 0d, amountpaidincurrency = 0d, amountduenonnegative = 0d;
                 Date invoiceCreationDate = null;
                 String currencyidtransaction = "", currencysymbol = "", currencysymboltransaction = "";
                 boolean isReceiptEdit = true;
                 DOref="";SOref="";QouteRef="";

                 customergoodsreceiptno = row.getInvoice().getInvoiceNumber();
                 grduedate = row.getInvoice().getDueDate();
            //                 grdate = row.getInvoice().getJournalEntry().getEntryDate();
                 grdate = row.getInvoice().getCreationDate();
                 invoicetax = row.getInvoice().getTax() != null ? row.getInvoice().getTax().getName() : "0%";
                 originalamount = row.getInvoice().getCustomerEntry().getAmount();//Original Amount of invoice grid

                 Discount disc = row.getInvoice().getDiscount();
                 if (disc != null) {
                     invoiceReturnedAmt = disc.getDiscountValue();
                 }

                 if (row.getInvoice() != null) {//getting currency name for calculating external currencyrate
                     obj.put("currencyidtransaction", row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()) : row.getInvoice().getCurrency().getCurrencyID());
                     obj.put("currencysymbol", row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()) : row.getInvoice().getCurrency().getSymbol());
                     obj.put("currencysymboltransaction", row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()) : row.getInvoice().getCurrency().getSymbol());
                     currencyidtransaction= row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()) : row.getInvoice().getCurrency().getCurrencyID();
                     currencysymbol=row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()) : row.getInvoice().getCurrency().getSymbol();
                     currencysymboltransaction=row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()) : row.getInvoice().getCurrency().getSymbol();
                 } else {
                     obj.put("currencyidtransaction", (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()));
                     obj.put("currencysymbol", (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()));
                     obj.put("currencysymboltransaction", (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()));
                     currencyidtransaction = (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID());
                     currencysymbol = (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol());
                     currencysymboltransaction = (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol());
                 }

                 if (row.getInvoice().isNormalInvoice()) {//ceration date.original amount of grid & external currency rate
            //                     invoiceCreationDate = row.getInvoice().getJournalEntry().getEntryDate();
                     originalamount = isReceiptEdit ? row.getInvoice().getCustomerEntry().getAmount() : row.getAmount();//Original amount of grid
                     externalCurrencyRate = row.getInvoice().getJournalEntry().getExternalCurrencyRate();
                 } else {// opening balance invoice creation date
                     originalamount = isReceiptEdit ? row.getInvoice().getOriginalOpeningBalanceAmount() : row.getAmount();
                     externalCurrencyRate = row.getInvoice().getExchangeRateForOpeningTransaction();
                 }
                 invoiceCreationDate = row.getInvoice().getCreationDate();

                 if (row.getInvoice() != null) {//transaction & invoice currency information
                     currencyidtransaction = row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()) : row.getInvoice().getCurrency().getCurrencyID();
                     currencysymbol = row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()) : row.getInvoice().getCurrency().getSymbol();
                     currencysymboltransaction = row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()) : row.getInvoice().getCurrency().getSymbol();
                 } else {
                     currencyidtransaction = (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID());
                     currencysymbol = (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol());
                     currencysymboltransaction = (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol());
                 }

                 if (isReceiptEdit) {
                     obj.put("amountpaid", row.getAmount());
                     if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                         obj.put("amountpaidincurrency", row.getAmount() / row.getExchangeRateForTransaction());
                         obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());
                         exchangeratefortransaction = row.getExchangeRateForTransaction();
                         amountpaidincurrency = row.getAmount() / row.getExchangeRateForTransaction();
                     } else {
                         double amount = row.getAmount();
                         String fromcurrencyid = (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID());
                         String tocurrencyid = (row.getInvoice().getCurrency() == null ? (row.getInvoice().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()) : row.getInvoice().getCurrency().getCurrencyID());
                         KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, amount, fromcurrencyid, tocurrencyid, invoiceCreationDate, externalCurrencyRate);
                         amount = (Double) bAmt.getEntityList().get(0);
                         obj.put("amountpaidincurrency", amount);
                         obj.put("exchangeratefortransaction", amount / row.getAmount());
                         exchangeratefortransaction = amount / row.getAmount();
                         amountpaidincurrency = amount;
                     }
                 } else {
                     if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                         obj.put("amountpaid", row.getAmount() / row.getExchangeRateForTransaction());
                     } else {
                         double amount = row.getAmount();
                         String fromcurrencyid = (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID());
                         String tocurrencyid = (row.getInvoice().getCurrency() == null ? (row.getInvoice().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()) : row.getInvoice().getCurrency().getCurrencyID());
                         KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, amount, fromcurrencyid, tocurrencyid, invoiceCreationDate, externalCurrencyRate);
                         amount = (Double) bAmt.getEntityList().get(0);
                         obj.put("amountpaid", amount);
                     }
                 }

                 double amountdue = 0.0;
                 double amountDueOriginal = 0.0;
                 double totalAmount = 0.0;
                 if (row.getInvoice().isNormalInvoice()) {
                     amountdue = accInvoiceCommon.getAmountDue(requestParams, row.getInvoice());
                     requestParams.put("amountDueOriginalFlag", true);
                     amountDueOriginal = accInvoiceCommon.getAmountDue(requestParams, row.getInvoice());
                     requestParams.remove("amountDueOriginalFlag");
                     totalAmount = row.getInvoice().getCustomerEntry().getAmount();
                 } else {
                     amountdue = row.getInvoice().getOpeningBalanceAmountDue() * row.getExchangeRateForTransaction();
                     amountDueOriginal = row.getInvoice().getOpeningBalanceAmountDue();
                     totalAmount = row.getInvoice().getOriginalOpeningBalanceAmount();
                 }
                 if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                     obj.put("amountduenonnegative", (isReceiptEdit ? (amountDueOriginal * row.getExchangeRateForTransaction()) + row.getAmount() : amountDueOriginal));
                     amountduenonnegative = (isReceiptEdit ? (amountDueOriginal * row.getExchangeRateForTransaction()) + row.getAmount() : amountDueOriginal);
                 } else {
                     obj.put("amountduenonnegative", (isReceiptEdit ? amountdue + obj.optDouble("amountpaid", 0) : amountdue));
                     amountduenonnegative = (isReceiptEdit ? amountdue + obj.optDouble("amountpaid", 0) : amountdue);
                 }
                 obj.put("amountDueOriginal", (isReceiptEdit ? amountDueOriginal + obj.optDouble("amountpaidincurrency", 0) : amountDueOriginal));
                 amountDueOriginal= (isReceiptEdit ? amountDueOriginal + obj.optDouble("amountpaidincurrency", 0) : amountDueOriginal);
                 obj.put("amountDueOriginalSaved", (isReceiptEdit ? amountDueOriginal + obj.optDouble("amountpaidincurrency", 0) : amountDueOriginal));
                 obj.put("totalamount", totalAmount);

                 invexchangerate="1 "+currencysymboltransaction+" = "+exchangeratefortransaction+" "+receipt.getCurrency().getSymbol();

                 // ## Get Custom Field Data 
                 Map<String, Object> variableMap = new HashMap<String, Object>();
                 HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                 ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                 Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                 Detailfilter_params.add(row.getID());
                 invDetailRequestParams.put("filter_names", Detailfilter_names);
                 invDetailRequestParams.put("filter_params", Detailfilter_params);
                 KwlReturnObject idcustresult = accReceiptDAOobj.getReciptPaymentCustomData(invDetailRequestParams);
                 if (idcustresult.getEntityList().size() > 0) {
                     AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                     AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                     Map<String, Object> resultMap = accMainAccountingService.getCustomFieldsForExport(customFieldMap, variableMap,customDateFieldMap);
                     for (Map.Entry<String, Object> varEntry : resultMap.entrySet()) {
                         String coldata = (varEntry.getValue() != null) ? (!varEntry.getValue().toString().equals("null") ? varEntry.getValue().toString() : "") : "";
                         obj.put(varEntry.getKey(), coldata);
                         summaryData.put(varEntry.getKey(), coldata);
                     }
                 }

                 for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//checks when field has any value
                     if (!obj.has(field.getKey())) {
                         obj.put(field.getKey(), "");
                         summaryData.put(field.getKey(), "");
                     }
                 }

                 //to calculate linking information in Customer Invoice
                 order_by.add("srno");
                 order_type.add("asc");
                 rRequestParams.put("order_by", order_by);
                 rRequestParams.put("order_type", order_type);
                 filter_params.clear();
                 filter_names.clear();
                 filter_names.add("invoice.ID");
                 filter_params.add(row.getInvoice().getID());
                 KwlReturnObject idresult = accInvoiceDAOobj.getInvoiceDetails(rRequestParams);
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
                             dOref=true;
                         }

                     } else if (invdrow.getSalesorderdetail() != null) {
                         if (SOref.indexOf(invdrow.getSalesorderdetail().getSalesOrder().getSalesOrderNumber()) == -1) {
                             SOref += invdrow.getSalesorderdetail().getSalesOrder().getSalesOrderNumber() + ",";
                             soref=true;
                         }

                     } else if (invdrow.getQuotationDetail() != null) {
                         if (QouteRef.indexOf(invdrow.getQuotationDetail().getQuotation().getquotationNumber()) == -1) {
                             QouteRef += invdrow.getQuotationDetail().getQuotation().getquotationNumber() + ",";
                             qouteRef=true;
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

                 commonAppendtext="Invoice # "+customergoodsreceiptno+" dated " + sdf.format(grdate);

                 obj.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId, QouteRef);
                 obj.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, SOref);
                 obj.put(CustomDesignerConstants.CustomDesignDORefNumber_fieldTypeId, DOref);

                 obj.put(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo, customergoodsreceiptno);
                 obj.put(CustomDesignerConstants.RPMP_InvoiceDate, sdf.format(grdate));
                 obj.put(CustomDesignerConstants.RPMP_DueDate, sdf.format(grduedate));
                 obj.put(CustomDesignerConstants.RPMP_Tax, invoicetax);
                 obj.put(CustomDesignerConstants.RPMP_Discount, authHandler.formattedCommaSeparatedAmount((disc != null ? disc.getDiscountValue() : invoicediscount), companyid));
                 obj.put(CustomDesignerConstants.RPMP_OriginalAmount, authHandler.formattedCommaSeparatedAmount(originalamount, companyid));
                 obj.put(CustomDesignerConstants.Invoice_Original_Amount_Due, authHandler.formattedCommaSeparatedAmount(amountDueOriginal, companyid));
                 obj.put(CustomDesignerConstants.Invoice_Exchange_Rate, invexchangerate);
                 obj.put(CustomDesignerConstants.RPMP_AmountDue, authHandler.formattedCommaSeparatedAmount(amountduenonnegative, companyid));
                 obj.put(CustomDesignerConstants.RPMP_EnterPayment, authHandler.formattedCommaSeparatedAmount(row.getAmount(), companyid));
                 obj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance,commonAppendtext);
                 obj.put(CustomDesignerConstants.Common_EnterPayment, authHandler.formattedCommaSeparatedAmount(row.getAmount(), companyid));
                 obj.put(CustomDesignerConstants.RPMP_CustomerVendorName, "");
                 obj.put(CustomDesignerConstants.SrNo,count);
                 invJSONArr.put(obj);
                 count++;
             }
            } else if (receiptType == 2 || receiptType == 9) {//receiptType=2 when edited glcode & receiptType=9 for creating glcode
             filter_names = new ArrayList();
             filter_params = new ArrayList();
             filter_names.add("receipt.ID");
             filter_params.add(receipt.getID());

             KwlReturnObject pdoresult = accReceiptDAOobj.getReceiptDetailOtherwise(rRequestParams);//to calculate tax
             List<ReceiptDetailOtherwise> list1 = pdoresult.getEntityList();

             Iterator pdoRow = list1.iterator();
             if (pdoRow != null && list1.size() > 0) {
                 for (ReceiptDetailOtherwise receiptDetailOtherwise : list1) {
                     if (receipt.getID().equals(receiptDetailOtherwise.getReceipt().getID())) {
                         double taxamount = 0,amount=0;
                         if (receiptDetailOtherwise.getTax() != null) {
                             taxamount = receiptDetailOtherwise.getTaxamount();
                             taxtotalgst += taxamount;
                         }
                         subtotal+=receiptDetailOtherwise.getAmount();

                         commonAppendtext=receiptDetailOtherwise.getAccount().getName();

                         JSONObject obj = new JSONObject();
                         obj.put(CustomDesignerConstants.GridType, "Credit");
                         obj.put(CustomDesignerConstants.GridAccountName, receiptDetailOtherwise.getAccount().getName());
                         obj.put(CustomDesignerConstants.GridAmountinSGD, authHandler.formattedCommaSeparatedAmount(receiptDetailOtherwise.getAmount(), companyid));
                         obj.put(CustomDesignerConstants.GridDesc, receiptDetailOtherwise.getDescription() != null ? receiptDetailOtherwise.getDescription() : "");
                         obj.put(CustomDesignerConstants.GridTax, receiptDetailOtherwise.getTax() != null ? receiptDetailOtherwise.getTax().getName() : "");
                         obj.put(CustomDesignerConstants.GridTaxAmount, authHandler.formattedCommaSeparatedAmount(taxamount, companyid));

                         if (receipt.isIsmanydbcr()) {
                             if (receiptDetailOtherwise.isIsdebit()) {
                                 amount -= Double.parseDouble(authHandler.formattedAmount(((receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount())), companyid));
                             } else {
                                 amount += Double.parseDouble(authHandler.formattedAmount(((receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount())), companyid));
                             }
                         } else {
                             amount += Double.parseDouble(authHandler.formattedAmount(((receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount())), companyid));
                         }

                         obj.put(CustomDesignerConstants.GridAmountinTax, authHandler.formattedCommaSeparatedAmount(amount, companyid));
                         obj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, commonAppendtext);
                         obj.put(CustomDesignerConstants.Common_EnterPayment, authHandler.formattedCommaSeparatedAmount(amount, companyid));
                         obj.put(CustomDesignerConstants.SrNo,count); 

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
                             Map<String, Object> resultMap = accMainAccountingService.getCustomFieldsForExport(customFieldMap, variableMap,customDateFieldMap);
                             for (Map.Entry<String, Object> varEntry : resultMap.entrySet()) {
                                String coldata = (varEntry.getValue() != null) ? (!varEntry.getValue().toString().equals("null") ? varEntry.getValue().toString() : "") : "";
                                obj.put(varEntry.getKey(), coldata);
                                summaryData.put(varEntry.getKey(), coldata);
                             }
                         } 

                         for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//checks when field has any value
                             if (!obj.has(field.getKey())) {
                                 obj.put(field.getKey(), "");
                                 summaryData.put(field.getKey(), "");
                             }
                         }
                         obj.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId, QouteRef);
                         obj.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, SOref);
                         obj.put(CustomDesignerConstants.CustomDesignDORefNumber_fieldTypeId, DOref);
                         accJSONArr.put(obj);
                         count++;
                     }
                 }
             }
            } else if (receiptType == 7) {//against debit note

             result1 = accReceiptDAOobj.getreceipthistory(receipt.getID());
             List ls = result1.getEntityList();
             Iterator<Object[]> itr1 = ls.iterator();
             while (itr1.hasNext()) {
                 JSONObject obj = new JSONObject();
                 Object[] debitrow = (Object[]) itr1.next();
                 String dnid = debitrow[0].toString();
                 KwlReturnObject debitnotedetails = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnid);
                 DebitNote debitnote = (DebitNote) debitnotedetails.getEntityList().get(0);
                 String debitnoteno = debitnote.getDebitNoteNumber();
                 Double debitnoteamount = debitnote.getDnamount();
                 Double amountpaid = Double.parseDouble(debitrow[1].toString());
                 Double amountdue = Double.parseDouble(debitrow[2].toString());

            //                  commonAppendtext="Debit Note # "+debitnoteno+" dated "+sdf.format(debitnote.getJournalEntry().getEntryDate());
                  commonAppendtext="Debit Note # "+debitnoteno+" dated "+sdf.format(debitnote.getCreationDate());

                 obj.put(CustomDesignerConstants.paymentDebit_note, debitnoteno);
                 obj.put(CustomDesignerConstants.paymentDebit_noteAmount, authHandler.formattedCommaSeparatedAmount(debitnoteamount, companyid));
                 obj.put(CustomDesignerConstants.paymentDebit_note_AmountDue, authHandler.formattedCommaSeparatedAmount(amountdue, companyid));
                 obj.put(CustomDesignerConstants.paymentDebit_note_EnterPayment, authHandler.formattedCommaSeparatedAmount(amountpaid, companyid));
                 obj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, commonAppendtext);
                 obj.put(CustomDesignerConstants.Common_EnterPayment, authHandler.formattedCommaSeparatedAmount(amountpaid, companyid));
                 obj.put(CustomDesignerConstants.SrNo,count);
                 obj.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId, QouteRef);
                 obj.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, SOref);
                 obj.put(CustomDesignerConstants.CustomDesignDORefNumber_fieldTypeId, DOref);

                 for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//putting custom field value as blank because debit note doesn't have line item custom field
                     if (!obj.has(field.getKey())) {
                         obj.put(field.getKey(), "");
                         summaryData.put(field.getKey(), "");
                     }
                 }
                 debitJSONArr.put(obj);
                 count++;
             }
            } else if (receiptType == 6) {//against vendor
             for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//checks when field has any value
                 summaryData.put(field.getKey(), "");
             }
             JSONObject obj = new JSONObject();
             obj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, "Advance Payment");
             obj.put(CustomDesignerConstants.Common_EnterPayment, requestObj.optDouble("amount"));
             VenJSONArr.put(obj);

            }

            //to calculate the cheque details of the payment
            int choice = receipt.getPayDetail().getPaymentMethod().getDetailType();
            if (choice == 2) {//if entry of cheque details
             bankchequeno = receipt.getPayDetail().getCheque().getChequeNo();
             chequebankname = receipt.getPayDetail().getCheque().getBankName();
             chequecleardate = receipt.getPayDetail().getCheque().getDueDate();
            //             Date chequebankrecon=bankreconciliation.getClearanceDate();
             invdesc = receipt.getPayDetail().getCheque().getDescription();
             summaryData.put(CustomDesignerConstants.Chequeno, bankchequeno);
             summaryData.put(CustomDesignerConstants.BankName, chequebankname);
             summaryData.put(CustomDesignerConstants.ChequeDate, sdf.format(chequecleardate));
             summaryData.put(CustomDesignerConstants.BankDescription, invdesc);
             KwlReturnObject clearanceDate = accBankReconciliationObj.getBRfromJE(receipt.getJournalEntry().getID(), receipt.getCompany().getCompanyID(), false);
             if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
                 BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
                 if (brd.getBankReconciliation().getClearanceDate() != null) {//if clearance date then Payment Status is cleared else uncleared
                     summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "Cleared");
                     summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, sdf.format(brd.getBankReconciliation().getClearanceDate()));
                 } else {
                     summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "Uncleared");
                     summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "");
                 }
             } else {
                 summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "Uncleared");
                 summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "");
             }
            } else {
             summaryData.put(CustomDesignerConstants.Chequeno, bankchequeno);
             summaryData.put(CustomDesignerConstants.BankName, chequebankname);
             summaryData.put(CustomDesignerConstants.ChequeDate, "");
             summaryData.put(CustomDesignerConstants.BankDescription, invdesc);
             summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "");
             summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "");
            }

            // Append comma separated invoice grid values-Against Customer Invoice
            if (invJSONArr != null) {
             for (int cnt = 0; cnt < invJSONArr.length(); cnt++) {
                 JSONObject jObj = (JSONObject) invJSONArr.get(cnt);
                 if (cnt == 0) {
                     invoiceNos = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo));
                     invoicedates = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_InvoiceDate)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_InvoiceDate));
                     invduedates = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DueDate)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_DueDate));
                     invtax = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Tax)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_Tax));
                     invdiscount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Discount)) ? "-" :jObj.getString(CustomDesignerConstants.RPMP_Discount));
                     invoriginalamount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_OriginalAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_OriginalAmount));
                     invoriginalamountdue = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Invoice_Original_Amount_Due)) ? "-" : jObj.getString(CustomDesignerConstants.Invoice_Original_Amount_Due));
                     invexchagerates = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Invoice_Exchange_Rate)) ? "-" : jObj.getString(CustomDesignerConstants.Invoice_Exchange_Rate));
                     invamountdue = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_AmountDue)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_AmountDue));
                     inventerpayment = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_EnterPayment)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_EnterPayment));
                 } else {
                     invoiceNos = invoiceNos + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo));
                     invoicedates = invoicedates + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_InvoiceDate)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_InvoiceDate));
                     invduedates = invduedates + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DueDate)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_DueDate));
                     invtax = invtax + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Tax)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_Tax));
                     invdiscount = invdiscount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Discount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_Discount));
                     invoriginalamount = invoriginalamount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_OriginalAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_OriginalAmount));
                     invoriginalamountdue = invoriginalamountdue + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Invoice_Original_Amount_Due)) ? "-" : jObj.getString(CustomDesignerConstants.Invoice_Original_Amount_Due));
                     invexchagerates = invexchagerates + "," +(StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Invoice_Exchange_Rate)) ? "-" : jObj.getString(CustomDesignerConstants.Invoice_Exchange_Rate));
                     invamountdue = invamountdue + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_AmountDue)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_AmountDue));
                     inventerpayment = inventerpayment + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_EnterPayment)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_EnterPayment));
                 }
             }
            }

            // Append comma separated Debit Note grid values for 3rd option-Debit Note
            if (debitJSONArr != null) {
             for (int cnt = 0; cnt < debitJSONArr.length(); cnt++) {
                 JSONObject jObj = (JSONObject) debitJSONArr.get(cnt);
                 if (cnt == 0) {
                     dnnumber = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_note)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_note));
                     dnamount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_noteAmount)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_noteAmount));
                     dnamountdue = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_note_AmountDue)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_note_AmountDue));
                     dnenterpayment = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_note_EnterPayment)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_note_EnterPayment));
                 } else {
                     dnnumber = dnnumber + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_note)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_note));
                     dnamount = dnamount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_noteAmount)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_noteAmount));
                     dnamountdue = dnamountdue + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_note_AmountDue)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_note_AmountDue));
                     dnenterpayment = dnenterpayment + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_note_EnterPayment)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_note_EnterPayment));
                 }
             }
            }

            // Append comma separated account grid values for 4rth option-GL COde
            if (accJSONArr != null) {
             for (int cnt = 0; cnt < accJSONArr.length(); cnt++) {
                 JSONObject jObj = (JSONObject) accJSONArr.get(cnt);
                 if (cnt == 0) {
                     gridtype = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridType)) ? "-" : jObj.getString(CustomDesignerConstants.GridType));
                     gridaccname = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridAccountName)) ? "-" : jObj.getString(CustomDesignerConstants.GridAccountName));
                     gridaccamount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridAmountinSGD)) ? "-" : jObj.getString(CustomDesignerConstants.GridAmountinSGD));
                     gridaccdesc = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridDesc)) ? "-" : jObj.getString(CustomDesignerConstants.GridDesc));
                     gridacctax = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridTax)) ? "-" : jObj.getString(CustomDesignerConstants.GridTax));
                     gridacctaxamount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridTaxAmount)) ? "-" : jObj.getString(CustomDesignerConstants.GridTaxAmount));
                     gridaccwithtax = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridAmountinTax)) ? "-" : jObj.getString(CustomDesignerConstants.GridAmountinTax));
                 } else {
                     gridtype = gridtype + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridType)) ? "-" : jObj.getString(CustomDesignerConstants.GridType));
                     gridaccname = gridaccname + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridAccountName)) ? "-" : jObj.getString(CustomDesignerConstants.GridAccountName));
                     gridaccamount = gridaccamount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridAmountinSGD)) ? "-" : jObj.getString(CustomDesignerConstants.GridAmountinSGD));
                     gridaccdesc = gridaccdesc + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridDesc)) ? "-" : jObj.getString(CustomDesignerConstants.GridDesc));
                     gridacctax = gridacctax + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridTax)) ? "-" : jObj.getString(CustomDesignerConstants.GridTax));
                     gridacctaxamount =gridacctaxamount+ "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridTaxAmount)) ? "-" : jObj.getString(CustomDesignerConstants.GridTaxAmount));
                     gridaccwithtax = gridaccwithtax + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridAmountinTax)) ? "-" : jObj.getString(CustomDesignerConstants.GridAmountinTax));
                 }
             }
            }

            rRequestParams = new HashMap<String, Object>();
            String netinword = "";
            String cust = requestObj.optString("customer");
            String accname = requestObj.optString("accname");
            String address = requestObj.optString("address");
            double amount = requestObj.optDouble("amount");

            boolean iscontraentryflag = requestObj.optBoolean("contraentryflag", false);

            if (!StringUtil.isNullOrEmpty(requestObj.optString("advanceAmount"))) {
                advanceAmount = requestObj.optDouble("advanceAmount");
            }
            boolean advanceFlag = requestObj.optBoolean("advanceFlag", false);
            receiptNumber = receipt.getReceiptNumber();
            boolean advancepayment = receipt.isIsadvancepayment();
            Receipt advancepaymentid = receipt.getAdvanceid();

            if (advancepaymentid == null) {
             if (advancepayment) {
                 KwlReturnObject pdoresultreceiptno = accReceiptDAOobj.gettotalrecordOfreceiptno(receiptNumber);
                 List<Receipt> listreceiptids = pdoresultreceiptno.getEntityList();
            //                    for (int count = 0; count < listreceiptids.size(); count++) {
                 if (listreceiptids.size() > 1) {
                     for (Receipt receiptitr : listreceiptids) {
                         Receipt advanceid = receiptitr.getAdvanceid();
                         if (advanceid == null) {
                             continue;
                         } else {
                             summaryData.put(CustomDesignerConstants.RPMP_AdvanceAmount, receiptitr.getAdvanceamount() != 0 ? receiptitr.getAdvanceamount() : 0);
                         }

                     }
                 } else {
                     summaryData.put(CustomDesignerConstants.RPMP_AdvanceAmount, receipt.getAdvanceamount());
                 }
             } else {
                 summaryData.put(CustomDesignerConstants.RPMP_AdvanceAmount, receipt.getAdvanceamount());
             }
            } else {
             summaryData.put(CustomDesignerConstants.RPMP_AdvanceAmount, receipt.getAdvanceamount());
            }
            AccountName = receipt.getPayDetail() != null ? receipt.getPayDetail().getPaymentMethod().getAccount().getName() : "";

            if (receiptType == 1) {//against invoice
             jArr.put(invJSONArr);
            } else if (receiptType == 7) {//against debit note
             jArr.put(debitJSONArr);
            } else if (receiptType == 2 || receiptType == 9) {//against gl code
             jArr.put(accJSONArr);
            }else{ //against vendor
            jArr.put(VenJSONArr);
            }

            double totalAdvanceAmount = amount;
            if (advanceFlag) {
             totalAdvanceAmount = amount + advanceAmount;
            }

            netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAdvanceAmount)), currency,countryLanguageId);
            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, totalAdvanceAmount);
            summaryData.put(CustomDesignerConstants.Include_GST, authHandler.formattedAmount(taxtotalgst, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, authHandler.formattedAmount(subtotal, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword + " Only.");

            if ((receiptType != 2) && (receiptType != 6) && (receiptType != 9) && (receiptType != 7)) {
             HashMap<String, Object> addressParams = new HashMap<String, Object>();
             addressParams.put("companyid", companyid);
             addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
             addressParams.put("isBillingAddress", true);    //true to get billing address
             addressParams.put("customerid", receipt.getCustomer().getID());
             CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
             if (customerAddressDetails != null) {
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, customerAddressDetails.getAddress());
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, customerAddressDetails.getCity());
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, customerAddressDetails.getState());
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, customerAddressDetails.getCountry());
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, customerAddressDetails.getPostalCode());
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, customerAddressDetails.getPhone());
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, customerAddressDetails.getFax());
             } else {
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "");
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "");
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "");
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "");
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "");
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "");
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "");
             }
             addressParams.put("isBillingAddress", false);    //true to get shipping address
             customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
             if (customerAddressDetails != null) {
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, customerAddressDetails.getAddress());
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, customerAddressDetails.getCity());
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, customerAddressDetails.getState());
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, customerAddressDetails.getCountry());
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, customerAddressDetails.getPostalCode());
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, customerAddressDetails.getPhone());
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, customerAddressDetails.getFax());
             } else {
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "");
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "");
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "");
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "");
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "");
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "");
                 summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "");
             }
            }
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, config == null ? "" : config.getPdfPostText());
            summaryData.put(CustomDesignerConstants.PaymentMethod, paymentmethod);
            summaryData.put(CustomDesignerConstants.PaymentAccount, paymentaccount);
            summaryData.put(CustomDesignerConstants.RPMP_CustomerVendorName, accname);
            //Invoice comma separated values-Against Customer Invoice
            summaryData.put(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo, invoiceNos);
            summaryData.put(CustomDesignerConstants.RPMP_InvoiceDate, invoicedates);
            summaryData.put(CustomDesignerConstants.RPMP_DueDate, invduedates);
            summaryData.put(CustomDesignerConstants.RPMP_Tax, invtax);
            summaryData.put(CustomDesignerConstants.RPMP_Discount, invdiscount);
            summaryData.put(CustomDesignerConstants.RPMP_OriginalAmount, invoriginalamount);
            summaryData.put(CustomDesignerConstants.Invoice_Original_Amount_Due, invoriginalamountdue);
            summaryData.put(CustomDesignerConstants.Invoice_Exchange_Rate, invexchagerates);
            summaryData.put(CustomDesignerConstants.RPMP_AmountDue, invamountdue);
            summaryData.put(CustomDesignerConstants.RPMP_EnterPayment, inventerpayment);
            //3rd option-Debit Note
            summaryData.put(CustomDesignerConstants.paymentDebit_note, dnnumber);
            summaryData.put(CustomDesignerConstants.paymentDebit_noteAmount, dnamount);
            summaryData.put(CustomDesignerConstants.paymentDebit_note_AmountDue, dnamountdue);
            summaryData.put(CustomDesignerConstants.paymentDebit_note_EnterPayment, dnenterpayment);

            //4rth option-GL Code
            summaryData.put(CustomDesignerConstants.GridType, gridtype);
            summaryData.put(CustomDesignerConstants.GridAccountName, gridaccname);
            summaryData.put(CustomDesignerConstants.GridAmountinSGD, gridaccamount);
            summaryData.put(CustomDesignerConstants.GridDesc, gridaccdesc);
            summaryData.put(CustomDesignerConstants.GridTax, gridacctax);
            summaryData.put(CustomDesignerConstants.GridTaxAmount, gridacctaxamount);
            summaryData.put(CustomDesignerConstants.GridAmountinTax, gridaccwithtax);

            summaryData.put(CustomDesignerConstants.SrNo, 1);
            summaryData.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignDORefNumber_fieldTypeId, "");
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
            summaryData.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, revExchangeRate);
            jArr.put(summaryData);


            //getting all the custom fields at line level
            result = customDesignDAOObj.getCustomLineFields(companyid, moduleid);
            list = result.getEntityList();
            for (int cnt = 0; cnt < list.size(); cnt++) {
             JSONObject obj = new JSONObject();
             HashMap<String,String> map=new   HashMap<String,String>();
             Object[] rowcustom = (Object[]) list.get(cnt);
             map.put("Custom_" + rowcustom[2], "{label:'Custom_" + rowcustom[2] + "',xtype:'" + rowcustom[1].toString() + "'}");
             dbCustomJSONArr.put(map);
            }
            jArr.put(dbCustomJSONArr);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
     }
     
     public void exportPettyCashVoucher(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        try {
            List jasperPrint = accExportReportsServiceDAOobj.exportPaymentReceipt(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRPdfExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }   
    
    public void exportLSHGroupReceipt(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        try {
            
            List jasperPrint = accExportReportsServiceDAOobj.exportLSHPaymentReceipt(request, response);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRPdfExporterParameter.JASPER_PRINT_LIST, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public ModelAndView getAdvanceCustomerPaymentForRefunds(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accReceiptServiceDAOobj.getAdvanceCustomerPaymentForRefunds(paramJobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getLinkedInvoicesAgainstAdvance(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean isSuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("dateformat", authHandler.getDateOnlyFormat(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("bills"))) {
                requestParams.put("bills", request.getParameterValues("bills"));
            }

            jArr = getLinkedInvoicesAgainstAdvanceJSON(requestParams);
            isSuccess = true;
            jObj.put("data", jArr);

        } catch (SessionExpiredException e) {
            msg = e.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception e) {
            msg = e.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                jObj.put("success", isSuccess);
                jObj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }

    public JSONArray getLinkedInvoicesAgainstAdvanceJSON(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray jArr = new JSONArray();
        String currencyId = (String) requestParams.get("gcurrencyid");
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyId);
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        String[] receiptid = null;
        String companyid = (String) requestParams.get("companyid");
        DateFormat df = (DateFormat) requestParams.get("dateformat");
        try {
            if (requestParams.containsKey("bills")) {
                receiptid = (String[]) requestParams.get("bills");
            }
            for (int index = 0; index < receiptid.length; index++) {
                KwlReturnObject receiptResult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid[index]);
                Receipt receipt = (Receipt) receiptResult.getEntityList().get(0);
                Set<Invoice> invoiceSet = new HashSet<Invoice>();
                Set<LinkDetailReceipt> linkDetailReceipt = receipt.getLinkDetailReceipts();
                for (LinkDetailReceipt LDR : linkDetailReceipt) {
                    if (!invoiceSet.contains(LDR.getInvoice())) {
                        JSONObject jObj = new JSONObject();
                        Date invoiceCreationDate = null;
                        invoiceCreationDate = LDR.getInvoice().getCreationDate();
//                        if (LDR.getInvoice().isNormalInvoice()) {
//                            invoiceCreationDate = LDR.getInvoice().getJournalEntry().getEntryDate();
//                        } else {
//                            invoiceCreationDate = LDR.getInvoice().getCreationDate();
//                        }
                        List ll;
                        double amountdueInInvoiceCurrency = 0.0;
                        if (LDR.getInvoice().isNormalInvoice()) {
                            if (Constants.InvoiceAmountDueFlag) {
                                ll = accInvoiceCommon.getInvoiceDiscountAmountInfo(requestParams, LDR.getInvoice());
                            } else {
                                ll = accInvoiceCommon.getAmountDue_Discount(requestParams, LDR.getInvoice());
                            }
                            amountdueInInvoiceCurrency = (Double) ll.get(3);
                        } else {
                            amountdueInInvoiceCurrency = LDR.getInvoice().getOpeningBalanceAmountDue() * (LDR.getExchangeRateForTransaction() == 0 ? 1 : LDR.getExchangeRateForTransaction());
                        }
                        amountdueInInvoiceCurrency += LDR.getAmountInInvoiceCurrency();
                        amountdueInInvoiceCurrency = authHandler.round(amountdueInInvoiceCurrency, companyid);
                        jObj.put("billid", LDR.getInvoice().getID());
                        jObj.put("linkdetailid", LDR.getID());
                        jObj.put("billno", LDR.getInvoice().getInvoiceNumber());
                        jObj.put("transectionno", LDR.getInvoice().getInvoiceNumber());
                        jObj.put("invoicedate", df.format(invoiceCreationDate));
                        jObj.put("amountDueOriginal", amountdueInInvoiceCurrency);
                        jObj.put("exchangeratefortransaction", LDR.getExchangeRateForTransaction());
                        double amountdueInPaymentCurrency = authHandler.round(amountdueInInvoiceCurrency * LDR.getExchangeRateForTransaction(), companyid);
                        jObj.put("amountdue", amountdueInPaymentCurrency);
                        jObj.put("invamount", LDR.getAmount());
                        jObj.put("currencysymbol", LDR.getInvoice().getCurrency() == null ? (LDR.getReceipt().getCurrency() == null ? currency.getSymbol() : LDR.getReceipt().getCurrency().getSymbol()) : LDR.getInvoice().getCurrency().getSymbol());
                        jObj.put("currencysymboltransaction", LDR.getInvoice().getCurrency() == null ? (LDR.getReceipt().getCurrency() == null ? currency.getSymbol() : LDR.getReceipt().getCurrency().getSymbol()) : LDR.getInvoice().getCurrency().getSymbol() );
                        jObj.put("currencysymbolpayment", LDR.getReceipt().getCurrency().getSymbol());
                        jArr.put(jObj);

                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return jArr;
    }
    
    public ModelAndView getDNLinkedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getDNLinkedInTransaction(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accGoodsReceiptControllerCMN.getGRLinkedInTransaction:" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accGoodsReceiptControllerCMN.getGRLinkedInTransaction:" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getDNLinkedInTransaction(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String noteId = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String isFixedAsset = request.getParameter("isFixedAsset");
            String cntype = request.getParameter("cntype");
            DateFormat df = authHandler.getDateOnlyFormat();

            if (!StringUtil.isNullOrEmpty(noteId)) {

                KwlReturnObject dnresult = accReceiptDAOobj.getPaymentIdLinkedWithNote(noteId);
                List listc = dnresult.getEntityList();
//                dnresult = accReceiptDAOobj.getAdvanceReceiptIdLinkedWithNote(noteId);
//                List listAdvance = dnresult.getEntityList();
//                listc.addAll(listAdvance);
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
                    obj.put("companyid", receipt.getCompany().getCompanyID());
                    obj.put("companyname", receipt.getCompany().getCompanyName());
                    obj.put("withoutinventory", "");
                    obj.put("transactionNo", receipt.getReceiptNumber());   //Receipt no
//                    obj.put("date", (receipt.isIsOpeningBalenceReceipt()) ? (df.format(receipt.getCreationDate())) : df.format(receipt.getJournalEntry().getEntryDate()));  //date of delivery order
                    obj.put("date", df.format(receipt.getCreationDate()));  //date of delivery order
//                    obj.put("linkingdate", (receipt.isIsOpeningBalenceReceipt()) ? (df.format(receipt.getCreationDate())) : df.format(receipt.getJournalEntry().getEntryDate()));  //date of delivery order
                    obj.put("linkingdate", df.format(receipt.getCreationDate()));  //date of delivery order
                    obj.put("journalEntryNo", jeNumbers);  //journal entry no
                    obj.put("mergedCategoryData", "Payment Receipt");  //type of data
                    
                    if (receipt.getVendor() != null) {
                        String vendorid = receipt.getVendor();
                        KwlReturnObject objItr2 = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorid);
                        Vendor vendor = (Vendor) objItr2.getEntityList().get(0);
                        obj.put("personname", vendor.getName());
                        obj.put("personid", vendor.getID());

                    } else {
                        Customer custid = receipt.getCustomer();
                        obj.put("personname", custid.getName());
                        obj.put("personid", custid.getID());

                    }
                String jeNumber=receipt.isIsOpeningBalenceReceipt()?"":receipt.getJournalEntry().getEntryNumber();
                String jeIds=receipt.isIsOpeningBalenceReceipt()?"":receipt.getJournalEntry().getID();
                String jeIdEntryDate = receipt.isIsOpeningBalenceReceipt()?"":df.format(receipt.getJournalEntry().getEntryDate());
                obj.put("isOpeningBalanceTransaction", receipt.isIsOpeningBalenceReceipt());
                obj.put("isNormalTransaction", receipt.isNormalReceipt());
                obj.put("companyid", receipt.getCompany().getCompanyID());
                obj.put("companyname", receipt.getCompany().getCompanyName());
                obj.put("entryno", jeNumber);
                obj.put("journalentryid",jeIds);
                obj.put("journalentrydate",jeIdEntryDate);
                obj.put(GoodsReceiptCMNConstants.DELETED, receipt.isDeleted());
//                obj.put("customervendorname", (customer!=null)? customer.getName() : (vendor!=null)? vendor.getName():"");
                obj.put("billno", receipt.getReceiptNumber());
                obj.put("isadvancepayment", receipt.isIsadvancepayment());
                obj.put("isadvancefromvendor", receipt.isIsadvancefromvendor());
                obj.put("ismanydbcr", receipt.isIsmanydbcr());
                obj.put("isprinted", receipt.isPrinted());
                obj.put("isDishonouredCheque", receipt.isIsDishonouredCheque());
                obj.put("bankCharges", receipt.getBankChargesAmount());
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
                    jArr.put(obj);
                }

                dnresult = accReceiptDAOobj.getAdvanceReceiptIdLinkedWithNote(noteId);
                List listAdvance = dnresult.getEntityList();
//                listc.addAll(listAdvance);
//                Iterator itr1 = listAdvance.iterator();
               for (int index = 0; index < listAdvance.size(); index++) {
                    Object[] linkedRPObj = (Object[]) listAdvance.get(index);
                    String orderid = (String) linkedRPObj[0];
                    //Date linkdate = new Date(Long.parseLong(linkedRPObj[1].toString()));
                    Date linkdate = (Date)linkedRPObj[1];
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
                    obj.put("companyid", receipt.getCompany().getCompanyID());
                    obj.put("companyname", receipt.getCompany().getCompanyName());
                    obj.put("withoutinventory", "");
                    obj.put("transactionNo", receipt.getReceiptNumber());   //Receipt no
//                    obj.put("date", (receipt.isIsOpeningBalenceReceipt()) ? (df.format(receipt.getCreationDate())) : df.format(receipt.getJournalEntry().getEntryDate()));  //date of delivery order
                    obj.put("date", df.format(receipt.getCreationDate()));  //date of delivery order
                    obj.put("journalEntryNo", jeNumbers);  //journal entry no
                    obj.put("linkingdate", df.format(linkdate));  //journal entry no
                    obj.put("mergedCategoryData", "Payment Receipt");  //type of data
                    
                    if (receipt.getVendor() != null) {
                        String vendorid = receipt.getVendor();
                        KwlReturnObject objItr2 = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorid);
                        Vendor vendor = (Vendor) objItr2.getEntityList().get(0);
                        obj.put("personname", vendor.getName());
                        obj.put("personid", vendor.getID());

                    } else {
                        Customer custid = receipt.getCustomer();
                        obj.put("personname", custid.getName());
                        obj.put("personid", custid.getID());

                    }
                String jeNumber=receipt.isIsOpeningBalenceReceipt()?"":receipt.getJournalEntry().getEntryNumber();
                String jeIds=receipt.isIsOpeningBalenceReceipt()?"":receipt.getJournalEntry().getID();
                String jeIdEntryDate = receipt.isIsOpeningBalenceReceipt()?"":df.format(receipt.getJournalEntry().getEntryDate());
                obj.put("isOpeningBalanceTransaction", receipt.isIsOpeningBalenceReceipt());
                obj.put("isNormalTransaction", receipt.isNormalReceipt());
                obj.put("companyid", receipt.getCompany().getCompanyID());
                obj.put("companyname", receipt.getCompany().getCompanyName());
                obj.put("entryno", jeNumber);
                obj.put("journalentryid",jeIds);
                obj.put("journalentrydate",jeIdEntryDate);
//                obj.put("customervendorname", (customer!=null)? customer.getName() : (vendor!=null)? vendor.getName():"");
                obj.put("billno", receipt.getReceiptNumber());
                obj.put("isadvancepayment", receipt.isIsadvancepayment());
                obj.put("isadvancefromvendor", receipt.isIsadvancefromvendor());
                obj.put("ismanydbcr", receipt.isIsmanydbcr());
                obj.put("isprinted", receipt.isPrinted());
                obj.put("isDishonouredCheque", receipt.isIsDishonouredCheque());
                obj.put("bankCharges", receipt.getBankChargesAmount());
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
                    jArr.put(obj);
                }
               KwlReturnObject invresult=null;

                if(cntype.equals("5")){
                    invresult = accInvoiceDAOobj.getInvoiceLinkedWithCreditNote(noteId, companyid);
                } else if (cntype.equals(""+Constants.DebitNoteForOvercharge)) {
                    invresult = accInvoiceDAOobj.getPurchaseInvoiceLinkedWithOverchargeDebitNote(noteId, companyid);
                }else{
                    invresult = accInvoiceDAOobj.getPurchaseInvoiceLinkedWithNote(noteId, companyid);
                }
                
//                KwlReturnObject invresult = accReceiptDAOobj.getPaymentIdLinkedWithNote(noteId);
                List invlist = invresult.getEntityList();
//                Iterator invitr = invlist.iterator();
//                while (invitr.hasNext()) {
                for (int index = 0; index < invlist.size(); index++) {
                    Object[] linkedPIObj = (Object[]) invlist.get(index);
                    String invorderid = (String) linkedPIObj[0];
//                    String invorderid = (String) invitr.next();

                    JSONObject invobj = new JSONObject();
                    KwlReturnObject invobjItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invorderid);
                    GoodsReceipt goodsreceipt = (GoodsReceipt) invobjItr.getEntityList().get(0);

                    Vendor vendor = goodsreceipt.getVendor();
                    invobj.put("billid", goodsreceipt.getID());
                    invobj.put("companyid", goodsreceipt.getCompany().getCompanyID());
                    invobj.put("companyname", goodsreceipt.getCompany().getCompanyName());
                    invobj.put("withoutinventory", "");
                    invobj.put("transactionNo", goodsreceipt.getGoodsReceiptNumber());   //delivery order no
                    invobj.put("duedate", goodsreceipt.getDueDate() != null ? df.format(goodsreceipt.getDueDate()) : "");
                    if (cntype.equals("5") || cntype.equals(""+Constants.DebitNoteForOvercharge)) {
                        invobj.put("linkingdate", df.format(new Date(goodsreceipt.getCreatedon())));
                    } else {
                        Date linkdate = (Date)linkedPIObj[1];
                        invobj.put("linkingdate", df.format(linkdate));
                    }
                    if (goodsreceipt.getJournalEntry() != null) {
//                        invobj.put("date", df.format(goodsreceipt.getJournalEntry().getEntryDate()));
                        invobj.put("date", df.format(goodsreceipt.getCreationDate()));
                        invobj.put("journalEntryId", goodsreceipt.getJournalEntry().getID());
                        invobj.put("journalEntryNo", goodsreceipt.getJournalEntry().getEntryNumber());  //journal entry no
                    }else if(goodsreceipt.isIsOpeningBalenceInvoice()==true){
                        invobj.put("date", df.format(goodsreceipt.getCreationDate()));  //date of invoice
                    }else{
                        Date tempdate = new Date(goodsreceipt.getCreatedon());
                        invobj.put("date", df.format(tempdate));  //date of invoice
                    }
                    if (goodsreceipt.isIsconsignment()) {
                        invobj.put("mergedCategoryData", "Consignment Vendor Invoice");  //type of data
                    } else if (goodsreceipt.isFixedAssetInvoice()) {
                        invobj.put("mergedCategoryData", "Fixed Asset Acquired Invoice");  //type of data
                    } else {
                        invobj.put("mergedCategoryData", "Vendor Invoice");  //type of data
                    }
                    invobj.put("personname", vendor.getName());
                    invobj.put("personid", vendor.getID());
                    double invoiceOriginalAmt = 0d;
                    JournalEntry je = null;
                    JournalEntryDetail d = null;
                    if (goodsreceipt.isNormalInvoice()) {
                        je = goodsreceipt.getJournalEntry();
                        d = goodsreceipt.getVendorEntry();
                    }
                    double externalCurrencyRate = 0d;
                    boolean isopeningBalanceInvoice = goodsreceipt.isIsOpeningBalenceInvoice();
                    Date creationDate = null;
                    String currencyid = goodsreceipt.getCurrency().getCurrencyID();
                    KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
                    KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
                    Account account = null;
                    creationDate = goodsreceipt.getCreationDate();
                    if (goodsreceipt.isIsOpeningBalenceInvoice() && !goodsreceipt.isNormalInvoice()) {
                        KwlReturnObject accObjItr = accountingHandlerDAOobj.getObject(Account.class.getName(), goodsreceipt.getVendor().getAccount().getID());
                        account = (Account) accObjItr.getEntityList().get(0);
                        externalCurrencyRate = goodsreceipt.getExchangeRateForOpeningTransaction();
                        invoiceOriginalAmt = goodsreceipt.getOriginalOpeningBalanceAmount();
                    } else {
                        account = d.getAccount();
                        externalCurrencyRate = je.getExternalCurrencyRate();
//                        creationDate = je.getEntryDate();
                        invoiceOriginalAmt = d.getAmount();
                    }
                    invobj.put("isOpeningBalanceTransaction", goodsreceipt.isIsOpeningBalenceInvoice());
                    invobj.put("isNormalTransaction", goodsreceipt.isNormalInvoice());
                    invobj.put("parentinvoiceid", goodsreceipt.getParentInvoice() != null ? goodsreceipt.getParentInvoice().getID() : "");
                    invobj.put("companyid", goodsreceipt.getCompany().getCompanyID());
                    invobj.put("companyname", goodsreceipt.getCompany().getCompanyName());
                    invobj.put(GoodsReceiptCMNConstants.PERSONID, vendor == null ? account.getID() : vendor.getID());
                    invobj.put(GoodsReceiptCMNConstants.ALIASNAME, vendor == null ? "" : vendor.getAliasname());
                    invobj.put(GoodsReceiptCMNConstants.PERSONEMAIL, vendor == null ? "" : vendor.getEmail());
                    invobj.put(GoodsReceiptCMNConstants.BILLNO, goodsreceipt.getGoodsReceiptNumber());
                    invobj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyid);
                    invobj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, (goodsreceipt.getCurrency() == null ? currency.getSymbol() : goodsreceipt.getCurrency().getSymbol()));
                    invobj.put("currencyCode", (goodsreceipt.getCurrency() == null ? currency.getCurrencyCode() : goodsreceipt.getCurrency().getCurrencyCode()));
                    invobj.put("currencycode", (goodsreceipt.getCurrency() == null ? currency.getCurrencyCode() : goodsreceipt.getCurrency().getCurrencyCode()));
                    invobj.put(GoodsReceiptCMNConstants.CURRENCYNAME, (goodsreceipt.getCurrency() == null ? currency.getName() : goodsreceipt.getCurrency().getName()));
                    invobj.put(GoodsReceiptCMNConstants.COMPANYADDRESS, goodsreceipt.getCompany().getAddress());
                    invobj.put(GoodsReceiptCMNConstants.COMPANYNAME, goodsreceipt.getCompany().getCompanyName());
                    invobj.put(GoodsReceiptCMNConstants.BILLTO, goodsreceipt.getBillFrom());
                    invobj.put(GoodsReceiptCMNConstants.ISEXPENSEINV, goodsreceipt.isIsExpenseType());
                    invobj.put(GoodsReceiptCMNConstants.SHIPTO, goodsreceipt.getShipFrom());
                    invobj.put(GoodsReceiptCMNConstants.JOURNALENTRYID, je != null ? je.getID() : "");
                    invobj.put(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE, externalCurrencyRate);
                    invobj.put(GoodsReceiptCMNConstants.ENTRYNO, je != null ? je.getEntryNumber() : "");
                    invobj.put(GoodsReceiptCMNConstants.DATE, df.format(creationDate));
                    invobj.put(GoodsReceiptCMNConstants.SHIPDATE, goodsreceipt.getShipDate() == null ? "" : df.format(goodsreceipt.getShipDate()));
                    invobj.put(GoodsReceiptCMNConstants.DUEDATE, df.format(goodsreceipt.getDueDate()));
                    invobj.put(GoodsReceiptCMNConstants.PERSONNAME, vendor == null ? account.getName() : vendor.getName());
                    invobj.put(GoodsReceiptCMNConstants.PERSONINFO, vendor == null ? account.getName() : vendor.getName()+"("+vendor.getAcccode()+")");
                    invobj.put("personcode", vendor == null ? (account.getAcccode() == null ? "" : account.getAcccode()) : (vendor.getAcccode() == null ? "" : vendor.getAcccode()));
                    invobj.put("agent", goodsreceipt.getMasterAgent() == null ? "" : goodsreceipt.getMasterAgent().getID());
                    invobj.put(GoodsReceiptCMNConstants.MEMO, goodsreceipt.getMemo());
                    invobj.put("posttext", goodsreceipt.getPostText());
                    invobj.put("shiplengthval", goodsreceipt.getShiplength());
                    invobj.put("invoicetype", goodsreceipt.getInvoicetype());
                    invobj.put(GoodsReceiptCMNConstants.TERMNAME, vendor == null ? "" : ((vendor.getDebitTerm() == null) ? "" : vendor.getDebitTerm().getTermname()));
                    invobj.put(GoodsReceiptCMNConstants.DELETED, goodsreceipt.isDeleted());
                    invobj.put(GoodsReceiptCMNConstants.TAXINCLUDED, goodsreceipt.getTax() == null ? false : true);
                    invobj.put(GoodsReceiptCMNConstants.TAXID, goodsreceipt.getTax() == null ? "" : goodsreceipt.getTax().getID());
                    invobj.put(GoodsReceiptCMNConstants.TAXNAME, goodsreceipt.getTax() == null ? "" : goodsreceipt.getTax().getName());
                    invobj.put(GoodsReceiptCMNConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + externalCurrencyRate + " " + (goodsreceipt.getCurrency() == null ? "" : goodsreceipt.getCurrency().getCurrencyCode()));
                    double taxAmt = 0d;
                    if (goodsreceipt.getTaxEntry() != null) {// if Invoice Level Tax is available
                        taxAmt = goodsreceipt.getTaxEntry() == null ? 0 : goodsreceipt.getTaxEntry().getAmount();
//                            obj.put(GoodsReceiptCMNConstants.TAXAMOUNT, gReceipt.getTaxEntry() == null ? 0 : gReceipt.getTaxEntry().getAmount());
                    }
                    //                        obj.put("amountbeforegst", gReceipt.getTaxEntry() == null ? invoiceOriginalAmt : (invoiceOriginalAmt - gReceipt.getTaxEntry().getAmount()));
                    invobj.put(GoodsReceiptCMNConstants.DISCOUNT, goodsreceipt.getDiscount() == null ? 0 : goodsreceipt.getDiscount().getDiscountValue());
                    invobj.put(GoodsReceiptCMNConstants.ISPERCENTDISCOUNT, goodsreceipt.getDiscount() == null ? false : goodsreceipt.getDiscount().isInPercent());
                    invobj.put(GoodsReceiptCMNConstants.DISCOUNTVAL, goodsreceipt.getDiscount() == null ? 0 : goodsreceipt.getDiscount().getDiscount());
                    invobj.put(CCConstants.JSON_costcenterid, je != null ? (je.getCostcenter() == null ? "" : je.getCostcenter().getID()) : "");
                    invobj.put(CCConstants.JSON_costcenterName, je != null ? (je.getCostcenter() == null ? "" : je.getCostcenter().getName()) : "");
                    invobj.put("isfavourite", goodsreceipt.isFavourite());
                    invobj.put("isprinted", goodsreceipt.isPrinted());
                    invobj.put("cashtransaction", goodsreceipt.isCashtransaction());
                    invobj.put("archieve", 0);
                    invobj.put("shipvia", goodsreceipt.getShipvia() == null ? "" : goodsreceipt.getShipvia());
                    invobj.put("fob", goodsreceipt.getFob() == null ? "" : goodsreceipt.getFob());
                    if (goodsreceipt.getTermsincludegst() != null) {
                        invobj.put(Constants.termsincludegst, goodsreceipt.getTermsincludegst());
                    }
                    invobj=AccountingAddressManager.getTransactionAddressJSON(invobj,goodsreceipt.getBillingShippingAddresses(),true);
                    invobj.put("termdays", goodsreceipt.getTermid() == null ? 0 : goodsreceipt.getTermid().getTermdays());
                    invobj.put("termid", goodsreceipt.getTermid() == null ? "" : goodsreceipt.getTermid().getID());
                    if (goodsreceipt.getLandedInvoice() != null) {
                        Set<GoodsReceipt> landInvoiceSet = goodsreceipt.getLandedInvoice();
                        String landedInvoiceId = "", landedInvoiceNumber = "";
                        for (GoodsReceipt grObj : landInvoiceSet) {
                            if (!(StringUtil.isNullOrEmpty(landedInvoiceId) && StringUtil.isNullOrEmpty(landedInvoiceId))) {
                                landedInvoiceId += ",";
                                landedInvoiceNumber += ",";
                            }
                            landedInvoiceId += grObj.getID();
                            landedInvoiceNumber += grObj.getGoodsReceiptNumber();
                        }
                        invobj.put("landedInvoiceID", landedInvoiceId);
                        invobj.put("landedInvoiceNumber", landedInvoiceNumber);
                    }
                    //                    invobj.put("landedInvoiceID", goodsreceipt.getLandedInvoice() == null ? "" : goodsreceipt.getLandedInvoice().getID());
//                    invobj.put("landedInvoiceNumber", goodsreceipt.getLandedInvoice() == null ? "" : goodsreceipt.getLandedInvoice().getGoodsReceiptNumber());
                    invobj.put("billto", goodsreceipt.getBillTo() == null ? "" : goodsreceipt.getBillTo());
                    invobj.put("shipto", goodsreceipt.getShipTo() == null ? "" : goodsreceipt.getShipTo());
                    invobj.put("isCapitalGoodsAcquired", goodsreceipt.isCapitalGoodsAcquired());
                    invobj.put("isRetailPurchase", goodsreceipt.isRetailPurchase());
                    invobj.put("importService", goodsreceipt.isImportService());
                    
                    /* Putting Amount & Amount in base currency in Json if invoice is opening*/
                    invobj.put("isOpeningBalanceTransaction", goodsreceipt.isIsOpeningBalenceInvoice());
                    Map<String, Object> requestParams = new HashMap<>();
                    requestParams.put(Constants.df, df);
                    requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
                    requestParams.put(Constants.companyKey, companyid);

                    if (goodsreceipt.isIsOpeningBalenceInvoice()) {
                        invoiceOriginalAmt = goodsreceipt.getOriginalOpeningBalanceAmount();
                    } else {
                        invoiceOriginalAmt = goodsreceipt.getVendorEntry().getAmount();
                    }

                    KwlReturnObject invoiceTotalAmtInBaseResult = null;
                    if (goodsreceipt.isIsOpeningBalenceInvoice() && goodsreceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        invoiceTotalAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, invoiceOriginalAmt, currencyid, creationDate, externalCurrencyRate);
                    } else {
                        invoiceTotalAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoiceOriginalAmt, currencyid, creationDate, externalCurrencyRate);
                    }
                    double invoiceTotalAmountInBase = authHandler.round((Double) invoiceTotalAmtInBaseResult.getEntityList().get(0), companyid);
                    invobj.put("amount", invoiceOriginalAmt);
                    invobj.put("amountinbase", invoiceTotalAmountInBase);
              
                    /* Code for dispalying tax amount of PI in view mode from DN linking report*/
                    boolean includeprotax = false;

                    if (!goodsreceipt.isIsExpenseType() && goodsreceipt.isNormalInvoice()) {

                        Set<GoodsReceiptDetail> goodsReceiptDetails = goodsreceipt.getRows();
                        for (GoodsReceiptDetail goodsReceiptDetail : goodsReceiptDetails) {
                            if (goodsReceiptDetail.getTax() != null) {
                                includeprotax = true;
                                break;
                            }
                        }
                    } else if (goodsreceipt.isIsExpenseType()) {
                        Set<ExpenseGRDetail> expenseGRDetails = goodsreceipt.getExpenserows();
                        for (ExpenseGRDetail expGReceiptDetail : expenseGRDetails) {

                            if (expGReceiptDetail.getTax() != null) {
                                includeprotax = true;
                                break;
                            }
                        }
                    }

                    invobj.put("includeprotax", includeprotax);
                    jArr.put(invobj);
                }
                              
                invresult = accInvoiceDAOobj.getCreditNoteLinkedWithDebitNote(noteId, companyid);
                List cnlist = invresult.getEntityList();
//                Iterator cnitr = cnlist.iterator();
//                while (cnitr.hasNext()) {
//                    String cnid = (String) cnitr.next();
                for (int index = 0; index < cnlist.size(); index++) {
                    Object[] linkedDNObj = (Object[]) cnlist.get(index);
                    String cnid =  (String) linkedDNObj[0];
                    Date linkdate = (Date)linkedDNObj[1];

                    JSONObject cnobj = new JSONObject();
                    KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnid);
                    CreditNote creditNote= (CreditNote) cnObj.getEntityList().get(0);

                    Vendor vendor = creditNote.getVendor();
                    cnobj.put("billid", creditNote.getID());
                    cnobj.put("noteid", creditNote.getID());
                    cnobj.put("companyid", creditNote.getCompany().getCompanyID());
                    cnobj.put("companyname", creditNote.getCompany().getCompanyName());
                    cnobj.put("withoutinventory", "");
                    cnobj.put("transactionNo", creditNote.getCreditNoteNumber());   //delivery order no
                    if (creditNote.getJournalEntry() != null) {
//                        cnobj.put("date", df.format(creditNote.getJournalEntry().getEntryDate()));
                        cnobj.put("date", df.format(creditNote.getCreationDate()));
                        cnobj.put("journalEntryId", creditNote.getJournalEntry().getID());
                        cnobj.put("journalEntryNo", creditNote.getJournalEntry().getEntryNumber());  //journal entry no
                    }else if(creditNote.isIsOpeningBalenceCN()==true){
                        cnobj.put("date", df.format(creditNote.getCreationDate()));  //date of invoice
                    }else{
                        Date tempdate = new Date(creditNote.getCreatedon());
                        cnobj.put("date", df.format(tempdate));  //date of invoice
                    }
                    cnobj.put("linkingdate",df.format(linkdate));
                    cnobj.put("mergedCategoryData", "Credit Note");  //type of data
                    cnobj.put("personname", vendor == null ? "" : vendor.getName());
                    cnobj.put("personid", vendor == null ? "" : vendor.getID());
                    double invoiceOriginalAmt = 0d;
                    JournalEntry je = null;
                    JournalEntryDetail d = null;
                    if (creditNote.isNormalCN()) {
                        je = creditNote.getJournalEntry();
                        d = creditNote.getCustomerEntry();
                    }
                    double externalCurrencyRate = 0d;
                    Date creationDate = null;
                    String currencyid = creditNote.getCurrency().getCurrencyID();
                    KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
                    KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
                    Account account = null;
                    creationDate = creditNote.getCreationDate();
                    if (creditNote.isIsOpeningBalenceCN() && !creditNote.isNormalCN()) {
                        String accountID = "";
                        if (creditNote.getVendor() != null) {
                            accountID = creditNote.getVendor().getAccount().getID();
                        } else {
                            accountID = creditNote.getCustomer().getAccount().getID();
                        }
                        KwlReturnObject accObjItr = accountingHandlerDAOobj.getObject(Account.class.getName(), accountID);
                        account = (Account) accObjItr.getEntityList().get(0);
                        creationDate = creditNote.getCreationDate();
                        externalCurrencyRate = creditNote.getExchangeRateForOpeningTransaction();
                        invoiceOriginalAmt = creditNote.getCnamount();
                    } else {
                        account = d.getAccount();
                        externalCurrencyRate = je.getExternalCurrencyRate();
//                        creationDate = je.getEntryDate();
                        invoiceOriginalAmt = d.getAmount();
                    }
                    cnobj.put("accid", account != null ? account.getID() : "");
                    cnobj.put("accountid", creditNote.getAccount() == null ? "" : creditNote.getAccount().getID());
                    cnobj.put("isOpeningBalanceTransaction", creditNote.isIsOpeningBalenceCN());
                    cnobj.put("isNormalTransaction", creditNote.isNormalCN());
                    cnobj.put("companyid", creditNote.getCompany().getCompanyID());
                    cnobj.put("companyname", creditNote.getCompany().getCompanyName());
                    cnobj.put("noteno", creditNote.getCreditNoteNumber());
                    cnobj.put("cntype", creditNote.getCntype());
                    cnobj.put(Constants.SEQUENCEFORMATID, creditNote.getSeqformat() != null ? creditNote.getSeqformat().getID() : "");
                    cnobj.put("includingGST", creditNote.isIncludingGST());
                    cnobj.put(GoodsReceiptCMNConstants.ALIASNAME, vendor == null ? "" : vendor.getAliasname());
                    cnobj.put(GoodsReceiptCMNConstants.PERSONEMAIL, vendor == null ? "" : vendor.getEmail());
                    cnobj.put(GoodsReceiptCMNConstants.BILLNO, creditNote.getCreditNoteNumber());
                    cnobj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyid);
                    cnobj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, (creditNote.getCurrency() == null ? currency.getSymbol() : creditNote.getCurrency().getSymbol()));
                    cnobj.put("currencyCode", (creditNote.getCurrency() == null ? currency.getCurrencyCode() : creditNote.getCurrency().getCurrencyCode()));
                    cnobj.put("currencycode", (creditNote.getCurrency() == null ? currency.getCurrencyCode() : creditNote.getCurrency().getCurrencyCode()));
                    cnobj.put(GoodsReceiptCMNConstants.CURRENCYNAME, (creditNote.getCurrency() == null ? currency.getName() : creditNote.getCurrency().getName()));
                    cnobj.put(GoodsReceiptCMNConstants.COMPANYADDRESS, creditNote.getCompany().getAddress());
                    cnobj.put(GoodsReceiptCMNConstants.COMPANYNAME, creditNote.getCompany().getCompanyName());
                    cnobj.put(GoodsReceiptCMNConstants.JOURNALENTRYID, je != null ? je.getID() : "");
                    cnobj.put(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE, externalCurrencyRate);
                    cnobj.put(GoodsReceiptCMNConstants.ENTRYNO, je != null ? je.getEntryNumber() : "");
                    cnobj.put(GoodsReceiptCMNConstants.DATE, df.format(creationDate));
                    cnobj.put("agent", creditNote.getSalesPerson() == null ? "" : creditNote.getSalesPerson().getID());
                    cnobj.put(GoodsReceiptCMNConstants.MEMO, creditNote.getMemo());
                    cnobj.put(GoodsReceiptCMNConstants.TERMNAME, vendor == null ? "" : ((vendor.getDebitTerm() == null) ? "" : vendor.getDebitTerm().getTermname()));
                    cnobj.put(GoodsReceiptCMNConstants.DELETED, creditNote.isDeleted());
                    cnobj.put(GoodsReceiptCMNConstants.ExchangeRate, "1 " + currency.getCurrencyCode() + " = " + externalCurrencyRate + " " + (creditNote.getCurrency() == null ? "" : creditNote.getCurrency().getCurrencyCode()));
                    cnobj.put(CCConstants.JSON_costcenterid, je != null ? (je.getCostcenter() == null ? "" : je.getCostcenter().getID()) : "");
                    cnobj.put(CCConstants.JSON_costcenterName, je != null ? (je.getCostcenter() == null ? "" : je.getCostcenter().getName()) : "");
                    cnobj.put("isprinted", creditNote.isPrinted());
                    cnobj.put("archieve", 0);
                    cnobj=AccountingAddressManager.getTransactionAddressJSON(cnobj,creditNote.getBillingShippingAddresses(),true);
                    jArr.put(cnobj);
                }
                
                DateFormat userdf = (DateFormat) authHandler.getUserDateFormatter(request);
                KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), AccountingManager.getGlobalCurrencyidFromRequest(request));
                KWLCurrency currency = (KWLCurrency) curresult1.getEntityList().get(0);
                HashMap requestparams = new HashMap();
                requestparams.put("noteId", noteId);
                requestparams.put("companyid", companyid);

                /*
                 * Getting Purchase Return linked in Debit Note
                 */
                KwlReturnObject result = accInvoiceDAOobj.getPurchaseReturnLinkedInDebitNote(requestparams);
                List invoiceList = result.getEntityList();
                if (invoiceList != null && invoiceList.size() > 0) {

                    jArr = accReceiptServiceDAOobj.getPurchaseReturnJson(jArr, invoiceList, currency, userdf, companyid);
                }

                jobj.put("count", jArr.length());
                jobj.put("data", jArr);

            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }

    public ModelAndView getAllInvoicesAndDebitNoteAgainstCustomerForPayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        boolean maxLimitReached = false;
        try {
            JSONArray DataJArr = new JSONArray();
            String paymentOptionString = request.getParameter("paymentOption");

            int paymentOption = !StringUtil.isNullOrEmpty(paymentOptionString) ? Integer.parseInt(paymentOptionString) : 0;
            if (paymentOption == 1) {//1 :- Against Customer
                DataJArr = getAllInvoicesAgainstCustomerForPayment(request, DataJArr); //get Invoices
            } else if (paymentOption == 2) {// 2 Against Vendor
                getAdvanceVendorPaymentForRefunds(request, DataJArr);//get deposit/refund payments
            }
            if (DataJArr.length() > Constants.MaxLimitOFDocumentsInReceivePayment) {
                maxLimitReached = true;
            } else if (DataJArr.length() < Constants.MaxLimitOFDocumentsInReceivePayment) {
                maxLimitReached = false;
            }
            if (!maxLimitReached) {
                getDebitNoteMergedForPayment(request, DataJArr);//get debit Notes
                if (DataJArr.length() > Constants.MaxLimitOFDocumentsInReceivePayment) {
                    maxLimitReached = true;
                }
            }
            if (maxLimitReached) {
                jobj.put("maxLimitReached", maxLimitReached);
            } else {
                jobj.put("maxLimitReached", maxLimitReached);
            }

            jobj.put("data", DataJArr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getAllInvoicesAgainstCustomerForPayment(HttpServletRequest request, JSONArray DataJArr) {
        try {
            HashMap requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            requestParams.put("getRecordBasedOnJEDate",true);          //sending getRecordBasedOnJEDate = true because only those invoice should be fetched whose JE posting date is greater then linking date
            KwlReturnObject result = accInvoiceDAOobj.getInvoices(requestParams);
            List list = result.getEntityList();
            boolean isEdit = request.getParameter("isEdit") == null ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            request.setAttribute("isEdit", isEdit);
            HashSet invoicesList = new HashSet();
            if (isEdit && !StringUtil.isNullOrEmpty(request.getParameter("billId").toString())) {
                KwlReturnObject receiptResult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), request.getParameter("billId").toString());
                Receipt receipt = (Receipt) receiptResult.getEntityList().get(0);
                Set<ReceiptDetail> receiptDetails = receipt.getRows();
                for (ReceiptDetail receiptDetail : receiptDetails) {
                    invoicesList.add(receiptDetail.getInvoice().getID());
                }
            }
            DataJArr = getInvoiceJsonForPayment(request, list, invoicesList).getJSONArray("data");
            getOpeningBalanceInvoicesJsonArray(request, DataJArr);

        } catch (Exception ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return DataJArr;
    }

    public JSONObject getInvoiceJsonForPayment(HttpServletRequest request, List<Invoice> list, HashSet invoicesList) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            HashMap requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean onlyAmountDue = requestParams.get("onlyamountdue") != null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            String currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
            KwlReturnObject currencyFilterResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
            KWLCurrency currencyFilter = (KWLCurrency) currencyFilterResult.getEntityList().get(0);
            
            boolean CashAndInvoice = Boolean.FALSE.parseBoolean(String.valueOf(request.getParameter("CashAndInvoice")));
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String cashAccount = pref.getCashAccount().getID();

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            List<String> idsList = new ArrayList<String>();
            for (Invoice invoice : list) {
                idsList.add(invoice.getID());
            }
            int countCustom = 0;
            HashMap<String, Object> prefparams = new HashMap<>();
            prefparams.put("id", companyid);
            Object columnPref = kwlCommonTablesDAOObj.getRequestedObjectFields(IndiaComplianceCompanyPreferences.class, new String[]{"istaxonadvancereceipt"}, prefparams);
            boolean istaxonadvancereceipt = false;
            if (columnPref != null) {
                istaxonadvancereceipt = Boolean.parseBoolean(columnPref.toString());
            }
            Map<String, JournalEntryDetail> invoiceCustomerEntryMap = accInvoiceDAOobj.getInvoiceCustomerEntryList(idsList);
            for (Invoice invoice : list) {
                int count=0;
                if (request.getParameter("isEdit")!=null && (!Boolean.parseBoolean(request.getParameter("isEdit").toString()) || Boolean.parseBoolean(request.getParameter("isEdit").toString()) && !(invoicesList.contains(invoice.getID())))) {
                    String invid = invoice.getID();
                    JournalEntry je = invoice.getJournalEntry();
                    JournalEntryDetail d = invoiceCustomerEntryMap.get(invid);
//                    Date invoiceDate = je.getEntryDate();
                    Date invoiceDate = invoice.getCreationDate();
                    Date invoiceDueDate = invoice.getDueDate();
                    Account account = d.getAccount();
                    if (account.getID().equals(cashAccount) && !CashAndInvoice) {
                        continue;
                    }
                    String currencyid = (invoice.getCurrency() == null ? currency.getCurrencyID() : invoice.getCurrency().getCurrencyID());
                    List ll = new ArrayList();
                    if (Constants.InvoiceAmountDueFlag) {
                        ll = accInvoiceCommon.getInvoiceDiscountAmountInfo(requestParams, invoice);
                    }
                    double amountdue = (Double) ll.get(0);
                    double amountduefORIndia =0.0 ;
                    double amountDueOriginal = (Double) ll.get(3);

                    if (onlyAmountDue && authHandler.round(amountdue, companyid) == 0) {
                        continue;
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("documentid", invoice.getID());
                    obj.put("documentType", 2);
                    obj.put("type", "Invoice");
                    obj.put("accountid", invoice.getAccount() == null ? "" : invoice.getAccount().getID());
                    obj.put("accountnames", invoice.getAccount() == null ? "" : invoice.getAccount().getName());
                    obj.put("documentno", invoice.getInvoiceNumber());
                    obj.put("currencyid", currencyid);
                    obj.put("currencysymbol", (invoice.getCurrency() == null ? currency.getSymbol() : invoice.getCurrency().getSymbol()));
                    obj.put("currencyidtransaction", currencyid);
                    obj.put("currencysymboltransaction", (invoice.getCurrency() == null ? currency.getSymbol() : invoice.getCurrency().getSymbol()));
                    obj.put("externalcurrencyrate", je.getExternalCurrencyRate());
//                    obj.put("date", df.format(je.getEntryDate()));
                    obj.put("date", df.format(invoice.getCreationDate()));
                    obj.put("isClaimedInvoice", (invoice.getBadDebtType() == 1 || invoice.getBadDebtType() == 2));// for Malasian Company

                    if (account.getID().equals(cashAccount)) {
                        obj.put("amountdue", 0);
                        obj.put("amountdueinbase", 0);
                        obj.put("incash", true);
                    } else {
                        obj.put("amountdue", authHandler.round(amountdue, companyid));
                    }
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                    obj.put("amount", d.getAmount());   //actual invoice amount
                    obj.put("claimedDate", invoice.getDebtClaimedDate() == null ? "" : df.format(invoice.getDebtClaimedDate()));
                    obj.put("currencysymbolpayment", (currencyFilter == null ? currency.getSymbol() : currencyFilter.getSymbol()));
                    obj.put("linkingdate",df.format(new Date()));
                    
                    JSONObject jObj = null;
                    double discountValue = 0.0;
                    int applicableDays = -1;
                    boolean discountType = false;
                    if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                        jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                        if (jObj.has(Constants.DISCOUNT_ON_PAYMENT_TERMS) && jObj.get(Constants.DISCOUNT_ON_PAYMENT_TERMS) != null && jObj.optBoolean(Constants.DISCOUNT_ON_PAYMENT_TERMS, false)) {
                            Term term = invoice.getTermid();
                            if (term != null && term.getDiscountName() != null) {
                                DiscountMaster discountMaster = term.getDiscountName();
                                discountValue = discountMaster.getValue();
                                discountType = discountMaster.isDiscounttype();
                                applicableDays=term.getApplicableDays();
                            }
                        }
                    }
                    DateFormat genericDF=authHandler.getGlobalDateFormat();
                    obj.put("discountvalue", discountValue);
                    obj.put("discounttype", discountType ? Integer.parseInt(Constants.DISCOUNT_MASTER_TYPE_PERCENTAGE) : Integer.parseInt(Constants.DISCOUNT_MASTER_TYPE_FLAT));
                    obj.put("applicabledays", applicableDays);
                    obj.put("invoicecreationdate", genericDF.format(invoiceDate));
                    obj.put("invoiceduedate", genericDF.format(invoiceDueDate));

                    if(istaxonadvancereceipt && invoice.getCompany().getCountry().getID().equalsIgnoreCase(""+Constants.indian_country_id)){
                        
                        /**
                         * Check whether invoice anRCM is of RCM type
                         */
                        boolean isRCMReceipt = false;
                        boolean isRCMInvoice = invoice.isRcmapplicable();
                        KwlReturnObject receiptResult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), request.getParameter("billId").toString());
                        Receipt receipt = (Receipt) receiptResult.getEntityList().get(0);
                        if (receipt!=null && receipt.getReceiptAdvanceDetails() != null) {
                            Set<ReceiptAdvanceDetail> advanceDetails = receipt.getReceiptAdvanceDetails();
                            for (ReceiptAdvanceDetail receiptAdvanceDetail : advanceDetails) {
                                isRCMReceipt = receiptAdvanceDetail.getProduct() != null ? receiptAdvanceDetail.getProduct().isRcmApplicable() : false;
                            }
                        }
                        if (isRCMInvoice != isRCMReceipt) {
                            continue;
                        }

                    //Get Invoice Custom Data For Payment
                    accInvoiceServiceDAOObj.getInvoiceCustomDataForPayment(requestParams, obj, invoice, je);
                    
                    JSONArray productcustomarray = null;
                    JSONArray receiptdetailarray = null;
                    JSONObject json = new JSONObject();
                    countCustom=0;
                    Set<InvoiceDetail> invDetails = invoice.getRows();
                    List<ReceiptAdvanceDetail> listFRec=null;
                    if (invDetails != null && !invDetails.isEmpty()) {
                        for (InvoiceDetail invDetail : invDetails) {
                            HashMap<String, Object> requestParamsCus = new HashMap<>();
                            requestParamsCus.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                            requestParamsCus.put("companyid", invoice.getCompany());
                            count++;
                            /**
                             * Get Product Custom fields with its column number info
                             */
                            KwlReturnObject savedFilesIdResultProd = accountingHandlerDAOobj.getFieldParamsForLinkProjectToInvoice(requestParamsCus);
                            List<Object[]> savedFilesListProd = savedFilesIdResultProd.getEntityList();
                            /**
                             * Get Product custom field data
                             */
                            productcustomarray = customFieldManupulation(savedFilesListProd, invoice.getCompany().getCompanyID(), invDetail.getInventory().getProduct().getID(), Constants.Acc_Product_Master_ModuleId);
                            requestParamsCus.clear();
                            requestParamsCus.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                            requestParamsCus.put("companyid", invoice.getCompany().getCompanyID());
                            /**
                             * Get Receipt Custom fields with its column number info
                             */
                            KwlReturnObject savedFilesIdResultRec = accountingHandlerDAOobj.getFieldParamsForLinkProjectToInvoice(requestParamsCus);
                            
                            KwlReturnObject savedFilesIdResultRec1 = accountingHandlerDAOobj.getReceiptDetails(invoice.getCompany().getCompanyID(), request.getParameter("billId").toString());
                            listFRec = savedFilesIdResultRec1.getEntityList();
                            Iterator<ReceiptAdvanceDetail> nrItr = listFRec.iterator();
                            if(listFRec!=null && !listFRec.isEmpty()){
                            ReceiptAdvanceDetail nr = nrItr.next();
                            List<Object[]> savedFilesListRec = savedFilesIdResultRec.getEntityList();
                            String fielddbname = "";
                            String fieldValue = "";
                            String fielddbnameRec = "";
                            String fieldValueRec = "";
                            /**
                             * Get Receipt custom data
                             */
                            receiptdetailarray = customFieldManupulation(savedFilesListRec, invoice.getCompany().getCompanyID(), nr.getTotalJED().getID(), Constants.Acc_Receive_Payment_ModuleId);
                            for (int i = 0; i < productcustomarray.length(); i++) {
                                JSONObject jobjArray = productcustomarray.getJSONObject(i);
                                String fieldname = jobjArray.getString(Constants.Acc_custom_field);
                                if (fieldname.equalsIgnoreCase("Custom_"+Constants.HSN_SACCODE)) {
                                    fielddbname = jobjArray.getString(fieldname);
                                    fieldValue = jobjArray.getString("value");
                                    for (int j = 0; j < receiptdetailarray.length(); j++) {
                                        JSONObject jobjArrayRec = receiptdetailarray.getJSONObject(j);
                                        String fieldnameRec = jobjArrayRec.getString(Constants.Acc_custom_field);
                                        if (fieldname.equalsIgnoreCase("Custom_" + Constants.HSN_SACCODE)) {
                                            fielddbnameRec = jobjArrayRec.getString(fieldnameRec);
                                            fieldValueRec = jobjArrayRec.getString("value");
                                        }
                                        if (!StringUtil.isNullOrEmpty(fieldValue) && !StringUtil.isNullOrEmpty(fieldValueRec) && fieldValue.equals(fieldValueRec)) {
                                            /**
                                             * If Value of product HSN and Receipt HSN is same
                                             */
                                            countCustom++;
                                            break;
                                        }
                                    }

                                }

                            }
                          }

                        }
                    }
                    if (countCustom != count && (listFRec!=null&&!listFRec.isEmpty())) {
                        /**
                         * If Invoice details having different HSN numbers
                         */
                        continue;
                }
                }
                    jArr.put(obj);
                }
            }

            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getInvoiceJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }
    
    
public JSONArray customFieldManupulation(List<Object[]> savedFilesList, String companyId, String jeId,int moduleId) {
        KwlReturnObject resultObj = null;
        JSONArray array = new JSONArray();
        try {
            HashMap<String, Object> requestParamsCus = new HashMap<>();
            if (savedFilesList != null && savedFilesList.size() > 0) {
                for (Object[] custRow : savedFilesList) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("filedid", custRow[0]);
                    String strData="";
                    String strDataVal="";
                    if(moduleId==Constants.Acc_Product_Master_ModuleId){
                        requestParamsCus.clear();
                        requestParamsCus = new HashMap<>();
                        requestParamsCus.put("productId", jeId);
                        requestParamsCus.put("companyid", companyId);
                        requestParamsCus.put("colNum", Constants.Custom_column_Prefix + custRow[1].toString());
                    strData = accountingHandlerDAOobj.getCustomDataUsingColNumInv(requestParamsCus);
                    strDataVal = accountingHandlerDAOobj.getCustomDataUsingColNumVal(strData);
                    }else{
                    requestParamsCus.clear();
                    requestParamsCus = new HashMap<>();
                    requestParamsCus.put("jedId", jeId);
                    requestParamsCus.put("companyid", companyId);
                    requestParamsCus.put("colNum", Constants.Custom_column_Prefix + custRow[1].toString());
                    strData = accountingHandlerDAOobj.getCustomDataUsingColNumRec(requestParamsCus); 
                    strDataVal = accountingHandlerDAOobj.getCustomDataUsingColNumVal(strData);
                    }
                    jSONObject.put("Col" + custRow[1].toString(), strData);
                    
                    jSONObject.put(custRow[3].toString(), "Col" + custRow[1].toString());
                    jSONObject.put("fieldname", custRow[3].toString());
                    jSONObject.put("value", strDataVal);
                    jSONObject.put("xtype", "" + custRow[4].toString());
                    jSONObject.put("refcolumn_name", "Col" + custRow[5].toString());
                    array.put(jSONObject);
                    
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return array;
    }

    public JSONArray getOpeningBalanceInvoicesJsonArray(HttpServletRequest request, JSONArray DataJArr) {
        try {
            HashMap requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String accountId = request.getParameter("accid");
            if (!StringUtil.isNullOrEmpty(accountId)) {
                requestParams.put("customerid", accountId);
            }
            boolean onlyAmountDue = requestParams.get("onlyamountdue") != null;

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            KWLCurrency currencyFilter=null;
            KwlReturnObject result = accInvoiceDAOobj.getOpeningBalanceInvoicesExcludingNormalInvoices(requestParams);
            List<Invoice> list = result.getEntityList();
            for (Invoice invoice : list) {

                double amountdue = invoice.getOpeningBalanceAmountDue();
                Date invoiceCreationDate = null;
                invoiceCreationDate = invoice.getCreationDate();
                if (onlyAmountDue && authHandler.round(amountdue, companyid) == 0) {
                    continue;
                }

                String currencyid = (invoice.getCurrency() == null ? currency.getCurrencyID() : invoice.getCurrency().getCurrencyID());
                JSONObject obj = new JSONObject();

                double externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                obj.put("documentid", invoice.getID());
                obj.put("documentType", 2);
                obj.put("type", "Invoice");
                obj.put("accountid", invoice.getAccount() == null ? "" : invoice.getAccount().getID());
                obj.put("accountnames", invoice.getAccount() == null ? "" : invoice.getAccount().getName());
                obj.put("documentno", invoice.getInvoiceNumber());
                obj.put("currencyid", (invoice.getCurrency() == null ? "" : invoice.getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (invoice.getCurrency() == null ? "" : invoice.getCurrency().getSymbol()));
                obj.put("currencyidtransaction", currencyid);
                obj.put("currencysymboltransaction", (invoice.getCurrency() == null ? currency.getSymbol() : invoice.getCurrency().getSymbol()));
                obj.put("date", df.format(invoice.getCreationDate()));
                obj.put("amount", invoice.getOriginalOpeningBalanceAmount());
                String currencyFilterForTrans = "";
                if (requestParams.containsKey("currencyfilterfortrans")) {
                    currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                    KwlReturnObject currencyFilterResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                    currencyFilter = (KWLCurrency) currencyFilterResult.getEntityList().get(0);
                }

                double amountDueOriginal = amountdue;
                if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                    KwlReturnObject bAmtCurrencyFilter = null;
                    if (invoice.isIsOpeningBalenceInvoice() && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, currencyid, currencyFilterForTrans, invoiceCreationDate, externalCurrencyRate);
                    } else {
                        bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyFilterForTrans, invoice.getCreationDate(), externalCurrencyRate);
                    }
                    amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                }
                obj.put("amountdue", authHandler.round(amountdue, companyid));
                obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                obj.put("currencysymbolpayment", (currencyFilter == null ? currency.getSymbol() : currencyFilter.getSymbol()));
                obj.put("linkingdate",df.format(new Date()));
                DataJArr.put(obj);
            }
        } catch (JSONException ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return DataJArr;
    }

    //*************Debit Note****************
    /*
     * get debit note for payment
     */
    public JSONArray getDebitNoteMergedForPayment(HttpServletRequest request, JSONArray DataJArr) {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = accDebitNoteController.gettDebitNoteMap(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("isReceiptForDebitNote"))) {
                requestParams.put("isReceipt", request.getParameter("isReceiptForDebitNote"));
            }
            boolean onlyAmountDue=false; 
            if (!StringUtil.isNullOrEmpty(request.getParameter("onlyAmountDue"))) {
                onlyAmountDue=Boolean.parseBoolean(request.getParameter("onlyAmountDue"));
            }
            requestParams.put("onlyAmountDue", onlyAmountDue);
            
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            boolean eliminateflag = consolidateFlag;
            boolean isEdit = request.getParameter("isEdit") == null ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            requestParams.put("isEdit", isEdit);
            HashSet dnList = new HashSet();
            KwlReturnObject result = null;
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                boolean isNoteForPayment = false;
                boolean isVendor = false;
                if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteForPayment"))) {
                    isNoteForPayment = Boolean.parseBoolean(request.getParameter("isNoteForPayment"));
                    isVendor = Boolean.parseBoolean(request.getParameter("isVendor"));
                }
                requestParams.put("isNoteForPayment", isNoteForPayment);
                requestParams.put("isNewUI", true);
                result = accDebitNoteobj.getDebitNoteMerged(requestParams);
                if (isEdit && !StringUtil.isNullOrEmpty(request.getParameter("billId").toString())) {
                    KwlReturnObject dnResult = accDebitNoteobj.getCustomerDnPayment(request.getParameter("billId"));
                    List<Object[]> DNlist = dnResult.getEntityList();
                    for (Object[] objects : DNlist) {
                        String cnnoteid = objects[0] != null ? (String) objects[1] : "";
                        dnList.add(cnnoteid);
                    }
                }
                getDebitNotesMergedJsonForPayment(requestParams, result.getEntityList(), DataJArr, dnList, isEdit);
                int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
                /*
                 removed  isNoteForPayment   flag while fetching opening CN/DN to solve   ERP-14948
                    opening CN/DN does not load in MP/RP when Document currency and Payment method currency is different
                 */
                if (cntype == 10 || ( isVendor)) { // get Vendor Debit Note
                    result = accDebitNoteobj.getOpeningBalanceDNs(requestParams);
                    requestParams.put("cntype", 10);
                    getOpeningDebitNotesJson(requestParams, result.getEntityList(), DataJArr);
                } else if (cntype == 11 || ( !isVendor)) {// get Customer Debit Note  
                    result = accDebitNoteobj.getOpeningBalanceCustomerDNs(requestParams);
                    requestParams.put("cntype", 11);
                    getOpeningDebitNotesJson(requestParams, result.getEntityList(), DataJArr);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
        return DataJArr;
    }

    public JSONArray getDebitNotesMergedJsonForPayment(HashMap<String, Object> requestParams, List list, JSONArray JArr, HashSet dnList, boolean isEdit) throws ServiceException {
//         JSONArray JArr = new JSONArray();
        try {
            String currencyid = (String) requestParams.get("gcurrencyid");
            String transactionCurrencyId = (String) requestParams.get("gcurrencyid");
            String companyid = "";
            if (requestParams.containsKey("companyid")) {
                companyid = (String) requestParams.get("companyid");
            }
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            for (Object Obj : list) {
                Object[] row = (Object[]) Obj;
                if (!isEdit || (isEdit && !dnList.contains((String) row[1]))) { // here, (String)row[1] refers to debit note id

                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                    JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);

                    JSONObject obj = new JSONObject();
                    resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), (String) row[1]);
                    DebitNote debitMemo = (DebitNote) resultObject.getEntityList().get(0);
                    JournalEntry je = debitMemo.getJournalEntry();
                    Date debitNoteDate = null;
                    double externalCurrencyRate = 0d;
                    boolean isopeningBalanceInvoice = debitMemo.isIsOpeningBalenceDN();
                    if (debitMemo.isNormalDN()) {
                        je = debitMemo.getJournalEntry();
//                        debitNoteDate = je.getEntryDate();
                        debitNoteDate = debitMemo.getCreationDate();
                        externalCurrencyRate = je.getExternalCurrencyRate();
                    }
                    transactionCurrencyId = (debitMemo.getCurrency() == null ? currency.getCurrencyID() : debitMemo.getCurrency().getCurrencyID());
                    obj.put("currencyidtransaction", transactionCurrencyId);
                    obj.put("currencysymboltransaction", (debitMemo.getCurrency() == null ? currency.getSymbol() : debitMemo.getCurrency().getSymbol()));
                    String currencyFilterForTrans = "";
                    boolean isNoteForPayment = false;
                    if (requestParams.get("isNoteForPayment") != null) {
                        isNoteForPayment = (Boolean) requestParams.get("isNoteForPayment");
                    }
                    if (requestParams.containsKey("currencyfilterfortrans") && isNoteForPayment) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                        KwlReturnObject curresultpayment = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        KWLCurrency currencyPayment = (KWLCurrency) curresultpayment.getEntityList().get(0);
                        obj.put("currencysymbol", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                        obj.put("currencyid", (currencyPayment == null ? currency.getCurrencyID() : currencyPayment.getCurrencyID()));
                        obj.put("currencysymbolpayment", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                    } else {
                        obj.put("currencysymbol", (debitMemo.getCurrency() == null ? currency.getSymbol() : debitMemo.getCurrency().getSymbol()));
                        obj.put("currencyid", (debitMemo.getCurrency() == null ? currency.getCurrencyID() : debitMemo.getCurrency().getCurrencyID()));

                    }
                    double amountdue = debitMemo.isOtherwise() ? debitMemo.getDnamountdue() : 0;
                    double amountDueOriginal = debitMemo.isOtherwise() ? debitMemo.getDnamountdue() : 0;
                    if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !currencyFilterForTrans.equals(transactionCurrencyId)) {
                        KwlReturnObject bAmtCurrencyFilter = null;
                        bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, debitNoteDate, externalCurrencyRate);
                        amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                    }
                    KwlReturnObject baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, transactionCurrencyId, debitNoteDate, externalCurrencyRate);
                    obj.put("documentid", debitMemo.getID());
                    obj.put("documentType", 3);
                    obj.put("type", "Debit Note");
                    obj.put("documentno", debitMemo.getDebitNoteNumber());
                    obj.put("amount", debitMemo.isOtherwise() ? debitMemo.getDnamount() : details.getAmount());
                    obj.put("amountdue", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
//                    obj.put("date", df.format(je.getEntryDate()));
                    obj.put("date", df.format(debitMemo.getCreationDate()));
                    obj.put("accountid", debitMemo.getAccount() == null ? "" : debitMemo.getAccount().getID());
                    obj.put("accountnames", debitMemo.getAccount() == null ? "" : debitMemo.getAccount().getName());
                    obj.put("linkingdate",df.format(new Date()));
                    /*
                     * Get global custom data for Receipt
                     */
                    accDebitNoteService.getDebitNoteCustomDataForPayment(requestParams, obj, debitMemo, je);
                    
                    if (requestParams.containsKey("isReceipt") && obj.optDouble("amountdue", 0.0) != 0) {
                        JArr.put(obj);
                    } else if (!requestParams.containsKey("isReceipt")) {
                        JArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptControllerCMN.getOpeningDebitNotesJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }

    public JSONArray getOpeningDebitNotesJson(HashMap<String, Object> requestParams, List<DebitNote> list, JSONArray JArr) throws ServiceException {
        try {

            DateFormat df = (DateFormat) requestParams.get("df");
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get("companyid");
            boolean isNoteForPayment = false;
            if (requestParams.containsKey("isNoteForPayment")) {
                isNoteForPayment = (Boolean) requestParams.get("isNoteForPayment");
            }
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            if (list != null && !list.isEmpty()) {
                for (DebitNote dn : list) {
//                    if (isNoteForPayment && dn.getOpeningBalanceAmountDue() <= 0) {
//                        continue;
//                    }
                    JSONObject obj = new JSONObject();
                    Date creditNoteDate = null;
                    double externalCurrencyRate = 0d;
                    creditNoteDate = dn.getCreationDate();
                    externalCurrencyRate = dn.getExchangeRateForOpeningTransaction();
                    String transactionCurrencyId = (dn.getCurrency() == null ? currency.getCurrencyID() : dn.getCurrency().getCurrencyID());
                    obj.put("currencyidtransaction", transactionCurrencyId);
                    obj.put("currencysymboltransaction", (dn.getCurrency() == null ? currency.getSymbol() : dn.getCurrency().getSymbol()));

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
                        obj.put("currencysymbol", (dn.getCurrency() == null ? currency.getSymbol() : dn.getCurrency().getSymbol()));
                        obj.put("currencyid", (dn.getCurrency() == null ? currency.getCurrencyID() : dn.getCurrency().getCurrencyID()));

                    }
                    double amountdue = dn.isOtherwise() ? dn.getDnamountdue() : 0;
                    double amountDueOriginal = dn.isOtherwise() ? dn.getDnamountdue() : 0;
                    if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !currencyFilterForTrans.equals(transactionCurrencyId)) {
                        KwlReturnObject bAmtCurrencyFilter = null;
                        if (dn.isIsOpeningBalenceDN() && dn.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            requestParams.put("isRevalue", true);
                            bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
                            amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                        } else {
                            bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
                            amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                        }
                    }
                    obj.put("documentid", dn.getID());
                    obj.put("documentno", dn.getDebitNoteNumber());
                    obj.put("documentType", 3);// 3 - Debit Note
                    obj.put("type", "Debit Note");
                    obj.put("amount", dn.getDnamount());
                    obj.put("amountdue", dn.getOpeningBalanceAmountDue());
                    obj.put("date", df.format(dn.getCreationDate()));
                    obj.put("amountdue", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("linkingdate",df.format(new Date()));
                    JArr.put(obj);
                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptControllerCMN.getOpeningDebitNotesJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }

    //to get refund/advance from vendor
    public JSONArray getAdvanceVendorPaymentForRefunds(HttpServletRequest request, JSONArray jArr) throws ServiceException {
        List<AdvanceDetail> advanceDetailList = new ArrayList<AdvanceDetail>();
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            HashMap<String, Object> requestParams = accVendorPaymentControllerCMN.getPaymentMap(request);
            String accid = request.getParameter("accid") != null ? request.getParameter("accid") : "";
            requestParams.put("accid", accid);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyFilterForTrans = "";
            KWLCurrency currencyFilter = null;
            currencyFilterForTrans = (request.getParameter("currencyfilterfortrans") == null) ? "" : request.getParameter("currencyfilterfortrans");
            if (!StringUtil.isNullOrEmpty(currencyFilterForTrans)) {
                KwlReturnObject currencyFilterResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                currencyFilter = (KWLCurrency) currencyFilterResult.getEntityList().get(0);
            }
            KwlReturnObject result = accVendorPaymentDAO.getPaymentAdvanceAmountDueDetails(requestParams);
            advanceDetailList = result.getEntityList();
            for (AdvanceDetail advanceDetail : advanceDetailList) {
                JSONObject obj = new JSONObject();
                Payment pm = advanceDetail.getPayment();
                String currencyid = pm.getCurrency().getCurrencyID();
                Vendor vendor = pm.getVendor();
                if (vendor != null) {
                    obj.put("accountid", vendor.getID());
                    obj.put("accountnames", vendor.getName());
                }
                obj.put("documentType", 1);
                obj.put("documentid", advanceDetail.getId());
//                obj.put("date", df.format(pm.getJournalEntry().getEntryDate()));
                obj.put("date", df.format(pm.getCreationDate()));
                obj.put("documentno", pm.getPaymentNumber());

                double amountdue = advanceDetail.getAmountDue();
                double amountDueOriginal = advanceDetail.getAmountDue();
                double externalCurrencyRate = pm.getExternalCurrencyRate();
                obj.put("amountDueOriginal", advanceDetail.getAmountDue());
                obj.put("amountDueOriginalSaved", advanceDetail.getAmountDue());
                obj.put("amount", advanceDetail.getAmount());
                obj.put("currencyid", currencyid);
                obj.put("currencysymbol", pm.getCurrency().getSymbol());
                obj.put("currencyidtransaction", currencyid);
                obj.put("currencysymboltransaction", pm.getCurrency().getSymbol());
                if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
//                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyFilterForTrans, pm.getJournalEntry().getEntryDate(), externalCurrencyRate);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyFilterForTrans, pm.getCreationDate(), externalCurrencyRate);
                    amountdue = (Double) bAmt.getEntityList().get(0);
                }
                obj.put("amountdue", authHandler.round(amountdue, companyid));
                obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                
                //Get Custom Field Data for Refund/Deposit
                accVendorPaymentServiceDAOobj.getAdvancePaymentCustomData(requestParams, advanceDetail, obj);
                jArr.put(obj);
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    public ModelAndView getDocumentsForLinkingWithAdvanceReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getDocumentsForLinkingWithAdvanceReceipt(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accReceiptControllerNew.getDocumentsForLinkingWithAdvanceReceipt:" + ex.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getDocumentsForLinkingWithAdvanceReceipt(HttpServletRequest request) {
        JSONObject returnObject = new JSONObject();
        try {
            JSONArray DataArray = new JSONArray();
            DataArray = getAllInvoicesAgainstCustomerForPayment(request, DataArray);
            DataArray = getDebitNoteMergedForPayment(request, DataArray);
            returnObject.put("data", DataArray);
            returnObject.put("count", DataArray.length());
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
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("dateformat", authHandler.getDateOnlyFormat(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("bills"))) {
                requestParams.put("bills", request.getParameterValues("bills"));
            }

            jArr = getLinkedDocumentsAgainstAdvanceJSON(requestParams);
            isSuccess = true;
            jObj.put("data", jArr);

        } catch (SessionExpiredException e) {
            msg = e.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception e) {
            msg = e.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                jObj.put("success", isSuccess);
                jObj.put("msg", msg);
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
            jArray = getLinkedDebitNotesAgainstAdvance(requestParams, jArray);
        } catch (Exception e) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return jArray;
    }

    public JSONArray getLinkedDebitNotesAgainstAdvance(HashMap<String, Object> requestParams, JSONArray jArray) throws ServiceException {
        String currencyId = (String) requestParams.get("gcurrencyid");
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyId);
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        String companyid = (String) requestParams.get("companyid");
        String[] receiptid = null;
        DateFormat df = (DateFormat) requestParams.get("dateformat");
        try {
            if (requestParams.containsKey("bills")) {
                receiptid = (String[]) requestParams.get("bills");
            }
            for (int index = 0; index < receiptid.length; index++) {
                KwlReturnObject receiptResult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid[index]);
                Receipt receipt = (Receipt) receiptResult.getEntityList().get(0);
                Set<Invoice> DnSet = new HashSet<Invoice>();
                Set<LinkDetailReceiptToDebitNote> linkDetailReceipt = receipt.getLinkDetailReceiptsToDebitNote();
                for (LinkDetailReceiptToDebitNote LDR : linkDetailReceipt) {
                    if (!DnSet.contains(LDR.getDebitnote())) {
                        DebitNote Dn = LDR.getDebitnote();
                        JSONObject jObj = new JSONObject();
                        Date dnCreationDate = null;
                        dnCreationDate = Dn.getCreationDate();
//                        if (Dn.isNormalDN()) {
//                            dnCreationDate = Dn.getJournalEntry().getEntryDate();
//                        } else {
//                            dnCreationDate = Dn.getCreationDate();
//                        }
                        List ll;
                        double amountdueInDnCurrency = 0.0;
                        if (Dn.isNormalDN()) {
                            amountdueInDnCurrency = Dn.getDnamountdue();
                        } else {
                            amountdueInDnCurrency = Dn.getOpeningBalanceAmountDue() * (LDR.getExchangeRateForTransaction() == 0 ? 1 : LDR.getExchangeRateForTransaction());
                        }
                        amountdueInDnCurrency += LDR.getAmountInDNCurrency();
                        amountdueInDnCurrency = authHandler.round(amountdueInDnCurrency, companyid);
                        jObj.put("billid", Dn.getID());
                        jObj.put("type", "Debit Note");
                        jObj.put("linkdetailid", LDR.getID());
                        jObj.put("documentno", LDR.getDebitnote().getDebitNoteNumber());
                        jObj.put("date", df.format(dnCreationDate));
                        jObj.put("amountDueOriginal", amountdueInDnCurrency);
                        jObj.put("exchangeratefortransaction", LDR.getExchangeRateForTransaction());
                        double amountdueInPaymentCurrency = authHandler.round(amountdueInDnCurrency * LDR.getExchangeRateForTransaction(), companyid);
                        jObj.put("amountdue", amountdueInPaymentCurrency);
                        jObj.put("linkamount", LDR.getAmount());
                        jObj.put("currencysymbol", Dn.getCurrency() == null ? (LDR.getReceipt().getCurrency() == null ? currency.getSymbol() : LDR.getReceipt().getCurrency().getSymbol()) : Dn.getCurrency().getSymbol());
                        jObj.put("currencysymboltransaction", Dn.getCurrency() == null ? (LDR.getReceipt().getCurrency() == null ? currency.getSymbol() : LDR.getReceipt().getCurrency().getSymbol()) : Dn.getCurrency().getSymbol());
                        jObj.put("currencysymbolpayment", LDR.getReceipt().getCurrency().getSymbol());
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
        String currencyId = (String) requestParams.get("gcurrencyid");
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyId);
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        String companyid = (String) requestParams.get("companyid");
        String[] receiptid = null;
        DateFormat df = (DateFormat) requestParams.get("dateformat");
        try {
            if (requestParams.containsKey("bills")) {
                receiptid = (String[]) requestParams.get("bills");
            }
            for (int index = 0; index < receiptid.length; index++) {
                KwlReturnObject receiptResult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid[index]);
                Receipt receipt = (Receipt) receiptResult.getEntityList().get(0);
                Set<Invoice> invoiceSet = new HashSet<Invoice>();
                Set<LinkDetailReceipt> linkDetailReceipt = receipt.getLinkDetailReceipts();
                for (LinkDetailReceipt LDR : linkDetailReceipt) {
                    if (!invoiceSet.contains(LDR.getInvoice())) {
                        JSONObject jObj = new JSONObject();
                        Date invoiceCreationDate = null;
                        invoiceCreationDate = LDR.getInvoice().getCreationDate();
                        Date invoicelinkingDate = LDR.getReceiptLinkDate();
//                        if (LDR.getInvoice().isNormalInvoice()) {
//                            invoiceCreationDate = LDR.getInvoice().getJournalEntry().getEntryDate();
//                        } else {
//                            invoiceCreationDate = LDR.getInvoice().getCreationDate();
//                        }
                        List ll;
                        double amountdueInInvoiceCurrency = 0.0;
                        if (LDR.getInvoice().isNormalInvoice()) {
                            if (Constants.InvoiceAmountDueFlag) {
                                ll = accInvoiceCommon.getInvoiceDiscountAmountInfo(requestParams, LDR.getInvoice());
                            } else {
                                ll = accInvoiceCommon.getAmountDue_Discount(requestParams, LDR.getInvoice());
                            }
                            amountdueInInvoiceCurrency = (Double) ll.get(3);
                        } else {
                            amountdueInInvoiceCurrency = LDR.getInvoice().getOpeningBalanceAmountDue() * (LDR.getExchangeRateForTransaction() == 0 ? 1 : LDR.getExchangeRateForTransaction());
                        }
                        amountdueInInvoiceCurrency += LDR.getAmountInInvoiceCurrency();
                        amountdueInInvoiceCurrency = authHandler.round(amountdueInInvoiceCurrency, companyid);
                        jObj.put("billid", LDR.getInvoice().getID());
                        jObj.put("type", "Invoice");
                        jObj.put("linkdetailid", LDR.getID());
                        jObj.put("documentno", LDR.getInvoice().getInvoiceNumber());
                        jObj.put("transectionno", LDR.getInvoice().getInvoiceNumber());
                        jObj.put("amountDueOriginal", amountdueInInvoiceCurrency);
                        jObj.put("date", df.format(invoiceCreationDate));
                        jObj.put("invoicelinkingdate", df.format(invoicelinkingDate));
                        jObj.put("exchangeratefortransaction", LDR.getExchangeRateForTransaction());
                        double amountdueInPaymentCurrency = authHandler.round(amountdueInInvoiceCurrency * LDR.getExchangeRateForTransaction(), companyid);
                        jObj.put("amountdue", amountdueInPaymentCurrency);
                        jObj.put("linkamount", LDR.getAmount());
                        jObj.put("currencysymbol", LDR.getInvoice().getCurrency() == null ? (LDR.getReceipt().getCurrency() == null ? currency.getSymbol() : LDR.getReceipt().getCurrency().getSymbol()) : LDR.getInvoice().getCurrency().getSymbol());
                        jObj.put("currencysymboltransaction", LDR.getInvoice().getCurrency() == null ? (LDR.getReceipt().getCurrency() == null ? currency.getSymbol() : LDR.getReceipt().getCurrency().getSymbol()) : LDR.getInvoice().getCurrency().getSymbol());
                        jObj.put("currencysymbolpayment", LDR.getReceipt().getCurrency().getSymbol());
                        jArray.put(jObj);

                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return jArray;
    }
    
    public ModelAndView getReceiptsForWriteOff(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            HashMap<String, Object> requestParams = getReceiptRequestMapJSON(paramJobj);
            requestParams.put("start", "");             // Start and limit parameters are set blank as we need to fetch all records initially including opening and normal receipts.
            requestParams.put("limit", "");
            DataJArr = getReceiptsForWriteOffJsonArray(request,requestParams);
            int count = DataJArr.length();
            JSONArray pagedJson = DataJArr;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("count", count);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getReceiptsForWriteOffJsonArray(HttpServletRequest request,HashMap<String,Object> requestMap){
        JSONArray jArr = new JSONArray();
        try{
            DateFormat df = authHandler.getDateOnlyFormat(request);
            List<Receipt> openingList = new ArrayList<Receipt>();
            List<Receipt> normalList  =new ArrayList<Receipt>();
            
            KwlReturnObject openingBalanceReceiptsResult = accReceiptDAOobj.getAllOpeningBalanceReceipts(requestMap);
            openingList = openingBalanceReceiptsResult.getEntityList();
            KwlReturnObject normalReceipt = accReceiptDAOobj.getReceipts(requestMap);
            normalList = normalReceipt.getEntityList();
            
            Iterator itrNormal = normalList.iterator();
            boolean isOpening=false;
            Date creationDate = null;
            Double amountDue=0.0;
            for(Receipt receipt :openingList){
                JSONObject obj = new JSONObject();
                amountDue = accReceiptServiceDAOobj.getReceiptAmountDue(receipt);
                if(amountDue<=0){
                    continue;
                }
                obj.put("billno", receipt.getReceiptNumber());
                obj.put("billid", receipt.getID());
                obj.put("amount", receipt.getDepositAmount());
                obj.put("amountdue", amountDue);
                obj.put("personname", receipt.getCustomer().getName());
                obj.put("currencysymboltransaction", receipt.getCurrency().getSymbol());
                obj.put("date", df.format(receipt.getCreationDate()));
                obj.put("allowwriteoff", true);
                obj.put("isWrittenOff", receipt.isIsWrittenOff());
                jArr.put(obj);
            }
            amountDue=0.0;
            while(itrNormal.hasNext()){
                Object[] row = (Object[]) itrNormal.next();
                Receipt receipt = (Receipt) row[0];
                JSONObject obj = new JSONObject();
                if((receipt.getReceiptAdvanceDetails() == null) || (receipt.getReceiptAdvanceDetails().isEmpty()) || (receipt.getPaymentWindowType()!=1)){
                    continue;
                } else {
                    boolean allowwriteoff=false;
                    for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                            amountDue = advanceDetail.getAmountDue();
                            JournalEntryDetail totalJedId  = advanceDetail.getTotalJED();
                            if(totalJedId != null){
                                allowwriteoff = true;
                            }
                        }
                    obj.put("allowwriteoff", allowwriteoff);
                }
                if(amountDue<=0){
                    continue;
                }
                obj.put("billno", receipt.getReceiptNumber());
                obj.put("billid", receipt.getID());
                obj.put("amount", receipt.getDepositAmount());
                obj.put("amountdue", amountDue);
                obj.put("personname", receipt.getCustomer().getName());
                obj.put("currencysymboltransaction", receipt.getCurrency().getSymbol());
//                obj.put("date", df.format(receipt.getJournalEntry().getEntryDate()));
                obj.put("date", df.format(receipt.getCreationDate()));
                obj.put("isWrittenOff", receipt.isIsWrittenOff());
                jArr.put(obj);
            }
        } catch(Exception e){
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return jArr;
    }
    /**
     * Controller for getting linkedIn transactions for Payment Receipt
     * @param request
     * @param response
     * @return ModelAndView
     */
    public ModelAndView getReceiptLinkedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getReceiptLinkedInTransaction(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accvendorPaymentControllerNew.getPaymentLinkedInTransaction:" + ex.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * Method for getting linkedIn transactions JSON for Payment Receipt
     * @param request
     * @return JSONObject
     */
    public JSONObject getReceiptLinkedInTransaction(HttpServletRequest request) throws ServiceException{
        JSONObject obj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try{
            String billid = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            KwlReturnObject receiptResult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), billid);
            Receipt receipt = (Receipt) receiptResult.getEntityList().get(0);
            Map invoiceMap = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            String gcurrencyid = sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), gcurrencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.df, df);
            requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
            requestParams.put(Constants.companyKey, companyid);
            if (receipt.getLinkDetailReceipts() != null && !receipt.getLinkDetailReceipts().isEmpty()) {
                for (LinkDetailReceipt adv : receipt.getLinkDetailReceipts()) {
                    JSONObject invobj = new JSONObject();
                    Invoice invoice = adv.getInvoice();
                    requestParams.put("linkingdate", adv.getReceiptLinkDate()!=null ? adv.getReceiptLinkDate() : "");
                    invobj = accReceiptServiceDAOobj.getInvoiceJSON(invoice, invoiceMap, requestParams);
                    jArr.put(invobj);
                }
            }
            if (receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty()) {
                /**
                 * isAdvPayment flag is use to determine whether the Refund
                 * Receipt is linked with Advance Payment or Advance receipt is
                 * linked with refund Payment.Because If refund receipt is linked with
                 * advance payment we display advance payments in linking
                 * information so there is no need to check whether advance
                 * receipt is linked to refund payment and vice versa. ERP-39559
                 */
                boolean isAdvPayment =false;
                for (ReceiptAdvanceDetail adv : receipt.getReceiptAdvanceDetails()) {
                    String advanceId = adv.getId();
                    List<Object[]> resultList1 = accReceiptDAOobj.getAdvanceReceiptUsedInRefundPayment(advanceId);
                    List<Object[]> resultList = new ArrayList<>();
                    if (resultList1 != null && !resultList1.isEmpty()) {
                        resultList.addAll(resultList1);
                    }
                    //advance payment used in refund receipt
                    if (!StringUtil.isNullOrEmpty(adv.getAdvancedetailid())) { // advancedetailid is not null
                        String paymentadvancedetailid = adv.getAdvancedetailid();
                        List<Object[]> resultList2 = accReceiptDAOobj.getAdvancePaymentUsedInRefundReceipt(paymentadvancedetailid);
                        if (resultList2 != null && !resultList2.isEmpty()) {
                            resultList.addAll(resultList2);
                        }
                    }
                    for (int i = 0; i < resultList.size(); i++) {
                        Object[] objArray = (Object[]) resultList.get(i);
                        String paymentId = objArray[3].toString();
                        KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentId);
                        Payment payment = (Payment) paymentResult.getEntityList().get(0);
                        if (payment != null) {
                            String JeNumber = "";
                            JournalEntry je = payment.getJournalEntry();
                            if(!payment.isIsOpeningBalencePayment() && je!=null){
                                JeNumber = je.getEntryNumber();
                            }
                            JSONObject paymentObj = new JSONObject();
                            requestParams.put("df", df);
                            requestParams.put("JeNumber", JeNumber);
                            requestParams.put("receipt", receipt);
                            paymentObj = accReceiptServiceDAOobj.getPaymentJSON(payment, requestParams);
                            jArr.put(paymentObj);
                            isAdvPayment = true;
                        }
                    }
                }
                Set<LinkDetailReceiptToAdvancePayment> linkDetailReceiptToAdvancePaymentSet = receipt.getLinkDetailReceiptsToAdvancePayment();
                if (linkDetailReceiptToAdvancePaymentSet != null && !linkDetailReceiptToAdvancePaymentSet.isEmpty()) {
                    for (LinkDetailReceiptToAdvancePayment linkDetailReceiptToAdvancePayment : linkDetailReceiptToAdvancePaymentSet) {
                        String paymentId = linkDetailReceiptToAdvancePayment.getPaymentId();
                        KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentId);
                        Payment payment = (Payment) paymentResult.getEntityList().get(0);
                        if (payment != null) {
                            String JeNumber = "";
                            JournalEntry je = payment.getJournalEntry();
                            if (!payment.isIsOpeningBalencePayment() && je != null) {
                                JeNumber = je.getEntryNumber();
                            }
                            JSONObject paymentObj = new JSONObject();
                            requestParams.put("df", df);
                            requestParams.put("JeNumber", JeNumber);
                            requestParams.put("receipt", receipt);
                            paymentObj = accReceiptServiceDAOobj.getPaymentJSON(payment, requestParams);
                            jArr.put(paymentObj);
                            isAdvPayment = true;
                        }
                    }
                }
                if (!isAdvPayment) {
                    List resultList = accReceiptDAOobj.getAdvanceReceiptLinkedWithRefundPayment(receipt.getID(), companyid);
                    for (int i = 0; i < resultList.size(); i++) {
                        Object[] objArray = (Object[]) resultList.get(i);
                        String paymentId = objArray[0].toString();
                        String paymentNumber = objArray[1].toString();
                        KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentId);
                        Payment payment = (Payment) paymentResult.getEntityList().get(0);
                        if (payment != null) {
                            String JeNumber = "";
                            JournalEntry je = payment.getJournalEntry();
                            if (!payment.isIsOpeningBalencePayment() && je != null) {
                                JeNumber = je.getEntryNumber();
                            }
                            JSONObject paymentObj = new JSONObject();
                            requestParams.put("df", df);
                            requestParams.put("JeNumber", JeNumber);
                            requestParams.put("receipt", receipt);
                            paymentObj = accReceiptServiceDAOobj.getPaymentJSON(payment, requestParams);
                            jArr.put(paymentObj);
                        }
                    }
                }
            }
            /* Sales Invoice Linked in Receipt */
            if (receipt != null && receipt.getRows() != null && !receipt.getRows().isEmpty()) {
                Set<ReceiptDetail> details = receipt.getRows();
                for (ReceiptDetail receiptDetail : details) {
                    Invoice invoice = receiptDetail.getInvoice();
                    if (invoice != null) {
                        JSONObject invobj = new JSONObject();
//                        requestParams.put("linkingdate", receipt.isIsOpeningBalenceReceipt()?receipt.getCreationDate():receipt.getJournalEntry().getEntryDate());
                        requestParams.put("linkingdate", receipt.getCreationDate());
                        invobj = accReceiptServiceDAOobj.getInvoiceJSON(invoice, invoiceMap, requestParams);
                        jArr.put(invobj);
                    }
                }
            }

            /* Debit Note Linked in Receipt */
            if (receipt != null && receipt.getDebitNotePaymentDetails() != null && !receipt.getDebitNotePaymentDetails().isEmpty()) {
                Set<DebitNotePaymentDetails> details = receipt.getDebitNotePaymentDetails();
                for (DebitNotePaymentDetails debitNotePaymentDetails : details) {
                    DebitNote debitNote=debitNotePaymentDetails.getDebitnote();
//                    requestParams.put("linkingdate", receipt.isIsOpeningBalenceReceipt()?receipt.getCreationDate():receipt.getJournalEntry().getEntryDate());
                    requestParams.put("linkingdate", receipt.getCreationDate());
                    JSONObject dnJSON=accReceiptServiceDAOobj.getDebitNoteJSON(debitNote, requestParams);
                    jArr.put(dnJSON);
                }
            }
             /* Debit Note Linked to advance Receipt */
            Set<LinkDetailReceiptToDebitNote> linkDetailReceipt = receipt.getLinkDetailReceiptsToDebitNote();
            if (linkDetailReceipt != null && !linkDetailReceipt.isEmpty()) {
                for (LinkDetailReceiptToDebitNote linkDetailReceiptToDebitNote : linkDetailReceipt) {
                    DebitNote debitNote = linkDetailReceiptToDebitNote.getDebitnote();
                    if (debitNote != null) {
                        requestParams.put("linkingdate", linkDetailReceiptToDebitNote.getReceiptLinkDate()!=null ? linkDetailReceiptToDebitNote.getReceiptLinkDate() : "");
                        JSONObject dnJSON = accReceiptServiceDAOobj.getDebitNoteJSON(debitNote, requestParams);
                        jArr.put(dnJSON);       
                    }
                }
            }
                       
            /**
             * Advance Receipt used in sales order
             */
            JSONObject params = new JSONObject();
            params.put("receiptid", receipt.getID());
            List<Object[]> soResultList = new ArrayList<>();
            soResultList = accReceiptDAOobj.getAdvanceReceiptUsedSalesOrder(params);
            if (soResultList != null && !soResultList.isEmpty()) {
                for (int i = 0; i < soResultList.size(); i++) {
                    Object[] objArray = (Object[]) soResultList.get(i);
                    String soId = objArray[2].toString();
                    KwlReturnObject soResult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soId);
                    SalesOrder salesOrder = (SalesOrder) soResult.getEntityList().get(0);
                    requestParams.put("linkingdate", salesOrder.getOrderDate() != null ? salesOrder.getOrderDate() : "");
                    List<Object[]> salesorders = new ArrayList<>();
                    salesorders.add(new Object[]{salesOrder, 6});
                    jArr = accSalesOrderServiceDAOobj.getSalesOrderJsonForLinking(jArr, salesorders, currency, df);
                }
            }
            obj.put("data", jArr);
        } catch(Exception e){
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return obj;
    }
    
     public ModelAndView getDocumentsForLinkingWithCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getDocumentsForLinkingWithCreditNote(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accReceiptControllerNew.getDocumentsForLinkingWithAdvanceReceipt:" + ex.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getDocumentsForLinkingWithCreditNote(HttpServletRequest request) {
        JSONObject returnObject = new JSONObject();
        try {
            JSONArray DataArray = new JSONArray();
            DataArray = getAllInvoicesAgainstCustomerForPayment(request, DataArray);
            DataArray = getDebitNoteMergedForPayment(request, DataArray);
            returnObject.put("data", DataArray);
            returnObject.put("count", DataArray.length());
        } catch (Exception ex) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }

        return returnObject;
    }
    
    /**
     * Controller for getting linkedIn transactions for Refund Receipt
     *
     * @param request
     * @param response
     * @return ModelAndView
     */
    public ModelAndView getLinkedDocumentsAgainstRefundReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean isSuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("dateformat", authHandler.getDateOnlyFormat(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("bills"))) {
                requestParams.put("bills", request.getParameterValues("bills"));
            }

            jArr = getLinkedDocumentsAgainstRefundReceiptJSON(requestParams);
            isSuccess = true;
            jObj.put("data", jArr);

        } catch (SessionExpiredException e) {
            msg = e.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception e) {
            msg = e.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                jObj.put("success", isSuccess);
                jObj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }
    
    /**
     * Description : Below Method is used to get Advance Payment linked to Refund Receipt
     *
     * @param <requestParams> This Map is used to get gcurrencyid, dateformat, bills
     * @return JSONArray
     */
    public JSONArray getLinkedDocumentsAgainstRefundReceiptJSON(Map<String, Object> requestParams) throws ServiceException {
        JSONArray jArray = new JSONArray();
        try {
            String currencyId = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyId);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String companyid = (String) requestParams.get("companyid");
            DateFormat df = (DateFormat) requestParams.get("dateformat");
            String[] receiptid = null;
            if (requestParams.containsKey("bills")) {
                receiptid = (String[]) requestParams.get("bills");
            }

            for (int index = 0; index < receiptid.length; index++) {
                KwlReturnObject receiptResult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid[index]);
                Receipt receipt = (Receipt) receiptResult.getEntityList().get(0);
                
                Set<LinkDetailReceiptToAdvancePayment> linkDetailReceipt = receipt.getLinkDetailReceiptsToAdvancePayment();
                for (LinkDetailReceiptToAdvancePayment LDR : linkDetailReceipt) {
                    KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Payment.class.getName(), LDR.getPaymentId());
                    Payment payment = (Payment) paymentResult.getEntityList().get(0);
                    
                    JSONObject jObj = new JSONObject();
                    
                    double amountdueInAdvPaymentCurrency = 0.0;
//                    Date dnCreationDate = payment.getJournalEntry().getEntryDate();
                    Date dnCreationDate = payment.getCreationDate();
                    
                    Set<AdvanceDetail> advDetails = payment.getAdvanceDetails();
                    for (AdvanceDetail advDetail : advDetails) {
                        amountdueInAdvPaymentCurrency += advDetail.getAmountDue();
                    }
                    
                    amountdueInAdvPaymentCurrency += LDR.getAmountInPaymentCurrency();
                    amountdueInAdvPaymentCurrency = authHandler.round(amountdueInAdvPaymentCurrency, companyid);
                    jObj.put("billid", payment.getID());
                    jObj.put("type", "Advance Payment");
                    jObj.put("linkdetailid", LDR.getID());
                    jObj.put("documentno", payment.getPaymentNumber());
                    jObj.put("date", df.format(dnCreationDate));
                    jObj.put("amountDueOriginal", amountdueInAdvPaymentCurrency);
                    jObj.put("exchangeratefortransaction", LDR.getExchangeRateForTransaction());
                    double amountdueInPaymentCurrency = authHandler.round(amountdueInAdvPaymentCurrency * LDR.getExchangeRateForTransaction(), companyid);
                    jObj.put("amountdue", amountdueInPaymentCurrency);
                    jObj.put("linkamount", LDR.getAmount());
                    jObj.put("currencysymbol", payment.getCurrency() == null ? (LDR.getReceipt().getCurrency() == null ? currency.getSymbol() : LDR.getReceipt().getCurrency().getSymbol()) : payment.getCurrency().getSymbol());
                    jObj.put("currencysymbolpayment", LDR.getReceipt().getCurrency().getSymbol());
                    jArray.put(jObj);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return jArray;
    }
}
