/*                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.compliance.philippines;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.PhilippinesConstants;
import com.krawler.common.util.PhilippinesConstants.VATSummaryReportSections;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.MessageSource;

/**
 *
 * @author krawler
 */
public class AccPhilippinesCompilanceServiceImpl implements AccPhilippinesComplianceService {

    private AccPhilippinesComplianceDAO accPhilippinesComplianceDAO;
    private MessageSource messageSource;

    public void setAccPhilippinesComplianceDAO(AccPhilippinesComplianceDAO accPhilippinesComplianceDAO) {
        this.accPhilippinesComplianceDAO = accPhilippinesComplianceDAO;
    }

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
}

    /**
     * Get Store records and column model details and sections data for VAT
     * Summary Report
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject getVATSummaryReportData(JSONObject requestParams) throws ServiceException, JSONException {
        JSONObject returnJSONobj = new JSONObject();
        JSONArray pagedJson = new JSONArray();
        String start = requestParams.optString("start");
        String limit = requestParams.optString("limit");
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        /**
         * Get Column details for VAT Summary Report
         */
        getColumnModelForVATSummaryReport(jarrRecords, jarrColumns, requestParams);
        JSONArray dataJArr = new JSONArray();
        /**
         * Get Data Details for VAT Summary Report
         */
        getVATSummaryReportSectionsData(dataJArr, requestParams);
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
        returnJSONobj.put("metaData", jMeta);
        return returnJSONobj;
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public JSONObject getVATDetailReportData(JSONObject requestParams) throws ServiceException, JSONException {
        JSONObject returnJSONobj = new JSONObject();
        JSONArray pagedJson = new JSONArray();
        String start = requestParams.optString("start");
        String limit = requestParams.optString("limit");
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        /**
         * Get Column details for VAT Summary Report
         */
        getColumnModelForVATDetailReport(jarrRecords, jarrColumns, requestParams);
        JSONArray dataJArr = new JSONArray();
        /**
         * Get Data Details for VAT Summary Report
         */
        dataJArr = getVATDetailReportSectionsData(requestParams);
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
        returnJSONobj.put("metaData", jMeta);
        return returnJSONobj;
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public JSONObject getVATReportSectionData(JSONObject requestParams) throws ServiceException, JSONException {
        JSONObject returnJSONobj = new JSONObject();
        JSONArray dataArray = new JSONArray();
        String noOtherSection = PhilippinesConstants.VATSummaryReportSections.NO_Others.get();
        String OtherSection = PhilippinesConstants.VATSummaryReportSections.Others.get();
        VATSummaryReportSections[] sectionList = PhilippinesConstants.VATSummaryReportSections.values();
        for (int i = 0; i < sectionList.length; i++) {
            JSONObject sectionJSONobj = new JSONObject();
            if (!(noOtherSection.equalsIgnoreCase(sectionList[i].get()) || OtherSection.equalsIgnoreCase(sectionList[i].get()))) {
                sectionJSONobj.put("sectionName", sectionList[i].get());
                sectionJSONobj.put("sectionId", sectionList[i]);
                dataArray.put(sectionJSONobj);
            }
        }
        returnJSONobj.put("data", dataArray);
        return returnJSONobj;
    }

    /**
     * Get Store records and column model details for VAT Summary Report
     *
     * @param jarrRecords
     * @param jarrColumns
     * @param requestParams
     * @throws JSONException
     */
    public void getColumnModelForVATSummaryReport(JSONArray jarrRecords, JSONArray jarrColumns, JSONObject requestParams) throws JSONException {
        Locale locale = Locale.forLanguageTag(requestParams.optString(Constants.language));
        JSONObject jobjTemp = new JSONObject();
        List<String> storeRecord = new ArrayList<String>();
        storeRecord.add(PhilippinesConstants.particulars);
        storeRecord.add(PhilippinesConstants.taxableAmount);
        storeRecord.add(PhilippinesConstants.taxAmount);
        storeRecord.add(PhilippinesConstants.totalAmount);
        for (String recordName : storeRecord) {
            jobjTemp = new JSONObject();
            jobjTemp.put("name", recordName);
            jarrRecords.put(jobjTemp);
        }
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.grtr1.Particulars", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.particulars);
        jobjTemp.put("width", 450);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "function(value){var res = \"<span class='gridRow'  wtf:qtip='\"+value+\"'><b>\"+value+\"</b></span>\";return res;}");//Added renderer for tooltip
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.vat.summary.report.taxableamount", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.taxableAmount);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.vat.summary.report.taxamount", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.taxAmount);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.vat.summary.report.totalamount", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.totalAmount);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.lp.dishonouredChequeView", null, locale));
        jobjTemp.put("dataIndex", "view");
        jobjTemp.put("width", 100);
        jobjTemp.put("align", "center");
        jobjTemp.put("pdfwidth", 100);
        jobjTemp.put("renderer", "function(value, css, record, row, column, store){\n"
                    + "                    return \"<img id='AcceptImg' class='add'  style='height:18px; width:18px;' src='images/report.gif' title='View Report '></img>\";}");
        jarrColumns.put(jobjTemp);
    }
    /**
     * 
     * @param jarrRecords
     * @param jarrColumns
     * @param requestParams
     * @throws JSONException 
     */
    public void getColumnModelForVATDetailReport(JSONArray jarrRecords, JSONArray jarrColumns, JSONObject requestParams) throws JSONException {
        Locale locale = Locale.forLanguageTag(requestParams.optString(Constants.language));
        JSONObject jobjTemp = new JSONObject();
        List<String> storeRecord = new ArrayList<String>();
        storeRecord.add(PhilippinesConstants.personName);
        storeRecord.add(PhilippinesConstants.documentNumber);
        storeRecord.add(PhilippinesConstants.taxableAmount);
        storeRecord.add(PhilippinesConstants.taxAmount);
        storeRecord.add(PhilippinesConstants.totalAmount);
        for (String recordName : storeRecord) {
            jobjTemp = new JSONObject();
            jobjTemp.put("name", recordName);
            jarrRecords.put(jobjTemp);
        }
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.vat.detail.report.personName", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.personName);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.vat.detail.report.documentNumber", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.documentNumber);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.vat.summary.report.taxableamount", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.taxableAmount);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.vat.summary.report.taxamount", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.taxAmount);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.vat.summary.report.totalamount", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.totalAmount);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);
    }

    /**
     * get VAT Summary Report sections data
     *
     * @param dataJSONArray
     * @param requestParams
     */
    public void getVATSummaryReportSectionsData(JSONArray dataJSONArray, JSONObject requestParams) throws JSONException, ServiceException {

        JSONObject dataJsonObject = new JSONObject();
        /**
         * 12. Vatable Sales/Receipt- Private
         */
        dataJsonObject = getVATSummary_Section_Vatable_Sales_Receipt_Private_Data(requestParams);
        dataJSONArray.put(dataJsonObject);

        /**
         * 13. Sales to Government
         */
        dataJsonObject = new JSONObject();
        dataJsonObject = getVATSummary_Section_Sales_to_Government_Data(requestParams);
        dataJSONArray.put(dataJsonObject);

        /**
         * 14. Zero Rated Sales/Receipts
         */
        dataJsonObject = new JSONObject();
        dataJsonObject = getVATSummary_Section_Zero_Rated_Sales_Receipts_Data(requestParams);
        dataJSONArray.put(dataJsonObject);

        /**
         * 15. Exempt Sales/Receipts
         */
        dataJsonObject = new JSONObject();
        dataJsonObject = getVATSummary_Section_Exempt_Sales_Receipts_Data(requestParams);
        dataJSONArray.put(dataJsonObject);
        
        /**
         * 18 A/B. Purchase of Capital Goods not exceeding P1Million
         */
        dataJsonObject = new JSONObject();
        dataJsonObject = getVATSummary_Section_Purchaseof_Capital_Goodsnot_Exceeding_P1Million_Data(requestParams);
        dataJSONArray.put(dataJsonObject);
        
        /**
         * 18 C/D. Purchase of Capital Goods exceeding P1Million
         */
        dataJsonObject = new JSONObject();
        dataJsonObject = getVATSummary_Section_Purchase_of_Capital_Goods_exceeding_P1Million_Data(requestParams);
        dataJSONArray.put(dataJsonObject);

        /**
         * 18 E/F. Domestic Purchase of Goods other than capital goods
         */
        dataJsonObject = new JSONObject();
        dataJsonObject = getVATSummary_Section_Domestic_PurchaseOf_GoodsOtherThan_capital_Goods_Data(requestParams);
        dataJSONArray.put(dataJsonObject);

        /**
         * 18 G/H. Importation of Goods other than capital goods
         */
        dataJsonObject = new JSONObject();
        dataJsonObject = getVATSummary_Section_ImportationOf_GoodsOtherThan_Capital_Goods_Data(requestParams);
        dataJSONArray.put(dataJsonObject);

        /**
         * 18 I/J. Domestic Purchase of Service Goods other than capital goods
         */
        dataJsonObject = new JSONObject();
        dataJsonObject = getVATSummary_Section_Domestic_PurchaseOf_Service_GoodsOtherThan_Capital_Goods_Data(requestParams);
        dataJSONArray.put(dataJsonObject);

        /**
         * 18 K/L. Service rendered by Non-residents
         */
        dataJsonObject = new JSONObject();
        dataJsonObject = getVATSummary_Section_Service_Renderedby_Non_Residents_Data(requestParams);
        dataJSONArray.put(dataJsonObject);

        /**
         * 18 M. Purchases Not Qualified for Input Tax
         */
        dataJsonObject = new JSONObject();
        dataJsonObject = getVATSummary_Section_PurchasesNot_QualifiedFor_InputTax_Data(requestParams);
        dataJSONArray.put(dataJsonObject);

        dataJsonObject = new JSONObject();
        dataJsonObject.put("particulars", PhilippinesConstants.VATSummaryReportSections.NO_Others.get());
        dataJSONArray.put(dataJsonObject);

        /**
         * 23A Creditable Value-Added Tax Withheld
         */
        dataJsonObject = new JSONObject();
        dataJsonObject = getVATSummary_Section_Creditable_Value_Added_Tax_Withheld_Data(requestParams);
        dataJSONArray.put(dataJsonObject);

        /**
         * 23C VATwithheld on Sales to Government
         */
        dataJsonObject = new JSONObject();
        dataJsonObject = getVATSummary_Section_VATwithheld_On_SalesTo_Government_Data(requestParams);
        dataJSONArray.put(dataJsonObject);

        /**
         * 23E Advance Payments made
         */
        dataJsonObject = new JSONObject();
        dataJsonObject = getVATSummary_Section_Advance_Payments_Made_Data(requestParams);
        dataJSONArray.put(dataJsonObject);

        dataJsonObject = new JSONObject();
        dataJsonObject.put("particulars", PhilippinesConstants.VATSummaryReportSections.Others.get());
        dataJSONArray.put(dataJsonObject);
    }
    /**
     * 
     * @param dataJSONArray
     * @param requestParams
     * @throws JSONException
     * @throws ServiceException 
     */
    public JSONArray getVATDetailReportSectionsData(JSONObject requestParams) throws JSONException, ServiceException {
        String sectionName = requestParams.optString("section", "");
        JSONObject dataJsonObject = new JSONObject();
        JSONArray dataJSONArray =new JSONArray();
        dataJsonObject.put("details",dataJSONArray);
        switch(PhilippinesConstants.VATSummaryReportSections.valueOf(sectionName)){
            case Vatable_Sales_Receipt_Private:
                /**
                 * 12. Vatable Sales/Receipt- Private
                 */
                dataJsonObject = getVATSummary_Section_Vatable_Sales_Receipt_Private_Data(requestParams);
                dataJSONArray = dataJsonObject.optJSONArray("details");
                break;
            case Sales_to_Government:
                /**
                 * 13. Sales to Government
                 */
                dataJsonObject = getVATSummary_Section_Sales_to_Government_Data(requestParams);
                dataJSONArray = dataJsonObject.optJSONArray("details");
                break;
            case Zero_Rated_Sales_Receipts:
                /**
                 * 14. Zero Rated Sales/Receipts
                 */
                dataJsonObject = getVATSummary_Section_Zero_Rated_Sales_Receipts_Data(requestParams);
                dataJSONArray = dataJsonObject.optJSONArray("details");
                break;
            case Exempt_Sales_Receipts:
                /**
                 * 15. Exempt Sales/Receipts
                 */
                dataJsonObject = getVATSummary_Section_Exempt_Sales_Receipts_Data(requestParams);
                dataJSONArray = dataJsonObject.optJSONArray("details");
                break;
            case Purchase_of_Capital_Goods_not_exceeding_P1Million:
                /**
                 * 18 A/B. Purchase of Capital Goods not exceeding P1Million
                 */
                dataJsonObject = getVATSummary_Section_Purchaseof_Capital_Goodsnot_Exceeding_P1Million_Data(requestParams);
                dataJSONArray = dataJsonObject.optJSONArray("details");
                break;
            case Purchase_of_Capital_Goods_exceeding_P1Million:
                /**
                 * 18 C/D. Purchase of Capital Goods exceeding P1Million
                 */
                dataJsonObject = getVATSummary_Section_Purchase_of_Capital_Goods_exceeding_P1Million_Data(requestParams);
                dataJSONArray = dataJsonObject.optJSONArray("details");
                break;
            case Domestic_Purchase_of_Goods_other_than_capital_goods:
                /**
                 * 18 E/F. Domestic Purchase of Goods other than capital goods
                 */
                dataJsonObject = getVATSummary_Section_Domestic_PurchaseOf_GoodsOtherThan_capital_Goods_Data(requestParams);
                dataJSONArray = dataJsonObject.optJSONArray("details");
                break;
            case Importation_of_Goods_other_than_capital_goods:
                /**
                 * 18 G/H. Importation of Goods other than capital goods
                 */
                dataJsonObject = getVATSummary_Section_ImportationOf_GoodsOtherThan_Capital_Goods_Data(requestParams);
                dataJSONArray = dataJsonObject.optJSONArray("details");
                break;
            case Domestic_Purchase_of_Service_Goods_other_than_capital_goods:
                /**
                 * 18 I/J. Domestic Purchase of Service Goods other than capital goods
                 */
                dataJsonObject = getVATSummary_Section_Domestic_PurchaseOf_Service_GoodsOtherThan_Capital_Goods_Data(requestParams);
                dataJSONArray = dataJsonObject.optJSONArray("details");
                break;
            case Service_rendered_by_Non_residents:
                /**
                 * 18 K/L. Service rendered by Non-residents
                 */
                dataJsonObject = getVATSummary_Section_Service_Renderedby_Non_Residents_Data(requestParams);
                dataJSONArray = dataJsonObject.optJSONArray("details");
                break;
            case Purchases_Not_Qualified_for_Input_Tax:
                /**
                 * 18 M. Purchases Not Qualified for Input Tax
                 */
                dataJsonObject = getVATSummary_Section_PurchasesNot_QualifiedFor_InputTax_Data(requestParams);
                dataJSONArray = dataJsonObject.optJSONArray("details");
                break;
            case NO_Others:
                break;
            case Creditable_Value_Added_Tax_Withheld:
                /**
                 * 23A Creditable Value-Added Tax Withheld
                 */
                dataJsonObject = getVATSummary_Section_Creditable_Value_Added_Tax_Withheld_Data(requestParams);
                dataJSONArray = dataJsonObject.optJSONArray("details");
                break;
            case VATwithheld_on_Sales_to_Government:
                /**
                 * 23C VATwithheld on Sales to Government
                 */
                dataJsonObject = getVATSummary_Section_VATwithheld_On_SalesTo_Government_Data(requestParams);
                dataJSONArray = dataJsonObject.optJSONArray("details");
                break;
            case Advance_Payments_made:
                /**
                 * 23E Advance Payments made
                 */
                dataJsonObject = getVATSummary_Section_Advance_Payments_Made_Data(requestParams);
                dataJSONArray = dataJsonObject.optJSONArray("details");
                break;
            case Others:
                break;
        }
        return dataJSONArray;
    }
    /**
     *  code for column model of Sales relief data ERP-41756
     * @param jarrRecords
     * @param jarrColumns
     * @param requestParams
     * @throws JSONException
     * @throws ServiceException 
     */
       public void getColumnModelForSalesReliefData(JSONArray jarrRecords, JSONArray jarrColumns, JSONObject requestParams) throws JSONException, ServiceException {
        Locale locale = Locale.forLanguageTag(requestParams.optString(Constants.language));
        JSONObject jobjTemp = new JSONObject();
        List<String> storeRecord = new ArrayList<String>();
        storeRecord.add(PhilippinesConstants.client_TIN);
        storeRecord.add(PhilippinesConstants.companyName);
        storeRecord.add(PhilippinesConstants.lastName);
        storeRecord.add(PhilippinesConstants.firstName);
        storeRecord.add(PhilippinesConstants.middleName);
        storeRecord.add(PhilippinesConstants.address1);
        storeRecord.add(PhilippinesConstants.address2);
        storeRecord.add(PhilippinesConstants.exempt);
        storeRecord.add(PhilippinesConstants.zeroRated);
        storeRecord.add(PhilippinesConstants.taxableNetofVat);
        storeRecord.add(PhilippinesConstants.vatRate);
        storeRecord.add(PhilippinesConstants.outputVat);
        storeRecord.add(PhilippinesConstants.totalSales);
        storeRecord.add(PhilippinesConstants.grossTaxable);
        
        for (String recordName : storeRecord) {
            jobjTemp = new JSONObject();
            jobjTemp.put("name", recordName);
            jarrRecords.put(jobjTemp);
        }
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.salesReliefsummary.report.client_TIN", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.client_TIN);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.salesReliefsummary.report.companyName", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.companyName);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.salesReliefsummary.report.lastName", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.lastName);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.salesReliefsummary.report.firstName", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.firstName);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.salesReliefsummary.report.middleName", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.middleName);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.salesReliefsummary.report.address1", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.address1);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.salesReliefsummary.report.address2", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.address2);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.salesReliefsummary.report.exempt", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.exempt);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.salesReliefsummary.report.zeroRated", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.zeroRated);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.salesReliefsummary.report.taxableNetofVat", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.taxableNetofVat);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.salesReliefsummary.report.vatRate", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.vatRate);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "right");
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.salesReliefsummary.report.outputVat", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.outputVat);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.salesReliefsummary.report.totalSales", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.totalSales);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.philippines.salesReliefsummary.report.grossTaxable", null, locale) + "<b>");
        jobjTemp.put("dataIndex", PhilippinesConstants.grossTaxable);
        jobjTemp.put("width", 200);
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(jobjTemp);
    }
        /**
     *
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject getVATSummary_Section_Vatable_Sales_Receipt_Private_Data(JSONObject requestParams) throws JSONException, ServiceException {
        JSONObject sectionExtraParams = new JSONObject();
        sectionExtraParams.put("CustomerType", PhilippinesConstants.CUSTOMER_VENDOR_TYPE_Normal);
        /**
         * Get Sales Invoices data for Customer Type Normal
         */
        List<Object> invoiceData = accPhilippinesComplianceDAO.getSalesInvoiceListDataInSQL(requestParams, sectionExtraParams);
        /**
         * Get Advanced payment data for Customer type Normal
         */
        List<Object> advanceReciptData = accPhilippinesComplianceDAO.getReceivePaymentAdvanceListDataInSql(requestParams, sectionExtraParams);
        invoiceData.addAll(advanceReciptData);
        /**
         * Get Invoice payment data for Customer type Normal
         */
        List<Object> invoiceReciptData = accPhilippinesComplianceDAO.getReceivePaymentInvoiceListDataInSql(requestParams, sectionExtraParams);
        invoiceData.addAll(invoiceReciptData);
        /**
         * Get OtherWise payment data for Customer type Normal
         */
        List<Object> OtherWiseReciptData = accPhilippinesComplianceDAO.getReceivePaymentOtherWiseListDataInSql(requestParams, sectionExtraParams);
        invoiceData.addAll(OtherWiseReciptData);
        /**
         * Convert Data array to JSON Array
         */
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, invoiceData);
        /**
         * Get particular Section Summary and Details Data
         */
        JSONObject dataDetails = getSummaryAndDetailsSalesDocumentData(requestParams, sectionExtraParams, dataJsonArray);
        dataDetails.put("particulars", PhilippinesConstants.VATSummaryReportSections.Vatable_Sales_Receipt_Private.get());
        return dataDetails;
    }

    /**
     *
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject getVATSummary_Section_Sales_to_Government_Data(JSONObject requestParams) throws JSONException, ServiceException {
        JSONObject sectionExtraParams = new JSONObject();
        sectionExtraParams.put("CustomerType", PhilippinesConstants.CUSTOMER_VENDOR_TYPE_Government);
        /**
         * Get Sales Invoices data for Customer Type Government
         */
        List<Object> invoiceData = accPhilippinesComplianceDAO.getSalesInvoiceListDataInSQL(requestParams, sectionExtraParams);
        /**
         * Get Advanced payment data for Customer type Government
         */
        List<Object> advanceReciptData = accPhilippinesComplianceDAO.getReceivePaymentAdvanceListDataInSql(requestParams, sectionExtraParams);
        invoiceData.addAll(advanceReciptData);
        /**
         * Get Advanced payment data for Customer type Government
         */
        List<Object> invoiceReciptData = accPhilippinesComplianceDAO.getReceivePaymentInvoiceListDataInSql(requestParams, sectionExtraParams);
        invoiceData.addAll(invoiceReciptData);
        /**
         * Get OtherWise payment data for Customer type Government
         */
        List<Object> OtherWiseReciptData = accPhilippinesComplianceDAO.getReceivePaymentOtherWiseListDataInSql(requestParams, sectionExtraParams);
        invoiceData.addAll(OtherWiseReciptData);
        /**
         * Convert Data array to JSON Array
         */
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, invoiceData);
        /**
         * Get particular Section Summary and Details Data
         */
        JSONObject dataDetails = getSummaryAndDetailsSalesDocumentData(requestParams, sectionExtraParams, dataJsonArray);
        dataDetails.put("particulars", PhilippinesConstants.VATSummaryReportSections.Sales_to_Government.get());
        return dataDetails;
    }

    /**
     *
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject getVATSummary_Section_Advance_Payments_Made_Data(JSONObject requestParams) throws JSONException, ServiceException {
        JSONObject sectionExtraParams = new JSONObject();
        /**
         * Get Advance Payment Received Data
         */
        List<Object> advanceReciptData = accPhilippinesComplianceDAO.getReceivePaymentAdvanceListDataInSql(requestParams, sectionExtraParams);
        /**
         * Convert Data array to JSON Array
         */
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, advanceReciptData);
        /**
         * Get particular Section Summary and Details Data
         */
        JSONObject dataDetails = getSummaryAndDetailsSalesDocumentData(requestParams, sectionExtraParams, dataJsonArray);
        dataDetails.put("particulars", PhilippinesConstants.VATSummaryReportSections.Advance_Payments_made.get());
        return dataDetails;
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    public JSONObject getVATSummary_Section_Purchaseof_Capital_Goodsnot_Exceeding_P1Million_Data(JSONObject requestParams) throws JSONException, ServiceException {
        JSONObject sectionExtraParams = new JSONObject();
        sectionExtraParams.put(PhilippinesConstants.isLowerLimitAmount, true);
        sectionExtraParams.put(PhilippinesConstants.limitAmountValue, PhilippinesConstants.VATReport_LimitAmount);
        sectionExtraParams.put(PhilippinesConstants.isAssetDocumentType, true);
        /**
         * Get Asset Invoices data for Total amount not exceeding 1Million
         */
        List<Object> invoiceData = accPhilippinesComplianceDAO.getPurchaseInvoiceListDataInSQL(requestParams, sectionExtraParams);
        /**
         * Convert Data array to JSON Array
         */
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, invoiceData);
        /**
         * Get particular Section Summary and Details Data
         */
        JSONObject dataDetails = getSummaryAndDetailsSalesDocumentData(requestParams, sectionExtraParams, dataJsonArray);
        dataDetails.put("particulars", PhilippinesConstants.VATSummaryReportSections.Purchase_of_Capital_Goods_not_exceeding_P1Million.get());
        return dataDetails;
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    public JSONObject getVATSummary_Section_Purchase_of_Capital_Goods_exceeding_P1Million_Data(JSONObject requestParams) throws JSONException, ServiceException {
        JSONObject sectionExtraParams = new JSONObject();
        sectionExtraParams.put(PhilippinesConstants.isGreaterLimitAmount, true);
        sectionExtraParams.put(PhilippinesConstants.limitAmountValue, PhilippinesConstants.VATReport_LimitAmount);
        sectionExtraParams.put(PhilippinesConstants.isAssetDocumentType, true);
        /**
         * Get Asset Invoices data for Total amount exceeding 1Million
         */
        List<Object> invoiceData = accPhilippinesComplianceDAO.getPurchaseInvoiceListDataInSQL(requestParams, sectionExtraParams);
        /**
         * Convert Data array to JSON Array
         */
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, invoiceData);
        /**
         * Get particular Section Summary and Details Data
         */
        JSONObject dataDetails = getSummaryAndDetailsSalesDocumentData(requestParams, sectionExtraParams, dataJsonArray);
        dataDetails.put("particulars", PhilippinesConstants.VATSummaryReportSections.Purchase_of_Capital_Goods_exceeding_P1Million.get());
        return dataDetails;
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    public JSONObject getVATSummary_Section_Zero_Rated_Sales_Receipts_Data(JSONObject requestParams) throws JSONException, ServiceException {
        JSONObject sectionExtraParams = new JSONObject();
        sectionExtraParams.put("taxType", PhilippinesConstants.TAX_Zero_Rated);
        //sectionExtraParams.put("CustomerType", PhilippinesConstants.CUSTOMER_VENDOR_TYPE_ZeroRated);
         /**
         * Get Sales Invoices data for Tax Zero Rated
         */
        List<Object> invoiceData = accPhilippinesComplianceDAO.getSalesInvoiceListDataInSQL(requestParams, sectionExtraParams);
        /**
         * Get OtherWise payment data for Tax Zero Rated
         */
        List<Object> OtherWiseReciptData = accPhilippinesComplianceDAO.getReceivePaymentOtherWiseListDataInSql(requestParams, sectionExtraParams);
        invoiceData.addAll(OtherWiseReciptData);
        /**
         * Convert Data array to JSON Array
         */
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, invoiceData);
        /**
         * Get particular Section Summary and Details Data
         */
        JSONObject dataDetails = getSummaryAndDetailsSalesDocumentData(requestParams, sectionExtraParams, dataJsonArray);
        dataDetails.put("particulars", PhilippinesConstants.VATSummaryReportSections.Zero_Rated_Sales_Receipts.get());
        return dataDetails;
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    public JSONObject getVATSummary_Section_Exempt_Sales_Receipts_Data(JSONObject requestParams) throws JSONException, ServiceException {
        JSONObject sectionExtraParams = new JSONObject();
        sectionExtraParams.put("taxType", PhilippinesConstants.TAX_Exempt);
         /**
         * Get Sales Invoices data for Tax Zero Rated
         */
        List<Object> invoiceData = accPhilippinesComplianceDAO.getSalesInvoiceListDataInSQL(requestParams, sectionExtraParams);
        /**
         * Get OtherWise payment data for Tax Zero Rated
         */
        List<Object> OtherWiseReciptData = accPhilippinesComplianceDAO.getReceivePaymentOtherWiseListDataInSql(requestParams, sectionExtraParams);
        invoiceData.addAll(OtherWiseReciptData);
        /**
         * Convert Data array to JSON Array
         */
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, invoiceData);
        /**
         * Get particular Section Summary and Details Data
         */
        JSONObject dataDetails = getSummaryAndDetailsSalesDocumentData(requestParams, sectionExtraParams, dataJsonArray);
        dataDetails.put("particulars", PhilippinesConstants.VATSummaryReportSections.Exempt_Sales_Receipts.get());
        return dataDetails;
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    public JSONObject getVATSummary_Section_Domestic_PurchaseOf_GoodsOtherThan_capital_Goods_Data(JSONObject requestParams) throws JSONException, ServiceException {
        JSONObject sectionExtraParams = new JSONObject();
        /**
         * Modules       : (Purchase Invoice) 
         * Tax           :  Other than “Import” Tax 
         * Customer Type : ALL 
         * Product Type  : Other than “Services type” product   
         */
        sectionExtraParams.put("taxType", PhilippinesConstants.TAX_Exempt + "," + PhilippinesConstants.TAX_Input_VAT + "," + PhilippinesConstants.TAX_Output_VAT + "," + PhilippinesConstants.TAX_Zero_Rated);
        sectionExtraParams.put("productType", Constants.ASSEMBLY + "," + Constants.INVENTORY_PART + "," + Constants.NON_INVENTORY_PART + "," + Constants.Inventory_Non_Sales);
        List<Object> invoiceData = accPhilippinesComplianceDAO.getPurchaseInvoiceListDataInSQL(requestParams, sectionExtraParams);
        /**
         * Convert Data array to JSON Array
         */
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, invoiceData);
        /**
         * Get particular Section Summary and Details Data
         */
        JSONObject dataDetails = getSummaryAndDetailsSalesDocumentData(requestParams, sectionExtraParams, dataJsonArray);
        dataDetails.put("particulars", PhilippinesConstants.VATSummaryReportSections.Domestic_Purchase_of_Goods_other_than_capital_goods.get());
        return dataDetails;
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    public JSONObject getVATSummary_Section_ImportationOf_GoodsOtherThan_Capital_Goods_Data(JSONObject requestParams) throws JSONException, ServiceException {
        JSONObject sectionExtraParams = new JSONObject();
        /**
         * Modules       : (Purchase Invoice) 
         * Tax           : “Import” Tax 
         * Customer Type : ALL 
         * Product Type  : Other than “Services type” product   
         */
        sectionExtraParams.put("taxType", PhilippinesConstants.TAX_Import);
        sectionExtraParams.put("productType", Constants.ASSEMBLY + "," + Constants.INVENTORY_PART + "," + Constants.NON_INVENTORY_PART + "," + Constants.Inventory_Non_Sales);
        List<Object> invoiceData = accPhilippinesComplianceDAO.getPurchaseInvoiceListDataInSQL(requestParams, sectionExtraParams);
        /**
         * Convert Data array to JSON Array
         */
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, invoiceData);
        /**
         * Get particular Section Summary and Details Data
         */
        JSONObject dataDetails = getSummaryAndDetailsSalesDocumentData(requestParams, sectionExtraParams, dataJsonArray);
        dataDetails.put("particulars", PhilippinesConstants.VATSummaryReportSections.Importation_of_Goods_other_than_capital_goods.get());
        return dataDetails;
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    public JSONObject getVATSummary_Section_Domestic_PurchaseOf_Service_GoodsOtherThan_Capital_Goods_Data(JSONObject requestParams) throws JSONException, ServiceException {
        JSONObject sectionExtraParams = new JSONObject();
        /**
         * Modules       : (Purchase Invoice) 
         * Tax           :  Other than “Import” Tax 
         * Customer Type : ALL 
         * Product Type  : Services type product only  
         */
        sectionExtraParams.put("taxType", PhilippinesConstants.TAX_Exempt + "," + PhilippinesConstants.TAX_Input_VAT + "," + PhilippinesConstants.TAX_Output_VAT + "," + PhilippinesConstants.TAX_Zero_Rated);
        sectionExtraParams.put("productType", Constants.SERVICE);
        List<Object> invoiceData = accPhilippinesComplianceDAO.getPurchaseInvoiceListDataInSQL(requestParams, sectionExtraParams);
        /**
         * Convert Data array to JSON Array
         */
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, invoiceData);
        /**
         * Get particular Section Summary and Details Data
         */
        JSONObject dataDetails = getSummaryAndDetailsSalesDocumentData(requestParams, sectionExtraParams, dataJsonArray);
        dataDetails.put("particulars", PhilippinesConstants.VATSummaryReportSections.Domestic_Purchase_of_Service_Goods_other_than_capital_goods.get());
        return dataDetails;
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    public JSONObject getVATSummary_Section_Service_Renderedby_Non_Residents_Data(JSONObject requestParams) throws JSONException, ServiceException {
        JSONObject sectionExtraParams = new JSONObject();
        /**
         * Modules       : (Purchase Invoice) 
         * Tax           : “Import” Tax 
         * Customer Type : ALL 
         * Product Type  : Services type product only  
         */
        sectionExtraParams.put("taxType", PhilippinesConstants.TAX_Import);
        sectionExtraParams.put("productType", Constants.SERVICE);
        List<Object> invoiceData = accPhilippinesComplianceDAO.getPurchaseInvoiceListDataInSQL(requestParams, sectionExtraParams);
        /**
         * Convert Data array to JSON Array
         */
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, invoiceData);
        /**
         * Get particular Section Summary and Details Data
         */
        JSONObject dataDetails = getSummaryAndDetailsSalesDocumentData(requestParams, sectionExtraParams, dataJsonArray);
        dataDetails.put("particulars", PhilippinesConstants.VATSummaryReportSections.Service_rendered_by_Non_residents.get());
        return dataDetails;
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    public JSONObject getVATSummary_Section_PurchasesNot_QualifiedFor_InputTax_Data(JSONObject requestParams) throws JSONException, ServiceException {
        JSONObject sectionExtraParams = new JSONObject();
        List<Object> invoiceData = new ArrayList<>();
        /**
         * Convert Data array to JSON Array
         */
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, invoiceData);
        /**
         * Get particular Section Summary and Details Data
         */
        JSONObject dataDetails = getSummaryAndDetailsSalesDocumentData(requestParams, sectionExtraParams, dataJsonArray);
        dataDetails.put("particulars", PhilippinesConstants.VATSummaryReportSections.Purchases_Not_Qualified_for_Input_Tax.get());
        return dataDetails;
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    public JSONObject getVATSummary_Section_Creditable_Value_Added_Tax_Withheld_Data(JSONObject requestParams) throws JSONException, ServiceException {
        JSONObject sectionExtraParams = new JSONObject();
        List<Object> invoiceData = new ArrayList<>();
        /**
         * Convert Data array to JSON Array
         */
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, invoiceData);
        /**
         * Get particular Section Summary and Details Data
         */
        JSONObject dataDetails = getSummaryAndDetailsSalesDocumentData(requestParams, sectionExtraParams, dataJsonArray);
        dataDetails.put("particulars", PhilippinesConstants.VATSummaryReportSections.Creditable_Value_Added_Tax_Withheld.get());
        return dataDetails;
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    public JSONObject getVATSummary_Section_VATwithheld_On_SalesTo_Government_Data(JSONObject requestParams) throws JSONException, ServiceException {
        JSONObject sectionExtraParams = new JSONObject();
        List<Object> invoiceData = new ArrayList<>();
        /**
         * Convert Data array to JSON Array
         */
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, invoiceData);
        /**
         * Get particular Section Summary and Details Data
         */
        JSONObject dataDetails = getSummaryAndDetailsSalesDocumentData(requestParams, sectionExtraParams, dataJsonArray);
        dataDetails.put("particulars", PhilippinesConstants.VATSummaryReportSections.VATwithheld_on_Sales_to_Government.get());
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
                jSONObject.put(PhilippinesConstants.documentID, data.length > 1 && data[0] != null ? data[0].toString() : "");
                jSONObject.put(PhilippinesConstants.documentNumber, data.length > 2 && data[1] != null ? data[1].toString() : "");
                jSONObject.put(PhilippinesConstants.rate, data.length > 2 && data[2] != null ? Double.parseDouble(data[2].toString()) : 0.0);
                jSONObject.put(PhilippinesConstants.quantity, data.length > 3 && data[3] != null ? Double.parseDouble(data[3].toString()) : 0.0);
                jSONObject.put(PhilippinesConstants.currencyrate, data.length > 4 && data[4] != null ? (Double) data[4] : 0.0);
                jSONObject.put(PhilippinesConstants.discountType, data.length > 5 && data[5] != null ? data[5].toString() : "F");
                jSONObject.put(PhilippinesConstants.discountValueInBase, data.length > 6 && data[6] != null ? Double.parseDouble(data[6].toString()) : 0.0);
                jSONObject.put(PhilippinesConstants.rowtaxamount, data.length > 7 && data[7] != null ? Double.parseDouble(data[7].toString()) : 0.0);
                jSONObject.put(PhilippinesConstants.personID, data.length > 8 && data[8] != null ? data[8].toString() : "");
                jSONObject.put(PhilippinesConstants.personName, data.length > 9 && data[9] != null ? data[9].toString() : "");
                jSONObject.put(PhilippinesConstants.documentDetailsID, data.length > 10 && data[10] != null ? data[10].toString() : "");
                jSONObject.put(PhilippinesConstants.isGlobalTax, data.length > 11 && data[11] != null && Integer.valueOf(data[11].toString()) == 1 ? true : false);
                jSONObject.put(PhilippinesConstants.globalTaxInBase, data.length > 12 && data[12] != null ? (Double) data[12] : 0.0);
                jSONObject.put(PhilippinesConstants.isPayment, data.length > 13 && data[13] != null ? (String) data[13] : "F");
                jSONObject.put(PhilippinesConstants.detailAmount, data.length > 14 && data[14] != null ? (Double) data[14] : 0d);
                jSONObject.put(PhilippinesConstants.address1, data.length > 15 && data[15] != null ? data[15].toString() : "");
                jSONObject.put(PhilippinesConstants.client_TIN, data.length > 16 && data[16] != null ? data[16].toString() : "");
                jSONObject.put(PhilippinesConstants.taxId, data.length > 17 && data[17] != null ? data[16].toString() : "");
                jSONObject.put(PhilippinesConstants.companyName, data.length > 18 && data[18] != null ? data[18].toString() : "");
                jSONObject.put(PhilippinesConstants.defaultTaxId, data.length > 19 && data[19] != null ? data[19].toString() : "");
                jSONObject.put(PhilippinesConstants.vatRate, data.length > 20 && data[20] != null ? data[20].toString() : "");
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
        JSONArray detailsJSONArray = new JSONArray();
        Map<String, JSONArray> documentMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(dataJsonArray, PhilippinesConstants.documentID);
        double totalInvoiceAmountSummary = 0d;
        double totalTaxableAmountSummary = 0d;
        double totalTaxAmountSummary = 0d;
        for (String documentKey : documentMap.keySet()) {
            JSONObject documentJSONObj = new JSONObject();
            JSONArray documentArr = documentMap.get(documentKey);
            Map<String, JSONArray> invoiceDetailMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(documentArr, PhilippinesConstants.documentDetailsID);
            double totalInvoiceAmount = 0d;
            double totalTaxableAmount = 0d;
            double totalTaxAmount = 0d;
            double globalTaxTaxAmount = 0d;
            boolean isGlobalTax = false;            
            for (String invoiceDetailKey : invoiceDetailMap.keySet()) {
                JSONArray invDetailArr = invoiceDetailMap.get(invoiceDetailKey);
                JSONObject invDetailobj = invDetailArr.getJSONObject(0);
                double rate = invDetailobj.optDouble(PhilippinesConstants.rate);
                double quantity = invDetailobj.optDouble(PhilippinesConstants.quantity);
                double discount = 0d;
                String discpercentage = invDetailobj.optString(PhilippinesConstants.discountType);
                double discountvalue = invDetailobj.optDouble(PhilippinesConstants.discountValueInBase);
                double rowTaxAmount = invDetailobj.optDouble(PhilippinesConstants.rowtaxamount);
                isGlobalTax = invDetailobj.optBoolean(PhilippinesConstants.isGlobalTax);
                globalTaxTaxAmount = invDetailobj.optDouble(PhilippinesConstants.globalTaxInBase, 0d);
                double detailAmount = invDetailobj.optDouble(PhilippinesConstants.detailAmount, 0d);
                String isPayment = invDetailobj.optString(PhilippinesConstants.isPayment, "F");
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
                if (!isPayment.equalsIgnoreCase("T")) {
                    taxableAmount = (quantity * rate) - discount;
                } else {
                    taxableAmount = detailAmount;
                }
                totalTaxAmount += rowTaxAmount;
                totalInvoiceAmount += taxableAmount + rowTaxAmount;
                totalTaxableAmount += taxableAmount;
                documentJSONObj.put(PhilippinesConstants.documentNumber, invDetailobj.optString(PhilippinesConstants.documentNumber,""));
                documentJSONObj.put(PhilippinesConstants.personName, invDetailobj.optString(PhilippinesConstants.personName,""));
                /**
                 * Summary Amount Details
                 */
                totalTaxAmountSummary += rowTaxAmount;
                totalInvoiceAmountSummary += taxableAmount + rowTaxAmount;
                totalTaxableAmountSummary += taxableAmount;
            }
            if (isGlobalTax) {
                totalTaxAmount += globalTaxTaxAmount;
                totalTaxAmountSummary += globalTaxTaxAmount;
                totalInvoiceAmount += globalTaxTaxAmount;
                totalInvoiceAmountSummary += globalTaxTaxAmount;
            }
            documentJSONObj.put(PhilippinesConstants.taxableAmount, totalTaxableAmount);
            documentJSONObj.put(PhilippinesConstants.taxAmount, totalTaxAmount);
            documentJSONObj.put(PhilippinesConstants.totalAmount, totalInvoiceAmount);
            detailsJSONArray.put(documentJSONObj);
        }
        jobjData.put("details", detailsJSONArray);
        jobjData.put(PhilippinesConstants.taxableAmount, totalTaxableAmountSummary);
        jobjData.put(PhilippinesConstants.taxAmount, totalTaxAmountSummary);
        jobjData.put(PhilippinesConstants.totalAmount, totalInvoiceAmountSummary);
        return jobjData;
    }
    /**
     * @param params
     * @return
     * @throws ServiceException
     * @throws JSONException This function return final json object that will
     * dispalyed in UI
     */
    public JSONObject getPurchaseReliefReportSummary(JSONObject params) throws ServiceException, JSONException {
        JSONArray jarrRecords = new JSONArray();
        JSONArray jarrColumns = new JSONArray();
        JSONObject returnObj = new JSONObject();
        JSONObject colData = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONArray pagedJson = new JSONArray();
        String start = params.optString("start");
        String limit = params.optString("limit");
        String companyId = params.optString("companyid");
        /**
         * Get column model for report.
         */
        getColumnModelForPurchaseReliefReportSummary(jarrRecords, jarrColumns, params);
        /**
         * Get data for purchase relief.
         */
        colData = getPurchaseReliefData(params);
        JSONArray dataJArr = colData.optJSONArray("data");
        pagedJson=dataJArr;
        /**
         * Get paging JSON Data array
         */
        if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
            pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
        }
        returnObj.put("totalCount", dataJArr.length());
        returnObj.put("coldata", pagedJson);
        returnObj.put("columns", jarrColumns);
        jMeta.put("totalProperty", "totalCount");
        jMeta.put("root", "coldata");
        jMeta.put("fields", jarrRecords);
        returnObj.put("metaData", jMeta);
        /**
         * if export of purchases relief report.
         */
        if(params.optBoolean("exportData",false)){
            List<String> titles =new ArrayList<>(),headers=new ArrayList<>(),align=new ArrayList<>();
            for(int i=0;i<jarrColumns.length();i++){
                JSONObject columnObj = jarrColumns.getJSONObject(i);
                titles.add(StringUtil.serverHTMLStripper(columnObj.optString("header")));
                headers.add(columnObj.optString("dataindex"));
                align.add(columnObj.optString("align"));
            }
            returnObj.put(PhilippinesConstants.TITLES,StringUtil.join(",", titles));
            returnObj.put(PhilippinesConstants.HEADERS,StringUtil.join(",", headers));
            returnObj.put(PhilippinesConstants.ALIGNMENT,StringUtil.join(",", align));
            returnObj.put("data",pagedJson);
        }
        return returnObj;
    }
    /**
     * This function prepare column data for grid
     *
     * @param params
     * @return
     * @throws JSONException
     * @throws ServiceException      *
     */
    public JSONObject getPurchaseReliefData(JSONObject params) throws JSONException, ServiceException {
        String msg = "";
        JSONObject sectionExtraParams=new JSONObject();
        JSONObject returnObject = new JSONObject();
        JSONArray vArr = new JSONArray();
        List<Object> list = accPhilippinesComplianceDAO.getPurchaseInvoiceListDataInSQL(params,sectionExtraParams);
        /**
         * Create JSON based on selected list of columns.
         */
        JSONArray bulkData = createJsonForDataFetchedFromDB(list);
        /**
         * Process data as per requirement.
         */
        vArr = getVendorWisePurchaseReliefData(bulkData);
        returnObject.put("data", vArr);
        return returnObject;
    }
    /**
     * This function provide vendor wise data
     *
     * @param array
     * @param dataList
     */
    public JSONArray getVendorWisePurchaseReliefData(JSONArray bulkData) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject vendorObj = null;
        Map<String, JSONArray> vendorData = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, PhilippinesConstants.personID);
        for (String venKey : vendorData.keySet()) {
            /**
             * Iterate vendor wise data.
             */
            vendorObj = new JSONObject();
            JSONArray venArray = vendorData.get(venKey);
            double totalInvoiceAmountSummary = 0d;
            double totalTaxableAmountSummary = 0d;
            double totalTaxAmountSummary = 0d;
            double capitalGoodsSummary = 0d;
            boolean isAssetPurchaseInvoice = false;
            Map<String, JSONArray> documentMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(venArray, PhilippinesConstants.documentID);
            for (String documentKey : documentMap.keySet()) {
                /**
                 * Iterate Invoice wise data
                 */
                JSONArray documentsArr = documentMap.get(documentKey);
                Map<String, JSONArray> invoiceDetailMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(documentsArr, PhilippinesConstants.documentDetailsID);
                double totalInvoiceAmount = 0d;
                double totalTaxableAmount = 0d;
                double totalTaxAmount = 0d;
                double globalTaxTaxAmount = 0d;
                double totalCapitalGoodsAmount = 0d; // to calculate amount of asset purchase invoice.
                boolean isGlobalTax = false;
                isAssetPurchaseInvoice = documentsArr.getJSONObject(0).optBoolean(PhilippinesConstants.isAssetPurchaseInvoice);
                for (String invoiceDetailKey : invoiceDetailMap.keySet()) {
                    /**
                     * Iterate Invoice Detail data
                     */
                    JSONArray invDetailArr = invoiceDetailMap.get(invoiceDetailKey);
                    JSONObject invDetailobj = invDetailArr.getJSONObject(0);
                    double rate = invDetailobj.optDouble(PhilippinesConstants.rate);
                    double quantity = invDetailobj.optDouble(PhilippinesConstants.quantity);
                    double discount = 0d;
                    String discpercentage = invDetailobj.optString(PhilippinesConstants.discountType);
                    double discountvalue = invDetailobj.optDouble(PhilippinesConstants.discountValueInBase);
                    double rowTaxAmount = invDetailobj.optDouble(PhilippinesConstants.rowtaxamount);
                    isGlobalTax = invDetailobj.optBoolean(PhilippinesConstants.isGlobalTax);
                    globalTaxTaxAmount = invDetailobj.optDouble(PhilippinesConstants.globalTaxInBase, 0d);
                    double unitPrice = rate * quantity;
                    double taxableAmount = 0d;
                    double taxAmount = 0d;

                    if (!StringUtil.isNullOrEmpty(discpercentage)) {
                        if (discpercentage.equalsIgnoreCase("T")) {
                            discount = discountvalue * (rate * quantity) / 100;
                        } else {
                            discount = discountvalue;
                        }
                    }
                    taxableAmount = unitPrice - discount;
                    totalInvoiceAmount += taxableAmount + rowTaxAmount; //total invoice amount
                    totalTaxAmount += rowTaxAmount; //total invoice tax amount
                    totalTaxableAmount += taxableAmount; // total invoice taxable amount
                    if (isAssetPurchaseInvoice) {
                        totalCapitalGoodsAmount += totalTaxableAmount;// IF asset purchase invoice
                    }
                }
                if (isGlobalTax) {
                    totalTaxAmount += globalTaxTaxAmount;
                    totalInvoiceAmount += globalTaxTaxAmount;
                }
                totalTaxableAmountSummary += totalTaxableAmount;
                totalInvoiceAmountSummary += totalInvoiceAmount;
                totalTaxAmountSummary += totalTaxAmount;
                capitalGoodsSummary += totalCapitalGoodsAmount;
            }
            JSONObject temp = venArray.getJSONObject(0);
            vendorObj.put(PhilippinesConstants.vendorTIN, temp.optString(PhilippinesConstants.vendorTIN));
            vendorObj.put(PhilippinesConstants.companyName, temp.optString(PhilippinesConstants.companyName));
            vendorObj.put(PhilippinesConstants.lastName, temp.optString(PhilippinesConstants.personName));
            vendorObj.put(PhilippinesConstants.firstName, "");
            vendorObj.put(PhilippinesConstants.middleName, "");
            vendorObj.put(PhilippinesConstants.address1, temp.optString(PhilippinesConstants.address1));
            vendorObj.put(PhilippinesConstants.address2, temp.optString(PhilippinesConstants.address2));
            vendorObj.put(PhilippinesConstants.taxablenetofvat, totalTaxableAmountSummary);
            vendorObj.put(PhilippinesConstants.totalpurchases, totalInvoiceAmountSummary);
            vendorObj.put(PhilippinesConstants.capitalGoods, capitalGoodsSummary);
            vendorObj.put(PhilippinesConstants.inputVAT, totalTaxAmountSummary);
            jsonArray.put(vendorObj);
        }
        return jsonArray;
    }

    /**
     * This function returns JsonArray for data fetched by DB
     *
     * @param dataList
     * @return
     */
    public JSONArray createJsonForDataFetchedFromDB(List<Object> dataList) throws JSONException {
        JSONArray returnDataObject = new JSONArray();

        for (Object object : dataList) {
            Object[] data = (Object[]) object;
            JSONObject jSONObject = new JSONObject();
            if (data != null) {
                jSONObject.put(PhilippinesConstants.documentID, data.length > 1 && data[0] != null ? data[0].toString() : "");
                jSONObject.put(PhilippinesConstants.documentNumber, data.length > 2 && data[1] != null ? data[1].toString() : "");
                jSONObject.put(PhilippinesConstants.rate, data.length > 2 && data[2] != null ? Double.parseDouble(data[2].toString()) : 0.0);
                jSONObject.put(PhilippinesConstants.quantity, data.length > 3 && data[3] != null ? Double.parseDouble(data[3].toString()) : 0.0);
                jSONObject.put(PhilippinesConstants.currencyrate, data.length > 4 && data[4] != null ? (Double) data[4] : 0.0);
                jSONObject.put(PhilippinesConstants.discountType, data.length > 5 && data[5] != null ? data[5].toString() : "F");
                jSONObject.put(PhilippinesConstants.discountValueInBase, data.length > 6 && data[6] != null ? Double.parseDouble(data[6].toString()) : 0.0);
                jSONObject.put(PhilippinesConstants.rowtaxamount, data.length > 7 && data[7] != null ? Double.parseDouble(data[7].toString()) : 0.0);
                jSONObject.put(PhilippinesConstants.personID, data.length > 8 && data[8] != null ? data[8].toString() : "");
                jSONObject.put(PhilippinesConstants.personName, data.length > 9 && data[9] != null ? data[9].toString() : "");
                jSONObject.put(PhilippinesConstants.documentDetailsID, data.length > 10 && data[10] != null ? data[10].toString() : "");
                jSONObject.put(PhilippinesConstants.isGlobalTax, data.length > 11 && data[11] != null && Integer.valueOf(data[11].toString()) == 1 ? true : false);
                jSONObject.put(PhilippinesConstants.globalTaxInBase, data.length > 12 && data[12] != null ? (Double) data[12] : 0.0);
                jSONObject.put(PhilippinesConstants.isPayment, data.length > 13 && data[13] != null ? (String) data[13] : "F");
                jSONObject.put(PhilippinesConstants.detailAmount, data.length > 14 && data[14] != null ? (Double) data[14] : 0d);
                jSONObject.put(PhilippinesConstants.address1, data.length > 15 && data[15] != null ? data[15].toString() : "");
                jSONObject.put(PhilippinesConstants.vendorTIN, data.length > 16 && data[16] != null ? data[16].toString() : "");
                jSONObject.put(PhilippinesConstants.taxId, data.length > 17 && data[17] != null ? data[16].toString() : "");
                jSONObject.put(PhilippinesConstants.isAssetPurchaseInvoice, data.length > 18 && Integer.valueOf(data[18].toString()) == 1 ? true : false);
            }
            returnDataObject.put(jSONObject);
        }
        return returnDataObject;
    }

    public void getColumnModelForPurchaseReliefReportSummary(JSONArray jarrRecords, JSONArray jarrColumns, JSONObject params) throws JSONException {
        String storeRec = "";
        Locale requestcontextutilsobj = null;
        if (params.has("requestcontextutilsobj")) {
            requestcontextutilsobj = (Locale) params.opt("requestcontextutilsobj");
        }
        JSONObject tempJobj = new JSONObject();
        storeRec = "vendortin,comapnyname,lastname,firstname,middlename,address1,address2,exemptamt,zeroratedamt,serviceamt,capitalgoodsamt,otherthancapitalgoodsamt,taxableamt,vatrate,inputvatamt,totalpurchasesamt";
        String[] recArray = storeRec.split(",");
        for (String rec : recArray) {
            tempJobj = new JSONObject();
            tempJobj.put("name", rec);
            jarrRecords.put(tempJobj);
        }
        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.Vendor_TIN", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "vendortin");
        tempJobj.put("align", "center");
        tempJobj.put("width", 150);
        jarrColumns.put(tempJobj);

        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.companyName", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "comapnyname");
        tempJobj.put("align", "center");
        tempJobj.put("width", 200);
        jarrColumns.put(tempJobj);

        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.lastName", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "lastname");
        tempJobj.put("align", "center");
        tempJobj.put("width", 150);
        jarrColumns.put(tempJobj);

        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.firstName", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "firstname");
        tempJobj.put("align", "center");
        tempJobj.put("width", 150);
        jarrColumns.put(tempJobj);

        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.middleName", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "middlename");
        tempJobj.put("align", "center");
        tempJobj.put("width", 150);
        jarrColumns.put(tempJobj);

        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.address1", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "address1");
        tempJobj.put("align", "center");
        tempJobj.put("width", 150);
        jarrColumns.put(tempJobj);

        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.address2", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "address2");
        tempJobj.put("align", "center");
        tempJobj.put("width", 150);
        jarrColumns.put(tempJobj);

        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.exempt", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "exemptamt");
        tempJobj.put("align", "right");
        tempJobj.put("width", 150);
        tempJobj.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(tempJobj);

        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.zeroRated", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "zeroratedamt");
        tempJobj.put("align", "right");
        tempJobj.put("width", 150);
        tempJobj.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(tempJobj);

        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.services", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "serviceamt");
        tempJobj.put("align", "right");
        tempJobj.put("width", 150);
        tempJobj.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(tempJobj);

        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.capitalGoods", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "capitalgoodsamt");
        tempJobj.put("align", "right");
        tempJobj.put("width", 150);
        tempJobj.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(tempJobj);

        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.otherThancapitalGoods", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "otherthancapitalgoods");
        tempJobj.put("align", "right");
        tempJobj.put("width", 150);
        tempJobj.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(tempJobj);

        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.taxableNetofVat", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "taxableamt");
        tempJobj.put("align", "right");
        tempJobj.put("width", 150);
        tempJobj.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(tempJobj);

        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.vatRate", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "vatrate");
        tempJobj.put("align", "right");
        tempJobj.put("width", 150);
        jarrColumns.put(tempJobj);

        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.inputVat", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "inputvatamt");
        tempJobj.put("align", "right");
        tempJobj.put("width", 150);
        tempJobj.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(tempJobj);

        tempJobj = new JSONObject();
        tempJobj.put("header", "<b>" + messageSource.getMessage("acc.php.header.totalPurchases", null, requestcontextutilsobj) + "</b>");
        tempJobj.put("dataindex", "totalpurchasesamt");
        tempJobj.put("align", "right");
        tempJobj.put("width", 150);
        tempJobj.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jarrColumns.put(tempJobj);
    }

    /**
     * ERP-41756 Code for sales relief report data (calculating respective values for customer wise invoices)
     * @param requestParams
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONArray getSalesReliefReportData(JSONObject requestParams) throws JSONException, ServiceException {   
        JSONObject sectionExtraParams=new JSONObject();        
        JSONArray returnArr=new JSONArray();
        /**
         * Get Sales Invoices data 
         */
        List<Object> invoiceData = accPhilippinesComplianceDAO.getSalesInvoiceListDataInSQL(requestParams, sectionExtraParams);
        JSONArray dataJsonArray = getSalesDocumentDetailsDataInJSONObject(requestParams, sectionExtraParams, invoiceData);
        Map<String, JSONArray> customerData = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(dataJsonArray, PhilippinesConstants.personID);
        for (String custKey : customerData.keySet()) {
            /**
             * Iterate Invoice wise data
             */
            
            JSONArray venArray = customerData.get(custKey);
            JSONObject customerObj = new JSONObject();
            double totalInvoiceAmountSummary = 0d;
            double totalTaxableAmountSummary = 0d;
            double totalTaxAmountSummary = 0d;
            double capitalGoodsSummary = 0d;
            double totalExemptAmount=0d;
            double totalZeroRatedAmount=0d;           
            String defaultTaxId = "";
                JSONArray documentsArr = customerData.get(custKey);
                Map<String, JSONArray> invoiceDetailMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(documentsArr, PhilippinesConstants.documentDetailsID);
                double totalInvoiceAmount = 0d;
                double totalTaxableAmount = 0d;
                double totalTaxAmount = 0d;
                double globalTaxTaxAmount = 0d;
                double totalCapitalGoodsAmount = 0d; // to calculate amount of asset purchase invoice.                
                boolean isGlobalTax = false;
                double vatRate=0d;
                for (String invoiceDetailKey : invoiceDetailMap.keySet()) {
                    /**
                     * Iterate Invoice Detail data
                     */
                    JSONArray invDetailArr = invoiceDetailMap.get(invoiceDetailKey);
                    JSONObject invDetailobj = invDetailArr.getJSONObject(0);
                    double rate = invDetailobj.optDouble(PhilippinesConstants.rate);
                    double quantity = invDetailobj.optDouble(PhilippinesConstants.quantity);
                    double discount = 0d;
                    String discpercentage = invDetailobj.optString(PhilippinesConstants.discountType);
                    double discountvalue = invDetailobj.optDouble(PhilippinesConstants.discountValueInBase);
                    double rowTaxAmount = invDetailobj.optDouble(PhilippinesConstants.rowtaxamount);
                    isGlobalTax = invDetailobj.optBoolean(PhilippinesConstants.isGlobalTax);
                    globalTaxTaxAmount = invDetailobj.optDouble(PhilippinesConstants.globalTaxInBase, 0d);
                    defaultTaxId = invDetailobj.optString(PhilippinesConstants.defaultTaxId);
                    vatRate = invDetailobj.optDouble(PhilippinesConstants.vatRate);
                    double unitPrice = rate * quantity;
                    double taxableAmount = 0d;
                    double taxAmount = 0d;                    
                    if (!StringUtil.isNullOrEmpty(discpercentage)) {
                        if (discpercentage.equalsIgnoreCase("T")) {
                            discount = discountvalue * (rate * quantity) / 100;
                        } else {
                            discount = discountvalue;
                        }
                    }
                    taxableAmount = unitPrice - discount;
                    totalInvoiceAmount += taxableAmount + rowTaxAmount; //total invoice amount
                    totalTaxAmount += rowTaxAmount; //total invoice tax amount                    
                    /*
                     * If default tax != Exempt,Zero_rated then only add taxableAmount into NetTaxableAmountofVAT ERP-41756 
                     */
                    if(!defaultTaxId.equalsIgnoreCase(PhilippinesConstants.TAX_LIST.get(PhilippinesConstants.TAX_Exempt))&&!defaultTaxId.equalsIgnoreCase(PhilippinesConstants.TAX_LIST.get(PhilippinesConstants.TAX_Zero_Rated))){
                        totalTaxableAmount += taxableAmount;                       
                    } 
                    /*
                     * If default tax != Exempt,Zero_rated then only add taxableAmount into NetTaxableAmountofVAT ERP-41756 
                     */
                    if(defaultTaxId.equalsIgnoreCase(PhilippinesConstants.TAX_LIST.get(PhilippinesConstants.TAX_Exempt))){
                        totalExemptAmount += taxableAmount; 
                        vatRate = 0d;
                    }else if(defaultTaxId.equalsIgnoreCase(PhilippinesConstants.TAX_LIST.get(PhilippinesConstants.TAX_Zero_Rated))){
                        totalZeroRatedAmount += taxableAmount;
                        vatRate = 0d;
                    }
                }
                if (isGlobalTax) {
                    totalTaxAmount += globalTaxTaxAmount;
                    totalInvoiceAmount += globalTaxTaxAmount;
                }
                totalTaxableAmountSummary += totalTaxableAmount;
                totalInvoiceAmountSummary += totalInvoiceAmount;
                totalTaxAmountSummary += totalTaxAmount;
                capitalGoodsSummary += totalCapitalGoodsAmount;
            
            JSONObject temp = new JSONObject();
            temp = venArray.getJSONObject(0);
            customerObj.put(PhilippinesConstants.client_TIN, temp.optString(PhilippinesConstants.client_TIN));
            customerObj.put(PhilippinesConstants.companyName, temp.optString(PhilippinesConstants.companyName));
            customerObj.put(PhilippinesConstants.lastName, temp.optString(PhilippinesConstants.personName));
            customerObj.put(PhilippinesConstants.firstName, "");
            customerObj.put(PhilippinesConstants.middleName, "");
            customerObj.put(PhilippinesConstants.address1, temp.optString(PhilippinesConstants.address1));
            customerObj.put(PhilippinesConstants.taxableNetofVat, totalTaxableAmountSummary);
            customerObj.put(PhilippinesConstants.totalSales, totalInvoiceAmountSummary);
            customerObj.put(PhilippinesConstants.vatRate, vatRate);
            customerObj.put(PhilippinesConstants.outputVat, totalTaxAmountSummary);  
            customerObj.put(PhilippinesConstants.grossTaxable,totalInvoiceAmountSummary);
            customerObj.put(PhilippinesConstants.exempt,totalExemptAmount);
            customerObj.put(PhilippinesConstants.zeroRated,totalZeroRatedAmount);
            returnArr.put(customerObj);           
        }      
        return returnArr;       
      }
    /**
     * ERP-41756 Code for sales relief summary report
     * @param requestParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    @Override
    public JSONObject getSalesReliefSummaryReport(JSONObject requestParams) throws ServiceException, JSONException {
        JSONObject returnJSONobj = new JSONObject();
        JSONArray pagedJson = new JSONArray();
        String start = requestParams.optString("start");
        String limit = requestParams.optString("limit");
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        /**
         * Get Column details for Sales Relief Report
         */
        getColumnModelForSalesReliefData(jarrRecords, jarrColumns, requestParams);
        JSONArray dataJArr = new JSONArray();
        /**
         * Get Data Details for Sales Relief Report
         */
        dataJArr=getSalesReliefReportData(requestParams);
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
        returnJSONobj.put("metaData", jMeta);
        return returnJSONobj;
    }
}