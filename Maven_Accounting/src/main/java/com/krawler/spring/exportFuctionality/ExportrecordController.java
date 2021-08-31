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
package com.krawler.spring.exportFuctionality;

import com.krawler.common.admin.AccCustomData;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import java.util.Map;
import java.util.Locale;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.servlets.ProfileImageServlet;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.HashMap;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.goodsreceipt.AccGoodsReceiptServiceDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.invoice.accInvoiceController;
import com.krawler.spring.accounting.reports.accReportsController;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

public class ExportrecordController extends MultiActionController {

    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private ExportRecord ExportrecordObj;
    private ExportRecord_VRNet ExportrecordVRnetObj;
    private ExportRecord_LSH ExportrecordLSHObj;
    private ExportRecord_VHQ ExportrecordVHQObj;
    private CreatePDF CreatePDFObj;
    private ExportRecord_SATS exportRecord_SATSObj;
    private CreatePrintWindow createPrintWindowObj;
    private AccExportReportsServiceDAO accExportReportsServiceDAOobj;
    private AccExportReportsServiceDAO accExportOtherReportsServiceDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accReportsController accReportsController;
    private CustomDesignDAO customDesignDAObj;
    private VelocityEngine velocityEngine;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accAccountDAO accAccountDAOobj;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;
    private AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj;

    public void setAccAccountDAOobj(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setAccGoodsReceiptServiceDAOObj(AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj) {
        this.accGoodsReceiptServiceDAOObj = accGoodsReceiptServiceDAOObj;
    }

    public void setAccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }

    public void setCustomDesignDAObj(CustomDesignDAO customDesignDAObj) {
        this.customDesignDAObj = customDesignDAObj;
    }

    public void setKwlCommonTablesDAOObj(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public void setAccReportsController(accReportsController accReportsController) {
        this.accReportsController = accReportsController;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setExportRecord(ExportRecord ExportrecordObj) {
        this.ExportrecordObj = ExportrecordObj;
    }

    public void setExportRecordVRnet(ExportRecord_VRNet ExportrecordVRnetObj) {
        this.ExportrecordVRnetObj = ExportrecordVRnetObj;
    }

    public void setExportRecordLSH(ExportRecord_LSH ExportrecordLSHObj) {
        this.ExportrecordLSHObj = ExportrecordLSHObj;
    }

    public void setExportRecordSATS(ExportRecord_SATS exportRecord_SATSObj) {
        this.exportRecord_SATSObj = exportRecord_SATSObj;
    }

    public void setExportRecordVHQ(ExportRecord_VHQ ExportrecordVHQObj) {
        this.ExportrecordVHQObj = ExportrecordVHQObj;
    }

    public void setCreatePDF(CreatePDF CreatePDFObj) {
        this.CreatePDFObj = CreatePDFObj;
    }

    public void setCreatePrintWindow(CreatePrintWindow createPrintWindowObj) {
        this.createPrintWindowObj = createPrintWindowObj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }

    public void setaccExportReportsServiceDAO(AccExportReportsServiceDAO accExportReportsServiceDAOobj) {
        this.accExportReportsServiceDAOobj = accExportReportsServiceDAOobj;
    }
 
    public void setaccExportOtherReportsServiceDAO(AccExportReportsServiceDAO accExportReportsServiceDAOobj) {
        this.accExportOtherReportsServiceDAOobj = accExportReportsServiceDAOobj;
    }
    
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj){
    	this.exportDaoObj = exportDaoObj;
    }

    public ModelAndView exportRecords(HttpServletRequest request,
            HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");
//        Session session = HibernateUtil.getCurrentSession();
        boolean otherwiseFlag = false;
        double advanceAmount = 0;
        HashMap<String, Object> requestmap = null;
        String[] billsArray = {};
        List<ByteArrayOutputStream> baosList = null;

        if (!StringUtil.isNullOrEmpty(request.getParameter("advanceAmount"))) {
            advanceAmount = Double.parseDouble(request.getParameter("advanceAmount"));
        }
        boolean advanceFlag = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("advanceFlag"))) {
            advanceFlag = Boolean.parseBoolean(request.getParameter("advanceFlag"));
        }

        if (!StringUtil.isNullOrEmpty(request.getParameter("otherwise"))) {
            otherwiseFlag = Boolean.parseBoolean(request.getParameter("otherwise"));
        }
        boolean iscontraentryflag = false;
        boolean isOpeningBalanceTransaction = false;
        int templateflag=0;
        try {
//          billsArray = new ArrayList<String>();
            baosList = new ArrayList<ByteArrayOutputStream>();
            requestmap = new HashMap<String, Object>();
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            String username = sessionHandlerImpl.getUserName(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("templateflag"))) {
                templateflag = Integer.parseInt(request.getParameter("templateflag"));
            }
            Locale loc = RequestContextUtils.getLocale(request);
            iscontraentryflag = Boolean.parseBoolean(request.getParameter("contraentryflag"));

            if (StringUtil.getBoolean(request.getParameter("isOpeningBalanceTransaction"))) {
                isOpeningBalanceTransaction = Boolean.parseBoolean(request.getParameter("isOpeningBalanceTransaction"));
            }
            boolean isLetterHead = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isLetterHead"))) {
                isLetterHead = Boolean.parseBoolean((String) request.getParameter("isLetterHead"));
            }

            requestmap.put("locale", loc);
            requestmap.put("username", username);
            requestmap.put("advanceAmount", advanceAmount);
            requestmap.put("advanceFlag", advanceFlag);
            requestmap.put("otherwiseFlag", otherwiseFlag);
            requestmap.put("isLetterHead", isLetterHead);
            requestmap.put("baseUrl", baseUrl);
            requestmap.put("iscontraentryflag", iscontraentryflag);
            requestmap.put("isOpeningBalanceTransaction", isOpeningBalanceTransaction);
            requestmap.put(Constants.companyKey, AccountingManager.getCompanyidFromRequest(request));
            requestmap.put(Constants.globalCurrencyKey, AccountingManager.getCompanyidFromRequest(request));
//            requestmap.put(Constants.df, authHandler.getDateFormatter(request));
            requestmap.put(Constants.df, authHandler.getDateOnlyFormat(request));
            String singleFilename = request.getParameter("filename") + ".pdf";
            ByteArrayOutputStream baos = null;
            double amount=0.0d;
            if (!StringUtil.isNullOrEmpty(request.getParameter("amount"))) {
                amount = Double.parseDouble(request.getParameter("amount"));
            }

            String filename = null;
            int mode = Integer.parseInt(request.getParameter("mode"));
            switch (mode) {
                case 1:
                    filename = "SalesOrder.pdf";
                    break;
                case 2:
                    filename = "Invoice.pdf";
                    break;
                case 3:
                    filename = "CreditNote.pdf";
                    break;
                case 4:
                    filename = "PaymentReceived.pdf";
                    break;
                case 5:
                    filename = "PurchaseOrder.pdf";
                    break;
                case 6:
                    filename = "VendorInvoice.pdf";
                    break;
                case 7:
                    filename = "DebitNote.pdf";
                    break;
                case 8:
                    filename = "PaymentMade.pdf";
                    break;
                case 53:
                    filename = "DelievryOrder.pdf";
                    break;
                case 54:
                    filename = "GoodsReceipt.pdf";
                    break;
                case 61:
                    filename = "SalesReturn.pdf";
                    break;
                case 63:
                    filename = "PurchaseReturn.pdf";
                    break;
                case 213:
                    filename = "CustomerQuotationVersion.pdf";
                    break;
                case 252:
                    filename = "VendorQuotationVersion.pdf";
                    break;
                case 57:
                    filename="VendorQuotation.pdf";
                    break;
                case 50: 
                    filename="CustomerQuotation.pdf";
                    break;
            }
            String billid = request.getParameter("bills");
            String recordids = request.getParameter("recordids");
            if (recordids != null) {
                billsArray = recordids.split(",");
            }

            boolean isexpenseinv = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isexpenseinv"))) {
                isexpenseinv = Boolean.parseBoolean((String) request.getParameter("isexpenseinv"));
            }
            String cust = request.getParameter("customer");
            String accname = request.getParameter("accname");
            String address = request.getParameter("address");
            String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
//            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
            KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Account.class.getName(), request.getParameter("personid"));
            Account account = (Account) cap.getEntityList().get(0);
            //Account account=(Account)session.get(Account.class, request.getParameter("personid"));
            String currencyid = account == null ? sessionHandlerImpl.getCurrencyID(request) : account.getCurrency().getCurrencyID();
            DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject companyresult = kwlCommonTablesDAOObj.getObject(Company.class.getName(), companyId);
            Company company = (Company) companyresult.getEntityList().get(0);
            int countryid = company.getCountry() != null ? Integer.parseInt(company.getCountry().getID()) : 0;
            String userId = sessionHandlerImpl.getUserid(request);
            if (storageHandlerImpl.GetVRnetCompanyId().contains(companyId)
                    && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_QUOTATION
                    || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_DELIVERYORDER
                    || mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE || mode == StaticValues.AUTONUM_PURCHASEORDER)) {
                baos = ExportrecordVRnetObj.createVRNetPdf(request, currencyid, billid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv, companyId, userId, sessionHandlerImpl.getCurrencyID(request));
            } else if (storageHandlerImpl.GetLSHCompanyId().contains(companyId)
                     && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_QUOTATION ||mode == StaticValues.AUTONUM_CUSTOMERQUOTATIONVERSION
                    || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_DELIVERYORDER
                    || mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE || mode == StaticValues.AUTONUM_PURCHASEORDER
                    || mode == StaticValues.AUTONUM_PAYMENT || mode == StaticValues.AUTONUM_BILLINGPAYMENT || mode == StaticValues.AUTONUM_RECEIPT || mode == StaticValues.AUTONUM_BILLINGRECEIPT)) {
                baos = ExportrecordLSHObj.createLSHPdf(request, currencyid, billid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv, companyId, userId, sessionHandlerImpl.getCurrencyID(request));
            } else if (storageHandlerImpl.GetVHQCompanyId().contains(companyId)
                    && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_QUOTATION
                    || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_DELIVERYORDER || mode == StaticValues.AUTONUM_GOODSRECEIPTORDER
                    || mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE || mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_GOODSRECEIPT 
                    || mode == StaticValues.AUTONUM_PAYMENT || mode == StaticValues.AUTONUM_BILLINGPAYMENT || mode == StaticValues.AUTONUM_RECEIPT || mode == StaticValues.AUTONUM_BILLINGRECEIPT)) {
                baos = ExportrecordVHQObj.createVHQPdf(request, currencyid, billid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv, companyId, userId, sessionHandlerImpl.getCurrencyID(request));
            } else if (storageHandlerImpl.GetVHQCompanyId().contains(companyId)) {
                baos = exportRecord_SATSObj.createPdf(request, currencyid, billid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv, companyId, userId, sessionHandlerImpl.getCurrencyID(request));
            } else if (countryid == Constants.malaysian_country_id && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_GOODSRECEIPT || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_PURCHASEORDER) && !isexpenseinv) {
                try {
                    List jasperPrint = accExportReportsServiceDAOobj.exportMalaysianGstTaxInvoiceJasper(request, response);
                    JRPdfExporter exp = new JRPdfExporter();
                    exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                    exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
                    exp.exportReport();
                } catch (Exception e) {
                    Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, e);
                }
            } else if ((templateflag == Constants.Guan_Chong_templateflag || templateflag == Constants.Guan_ChongBF_templateflag) && (mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE)) {
                try {

                    if (billsArray.length > 1) {
                        List jasperPrint;
                        for (int i = 0; i < billsArray.length; i++) {
                            String recbillid = billsArray[i];
                            if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                                KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), recbillid);
                                DebitNote dn = (DebitNote) creditNoteResult.getEntityList().get(0);
                                if (dn.getPurchaseReturn() != null) {
                                    jasperPrint = accExportReportsServiceDAOobj.exportPurchaseReturn(request,response, dn.getPurchaseReturn().getID(), dn.getDebitNoteNumber());
                                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                    JRPdfExporter exporter = new JRPdfExporter();
                                    exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                    exporter.exportReport();
                                    baosList.add(baos1);
                                } else {
                                    baos = CreatePDFObj.createPdf(requestmap, currencyid, recbillid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv, companyId, userId, sessionHandlerImpl.getCurrencyID(request));
                                    baosList.add(baos);
                                }
                            } else if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                                KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), recbillid);
                                CreditNote cn = (CreditNote) creditNoteResult.getEntityList().get(0);
                                if (cn.getSalesReturn() != null) {
                                    jasperPrint = accExportReportsServiceDAOobj.exportSalesReturnJasper(request, response,cn.getSalesReturn().getID(),cn.getCreditNoteNumber());
                                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                    JRPdfExporter exporter = new JRPdfExporter();
                                    exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                    exporter.exportReport();
                                    baosList.add(baos1);
                                } else {
                                    baos = CreatePDFObj.createPdf(requestmap, currencyid, recbillid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv, companyId, userId, sessionHandlerImpl.getCurrencyID(request));
                                    baosList.add(baos);
                                }
                            }
                        }
                    } else {
                        if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                            KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), billid);
                            DebitNote dn = (DebitNote) creditNoteResult.getEntityList().get(0);
                            if (dn.getPurchaseReturn() != null) {
                                List jasperPrint = accExportReportsServiceDAOobj.exportPurchaseReturn(request,response, dn.getPurchaseReturn().getID(), dn.getDebitNoteNumber());
                                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                JRPdfExporter exporter = new JRPdfExporter();
                                exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                exporter.exportReport();
                                baos = baos1;
                            } else {
                                baos = CreatePDFObj.createPdf(requestmap, currencyid, billid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv, companyId, userId, sessionHandlerImpl.getCurrencyID(request));
                                baosList.add(baos);
                            }
                        } else if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                            KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), billid);
                            CreditNote cn = (CreditNote) creditNoteResult.getEntityList().get(0);
                            if (cn.getSalesReturn() != null) {
                                List jasperPrint = accExportReportsServiceDAOobj.exportSalesReturnJasper(request, response,cn.getSalesReturn().getID(),cn.getCreditNoteNumber());
                                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                JRPdfExporter exporter = new JRPdfExporter();
                                exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                exporter.exportReport();
                                baos = baos1;
                            } else {
                                baos = CreatePDFObj.createPdf(requestmap, currencyid, billid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv, companyId, userId, sessionHandlerImpl.getCurrencyID(request));
                                baosList.add(baos);
                            }
                        }
                    }
                } catch (Exception e) {
                    Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, e);
                }
            } else if (countryid == Constants.malaysian_country_id && (mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE)) {
                try {

                    if (billsArray.length > 1) {
                        List jasperPrint;
                        for (int i = 0; i < billsArray.length; i++) {
                            String recbillid = billsArray[i];
                            if(mode==StaticValues.AUTONUM_DEBITNOTE){
                                  KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), recbillid);
                                  DebitNote dn = (DebitNote) creditNoteResult.getEntityList().get(0);
                                  if(dn.getPurchaseReturn()!=null){
                                      jasperPrint = accExportReportsServiceDAOobj.exportMalaysianGstCreditDebitNote(request, recbillid, companyId, mode);
                                      ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                      JRPdfExporter exporter = new JRPdfExporter();
                                      exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                      exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                      exporter.exportReport();
                                      baosList.add(baos1);
                                  }else{
                                      jasperPrint = accExportReportsServiceDAOobj.exportMalaysianGstNormalCreditDebitNote(request, recbillid, companyId, mode);
                                      baos = new ByteArrayOutputStream();
                                      JRPdfExporter exporter = new JRPdfExporter();
                                      exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                      exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
                                      exporter.exportReport();
                                       baosList.add(baos);
                                  }
                            }else if(mode==StaticValues.AUTONUM_CREDITNOTE){
                                  KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), recbillid);
                                  CreditNote cn = (CreditNote) creditNoteResult.getEntityList().get(0);
                                  if(cn.getSalesReturn()!=null){
                                       jasperPrint = accExportReportsServiceDAOobj.exportMalaysianGstCreditDebitNote(request, recbillid, companyId, mode);
                                      ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                      JRPdfExporter exporter = new JRPdfExporter();
                                      exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                      exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                      exporter.exportReport();
                                      baosList.add(baos1);
                                  }else{
                                      jasperPrint = accExportReportsServiceDAOobj.exportMalaysianGstNormalCreditDebitNote(request, recbillid, companyId, mode);
                                      baos = new ByteArrayOutputStream();
                                      JRPdfExporter exporter = new JRPdfExporter();
                                      exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                      exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
                                      exporter.exportReport();
                                       baosList.add(baos);
                                  }
                            }
                        }
                    }else{
                            if(mode==StaticValues.AUTONUM_DEBITNOTE){
                                  KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), billid);
                                  DebitNote dn = (DebitNote) creditNoteResult.getEntityList().get(0);
                                  if(dn.getPurchaseReturn()!=null){
                                      List jasperPrint = accExportReportsServiceDAOobj.exportMalaysianGstCreditDebitNote(request, billid, companyId, mode);
                                      ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                      JRPdfExporter exporter = new JRPdfExporter();
                                      exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                      exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                      exporter.exportReport();
                                      baos=baos1;
                                  }else{
                                      List jasperPrint = accExportReportsServiceDAOobj.exportMalaysianGstNormalCreditDebitNote(request, billid, companyId, mode);
                                      ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                      JRPdfExporter exporter = new JRPdfExporter();
                                      exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                      exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                      exporter.exportReport();
                                      baos = baos1;
                                  }
                            }else if(mode==StaticValues.AUTONUM_CREDITNOTE){
                                  KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), billid);
                                  CreditNote cn = (CreditNote) creditNoteResult.getEntityList().get(0);
                                  if(cn.getSalesReturn()!=null){
                                      List jasperPrint = accExportReportsServiceDAOobj.exportMalaysianGstCreditDebitNote(request, billid, companyId, mode);
                                      ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                      JRPdfExporter exporter = new JRPdfExporter();
                                      exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                      exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                      exporter.exportReport();
                                      baos=baos1;
                                  }else{
                                      List jasperPrint = accExportReportsServiceDAOobj.exportMalaysianGstNormalCreditDebitNote(request, billid, companyId, mode);
                                      ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                      JRPdfExporter exporter = new JRPdfExporter();
                                      exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                      exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                      exporter.exportReport();
                                      baos = baos1;
                                  }
                            }
                    }
                    //                    List jasperPrint = accExportReportsServiceDAOobj.exportMalaysianGstTaxInvoiceJasper(request, response);
//                    JRPdfExporter exp = new JRPdfExporter();
//                    exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
//                    exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
//                    exp.exportReport();
                } catch (Exception e) {
                    Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, e);
                }
            } else if ((templateflag == Constants.F1RecreationLeasing_templateflag || templateflag == Constants.F1Recreation_templateflag) && (mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE)) {
                try {

                    if (billsArray.length > 1) {
                        List jasperPrint;
                        for (int i = 0; i < billsArray.length; i++) {
                            String recbillid = billsArray[i];
                            if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                                KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), recbillid);
                                DebitNote dn = (DebitNote) creditNoteResult.getEntityList().get(0);
                                if (dn.getPurchaseReturn() != null) {
                                    jasperPrint = accExportReportsServiceDAOobj.exportF1RecreationPurchaseReturn(request, response, dn.getPurchaseReturn().getID(), dn.getDebitNoteNumber());
                                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                    JRPdfExporter exporter = new JRPdfExporter();
                                    exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                    exporter.exportReport();
                                    baosList.add(baos1);
                                } else if (dn.isOtherwise()) {
                                    jasperPrint = accExportOtherReportsServiceDAOobj.exportF1RecreationCreditDebitNote(request, response, recbillid);
                                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                    JRPdfExporter exporter = new JRPdfExporter();
                                    exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                    exporter.exportReport();
                                    baosList.add(baos1);
                                } else {
                                    baos = CreatePDFObj.createPdf(requestmap, currencyid, recbillid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv, companyId, userId, sessionHandlerImpl.getCurrencyID(request));
                                    baosList.add(baos);
                                }
                            } else if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                                KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), recbillid);
                                CreditNote cn = (CreditNote) creditNoteResult.getEntityList().get(0);
                                if (cn.getSalesReturn() != null) {
                                    jasperPrint = accExportReportsServiceDAOobj.exportF1SalesReturnReport(request, response, cn.getSalesReturn().getID(), cn.getCreditNoteNumber());
                                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                    JRPdfExporter exporter = new JRPdfExporter();
                                    exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                    exporter.exportReport();
                                    baosList.add(baos1);
                                } else {
                                    if (cn.isOtherwise()) {
                                        jasperPrint = accExportOtherReportsServiceDAOobj.exportF1RecreationCreditDebitNote(request, response, recbillid);
                                        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                        JRPdfExporter exporter = new JRPdfExporter();
                                        exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                        exporter.exportReport();
                                        baosList.add(baos1);
                                    } else {
                                        baos = CreatePDFObj.createPdf(requestmap, currencyid, recbillid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv, companyId, userId, sessionHandlerImpl.getCurrencyID(request));
                                        baosList.add(baos);
                                    }
                                }
                            }
                        }
                    } else {
                        if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                            KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), billid);
                            DebitNote dn = (DebitNote) creditNoteResult.getEntityList().get(0);
                            if (dn.getPurchaseReturn() != null) {
                                List jasperPrint = accExportReportsServiceDAOobj.exportF1RecreationPurchaseReturn(request, response, dn.getPurchaseReturn().getID(), dn.getDebitNoteNumber());
                                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                JRPdfExporter exporter = new JRPdfExporter();
                                exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                exporter.exportReport();
                                baos = baos1;
                            } else if (dn.isOtherwise()) {
                                List jasperPrint = accExportOtherReportsServiceDAOobj.exportF1RecreationCreditDebitNote(request, response, billid);
                                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                JRPdfExporter exporter = new JRPdfExporter();
                                exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                exporter.exportReport();
                                baos = baos1;
                            } else {
                                baos = CreatePDFObj.createPdf(requestmap, currencyid, billid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv, companyId, userId, sessionHandlerImpl.getCurrencyID(request));
                                baosList.add(baos);
                            }
                        } else if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                            KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), billid);
                            CreditNote cn = (CreditNote) creditNoteResult.getEntityList().get(0);
                            List jasperPrint = null;
                            if (cn.getSalesReturn() != null) {
                                jasperPrint = accExportReportsServiceDAOobj.exportF1SalesReturnReport(request, response, cn.getSalesReturn().getID(), cn.getCreditNoteNumber());
                                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                JRPdfExporter exporter = new JRPdfExporter();
                                exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                exporter.exportReport();
                                baos = baos1;
                            } else {
                                if (cn.isOtherwise()) {
                                    jasperPrint = accExportOtherReportsServiceDAOobj.exportF1RecreationCreditDebitNote(request, response, billid);
                                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                    JRPdfExporter exporter = new JRPdfExporter();
                                    exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                    exporter.exportReport();
                                    baos = baos1;
                                } else {
                                    baos = CreatePDFObj.createPdf(requestmap, currencyid, billid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv, companyId, userId, sessionHandlerImpl.getCurrencyID(request));
                                    baosList.add(baos);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, e);
                }
            }else if ( (mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE) && (templateflag == Constants.FastenEnterprises_templateflag || templateflag == Constants.FastenHardwareEngineering_templateflag)) {
                try {
                    if (billsArray.length > 1) {
                        List jasperPrint;
                        for (int i = 0; i < billsArray.length; i++) {
                            String recbillid = billsArray[i];
                            if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                                KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), recbillid);
                                DebitNote dn = (DebitNote) creditNoteResult.getEntityList().get(0);
                                if (dn.getPurchaseReturn() != null) {
                                    jasperPrint = accExportOtherReportsServiceDAOobj.exportCNDNSRPRJasperForFasten(request, response, dn.getID(), dn.getDebitNoteNumber(), Constants.Acc_Debit_Note_ModuleId);//last param sent a constant module id
                                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                    JRPdfExporter exporter = new JRPdfExporter();
                                    exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                    exporter.exportReport();
                                    baosList.add(baos1);
                                } else {
                                    JasperPrint jp = accExportOtherReportsServiceDAOobj.exportDefaultFormatDebitNoteJasperReport(request, response, dn.getID());
                                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                    JRPdfExporter exp = new JRPdfExporter();
                                    exp.setParameter(JRExporterParameter.JASPER_PRINT, jp);
                                    exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, baos1);
                                    exp.exportReport();
                                    baosList.add(baos1);
                                }
                            } else if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                                KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), recbillid);
                                CreditNote cn = (CreditNote) creditNoteResult.getEntityList().get(0);
                                if (cn.getSalesReturn() != null) {
                                    jasperPrint = accExportOtherReportsServiceDAOobj.exportCNDNSRPRJasperForFasten(request, response, cn.getID(), cn.getCreditNoteNumber(), Constants.Acc_Credit_Note_ModuleId);//last param sent a constant module id
                                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                    JRPdfExporter exporter = new JRPdfExporter();
                                    exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                    exporter.exportReport();
                                    baosList.add(baos1);
                                } else {
                                    JasperPrint jp = accExportOtherReportsServiceDAOobj.exportDefaultFormatCreditNoteJasperReport(request, response, cn.getID());
                                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                    JRPdfExporter exp = new JRPdfExporter();
                                    exp.setParameter(JRExporterParameter.JASPER_PRINT, jp);
                                    exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, baos1);
                                    exp.exportReport();
                                    baosList.add(baos1);
                                }
                            }
                        }
                    } else {
                        if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                            KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), billid);
                            DebitNote dn = (DebitNote) creditNoteResult.getEntityList().get(0);
                            if (dn.getPurchaseReturn() != null) {
                                List jasperPrint = accExportOtherReportsServiceDAOobj.exportCNDNSRPRJasperForFasten(request, response, dn.getID(), dn.getDebitNoteNumber(), Constants.Acc_Debit_Note_ModuleId);//last param sent a constant module id
                                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                JRPdfExporter exporter = new JRPdfExporter();
                                exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                exporter.exportReport();
                                baos = baos1;
                            } else {
                                JasperPrint jp = accExportOtherReportsServiceDAOobj.exportDefaultFormatDebitNoteJasperReport(request, response, dn.getID());
                                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                JRPdfExporter exp = new JRPdfExporter();
                                exp.setParameter(JRExporterParameter.JASPER_PRINT, jp);
                                exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, baos1);
                                exp.exportReport();
                                baos = baos1;
                            }
                        } else if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                            KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), billid);
                            CreditNote cn = (CreditNote) creditNoteResult.getEntityList().get(0);
                            if (cn.getSalesReturn() != null) {
                                List jasperPrint = accExportOtherReportsServiceDAOobj.exportCNDNSRPRJasperForFasten(request, response, cn.getID(), cn.getCreditNoteNumber(), Constants.Acc_Credit_Note_ModuleId);//last param sent a constant module id
                                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                JRPdfExporter exporter = new JRPdfExporter();
                                exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                exporter.exportReport();
                                baos = baos1;
                            } else {
                                JasperPrint jp = accExportOtherReportsServiceDAOobj.exportDefaultFormatCreditNoteJasperReport(request, response, cn.getID());
                                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                JRPdfExporter exp = new JRPdfExporter();
                                exp.setParameter(JRExporterParameter.JASPER_PRINT, jp);
                                exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, baos1);
                                exp.exportReport();
                                baos = baos1;
                            }
                        }
                    }
                } catch (Exception e) {
                    Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, e);
                }
            }
            else if ( (mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE)) {
                try {

                    if (billsArray.length > 1) {
                        List jasperPrint;
                        for (int i = 0; i < billsArray.length; i++) {
                            String recbillid = billsArray[i];
                            if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                                KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), recbillid);
                                DebitNote dn = (DebitNote) creditNoteResult.getEntityList().get(0);
                                if (dn.getPurchaseReturn() != null) {
                                     if (templateflag == Constants.Tony_FiberGlass_templateflag) {
                                        jasperPrint = accExportOtherReportsServiceDAOobj.exportDefaultFormatDebitNoteJasperReportForTonyFibreGlass(request, response, dn.getID());
                                    } else if (templateflag == Constants.FastenEnterprises_templateflag || templateflag == Constants.FastenHardwareEngineering_templateflag){
                                        jasperPrint = accExportOtherReportsServiceDAOobj.exportCNDNSRPRJasperForFasten(request, response, dn.getID(), dn.getDebitNoteNumber(), Constants.Acc_Debit_Note_ModuleId);//last param sent a constant module id
                                    }else{
                                        jasperPrint = accExportReportsServiceDAOobj.exportPurchaseReturn(request,response, dn.getPurchaseReturn().getID(), dn.getDebitNoteNumber());
                                    }
                                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                    JRPdfExporter exporter = new JRPdfExporter();
                                    exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                    exporter.exportReport();
                                    baosList.add(baos1);
                                } else {
                                    if (templateflag == Constants.Tony_FiberGlass_templateflag) {
                                        jasperPrint = accExportOtherReportsServiceDAOobj.exportDefaultFormatDebitNoteJasperReportForTonyFibreGlass(request, response, dn.getID());
                                    } else {
                                        jasperPrint = accExportReportsServiceDAOobj.exportDebitNoteJasperReport(request, response, dn.getID());
                                    }
                                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                    JRPdfExporter exp = new JRPdfExporter();
                                    exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                    exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, baos1);
                                    exp.exportReport();
                                    baosList.add(baos1);
                                }
                            } else if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                                KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), recbillid);
                                CreditNote cn = (CreditNote) creditNoteResult.getEntityList().get(0);
                                if (cn.getSalesReturn() != null) {
                                    if (templateflag == Constants.Tony_FiberGlass_templateflag) {
                                        jasperPrint = accExportOtherReportsServiceDAOobj.exportDefaultFormatCreditNoteJasperReportForTonyFibreGlass(request, response, cn.getID());
                                    } else if (templateflag == Constants.FastenEnterprises_templateflag || templateflag == Constants.FastenHardwareEngineering_templateflag) {
                                        jasperPrint = accExportOtherReportsServiceDAOobj.exportCNDNSRPRJasperForFasten(request, response, cn.getID(), cn.getCreditNoteNumber(), Constants.Acc_Credit_Note_ModuleId);//last param sent a constant module id
                                    } else {
                                        jasperPrint = accExportReportsServiceDAOobj.exportSalesReturnJasper(request, response, cn.getSalesReturn().getID(), cn.getCreditNoteNumber());
                                    }
                                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                    JRPdfExporter exporter = new JRPdfExporter();
                                    exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                    exporter.exportReport();
                                    baosList.add(baos1);
                                } else {
                                    if (templateflag == Constants.Tony_FiberGlass_templateflag) {
                                        jasperPrint = accExportOtherReportsServiceDAOobj.exportDefaultFormatCreditNoteJasperReportForTonyFibreGlass(request, response, cn.getID());
                                    } else {
                                        jasperPrint = accExportReportsServiceDAOobj.exportCreditNoteJasperReport(request, response, cn.getID());
                                    }
                                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                    JRPdfExporter exp = new JRPdfExporter();
                                    exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                    exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, baos1);
                                    exp.exportReport();
                                    baosList.add(baos1);
                                }
                            }
                        }
                    } else {
                        if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                            KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), billid);
                            DebitNote dn = (DebitNote) creditNoteResult.getEntityList().get(0);
                            if (dn.getPurchaseReturn() != null) {
                                List jasperPrint = null;
                                if (templateflag == Constants.Tony_FiberGlass_templateflag) {
                                    jasperPrint = accExportOtherReportsServiceDAOobj.exportDefaultFormatDebitNoteJasperReportForTonyFibreGlass(request, response, dn.getID());
                                } else if (templateflag == Constants.FastenEnterprises_templateflag || templateflag == Constants.FastenHardwareEngineering_templateflag) {
                                    jasperPrint = accExportOtherReportsServiceDAOobj.exportCNDNSRPRJasperForFasten(request, response, dn.getID(), dn.getDebitNoteNumber(), Constants.Acc_Debit_Note_ModuleId);//last param sent a constant module id
                                } else {
                                    jasperPrint = accExportReportsServiceDAOobj.exportPurchaseReturn(request, response, dn.getPurchaseReturn().getID(), dn.getDebitNoteNumber());
                                }
                                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                JRPdfExporter exporter = new JRPdfExporter();
                                exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                exporter.exportReport();
                                baos = baos1;
                            } else {
                                List jasperPrint = null;
                                if (templateflag == Constants.Tony_FiberGlass_templateflag) {
                                    jasperPrint = accExportOtherReportsServiceDAOobj.exportDefaultFormatDebitNoteJasperReportForTonyFibreGlass(request, response, dn.getID());
                                } else {
                                    jasperPrint = accExportReportsServiceDAOobj.exportDebitNoteJasperReport(request, response, dn.getID());
                                }
                                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                JRPdfExporter exp = new JRPdfExporter();
                                exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, baos1);
                                exp.exportReport();
                                baos = baos1;
                            }
                        } else if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                            KwlReturnObject creditNoteResult = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), billid);
                            CreditNote cn = (CreditNote) creditNoteResult.getEntityList().get(0);
                            if (cn.getSalesReturn() != null) {
                                List jasperPrint = null;
                                if (templateflag == Constants.Tony_FiberGlass_templateflag) {
                                    jasperPrint = accExportOtherReportsServiceDAOobj.exportDefaultFormatCreditNoteJasperReportForTonyFibreGlass(request, response, cn.getID());
                                } else if (templateflag == Constants.FastenEnterprises_templateflag || templateflag == Constants.FastenHardwareEngineering_templateflag) {
                                    jasperPrint = accExportOtherReportsServiceDAOobj.exportCNDNSRPRJasperForFasten(request, response, cn.getID(), cn.getCreditNoteNumber(), Constants.Acc_Credit_Note_ModuleId);//last param sent a constant module id
                                } else {
                                    jasperPrint = accExportReportsServiceDAOobj.exportSalesReturnJasper(request, response, cn.getSalesReturn().getID(), cn.getCreditNoteNumber());
                                }
                                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                JRPdfExporter exporter = new JRPdfExporter();
                                exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
                                exporter.exportReport();
                                baos = baos1;
                            } else {
                                List jasperPrint = null;
                                if (templateflag == Constants.Tony_FiberGlass_templateflag) {
                                    jasperPrint = accExportOtherReportsServiceDAOobj.exportDefaultFormatCreditNoteJasperReportForTonyFibreGlass(request, response, cn.getID());
                                } else {
                                    jasperPrint = accExportReportsServiceDAOobj.exportCreditNoteJasperReport(request, response, cn.getID());
                                }
                                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                                JRPdfExporter exp = new JRPdfExporter();
                                exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrint);
                                exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, baos1);
                                exp.exportReport();
                                baos = baos1;
                            }
                        }
                    }
                } catch (Exception e) {
                    Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, e);
                }
            } else {//For other companies
                if (billsArray.length > 1) { //case for multiple records to print
                    Map<String, Object> paramMap = new HashMap<>();
                    Object prefObject = null;
                    boolean isExpenseType = false;
                    for (int i = 0; i < billsArray.length; i++) {
                        String recbillid = billsArray[i];
                        isExpenseType = isexpenseinv;
                        if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                            paramMap.put("ID", recbillid);
                            prefObject = kwlCommonTablesDAOObj.getRequestedObjectFields(GoodsReceipt.class, new String[]{"isExpenseType"}, paramMap);

                            if (prefObject != null && prefObject instanceof Boolean) {
                                isExpenseType = (Boolean) prefObject;
                            }
                        }
                        baos = CreatePDFObj.createPdf(requestmap, currencyid, recbillid, formatter, mode, amount, logoPath, cust, accname, address, isExpenseType, companyId, userId, sessionHandlerImpl.getCurrencyID(request));
                        baosList.add(baos);
                    }
                    paramMap = null;
                    prefObject = null;
                } else { //case for single record to print
                    baos = CreatePDFObj.createPdf(requestmap, currencyid, billid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv, companyId, userId, sessionHandlerImpl.getCurrencyID(request));
                }
            }

            if (baosList.size() > 1) {
                ExportrecordObj.concatPDFs(baosList, filename, response);
            } else if(baos!=null) {
                if (templateflag==Constants.tanejaHomes_templateflag && mode == 53) {
                    ExportrecordObj.directPrintToPrinter(singleFilename, baos, response,request.getParameter("companyid"));
                } else {
                ExportrecordObj.writeDataToFile(singleFilename, baos, response);
            }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            HibernateUtil.closeSession(session);
        }
        return new ModelAndView("jsonView-empty", "model", "Success");
    }

    public ModelAndView printRecords(HttpServletRequest request,
            HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");
        try {
            String filename = request.getParameter("filename") + ".pdf";
            double amount = Double.parseDouble(request.getParameter("amount"));
            int mode = Integer.parseInt(request.getParameter("mode"));
            String billid = request.getParameter("bills");
            boolean isexpenseinv = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isexpenseinv"))) {
                isexpenseinv = Boolean.parseBoolean((String) request.getParameter("isexpenseinv"));
            }
            String cust = request.getParameter("customer");
            String accname = request.getParameter("accname");
            String address = ((address = request.getParameter("address")) == null) ? " " : address;  //if address is null then print blank
            String logoPath = this.getServletContext().getInitParameter("platformURL");  //ProfileImageServlet.getProfileImagePath(request, true, null);
            KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Account.class.getName(), request.getParameter("personid"));
            Account account = (Account) cap.getEntityList().get(0);
            //Account account=(Account)session.get(Account.class, request.getParameter("personid"));
            String currencyid = account == null ? sessionHandlerImpl.getCurrencyID(request) : account.getCurrency().getCurrencyID();
            DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);

            createPrintWindowObj.createPrintWindow(request, response, currencyid, billid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            HibernateUtil.closeSession(session);
        }
        return new ModelAndView("jsonView-empty", "model", "Success");
    }

    public void downloadSampleFile(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray fileData = null;                           //Contains File Record json array
            JSONArray headerArr = null;                         //Contains title,header,align json array
            String filename = request.getParameter("filename");//For retrive the record from DB
            String samplefilename = filename;                 //For download Filename
            String moduleName = request.getParameter("moduleName");
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject companyresult = kwlCommonTablesDAOObj.getObject(Company.class.getName(), companyId);
            Company company = (Company) companyresult.getEntityList().get(0);
            String countryid= (company==null)?String.valueOf(0):company.getCountry().getID(); 
            String version = "";
            String isSampleFile = "";       //For samplefile check
            
            
            
            boolean withoutBOM = !StringUtil.isNullOrEmpty(request.getParameter("withoutBOM")) ? Boolean.parseBoolean(request.getParameter("withoutBOM")) : false;
            HashMap<String, Object> map = new HashMap<String, Object>();
            if (withoutBOM) {
                map.put("id", Constants.SAMPLE_ASSEMBLY_FILE_WITHOUT_BOM_ID);
                map.put("withoutBOM", withoutBOM);
            }
            map.put("filename", samplefilename);   
            map.put("countryid", countryid);
            map.put("moduleName", moduleName);
 
            KwlReturnObject result = kwlCommonTablesDAOObj.getSampleFileDataList(map);
            List list = result.getEntityList();
            if (list.size() > 0) {
                Object[] row = (Object[]) list.get(0);
                headerArr = new JSONArray(row[0].toString());
                fileData = new JSONArray(row[1].toString());
                samplefilename = row[2].toString();
                version = row[3].toString();
                isSampleFile = row[4].toString();

                jobj.put("title", headerArr.getJSONObject(0).optString("title", ""));
                jobj.put("header", headerArr.getJSONObject(1).optString("header", ""));
                jobj.put("align", headerArr.getJSONObject(2).optString("align", ""));
                jobj.put("data", fileData);
                jobj.put("filename", samplefilename+version);   //filename with version
                jobj.put("isSampleFile", isSampleFile);         
                exportDaoObj.processRequest(request, response, jobj);
                jobj.put("success", true);
            } else {
                try {
                    /*If Sample file data is not exist in DB, then it will download from Samplefile directory
                    */
                    String storagename = request.getParameter("storagename");
                    String filetype = request.getParameter("filetype");
                    String destinationDirectory = storageHandlerImpl.GetDocStorePath();
                    destinationDirectory += filetype.equalsIgnoreCase("csv") ? "importplans" : "xlsfiles";
                    File intgfile = new File(destinationDirectory + StorageHandler.GetFileSeparator() + storagename);

                    // if sample file is not exist then create it
                    if (!intgfile.exists()) {
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("storagename", storagename);
                        requestParams.put("inputFile", intgfile);

                        copyFileFromWarToStore(requestParams);
                        intgfile = new File(destinationDirectory + StorageHandler.GetFileSeparator() + storagename);
                    }

                    byte[] buff = new byte[(int) intgfile.length()];
                    try {
                        FileInputStream fis = new FileInputStream(intgfile);
                        int read = fis.read(buff);
                    } catch (IOException ex) {
                        filename = "file_not_found.txt";
                    }
                    response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "_v."+filetype + "\"");
                    response.setContentType("application/octet-stream");
                    response.setContentLength(buff.length);
                    response.getOutputStream().write(buff);
                    response.getOutputStream().flush();
                } catch (IOException ex) {
                    Logger.getLogger(ExportrecordController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(ExportrecordController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SessionExpiredException | ServiceException | JSONException | IOException ex ) {
            Logger.getLogger(ExportrecordController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void copyFileFromWarToStore(HashMap<String, Object> requestParams) {
        try {
            String storagename = "";
            if (requestParams.containsKey("storagename") && requestParams.get("storagename") != null) {
                storagename = (String) requestParams.get("storagename");
            }
            
            File inputFile = null;
            if (requestParams.containsKey("inputFile") && requestParams.get("inputFile") != null) {
                inputFile = (File) requestParams.get("inputFile");
            }

            // create directory if not exist
            if (inputFile != null && !inputFile.isDirectory()) {
                inputFile.getParentFile().mkdirs();
            }
            
            ServletContext context = this.getServletContext();
            String warFileFullPath = context.getRealPath("/WEB-INF/sampleFiles/" + storagename);
            File warFile = new File(warFileFullPath);
            
            if (inputFile != null && warFile.exists()) {
                FileInputStream input = new FileInputStream(warFile);
                java.io.FileOutputStream output = new java.io.FileOutputStream(inputFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                // copy the file content in bytes 
                while ((bytesRead = input.read(buffer)) > 0) {
                    output.write(buffer, 0, bytesRead);
                }
                
                output.close();
                input.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * print document designer Bank Reconciliation template
     */
    public void exportBankReconciliationDD(HttpServletRequest request, HttpServletResponse response){
        String recordid = "";
        try {
            String companyid = AccountingManager.getCompanyidFromRequest(request);
            //get accountid
            recordid = StringUtil.isNullOrEmpty(request.getParameter("recordids")) ? "" : request.getParameter("recordids");
            
            HashMap<String, Object>otherconfigrequestParams = new HashMap();
            LinkedHashMap<String, JSONArray> lineLevelData = new LinkedHashMap<String, JSONArray>();
            AccCustomData accCustomData = null;
            //get reconciliation details json
            JSONArray lineItemsArr = getReconciliationData(request, companyid, recordid);
            lineLevelData.put(recordid, lineItemsArr);
            //put moduleid and acocuntid in config map
            otherconfigrequestParams.put(Constants.moduleid, Constants.Bank_Reconciliation_ModuleId);
            otherconfigrequestParams.put("recordids",recordid);
            //export reconciliation template with details
            ExportRecordHandler.exportSingleGeneric(request, response, lineLevelData, accCustomData, customDesignDAObj,accCommonTablesDAO, accAccountDAOobj, accountingHandlerDAOobj,
                    velocityEngine,"",otherconfigrequestParams,accInvoiceServiceDAO,accGoodsReceiptServiceDAOObj);
        } catch(Exception e) {
            Logger.getLogger(ExportrecordController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    /**
     * create reconciliation details json
     * @param request
     * @param companyid
     * @param recordid
     * @return 
     */
    public JSONArray getReconciliationData(HttpServletRequest request, String companyid, String recordid){
        JSONArray returnJArr = new JSONArray();
        try {
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            //get base currency details
            String baseCurrencySymbol = extraCompanyPreferences.getCompany().getCurrency().getSymbol();
            String baseCurrencyCode = extraCompanyPreferences.getCompany().getCurrency().getCurrencyCode();
            //get date formatters
            DateFormat globaldf = authHandler.getGlobalDateFormat();
            DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(request);
            //get and convert start date and end date in user date format
            String startDate = userdf.format(globaldf.parse(request.getParameter("stdate")));
            String endDate = userdf.format(globaldf.parse(request.getParameter("enddate")));
            //get reconciliation details
            JSONObject jobj = accReportsController.getReconciliationData(request);
            
            JSONObject detailsTableData = new JSONObject();
            JSONArray depositsDetailsTableDataArr = new JSONArray();
            JSONArray checksDetailsTableDataArr = new JSONArray();
            double debitAmountInAccountCurrency = 0, debitAmountInBaseCurrency = 0, creditAmountInAccountCurrency = 0, creditAmountInBaseCurrency = 0;
            double openingBalanceBankBook = 0, balanceBankBook = 0, unclearedDeposits = 0, unclearedChecks = 0, balanceBankStatement = 0;
            String currencySymbol = "", currencyCode = "", accountName = "";
            //create reconciliation details json
            if(jobj.getJSONObject("data") != null){
                JSONObject data = jobj.getJSONObject("data");
                JSONArray depositsDetailsJArr = data.getJSONArray("left");
                JSONArray checksDetailsJArr = data.getJSONArray("right");
                //build Deposits and Other Credits - details table json
                for(int ind = 0; ind < depositsDetailsJArr.length(); ind++){
                    JSONObject depositsDetailsTableData = new JSONObject();
                    JSONObject depositsDetailsJobj = depositsDetailsJArr.getJSONObject(ind);

                    depositsDetailsTableData.put(CustomDesignerConstants.SrNo, ind+1);
                    depositsDetailsTableData.put(CustomDesignerConstants.DATE, depositsDetailsJobj.optString("d_dateinuserdf", ""));
                    depositsDetailsTableData.put(CustomDesignerConstants.ChequeDate, depositsDetailsJobj.optString("chequedateinuserdf", ""));
                    depositsDetailsTableData.put(CustomDesignerConstants.Chequeno, depositsDetailsJobj.optString("chequeno", ""));
                    depositsDetailsTableData.put(CustomDesignerConstants.CUSTOMER_VENDOR_NAME, depositsDetailsJobj.optString("d_accountname", ""));
                    depositsDetailsTableData.put(CustomDesignerConstants.REFERENCE_NO_DESC, depositsDetailsJobj.optString("description", ""));
                    depositsDetailsTableData.put(CustomDesignerConstants.JOURNAL_ENTRY_NO, depositsDetailsJobj.optString("d_entryno", ""));
                    depositsDetailsTableData.put(CustomDesignerConstants.TRANSACTION_ID, depositsDetailsJobj.optString("transactionID", ""));
                    depositsDetailsTableData.put(CustomDesignerConstants.RECEIVED_FROM, depositsDetailsJobj.optString("paidto", ""));
                    depositsDetailsTableData.put(CustomDesignerConstants.DEBIT_AMOUNT_IN_DOCUMENT_CURRENCY, depositsDetailsJobj.optDouble("d_amountintransactioncurrency", 0));
                    depositsDetailsTableData.put(CustomDesignerConstants.DEBIT_AMOUNT_IN_ACCOUNT_CURRENCY, depositsDetailsJobj.optDouble("d_amountinacc", 0));
                    depositsDetailsTableData.put(CustomDesignerConstants.DEBIT_AMOUNT_IN_BASE_CURRENCY, depositsDetailsJobj.optDouble("d_amount", 0));
                    depositsDetailsTableData.put(CustomDesignerConstants.MEMO, depositsDetailsJobj.optString("d_memo", ""));
                    depositsDetailsTableData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_SYMBOL, depositsDetailsJobj.optString("d_transCurrSymbol", ""));
                    //put deposits details object in details table
                    depositsDetailsTableDataArr.put(depositsDetailsTableData);
                    //calculate total debit amounts
                    debitAmountInAccountCurrency += depositsDetailsJobj.optDouble("d_amountinacc", 0);
                    debitAmountInBaseCurrency += depositsDetailsJobj.optDouble("d_amount", 0);
                }
                //build Checks and Payments - details table json
                for(int ind = 0; ind < checksDetailsJArr.length(); ind++){
                    JSONObject checksDetailsTableData = new JSONObject();
                    JSONObject checksDetailsJobj = checksDetailsJArr.getJSONObject(ind);

                    checksDetailsTableData.put(CustomDesignerConstants.SrNo, ind+1);
                    checksDetailsTableData.put(CustomDesignerConstants.DATE, checksDetailsJobj.optString("c_dateinuserdf", ""));
                    checksDetailsTableData.put(CustomDesignerConstants.ChequeDate, checksDetailsJobj.optString("chequedateinuserdf", ""));
                    checksDetailsTableData.put(CustomDesignerConstants.Chequeno, checksDetailsJobj.optString("chequeno", ""));
                    checksDetailsTableData.put(CustomDesignerConstants.CUSTOMER_VENDOR_NAME, checksDetailsJobj.optString("c_accountname", ""));
                    checksDetailsTableData.put(CustomDesignerConstants.REFERENCE_NO_DESC, checksDetailsJobj.optString("description", ""));
                    checksDetailsTableData.put(CustomDesignerConstants.JOURNAL_ENTRY_NO, checksDetailsJobj.optString("c_entryno", ""));
                    checksDetailsTableData.put(CustomDesignerConstants.TRANSACTION_ID, checksDetailsJobj.optString("transactionID", ""));
                    checksDetailsTableData.put(CustomDesignerConstants.PAID_TO, checksDetailsJobj.optString("paidto", ""));
                    checksDetailsTableData.put(CustomDesignerConstants.CREDIT_AMOUNT_IN_DOCUMENT_CURRENCY, checksDetailsJobj.optDouble("c_amountintransactioncurrency", 0));
                    checksDetailsTableData.put(CustomDesignerConstants.CREDIT_AMOUNT_IN_ACCOUNT_CURRENCY, checksDetailsJobj.optDouble("c_amountinacc", 0));
                    checksDetailsTableData.put(CustomDesignerConstants.CREDIT_AMOUNT_IN_BASE_CURRENCY, checksDetailsJobj.optDouble("c_amount", 0));
                    checksDetailsTableData.put(CustomDesignerConstants.MEMO, checksDetailsJobj.optString("c_memo", ""));
                    checksDetailsTableData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_SYMBOL, checksDetailsJobj.optString("c_transCurrSymbol", ""));
                    //put checks details object in details table
                    checksDetailsTableDataArr.put(checksDetailsTableData);
                    //calculate total credit amounts
                    creditAmountInAccountCurrency += checksDetailsJobj.optDouble("c_amountinacc", 0);
                    creditAmountInBaseCurrency += checksDetailsJobj.optDouble("c_amount", 0);
                }
                //get global level fields from json
                openingBalanceBankBook = data.optDouble("openingbankdatainacc", 0);
                balanceBankBook = data.optDouble("bankdatainacc", 0);
                unclearedDeposits = Math.abs(data.optJSONArray("totalinacc").optDouble(0, 0)) == 0 ? 0 : data.optJSONArray("totalinacc").optDouble(0, 0);
                unclearedChecks = data.optJSONArray("totalinacc").optDouble(1, 0);
                balanceBankStatement = balanceBankBook + unclearedChecks + unclearedDeposits;
                currencySymbol = data.optString("currencysymbol", "");
                currencyCode = data.optString("currencycode", "");
                accountName = data.optString("accountname", "");
            }
            //put details table data and currency details in json
            detailsTableData.put("deposits_and_other_credits", depositsDetailsTableDataArr);
            detailsTableData.put("checks_and_payments", checksDetailsTableDataArr);
            detailsTableData.put("isDetailsTableData", true);
            detailsTableData.put("currencysymbol", currencySymbol);
            detailsTableData.put("currencycode", currencyCode);
            detailsTableData.put("basecurrencysymbol", baseCurrencySymbol);
            detailsTableData.put("basecurrencycode", baseCurrencyCode);
            returnJArr.put(detailsTableData);
            //put global level details in json
            JSONObject summaryData = new JSONObject();
            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.DEBIT_AMOUNT_IN_ACCOUNT_CURRENCY, debitAmountInAccountCurrency);
            summaryData.put(CustomDesignerConstants.DEBIT_AMOUNT_IN_BASE_CURRENCY, debitAmountInBaseCurrency);
            summaryData.put(CustomDesignerConstants.CREDIT_AMOUNT_IN_ACCOUNT_CURRENCY, creditAmountInAccountCurrency);
            summaryData.put(CustomDesignerConstants.CREDIT_AMOUNT_IN_BASE_CURRENCY, creditAmountInBaseCurrency);
            summaryData.put(CustomDesignerConstants.OPENING_BALANCE_BANK_BOOK, openingBalanceBankBook);
            summaryData.put(CustomDesignerConstants.BALANCE_BANK_BOOK, balanceBankBook);
            summaryData.put(CustomDesignerConstants.UNCLEARED_DEPOSITS, unclearedDeposits);
            summaryData.put(CustomDesignerConstants.UNCLEARED_CHECKS, unclearedChecks);
            summaryData.put(CustomDesignerConstants.BALANCE_BANK_STATEMENT, balanceBankStatement);
            summaryData.put(CustomDesignerConstants.FROM_DATE, startDate);
            summaryData.put(CustomDesignerConstants.TO_DATE, endDate);
            summaryData.put(CustomDesignerConstants.BANK_NAME, accountName);
            
            returnJArr.put(summaryData);
        } catch (Exception e) {
            Logger.getLogger(ExportrecordController.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnJArr;
    }
     
}
