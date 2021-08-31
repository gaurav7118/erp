/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.FieldConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.ExpenseGRDetail;
import com.krawler.hql.accounting.GoodsReceiptDetail;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.hql.accounting.ReceiptDetail;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.accInvoiceControllerCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.service.AccProductService;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.salescommission.AccSalesCommissionDAO;
import com.krawler.spring.accounting.salescommission.SalesCommissionRuleCondition;
import com.krawler.spring.accounting.salescommission.SalesCommissionRules;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.mrp.WorkOrder.WorkOrderDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.context.MessageSource;

public class AccOtherReportsServiceImpl implements AccOtherReportsService {

    private fieldDataManager fieldDataManagercntrl;
    private accAccountDAO accAccountDAOobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accReceiptDAO accReceiptDAOobj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private accGoodsReceiptDAO accGoodsReceiptDAOObj;
    private AccSalesCommissionDAO accSalesCommissionDAO;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private MessageSource messageSource;
    private WorkOrderDAO workOrderDAOObj;
    private AccProductService AccProductService;
    private fieldManagerDAO fieldManagerDAOobj;
    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
    public void setAccReceiptDAOobj(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }

    public void setAccMasterItemsDAOobj(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    
    public void setAccSalesCommissionDAO(AccSalesCommissionDAO accSalesCommissionDAO) {
        this.accSalesCommissionDAO = accSalesCommissionDAO;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setAccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptDAOObj) {
        this.accGoodsReceiptDAOObj = accGoodsReceiptDAOObj;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }
    
    public void setWorkOrderDAOObj(WorkOrderDAO workOrderDAOObj) {
        this.workOrderDAOObj = workOrderDAOObj;
    }
    
    public void setAccProductService(AccProductService AccProductService) {
        this.AccProductService = AccProductService;
    }

    public void getCommissionData(JSONObject params, JSONObject dataObj) throws ServiceException, JSONException, SessionExpiredException, UnsupportedEncodingException {
        String schemaVal = params.optString("SchemaValue");
        String dimension = params.optString("Dimension");
        String companyid = params.optString("companyid");
        String filterby = params.optString("filterby");
        KwlReturnObject result = null;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        
        if (schemaVal.trim().equalsIgnoreCase("all")) {
            /*
             * In Case of all value Getting all values of schema dimension.
             */
            requestParams.put(Constants.filter_names, Arrays.asList(FieldConstants.Crm_fieldid, FieldConstants.Crm_deleteflag));
            requestParams.put(Constants.filter_values, Arrays.asList(params.optString("SchemaDimension"), 0));
            ArrayList order_by = new ArrayList();
            ArrayList order_type = new ArrayList();
            order_by.add("itemsequence");
            order_by.add("value");
            order_type.add(" ");
            order_type.add("asc");
            requestParams.put("order_by", order_by);
            requestParams.put("order_type", order_type);
            result = accAccountDAOobj.getCustomCombodata(requestParams);
            schemaVal = "";

            List<Object[]> list = result.getEntityList();
            for(Object row[] : list){
                FieldComboData tmpcontyp = (FieldComboData) row[0];
                schemaVal += tmpcontyp.getId() +",";
            }
            schemaVal = schemaVal.substring(0, schemaVal.length()-1);
        }
        
        String[] schemaValueArr = schemaVal.split(",");
        String schemaValue = "";
        JSONArray dataArr = new JSONArray();
        for (int cnt = 0; cnt < schemaValueArr.length; cnt++) {
            schemaValue = schemaValueArr[cnt];

            /**
             * Tagged Schema for schema dimension value
             */
            String commissionSchema = "";
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), schemaValue);
            FieldComboData fieldComboData1 = (FieldComboData) rdresult.getEntityList().get(0);
            if (fieldComboData1 != null) {
                commissionSchema = fieldComboData1.getSalesCommissionSchemaMaster();
            }

            /**
             * Search Json for selected schema value
             */
            JSONObject searchobjforschema = fieldDataManagercntrl.getAdvanceSearchJson(schemaValue);

            /**
             * Get field combo data for 3rd option
             */
            KwlReturnObject kwlReturnObject = accAccountDAOobj.getFieldComboDatabyFieldID(dimension, companyid);
            List<FieldComboData> comboDatas = kwlReturnObject.getEntityList();
            /*
             * Need to get data for each value of selected dimension
             */

            for (FieldComboData fieldComboData : comboDatas) {
                JSONArray searchJsonArr = new JSONArray();
                String Value = fieldComboData.getId();
                JSONObject searchobjfordim = fieldDataManagercntrl.getAdvanceSearchJson(Value);
                searchJsonArr.put(searchobjfordim);
                searchJsonArr.put(searchobjforschema);
                JSONObject serchJson = new JSONObject();
                serchJson.put("root", searchJsonArr);
                String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
                /**
                 * Get Goods receipt
                 */
                double cost = 0d;
                double revenue = 0d;
                double grossprofit = 0d;
                double commission = 0d;
                double marginPer = 0d;

                HashMap<String, Object> request = new HashMap();
                request.put("df", params.opt("df"));
                request.put("startdate", params.optString("startdate"));
                request.put("enddate", params.optString("enddate"));
                request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                request.put("searchJson", serchJson);
                request.put("companyid", companyid);
                request.put("filterby", filterby);
                KwlReturnObject returnObject = accGoodsReceiptDAOObj.getGoodsReceiptForCommissionSchema(request);
                List<GoodsReceiptDetail> goodsReceiptDetails = returnObject.getEntityList();
                for (GoodsReceiptDetail goodsReceiptDetail : goodsReceiptDetails) {
                    cost += goodsReceiptDetail.getPurchaseJED().getAmountinbase();
                }

                /**
                 * Get Invoice
                 */
                request.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                String searchJsonforInvoice = fieldDataManagercntrl.getSearchJsonByModuleID(request);
                request.put("searchJson", searchJsonforInvoice);

                returnObject = accInvoiceDAOobj.getInvoicesForCommissionSchema(request);
                List<InvoiceDetail> invoiceDetails = returnObject.getEntityList();
                for (InvoiceDetail invoiceDetail : invoiceDetails) {
                    revenue += invoiceDetail.getSalesJED().getAmountinbase();
                }

                /**
                 * Calculate gross profit
                 */
                grossprofit = revenue - cost;

                /**
                 * Calculate Margin %
                 */
                if (grossprofit > 0 && revenue > 0) {
                    marginPer = grossprofit * 100 / revenue;
                }

                /**
                 * Calculate sales commission schema
                 */
                request.put("schemamaster", commissionSchema);
                kwlReturnObject = accSalesCommissionDAO.getCommossionSchemaTaggedToDimension(request);
                List<SalesCommissionRuleCondition> commissionRuleConditions = kwlReturnObject.getEntityList();
                double upperlimit = 0d;
                double lowerlimit = 0d;
                for (SalesCommissionRuleCondition salesCommissionRuleCondition : commissionRuleConditions) {
                    if (salesCommissionRuleCondition.getCommissionType() == 4) {
                        /**
                         * for margin
                         */
                        upperlimit = salesCommissionRuleCondition.getUpperLimit();
                        lowerlimit = salesCommissionRuleCondition.getLowerLimit();
                        boolean isPercentage = salesCommissionRuleCondition.getCommissionRules().getSchemaType() == 1 ? true : false;
                        double commsionamt = salesCommissionRuleCondition.getCommissionRules().getAmount();
                        if (salesCommissionRuleCondition.getMarginCondition() == 1) {
                            /*
                             * Between
                             */
                            if (marginPer <= upperlimit && marginPer >= lowerlimit) {
                                if (isPercentage) {
                                    /*
                                     * Percenatage
                                     */
                                    commission = grossprofit * commsionamt / 100;
                                } else {
                                    /**
                                     * Flat
                                     */
                                    commission = commsionamt;
                                }
                            }
                        } else if (salesCommissionRuleCondition.getMarginCondition() == 2) {
                            /**
                             * Greater than
                             */
                            if (marginPer >= lowerlimit) {
                                if (isPercentage) {
                                    commission = grossprofit * commsionamt / 100;
                                } else {
                                    commission = commsionamt;
                                }
                            }

                        } else {
                            if (marginPer <= upperlimit) {
                                if (isPercentage) {
                                    commission = grossprofit * commsionamt / 100;
                                } else {
                                    commission = commsionamt;
                                }
                            }
                        }
                    }
                }
                /**
                 * put all data
                 */
                if(cost !=0 || revenue !=0) { // Avoiding data having zero values
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("fieldid", fieldComboData.getValue());
                    jSONObject.put("schemaValue", fieldComboData1.getValue());
                    jSONObject.put("cost", cost);
                    jSONObject.put("revenue", revenue);
                    jSONObject.put("marginper", marginPer);
                    jSONObject.put("grossprofit", grossprofit);
                    jSONObject.put("commission", commission);
                    dataArr.put(jSONObject);
                }
            }
        }
        dataObj.put("dataArr", dataArr);
    }
    public void getProfitabilityReport(JSONObject params, JSONObject dataObj) throws ServiceException, JSONException, SessionExpiredException, UnsupportedEncodingException {
        String dimension = params.optString("Dimension");
        String companyid = params.optString("companyid");
        JSONArray dataArr = new JSONArray();
        String searchjson = params.optString("searchJson");
        JSONArray searcharray = null;
        if (!StringUtil.isNullOrEmpty(searchjson)) {
            JSONObject serachJobj = new JSONObject(searchjson);
            searcharray = serachJobj.getJSONArray(Constants.root);
        }
        
        /**
         * Get field combo data for dimension
         */
        KwlReturnObject kwlReturnObject = accAccountDAOobj.getFieldComboDatabyFieldID(dimension, companyid);
        List<FieldComboData> comboDatas = kwlReturnObject.getEntityList();
        /*
         Need to get data for each value of selected dimension
         */

        for (FieldComboData fieldComboData : comboDatas) {
            JSONArray searchJsonArr = new JSONArray();
            String Value = fieldComboData.getId();
            JSONObject searchobjfordim = fieldDataManagercntrl.getAdvanceSearchJson(Value);
            searchJsonArr.put(searchobjfordim);
            if (searcharray != null && searcharray.length() > 0) {
                for (int i = 0; i < searcharray.length(); i++) {
                    JSONObject nObject = searcharray.getJSONObject(i);
                    searchJsonArr.put(nObject);
                }
            }
            JSONObject serchJson = new JSONObject();
            serchJson.put("root", searchJsonArr);
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            /**
             * Get Goods receipt
             */
            double cost = 0d;
            double revenue = 0d;
            double grossprofit = 0d;
            double grosspercentage = 0d;

            HashMap<String, Object> request = new HashMap();
            request.put("df", params.opt("df"));
            request.put("startdate", params.optString("startdate"));
            request.put("enddate", params.optString("enddate"));
            request.put("filterConjuctionCriteria", filterConjuctionCriteria);
            request.put("searchJson", serchJson);
            request.put("companyid", companyid);
            /*
             *get custom fields search json for vendor invoice.
             */
            request.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
            String searchJsonforVendorInvoice = fieldDataManagercntrl.getSearchJsonByModuleID(request);
            request.put("searchJson", searchJsonforVendorInvoice);
            searchJsonforVendorInvoice = fieldDataManagercntrl.getSearchJsonForModuleID(request).toString();
            request.put("searchJson", searchJsonforVendorInvoice);
            
            request.put("isExpense", false);
            KwlReturnObject returnObject = accGoodsReceiptDAOObj.getGoodsReceiptForCommissionSchema(request);
            List<GoodsReceiptDetail> goodsReceiptDetails = returnObject.getEntityList();
            for (GoodsReceiptDetail goodsReceiptDetail : goodsReceiptDetails) {
                cost += goodsReceiptDetail.getPurchaseJED().getAmountinbase();
            }

            /**
             * get Expense Invoice
             */
            request.put("isExpense", true);
            returnObject = accGoodsReceiptDAOObj.getGoodsReceiptForCommissionSchema(request);
            List<ExpenseGRDetail> expenseGRDetails = returnObject.getEntityList();
            for (ExpenseGRDetail expenseGRDetail : expenseGRDetails) {
                cost += expenseGRDetail.getPurchaseJED().getAmountinbase();
            }
            /**
             * Get Invoice
             */
            request.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
            String searchJsonforInvoice = fieldDataManagercntrl.getSearchJsonByModuleID(request);
            request.put("searchJson", searchJsonforInvoice);
            searchJsonforVendorInvoice = fieldDataManagercntrl.getSearchJsonForModuleID(request).toString();
            request.put("searchJson", searchJsonforVendorInvoice);

            returnObject = accInvoiceDAOobj.getInvoicesForCommissionSchema(request);
            List<InvoiceDetail> invoiceDetails = returnObject.getEntityList();
            for (InvoiceDetail invoiceDetail : invoiceDetails) {
                revenue += invoiceDetail.getSalesJED().getAmountinbase();
            }

            /**
             * Calculate gross profit
             */
            grossprofit = revenue - cost;
            
            /**
             * Calculate gross profit percentage
             */
            if (cost > 0) {
                grosspercentage = (grossprofit * 100) / revenue;
            }
            boolean showValue = true;
            if ((searcharray != null && searcharray.length() == 1 && (StringUtil.DecodeText(searcharray.optJSONObject(0).optString("columnheader")).equalsIgnoreCase(params.optString("DimensionLable"))))) {
                /*
                 * For Job to date profitability report when we are grouping and
                 * searching on same field. Then show result for only selected
                 * value.
                 */
                showValue = false;
                String searchStr = StringUtil.DecodeText(searcharray.optJSONObject(0).optString("combosearch"));
                String[] searchTextArr = searchStr.split(",");
                for (String searchText : searchTextArr) {
                    if (searchText.trim().equalsIgnoreCase(fieldComboData.getValue())) {
                        showValue = true;
                        break;
                    }
                }
            }
                
            if(showValue) {
                /**
                 * put all data
                 */
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("fieldid", fieldComboData.getValue());
                jSONObject.put("cost", cost);
                jSONObject.put("revenue", revenue);
                jSONObject.put("grossprofit", grossprofit);
                jSONObject.put("grosspercent", grosspercentage);
                dataArr.put(jSONObject);
            }

        }
        dataObj.put("dataArr", dataArr);
    }

    public JSONObject getSalesCommission(JSONObject params) throws ServiceException, SessionExpiredException, UnsupportedEncodingException {
        JSONObject object = new JSONObject();
        JSONObject jobj1 = new JSONObject();
        Locale locale = (Locale) params.opt("locale");
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        String storeRec = "";
        String start = params.optString("start");
        String limit = params.optString("limit");
        boolean isprofitreport = params.optBoolean("isprofitreport", false);
        try {
            storeRec = "fieldid,revenue,cost,grossprofit,commission,schemaValue,grosspercent,marginper";
            String[] recArr = storeRec.split(",");
            // Get those fields in record for whome, no special properties present like type, defVal, mapping etc.
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }
            if (!StringUtil.isNullOrEmpty(params.optString("Dimension"))) {

                // Gel column model - 
                jobjTemp = new JSONObject();
                jobjTemp.put("header", params.optString("DimensionLable"));
                jobjTemp.put("dataIndex", "fieldid");
                jobjTemp.put("align", "center");
                jobjTemp.put("summaryRenderer", "function(value){ return \"<b>Total</b>\"}");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("header", isprofitreport ? messageSource.getMessage("acc.jobdateprofit.Actualrevenue", null, locale) : messageSource.getMessage("acc.salescommission.revenue", null, locale));
                jobjTemp.put("dataIndex", "revenue");
                jobjTemp.put("summaryType", "sum");
                jobjTemp.put("width", 150);
                jobjTemp.put("align", "center");
                jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
                jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("header", isprofitreport ? messageSource.getMessage("acc.jobdateprofit.ActualExpense", null, locale) : messageSource.getMessage("acc.salescommission.cost", null, locale));
                jobjTemp.put("dataIndex", "cost");
                jobjTemp.put("summaryType", "sum");
                jobjTemp.put("width", 150);
                jobjTemp.put("align", "center");
                jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
                jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
                jobjTemp = new JSONObject();
                jobjTemp.put("header", messageSource.getMessage("acc.salescommission.grossprofit", null, locale));
                jobjTemp.put("dataIndex", "grossprofit");
                jobjTemp.put("summaryType", "sum");
                jobjTemp.put("width", 150);
                jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
                jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
                jobjTemp.put("align", "center");
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
                if (!params.optBoolean("isprofitreport", false)) {
                    jobjTemp = new JSONObject();
                    jobjTemp.put("header", messageSource.getMessage("acc.salescommission.marginper", null, locale));
                    jobjTemp.put("dataIndex", "marginper");
                    jobjTemp.put("width", 150);
                    jobjTemp.put("align", "center");
                    jobjTemp.put("renderer", "WtfGlobal.percentageRenderer");
                    jobjTemp.put("pdfwidth", 150);
                    jarrColumns.put(jobjTemp);
                    jobjTemp = new JSONObject();
                    jobjTemp.put("header", messageSource.getMessage("acc.salescommission.commission", null, locale));
                    jobjTemp.put("dataIndex", "commission");
                    jobjTemp.put("summaryType", "sum");
                    jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
                    jobjTemp.put("width", 150);
                    jobjTemp.put("align", "center");
                    jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
                    jobjTemp.put("pdfwidth", 150);
                    jarrColumns.put(jobjTemp);
                    jobjTemp = new JSONObject();
                    jobjTemp.put("header", "Schema Value");
                    jobjTemp.put("dataIndex", "schemaValue");
                    jobjTemp.put("width", 150);
                    jobjTemp.put("hidden", true);
                    jobjTemp.put("align", "center");
                    jobjTemp.put("pdfwidth", 150);
                    jarrColumns.put(jobjTemp);

                } else {
                    jobjTemp = new JSONObject();
                    jobjTemp.put("header", "Gross Percentage");
                    jobjTemp.put("dataIndex", "grosspercent");
                    jobjTemp.put("width", 150);
                    jobjTemp.put("align", "center");
                    jobjTemp.put("renderer", "WtfGlobal.percentageRenderer");
                    jobjTemp.put("pdfwidth", 150);
                    jarrColumns.put(jobjTemp);
                }

            } else {
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "");
                jobjTemp.put("dataIndex", "fieldid");
                jobjTemp.put("align", "center");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
            }

            /**
             *
             * Get Data
             */
            if (!StringUtil.isNullOrEmpty(params.optString("Dimension"))) {
                JSONObject jsono = new JSONObject();
                if (isprofitreport) {
                    /**
                     * Execute for Profit report
                     */
                    getProfitabilityReport(params, jsono);
                } else {
                    /**
                     * Execute for commission schema report
                     */
                    getCommissionData(params, jsono);
                }
                dataJArr = jsono.getJSONArray("dataArr");
            }

            // get above data along with extra data
            JSONArray pagedJson = new JSONArray();
            pagedJson = dataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            object.put("success", true);
            object.put("coldata", pagedJson);
            object.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            object.put("totalCount", dataJArr.length());
            jMeta.put("fields", jarrRecords);
            object.put("metaData", jMeta);

            jobj1.put("valid", true);
            boolean isExport = params.optBoolean("isExport", false);
            if (isExport) {
                object.put("data", dataJArr);
            } else {
                jobj1.put("data", object);
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return object;
    }
    /**
     * 
     * @param params
     * @description Get entire JSON Object with column model,recordset and actual data 
     * @return JSONObject
     * @throws ServiceException
     * @throws SessionExpiredException
     * @throws UnsupportedEncodingException 
     */
    public JSONObject getAmountSalesCommission(JSONObject params) throws ServiceException, SessionExpiredException, UnsupportedEncodingException {
        JSONObject object = new JSONObject();
        JSONObject jobj1 = new JSONObject();
        Locale locale = (Locale) params.opt("locale");
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        String storeRec = "";
        String start = params.optString("start");
        String limit = params.optString("limit");
        boolean isprofitreport = params.optBoolean("isprofitreport", false);
        try {
            storeRec = "fieldid,invoiceamt,commission,schemaValue";
            String[] recArr = storeRec.split(",");
            /**
             * Get Recordset
             */
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }
            if (!StringUtil.isNullOrEmpty(params.optString("Dimension"))) {
                /**
                 * Gel column model
                 */
                jobjTemp = new JSONObject();
                jobjTemp.put("header", params.optString("DimensionLable"));
                jobjTemp.put("dataIndex", "fieldid");
                jobjTemp.put("align", "center");
                jobjTemp.put("summaryRenderer", "function(value){ return \"<b>Total</b>\"}");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("header", messageSource.getMessage("acc.field.TotalInvoiceAmount", null, locale));
                jobjTemp.put("dataIndex", "invoiceamt");
                jobjTemp.put("summaryType", "sum");
                jobjTemp.put("width", 150);
                jobjTemp.put("align", "center");
                jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
                jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
                
                jobjTemp = new JSONObject();
                jobjTemp.put("header", messageSource.getMessage("acc.common.totalCommissionAmount", null, locale));
                jobjTemp.put("dataIndex", "commission");
                jobjTemp.put("summaryType", "sum");
                jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
                jobjTemp.put("width", 150);
                jobjTemp.put("align", "center");
                jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
                
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "Schema Value");
                jobjTemp.put("dataIndex", "schemaValue");
                jobjTemp.put("width", 150);
                jobjTemp.put("hidden", true);
                jobjTemp.put("align", "center");
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
            } else {
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "");
                jobjTemp.put("dataIndex", "fieldid");
                jobjTemp.put("align", "center");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
            }

            /**
             * Get actual JSON data
             */
            if (!StringUtil.isNullOrEmpty(params.optString("Dimension"))) {
                JSONObject jsono = new JSONObject();
                getAmountCommissionData(params, jsono);
                dataJArr = jsono.getJSONArray("dataArr");
            }

            JSONArray pagedJson = new JSONArray();
            pagedJson = dataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            object.put("success", true);
            object.put("coldata", pagedJson);
            object.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            object.put("totalCount", dataJArr.length());
            jMeta.put("fields", jarrRecords);
            object.put("metaData", jMeta);

            jobj1.put("valid", true);
            boolean isExport = params.optBoolean("isExport", false);
            if (isExport) {
                object.put("data", dataJArr);
            } else {
                jobj1.put("data", object);
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return object;
    }
    
    public void getAmountCommissionData(JSONObject params, JSONObject dataObj) throws ServiceException, JSONException, SessionExpiredException, UnsupportedEncodingException {
        String schemaVal = params.optString("SchemaValue");
        String dimension = params.optString("Dimension");
        String companyid = params.optString("companyid");
        String filterby = params.optString("filterby");
        KwlReturnObject result = null;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        
        if (schemaVal.trim().equalsIgnoreCase("all")) {
            /*
             * In Case of all value Getting all values of schema dimension.
             */
            requestParams.put(Constants.filter_names, Arrays.asList(FieldConstants.Crm_fieldid, FieldConstants.Crm_deleteflag));
            requestParams.put(Constants.filter_values, Arrays.asList(params.optString("SchemaDimension"), 0));
            ArrayList order_by = new ArrayList();
            ArrayList order_type = new ArrayList();
            order_by.add("itemsequence");
            order_by.add("value");
            order_type.add(" ");
            order_type.add("asc");
            requestParams.put("order_by", order_by);
            requestParams.put("order_type", order_type);
            result = accAccountDAOobj.getCustomCombodata(requestParams);
            schemaVal = "";

            List<Object[]> list = result.getEntityList();
            for(Object row[] : list){
                FieldComboData tmpcontyp = (FieldComboData) row[0];
                schemaVal += tmpcontyp.getId() +",";
            }
            schemaVal = schemaVal.substring(0, schemaVal.length()-1);
        }
        
        String[] schemaValueArr = schemaVal.split(",");
        String schemaValue = "";
        JSONArray dataArr = new JSONArray();
        for (int cnt = 0; cnt < schemaValueArr.length; cnt++) {
            schemaValue = schemaValueArr[cnt];
            /**
             * Tagged Schema for schema dimension value
             */
            String commissionSchema = "";
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), schemaValue);
            FieldComboData fieldComboData1 = (FieldComboData) rdresult.getEntityList().get(0);
            if (fieldComboData1 != null) {
                commissionSchema = fieldComboData1.getSalesCommissionSchemaMaster();
            }
            /**
             * Search Json for selected schema value
             */
            JSONObject searchobjforschema = fieldDataManagercntrl.getAdvanceSearchJson(schemaValue);

            /**
             * Get field combo data for 3rd option
             */
            KwlReturnObject kwlReturnObject = accAccountDAOobj.getFieldComboDatabyFieldID(dimension, companyid);
            List<FieldComboData> comboDatas = kwlReturnObject.getEntityList();
            /*
             * Need to get data for each value of selected dimension
             */

            for (FieldComboData fieldComboData : comboDatas) {
                JSONArray searchJsonArr = new JSONArray();
                String Value = fieldComboData.getId();
                JSONObject searchobjfordim = fieldDataManagercntrl.getAdvanceSearchJson(Value);
                searchJsonArr.put(searchobjfordim);
                searchJsonArr.put(searchobjforschema);
                JSONObject serchJson = new JSONObject();
                serchJson.put("root", searchJsonArr);
                String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
                /**
                 * Get Goods receipt
                 */
                double invoiceamt = 0d;
                double commissionAmount = 0d;

                HashMap<String, Object> request = new HashMap();
                request.put("df", params.opt("df"));
                request.put("startdate", params.optString("startdate"));
                request.put("enddate", params.optString("enddate"));
                request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                request.put("searchJson", serchJson);
                request.put("companyid", companyid);
                request.put("filterby", filterby);

                request.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                String searchJsonforInvoice = fieldDataManagercntrl.getSearchJsonByModuleID(request);
                request.put("searchJson", searchJsonforInvoice);

                /**
                 * Get Invoice
                 */
                KwlReturnObject returnObject = accInvoiceDAOobj.getInvoicesForCommissionSchema(request);
                List<InvoiceDetail> invoiceDetails = returnObject.getEntityList();
                for (InvoiceDetail invoiceDetail : invoiceDetails) {
                    invoiceamt += invoiceDetail.getSalesJED().getAmountinbase();
                }

                /**
                 * Get sales commission schema
                 */
                request.put("schemamaster", commissionSchema);
                kwlReturnObject = accSalesCommissionDAO.getCommossionSchemaTaggedToDimension(request);
                List<SalesCommissionRuleCondition> commissionRuleConditions = kwlReturnObject.getEntityList();
                double upperlimit = 0d;
                double lowerlimit = 0d;
                /**
                 * Calculate sales commission schema for Amount
                 */
                for (SalesCommissionRuleCondition salesCommissionRuleCondition : commissionRuleConditions) {
                    if (salesCommissionRuleCondition.getCommissionType() == 1) {
                        upperlimit = salesCommissionRuleCondition.getUpperLimit();
                        lowerlimit = salesCommissionRuleCondition.getLowerLimit();
                        boolean isPercentage = salesCommissionRuleCondition.getCommissionRules().getSchemaType() == 1 ? true : false;
                        double comAmount = salesCommissionRuleCondition.getCommissionRules().getAmount();
                        if (isPercentage) {
                            if (invoiceamt >= lowerlimit && invoiceamt >= upperlimit) {
                                /**
                                 * In this case, we already get diff "including lowerlimit & including upperlimit".
                                 */
                                if (lowerlimit == 0) {
                                    commissionAmount = commissionAmount + ((upperlimit - lowerlimit) * comAmount) / 100.00;
                                } else {
                                    /**
                                     * Adding +1 in diff because we are considering both values as "including lowerlimit & including upperlimit".
                                     */
                                    commissionAmount = commissionAmount + (((upperlimit - lowerlimit)+1) * comAmount) / 100.00;
                                }
                            } else if (invoiceamt >= lowerlimit && invoiceamt < upperlimit) {
                                /**
                                 * In this case, we already get diff "including lowerlimit & including upperlimit".
                                 */
                                if (lowerlimit == 0) {
                                    commissionAmount = commissionAmount + ((invoiceamt - lowerlimit) * comAmount) / 100.00;
                                } else {
                                    /**
                                     * Adding +1 in diff because we are considering both values as "including lowerlimit & including upperlimit".
                                     */
                                commissionAmount = commissionAmount + (((invoiceamt - lowerlimit)) * comAmount) / 100.00;
                            }
                            }
                        } else {
                            /**
                             * Calculation in case of flat discount
                             */
                            if (invoiceamt >= lowerlimit && invoiceamt <= upperlimit) {
                                commissionAmount = commissionAmount + comAmount;
                            }    
                        }
                    }
                }
                /**
                 * put all data and avoiding data having zero values
                 */
                if(invoiceamt !=0) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("fieldid", fieldComboData.getValue());
                    jSONObject.put("schemaValue", fieldComboData1.getValue());
                    jSONObject.put("invoiceamt", invoiceamt);
                    jSONObject.put("commission", commissionAmount);
                    dataArr.put(jSONObject);
                }
            }
        }
        dataObj.put("dataArr", dataArr);
    }
    /**
     * 
     * @param params
     * @description Get entire JSON Object with column model,recordset and actual data 
     * @return JSONObject
     * @throws ServiceException
     * @throws SessionExpiredException
     * @throws UnsupportedEncodingException 
     */
    public JSONObject getPaymentTermCommission(JSONObject params) throws ServiceException, SessionExpiredException, UnsupportedEncodingException {
        JSONObject object = new JSONObject();
        JSONObject jobj1 = new JSONObject();
        Locale locale = (Locale) params.opt("locale");
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        String storeRec = "";
        String start = params.optString("start");
        String limit = params.optString("limit");
        boolean isprofitreport = params.optBoolean("isprofitreport", false);
        try {
            storeRec = "fieldid,invoiceamt,commission,schemaValue";
            String[] recArr = storeRec.split(",");
            /**
             * Get Recordset
             */
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }
            if (!StringUtil.isNullOrEmpty(params.optString("Dimension"))) {
                /**
                 * Gel column model
                 */
                jobjTemp = new JSONObject();
                jobjTemp.put("header", params.optString("DimensionLable"));
                jobjTemp.put("dataIndex", "fieldid");
                jobjTemp.put("align", "center");
                jobjTemp.put("summaryRenderer", "function(value){ return \"<b>Total</b>\"}");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("header", messageSource.getMessage("acc.field.TotalInvoiceAmount", null, locale));
                jobjTemp.put("dataIndex", "invoiceamt");
                jobjTemp.put("summaryType", "sum");
                jobjTemp.put("width", 150);
                jobjTemp.put("align", "center");
                jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
                jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
                
                jobjTemp = new JSONObject();
                jobjTemp.put("header", messageSource.getMessage("acc.common.totalCommissionAmount", null, locale));
                jobjTemp.put("dataIndex", "commission");
                jobjTemp.put("summaryType", "sum");
                jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
                jobjTemp.put("width", 150);
                jobjTemp.put("align", "center");
                jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
                
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "Schema Value");
                jobjTemp.put("dataIndex", "schemaValue");
                jobjTemp.put("width", 150);
                jobjTemp.put("hidden", true);
                jobjTemp.put("align", "center");
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
            } else {
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "");
                jobjTemp.put("dataIndex", "fieldid");
                jobjTemp.put("align", "center");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
            }

            /**
             * Get actual JSON data
             */
            if (!StringUtil.isNullOrEmpty(params.optString("Dimension"))) {
                JSONObject jsono = new JSONObject();
                getPaymentTermCommissionData(params, jsono);
                dataJArr = jsono.getJSONArray("dataArr");
            }

            JSONArray pagedJson = new JSONArray();
            pagedJson = dataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            object.put("success", true);
            object.put("coldata", pagedJson);
            object.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            object.put("totalCount", dataJArr.length());
            jMeta.put("fields", jarrRecords);
            object.put("metaData", jMeta);

            jobj1.put("valid", true);
            boolean isExport = params.optBoolean("isExport", false);
            if (isExport) {
                object.put("data", dataJArr);
            } else {
                jobj1.put("data", object);
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return object;
    }
    public void getPaymentTermCommissionData(JSONObject params, JSONObject dataObj) throws ServiceException, JSONException, SessionExpiredException, UnsupportedEncodingException {
        String schemaVal = params.optString("SchemaValue");
        String dimension = params.optString("Dimension");
        String companyid = params.optString("companyid");
        String filterby = params.optString("filterby");
        KwlReturnObject result = null;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        
        if (schemaVal.trim().equalsIgnoreCase("all")) {
            /*
             * In Case of all value Getting all values of schema dimension.
             */
            requestParams.put(Constants.filter_names, Arrays.asList(FieldConstants.Crm_fieldid, FieldConstants.Crm_deleteflag));
            requestParams.put(Constants.filter_values, Arrays.asList(params.optString("SchemaDimension"), 0));
            ArrayList order_by = new ArrayList();
            ArrayList order_type = new ArrayList();
            order_by.add("itemsequence");
            order_by.add("value");
            order_type.add(" ");
            order_type.add("asc");
            requestParams.put("order_by", order_by);
            requestParams.put("order_type", order_type);
            result = accAccountDAOobj.getCustomCombodata(requestParams);
            schemaVal = "";

            List<Object[]> list = result.getEntityList();
            for(Object row[] : list){
                FieldComboData tmpcontyp = (FieldComboData) row[0];
                schemaVal += tmpcontyp.getId() +",";
            }
            schemaVal = schemaVal.substring(0, schemaVal.length()-1);
        }
        
        String[] schemaValueArr = schemaVal.split(",");
        String schemaValue = "";
        JSONArray dataArr = new JSONArray();
        for (int cnt = 0; cnt < schemaValueArr.length; cnt++) {
            schemaValue = schemaValueArr[cnt];
            /**
             * Tagged Schema for schema dimension value
             */
            String commissionSchema = "";
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), schemaValue);
            FieldComboData fieldComboData1 = (FieldComboData) rdresult.getEntityList().get(0);
            if (fieldComboData1 != null) {
                commissionSchema = fieldComboData1.getSalesCommissionSchemaMaster();
            }
            /**
             * Search Json for selected schema value
             */
            JSONObject searchobjforschema = fieldDataManagercntrl.getAdvanceSearchJson(schemaValue);

            /**
             * Get field combo data for 3rd option
             */
            KwlReturnObject kwlReturnObject = accAccountDAOobj.getFieldComboDatabyFieldID(dimension, companyid);
            List<FieldComboData> comboDatas = kwlReturnObject.getEntityList();
            /*
             * Need to get data for each value of selected dimension
             */

            for (FieldComboData fieldComboData : comboDatas) {
                JSONArray searchJsonArr = new JSONArray();
                String Value = fieldComboData.getId();
                JSONObject searchobjfordim = fieldDataManagercntrl.getAdvanceSearchJson(Value);
                searchJsonArr.put(searchobjfordim);
                searchJsonArr.put(searchobjforschema);
                JSONObject serchJson = new JSONObject();
                serchJson.put("root", searchJsonArr);
                String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
                double invoiceamt = 0d;
                double commissionAmount = 0d;

                HashMap<String, Object> request = new HashMap();
                request.put("df", params.opt("df"));
                request.put("startdate", params.optString("startdate"));
                request.put("enddate", params.optString("enddate"));
                request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                request.put("searchJson", serchJson);
                request.put("companyid", companyid);
                request.put("filterby", filterby);

                request.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                String searchJsonforInvoice = fieldDataManagercntrl.getSearchJsonByModuleID(request);
                request.put("searchJson", searchJsonforInvoice);

                /**
                 * Get Invoices
                 */
                KwlReturnObject returnObject = accInvoiceDAOobj.getInvoicesForCommissionSchema(request);
                List<InvoiceDetail> invoiceDetails = returnObject.getEntityList();
                HashMap<String,Date> invoicemap=new HashMap<String,Date>();
                for (InvoiceDetail invoiceDetail : invoiceDetails) {
                    invoiceamt += invoiceDetail.getSalesJED().getAmountinbase();
                    if(!invoicemap.containsKey(invoiceDetail.getInvoice().getID())){
//                        invoicemap.put(invoiceDetail.getInvoice().getID(), invoiceDetail.getInvoice().getJournalEntry().getEntryDate());
                        invoicemap.put(invoiceDetail.getInvoice().getID(), invoiceDetail.getInvoice().getCreationDate());
                    }
                }
                /**
                 * Calculate sales commission schema for Payments
                 */
                for (Map.Entry<String, Date> entry : invoicemap.entrySet()) {
                    request.put("schemamaster", commissionSchema);
                    kwlReturnObject = accSalesCommissionDAO.getCommossionSchemaTaggedToDimension(request);
                    List<SalesCommissionRuleCondition> commissionRuleConditions = kwlReturnObject.getEntityList();
                    double upperlimit = 0d;
                    double lowerlimit = 0d;
                    for (SalesCommissionRuleCondition salesCommissionRuleCondition : commissionRuleConditions) {
                        if (salesCommissionRuleCondition.getCommissionType() == 3) {
                            upperlimit = salesCommissionRuleCondition.getUpperLimit();
                            lowerlimit = salesCommissionRuleCondition.getLowerLimit();
                            
                            Date creationDate = entry.getValue();

                            Calendar scal = Calendar.getInstance();
                            scal.setTime(creationDate);
                            scal.add(Calendar.DATE, (int) lowerlimit);
                            Date scalDate = null;
                            String scalString = authHandler.getDateOnlyFormat().format(scal.getTime());
                            try {
                                scalDate = authHandler.getDateOnlyFormat().parse(scalString);
                            } catch (ParseException ex) {
                                scalDate = scal.getTime();
                                Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            Date fromDate = scalDate;

                            Calendar ecal = Calendar.getInstance();
                            ecal.setTime(creationDate);
                            ecal.add(Calendar.DATE, (int)upperlimit);

                            Date ecalDate = null;
                            String ecalString = authHandler.getDateOnlyFormat().format(ecal.getTime());
                            try {
                                ecalDate = authHandler.getDateOnlyFormat().parse(ecalString);
                            } catch (ParseException ex) {
                                ecalDate = ecal.getTime();
                                Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            Date toDate = ecalDate;

                            HashMap<String, Object> receiptMap = new HashMap<String, Object>();

                            receiptMap.put("invoiceid", entry.getKey());
                            receiptMap.put(Constants.companyKey, companyid);
                            receiptMap.put("startDate", fromDate);
                            receiptMap.put("endDate", toDate);
                            receiptMap.put("filterby", filterby);

                            KwlReturnObject receiptResult = accReceiptDAOobj.getReceiptFromInvoice(receiptMap);

                            double receivedAmtInBase = 0d;

                            List l = receiptResult.getEntityList();
                            Iterator recitr = l.iterator();
                            while (recitr.hasNext()) {
                                ReceiptDetail rd = (ReceiptDetail) recitr.next();
                                receivedAmtInBase += rd.getAmountInBaseCurrency();

                            }
                            boolean isPercentage = salesCommissionRuleCondition.getCommissionRules().getSchemaType() == 1 ? true : false;
                            double comAmount = salesCommissionRuleCondition.getCommissionRules().getAmount();
                            if(receivedAmtInBase!=0){
                                if (isPercentage) {
                                    commissionAmount += (receivedAmtInBase * comAmount / 100);
                                } else {
                                    commissionAmount +=comAmount;
                                }
                            }
                        }
                    }
                }
                /**
                 * put all data and avoiding data having zero values
                 */
                if(invoiceamt !=0) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("fieldid", fieldComboData.getValue());
                    jSONObject.put("schemaValue", fieldComboData1.getValue());
                    jSONObject.put("invoiceamt", invoiceamt);
                    jSONObject.put("commission", commissionAmount);
                    dataArr.put(jSONObject);
                }
            }
        }
        dataObj.put("dataArr", dataArr);
    }
    
    public JSONObject getBrandCommission(JSONObject params) throws ServiceException, SessionExpiredException, UnsupportedEncodingException {
        JSONObject object = new JSONObject();
        JSONObject jobj1 = new JSONObject();
        Locale locale = (Locale) params.opt("locale");
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        String storeRec = "";
        String start = params.optString("start");
        String limit = params.optString("limit");
        boolean isprofitreport = params.optBoolean("isprofitreport", false);
        try {
            storeRec = "fieldid,categoryname,invoiceamt,commission,schemaValue";
            String[] recArr = storeRec.split(",");
            // Get those fields in record for whome, no special properties present like type, defVal, mapping etc.
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }
            if (!StringUtil.isNullOrEmpty(params.optString("Dimension"))) {

                // Gel column model - 
                jobjTemp = new JSONObject();
                jobjTemp.put("header", params.optString("DimensionLable"));
                jobjTemp.put("dataIndex", "fieldid");
                jobjTemp.put("align", "center");
                jobjTemp.put("summaryRenderer", "function(value){ return \"<b>Total</b>\"}");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("header", messageSource.getMessage("acc.commission.ProductCategory", null, locale));
                jobjTemp.put("dataIndex", "categoryname");
                jobjTemp.put("width", 150);
                jobjTemp.put("align", "center");
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
                
                jobjTemp = new JSONObject();
                jobjTemp.put("header", messageSource.getMessage("acc.field.TotalInvoiceAmount", null, locale));
                jobjTemp.put("dataIndex", "invoiceamt");
                jobjTemp.put("summaryType", "sum");
                jobjTemp.put("width", 150);
                jobjTemp.put("align", "center");
                jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
                jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
                
                jobjTemp = new JSONObject();
                jobjTemp.put("header", messageSource.getMessage("acc.common.totalCommissionAmount", null, locale));
                jobjTemp.put("dataIndex", "commission");
                jobjTemp.put("summaryType", "sum");
                jobjTemp.put("summaryRenderer", "WtfGlobal.currencySummaryRenderer");
                jobjTemp.put("width", 150);
                jobjTemp.put("align", "center");
                jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
                
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "Schema Value");
                jobjTemp.put("dataIndex", "schemaValue");
                jobjTemp.put("width", 150);
                jobjTemp.put("hidden", true);
                jobjTemp.put("align", "center");
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
            } else {
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "");
                jobjTemp.put("dataIndex", "fieldid");
                jobjTemp.put("align", "center");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
            }

            /**
             * Get Data
             */
            if (!StringUtil.isNullOrEmpty(params.optString("Dimension"))) {
                JSONObject jsono = new JSONObject();
                getBrandCommissionData(params, jsono);
                dataJArr = jsono.getJSONArray("dataArr");
            }

            // get above data along with extra data
            JSONArray pagedJson = new JSONArray();
            pagedJson = dataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            object.put("success", true);
            object.put("coldata", pagedJson);
            object.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            object.put("totalCount", dataJArr.length());
            jMeta.put("fields", jarrRecords);
            object.put("metaData", jMeta);

            jobj1.put("valid", true);
            boolean isExport = params.optBoolean("isExport", false);
            if (isExport) {
                object.put("data", dataJArr);
            } else {
                jobj1.put("data", object);
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return object;
    }

    public void getBrandCommissionData(JSONObject params, JSONObject dataObj) throws ServiceException, JSONException, SessionExpiredException, UnsupportedEncodingException {
        String schemaVal = params.optString("SchemaValue");
        String dimension = params.optString("Dimension");
        String companyid = params.optString("companyid");
        String filterby = params.optString("filterby");
        KwlReturnObject result = null;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        
        if (schemaVal.trim().equalsIgnoreCase("all")) {
            /*
             * In Case of all value Getting all values of schema dimension.
             */
            requestParams.put(Constants.filter_names, Arrays.asList(FieldConstants.Crm_fieldid, FieldConstants.Crm_deleteflag));
            requestParams.put(Constants.filter_values, Arrays.asList(params.optString("SchemaDimension"), 0));
            ArrayList order_by = new ArrayList();
            ArrayList order_type = new ArrayList();
            order_by.add("itemsequence");
            order_by.add("value");
            order_type.add(" ");
            order_type.add("asc");
            requestParams.put("order_by", order_by);
            requestParams.put("order_type", order_type);
            result = accAccountDAOobj.getCustomCombodata(requestParams);
            schemaVal = "";

            List<Object[]> list = result.getEntityList();
            for(Object row[] : list){
                FieldComboData tmpcontyp = (FieldComboData) row[0];
                schemaVal += tmpcontyp.getId() +",";
            }
            schemaVal = schemaVal.substring(0, schemaVal.length()-1);
        }
        ArrayList filter_names = new ArrayList(),filter_params = new ArrayList();
        filter_names.add("company.companyID");
        filter_params.add(companyid);
        filter_names.add("masterGroup.ID");
        filter_params.add("19");//For Geting All Product Category
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        String[] schemaValueArr = schemaVal.split(",");
        String schemaValue = "";
        JSONArray dataArr = new JSONArray();
        for (int cnt = 0; cnt < schemaValueArr.length; cnt++) {
            schemaValue = schemaValueArr[cnt];
            /**
             * Tagged Schema for schema dimension value
             */
            String commissionSchema = "";
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), schemaValue);
            FieldComboData fieldComboData1 = (FieldComboData) rdresult.getEntityList().get(0);
            if (fieldComboData1 != null) {
                commissionSchema = fieldComboData1.getSalesCommissionSchemaMaster();
            }
            /**
             * Search Json for selected schema value
             */
            JSONObject searchobjforschema = fieldDataManagercntrl.getAdvanceSearchJson(schemaValue);

            /**
             * Get field combo data for 3rd option
             */
            KwlReturnObject kwlReturnObject = accAccountDAOobj.getFieldComboDatabyFieldID(dimension, companyid);
            List<FieldComboData> comboDatas = kwlReturnObject.getEntityList();
            /*
             * Need to get data for each value of selected dimension
             */

            for (FieldComboData fieldComboData : comboDatas) {
                JSONArray searchJsonArr = new JSONArray();
                String Value = fieldComboData.getId();
                JSONObject searchobjfordim = fieldDataManagercntrl.getAdvanceSearchJson(Value);
                searchJsonArr.put(searchobjfordim);
                searchJsonArr.put(searchobjforschema);
                JSONObject serchJson = new JSONObject();
                serchJson.put("root", searchJsonArr);
                String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
                /**
                 * Get Goods receipt
                 */

                HashMap<String, Object> request = new HashMap();
                request.put("df", params.opt("df"));
                request.put("startdate", params.optString("startdate"));
                request.put("enddate", params.optString("enddate"));
                request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                request.put("searchJson", serchJson);
                request.put("companyid", companyid);
                request.put("filterby", filterby);

                /**
                 * Get Invoice
                 */
                request.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                String searchJsonforInvoice = fieldDataManagercntrl.getSearchJsonByModuleID(request);
                request.put("searchJson", searchJsonforInvoice);

                KwlReturnObject categoryResult = accMasterItemsDAOobj.getMasterItems(filterRequestParams);    
                List category = categoryResult.getEntityList();
                Iterator itrcateogry = category.iterator();
                
                while (itrcateogry.hasNext()) {
                    MasterItem catItem = (MasterItem) itrcateogry.next();
                    request.put("categoryid", catItem.getID());
                    KwlReturnObject returnObject = accInvoiceDAOobj.getInvoicesForCommissionSchema(request);
                    List<InvoiceDetail> invoiceDetails = returnObject.getEntityList();
                    double invoiceamt = 0d;
                    double totalinvoiceamt = 0d;
                    double commissionAmount = 0d;
                    for (InvoiceDetail invoiceDetail : invoiceDetails) {
                        totalinvoiceamt += invoiceDetail.getSalesJED().getAmountinbase();
                        invoiceamt = invoiceDetail.getSalesJED().getAmountinbase();
                        
                        /**
                         * Calculate sales commission schema for brand
                         */
                        request.put("schemamaster", commissionSchema);
                        kwlReturnObject = accSalesCommissionDAO.getCommossionSchemaTaggedToDimension(request);
                        List<SalesCommissionRuleCondition> commissionRuleConditions = kwlReturnObject.getEntityList();
                        for (SalesCommissionRuleCondition salesCommissionRuleCondition : commissionRuleConditions) {
                            if (salesCommissionRuleCondition.getCommissionType() == 2 && salesCommissionRuleCondition.getCategoryId().equals(catItem.getID())) {
                                boolean isPercentage = salesCommissionRuleCondition.getCommissionRules().getSchemaType() == 1 ? true : false;
                                double comAmount = salesCommissionRuleCondition.getCommissionRules().getAmount();
                                if (isPercentage) {
                                    commissionAmount=commissionAmount+(invoiceamt*comAmount)/100.00;
                                } else {
                                    commissionAmount = commissionAmount + comAmount;
                                }
                            }
                        }
                    }
                    /**
                     * put all data
                     */
                    if (totalinvoiceamt != 0) { // Avoiding data having zero values
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("fieldid", fieldComboData.getValue());
                        jSONObject.put("schemaValue", fieldComboData1.getValue());
                        jSONObject.put("invoiceamt", totalinvoiceamt);
                        jSONObject.put("commission", commissionAmount);
                        jSONObject.put("categoryname", catItem.getValue());
                        dataArr.put(jSONObject);
                    }
                }
            }
        }
        dataObj.put("dataArr", dataArr);
    }
    
    @Override
    public List<MasterItem> getCostCategoryMasterItems(JSONObject jobject) throws ServiceException {
        List<MasterItem> list = null;
        try {
            
            String companyid = (jobject.has(Constants.companyKey) && !StringUtil.isNullOrEmpty(jobject.getString(Constants.companyKey))) ? jobject.getString(Constants.companyKey) : "";
            
            HashMap<String, Object> filterRequestParams = new HashMap<>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("masterGroup.ID");
            filter_params.add(Constants.COST_OF_MANUFACTURING_ID);
            filter_names.add("company.companyID");
            filter_params.add(companyid);
            if(jobject.has("costCategoryExpenseIds") && !StringUtil.isNullOrEmpty(jobject.getString("costCategoryExpenseIds"))) {
                if(!jobject.getString("costCategoryExpenseIds").equals("All")){
                    String costCategoryExpenseIds = "";
                    String[] ids = jobject.getString("costCategoryExpenseIds").split(",");
                    for(String id : ids){
                        costCategoryExpenseIds += "'" + id + "',";
                    }
                    if(!StringUtil.isNullOrEmpty(costCategoryExpenseIds)){
                        costCategoryExpenseIds = costCategoryExpenseIds.substring(0, costCategoryExpenseIds.length()-1);
                        
                        filter_names.add("INID");
                        filter_params.add(costCategoryExpenseIds);
                    }
                }
            }
            order_by.add("value");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
            list = result.getEntityList();
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(AccOtherReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return list;
    }
    
    @Override
    public JSONObject getCostOfManufacturingColumnModel(JSONObject jobject,Map<String,Object>dataMap) throws JSONException, ServiceException {
        int colWidth = 150;
        int colWidth_180 = 180;
        JSONObject commData = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        Locale requestcontextutilsobj = null;

        if (jobject.has("locale") && jobject.get("locale")!=null) {
            requestcontextutilsobj = (Locale) jobject.get("locale");
        }
        
        int calculationBasedOn = 0;
        if(jobject.has("calculationBasedOn") && !StringUtil.isNullOrEmpty(jobject.getString("calculationBasedOn"))) {
            calculationBasedOn = Integer.parseInt(jobject.getString("calculationBasedOn"));
        }

        String StoreRec = "name,id,quantity,cost_of_product,percentage_of_total,customfield";

        List<FieldComboData> comboData = Collections.EMPTY_LIST;
        if (dataMap.containsKey("accountFieldComboDatas")) {
            comboData = (List) dataMap.get("accountFieldComboDatas");
        }

        for (FieldComboData fieldComboData : comboData) {
            StoreRec += ",percentage_"+fieldComboData.getId()+"," + fieldComboData.getId();
        }
        StoreRec += ",total_expenses,total_cost_of_product";

        String[] recArr = StoreRec.split(",");
        for (String rec : recArr) {
            jobjTemp = new JSONObject();
            jobjTemp.put("name", rec);
            jarrRecords.put(jobjTemp);
        }


        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.CostOfManufacturingReport.header1", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "name");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jobjTemp.put("sortable", true);
        jobjTemp.put("calculationBasedOn", calculationBasedOn);
        jobjTemp.put("summaryRenderer", "function(){"
                + "return '<div class=\"grid-summary-common\">'+WtfGlobal.getLocaleText(\"acc.common.total\")+'</div>';"
                + "}");
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.CostOfManufacturingReport.header2", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "quantity");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jobjTemp.put("calculationBasedOn", calculationBasedOn);
        jobjTemp.put("summaryType", "sum");
        jobjTemp.put("summaryRenderer", "function(value, m, rec) {"
                + "return WtfGlobal.summaryRenderer(value);"
                + "}");
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.CostOfManufacturingReport.header3", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "cost_of_product");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("calculationBasedOn", calculationBasedOn);
        jobjTemp.put("pdfwidth", 75);
        jobjTemp.put("summaryType", "sum");
        jobjTemp.put("summaryRenderer", "function(value, m, rec) {"
                + "return WtfGlobal.currencySummaryRenderer(value, m, rec);"
                + "}");
        jarrColumns.put(jobjTemp);

//        jobjTemp = new JSONObject();
//        jobjTemp.put("header", messageSource.getMessage("acc.CostOfManufacturingReport.header4", null, requestcontextutilsobj));
//        jobjTemp.put("dataIndex", "percentage_of_total");
//        jobjTemp.put("align", "center");
//        jobjTemp.put("width", colWidth);
//        jobjTemp.put("pdfwidth", 75);
//        jobjTemp.put("calculationBasedOn", calculationBasedOn);
//        jarrColumns.put(jobjTemp);


        for(FieldComboData fcd : comboData){
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.CostOfManufacturingReport.header4", null, requestcontextutilsobj)+" ("+fcd.getValue()+")");
            jobjTemp.put("dataIndex", "percentage_"+fcd.getId());
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colWidth);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("ispercentage", true);
            jobjTemp.put("summaryType", "sum");
            jobjTemp.put("summaryRenderer", "function(value, m, rec) {"
                + "return WtfGlobal.returnsummaryRenderer(WtfGlobal.percentageRenderer(value, m, rec));"
                + "}");
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", fcd.getValue());
            jobjTemp.put("dataIndex", fcd.getId());
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colWidth);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("applycurrency", true);
            jobjTemp.put("summaryType", "sum");
            jobjTemp.put("summaryRenderer", "function(value, m, rec) {"
                + "return WtfGlobal.currencySummaryRenderer(value, m, rec);"
                + "}");
            jarrColumns.put(jobjTemp);
        }

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.CostOfManufacturingReport.header5", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "total_expenses");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jobjTemp.put("calculationBasedOn", calculationBasedOn);
        jobjTemp.put("summaryType", "sum");
        jobjTemp.put("summaryRenderer", "function(value, m, rec) {"
                + "return WtfGlobal.currencySummaryRenderer(value, m, rec);"
                + "}");
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.CostOfManufacturingReport.header6", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "total_cost_of_product");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jobjTemp.put("calculationBasedOn", calculationBasedOn);
        jobjTemp.put("summaryType", "sum");
        jobjTemp.put("summaryRenderer", "function(value, m, rec) {"
                + "return WtfGlobal.currencySummaryRenderer(value, m, rec);"
                + "}");
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", "Custom Field");
        jobjTemp.put("dataIndex", "customfield");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jobjTemp.put("calculationBasedOn", calculationBasedOn);
        jobjTemp.put("hidden", true);
        jarrColumns.put(jobjTemp);

        jMeta.put("totalProperty", "totalCount");
        jMeta.put("root", "coldata");
        jMeta.put("fields", jarrRecords);
        commData.put("columns", jarrColumns);
        commData.put("metadata", jMeta);
        return commData;
    }

    @Override
    public JSONObject getCostOfManufacturingData(JSONObject jobject,Map<String,Object>dataMap) throws JSONException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        try {
            JSONObject commData = new JSONObject();
            if (jobject.optBoolean("returnEmptyData", false)) {
                /**
                 * Empty data if custom field i.e.cost category is not selected.
                 */
                commData.put("coldata", dataJArr);
                commData.put("totalCount", 0);
                JSONArray jcom = new JSONArray();
                jcom.put(commData);
                jobj.put("valid", true);
                jobj.put("data", commData);
                return jobj;
            }
            
            String accountids = "";
            String companyid = (jobject.has(Constants.companyKey) && !StringUtil.isNullOrEmpty(jobject.getString(Constants.companyKey))) ? jobject.getString(Constants.companyKey) : "";
            DateFormat df=authHandler.getDateOnlyFormat();
            Date startdt = null;
            Date enddt = null;
                        
            if((jobject.has("startdate") && jobject.getString("startdate")!=null) && (jobject.has("enddate") && jobject.getString("enddate")!=null)) {
                startdt = df.parse(jobject.getString("startdate").toString());
                enddt = df.parse(jobject.getString("enddate").toString());
                dataMap.put(Constants.REQ_startdate,startdt);
                dataMap.put(Constants.REQ_enddate,enddt);
            }
            
            if((jobject.has("start") && jobject.getString("start")!=null) && (jobject.has("limit") && jobject.getString("limit")!=null)) {
                dataMap.put(Constants.start, jobject.getString("start"));
                dataMap.put(Constants.limit, jobject.getString("limit"));
            }

            dataMap.put("companyid", companyid);
            HashMap<String, Object> params = new HashMap<>();
            params.put("companyId", companyid);
            params.put("defaultStatusId", Constants.defaultWOstatus_CLOSED);
            KwlReturnObject kmsg = workOrderDAOObj.getWOStatusidFromDefaultID(params);// fetching company's In process id based on default WO In process id
            if (kmsg.getEntityList().size() > 0) {
                MasterItem miObj = (MasterItem) kmsg.getEntityList().get(0);
                dataMap.put("WOstatus", miObj.getID());
            }
            /* 
             * Get list of assembly products . Return all product i.e. don't apply product filter 
             */
            KwlReturnObject result = workOrderDAOObj.getCostOfManufacturingDetails(dataMap);
            List<Object[]> list = result.getEntityList();

            if (jobject.has("productIds") && !StringUtil.isNullOrEmpty(jobject.getString("productIds"))) {
                if (!jobject.getString("productIds").equals("All")) {
                    String productIds = "";
                    String[] ids = jobject.getString("productIds").split(",");
                    for (String prod : ids) {
                        productIds += "'" + prod + "',";
                    }
                    if (!StringUtil.isNullOrEmpty(productIds)) {
                        productIds = productIds.substring(0, productIds.length() - 1);
                        dataMap.put("productIds", productIds);
                    }
                }
            }
                        
            
            /*
             * Get product valuation data
             */
            JSONArray jsonArr = new JSONArray();
            HashMap<String, Object> requestParamsValuation = new HashMap();
            requestParamsValuation.put("companyid", companyid);
            if((jobject.has("startdate") && jobject.getString("startdate")!=null) && (jobject.has("enddate") && jobject.getString("enddate")!=null)) {
                requestParamsValuation.put(Constants.REQ_startdate, jobject.getString("startdate").toString());
                requestParamsValuation.put(Constants.REQ_enddate, jobject.getString("enddate").toString());
            }
            requestParamsValuation.put(Constants.df, df);
            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            requestParamsValuation.put("isActivateMRPModule", extraCompanyPreferences.isActivateMRPModule());
            requestParamsValuation.put("productCategoryid", "All");
            requestParamsValuation.put("productType", "All");
            requestParamsValuation.put("isFromStockReport", true);
            jsonArr = AccProductService.getInventoryValuationData(requestParamsValuation);
            
            
            
//            for(MasterItem masterItem : listMasterItems){
//                accountids += "'" + masterItem.getAccID() + "',";
//            }
//            if(!StringUtil.isNullOrEmpty(accountids)){
//                accountids = accountids.substring(0, accountids.length()-1);
//                requestParams.put("accountids", accountids);
//            }
//            /* 
//             * Get Cost Category Expense Amount accountwise
//             */
//            KwlReturnObject resultExpenseAmount = workOrderDAOObj.getCostCategoryExpenseAmount(requestParams);
//            List<Object[]> listExpenseAmount = resultExpenseAmount.getEntityList();
            
            /* 
             * Get report JSON data
             */
            dataJArr = getCostOfManufacturingJSON(jobject, list, jsonArr, dataMap);

            commData.put("coldata", dataJArr);
            commData.put("totalCount", list.size());
            JSONArray jcom = new JSONArray();
            jcom.put(commData);
            jobj.put("valid", true);
            jobj.put("data", commData);
        } catch (SessionExpiredException | ParseException ex) {
            Logger.getLogger(AccOtherReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccOtherReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }

        return jobj;
    }

    public JSONArray getCostOfManufacturingJSON(JSONObject jobject, List<Object[]> list, JSONArray jsonArr, Map<String,Object>dataObj) throws ServiceException, SessionExpiredException, ParseException {
        JSONArray dataJArr = new JSONArray();
        try {
            List<FieldComboData> comboDatas = (List) dataObj.get("accountFieldComboDatas");
            List<FieldComboData> comboDatasProduct = (List) dataObj.get("productFieldComboDatas");
            String companyid = (jobject.has(Constants.companyKey) && !StringUtil.isNullOrEmpty(jobject.getString(Constants.companyKey))) ? jobject.getString(Constants.companyKey) : "";
            int calculationBasedOn = 0;
            if(jobject.has("calculationBasedOn") && !StringUtil.isNullOrEmpty(jobject.getString("calculationBasedOn"))) {
                calculationBasedOn = Integer.parseInt(jobject.getString("calculationBasedOn"));
            }
            double totalcostofproduct = 0.0d;
//            double summation_costofproduct = 0.0d;
//            double summation_quantityofproduct = 0.0d;
            
            Map<String, Object[]> expenseWiseSumMap = new HashMap();
            for(FieldComboData fieldComboData : comboDatasProduct){
                Object[] objExpenseWise = {0.0d, 0.0d};
                expenseWiseSumMap.put(fieldComboData.getId(), objExpenseWise);
            }
            
            Map<String, Object> listExpenseAmountMap = getBalanceAgainstCustomFieldValue(dataObj);
//            for (Object row[] : listExpenseAmount) {
//                String accountid = row[0]!=null ? (String) row[0] : "";
//                double expenseamount = row[1]!=null ? (Double) row[1] : 0.0d;
//                listExpenseAmountMap.put(accountid, expenseamount);
//            }
            
            HashMap<String, Object[]> listProductMap = new HashMap();
            for (Object row[] : list) {
                String productid = row[0] != null ? (String) row[0] : "";
                /**
                 * Custom Values tagged to particular product.
                 */
                String productcustdata = row[5] != null ? (String) row[5] : "";
                Object[] obj = {0.0d, 0.0d, productcustdata};
                listProductMap.put(productid, obj);
            }
            JSONObject summation_costofproduct = new JSONObject();
            JSONObject summation_quantityofproduct = new JSONObject();
            List<String> fcdids = (List) dataObj.get("productsfcdids");
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject jObject = jsonArr.getJSONObject(i);
//                if(jObject.has("productTypeID") && !StringUtil.isNullOrEmpty(jObject.getString("productTypeID")) && jObject.getString("productTypeID").equals(Constants.ASSEMBLY)){
                if (listProductMap.containsKey(jObject.getString("productid"))) {
//                    System.out.println(jObject.getString("productname") + " , " + jObject.getString("evaluationcost")+ " , " + jObject.getString("quantity"));
                    double cost = !StringUtil.isNullOrEmpty(jObject.getString("evaluationcost")) ? Double.parseDouble(jObject.getString("evaluationcost")) : 0.0d;
                    double quantity = !StringUtil.isNullOrEmpty(jObject.getString("quantity")) ? Double.parseDouble(jObject.getString("quantity")) : 0.0d;
                    Object[] productData = listProductMap.get(jObject.optString("productid"));
                    String productcustdata = productData[2] != null ? (String) productData[2] : "";
                    List<String> productcustdataArr = Arrays.asList(productcustdata.split("\\s*,\\s*"));
                    List<String> matchedprodArr = new ArrayList();
                    if (!fcdids.isEmpty()) {
                        /**
                         * If any custom values selected to fetch data.
                         */
                        for (String string : productcustdataArr) {
                            if (fcdids.contains(string)) {
                                matchedprodArr.add(string);
                            }
                        }
                    }else {
                        /**
                         * If all values for selected custom field.
                         */
                        matchedprodArr = productcustdataArr;
                    }
                    int customdatalength = matchedprodArr.size();
                    for (String string : matchedprodArr) {
                        /**
                         * Create Map with : Key = Custom field values Values =
                         * Sum of cost of products to which that custom field
                         * values is tagged need split values if more than one
                         * value tagged to product therefor divide cost by
                         * length.
                         */
                        summation_costofproduct.put(string, summation_costofproduct.optDouble(string, 0) + cost / customdatalength);
                        summation_quantityofproduct.put(string, summation_quantityofproduct.optDouble(string, 0) + quantity / customdatalength);
                    }

                    Object[] obj = {cost, quantity, productcustdata};
                    if (customdatalength > 0) {
                        /**
                         * If products tagged with selected custom values.
                         */
                        listProductMap.put(jObject.getString("productid"), obj);
                    } else {
                        /**
                         * Remove product if selected custom values does not
                         * tagged to it.
                         */
                        listProductMap.remove(jObject.getString("productid"));
                    }
//                    summation_costofproduct += cost;
//                    summation_quantityofproduct += quantity;
                }
            }
            
//            double summation_cost=0d;
//            double summation_quantity=0d;
            for (Object row[] : list) {
                String productid = row[0] != null ? (String) row[0] : "";
                if (!listProductMap.containsKey(productid)) {
                    /**
                    * Continue if product not present in Map created using selected
                    * value basis. Ex If Salary Category tag to two products
                    * i.e.P1,P2 and there are 10 there products in the system then
                    * put only P1 and P2 in report.
                    */
                    continue;
                } else if (dataObj.containsKey("productIds")) {
                    /**
                    * Continue if product is not match with products selected from
                    * drop down
                    */
                    String productIds = dataObj.get("productIds").toString();
                    if (!productIds.contains(productid)) {
                        continue;
                    }
                }
                Object[] obj = listProductMap.get(productid);
//                summation_cost += obj[0] != null ? (Double) obj[0] : 0;
//                summation_quantity += obj[1] != null ? (Double) obj[1] : 0;
                String productcustdata = obj[2] != null ? (String) obj[2] : "";
                List<String> productcustdataArr = Arrays.asList(productcustdata.split("\\s*,\\s*"));
                for (String string : productcustdataArr) {
                    if(expenseWiseSumMap.containsKey(string)){
                        /*
                         * SUM(Product cost) by Dimension value to calculate ratio
                         */
                        Object[] objExpenseWise = expenseWiseSumMap.get(string);
                        double expwisecost = (Double) objExpenseWise[0] + (Double) obj[0];
                        double expwisequantity = (Double) objExpenseWise[1] + (Double) obj[1];
                        Object[] objExpenseWiseSum = {expwisecost, expwisequantity};
                        expenseWiseSumMap.put(string, objExpenseWiseSum);
                    }                        
                }
            }
            
            for (Object row[] : list) {
                JSONObject jSONObject = new JSONObject();

                String productid = row[0]!=null ? (String) row[0] : "";
                String productname = row[1]!=null ? (String) row[1] : "";
                double costofproduct = 0.0d;
                double quantity = 0.0d;

                if (!listProductMap.containsKey(productid)) {
                    /**
                     * Continue if product not present in Map created using
                     * selected value basis.
                     * Ex If Salary Category tag to two products i.e.P1,P2 and there are 10 there products in the system then
                     * put only P1 and P2 in report.
                     */
                    continue;
                } else if (dataObj.containsKey("productIds")) {
                    /**
                     * Continue if product is not match with products selected from drop down
                     */
                    String productIds = dataObj.get("productIds").toString();
                    if (!productIds.contains(productid)) {
                        continue;
                    }
                }
                Object[] obj = listProductMap.get(productid);
                costofproduct = (Double) obj[0];
                quantity = (Double) obj[1];
                
//                String workorderstatus = row[4]!=null ? (String) row[4] : "";
                
                double totalexpenses = 0.0d;// percentage_of_total = 0.0d, costofproductratio = 0.0d;
                String productcustdata=row[5]!=null ? (String) row[5] : "";
                String[] productcustdataArr = productcustdata.split(",");
//                if(summation_cost != 0 && summation_quantity != 0){
//                    if(calculationBasedOn == 1){
//                        costofproductratio = quantity / summation_quantity;
//                    }else{
//                        costofproductratio = costofproduct / summation_cost;
//                    }
//                    percentage_of_total = costofproductratio * 100;
//                }
                costofproduct = authHandler.round(costofproduct, companyid);
//                percentage_of_total = authHandler.round(percentage_of_total, companyid);
                        
                jSONObject.put("id", productid);
                jSONObject.put("name", productname);
                jSONObject.put("cost_of_product", costofproduct);
                jSONObject.put("quantity", quantity);
//                jSONObject.put("percentage_of_total", percentage_of_total);

                for(FieldComboData comboData : comboDatas){
                    /**
                     * Iterate values for selected custom field i.e.for cost category.
                     */
                    double categoryexpenseamount = 0.0d;
                    double costofproductratioexpensewise = 0.0d, expenseWiseSum = 0.0d;
                    boolean isPresent = false;
                    for(String str : productcustdataArr){
                        if(!StringUtil.isNullOrEmpty(str)){
                            FieldComboData fieldComboData = null;                           
                            for(FieldComboData productFieldComboData : comboDatasProduct){
                                if(productFieldComboData.getId().equals(str)){
                                    fieldComboData = productFieldComboData;
                                    break;
                                }
                            }
                            
                            if(fieldComboData!=null && fieldComboData.getValue().equals(comboData.getValue())){
                                if(calculationBasedOn == 1){
                                    expenseWiseSum = expenseWiseSumMap.containsKey(fieldComboData.getId()) ? (Double) expenseWiseSumMap.get(fieldComboData.getId())[1] : 0.0d;
                                }else{
                                    expenseWiseSum = expenseWiseSumMap.containsKey(fieldComboData.getId()) ? (Double) expenseWiseSumMap.get(fieldComboData.getId())[0] : 0.0d;
                                }
                                if(listExpenseAmountMap.containsKey(comboData.getValue()) && expenseWiseSum > 0){
                                    if(calculationBasedOn == 1){
                                        costofproductratioexpensewise = (quantity / expenseWiseSum) * 100;
                                    }else{
                                        costofproductratioexpensewise = (costofproduct / expenseWiseSum) * 100;
                                    }
                                    costofproductratioexpensewise = authHandler.round(costofproductratioexpensewise, companyid);
                                    costofproductratioexpensewise /= 100;
                                    categoryexpenseamount = ((Double) listExpenseAmountMap.get(comboData.getValue())) * costofproductratioexpensewise;
                                    isPresent = true;
                                    break;
                                }
                            }
                        }
                    }
                    categoryexpenseamount = authHandler.round(categoryexpenseamount, companyid);
                    costofproductratioexpensewise *= 100;
                    costofproductratioexpensewise = authHandler.round(costofproductratioexpensewise, companyid);
                    if(isPresent){
                        jSONObject.put(comboData.getId(), categoryexpenseamount);
                        totalexpenses += categoryexpenseamount;
                        jSONObject.put("percentage_"+comboData.getId(), costofproductratioexpensewise);
                    } else {
                        jSONObject.put(comboData.getId(), "NA");
                        jSONObject.put("percentage_"+comboData.getId(), costofproductratioexpensewise);
                    }
                }

                totalexpenses = authHandler.round(totalexpenses, companyid);
                jSONObject.put("total_expenses", authHandler.round(totalexpenses, companyid));
//                if(calculationBasedOn == 1){
//                    totalcostofproduct = quantity + totalexpenses;
//                }else{
                    totalcostofproduct = costofproduct + totalexpenses;
//                }
                
                jSONObject.put("total_cost_of_product", authHandler.round(totalcostofproduct, companyid));
                
                for(FieldComboData comboData : comboDatas){
                    jSONObject.put("customfield", comboData.getField().getFieldlabel());
                    break;
                }

                dataJArr.put(jSONObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccOtherReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return dataJArr;
    }
    /**
     * Function which returns the field comb data for any custom field
     *
     * @param params
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List<FieldComboData> getFieldComboData(JSONObject params) throws ServiceException, JSONException {
        List<FieldComboData> fieldComboDatas = Collections.emptyList();
        String companyid = params.optString("companyid");
        String customfieldname = params.optString("customfieldname");
        int moduleid = params.optInt("moduleid");
        /**
         * Get Field Param id
         */
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("filedname", "custom_" + customfieldname);
        hashMap.put("moduleid", moduleid);
        hashMap.put("companyid", companyid);
        String fieldid = fieldManagerDAOobj.getFieldParamsId(hashMap);

        HashMap<String, Object> filterRequestParams = new HashMap<>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        filter_names.add("fieldid");
        filter_params.add(fieldid);
        if (!StringUtil.isNullOrEmpty(customfieldname)) {
            if (params.has("customValues") && !StringUtil.isNullOrEmpty(params.getString("customValues"))) {
                if (!params.getString("customValues").equals("All")) {
                    String costCategoryExpenseIds = "";
                    String[] ids = params.getString("customValues").split(",");
                    for (String id : ids) {
                        costCategoryExpenseIds += "'" + id + "',";
                    }
                    if (!StringUtil.isNullOrEmpty(costCategoryExpenseIds)) {
                        costCategoryExpenseIds = costCategoryExpenseIds.substring(0, costCategoryExpenseIds.length() - 1);
                        filter_names.add("INValue");
                        filter_params.add(costCategoryExpenseIds);
                    }
                }
            }
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_values", filter_params);
            /**
             * Get Id from field combo data using column value
             */
            KwlReturnObject result = accAccountDAOobj.getFieldCombo(filterRequestParams);
            fieldComboDatas = result.getEntityList();
        }
        return fieldComboDatas;
    }

    /**
     * Function to get summation of account balance for which custom field
     * values are tagged.
     *
     * @param dataObj
     * @return
     * @throws ServiceException
     */
    public Map getBalanceAgainstCustomFieldValue(Map<String, Object> dataObj) throws ServiceException {
        List<FieldComboData> comboDatas = (List) dataObj.get("accountFieldComboDatas");
        String fcdids = "";
        for (FieldComboData fieldComboData : comboDatas) {
            fcdids += "'" + fieldComboData.getId() + "',";
        }
        if (fcdids.length() > 1) {
            fcdids = fcdids.substring(0, fcdids.length() - 1);
        }
        dataObj.put("fcdids", fcdids);
        KwlReturnObject resultExpenseAmount = workOrderDAOObj.getCostCategoryExpenseAmount(dataObj);
        List<Object[]> listExpenseAmount = resultExpenseAmount.getEntityList();
        HashMap<String, Object> listExpenseAmountMap = new HashMap();
        for (Object row[] : listExpenseAmount) {
            String fcdValue = row[0] != null ? (String) row[0] : "";
            double expenseamount = row[1] != null ? (Double) row[1] : 0.0d;
            listExpenseAmountMap.put(fcdValue, expenseamount);
        }
        return listExpenseAmountMap;
    }

    /**
     * Get Column number for any custom field based on module id.
     *
     * @param params
     * @return
     * @throws ServiceException
     */
    public int getCustomColumnNoForModuleField(JSONObject params) throws ServiceException {
        String fieldLabel = params.optString("customfieldname");
        String companyId = params.optString("companyid");
        int module = params.optInt("moduleid");
        int customColumn = params.optInt("islineitem", 0);
        int colnum = fieldManagerDAOobj.getColumnFromFieldParams(fieldLabel, companyId, module, customColumn);
        return colnum;
    }

    /**
     * Get only field combo data id.
     *
     * @param dataMap
     * @param params
     * @return
     * @throws JSONException
     */
    public List getFcdIdForField(Map<String, Object> dataMap, JSONObject params) throws JSONException {
        List<String> fcdids = new ArrayList();
        List<FieldComboData> productFieldComboDatas = (List) dataMap.get("productFieldComboDatas");
        if (params.has("customValues") && !StringUtil.isNullOrEmpty(params.getString("customValues"))) {
            if (!params.getString("customValues").equals("All")) {
                for (FieldComboData fieldComboData : productFieldComboDatas) {
                    fcdids.add(fieldComboData.getId());
                }
            }
        }
        return fcdids;
    }
}
