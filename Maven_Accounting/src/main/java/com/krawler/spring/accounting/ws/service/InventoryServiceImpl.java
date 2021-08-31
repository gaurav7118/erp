/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.inventory.model.store.StoreService;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.cyclecount.*;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.masteritems.service.AccMasterItemsService;
import com.krawler.inventory.model.sequence.SeqFormat;
import com.krawler.inventory.model.sequence.SeqModule;
import com.krawler.inventory.model.sequence.SeqService;
import com.krawler.inventory.model.stock.Stock;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.store.StorageException;
import org.springframework.context.MessageSource;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreType;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import com.krawler.inventory.model.location.Location;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.pos.AccPOSInterfaceService;
import com.krawler.spring.accounting.pos.POSERPMapping;


/**
 *
 * @author krawler
 */
public class InventoryServiceImpl implements InventoryService{
 
    private WSUtilService wsUtilService;
    private StoreService storeService;
    private MessageSource messageSource;
    private SeqService seqService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private static final DateFormat yyyyMMdd_HIPHON = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat yyyyMMddHHmmss_HIPHON = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private CycleCountService cycleCountService;
    private StockService stockService;
    private AccMasterItemsService accMasterItemsService;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private AccPOSInterfaceService accPOSInterfaceService ;
    
    public void setaccPOSInterfaceService(AccPOSInterfaceService accPOSInterfaceService) {
        this.accPOSInterfaceService = accPOSInterfaceService;
    }
    
    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }
        
    public void setwsUtilService(WSUtilService wsUtilService) {
        this.wsUtilService = wsUtilService;
    }
    
    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    
    public void setSeqService(SeqService seqService) {
        this.seqService = seqService;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }
    
    public void setCycleCountService(CycleCountService cycleCountService) {
        this.cycleCountService = cycleCountService;
    }
    
    public void setAccMasterItemsService(AccMasterItemsService accMasterItemsService) {
        this.accMasterItemsService = accMasterItemsService;
    }
    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getSequenceFormats(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (paramJobj != null) {
                if (!paramJobj.has("isActive") || !paramJobj.has("moduleId")) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }
           String companyId =paramJobj.optString(Constants.companyKey);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            String start = paramJobj.optString("start",null);
            String limit = paramJobj.optString("limit",null);
            paging = new Paging(start, limit);
            SeqModule seqModule = null;
            String searchString = paramJobj.optString("ss",null);
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("moduleId",null))) {
                seqModule = seqService.getSeqModule(Integer.parseInt(paramJobj.getString("moduleId")));
            }

            List<SeqFormat> seqFormatList;
            if ("true".equalsIgnoreCase(paramJobj.optString("isActive",null))) {
                seqFormatList = seqService.getActiveSeqFormats(company, seqModule, searchString, paging);
            } else {
                seqFormatList = seqService.getSeqFormats(company, seqModule, searchString, paging);
            }

            for (SeqFormat sf : seqFormatList) {
                JSONObject jObj = new JSONObject();
                jObj.put("seqFormatId", sf.getId());
                jObj.put("moduleId", sf.getSeqModule().getId());
                jObj.put("moduleName", sf.getSeqModule().getName());
                jObj.put("companyId", sf.getCompany().getCompanyID());
                jObj.put("prefix", sf.getPrefix());
                jObj.put("suffix", sf.getSuffix());
                jObj.put("separator", sf.getSeparator());
                jObj.put("prefixDateFormat", sf.getPrefixDateFormat() == null ? null : sf.getPrefixDateFormat().toString());
                jObj.put("suffixDateFormat", sf.getSuffixDateFormat() == null ? null : sf.getSuffixDateFormat().toString());
                jObj.put("numberOfDigits", sf.getNumberOfDigits());
                jObj.put("startFrom", sf.getStartFrom());
                jObj.put("leadingZero", true);
                jObj.put("formatedNumber", sf.getFormat());
                jObj.put("isDefault", sf.isDefaultFormat());
                jObj.put("isActive", sf.isActive());
                jArray.put(jObj);
            }
            issuccess = true;
            msg = "Seq formates have been fetched successfully";

        } catch (StorageException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } finally {
            try {
                if (paging != null) {
                    result.put(Constants.RES_TOTALCOUNT, paging.getTotalRecord());
                } else {
                    result.put(Constants.RES_TOTALCOUNT, jArray.length());
                }
                result.put(Constants.RES_data, jArray);
                result.put(Constants.RES_success, issuccess);
                result.put(Constants.RES_MESSAGE, msg);

            } catch (JSONException ex) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
        }
        return result;
    } 
    
  @Override  
  @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getStoreList(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (paramJobj != null) {
                if (!paramJobj.has("isActive") || !paramJobj.has("excludeQARepair") || ! paramJobj.has(Constants.useridKey)) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }
            String userId = paramJobj.optString(Constants.useridKey);

            String start = paramJobj.optString("start",null);
            String limit = paramJobj.optString("limit", null);
            paging = new Paging(start, limit);

            String searchString = paramJobj.optString("ss", null);

            Boolean isActive = null;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isActive", null))) {
                isActive = Boolean.parseBoolean(paramJobj.optString("isActive"));
            }
            boolean excludeQARepair = false;
            boolean includePickandPackStore=false;
            if ("true".equalsIgnoreCase(paramJobj.optString("excludeQARepair", null))) {
                excludeQARepair = true;
            }
            /**
             * Include pack Store or not. If true, include pack store else exclude.
             */
            if ("true".equalsIgnoreCase(paramJobj.optString("includePickandPackStore", null))) {
                includePickandPackStore = true;
            }
            
            String storeTypes = paramJobj.optString("storeTypes", null); // comma seperated
            StoreType[] storeTypeList = null;
            if (!StringUtil.isNullOrEmpty(storeTypes)) {
                String[] storeTypesArr = storeTypes.split(",");
                storeTypeList = new StoreType[storeTypesArr.length];
                int count = 0;
                for (String st : storeTypesArr) {
                    int stId = Integer.parseInt(st);
                    for (StoreType storeType : StoreType.values()) {
                        if (stId == storeType.ordinal()) {
                            storeTypeList[count++] = storeType;
                        }
                    }
                }
            }
            List<Store> storeList = storeService.getStoresByUser(userId, isActive, storeTypeList, excludeQARepair, searchString, paging,includePickandPackStore);

            for (Store store : storeList) {
                paramJobj.put(POSERPMapping.StoreId,store.getId());
                JSONObject storeConfigsJobj = accPOSInterfaceService.getERPPOSMappingDetails(paramJobj);
                
                JSONObject jObj = storeService.getStoreJson(store);
                if (storeConfigsJobj.has(Constants.RES_data) && storeConfigsJobj.get(Constants.RES_data) != null) {
                    jObj.put(Constants.RES_data, storeConfigsJobj.opt(Constants.RES_data));
                }
                jArray.put(jObj);
            }
            issuccess = true;
            msg = "Store has been fetched successfully";

        } catch (StorageException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } finally {
            try {
                if (paging != null) {
                    result.put(Constants.RES_TOTALCOUNT, paging.getTotalRecord());
                } else {
                    result.put(Constants.RES_TOTALCOUNT, jArray.length());
                }
                result.put(Constants.RES_data, jArray);
                result.put(Constants.RES_success, issuccess);
                result.put(Constants.RES_MESSAGE, msg);

            } catch (JSONException ex) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
        }
        return result;
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getUsersfromStore(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (paramJobj != null) {
                if (!paramJobj.has(Constants.storeid)) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }
            String storeid = paramJobj.optString(Constants.storeid, null);
            Store store = storeService.getStoreById(storeid);
            JSONArray userJsonArray = new JSONArray();
            if (store != null) {
                JSONObject storeJObj = new JSONObject();
                Set<User> mgrSet = store.getStoreManagerSet();
                mgrSet.addAll(store.getStoreExecutiveSet());
                for (User user : mgrSet) {
                    JSONObject userJObj = new JSONObject();
                    userJObj.put(Constants.useridKey, user.getUserID());
                    userJObj.put("designation", !StringUtil.isNullOrEmpty(user.getDesignation()) ? user.getDesignation() : "");
                    userJObj.put(Constants.userfullname, user.getFullName());
                    userJObj.put(Constants.username, user.getUserLogin()!=null ? user.getUserLogin().getUserName() : "");
                    
                    userJObj.put(Constants.roleid, user.getRoleID());
                    userJObj.put("department", !StringUtil.isNullOrEmpty(user.getDepartment()) ? user.getDepartment() : "");
                    userJObj.put("address", !StringUtil.isNullOrEmpty(user.getAddress()) ? user.getAddress() : "");
                    userJObj.put("contactnumber", !StringUtil.isNullOrEmpty(user.getContactNumber()) ? user.getContactNumber() : "");
                    userJObj.put("emailid", !StringUtil.isNullOrEmpty(user.getEmailID()) ? user.getEmailID() : "");
                    userJsonArray.put(userJObj);
                }
                storeJObj.put("users", userJsonArray);
                storeJObj.put(Constants.storeid, store.getId());
                storeJObj.put("storetype", store.getStoreType());
                storeJObj.put("storefullname", !StringUtil.isNullOrEmpty(store.getFullName()) ? store.getFullName() : "");
                jArray.put(storeJObj);
            }

            if (jArray.length() > 0) {
                issuccess = true;
                msg = messageSource.getMessage("acc.common.erp36", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
            }

        } catch (StorageException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } finally {
            try {
                result.put(Constants.RES_data, jArray);
                result.put(Constants.RES_success, issuccess);
                result.put(Constants.RES_MESSAGE, msg);
            } catch (JSONException ex) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
        }
        return result;
    }
  
  
  @Override  
  @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getCycleCountList(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        JSONArray pagedJsonArray = new JSONArray();
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (paramJobj.has("viewdraft") && paramJobj.optBoolean("viewdraft", false) == true) {
                if (paramJobj != null) {
                    if (!paramJobj.has(Constants.useridKey)) {
                        throw ServiceException.FAILURE("Missing required field", "e01", false);
                    }
                }
                String userId = paramJobj.optString(Constants.useridKey);
                String companyId = paramJobj.optString(Constants.companyKey);
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

                issuccess = true;
                msg = "Cycle Count Drafts  has been fetched successfully";
            
            } else {
                if (paramJobj != null) {
                    if (!paramJobj.has("countdate") || !paramJobj.has("isDraft") || !paramJobj.has("storeid")) {
                        throw ServiceException.FAILURE("Missing required field", "e01", false);
                    }
                }

                String companyId = paramJobj.optString(Constants.companyKey);
                KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
                Company company = (Company) jeresult.getEntityList().get(0);
                String countDate = paramJobj.getString("countdate");
                Date date = yyyyMMdd_HIPHON.parse(countDate);

                boolean isDraft = false;
                if ("true".equals(paramJobj.optString("isDraft", null))) {
                    isDraft = true;
                }

                String storeId = paramJobj.optString("storeid", null);
                Store store = null;
                Set storeSet = new HashSet();
                if (!StringUtil.isNullOrEmpty(storeId)) {
                    store = storeService.getStoreById(storeId);
                    storeSet.add(store);
                }

                Map<String, CycleCount> ccDraftMap = new HashMap();
                if (isDraft) {
                    List<CycleCount> ccDraftList = cycleCountService.getCycleCountDraftList(storeId, date);
                    for (CycleCount draft : ccDraftList) {
                        ccDraftMap.put(draft.getProduct().getID(), draft);
                    }
                }

                List<Object[]> ccProducts = cycleCountService.getCycleCountProducts(company, date);
                Map<Product, Double> dateWiseStockMap = stockService.getDateWiseStockList(company, storeSet, null, date, null, null);
                List<Stock> currentStockList = stockService.getStoreWiseStockList(company, storeSet, null, null, null, null);
                Map<Product, Double> currentProductStockMap = new HashMap();
                for (Stock stock : currentStockList) {
                    currentProductStockMap.put(stock.getProduct(), stock.getQuantity());
                }
                cycleCountService.updateCCProductJArray(ccProducts, dateWiseStockMap, ccDraftMap, jArray, currentProductStockMap);
                if (isDraft) {
                    List<Object[]> ccExtraProducts = cycleCountService.getCycleCountDraftExtraProducts(company, date);
                    cycleCountService.updateCCProductJArray(ccExtraProducts, dateWiseStockMap, ccDraftMap, jArray, currentProductStockMap);
                }

                issuccess = true;
                msg = "Product  has been fetched successfully";
            }//else
            
            
            pagedJsonArray = jArray;
            String start = paramJobj.optString(Constants.start);
            String limit = paramJobj.optString(Constants.limit);
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJsonArray = StringUtil.getPagedJSON(pagedJsonArray, Integer.parseInt(start), Integer.parseInt(limit));
            }
            
        } catch (StorageException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } finally {
            try {
                result.put(Constants.RES_TOTALCOUNT, pagedJsonArray.length());
                result.put(Constants.RES_data, jArray);
                result.put(Constants.RES_success, issuccess);
                result.put(Constants.RES_MESSAGE, msg);

            } catch (JSONException ex) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
        }
        return result;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getStoreLocations(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (paramJobj != null) {
                if (paramJobj.has("storeid")) {
                    String storeId = paramJobj.getString("storeid");
                    Store store = storeService.getStoreById(storeId);
                    if (store != null) {
                        Set<Location> locationSet = store.getLocationSet();
                        for (Location loc : locationSet) {
                            if (loc.isActive()) {
                                JSONObject jObj = new JSONObject();
                                jObj.put("id", loc.getId());
                                jObj.put("name", loc.getName());
                                jObj.put("isdefault", loc.isDefaultLocation());
                                jArray.put(jObj);
                            }
                        }
                    }
                } else {
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                    filter_names.add("company.companyID");
                    filter_params.add(paramJobj.getString(Constants.companyKey));
                    order_by.add("name");
                    order_type.add("asc");
                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    filterRequestParams.put("order_by", order_by);
                    filterRequestParams.put("order_type", order_type);
                    KwlReturnObject masterresult = accMasterItemsDAOobj.getLocationItems(filterRequestParams);

                    List<InventoryLocation> list = masterresult.getEntityList();
                    Iterator itr = list.iterator();
                    JSONArray jArr = new JSONArray();
                    for (InventoryLocation inventoryLocation : list) {
                        JSONObject obj = new JSONObject();
                        obj.put("id", inventoryLocation.getId());
                        obj.put("name", inventoryLocation.getName());
                        obj.put("parentid", inventoryLocation.getParentId() != null ? inventoryLocation.getParentId() : "");
                        obj.put("isdefault", inventoryLocation.isIsdefault());
                        jArray.put(obj);
                    }
                }
                issuccess = true;
                msg = "Store locations has been fetched successfully";
            }

        } catch (StorageException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } finally {
            try {
                if (paging != null) {
                    result.put(Constants.RES_TOTALCOUNT, paging.getTotalRecord());
                } else {
                    result.put(Constants.RES_TOTALCOUNT, jArray.length());
                }
                result.put(Constants.RES_data, jArray);
                result.put(Constants.RES_success, issuccess);
                result.put(Constants.RES_MESSAGE, msg);

            } catch (JSONException ex) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
        }
        return result;
    }  
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getStoreProductWiseAllAvailableStockDetailList(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (paramJobj != null) {
                if (!paramJobj.has("storeid")||!paramJobj.has("productId")||!paramJobj.has("businessDate")) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }
            if (paramJobj.has("storeid") && paramJobj.get("storeid") != null) {
                paramJobj.put("storeId", paramJobj.optString("storeid"));
            }
            result = stockService.getStoreProductWiseAllAvailableStockDetailList(paramJobj);
            if (result.has(Constants.RES_msg) && result.get(Constants.RES_msg) != null) {
                result.put(Constants.RES_MESSAGE, result.optString(Constants.RES_msg));
                result.remove(Constants.RES_msg);
            }
            
            if (result.has("count") && result.get("count") != null) {
                result.put(Constants.RES_TOTALCOUNT, result.optString("count"));
                result.remove("count");
            }
        } catch (StorageException ex) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        return result;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject validateCycleCountDate(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        JSONArray jArray = new JSONArray();
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (paramJobj != null) {
                if (!paramJobj.has("storeid") || !paramJobj.has("countdate")) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }
            
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("countdate",null))) {
                paramJobj.put("countDate", paramJobj.optString("countdate"));
            }
            
            String storeId = paramJobj.optString("storeid");
            String countDate = paramJobj.optString("countDate");

            Date businessDate = yyyyMMdd_HIPHON.parse(countDate);

            Date latestCycleCountDate = cycleCountService.getLastCycleCountDate(storeId);

            boolean isCycleCountDone = false;
            String latestCCDate = "";
            if (latestCycleCountDate != null) {
                latestCCDate = yyyyMMdd_HIPHON.format(latestCycleCountDate);
                if (latestCycleCountDate != null && yyyyMMdd_HIPHON.format(businessDate).compareTo(latestCCDate) <= 0) {
                    isCycleCountDone = true;
                    issuccess = false;
                    msg = "Cycle count is already done for " + latestCCDate + ", so you can not save cycle count details for any past date.";
                }
            }

            JSONObject jObj = new JSONObject();
            jObj.put("cycleCountDone", isCycleCountDone);
            jObj.put("cycleCountDate", latestCCDate);

            jArray.put(jObj);

        } catch (StorageException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } finally {
            try {
                result.put(Constants.RES_TOTALCOUNT, jArray.length());
                result.put(Constants.RES_data, jArray);
                result.put(Constants.RES_success, issuccess);
                result.put(Constants.RES_MESSAGE, msg);

            } catch (JSONException ex) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
        }
        return result;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject saveCycleCount(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (paramJobj != null) {
                if (!paramJobj.has("storeid") || !paramJobj.has("countingdate") || !paramJobj.has(Constants.isdefaultHeaderMap) || !paramJobj.has("jsondata") || !paramJobj.has("seqFormatId") || !paramJobj.has("isDraft")
                        ||!paramJobj.has(Constants.useridKey)) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }
            jobj = cycleCountService.addCycleCountRequest(paramJobj);
            if (jobj.has(Constants.RES_msg) && jobj.get(Constants.RES_msg) != null) {
                jobj.put(Constants.RES_MESSAGE, jobj.optString(Constants.RES_msg));
            }
            
        } catch (StorageException ex) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } catch (Exception ex) {
            msg = ex.getMessage();
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        return jobj;
    }
    
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getCycleCountReport(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (paramJobj != null) {
                if (!paramJobj.has("fromDate") || !paramJobj.has("toDate") || !paramJobj.has("storeId") ||!paramJobj.has(Constants.timezonedifference)||!paramJobj.has(Constants.userdateformat)) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }
            String start = paramJobj.optString("start", null);
            String limit = paramJobj.optString("limit", null);
            paging = new Paging(start, limit);
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("pagingObject", paging);
            jArray = cycleCountService.getCycleCountReport(paramJobj, requestParams);
            issuccess = true;
            msg = "Cycle Count Report fetched successfully";
        } catch (StorageException ex) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } catch (Exception ex) {
            msg = ex.getMessage();
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_MESSAGE, msg);
                jobj.put(Constants.RES_data, jArray);
                if (paging != null) {
                    jobj.put(Constants.RES_count, paging.getTotalRecord());
                } else {
                    jobj.put(Constants.RES_count, jArray.length());
                }
            } catch (JSONException ex) {
                 throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
        }
        return jobj;
    }
    
 
   //    @Override
//    @Transactional(propagation = Propagation.REQUIRED)
//    public JSONObject getExtraItemList(JSONObject paramJobj) throws ServiceException, JSONException {
//        JSONObject result = new JSONObject();
//        String msg = "";
//        boolean issuccess = false;
//        JSONArray jArray = new JSONArray();
//        JSONArray pagedJsonArray = new JSONArray();
//        try {
//            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
//             if (paramJobj != null) {
//                    if (!paramJobj.has("countdate")) {
//                        throw ServiceException.FAILURE("Missing required field", "e01", false);
//                    }
//                }
//
//            String companyId = paramJobj.optString(Constants.companyKey);
//            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
//            Company company = (Company) jeresult.getEntityList().get(0);
//            String countDate = paramJobj.getString("countdate");
//            Date date = yyyyMMdd_HIPHON.parse(countDate);
//
//            String storeId = paramJobj.optString("storeid", null);
//            Store store = null;
//            Set storeSet = new HashSet();
//            if (!StringUtil.isNullOrEmpty(storeId)) {
//                store = storeService.getStoreById(storeId);
//                storeSet.add(store);
//            }
//
//            Map<String, CycleCount> ccDraftMap = new HashMap();
//
//
//            List<Object[]> ccProducts = cycleCountService.getCycleCountExtraProducts(company, date);
//
//            Map<Product, Double> map = stockService.getDateWiseStockList(company, storeSet, null, date, null, null);
//            List<Stock> currentStockList = stockService.getStoreWiseStockList(company, storeSet, null, null, null, null);
//            Map<Product, Double> currentProductStockMap = new HashMap();
//            for (Stock stock : currentStockList) {
//                currentProductStockMap.put(stock.getProduct(), stock.getQuantity());
//            }
//
//            for (Object[] p : ccProducts) {
//                Product product = p[0] != null ? (Product) p[0] : null;
//                String casingUomName = p[1] != null ? (String) p[1] : "-";
//                String innerUomName = p[2] != null ? (String) p[2] : "-";
//                String looseUomName = p[3] != null ? (String) p[3] : "-";
//                double casingUomValue = p[4] != null ? (Double) p[4] : 0.0;
//                double innerUomValue = p[5] != null ? (Double) p[5] : 0.0;
//                double looseUomValue = p[6] != null ? (Double) p[6] : 0.0;
//
//                String id = product != null ? product.getID() : null;
//                String productCode = product != null ? product.getProductid() : null;
//                String productName = product != null ? product.getProductName() : null;
//                boolean batchForProduct = product != null ? product.isIsBatchForProduct() : false;
//                boolean serialForProduct = product != null ? product.isIsSerialForProduct() : false;
//                boolean rowForProduct = product != null ? product.isIsrowforproduct() : false;
//                boolean rackForProduct = product != null ? product.isIsrackforproduct() : false;
//                boolean binForProduct = product != null ? product.isIsbinforproduct() : false;
//
//                JSONObject jObj = new JSONObject();
//
//                jObj.put("id", id);
//                jObj.put("code", productCode);
//                jObj.put("name", productName);
//                jObj.put("isRowForProduct", rowForProduct);
//                jObj.put("isRackForProduct", rackForProduct);
//                jObj.put("isBinForProduct", binForProduct);
//                jObj.put("isBatchForProduct", batchForProduct);
//                jObj.put("isSerialForProduct", serialForProduct);
//                jObj.put("casinguom", casingUomName);
//                jObj.put("inneruom", innerUomName);
//                jObj.put("looseuom", looseUomName);
//                jObj.put("casinguomval", casingUomValue);
//                jObj.put("inneruomval", innerUomValue);
//                jObj.put("looseuomval", looseUomValue);
//                String packaging = Packaging.packagingPreview(casingUomName, casingUomValue, innerUomName, innerUomValue, looseUomName, looseUomValue);
//                jObj.put("packaging", packaging);
//                double qty = map.containsKey(product) ? map.get(product) : 0;
//                jObj.put("sysqty", qty);
//                jObj.put("casinguomcnt", "");
//                jObj.put("inneruomcnt", "");
//                jObj.put("looseuomcnt", "");
//                jObj.put("currentsysqty", currentProductStockMap.containsKey(product) ? currentProductStockMap.get(product) : 0);
//                if (ccDraftMap.containsKey(id)) {
//                    CycleCount draft = ccDraftMap.get(id);
//                    JSONObject draftObject = new JSONObject();
//                    draftObject.put("casinguomcnt", draft.getCasingUomCount());
//                    draftObject.put("inneruomcnt", draft.getInnerUomCount());
//                    draftObject.put("looseuomcnt", draft.getStockUomCount());
//                    double detailQty = 0;
//                    JSONArray stockDetails = new JSONArray();
//                    for (CycleCountDetail draftDetail : draft.getCycleCountDetails()) {
//                        JSONObject ccdObj = new JSONObject();
//                        ccdObj.put("locationId", draftDetail.getLocation().getId());
//                        ccdObj.put("locationName", draftDetail.getLocation().getName());
//                        StoreMaster row = draftDetail.getRow();
//                        if (row != null) {
//                            ccdObj.put("rowId", row.getId());
//                            ccdObj.put("rowName", row.getName());
//                        }
//                        StoreMaster rack = draftDetail.getRack();
//                        if (rack != null) {
//                            ccdObj.put("rackId", rack.getId());
//                            ccdObj.put("rackName", rack.getName());
//                        }
//                        StoreMaster bin = draftDetail.getBin();
//                        if (bin != null) {
//                            ccdObj.put("rowId", bin.getId());
//                            ccdObj.put("rowName", bin.getName());
//                        }
//                        ccdObj.put("batchName", draftDetail.getBatchName());
//                        ccdObj.put("actualQty", draftDetail.getActualQuantity());
//                        ccdObj.put("actualSerials", draftDetail.getActualSerials());
//
//                        stockDetails.put(ccdObj);
//
//                        detailQty += draftDetail.getActualQuantity();
//                    }
//                    draftObject.put("stockDetailQuantity", detailQty);
//                    draftObject.put("stockDetails", stockDetails);
//                    jObj.put("draftDetail", draftObject);
//                }
//
//                jArray.put(jObj);
//            }
//
//            issuccess = true;
//            msg = "Product  has been fetched successfully";
//            pagedJsonArray = jArray;
//            String start = paramJobj.optString(Constants.start);
//            String limit = paramJobj.optString(Constants.limit);
//            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
//                pagedJsonArray = StringUtil.getPagedJSON(pagedJsonArray, Integer.parseInt(start), Integer.parseInt(limit));
//            }
//
//        } catch (StorageException ex) {
//            msg = ex.getMessage();
//        } catch (Exception ex) {
//            msg = ex.getMessage();
//            throw ServiceException.FAILURE("Missing required field", "e01", false);
//        } finally {
//            try {
//                result.put(Constants.RES_TOTALCOUNT, pagedJsonArray.length());
//                result.put(Constants.RES_data, jArray);
//                result.put(Constants.RES_success, issuccess);
//                result.put(Constants.RES_MESSAGE, msg);
//
//            } catch (JSONException ex) {
//                throw ServiceException.FAILURE("Missing required field", "e01", false);
//            }
//        }
//        return result;
//    }  
}
