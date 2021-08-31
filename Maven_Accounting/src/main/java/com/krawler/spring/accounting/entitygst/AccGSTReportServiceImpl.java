/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.entitygst;

import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.currency.accCurrencyImpl;
import com.krawler.spring.accounting.gst.services.GSTR1ServiceDao;
import com.krawler.spring.accounting.gst.services.GSTRConstants;
import com.krawler.spring.accounting.gst.services.gstr2.GSTR2Dao;
import com.krawler.spring.accounting.gst.services.gstr2.GSTR2Service;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Locale;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import java.util.List;
import java.util.ArrayList;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

/**
 * Class to be used for GSTR report for INDIAN country.
 *
 * @author swapnil.khandre
 */
public class AccGSTReportServiceImpl implements AccGSTReportServiceDAO, GSTR3BConstants {

    private AccEntityGstDao accEntityGstDao;
    private GSTR1ServiceDao gSTR1DeskeraServiceDao;
    private GSTR2Dao gstr2Dao;
    private GSTR2Service gSTR2Service;
    private AccountingHandlerDAO accountingHandlerDAO;
    private MessageSource messageSource;
    private accCurrencyImpl accCurrencyDAO;
    private kwlCommonTablesDAO kwlCommonTablesDAO;
    private AccEntityGstService accEntityGstService;

    public void setAccEntityGstDao(AccEntityGstDao accEntityGstDao) {
        this.accEntityGstDao = accEntityGstDao;
    }

    public void setgSTR1DeskeraServiceDao(GSTR1ServiceDao gSTR1DeskeraServiceDao) {
        this.gSTR1DeskeraServiceDao = gSTR1DeskeraServiceDao;
    }

    public void setGstr2Dao(GSTR2Dao gstr2Dao) {
        this.gstr2Dao = gstr2Dao;
    }

    public void setgSTR2Service(GSTR2Service gSTR2Service) {
        this.gSTR2Service = gSTR2Service;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setAccCurrencyDAO(accCurrencyImpl accCurrencyDAO) {
        this.accCurrencyDAO = accCurrencyDAO;
    }

    public void setKwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAO) {
        this.kwlCommonTablesDAO = kwlCommonTablesDAO;
    }

    public void setAccEntityGstService(AccEntityGstService accEntityGstService) {
        this.accEntityGstService = accEntityGstService;
    }

    /**
     * Method to be used for creating column model for GSTR3B detail report.
     *
     * @param jarrRecords
     * @param jarrColumns
     * @param params
     * @throws JSONException
     */
    @Override
    public void getColumnModelForGSTR3BDetails(JSONArray jarrRecords, JSONArray jarrColumns, JSONObject params) throws JSONException {
        try {
            Locale requestcontextutilsobj = null;
            if (params.has("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) params.opt("requestcontextutilsobj");
            }
            JSONObject jobjTemp = new JSONObject();
            List<String> list = new ArrayList();
            list.add(DATAINDEX_DOCTYPE);
            list.add(DATAINDEX_TRANSACTION_NUMBER);
            list.add(DATAINDEX_GSTIN);
            list.add(DATAINDEX_DATE);
            list.add(DATAINDEX_PERSONNAME);
            list.add(DATAINDEX_POS);
            list.add(DATAINDEX_TAXABLE_AMOUNT);
            list.add(DATAINDEX_TOTAL_AMOUNT);
            list.add(DATAINDEX_TOTAL_TAX);
            list.add(DATAINDEX_SGSTAMOUNT);
            list.add(DATAINDEX_CGSTAMOUNT);
            list.add(DATAINDEX_IGSTAMOUNT);
            list.add(DATAINDEX_CESS);
            list.add(DATAINDEX_TAX_CLASS_TYPE);
            for (String rec : list) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                if (rec.equals(DATAINDEX_TAXABLE_AMOUNT) || rec.equals(DATAINDEX_IGSTAMOUNT) || rec.equals(DATAINDEX_CGSTAMOUNT) || rec.equals(DATAINDEX_SGSTAMOUNT)
                        || rec.equals(DATAINDEX_TOTAL_TAX) || rec.equals(DATAINDEX_TOTAL_AMOUNT) || rec.equals(DATAINDEX_CESS)) {
                    jobjTemp.put("type", "float");
                }
                jarrRecords.put(jobjTemp);
            }

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.field.DocumentType", null, requestcontextutilsobj) + "<b>");
            jobjTemp.put("dataIndex", DATAINDEX_DOCTYPE);
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 120);
            jobjTemp.put("pdfwidth", 120);
            jarrColumns.put(jobjTemp);

            if (params.optBoolean("showtypeforexempt", false)) {
                /**
                 * Show column in GSTR1 Nil rated section
                 */
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.product.gridType", null, requestcontextutilsobj) + "<b>");
                jobjTemp.put("dataIndex", DATAINDEX_TAX_CLASS_TYPE);
                jobjTemp.put("align", "left");
                jobjTemp.put("hidden", true);
                jobjTemp.put("width", 120);
                jobjTemp.put("pdfwidth", 120);
                jarrColumns.put(jobjTemp);
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.gstr1.gstin", null, requestcontextutilsobj) + "<b>");
                jobjTemp.put("dataIndex", DATAINDEX_GSTIN);
                jobjTemp.put("align", "left");
                jobjTemp.put("width", 120);
                jobjTemp.put("pdfwidth", 120);
                jarrColumns.put(jobjTemp);
            }
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.inventoryList.date", null, requestcontextutilsobj) + "<b>");
            jobjTemp.put("dataIndex", DATAINDEX_DATE);
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 120);
            jobjTemp.put("pdfwidth", 120);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + "Person Name" + "<b>");
            jobjTemp.put("dataIndex", DATAINDEX_PERSONNAME);
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 120);
            jobjTemp.put("pdfwidth", 120);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + "Place of Supply" + "<b>");
            jobjTemp.put("dataIndex", DATAINDEX_POS);
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 120);
            jobjTemp.put("pdfwidth", 120);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.cnList.TransNo", null, requestcontextutilsobj) + "<b>");
            jobjTemp.put("dataIndex", DATAINDEX_TRANSACTION_NUMBER);
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 120);
            jobjTemp.put("pdfwidth", 120);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.gstr1.taxableamount", null, requestcontextutilsobj) + "<b>");
            jobjTemp.put("dataIndex", DATAINDEX_TAXABLE_AMOUNT);
            jobjTemp.put("align", "right");
            jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
            jobjTemp.put("width", 120);
            jobjTemp.put("pdfwidth", 120);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.gstr1.IGST", null, requestcontextutilsobj) + "<b>");
            jobjTemp.put("dataIndex", DATAINDEX_IGSTAMOUNT);
            jobjTemp.put("width", 120);
            jobjTemp.put("summaryType", "sum");
            jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
            jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfwidth", 120);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.gstr1.CGST", null, requestcontextutilsobj) + "<b>");
            jobjTemp.put("dataIndex", DATAINDEX_CGSTAMOUNT);
            jobjTemp.put("width", 120);
            jobjTemp.put("summaryType", "sum");
            jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
            jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfwidth", 120);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.gstr1.SGST", null, requestcontextutilsobj) + "<b>");
            jobjTemp.put("dataIndex", DATAINDEX_SGSTAMOUNT);
            jobjTemp.put("width", 120);
            jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
            jobjTemp.put("summaryType", "sum");
            jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfwidth", 120);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.gstr1.cessamount", null, requestcontextutilsobj) + "<b>");
            jobjTemp.put("dataIndex", DATAINDEX_CESS);
            jobjTemp.put("align", "right");
            jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
            jobjTemp.put("summaryType", "sum");
            jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
            jobjTemp.put("width", 120);
            jobjTemp.put("pdfwidth", 120);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.invoice.TotalTaxAmt", null, requestcontextutilsobj) + "<b>");
            jobjTemp.put("dataIndex", DATAINDEX_TOTAL_TAX);
            jobjTemp.put("align", "right");
            jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
            jobjTemp.put("summaryType", "sum");
            jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
            jobjTemp.put("width", 120);
            jobjTemp.put("pdfwidth", 120);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.gstr1.totalAmountInclTaxes", null, requestcontextutilsobj) + "<b>");
            jobjTemp.put("dataIndex", DATAINDEX_TOTAL_AMOUNT);
            jobjTemp.put("align", "right");
            jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
            jobjTemp.put("summaryType", "sum");
            jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
            jobjTemp.put("width", 120);
            jobjTemp.put("pdfwidth", 120);
            jarrColumns.put(jobjTemp);
        } catch (Exception ex) {
            Logger.getLogger(AccGSTReportServiceImpl.class).log(Level.WARN, ex.getMessage());
        }
    }

    /**
     *
     * @param invoiceData
     * @param reqParams
     * @param companyId
     * @return
     * @throws JSONException
     */
    @Override
    public JSONObject getSalesInvoiceJSONArrayForGSTR3B(List invoiceData, JSONObject reqParams, String companyId) throws JSONException {
        JSONArray b2bArr = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        JSONArray columnDataArr = new JSONArray();
        double totalTaxableAmount, totalIGST, totalCGST, totalSGST, totalCess;
        totalCGST = totalIGST = totalSGST = totalTaxableAmount = totalCess = 0;
        String idate, invNum, place, customerName, supplierName, ecomGstin, Gstin;
        int count=0;
        gSTR1DeskeraServiceDao.setB2BInvoiceList(b2bArr, invoiceData, reqParams);
        for (int i = 0; i < b2bArr.length(); i++) {
            JSONObject inv = b2bArr.getJSONObject(i);
            Gstin = inv.getString("ctin");
            JSONArray invArr = inv.getJSONArray("inv");
            for (int j = 0; j < invArr.length(); j++) {
                double taxableAmtInv = 0.0;
                double totalTax = 0.0;
                double totalAmt = 0.0;
                double cess = 0.0;
                double igstAmt = 0.0;
                double cgstAmt = 0.0;
                double sgstAmt = 0.0;
                JSONObject Itms = invArr.getJSONObject(j);
                JSONArray itmsArr = Itms.getJSONArray("itms");
                idate = Itms.getString("idt");
                invNum = Itms.getString("inum");
                place = Itms.getString("pos");
                customerName = Itms.optString("customerName");
                ecomGstin = Itms.getString("etin");
                supplierName = Itms.optString("vendorname");
                for (int k = 0; k < itmsArr.length(); k++) {
                    JSONObject itmsDetail = itmsArr.getJSONObject(k);
                    JSONObject ItmsDetail = itmsDetail.getJSONObject("itm_det");
                    Double cgst = ItmsDetail.optDouble("camt");
                    Double sgst = ItmsDetail.optDouble("samt");
                    Double igst = ItmsDetail.optDouble("iamt");
                    Double csgst = ItmsDetail.optDouble("csamt");
                    cess += csgst;
                    igstAmt += igst;
                    sgstAmt += sgst;
                    cgstAmt += cgst;
                    taxableAmtInv += ItmsDetail.optDouble("txval");
                    totalTax += (cgst + sgst + igst + csgst);
                }
                totalAmt = (taxableAmtInv + totalTax);
                totalTaxableAmount += taxableAmtInv;
                totalCess += cess;
                totalIGST += igstAmt;
                totalCGST += cgstAmt;
                totalSGST += sgstAmt;
                count++;
                if (!(reqParams.has("reportid") && reqParams.optInt("reportid", 0) == Constants.GSTR3B_Summary_Report)) {
                    JSONObject columnData = new JSONObject();
                    //Determine whether the supplier is Inter-State or Intra-State (Comparing with local State of Entity)
                    if (!StringUtil.isNullOrEmpty(place) && place.equals(reqParams.optString("entityState"))) {
                        columnData.put("supplierType", "Intra-State");
                    } else {
                        columnData.put("supplierType", "Inter-State");
                    }
                    columnData.put(DATAINDEX_TAXABLE_AMOUNT, authHandler.formattedAmount(taxableAmtInv, companyId));
                    columnData.put(DATAINDEX_TOTAL_TAX, authHandler.formattedAmount(totalTax, companyId));
                    columnData.put(DATAINDEX_TOTAL_AMOUNT, authHandler.formattedAmount(totalAmt, companyId));
                    columnData.put(DATAINDEX_DATE, idate);
                    columnData.put(DATAINDEX_TRANSACTION_NUMBER, invNum);
                    columnData.put(DATAINDEX_POS, place);
                    columnData.put(DATAINDEX_CESS, authHandler.formattedAmount(cess, companyId));
                    columnData.put(DATAINDEX_IGSTAMOUNT, authHandler.formattedAmount(igstAmt, companyId));
                    columnData.put(DATAINDEX_SGSTAMOUNT, authHandler.formattedAmount(sgstAmt, companyId));
                    columnData.put(DATAINDEX_CGSTAMOUNT, authHandler.formattedAmount(cgstAmt, companyId));
                    columnData.put(DATAINDEX_GSTIN, Gstin);
                    columnData.put(DATAINDEX_PERSONNAME, customerName);
                    columnData.put(GSTRConstants.supplierinvoiceno, Itms.optString(GSTRConstants.supplierinvoiceno, ""));
                    columnData.put(GSTRConstants.vendorname, Itms.optString(GSTRConstants.vendorname, ""));
                    columnData.put(GSTRConstants.invoiceid, Itms.optString("invoiceId", ""));
                    columnData.put(DATAINDEX_DOCTYPE, "Sales Invoice");
                    columnDataArr.put(columnData);
                } 
            }
        }
//        System.out.println("Taxable Value:"+totalTaxableAmount);
//        System.out.println("CSGT Value:"+totalCGST);
//        System.out.println("SSGT Value:"+totalSGST);
//        System.out.println("CESS Value:"+totalCess);
//        System.out.println("IGST Value:"+totalIGST);
        if (reqParams.has("reportid") && reqParams.optInt("reportid", 0) == Constants.GSTR3B_Summary_Report) {
            jSONObject.put("taxableamt", authHandler.formattedAmount(totalTaxableAmount, companyId));
            jSONObject.put("igst", authHandler.formattedAmount(totalIGST, companyId));
            jSONObject.put("cgst", authHandler.formattedAmount(totalCGST, companyId));
            jSONObject.put("sgst", authHandler.formattedAmount(totalSGST, companyId));
            jSONObject.put("csgst", authHandler.formattedAmount(totalCess, companyId));
            jSONObject.put("totaltax", authHandler.formattedAmount(totalIGST + totalCGST + totalSGST + totalCess, companyId));
            jSONObject.put("totalamount", authHandler.formattedAmount(totalTaxableAmount + totalIGST + totalCGST + totalSGST + totalCess, companyId));
            jSONObject.put("count", count);
        } else {
            jSONObject.put("data", columnDataArr);
        }

        return jSONObject;
    }

    /**
     *
     * @param invoiceData
     * @param reqParams
     * @param companyId
     * @return
     * @throws JSONException
     */
    @Override
    public JSONObject getPurchaseInvoiceJSONArrayForGSTR3B(List invoiceData, JSONObject reqParams, String companyId) throws JSONException {
        JSONArray b2bArr = new JSONArray();
        JSONArray columnDataArr = new JSONArray();
        double totalTaxableAmount, totalIGST, totalCGST, totalSGST, totalCess;
        totalCGST = totalIGST = totalSGST = totalTaxableAmount = totalCess = 0;
        JSONObject jSONObject = new JSONObject();
        gSTR2Service.setB2BInvoiceDetailsList(b2bArr, invoiceData, reqParams);
        String idate, invNum, place, customerName, supplierName, ecomGstin, Gstin;
        for (int i = 0; i < b2bArr.length(); i++) {
            JSONObject inv = b2bArr.getJSONObject(i);
            Gstin = inv.getString("ctin");
            JSONArray invArr = inv.getJSONArray("inv");
            for (int j = 0; j < invArr.length(); j++) {
                double taxableAmtInv = 0.0;
                double totalTax = 0.0;
                double totalAmt = 0.0;
                double cess = 0.0;
                double igstAmt = 0.0;
                double cgstAmt = 0.0;
                double sgstAmt = 0.0;
                JSONObject Itms = invArr.getJSONObject(j);
                JSONArray itmsArr = Itms.getJSONArray("itms");
                idate = Itms.getString("idt");
                invNum = Itms.getString("inum");
                place = Itms.getString("pos");
                customerName = Itms.optString("customerName");
                ecomGstin = Itms.getString("etin");
                supplierName = Itms.optString("vendorname");
                for (int k = 0; k < itmsArr.length(); k++) {
                    JSONObject itmsDetail = itmsArr.getJSONObject(k);
                    JSONObject ItmsDetail = itmsDetail.getJSONObject("itm_det");
                    Double cgst = ItmsDetail.optDouble("camt");
                    Double sgst = ItmsDetail.optDouble("samt");
                    Double igst = ItmsDetail.optDouble("iamt");
                    Double csgst = ItmsDetail.optDouble("csamt");
                    cess += csgst;
                    igstAmt += igst;
                    sgstAmt += sgst;
                    cgstAmt += cgst;
//                    if (ItmsDetail.optDouble("rt") != 0.0) {
                    taxableAmtInv += ItmsDetail.optDouble("txval");
                    totalTax += (cgst + sgst + igst + csgst);
//                }
                }
                totalAmt = (taxableAmtInv + totalTax);
                totalTaxableAmount += taxableAmtInv;
                totalCess += cess;
                totalIGST += igstAmt;
                totalCGST += cgstAmt;
                totalSGST += sgstAmt;
                if (!(reqParams.has("reportid") && reqParams.optInt("reportid", 0) == Constants.GSTR3B_Summary_Report)) {
                    JSONObject columnData = new JSONObject();
                    //Determine whether the supplier is Inter-State or Intra-State (Comparing with local State of Entity)
                    if (!StringUtil.isNullOrEmpty(place) && place.equals(reqParams.optString("entityState"))) {
                        columnData.put("supplierType", "Intra-State");
                    } else {
                        columnData.put("supplierType", "Inter-State");
                    }
                    columnData.put(GSTRConstants.supplierinvoiceno, Itms.optString(GSTRConstants.supplierinvoiceno, ""));
                    columnData.put(GSTRConstants.vendorname, Itms.optString(GSTRConstants.vendorname, ""));
                    columnData.put(DATAINDEX_TAXABLE_AMOUNT, authHandler.formattedAmount(taxableAmtInv, companyId));
                    columnData.put(DATAINDEX_TOTAL_TAX, authHandler.formattedAmount(totalTax, companyId));
                    columnData.put(DATAINDEX_TOTAL_AMOUNT, authHandler.formattedAmount(totalAmt, companyId));
                    columnData.put(DATAINDEX_DATE, idate);
                    columnData.put(DATAINDEX_POS, place);
                    columnData.put(DATAINDEX_TRANSACTION_NUMBER, invNum);
                    columnData.put(DATAINDEX_CESS, authHandler.formattedAmount(cess, companyId));
                    columnData.put(DATAINDEX_IGSTAMOUNT, authHandler.formattedAmount(igstAmt, companyId));
                    columnData.put(DATAINDEX_SGSTAMOUNT, authHandler.formattedAmount(sgstAmt, companyId));
                    columnData.put(DATAINDEX_CGSTAMOUNT, authHandler.formattedAmount(cgstAmt, companyId));
                    columnData.put(DATAINDEX_GSTIN, Gstin);
                    columnData.put(DATAINDEX_PERSONNAME, supplierName);
                    columnData.put(DATAINDEX_DOCTYPE, "Purchase Invoice");

                    columnDataArr.put(columnData);
                }
            }
        }
        
        if (reqParams.has("reportid") && reqParams.optInt("reportid", 0) == Constants.GSTR3B_Summary_Report) {
            jSONObject.put("taxableamt", authHandler.formattedAmount(totalTaxableAmount, companyId));
            jSONObject.put("igst", authHandler.formattedAmount(totalIGST, companyId));
            jSONObject.put("cgst", authHandler.formattedAmount(totalCGST, companyId));
            jSONObject.put("sgst", authHandler.formattedAmount(totalSGST, companyId));
            jSONObject.put("csgst", authHandler.formattedAmount(totalCess, companyId));
            jSONObject.put("totaltax", authHandler.formattedAmount(totalIGST + totalCGST + totalSGST + totalCess, companyId));
            jSONObject.put("totalamount", authHandler.formattedAmount(totalTaxableAmount + totalIGST + totalCGST + totalSGST + totalCess, companyId));
        } else {
            jSONObject.put("data", columnDataArr);
        }
        return jSONObject;
    }

    @Override
    public JSONObject getCreditNoteJSONArrayForGSTR3B(List cnData, JSONObject reqParams, String companyId) throws JSONException {
        JSONArray cdnrArr = new JSONArray();
        reqParams.put("cdnr", true);
        JSONObject jSONObject = new JSONObject();
        double totalTaxableAmount, totalIGST, totalCGST, totalSGST, totalCess;
        totalCGST = totalIGST = totalSGST = totalTaxableAmount = totalCess = 0;
        int count=0;
        gSTR1DeskeraServiceDao.setB2BCNDetailsList(cdnrArr, cnData, reqParams);
        JSONArray columnDataArr = new JSONArray();
        String invdate, invNumber, place, customerName, supplierName, Gstin, invType, originalDate, originalInvNum,nttype;
        for (int cdnr = 0; cdnr < cdnrArr.length(); cdnr++) {
            double totalTax = 0.0;
            double totalAmt = 0.0;
            double cess = 0.0;
            double taxableAmtInv = 0.0;
            JSONObject jdata = cdnrArr.optJSONObject(cdnr);
            JSONArray invArr = jdata.optJSONArray("nt");
            Gstin = jdata.optString("ctin");
            for (int invoice = 0; invoice < invArr.length(); invoice++) {
                taxableAmtInv = 0.0;
                totalTax = 0.0;
                cess = 0.0;
                double igstAmt = 0.0;
                double cgstAmt = 0.0;
                double sgstAmt = 0.0;
                JSONObject Itms = invArr.optJSONObject(invoice);
                JSONArray itmsArr = Itms.optJSONArray("itms");
                invdate = Itms.optString("idt");
                invNumber = Itms.optString("inum");
                invType = Itms.optString("inv_typ");
                originalDate = Itms.optString("nt_dt");
                originalInvNum = Itms.optString("nt_num");
                String reason = Itms.optString("rsn");
                place = Itms.optString("pos");
                customerName = Itms.optString("customerName");
                supplierName = Itms.optString("vendorname");
                nttype = Itms.optString("ntty");
                for (int k = 0; k < itmsArr.length(); k++) {
                    JSONObject itmsDetail = itmsArr.optJSONObject(k);
                    JSONObject ItmsDetail = itmsDetail.optJSONObject("itm_det");
                    Double cgst = ItmsDetail.optDouble("camt");
                    Double sgst = ItmsDetail.optDouble("samt");
                    Double igst = ItmsDetail.optDouble("iamt");
                    Double csgst = ItmsDetail.optDouble("csamt");
                    cess += csgst;
                    cgstAmt += cgst;
                    sgstAmt += sgst;
                    igstAmt += igst;
                    Double TaxableAmt = ItmsDetail.optDouble("txval");
                    totalTax += (cgst + sgst + igst + csgst);
                    taxableAmtInv += TaxableAmt;
                }
                totalAmt = 0.0;
                totalAmt += (taxableAmtInv + totalTax);
                
                totalTaxableAmount += taxableAmtInv;
                totalCGST += cgstAmt;
                totalSGST += sgstAmt;
                totalIGST += igstAmt;
                totalCess += cess;
                count++;
                if (!(reqParams.has("reportid") && reqParams.optInt("reportid", 0) == Constants.GSTR3B_Summary_Report)) {
                    JSONObject columnData = new JSONObject();
                    columnData.put(DATAINDEX_TAXABLE_AMOUNT, authHandler.formattedAmount(taxableAmtInv, companyId));
                    columnData.put(DATAINDEX_TOTAL_TAX, authHandler.formattedAmount(totalTax, companyId));
                    columnData.put(DATAINDEX_TOTAL_AMOUNT, authHandler.formattedAmount(totalAmt, companyId));
                    columnData.put(DATAINDEX_DATE, originalDate);
                    columnData.put(DATAINDEX_TRANSACTION_NUMBER, originalInvNum);
                    columnData.put(DATAINDEX_POS, place);
                    columnData.put(DATAINDEX_IGSTAMOUNT, authHandler.formattedAmount(igstAmt, companyId));
                    columnData.put(DATAINDEX_SGSTAMOUNT, authHandler.formattedAmount(sgstAmt, companyId));
                    columnData.put(DATAINDEX_CGSTAMOUNT, authHandler.formattedAmount(cgstAmt, companyId));
                    columnData.put(DATAINDEX_CESS, authHandler.formattedAmount(cess, companyId));
                    columnData.put(DATAINDEX_GSTIN, Gstin);
                    columnData.put(DATAINDEX_PERSONNAME, customerName);
                    if (nttype.equalsIgnoreCase("D")) {
                        columnData.put(DATAINDEX_DOCTYPE, "Debit Note");
                    } else if (nttype.equalsIgnoreCase("R")) {
                        columnData.put(DATAINDEX_DOCTYPE, "Sales Refund");
                    } else {
                        columnData.put(DATAINDEX_DOCTYPE, "Credit Note");
                    }
                    columnDataArr.put(columnData);
                }
            }
        }
        if (reqParams.has("reportid") && reqParams.optInt("reportid", 0) == Constants.GSTR3B_Summary_Report) {
            jSONObject.put("taxableamt", authHandler.formattedAmount(totalTaxableAmount, companyId));
            jSONObject.put("igst", authHandler.formattedAmount(totalIGST, companyId));
            jSONObject.put("cgst", authHandler.formattedAmount(totalCGST, companyId));
            jSONObject.put("sgst", authHandler.formattedAmount(totalSGST, companyId));
            jSONObject.put("csgst", authHandler.formattedAmount(totalCess, companyId));
            jSONObject.put("totaltax", authHandler.formattedAmount(totalIGST + totalCGST + totalSGST + totalCess, companyId));
            jSONObject.put("totalamount", authHandler.formattedAmount(totalTaxableAmount + totalIGST + totalCGST + totalSGST + totalCess, companyId));
            jSONObject.put("count", count);
        } else {
            jSONObject.put("data", columnDataArr);
        }
        return jSONObject;
    }
    /**
     * Function to get DN/CN data for computation report
     *
     * @param cnData
     * @param reqParams
     * @param companyId
     * @return
     * @throws JSONException
     */
    public JSONObject getDebitNoteJSONArrayForGSTR3B(List cnData, JSONObject reqParams, String companyId) throws JSONException {
        JSONArray cdnrArr = new JSONArray();
        reqParams.put("cdnr", true);
        JSONObject jSONObject = new JSONObject();
        double totalTaxableAmount, totalIGST, totalCGST, totalSGST, totalCess;
        totalCGST = totalIGST = totalSGST = totalTaxableAmount = totalCess = 0;
        int count=0;
        gSTR2Service.setDNCNDetailsList(cdnrArr, cnData, reqParams);
        JSONArray columnDataArr = new JSONArray();
        String invdate, invNumber, place, vendorname, supplierName, Gstin, invType, originalDate, originalInvNum;
        for (int cdnr = 0; cdnr < cdnrArr.length(); cdnr++) {
            double totalTax = 0.0;
            double totalAmt = 0.0;
            double cess = 0.0;
            double taxableAmtInv = 0.0;
            JSONObject jdata = cdnrArr.optJSONObject(cdnr);
            JSONArray invArr = jdata.optJSONArray("nt");
            Gstin = jdata.optString("ctin");
            for (int invoice = 0; invoice < invArr.length(); invoice++) {
                taxableAmtInv = 0.0;
                totalTax = 0.0;
                cess = 0.0;
                double igstAmt = 0.0;
                double cgstAmt = 0.0;
                double sgstAmt = 0.0;
                JSONObject Itms = invArr.optJSONObject(invoice);
                JSONArray itmsArr = Itms.optJSONArray("itms");
                invdate = Itms.optString("idt");
                invNumber = Itms.optString("inum");
                invType = Itms.optString("inv_typ");
                originalDate = Itms.optString("nt_dt");
                originalInvNum = Itms.optString("nt_num");
                String reason = Itms.optString("rsn");
                place = Itms.optString("pos");
                vendorname = Itms.optString("vendorname");
                supplierName = Itms.optString("vendorname");
                for (int k = 0; k < itmsArr.length(); k++) {
                    JSONObject itmsDetail = itmsArr.optJSONObject(k);
                    JSONObject ItmsDetail = itmsDetail.optJSONObject("itm_det");
                    Double cgst = ItmsDetail.optDouble("camt");
                    Double sgst = ItmsDetail.optDouble("samt");
                    Double igst = ItmsDetail.optDouble("iamt");
                    Double csgst = ItmsDetail.optDouble("csamt");
                    cess += csgst;
                    cgstAmt += cgst;
                    sgstAmt += sgst;
                    igstAmt += igst;
                    Double TaxableAmt = ItmsDetail.optDouble("txval");
                    totalTax += (cgst + sgst + igst + csgst);
                    taxableAmtInv += TaxableAmt;
                }
                totalAmt = 0.0;
                totalAmt += (taxableAmtInv + totalTax);

                totalTaxableAmount += taxableAmtInv;
                totalCGST += cgstAmt;
                totalSGST += sgstAmt;
                totalIGST += igstAmt;
                totalCess += cess;
                count++;
                if (!(reqParams.has("reportid") && reqParams.optInt("reportid", 0) == Constants.GSTR3B_Summary_Report)) {
                    JSONObject columnData = new JSONObject();
                    columnData.put(DATAINDEX_TAXABLE_AMOUNT, authHandler.formattedAmount(taxableAmtInv, companyId));
                    columnData.put(DATAINDEX_TOTAL_TAX, authHandler.formattedAmount(totalTax, companyId));
                    columnData.put(DATAINDEX_TOTAL_AMOUNT, authHandler.formattedAmount(totalAmt, companyId));
                    columnData.put(DATAINDEX_DATE, originalDate);
                    columnData.put(DATAINDEX_TRANSACTION_NUMBER, originalInvNum);
                    columnData.put(DATAINDEX_POS, place);
                    columnData.put(DATAINDEX_IGSTAMOUNT, authHandler.formattedAmount(igstAmt, companyId));
                    columnData.put(DATAINDEX_SGSTAMOUNT, authHandler.formattedAmount(sgstAmt, companyId));
                    columnData.put(DATAINDEX_CGSTAMOUNT, authHandler.formattedAmount(cgstAmt, companyId));
                    columnData.put(DATAINDEX_CESS, authHandler.formattedAmount(cess, companyId));
                    columnData.put(DATAINDEX_GSTIN, Gstin);
                    columnData.put(DATAINDEX_PERSONNAME, vendorname);
                    if (reqParams.optBoolean("isDebitNoteTransaction", false)) {
                        columnData.put(DATAINDEX_DOCTYPE, "Debit Note");
                    } else {
                        columnData.put(DATAINDEX_DOCTYPE, "Credit Note");
                    }
                    columnDataArr.put(columnData);
                }
            }
        }
        if (reqParams.has("reportid") && reqParams.optInt("reportid", 0) == Constants.GSTR3B_Summary_Report) {
            jSONObject.put("taxableamt", authHandler.formattedAmount(totalTaxableAmount, companyId));
            jSONObject.put("igst", authHandler.formattedAmount(totalIGST, companyId));
            jSONObject.put("cgst", authHandler.formattedAmount(totalCGST, companyId));
            jSONObject.put("sgst", authHandler.formattedAmount(totalSGST, companyId));
            jSONObject.put("csgst", authHandler.formattedAmount(totalCess, companyId));
            jSONObject.put("totaltax", authHandler.formattedAmount(totalIGST + totalCGST + totalSGST + totalCess, companyId));
            jSONObject.put("totalamount", authHandler.formattedAmount(totalTaxableAmount + totalIGST + totalCGST + totalSGST + totalCess, companyId));
            jSONObject.put("count", count);
        } else {
            jSONObject.put("data", columnDataArr);
        }
        return jSONObject;
    }

    public static void writeDataForGSTR3B(HSSFWorkbook wb, JSONArray b2bArray, String sheetName, String section, JSONObject params) throws JSONException {
        HSSFSheet sheet = wb.createSheet(sheetName);
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, params.optString(Constants.companyKey));
//        DataFormat format = wb.createDataFormat();
//        amountStyle.setDataFormat(format.getFormat("0.00"));
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(HSSFFont.COLOR_NORMAL);
        font.setColor(HSSFColor.BLACK.index);
        rowstyle.setFont(font);
        cellstyle.setFont(font);
        cellstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellstyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        /////////// Header Start ////////////
        cellnum = 0;
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue("Section");
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(section);
        
        cellnum = 0;
        headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue("Entity");
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(params.optString("entity"));
        
        headerRow = sheet.createRow(rownum++);
        headerRow = sheet.createRow(rownum++);
        
        cellnum = 0;
        headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTR3BConstants.HEADER_DOCTYPE);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTR3BConstants.HEADER_DATE);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTR3BConstants.HEADER_PERSONNAME);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTR3BConstants.HEADER_POS);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTR3BConstants.HEADER_TRANSACTION_NUMBER);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTR3BConstants.HEADER_TAXABLE_AMOUNT);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTR3BConstants.HEADER_IGST);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTR3BConstants.HEADER_CGST);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTR3BConstants.HEADER_SGST);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTR3BConstants.HEADER_CESS);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTR3BConstants.HEADER_TOTAL_TAX);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTR3BConstants.HEADER_TOTAL_AMOUNT);
        for (int i = 0; i < b2bArray.length(); i++) {
            JSONObject invDetails = b2bArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString(GSTR3BConstants.DATAINDEX_DOCTYPE));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString(GSTR3BConstants.DATAINDEX_DATE));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString(GSTR3BConstants.DATAINDEX_PERSONNAME));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString(GSTR3BConstants.DATAINDEX_POS));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString(GSTR3BConstants.DATAINDEX_TRANSACTION_NUMBER));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble(GSTR3BConstants.DATAINDEX_TAXABLE_AMOUNT, 0.0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble(GSTR3BConstants.DATAINDEX_IGSTAMOUNT, 0.0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble(GSTR3BConstants.DATAINDEX_CGSTAMOUNT, 0.0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble(GSTR3BConstants.DATAINDEX_SGSTAMOUNT, 0.0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble(GSTR3BConstants.DATAINDEX_CESS, 0.0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble(GSTR3BConstants.DATAINDEX_TOTAL_TAX, 0.0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble(GSTR3BConstants.DATAINDEX_TOTAL_AMOUNT, 0.0));
        }
    }
}
