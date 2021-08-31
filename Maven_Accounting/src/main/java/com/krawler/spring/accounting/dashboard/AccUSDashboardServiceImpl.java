/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.accounting.dashboard;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.product.service.AccProductService;
import com.krawler.spring.accounting.purchaseorder.AccPurchaseOrderServiceDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccUSDashboardServiceImpl implements AccUSDashboardService{
    private AccUSDashboardDAO accUSDashboardServiceDao;
    private StockService stockService;
    private StoreService storeService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private AccPurchaseOrderServiceDAO accPurchaseOrderServiceDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private AccProductService AccProductService;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    
    public void setAccUSDashboardServiceDao(AccUSDashboardDAO accUSDashboardServiceDao) {
        this.accUSDashboardServiceDao = accUSDashboardServiceDao;
    }
    
    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }
    
    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }
    
    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }
    
    public void setaccPurchaseOrderobj(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }
    public void setaccPurchaseOrderServiceDAOobj(AccPurchaseOrderServiceDAO accPurchaseOrderServiceDAOobj) {
        this.accPurchaseOrderServiceDAOobj = accPurchaseOrderServiceDAOobj;
    }
    
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setAccProductService(AccProductService AccProductService) {
        this.AccProductService = AccProductService;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    
    @Override
    public JSONObject saveDashboard(JSONObject paramJobj) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject resultObj = null;
        try {

            long createdon = System.currentTimeMillis();
            long updatedon = createdon;
            paramJobj.put("createdon", createdon);
            paramJobj.put("updatedon", updatedon);

            resultObj = accUSDashboardServiceDao.saveDashboard(paramJobj);
            List<Dashboard> list = resultObj.getEntityList();

            if (resultObj.getEntityList().size() > 0) {
                jobj.put("success", true);
                jobj.put("valid", true);
                jobj.put("msg", "Dashboard saved successfully.");
            } else {
                jobj.put("success", false);
            }

        } catch (JSONException ex) {
            Logger.getLogger(AccUSDashboardServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    @Override
    public JSONObject getDashboard(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        JSONObject dataObj = null;

        KwlReturnObject resultObj = null;
            resultObj = accUSDashboardServiceDao.getDashboard(paramJobj);
            List<Dashboard> list = resultObj.getEntityList();

            for (Dashboard dashboard : list) {
                dataObj = new JSONObject();
                dataObj.put("id", dashboard.getID());
                dataObj.put("name", dashboard.getName());
                dataObj.put("description", dashboard.getDescription());

                dataObj.put("json", dashboard.getJson());
                dataObj.put("isactive", dashboard.isActive());
                dataObj.put("deleted", dashboard.isDeleted());
                dataObj.put("createdby", dashboard.getCreatedby().getFullName());
                dataObj.put("createdon", dashboard.getCreatedon());
                dataObj.put("updatedon", dashboard.getUpdatedon());
                dataArr.put(dataObj);
            }

        jobj.put(Constants.RES_success, true);
        jobj.put("valid", true);
        jobj.put(Constants.RES_data, dataArr);
        jobj.put("count", resultObj.getRecordTotalCount());
        return jobj;
    }
    
    @Override
    public JSONObject setActiveDashboard(JSONObject paramJobj) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONArray dataArray = new JSONArray();
        KwlReturnObject resultObj = null;
        try {


            resultObj = accUSDashboardServiceDao.setActiveDashboard(paramJobj);
            List<Dashboard> list = resultObj.getEntityList();
            
            for(Dashboard dashboard : list){
                JSONObject dataobj = new JSONObject();
                dataobj.put("id",dashboard.getID());
                dataobj.put("company",dashboard.getCompany());
                dataobj.put("json",dashboard.getJson());
                dataobj.put("name",dashboard.getName());
                dataobj.put("isactive",dashboard.isActive());
                dataobj.put("isdeleted",dashboard.isDeleted());
                dataArray.put(dataobj);
            }

            if (resultObj.getEntityList().size() > 0) {
                jobj.put("success", true);
                jobj.put("data", dataArray);
                jobj.put("valid", true);
                jobj.put("msg", "Dashboard saved successfully.");
            } else {
                jobj.put("success", false);
            }

        } catch (JSONException ex) {
            Logger.getLogger(AccUSDashboardServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    /*
     * Function to get inventory details of product. Used from configured
     * product view.
     */
    @Override
    public JSONObject getProductViewInvDetails(JSONObject paramJobj) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONArray dataArray = new JSONArray();
        KwlReturnObject resultObj = null;
        try {
            //Location Details
            String userId = paramJobj.optString(Constants.useridKey);
            String productId = paramJobj.optString("productid");
            KwlReturnObject result = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) result.getEntityList().get(0);
            Company company = user.getCompany();
            String companyId = company.getCompanyID();
            SimpleDateFormat df = new SimpleDateFormat(Constants.ddMMyyyy);
            SimpleDateFormat df1 = new SimpleDateFormat(Constants.yyyyMMdd);
            JSONObject dataobj = new JSONObject();

            try {
                /*
                 * Get Location details of product
                 */
                Set<Store> storeSet = null;
                boolean includeQAAndRepairStore = true;
                boolean includePickandPackStore = true;
                List<Store> stores = storeService.getStoresByStoreExecutivesAndManagers(user, true, null, null, null, includeQAAndRepairStore, includePickandPackStore);
                if (!stores.isEmpty()) {
                    storeSet = new HashSet<>(stores);
                }
                Location location = null;

                List<Object[]> list = stockService.getStoreWiseDetailedStockList(company, storeSet, location, null, null, productId);
                StringBuilder locationDetails = new StringBuilder();
                double stockOnHand = 0;
                for (Object[] stock : list) {
                    String uomName = stock[4] != null ? (String) stock[4] : null;
                    String locName = stock[8] != null ? (String) stock[8] : null;
                    double quantity = stock[14] != null ? (double) stock[14] : 0;
                    String qty = authHandler.formattedQuantity(quantity, companyId); 
                    locationDetails.append(locName).append(" - ").append(quantity >= 0 ? qty : "(<span style='color:red;'>"+qty+"</span>)").append(" ").append(uomName).append("<br>");
                    stockOnHand += quantity;
                }
                String stockonhand = authHandler.formattedQuantity(stockOnHand, companyId);
                //Showing negative value in red and in bracket.
                stockonhand = stockOnHand >= 0 ? stockonhand : "(<span style='color:red;'>"+stockonhand+"</span>)";
                dataobj.put("locationDetails", locationDetails.toString());
                dataobj.put("stockOnHand", stockonhand);
            } catch (Exception ex) {
                Logger.getLogger(AccUSDashboardServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            
            
            HashMap<String, Object> requestParams = accPurchaseOrderServiceDAOobj.getPurchaseOrderMap(paramJobj);
            requestParams.put("companyid", companyId);
            requestParams.put("gcurrencyid", paramJobj.optString(Constants.globalCurrencyKey));
            requestParams.put("isOutstanding", true);
            requestParams.put(Constants.REQ_startdate, paramJobj.optString("startdate"));
            requestParams.put(Constants.REQ_enddate, paramJobj.optString("enddate"));
            
            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("ID", companyId);
            Object prefObject = kwlCommonTablesDAOObj.getRequestedObjectFields(CompanyAccountPreferences.class, new String[]{"withInvUpdate"}, paramMap);

            Boolean isTradingFlow = false;
            if (prefObject != null && prefObject instanceof Boolean) {
                isTradingFlow = (Boolean)prefObject;
            }
            requestParams.put("isTradingFlow", isTradingFlow);
            requestParams.put(Constants.start, "");
            requestParams.put(Constants.limit, "");
            try {
                /*
                 * Calculate Last outstanding purchase order date and quantity.
                 */

                requestParams.put("approvedPO", true);
                result = accPurchaseOrderobj.getOutstandingPurchaseOrders(requestParams);
                requestParams.remove("approvedPO");
                

                List<Object[]> list = result.getEntityList();
                dataobj.put("outStandingPOs", list!=null ? list.size() : 0);
              
            } catch (Exception ex) {
                Logger.getLogger(AccUSDashboardServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
           
            try {
                /*
                 * Get supplier(vendor) from latest purchase order.
                 */
                String lastSupplier = "";
                double totalOrderedQuantity = 0d;
                requestParams.remove("isOutstanding");
                requestParams.remove(Constants.productCategoryid);
//                requestParams.put(Constants.start, "0");
//                requestParams.put(Constants.limit, "1");
                requestParams.put("dir", "desc");
                requestParams.put("sort", "createdon");
                requestParams.put("isProductView", true);
                if (requestParams.containsKey(Constants.productid) && requestParams.containsKey(Constants.productCategoryid)) {
                    requestParams.remove(Constants.productCategoryid);
                }
                requestParams.remove(Constants.REQ_startdate);
                requestParams.remove(Constants.REQ_enddate);
                
                result = accPurchaseOrderobj.getPurchaseOrdersMerged(requestParams);
                List<Object[]> list = result.getEntityList();
                for (Object[] obj : list) {
                    if (StringUtil.isNullOrEmpty(lastSupplier)) {
                        String poid = obj[0].toString();
                        paramMap.clear();
                        Map<String, String> aliasMap = new HashMap<>();
                        aliasMap.put("vendor", "ve");
                        paramMap.put("ID", poid);
                        paramMap.put("aliasMap", aliasMap);
                        Object supplier = kwlCommonTablesDAOObj.getRequestedObjectFields(PurchaseOrder.class, new String[]{"ve.name"}, paramMap);
                        lastSupplier = (String) supplier;
                    }
                    
                    if (obj[2] != null && obj[2] instanceof Double) {
                        totalOrderedQuantity += ((Double) obj[2]);
                    }
                }

                dataobj.put("lastSupplier", lastSupplier);
                String totalqtyordered = authHandler.formattedQuantity(totalOrderedQuantity, companyId);
                totalqtyordered = totalOrderedQuantity >= 0 ? totalqtyordered : "(<span style='color:red;'>"+totalqtyordered+"</span>)";
                dataobj.put("totalOrderedQuantity", totalqtyordered);
                
            } catch (Exception ex) {
                Logger.getLogger(AccUSDashboardServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try {
                /*
                 * Get last GRN date and received quantity as last stock in date
                 * and quantity.
                 */
                String lastStockInDateQty = "----";
                double deliveredOty = 0;
                requestParams.put(Constants.REQ_startdate, paramJobj.optString("startdate"));
                requestParams.put(Constants.REQ_enddate, paramJobj.optString("enddate"));
                requestParams.put(Constants.start, "0");
                requestParams.put(Constants.limit, "1");
                requestParams.put("nondeleted", "true");
                result = accGoodsReceiptobj.getGoodsReceiptOrdersMerged(requestParams);
                requestParams.remove("nondeleted");
                List<Object[]> list = result.getEntityList();
                for (Object[] obj : list) {
                    Date stockInDate = (Date) obj[2];
                    deliveredOty = obj[3] != null ? (double) obj[3] : 0;
                    String laststockinQty = authHandler.formattedQuantity(deliveredOty, companyId);
                    //Showing negative value in red and in bracket.
                    laststockinQty = deliveredOty >= 0 ? laststockinQty : "(<span style='color:red;'>"+laststockinQty+"</span>)";
                    
                    lastStockInDateQty = df.format(stockInDate) + " ," + laststockinQty;
                }
                dataobj.put("lastStockInDate", lastStockInDateQty);
            } catch (Exception ex) {
                Logger.getLogger(AccUSDashboardServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
//            HashMap<String, Object> requestParams = accPurchaseOrderServiceDAOobj.getPurchaseOrderMap(paramJobj);
//            requestParams.put("isProductView", true);
//            result = accPurchaseOrderobj.getPurchaseOrderByProduct(requestParams);
//            list = result.getEntityList();
//            String lastDateAndQuantity ="";
//            double quantity;
//            double totalOrderedQuantity = 0d;
//            for (Object[] obj : list) {
//                String poid = obj[0].toString();
//                KwlReturnObject objItr = accountingHandlerDAO.getObject(PurchaseOrder.class.getName(), poid);
//                PurchaseOrder purchaseOrder = (PurchaseOrder) objItr.getEntityList().get(0);
//
//                String idvString = !StringUtil.isNullOrEmpty(obj[3].toString()) ? obj[3].toString() : ""; //as in list invoiedetail id comes 4th
//                KwlReturnObject objItrID = accountingHandlerDAO.getObject(PurchaseOrderDetail.class.getName(), idvString);
//                PurchaseOrderDetail idvObj = (PurchaseOrderDetail) objItrID.getEntityList().get(0);
//                
//                quantity = 0;
//                if (idvObj != null) {
//                    quantity = idvObj.getQuantity();
//                    totalOrderedQuantity += quantity;
//                }
//
//                if (StringUtil.isNullOrEmpty(lastDateAndQuantity)) {
//                    lastDateAndQuantity = df.format(purchaseOrder.getOrderDate()) + ", "+quantity;
//                }
//            }
//            
//            dataobj.put("lastPoDateAndQuantity", StringUtil.isNullOrEmpty(lastDateAndQuantity) ? "----":lastDateAndQuantity);
//            dataobj.put("totalOrderedQuantity", totalOrderedQuantity);
            dataArray.put(dataobj);

//            if (resultObj.getEntityList().size() > 0) {
                jobj.put("success", true);
                jobj.put("data", dataArray);
                jobj.put("valid", true);
                jobj.put("msg", "Dashboard saved successfully.");
//            } else {
//                jobj.put("success", false);
//            }

        } catch (JSONException ex) {
            Logger.getLogger(AccUSDashboardServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
}