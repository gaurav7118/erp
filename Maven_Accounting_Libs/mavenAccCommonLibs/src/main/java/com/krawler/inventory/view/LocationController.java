/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.view;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ImportLog;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.store.StorageException;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.inventory.model.store.StoreType;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
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
public class LocationController extends MultiActionController implements MessageSourceAware{

    private static final Logger lgr = Logger.getLogger(LocationController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    private LocationService locationService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private StoreService storeService;
    private auditTrailDAO auditTrailObj;
    public ImportHandler importHandler;
    private companyDetailsDAO companyDetailsDAOObj;
    private kwlCommonTablesDAO kwlCommonTablesdao;
    private ImportDAO importDaoObj;
    private MessageSource messageSource;
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesdao) {
        this.kwlCommonTablesdao = kwlCommonTablesdao;
    }

    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj1) {
        this.companyDetailsDAOObj = companyDetailsDAOObj1;
    }

    public void setimportDAO(ImportDAO importDaoObj) {
        this.importDaoObj = importDaoObj;
    }
    public ModelAndView getLocations(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            boolean isInventoryTabOn = locationService.getCompanPreferencesSql(companyId);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);
            String searchString = request.getParameter("ss");
            List<Location> locationList = locationService.getLocations(company, searchString, paging);

            for (Location loc : locationList) {
                JSONObject jObj = new JSONObject();
                jObj.put("id", loc.getId());
                jObj.put("name", loc.getName());
                jObj.put("isactive", loc.isActive());
                jObj.put("isdefault", loc.isDefaultLocation());
                jObj.put("name", loc.getName());
                jObj.put("parentid", loc.getParentId());
//                if (isInventoryTabOn) {   ERP-21889
                    Set<Store> store = loc.getStores();
                    String sName = "";
                    String sIds = "";
                    for (Store storename : store) {
                        sIds += storename.getId() + ",";
                        sName += " " + storeService.getStoreById(storename.getId()).getAbbreviation() + " ,";
                    }
                    if (sName.endsWith(",")) {
                        sName = sName.substring(0, sName.length() - 1);
                    }
                    if (sIds.endsWith(",")) {
                        sIds = sIds.substring(0, sIds.length() - 1);
                    }
                    jObj.put("stores", sName);
                    jObj.put("storeids", sIds);
//                }
                jArray.put(jObj);
            }
            issuccess = true;
            msg = "locations has been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (StorageException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView addOrUpdateLocation(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        String auditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String storeidsStr = request.getParameter("store"); //multiple store ids with comma separeted
            String[] storeIds = storeidsStr.split(",");
            Set<Store> storSet = new HashSet<Store>();

            String newStoreNames = "";
            for (String s : storeIds) {
                Store store = storeService.getStoreById(s);
                storSet.add(store);

                if (!StringUtil.isNullOrEmpty(newStoreNames)) {
                    newStoreNames += ", ";
                }
                newStoreNames += store.getAbbreviation();
            }
            String locationName = request.getParameter("location");
            String action = request.getParameter("action");
            String parentId = request.getParameter("parentId");
            Location loc = null;
            if ("add".equalsIgnoreCase(action)) {
                loc = new Location(user.getCompany(), locationName);
                String newitemID = UUID.randomUUID().toString();
                loc.setId(newitemID);
                loc.setStores(storSet);
                loc.setParentId(parentId);
                locationService.addLocation(user, loc);
                msg = "Location has been added successfully"; //ERP-6053

                auditMessage += " added Location : " + locationName + ", mapped with Stores : (" + newStoreNames + ")";

            } else if ("edit".equalsIgnoreCase(action)) {
                String locationId = request.getParameter("locid");
                loc = locationService.getLocation(locationId);

                String oldLocationName = loc.getName();
                Set<Store> prevStore = loc.getStores();
                String oldStoreNames = "";
                for (Store ss : prevStore) {
                    if (!StringUtil.isNullOrEmpty(oldStoreNames)) {
                        oldStoreNames += ", ";
                    }
                    oldStoreNames += ss.getAbbreviation();
                }

                Set<Store> removedStores = loc.getStores();
                removedStores.removeAll(storSet);
                for (Store ss : removedStores) {
                    if (ss.getDefaultLocation() == loc) {
                        ss.setDefaultLocation(null);
                        storeService.updateStore(user, ss);
                    }
                }

                loc.setName(locationName);
                loc.setStores(storSet);
                loc.setParentId(parentId);

                locationService.updateLocation(user, loc);
                msg = "Location has been Edited successfully"; //ERP-6053

                if (!oldLocationName.equals(newStoreNames)) {
                    auditMessage += " updated Location: " + oldLocationName + " from (" + oldLocationName + " to " + locationName + ")";
                }
                if (prevStore.size() != loc.getStores().size() || (!prevStore.containsAll(loc.getStores()))) {
                    if (StringUtil.isNullOrEmpty(auditMessage)) {
                        auditMessage += " updated Location: " + oldLocationName + " mapped Stores: from (" + oldStoreNames + " to " + newStoreNames + ")";
                    } else {
                        auditMessage += ", mapped Stores: from (" + oldStoreNames + " to " + newStoreNames + ")";
                    }
                }
            }
            issuccess = true;

            if (!StringUtil.isNullOrEmpty(auditMessage)) {
                auditMessage = "User " + user.getFullName() + " has " + auditMessage;
                auditTrailObj.insertAuditLog(AuditAction.LOCATION_MASTER, auditMessage, request, loc.getId());
            }

            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (StorageException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());


    }

    public ModelAndView activateLocation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {

            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String id = request.getParameter("id");
            Location loc = locationService.getLocation(id);

            locationService.activateLocation(user, loc);

            issuccess = true;
            msg = "Store : " + loc.getName() + " has been activated successfully";

            auditMessage = "User " + user.getFullName() + " has activated Location: " + loc.getName();
            auditTrailObj.insertAuditLog(AuditAction.LOCATION_MASTER, auditMessage, request, loc.getId());

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (StorageException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView deactivateLocation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {

            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String id = request.getParameter("id");
            Location loc = locationService.getLocation(id);
            
            boolean isavlblQty = locationService.isStockInLocation(loc);
            if (!isavlblQty) {
                locationService.deactivateLocation(user, loc);

                issuccess = true;
                msg = "Store : " + loc.getName() + " has been deactivated successfully";

                auditMessage = "User " + user.getFullName() + " has deactivated Location: " + loc.getName();
                auditTrailObj.insertAuditLog(AuditAction.LOCATION_MASTER, auditMessage, request, loc.getId());

                txnManager.commit(status);
            }else{
                 issuccess = false;
                 msg = "Stock is available in  : " + loc.getName() + " location ,so you can not deactivate it ";
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (StorageException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView markLocationAsDefault(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("GSM_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject data = new JSONObject();
        String auditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String locationId = request.getParameter("locationid");
            Location loc = locationService.getLocation(locationId);

            locationService.setLocationAsDefault(user, loc);
            msg = "Location set as default successfully.";

            issuccess = true;

            auditMessage = "User " + user.getFullName() + " has set Location: " + loc.getName() + " as default";
            auditTrailObj.insertAuditLog(AuditAction.LOCATION_MASTER, auditMessage, request, loc.getId());

            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", data);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView importLocationRecords(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));

            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());

            if (doAction.compareToIgnoreCase("import") == 0) {
                System.out.println("A(( Import start : " + new Date());
                JSONObject datajobj = new JSONObject();
                JSONObject resjson = new JSONObject(request.getParameter("resjson").toString());
                JSONArray resjsonJArray = resjson.getJSONArray("root");

                String filename = request.getParameter("filename");
                datajobj.put("filename", filename);

                String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
                File filepath = new File(destinationDirectory + "/" + filename);
                datajobj.put("FilePath", filepath);

                datajobj.put("resjson", resjsonJArray);

                jobj = importLocationRecords(request, datajobj);
                System.out.println("A(( Import end : " + new Date());
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                System.out.println("A(( Validation start : " + new Date());
                jobj = importHandler.validateFileData(requestParams);
                System.out.println("A(( Validation end : " + new Date());
            }
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(Store.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(Store.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject importLocationRecords(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
        BufferedReader br = null;
        int total = 0, failed = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("filename");
        String masterPreference = request.getParameter("masterPreference");
        String prevUomSchema = "";
        JSONArray jArr = new JSONArray();
        boolean isRecordFailed = false;

        JSONObject returnObj = new JSONObject();

        try {
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyid);
            Company company = (Company) jeresult.getEntityList().get(0);
            jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User userLoging = (User) jeresult.getEntityList().get(0);
            boolean createUomSchemaTypeFlag = (request.getParameter("createUomSchemaTypeFlag") != null) ? Boolean.parseBoolean(request.getParameter("createUomSchemaTypeFlag")) : false;
            boolean deleteExistingUomSchemaFlag = (request.getParameter("deleteExistingUomSchemaFlag") != null) ? Boolean.parseBoolean(request.getParameter("deleteExistingUomSchemaFlag")) : false;

            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cnt = 0;

            StringBuilder failedRecords = new StringBuilder();
            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");

            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }

            while ((record = br.readLine()) != null) {
                String[] recarr = record.split(",");
                if (cnt == 0) {
                    failedRecords.append(createCSVrecord(recarr) + "\"Error Message\"");
                }
                if (cnt != 0) {
                    try {
                        Location location = null;
                        if (columnConfig.containsKey("Location Name")) {
                            String locationName = recarr[(Integer) columnConfig.get("Location Name")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(locationName)) {
                                location = locationService.getLocationByName(company, locationName);
                                if (masterPreference.equals("2") && location == null) {
                                    location = new Location();
                                    String newitemID = UUID.randomUUID().toString();
                                    location.setName(locationName);
                                    location.setId(newitemID);
                                } else if (location == null) {
                                    throw new AccountingException("Location is not found for " + locationName);
                                }
                            } else {
                                throw new AccountingException("Location is not available.");
                            }
                        } else {
                            throw new AccountingException("Location column is not found.");
                        }

                        Set<Store> storeSet = new HashSet<Store>();
                        if (columnConfig.containsKey("Store")) {
                            String stores = recarr[(Integer) columnConfig.get("Store")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(stores)) {
                                String[] storesNames = stores.split(";");
                                if (storesNames.length > 0) {
                                    storeSet = storeService.getStoreMapping(companyid, storesNames);
                                    if(storeSet.size()>0){
                                    location.setStores(storeSet);
                                    }else{
                                         throw new AccountingException("Store names are not found.");
                                    }
                                } else {
                                    throw new AccountingException("Store not available.");
                                }

                            } else {
                                throw new AccountingException("Store name available ");
                            }
                        } else {
                            throw new AccountingException("Store column not available");
                        }
                        location.setCompany(company);
                        location.setCreatedOn(new Date());
                        locationService.addLocation(userLoging, location);
                    } catch (Exception ex) {
                        failed++;
                        isRecordFailed = true;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                        }

                        failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }
                cnt++;
            }

            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = messageSource.getMessage("acc.field.Emptyfile", null, RequestContextUtils.getLocale(request));
            } else if (success == 0) {
//                issuccess = false;
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
            } else if (success == total) {
                msg = messageSource.getMessage("acc.rem.168", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request))+ " " + success + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
                msg += (failed == 0 ? "." : " "+messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request))+ " " + failed + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { // if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(Store.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            br.close();

            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                //Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_UOM_Schema_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDaoObj.saveImportLog(logDataMap);

                String tableName = importDaoObj.getTableName(fileName);
                importDaoObj.removeFileTable(tableName); // Remove table after importing all records

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(Store.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(Store.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }

    public static String getActualFileName(String storageName) {
        String ext = storageName.substring(storageName.lastIndexOf("."));
        String actualName = storageName.substring(0, storageName.lastIndexOf("_"));
        actualName = actualName + ext;
        return actualName;
    }

    public void createFailureFiles(String filename, StringBuilder failedRecords, String ext) {
        String destinationDirectory;
        try {
            destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = filename.substring(filename.lastIndexOf("."));
            }
            filename = filename.substring(0, filename.lastIndexOf("."));

            java.io.FileOutputStream failurefileOut = new java.io.FileOutputStream(destinationDirectory + "/" + filename + ImportLog.failureTag + ext);
            failurefileOut.write(failedRecords.toString().getBytes());
            failurefileOut.flush();
            failurefileOut.close();
        } catch (Exception ex) {
            System.out.println("\nError file write [success/failed] " + ex);
        }
    }

    public String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 0; i < listArray.length; i++) {    // Discard columns id at index 0 and isvalid, invalidColumns, validationlog at last 3 indexes.
//            String s = (listArray[i]==null)?"":listArray[i].toString();
            rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString().replaceAll("\"", "")) + "\",";
        }
        return rec;
    }
}
