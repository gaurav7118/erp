/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 */
package com.krawler.spring.compliance.indonesia;

import com.krawler.common.admin.FieldParams;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import static com.krawler.common.util.Constants.Date;
import com.krawler.common.util.IndonesiaConstants;
import com.krawler.common.util.PhilippinesConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.MessageSource;

/**
 *
 * @author Rahul A. Bhawar - Indonesia Compliance
 */
public class AccIndonesiaCompilanceServiceImpl implements AccIndonesiaComplianceService {

    private AccIndonesiaComplianceDAO accIndonesiaComplianceDAO;
    private fieldManagerDAO fieldManagerDAOobj;
    private MessageSource messageSource;

    public void setAccIndonesiaComplianceDAO(AccIndonesiaComplianceDAO accIndonesiaComplianceDAO) {
        this.accIndonesiaComplianceDAO = accIndonesiaComplianceDAO;
    }
    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    /**
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject getVATOutReportData(JSONObject requestParams) throws ServiceException, JSONException {
        JSONObject returnJSONobj = new JSONObject();
        JSONArray pagedJson = new JSONArray();
        String start = requestParams.optString("start");
        String limit = requestParams.optString("limit");
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONArray jarrHeader = new JSONArray();
        /**
         * Get Column details for VAT Out Report
         */
        getColumnModelForVATOutReport(jarrRecords, jarrColumns, jarrHeader, requestParams);
        JSONArray dataJArr = new JSONArray();
        /**
         * Get Data Details for VAT Summary Report
         */
        JSONObject detailsObj = getVATOutReportSectionsData(requestParams);
        dataJArr = detailsObj.optJSONArray("details");
        pagedJson = dataJArr;
        /**
         * Get paging JSON Data array
         */
        if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
            pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
        }
        returnJSONobj.put("totalCount", dataJArr.length());
        returnJSONobj.put("columns", jarrColumns);
        returnJSONobj.put("coldata", pagedJson);
        jMeta.put("totalProperty", "totalCount");
        jMeta.put("root", "coldata");
        jMeta.put("fields", jarrRecords);
        jMeta.put("headers", jarrHeader);
        returnJSONobj.put("metaData", jMeta);
        if (requestParams.optBoolean(IndonesiaConstants.isExportData, false)) {
            List<String> titles = new ArrayList<>(), headers = new ArrayList<>(), align = new ArrayList<>();
            for (int i = 0; i < jarrColumns.length(); i++) {
                JSONObject columnObj = jarrColumns.getJSONObject(i);
                titles.add(StringUtil.serverHTMLStripper(columnObj.optString("header")));
                headers.add(columnObj.optString("dataIndex"));
                align.add(columnObj.optString("align"));
            }
            returnJSONobj.put(IndonesiaConstants.TITLES, StringUtil.join(",", titles));
            returnJSONobj.put(IndonesiaConstants.HEADERS, StringUtil.join(",", headers));
            returnJSONobj.put(IndonesiaConstants.ALIGNMENT, StringUtil.join(",", align));
            JSONArray OtherHeadersArray = new JSONArray();
            JSONObject newHeader = new JSONObject();
            //newHeader.put("header", StringUtil.join(",", IndonesiaConstants.LT_COLUMN.getNames()));
            //OtherHeadersArray.put(newHeader);
            newHeader = new JSONObject();
            newHeader.put("header", StringUtil.join(",", IndonesiaConstants.OF_COLUMN.getNames()));
            OtherHeadersArray.put(newHeader);
            returnJSONobj.put("OtherHeaders", OtherHeadersArray);
            returnJSONobj.put("data", pagedJson);
        }
        return returnJSONobj;
    }

    /**
     *
     * @param jarrRecords
     * @param jarrColumns
     * @param jarrHeader
     * @param requestParams
     * @throws JSONException
     */
    public void getColumnModelForVATOutReport(JSONArray jarrRecords, JSONArray jarrColumns, JSONArray jarrHeader, JSONObject requestParams) throws JSONException {
        JSONObject jobjTemp = new JSONObject();
        List<String> storeRecord = new ArrayList<String>();
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN1.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN2.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN3.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN4.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN5.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN6.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN7.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN8.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN9.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN10.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN11.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN12.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN13.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN14.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN15.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN16.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN17.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN18.name());
        storeRecord.add(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN19.name());
        for (String recordName : storeRecord) {
            jobjTemp = new JSONObject();
            jobjTemp.put("name", recordName);
            jarrRecords.put(jobjTemp);
        }
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.TYPE.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN1.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.KD_TYPE_OF_TRANSACTION.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN2.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.FG_REPLACEMENT.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN3.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.FACTOR_NUMBER.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN4.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.TAX_PERIOD.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN5.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.TAX_YEAR.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN6.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.DATE_OF_INVOICE.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN7.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.NPWP.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN8.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.CUSTOMER_NAME.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN9.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.COMPLETE_ADDRESS.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN10.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.DPP_TOTAL_AMOUNT_BEFORE_TAX.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN11.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.TOTAL_VAT_AMOUNT.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN12.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.TOTAL_PPNBM.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN13.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.ADDITIONAL_INFORMATION_ID.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN14.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.FG_ADVANCE.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN15.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.ADVANCES_DP.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN16.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.ADVANCE_MONEY_VAT.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN17.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.ADVANCES_OF_ADVANCE.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN18.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + IndonesiaConstants.FK_COLUMN.REFERENCE.get() + "<b>");
        jobjTemp.put("dataIndex", IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN19.name());
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
    }

    /**
     *
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject getVATOutReportSectionsData(JSONObject requestParams) throws JSONException, ServiceException {

        JSONObject sectionExtraParams = new JSONObject();
        /**
         * Get Fieldparams details for FG_PENGGANTI
         */
        HashMap<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, "fieldlabel","moduleid"));
        reqParams.put(Constants.filter_values, Arrays.asList(requestParams.optString(Constants.companyKey, ""), IndonesiaConstants.Custom_FG_PENGGANTI,Constants.Acc_Invoice_ModuleId));
        KwlReturnObject result = fieldManagerDAOobj.getFieldParams(reqParams); // get custom field for module
        int FG_PENGGANTI_Colnum =0;
        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty() && result.getEntityList().get(0) != null) {
            FieldParams fp = (FieldParams) result.getEntityList().get(0);
            FG_PENGGANTI_Colnum = fp.getColnum();
        }
        sectionExtraParams.put(IndonesiaConstants.Custom_FG_PENGGANTI,FG_PENGGANTI_Colnum);
        /**
         * Get ALL Sales Invoices data in SQL query
         */
        List<Object> invoiceData = accIndonesiaComplianceDAO.getSalesInvoiceListDataInSQL(requestParams, sectionExtraParams);
        /**
         * Convert Invoice data array to JSON Array
         */
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, invoiceData);
        /**
         * Convert All data in decided format for grid or Export file
         */
        JSONObject dataDetails = getSummaryAndDetailsSalesDocumentData(requestParams, sectionExtraParams, dataJsonArray);
        return dataDetails;
    }

    /**
     *
     * @param requestParams
     * @param sectionExtraParams
     * @param invoiceData
     * @return
     * @throws JSONException
     */
    public JSONArray getSalesDocumentDetailsDataInJSONObject(JSONObject requestParams, JSONObject sectionExtraParams, List<Object> invoiceData) throws JSONException {
        JSONArray returnDataObject = new JSONArray();
        for (Object object : invoiceData) {
            Object[] data = (Object[]) object;
            JSONObject jSONObject = new JSONObject();
            if (data != null) {
                jSONObject.put(IndonesiaConstants.documentID, data.length > 1 && data[0] != null ? data[0].toString() : "");
                jSONObject.put(IndonesiaConstants.documentNumber, data.length > 2 && data[1] != null ? data[1].toString() : "");
                jSONObject.put(IndonesiaConstants.rate, data.length > 2 && data[2] != null ? Double.parseDouble(data[2].toString()) : 0.0);
                jSONObject.put(IndonesiaConstants.quantity, data.length > 3 && data[3] != null ? Double.parseDouble(data[3].toString()) : 0.0);
                jSONObject.put(IndonesiaConstants.currencyrate, data.length > 4 && data[4] != null ? (Double) data[4] : 0.0);
                jSONObject.put(IndonesiaConstants.discountType, data.length > 5 && data[5] != null ? data[5].toString() : "F");
                jSONObject.put(IndonesiaConstants.discountValueInBase, data.length > 6 && data[6] != null ? Double.parseDouble(data[6].toString()) : 0.0);
                jSONObject.put(IndonesiaConstants.rowtaxamount, data.length > 7 && data[7] != null ? Double.parseDouble(data[7].toString()) : 0.0);
                jSONObject.put(IndonesiaConstants.personID, data.length > 8 && data[8] != null ? data[8].toString() : "");
                jSONObject.put(IndonesiaConstants.personName, data.length > 9 && data[9] != null ? data[9].toString() : "");
                jSONObject.put(IndonesiaConstants.documentDetailsID, data.length > 10 && data[10] != null ? data[10].toString() : "");
                jSONObject.put(IndonesiaConstants.isGlobalTax, data.length > 11 && data[11] != null && Integer.valueOf(data[11].toString()) == 1 ? true : false);
                jSONObject.put(IndonesiaConstants.globalTaxInBase, data.length > 12 && data[12] != null ? (Double) data[12] : 0.0);
                jSONObject.put(IndonesiaConstants.documentAmountInBase, data.length > 13 && data[13] != null ? (Double) data[13] : 0d);
                jSONObject.put(IndonesiaConstants.taxType, data.length > 14 && data[14] != null ? (String) data[14] : "");
                jSONObject.put(IndonesiaConstants.isSalesInvoice, data.length > 15 && data[15] != null ? (String) data[15] : "");
                jSONObject.put(IndonesiaConstants.invoiceDateFromJE, data.length > 16 && data[16] != null ? (Date) data[16] : null);
                jSONObject.put(IndonesiaConstants.NPWP_NUMBER, data.length > 17 && data[17] != null ? (String) data[17] : "");
                jSONObject.put(IndonesiaConstants.billingAddress, data.length > 18 && data[18] != null ? (String) data[18] : "");
                jSONObject.put(IndonesiaConstants.productid, data.length > 19 && data[19] != null ? (String) data[19] : "");
                jSONObject.put(IndonesiaConstants.productName, data.length > 20 && data[20] != null ? (String) data[20] : "");
                jSONObject.put(IndonesiaConstants.taxPercent, data.length > 21 && data[21] != null ? (Double) data[21] : 0d);
                jSONObject.put(IndonesiaConstants.billingPostalCode, data.length > 22 && data[22] != null ? (String) data[22] : "");
                jSONObject.put(IndonesiaConstants.billingPhone, data.length > 23 && data[23] != null ? (String) data[23] : "");
                jSONObject.put(IndonesiaConstants.isTaxApplied, data.length > 24 && data[24] != null && (data[24].toString()).equalsIgnoreCase("T") ? true : false);
                jSONObject.put(Constants.additionalMemo, data.length > 25 && data[25] != null ? data[25].toString() : "0");
                jSONObject.put(IndonesiaConstants.REPLACEMENT, data.length > 26 && data[26] != null ? data[26].toString() : "0");
                returnDataObject.put(jSONObject);
            }
        }
        return returnDataObject;
    }

    /**
     *
     * @param requestParams
     * @param sectionExtraParams
     * @param dataJsonArray
     * @return
     * @throws JSONException
     */
    public JSONObject getSummaryAndDetailsSalesDocumentData(JSONObject requestParams, JSONObject sectionExtraParams, JSONArray dataJsonArray) throws JSONException {
        JSONObject jobjData = new JSONObject();
        String companyid = requestParams.optString(Constants.companyKey, "");
        JSONArray detailsJSONArray = new JSONArray();
        /**
         * Iterate All Data By Customer 
         */
        Map<String, JSONArray> customerMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(dataJsonArray, IndonesiaConstants.personID);
        for (String customerKey : customerMap.keySet()) {
            JSONArray customerArr = customerMap.get(customerKey);
            /**
             * Iterate All Data By Document ID
             */
            Map<String, JSONArray> documentMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(customerArr, IndonesiaConstants.documentID);
            for (String documentKey : documentMap.keySet()) {
                JSONArray documentArr = documentMap.get(documentKey);
                /**
                 * Iterate All Data by document detail id
                 */
                Map<String, JSONArray> invoiceDetailMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(documentArr, IndonesiaConstants.documentDetailsID);
                boolean isAddFirstTwoColumn = true;
                //JSONObject documentJSONObj_LT = new JSONObject();
                JSONObject documentJSONObj_FK = new JSONObject();
                JSONArray detailsJSONArray_OF = new JSONArray();
                double totalAmountBeforeTax = 0d;
                for (String invoiceDetailKey : invoiceDetailMap.keySet()) {
                    JSONObject documentJSONObj_OF = new JSONObject();  
                    JSONArray invDetailArr = invoiceDetailMap.get(invoiceDetailKey);
                    JSONObject invDetailobj = invDetailArr.getJSONObject(0);
                    double rate = invDetailobj.optDouble(IndonesiaConstants.rate);
                    double quantity = invDetailobj.optDouble(IndonesiaConstants.quantity);
                    double discount = 0d;
                    String discpercentage = invDetailobj.optString(IndonesiaConstants.discountType);
                    double discountvalue = invDetailobj.optDouble(IndonesiaConstants.discountValueInBase);
                    double rowTaxAmount = invDetailobj.optDouble(IndonesiaConstants.rowtaxamount);
                    double globalTaxTaxAmount = invDetailobj.optDouble(IndonesiaConstants.globalTaxInBase, 0d);
                    double documentAmountInBase = invDetailobj.optDouble(IndonesiaConstants.documentAmountInBase, 0d);
                    String documentNumber = invDetailobj.optString(IndonesiaConstants.documentNumber, ""); // Invoice Number
                    String personName = invDetailobj.optString(IndonesiaConstants.personName, ""); //Customer Name
                    boolean isGlobalTax = invDetailobj.optBoolean(IndonesiaConstants.isGlobalTax, false);
                    boolean isTaxApplied = invDetailobj.optBoolean(IndonesiaConstants.isTaxApplied, false);
                    Date invoiceDateFromJE = (Date) invDetailobj.opt(IndonesiaConstants.invoiceDateFromJE);
                    String productid = invDetailobj.optString(IndonesiaConstants.productid, ""); //Product ID
                    String productName = invDetailobj.optString(IndonesiaConstants.productName, ""); //Product Name
                    double taxPercent = invDetailobj.optDouble(IndonesiaConstants.taxPercent, 0d); // Tax Percentages

                    if (!StringUtil.isNullOrEmpty(discpercentage)) {
                        if (discpercentage.equalsIgnoreCase("T")) {
                            discount = discountvalue * (rate * quantity) / 100;
                        } else {
                            discount = discountvalue;
                        }
                    }
                    /**
                     * Details Reports amounts
                     */
                    double taxableAmount = 0d;
                    taxableAmount = (quantity * rate) - discount;
                    /**
                     * If Global Tax Applicable
                     */
                    if (isGlobalTax && taxPercent != 0) {
                        rowTaxAmount = (taxableAmount * taxPercent) / 100;
                    }
                    /**
                     * This will be total taxable amount only. See following
                     * scenario: 
                     * [1] If tax is on global level, then take total
                     * amount for price*quantity. 
                     * [2] If tax is on line level,
                     * then take amount of price*quantity only for those
                     * products which are taxable. 
                     * 
                     * Note: Amount before tax
                     * should not include invoice term amount.
                     */
                    if(isTaxApplied){
                        totalAmountBeforeTax += taxableAmount;
                    }
                    /**
                     * Put Data Customer And Customer Address details row data 
                     */
                    if (isAddFirstTwoColumn) {
                        isAddFirstTwoColumn = false;
                        String taxType = invDetailobj.optString(IndonesiaConstants.taxType, "");
                        //String isSalesInvoice = invDetailobj.optString(IndonesiaConstants.isSalesInvoice, "");
                        String NPWP_NUMBER = invDetailobj.optString(IndonesiaConstants.NPWP_NUMBER, "");
                        String billingAddress = invDetailobj.optString(IndonesiaConstants.billingAddress, "");
                        String additionalMemo = invDetailobj.optString(Constants.additionalMemo, "0");
                        String replacement = invDetailobj.optString(IndonesiaConstants.REPLACEMENT, "0"); // FG_PENGGANTI
                        //String billingPostalCode = invDetailobj.optString(IndonesiaConstants.billingPostalCode, "");
                        //String billingPhone = invDetailobj.optString(IndonesiaConstants.billingPhone, "");
                        /**
                         * Customer Details ROW - FK
                         */
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN1.name(), IndonesiaConstants.COLUMNTYPE_FK);
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN2.name(), taxType);
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN3.name(), replacement);
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN4.name(), documentNumber);
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN5.name(), IndonesiaConstants.dayFormatter.format(invoiceDateFromJE)); // Day
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN6.name(), IndonesiaConstants.yearFormatter.format(invoiceDateFromJE)); // Year
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN7.name(), IndonesiaConstants.dateFormatter.format(invoiceDateFromJE)); // Invoice Date
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN8.name(), NPWP_NUMBER);
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN9.name(), personName);
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN10.name(), billingAddress);
                        //documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN11.name(), authHandler.round((documentAmountInBase - globalTaxTaxAmount), companyid)); // Amount before tax PPn
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN12.name(), authHandler.round(globalTaxTaxAmount, companyid)); // PPn Tax amount
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN13.name(), authHandler.round(0d, companyid)); // JUMLAH_PPNBM
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN14.name(), additionalMemo); // ID_KETERANGAN_TAMBAHAN - Additional MEMO
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN15.name(), authHandler.round(0d, companyid));
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN16.name(), authHandler.round(0d, companyid));
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN17.name(), authHandler.round(0d, companyid));
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN18.name(), authHandler.round(0d, companyid));
                        documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN19.name(), "No Invoice : " + documentNumber); // Sales Invoice Number: Need to append No Invoice in front
                        //detailsJSONArray.put(documentJSONObj_FK);
                        /**
                         * Customer Address Details ROW - LT
                         */
//                        documentJSONObj_LT.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN1.name(), IndonesiaConstants.COLUMNTYPE_LT);
//                        documentJSONObj_LT.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN2.name(), NPWP_NUMBER);
//                        documentJSONObj_LT.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN3.name(), personName);
//                        documentJSONObj_LT.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN4.name(), billingAddress);
//                        documentJSONObj_LT.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN5.name(), "");
//                        documentJSONObj_LT.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN6.name(), "");
//                        documentJSONObj_LT.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN7.name(), "");
//                        documentJSONObj_LT.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN8.name(), "");
//                        documentJSONObj_LT.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN9.name(), "");
//                        documentJSONObj_LT.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN10.name(), "");
//                        documentJSONObj_LT.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN11.name(), "");
//                        documentJSONObj_LT.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN12.name(), "");
//                        documentJSONObj_LT.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN13.name(), billingPostalCode);
//                        documentJSONObj_LT.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN14.name(), billingPhone);
//                        detailsJSONArray.put(documentJSONObj_LT);
                    }
                    /**
                     * Put document details data after Customer And Customer address details data added in Array
                     * Document Line details ROW - OF
                     */
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN1.name(), IndonesiaConstants.COLUMNTYPE_OF);
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN2.name(), productid);
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN3.name(), productName);
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN4.name(), authHandler.round(rate, companyid));
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN5.name(), authHandler.round(quantity,companyid));
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN6.name(), authHandler.round((rate * quantity),companyid));
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN7.name(), authHandler.round(discount,companyid));
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN8.name(), authHandler.round(taxableAmount,companyid)); // Amount Before Tax
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN9.name(), authHandler.round(rowTaxAmount,companyid)); // VAT Amount
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN10.name(), taxPercent); // VAT rate for Luxury Goods
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN11.name(), authHandler.round(0d,companyid)); // Amount before tax for luxury goods
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN12.name(), authHandler.round(0d,companyid)); // VAT amount for Luxury Goods
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN13.name(), "");
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN14.name(), "");
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN15.name(), "");
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN16.name(), "");
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN17.name(), "");
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN18.name(), "");
                    documentJSONObj_OF.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN19.name(), "");
                    detailsJSONArray_OF.put(documentJSONObj_OF);
                }
                /**
                 * Put All data in Main JSON Array
                 */
                documentJSONObj_FK.put(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN11.name(), authHandler.round((totalAmountBeforeTax), companyid)); // Amount before tax PPn
                detailsJSONArray.put(documentJSONObj_FK);
                StringUtil.concatJSONArray(detailsJSONArray, detailsJSONArray_OF);
            }
        }
        jobjData.put("details", detailsJSONArray);
        return jobjData;
    }
}
