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
package com.krawler.spring.accounting.product;

import com.krawler.common.admin.ServerSpecificOptions;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;

import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.LicenseType;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.frequency.Frequency;
import com.krawler.inventory.model.packaging.Packaging;     //INV_ACC_MERGE
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.CommonFnController;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.springframework.context.MessageSource;

/**
 *
 * @author krawler
 */
public class productHandler {

    private static MessageSource messageSource;
    
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    public static HashMap<String, Object> getProductRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("productid", request.getParameter("productid"));
        requestParams.put("transactiondate", request.getParameter("transactiondate"));
        requestParams.put("type", request.getParameter("type"));
        requestParams.put("ids",  request.getParameterValues("ids"));
        
        if (request.getParameter("isAllSync") != null && !StringUtil.isNullOrEmpty(request.getParameter("isAllSync"))) {
            requestParams.put("isAllSync",  request.getParameter("isAllSync"));
        }
        
        if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
            requestParams.put("ss", request.getParameter("ss"));
        } else if (request.getParameter("query") != null && !StringUtil.isNullOrEmpty(request.getParameter("query"))) {
            requestParams.put("ss", request.getParameter("query"));
        }
        if (request.getParameter("isForBarcode") != null && !StringUtil.isNullOrEmpty(request.getParameter("isForBarcode"))) {
            requestParams.put("isForBarcode", Boolean.valueOf(request.getParameter("isForBarcode")));
        }
        if (request.getParameter("searchProductString") != null && !StringUtil.isNullOrEmpty(request.getParameter("searchProductString"))) {
            requestParams.put("searchProductString", request.getParameter("searchProductString"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("showallproduct"))) {
            requestParams.put("showallproduct", Integer.parseInt(request.getParameter("showallproduct")));
        }
        if (StringUtil.isNullOrEmpty(request.getParameter("filetype"))) {
            if ( !StringUtil.isNullOrEmpty(request.getParameter("start")) ) {
                requestParams.put("start", request.getParameter("start"));
            }
            if ( !StringUtil.isNullOrEmpty(request.getParameter("limit")) ) {
                requestParams.put("limit", request.getParameter("limit"));
            }
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("excludeParent"))) {
            requestParams.put("excludeParent", request.getParameter("excludeParent"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("natureOfStockItem"))) {
            requestParams.put("natureOfStockItem", request.getParameter("natureOfStockItem"));
        }
        if (request.getParameter(Constants.isFromDashBoard) != null && !StringUtil.isNullOrEmpty(request.getParameter(Constants.isFromDashBoard))) {
            requestParams.put(Constants.isFromDashBoard, Boolean.valueOf(request.getParameter(Constants.isFromDashBoard)));
        }
        if (request.getAttribute("exportPDFCSV") != null && !StringUtil.isNullOrEmpty(request.getAttribute("exportPDFCSV").toString())) {
            requestParams.put("exportPDFCSV", Boolean.parseBoolean(request.getAttribute("exportPDFCSV").toString()));
        }
        boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
        requestParams.put("isFixedAsset", isFixedAsset);
        boolean includeBothFixedAssetAndProductFlag = (!StringUtil.isNullOrEmpty(request.getParameter("includeBothFixedAssetAndProductFlag"))) ? Boolean.parseBoolean(request.getParameter("includeBothFixedAssetAndProductFlag")) : false;
        requestParams.put("includeBothFixedAssetAndProductFlag", includeBothFixedAssetAndProductFlag);
        requestParams.put("affecteduser", (request.getParameter("affecteduser") == null) ? "" : request.getParameter("affecteduser"));
        requestParams.put("forCurrency", (request.getParameter("forCurrency") == null) ? "" : request.getParameter("forCurrency"));
        requestParams.put("currency", (request.getParameter("currency") == null) ? "" : request.getParameter("currency"));
        
        //for Inventory
        boolean isStoreLocationEnable = (!StringUtil.isNullOrEmpty(request.getParameter("isStoreLocationEnable"))) ? Boolean.parseBoolean(request.getParameter("isStoreLocationEnable")) : false;
        requestParams.put("isStoreLocationEnable", isStoreLocationEnable);
        
        //for Inventory
        boolean isWarehouseLocationSet = (!StringUtil.isNullOrEmpty(request.getParameter("isWarehouseLocationSet"))) ? Boolean.parseBoolean(request.getParameter("isWarehouseLocationSet")) : false;
        requestParams.put("isWarehouseLocationSet", isWarehouseLocationSet);
        
         if (request.getParameter("isInventoryForm") != null && !StringUtil.isNullOrEmpty(request.getParameter("isInventoryForm"))) {
            requestParams.put("isInventoryForm", request.getParameter("isInventoryForm"));
         }   
        
        if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
            requestParams.put("dir", request.getParameter("dir"));
            requestParams.put("sort", request.getParameter("sort"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("quantity"))) {
            double doubleValue = Double.parseDouble(request.getParameter("quantity"));    //In Server Exception Log exception tracking-for case Quantity in double value i.e. 17.10 etc.
            int intValue = (int) doubleValue;
            requestParams.put("quantity", intValue);
        }
        requestParams.put("id", sessionHandlerImpl.getCompanyid(request));
        boolean isSalesPrice = (!StringUtil.isNullOrEmpty(request.getParameter("isSalesPrice"))) ? Boolean.parseBoolean(request.getParameter("isSalesPrice")) : false;
        requestParams.put("isSalesPrice", isSalesPrice);
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        boolean isWastageApplicable = (!StringUtil.isNullOrEmpty(request.getParameter("isWastageApplicable"))) ? Boolean.parseBoolean(request.getParameter("isWastageApplicable")) : false;
        requestParams.put("isWastageApplicable", isWastageApplicable);
        requestParams.put("companyCurrencyID", sessionHandlerImpl.getCurrencyID(request));
        requestParams.put("dateonlyformatter", authHandler.getDateOnlyFormatter(request));
        requestParams.put("isExportStr", request.getParameter("isExport"));
        requestParams.put("get", request.getParameter("get"));
        boolean isForProductQuantityDetailsReport = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isForProductQuantityDetailsReport"))) {
            isForProductQuantityDetailsReport = Boolean.parseBoolean(request.getParameter("isForProductQuantityDetailsReport"));
        }
        requestParams.put("isForProductQuantityDetailsReport", isForProductQuantityDetailsReport);
        requestParams.put("selectedUOMs", request.getParameter("selectedUOMs"));
        requestParams.put("loadInventory", Boolean.parseBoolean((String) request.getParameter("loadInventory")));
        return requestParams;
    }

    public static HashMap<String, Object> getProductRequestMapfromJson(JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
        requestParams.put("productid", paramJobj.optString("productid", null));
        requestParams.put("transactiondate", paramJobj.optString("transactiondate", null));
        requestParams.put("type", paramJobj.optString("type", null));
        if (paramJobj.has("ids") && !StringUtil.isNullOrEmpty(paramJobj.optString("ids",null))) {
            String ids=(String) paramJobj.get("ids");
            requestParams.put("ids", ids.split(","));
        }
         
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("isInventoryForm", null))) {
            requestParams.put("isInventoryForm", paramJobj.optString("isInventoryForm"));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("productTypeFilter", null))) {
            requestParams.put("productTypeFilter", paramJobj.optString("productTypeFilter"));
        }
                
        if (paramJobj.optString(Constants.ss,null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.ss,null))) {
            requestParams.put(Constants.ss, paramJobj.optString(Constants.ss));
        } else if (!StringUtil.isNullOrEmpty(paramJobj.optString("query",null))) {
            requestParams.put(Constants.ss, paramJobj.getString("query"));
        }
        
        if (paramJobj.optString(Constants.sortstring,null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.sortstring,null))) {
            requestParams.put(Constants.sortstring, paramJobj.optString(Constants.sortstring));
        }
        
        if (paramJobj.optString("searchProductString",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("searchProductString",null))) {
            requestParams.put("searchProductString", paramJobj.getString("searchProductString"));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("showallproduct",null))) {
            requestParams.put("showallproduct", Integer.parseInt(paramJobj.getString("showallproduct")));
        }
        if (StringUtil.isNullOrEmpty(paramJobj.optString("filetype",null))) {
            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.start,null))) {
                requestParams.put(Constants.start, paramJobj.getString(Constants.start));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.limit,null))) {
                requestParams.put(Constants.limit, paramJobj.getString(Constants.limit));
            }
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("excludeParent",null))) {
            requestParams.put("excludeParent", paramJobj.getString("excludeParent"));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("natureOfStockItem",null))) {
            requestParams.put("natureOfStockItem", paramJobj.getString("natureOfStockItem"));
        }
        boolean isFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString("isFixedAsset",null))) ? Boolean.parseBoolean(paramJobj.getString("isFixedAsset")) : false;
        requestParams.put("isFixedAsset", isFixedAsset);
        boolean includeBothFixedAssetAndProductFlag = (!StringUtil.isNullOrEmpty(paramJobj.optString("includeBothFixedAssetAndProductFlag",null))) ? Boolean.parseBoolean(paramJobj.getString("includeBothFixedAssetAndProductFlag")) : false;
        requestParams.put("includeBothFixedAssetAndProductFlag", includeBothFixedAssetAndProductFlag);
        requestParams.put("affecteduser", (paramJobj.optString("affecteduser",null) == null) ? "" : paramJobj.getString("affecteduser"));
        requestParams.put("forCurrency", (paramJobj.optString("forCurrency",null) == null) ? "" : paramJobj.getString("forCurrency"));
        requestParams.put("currency", (paramJobj.optString("currency",null) == null) ? "" : paramJobj.getString("currency"));

        //for Inventory
        boolean isStoreLocationEnable = (!StringUtil.isNullOrEmpty(paramJobj.optString("isStoreLocationEnable",null))) ? Boolean.parseBoolean(paramJobj.getString("isStoreLocationEnable")) : false;
        requestParams.put("isStoreLocationEnable", isStoreLocationEnable);

        //for Inventory
        boolean isWarehouseLocationSet = (!StringUtil.isNullOrEmpty(paramJobj.optString("isWarehouseLocationSet",null))) ? Boolean.parseBoolean(paramJobj.getString("isWarehouseLocationSet")) : false;
        requestParams.put("isWarehouseLocationSet", isWarehouseLocationSet);

        if (!StringUtil.isNullOrEmpty(paramJobj.optString("dir",null)) && !StringUtil.isNullOrEmpty(paramJobj.optString("sort",null))) {
            requestParams.put("dir", paramJobj.getString("dir"));
            requestParams.put("sort", paramJobj.getString("sort"));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("quantity",null))) {
            double doubleValue = Double.parseDouble(paramJobj.getString("quantity"));    //In Server Exception Log exception tracking-for case Quantity in double value i.e. 17.10 etc.
            int intValue = (int) doubleValue;
            requestParams.put("quantity", intValue);
        }
        requestParams.put("id", paramJobj.optString(Constants.companyKey, null));
        boolean isSalesPrice = (!StringUtil.isNullOrEmpty(paramJobj.optString("isSalesPrice",null))) ? Boolean.parseBoolean(paramJobj.getString("isSalesPrice")) : false;
        requestParams.put("isSalesPrice", isSalesPrice);
        requestParams.put(Constants.Acc_Search_Json, paramJobj.optString(Constants.Acc_Search_Json,null));
        requestParams.put(Constants.Filter_Criteria, paramJobj.optString(Constants.Filter_Criteria,null));
        requestParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));
        boolean isWastageApplicable = (!StringUtil.isNullOrEmpty(paramJobj.optString("isWastageApplicable",null))) ? Boolean.parseBoolean(paramJobj.getString("isWastageApplicable")) : false;
        requestParams.put("isWastageApplicable", isWastageApplicable);
        return requestParams;
    }

     public static JSONArray getProductsJson(HashMap<String, Object> requestMap, List list, accProductDAO accProductObj,accAccountDAO accAccountDAOobj,AccountingHandlerDAO accountingHandlerDAOobj, accCurrencyDAO accCurrencyDAOobj, boolean isCustomColumnExport) throws JSONException, ServiceException, ParseException {
        Calendar cstart = Calendar.getInstance();

        
        //If you are changing anything in this function then make same changes in the getProductsJson function which is available in accProductController file
        JSONArray jArr = new JSONArray();
            String companyId = (String) requestMap.get(Constants.companyKey);
            DateFormat userdf = (DateFormat) requestMap.get(Constants.userdf);
            boolean exportPDFCSV = false;
            if(requestMap.containsKey("exportPDFCSV") && requestMap.get("exportPDFCSV") != null){
                exportPDFCSV = (Boolean) requestMap.get("exportPDFCSV");
            }
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            Map<String, Double> quantityUnderQA=null;
            Map<String, Double> quantityUnderRepair=null;
            if(exportPDFCSV){
                cap = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company compObj=(Company) cap.getEntityList().get(0);
                quantityUnderQA = accProductObj.getProductQuantityUnderQA(compObj, null);
                quantityUnderRepair = accProductObj.getProductQuantityUnderRepair(compObj, null);
            }
            Calendar tempDate = Calendar.getInstance();
            String companyCurrencyID = (String) requestMap.get("companyCurrencyID");
            tempDate.setTime(preferences.getBookBeginningFrom());
            Date transactionDate = null;
            if(requestMap.containsKey("transactiondate") && requestMap.get("transactiondate") != null && !StringUtil.isNullOrEmpty(requestMap.get("transactiondate").toString())){
                //Used same date formatter which have used to save currency exchange
                DateFormat dateonlyformatter = (DateFormat) requestMap.get("dateonlyformatter");
                transactionDate = dateonlyformatter.parse((String) requestMap.get("transactiondate"));
            }
            Producttype producttype = new Producttype();
            String productid = (String) requestMap.get("productid");
            Boolean isSearch = false;
            int mode = 0;
            String isExportStr = (String) requestMap.get("isExportStr");
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(isExportStr) && isExportStr.equals("true")) {
                isExport = true;
            }
            String ss = "";
            if(requestMap.containsKey("ss") && requestMap.get("ss") != null && !StringUtil.isNullOrEmpty(requestMap.get("ss").toString())){
                ss = (String) requestMap.get("ss");
                isSearch = true;
            }
            if(requestMap.containsKey("get") && requestMap.get("get") != null && !StringUtil.isNullOrEmpty(requestMap.get("get").toString())){
                mode = Integer.parseInt((String) requestMap.get("get"));
            }

            boolean isForProductQuantityDetailsReport = (Boolean) requestMap.get("isForProductQuantityDetailsReport");

            String[] selectedUOMsArray = null;

            if (isForProductQuantityDetailsReport) {
                String selectedUOMs = (String) requestMap.get("selectedUOMs");
                
                selectedUOMsArray = selectedUOMs.split(",");
            }
            
            Boolean isFixedAsset = (Boolean) requestMap.get("isFixedAsset");
            Boolean nonSaleInventory = (Boolean) requestMap.get("loadInventory");
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            if (isCustomColumnExport) {
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, (isFixedAsset ? Constants.Acc_FixedAssets_AssetsGroups_ModuleId : Constants.Acc_Product_Master_ModuleId)));
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }
            
            KwlReturnObject basecurrresult = accProductObj.getObject(KWLCurrency.class.getName(), companyCurrencyID);
            KWLCurrency baseCurrency = (KWLCurrency) basecurrresult.getEntityList().get(0);
            String baseCurrencyName =  baseCurrency == null ? "" : baseCurrency.getName();
            Map<String, Double> blkQuantityList = accProductObj.getProductBlockedQuantity(preferences.getCompany(),  null,  null,  ss);
            Map<String, Double> reserveQuantityTaggedInQuotationMap = accProductObj.getReserveQuantityTaggedInQuotation(companyId);
            Map<String, String> currencyFromPriceListMap = accCurrencyDAOobj.getCurrencyFromPriceList(companyId, "T");
//            while (itr.hasNext()) {
            for (Object object : list) {
                try {
                    Object[] row = (Object[]) object;
                    Product product = (Product) row[0];
                    Product parentProduct = product.getParent();
                    if (product.getID().equals(productid)) {
                        continue;
                    }
                    double qaQuantity = 0, repairQuantity = 0;
                    if(exportPDFCSV){
                        if (quantityUnderQA.containsKey(product.getID()) && quantityUnderQA.get(product.getID()) != null) {
                            qaQuantity = quantityUnderQA.get(product.getID());
                        }
                        if (quantityUnderRepair.containsKey(product.getID()) && quantityUnderRepair.get(product.getID()) != null) {
                            repairQuantity = quantityUnderRepair.get(product.getID());
                        }
                    }
//                ProductCyclecount pcObject = (ProductCyclecount) row[8];
                    
                    //======refer ticket ERP-11075 & ERP-11606============
                    Date creationDate = new Date(product.getCreatedon());
                    Calendar cal = Calendar.getInstance(); // locale-specific
                    cal.setTime(creationDate);
//                    cal.setTimeZone(TimeZone.getTimeZone("GMT"+AuthHandler.getTimeZoneDifference(request)));        // need to add Application TimeZone
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    long time = cal.getTimeInMillis();
                    
                    JSONObject obj = new JSONObject();
                    obj.put("blockloosesell", product.isblockLooseSell() ? "T" : "F");
                    obj.put("isactive", product.isIsActive() ? "T" : "F");
                    obj.put("isknittingitem", product.isIsKnittingItem() ? "T" : "F");
                    obj.put("wipoffset", product.getWIPOffset());
                    obj.put("inventoryoffset", product.getInventoryOffset());
                    obj.put("recyclable", product.isRecyclable() ? "T" : "F");
                    obj.put("pricelist", (product.getPriceList() != null) ? product.getPriceList() : "");                    
                    obj.put("qaleadtimeindays", product.getQALeadTimeInDays());
                    obj.put("createdon", time);
                    obj.put("productid", mode==1110 ? product.getProductid():product.getID());
                    obj.put("currencyid", product.getCurrency()!=null?product.getCurrency().getCurrencyID():companyCurrencyID);
                    obj.put("productname", product.getName());
                    
                    /*---------Execute only for malaysian company----------   */
                    if (!StringUtil.isNullOrEmpty(product.getPurchasetaxid())) {
                        KwlReturnObject taxresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), product.getPurchasetaxid());
                        Tax tax = (Tax) taxresult.getEntityList().get(0);
                        obj.put("purchasetax", tax != null ? tax.getTaxCode() : "");
                    } else {
                        obj.put("purchasetax", "");
                    }

                    if (!StringUtil.isNullOrEmpty(product.getSalestaxid())) {
                        KwlReturnObject taxresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), product.getSalestaxid());
                        Tax tax = (Tax) taxresult.getEntityList().get(0);
                        obj.put("salestax", tax != null ? tax.getTaxCode() : "");
                    } else {
                        obj.put("salestax", "");
                    }
                    
                    obj.put("productcodeid", !StringUtil.isNullOrEmpty(product.getProductid())?product.getProductid():"");
                    obj.put("description",!StringUtil.isNullOrEmpty(product.getDescription()) ? StringUtil.DecodeText(StringUtil.replaceFullHTML(product.getDescription().replaceAll("<br>", "\n"))):""); //ERP-35791
//                    obj.put("desc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));
                    obj.put("desc", StringUtil.isNullOrEmpty(product.getDescription()) ? "" : StringUtil.DecodeText(StringUtil.replaceFullHTML(product.getDescription().replaceAll("<br>", "\n"))));    //ERP-27500
                    obj.put("tariffname", product.getTariffName());
                    obj.put("hsncode", product.getHSNCode());
                    obj.put("asofdate", product.getAsOfDate()!=null ? userdf.format(product.getAsOfDate()) : "");
                    obj.put("reportinguom", product.getReportinguom());
                    UnitOfMeasure uom = product.getUnitOfMeasure();
                    UnitOfMeasure purchaseuom = product.getPurchaseUOM();
                    UnitOfMeasure salesuom = product.getSalesUOM();
                    obj.put("uomid", uom == null ? "" : uom.getID());
                    obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
                    obj.put("purchaseuom", purchaseuom==null?"":isExport?purchaseuom.getNameEmptyforNA():purchaseuom.getID());
                    obj.put("salesuom", salesuom==null?"":isExport?salesuom.getNameEmptyforNA():salesuom.getID());
                    obj.put("stockpurchaseuomvalue",  purchaseuom==null || product.getPackaging()==null ? 1 :product.getPackaging().getStockUomQtyFactor(purchaseuom));
                    obj.put("stocksalesuomvalue", salesuom==null || product.getPackaging()==null ? 1 :product.getPackaging().getStockUomQtyFactor(salesuom));
                    obj.put("precision", uom == null ? 0 : (Integer) uom.getAllowedPrecision());
                    if(product.getLeadTimeInDays()==0 && (product.getProducttype() != null && product.getProducttype().getName().equalsIgnoreCase("Service")) && exportPDFCSV){
                        obj.put("leadtime", "");
                    } else {
                        obj.put("leadtime", product.getLeadTimeInDays());
                    }
                    obj.put("QAleadtime", product.getQALeadTimeInDays());
                    obj.put("hsCode", product.getHSCode());
                    //if(product.getWarrantyperiod() !=-1){
                    obj.put("warrantyperiod", product.getWarrantyperiod());
                    obj.put("warrantyperiodsal", product.getWarrantyperiodsal());
                    obj.put("productBrandName", product.getProductBrand() != null ? product.getProductBrand().getValue().toString() : ""); //ERP-41953 : SDP-16424"While exporting product master to EXCEL, Product Brand column is exported Blank"
                    double reservestock = 0;
                    if(reserveQuantityTaggedInQuotationMap.containsKey(product.getID()) && reserveQuantityTaggedInQuotationMap.get(product.getID()) != null){
                        reservestock = reserveQuantityTaggedInQuotationMap.get(product.getID());
                    }
                    if(product.getProducttype() != null && product.getProducttype().getName().equalsIgnoreCase("Service") && exportPDFCSV){
                        obj.put("reservestock", "N/A");
                    } else {
                        obj.put("reservestock", reservestock);
                    }
                    // }
                    if(exportPDFCSV){
                        obj.put("qaquantity", qaQuantity);
                        obj.put("repairquantity", repairQuantity);
                    }
                    obj.put("supplier", product.getSupplier());
                    obj.put("coilcraft", product.getCoilcraft());
                    obj.put("interplant", product.getInterplant());
                    obj.put("syncable", product.isSyncable()? "T" : "F");
                    obj.put("deleted", product.isDeleted());
                    obj.put("multiuom", product.isMultiuom()? "T" : "F");
                    obj.put("uomschematypeid", product.getUomSchemaType()!=null?product.getUomSchemaType().getID():"");
                    obj.put("uomSchemaTypeName", product.getUomSchemaType() != null ? product.getUomSchemaType().getName() : "");
                    obj.put("displayUoMName", product.getDisplayUoM() != null ? product.getDisplayUoM().getNameEmptyforNA() : "");
//                    obj.put("autoAssembly", product.isAutoAssembly());
                    if ((product.getProducttype() != null && product.getProducttype().getName().equalsIgnoreCase("Inventory Assembly"))) {
                        if (product.isAutoAssembly()) {
                            obj.put("autoAssembly", "Yes");
                        } else {
                            obj.put("autoAssembly", "No");
                        }
                    } else {
                        obj.put("autoAssembly", "N/A");
                    }
                    obj.put("isLocationForProduct", product.isIslocationforproduct());
                    obj.put("isWarehouseForProduct", product.isIswarehouseforproduct());
                    obj.put("isBatchForProduct", product.isIsBatchForProduct());
                    obj.put("isSerialForProduct", product.isIsSerialForProduct());
                    obj.put("isRecyclable", product.isRecyclable());
                    obj.put("rcmapplicable", product.isRcmApplicable() ? "Yes" : "No");
                    if(product.getProducttype() != null && product.getProducttype().getName().equalsIgnoreCase("Service") && exportPDFCSV){
                        obj.put("recycleQuantity", "N/A");
                    } else {
                        obj.put("recycleQuantity", product.getRecycleQuantity());
                    }
                    obj.put("qaenable", product.isQaenable() ? "T" : "F");
                    obj.put("reorderlevel", product.getReorderLevel());
                    if(product.getProducttype() != null && product.getProducttype().getName().equalsIgnoreCase("Service") && exportPDFCSV){
                        obj.put("reorderlevel", "N/A");
                    } else {
                        obj.put("reorderlevel", product.getReorderLevel());
                    }
                    if(product.getProducttype() != null && product.getProducttype().getName().equalsIgnoreCase("Service") && exportPDFCSV){
                        obj.put("reorderquantity", "N/A");
                    } else {
                        obj.put("reorderquantity", product.getReorderQuantity());
                    }                   
                    obj.put("minorderingquantity", product.getMinOrderingQuantity());
                    obj.put("maxorderingquantity", product.getMaxOrderingQuantity());
                    obj.put("purchaseaccountid", (product.getPurchaseAccount() != null ? product.getPurchaseAccount().getID() : ""));
                    obj.put("salesaccountid", (product.getSalesAccount() != null ? product.getSalesAccount().getID() : ""));
                    obj.put("shelfLocationId", (product.getShelfLocation() != null ? product.getShelfLocation().getId() : ""));
                    obj.put("location", (product.getLocation() != null ? product.getLocation().getId() : ""));
                    obj.put("warehouse", (product.getWarehouse() != null ? product.getWarehouse().getId() : ""));
                    obj.put("locationName", (product.getLocation() != null ? product.getLocation().getName() : ""));
                    obj.put("warehouseName", (product.getWarehouse() != null ? product.getWarehouse().getName() : ""));

                    //            obj.put("salesacctaxcode", "c340667e2896c0d80128a569f065017a");//(product.getPurchaseAccount()!=null?product.getPurchaseAccount().getID():""));
                    //            obj.put("purchaseacctaxcode", "c340667e2896c0d80128a569f065017a");//(product.getSalesAccount()!=null?product.getSalesAccount().getID():""));
                    obj.put("purchaseacctaxcode", ((product.getPurchaseAccount() != null && (!StringUtil.isNullOrEmpty(product.getPurchaseAccount().getTaxid()))) ? product.getPurchaseAccount().getTaxid() : ""));
                    obj.put("salesacctaxcode", ((product.getSalesAccount() != null && (!StringUtil.isNullOrEmpty(product.getSalesAccount().getTaxid()))) ? product.getSalesAccount().getTaxid() : ""));
                    obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : ""));
                    obj.put("salesretaccountid", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getID() : ""));
                    obj.put("salesRevenueRecognitionAccountid", (product.getSalesRevenueRecognitionAccount() != null ? product.getSalesRevenueRecognitionAccount().getID() : ""));
                    obj.put("revenueRecognitionProcess", (product.isRevenueRecognitionProcess()));
                    obj.put("vendor", (product.getVendor() != null ? product.getVendor().getID() : ""));
                    obj.put("vendornameid", (product.getVendor() != null ? product.getVendor().getName() : ""));
                    obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
                    obj.put("vendorphoneno", (product.getVendor() != null ? product.getVendor().getContactNumber() : ""));
                    obj.put("vendoremail", (product.getVendor() != null ? product.getVendor().getEmail() : ""));
                    if (product.isAsset()) {   //For Fixed Asset Group, type will be "Asset"
                        obj.put("type", "Asset");
                    } else {
                        obj.put("type", (product.getProducttype() != null ? product.getProducttype().getName() : ""));
                    }
                    obj.put("dependenttype", (product.getDependenttype() != null ? product.getDependenttype().getID() : ""));
                    obj.put("dependenttypename", (product.getDependenttype() != null ? product.getDependenttype().getValue() : ""));
                    obj.put("intervalfield", product.isIntervalfield());
                    obj.put("timeinterval", product.getTimeinterval());
                    obj.put("addshiplentheithqty", product.isAddshiplentheithqty());
                    obj.put("pid", product.getProductid());
                    obj.put("noofqty", product.getNoofquqntity());
                    obj.put("qtyUOM", product.getNoofqtyvalue());
                    if (product.getWarrantyperiod() == 0 && exportPDFCSV) {
                        obj.put("warranty", "");
                    } else if(product.getWarrantyperiod() == 0){
                        obj.put("warranty", "NA");
                    } else{
                        obj.put("warranty", product.getWarrantyperiod());
                    }
                    if(product.getWarrantyperiodsal() == 0 && exportPDFCSV){
                        obj.put("warrantysal", "");
                    }else if (product.getWarrantyperiodsal() == 0) {
                        obj.put("warrantysal", "N/A");
                    } else {
                        obj.put("warrantysal", product.getWarrantyperiodsal());
                    }
                    obj.put("sequenceformatid", product.getSeqformat() != null ? product.getSeqformat().getID() : "");
                    obj.put("parentuuid", parentProduct == null ? "" : parentProduct.getID());
                    obj.put("parentid", parentProduct == null ? "" : parentProduct.getProductid());
                    obj.put("parentname", parentProduct == null ? "" : parentProduct.getName());
                    if (isSearch) {
                        obj.put("level", 0);
                        obj.put("leaf", true);
                    } else {
                        obj.put("level", row[1]);
                        obj.put("leaf", row[2]);
                    }
                    

                    Double purchasePrice = row[3] != null ? (Double) row[3] : 0;
                    Double salesPrice = row[4] != null ? (Double) row[4] : 0;
                    double purchasePriceInBase = purchasePrice;
                    double salesPriceInBase = salesPrice;
                    String productCurrencyId = "";//product.getCompany().getCurrency().getCurrencyID();
    //                if(product.getCurrency()!=null){
    //                    productCurrencyId=productCurrencyID;
    //                }
                    if (currencyFromPriceListMap.containsKey(product.getID()) && currencyFromPriceListMap.get(product.getID()) != null) {
                        productCurrencyId = currencyFromPriceListMap.get(product.getID());
                    } else {
                        productCurrencyId = companyCurrencyID;
                     }

                    if(!productCurrencyId.equals(companyCurrencyID)) {
                        KwlReturnObject pAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestMap, purchasePrice, productCurrencyId, transactionDate, 0);
                        purchasePriceInBase= (Double)pAmt.getEntityList().get(0);
                    
                        KwlReturnObject sAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestMap, salesPrice, productCurrencyId, transactionDate, 0);
                        salesPriceInBase= (Double)sAmt.getEntityList().get(0);
                    }
                    obj.put("purchaseprice", authHandler.round(purchasePriceInBase, companyId));
                    obj.put("assetSaleGL", (product.getSellAssetGLAccount() != null) ? product.getSellAssetGLAccount().getID() : "");
                    obj.put("writeoffassetaccount", (product.getWriteOffAssetAccount()!= null) ? product.getWriteOffAssetAccount().getName() : "");
                    obj.put("sellAssetGLAccountID", (product.getSellAssetGLAccount() != null) ? product.getSellAssetGLAccount().getName() : "");
                    obj.put("depreciationGLAccount", (product.getDepreciationGLAccount() != null) ? product.getDepreciationGLAccount().getID() : "");
                    obj.put("depreciationGLAccountID", (product.getDepreciationGLAccount() != null) ? product.getDepreciationGLAccount().getAccountName() : "");
                    obj.put("depreciationProvisionGLAccount", (product.getDepreciationProvisionGLAccount() != null) ? product.getDepreciationProvisionGLAccount().getID() : "");
                    obj.put("depreciationProvisionGLAccountID", (product.getDepreciationProvisionGLAccount() != null) ? product.getDepreciationProvisionGLAccount().getName() : "");
                    obj.put("depreciationRate",authHandler.formattedAmount(product.getDepreciationRate(),companyId));
                    String depreciationMethod = "";
                    if(product.getDepreciationMethod() != 0){
                        if(product.getDepreciationMethod() ==1){
                            depreciationMethod = "Straight Line Depreciation";
                        } else if(product.getDepreciationMethod() ==2){
                            depreciationMethod = "Double Decline Depreciation";
                        }else if(product.getDepreciationMethod() ==3){
                            depreciationMethod = "Non Depreciable";
                        }else if(product.getDepreciationMethod() ==4){
                            depreciationMethod = "Written Down Value";
                        }
                       obj.put("depreciationMethod", depreciationMethod); 
                    }
                    obj.put("assetControllingAccountId", (product.getPurchaseAccount() != null) ? product.getPurchaseAccount().getAccountName() : "");
                    obj.put("saleprice", authHandler.round(salesPriceInBase, companyId));
                    obj.put("salespriceinpricecurrency", row[4] == null ? 0 : row[4]);
                    double availableQuantity  = 0;
                    if (product.isblockLooseSell()) {
                        KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(product.getID(), product.getUnitOfMeasure().getID());
                        availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                        if(product.getProducttype() != null && (product.getProducttype().getName().equalsIgnoreCase("Service")) && exportPDFCSV){
                            obj.put("quantity", "N/A");
                        } else {
                            obj.put("quantity",authHandler.formattedQuantity(availableQuantity,companyId));
                        }
                    } else {
                        if(product.getProducttype() != null && (product.getProducttype().getName().equalsIgnoreCase("Service")) && exportPDFCSV){
                            obj.put("quantity", "N/A");
                        } else {
                            availableQuantity = row[5] == null ? 0 : (Double) row[5];
                            obj.put("quantity",authHandler.formattedQuantity((availableQuantity),companyId));
                        }
                    }
                    if(product.getProducttype() != null && (product.getProducttype().getName().equalsIgnoreCase("Service")) && exportPDFCSV){
                        obj.put("leasedQuantity", "N/A");
                    } else {
                        obj.put("leasedQuantity", (row[14] == null ? 0 : row[14]));
                    }
                    if(product.getProducttype() != null && (product.getProducttype().getName().equalsIgnoreCase("Service")) && exportPDFCSV){
                        obj.put("consignquantity", "N/A");
                    } else {
                        obj.put("consignquantity", (row[15] == null ? 0 : row[15]));
                    }
                    if(product.getProducttype() != null && (product.getProducttype().getName().equalsIgnoreCase("Service")) && exportPDFCSV){
                        obj.put("venconsignquantity", "N/A");
                    } else {
                        obj.put("venconsignquantity", (row[16] == null ? 0 : row[16]));
                    }
                    if(product.getProducttype() != null && (product.getProducttype().getName().equalsIgnoreCase("Service")) && exportPDFCSV){
                        obj.put("initialquantity", "N/A");
                    } else {
                        obj.put("initialquantity", (row[6] == null ? 0 : row[6]));
                    }
                    obj.put("initialprice", (row[7] == null ? 0 : row[7]));
                    obj.put("salespricedatewise", (row[9] == null ? 0 : row[9]));
                    obj.put("purchasepricedatewise", (row[10] == null ? 0 : row[10]));
                    obj.put("initialsalesprice", (row[11] == null ? 0 : row[11]));
                    double lockQuantity= 0;
//                    if((row[13] == null || (Double)row[13]==0) && (product.getProducttype() != null && product.getProducttype().getName().equalsIgnoreCase("Service")) && exportPDFCSV){
//                        obj.put("lockquantity", "");
//                    }
                    if(product.getProducttype() != null && (product.getProducttype().getName().equalsIgnoreCase("Service")) && exportPDFCSV){
                        obj.put("lockquantity", "N/A");
                    } else {
                        if(blkQuantityList.containsKey(product.getID())){
                            obj.put("lockquantity", blkQuantityList.get(product.getID()));
                        }
                        else {
                            lockQuantity = row[13] == null ? 0 : (Double) row[13];
                            obj.put("lockquantity",authHandler.formattedQuantity((lockQuantity),companyId));
                        }
                    }
                    if(product.getProducttype() != null && (product.getProducttype().getName().equalsIgnoreCase("Service")) && exportPDFCSV){
                        obj.put("qaquantity", "N/A");
                        obj.put("repairquantity", "N/A");
                    }
                    double balanceQuantity = availableQuantity - (lockQuantity);
                    if(isExport && (product.getProducttype().getID().equalsIgnoreCase(Constants.SERVICE) || product.getProducttype().getID().equalsIgnoreCase(Constants.NON_INVENTORY_PART))){
                        obj.put("balancequantity", "N/A");
                    } else {
                        obj.put("balancequantity", authHandler.formattedQuantity((balanceQuantity),companyId)); 
                    }
                    obj.put("productcategories", (row[17] == null ? "" : row[17]));
//                obj.put("ccountinterval", pcObject != null ? pcObject.getCountInterval() : "");
//                obj.put("ccounttolerance", pcObject != null ? pcObject.getTolerance() : "");
                    obj.put("productweight", product != null ? (Double) product.getProductweight() : "");
                    obj.put("netproductweight", product != null ? (Double) (product.getProductweight() * availableQuantity) : "");
                    obj.put("productweightperstockuom",  product.getProductWeightPerStockUom());
                    obj.put("productweightincludingpakagingperstockuom", product.getProductWeightIncludingPakagingPerStockUom());
                    obj.put("productvolumeperstockuom",product.getProductVolumePerStockUom());
                    obj.put("productvolumeincludingpakagingperstockuom",product.getProductVolumeIncludingPakagingPerStockUom());
                    if (isForProductQuantityDetailsReport) {
                        boolean isFromQuantityDetailsReport = false;
                        if (requestMap.containsKey("isFromQuantityDetailsReport")) {
                            isFromQuantityDetailsReport = (Boolean) requestMap.get("isFromQuantityDetailsReport");//This flag is true from "Product quantity details" and false from inventory call
                        }

                        if (isFromQuantityDetailsReport) {//If true then run code of Block loose quantity details.

                            for (int i = 0; i < selectedUOMsArray.length; i++) {
                                // get quantity in this uom
                                String selectedUOM = selectedUOMsArray[i];
                                KwlReturnObject uomresult = accProductObj.getObject(UnitOfMeasure.class.getName(), selectedUOM);
                                UnitOfMeasure unitOfMeasure = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                if (unitOfMeasure != null) {
                                    KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(product.getID(), selectedUOM);
                                    double availQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                                    obj.put(selectedUOM, availQuantity);
                                }

                            }

                        } else {
                            double newQuantity = 0.0;
                            for (int i = 0; i < selectedUOMsArray.length; i++) {
                                // get quantity in this uom
                                String selectedUOM = selectedUOMsArray[i];
                                KwlReturnObject uomresult = accProductObj.getObject(UnitOfMeasure.class.getName(), selectedUOM);
                                UnitOfMeasure unitOfMeasure = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                UOMschemaType uOMschemaType = product.getUomSchemaType();
                                if (!StringUtil.isNullObject(uOMschemaType)) {
                                    HashMap<String, Object> requestParamForUOMSchema = new HashMap<>();
                                    requestParamForUOMSchema.put("uomschematypeid", uOMschemaType.getID());
                                    requestParamForUOMSchema.put("currentuomid", selectedUOM);
                                    requestParamForUOMSchema.put("companyid", companyId);
                                    requestParamForUOMSchema.put("carryin", false);
                                    KwlReturnObject res = accProductObj.getProductBaseUOMRate(requestParamForUOMSchema);
                                    List uomSchemaList = res.getEntityList();
                                    Iterator itr = uomSchemaList.iterator();
                                    if (itr.hasNext()) {
                                        UOMSchema uOMSchema = (UOMSchema) itr.next();
                                        if (uOMSchema == null) {
                                            newQuantity = 0.0;
                                        } else {
                                            newQuantity = uOMSchema.getBaseuomrate() != 0 ? authHandler.roundQuantity((obj.optDouble("quantity", 0.0) / uOMSchema.getBaseuomrate()), companyId) : 0.0;
                                        }
                                    }
                                }
                                if (unitOfMeasure != null) {
                                    if (product != null && product.getUnitOfMeasure() != null && unitOfMeasure.getID().equals(product.getUnitOfMeasure().getID())) {
                                        obj.put(selectedUOM, (obj.optDouble("quantity", 0.0)));
                                    } else {
                                        obj.put(selectedUOM, (newQuantity));
                                        newQuantity = 0.0;
                                    }
                                }

                            }
                        }
                    }
//
//               //INV_ACC_MERGE

                    //General Tab extra added fields
                    obj.put("barcodefield",product.getBarcodefield());
                    obj.put("barcode", (product.getBarcode() != null) ? product.getBarcode() : "");
                    obj.put("additionaldescription", (product.getAdditionalDesc() != null) ? product.getAdditionalDesc() : "");
                    obj.put("additionaldesc", (product.getAdditionalDesc() != null) ? product.getAdditionalDesc() : "");
                    obj.put("foreigndescription", (product.getDescInForeign() != null) ? product.getDescInForeign() : "");
                    obj.put("itemgroup", (product.getItemGroup() != null) ? product.getItemGroup() : "");
                    obj.put("itempricelist", (product.getPriceList() != null) ? product.getPriceList() : "");
                    obj.put("shippingtype", (product.getShippingType() != null) ? product.getShippingType() : "");
                    if(exportPDFCSV){
                        obj.put("isActiveItem", product.isIsActive()?"Active":"Dormant");
                    } else {
                        obj.put("isActiveItem", product.isIsActive());
                    }
                    obj.put("isKnittingItem", product.isIsKnittingItem());
                    obj.put("itemReusability",product.getItemReusability()!=null ? product.getItemReusability().ordinal() : 0);
                    obj.put("reusabilitycount",authHandler.formattedQuantity(product.getReusabilityCount(),companyId) );
                    obj.put("licensetype",product.getLicenseType()!=null ? product.getLicenseType() : LicenseType.NONE);
                    obj.put("licensecode", product.getLicenseCode() != null ? product.getLicenseCode() : "");
                    obj.put("itemissuecount",  authHandler.formattedQuantity(product.getTotalIssueCount(),companyId));

                    obj.put("customercategory",product.getCustomerCategory() !=null ? product.getCustomerCategory() : "");

                    obj.put("inspectionTemplate", (product.getInspectionTemplate() != null) ? product.getInspectionTemplate().getId() : "");
                    // Purchase Tab fields

                    obj.put("catalogNo", (product.getCatalogNo() != null) ? product.getCatalogNo() : "");

                    if (product.getPurchaseUOM() != null) {
                        obj.put("purchaseuomid", (product.getPurchaseUOM().getID() != null) ? product.getPurchaseUOM().getID() : "");
                        obj.put("purchaseuomname", (product.getPurchaseUOM().getID() != null) ? product.getPurchaseUOM().getNameEmptyforNA() : "");
                    } else {
                        obj.put("purchaseuomid", "");
                    }

                    obj.put("itempurchaseheight", product.getItemPurchaseHeight());
                    obj.put("itempurchasewidth", product.getItemPurchaseWidth());
                    obj.put("itempurchaselength", product.getItemPurchaseLength());
                    obj.put("itempurchasevolume", product.getItemPurchaseVolume());
                    obj.put("purchasemfg", (product.getPurchaseMfg() != null) ? product.getPurchaseMfg() : "");


                    // Sales Tab fields
                if(product.getSalesUOM() != null ){
                    obj.put("salesuomid",(product.getSalesUOM().getID() != null) ? product.getSalesUOM().getID() : "");
                    obj.put("salesuomname",(product.getSalesUOM().getID() != null) ? product.getSalesUOM().getNameEmptyforNA() : "");
                }else{
                    obj.put("salesuomid","");
                    }
                    obj.put("itemsalesheight", product.getItemSalesHeight());
                    obj.put("itemsaleswidth", product.getItemSalesWidth());
                    obj.put("itemsaleslength", product.getItemSalesLength());
                    obj.put("itemsalesvolume", product.getItemSalesVolume());

                    obj.put("alternateproductid", (product.getAlternateProduct() != null) ? product.getAlternateProduct() : "");

                    //  Properties Tab fields

                    obj.put("itemheight", product.getItemHeight());
                    obj.put("itemwidth", product.getItemWidth());
                    obj.put("itemlength", product.getItemLength());
                    obj.put("itemvolume", product.getItemVolume());
                    obj.put("itemcolor", (product.getItemColor() != null) ? product.getItemColor() : "");

                    //  Remarks Tab fields

                    obj.put("additionalfreetext", (product.getAdditionalFreeText() != null) ? product.getAdditionalFreeText() : "");


                    //  Inventory Data Tab fields

                    Packaging packaging = product.getPackaging();
                    if (packaging != null && packaging.getId() != null) {
                        obj.put("casinguomid", (packaging.getCasingUoM() != null) ? packaging.getCasingUoM().getID() : "");
                        obj.put("casinguomvalue", (packaging.getCasingUomValue()));
                        obj.put("inneruomid", (packaging.getInnerUoM() != null) ? packaging.getInnerUoM().getID() : "");
                        obj.put("inneruomvalue", (packaging.getInnerUomValue()));
                        obj.put("stockuomid", (packaging.getStockUoM() != null) ? packaging.getStockUoM().getID() : "");
                        obj.put("stockuomvalue", (packaging.getStockUomValue()));
                        obj.put("packagingId", (packaging.getId() != null) ? packaging.getId() : "");
                        obj.put("packagingValue", (packaging.toString() != null) ? packaging.toString() : "");
                    } else {
                        obj.put("casinguomid", "");
                        obj.put("casinguomvalue", 0);
                        obj.put("inneruomid", "");
                        obj.put("inneruomvalue", 0);
                        obj.put("stockuomid", "");
                        obj.put("stockuomvalue", 1);
                        obj.put("packagingId", "");
                        obj.put("packagingValue", "");
                    }
                    if (product.getValuationMethod() != null) {
                        if (product.getValuationMethod()==product.getValuationMethod().STANDARD) {
                            obj.put("valuationmethod", "LIFO");
                        } else if (product.getValuationMethod()==product.getValuationMethod().AVERAGE) {
                            obj.put("valuationmethod", "Moving Average");
                        } else {
                            obj.put("valuationmethod", "FIFO");
                        }
                    }
                    //obj.put("itemcost", product.getItemCost());

                if(product.getOrderingUOM() != null ){
                    obj.put("orderinguomid",(product.getOrderingUOM().getID() != null) ? product.getOrderingUOM().getID() : "");
                    obj.put("orderinguomname",(product.getOrderingUOM().getID() != null) ? product.getOrderingUOM().getNameEmptyforNA() : "");
                    obj.put("orderinguom",(product.getOrderingUOM().getID() != null) ? product.getOrderingUOM().getNameEmptyforNA() : "");
                }else{
                    obj.put("orderinguomid","");
                    }

                if(product.getTransferUOM() != null ){
                    obj.put("transferuomid",(product.getTransferUOM().getID() != null) ? product.getTransferUOM().getID() : "");
                    obj.put("transferuomname",(product.getTransferUOM().getID() != null) ? product.getTransferUOM().getNameEmptyforNA() : "");
                    obj.put("transferuom",(product.getTransferUOM().getID() != null) ? product.getTransferUOM().getNameEmptyforNA() : "");
                }else{
                    obj.put("transferuomid","");
                    }

                    obj.put("WIPoffset", (product.getWIPOffset() != null) ? product.getWIPOffset() : "");
                    obj.put("Inventoryoffset", (product.getInventoryOffset() != null) ? product.getInventoryOffset() : "");

                Set<Frequency> frequencies = product.getCycleCountFrequencies();
                String fqs = "";
                if (frequencies != null) {
                    boolean first = true;
                    for (Frequency frequency : frequencies) {
                        if (first) {
                            fqs = String.valueOf(frequency.getName());
                            first = false;
                        } else {
                            fqs += "," + String.valueOf(frequency.getName());
                        }
                    }
                }
                obj.put("CCFrequency", fqs);

                    if (isExport) {
                        obj.put("Activate Batch", product.isIsBatchForProduct() ? "T" : "F");
                        obj.put("Activate Serial No", product.isIsSerialForProduct() ? "T" : "F");
                        obj.put("Activate Location", product.isIslocationforproduct() ? "T" : "F");
                        obj.put("Activate Warehouse", product.isIswarehouseforproduct() ? "T" : "F");
                        obj.put("Activate Row", product.isIsrowforproduct() ? "T" : "F");
                        obj.put("Activate Rack", product.isIsrackforproduct() ? "T" : "F");
                        obj.put("Activate Bin", product.isIsbinforproduct() ? "T" : "F");
                        obj.put("purchaseaccountname", product.getPurchaseAccount() == null ? "" : product.getPurchaseAccount().getName());
                        obj.put("purchaseretaccountname", product.getPurchaseReturnAccount() == null ? "" : product.getPurchaseReturnAccount().getName());
                        obj.put("salesaccountname", product.getSalesAccount() == null ? "" : product.getSalesAccount().getName());
                        obj.put("salesretaccountname", product.getSalesReturnAccount() == null ? "" : product.getSalesReturnAccount().getName());
                        obj.put("salesretaccountname", product.getSalesReturnAccount() == null ? "" : product.getSalesReturnAccount().getName());
                        obj.put("cogsaccountid", product.getCostOfGoodsSoldAccount() == null ? "" : product.getCostOfGoodsSoldAccount().getName());
                        obj.put("inventoryaccountid", product.getInventoryAccount() == null ? "" : product.getInventoryAccount().getName());
                        obj.put("stockadjustmentaccountid", product.getStockAdjustmentAccount() == null ? "" : product.getStockAdjustmentAccount().getName());
                        /*
                           ERP-35335 removes the tag of description used in html editor
                        */
                        obj.put("desc", StringUtil.isNullOrEmpty(product.getDescription()) ? "" : StringUtil.DecodeText(StringUtil.replaceFullHTML(product.getDescription().replaceAll("<br>", "\n").replaceAll("&amp;","&"))));
                        if (packaging != null && packaging.getId() != null) {
                            obj.put("casinguom", (packaging.getCasingUoM() != null) ? packaging.getCasingUoM().getNameEmptyforNA() : "");
                            obj.put("casinguom_value", (packaging.getCasingUomValue()));
                            obj.put("inneruom", (packaging.getInnerUoM() != null) ? packaging.getInnerUoM().getNameEmptyforNA() : "");
                            obj.put("inneruom_value", (packaging.getInnerUomValue()));
                            obj.put("stockuom_value", packaging.getStockUomValue());
                        } else {
                            obj.put("stockuom_value", 1);
                        }
                        obj.put("currencyName", baseCurrencyName);
                        Set<LandingCostCategory> landingCostCategories = product.getLccategoryid();
                        if (landingCostCategories != null && !landingCostCategories.isEmpty()) {
                            String landedCostCategories = "";
                            for (LandingCostCategory landingCostCategory : landingCostCategories) {
                                landedCostCategories += landingCostCategory.getLccName()+ ",";
                            }
                            obj.put("landingcostcategory", !StringUtil.isNullOrEmpty(landedCostCategories) ? landedCostCategories.substring(0, landedCostCategories.length() - 1) : "");
                        }
                    }
                    
                    if (isCustomColumnExport) {
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        KwlReturnObject idcustresult = accountingHandlerDAOobj.getObject(AccProductCustomData.class.getName(), product.getID());
                        if (idcustresult.getEntityList().size() > 0) {
                            AccProductCustomData jeCustom = (AccProductCustomData) idcustresult.getEntityList().get(0);
                            AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap, variableMap);
                            for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                String coldata = varEntry.getValue().toString();
                                if (customFieldMap.containsKey(varEntry.getKey())) {
                                    String value = "";
                                    String Ids[] = coldata.split(",");
                                    for (int i = 0; i < Ids.length; i++) {
                                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
                                        FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                        if (fieldComboData != null) {
                                            if ((fieldComboData.getField().getFieldtype() == 12 || fieldComboData.getField().getFieldtype() == 7) && !exportPDFCSV) {
                                                value += Ids[i] != null ? Ids[i] + "," : ",";
                                            } else {
                                                value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                            }
                                        }
                                    }
                                    if (!StringUtil.isNullOrEmpty(value)) {
                                        value = value.substring(0, value.length() - 1);
                                    }
                                    obj.put(varEntry.getKey(), value);
                                } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
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
                    }
//                // End new properties
//                
                    //  obj.put("createdon", (row[12]==null?"":sdf.format(row[12])));
                    //            jArr.put(obj);
                    if (nonSaleInventory && obj.get("producttype").equals(producttype.Inventory_Non_Sales)) {
                        // Do Nothing
                    } else {
                        jArr.put(obj);
                    }
                } catch (Exception ex) {
                    throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
                }
            }
            replaceFieldMap = null;
            customFieldMap = null;
            customDateFieldMap = null;
            FieldMap = null;
            fieldrequestParams = null;
        return jArr;
    }
 
    public static JSONArray getProductsJsonForDashBoradUpdates(HashMap<String, Object> request, List list, accProductDAO accProductObj, accAccountDAO accAccountDAOobj, AccountingHandlerDAO accountingHandlerDAOobj, accCurrencyDAO accCurrencyDAOobj, boolean isCustomColumnExport) throws JSONException, ServiceException, ParseException {

        JSONArray jArr = new JSONArray();
        String companyId = (String) request.get("id");
        boolean exportPDFCSV = false;
        if (request.get("exportPDFCSV") != null && !StringUtil.isNullOrEmpty(request.get("exportPDFCSV").toString())) {
            exportPDFCSV = Boolean.parseBoolean(request.get("exportPDFCSV").toString());
        }
        JSONObject requestJSONObj = new JSONObject();
        requestJSONObj.put(Constants.companyKey, companyId);
        KwlReturnObject cap = null;
        cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
        Calendar tempDate = Calendar.getInstance();
        tempDate.setTime(preferences.getBookBeginningFrom());
        Producttype producttype = new Producttype();
        String productid = (String) request.get("productid");
        int mode = 0;
        if (request.get("get") != null && !StringUtil.isNullOrEmpty((String) request.get("get"))) {
            mode = Integer.parseInt((String) request.get("get"));
        }
        boolean isForProductQuantityDetailsReport = false;
        if (!StringUtil.isNullOrEmpty(request.get("isForProductQuantityDetailsReport").toString())) {
            isForProductQuantityDetailsReport = Boolean.parseBoolean(request.get("isForProductQuantityDetailsReport").toString());
        }
        String[] selectedUOMsArray = null;
        if (isForProductQuantityDetailsReport) {
            String selectedUOMs = (String) request.get("selectedUOMs");
            selectedUOMsArray = selectedUOMs.split(",");
        }
        Boolean nonSaleInventory = (Boolean) request.get("loadInventory");
        for (Object object : list) {
            try {
                Object[] row = (Object[]) object;
                Product product = (Product) row[0];
                Product parentProduct = product.getParent();
                if (product.getID().equals(productid)) {
                    continue;
                }
                Date creationDate = new Date(product.getCreatedon());
                Calendar cal = Calendar.getInstance(); // locale-specific
                cal.setTime(creationDate);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long time = cal.getTimeInMillis();

                JSONObject obj = new JSONObject();

                obj.put("productid", mode == 1110 ? product.getProductid() : product.getID());
                obj.put("productname", product.getName());
                obj.put("productcodeid", !StringUtil.isNullOrEmpty(product.getProductid()) ? product.getProductid() : "");
                obj.put("description", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));
                obj.put("desc", StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription());    //ERP-27500
                obj.put("tariffname", product.getTariffName());
                obj.put("hsncode", product.getHSNCode());
                obj.put("reportinguom", product.getReportinguom());
                UnitOfMeasure uom = product.getUnitOfMeasure();
                obj.put("uomid", uom == null ? "" : uom.getID());
                obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
                obj.put("reorderlevel", product.getReorderLevel());
                if (product.getReorderLevel() == 0 && (product.getProducttype() != null && product.getProducttype().getName().equalsIgnoreCase("Service")) && exportPDFCSV) {
                    obj.put("reorderlevel", "");
                } else {
                    obj.put("reorderlevel", product.getReorderLevel());
                }
                if (isForProductQuantityDetailsReport) {
                    for (int i = 0; i < selectedUOMsArray.length; i++) {
                        // get quantity in this uom
                        String selectedUOM = selectedUOMsArray[i];
                        KwlReturnObject uomresult = accProductObj.getObject(UnitOfMeasure.class.getName(), selectedUOM);
                        UnitOfMeasure unitOfMeasure = (UnitOfMeasure) uomresult.getEntityList().get(0);
                        if (unitOfMeasure != null) {
                            KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(product.getID(), selectedUOM);
                            double availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                            obj.put(selectedUOM, authHandler.formattedQuantity(availableQuantity, companyId));
                        }

                    }
                }
                double availableQuantity = 0;
                if (product.isblockLooseSell()) {
                    KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(product.getID(), product.getUnitOfMeasure().getID());
                    availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                    if (availableQuantity == 0 && (product.getProducttype() != null && product.getProducttype().getName().equalsIgnoreCase("Service")) && exportPDFCSV) {
                        obj.put("quantity", "");
                    } else {
                        obj.put("quantity", (availableQuantity));
                    }
                } else {
                    if ((row[5] == null || (Double) row[5] == 0) && (product.getProducttype() != null && product.getProducttype().getName().equalsIgnoreCase("Service")) && exportPDFCSV) {
                        obj.put("quantity", "");
                    } else {
                        availableQuantity = row[5] == null ? 0 : (Double) row[5];
                        obj.put("quantity", (availableQuantity));
                    }
                }
                if (nonSaleInventory && obj.get("producttype").equals(producttype.Inventory_Non_Sales)) {
                    // Do Nothing
                } else {
                    jArr.put(obj);
                }
            } catch (UnsupportedEncodingException ex) {
                throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
            }
        }
        return jArr;
    }
    /* --------- The below function used for only product sync with CRM (ERP-18753)-----------*/
    
    public static JSONArray getSyncProductsJson(HttpServletRequest request, List list, accProductDAO accProductObj, accAccountDAO accAccountDAOobj, AccountingHandlerDAO accountingHandlerDAOobj, accCurrencyDAO accCurrencyDAOobj, boolean isCustomColumnExport) throws JSONException, ServiceException {
        Iterator itr = list.iterator();

        //If you are changing anything in this function then make same changes in the getProductsJson function which is available in accProductController file
        JSONArray jArr = new JSONArray();
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            KwlReturnObject cap = null;
            try {
                cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            } catch (SessionExpiredException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
            /**
             * Get Extra Company pref. checks 
            */
            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            Calendar tempDate = Calendar.getInstance();
            tempDate.setTime(preferences.getBookBeginningFrom());
            Date transactionDate = tempDate.getTime();
            String date=df.format(transactionDate);
            try {
                transactionDate=df.parse(date);
            } catch (ParseException ex) {
                transactionDate = tempDate.getTime();
                Logger.getLogger(productHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            Producttype producttype = new Producttype();
            String productid = request.getParameter("productid");
            Boolean nonSaleInventory = Boolean.parseBoolean((String) request.getParameter("loadInventory"));
            
            //To send product custom data
            Date currentDate = authHandler.getDateOnlyFormat(request).parse(authHandler.getDateOnlyFormat(request).format(new Date()));
            HashMap<String, Object> requestParamsCustom = new HashMap<String, Object>();
            requestParamsCustom.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            requestParamsCustom.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Product_Master_ModuleId));
            requestParamsCustom.put("isActivated", 1);
            requestParamsCustom.put("order_by", Arrays.asList("sequence"));
            requestParamsCustom.put("order_type", Arrays.asList("asc"));
            KwlReturnObject result = accAccountDAOobj.getFieldParams(requestParamsCustom);
            List lst = result.getEntityList();
            
            while (itr.hasNext()) {
                try {
                    Object[] row = (Object[]) itr.next();
                    Product product = (Product) row[0];
                    Product parentProduct = product.getParent();
                    /**
                     * !product.isIsActive() used to restrict deactivated product
                     */
                    if (product.getID().equals(productid) || !product.isIsActive()) {
                        continue;
                    }

                    //======refer ticket ERP-11075 & ERP-11606============
                    Date creationDate = new Date(product.getCreatedon());
                    Calendar cal = Calendar.getInstance(); // locale-specific
                    cal.setTime(creationDate);
                    cal.setTimeZone(TimeZone.getTimeZone("GMT" + sessionHandlerImpl.getTimeZoneDifference(request)));        // need to add Application TimeZone
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    long time = cal.getTimeInMillis();

                    JSONObject obj = new JSONObject();
                    obj.put("createdon", time);
                    obj.put("id", product.getID());
                    obj.put("currencyid", product.getCurrency() != null ? product.getCurrency().getCurrencyID() : product.getCompany().getCurrency().getCurrencyID());
                    obj.put("productname", product.getName());
                    obj.put("pid", !StringUtil.isNullOrEmpty(product.getProductid()) ? product.getProductid() : "");
                    obj.put("description", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));
                    obj.put("desc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));
//                   
                    obj.put("vendor", (product.getVendor() != null ? product.getVendor().getID() : ""));
                    obj.put("vendornameid", (product.getVendor() != null ? product.getVendor().getName() : ""));
                    obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
                    obj.put("vendorphoneno", (product.getVendor() != null ? product.getVendor().getContactNumber() : ""));
                    obj.put("vendoremail", (product.getVendor() != null ? product.getVendor().getEmail() : ""));
                    if (product.isAsset()) {   //For Fixed Asset Group, type will be "Asset"
                        obj.put("type", "Asset");
                    } else {
                        obj.put("type", (product.getProducttype() != null ? product.getProducttype().getName() : ""));
                    }
                    if (product.getWarrantyperiod() == 0) {
                        obj.put("warranty", "N/A");
                    } else {
                        obj.put("warranty", product.getWarrantyperiod());
                    }
                    if (product.getWarrantyperiodsal() == 0) {
                        obj.put("warrantysal", "N/A");
                    } else {
                        obj.put("warrantysal", product.getWarrantyperiodsal());
                    }
//                    
                    double purchasePriceInBase = 0;
                    double salesPriceInBase = 0;

                    Double purchasePrice = row[3] != null ? (Double) row[3] : 0;
                    Double salesPrice = row[4] != null ? (Double) row[4] : 0;
                    String productCurrencyId = product.getCompany().getCurrency().getCurrencyID();
                    if (product.getCurrency() != null) {
                        productCurrencyId = product.getCurrency().getCurrencyID();
                    }

                    KwlReturnObject pAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, purchasePrice, productCurrencyId, transactionDate, 0);
                    purchasePriceInBase = (Double) pAmt.getEntityList().get(0);

                    KwlReturnObject sAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, salesPrice, productCurrencyId, transactionDate, 0);
                    salesPriceInBase = (Double) sAmt.getEntityList().get(0);
                    obj.put("currentpurchaseprice", authHandler.round(purchasePriceInBase, companyId));
                    obj.put("assetSaleGL", (product.getSellAssetGLAccount() != null) ? product.getSellAssetGLAccount().getID() : "");
                    obj.put("depreciationGLAccount", (product.getDepreciationGLAccount() != null) ? product.getDepreciationGLAccount().getID() : "");
                    obj.put("depreciationProvisionGLAccount", (product.getDepreciationProvisionGLAccount() != null) ? product.getDepreciationProvisionGLAccount().getID() : "");
                    obj.put("depreciationRate", product.getDepreciationRate());
                    obj.put("saleprice", authHandler.round(salesPriceInBase, companyId));
                    obj.put("salespriceinpricecurrency", row[4] == null ? 0 : row[4]);
                    
                    //====extra fields to be send to map the columns data====
                    
                    obj.put("purchaseprice", (row[7] == null ? 0 : row[7]));
                    
                    obj.put("uomname", (product.getPackaging() != null && product.getPackaging().getStockUoM() != null) ? product.getPackaging().getStockUoM().getNameEmptyforNA() : product.getUnitOfMeasure() !=null ? product.getUnitOfMeasure().getNameEmptyforNA():"");
                    obj.put("reorderlevel", product.getReorderLevel());
                    obj.put("reorderquantity", product.getReorderQuantity());
                    obj.put("leadtime", product.getLeadTimeInDays());
                    obj.put("parentid", product.getParent()!=null ? product.getParent().getProductName(): "");
                    obj.put("salesaccountname", product.getSalesAccount()!=null ? product.getSalesAccount().getAccountName(): "");
                    obj.put("salesretaccountname", product.getSalesReturnAccount()!=null ? product.getSalesReturnAccount().getAccountName(): "");
                    obj.put("purchaseaccountname", product.getPurchaseAccount()!=null ? product.getPurchaseAccount().getAccountName(): "");
                    obj.put("purchaseretaccountname", product.getPurchaseReturnAccount()!=null ? product.getPurchaseReturnAccount().getAccountName(): "");
                    obj.put("quantity", product.getAvailableQuantity());
                    obj.put("locationName", product.getLocation()!=null ? product.getLocation().getName(): "");
                    obj.put("warehouseName", product.getWarehouse()!=null ? product.getWarehouse().getName(): "");
                    obj.put("currencyName", product.getCurrency()!=null ? product.getCurrency().getCurrencyID() : "");
                    obj.put("purchaseuom", product.getPurchaseUOM()!=null ? product.getPurchaseUOM().getNameEmptyforNA(): "");
                    obj.put("salesuom", product.getSalesUOM()!=null ? product.getSalesUOM().getNameEmptyforNA(): "");
                    obj.put("casinguom", (product.getPackaging()!=null && product.getPackaging().getCasingUoM()!=null) ? product.getPackaging().getCasingUoM().getNameEmptyforNA() : "");
                    obj.put("inneruom", (product.getPackaging()!=null && product.getPackaging().getInnerUoM()!=null) ? product.getPackaging().getInnerUoM().getNameEmptyforNA() : "");
                    obj.put("casinguom_value", product.getPackaging()!=null ? product.getPackaging().getCasingUomValue() : 0);
                    obj.put("inneruom_value", product.getPackaging()!=null ? product.getPackaging().getInnerUomValue() : 0);
                    obj.put("stockuom_value", product.getPackaging()!=null ? product.getPackaging().getStockUomValue() : 0);
                    obj.put("supplier", product.getSupplier());
                    obj.put("coilcraft", product.getCoilcraft());
                    obj.put("interplant", product.getInterplant());
                    obj.put("wipoffset", product.getWIPOffset());
                    obj.put("inventoryoffset", product.getInventoryOffset());
                    obj.put("hscode", product.getHSCode());
                    obj.put("additionalfreetext", product.getAdditionalFreeText());
                    obj.put("itemcolor", product.getItemColor());
                    obj.put("alternateproduct", product.getAlternateProduct());
                    obj.put("purchasemfg", product.getPurchaseMfg());
                    obj.put("catalogno", product.getCatalogNo());
                    obj.put("barcode", product.getBarcode());
                    obj.put("additionaldesc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getAdditionalDesc()) ? "" : product.getAdditionalDesc(), "UTF-8"));
                    obj.put("descinforeign", product.getDescInForeign());
                    obj.put("licensecode", product.getLicenseCode());
                    obj.put("itemgroup", product.getItemGroup());
                    obj.put("pricelist", product.getPriceList());
                    obj.put("shippingtype", product.getShippingType());
                    obj.put("itemsalesvolume", product.getItemSalesVolume());
                    obj.put("productweight", product.getProductweight());
                    obj.put("productweightperstockuom",  product.getProductWeightPerStockUom());
                    obj.put("productweightincludingpakagingperstockuom", product.getProductWeightIncludingPakagingPerStockUom());
                    obj.put("productvolumeperstockuom",product.getProductVolumePerStockUom());
                    obj.put("productvolumeincludingpakagingperstockuom",product.getProductVolumeIncludingPakagingPerStockUom());
                    obj.put("itemsaleswidth", product.getItemSalesWidth());
                    obj.put("itemsalesheight", product.getItemSalesHeight());
                    obj.put("itemwidth", product.getItemWidth());
                    obj.put("itemvolume", product.getItemVolume());
                    obj.put("itempurchasewidth", product.getItemPurchaseWidth());
                    obj.put("itempurchaselength", product.getItemPurchaseLength());
                    obj.put("qaleadtimeindays", product.getQALeadTimeInDays());
                    obj.put("reusabilitycount", product.getReusabilityCount());
                    obj.put("orderinguom", product.getOrderingUOM()!=null ? product.getOrderingUOM().getNameEmptyforNA(): "");
                    obj.put("transferuom", product.getTransferUOM()!=null ? product.getTransferUOM().getNameEmptyforNA(): "");
                    obj.put("itempurchaseheight", product.getItemPurchaseHeight());
                    obj.put("itempurchasevolume", product.getItemPurchaseVolume());
                    obj.put("itemsaleslength", product.getItemSalesLength());
                    obj.put("itemlength", product.getItemLength());
                    obj.put("itemheight", product.getItemHeight());
                    obj.put("asofdate", product.getAsOfDate()!=null ? product.getAsOfDate().getTime() : "");
                    /**
                    * Get Product level Terms if subdomain have line level term check ON and Integration with CRM from ERP
                    * Get Product terms mapped with product while creating 
                    * Only Sales side terms send to CRM side.
                    *
                    */
                    if (extrareferences != null && extrareferences.getLineLevelTermFlag() == 1) {
                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
                        hashMap.put(IndiaComplianceConstants.SALES_OR_PURCHASE_FLAG, true);//For Sales
                        hashMap.put(Constants.productid, product.getID());
                        JSONArray ProductTermSalesArr = accProductObj.getProductTermsJsonArray(hashMap);
                        obj.put("ProductTermSalesMapp", ProductTermSalesArr);
                    }
                    
                    //Send shared Documents related data to CRM
                    Map<String, Object> docsParams = new HashMap<String, Object>();
                    String recordID = product.getID();
                    docsParams.put("recid", recordID);
                    docsParams.put(Constants.companyKey, product.getCompany().getCompanyID());
                    docsParams.put("module", "30");
                    JSONArray docsjson = accProductObj.getProductDocumentsArray(docsParams);
                    obj.put("shareddocs", docsjson);                    
                    //=======================================================
                    
                    Map<String, Object> customParams = new HashMap<String, Object>();
                    customParams.put("fieldList", lst);
                    customParams.put("companyid", companyId);
                    customParams.put("currentDate", currentDate);
                    customParams.put("productObj", product);
                    customParams.put("accountingHandlerDAOobj", accountingHandlerDAOobj);
                    customParams.put("accProductObj", accProductObj);

                    JSONArray customJobj = getProductCutomDataJson(customParams);
                    obj.put("customdata", customJobj);
                    
                    if (nonSaleInventory && obj.get("producttype").equals(producttype.Inventory_Non_Sales)) {
                        // Do Nothing
                    } else {
                        jArr.put(obj);
                    }
                } catch (UnsupportedEncodingException ex) {
                    throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
                }
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(productHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    public static JSONArray getSyncProductsJson(JSONObject inputParamJobj, List list, accProductDAO accProductObj, accAccountDAO accAccountDAOobj, AccountingHandlerDAO accountingHandlerDAOobj, accCurrencyDAO accCurrencyDAOobj, boolean isCustomColumnExport) throws JSONException, ServiceException {
        Iterator itr = list.iterator();
        
        //If you are changing anything in this function then make same changes in the getProductsJson function which is available in accProductController file
        JSONArray jArr = new JSONArray();
        try {
            String companyId = inputParamJobj.getString("companyid");
            DateFormat df = authHandler.getDateFormatter(inputParamJobj);
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(inputParamJobj);
            KwlReturnObject cap = null;
            try {
                cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
            } catch (Exception ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
            /**
             * Get Extra Company pref. checks 
            */
            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            Calendar tempDate = Calendar.getInstance();
            tempDate.setTime(preferences.getBookBeginningFrom());
            Date transactionDate = tempDate.getTime();
            String date=df.format(transactionDate);
            try {
                transactionDate=df.parse(date);
            } catch (ParseException ex) {
                transactionDate = tempDate.getTime();
                Logger.getLogger(productHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            Producttype producttype = new Producttype();
            String productid = null;
            Boolean nonSaleInventory =false;
            
            //To send product custom data
            Date currentDate = authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(new Date()));
            HashMap<String, Object> requestParamsCustom = new HashMap<String, Object>();
            requestParamsCustom.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            requestParamsCustom.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Product_Master_ModuleId));
            requestParamsCustom.put("isActivated", 1);
            requestParamsCustom.put("order_by", Arrays.asList("sequence"));
            requestParamsCustom.put("order_type", Arrays.asList("asc"));
            KwlReturnObject result = accAccountDAOobj.getFieldParams(requestParamsCustom);
            List lst = result.getEntityList();
            
            while (itr.hasNext()) {
                try {
                    Object[] row = (Object[]) itr.next();
                    Product product = (Product) row[0];
                    Product parentProduct = product.getParent();
                    /**
                     * !product.isIsActive() used to restrict deactivated product
                     */
                    if (product.getID().equals(productid) || !product.isIsActive()) {
                        continue;
                    }

                    //======refer ticket ERP-11075 & ERP-11606============
                    Date creationDate = new Date(product.getCreatedon());
                    Calendar cal = Calendar.getInstance(); // locale-specific
                    cal.setTime(creationDate);
                    cal.setTimeZone(TimeZone.getTimeZone("GMT" +inputParamJobj.getString("userId")));        // need to add Application TimeZone
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    long time = cal.getTimeInMillis();

                    JSONObject obj = new JSONObject();
                    obj.put("createdon", time);
                    obj.put("id", product.getID());
                    obj.put("currencyid", product.getCurrency() != null ? product.getCurrency().getCurrencyID() : product.getCompany().getCurrency().getCurrencyID());
                    obj.put("productname", product.getName());
                    obj.put("pid", !StringUtil.isNullOrEmpty(product.getProductid()) ? product.getProductid() : "");
                    obj.put("description", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));
                    obj.put("desc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));
//                   
                    obj.put("vendor", (product.getVendor() != null ? product.getVendor().getID() : ""));
                    obj.put("vendornameid", (product.getVendor() != null ? product.getVendor().getName() : ""));
                    obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
                    obj.put("vendorphoneno", (product.getVendor() != null ? product.getVendor().getContactNumber() : ""));
                    obj.put("vendoremail", (product.getVendor() != null ? product.getVendor().getEmail() : ""));
                    if (product.isAsset()) {   //For Fixed Asset Group, type will be "Asset"
                        obj.put("type", "Asset");
                    } else {
                        obj.put("type", (product.getProducttype() != null ? product.getProducttype().getName() : ""));
                    }
                    if (product.getWarrantyperiod() == 0) {
                        obj.put("warranty", "N/A");
                    } else {
                        obj.put("warranty", product.getWarrantyperiod());
                    }
                    if (product.getWarrantyperiodsal() == 0) {
                        obj.put("warrantysal", "N/A");
                    } else {
                        obj.put("warrantysal", product.getWarrantyperiodsal());
                    }
//                    
                    double purchasePriceInBase = 0;
                    double salesPriceInBase = 0;

                    Double purchasePrice = row[3] != null ? (Double) row[3] : 0;
                    Double salesPrice = row[4] != null ? (Double) row[4] : 0;
                    String productCurrencyId = product.getCompany().getCurrency().getCurrencyID();
                    if (product.getCurrency() != null) {
                        productCurrencyId = product.getCurrency().getCurrencyID();
                    }

                    KwlReturnObject pAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, purchasePrice, productCurrencyId, transactionDate, 0);
                    purchasePriceInBase = (Double) pAmt.getEntityList().get(0);

                    KwlReturnObject sAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, salesPrice, productCurrencyId, transactionDate, 0);
                    salesPriceInBase = (Double) sAmt.getEntityList().get(0);
                    obj.put("currentpurchaseprice", authHandler.round(purchasePriceInBase, companyId));
                    obj.put("assetSaleGL", (product.getSellAssetGLAccount() != null) ? product.getSellAssetGLAccount().getID() : "");
                    obj.put("depreciationGLAccount", (product.getDepreciationGLAccount() != null) ? product.getDepreciationGLAccount().getID() : "");
                    obj.put("depreciationProvisionGLAccount", (product.getDepreciationProvisionGLAccount() != null) ? product.getDepreciationProvisionGLAccount().getID() : "");
                    obj.put("depreciationRate", product.getDepreciationRate());
                    obj.put("saleprice", authHandler.round(salesPriceInBase, companyId));
                    obj.put("salespriceinpricecurrency", row[4] == null ? 0 : row[4]);
                    
                    //====extra fields to be send to map the columns data====
                    
                    obj.put("purchaseprice", (row[7] == null ? 0 : row[7]));
                    
                    obj.put("uomname", (product.getPackaging() != null && product.getPackaging().getStockUoM() != null) ? product.getPackaging().getStockUoM().getNameEmptyforNA() : product.getUnitOfMeasure() !=null ? product.getUnitOfMeasure().getNameEmptyforNA():"");
                    obj.put("reorderlevel", product.getReorderLevel());
                    obj.put("reorderquantity", product.getReorderQuantity());
                    obj.put("leadtime", product.getLeadTimeInDays());
                    obj.put("parentid", product.getParent()!=null ? product.getParent().getProductName(): "");
                    obj.put("salesaccountname", product.getSalesAccount()!=null ? product.getSalesAccount().getAccountName(): "");
                    obj.put("salesretaccountname", product.getSalesReturnAccount()!=null ? product.getSalesReturnAccount().getAccountName(): "");
                    obj.put("purchaseaccountname", product.getPurchaseAccount()!=null ? product.getPurchaseAccount().getAccountName(): "");
                    obj.put("purchaseretaccountname", product.getPurchaseReturnAccount()!=null ? product.getPurchaseReturnAccount().getAccountName(): "");
                    obj.put("quantity", product.getAvailableQuantity());
                    obj.put("locationName", product.getLocation()!=null ? product.getLocation().getName(): "");
                    obj.put("warehouseName", product.getWarehouse()!=null ? product.getWarehouse().getName(): "");
                    obj.put("currencyName", product.getCurrency()!=null ? product.getCurrency().getCurrencyID() : "");
                    obj.put("purchaseuom", product.getPurchaseUOM()!=null ? product.getPurchaseUOM().getNameEmptyforNA(): "");
                    obj.put("salesuom", product.getSalesUOM()!=null ? product.getSalesUOM().getNameEmptyforNA(): "");
                    obj.put("casinguom", (product.getPackaging()!=null && product.getPackaging().getCasingUoM()!=null) ? product.getPackaging().getCasingUoM().getNameEmptyforNA() : "");
                    obj.put("inneruom", (product.getPackaging()!=null && product.getPackaging().getInnerUoM()!=null) ? product.getPackaging().getInnerUoM().getNameEmptyforNA() : "");
                    obj.put("casinguom_value", product.getPackaging()!=null ? product.getPackaging().getCasingUomValue() : 0);
                    obj.put("inneruom_value", product.getPackaging()!=null ? product.getPackaging().getInnerUomValue() : 0);
                    obj.put("stockuom_value", product.getPackaging()!=null ? product.getPackaging().getStockUomValue() : 0);
                    obj.put("supplier", product.getSupplier());
                    obj.put("coilcraft", product.getCoilcraft());
                    obj.put("interplant", product.getInterplant());
                    obj.put("wipoffset", product.getWIPOffset());
                    obj.put("inventoryoffset", product.getInventoryOffset());
                    obj.put("hscode", product.getHSCode());
                    obj.put("additionalfreetext", product.getAdditionalFreeText());
                    obj.put("itemcolor", product.getItemColor());
                    obj.put("alternateproduct", product.getAlternateProduct());
                    obj.put("purchasemfg", product.getPurchaseMfg());
                    obj.put("catalogno", product.getCatalogNo());
                    obj.put("barcode", product.getBarcode());
                    obj.put("additionaldesc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getAdditionalDesc()) ? "" : product.getAdditionalDesc(), "UTF-8"));
                    obj.put("descinforeign", product.getDescInForeign());
                    obj.put("licensecode", product.getLicenseCode());
                    obj.put("itemgroup", product.getItemGroup());
                    obj.put("pricelist", product.getPriceList());
                    obj.put("shippingtype", product.getShippingType());
                    obj.put("itemsalesvolume", product.getItemSalesVolume());
                    obj.put("productweight", product.getProductweight());
                    obj.put("productweightperstockuom",  product.getProductWeightPerStockUom());
                    obj.put("productweightincludingpakagingperstockuom", product.getProductWeightIncludingPakagingPerStockUom());
                    obj.put("productvolumeperstockuom",product.getProductVolumePerStockUom());
                    obj.put("productvolumeincludingpakagingperstockuom",product.getProductVolumeIncludingPakagingPerStockUom());
                    obj.put("itemsaleswidth", product.getItemSalesWidth());
                    obj.put("itemsalesheight", product.getItemSalesHeight());
                    obj.put("itemwidth", product.getItemWidth());
                    obj.put("itemvolume", product.getItemVolume());
                    obj.put("itempurchasewidth", product.getItemPurchaseWidth());
                    obj.put("itempurchaselength", product.getItemPurchaseLength());
                    obj.put("qaleadtimeindays", product.getQALeadTimeInDays());
                    obj.put("reusabilitycount", product.getReusabilityCount());
                    obj.put("orderinguom", product.getOrderingUOM()!=null ? product.getOrderingUOM().getNameEmptyforNA(): "");
                    obj.put("transferuom", product.getTransferUOM()!=null ? product.getTransferUOM().getNameEmptyforNA(): "");
                    obj.put("itempurchaseheight", product.getItemPurchaseHeight());
                    obj.put("itempurchasevolume", product.getItemPurchaseVolume());
                    obj.put("itemsaleslength", product.getItemSalesLength());
                    obj.put("itemlength", product.getItemLength());
                    obj.put("itemheight", product.getItemHeight());
                    obj.put("asofdate", product.getAsOfDate()!=null ? product.getAsOfDate().getTime() : "");
                    /**
                    * Get Product level Terms if subdomain have line level term check ON and Integration with CRM from ERP
                    * Get Product terms mapped with product while creating 
                    * Only Sales side terms send to CRM side.
                    *
                    */
                    if (extrareferences != null && extrareferences.getLineLevelTermFlag() == 1) {
                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
                        hashMap.put(IndiaComplianceConstants.SALES_OR_PURCHASE_FLAG, true);//For Sales
                        hashMap.put(Constants.productid, product.getID());
                        JSONArray ProductTermSalesArr = accProductObj.getProductTermsJsonArray(hashMap);
                        obj.put("ProductTermSalesMapp", ProductTermSalesArr);
                    }
                    
                    //Send shared Documents related data to CRM
                    Map<String, Object> docsParams = new HashMap<String, Object>();
                    String recordID = product.getID();
                    docsParams.put("recid", recordID);
                    docsParams.put(Constants.companyKey, product.getCompany().getCompanyID());
                    docsParams.put("module", "30");
                    JSONArray docsjson = accProductObj.getProductDocumentsArray(docsParams);
                    obj.put("shareddocs", docsjson);                    
                    //=======================================================
                    
                    Map<String, Object> customParams = new HashMap<String, Object>();
                    customParams.put("fieldList", lst);
                    customParams.put("companyid", companyId);
                    customParams.put("currentDate", currentDate);
                    customParams.put("productObj", product);
                    customParams.put("accountingHandlerDAOobj", accountingHandlerDAOobj);
                    customParams.put("accProductObj", accProductObj);

                    JSONArray customJobj = getProductCutomDataJson(customParams);
                    obj.put("customdata", customJobj);
                    
                    if (nonSaleInventory && obj.get("producttype").equals(producttype.Inventory_Non_Sales)) {
                        // Do Nothing
                    } else {
                        jArr.put(obj);
                    }
                } catch (UnsupportedEncodingException ex) {
                    throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
                }
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(productHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    public static JSONArray getProductCutomDataJson(Map<String, Object> customParams) {
        JSONArray customData = new JSONArray();
        List fieldList = (List) customParams.get("fieldList");
        String companyID = (String) customParams.get("companyid");
        DateFormat df=new SimpleDateFormat(Constants.MMMMdyyyy);
        Date currentDate = (Date) customParams.get("currentDate");
        Product product = (Product) customParams.get("productObj");
        AccountingHandlerDAO accountingHandlerDAOobj = (AccountingHandlerDAO) customParams.get("accountingHandlerDAOobj");
        accProductDAO accProductObj = (accProductDAO) customParams.get("accProductObj");
        
        try {
            //following lines added while converting long dates to Date object and are temporary
          
        KwlReturnObject serverSpecResult = accountingHandlerDAOobj.getObject(ServerSpecificOptions.class.getName(),ServerSpecificOptions.Case_CustomDateTypeChange);
        ServerSpecificOptions serverSpecificOptions=(ServerSpecificOptions)serverSpecResult.getEntityList().get(0);
         boolean isDeployed=Boolean.valueOf(StringUtil.isAppDeployed("1",serverSpecificOptions)); //1 is CRM Application ID
        Iterator itr = fieldList.iterator();
        while (itr.hasNext()) {
            FieldParams tmpcontyp = (FieldParams) itr.next();
            JSONObject customJobj = new JSONObject();
            customJobj.put("fieldname", tmpcontyp.getFieldname());
            customJobj.put("sequence", tmpcontyp.getSequence());

            customJobj.put("isessential", tmpcontyp.getIsessential());
            customJobj.put("maxlength", tmpcontyp.getMaxlength());
            customJobj.put("validationtype", tmpcontyp.getValidationtype());
            customJobj.put("fieldid", tmpcontyp.getId());
            customJobj.put("moduleid", tmpcontyp.getModuleid());
            customJobj.put("modulename", "\"Products & Services\"");
            customJobj.put("fieldtype", tmpcontyp.getFieldtype());
            customJobj.put("iseditable", tmpcontyp.getIseditable());
            customJobj.put("comboid", tmpcontyp.getComboid());
            customJobj.put("comboname", tmpcontyp.getComboname());
            customJobj.put("refcolumn_number", Constants.Custom_Column_Prefix + tmpcontyp.getRefcolnum());
            customJobj.put("column_number", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
            customJobj.put("fieldlabel", tmpcontyp.getFieldlabel());

            KwlReturnObject result = accountingHandlerDAOobj.getObject(AccProductCustomData.class.getName(), product.getID());
            AccProductCustomData accProductCustomData = (AccProductCustomData) result.getEntityList().get(0);
            if (accProductCustomData != null) {
                String coldata = accProductCustomData.getCol(tmpcontyp.getColnum());
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("fieldId", tmpcontyp.getId());
                params.put("productId", accProductCustomData.getProductId());
                params.put("companyId", companyID);
                params.put("transactionDate", currentDate);
                params.put("accountingHandlerDAOobj", accountingHandlerDAOobj);
                params.put("accProductObj", accProductObj);
                
                Object fieldValueObject = getProductCustomFieldValue(params);
                String latestValue = "";
                if (fieldValueObject != null) {
                    latestValue = (String) fieldValueObject;
                }
                
                if (!StringUtil.isNullOrEmpty(coldata)) {
                    String value = "";
                    if (latestValue.equalsIgnoreCase(coldata) || StringUtil.isNullOrEmpty(latestValue)) {
                        value = coldata;
                    } else {
                        value = latestValue;
                    }
                    
                    if(tmpcontyp.getFieldtype()==4 || tmpcontyp.getFieldtype()==7) {//for combo or multi-select sent display values of selected items
                        String[] array = value.split(",", -1);
                        value = "";
                        for(String id:array) {
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), id);
                            FieldComboData field = (FieldComboData) (FieldComboData) rdresult.getEntityList().get(0);
                            value += field.getValue()+", ";
                        }
                        customJobj.put("fieldData", value.substring(0, Math.max(0, value.length()-2)));
                    } else {
                        
                        if (tmpcontyp.getFieldtype() == 3) {

                            if (isDeployed) {

                                customJobj.put("fieldData", value);
                            } else {
                                //this if check is temporary , plz ref ERP-32324
                                Date customDate;
                                try {
                                    customDate = df.parse(value);
                                    value = Long.toString(customDate.getTime());
                                } catch (ParseException ex) {
                                    Logger.getLogger(productHandler.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                customJobj.put("fieldData", value);

                            }

                        } else {

                            customJobj.put("fieldData", value);
                        }
                    }
                }
            }
            customData.put(customJobj);
        }
        } catch(Exception e) {
            Logger.getLogger(productHandler.class.getName()).log(Level. SEVERE, "Exception While fetching product custom data", e);
        }
        
        return customData;
    }
    

//This method used to create JSON for Sync process.        
public static JSONArray getProductsJsonSync(JSONObject inputParamJobj, List<Product> list, accProductDAO accProductObj, accAccountDAO accAccountDAOobj, AccountingHandlerDAO accountingHandlerDAOobj, accCurrencyDAO accCurrencyDAOobj, boolean isCustomColumnExport) throws JSONException, ServiceException {
     
        JSONArray jArr = new JSONArray();
        System.out.println("Check point1: " + new Date());
        try {
            String companyId = inputParamJobj.getString("companyid");
            DateFormat df = authHandler.getDateFormatter(inputParamJobj);
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(inputParamJobj);
            KwlReturnObject cap = null;
            try {
                cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
            } catch (Exception ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
            /**
             * Get Extra Company pref. checks 
            */
            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            Calendar tempDate = Calendar.getInstance();
            tempDate.setTime(preferences.getBookBeginningFrom());
            Date transactionDate = tempDate.getTime();
            String date=df.format(transactionDate);
            try {
                transactionDate=df.parse(date);
            } catch (ParseException ex) {
                transactionDate = tempDate.getTime();
                Logger.getLogger(productHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            Producttype producttype = new Producttype();
            String productid = null;
            Boolean nonSaleInventory =false;
            
            //To send product custom data
            Date currentDate = authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(new Date()));
            HashMap<String, Object> requestParamsCustom = new HashMap<String, Object>();
            requestParamsCustom.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            requestParamsCustom.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Product_Master_ModuleId));
            requestParamsCustom.put("isActivated", 1);
            requestParamsCustom.put("order_by", Arrays.asList("sequence"));
            requestParamsCustom.put("order_type", Arrays.asList("asc"));
            KwlReturnObject result = accAccountDAOobj.getFieldParams(requestParamsCustom);
            List lst = result.getEntityList();
            
            for (Product product : list) {
                try {
                    Product parentProduct = product.getParent();
                    /**
                     * !product.isIsActive() used to restrict deactivated product
                     */
                    if (product.getID().equals(productid) || !product.isIsActive()) {
                        continue;
                    }

                    //======refer ticket ERP-11075 & ERP-11606============
                    Date creationDate = new Date(product.getCreatedon());
                    Calendar cal = Calendar.getInstance(); // locale-specific
                    cal.setTime(creationDate);
                    cal.setTimeZone(TimeZone.getTimeZone("GMT" +inputParamJobj.getString("userId")));        // need to add Application TimeZone
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    long time = cal.getTimeInMillis();

                    JSONObject obj = new JSONObject();
                    obj.put("createdon", time);
                    obj.put("id", product.getID());
                    obj.put("currencyid", product.getCurrency() != null ? product.getCurrency().getCurrencyID() : product.getCompany().getCurrency().getCurrencyID());
                    obj.put("productname", product.getName());
                    obj.put("pid", !StringUtil.isNullOrEmpty(product.getProductid()) ? product.getProductid() : "");
                    obj.put("description", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));
                    obj.put("desc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));
//                   
                    obj.put("vendor", (product.getVendor() != null ? product.getVendor().getID() : ""));
                    obj.put("vendornameid", (product.getVendor() != null ? product.getVendor().getName() : ""));
                    obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
                    obj.put("vendorphoneno", (product.getVendor() != null ? product.getVendor().getContactNumber() : ""));
                    obj.put("vendoremail", (product.getVendor() != null ? product.getVendor().getEmail() : ""));
                    if (product.isAsset()) {   //For Fixed Asset Group, type will be "Asset"
                        obj.put("type", "Asset");
                    } else {
                        obj.put("type", (product.getProducttype() != null ? product.getProducttype().getName() : ""));
                    }
                    if (product.getWarrantyperiod() == 0) {
                        obj.put("warranty", "N/A");
                    } else {
                        obj.put("warranty", product.getWarrantyperiod());
                    }
                    if (product.getWarrantyperiodsal() == 0) {
                        obj.put("warrantysal", "N/A");
                    } else {
                        obj.put("warrantysal", product.getWarrantyperiodsal());
                    }
                    
                    Object[] detailsArray = new Object[3];;
                    String currencyId="";
                    if(product.getCurrency()!=null){
                        currencyId=product.getCurrency().getCurrencyID();
                    }
                    boolean excludeInitialPrice = true;
                    String stockUomID = product.getUnitOfMeasure() != null ? product.getUnitOfMeasure().getID() : "";
                    KwlReturnObject result1 = accProductObj.getProductPrice(product.getID(), true, null, "", currencyId, stockUomID,excludeInitialPrice); // purchasePrice
                    if (result1.getEntityList().get(0) == null) {
                        result1 = accProductObj.getProductPrice(product.getID(), true, null, "", currencyId); // purchasePrice
                    }
                    detailsArray[0] = result1.getEntityList().get(0);

                    result1 = accProductObj.getProductPrice(product.getID(), false, null, "", currencyId, stockUomID,excludeInitialPrice); // salesPrice
                    if (result1.getEntityList().get(0) == null) {
                        result1 = accProductObj.getProductPrice(product.getID(), false, null, "", currencyId); // salesPrice
                    }
                    detailsArray[1] = result1.getEntityList().get(0);


                    result1 = accProductObj.getInitialPrice(product.getID(), true); 
                    detailsArray[2] = result1.getEntityList().get(0); // initialprice                    
                    
                    
//                    
                    double purchasePriceInBase = 0;
                    double salesPriceInBase = 0;

                    Double purchasePrice = detailsArray[0] != null ? (Double) detailsArray[0] : 0;
                    Double salesPrice = detailsArray[1] != null ? (Double) detailsArray[1] : 0;
                    
                    String productCurrencyId = product.getCompany().getCurrency().getCurrencyID();
                    if (product.getCurrency() != null) {
                        productCurrencyId = product.getCurrency().getCurrencyID();
                    }

                    KwlReturnObject pAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, purchasePrice, productCurrencyId, transactionDate, 0);
                    purchasePriceInBase = (Double) pAmt.getEntityList().get(0);

                    KwlReturnObject sAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, salesPrice, productCurrencyId, transactionDate, 0);
                    salesPriceInBase = (Double) sAmt.getEntityList().get(0);
                    obj.put("currentpurchaseprice", authHandler.round(purchasePriceInBase, companyId));
                    obj.put("assetSaleGL", (product.getSellAssetGLAccount() != null) ? product.getSellAssetGLAccount().getID() : "");
                    obj.put("depreciationGLAccount", (product.getDepreciationGLAccount() != null) ? product.getDepreciationGLAccount().getID() : "");
                    obj.put("depreciationProvisionGLAccount", (product.getDepreciationProvisionGLAccount() != null) ? product.getDepreciationProvisionGLAccount().getID() : "");
                    obj.put("depreciationRate", product.getDepreciationRate());
                    obj.put("saleprice", authHandler.round(salesPriceInBase, companyId));
                    obj.put("salespriceinpricecurrency", detailsArray[1] == null ? 0 : detailsArray[1]);
                    
                    //====extra fields to be send to map the columns data====
                    
                    obj.put("purchaseprice", (detailsArray[2] == null ? 0 : detailsArray[2]));
                    
                    obj.put("uomname", (product.getPackaging() != null && product.getPackaging().getStockUoM() != null) ? product.getPackaging().getStockUoM().getNameEmptyforNA() : product.getUnitOfMeasure() !=null ? product.getUnitOfMeasure().getNameEmptyforNA():"");
                    obj.put("reorderlevel", product.getReorderLevel());
                    obj.put("reorderquantity", product.getReorderQuantity());
                    obj.put("leadtime", product.getLeadTimeInDays());
                    obj.put("parentid", product.getParent()!=null ? product.getParent().getProductName(): "");
                    obj.put("salesaccountname", product.getSalesAccount()!=null ? product.getSalesAccount().getAccountName(): "");
                    obj.put("salesretaccountname", product.getSalesReturnAccount()!=null ? product.getSalesReturnAccount().getAccountName(): "");
                    obj.put("purchaseaccountname", product.getPurchaseAccount()!=null ? product.getPurchaseAccount().getAccountName(): "");
                    obj.put("purchaseretaccountname", product.getPurchaseReturnAccount()!=null ? product.getPurchaseReturnAccount().getAccountName(): "");
                    obj.put("quantity", product.getAvailableQuantity());
                    obj.put("locationName", product.getLocation()!=null ? product.getLocation().getName(): "");
                    obj.put("warehouseName", product.getWarehouse()!=null ? product.getWarehouse().getName(): "");
                    obj.put("currencyName", product.getCurrency()!=null ? product.getCurrency().getCurrencyID() : "");
                    obj.put("purchaseuom", product.getPurchaseUOM()!=null ? product.getPurchaseUOM().getNameEmptyforNA(): "");
                    obj.put("salesuom", product.getSalesUOM()!=null ? product.getSalesUOM().getNameEmptyforNA(): "");
                    obj.put("casinguom", (product.getPackaging()!=null && product.getPackaging().getCasingUoM()!=null) ? product.getPackaging().getCasingUoM().getNameEmptyforNA() : "");
                    obj.put("inneruom", (product.getPackaging()!=null && product.getPackaging().getInnerUoM()!=null) ? product.getPackaging().getInnerUoM().getNameEmptyforNA() : "");
                    obj.put("casinguom_value", product.getPackaging()!=null ? product.getPackaging().getCasingUomValue() : 0);
                    obj.put("inneruom_value", product.getPackaging()!=null ? product.getPackaging().getInnerUomValue() : 0);
                    obj.put("stockuom_value", product.getPackaging()!=null ? product.getPackaging().getStockUomValue() : 0);
                    obj.put("supplier", product.getSupplier());
                    obj.put("coilcraft", product.getCoilcraft());
                    obj.put("interplant", product.getInterplant());
                    obj.put("wipoffset", product.getWIPOffset());
                    obj.put("inventoryoffset", product.getInventoryOffset());
                    obj.put("hscode", product.getHSCode());
                    obj.put("additionalfreetext", product.getAdditionalFreeText());
                    obj.put("itemcolor", product.getItemColor());
                    obj.put("alternateproduct", product.getAlternateProduct());
                    obj.put("purchasemfg", product.getPurchaseMfg());
                    obj.put("catalogno", product.getCatalogNo());
                    obj.put("barcode", product.getBarcode());
                    obj.put("additionaldesc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getAdditionalDesc()) ? "" : product.getAdditionalDesc(), "UTF-8"));
                    obj.put("descinforeign", product.getDescInForeign());
                    obj.put("licensecode", product.getLicenseCode());
                    obj.put("itemgroup", product.getItemGroup());
                    obj.put("pricelist", product.getPriceList());
                    obj.put("shippingtype", product.getShippingType());
                    obj.put("itemsalesvolume", product.getItemSalesVolume());
                    obj.put("productweight", product.getProductweight());
                    obj.put("productweightperstockuom",  product.getProductWeightPerStockUom());
                    obj.put("productweightincludingpakagingperstockuom", product.getProductWeightIncludingPakagingPerStockUom());
                    obj.put("productvolumeperstockuom",product.getProductVolumePerStockUom());
                    obj.put("productvolumeincludingpakagingperstockuom",product.getProductVolumeIncludingPakagingPerStockUom());
                    obj.put("itemsaleswidth", product.getItemSalesWidth());
                    obj.put("itemsalesheight", product.getItemSalesHeight());
                    obj.put("itemwidth", product.getItemWidth());
                    obj.put("itemvolume", product.getItemVolume());
                    obj.put("itempurchasewidth", product.getItemPurchaseWidth());
                    obj.put("itempurchaselength", product.getItemPurchaseLength());
                    obj.put("qaleadtimeindays", product.getQALeadTimeInDays());
                    obj.put("reusabilitycount", product.getReusabilityCount());
                    obj.put("orderinguom", product.getOrderingUOM()!=null ? product.getOrderingUOM().getNameEmptyforNA(): "");
                    obj.put("transferuom", product.getTransferUOM()!=null ? product.getTransferUOM().getNameEmptyforNA(): "");
                    obj.put("itempurchaseheight", product.getItemPurchaseHeight());
                    obj.put("itempurchasevolume", product.getItemPurchaseVolume());
                    obj.put("itemsaleslength", product.getItemSalesLength());
                    obj.put("itemlength", product.getItemLength());
                    obj.put("itemheight", product.getItemHeight());
                    obj.put("asofdate", product.getAsOfDate()!=null ? product.getAsOfDate().getTime() : "");
                    /**
                    * Get Product level Terms if subdomain have line level term check ON and Integration with CRM from ERP
                    * Get Product terms mapped with product while creating 
                    * Only Sales side terms send to CRM side.
                    *
                    */
                    if (extrareferences != null && extrareferences.getLineLevelTermFlag() == 1) {
                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
                        hashMap.put(IndiaComplianceConstants.SALES_OR_PURCHASE_FLAG, true);//For Sales
                        hashMap.put(Constants.productid, product.getID());
                        JSONArray ProductTermSalesArr = accProductObj.getProductTermsJsonArray(hashMap);
                        obj.put("ProductTermSalesMapp", ProductTermSalesArr);
                    }
                    
                    //Send shared Documents related data to CRM
                    Map<String, Object> docsParams = new HashMap<String, Object>();
                    String recordID = product.getID();
                    docsParams.put("recid", recordID);
                    docsParams.put(Constants.companyKey, product.getCompany().getCompanyID());
                    docsParams.put("module", "30");
                    JSONArray docsjson = accProductObj.getProductDocumentsArray(docsParams);
                    obj.put("shareddocs", docsjson);                    
                    //=======================================================
                    
                    Map<String, Object> customParams = new HashMap<String, Object>();
                    customParams.put("fieldList", lst);
                    customParams.put("companyid", companyId);
                    customParams.put("currentDate", currentDate);
                    customParams.put("productObj", product);
                    customParams.put("accountingHandlerDAOobj", accountingHandlerDAOobj);
                    customParams.put("accProductObj", accProductObj);

                    JSONArray customJobj = getProductCutomDataJson(customParams);
                    obj.put("customdata", customJobj);
                    
                    if (nonSaleInventory && obj.get("producttype").equals(producttype.Inventory_Non_Sales)) {
                        // Do Nothing
                    } else {
                        jArr.put(obj);
                    }
                } catch (UnsupportedEncodingException ex) {
                    throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
                }
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(productHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Check point2: " + new Date());
        return jArr;
    }    
    
    public static Object getProductCustomFieldValue(Map<String, Object> params) {
        Object returnObject = null;
        String fieldId = (String) params.get("fieldId");
        String productId = (String) params.get("productId");
        String companyId = (String) params.get("companyId");
        Date transactionDate = (Date) params.get("transactionDate");
        AccountingHandlerDAO accountingHandlerDAOobj = (AccountingHandlerDAO) params.get("accountingHandlerDAOobj");
        accProductDAO accProductObj = (accProductDAO) params.get("accProductObj");
        try {
            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
            customrequestParams.put("fieldId", fieldId);
            customrequestParams.put("productId", productId);
            customrequestParams.put("companyId", companyId);
            customrequestParams.put("transactionDate", transactionDate);
            KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), fieldId);
            FieldParams fieldParams = (FieldParams) custumObjresult.getEntityList().get(0);
            if (fieldParams != null && fieldParams.getFieldtype() == 1 || fieldParams.getFieldtype() == 2) {
                KwlReturnObject result = accProductObj.getProductCustomFieldValue(customrequestParams);

                List list = result.getEntityList();
                Iterator itr = list.iterator();
                if (itr.hasNext()) {
                    returnObject = itr.next();
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception e){
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnObject;
    }
    
    public static JSONArray getAssemblyProductsJSON(HttpServletRequest request, List list) throws JSONException, ServiceException {
        Iterator itr = list.iterator();
        JSONArray jArr = new JSONArray();
        Producttype producttype = new Producttype();
        String productid = request.getParameter("productid");
        Boolean isSearch = false;
        try {
//            DateFormat df = authHandler.getDateFormatter(request);    //refer ticket ERP-15117
            DateFormat df = authHandler.getDateOnlyFormat(request);
            if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
                isSearch = true;
            }
            while (itr.hasNext()) {

//                Object[] row = (Object[]) itr.next();
                ProductBuild productBuild = (ProductBuild) itr.next();
                Product product = productBuild.getProduct();
                JSONObject obj = new JSONObject();
                obj.put("productid", productBuild.getID());
                obj.put("billid", productBuild.getID());    //ERM-26 to use generic export functionality for print button in Document Designer
                obj.put("mainproductid", product.getID());
                obj.put("productname", product.getName());
                obj.put("quantity", productBuild.getQuantity());
                obj.put("productrefno", productBuild.getRefno());
                obj.put("sequenceformatid", productBuild.getSeqformat() != null ? productBuild.getSeqformat().getID() : "");
                obj.put("memo", productBuild.getMemo());
                if (productBuild.getEntryDate() != null) {
                    Date assemblyCreationDate = productBuild.getEntryDate();
                    obj.put("entrydate", df.format(assemblyCreationDate));
                } else {
                    obj.put("entrydate", "");
                }
                obj.put("description", StringUtil.isNullOrEmpty(productBuild.getDescription()) ? "" : productBuild.getDescription());
                obj.put("buildProdId", product.getProductid());
                obj.put("journalentryid", (productBuild.getJournalentry() != null) ? productBuild.getJournalentry().getID() : "");
                obj.put("entryno", (productBuild.getJournalentry() != null) ? productBuild.getJournalentry().getEntryNumber() : "");
                obj.put("bomCode", (productBuild.getBomdetail() != null) ? productBuild.getBomdetail().getBomCode() : "");
                obj.put("bomdetailid", (productBuild.getBomdetail() != null) ? productBuild.getBomdetail().getID() : "");
                jArr.put(obj);

            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    public static JSONArray getBuildAssemblyDetailsJSON(HttpServletRequest request, List list) throws JSONException, ServiceException {
        Iterator itr = list.iterator();
        JSONArray jArr = new JSONArray();
        while (itr.hasNext()) {
            try {
                ProductBuildDetails productBuildDetails = (ProductBuildDetails) itr.next();
                Product product = productBuildDetails.getBuild().getProduct();
                JSONObject obj = new JSONObject();
                obj.put("buildId", productBuildDetails.getBuild().getID());
                obj.put("rowProductId", productBuildDetails.getAproduct().getID());
                obj.put("rowProductName", productBuildDetails.getAproduct().getName());
                obj.put("quantity", productBuildDetails.getBuild().getQuantity() * productBuildDetails.getAquantity());
                obj.put("cost", productBuildDetails.getBuild().getQuantity()* productBuildDetails.getAquantity() * productBuildDetails.getRate());
                if (productBuildDetails.getBuild().getEntryDate() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    obj.put("entrydate", sdf.format(productBuildDetails.getBuild().getEntryDate()));
                } else {
                    obj.put("entrydate", "");
                }
                obj.put("productid", product.getProductid());
                jArr.put(obj);
            } catch (Exception ex) {
                throw ServiceException.FAILURE("getBuildAssemblyDetailsJSON : " + ex.getMessage(), ex);
            }
        }
        return jArr;
    }
    public static JSONArray getProductsPDFJson(HttpServletRequest request, List list, HashMap<String, Object> requestParams, accProductDAO accProductObj) throws JSONException, ServiceException {
        Iterator itr = list.iterator();
        JSONArray jArr = new JSONArray();
        Producttype producttype = new Producttype();
        String productid = request.getParameter("productid");
        String companyid = "";
        Boolean isSearch = false;
        if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
            isSearch = true;
        }
        
        
        boolean isForProductQuantityDetailsReport = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isForProductQuantityDetailsReport"))) {
            isForProductQuantityDetailsReport = Boolean.parseBoolean(request.getParameter("isForProductQuantityDetailsReport"));
        }
        
        String[] selectedUOMsArray = null;
        
        if(isForProductQuantityDetailsReport){
            String selectedUOMs = request.getParameter("selectedUOMs");
            
            selectedUOMsArray = selectedUOMs.split(",");
        }
        
        Boolean nonSaleInventory = Boolean.parseBoolean((String) request.getParameter("loadInventory"));
        while (itr.hasNext()) {
            try {
                Object[] row = (Object[]) itr.next();
                Product product = (Product) row[0];
                Product parentProduct = product.getParent();
                if(requestParams.containsKey("companyid")){
                    companyid = (String) requestParams.get("companyid");
                }
                if (product.getID().equals(productid)) {
                    continue;
                }
//                ProductCyclecount pcObject = (ProductCyclecount) row[8];
                JSONObject obj = new JSONObject();
                obj.put("currencyName", (String) requestParams.get("currencyName"));
                obj.put("productid", product.getID());
                obj.put("productname", product.getName());
                obj.put("description", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));
                obj.put("desc", product.getDescription());
                UnitOfMeasure uom = product.getUnitOfMeasure();
                obj.put("uomid", uom == null ? "" : uom.getID());
                obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
                obj.put("precision", uom == null ? 0 : (Integer) uom.getAllowedPrecision());
                obj.put("leadtime", product.getLeadTimeInDays());
//                obj.put("leadtime", (product.getLeadTimeInDays() == 1 || product.getLeadTimeInDays() == 0) ? product.getLeadTimeInDays() + " Day" : product.getLeadTimeInDays() + " Days");
                //if(product.getWarrantyperiod() !=-1){
                obj.put("warrantyperiod", product.getWarrantyperiod());
                obj.put("warrantyperiodsal", product.getWarrantyperiodsal());
                // }

                obj.put("supplier", product.getSupplier());
                obj.put("coilcraft", product.getCoilcraft());
                obj.put("interplant", product.getInterplant());
                obj.put("syncable", product.isSyncable());
                obj.put("multiuom", product.isMultiuom());
                obj.put("uomschematypeid", product.getUomSchemaType()!=null?product.getUomSchemaType().getID():"");
                obj.put("reorderlevel", authHandler.formattedQuantity(product.getReorderLevel(), companyid));
                obj.put("reorderquantity", authHandler.formattedQuantity(product.getReorderQuantity(), companyid));
                obj.put("purchaseaccountid", (product.getPurchaseAccount() != null ? product.getPurchaseAccount().getID() : ""));
                obj.put("purchaseaccountname", (product.getPurchaseAccount() != null ? product.getPurchaseAccount().getName() : ""));
                obj.put("salesaccountid", (product.getSalesAccount() != null ? product.getSalesAccount().getID() : ""));
                obj.put("salesaccountname", (product.getSalesAccount() != null ? product.getSalesAccount().getName() : ""));
                obj.put("purchaseacctaxcode", ((product.getPurchaseAccount() != null && (!StringUtil.isNullOrEmpty(product.getPurchaseAccount().getTaxid()))) ? product.getPurchaseAccount().getTaxid() : ""));
                obj.put("salesacctaxcode", ((product.getSalesAccount() != null && (!StringUtil.isNullOrEmpty(product.getSalesAccount().getTaxid()))) ? product.getSalesAccount().getTaxid() : ""));
                obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : ""));
                obj.put("purchaseretaccountname", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getName() : ""));
                obj.put("salesretaccountid", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getID() : ""));
                obj.put("salesretaccountname", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getName() : ""));
                obj.put("vendor", (product.getVendor() != null ? product.getVendor().getID() : ""));
                obj.put("vendornameid", (product.getVendor() != null ? product.getVendor().getName() : ""));
                obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
                obj.put("vendorphoneno", (product.getVendor() != null ? product.getVendor().getContactNumber() : ""));
                obj.put("vendoremail", (product.getVendor() != null ? product.getVendor().getEmail() : ""));
                obj.put("type", (product.getProducttype() != null ? product.getProducttype().getName() : ""));
                obj.put("pid", product.getProductid());
                obj.put("locationName", (product.getLocation() != null) ? product.getLocation().getName() : "");
                obj.put("warehouseName", (product.getWarehouse() != null) ? product.getWarehouse().getName() : "");
                if (product.getWarrantyperiod() == 0) {
                    obj.put("warranty", "N/A");
                } else {
//                    obj.put("warranty", (product.getWarrantyperiod() == 1 || product.getWarrantyperiod() == 0) ? product.getWarrantyperiod() + " Day" : product.getWarrantyperiod() + " Days");
                    obj.put("warranty", product.getWarrantyperiod());
                }
                if (product.getWarrantyperiodsal() == 0) {
                    obj.put("warrantysal", "N/A");
                } else {
//                    obj.put("warrantysal", (product.getWarrantyperiodsal() == 1 || product.getWarrantyperiodsal() == 0) ? product.getWarrantyperiodsal() + " Day" : product.getWarrantyperiodsal() + " Days");
                    obj.put("warrantysal", product.getWarrantyperiodsal());
                }
                obj.put("parentuuid", parentProduct == null ? "" : parentProduct.getID());
                obj.put("parentid", parentProduct == null ? "" : parentProduct.getProductid());
                obj.put("parentname", parentProduct == null ? "" : parentProduct.getName());
                if (isForProductQuantityDetailsReport) {
                    for (int i = 0; i < selectedUOMsArray.length; i++) {
                        // get quantity in this uom
                        String selectedUOM = selectedUOMsArray[i];
                        KwlReturnObject uomresult = accProductObj.getObject(UnitOfMeasure.class.getName(), selectedUOM);
                        UnitOfMeasure unitOfMeasure = (UnitOfMeasure) uomresult.getEntityList().get(0);
                        if (unitOfMeasure != null) {
                            KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(product.getID(), selectedUOM);
                            double availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                            obj.put(selectedUOM, authHandler.formattedQuantity(availableQuantity, companyid));
                        }

                    }
                }
                if (isSearch) {
                    obj.put("level", 0);
                    obj.put("leaf", true);
                } else {
                    obj.put("level", row[1]);
                    obj.put("leaf", row[2]);
                }
                double purchaseprice = row[3] == null ? 0.0 : Double.parseDouble(row[3].toString());
                double saleprice = row[4] == null ? 0.0 : Double.parseDouble(row[4].toString());
                double quantity = row[5] == null ? 0.0 : Double.parseDouble(row[5].toString());
                obj.put("purchaseprice", purchaseprice);
                obj.put("saleprice", saleprice);
                if (product.isblockLooseSell()) {
                    KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(product.getID(), product.getUnitOfMeasure().getID());
                    double availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                    obj.put("quantity", (availableQuantity));
                } else {
                    obj.put("quantity", authHandler.formattedQuantity(quantity, companyid));
                }
                
                obj.put("initialquantity", (row[6] == null ? 0 : row[6]));
                obj.put("initialprice", (row[7] == null ? 0 : row[7]));
                obj.put("salespricedatewise", (row[9] == null ? 0 : row[9]));
                obj.put("purchasepricedatewise", (row[10] == null ? 0 : row[10]));
                obj.put("initialsalesprice", (row[11] == null ? 0 : row[11]));
                obj.put("lockquantity", authHandler.formattedQuantity(((Double)(row[13] == null ? 0 : row[13])), companyid));
                obj.put("consignquantity", (row[15] == null ? 0 : row[15]));
                obj.put("venconsignquantity", (row[16] == null ? 0 : row[16]));
//                obj.put("ccountinterval",pcObject!=null?((pcObject.getCountInterval()==1||pcObject.getCountInterval()==0)?pcObject.getCountInterval()+" Day":pcObject.getCountInterval()+" Days"):"");
//                obj.put("ccountinterval", pcObject != null ? pcObject.getCountInterval() : "");
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
                if (nonSaleInventory && obj.get("producttype").equals(producttype.Inventory_Non_Sales)) {
                    // Do Nothing
                } else {
                    jArr.put(obj);
                }
            } catch (UnsupportedEncodingException ex) {
                throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
            }
            }
        return jArr;
    }

    public static JSONArray getProductTypesJson(HttpServletRequest request, List list) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Producttype ptype = (Producttype) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", ptype.getID());
                obj.put("name", ptype.getName());
                jArr.put(obj);
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getProductTypesJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    public static JSONArray getProductTypesJson(List list) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        try {
            for (Object listType:list) {
                Producttype ptype = (Producttype) listType;
                JSONObject obj = new JSONObject();
                obj.put("id", ptype.getID());
                obj.put("name", ptype.getName());
                jArr.put(obj);
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getProductTypesJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public static void setCustomColumnValuesForProduct(AccProductCustomData customData, HashMap<String, Integer> fieldMap, Map<String, String> replaceFieldMap,
            Map<String, Object> variableMap) {
        for (Map.Entry<String, Integer> field : fieldMap.entrySet()) {
            Integer colnumber = field.getValue();
            if (colnumber > 0) { // colnumber will be 0 if key is part of reference map
                Integer isref = fieldMap.get(field.getKey() + "#" + colnumber);// added '#' while creating map collection for custom fields.
                // Without this change, it creates problem if two custom columns having name like XYZ and XYZ1
                String coldata = null;
                if (isref != null) {
                    try {
                        coldata = customData.getCol(colnumber);
                        String coldataVal = null;
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            if (coldata.length() > 1) {
                                if (isref == 1) {
//                                        coldataVal = fieldDataManagercntrl.getMultiSelectColData(coldata);
//                                        if(isExport) {
//                                            coldata = coldataVal;
//                                        }
//                                        variablesMapForCustomData.put(field.getKey(), coldataVal);
                                } else if (isref == 0) {
//                                        coldataVal = customData.getRefCol(colnumber);
//                                        if(isExport) {
//                                            coldata = coldataVal;
//                                        }
//                                        variablesMapForCustomData.put(field.getKey(), coldataVal);
                                } else if (isref == 3) {
                                    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                                    DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                                    Date dateFromDB=null;
                                    try {
                                        dateFromDB = defaultDateFormat.parse(coldata);
                                        coldata = df2.format(dateFromDB);
                                    } catch (Exception e) {
                                    }
                                }
                            }
                            variableMap.put(field.getKey(), coldata);
//                                try {
//                                    variableMapForFormula.put(replaceFieldMap.get(field.getKey()), Double.parseDouble(coldata));
//                                } catch (Exception ex) {
//                                    variableMapForFormula.put(replaceFieldMap.get(field.getKey()), 0);
//                                }
                        }
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                    } catch (ObjectNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    /**
     *
     * @param jarrColumns = put store config
     * @param jarrRecords = put Store record
     * @throws JSONException
     */
    public static void createColumnModelForStockMovement(JSONArray jarrColumns, JSONArray jarrRecords, HashMap<String, Object> hashmap) throws JSONException {
        JSONObject jobjTemp = new JSONObject();
        
        Locale locale = null;
        if(hashmap.containsKey("locale")){
            locale = (Locale) hashmap.get("locale");
        }
        
        jobjTemp.put("name", "itemcode");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.product.threshold.grid.productID", null, locale));
        jobjTemp.put("dataIndex", "itemcode");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "itemname");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header",messageSource.getMessage("acc.contractMasterGrid.header7", null, locale));
        jobjTemp.put("dataIndex", "itemname");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "itemdescription");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header",messageSource.getMessage("acc.bankReconcile.import.grid.Description", null, locale));
        jobjTemp.put("dataIndex", "itemdescription");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "storedescription");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.inv.loclevel.1", null, locale));
        jobjTemp.put("dataIndex", "storedescription");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "locationName");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.inventorySetup.locationStatusttp.add", null, locale));
        jobjTemp.put("dataIndex", "locationName");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "rowName");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.inventorysetup.row", null, locale));
        jobjTemp.put("dataIndex", "rowName");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("hidden", "Wtf.account.companyAccountPref.isRowCompulsory ? false : true");
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "rackName");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header",messageSource.getMessage("acc.inventorysetup.rack", null, locale));
        jobjTemp.put("dataIndex", "rackName");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("hidden", "Wtf.account.companyAccountPref.isRackCompulsory ? false : true");
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "binName");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.inventorysetup.bin", null, locale));
        jobjTemp.put("dataIndex", "binName");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("hidden", "Wtf.account.companyAccountPref.isBinCompulsory ? false : true");
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "date");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.field.TransactionDate", null, locale));
        jobjTemp.put("dataIndex", "date");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "costcenter");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.field.CostCenter", null, locale));
        jobjTemp.put("dataIndex", "costcenter");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "moduleName");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.vppl.TransactionModule", null, locale));
        jobjTemp.put("dataIndex", "moduleName");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "orderno");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header",messageSource.getMessage("acc.common.reference", null, locale));
        jobjTemp.put("dataIndex", "orderno");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "remark");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.product.Remarks", null, locale));
        jobjTemp.put("dataIndex", "remark");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.remarkRenderer");
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "vendor");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header",messageSource.getMessage("acc.exportdetails.custven", null, locale));
        jobjTemp.put("dataIndex", "vendor");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "type");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header",messageSource.getMessage("acc.masterConfig.type", null, locale));
        jobjTemp.put("dataIndex", "type");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "orderuom");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.invoice.gridUOM", null, locale));
        jobjTemp.put("dataIndex", "orderuom");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "qtyIn");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header",messageSource.getMessage("acc.vppl.QuantityIN", null, locale));
        jobjTemp.put("dataIndex", "qtyIn");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("summaryType", "sum");
        jobjTemp.put("renderer", "WtfGlobal.quantityInRenderer");
        jobjTemp.put("summaryRenderer", "WtfGlobal.quantityInsummaryRenderer");
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "qtyOut");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header",messageSource.getMessage("acc.vppl.QuantityOUT", null, locale));
        jobjTemp.put("dataIndex", "qtyOut");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("summaryType", "sum");
        jobjTemp.put("renderer", "WtfGlobal.quantityOUTRenderer");
        jobjTemp.put("summaryRenderer", "WtfGlobal.quantityOUTsummaryRenderer");
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "batchName");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header",messageSource.getMessage("acc.field.lotBatch", null, locale));
        jobjTemp.put("dataIndex", "batchName");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.returnValRenderer");
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "serialNames");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header",messageSource.getMessage("acc.field.SerialNo", null, locale));
        jobjTemp.put("dataIndex", "serialNames");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.serialNoRenderer");
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "avgCost");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header",messageSource.getMessage("acc.invoice.gridUnitPrice", null, locale));
        jobjTemp.put("dataIndex", "avgCost");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.returnValRenderer");
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "amount");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header",messageSource.getMessage("acc.masterconfig.amount", null, locale));
        jobjTemp.put("dataIndex", "amount");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("summaryType", "sum");
        jobjTemp.put("renderer", "WtfGlobal.returnValRenderer");
        jobjTemp.put("summaryRenderer", "WtfGlobal.quantityInsummaryRenderer");
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "stockType");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.field.stockType_ReusableOrNonReusable", null, locale));
        jobjTemp.put("dataIndex", "stockType");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.reusableRenderer");
        jarrColumns.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "reusabilityCount");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("header",messageSource.getMessage("acc.vppl.ReusabilityCount", null, locale));
        jobjTemp.put("dataIndex", "reusabilityCount");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("renderer", "WtfGlobal.ReusableCountRenderer");
        jarrColumns.put(jobjTemp);
    }
}
