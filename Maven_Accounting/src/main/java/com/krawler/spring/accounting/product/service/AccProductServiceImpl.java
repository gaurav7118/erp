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
package com.krawler.spring.accounting.product.service;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.common.util.ValuationMethod;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.ist.InterStoreTransferCustomData;
import com.krawler.inventory.model.ist.InterStoreTransferRequest;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stock.StockDAO;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.TransactionType;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockout.StockAdjustmentCustomData;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.account.accVendorCustomerProductDAO;
import com.krawler.spring.accounting.assemblyQA.AssemblyProductApprovalDetails;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.jasperreports.StockAgeing;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.*;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleServiceImpl;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.reports.AccReportsService;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.spring.mrp.WorkOrder.WorkOrderComponentDetails;
import com.krawler.spring.mrp.WorkOrder.WorkOrderDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.context.MessageSource;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.common.admin.AuditAction;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.uom.accUomDAO;
import com.krawler.spring.accounting.uom.service.AccUomService;
import java.util.Map.Entry;
import javax.sound.midi.SysexMessage;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.krawler.common.util.LandingCostAllocationType;
import com.krawler.inventory.model.ist.WOCDetailISTMapping;
import com.krawler.inventory.model.ist.InterStoreTransferStatus;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.stockmovement.StockMovement;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.accounting.asset.AccAssetService;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.common.fieldManagerDAO;
import net.sf.jasperreports.olap.mapping.DataMapping;

/**
 *
 * @author krawler
 */
public class AccProductServiceImpl implements AccProductService {
    private accProductDAO accProductObj;
    private accVendorCustomerProductDAO accVendorCustomerProductDAOobj;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accGoodsReceiptDAO accGoodsReceiptDAOobj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accInvoiceDAO accInvoiceDAOobj;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;
    private accAccountDAO accAccountDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private fieldDataManager fieldDataManagercntrl;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private APICallHandlerService apiCallHandlerService;
    private WorkOrderDAO workOrderDAOObj;
    private MessageSource messageSource;
    private StockDAO stockDAO;
    private StockService stockService;
    private accCurrencyDAO currencyDAO;
    private AccProductModuleService accProductModuleService;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccReportsService accReportsService;
    private auditTrailDAO auditTrailObj;
    private accUomDAO accUomObj;
    private AccUomService accUomService;
    private fieldManagerDAO fieldManagerDAOobj;
    private AccAssetService accAssetService;
    private StoreService storeService;
    private LocationService locationService;
    
    public void setAccAssetService(AccAssetService accAssetService) {
        this.accAssetService = accAssetService;
    }
    
    
    public void setAccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }

    public void setfieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setStockDAO(StockDAO stockDAO) {
        this.stockDAO = stockDAO;
    }
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public accVendorCustomerProductDAO getAccVendorCustomerProductDAOobj() {
        return accVendorCustomerProductDAOobj;
    }
    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }
    public void setaccCurrencyDAO(accCurrencyDAO currencyDAO) {
        this.currencyDAO = currencyDAO;
    }
    public void setAccVendorCustomerProductDAOobj(accVendorCustomerProductDAO accVendorCustomerProductDAOobj) {
        this.accVendorCustomerProductDAOobj = accVendorCustomerProductDAOobj;
    }
    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
     public void setAccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptDAOobj) {
        this.accGoodsReceiptDAOobj = accGoodsReceiptDAOobj;
    }
     
    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    
    public void setWorkOrderDAOObj(WorkOrderDAO workOrderDAOObj) {
        this.workOrderDAOObj = workOrderDAOObj;
    } 

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }
     public void setAccReportsService(AccReportsService accReportsService) {
        this.accReportsService = accReportsService;
    }

    public auditTrailDAO getAuditTrailObj() {
        return auditTrailObj;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }   
    public void setaccUomDAO(accUomDAO accUomObj) {
        this.accUomObj = accUomObj;
    }
    public void setaccUomService(AccUomService accUomService) {
        this.accUomService = accUomService;
    }
    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }
    @Override
    /* Product JSON Without Request*/
    public JSONObject getProductsJsonForCombo(JSONObject paramsjobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = productHandler.getProductRequestMapfromJson(paramsjobj);
            String venid = paramsjobj.optString(Constants.customerid, null);
            if (!StringUtil.isNullOrEmpty(paramsjobj.optString(Constants.moduleid, null))) {
                requestParams.put(Constants.moduleid, paramsjobj.getString(Constants.moduleid));
            }
            String companyid = requestParams.get(Constants.companyKey).toString();
            KwlReturnObject result01 = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences preferences = (ExtraCompanyPreferences) result01.getEntityList().get(0);
            requestParams.put(Constants.PRODUCT_SEARCH_FLAG,preferences.getProductSearchingFlag());
            KwlReturnObject result = accProductObj.getProductsForCombo(requestParams);
            KwlReturnObject result1, result2 = null;
            JSONArray vendorjarray = new JSONArray();
            JSONArray DataJArr = getProductsJSONArray(paramsjobj, result.getEntityList());
            String ss = "";
            if (!StringUtil.isNullOrEmpty(venid)) {//for vendor side
                result1 = accVendorCustomerProductDAOobj.getProductsByVendor(venid, ss, null);
                List<VendorProductMapping> listvendorproducts = result1.getEntityList();
                if (listvendorproducts.size() > 0) {
                    for (VendorProductMapping VendorProductObj : listvendorproducts) {
                        String productid = VendorProductObj.getProducts().getID();
                        //First comparing the product present in DataArrray i.e.Total Product List
                        if (listvendorproducts.size() > 0) {
                            for (int cnt = 0; cnt < DataJArr.length(); cnt++) {
                                String prodid = DataJArr.getJSONObject(cnt).getString(Constants.productid);
                                if (productid.equals(prodid)) {
                                    vendorjarray.put(DataJArr.getJSONObject(cnt));
                                }
                            }
                        }
                    }
                    //to remove the duplicates from DataArray
                    result2 = accVendorCustomerProductDAOobj.getProductsVendorabsence(requestParams, listvendorproducts);
                } else if (!StringUtil.isNullOrEmpty(venid)) {//for customer side
                    if (result1.getEntityList().isEmpty()) {
                        result1 = accVendorCustomerProductDAOobj.getProductsByCustomer(venid, ss, null);
                        List<CustomerProductMapping> listcustomerproducts = result1.getEntityList();
                        if (listcustomerproducts.size() > 0) {
                            for (CustomerProductMapping CustomerProductObj : listcustomerproducts) {
                                String productid = CustomerProductObj.getProducts().getID();
                                //First comparing the product present in DataArrray i.e.Total Product List
                                if (listcustomerproducts.size() > 0) {
                                    for (int cnt = 0; cnt < DataJArr.length(); cnt++) {
                                        String prodid = DataJArr.getJSONObject(cnt).getString(Constants.productid);
                                        if (productid.equals(prodid)) {
                                            vendorjarray.put(DataJArr.getJSONObject(cnt));
                                        }
                                    }
                                }
                            }
                            //to remove the duplicates from DataArray
                            result2 = accVendorCustomerProductDAOobj.getProductsCustomerabsence(requestParams, listcustomerproducts);
                        }
                    }
                }
                if (result1.getEntityList().size() > 0) {
                    JSONObject objreturn = getProductJson(result2, result1,paramsjobj);
                    JSONArray objARRAY = objreturn.getJSONArray("objreturn");
                    for (int jcount = 0; jcount < objARRAY.length(); jcount++) {
                        vendorjarray.put(objARRAY.getJSONObject(jcount));
                    }
                } else {//other than vendor & customer side
                    vendorjarray = DataJArr;
                }
            } else {
                vendorjarray = DataJArr;
            }//venid
            
            JSONArray pagedJson = vendorjarray;
            String start = paramsjobj.optString(Constants.start, null);
            String limit = paramsjobj.optString(Constants.limit, null);
            if (!paramsjobj.optBoolean(Constants.isdefaultHeaderMap)) {//ERP-37715 (Start & limit is already applied
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                }
            }
            jobj.put(Constants.RES_data, pagedJson);
            jobj.put(Constants.RES_TOTALCOUNT, pagedJson.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    //Used in Rest Services
    public JSONArray getProductsJSONArray(JSONObject paramsjobj, List<Product> list) throws JSONException, ServiceException, SessionExpiredException, Exception {
        JSONArray jArr = new JSONArray();
        Producttype producttype = new Producttype();
        String companyid = paramsjobj.getString(Constants.companyKey);
        KwlReturnObject kwlReturnObject = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) kwlReturnObject.getEntityList().get(0);

        kwlReturnObject = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) kwlReturnObject.getEntityList().get(0);

        String productid = paramsjobj.optString(Constants.productid, null);
        Boolean onlyProduct = Boolean.parseBoolean((String) paramsjobj.get("onlyProduct"));
        Boolean nonSaleInventory = Boolean.parseBoolean((String) paramsjobj.get("loadInventory"));
        
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap= new HashMap<String, Integer>();
        DateFormat df = new SimpleDateFormat(Constants.MMMMdyyyy);
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        
        if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap) ){//required only in case of mobiel apps
            FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        }
        
        for (Product product: list) {
            if (product.getID().equals(productid)) {
                continue;
            }
            JSONObject obj = new JSONObject();
            String productType = "";
            String gcurrencyid = paramsjobj.getString(Constants.globalCurrencyKey);
            productType = (product.getProducttype() != null ? product.getProducttype().getName() : "");
            if (nonSaleInventory && productType.equals(producttype.Inventory_Non_Sales)) {
                continue;
            }
            UnitOfMeasure uom = product.getUnitOfMeasure();
           
            if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                obj.put("productidValue", product.getName());
                obj.put("productID", product.getProductid());
                obj.put("uomname", uom == null ? "" : uom.getID());
                obj.put("uomnameValue", uom == null ? "" : uom.getNameEmptyforNA());
                if (product.getPackaging() != null && product.getPackaging().toString() != null) {
                    String packagingString = product.getPackaging().toString();
                    obj.put(Constants.packaging, packagingString);
                } else {
                    obj.put(Constants.packaging, "");
                }
                
                JSONArray jSONArray = new JSONArray();
                Map<String, Object> variableMap = new HashMap<String, Object>();

                KwlReturnObject proObjresult = accountingHandlerDAOobj.getObject(AccProductCustomData.class.getName(), product.getID());
                if (proObjresult != null && proObjresult.getEntityList().size() > 0) {
                    AccProductCustomData jeDetailCustom = (AccProductCustomData) proObjresult.getEntityList().get(0);
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                            JSONObject jSONObject = new JSONObject();
                            String fieldId = replaceFieldMap.get(varEntry.getKey());
                            fieldId = fieldId.replaceAll("custom_", "");
                            jSONObject.put("fieldid", fieldId);
                            KwlReturnObject fpresult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), fieldId);
                            FieldParams tmpcontyp = (FieldParams) fpresult.getEntityList().get(0);
                            if (tmpcontyp != null) {
                                String fieldLabel = tmpcontyp.getFieldlabel();
                                jSONObject.put("fieldname", tmpcontyp.getFieldname());
                                jSONObject.put("fieldlabel", fieldLabel);
                            }//end of tmpcontyp

                            String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                            String colValue = "";
                            String colDescription = "";
                            if (customFieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                                try {
                                    String[] valueData = coldata.split(",");
                                    for (String value : valueData) {
                                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                                        FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                        if (fieldComboData != null) {
                                            String fieldValue = fieldComboData.getValue();
                                            if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap)) {
                                                fieldValue = fieldManagerDAOobj.getIdsUsingParamsValue(fieldId, fieldValue);
                                            }
                                            colValue += fieldComboData.getValue() != null ? fieldValue + "," : ",";
                                        }
                                    }
                                    if (colValue.length() > 1) {
                                        colValue = colValue.substring(0, colValue.length() - 1);
                                    }
                                    jSONObject.put("fieldData", colValue);
                                } catch (Exception ex) {
                                    jSONObject.put("fieldData", coldata);
                                }
                            } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                                Date customDate;
                                try {
                                    customDate = df.parse(coldata);
                                    coldata = Long.toString(customDate.getTime());
                                } catch (ParseException ex) {
                                    Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                jSONObject.put("fieldData", coldata);

                            } else {
                                jSONObject.put("fieldData", coldata != null ? coldata : "");
                            }
                            jSONArray.put(jSONObject);
                        }
                    }
                }
                obj.put("customdata", jSONArray);
            } else {
                obj.put("productname", product.getName());
                obj.put("pid", product.getProductid());
                obj.put("uomid", uom == null ? "" : uom.getID());
                obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
            }
            /**
             * If Packaging schema Enable then send data to android app
             */
            if (extraCompanyPreferences != null && extraCompanyPreferences.getUomSchemaType() == Constants.PackagingUOM) {
                obj.put("caseuom", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUoM().getID() : "");
                obj.put("caseuomvalue", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUomValue() : 1);
                obj.put("inneruom", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUoM().getID() : "");
                obj.put("inneruomvalue", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUomValue() : 1);
                obj.put("stockuomvalue", (product.getPackaging() != null && product.getPackaging().getStockUoM()!= null) ? product.getPackaging().getStockUomValue(): 1);
                obj.put("stockuom", (product.getUnitOfMeasure() != null) ? product.getUnitOfMeasure().getID() : "");

            }

            obj.put(Constants.productid, product.getID());
            obj.put("desc", product.getDescription());
            obj.put("supplierpartnumber", StringUtil.isNullOrEmpty(product.getSupplier()) ? "" : product.getSupplier());

            obj.put("multiuom", product.isMultiuom());
            obj.put("uomschematypeid", product.getUomSchemaType() != null ? product.getUomSchemaType().getID() : "");
            obj.put("isBatchForProduct", product.isIsBatchForProduct());
            obj.put("isSerialForProduct", product.isIsSerialForProduct());
            obj.put("isLocationForProduct", product.isIslocationforproduct());
            obj.put("isWarehouseForProduct", product.isIswarehouseforproduct());
            obj.put("isRowForProduct", product.isIsrowforproduct());
            obj.put("isRackForProduct", product.isIsrackforproduct());
            obj.put("isBinForProduct", product.isIsbinforproduct());
            obj.put("isRecyclable", product.isRecyclable());
            obj.put("rcmapplicable", product.isRcmApplicable());
            obj.put("activateProductComposition", product.isActivateProductComposition());
            obj.put("recycleQuantity", product.getRecycleQuantity());
            obj.put("purchaseacctaxcode", ((product.getPurchaseAccount() != null && (!StringUtil.isNullOrEmpty(product.getPurchaseAccount().getTaxid()))) ? product.getPurchaseAccount().getTaxid() : ""));
            obj.put("salesacctaxcode", ((product.getSalesAccount() != null && (!StringUtil.isNullOrEmpty(product.getSalesAccount().getTaxid()))) ? product.getSalesAccount().getTaxid() : ""));
            obj.put("warrantyperiod", product.getWarrantyperiod());
            obj.put("warrantyperiodsal", product.getWarrantyperiodsal());
            // }
            if (!onlyProduct && !storageHandlerImpl.GetLowercaseCompanyId().contains(companyid)) {

                obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : ""));
                obj.put("salesretaccountid", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getID() : ""));
                KwlReturnObject purchase = accProductObj.getProductPrice(product.getID(), true, null, "", "");
                obj.put("purchaseprice", purchase.getEntityList().get(0));
                KwlReturnObject sales = accProductObj.getProductPrice(product.getID(), false, null, "", "");
                obj.put("saleprice", sales.getEntityList().get(0));
            }
            String type = "";

            boolean getSOPOflag = paramsjobj.optString("getSOPOflag", null) != null ? Boolean.parseBoolean(paramsjobj.getString("getSOPOflag")) : false;
            if (getSOPOflag) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                requestParams.put("df", authHandler.getDateFormatter(paramsjobj));
                requestParams.put("pendingapproval", false);
                requestParams.put(Constants.REQ_startdate, paramsjobj.optString(Constants.REQ_startdate, null));
                requestParams.put(Constants.REQ_enddate, paramsjobj.optString(Constants.REQ_enddate, null));
                KwlReturnObject poresult = accPurchaseOrderobj.getPurchaseOrdersMerged(requestParams);
                List list1 = poresult.getEntityList();
                double productCount = getOutstandingPoSoProductsCount(list1, true, product.getID(), accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj);
                obj.put("pocount", productCount);

                KwlReturnObject soresult = accSalesOrderDAOobj.getSalesOrdersMerged(requestParams);
                list1 = soresult.getEntityList();
                productCount = getOutstandingPoSoProductsCount(list1, false, product.getID(), accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj);
                obj.put("socount", productCount);
            }

            if (product.getProducttype() != null) {
                if (storageHandlerImpl.GetVRnetCompanyId().contains(companyid)) {
                    if (StringUtil.equal(product.getProducttype().getName(), "Inventory Assembly")) {
                        type = "Inventory Bundle";
                    } else {
                        type = product.getProducttype().getName();
                    }
                } else {
                    type = product.getProducttype().getName();
                }
            }

            if (preferences.isInventoryAccountingIntegration()) {
                JSONArray customJSONArray = getCustomColumnData(product.getID(), companyid);
                if (customJSONArray.length() > 0) {
                    for (int count = 0; count < customJSONArray.length(); count++) {

                        JSONObject customJSONObject = customJSONArray.getJSONObject(count);
                        String customFieldName = StringUtil.isNullOrEmpty(customJSONObject.optString("fieldname", "")) ? "" : customJSONObject.optString("fieldname", "");
                        if (!customJSONObject.isNull("fieldname") && (customFieldName.equalsIgnoreCase(Constants.CUSTOM_STOP_PURCHASE) || customFieldName.equalsIgnoreCase(Constants.CUSTOM_OBSOLETE)) && (customJSONObject.getInt("fieldtype") == Constants.CUSTOM_CHECKBOX)) {
                            if (!customJSONObject.isNull("fieldData") && !StringUtil.isNullOrEmpty(customJSONObject.optString("fieldData", "")) && customJSONObject.getString("fieldData").equalsIgnoreCase("true")) {
                                obj.put("isStopPurchase", true);
                                break;
                            }
                        }
                    }
                }
            }

            obj.put("type", type);
            obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
            if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || product.isIsBatchForProduct() || product.isIsSerialForProduct()) {
                ProductBatch batch = getBatchDetailsForProduct(product.getID());
                if (batch != null && batch.getLocation() != null && batch.getWarehouse() != null) {
                    obj.put("location", batch.getLocation().getId());
                    obj.put("warehouse", batch.getWarehouse().getId());
                } else {
                    obj.put("location", "");
                    obj.put("warehouse", "");
                }
            }
            KwlReturnObject result = accProductObj.getQuantity(product.getID());
            obj.put("quantity", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));

            KwlReturnObject result4 = accProductObj.getVendorConsignedQuantity(product.getID());
            obj.put("venconsignuomquantity", (result4.getEntityList().get(0) == null ? 0 : result4.getEntityList().get(0)));
            jArr.put(obj);
        }
        return jArr;
    }

    public JSONObject getProductJson(KwlReturnObject novendorproductresult, KwlReturnObject customervendorproductmapresult,JSONObject paramsjobj) throws SessionExpiredException, ServiceException, JSONException, UnsupportedEncodingException {
        String type = "";
        List novendorproductlist;
        JSONObject objfinal = new JSONObject();
        try {
            novendorproductlist = novendorproductresult.getEntityList();
            JSONArray jArray = new JSONArray();
            for (int cnt = 0; cnt < novendorproductlist.size(); cnt++) {
                JSONObject obj = new JSONObject();
                Product product = (Product) novendorproductlist.get(cnt);
                obj.put(Constants.productid, product.getID());
                obj.put("desc", product.getDescription());
                obj.put("supplierpartnumber", StringUtil.isNullOrEmpty(product.getSupplier()) ? "" : product.getSupplier());
                obj.put("multiuom", product.isMultiuom());
                obj.put("uomschematypeid", product.getUomSchemaType() != null ? product.getUomSchemaType().getID() : "");
                obj.put("isBatchForProduct", product.isIsBatchForProduct());
                obj.put("activateProductComposition", product.isActivateProductComposition());
                obj.put("isSerialForProduct", product.isIsSerialForProduct());
                obj.put("isRecyclable", product.isRecyclable());
                obj.put("rcmapplicable", product.isRcmApplicable());
                obj.put("recycleQuantity", product.getRecycleQuantity());
                obj.put("purchaseacctaxcode", ((product.getPurchaseAccount() != null && (!StringUtil.isNullOrEmpty(product.getPurchaseAccount().getTaxid()))) ? product.getPurchaseAccount().getTaxid() : ""));
                obj.put("salesacctaxcode", ((product.getSalesAccount() != null && (!StringUtil.isNullOrEmpty(product.getSalesAccount().getTaxid()))) ? product.getSalesAccount().getTaxid() : ""));
                obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : ""));
                obj.put("type", "Inventory Part");
                obj.put("salesretaccountid", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getID() : ""));
                KwlReturnObject purchase = accProductObj.getProductPrice(product.getID(), true, null, "", "");
                obj.put("purchaseprice", purchase.getEntityList().get(0));
                KwlReturnObject sales = accProductObj.getProductPrice(product.getID(), false, null, "", "");
                obj.put("saleprice", sales.getEntityList().get(0));

                if (product.getProducttype() != null) {
                    type = product.getProducttype().getName();
                }
                obj.put("type", type);
                UnitOfMeasure uom = product.getUnitOfMeasure();
                if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    obj.put("productID", product.getProductid());
                    obj.put("productidValue", product.getName());//"<font color='blue'>"+ product.getName()+"</font>");//to give color to a font
                    obj.put("uomname", uom == null ? "" : uom.getID());
                    obj.put("uomnameValue", uom == null ? "" : uom.getNameEmptyforNA());

                    if (product.getPackaging() != null && product.getPackaging().toString() != null) {
                        String packagingString = product.getPackaging().toString();
                        obj.put(Constants.packaging, packagingString);
                    } else {
                        obj.put(Constants.packaging, "");
                    }

                } else {
                    obj.put("pid", product.getProductid());
                    obj.put("productname", product.getName());//"<font color='blue'>"+ product.getName()+"</font>");//to give color to a font
                    obj.put("uomid", uom == null ? "" : uom.getID());
                    obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
                    obj.put(Constants.packaging, "");
                }
                obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
                if (product.isblockLooseSell()) {
                    KwlReturnObject result = accProductObj.getAvailableQuantityInSelectedUOM(product.getID(), product.getUnitOfMeasure().getID());
                    obj.put("quantity", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));
                } else {
                    KwlReturnObject result = accProductObj.getQuantity(product.getID());;
                    obj.put("quantity", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));
                }
                jArray.put(obj);
            }
            objfinal.put("objreturn", jArray);
        } catch (Exception ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getProductDetailJson : " + ex.getMessage(), ex);
        }
        return objfinal;
    }
    
    @Override
    public double getOutstandingPoSoProductsCount(List list, boolean isPo, String productid,
            AccountingHandlerDAO accountingHandlerDAOobj, accGoodsReceiptDAO accGoodsReceiptDAOobj, accInvoiceDAO accInvoiceDAOobj) throws JSONException, ServiceException, SessionExpiredException {
        double count = 0;
        try {
            Iterator itr1 = list.iterator();
            while (itr1.hasNext()) {
                Object[] oj = (Object[]) itr1.next();
                String orderid = oj[0].toString();
                //Withoutinventory 0 for normal, 1 for billing
                boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());
                if (isPo) {
                    if (!withoutinventory) {
                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), orderid);
                        PurchaseOrder purchaseOrder = (PurchaseOrder) objItr.getEntityList().get(0);

                        Set<PurchaseOrderDetail> orderDetail = purchaseOrder.getRows();
                        Iterator ite = orderDetail.iterator();
//                         String result = "Closed";
                        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), purchaseOrder.getCompany().getCompanyID());
                        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
                        double actualQty = 0;
                        double deliveredQty = 0;
                        while (ite.hasNext()) {
                            PurchaseOrderDetail pDetail = (PurchaseOrderDetail) ite.next();
                            if (productid.equals(pDetail.getProduct().getID())) {
                                actualQty = pDetail.getBaseuomquantity();
                                deliveredQty = 0;
                                if (pref.isWithInvUpdate()) { //In Trading Flow                 
                                    KwlReturnObject groresult = accGoodsReceiptDAOobj.getReceiptOrderDFromPODOptimized(pDetail.getID(), pref.getCompany().getCompanyID());
                                    List grolist = groresult.getEntityList();
                                    if (grolist.size() > 0) {
                                        Iterator ite1 = grolist.iterator();
                                        while (ite1.hasNext()) {
                                            String groOrderId = (String) ite1.next();
                                            KwlReturnObject res = accountingHandlerDAOobj.getObject(GoodsReceiptOrderDetails.class.getName(), groOrderId);
                                            GoodsReceiptOrderDetails goodsReceiptOrderDetails = (GoodsReceiptOrderDetails) res.getEntityList().get(0);
                                            deliveredQty += goodsReceiptOrderDetails.getBaseuomdeliveredquantity();
                                        }
                                    }
                                } else { //In Non Trading Flow 
                                    KwlReturnObject grresult = accGoodsReceiptDAOobj.getReceiptDFromPOD(pDetail.getID());
                                    List grList = grresult.getEntityList();
                                    Iterator ite1 = grList.iterator();
                                    while (ite1.hasNext()) {
                                        GoodsReceiptDetail ge = (GoodsReceiptDetail) ite1.next();
                                        //                qua += ge.getInventory().getQuantity();
//                                        deliveredQty += pref.isWithInvUpdate()? ge.getInventory().getQuantity() : ge.getInventory().getQuantity();
                                        deliveredQty += ge.getInventory().getBaseuomquantity();
                                    }
                                }
                                if (deliveredQty < actualQty) {
                                    count = count + (actualQty - deliveredQty);
                                }
                            }
                        }
                    }
                } else {
                    if (!withoutinventory) {
                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), orderid);
                        SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);

                        Set<SalesOrderDetail> orderDetail = salesOrder.getRows();
                        Iterator ite = orderDetail.iterator();

                        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), salesOrder.getCompany().getCompanyID());
                        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

                        double actualQty = 0;
                        double deliveredQty = 0;
                        boolean fullInv = false;
                        while (ite.hasNext()) {
                            SalesOrderDetail soDetail = (SalesOrderDetail) ite.next();
                            if (productid.equals(soDetail.getProduct().getID())) {
                                actualQty = soDetail.getBaseuomquantity();
                                deliveredQty = 0;
                                double quantPartTt = soDetail.getQuantity() * 100;
                                double quantPartTtInv = 0.0;
                                if (pref.isWithInvUpdate()) { //In Trading Flow                 
                                    KwlReturnObject doresult = accInvoiceDAOobj.getDeliveryOrderDFromSOD(soDetail.getID(), pref.getCompany().getCompanyID());
                                    List dodlist = doresult.getEntityList();
                                    if (dodlist.size() > 0) {
                                        Iterator ite1 = dodlist.iterator();
                                        while (ite1.hasNext()) {
                                            String dodOrderid = (String) ite1.next();
                                            KwlReturnObject res = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), dodOrderid);
                                            DeliveryOrderDetail deliveryOrderDetail = (DeliveryOrderDetail) res.getEntityList().get(0);
                                            fullInv = true;
                                            deliveredQty += deliveryOrderDetail.getBaseuomdeliveredquantity();
                                        }
                                    }
                                } else { //In Non Trading Flow 

                                    KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSOD(soDetail.getID());
                                    List invdlist = idresult.getEntityList();
                                    Iterator ite1 = invdlist.iterator();
                                    while (ite1.hasNext()) {
                                        InvoiceDetail ge = (InvoiceDetail) ite1.next();
                                        if (ge.getInvoice().isPartialinv()) {
                                            deliveredQty = ge.getInventory().getBaseuomquantity();
                                            quantPartTtInv += ge.getInventory().getQuantity() * ge.getPartamount();
                                        } else {
                                            fullInv = true;
                                            deliveredQty += ge.getInventory().getBaseuomquantity();
                                        }
                                    }
                                }

                                if (fullInv) {
                                    if (deliveredQty < actualQty) {
                                        count = count + (actualQty - deliveredQty);
                                    }
                                } else if (quantPartTt > quantPartTtInv) {
                                    count = count + (actualQty - deliveredQty);
                                }
                            }
                        }
//                          if(result.equalsIgnoreCase("Open")){
//                             for (SalesOrderDetail salesOrderDetail : orderDetail) {
//                                 if(productid.equals(salesOrderDetail.getProduct().getID())){
//                                     count=count+salesOrderDetail.getQuantity();
//                                 }
//                             }
//                         }
                    }
                }
            }
        } catch (Exception exception) {
            throw ServiceException.FAILURE("getProductsCount : " + exception.getMessage(), exception);
        }
        return count;
    }
    
       public JSONArray getCustomColumnData(String productId, String companyId) throws JSONException,Exception{
    
        JSONArray customJSONArray=new  JSONArray();
        
        int lineitem = 0;//we don't have line items so its value is 0
        String module = String.valueOf(Constants.Acc_Product_Master_ModuleId);
//        String itemId = request.getParameter("itemId");
        String[] moduleidarray = null; //we don't have multiple modules so its value is null and we have only one module i.e ItemMaster_ModuleId
        String commaSepratedModuleids = "";
        if (moduleidarray != null) {
            for (int i = 0; i < moduleidarray.length; i++) {
                if (!StringUtil.isNullOrEmpty(moduleidarray[i])) {
                    commaSepratedModuleids += moduleidarray[i] + ",";
                }
            }
            if (moduleidarray.length > 1) {
                commaSepratedModuleids = commaSepratedModuleids.substring(0, commaSepratedModuleids.length() - 1);
            }
        }
        HashMap<String, Object> requestParams = new HashMap<String, Object>();        
        String companyid = companyId;
        List<String> paramStrings = new ArrayList<String>();
        List<Object> paramValues = new ArrayList<Object>();
        Integer colcount = 1;
        if (StringUtil.isNullOrEmpty(commaSepratedModuleids) && StringUtil.isNullOrEmpty(module)) {                
                paramStrings.add(Constants.companyid);
                paramStrings.add(Constants.customcolumn);                
                requestParams.put(Constants.filter_names, paramStrings);
                //requestParams.put(CustomColumnConstants.filter_names, Arrays.asList(CustomColumnConstants.companyid,CustomColumnConstants.customcolumn));
                
                paramValues.add(companyid);
                paramValues.add(lineitem);                
                requestParams.put(Constants.filter_values, paramValues);
                //requestParams.put(Constants.filter_values, Arrays.asList(companyid,lineitem));
            
            } else if (StringUtil.isNullOrEmpty(commaSepratedModuleids)) {
                Integer moduleid = Integer.parseInt(module);
               if(moduleid>99){  //Added module >100 for Report like ledger, Balance sheet etc
               if(moduleid==101){ 
                    paramStrings.add(Constants.companyid);
                    requestParams.put(Constants.filter_names, paramStrings);
                    //requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid));
                
                    paramValues.add(companyid);
                    requestParams.put(Constants.filter_values, paramValues);
                    //requestParams.put(Constants.filter_values, Arrays.asList(companyid));
                
               }else{
                   paramStrings.add(Constants.companyid);
                   paramStrings.add(Constants.customcolumn);                
                   requestParams.put(Constants.filter_names, paramStrings);
                   //requestParams.put(CustomColumnConstants.filter_names, Arrays.asList(CustomColumnConstants.companyid,CustomColumnConstants.customcolumn));
                
                   paramValues.add(companyid);
                   paramValues.add(lineitem);                
                   requestParams.put(Constants.filter_values, paramValues);
                   //requestParams.put(Constants.filter_values, Arrays.asList(companyid,lineitem));                                      
               }
               }else{
                    paramStrings.add(Constants.companyid);
                    paramStrings.add(Constants.moduleid);
                    paramStrings.add(Constants.customcolumn);                
                    requestParams.put(Constants.filter_names, paramStrings);
                    //requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));                

                    paramValues.add(companyid);
                    paramValues.add(moduleid);
                    paramValues.add(lineitem);                
                    requestParams.put(Constants.filter_values, paramValues);                                
                    //requestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid,lineitem));                                   
               }
            } else {
                paramStrings.add(Constants.companyid);
                paramStrings.add(Constants.INmoduleid);
                paramStrings.add(Constants.customcolumn);                
                requestParams.put(Constants.filter_names, paramStrings);
                //requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.INmoduleid,Constants.customcolumn));                
                
                paramValues.add(companyid);
                paramValues.add(commaSepratedModuleids);
                paramValues.add(lineitem);                
                requestParams.put(Constants.filter_values, paramValues);                                
                //requestParams.put(Constants.filter_values, Arrays.asList(companyid, commaSepratedModuleids,lineitem));
            }
             KwlReturnObject result = accAccountDAOobj.getFieldParams(requestParams);             
             List lst=result.getEntityList();
             colcount = lst.size();
             Iterator ite = lst.iterator();
            while (ite.hasNext()) {
                FieldParams tmpcontyp = (FieldParams) ite.next();
                JSONObject jobj = new JSONObject();
                jobj.put("fieldname", tmpcontyp.getFieldname());
                AccProductCustomData accProductCustomData=new AccProductCustomData();
                KwlReturnObject custumObjresult = null;
                if (!StringUtil.isNullOrEmpty(productId)) {                    
                    try {
                        custumObjresult = accountingHandlerDAOobj.getObject(AccProductCustomData.class.getName(), productId);
                    } catch (Exception e) {
                    }
                    accProductCustomData = (AccProductCustomData) custumObjresult.getEntityList().get(0);
                        if (accProductCustomData != null) {
                            String coldata = accProductCustomData.getCol(tmpcontyp.getColnum());
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                jobj.put("fieldData", coldata);
                            }
                        }                                        
                }
                
                if(StringUtil.isNullOrEmpty(module)){
                    if(lineitem==0)
                        jobj.put("fieldlabel", tmpcontyp.getFieldlabel()+"(Product Master)");
                    else 
                        jobj.put("fieldlabel", tmpcontyp.getFieldlabel());
                }else{
                     Integer moduleid = Integer.parseInt(module);
                     if(moduleid>99){ 
                         jobj.put("fieldlabel", tmpcontyp.getFieldlabel()+"(Product Master)");
                     }else
                     jobj.put("fieldlabel", tmpcontyp.getFieldlabel());
                }
                jobj.put("isessential", tmpcontyp.getIsessential());
                jobj.put("maxlength", tmpcontyp.getMaxlength());
                jobj.put("validationtype", tmpcontyp.getValidationtype());
                jobj.put("fieldid", tmpcontyp.getId());
                jobj.put("moduleid", tmpcontyp.getModuleid());
                jobj.put("fieldtype", tmpcontyp.getFieldtype());
                jobj.put("iseditable", tmpcontyp.getIseditable());
                jobj.put("comboid", tmpcontyp.getComboid());
                jobj.put("comboname", tmpcontyp.getComboname());
                jobj.put("moduleflag", tmpcontyp.getModuleflag());
                jobj.put("refcolumn_number", Constants.Custom_column_Prefix + tmpcontyp.getRefcolnum());
                jobj.put("column_number", Constants.Custom_column_Prefix + tmpcontyp.getColnum());
                jobj.put("sendnotification", tmpcontyp.getsendNotification());
                jobj.put("notificationdays", tmpcontyp.getnotificationDays());
                jobj.put("iscustomfield", tmpcontyp.getCustomfield()==1 ? true : false);
                jobj.put("iscustomcolumn", tmpcontyp.getCustomcolumn()==1 ? true : false);
                customJSONArray.put(jobj);
            }
            
        return customJSONArray; 
    }
       
     public ProductBatch getBatchDetailsForProduct(String productid) throws ServiceException {
        ProductBatch productBatch = new ProductBatch();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("filter_names", Arrays.asList("product"));
        requestParams.put("filter_values", productid);
        KwlReturnObject kmsg = accCommonTablesDAO.getBatch(requestParams);
        List list = kmsg.getEntityList();
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            productBatch = (ProductBatch) iter.next();
        }
        return productBatch;
    } 
     
 public JSONObject getIndividualProductPrice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateOnlyFormat = authHandler.getDateOnlyFormatter(request);
            String date = (String) requestParams.get("transactiondate");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean carryin = Boolean.parseBoolean(request.getParameter("carryin"));
            Date transactionDate = null;
            try {
                transactionDate = (date == null ? null : df.parse(date));
            } catch (ParseException ex) {
                Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE("getProducts : "+ex.getMessage(), ex);
            }

            String productid[] = ((String)requestParams.get(Constants.productid)).split(",");
            
            for(int i=0; i<productid.length; i++) {
                JSONObject obj = new JSONObject();
                KwlReturnObject result = accProductObj.getProductPrice(productid[i], carryin, transactionDate, (String)requestParams.get("affecteduser"),"");
                List list = result.getEntityList();
                Iterator itr = list.iterator();
                if(itr.hasNext()) {
                    Object row = itr.next();
                    if(row == null){
                        obj.put("price", 0);
                    } else {
                        obj.put("price", row);
                    }
                } else {
                    obj.put("price", 0);
                }
                obj.put(Constants.productid, productid[i]);
                jobj.append("data", obj);
            }
            if (productid.length == 1) {
                
                transactionDate = (transactionDate==null)?new Date():transactionDate;
                HashMap<String, Object> basicParams = new HashMap();
                basicParams.put("productId", productid[0]);
                basicParams.put("companyId", companyid);
                basicParams.put("transactionDate", transactionDate);
                basicParams.put("dateFormat", dateOnlyFormat);
                
                HashMap<String, Object> fieldrequestParams = new HashMap();
                Map<String, Object> variableMap = new HashMap<String, Object>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                AccProductCustomData obj = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), productid[0]);
                if (obj != null) {
                    productHandler.setCustomColumnValuesForProduct(obj, FieldMap, replaceFieldMap, variableMap);
                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                            String coldata = varEntry.getValue().toString();
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                JSONObject jsonObj = new JSONObject();
                                jsonObj.put(varEntry.getKey(), coldata);
                                jsonObj.put("key", varEntry.getKey());
                                jobj.append("data", jsonObj);
                            }
                        }
                    }
                
                 
            }
            //jobj = productHandler.getIndividualProductsPriceJson(request, listFinal);

            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }   
     /**
      * 
      * @param jSONObject = Put values for GR custom column 
      * @throws ServiceException
      * @throws JSONException 
      */
    public void putGoodsReeiptCustomData(JSONObject jSONObject) throws ServiceException, JSONException {
        String companyid = jSONObject.optString("companyId");
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        Map<String, Object> variableMap = new HashMap<String, Object>();
        String recId = jSONObject.optString("recId");
        String detailId = jSONObject.optString("detailId");
        GoodsReceiptOrder goodsReceiptOrder = null;
        GoodsReceiptOrderDetails goodsReceiptOrderDetails = null;
        GoodsReceiptOrderCustomData goodsReceiptOrderCustomData = null;
        GoodsReceiptOrderDetailsCustomDate goodsReceiptOrderDetailsCustomDate = null;
        if (!StringUtil.isNullOrEmpty(recId)) {
            goodsReceiptOrder = (GoodsReceiptOrder) kwlCommonTablesDAOObj.getClassObject(GoodsReceiptOrder.class.getName(), recId);
}
        if (!StringUtil.isNullOrEmpty(detailId)) {
            goodsReceiptOrderDetails = (GoodsReceiptOrderDetails) kwlCommonTablesDAOObj.getClassObject(GoodsReceiptOrderDetails.class.getName(), detailId);
        }
        if (goodsReceiptOrder != null) {
            goodsReceiptOrderCustomData = (GoodsReceiptOrderCustomData) goodsReceiptOrder.getGoodsReceiptOrderCustomData();
        }
        if (goodsReceiptOrderDetails != null) {
            goodsReceiptOrderDetailsCustomDate = (GoodsReceiptOrderDetailsCustomDate) goodsReceiptOrderDetails.getGoodsReceiptOrderDetailsCustomDate();
        }
        if (goodsReceiptOrderCustomData != null) {
            fieldrequestParams = new HashMap();
            replaceFieldMap1 = new HashMap<String, String>();
            customFieldMap = new HashMap<String, String>();
            customDateFieldMap = new HashMap<String, String>();
            variableMap = new HashMap<String, Object>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Goods_Receipt_ModuleId));
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            AccountingManager.setCustomColumnValues(goodsReceiptOrderCustomData, fieldMap, replaceFieldMap, variableMap);
            JSONObject params = new JSONObject();
            params.put("companyid", companyid);
            params.put("isExport", true);
            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
        }
        if (goodsReceiptOrderDetailsCustomDate != null) {
            fieldrequestParams = new HashMap();
            replaceFieldMap1 = new HashMap<String, String>();
            customFieldMap = new HashMap<String, String>();
            customDateFieldMap = new HashMap<String, String>();
            variableMap = new HashMap<String, Object>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Goods_Receipt_ModuleId, 1));
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            AccountingManager.setCustomColumnValues(goodsReceiptOrderDetailsCustomDate, fieldMap, replaceFieldMap, variableMap);
            JSONObject params = new JSONObject();
            params.put("companyid", companyid);
            params.put("isExport", true);
            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
        }
    }
     /**
      * 
      * @param jSONObject = Put values for DO custom column 
      * @throws ServiceException
      * @throws JSONException 
      */
    public void putDeliveryOrderCustomData(JSONObject jSONObject) throws ServiceException, JSONException {
        String companyid = jSONObject.optString("companyId");
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        Map<String, Object> variableMap = new HashMap<String, Object>();
        String recId = jSONObject.optString("recId");
        String detailId = jSONObject.optString("detailId");
        DeliveryOrder deliveryOrder = null;
        DeliveryOrderDetail deliveryOrderDetail = null;
        DeliveryOrderCustomData deliveryOrderCustomData = null;
        DeliveryOrderDetailCustomData deliveryOrderDetailCustomData = null;
        if (!StringUtil.isNullOrEmpty(recId)) {
            deliveryOrder = (DeliveryOrder) kwlCommonTablesDAOObj.getClassObject(DeliveryOrder.class.getName(), recId);
        }
        if (!StringUtil.isNullOrEmpty(detailId)) {
            deliveryOrderDetail = (DeliveryOrderDetail) kwlCommonTablesDAOObj.getClassObject(DeliveryOrderDetail.class.getName(), detailId);
        }
        if (deliveryOrder != null) {
            deliveryOrderCustomData = (DeliveryOrderCustomData) deliveryOrder.getDeliveryOrderCustomData();
        }
        if (deliveryOrderDetail != null) {
            deliveryOrderDetailCustomData = (DeliveryOrderDetailCustomData) deliveryOrderDetail.getDeliveryOrderDetailCustomData();
        }
        if (deliveryOrderCustomData != null) {
            fieldrequestParams = new HashMap();
            replaceFieldMap1 = new HashMap<String, String>();
            customFieldMap = new HashMap<String, String>();
            customDateFieldMap = new HashMap<String, String>();
            variableMap = new HashMap<String, Object>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Delivery_Order_ModuleId));
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            AccountingManager.setCustomColumnValues(deliveryOrderCustomData, fieldMap, replaceFieldMap, variableMap);
            JSONObject params = new JSONObject();
            params.put("companyid", companyid);
            params.put("isExport", true);
            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
        }
        if (deliveryOrderDetailCustomData != null) {
            fieldrequestParams = new HashMap();
            replaceFieldMap1 = new HashMap<String, String>();
            customFieldMap = new HashMap<String, String>();
            customDateFieldMap = new HashMap<String, String>();
            variableMap = new HashMap<String, Object>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Delivery_Order_ModuleId, 1));
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            AccountingManager.setCustomColumnValues(deliveryOrderDetailCustomData, fieldMap, replaceFieldMap, variableMap);
            JSONObject params = new JSONObject();
            params.put("companyid", companyid);
            params.put("isExport", true);
            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
        }
    }
     /**
      * 
      * @param jSONObject = Put values for Purchase return custom column 
      * @throws ServiceException
      * @throws JSONException 
      */
    public void putPurchaseReturnCustomData(JSONObject jSONObject) throws ServiceException, JSONException {
        String companyid = jSONObject.optString("companyId");
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        Map<String, Object> variableMap = new HashMap<String, Object>();
        String recId = jSONObject.optString("recId");
        String detailId = jSONObject.optString("detailId");
        PurchaseReturn purchaseReturn = null;
        PurchaseReturnDetail purchaseReturnDetail = null;
        PurchaseReturnCustomData purchaseReturnCustomData = null;
        PurchaseReturnDetailCustomDate purchaseReturnDetailCustomDate = null;
        if (!StringUtil.isNullOrEmpty(recId)) {
            purchaseReturn = (PurchaseReturn) kwlCommonTablesDAOObj.getClassObject(PurchaseReturn.class.getName(), recId);
        }
        if (!StringUtil.isNullOrEmpty(detailId)) {
            purchaseReturnDetail = (PurchaseReturnDetail) kwlCommonTablesDAOObj.getClassObject(PurchaseReturnDetail.class.getName(), detailId);
        }
        if (purchaseReturn != null) {
            purchaseReturnCustomData = (PurchaseReturnCustomData) purchaseReturn.getPurchaseReturnCustomData();
        }
        if (purchaseReturnDetail != null) {
            purchaseReturnDetailCustomDate = (PurchaseReturnDetailCustomDate) purchaseReturnDetail.getPurchaseReturnDetailCustomDate();
        }
        if (purchaseReturnCustomData != null) {
            fieldrequestParams = new HashMap();
            replaceFieldMap1 = new HashMap<String, String>();
            customFieldMap = new HashMap<String, String>();
            customDateFieldMap = new HashMap<String, String>();
            variableMap = new HashMap<String, Object>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Purchase_Return_ModuleId));
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            AccountingManager.setCustomColumnValues(purchaseReturnCustomData, fieldMap, replaceFieldMap, variableMap);
            JSONObject params = new JSONObject();
            params.put("companyid", companyid);
            params.put("isExport", true);
            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
        }
        if (purchaseReturnDetailCustomDate != null) {
            fieldrequestParams = new HashMap();
            replaceFieldMap1 = new HashMap<String, String>();
            customFieldMap = new HashMap<String, String>();
            customDateFieldMap = new HashMap<String, String>();
            variableMap = new HashMap<String, Object>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Purchase_Return_ModuleId, 1));
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            AccountingManager.setCustomColumnValues(purchaseReturnDetailCustomDate, fieldMap, replaceFieldMap, variableMap);
            JSONObject params = new JSONObject();
            params.put("companyid", companyid);
            params.put("isExport", true);
            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
        }
    }
     /**
      * 
      * @param jSONObject = Put values for sales return custom column 
      * @throws ServiceException
      * @throws JSONException 
      */
    public void putSalesReturnCustomData(JSONObject jSONObject) throws ServiceException, JSONException {
        String companyid = jSONObject.optString("companyId");
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        Map<String, Object> variableMap = new HashMap<String, Object>();
        String recId = jSONObject.optString("recId");
        String detailId = jSONObject.optString("detailId");
        SalesReturn salesReturn = null;
        SalesReturnDetail salesReturnDetail = null;
        SalesReturnCustomData salesReturnCustomData = null;
        SalesReturnDetailCustomData salesReturnDetailCustomData = null;
        if (!StringUtil.isNullOrEmpty(recId)) {
            salesReturn = (SalesReturn) kwlCommonTablesDAOObj.getClassObject(SalesReturn.class.getName(), recId);
        }
        if (!StringUtil.isNullOrEmpty(detailId)) {
            salesReturnDetail = (SalesReturnDetail) kwlCommonTablesDAOObj.getClassObject(SalesReturnDetail.class.getName(), detailId);
        }
        if (salesReturn != null) {
            salesReturnCustomData = (SalesReturnCustomData) salesReturn.getSalesReturnCustomData();
        }
        if (salesReturnDetail != null) {
            salesReturnDetailCustomData = (SalesReturnDetailCustomData) salesReturnDetail.getSalesReturnDetailCustomData();
        }
        if (salesReturnCustomData != null) {
            fieldrequestParams = new HashMap();
            replaceFieldMap1 = new HashMap<String, String>();
            customFieldMap = new HashMap<String, String>();
            customDateFieldMap = new HashMap<String, String>();
            variableMap = new HashMap<String, Object>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Sales_Return_ModuleId));
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            AccountingManager.setCustomColumnValues(salesReturnCustomData, fieldMap, replaceFieldMap, variableMap);
            JSONObject params = new JSONObject();
            params.put("companyid", companyid);
            params.put("isExport", true);
            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
        }
        if (salesReturnDetailCustomData != null) {
            fieldrequestParams = new HashMap();
            replaceFieldMap1 = new HashMap<String, String>();
            customFieldMap = new HashMap<String, String>();
            customDateFieldMap = new HashMap<String, String>();
            variableMap = new HashMap<String, Object>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Sales_Return_ModuleId, 1));
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            AccountingManager.setCustomColumnValues(salesReturnDetailCustomData, fieldMap, replaceFieldMap, variableMap);
            JSONObject params = new JSONObject();
            params.put("companyid", companyid);
            params.put("isExport", true);
            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
        }
    }
         /**
      * 
      * @param jSONObject = Put values for Product custom column 
      * @throws ServiceException
      * @throws JSONException 
      */
    public void putProductCustomData(JSONObject jSONObject) throws ServiceException, JSONException {
        String companyid = jSONObject.optString("companyId");
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        Map<String, Object> variableMap = new HashMap<String, Object>();
        String recId = jSONObject.optString("recId");
        String detailId = jSONObject.optString("detailId");
        Product product = null;
        AccProductCustomData accProductCustomData = null;
        if (!StringUtil.isNullOrEmpty(recId)) {
            product = (Product) kwlCommonTablesDAOObj.getClassObject(Product.class.getName(), recId);
        }
        if (product != null) {
            accProductCustomData = (AccProductCustomData) product.getProductCustomData();
        }
        if (accProductCustomData != null) {
            fieldrequestParams = new HashMap();
            replaceFieldMap1 = new HashMap<String, String>();
            customFieldMap = new HashMap<String, String>();
            customDateFieldMap = new HashMap<String, String>();
            variableMap = new HashMap<String, Object>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId));
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            AccountingManager.setCustomColumnValues(accProductCustomData, fieldMap, replaceFieldMap, variableMap);
            JSONObject params = new JSONObject();
            params.put("companyid", companyid);
            params.put("isExport", true);
            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
        }
    }
    /**
     *
     * @param jSONObject = Put values for Stock Adjustment custom column
     * @throws ServiceException
     * @throws JSONException
     */
    public void putStockAdjustmentCustomData(JSONObject jSONObject) throws ServiceException, JSONException {
        String companyid = jSONObject.optString("companyId");
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        Map<String, Object> variableMap = new HashMap<String, Object>();
        String recId = jSONObject.optString("recId");
        String detailId = jSONObject.optString("detailId");
        StockAdjustment stockAdjustment = null;
        StockAdjustmentCustomData stockAdjustmentCustomData = null;
        if (!StringUtil.isNullOrEmpty(recId)) {
            stockAdjustment = (StockAdjustment) kwlCommonTablesDAOObj.getClassObject(StockAdjustment.class.getName(), recId);
        }
        if (stockAdjustment != null) {
            stockAdjustmentCustomData = (StockAdjustmentCustomData) stockAdjustment.getStockAdjustmentCustomData();
        }
        if (stockAdjustmentCustomData != null) {
            fieldrequestParams = new HashMap();
            replaceFieldMap1 = new HashMap<String, String>();
            customFieldMap = new HashMap<String, String>();
            customDateFieldMap = new HashMap<String, String>();
            variableMap = new HashMap<String, Object>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Inventory_Stock_Adjustment_ModuleId));
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            AccountingManager.setCustomColumnValues(stockAdjustmentCustomData, fieldMap, replaceFieldMap, variableMap);
            JSONObject params = new JSONObject();
            params.put("companyid", companyid);
            params.put("isExport", true);
            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
        }
    }

    /**
     *
     * @param jSONObject = Put values for Inter Store custom column
     * @throws ServiceException
     * @throws JSONException
     */
    public void putInterStoreTransferCustomData(JSONObject jSONObject) throws ServiceException, JSONException {
        String companyid = jSONObject.optString("companyId");
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        Map<String, Object> variableMap = new HashMap<String, Object>();
        String recId = jSONObject.optString("recId");
        String detailId = jSONObject.optString("detailId");
        InterStoreTransferCustomData interStoreTransferCustomData = null;
        InterStoreTransferRequest interStoreTransferRequest = null;
        if (!StringUtil.isNullOrEmpty(recId)) {
            interStoreTransferRequest = (InterStoreTransferRequest) kwlCommonTablesDAOObj.getClassObject(InterStoreTransferRequest.class.getName(), recId);
        }
        if (interStoreTransferRequest != null) {
            interStoreTransferCustomData = (InterStoreTransferCustomData) interStoreTransferRequest.getISTCustomData();
        }
        if (interStoreTransferCustomData != null) {
            fieldrequestParams = new HashMap();
            replaceFieldMap1 = new HashMap<String, String>();
            customFieldMap = new HashMap<String, String>();
            customDateFieldMap = new HashMap<String, String>();
            variableMap = new HashMap<String, Object>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_InterStore_ModuleId));
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            AccountingManager.setCustomColumnValues(interStoreTransferCustomData, fieldMap, replaceFieldMap, variableMap);
            JSONObject params = new JSONObject();
            params.put("companyid", companyid);
            params.put("isExport", true);
            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
        }
    }
    
    /**
     *
     * @param requestMap= contains required params
     * @return = Return product details
     * @throws ServiceException
     */
    public JSONObject getProductSummary(Map<String, Object> requestMap) throws ServiceException {
        JSONArray jSONArray = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        try {
            String companyId = "";
            String productId = "";
            int moduleId = 0;
            String start = "";
            String limit = "";
            boolean isExport = false;
            if (requestMap.containsKey("productId")) {
                productId = requestMap.get("productId").toString();
            }
            if (requestMap.containsKey("companyId")) {
                companyId = requestMap.get("companyId").toString();
            }
            if (requestMap.containsKey("type")) {
                moduleId = Integer.parseInt(requestMap.get("type").toString());
            }
            if (requestMap.containsKey("start")) {
                start = requestMap.get("start").toString();
            }
            if (requestMap.containsKey("limit")) {
                limit = requestMap.get("limit").toString();
            }
            if (requestMap.containsKey("isExport")) {
                isExport = Boolean.parseBoolean(requestMap.get("isExport").toString());
            }
            requestMap.put("isApproved", true);
            KwlReturnObject result = null;
            if (moduleId == Constants.Acc_Sales_Order_ModuleId || moduleId == 0) {
                result = accSalesOrderDAOobj.getSO_Product(requestMap);  // get Sales Order
                List<SalesOrderDetail> list = result.getEntityList();
                putSalesOrderDetails(list, jSONArray, requestMap);
            }
            if (moduleId == Constants.Acc_Purchase_Order_ModuleId || moduleId == 0) {
                result = accPurchaseOrderobj.getPO_Product(requestMap); //get Purchase Order
                List<PurchaseOrderDetail> list = result.getEntityList();
                putPurchaseOrderDetails(list, jSONArray, requestMap);
            }
            if (moduleId == Constants.Acc_Invoice_ModuleId || moduleId == 0) {
                result = accInvoiceDAOobj.getInvoice_Product(requestMap);  // get Customer Invoice
                List<InvoiceDetail> list = result.getEntityList();
                putCustomerInvoiceDetails(list, jSONArray, requestMap);
            }
            if (moduleId == Constants.Acc_Vendor_Invoice_ModuleId || moduleId == 0) {
                result = accGoodsReceiptDAOobj.getGoodsReceipt_Product(requestMap); // get Vendor Invoice
                List<GoodsReceiptDetail> list = result.getEntityList();
                putVendorInvoiceDetails(list, jSONArray, requestMap);
            }
            if (moduleId == Constants.Acc_Goods_Receipt_ModuleId || moduleId == 0) {
                result = accGoodsReceiptDAOobj.getGR_Product(requestMap); // get Goods Receipt
                List<GoodsReceiptOrderDetails> list = result.getEntityList();
                putGoodsReceiptOrderDetails(list, jSONArray, requestMap);
            }
            if (moduleId == Constants.Acc_Delivery_Order_ModuleId || moduleId == 0) {
                result = accInvoiceDAOobj.getDO_Product(requestMap); //get Delivery Order
                List<DeliveryOrderDetail> list = result.getEntityList();
                putDeliveryOrderDetails(list, jSONArray, requestMap);
            }
            if (moduleId == Constants.Inventory_Stock_Adjustment_ModuleId || moduleId == 0) {
                result = accProductObj.getStockAdjustment_Product(requestMap); //get Stock Adjustment
                List<StockAdjustment> list = result.getEntityList();
                putStockAdjustmentDetails(list, jSONArray, requestMap);
            }
             if (moduleId == Constants.Acc_Customer_Quotation_ModuleId || moduleId == 0) {
                result = accPurchaseOrderobj.getCQ_Product(requestMap); //get Customer Quotation
                List<QuotationDetail> list = result.getEntityList();
                putCustomerQuotationDetails(list, jSONArray, requestMap);
            }
            if (moduleId == Constants.Acc_Vendor_Quotation_ModuleId || moduleId == 0) {
                result = accPurchaseOrderobj.getVQ_Product(requestMap); //get Vendor Quotation
                List<VendorQuotationDetail> list = result.getEntityList();
                putVendorQuotationDetails(list, jSONArray, requestMap);
            }
            if (moduleId == Constants.Acc_Purchase_Requisition_ModuleId || moduleId == 0) {
                result = accPurchaseOrderobj.getPReq_Product(requestMap); //get Customer Quotation
                List<PurchaseRequisitionDetail> list = result.getEntityList();
                putPurchaseRequisitionDetails(list, jSONArray, requestMap);
            }
            if (moduleId == Constants.Acc_RFQ_ModuleId || moduleId == 0) {
                result = accPurchaseOrderobj.getRFQ_Product(requestMap); //get Vendor Quotation
                List<RequestForQuotationDetail> list = result.getEntityList();
                putRFQDetails(list, jSONArray, requestMap);
            }
            if (moduleId == Constants.Acc_Sales_Return_ModuleId || moduleId == 0) {
                result = accPurchaseOrderobj.getSR_Product(requestMap); //get Vendor Quotation
                List<SalesReturnDetail> list = result.getEntityList();
                putSalesReturnDetails(list, jSONArray, requestMap);
            }
            if (moduleId == Constants.Acc_Purchase_Return_ModuleId || moduleId == 0) {
                result = accPurchaseOrderobj.getPR_Product(requestMap); //get Vendor Quotation
                List<PurchaseReturnDetail> list = result.getEntityList();
                putPurchaseReturnDetails(list, jSONArray, requestMap);
            }
            /*
            Sort Json array
            */
            jSONArray=sortJsonByDate(jSONArray,requestMap);
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray pagedJson = jSONArray;
            Locale locale = (Locale) requestMap.get("locale");  //ERP-24382
            createColumnModelForProductSummary(jarrColumns, jarrRecords,locale);
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            commData.put("success", true);
            commData.put("coldata", pagedJson);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", jSONArray.length());
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);
            jSONObject.put("valid", true);

            if (isExport) {
                jSONObject.put("data", jSONArray);
            } else {
                jSONObject.put("data", commData);
            }

        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jSONObject;
    }

    /**
     *
     * @param jarrColumns= Column Model for product summary report
     * @param jarrRecords = Records for same
     * @param locale
     * @throws JSONException
     */
    public void createColumnModelForProductSummary(JSONArray jarrColumns, JSONArray jarrRecords ,Locale locale) throws JSONException {
        // Column Model

        JSONObject jobjTemp = new JSONObject();

        // View Case Records
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "isLeaseFixedAsset");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "isConsignment");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "fixedAssetLeaseInvoice");
        jarrRecords.put(jobjTemp);

        jobjTemp.put("name", "date");
        jobjTemp.put("type", "date");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.procduct.trans.header.1", null, locale));
        jobjTemp.put("dataIndex", "date");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("sortable", true);
        jobjTemp.put("renderer", "WtfGlobal.onlyDateDeletedRenderer");
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "type");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.procduct.trans.header.2", null, locale));
        jobjTemp.put("dataIndex", "type");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("sortable", true);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "transactionno");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.procduct.trans.header.3", null, locale));
        jobjTemp.put("dataIndex", "transactionno");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("sortable", true);
        jobjTemp.put("renderer", "function(v,m,rec) {\n"
                + "        if(rec.data.isInventory){\n"
                + "              v=\"<span class='EntryNumPedding'>\"+v+\"</span>\";\n"
                + "        }else{\n"
                + "            v= \"<a class='jumplink' href='#'>\"+v+\"</a>\";\n"
                + "        }\n"
                + "        return v;\n"
                + "    }");
//        jobjTemp.put("renderer", "WtfGlobal.linkDeletedRenderer1");

        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "name");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.procduct.trans.header.4", null, locale));
        jobjTemp.put("dataIndex", "name");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("sortable", true);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "quantity");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.procduct.trans.header.5", null, locale));
        jobjTemp.put("dataIndex", "quantity");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("align", "right");
        jobjTemp.put("renderer", "function(value){\n"
                + "                return parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);\n"
                + "            }");
        jobjTemp.put("sortable", true);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "rate");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.procduct.trans.header.6", null, locale));
        jobjTemp.put("dataIndex", "rate");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("align", "right");
        jobjTemp.put("renderer", "function(v,m,rec){\n"
                + "if(!Wtf.dispalyUnitPriceAmountInSales || !Wtf.dispalyUnitPriceAmountInPurchase) {\n"
                + "   return Wtf.UpriceAndAmountDisplayValue;\n"
                + "} else{\n"
                + "   return WtfGlobal.withCurrencyUnitPriceRenderer(v,false,rec);\n"
                + "}\n"
                + "}");
        jobjTemp.put("sortable", true);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "amount");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.procduct.trans.header.7", null, locale));
        jobjTemp.put("dataIndex", "amount");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("align", "right");
        jobjTemp.put("renderer", "function(v,m,rec){\n"
                + "if(!Wtf.dispalyUnitPriceAmountInSales || !Wtf.dispalyUnitPriceAmountInPurchase) {\n"
                + "   return Wtf.UpriceAndAmountDisplayValue;\n"
                + "} else {\n"
                + "   return WtfGlobal.withoutRateCurrencyDeletedSymbol(v,m,rec);\n"
                + "}\n"
                + "}");
        jobjTemp.put("sortable", true);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "currencysymbol");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "moduleid");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "isInventory");
        jarrRecords.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "billid");
        jarrRecords.put(jobjTemp);

    }

    /**
     *
     * @param list = List of Sales Order Details
     * @param jSONArray = Put data in Array
     * @param requestMap
     */
    public void putSalesOrderDetails(List<SalesOrderDetail> list, JSONArray jSONArray, Map<String, Object> requestMap) {
        try {
            DateFormat df = null;
            String companyid = (String) requestMap.get("companyid");
            if (requestMap.containsKey("df")) {
                df = (DateFormat) requestMap.get("df");
            }
            for (SalesOrderDetail salesOrderDetail : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "Sales Order");
                jSONObject.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                jSONObject.put("billid", salesOrderDetail.getSalesOrder().getID());
                jSONObject.put("transactionno", salesOrderDetail.getSalesOrder().getSalesOrderNumber());
                jSONObject.put("name", salesOrderDetail.getSalesOrder().getCustomer().getName());
                jSONObject.put("date", df != null ? df.format(salesOrderDetail.getSalesOrder().getOrderDate()) : salesOrderDetail.getSalesOrder().getOrderDate());
                jSONObject.put("quantity", salesOrderDetail.getQuantity());
                jSONObject.put("currencysymbol", salesOrderDetail.getSalesOrder().getCurrency().getSymbol());
                jSONObject.put("rate", salesOrderDetail.getRate());
                jSONObject.put("amount", authHandler.round(salesOrderDetail.getQuantity() * salesOrderDetail.getRate(), companyid));
                jSONObject.put("isConsignment", salesOrderDetail.getSalesOrder().isIsconsignment());
                jSONObject.put("isLeaseFixedAsset", salesOrderDetail.getSalesOrder().getLeaseOrMaintenanceSO()==1?true:false);
                jSONArray.put(jSONObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param list = List of customer Quotation Details
     * @param jSONArray = Put data in Array
     * @param requestMap
     */
    public void putCustomerQuotationDetails(List<QuotationDetail> list, JSONArray jSONArray, Map<String, Object> requestMap) {
        try {
            DateFormat df = null;
            String companyid = (String) requestMap.get("companyid");
            if (requestMap.containsKey("df")) {
                df = (DateFormat) requestMap.get("df");
            }
            for (QuotationDetail quotationDetail : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "Customer Quotation");
                jSONObject.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                jSONObject.put("billid", quotationDetail.getQuotation().getID());
                jSONObject.put("transactionno", quotationDetail.getQuotation().getQuotationNumber());
                jSONObject.put("name", quotationDetail.getQuotation().getCustomer().getName());
                jSONObject.put("date", df != null ? df.format(quotationDetail.getQuotation().getQuotationDate()) : quotationDetail.getQuotation().getQuotationDate());
                jSONObject.put("quantity", quotationDetail.getQuantity());
                jSONObject.put("currencysymbol",  quotationDetail.getQuotation().getCurrency().getSymbol());
                jSONObject.put("rate", quotationDetail.getRate());
                jSONObject.put("amount", authHandler.round(quotationDetail.getQuantity() * quotationDetail.getRate(), companyid));
                jSONObject.put("isLeaseFixedAsset", quotationDetail.getQuotation().isLeaseQuotation()==true?true:false);
                jSONArray.put(jSONObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  /**
     *
     * @param list = List of vendor  Quotation Details
     * @param jSONArray = Put data in Array
     * @param requestMap
     */
    public void putVendorQuotationDetails(List<VendorQuotationDetail> list, JSONArray jSONArray, Map<String, Object> requestMap) {
        try {
            DateFormat df = null;
            String companyid = (String) requestMap.get("companyid");
            if (requestMap.containsKey("df")) {
                df = (DateFormat) requestMap.get("df");
            }
            for (VendorQuotationDetail vqdetail : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "Vendor Quotation");
                jSONObject.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
                jSONObject.put("billid", vqdetail.getVendorquotation().getID());
                jSONObject.put("transactionno", vqdetail.getVendorquotation().getQuotationNumber());
                jSONObject.put("name", vqdetail.getVendorquotation().getVendor().getName());
                jSONObject.put("date", df != null ? df.format(vqdetail.getVendorquotation().getQuotationDate()) : vqdetail.getVendorquotation().getQuotationDate());
                jSONObject.put("quantity", vqdetail.getQuantity());
                jSONObject.put("currencysymbol", vqdetail.getVendorquotation().getCurrency().getSymbol());
                jSONObject.put("rate", vqdetail.getRate());
                jSONObject.put("amount", authHandler.round(vqdetail.getQuantity() * vqdetail.getRate(), companyid));
                jSONObject.put("isLeaseFixedAsset", vqdetail.getVendorquotation().isFixedAssetVQ()==true?true:false);
                jSONArray.put(jSONObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void putPurchaseRequisitionDetails(List<PurchaseRequisitionDetail> list, JSONArray jSONArray, Map<String, Object> requestMap) {
        try {
            DateFormat df = null;
            String companyid = (String) requestMap.get("companyid");
            if (requestMap.containsKey("df")) {
                df = (DateFormat) requestMap.get("df");
            }
            for (PurchaseRequisitionDetail preqdetail : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "Purchase Requisition");
                jSONObject.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                jSONObject.put("billid", preqdetail.getPurchaserequisition().getID());
                jSONObject.put("transactionno", preqdetail.getPurchaserequisition().getPrNumber());
                jSONObject.put("name", preqdetail.getPurchaserequisition().getVendor() != null ? preqdetail.getPurchaserequisition().getVendor().getName() : "");
                jSONObject.put("date", df != null ? df.format(preqdetail.getPurchaserequisition().getRequisitionDate()) : preqdetail.getPurchaserequisition().getRequisitionDate());
                jSONObject.put("quantity", preqdetail.getQuantity());
                jSONObject.put("currencysymbol", preqdetail.getPurchaserequisition().getCurrency().getSymbol());
                jSONObject.put("rate", preqdetail.getRate());
                jSONObject.put("amount", authHandler.round(preqdetail.getQuantity() * preqdetail.getRate(), companyid));
                jSONObject.put("isLeaseFixedAsset", preqdetail.getPurchaserequisition().isFixedAssetPurchaseRequisition() == true ? true : false);
                jSONArray.put(jSONObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void putRFQDetails(List<RequestForQuotationDetail> list, JSONArray jSONArray, Map<String, Object> requestMap) throws ServiceException{
        try {
            DateFormat df = null;
            String companyid = (String) requestMap.get("companyid");
            if (requestMap.containsKey("df")) {
                df = (DateFormat) requestMap.get("df");
            }
            for (RequestForQuotationDetail rfqdetail : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "RFQ");
                jSONObject.put("moduleid", Constants.Acc_RFQ_ModuleId);
                jSONObject.put("billid", rfqdetail.getRequestforquotation().getID());
                jSONObject.put("transactionno", rfqdetail.getRequestforquotation().getRfqNumber());
                if(!StringUtil.isNullOrEmpty(rfqdetail.getRequestforquotation().getVendors())) {
                    HashMap<String, Object> vReqParams = new HashMap();
                   String vendors = "";
                    ArrayList filter_names = new ArrayList();
                    ArrayList filter_params = new ArrayList();
                    String[] vendorIds = rfqdetail.getRequestforquotation().getVendors().split(",");
                    for (int i = 0; i < vendorIds.length; i++) {
                            vendors += "'"+vendorIds[i]+"',";
                    }
                    vendors = vendors.substring(0, Math.max(0, vendors.length() - 1));
                    filter_names.add("INID");
                    filter_params.add(vendors);
                    vReqParams.put("filter_names", filter_names);
                    vReqParams.put("filter_params", filter_params);
                    KwlReturnObject result = accProductObj.getVendorList(vReqParams);
                    List<Vendor> vendorObjs = result.getEntityList();
                    String vendorName = "";
                     if(vendorObjs.size()>0) {
                        for(Vendor vobj : vendorObjs) {
                            vendorName += StringUtil.isNullOrEmpty(vobj.getName()) ? "" : " "+vobj.getName()+",";
                          }
                         
                        
                        
                        vendorName = vendorName.substring(0, Math.max(0, vendorName.length() - 1));
                        jSONObject.put("name",vendorName);
                    }
                }
               jSONObject.put("date", df != null ? df.format(rfqdetail.getRequestforquotation().getRfqDate()) : rfqdetail.getRequestforquotation().getRfqDate());
                jSONObject.put("quantity", rfqdetail.getQuantity());
                jSONObject.put("rate", rfqdetail.getRate());
                jSONObject.put("amount", authHandler.round(rfqdetail.getQuantity() * rfqdetail.getRate(), companyid));
                jSONObject.put("isLeaseFixedAsset", rfqdetail.getRequestforquotation().isFixedAssetRFQ() == true ? true : false);
                jSONArray.put(jSONObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   public void putSalesReturnDetails(List<SalesReturnDetail> list, JSONArray jSONArray, Map<String, Object> requestMap) {
        try {
            DateFormat df = null;
            String companyid = (String) requestMap.get("companyid");
            if (requestMap.containsKey("df")) {
                df = (DateFormat) requestMap.get("df");
            }
            for (SalesReturnDetail srdetail : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "Sales Return");
                jSONObject.put("moduleid", Constants.Acc_Sales_Return_ModuleId);
                jSONObject.put("billid", srdetail.getSalesReturn().getID());
                jSONObject.put("transactionno", srdetail.getSalesReturn().getSalesReturnNumber());
                jSONObject.put("name", srdetail.getSalesReturn().getCustomer()!= null ? srdetail.getSalesReturn().getCustomer().getName() : "");
                jSONObject.put("date", df != null ? df.format(srdetail.getSalesReturn().getOrderDate()) : srdetail.getSalesReturn().getOrderDate());
                jSONObject.put("quantity", srdetail.getReturnQuantity());
                jSONObject.put("currencysymbol", srdetail.getSalesReturn().getCurrency().getSymbol());
                jSONObject.put("rate", srdetail.getRate());
                jSONObject.put("amount", authHandler.round(srdetail.getReturnQuantity() * srdetail.getRate(), companyid));
                jSONObject.put("isLeaseFixedAsset", srdetail.getSalesReturn().isFixedAsset() == true ? true : false);
                jSONArray.put(jSONObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void putPurchaseReturnDetails(List<PurchaseReturnDetail> list, JSONArray jSONArray, Map<String, Object> requestMap) {
        try {
            DateFormat df = null;
            String companyid = (String) requestMap.get("companyid");
            if (requestMap.containsKey("df")) {
                df = (DateFormat) requestMap.get("df");
            }
            for (PurchaseReturnDetail prdetail : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "Purchase Return");
                jSONObject.put("moduleid", Constants.Acc_Purchase_Return_ModuleId);
                jSONObject.put("billid", prdetail.getPurchaseReturn().getID());
                jSONObject.put("transactionno", prdetail.getPurchaseReturn().getPurchaseReturnNumber());
                jSONObject.put("name", prdetail.getPurchaseReturn().getVendor()!= null ? prdetail.getPurchaseReturn().getVendor().getName() : "");
                jSONObject.put("date", df != null ? df.format(prdetail.getPurchaseReturn().getOrderDate()) : prdetail.getPurchaseReturn().getOrderDate());
                jSONObject.put("quantity", prdetail.getReturnQuantity());
                jSONObject.put("currencysymbol", prdetail.getPurchaseReturn().getCurrency().getSymbol());
                jSONObject.put("rate", prdetail.getRate());
                jSONObject.put("amount", authHandler.round(prdetail.getReturnQuantity() * prdetail.getRate(), companyid));
                jSONObject.put("isLeaseFixedAsset", prdetail.getPurchaseReturn().isFixedAsset() == true ? true : false);
                jSONArray.put(jSONObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  /**
     *
     * @param list = List of Purchase Order Details
     * @param jSONArray = Put data in Array
     * @param requestMap
     */
    public void putPurchaseOrderDetails(List<PurchaseOrderDetail> list, JSONArray jSONArray, Map<String, Object> requestMap) {
        try {
            DateFormat df = null;
            String companyid = (String) requestMap.get("companyid");
            if (requestMap.containsKey("df")) {
                df = (DateFormat) requestMap.get("df");
            }
            for (PurchaseOrderDetail purchaseOrderDetail : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "Purchase Order");
                jSONObject.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
                jSONObject.put("billid", purchaseOrderDetail.getPurchaseOrder().getID());
                jSONObject.put("transactionno", purchaseOrderDetail.getPurchaseOrder().getPurchaseOrderNumber());
                jSONObject.put("name", purchaseOrderDetail.getPurchaseOrder().getVendor().getName());
                jSONObject.put("date", df != null ? df.format(purchaseOrderDetail.getPurchaseOrder().getOrderDate()) : purchaseOrderDetail.getPurchaseOrder().getOrderDate());
                jSONObject.put("quantity", purchaseOrderDetail.getQuantity());
                jSONObject.put("currencysymbol", purchaseOrderDetail.getPurchaseOrder().getCurrency().getSymbol());
                jSONObject.put("rate", purchaseOrderDetail.getRate());
                jSONObject.put("amount", authHandler.round(purchaseOrderDetail.getQuantity() * purchaseOrderDetail.getRate(), companyid));
                jSONObject.put("isConsignment", purchaseOrderDetail.getPurchaseOrder().isIsconsignment());
                jSONArray.put(jSONObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param list = List of Customer Invoice Details
     * @param jSONArray = Put data in Array
     * @param requestMap
     */
    public void putCustomerInvoiceDetails(List<InvoiceDetail> list, JSONArray jSONArray, Map<String, Object> requestMap) {
        try {
            DateFormat df = null;
            String companyid = (String) requestMap.get("companyid");
            if (requestMap.containsKey("df")) {
                df = (DateFormat) requestMap.get("df");
            }
            for (InvoiceDetail invoiceDetail : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "Customer Invoice");
                jSONObject.put("moduleid", Constants.Acc_Invoice_ModuleId);
                jSONObject.put("billid", invoiceDetail.getInvoice().getID());
                jSONObject.put("transactionno", invoiceDetail.getInvoice().getInvoiceNumber());
                jSONObject.put("name", invoiceDetail.getInvoice().getCustomer().getName());
//                jSONObject.put("date", df != null ? df.format(invoiceDetail.getInvoice().getJournalEntry().getEntryDate()) : invoiceDetail.getInvoice().getJournalEntry().getEntryDate());
                jSONObject.put("date", df != null ? df.format(invoiceDetail.getInvoice().getCreationDate()) : invoiceDetail.getInvoice().getCreationDate());
                jSONObject.put("quantity", invoiceDetail.getInventory().getQuantity());
                jSONObject.put("currencysymbol", invoiceDetail.getInvoice().getCurrency().getSymbol());
                jSONObject.put("rate", invoiceDetail.getRate());
                jSONObject.put("amount", authHandler.round(invoiceDetail.getInventory().getQuantity() * invoiceDetail.getRate(), companyid));
                jSONObject.put("isConsignment", invoiceDetail.getInvoice().isIsconsignment());
                jSONObject.put("isLeaseFixedAsset", invoiceDetail.getInvoice().isFixedAssetLeaseInvoice());
                jSONArray.put(jSONObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param list = List of Vendor Invoice Details
     * @param jSONArray = Put data in Array
     * @param requestMap
     */
    public void putVendorInvoiceDetails(List<GoodsReceiptDetail> list, JSONArray jSONArray, Map<String, Object> requestMap) {
        try {
            DateFormat df = null;
            String companyid = (String) requestMap.get("companyid");
            if (requestMap.containsKey("df")) {
                df = (DateFormat) requestMap.get("df");
            }
            for (GoodsReceiptDetail goodsReceiptDetail : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "Vendor Invoice");
                jSONObject.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                jSONObject.put("billid", goodsReceiptDetail.getGoodsReceipt().getID());
                jSONObject.put("transactionno", goodsReceiptDetail.getGoodsReceipt().getGoodsReceiptNumber());
                jSONObject.put("name", goodsReceiptDetail.getGoodsReceipt().getVendor().getName());
//                jSONObject.put("date", df != null ? df.format(goodsReceiptDetail.getGoodsReceipt().getJournalEntry().getEntryDate()) : goodsReceiptDetail.getGoodsReceipt().getJournalEntry().getEntryDate());
                jSONObject.put("date", df != null ? df.format(goodsReceiptDetail.getGoodsReceipt().getCreationDate()) : goodsReceiptDetail.getGoodsReceipt().getCreationDate());
                jSONObject.put("quantity", goodsReceiptDetail.getInventory().getQuantity());
                jSONObject.put("currencysymbol", goodsReceiptDetail.getGoodsReceipt().getCurrency().getSymbol());
                jSONObject.put("rate", goodsReceiptDetail.getRate());
                jSONObject.put("amount", authHandler.round(goodsReceiptDetail.getInventory().getQuantity() * goodsReceiptDetail.getRate(), companyid));
                jSONObject.put("isConsignment", goodsReceiptDetail.getGoodsReceipt().isIsconsignment());
                jSONArray.put(jSONObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param list = List of Delivery Order Details
     * @param jSONArray = Put data in Array
     * @param requestMap
     */
    public void putDeliveryOrderDetails(List<DeliveryOrderDetail> list, JSONArray jSONArray, Map<String, Object> requestMap) {
        try {
            DateFormat df = null;
            String companyid = (String) requestMap.get("companyid");
            if (requestMap.containsKey("df")) {
                df = (DateFormat) requestMap.get("df");
            }
            for (DeliveryOrderDetail deliveryOrderDetail : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "Delivery Order");
                jSONObject.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
                jSONObject.put("billid", deliveryOrderDetail.getDeliveryOrder().getID());
                jSONObject.put("transactionno", deliveryOrderDetail.getDeliveryOrder().getDeliveryOrderNumber());
                jSONObject.put("name", deliveryOrderDetail.getDeliveryOrder().getCustomer().getName());
                jSONObject.put("date", df != null ? df.format(deliveryOrderDetail.getDeliveryOrder().getOrderDate()) : deliveryOrderDetail.getDeliveryOrder().getOrderDate());
                jSONObject.put("quantity", deliveryOrderDetail.getDeliveredQuantity());
                jSONObject.put("currencysymbol", deliveryOrderDetail.getDeliveryOrder().getCurrency().getSymbol());
                jSONObject.put("rate", deliveryOrderDetail.getRate());
                jSONObject.put("amount", authHandler.round(deliveryOrderDetail.getDeliveredQuantity() * deliveryOrderDetail.getRate(), companyid));
                jSONObject.put("isConsignment", deliveryOrderDetail.getDeliveryOrder().isIsconsignment());
                jSONObject.put("isLeaseFixedAsset", deliveryOrderDetail.getDeliveryOrder().isLeaseDO());
                jSONArray.put(jSONObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param list = List of GR Details
     * @param jSONArray = Put data in Array
     * @param requestMap
     */
    public void putGoodsReceiptOrderDetails(List<GoodsReceiptOrderDetails> list, JSONArray jSONArray, Map<String, Object> requestMap) {
        try {
            DateFormat df = null;
            String companyid = (String) requestMap.get("companyid");
            if (requestMap.containsKey("df")) {
                df = (DateFormat) requestMap.get("df");
            }
            for (GoodsReceiptOrderDetails goodsReceiptOrderDetails : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "Goods Receipt Order");
                jSONObject.put("moduleid", Constants.Acc_Goods_Receipt_ModuleId);
                jSONObject.put("billid", goodsReceiptOrderDetails.getGrOrder().getID());
                jSONObject.put("transactionno", goodsReceiptOrderDetails.getGrOrder().getGoodsReceiptOrderNumber());
                jSONObject.put("name", goodsReceiptOrderDetails.getGrOrder().getVendor().getName());
                jSONObject.put("date", df != null ? df.format(goodsReceiptOrderDetails.getGrOrder().getOrderDate()) : goodsReceiptOrderDetails.getGrOrder().getOrderDate());
                jSONObject.put("quantity", goodsReceiptOrderDetails.getDeliveredQuantity());
                jSONObject.put("currencysymbol", goodsReceiptOrderDetails.getGrOrder().getCurrency().getSymbol());
                jSONObject.put("rate", goodsReceiptOrderDetails.getRate());
                jSONObject.put("amount", authHandler.round(goodsReceiptOrderDetails.getDeliveredQuantity() * goodsReceiptOrderDetails.getRate(), companyid));
                jSONObject.put("isConsignment", goodsReceiptOrderDetails.getGrOrder().isIsconsignment());
                jSONArray.put(jSONObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        /**
     *
     * @param list = List of Stock Adjustment Details
     * @param jSONArray = Put data in Array
     * @param requestMap
     */
    public void putStockAdjustmentDetails(List<StockAdjustment> list, JSONArray jSONArray, Map<String, Object> requestMap) {
        try {
            DateFormat df = null;
            String companyid = (String) requestMap.get("companyid");
            if (requestMap.containsKey("df")) {
                df = (DateFormat) requestMap.get("df");
            }
            for (StockAdjustment stockAdjustment : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "Stock Adjustment ("+stockAdjustment.getAdjustmentType()+")");
                jSONObject.put("moduleid", Constants.Acc_Stock_Adjustment_ModuleId);
                jSONObject.put("billid",stockAdjustment.getId());
                jSONObject.put("isInventory",true);
                jSONObject.put("transactionno", stockAdjustment.getTransactionNo());
                jSONObject.put("date", df != null ? df.format(stockAdjustment.getBusinessDate()) : stockAdjustment.getBusinessDate());
                jSONObject.put("quantity", stockAdjustment.getFinalQuantity());
                jSONObject.put("currencysymbol", stockAdjustment.getCompany().getCurrency().getSymbol());
                jSONObject.put("rate", stockAdjustment.getPricePerUnit());
                jSONObject.put("amount", authHandler.round(stockAdjustment.getFinalQuantity() * stockAdjustment.getPricePerUnit(), companyid));
                jSONArray.put(jSONObject);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * 
     * @param array = Array need to be sort
     * @param requestMap = pass date format
     * @return =  return sorted array by date (Ascending).
     * @throws JSONException 
     */
    public static JSONArray sortJsonByDate(JSONArray array, final Map<String, Object> requestMap) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject ja, JSONObject jb) {
                    try {
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        if (requestMap.containsKey("df")) {
                            df = (DateFormat) requestMap.get("df");
                        }
                        String s1 = ja.optString("date");
                        String s2 = jb.optString("date");
                        Date date1 = df.parse(s1);
                        Date date2 = df.parse(s2);

                        if (date1.getTime() < date2.getTime()) {
                            return 1;
                        } else if (date1.getTime() > date2.getTime()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    } catch (ParseException ex) {
                        Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return 0;
                }
            });

        } catch (JSONException ex) {
            Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new JSONArray(jsons);
    }
    public JSONObject getProductRecipes(HashMap<String, Object> requestMap) throws ServiceException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            String productID = (String) requestMap.get(Constants.productid);
            String currencyid = (String) requestMap.get("currencyid");
            String bomid = (String) requestMap.get("bomid");
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.productid, productID);
            requestParams.put("bomid", bomid);
            jsonArray = getBOMDetail(requestParams, jsonArray, currencyid);
            jsonObject.put("valid", true);
            jsonObject.put("data", jsonArray);
        } catch (Exception ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jsonObject;
    }

    private JSONArray getBOMDetail(Map<String, Object> requestMap, JSONArray jsonArray, String currencyid) throws ServiceException {
        try {
            String productID = (String) requestMap.get(Constants.productid);
            String bomid = (String) requestMap.get("bomid");
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.productid, productID);
            if (requestMap.containsKey("isdefaultbom") && requestMap.get("isdefaultbom") != null) {
                requestParams.put("isdefaultbom", Boolean.parseBoolean(requestMap.get("isdefaultbom").toString()));
            }
            if (requestMap.containsKey("bomid") && requestMap.get("bomid") != null) {
                requestParams.put("bomid", bomid);
            }
            KwlReturnObject result = accProductObj.getBOMDetail(requestParams);
            List<BOMDetail> bomdetails = result.getEntityList();
            if (bomdetails != null && !bomdetails.isEmpty()) {
                for (BOMDetail bomdetail : bomdetails) {
                    JSONObject temp = new JSONObject();
                    temp.put("id",bomdetail.getID()+"_"+bomdetail.getBomCode().replace(" ", ""));
                    temp.put("text", bomdetail.getBomName());
                    temp.put("name", bomdetail.getBomName());
                    temp.put(Constants.productid, bomdetail.getProduct().getID());
                    temp.put("parentid", productID);
                    jsonArray.put(temp);
                    getBOMRecipes(jsonArray, bomdetail.getProduct(), bomdetail, currencyid);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jsonArray;
    }

    private JSONArray getBOMRecipes(JSONArray jsonArray, Product parent, BOMDetail bomdetail, String currencyid) {
        try {
            HashMap<String, Object> requestMap = new HashMap<>();
            requestMap.put(Constants.productid, parent.getID());
            requestMap.put("currencyid", currencyid);
            requestMap.put("bomdetailid", bomdetail.getID());
            KwlReturnObject result = accProductObj.getAssemblyItems(requestMap);
            List list = result.getEntityList();
            if (list != null && !list.isEmpty()) {
                for (Object object : list) {
                    Object[] row = (Object[]) object;
                    ProductAssembly passembly = (ProductAssembly) row[0];
                    if (passembly != null && passembly.getSubproducts() != null) {
                        JSONObject temp = new JSONObject();
                        temp.put("text", passembly.getSubproducts().getProductid() + " (" + passembly.getSubproducts().getName() + ")");
                        temp.put("name", passembly.getSubproducts().getName());
                        temp.put(Constants.productid, passembly.getSubproducts().getID());
                        temp.put("parentid", bomdetail.getID()+"_"+bomdetail.getBomCode().replace(" ", ""));
                        temp.put("producttype", passembly.getSubproducts().getProducttype() != null ? passembly.getSubproducts().getProducttype().getName() : "");
                        jsonArray.put(temp);
                        Map<String, Object> requestParams = new HashMap<>();
                        requestParams.put(Constants.productid, passembly.getSubproducts().getID());
                        if (passembly.getSubbom() != null) {
                            requestParams.put("bomid", passembly.getSubbom().getID());
                        } else {
                            requestParams.put("isdefaultbom", true);
                        }
                        jsonArray = getBOMDetail(requestParams, jsonArray, currencyid);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jsonArray;
    }
    @Override
    public JSONArray getBOMCombo(Map<String, Object> map) throws ServiceException {
        JSONArray jArr = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        String productid = "";
        if (map.get(Constants.productid) != null) {
            productid = map.get(Constants.productid).toString();

            String[] productidArr = productid.split(",");
            productid = "";
            for (int count = 0; count < productidArr.length; count++) {
                if (count == 0) {
                    productid += "'" + productidArr[count] + "'";
                } else {
                    productid += ",'" + productidArr[count] + "'";
                }
            }
            map.put(Constants.productid, productid);
        }
        KwlReturnObject result = accProductObj.getBOMCombo(map);
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            try {
                jSONObject = new JSONObject();
                Object obj[] = (Object[]) itr.next();
                jSONObject.put("id", (String) obj[0]);
                jSONObject.put("bomid", (String) obj[0]); // Added new Keys bomid and bomcode which is as same as id and name
                jSONObject.put("name", "["+(String) obj[1]+"]");
                jSONObject.put("bomcode", (String) obj[2]);
                jSONObject.put("pid", (String) obj[3]);
                jSONObject.put("productname", (String) obj[4]);
                jSONObject.put("isdefaultbom", (Boolean) obj[5]);   //ERP-31663
                jArr.put(jSONObject);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                throw ServiceException.FAILURE(ex.getMessage(), ex);
            }
        }
        return jArr;
    }
    
    /**
     * Description: Method used to get Product Brand Discount details
     * @param requestMap
     * @param list
     * @return JSONArray
     * @throws JSONException
     * @throws ServiceException 
     */
    @Override
    public JSONArray getProductBrandDiscountDetailsJson(Map<String, Object> requestMap, List<Object> list) throws JSONException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            boolean isCustomerCategory = false;
            if (requestMap.containsKey("isCustomerCategory") && requestMap.get("isCustomerCategory") != null) {
                isCustomerCategory = (Boolean) requestMap.get("isCustomerCategory");
            }
            boolean isCustomerCategoryRecordExist = false;
            if (requestMap.containsKey("isCustomerCategoryRecordExist") && requestMap.get("isCustomerCategoryRecordExist") != null) {
                isCustomerCategoryRecordExist = (Boolean) requestMap.get("isCustomerCategoryRecordExist");
            }
            boolean isAnyRuleRecordExist = false;
            if (requestMap.containsKey("isAnyRuleRecordExist") && requestMap.get("isAnyRuleRecordExist") != null) {
                isAnyRuleRecordExist = (Boolean) requestMap.get("isAnyRuleRecordExist");
            }
            // if already haing records with customer category rule then fetch customer category records
            if (isCustomerCategoryRecordExist) {
                isCustomerCategory = isCustomerCategoryRecordExist;
            }
            
            for (Object rowObj : list) {
                JSONObject obj = new JSONObject();
                obj.put("isCustomerCategoryRecordExist", isCustomerCategoryRecordExist);
                obj.put("isAnyRuleRecordExist", isAnyRuleRecordExist);
                if (isCustomerCategory) {
                    MasterItem customerCategory = (MasterItem) rowObj;
                    obj.put("customerUUID", customerCategory.getID());
                    obj.put("customerName", customerCategory.getValue());
                    
                    requestMap.put("customerCategoryID", customerCategory.getID());
                } else {
                    Customer customer = (Customer) rowObj;
                    obj.put("customerUUID", customer.getID());
                    obj.put("customerName", customer.getName());
                    
                    requestMap.put("customerID", customer.getID());
                }
                
                /**
                 * Get dimension values 
                 */
                List<FieldComboData> fieldComboDatas = Collections.emptyList();
                if (requestMap.containsKey("dimension")) {
                    HashMap<String, Object> filterRequestParams = new HashMap<>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                    filter_names.add("field.id");
                    filter_params.add(requestMap.get("dimension").toString());
                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_values", filter_params);
                    /**
                     * Get Id from field combo data using column value 
                     */
                    KwlReturnObject result = accAccountDAOobj.getFieldCombo(filterRequestParams);
                    fieldComboDatas = result.getEntityList();
                }
                
                for (FieldComboData FieldComboData : fieldComboDatas) {
                    requestMap.put("productBrandID", FieldComboData.getId());
                    
                    KwlReturnObject detailResult = accProductObj.getProductBrandDiscountDetails(requestMap);
                    if (detailResult.getEntityList() != null && !detailResult.getEntityList().isEmpty()) {
                        String detailID = (String) detailResult.getEntityList().get(0);
                        
                        KwlReturnObject detailObject = accountingHandlerDAOobj.getObject(ProductBrandDiscountDetails.class.getName(), detailID);
                        ProductBrandDiscountDetails detailRow = (ProductBrandDiscountDetails) detailObject.getEntityList().get(0);
                        
                        obj.put(FieldComboData.getValue(), detailRow.getDiscountValue());
                    } else {
                        obj.put(FieldComboData.getValue(), 0);
                    }
                }

                jArr.put(obj);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    /**
     * Description: Method is used to create column model for POroduct Brand Discount details
     * @param jarrColumns
     * @param jarrRecords
     * @param companyID
     * @throws JSONException
     * @throws ServiceException 
     */
    @Override
    public void createColumnModelForProcuctBrandDisocuntDetails(JSONArray jarrColumns, JSONArray jarrRecords, JSONObject paramJobj) throws JSONException, ServiceException {
        JSONObject jobjTemp = new JSONObject();
        jobjTemp.put("name", "isAnyRuleRecordExist");
        jarrRecords.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "isCustomerCategoryRecordExist");
        jarrRecords.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "customerUUID");
        jarrRecords.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "customerName");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", "Customer Name / Customer Category");
        jobjTemp.put("dataIndex", "customerName");
        jobjTemp.put("width", 200);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "currencycode");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "currencysymbol");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "currencyid");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", "Currency");
        jobjTemp.put("dataIndex", "currencycode");
        jobjTemp.put("hidden", "true");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
        
        /**
         * Create column for dimension values
         */
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("dimension"))) {
            HashMap<String, Object> filterRequestParams = new HashMap<>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            filter_names.add("field.id");
            filter_params.add(paramJobj.optString("dimension"));
//            filter_names.add("company.companyID");
//            filter_params.add(paramJobj.optString("companyid"));
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_values", filter_params);
            KwlReturnObject result = accAccountDAOobj.getFieldCombo(filterRequestParams);
            List<FieldComboData> fieldComboDatas = result.getEntityList();

            for (FieldComboData comboData : fieldComboDatas) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", comboData.getValue());
                jarrRecords.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("header", comboData.getValue());
                jobjTemp.put("dataIndex", comboData.getValue());
                jobjTemp.put("isBrand", true);
                jobjTemp.put("align", "right");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
            }
        }
    }
        
    
    
    @Override
    public JSONObject SyncProductsIntoPM(JSONArray DataJArr,String companyid) throws ServiceException{
         JSONObject jobj=new JSONObject();
        try {

            JSONObject userData = new JSONObject();
            userData.put("companyid", companyid);
            userData.put("data", DataJArr);
            String accRestURL = URLUtil.buildRestURL("pmURL");
            String endpoint = accRestURL + "company/products";
             jobj = apiCallHandlerService.restPostMethod(endpoint, userData.toString());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccProductServiceImpl.SyncProductsIntoPM", ex);
        }
        return jobj;
    }
    
    @Override
   public JSONObject syncDataIntoPM(HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        try {

            String companyid = requestParams.containsKey("companyid") ? (String) requestParams.get("companyid") : "";
             JSONArray DataJArr=null;
            if (requestParams.containsKey("jArr") && requestParams.get("jArr")!=null) {
                 DataJArr = (JSONArray) requestParams.get("jArr");
            }else{
                KwlReturnObject result = accProductObj.getProducts(requestParams);
                List list = result.getEntityList();
                 DataJArr = getProductsJsonToSyncTOPM(list);
            }

            jobj= SyncProductsIntoPM(DataJArr, companyid);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccProductServiceImpl.syncDataIntoPM", ex);
        }
        return jobj;
    }

    
    @Override
    public JSONArray getProductsJsonToSyncTOPM(List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        JSONObject obj = null;
        try {
            
            for (Object Obj : list) {
                Object[] row = (Object[]) Obj;
                Product product = (Product) row[0];

                obj = new JSONObject();
                obj.put("id", product.getID());
                obj.put("name", product.getName());
                obj.put("producttype", product.getProducttype().getID());
                obj.put("inspectiontemplateid",product.getInspectionTemplate()!=null?product.getInspectionTemplate().getId():"NULL");

                jArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccProductServiceImpl.getProductsJsonToSyncTOPM", ex);
        }
        return jArr;
    }
    
    @Override
    public JSONArray getAssemblyProducts(Product product,Map<String,Object> requestParams , JSONArray JArr) throws ServiceException,JSONException,SessionExpiredException {
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put(Constants.productid, product.getID());
        if (!requestParams.containsKey("bomdetailid") || requestParams.get("bomdetailid") == null) {
            dataMap.put("isdefaultbom", true);
        } else {
            dataMap.put("bomdetailid", requestParams.get("bomdetailid"));
        }
        dataMap.put("currencyid", requestParams.get("currencyid"));
        if (requestParams.containsKey("isManageQuantity") && requestParams.get("isManageQuantity")!=null && requestParams.get("isManageQuantity").toString()!="") {
            dataMap.put("isManageQuantity", requestParams.get("isManageQuantity"));
        }
        KwlReturnObject retObj = accProductObj.getAssemblyItems(dataMap);
        requestParams.put("levelCount", 1);
        requestParams.put(Constants.parentProduct, product);
        if (requestParams.containsKey("isForCompAvailablity")) {
            requestParams.put("isForCompAvailablity", requestParams.get("isForCompAvailablity").toString());
        } else {
            requestParams.put("isForCompAvailablity", "true");
        }
        List<ProductAssembly> list = retObj.getEntityList();
        JArr=getAssemblyItemsJson(list, requestParams, JArr);
        return JArr;
    }
    
    @Override
    public JSONArray getProductsForProject(Map<String, Object> requestParams) throws ServiceException ,JSONException,SessionExpiredException{
        JSONArray JArr = new JSONArray();
        Locale locale = null;
        if (requestParams.containsKey(Constants.locale)){
            locale = (Locale)requestParams.get(Constants.locale);
        }
         KwlReturnObject objresult = workOrderDAOObj.getWorkOrderComponentDetails(requestParams);
         /*If requestParams contains product id then set productidtemp value as productId*/
         String productidtemp="",companyid="";
         boolean isQaApprovalFlowInMRP=false, isManageQuantity=false;
         if(requestParams.containsKey("productId") && requestParams.get("productId") != null && !StringUtil.isNullOrEmpty(requestParams.get("productId").toString())){
            productidtemp=requestParams.get("productId").toString();
         }
         if (requestParams.containsKey("isManageQuantity") && requestParams.get("isManageQuantity") != null) {
            isManageQuantity = Boolean.parseBoolean(requestParams.get("isManageQuantity").toString());
        }
         if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullObject(requestParams.get("companyid"))) {
                companyid = (String) requestParams.get("companyid");
                Map<String, Object> filterMapForECP = new HashMap();
                filterMapForECP.put(Constants.Acc_id, companyid);
                String columnprefStr = (String) kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"columnPref"}, filterMapForECP);
                JSONObject columnpref = new JSONObject(columnprefStr);
                if (columnpref.has("isQaApprovalFlowInMRP")) {
                    isQaApprovalFlowInMRP = columnpref.optBoolean("isQaApprovalFlowInMRP", false);
                }
         }
        List<WorkOrderComponentDetails> detailsesList = objresult.getEntityList();
        boolean isTransactionSendForQA;
        for (WorkOrderComponentDetails workOrderComponentDetails : detailsesList) {
            if(workOrderComponentDetails.getProduct()!= null){
                isTransactionSendForQA=false;
                JSONObject obj=getProductDetailJSON(workOrderComponentDetails.getProduct(), requestParams);
                /*
                 * Block quantity has been fetched from SO, Assembly and WO block quanities for that product.
                 */
                String productid = workOrderComponentDetails.getProduct().getID();
                KwlReturnObject result2 = accProductObj.getAssemblyLockQuantity(productid);
                Double assmblyLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));
                KwlReturnObject result1 = accProductObj.getLockQuantity(productid);
                Double SoLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));
                KwlReturnObject woresult = accProductObj.getWOLockQuantity(productid, isManageQuantity);    //ERP-40045 : Total Block Quantity Calculation
                Double WOLockQuantity = (Double) (woresult.getEntityList().get(0) == null ? 0.0 : woresult.getEntityList().get(0));
//                Double OtherWOLockQuantity = WOLockQuantity - (workOrderComponentDetails.getBlockQuantity() - workOrderComponentDetails.getBlockQuantityUsed() - workOrderComponentDetails.getRejectedQuantity() - workOrderComponentDetails.getReturnQuantity());
                double totalLockQuantity = assmblyLockQuantity + SoLockQuantity + WOLockQuantity;
                double availableQuantity = workOrderComponentDetails.getProduct().getAvailableQuantity() ;
                double availableQuantityForUse = availableQuantity - totalLockQuantity;  
                availableQuantityForUse = authHandler.roundQuantity(availableQuantityForUse, workOrderComponentDetails.getProduct().getCompany().getCompanyID());
                if (availableQuantityForUse < 0) {
                    availableQuantityForUse = 0.0;
                }
                
                String bomdetailid = workOrderComponentDetails.getWorkOrder().getBomid().getID();
                KwlReturnObject kwl = accProductObj.selectSubProductFromAssemblyMRP(productid, bomdetailid);
                /**
                 * For producing product if any quantity is blocked then not
                 * need to do manage quantity if blocked more than minimum percent Quantity from Component Availability only.
                 */
                if (isManageQuantity && workOrderComponentDetails.getParentProduct() != null && workOrderComponentDetails.getProduct().getProducttype().getID().equals(Producttype.ASSEMBLY) && workOrderComponentDetails.getBlockQuantity() > 0 && workOrderComponentDetails.isBlockedFromCA()) {
                    double percent = workOrderComponentDetails.getMinpercent();
                    double blockQty = authHandler.roundQuantity((workOrderComponentDetails.getBlockQuantity()), companyid);
                    double requiredQty = authHandler.roundQuantity((workOrderComponentDetails.getRequiredQuantity()), companyid);
                    double actualreqqty = authHandler.roundQuantity(((percent / 100) * requiredQty), companyid);
                    if (blockQty >= actualreqqty && blockQty <= requiredQty) {
                        JSONObject jObj = new JSONObject();
                        String msg = messageSource.getMessage("acc.product.managequantity.quantityAlreadyBlocked", new Object[]{workOrderComponentDetails.getProduct().getProductid()}, locale);
                        jObj.put(Constants.RES_msg, msg);
                        jObj.put(Constants.isAlreadyBlocked, true);
                        JArr.put(jObj);
                        return JArr;
                    }
                }
            List klist=kwl.getEntityList();
            Iterator itr = klist.iterator();
            double ratioquantity=0;
            while(itr.hasNext())
            {
                Object ko=itr.next();
                ProductAssembly pa=(ProductAssembly) ko;
                
                int ComponentType=pa.getComponentType();
                 obj.put("ComponentType", ComponentType);
                 ratioquantity=pa.getQuantity();
                 obj.put("ratioquantity", ratioquantity);

                 if(ComponentType==2 || ComponentType==3)
                 {
             
                 obj.put("finalquantityproduce",pa.getQuantity()*(workOrderComponentDetails.getWorkOrder().getQuantity()));
                 
                 }
            }
                JSONObject consumptionDetails = null;
                if (!StringUtil.isNullOrEmpty(workOrderComponentDetails.getConsumptionDetails())) {
                    consumptionDetails = new JSONObject(workOrderComponentDetails.getConsumptionDetails());
                    consumptionDetails.put(WorkOrderComponentDetails.PARAM_AVAILABLE_QUANTITY, availableQuantityForUse);
                    /**
                     * Check if Product in manage quantity for producing, is
                     * already consumed.
                     */
                    String batchDetailsString = consumptionDetails.optString(WorkOrderComponentDetails.BATCH_DETAILS);
                    if (!StringUtil.isNullOrEmpty(batchDetailsString)) {
                        try {
                            JSONArray batchDetails = new JSONArray(batchDetailsString);
                            if (batchDetails != null && batchDetails.length() > 0 && !batchDetails.isNull(0)) {
                                obj.put(Constants.readOnly, true);
                            } else {
                                obj.put(Constants.readOnly, false);
                            }
                        } catch (JSONException ex) {
                            obj.put(Constants.readOnly, false);
                        } 
                    }
                    /**
                     * Check if Product is already produced then actual quantity
                     * to be 0.
                     */
                    String produceQtyDetailsString = consumptionDetails.optString(WorkOrderComponentDetails.PRODUCE_DETAILS);
                    if (!StringUtil.isNullOrEmpty(produceQtyDetailsString)) {
                        try {
                            JSONArray produceQtyDetails = new JSONArray(produceQtyDetailsString);
                            if (produceQtyDetails != null && produceQtyDetails.length() > 0 && !produceQtyDetails.isNull(0)) {
                                consumptionDetails.put("actualquantity", 0.0);
                                /**
                                 * Reduce the produced quantity for 2nd time
                                 * manage quantity.
                                 */
                                Double produceQty = 0.0;
                                for (int i = 0; i < produceQtyDetails.length(); i++) {
                                    produceQty += produceQtyDetails.getJSONObject(i).optDouble(Constants.quantity);
                                }
                                /**
                                 * Initial available quantity describes the
                                 * quantity before doing manage quantity.
                                 */
                                obj.put(Constants.initialAvailableQuantity, availableQuantityForUse - produceQty);
                                consumptionDetails.put(Constants.initialAvailableQuantity, availableQuantityForUse - produceQty);
                                /**
                                 * If Product to be produced is already is
                                 * produced then MQ is done multiple times,
                                 * hence MQ in Edit case.
                                 */
                                obj.put(Constants.isEdit, true);
                                /**
                                 * Check if quantity in produceQtyDetail and
                                 * in Batch Matches i.e. quantity mentioned
                                 * in WorkOrder is still present or have
                                 * been consumed by some other means.
                                 */
                                for (int i = 0; i < produceQtyDetails.length(); i++) {
                                    JSONObject jSONObject = (JSONObject) produceQtyDetails.get(i);
                                    String productBatchId = accCommonTablesDAO.getpurchaseBatchIdForLocationWarehouseRowRackBin(jSONObject.getString("productid"), jSONObject.getString("location"), jSONObject.getString("warehouse"), jSONObject.optString("row", null), jSONObject.optString("rack", null), jSONObject.optString("bin", null), jSONObject.optString("batch", null));
                                    Map<String, Object> filterMap = new HashMap();
                                    filterMap.put(Constants.Acc_id, productBatchId);
                                    Double quantityDue = (Double) kwlCommonTablesDAOObj.getRequestedObjectFields(NewProductBatch.class, new String[]{Constants.quantityDue}, filterMap);
                                    if (quantityDue < jSONObject.optDouble(Constants.quantity)) {
                                        obj.put(Constants.readOnly, true);
                                    }
                                }
                                
                                /**
                                 * If MRP QA flow is ON and Produced product stock is accepted by QA store then disable submit button in manage quantity(readOnly = true). 
                                 */
                                
                                if (isQaApprovalFlowInMRP && produceQtyDetails.length() > 0) {                                    
                                    if (!isTransactionSendForQA) {
                                        JSONObject json = new JSONObject();
                                        json.put("wocdid", workOrderComponentDetails.getID());
                                        KwlReturnObject kwlReturnObject = stockService.getWOCDetailISTMapping(json);
                                        List<WOCDetailISTMapping> wocDetailISTMappings = kwlReturnObject.getEntityList();
                                        for (WOCDetailISTMapping wocDetailISTMapping : wocDetailISTMappings) {
                                            if (wocDetailISTMapping.getApprovedInterStoreTransferRequests() != null && !wocDetailISTMapping.getApprovedInterStoreTransferRequests().isEmpty()) {
                                                isTransactionSendForQA = true;
                                            }
                                            if (wocDetailISTMapping.getRejectedInterStoreTransferRequests() != null && !wocDetailISTMapping.getRejectedInterStoreTransferRequests().isEmpty()) {
                                                isTransactionSendForQA = true;
                                            }
                                            if (wocDetailISTMapping.getInterStoreTransferRequest() != null) {
                                                if (wocDetailISTMapping.getInterStoreTransferRequest().getStatus() != InterStoreTransferStatus.INTRANSIT) {
                                                    isTransactionSendForQA = true;
                                                }
                                            }
                                        }
                                    }
                                    if(isTransactionSendForQA){
                                       obj.put(Constants.readOnly, true);
                                    }
                                }
                            }
                        } catch (JSONException ex) {
                        }
                    }
                } else {
                    /**
                     * No consumption Detail describes MQ for first time. In
                     * which initial available quantity is same as available
                     * quantity.
                     */
                    obj.put(Constants.initialAvailableQuantity, availableQuantity);
                }
                /**
                 * get MasterItem for status of closed WO in current company to
                 * compare it with current WO status.
                 */
                HashMap<String, Object> params = new HashMap<>();
                params.put("defaultStatusId", Constants.defaultWOstatus_CLOSED);
                params.put("companyId", workOrderComponentDetails.getWorkOrder().getCompany().getCompanyID());
                KwlReturnObject kmsg = workOrderDAOObj.getWOStatusidFromDefaultID(params);
                if (kmsg.getEntityList().size() > 0) {
                    MasterItem miObj = (MasterItem) kmsg.getEntityList().get(0);
                    if (workOrderComponentDetails.getWorkOrder().getWorkOrderStatus().getID().equals(miObj.getID())) {
                        obj.put(Constants.readOnly, true);
                    }
                }
            
                
                obj.put("orderdetailid", workOrderComponentDetails.getID());
                obj.put("lockquantity", totalLockQuantity);
                obj.put("workorderid", workOrderComponentDetails.getWorkOrder().getID());          
                obj.put(WorkOrderComponentDetails.PARAM_BLOCKED_QUANTITY,workOrderComponentDetails.getBlockQuantity());
                obj.put(WorkOrderComponentDetails.PARAM_AVAILABLE_QUANTITY,availableQuantityForUse);
                obj.put(WorkOrderComponentDetails.PARAM_REQUIRED_QUANTITY,workOrderComponentDetails.getRequiredQuantity());
                  obj.put("level", 0);
                obj.put("consumptiondetails", consumptionDetails != null ? consumptionDetails.toString() : null);
                obj.put("parentid",workOrderComponentDetails.getParentProduct() != null ? workOrderComponentDetails.getParentProduct().getID():"");
                
                                
                /* ERP-37264 : Below piece of code has written to calculate Balance Quantity of Product in 'W/L window' when it used in multiple work order.
                   Balance Quantity = (Available Quantity-Total Blocked Quantity) + Blocked Quantity for selected Work Order.
                   Here, we replace value of 'avlquantity' key from 'blockdetails' String which is saved while creating Work Order.
                   It reflects in Consumption Detail window when we click on manage quantity.
                 */
                if (workOrderComponentDetails.getBlockDetails() != null && workOrderComponentDetails.getBlockDetails() != "") {
                    double avlqty = 0;
                    JSONArray blockdetailsArr = new JSONArray();
                    JSONArray jArr = new JSONArray(workOrderComponentDetails.getBlockDetails());
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject blockdetailsObj = jArr.getJSONObject(i);
                        avlqty = availableQuantity - totalLockQuantity; //Product's available quantity - Total Blocked Quantity
                        blockdetailsObj.put("avlquantity", avlqty);
                        blockdetailsArr.put(blockdetailsObj);
                    }
                    obj.put("blockdetails", !StringUtil.isNullOrEmpty(blockdetailsArr.toString()));
                } else {
                    obj.put("blockdetails", !StringUtil.isNullOrEmpty(workOrderComponentDetails.getBlockDetails()) ? workOrderComponentDetails.getBlockDetails() : "");
                }
                 
                /*
                get rejected, wasted and recycled item details
                */
                obj.put(WorkOrderComponentDetails.PARAM_REJECTED_QUANTITY,workOrderComponentDetails.getRejectedQuantity());
                obj.put(WorkOrderComponentDetails.PARAM_WASTE_QUANTITY,workOrderComponentDetails.getWastedQuantity());
                obj.put(WorkOrderComponentDetails.PARAM_RECYCLE_QUANTITY,workOrderComponentDetails.getRecycledQuantity());
                obj.put("blockdetails", !StringUtil.isNullOrEmpty(workOrderComponentDetails.getBlockDetails()) ? workOrderComponentDetails.getBlockDetails() : "");
                
                JArr.put(obj);
                if(workOrderComponentDetails.getProduct().getProducttype().getID().equalsIgnoreCase(Producttype.ASSEMBLY)){
                    JSONArray jarrObj = new JSONArray();
//                    JArr=getAssemblyProducts(workOrderComponentDetails.getProduct(), requestParams ,jarrObj );
                    
                    /**
                     * If work order contains ProductID and check it is equal to productidtemp then put bomdetailid into requestParams.
                     */
                    if(workOrderComponentDetails.getWorkOrder().getProductID().getID().equals(productidtemp)){
                        requestParams.put("bomdetailid",workOrderComponentDetails.getWorkOrder().getBomid().getID());
                    }
                    
                    /* ERP-37264 : Below piece of code has written to calculate Available Quantity of BOM Product of Parent Assembly in 'Inventory Consumption' Window.
                     Available Quantity = (Available Quantity-Total Blocked Quantity).
                     Here, we replace value of 'availablequantity' key from 'getAssemblyProducts()'s JSONArray' with updated value.
                     */                   
                    jarrObj=getAssemblyProducts(workOrderComponentDetails.getProduct(), requestParams ,jarrObj );
                    for (int i = 0; i < jarrObj.length(); i++) {
                        JSONObject subassemblyJsonObj = jarrObj.getJSONObject(i);
                        double availableQty = subassemblyJsonObj.optDouble("availablequantity") - subassemblyJsonObj.optDouble("lockquantity");
                        subassemblyJsonObj.put("availablequantity", availableQty);
                        JArr.put(subassemblyJsonObj);
                    }
                }
                
            }
        } 
        return JArr;

    }
    @Override
    public JSONObject getProductDetailJSON(Product product,Map<String,Object> requestParams) throws ServiceException ,JSONException {
        KwlReturnObject kwlReturnObject = null;
        String companyid = product.getCompany().getCompanyID();
        kwlReturnObject = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) kwlReturnObject.getEntityList().get(0);
            JSONObject obj = new JSONObject();
            String productType = "";
            productType = (product.getProducttype() != null ? product.getProducttype().getName() : "");
            obj.put(Constants.productid, product.getID());
            obj.put("productname", product.getName());
            obj.put("productType", productType);
            obj.put("isActive", product.isIsActive());
            obj.put("hasAccess", product.isIsActive());
            obj.put("desc", product.getDescription());
            obj.put("isProductQAFlowActivate", product.isQaenable());
            obj.put("supplierpartnumber", StringUtil.isNullOrEmpty(product.getSupplier()) ? "" : product.getSupplier());
            obj.put("minorderingquantity", product.getMinOrderingQuantity());
            obj.put("maxorderingquantity", product.getMaxOrderingQuantity());
            obj.put("isAutoAssembly", product.isAutoAssembly());
            UnitOfMeasure uom = product.getUnitOfMeasure();
            obj.put("uomid", uom == null ? "" : uom.getID());
            obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
            obj.put("uomallowprecision",(uom!=null)?uom.getAllowedPrecision():"");
            obj.put("multiuom", product.isMultiuom());
            obj.put("blockLooseSell", product.isblockLooseSell());
            obj.put("uomschematypeid", product.getUomSchemaType() != null ? product.getUomSchemaType().getID() : "");
            UnitOfMeasure purchaseuom = product.getPurchaseUOM();
            UnitOfMeasure salesuom = product.getSalesUOM();
            obj.put("purchaseuom", purchaseuom == null ? "" : purchaseuom.getID());
            //obj.put("purchaseuomname", purchaseuom == null ? "" : purchaseuom.getName());
            obj.put("salesuom", salesuom == null ? "" : salesuom.getID());
            //obj.put("salesuomname", salesuom == null ? "" : salesuom.getName());
            obj.put("stockuom", (product.getPackaging() == null || product.getPackaging().getStockUoM() == null) ? (uom == null ? "" : uom.getID()) : product.getPackaging().getStockUoM().getID());
            obj.put("caseuom", (product.getPackaging() == null || product.getPackaging().getCasingUoM() == null) ? "" : product.getPackaging().getCasingUoM().getID());
            obj.put("inneruom", (product.getPackaging() == null || product.getPackaging().getInnerUoM() == null) ? "" : product.getPackaging().getInnerUoM().getID());
            obj.put("caseuomvalue", (product.getPackaging() == null || product.getPackaging().getCasingUoM() == null) ? 1 : product.getPackaging().getStockUomQtyFactor(product.getPackaging().getCasingUoM()));
            obj.put("inneruomvalue", (product.getPackaging() == null || product.getPackaging().getInnerUoM() == null) ? 1 : product.getPackaging().getStockUomQtyFactor(product.getPackaging().getInnerUoM()));
            obj.put("stockpurchaseuomvalue", purchaseuom == null || product.getPackaging() == null ? 1 : product.getPackaging().getStockUomQtyFactor(purchaseuom));
            obj.put("stocksalesuomvalue", salesuom == null || product.getPackaging() == null ? 1 : product.getPackaging().getStockUomQtyFactor(salesuom));
            obj.put("isLocationForProduct", product.isIslocationforproduct());
            obj.put("isWarehouseForProduct", product.isIswarehouseforproduct());
            obj.put("isRowForProduct", product.isIsrowforproduct());
            obj.put("isRackForProduct", product.isIsrackforproduct());
            obj.put("isBinForProduct", product.isIsbinforproduct());
            obj.put("isBatchForProduct", product.isIsBatchForProduct());
            obj.put("isSerialForProduct", product.isIsSerialForProduct());
            obj.put("isSKUForProduct", product.isIsSKUForProduct());
            obj.put("productpurchaseaccountid", product.getPurchaseAccount() != null ? product.getPurchaseAccount().getID() : "");
            obj.put("productsalesaccountid", product.getSalesAccount() != null ? product.getSalesAccount().getID() : "");
            //obj.put("isRecyclable", product.isRecyclable());
            //obj.put("recycleQuantity", product.getRecycleQuantity());
            obj.put("warrantyperiod", product.getWarrantyperiod());
            obj.put("percentage", 100);
            obj.put("rejectedQuantity", 0);
            obj.put("warrantyperiodsal", product.getWarrantyperiodsal());
            obj.put("location", (product.getLocation() != null ? product.getLocation().getId() : ""));
            obj.put("warehouse", (product.getWarehouse() != null ? product.getWarehouse().getId() : ""));
            String type="";
            if (product.getProducttype() != null) {
                if (storageHandlerImpl.GetVRnetCompanyId().contains(companyid)) {
                    if (StringUtil.equal(product.getProducttype().getName(), "Inventory Assembly")) {
                        type = "Inventory Bundle";
                    } else {
                        type = product.getProducttype().getName();
                    }
                } else {
                    type = product.getProducttype().getName();
                }
            }

       
            if (product.isAsset()) {     //For Fixed Asset Group, type will be "Asset"
                obj.put("type", "Asset");
            } else {
                obj.put("type", type);
            }
            obj.put("pid", product.getProductid());
            obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
            if (preferences.isShowprodserial()) {
                ProductBatch batch = getBatchDetailsForProduct(product.getID());
                if (batch != null) {
                    obj.put("location", batch.getLocation().getId());
                    obj.put("warehouse", batch.getWarehouse().getId());
                } else {
                    obj.put("location", "");
                    obj.put("warehouse", "");
                }
            }
            if (product.isblockLooseSell()) {
                KwlReturnObject result = accProductObj.getAvailableQuantityInSelectedUOM(product.getID(), product.getUnitOfMeasure().getID());
                obj.put("quantity", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));
            } else {
                KwlReturnObject result = accProductObj.getQuantity(product.getID());;
                obj.put("quantity", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));
            }
            
            KwlReturnObject result2 = accProductObj.getAssemblyLockQuantity(product.getID()); //get the lock quantity locked in assembly type of product in SO
            Double assmblyLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));

            KwlReturnObject result1 = accProductObj.getLockQuantity(product.getID());//get the lock quantity locked in inventory type of product in  all SO
            Double SoLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));
            obj.put("lockquantity", assmblyLockQuantity + SoLockQuantity);  //total lock quantity of product locked in SO
            /**
             * QUANTITY_DIGIT_AFTER_DECIMAL based on CompanyAccountPreferences
             * needed in PM Side (inventory consumption window) for displaying values in proper format.
             */
            
            int QUANTITY_DIGIT_AFTER_DECIMAL=4;
            QUANTITY_DIGIT_AFTER_DECIMAL=preferences.getQuantitydigitafterdecimal();
            obj.put("QUANTITY_DIGIT_AFTER_DECIMAL", QUANTITY_DIGIT_AFTER_DECIMAL); 
            return obj;
    }
    
     
    public JSONArray getAssemblyItemsJson(List<ProductAssembly> list,Map<String,Object> requestParams ,JSONArray jArr) throws ServiceException, SessionExpiredException {
        try {
            
            
            boolean isLocationForProduct = false;
            boolean isWarehouseForProduct = false;
            boolean isBatchForProduct = false;
            boolean isSerialForProduct = false;
            boolean isRowForProduct = false;
            boolean isRackForProduct = false;
            boolean isBinForProduct = false;
            boolean isAssemblyProduct =false;
            boolean isManageQuantity =false;
            boolean isForCompAvailablity =false;
            double mrpProductQuantity = 0.0;
            
             int levelCount=0;
             if(requestParams.containsKey("levelCount")){
                 levelCount=(Integer) requestParams.get("levelCount");
             }
             
            String productid = "";
            if (requestParams.containsKey("mrproductquantity")) {
                mrpProductQuantity = Double.parseDouble((String)requestParams.get("mrproductquantity"));
            }
            if (requestParams.containsKey("isForCompAvailablity") ) {
                isForCompAvailablity = Boolean.parseBoolean((String)requestParams.get("isForCompAvailablity"));
            }
            if (requestParams.containsKey("isManageQuantity") ) {
                isManageQuantity = Boolean.parseBoolean((String)requestParams.get("isManageQuantity"));
            }
            String mainProductid = "";
            if (requestParams.containsKey("productId") ) {
                mainProductid = (String)requestParams.get("productId");
            }
                    
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                isAssemblyProduct = false;
                Product product=null;
                Object[] row = (Object[]) itr.next();
                ProductAssembly passembly = (ProductAssembly) row[0];
                JSONObject obj = new JSONObject();
                String subProductid = passembly.getSubproducts().getID();
                double availableRecyclableQuantity = 0;
                  if (!StringUtil.isNullOrEmpty(passembly.getSubproducts().getID())) {
                    KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), passembly.getSubproducts().getID());
                    product = (Product) prodresult.getEntityList().get(0);
                    isLocationForProduct = product.isIslocationforproduct();
                    isWarehouseForProduct = product.isIswarehouseforproduct();
                    if (isForCompAvailablity) {
                        if (product.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                            isAssemblyProduct = true;
                            productid = product.getID();
                        }
                    }
                    isBatchForProduct = product.isIsBatchForProduct();
                    isSerialForProduct = product.isIsSerialForProduct();
                    isRowForProduct = product.isIsrowforproduct();
                    isRackForProduct = product.isIsrackforproduct();
                    isBinForProduct = product.isIsbinforproduct();
                    availableRecyclableQuantity = product.getRecycleQuantity();
                }
                   int ComponentType=0;
                ComponentType=passembly.getComponentType();
                
                double crate=0;
                crate=passembly.getCrate();
                
                obj.put("componentType", ComponentType);
                obj.put("crate", crate);
                obj.put("isLocationForProduct", isLocationForProduct);
                obj.put("isWarehouseForProduct", isWarehouseForProduct);
                obj.put("isBatchForProduct", isBatchForProduct);
                obj.put("isSerialForProduct", isSerialForProduct);
                obj.put("isRowForProduct", isRowForProduct);
                obj.put("isRackForProduct", isRackForProduct);
                obj.put("isBinForProduct", isBinForProduct);
                obj.put("id", passembly.getID());
                obj.put(Constants.productid, passembly.getSubproducts().getID());
                obj.put("hasAccess", passembly.getSubproducts().isIsActive());
                obj.put("productname", passembly.getSubproducts().getName());
                obj.put("desc", passembly.getSubproducts().getDescription());
                obj.put("producttype", passembly.getSubproducts().getProducttype().getID());
                obj.put("isProductQAFlowActivate", passembly.getSubproducts().isQaenable());                
                obj.put("type", passembly.getSubproducts().getProducttype().getName());
                obj.put("isWastageApplicable", passembly.getSubproducts().isWastageApplicable());
                obj.put("purchaseprice", row[1] == null ? 0 : row[1]); //uncomment this line to pass purchaseprice (required to calculate total cost in work order close Batch serial Window)
                //obj.put("saleprice", row[2] == null ? 0 : row[2]);              
                obj.put("ratioquantity", passembly.getQuantity());              
                double availableQuantity = passembly.getSubproducts()!= null?passembly.getSubproducts().getAvailableQuantity():0.0;
                obj.put("availablequantity", availableQuantity);
                obj.put("quantity", availableQuantity);
//                obj.put("actualquantity", passembly.getQuantity());
                double reqQuantity = passembly.getQuantity() * mrpProductQuantity;
                obj.put("requiredquantity", reqQuantity);
                double shortfallQuantity = reqQuantity - availableQuantity;
                double reorderQuantity = passembly.getSubproducts()!= null?passembly.getSubproducts().getReorderQuantity():0.0;
                String reorderStr  = "";
                if (shortfallQuantity < 0) {
                    shortfallQuantity = shortfallQuantity < 0?0.0:shortfallQuantity;
                    reorderStr = "0.0";
                }else {
                    shortfallQuantity = shortfallQuantity < 0?0.0:shortfallQuantity;
                    reorderStr = reorderQuantity + "(" + shortfallQuantity + ")";
                }
                obj.put("shortfallquantity", shortfallQuantity);
                obj.put("orderquantity", reorderStr);
                if (shortfallQuantity > 0 ) {
                    obj.put("genpo", "yes");
                } else {
                    obj.put("genpo", "no");
                }
                              
                obj.put("inventoryquantiy", passembly.getQuantity());
                obj.put("percentage", passembly.getPercentage());
                obj.put("availablerecylequantity", availableRecyclableQuantity);
                obj.put("subbomid", passembly.getSubbom() != null ? passembly.getSubbom().getID() : "");
                obj.put("subbomcode", passembly.getSubbom() != null ? passembly.getSubbom().getBomCode() : "");
                obj.put("uomid",(passembly.getSubproducts()!=null && passembly.getSubproducts().getUnitOfMeasure()!=null)?passembly.getSubproducts().getUnitOfMeasure().getID():"");
                obj.put("uomname",(passembly.getSubproducts()!=null && passembly.getSubproducts().getUnitOfMeasure()!=null)?passembly.getSubproducts().getUnitOfMeasure().getNameEmptyforNA():"");
                obj.put("uomallowprecision",(passembly.getSubproducts()!=null && passembly.getSubproducts().getUnitOfMeasure()!=null)?passembly.getSubproducts().getUnitOfMeasure().getAllowedPrecision():"");
                
                if (requestParams.containsKey("projectId")) {
                    requestParams.put("productId",passembly.getSubproducts().getID());
                    KwlReturnObject result = workOrderDAOObj.getWorkOrderComponentDetails(requestParams);
                    List<WorkOrderComponentDetails> detailsesList = result.getEntityList();
                    for (WorkOrderComponentDetails workOrderComponentDetails : detailsesList) {
                        obj.put("orderdetailid", workOrderComponentDetails.getID());
                        obj.put("workorderid", workOrderComponentDetails.getWorkOrder().getID());
//                        if (!isAssemblyProduct) {
//                            obj.put("consumptiondetails", workOrderComponentDetails.getConsumptionDetails());
//                        }
                        obj.put("consumptiondetails", workOrderComponentDetails.getConsumptionDetails());
                        obj.put("blockdetails", !StringUtil.isNullOrEmpty(workOrderComponentDetails.getBlockDetails()) ? workOrderComponentDetails.getBlockDetails() : "");
                        obj.put("parentid", workOrderComponentDetails.getParentProduct() != null ? workOrderComponentDetails.getParentProduct().getID():"");
                        obj.put(WorkOrderComponentDetails.PARAM_BLOCKED_QUANTITY, workOrderComponentDetails.getBlockQuantity());
                        obj.put(WorkOrderComponentDetails.PARAM_REQUIRED_QUANTITY, workOrderComponentDetails.getRequiredQuantity());
                        obj.put("purchaseprice", workOrderComponentDetails.getInitialPurchasePrice());
                        if (ComponentType == 2 || ComponentType == 3) {
                            double producedQuantity = workOrderComponentDetails.getProducedQuantity();
                            if (producedQuantity > 0) {
                                obj.put("ratioquantity", producedQuantity);
                                obj.put("inventoryquantiy", producedQuantity);                                                            
                           }else{
                                obj.put("ratioquantity", passembly.getQuantity()/workOrderComponentDetails.getWorkOrder().getQuantity());
                                obj.put("inventoryquantiy", passembly.getQuantity()/workOrderComponentDetails.getWorkOrder().getQuantity());                                                            
                           }
                                
                        }
                        
                        /*
                        put merge batch details
                        */
                        String consumptionDetails = workOrderComponentDetails.getConsumptionDetails();
                        if (!StringUtil.isNullOrEmpty(consumptionDetails) && workOrderComponentDetails.getProduct() != null) {
                            JSONObject jobj = new JSONObject(consumptionDetails);
                            String batchdetails = "";
                            if (jobj.has("batchdetails")) {
                                batchdetails = (String) jobj.getString("batchdetails");
                            }
                            String producedqtydetails = "";
                            if (jobj.has("producedqtydetails")) {
                                producedqtydetails = (String) jobj.getString("producedqtydetails");
                            }
                            String wasteQtyDetails = "";
                            if (jobj.has(WorkOrderComponentDetails.WASTE_DETAILS)) {
                                wasteQtyDetails = (String) jobj.getString(WorkOrderComponentDetails.WASTE_DETAILS);
                            }
                            Double wasteQty = 0.0;
                            if (!StringUtil.isNullOrEmpty(wasteQtyDetails)) {
                                JSONArray wasteQtyArr = new JSONArray(wasteQtyDetails);
                                /**
                                 * wasteQty Required to adjust initial Available
                                 * quantity in case when Waste Stocked OUT.
                                 */
                                if (jobj.optInt(Constants.wasteMovementFlag) == Constants.MRP_WASTEMOVEMENTFLAG_STOCKOUT) {
                                    for (int i = 0; i < wasteQtyArr.length(); i++) {
                                        wasteQty += wasteQtyArr.optJSONObject(i).optDouble(Constants.quantity);
                                    }
                                }
                            }
                            JSONArray finalbatcharr = new JSONArray();
                            if (!StringUtil.isNullOrEmpty(batchdetails)) {
                                JSONArray batchdetailsarr = new JSONArray(batchdetails);
                                Double consumedQty = 0.0;
                                for (int i = 0; i < batchdetailsarr.length(); i++) {
                                    finalbatcharr.put(batchdetailsarr.get(i));
                                    consumedQty += batchdetailsarr.optJSONObject(i).optDouble(Constants.quantity);
                                }
                                /**
                                 * initialavailablequantity gives available quantity before MQ.
                                 */
                                obj.put(Constants.initialAvailableQuantity, availableQuantity + consumedQty + wasteQty);
                                jobj.put(Constants.initialAvailableQuantity, availableQuantity + consumedQty + wasteQty);
                                if (!StringUtil.isNullOrEmpty(producedqtydetails)) {
                                    JSONArray producedqtydetailsarr = new JSONArray(producedqtydetails);

                                    /*
                                     merge data if location,warehouse,batch same 
                                     */
                                    for (int i = 0; i < finalbatcharr.length(); i++) {

                                        JSONObject jSONObject = finalbatcharr.getJSONObject(i);
                                        String location = jSONObject.optString("location");
                                        String warehouse = jSONObject.optString("warehouse");
                                        String batch = jSONObject.optString("batch");
                                        for (int j = 0; j < producedqtydetailsarr.length(); j++) {
                                            JSONObject produceObj = producedqtydetailsarr.getJSONObject(j);
                                            String plocation = produceObj.optString("location");
                                            String pwarehouse = produceObj.optString("warehouse");
                                            String pbatch = produceObj.optString("batch");
                                            if (product.isIslocationforproduct() && product.isIswarehouseforproduct() && product.isIsBatchForProduct()) {
                                                /*
                                                If Location batch Warehouse same
                                                */
                                                if (plocation.equalsIgnoreCase(location) && pwarehouse.equalsIgnoreCase(warehouse) && pbatch.equalsIgnoreCase(batch)) {
                                                    int qty = produceObj.optInt("quantity");
                                                    int bqty = jSONObject.optInt("quantity");
                                                    finalbatcharr.getJSONObject(i).put("quantity", qty + bqty);
                                                } else {
                                                    finalbatcharr.put(produceObj);
                                                }
                                            } else if (product.isIslocationforproduct() && product.isIswarehouseforproduct()) {
                                                /*
                                                If Location Warehouse same
                                                */
                                                if (plocation.equalsIgnoreCase(location) && pwarehouse.equalsIgnoreCase(warehouse)) {
                                                    int qty = produceObj.optInt("quantity");
                                                    int bqty = jSONObject.optInt("quantity");
                                                    finalbatcharr.getJSONObject(i).put("quantity", qty + bqty);
                                                } else {
                                                    finalbatcharr.put(produceObj);
                                                }
                                            } else {
                                                finalbatcharr.put(produceObj);
                                            }

                                        }
                                    }

                                }
                            } else if (!StringUtil.isNullOrEmpty(producedqtydetails)) {
                                JSONArray producedqtydetailsarr = new JSONArray(producedqtydetails);
                                for (int i = 0; i < producedqtydetailsarr.length(); i++) {
                                    finalbatcharr.put(producedqtydetailsarr.get(i));
                                }
                            }
                            if (StringUtil.isNullOrEmpty(batchdetails)) {
                                obj.put(Constants.initialAvailableQuantity, availableQuantity + wasteQty);
                                jobj.put(Constants.initialAvailableQuantity, availableQuantity + wasteQty);
                            }
                            obj.put("batchdetails", finalbatcharr.toString());
                            /**
                             * update consumptiondetails for initialAvailableQuantity
                             */
                            obj.put("consumptiondetails", jobj.toString());
                        } else {
                            /**
                             * while doing MQ first time initialAvailableQuantity and availableQuantity is same
                             */
                            obj.put(Constants.initialAvailableQuantity, availableQuantity);
                        }
                    }
                }
//                obj.put("recylequantity", passembly.getRecycleQuantity());
//                obj.put("remainingquantity", passembly.getRemainingQuantity());
//                obj.put("onhand", row[3]==null?0:row[3]);
//                Double availableQty = (Double) (row[3] == null ? 0.0 : row[3]);  //iis the actual available wuantity for product
                KwlReturnObject result2 = accProductObj.getAssemblyLockQuantityForBuild(mainProductid, subProductid);
                Double assmblyLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));  //it is the lock quantity in assembly product locked in SO

                KwlReturnObject result1 = accProductObj.getLockQuantity(subProductid); //for geting a locked quantity of inventory product used in salesorder
                Double SoLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));   //it is the lock quantity of product locked in SO
                Double lockquantity = assmblyLockQuantity + SoLockQuantity;   //total lock quantity
                
                /**
                 * MRP
                 * In manage quantity window total block quantity will be as below:
                 * lock quantity = block quantity in SO + block quantity in all work order.
                 */
                Double WOLockQuantity=0.0;
                if(isManageQuantity){
                     KwlReturnObject workordercomponentdetails = accProductObj.getWOLockQuantity(subProductid,isManageQuantity);
                     WOLockQuantity = (Double) (workordercomponentdetails.getEntityList().get(0) == null ? 0.0 : workordercomponentdetails.getEntityList().get(0));
                     WOLockQuantity=authHandler.roundQuantity(WOLockQuantity, product.getCompany().getCompanyID());
                     lockquantity+=WOLockQuantity;
                     obj.put("location", (passembly.getSubproducts().getLocation() != null ? passembly.getSubproducts().getLocation().getId(): ""));
                     obj.put("warehouse", (passembly.getSubproducts().getWarehouse() != null ? passembly.getSubproducts().getWarehouse().getId() : ""));
                }
                obj.put("lockquantity", lockquantity);
//                obj.put("onhand", availableQty - lockquantity);   //its actual quantity available for user
                obj.put("wastageInventoryQuantity", passembly.getWastageInventoryQuantity());
                obj.put("wastageQuantityType", passembly.getWastageQuantityType());
                obj.put("wastageQuantity", passembly.getWastageQuantity());             
                obj.put("rejectedQuantity", 0);
                obj.put("level", levelCount);

                jArr.put(obj);
                if (isAssemblyProduct && !isManageQuantity) {
                     HashMap<String, Object> dataMap = new HashMap<>();
                    dataMap.put(Constants.productid, productid);
                    dataMap.put("isdefaultbom", true);
                    dataMap.put("currencyid", requestParams.get("currencyid"));
                    KwlReturnObject retObj = accProductObj.getAssemblyItems(dataMap);
                    int tmpLevel = levelCount + 1;
                    requestParams.put("levelCount", tmpLevel);
                    requestParams.put("product", product);
                    requestParams.put("isForCompAvailablity", "true");
                    List<ProductAssembly> sublist = retObj.getEntityList();
                    jArr = getAssemblyItemsJson(sublist, requestParams, jArr);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getAssemblyItemsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    @Override
    public JSONObject getBatchRemainingQuantity(Map<String,Object> requestMap) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        JSONArray detailsArray = new JSONArray();
        JSONArray JArr = new JSONArray();
        try {
            double quantity = 0;
            int moduleid = 0;
            boolean isEdit = false;
            boolean fromSubmit = false;
            boolean isfromdo = false;
            boolean isforReturn = false;
            boolean linkflag = false;
            String companyId = (String) requestMap.get(Constants.companyKey);
            String batchdetails = requestMap.containsKey("batchdetails") ?(String) requestMap.get("batchdetails"):"";
            String transType = requestMap.containsKey("transType") ? (String) requestMap.get("transType"):"";
            int transTypeformrp = 0;
                    
            if (!StringUtil.isNullOrEmpty(transType)) {
                transTypeformrp = Integer.parseInt(transType);
            }
            if (requestMap.containsKey("isEdit") && requestMap.get("isEdit") != null) { 
                isEdit = Boolean.parseBoolean((String) requestMap.get("isEdit"));
            }
            if (requestMap.containsKey("isfromdo") && requestMap.get("isfromdo") != null) {
                isfromdo = Boolean.parseBoolean((String) requestMap.get("isfromdo"));
            }
            if (requestMap.containsKey("isforReturn") && requestMap.get("isforReturn") != null) {
                isforReturn = Boolean.parseBoolean((String) requestMap.get("isforReturn"));
            }
            if (requestMap.containsKey("fromSubmit") && requestMap.get("fromSubmit") != null) {
                fromSubmit =Boolean.parseBoolean((String) requestMap.get("fromSubmit"));
            }
        
            if (requestMap.containsKey("linkflag") && requestMap.get("linkflag") != null) {
                linkflag = Boolean.parseBoolean((String) requestMap.get("linkflag"));
            }
        
            if (isfromdo) {
                String detail = (String) requestMap.get("detail");
                detailsArray = new JSONArray(detail);
                for (int k = 0; k < detailsArray.length(); k++) {
                    JSONObject detailsobj = detailsArray.getJSONObject(k);
                    if (detailsobj.has("batchdetails") && detailsobj.getString("batchdetails") != null) {
                        batchdetails = detailsobj.optString("batchdetails", "");
                    }
                    if (!StringUtil.isNullOrEmpty(batchdetails)) {
                        JSONArray batchDetailsArray = new JSONArray(batchdetails);
                        for (int i = 0; i < batchDetailsArray.length(); i++) {
                            JSONObject batchObj = batchDetailsArray.optJSONObject(i);
                            String purchasebatchid = batchObj.optString("purchasebatchid", "");
                            String documentid = batchObj.optString("documentid", "");
                            double qty = 0.0;
                            String locationid = batchObj.optString("location","");
                            String warehouseid = batchObj.optString("warehouse","");
                            qty = batchObj.optDouble("quantity", 0.0);
                            String productid = batchObj.getString(Constants.productid);
                            String prodname="";
                            Product product = null;
                            KwlReturnObject prodresult = accountingHandlerDAOobj.getObject(Product.class.getName(), productid);
                            product = prodresult != null ? (Product) prodresult.getEntityList().get(0) : null;
                            if(product.isIsSerialForProduct() || product.isIsBatchForProduct()){
                                continue;
                            }
                            if (product != null) {
                                prodname = product.getName();
                            }
                            quantity = getNewBatchRemainingQuantity(locationid, warehouseid, companyId, productid, purchasebatchid, transType, isEdit, documentid,linkflag);
//                            if (fromSubmit) {  // on submit window check all rows quantity are availble otherwise make quantty unavaiabale                            
                             if (isforReturn ?(qty > quantity):(quantity < qty)) {
                                    jobj.put("prodname", prodname);
                                    quantity = 0;
                                    break;
//                                }
                            }
                        }
                    }
                }
            }else{               
            if (!StringUtil.isNullOrEmpty(batchdetails)) {
                JSONArray batchDetailsArray = new JSONArray(batchdetails);
                for (int i = 0; i < batchDetailsArray.length(); i++) {
                    JSONObject batchObj = batchDetailsArray.optJSONObject(i);
                    String purchasebatchid = batchObj.optString("purchasebatchid","");
                    String documentid = batchObj.optString("documentid","");
                    double qty = 0.0;
                    String locationid = batchObj.getString("location");
                    String warehouseid = batchObj.getString("warehouse");
//                     qty = batchObj.optDouble("quantity",0.0);
//                    if (!StringUtil.isNullOrEmpty(enterquantity)) {
//                        qty = Integer.parseInt(enterquantity);
//                    }
                    String productid = batchObj.getString(Constants.productid);
                    /**
                     * get all WCD of same products in a WO to add into balance
                     * quantity.
                     */
                    
                    /*
                        Checked the projectid of current WO if present 
                    */
                    if (requestMap.containsKey(Constants.projectId)) {
                        requestMap.put(Constants.productId, productid);

                        /*
                            Calculated total block quantity from Current WO for provided product.
                        */
                        KwlReturnObject result = workOrderDAOObj.getWorkOrderComponentDetails(requestMap);
                        List<WorkOrderComponentDetails> detailsesList = result.getEntityList();
                        StringBuilder wcdProductId = new StringBuilder();
                        
                        /*
                            Loop to get all block quantity for particular product from work order details
                        */
                        for (WorkOrderComponentDetails workOrderComponentDetails : detailsesList) { 
                            wcdProductId.append(workOrderComponentDetails.getProduct().getID());
                            if (wcdProductId.toString().equals(productid)) {
                                /**
                                 * add only if Blocked quantity is not used.
                                 */
                                if (!workOrderComponentDetails.isBlockQtyUsed()) {
                                    /**
                                     * Add only if blocked from particular
                                     * warehouse, location and purchasebatchid.
                                     */
                                    JSONArray blockDetails = new JSONArray(workOrderComponentDetails.getBlockDetails());
                                    for (int blockIndex = 0; blockIndex < blockDetails.length(); blockIndex++){
                                        JSONObject blockDetailjobj = blockDetails.optJSONObject(blockIndex);
                                        String tempLocation = blockDetailjobj.optString("location");
                                        String tempWarehouse = blockDetailjobj.optString("warehouse");
                                        String tempPurchaseBatchID = blockDetailjobj.optString("purchasebatchid");
                                        if(tempLocation.equals(locationid) && tempWarehouse.equals(warehouseid) && tempPurchaseBatchID.equals(purchasebatchid)){
                                            qty += blockDetailjobj.optDouble("quantity");
                                        }
                                    }
                                }
                            }
                            wcdProductId.setLength(0);
                        }
                    } else {
                        qty = batchObj.optDouble("quantity", 0.0);
                    }
                    
                    quantity = getNewBatchRemainingQuantity(locationid, warehouseid, companyId, productid, purchasebatchid, transType,isEdit,documentid,linkflag);  
                    
                    /**
                     * MRP (Constants.MRP_WORK_ORDER_MODULEID=1105)
                     * In Manage Quantity Batch Serial Window will show Available quantity as per below formula
                     * Available Quantity=Total Quantity of Product P1 - Block Quantity of Product P1 in Other Work Order
                     * ((quantityDue-lockQuantity)+actualQuantity).
                     */
                    if (transTypeformrp == Constants.MRP_WORK_ORDER_MODULEID) { 
                        quantity = quantity + qty;
                    }
                    if (fromSubmit) {  // on submit window check all rows quantity are availble otherwise make quantty unavaiabale
                        if (quantity < qty) {
                            quantity = 0;
                            break;
                        }
                    }
                }
            }
            }
            jobj.put("quantity", authHandler.formattedQuantity(quantity, companyId));
            JSONObject obj=new JSONObject();
            obj.put("quantity", authHandler.formattedQuantity(quantity, companyId));
            JArr.put(obj);
            jobj.put("data", JArr);
            issuccess = true;
            msg = "Batch Remaining Quantity has been received successfully.";
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return jobj;
    }
     
    @Override
    public JSONObject getBatchRemainingQuantityForMultipleRecords(Map<String, Object> requestMap) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        JSONArray JArray = new JSONArray();
        try {
            double quantity = 0;
            int moduleid = 0;
            boolean isEdit = false, isManageQtyCall = false;
            boolean linkflag = false;
            String companyId = (String) requestMap.get(Constants.companyKey);
            String batchdetails = (String) requestMap.get("batchdetails");
            String transType = (String) requestMap.get("transType");

            int transTypeformrp = 0;
            if (!StringUtil.isNullOrEmpty(transType)) {
                transTypeformrp = Integer.parseInt(transType);
            }
            if (requestMap.containsKey("isManageQty") && requestMap.get("isManageQty") != null) {
                isManageQtyCall = Boolean.parseBoolean(requestMap.get("isManageQty").toString());
            }

            boolean fromSubmit = false;
            if (requestMap.containsKey("fromSubmit") && requestMap.get("fromSubmit") != null) {
                fromSubmit =Boolean.parseBoolean((String) requestMap.get("fromSubmit"));
            }
            if (requestMap.containsKey("isEdit") && requestMap.get("isEdit") != null) {
                isEdit = Boolean.parseBoolean(requestMap.get("isEdit").toString());
            }
            if (requestMap.containsKey("linkflag") && requestMap.get("linkflag") != null) {
                linkflag = (Boolean) requestMap.get("linkflag");
            }

            if (!StringUtil.isNullOrEmpty(batchdetails)) {
                JSONArray batchDetailsArray = new JSONArray(batchdetails);
                for (int i = 0; i < batchDetailsArray.length(); i++) {
                    JSONObject batchObj = batchDetailsArray.optJSONObject(i);
                    String purchasebatchid = batchObj.optString("purchasebatchid", "");
                    String documentid = batchObj.optString("documentid", "");
                    double qty = 0.0;
                    String locationid = batchObj.optString("location", "");
                    String warehouseid = batchObj.optString("warehouse", "");
                    qty = batchObj.optDouble("quantity", 0.0);
                    String productid = batchObj.getString(Constants.productid);
                    /**
                     * get all WCD of same products in a WO to add into balance
                     * quantity.
                     */

                    /*
                     Checked the projectid of current WO if present 
                     */
                    if (isManageQtyCall) {
                        if (requestMap.containsKey(Constants.projectId)) {
                            requestMap.put(Constants.productId, productid);

                            /*
                             Calculated total block quantity from Current WO for provided product.
                             */
                            KwlReturnObject result = workOrderDAOObj.getWorkOrderComponentDetails(requestMap);
                            List<WorkOrderComponentDetails> detailsesList = result.getEntityList();
                            StringBuilder wcdProductId = new StringBuilder();

                            /*
                             Loop to get all block quantity for particular product from work order details
                             */
                            qty = 0;
                            for (WorkOrderComponentDetails workOrderComponentDetails : detailsesList) {
                                wcdProductId.append(workOrderComponentDetails.getProduct().getID());
                                if (wcdProductId.toString().equals(productid)) {
                                    /**
                                     * add only if Blocked quantity is not used.
                                     */
                                    if (!workOrderComponentDetails.isBlockQtyUsed()) {
                                        /**
                                         * Add only if blocked from particular
                                         * warehouse, location and
                                         * purchasebatchid.
                                         */
                                        JSONArray blockDetails = new JSONArray(workOrderComponentDetails.getBlockDetails());
                                        for (int blockIndex = 0; blockIndex < blockDetails.length(); blockIndex++) {
                                            JSONObject blockDetailjobj = blockDetails.optJSONObject(blockIndex);
                                            String tempLocation = blockDetailjobj.optString("location");
                                            String tempWarehouse = blockDetailjobj.optString("warehouse");
                                            String tempPurchaseBatchID = blockDetailjobj.optString("purchasebatchid");
                                            if (tempLocation.equals(locationid) && tempWarehouse.equals(warehouseid) && tempPurchaseBatchID.equals(purchasebatchid)) {
                                                qty += blockDetailjobj.optDouble("quantity");
                                            }
                                        }
                                    }
                                }
                                wcdProductId.setLength(0);
                            }
                        } else {
                            qty = batchObj.optDouble("quantity", 0.0);
                        }
                    }
                    quantity = getNewBatchRemainingQuantity(locationid, warehouseid, companyId, productid, purchasebatchid, transType, isEdit, documentid, linkflag);

                    if (isManageQtyCall) {
                        if (transTypeformrp == Constants.MRP_WORK_ORDER_MODULEID) {
                            quantity = quantity + qty;
                        }
                        if (fromSubmit) {  // on submit window check all rows quantity are availble otherwise make quantty unavaiabale
                            if (quantity < qty) {
                                quantity = 0;
                                break;
                            }
                        }
                    }
                    batchObj.put("avlquantity", quantity);
                    JArray.put(batchObj);
                }
            }
            jobj.put("data", JArray);
            issuccess = true;
            msg = "Batch Remaining Quantity has been received successfully.";
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return jobj;
    }

    private double getBatchRemainingQuantity(String purchasebatchid, int moduleid, String companyId) throws ServiceException {
        double quantity = 0.0;
        try {
            if (!StringUtil.isNullOrEmpty(purchasebatchid)) {
                quantity = accCommonTablesDAO.getBatchRemainingQuantity(purchasebatchid, moduleid, companyId);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accInvoiceController.getBatchRemainingQuantity() -: " + ex.getMessage(), ex);
        }
        return quantity;
    }
    
         private double getNewBatchRemainingQuantity(String locationid, String warehouseid, String companyId, String productid, String purchasebatchid, String transType,boolean isEdit,String documentid,boolean linkflag) throws ServiceException {
            double quantity = 0.0;
            double srquantity = 0.0;
            try {
                HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                HashMap<String, Object> filterRequestParamsforEdit = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                ArrayList filter_namesforEdit = new ArrayList(), filter_paramsforEdit = new ArrayList(), order_byforEdit = new ArrayList(), order_typeforEdit = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(companyId);
                boolean isnegativestockforlocwar = false;
                ExtraCompanyPreferences extraCompanyPreferences = null;
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
                extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
                isnegativestockforlocwar = extraCompanyPreferences.isIsnegativestockforlocwar();
                if (!StringUtil.isNullOrEmpty(warehouseid)) {
                    filter_names.add("warehouse.id");
                    filter_params.add(warehouseid);
                }
                if (!StringUtil.isNullOrEmpty(locationid)) {
                    filter_names.add("location.id");
                    filter_params.add(locationid);
                }
                if (!StringUtil.isNullOrEmpty(purchasebatchid)) {
                    filter_names.add("id");
                    filter_params.add(purchasebatchid);
                }

                filter_names.add("product");
                filter_params.add(productid);

                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);
                filterRequestParams.put("order_by", order_by);
                filterRequestParams.put("order_type", order_type);
                KwlReturnObject result = accMasterItemsDAOobj.getNewBatches(filterRequestParams,false,false);
                int inttransType = 0;
                if (!StringUtil.isNullOrEmpty(transType)) {
                    inttransType = Integer.parseInt(transType);
                }

                //as while saving consignment deliveryorder we are not saving  consignment moduleid we are saving normal do id so we are not getting the data from consignment moduleid
                if(inttransType==Constants.Acc_ConsignmentDeliveryOrder_ModuleId){
                    inttransType=Constants.Acc_Delivery_Order_ModuleId;
                }
                List listResult = result.getEntityList();
                Iterator itrResult = listResult.iterator();
                while (itrResult.hasNext()) {
                    NewProductBatch newProductBatch = (NewProductBatch) itrResult.next();
                    HashMap<String, Object> filterParamsForPRBatch = new HashMap<String, Object>();
                    ArrayList PRfilter_names = new ArrayList(), PRfilter_params = new ArrayList();
                    HashMap<String, Object> filterParamsForPRCustBatch = new HashMap<String, Object>();
                    ArrayList PRCustfilter_names = new ArrayList(), PRCustfilter_params = new ArrayList();
                    if (inttransType == Constants.Acc_Sales_Return_ModuleId || inttransType == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
                        if(inttransType == Constants.Acc_Sales_Return_ModuleId){
                        PRfilter_names.add("ispurchasereturn");
                        PRfilter_params.add(false);
                        PRfilter_names.add("transactiontype");
                        PRfilter_params.add(27);
                        PRfilter_names.add("batchmapid.id");
                        PRfilter_params.add(newProductBatch.getId());
                        filterParamsForPRBatch.put("filter_names", PRfilter_names);
                        filterParamsForPRBatch.put("filter_params", PRfilter_params);
                        KwlReturnObject PRresult = accMasterItemsDAOobj.getPRBatchQuantity(filterParamsForPRBatch);
                        List locationResult = PRresult.getEntityList();
                        Iterator LocitrResult = locationResult.iterator();
                        while (LocitrResult.hasNext()) {
                            LocationBatchDocumentMapping locationBatchDocumentMapping = (LocationBatchDocumentMapping) LocitrResult.next();
                            quantity = quantity + locationBatchDocumentMapping.getQuantity();
                        }
                        }else if(inttransType == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
                            //To Check quantity in Customer warehouse while creating Sales Return linking with DO.
                            PRCustfilter_names.add("ispurchasereturn");
                            PRCustfilter_params.add(false);
                            PRCustfilter_names.add("transactiontype");
                            PRCustfilter_params.add(28);
                            PRCustfilter_names.add("batchmapid.id");
                            PRCustfilter_params.add(newProductBatch.getId());
                            if (!StringUtil.isNullOrEmpty(documentid)) {
                                PRCustfilter_names.add("documentid");
                                PRCustfilter_params.add(documentid);
                            }
                            filterParamsForPRCustBatch.put("filter_names", PRCustfilter_names);
                            filterParamsForPRCustBatch.put("filter_params", PRCustfilter_params);
                            KwlReturnObject PRCustresult = accMasterItemsDAOobj.getPRBatchQuantity(filterParamsForPRCustBatch);
                            List locationCustResult = PRCustresult.getEntityList();
                            Iterator LocCustitrResult = locationCustResult.iterator();
                            while (LocCustitrResult.hasNext()) {
                                LocationBatchDocumentMapping locationBatchDocumentMapping = (LocationBatchDocumentMapping) LocCustitrResult.next();
                                quantity = quantity + locationBatchDocumentMapping.getQuantity();
                            }
                        }
                        //for Sales return we are showing those quantity whose DO has been made
                        HashMap<String, Object> filterParamsForSRBatch = new HashMap<String, Object>();
                        ArrayList SRfilter_names = new ArrayList(), SRfilter_params = new ArrayList();
                        SRfilter_names.add("ispurchasereturn");
                        SRfilter_params.add(false);
                        SRfilter_names.add("transactiontype");
                        SRfilter_params.add(29);
                        SRfilter_names.add("batchmapid.id");
                        SRfilter_params.add(newProductBatch.getId());
                        if (!StringUtil.isNullOrEmpty(documentid)) {
                            SRfilter_names.add("documentid");
                            SRfilter_params.add(documentid);
                        }
                        filterParamsForSRBatch.put("filter_names", SRfilter_names);
                        filterParamsForSRBatch.put("filter_params", SRfilter_params);
                        KwlReturnObject SRresult = accMasterItemsDAOobj.getPRBatchQuantity(filterParamsForSRBatch);
                        List SRLocResult = SRresult.getEntityList();
                        Iterator SRLocitrResult = SRLocResult.iterator();
                        while (SRLocitrResult.hasNext()) {
                            LocationBatchDocumentMapping locationBatchDocumentMapping = (LocationBatchDocumentMapping) SRLocitrResult.next();
                            srquantity = srquantity + locationBatchDocumentMapping.getQuantity();
                        }
                        quantity=quantity-srquantity;
                    } else {
                    
                    
                        double dueQty = newProductBatch.getQuantitydue();
                        double lockQty=0.0;
                        if(!linkflag){
                        lockQty=newProductBatch.getLockquantity();
                        }
                        //as avaiable quabtity should be  quantitydue- lock quantity otherwise those batches which
                        double avlqty=dueQty-lockQty;
                        if (avlqty > 0) {
                            quantity = quantity + avlqty;
                        }else if(isnegativestockforlocwar && avlqty < 0){
                            quantity = quantity + avlqty;
                        }
                    }
                }
                if (isEdit && !StringUtil.isNullOrEmpty(documentid)) {

                    if (!StringUtil.isNullOrEmpty(warehouseid)) {
                        filter_namesforEdit.add("batchmapid.warehouse.id");
                        filter_paramsforEdit.add(warehouseid);
                    }
                    if (!StringUtil.isNullOrEmpty(locationid)) {
                        filter_namesforEdit.add("batchmapid.location.id");
                        filter_paramsforEdit.add(locationid);
                    }
                    if (!StringUtil.isNullOrEmpty(purchasebatchid)) {
                        filter_namesforEdit.add("batchmapid.id");
                        filter_paramsforEdit.add(purchasebatchid);
                    }
                    if (!StringUtil.isNullOrEmpty(documentid)) {
                        filter_namesforEdit.add("documentid");
                        filter_paramsforEdit.add(documentid);
                    }

                    filter_namesforEdit.add("ispurchasereturn");
                    if (inttransType == Constants.Acc_Purchase_Return_ModuleId) {
                        filter_paramsforEdit.add(true);
                    } else {
                        filter_paramsforEdit.add(false);
                    }
                    filter_namesforEdit.add("transactiontype");
                    filter_paramsforEdit.add(inttransType);

                    filterRequestParamsforEdit.put("filter_names", filter_namesforEdit);
                    filterRequestParamsforEdit.put("filter_params", filter_paramsforEdit);
                    KwlReturnObject PRresult = accMasterItemsDAOobj.getPRBatchQuantity(filterRequestParamsforEdit);
                    List locationResult = PRresult.getEntityList();
                    Iterator LocitrResult = locationResult.iterator();
                    while (LocitrResult.hasNext()) {
                        LocationBatchDocumentMapping locationBatchDocumentMapping = (LocationBatchDocumentMapping) LocitrResult.next();
                        quantity = quantity + locationBatchDocumentMapping.getQuantity();
                    }
                }

            } catch (Exception ex) {
                Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE("accInvoiceController.getBatchRemainingQuantity() -: " + ex.getMessage(), ex);
            }
            return quantity;
        }
    
                
    /**
     * Function to get the Inventory Valuation Array for financial reports (For
     * example, Balance sheet, P &L Report)
     * @param requestParams
     * @author Swapnil K.
     * @return inventory valuation result into an array of double
     */
    @Override
    public double[] getInventoryValuationDataForFinancialReports(HashMap<String, Object> requestParams) {
        double[] valuation = {0, 0, 0, 0, 0, 0, 0, 0, 0};
        try {
            String startDateString = (String) requestParams.get(Constants.REQ_startdate);
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            Date startDate = null;
            if (!StringUtil.isNullOrEmpty(startDateString) && df != null) {
                startDate = df.parse(startDateString);
            }
            requestParams.remove(Constants.REQ_startdate);
            boolean isDifferenceInOpeningBalanceFlag = false,deductSOBlockedQtyFromValuation=false;
            if (requestParams.containsKey("isDifferenceInOpeningBalanceFlag") && requestParams.get("isDifferenceInOpeningBalanceFlag") != null) {
                isDifferenceInOpeningBalanceFlag = Boolean.parseBoolean(requestParams.get("isDifferenceInOpeningBalanceFlag").toString());
            }
            String endDateString = (String) requestParams.get(Constants.REQ_enddate);
            Date endDate = null;
            if (!StringUtil.isNullOrEmpty(endDateString) && df != null) {
                endDate = df.parse(endDateString);
            }
            CompanyAccountPreferences pref = null;
            if (requestParams.containsKey(Constants.preferences) && requestParams.get(Constants.preferences) != null) {
                pref = (CompanyAccountPreferences) requestParams.get(Constants.preferences);
            } else {
                KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), (String) requestParams.get(Constants.companyKey));
                pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
            }
            if (pref.getNegativestock() != 1 || pref.getNegativeStockPR() != 1) { // Block Case for DO & PR
                if (!isDifferenceInOpeningBalanceFlag) { // No need to remove the end date for Difference in opening balance 
                    requestParams.remove(Constants.REQ_enddate);
                }
            }
            ExtraCompanyPreferences extrapref = null;
            if (requestParams.containsKey(Constants.extraCompanyPreferences) && requestParams.get(Constants.extraCompanyPreferences) != null) {
                extrapref = (ExtraCompanyPreferences) requestParams.get(Constants.extraCompanyPreferences);
            } else {
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), (String) requestParams.get(Constants.companyKey));
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            }
            if (!StringUtil.isNullOrEmpty(extrapref.getColumnPref())) {
                JSONObject columnPrefjObj = new JSONObject((String) extrapref.getColumnPref());
                if (columnPrefjObj.has("deductSOBlockedQtyFromValuation") && columnPrefjObj.get("deductSOBlockedQtyFromValuation") != null && columnPrefjObj.optBoolean("deductSOBlockedQtyFromValuation", false)) {
                    deductSOBlockedQtyFromValuation = true;
                }
            }
            Map<String, List> proMap = null;
            Map<String, Map> productAdvSearchMap = null;
            boolean isAdvanceSearchForValuation = false;
            if (requestParams.containsKey(Constants.Acc_Search_Json) && requestParams.get(Constants.Acc_Search_Json) != null && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
                /**
                 * Code for advance search SDP-9634.
                 */
                HashMap<String, Object> tempRequestParams = new HashMap<>(requestParams);
                String searchJson = (String) requestParams.get(Constants.Acc_Search_Json);
                tempRequestParams.remove(Constants.Acc_Search_Json);
                proMap = getProductTransaction(tempRequestParams);
                tempRequestParams.put(Constants.Acc_Search_Json, searchJson);
                tempRequestParams.put("isAdvanceSearchForValuation", true);
                tempRequestParams.put(Constants.Filter_Criteria, requestParams.get(Constants.Filter_Criteria));
                productAdvSearchMap = getProductTransactionForAdvanceSearch(tempRequestParams);
                isAdvanceSearchForValuation = true;
            } else {
                proMap = getProductTransaction(requestParams);
            }
            boolean isMaterialInOutReport = false;// Material IN/OUT Report
            if (requestParams.containsKey("isMaterialInOutReport") && requestParams.get("isMaterialInOutReport") != null) {
                isMaterialInOutReport = Boolean.parseBoolean(requestParams.get("isMaterialInOutReport").toString());
            }
            if (requestParams.containsKey(Constants.basecurrencyid)) {
                requestParams.put("basecurrencyid", (String) requestParams.get(Constants.basecurrencyid));
            } else {
                requestParams.put("basecurrencyid", pref.getCompany().getCurrency().getCurrencyID());
            }
            JSONObject blockqtyJson = new JSONObject();
            blockqtyJson.put("df", df);
            blockqtyJson.put("startdate", startDate);
            blockqtyJson.put("enddate", endDate);
            blockqtyJson.put("deductSOBlockedQtyFromValuation", deductSOBlockedQtyFromValuation);
            blockqtyJson.put("companyid",  (String) requestParams.get(Constants.companyKey));
            if (!proMap.isEmpty()) {
                Product product = null;
                String producttype = null;
                for (Map.Entry<String, List> entry : proMap.entrySet()) {
                    String productid = entry.getKey();
                    List list = entry.getValue();
                    if (list != null && !list.isEmpty()) {
                        Map advanceSearchTransactionlist = null;
                        if (productAdvSearchMap != null && productAdvSearchMap.containsKey(productid) && productAdvSearchMap.get(productid) != null) {
                            advanceSearchTransactionlist = productAdvSearchMap.get(productid);
                        }
                        PriceValuationStack stack = new PriceValuationStack();
                        pushTransactionForProduct(list, requestParams, startDate, product, productid, stack, endDate,advanceSearchTransactionlist);
                        Object[] row = (Object[]) list.get(0);

                        if (isMaterialInOutReport) {
                            producttype = row[40] != null ? (String) row[40] : "";
                        } else {
                            producttype = row[38] != null ? (String) row[38] : "";

                        }
                        blockqtyJson.put("id", productid);
                        valuation = getValuationValues(stack, valuation, product, producttype, isDifferenceInOpeningBalanceFlag, isAdvanceSearchForValuation,blockqtyJson);
                        stack = null;
                    }
                }
            }
            proMap = null;
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return valuation;
    }
    
    @Override
    public Map<String, Map> getInventoryValuationDataForFinancialReports(HashMap<String, Object> requestParams, Map<String, Map> stockDateMap) {
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            CompanyAccountPreferences pref = null;
            if (requestParams.containsKey(Constants.preferences) && requestParams.get(Constants.preferences) != null) {
                pref = (CompanyAccountPreferences) requestParams.get(Constants.preferences);
            } else {
                KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            }
            Map<String, List> proMap = null;
            Map<String, Map> productAdvSearchMap = null;
            boolean isAdvanceSearchForValuation = false;
            boolean deductSOBlockedQtyFromValuation = false;
            ExtraCompanyPreferences extrapref = null;
            if (requestParams.containsKey(Constants.extraCompanyPreferences) && requestParams.get(Constants.extraCompanyPreferences) != null) {
                extrapref = (ExtraCompanyPreferences) requestParams.get(Constants.extraCompanyPreferences);
            } else {
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), (String) requestParams.get(Constants.companyKey));
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            }
            if (!StringUtil.isNullOrEmpty(extrapref.getColumnPref())) {
                JSONObject columnPrefjObj = new JSONObject((String) extrapref.getColumnPref());
                if (columnPrefjObj.has("deductSOBlockedQtyFromValuation") && columnPrefjObj.get("deductSOBlockedQtyFromValuation") != null && columnPrefjObj.optBoolean("deductSOBlockedQtyFromValuation", false)) {
                    deductSOBlockedQtyFromValuation = true;
                }
            }
            if (requestParams.containsKey(Constants.Acc_Search_Json) && requestParams.get(Constants.Acc_Search_Json) != null && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
                /**
                 * Code for advance search SDP-9634.
                 */
                HashMap<String, Object> tempRequestParams = new HashMap<>(requestParams);
                String searchJson = (String) requestParams.get(Constants.Acc_Search_Json);
                tempRequestParams.remove(Constants.Acc_Search_Json);
                proMap = getProductTransaction(tempRequestParams);
                tempRequestParams.put(Constants.Acc_Search_Json, searchJson);
                tempRequestParams.put("isAdvanceSearchForValuation", true);
                tempRequestParams.put(Constants.Filter_Criteria, requestParams.get(Constants.Filter_Criteria));
                productAdvSearchMap = getProductTransactionForAdvanceSearch(tempRequestParams);
                isAdvanceSearchForValuation = true;
            } else {
                proMap = getProductTransaction(requestParams);
            }
            boolean isNegativeStock = false;
            if (pref.getNegativestock() == 1 && pref.getNegativeStockPR() == 1) { // Block Case for DO & PR
                isNegativeStock=true;
            }
            boolean isMaterialInOutReport = false;// Material IN/OUT Report
            if (requestParams.containsKey("isMaterialInOutReport") && requestParams.get("isMaterialInOutReport") != null) {
                isMaterialInOutReport = Boolean.parseBoolean(requestParams.get("isMaterialInOutReport").toString());
            }
            requestParams.put("basecurrencyid", pref.getCompany().getCurrency().getCurrencyID());
            List goodsReceiptOrderDetailSentToQAList = getGoodsReceiptOrderDetailSentToQA(companyid);
            Map<String, Object> extraParameters = new HashMap<String, Object>();
            
            extraParameters.put("goodsReceiptOrderDetailSentToQAList", goodsReceiptOrderDetailSentToQAList);
            extraParameters.put("inventoryValuationType", pref.getInventoryValuationType());
            KwlReturnObject kwl = accProductObj.getRateAndExchangeRateFromGoodsReceiptOrderDetail(null,companyid);
            Map rateExchangeRateFromGRODMap = new HashMap();
            if (kwl != null && kwl.getEntityList() != null && !kwl.getEntityList().isEmpty()) {
                List l = kwl.getEntityList();
                if (l != null && !l.isEmpty()) {
                    for (int i = 0; i < l.size(); i++) {
                        Object o = l.get(i);
                        Object[] objArr = (Object[]) o;
                        if (objArr != null && objArr.length > 0) {
                            rateExchangeRateFromGRODMap.put(objArr[4], objArr);
                        }
                    }
                }
            }
            extraParameters.put("rateExchangeRateFromGRODMap", rateExchangeRateFromGRODMap);
            if (!proMap.isEmpty()) {
                Product product = null;
                String producttype = null;
                for (String key : stockDateMap.keySet()) {
                    Map stockDateObject = stockDateMap.get(key);
                    stockDateObject.put("valuation", new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0});
                }
                JSONObject blockqtyJson = new JSONObject();
                if (deductSOBlockedQtyFromValuation) {
                    DateFormat df = null;
                    if (requestParams.containsKey("df") && requestParams.get("df") != null) {
                        df = (DateFormat) requestParams.get("df");
                        blockqtyJson.put("df", df);
                    }
                    blockqtyJson.put("deductSOBlockedQtyFromValuation", deductSOBlockedQtyFromValuation);
                    blockqtyJson.put("companyid", companyid);
                }
                for (Map.Entry<String, List> entry : proMap.entrySet()) {
                    for (String key : stockDateMap.keySet()) {
                        Map stockDateObject = stockDateMap.get(key);
                        PriceValuationStack stack = new PriceValuationStack();
                        stockDateObject.put("stack", stack);
                    }
                    String productid = entry.getKey();
                    List list = entry.getValue();
                    if (list != null && !list.isEmpty()) {
                        Map advanceSearchTransactionlist = null;
                        if (productAdvSearchMap != null && productAdvSearchMap.containsKey(productid) && productAdvSearchMap.get(productid) != null) {
                            advanceSearchTransactionlist = productAdvSearchMap.get(productid);
                        }
                        Object[] row = (Object[]) list.get(0);

                        if (isMaterialInOutReport) {
                            producttype = row[40] != null ? (String) row[40] : "";
                        } else {
                            producttype = row[38] != null ? (String) row[38] : "";

                        }
                        blockqtyJson.put("id", productid);
                        pushTransactionForProduct(list, requestParams, product, productid, stockDateMap, advanceSearchTransactionlist, extraParameters, isAdvanceSearchForValuation, isNegativeStock);
                        calculateValuationFromStockDateMap(stockDateMap, product, producttype, isAdvanceSearchForValuation,blockqtyJson);
                    }
                }
            } else {
                for (String key : stockDateMap.keySet()) {
                    Map stockDateObject = stockDateMap.get(key);
                    stockDateObject.put("valuation", new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0});
                }
            }
            proMap = null;
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stockDateMap;
    }
    /**
     * Method to get valuation map for the cost and selling price report
     *
     * @param requestParams
     * @author Swapnil K.
     * @return Map
     */
    @Override
    public Map<String, PriceValuationStack.Batch> getValuationMapForCostAndSellingPriceReport(JSONObject requestParams) {
        Map<String, PriceValuationStack.Batch> valuationMap = new HashMap<>();
        try {
            String companyid = requestParams.getString(Constants.companyKey);
            String gcurrencyid = requestParams.getString(Constants.globalCurrencyKey);
            requestParams.put("callForProduct", true);
            KwlReturnObject productResult = accInvoiceDAOobj.getDataForCostAndSellingPriceReport(requestParams);
            List productList = productResult.getEntityList();
            requestParams.remove("callForProduct");
            if (productList != null && !productList.isEmpty()) {
                PriceValuationStack stack = new PriceValuationStack();
                for (Object object : productList) {
                    String productid = (String) object;
                    HashMap reqParams = new HashMap();
                    reqParams.put("productId", productid);
                    reqParams.put(Constants.companyKey, companyid);
                    if (!StringUtil.isNullOrEmpty(gcurrencyid)) {
                        reqParams.put(Constants.globalCurrencyKey, gcurrencyid);
                    }
                    reqParams.put("isItemHistoryReport",true);  // to be used to put all the transaction in period
                    /**
                     * Flag 'includeProductDetailsInSelectQuery' is used in method 'accProductImpl.getStockLedger' to fetch product details along with transaction data
                     * It has been added to remove Product object's dependency from method 'getProductTransactionJson'
                     * When this flag is passed as true, product details required for inventory reports are also fetched from database along with transaction details
                     */
                    reqParams.put(Constants.includeProductDetailsInSelectQuery, true);
                    KwlReturnObject result = accProductObj.getStockLedger(reqParams);
                    List list = result.getEntityList();
                    if (list != null && !list.isEmpty() && !StringUtil.isNullOrEmpty(productid)) {
                        pushTransactionForProduct(list, reqParams, null, productid, stack, null, null);
                        Map<String, PriceValuationStack.Batch> detailsMap = stack.getAllTransactionBatch();
                        valuationMap.putAll(detailsMap);
                        stack.clear();
                    }
                }
            }
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        } finally {
            return valuationMap;
        }
    }

    
        /**
     * Function to get Consolidation Stock valuation data for all child company
     * with parent for Consolidation Stock Report
     *
     * @param paramJobj
     * @author Raveesh Pathak
     * @return JSONArray for all the transactions
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getConsolidationStockReport(JSONObject paramJobj) throws ServiceException, SessionExpiredException {
        JSONObject returnObj = new JSONObject();
        JSONArray array = new JSONArray();
        int totalPrdRecordCount = 0;
        try {
            boolean exportConsolidationStockReport = paramJobj.optBoolean("exportConsolidationStockReport", false);
            String companyid = paramJobj.getString(Constants.companyKey);
            Date startDate = new Date(0);
            Date endDate = authHandler.getDateOnlyFormat().parse(paramJobj.getString("enddate"));

            int start = paramJobj.optInt("start", 0);
            int limit = paramJobj.optInt("limit", 30);

            Map<String, JSONObject> conslidationCompanyMap = new HashMap();// This Map is used to hold consolidation companies data like companyid,current exchange rate, stake in percentage etc 
            StringBuilder childCompanyIDs = new StringBuilder();

            Map requestMap = new HashMap();
            requestMap.put(Constants.companyKey, companyid);
            KwlReturnObject result = currencyDAO.getSQLConsolidation(requestMap);
            if (result != null && !result.getEntityList().isEmpty()) {
                Date requestTime = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
                List consolidationDatas = result.getEntityList();
                for (int i = 0;i<consolidationDatas.size();i++) {
                    Object[] details = (Object[])consolidationDatas.get(i);
                    JSONObject object = new JSONObject();
                    if (details[1] != null) {
                        Map<String, Object> filterMap = new HashMap();
                        filterMap.put("consolidationid", details[3]);
                        filterMap.put("recentapplydate", requestTime);
                        KwlReturnObject exchangerate = currencyDAO.getConsolidationExchangeRate(filterMap);
//                        List exchangerate = kwlCommonTablesDAOObj.getRequestedObjectFieldsInCollection(ConsolidationExchangeRateDetails.class, new String[]{"exchangeRate"}, filterMap);
                        if (StringUtil.isNullObject(exchangerate)) {
                            continue;
                        } else {
                            if (exchangerate != null && !exchangerate.getEntityList().isEmpty()) {
                                object.put("exchangerate", (Double) ((ConsolidationExchangeRateDetails) exchangerate.getEntityList().get(0)).getExchangeRate());
                            }
                        }
                        object.put("id", details[3]);
                        filterMap.clear();
                        filterMap.put("companyID", details[3]);
                        Object company = kwlCommonTablesDAOObj.getRequestedObjectFields(Company.class, new String[]{"subDomain"}, filterMap);
                        
                        object.put("stakeinpercentage", details[0]);
                        object.put("subdomainid", details[1]);
                        object.put("subdomainname", (String)company);
                        childCompanyIDs.append(details[1]).append(",");
                        conslidationCompanyMap.put(details[1].toString(), object);
                    }
                }
            }

            if (!StringUtil.isNullOrEmpty(childCompanyIDs.toString())) {
                String childCompanyids = childCompanyIDs.toString().substring(0, childCompanyIDs.toString().length() - 1);
                HashMap<String, Object> prdParams = new HashMap<>();
                prdParams.put("isConsolidationStockReport", true);
                prdParams.put("companyids", childCompanyids);
                prdParams.put("dir", "asc");
                prdParams.put("sort", "pid");
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("ss", ""))) {
                    prdParams.put("ss", paramJobj.optString("ss"));
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("categoryname", "")) && !paramJobj.optString("categoryname", "").equalsIgnoreCase("All")) {
                    Map<String, Object> categoryParams = new HashMap();
                    categoryParams.put("categoryname", paramJobj.optString("categoryname"));
                    categoryParams.put("companyids", childCompanyids);
                    KwlReturnObject categoryResult = accProductObj.getSameNameProductCategoryIds(categoryParams);
                    List<String> categoryList = categoryResult.getEntityList();
                    StringBuilder sb = new StringBuilder();
                    for (String categoryid : categoryList) {
                        sb.append(categoryid).append(",");
                    }
                    if (!StringUtil.isNullOrEmpty(sb.toString())) {
                        String categoryids = sb.toString().substring(0, sb.toString().length() - 1);
                        prdParams.put("categoryids", categoryids);
                    }
                }
                
                if(!prdParams.containsKey("producttypeids")) {
                    prdParams.put("producttypeids", Producttype.INVENTORY_PART+","+Producttype.ASSEMBLY+","+Producttype.Inventory_Non_Sales);
                }

                if (!exportConsolidationStockReport) {
                    // converting it into string because it was already used as string in AccProductModuleServiceImpl.getProductsByCategory()
                    prdParams.put(Constants.start, start + "");
                    prdParams.put(Constants.limit, limit + "");
                } else {
                    prdParams.put("exportConsolidationStockReport", exportConsolidationStockReport);
                    limit = 1000;
                    prdParams.put(Constants.start, start + "");
                    prdParams.put(Constants.limit, limit + "");
                }
                
                KwlReturnObject prdResult = null;
                boolean executeCountQuery = false;
                Calendar cc = null, cc1 = null;
                do{
                cc = Calendar.getInstance();
                prdParams.put("executeCountQuery", executeCountQuery);
                prdParams.put("totalPrdRecordCount", totalPrdRecordCount);
                prdResult = accProductObj.getNewProductList(prdParams);
                cc1 = Calendar.getInstance();
                Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.WARNING, cc.getTime()+ " getNewProductList  time : " + (cc1.getTimeInMillis() - cc.getTimeInMillis()) / 1000);
                List<String> prdlist = prdResult.getEntityList();
                totalPrdRecordCount = prdResult.getRecordTotalCount();
                Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.WARNING, Constants.start + " : "+start+"    totalPrdRecordCount : "+totalPrdRecordCount);
        
	        HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
                requestParams.put("isConsolidationStockReport", true);

                StringTokenizer st = null;
                Set<String> pidset = null;
                Map<String, Set<String>> productListbyCompany = new HashMap<String, Set<String>>();
                List prodList = null;
                
                if (!StringUtil.isNullObject(prdlist)) {
                    String company = "", product = "";
                    for (int i = 0; i < prdlist.size(); i++) {
                        st = new StringTokenizer(prdlist.get(i), ",");
                        while (st.hasMoreElements()) {
                            company = (String) st.nextElement();
                            product = company.substring(company.indexOf("=")+1);
                            company = company.substring(0,company.indexOf("="));
                            if(productListbyCompany.containsKey(company)){
                                pidset = productListbyCompany.get(company);
                            }else {
                                pidset = new HashSet<String>();
                            }
                            pidset.add(product);
                            productListbyCompany.put(company, pidset);
                        }
                }
                    Map<String,JSONObject> productListbyCode = new HashMap<String,JSONObject> ();
                    // loop for each company
                    for (Map.Entry<String, Set<String>> companyProduct : productListbyCompany.entrySet()) {
                        prodList = accProductObj.getProductListByIdsSQL(companyProduct.getValue());
                        processReport(getJsonProductArray(prodList), conslidationCompanyMap, requestParams, exportConsolidationStockReport, startDate, endDate, companyid, companyProduct.getValue(), productListbyCode, companyProduct.getKey());
                    }
                    
                    JSONObject temp = null;
                    for (Map.Entry<String,JSONObject> finalJson : productListbyCode.entrySet()) {
                            temp = finalJson.getValue();
                        if(exportConsolidationStockReport){
                            temp.put("totalquantityonhand", temp.getDouble("totalquantityonhand") + " "+temp.getString("uomname"));
                        }
                        array.put(temp);
                    }
                    
            }
               start = start+limit; 
               executeCountQuery = true;
            }while (exportConsolidationStockReport && start <= totalPrdRecordCount);
                
            }
            returnObj.put(Constants.RES_data, array);
            returnObj.put(Constants.RES_count, totalPrdRecordCount);
        } catch (JSONException | SessionExpiredException | ParseException | ServiceException ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("getTradingAndProfitLoss : " + ex.getMessage(), ex);
        }
        return returnObj;
    }
    
    private JSONArray getJsonProductArray(List prodList) throws JSONException{
        JSONArray productArray = new JSONArray();
        if (prodList == null)
            return productArray;
        JSONObject jobj = null;
        for(Object obj : prodList){
            Object[] row = (Object[]) obj;
            jobj = new JSONObject();
            jobj.put("id", row[0]);
            jobj.put("productid", row[1]);
            jobj.put("name", row[2]);
            jobj.put("description", row[3]);
            jobj.put("prodcurrency", row[4]);
            jobj.put("valuationmethod", row[5]);
            jobj.put("isSerialForProduct", row[6]);
            jobj.put("isBatchForProduct", row[7]);
            jobj.put("isrowforproduct", row[8]);
            jobj.put("israckforproduct", row[9]);
            jobj.put("isbinforproduct", row[10]);
            jobj.put("companyid", row[11]);
            jobj.put("subdomain", row[12]);
            jobj.put("compcurrency", row[13]);
            jobj.put("producttypeid", row[14]);
            jobj.put("producttypename", row[15]);
            jobj.put("uom", row[16]);
            jobj.put("uomid", row[17]);
            productArray.put(jobj);
        }
        return productArray;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void processReport(JSONArray prodList, Map<String, JSONObject> conslidationCompanyMap, HashMap<String, Object> requestParams, boolean exportConsolidationStockReport,
            Date startDate, Date endDate, String companyid, Set<String> productSet, Map<String, JSONObject> productListbyCode, String currentCompanyid) throws ServiceException, JSONException {
        JSONObject prdObjByCode = null;
        double totalProductQuantityOnHand = 0;
        double totalProductValuation = 0;
        String uomname = "";
        requestParams.put(Constants.companyKey, currentCompanyid);
        requestParams.put("selproductIds", StringUtil.join(",", productSet));
        Calendar cc = Calendar.getInstance();
        Map<String, List> proMap = getProductTransaction(requestParams);
        Calendar cc1 = Calendar.getInstance();
        JSONObject product = null;
        if (prodList != null) {  // handling NPE
            for (int i = 0; i < prodList.length(); i++) {
                JSONArray dataArr = new JSONArray();
                product = prodList.getJSONObject(i);
                if (productListbyCode.containsKey(product.optString("productid"))) {
                    prdObjByCode = productListbyCode.get(product.optString("productid"));
                    totalProductQuantityOnHand = prdObjByCode.optDouble("totalquantityonhand");
                    totalProductValuation = prdObjByCode.optDouble("totalvaluation");
                } else {
                    prdObjByCode = new JSONObject();
                    totalProductQuantityOnHand = 0;
                    totalProductValuation = 0;
                }
                String subdomain = product.getString("subdomain");
                uomname = product.optString("uom");

                JSONObject consolidationCompanyObject = conslidationCompanyMap.get(product.getString("companyid"));
                double stakeInPercentage = consolidationCompanyObject.getDouble("stakeinpercentage");
                double exchangeRate = consolidationCompanyObject.getDouble("exchangerate");

                prdObjByCode.put("productname", product.getString("name"));
                prdObjByCode.put("productcode", product.getString("productid"));
                prdObjByCode.put("producttypename", product.getString("producttypename"));
                prdObjByCode.put("uomname", uomname);

                if (!proMap.isEmpty()) {
                    List list = proMap.get(product.optString("id"));
                    if (list != null && !list.isEmpty()) {
                        PriceValuationStack stack = new PriceValuationStack();
                        pushTransactionForProduct(list, requestParams, startDate, product, null, stack, endDate, null);
                        dataArr = getTransactionJSON(product, stack, dataArr, requestParams);
                        if (dataArr.length() > 0) {
                            JSONObject obj = dataArr.getJSONObject(0);
                            double quantityonhand = obj.optDouble("quantity", 0);
                            double valuation = obj.optDouble("evaluationcost", 0);

                            if (valuation != 0) {
                                valuation = valuation * (stakeInPercentage / 100) * exchangeRate;
                            }
                            if (quantityonhand != 0 || valuation != 0) {
                                if (exportConsolidationStockReport) {
                                    prdObjByCode.put(subdomain + "_quantityonhand", authHandler.formattedQuantity(quantityonhand, companyid) + " " + uomname);
                                } else {
                                    prdObjByCode.put(subdomain + "_quantityonhand", quantityonhand);
                                }
                                prdObjByCode.put(subdomain + "_valuation", valuation);
                            }
                            totalProductQuantityOnHand += quantityonhand;
                            totalProductValuation += valuation;
                        }
                        stack = null;
                    }
                }
                prdObjByCode.put("totalquantityonhand", authHandler.formattedQuantity(totalProductQuantityOnHand, companyid));
                prdObjByCode.put("totalvaluation", totalProductValuation);

                productListbyCode.put(product.getString("productid"), prdObjByCode);
            }
        }
    }
    
    /**
     * Function to get inventory valuation data for all stock related reports
     * @param requestParams
     * @author Swapnil K.
     * @return JSONArray for all the transactions
     */
    @Override
    public JSONArray getInventoryValuationData(HashMap<String, Object> requestParams) {
        JSONArray dataArr = new JSONArray();
        Map<String, List> proMap = new HashMap<>();
        int reportId = 0; //Check for reportId.
        boolean isFromSalesReport = false;
        if (requestParams.containsKey("isFromSalesReport") && requestParams.get("isFromSalesReport") != null ) {
            isFromSalesReport = Boolean.parseBoolean(requestParams.get("isFromSalesReport").toString());
        }
        if (requestParams.containsKey(Constants.REPORT_ID) && requestParams.get(Constants.REPORT_ID) != null ) {
            reportId = Integer.parseInt(requestParams.get(Constants.REPORT_ID).toString());
        }
        boolean isExportReport = false;
        if (requestParams.containsKey(Constants.IS_EXPORT_REPORT) && requestParams.get(Constants.IS_EXPORT_REPORT) != null  && !StringUtil.isNullOrEmpty((String)requestParams.get(Constants.IS_EXPORT_REPORT))  ) {
            isExportReport = Boolean.parseBoolean(requestParams.get(Constants.IS_EXPORT_REPORT).toString());
        }

        try {
            String startDateString = (String) requestParams.get(Constants.REQ_startdate);
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            Date startDate = null;
            if (!StringUtil.isNullOrEmpty(startDateString) && df != null) {
                startDate = df.parse(startDateString);
            }
            requestParams.remove(Constants.REQ_startdate);
            String endDateString = (String) requestParams.get(Constants.REQ_enddate);
            Date endDate = null;
            if (!StringUtil.isNullOrEmpty(endDateString) && df != null) {
                endDate = df.parse(endDateString);
            }
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), (String) requestParams.get(Constants.companyKey));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            
            boolean deductSOBlockedQtyFromValuation = false;
            JSONObject columnPrefjObj = new JSONObject();
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), (String) requestParams.get(Constants.companyKey));
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                columnPrefjObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                if (columnPrefjObj.has("deductSOBlockedQtyFromValuation") && columnPrefjObj.get("deductSOBlockedQtyFromValuation") != null && columnPrefjObj.optBoolean("deductSOBlockedQtyFromValuation", false)) {
                    deductSOBlockedQtyFromValuation = true;
                    requestParams.put("deductSOBlockedQtyFromValuation",deductSOBlockedQtyFromValuation);
                    requestParams.put("startdateforsoblockqty", startDateString);
                    requestParams.put("enddateforsoblockqty", endDateString);
                }
            }
            
            HashMap<String, Object> prefparams = new HashMap<>();
            prefparams.put("id", (String) requestParams.get(Constants.companyid));
            Object extrapref = kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class,new String[]{"activelandingcostofitem"}, prefparams);

            if (extrapref!=null && (Boolean) extrapref==true) {
                requestParams.put("isactivatelandedinvamt", true);
            }
            boolean stockAgeing = requestParams.get("stockAgeing")!=null?Boolean.parseBoolean(requestParams.get("stockAgeing").toString()):false;
            if ((pref.getNegativestock() != 1 || pref.getNegativeStockPR() != 1) && !stockAgeing) { // Block Case for DO & PR
                requestParams.remove(Constants.REQ_enddate);
            }
            requestParams.put("basecurrencyid", pref.getCompany().getCurrency().getCurrencyID());
            
            requestParams.put("isFromSalesReport", isFromSalesReport);
            
            /**
             * Flag 'includeProductDetailsInSelectQuery' is used in method 'accProductImpl.getStockLedger' to fetch product details along with transaction data
             * It has been added to remove Product object's dependency from method 'getProductTransactionJson'
             * When this flag is passed as true, product details required for inventory reports  are also fetched from database along with transaction details
             */
            requestParams.put(Constants.includeProductDetailsInSelectQuery, true);

            Map<String, Map> productAdvSearchMap = new HashMap<>();
            if (requestParams.containsKey(Constants.Acc_Search_Json) && requestParams.get(Constants.Acc_Search_Json) != null && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
                /**
                 * Code for advance search SDP-9634.
                 */
                HashMap<String, Object> tempRequestParams = new HashMap<>(requestParams);
                String searchJson = (String) requestParams.get(Constants.Acc_Search_Json);
                tempRequestParams.remove(Constants.Acc_Search_Json);
                proMap = getProductTransaction(tempRequestParams);
                tempRequestParams.put(Constants.Acc_Search_Json, searchJson);
                tempRequestParams.put("isAdvanceSearchForValuation", true);
                productAdvSearchMap = getProductTransactionForAdvanceSearch(tempRequestParams);
                dataArr = getProductTransactionJson(proMap, requestParams, startDate, endDate, reportId, endDateString, startDateString, productAdvSearchMap);
            } else if (reportId == Constants.STOCK_STATUS_REPORT_ID) {
                int st = 0;
                int  end = 0;
                if (requestParams.containsKey(Constants.start) && requestParams.get(Constants.start) != null && requestParams.containsKey(Constants.limit) && requestParams.get(Constants.limit) != null) {
                    String start = (String) requestParams.get(Constants.start);
                    String limit = (String) requestParams.get(Constants.limit);

                    if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                        st = Integer.parseInt(start);
                        end = Integer.parseInt(limit);
                        requestParams.remove(Constants.start);
                        requestParams.remove(Constants.limit);
                    }
                }
                String requestUUID = "";
                if (requestParams.containsKey(Constants.REQUEST_ID) && requestParams.get(Constants.REQUEST_ID) != null && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.REQUEST_ID))) {
                    requestUUID = (String) requestParams.get(Constants.REQUEST_ID);
                }
                          
                String companyId =  requestParams.get(Constants.companyid).toString();
                if (!StringUtil.isNullOrEmpty(requestUUID)) {
                    String searchString = "";    
                    if (requestParams.containsKey("ss") && requestParams.get("ss") != null ) {
                        searchString =requestParams.get("ss").toString();
                    }                    
                    int totalProductIdsCount = accProductObj.getTempProductIdsForReport(endDate, st, end, companyId, requestUUID, searchString,isExportReport);
                    //This Flag is used to decide in Stock ledger to Join products with temporary table 'tempproductcompanyid'
                    requestParams.put(Constants.USE_OF_TEMP_TABLE, "true");
                    proMap = getProductTransaction(requestParams);
                    dataArr = getProductTransactionJson(proMap, requestParams, startDate, endDate, reportId, endDateString, startDateString, null);
//                    if (!isExportReport) {
                        //Add Total Length to the Last index;
//                        dataArr.put(dataArr.length(), dataArr.length());
//                    }
                }
            } else {
                proMap = getProductTransaction(requestParams);
                dataArr = getProductTransactionJson(proMap, requestParams, startDate, endDate, reportId, endDateString, startDateString,  null);
            }

        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException | ServiceException | NumberFormatException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
        return dataArr;
    }

    /**
     * This method iterates over product-wise entries in the map 'proMap'
     * Transactions for each product are pushed into a stack (PriceValuationStack object)
     * After pushing all transactions into the stack, a JSONArray is prepared from stack entries
     * JSONArray contains transaction-wise stock valuation details
     * @param proMap
     * @param requestParams
     * @param startDate
     * @param endDate
     * @param reportId
     * @param endDateString
     * @param startDateString
     * @param productAdvSearchMap
     * @return
     * @throws ServiceException 
     */
    private JSONArray getProductTransactionJson(Map<String, List> proMap, HashMap<String, Object> requestParams, Date startDate, Date endDate, int reportId, String endDateString, String startDateString, Map<String, Map> productAdvSearchMap) throws ServiceException, JSONException {
        JSONArray dataArr = new JSONArray();
        boolean isFromSalesReport = false;
        if (requestParams.containsKey("isFromSalesReport") && requestParams.get("isFromSalesReport") != null ) {
            isFromSalesReport = Boolean.parseBoolean(requestParams.get("isFromSalesReport").toString());
        }
        if (!proMap.isEmpty()) {
            for (Map.Entry<String, List> entry : proMap.entrySet()) {
                String productid = entry.getKey();
                List list = entry.getValue();
                if (list != null && !list.isEmpty() && !StringUtil.isNullOrEmpty(productid)) {
                    JSONObject productJson = getProductDetailsFromTransactionRow((Object[]) list.get(0), requestParams);//Get Product details from list, in a JSONObject
                    PriceValuationStack stack = new PriceValuationStack();
                    Map advanceSearchTransactionlist = null;
                    if (productAdvSearchMap != null && productAdvSearchMap.containsKey(productid)) {
                        advanceSearchTransactionlist = productAdvSearchMap.get(productid);
                    }
                    pushTransactionForProduct(list, requestParams, startDate, productid, stack, endDate, advanceSearchTransactionlist);
                    if (reportId == Constants.STOCK_STATUS_REPORT_ID) {
                        //Start date and end date required to calculate so and po Quantity.
                        requestParams.put(Constants.REQ_startdate, startDateString);
                        requestParams.put(Constants.REQ_enddate, endDateString);
                        dataArr = getTransactionJson(productJson, stack, dataArr, requestParams);
                    } else {
                        if (isFromSalesReport) {
                            dataArr = getTransactionJsonForSalesReport(productJson, stack, dataArr, requestParams);
                        } else {
                        dataArr = getTransactionJson(productJson, stack, dataArr, requestParams);
                    }
                    }
                    stack = null;
                }
            }
        }
        return dataArr;
    }

    /**
     * This method fetches transactions for products which are required for stock valuation
     * After fetching transactions from database, transactions for each product are put in a list and
     * the list is put in the map 'proMap' against product's UUID
     * @param requestParams
     * @return 
     */
    private Map<String, List> getProductTransaction(HashMap<String, Object> requestParams) {
        Map<String, List> proMap = new LinkedHashMap();
        try {
//            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.INFO, "getStockLedger-start" + new Date());
            KwlReturnObject result = accProductObj.getStockLedger(requestParams);
//            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.INFO, "getStockLedger-End" + new Date());
            List list = result.getEntityList();
//            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.INFO, "build Map-Start" + new Date() + "list.size() = " + list.size());
            for (Object object : list) {
                Object[] row = (Object[]) object;
                String productId = (String) row[1];
                if (proMap.containsKey(productId)) {
                    proMap.get(productId).add(object);
                } else {
                    List transactionList = new ArrayList();
                    transactionList.add(object);
                    proMap.put(productId, transactionList);
                }
            }
//            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.INFO, "build Map-End", new Date());
        } catch (Exception ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return proMap;
    }
    
    /**
     * Method to retrieve product details from object array fetched by method accProductImpl.getStockLedger, 
     * and to create JSONObject with product details
     * @param row
     * @param requestParams
     * @return
     * @throws JSONException 
     */
    private JSONObject getProductDetailsFromTransactionRow(Object[] row, HashMap<String, Object> requestParams) throws JSONException {
        JSONObject returnJobj = new JSONObject();
        boolean isMaterialInOutReport = false;// Material IN/OUT Report
        if (requestParams.containsKey("isMaterialInOutReport") && requestParams.get("isMaterialInOutReport") != null) {
            isMaterialInOutReport = Boolean.parseBoolean(requestParams.get("isMaterialInOutReport").toString());
        }
        if (isMaterialInOutReport) {
            returnJobj.put("id", row[1] != null ? (String) row[1] : "");
            returnJobj.put("productid", row[2] != null ? (String) row[2] : "");
            returnJobj.put("isSerialForProduct", row[41] != null && (char) row[41] == 'T');
            returnJobj.put("producttypeid", row[42] != null ? (String) row[42] : "");
            returnJobj.put("valuationmethod", row[43] != null ? (int) row[43] : 0);
            returnJobj.put("name", row[44] != null ? (String) row[44] : "");

            returnJobj.put("productweight", row[45] != null ? (double) row[45] : 0d);
            returnJobj.put("description", row[46] != null ? (String) row[46] : "");
            returnJobj.put("isBatchForProduct", row[47] != null && (char) row[47] == 'T');
            returnJobj.put("isrowforproduct", row[48] != null && (char) row[48] == 'T');
            returnJobj.put("israckforproduct", row[49] != null && (char) row[49] == 'T');
            returnJobj.put("isbinforproduct", row[50] != null && (char) row[50] == 'T');
            returnJobj.put("uomid", row[51] != null ? (String) row[51] : "");
            returnJobj.put("uom", row[52] != null ? (String) row[52] : "");
            returnJobj.put("producttypename", row[53] != null ? (String) row[53] : "");
            returnJobj.put("prodcurrency", row[54] != null ? (String) row[54] : "");
        } else {
            returnJobj.put("id", row[1] != null ? (String) row[1] : "");
            returnJobj.put("productid", row[2] != null ? (String) row[2] : "");
            returnJobj.put("isSerialForProduct", row[37] != null && (char) row[37] == 'T');
            returnJobj.put("producttypeid", row[38] != null ? (String) row[38] : "");
            returnJobj.put("valuationmethod", row[39] != null ? (int) row[39] : 0);
            returnJobj.put("name", row[40] != null ? (String) row[40] : "");

            returnJobj.put("productweight", row[41] != null ? (double) row[41] : 0d);
            returnJobj.put("description", row[42] != null ? (String) row[42] : "");
            returnJobj.put("isBatchForProduct", row[43] != null && (char) row[43] == 'T');
            returnJobj.put("isrowforproduct", row[44] != null && (char) row[44] == 'T');
            returnJobj.put("israckforproduct", row[45] != null && (char) row[45] == 'T');
            returnJobj.put("isbinforproduct", row[46] != null && (char) row[46] == 'T');
            returnJobj.put("uomid", row[47] != null ? (String) row[47] : "");
            returnJobj.put("uom", row[48] != null ? (String) row[48] : "");
            returnJobj.put("producttypename", row[49] != null ? (String) row[49] : "");
            returnJobj.put("prodcurrency", row[50] != null ? (String) row[50] : "");
        }
        return returnJobj;
    }
    
    /**
     * Method to get the product transaction based on the advance search
     * criteria.
     *
     * @param requestParams Request Params
     * @return Map<String,List> Collection of product Id and the list of
     * transaction detail ID for which advance search criteria is matching.
     */
    private Map<String, Map> getProductTransactionForAdvanceSearch(HashMap<String, Object> requestParams) {
        Map<String, Map> proMap = new LinkedHashMap();
        try {
            KwlReturnObject result = accProductObj.getStockLedger(requestParams);
            List list = result.getEntityList();
            for (Object object : list) {
                Object[] row = (Object[]) object;
                String productId = (String) row[0];
                String detailID = (String) row[1];
                /*
                 Code to check if case of Serial JSON or Product JSON
                 */
                String isSerialJson = row[8] != null ? (String) row[8] : "";          // Check if Serial JSON or Product JSON
                if (StringUtil.isNullOrEmptyWithTrim(isSerialJson)) {
                    isSerialJson = "F";
                }
                if (proMap.containsKey(productId)) {
                    proMap.get(productId).put(detailID, isSerialJson);
                } else {
                    Map transactionList = new HashMap<>();
                    transactionList.put(detailID, isSerialJson);
                    proMap.put(productId, transactionList);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return proMap;
    }
    /**
     * Function to get valuation array for the financial report
     * valuation[0] - Opening Stock (Opening)
     * valuation[1] - Opening Stock (Period)
     * valuation[2] - Opening Stock (Closing)
     * valuation[3] - Closing Stock (Opening)
     * valuation[4] - Closing Stock (Period)
     * valuation[5] - Closing Stock (Closing)
     * @param stack
     * @param valuation[]
     * @param isAdvanceSearchForValuation <code>true</code> if advance search is applied.
     * @param isDifferenceInOpeningBalanceFlag <code>true</code> if call is for
     * <b>Difference In Opening Balance</b>
     * @author Swapnil K.
     * @return inventory valuation result into an array of double
     */
    private double[] getValuationValues(PriceValuationStack stack, double[] valuation, Product product, String producttype, boolean isDifferenceInOpeningBalanceFlag, boolean isAdvanceSearchForValuation, JSONObject productJson) {
        try {
            double ledgerFinalValuation = 0, ledgerOnHandQuantity = 0;
            if (product != null) { // product will be passed null in case of balance sheet
                producttype = product.getProducttype().getID();
            }
            if (isDifferenceInOpeningBalanceFlag) {
//                if (!product.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                PriceValuationStack.Batch initialBatch = null;
                if (isAdvanceSearchForValuation) {
                    /**
                     * Get Initial transaction batch(Initial Stock- call for
                     * Difference In Opening Balance) for the transaction(s)
                     * which are fulfilling the advance search criteria.
                     */
                    initialBatch = stack.getInitialTransBatch(isAdvanceSearchForValuation);
                } else {
                    initialBatch = stack.getInitialTransactionBatch();
                }
                valuation[0] += initialBatch.getAmount(); // Opening Stock-> Opening 
                valuation[1] = 0;
                valuation[2] = valuation[0];
//                }
                PriceValuationStack.Batch openingBatch = null;
                if (isAdvanceSearchForValuation) {
                    /**
                     * Get Opening transaction batch for the transaction(s)
                     * which are fulfilling the advance search criteria.
                     */
                    openingBatch = stack.getOpeningTransBatch(isAdvanceSearchForValuation);
                } else {
                    openingBatch = stack.getOpeningTransactionBatch();
                }
                valuation[3] += openingBatch.getAmount();
                PriceValuationStack.Batch periodBatch = null;
                if (isAdvanceSearchForValuation) {
                    /**
                     * Get Period transaction batch for the transaction(s) which
                     * are fulfilling the advance search criteria.
                     */
                    periodBatch = stack.getPeriodTransBatch(isAdvanceSearchForValuation);
                } else {
                    periodBatch = stack.getPeriodTransactionBatch();
                }
                valuation[4] += periodBatch.getAmount(); // Closing Stock-> Period
                valuation[5] = valuation[3] + valuation[4];// Closing Stock-> Ending
                if (producttype.equals(Producttype.ASSEMBLY)) {
                    valuation[6] += openingBatch.getAmount();     // Assembly Valuation -> Opening
                    valuation[7] += periodBatch.getAmount();     // Assembly Valuation -> Period
                    valuation[8] = valuation[6] + valuation[7];     // Assembly Valuation -> Closing
                }
            } else {
                PriceValuationStack.Batch openingBatch = null;
                if (isAdvanceSearchForValuation) {
                    /**
                     * Get Opening transaction batch for the transaction(s)
                     * which are fulfilling the advance search criteria.
                     */
                    openingBatch = stack.getOpeningTransBatch(isAdvanceSearchForValuation);
                } else {
                    openingBatch = stack.getOpeningTransactionBatch();
                }
                valuation[0] += openingBatch.getAmount(); // Opening Stock-> Opening 
                valuation[1] = 0; // Opening Stock-> Period
                valuation[2] = valuation[0]; // Opening Stock-> Closing
                PriceValuationStack.Batch periodBatch = null;
                if (isAdvanceSearchForValuation) {
                    /**
                     * Get Period transaction batch for the transaction(s) which
                     * are fulfilling the advance search criteria.
                     */
                    periodBatch = stack.getPeriodTransBatch(isAdvanceSearchForValuation);
                } else {
                    periodBatch = stack.getPeriodTransactionBatch();
                }
                valuation[3] = valuation[0]; // Closing Stock-> Opening 
                valuation[4] += periodBatch.getAmount(); // Closing Stock-> Period
                /**
                 * Below Block is used to update the opening amount array of Valuation i.e valuation[0],valuation[2], valuation[3]
                 * And Periodic amount raay of Valuation i.e valuation[4].
                 */
                if (productJson.length() > 0 && productJson.optBoolean("deductSOBlockedQtyFromValuation", false)) {
                    ledgerFinalValuation += openingBatch.getAmount() + periodBatch.getAmount();
                    ledgerOnHandQuantity += openingBatch.getQuantity() + periodBatch.getQuantity();
                    Date startDate = null, soOrderDate = null;
                    startDate = (Date) productJson.opt("startdate");
                    productJson.put("isFinancialReport",true);
                    JSONObject blockqtyJson = getSOblockqtyJson(productJson, ledgerFinalValuation, ledgerOnHandQuantity);
                    if (blockqtyJson.length() > 0) {
                        double soledgerValuation = (blockqtyJson.optDouble("soledgerValuation", 0) * blockqtyJson.optDouble("blockqty", 0));
                        soOrderDate = (Date) blockqtyJson.opt("orderdate");
//                    
                        if (startDate != null && soOrderDate != null && (stockService.isOpeingOrPeriodTransaction(soOrderDate, startDate, 3))) {
                            valuation[0] = valuation[0] != 0 ? valuation[0] - soledgerValuation : valuation[0];
                            valuation[2] = valuation[0]; // Opening Stock-> Closing
                            valuation[3] = valuation[0]; // Closing Stock-> Opening 
                        } else {
                            valuation[4] = valuation[4] != 0 ? valuation[4] - soledgerValuation : valuation[4];// Periodic amount
                        }
                    }
                }
                valuation[5] = valuation[3] + valuation[4];// Closing Stock-> Ending
                if (producttype.equals(Producttype.ASSEMBLY)) {
                    valuation[6] += openingBatch.getAmount();     // Assembly Valuation -> Opening
                    valuation[7] += periodBatch.getAmount();     // Assembly Valuation -> Period
                    valuation[8] = valuation[6] + valuation[7];     // Assembly Valuation -> Closing
                }

            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return valuation;
    }
    
    private List getGoodsReceiptOrderDetailSentToQA(String companyid) throws ServiceException{
        List goodsReceiptOrderDetailSentToQA = new ArrayList();
        List list = accProductObj.getGoodsReceiptOrderDetailSentToQA(companyid);
        for (int i = 0; i < list.size(); i++) {
            String detail = (String)list.get(i);
            if (!StringUtil.isNullOrEmpty(detail)) {
                goodsReceiptOrderDetailSentToQA.add(detail);
            }
        }
        return goodsReceiptOrderDetailSentToQA;
    }
    /**
     * Function to push All transactions using productID into the stack
     * @param list
     * @param requestParams
     * @param jsonArr
     * @param endDate
     * @param advanceSearchTransactionlist (Optional)it will contain list of
     * transaction detail ID for which advance search criteria is matching.
     * @return 
     */
    private void pushTransactionForProduct(List list, HashMap<String, Object> requestParams, Date startDate, Product product, String productid, PriceValuationStack stack, Date endDate, Map advanceSearchTransactionlist, Map<String, Object> extraParameters) {
        try {       
            String basecurrency = requestParams.containsKey("basecurrencyid") ? (String)requestParams.get("basecurrencyid") : null;
            boolean isFromStockValuationDetail=false;
            boolean isFromStockValuationSummary=false;
            boolean isStockLedgerDetailedReport = false;// Stock Ledger Detailed Report
            if (requestParams.containsKey("isStockLedgerDetailedReport") && requestParams.get("isStockLedgerDetailedReport") != null) {
                isStockLedgerDetailedReport = Boolean.parseBoolean(requestParams.get("isStockLedgerDetailedReport").toString());
            }
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            boolean isMaterialInOutReport = false;// Material IN/OUT Report
            if (requestParams.containsKey("isMaterialInOutReport") && requestParams.get("isMaterialInOutReport") != null) {
                isMaterialInOutReport = Boolean.parseBoolean(requestParams.get("isMaterialInOutReport").toString());
            }
            String companyid=(String) requestParams.get("companyid");
          
//            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences pref = (CompanyAccountPreferences) extraprefresult.getEntityList().get(0);

            boolean isActivateLandedInvAmt = false;
            if (requestParams.containsKey("isactivatelandedinvamt") && requestParams.get("isactivatelandedinvamt") != null) {
                isActivateLandedInvAmt = Boolean.parseBoolean(requestParams.get("isactivatelandedinvamt").toString());
            }
            if (requestParams.containsKey("isFromStockValuationDetail") && requestParams.get("isFromStockValuationDetail") != null) {
                isFromStockValuationDetail = Boolean.parseBoolean(requestParams.get("isFromStockValuationDetail").toString());
            }
            if (requestParams.containsKey("isFromStockValuationSummary") && requestParams.get("isFromStockValuationSummary") != null) {
                isFromStockValuationSummary = Boolean.parseBoolean(requestParams.get("isFromStockValuationSummary").toString());
            }
            String serialNameSearch = "";
            /**
             * If Quick Search is on serial names. Added only for Material
             * IN/OUT Report.
             */
            if ((isMaterialInOutReport || isStockLedgerDetailedReport) && requestParams.containsKey("serialNameSearch") && requestParams.get("serialNameSearch") != null) {
                serialNameSearch = requestParams.get("serialNameSearch").toString();
            }
            String Searchjson = "";
            HashMap<String, Object> reqPar1 = new HashMap<String, Object>();
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
                if (requestParams.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }

            if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                Searchjson = requestParams.get("searchJson").toString();
            }
            if (!StringUtil.isNullOrEmpty(Searchjson)) {
                reqPar1.put(Constants.companyKey, companyid);
                reqPar1.put(Constants.Acc_Search_Json, Searchjson);
                reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                reqPar1.put(Constants.appendCase, "and");
                reqPar1.put(Constants.moduleid, Constants.SerialWindow_ModuleId);
//                          reqPar1.put("removeProductCustomFilter", true);

                Searchjson = accReportsService.getSearchJsonByModule(reqPar1);
            }
            if (list != null && !list.isEmpty()) {
                for (Object object : list) {
                    Object[] row = (Object[]) object;
                    double quantity = (Double) row[9];
                    BigInteger transType = (BigInteger) row[0];
                    ValuationMethod valuationMethod = null;
                    boolean isSerialForProduct = false;
                    int valuationmethod = 0;
                    
                    if (product != null) {
                        valuationMethod = product.getValuationMethod();
                        isSerialForProduct = product.isIsSerialForProduct();
                        productid = product.getID();
                    } else {
                        if (isMaterialInOutReport) {
                            valuationmethod = row[43] != null ? (Integer) row[43] : 0;
                            isSerialForProduct = (row[41] != null &&  row[41].toString().equals("T")) ? true : false;
                        } else {
                            valuationmethod = row[39] != null ? (Integer) row[39] : 0;
                            isSerialForProduct = (row[37] != null &&  row[37].toString().equals("T"))  ? true : false;

                        }
                        if (valuationmethod == 0) { // STANDARD 
                            valuationMethod = ValuationMethod.STANDARD;
                        } else if (valuationmethod == 1) { // FIFO
                            valuationMethod = ValuationMethod.FIFO;
                        } else if (valuationmethod == 2) { // AVERAGE
                            valuationMethod = ValuationMethod.AVERAGE;
                        }
                    }
                    
                    String detailID = "";
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[17])) {
                        detailID = (String) row[17];
                    }
                    boolean linkflag = false;
                    if (row.length >= 30 && !StringUtil.isNullOrEmptyWithTrim((String) row[30])) {
                        linkflag = true;
                    }
                    String currencyid = "";
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[13])) {
                        currencyid = (String) row[13];
                    }
                    Date transactionDate = (Date) row[4];
                    double baseUOMRate;
                    if (row[11] instanceof BigInteger) {
                        BigInteger baseRate = (BigInteger) row[11];
                        baseUOMRate = baseRate.doubleValue();
                    } else {
                        baseUOMRate = (Double) row[11];
                    }
//                    double baseUOMRate = (Double) row[11]; // Conversion Factor
                    String invoiceID = (String) row[14];
                    double grSpotRate = 0.0;
                    double amount = 0.0;
                    
                    try {
                        if (!StringUtil.isNullOrEmptyWithTrim((String) row[16])) {
                            grSpotRate = StringUtil.getDouble((String) row[16]);
                        }
                    } catch (java.lang.ClassCastException ex) {
                        if (row[16] != null) {
                            grSpotRate = (double) row[16];
                        }
                    }
                    double price = 0;
                    quantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate, companyid);
                    if (transType.intValue() != TransactionBatch.DocType_DO) {
                        price = row[10] != null ? (Double) row[10] : 0.0;
                        int discount = 0;
                        if (row[33] instanceof BigInteger) {
                            BigInteger discountispercent = (BigInteger) row[33];
                            discount = discountispercent.intValue();
                        } else {
                            discount = (Integer) row[33];
                        }

                        String gstincluded = row[34] != null ? (String) row[34] : "";
                        if (StringUtil.isNullOrEmptyWithTrim(gstincluded)) {
                            gstincluded = "F";
                        }
                        if (gstincluded.equals("F")) {
                            /*
                             In Including GST case, discount price is already subtracted from the unit price so no need to subtract discount
                             */
                            double discountPrice;
                            if (row[32] != null && row[32] instanceof BigInteger) {
                                BigInteger discP = (BigInteger) row[32];
                                discountPrice = discP.doubleValue();
                            } else {
                                discountPrice = row[32] != null ? (Double) row[32] : 0.0;
                            }
                            if (discount != 0) {
                                discountPrice = discountPrice / 100;
                                discountPrice = discountPrice * (price * quantity);
                            }
                            if (quantity != 0) {
                                double pricePerQty = ((price * quantity) - discountPrice) / quantity;
                                price = pricePerQty;
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(currencyid) && (StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(currencyid))) {
                            KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, price, currencyid, transactionDate, grSpotRate);
                            price = (Double) crresult.getEntityList().get(0);
                        }
                        price = price / baseUOMRate;
                        int inventoryValuationType = 0;
                        if(extraParameters.containsKey("inventoryValuationType")){
                            inventoryValuationType = (Integer)extraParameters.get("inventoryValuationType");
                        }
                        else{
                            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                            CompanyAccountPreferences pref = (CompanyAccountPreferences) extraprefresult.getEntityList().get(0);
                            inventoryValuationType=pref.getInventoryValuationType();
                        }
                        if (!(inventoryValuationType == Constants.PERPETUAL_VALUATION_METHOD)) {
                            price = Double.parseDouble(authHandler.getFormattedUnitPrice(price, companyid));
                        }
                    }
                    String billid = (String) row[15];
                    JSONObject json = new JSONObject();
                    if (transType.intValue() == TransactionBatch.DocType_IST_ISSUE) {
                        /**
                         * If InterStoreTransfer is created with GRN QA flow or
                         * not. If IST is created for GRN QA then the price of
                         * IST should only be adjusted with GRN and not with
                         * other document(s) i.e. price of IST OUT will be same
                         * as of GRN (ERP-35843).
                         */
                        Object[] objArr = null;
                        if(extraParameters.containsKey("rateExchangeRateFromGRODMap")){
                            objArr = (Object[])((Map)extraParameters.get("rateExchangeRateFromGRODMap")).get(billid);
                        }
                        else{
                        KwlReturnObject kwl = null;
                        kwl = accProductObj.getRateAndExchangeRateFromGoodsReceiptOrderDetail(billid, companyid);
                        if (kwl != null && kwl.getEntityList() != null && !kwl.getEntityList().isEmpty()) {
                            List l = kwl.getEntityList();
                            if (l != null && !l.isEmpty()) {
                                Object o = l.get(0);
                                objArr = (Object[]) o;
                            }
                            if (objArr != null && objArr.length > 0) {
                                double groRate = (double) objArr[0];
                                double groExternalCurrencyRate = (double) objArr[1];
                                String groCurrency = "";
                                if (!StringUtil.isNullOrEmptyWithTrim((String) objArr[2])) {
                                    groCurrency = (String) objArr[2];
                                }
                                String grodID = (String) objArr[3];
                                if (!StringUtil.isNullOrEmpty(groCurrency) && (StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(groCurrency))) {
                                    KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, groRate, groCurrency, transactionDate, groExternalCurrencyRate);
                                    price = (Double) crresult.getEntityList().get(0);
                                } else {
                                    price = groRate;
                                }
                                json.put("considerGRNPrice", true);
                                json.put("groDetailID", grodID);
                            }
                            }
                        }
                    } else if (transType.intValue() == TransactionBatch.DocType_GRN) {
                        /**
                         * Is GoodsReceiptOrder is sent to QA to not. If
                         * isGoodsReceiptOrderDetailSentToQA is true then don't
                         * adjust it with outstanding DO (ERP-35843).
                         */
                        boolean isGoodsReceiptOrderDetailSentToQA = false;
                        if(extraParameters.containsKey("goodsReceiptOrderDetailSentToQAList")){
                            if(((List)extraParameters.get("goodsReceiptOrderDetailSentToQAList")).contains(detailID)){
                                isGoodsReceiptOrderDetailSentToQA = true;
                            }
                        }
                        else{
                            isGoodsReceiptOrderDetailSentToQA = accProductObj.isGoodsReceiptOrderDetailSentToQA(detailID, companyid);
                        }
                        if (isGoodsReceiptOrderDetailSentToQA) {
                            json.put("isGoodsReceiptOrderDetailSentToQA", isGoodsReceiptOrderDetailSentToQA);
                        }
                    }
                    String transactionNumber = (String) row[5];
                    String personCode = "";
                    String personName = "";
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[6])) {
                        personCode = (String) row[6];
                    }
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[7])) {
                        personName = (String) row[7];
                    }
                    
                    
                    String warehouseId = null;
                    String locationId = null;
                    String rowId = null;
                    String rackId = null;
                    String binId = null;
                    String batchName = "";
                    String serialNames = "";
                    
                    boolean openingtransaction = false;
                    if (!(isFromStockValuationDetail || isFromStockValuationSummary)) {
                        openingtransaction = stockService.isOpeingOrPeriodTransaction(transactionDate, startDate, transType.intValue());
                    }
                    Map storageParams = new HashMap();
                    if (row[24] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[24])) { // Warehouse ID
                        storageParams.put("warehouseId", (String) row[24]);
                        warehouseId = (String) row[24];
                    }
                    if (row[23] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[23])) { // Location ID
                        storageParams.put("locationId", (String) row[23]);
                        locationId = (String) row[23];
                    }
                    if (row[25] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[25])) { // Row ID
                        storageParams.put("rowId", (String) row[25]);
                        rowId = (String) row[25];
                    }
                    if (row[26] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[26])) { // Rack ID
                        storageParams.put("rackId", (String) row[26]);
                        rackId = (String) row[26];
                    }
                    if (row[27] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[27])) { // BIN ID
                        storageParams.put("binId", (String) row[27]);
                        binId = (String) row[27];
                    }
//                    if (product.isIsBatchForProduct() && StringUtil.isNullOrEmpty((String) row[28])) {
//                        System.out.println(product.getProductid());
//                    }
                    if (row[28] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[28])) { // Batch Name
                        storageParams.put("batchName", (String) row[28]);
                        batchName = (String) row[28];
                    }
                    if (row[31] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[31])) {// comma separated serials from inventory modules
                        serialNames = (String) row[31];
                    }
                    boolean isPeriodTransaction = false;
                    if (!openingtransaction && endDate != null && transactionDate != null) {
                        /* if "transaction" is not an opening transaction and enddate is not null */
                        if (DateUtils.isSameDay(transactionDate, endDate) || transactionDate.before(endDate)) {
                            /* check if the transaction date <= enddate */
                            isPeriodTransaction = true;
                        }
                    }
                    if ((requestParams.containsKey("isItemHistoryReport") && requestParams.get("isItemHistoryReport") != null) || (requestParams.containsKey("stockAgeing") && requestParams.get("stockAgeing") != null)) {
                        /* For "Item History Report" consider the isPeriodTransaction as "TRUE" (All transactions need to be considered) */
                        isPeriodTransaction = true; 
                    }
                    if (storageParams != null && !storageParams.isEmpty() && row[29] != null) {
                        if (isSerialForProduct) {
                            quantity = 1;
                            valuationMethod = ValuationMethod.FIFO;
                        } else if (!StringUtil.isNullOrEmptyWithTrim(row[29].toString())) {
                            quantity = Double.parseDouble(row[29].toString());
//                            quantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate);
                        }
                    }
                    if (isSerialForProduct && quantity != 0) {
                        quantity = 1;
                        valuationMethod = ValuationMethod.FIFO;
                    }
                    Integer srNo = null;
                    Long createdon = null;
                    if (row[20] != null && !StringUtil.isNullOrEmptyWithTrim((row[20].toString()).trim())) {
                        srNo = Integer.parseInt(row[20].toString().trim());
                    }
                    if (row[21] != null && !StringUtil.isNullOrEmptyWithTrim(row[21].toString()) && df != null) {
                        try {
                            createdon = Long.parseLong(row[21].toString());
                        } catch (Exception ex) {
                            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                Date d = f.parse(row[21].toString());
                                createdon = d.getTime();
                            } catch (ParseException x) {
                                System.out.println("" + ex.getMessage());
                            }
                        }
                    }
                    /**
                     * For Landed Invoice calculation.
                     */
                    double prerate = price;
                    json.put("withoutlanded", prerate);
                    json.put("isActivateLandedInvAmt", isActivateLandedInvAmt);
                    if (isActivateLandedInvAmt && transType.intValue() == TransactionBatch.DocType_GRN && invoiceID!=null) {
                        /**
                         * Getting landing cost categories for a product. 
                         */
                        Set<LandingCostCategory> lccSet = product != null ? product.getLccategoryid() : new HashSet<LandingCostCategory>();
                        double unitlandedcost=0;
                        double groqty = 0; //goodsreceipt order quantity
                        double landingcost=0;
                        
                        if (lccSet != null && !lccSet.isEmpty()) {
                            for (LandingCostCategory lcc : lccSet) {
                                String landingcostcategory = lcc.getId();
                                /**
                                 * Iterate all expense invoices and calculate
                                 * total amount in base currency.
                                 */
                                
                                KwlReturnObject pikwl = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceID);
                                GoodsReceipt consignmentgrobj = (GoodsReceipt) (pikwl.getEntityList().isEmpty() ? null : pikwl.getEntityList().get(0));

                                KwlReturnObject kwlLCObj = accGoodsReceiptDAOobj.getLandedInviceList(invoiceID, landingcostcategory);
                                List<String> expenseInvoicelist = kwlLCObj.getEntityList();
                                
                                
                                for (String expenseInvid : expenseInvoicelist) {
                                    double landingCosttax=0.0; //tax on the expense invoice excluded from inventory side
                                    double rate = 0d; //in transaction currency
                                    double unitrate = 0d;
                                    double totalamount = 0d; //total amount of all products
                                    double expenseqty = 0d;
                                    double pw = product.getProductweight();
                                    double totalWeight = 0d;
                                    double itemWgt = 0.0;
                                    double totalvalue = 0d;
                                    double manualproductamt = 0d;
                                    double manualbaseamount = 0d;
                                    double noexpenseitem = 0d;
                                    double baserate = 0;
                                    double expensecharge = 0;
                                    KwlReturnObject crresult = null;
                                   
                                    //get the specific product details from Grodetails table for unit rate of that product
//                                    KwlReturnObject grodkwl = accGoodsReceiptDAOobj.getProductDetailsFromGoodsReceipt(invoiceID, productid,detailID ,companyid);
//                                   Object[] grdo = grodkwl.getEntityList().isEmpty()?null:(Object[]) grodkwl.getEntityList().get(0);
//                                    if(grdo!=null){
//                                        groqty = Double.parseDouble(grdo[0]!=null?grdo[0].toString():"0.0");
//                                        double grdorate = Double.parseDouble(grdo[1].toString());
//                                        unitrate = authHandler.roundUnitPrice(grdorate, companyid);                                        
//                                    }
                                                                        
                                    //convert unit rate to base currency for landed cost calculation
                                    if (!StringUtil.isNullOrEmpty(currencyid) && (StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(currencyid))) {
                                         crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, unitrate, currencyid, transactionDate, grSpotRate);
                                         unitrate = authHandler.roundUnitPrice((Double) crresult.getEntityList().get(0),companyid);
                                    }
                                    totalvalue  = authHandler.roundUnitPrice(groqty*unitrate, companyid);
                                    
                                    KwlReturnObject custresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), expenseInvid);
                                    GoodsReceipt expenseInvObj = (GoodsReceipt) custresult.getEntityList().get(0);
                                    if (expenseInvObj != null) {
                                       
                                        amount = price * quantity; //price is already in base currency converted above

                                        //iterate on values that match with the expense invoice and the given landing cost category
                                        if (landingcostcategory.equalsIgnoreCase(expenseInvObj.getLandingCostCategory().getId())) {
                                            KwlReturnObject kwlnoEligiableItem = accGoodsReceiptDAOobj.getNumberEligiableItem(expenseInvObj.getID(), landingcostcategory);
                                            List noEligiableItemList = kwlnoEligiableItem.getEntityList();
                                            Iterator itrItem = noEligiableItemList.iterator();

                                            while (itrItem.hasNext()) {
                                                Object[] valueArray = (Object[]) itrItem.next();
                                                List<Object> valueObjLit = (valueArray != null) ? new ArrayList(Arrays.asList(valueArray)) : null;
                                                expenseqty = (valueObjLit.size() > 0 && valueObjLit.get(0) != null) ? (double) valueObjLit.get(0) : 0.0D;
                                                rate = (valueObjLit.size() > 1 && valueObjLit.get(1) != null) ? (double) valueObjLit.get(1) : 0.0D;
                                                itemWgt = (valueObjLit.size() > 2 && valueObjLit.get(2) != null) ? (double) valueObjLit.get(2) : 0.0D;
                                                String goodsrecId= (valueObjLit.size() > 3 && valueObjLit.get(3) != null) ? (String) valueObjLit.get(3) : "";                                       
                                                //PI Object from the eligible Pis for the expense invoice
                                                KwlReturnObject custresulttemp = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), goodsrecId);
                                                GoodsReceipt tempinvoiceObj = (GoodsReceipt) custresulttemp.getEntityList().get(0);
                                                
                                                KWLCurrency currencytemp = (KWLCurrency) tempinvoiceObj.getCurrency();
                                                String currencyIdtemp = currencytemp.getCurrencyID();
                                                Date billDateTemp = tempinvoiceObj.getJournalEntry() != null ? tempinvoiceObj.getJournalEntry().getEntryDate() : tempinvoiceObj.getFormdate();
                                                KwlReturnObject ruternBR = currencyDAO.getCurrencyToBaseAmount(requestParams, rate, currencyIdtemp, billDateTemp, tempinvoiceObj.getJournalEntry().getExternalCurrencyRate());
                                                baserate = authHandler.roundUnitPrice((Double) ruternBR.getEntityList().get(0), companyid);
                                                //total valuations to go in the map for landed cost
                                                totalamount += (expenseqty * baserate);
                                                totalWeight += (itemWgt * expenseqty);
                                                noexpenseitem += expenseqty;
                                            }
                                            //For manual category landed cost expenseinv set value directly as manually allocated by user   
                                            if (LandingCostAllocationType.getByValue(lcc.getLcallocationid()) == LandingCostAllocationType.MANUAL) {
                                                Set<GoodsReceiptDetail> grdetailset = consignmentgrobj != null ? consignmentgrobj.getRows() : null;
                                                if (grdetailset != null && !grdetailset.isEmpty()) {
                                                    for (GoodsReceiptDetail grd : grdetailset) {
                                                        String grnid = grd.getGoodsReceiptOrderDetails()!=null?grd.getGoodsReceiptOrderDetails().getGrOrder().getID():"";
                                                        //match the productid in the PI detail with the current one in row[] or match the GRN id with the current billid
                                                        if ((grd.getInventory().getProduct().getID().equalsIgnoreCase(productid) && StringUtil.isNullOrEmpty(grnid)) || (grd.getInventory().getProduct().getID().equalsIgnoreCase(productid) && grnid.equalsIgnoreCase(billid))) {
                                                            KwlReturnObject kwlreturn = accGoodsReceiptDAOobj.getManualProductCostLCC(expenseInvObj.getID(), grd.getID());
                                                            List itemList = kwlreturn.getEntityList();
                                                            Iterator itemItr = itemList.iterator();
                                                            while (itemItr.hasNext()) {
                                                                LccManualWiseProductAmount lccManualWiseProductAmount = (LccManualWiseProductAmount) itemItr.next();
                                                                manualbaseamount = lccManualWiseProductAmount.isCustomDutyAllocationType() ? lccManualWiseProductAmount.getTaxablevalueforigst() : lccManualWiseProductAmount.getAmount();
                                                                KWLCurrency currencytemp = (KWLCurrency) expenseInvObj.getCurrency();
                                                                String currencyIdtemp = currencytemp.getCurrencyID();
                                                                Date billDateTemp = expenseInvObj.getJournalEntry() != null ? expenseInvObj.getJournalEntry().getEntryDate() : expenseInvObj.getFormdate();
                                                                KwlReturnObject ruternBRExpan = currencyDAO.getCurrencyToBaseAmount(requestParams, manualbaseamount, currencyIdtemp, billDateTemp, expenseInvObj.getJournalEntry().getExternalCurrencyRate());
                                                                manualproductamt = authHandler.roundUnitPrice((Double) ruternBRExpan.getEntityList().get(0), companyid);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            //to calculate row level tax in the invoice and reduce tax amount from landed cost 
                                            if (expenseInvObj.getTax() == null) {
                                                Set<ExpenseGRDetail> expensegrset = expenseInvObj.getExpenserows();
                                                for (ExpenseGRDetail expgrd : expensegrset) {
                                                        landingCosttax += expgrd.getRowTaxAmount();
                                                    }
                                            } else if (expenseInvObj.getTax() != null) { //global level tax
                                                landingCosttax = expenseInvObj.getTaxamountinbase();
                                            }
                                            if (landingCosttax != 0.0) {
                                                String transcurrency = expenseInvObj.getCurrency()!=null?expenseInvObj.getCurrency().getCurrencyID():"";
                                                if (!transcurrency.equalsIgnoreCase(currencyid)) {
                                                    double exrate = expenseInvObj.getExchangeRateDetail().getExchangeRate();
                                                    crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, landingCosttax, currencyid, transactionDate, exrate);
                                                    landingCosttax = authHandler.roundUnitPrice((Double) crresult.getEntityList().get(0), companyid);
                                                }
                                            }
                                            
                                            Map<String, Double> allcactionMthdData = new HashMap<>();
                                            allcactionMthdData.put("totLandedCost", expenseInvObj.getInvoiceAmountInBase() - landingCosttax);
                                            allcactionMthdData.put("noEligiableItem", noexpenseitem);
                                            allcactionMthdData.put("lineItemQty", groqty);
                                            allcactionMthdData.put("valueOfItem", totalvalue);
                                            allcactionMthdData.put("eligiableItemCost", totalamount);
                                            allcactionMthdData.put("eligiableItemWgt", totalWeight);
                                            allcactionMthdData.put("itemWght", (pw * groqty));
                                            allcactionMthdData.put("manualProductAmount", manualproductamt);

                                            expensecharge = LandingCostAllocationType.getTotalLanddedCost(expenseInvObj.getLandingCostCategory().getLcallocationid(), allcactionMthdData);
                                            landingcost += expensecharge;
                                            
                                            //if similar landing cost category already exists then add current amount into that
                                            if (json.has("landingcostpairs")) {
                                                StringBuilder existingpair = new StringBuilder(json.getString("landingcostpairs"));
                                                existingpair.append(",").append(landingcostcategory).append(":").append(authHandler.roundUnitPrice(expensecharge, companyid));
                                                json.put("landingcostpairs", existingpair);
                                            } else {
                                                StringBuilder newpair = new StringBuilder("");
                                                newpair.append(landingcostcategory).append(":").append(authHandler.roundUnitPrice(expensecharge, companyid));
                                                json.put("landingcostpairs", newpair);
                                            }
                                        }
                                    }
                                }
                            }
                             
                            //pushing these fields into JSON which is passed to PriceValuationStack to display on stockLedger/Valuation Reports
                                    landingcost=authHandler.roundUnitPrice(landingcost, companyid);   //rounding the total landed cost
                                    unitlandedcost = authHandler.roundUnitPrice((landingcost / groqty), companyid);  //get the unit landed cost
                                    price =authHandler.roundUnitPrice((price + unitlandedcost), companyid); //add unit landed cost into the product price
                                    if (landingcost != 0.0) {
                                        json.put("avglandedcost",price);
                                    }                                  
                        }
//                        amount = stockRate * quantity;
                    }
                    /*
                    Code to check if case of Serial JSON or Product JSON
                    */
                    String isSerialJson = row[35] != null ? (String) row[35] : "";          // Check if Serial JSON or Product JSON
                    if (StringUtil.isNullOrEmptyWithTrim(isSerialJson)) {
                        isSerialJson = "F";
                    }
                    
                    String remark = "", assembledProductID = "", costCenterID = "", stockUOMID = "", memo = "";
                    if (isMaterialInOutReport) {
                        remark = row[36] != null ? (String) row[36] : "";
                        assembledProductID = row[37] != null ? (String) row[37] : "";
                        costCenterID = row[38] != null ? (String) row[38] : "";
                        stockUOMID = row[39] != null ? (String) row[39] : "";
                        memo = row[40] != null ? (String) row[40] : "";
                    } else {
                        memo = row[36] != null ? (String) row[36] : "";
                    }
                    
                    
                    json.put(Constants.companyid, companyid);
                    if (advanceSearchTransactionlist != null && !advanceSearchTransactionlist.isEmpty() && advanceSearchTransactionlist.containsKey(detailID)) {
                        /**
                         * If detail ID is present in
                         * advanceSearchTransactionlist then set
                         * isAdvanceSearchTransaction flag to true for
                         * particular transaction detail.
                         */
                        json.put(PriceValuationStack.isAdvanceSearchTransaction, true);
                        if (advanceSearchTransactionlist.get(detailID).equals("T")) {
                            isSerialJson = "T";
                        } 
                    }
                    
                    if (isSerialForProduct && (transType.intValue() == 0 || transType.intValue() == 1 || transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4 || transType.intValue() == 6)) {
                        int transactiontype = 28;
                        if (transType.intValue() == 0 || transType.intValue() == 1 || transType.intValue() == 6) {
                            transactiontype = 28;
                        } else if (transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4) {
                            if (transType.intValue() == 2) {
                                transactiontype = 31;
                            } else if (transType.intValue() == 3 || transType.intValue() == 5) {
                                transactiontype = 27;
                            } else if (transType.intValue() == 4) {
                                transactiontype = 29;
                            }
                        }
                        storageParams.put("transactiontype", (transactiontype + ""));
                        storageParams.put("detailid", detailID);
                        
                        /*
                        Pass searchjson to fetch only serials which matches search criterion.
                        */
                        if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                            Searchjson = requestParams.get("searchJson").toString();
                        }
                        if (!StringUtil.isNullOrEmptyWithTrim(Searchjson)) {
                            reqPar1.put(Constants.companyKey, requestParams.get(Constants.companyKey));
                            reqPar1.put(Constants.Acc_Search_Json, Searchjson);
                            reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                            reqPar1.put(Constants.appendCase, "and");
                            reqPar1.put(Constants.moduleid, Constants.SerialWindow_ModuleId);
//                          reqPar1.put("removeProductCustomFilter", true);
     
                             Searchjson = accReportsService.getSearchJsonByModule(reqPar1);
                        }
                        
                        storageParams.put("searchjson", Searchjson);
                        storageParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        storageParams.put("transType", transType.intValue());
                        storageParams.put("isSerialJson", "F");
                        storageParams.put("serialNameSearch", serialNameSearch); 
                        KwlReturnObject srno = accProductObj.getSerialIdByStorageDetails(storageParams);
                        KwlReturnObject srno1 =null;
                        if (advanceSearchTransactionlist != null && !advanceSearchTransactionlist.isEmpty() && advanceSearchTransactionlist.containsKey(detailID)) {
                            storageParams.put("isSerialJson", isSerialJson);
                            srno1 = accProductObj.getSerialIdByStorageDetails(storageParams);
                        }
                        storageParams.remove("transactiontype");
                        storageParams.remove("detailid");
                        storageParams.remove("searchjson");
                        storageParams.remove("filterConjuctionCriteria");
                        storageParams.remove("transType");
                        storageParams.remove("isSerialJson");
                        List list2 = srno.getEntityList();
                        List list3 = null;
                        if (srno1 !=null && !srno1.getEntityList().isEmpty()) {
                            list3 = srno1.getEntityList();
                        }
                        if (list2 != null && !list2.isEmpty()) {
                            for (Object obj : list2) {
                                String serialID = obj.toString();
                                storageParams.put("serialName", serialID);
                                if (advanceSearchTransactionlist != null && !advanceSearchTransactionlist.isEmpty()) {
                                    if (list3 != null && !list3.isEmpty() && list3.contains(serialID)) {
                                        json.put(PriceValuationStack.isAdvanceSearchTransaction, true);
                                    } else {
                                        json.put(PriceValuationStack.isAdvanceSearchTransaction, false);
                                    }
                                }
                                stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                            }
                        }
                    } else if (transType.intValue() == 7 || transType.intValue() == 8 || transType.intValue() == 9 || transType.intValue() == 10 || transType.intValue() == 11 || transType.intValue() == 12 || transType.intValue() == 13 || transType.intValue() == 14 || transType.intValue() == 15 || transType.intValue() == 16) {
                        if (!StringUtil.isNullOrEmptyWithTrim(serialNames)) {
                            List serialList = stockDAO.getERPSerialFromBatch(companyid, productid, warehouseId, locationId, rowId, rackId, binId, batchName, serialNames);
                            if (serialList != null && !serialList.isEmpty()) {
                                for (Object obj : serialList) {
                                    String serialID = obj.toString();
                                    if (isMaterialInOutReport || isStockLedgerDetailedReport) {
                                        /**
                                         * If Material In/Out or Stock Ledger
                                         * Detailed Report
                                         */
                                        if (StringUtil.isNullOrEmptyWithTrim(serialNameSearch) || (!StringUtil.isNullOrEmptyWithTrim(serialNameSearch) && !StringUtil.isNullOrEmptyWithTrim(serialID))) {
                                            /**
                                             * Push Transaction into stack only
                                             * if (serial name search is blank)
                                             * or (both serial name search and
                                             * serial Id is not empty).
                                             */
                                            storageParams.put("serialName", serialID);
                                            stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                                        }
                                    } else {
                                        storageParams.put("serialName", serialID);
                                        stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                                    }
                                }
                            }
                        } else {
                            if (isMaterialInOutReport || isStockLedgerDetailedReport) {
                                /**
                                 * If Material In/Out or Stock Ledger Detailed
                                 * Report
                                 */
                                if (StringUtil.isNullOrEmptyWithTrim(serialNameSearch) || (!StringUtil.isNullOrEmptyWithTrim(serialNameSearch) && !StringUtil.isNullOrEmptyWithTrim(""))) {
                                    /**
                                     * Push Transaction into stack only if
                                     * (serial name search is blank) or (both
                                     * serial name search and serial Id is not
                                     * empty).
                                     */
                                    storageParams.put("serialName", "");
                                    stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                                }
                            } else {
                                storageParams.put("serialName", "");
                                stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
//                            }
                            }
                        }
                    } else {
                        /* put details related to the location/warehouse/row /rack/bin/batch/serial */
                        if (!((isMaterialInOutReport || isStockLedgerDetailedReport) && !StringUtil.isNullOrEmpty(serialNameSearch))) {
                            // For Material In/Out Report and quick search applied on serial names then do not add products for which serial is not activated.
                            stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void pushTransactionForProduct(List list, HashMap<String, Object> requestParams, Date startDate, Product product, String productid, PriceValuationStack stack, Date endDate, Map advanceSearchTransactionlist) {
        try {       
            String basecurrency = requestParams.containsKey("basecurrencyid") ? (String)requestParams.get("basecurrencyid") : null;
            boolean isFromStockValuationDetail=false;
            boolean isFromStockValuationSummary=false;
            boolean isStockLedgerDetailedReport = false;// Stock Ledger Detailed Report
            if (requestParams.containsKey("isStockLedgerDetailedReport") && requestParams.get("isStockLedgerDetailedReport") != null) {
                isStockLedgerDetailedReport = Boolean.parseBoolean(requestParams.get("isStockLedgerDetailedReport").toString());
            }
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            boolean isMaterialInOutReport = false;// Material IN/OUT Report
            if (requestParams.containsKey("isMaterialInOutReport") && requestParams.get("isMaterialInOutReport") != null) {
                isMaterialInOutReport = Boolean.parseBoolean(requestParams.get("isMaterialInOutReport").toString());
            }
            String companyid=(String) requestParams.get("companyid");
          
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) extraprefresult.getEntityList().get(0);

            boolean isActivateLandedInvAmt = false;
            if (requestParams.containsKey("isactivatelandedinvamt") && requestParams.get("isactivatelandedinvamt") != null) {
                isActivateLandedInvAmt = Boolean.parseBoolean(requestParams.get("isactivatelandedinvamt").toString());
            }
            if (requestParams.containsKey("isFromStockValuationDetail") && requestParams.get("isFromStockValuationDetail") != null) {
                isFromStockValuationDetail = Boolean.parseBoolean(requestParams.get("isFromStockValuationDetail").toString());
            }
            if (requestParams.containsKey("isFromStockValuationSummary") && requestParams.get("isFromStockValuationSummary") != null) {
                isFromStockValuationSummary = Boolean.parseBoolean(requestParams.get("isFromStockValuationSummary").toString());
            }
            String serialNameSearch = "";
            /**
             * If Quick Search is on serial names. Added only for Material
             * IN/OUT Report.
             */
            if ((isMaterialInOutReport || isStockLedgerDetailedReport) && requestParams.containsKey("serialNameSearch") && requestParams.get("serialNameSearch") != null) {
                serialNameSearch = requestParams.get("serialNameSearch").toString();
            }
            String Searchjson = "";
            HashMap<String, Object> reqPar1 = new HashMap<String, Object>();
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
                if (requestParams.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }

            if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                Searchjson = requestParams.get("searchJson").toString();
            }
            if (!StringUtil.isNullOrEmpty(Searchjson)) {
                reqPar1.put(Constants.companyKey, companyid);
                reqPar1.put(Constants.Acc_Search_Json, Searchjson);
                reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                reqPar1.put(Constants.appendCase, "and");
                reqPar1.put(Constants.moduleid, Constants.SerialWindow_ModuleId);
//                          reqPar1.put("removeProductCustomFilter", true);

                Searchjson = accReportsService.getSearchJsonByModule(reqPar1);
            }
            if (list != null && !list.isEmpty()) {
                for (Object object : list) {
                    Object[] row = (Object[]) object;
                    double quantity = (Double) row[9];
                    BigInteger transType = (BigInteger) row[0];
                    ValuationMethod valuationMethod = null;
                    boolean isSerialForProduct = false;
                    int valuationmethod = 0;
                    
                    if (product != null) {
                        valuationMethod = product.getValuationMethod();
                        isSerialForProduct = product.isIsSerialForProduct();
                        productid = product.getID();
                    } else {
                        if (isMaterialInOutReport) {
                            valuationmethod = row[43] != null ? (Integer) row[43] : 0;
                            isSerialForProduct = (row[41] != null &&  row[41].toString().equals("T")) ? true : false;
                        } else {
                            valuationmethod = row[39] != null ? (Integer) row[39] : 0;
                            isSerialForProduct = (row[37] != null &&  row[37].toString().equals("T"))  ? true : false;

                        }
                        if (valuationmethod == 0) { // STANDARD 
                            valuationMethod = ValuationMethod.STANDARD;
                        } else if (valuationmethod == 1) { // FIFO
                            valuationMethod = ValuationMethod.FIFO;
                        } else if (valuationmethod == 2) { // AVERAGE
                            valuationMethod = ValuationMethod.AVERAGE;
                        }
                    }
                    
                    String detailID = "";
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[17])) {
                        detailID = (String) row[17];
                    }
                    boolean linkflag = false;
                    if (row.length >= 30 && !StringUtil.isNullOrEmptyWithTrim((String) row[30])) {
                        linkflag = true;
                    }
                    String currencyid = "";
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[13])) {
                        currencyid = (String) row[13];
                    }
                    Date transactionDate = (Date) row[4];
                    double baseUOMRate;
                    if (row[11] instanceof BigInteger) {
                        BigInteger baseRate = (BigInteger) row[11];
                        baseUOMRate = baseRate.doubleValue();
                    } else {
                        baseUOMRate = (Double) row[11];
                    }
//                    double baseUOMRate = (Double) row[11]; // Conversion Factor
                    String invoiceID = (String) row[14];
                    double grSpotRate = 0.0;
                    double amount = 0.0;
                    
                    try {
                        if (!StringUtil.isNullOrEmptyWithTrim((String) row[16])) {
                            grSpotRate = StringUtil.getDouble((String) row[16]);
                        }
                    } catch (java.lang.ClassCastException ex) {
                        if (row[16] != null) {
                            grSpotRate = (double) row[16];
                        }
                    }
                    double price = 0;
                    quantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate, companyid);
                    if (transType.intValue() != TransactionBatch.DocType_DO) {
                        price = row[10] != null ? (Double) row[10] : 0.0;
                        int discount = 0;
                        if (row[33] instanceof BigInteger) {
                            BigInteger discountispercent = (BigInteger) row[33];
                            discount = discountispercent.intValue();
                        } else {
                            discount = (Integer) row[33];
                        }

                        String gstincluded = row[34] != null ? (String) row[34] : "";
                        if (StringUtil.isNullOrEmptyWithTrim(gstincluded)) {
                            gstincluded = "F";
                        }
                        if (gstincluded.equals("F")) {
                            /*
                             In Including GST case, discount price is already subtracted from the unit price so no need to subtract discount
                             */
                            double discountPrice;
                            if (row[32] != null && row[32] instanceof BigInteger) {
                                BigInteger discP = (BigInteger) row[32];
                                discountPrice = discP.doubleValue();
                            } else {
                                discountPrice = row[32] != null ? (Double) row[32] : 0.0;
                            }
                            if (discount != 0) {
                                discountPrice = discountPrice / 100;
                                discountPrice = discountPrice * (price * quantity);
                            }
                            if (quantity != 0) {
                                double pricePerQty = ((price * quantity) - discountPrice) / quantity;
                                price = pricePerQty;
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(currencyid) && (StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(currencyid))) {
                            KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, price, currencyid, transactionDate, grSpotRate);
                            price = (Double) crresult.getEntityList().get(0);
                        }
                        price = price / baseUOMRate;
                        if (!(pref.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                            price = Double.parseDouble(authHandler.getFormattedUnitPrice(price, companyid));
                        }
                    }
                    String billid = (String) row[15];
                    JSONObject json = new JSONObject();
                    if (transType.intValue() == TransactionBatch.DocType_IST_ISSUE) {
                        /**
                         * If InterStoreTransfer is created with GRN QA flow or
                         * not. If IST is created for GRN QA then the price of
                         * IST should only be adjusted with GRN and not with
                         * other document(s) i.e. price of IST OUT will be same
                         * as of GRN (ERP-35843).
                         */
                        KwlReturnObject kwl = accProductObj.getRateAndExchangeRateFromGoodsReceiptOrderDetail(billid, companyid);
                        if (kwl != null && kwl.getEntityList() != null && !kwl.getEntityList().isEmpty()) {
                            List l = kwl.getEntityList();
                            if (l != null && !l.isEmpty()) {
                                Object o = l.get(0);
                                Object[] objArr = (Object[]) o;
                                if (objArr != null && objArr.length > 0) {
                                    double groRate = (double)objArr[0];
                                    double groExternalCurrencyRate = (double) objArr[1];
                                    String groCurrency = "";
                                    if (!StringUtil.isNullOrEmptyWithTrim((String) objArr[2])) {
                                        groCurrency = (String) objArr[2];
                                    }
                                    String grodID = (String) objArr[3];
                                    if (!StringUtil.isNullOrEmpty(groCurrency) && (StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(groCurrency))) {
                                        KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, groRate, groCurrency, transactionDate, groExternalCurrencyRate);
                                        price = (Double) crresult.getEntityList().get(0);
                                    } else {
                                        price = groRate;
                                    }
                                    json.put("considerGRNPrice", true);
                                    json.put("groDetailID", grodID);
                                }
                            }
                        }
                    } else if (transType.intValue() == TransactionBatch.DocType_GRN) {
                        /**
                         * Is GoodsReceiptOrder is sent to QA to not. If
                         * isGoodsReceiptOrderDetailSentToQA is true then don't
                         * adjust it with outstanding DO (ERP-35843).
                         */
                        boolean isGoodsReceiptOrderDetailSentToQA = accProductObj.isGoodsReceiptOrderDetailSentToQA(detailID, companyid);
                        if (isGoodsReceiptOrderDetailSentToQA) {
                            json.put("isGoodsReceiptOrderDetailSentToQA", isGoodsReceiptOrderDetailSentToQA);
                        }
                    }
                    String transactionNumber = (String) row[5];
                    String personCode = "";
                    String personName = "";
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[6])) {
                        personCode = (String) row[6];
                    }
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[7])) {
                        personName = (String) row[7];
                    }
                    
                    
                    String warehouseId = null;
                    String locationId = null;
                    String rowId = null;
                    String rackId = null;
                    String binId = null;
                    String batchName = "";
                    String serialNames = "";
                    
                    boolean openingtransaction = false;
                    if (!(isFromStockValuationDetail || isFromStockValuationSummary)) {
                        openingtransaction = stockService.isOpeingOrPeriodTransaction(transactionDate, startDate, transType.intValue());
                    }
                    Map storageParams = new HashMap();
                    if (row[24] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[24])) { // Warehouse ID
                        storageParams.put("warehouseId", (String) row[24]);
                        warehouseId = (String) row[24];
                    }
                    if (row[23] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[23])) { // Location ID
                        storageParams.put("locationId", (String) row[23]);
                        locationId = (String) row[23];
                    }
                    if (row[25] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[25])) { // Row ID
                        storageParams.put("rowId", (String) row[25]);
                        rowId = (String) row[25];
                    }
                    if (row[26] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[26])) { // Rack ID
                        storageParams.put("rackId", (String) row[26]);
                        rackId = (String) row[26];
                    }
                    if (row[27] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[27])) { // BIN ID
                        storageParams.put("binId", (String) row[27]);
                        binId = (String) row[27];
                    }
//                    if (product.isIsBatchForProduct() && StringUtil.isNullOrEmpty((String) row[28])) {
//                        System.out.println(product.getProductid());
//                    }
                    if (row[28] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[28])) { // Batch Name
                        storageParams.put("batchName", (String) row[28]);
                        batchName = (String) row[28];
                    }
                    if (row[31] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[31])) {// comma separated serials from inventory modules
                        serialNames = (String) row[31];
                    }
                    boolean isPeriodTransaction = false;
                    if (!openingtransaction && endDate != null && transactionDate != null) {
                        /* if "transaction" is not an opening transaction and enddate is not null */
                        if (DateUtils.isSameDay(transactionDate, endDate) || transactionDate.before(endDate)) {
                            /* check if the transaction date <= enddate */
                            isPeriodTransaction = true;
                        }
                    }
                    if ((requestParams.containsKey("isItemHistoryReport") && requestParams.get("isItemHistoryReport") != null) || (requestParams.containsKey("stockAgeing") && requestParams.get("stockAgeing") != null)) {
                        /* For "Item History Report" consider the isPeriodTransaction as "TRUE" (All transactions need to be considered) */
                        isPeriodTransaction = true; 
                    }
                    if (storageParams != null && !storageParams.isEmpty() && row[29] != null) {
                        if (isSerialForProduct) {
                            quantity = 1;
                            valuationMethod = ValuationMethod.FIFO;
                        } else if (!StringUtil.isNullOrEmptyWithTrim(row[29].toString())) {
                            quantity = Double.parseDouble(row[29].toString());
//                            quantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate);
                        }
                    }
                    if (isSerialForProduct && quantity != 0) {
                        quantity = 1;
                        valuationMethod = ValuationMethod.FIFO;
                    }
                    Integer srNo = null;
                    Long createdon = null;
                    if (row[20] != null && !StringUtil.isNullOrEmptyWithTrim((row[20].toString()).trim())) {
                        srNo = Integer.parseInt(row[20].toString().trim());
                    }
                    if (row[21] != null && !StringUtil.isNullOrEmptyWithTrim(row[21].toString()) && df != null) {
                        try {
                            createdon = Long.parseLong(row[21].toString());
                        } catch (Exception ex) {
                            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                Date d = f.parse(row[21].toString());
                                createdon = d.getTime();
                            } catch (ParseException x) {
                                System.out.println("" + ex.getMessage());
                            }
                        }
                    }
                    /**
                     * For Landed Invoice calculation.
                     */
                    double prerate = price;
                    json.put("withoutlanded", prerate);
                    json.put("isActivateLandedInvAmt", isActivateLandedInvAmt);
                    if (isActivateLandedInvAmt && transType.intValue() == TransactionBatch.DocType_GRN && invoiceID!=null) {
                        /**
                         * Getting landing cost categories for a product. 
                         */
                        Set<LandingCostCategory> lccSet = product != null ? product.getLccategoryid() : new HashSet<LandingCostCategory>();
                        double unitlandedcost=0;
                        double groqty = 0; //goodsreceipt order quantity
                        double landingcost=0;
                        
                        if (lccSet != null && !lccSet.isEmpty()) {
                            for (LandingCostCategory lcc : lccSet) {
                                String landingcostcategory = lcc.getId();
                                /**
                                 * Iterate all expense invoices and calculate
                                 * total amount in base currency.
                                 */
                                
                                KwlReturnObject pikwl = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceID);
                                GoodsReceipt consignmentgrobj = (GoodsReceipt) (pikwl.getEntityList().isEmpty() ? null : pikwl.getEntityList().get(0));

                                KwlReturnObject kwlLCObj = accGoodsReceiptDAOobj.getLandedInviceList(invoiceID, landingcostcategory);
                                List<String> expenseInvoicelist = kwlLCObj.getEntityList();
                                
                                
                                for (String expenseInvid : expenseInvoicelist) {
                                    double landingCosttax=0.0; //tax on the expense invoice excluded from inventory side
                                    double rate = 0d; //in transaction currency
                                    double unitrate = 0d;
                                    double totalamount = 0d; //total amount of all products
                                    double expenseqty = 0d;
                                    double pw = product.getProductweight();
                                    double totalWeight = 0d;
                                    double itemWgt = 0.0;
                                    double totalvalue = 0d;
                                    double manualproductamt = 0d;
                                    double manualbaseamount = 0d;
                                    double noexpenseitem = 0d;
                                    double baserate = 0;
                                    double expensecharge = 0;
                                    KwlReturnObject crresult = null;
                                   
                                    //get the specific product details from Grodetails table for unit rate of that product
//                                    KwlReturnObject grodkwl = accGoodsReceiptDAOobj.getProductDetailsFromGoodsReceipt(invoiceID, productid,detailID ,companyid);
//                                   Object[] grdo = grodkwl.getEntityList().isEmpty()?null:(Object[]) grodkwl.getEntityList().get(0);
//                                    if(grdo!=null){
//                                        groqty = Double.parseDouble(grdo[0]!=null?grdo[0].toString():"0.0");
//                                        double grdorate = Double.parseDouble(grdo[1].toString());
//                                        unitrate = authHandler.roundUnitPrice(grdorate, companyid);                                        
//                                    }
                                                                        
                                    //convert unit rate to base currency for landed cost calculation
                                    if (!StringUtil.isNullOrEmpty(currencyid) && (StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(currencyid))) {
                                         crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, unitrate, currencyid, transactionDate, grSpotRate);
                                         unitrate = authHandler.roundUnitPrice((Double) crresult.getEntityList().get(0),companyid);
                                    }
                                    totalvalue  = authHandler.roundUnitPrice(groqty*unitrate, companyid);
                                    
                                    KwlReturnObject custresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), expenseInvid);
                                    GoodsReceipt expenseInvObj = (GoodsReceipt) custresult.getEntityList().get(0);
                                    if (expenseInvObj != null) {
                                       
                                        amount = price * quantity; //price is already in base currency converted above

                                        //iterate on values that match with the expense invoice and the given landing cost category
                                        if (landingcostcategory.equalsIgnoreCase(expenseInvObj.getLandingCostCategory().getId())) {
                                            KwlReturnObject kwlnoEligiableItem = accGoodsReceiptDAOobj.getNumberEligiableItem(expenseInvObj.getID(), landingcostcategory);
                                            List noEligiableItemList = kwlnoEligiableItem.getEntityList();
                                            Iterator itrItem = noEligiableItemList.iterator();

                                            while (itrItem.hasNext()) {
                                                Object[] valueArray = (Object[]) itrItem.next();
                                                List<Object> valueObjLit = (valueArray != null) ? new ArrayList(Arrays.asList(valueArray)) : null;
                                                expenseqty = (valueObjLit.size() > 0 && valueObjLit.get(0) != null) ? (double) valueObjLit.get(0) : 0.0D;
                                                rate = (valueObjLit.size() > 1 && valueObjLit.get(1) != null) ? (double) valueObjLit.get(1) : 0.0D;
                                                itemWgt = (valueObjLit.size() > 2 && valueObjLit.get(2) != null) ? (double) valueObjLit.get(2) : 0.0D;
                                                String goodsrecId= (valueObjLit.size() > 3 && valueObjLit.get(3) != null) ? (String) valueObjLit.get(3) : "";                                       
                                                //PI Object from the eligible Pis for the expense invoice
                                                KwlReturnObject custresulttemp = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), goodsrecId);
                                                GoodsReceipt tempinvoiceObj = (GoodsReceipt) custresulttemp.getEntityList().get(0);
                                                
                                                KWLCurrency currencytemp = (KWLCurrency) tempinvoiceObj.getCurrency();
                                                String currencyIdtemp = currencytemp.getCurrencyID();
                                                Date billDateTemp = tempinvoiceObj.getJournalEntry() != null ? tempinvoiceObj.getJournalEntry().getEntryDate() : tempinvoiceObj.getFormdate();
                                                KwlReturnObject ruternBR = currencyDAO.getCurrencyToBaseAmount(requestParams, rate, currencyIdtemp, billDateTemp, tempinvoiceObj.getJournalEntry().getExternalCurrencyRate());
                                                baserate = authHandler.roundUnitPrice((Double) ruternBR.getEntityList().get(0), companyid);
                                                //total valuations to go in the map for landed cost
                                                totalamount += (expenseqty * baserate);
                                                totalWeight += (itemWgt * expenseqty);
                                                noexpenseitem += expenseqty;
                                            }
                                            //For manual category landed cost expenseinv set value directly as manually allocated by user   
                                            if (LandingCostAllocationType.getByValue(lcc.getLcallocationid()) == LandingCostAllocationType.MANUAL) {
                                                Set<GoodsReceiptDetail> grdetailset = consignmentgrobj != null ? consignmentgrobj.getRows() : null;
                                                if (grdetailset != null && !grdetailset.isEmpty()) {
                                                    for (GoodsReceiptDetail grd : grdetailset) {
                                                        String grnid = grd.getGoodsReceiptOrderDetails()!=null?grd.getGoodsReceiptOrderDetails().getGrOrder().getID():"";
                                                        //match the productid in the PI detail with the current one in row[] or match the GRN id with the current billid
                                                        if ((grd.getInventory().getProduct().getID().equalsIgnoreCase(productid) && StringUtil.isNullOrEmpty(grnid)) || (grd.getInventory().getProduct().getID().equalsIgnoreCase(productid) && grnid.equalsIgnoreCase(billid))) {
                                                            KwlReturnObject kwlreturn = accGoodsReceiptDAOobj.getManualProductCostLCC(expenseInvObj.getID(), grd.getID());
                                                            List itemList = kwlreturn.getEntityList();
                                                            Iterator itemItr = itemList.iterator();
                                                            while (itemItr.hasNext()) {
                                                                LccManualWiseProductAmount lccManualWiseProductAmount = (LccManualWiseProductAmount) itemItr.next();
                                                                manualbaseamount = lccManualWiseProductAmount.isCustomDutyAllocationType() ? lccManualWiseProductAmount.getTaxablevalueforigst() : lccManualWiseProductAmount.getAmount();                                                                
                                                                KWLCurrency currencytemp = (KWLCurrency) expenseInvObj.getCurrency();
                                                                String currencyIdtemp = currencytemp.getCurrencyID();
                                                                Date billDateTemp = expenseInvObj.getJournalEntry() != null ? expenseInvObj.getJournalEntry().getEntryDate() : expenseInvObj.getFormdate();
                                                                KwlReturnObject ruternBRExpan = currencyDAO.getCurrencyToBaseAmount(requestParams, manualbaseamount, currencyIdtemp, billDateTemp, expenseInvObj.getJournalEntry().getExternalCurrencyRate());
                                                                manualproductamt = authHandler.roundUnitPrice((Double) ruternBRExpan.getEntityList().get(0), companyid);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            //to calculate row level tax in the invoice and reduce tax amount from landed cost 
                                            if (expenseInvObj.getTax() == null) {
                                                Set<ExpenseGRDetail> expensegrset = expenseInvObj.getExpenserows();
                                                for (ExpenseGRDetail expgrd : expensegrset) {
                                                    landingCosttax += expgrd.getRowTaxAmount();
                                                }
                                            } else if (expenseInvObj.getTax() != null) { //global level tax
                                                landingCosttax = expenseInvObj.getTaxamountinbase();
                                            }
                                            if (landingCosttax != 0.0) {
                                                String transcurrency = expenseInvObj.getCurrency()!=null?expenseInvObj.getCurrency().getCurrencyID():"";
                                                if (!transcurrency.equalsIgnoreCase(currencyid)) {
                                                    double exrate = expenseInvObj.getExchangeRateDetail().getExchangeRate();
                                                    crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, landingCosttax, currencyid, transactionDate, exrate);
                                                    landingCosttax = authHandler.roundUnitPrice((Double) crresult.getEntityList().get(0), companyid);
                                                }
                                            }
                                            
                                            Map<String, Double> allcactionMthdData = new HashMap<>();
                                            allcactionMthdData.put("totLandedCost", expenseInvObj.getInvoiceAmountInBase() - landingCosttax);
                                            allcactionMthdData.put("noEligiableItem", noexpenseitem);
                                            allcactionMthdData.put("lineItemQty", groqty);
                                            allcactionMthdData.put("valueOfItem", totalvalue);
                                            allcactionMthdData.put("eligiableItemCost", totalamount);
                                            allcactionMthdData.put("eligiableItemWgt", totalWeight);
                                            allcactionMthdData.put("itemWght", (pw * groqty));
                                            allcactionMthdData.put("manualProductAmount", manualproductamt);

                                            expensecharge = LandingCostAllocationType.getTotalLanddedCost(expenseInvObj.getLandingCostCategory().getLcallocationid(), allcactionMthdData);
                                            landingcost += authHandler.roundUnitPrice(expensecharge, companyid);
                                            
                                            //if similar landing cost category already exists then add current amount into that
                                            if (json.has("landingcostpairs")) {
                                                StringBuilder existingpair = new StringBuilder(json.getString("landingcostpairs"));
                                                existingpair.append(",").append(landingcostcategory).append(":").append(authHandler.roundUnitPrice(expensecharge, companyid));
                                                json.put("landingcostpairs", existingpair);
                                            } else {
                                                StringBuilder newpair = new StringBuilder("");
                                                newpair.append(landingcostcategory).append(":").append(authHandler.roundUnitPrice(expensecharge, companyid));
                                                json.put("landingcostpairs", newpair);
                                            }
                                        }
                                    }
                                }
                            }
                             
                            //pushing these fields into JSON which is passed to PriceValuationStack to display on stockLedger/Valuation Reports
                                    landingcost=authHandler.roundUnitPrice(landingcost, companyid);   //rounding the total landed cost
                                    unitlandedcost = authHandler.roundUnitPrice((landingcost / groqty), companyid);  //get the unit landed cost
                                    price =authHandler.roundUnitPrice((price + unitlandedcost), companyid); //add unit landed cost into the product price
                                    if (landingcost != 0.0) {
                                        json.put("avglandedcost",price);
                                    }                                  
                        }
//                        amount = stockRate * quantity;
                    }
                    /*
                    Code to check if case of Serial JSON or Product JSON
                    */
                    String isSerialJson = row[35] != null ? (String) row[35] : "";          // Check if Serial JSON or Product JSON
                    if (StringUtil.isNullOrEmptyWithTrim(isSerialJson)) {
                        isSerialJson = "F";
                    }
                    
                    String remark = "", assembledProductID = "", costCenterID = "", stockUOMID = "", memo = "";
                    if (isMaterialInOutReport) {
                        remark = row[36] != null ? (String) row[36] : "";
                        assembledProductID = row[37] != null ? (String) row[37] : "";
                        costCenterID = row[38] != null ? (String) row[38] : "";
                        stockUOMID = row[39] != null ? (String) row[39] : "";
                        memo = row[40] != null ? (String) row[40] : "";
                    } else {
                        memo = row[36] != null ? (String) row[36] : "";
                    }
                    
                    
                    json.put(Constants.companyid, companyid);
                    if (advanceSearchTransactionlist != null && !advanceSearchTransactionlist.isEmpty() && advanceSearchTransactionlist.containsKey(detailID)) {
                        /**
                         * If detail ID is present in
                         * advanceSearchTransactionlist then set
                         * isAdvanceSearchTransaction flag to true for
                         * particular transaction detail.
                         */
                        json.put(PriceValuationStack.isAdvanceSearchTransaction, true);
                        if (advanceSearchTransactionlist.get(detailID).equals("T")) {
                            isSerialJson = "T";
                        } 
                    }
                    
                    if (isSerialForProduct && (transType.intValue() == 0 || transType.intValue() == 1 || transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4 || transType.intValue() == 6)) {
                        int transactiontype = 28;
                        if (transType.intValue() == 0 || transType.intValue() == 1 || transType.intValue() == 6) {
                            transactiontype = 28;
                        } else if (transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4) {
                            if (transType.intValue() == 2) {
                                transactiontype = 31;
                            } else if (transType.intValue() == 3 || transType.intValue() == 5) {
                                transactiontype = 27;
                            } else if (transType.intValue() == 4) {
                                transactiontype = 29;
                            }
                        }
                        storageParams.put("transactiontype", (transactiontype + ""));
                        storageParams.put("detailid", detailID);
                        
                        /*
                        Pass searchjson to fetch only serials which matches search criterion.
                        */
                        if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                            Searchjson = requestParams.get("searchJson").toString();
                        }
                        if (!StringUtil.isNullOrEmptyWithTrim(Searchjson)) {
                            reqPar1.put(Constants.companyKey, requestParams.get(Constants.companyKey));
                            reqPar1.put(Constants.Acc_Search_Json, Searchjson);
                            reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                            reqPar1.put(Constants.appendCase, "and");
                            reqPar1.put(Constants.moduleid, Constants.SerialWindow_ModuleId);
//                          reqPar1.put("removeProductCustomFilter", true);
     
                             Searchjson = accReportsService.getSearchJsonByModule(reqPar1);
                        }
                        
                        storageParams.put("searchjson", Searchjson);
                        storageParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        storageParams.put("transType", transType.intValue());
                        storageParams.put("isSerialJson", "F");
                        storageParams.put("serialNameSearch", serialNameSearch); 
                        KwlReturnObject srno = accProductObj.getSerialIdByStorageDetails(storageParams);
                        KwlReturnObject srno1 =null;
                        if (advanceSearchTransactionlist != null && !advanceSearchTransactionlist.isEmpty() && advanceSearchTransactionlist.containsKey(detailID)) {
                            storageParams.put("isSerialJson", isSerialJson);
                            srno1 = accProductObj.getSerialIdByStorageDetails(storageParams);
                        }
                        storageParams.remove("transactiontype");
                        storageParams.remove("detailid");
                        storageParams.remove("searchjson");
                        storageParams.remove("filterConjuctionCriteria");
                        storageParams.remove("transType");
                        storageParams.remove("isSerialJson");
                        List list2 = srno.getEntityList();
                        List list3 = null;
                        if (srno1 !=null && !srno1.getEntityList().isEmpty()) {
                            list3 = srno1.getEntityList();
                        }
                        if (list2 != null && !list2.isEmpty()) {
                            for (Object obj : list2) {
                                String serialID = obj.toString();
                                storageParams.put("serialName", serialID);
                                if (advanceSearchTransactionlist != null && !advanceSearchTransactionlist.isEmpty()) {
                                    if (list3 != null && !list3.isEmpty() && list3.contains(serialID)) {
                                        json.put(PriceValuationStack.isAdvanceSearchTransaction, true);
                                    } else {
                                        json.put(PriceValuationStack.isAdvanceSearchTransaction, false);
                                    }
                                }
                                stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                            }
                        }
                    } else if (transType.intValue() == 7 || transType.intValue() == 8 || transType.intValue() == 9 || transType.intValue() == 10 || transType.intValue() == 11 || transType.intValue() == 12 || transType.intValue() == 13 || transType.intValue() == 14 || transType.intValue() == 15 || transType.intValue() == 16) {
                        if (!StringUtil.isNullOrEmptyWithTrim(serialNames)) {
                            List serialList = stockDAO.getERPSerialFromBatch(companyid, productid, warehouseId, locationId, rowId, rackId, binId, batchName, serialNames);
                            if (serialList != null && !serialList.isEmpty()) {
                                for (Object obj : serialList) {
                                    String serialID = obj.toString();
                                    if (isMaterialInOutReport || isStockLedgerDetailedReport) {
                                        /**
                                         * If Material In/Out or Stock Ledger
                                         * Detailed Report
                                         */
                                        if (StringUtil.isNullOrEmptyWithTrim(serialNameSearch) || (!StringUtil.isNullOrEmptyWithTrim(serialNameSearch) && !StringUtil.isNullOrEmptyWithTrim(serialID))) {
                                            /**
                                             * Push Transaction into stack only
                                             * if (serial name search is blank)
                                             * or (both serial name search and
                                             * serial Id is not empty).
                                             */
                                            storageParams.put("serialName", serialID);
                                            stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                                        }
                                    } else {
                                        storageParams.put("serialName", serialID);
                                        stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                                    }
                                }
                            }
                        } else {
                            if (isMaterialInOutReport || isStockLedgerDetailedReport) {
                                /**
                                 * If Material In/Out or Stock Ledger Detailed
                                 * Report
                                 */
                                if (StringUtil.isNullOrEmptyWithTrim(serialNameSearch) || (!StringUtil.isNullOrEmptyWithTrim(serialNameSearch) && !StringUtil.isNullOrEmptyWithTrim(""))) {
                                    /**
                                     * Push Transaction into stack only if
                                     * (serial name search is blank) or (both
                                     * serial name search and serial Id is not
                                     * empty).
                                     */
                                    storageParams.put("serialName", "");
                                    stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                                }
                            } else {
                                storageParams.put("serialName", "");
                                stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
//                            }
                            }
                        }
                    } else {
                        /* put details related to the location/warehouse/row /rack/bin/batch/serial */
                        if (!((isMaterialInOutReport || isStockLedgerDetailedReport) && !StringUtil.isNullOrEmpty(serialNameSearch))) {
                            // For Material In/Out Report and quick search applied on serial names then do not add products for which serial is not activated.
                            stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void pushTransactionForProduct(List list, HashMap<String, Object> requestParams, Product product, String productid, Map<String, Map> stockDateMap, Map advanceSearchTransactionlist, Map<String, Object> extraParameters,boolean isAdvanceSearchForValuation, boolean isNegativeStock) {
        try {       
            String basecurrency = requestParams.containsKey("basecurrencyid") ? (String)requestParams.get("basecurrencyid") : null;
            boolean isFromStockValuationDetail=false;
            boolean isFromStockValuationSummary=false;
            boolean isStockLedgerDetailedReport = false;// Stock Ledger Detailed Report
            if (requestParams.containsKey("isStockLedgerDetailedReport") && requestParams.get("isStockLedgerDetailedReport") != null) {
                isStockLedgerDetailedReport = Boolean.parseBoolean(requestParams.get("isStockLedgerDetailedReport").toString());
            }
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            boolean isMaterialInOutReport = false;// Material IN/OUT Report
            if (requestParams.containsKey("isMaterialInOutReport") && requestParams.get("isMaterialInOutReport") != null) {
                isMaterialInOutReport = Boolean.parseBoolean(requestParams.get("isMaterialInOutReport").toString());
            }
            String companyid=(String) requestParams.get("companyid");
          
//            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences pref = (CompanyAccountPreferences) extraprefresult.getEntityList().get(0);

            boolean isActivateLandedInvAmt = false;
            if (requestParams.containsKey("isactivatelandedinvamt") && requestParams.get("isactivatelandedinvamt") != null) {
                isActivateLandedInvAmt = Boolean.parseBoolean(requestParams.get("isactivatelandedinvamt").toString());
            }
            if (requestParams.containsKey("isFromStockValuationDetail") && requestParams.get("isFromStockValuationDetail") != null) {
                isFromStockValuationDetail = Boolean.parseBoolean(requestParams.get("isFromStockValuationDetail").toString());
            }
            if (requestParams.containsKey("isFromStockValuationSummary") && requestParams.get("isFromStockValuationSummary") != null) {
                isFromStockValuationSummary = Boolean.parseBoolean(requestParams.get("isFromStockValuationSummary").toString());
            }
            String serialNameSearch = "";
            /**
             * If Quick Search is on serial names. Added only for Material
             * IN/OUT Report.
             */
            if ((isMaterialInOutReport || isStockLedgerDetailedReport) && requestParams.containsKey("serialNameSearch") && requestParams.get("serialNameSearch") != null) {
                serialNameSearch = requestParams.get("serialNameSearch").toString();
            }
            String Searchjson = "";
            HashMap<String, Object> reqPar1 = new HashMap<String, Object>();
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
                if (requestParams.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }

            if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                Searchjson = requestParams.get("searchJson").toString();
            }
            if (!StringUtil.isNullOrEmpty(Searchjson)) {
                reqPar1.put(Constants.companyKey, companyid);
                reqPar1.put(Constants.Acc_Search_Json, Searchjson);
                reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                reqPar1.put(Constants.appendCase, "and");
                reqPar1.put(Constants.moduleid, Constants.SerialWindow_ModuleId);
//                          reqPar1.put("removeProductCustomFilter", true);

                Searchjson = accReportsService.getSearchJsonByModule(reqPar1);
            }
            if (list != null && !list.isEmpty()) {
                for (Object object : list) {
                    Object[] row = (Object[]) object;
                    double quantity = (Double) row[9];
                    BigInteger transType = (BigInteger) row[0];
                    ValuationMethod valuationMethod = null;
                    boolean isSerialForProduct = false;
                    int valuationmethod = 0;
                    
                    if (product != null) {
                        valuationMethod = product.getValuationMethod();
                        isSerialForProduct = product.isIsSerialForProduct();
                        productid = product.getID();
                    } else {
                        if (isMaterialInOutReport) {
                            valuationmethod = row[43] != null ? (Integer) row[43] : 0;
                            isSerialForProduct = (row[41] != null &&  row[41].toString().equals("T")) ? true : false;
                        } else {
                            valuationmethod = row[39] != null ? (Integer) row[39] : 0;
                            isSerialForProduct = (row[37] != null &&  row[37].toString().equals("T"))  ? true : false;

                        }
                        if (valuationmethod == 0) { // STANDARD 
                            valuationMethod = ValuationMethod.STANDARD;
                        } else if (valuationmethod == 1) { // FIFO
                            valuationMethod = ValuationMethod.FIFO;
                        } else if (valuationmethod == 2) { // AVERAGE
                            valuationMethod = ValuationMethod.AVERAGE;
                        }
                    }
                    
                    String detailID = "";
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[17])) {
                        detailID = (String) row[17];
                    }
                    boolean linkflag = false;
                    if (row.length >= 30 && !StringUtil.isNullOrEmptyWithTrim((String) row[30])) {
                        linkflag = true;
                    }
                    String currencyid = "";
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[13])) {
                        currencyid = (String) row[13];
                    }
                    Date transactionDate = (Date) row[4];
                    double baseUOMRate;
                    if (row[11] instanceof BigInteger) {
                        BigInteger baseRate = (BigInteger) row[11];
                        baseUOMRate = baseRate.doubleValue();
                    } else {
                        baseUOMRate = (Double) row[11];
                    }
//                    double baseUOMRate = (Double) row[11]; // Conversion Factor
                    String invoiceID = (String) row[14];
                    double grSpotRate = 0.0;
                    double amount = 0.0;
                    
                    try {
                        if (!StringUtil.isNullOrEmptyWithTrim((String) row[16])) {
                            grSpotRate = StringUtil.getDouble((String) row[16]);
                        }
                    } catch (java.lang.ClassCastException ex) {
                        if (row[16] != null) {
                            grSpotRate = (double) row[16];
                        }
                    }
                    double price = 0;
                    quantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate, companyid);
                    if (transType.intValue() != TransactionBatch.DocType_DO) {
                        price = row[10] != null ? (Double) row[10] : 0.0;
                        int discount = 0;
                        if (row[33] instanceof BigInteger) {
                            BigInteger discountispercent = (BigInteger) row[33];
                            discount = discountispercent.intValue();
                        } else {
                            discount = (Integer) row[33];
                        }

                        String gstincluded = row[34] != null ? (String) row[34] : "";
                        if (StringUtil.isNullOrEmptyWithTrim(gstincluded)) {
                            gstincluded = "F";
                        }
                        if (gstincluded.equals("F")) {
                            /*
                             In Including GST case, discount price is already subtracted from the unit price so no need to subtract discount
                             */
                            double discountPrice;
                            if (row[32] != null && row[32] instanceof BigInteger) {
                                BigInteger discP = (BigInteger) row[32];
                                discountPrice = discP.doubleValue();
                            } else {
                                discountPrice = row[32] != null ? (Double) row[32] : 0.0;
                            }
                            if (discount != 0) {
                                discountPrice = discountPrice / 100;
                                discountPrice = discountPrice * (price * quantity);
                            }
                            if (quantity != 0) {
                                double pricePerQty = ((price * quantity) - discountPrice) / quantity;
                                price = pricePerQty;
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(currencyid) && (StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(currencyid))) {
                            KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, price, currencyid, transactionDate, grSpotRate);
                            price = (Double) crresult.getEntityList().get(0);
                        }
                        price = price / baseUOMRate;
                        int inventoryValuationType = 0;
                        if(extraParameters.containsKey("inventoryValuationType")){
                            inventoryValuationType = (Integer)extraParameters.get("inventoryValuationType");
                        }
                        else{
                            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                            CompanyAccountPreferences pref = (CompanyAccountPreferences) extraprefresult.getEntityList().get(0);
                            inventoryValuationType=pref.getInventoryValuationType();
                        }
                        if (!(inventoryValuationType == Constants.PERPETUAL_VALUATION_METHOD)) {
                            price = Double.parseDouble(authHandler.getFormattedUnitPrice(price, companyid));
                        }
                    }
                    String billid = (String) row[15];
                    JSONObject json = new JSONObject();
                    if (transType.intValue() == TransactionBatch.DocType_IST_ISSUE) {
                            /**
                         * If InterStoreTransfer is created with GRN QA flow or
                         * not. If IST is created for GRN QA then the price of
                         * IST should only be adjusted with GRN and not with
                         * other document(s) i.e. price of IST OUT will be same
                         * as of GRN (ERP-35843).
                         */
                        Object[] objArr = null;
                        if (extraParameters.containsKey("rateExchangeRateFromGRODMap")) {
                            objArr = (Object[]) ((Map) extraParameters.get("rateExchangeRateFromGRODMap")).get(billid);
                        } else {
                        KwlReturnObject kwl = null;
                        kwl = accProductObj.getRateAndExchangeRateFromGoodsReceiptOrderDetail(billid, companyid);
                        if (kwl != null && kwl.getEntityList() != null && !kwl.getEntityList().isEmpty()) {
                            List l = kwl.getEntityList();
                            if (l != null && !l.isEmpty()) {
                                Object o = l.get(0);
                                objArr = (Object[]) o;
                            }
                            }
                        }
                            if (objArr != null && objArr.length > 0) {
                                double groRate = (double) objArr[0];
                                double groExternalCurrencyRate = (double) objArr[1];
                                String groCurrency = "";
                                if (!StringUtil.isNullOrEmptyWithTrim((String) objArr[2])) {
                                    groCurrency = (String) objArr[2];
                                }
                                String grodID = (String) objArr[3];
                                if (!StringUtil.isNullOrEmpty(groCurrency) && (StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(groCurrency))) {
                                    KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, groRate, groCurrency, transactionDate, groExternalCurrencyRate);
                                    price = (Double) crresult.getEntityList().get(0);
                                } else {
                                    price = groRate;
                                }
                                json.put("considerGRNPrice", true);
                                json.put("groDetailID", grodID);

                            }
                    } else if (transType.intValue() == TransactionBatch.DocType_GRN) {
                        /**
                         * Is GoodsReceiptOrder is sent to QA to not. If
                         * isGoodsReceiptOrderDetailSentToQA is true then don't
                         * adjust it with outstanding DO (ERP-35843).
                         */
                        boolean isGoodsReceiptOrderDetailSentToQA = false;
                        if(extraParameters.containsKey("goodsReceiptOrderDetailSentToQAList")){
                            if(((List)extraParameters.get("goodsReceiptOrderDetailSentToQAList")).contains(detailID)){
                                isGoodsReceiptOrderDetailSentToQA = true;
                            }
                        }
                        else{
                            isGoodsReceiptOrderDetailSentToQA = accProductObj.isGoodsReceiptOrderDetailSentToQA(detailID, companyid);
                        }
                        if (isGoodsReceiptOrderDetailSentToQA) {
                            json.put("isGoodsReceiptOrderDetailSentToQA", isGoodsReceiptOrderDetailSentToQA);
                        }
                    }
                    String transactionNumber = (String) row[5];
                    String personCode = "";
                    String personName = "";
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[6])) {
                        personCode = (String) row[6];
                    }
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[7])) {
                        personName = (String) row[7];
                    }
                    
                    
                    String warehouseId = null;
                    String locationId = null;
                    String rowId = null;
                    String rackId = null;
                    String binId = null;
                    String batchName = "";
                    String serialNames = "";
                    
                    Map storageParams = new HashMap();
                    if (row[24] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[24])) { // Warehouse ID
                        storageParams.put("warehouseId", (String) row[24]);
                        warehouseId = (String) row[24];
                    }
                    if (row[23] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[23])) { // Location ID
                        storageParams.put("locationId", (String) row[23]);
                        locationId = (String) row[23];
                    }
                    if (row[25] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[25])) { // Row ID
                        storageParams.put("rowId", (String) row[25]);
                        rowId = (String) row[25];
                    }
                    if (row[26] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[26])) { // Rack ID
                        storageParams.put("rackId", (String) row[26]);
                        rackId = (String) row[26];
                    }
                    if (row[27] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[27])) { // BIN ID
                        storageParams.put("binId", (String) row[27]);
                        binId = (String) row[27];
                    }
//                    if (product.isIsBatchForProduct() && StringUtil.isNullOrEmpty((String) row[28])) {
//                        System.out.println(product.getProductid());
//                    }
                    if (row[28] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[28])) { // Batch Name
                        storageParams.put("batchName", (String) row[28]);
                        batchName = (String) row[28];
                    }
                    if (row[31] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[31])) {// comma separated serials from inventory modules
                        serialNames = (String) row[31];
                    }
                    if (storageParams != null && !storageParams.isEmpty() && row[29] != null) {
                        if (isSerialForProduct) {
                            quantity = 1;
                            valuationMethod = ValuationMethod.FIFO;
                        } else if (!StringUtil.isNullOrEmptyWithTrim(row[29].toString())) {
                            quantity = Double.parseDouble(row[29].toString());
//                            quantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate);
                        }
                    }
                    if (isSerialForProduct && quantity != 0) {
                        quantity = 1;
                        valuationMethod = ValuationMethod.FIFO;
                    }
                    Integer srNo = null;
                    Long createdon = null;
                    if (row[20] != null && !StringUtil.isNullOrEmptyWithTrim((row[20].toString()).trim())) {
                        srNo = Integer.parseInt(row[20].toString().trim());
                    }
                    if (row[21] != null && !StringUtil.isNullOrEmptyWithTrim(row[21].toString()) && df != null) {
                        try {
                            createdon = Long.parseLong(row[21].toString());
                        } catch (Exception ex) {
                            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                Date d = f.parse(row[21].toString());
                                createdon = d.getTime();
                            } catch (ParseException x) {
                                System.out.println("" + ex.getMessage());
                            }
                        }
                    }
                    /**
                     * For Landed Invoice calculation.
                     */
                    double prerate = price;
                    json.put("withoutlanded", prerate);
                    json.put("isActivateLandedInvAmt", isActivateLandedInvAmt);
                    if (isActivateLandedInvAmt && transType.intValue() == TransactionBatch.DocType_GRN && invoiceID!=null) {
                        /**
                         * Getting landing cost categories for a product. 
                         */
                        Set<LandingCostCategory> lccSet = product != null ? product.getLccategoryid() : new HashSet<LandingCostCategory>();
                        double unitlandedcost=0;
                        double groqty = 0; //goodsreceipt order quantity
                        double landingcost=0;
                        
                        if (lccSet != null && !lccSet.isEmpty()) {
                            for (LandingCostCategory lcc : lccSet) {
                                String landingcostcategory = lcc.getId();
                                /**
                                 * Iterate all expense invoices and calculate
                                 * total amount in base currency.
                                 */
                                
                                KwlReturnObject pikwl = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceID);
                                GoodsReceipt consignmentgrobj = (GoodsReceipt) (pikwl.getEntityList().isEmpty() ? null : pikwl.getEntityList().get(0));

                                KwlReturnObject kwlLCObj = accGoodsReceiptDAOobj.getLandedInviceList(invoiceID, landingcostcategory);
                                List<String> expenseInvoicelist = kwlLCObj.getEntityList();
                                
                                
                                for (String expenseInvid : expenseInvoicelist) {
                                    double landingCosttax=0.0; //tax on the expense invoice excluded from inventory side
                                    double rate = 0d; //in transaction currency
                                    double unitrate = 0d;
                                    double totalamount = 0d; //total amount of all products
                                    double expenseqty = 0d;
                                    double pw = product.getProductweight();
                                    double totalWeight = 0d;
                                    double itemWgt = 0.0;
                                    double totalvalue = 0d;
                                    double manualproductamt = 0d;
                                    double manualbaseamount = 0d;
                                    double noexpenseitem = 0d;
                                    double baserate = 0;
                                    double expensecharge = 0;
                                    KwlReturnObject crresult = null;
                                   
                                    //get the specific product details from Grodetails table for unit rate of that product
//                                    KwlReturnObject grodkwl = accGoodsReceiptDAOobj.getProductDetailsFromGoodsReceipt(invoiceID, productid,detailID ,companyid);
//                                   Object[] grdo = grodkwl.getEntityList().isEmpty()?null:(Object[]) grodkwl.getEntityList().get(0);
//                                    if(grdo!=null){
//                                        groqty = Double.parseDouble(grdo[0]!=null?grdo[0].toString():"0.0");
//                                        double grdorate = Double.parseDouble(grdo[1].toString());
//                                        unitrate = authHandler.roundUnitPrice(grdorate, companyid);                                        
//                                    }
                                                                        
                                    //convert unit rate to base currency for landed cost calculation
                                    if (!StringUtil.isNullOrEmpty(currencyid) && (StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(currencyid))) {
                                         crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, unitrate, currencyid, transactionDate, grSpotRate);
                                         unitrate = authHandler.roundUnitPrice((Double) crresult.getEntityList().get(0),companyid);
                                    }
                                    totalvalue  = authHandler.roundUnitPrice(groqty*unitrate, companyid);
                                    
                                    KwlReturnObject custresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), expenseInvid);
                                    GoodsReceipt expenseInvObj = (GoodsReceipt) custresult.getEntityList().get(0);
                                    if (expenseInvObj != null) {
                                       
                                        amount = price * quantity; //price is already in base currency converted above

                                        //iterate on values that match with the expense invoice and the given landing cost category
                                        if (landingcostcategory.equalsIgnoreCase(expenseInvObj.getLandingCostCategory().getId())) {
                                            KwlReturnObject kwlnoEligiableItem = accGoodsReceiptDAOobj.getNumberEligiableItem(expenseInvObj.getID(), landingcostcategory);
                                            List noEligiableItemList = kwlnoEligiableItem.getEntityList();
                                            Iterator itrItem = noEligiableItemList.iterator();

                                            while (itrItem.hasNext()) {
                                                Object[] valueArray = (Object[]) itrItem.next();
                                                List<Object> valueObjLit = (valueArray != null) ? new ArrayList(Arrays.asList(valueArray)) : null;
                                                expenseqty = (valueObjLit.size() > 0 && valueObjLit.get(0) != null) ? (double) valueObjLit.get(0) : 0.0D;
                                                rate = (valueObjLit.size() > 1 && valueObjLit.get(1) != null) ? (double) valueObjLit.get(1) : 0.0D;
                                                itemWgt = (valueObjLit.size() > 2 && valueObjLit.get(2) != null) ? (double) valueObjLit.get(2) : 0.0D;
                                                String goodsrecId= (valueObjLit.size() > 3 && valueObjLit.get(3) != null) ? (String) valueObjLit.get(3) : "";                                       
                                                //PI Object from the eligible Pis for the expense invoice
                                                KwlReturnObject custresulttemp = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), goodsrecId);
                                                GoodsReceipt tempinvoiceObj = (GoodsReceipt) custresulttemp.getEntityList().get(0);
                                                
                                                KWLCurrency currencytemp = (KWLCurrency) tempinvoiceObj.getCurrency();
                                                String currencyIdtemp = currencytemp.getCurrencyID();
                                                Date billDateTemp = tempinvoiceObj.getJournalEntry() != null ? tempinvoiceObj.getJournalEntry().getEntryDate() : tempinvoiceObj.getFormdate();
                                                KwlReturnObject ruternBR = currencyDAO.getCurrencyToBaseAmount(requestParams, rate, currencyIdtemp, billDateTemp, tempinvoiceObj.getJournalEntry().getExternalCurrencyRate());
                                                baserate = authHandler.roundUnitPrice((Double) ruternBR.getEntityList().get(0), companyid);
                                                //total valuations to go in the map for landed cost
                                                totalamount += (expenseqty * baserate);
                                                totalWeight += (itemWgt * expenseqty);
                                                noexpenseitem += expenseqty;
                                            }
                                            //For manual category landed cost expenseinv set value directly as manually allocated by user   
                                            if (LandingCostAllocationType.getByValue(lcc.getLcallocationid()) == LandingCostAllocationType.MANUAL) {
                                                Set<GoodsReceiptDetail> grdetailset = consignmentgrobj != null ? consignmentgrobj.getRows() : null;
                                                if (grdetailset != null && !grdetailset.isEmpty()) {
                                                    for (GoodsReceiptDetail grd : grdetailset) {
                                                        String grnid = grd.getGoodsReceiptOrderDetails()!=null?grd.getGoodsReceiptOrderDetails().getGrOrder().getID():"";
                                                        //match the productid in the PI detail with the current one in row[] or match the GRN id with the current billid
                                                        if ((grd.getInventory().getProduct().getID().equalsIgnoreCase(productid) && StringUtil.isNullOrEmpty(grnid)) || (grd.getInventory().getProduct().getID().equalsIgnoreCase(productid) && grnid.equalsIgnoreCase(billid))) {
                                                            KwlReturnObject kwlreturn = accGoodsReceiptDAOobj.getManualProductCostLCC(expenseInvObj.getID(), grd.getID());
                                                            List itemList = kwlreturn.getEntityList();
                                                            Iterator itemItr = itemList.iterator();
                                                            while (itemItr.hasNext()) {
                                                                LccManualWiseProductAmount lccManualWiseProductAmount = (LccManualWiseProductAmount) itemItr.next();
                                                                manualbaseamount = lccManualWiseProductAmount.isCustomDutyAllocationType() ? lccManualWiseProductAmount.getTaxablevalueforigst() : lccManualWiseProductAmount.getAmount();
                                                                KWLCurrency currencytemp = (KWLCurrency) expenseInvObj.getCurrency();
                                                                String currencyIdtemp = currencytemp.getCurrencyID();
                                                                Date billDateTemp = expenseInvObj.getJournalEntry() != null ? expenseInvObj.getJournalEntry().getEntryDate() : expenseInvObj.getFormdate();
                                                                KwlReturnObject ruternBRExpan = currencyDAO.getCurrencyToBaseAmount(requestParams, manualbaseamount, currencyIdtemp, billDateTemp, expenseInvObj.getJournalEntry().getExternalCurrencyRate());
                                                                manualproductamt = authHandler.roundUnitPrice((Double) ruternBRExpan.getEntityList().get(0), companyid);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            //to calculate row level tax in the invoice and reduce tax amount from landed cost 
                                            if (expenseInvObj.getTax() == null) {
                                                Set<ExpenseGRDetail> expensegrset = expenseInvObj.getExpenserows();
                                                for (ExpenseGRDetail expgrd : expensegrset) {
                                                        landingCosttax += expgrd.getRowTaxAmount();
                                                    }
                                            } else if (expenseInvObj.getTax() != null) { //global level tax
                                                landingCosttax = expenseInvObj.getTaxamountinbase();
                                            }
                                            if (landingCosttax != 0.0) {
                                                String transcurrency = expenseInvObj.getCurrency()!=null?expenseInvObj.getCurrency().getCurrencyID():"";
                                                if (!transcurrency.equalsIgnoreCase(currencyid)) {
                                                    double exrate = expenseInvObj.getExchangeRateDetail().getExchangeRate();
                                                    crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, landingCosttax, currencyid, transactionDate, exrate);
                                                    landingCosttax = authHandler.roundUnitPrice((Double) crresult.getEntityList().get(0), companyid);
                                                }
                                            }
                                            
                                            Map<String, Double> allcactionMthdData = new HashMap<>();
                                            allcactionMthdData.put("totLandedCost", expenseInvObj.getInvoiceAmountInBase() - landingCosttax);
                                            allcactionMthdData.put("noEligiableItem", noexpenseitem);
                                            allcactionMthdData.put("lineItemQty", groqty);
                                            allcactionMthdData.put("valueOfItem", totalvalue);
                                            allcactionMthdData.put("eligiableItemCost", totalamount);
                                            allcactionMthdData.put("eligiableItemWgt", totalWeight);
                                            allcactionMthdData.put("itemWght", (pw * groqty));
                                            allcactionMthdData.put("manualProductAmount", manualproductamt);

                                            expensecharge = LandingCostAllocationType.getTotalLanddedCost(expenseInvObj.getLandingCostCategory().getLcallocationid(), allcactionMthdData);
                                            landingcost += authHandler.roundUnitPrice(expensecharge, companyid);
                                            
                                            //if similar landing cost category already exists then add current amount into that
                                            if (json.has("landingcostpairs")) {
                                                StringBuilder existingpair = new StringBuilder(json.getString("landingcostpairs"));
                                                existingpair.append(",").append(landingcostcategory).append(":").append(authHandler.roundUnitPrice(expensecharge, companyid));
                                                json.put("landingcostpairs", existingpair);
                                            } else {
                                                StringBuilder newpair = new StringBuilder("");
                                                newpair.append(landingcostcategory).append(":").append(authHandler.roundUnitPrice(expensecharge, companyid));
                                                json.put("landingcostpairs", newpair);
                                            }
                                        }
                                    }
                                }
                            }
                             
                            //pushing these fields into JSON which is passed to PriceValuationStack to display on stockLedger/Valuation Reports
                                    landingcost=authHandler.roundUnitPrice(landingcost, companyid);   //rounding the total landed cost
                                    unitlandedcost = authHandler.roundUnitPrice((landingcost / groqty), companyid);  //get the unit landed cost
                                    price =authHandler.roundUnitPrice((price + unitlandedcost), companyid); //add unit landed cost into the product price
                                    if (landingcost != 0.0) {
                                        json.put("avglandedcost",price);
                                    }                                  
                        }
//                        amount = stockRate * quantity;
                    }
                    /*
                    Code to check if case of Serial JSON or Product JSON
                    */
                    String isSerialJson = row[35] != null ? (String) row[35] : "";          // Check if Serial JSON or Product JSON
                    if (StringUtil.isNullOrEmptyWithTrim(isSerialJson)) {
                        isSerialJson = "F";
                    }
                    
                    String remark = "", assembledProductID = "", costCenterID = "", stockUOMID = "", memo = "";
                    if (isMaterialInOutReport) {
                        remark = row[36] != null ? (String) row[36] : "";
                        assembledProductID = row[37] != null ? (String) row[37] : "";
                        costCenterID = row[38] != null ? (String) row[38] : "";
                        stockUOMID = row[39] != null ? (String) row[39] : "";
                        memo = row[40] != null ? (String) row[40] : "";
                    } else {
                        memo = row[36] != null ? (String) row[36] : "";
                    }
                    
                    
                    json.put(Constants.companyid, companyid);
                    if (advanceSearchTransactionlist != null && !advanceSearchTransactionlist.isEmpty() && advanceSearchTransactionlist.containsKey(detailID)) {
                        /**
                         * If detail ID is present in
                         * advanceSearchTransactionlist then set
                         * isAdvanceSearchTransaction flag to true for
                         * particular transaction detail.
                         */
                        json.put(PriceValuationStack.isAdvanceSearchTransaction, true);
                        if (advanceSearchTransactionlist.get(detailID).equals("T")) {
                            isSerialJson = "T";
                        } 
                    }
                    if (isSerialForProduct && (transType.intValue() == 0 || transType.intValue() == 1 || transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4 || transType.intValue() == 6)) {
                        int transactiontype = 28;
                        if (transType.intValue() == 0 || transType.intValue() == 1 || transType.intValue() == 6) {
                            transactiontype = 28;
                        } else if (transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4) {
                            if (transType.intValue() == 2) {
                                transactiontype = 31;
                            } else if (transType.intValue() == 3 || transType.intValue() == 5) {
                                transactiontype = 27;
                            } else if (transType.intValue() == 4) {
                                transactiontype = 29;
                            }
                        }
                        storageParams.put("transactiontype", (transactiontype + ""));
                        storageParams.put("detailid", detailID);
                        
                        /*
                        Pass searchjson to fetch only serials which matches search criterion.
                        */
                        if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                            Searchjson = requestParams.get("searchJson").toString();
                        }
                        if (!StringUtil.isNullOrEmptyWithTrim(Searchjson)) {
                            reqPar1.put(Constants.companyKey, requestParams.get(Constants.companyKey));
                            reqPar1.put(Constants.Acc_Search_Json, Searchjson);
                            reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                            reqPar1.put(Constants.appendCase, "and");
                            reqPar1.put(Constants.moduleid, Constants.SerialWindow_ModuleId);
//                          reqPar1.put("removeProductCustomFilter", true);
     
                             Searchjson = accReportsService.getSearchJsonByModule(reqPar1);
                        }
                        
                        storageParams.put("searchjson", Searchjson);
                        storageParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        storageParams.put("transType", transType.intValue());
                        storageParams.put("isSerialJson", "F");
                        storageParams.put("serialNameSearch", serialNameSearch); 
                        KwlReturnObject srno = accProductObj.getSerialIdByStorageDetails(storageParams);
                        KwlReturnObject srno1 =null;
                        if (advanceSearchTransactionlist != null && !advanceSearchTransactionlist.isEmpty() && advanceSearchTransactionlist.containsKey(detailID)) {
                            storageParams.put("isSerialJson", isSerialJson);
                            srno1 = accProductObj.getSerialIdByStorageDetails(storageParams);
                        }
                        storageParams.remove("transactiontype");
                        storageParams.remove("detailid");
                        storageParams.remove("searchjson");
                        storageParams.remove("filterConjuctionCriteria");
                        storageParams.remove("transType");
                        storageParams.remove("isSerialJson");
                        List list2 = srno.getEntityList();
                        List list3 = null;
                        if (srno1 !=null && !srno1.getEntityList().isEmpty()) {
                            list3 = srno1.getEntityList();
                        }
                        if (list2 != null && !list2.isEmpty()) {
                            for (Object obj : list2) {
                                String serialID = obj.toString();
                                storageParams.put("serialName", serialID);
                                if (advanceSearchTransactionlist != null && !advanceSearchTransactionlist.isEmpty()) {
                                    if (list3 != null && !list3.isEmpty() && list3.contains(serialID)) {
                                        json.put(PriceValuationStack.isAdvanceSearchTransaction, true);
                                    } else {
                                        json.put(PriceValuationStack.isAdvanceSearchTransaction, false);
                                    }
                                }
                                pushStack(stockDateMap, isFromStockValuationDetail, isFromStockValuationSummary, transType, storageParams, detailID, valuationMethod, linkflag, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, remark, assembledProductID, costCenterID, stockUOMID, memo, json, isNegativeStock);
                            }
                        }
                    } else if (transType.intValue() == 7 || transType.intValue() == 8 || transType.intValue() == 9 || transType.intValue() == 10 || transType.intValue() == 11 || transType.intValue() == 12 || transType.intValue() == 13 || transType.intValue() == 14 || transType.intValue() == 15 || transType.intValue() == 16) {
                        if (!StringUtil.isNullOrEmptyWithTrim(serialNames)) {
                            List serialList = stockDAO.getERPSerialFromBatch(companyid, productid, warehouseId, locationId, rowId, rackId, binId, batchName, serialNames);
                            if (serialList != null && !serialList.isEmpty()) {
                                for (Object obj : serialList) {
                                    String serialID = obj.toString();
                                    if (isMaterialInOutReport || isStockLedgerDetailedReport) {
                                        /**
                                         * If Material In/Out or Stock Ledger
                                         * Detailed Report
                                         */
                                        if (StringUtil.isNullOrEmptyWithTrim(serialNameSearch) || (!StringUtil.isNullOrEmptyWithTrim(serialNameSearch) && !StringUtil.isNullOrEmptyWithTrim(serialID))) {
                                            /**
                                             * Push Transaction into stack only
                                             * if (serial name search is blank)
                                             * or (both serial name search and
                                             * serial Id is not empty).
                                             */
                                            storageParams.put("serialName", serialID);
                                            pushStack(stockDateMap, isFromStockValuationDetail, isFromStockValuationSummary, transType, storageParams, detailID, valuationMethod, linkflag, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, remark, assembledProductID, costCenterID, stockUOMID, memo, json, isNegativeStock);
                                        }
                                    } else {
                                        storageParams.put("serialName", serialID);
                                        pushStack(stockDateMap, isFromStockValuationDetail, isFromStockValuationSummary, transType, storageParams, detailID, valuationMethod, linkflag, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, remark, assembledProductID, costCenterID, stockUOMID, memo, json, isNegativeStock);
                                    }
                                }
                            }
                        } else {
                            if (isMaterialInOutReport || isStockLedgerDetailedReport) {
                                /**
                                 * If Material In/Out or Stock Ledger Detailed
                                 * Report
                                 */
                                if (StringUtil.isNullOrEmptyWithTrim(serialNameSearch) || (!StringUtil.isNullOrEmptyWithTrim(serialNameSearch) && !StringUtil.isNullOrEmptyWithTrim(""))) {
                                    /**
                                     * Push Transaction into stack only if
                                     * (serial name search is blank) or (both
                                     * serial name search and serial Id is not
                                     * empty).
                                     */
                                    storageParams.put("serialName", "");
                                    pushStack(stockDateMap, isFromStockValuationDetail, isFromStockValuationSummary, transType, storageParams, detailID, valuationMethod, linkflag, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, remark, assembledProductID, costCenterID, stockUOMID, memo, json, isNegativeStock);
                                }
                            } else {
                                storageParams.put("serialName", "");
                                pushStack(stockDateMap, isFromStockValuationDetail, isFromStockValuationSummary, transType, storageParams, detailID, valuationMethod, linkflag, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, remark, assembledProductID, costCenterID, stockUOMID, memo, json, isNegativeStock);
//                            }
                            }
                        }
                    } else {
                        /* put details related to the location/warehouse/row /rack/bin/batch/serial */
                        if (!((isMaterialInOutReport || isStockLedgerDetailedReport) && !StringUtil.isNullOrEmpty(serialNameSearch))) {
                            // For Material In/Out Report and quick search applied on serial names then do not add products for which serial is not activated.
                            pushStack(stockDateMap, isFromStockValuationDetail, isFromStockValuationSummary, transType, storageParams, detailID, valuationMethod, linkflag, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, remark, assembledProductID, costCenterID, stockUOMID, memo, json, isNegativeStock);
                        }
                    }
                }                
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void pushStack(Map<String, Map> stockDateMap, boolean isFromStockValuationDetail, boolean isFromStockValuationSummary, BigInteger transType, Map requestParams, String detailID, ValuationMethod valuationMethod, boolean linkflag, double quantity, double price, Map<String,String> storageParams, String personCode, String personName, String transactionNumber, Date transactionDate, String billid, Integer srNo, Long createdon, String remark, String assembledProductID, String costCenterID, String stockUOMID, String memo, JSONObject json, boolean isNegativeStock) throws SessionExpiredException, ParseException{
        for (String key : stockDateMap.keySet()) {
            Map stockDateObject = stockDateMap.get(key);
            Date startDate = (Date) stockDateObject.get("startdate");
            Date endDate = (Date) stockDateObject.get("enddate");
            boolean isDifferenceInOpeningBalanceFlag = stockDateObject.containsKey("isDifferenceInOpeningBalanceFlag") ? (Boolean)stockDateObject.get("isDifferenceInOpeningBalanceFlag") : false;
            if((isDifferenceInOpeningBalanceFlag || isNegativeStock) && transactionDate != null && transactionDate.after(endDate)){
                continue;
            }
            PriceValuationStack stack = (PriceValuationStack) stockDateObject.get("stack");
            boolean openingtransaction = false;
            if (!(isFromStockValuationDetail || isFromStockValuationSummary)) {
                openingtransaction = stockService.isOpeingOrPeriodTransaction(transactionDate, startDate, transType.intValue());
            }
            boolean isPeriodTransaction = false;
            if (!openingtransaction && endDate != null && transactionDate != null) {
                /* if "transaction" is not an opening transaction and enddate is not null */
                if (DateUtils.isSameDay(transactionDate, endDate) || transactionDate.before(endDate)) {
                    /* check if the transaction date <= enddate */
                    isPeriodTransaction = true;
                }
            }
            if ((requestParams.containsKey("isItemHistoryReport") && requestParams.get("isItemHistoryReport") != null) || (requestParams.containsKey("stockAgeing") && requestParams.get("stockAgeing") != null)) {
                /* For "Item History Report" consider the isPeriodTransaction as "TRUE" (All transactions need to be considered) */
                isPeriodTransaction = true;
            }
            stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
        }
    }
    
    private void calculateValuationFromStockDateMap(Map<String, Map> stockDateMap, Product product, String producttype, boolean isAdvanceSearchForValuation,JSONObject blockqtyJson) throws JSONException{
        for (String key : stockDateMap.keySet()) {
            Map stockDateObject = stockDateMap.get(key);
            boolean isDifferenceInOpeningBalanceFlag = stockDateObject.containsKey("isDifferenceInOpeningBalanceFlag") ? (Boolean) stockDateObject.get("isDifferenceInOpeningBalanceFlag") : false;
            PriceValuationStack stack = (PriceValuationStack) stockDateObject.get("stack");
            double[] valuation = (double[]) stockDateObject.get("valuation");
            blockqtyJson.put("startdate", stockDateObject.get("startdate"));
            blockqtyJson.put("enddate", stockDateObject.get("enddate"));
            valuation = getValuationValues(stack, valuation, product, producttype, isDifferenceInOpeningBalanceFlag, isAdvanceSearchForValuation,blockqtyJson);
            stockDateObject.put("valuation", valuation);
            stockDateObject.remove("stack");
        }
    }
    /**
     * Function to push All transactions using productID into the stack
     * @param list
     * @param requestParams
     * @param jsonArr
     * @param endDate
     * @param advanceSearchTransactionlist (Optional)it will contain list of
     * transaction detail ID for which advance search criteria is matching.
     * @return 
     */
    private void pushTransactionForProduct(List list, HashMap<String, Object> requestParams, Date startDate, String productid, PriceValuationStack stack, Date endDate, Map advanceSearchTransactionlist) {
        try {       
            String basecurrency = requestParams.containsKey("basecurrencyid") ? (String)requestParams.get("basecurrencyid") : null;
            boolean isFromStockValuationDetail=false;
            boolean isFromStockValuationSummary=false;
            boolean isStockLedgerDetailedReport = false;// Stock Ledger Detailed Report
            boolean isActivateMRPModule = false;
            Map<String,Object> valuationISTMap = new HashMap<>(); //Map for proper avg cost calculation during landed cost 
            if (requestParams.containsKey("isStockLedgerDetailedReport") && requestParams.get("isStockLedgerDetailedReport") != null) {
                isStockLedgerDetailedReport = Boolean.parseBoolean(requestParams.get("isStockLedgerDetailedReport").toString());
            }
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            boolean isMaterialInOutReport = false;// Material IN/OUT Report
            if (requestParams.containsKey("isMaterialInOutReport") && requestParams.get("isMaterialInOutReport") != null) {
                isMaterialInOutReport = Boolean.parseBoolean(requestParams.get("isMaterialInOutReport").toString());
            }
            String companyid=(String) requestParams.get("companyid");
          
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) extraprefresult.getEntityList().get(0);
            
            JSONObject columnpref = null;
            boolean istermactivatedforlandedcost = false;
            HashMap<String, Object> prefparams = new HashMap<>();
            prefparams.put("id", companyid);
            Object extrapref = kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class,new String[]{"activateMRPModule"}, prefparams);
            
            if (extrapref!=null && (Boolean) extrapref==true) {
                isActivateMRPModule=true;
            }
            //ERP-39781 re using the extrapref obj to get the columpref JSON from extra company pref table
            extrapref = kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"columnPref"}, prefparams);
            if (extrapref != null) {
                columnpref = new JSONObject(extrapref.toString());
                istermactivatedforlandedcost = columnpref.optBoolean(Constants.isLandedCostTermJE);
            }           
           
            boolean isActivateLandedInvAmt = false;
            if (requestParams.containsKey("isactivatelandedinvamt") && requestParams.get("isactivatelandedinvamt") != null) {
                isActivateLandedInvAmt = Boolean.parseBoolean(requestParams.get("isactivatelandedinvamt").toString());
            }
            if (requestParams.containsKey("isFromStockValuationDetail") && requestParams.get("isFromStockValuationDetail") != null) {
                isFromStockValuationDetail = Boolean.parseBoolean(requestParams.get("isFromStockValuationDetail").toString());
            }
            if (requestParams.containsKey("isFromStockValuationSummary") && requestParams.get("isFromStockValuationSummary") != null) {
                isFromStockValuationSummary = Boolean.parseBoolean(requestParams.get("isFromStockValuationSummary").toString());
            }
            String serialNameSearch = "";
            /**
             * If Quick Search is on serial names. Added only for Material
             * IN/OUT Report.
             */
            if ((isMaterialInOutReport || isStockLedgerDetailedReport) && requestParams.containsKey("serialNameSearch") && requestParams.get("serialNameSearch") != null) {
                serialNameSearch = requestParams.get("serialNameSearch").toString();
            }
            if(isActivateLandedInvAmt && isFromStockValuationDetail){
                Map<String, Object> istmap = new HashMap<>();
                istmap.put("isforlandedcost", isActivateLandedInvAmt);
                istmap.put("productid", productid);
                List<String> pickpackISTlist = accInvoiceDAOobj.getDOISTMapping(istmap);
                for (String istid : pickpackISTlist) {
                    valuationISTMap.put(istid, "");
                }
            }
            String Searchjson = "";
            HashMap<String, Object> reqPar1 = new HashMap<String, Object>();
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
                if (requestParams.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }

            if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                Searchjson = requestParams.get("searchJson").toString();
            }
            if (!StringUtil.isNullOrEmpty(Searchjson)) {
                reqPar1.put(Constants.companyKey, companyid);
                reqPar1.put(Constants.Acc_Search_Json, Searchjson);
                reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                reqPar1.put(Constants.appendCase, "and");
                reqPar1.put(Constants.moduleid, Constants.SerialWindow_ModuleId);
//                          reqPar1.put("removeProductCustomFilter", true);

                Searchjson = accReportsService.getSearchJsonByModule(reqPar1);
            }
            if (list != null && !list.isEmpty()) {
                for (Object object : list) {
                    Object[] row = (Object[]) object;
                    double quantity = (Double) row[9];
                    BigInteger transType = (BigInteger) row[0];
                    ValuationMethod valuationMethod = null;
                    boolean isSerialForProduct = false;
                    int valuationmethod = 0;
                    
                    if (isMaterialInOutReport) {
                        valuationmethod = row[43] != null ? (Integer) row[43] : 0;
                        isSerialForProduct = (row[41] != null &&  row[41].toString().equals("T")) ? true : false;
                    } else {
                        valuationmethod = row[39] != null ? (Integer) row[39] : 0;
                        isSerialForProduct = (row[37] != null &&  row[37].toString().equals("T"))  ? true : false;
                    }
                    if (valuationmethod == 0) { // STANDARD 
                        valuationMethod = ValuationMethod.STANDARD;
                    } else if (valuationmethod == 1) { // FIFO
                        valuationMethod = ValuationMethod.FIFO;
                    } else if (valuationmethod == 2) { // AVERAGE
                        valuationMethod = ValuationMethod.AVERAGE;
                    }
                    
                    String detailID = "";
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[17])) {
                        detailID = (String) row[17];
                    }
                    boolean linkflag = false;
                    if (row.length >= 30 && !StringUtil.isNullOrEmptyWithTrim((String) row[30])) {
                        linkflag = true;
                    }
                    String currencyid = "";
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[13])) {
                        currencyid = (String) row[13];
                    }
                    Date transactionDate = (Date) row[4];
                    double baseUOMRate;
                    if (row[11] instanceof BigInteger) {
                        BigInteger baseRate = (BigInteger) row[11];
                        baseUOMRate = baseRate.doubleValue();
                    } else {
                        baseUOMRate = (Double) row[11];
                    }
//                    double baseUOMRate = (Double) row[11]; // Conversion Factor
                    String invoiceID = (String) row[14];
                    double grSpotRate = 0.0;
                    double amount = 0.0;
                    
                    try {
                        if (!StringUtil.isNullOrEmptyWithTrim((String) row[16])) {
                            grSpotRate = StringUtil.getDouble((String) row[16]);
                        }
                    } catch (java.lang.ClassCastException ex) {
                        if (row[16] != null) {
                            grSpotRate = (double) row[16];
                        }
                    }
                    double price = 0;
                    quantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate, companyid);
                    if (transType.intValue() != TransactionBatch.DocType_DO) {
                        price = row[10] != null ? (Double) row[10] : 0.0;
                        int discount = 0;
                        if (row[33] instanceof BigInteger) {
                            BigInteger discountispercent = (BigInteger) row[33];
                            discount = discountispercent.intValue();
                        } else {
                            discount = (Integer) row[33];
                        }

                        String gstincluded = row[34] != null ? (String) row[34] : "";
                        if (StringUtil.isNullOrEmptyWithTrim(gstincluded)) {
                            gstincluded = "F";
                        }
                        if (gstincluded.equals("F")) {
                            /*
                             In Including GST case, discount price is already subtracted from the unit price so no need to subtract discount
                             */
                            double discountPrice;
                            if (row[32] != null && row[32] instanceof BigInteger) {
                                BigInteger discP = (BigInteger) row[32];
                                discountPrice = discP.doubleValue();
                            } else {
                                discountPrice = row[32] != null ? (Double) row[32] : 0.0;
                            }
                            if (discount != 0) {
                                discountPrice = discountPrice / 100;
                                discountPrice = discountPrice * (price * quantity);
                            }
                            if (quantity != 0) {
                                double pricePerQty = ((price * quantity) - discountPrice) / quantity;
                                price = pricePerQty;
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(currencyid) && (StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(currencyid))) {
                            KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, price, currencyid, transactionDate, grSpotRate);
                            price = (Double) crresult.getEntityList().get(0);
                        }
                        price = price / baseUOMRate;
                        if (!(pref.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                            price = Double.parseDouble(authHandler.getFormattedUnitPrice(price, companyid));
                        }
                    }
                    String billid = (String) row[15];
                    JSONObject json = new JSONObject();
                    // ERP-40156 for landed cost stock moving internally in IST contains landed cost as well 
                    // to calculate average cost column in Stock Valuation report getting the price without landed cost first
                    if (transType.intValue() == TransactionBatch.DocType_IST_COLLECT && isActivateLandedInvAmt && isFromStockValuationDetail) {
                        String collectist = billid.replace("collect", "");
                        if ((valuationISTMap.containsKey(collectist))) {
                            PriceValuationStack.Batch istoutbatch = stack.getTransactionBatch(valuationISTMap.get(collectist).toString());
                            double prerate = istoutbatch != null ? istoutbatch.getWithoutlanded() : price;
                            json.put("withoutlanded", prerate);                            
                        }
                    }
                    if (transType.intValue() == TransactionBatch.DocType_IST_ISSUE) {
                        /**
                         * If InterStoreTransfer is created with GRN QA flow or
                         * not. If IST is created for GRN QA then the price of
                         * IST should only be adjusted with GRN and not with
                         * other document(s) i.e. price of IST OUT will be same
                         * as of GRN (ERP-35843).
                         */
                        KwlReturnObject kwl = accProductObj.getRateAndExchangeRateFromGoodsReceiptOrderDetail(billid, companyid);
                        if (kwl != null && kwl.getEntityList() != null && !kwl.getEntityList().isEmpty()) {
                            List l = kwl.getEntityList();
                            if (l != null && !l.isEmpty()) {
                                Object o = l.get(0);
                                Object[] objArr = (Object[]) o;
                                if (objArr != null && objArr.length > 0) {
                                    double groRate = (double)objArr[0];
                                    double groExternalCurrencyRate = (double) objArr[1];
                                    String groCurrency = "";
                                    if (!StringUtil.isNullOrEmptyWithTrim((String) objArr[2])) {
                                        groCurrency = (String) objArr[2];
                                    }
                                    String grodID = (String) objArr[3];
                                    if (!StringUtil.isNullOrEmpty(groCurrency) && (StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(groCurrency))) {
                                        KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, groRate, groCurrency, transactionDate, groExternalCurrencyRate);
                                        price = (Double) crresult.getEntityList().get(0);
                                    } else {
                                        price = groRate;
                                    }
                                    json.put("considerGRNPrice", true);
                                    json.put("groDetailID", grodID);
                                }
                            }
                        }
                        
                       /** MRP: WORK ORDER MODULE
                                * If InterStoreTransfer is created with WorkOrder Finished goods product QA flow or
                                * not. If IST is created for WorkOrder Finished goods product QA then the price of
                                * IST should only be adjusted with Produced WorkOrder Finished goods product price and not with
                                * other document(s) i.e. price of IST OUT will be same
                                * as of Produced WorkOrder Finished goods product price
                                * Fetched WOCD initialpurchaseprice as price and stockmovementid as documentid
                                * this price will set to IST OUT
                                */
                        if (isActivateMRPModule) {
                            KwlReturnObject kwlObjectWOCD = accProductObj.getRateFromWorkOrderComponentDetail(billid, companyid);
                            if (kwlObjectWOCD != null && kwlObjectWOCD.getEntityList() != null && !kwlObjectWOCD.getEntityList().isEmpty()) {
                                List l = kwlObjectWOCD.getEntityList();
                                if (l != null && !l.isEmpty()) {
                                    Object o = l.get(0);
                                    Object[] objArr = (Object[]) o;
                                    if (objArr != null && objArr.length > 0) {
                                        double wocdRate = (double) objArr[0];

                                        String stockMovementID = (String) objArr[1];
                                        price = wocdRate;

                                        json.put("considerWODPrice", true);
                                        json.put("stockMovementID", stockMovementID);
                                    }
                                }
                            }
                        }                        
                        
                    } else if (transType.intValue() == TransactionBatch.DocType_GRN) {
                        /**
                         * Is GoodsReceiptOrder is sent to QA to not. If
                         * isGoodsReceiptOrderDetailSentToQA is true then don't
                         * adjust it with outstanding DO (ERP-35843).
                         */
                        boolean isGoodsReceiptOrderDetailSentToQA = accProductObj.isGoodsReceiptOrderDetailSentToQA(detailID, companyid);
                        if (isGoodsReceiptOrderDetailSentToQA) {
                            json.put("isGoodsReceiptOrderDetailSentToQA", isGoodsReceiptOrderDetailSentToQA);
                        }
                    } else if (transType.intValue() == TransactionBatch.DocType_WO_IN) {
                        /**
                            * Is WorkOrder Finished goods product is sent to QA or not. If
                            * isWorkOrderComponentDetailSentToQA is true
                            * then adjust it with WorkOrder Finished goods product QA IST OUT transaction.
                            */
                        KwlReturnObject stockMovementKwlObject = accountingHandlerDAOobj.getObject(StockMovement.class.getName(), detailID);
                        if (stockMovementKwlObject != null && stockMovementKwlObject.getEntityList() != null && !stockMovementKwlObject.getEntityList().isEmpty()) {
                            StockMovement stockMovementObject = (StockMovement) stockMovementKwlObject.getEntityList().get(0);
                            if (stockMovementObject != null) {
                                String wocdetailid = stockMovementObject.getModuleRefDetailId();
                                boolean isWorkOrderComponentDetailSentToQA = accProductObj.isWorkOrderComponentDetailSentToQA(wocdetailid, companyid);
                                if (isWorkOrderComponentDetailSentToQA) {
                                    json.put("isWorkOrderComponentDetailSentToQA", isWorkOrderComponentDetailSentToQA);
                                }
                            }
                        }
                    }
                    String transactionNumber = (String) row[5];
                    String personCode = "";
                    String personName = "";
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[6])) {
                        personCode = (String) row[6];
                    }
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[7])) {
                        personName = (String) row[7];
                    }
                    
                    
                    String warehouseId = null;
                    String locationId = null;
                    String rowId = null;
                    String rackId = null;
                    String binId = null;
                    String batchName = "";
                    String serialNames = "";
                    
                    boolean openingtransaction = false;
                    if (!(isFromStockValuationDetail || isFromStockValuationSummary)) {
                        openingtransaction = stockService.isOpeingOrPeriodTransaction(transactionDate, startDate, transType.intValue());
                    }
                    Map storageParams = new HashMap();
                    if (row[24] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[24])) { // Warehouse ID
                        storageParams.put("warehouseId", (String) row[24]);
                        warehouseId = (String) row[24];
                    }
                    if (row[23] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[23])) { // Location ID
                        storageParams.put("locationId", (String) row[23]);
                        locationId = (String) row[23];
                    }
                    if (row[25] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[25])) { // Row ID
                        storageParams.put("rowId", (String) row[25]);
                        rowId = (String) row[25];
                    }
                    if (row[26] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[26])) { // Rack ID
                        storageParams.put("rackId", (String) row[26]);
                        rackId = (String) row[26];
                    }
                    if (row[27] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[27])) { // BIN ID
                        storageParams.put("binId", (String) row[27]);
                        binId = (String) row[27];
                    }
//                    if (product.isIsBatchForProduct() && StringUtil.isNullOrEmpty((String) row[28])) {
//                        System.out.println(product.getProductid());
//                    }
                    if (row[28] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[28])) { // Batch Name
                        storageParams.put("batchName", (String) row[28]);
                        batchName = (String) row[28];
                    }
                    if (row[31] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[31])) {// comma separated serials from inventory modules
                        serialNames = (String) row[31];
                    }
                    boolean isPeriodTransaction = false;
                    if (!openingtransaction && endDate != null && transactionDate != null) {
                        /* if "transaction" is not an opening transaction and enddate is not null */
                        if (DateUtils.isSameDay(transactionDate, endDate) || transactionDate.before(endDate)) {
                            /* check if the transaction date <= enddate */
                            isPeriodTransaction = true;
                        }
                    }
                    if ((requestParams.containsKey("isItemHistoryReport") && requestParams.get("isItemHistoryReport") != null) || (requestParams.containsKey("stockAgeing") && requestParams.get("stockAgeing") != null)) {
                        /* For "Item History Report" consider the isPeriodTransaction as "TRUE" (All transactions need to be considered) */
                        isPeriodTransaction = true; 
                    }
                    //ERP-41772 when Landed cost enabled the stock valuation detail report should calculate average cost excluding landed cost 
                    //hence getting the IN transaction at location/warehouse of IST out Entry to set the IN price (withoutlanded) for IST transactions as well
                    if (transType.intValue() == TransactionBatch.DocType_IST_ISSUE && isActivateLandedInvAmt && isFromStockValuationDetail) {
                        PriceValuationStack.Batch intransbatch = stack.getAllTransactionBatch(null, storageParams, false, false, false);
                        if (intransbatch != null && intransbatch.getWithoutlanded() != 0.0) {
                            json.put("withoutlanded", intransbatch.getWithoutlanded());
                        }
                        valuationISTMap.put(billid, detailID);
                    }
                    if (storageParams != null && !storageParams.isEmpty() && row[29] != null) {
                        if (isSerialForProduct) {
                            quantity = 1;
                            valuationMethod = ValuationMethod.FIFO;
                        } else if (!StringUtil.isNullOrEmptyWithTrim(row[29].toString())) {
                            quantity = Double.parseDouble(row[29].toString());
//                            quantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate);
                        }
                    }
                    if (isSerialForProduct && quantity != 0) {
                        quantity = 1;
                        valuationMethod = ValuationMethod.FIFO;
                    }
                    Integer srNo = null;
                    Long createdon = null;
                    if (row[20] != null && !StringUtil.isNullOrEmptyWithTrim((row[20].toString()).trim())) {
                        srNo = Integer.parseInt(row[20].toString().trim());
                    }
                    if (row[21] != null && !StringUtil.isNullOrEmptyWithTrim(row[21].toString()) && df != null) {
                        try {
                            createdon = Long.parseLong(row[21].toString());
                        } catch (Exception ex) {
                            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                Date d = f.parse(row[21].toString());
                                createdon = d.getTime();
                            } catch (ParseException x) {
                                System.out.println("" + ex.getMessage());
                            }
                        }
                    } 
                     /**
                     * For Landed Invoice calculation.
                     */ 
                    json.put("isActivateLandedInvAmt", isActivateLandedInvAmt);
                    //for transactions like SR/GRN/DO put withoutlanded price as well into this json
                    //for IST it has been inserted above already
                    if(!json.has("withoutlanded") && isActivateLandedInvAmt){ 
                        double prerate = price;
                        json.put("withoutlanded", prerate);
                    }
                    if (isActivateLandedInvAmt && transType.intValue() == TransactionBatch.DocType_GRN) {                        
                        //If invoice ID is null then GRN could be generated through the flow PO->GRN->PI flow so check if any Invoice is still linked
                        List<Object> purchaseInvoicelist = new ArrayList<>();
                        /**
                         * Getting landing cost categories for a product. 
                         */
                        KwlReturnObject lccKwlObj = accProductObj.getLandedCostCategoriesforProduct(productid, companyid);
                        List<Object[]> lccList = lccKwlObj != null ? lccKwlObj.getEntityList() :  null;
//                        List<LandingCostCategory> lccSet = product != null ? product.getLccategoryid() : new HashSet<LandingCostCategory>();
                        double unitlandedcost=0;
                        double groqty = 0; //goodsreceipt order quantity
                        double landingcost=0;
                        boolean isMalaysiaOrSingaporeCompany = false;
                        
                        String countrycode = pref.getCompany().getCountry().getID();
                        if (countrycode.equalsIgnoreCase(String.valueOf(Constants.malaysian_country_id)) || countrycode.equalsIgnoreCase(String.valueOf(Constants.SINGAPOREID))) {
                            isMalaysiaOrSingaporeCompany = true;
                        }
                        boolean isPItoGRNFlow = StringUtil.isNullOrEmpty(invoiceID) ? false:true;  //landed cost flow 1 where PI---->Auto GRN 
                        if (lccList != null && !lccList.isEmpty()) {
                            if (StringUtil.isNullOrEmpty(invoiceID)) { //if GRN has linked Invoice and is from the flow GRN - > PI then this id will be null
                                purchaseInvoicelist = accGoodsReceiptDAOobj.getGRIDfromGROID(billid, companyid); //get PIs linked with current GRN if any
                            } else {
                                purchaseInvoicelist.add(invoiceID); // here invoice id is from the flow PI-> Auto GRN 
                            }
                            Iterator invoicers = purchaseInvoicelist.iterator(); //Iterate on Purchase Invoices linked with current GRN
                            while (invoicers.hasNext()) {
                                String purchaseinvoiceid = (String) invoicers.next();
                                for (Object[] lcc : lccList) {
                                    String landingcostcategory = (String) lcc[0];
                                /**
                                 * Iterate all expense invoices and calculate
                                 * total amount in base currency.
                                 */
                                
                                KwlReturnObject pikwl = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), purchaseinvoiceid);
                                GoodsReceipt consignmentgrobj = (GoodsReceipt) (pikwl.getEntityList().isEmpty() ? null : pikwl.getEntityList().get(0));

                                KwlReturnObject kwlLCObj = accGoodsReceiptDAOobj.getLandedInviceList(purchaseinvoiceid, landingcostcategory);
                                List<String> expenseInvoicelist = kwlLCObj.getEntityList();
                                
                                
                                for (String expenseInvid : expenseInvoicelist) {                                    
                                    double landingCosttax=0.0; //tax on the expense invoice excluded from inventory side
                                    double termtotalamount=0.0; //Expense invoice term amount
                                    double rate = 0d; //in transaction currency
                                    double unitrate = 0d;
                                    double totalamount = 0d; //total amount of all products
                                    double expenseqty = 0d;
                                    double pw = 0d;
                                    if (isMaterialInOutReport && row[45] != null) {
                                        pw =  (double) row[45];
                                    } else if (row[41] != null) {
                                        pw = (double) row[41];
                                    }
                                    double totalWeight = 0d;
                                    double itemWgt = 0.0;
                                    double totalvalue = 0d;
                                    double manualproductamt = 0d;
                                    double manualbaseamount = 0d;
                                    double noexpenseitem = 0d;
                                    double baserate = 0;
                                    double expensecharge = 0;
                                    double piexchangerate = 0;
                                    KwlReturnObject crresult = null;
                                    double productlandedcostinJE = 0; //products landed cost amount in the Landed cost JE
                                    String grdetailid = "";
                                   
                                    //get the specific product quantity/rate from invoicedetails rows to prevent mismatch issues for stock ledger/financial reports
                                    JSONObject grreqparams = new JSONObject();
                                    grreqparams.put("invoiceid", purchaseinvoiceid); // PI
                                    grreqparams.put("productid", productid);
                                    grreqparams.put("grodetailid", detailID); //GRN detail id 
                                    grreqparams.put("companyid", companyid);
                                    grreqparams.put("isPITOGRNLinking", isPItoGRNFlow); //Flow of GRN/PI linking true when PI-->AutoGRN
                                    KwlReturnObject grodkwl = accGoodsReceiptDAOobj.getProductDetailsFromGoodsReceipt(grreqparams);
                                    Object[] grdo = grodkwl.getEntityList().isEmpty()?null:(Object[]) grodkwl.getEntityList().get(0);
                                   //GRN rate and PI rate could be different hence for landed cost always refer PI rate as landed cost JE is posted based on PI rate 
                                   if (grdo != null) {
                                        groqty = Double.parseDouble(grdo[0]!=null?grdo[0].toString():"0.0");
                                        double grdorate = Double.parseDouble(grdo[1]!=null?grdo[1].toString():"0.0");
                                        unitrate = authHandler.roundUnitPrice(grdorate, companyid);
                                        piexchangerate = Double.parseDouble(grdo[2]!=null?grdo[2].toString():"0.0");
                                        grdetailid = grdo[3]!=null?grdo[3].toString():"";
                                    }
                                   
                                    /** SDP-15928(For perpetual JE case only)
                                     * For adjustment of the rounding difference we are checking if this product has the rounding value 
                                     * adjusted during JE posting.
                                     */
                                    if (pref.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
                                        HashMap<String, Object> lcdmparams = new HashMap<>();
                                        lcdmparams.put("grdetailid", grdetailid);
                                        KwlReturnObject mappingkwl = accGoodsReceiptDAOobj.getLandingCostDetailMapping(lcdmparams);
                                        Object[] mappingobj = mappingkwl.getEntityList().isEmpty() ? null : (Object[]) mappingkwl.getEntityList().get(0);
                                        String landingcostmappingid = (mappingobj != null ? mappingobj[0].toString() : " "); //landingcostmapping table id

                                        mappingkwl = accountingHandlerDAOobj.getObject(LandingCostDetailMapping.class.getName(), landingcostmappingid);
                                        LandingCostDetailMapping lcmappingobj = (LandingCostDetailMapping) (!mappingkwl.getEntityList().isEmpty() ? mappingkwl.getEntityList().get(0) : null);
                                        productlandedcostinJE = lcmappingobj != null ? lcmappingobj.getInventoryJED().getAmountinbase() : 0.0d;
                                    }
                                   
                                    //convert unit rate to base currency for landed cost calculation always refer exchange rate of PI not GRN
                                    if (!StringUtil.isNullOrEmpty(currencyid) && (StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(currencyid))) {
                                         crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, unitrate, currencyid, transactionDate, piexchangerate);
                                         unitrate = authHandler.roundUnitPrice((Double) crresult.getEntityList().get(0),companyid);
                                    }
                                    totalvalue  = authHandler.roundUnitPrice(groqty*unitrate, companyid);
                                    
                                    KwlReturnObject custresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), expenseInvid);
                                    GoodsReceipt expenseInvObj = (GoodsReceipt) custresult.getEntityList().get(0);
                                    if (expenseInvObj != null) {
                                       
                                        amount = price * quantity; //price is already in base currency converted above

                                        //iterate on values that match with the expense invoice and the given landing cost category
                                        if (landingcostcategory.equalsIgnoreCase(expenseInvObj.getLandingCostCategory().getId())) {
                                            KwlReturnObject kwlnoEligiableItem = accGoodsReceiptDAOobj.getNumberEligiableItem(expenseInvObj.getID(), landingcostcategory);
                                            List noEligiableItemList = kwlnoEligiableItem.getEntityList();
                                            Iterator itrItem = noEligiableItemList.iterator();

                                            while (itrItem.hasNext()) {
                                                Object[] valueArray = (Object[]) itrItem.next();
                                                List<Object> valueObjLit = (valueArray != null) ? new ArrayList(Arrays.asList(valueArray)) : null;
                                                expenseqty = (valueObjLit.size() > 0 && valueObjLit.get(0) != null) ? (double) valueObjLit.get(0) : 0.0D;
                                                rate = (valueObjLit.size() > 1 && valueObjLit.get(1) != null) ? (double) valueObjLit.get(1) : 0.0D;
                                                itemWgt = (valueObjLit.size() > 2 && valueObjLit.get(2) != null) ? (double) valueObjLit.get(2) : 0.0D;
                                                String goodsrecId= (valueObjLit.size() > 3 && valueObjLit.get(3) != null) ? (String) valueObjLit.get(3) : "";                                       
                                                //PI Object from the eligible Pis for the expense invoice
                                                KwlReturnObject custresulttemp = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), goodsrecId);
                                                GoodsReceipt tempinvoiceObj = (GoodsReceipt) custresulttemp.getEntityList().get(0);
                                                
                                                KWLCurrency currencytemp = (KWLCurrency) tempinvoiceObj.getCurrency();
                                                String currencyIdtemp = currencytemp.getCurrencyID();
                                                Date billDateTemp = tempinvoiceObj.getJournalEntry() != null ? tempinvoiceObj.getJournalEntry().getEntryDate() : tempinvoiceObj.getFormdate();
                                                KwlReturnObject ruternBR = currencyDAO.getCurrencyToBaseAmount(requestParams, rate, currencyIdtemp, billDateTemp, tempinvoiceObj.getJournalEntry().getExternalCurrencyRate());
                                                baserate = authHandler.roundUnitPrice((Double) ruternBR.getEntityList().get(0), companyid);
                                                //total valuations to go in the map for landed cost
                                                totalamount += (expenseqty * baserate);
                                                totalWeight += (itemWgt * expenseqty);
                                                noexpenseitem += expenseqty;
                                            }
                                            //For manual category landed cost expenseinv set value directly as manually allocated by user   
                                            if (LandingCostAllocationType.getByValue((int) lcc[1]) == LandingCostAllocationType.MANUAL || LandingCostAllocationType.getByValue((int) lcc[1]) == LandingCostAllocationType.CUSTOMDUTY) {
                                                Set<GoodsReceiptDetail> grdetailset = consignmentgrobj != null ? consignmentgrobj.getRows() : null;
                                                if (grdetailset != null && !grdetailset.isEmpty()) {
                                                    for (GoodsReceiptDetail grd : grdetailset) {
                                                        String grnid = grd.getGoodsReceiptOrderDetails()!=null?grd.getGoodsReceiptOrderDetails().getGrOrder().getID():"";
                                                        //match the productid in the PI detail with the current one in row[] or match the GRN id with the current billid
                                                        if ((grd.getInventory().getProduct().getID().equalsIgnoreCase(productid) && StringUtil.isNullOrEmpty(grnid)) || (grd.getInventory().getProduct().getID().equalsIgnoreCase(productid) && grnid.equalsIgnoreCase(billid))) {
                                                            KwlReturnObject kwlreturn = accGoodsReceiptDAOobj.getManualProductCostLCC(expenseInvObj.getID(), grd.getID());
                                                            List itemList = kwlreturn.getEntityList();
                                                            Iterator itemItr = itemList.iterator();
                                                            while (itemItr.hasNext()) {
                                                                LccManualWiseProductAmount lccManualWiseProductAmount = (LccManualWiseProductAmount) itemItr.next();
                                                                manualbaseamount = lccManualWiseProductAmount.isCustomDutyAllocationType() ? lccManualWiseProductAmount.getTaxablevalueforigst() : lccManualWiseProductAmount.getAmount();
                                                                KWLCurrency currencytemp = (KWLCurrency) expenseInvObj.getCurrency();
                                                                String currencyIdtemp = currencytemp.getCurrencyID();
                                                                Date billDateTemp = expenseInvObj.getJournalEntry() != null ? expenseInvObj.getJournalEntry().getEntryDate() : expenseInvObj.getFormdate();
                                                                KwlReturnObject ruternBRExpan = currencyDAO.getCurrencyToBaseAmount(requestParams, manualbaseamount, currencyIdtemp, billDateTemp, expenseInvObj.getJournalEntry().getExternalCurrencyRate());
                                                                manualproductamt = authHandler.roundUnitPrice((Double) ruternBRExpan.getEntityList().get(0), companyid);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            //ERM-971 landed cost tax level check
                                            //to calculate row level tax in the invoice and reduce tax amount from landed cost 
                                             if (expenseInvObj.getTax() == null) {
                                                Set<ExpenseGRDetail> expensegrset = expenseInvObj.getExpenserows();
                                                for (ExpenseGRDetail expgrd : expensegrset) {
                                                    //ERM-971 landed cost tax level check
                                                    if (expgrd.getTax() != null) {
                                                        //tax of debit type expense accounts
                                                        if ((!isMalaysiaOrSingaporeCompany || (isMalaysiaOrSingaporeCompany && !expgrd.getTax().isInputCredit()))) {
                                                            if (expgrd.isIsdebit()) { //tax of debit type expense accounts
                                                                landingCosttax += expgrd.getRowTaxAmount();
                                                            } else if (isMalaysiaOrSingaporeCompany && !expgrd.isIsdebit()) {//tax of credit type expense accounts 
                                                                landingCosttax -= expgrd.getRowTaxAmount();
                                                            }
                                                        }
                                                    }
                                              }
                                            } else if (expenseInvObj.getTax() != null && (!isMalaysiaOrSingaporeCompany || (isMalaysiaOrSingaporeCompany && !expenseInvObj.getTax().isInputCredit()))){   //global level tax
                                                landingCosttax = expenseInvObj.getTaxamountinbase();
                                            }
                                            if (landingCosttax != 0.0) {
                                                String transcurrency = expenseInvObj.getCurrency()!=null?expenseInvObj.getCurrency().getCurrencyID():"";
                                                if (!transcurrency.equalsIgnoreCase(currencyid)) {
                                                    double exrate = expenseInvObj.getExchangeRateDetail().getExchangeRate();
                                                    crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, landingCosttax, currencyid, transactionDate, exrate);
                                                    landingCosttax = authHandler.roundUnitPrice((Double) crresult.getEntityList().get(0), companyid);
                                                }
                                            }
                                            //Get amount from Invoice Terms of Expense Invoice and exclude this from landed cost depending on the check in company preferences
                                            if (!istermactivatedforlandedcost) {
                                                HashMap<String, Object> termParams = new HashMap();
                                                termParams.put("invoiceid", expenseInvObj.getID());
                                                KwlReturnObject invoicetermkwl = accGoodsReceiptDAOobj.getInvoiceTermMap(termParams);
                                                List<ReceiptTermsMap> invoicetermlist = invoicetermkwl != null ? invoicetermkwl.getEntityList() : null;
                                                for (ReceiptTermsMap termmap : invoicetermlist) {
                                                    if (termmap != null) {
                                                        termtotalamount += termmap.getTermamount();
                                                    }
                                                }
                                                termtotalamount = (termtotalamount > 0 ? 0 : termtotalamount);
                                            }
                                            
                                            Map<String, Double> allcactionMthdData = new HashMap<>();
                                            allcactionMthdData.put("totLandedCost", expenseInvObj.getInvoiceAmountInBase() - landingCosttax - termtotalamount);
                                            allcactionMthdData.put("noEligiableItem", noexpenseitem);
                                            allcactionMthdData.put("lineItemQty", groqty);
                                            allcactionMthdData.put("valueOfItem", totalvalue);
                                            allcactionMthdData.put("eligiableItemCost", totalamount);
                                            allcactionMthdData.put("eligiableItemWgt", totalWeight);
                                            allcactionMthdData.put("itemWght", (pw * groqty));
                                            allcactionMthdData.put("manualProductAmount", manualproductamt);

                                            expensecharge = LandingCostAllocationType.getTotalLanddedCost(expenseInvObj.getLandingCostCategory().getLcallocationid(), allcactionMthdData);
                                            
                                             /**
                                             * SDP-15928(Perpetual Only) Check in the landed cost JE if the posted amount is the same 
                                             * as the one processed here to check if this is the product where rounding difference has been adjusted.
                                             */
                                            if (pref.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD && productlandedcostinJE!=0.0) {
                                                double roundedlc = authHandler.round(expensecharge, companyid);
                                                if (productlandedcostinJE != roundedlc) {
                                                    double roundingdiff = Math.abs(productlandedcostinJE - roundedlc);
                                                    expensecharge += roundingdiff;
                                                }
                                            }
                                            
                                            landingcost += authHandler.roundUnitPrice(expensecharge, companyid);
                                            
                                                //if similar landing cost category already exists then add current amount into that
                                                if (json.has("landingcostpairs")) {
                                                    StringBuilder existingpair = new StringBuilder(json.getString("landingcostpairs"));
                                                    existingpair.append(",").append(landingcostcategory).append(":").append(authHandler.round(expensecharge, companyid));
                                                    json.put("landingcostpairs", existingpair);
                                                } else {
                                                    StringBuilder newpair = new StringBuilder("");
                                                    newpair.append(landingcostcategory).append(":").append(authHandler.round(expensecharge, companyid));
                                                    json.put("landingcostpairs", newpair);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                             
                                    //pushing these fields into JSON which is passed to PriceValuationStack to display on stockLedger/Valuation Reports
                                    double grnqty = (Double) row[9]; //GRN total quantity
                                    unitlandedcost = (landingcost / grnqty);  //get the unit landed cost withoud rounding any value to avoid precision loss
                                    price =(price + unitlandedcost); //add unit landed cost into the product price
                                    if (landingcost != 0.0) {
                                        json.put("avglandedcost",price);
                            }
                        }
//                        amount = stockRate * quantity;
                    }
                    /*
                    Code to check if case of Serial JSON or Product JSON
                    */
                    String isSerialJson = row[35] != null ? (String) row[35] : "";          // Check if Serial JSON or Product JSON
                    if (StringUtil.isNullOrEmptyWithTrim(isSerialJson)) {
                        isSerialJson = "F";
                    }
                    
                    String remark = "", assembledProductID = "", costCenterID = "", stockUOMID = "", memo = "";
                    if (isMaterialInOutReport) {
                        remark = row[36] != null ? (String) row[36] : "";
                        assembledProductID = row[37] != null ? (String) row[37] : "";
                        costCenterID = row[38] != null ? (String) row[38] : "";
                        stockUOMID = row[39] != null ? (String) row[39] : "";
                        memo = row[40] != null ? (String) row[40] : "";
                    } else {
                        memo = row[36] != null ? (String) row[36] : "";
                    }
                    
                    
                    json.put(Constants.companyid, companyid);
                    if (advanceSearchTransactionlist != null && !advanceSearchTransactionlist.isEmpty() && advanceSearchTransactionlist.containsKey(detailID)) {
                        /**
                         * If detail ID is present in
                         * advanceSearchTransactionlist then set
                         * isAdvanceSearchTransaction flag to true for
                         * particular transaction detail.
                         */
                        json.put(PriceValuationStack.isAdvanceSearchTransaction, true);
                        if (advanceSearchTransactionlist.get(detailID).equals("T")) {
                            isSerialJson = "T";
                        } 
                    }
                    
                    if (isSerialForProduct && (transType.intValue() == 0 || transType.intValue() == 1 || transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4 || transType.intValue() == 6)) {
                        int transactiontype = 28;
                        if (transType.intValue() == 0 || transType.intValue() == 1 || transType.intValue() == 6) {
                            transactiontype = 28;
                        } else if (transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4) {
                            if (transType.intValue() == 2) {
                                transactiontype = 31;
                            } else if (transType.intValue() == 3 || transType.intValue() == 5) {
                                transactiontype = 27;
                            } else if (transType.intValue() == 4) {
                                transactiontype = 29;
                            }
                        }
                        storageParams.put("transactiontype", (transactiontype + ""));
                        storageParams.put("detailid", detailID);
                        
                        /*
                        Pass searchjson to fetch only serials which matches search criterion.
                        */
                        if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                            Searchjson = requestParams.get("searchJson").toString();
                        }
                        if (!StringUtil.isNullOrEmptyWithTrim(Searchjson)) {
                            reqPar1.put(Constants.companyKey, requestParams.get(Constants.companyKey));
                            reqPar1.put(Constants.Acc_Search_Json, Searchjson);
                            reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                            reqPar1.put(Constants.appendCase, "and");
                            reqPar1.put(Constants.moduleid, Constants.SerialWindow_ModuleId);
//                          reqPar1.put("removeProductCustomFilter", true);
     
                             Searchjson = accReportsService.getSearchJsonByModule(reqPar1);
                        }
                        
                        storageParams.put("searchjson", Searchjson);
                        storageParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        storageParams.put("transType", transType.intValue());
                        storageParams.put("isSerialJson", "F");
                        storageParams.put("serialNameSearch", serialNameSearch); 
                        KwlReturnObject srno = accProductObj.getSerialIdByStorageDetails(storageParams);
                        KwlReturnObject srno1 =null;
                        if (advanceSearchTransactionlist != null && !advanceSearchTransactionlist.isEmpty() && advanceSearchTransactionlist.containsKey(detailID)) {
                            storageParams.put("isSerialJson", isSerialJson);
                            srno1 = accProductObj.getSerialIdByStorageDetails(storageParams);
                        }
                        storageParams.remove("transactiontype");
                        storageParams.remove("detailid");
                        storageParams.remove("searchjson");
                        storageParams.remove("filterConjuctionCriteria");
                        storageParams.remove("transType");
                        storageParams.remove("isSerialJson");
                        List list2 = srno.getEntityList();
                        List list3 = null;
                        if (srno1 !=null && !srno1.getEntityList().isEmpty()) {
                            list3 = srno1.getEntityList();
                        }
                        if (list2 != null && !list2.isEmpty()) {
                            for (Object obj : list2) {
                                String serialID = obj.toString();
                                storageParams.put("serialName", serialID);
                                if (advanceSearchTransactionlist != null && !advanceSearchTransactionlist.isEmpty()) {
                                    if (list3 != null && !list3.isEmpty() && list3.contains(serialID)) {
                                        json.put(PriceValuationStack.isAdvanceSearchTransaction, true);
                                    } else {
                                        json.put(PriceValuationStack.isAdvanceSearchTransaction, false);
                                    }
                                }
                                stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                            }
                        }
                    } else if (transType.intValue() == 7 || transType.intValue() == 8 || transType.intValue() == 9 || transType.intValue() == 10 || transType.intValue() == 11 || transType.intValue() == 12 || transType.intValue() == 13 || transType.intValue() == 14 || transType.intValue() == 15 || transType.intValue() == 16) {
                        if (!StringUtil.isNullOrEmptyWithTrim(serialNames)) {
                            List serialList = stockDAO.getERPSerialFromBatch(companyid, productid, warehouseId, locationId, rowId, rackId, binId, batchName, serialNames);
                            if (serialList != null && !serialList.isEmpty()) {
                                for (Object obj : serialList) {
                                    String serialID = obj.toString();
                                    if (isMaterialInOutReport || isStockLedgerDetailedReport) {
                                        /**
                                         * If Material In/Out or Stock Ledger
                                         * Detailed Report
                                         */
                                        if (StringUtil.isNullOrEmptyWithTrim(serialNameSearch) || (!StringUtil.isNullOrEmptyWithTrim(serialNameSearch) && !StringUtil.isNullOrEmptyWithTrim(serialID))) {
                                            /**
                                             * Push Transaction into stack only
                                             * if (serial name search is blank)
                                             * or (both serial name search and
                                             * serial Id is not empty).
                                             */
                                            storageParams.put("serialName", serialID);
                                            stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                                        }
                                    } else {
                                        storageParams.put("serialName", serialID);
                                        stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                                    }
                                }
                            }
                        } else {
                            if (isMaterialInOutReport || isStockLedgerDetailedReport) {
                                /**
                                 * If Material In/Out or Stock Ledger Detailed
                                 * Report
                                 */
                                if (StringUtil.isNullOrEmptyWithTrim(serialNameSearch) || (!StringUtil.isNullOrEmptyWithTrim(serialNameSearch) && !StringUtil.isNullOrEmptyWithTrim(""))) {
                                    /**
                                     * Push Transaction into stack only if
                                     * (serial name search is blank) or (both
                                     * serial name search and serial Id is not
                                     * empty).
                                     */
                                    storageParams.put("serialName", "");
                                    stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                                }
                            } else {
                                storageParams.put("serialName", "");
                                stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
//                            }
                            }
                        }
                    } else {
                        /* put details related to the location/warehouse/row /rack/bin/batch/serial */
                        if (!((isMaterialInOutReport || isStockLedgerDetailedReport) && !StringUtil.isNullOrEmpty(serialNameSearch))) {
                            // For Material In/Out Report and quick search applied on serial names then do not add products for which serial is not activated.
                            stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Function to push All transactions using productID into the stack
     * @param list
     * @param requestParams
     * @param jsonArr
     * @param endDate
     * @param advanceSearchTransactionlist (Optional)it will contain list of
     * transaction detail ID for which advance search criteria is matching.
     * @return 
     */
    private void pushTransactionForProduct(List list, HashMap<String, Object> requestParams, Date startDate, JSONObject productjson, String productid, PriceValuationStack stack, Date endDate, Map advanceSearchTransactionlist) {
        try {       
            String basecurrency = requestParams.containsKey("basecurrencyid") ? (String)requestParams.get("basecurrencyid") : null;
            boolean isFromStockValuationDetail=false;
            boolean isFromStockValuationSummary=false;
            boolean isStockLedgerDetailedReport = false;// Stock Ledger Detailed Report
            if (requestParams.containsKey("isStockLedgerDetailedReport") && requestParams.get("isStockLedgerDetailedReport") != null) {
                isStockLedgerDetailedReport = Boolean.parseBoolean(requestParams.get("isStockLedgerDetailedReport").toString());
            }
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            boolean isMaterialInOutReport = false;// Material IN/OUT Report
            if (requestParams.containsKey("isMaterialInOutReport") && requestParams.get("isMaterialInOutReport") != null) {
                isMaterialInOutReport = Boolean.parseBoolean(requestParams.get("isMaterialInOutReport").toString());
            }
            String companyid=(String) requestParams.get("companyid");
            boolean isActivateLandedInvAmt = false;
            if (requestParams.containsKey("isactivatelandedinvamt") && requestParams.get("isactivatelandedinvamt") != null) {
                isActivateLandedInvAmt = Boolean.parseBoolean(requestParams.get("isactivatelandedinvamt").toString());
            }
            if (requestParams.containsKey("isFromStockValuationDetail") && requestParams.get("isFromStockValuationDetail") != null) {
                isFromStockValuationDetail = Boolean.parseBoolean(requestParams.get("isFromStockValuationDetail").toString());
            }
            if (requestParams.containsKey("isFromStockValuationSummary") && requestParams.get("isFromStockValuationSummary") != null) {
                isFromStockValuationSummary = Boolean.parseBoolean(requestParams.get("isFromStockValuationSummary").toString());
            }
            String serialNameSearch = "";
            /**
             * If Quick Search is on serial names. Added only for Material
             * IN/OUT Report.
             */
            if ((isMaterialInOutReport || isStockLedgerDetailedReport) && requestParams.containsKey("serialNameSearch") && requestParams.get("serialNameSearch") != null) {
                serialNameSearch = requestParams.get("serialNameSearch").toString();
            }
            String Searchjson = "";
            HashMap<String, Object> reqPar1 = new HashMap<String, Object>();
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
                if (requestParams.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
             
            if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                Searchjson = requestParams.get("searchJson").toString();
            }
            if (!StringUtil.isNullOrEmpty(Searchjson)) {
                reqPar1.put(Constants.companyKey, companyid);
                reqPar1.put(Constants.Acc_Search_Json, Searchjson);
                reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                reqPar1.put(Constants.appendCase, "and");
                reqPar1.put(Constants.moduleid, Constants.SerialWindow_ModuleId);
//                          reqPar1.put("removeProductCustomFilter", true);

                Searchjson = accReportsService.getSearchJsonByModule(reqPar1);
            }
            if (list != null && !list.isEmpty()) {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Object[] row = null;
                double quantity = 0;
                BigInteger transType = new BigInteger("0");
                ValuationMethod valuationMethod = null;
                boolean isSerialForProduct = false;
                int valuationmethod = 0;
                for (Object object : list) {
                    row = (Object[]) object;
                    quantity = (Double) row[9];
                    transType = (BigInteger) row[0];
                    valuationMethod = null;
                    isSerialForProduct = false;
                    valuationmethod = 0;
                    
                    if (productjson != null) {
                        valuationMethod = ValuationMethod.getValue(productjson.getInt("valuationmethod"));
                        isSerialForProduct = productjson.optBoolean("isSerialForProduct");
                        productid = productjson.getString("id");
                    } else {
                        if (isMaterialInOutReport) {
                            valuationmethod = row[43] != null ? (Integer) row[43] : 0;
                            isSerialForProduct = (row[41] != null &&  row[41].toString().equals("T")) ? true : false;
                        } else {
                            valuationmethod = row[39] != null ? (Integer) row[39] : 0;
                            isSerialForProduct = (row[37] != null &&  row[37].toString().equals("T"))  ? true : false;

                        }
                        if (valuationmethod == 0) { // STANDARD 
                            valuationMethod = ValuationMethod.STANDARD;
                        } else if (valuationmethod == 1) { // FIFO
                            valuationMethod = ValuationMethod.FIFO;
                        } else if (valuationmethod == 2) { // AVERAGE
                            valuationMethod = ValuationMethod.AVERAGE;
                        }
                    }
                    
                    String detailID = "";
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[17])) {
                        detailID = (String) row[17];
                    }
                    boolean linkflag = false;
                    if (row.length >= 30 && !StringUtil.isNullOrEmptyWithTrim((String) row[30])) {
                        linkflag = true;
                    }
                    String currencyid = "";
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[13])) {
                        currencyid = (String) row[13];
                    }
                    Date transactionDate = (Date) row[4];
                    double baseUOMRate;
                    if (row[11] instanceof BigInteger) {
                        BigInteger baseRate = (BigInteger) row[11];
                        baseUOMRate = baseRate.doubleValue();
                    } else {
                        baseUOMRate = (Double) row[11];
                    }
//                    double baseUOMRate = (Double) row[11]; // Conversion Factor
                    String invoiceID = (String) row[14];
                    double grSpotRate = 0.0;
                    double amount = 0.0;
                    
                    try {
                        if (!StringUtil.isNullOrEmptyWithTrim((String) row[16])) {
                            grSpotRate = StringUtil.getDouble((String) row[16]);
                        }
                    } catch (java.lang.ClassCastException ex) {
                        if (row[16] != null) {
                            grSpotRate = (double) row[16];
                        }
                    }
                    double price = 0;
                    quantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate, companyid);
                    if (transType.intValue() != TransactionBatch.DocType_DO) {
                        price = row[10] != null ? (Double) row[10] : 0.0;
                        int discount = 0;
                        if (row[33] instanceof BigInteger) {
                            BigInteger discountispercent = (BigInteger) row[33];
                            discount = discountispercent.intValue();
                        } else {
                            discount = (Integer) row[33];
                        }

                        String gstincluded = row[34] != null ? (String) row[34] : "";
                        if (StringUtil.isNullOrEmptyWithTrim(gstincluded)) {
                            gstincluded = "F";
                        }
                        if (gstincluded.equals("F")) { 
                            /*
                             In Including GST case, discount price is already subtracted from the unit price so no need to subtract discount
                             */
                            double discountPrice;
                            if (row[32] != null && row[32] instanceof BigInteger) {
                                BigInteger discP = (BigInteger) row[32];
                                discountPrice = discP.doubleValue();
                            } else {
                                discountPrice = row[32] != null ? (Double) row[32] : 0.0;
                            }
                            if (discount != 0) {
                                discountPrice = discountPrice / 100;
                                discountPrice = discountPrice * (price * quantity);
                            }
                            if (quantity != 0) {
                                double pricePerQty = ((price * quantity) - discountPrice) / quantity;
                                price = pricePerQty;
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(currencyid) && (StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(currencyid))) {
                            KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, price, currencyid, transactionDate, grSpotRate);
                            price = (Double) crresult.getEntityList().get(0);
                        }
                        price = price / baseUOMRate;
                        price = Double.parseDouble(authHandler.getFormattedUnitPrice(price, companyid));
                    }
                    String transactionNumber = (String) row[5];
                    String billid = (String) row[15];
                    JSONObject json = new JSONObject();
                    if (transType.intValue() == TransactionBatch.DocType_IST_ISSUE) {
                        /**
                         * If InterStoreTransfer is created with GRN QA flow or
                         * not. If IST is created for GRN QA then the price of
                         * IST should only be adjusted with GRN and not with
                         * other document(s) i.e. price of IST OUT will be same
                         * as of GRN (ERP-35843).
                         */
                        KwlReturnObject kwl = accProductObj.getRateAndExchangeRateFromGoodsReceiptOrderDetail(billid, companyid);
                        if (kwl != null && kwl.getEntityList() != null && !kwl.getEntityList().isEmpty()) {
                            List l = kwl.getEntityList();
                            if (l != null && !l.isEmpty()) {
                                Object o = l.get(0);
                                Object[] objArr = (Object[]) o;
                                if (objArr != null && objArr.length > 0) {
                                    double groRate = (double) objArr[0];
                                    double groExternalCurrencyRate = (double) objArr[1];
                                    String groCurrency = "";
                                    if (!StringUtil.isNullOrEmptyWithTrim((String) objArr[2])) {
                                        groCurrency = (String) objArr[2];
                                    }
                                    String grodID = (String) objArr[3];
                                    if (!StringUtil.isNullOrEmpty(groCurrency) && (StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(groCurrency))) {
                                        KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, groRate, groCurrency, transactionDate, groExternalCurrencyRate);
                                        price = (Double) crresult.getEntityList().get(0);
                                    } else {
                                        price = groRate;
                                    }
                                    json.put("considerGRNPrice", true);
                                    json.put("groDetailID", grodID);
                                }
                            }
                        }
                    } else if (transType.intValue() == TransactionBatch.DocType_GRN) {
                        /**
                         * Is GoodsReceiptOrder is sent to QA to not. If
                         * isGoodsReceiptOrderDetailSentToQA is true then don't
                         * adjust it with outstanding DO (ERP-35843).
                         */
                        boolean isGoodsReceiptOrderDetailSentToQA = accProductObj.isGoodsReceiptOrderDetailSentToQA(detailID, companyid);
                        if (isGoodsReceiptOrderDetailSentToQA) {
                            json.put("isGoodsReceiptOrderDetailSentToQA", isGoodsReceiptOrderDetailSentToQA);
                        }
                    }
                    String personCode = "";
                    String personName = "";
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[6])) {
                        personCode = (String) row[6];
                    }
                    if (!StringUtil.isNullOrEmptyWithTrim((String) row[7])) {
                        personName = (String) row[7];
                    }
                    
                    String warehouseId = null;
                    String locationId = null;
                    String rowId = null;
                    String rackId = null;
                    String binId = null;
                    String batchName = "";
                    String serialNames = "";
                    
                    boolean openingtransaction = false;
                    if (!(isFromStockValuationDetail || isFromStockValuationSummary)) {
                        openingtransaction = stockService.isOpeingOrPeriodTransaction(transactionDate, startDate, transType.intValue());
                    }
                    Map storageParams = new HashMap();
                    if (row[24] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[24])) { // Warehouse ID
                        storageParams.put("warehouseId", (String) row[24]);
                        warehouseId = (String) row[24];
                    }
                    if (row[23] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[23])) { // Location ID
                        storageParams.put("locationId", (String) row[23]);
                        locationId = (String) row[23];
                    }
                    if (row[25] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[25])) { // Row ID
                        storageParams.put("rowId", (String) row[25]);
                        rowId = (String) row[25];
                    }
                    if (row[26] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[26])) { // Rack ID
                        storageParams.put("rackId", (String) row[26]);
                        rackId = (String) row[26];
                    }
                    if (row[27] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[27])) { // BIN ID
                        storageParams.put("binId", (String) row[27]);
                        binId = (String) row[27];
                    }
//                    if (product.isIsBatchForProduct() && StringUtil.isNullOrEmpty((String) row[28])) {
//                        System.out.println(product.getProductid());
//                    }
                    if (row[28] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[28])) { // Batch Name
                        storageParams.put("batchName", (String) row[28]);
                        batchName = (String) row[28];
                    }
                    if (row[31] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[31])) {// comma separated serials from inventory modules
                        serialNames = (String) row[31];
                    }
                    boolean isPeriodTransaction = false;
                    if (!openingtransaction && endDate != null && transactionDate != null) {
                        /* if "transaction" is not an opening transaction and enddate is not null */
                        if (DateUtils.isSameDay(transactionDate, endDate) || transactionDate.before(endDate)) {
                            /* check if the transaction date <= enddate */
                            isPeriodTransaction = true;
                        }
                    }
                    if ((requestParams.containsKey("isItemHistoryReport") && requestParams.get("isItemHistoryReport") != null) || (requestParams.containsKey("stockAgeing") && requestParams.get("stockAgeing") != null)) {
                        /* For "Item History Report" consider the isPeriodTransaction as "TRUE" (All transactions need to be considered) */
                        isPeriodTransaction = true; 
                    }
                    if (storageParams != null && !storageParams.isEmpty() && row[29] != null) {
                        if (isSerialForProduct) {
                            quantity = 1;
                            valuationMethod = ValuationMethod.FIFO;
                        } else if (!StringUtil.isNullOrEmptyWithTrim(row[29].toString())) {
                            quantity = Double.parseDouble(row[29].toString());
//                            quantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate);
                        }
                    }
                    if (isSerialForProduct && quantity != 0) {
                        quantity = 1;
                        valuationMethod = ValuationMethod.FIFO;
                    }
                    Integer srNo = null;
                    Long createdon = null;
                    if (row[20] != null && !StringUtil.isNullOrEmptyWithTrim((row[20].toString()).trim())) {
                        srNo = Integer.parseInt(row[20].toString().trim());
                    }
                    if (row[21] != null && !StringUtil.isNullOrEmptyWithTrim(row[21].toString()) && df != null && (transType.intValue() != TransactionBatch.DocType_IST_COLLECT||transType.intValue() != TransactionBatch.DocType_IST_ISSUE)) {
                        try {
                            createdon = Long.parseLong(row[21].toString());

                        } catch (Exception e) {
                            try {
                                Date d = f.parse(row[21].toString());
                                createdon = d.getTime();
                            } catch (ParseException ex) {
                                System.out.println(""+ex.getMessage());
                            }
                        }

                    }
                    /**
                     * Get All Expense Invoice ID Where invoiceID is linked as
                     * Landed invoice.
                     */
                    
                    if (isActivateLandedInvAmt) {
                        List expenseInvoiceIds = accProductObj.getConsignmentInvoice(invoiceID, companyid);
                        if (expenseInvoiceIds != null && !expenseInvoiceIds.isEmpty()) {
                            double expenseVendorInvoiceTotalAmountInBase = 0.0;
                            double consignmentInvoiceTotalAmountInBase = 0.0;
                            price = row[10] != null ? (Double) row[10] : 0.0;
                            /**
                             * Iterate all expense invoices and calculate total
                             * amount in base currency.
                             */
                            for (Object o : expenseInvoiceIds) {
                                String expenseInvoice = (String) o;
                                List invoiceTotalAmountDetail = accProductObj.getVendorInvoiceTotalAmountDetail(expenseInvoice, personName, companyid);
                                Iterator it = invoiceTotalAmountDetail.iterator();
                                while (it.hasNext()) {
                                    Object[] itrow = (Object[]) it.next();
                                    double vendorInvoiceTotalAmount = (Double) itrow[0];
                                    String fromcurrencyid = (String) itrow[1];
                                    Date entryDate = (Date) itrow[2];
                                    double externalcurrencyrate = (Double) itrow[3];
                                    Date expInvoiceCreationDate = (Date) itrow[4];

//                                    KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, vendorInvoiceTotalAmount, fromcurrencyid, entryDate, externalcurrencyrate);
                                    KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, vendorInvoiceTotalAmount, fromcurrencyid, expInvoiceCreationDate, externalcurrencyrate);
                                    expenseVendorInvoiceTotalAmountInBase += (Double) crresult.getEntityList().get(0);
                                }
                            }

                            List consignmentInvoiceTotalAmountDetail = accProductObj.getVendorInvoiceTotalAmountDetail(invoiceID, personName, companyid);
                            Iterator it = consignmentInvoiceTotalAmountDetail.iterator();
                            while (it.hasNext()) {
                                Object[] itrow = (Object[]) it.next();
                                double consignmentInvoiceTotalAmount = (Double) itrow[0];
                                String fromcurrencyid = (String) itrow[1];
                                Date entryDate = (Date) itrow[2];
                                double externalcurrencyrate = (Double) itrow[3];
                                Date consigInvoiceCreationDate = (Date) itrow[4];

//                                KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, consignmentInvoiceTotalAmount, fromcurrencyid, entryDate, externalcurrencyrate);
                                KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, consignmentInvoiceTotalAmount, fromcurrencyid, consigInvoiceCreationDate, externalcurrencyrate);
                                consignmentInvoiceTotalAmountInBase += (Double) crresult.getEntityList().get(0);
                            }
                            KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, price, currencyid, transactionDate, 0);
                            price = (Double) crresult.getEntityList().get(0);

                            amount = price * quantity;
                            price = amount + ((amount / expenseVendorInvoiceTotalAmountInBase) * consignmentInvoiceTotalAmountInBase);
                        }
//                        amount = stockRate * quantity;
                    }
                    /*
                    Code to check if case of Serial JSON or Product JSON
                    */
                    String isSerialJson = row[35] != null ? (String) row[35] : "";          // Check if Serial JSON or Product JSON
                    if (StringUtil.isNullOrEmptyWithTrim(isSerialJson)) {
                        isSerialJson = "F";
                    }
                    
                    String remark = "", assembledProductID = "", costCenterID = "", stockUOMID = "", memo = "";
                    if (isMaterialInOutReport) {
                        remark = row[36] != null ? (String) row[36] : "";
                        assembledProductID = row[37] != null ? (String) row[37] : "";
                        costCenterID = row[38] != null ? (String) row[38] : "";
                        stockUOMID = row[39] != null ? (String) row[39] : "";
                        memo = row[40] != null ? (String) row[40] : "";
                    } else {
                        memo = row[36] != null ? (String) row[36] : "";
                    }
                    
                    json.put(Constants.companyid, companyid);
                    if (advanceSearchTransactionlist != null && !advanceSearchTransactionlist.isEmpty() && advanceSearchTransactionlist.containsKey(detailID)) {
                        /**
                         * If detail ID is present in
                         * advanceSearchTransactionlist then set
                         * isAdvanceSearchTransaction flag to true for
                         * particular transaction detail.
                         */
                        json.put(PriceValuationStack.isAdvanceSearchTransaction, true);
                        if (advanceSearchTransactionlist.get(detailID).equals("T")) {
                            isSerialJson = "T";
                        } 
                    }
                    
                    if (isSerialForProduct && (transType.intValue() == 0 || transType.intValue() == 1 || transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4 || transType.intValue() == 6)) {
                        int transactiontype = 28;
                        if (transType.intValue() == 0 || transType.intValue() == 1 || transType.intValue() == 6) {
                            transactiontype = 28;
                        } else if (transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4) {
                            if (transType.intValue() == 2) {
                                transactiontype = 31;
                            } else if (transType.intValue() == 3 || transType.intValue() == 5) {
                                transactiontype = 27;
                            } else if (transType.intValue() == 4) {
                                transactiontype = 29;
                            }
                        }
                        storageParams.put("transactiontype", (transactiontype + ""));
                        storageParams.put("detailid", detailID);
                        
                        /*
                        Pass searchjson to fetch only serials which matches search criterion.
                        */
                        if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                            Searchjson = requestParams.get("searchJson").toString();
                        }
                        if (!StringUtil.isNullOrEmptyWithTrim(Searchjson)) {
                            reqPar1.put(Constants.companyKey, requestParams.get(Constants.companyKey));
                            reqPar1.put(Constants.Acc_Search_Json, Searchjson);
                            reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                            reqPar1.put(Constants.appendCase, "and");
                            reqPar1.put(Constants.moduleid, Constants.SerialWindow_ModuleId);
//                          reqPar1.put("removeProductCustomFilter", true);
     
                             Searchjson = accReportsService.getSearchJsonByModule(reqPar1);
                        }
                        
                        storageParams.put("searchjson", Searchjson);
                        storageParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        storageParams.put("transType", transType.intValue());
                        storageParams.put("isSerialJson", "F");
                        storageParams.put("serialNameSearch", serialNameSearch); 
                        KwlReturnObject srno = accProductObj.getSerialIdByStorageDetails(storageParams);
                        KwlReturnObject srno1 =null;
                        if (advanceSearchTransactionlist != null && !advanceSearchTransactionlist.isEmpty() && advanceSearchTransactionlist.containsKey(detailID)) {
                            storageParams.put("isSerialJson", isSerialJson);
                            srno1 = accProductObj.getSerialIdByStorageDetails(storageParams);
                        }
                        storageParams.remove("transactiontype");
                        storageParams.remove("detailid");
                        storageParams.remove("searchjson");
                        storageParams.remove("filterConjuctionCriteria");
                        storageParams.remove("transType");
                        storageParams.remove("isSerialJson");
                        List list2 = srno.getEntityList();
                        List list3 = null;
                        if (srno1 !=null && !srno1.getEntityList().isEmpty()) {
                            list3 = srno1.getEntityList();
                        }
                        if (list2 != null && !list2.isEmpty()) {
                            for (Object obj : list2) {
                                String serialID = obj.toString();
                                storageParams.put("serialName", serialID);
                                if (advanceSearchTransactionlist != null && !advanceSearchTransactionlist.isEmpty()) {
                                    if (list3 != null && !list3.isEmpty() && list3.contains(serialID)) {
                                        json.put(PriceValuationStack.isAdvanceSearchTransaction, true);
                                    } else {
                                        json.put(PriceValuationStack.isAdvanceSearchTransaction, false);
                                    }
                                }
                                stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                            }
                        }
                    } else if (transType.intValue() == 7 || transType.intValue() == 8 || transType.intValue() == 9 || transType.intValue() == 10 || transType.intValue() == 11 || transType.intValue() == 12 || transType.intValue() == 13 || transType.intValue() == 14 || transType.intValue() == 15 || transType.intValue() == 16) {
                        if (!StringUtil.isNullOrEmptyWithTrim(serialNames)) {
                            List serialList = stockDAO.getERPSerialFromBatch(companyid, productid, warehouseId, locationId, rowId, rackId, binId, batchName, serialNames);
                            if (serialList != null && !serialList.isEmpty()) {
                                for (Object obj : serialList) {
                                    String serialID = obj.toString();
                                    if (isMaterialInOutReport || isStockLedgerDetailedReport) {
                                        /**
                                         * If Material In/Out or Stock Ledger
                                         * Detailed Report
                                         */
                                        if (StringUtil.isNullOrEmptyWithTrim(serialNameSearch) || (!StringUtil.isNullOrEmptyWithTrim(serialNameSearch) && !StringUtil.isNullOrEmptyWithTrim(serialID))) {
                                            /**
                                             * Push Transaction into stack only
                                             * if (serial name search is blank)
                                             * or (both serial name search and
                                             * serial Id is not empty).
                                             */
                                            storageParams.put("serialName", serialID);
                                            stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                                        }
                                    } else {
                                        storageParams.put("serialName", serialID);
                                        stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                                    }
                                }
                            }
                        } else {
                            if (isMaterialInOutReport || isStockLedgerDetailedReport) {
                                /**
                                 * If Material In/Out or Stock Ledger Detailed
                                 * Report
                                 */
                                if (StringUtil.isNullOrEmptyWithTrim(serialNameSearch) || (!StringUtil.isNullOrEmptyWithTrim(serialNameSearch) && !StringUtil.isNullOrEmptyWithTrim(""))) {
                                    /**
                                     * Push Transaction into stack only if
                                     * (serial name search is blank) or (both
                                     * serial name search and serial Id is not
                                     * empty).
                                     */
                                    storageParams.put("serialName", "");
                                    stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                                }
                            } else {
                                storageParams.put("serialName", "");
                                stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
//                            }
                            }
                        }
                    } else {
                        /* put details related to the location/warehouse/row /rack/bin/batch/serial */
                        if (!((isMaterialInOutReport || isStockLedgerDetailedReport) && !StringUtil.isNullOrEmpty(serialNameSearch))) {
                            // For Material In/Out Report and quick search applied on serial names then do not add products for which serial is not activated.
                            stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, memo, json);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * NOTE: All calls to this method now go to a new method in which Product objects dependency has been removed (ERP-38563)
     * New method: {@link #getTransactionJson(JSONObject productJson,PriceValuationStack stack,JSONArray dataArr,HashMap<String, Object> requestParams)}
     * 
     * Function to be used to create transaction JSON using the list of transactions push into the stack
     * @param product
     * @param stack
     * @param dataArr
     * @param requestParams
     * @return  JSONArray for all the transactions using productID & stack
     */
    @Deprecated
    private JSONArray getTransactionJSON(Product product, PriceValuationStack stack, JSONArray dataArr, HashMap<String, Object> requestParams) {
        try {
            double ledgerOnHandQuantity = 0, ledgerFinalValuation = 0,grandTotal=0;
            String companyid = (String) requestParams.get(Constants.companyid);
            double stockInQty = 0;
            double stockOutQty = 0;
            boolean isInventoryValuation = false, isFromStockReport = false, isFromStockValuationDetail = false, isFromStockValuationSummary = false, isFromStockMovement = false;
            String storageType=null, storageTypeId=null, batchName=null;

            //This function is called from accReportController.getStockStatus through getInventoryValuationData, to identify it, we have checked reportid
            int reportId = 0; //Check for the report id
            if (requestParams.containsKey(Constants.REPORT_ID) && requestParams.get(Constants.REPORT_ID) != null ) {
                reportId = Integer.parseInt(requestParams.get(Constants.REPORT_ID).toString());
            }          
            
            if (requestParams.containsKey("isInventoryValuation") && requestParams.get("isInventoryValuation") != null) {
                isInventoryValuation = Boolean.parseBoolean(requestParams.get("isInventoryValuation").toString());
            }
            if (requestParams.containsKey("isFromStockReport") && requestParams.get("isFromStockReport") != null) {
                isFromStockReport = Boolean.parseBoolean(requestParams.get("isFromStockReport").toString());
            }
            if (requestParams.containsKey("isFromStockMovement") && requestParams.get("isFromStockMovement") != null) {
                isFromStockMovement = Boolean.parseBoolean(requestParams.get("isFromStockMovement").toString());
            }
            if (requestParams.containsKey("isFromStockValuationDetail") && requestParams.get("isFromStockValuationDetail") != null) {
                isFromStockValuationDetail = Boolean.parseBoolean(requestParams.get("isFromStockValuationDetail").toString());
                storageType = requestParams.get("storageType") != null ? requestParams.get("storageType").toString(): null;
                storageTypeId = requestParams.get("storageTypeId") != null? requestParams.get("storageTypeId").toString(): null;
                batchName = requestParams.get("batchName") != null? requestParams.get("batchName").toString(): null;
            }
            if (requestParams.containsKey("isFromStockValuationSummary") && requestParams.get("isFromStockValuationSummary") != null) {
                isFromStockValuationSummary = Boolean.parseBoolean(requestParams.get("isFromStockValuationSummary").toString());
                storageType = requestParams.get("storageType") != null ? requestParams.get("storageType").toString(): null;
            }
            boolean stockAgeing = false;// Stock Ageing Report Flag
            if (requestParams.containsKey("stockAgeing") && requestParams.get("stockAgeing") != null) {
                stockAgeing = Boolean.parseBoolean(requestParams.get("stockAgeing").toString());
            }
            boolean isFromStockSummaryReport = false;// Stock Ageing Report Flag
            if (requestParams.containsKey("isFromStockSummaryReport") && requestParams.get("isFromStockSummaryReport") != null) {
                isFromStockSummaryReport = Boolean.parseBoolean(requestParams.get("isFromStockSummaryReport").toString());
            }
            boolean isConsolidationStockReport = false;// Stock Ageing Report Flag
            if (requestParams.containsKey("isConsolidationStockReport") && requestParams.get("isConsolidationStockReport") != null) {
                isConsolidationStockReport = Boolean.parseBoolean(requestParams.get("isConsolidationStockReport").toString());
            }
            boolean isStockLedgerDetailedReport = false;// Stock Ageing Report Flag
            if (requestParams.containsKey("isStockLedgerDetailedReport") && requestParams.get("isStockLedgerDetailedReport") != null) {
                isStockLedgerDetailedReport = Boolean.parseBoolean(requestParams.get("isStockLedgerDetailedReport").toString());
            }
            boolean isMaterialInOutReport = false;// Material IN/OUT Report
            if (requestParams.containsKey("isMaterialInOutReport") && requestParams.get("isMaterialInOutReport") != null) {
                isMaterialInOutReport = Boolean.parseBoolean(requestParams.get("isMaterialInOutReport").toString());
            }
            boolean isExport = false;
            if (requestParams.containsKey("isExportPDF") && requestParams.get("isExportPDF") != null) {
                isExport = Boolean.parseBoolean(requestParams.get("isExportPDF").toString());
            }
            boolean isActivateLandedInvAmt = false;
            if (requestParams.containsKey("isactivatelandedinvamt") && requestParams.get("isactivatelandedinvamt") != null) {
                isActivateLandedInvAmt = Boolean.parseBoolean(requestParams.get("isactivatelandedinvamt").toString());
            }
            String serialNameSearch = "";
            if (requestParams.containsKey("serialNameSearch") && requestParams.get("serialNameSearch") != null) {
                serialNameSearch = requestParams.get("serialNameSearch").toString();
            }
             if (requestParams.containsKey("gTotal") && requestParams.get("gTotal") != null) {
                grandTotal = Double.parseDouble(requestParams.get("gTotal").toString());
            }
            boolean isAdvanceSearchForValuation = false;
                        
             double landingcatamt = 0d;
             double avglandedcost = 0d;
             double landingcostvalue = 0d;
             if (requestParams.containsKey(Constants.Acc_Search_Json) && requestParams.get(Constants.Acc_Search_Json) != null && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
                /**
                 * If searchJson is not empty then set
                 * isAdvanceSearchForValuation to true.
                 * <code>isAdvanceSearchForValuation</code> flag is used to
                 * fetch only those transactions for which search criteria is
                 * matching.
                 */
                isAdvanceSearchForValuation = true;
            }
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            if (isFromStockValuationDetail || isFromStockValuationSummary) {
                if(isFromStockValuationSummary && StringUtil.isNullOrEmpty(storageType)){
                    return dataArr;
                }
                Map<String, String> partialStorageDetail = new HashMap();
                
                List<PriceValuationStack.StorageFilter> scList = new ArrayList();
                String storageName = null;
                String storageDesc = null;
                int type = 6;
                try {
                    type = Integer.valueOf(storageType);
                } catch (Exception ex) {
                    type = 6;
                }

                switch (type) {
                    case 1:
                        scList.add(PriceValuationStack.StorageFilter.WAREHOUSE);
                        if (!StringUtil.isNullOrEmpty(storageTypeId)) {
                            partialStorageDetail.put("warehouseId", storageTypeId);
                        }
                        break;
                    case 2:
                        scList.add(PriceValuationStack.StorageFilter.LOCATION);
                        if (!StringUtil.isNullOrEmpty(storageTypeId)) {
                            partialStorageDetail.put("locationId", storageTypeId);
                        }
                        break;
                    case 3:
                        scList.add(PriceValuationStack.StorageFilter.ROW);
                        if (!StringUtil.isNullOrEmpty(storageTypeId)) {
                            partialStorageDetail.put("rowId", storageTypeId);
                        }
                        break;
                    case 4:
                        scList.add(PriceValuationStack.StorageFilter.RACK);
                        if (!StringUtil.isNullOrEmpty(storageTypeId)) {
                            partialStorageDetail.put("rackId", storageTypeId);
                        }
                        break;
                    case 5:
                        scList.add(PriceValuationStack.StorageFilter.BIN);
                        if (!StringUtil.isNullOrEmpty(storageTypeId)) {
                            partialStorageDetail.put("binId", storageTypeId);
                        }
                        break;
                }

                if (isFromStockValuationDetail) {
                    scList.add(PriceValuationStack.StorageFilter.BATCH);
                    if (!StringUtil.isNullOrEmpty(batchName)) {
                        partialStorageDetail.put("batchName", batchName);
                    }
                }
                PriceValuationStack.StorageFilter[] sc = new PriceValuationStack.StorageFilter[scList.size()];
                List<TransactionBatch> tbList = stack.getTransactionBatchList(null, partialStorageDetail, true);
                Map<String, PriceValuationStack.Batch> storewiseValuationDetails = stack.getStorageDetailwiseQuantityBatch(tbList, scList.toArray(sc), false,true);
                KwlReturnObject res = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), requestParams.get(Constants.globalCurrencyKey).toString());
                KWLCurrency currency = (KWLCurrency) res.getEntityList().get(0);
                if (storewiseValuationDetails != null) {
                    String categoryName = "";
                    Map<String, JSONObject> locationSummaryMap = new HashMap();
                    if(isFromStockValuationSummary){
                        for(int i=0; i<dataArr.length(); i++){
                            JSONObject jObj = dataArr.getJSONObject(i);
                            locationSummaryMap.put(jObj.optString("locationid"), jObj);
                        }
                    }
                    for (Map.Entry<String, PriceValuationStack.Batch> detailMap : storewiseValuationDetails.entrySet()) {
                        PriceValuationStack.Batch batch = detailMap.getValue();
                        if (batch != null) {
                            double price = batch.getPrice();
                            double amount = batch.getAmount();
                            double quantity = batch.getQuantity();
                            String serialNames="";
                            for (String serial : batch.getAvailableSerial()) {
                                if (!StringUtil.isNullOrEmpty(serial)) {
                                    res = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), serial);
                                    NewBatchSerial nbs = (NewBatchSerial) res.getEntityList().get(0);
                                    serialNames += nbs.getSerialname().toString()+",";
                                }
                            }
                            if(!StringUtil.isNullOrEmpty(serialNames)){
                                serialNames=serialNames.substring(0, serialNames.length()-1);
                            }
                           if(batch.getQuantity()==0 && batch.getAmount()==0.0){
                                continue;
                            }
                            storageTypeId = "";
                            storageName = "";
                            
                            StoreMaster sm;
                            switch (type) {
                                case 1:
                                    if (!StringUtil.isNullOrEmpty(batch.getWarehouseId())) {
                                        res = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), batch.getWarehouseId());
                                        InventoryWarehouse iw = (InventoryWarehouse) res.getEntityList().get(0);
                                        res = accountingHandlerDAOobj.getObject(Store.class.getName(), batch.getWarehouseId()); // For Jasper Report Description
                                        Store iwStore = (Store) res.getEntityList().get(0);
                                        storageTypeId = iw.getId();
                                        storageName = iw.getName();
                                        storageDesc = iwStore.getDescription();
                                    }
                                    break;
                                case 2:
                                    if (!StringUtil.isNullOrEmpty(batch.getLocationId())) {
                                        res = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), batch.getLocationId());
                                        InventoryLocation il = (InventoryLocation) res.getEntityList().get(0);
                                        storageTypeId = il.getId();
                                        storageName = il.getName();
                                    }
                                    
                                    break;
                                case 3:
                                    if (!StringUtil.isNullOrEmpty(batch.getRowId())) {
                                        res = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), batch.getRowId());
                                        sm = (StoreMaster) res.getEntityList().get(0);
                                        storageTypeId = sm.getId();
                                        storageName = sm.getName();
                                    }
                                    break;
                                case 4:
                                    if (!StringUtil.isNullOrEmpty(batch.getRackId())) {
                                        res = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), batch.getRackId());
                                        sm = (StoreMaster) res.getEntityList().get(0);
                                        storageTypeId = sm.getId();
                                        storageName = sm.getName();
                                    }
                                    break;
                                case 5:
                                    if (!StringUtil.isNullOrEmpty(batch.getBinId())) {
                                        res = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), batch.getBinId());
                                        sm = (StoreMaster) res.getEntityList().get(0);
                                        storageTypeId = sm.getId();
                                        storageName = sm.getName();
                                    }
                                    break;
                            }
                            
                            if(isFromStockValuationDetail){
//                                if(!StringUtil.isNullOrEmpty(batch.getBatchId())){
//                                    res = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), batch.getBatchId());
//                                    NewProductBatch npb = (NewProductBatch) res.getEntityList().get(0);
//                                    batchName = npb.getBatchname();
//                                }
//                                KwlReturnObject kwlObj = accProductObj.getProductCategoryForDetailsReport(product.getID());   //getting product category
//                                if (kwlObj != null && kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty()) {
//                                    List l1 = kwlObj.getEntityList();
//                                    int len = l1.size();
//                                    boolean isfirst = true;
//                                    for (int i = 0; i < len; i++) {
//                                        ProductCategoryMapping pcm = (ProductCategoryMapping) l1.get(i);
//                                        if (pcm != null && pcm.getProductCategory() != null) {
//                                            String category_Name = pcm.getProductCategory().getValue();
//                                            if (isfirst) {
//                                                categoryName = category_Name;
//                                                isfirst = false;
//                                            } else {
//                                                categoryName += "," + category_Name;
//                                            }
//                                        } else {
//                                            categoryName = "None";
//                                        }
//                                    }
//                                } else {
//                                    categoryName = "None";
//                                }
                                //ERM-447 Inserting landing cost value for Stock Valuation Detail Report
                                JSONObject obj = new JSONObject();
                                if (isActivateLandedInvAmt && batch.getExtraJSON()!=null) {
                                    obj.put("avglandedcost", (authHandler.roundUnitPrice(Math.abs(batch.getPrice()), companyid)));
                                }
                                obj.put(Constants.productid, product.getProductid());
                                obj.put("productIDJASPER", product.getProductid());
                                obj.put("productname", product.getProductName());
                                obj.put("uom", product.getUnitOfMeasure() != null  ?  product.getUnitOfMeasure().getNameEmptyforNA() : "");
                                obj.put("productDesc", StringUtil.isNullOrEmpty(product.getDescription())?"":isExport ? StringUtil.replaceFullHTML(product.getDescription().replace("<br>","\n")) : product.getDescription());
//                                obj.put("productCategory", categoryName);
                                obj.put("productBatch", batch.getBatchId());
                                obj.put("quantity", authHandler.formattedQuantity(quantity,companyid));
                                price=quantity!=0?authHandler.round(amount, companyid)/quantity:price;
                                obj.put("rate",authHandler.formattingDecimalForUnitPrice(Math.abs(price), companyid));
                                if (isActivateLandedInvAmt && batch.getDocType() != TransactionBatch.DocType_DO && quantity!=0.0) {
                                    obj.put("rate", authHandler.formattingDecimalForUnitPrice(Math.abs(batch.getWithoutlandedamount() / quantity), companyid));
                                }
                                obj.put("currencysymbol", currency.getSymbol());
                                obj.put("currencyname", currency.getName());
                                obj.put("categoryName", "Grand");
                                obj.put("totalcategorycost", authHandler.round(amount, companyid));
                                obj.put("locationid",storageTypeId);
                                obj.put("locationName", storageName);
                                obj.put("locationDescription", storageDesc);
                                obj.put("serial", serialNames);
                                obj.put("value", authHandler.round(amount, companyid));
                                dataArr.put(obj);
                            }else{
                                if (locationSummaryMap.containsKey(storageTypeId)) {
                                    JSONObject obj = locationSummaryMap.get(storageTypeId);
                                    obj.put("value", (Double) (obj.get("value")) + authHandler.round(amount, companyid));
                                } else {
                                    if (!StringUtil.isNullOrEmpty(storageTypeId)) { // ERP-29818
                                        JSONObject obj = new JSONObject();
                                        obj.put("locationid", storageTypeId);
                                        obj.put("locationName", storageName);
                                        obj.put("locationDescription", storageDesc);
                                        obj.put("value", authHandler.round(amount, companyid));
                                        dataArr.put(obj);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (isMaterialInOutReport) {
                String tType = requestParams.get("transactionType") != null ? (String) requestParams.get("transactionType") : null;
                TransactionType transactionType = null;
                if (!StringUtil.isNullOrEmpty(tType) && !tType.equals("ALL")) {
                    transactionType = TransactionType.valueOf(tType);
                }
                String storeId = requestParams.get("store") != null ? (String) requestParams.get("store") : null;
                Map<String, String> partialStorageDetails = new HashMap();
                partialStorageDetails.put("warehouseId", storeId);
                PriceValuationStack.Batch openingBatch = null;
                if (transactionType == null || transactionType == TransactionType.OPENING) {
                    openingBatch = stack.getPartialOpeningTransactionBatch(partialStorageDetails);
                }
                Store iwStore = null;
                if (!StringUtil.isNullOrEmpty(storeId)) {
                    KwlReturnObject res = accountingHandlerDAOobj.getObject(Store.class.getName(), storeId);
                    iwStore = (Store) res.getEntityList().get(0);
                }
                if (openingBatch != null) {
                    /**
                     * Add OpeningBatch Transaction details only if (quantity is
                     * not zero and serial name is not empty).
                     */
                    if (!(openingBatch.getQuantity() == 0.0 && !StringUtil.isNullOrEmpty(serialNameSearch))) {
                        JSONObject jObj = new JSONObject();
                        JSONArray smDetails = new JSONArray();
                        if (openingBatch.getMaterialInOutlocationMap() != null && !openingBatch.getMaterialInOutlocationMap().isEmpty()) {
                            for (Map.Entry<String, Double> entrySet : openingBatch.getMaterialInOutlocationMap().entrySet()) {
                                JSONObject srObject = locationMapBuilder(entrySet);
                                smDetails.put(srObject);
                            }
                        }
                        jObj.put("stockDetails", smDetails);
                        jObj.put("itemcode", product.getProductid());
                        jObj.put("itemdescription", product.getDescription());
                        jObj.put("itemname", product.getName());
                        jObj.put("store", (iwStore == null) ? "" : iwStore.getDescription());
                        jObj.put("assemble", "-");
                        jObj.put("orderuom", (product.getUnitOfMeasure() == null) ? "" : product.getUnitOfMeasure().getNameEmptyforNA());
                        jObj.put("costcenter", "-");
                        jObj.put("vendor", "-");
                        jObj.put("type", "BACK FORWARD");
                        jObj.put("orderquantity", openingBatch.getQuantity());
                        jObj.put("amount", authHandler.round(openingBatch.getAmount(), companyid));
                        double price = openingBatch.getPrice();
                        jObj.put("avgCost", authHandler.round(price, companyid));
                        jObj.put("orderno", "-");
                        jObj.put("remark", "Back forward transaction balance");
                        jObj.put("isBatchForProduct", product.isIsBatchForProduct());
                        jObj.put("isSerialForProduct", product.isIsSerialForProduct());
                        jObj.put("isRowForProduct", product.isIsrowforproduct());
                        jObj.put("isRackForProduct", product.isIsrackforproduct());
                        jObj.put("isBinForProduct", product.isIsbinforproduct());
                        jObj.put("moduleName", "-");
                        jObj.put(Constants.productid, product.getID());
                        dataArr.put(jObj);
                    }
                }
                Map<String, PriceValuationStack.Batch> allTransactionBatch = null;
                if(transactionType == TransactionType.OPENING){
                    PriceValuationStack.Batch batch = stack.getInitialTransactionBatch(null, partialStorageDetails);
                    JSONObject json = getTransactionJSON(product, batch, iwStore, df, companyid);
                    dataArr.put(json);
                } else {
                    if (transactionType == null) {
                        if (!isAdvanceSearchForValuation) {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, null, null);
                        } else {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, null, true);
                        }
                    } else if (transactionType == TransactionType.IN) {
                        if (!isAdvanceSearchForValuation) {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, false, null);
                        } else {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, false, true);
                        }
                    } else if (transactionType == TransactionType.OUT) {
                        if (!isAdvanceSearchForValuation) {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, true, null);
                        } else {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, true, true);
                        }
                    }
                    if (allTransactionBatch != null) {
                        SortTransactionMap sortTransactionMap = new SortTransactionMap();
                        allTransactionBatch = sortTransactionMap.getSortedMap(allTransactionBatch);
                        for (Map.Entry<String, PriceValuationStack.Batch> entrySet : allTransactionBatch.entrySet()) {
                            PriceValuationStack.Batch batch = entrySet.getValue();
                            JSONObject json = getTransactionJSON(product, batch, iwStore, df, companyid);
                            dataArr.put(json);
                        }
                    }
                }
            } else if (isFromStockMovement) {
                String storeId = requestParams.get("storeId") != null ? (String)requestParams.get("storeId") : null;
                String locationId = requestParams.get("locationId") != null ? (String)requestParams.get("locationId") : null;
                Map<String, String> partialStorageDetails = new HashMap();
                if(!StringUtil.isNullOrEmpty(storeId)){
                    partialStorageDetails.put("warehouseId", storeId);
                }
                if(!StringUtil.isNullOrEmpty(storeId)){
                    partialStorageDetails.put("locationId", locationId);
                }
                Map<String, PriceValuationStack.Batch> inTransactions = null;
                if (!isAdvanceSearchForValuation) {
                    inTransactions = stack.getAllTransactionBatches(null, partialStorageDetails, true, null, null, null);
                } else {
                    inTransactions = stack.getAllTransactionBatches(null, partialStorageDetails, true, null, null, true);
                }
                JSONObject productObj = dataArr.optJSONObject(0);
                if(dataArr.optJSONObject(0) == null){
                    productObj = new JSONObject();
                }
                JSONObject obj = new JSONObject();
                productObj.put(product.getID(), obj);
                for (Map.Entry<String, PriceValuationStack.Batch> entry : inTransactions.entrySet()) {
                    PriceValuationStack.Batch batch = entry.getValue();
                    if (batch != null) {
                        obj.put(entry.getKey(), batch.getPrice());
                    }
                }
                dataArr.put(productObj);
            } else if (isFromStockSummaryReport) {
                JSONObject obj = new JSONObject();
                obj.put("itemcode", !StringUtil.isNullOrEmpty(product.getProductid()) ? product.getProductid() : "");
                obj.put("itemdescription", product.getDescription());
                obj.put("itemname", product.getName());
                PriceValuationStack.Batch openingBatch = null;
                if (!isAdvanceSearchForValuation) {
                    openingBatch = stack.getOpeningTransactionBatch();
                } else {
                    /**
                     * Get opening batch for transactions for which search
                     * criteria is matching.
                     */
                    openingBatch = stack.getOpeningTransBatch(true);
                }
                if (openingBatch != null) {
                    obj.put("openingqty", authHandler.formattedQuantity(openingBatch.getQuantity(), companyid));
                    ledgerFinalValuation += openingBatch.getAmount();
                    ledgerOnHandQuantity += openingBatch.getQuantity();
                }else{
                    obj.put("openingqty", authHandler.formattedQuantity(0.0, companyid));
                }
                
                Map<String, PriceValuationStack.Batch> detailsMap = null;
                if (!isAdvanceSearchForValuation) {
                    detailsMap = stack.getAllTransactionBatches(null, null, false, null);
                } else {
                    detailsMap = stack.getAllTransactionBatches(null, null, false, false, null, true);
                }
                SortTransactionMap sortTransactionMap = new SortTransactionMap();
                detailsMap = sortTransactionMap.getSortedMap(detailsMap);
                double goodsReceiptOrderQty=0;
                double deliveryOrderQty=0;
                double stockAdjustmentQty=0;
                double stockTransferOutQty=0;
                double stockTransferInQty=0;
                if (openingBatch == null) {
                    goodsReceiptOrderQty += openingBatch.getQuantity();
                }
                if (detailsMap != null && detailsMap.size() > 0) {
                    for (Map.Entry<String, PriceValuationStack.Batch> detailMap : detailsMap.entrySet()) {
                        PriceValuationStack.Batch batch = detailMap.getValue();
                        if (batch != null) {
                            double price = batch.getPrice();
                            double amount = batch.getAmount();
                            int transtype = batch.getDocType();
                            ledgerFinalValuation += amount;
                            ledgerOnHandQuantity += batch.getQuantity();
                            if (transtype == TransactionBatch.DocType_GRN || transtype == TransactionBatch.DocType_OPENING || transtype == TransactionBatch.DocType_ASSEMBLY_MAIN) {
                                goodsReceiptOrderQty += batch.getQuantity();
                            } else if (transtype == TransactionBatch.DocType_PURCHASE_RETURN) {
                                goodsReceiptOrderQty -= Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_DO || transtype == TransactionBatch.DocType_ASSEMBLY_SUB) {
                                deliveryOrderQty += Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_SALES_RETURN) {
                                deliveryOrderQty -= Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_SA_IN) {
                                stockAdjustmentQty += Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_SA_OUT) {
                                stockAdjustmentQty -= Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_SR_ISSUE || transtype == TransactionBatch.DocType_IN_ISSUE || transtype == TransactionBatch.DocType_IST_ISSUE || transtype == TransactionBatch.DocType_ILT_ISSUE || transtype == TransactionBatch.DocType_WO_OUT) {
                                stockTransferOutQty += Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_SR_COLLECT || transtype == TransactionBatch.DocType_IN_COLLECT || transtype == TransactionBatch.DocType_IST_COLLECT || transtype == TransactionBatch.DocType_ILT_COLLECT || transtype == TransactionBatch.DocType_WO_IN) {
                                stockTransferInQty += Math.abs(batch.getQuantity());
                            }
                        }
                    }
                }
                obj.put("uom", product.getUnitOfMeasure() != null ? product.getUnitOfMeasure().getNameEmptyforNA() : "");
                obj.put("goodsreceiptorderqty", authHandler.formattedAmount(goodsReceiptOrderQty, companyid));
                obj.put("deliveryorderqty", authHandler.formattedAmount(deliveryOrderQty, companyid));
                obj.put("stocktransferINqty", authHandler.formattedAmount(stockTransferInQty, companyid));
                obj.put("stocktransferOUTqty", authHandler.formattedAmount(stockTransferOutQty, companyid));
                obj.put("stockadjustmentqty", authHandler.formattedAmount(stockAdjustmentQty, companyid));
                obj.put("amount", authHandler.formattedAmount(ledgerFinalValuation, companyid));
                obj.put("balanceqty", authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));
                if (ledgerOnHandQuantity != 0) {
                    double avgcost = 0;
                    avgcost = ledgerFinalValuation / ledgerOnHandQuantity;
                    obj.put("avgcost", authHandler.formattedAmount(avgcost, companyid));
                } else {
                    obj.put("avgcost", authHandler.formattedAmount(0, companyid));
                }
                Map<String, Object> reqprm = new HashMap<String, Object>();
                reqprm.put("company", companyid);
                reqprm.put("product", product.getID());
                reqprm.put("store", (String) requestParams.get("store"));
                Date todate = (Date) requestParams.get("toDate");
                String transDate = authHandler.getDateOnlyFormat().format(todate);
                todate = authHandler.getDateOnlyFormat().parse(transDate);
                reqprm.put("businessDate", todate);
                double cyclyCountQty = accProductObj.getCycleCountQuantity(reqprm);
                if (cyclyCountQty == 0) {
                    obj.put("cyclecountqty", "N/A");
                    obj.put("varianceqty", "N/A");
                } else {
                    obj.put("cyclecountqty", cyclyCountQty);
                    double variance = ledgerOnHandQuantity - cyclyCountQty;
                    obj.put("varianceqty", variance);
                }
               dataArr.put(obj);
            } else if (stockAgeing) {
                int duration = 45;
                String asofDateString = (String) requestParams.get(Constants.REQ_enddate);
                Date asofDate = null;
                if (!StringUtil.isNullOrEmpty(asofDateString) && df != null) {
                    asofDate = df.parse(asofDateString);
                }
                Date curDate = asofDate;
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal1.add(Calendar.DAY_OF_YEAR, -duration);
                cal2.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                Date cdate1 = cal1.getTime();
                String caldate1 = df.format(cdate1);
                try {
                    cdate1 = df.parse(caldate1);
                } catch (ParseException ex) {
                    cdate1 = cal1.getTime();
                }
                Date cdate2 = cal2.getTime();
                String caldate2 = df.format(cdate2);
                try {
                    cdate2 = df.parse(caldate2);
                } catch (ParseException ex) {
                    cdate2 = cal2.getTime();
                }
                Map<String, PriceValuationStack.Batch> inTransactions = null;
                if (!isAdvanceSearchForValuation) {
                    inTransactions = stack.getAllTransactionBatches(null, null, null, false);
                } else {
                    inTransactions = stack.getAllTransactionBatches(null, null, false, null, false, true);
                }
                SortTransactionMap sortTransactionMap = new SortTransactionMap();
                inTransactions = sortTransactionMap.getSortedMap(inTransactions);
                for (Map.Entry<String, PriceValuationStack.Batch> entry : inTransactions.entrySet()) {
                    PriceValuationStack.Batch batch = entry.getValue();
                    if (batch != null) {
                        JSONObject obj = new JSONObject();
                        obj.put("pid", product.getProductid());
                        obj.put("productname", product.getName());
                        obj.put("productDesc", product.getDescription());
                        obj.put("unit", (product.getUnitOfMeasure() == null) ? "" : product.getUnitOfMeasure().getNameEmptyforNA());
                        obj.put("uom", product.getUnitOfMeasure() != null  ?  product.getUnitOfMeasure().getNameEmptyforNA() : "");
                        obj.put("transactionNumber", StringUtil.isNullOrEmpty(batch.getTransactionNo()) ? "" : batch.getTransactionNo());
                        obj.put("transactionDate", batch.getTransactionDate() != null ? df.format(batch.getTransactionDate()) : "");
                        obj.put("transactionType", batch.getDocType());
                        if (batch.getDocType() == TransactionBatch.DocType_INITIAL) {
                            obj.put("transactionType", "Opening");
                        } else if (batch.getDocType() == TransactionBatch.DocType_GRN) {
                            obj.put("transactionType", "Goods Receipt");
                        } else if (batch.getDocType() == TransactionBatch.DocType_SA_IN) {
                            obj.put("transactionType", "Stock Adjustment");
                        } else if (batch.getDocType() == TransactionBatch.DocType_PURCHASE_RETURN) {
                            obj.put("transactionType", "Purchase Return");
                        } else if (batch.getDocType() == TransactionBatch.DocType_SALES_RETURN) {
                            obj.put("transactionType", "Sales Return");
                        } else if (batch.getDocType() == TransactionBatch.DocType_ASSEMBLY_MAIN) {
                            obj.put("transactionType", "Assembly");
                        } else if (batch.getDocType() == TransactionBatch.DocType_IN_ISSUE) {
                            obj.put("transactionType", "Issue Note Issue");
                        } else if (batch.getDocType() == TransactionBatch.DocType_IN_COLLECT) {
                            obj.put("transactionType", "Issue Note Collect");
                        } else if (batch.getDocType() == TransactionBatch.DocType_IST_COLLECT) {
                            obj.put("transactionType", "Inter Store Transfer");
                        } else if (batch.getDocType() == TransactionBatch.DocType_IST_COLLECT) {
                            obj.put("transactionType", "Inter Store Transfer");
                        } else if (batch.getDocType() == TransactionBatch.DocType_ILT_ISSUE) {
                            obj.put("transactionType", "Inter Location Transfer Collect Issue");
                        } else if (batch.getDocType() == TransactionBatch.DocType_ILT_COLLECT) {
                            obj.put("transactionType", "Inter Location Transfer Collect");
                        } else if (batch.getDocType() == TransactionBatch.DocType_WO_IN){ /* In Stock Aging Report show transaction type:Work Order */
                            obj.put("transactionType", "Work Order");
                        }
                        
                        double outQuantity = 0; 
                        /**
                         * Get OUT transaction quantity for the IN transaction.
                         */
                        if (batch.getOutTransactionQtyAmountMap() != null) {
                            for (Map.Entry<String, List<Double>> outEntry : batch.getOutTransactionQtyAmountMap().entrySet()) {
                                List<Double> value = outEntry.getValue();
                                if (value != null && !value.isEmpty()) {
                                    outQuantity += value.get(0);
                                }
                            }
                        }
                        /**
                         * Remaining Quantity is IN quantity - OUT quantity i.e.
                         * remainingQty = inTransactionQty - outTransactionQty.
                         */
                        double remainingQty = batch.getQuantity() - outQuantity; 
                        /**
                         * Show amount according to the remaining quantity.
                         */
                        double amount = batch.getPrice() * remainingQty;
                        if (product.getValuationMethod() == ValuationMethod.AVERAGE) {                           // If valuation method is average then price should average price
                            PriceValuationStack.Batch avgCostBatch = stack.getTransactionBatch();
                            amount = remainingQty * (avgCostBatch.getPrice() != 0 ? avgCostBatch.getPrice() : 0);
                        }
                        if (batch.getTransactionDate() != null && (cdate1.before(batch.getTransactionDate()) || cdate1.equals(batch.getTransactionDate()))) {
                            obj.put("quantity1", authHandler.formattedQuantity(remainingQty, companyid));
                            obj.put("amount1", amount);
                        } else if (batch.getTransactionDate() != null && ((cdate2.before(batch.getTransactionDate()) || cdate2.equals(batch.getTransactionDate())) && cdate1.after(batch.getTransactionDate()))) {
                            obj.put("quantity2", authHandler.formattedQuantity(remainingQty, companyid));
                            obj.put("amount2", amount);
                        } else {
                            obj.put("quantity3", authHandler.formattedQuantity(remainingQty, companyid));
                            obj.put("amount3", amount);
                        }
                        if (remainingQty != 0.0) { 
                            /**
                             * Don't include transaction which are having
                             * remaining quantity as 0. (ERP-33899)
                             */
                            dataArr.put(obj);
                        }
                    }
                }
            } else if(reportId == Constants.STOCK_STATUS_REPORT_ID){
                dataArr = getTransactionJSONForStockStatusReport(product, stack, dataArr, requestParams);
            }// end of isFromStatusReporyt else if
             else {
                PriceValuationStack.Batch openingBatch = null;
                if (!isAdvanceSearchForValuation) {
                    openingBatch = stack.getOpeningTransactionBatch();
                } else {
                    /**
                     * Get opening batch for transactions for which search
                     * criteria is matching.
                     */
                    openingBatch = stack.getOpeningTransBatch(true);
                }
                /**
                 * Show opening batch only if quantity is not equal to 0(When
                 * isAdvanceSearchForValuation is true).
                 */
                if ((openingBatch != null && !isAdvanceSearchForValuation) || (isAdvanceSearchForValuation && openingBatch!=null && openingBatch.getQuantity() != 0.0)) {
                    /**
                     * Add OpeningBatch Transaction details only if (quantity is
                     * not zero and serial name is not empty).
                     */
                    if (!(openingBatch.getQuantity()==0.0 && !StringUtil.isNullOrEmpty(serialNameSearch))) {
                        JSONObject OpeningRowObj = new JSONObject();
                        OpeningRowObj.put("pid", !StringUtil.isNullOrEmpty(product.getProductid()) ? product.getProductid() : "");
//                    OpeningRowObj.put("productDesc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));//desc
                        OpeningRowObj.put("productDesc", StringUtil.isNullOrEmpty(product.getDescription())?"":isExport ? StringUtil.replaceFullHTML(product.getDescription().replace("<br>","\n")) : product.getDescription());
                        OpeningRowObj.put("transactionNumber", "Opening");
                        OpeningRowObj.put("personCode", "");
                        OpeningRowObj.put("uom", product.getUnitOfMeasure() != null ? product.getUnitOfMeasure().getNameEmptyforNA() : "");
                        OpeningRowObj.put("personName", "");
                        OpeningRowObj.put("received", openingBatch.getQuantity());
                        OpeningRowObj.put("stockRate", authHandler.formattingDecimalForUnitPrice(openingBatch.getPrice(), companyid));
                        OpeningRowObj.put("value", authHandler.formattedAmount(openingBatch.getAmount(), companyid));
                        OpeningRowObj.put("balance", "-");
                        OpeningRowObj.put(Constants.productid, product.getID());
                        if (isStockLedgerDetailedReport) { /* Stock Ledger Detailed Report*/
                            if (openingBatch.getLocationMap() != null) {
                                Map<String, Double> locationMap = openingBatch.getLocationMap();
                                StringBuilder locationBuilder = locationKeyBuilder(locationMap);
                                if (locationBuilder.length() > 0) {
                                    OpeningRowObj.put("location", locationBuilder.toString());
                                }
                            } else {
                                OpeningRowObj.put("location", "N/A");
                            }

                            if (openingBatch.getWarehouseMap() != null) {
                                Map<String, Double> warehouseMap = openingBatch.getWarehouseMap();
                                StringBuilder warehouseBuilder = warehouseKeyBuilder(warehouseMap);
                                if (warehouseBuilder.length() > 0) {
                                    OpeningRowObj.put("warehouse", warehouseBuilder.toString());
                                }
                            } else {
                                OpeningRowObj.put("warehouse", "N/A");
                            }
                            if (openingBatch.getBatchMap() != null) {
                                Map<String, Double> batchMap = openingBatch.getBatchMap();
                                StringBuilder batchBuilder = batchKeyBuilder(batchMap);
                                if (batchBuilder.length() > 0) {
                                    OpeningRowObj.put("batch", batchBuilder.toString());
                                }
                            } else {
                                OpeningRowObj.put("batch", "N/A");
                            }
                            if (openingBatch.getSerialMap() != null) {
                                Map<String, Double> serialMap = openingBatch.getSerialMap();
                                StringBuilder serialBuilder = serialKeyBuilder(serialMap);
                                if (serialBuilder.length() > 0) {
                                    OpeningRowObj.put("serial", serialBuilder.toString());
                                }

                            } else {
                                OpeningRowObj.put("serial", "N/A");
                            }
                        } else {
                            OpeningRowObj.put("batch", "");
                            OpeningRowObj.put("warehouse", "");
                            OpeningRowObj.put("location", "");
                            OpeningRowObj.put("serial", "");
                        }
                        if (isExport) {
                            OpeningRowObj.put("productDesc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));//desc
                            OpeningRowObj.put("productDesc", StringUtil.isNullOrEmpty(product.getDescription())?"":isExport ? StringUtil.replaceFullHTML(product.getDescription().replace("<br>","\n")) : product.getDescription());
                            OpeningRowObj.put(Constants.productid, product.getID());
                            OpeningRowObj.put("isQtyAndValuationJSON", 0);//
                        }
                        if (!isFromStockReport && !isInventoryValuation && !isConsolidationStockReport) {
                            dataArr.put(OpeningRowObj);
                        }
                        ledgerFinalValuation = Double.parseDouble(authHandler.getFormattedUnitPrice(ledgerFinalValuation,companyid)) +Double.parseDouble(authHandler.getFormattedUnitPrice(openingBatch.getAmount(),companyid));
                        ledgerOnHandQuantity += openingBatch.getQuantity();
                    }
                }
                Map<String, PriceValuationStack.Batch> detailsMap = null;
                if (!isAdvanceSearchForValuation) {
                    detailsMap = stack.getAllTransactionBatches(null, null, false, null);
                } else {
                    /**
                     * Get period transactions map for transactions for which
                     * fulfills the search criteria.
                     */
                    detailsMap = stack.getAllTransactionBatches(null, null, false, false, null, true);
                }
                SortTransactionMap sortTransactionMap = new SortTransactionMap();
                detailsMap = sortTransactionMap.getSortedMap(detailsMap);
                if (detailsMap != null) {
                    for (Map.Entry<String, PriceValuationStack.Batch> detailMap : detailsMap.entrySet()) {
//                            String detailID = detailMap.getKey();
                        PriceValuationStack.Batch batch = detailMap.getValue();
                        if (batch != null) {
                            double price = batch.getPrice();
                            double amount = batch.getAmount();
                            int transtype = batch.getDocType();
                            if (isActivateLandedInvAmt && batch.getDocType() == TransactionBatch.DocType_GRN) {
                                price = batch.getWithoutlanded();
                            }
                            ledgerFinalValuation=Double.parseDouble(authHandler.getFormattedUnitPrice(ledgerFinalValuation,companyid)) + Double.parseDouble(authHandler.getFormattedUnitPrice(amount,companyid));
                            ledgerOnHandQuantity += batch.getQuantity();
                            JSONObject obj = new JSONObject();
                            obj.put("pid", !StringUtil.isNullOrEmpty(product.getProductid()) ? product.getProductid() : "");
//                            obj.put("productDesc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));//desc
                            obj.put("productDesc", StringUtil.isNullOrEmpty(product.getDescription())?"":isExport ? StringUtil.replaceFullHTML(product.getDescription().replace("<br>","\n")) : product.getDescription());
                            if (transtype != -1) {
                                obj.put("transactionDate", batch.getTransactionDate() != null ? df.format(batch.getTransactionDate()) : "");
                            }
                            if (isActivateLandedInvAmt && batch.getDocType() == TransactionBatch.DocType_GRN) {
                                /**
                                 * Iterate on landing category as JSON contains (uuid:value) pairs.
                                 */
                                JSONObject batchjson = batch.getExtraJSON();
                                if (batchjson != null) {
                                    String[] landedpairs = batchjson.optString("landingcostpairs", "").split(",");
                                    avglandedcost = batchjson.optDouble("avglandedcost", 0.0);
                                    if (avglandedcost != 0.0) {
                                        obj.put("avglandedcost", (authHandler.roundUnitPrice(Math.abs(avglandedcost), companyid)));
                                    }
                                    for (String landedcategorypair : landedpairs) {
                                        String[] landedpair = landedcategorypair.split(":");
                                        if (landedpair.length == 2) {
                                            double expensecost = Double.parseDouble(StringUtil.isNullOrEmpty(landedpair[1]) ? "0" : landedpair[1]);
                                            if (obj.has(landedpair[0])) {
                                                expensecost += obj.optDouble(landedpair[0], 0);
                                            }
                                            obj.put(landedpair[0], expensecost);
                                        }
                                    }
                                }
                            }
                            obj.put("transactionNumber", StringUtil.isNullOrEmpty(batch.getTransactionNo()) ? "" : batch.getTransactionNo());
                            obj.put("uom", product.getUnitOfMeasure() != null  ?  product.getUnitOfMeasure().getNameEmptyforNA() : "");
                            obj.put("personCode", StringUtil.isNullOrEmpty(batch.getPersonCode()) ? "" : batch.getPersonCode());
                            obj.put("personName", StringUtil.isNullOrEmpty(batch.getPersonName()) ? "" : batch.getPersonName());
                            if (transtype == 0 || transtype == 1 || transtype == 4 || transtype == 6 || transtype == -1 || transtype == 7|| transtype == 10|| transtype == 12|| transtype == 14|| transtype == 16 || transtype == 17) {/*ERM-564 Stock Ledger Report 17(Stock IN) for Receive */
                                obj.put("received", batch.getQuantity());
                                stockInQty += batch.getQuantity();
                            } else {
                                obj.put("delivered", batch.getQuantity());

                                stockOutQty -= batch.getQuantity();

                            }
                            obj.put("memo", StringUtil.isNullOrEmpty(batch.getMemo()) ? "" : batch.getMemo());
                            obj.put("transactiontype", transtype);
                            obj.put("billid", batch.getBillid());
                            obj.put("stockRate", authHandler.formattingDecimalForUnitPrice(price, companyid));
                            if (isStockLedgerDetailedReport) { /* Stock Ledger Detailed Report*/
                                if (batch.getLocationMap() != null) {
                                    Map<String, Double> locationMap = batch.getLocationMap();
                                    StringBuilder locationBuilder = locationKeyBuilder(locationMap);
                                    if (locationBuilder.length() > 0) {
                                        obj.put("location", locationBuilder.toString());
                                    }
                                } else {
                                    obj.put("location", "N/A");
                                }

                                if (batch.getBatchMap() != null) {
                                    Map<String, Double> batchMap = batch.getBatchMap();
                                    StringBuilder batchBuilder = batchKeyBuilder(batchMap);
                                    if (batchBuilder.length() > 0) {
                                        obj.put("batch", batchBuilder.toString());
                                    }
                                } else {
                                    obj.put("batch", "N/A");
                                }
                                if (batch.getWarehouseMap() != null) {
                                    Map<String, Double> warehouseMap = batch.getWarehouseMap();
                                    StringBuilder warehouseBuilder = warehouseKeyBuilder(warehouseMap);
                                    if (warehouseBuilder.length() > 0) {
                                        obj.put("warehouse", warehouseBuilder.toString());
                                    }
                                } else {
                                    obj.put("warehouse", "N/A");
                                }
                                if (batch.getSerialMap() != null) {
                                    Map<String, Double> serialMap = batch.getSerialMap();
                                    StringBuilder serialBuilder = serialKeyBuilder(serialMap);
                                    if (serialBuilder.length() > 0) {
                                        obj.put("serial", serialBuilder.toString());
                                    }
                                } else {
                                    obj.put("serial", "N/A");
                                }
                            } else {
                                obj.put("serial", "");
                                obj.put("warehouse", "");
                                obj.put("location", "");
                                obj.put("batch", "");
                            }
                            if (transtype == -1) {
                                obj.put("stockRate", "-");
                            }
                            obj.put("value", authHandler.round(amount, companyid));
                            if (isExport) {
                                obj.put(Constants.productid, product.getID());
                                obj.put("isQtyAndValuationJSON", 0);//
                                obj.put("transType", transtype);
                            }
                            obj.put("balance", authHandler.round(amount, companyid));
                            obj.put(Constants.productid, product.getID());
                            if (!isFromStockReport && !isInventoryValuation && !isConsolidationStockReport) {
                                dataArr.put(obj);
                            }
                        }
                    }
                }
                /**
                 * Add Final Transaction details only if (On hand quantity is not
                 * zero and serial name is not empty).
                 */
                if ((!(ledgerOnHandQuantity == 0.0 && !StringUtil.isNullOrEmpty(serialNameSearch)) && !isAdvanceSearchForValuation) || (isAdvanceSearchForValuation && ((openingBatch != null && openingBatch.getQuantity() != 0.0) || (detailsMap != null && detailsMap.size() > 0)))) {
                    JSONObject finalRowObj = new JSONObject();
                    grandTotal+=Double.parseDouble(authHandler.formattedAmount(ledgerFinalValuation,companyid));
                    PriceValuationStack.Batch avgCostBatch = stack.getTransactionBatch();
                    finalRowObj.put("pid", !StringUtil.isNullOrEmpty(product.getProductid()) ? product.getProductid() : "");
                    finalRowObj.put("productDesc", "");
                    finalRowObj.put("transactionNumber", "");
                    finalRowObj.put("personCode", "");
                    finalRowObj.put("personName", "");
                    finalRowObj.put("uom", product.getUnitOfMeasure() != null ? product.getUnitOfMeasure().getNameEmptyforNA() : "");
                    finalRowObj.put("received", "");
                    finalRowObj.put("stockRate", "Quantity on Hand: " + authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));
                    finalRowObj.put("value", "Valuation: " + authHandler.formattedAmount(ledgerFinalValuation, companyid));
//                    finalRowObj.put("value", "Valuation: " + (ledgerOnHandQuantity == 0.0?0.0:authHandler.formattedAmount(ledgerFinalValuation, companyid)));
                    finalRowObj.put("ledgerFinalValuation", authHandler.formattedAmount(ledgerFinalValuation, companyid));
//                    finalRowObj.put("ledgerFinalValuation", ledgerOnHandQuantity == 0.0?0.0:authHandler.formattedAmount(ledgerFinalValuation, companyid));
                    finalRowObj.put("grandtotal", authHandler.formattedAmount(grandTotal, companyid));
                    finalRowObj.put("balance", "-");
                    if (isExport) {
                        finalRowObj.put(Constants.productid, product.getID());//
                        finalRowObj.put("QtyOnHandJasper", authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));
                        finalRowObj.put("ValuationJasper", authHandler.formattedAmount(ledgerFinalValuation, companyid));
//                        finalRowObj.put("ValuationJasper", ledgerOnHandQuantity == 0.0?0.0:authHandler.formattedAmount(ledgerFinalValuation, companyid));
//                        finalRowObj.put("stockRate", 0);// commenting for ERM-890 export issue with final valuation row does not get displayed on 21
//                        finalRowObj.put("value", 0);//
                        finalRowObj.put("isQtyAndValuationJSON", 1);
                    }
                    finalRowObj.put(Constants.productid, product.getID());
                    finalRowObj.put("pid", product.getProductid());
                    finalRowObj.put("productname", product.getName());
                    
                    finalRowObj.put("productdesc", StringUtil.isNullOrEmpty(product.getDescription())?"":isExport ? StringUtil.replaceFullHTML(product.getDescription().replace("<br>","\n")) : product.getDescription());
                    finalRowObj.put("productType", product.getProducttype().getName());
                    finalRowObj.put("productTypeID", product.getProducttype().getID());
                    if (product.getValuationMethod() == ValuationMethod.FIFO) {
                        finalRowObj.put("fifo", ledgerFinalValuation);
                    } else {
                        finalRowObj.put("fifo", "NA");
                    }
                    if (product.getValuationMethod() == ValuationMethod.STANDARD) {
                        finalRowObj.put("lifo", ledgerFinalValuation);
                    } else {
                        finalRowObj.put("lifo", "NA");
                    }
                    KwlReturnObject priceResult = accProductObj.getProductPrice(product.getID(), true, null, "-1", product.getCurrency().getCurrencyID());
                    List<Object> priceList = priceResult.getEntityList();
                    double proPrice = 0;
                    if (priceList != null) {
                        for (Object cogsval : priceList) {
                            proPrice = (cogsval == null ? 0.0 : (Double) cogsval);
                        }
                        KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, proPrice, product.getCurrency().getCurrencyID(), null, 0);
                        proPrice = (Double) crresult.getEntityList().get(0);
                    }
                    finalRowObj.put("purchasecost", proPrice);
                    finalRowObj.put("quantity", authHandler.formattedQuantity(ledgerOnHandQuantity,companyid));
                    if (openingBatch != null) {
                        finalRowObj.put("openingstockQty", openingBatch.getQuantity());
                        finalRowObj.put("openingstockvalue", authHandler.formattedAmount(openingBatch.getAmount(), companyid));
                    } else {
                        finalRowObj.put("openingstockvalue", authHandler.formattedAmount((0.0), companyid));
                    }
                    finalRowObj.put("stockInQty", stockInQty);
                    finalRowObj.put("stockOutQty", stockOutQty);
                    finalRowObj.put("evaluationcost", authHandler.formattedAmount(ledgerFinalValuation, companyid));
//                    finalRowObj.put("evaluationcost", ledgerOnHandQuantity == 0.0?0.0:authHandler.formattedAmount(ledgerFinalValuation, companyid));
                    if (product.getValuationMethod() == ValuationMethod.AVERAGE && !product.isIsSerialForProduct() && !product.isIsBatchForProduct()) {
                        finalRowObj.put("avgcost", (avgCostBatch.getPrice() != 0 && ledgerOnHandQuantity != 0) ? avgCostBatch.getPrice() : "N.A");
                        finalRowObj.put("valuation", ledgerFinalValuation);
                    } else if (product.getValuationMethod() == ValuationMethod.AVERAGE && (product.isIsSerialForProduct() || product.isIsBatchForProduct())) {//By Dipak P.
                        finalRowObj.put("valuation", ledgerFinalValuation);
                    } else {
                        finalRowObj.put("valuation", "NA");
                    }
                    requestParams.put("gTotal", grandTotal);
                    dataArr.put(finalRowObj);
                }                
             } // end of else

        } catch (NumberFormatException | ServiceException | JSONException | SessionExpiredException | ParseException | UnsupportedEncodingException ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
     return dataArr;
    }
    /**
     * Function to be used to create transaction JSON using the list of transactions push into the stack
     * @param productJson
     * @param stack
     * @param dataArr
     * @param requestParams
     * @return  JSONArray for all the transactions using productID & stack
     */
    private JSONArray getTransactionJsonForSalesReport(JSONObject productJson, PriceValuationStack stack, JSONArray dataArr, HashMap<String, Object> requestParams) {
        try {
            Map<String, PriceValuationStack.Batch> detailsMap = stack.getAllTransactionBatch();
            if (detailsMap != null) {
                for (Map.Entry<String, PriceValuationStack.Batch> detailMap : detailsMap.entrySet()) {
                    PriceValuationStack.Batch batch = detailMap.getValue();
                    if (batch != null) {
                        if (batch.getDocType() == TransactionBatch.DocType_DO) {
                            /**
                             * Map created for DODetail ID & price.
                             */
                            double price = batch.getPrice();
                            String dodId = detailMap.getKey();
                            requestParams.put(dodId, price);
                        } else if (batch.getDocType() == TransactionBatch.DocType_SALES_RETURN) {
                            /**
                             * Map created for SRDetails ID & price.
                             */
                            double price = batch.getPrice();
                            String srdId = detailMap.getKey();
                            requestParams.put(srdId, price);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dataArr;
    }
    private JSONArray getTransactionJson(JSONObject productJson, PriceValuationStack stack, JSONArray dataArr, HashMap<String, Object> requestParams) {
        try {
            double ledgerOnHandQuantity = 0, ledgerFinalValuation = 0,grandTotal=0;
            String companyid = (String) requestParams.get(Constants.companyid);
            double stockInQty = 0;
            double stockOutQty = 0;
            boolean isInventoryValuation = false, isFromStockReport = false, isFromStockValuationDetail = false, isFromStockValuationSummary = false, isFromStockMovement = false;
            String storageType=null, storageTypeId=null, batchName=null;
            boolean deductSOBlockedQtyFromValuation = false;
            if (requestParams.containsKey("deductSOBlockedQtyFromValuation") && requestParams.get("deductSOBlockedQtyFromValuation") != null) {
                deductSOBlockedQtyFromValuation = Boolean.parseBoolean(requestParams.get("deductSOBlockedQtyFromValuation").toString());
            }
            //This function is called from accReportController.getStockStatus through getInventoryValuationData, to identify it, we have checked reportid
            int reportId = 0; //Check for the report id
            if (requestParams.containsKey(Constants.REPORT_ID) && requestParams.get(Constants.REPORT_ID) != null ) {
                reportId = Integer.parseInt(requestParams.get(Constants.REPORT_ID).toString());
            }          
            
            if (requestParams.containsKey("isInventoryValuation") && requestParams.get("isInventoryValuation") != null) {
                isInventoryValuation = Boolean.parseBoolean(requestParams.get("isInventoryValuation").toString());
            }
            if (requestParams.containsKey("isFromStockReport") && requestParams.get("isFromStockReport") != null) {
                isFromStockReport = Boolean.parseBoolean(requestParams.get("isFromStockReport").toString());
            }
            if (requestParams.containsKey("isFromStockMovement") && requestParams.get("isFromStockMovement") != null) {
                isFromStockMovement = Boolean.parseBoolean(requestParams.get("isFromStockMovement").toString());
            }
            if (requestParams.containsKey("isFromStockValuationDetail") && requestParams.get("isFromStockValuationDetail") != null) {
                isFromStockValuationDetail = Boolean.parseBoolean(requestParams.get("isFromStockValuationDetail").toString());
                storageType = requestParams.get("storageType") != null ? requestParams.get("storageType").toString(): null;
                storageTypeId = requestParams.get("storageTypeId") != null? requestParams.get("storageTypeId").toString(): null;
                batchName = requestParams.get("batchName") != null? requestParams.get("batchName").toString(): null;
            }
            if (requestParams.containsKey("isFromStockValuationSummary") && requestParams.get("isFromStockValuationSummary") != null) {
                isFromStockValuationSummary = Boolean.parseBoolean(requestParams.get("isFromStockValuationSummary").toString());
                storageType = requestParams.get("storageType") != null ? requestParams.get("storageType").toString(): null;
            }
            boolean stockAgeing = false;// Stock Ageing Report Flag
            if (requestParams.containsKey("stockAgeing") && requestParams.get("stockAgeing") != null) {
                stockAgeing = Boolean.parseBoolean(requestParams.get("stockAgeing").toString());
            }
            boolean isFromStockSummaryReport = false;// Stock Ageing Report Flag
            if (requestParams.containsKey("isFromStockSummaryReport") && requestParams.get("isFromStockSummaryReport") != null) {
                isFromStockSummaryReport = Boolean.parseBoolean(requestParams.get("isFromStockSummaryReport").toString());
            }
            boolean isConsolidationStockReport = false;// Stock Ageing Report Flag
            if (requestParams.containsKey("isConsolidationStockReport") && requestParams.get("isConsolidationStockReport") != null) {
                isConsolidationStockReport = Boolean.parseBoolean(requestParams.get("isConsolidationStockReport").toString());
            }
            boolean isStockLedgerDetailedReport = false;// Stock Ageing Report Flag
            if (requestParams.containsKey("isStockLedgerDetailedReport") && requestParams.get("isStockLedgerDetailedReport") != null) {
                isStockLedgerDetailedReport = Boolean.parseBoolean(requestParams.get("isStockLedgerDetailedReport").toString());
            }
            boolean isMaterialInOutReport = false;// Material IN/OUT Report
            if (requestParams.containsKey("isMaterialInOutReport") && requestParams.get("isMaterialInOutReport") != null) {
                isMaterialInOutReport = Boolean.parseBoolean(requestParams.get("isMaterialInOutReport").toString());
            }
            boolean isExport = false;
            if (requestParams.containsKey("isExportPDF") && requestParams.get("isExportPDF") != null) {
                isExport = Boolean.parseBoolean(requestParams.get("isExportPDF").toString());
            }
            boolean isActivateLandedInvAmt = false;
            if (requestParams.containsKey("isactivatelandedinvamt") && requestParams.get("isactivatelandedinvamt") != null) {
                isActivateLandedInvAmt = Boolean.parseBoolean(requestParams.get("isactivatelandedinvamt").toString());
            }
            String serialNameSearch = "";
            if (requestParams.containsKey("serialNameSearch") && requestParams.get("serialNameSearch") != null) {
                serialNameSearch = requestParams.get("serialNameSearch").toString();
            }
             if (requestParams.containsKey("gTotal") && requestParams.get("gTotal") != null) {
                grandTotal = Double.parseDouble(requestParams.get("gTotal").toString());
            }
            boolean isAdvanceSearchForValuation = false;
                        
             double landingcatamt = 0d;
             double avglandedcost = 0d;
             double landingcostvalue = 0d;
             if (requestParams.containsKey(Constants.Acc_Search_Json) && requestParams.get(Constants.Acc_Search_Json) != null && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
                /**
                 * If searchJson is not empty then set
                 * isAdvanceSearchForValuation to true.
                 * <code>isAdvanceSearchForValuation</code> flag is used to
                 * fetch only those transactions for which search criteria is
                 * matching.
                 */
                isAdvanceSearchForValuation = true;
            }
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            if (isFromStockValuationDetail || isFromStockValuationSummary) {
                if(isFromStockValuationSummary && StringUtil.isNullOrEmpty(storageType)){
                    return dataArr;
                }
                Map<String, String> partialStorageDetail = new HashMap();
                
                List<PriceValuationStack.StorageFilter> scList = new ArrayList();
                String storageName = null;
                String storageDesc = null;
                int type = 6;
                try {
                    type = Integer.valueOf(storageType);
                } catch (Exception ex) {
                    type = 6;
                }

                switch (type) {
                    case 1:
                        scList.add(PriceValuationStack.StorageFilter.WAREHOUSE);
                        if (!StringUtil.isNullOrEmpty(storageTypeId)) {
                            partialStorageDetail.put("warehouseId", storageTypeId);
                        }
                        break;
                    case 2:
                        scList.add(PriceValuationStack.StorageFilter.LOCATION);
                        if (!StringUtil.isNullOrEmpty(storageTypeId)) {
                            partialStorageDetail.put("locationId", storageTypeId);
                        }
                        break;
                    case 3:
                        scList.add(PriceValuationStack.StorageFilter.ROW);
                        if (!StringUtil.isNullOrEmpty(storageTypeId)) {
                            partialStorageDetail.put("rowId", storageTypeId);
                        }
                        break;
                    case 4:
                        scList.add(PriceValuationStack.StorageFilter.RACK);
                        if (!StringUtil.isNullOrEmpty(storageTypeId)) {
                            partialStorageDetail.put("rackId", storageTypeId);
                        }
                        break;
                    case 5:
                        scList.add(PriceValuationStack.StorageFilter.BIN);
                        if (!StringUtil.isNullOrEmpty(storageTypeId)) {
                            partialStorageDetail.put("binId", storageTypeId);
                        }
                        break;
                }

                if (isFromStockValuationDetail) {
                    scList.add(PriceValuationStack.StorageFilter.BATCH);
                    if (!StringUtil.isNullOrEmpty(batchName)) {
                        partialStorageDetail.put("batchName", batchName);
                    }
                }
                PriceValuationStack.StorageFilter[] sc = new PriceValuationStack.StorageFilter[scList.size()];
                List<TransactionBatch> tbList = stack.getTransactionBatchList(null, partialStorageDetail, true);
                Map<String, PriceValuationStack.Batch> storewiseValuationDetails = stack.getStorageDetailwiseQuantityBatch(tbList, scList.toArray(sc), false,true);
                KwlReturnObject res = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), requestParams.get(Constants.globalCurrencyKey).toString());
                KWLCurrency currency = (KWLCurrency) res.getEntityList().get(0);
                if (storewiseValuationDetails != null) {
                    String categoryName = "";
                    Map<String, JSONObject> locationSummaryMap = new HashMap();
                    if(isFromStockValuationSummary){
                        for(int i=0; i<dataArr.length(); i++){
                            JSONObject jObj = dataArr.getJSONObject(i);
                            locationSummaryMap.put(jObj.optString("locationid"), jObj);
                        }
                    }
                    for (Map.Entry<String, PriceValuationStack.Batch> detailMap : storewiseValuationDetails.entrySet()) {
                        PriceValuationStack.Batch batch = detailMap.getValue();
                        if (batch != null) {
                            double price = batch.getPrice();
                            double amount = batch.getAmount();
                            double quantity = batch.getQuantity();
                            String serialNames="";
                            for (String serial : batch.getAvailableSerial()) {
                                if (!StringUtil.isNullOrEmpty(serial)) {
                                    res = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), serial);
                                    NewBatchSerial nbs = (NewBatchSerial) res.getEntityList().get(0);
                                    serialNames += nbs.getSerialname().toString()+",";
                                }
                            }
                            if(!StringUtil.isNullOrEmpty(serialNames)){
                                serialNames=serialNames.substring(0, serialNames.length()-1);
                            }
                           if(batch.getQuantity()==0 && batch.getAmount()==0.0){
                                continue;
                            }
                            storageTypeId = "";
                            storageName = "";
                            
                            StoreMaster sm;
                            switch (type) {
                                case 1:
                                    if (!StringUtil.isNullOrEmpty(batch.getWarehouseId())) {
                                        res = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), batch.getWarehouseId());
                                        InventoryWarehouse iw = (InventoryWarehouse) res.getEntityList().get(0);
                                        res = accountingHandlerDAOobj.getObject(Store.class.getName(), batch.getWarehouseId()); // For Jasper Report Description 
                                        Store iwStore = (res != null && res.getEntityList() != null && !res.getEntityList().isEmpty()) ? (Store) res.getEntityList().get(0) : null;
                                        storageTypeId = iw.getId();
                                        storageName = iw.getName();
                                        /**
                                         * Added null check on iwStore object;
                                         * We get null value from database in iwStore object in case of Customer Warehouse (used in sales consignment flow);
                                         * When a warehouse is created from 'Store Master' or from 'Inventory Setup' on UI, entry for warehouse is inserted in both 
                                         * 'InventoryWarehouse' and 'Store' tables with same UUID;
                                         * However entry of a customer warehouse is inserted only in table 'InventoryWarehouse', not in 'Store', due to which we get null 
                                         * object when we try to create object of 'Store' table using ID of a customer warehouse;
                                         * ERP-38563.
                                         */
                                        storageDesc = iwStore != null ? iwStore.getDescription() : "";
                                    }
                                    break;
                                case 2:
                                    if (!StringUtil.isNullOrEmpty(batch.getLocationId())) {
                                        res = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), batch.getLocationId());
                                        InventoryLocation il = (InventoryLocation) res.getEntityList().get(0);
                                        storageTypeId = il.getId();
                                        storageName = il.getName();
                                    }
                                    
                                    break;
                                case 3:
                                    if (!StringUtil.isNullOrEmpty(batch.getRowId())) {
                                        res = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), batch.getRowId());
                                        sm = (StoreMaster) res.getEntityList().get(0);
                                        storageTypeId = sm.getId();
                                        storageName = sm.getName();
                                    }
                                    break;
                                case 4:
                                    if (!StringUtil.isNullOrEmpty(batch.getRackId())) {
                                        res = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), batch.getRackId());
                                        sm = (StoreMaster) res.getEntityList().get(0);
                                        storageTypeId = sm.getId();
                                        storageName = sm.getName();
                                    }
                                    break;
                                case 5:
                                    if (!StringUtil.isNullOrEmpty(batch.getBinId())) {
                                        res = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), batch.getBinId());
                                        sm = (StoreMaster) res.getEntityList().get(0);
                                        storageTypeId = sm.getId();
                                        storageName = sm.getName();
                                    }
                                    break;
                            }
                            
                            if(isFromStockValuationDetail){
//                                if(!StringUtil.isNullOrEmpty(batch.getBatchId())){
//                                    res = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), batch.getBatchId());
//                                    NewProductBatch npb = (NewProductBatch) res.getEntityList().get(0);
//                                    batchName = npb.getBatchname();
//                                }
//                                KwlReturnObject kwlObj = accProductObj.getProductCategoryForDetailsReport(product.getID());   //getting product category
//                                if (kwlObj != null && kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty()) {
//                                    List l1 = kwlObj.getEntityList();
//                                    int len = l1.size();
//                                    boolean isfirst = true;
//                                    for (int i = 0; i < len; i++) {
//                                        ProductCategoryMapping pcm = (ProductCategoryMapping) l1.get(i);
//                                        if (pcm != null && pcm.getProductCategory() != null) {
//                                            String category_Name = pcm.getProductCategory().getValue();
//                                            if (isfirst) {
//                                                categoryName = category_Name;
//                                                isfirst = false;
//                                            } else {
//                                                categoryName += "," + category_Name;
//                                            }
//                                        } else {
//                                            categoryName = "None";
//                                        }
//                                    }
//                                } else {
//                                    categoryName = "None";
//                                }
                                //ERM-447 Inserting landing cost value for Stock Valuation Detail Report
                                JSONObject obj = new JSONObject();
                                if (isActivateLandedInvAmt && batch.getExtraJSON()!=null) {
                                    obj.put("avglandedcost", (authHandler.roundUnitPrice(Math.abs(batch.getPrice()), companyid)));
                                }
                                obj.put(Constants.productid, productJson.getString("productid"));
                                obj.put("productIDJASPER", productJson.getString("productid"));
                                obj.put("productname", productJson.getString("name"));
                                obj.put("uom", productJson.getString("uom"));
                                obj.put("productDesc", StringUtil.isNullOrEmpty(productJson.getString("description"))?"":isExport ? StringUtil.replaceFullHTML(productJson.getString("description").replace("<br>","\n")) : productJson.getString("description"));
//                                obj.put("productCategory", categoryName);
                                obj.put("productBatch", batch.getBatchId());
                                price=quantity!=0?authHandler.round(amount, companyid)/quantity:price;
                                obj.put("rate",authHandler.formattingDecimalForUnitPrice(Math.abs(price), companyid));
                                if (isActivateLandedInvAmt && batch.getDocType() != TransactionBatch.DocType_DO && quantity!=0.0) {
                                    obj.put("rate", authHandler.formattingDecimalForUnitPrice(Math.abs(batch.getWithoutlandedamount() / quantity), companyid));
                                }
                                /**
                                 * Subtracting  SO block qty from total quantity and  Valuation of Blocked SO from  total valuation. 
                                 */
                                if (deductSOBlockedQtyFromValuation) {
                                    String storeId = requestParams.get("storeId") != null ? (String) requestParams.get("storeId") : null;
                                    String locationId = requestParams.get("locationId") != null ? (String) requestParams.get("locationId") : null;
                                    DateFormat dateformat = requestParams.get(Constants.df) != null ? (DateFormat) requestParams.get(Constants.df) : null;
                                    productJson.put("storeId", storeId);
                                    productJson.put("storeId", locationId);
                                    productJson.put("df",dateformat);
                                    String startDateStr = requestParams.get("startdateforsoblockqty")!=null?(String) requestParams.get("startdateforsoblockqty"):"";
                                    String endDateStr = requestParams.get("enddateforsoblockqty")!=null?(String) requestParams.get("enddateforsoblockqty"):"";
                                    productJson.put("startdate",!StringUtil.isNullOrEmpty(startDateStr) ? df.parse(startDateStr) : "");
                                    productJson.put("endate",!StringUtil.isNullOrEmpty(endDateStr) ? df.parse(endDateStr) : "");
                                    productJson.put("companyid",companyid);
                                    JSONObject blockqtyJson = getSOblockqtyJson(productJson, amount, quantity);
                                    amount = amount - (blockqtyJson.optDouble("soledgerValuation", 0) * blockqtyJson.optDouble("blockqty", 0));
                                    quantity = quantity - blockqtyJson.optDouble("blockqty", 0);
                                }
                                obj.put("quantity", authHandler.formattedQuantity(quantity,companyid));
                                obj.put("currencysymbol", currency.getSymbol());
                                obj.put("currencyname", currency.getName());
                                obj.put("categoryName", "Grand");
                                obj.put("totalcategorycost", authHandler.round(amount, companyid));
                                obj.put("locationid",storageTypeId);
                                obj.put("locationName", storageName);
                                obj.put("locationDescription", storageDesc);
                                obj.put("serial", serialNames);
                                obj.put("value", amount);
                                dataArr.put(obj);
                            }else{
                                if (locationSummaryMap.containsKey(storageTypeId)) {
                                    JSONObject obj = locationSummaryMap.get(storageTypeId);
                                    obj.put("value", (Double) (obj.get("value")) + authHandler.round(amount, companyid));
                                } else {
                                    if (!StringUtil.isNullOrEmpty(storageTypeId)) { // ERP-29818
                                        JSONObject obj = new JSONObject();
                                        obj.put("locationid", storageTypeId);
                                        obj.put("locationName", storageName);
                                        obj.put("locationDescription", storageDesc);
                                        obj.put("value", authHandler.round(amount, companyid));
                                        dataArr.put(obj);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (isMaterialInOutReport) {
                String tType = requestParams.get("transactionType") != null ? (String) requestParams.get("transactionType") : null;
                TransactionType transactionType = null;
                if (!StringUtil.isNullOrEmpty(tType) && !tType.equals("ALL")) {
                    transactionType = TransactionType.valueOf(tType);
                }
                String storeId = requestParams.get("store") != null ? (String) requestParams.get("store") : null;
                Map<String, String> partialStorageDetails = new HashMap();
                partialStorageDetails.put("warehouseId", storeId);
                PriceValuationStack.Batch openingBatch = null;
                if (transactionType == null || transactionType == TransactionType.OPENING) {
                    openingBatch = stack.getPartialOpeningTransactionBatch(partialStorageDetails);
                }
                Store iwStore = null;
                if (!StringUtil.isNullOrEmpty(storeId)) {
                    KwlReturnObject res = accountingHandlerDAOobj.getObject(Store.class.getName(), storeId);
                    iwStore = (Store) res.getEntityList().get(0);
                }
                if (openingBatch != null) {
                    /**
                     * Add OpeningBatch Transaction details only if (quantity is
                     * not zero and serial name is not empty).
                     */
                    if (!(openingBatch.getQuantity() == 0.0 && !StringUtil.isNullOrEmpty(serialNameSearch))) {
                        JSONObject jObj = new JSONObject();
                        JSONArray smDetails = new JSONArray();
                        if (openingBatch.getMaterialInOutlocationMap() != null && !openingBatch.getMaterialInOutlocationMap().isEmpty()) {
                            for (Map.Entry<String, Double> entrySet : openingBatch.getMaterialInOutlocationMap().entrySet()) {
                                JSONObject srObject = locationMapBuilder(entrySet);
                                smDetails.put(srObject);
                            }
                        }
                        jObj.put("stockDetails", smDetails);
                        jObj.put("itemcode", productJson.getString("productid"));
                        jObj.put("itemdescription", productJson.getString("description"));
                        jObj.put("itemname", productJson.getString("name"));
                        jObj.put("store", (iwStore == null) ? "" : iwStore.getDescription());
                        jObj.put("assemble", "-");
                        jObj.put("orderuom", productJson.getString("uom"));
                        jObj.put("costcenter", "-");
                        jObj.put("vendor", "-");
                        jObj.put("type", "BACK FORWARD");
                        jObj.put("orderquantity", authHandler.formattedQuantity(openingBatch.getQuantity(),companyid));
                        jObj.put("amount", authHandler.round(openingBatch.getAmount(), companyid));
                        double price = openingBatch.getPrice();
                        jObj.put("avgCost", authHandler.round(price, companyid));
                        jObj.put("orderno", "-");
                        jObj.put("remark", "Back forward transaction balance");
                        jObj.put("isBatchForProduct", productJson.getBoolean("isSerialForProduct"));
                        jObj.put("isSerialForProduct", productJson.getBoolean("isBatchForProduct"));
                        jObj.put("isRowForProduct", productJson.getBoolean("isrowforproduct"));
                        jObj.put("isRackForProduct", productJson.getBoolean("israckforproduct"));
                        jObj.put("isBinForProduct", productJson.getBoolean("isbinforproduct"));
                        jObj.put("moduleName", "-");
                        jObj.put(Constants.productid, productJson.getString("id"));
                        dataArr.put(jObj);
                    }
                }
                Map<String, PriceValuationStack.Batch> allTransactionBatch = null;
                if(transactionType == TransactionType.OPENING){
                    PriceValuationStack.Batch batch = stack.getInitialTransactionBatch(null, partialStorageDetails);
                    JSONObject json = getTransactionJson(productJson, batch, iwStore, df, companyid);
                    dataArr.put(json);
                } else {
                    if (transactionType == null) {
                        if (!isAdvanceSearchForValuation) {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, null, null);
                        } else {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, null, true);
                        }
                    } else if (transactionType == TransactionType.IN) {
                        if (!isAdvanceSearchForValuation) {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, false, null);
                        } else {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, false, true);
                        }
                    } else if (transactionType == TransactionType.OUT) {
                        if (!isAdvanceSearchForValuation) {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, true, null);
                        } else {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, true, true);
                        }
                    }
                    if (allTransactionBatch != null) {
                        SortTransactionMap sortTransactionMap = new SortTransactionMap();
                        allTransactionBatch = sortTransactionMap.getSortedMap(allTransactionBatch);
                        for (Map.Entry<String, PriceValuationStack.Batch> entrySet : allTransactionBatch.entrySet()) {
                            PriceValuationStack.Batch batch = entrySet.getValue();
                            JSONObject json = getTransactionJson(productJson, batch, iwStore, df, companyid);
                            dataArr.put(json);
                        }
                    }
                }
            } else if (isFromStockMovement) {
                String storeId = requestParams.get("storeId") != null ? (String)requestParams.get("storeId") : null;
                String locationId = requestParams.get("locationId") != null ? (String)requestParams.get("locationId") : null;
                Map<String, String> partialStorageDetails = new HashMap();
                if(!StringUtil.isNullOrEmpty(storeId)){
                    partialStorageDetails.put("warehouseId", storeId);
                }
                if(!StringUtil.isNullOrEmpty(storeId)){
                    partialStorageDetails.put("locationId", locationId);
                }
                Map<String, PriceValuationStack.Batch> inTransactions = null;
                if (!isAdvanceSearchForValuation) {
                    inTransactions = stack.getAllTransactionBatches(null, partialStorageDetails, true, null, null, null);
                } else {
                    inTransactions = stack.getAllTransactionBatches(null, partialStorageDetails, true, null, null, true);
                }
                JSONObject productObj = dataArr.optJSONObject(0);
                if(dataArr.optJSONObject(0) == null){
                    productObj = new JSONObject();
                }
                JSONObject obj = new JSONObject();
                productObj.put(productJson.getString("id"), obj);
                for (Map.Entry<String, PriceValuationStack.Batch> entry : inTransactions.entrySet()) {
                    PriceValuationStack.Batch batch = entry.getValue();
                    if (batch != null) {
                        obj.put(entry.getKey(), batch.getPrice());
                    }
                }
                dataArr.put(productObj);
            } else if (isFromStockSummaryReport) {
                JSONObject obj = new JSONObject();
                obj.put("itemcode", productJson.getString("productid"));
                obj.put("itemdescription", productJson.getString("description"));
                obj.put("itemname", productJson.getString("name"));
                PriceValuationStack.Batch openingBatch = null;
                if (!isAdvanceSearchForValuation) {
                    openingBatch = stack.getOpeningTransactionBatch();
                } else {
                    /**
                     * Get opening batch for transactions for which search
                     * criteria is matching.
                     */
                    openingBatch = stack.getOpeningTransBatch(true);
                }
                if (openingBatch != null) {
                    obj.put("openingqty", authHandler.formattedQuantity(openingBatch.getQuantity(), companyid));
                    ledgerFinalValuation += openingBatch.getAmount();
                    ledgerOnHandQuantity += openingBatch.getQuantity();
                }else{
                    obj.put("openingqty", authHandler.formattedQuantity(0.0, companyid));
                }
                
                Map<String, PriceValuationStack.Batch> detailsMap = null;
                if (!isAdvanceSearchForValuation) {
                    detailsMap = stack.getAllTransactionBatches(null, null, false, null);
                } else {
                    detailsMap = stack.getAllTransactionBatches(null, null, false, false, null, true);
                }
                SortTransactionMap sortTransactionMap = new SortTransactionMap();
                detailsMap = sortTransactionMap.getSortedMap(detailsMap);
                double goodsReceiptOrderQty=0;
                double deliveryOrderQty=0;
                double stockAdjustmentQty=0;
                double stockTransferOutQty=0;
                double stockTransferInQty=0;
                if (openingBatch == null) {
                    goodsReceiptOrderQty += openingBatch.getQuantity();
                }
                if (detailsMap != null && detailsMap.size() > 0) {
                    for (Map.Entry<String, PriceValuationStack.Batch> detailMap : detailsMap.entrySet()) {
                        PriceValuationStack.Batch batch = detailMap.getValue();
                        if (batch != null) {
                            double price = batch.getPrice();
                            double amount = batch.getAmount();
                            int transtype = batch.getDocType();
                            ledgerFinalValuation += amount;
                            ledgerOnHandQuantity += batch.getQuantity();
                            //ERP-34330 Stock Adjustment generated from DO or GRN QC flow should be shown under those respective transactions only 
                            if (transtype == TransactionBatch.DocType_SA_OUT) {
                                String transid = batch.getBillid();
                                Map<String, Object> paramMap = new HashMap<>();
                                paramMap.put("id", transid);
                                //check if current sa is from  approved / rejected / repaired  DO qc entries and change transtype if atleast one is true
                                Object qckwl = kwlCommonTablesDAOObj.getRequestedObjectFields(StockAdjustment.class, new String[]{"rejectedApprovedDODQCISTMapping","rejectedDODQCISTMapping","approvedDODQCISTMapping"}, paramMap);
                                Object qcarr[] = (Object[]) (qckwl!=null?qckwl:null);
                                if(qcarr!=null && (qcarr[0]!=null ||qcarr[1]!=null || qcarr[2]!=null)){
                                    transtype = TransactionBatch.DocType_DO;
                                }
                            }
                            
                            if (transtype == TransactionBatch.DocType_GRN || transtype == TransactionBatch.DocType_OPENING || transtype == TransactionBatch.DocType_ASSEMBLY_MAIN) {
                                goodsReceiptOrderQty += batch.getQuantity();
                            } else if (transtype == TransactionBatch.DocType_PURCHASE_RETURN) {
                                goodsReceiptOrderQty -= Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_DO || transtype == TransactionBatch.DocType_ASSEMBLY_SUB) {
                                deliveryOrderQty += Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_SALES_RETURN) {
                                deliveryOrderQty -= Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_SA_IN) {
                                stockAdjustmentQty += Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_SA_OUT) {
                                stockAdjustmentQty -= Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_SR_ISSUE || transtype == TransactionBatch.DocType_IN_ISSUE || transtype == TransactionBatch.DocType_IST_ISSUE || transtype == TransactionBatch.DocType_ILT_ISSUE || transtype == TransactionBatch.DocType_WO_OUT) {
                                stockTransferOutQty += Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_SR_COLLECT || transtype == TransactionBatch.DocType_IN_COLLECT || transtype == TransactionBatch.DocType_IST_COLLECT || transtype == TransactionBatch.DocType_ILT_COLLECT || transtype == TransactionBatch.DocType_WO_IN) {
                                stockTransferInQty += Math.abs(batch.getQuantity());
                            }
                        }
                    }
                }
                obj.put("uom", productJson.getString("uom"));
                obj.put("goodsreceiptorderqty", authHandler.formattedAmount(goodsReceiptOrderQty, companyid));
                obj.put("deliveryorderqty", authHandler.formattedAmount(deliveryOrderQty, companyid));
                obj.put("stocktransferINqty", authHandler.formattedAmount(stockTransferInQty, companyid));
                obj.put("stocktransferOUTqty", authHandler.formattedAmount(stockTransferOutQty, companyid));
                obj.put("stockadjustmentqty", authHandler.formattedAmount(stockAdjustmentQty, companyid));
                obj.put("amount", authHandler.formattedAmount(ledgerFinalValuation, companyid));
                obj.put("balanceqty", authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));
                if (ledgerOnHandQuantity != 0) {
                    double avgcost = 0;
                    avgcost = ledgerFinalValuation / ledgerOnHandQuantity;
                    obj.put("avgcost", authHandler.formattedAmount(avgcost, companyid));
                } else {
                    obj.put("avgcost", authHandler.formattedAmount(0, companyid));
                }
                Map<String, Object> reqprm = new HashMap<String, Object>();
                reqprm.put("company", companyid);
                reqprm.put("product", productJson.getString("id"));
                reqprm.put("store", (String) requestParams.get("store"));
                Date todate = (Date) requestParams.get("toDate");
                String transDate = authHandler.getDateOnlyFormat().format(todate);
                todate = authHandler.getDateOnlyFormat().parse(transDate);
                reqprm.put("businessDate", todate);
                double cyclyCountQty = accProductObj.getCycleCountQuantity(reqprm);
                if (cyclyCountQty == 0) {
                    obj.put("cyclecountqty", "N/A");
                    obj.put("varianceqty", "N/A");
                } else {
                    obj.put("cyclecountqty", cyclyCountQty);
                    double variance = ledgerOnHandQuantity - cyclyCountQty;
                    obj.put("varianceqty", variance);
                }
               dataArr.put(obj);
            } else if (stockAgeing) {
                int duration = 45;
                boolean isSummary = requestParams.get("isSummary") == null || StringUtil.isNullOrEmpty(requestParams.get("isSummary").toString()) ? false : Boolean.parseBoolean(requestParams.get("isSummary").toString());
                if (isSummary) {
                    duration = requestParams.get("duration") == null ? 0 : Integer.parseInt(requestParams.get("duration").toString());
                }
                String asofDateString = (String) requestParams.get(Constants.REQ_enddate);
                Date asofDate = null;
                if (!StringUtil.isNullOrEmpty(asofDateString) && df != null) {
                    asofDate = df.parse(asofDateString);
                }
                Date curDate = asofDate;
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                Calendar cal3 = Calendar.getInstance();
                Calendar cal4 = Calendar.getInstance();
                Calendar cal5 = Calendar.getInstance();
                Calendar cal6 = Calendar.getInstance();
                Calendar cal7 = Calendar.getInstance();

                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal1.add(Calendar.DAY_OF_YEAR, -duration);
                cal2.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 6));
                cal7.add(Calendar.DAY_OF_YEAR, -(365));
                Date cdate1 = cal1.getTime();
                String caldate1 = df.format(cdate1);

                Date cal3Date = null;
                Date cal4Date = null;
                Date cal5Date = null;
                Date cal6Date = null;
                Date cal7Date = null;

                double quantitydue1 = 0, quantitydue2 = 0, quantitydue3 = 0, quantitydue4 = 0, quantitydue5 = 0, quantitydue6 = 0, quantitydue7 = 0, quantitydue8 = 0;

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
                try {
                    cdate1 = df.parse(caldate1);
                } catch (ParseException ex) {
                    cdate1 = cal1.getTime();
                }
                Date cdate2 = cal2.getTime();
                String caldate2 = df.format(cdate2);
                try {
                    cdate2 = df.parse(caldate2);
                } catch (ParseException ex) {
                    cdate2 = cal2.getTime();
                }
                Map<String, PriceValuationStack.Batch> inTransactions = null;
                if (!isAdvanceSearchForValuation) {
                    inTransactions = stack.getAllTransactionBatches(null, null, null, false);
                } else {
                    inTransactions = stack.getAllTransactionBatches(null, null, false, null, false, true);
                }
                SortTransactionMap sortTransactionMap = new SortTransactionMap();
                inTransactions = sortTransactionMap.getSortedMap(inTransactions);
                double totalInAmount = 0.0;
                double totalInQty = 0.0;
                double totalAvgPrice = 0.0;
                Map<String, String> storagekeyWiseAvgValueMap = new HashMap();
                for (Map.Entry<String, PriceValuationStack.Batch> entry : inTransactions.entrySet()) {
                    PriceValuationStack.Batch batch = entry.getValue();
                    if (batch != null) {
                        JSONObject obj = new JSONObject();
                        obj.put("pid", productJson.getString("productid"));
                        obj.put("productname", productJson.getString("name"));
                        obj.put("productDesc", productJson.getString("description"));
                        obj.put("unit", productJson.getString("uom"));
                        obj.put("uom", productJson.getString("uom"));
                        obj.put("transactionNumber", StringUtil.isNullOrEmpty(batch.getTransactionNo()) ? "" : batch.getTransactionNo());
                        obj.put("transactionDate", batch.getTransactionDate() != null ? df.format(batch.getTransactionDate()) : "");
                        obj.put("transactionType", batch.getDocType());
                        if (batch.getDocType() == TransactionBatch.DocType_INITIAL) {
                            obj.put("transactionType", "Opening");
                        } else if (batch.getDocType() == TransactionBatch.DocType_GRN) {
                            obj.put("transactionType", "Goods Receipt");
                        } else if (batch.getDocType() == TransactionBatch.DocType_SA_IN) {
                            obj.put("transactionType", "Stock Adjustment");
                        } else if (batch.getDocType() == TransactionBatch.DocType_PURCHASE_RETURN) {
                            obj.put("transactionType", "Purchase Return");
                        } else if (batch.getDocType() == TransactionBatch.DocType_SALES_RETURN) {
                            obj.put("transactionType", "Sales Return");
                        } else if (batch.getDocType() == TransactionBatch.DocType_ASSEMBLY_MAIN) {
                            obj.put("transactionType", "Assembly");
                        } else if (batch.getDocType() == TransactionBatch.DocType_IN_ISSUE) {
                            obj.put("transactionType", "Issue Note Issue");
                        } else if (batch.getDocType() == TransactionBatch.DocType_IN_COLLECT) {
                            obj.put("transactionType", "Issue Note Collect");
                        } else if (batch.getDocType() == TransactionBatch.DocType_IST_COLLECT) {
                            obj.put("transactionType", "Inter Store Transfer");
                        } else if (batch.getDocType() == TransactionBatch.DocType_IST_COLLECT) {
                            obj.put("transactionType", "Inter Store Transfer");
                        } else if (batch.getDocType() == TransactionBatch.DocType_ILT_ISSUE) {
                            obj.put("transactionType", "Inter Location Transfer Collect Issue");
                        } else if (batch.getDocType() == TransactionBatch.DocType_ILT_COLLECT) {
                            obj.put("transactionType", "Inter Location Transfer Collect");
                        } else if (batch.getDocType() == TransactionBatch.DocType_WO_IN){ /* In Stock Aging Report show transaction type:Work Order */
                            obj.put("transactionType", "Work Order");
                        }

                        Map<String, String> StorageDetail = new HashMap();

                        StorageDetail.put("warehouseId", (batch.getWarehouseMap() != null) && (batch.getWarehouseMap().size() > 0) ? (String) batch.getWarehouseMap().keySet().iterator().next() : "");
                        StorageDetail.put("locationId", (batch.getLocationMap() != null) && (batch.getLocationMap().size() > 0) ? (String) batch.getLocationMap().keySet().iterator().next() : "");
                        StorageDetail.put("rowId", batch.getRowId());
                        StorageDetail.put("rackId", batch.getRackId());
                        StorageDetail.put("binId", batch.getBinId());
                        String key = PriceValuationStack.getStoregeKey(StorageDetail);

                        double outQuantity = 0.0;

                        double avgPrice = 0.0;
                        if (batch.getOutTransactionQtyAmountMap() != null) {
                            for (Map.Entry<String, List<Double>> outEntry : batch.getOutTransactionQtyAmountMap().entrySet()) {
                                List<Double> value = (List) outEntry.getValue();
                                if ((value != null) && (!value.isEmpty())) {
                                    outQuantity += ((Double) value.get(0)).doubleValue();
                                    if (ValuationMethod.getValue(productJson.getInt("valuationmethod")) == ValuationMethod.AVERAGE) {
                                        avgPrice = ((Double) value.get(1)).doubleValue();
                                    }
                                }
                            }
                        }

                        double remainingQty = batch.getQuantity() - outQuantity;
                        if ((ValuationMethod.getValue(productJson.getInt("valuationmethod")) == ValuationMethod.AVERAGE) && (outQuantity > 0.0) && (remainingQty > 0.0)) {
                            totalInAmount += batch.getQuantity() * batch.getPrice();
                            totalInQty += batch.getQuantity();
                            storagekeyWiseAvgValueMap.put(key, totalInAmount + "," + totalInQty);
                        }

                        double amount = batch.getPrice() * remainingQty;
                        if (ValuationMethod.getValue(productJson.getInt("valuationmethod")) == ValuationMethod.AVERAGE) {
                            amount = remainingQty * (avgPrice != 0 ? avgPrice : 0);
                        }
                        if (batch.getTransactionDate() != null && (cdate1.before(batch.getTransactionDate()) || cdate1.equals(batch.getTransactionDate()))) {
                            obj.put("quantity1", authHandler.formattedQuantity(remainingQty, companyid));
                            quantitydue1 += remainingQty;
                            obj.put("amount1", amount);
                        } else if (batch.getTransactionDate() != null && ((cdate2.before(batch.getTransactionDate()) || cdate2.equals(batch.getTransactionDate())) && cdate1.after(batch.getTransactionDate()))) {
                            obj.put("quantity2", authHandler.formattedQuantity(remainingQty, companyid));
                            quantitydue2 += remainingQty;
                            obj.put("amount2", amount);
                        } else if (batch.getTransactionDate() != null && ((cal3Date.before(batch.getTransactionDate()) || cal3Date.equals(batch.getTransactionDate())) && cdate2.after(batch.getTransactionDate()))) {
                            quantitydue3 += remainingQty;
                        } else if (batch.getTransactionDate() != null && ((cal4Date.before(batch.getTransactionDate()) || cal4Date.equals(batch.getTransactionDate())) && cal3Date.after(batch.getTransactionDate()))) {
                            quantitydue4 += remainingQty;
                        } else if (batch.getTransactionDate() != null && ((cal5Date.before(batch.getTransactionDate()) || cal5Date.equals(batch.getTransactionDate())) && cal4Date.after(batch.getTransactionDate()))) {
                            quantitydue5 += remainingQty;
                        } else if (batch.getTransactionDate() != null && ((cal6Date.before(batch.getTransactionDate()) || cal6Date.equals(batch.getTransactionDate())) && cal5Date.after(batch.getTransactionDate()))) {
                            quantitydue6 += remainingQty;
                        } else if (batch.getTransactionDate() != null && ((cal7Date.before(batch.getTransactionDate()) || cal7Date.equals(batch.getTransactionDate())) && cal6Date.after(batch.getTransactionDate()))) {
                            quantitydue7 += remainingQty;
                        } else {
                            obj.put("quantity3", authHandler.formattedQuantity(remainingQty, companyid));
                            obj.put("amount3", amount);
                            quantitydue8 += remainingQty;
                        }
                        if (remainingQty != 0.0 && !isSummary) {
                            /**
                             * Don't include transaction which are having
                             * remaining quantity as 0. (ERP-33899)
                             */
                            dataArr.put(obj);
                        }
                    }
                }
                if (isSummary) {
                    JSONObject obj = new JSONObject();
                    obj.put("pid", productJson.getString("productid"));
                    obj.put("productname", productJson.getString("name"));
                    obj.put("productDesc", productJson.getString("description"));
                    obj.put("quantitydue1", authHandler.formattedQuantity(quantitydue1, companyid));
                    obj.put("quantitydue2", authHandler.formattedQuantity(quantitydue2, companyid));
                    obj.put("quantitydue3", authHandler.formattedQuantity(quantitydue3, companyid));
                    obj.put("quantitydue4", authHandler.formattedQuantity(quantitydue4, companyid));
                    obj.put("quantitydue5", authHandler.formattedQuantity(quantitydue5, companyid));
                    obj.put("quantitydue6", authHandler.formattedQuantity(quantitydue6, companyid));
                    obj.put("quantitydue7", authHandler.formattedQuantity(quantitydue7, companyid));
                    obj.put("quantitydue8", authHandler.formattedQuantity(quantitydue8, companyid));
                    dataArr.put(obj);
                }
            } else if (reportId == Constants.STOCK_STATUS_REPORT_ID) {
                dataArr = getTransactionJsonForStockStatusReport(productJson, stack, dataArr, requestParams);
            }// end of isFromStatusReporyt else if
             else {
                PriceValuationStack.Batch openingBatch = null;
                if (!isAdvanceSearchForValuation) {
                    openingBatch = stack.getOpeningTransactionBatch();
                } else {
                    /**
                     * Get opening batch for transactions for which search
                     * criteria is matching.
                     */
                    openingBatch = stack.getOpeningTransBatch(true);
                }
                String categoryName = "";
                KwlReturnObject kwlObj = accProductObj.getProductCategoryForDetailsReport(productJson.getString("id"));   //getting product category
                if (kwlObj != null && kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty()) {
                    List l1 = kwlObj.getEntityList();
                    int len = l1.size();
                    boolean isfirst = true;
                    for (int i = 0; i < len; i++) {
                        ProductCategoryMapping pcm = (ProductCategoryMapping) l1.get(i);
                        if (pcm != null && pcm.getProductCategory() != null) {
                            String category_Name = pcm.getProductCategory().getValue();
                            if (isfirst) {
                                categoryName = category_Name;
                                isfirst = false;
                            } else {
                                categoryName += "," + category_Name;
                            }
                        } else {
                            categoryName = "None";
                        }
                    }
                } else {
                    categoryName = "None";
                }
                /**
                 * Show opening batch only if quantity is not equal to 0(When
                 * isAdvanceSearchForValuation is true).
                 */
                if ((openingBatch != null && !isAdvanceSearchForValuation) || (isAdvanceSearchForValuation && openingBatch!=null && openingBatch.getQuantity() != 0.0)) {
                    /**
                     * Add OpeningBatch Transaction details only if (quantity is
                     * not zero and serial name is not empty).
                     */
                    if (!(openingBatch.getQuantity()==0.0 && !StringUtil.isNullOrEmpty(serialNameSearch))) {
                        JSONObject OpeningRowObj = new JSONObject();
                        OpeningRowObj.put("pid", productJson.getString("productid"));
//                    OpeningRowObj.put("productDesc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));//desc
                        OpeningRowObj.put("productDesc", StringUtil.isNullOrEmpty(productJson.getString("description"))?"":isExport ? StringUtil.replaceFullHTML(productJson.getString("description").replace("<br>","\n")) : productJson.getString("description"));
                        OpeningRowObj.put("transactionNumber", "Opening");
                        OpeningRowObj.put("personCode", "");
                        OpeningRowObj.put("uom", productJson.getString("uom"));
                        OpeningRowObj.put("personName", "");
                        OpeningRowObj.put("received", openingBatch.getQuantity());
                        OpeningRowObj.put("stockRate", authHandler.formattingDecimalForUnitPrice(openingBatch.getPrice(), companyid));
                        OpeningRowObj.put("value", authHandler.formattedAmount(openingBatch.getAmount(), companyid));
                        OpeningRowObj.put("productcategory",categoryName);
                        OpeningRowObj.put("balance", "-");
                        OpeningRowObj.put(Constants.productid, productJson.getString("id"));
                        if (isStockLedgerDetailedReport) { /* Stock Ledger Detailed Report*/
                            if (openingBatch.getLocationMap() != null) {
                                Map<String, Double> locationMap = openingBatch.getLocationMap();
                                StringBuilder locationBuilder = locationKeyBuilder(locationMap);
                                if (locationBuilder.length() > 0) {
                                    OpeningRowObj.put("location", locationBuilder.toString());
                                }
                            } else {
                                OpeningRowObj.put("location", "N/A");
                            }

                            if (openingBatch.getWarehouseMap() != null) {
                                Map<String, Double> warehouseMap = openingBatch.getWarehouseMap();
                                StringBuilder warehouseBuilder = warehouseKeyBuilder(warehouseMap);
                                if (warehouseBuilder.length() > 0) {
                                    OpeningRowObj.put("warehouse", warehouseBuilder.toString());
                                }
                            } else {
                                OpeningRowObj.put("warehouse", "N/A");
                            }
                            if (openingBatch.getBatchMap() != null) {
                                Map<String, Double> batchMap = openingBatch.getBatchMap();
                                StringBuilder batchBuilder = batchKeyBuilder(batchMap);
                                if (batchBuilder.length() > 0) {
                                    OpeningRowObj.put("batch", batchBuilder.toString());
                                }
                            } else {
                                OpeningRowObj.put("batch", "N/A");
                            }
                            if (openingBatch.getSerialMap() != null) {
                                Map<String, Double> serialMap = openingBatch.getSerialMap();
                                StringBuilder serialBuilder = serialKeyBuilder(serialMap);
                                if (serialBuilder.length() > 0) {
                                    OpeningRowObj.put("serial", serialBuilder.toString());
                                }

                            } else {
                                OpeningRowObj.put("serial", "N/A");
                            }
                        } else {
                            OpeningRowObj.put("batch", "");
                            OpeningRowObj.put("warehouse", "");
                            OpeningRowObj.put("location", "");
                            OpeningRowObj.put("serial", "");
                        }
                        if (isExport) {
                            OpeningRowObj.put("productDesc", URLEncoder.encode(productJson.getString("description"), "UTF-8"));//desc
                            OpeningRowObj.put("productDesc", StringUtil.isNullOrEmpty(productJson.getString("description"))?"":isExport ? StringUtil.replaceFullHTML(productJson.getString("description").replace("<br>","\n")) : productJson.getString("description"));
                            OpeningRowObj.put(Constants.productid, productJson.getString("id"));
                            OpeningRowObj.put("isQtyAndValuationJSON", 0);//
                        }
                        if (!isFromStockReport && !isInventoryValuation && !isConsolidationStockReport) {
                            dataArr.put(OpeningRowObj);
                        }
                        ledgerFinalValuation = Double.parseDouble(authHandler.getFormattedUnitPrice(ledgerFinalValuation,companyid)) +Double.parseDouble(authHandler.getFormattedUnitPrice(openingBatch.getAmount(),companyid));
                        ledgerOnHandQuantity += openingBatch.getQuantity();
                        OpeningRowObj.put("balanceqty", authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));//
                        OpeningRowObj.put("avgcostll", authHandler.formattingDecimalForUnitPrice(openingBatch.getPrice(), companyid));//
                    }
                }
                Map<String, PriceValuationStack.Batch> detailsMap = null;
                if (!isAdvanceSearchForValuation) {
                    detailsMap = stack.getAllTransactionBatches(null, null, false, null);
                } else {
                    /**
                     * Get period transactions map for transactions for which
                     * fulfills the search criteria.
                     */
                    detailsMap = stack.getAllTransactionBatches(null, null, false, false, null, true);
                }
                SortTransactionMap sortTransactionMap = new SortTransactionMap();
                detailsMap = sortTransactionMap.getSortedMap(detailsMap);
                if (detailsMap != null) {
                    for (Map.Entry<String, PriceValuationStack.Batch> detailMap : detailsMap.entrySet()) {
//                            String detailID = detailMap.getKey();
                        PriceValuationStack.Batch batch = detailMap.getValue();
                        if (batch != null) {
                            double price = batch.getPrice();
                            double amount = batch.getAmount();
                            int transtype = batch.getDocType();
                            if (isActivateLandedInvAmt && batch.getDocType() == TransactionBatch.DocType_GRN) {
                                price = batch.getWithoutlanded();
                            }
                            ledgerFinalValuation=Double.parseDouble(authHandler.getFormattedUnitPrice(ledgerFinalValuation,companyid)) + Double.parseDouble(authHandler.getFormattedUnitPrice(amount,companyid));
                            ledgerOnHandQuantity += batch.getQuantity();
                            JSONObject obj = new JSONObject();
                            obj.put("pid", productJson.getString("productid"));
                            obj.put("balanceqty", authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));//
                            obj.put("avgcostll", ledgerOnHandQuantity!=0?ledgerFinalValuation/ledgerOnHandQuantity:0);//
                            obj.put("doctype", PriceValuationStack.getTransactionModule(batch.getDocType()));//
//                            obj.put("productDesc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));//desc
                            obj.put("productDesc", StringUtil.isNullOrEmpty(productJson.getString("description"))?"":isExport ? StringUtil.replaceFullHTML(productJson.getString("description").replace("<br>","\n")) : productJson.getString("description"));
                            if (transtype != -1) {
                                obj.put("transactionDate", batch.getTransactionDate() != null ? df.format(batch.getTransactionDate()) : "");
                            }
                            if (isActivateLandedInvAmt && batch.getDocType() == TransactionBatch.DocType_GRN) {
                                /**
                                 * Iterate on landing category as JSON contains (uuid:value) pairs.
                                 */
                                JSONObject batchjson = batch.getExtraJSON();
                                if (batchjson != null) {
                                    String[] landedpairs = batchjson.optString("landingcostpairs", "").split(",");
                                    avglandedcost = batchjson.optDouble("avglandedcost", 0.0);
                                    if (avglandedcost != 0.0) {
                                        obj.put("avglandedcost", (authHandler.roundUnitPrice(Math.abs(avglandedcost), companyid)));
                                    }
                                    for (String landedcategorypair : landedpairs) {
                                        String[] landedpair = landedcategorypair.split(":");
                                        if (landedpair.length == 2) {
                                            double expensecost = Double.parseDouble(StringUtil.isNullOrEmpty(landedpair[1]) ? "0" : landedpair[1]);
                                            if (obj.has(landedpair[0])) {
                                                expensecost += obj.optDouble(landedpair[0], 0);
                                            }
                                            obj.put(landedpair[0], expensecost);
                                        }
                                    }
                                }
                            }
                            obj.put("transactionNumber", StringUtil.isNullOrEmpty(batch.getTransactionNo()) ? "" : batch.getTransactionNo());
                            obj.put("uom", productJson.getString("uom"));
                            obj.put("personCode", StringUtil.isNullOrEmpty(batch.getPersonCode()) ? "" : batch.getPersonCode());
                            obj.put("personName", StringUtil.isNullOrEmpty(batch.getPersonName()) ? "" : batch.getPersonName());
                            if (transtype == 0 || transtype == 1 || transtype == 4 || transtype == 6 || transtype == -1 || transtype == 7|| transtype == 10|| transtype == 12|| transtype == 14|| transtype == 16 || transtype == 17) {/*ERM-564 Stock Ledger Report 17(Stock IN) for Receive */
                                obj.put("received", batch.getQuantity());
                                stockInQty += batch.getQuantity();
                            } else {
                                obj.put("delivered", batch.getQuantity());

                                stockOutQty -= batch.getQuantity();

                            }
                            obj.put("memo", StringUtil.isNullOrEmpty(batch.getMemo()) ? "" : batch.getMemo());
                            obj.put("transactiontype", transtype);
                            obj.put("billid", batch.getBillid());
                            obj.put("stockRate", authHandler.formattingDecimalForUnitPrice(price, companyid));
                            if (isStockLedgerDetailedReport) { /* Stock Ledger Detailed Report*/
                                if (batch.getLocationMap() != null) {
                                    Map<String, Double> locationMap = batch.getLocationMap();
                                    StringBuilder locationBuilder = locationKeyBuilder(locationMap);
                                    if (locationBuilder.length() > 0) {
                                        obj.put("location", locationBuilder.toString());
                                    }
                                } else {
                                    obj.put("location", "N/A");
                                }

                                if (batch.getBatchMap() != null) {
                                    Map<String, Double> batchMap = batch.getBatchMap();
                                    StringBuilder batchBuilder = batchKeyBuilder(batchMap);
                                    if (batchBuilder.length() > 0) {
                                        obj.put("batch", batchBuilder.toString());
                                    }
                                } else {
                                    obj.put("batch", "N/A");
                                }
                                if (batch.getWarehouseMap() != null) {
                                    Map<String, Double> warehouseMap = batch.getWarehouseMap();
                                    StringBuilder warehouseBuilder = warehouseKeyBuilder(warehouseMap);
                                    if (warehouseBuilder.length() > 0) {
                                        obj.put("warehouse", warehouseBuilder.toString());
                                    }
                                } else {
                                    obj.put("warehouse", "N/A");
                                }
                                if (batch.getSerialMap() != null) {
                                    Map<String, Double> serialMap = batch.getSerialMap();
                                    StringBuilder serialBuilder = serialKeyBuilder(serialMap);
                                    if (serialBuilder.length() > 0) {
                                        obj.put("serial", serialBuilder.toString());
                                    }
                                } else {
                                    obj.put("serial", "N/A");
                                }
                            } else {
                                obj.put("serial", "");
                                obj.put("warehouse", "");
                                obj.put("location", "");
                                obj.put("batch", "");
                            }
                            if (transtype == -1) {
                                obj.put("stockRate", "-");
                            }
                            obj.put("value", authHandler.round(amount, companyid));
                            if (isExport) {
                                if (requestParams.containsKey("filetype") && requestParams.get("filetype") != null && !requestParams.get("filetype").equals("csv")){
                                    obj.put("value", authHandler.round(amount < 0 ? -amount : amount, companyid));
                                }
                                obj.put("isQtyAndValuationJSON", 0);//
                                obj.put("transType", transtype);
                            }
                            obj.put("balance", authHandler.round(amount, companyid));
                            obj.put(Constants.productid, productJson.getString("id"));
                            if (!isFromStockReport && !isInventoryValuation && !isConsolidationStockReport) {
                                dataArr.put(obj);
                            }
                        }
                    }
                }
                 /**
                 * Subtracting SO block qty from total quantity and Valuation of
                 * Blocked SO from total valuation and adding row for Blocked SOales Orders in Stock ledger.
                 */
                if (deductSOBlockedQtyFromValuation) {
                    String storeId = requestParams.get("storeId") != null ? (String) requestParams.get("storeId") : null;
                    String locationId = requestParams.get("locationId") != null ? (String) requestParams.get("locationId") : null;
                    DateFormat dateformat = requestParams.get(Constants.df) != null ? (DateFormat) requestParams.get(Constants.df) : null;
                    productJson.put("storeId", storeId);
                    productJson.put("storeId", locationId);
                    productJson.put("df", dateformat);
                    String startDateStr = requestParams.get("startdateforsoblockqty") != null ? (String) requestParams.get("startdateforsoblockqty") : "";
                    String endDateStr = requestParams.get("enddateforsoblockqty") != null ? (String) requestParams.get("enddateforsoblockqty") : "";
                    productJson.put("startdate", !StringUtil.isNullOrEmpty(startDateStr) ? df.parse(startDateStr) : "");
                    productJson.put("enddate",  !StringUtil.isNullOrEmpty(endDateStr) ? df.parse(endDateStr) : "");
                    productJson.put("companyid", companyid);
                    JSONObject blockqtyJson = getSOblockqtyJson(productJson, ledgerFinalValuation, ledgerOnHandQuantity);
                    ledgerFinalValuation = ledgerFinalValuation - (blockqtyJson.optDouble("soledgerValuation",0) * blockqtyJson.optDouble("blockqty",0));
                    ledgerOnHandQuantity = ledgerOnHandQuantity - blockqtyJson.optDouble("blockqty", 0);
                    if (blockqtyJson.length()>0 && !isFromStockReport && !isInventoryValuation && !isConsolidationStockReport) {
                        dataArr.put(blockqtyJson);
                    }
                }
                /**
                 * Add Final Transaction details only if (On hand quantity is not
                 * zero and serial name is not empty).
                 */
                if ((!(ledgerOnHandQuantity == 0.0 && !StringUtil.isNullOrEmpty(serialNameSearch)) && !isAdvanceSearchForValuation) || (isAdvanceSearchForValuation && ((openingBatch != null && openingBatch.getQuantity() != 0.0) || (detailsMap != null && detailsMap.size() > 0)))) {
                    JSONObject finalRowObj = new JSONObject();
                    grandTotal+=Double.parseDouble(authHandler.formattedAmount(ledgerFinalValuation,companyid));
                    PriceValuationStack.Batch avgCostBatch = stack.getTransactionBatch();
                    finalRowObj.put("pid", productJson.getString("productid"));
                    finalRowObj.put("productDesc", "");
                    finalRowObj.put("transactionNumber", "");
                    finalRowObj.put("personCode", "");
                    finalRowObj.put("personName", "");
                    finalRowObj.put("uom", productJson.getString("uom"));
                    finalRowObj.put("received", "");
                    finalRowObj.put("stockRate", "Quantity on Hand: " + authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));
                    finalRowObj.put("value", "Valuation: " + authHandler.formattedAmount(ledgerFinalValuation, companyid));
//                    finalRowObj.put("value", "Valuation: " + (ledgerOnHandQuantity == 0.0?0.0:authHandler.formattedAmount(ledgerFinalValuation, companyid)));
                    finalRowObj.put("ledgerFinalValuation", authHandler.formattedAmount(ledgerFinalValuation, companyid));
//                    finalRowObj.put("ledgerFinalValuation", ledgerOnHandQuantity == 0.0?0.0:authHandler.formattedAmount(ledgerFinalValuation, companyid));
                    finalRowObj.put("grandtotal", authHandler.formattedAmount(grandTotal, companyid));
                    finalRowObj.put("balance", "-");
                    if (isExport) {
                        finalRowObj.put("QtyOnHandJasper", authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));
                        finalRowObj.put("ValuationJasper", authHandler.formattedAmount(ledgerFinalValuation, companyid));
//                        finalRowObj.put("ValuationJasper", ledgerOnHandQuantity == 0.0?0.0:authHandler.formattedAmount(ledgerFinalValuation, companyid));
//                        finalRowObj.put("stockRate", 0);// commenting for ERM-890 export issue with final valuation row does not get displayed on 21
//                        finalRowObj.put("value", 0);//
                        finalRowObj.put("isQtyAndValuationJSON", 1);
                    }
                    finalRowObj.put(Constants.productid, productJson.getString("id"));
                    finalRowObj.put("pid", productJson.getString("productid"));
                    finalRowObj.put("productname", productJson.getString("name"));
                    
                    finalRowObj.put("productdesc", StringUtil.isNullOrEmpty(productJson.getString("description"))?"":isExport ? StringUtil.replaceFullHTML(productJson.getString("description").replace("<br>","\n")) : productJson.getString("description"));
                    finalRowObj.put("productType", productJson.getString("producttypename"));
                    finalRowObj.put("productTypeID", productJson.getString("producttypeid"));
                    if (ValuationMethod.getValue(productJson.getInt("valuationmethod")) == ValuationMethod.FIFO) {
                        finalRowObj.put("fifo", ledgerFinalValuation);
                    } else {
                        finalRowObj.put("fifo", "NA");
                    }
                    if (ValuationMethod.getValue(productJson.getInt("valuationmethod")) == ValuationMethod.STANDARD) {
                        finalRowObj.put("lifo", ledgerFinalValuation);
                    } else {
                        finalRowObj.put("lifo", "NA");
                    }
                    KwlReturnObject priceResult = accProductObj.getProductPrice(productJson.getString("id"), true, null, "-1", productJson.getString("prodcurrency"));
                    List<Object> priceList = priceResult.getEntityList();
                    double proPrice = 0;
                    if (priceList != null) {
                        for (Object cogsval : priceList) {
                            proPrice = (cogsval == null ? 0.0 : (Double) cogsval);
                        }
                        KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, proPrice, productJson.getString("prodcurrency"), null, 0);
                        proPrice = (Double) crresult.getEntityList().get(0);
                    }
                    finalRowObj.put("purchasecost", proPrice);
                    finalRowObj.put("quantity", authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));
                    if (openingBatch != null) {
                        finalRowObj.put("openingstockQty", authHandler.formattedQuantity(openingBatch.getQuantity(), companyid));
                        finalRowObj.put("openingstockvalue", authHandler.formattedAmount(openingBatch.getAmount(), companyid));
                    } else {
                        finalRowObj.put("openingstockvalue", authHandler.formattedAmount((0.0), companyid));
                    }
                    finalRowObj.put("stockInQty", authHandler.formattedQuantity(stockInQty,companyid));
                    finalRowObj.put("stockOutQty", authHandler.formattedQuantity(stockOutQty,companyid));
                    finalRowObj.put("evaluationcost", authHandler.formattedAmount(ledgerFinalValuation, companyid));
//                    finalRowObj.put("evaluationcost", ledgerOnHandQuantity == 0.0?0.0:authHandler.formattedAmount(ledgerFinalValuation, companyid));
                    if (ValuationMethod.getValue(productJson.getInt("valuationmethod")) == ValuationMethod.AVERAGE && !productJson.getBoolean("isSerialForProduct") && !productJson.getBoolean("isBatchForProduct")) {
                        finalRowObj.put("avgcost", (avgCostBatch.getPrice() != 0 && ledgerOnHandQuantity != 0) ? avgCostBatch.getPrice() : "N.A");
                        finalRowObj.put("valuation", ledgerFinalValuation);
                    } else if (ValuationMethod.getValue(productJson.getInt("valuationmethod")) == ValuationMethod.AVERAGE && (productJson.getBoolean("isSerialForProduct") || productJson.getBoolean("isBatchForProduct"))) {//By Dipak P.
                        finalRowObj.put("valuation", ledgerFinalValuation);
                    } else {
                        finalRowObj.put("valuation", "NA");
                    }
                    requestParams.put("gTotal", grandTotal);
                    dataArr.put(finalRowObj);
                }                
             } // end of else

        } catch (NumberFormatException | ServiceException | JSONException | SessionExpiredException | ParseException | UnsupportedEncodingException ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
     return dataArr;
    }
    /**
     * Method to get block qty of each product id passed in productJson
     * @param productJson
     * @param ledgerFinalValuation
     * @param ledgerOnHandQuantity
     * @return
     * @throws JSONException
     * @throws ServiceException
     * @throws ParseException.
     */
    private JSONObject getSOblockqtyJson(JSONObject productJson, double ledgerFinalValuation, double ledgerOnHandQuantity) throws JSONException, ServiceException, ParseException {
        String storeId = "", locationId = "";
        String companyid = "";
        DateFormat df = null;
        Store store = null;
        boolean isFinancialReport = false;
        if (productJson.has(Constants.companyid)) {
            companyid = productJson.optString(Constants.companyid, "");
        }
        if (productJson.has("storeId")) {
            storeId = productJson.optString("storeId", "");
        }
        if (productJson.has("locationId")) {
            locationId = productJson.optString("locationId", "");
        }
        if (productJson.has(Constants.df)) {
            df = (DateFormat) productJson.opt(Constants.df);
        }
        if (!StringUtil.isNullOrEmpty(storeId)) {
            store = storeService.getStoreById(storeId);
        }
        Location location = null;
        if (!StringUtil.isNullOrEmpty(locationId)) {
            location = locationService.getLocation(locationId);
        }
        Date startDate = null;
        Date endDate = null;
        if (productJson.has("startdate") && !StringUtil.isNullOrEmpty(productJson.optString("startdate"))) {
            startDate = (Date) productJson.opt("startdate");
        }
        if (productJson.has("enddate") &&  !StringUtil.isNullOrEmpty(productJson.optString("enddate"))) {
            endDate = (Date) productJson.opt("enddate");
        }
        if (productJson.has("isFinancialReport")) {
            isFinancialReport = productJson.optBoolean("isFinancialReport",false);
        }
        JSONObject blockqtyJson = new JSONObject();
        blockqtyJson.put("companyid", companyid);
        blockqtyJson.put(Constants.productid, productJson.optString("id"));
        blockqtyJson.put("isFinancialReport", isFinancialReport);
        blockqtyJson = accProductObj.getProductBlockedQuantitybyWarehouseLocation(blockqtyJson, store, location, startDate, endDate);
        /**
         * Creating row for block qty sales orders in stock ledger report.
         */
        if (blockqtyJson.length() > 0) {
            blockqtyJson.put("orderdate", blockqtyJson.opt("orderdate") != null ? blockqtyJson.opt("orderdate") : null);
            double soRate = ledgerOnHandQuantity != 0 ? ledgerFinalValuation / (ledgerOnHandQuantity) : 0;
            blockqtyJson.put("stockRate", authHandler.round(soRate, companyid));
            blockqtyJson.put("delivered", "-" + authHandler.formattedQuantity(blockqtyJson.optDouble("blockqty"),companyid));
            blockqtyJson.put("transactiontype", 0);
            blockqtyJson.put("pid", productJson.optString("productid"));
            blockqtyJson.put("balanceqty", authHandler.formattedQuantity(ledgerOnHandQuantity - blockqtyJson.optDouble("blockqty"), companyid));//
            blockqtyJson.put("avgcostll", authHandler.round(soRate * blockqtyJson.optDouble("blockqty"), companyid));
            blockqtyJson.put("transactionDate", (endDate != null && endDate.before(new Date())) ? df.format(endDate) : df.format(new Date()));
            blockqtyJson.put("balance", "-");
            blockqtyJson.put("productDesc", productJson.optString("description", ""));
            blockqtyJson.put("productid", productJson.optString("id"));
            blockqtyJson.put("uom", productJson.optString("uom", ""));
            blockqtyJson.put("value", "-"+authHandler.round(soRate * blockqtyJson.optDouble("blockqty", 0), companyid));
            blockqtyJson.put("transactionNumber", "Blocked Sales order");
            blockqtyJson.put("doctype", "Sales order");
            blockqtyJson.put("soledgerValuation", authHandler.round(soRate, companyid));

        }
        return blockqtyJson;
    }
    private JSONArray getTransactionJSON(JSONObject product, PriceValuationStack stack, JSONArray dataArr, HashMap<String, Object> requestParams) {
        try {
            double ledgerOnHandQuantity = 0, ledgerFinalValuation = 0,grandTotal=0;
            String companyid = (String) requestParams.get(Constants.companyid);
            double stockInQty = 0;
            double stockOutQty = 0;
            boolean isInventoryValuation = false, isFromStockReport = false, isFromStockValuationDetail = false, isFromStockValuationSummary = false, isFromStockMovement = false;
            String storageType=null, storageTypeId=null, batchName=null;

            //This function is called from accReportController.getStockStatus through getInventoryValuationData, to identify it, we have checked reportid
            int reportId = 0; //Check for the report id
            if (requestParams.containsKey(Constants.REPORT_ID) && requestParams.get(Constants.REPORT_ID) != null ) {
                reportId = Integer.parseInt(requestParams.get(Constants.REPORT_ID).toString());
            }          
            
            if (requestParams.containsKey("isInventoryValuation") && requestParams.get("isInventoryValuation") != null) {
                isInventoryValuation = Boolean.parseBoolean(requestParams.get("isInventoryValuation").toString());
            }
            if (requestParams.containsKey("isFromStockReport") && requestParams.get("isFromStockReport") != null) {
                isFromStockReport = Boolean.parseBoolean(requestParams.get("isFromStockReport").toString());
            }
            if (requestParams.containsKey("isFromStockMovement") && requestParams.get("isFromStockMovement") != null) {
                isFromStockMovement = Boolean.parseBoolean(requestParams.get("isFromStockMovement").toString());
            }
            if (requestParams.containsKey("isFromStockValuationDetail") && requestParams.get("isFromStockValuationDetail") != null) {
                isFromStockValuationDetail = Boolean.parseBoolean(requestParams.get("isFromStockValuationDetail").toString());
                storageType = requestParams.get("storageType") != null ? requestParams.get("storageType").toString(): null;
                storageTypeId = requestParams.get("storageTypeId") != null? requestParams.get("storageTypeId").toString(): null;
                batchName = requestParams.get("batchName") != null? requestParams.get("batchName").toString(): null;
            }
            if (requestParams.containsKey("isFromStockValuationSummary") && requestParams.get("isFromStockValuationSummary") != null) {
                isFromStockValuationSummary = Boolean.parseBoolean(requestParams.get("isFromStockValuationSummary").toString());
                storageType = requestParams.get("storageType") != null ? requestParams.get("storageType").toString(): null;
            }
            boolean stockAgeing = false;// Stock Ageing Report Flag
            if (requestParams.containsKey("stockAgeing") && requestParams.get("stockAgeing") != null) {
                stockAgeing = Boolean.parseBoolean(requestParams.get("stockAgeing").toString());
            }
            boolean isFromStockSummaryReport = false;// Stock Ageing Report Flag
            if (requestParams.containsKey("isFromStockSummaryReport") && requestParams.get("isFromStockSummaryReport") != null) {
                isFromStockSummaryReport = Boolean.parseBoolean(requestParams.get("isFromStockSummaryReport").toString());
            }
            boolean isConsolidationStockReport = false;// Stock Ageing Report Flag
            if (requestParams.containsKey("isConsolidationStockReport") && requestParams.get("isConsolidationStockReport") != null) {
                isConsolidationStockReport = Boolean.parseBoolean(requestParams.get("isConsolidationStockReport").toString());
            }
            boolean isStockLedgerDetailedReport = false;// Stock Ageing Report Flag
            if (requestParams.containsKey("isStockLedgerDetailedReport") && requestParams.get("isStockLedgerDetailedReport") != null) {
                isStockLedgerDetailedReport = Boolean.parseBoolean(requestParams.get("isStockLedgerDetailedReport").toString());
            }
            boolean isMaterialInOutReport = false;// Material IN/OUT Report
            if (requestParams.containsKey("isMaterialInOutReport") && requestParams.get("isMaterialInOutReport") != null) {
                isMaterialInOutReport = Boolean.parseBoolean(requestParams.get("isMaterialInOutReport").toString());
            }
            boolean isExport = false;
            if (requestParams.containsKey("isExportPDF") && requestParams.get("isExportPDF") != null) {
                isExport = Boolean.parseBoolean(requestParams.get("isExportPDF").toString());
            }
            boolean isActivateLandedInvAmt = false;
            if (requestParams.containsKey("isactivatelandedinvamt") && requestParams.get("isactivatelandedinvamt") != null) {
                isActivateLandedInvAmt = Boolean.parseBoolean(requestParams.get("isactivatelandedinvamt").toString());
            }
            String serialNameSearch = "";
            if (requestParams.containsKey("serialNameSearch") && requestParams.get("serialNameSearch") != null) {
                serialNameSearch = requestParams.get("serialNameSearch").toString();
            }
             if (requestParams.containsKey("gTotal") && requestParams.get("gTotal") != null) {
                grandTotal = Double.parseDouble(requestParams.get("gTotal").toString());
            }
            boolean isAdvanceSearchForValuation = false;
            if (requestParams.containsKey(Constants.Acc_Search_Json) && requestParams.get(Constants.Acc_Search_Json) != null && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
                /**
                 * If searchJson is not empty then set
                 * isAdvanceSearchForValuation to true.
                 * <code>isAdvanceSearchForValuation</code> flag is used to
                 * fetch only those transactions for which search criteria is
                 * matching.
                 */
                isAdvanceSearchForValuation = true;
            }
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            if (isFromStockValuationDetail || isFromStockValuationSummary) {
                if(isFromStockValuationSummary && StringUtil.isNullOrEmpty(storageType)){
                    return dataArr;
                }
                Map<String, String> partialStorageDetail = new HashMap();
                
                List<PriceValuationStack.StorageFilter> scList = new ArrayList();
                String storageName = null;
                String storageDesc = null;
                int type = 6;
                try {
                    type = Integer.valueOf(storageType);
                } catch (Exception ex) {
                    type = 6;
                }

                switch (type) {
                    case 1:
                        scList.add(PriceValuationStack.StorageFilter.WAREHOUSE);
                        if (!StringUtil.isNullOrEmpty(storageTypeId)) {
                            partialStorageDetail.put("warehouseId", storageTypeId);
                        }
                        break;
                    case 2:
                        scList.add(PriceValuationStack.StorageFilter.LOCATION);
                        if (!StringUtil.isNullOrEmpty(storageTypeId)) {
                            partialStorageDetail.put("locationId", storageTypeId);
                        }
                        break;
                    case 3:
                        scList.add(PriceValuationStack.StorageFilter.ROW);
                        if (!StringUtil.isNullOrEmpty(storageTypeId)) {
                            partialStorageDetail.put("rowId", storageTypeId);
                        }
                        break;
                    case 4:
                        scList.add(PriceValuationStack.StorageFilter.RACK);
                        if (!StringUtil.isNullOrEmpty(storageTypeId)) {
                            partialStorageDetail.put("rackId", storageTypeId);
                        }
                        break;
                    case 5:
                        scList.add(PriceValuationStack.StorageFilter.BIN);
                        if (!StringUtil.isNullOrEmpty(storageTypeId)) {
                            partialStorageDetail.put("binId", storageTypeId);
                        }
                        break;
                }

                if (isFromStockValuationDetail) {
                    scList.add(PriceValuationStack.StorageFilter.BATCH);
                    if (!StringUtil.isNullOrEmpty(batchName)) {
                        partialStorageDetail.put("batchName", batchName);
                    }
                }
                PriceValuationStack.StorageFilter[] sc = new PriceValuationStack.StorageFilter[scList.size()];
                List<TransactionBatch> tbList = stack.getTransactionBatchList(null, partialStorageDetail, true);
                Map<String, PriceValuationStack.Batch> storewiseValuationDetails = stack.getStorageDetailwiseQuantityBatch(tbList, scList.toArray(sc), false,true);
                KwlReturnObject res = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), requestParams.get(Constants.globalCurrencyKey).toString());
                KWLCurrency currency = (KWLCurrency) res.getEntityList().get(0);
                if (storewiseValuationDetails != null) {
                    String categoryName = "";
                    Map<String, JSONObject> locationSummaryMap = new HashMap();
                    if(isFromStockValuationSummary){
                        for(int i=0; i<dataArr.length(); i++){
                            JSONObject jObj = dataArr.getJSONObject(i);
                            locationSummaryMap.put(jObj.optString("locationid"), jObj);
                        }
                    }
                    for (Map.Entry<String, PriceValuationStack.Batch> detailMap : storewiseValuationDetails.entrySet()) {
                        PriceValuationStack.Batch batch = detailMap.getValue();
                        if (batch != null) {
                            double price = batch.getPrice();
                            double amount = batch.getAmount();
                            double quantity = batch.getQuantity();
                            String serialNames="";
                            for (String serial : batch.getAvailableSerial()) {
                                if (!StringUtil.isNullOrEmpty(serial)) {
                                    res = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), serial);
                                    NewBatchSerial nbs = (NewBatchSerial) res.getEntityList().get(0);
                                    serialNames += nbs.getSerialname().toString()+",";
                                }
                            }
                            if(!StringUtil.isNullOrEmpty(serialNames)){
                                serialNames=serialNames.substring(0, serialNames.length()-1);
                            }
                           if(batch.getQuantity()==0 && batch.getAmount()==0.0){
                                continue;
                            }
                            storageTypeId = "";
                            storageName = "";
                            
                            StoreMaster sm;
                            switch (type) {
                                case 1:
                                    if (!StringUtil.isNullOrEmpty(batch.getWarehouseId())) {
                                        res = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), batch.getWarehouseId());
                                        InventoryWarehouse iw = (InventoryWarehouse) res.getEntityList().get(0);
                                        res = accountingHandlerDAOobj.getObject(Store.class.getName(), batch.getWarehouseId()); // For Jasper Report Description 
                                        Store iwStore = (Store) res.getEntityList().get(0);
                                        storageTypeId = iw.getId();
                                        storageName = iw.getName();
                                        storageDesc = iwStore.getDescription();
                                    }
                                    break;
                                case 2:
                                    if (!StringUtil.isNullOrEmpty(batch.getLocationId())) {
                                        res = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), batch.getLocationId());
                                        InventoryLocation il = (InventoryLocation) res.getEntityList().get(0);
                                        storageTypeId = il.getId();
                                        storageName = il.getName();
                                    }
                                    
                                    break;
                                case 3:
                                    if (!StringUtil.isNullOrEmpty(batch.getRowId())) {
                                        res = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), batch.getRowId());
                                        sm = (StoreMaster) res.getEntityList().get(0);
                                        storageTypeId = sm.getId();
                                        storageName = sm.getName();
                                    }
                                    break;
                                case 4:
                                    if (!StringUtil.isNullOrEmpty(batch.getRackId())) {
                                        res = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), batch.getRackId());
                                        sm = (StoreMaster) res.getEntityList().get(0);
                                        storageTypeId = sm.getId();
                                        storageName = sm.getName();
                                    }
                                    break;
                                case 5:
                                    if (!StringUtil.isNullOrEmpty(batch.getBinId())) {
                                        res = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), batch.getBinId());
                                        sm = (StoreMaster) res.getEntityList().get(0);
                                        storageTypeId = sm.getId();
                                        storageName = sm.getName();
                                    }
                                    break;
                            }
                            
                            if(isFromStockValuationDetail){
                                
                                JSONObject obj = new JSONObject();
                                obj.put(Constants.productid, product.getString("id"));
                                obj.put("productIDJASPER", product.getString("id"));
                                obj.put("productname", product.getString("name"));
                                obj.put("uom", product.optString("uom"));
                                obj.put("productDesc", product.optString("description"));
//                                obj.put("productCategory", categoryName);
                                obj.put("productBatch", batch.getBatchId());
                                obj.put("quantity", quantity);
                                price=authHandler.round(amount, companyid)/quantity;
                                obj.put("rate",authHandler.roundUnitPrice(Math.abs(price), companyid));
                                obj.put("currencysymbol", currency.getSymbol());
                                obj.put("currencyname", currency.getName());
                                obj.put("categoryName", "Grand");
                                obj.put("totalcategorycost", authHandler.round(amount, companyid));
                                obj.put("locationid",storageTypeId);
                                obj.put("locationName", storageName);
                                obj.put("locationDescription", storageDesc);
                                obj.put("serial", serialNames);
                                obj.put("value", authHandler.round(amount, companyid));
                                dataArr.put(obj);
                            }else{
                                if (locationSummaryMap.containsKey(storageTypeId)) {
                                    JSONObject obj = locationSummaryMap.get(storageTypeId);
                                    obj.put("value", (Double) (obj.get("value")) + authHandler.round(amount, companyid));
                                } else {
                                    if (!StringUtil.isNullOrEmpty(storageTypeId)) { // ERP-29818
                                        JSONObject obj = new JSONObject();
                                        obj.put("locationid", storageTypeId);
                                        obj.put("locationName", storageName);
                                        obj.put("locationDescription", storageDesc);
                                        obj.put("value", authHandler.round(amount, companyid));
                                        dataArr.put(obj);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (isMaterialInOutReport) {
                String tType = requestParams.get("transactionType") != null ? (String) requestParams.get("transactionType") : null;
                TransactionType transactionType = null;
                if (!StringUtil.isNullOrEmpty(tType) && !tType.equals("ALL")) {
                    transactionType = TransactionType.valueOf(tType);
                }
                String storeId = requestParams.get("store") != null ? (String) requestParams.get("store") : null;
                Map<String, String> partialStorageDetails = new HashMap();
                partialStorageDetails.put("warehouseId", storeId);
                PriceValuationStack.Batch openingBatch = null;
                if (transactionType == null || transactionType == TransactionType.OPENING) {
                    openingBatch = stack.getPartialOpeningTransactionBatch(partialStorageDetails);
                }
                Store iwStore = null;
                if (!StringUtil.isNullOrEmpty(storeId)) {
                    KwlReturnObject res = accountingHandlerDAOobj.getObject(Store.class.getName(), storeId);
                    iwStore = (Store) res.getEntityList().get(0);
                }
                if (openingBatch != null) {
                    /**
                     * Add OpeningBatch Transaction details only if (quantity is
                     * not zero and serial name is not empty).
                     */
                    if (!(openingBatch.getQuantity() == 0.0 && !StringUtil.isNullOrEmpty(serialNameSearch))) {
                        JSONObject jObj = new JSONObject();
                        JSONArray smDetails = new JSONArray();
                        if (openingBatch.getMaterialInOutlocationMap() != null && !openingBatch.getMaterialInOutlocationMap().isEmpty()) {
                            for (Map.Entry<String, Double> entrySet : openingBatch.getMaterialInOutlocationMap().entrySet()) {
                                JSONObject srObject = locationMapBuilder(entrySet);
                                smDetails.put(srObject);
                            }
                        }
                        jObj.put("stockDetails", smDetails);
                        jObj.put("itemcode", product.getString("productid"));
                        jObj.put("itemdescription", product.optString("description"));
                        jObj.put("itemname", product.optString("name"));
                        jObj.put("store", (iwStore == null) ? "" : iwStore.getDescription());
                        jObj.put("assemble", "-");
                        jObj.put("orderuom", product.optString("uom"));
                        jObj.put("costcenter", "-");
                        jObj.put("vendor", "-");
                        jObj.put("type", "BACK FORWARD");
                        jObj.put("orderquantity", openingBatch.getQuantity());
                        jObj.put("amount", authHandler.round(openingBatch.getAmount(), companyid));
                        double price = openingBatch.getPrice();
                        jObj.put("avgCost", authHandler.round(price, companyid));
                        jObj.put("orderno", "-");
                        jObj.put("remark", "Back forward transaction balance");
                        jObj.put("isBatchForProduct", product.getBoolean("isBatchForProduct")); ;
                        jObj.put("isSerialForProduct", product.getBoolean("isSerialForProduct"));
                        jObj.put("isRowForProduct", product.getBoolean("isrowforproduct"));
                        jObj.put("isRackForProduct", product.getBoolean("israckforproduct"));
                        jObj.put("isBinForProduct", product.getBoolean("isbinforproduct"));
                        jObj.put("moduleName", "-");
                        jObj.put(Constants.productid, product.getString("id"));
                        dataArr.put(jObj);
                    }
                }
                Map<String, PriceValuationStack.Batch> allTransactionBatch = null;
                if(transactionType == TransactionType.OPENING){
                    PriceValuationStack.Batch batch = stack.getInitialTransactionBatch(null, partialStorageDetails);
                    JSONObject json = getTransactionJSON(product, batch, iwStore, df, companyid);
                    dataArr.put(json);
                } else {
                    if (transactionType == null) {
                        if (!isAdvanceSearchForValuation) {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, null, null);
                        } else {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, null, true);
                        }
                    } else if (transactionType == TransactionType.IN) {
                        if (!isAdvanceSearchForValuation) {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, false, null);
                        } else {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, false, true);
                        }
                    } else if (transactionType == TransactionType.OUT) {
                        if (!isAdvanceSearchForValuation) {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, true, null);
                        } else {
                            allTransactionBatch = stack.getAllTransactionBatches(null, partialStorageDetails, true, false, true, true);
                        }
                    }
                    if (allTransactionBatch != null) {
                        SortTransactionMap sortTransactionMap = new SortTransactionMap();
                        allTransactionBatch = sortTransactionMap.getSortedMap(allTransactionBatch);
                        for (Map.Entry<String, PriceValuationStack.Batch> entrySet : allTransactionBatch.entrySet()) {
                            PriceValuationStack.Batch batch = entrySet.getValue();
                            JSONObject json = getTransactionJSON(product, batch, iwStore, df, companyid);
                            dataArr.put(json);
                        }
                    }
                }
            } else if (isFromStockMovement) {
                String storeId = requestParams.get("storeId") != null ? (String)requestParams.get("storeId") : null;
                String locationId = requestParams.get("locationId") != null ? (String)requestParams.get("locationId") : null;
                Map<String, String> partialStorageDetails = new HashMap();
                if(!StringUtil.isNullOrEmpty(storeId)){
                    partialStorageDetails.put("warehouseId", storeId);
                }
                if(!StringUtil.isNullOrEmpty(storeId)){
                    partialStorageDetails.put("locationId", locationId);
                }
                Map<String, PriceValuationStack.Batch> inTransactions = null;
                if (!isAdvanceSearchForValuation) {
                    inTransactions = stack.getAllTransactionBatches(null, partialStorageDetails, true, null, null, null);
                } else {
                    inTransactions = stack.getAllTransactionBatches(null, partialStorageDetails, true, null, null, true);
                }
                JSONObject productObj = dataArr.optJSONObject(0);
                if(dataArr.optJSONObject(0) == null){
                    productObj = new JSONObject();
                }
                JSONObject obj = new JSONObject();
                productObj.put(product.getString("id"), obj);
                for (Map.Entry<String, PriceValuationStack.Batch> entry : inTransactions.entrySet()) {
                    PriceValuationStack.Batch batch = entry.getValue();
                    if (batch != null) {
                        obj.put(entry.getKey(), batch.getPrice());
                    }
                }
                dataArr.put(productObj);
            } else if (isFromStockSummaryReport) {
                JSONObject obj = new JSONObject();
                obj.put("itemcode", product.getString("productid"));
                obj.put("itemdescription", product.optString("description"));
                obj.put("itemname", product.getString("name"));
                PriceValuationStack.Batch openingBatch = null;
                if (!isAdvanceSearchForValuation) {
                    openingBatch = stack.getOpeningTransactionBatch();
                } else {
                    /**
                     * Get opening batch for transactions for which search
                     * criteria is matching.
                     */
                    openingBatch = stack.getOpeningTransBatch(true);
                }
                if (openingBatch != null) {
                    obj.put("openingqty", authHandler.formattedQuantity(openingBatch.getQuantity(), companyid));
                    ledgerFinalValuation += openingBatch.getAmount();
                    ledgerOnHandQuantity += openingBatch.getQuantity();
                }else{
                    obj.put("openingqty", authHandler.formattedQuantity(0.0, companyid));
                }
                
                Map<String, PriceValuationStack.Batch> detailsMap = null;
                if (!isAdvanceSearchForValuation) {
                    detailsMap = stack.getAllTransactionBatches(null, null, false, null);
                } else {
                    detailsMap = stack.getAllTransactionBatches(null, null, false, false, null, true);
                }
                SortTransactionMap sortTransactionMap = new SortTransactionMap();
                detailsMap = sortTransactionMap.getSortedMap(detailsMap);
                double goodsReceiptOrderQty=0;
                double deliveryOrderQty=0;
                double stockAdjustmentQty=0;
                double stockTransferOutQty=0;
                double stockTransferInQty=0;
                if (openingBatch == null) {
                    goodsReceiptOrderQty += openingBatch.getQuantity();
                }
                if (detailsMap != null && detailsMap.size() > 0) {
                    for (Map.Entry<String, PriceValuationStack.Batch> detailMap : detailsMap.entrySet()) {
                        PriceValuationStack.Batch batch = detailMap.getValue();
                        if (batch != null) {
                            double price = batch.getPrice();
                            double amount = batch.getAmount();
                            int transtype = batch.getDocType();
                            ledgerFinalValuation += amount;
                            ledgerOnHandQuantity += batch.getQuantity();
                            if (transtype == TransactionBatch.DocType_GRN || transtype == TransactionBatch.DocType_OPENING || transtype == TransactionBatch.DocType_ASSEMBLY_MAIN) {
                                goodsReceiptOrderQty += batch.getQuantity();
                            } else if (transtype == TransactionBatch.DocType_PURCHASE_RETURN) {
                                goodsReceiptOrderQty -= Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_DO || transtype == TransactionBatch.DocType_ASSEMBLY_SUB) {
                                deliveryOrderQty += Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_SALES_RETURN) {
                                deliveryOrderQty -= Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_SA_IN) {
                                stockAdjustmentQty += Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_SA_OUT) {
                                stockAdjustmentQty -= Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_SR_ISSUE || transtype == TransactionBatch.DocType_IN_ISSUE || transtype == TransactionBatch.DocType_IST_ISSUE || transtype == TransactionBatch.DocType_ILT_ISSUE) {
                                stockTransferOutQty += Math.abs(batch.getQuantity());
                            } else if (transtype == TransactionBatch.DocType_SR_COLLECT || transtype == TransactionBatch.DocType_IN_COLLECT || transtype == TransactionBatch.DocType_IST_COLLECT || transtype == TransactionBatch.DocType_ILT_COLLECT) {
                                stockTransferInQty += Math.abs(batch.getQuantity());
                            }
                        }
                    }
                }
                obj.put("uom", product.optString("uom"));
                obj.put("goodsreceiptorderqty", authHandler.formattedAmount(goodsReceiptOrderQty, companyid));
                obj.put("deliveryorderqty", authHandler.formattedAmount(deliveryOrderQty, companyid));
                obj.put("stocktransferINqty", authHandler.formattedAmount(stockTransferInQty, companyid));
                obj.put("stocktransferOUTqty", authHandler.formattedAmount(stockTransferOutQty, companyid));
                obj.put("stockadjustmentqty", authHandler.formattedAmount(stockAdjustmentQty, companyid));
                obj.put("amount", authHandler.formattedAmount(ledgerFinalValuation, companyid));
                obj.put("balanceqty", authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));
                if (ledgerOnHandQuantity != 0) {
                    double avgcost = 0;
                    avgcost = ledgerFinalValuation / ledgerOnHandQuantity;
                    obj.put("avgcost", authHandler.formattedAmount(avgcost, companyid));
                } else {
                    obj.put("avgcost", authHandler.formattedAmount(0, companyid));
                }
                Map<String, Object> reqprm = new HashMap<String, Object>();
                reqprm.put("company", companyid);
                reqprm.put("product", product.getString("id"));
                reqprm.put("store", (String) requestParams.get("store"));
                Date todate = (Date) requestParams.get("toDate");
                String transDate = authHandler.getDateOnlyFormat().format(todate);
                todate = authHandler.getDateOnlyFormat().parse(transDate);
                reqprm.put("businessDate", todate);
                double cyclyCountQty = accProductObj.getCycleCountQuantity(reqprm);
                if (cyclyCountQty == 0) {
                    obj.put("cyclecountqty", "N/A");
                    obj.put("varianceqty", "N/A");
                } else {
                    obj.put("cyclecountqty", cyclyCountQty);
                    double variance = ledgerOnHandQuantity - cyclyCountQty;
                    obj.put("varianceqty", variance);
                }
               dataArr.put(obj);
            } else if (stockAgeing) {
                int duration = 45;
                String asofDateString = (String) requestParams.get(Constants.REQ_enddate);
                Date asofDate = null;
                if (!StringUtil.isNullOrEmpty(asofDateString) && df != null) {
                    asofDate = df.parse(asofDateString);
                }
                Date curDate = asofDate;
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal1.add(Calendar.DAY_OF_YEAR, -duration);
                cal2.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                Date cdate1 = cal1.getTime();
                String caldate1 = df.format(cdate1);
                try {
                    cdate1 = df.parse(caldate1);
                } catch (ParseException ex) {
                    cdate1 = cal1.getTime();
                }
                Date cdate2 = cal2.getTime();
                String caldate2 = df.format(cdate2);
                try {
                    cdate2 = df.parse(caldate2);
                } catch (ParseException ex) {
                    cdate2 = cal2.getTime();
                }
                Map<String, PriceValuationStack.Batch> inTransactions = null;
                if (!isAdvanceSearchForValuation) {
                    inTransactions = stack.getAllTransactionBatches(null, null, null, false);
                } else {
                    inTransactions = stack.getAllTransactionBatches(null, null, false, null, false, true);
                }
                SortTransactionMap sortTransactionMap = new SortTransactionMap();
                inTransactions = sortTransactionMap.getSortedMap(inTransactions);
                for (Map.Entry<String, PriceValuationStack.Batch> entry : inTransactions.entrySet()) {
                    PriceValuationStack.Batch batch = entry.getValue();
                    if (batch != null) {
                        JSONObject obj = new JSONObject();
                        obj.put("pid", product.getString("id"));
                        obj.put("productname", product.getString("name"));
                        obj.put("productDesc", product.optString("description"));
                        obj.put("unit", product.optString("uom"));
                        obj.put("uom", product.optString("uom"));
                        obj.put("transactionNumber", StringUtil.isNullOrEmpty(batch.getTransactionNo()) ? "" : batch.getTransactionNo());
                        obj.put("transactionDate", batch.getTransactionDate() != null ? df.format(batch.getTransactionDate()) : "");
                        obj.put("transactionType", batch.getDocType());
                        if (batch.getDocType() == TransactionBatch.DocType_INITIAL) {
                            obj.put("transactionType", "Opening");
                        } else if (batch.getDocType() == TransactionBatch.DocType_GRN) {
                            obj.put("transactionType", "Goods Receipt");
                        } else if (batch.getDocType() == TransactionBatch.DocType_SA_IN) {
                            obj.put("transactionType", "Stock Adjustment");
                        } else if (batch.getDocType() == TransactionBatch.DocType_PURCHASE_RETURN) {
                            obj.put("transactionType", "Purchase Return");
                        } else if (batch.getDocType() == TransactionBatch.DocType_SALES_RETURN) {
                            obj.put("transactionType", "Sales Return");
                        } else if (batch.getDocType() == TransactionBatch.DocType_ASSEMBLY_MAIN) {
                            obj.put("transactionType", "Assembly");
                        } else if (batch.getDocType() == TransactionBatch.DocType_IN_ISSUE) {
                            obj.put("transactionType", "Issue Note Issue");
                        } else if (batch.getDocType() == TransactionBatch.DocType_IN_COLLECT) {
                            obj.put("transactionType", "Issue Note Collect");
                        } else if (batch.getDocType() == TransactionBatch.DocType_IST_COLLECT) {
                            obj.put("transactionType", "Inter Store Transfer");
                        } else if (batch.getDocType() == TransactionBatch.DocType_IST_COLLECT) {
                            obj.put("transactionType", "Inter Store Transfer");
                        } else if (batch.getDocType() == TransactionBatch.DocType_ILT_ISSUE) {
                            obj.put("transactionType", "Inter Location Transfer Collect Issue");
                        } else if (batch.getDocType() == TransactionBatch.DocType_ILT_COLLECT) {
                            obj.put("transactionType", "Inter Location Transfer Collect");
                        } else if (batch.getDocType() == TransactionBatch.DocType_WO_IN){ /* In Stock Aging Report show transaction type:Work Order */
                            obj.put("transactionType", "Work Order");
                        } 
                        
                        double outQuantity = 0; 
                        /**
                         * Get OUT transaction quantity for the IN transaction.
                         */
                        if (batch.getOutTransactionQtyAmountMap() != null) {
                            for (Map.Entry<String, List<Double>> outEntry : batch.getOutTransactionQtyAmountMap().entrySet()) {
                                List<Double> value = outEntry.getValue();
                                if (value != null && !value.isEmpty()) {
                                    outQuantity += value.get(0);
                                }
                            }
                        }
                        /**
                         * Remaining Quantity is IN quantity - OUT quantity i.e.
                         * remainingQty = inTransactionQty - outTransactionQty.
                         */
                        double remainingQty = batch.getQuantity() - outQuantity; 
                        /**
                         * Show amount according to the remaining quantity.
                         */
                        double amount = batch.getPrice() * remainingQty;
                        if (batch.getTransactionDate() != null && (cdate1.before(batch.getTransactionDate()) || cdate1.equals(batch.getTransactionDate()))) {
                            obj.put("quantity1", authHandler.formattedQuantity(remainingQty, companyid));
                            obj.put("amount1", authHandler.round(amount, companyid));
                        } else if (batch.getTransactionDate() != null && ((cdate2.before(batch.getTransactionDate()) || cdate2.equals(batch.getTransactionDate())) && cdate1.after(batch.getTransactionDate()))) {
                            obj.put("quantity2", authHandler.formattedQuantity(remainingQty, companyid));
                            obj.put("amount2", authHandler.round(amount, companyid));
                        } else {
                            obj.put("quantity3", authHandler.formattedQuantity(remainingQty, companyid));
                            obj.put("amount3", authHandler.round(amount, companyid));
                        }
                        if (remainingQty != 0.0) { 
                            /**
                             * Don't include transaction which are having
                             * remaining quantity as 0. (ERP-33899)
                             */
                            dataArr.put(obj);
                        }
                    }
                }
            } else if(reportId == Constants.STOCK_STATUS_REPORT_ID){
                dataArr = getTransactionJSONForStockStatusReport(product, stack, dataArr, requestParams);
            }// end of isFromStatusReporyt else if
             else {
                PriceValuationStack.Batch openingBatch = null;
                if (!isAdvanceSearchForValuation) {
                    openingBatch = stack.getOpeningTransactionBatch();
                } else {
                    /**
                     * Get opening batch for transactions for which search
                     * criteria is matching.
                     */
                    openingBatch = stack.getOpeningTransBatch(true);
                }
                /**
                 * Show opening batch only if quantity is not equal to 0(When
                 * isAdvanceSearchForValuation is true).
                 */
                if ((openingBatch != null && !isAdvanceSearchForValuation) || (isAdvanceSearchForValuation && openingBatch!=null && openingBatch.getQuantity() != 0.0)) {
                    /**
                     * Add OpeningBatch Transaction details only if (quantity is
                     * not zero and serial name is not empty).
                     */
                    if (!(openingBatch.getQuantity()==0.0 && !StringUtil.isNullOrEmpty(serialNameSearch))) {
                        JSONObject OpeningRowObj = new JSONObject();
                        OpeningRowObj.put("pid", product.getString("id"));
//                    OpeningRowObj.put("productDesc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));//desc
                        OpeningRowObj.put("productDesc", product.optString("description"));
                        OpeningRowObj.put("transactionNumber", "Opening");
                        OpeningRowObj.put("personCode", "");
                        OpeningRowObj.put("uom", product.optString("uom"));
                        OpeningRowObj.put("personName", "");
                        OpeningRowObj.put("received", openingBatch.getQuantity());
                        OpeningRowObj.put("stockRate", authHandler.roundUnitPrice(openingBatch.getPrice(), companyid));
                        OpeningRowObj.put("value", authHandler.formattedAmount(openingBatch.getAmount(), companyid));
                        OpeningRowObj.put("balance", "-");
                        OpeningRowObj.put(Constants.productid, product.getString("id"));
                        if (isStockLedgerDetailedReport) { /* Stock Ledger Detailed Report*/
                            if (openingBatch.getLocationMap() != null) {
                                Map<String, Double> locationMap = openingBatch.getLocationMap();
                                StringBuilder locationBuilder = locationKeyBuilder(locationMap);
                                if (locationBuilder.length() > 0) {
                                    OpeningRowObj.put("location", locationBuilder.toString());
                                }
                            } else {
                                OpeningRowObj.put("location", "N/A");
                            }

                            if (openingBatch.getWarehouseMap() != null) {
                                Map<String, Double> warehouseMap = openingBatch.getWarehouseMap();
                                StringBuilder warehouseBuilder = warehouseKeyBuilder(warehouseMap);
                                if (warehouseBuilder.length() > 0) {
                                    OpeningRowObj.put("warehouse", warehouseBuilder.toString());
                                }
                            } else {
                                OpeningRowObj.put("warehouse", "N/A");
                            }
                            if (openingBatch.getBatchMap() != null) {
                                Map<String, Double> batchMap = openingBatch.getBatchMap();
                                StringBuilder batchBuilder = batchKeyBuilder(batchMap);
                                if (batchBuilder.length() > 0) {
                                    OpeningRowObj.put("batch", batchBuilder.toString());
                                }
                            } else {
                                OpeningRowObj.put("batch", "N/A");
                            }
                            if (openingBatch.getSerialMap() != null) {
                                Map<String, Double> serialMap = openingBatch.getSerialMap();
                                StringBuilder serialBuilder = serialKeyBuilder(serialMap);
                                if (serialBuilder.length() > 0) {
                                    OpeningRowObj.put("serial", serialBuilder.toString());
                                }

                            } else {
                                OpeningRowObj.put("serial", "N/A");
                            }
                        } else {
                            OpeningRowObj.put("batch", "");
                            OpeningRowObj.put("warehouse", "");
                            OpeningRowObj.put("location", "");
                            OpeningRowObj.put("serial", "");
                        }
                        if (isExport) {
                            OpeningRowObj.put("productDesc", URLEncoder.encode(product.optString("description"), "UTF-8"));//desc
                            OpeningRowObj.put("productDesc", product.optString("description"));
                            OpeningRowObj.put(Constants.productid, product.getString("id"));
                            OpeningRowObj.put("isQtyAndValuationJSON", 0);//
                        }
                        if (!isFromStockReport && !isInventoryValuation && !isConsolidationStockReport) {
                            dataArr.put(OpeningRowObj);
                        }
                        ledgerFinalValuation += openingBatch.getAmount();
                        ledgerOnHandQuantity += openingBatch.getQuantity();
                    }
                }
                Map<String, PriceValuationStack.Batch> detailsMap = null;
                if (!isAdvanceSearchForValuation) {
                    detailsMap = stack.getAllTransactionBatches(null, null, false, null);
                } else {
                    /**
                     * Get period transactions map for transactions for which
                     * fulfills the search criteria.
                     */
                    detailsMap = stack.getAllTransactionBatches(null, null, false, false, null, true);
                }
                SortTransactionMap sortTransactionMap = new SortTransactionMap();
                detailsMap = sortTransactionMap.getSortedMap(detailsMap);
                if (detailsMap != null) {
                    for (Map.Entry<String, PriceValuationStack.Batch> detailMap : detailsMap.entrySet()) {
//                            String detailID = detailMap.getKey();
                        PriceValuationStack.Batch batch = detailMap.getValue();
                        if (batch != null) {
                            double price = batch.getPrice();
                            double amount = batch.getAmount();
                            int transtype = batch.getDocType();
                            ledgerFinalValuation += amount;
                            ledgerOnHandQuantity += batch.getQuantity();
                            JSONObject obj = new JSONObject();
                            obj.put("pid", product.getString("id"));
//                            obj.put("productDesc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));//desc
                            obj.put("productDesc", product.optString("description"));
                            if (transtype != -1) {
                                obj.put("transactionDate", batch.getTransactionDate() != null ? df.format(batch.getTransactionDate()) : "");
                            }
                            obj.put("transactionNumber", StringUtil.isNullOrEmpty(batch.getTransactionNo()) ? "" : batch.getTransactionNo());
                            obj.put("uom", product.optString("uom"));
                            obj.put("personCode", StringUtil.isNullOrEmpty(batch.getPersonCode()) ? "" : batch.getPersonCode());
                            obj.put("personName", StringUtil.isNullOrEmpty(batch.getPersonName()) ? "" : batch.getPersonName());
                            if (transtype == 0 || transtype == 1 || transtype == 4 || transtype == 6 || transtype == -1 || transtype == 7|| transtype == 10|| transtype == 12|| transtype == 14|| transtype == 16) {
                                obj.put("received", batch.getQuantity());
                                stockInQty += batch.getQuantity();
                            } else {
                                obj.put("delivered", batch.getQuantity());

                                stockOutQty -= batch.getQuantity();

                            }
                            obj.put("memo", StringUtil.isNullOrEmpty(batch.getMemo()) ? "" : batch.getMemo());
                            obj.put("transactiontype", transtype);
                            obj.put("billid", batch.getBillid());
                            obj.put("stockRate", authHandler.roundUnitPrice(price, companyid));
                            if (isStockLedgerDetailedReport) { /* Stock Ledger Detailed Report*/
                                if (batch.getLocationMap() != null) {
                                    Map<String, Double> locationMap = batch.getLocationMap();
                                    StringBuilder locationBuilder = locationKeyBuilder(locationMap);
                                    if (locationBuilder.length() > 0) {
                                        obj.put("location", locationBuilder.toString());
                                    }
                                } else {
                                    obj.put("location", "N/A");
                                }

                                if (batch.getBatchMap() != null) {
                                    Map<String, Double> batchMap = batch.getBatchMap();
                                    StringBuilder batchBuilder = batchKeyBuilder(batchMap);
                                    if (batchBuilder.length() > 0) {
                                        obj.put("batch", batchBuilder.toString());
                                    }
                                } else {
                                    obj.put("batch", "N/A");
                                }
                                if (batch.getWarehouseMap() != null) {
                                    Map<String, Double> warehouseMap = batch.getWarehouseMap();
                                    StringBuilder warehouseBuilder = warehouseKeyBuilder(warehouseMap);
                                    if (warehouseBuilder.length() > 0) {
                                        obj.put("warehouse", warehouseBuilder.toString());
                                    }
                                } else {
                                    obj.put("warehouse", "N/A");
                                }
                                if (batch.getSerialMap() != null) {
                                    Map<String, Double> serialMap = batch.getSerialMap();
                                    StringBuilder serialBuilder = serialKeyBuilder(serialMap);
                                    if (serialBuilder.length() > 0) {
                                        obj.put("serial", serialBuilder.toString());
                                    }
                                } else {
                                    obj.put("serial", "N/A");
                                }
                            } else {
                                obj.put("serial", "");
                                obj.put("warehouse", "");
                                obj.put("location", "");
                                obj.put("batch", "");
                            }
                            if (transtype == -1) {
                                obj.put("stockRate", "-");
                            }
                             obj.put("value", authHandler.round(amount, companyid));
                            if (isExport) {
                                obj.put(Constants.productid, product.getString("id"));
                                obj.put("isQtyAndValuationJSON", 0);//
                                obj.put("transType", transtype);
                            }
                            obj.put("balance", authHandler.round(amount, companyid));
                            obj.put(Constants.productid, product.getString("id"));
                            if (!isFromStockReport && !isInventoryValuation && !isConsolidationStockReport) {
                                dataArr.put(obj);
                            }
                        }
                    }
                }
                /**
                 * Add Final Transaction details only if (On hand quantity is not
                 * zero and serial name is not empty).
                 */
                if ((!(ledgerOnHandQuantity == 0.0 && !StringUtil.isNullOrEmpty(serialNameSearch)) && !isAdvanceSearchForValuation) || (isAdvanceSearchForValuation && ((openingBatch != null && openingBatch.getQuantity() != 0.0) || (detailsMap != null && detailsMap.size() > 0)))) {
                    JSONObject finalRowObj = new JSONObject();
                    grandTotal+=ledgerFinalValuation;
                    PriceValuationStack.Batch avgCostBatch = stack.getTransactionBatch();
                    finalRowObj.put("pid", product.getString("id"));
                    finalRowObj.put("productDesc", "");
                    finalRowObj.put("transactionNumber", "");
                    finalRowObj.put("personCode", "");
                    finalRowObj.put("personName", "");
                    finalRowObj.put("uom", product.optString("uom"));
                    finalRowObj.put("received", "");
                    finalRowObj.put("stockRate", "Quantity on Hand: " + authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));
                    finalRowObj.put("value", "Valuation: " + (authHandler.formattedAmount(ledgerFinalValuation, companyid)));
                    finalRowObj.put("ledgerFinalValuation", authHandler.formattedAmount(ledgerFinalValuation, companyid));
                    finalRowObj.put("grandtotal", authHandler.formattedAmount(grandTotal, companyid));
                    finalRowObj.put("balance", "-");
                    if (isExport) {
                        finalRowObj.put(Constants.productid, product.getString("id"));//
                        finalRowObj.put("QtyOnHandJasper", authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));
                        finalRowObj.put("ValuationJasper", authHandler.formattedAmount(ledgerFinalValuation, companyid));
                        finalRowObj.put("stockRate", 0);//
                        finalRowObj.put("value", 0);//
                        finalRowObj.put("isQtyAndValuationJSON", 1);
                    }
                    finalRowObj.put(Constants.productid, product.getString("id"));
                    finalRowObj.put("pid", product.getString("productid"));
                    finalRowObj.put("productname", product.getString("name"));
                    finalRowObj.put("productdesc", product.optString("description"));
                    finalRowObj.put("productType", product.getString("producttypeid"));
                    finalRowObj.put("productTypeID", product.getString("producttypename"));
                    if (ValuationMethod.getValue(product.getInt("valuationmethod")) == ValuationMethod.FIFO) {
                        finalRowObj.put("fifo", ledgerFinalValuation);
                    } else {
                        finalRowObj.put("fifo", "NA");
                    }
                    if (ValuationMethod.getValue(product.getInt("valuationmethod")) == ValuationMethod.STANDARD) {
                        finalRowObj.put("lifo", ledgerFinalValuation);
                    } else {
                        finalRowObj.put("lifo", "NA");
                    }
                    KwlReturnObject priceResult = accProductObj.getProductPrice(product.getString("id"), true, null, "-1", product.getString("prodcurrency"));
                    List<Object> priceList = priceResult.getEntityList();
                    double proPrice = 0;
                    if (priceList != null) {
                        for (Object cogsval : priceList) {
                            proPrice = (cogsval == null ? 0.0 : (Double) cogsval);
                        }
                        KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, proPrice, product.getString("prodcurrency"), null, 0);
                        proPrice = (Double) crresult.getEntityList().get(0);
                    }
                    finalRowObj.put("purchasecost", proPrice);
                    finalRowObj.put("quantity", ledgerOnHandQuantity);
                    if (openingBatch != null) {
                        finalRowObj.put("openingstockQty", openingBatch.getQuantity());
                        finalRowObj.put("openingstockvalue", authHandler.formattedAmount(openingBatch.getAmount(), companyid));
                    } else {
                        finalRowObj.put("openingstockvalue", authHandler.formattedAmount((0.0), companyid));
                    }
                    finalRowObj.put("stockInQty", stockInQty);
                    finalRowObj.put("stockOutQty", stockOutQty);
                    finalRowObj.put("evaluationcost", ledgerOnHandQuantity == 0.0?0.0:authHandler.formattedAmount(ledgerFinalValuation, companyid));
                    if (ValuationMethod.getValue(product.getInt("valuationmethod")) == ValuationMethod.AVERAGE && !product.getBoolean("isSerialForProduct") && !product.optBoolean("isBatchForProduct")) {
                        finalRowObj.put("avgcost", (avgCostBatch.getPrice() != 0 && ledgerOnHandQuantity != 0) ? avgCostBatch.getPrice() : "N.A");
                        finalRowObj.put("valuation", ledgerFinalValuation);
                    } else if (ValuationMethod.getValue(product.getInt("valuationmethod")) == ValuationMethod.AVERAGE && (product.getBoolean("isSerialForProduct") || product.optBoolean("isBatchForProduct"))) {//By Dipak P.
                        finalRowObj.put("valuation", ledgerFinalValuation);
                    } else {
                        finalRowObj.put("valuation", "NA");
                    }
                    requestParams.put("gTotal", grandTotal);
                    dataArr.put(finalRowObj);
                }                
             } // end of else

        } catch (NumberFormatException | ServiceException | JSONException | SessionExpiredException | ParseException | UnsupportedEncodingException ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
     return dataArr;
    }
    
    /* Returns JSON for StockStatusReport. */
    
    private JSONArray getTransactionJSONForStockStatusReport(Product product, PriceValuationStack stack, JSONArray dataArr, HashMap<String, Object> requestParams) throws ServiceException {
        //Code for Stock report Status will go Here
        String companyid = (String) requestParams.get(Constants.companyid);
        String globalCurrencyKey = "";
        boolean isExport = false;
        if (requestParams.containsKey(Constants.IS_EXPORT_REPORT) && requestParams.get(Constants.IS_EXPORT_REPORT) != null) {
            isExport = Boolean.parseBoolean(requestParams.get(Constants.IS_EXPORT_REPORT).toString());
        }
        if (requestParams.containsKey(Constants.globalCurrencyKey) && requestParams.get(Constants.globalCurrencyKey) != null) {
            globalCurrencyKey = requestParams.get(Constants.globalCurrencyKey).toString();
        }
        try {
            Object startDateStr = requestParams.get(Constants.REQ_startdate);
            Date startDate = null;
            if (startDateStr != null) {
                startDate = authHandler.getDateOnlyFormat().parse(startDateStr.toString());
            }

            Object endDateStr = requestParams.get(Constants.REQ_enddate);
            Date endDate = null;
            if (endDateStr != null) {
                endDate = authHandler.getDateOnlyFormat().parse(endDateStr.toString());
            }

            Calendar startcal = Calendar.getInstance();
            Calendar endcal = Calendar.getInstance();

            if (startDate != null) {
                startcal.setTime(startDate);
            }
            if (endDate != null) {
                endcal.setTime(endDate);
            }
            
            PriceValuationStack.Batch openingBatch = stack.getOpeningTransactionBatch();
            PriceValuationStack.Batch periodicBatch = stack.getPeriodTransactionBatch();

            JSONObject obj = new JSONObject();
            obj.put("uom", product.getUnitOfMeasure() != null ? product.getUnitOfMeasure().getNameEmptyforNA() : "");
            obj.put("productid", product.getID());
            obj.put("productname", product.getName());
            obj.put("pid", product.getProductid());
            obj.put("productdesc", StringUtil.isNullOrEmpty(product.getDescription())?"":isExport ? StringUtil.replaceFullHTML(product.getDescription().replace("<br>","\n")) : product.getDescription());
            obj.put("productType", product.getProducttype().getName());
            obj.put("productTypeID", product.getProducttype().getID());
            
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
            requestParams1.put(Constants.companyKey, companyid);
            requestParams1.put(Constants.globalCurrencyKey, globalCurrencyKey);
            requestParams1.put("df", authHandler.getDateOnlyFormat());
            requestParams1.put("pendingapproval", false);
            requestParams1.put("startdate", startDate);
            requestParams1.put("enddate", endDate);
            requestParams1.put("productId", product.getID());
            requestParams1.put("isStockStatus", true);

            double poQty = getOutstandingPoSoProductsCount(requestParams1, true, product.getID(), accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj, accSalesOrderDAOobj, accPurchaseOrderobj, product.getUnitOfMeasure().getID());

            double soQty = getOutstandingPoSoProductsCount(requestParams1, false, product.getID(), accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj, accSalesOrderDAOobj, accPurchaseOrderobj, product.getUnitOfMeasure().getID());
            
            double openingQuantity = openingBatch.getQuantity();
            double periodicQuantity = periodicBatch.getQuantity();
            
            double onhand = (openingQuantity + periodicQuantity);
            obj.put("poQty", authHandler.formattedQuantity(poQty, companyid)); //outstanding purchase 
            obj.put("soQty", authHandler.formattedQuantity(soQty, companyid)); //outstanding sales
            double netQty = (onhand + poQty - soQty);
            obj.put("netQty", authHandler.formattedQuantity(netQty, companyid)); // net quantity
            obj.put("onhand", authHandler.formattedQuantity(onhand, companyid));

            dataArr.put(obj);
        } catch (SessionExpiredException | ParseException | JSONException | ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return dataArr;
    }
    /**
     * Returns JSON for StockStatusReport.
     * @param productJson
     * @param stack
     * @param dataArr
     * @param requestParams
     * @return
     * @throws ServiceException 
     */
    private JSONArray getTransactionJsonForStockStatusReport(JSONObject productJson, PriceValuationStack stack, JSONArray dataArr, HashMap<String, Object> requestParams) throws ServiceException {
        //Code for Stock report Status will go Here
        String companyid = (String) requestParams.get(Constants.companyid);
        String globalCurrencyKey = "";
        boolean isExport = false;
        if (requestParams.containsKey(Constants.IS_EXPORT_REPORT) && requestParams.get(Constants.IS_EXPORT_REPORT) != null) {
            isExport = Boolean.parseBoolean(requestParams.get(Constants.IS_EXPORT_REPORT).toString());
        }
        if (requestParams.containsKey(Constants.globalCurrencyKey) && requestParams.get(Constants.globalCurrencyKey) != null) {
            globalCurrencyKey = requestParams.get(Constants.globalCurrencyKey).toString();
        }
        try {
            Object startDateStr = requestParams.get(Constants.REQ_startdate);
            Date startDate = null;
            if (startDateStr != null) {
                startDate = authHandler.getDateOnlyFormat().parse(startDateStr.toString());
            }

            Object endDateStr = requestParams.get(Constants.REQ_enddate);
            Date endDate = null;
            if (endDateStr != null) {
                endDate = authHandler.getDateOnlyFormat().parse(endDateStr.toString());
            }

            Calendar startcal = Calendar.getInstance();
            Calendar endcal = Calendar.getInstance();

            if (startDate != null) {
                startcal.setTime(startDate);
            }
            if (endDate != null) {
                endcal.setTime(endDate);
            }
            
            PriceValuationStack.Batch openingBatch = stack.getOpeningTransactionBatch();
            PriceValuationStack.Batch periodicBatch = stack.getPeriodTransactionBatch();

            JSONObject obj = new JSONObject();
            obj.put("uom", productJson.getString("uom"));
            obj.put("productid", productJson.getString("id"));
            obj.put("productname", productJson.getString("name"));
            obj.put("pid", productJson.getString("productid"));
            obj.put("productdesc", StringUtil.isNullOrEmpty(productJson.getString("description"))?"":isExport ? StringUtil.replaceFullHTML(productJson.getString("description").replace("<br>","\n")) : productJson.getString("description"));
            obj.put("productType", productJson.getString("producttypename"));
            obj.put("productTypeID", productJson.getString("producttypeid"));
            
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
            requestParams1.put(Constants.companyKey, companyid);
            requestParams1.put(Constants.globalCurrencyKey, globalCurrencyKey);
            requestParams1.put("df", authHandler.getDateOnlyFormat());
            requestParams1.put("pendingapproval", false);
            requestParams1.put("startdate", startDate);
            requestParams1.put("enddate", endDate);
            requestParams1.put("productId", productJson.getString("id"));
            requestParams1.put("isStockStatus", true);

            double poQty = getOutstandingPoSoProductsCount(requestParams1, true, productJson.getString("id"), accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj, accSalesOrderDAOobj, accPurchaseOrderobj, productJson.getString("uomid"));

            double soQty = getOutstandingPoSoProductsCount(requestParams1, false, productJson.getString("id"), accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj, accSalesOrderDAOobj, accPurchaseOrderobj, productJson.getString("uomid"));
            
            double openingQuantity = openingBatch.getQuantity();
            double periodicQuantity = periodicBatch.getQuantity();
            
            double onhand = (openingQuantity + periodicQuantity);
            obj.put("poQty", authHandler.formattedQuantity(poQty, companyid)); //outstanding purchase 
            obj.put("soQty", authHandler.formattedQuantity(soQty, companyid)); //outstanding sales
            double netQty = (onhand + poQty - soQty);
            obj.put("netQty", authHandler.formattedQuantity(netQty, companyid)); // net quantity
            obj.put("onhand", authHandler.formattedQuantity(onhand, companyid));

            dataArr.put(obj);
        } catch (SessionExpiredException | ParseException | JSONException | ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return dataArr;
    }
    
    private JSONArray getTransactionJSONForStockStatusReport(JSONObject product, PriceValuationStack stack, JSONArray dataArr, HashMap<String, Object> requestParams) throws ServiceException {
        //Code for Stock report Status will go Here
        String companyid = (String) requestParams.get(Constants.companyid);
        String globalCurrencyKey = "";

        if (requestParams.containsKey(Constants.globalCurrencyKey) && requestParams.get(Constants.globalCurrencyKey) != null) {
            globalCurrencyKey = requestParams.get(Constants.globalCurrencyKey).toString();
        }
        try {
            Object startDateStr = requestParams.get(Constants.REQ_startdate);
            Date startDate = null;
            if (startDateStr != null) {
                startDate = authHandler.getDateOnlyFormat().parse(startDateStr.toString());
            }

            Object endDateStr = requestParams.get(Constants.REQ_enddate);
            Date endDate = null;
            if (endDateStr != null) {
                endDate = authHandler.getDateOnlyFormat().parse(endDateStr.toString());
            }

            Calendar startcal = Calendar.getInstance();
            Calendar endcal = Calendar.getInstance();

            if (startDate != null) {
                startcal.setTime(startDate);
            }
            if (endDate != null) {
                endcal.setTime(endDate);
            }
            
            PriceValuationStack.Batch openingBatch = stack.getOpeningTransactionBatch();
            PriceValuationStack.Batch periodicBatch = stack.getPeriodTransactionBatch();

            JSONObject obj = new JSONObject();
            obj.put("uom", product.optString("uom"));
            obj.put("productid", product.getString("productid") );
            obj.put("productname", product.getString("name"));
            obj.put("pid", product.getString("id"));
            obj.put("productdesc", product.optString("description"));
            obj.put("productType", product.optString("producttypename"));
            obj.put("productTypeID", product.optString("producttypeid"));
            
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
            requestParams1.put(Constants.companyKey, companyid);
            requestParams1.put(Constants.globalCurrencyKey, globalCurrencyKey);
            requestParams1.put("df", authHandler.getDateOnlyFormat());
            requestParams1.put("pendingapproval", false);
            requestParams1.put("startdate", startDate);
            requestParams1.put("enddate", endDate);
            requestParams1.put("productId", product.getString("id"));
            requestParams1.put("isStockStatus", true);

            double poQty = getOutstandingPoSoProductsCount(requestParams1, true, product.getString("id"), accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj, accSalesOrderDAOobj, accPurchaseOrderobj, product.getString("uomid"));

            double soQty = getOutstandingPoSoProductsCount(requestParams1, false, product.getString("id"), accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj, accSalesOrderDAOobj, accPurchaseOrderobj, product.getString("uomid"));
            
            double openingQuantity = openingBatch.getQuantity();
            double periodicQuantity = periodicBatch.getQuantity();
            
            double onhand = (openingQuantity + periodicQuantity);
            obj.put("poQty", authHandler.formattedQuantity(poQty, companyid)); //outstanding purchase 
            obj.put("soQty", authHandler.formattedQuantity(soQty, companyid)); //outstanding sales
            double netQty = (onhand + poQty - soQty);
            obj.put("netQty", authHandler.formattedQuantity(netQty, companyid)); // net quantity
            obj.put("onhand", authHandler.formattedQuantity(onhand, companyid));

            dataArr.put(obj);
        } catch (SessionExpiredException | ParseException | JSONException | ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return dataArr;
    }
    
    /**
     * Method used to provide data for export the Stock Ageing Report in Jasper
     *
     * @param requestParams
     * @author Swapnil K.
     * @return Map
     */
    @Override
    public Map<String, Object> getDataForStockAgeingReport(HashMap<String, Object> requestParams) {
        Map<String, Object> financeDetailsMap = new HashMap<String, Object>();
        try {
            ArrayList<StockAgeing> stockAgeingList = new ArrayList<StockAgeing>();
            String startDateString = (String) requestParams.get(Constants.REQ_startdate);
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            Date startDate = null;
            if (!StringUtil.isNullOrEmpty(startDateString) && df != null) {
                startDate = df.parse(startDateString);
            }
            String Searchjson = "";
            if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                Searchjson = (String) requestParams.get("searchJson");
            }
            String gcurrencyid = "";
            if (requestParams.containsKey(Constants.globalCurrencyKey) && requestParams.get(Constants.globalCurrencyKey) != null) {
                gcurrencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            }
            requestParams.remove(Constants.REQ_startdate);
            String asofDateString = (String) requestParams.get("asofdate");
            Date asofDate = null;
            if (!StringUtil.isNullOrEmpty(asofDateString) && df != null) {
                asofDate = df.parse(asofDateString);
            }
            Date endDate = asofDate;
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), (String) requestParams.get(Constants.companyKey));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            if (pref.getNegativestock() != 1 || pref.getNegativeStockPR() != 1) { 
                /* warn/ignore case for DO & PR */
                requestParams.remove(Constants.REQ_enddate);
            }
            String companyid = (String) requestParams.get("companyid");
            
            Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyid);
            String companyCurrencyCode = company != null ? company.getCurrency().getCurrencyCode() : "";
            
            Map<String, Object> reqMap = new HashMap();
            reqMap.put("companyid", companyid);
            if (requestParams.containsKey("valuationMethod")) {
                reqMap.put("valuationMethod", requestParams.get("valuationMethod"));
            }
            KwlReturnObject productResult = accProductObj.getProductIdsForCompany(reqMap);
            List productList = productResult.getEntityList();
            if (productList != null && !productList.isEmpty()) {
                PriceValuationStack stack = new PriceValuationStack();
                for (Object object : productList) {
                    String productid = (String) object;
                    requestParams.put("productId", productid);
                    requestParams.put("companyid", companyid);
                    if (!StringUtil.isNullOrEmpty(Searchjson)) {
                        requestParams.put("searchJson", Searchjson);
                    }
                    if (!StringUtil.isNullOrEmpty(gcurrencyid)) {
                        requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
                    }
                    /**
                     * Flag 'includeProductDetailsInSelectQuery' is used in method 'accProductImpl.getStockLedger' to fetch product details along with transaction data
                     * It has been added to remove Product object's dependency from method 'getProductTransactionJson'
                     * When this flag is passed as true, product details required for inventory reports  are also fetched from database along with transaction details
                     */
                    requestParams.put(Constants.includeProductDetailsInSelectQuery, true);
                    KwlReturnObject result = accProductObj.getStockLedger(requestParams);
                    List list = result.getEntityList();
                    if (list != null && !list.isEmpty() && !StringUtil.isNullOrEmpty(productid)) {
                        JSONObject productJson = getProductDetailsFromTransactionRow((Object[]) list.get(0), requestParams);//Get Product details from list, in a JSONObject
                        pushTransactionForProduct(list, requestParams, startDate, productid, stack, endDate, null);
                        addTransactionToStockAgeingList(productJson, stack, stockAgeingList, requestParams, companyCurrencyCode);
                        stack.clear();
                    }
                }
            }
            financeDetailsMap.put("StockAgeingSubReportData", new JRBeanCollectionDataSource(stockAgeingList));
        } catch (Exception ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
        return financeDetailsMap;
    }

    /**
     * Method used to provide data for export the Stock Ageing Report in Jasper
     *
     * @param productJson
     * @param stack
     * @param ArrayList
     * @param requestParams
     * @author Swapnil K.
     * @return ArrayList
     */
    private ArrayList addTransactionToStockAgeingList(JSONObject productJson, PriceValuationStack stack, ArrayList<StockAgeing> stockAgeingList, HashMap<String, Object> requestParams, String companyCurrencyCode) {
        try {
            String companyid = (String) requestParams.get("companyid");
            int duration = 45;
            Date curDate = new Date();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(curDate);
            cal2.setTime(curDate);
            cal1.add(Calendar.DAY_OF_YEAR, -duration);
            cal2.add(Calendar.DAY_OF_YEAR, -(duration * 2));
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String decimalFormatForAmount= authHandler.getCompleteDFStringForAmount("#,##0.", companyid);
            String decimalFormatForQuantity= authHandler.getCompleteDFStringForQuantity("#,##0.", companyid);
            Map<String, PriceValuationStack.Batch> inTransactions = stack.getAllTransactionBatches(null, null, null, false);
            for (Map.Entry<String, PriceValuationStack.Batch> entry : inTransactions.entrySet()) {
                PriceValuationStack.Batch batch = entry.getValue();
                if (batch != null) {
                    Date transactionDate = batch.getTransactionDate();
                    StockAgeing stockAgeing = new StockAgeing();
                    stockAgeing.setHeader1("Below - " + duration);
                    stockAgeing.setHeader2(duration + 1 + " - " + duration * 2);
                    stockAgeing.setHeader3("Over - " + duration * 2);
                    stockAgeing.setProdcode(productJson.getString("productid"));
                    stockAgeing.setProdid(productJson.getString("id"));
                    stockAgeing.setProdname(productJson.getString("description"));
                    stockAgeing.setUnit(productJson.getString("uom"));
                    stockAgeing.setDocumentno(batch.getTransactionNo());
                    //To format date in User Date Format
                    stockAgeing.setDate(transactionDate != null ? df.format(transactionDate) : "");
                    if (batch.getDocType() == TransactionBatch.DocType_INITIAL) {
                        stockAgeing.setDocumentname("Opening");
                    } else if (batch.getDocType() == TransactionBatch.DocType_GRN) {
                        stockAgeing.setDocumentname("Goods Receipt");
                    } else if (batch.getDocType() == TransactionBatch.DocType_SA_IN) {
                        stockAgeing.setDocumentname("Stock Adjustment");
                    } else if (batch.getDocType() == TransactionBatch.DocType_PURCHASE_RETURN) {
                        stockAgeing.setDocumentname("Purchase Return");
                    } else if (batch.getDocType() == TransactionBatch.DocType_SALES_RETURN) {
                        stockAgeing.setDocumentname("Sales Return");
                    }
                    stockAgeing.setCurrency(companyCurrencyCode);

                    Date cdate1 = cal1.getTime();
                    String caldate1 = df.format(cdate1);
                    try {
                        cdate1 = df.parse(caldate1);
                    } catch (ParseException ex) {
                        cdate1 = cal1.getTime();
                    }
                    Date cdate2 = cal2.getTime();
                    String caldate2 = df.format(cdate2);
                    try {
                        cdate2 = df.parse(caldate2);
                    } catch (ParseException ex) {
                        cdate2 = cal2.getTime();
                    }
                    if (batch.getTransactionDate() != null && (cdate1.before(batch.getTransactionDate()) || cdate1.equals(batch.getTransactionDate()))) {
                        stockAgeing.setQuantity1(batch.getQuantity());
                        stockAgeing.setAmount1(authHandler.round(batch.getAmount(), companyid));
                    } else if (batch.getTransactionDate() != null && ((cdate2.before(batch.getTransactionDate()) || cdate2.equals(batch.getTransactionDate())) && cdate1.after(batch.getTransactionDate()))) {
                        stockAgeing.setQuantity2(batch.getQuantity());
                        stockAgeing.setAmount2(authHandler.round(batch.getAmount(), companyid));
                    } else {
                        stockAgeing.setQuantity3(batch.getQuantity());
                        stockAgeing.setAmount3(authHandler.round(batch.getAmount(), companyid));
                    }
                    stockAgeing.setDecimalFormatForAmount(decimalFormatForAmount);
                    stockAgeing.setDecimalFormatForQuantity(decimalFormatForQuantity);
                    stockAgeingList.add(stockAgeing);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
        return stockAgeingList;
    }

 @Override
    public double getOutstandingSICount(HashMap<String, Object> requestParams, accInvoiceDAO accInvoiceDAOobj) {
        double quantity = 0;
        try {
            KwlReturnObject result = accInvoiceDAOobj.getInvoiceDetailsForProduct(requestParams);
            List list = result.getEntityList();
            Iterator ite = list.iterator();
            while (ite.hasNext()) {
                InvoiceDetail invoicedetail = (InvoiceDetail) ite.next();
                double actualQty = 0, deliveredQty = 0;
                if (invoicedetail.getInventory() != null) {
                    actualQty = invoicedetail.getInventory().getActquantity();
                }
                deliveredQty += accInvoiceDAOobj.getDeliveryOrderQuantityFromSI(invoicedetail.getID(), (String) requestParams.get("companyid"), false);
                if (invoicedetail.getDeliveryOrderDetail() != null) {
                    deliveredQty += accInvoiceDAOobj.getInvoiceFromQuantityDO(invoicedetail.getDeliveryOrderDetail().getID(), (String) requestParams.get("companyid"), false);
                }
                if (deliveredQty < actualQty) {
                    quantity = quantity + (actualQty - deliveredQty);
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception occurred in getOutstandingSICount: " + ex.getMessage());
        } finally {
            return quantity;
        }
    }  
 
 @Override
    public JSONObject getOutstandingPOSOCount(JSONObject paramJobj) throws ServiceException, SessionExpiredException, ParseException, JSONException {
        JSONObject returnObj = new JSONObject();
        boolean getSOPOflag = paramJobj.optString("getSOPOflag") != null ? Boolean.parseBoolean(paramJobj.optString("getSOPOflag", "false")) : false;
        JSONArray jArray = new JSONArray();
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferencesObj = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            KwlReturnObject kwlReturnObject = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) kwlReturnObject.getEntityList().get(0);
            String gcurrencyid = paramJobj.optString(Constants.globalCurrencyKey);
            String[] productids = paramJobj.optString("ids").split(",");
            int moduleid = (StringUtil.isNullOrEmpty(paramJobj.optString("moduleid",null))) ? 0 : Integer.parseInt(paramJobj.optString("moduleid","0"));
            for (int i = 0; i < productids.length; i++) {
                JSONObject obj = new JSONObject();

                KwlReturnObject prodresult = accountingHandlerDAOobj.getObject(Product.class.getName(), productids[i]);
                Product productObj = prodresult != null ? (Product) prodresult.getEntityList().get(0) : null;
                obj.put("blockLooseSell", productObj.isblockLooseSell());
                UnitOfMeasure uom = productObj.getUnitOfMeasure();
                obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
                String type="";
                if (productObj.getProducttype() != null) {
                    if (storageHandlerImpl.GetVRnetCompanyId().contains(companyid)) {
                        if (StringUtil.equal(productObj.getProducttype().getName(), "Inventory Assembly")) {
                            type = "Inventory Bundle";
                        } else {
                            type = productObj.getProducttype().getName();
                        }
                    } else if (productObj.isAsset()) {     //For Fixed Asset Group, type will be "Asset"
                        obj.put("type", "Asset");
                    } else {
                        type = productObj.getProducttype().getName();
                        obj.put("type", type);
                    }
                }
                if (moduleid == Constants.Acc_Sales_Order_ModuleId && (preferences.getNegativestock() == 1 || preferences.getNegativestock() == 2)) {
                    getSOPOflag = true;
                }
                
                if (getSOPOflag) {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("companyid", companyid);
                    requestParams.put("gcurrencyid", gcurrencyid);
                    requestParams.put("df", authHandler.getDateOnlyFormat());
                    requestParams.put("pendingapproval", false);
                    if (!StringUtil.isNullOrEmpty(paramJobj.optString("startdate", null))) {
                        requestParams.put("startdate", authHandler.getDateOnlyFormat().parse(paramJobj.optString("startdate")));
                    }
                    if (!StringUtil.isNullOrEmpty(paramJobj.optString("enddate", null))) {
                        requestParams.put("enddate", authHandler.getDateOnlyFormat().parse(paramJobj.optString("enddate")));
                    }
                    requestParams.put("productId", productObj.getID());
                    String productuomId = (productObj.getUnitOfMeasure() != null) ? productObj.getUnitOfMeasure().getID() : "";
                    double productCount = getOutstandingPoSoProductsCount(requestParams, true, productObj.getID(), accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj, accSalesOrderDAOobj, accPurchaseOrderobj, productuomId);//List list,

                    obj.put("pocount", productCount);
                    obj.put("pocountinselecteduom", productCount);
                    /*
                     * Skipping calculation for getting PO and SO outstanding
                     * quantity when open PO snd SO disabled from system
                     * preferences
                     */
                    if (extraCompanyPreferencesObj.isOpenPOandSO()) {
                        double openPocount = getProductOutstandingPOSOQuantity(paramJobj, productObj.getID(), false);
                        obj.put("openpocount", openPocount);
                    }
                    //Sales Order socount-invoice.js required
                    productCount = getOutstandingPoSoProductsCount(requestParams, false, productObj.getID(), accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj, accSalesOrderDAOobj, accPurchaseOrderobj, productuomId);//List list,

                    obj.put("socount", productCount);
                    obj.put("socountinselecteduom", productCount);
                    /*
                     * Skipping calculation for getting PO and SO outstanding
                     * quantity when open PO snd SO disabled from system
                     * preferences
                     */
                    if (extraCompanyPreferencesObj.isOpenPOandSO()) {
                        double openSocount = getProductOutstandingPOSOQuantity(paramJobj, productObj.getID(), true);
                        obj.put("opensocount", openSocount);
                    }
                }
                if ((moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Cash_Sales_ModuleId) && (preferences.getNegativeStockSICS() == 1 || preferences.getNegativeStockSICS() == 2)) {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("companyid", companyid);
                    requestParams.put("gcurrencyid", gcurrencyid);
                    requestParams.put("df", authHandler.getDateOnlyFormat());
                    requestParams.put("pendingapproval", false);
                    requestParams.put("startdate", paramJobj.optString("startdate"));
                    requestParams.put("enddate", paramJobj.optString("enddate"));
                    requestParams.put("productId", productObj.getID());
                    double invoiceProductCount = getOutstandingSICount(requestParams, accInvoiceDAOobj);
                    obj.put("sicount", invoiceProductCount);
                }

                if (productObj.isblockLooseSell()) {
                    KwlReturnObject result = accProductObj.getAvailableQuantityInSelectedUOM(productObj.getID(), productObj.getUnitOfMeasure().getID());
                    obj.put("quantity", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));
                    obj.put("availableQtyInSelectedUOM", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));
                } else {
                    KwlReturnObject result = accProductObj.getQuantity(productObj.getID());;
                    obj.put("quantity", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));
                    obj.put("availableQtyInSelectedUOM", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));
                }

                KwlReturnObject result2 = accProductObj.getAssemblyLockQuantity(productObj.getID()); //get the lock quantity locked in assembly type of product in SO
                Double assmblyLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));

                KwlReturnObject result1 = accProductObj.getLockQuantity(productObj.getID());//get the lock quantity locked in inventory type of product in  all SO
                Double SoLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));

                obj.put("lockquantity", assmblyLockQuantity + SoLockQuantity);  //total lock quantity of product locked in SO
                /*
                 * "Reserve Stock" for showing in transaction form summary
                 */
                obj.put("reservestock", accProductObj.getReserveQuantityTaggedInQuotation(productObj.getID(), companyid));
                
                jArray.put(obj);
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnObj.put(Constants.data, jArray);
            returnObj.put(Constants.RES_TOTALCOUNT, jArray.length());
        }
        return returnObj;
    }
 
    public double getProductOutstandingPOSOQuantity(JSONObject paramJobj, String productid, boolean SoPoFlag) {
        double TotalCount = 0;
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            HashMap<String, Object> requestParams = productHandler.getProductRequestMapfromJson(paramJobj);
            requestParams.remove(Constants.ss);
            requestParams.put("productid", productid);
            requestParams.put("companyid", companyid);
            requestParams.put("df", authHandler.getDateOnlyFormat());
            requestParams.put("pendingapproval", false);
            requestParams.put("issopoclosed", true);
            if (!StringUtil.isNullOrEmpty(productid)) {
                if (SoPoFlag) {
                    KwlReturnObject result = accSalesOrderDAOobj.getSalesOrdersMerged(requestParams);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        JSONObject obj = new JSONObject();
                        Object[] oj = (Object[]) itr.next();
                        String invid = oj[0].toString();
                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), invid);
                        SalesOrder soorder = (SalesOrder) objItr.getEntityList().get(0);
                        Set<SalesOrderDetail> orderDetail = soorder.getRows();
                        Iterator ite = orderDetail.iterator();

                        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), soorder.getCompany().getCompanyID());
                        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
                        String resultorder = "Closed";
                        boolean fullInv = false;
                        for (SalesOrderDetail soDetail:orderDetail) {

                            if (!productid.equals(soDetail.getProduct().getID())) {
                                continue;
                            }
                            double qua = 0;
                            double quantPartTt = soDetail.getQuantity() * 100;
                            double quantPartTtInv = 0.0;
                            if (pref.isWithInvUpdate()) { //In Trading Flow                 
                                KwlReturnObject doresult = accInvoiceDAOobj.getDeliveryOrderDFromSOD(soDetail.getID(), pref.getCompany().getCompanyID());
                                List list1 = doresult.getEntityList();
                                if (list1.size() > 0) {
                                    Iterator ite1 = list1.iterator();
                                    while (ite1.hasNext()) {
                                        String orderid = (String) ite1.next();
                                        KwlReturnObject res = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), orderid);
                                        DeliveryOrderDetail deliveryOrderDetail = (DeliveryOrderDetail) res.getEntityList().get(0);
                                        fullInv = true;
                                        qua += deliveryOrderDetail.getBaseuomdeliveredquantity();
                                    }
                                }
                            } else { //In Non Trading Flow 

                                KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSOD(soDetail.getID());
                                List list1 = idresult.getEntityList();
                                Iterator ite1 = list1.iterator();
                                while (ite1.hasNext()) {
                                    InvoiceDetail ge = (InvoiceDetail) ite1.next();
                                    if (ge.getInvoice().isPartialinv()) {
                                        double quantity = ge.getInventory().getBaseuomquantity();
                                        quantPartTtInv += quantity * ge.getPartamount();
                                    } else {
                                        fullInv = true;
                                        qua += ge.getInventory().getBaseuomquantity();
                                    }
                                }
                            }

                            if (fullInv) {
                                if (qua < soDetail.getBaseuomquantity()) {
                                    TotalCount = TotalCount + (soDetail.getBaseuomquantity() - qua);
                                    break;

                                }

                            } else if (quantPartTt > quantPartTtInv && !(soDetail.isIsLineItemClosed())) {
                                TotalCount = TotalCount + (soDetail.getBaseuomquantity() - qua);
                                //break;
                            }
                        }

                    }

                } else {
                    KwlReturnObject result1 = accPurchaseOrderobj.getPurchaseOrdersMerged(requestParams);
                    List<Object[]> list1 = result1.getEntityList();
                    Iterator itr1 = list1.iterator();
                    for (Object[] oj:list1) {
                        String invid = oj[0].toString();
                        JSONObject obj = new JSONObject();
                        KwlReturnObject objItr1 = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), invid);
                        PurchaseOrder purchaseOrder = (PurchaseOrder) objItr1.getEntityList().get(0);

                        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), purchaseOrder.getCompany().getCompanyID());
                        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

                        String resultorder = "Closed";
                        Set<PurchaseOrderDetail> orderDetail = purchaseOrder.getRows();
                        Iterator ite = orderDetail.iterator();
                        for (PurchaseOrderDetail pDetail:orderDetail) {
                            if (!productid.equals(pDetail.getProduct().getID())) {
                                continue;
                            }
                            double qua = 0;
                            if (pref.isWithInvUpdate()) { //In Trading Flow                  
                                KwlReturnObject groresult = accGoodsReceiptDAOobj.getReceiptOrderDFromPODOptimized(pDetail.getID(), pref.getCompany().getCompanyID());
                                List list = groresult.getEntityList();
                                if (list.size() > 0) {
                                    Iterator ite1 = list.iterator();
                                    while (ite1.hasNext()) {
                                        String orderid = (String) ite1.next();
                                        KwlReturnObject res = accountingHandlerDAOobj.getObject(GoodsReceiptOrderDetails.class.getName(), orderid);
                                        GoodsReceiptOrderDetails goodsReceiptOrderDetails = (GoodsReceiptOrderDetails) res.getEntityList().get(0);
                                        qua += goodsReceiptOrderDetails.getBaseuomdeliveredquantity();
                                    }
                                }
                            } else { //In Non Trading Flow 
                                KwlReturnObject grresult = accGoodsReceiptDAOobj.getReceiptDFromPOD(pDetail.getID());
                                List list = grresult.getEntityList();
                                Iterator ite1 = list.iterator();
                                while (ite1.hasNext()) {
                                    GoodsReceiptDetail ge = (GoodsReceiptDetail) ite1.next();
                                    qua += ge.getInventory().getBaseuomquantity();
                                }
                            }
                            if (qua < pDetail.getBaseuomquantity() && !(pDetail.isIsLineItemClosed())) {
                                TotalCount = TotalCount + (pDetail.getBaseuomquantity() - qua);
//                                break;

                            }
                        }

                    }
                }

            }
        } catch (Exception ex) {
            Logger.getLogger(accProductControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return TotalCount;
    }
 
 
 @Override
    public JSONObject getIndividualProductPrice(JSONObject paramsjobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {  
            String currencyID =paramsjobj.optString(Constants.globalCurrencyKey,"");
            String companyid= paramsjobj.getString(Constants.companyKey);
            boolean getSOPOflag = paramsjobj.optString("getSOPOflag",null) != null ? Boolean.parseBoolean(paramsjobj.getString("getSOPOflag")) : false;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            KwlReturnObject amendingPriceresult = accountingHandlerDAOobj.getObject(AmendingPrice.class.getName(), paramsjobj.optString("userid"));
            AmendingPrice AmendingPriceObj = amendingPriceresult != null ? (AmendingPrice) amendingPriceresult.getEntityList().get(0) : null;
            boolean isPriceFromBand = false ,isPriceBandMappedWithVolDisc=false;
            boolean isbandPriceNotAvailable = false; // Flag is used to warn user that pricing not available
            boolean isamendpurchasepriceNotAvl = false; // Flag is used to warn user that purchase (for amend fuctionality) is not available
            boolean isPriceFromVolumeDiscount = false;
            boolean isPriceFromUseDiscount = false;
            String discountType = "" ,volumeDiscountID="";
            double discountValue = 0;
            double qty=0d;
            boolean isIncludingGst = false;
            boolean isSpecialRateExist = false;
            HashMap<String, Object> requestParams = productHandler.getProductRequestMapfromJson(paramsjobj);

            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateOnlyFormat = authHandler.getDateOnlyFormatter(paramsjobj);
            String date = (String) requestParams.get("transactiondate");
            //carryin : This will be true for vendor documents, false for customer documents  
            boolean carryin = !StringUtil.isNullOrEmpty(paramsjobj.optString("carryin",null))?Boolean.parseBoolean(paramsjobj.getString("carryin")):false;
            String affecteduser = !StringUtil.isNullOrEmpty((String) requestParams.get("affecteduser"))?(String) requestParams.get("affecteduser"):"";
            String uomschematypeid =paramsjobj.optString("uomschematypeid",null);
            int  moduleid =paramsjobj.optInt("moduleid",0);
            String productId = paramsjobj.optString("productId",null);
            String currentuomid = paramsjobj.optString("currentuomid",null);
            String gcurrencyid =currencyID;
            boolean skipRichTextArea=false;
            if(paramsjobj.has("skipRichTextArea")){
                skipRichTextArea= Boolean.parseBoolean(String.valueOf(paramsjobj.get("skipRichTextArea")));
            }
            Date transactionDate = null;
            try {
                transactionDate = (date == null ? null : df.parse(date));
            } catch (ParseException ex) {
                Logger.getLogger(accProductControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE("getProducts : " + ex.getMessage(), ex);
            }
            /**
             *Special Rate check for purchase side
             */
            boolean isBandsWithSpecialRateForPurchase = false;
            JSONObject columnPrefjObj = new JSONObject();
            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                columnPrefjObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                if (columnPrefjObj.has("bandsWithSpecialRateForPurchase") && columnPrefjObj.get("bandsWithSpecialRateForPurchase") != null && columnPrefjObj.optBoolean("bandsWithSpecialRateForPurchase", false)) {
                    isBandsWithSpecialRateForPurchase = true;
                }
            }
            String productid[] = ((String) requestParams.get(Constants.productid)).split(",");
            if (!StringUtil.isNullOrEmpty(paramsjobj.optString(Constants.productid))) {
                for (int i = 0; i < productid.length; i++) {
                    JSONObject obj = new JSONObject();
                    KwlReturnObject result = null;
                   
                    // for get price from pricing band
                    if (extraCompanyPreferences != null && ((carryin && extraCompanyPreferences.isProductPricingOnBands()) || (!carryin && extraCompanyPreferences.isProductPricingOnBandsForSales()))) {
                        // if Price List band with special rate for sale activated then first check for special rate
                        if ((!carryin && extraCompanyPreferences.isProductPricingOnBandsForSales() && extraCompanyPreferences.isBandsWithSpecialRateForSales()) || (carryin && extraCompanyPreferences.isProductPricingOnBands() && isBandsWithSpecialRateForPurchase)) {
                            KwlReturnObject specialRateResult = accProductObj.getSpecialRateofProduct(productid[i], carryin, transactionDate, (String) requestParams.get("affecteduser"), (String) requestParams.get("forCurrency"));
                            if (specialRateResult.getEntityList() != null && !specialRateResult.getEntityList().isEmpty() && specialRateResult.getEntityList().get(0) != null) {
                                isSpecialRateExist = true;
                                double specialRate = (Double) specialRateResult.getEntityList().get(0);

                                obj.put("price", specialRate);
                                if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                    obj.put("rate", specialRate);
                                }
                                obj.put(Constants.productid, productid[i]);
                                jobj.append("data", obj);
                            }
                        }

                        if (!isSpecialRateExist) {
                            String pricingBandMasterID = "";
                            KwlReturnObject affectedUserResult = null;

                            boolean isVolumeDisocuntExist = false;
                            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                            filterRequestParams.put("companyid",companyid);
                            KwlReturnObject volmResult = accMasterItemsDAOobj.getPriceListVolumeDiscountItems(filterRequestParams);
                            if (volmResult != null && !volmResult.getEntityList().isEmpty()) {
                                isVolumeDisocuntExist = true;
                            }
                            obj.put("isVolumeDisocuntExist", isVolumeDisocuntExist);

                            // to get price from price list volume discount - use discount
                            HashMap<String, Object> pricingDiscountRequestParams = new HashMap<String, Object>();
                            pricingDiscountRequestParams.put("isPricePolicyUseDiscount", true);
                            pricingDiscountRequestParams.put("productID", productid[i]);
                            pricingDiscountRequestParams.put("isPurchase", carryin);
                            pricingDiscountRequestParams.put("applicableDate", transactionDate);
                            pricingDiscountRequestParams.put("companyID", companyid);
                            pricingDiscountRequestParams.put("currencyID", requestParams.get("currency"));
                            if (requestParams.containsKey("quantity") && requestParams.get("quantity") != null) {
                                qty=Double.parseDouble(requestParams.get("quantity").toString());
                                pricingDiscountRequestParams.put("quantity", requestParams.get("quantity"));
                            }
                            /**
                             *check price band is mapped with customer/vendor or not.
                             */
                            if (!carryin) { 
                                affectedUserResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) requestParams.get("affecteduser"));
                                Customer customer = affectedUserResult != null ? (Customer) affectedUserResult.getEntityList().get(0) : null;
                                if (customer != null && customer.getPricingBandMaster() != null) {
                                    pricingBandMasterID = customer.getPricingBandMaster().getID();
                                    isPriceFromBand = true;
                                    isIncludingGst = customer.getPricingBandMaster().isIsIncludingGST();
                                } else {
                                    isPriceFromBand = false;
                                }
                            } else { // check price band is mapped with vendor or not
                                affectedUserResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), (String) requestParams.get("affecteduser"));
                                Vendor vendor = affectedUserResult != null ? (Vendor) affectedUserResult.getEntityList().get(0) : null;
                                if (vendor != null && vendor.getPricingBandMaster() != null) {
                                    pricingBandMasterID = vendor.getPricingBandMaster().getID();
                                    isPriceFromBand = true;
                                    isIncludingGst = vendor.getPricingBandMaster().isIsIncludingGST();
                                } else {
                                    isPriceFromBand = false;
                                }
                            }
                            /**
                             * For purchase requisition is priceband where customer and vendor is not present so
                             * making isPriceFromBand true.
                             */
                            if (moduleid == Constants.Acc_Purchase_Requisition_ModuleId || moduleid ==  Constants.Acc_RFQ_ModuleId) {
                                isPriceFromBand = true;
                            }
                            /*
                             change mapped masterband to selected band
                             */
                            if (paramsjobj.has("pricingbandmaster") && !StringUtil.isNullOrEmpty(paramsjobj.getString("pricingbandmaster"))) {
                                pricingBandMasterID = paramsjobj.optString("pricingbandmaster", pricingBandMasterID);
                            }
                            /**
                             *checks volume discount matches with entered qty.
                             */
                            result = accProductObj.getProductPriceFromPriceListVolumeDiscount(pricingDiscountRequestParams);
                            /**
                             *getting the volume discount which fall under quantity and also mapped with customer/vendor. 
                             */
                            JSONObject JVolObj = accProductModuleService.getMappedVolumeDiscountwithBand(result, volumeDiscountID, pricingBandMasterID, qty);
                            int volumlistCount = JVolObj.optInt("volumlistCount");
                            volumeDiscountID = JVolObj.optString("volumeDiscountID");
                            isPriceBandMappedWithVolDisc = JVolObj.optBoolean("isPriceBandMappedWithVolDisc");
                            
                            if (result.getEntityList() != null && !result.getEntityList().isEmpty()) { // For Use Discount
                               /**
                                 * volumlistCount : Get productdetails record matched with volume discount.
                                 */
                                Object[] rowObj = (Object[]) result.getEntityList().get(volumlistCount);
                           
                                KwlReturnObject detailResult = accountingHandlerDAOobj.getObject(PricingBandMasterDetail.class.getName(), (String) rowObj[5]);
                                PricingBandMasterDetail pricingBandMasterDetail = detailResult != null ? (PricingBandMasterDetail) detailResult.getEntityList().get(0) : null;

                                discountType = pricingBandMasterDetail.getDiscountType();
                                discountValue = pricingBandMasterDetail.getDiscountValue();
                                volumeDiscountID = pricingBandMasterDetail.getPricingBandMaster().getID();
                                isPriceFromUseDiscount = true;
                                obj.put("isPriceFromUseDiscount", true);
                                obj.put("priceSourceUseDiscount", pricingBandMasterDetail.getPricingBandMaster().getName());

                                isPriceFromBand = true;
                                isPriceFromVolumeDiscount = false;
                            } else {
                                obj.put("isPriceFromUseDiscount", false);
                                // to get price from price list volume discount
                                pricingDiscountRequestParams.put("currencyID", requestParams.get("currency"));
                                pricingDiscountRequestParams.put("isPricePolicyUseDiscount", false);

                                result = accProductObj.getProductPriceFromPriceListVolumeDiscount(pricingDiscountRequestParams);
                                /**
                                 * getting the volume discount which fall under
                                 * quantity and also mapped with
                                 * customer/vendor.
                                 */
                                JVolObj = accProductModuleService.getMappedVolumeDiscountwithBand(result, volumeDiscountID, pricingBandMasterID, qty);
                                volumlistCount = JVolObj.optInt("volumlistCount");
                                volumeDiscountID = JVolObj.optString("volumeDiscountID");
                                isPriceBandMappedWithVolDisc = JVolObj.optBoolean("isPriceBandMappedWithVolDisc");
                                isPriceBandMappedWithVolDisc = JVolObj.optBoolean("isPriceBandMappedWithVolDisc");
                                if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                    /**
                                     * volumlistCount : Get productdetails
                                     * record matched with volume discount.
                                     */
                                    Object[] row = (Object[]) result.getEntityList().get(volumlistCount);
                                    KwlReturnObject detailResult = accountingHandlerDAOobj.getObject(PricingBandMasterDetail.class.getName(), (String) row[5]);
                                    PricingBandMasterDetail pricingBandMasterDetail = detailResult != null ? (PricingBandMasterDetail) detailResult.getEntityList().get(0) : null;
                                    volumeDiscountID = pricingBandMasterDetail.getPricingBandMaster().getID();
                                    if (row == null) {
                                        obj.put("isVolumeDisocunt", false);
                                        obj.put("price", 0);
                                        if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                            obj.put("rate", 0);
                                        }
                                        obj.put("priceSource", "");
                                    } else {
                                        isPriceFromVolumeDiscount = true;
                                        obj.put("isVolumeDisocunt", true);
                                        obj.put("price", row[0]);
                                        if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                            obj.put("rate", row[0]);
                                        }
                                        obj.put("priceSourceUseDiscount", pricingBandMasterDetail.getPricingBandMaster().getName());
                                        obj.put("purchaseprice", ((Double) row[6]) >= 0 ? (Double) row[6] : 0);
                                    }

                                    obj.put(Constants.productid, productid[i]);
                                    /*
                                    When quantity is zero in that case no need to put data
                                    */
                                    if (qty > 0) {
                                        jobj.append("data", obj);
                                    }
                                    
                                }
                            }

                            obj.put("pricingbandmasterid", pricingBandMasterID);
                            /*
                             If price band selected from drop down
                             */
                            if (!StringUtil.isNullObject(pricingBandMasterID)) {
                                isPriceFromBand = true;
                            }
                            if (isPriceFromBand) {
                                HashMap<String, Object> pricingBandRequestParams = new HashMap<String, Object>();
                                pricingBandRequestParams.put("productID", productid[i]);
                                pricingBandRequestParams.put("isPurchase", carryin);
                                pricingBandRequestParams.put("pricingBandMasterID", pricingBandMasterID);
                                pricingBandRequestParams.put("applicableDate", transactionDate);
                                pricingBandRequestParams.put("currencyID", requestParams.get("currency"));
                                pricingBandRequestParams.put("companyID", companyid);

                                result = accProductObj.getProductPriceFromPricingBand(pricingBandRequestParams);
                                isbandPriceNotAvailable=true;
                                if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                    isbandPriceNotAvailable=false;
                                    if (!isPriceBandMappedWithVolDisc) {
                                        obj.put("priceSource", "");
                                        obj.put("isVolumeDisocunt", false);
                                        obj.put("isPriceFromUseDiscount", false);
                                    }else{//put matched volume discount id to JS side
                                        obj.put("matchvolumeDiscountid", volumeDiscountID);
                                    }
                                    isPriceFromBand = true;
                                    Object[] row = (Object[]) result.getEntityList().get(0);

                                    if (row == null) {
                                        obj.put("isPriceListBand", false);
                                        obj.put("price", 0);
                                        if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                            obj.put("rate", 0);
                                        }
                                        obj.put("priceSource", "");
                                        obj.put("pricingbandmasterid", "");
                                    } else {
                                        double price = (Double) row[0];
                                        double purchaseprice = (Double) row[3];
                                        if (isPriceFromUseDiscount && requestParams.containsKey("quantity") && requestParams.get("quantity") != null && (isPriceBandMappedWithVolDisc)) {
                                            if (discountType.equalsIgnoreCase("0")) { // for Flat discount
                                                price = price - discountValue;
                                                purchaseprice = purchaseprice - discountValue;
                                            } else if (discountType.equalsIgnoreCase("1")) { // for Percent discount
                                                price = price - ((price * discountValue) / 100);
                                                purchaseprice = purchaseprice - ((purchaseprice * discountValue) / 100);
                                            }
                                        }
                                      /*
                                        if volume discount is applied to given qty
                                         */
                                        if ((isPriceFromVolumeDiscount && requestParams.containsKey("quantity") && requestParams.get("quantity") != null) && (isPriceBandMappedWithVolDisc)) {
                                            // dont put price values of band i.e. keep price value of flat discount which are put initial block 
                                        } else {
                                            obj.put("price", (price >= 0) ? price : 0);
                                            obj.put("purchaseprice", purchaseprice >= 0 ? purchaseprice : 0);
                                            obj.put("priceSource", row[2]);
                                        }
                                        
                                        if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                            obj.put("rate",  (price >= 0) ? price : 0);
                                        }
                                        obj.put("isPriceListBand", true);
                                        obj.put("isIncludingGst", isIncludingGst);
                                        obj.put("pricingbandmasterid", pricingBandMasterID);

                                        if (!carryin && !StringUtil.isNullOrEmpty((String) requestParams.get("affecteduser"))) {
                                            accProductModuleService.getProductBrandDiscountForBand(productid[i], (String) requestParams.get("affecteduser"), companyid, (String) requestParams.get("currency"), transactionDate, obj, pricingBandMasterID);
                                        }
                                    }
                                    obj.put(Constants.productid, productid[i]);
                                    jobj.append("data", obj);
                                } else {
                                    /**
                                     * Scenario 1 -->Price is only available in
                                     * SGD (Base Currency) -->Transaction is
                                     * entered in USD (Document Currency)
                                     */
                                    if (requestParams.containsKey("currency") && requestParams.get("currency") != null && !requestParams.get("currency").toString().equalsIgnoreCase(currencyID)) {
                                        /**
                                         * isPriceFromBand:- this flag is true when value of priceband is not present for foreign currency. then
                                         * price is converted from base currency of priceband. And below method checks the band is mapped with which volume discount.
                                         */
//                                        isbandPriceNotAvailable = false;
                                        List returnList = accProductModuleService.getPriceListBandPriceInDocumentCurrencyConvertedFromBaseCurrency(paramsjobj, pricingBandRequestParams, requestParams, productid[i], carryin, currencyID, obj, isPriceFromBand, isPriceFromUseDiscount, isPriceBandMappedWithVolDisc, isIncludingGst, transactionDate, discountType, discountValue, pricingBandMasterID, companyid, jobj);
                                        if (returnList != null && !returnList.isEmpty()) {
                                            isPriceFromBand = (Boolean) returnList.get(0);
                                            
                                        } else {
                                            isPriceFromBand = false;
                                            isbandPriceNotAvailable = true;
                                        }
                                    } else {
                                        isPriceFromBand = false;
                                        isbandPriceNotAvailable = true;
                                    }
                                }
                            }
                            /*
                             reset all if any volume discount not applied to band
                             */
                            if (!isPriceBandMappedWithVolDisc) {
                                affectedUserResult = accountingHandlerDAOobj.getObject(PricingBandMaster.class.getName(), pricingBandMasterID);
                                PricingBandMaster bandMaster = affectedUserResult != null ? (PricingBandMaster) affectedUserResult.getEntityList().get(0) : null;
                                if (bandMaster != null && !isbandPriceNotAvailable) {
                                    obj.put("priceSource", bandMaster.getName());
                                    obj.put("isPriceListBand", true);
                                    obj.put("pricingbandmasterid", pricingBandMasterID);
                                } else {
                                    obj.put("priceSource", "");
                                    obj.put("isPriceListBand", false);
                                    obj.put("pricingbandmasterid", "");
                                }
                                obj.put("isVolumeDisocunt", false);
                                obj.put("isPriceFromUseDiscount", false);
                                obj.put("isVolumeDisocuntExist", false);
                            }
                        }
                    }
                   
                    KwlReturnObject kwlReturnObject = accountingHandlerDAOobj.getObject(Product.class.getName(), productid[i]);
                    Product product = (Product) kwlReturnObject.getEntityList().get(0);
                    boolean isAssemblyProduct=product.getProducttype().getID().equals(Producttype.ASSEMBLY);
		    String producttype = product.getProducttype()!=null ? product.getProducttype().getID() : "";
                    obj.put("producttype", producttype);    //SDP-12202
                    /**
                     * Checking if UOM id is passed as param from js if not then
                     * setting products stock uomid and checking if price is
                     * mapped to uom, if not then passing uomid as empty to
                     * fetch the initial price of product.
                     * ERM-389 / ERP-35140
                     */
                    String uomid = "";
                    if (!StringUtil.isNullOrEmpty(paramsjobj.optString("uomid", ""))) {
                        uomid = paramsjobj.optString("uomid", "");
                    } else {
                        uomid = product != null ? product.getUnitOfMeasure() != null ? product.getUnitOfMeasure().getID() : "" : "";
                    }
                    boolean isUOMPresent = false;
                    if (!StringUtil.isNullOrEmpty(uomid)) {
                        isUOMPresent = accProductObj.checkIfPriceIsMappedToUOMInPriceList(productid[i], carryin, transactionDate, (String) requestParams.get("affecteduser"), (String) requestParams.get("forCurrency"), uomid);
                        if (!isUOMPresent) {
                            uomid = "";
                        }
                    }
                    
                    // if pricing band is not activated or band price for product is not available and (if special rate not exist)  then check from price list
                    /*
                     Adding (!isPriceFromVolumeDiscount || isbandPriceNotAvailable) = Volume discount not applied to qty and band price not available 
                     but thr might be case i.e. isPriceFromVolumeDiscount is true and isbandPriceNotAvailable is true in that case if block should be executed.
                     */
                    // For assembly product while calculating price for sales side document if above said condition is true then it will take sum of its component sales value.
                    if ((((carryin && extraCompanyPreferences.isProductPricingOnBands()) || (!carryin && extraCompanyPreferences.isProductPricingOnBandsForSales())) && !isPriceFromBand && (!isPriceFromVolumeDiscount || isbandPriceNotAvailable) && !isPriceBandMappedWithVolDisc && (requestParams.get("currency").toString().equalsIgnoreCase(currencyID) || extraCompanyPreferences.isProductPriceinMultipleCurrency()) || (carryin && !extraCompanyPreferences.isProductPricingOnBands() || !carryin && !extraCompanyPreferences.isProductPricingOnBandsForSales())) && !isSpecialRateExist) {
                        /**
                         * If price list band is activated and band is not set
                         * for vendor/customer then populate initial price in
                         * the purchase/sales transactions. ERP-32054
                         */
                        if (carryin && !extraCompanyPreferences.isProductPricingOnBands() || !carryin && !extraCompanyPreferences.isProductPricingOnBandsForSales()) {
                            /**
                             * If price list band is not activated, then take
                             * the price from pricelist.
                             */
                            obj.put("isPriceMappedToUOM", isUOMPresent);
                            boolean excludeInitialPrice = product.isAsset() ? false:true;
                            result = accProductObj.getProductPrice(productid[i], carryin, transactionDate, affecteduser, (String) requestParams.get("forCurrency"), uomid, excludeInitialPrice);            //ERM-389 / ERP-35140
                        } else {
                            /**
                             * If band is activated in company preferences and band is not set for the
                             * vendor/customer then populate initial price.
                             */
                            if (requestParams.get("currency") != null && product.getCurrency() != null && product.getCurrency().getCurrencyID().equals((String) requestParams.get("currency"))) {
                                result = accProductObj.getInitialPrice(productid[i], carryin);
                            } else {
                                result = null;
                            }
                        }

                        List list = result != null ? result.getEntityList() : new ArrayList();
                        Iterator itr = list.iterator();
                        if (itr.hasNext()) {
                            Object row = itr.next();
                            if (row == null) {
                                isbandPriceNotAvailable = true;
                                obj.put("price", 0);
                                if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                    obj.put("rate", 0);
                                }
                            } else {
                                isbandPriceNotAvailable = false;
                                double price = (Double) row;
                                /**
                                 * If price list band is activated and band is
                                 * not set for vendor/customer then populate
                                 * initial price in the purchase/sales
                                 * transactions. ERP-32054
                                 */
                                KwlReturnObject purchase = null;
                                if (carryin && !extraCompanyPreferences.isProductPricingOnBands() || !carryin && !extraCompanyPreferences.isProductPricingOnBandsForSales()) {
                                    /**
                                     * If price list band is not activated, then
                                     * take the price from pricelist.
                                     */
                                    boolean excludeInitialPrice = product.isAsset() ? false:true;
                                    purchase = accProductObj.getProductPrice(productid[i], true, transactionDate, affecteduser, (String) requestParams.get("forCurrency"), uomid, excludeInitialPrice);                 //ERM-389 / ERP-35140
                                } else {
                                    /**
                                     * If band is activated and band is not set
                                     * for the vendor/customer then populate
                                     * initial price.
                                     */
                                    if (requestParams.get("currency") != null && product.getCurrency() != null && product.getCurrency().getCurrencyID().equals((String) requestParams.get("currency"))) {
                                        purchase = accProductObj.getInitialPrice(productid[i], true);
                                    } else {
                                        purchase = null;
                                    }
                                }
                                double purchaseprice = purchase != null ? (purchase.getEntityList().isEmpty() ? 0 : (purchase.getEntityList().get(0) == null ? 0 : (Double) purchase.getEntityList().get(0))) : 0;
                                if (isPriceBandMappedWithVolDisc && isPriceFromUseDiscount && requestParams.containsKey("quantity") && requestParams.get("quantity") != null) {
                                    if (discountType.equalsIgnoreCase("0")) { // for Flat discount
                                        price = price - discountValue;
                                        purchaseprice = purchaseprice - discountValue;
                                    } else if (discountType.equalsIgnoreCase("1")) { // for Percent discount
                                        price = price - ((price * discountValue) / 100);
                                        purchaseprice = purchaseprice - ((purchaseprice * discountValue) / 100);
                                    }
                                }
                                obj.put("price", (price >= 0) ? price : 0);
                                /**
                                 * If the product is inventory Assembly type of product then following scenarios will apply:
                                 * 1 If price band is active and band price is set for Assembly type of product then it will take band price
                                 * 2 If band price is not active or band price not given for Assembly type of product and price is given in price list then it will take price list price
                                 * 3 If band price is not given and price list price is also not given then the price will be taken as sum of its BOM products.
                                 */
                                if (isAssemblyProduct && row==null && !carryin && !isSpecialRateExist && (extraCompanyPreferences.isProductPricingOnBandsForSales() && !isPriceFromBand && (!isPriceFromVolumeDiscount || isbandPriceNotAvailable) && !isPriceBandMappedWithVolDisc || (!extraCompanyPreferences.isProductPricingOnBandsForSales()))) {
                                    JSONObject assmPrdPriceObj = accProductModuleService.getAssemblyProductSalesPrice(paramsjobj, productid[i]);
                                    double priceForAssembly = assmPrdPriceObj.optDouble("price", 0);
                                    obj.put("price", priceForAssembly);
                                    if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                        obj.put("rate", priceForAssembly);
                                    }
                                    jobj.append("data", obj);
                                    if (price != 0) {
                                        isbandPriceNotAvailable = false;
                                    }
                                }
                                if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                    obj.put("rate", (price >= 0) ? price : 0);
                                }
                                obj.put("purchaseprice", purchaseprice >= 0 ? purchaseprice : 0);
                            }
                           
                        } else {
                            isbandPriceNotAvailable = true;
                            obj.put("price", 0);
                            if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                obj.put("rate", 0);
                            }
                        }
                        obj.put(Constants.productid, productid[i]);
                        jobj.append("data", obj);
                    } else if ((((carryin && extraCompanyPreferences.isProductPricingOnBands()) || (!carryin && extraCompanyPreferences.isProductPricingOnBandsForSales())) && !isPriceFromBand && !isPriceFromVolumeDiscount && !isPriceFromUseDiscount) && !isSpecialRateExist) {
                        isbandPriceNotAvailable = true;
                        obj.put("price", 0);                          //For Foreign currency if product isproductpricingonband is true
                        if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                            obj.put("rate",0);
                        }
                        obj.put(Constants.productid, productid[i]);
                        jobj.append("data", obj);
                    }
                    /**
                     * below code will execute when amend price fuctionality is activated.
                     */
                    accProductObj.getamendingPurchaseprice(productid[i], paramsjobj.optString("userid"), transactionDate, (String) requestParams.get("forCurrency"), uomid, obj);
                    
                    /**
                     * Scenario 2 -->Price is available in USD (Other than Base
                     * Currency) -->Transaction is entered in SGD (Base
                     * Currency) or any other currency aside from USD Result:
                     * warn user "Price is not available, please maintain your
                     * pricing"
                     */
                    if (isbandPriceNotAvailable && extraCompanyPreferences != null && ((carryin && extraCompanyPreferences.isProductPricingOnBands()) || (!carryin && extraCompanyPreferences.isProductPricingOnBandsForSales()))) {
                        obj.put("isbandPriceNotAvailable", isbandPriceNotAvailable);
                    }
                    
                    double availableQuantity = 0, lockQuantityInSelectedUOM = 0;
                    if (!StringUtil.isNullOrEmpty(productId)) {
                        KwlReturnObject resultavaiqty = accProductObj.getQuantity(productId);
                        obj.put("availablequantity", (resultavaiqty.getEntityList().get(0) == null ? 0 : resultavaiqty.getEntityList().get(0)));
                        availableQuantity =  (resultavaiqty.getEntityList().get(0) == null ? 0 :(Double) resultavaiqty.getEntityList().get(0));


                        KwlReturnObject productObj = accountingHandlerDAOobj.getObject(Product.class.getName(), productId);
                        Product pro = (Product) productObj.getEntityList().get(0);

                        if (pro.isblockLooseSell()) {
                            KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(productId, currentuomid);
                            availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                            qtyResult = accProductObj.getLockQuantityInSelectedUOM(productId, currentuomid);
                            lockQuantityInSelectedUOM = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                            HashMap<String, Object> orderParams = new HashMap<String, Object>();
                            orderParams.put("companyid", companyid);
                            orderParams.put("gcurrencyid", gcurrencyid);
                            orderParams.put("df", authHandler.getDateOnlyFormat());
                            orderParams.put("pendingapproval", false);
                            if (!StringUtil.isNullOrEmpty(paramsjobj.optString(Constants.REQ_startdate))) {
                                orderParams.put(Constants.REQ_startdate, authHandler.getDateOnlyFormat().parse(paramsjobj.optString(Constants.REQ_startdate)));
                            }
                            if (!StringUtil.isNullOrEmpty(paramsjobj.optString(Constants.REQ_enddate))) {
                                orderParams.put(Constants.REQ_enddate, authHandler.getDateOnlyFormat().parse(paramsjobj.optString(Constants.REQ_enddate)));
                            }
                            orderParams.put("productId", pro.getID());
                            double productCount = 0;
                            productCount = getOutstandingPoSoProductsCount(orderParams, true, pro.getID(), accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj, accSalesOrderDAOobj, accPurchaseOrderobj, currentuomid);//list1,

                            obj.put("pocountinselecteduom", productCount);
                            productCount = getOutstandingPoSoProductsCount(orderParams, false, pro.getID(), accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj, accSalesOrderDAOobj, accPurchaseOrderobj, currentuomid);//list1,

                            obj.put("socountinselecteduom", productCount);
                        }
                        if (!StringUtil.isNullOrEmpty(paramsjobj.optString("uomschematypeid"))) {                                                       
                            requestParams.put("uomschematypeid", uomschematypeid);
                            requestParams.put("currentuomid", currentuomid);
                            requestParams.put("companyid", companyid);
                            requestParams.put("carryin", carryin);
                            KwlReturnObject res = accProductObj.getProductBaseUOMRate(requestParams);
                            List list = res.getEntityList();
                            Iterator itr = list.iterator();
                            if (itr.hasNext()) {
                                UOMSchema row = (UOMSchema) itr.next();
                                if (row == null) {
                                    obj.put("baseuomrate", 1);
                                    obj.put("rateperuom", 0);
                                } else {
                                    obj.put("baseuomrate", row.getBaseuomrate());
                                    obj.put("rateperuom", row.getRateperuom());
                                }
                                obj.put("availableQtyInSelectedUOM", availableQuantity);
                                obj.put("lockQuantityInSelectedUOM", lockQuantityInSelectedUOM);
                                jobj.append("data", obj);
                            } else {
                                obj.put("baseuomrate", 1);
                                obj.put("rateperuom", 0);
                                obj.put("availableQtyInSelectedUOM", availableQuantity);
                                obj.put("lockQuantityInSelectedUOM", lockQuantityInSelectedUOM);
                                jobj.append("data", obj);
                            }
                        } else {
                            obj.put("baseuomrate", 1);
                            obj.put("rateperuom", 0);
                            obj.put("availableQtyInSelectedUOM", availableQuantity);
                            obj.put("lockQuantityInSelectedUOM", lockQuantityInSelectedUOM);
                            jobj.append("data", obj);
                        }
                        if (requestParams.containsKey("quantity") && requestParams.get("quantity") != null) {
                            qty = Double.parseDouble(requestParams.get("quantity").toString());
                        } else {
                            qty = 1;
                        }
                        double baseuomrate = 1;
                        if (obj.has("baseuomrate")) {
                            baseuomrate = obj.getDouble("baseuomrate");
                        }
                        JSONObject jObj = null;
                        if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                            jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                            if (jObj.has("isDisplayUOM") && jObj.get("isDisplayUOM") != null && (Boolean) jObj.get("isDisplayUOM") != false) {
                                obj = accProductObj.getProductDisplayUOM(product, qty, baseuomrate, carryin, obj);
                            }
                        }
                        jobj.append("data", obj);
                    }
                    if (getSOPOflag) {

                        HashMap<String, Object> requestParamsPOSO = new HashMap<String, Object>();
                        requestParamsPOSO.put("companyid", companyid);
                        requestParamsPOSO.put("gcurrencyid", currencyID);
                        requestParamsPOSO.put("df", authHandler.getDateOnlyFormat());
                        requestParamsPOSO.put("pendingapproval", false);
                        if (!StringUtil.isNullOrEmpty(paramsjobj.optString(Constants.REQ_startdate))) {
                            requestParamsPOSO.put(Constants.REQ_startdate, authHandler.getDateOnlyFormat().parse(paramsjobj.optString(Constants.REQ_startdate)));
                        }
                        if (!StringUtil.isNullOrEmpty(paramsjobj.optString(Constants.REQ_enddate))) {
                            requestParamsPOSO.put(Constants.REQ_enddate, authHandler.getDateOnlyFormat().parse(paramsjobj.optString(Constants.REQ_enddate)));
                        }
                        requestParamsPOSO.put("productId", product.getID());
                        
                        double quantity = 1;
                        double baseuomrate = 1;
                        JSONObject jObj = new JSONObject();
                        if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                            jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                        }
                        if (jObj.has("isDisplayUOM") && jObj.get("isDisplayUOM") != null && (Boolean) jObj.get("isDisplayUOM") != false) {
                            obj = accProductObj.getProductDisplayUOM(product, quantity, baseuomrate, carryin, obj);
                        }
                        if (!(paramsjobj.has(Constants.isdefaultHeaderMap) && paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true)) {
                            jobj.append("data", obj);//                        
                        }
                        String productuomId = (product.getUnitOfMeasure() != null) ? product.getUnitOfMeasure().getID() : "";
                        double productCount = getOutstandingPoSoProductsCount(requestParamsPOSO, true, product.getID(), accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj, accSalesOrderDAOobj, accPurchaseOrderobj, productuomId);
                        obj.put("pocountinselecteduom", productCount);
                        if (product != null) {
                            obj.put("producttype", product.getProducttype().getID());
                        }
                        productCount = getOutstandingPoSoProductsCount(requestParamsPOSO, false, product.getID(), accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj, accSalesOrderDAOobj, accPurchaseOrderobj, productuomId);
                        obj.put("socountinselecteduom", productCount);
                    }
                }

                if (productid.length == 1) {

                    transactionDate = (transactionDate == null) ? new Date() : transactionDate;
                    HashMap<String, Object> basicParams = new HashMap();
                    basicParams.put("productId", productid[0]);
                    basicParams.put("companyId", companyid);
                    basicParams.put("transactionDate", transactionDate);
                    basicParams.put("dateFormat", dateOnlyFormat);

                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                    fieldrequestParams.put("skipRichTextArea",skipRichTextArea);
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                    AccProductCustomData obj = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), productid[0]);
                    if (obj != null) {
                        productHandler.setCustomColumnValuesForProduct(obj, FieldMap, replaceFieldMap, variableMap);
                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                            String coldata = varEntry.getValue().toString();
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                JSONObject jsonObj = new JSONObject();
                                jsonObj.put(varEntry.getKey(), coldata);
                                jsonObj.put("key", varEntry.getKey());
                                jobj.append("data", jsonObj);
                            }
                        }
                    }
                }

            }
            
            /**
             * Below code is for getting multiple discount applied on product 
             * and passing it as JSON Object in discountData key
             */
            JSONObject jObj = null;
            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref()) && !isSpecialRateExist && !carryin) {
                jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                if (jObj.has(Constants.DISCOUNTMASTER) && jObj.get(Constants.DISCOUNTMASTER) != null && jObj.optBoolean(Constants.DISCOUNTMASTER,false)) {
                    JSONObject params = new JSONObject();
                    JSONObject resultJobj = jobj.getJSONArray("data").getJSONObject(0);
                    params.put("pricingBandMasterId", resultJobj.optString("pricingbandmasterid", ""));
                    params.put("productId", resultJobj.optString("productid", ""));
                    params.put("companyid", companyid);
                    params.put("applicableDate", transactionDate);
                    params.put("currencyId", requestParams.get("currency"));

                    JSONObject productDiscount = getIndividualProductDiscount(params);
                    JSONArray productDiscountJArr = productDiscount.getJSONArray("data");
                    if (productDiscountJArr.length() > 0) {
                        jobj.put("discountData", productDiscountJArr);
                    }
                }
            }
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

 @Override
 public  double getOutstandingPoSoProductsCount(HashMap<String, Object> requestParams, boolean isPo, String productid,
            AccountingHandlerDAO accountingHandlerDAOobj, accGoodsReceiptDAO accGoodsReceiptDAOobj, accInvoiceDAO accInvoiceDAOobj,
             accSalesOrderDAO accSalesOrderDAOobj,accPurchaseOrderDAO accPurchaseOrderobj, String compareUOMId) throws JSONException, ServiceException, SessionExpiredException {
        double count = 0;  //List list,
        try {
            KwlReturnObject productObj = accountingHandlerDAOobj.getObject(Product.class.getName(), productid);
            Product product = (Product) productObj.getEntityList().get(0);
            
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), (String) requestParams.get("companyid"));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
//            Iterator itr1 = list.iterator();
//            while (itr1.hasNext()) {
//                Object[] oj = (Object[]) itr1.next();
//                String orderid = oj[0].toString();
                //Withoutinventory 0 for normal, 1 for billing
//                boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());
            requestParams.put("compareUOMId",compareUOMId);
            boolean inSelectedUOM=true;
                if (isPo) {
//                    if (!withoutinventory) {
//                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), orderid);
//                        PurchaseOrder purchaseOrder = (PurchaseOrder) objItr.getEntityList().get(0);

//                        Set<PurchaseOrderDetail> orderDetail = purchaseOrder.getRows();
//                     KwlReturnObject result = accPurchaseOrderobj.getPodForProduct(requestParams);
//                     List list = result.getEntityList();
//                        
//                        Iterator ite = list.iterator();
//                         String result = "Closed";
                        
//                        double actualQty = 0;
//                        double deliveredQty = 0;
                        
//                        while (ite.hasNext()) {
//                            PurchaseOrderDetail pDetail = (PurchaseOrderDetail) ite.next();

                            if (product.isblockLooseSell() && !StringUtil.isNullOrEmpty(compareUOMId)) {
//                                if (compareUOMId.equals(pDetail.getUom().getID())) { //productid.equals(pDetail.getProduct().getID()) && 
//                                    count = getPurchaseOrderCountInSelectedUOM(accountingHandlerDAOobj, accGoodsReceiptDAOobj, pDetail, pref, count);
                                inSelectedUOM=true;
//                                }
                            } else {
//                                if (productid.equals(pDetail.getProduct().getID())) {
//                                    count = getPurchaseOrderCount(accountingHandlerDAOobj, accGoodsReceiptDAOobj, pDetail, pref, count);
                                inSelectedUOM=false;
//                                }
                            }
                            requestParams.put("inSelectedUOM", inSelectedUOM);
                            
                            if (requestParams.containsKey("isStockStatus") && requestParams.get("isStockStatus") != null && (Boolean)requestParams.get("isStockStatus") == true) {
                                count = getPurchaseOrderOutStanding(requestParams, accGoodsReceiptDAOobj, pref, count);
                            }else{
                                 count = getPurchaseOrderCountFromProduct(requestParams, accGoodsReceiptDAOobj, pref, count);
                            }

//                        }
//                    }
                } else {
//                    if (!withoutinventory) {
//                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), orderid);
//                        SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
//
//                        Set<SalesOrderDetail> orderDetail = salesOrder.getRows();
                        
//                        KwlReturnObject result = accSalesOrderDAOobj.getSodForProduct(requestParams);
//                        List list = result.getEntityList();
//                        Iterator ite = list.iterator();

//                        double actualQty = 0;
//                        double deliveredQty = 0;
                        boolean fullInv = false;
//                        while (ite.hasNext()) {
//                            SalesOrderDetail soDetail = (SalesOrderDetail) ite.next();

                            if (product.isblockLooseSell() && !StringUtil.isNullOrEmpty(compareUOMId)) {
//                                if (compareUOMId.equals(soDetail.getUom().getID())) { //productid.equals(soDetail.getProduct().getID()) &&
//                                    count = getSalesOrderCountInSelectedUOM(accountingHandlerDAOobj, accInvoiceDAOobj, soDetail, pref, fullInv, count);
                                inSelectedUOM=true;
//                                }
                            } else {
//                                if (productid.equals(soDetail.getProduct().getID())) {
//                                    count = getSalesOrderCount(accountingHandlerDAOobj, accInvoiceDAOobj, soDetail, pref, fullInv, count);
                                inSelectedUOM=false;
//                                }
                            }
                            requestParams.put("inSelectedUOM", inSelectedUOM);
                            /**
                            * SDP-14712: for Stock Status report, call new Method to
                            * get Out standing quantity.
                            */
                            if (requestParams.containsKey("isStockStatus") && requestParams.get("isStockStatus") != null && (Boolean) requestParams.get("isStockStatus") == true) {
                                count = getSalesOrderOutStanding(requestParams, accGoodsReceiptDAOobj, pref, count);
                            } else {
                            count = getSalesOrderCountFromProduct(requestParams, accInvoiceDAOobj, pref, count);
                            }
                            
//                        }
//                          if(result.equalsIgnoreCase("Open")){
//                             for (SalesOrderDetail salesOrderDetail : orderDetail) {
//                                 if(productid.equals(salesOrderDetail.getProduct().getID())){
//                                     count=count+salesOrderDetail.getQuantity();
//                                 }
//                             }
//                         }
                    }
//                }
//            }
        } catch (Exception exception) {
            throw ServiceException.FAILURE("getProductsCount : " + exception.getMessage(), exception);
        }
        return count;
    } 
 
 @Override
    public double getPurchaseOrderCountFromProduct(HashMap<String, Object> requestParams, accGoodsReceiptDAO accGoodsReceiptDAOobj, CompanyAccountPreferences pref, double count) throws ServiceException {
        double deliveredQty = 0;
        deliveredQty = accGoodsReceiptDAOobj.getGRODetailQuantityFromProduct(requestParams);
        count += deliveredQty;
        return count;
    }
    
    public double getPurchaseOrderOutStanding(HashMap<String, Object> requestParams, accGoodsReceiptDAO accGoodsReceiptDAOobj, CompanyAccountPreferences pref, double count) throws ServiceException {
        double deliveredQty = 0;
        deliveredQty = accGoodsReceiptDAOobj.getGRODetailQuantityForProduct(requestParams);
        count += deliveredQty;
        return count;
    }
 
    public double getSalesOrderOutStanding(HashMap<String, Object> requestParams, accGoodsReceiptDAO accGoodsReceiptDAOobj, CompanyAccountPreferences pref, double count) throws ServiceException {
        double deliveredQty = 0;
        deliveredQty = accInvoiceDAOobj.getDODetailQuantityForProduct(requestParams);
        count += deliveredQty;
        return count;
    }
 
@Override 
    public  double getSalesOrderCountFromProduct(HashMap<String, Object> requestParams, accInvoiceDAO accInvoiceDAOobj, CompanyAccountPreferences pref, double count) throws ServiceException {
        double deliveredQty = 0;
        deliveredQty = accInvoiceDAOobj.getDODetailQuantityFromProduct(requestParams);
        count += deliveredQty;
        return count;
    }

    @Override
    public JSONObject getIncidentCase(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        JSONObject jobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        JSONObject dataObj = null;

        KwlReturnObject resultObj = null;
            resultObj = accProductObj.getIncidentCase(paramJobj);
            int totalCount = resultObj.getRecordTotalCount();
            List<IncidentCases> list = resultObj.getEntityList();

            for (IncidentCases incident : list) {
                dataObj = new JSONObject();
                dataObj.put("id", incident.getID());
                dataObj.put("report", incident.getReport());
                dataObj.put("incidentdate", incident.getIncidentDate());
                dataObj.put("description", incident.getDescription());

                dataObj.put("attachment", incident.getAttatchment());
                dataObj.put("resolution", incident.getResolution());
                dataObj.put("location", incident.getLocation());
                dataObj.put("latitude", incident.getLatitude());
                dataObj.put("longitude", incident.getLongitude());
                dataObj.put("productname", incident.getProduct().getName());
                dataObj.put("productid", incident.getProduct().getID());
                dataObj.put("productcode", incident.getProduct()!=null?(!StringUtil.isNullOrEmpty(incident.getProduct().getProductid())?incident.getProduct().getProductid():""):"" );
                dataObj.put("incidenttime", incident.getIncidenttime());
                dataObj.put("deleted", incident.isDeleted());
                dataObj.put(Constants.customerid, incident.getCustomer() != null ? incident.getCustomer().getID() : "");
                dataObj.put("customername", incident.getCustomer() != null ? incident.getCustomer().getName() : "");
                dataObj.put("customercode", incident.getCustomer() != null ? (!StringUtil.isNullOrEmpty(incident.getCustomer().getAcccode())?incident.getCustomer().getAcccode():"") : "");
                dataArr.put(dataObj);
            }

        jobj.put(Constants.RES_success, true);
        jobj.put(Constants.RES_data, dataArr);
        if (totalCount != 0) {
            jobj.put(Constants.RES_TOTALCOUNT, totalCount);
        } else {
            jobj.put(Constants.RES_TOTALCOUNT, dataArr.length());
        }
        return jobj;
    }
    
    @Override
    public JSONObject getIncidentChart(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        JSONObject jobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        JSONObject dataObj = null;
        KwlReturnObject resultObj = null;
        resultObj = accProductObj.getIncidentChart(paramJobj);
        List<Object[]> list = resultObj.getEntityList();

        for (Object[] incident : list) {
            dataObj = new JSONObject();
            dataObj.put("month", incident[0]);
            dataObj.put("incidentcount", incident[1]);
            dataArr.put(dataObj);
        }
        jobj.put(Constants.RES_success, true);
        jobj.put(Constants.RES_data, dataArr);
        return jobj;
    }

    @Override
    public JSONObject saveIncidentCase(JSONObject paramJobj) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject resultObj = null;
        Map<String, Object> auditRequestParams = new HashMap<>();
        auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
        auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
        auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
        try {

            long createdon = System.currentTimeMillis();
            long updatedon = createdon;
            paramJobj.put("createdon", createdon);
            paramJobj.put("updatedon", updatedon);

            resultObj = accProductObj.saveIncidentCase(paramJobj);
            List<IncidentCases> list = resultObj.getEntityList();
            IncidentCases incidentCasesObj;
            String auditString ="";
            if (resultObj.getEntityList().size() > 0) {
                incidentCasesObj = list.get(0);
                jobj.put("id", list.get(0).getID());
                jobj.put("success", true);
                SimpleDateFormat sdf=new SimpleDateFormat(Constants.yyyyMMdd);                  
                SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
                SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
                Date time = parseFormat.parse(incidentCasesObj.getIncidenttime());
                String incidentTime = displayFormat.format(time);
                String incidentDate = sdf.format(incidentCasesObj.getIncidentDate());
                if (paramJobj.has("id")) {
                    auditString = incidentCasesObj.getModifiedby().getFullName() +" updated incident case for customer " + incidentCasesObj.getCustomer().getName() + " and product " + incidentCasesObj.getProduct().getName() + " rasied on " + incidentDate +" "+ incidentTime;
                } else {
                    auditString = incidentCasesObj.getCreatedby().getFullName()  +" created incident case for customer " + incidentCasesObj.getCustomer().getName() + " and product " + incidentCasesObj.getProduct().getName() + " on " + incidentDate +" "+ incidentTime;
                }
                auditTrailObj.insertAuditLog(AuditAction.INCIDENT_ADDED, auditString, auditRequestParams, incidentCasesObj.getID());
            } else {
                jobj.put("success", false);
            }

        } catch (JSONException | ParseException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    @Override
    public JSONObject deleteIncident(JSONObject paramJObj) throws JSONException, ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        JSONObject response = new JSONObject();
        try {
            if (paramJObj.has(Constants.deletepermanentflag) && paramJObj.optBoolean(Constants.deletepermanentflag, false) == true) {
                response = deleteIncidentPermanent(paramJObj);
            } else {
                response = deleteIncidentTemporary(paramJObj);
            }
            jobj.put(Constants.RES_msg, response.getString(Constants.RES_msg));
            jobj.put(Constants.RES_success, response.getBoolean(Constants.RES_success));
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new JSONException(ex.getMessage());
        } catch (Exception ex) {
            msg = "" + (ex.getMessage() != null ? ex.getMessage() : ex.getCause().getMessage());
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject deleteIncidentPermanent(JSONObject paramJobj) throws ServiceException, AccountingException, SessionExpiredException, ParseException {
        String msg = "";
        boolean issuccess = false;
        JSONObject response = new JSONObject();
        KwlReturnObject result = null;
        Map<String, Object> requestParams = new HashMap<>();
        Map<String, Object> auditRequestParams = new HashMap<>();
        auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
        auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
        auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
        try {
            JSONArray jArr = paramJobj.getJSONArray(Constants.RES_data);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                String incidentid = jobj.getString("id");
                requestParams.put("incidentId", incidentid);
                IncidentCases incidentCasesObj=accProductObj.getIncidentById(requestParams);
                SimpleDateFormat sdf=new SimpleDateFormat(Constants.yyyyMMdd);
                SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
                SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
                Date time = parseFormat.parse(incidentCasesObj.getIncidenttime());
                String incidentTime = displayFormat.format(time);              
                String incidentDate = sdf.format(incidentCasesObj.getIncidentDate());                
                String auditString=incidentCasesObj.getCreatedby().getFullName()+" permanently deleted incident case for customer " + incidentCasesObj.getCustomer().getName() + " and product " + incidentCasesObj.getProduct().getName() + " raised on " + incidentDate + " " + incidentTime;
                result = accProductObj.deleteIncidentPermanent(incidentid);
                msg = result.getMsg();
                auditTrailObj.insertAuditLog(AuditAction.INCIDENT_DELETED, auditString, auditRequestParams, incidentid);
            }

        } catch (JSONException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Cannot extract data from client");
        } finally {
            try {
                response.put(Constants.RES_success, issuccess);
                response.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return response;
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject deleteIncidentTemporary(JSONObject paramJobj) throws ServiceException, AccountingException, SessionExpiredException {
        String linkedTransaction = "";
        String msg = "";
        boolean issuccess = false;
        JSONObject response = new JSONObject();
        KwlReturnObject result = null;
        Map<String, Object> auditRequestParams = new HashMap<>();
        auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
        auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
        auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
        try {
            JSONArray jArr = paramJobj.getJSONArray(Constants.RES_data);
            String userId = paramJobj.optString(Constants.useridKey);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                long updatedon = System.currentTimeMillis();
                jobj.put("updatedon",updatedon);
                if(!StringUtil.isNullOrEmpty(userId)){
                    jobj.put(Constants.useridKey,userId);
                }
                result = accProductObj.deleteIncidentTemporary(jobj);
                IncidentCases incidentCasesObj = (IncidentCases) result.getEntityList().get(0);
                SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
                SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
                SimpleDateFormat sdf=new SimpleDateFormat(Constants.yyyyMMdd);
                Date time = parseFormat.parse(incidentCasesObj.getIncidenttime());
                String incidentTime = displayFormat.format(time);
                String incidentDate = sdf.format(incidentCasesObj.getIncidentDate());   
                String auditString=incidentCasesObj.getModifiedby().getFullName()+" temporary deleted incident case for customer " + incidentCasesObj.getCustomer().getName() + " and product " + incidentCasesObj.getProduct().getName() + " raised on " + incidentDate + " "  + incidentTime;
                msg = result.getMsg();                
                auditTrailObj.insertAuditLog(AuditAction.INCIDENT_DELETED, auditString, auditRequestParams, jobj.optString("id"));
            }

        } catch (JSONException ex) {
            throw new AccountingException("Cannot extract data from client");
        } catch(Exception ex){
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                response.put(Constants.RES_success, issuccess);
                response.put(Constants.RES_msg, msg);
                response.put(Constants.linkedTransaction, linkedTransaction);
            } catch (JSONException ex) {
                Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return response;
    }

    public void getDepreciationJsonFormonthwise(int Depreciationyear, JSONObject newjobj, int monthval, JSONObject subtotal, JSONObject grandTotal) throws ServiceException, JSONException {
        if (subtotal.has("month" + Depreciationyear + "_" + monthval)) {
            subtotal.put("month" + Depreciationyear + "_" + monthval, subtotal.getDouble("month" + Depreciationyear + "_" + monthval) + newjobj.getDouble("firstperiodamtInBase"));
        } else {
            subtotal.put("month" + Depreciationyear + "_" + monthval, newjobj.getDouble("firstperiodamtInBase"));
        }
        if (grandTotal.has("month" + Depreciationyear + "_" + monthval)) {
            grandTotal.put("month" + Depreciationyear + "_" + monthval, grandTotal.getDouble("month" + Depreciationyear + "_" + monthval) + newjobj.getDouble("firstperiodamtInBase"));
        } else {
            grandTotal.put("month" + Depreciationyear + "_" + monthval, newjobj.getDouble("firstperiodamtInBase"));
        }
    }
    public Map<String, List<Object[]>> getBatchDetailsMap(Map<String, Object> requestParams) {
        Map<String, List<Object[]>> baMap = new HashMap<>();
        try {
            boolean linkingFlag = false;
            if (requestParams.containsKey("linkingFlag")) {
                linkingFlag = Boolean.parseBoolean(requestParams.get("linkingFlag").toString());
            }
            boolean isEdit = false;
            if (requestParams.containsKey("isEdit")) {
                isEdit = Boolean.parseBoolean(requestParams.get("isEdit").toString());
            }
            String moduleID = "";
            if (requestParams.containsKey("moduleID")) {
                moduleID = requestParams.get("moduleID").toString();
            }
            String documentIds = "";
            if (requestParams.containsKey("documentIds")) {
                documentIds = requestParams.get("documentIds").toString();
            }
            KwlReturnObject kmsg = accCommonTablesDAO.getBatchSerialDetails("", true, linkingFlag, moduleID, false, isEdit, documentIds);
            List<Object[]> batchserialdetails = kmsg.getEntityList();
            for (Object[] objects : batchserialdetails) {
                if (objects.length >= 20 && objects[20] != null) {  // chek wheather result having the documentid or not
                    if (baMap.containsKey(objects[20].toString())) {
                        List<Object[]> details = baMap.get(objects[20].toString());
                        details.add(objects);
                        baMap.put(objects[20].toString(), details);
                    } else {
                        List<Object[]> details = new ArrayList<>();
                        details.add(objects);
                        baMap.put(objects[20].toString(), details);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return baMap;
    }
    /*
     Class to perform sorting on the transaction date and if two transction is having same transaction dates then in that the transactionno will be compared
     */

    @Override
    public JSONObject saveQAApprovalDetails(HashMap<String, Object> QaDetailsMap) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            KwlReturnObject QaObj = accProductObj.saveQAApprovalDetails(QaDetailsMap);
            AssemblyProductApprovalDetails apdq = (AssemblyProductApprovalDetails) QaObj.getEntityList().get(0);
        } catch (Exception e) {
            throw ServiceException.FAILURE("AccProductServiceImpl.saveQAApprovalDetails", e);
        }
        return jobj;
    }

    @Override
    public JSONObject checkAssetIdIsPresent(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jobj= new JSONObject();
        boolean isDuplicate=false;
        try{
            KwlReturnObject kwlresult=accProductObj.checkIsAssetIDAlreadyUsed(requestParams);
            
            if(kwlresult.getRecordTotalCount() > 0){
                isDuplicate=true;
            }
            jobj.put("isDuplicate", isDuplicate);
        
        }catch(Exception ex){
             throw ServiceException.FAILURE("AccProductServiceImpl.saveQAApprovalDetails", ex);
        }
        return jobj;
    }

    /*  Fetch Block Quantity*/
    @Override
    public JSONObject getBlockedQuantityOfProduct(HashMap<String, Object> requestParams) throws ServiceException {
       
        JSONObject obj = new JSONObject();
       
        try {

            String ss = (String) requestParams.get("ss");
            Product product = (Product) requestParams.get("productObj");
            Company compObj = (Company) requestParams.get("companyObj");
            /*Calculating block quantity batch serial wise  */
            Map<String, Double> blockedQuantityList = accProductObj.getProductBlockedQuantity(compObj, null, null, ss);
            UnitOfMeasure uom = product.getUnitOfMeasure();
            double lockQuantityInSelectedUOM = 0;

            /*If "block loose selling" option is true for that particular product */
            if (product.isblockLooseSell()) {
                KwlReturnObject qtyResult = accProductObj.getLockQuantityInSelectedUOM(product.getID(), uom.getID());
                lockQuantityInSelectedUOM = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                obj.put("lockquantity", (lockQuantityInSelectedUOM));
            } else {

                /* Calculating Block Quantity */
                KwlReturnObject result2 = accProductObj.getAssemblyLockQuantity(product.getID());
                Double assmblyLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));

                KwlReturnObject result1 = accProductObj.getLockQuantity(product.getID());
                Double SoLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));

                KwlReturnObject woresult = accProductObj.getWOLockQuantity(product.getID());
                Double WOLockQuantity = (Double) (woresult.getEntityList().get(0) == null ? 0.0 : woresult.getEntityList().get(0));

                lockQuantityInSelectedUOM = assmblyLockQuantity + SoLockQuantity + WOLockQuantity; // lockquantity

                obj.put("lockquantity", (lockQuantityInSelectedUOM));
            }
            if (blockedQuantityList.containsKey(product.getID())) {

                lockQuantityInSelectedUOM = blockedQuantityList.get(product.getID());
                obj.put("lockquantity", lockQuantityInSelectedUOM);
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("AccProductServiceImpl.saveQAApprovalDetails", ex);
        }
        return obj;
    }

    
    class SortTransactionMap {

        public Map<String, PriceValuationStack.Batch> getSortedMap(Map<String, PriceValuationStack.Batch> mapToSort) {
            Set<Map.Entry<String, PriceValuationStack.Batch>> mapEntry = mapToSort.entrySet();

            List<Map.Entry<String, PriceValuationStack.Batch>> listMap = new ArrayList<Map.Entry<String, PriceValuationStack.Batch>>();
            listMap.addAll(mapEntry);
            Collections.sort(listMap, new Comparator<Map.Entry<String, PriceValuationStack.Batch>>() {

                @Override
                public int compare(Map.Entry<String, PriceValuationStack.Batch> o1, Map.Entry<String, PriceValuationStack.Batch> o2) {
                    int returnVal = 0;
                    PriceValuationStack.Batch batch1 = o1.getValue();
                    PriceValuationStack.Batch batch2 = o2.getValue();
                    if (batch1.getTransactionDate() != null && batch2.getTransactionDate() != null) {
                        returnVal = batch1.getTransactionDate().compareTo(batch2.getTransactionDate());
                    } else {
                        returnVal = -1;
                    }
                    if (returnVal == 0 && (batch1.getCreatedon()!= null && batch2.getCreatedon() != null)) {
                        returnVal = batch1.getCreatedon().compareTo(batch2.getCreatedon());
                    }
                    if (returnVal == 0 && (batch1.getSrNo()!= null && batch2.getSrNo() != null)) {
                        returnVal = batch1.getSrNo().compareTo(batch2.getSrNo());
                    }
                    if (returnVal == 0 && (batch1.getTransactionNo().equals(batch2.getTransactionNo()))) {
                        /**
                         * Stock Issue should be shown before the stock collect.
                         */
                        returnVal = (batch1.getDocType() < batch2.getDocType()) ? -1 : ((batch1.getDocType() == batch2.getDocType()) ? 0 : 1);
                    }
                    return returnVal;
                }

            });
            Map<String, PriceValuationStack.Batch> sortedMap = new LinkedHashMap<>();
            for (Map.Entry<String, PriceValuationStack.Batch> mapE : listMap) {
                sortedMap.put(mapE.getKey(), mapE.getValue());
            }
            return sortedMap;
        }
    }
    
    /**
     * Method is used to check negative stock is present in system or not.
     *
     * @param requestParams Request parameters
     * @return boolean <code>true</code> Negative stock is present. <code>false</code>Negative stock is not present.
     *
     */
    @Override
    public boolean isNegativeStockPresent(HashMap<String, Object> requestParams) {
        boolean isNegativeStockPresent = false;
        String companyid = "";
        if(requestParams.containsKey("companyid")){
            companyid = (String) requestParams.get("companyid");
        }
        try {
            Map<String, List> proMap = getProductTransaction(requestParams);
            if (!proMap.isEmpty()) {
                for (Map.Entry<String, List> entry : proMap.entrySet()) {
                    String productid = entry.getKey();
                    List list = entry.getValue();
                    if (list != null && !list.isEmpty()) {
                        Product product = (Product) kwlCommonTablesDAOObj.getClassObject(Product.class.getName(), productid);
                        if (product != null) {
                            Map<String, Map<String, Double>> productQtyMap = buildProductQtyMap(list, product, companyid);
                            if (productQtyMap != null && !productQtyMap.isEmpty()) {
                                for (Map.Entry<String, Map<String, Double>> productQtyMapEntry : productQtyMap.entrySet()) {
                                    Map<String, Double> storageQtyMap = productQtyMapEntry.getValue();
                                    for (Map.Entry<String, Double> storageQtyMapEntry : storageQtyMap.entrySet()) {
                                        Double quantity = storageQtyMapEntry.getValue();
                                        if (quantity < 0) {
                                            isNegativeStockPresent = true;
                                            break;
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ServiceException ex) {
            isNegativeStockPresent = true;
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
        return isNegativeStockPresent;
    }
    
    private Map<String, Map<String, Double>> buildProductQtyMap(List list, Product product, String companyid) {
        Map<String, Map<String, Double>> productQtyMap = new HashMap<>();
        if (list != null && !list.isEmpty()) {
            for (Object object : list) {
                Object[] row = (Object[]) object;
                double quantity = (Double) row[9];
                BigInteger transType = (BigInteger) row[0];
                double baseUOMRate;
                if (row[11] instanceof BigInteger) {
                    BigInteger baseRate = (BigInteger) row[11];
                    baseUOMRate = baseRate.doubleValue();
                } else {
                    baseUOMRate = (Double) row[11];
                }
                quantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate, companyid);
                Map storageParams = new HashMap();
                if (row[24] != null && !StringUtil.isNullOrEmpty((String) row[24])) { // Warehouse ID
                    storageParams.put("warehouseId", (String) row[24]);
                }
                if (row[23] != null && !StringUtil.isNullOrEmpty((String) row[23])) { // Location ID
                    storageParams.put("locationId", (String) row[23]);
                }
                if (row[25] != null && !StringUtil.isNullOrEmpty((String) row[25])) { // Row ID
                    storageParams.put("rowId", (String) row[25]);
                }
                if (row[26] != null && !StringUtil.isNullOrEmpty((String) row[26])) { // Rack ID
                    storageParams.put("rackId", (String) row[26]);
                }
                if (row[27] != null && !StringUtil.isNullOrEmpty((String) row[27])) { // BIN ID
                    storageParams.put("binId", (String) row[27]);
                }
                if (row[28] != null && !StringUtil.isNullOrEmpty((String) row[28])) { // Batch Name
                    storageParams.put("batchName", (String) row[28]);
                }
                if (!storageParams.isEmpty() && row[29] != null) {
                    if (!StringUtil.isNullOrEmpty(row[29].toString())) {
                        quantity = Double.parseDouble(row[29].toString());
                    }
                }
                String storageKey = PriceValuationStack.getStoregeKey(storageParams);
                TransType transactionType = PriceValuationStack.getTransType(transType.intValue());
                if (productQtyMap.containsKey(product.getID())) {
                    Map<String, Double> storageQtyMap = productQtyMap.get(product.getID());
                    if (storageQtyMap.containsKey(storageKey)) {
                        Double presentQty = storageQtyMap.get(storageKey);
                        if (transactionType == TransType.IN || transactionType == TransType.OUT_RETURN) {
                            storageQtyMap.put(storageKey, authHandler.round((presentQty + quantity),companyid));
                        } else  if(transactionType == TransType.OUT || transactionType == TransType.IN_RETURN){
                            storageQtyMap.put(storageKey, authHandler.round((presentQty - quantity),companyid));
                        }
                    } else {
                        if (transactionType == TransType.OUT || transactionType == TransType.IN_RETURN) {
                            storageQtyMap.put(storageKey, (quantity * -1));
                        }else{
                            storageQtyMap.put(storageKey, quantity);
                        }
                    }
                    productQtyMap.put(product.getID(), storageQtyMap);
                } else {
                    Map<String, Double> storageQtyMap = new HashMap<>();
                    if (transactionType == TransType.OUT || transactionType == TransType.IN_RETURN) {
                        storageQtyMap.put(storageKey, (quantity * -1));
                    } else {
                        storageQtyMap.put(storageKey, quantity);
                    }
                    productQtyMap.put(product.getID(), storageQtyMap);
                }
            }
        }
        return productQtyMap;
    }
    
    /**
     * Method is used to generate the serial string. Method is used in Stock
     * Ledger Detailed Report.
     *
     * @param serialMap
     * @return serial string (For example, Serial Name[Batch: Batch Name,
     * Location: Location Name, Warehouse: Warehouse Name])
     */
    private StringBuilder serialKeyBuilder(Map<String, Double> serialMap) {
        StringBuilder serialBuilder = new StringBuilder();
        try {
            for (Map.Entry<String, Double> entrySet : serialMap.entrySet()) {
                String key = entrySet.getKey();
                String[] keyArr = key.split("\\|");
                String batchName = keyArr[2];
                String locationId = keyArr[0], warehouseId = keyArr[1], serialId = keyArr[3];
                if (!StringUtil.isNullOrEmpty(serialId)) {
                    KwlReturnObject loct = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), serialId);
                    NewBatchSerial newBatchSerial = (NewBatchSerial) loct.getEntityList().get(0);
                    if (newBatchSerial != null && !StringUtil.isNullOrEmpty(newBatchSerial.getSerialname())) {
                        if (serialBuilder.length() > 0) {
                            serialBuilder.append("; ").append(newBatchSerial.getSerialname());
                        } else {
                            serialBuilder.append(newBatchSerial.getSerialname());
                        }
                        String locationName = "", warehouseName = "";
                        if (!StringUtil.isNullOrEmpty(locationId) && !locationId.equals(" ")) {
                            loct = accountingHandlerDAOobj.getObject(Location.class.getName(), locationId);
                            Location location = (Location) loct.getEntityList().get(0);
                            if (location != null && !StringUtil.isNullOrEmpty(location.getName())) {
                                locationName = location.getName();
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(warehouseId) && !warehouseId.equals(" ")) {
                            loct = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), warehouseId);
                            InventoryWarehouse warehouse = (InventoryWarehouse) loct.getEntityList().get(0);
                            if (warehouse != null && !StringUtil.isNullOrEmpty(warehouse.getName())) {
                                warehouseName = warehouse.getName();
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(locationName) || !StringUtil.isNullOrEmpty(warehouseName) || !StringUtil.isNullOrEmpty(batchName)) {
                            serialBuilder.append("[");
                            boolean keyAdded = false;
                            if (!StringUtil.isNullOrEmpty(batchName)) {
                                serialBuilder.append("Batch : ").append(batchName);
                                keyAdded = true;
                            }
                            if (!StringUtil.isNullOrEmpty(locationName)) {
                                if (keyAdded) {
                                    serialBuilder.append(",").append("Location : ").append(locationName);
                                } else {
                                    serialBuilder.append("Location : ").append(locationName);
                                    keyAdded = true;
                                }
                            }
                            if (!StringUtil.isNullOrEmpty(warehouseName)) {
                                if (keyAdded) {
                                    serialBuilder.append(",").append("Warehouse : ").append(warehouseName);
                                } else {
                                    serialBuilder.append("Warehouse : ").append(warehouseName);
                                }
                            }
                            serialBuilder.append("]");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return serialBuilder;
    }

    /**
     * Method is used to generate the location string. Method is used in Stock
     * Ledger Detailed Report.
     *
     * @param locationMap
     * @return location string (For example, Location Name [Quantity])
     */
    private StringBuilder locationKeyBuilder(Map<String, Double> locationMap) {
        StringBuilder locationBuilder = new StringBuilder();
        try {
            for (Map.Entry<String, Double> entrySet : locationMap.entrySet()) {
                String locationId = entrySet.getKey();
                Double quantity = entrySet.getValue();
                KwlReturnObject loct = accountingHandlerDAOobj.getObject(Location.class.getName(), locationId);
                Location location = (Location) loct.getEntityList().get(0);
                if (location != null && !StringUtil.isNullOrEmpty(location.getName())) {
                    if (locationBuilder.length() == 0) {
                        locationBuilder.append(location.getName()).append(" [").append(quantity).append("] ");
                    } else {
                        locationBuilder.append("; ").append(location.getName()).append(" [").append(quantity).append("] ");
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return locationBuilder;
    }

    /**
     * Method is used to generate the warehouse string. Method is used in Stock
     * Ledger Detailed Report.
     *
     * @param warehouseMap
     * @return warehouse string (For example, Warehouse Name [Quantity])
     */
    private StringBuilder warehouseKeyBuilder(Map<String, Double> warehouseMap) {
        StringBuilder warehouseBuilder = new StringBuilder();
        try {
            for (Map.Entry<String, Double> entrySet : warehouseMap.entrySet()) {
                String warehouseId = entrySet.getKey();
                Double qty = entrySet.getValue();
                KwlReturnObject loct = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), warehouseId);
                InventoryWarehouse warehouse = (InventoryWarehouse) loct.getEntityList().get(0);
                if (warehouse != null && !StringUtil.isNullOrEmpty(warehouse.getName())) {
                    if (warehouseBuilder.length() == 0) {
                        warehouseBuilder.append(warehouse.getName()).append(" [").append(qty).append("] ");
                    } else {
                        warehouseBuilder.append("; ").append(warehouse.getName()).append(" [").append(qty).append("] ");
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return warehouseBuilder;
    }

    /**
     * Method is used to generate the batch string. Method is used in Stock
     * Ledger Detailed Report.
     *
     * @param batchMap
     * @return batch string (For example, Batch Name[Location: Location Name,
     * Warehouse: Warehouse Name] [Quantity])
     */
    private StringBuilder batchKeyBuilder(Map<String, Double> batchMap) {
        StringBuilder batchBuilder = new StringBuilder();
        try {
            for (Map.Entry<String, Double> entrySet : batchMap.entrySet()) {
                Double qty = entrySet.getValue();
                String key = entrySet.getKey();
                String[] keyArr = key.split("\\|");
                String batchName = keyArr[2];
                String locationId = keyArr[0], warehouseId = keyArr[1];
                if (!StringUtil.isNullOrEmpty(batchName)) {
                    if (batchBuilder.length() > 0) {
                        batchBuilder.append("; ").append(batchName);
                    } else {
                        batchBuilder.append(batchName);
                    }
                }
                String locationName = "", warehouseName = "";
                if (!StringUtil.isNullOrEmpty(locationId) && !locationId.equals(" ")) {
                    KwlReturnObject loct = accountingHandlerDAOobj.getObject(Location.class.getName(), locationId);
                    Location location = (Location) loct.getEntityList().get(0);
                    if (location != null && !StringUtil.isNullOrEmpty(location.getName())) {
                        locationName = location.getName();
                    }
                }
                if (!StringUtil.isNullOrEmpty(warehouseId) && !warehouseId.equals(" ")) {
                    KwlReturnObject loct = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), warehouseId);
                    InventoryWarehouse warehouse = (InventoryWarehouse) loct.getEntityList().get(0);
                    if (warehouse != null && !StringUtil.isNullOrEmpty(warehouse.getName())) {
                        warehouseName = warehouse.getName();
                    }
                }
                if (!StringUtil.isNullOrEmpty(locationName) || !StringUtil.isNullOrEmpty(warehouseName)) {
                    batchBuilder.append("[");
                    boolean keyAdded = false;
                    if (!StringUtil.isNullOrEmpty(locationName)) {
                        batchBuilder.append("Location : ").append(locationName);
                        keyAdded = true;
                    }
                    if (!StringUtil.isNullOrEmpty(warehouseName)) {
                        if (keyAdded) {
                            batchBuilder.append(",").append("Warehouse : ").append(warehouseName);
                        } else {
                            batchBuilder.append("Warehouse : ").append(warehouseName);
                        }
                    }
                    batchBuilder.append("]");
                }
                if (!StringUtil.isNullOrEmpty(batchName)) {
                    batchBuilder.append(" [").append(qty).append("] ");
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return batchBuilder;
    }
    
    
    /**
     * Method is used to generate the inventory details such as Location, Batch, Serial, Row, Rack, Bin for Material IN/OUT Report.
     * @param Map.Entry
     * @return JSONObject
     */
    private JSONObject locationMapBuilder(Map.Entry<String, Double> entrySet) {
        JSONObject locationMapBuilder = new JSONObject();
        try {
            Double qty = entrySet.getValue();
            String key = entrySet.getKey();
            String[] keyArr = key.split("\\|");
            /**
             * Transaction Location Map for Material In/Out Report. Key to Map
             * is build as : Location | Batch | Serial | Row | Rack | Bin
             */
            String locationId = keyArr[0], batchName = keyArr[1], serialId = keyArr[2], rowId = keyArr[3], rackId = keyArr[4], binId = keyArr[5];
            if (batchName != null && !StringUtil.isNullOrEmpty(batchName.trim())) {
                locationMapBuilder.put(Constants.MATERIAL_IN_OUT_REPORT_BATCH_NAME, batchName);
            }
            if (!StringUtil.isNullOrEmpty(locationId) && !locationId.equals(" ")) {
                KwlReturnObject loct = accountingHandlerDAOobj.getObject(Location.class.getName(), locationId);
                Location location = (Location) loct.getEntityList().get(0);
                if (location != null && !StringUtil.isNullOrEmpty(location.getName())) {
                    locationMapBuilder.put(Constants.MATERIAL_IN_OUT_REPORT_LOCATION_NAME, location.getName());
                }
            }
            locationMapBuilder.put(Constants.MATERIAL_IN_OUT_REPORT_QUANTITY, qty);
            if (!StringUtil.isNullOrEmpty(serialId)) {
                KwlReturnObject loct = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), serialId);
                NewBatchSerial newBatchSerial = (NewBatchSerial) loct.getEntityList().get(0);
                if (newBatchSerial != null && !StringUtil.isNullOrEmpty(newBatchSerial.getSerialname())) {
                    locationMapBuilder.put(Constants.MATERIAL_IN_OUT_REPORT_SERIAL_NAME, newBatchSerial.getSerialname());
                }
            }
            if (!StringUtil.isNullOrEmpty(rowId)) {
                KwlReturnObject rowObj = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), rowId);
                StoreMaster row = (StoreMaster) rowObj.getEntityList().get(0);
                if (row != null && !StringUtil.isNullOrEmpty(row.getName())) {
                    locationMapBuilder.put(Constants.MATERIAL_IN_OUT_REPORT_ROW_NAME, row.getName());
                }
            }
            if (!StringUtil.isNullOrEmpty(rackId)) {
                KwlReturnObject rackObj = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), rackId);
                StoreMaster rack = (StoreMaster) rackObj.getEntityList().get(0);
                if (rack != null && !StringUtil.isNullOrEmpty(rack.getName())) {
                    locationMapBuilder.put(Constants.MATERIAL_IN_OUT_REPORT_RACK_NAME, rack.getName());
                }
            }
            if (!StringUtil.isNullOrEmpty(binId)) {
                KwlReturnObject binObj = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), binId);
                StoreMaster bin = (StoreMaster) binObj.getEntityList().get(0);
                if (bin != null && !StringUtil.isNullOrEmpty(bin.getName())) {
                    locationMapBuilder.put(Constants.MATERIAL_IN_OUT_REPORT_BIN_NAME, bin.getName());
                }
            }
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return locationMapBuilder;
    }
    /**
     * Method is used to build the transaction JSON for Material IN/OUT Report.
     *
     * @param Product
     * @param PriceValuationStack.Batch
     * @param Store
     * @param DateFormat
     * @return 
     */
    private JSONObject getTransactionJSON(Product product, PriceValuationStack.Batch batch, Store iwStore, DateFormat df, String companyid) {
        JSONObject jObj = new JSONObject();
        JSONArray smDetails = new JSONArray();
        try {
            jObj.put("itemcode", product.getProductid());
            jObj.put("itemdescription", product.getDescription());
            jObj.put("itemname", product.getName());
            jObj.put("store", (iwStore == null) ? "" : iwStore.getDescription());
            String assemblyProductName = "";
            Map<String, Object> map = new HashMap<>();
            Object res = null;
            if (!StringUtil.isNullOrEmpty(batch.getAssemblyProductID())) {
//                assemblyProductName = (String)res.getEntityList().get(0);
                map.put("id", batch.getAssemblyProductID());
                res = kwlCommonTablesDAOObj.getRequestedObjectFields(Product.class, new String []{"name"}, map);
                assemblyProductName = res != null ?(String)res : "";
            }
            jObj.put("assemble", assemblyProductName);
            String costCenterName = "";
            if (!StringUtil.isNullOrEmpty(batch.getCostCenterID())) {
                map.put("id", batch.getCostCenterID());
                res = kwlCommonTablesDAOObj.getRequestedObjectFields(CostCenter.class, new String []{"name"}, map);
                costCenterName = res != null ?(String)res : "";
//                costCenterName = accProductObj.getNamebyId(batch.getCostCenterID(), "costcenter");
            }
            String stockUOMName = "";
            if (!StringUtil.isNullOrEmpty(batch.getStockUOMID())) {
                map.put("id", batch.getStockUOMID());
                res = kwlCommonTablesDAOObj.getRequestedObjectFields(UnitOfMeasure.class, new String []{"name"}, map);
                stockUOMName = res != null ?(String)res : "";
//                stockUOMName = accProductObj.getNamebyId(batch.getStockUOMID(), "uom");
            }
            jObj.put("costcenter", costCenterName);
            jObj.put("orderuom", stockUOMName);
            String vcName = StringUtil.isNullOrEmpty(batch.getPersonName()) ? "" : batch.getPersonName();
            jObj.put("vendor", vcName);
            jObj.put("date", batch.getTransactionDate() != null ? df.format(batch.getTransactionDate()) : "");
            TransactionType docTransactionType = TransactionType.getTransactionType(batch.getDocType());
            jObj.put("type", docTransactionType.toString());
            double quantity = batch.getQuantity();
            double price = batch.getPrice();
            double amount = quantity * price;
            jObj.put("orderquantity", quantity);
            jObj.put("amount", authHandler.round(amount, companyid));
            jObj.put("avgCost", authHandler.round(price, companyid));
            jObj.put("orderno", batch.getTransactionNo());
            jObj.put("remark", StringUtil.isNullOrEmpty(batch.getRemark()) ? "" : batch.getRemark());
            jObj.put("isBatchForProduct", product.isIsBatchForProduct());
            jObj.put("isSerialForProduct", product.isIsSerialForProduct());
            jObj.put("isRowForProduct", product.isIsrowforproduct());
            jObj.put("isRackForProduct", product.isIsrackforproduct());
            jObj.put("isBinForProduct", product.isIsbinforproduct());
            if (batch.getMaterialInOutlocationMap() != null && !batch.getMaterialInOutlocationMap().isEmpty()) {
                for (Map.Entry<String, Double> entry : batch.getMaterialInOutlocationMap().entrySet()) {
                    JSONObject srObject = locationMapBuilder(entry);
                    smDetails.put(srObject);
                }
            }
            jObj.put("stockDetails", smDetails);
            jObj.put(Constants.productid, product.getID());
            jObj.put("moduleName", PriceValuationStack.getTransactionModule(batch.getDocType()));
            /*
             jObj.put("module", sm.getTransactionModule() != null ? sm.getTransactionModule().ordinal() : "");
             String moduleName = sm.getTransactionModule() != null ? sm.getTransactionModule().getString() : "";
             jObj.put("moduleName", moduleName);
             */

        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return jObj;
    }
    /**
     * Method is used to build the transaction JSON for Material IN/OUT Report.
     *
     * @param Product
     * @param PriceValuationStack.Batch
     * @param Store
     * @param DateFormat
     * @return 
     */
    private JSONObject getTransactionJson(JSONObject productJson, PriceValuationStack.Batch batch, Store iwStore, DateFormat df, String companyid) {
        JSONObject jObj = new JSONObject();
        JSONArray smDetails = new JSONArray();
        try {
            jObj.put("itemcode", productJson.getString("productid"));
            jObj.put("itemdescription", productJson.getString("description"));
            jObj.put("itemname", productJson.getString("name"));
            jObj.put("store", (iwStore == null) ? "" : iwStore.getDescription());
            String assemblyProductName = "";
            Map<String, Object> map = new HashMap<>();
            Object res = null;
            if (!StringUtil.isNullOrEmpty(batch.getAssemblyProductID())) {
//                assemblyProductName = (String)res.getEntityList().get(0);
                map.put("id", batch.getAssemblyProductID());
                res = kwlCommonTablesDAOObj.getRequestedObjectFields(Product.class, new String []{"name"}, map);
                assemblyProductName = res != null ?(String)res : "";
            }
            jObj.put("assemble", assemblyProductName);
            String costCenterName = "";
            if (!StringUtil.isNullOrEmpty(batch.getCostCenterID())) {
                map.put("id", batch.getCostCenterID());
                res = kwlCommonTablesDAOObj.getRequestedObjectFields(CostCenter.class, new String []{"name"}, map);
                costCenterName = res != null ?(String)res : "";
//                costCenterName = accProductObj.getNamebyId(batch.getCostCenterID(), "costcenter");
            }
            String stockUOMName = "";
            if (!StringUtil.isNullOrEmpty(batch.getStockUOMID())) {
                map.put("id", batch.getStockUOMID());
                res = kwlCommonTablesDAOObj.getRequestedObjectFields(UnitOfMeasure.class, new String []{"name"}, map);
                stockUOMName = res != null ?(String)res : "";
//                stockUOMName = accProductObj.getNamebyId(batch.getStockUOMID(), "uom");
            }
            jObj.put("costcenter", costCenterName);
            jObj.put("orderuom", stockUOMName);
            String vcName = StringUtil.isNullOrEmpty(batch.getPersonName()) ? "" : batch.getPersonName();
            jObj.put("vendor", vcName);
            jObj.put("date", batch.getTransactionDate() != null ? df.format(batch.getTransactionDate()) : "");
            TransactionType docTransactionType = TransactionType.getTransactionType(batch.getDocType());
            jObj.put("type", docTransactionType.toString());
            double quantity = batch.getQuantity();
            double price = batch.getPrice();
            double amount = quantity * price;
            jObj.put("orderquantity", quantity);
            jObj.put("amount", authHandler.round(amount, companyid));
            jObj.put("avgCost", authHandler.round(price, companyid));
            jObj.put("orderno", batch.getTransactionNo());
            jObj.put("remark", StringUtil.isNullOrEmpty(batch.getRemark()) ? "" : batch.getRemark());
            jObj.put("isBatchForProduct", productJson.getBoolean("isBatchForProduct"));
            jObj.put("isSerialForProduct", productJson.getBoolean("isSerialForProduct"));
            jObj.put("isRowForProduct", productJson.getBoolean("isrowforproduct"));
            jObj.put("isRackForProduct", productJson.getBoolean("israckforproduct"));
            jObj.put("isBinForProduct", productJson.getBoolean("isbinforproduct"));
            if (batch.getMaterialInOutlocationMap() != null && !batch.getMaterialInOutlocationMap().isEmpty()) {
                for (Map.Entry<String, Double> entry : batch.getMaterialInOutlocationMap().entrySet()) {
                    JSONObject srObject = locationMapBuilder(entry);
                    smDetails.put(srObject);
                }
            }
            jObj.put("stockDetails", smDetails);
            jObj.put(Constants.productid, productJson.getString("id"));
            jObj.put("moduleName", PriceValuationStack.getTransactionModule(batch.getDocType()));
            /*
             jObj.put("module", sm.getTransactionModule() != null ? sm.getTransactionModule().ordinal() : "");
             String moduleName = sm.getTransactionModule() != null ? sm.getTransactionModule().getString() : "";
             jObj.put("moduleName", moduleName);
             */
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return jObj;
    }
    
    private JSONObject getTransactionJSON(JSONObject product, PriceValuationStack.Batch batch, Store iwStore, DateFormat df, String companyid) {
        JSONObject jObj = new JSONObject();
        JSONArray smDetails = new JSONArray();
        try {
            jObj.put("itemcode", product.getString("id"));
            jObj.put("itemdescription", product.optString("description"));
            jObj.put("itemname", product.getString("name"));
            jObj.put("store", (iwStore == null) ? "" : iwStore.getDescription());
            String assemblyProductName = "";
            Map<String, Object> map = new HashMap<>();
            Object res = null;
            if (!StringUtil.isNullOrEmpty(batch.getAssemblyProductID())) {
//                assemblyProductName = (String)res.getEntityList().get(0);
                map.put("id", batch.getAssemblyProductID());
                res = kwlCommonTablesDAOObj.getRequestedObjectFields(Product.class, new String []{"name"}, map);
                assemblyProductName = res != null ?(String)res : "";
            }
            jObj.put("assemble", assemblyProductName);
            String costCenterName = "";
            if (!StringUtil.isNullOrEmpty(batch.getCostCenterID())) {
                map.put("id", batch.getCostCenterID());
                res = kwlCommonTablesDAOObj.getRequestedObjectFields(CostCenter.class, new String []{"name"}, map);
                costCenterName = res != null ?(String)res : "";
//                costCenterName = accProductObj.getNamebyId(batch.getCostCenterID(), "costcenter");
            }
            String stockUOMName = "";
            if (!StringUtil.isNullOrEmpty(batch.getStockUOMID())) {
                map.put("id", batch.getStockUOMID());
                res = kwlCommonTablesDAOObj.getRequestedObjectFields(UnitOfMeasure.class, new String []{"name"}, map);
                stockUOMName = res != null ?(String)res : "";
//                stockUOMName = accProductObj.getNamebyId(batch.getStockUOMID(), "uom");
            }
            jObj.put("costcenter", costCenterName);
            jObj.put("orderuom", stockUOMName);
            String vcName = StringUtil.isNullOrEmpty(batch.getPersonName()) ? "" : batch.getPersonName();
            jObj.put("vendor", vcName);
            jObj.put("date", batch.getTransactionDate() != null ? df.format(batch.getTransactionDate()) : "");
            TransactionType docTransactionType = TransactionType.getTransactionType(batch.getDocType());
            jObj.put("type", docTransactionType.toString());
            double quantity = batch.getQuantity();
            double price = batch.getPrice();
            double amount = quantity * price;
            jObj.put("orderquantity", quantity);
            jObj.put("amount", authHandler.round(amount, companyid));
            jObj.put("avgCost", authHandler.round(price, companyid));
            jObj.put("orderno", batch.getTransactionNo());
            jObj.put("remark", StringUtil.isNullOrEmpty(batch.getRemark()) ? "" : batch.getRemark());
            jObj.put("isBatchForProduct", product.getBoolean("isBatchForProduct"));
            jObj.put("isSerialForProduct", product.getBoolean("isSerialForProduct"));
            jObj.put("isRowForProduct", product.getBoolean("isrowforproduct"));
            jObj.put("isRackForProduct", product.getBoolean("israckforproduct"));
            jObj.put("isBinForProduct", product.getBoolean("isbinforproduct"));
            if (batch.getMaterialInOutlocationMap() != null && !batch.getMaterialInOutlocationMap().isEmpty()) {
                for (Map.Entry<String, Double> entry : batch.getMaterialInOutlocationMap().entrySet()) {
                    JSONObject srObject = locationMapBuilder(entry);
                    smDetails.put(srObject);
                }
            }
            jObj.put("stockDetails", smDetails);
            jObj.put(Constants.productid, product.getString("id"));
            jObj.put("moduleName", PriceValuationStack.getTransactionModule(batch.getDocType()));
            /*
             jObj.put("module", sm.getTransactionModule() != null ? sm.getTransactionModule().ordinal() : "");
             String moduleName = sm.getTransactionModule() != null ? sm.getTransactionModule().getString() : "";
             jObj.put("moduleName", moduleName);
             */

        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return jObj;
    }
    /**
     * Get Build Assembly details JSON for print
     * @param paramJobj
     * @param companyid
     * @param transactionId
     * @return 
     */
    @Override
    public JSONArray getBuildAssemblyDetailsItemJSON(JSONObject paramJobj, String companyid, String transactionId) {
        JSONArray jArr = new JSONArray();
        try {
            //Date Formatters for converting date in particular format
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj);//User Date Formatter
            KwlReturnObject result = accProductObj.getBuildAssemblyProdcutDetails(transactionId);
            for (int ind = 0; ind < result.getEntityList().size(); ind++) {
                ProductBuildDetails prodBuildDetailsObj = (ProductBuildDetails) result.getEntityList().get(ind);
                if (prodBuildDetailsObj != null) {
                    JSONObject obj = new JSONObject();
                    obj.put(CustomDesignerConstants.SrNo, ind + 1);
                    obj.put(CustomDesignerConstants.IN_ProductCode, (prodBuildDetailsObj.getAproduct() != null) ? prodBuildDetailsObj.getAproduct().getProductid() : "");
                    obj.put(CustomDesignerConstants.ProductName, (prodBuildDetailsObj.getAproduct() != null) ? prodBuildDetailsObj.getAproduct().getName() : "");
                    obj.put(CustomDesignerConstants.PRODUCT_TYPE, (prodBuildDetailsObj.getAproduct().getProducttype() != null) ? prodBuildDetailsObj.getAproduct().getProducttype().getName() : "");
                    obj.put(CustomDesignerConstants.QUANTITY_NEEDED, prodBuildDetailsObj.getAquantity());
                    obj.put(CustomDesignerConstants.CustomDesignActualQuantity_fieldTypeId, prodBuildDetailsObj.getActualQuantity());
                    obj.put(CustomDesignerConstants.INVENTORY_QUANTITY, prodBuildDetailsObj.getInventoryQuantity());
                    obj.put(CustomDesignerConstants.RECYCLE_QUANTITY, prodBuildDetailsObj.getRecycleQuantity());
                    obj.put(CustomDesignerConstants.REMAINING_QUANTITY, prodBuildDetailsObj.getRemainingQuantity());
                    obj.put(CustomDesignerConstants.WASTAGE_QUANTITY, prodBuildDetailsObj.getWastageQuantity());
                    obj.put(CustomDesignerConstants.Rate, prodBuildDetailsObj.getRate());
                    obj.put(CustomDesignerConstants.UOM, (prodBuildDetailsObj.getAproduct().getUnitOfMeasure() != null) ? prodBuildDetailsObj.getAproduct().getUnitOfMeasure().getNameEmptyforNA() : "");
                    //get batch, warehouse, location related details
                    String batchdetails = accInvoiceServiceDAO.getNewBatchJson(prodBuildDetailsObj.getAproduct(), paramJobj, prodBuildDetailsObj.getID());
                    JSONArray detailsJSONArr = new JSONArray(batchdetails);

                    String batchNameStr = "", batchExpDateStr = "", locationStr = "", warehouseStr = "",serialNumberStr="";
                    LinkedList<String> batchnames = new LinkedList();
                    LinkedList<String> batchesexpirydate = new LinkedList();
                    LinkedList<String> locations = new LinkedList();
                    LinkedList<String> warehouses = new LinkedList();
                    Set<String> serialNumbers = new LinkedHashSet(); //ERP-38919
                    /**
                     * Iterate over inventory details. 
                     * Get batch,Serial location, warehouse related details
                     */
                    for (int i = 0; i < detailsJSONArr.length(); i++) {
                        JSONObject jSONObject = new JSONObject(detailsJSONArr.get(i).toString());
                        String batchname = jSONObject.optString("batchname", "");
                        String batchexpdate = jSONObject.optString("expdate", "");
                        String location = jSONObject.optString("location", "");
                        String warehouse = jSONObject.optString("warehouse", "");
                        String serialnumber = jSONObject.optString("serialno", "");
                        //add batch name
                        if (!StringUtil.isNullOrEmpty(batchname)) {
                            if (!batchnames.contains(batchname)) {
                                batchnames.add(batchname);
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(serialnumber)) {
                            serialNumbers.add(serialnumber);
                        }
                        //add expiry date
                        Date date = null;
                        if (!StringUtil.isNullOrEmpty(batchexpdate)) {
                            date = df.parse(batchexpdate);
                            batchexpdate = userdf.format(date);
                            if (!batchesexpirydate.contains(batchexpdate)) {
                                batchesexpirydate.add(batchexpdate);
                            }
                        }
                        //add location
                        if (!StringUtil.isNullOrEmpty(location)) {
                            KwlReturnObject loc = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), location);
                            InventoryLocation localist = (InventoryLocation) loc.getEntityList().get(0);
                            location = localist.getName();
                            if (!locations.contains(location)) {
                                locations.add(location);
                            }
                        }
                        //add warehouse
                        if (!StringUtil.isNullOrEmpty(warehouse)) {
                            KwlReturnObject ware = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), warehouse);
                            InventoryWarehouse warehouseList = (InventoryWarehouse) ware.getEntityList().get(0);
                            warehouse = warehouseList.getName();
                            if (!warehouses.contains(warehouse)) {
                                warehouses.add(warehouse);
                            }
                        }
                    }
                    /**
                     * create comma separated single string of 
                     * particular details to put in object
                     */
                    for (String bno : batchnames) {
                        batchNameStr += bno.concat("!##");
                    }
                    for (String sno : serialNumbers) {
                        if (!StringUtil.isNullOrEmpty(sno) && !sno.equals(" ")) {
                            serialNumberStr += sno.concat("!##");
                        }
                    }
                    for (String bexp : batchesexpirydate) {
                        if (!StringUtil.isNullOrEmpty(bexp) && !bexp.equals(" ")) {
                            batchExpDateStr += bexp.concat("!##");
                        }
                    }
                    for (String loc : locations) {
                        if (!StringUtil.isNullOrEmpty(loc) && !loc.equals(" ")) {
                            locationStr += loc.concat("!##");
                        }
                    }
                    for (String ware : warehouses) {
                        if (!StringUtil.isNullOrEmpty(ware) && !ware.equals(" ")) {
                            warehouseStr += ware.concat("!##");
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(batchNameStr)) {
                        batchNameStr = batchNameStr.substring(0, batchNameStr.length() - 3);
                    }
                    if (!StringUtil.isNullOrEmpty(serialNumberStr)) {
                        serialNumberStr = serialNumberStr.substring(0, serialNumberStr.length() - 3);
                    }
                    if (!StringUtil.isNullOrEmpty(batchExpDateStr)) {
                        batchExpDateStr = batchExpDateStr.substring(0, batchExpDateStr.length() - 3);
                    }
                    if (!StringUtil.isNullOrEmpty(locationStr)) {
                        locationStr = locationStr.substring(0, locationStr.length() - 3);
                    }
                    if (!StringUtil.isNullOrEmpty(warehouseStr)) {
                        warehouseStr = warehouseStr.substring(0, warehouseStr.length() - 3);
                    }
                    //put all details in object
                    obj.put(CustomDesignerConstants.BatchNumber, batchNameStr);
                    obj.put(CustomDesignerConstants.SerialNumber, serialNumberStr);
                    obj.put(CustomDesignerConstants.BatchNumberExp, batchExpDateStr);
                    obj.put(CustomDesignerConstants.Location, locationStr);
                    obj.put(CustomDesignerConstants.Warehouse, warehouseStr);
                    jArr.put(obj);
                }
            }
            //fetch global level details
            KwlReturnObject productBuildObj = accountingHandlerDAOobj.getObject(ProductBuild.class.getName(), transactionId);
            ProductBuild productBuild = (ProductBuild) productBuildObj.getEntityList().get(0);
            //get assembled product batch details
            String batchdetails = accInvoiceServiceDAO.getNewBatchJson(productBuild.getProduct(), paramJobj, transactionId);
            JSONArray detailsJSONArr = new JSONArray(batchdetails);
            /**
             * Fetch batch number Serial Number,and expiry date Details.
             * Batch Number is same for all quantity.
             * Serial Number is different for all quantity so need to iterate over details
             */
            
            String batchNameStr = "", batchExpDateStr = "",serialNumberStr="";
            Set<String> serialnumbers = new LinkedHashSet();
            if(detailsJSONArr.length() > 0){
                for (int i = 0; i < detailsJSONArr.length(); i++) {
                    JSONObject batchDetialObj = detailsJSONArr.optJSONObject(i);
                    batchNameStr = batchDetialObj.optString("batchname", "");
                    String serialNumber = batchDetialObj.optString("serialno", "");
                    String batchexpdate = batchDetialObj.optString("expdate", "");
                    Date date = null;
                    if(!StringUtil.isNullOrEmpty(batchexpdate)){
                        date = df.parse(batchexpdate);
                        batchExpDateStr = userdf.format(date);
                    }
                    if (!StringUtil.isNullOrEmpty(serialNumber)) {
                        serialnumbers.add(serialNumber);
                    }
                }
            }
            for (String sno : serialnumbers) {
                if (!StringUtil.isNullOrEmpty(sno) && !sno.equals(" ")) {
                    serialNumberStr += sno.concat("!##");
                }
            }
            if (!StringUtil.isNullOrEmpty(serialNumberStr)) {
                serialNumberStr = serialNumberStr.substring(0, serialNumberStr.length() - 3);
            }
            //get created by full name
            String createdBy = "";
            createdBy = (productBuild.getJournalentry() != null ? (productBuild.getJournalentry().getCreatedby() != null ? productBuild.getJournalentry().getCreatedby().getFullName() : "") : "");
            JSONObject summarydata = new JSONObject();
            summarydata.put("summarydata", true);
            summarydata.put(CustomDesignerConstants.BatchNumber, batchNameStr);
            summarydata.put(CustomDesignerConstants.SerialNumber, serialNumberStr);
            summarydata.put(CustomDesignerConstants.BatchNumberExp, batchExpDateStr);
            summarydata.put(CustomDesignerConstants.Createdby, createdBy);
            jArr.put(summarydata);

        } catch (Exception e) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.WARNING, e.getMessage());
        }
        return jArr;
    }
    @Override
    public JSONObject checkAssetIdIsPresent(Map<String, Object> requestParams,NewBatchSerial batchserialOBj) throws ServiceException {
        JSONObject jobj= new JSONObject();
        boolean isDuplicate=false;
        try{
            KwlReturnObject kwlresult=accProductObj.checkIsAssetIDAlreadyUsed(requestParams,batchserialOBj);
            
            if(kwlresult.getRecordTotalCount() > 0){
                isDuplicate=true;
            }
            jobj.put("isDuplicate", isDuplicate);
        
        }catch(Exception ex){
             throw ServiceException.FAILURE("AccProductServiceImpl.saveQAApprovalDetails", ex);
        }
        return jobj;
    }
    /**
     * 
     * @param paramsjobj
     * @return jobj(JSONbject)
     * @throws ServiceException 
     * Description : Method to get multiple discount mapped with specific product and return.
     */
    @Override
    public JSONObject getIndividualProductDiscount(JSONObject paramsjobj) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray discountMasterJarr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            KwlReturnObject resultOfDiscount = accMasterItemsDAOobj.getDiscountOfProductForPricingBand(paramsjobj);
            List<Object[]> listOfDiscount = resultOfDiscount.getEntityList();
            Iterator listOfDiscountIterator = listOfDiscount.iterator();
            while (listOfDiscountIterator.hasNext()) {
                JSONObject jsonobj = new JSONObject();
                String ids=(String)listOfDiscountIterator.next();
                KwlReturnObject discountMasterReturnObj=accountingHandlerDAOobj.getObject(DiscountMaster.class.getName(), ids);
                DiscountMaster discountMaster=(DiscountMaster)discountMasterReturnObj.getEntityList().get(0);                                    
                jsonobj.put("discountid", discountMaster.getId());
                jsonobj.put("discountname", discountMaster.getName());
                jsonobj.put("discountdescription", discountMaster.getDescription());
                jsonobj.put("discountvalue", discountMaster.getValue());
                jsonobj.put("discountaccount", discountMaster.getAccount());
                jsonobj.put("discounttype", discountMaster.isDiscounttype() ? Constants.DISCOUNT_MASTER_TYPE_PERCENTAGE : Constants.DISCOUNT_MASTER_TYPE_FLAT);
                discountMasterJarr.put(jsonobj);                                
            }
            jobj.put("data", discountMasterJarr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    public boolean migrationPriceList(HashMap<String, Object> requestParams) {
        boolean success = true;
        try {
            String subdomain = (String) requestParams.get("subdomain");
            String companyid = (String) requestParams.get("companyid");
            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            String startDateString = (String) requestParams.get(Constants.REQ_startdate);
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            Date startDate = null;
            if (!StringUtil.isNullOrEmpty(startDateString) && df != null) {
                startDate = df.parse(startDateString);
            }
            requestParams.remove(Constants.REQ_startdate);
            
            String endDateString = (String) requestParams.get(Constants.REQ_enddate);
            Date endDate = null;
            if (!StringUtil.isNullOrEmpty(endDateString) && df != null) {
                endDate = df.parse(endDateString);
            }
            Map<String, List> proMap =  getProductTransaction(requestParams);
            if (!proMap.isEmpty()) {
                for (Map.Entry<String, List> entry : proMap.entrySet()) {
                    String productid = entry.getKey();
                    List list = entry.getValue();
                    Product product = (Product) kwlCommonTablesDAOObj.getClassObject(Product.class.getName(), productid);
                    if (list != null && !list.isEmpty()) {
                        PriceValuationStack stack = new PriceValuationStack();
                        pushTransactionForProduct(list, requestParams, startDate, product, productid, stack, endDate, null);
                        PriceValuationStack.Batch batch = stack.getTransactionBatch();
                        if (batch != null && batch.getQuantity() != 0.0) {
                            JSONObject json = new JSONObject();
                            json.put("subdomain", subdomain);
                            json.put("productid", product.getProductid());
                            json.put("currencyid", currencyid);
                            json.put("price", authHandler.roundUnitPrice(batch.getPrice(),companyid));
                            accProductObj.saveCompanyProductPriceList(json);
                        }
                        stack = null;
                    }
                }
            }
            proMap = null;
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
    }
    
     /* Function is used to get "Balance Quantity" of product available in inventory
    
     Return "Balance Quantity" which value is same as "Balance Quantity"  column of Product Master
    
     */
    public List getListOfInventoryQuantitiesOfProduct(HashMap balanceQuantityParams) throws ServiceException {
        double balanceQuantity = 0;
        List inventoryQuantityList = new ArrayList();
        try {

            Product product = (Product) balanceQuantityParams.get("productObj");
            Company compObj = (Company) balanceQuantityParams.get("companyObj");
            String companyid = (String) balanceQuantityParams.get("companyid");
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) balanceQuantityParams.get("extraCompanyPreferences");
            UnitOfMeasure uom = product.getUnitOfMeasure();
            double availbleQuantity = 0;

            Map<String, Double> quantityUnderQAForGRNAndDO = new HashMap<>();
            if (extraCompanyPreferences != null && extraCompanyPreferences.isPickpackship()) {
                /**
                 * If Pick, Pack & Ship flow is activated then we don't add
                 * entry in inventory table. Get quantity present in QC store
                 * for Delivery Order.
                 */
            
                quantityUnderQAForGRNAndDO = accProductObj.getProductQuantityUnderQAForGRNAndDO(compObj, null, false);
            } else {
              
                /**
                 * If Pick, Pack & Ship flow is deactivated then we add entry in
                 * inventory table So, no need to get quantity present in QC
                 * store for delivery order separately.
                 */
                quantityUnderQAForGRNAndDO = accProductObj.getProductQuantityUnderQAForGRNAndDO(compObj, null, true);
            }

            double qaQuantityForGRNAndDO = 0, repairQuantityForGRNAndDO = 0, doApprovedOrRejectedOrPickedQty = 0;
            if (quantityUnderQAForGRNAndDO.containsKey(product.getID()) && quantityUnderQAForGRNAndDO.get(product.getID()) != null) {
                qaQuantityForGRNAndDO = quantityUnderQAForGRNAndDO.get(product.getID());
            }

            Map<String, Double> quantityUnderRepairForGRNAndDO = new HashMap<>();
            if (extraCompanyPreferences != null && extraCompanyPreferences.isPickpackship()) {
                /**
                 * If Pick, Pack & Ship flow is activated then we don't add
                 * entry in inventory table. Get quantity present in QC store
                 * for Delivery Order.
                 */
                quantityUnderRepairForGRNAndDO = accProductObj.getProductQuantityUnderRepairForGRNAndDO(compObj, null, false);
            } else {
                /**
                 * If Pick, Pack & Ship flow is deactivated then we add entry in
                 * inventory table So, no need to get quantity present in QC
                 * store for delivery order separately.
                 */
                quantityUnderRepairForGRNAndDO = accProductObj.getProductQuantityUnderRepairForGRNAndDO(compObj, null, true);
            }

            if (quantityUnderRepairForGRNAndDO.containsKey(product.getID()) && quantityUnderRepairForGRNAndDO.get(product.getID()) != null) {
                repairQuantityForGRNAndDO = quantityUnderRepairForGRNAndDO.get(product.getID());
            }

            Map<String, Double> doApprovedOrRejectedOrPickedQtyMap = new HashMap<>();
            if (!extraCompanyPreferences.isPickpackship()) {
                doApprovedOrRejectedOrPickedQtyMap = accProductObj.getProductApprovedOrRejectedOrPickedQty(compObj, null);
            }

            if (doApprovedOrRejectedOrPickedQtyMap.containsKey(product.getID()) && doApprovedOrRejectedOrPickedQtyMap.get(product.getID()) != null) {
                doApprovedOrRejectedOrPickedQty = doApprovedOrRejectedOrPickedQtyMap.get(product.getID());
            }

            if (product.isblockLooseSell()) {
                KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(product.getID(), uom != null ? uom.getID() : "");
                availbleQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                availbleQuantity = (availbleQuantity - (qaQuantityForGRNAndDO + repairQuantityForGRNAndDO) + doApprovedOrRejectedOrPickedQty);

            } else {

                boolean isProductQuantityFromColumn = false;

                isProductQuantityFromColumn = accProductObj.isProductQuantityFromColumn(companyid);

                if (isProductQuantityFromColumn) {
                    availbleQuantity = product.getAvailableQuantity(); // availableQuantity
                } else {
                    KwlReturnObject result = accProductObj.getQuantity(product.getID());
                    availbleQuantity = (Double) (result.getEntityList().get(0)); // availableQuantity
                }
                availbleQuantity = (availbleQuantity - (qaQuantityForGRNAndDO + repairQuantityForGRNAndDO) + doApprovedOrRejectedOrPickedQty);

            }

            Map<String, Double> quantityUnderQA = accProductObj.getProductQuantityUnderQA(compObj, null);
            Map<String, Double> quantityUnderRepair = accProductObj.getProductQuantityUnderRepair(compObj, null);

            double qaQuantity = 0, repairQuantity = 0;
            if (quantityUnderQA.containsKey(product.getID()) && quantityUnderQA.get(product.getID()) != null) {
                qaQuantity = quantityUnderQA.get(product.getID());
            }
            if (quantityUnderRepair.containsKey(product.getID()) && quantityUnderRepair.get(product.getID()) != null) {
                repairQuantity = quantityUnderRepair.get(product.getID());
            }

            String ss = "";

            Map<String, Double> blkQuantityList = accProductObj.getProductBlockedQuantity(compObj, null, null, ss);

            Map<String, Double> blockedQuantityList = accProductObj.getProductBlockedQuantity(compObj, null, null, ss);

            double lockQuantityInSelectedUOM = 0;
            if (product.isblockLooseSell() && blkQuantityList.containsKey(product.getID())) {

                lockQuantityInSelectedUOM = blkQuantityList.get(product.getID());
            } else {

                KwlReturnObject result2 = accProductObj.getAssemblyLockQuantity(product.getID());
                Double assmblyLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));

                KwlReturnObject result1 = accProductObj.getLockQuantity(product.getID());
                Double SoLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));

                KwlReturnObject woresult = accProductObj.getWOLockQuantity(product.getID());
                Double WOLockQuantity = (Double) (woresult.getEntityList().get(0) == null ? 0.0 : woresult.getEntityList().get(0));

                lockQuantityInSelectedUOM = assmblyLockQuantity + SoLockQuantity + WOLockQuantity; // lockquantity

            }
            if (blockedQuantityList.containsKey(product.getID())) {

                lockQuantityInSelectedUOM = blockedQuantityList.get(product.getID());
            }

            balanceQuantity = availbleQuantity - (lockQuantityInSelectedUOM) + qaQuantity + repairQuantity;

            inventoryQuantityList.add(balanceQuantity);

        } catch (Exception exception) {
            throw ServiceException.FAILURE("AccSalesOrderServiceImpl.getListOfInventoryQuantitiesOfProduct :" + exception.getMessage(), exception);
        }

        return inventoryQuantityList;
    }
    
   @Override 
   public JSONObject getProductsIdNameforCombo(JSONObject paramJObj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = productHandler.getProductRequestMapfromJson(paramJObj);
            List<Object[]> selectedProductsList = new ArrayList();
            String selectedProductsIds = paramJObj.optString("combovalue",null);
           
            KwlReturnObject companyPrefObj = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            if (companyPrefObj != null && companyPrefObj.getEntityList() != null && companyPrefObj.getEntityList().size() > 0 && companyPrefObj.getEntityList().get(0) != null) {
                CompanyAccountPreferences prefObj = (CompanyAccountPreferences) companyPrefObj.getEntityList().get(0);
                requestParams.put("productsortingflag", prefObj.getProductSortingFlag());
            }
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", paramJObj.optString(Constants.companyKey));
            requestParams.put(Constants.PRODUCT_SEARCH_FLAG, extraPref != null ? extraPref.getProductSearchingFlag() : Constants.PRODUCT_SEARCH_ANYWHERE);
            JSONArray jArray = new JSONArray();
            
            /**
             * Block used to get selected products using their ids from customer/vendor preferred product selection
             */
            if (!StringUtil.isNullOrEmpty(selectedProductsIds) && !selectedProductsIds.equals("All")) {
                requestParams.put("selectedProductsIds", selectedProductsIds);
                requestParams.put("isMultiSelectProductsids", true);
                KwlReturnObject selectedProducts =  accProductObj.getProductsForComboSQLQuery(requestParams);
                requestParams.remove("isMultiSelectProductsids");
                selectedProductsList = selectedProducts.getEntityList();
            }
            
            /**
             * This Function will use when Users Visibility Feature is Enable
             * Append user condition while querying data
             */
            if (extraPref != null && extraPref.isUsersVisibilityFlow()) {
                KwlReturnObject object = accountingHandlerDAOobj.getObject(User.class.getName(), paramJObj.optString("userid"));
                User user = object.getEntityList().size() > 0 ? (User) object.getEntityList().get(0) : null;
                if (!AccountingManager.isCompanyAdmin(user)) {
                    /**
                     * if Users visibility enable and current user is not admin
                     */
                    Map<String, Object> reqMap = new HashMap();
                    requestParams.put("isUserVisibilityFlow", true);
                    reqMap.put("companyid", paramJObj.optString("companyid"));
                    reqMap.put("userid", paramJObj.optString("userid"));
                    reqMap.put("jointable", "pcd");
                    reqMap.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                    String custcondition = fieldManagerDAOobj.appendUsersCondition(reqMap);
                    if (!StringUtil.isNullOrEmpty(custcondition)) {
                        /**
                         * If mapping found with dimension
                         */
                        String usercondition = " and (" + custcondition + ")";
                        requestParams.put("appendusercondtion", usercondition);
                    }
                }
            }
            
            KwlReturnObject result = accProductObj.getProductsForComboSQLQuery(requestParams);
            List<Object[]> prolist = result.getEntityList();
            selectedProductsList.addAll(prolist);
            for (Object[] row : selectedProductsList) {
                JSONObject obj = new JSONObject();
                if (row != null) {
                    if (row[0] != null) {
                        obj.put(Constants.productid, row[0].toString());
                        String prodid=row[0].toString();
                        
                        //For POS :ERP-40023
                        if (paramJObj.optBoolean(Constants.isForPos)) {
                            obj=buildPriceListBandJSON(paramJObj,prodid,obj); 
                        }
                    }
                    
                    if (row[1] != null) {
                        obj.put("productname", row[1].toString());
                    }
                    if (row[2] != null) {
                        obj.put("pid", row[2].toString());
                        
                    }
                    if (row[3] != null) {
                        obj.put("type", row[3].toString());
                    }
                    if (row[4] != null) {
                        String hasaccess = row[4].toString();
                        if (StringUtil.equalIgnoreCase(hasaccess, "T")) {
                            obj.put("hasAccess", true);
                        } else {
                            obj.put("hasAccess", false);
                        }

                    }
                }
                jArray.put(obj);
            }
            jobj.put(Constants.data, jArray);
            jobj.put(Constants.RES_TOTALCOUNT, jArray.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, "");
            } catch (JSONException ex) {
                Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }   
    
    /*ERP-40023:Build Price List Band JSON for each product*/
    public JSONObject buildPriceListBandJSON(JSONObject paramJObj, String prodid, JSONObject obj) throws JSONException {
        JSONObject returnJobj = new JSONObject(obj.toString());
        String companyid = paramJObj.optString(Constants.companyKey);
         HashMap<String, Object> requestParams = new HashMap<String, Object>();
        try {
                String globalCurrencyid = paramJObj.optString(Constants.globalCurrencyKey);
                String currencyIDStr = paramJObj.optString(Constants.currencyKey, globalCurrencyid);
                KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), prodid);
                Product product = (Product) prodresult.getEntityList().get(0);
                UnitOfMeasure uom = product.getUnitOfMeasure();
                returnJobj.put("uomid", uom == null ? "" : uom.getID());
                returnJobj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
                MasterItem masterItem = null;
                
                KwlReturnObject productresult1 = accProductObj.getProductCategory(prodid);

                if (productresult1 != null && productresult1.getEntityList() != null && !productresult1.getEntityList().isEmpty()) {
                    String productCategoryId = (String) productresult1.getEntityList().get(0);

                    if (!StringUtil.isNullOrEmpty(productCategoryId)) {
                        KwlReturnObject catresult = accProductObj.getObject(MasterItem.class.getName(), productCategoryId);
                        masterItem = (MasterItem) catresult.getEntityList().get(0);
                    }
                    returnJobj.put("categoryid", masterItem == null ? "" : masterItem.getID());
                    returnJobj.put("category", masterItem == null ? "" : masterItem.getValue());
                }
                requestParams.clear();
                requestParams.put(Constants.ss, paramJObj.optString(Constants.ss, null));
                requestParams.put(Constants.productid, prodid);
                requestParams.put(Constants.start, paramJObj.optString(Constants.start, null));
                requestParams.put(Constants.limit, paramJObj.optString(Constants.limit, null));
                requestParams.put("locale", Locale.forLanguageTag(paramJObj.optString("language")));
                requestParams.put(Constants.isForPos, true);
                /*
                 * Business login to fetch data
                 */

                JSONObject bandDetailJSONObject = new JSONObject();
                HashMap<String, Object> bandParams = new HashMap<>();
                bandParams.put(Constants.companyKey, companyid);

                KwlReturnObject bandResult = accMasterItemsDAOobj.getPricingBandItems(bandParams);
                List<PricingBandMaster> bandList = bandResult.getEntityList();
                for (PricingBandMaster pricingBandMaster : bandList) {
                    HashMap<String, Object> bandDetailParams = new HashMap<>();
                    bandDetailParams.put("pricingBandMasterID", pricingBandMaster.getID());
                    bandDetailParams.put("applicableDate", new Date());
                    bandDetailParams.put("currencyID", currencyIDStr);
                    bandDetailParams.put("productID", prodid);
                    bandDetailParams.put("companyID", companyid);
                    KwlReturnObject result  = accMasterItemsDAOobj.getPriceOfBandForProductAndCurrency(bandDetailParams);
                    if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        List<Object[]> list = result.getEntityList();
                        for (Object[] priceObj : list) {
                            JSONObject bandJobj = new JSONObject();
                            KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(PricingBandMasterDetail.class.getName(), (String) priceObj[2]);
                            PricingBandMasterDetail bandDetails = (PricingBandMasterDetail) prefresult.getEntityList().get(0);
                            bandJobj.put("saleprice", bandDetails.getSalesPrice());
                            bandJobj.put("purchaseprice", bandDetails.getPurchasePrice());
                            bandJobj.put(Constants.currencyKey, (bandDetails.getCurrency() != null ? bandDetails.getCurrency().getCurrencyID() : ""));
                            bandJobj.put("isRetail",pricingBandMaster.isDefaultToPOS());
                            //DISCOUNT CODE
                            bandJobj = buildBandDiscountJSON(paramJObj, currencyIDStr, pricingBandMaster, prodid, bandJobj);
                            bandDetailJSONObject.put(pricingBandMaster.getID(), bandJobj);
                        }
                    }
                }
                returnJobj.put("banddetails", bandDetailJSONObject);
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnJobj;
    }    
   
   /*ERP-40023:Build Discount JSON In pricing band details*/
    public JSONObject buildBandDiscountJSON(JSONObject paramJObj,String currencyIDStr,PricingBandMaster pricingBandMaster,String productid,JSONObject bandJobj) throws JSONException {
        JSONObject jobj = new JSONObject(bandJobj.toString());
        String companyid=paramJObj.optString(Constants.companyKey);
        try {
            JSONArray discountMasterJarr = new JSONArray();
            //DISCOUNT CODE
            String[] currencyIDArr = currencyIDStr.split(",");
            for (int i = 0; i < currencyIDArr.length; i++) {

                JSONObject requestParamsJson = new JSONObject();
                requestParamsJson.put("productId", productid);
                requestParamsJson.put("pricingBandMasterId", pricingBandMaster.getID());
                requestParamsJson.put("companyid", companyid);
                requestParamsJson.put("applicableDate", new Date());
                requestParamsJson.put("currencyId", currencyIDArr[i]);
                KwlReturnObject resultOfDiscount = accMasterItemsDAOobj.getDiscountOfProductForPricingBand(requestParamsJson);
                List<Object[]> listOfDiscount = resultOfDiscount.getEntityList();

                Iterator listOfDiscountIterator = listOfDiscount.iterator();
                while (listOfDiscountIterator.hasNext()) {
                    JSONObject jsonobj = new JSONObject();
                    Object obj = listOfDiscountIterator.next();
                    String ids = obj!=null?(String) obj:"";
                    if (!StringUtil.isNullOrEmpty(ids)) {
                        KwlReturnObject discountMasterReturnObj = accountingHandlerDAOobj.getObject(DiscountMaster.class.getName(), ids);
                        DiscountMaster discountMaster = (DiscountMaster) discountMasterReturnObj.getEntityList().get(0);
                        jsonobj.put("discountid", discountMaster.getId());
                        jsonobj.put("discountname", discountMaster.getName());
                        jsonobj.put("discountdescription", discountMaster.getDescription());
                        jsonobj.put("discountvalue", discountMaster.getValue());
                        jsonobj.put("discountaccount", discountMaster.getAccount());
                        jsonobj.put("discounttype", discountMaster.isDiscounttype() ? Constants.DISCOUNT_MASTER_TYPE_PERCENTAGE : Constants.DISCOUNT_MASTER_TYPE_FLAT);
                        discountMasterJarr.put(jsonobj);
                    }
                }//end of listOfDiscountIterator.hasNext()
            }//end of for (int i = 0; i < currencyIDArr.length; i++) {
            jobj.put("discountarray", discountMasterJarr);

        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(AccProductServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return jobj;
    }   
    
}
