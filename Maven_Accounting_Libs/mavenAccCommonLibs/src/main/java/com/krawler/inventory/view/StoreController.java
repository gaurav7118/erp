/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.view;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
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
import com.krawler.spring.authHandler.authHandler;
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
public class StoreController extends MultiActionController {

    private static final Logger lgr = Logger.getLogger(StoreController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    private StoreService storeService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private LocationService locationService;
    private auditTrailDAO auditTrailObj;
    public ImportHandler importHandler;
    private companyDetailsDAO companyDetailsDAOObj;
    private kwlCommonTablesDAO kwlCommonTablesdao;
    private ImportDAO importDaoObj;
    private MessageSource messageSource;
    
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
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
    public ModelAndView saveStore(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx_Save");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            String st = request.getParameter("storeType");
            String movementTypesStr = request.getParameter("movementType");
            Set<String> movementTypeSet = new HashSet<String>();
            if (!StringUtil.isNullOrEmpty(movementTypesStr)) {
                String[] movementtype = movementTypesStr.split(",");
                for (String movementtyp : movementtype) {
                    movementTypeSet.add(movementtyp);
                }
            }

            int stId = Integer.parseInt(st);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            Store store = new Store();
            store.setStoreType(storeService.getStoreType(stId));
            String abbreviation = request.getParameter("abbreviation");
            store.setAbbreviation(abbreviation);
            store.setDescription(request.getParameter("description"));
            store.setAddress(request.getParameter("address"));
            store.setContactNo(request.getParameter("contactno"));
            store.setFaxNo(request.getParameter("faxno"));
            store.setCcDateAllow(Boolean.parseBoolean(request.getParameter("ccdateallow")));
            store.setSmccAllow(Boolean.parseBoolean(request.getParameter("smccallow")));
            store.setParentId(request.getParameter("parentId"));
            store.setVATTINnumber(request.getParameter("vatno")!=null?request.getParameter("vatno"):"");
            store.setCSTTINnumber(request.getParameter("cstno")!=null?request.getParameter("cstno"):"");
            store.setCreatedBy(user);
            store.setMovementTypeSet(movementTypeSet);
            String userIdsStr = request.getParameter("users");
            Set<User> userSet = new HashSet<User>();
            if (!StringUtil.isNullOrEmpty(userIdsStr)) {
                String[] userIds = userIdsStr.split(",");
                for (String usrId : userIds) {
                    jeresult = accountingHandlerDAO.getObject(User.class.getName(), usrId);
                    User user1 = (User) jeresult.getEntityList().get(0);
                    userSet.add(user1);
                }
            }
            String executiveIdsStr = request.getParameter("executives");
            Set<User> executiveSet = new HashSet<User>();
            if (!StringUtil.isNullOrEmpty(executiveIdsStr)) {
                String[] executiveIds = executiveIdsStr.split(",");
                for (String executiveId : executiveIds) {
                    jeresult = accountingHandlerDAO.getObject(User.class.getName(), executiveId);
                    User user1 = (User) jeresult.getEntityList().get(0);
                    executiveSet.add(user1);
                }
            }
            store.setCompany(company);
            store.setStoreManagerSet(userSet);
            store.setStoreExecutiveSet(executiveSet);
            storeService.addStore(user, store);

            issuccess = true;
            msg = messageSource.getMessage("acc.store.saved.success", null, RequestContextUtils.getLocale(request));
            
            auditMessage = "User " + user.getFullName() + " has created  Store: " + store.getAbbreviation() + " (" + store.getDescription() + ") Type: " + store.getStoreType().toString();
            auditTrailObj.insertAuditLog(AuditAction.STORE_MASTER, auditMessage, request, store.getId());

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

    public ModelAndView updateStore(HttpServletRequest request, HttpServletResponse response) {
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
            String st = request.getParameter("storeType");
            String abbreviation = request.getParameter("abbreviation");
            String description = request.getParameter("description");
            String address = request.getParameter("address");
            String contactNo = request.getParameter("contactno");
            String faxNo = request.getParameter("faxno");
            boolean ccDateAllow = "1".equals(request.getParameter("ccdateallow")) ? true : false;
            boolean smccDateAllow = "1".equals(request.getParameter("smccallow")) ? true : false;
            String movementTypesStr = request.getParameter("movementType");
            String userIdsStr = request.getParameter("users");
            String executiveIdsStr = request.getParameter("executives");
            String parentId = request.getParameter("parentId");
            String vattinno=(request.getParameter("vatno")!=null?request.getParameter("vatno"):"");
            String csttinno=(request.getParameter("cstno")!=null?request.getParameter("cstno"):"");

            Set<String> movementTypeSet = new HashSet<String>();
            if (!StringUtil.isNullOrEmpty(movementTypesStr)) {
                String[] movementtype = movementTypesStr.split(",");
                movementTypeSet.addAll(Arrays.asList(movementtype));
            }
            Set<User> userSet = new HashSet<User>();
            if (!StringUtil.isNullOrEmpty(userIdsStr)) {
                String[] userIds = userIdsStr.split(",");
                for (String usrId : userIds) {
                    jeresult = accountingHandlerDAO.getObject(User.class.getName(), usrId);
                    User user1 = (User) jeresult.getEntityList().get(0);
                    userSet.add(user1);
                }
            }
            
            Set<User> executiveSet = new HashSet<User>();
            if (!StringUtil.isNullOrEmpty(executiveIdsStr)) {
                String[] executiveIds = executiveIdsStr.split(",");
                for (String executiveId : executiveIds) {
                    jeresult = accountingHandlerDAO.getObject(User.class.getName(), executiveId);
                    User user1 = (User) jeresult.getEntityList().get(0);
                    executiveSet.add(user1);
                }
            }

            int stId = Integer.parseInt(st);
            StoreType storeType = storeService.getStoreType(stId);

            Store store = storeService.getStoreById(id);
                //ERM-691 do not allow editing of a Scrap/Repair store if it has been used in QA transactions
                if ((store.getStoreType() == StoreType.SCRAP || store.getStoreType() == StoreType.REPAIR) && (store.getStoreType().ordinal()!=storeType.ordinal())) {
                    String companyid = store.getCompany().getCompanyID();
                    boolean isForStockRequest=false;
                    List storetransactions = storeService.getTransactionCountForStoreId(id, companyid,isForStockRequest);
                    if (!storetransactions.isEmpty()) {
                        throw new StorageException(messageSource.getMessage("acc.store.cannotedit", new Object[]{"Warning"}, RequestContextUtils.getLocale(request)));
                    }
                }
                
                if (store.getStoreType().ordinal() != storeType.ordinal()) {
                String companyid = store.getCompany().getCompanyID();
                List storetransactions = storeService.getProductCountForStoreId(id, companyid);
                if (!storetransactions.isEmpty()) {
                    throw new StorageException(messageSource.getMessage("acc.store.cannoteditStoreType", new Object[]{"Warning"}, RequestContextUtils.getLocale(request)));
                } else { // Audit Trail data
                auditMessage += "Type : from(" + store.getStoreType().toString() + " to " + storeType.toString() + ")";
            }
            }
            String oldAbbreviation = store.getAbbreviation();
            if (!abbreviation.equals(oldAbbreviation)) {
                if (!StringUtil.isNullOrEmpty(auditMessage)) {
                    auditMessage += ", ";
                }
                auditMessage += "Code : from(" + oldAbbreviation + " to " + abbreviation + ")";
            }
            String oldDescription = store.getDescription();
            if (!description.equals(oldDescription)) {
                if (!StringUtil.isNullOrEmpty(auditMessage)) {
                    auditMessage += ", ";
                }
                auditMessage += "Description : from(" + oldDescription + " to " + description + ")";
            }

            String newAuditAddress = address != null ? address : "";
            String oldAuditAddress = store.getAddress() != null ? store.getAddress() : "";
            if (!newAuditAddress.equals(oldAuditAddress)) {
                if (!StringUtil.isNullOrEmpty(auditMessage)) {
                    auditMessage += ", ";
                }
                auditMessage += "Address : from(" + oldAuditAddress + " to " + newAuditAddress + ")";
            }
            String newAuditContact = contactNo != null ? contactNo : "";
            String oldAuditContact = store.getContactNo() != null ? store.getContactNo() : "";
            if (!newAuditContact.equals(oldAuditContact)) {
                if (!StringUtil.isNullOrEmpty(auditMessage)) {
                    auditMessage += ", ";
                }
                auditMessage += "Contact No : from(" + oldAuditContact + " to " + newAuditContact + ")";
            }
            String newAuditFaxNo = faxNo != null ? faxNo : "";
            String oldAuditFaxNo = store.getFaxNo() != null ? store.getFaxNo() : "";
            if (!newAuditFaxNo.equals(oldAuditFaxNo)) {
                if (!StringUtil.isNullOrEmpty(auditMessage)) {
                    auditMessage += ", ";
                }
                auditMessage += "Fax No : from(" + oldAuditFaxNo + " to " + newAuditFaxNo + ")";
            }

            Set<String> oldMovementtypeSet = store.getMovementTypeSet();
            if (movementTypeSet.size() != oldMovementtypeSet.size() || (movementTypeSet.size() == oldMovementtypeSet.size() && !movementTypeSet.containsAll(oldMovementtypeSet))) {
                String newMovementTypes = "";
                String oldMovementTypes = "";
                for (String movementTypeId : movementTypeSet) {
                    String masterItemName = storeService.getMovementTypeName(movementTypeId);
                    if (StringUtil.isNullOrEmpty(newMovementTypes)) {
                        newMovementTypes = masterItemName;
                    } else {
                        newMovementTypes += ", " + masterItemName;
                    }
                }
                for (String movementTypeId : oldMovementtypeSet) {
                    String masterItemName = storeService.getMovementTypeName(movementTypeId);
                    if (StringUtil.isNullOrEmpty(oldMovementTypes)) {
                        oldMovementTypes = masterItemName;
                    } else {
                        oldMovementTypes += ", " + masterItemName;
                    }
                }
                if (!StringUtil.isNullOrEmpty(auditMessage)) {
                    auditMessage += ", ";
                }
                auditMessage += "Movement Types : from(" + oldMovementTypes + " to " + newMovementTypes + ")";
            }

            Set<User> oldManagerSet = store.getStoreManagerSet();
            if (userSet.size() != oldManagerSet.size() || (userSet.size() == oldManagerSet.size() && !userSet.containsAll(oldManagerSet))) {
                String newStoreManagers = "";
                String oldStoreManagers = "";
                for (User sm : userSet) {
                    if (StringUtil.isNullOrEmpty(newStoreManagers)) {
                        newStoreManagers = sm.getFullName();
                    } else {
                        newStoreManagers += ", " + sm.getFullName();
                    }
                }
                for (User sm : oldManagerSet) {
                    if (StringUtil.isNullOrEmpty(oldStoreManagers)) {
                        oldStoreManagers = sm.getFullName();
                    } else {
                        oldStoreManagers += ", " + sm.getFullName();
                    }
                }
                if (!StringUtil.isNullOrEmpty(auditMessage)) {
                    auditMessage += ", ";
                }
                auditMessage += "Store Managers : from(" + oldStoreManagers + " to " + newStoreManagers + ")";
            }
            Set<User> oldExecutiveSet = store.getStoreExecutiveSet();
            if (executiveSet.size() != oldExecutiveSet.size() || (executiveSet.size() == oldExecutiveSet.size() && !executiveSet.containsAll(oldExecutiveSet))) {
                String newStoreExecutive = "";
                String oldStoreExecutive = "";
                for (User se : executiveSet) {
                    if (StringUtil.isNullOrEmpty(newStoreExecutive)) {
                        newStoreExecutive = se.getFullName();
                    } else {
                        newStoreExecutive += ", " + se.getFullName();
                    }
                }
                for (User se : oldExecutiveSet) {
                    if (StringUtil.isNullOrEmpty(oldStoreExecutive)) {
                        oldStoreExecutive = se.getFullName();
                    } else {
                        oldStoreExecutive += ", " + se.getFullName();
                    }
                }
                if (!StringUtil.isNullOrEmpty(auditMessage)) {
                    auditMessage += ", ";
                }
                auditMessage += "Store Executive : from(" + oldStoreExecutive + " to " + newStoreExecutive + ")";
            }
              
            /* ----------------- Its for only Indian Company Store Creation Fields(VAT No. , CST NO.)---------------------------*/
            String newVATTINnumber = vattinno != null ? vattinno : "";
            String oldVATTINnumber = store.getVATTINnumber() != null ? store.getVATTINnumber() : "";
            if (!newVATTINnumber.equals(oldVATTINnumber)) {
                if (!StringUtil.isNullOrEmpty(auditMessage)) {
                    auditMessage += ", ";
                }
                auditMessage += "VAT Tin No : from(" + oldVATTINnumber + " to " + newVATTINnumber + ")";
            }
            
            String newCSTTINnumber = csttinno != null ? csttinno : "";
            String oldCSTTINnumber = store.getCSTTINnumber() != null ? store.getCSTTINnumber() : "";
            if (!newCSTTINnumber.equals(oldCSTTINnumber)) {
                if (!StringUtil.isNullOrEmpty(auditMessage)) {
                    auditMessage += ", ";
                }
                auditMessage += "CST Tin No : from(" + oldCSTTINnumber + " to " + newCSTTINnumber + ")";
            }
            
            /*-----------------------------------------------------------------------------------------*/
            
            // End audit trail data

            Location defaultLocation = locationService.getDefaultLocation(user.getCompany());
            if (defaultLocation != null && !store.getLocationSet().contains(defaultLocation)) {
                store.getLocationSet().add(defaultLocation);
            }

            store.setStoreType(storeType);
            store.setAbbreviation(abbreviation);
            store.setDescription(description);
            store.setAddress(address);
            store.setContactNo(contactNo);
            store.setFaxNo(faxNo);
            store.setMovementTypeSet(movementTypeSet);
            store.setStoreManagerSet(userSet);
            store.setStoreExecutiveSet(executiveSet);
            store.setCcDateAllow(ccDateAllow);
            store.setSmccAllow(smccDateAllow);
            store.setParentId(parentId);
            store.setVATTINnumber(vattinno);
            store.setCSTTINnumber(csttinno);            
            storeService.updateStore(user, store);

            issuccess = true;
            msg = messageSource.getMessage("acc.inventorysetup.StoreStatus.updated", null, RequestContextUtils.getLocale(request));
            
            auditMessage = "User " + user.getFullName() + " has updated Store: " + oldAbbreviation + " " + auditMessage; //Change Audit message for ERP-33229
            auditTrailObj.insertAuditLog(AuditAction.STORE_MASTER, auditMessage, request, store.getId());

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

    public ModelAndView activateStore(HttpServletRequest request, HttpServletResponse response) {
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
            Store store = storeService.getStoreById(id);

            storeService.activateStore(user, store);

            issuccess = true;
            msg = "Store : " + store.getAbbreviation() + " has been activated successfully";

            auditMessage = "User " + user.getFullName() + " has activated Store: " + store.getAbbreviation();
            auditTrailObj.insertAuditLog(AuditAction.STORE_MASTER, auditMessage, request, store.getId());

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

    public ModelAndView deactivateStore(HttpServletRequest request, HttpServletResponse response) {
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
            Store store = storeService.getStoreById(id);
            double avlQty = storeService.getProductQuantityUnderParticularStore(store.getId(), store.getCompany().getCompanyID());
            double avlQtyPending = storeService.getQuantityPendingUnderParticularStore(store.getId(), store.getCompany().getCompanyID());
            if (avlQty == 0 && avlQtyPending == 0) {
                storeService.deactivateStore(user, store);

                issuccess = true;
                msg = "Store : " + store.getAbbreviation() + " has been deactivated successfully";

                auditMessage = "User " + user.getFullName() + " has deactivated Store: " + store.getAbbreviation();
                auditTrailObj.insertAuditLog(AuditAction.STORE_MASTER, auditMessage, request, store.getId());

                txnManager.commit(status);
            } else {
                issuccess = false;
                if(avlQty != 0 && avlQtyPending != 0){
                    msg = " Stock available in Store And Transaction are in Pending QA / Pending Repair / Pending Approval for '"+store.getAbbreviation()+ "', so you can not deactivate it";
                }
                else if(avlQty != 0){
                    msg = "Stock available in Store  '"+store.getAbbreviation()+ "', so you can not deactivate it";
                }
                else if(avlQtyPending != 0){
                    msg = "Transaction are in Pending QA / Pending Repair / Pending Approval for ' "+store.getAbbreviation()+ " ', so you can not deactivate it";
                }
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

    public ModelAndView getStoreTypeList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        try {
            for (StoreType st : StoreType.values()) {
                JSONObject jObj = new JSONObject();
                jObj.put("id", st.ordinal());
                jObj.put("name", st.toString());
                jArray.put(jObj);
            }
            issuccess = true;
            msg = "Store Types has been fetched successfully";


        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                jobj.put("count", jArray.length());
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getStoreList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isForAvailableWarehouse = !StringUtil.isNullOrEmpty(request.getParameter("isForAvailableWarehouse"))?true:false;
        boolean isRepairStoreOnly = !StringUtil.isNullOrEmpty(request.getParameter("isRepairStoreOnly"))?true:false;
        boolean isScrapStoreOnly = !StringUtil.isNullOrEmpty(request.getParameter("isScrapstoreonly"))?true:false;
        boolean isFromLocationMaster = !StringUtil.isNullOrEmpty(request.getParameter("isFromLocationMaster"))?true:false;
        boolean isFromStoreMaster = !StringUtil.isNullOrEmpty(request.getParameter("isFromStoreMaster"))?true:false;
        boolean isFromInvTransaction = !StringUtil.isNullOrEmpty(request.getParameter("isFromInvTransaction"))?true:false;
        boolean isFromInvSATransaction = !StringUtil.isNullOrEmpty(request.getParameter("isFromInvSATransaction"))?true:false;//This flag will be true only when call is from inventory stock adjustment
        boolean isFromStockIssue = false; //ERM-894
        if(!StringUtil.isNullOrEmpty(request.getParameter("isFromStockIssue"))){
            isFromStockIssue = Boolean.parseBoolean(request.getParameter("isFromStockIssue"));
        }
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User loginUser = (User) jeresult.getEntityList().get(0);
            ExtraCompanyPreferences extraCompanyPreferences = null;
            boolean isFromCompanyPreferences=false;            
            boolean isJobWorkStockOut = !StringUtil.isNullOrEmpty(request.getParameter("isJobWorkStockOut")) ? Boolean.parseBoolean(request.getParameter("isJobWorkStockOut")) : false;
            jeresult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), loginUser.getCompany().getCompanyID());
            extraCompanyPreferences = (ExtraCompanyPreferences) jeresult.getEntityList().get(0);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            String assetdtl = request.getParameter("assetdtl");
            paging = new Paging(start, limit);
            
            String searchString = request.getParameter("ss");

            String storeTypes = request.getParameter("storeTypes"); // comma seperated
            if (!StringUtil.isNullOrEmpty(request.getParameter("isFromCompanyPreferences"))) {
                isFromCompanyPreferences = Boolean.parseBoolean(request.getParameter("isFromCompanyPreferences"));
            }
            //ERM-691 Show Repair Stores or Scrap Stores in the combo hence preparing store type string for specific stores
            if (!(isFromStoreMaster || isFromLocationMaster) && (isRepairStoreOnly || isScrapStoreOnly || isFromInvTransaction || isFromCompanyPreferences || isFromInvSATransaction)) {
                if (isRepairStoreOnly) { //only repair stores
                    storeTypes = String.valueOf(StoreType.REPAIR.ordinal());
                } else if (isScrapStoreOnly) { //only scrap stores
                    storeTypes = String.valueOf(StoreType.SCRAP.ordinal());
                } else if (isScrapStoreOnly && isRepairStoreOnly) { //both repair and scrap stores
                    storeTypes = String.valueOf(StoreType.SCRAP.ordinal()) + "," + String.valueOf(StoreType.REPAIR.ordinal());
                } else if (isFromInvTransaction || isFromCompanyPreferences) { //1.For inventory side transactions do not show Repair / Scrap Stores as well 2.'isFromCompanyPreferences'- Don't load Repair and Scrap store for QA , Job workout and Pick- Pack store in Company Preferences.
                    storeTypes = String.valueOf(StoreType.RETAIL.ordinal()) + "," + String.valueOf(StoreType.WAREHOUSE.ordinal())+ "," + String.valueOf(StoreType.HEADQUARTER.ordinal());
                } else if (isFromInvSATransaction) { //1.For inventory Stock adjustment transactions do not show Repair 
                    storeTypes = String.valueOf(StoreType.RETAIL.ordinal()) + "," + String.valueOf(StoreType.WAREHOUSE.ordinal())+ "," + String.valueOf(StoreType.HEADQUARTER.ordinal() + "," + String.valueOf(StoreType.SCRAP.ordinal()));
                } 
            }
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
            Boolean isActive = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isActive"))) {
                isActive = Boolean.parseBoolean(request.getParameter("isActive"));
            }
            List<Store> storeList;
            boolean includeQAAndRepairStore = false;
            boolean includePickandPackStore=false;            
            /**
             * Include pack Store or not. If true, include pack store else exclude.
             */
            if (!StringUtil.isNullOrEmpty(request.getParameter("includePickandPackStore"))) {
                includePickandPackStore = Boolean.parseBoolean(request.getParameter("includePickandPackStore"));
            }
            
            
            
            /**
             * Include QA and Repair Store or not. If true, include QA and
             * repair store else exclude both.
             */
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("includeQAAndRepairStore"))) {
                includeQAAndRepairStore = Boolean.parseBoolean(request.getParameter("includeQAAndRepairStore"));
            }
          
            if (!StringUtil.isNullOrEmpty(request.getParameter("byStoreManager")) && !StringUtil.isNullOrEmpty(request.getParameter("byStoreExecutive"))) {
                storeList = storeService.getStoresByStoreExecutivesAndManagers(loginUser, isActive, storeTypeList, searchString, paging ,includeQAAndRepairStore,includePickandPackStore);
            } else if (!StringUtil.isNullOrEmpty(request.getParameter("byStoreExecutive"))) {
                storeList = storeService.getStoresByStoreExecutives(loginUser, isActive, storeTypeList, searchString, paging);
            } else if (!StringUtil.isNullOrEmpty(request.getParameter("byStoreManager"))) {
                storeList = storeService.getStoresByStoreManagers(loginUser, isActive, storeTypeList, searchString, paging);
            } else {
                storeList = storeService.getStoresByTypes(loginUser.getCompany(), isActive, storeTypeList, searchString, paging, isForAvailableWarehouse, includeQAAndRepairStore,includePickandPackStore);
            }

            for (Store store : storeList) {
                    /**
                     * If Request come from Job Work Transfer
                     */
                if (!isJobWorkStockOut || (isJobWorkStockOut && extraCompanyPreferences.getVendorjoborderstore().equalsIgnoreCase(store.getId()))) {
                    /**
                     * ERM-894 show all types of warehouses for Goods Issue except scrap/Repair/Job work/Pick Pack.
                     */
                    if (!isFromStockIssue || (isFromStockIssue && !(extraCompanyPreferences.getVendorjoborderstore()!=null && extraCompanyPreferences.getVendorjoborderstore().equalsIgnoreCase(store.getId())))) {
                        JSONObject jObj = storeService.getStoreJson(store);
                        jArray.put(jObj);
                    }
                }
            }
            if (!StringUtil.isNullOrEmpty(assetdtl) && "true".equals(assetdtl)) {
                JSONObject jObjAset = new JSONObject();
                jObjAset.put("store_id", "Customer");
                jObjAset.put("abbr", "Customer");
                jObjAset.put("description", "Customer");
                jObjAset.put("fullname", "Customer");
                jArray.put(jObjAset);
            }
            issuccess = true;
            msg = "Store has been fetched successfully";

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

    public ModelAndView getStoreListByUser(HttpServletRequest request, HttpServletResponse response) {
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
            boolean isFromInvTransaction = !StringUtil.isNullOrEmpty(request.getParameter("isFromInvTransaction"))?true:false;
            String searchString = request.getParameter("ss");

            String storeTypes = request.getParameter("storeTypes"); // comma seperated
            
            if (isFromInvTransaction) {
                storeTypes = String.valueOf(StoreType.RETAIL.ordinal()) + "," + String.valueOf(StoreType.WAREHOUSE.ordinal()) + "," + String.valueOf(StoreType.HEADQUARTER.ordinal());
            }
            
           
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
            Boolean isActive = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isActive"))) {
                isActive = Boolean.parseBoolean(request.getParameter("isActive"));
            }
            boolean excludeQARepair = false;
            boolean includePickandPackStore=false;
            /**
             * Include pack Store or not. If true, include pack store else exclude.
             */
            if (!StringUtil.isNullOrEmpty(request.getParameter("includePickandPackStore"))) {
                includePickandPackStore = Boolean.parseBoolean(request.getParameter("includePickandPackStore"));
            }
            
            if ("true".equalsIgnoreCase(request.getParameter("excludeQARepair"))) {
                excludeQARepair = true;
            }
            List<Store> storeList = storeService.getStoresByUser(userId, isActive, storeTypeList, excludeQARepair, searchString, paging,includePickandPackStore);
            
            for (Store store : storeList) {
                JSONObject jObj = storeService.getStoreJson(store);
                jArray.put(jObj);
            }
            issuccess = true;
            msg = "Store has been fetched successfully";

        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (StorageException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
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

    public ModelAndView getStoreLocations(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STL_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            String storeId = request.getParameter("storeid");
//            String allLocations = request.getParameter("allLoc");
            boolean allLocations=(request.getParameter("allLoc") != null) ? Boolean.parseBoolean(request.getParameter("allLoc")) : false;
            Store store = storeService.getStoreById(storeId);
            if (store != null) {
                Set<Location> locationSet = store.getLocationSet();
                Iterator<Location> itr = locationSet.iterator();
                while (itr.hasNext()) {
                    Location loc = itr.next();
                    if (loc.isActive()) {
                        JSONObject jObj = new JSONObject();
                        jObj.put("id", loc.getId());
                        jObj.put("name", loc.getName());
                        jObj.put("isdefault", loc.isDefaultLocation());
                        jArray.put(jObj);
                    }
                }
            }else if (allLocations) {
                List<String> locList=new ArrayList<>();
                List<Store> storeList = storeService.getStoresByUser(userId, true, null, true, null, paging, true);
                if (storeList != null && !storeList.isEmpty()) {
                    for (Store str : storeList) {
                        Set<Location> locationSet = str.getLocationSet();
                        Iterator<Location> itr = locationSet.iterator();
                        while (itr.hasNext()) {
                            Location loc = itr.next();
                            if (loc.isActive() && !locList.contains(loc.getId())) {
                                locList.add(loc.getId());
                                JSONObject jObj = new JSONObject();
                                jObj.put("id", loc.getId());
                                jObj.put("name", loc.getName());
                                jObj.put("isdefault", loc.isDefaultLocation());
                                jArray.put(jObj);
                            }
                        }
                    }
                }
            }
            issuccess = true;
            msg = "Store locations has been fetched successfully";
            txnManager.commit(status);

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

    public ModelAndView setStoreDefaultLocation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STL_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {

            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String storeId = request.getParameter("storeid");
            String locationId = request.getParameter("locationId");
            if (StringUtil.isNullOrEmpty(storeId) && StringUtil.isNullOrEmpty(locationId)) {
                throw new StorageException("Invalid data");
            }
            Store store = storeService.getStoreById(storeId);

            Location locn = locationService.getLocation(locationId);

            String oldDefaultLocation = store.getDefaultLocation() != null ? store.getDefaultLocation().getName() : "";
            String newDefaultLocation = locn != null ? locn.getName() : "";

            store.setDefaultLocation(locn);

            storeService.updateStore(user, store);

            issuccess = true;
            msg = "Store Default locations has been updated successfully";

            if (!oldDefaultLocation.equals(newDefaultLocation)) {
                auditMessage = "User " + user.getFullName() + " has updated Store: " + store.getAbbreviation() + " - Default Location : from(" + oldDefaultLocation + " to " + newDefaultLocation + ")";
                auditTrailObj.insertAuditLog(AuditAction.STORE_MASTER, auditMessage, request, store.getId());
            }

            txnManager.commit(status);
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

    public ModelAndView importWarehousRecords(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
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

                jobj = importWarehousRecords(request, datajobj);
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

    public JSONObject importWarehousRecords(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
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
                        Store store = null;
                        if (columnConfig.containsKey("Warehouse")) {
                            String WarehouseName = recarr[(Integer) columnConfig.get("Warehouse")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(WarehouseName)) {
                                store = storeService.getStoreByAbbreviation(company, WarehouseName);
                                if (masterPreference.equals("2")) {
                                    store = new Store();
                                    store.setAbbreviation(WarehouseName);
                                } else if (store == null) {
                                    throw new AccountingException("Warehouse is not found for " + WarehouseName);
                                }
                            } else {
                                throw new AccountingException("Warehouse is not available.");
                            }
                        } else {
                            throw new AccountingException("Warehouse column is not found.");
                        }

                        String description = "";
                        if (columnConfig.containsKey("Warehouse Description")) {
                            description = recarr[(Integer) columnConfig.get("Warehouse Description")].replaceAll("\"", "").trim();
                            store.setDescription(description);
                        }

                        String address = "";
                        if (columnConfig.containsKey("Warehouse Address")) {
                            address = recarr[(Integer) columnConfig.get("Warehouse Address")].replaceAll("\"", "").trim();
                            store.setAddress(address);
                        }

                        StoreType storeType = StoreType.WAREHOUSE;
                        if (columnConfig.containsKey("Type Warehouse")) {
                            String warehouseType = recarr[(Integer) columnConfig.get("Type Warehouse")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(warehouseType)) {
                                if ("Retail".equals(warehouseType)) {
                                    storeType = StoreType.RETAIL;
                                } else if ("Headquarter".equals(warehouseType)) {
                                    storeType = StoreType.HEADQUARTER;
                                }
                            }
                        }
                        store.setStoreType(storeType);
                        Set<User> userSet = new HashSet<User>();
                        if (columnConfig.containsKey("Store Managers")) {
                            String storeManagers = recarr[(Integer) columnConfig.get("Store Managers")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(storeManagers)) {
                                String[] userNames = storeManagers.split(";");
                                if (userNames.length > 0) {
                                    userSet = storeService.saveStoreManagerMapping(companyid, userNames);
                                    store.setStoreManagerSet(userSet);
                                } else {
                                    throw new AccountingException("Store Managers not available.");
                                }

                            } else {
                                throw new AccountingException("Store Managers not available ");
                            }
                        } else {
                            throw new AccountingException("Store Managers column is not found.");
                        }

                        Set<User> useExecutiveSet = new HashSet<User>();
                        if (columnConfig.containsKey("Store Executives")) {
                            String storeExecutives = recarr[(Integer) columnConfig.get("Store Executives")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(storeExecutives)) {
                                String[] userNames = storeExecutives.split(";");
                                if (userNames.length > 0) {
                                    useExecutiveSet = storeService.saveStoreManagerMapping(companyid, userNames);
                                    store.setStoreExecutiveSet(useExecutiveSet);
                                } else {
                                    throw new AccountingException("Store Executives not available ");
                                }

                            } else {
                                throw new AccountingException("Store Executives not available.");
                            }
                        } else {
                            throw new AccountingException("Store Executives column is not found.");
                        }
                        String VATTINnumber = "";
                        if (columnConfig.containsKey("VAT TIN Number") && recarr.length > (Integer)columnConfig.get("VAT TIN Number")) { // SDP-13147 - handle if empty value in column
                            VATTINnumber = recarr[(Integer) columnConfig.get("VAT TIN Number")]!=null?recarr[(Integer) columnConfig.get("VAT TIN Number")].replaceAll("\"", "").trim():"";
                            store.setVATTINnumber(VATTINnumber);
                        }
                        String CSTTINnumber = "";
                        if (columnConfig.containsKey("CST TIN Number") && recarr.length > (Integer)columnConfig.get("CST TIN Number")) { // SDP-13147 - handle if empty value in column
                            CSTTINnumber = recarr[(Integer) columnConfig.get("CST TIN Number")]!=null?recarr[(Integer) columnConfig.get("CST TIN Number")].replaceAll("\"", "").trim():"";
                            store.setCSTTINnumber(CSTTINnumber);
                        }
                        store.setCompany(company);
                        storeService.addStore(userLoging, store);
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
                logDataMap.put("Module", Constants.Store_Master);
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
