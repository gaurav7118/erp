/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.inventory.view;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.StoreMaster;
import com.krawler.common.admin.User;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.model.cyclecount.*;
import com.krawler.inventory.model.cyclecount.impl.CycleCountBlankSheet;
import com.krawler.inventory.model.frequency.Frequency;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.stock.Stock;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author Vipin Gupta
 */
public class CycleCountController extends MultiActionController implements MessageSourceAware{

    private static final Logger lgr = Logger.getLogger(CycleCountController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    private StockService stockService;
    private StoreService storeService;
    private CycleCountService cycleCountService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private exportMPXDAOImpl exportDAO;
    private auditTrailDAO auditTrailObj;
    private CycleCountBlankSheet ccbs;
    private static final DateFormat yyyyMMdd_HIPHON = new SimpleDateFormat("yyyy-MM-dd");
    private MessageSource messageSource;

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setCycleCountService(CycleCountService cycleCountService) {
        this.cycleCountService = cycleCountService;
    }


    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setExportDAO(exportMPXDAOImpl exportDAO) {
        this.exportDAO = exportDAO;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }

    public void setCcbs(CycleCountBlankSheet ccbs) {
        this.ccbs = ccbs;
    }
    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    public ModelAndView getWeekNames(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        JSONArray jArray = new JSONArray();
        try {
            for (Week week : Week.values()) {
                JSONObject jObj = new JSONObject();
                jObj.put("id", week.ordinal());
                jObj.put("name", week.toString());
                jArray.put(jObj);
            }
            isSuccess = true;
            msg = "Store Types has been fetched successfully";


        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, isSuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_data, jArray);
                jobj.put(Constants.RES_count, jArray.length());
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getCycleCountCalendar(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        JSONArray jArray = new JSONArray();
        try {
            //DateFormat df2 = authHandler.getDateFormatter(request);

            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            String countMonth = request.getParameter("countmonth");
            Calendar cal = Calendar.getInstance();
            // DateFormat df2 = new SimpleDateFormat("dd-MM-yyyy");
            Date date = null;

            try {
                date = yyyyMMdd_HIPHON.parse(countMonth);
            } catch (ParseException ex) {
            }
            List<CycleCountCalendar> cclist = cycleCountService.getCycleCountCalendarForMonth(company, date);
            if (cclist.isEmpty()) {
                cclist = cycleCountService.getDefaultCalendarForMonth(company, date);
            }
            for (CycleCountCalendar ccl : cclist) {
                JSONObject jObj = new JSONObject();
                cal.setTime(ccl.getDate());
                jObj.put("id", ccl.getId());
                jObj.put("countdate", yyyyMMdd_HIPHON.format(ccl.getDate()));
                jObj.put("day", cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, java.util.Locale.ENGLISH));
                Set<Frequency> cclFrequencies = ccl.getFrequencies();
                String fName = "";
                String fIds = "";
                for (Frequency fN : cclFrequencies) {
                    if (!StringUtil.isNullOrEmpty(fIds)) {
                        fIds += ",";
                        fName += " ,";
                    }
                    fIds += fN.getId();
                    fName += fN.getName();
                }


                jObj.put("frequency", fName);
                jObj.put("frequencyid", fIds);
                jArray.put(jObj);

            }
            isSuccess = true;
            msg = "Cycle Count Calendar has been fetched successfully";


        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, isSuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_data, jArray);
                jobj.put(Constants.RES_count, jArray.length());
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }
    public ModelAndView getCycleCountFrequencyForDate(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        JSONArray jArray = new JSONArray();
        try {
            //DateFormat df2 = authHandler.getDateFormatter(request);

            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            String countdate = request.getParameter("countdate");
            Calendar cal = Calendar.getInstance();
            Date date = null;

            try {
                date = yyyyMMdd_HIPHON.parse(countdate);
            } catch (ParseException ex) {
            }
            List<CycleCountCalendar> cclist = cycleCountService.getCycleCountCalendarForDate(company, date);
            
            for (CycleCountCalendar ccl : cclist) {
                JSONObject jObj = new JSONObject();
                Set<Frequency> cclFrequencies = ccl.getFrequencies();
                String fName = "";
                String fIds = "";
                for (Frequency fN : cclFrequencies) {
                    if (!StringUtil.isNullOrEmpty(fIds)) {
                        fIds += ",";
                        fName += " ,";
                    }
                    fIds += fN.getId();
                    fName += fN.getName();
                }
                jObj.put("frequency", fName);
                jObj.put("frequencyid", fIds);
                jArray.put(jObj);

            }
            isSuccess = true;
            msg = "Cycle Count Calendar for date has been fetched successfully";


        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, isSuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_data, jArray);
                jobj.put(Constants.RES_count, jArray.length());
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView updateCCCalendar(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SOR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            String cmonth = request.getParameter("calendarMonth");
            JSONArray jArr = new JSONArray(request.getParameter("jsondata"));

            Date ccCalendarDate = yyyyMMdd_HIPHON.parse(cmonth);
            Map<Integer, Frequency> frequencyMap = cycleCountService.getAllFrequencyMap();
            List<CycleCountCalendar> cclMonthList = cycleCountService.getCycleCountCalendarForMonth(user.getCompany(), ccCalendarDate);

            Set<Integer> keySet = frequencyMap.keySet();

            Map<String, Set<Frequency>> givenFrequency = new HashMap();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj1 = jArr.getJSONObject(i);
                String freqIds = jobj1.getString("frequencyid"); // comma seperated ids
                String countDate = jobj1.getString("countdate");
                Date date = yyyyMMdd_HIPHON.parse(countDate);
                Set<Frequency> frequencySet = new HashSet<>();
                if (!StringUtil.isNullOrEmpty(freqIds)) {
                    String[] fIds = freqIds.split(",");
                    for (String fId : fIds) {
                        int frequncyId = Integer.parseInt(fId);
                        if (keySet.contains(frequncyId)) {
                            frequencySet.add(frequencyMap.get(frequncyId));
                        }
                    }
                }
                givenFrequency.put(yyyyMMdd_HIPHON.format(date), frequencySet);
            }
            if (cclMonthList.isEmpty()) {
                cclMonthList = cycleCountService.getDefaultCalendarForMonth(user.getCompany(), ccCalendarDate);
            }
            boolean updated = false;
            for (CycleCountCalendar ccc : cclMonthList) {
                Set<Frequency> fset = givenFrequency.get(yyyyMMdd_HIPHON.format(ccc.getDate()));
                boolean modified = false;
                if (ccc.getId() == null) {
                    ccc.setFrequencies(fset);
                    modified = true;
                } else {
                    Set<Frequency> oldFrequencys = ccc.getFrequencies();
                    if (oldFrequencys == null || fset.size() != oldFrequencys.size() || !fset.containsAll(oldFrequencys)) {
                        ccc.setFrequencies(fset);
                        modified = true;
                    }
                }
                if (modified) {
                    cycleCountService.addOrUpdateCCCalendar(ccc);
                    updated = true;
                }
            }

            if (updated) {
                DateFormat monthDF = new SimpleDateFormat("MMM yyyy");
                auditMessage = "User " + user.getFullName() + " has updated cycle count calendar for month: " + monthDF.format(ccCalendarDate);
                auditTrailObj.insertAuditLog(AuditAction.CC_CALENDAR_UPDATED, auditMessage, request, "0");
            }

            isSuccess = true;
            msg = messageSource.getMessage("acc.rem.CycleCountCalendarupdatedsuccessfully", null, RequestContextUtils.getLocale(request));

            txnManager.commit(status);

        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, isSuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getCCDraftList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        JSONArray jArray = new JSONArray();
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences ecp = (ExtraCompanyPreferences) jeresult.getEntityList().get(0);
            List<Object[]> ccDraftList = cycleCountService.getCycleCountDraftList(userId, null, null,ecp);
            for (Object[] ccd : ccDraftList) {
                Store store = ccd[0] != null ? (Store) ccd[0] : null;
                Date businessDate = ccd[1] != null ? (Date) ccd[1] : null;

                JSONObject jObj = new JSONObject();
                jObj.put("storeId", store.getId());
                jObj.put("storeName", store.getFullName());
                jObj.put("businessDate", yyyyMMdd_HIPHON.format(businessDate));
                jArray.put(jObj);
            }

            isSuccess = true;
            msg = "Cycle Count Drafts  has been fetched successfully";

        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, isSuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_data, jArray);
                jobj.put(Constants.RES_count, jArray.length() - 1);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }
    public ModelAndView getCCItemList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        JSONArray jArray = new JSONArray();
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            String countDate = request.getParameter("countdate");
            Date date = yyyyMMdd_HIPHON.parse(countDate);

            boolean isDraft = false;
            if("true".equals(request.getParameter("isDraft"))){
                isDraft = true;
            }
            
            String storeId = request.getParameter("storeid");
            Store store = null;
            Set storeSet = new HashSet();
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
                storeSet.add(store);
            }

            Map<String, CycleCount> ccDraftMap = new HashMap();
            if(isDraft){
                List<CycleCount> ccDraftList = cycleCountService.getCycleCountDraftList(storeId, date);
                for(CycleCount draft : ccDraftList){
                    ccDraftMap.put(draft.getProduct().getID(), draft);
                }
            }
            
            List<Object[]> ccProducts = cycleCountService.getCycleCountProducts(company, date);
            Map<Product, Double> dateWiseStockMap = stockService.getDateWiseStockList(company, storeSet, null, date, null, null);
            List<Stock> currentStockList = stockService.getStoreWiseStockList(company,storeSet,null,null,null,null);
            Map<Product, Double> currentProductStockMap = new HashMap();
            for(Stock stock : currentStockList){
                currentProductStockMap.put(stock.getProduct(), stock.getQuantity());
            }
             cycleCountService.updateCCProductJArray(ccProducts, dateWiseStockMap, ccDraftMap, jArray, currentProductStockMap);
            if(isDraft){
                List<Object[]> ccExtraProducts = cycleCountService.getCycleCountDraftExtraProducts(company, date);
                 cycleCountService.updateCCProductJArray(ccExtraProducts, dateWiseStockMap, ccDraftMap, jArray, currentProductStockMap);
            }

            isSuccess = true;
            msg = "Product  has been fetched successfully";

        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, isSuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_data, jArray);
                jobj.put(Constants.RES_count, jArray.length() - 1);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

   
    public ModelAndView getExtraItemList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        JSONArray jArray = new JSONArray();
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            String countDate = request.getParameter("countdate");
            Date date = yyyyMMdd_HIPHON.parse(countDate);

            String storeId = request.getParameter("storeid");
            Store store = null;
            Set storeSet = new HashSet();
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
                storeSet.add(store);
            }

            Map<String, CycleCount> ccDraftMap = new HashMap();
            
            
            List<Object[]> ccProducts = cycleCountService.getCycleCountExtraProducts(company, date);

            Map<Product, Double> map = stockService.getDateWiseStockList(company, storeSet, null, date, null, null);
            List<Stock> currentStockList = stockService.getStoreWiseStockList(company,storeSet,null,null,null,null);
            Map<Product, Double> currentProductStockMap = new HashMap();
            for(Stock stock : currentStockList){
                currentProductStockMap.put(stock.getProduct(), stock.getQuantity());
            }
            
            for (Object[] p : ccProducts) {
                Product product = p[0] != null ? (Product) p[0] : null;
                String casingUomName = p[1] != null ? (String) p[1] : "-";
                String innerUomName = p[2] != null ? (String) p[2] : "-";
                String looseUomName = p[3] != null ? (String) p[3] : "-";
                double casingUomValue = p[4] != null ? (Double) p[4] : null;
                double innerUomValue = p[5] != null ? (Double) p[5] : null;
                double looseUomValue = p[6] != null ? (Double) p[6] : null;
                
                String id = product != null ? product.getID() : null;
                String productCode = product != null ? product.getProductid() : null;
                String productName = product != null ? product.getProductName() : null;
                boolean batchForProduct = product != null ? product.isIsBatchForProduct() : false;
                boolean serialForProduct = product != null ? product.isIsSerialForProduct() : false;
                boolean rowForProduct = product != null ? product.isIsrowforproduct() : false;
                boolean rackForProduct = product != null ? product.isIsrackforproduct() : false;
                boolean binForProduct = product != null ? product.isIsbinforproduct() : false;

                JSONObject jObj = new JSONObject();

                jObj.put("id", id);
                jObj.put("code", productCode);
                jObj.put("name", productName);
                jObj.put("isRowForProduct", rowForProduct);
                jObj.put("isRackForProduct", rackForProduct);
                jObj.put("isBinForProduct", binForProduct);
                jObj.put("isBatchForProduct", batchForProduct);
                jObj.put("isSerialForProduct", serialForProduct);
                jObj.put("casinguom", casingUomName);
                jObj.put("inneruom", innerUomName);
                jObj.put("looseuom", looseUomName);
                jObj.put("casinguomval", casingUomValue);
                jObj.put("inneruomval", innerUomValue);
                jObj.put("looseuomval", looseUomValue);
                String packaging = Packaging.packagingPreview(casingUomName, casingUomValue, innerUomName, innerUomValue, looseUomName, looseUomValue);
                jObj.put("packaging", packaging);
                double qty = map.containsKey(product) ? map.get(product) : 0;
                jObj.put("sysqty", qty);
                jObj.put("casinguomcnt", "");
                jObj.put("inneruomcnt", "");
                jObj.put("looseuomcnt", "");
                jObj.put("currentsysqty", currentProductStockMap.containsKey(product) ? currentProductStockMap.get(product) : 0);
                if(ccDraftMap.containsKey(id)){
                    CycleCount draft = ccDraftMap.get(id);
                    JSONObject draftObject = new JSONObject();
                    draftObject.put("casinguomcnt", draft.getCasingUomCount());
                    draftObject.put("inneruomcnt", draft.getInnerUomCount());
                    draftObject.put("looseuomcnt", draft.getStockUomCount());
                    double detailQty = 0;
                    JSONArray stockDetails = new JSONArray();
                    for (CycleCountDetail draftDetail : draft.getCycleCountDetails()) {
                        JSONObject ccdObj = new JSONObject();
                        ccdObj.put("locationId", draftDetail.getLocation().getId());
                        ccdObj.put("locationName", draftDetail.getLocation().getName());
                        StoreMaster row = draftDetail.getRow();
                        if(row != null){
                            ccdObj.put("rowId", row.getId());
                            ccdObj.put("rowName", row.getName());
                        }
                        StoreMaster rack = draftDetail.getRack();
                        if(rack != null){
                             ccdObj.put("rackId", rack.getId());
                             ccdObj.put("rackName", rack.getName());
                        }
                        StoreMaster bin = draftDetail.getBin();
                        if(bin != null){
                            ccdObj.put("rowId", bin.getId());
                            ccdObj.put("rowName", bin.getName());
                        }
                        ccdObj.put("batchName", draftDetail.getBatchName());
                        ccdObj.put("actualQty", draftDetail.getActualQuantity());
                        ccdObj.put("actualSerials", draftDetail.getActualSerials());
                        
                        stockDetails.put(ccdObj);
                        
                        detailQty += draftDetail.getActualQuantity();
                    }
                    draftObject.put("stockDetailQuantity", detailQty);
                    draftObject.put("stockDetails", stockDetails);
                    jObj.put("draftDetail", draftObject);
                }
                
                jArray.put(jObj);
            }

            isSuccess = true;
            msg = "Product  has been fetched successfully";

        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, isSuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_data, jArray);
                jobj.put(Constants.RES_count, jArray.length() - 1);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }
    public ModelAndView printBlankSheet(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        JSONArray jArray = new JSONArray();
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            String countDate = request.getParameter("countdate");
            Date date = yyyyMMdd_HIPHON.parse(countDate);
            // get location, batch, rack, row, bin, serial activation details from cmopanypreferences
            KwlReturnObject extraPreferences = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraPreferences.getEntityList().get(0);
            KwlReturnObject compPreferences = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), companyId);
            CompanyAccountPreferences compAccPreferences = (CompanyAccountPreferences) compPreferences.getEntityList().get(0);

            String storeId = request.getParameter("storeid");
            Store store = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
            }

            Map config = new HashMap();
            config.put("company", company);
            config.put("store", store);
            config.put("businessDate", date);
            config.put("uomschema", extraCompanyPreferences.getUomSchemaType() == 0 ? true : false);
            config.put("islocationcompulsory", compAccPreferences.isIslocationcompulsory());
            config.put("isbatchcompulsory", compAccPreferences.isIsBatchCompulsory());
            config.put("israckcompulsory", compAccPreferences.isIsrackcompulsory());
            config.put("isrowcompulsory", compAccPreferences.isIsrowcompulsory());
            config.put("isbincompulsory", compAccPreferences.isIsbincompulsory());
            config.put("isserialcompulsory", compAccPreferences.isIsSerialCompulsory());
            ByteArrayOutputStream baos = ccbs.getPdfData(request, config);
            exportDAO.writeDataToFile("CycleCountSheet", "pdf", baos, response);
            

            isSuccess = true;
            msg = "Product  has been fetched successfully";

        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, isSuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_data, jArray);
                jobj.put(Constants.RES_count, jArray.length() - 1);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getCycleCountStatusReport(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);
            String businessDate = request.getParameter("businessDate");
            Date date = null;
            if (!StringUtil.isNullOrEmpty(businessDate)) {
                try {
                    date = yyyyMMdd_HIPHON.parse(businessDate);
                } catch (ParseException ex) {
                    lgr.log(Level.SEVERE, "Trying to parse " + businessDate + " with " + yyyyMMdd_HIPHON.toString() + " format", ex);
                }
            }
            List<Object[]> cycleCountStatusList = cycleCountService.getCycleCountStatusReport(userId, date, paging);

            for (Object[] cc : cycleCountStatusList) {
                String storeCode = cc[0] != null ? (String) cc[0] : "";
                String storeDesc = cc[1] != null ? (String) cc[1] : "";
                long itemCount = cc[2] != null ? ((BigInteger) cc[2]).longValue() : 0;
                JSONObject jObj = new JSONObject();
                jObj.put("storeCode", storeCode);
                jObj.put("storeDesc", storeDesc);
                jObj.put("itemCount", itemCount);

                jArray.put(jObj);
            }
            issuccess = true;
            msg = "Cycle Count Status Report fetched successfully";

        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_data, jArray);
                if (paging != null) {
                    jobj.put(Constants.RES_count, paging.getTotalRecord());
                } else {
                    jobj.put(Constants.RES_count, jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView getCycleCountDates(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            String storeId = request.getParameter("storeId");

            Date lastCycleCountDate = cycleCountService.getLastCycleCountDate(storeId);
            Date today = new Date();
            if (lastCycleCountDate == null) {
                lastCycleCountDate = today;
            }

            JSONObject jObj = new JSONObject();
            jObj.put("businessDate", yyyyMMdd_HIPHON.format(today));
            jObj.put("minDate", yyyyMMdd_HIPHON.format(lastCycleCountDate));

            jArray.put(jObj);

            issuccess = true;
            msg = "Cycle Count Status Report fetched successfully";

        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_data, jArray);
                jobj.put(Constants.RES_count, jArray.length());
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView validateCycleCountDate(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);

            String storeId = request.getParameter("storeId");
            String countDate = request.getParameter("countDate");

            Date businessDate = yyyyMMdd_HIPHON.parse(countDate);

            Date latestCycleCountDate = cycleCountService.getLastCycleCountDate(storeId);
            
            boolean isCycleCountDone = false;
            String latestCCDate = "";
            if(latestCycleCountDate != null){
                latestCCDate = yyyyMMdd_HIPHON.format(latestCycleCountDate);
                if(latestCycleCountDate != null && yyyyMMdd_HIPHON.format(businessDate).compareTo(latestCCDate) <= 0){
                    isCycleCountDone = true;
                }    
            }
//            boolean isCycleCountDone = cycleCountService.isCycleCountDone(storeId, businessDate);

            JSONObject jObj = new JSONObject();
            jObj.put("cycleCountDone", isCycleCountDone);
            jObj.put("cycleCountDate", latestCCDate);

            jArray.put(jObj);

            issuccess = true;
            msg = "Cycle Count Status Report fetched successfully";

        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_data, jArray);
                jobj.put(Constants.RES_count, jArray.length());
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }
    
    public ModelAndView getCycleCountReport(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String start = paramJobj.optString("start",null);
            String limit = paramJobj.optString("limit",null);
            paging = new Paging(start, limit);
            String exportFileName = paramJobj.optString("filename",null); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
                paging = null;
            }
            
            HashMap<String,Object> requestParams=new HashMap<>();
            requestParams.put("pagingObject", paging);
            String searchJson = request.getParameter("searchJson");
            String filterConjuction = request.getParameter("filterConjuctionCriteria");
            if (!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuction)) {
                requestParams.put("searchJson", searchJson);
                requestParams.put("filterConjuctionCriteria", filterConjuction);
            }
            jArray=cycleCountService.getCycleCountReport(paramJobj,requestParams);
            
            if (isExport) {
                JSONObject jsono = new JSONObject();
                jsono.put(Constants.RES_data, jArray);
                exportDAO.processRequest(request, response, jsono);
            }
            issuccess = true;
            msg = "Cycle Count Report fetched successfully";

        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_data, jArray);
                if (paging != null) {
                    jobj.put(Constants.RES_count, paging.getTotalRecord());
                } else {
                    jobj.put(Constants.RES_count, jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView addCycleCountRequest(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jobj=cycleCountService.addCycleCountRequest(paramJobj);   
        return new ModelAndView(successView, "model", jobj.toString());
    }

}
