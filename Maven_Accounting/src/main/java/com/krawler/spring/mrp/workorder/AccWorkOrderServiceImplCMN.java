/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.workorder;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.exception.SeqFormatException;
import com.krawler.inventory.model.inspection.InspectionForm;
import com.krawler.inventory.model.ist.ISTDetail;
import com.krawler.inventory.model.ist.InterStoreTransferRequest;
import com.krawler.inventory.model.ist.InterStoreTransferService;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.sequence.SeqFormat;
import com.krawler.inventory.model.sequence.SeqService;
import com.krawler.inventory.model.stockmovement.*;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.service.AccProductService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.mrp.WorkOrder.*;
import com.krawler.spring.mrp.routingmanagement.AccRoutingManagementService;
import com.krawler.spring.mrp.workcentremanagement.WorkCentre;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.lang.*;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author krawler
 */
public class AccWorkOrderServiceImplCMN implements AccWorkOrderServiceDAOCMN{
    
     private MessageSource messageSource;
    private WorkOrderDAO workOrderDAOObj;
     private AccRoutingManagementService accRoutingManagementService;
     private accCompanyPreferencesDAO accCompanyPreferencesObj;
     private AccWorkOrderServiceDAO accWorkOrderServiceDAOObj;
     private exportMPXDAOImpl exportDaoObj;
     private accProductDAO accProductDaoObj;
     private APICallHandlerService apiCallHandlerService;
     private AccountingHandlerDAO accountingHandlerDAOobj;
     private accAccountDAO accAccountDAOobj;
     private fieldDataManager fieldDataManagercntrl;
     private kwlCommonTablesDAO kwlCommonTablesDAOObj;
     private HibernateTransactionManager txnManager;
     private accJournalEntryDAO accJournalEntryobj;
     private AccCommonTablesDAO accCommonTablesDAO;
     private AccProductService AccProductService;
     private accMasterItemsDAO accMasterItemsDAO;
     private StockMovementService stockMovementService;
     private InterStoreTransferService istService;
     private SeqService seqService;

           
    public void setAccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
     
    public void setExportDaoObj(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }
    
    
    public void setAccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public void setWorkOrderDAOObj(WorkOrderDAO workOrderDAOObj) {
        this.workOrderDAOObj = workOrderDAOObj;
    }

    public void setAccProductDaoObj(accProductDAO accProductDaoObj) {
        this.accProductDaoObj = accProductDaoObj;
    }
    
    public void setAccRoutingManagementService(AccRoutingManagementService accRoutingManagementService) {
        this.accRoutingManagementService = accRoutingManagementService;
    }
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setKwlCommonTablesDAOObj(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }
    
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setAccWorkOrderServiceDAOObj(AccWorkOrderServiceDAO accWorkOrderServiceDAOObj) {
        this.accWorkOrderServiceDAOObj = accWorkOrderServiceDAOObj;
    }
    
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    
    public void setAccProductService(AccProductService AccProductService) {
        this.AccProductService = AccProductService;
    }
    
    public void setAccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAO) {
        this.accMasterItemsDAO = accMasterItemsDAO;
    }
    
    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public InterStoreTransferService getIstService() {
        return istService;
    }

    public void setIstService(InterStoreTransferService istService) {
        this.istService = istService;
    }

    public SeqService getSeqService() {
        return seqService;
    }

    public void setSeqService(SeqService seqService) {
        this.seqService = seqService;
    }
   
    
     /**
      * 
      * @param requestParams = Request Params
      * @return = Response from Remote App
      * @Desc= Send Req to PM for closing Work Order
      * @throws ServiceException
      * @throws IOException
      * @throws com.krawler.utils.json.base.JSONException 
      * @throws AccountingException
      */
     
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject sendWOCloseReq(Map<String, Object> requestParams) throws ServiceException, IOException, JSONException {
        JSONObject jSONObject = new JSONObject();
        try {
            String workorderid = "";
            String projectId = "";
            JSONArray dataJArr = new JSONArray();

            if (requestParams.containsKey("workorderid")) {
                workorderid = (String) requestParams.get("workorderid");
                jSONObject.put("workorderid", workorderid);
            }
            if (requestParams.containsKey("projectid")) {
                projectId = (String) requestParams.get("projectid");
                jSONObject.put("projectid", projectId);
            }
            String userid = "";
            String companyid = "";
            if (requestParams.containsKey("companyid")) {
                companyid = (String) requestParams.get("companyid");
            }
            if (requestParams.containsKey("userid")) {
                userid = (String) requestParams.get("userid");
            }
            dataJArr.put(jSONObject);          
            String accRestURL = URLUtil.buildRestURL("pmURL");
            JSONObject userData = new JSONObject();

            userData.put("iscommit", true);
            userData.put("userid", userid);
            userData.put("companyid", companyid);
            userData.put("inputdata", jSONObject);
            userData.put("changeWOStatus", false); //This parameter is passed to maintain work order status while closing.
            /**
             * update WO status in PM side.
             */
            userData.put(Constants.MRP_WOStatus, Constants.defaultWOstatus_CLOSED);
            String endpoint = accRestURL + "transaction/closeWO";
            JSONObject resObj = apiCallHandlerService.restPostMethod(endpoint, userData.toString());
            jSONObject = new JSONObject();
            if (resObj.getBoolean("success")) {
                /*
                 * Update Work Oder Status
                 */
                boolean consumptionDetailsCheck=false;
                boolean isProjectCompleted = resObj.getBoolean("isProjectCompleted");
                if (isProjectCompleted) {
                    KwlReturnObject worReturnObject = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), workorderid);
                    WorkOrder workOrder = (WorkOrder) worReturnObject.getEntityList().get(0);
                    String invalidProduct="";
                    Set<WorkOrderComponentDetails> workOrderComponentDetailsSet = workOrder.getComponentDetails();
                    for (WorkOrderComponentDetails workOrderComponentDetails : workOrderComponentDetailsSet) {
                        String consumptionDetails = workOrderComponentDetails.getConsumptionDetails();
                        Product product=workOrderComponentDetails.getParentProduct();
                        boolean parentProductCheck=(product != null && workOrder.getProductID() != null ) ?  product.getID().equalsIgnoreCase(workOrder.getProductID().getID()) : false;
                        if (parentProductCheck && !StringUtil.isNullOrEmpty(consumptionDetails) && workOrderComponentDetails.getProduct() != null ) {
                            JSONObject jobj = new JSONObject(consumptionDetails);
                            double actualquantity = 0;
                            double rejectedQuantity = 0;
                            double returnQuantity = 0;
                            
                            if (jobj.has("actualquantity")) {
                                actualquantity = jobj.getDouble("actualquantity");
                            }
                            if (jobj.has("returnQuantity") && !StringUtil.isNullOrEmpty(jobj.optString("returnQuantity"))) {
                                returnQuantity = jobj.getDouble("returnQuantity");
                            }
                            
                            if (jobj.has("rejectedQuantity")) {
                                rejectedQuantity = jobj.getDouble("rejectedQuantity");
                            }
                            if (authHandler.round(workOrderComponentDetails.getBlockQuantity(), companyid) > (authHandler.round(actualquantity + rejectedQuantity + returnQuantity, companyid))) {
                                consumptionDetailsCheck = true;
                                break;
                            }
                        }
                        else if(parentProductCheck && workOrderComponentDetails.getProduct() !=null && workOrderComponentDetails.getProducedQuantity() !=0 && StringUtil.isNullOrEmpty(consumptionDetails))
                        {
                             consumptionDetailsCheck=false;
                        }
                        else if(parentProductCheck && workOrderComponentDetails.getProduct() != null && StringUtil.isNullOrEmpty(consumptionDetails) && !Producttype.SERVICE.equals(workOrderComponentDetails.getProduct().getProducttype().getID())){
                            consumptionDetailsCheck=true;
                            if(StringUtil.isNullOrEmpty(invalidProduct)){
                                invalidProduct=workOrderComponentDetails.getProduct().getProductid();
                            }else{
                                invalidProduct =invalidProduct + "," + workOrderComponentDetails.getProduct().getProductid();
                            }
                        }
                    }
                    if(consumptionDetailsCheck){
                          jSONObject.put("success", false);
                          jSONObject.put("isProjectCompleted", true);
                          jSONObject.put("response_msg", "Consumption Details are not available for Product's <b>" + invalidProduct + "</b>.");
                          
                    } else {
//                        HashMap<String, Object> params = new HashMap<>();
//                        params.put("id", workorderid);
//                        params.put("defaultStatusId", Constants.defaultWOstatus_CLOSED);
//                        params.put("isEdit", true);
//                        JSONObject temJobj = accWorkOrderServiceDAOObj.changeWOStatus(params);
                        jSONObject=calCulateQuantityToBuild(requestParams, workOrder, null);
                        jSONObject.put("isProjectCompleted", isProjectCompleted);
                        //jSONObject.put("success", temJobj.has("isSuccess") ? temJobj.getBoolean("isSuccess") : false);
                        jSONObject.put("success", true);
                    }
                } else {
                        jSONObject.put("success", false);
                    jSONObject.put("isProjectCompleted", isProjectCompleted);
                }    
            } else {
                jSONObject.put("success", false);
                jSONObject.put("isProjectCompleted", false);
            }
//            
//            jSONObject.put("success", true);
//            jSONObject.put("isProjectCompleted", true);
        } catch (Exception e) {
            throw ServiceException.FAILURE("buildProductAssembly : " + e.getMessage(), e);
        }
        return jSONObject;
    }
    
    
//   @Override
//    public JSONObject updateWorkOrderConsumptionDetails(Map<String, Object> requestParams) throws ServiceException, IOException, JSONException {
//        JSONObject jSONObject = new JSONObject();
//        try {
//            String workorderid = "";
//            String companyid = (String) requestParams.get(Constants.companyKey);
//            if (requestParams.containsKey("id")) {
//                workorderid = (String) requestParams.get("id");
//                KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
//                ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
//                KwlReturnObject  mrpcap = accountingHandlerDAOobj.getObject(MRPCompanyPreferences.class.getName(), companyid);
//                MRPCompanyPreferences mRPCompanyPreferences = (MRPCompanyPreferences) mrpcap.getEntityList().get(0);
//                KwlReturnObject worReturnObject = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), workorderid);
//                WorkOrder workOrder = (WorkOrder) worReturnObject.getEntityList().get(0);
//                
//                List<StockMovement> stockMovementsList = new ArrayList<StockMovement>();
//                boolean isUpdateQtyonWOComplete=false;
//                int workOrderQty=0;
//                if (mRPCompanyPreferences != null && mRPCompanyPreferences.getWoInventoryUpdateType() == 1) { // If Option set Update Inventory on Work Order completion
////                    Set<WorkOrderComponentDetails> workOrderComponentDetailsSet = workOrder.getComponentDetails();
////                     for (WorkOrderComponentDetails workOrderComponentDetails : workOrderComponentDetailsSet) {
////                         updateTaskLevelInventory(requestParams, workOrderComponentDetails, stockMovementsList);
////                     }
//                    JSONObject jObj=calCulateQuantityToBuild(requestParams, workOrder, stockMovementsList);
//                    if(jObj.has("workOrderQty")){
//                        workOrderQty=jObj.optInt("workOrderQty", 0);
//                    }
//                    isUpdateQtyonWOComplete=true;
//                 }
//                     
//                    HashMap<String, Object> assemblyParams = new HashMap<>();
//                    assemblyParams.put("carryin", true); //increasing main product Price 
//                    assemblyParams.put("quantity", isUpdateQtyonWOComplete ? workOrderQty : workOrder.getQuantity());
//                    assemblyParams.put("companyid", companyid);
//                    assemblyParams.put("updatedate", new Date()); //Inventory Updated On work order date
//                    assemblyParams.put("product", workOrder.getProductID());
//                    assemblyParams.put("productid", workOrder.getProductID() != null ? workOrder.getProductID().getID() : null);
//                    Inventory inventory =UpdateInventory(assemblyParams);
//                    String batchdetails = "";
//                    
//                    if (requestParams.containsKey("batchDetails")) {
//                        batchdetails = (String) requestParams.get("batchDetails");
//                    }
//                    if (!StringUtil.isNullOrEmpty(batchdetails)) {
//                        saveWorkOrderNewBatch(batchdetails, inventory, requestParams, workOrder,workOrder.getID(), stockMovementsList);
//                    }
//                    
//                    if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateInventoryTab() && !stockMovementsList.isEmpty()) {
//                        stockMovementService.addOrUpdateBulkStockMovement(workOrder.getCompany(), workOrder.getID(), stockMovementsList);
//                    }
//                    
//                    //Updating Status on work order Inventory Consumption
//                    HashMap<String, Object> params = new HashMap<>();
//                    params.put("id", workorderid);
//                    params.put("defaultStatusId", Constants.defaultWOstatus_CLOSED);
//                    params.put("isEdit", true);
//                    jSONObject = accWorkOrderServiceDAOObj.changeWOStatus(params);
//                                  
//            }
//            
//        } catch (Exception e) {
//            throw ServiceException.FAILURE("buildProductAssembly : " + e.getMessage(), e);
//        }
//        return jSONObject;
//    }                
//    
   
       
    @Override
    public JSONObject updateInventoryForFinishedGood(Map<String, Object> requestParams, Map<String, Object> jeDataMap) throws ServiceException, IOException, JSONException {
        JSONObject jSONObject = new JSONObject();
        try {
            String workorderid = "";
            String companyid = (String) requestParams.get(Constants.companyKey);
            String createdby = (String) requestParams.get("createdby");
            String currencyid = (String) requestParams.get("currencyid");
            if (requestParams.containsKey("id")) {
                workorderid = (String) requestParams.get("id");
                KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
                KwlReturnObject  mrpcap = accountingHandlerDAOobj.getObject(MRPCompanyPreferences.class.getName(), companyid);
                MRPCompanyPreferences mRPCompanyPreferences = (MRPCompanyPreferences) mrpcap.getEntityList().get(0);
                KwlReturnObject worReturnObject = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), workorderid);
                WorkOrder workOrder = (WorkOrder) worReturnObject.getEntityList().get(0);
                
                List<StockMovement> stockMovementsList = new ArrayList<StockMovement>();
                boolean isUpdateQtyonWOComplete=false;
                               
                if (mRPCompanyPreferences != null && mRPCompanyPreferences.getWoInventoryUpdateType() == 1) { // If Option set Update Inventory on Work Order completion
                    
                    
                    Set<WorkOrderComponentDetails> workOrderComponentDetailsSet = workOrder.getComponentDetails();
                     for (WorkOrderComponentDetails workOrderComponentDetails : workOrderComponentDetailsSet) {
                         String consumptionDetails = workOrderComponentDetails.getConsumptionDetails();
                           // Updating Produced Quantity Inventory
                         
                          if (!StringUtil.isNullOrEmpty(consumptionDetails) && workOrderComponentDetails.getProduct() != null) {
                                JSONObject jobj = new JSONObject(consumptionDetails);
                                double producedquantity=0.0;
                                if (jobj.has("producedquantity")) {
                                    producedquantity = (Double) jobj.optDouble("producedquantity",0.0);
                                }
                                if (producedquantity > 0) {
                                     HashMap<String, Object> assemblyParams = new HashMap<>();
                                    assemblyParams.put("carryin", true); //increasing main product Price 
                                    assemblyParams.put("quantity", producedquantity);
                                    assemblyParams.put("companyid", companyid);
                                    assemblyParams.put("updatedate", new Date()); //Inventory Updated On work order date
                                    assemblyParams.put("product", workOrderComponentDetails.getProduct());
                                    assemblyParams.put("productid", workOrderComponentDetails.getProduct() != null ? workOrderComponentDetails.getProduct().getID() : null);
                                    Inventory inventory =UpdateInventory(assemblyParams);
                                 
                                    String producedqtydetails = "";
                                    if (jobj.has("producedqtydetails")) {
                                        producedqtydetails = (String) jobj.getString("producedqtydetails");
                                    }
                                    if (!StringUtil.isNullOrEmpty(producedqtydetails)) {
                                        saveProductNewBatch(producedqtydetails, inventory, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
                                    }
                                    
                                    String orderdetailid = "";
                                    if (jobj.has("orderdetailid")) {
                                        orderdetailid = (String) jobj.getString("orderdetailid");
                                    }
                                    
                                    KwlReturnObject detailObject = accountingHandlerDAOobj.getObject(WorkOrderComponentDetails.class.getName(), orderdetailid);
                                    WorkOrderComponentDetails orderComponentDetails = (WorkOrderComponentDetails) detailObject.getEntityList().get(0);
                                    for (WorkOrderComponentDetails details : workOrderComponentDetailsSet) {
                                        if (details.getParentProduct() != null && details.getParentProduct().getID().equalsIgnoreCase(orderComponentDetails.getProduct().getID())) {
                                            String consumption = details.getConsumptionDetails();
                                            if (!StringUtil.isNullOrEmpty(consumption) && details.getProduct() != null) {
                                                JSONObject obj = new JSONObject(consumption);
                                                double actualquantity = 0;
                                                double rejectedQuantity = 0;
                                                if (obj.has("actualquantity")) {
                                                    actualquantity = obj.getDouble("actualquantity");
                                                }
                                                if (obj.has("rejectedQuantity")) {
                                                    rejectedQuantity = obj.getDouble("rejectedQuantity");
                                                }

                                                assemblyParams = new HashMap<>();
                                                assemblyParams.put("carryin", false); //reducing the ingradient price
                                                assemblyParams.put("quantity", (actualquantity));//+ rejectedQuantity
                                                assemblyParams.put("companyid", companyid);
                                                assemblyParams.put("updatedate", new Date()); //Inventory Updated On work order date
                                                assemblyParams.put("product", workOrderComponentDetails.getProduct());
                                                assemblyParams.put("productid", workOrderComponentDetails.getProduct() != null ? workOrderComponentDetails.getProduct().getID() : null);
                                                inventory = UpdateInventory(assemblyParams);
                                                String batchdetails = "";
                                                if (obj.has("batchdetails")) {
                                                    batchdetails = (String) obj.getString("batchdetails");
                                                }
                                                if (!StringUtil.isNullOrEmpty(batchdetails)) {
                                                    requestParams.put("actualProducedQty", producedquantity);
                                                    saveNewAssemblyBatch(batchdetails, inventory, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
                                                }
                                                
                                                if (details.getBlockQuantity() > 0) {
                                                    double blockquantityused = 0;
                                                    if (actualquantity > details.getBlockQuantity()) {
                                                        blockquantityused = details.getBlockQuantity();
                                                    } else {
                                                        blockquantityused = actualquantity;
                                                    }

                                                    details.setBlockQuantityUsed(blockquantityused);
                                                    details.setBlockQtyUsed(true);
                                                }
                                            }

                                        }

                                    }
                                }
//                                // Code to Update Consumption Details for Leaf Nodes 
//                                  int level=0;
//                                  if (jobj.has("level")) {
//                                      level = jobj.getInt("level");
//                                  }
//                                  
//                                  if(level > 0){ 
//                                        double actualquantity = 0;
//                                        double rejectedQuantity = 0;
//                                        if (jobj.has("actualquantity")) {
//                                            actualquantity = jobj.getDouble("actualquantity");
//                                        }
//                                        if (jobj.has("rejectedQuantity")) {
//                                            rejectedQuantity = jobj.getDouble("rejectedQuantity");
//                                        }
//                                        HashMap<String, Object> assemblyParams = new HashMap<>();
//                                        assemblyParams.put("carryin", false); //reducing the ingradient price
//                                        assemblyParams.put("quantity", (actualquantity ));//+ rejectedQuantity
//                                        assemblyParams.put("companyid", companyid);
//                                        assemblyParams.put("updatedate", new Date()); //Inventory Updated On work order date
//                                        assemblyParams.put("product", workOrderComponentDetails.getProduct());
//                                        assemblyParams.put("productid", workOrderComponentDetails.getProduct() != null ? workOrderComponentDetails.getProduct().getID() : null);
//                                        Inventory inventory = UpdateInventory(assemblyParams);
//                                        String batchdetails = "";
//                                        if (jobj.has("batchdetails")) {
//                                            batchdetails = (String) jobj.getString("batchdetails");
//                                        }
//                                        if (!StringUtil.isNullOrEmpty(batchdetails)) {
//                                            saveNewAssemblyBatch(batchdetails, inventory, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
//                                        }
//                                  }
                                  
                            }
                          
                        }
                     
                     
                     //Update Sub assemble Product Inventory
                     
                     String assemblygridJson= (String) requestParams.get("assemblygridJson");
                     JSONArray jArr = new JSONArray(assemblygridJson);
                      
                    for (int i = 0; i < jArr.length(); i++) {
                        String orderdetailid="";
                        double quantity=0;
                        String batchdetails="";
                        int componentType=0;
                        double crate=0;
                        JSONObject jobj = new JSONObject(jArr.get(i).toString());
                        
                        if (jobj.has("orderdetailid") &&  !jobj.getString("orderdetailid").isEmpty()) {
                            orderdetailid = jobj.getString("orderdetailid");
                        }
                        
                        if (jobj.has("batchdetails") &&  !jobj.getString("batchdetails").isEmpty()) {
                            batchdetails = jobj.getString("batchdetails");
                        }
                        
                        if (jobj.has("quantity") &&  !jobj.getString("quantity").isEmpty()) {
                            quantity = jobj.getDouble("quantity");
                        }
                        
                        if(jobj.has("componentType") && !jobj.getString("componentType").isEmpty())
                        {
                                componentType=jobj.optInt("componentType");
                        }
                        
                        if(jobj.has("crate") && !jobj.getString("crate").isEmpty())
                        {
                                crate=jobj.optDouble("crate");
                        }
                        
                        KwlReturnObject detailObject = accountingHandlerDAOobj.getObject(WorkOrderComponentDetails.class.getName(), orderdetailid);
                        WorkOrderComponentDetails orderComponentDetails = (WorkOrderComponentDetails) detailObject.getEntityList().get(0);
                        
                        if(orderComponentDetails != null && quantity > 0 ){
                                double blockquantityused = 0;
                            if (componentType == 2 || componentType == 3) {
                           
                            
                                orderComponentDetails.setBlockQuantityUsed(0);
                                orderComponentDetails.setBlockQtyUsed(false);
                            
                            } else {
                                if (quantity > orderComponentDetails.getBlockQuantity()) {
                                    blockquantityused = orderComponentDetails.getBlockQuantity();
                                } else {
                                    blockquantityused = quantity;
                                }
                            
                                orderComponentDetails.setBlockQuantityUsed(blockquantityused);
                                orderComponentDetails.setBlockQtyUsed(true);
                            
                            }   
                            HashMap<String, Object> assemblyParams = new HashMap<>();
                           
                            if (componentType == 2 || componentType == 3) {
                                JSONArray jArr1 = new JSONArray(batchdetails);
                                JSONObject jSONObject1 = new JSONObject(jArr1.get(0).toString());
                                jSONObject1.put("ComponentType", componentType);
                                double comproquantity = jSONObject1.getDouble("quantity");
                                batchdetails = jSONObject1.toString();
                                batchdetails = '[' + batchdetails + ']';
                                assemblyParams.put("carryin", true); //increase the ingradient price
                                assemblyParams.put("quantity", comproquantity); //Inventory Will increase 
                                
                            } else {
                                
                                assemblyParams.put("carryin", false); //reducing the ingradient price
                                assemblyParams.put("quantity", quantity); //Inventory Will consume Used and rejected qty
                            }
                                    
                            assemblyParams.put("companyid", companyid);
                            assemblyParams.put("updatedate", new Date()); //Inventory Updated On work order date
                            assemblyParams.put("product", orderComponentDetails.getProduct());
                            assemblyParams.put("productid", orderComponentDetails.getProduct() != null ? orderComponentDetails.getProduct().getID() : null);
                            Inventory inventory = UpdateInventory(assemblyParams);
                           
                            if (!StringUtil.isNullOrEmpty(batchdetails)) {

                                if (componentType == 2 || componentType == 3) {
                                    saveProductNewBatch(batchdetails, inventory, requestParams, orderComponentDetails.getWorkOrder(), orderComponentDetails.getID(), stockMovementsList);
                                
                                } else {
                                    saveNewAssemblyBatch(batchdetails, inventory, requestParams, orderComponentDetails.getWorkOrder(), orderComponentDetails.getID(), stockMovementsList);

                                }
                            }
                        }
                    }
                   
                } 
                     
                   //Update Inventory for Finished Good 
                double workOrderQty = 0;
                if (requestParams.containsKey("finalQuantity") && requestParams.get("finalQuantity") != null) {
                    workOrderQty = Double.parseDouble((String) requestParams.get("finalQuantity"));
                }
                String fgproductbatchDetails = "";
                if (requestParams.containsKey("fgproductbatchDetails")) {
                    fgproductbatchDetails = (String) requestParams.get("fgproductbatchDetails");
                }
                
                
                /**
                 * If Final finished product is selected at task level then it get produce at task level, not need to produce it at work order close
                 * If Final finished product is not selected at task level then it not get produce at task level, so need to produce it at work order close.
                 */
                double FinalProductWODProduceQuantiy = 0.0;
                boolean FinalProductWODIsblockquantiyUsed = false;
                String FinalProductWODConsumptionDetails = "";

                KwlReturnObject result = null;
                HashMap<String, Object> WODParameter = new HashMap<>();
                WODParameter.put("companyid", companyid);
                WODParameter.put("productid", workOrder.getProductID() != null ? workOrder.getProductID().getID() : null);
                WODParameter.put("woid", workorderid);
                result = workOrderDAOObj.getWODetailfromProductandWO(WODParameter);
                
                if (result.getEntityList() != null) {
                    List<WorkOrderComponentDetails> WODList = result.getEntityList();
                    for (WorkOrderComponentDetails WOD : WODList) {

                        FinalProductWODProduceQuantiy = WOD.getProducedQuantity();
                        FinalProductWODIsblockquantiyUsed = WOD.isBlockQtyUsed();
                        FinalProductWODConsumptionDetails = WOD.getConsumptionDetails()!=null? WOD.getConsumptionDetails():"";

                        if (FinalProductWODProduceQuantiy == 0 && FinalProductWODIsblockquantiyUsed == false && StringUtil.isNullOrEmpty(FinalProductWODConsumptionDetails)) {

                            HashMap<String, Object> assemblyParams = new HashMap<>();
                            assemblyParams.put("carryin", true); //increasing main product Price 
                            assemblyParams.put("quantity", workOrderQty);
                            assemblyParams.put("companyid", companyid);
                            assemblyParams.put("updatedate", new Date()); //Inventory Updated On work order date
                            assemblyParams.put("product", workOrder.getProductID());
                            assemblyParams.put("productid", workOrder.getProductID() != null ? workOrder.getProductID().getID() : null);
                            Inventory inventory = UpdateInventory(assemblyParams);
                            if (!StringUtil.isNullOrEmpty(fgproductbatchDetails)) {
                                requestParams.put("isProduceQuantity", true);
                                requestParams.put("remark", WorkOrder.ADDED_THROUGH_PRODUCTION);
                                requestParams.put("isFinalProductProducedOnWorkOrderClose",true);
                                requestParams.put("FinalProductTotalProducedQuantity",workOrderQty);
                                saveProductNewBatch(fgproductbatchDetails, inventory, requestParams, workOrder, WOD.getID(), stockMovementsList);
                                requestParams.remove("FinalProductTotalProducedQuantity");
                                requestParams.remove("isFinalProductProducedOnWorkOrderClose");
                                requestParams.remove("remark");
                                requestParams.remove("isProduceQuantity");                          
   
                            }
                        }

                    }
                }

                if (!StringUtil.isNullOrEmpty(fgproductbatchDetails)) {
                    workOrder.setProducedquantity(workOrderQty);
                }
  
//                    if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateInventoryTab() && !stockMovementsList.isEmpty()) {
//                        stockMovementService.addOrUpdateBulkStockMovement(workOrder.getCompany(), workOrder.getID(), stockMovementsList);
//                    }
                    
                    
                    /*********/
                  JSONObject jo=postCloseWorkOrderJE(requestParams,workOrder);
                   String productIds="";
                   String jeid="";
                   String assemblyJedid="";
                  if(jo.has("productIds"))
                  {
                   productIds=jo.getString("productIds");
                  } 
                  if(jo.has("jeid"))
                  {
                  
                  jeid=jo.getString("jeid");
                  }
                  if(jo.has("assemblyJedid"))
                  {
                  
                  assemblyJedid=jo.getString("assemblyJedid");
                  }
                  /*********/
                    
                    //Updating Status on work order Inventory Consumption
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("id", workorderid);
                    params.put("companyId", companyid);
                    params.put("defaultStatusId", Constants.defaultWOstatus_CLOSED);
                    params.put("isEdit", true);
                   // Save journalentry object in workorder table
                    if (!StringUtil.isNullOrEmpty(jeid)) { // If Option set Update Inventory on Work Order completion
                        params.put("closewoje", jeid);
                    }
                    if (!StringUtil.isNullOrEmpty(assemblyJedid)) { // If Option set Update Inventory on Work Order completion
                        params.put("assemblyJedid", assemblyJedid);
                    }
                    jSONObject = accWorkOrderServiceDAOObj.changeWOStatus(params);
                    
                   /*
                    * The parameters is passed to PM side with rest call to update work order status - as CloseWO
                    */
                    String userid = "";     
                    jSONObject.put("projectid", workOrder.getProjectId());
                    
                    if (requestParams.containsKey("userid")) {
                        userid = (String) requestParams.get("userid");
                    }
                   
                    String accRestURL = URLUtil.buildRestURL("pmURL");
                    JSONObject userData = new JSONObject();
                    userData.put("iscommit", true);
                    userData.put("userid", userid);
                    userData.put("companyid", companyid);
                    userData.put("inputdata", jSONObject);
                    userData.put("changeWOStatus", true);
                    userData.put(Constants.MRP_WOStatus, Constants.defaultWOstatus_CLOSED);
                    String endpoint = accRestURL + "transaction/closeWO";
                    JSONObject resObj = apiCallHandlerService.restPostMethod(endpoint, userData.toString());

                    jSONObject.put("productIds",productIds);
                        }

        } catch (Exception e) {
            throw ServiceException.FAILURE("buildProductAssembly : " + e.getMessage(), e);
        }
        return jSONObject;
    }  
   
    public JSONObject postCloseWorkOrderJE(Map<String, Object> requestParams, WorkOrder workOrder) throws ServiceException, IOException, JSONException {
        JSONObject jSONObject = new JSONObject();
   
        try {
            
            String companyid = (String) requestParams.get(Constants.companyKey);
            String createdby = (String) requestParams.get("createdby");
            String currencyid = (String) requestParams.get("currencyid");
            String productids="";
            String WOProductid="";
            JournalEntry journalEntry = null;
            JSONArray jarr = new JSONArray();
            HashSet jeDetails = new HashSet();
            String jeid = "";
            double inventoryassemblyamount = 0.0d;
            double inventorypartCoproductScraptotalammount = 0.0d;
            
            // Create Journal Entry Number for new case
            String jeentryNumber = "";
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            boolean jeautogenflag = false;
            synchronized (this) {
                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                JEFormatParams.put("companyid", companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, new Date());
                jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                jeSeqFormatId = format.getID();
                jeautogenflag = true;
            }

            Map<String, Object> jeDataMap = new HashMap();;
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
            jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
            jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
            jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
            jeDataMap.put("entrydate", new Date());
            jeDataMap.put("companyid", companyid);
//            jeDataMap.put("memo", "Close Work Order JE for "+workOrder.getWorkOrderName());
            jeDataMap.put("createdby", createdby);
            jeDataMap.put("currencyid", currencyid);
            jeDataMap.put("transactionModuleid", Constants.MRP_WORK_ORDER_MODULEID);
            jeDataMap.put("transactionId", workOrder.getID());            
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            
            
            
            
             String assemblygridJson= (String) requestParams.get("assemblygridJson");
             JSONArray jArr = new JSONArray(assemblygridJson);
            
              
              for (int i = 0; i < jArr.length(); i++) {
                        String orderdetailid="";
                        double quantity=0;
                        String batchdetails="";
                        JSONObject jobj = new JSONObject(jArr.get(i).toString());
                        
                        if (jobj.has("orderdetailid") &&  !jobj.getString("orderdetailid").isEmpty()) {
                            orderdetailid = jobj.getString("orderdetailid");
                        }
                        
                        if (jobj.has("batchdetails") &&  !jobj.getString("batchdetails").isEmpty()) {
                            batchdetails = jobj.getString("batchdetails");
                        }
                        
                        if (jobj.has("quantity") &&  !jobj.getString("quantity").isEmpty()) {
                            quantity = jobj.getDouble("quantity");
                        }
                        
                        KwlReturnObject detailObject = accountingHandlerDAOobj.getObject(WorkOrderComponentDetails.class.getName(), orderdetailid);
                        WorkOrderComponentDetails orderComponentDetails = (WorkOrderComponentDetails) detailObject.getEntityList().get(0);
                        
             
              
             if(orderComponentDetails != null && quantity > 0 ){
            
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jeDetails.size() + 1);
            jedjson.put("companyid", companyid);
            HashMap<String, Object> requestParamsPrice = new HashMap();
            requestParamsPrice.put("productid", orderComponentDetails.getProduct().getID());
            requestParamsPrice.put("carryin", true);//Purchase
            requestParamsPrice.put("currencyid", currencyid);
            requestParamsPrice.put("companyid", companyid);
            requestParamsPrice.put("uomid", orderComponentDetails.getProduct().getUnitOfMeasure().getID());
            KwlReturnObject priceListResult = accProductDaoObj.getPriceListEntry(requestParamsPrice);
           
            if(StringUtil.isNullOrEmpty(orderComponentDetails.getConsumptionDetails()) && orderComponentDetails.getProducedQuantity()!=0)
            {
                double inventorypartamount = 0.0d;
                inventorypartamount=orderComponentDetails.getInitialPurchasePrice();
                inventorypartCoproductScraptotalammount+=inventorypartamount;
               jedjson.put("amount", inventorypartamount);
               jedjson.put("debit", true);
            }
            else
            {
            String productid=orderComponentDetails.getProduct().getID();
            productids = (productids.equals("") ? productid : productids + "," + productid);        
            double inventorypartamount = 0.0d;
            inventorypartamount = authHandler.round(orderComponentDetails.getInitialPurchasePrice() * orderComponentDetails.getBlockQuantityUsed(), companyid); 
            inventoryassemblyamount += inventorypartamount;
            jedjson.put("amount", inventorypartamount);
            jedjson.put("debit", false);
            }
           
            if(Producttype.SERVICE.equals(orderComponentDetails.getProduct().getProducttype().getID())){
                jedjson.put("accountid", orderComponentDetails.getProduct().getPurchaseAccount().getID());
            }else{
                jedjson.put("accountid", orderComponentDetails.getProduct().getInventoryAccount().getID());
            }
            
            jedjson.put("jeid", jeid);
            jedjson.put("orderdetailid",orderdetailid);

            jarr.put(jedjson);
            
              }
              }
            
          //  if (mRPCompanyPreferences != null && mRPCompanyPreferences.getWoInventoryUpdateType() == 1) { // If Option set Update Inventory on Work Order completion
                // Assembly product inventory account
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", jeDetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", inventoryassemblyamount-inventorypartCoproductScraptotalammount);
                jedjson.put("accountid", workOrder.getProductID().getInventoryAccount().getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                String assemblyJedid=jed!=null?jed.getID():"";
                jeDetails.add(jed);
                
                //Save in workordercomponenetdetails table -> JED
              
                for(int i=0 ; i< jarr.length() ; i++){
                    JSONObject jobj = (JSONObject)jarr.get(i);
                    jobj.remove("srno");
                    jobj.put("srno", jeDetails.size() + 1);
                    
                    KwlReturnObject jedresult1 = accJournalEntryobj.addJournalEntryDetails(jobj);
                    JournalEntryDetail jed1 = (JournalEntryDetail) jedresult1.getEntityList().get(0);
                    String Jedid=jed1!=null?jed1.getID():"";
                    jeDetails.add(jed1);  
                    if(jobj.has("orderdetailid") && !jobj.getString("orderdetailid").isEmpty())
                    {
                            String orderdetailid=jobj.getString("orderdetailid");
                            KwlReturnObject detailObject = accountingHandlerDAOobj.getObject(WorkOrderComponentDetails.class.getName(), orderdetailid);
                            WorkOrderComponentDetails orderComponentDetails = (WorkOrderComponentDetails) detailObject.getEntityList().get(0);
                            
                            KwlReturnObject JEDObject=accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), Jedid);
                            JournalEntryDetail journalEntryDetail=(JournalEntryDetail) JEDObject.getEntityList().get(0);
                            orderComponentDetails.setInventoryJEdetail(journalEntryDetail);
                    }
                    
                    
                                              
                }
//                WOProductid=workOrder.getProductID()!=null?workOrder.getProductID().getID():"";
//                productids = (productids.equals("") ? WOProductid : productids + "," + WOProductid);
                jSONObject.put("productIds",productids);
                jSONObject.put("jeid", jeid);
                jSONObject.put("assemblyJedid",assemblyJedid);
          //  }
        } catch (Exception e) {
            throw ServiceException.FAILURE("WorkOrde Journal Entry : " + e.getMessage(), e);
        }
        return jSONObject;
    }
   
    @Override
    public void updateTaskLevelInventory(Map<String, Object> requestParams, WorkOrderComponentDetails workOrderComponentDetails, List<StockMovement> stockMovementsList) throws ServiceException {
        try {
            boolean isEditConsumption = false;
            int ComponentType=0;
            ComponentType=(Integer) requestParams.get("ComponentType");
            isEditConsumption = Boolean.parseBoolean(requestParams.get("isEditConsumption").toString());
            int producedQuantityConsumed=0;
            double comproquantity = 0;
            double recycleQuantityFractionalPart=0.0d;
            boolean isProducedProductIngredientOfFinalAssemblyProduct=false;
            String companyid = (String) requestParams.get(Constants.companyKey);
            /* Not sure to deleteworkorderbatchserialdetails when product componenttype is co-product or scrap*/
            if (isEditConsumption) {
                workOrderDAOObj.deleteWorkOrderBatchSerialDetails(requestParams);
                /**
                 * delete previous SM entries for multiple MQ.
                 */
                stockMovementService.removeStockMovementByReferenceIdForWorkOrder(workOrderComponentDetails.getWorkOrder().getCompany(), workOrderComponentDetails.getID());
            }
            String consumptionDetails = workOrderComponentDetails.getConsumptionDetails();
            String blockDetails = workOrderComponentDetails.getBlockDetails();
            JSONArray blockDetailsJSONArray = new JSONArray();
            if(!StringUtil.isNullOrEmpty(blockDetails)){
                blockDetailsJSONArray = new JSONArray(blockDetails);
            }
            if (!StringUtil.isNullOrEmpty(consumptionDetails) && workOrderComponentDetails.getProduct() != null) {
                JSONObject jobj = new JSONObject(consumptionDetails);
                double actualquantity = 0;
                double rejectedQuantity = 0;
                if (jobj.has("actualquantity")) {
                    actualquantity = jobj.getDouble("actualquantity");
                }
                if (jobj.has("rejectedQuantity")) {
                    rejectedQuantity = jobj.getDouble("rejectedQuantity");
                }
                HashMap<String, Object> assemblyParams = new HashMap<>();
                
                // Updating Produced Deatils 
                String producedqtydetails = "";
                if (jobj.has("producedqtydetails")) {
                    producedqtydetails = (String) jobj.getString("producedqtydetails");
                }
                
                String batchdetails = "";
                if (jobj.has("batchdetails")) {
                    batchdetails = (String) jobj.getString("batchdetails");
                }
                /**
                 * delete previously produced batch.
                 */
                if (StringUtil.isNullOrEmpty(batchdetails) && !StringUtil.isNullOrEmpty(producedqtydetails) && isEditConsumption) {
                    String oldProduceQtyDetail = (String) requestParams.get(Constants.oldProduceQtyDetail);
                    if (!StringUtil.isNullOrEmpty(oldProduceQtyDetail)) {
                        try {
                            JSONArray jArr = new JSONArray(oldProduceQtyDetail);
                            for (int i = 0; i < jArr.length(); i++) {
                                JSONObject jSONObject = (JSONObject) jArr.get(i);
                                String productID = workOrderComponentDetails.getInventoryProduced() != null ? workOrderComponentDetails.getInventoryProduced().getProduct().getID() : "";
                                String productBatchId = accCommonTablesDAO.getpurchaseBatchIdForLocationWarehouseRowRackBin(productID, jSONObject.getString("location"), jSONObject.getString("warehouse"), jSONObject.optString("row", null), jSONObject.optString("rack", null), jSONObject.optString("bin", null), jSONObject.optString("batch", null));
                                accCommonTablesDAO.deleteBatches(productBatchId, companyid);
                            }
                        } catch (JSONException ex) {
                            System.out.println(ex);
                        }
                    }
                }

                /**
                 * Code implemented to handle co-product and scraps quantity in
                 * multiple locations.
                 */
                 boolean isActualQuntity = false;
                if (ComponentType == 2 || ComponentType == 3) {
                    JSONArray jArr = new JSONArray(batchdetails);
                    JSONArray jArrCoScrap = new JSONArray();                    
                    for (int coscrapindex = 0; coscrapindex < jArr.length(); coscrapindex++) {

                        JSONObject jSONObject = new JSONObject(jArr.get(coscrapindex).toString());
                        jSONObject.put("ComponentType", ComponentType);
                        comproquantity = comproquantity + jSONObject.getDouble("quantity");

                        jArrCoScrap.put(jSONObject);
                    }
                    
                    batchdetails = jArrCoScrap.toString();
//                    batchdetails = '[' + batchdetails + ']';
                    isActualQuntity = true;
                    assemblyParams.put("carryin", true);

                    /*
                     * Increase the ingradient price for co-product or scrap
                     * product
                     */
                    assemblyParams.put("quantity", comproquantity);

                } else {
                    if(!StringUtil.isNullOrEmpty(producedqtydetails)){//Produce product
                        isActualQuntity = true;
                        assemblyParams.put("carryin", true); //product assembly product
                        double producedQuantity = 0.0d;
                        JSONArray jArr = new JSONArray(producedqtydetails);
                        for (int i = 0; i < jArr.length(); i++) {
                            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
                            if(jSONObject.has("quantity") && !StringUtil.isNullOrEmpty(jSONObject.getString("quantity"))){
                                producedQuantity += Double.parseDouble(jSONObject.getString("quantity").toString());
                            }
                        }
                        assemblyParams.put("quantity", producedQuantity);
                    }
                    if(!StringUtil.isNullOrEmpty(batchdetails)){//Consume product
                        isActualQuntity = true;
                        assemblyParams.put("carryin", false); //reducing the ingradient price
                        assemblyParams.put("quantity", actualquantity);
                    }
                    
                    if(!StringUtil.isNullOrEmpty(producedqtydetails) && !StringUtil.isNullOrEmpty(batchdetails))
                    {
                        isActualQuntity= true;
                        assemblyParams.put("carryin", false); //reducing the ingradient price
                        assemblyParams.put("quantity", (actualquantity));
                        
                    }
                    
                }
                assemblyParams.put("companyid", companyid);
                assemblyParams.put("updatedate", new Date()); //Inventory Updated On work order date
                assemblyParams.put("product", workOrderComponentDetails.getProduct());
                assemblyParams.put("productid", workOrderComponentDetails.getProduct() != null ? workOrderComponentDetails.getProduct().getID() : null);
                String inventoryid = "";
                if (StringUtil.isNullOrEmpty(batchdetails) && !StringUtil.isNullOrEmpty(producedqtydetails)) {
                    inventoryid = workOrderComponentDetails.getInventoryProduced() != null ? workOrderComponentDetails.getInventoryProduced().getID() : "";
                } else {
                    inventoryid = workOrderComponentDetails.getInventory() != null ? workOrderComponentDetails.getInventory().getID() : "";
                }
                Inventory inventory=null;
                /* If isActualQuntity is true then this part will execute(In case actual quantity = 0 then isActualQuntity is false) */
                if(isActualQuntity){
                    assemblyParams.put("inventoryid", inventoryid);
                    inventory = UpdateInventory(assemblyParams);
                /** 
                 * Now Final Finished product is selected at Task level 
                 * Now we have window to consume ingredient of final assembly product of work order
                 * Need to remove logic of If produced product is ingredient of final assembly product of work order, then consume it.
                 */
               
                //Start 031117
//                Inventory inventoryConsumeProduced = null;
//                if(!StringUtil.isNullOrEmpty(producedqtydetails)){
//                    if(workOrderComponentDetails.getParentProduct().getID().equals(workOrderComponentDetails.getWorkOrder().getProductID().getID())){
//                        //If produced product is ingredient of final assembly product of work order, then consume it
//                        assemblyParams.put("carryin", false);
//
//                        double producedQuantity = 0.0d;                      
//                        JSONArray jArr = new JSONArray(producedqtydetails);
//                        for (int i = 0; i < jArr.length(); i++) {
//                            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
//                            if(jSONObject.has("quantity") && !StringUtil.isNullOrEmpty(jSONObject.getString("quantity"))){
//                                producedQuantity = Double.parseDouble(jSONObject.getString("quantity").toString());
//                            }
//                        }
//                        producedQuantityConsumed=(int)producedQuantity;
//                        recycleQuantityFractionalPart=(double) (producedQuantity-producedQuantityConsumed);
//                        assemblyParams.put("quantity", producedQuantityConsumed);
//                        
//                        inventoryConsumeProduced = UpdateInventory(assemblyParams);
//                        isProducedProductIngredientOfFinalAssemblyProduct=true;
//                    }
//                }
                //End 031117
                
                    Map<String, String> blockDetailsBatchMap = new HashMap();
                    Map<String, String> actualConsumptionBatchMap = new HashMap();
                    if (!StringUtil.isNullOrEmpty(batchdetails)) {
                        JSONArray actualCusumptionJSONArray = new JSONArray(batchdetails);

                        //From where quantiy was blocked
                        for (int i = 0; i < blockDetailsJSONArray.length(); i++) {
                            JSONObject blockDetailsJSONObject = new JSONObject(blockDetailsJSONArray.get(i).toString());
                            if (blockDetailsJSONObject.has("documentbatchid") && !StringUtil.isNullOrEmpty(blockDetailsJSONObject.getString("documentbatchid"))) {
                                blockDetailsBatchMap.put(blockDetailsJSONObject.getString("documentbatchid"), blockDetailsJSONObject.getString("quantity"));
                            } else if(blockDetailsJSONObject.has("warehouse") && !StringUtil.isNullOrEmpty(blockDetailsJSONObject.getString("warehouse")) && blockDetailsJSONObject.has("location") && !StringUtil.isNullOrEmpty(blockDetailsJSONObject.getString("location"))){
                                //ERP-37086 - If Quantity is blocked from Location and Warehouse Only.
                                String newprodbatchid = accCommonTablesDAO.getpurchaseBatchIdForLocationWarehouseRowRackBin(workOrderComponentDetails.getProduct().getID(), blockDetailsJSONObject.optString("location", null), blockDetailsJSONObject.optString("warehouse", null), blockDetailsJSONObject.optString("row", null), blockDetailsJSONObject.optString("rack", null), blockDetailsJSONObject.optString("bin", null), "");
                                if (!StringUtil.isNullOrEmpty(newprodbatchid)) {
                                    blockDetailsBatchMap.put(newprodbatchid, blockDetailsJSONObject.getString("quantity"));
                                }
                            } else if(blockDetailsJSONObject.has("warehouse") && !StringUtil.isNullOrEmpty(blockDetailsJSONObject.getString("warehouse"))) {
                                //ERP-37086 - If Quantity is blocked from Warehouse Only.
                                String newprodbatchid = accCommonTablesDAO.getpurchaseBatchIdForLocationWarehouseRowRackBin(workOrderComponentDetails.getProduct().getID(), null, blockDetailsJSONObject.optString("warehouse", null), blockDetailsJSONObject.optString("row", null), blockDetailsJSONObject.optString("rack", null), blockDetailsJSONObject.optString("bin", null), "");
                                if (!StringUtil.isNullOrEmpty(newprodbatchid)) {
                                    blockDetailsBatchMap.put(newprodbatchid, blockDetailsJSONObject.getString("quantity"));
                                }
                            } else if(blockDetailsJSONObject.has("location") && !StringUtil.isNullOrEmpty(blockDetailsJSONObject.getString("location"))) {
                                //ERP-37086 - If Quantity is blocked from Location Only.
                                String newprodbatchid = accCommonTablesDAO.getpurchaseBatchIdForLocationWarehouseRowRackBin(workOrderComponentDetails.getProduct().getID(), blockDetailsJSONObject.optString("location", null), null, blockDetailsJSONObject.optString("row", null), blockDetailsJSONObject.optString("rack", null), blockDetailsJSONObject.optString("bin", null), "");
                                if (!StringUtil.isNullOrEmpty(newprodbatchid)) {
                                    blockDetailsBatchMap.put(newprodbatchid, blockDetailsJSONObject.getString("quantity"));
                                }
                            }
                        }

                        //From where quantiy is actually being consumed
                        for (int i = 0; i < actualCusumptionJSONArray.length(); i++) {
                            JSONObject actualCusumptionJSONObject = new JSONObject(actualCusumptionJSONArray.get(i).toString());
                            String batchname = StringUtil.DecodeText(actualCusumptionJSONObject.optString("batch"));
                            String productBatchId = accCommonTablesDAO.getpurchaseBatchIdForLocationWarehouseRowRackBin(workOrderComponentDetails.getProduct().getID(), actualCusumptionJSONObject.getString("location"), actualCusumptionJSONObject.getString("warehouse"), actualCusumptionJSONObject.optString("row", null), actualCusumptionJSONObject.optString("rack", null), actualCusumptionJSONObject.optString("bin", null), batchname);
                            if (!StringUtil.isNullOrEmpty(productBatchId)) {
                                actualConsumptionBatchMap.put(productBatchId, actualCusumptionJSONObject.getString("quantity"));
                            }
                        }


                        HashMap<String, String> outQuantityDetailsMap = new HashMap();
                        HashMap<String, String> inQuantityDetailsMap = new HashMap();

                        for (Map.Entry<String, String> blockEntry : blockDetailsBatchMap.entrySet()) {
                            if (actualConsumptionBatchMap.containsKey(blockEntry.getKey())) {
                                double diff = Double.parseDouble(blockEntry.getValue()) - Double.parseDouble(actualConsumptionBatchMap.get(blockEntry.getKey().toString()));
                                diff = authHandler.roundQuantity(diff, companyid);
                                if (diff <= 0) {
                                    //Fully consumed from it was blocked
                                    outQuantityDetailsMap.put(blockEntry.getKey().toString(), actualConsumptionBatchMap.get(blockEntry.getKey().toString()));

                                    if(diff < 0){//If consumed more than blocked quantity
                                        inQuantityDetailsMap.put(blockEntry.getKey().toString(), "" + blockEntry.getValue());
                                    }
                                } else if (diff > 0) {
                                    //Partially consumed from where it was blocked
                                    outQuantityDetailsMap.put(blockEntry.getKey().toString(), "" + Double.parseDouble(actualConsumptionBatchMap.get(blockEntry.getKey().toString())));

                                    //Partially un-consumed from where it was blocked
                                    inQuantityDetailsMap.put(blockEntry.getKey().toString(), "" + diff);
                                }
                            } else {
                                //Consumed from where it was not blocked
                                inQuantityDetailsMap.put(blockEntry.getKey().toString(), blockEntry.getValue());
                            }
                        }

                        for (Map.Entry<String, String> actualEntry : actualConsumptionBatchMap.entrySet()) {
                            if (!blockDetailsBatchMap.containsKey(actualEntry.getKey())) {
                                //Fully consumed from it was not blocked
                                outQuantityDetailsMap.put(actualEntry.getKey().toString(), actualEntry.getValue());
                            }
                        }

                        if (ComponentType == 2 || ComponentType == 3) {
                            requestParams.put("coProductScrapTotalProducedQuantity", comproquantity);
                            if (ComponentType == 2) {
                                requestParams.put("isCoProductProduceQuantity", true);
                                requestParams.put("remark", WorkOrder.Co_Product_Stock_Added);
                            } else {
                                requestParams.put("isScrapProductProduceQuantity", true);
                                requestParams.put("remark", WorkOrder.Scrap_Product_Stock_Added);
                            }
                            saveProductNewBatch(batchdetails, inventory, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
                            if (ComponentType == 2) {
                                requestParams.remove("isCoProductProduceQuantity");
                            } else {
                                requestParams.remove("isScrapProductProduceQuantity");
                            }
                            requestParams.remove("coProductScrapTotalProducedQuantity");
                            requestParams.remove("remark");
                        } else {
                            System.out.println("\nOUT - \n");

                            JSONArray outQuantityDetailsJSONArray = new JSONArray();
                            for(Map.Entry<String, String> quantityDetails : outQuantityDetailsMap.entrySet()){ 
                                double quantity = Double.parseDouble(quantityDetails.getValue());
                                KwlReturnObject NewProductBatchObj = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), quantityDetails.getKey());
                                NewProductBatch newProductBatch = (NewProductBatch) NewProductBatchObj.getEntityList().get(0);

                                JSONObject obj = new JSONObject();
                                obj.put("id", "");
                                obj.put("location", newProductBatch.getLocation()!=null ? newProductBatch.getLocation().getId() : workOrderComponentDetails.getProduct().getLocation().getId());
                                obj.put("row", newProductBatch.getRow()!=null ? newProductBatch.getRow().getId() : "");
                                obj.put("rack", newProductBatch.getRack()!=null ? newProductBatch.getRack().getId() : "");
                                obj.put("bin", newProductBatch.getBin()!=null ? newProductBatch.getBin().getId() : "");
                                obj.put("warehouse", newProductBatch.getWarehouse()!=null ? newProductBatch.getWarehouse().getId() : workOrderComponentDetails.getProduct().getWarehouse().getId());
                                obj.put("productid", newProductBatch.getProduct());
                                obj.put("mfgdate", newProductBatch.getMfgdate());
                                obj.put("expdate", newProductBatch.getExpdate());
                                obj.put("quantity", quantity);
                                obj.put("batch", newProductBatch.getBatchname());
                                obj.put("batchname", newProductBatch.getBatchname());
                                obj.put("purchasebatchid", newProductBatch.getId());

                                System.out.println(newProductBatch.getId() + ", " + newProductBatch.getBatchname() + ", " + quantity);
                                outQuantityDetailsJSONArray.put(obj);
                            }

                            //Quantity OUT
                            requestParams.put("isConsumeQuantity", true);
                            requestParams.put("remark", WorkOrder.USED_IN_CONSUMPTION);
                            saveNewAssemblyBatch(outQuantityDetailsJSONArray.toString(), inventory, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
                            requestParams.remove("isConsumeQuantity");
                            requestParams.remove("remark");

                            System.out.println("\nIN - \n");

                            JSONArray inQuantityDetailsJSONArray = new JSONArray();
                            for(Map.Entry<String, String> quantityDetails : inQuantityDetailsMap.entrySet()){
                                double quantity = Double.parseDouble(quantityDetails.getValue());
                                KwlReturnObject NewProductBatchObj = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), quantityDetails.getKey());
                                NewProductBatch newProductBatch = (NewProductBatch) NewProductBatchObj.getEntityList().get(0);

                                JSONObject obj = new JSONObject();
                                obj.put("id", "");
                                obj.put("location", newProductBatch.getLocation()!=null ? newProductBatch.getLocation().getId() : workOrderComponentDetails.getProduct().getLocation().getId());
                                obj.put("row", newProductBatch.getRow()!=null ? newProductBatch.getRow().getId() : "");
                                obj.put("rack", newProductBatch.getRack()!=null ? newProductBatch.getRack().getId() : "");
                                obj.put("bin", newProductBatch.getBin()!=null ? newProductBatch.getBin().getId() : "");
                                obj.put("warehouse", newProductBatch.getWarehouse()!=null ? newProductBatch.getWarehouse().getId() : workOrderComponentDetails.getProduct().getWarehouse().getId());
                                obj.put("productid", newProductBatch.getProduct());
                                obj.put("mfgdate", newProductBatch.getMfgdate());
                                obj.put("expdate", newProductBatch.getExpdate());
                                obj.put("quantity", quantity);
                                obj.put("batch", newProductBatch.getBatchname());
                                obj.put("batchname", newProductBatch.getBatchname());
                                obj.put("purchasebatchid", newProductBatch.getId());

                                System.out.println(newProductBatch.getId() + ", " + newProductBatch.getBatchname() + ", " + quantity);

                                inQuantityDetailsJSONArray.put(obj);
                            }

                            if(inQuantityDetailsJSONArray.length()>0){

                                //Quantity OUT
                                requestParams.put("isRemainingBlockQuantityFree", true);
    //                            requestParams.put("remark", WorkOrder.STOCKOUT_DONE_FOR_BLOCK_QUANTITY_FREE);
                                saveNewAssemblyBatch(inQuantityDetailsJSONArray.toString(), inventory, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
                                requestParams.remove("isRemainingBlockQuantityFree");
    //                            requestParams.remove("remark");

                                //Need to Reduce (R/W) quantity from this quantity
                                //quantity=quantity-(R/W) 
                                //Quantity IN
                                requestParams.put("isRemainingBlockQuantityFree", true);
    //                            requestParams.put("remark", WorkOrder.ADDED_THROUGH_BLOCK_QUANTITY_FREE_MANAGEMENT);
                                saveProductNewBatch(inQuantityDetailsJSONArray.toString(), inventory, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
                                requestParams.remove("isRemainingBlockQuantityFree");
    //                            requestParams.remove("remark");

                            }
                        }
                    }
                    /**
                    * For sub assembly product double quantity was produce( 1st manage quantity and 2nd manage quantity )
                    * so , adding check if batch details is null or empty then only produce quantity for sub assembly product.  
                    */
                    if (!StringUtil.isNullOrEmpty(producedqtydetails) && StringUtil.isNullOrEmpty(batchdetails)) {  
                        requestParams.put("isProduceQuantity", true);
                        requestParams.put("remark", WorkOrder.ADDED_THROUGH_PRODUCTION);
                        saveProductNewBatch(producedqtydetails, inventory, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
                        requestParams.remove("isProduceQuantity");
                        requestParams.remove("remark");
                    }
                }
                
                 /** 
                    * Now Final Finished product is selected at Task level 
                    * Now we have window to consume ingredient of final assembly product of work order
                    * Need to remove logic of If produced product is ingredient of final assembly product of work order, then consume it.
                  */
                
                /**
                 * For Produced Product who is ingredient of final assembly product then consume it and make out entry in Inventory side Reports 
                 * update following tables:
                                          * newproductbatch
                                          * locationbatchdocumentmapping
                                          * stock movement and stock movement detail.
                 */
//                if (isProducedProductIngredientOfFinalAssemblyProduct) {
//
//                    if (!StringUtil.isNullOrEmpty(producedqtydetails)) {
//
//                        JSONArray jArrayconsumed = new JSONArray();
//                        JSONArray jArrproduced = new JSONArray(producedqtydetails);
//                        for (int i = 0; i < jArrproduced.length(); i++) {
//                            JSONObject jSONObjectProduced = new JSONObject(jArrproduced.get(i).toString());
//                            jSONObjectProduced.put("quantity", producedQuantityConsumed);
//                            jArrayconsumed.put(jSONObjectProduced);
//                        }
//                        requestParams.put("isConsumeQuantity", true);
//                        requestParams.put("remark", WorkOrder.USED_IN_CONSUMPTION);
//                        requestParams.put("isProducedProductIngredientOfFinalAssemblyProduct", isProducedProductIngredientOfFinalAssemblyProduct);
//                        saveNewAssemblyBatch(jArrayconsumed.toString(), inventory, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
//                        requestParams.remove("isConsumeQuantity");
//                        requestParams.remove("remark");
//                        requestParams.remove("isProducedProductIngredientOfFinalAssemblyProduct");
//                    }
//                }
                
                Inventory inventoryWasteOut = null;
                Inventory inventoryWasteIn = null;
                String wasteqtydetails = "";
                if (jobj.has("wasteqtydetails")) {
                    wasteqtydetails = (String) jobj.getString("wasteqtydetails");
                }
                int wastemovementflag = (jobj.has("wastemovementflag") && !StringUtil.isNullOrEmpty(jobj.getString("wastemovementflag"))) ? Integer.parseInt(jobj.getString("wastemovementflag")) : 0;
                if(wastemovementflag == Constants.MRP_WASTEMOVEMENTFLAG_STOCKTRANSFER || wastemovementflag == Constants.MRP_WASTEMOVEMENTFLAG_STOCKOUT){
                    JSONArray jArrayOutWasteDetails = new JSONArray();
                    double totalWasteQuantityOut = 0.0d;
                    JSONArray jArr = new JSONArray(wasteqtydetails);
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
                        String purchaseBatchId=StringUtil.DecodeText(jSONObject.optString("purchasebatchid"));
                        double wasteQuantity=(jSONObject.has("quantity") && !StringUtil.isNullOrEmpty(jSONObject.getString("quantity"))) ? Double.parseDouble(jSONObject.getString("quantity")) : 0.0d;
                        totalWasteQuantityOut+=wasteQuantity;
                        if(wasteQuantity > 0){
                            for (int j = 0; j < blockDetailsJSONArray.length(); j++) {
                                JSONObject blockDetailsJSONObject = new JSONObject(blockDetailsJSONArray.get(j).toString());
                                if(purchaseBatchId.equals(blockDetailsJSONObject.getString("purchasebatchid"))){
                                    blockDetailsJSONObject.put("quantity", jSONObject.getString("quantity"));
                                    jArrayOutWasteDetails.put(blockDetailsJSONObject);
                                }
                            }
                        }
                    }
                    
                    HashMap<String, Object> requestParamsWaste = new HashMap<>();
                    /*
                    * OUT - Waste Quantity out from product's W/L
                    */
                    requestParamsWaste.put("carryin", false);
                    requestParamsWaste.put("companyid", companyid);
                    requestParamsWaste.put("updatedate", new Date()); //Inventory Updated On work order date
                    requestParamsWaste.put("product", workOrderComponentDetails.getProduct());
                    requestParamsWaste.put("productid", workOrderComponentDetails.getProduct() != null ? workOrderComponentDetails.getProduct().getID() : null);
                    String inventoryidwaste = workOrderComponentDetails.getInventoryWasteOut()!=null ? workOrderComponentDetails.getInventoryWasteOut().getID(): "";
                    requestParamsWaste.put("inventoryid", inventoryidwaste);                    
//                    double wasteQuantity = (jobj.has("wasteQuantity") && !StringUtil.isNullOrEmpty(jobj.getString("wasteQuantity"))) ? Double.parseDouble(jobj.getString("wasteQuantity")) : 0.0d;
//                    requestParamsWaste.put("quantity", wasteQuantity);
                    
//                    for (int i = 0; i < jArrayOutWasteDetails.length(); i++) {
//                        JSONObject jSONObject = new JSONObject(jArrayOutWasteDetails.get(i).toString());
//                        double wasteQuantity=(jSONObject.has("quantity") && !StringUtil.isNullOrEmpty(jSONObject.getString("quantity"))) ? Double.parseDouble(jSONObject.getString("quantity")) : 0.0d;                                                
//                    }

                    if (jArrayOutWasteDetails.length() > 0) {
                        requestParamsWaste.put("quantity", totalWasteQuantityOut);
                        inventoryWasteOut = UpdateInventory(requestParamsWaste);
//                      JSONArray jArrayReturn = new JSONArray();
//                      jArrayReturn.put(jSONObject);
                        requestParams.put("isWasteQuantity", true);
                        requestParams.put("remark", WorkOrder.STOCKOUT_DONE_FOR_WASTAGE);
                        saveNewAssemblyBatch(jArrayOutWasteDetails.toString(), inventoryWasteOut, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
                        requestParams.remove("isWasteQuantity");
                        requestParams.remove("remark");
                    }
                    
                    
//                    Inventory inventoryWasteOut = UpdateInventory(requestParamsWaste);
//
//                    JSONArray jArrayWaste = new JSONArray();
//                    JSONArray jArrWasteOut = new JSONArray(batchdetails);
//                    for (int i = 0; i < jArrWasteOut.length(); i++) {    
//                        JSONObject jSONObjectWaste = new JSONObject(jArrWasteOut.get(i).toString());
//                        jSONObjectWaste.put("quantity", wasteQuantity);
//                        jArrayWaste.put(jSONObjectWaste);
//                    }
//
//                    requestParams.put("isWasteQuantity", true);
//                    requestParams.put("remark", WorkOrder.STOCKOUT_DONE_FOR_WASTAGE);
//                    saveNewAssemblyBatch(jArrayWaste.toString(), inventoryWasteOut, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
//                    requestParams.remove("isWasteQuantity");
//                    requestParams.remove("remark");

                    if(wastemovementflag == Constants.MRP_WASTEMOVEMENTFLAG_STOCKTRANSFER){
                        if (!StringUtil.isNullOrEmpty(wasteqtydetails)) {
                            /*
                            * IN - Waste Quantity in to waste W/L
                            */
                            double totalWasteQuantity = 0.0d;
                            JSONArray jArrayInWasteDetails = new JSONArray();
                            for (int i = 0; i < jArr.length(); i++) {
                                JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
                                double wasteQuantity=(jSONObject.has("quantity") && !StringUtil.isNullOrEmpty(jSONObject.getString("quantity"))) ? Double.parseDouble(jSONObject.getString("quantity")) : 0.0d;
                                if(wasteQuantity > 0){
                                    jArrayInWasteDetails.put(jSONObject);
                                    totalWasteQuantity += wasteQuantity;
                                }
                            }

                            if(jArrayInWasteDetails.length() > 0){
                                requestParamsWaste.put("carryin", true);
                                requestParamsWaste.put("quantity", totalWasteQuantity);
                                inventoryidwaste = workOrderComponentDetails.getInventoryWasteIn()!=null ? workOrderComponentDetails.getInventoryWasteIn().getID(): "";
                                requestParamsWaste.put("inventoryid", inventoryidwaste);
                                inventoryWasteIn = UpdateInventory(requestParamsWaste);

                                requestParams.put("isWasteQuantity", true);
                                requestParams.put("remark", WorkOrder.ADDED_THROUGH_WASTE_MANAGEMENT);
                                saveProductNewBatch(jArrayInWasteDetails.toString(), inventoryWasteIn, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
                                requestParams.remove("isWasteQuantity");
                                requestParams.remove("remark");
                            }
                        }
                    }
                }
                Inventory inventoryRecycleIn = null;
                Inventory inventoryRecycleOut = null;
                String recycleqtydetails = "";
                if (jobj.has("recycleqtydetails")) {
                    recycleqtydetails = (String) jobj.getString("recycleqtydetails");
                }
                int recyclemovementflag = (jobj.has("recyclemovementflag") && !StringUtil.isNullOrEmpty(jobj.getString("recyclemovementflag"))) ? Integer.parseInt(jobj.getString("recyclemovementflag")) : 0;
                if(recyclemovementflag == Constants.MRP_RECYCLEMOVEMENTFLAG_STOCKTRANSFER){
                    JSONArray jArrayOutRecycleDetails = new JSONArray();
                    double totalRecycleQuantityOut = 0.0d;
                    JSONArray jArr = new JSONArray(recycleqtydetails);
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
                        String purchaseBatchId=StringUtil.DecodeText(jSONObject.optString("purchasebatchid"));
                        double recycleQuantity=(jSONObject.has("quantity") && !StringUtil.isNullOrEmpty(jSONObject.getString("quantity"))) ? Double.parseDouble(jSONObject.getString("quantity")) : 0.0d;
                        totalRecycleQuantityOut+=recycleQuantity;
                        if(recycleQuantity > 0){
                            for (int j = 0; j < blockDetailsJSONArray.length(); j++) {
                                JSONObject blockDetailsJSONObject = new JSONObject(blockDetailsJSONArray.get(j).toString());
                                if(purchaseBatchId.equals(blockDetailsJSONObject.getString("purchasebatchid"))){
                                    blockDetailsJSONObject.put("quantity", jSONObject.getString("quantity"));
                                    jArrayOutRecycleDetails.put(blockDetailsJSONObject);
                                }
                            }
                        }
                    }
                    
                    HashMap<String, Object> requestParamsRecycle = new HashMap<>();
                    /*
                    * OUT - Recycle Quantity out from product's W/L
                    */
                    requestParamsRecycle.put("carryin", false);
                    requestParamsRecycle.put("companyid", companyid);
                    requestParamsRecycle.put("updatedate", new Date()); //Inventory Updated On work order date
                    requestParamsRecycle.put("product", workOrderComponentDetails.getProduct());
                    requestParamsRecycle.put("productid", workOrderComponentDetails.getProduct() != null ? workOrderComponentDetails.getProduct().getID() : null);
                    String inventoryidrecycle = workOrderComponentDetails.getInventoryRecycleOut()!=null ? workOrderComponentDetails.getInventoryRecycleOut().getID(): "";
                    requestParamsRecycle.put("inventoryid", inventoryidrecycle);
//                    double recycleQuantity = (jobj.has("recycleQuantity") && !StringUtil.isNullOrEmpty(jobj.getString("recycleQuantity"))) ? Double.parseDouble(jobj.getString("recycleQuantity")) : 0.0d;
//                    requestParamsRecycle.put("quantity", recycleQuantity);
                    
//                    for (int i = 0; i < jArrayOutRecycleDetails.length(); i++) {
//                        JSONObject jSONObject = new JSONObject(jArrayOutRecycleDetails.get(i).toString());
//                        double recycleQuantity=(jSONObject.has("quantity") && !StringUtil.isNullOrEmpty(jSONObject.getString("quantity"))) ? Double.parseDouble(jSONObject.getString("quantity")) : 0.0d;
//                    }

                    if (jArrayOutRecycleDetails.length() > 0) {
                        requestParamsRecycle.put("quantity", totalRecycleQuantityOut);

                        inventoryRecycleOut = UpdateInventory(requestParamsRecycle);

//                      JSONArray jArrayRecycle = new JSONArray();
//                      jArrayRecycle.put(jSONObject);
                        requestParams.put("isRecycleQuantity", true);
                        requestParams.put("remark", WorkOrder.STOCKOUT_DONE_FOR_RECYCLE);
                        saveNewAssemblyBatch(jArrayOutRecycleDetails.toString(), inventoryRecycleOut, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
                        requestParams.remove("isRecycleQuantity");
                        requestParams.remove("remark");
                    }

//                    Inventory inventoryRecycleOut = UpdateInventory(requestParamsRecycle);
//
//                    JSONArray jArrayRecycle = new JSONArray();
//                    JSONArray jArrRecycleOut = new JSONArray(batchdetails);
//                    for (int i = 0; i < jArrRecycleOut.length(); i++) {
//                        JSONObject jSONObjectRecycle = new JSONObject(jArrRecycleOut.get(i).toString());
//                        jSONObjectRecycle.put("quantity", recycleQuantity);
//                        jArrayRecycle.put(jSONObjectRecycle);
//                    }
//
//                    requestParams.put("isRecycleQuantity", true);
//                    requestParams.put("remark", WorkOrder.STOCKOUT_DONE_FOR_RECYCLE);
//                    saveNewAssemblyBatch(jArrayRecycle.toString(), inventoryRecycleOut, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
//                    requestParams.remove("isRecycleQuantity");
//                    requestParams.remove("remark");

                    if(recyclemovementflag == Constants.MRP_RECYCLEMOVEMENTFLAG_STOCKTRANSFER){
                        if (!StringUtil.isNullOrEmpty(recycleqtydetails)) {
                            /*
                            * IN - Recycle Quantity in to recycle W/L
                            */
                            double totalRecycleQuantity = 0.0d;
                            JSONArray jArrayInRecycleDetails = new JSONArray();
                            for (int i = 0; i < jArr.length(); i++) {
                                JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
                                double wasteQuantity=(jSONObject.has("quantity") && !StringUtil.isNullOrEmpty(jSONObject.getString("quantity"))) ? Double.parseDouble(jSONObject.getString("quantity")) : 0.0d;
                                if(wasteQuantity > 0){
                                    jArrayInRecycleDetails.put(jSONObject);
                                    totalRecycleQuantity += wasteQuantity;
                                }
                            }

                            if(jArrayInRecycleDetails.length() > 0){
                                requestParamsRecycle.put("carryin", true);
                                requestParamsRecycle.put("quantity", totalRecycleQuantity);
                                inventoryidrecycle = workOrderComponentDetails.getInventoryRecycleIn()!=null ? workOrderComponentDetails.getInventoryRecycleIn().getID(): "";
                                requestParamsRecycle.put("inventoryid", inventoryidrecycle);
                                
                                inventoryRecycleIn = UpdateInventory(requestParamsRecycle);

                                requestParams.put("isRecycleQuantity", true);
                                requestParams.put("remark", WorkOrder.ADDED_THROUGH_RECYCLE_MANAGEMENT);
                                saveProductNewBatch(jArrayInRecycleDetails.toString(), inventoryRecycleIn, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
                                requestParams.remove("isRecycleQuantity");
                                requestParams.remove("remark");
                            }
                        }
                    }
                }
                
                
                
                /**
                 * Return Quantity.
                 */
                int returnmovementflag = (jobj.has("returnmovementflag") && !StringUtil.isNullOrEmpty(jobj.getString("returnmovementflag"))) ? Integer.parseInt(jobj.getString("returnmovementflag")) : 0;
                String returnOutInventoryID = "";
                String returnInInventoryID = "";
                Inventory inventoryReturnOut = null;
                Inventory inventoryReturnIn = null;
                
                String returnqtydetails = "";
                if (jobj.has("returnqtydetails")) {
                    returnqtydetails = (String) jobj.getString("returnqtydetails");
                }
                
                if (returnmovementflag == Constants.MRP_RETURNMOVEMENTFLAG_STOCKTRANSFER) {
                    if (!StringUtil.isNullOrEmpty(returnqtydetails)) {
                        JSONArray jArrayOutReturnDetails = new JSONArray();
                        double totalReturnQuantityOut = 0.0d;
                        JSONArray jArr = new JSONArray(returnqtydetails);
                        for (int i = 0; i < jArr.length(); i++) {
                            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
                            String purchaseBatchId=StringUtil.DecodeText(jSONObject.optString("purchasebatchid"));
                            double returnQuantity=(jSONObject.has("quantity") && !StringUtil.isNullOrEmpty(jSONObject.getString("quantity"))) ? Double.parseDouble(jSONObject.getString("quantity")) : 0.0d;
                            totalReturnQuantityOut+=returnQuantity;
                            if(returnQuantity > 0){
                                for (int j = 0; j < blockDetailsJSONArray.length(); j++) {
                                    JSONObject blockDetailsJSONObject = new JSONObject(blockDetailsJSONArray.get(j).toString());
                                    if(purchaseBatchId.equals(blockDetailsJSONObject.getString("purchasebatchid"))){
                                        blockDetailsJSONObject.put("quantity", jSONObject.getString("quantity"));
                                        jArrayOutReturnDetails.put(blockDetailsJSONObject);
                                    }
                                }
                            }
                        }

                        /*
                         * OUT - Return Quantity out from product's W/L
                         */
                        
                        HashMap<String, Object> requestParamsReturn = new HashMap<>();
                        requestParamsReturn.put("carryin", false);//OUT
                        requestParamsReturn.put("companyid", companyid);
                        requestParamsReturn.put("updatedate", new Date()); //Inventory Updated On work order date
                        requestParamsReturn.put("product", workOrderComponentDetails.getProduct());
                        requestParamsReturn.put("productid", workOrderComponentDetails.getProduct() != null ? workOrderComponentDetails.getProduct().getID() : null);
                        returnOutInventoryID= workOrderComponentDetails.getInventoryReturnedOut()!= null ? workOrderComponentDetails.getInventoryReturnedOut().getID() : "";
                        requestParamsReturn.put("inventoryid", returnOutInventoryID);
//                        double returnQuantity = (jobj.has("returnQuantity") && !StringUtil.isNullOrEmpty(jobj.getString("returnQuantity"))) ? Double.parseDouble(jobj.getString("returnQuantity")) : 0.0d;
//                        requestParamsReturn.put("quantity", returnQuantity);
                        
//                        for (int i = 0; i < jArrayOutReturnDetails.length(); i++) {
//                            JSONObject jSONObject = new JSONObject(jArrayOutReturnDetails.get(i).toString());
//                            double returnQuantity=(jSONObject.has("quantity") && !StringUtil.isNullOrEmpty(jSONObject.getString("quantity"))) ? Double.parseDouble(jSONObject.getString("quantity")) : 0.0d;
//                        }

                        if (jArrayOutReturnDetails.length() > 0) {
                            requestParamsReturn.put("quantity", totalReturnQuantityOut);
                            inventoryReturnOut = UpdateInventory(requestParamsReturn);

//                          JSONArray jArrayReturn = new JSONArray();
//                          jArrayReturn.put(jSONObject);
                            requestParams.put("isReturnQuantity", true);
                            requestParams.put("remark", WorkOrder.STOCKOUT_DONE_FOR_RETURN);
                            saveNewAssemblyBatch(jArrayOutReturnDetails.toString(), inventoryReturnOut, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
                            requestParams.remove("isReturnQuantity");
                            requestParams.remove("remark");
                        }

//                        JSONArray jArrayReturn = new JSONArray();
//                        JSONArray jArrReturnOut = new JSONArray(batchdetails);
//                        for (int i = 0; i < jArrReturnOut.length(); i++) {
//                            JSONObject jSONObjectReturn = new JSONObject(jArrReturnOut.get(i).toString());
//                            jSONObjectReturn.put("quantity", returnQuantity);
//                            jArrayReturn.put(jSONObjectReturn);
//                        }
//
//                        requestParams.put("isReturnQuantity", true);
//                        requestParams.put("remark", WorkOrder.STOCKOUT_DONE_FOR_RETURN);
//                        saveNewAssemblyBatch(jArrayReturn.toString(), inventoryReturnOut, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
//                        requestParams.remove("isReturnQuantity");
//                        requestParams.remove("remark");

                        if (returnmovementflag == Constants.MRP_RETURNMOVEMENTFLAG_STOCKTRANSFER) {
                            if (!StringUtil.isNullOrEmpty(returnqtydetails)) {
                                /*
                                 * IN - Return Quantity in to return W/L
                                 */
                                double totalReturnQuantity = 0.0d;
                                JSONArray jArrayInReturnDetails = new JSONArray();
                                for (int i = 0; i < jArr.length(); i++) {
                                    JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
                                    double returnQuantity=(jSONObject.has("quantity") && !StringUtil.isNullOrEmpty(jSONObject.getString("quantity"))) ? Double.parseDouble(jSONObject.getString("quantity")) : 0.0d;
                                    if(returnQuantity > 0){
                                        jArrayInReturnDetails.put(jSONObject);
                                        totalReturnQuantity += returnQuantity;
                                    }
                                }
                            
                                if(jArrayInReturnDetails.length() > 0){
                                    requestParamsReturn.put("carryin", true);
                                    requestParamsReturn.put("quantity", totalReturnQuantity);
                                    returnInInventoryID = workOrderComponentDetails.getInventoryReturnedIn()!= null ? workOrderComponentDetails.getInventoryReturnedIn().getID() : "";
                                    requestParamsReturn.put(Constants.inventoryID, returnInInventoryID);
                                    inventoryReturnIn = UpdateInventory(requestParamsReturn);

                                    requestParams.put("isReturnQuantity", true);//IN
                                    requestParams.put("remark", WorkOrder.ADDED_THROUGH_RETURN_MANAGEMENT);
                                    saveProductNewBatch(jArrayInReturnDetails.toString(), inventoryReturnIn, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
                                    requestParams.remove("isReturnQuantity");
                                    requestParams.remove("remark");
                                }
                            }
                        }
                    } 



                }
                /**
                 *  case added for BatchDetails(BD) absent and produceQtyDetail absent then do not allow.
                 */
                 if ((!(StringUtil.isNullOrEmpty(batchdetails) &&  StringUtil.isNullOrEmpty(producedqtydetails) && inventory!=null) || wastemovementflag!=0 || recyclemovementflag!=0 || !StringUtil.isNullOrEmpty(returnqtydetails)) && workOrderComponentDetails.getBlockQuantity() > 0) {
                    double blockquantityused = 0;
                    if(StringUtil.isNullOrEmpty(batchdetails) && !StringUtil.isNullOrEmpty(producedqtydetails)){
                        workOrderComponentDetails.setInventoryProduced(inventory);
                    }else{
                        /**
                         * Inventory Attribute is used for consume
                         */
                        workOrderComponentDetails.setInventory(inventory);
                        /**
                         * setBlock quantity used for consumed product (WCD)
                         */
                        if (actualquantity > workOrderComponentDetails.getBlockQuantity()) {
                            blockquantityused = workOrderComponentDetails.getBlockQuantity();
                        } else {
                            blockquantityused = actualquantity;
                        }
                        workOrderComponentDetails.setBlockQuantityUsed(blockquantityused);
                        workOrderComponentDetails.setBlockQtyUsed(true);
                        /**
                         * Remove Block quantity LBDM entry when product consumed.
                         */
                        Map<String, Object> params = new HashMap();
                        params.put(Constants.DOCUMENTID, workOrderComponentDetails.getID());
                        params.put(Constants.MRP_TransactionType, Constants.MRP_WorkOrderBlockQuantityTransactionType);
                        accProductDaoObj.deleteLBDMOnDocumentID(params);
                    }
                    /**
                     * Return Inventory Entries.
                     */
                    if(inventoryReturnOut != null && inventoryReturnIn != null){
                        workOrderComponentDetails.setInventoryReturnedOut(inventoryReturnOut);
                        workOrderComponentDetails.setInventoryReturnedIn(inventoryReturnIn);
                    } else {
                        /**
                         * delete ReturnInventory Entries if previously present and currently removed.
                         */
                        inventoryReturnOut = workOrderComponentDetails.getInventoryReturnedOut();
                        inventoryReturnIn = workOrderComponentDetails.getInventoryReturnedIn();
                        if (StringUtil.isNullOrEmpty(returnqtydetails) && inventoryReturnOut != null && inventoryReturnIn != null) {
                            workOrderComponentDetails.setInventoryReturnedOut(null);
                            workOrderComponentDetails.setInventoryReturnedIn(null);
                            accProductDaoObj.deleteInventory(inventoryReturnOut.getID(), companyid);
                            accProductDaoObj.deleteInventory(inventoryReturnIn.getID(), companyid);
                        }
                    }
                    /**
                     * waste Inventory Entries.
                     */
                    if(inventoryWasteOut != null){
                        workOrderComponentDetails.setInventoryWasteOut(inventoryWasteOut);
                    } else {
                        /**
                         * delete inventoryWasteOut Entries if previously present and currently removed.
                         */
                        inventoryWasteOut = workOrderComponentDetails.getInventoryWasteOut();
                        if (inventoryWasteOut != null) {
                            workOrderComponentDetails.setInventoryWasteOut(null);
                            accProductDaoObj.deleteInventory(inventoryWasteOut.getID(), companyid);
                        }
                    }
                    if(inventoryWasteIn != null){
                        workOrderComponentDetails.setInventoryWasteIn(inventoryWasteIn);
                    } else {
                        /**
                         * delete inventoryWasteOut Entries if previously present and currently removed.
                         */
                        inventoryWasteIn = workOrderComponentDetails.getInventoryWasteIn();
                        if (inventoryWasteIn != null) {
                            workOrderComponentDetails.setInventoryWasteIn(null);
                            accProductDaoObj.deleteInventory(inventoryWasteIn.getID(), companyid);
                        }
                    }
                    /**
                     * Recycle Inventory Entries.
                     */
                    if(inventoryRecycleIn != null && inventoryRecycleOut != null){
                        workOrderComponentDetails.setInventoryRecycleOut(inventoryRecycleOut);
                        workOrderComponentDetails.setInventoryRecycleIn(inventoryRecycleIn);
                    } else {
                        /**
                         * delete ReturnInventory Entries if previously present and currently removed.
                         */
                        inventoryRecycleOut = workOrderComponentDetails.getInventoryRecycleOut();
                        inventoryRecycleIn = workOrderComponentDetails.getInventoryRecycleIn();
                        if (StringUtil.isNullOrEmpty(recycleqtydetails) && inventoryRecycleOut != null && inventoryRecycleIn != null) {
                            workOrderComponentDetails.setInventoryRecycleOut(null);
                            workOrderComponentDetails.setInventoryRecycleIn(null);
                            accProductDaoObj.deleteInventory(inventoryRecycleOut.getID(), companyid);
                            accProductDaoObj.deleteInventory(inventoryRecycleIn.getID(), companyid);
                        }
                    }
                    accountingHandlerDAOobj.saveOrUpdateObject(workOrderComponentDetails);
                }
                
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("buildProductAssembly : " + e.getMessage(), e);
        }
    }
   
    @Override
    public JSONObject calCulateQuantityToBuild(Map<String, Object> requestParams,WorkOrder workOrder,  List<StockMovement> stockMovementsList) throws ServiceException, IOException, JSONException {
        JSONObject jSONObject = new JSONObject();
        try {
                String companyid = (String) requestParams.get(Constants.companyKey);
                Set<WorkOrderComponentDetails> workOrderComponentDetailsSet = workOrder.getComponentDetails();
                List<AssemblyNode> assemblyNodes = new ArrayList<>();
                for (WorkOrderComponentDetails workOrderComponentDetails : workOrderComponentDetailsSet) {
                    if(workOrderComponentDetails.getProduct().getID().equals(workOrder.getProductID().getID()))
                    {
                    continue;
                    }
                    String consumptionDetails = workOrderComponentDetails.getConsumptionDetails();
                    Product product = workOrderComponentDetails.getProduct();
                    if (!StringUtil.isNullOrEmpty(consumptionDetails) && product != null) {
                        JSONObject jobj = new JSONObject(consumptionDetails);
                        double actualquantity = 0;
                        double ratioquantity = 1;
                        double rejectedQuantity = 0;
                        if (jobj.has("actualquantity")) {
                            actualquantity = jobj.getDouble("actualquantity");
                        }
                        if (jobj.has("rejectedQuantity")) {
                            rejectedQuantity = jobj.getDouble("rejectedQuantity");
                        }

                        if (jobj.has("ratioquantity")) {
                            ratioquantity = jobj.optDouble("ratioquantity", 1.0);
                        }
                        JSONObject job=new JSONObject();
                        String wocParentProductID=workOrderComponentDetails.getParentProduct().getID();
                        job.put("productid",wocParentProductID);
                        KwlReturnObject subAssemblyProduct = accProductDaoObj.getSubAssemblyProduct(job);
                        int componentType = 0;
                        if (subAssemblyProduct.getEntityList() != null) {
                            List<ProductAssembly> subAssemblys = subAssemblyProduct.getEntityList();
                            
                            for (ProductAssembly subAssembly : subAssemblys) {
                                if (subAssembly.getSubproducts().getID().equals(workOrderComponentDetails.getProduct().getID())) {

                                    componentType = subAssembly.getComponentType();
                                }

                            }
                        }
                        
                        /** 
                         * if product is Co-product/scrap do not create Node for it.
                         * below code written to get work order build quantity (Assembly Product build Quantity) based on Consume Product. 
                         */
                        
                        //Creating Node for Each Sub-products of Work Order which are mapped at Detail level
                        if (!((componentType==2) || (componentType==3))) {
                            AssemblyNode assemblyNode = new AssemblyNode(product.getID(), ratioquantity, (actualquantity)); //Only the Quantity used is actual quantity-rejected Quantity
                            assemblyNode.setParentProductId(workOrderComponentDetails.getParentProduct() != null ? workOrderComponentDetails.getParentProduct().getID() : "");
                            assemblyNode.setWorkOrderComponentDetails(workOrderComponentDetails);
                            assemblyNodes.add(assemblyNode);
                        }
                        }

                }

                //Creating Node for  Work Order which Product
                AssemblyNode assemblyNode = new AssemblyNode(workOrder.getProductID().getID(), 1, workOrder.getQuantity());
                assemblyNode.setParentProductId(""); //Work Order product has the empty parent Product id
                assemblyNode.setWorkOrderComponentDetails(null);
                assemblyNodes.add(assemblyNode);


                //Assigning Child to parent products
                for (AssemblyNode assemblyNode1 : assemblyNodes) {
                    for (AssemblyNode assemblyNode2 : assemblyNodes) {
                        if (assemblyNode2.getParentProductId().equalsIgnoreCase(assemblyNode1.getProductId())) {
                            assemblyNode1.addSubProduct(assemblyNode2);
                        }
                    }

                }

                //Calculating Min Quantity Build for Work Order on the Basis of Availability
                assemblyNode.processBuild((int) workOrder.getQuantity()); //function must be Call from root (Work order product) 
                double workOrderBuildqty = assemblyNode.getMaxBuildQty();
                double workOrderBuildquantity=authHandler.roundQuantity((double) workOrderBuildqty, workOrder.getCompany().getCompanyID());   //ERP-40349 : workOrderBuildquantity will be double - Quantity for Finishing Product in Close WO.
                
                jSONObject.put("workOrderQty", workOrderBuildquantity);
                jSONObject.put("finalBuildQty", workOrderBuildquantity);
                jSONObject.put("assemblyNode", assemblyNode);
                



//                for (AssemblyNode assemblyNode1 : assemblyNodes) {
//                    if (!StringUtil.isNullOrEmpty(assemblyNode1.getParentProductId()) && assemblyNode1.getWorkOrderComponentDetails() != null) {
//                        WorkOrderComponentDetails workOrderComponentDetails = assemblyNode1.getWorkOrderComponentDetails();
//                        int usedBuildQty = assemblyNode1.getMaxBuildQty();
//                        String consumptionDetails = workOrderComponentDetails.getConsumptionDetails();
//                        if (!StringUtil.isNullOrEmpty(consumptionDetails) && workOrderComponentDetails.getProduct() != null) {
//                            JSONObject jobj = new JSONObject(consumptionDetails);
//                            double actualquantity = 0;
//                            double rejectedQuantity = 0;
//                            if (jobj.has("actualquantity")) {
//                                actualquantity = jobj.getDouble("actualquantity");
//                            }
//                            if (jobj.has("rejectedQuantity")) {
//                                rejectedQuantity = jobj.getDouble("rejectedQuantity");
//                            }
//
//                            double blockquantity = 0;
//                            if (jobj.has("blockquantity")) {
//                                blockquantity = jobj.optDouble("blockquantity", 0.0);
//                            }
//
//                            double blockquantityused = 0;
//                            if (blockquantity > usedBuildQty ) {
//                                blockquantityused = blockquantity - usedBuildQty ;
//                            }else{
//                                blockquantityused=usedBuildQty;
//                            }
//                                
//                            
//                            workOrderComponentDetails.setBlockQuantityUsed(blockquantityused);
//                            workOrderComponentDetails.setBlockQtyUsed(true);
//                            
//                            HashMap<String, Object> assemblyParams = new HashMap<>();
//                            assemblyParams.put("carryin", false); //reducing the ingradient price
//                            assemblyParams.put("quantity", (usedBuildQty + rejectedQuantity)); //Inventory Will consume Used and rejected qty
//                            assemblyParams.put("companyid", companyid);
//                            assemblyParams.put("updatedate", new Date()); //Inventory Updated On work order date
//                            assemblyParams.put("product", workOrderComponentDetails.getProduct());
//                            assemblyParams.put("productid", workOrderComponentDetails.getProduct() != null ? workOrderComponentDetails.getProduct().getID() : null);
//                            Inventory inventory = UpdateInventory(assemblyParams);
//                            String batchdetails = "";
//                            if (jobj.has("batchdetails")) {
//                                batchdetails = (String) jobj.getString("batchdetails");
//                            }
//                            if (!StringUtil.isNullOrEmpty(batchdetails)) {
//                                saveNewAssemblyBatch(batchdetails, inventory, requestParams, workOrderComponentDetails.getWorkOrder(), workOrderComponentDetails.getID(), stockMovementsList);
//                            }
//                        }
//                    }
//            }

        } catch (Exception e) {
            throw ServiceException.FAILURE("calCulateQuantityToBuild : " + e.getMessage(), e);
        }
        return jSONObject;
    }
    public Inventory UpdateInventory(HashMap<String, Object> assemblyParams) throws ServiceException, AccountingException {
        Inventory inventory = null;
        try {
            Product product= (Product)assemblyParams.get("product");
            JSONObject inventoryjson = new JSONObject();
            inventoryjson.put("productid", assemblyParams.get("productid"));
            inventoryjson.put("quantity", assemblyParams.get("quantity"));
            inventoryjson.put("baseuomquantity", assemblyParams.get("quantity"));
            inventoryjson.put("baseuomrate", 1);
            if (product.getUnitOfMeasure() != null) {
                inventoryjson.put("uomid", product.getUnitOfMeasure().getID());
            }
            inventoryjson.put("description", "");//Build Assembly
            inventoryjson.put("carryin", assemblyParams.get("carryin"));
            inventoryjson.put("defective", false);
            inventoryjson.put("newinventory", false);
            inventoryjson.put("updatedate", assemblyParams.get("updatedate")); //need to check which date to set
            inventoryjson.put("companyid", assemblyParams.get("companyid"));
            KwlReturnObject invresult = null;
            if (assemblyParams.containsKey(Constants.inventoryID) && !StringUtil.isNullOrEmpty((String) assemblyParams.get(Constants.inventoryID))) {
                inventoryjson.put(Constants.inventoryID, assemblyParams.get(Constants.inventoryID));
                invresult = accProductDaoObj.updateInventory(inventoryjson);
            } else {
                invresult = accProductDaoObj.addInventory(inventoryjson);
            }
            inventory = (Inventory) invresult.getEntityList().get(0);
        } catch (Exception e) {
            throw ServiceException.FAILURE("buildProductAssembly : " + e.getMessage(), e);
        }
        return inventory;
    }
    
    @Override
    public void saveorUpdateLockedBatchDetails(HashMap<String, Object> requestParams) throws ServiceException{
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("WO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            
          //  Free Existing Lock Quantity  
          workOrderDAOObj.deleteWorkOrderBatchSerialDetails(requestParams);
          
          //  Lock Quantity for Consumed Batch Details
          String batchdetails= requestParams.containsKey("batchdetails") ? (String) requestParams.get("batchdetails") :"";
          String productId= (String) requestParams.get("productId");
          String workorderdetailid= (String) requestParams.get("workorderdetailid");
           if (!StringUtil.isNullOrEmpty(batchdetails)) {
            JSONObject detJobj = new JSONObject();
            detJobj.put("df", "");
            detJobj.put("companyid", requestParams.get("companyid"));
            detJobj.put("userid", requestParams.get("userid"));
            accWorkOrderServiceDAOObj.lockWorkOrderDeatilsBatch(batchdetails,productId , detJobj, workorderdetailid);
        }
         
         txnManager.commit(status); 
         } catch(Exception ex) {
            txnManager.rollback(status); 
            Logger.getLogger(AccWorkOrderServiceImplCMN.class.getName()).log(Level.SEVERE, null, ex);
        }  
          
    }
    
    @Override
    public JSONObject getWorkOrderProductDetail(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        KwlReturnObject kmsg = null;
        try {
            String workOrderID = "";
            if (requestParams.containsKey(WorkOrder.WOID) && requestParams.get(WorkOrder.WOID) != null) {
                workOrderID = requestParams.get(WorkOrder.WOID).toString();
            }
            if(!StringUtil.isNullOrEmpty(workOrderID)){
                KwlReturnObject result = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), workOrderID);
                WorkOrder workOrder = (WorkOrder) result.getEntityList().get(0);
                Product product=workOrder.getProductID() != null ? workOrder.getProductID() : null ;
                if(product != null){
                   JSONObject jObj=AccProductService.getProductDetailJSON(product, requestParams);
                   jObj.put("quantity", workOrder.getQuantity());
                   jArr.put(jObj);
                }
            }
            jobj.put("data", jArr);
        } catch(Exception ex) {
            Logger.getLogger(AccWorkOrderServiceImplCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
     public void saveProductNewBatch(String batchJSON, Inventory inventory, Map<String,Object> requestMap, WorkOrder workOrder,String documentId, List<StockMovement> stockMovementsList) throws com.krawler.utils.json.base.JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {
        JSONArray jArr = new JSONArray(batchJSON);
        String purchasebatchid = "";
        KwlReturnObject kmsg = null;
        int ComponentType=0;
        double ActbatchQty = 1;
        double batchQty = 0;
        boolean isBatch = false;
        boolean isserial = false;
        double totalQty = 0.0;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isRowForProduct = false;        
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        boolean isnegativestockforlocwar = false;
        boolean isQAEnable = false;
        DateFormat df = authHandler.getDateOnlyFormat();
        DateFormat df2 = new SimpleDateFormat(Constants.yyyyMMdd);
        String companyid = (String)requestMap.get(Constants.companyKey);
        boolean isEdit = requestMap.containsKey("isEdit") ? (Boolean) requestMap.get("isEdit") : false ;
        boolean isWasteQuantity = requestMap.containsKey("isWasteQuantity") ? (Boolean) requestMap.get("isWasteQuantity") : false;
        boolean isRecycleQuantity = requestMap.containsKey("isRecycleQuantity") ? (Boolean) requestMap.get("isRecycleQuantity") : false;
        boolean isProduceQuantity = requestMap.containsKey("isProduceQuantity") ? (Boolean) requestMap.get("isProduceQuantity") : false;
        boolean isRemainingBlockQuantityFree = requestMap.containsKey("isRemainingBlockQuantityFree") ? (Boolean) requestMap.get("isRemainingBlockQuantityFree") : false;
        boolean isReturnQuantity =  requestMap.containsKey("isReturnQuantity") ? (Boolean) requestMap.get("isReturnQuantity") : false;
        boolean isCoProductProduceQuantity = requestMap.containsKey("isCoProductProduceQuantity") ? (Boolean) requestMap.get("isCoProductProduceQuantity") : false;
        boolean isScrapProductProduceQuantity = requestMap.containsKey("isScrapProductProduceQuantity") ? (Boolean) requestMap.get("isScrapProductProduceQuantity") : false;   
        boolean isFinalProductProducedOnWorkOrderClose = requestMap.containsKey("isFinalProductProducedOnWorkOrderClose") ? (Boolean) requestMap.get("isFinalProductProducedOnWorkOrderClose") : false;
        double FinalProductTotalProducedQuantity = requestMap.containsKey("FinalProductTotalProducedQuantity") ? (Double) requestMap.get("FinalProductTotalProducedQuantity") : 0;        
        double coProductScrapTotalProducedQuantity=requestMap.containsKey("coProductScrapTotalProducedQuantity") ? (Double) requestMap.get("coProductScrapTotalProducedQuantity") : 0;                
        boolean sendForQAApproval = requestMap.containsKey("sendForQAApproval") && isProduceQuantity? (Boolean) requestMap.get("sendForQAApproval") : false;
        String inspectionAreaDetails = requestMap.containsKey("inspectionAreaDetails") && !StringUtil.isNullOrEmpty((String) requestMap.get("inspectionAreaDetails")) ? (String) requestMap.get("inspectionAreaDetails") : "";  
        String inspectionformInfo = requestMap.containsKey("inspectionformInfo") && !StringUtil.isNullOrEmpty((String) requestMap.get("inspectionformInfo")) ? (String) requestMap.get("inspectionformInfo") : "";
        Date taskTransactionDate = requestMap.containsKey("taskTransactionDate") && requestMap.get("taskTransactionDate")!=null ? (Date) requestMap.get("taskTransactionDate") : new Date();
                
        WorkOrderComponentDetails wocdObj = (WorkOrderComponentDetails) kwlCommonTablesDAOObj.getClassObject(WorkOrderComponentDetails.class.getName(), documentId);
        String remark = "";
        if(requestMap.containsKey("remark") &&  requestMap.get("remark") != null ){
            remark = requestMap.get("remark").toString();
        }
        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
        isBatch = preferences.isIsBatchCompulsory();
        isserial = preferences.isIsSerialCompulsory();
        
        ExtraCompanyPreferences extraCompanyPreferences = null;
        KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
        isnegativestockforlocwar= extraCompanyPreferences.isIsnegativestockforlocwar();

        if (!StringUtil.isNullOrEmpty(inventory.getProduct().getID())) {
            KwlReturnObject prodresult = accProductDaoObj.getObject(Product.class.getName(), inventory.getProduct().getID());
            Product product = (Product) prodresult.getEntityList().get(0);
            isLocationForProduct = product.isIslocationforproduct();
            isWarehouseForProduct = product.isIswarehouseforproduct();
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
            isRowForProduct = product.isIsrowforproduct();
            isRackForProduct = product.isIsrackforproduct();
            isBinForProduct = product.isIsbinforproduct();
            isQAEnable = product.isQaenable();
        }
        NewProductBatch productBatch = null;
        String productBatchId = "";
        JSONArray jArrBlockDetails = new JSONArray();
        //Save Batch detail for both  option for serial no and batch also as if batch option is off then also we are generating batch in backend
        StockMovementDetail smd = null;
        StockMovement stockMovement = null;
        Map<Store, StockMovement> storeWiseStockMovement = new HashMap<Store, StockMovement>();
        
         /**
         * Get Inter Store / Location Sequence format
         */
         String interstore_loc_No = "";
         SeqFormat interStoreTransferSeqFormat = null;
         Store qaStore = null;
         InterStoreTransferRequest interStoreTransferQC = null;
         ISTDetail istDetailQC = null;
         Map<Store, InterStoreTransferRequest> storeWiseInterStoreTransferRequestForQC = new HashMap<Store, InterStoreTransferRequest>();
         try {
             if (sendForQAApproval) {
                 if (requestMap.containsKey("interstoreSeqNo") && requestMap.containsKey("interstoreseqFormat") && !StringUtil.isNullOrEmpty((String) requestMap.get("interstoreSeqNo"))) {
                     interstore_loc_No = (String) requestMap.get("interstoreSeqNo");
                     interStoreTransferSeqFormat = (SeqFormat) requestMap.get("interstoreseqFormat");
                 } else {
                     throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, Locale.forLanguageTag((String) requestMap.get(Constants.language))));
                 }
                 KwlReturnObject tostoreres = accountingHandlerDAOobj.getObject(Store.class.getName(), extraCompanyPreferences.getInspectionStore());
                 qaStore = (Store) tostoreres.getEntityList().get(0);
                 if (qaStore == null) {
                     throw new InventoryException(messageSource.getMessage("acc.companypreferences.qastore.notset", null, Locale.forLanguageTag((String) requestMap.get(Constants.language))));
                 }
             }
         } catch (AccountingException ex) {
             throw new AccountingException(ex.getMessage());
         }
        
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
           
             if ((jSONObject.has("ComponentType") && !jSONObject.getString("ComponentType").equals("undefined") && !jSONObject.getString("ComponentType").isEmpty()) || 
                     (jSONObject.has("componentType") && !jSONObject.getString("componentType").equals("undefined") && !jSONObject.getString("componentType").isEmpty())) {
                ComponentType=jSONObject.getInt("ComponentType");
            }
            if (jSONObject.has("quantity") && !jSONObject.getString("quantity").equals("undefined") && !jSONObject.getString("quantity").isEmpty()) {
                ActbatchQty = jSONObject.getDouble("quantity");
                totalQty += jSONObject.getDouble("quantity");
            }
            if (batchQty == 0) {
                batchQty = jSONObject.getDouble("quantity");

                KwlReturnObject warehouseObj = accountingHandlerDAOobj.getObject(Store.class.getName(), jSONObject.getString("warehouse"));
                Store store = (Store) warehouseObj.getEntityList().get(0);
                if (isWarehouseForProduct && isLocationForProduct) {
                        if (storeWiseStockMovement.containsKey(store)) {
                            stockMovement = storeWiseStockMovement.get(store);
                            stockMovement.setQuantity(stockMovement.getQuantity() + jSONObject.optDouble("quantity", 0.0));
                        } else {
                            stockMovement = new StockMovement();
                            if (store != null) {
                                stockMovement.setStore(store);
                            }
                            double wocdPricePerUnit = 1;
                            double totalPriceOfFinalProduct = 0;
                            if (wocdObj != null) {
                                if (isCoProductProduceQuantity || isScrapProductProduceQuantity) {
                                    wocdPricePerUnit = (wocdObj.getInitialPurchasePrice() != 0) ? authHandler.round((wocdObj.getInitialPurchasePrice() / coProductScrapTotalProducedQuantity), companyid) : 1;
                                } else {
                                    if (isFinalProductProducedOnWorkOrderClose) {
                                        if (wocdObj.getInitialPurchasePrice() != 0) {
                                            totalPriceOfFinalProduct = authHandler.round(wocdObj.getInitialPurchasePrice() * wocdObj.getRequiredQuantity(), companyid);
                                            wocdPricePerUnit = authHandler.round(totalPriceOfFinalProduct / FinalProductTotalProducedQuantity, companyid);
                                        } else {
                                            wocdPricePerUnit = 1;
                                        }
                                    } else {
                                        wocdPricePerUnit = (wocdObj.getInitialPurchasePrice() != 0) ? wocdObj.getInitialPurchasePrice() : 1;
                                    }
                                }
                            }
                            Date transactionDate = new Date();
                            stockMovement.setCompany(inventory.getCompany());
                            stockMovement.setProduct(inventory.getProduct());
                            stockMovement.setStockUoM(inventory.getProduct().getUnitOfMeasure());
                            stockMovement.setPricePerUnit(wocdPricePerUnit);//workordercomponentdetails Initial Purchase price
                            stockMovement.setQuantity(jSONObject.optDouble("quantity", 0.0));
                            /**
                             * Set Transaction date as Task End Date for particular manage quantity.
                             */
                            stockMovement.setTransactionDate(authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(taskTransactionDate)));
                            if (isProduceQuantity) {
                                transactionDate.setTime(transactionDate.getTime() - Constants.MILLIS_PER_SECOND);
                            }
                            stockMovement.setCreatedOn(transactionDate);
                            stockMovement.setModuleRefId(workOrder.getID());
                            stockMovement.setModuleRefDetailId(documentId);
                            stockMovement.setVendor(null);
                            stockMovement.setCostCenter(null);
                            stockMovement.setTransactionNo(workOrder.getWorkOrderID());
                            stockMovement.setTransactionModule(TransactionModule.Work_Order);/*Transaction module:Work Order*/
                            stockMovement.setTransactionType(TransactionType.IN);

                    /** MRP : STOCK_MANAGEMENT_FLAG.
                      * NON MRP ACTIVATED COMPANY : DEFAULT_STOCK_MANAGEMENT_FLAG 
                      * MRP ACTIVATED COMPANY :
                                        * CONSUME / PRODUCE MANAGEMENT : MRP_CONSUME_PRODUCE_STOCK_MANAGEMENT_FLAG
                                        * WASTE MANAGEMENT : MRP_WASTE_STOCK_MANAGEMENT_FLAG
                                        * RECYCLE MANAGEMENT : MRP_RECYCLE_STOCK_MANAGEMENT_FLAG
                                        * RETURN MANAGEMENT : MRP_RETURN_STOCK_MANAGEMENT_FLAG
                      * This FLAG Used to differentiating or distinguishing between Above FOUR MANAGEMENTS.                  
                      */
                            if (isProduceQuantity || isCoProductProduceQuantity || isScrapProductProduceQuantity) {
                                stockMovement.setStock_management_flag(Constants.MRP_CONSUME_PRODUCE_STOCK_MANAGEMENT_FLAG);
                            } else if (isWasteQuantity) {
                                stockMovement.setStock_management_flag(Constants.MRP_WASTE_STOCK_MANAGEMENT_FLAG);
                            } else if (isRecycleQuantity) {
                                stockMovement.setStock_management_flag(Constants.MRP_RECYCLE_STOCK_MANAGEMENT_FLAG);
                            } else if (isReturnQuantity) {
                                stockMovement.setStock_management_flag(Constants.MRP_RETURN_STOCK_MANAGEMENT_FLAG);
                            } else {
                                stockMovement.setStock_management_flag(Constants.DEFAULT_STOCK_MANAGEMENT_FLAG);
                            }

                            if (isWasteQuantity || isRecycleQuantity || isProduceQuantity || isReturnQuantity || isCoProductProduceQuantity || isScrapProductProduceQuantity) {
                                stockMovement.setRemark(remark);
                            }
                            if (workOrder.getCustomer() != null) {
                                stockMovement.setCustomer(workOrder.getCustomer());
                            }
                            if (!isRemainingBlockQuantityFree) {
                                storeWiseStockMovement.put(store, stockMovement);
                            }
                        }
                        if (sendForQAApproval && isQAEnable) {
                        /**
                         * If Produced Product Quantity is send for QA approval then need to create
                         * store wise separate inter store transfer request.
                         * Create IST request and set values for required
                         * fields.
                         */
                        if (storeWiseInterStoreTransferRequestForQC.containsKey(store)) {
                            interStoreTransferQC = storeWiseInterStoreTransferRequestForQC.get(store);
                            interStoreTransferQC.setAcceptedQty(interStoreTransferQC.getAcceptedQty() + jSONObject.optDouble("quantity", 0.0));
                            interStoreTransferQC.setOrderedQty(interStoreTransferQC.getOrderedQty() + jSONObject.optDouble("quantity", 0.0));
                        } else {
                            if (store != null) {
                                interStoreTransferQC = new InterStoreTransferRequest(wocdObj.getProduct(), store, qaStore, wocdObj.getProduct().getUnitOfMeasure() != null ? wocdObj.getProduct().getUnitOfMeasure() : wocdObj.getProduct().getUnitOfMeasure());
                                interStoreTransferQC.setAcceptedQty(jSONObject.optDouble("quantity", 0.0));
                                interStoreTransferQC.setOrderedQty(jSONObject.optDouble("quantity", 0.0));
                                
//                                interStoreTransferQC.setCostCenter(deliveryOrderDetail.getDeliveryOrder().getCostcenter());
                                Date businessDate = new Date();
                                interStoreTransferQC.setTransactionNo(interstore_loc_No);
                                interStoreTransferQC.setBusinessDate(businessDate);
                                interStoreTransferQC.setUom(wocdObj.getProduct().getUnitOfMeasure());
                                String woNumber = workOrder!= null && !StringUtil.isNullOrEmpty(workOrder.getWorkOrderID()) ? workOrder.getWorkOrderID(): "";
                                interStoreTransferQC.setMemo((StringUtil.isNullOrEmpty(woNumber) ? ("IST created for QC against Work Order.") : ("IST created for QC against Work Order:" + woNumber)));
                                interStoreTransferQC.setRemark((StringUtil.isNullOrEmpty(woNumber) ? ("IST created for QC against Work Order.") : ("IST created for QC against Work Order:" + woNumber)));
                                interStoreTransferQC.setProduct(wocdObj.getProduct());
                                interStoreTransferQC.setPackaging(wocdObj.getProduct().getPackaging());
                                interStoreTransferQC.setCreatedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(businessDate)));
                                long creationDate = System.currentTimeMillis();
                                interStoreTransferQC.setCreationdate(creationDate);
                                interStoreTransferQC.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(businessDate)));
                                Logger.getLogger(AccWorkOrderServiceImplCMN.class.getName()).log(Level.INFO, "IST Creation: " + interStoreTransferQC.getCreatedOn());                                
                                storeWiseInterStoreTransferRequestForQC.put(store, interStoreTransferQC);

                            }
                        }
                    } 
                    }
                }
            
            
            
            if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct || isRowForProduct || isRackForProduct || isBinForProduct) && (batchQty == ActbatchQty)) {
                
                String batchname=StringUtil.DecodeText(jSONObject.optString("batch"));
                productBatchId = accCommonTablesDAO.getpurchaseBatchIdForLocationWarehouseRowRackBin(inventory.getProduct().getID(), jSONObject.getString("location"), jSONObject.getString("warehouse"),jSONObject.optString("row",null),jSONObject.optString("rack",null),jSONObject.optString("bin",null),batchname);
                
                   if (StringUtil.isNullOrEmpty(productBatchId)){
                    //if negative stock for location warehouse is not activated thencreate new batch always otherwise id negative stock is activated and batch not found then also create new batch
                    HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                    pdfTemplateMap.put("companyid", inventory.getCompany().getCompanyID());
                    pdfTemplateMap.put("name", batchname);
                    if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                        pdfTemplateMap.put("mfgdate", authHandler.getDateOnlyFormat().parse(jSONObject.getString("mfgdate")));
                    }
                    if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                        pdfTemplateMap.put("expdate", authHandler.getDateOnlyFormat().parse(jSONObject.getString("expdate")));
                    }
                    pdfTemplateMap.put("quantity", jSONObject.getString("quantity"));
                    if (jSONObject.has("balance") && !StringUtil.isNullOrEmpty(jSONObject.getString("balance"))) {
                    pdfTemplateMap.put("balance", jSONObject.getString("balance"));
                    }
                    pdfTemplateMap.put("location", jSONObject.optString("location",""));
                    pdfTemplateMap.put("product", inventory.getProduct().getID());
                    pdfTemplateMap.put("warehouse", jSONObject.optString("warehouse",""));

                    pdfTemplateMap.put("row", jSONObject.optString("row",null));
                    pdfTemplateMap.put("rack", jSONObject.optString("rack",null));
                    pdfTemplateMap.put("bin", jSONObject.optString("bin",null));

                    pdfTemplateMap.put("isopening", false);
                    pdfTemplateMap.put("transactiontype", "28");//This is GRN Type Tranction  
                    pdfTemplateMap.put("ispurchase", true);
                    kmsg = accCommonTablesDAO.saveNewBatchForProduct(pdfTemplateMap);

                    if (kmsg != null && kmsg.getEntityList().size() != 0) {
                        productBatch = (NewProductBatch) kmsg.getEntityList().get(0);
                        productBatchId = productBatch.getId();
                        
                        //For produced details setting as blockdetails in DB
                        jSONObject.put("batch", StringUtil.DecodeText(batchname));
                        jSONObject.put("batchname", StringUtil.DecodeText(batchname));
                        jSONObject.put("purchasebatchid", productBatchId);
                        jSONObject.put("documentbatchid", productBatchId);
                        /**
                         * Remove avlquantity as it shows up in batch serial
                         * window.
                         */
                        jSONObject.remove(Constants.avlQuantity);
                        jArrBlockDetails.put(jSONObject);
                    }
                } else {
                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                    batchUpdateQtyMap.put("qty", String.valueOf(Double.parseDouble(jSONObject.getString("quantity"))));
//                    batchUpdateQtyMap.put("quantity", String.valueOf(Double.parseDouble(jSONObject.getString("quantity"))));
                    batchUpdateQtyMap.put("id",productBatchId);
                    accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
                    
                    if (!StringUtil.isNullOrEmpty(productBatchId)) {
                        //For produced details setting as blockdetails in DB
                        jSONObject.put("batch", StringUtil.DecodeText(batchname));
                        jSONObject.put("batchname", StringUtil.DecodeText(batchname));
                        jSONObject.put("purchasebatchid", productBatchId);
                        jSONObject.put("documentbatchid", productBatchId);
                        /**
                         * Remove avlquantity as it shows up in batch serial
                         * window.
                         */
                        jSONObject.remove(Constants.avlQuantity);
                        jArrBlockDetails.put(jSONObject);
                    }
                }
                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", jSONObject.getString("quantity"));
                documentMap.put("batchmapid", productBatchId);
                documentMap.put("documentid",documentId);
                documentMap.put("transactiontype", "28");//This is GRN Type Tranction
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                    documentMap.put("mfgdate", authHandler.getDateOnlyFormat().parse(jSONObject.getString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                    documentMap.put("expdate", authHandler.getDateOnlyFormat().parse(jSONObject.getString("expdate")));
                }

                //Code to Send Batch
                KwlReturnObject batchObj = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), productBatchId);
                NewProductBatch newProductBatch1 = (NewProductBatch) batchObj.getEntityList().get(0);
                
                KwlReturnObject locationUpdate = accountingHandlerDAOobj.getObject(Location.class.getName(), jSONObject.getString("location"));
                Location locationObj = (Location) locationUpdate.getEntityList().get(0);
                if (isWarehouseForProduct && isLocationForProduct) {
                    smd = new StockMovementDetail();
                     /**
                     * Create ISTDetail object for IST request when produced product quantity sent for QA approval.                    
                     */
                    istDetailQC = new ISTDetail();
                    if (locationObj != null) {
                        smd.setLocation(locationObj);
                        if (sendForQAApproval && isQAEnable) {
                            if (qaStore.getDefaultLocation() == null) {
                                throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultLocationNotSetforQAStore", null, Locale.forLanguageTag((String) requestMap.get(Constants.language))));
                            }
                            Location toLocatioObj = qaStore.getDefaultLocation();
                            istDetailQC.setDeliveredLocation(toLocatioObj);
                            istDetailQC.setIssuedLocation(locationObj);
                        } 
                    }
                    if (isRowForProduct) {                        
                        KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), jSONObject.optString("row"));
                        StoreMaster row = (StoreMaster) krObject.getEntityList().get(0);
                        smd.setRow(row);
                        if (sendForQAApproval && isQAEnable) {
                            istDetailQC.setIssuedRow(row);
                            istDetailQC.setDeliveredRow(row);
                        } 
                    }
                    if (isRackForProduct) {
                        KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), jSONObject.optString("rack"));
                        StoreMaster rack = (StoreMaster) krObject.getEntityList().get(0);
                        smd.setRack(rack);
                        if (sendForQAApproval && isQAEnable) {
                            istDetailQC.setIssuedRack(rack);
                            istDetailQC.setDeliveredRack(rack);
                        } 
                    }
                    if (isBinForProduct) {
                        KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), jSONObject.optString("bin"));
                        StoreMaster bin = (StoreMaster) krObject.getEntityList().get(0);
                        smd.setBin(bin);
                        if (sendForQAApproval && isQAEnable) {
                            istDetailQC.setDeliveredBin(bin);
                            istDetailQC.setIssuedBin(bin);
                        } 
                    }
                      smd.setQuantity(Double.parseDouble(jSONObject.getString("quantity")));
                      smd.setBatchName(newProductBatch1.getBatchname());
                      smd.setStockMovement(stockMovement);
                      
                      stockMovement.getStockMovementDetails().add(smd);
                    
                      if (sendForQAApproval && isQAEnable) {
                        istDetailQC.setDeliveredQuantity(Double.parseDouble(jSONObject.getString("quantity")));
                        istDetailQC.setIssuedQuantity(Double.parseDouble(jSONObject.getString("quantity")));
                        istDetailQC.setBatchName(newProductBatch1.getBatchname());
                        istDetailQC.setIstRequest(interStoreTransferQC);
                        
                        /**
                         * save inspection area detail mapped at Task Level to IST detail for fetching inspection area in QA approval window. 
                         */
                          if (!StringUtil.isNullOrEmpty(inspectionAreaDetails) && !StringUtil.isNullOrEmpty(inspectionformInfo)) {
                             
 
                              SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                              
                              JSONObject jobjInForm = new JSONObject(inspectionformInfo);
                              JSONArray jarrayInForm = jobjInForm.getJSONArray("data");
                              JSONObject params = jarrayInForm.getJSONObject(0);                              
                              params.put("inspectionDate", sdf.format(sdf.parse(params.getString("inspectionDate"))));
                              String inspectionFormId = "";
                            
                              //save inspection form
                              KwlReturnObject inspectionFormResult = accCommonTablesDAO.saveOrUpdateInspectionForm(params);
                              List list = inspectionFormResult.getEntityList();
                              InspectionForm insForm = (InspectionForm) list.get(0);
                              if (StringUtil.isNullOrEmpty(inspectionFormId)) {

                                  istDetailQC.setInspectionForm(insForm);
                                  inspectionFormId = insForm.getId();

                                  params.put("inspectionFormId", inspectionFormId);
                              }
                          
                              //delete existing inspection form details
                              accCommonTablesDAO.deleteInspectionFormDetails(inspectionFormId);
                              JSONArray inspectionAreaJarr = new JSONArray(inspectionAreaDetails);
                              //save inspection form details
                              for (int ind = 0; ind < inspectionAreaJarr.length(); ind++) {
                                  JSONObject inspectionAreaObj = inspectionAreaJarr.optJSONObject(ind);

                                  HashMap<String, Object> inspectionFormDetailsMap = new HashMap<String, Object>();
                                  inspectionFormDetailsMap.put("inspectionFormId", inspectionFormId);
                                  inspectionFormDetailsMap.put("areaId", inspectionAreaObj.optString("areaId", ""));
                                  inspectionFormDetailsMap.put("areaName", inspectionAreaObj.optString("areaName", ""));
                                  inspectionFormDetailsMap.put("status", inspectionAreaObj.optString("status", ""));
                                  inspectionFormDetailsMap.put("faults", inspectionAreaObj.optString("faults", ""));
                                  inspectionFormDetailsMap.put("passingValue", inspectionAreaObj.optString("passingValue", ""));

                                  accCommonTablesDAO.saveInspectionFormDetails(inspectionFormDetailsMap);
                              }
      
                          }
                        
                        interStoreTransferQC.getIstDetails().add(istDetailQC);
                    }                   
                }
                accCommonTablesDAO.saveBatchDocumentMapping(documentMap);
            }
            batchQty--;

            if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 

                HashMap<String, Object> pdfTemplateMap = new HashMap<>();
                if(isEdit){
                pdfTemplateMap.put("id", "");
                }else{
                 pdfTemplateMap.put("id", jSONObject.getString("serialnoid"));
                }
                pdfTemplateMap.put("companyid", inventory.getCompany().getCompanyID());
                pdfTemplateMap.put("product", inventory.getProduct().getID());
                pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("serialno")));
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    pdfTemplateMap.put("expfromdate", authHandler.getDateOnlyFormat().parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    pdfTemplateMap.put("exptodate", authHandler.getDateOnlyFormat().parse(jSONObject.getString("expend")));
                }
                pdfTemplateMap.put("batch", productBatchId);
                pdfTemplateMap.put("transactiontype", "28");//This is GRN Type Tranction  
                pdfTemplateMap.put("quantity", "1");//This is GRN Type Tranction  
                pdfTemplateMap.put("ispurchase", true);
                pdfTemplateMap.put("purchasereturn", false);
                pdfTemplateMap.put("skuvalue",jSONObject.optString("skufield",""));
                kmsg = accCommonTablesDAO.saveNewSerialForBatch(pdfTemplateMap);
                String serialDetailsId = "";
                if (kmsg != null && kmsg.getEntityList().size() != 0) {
                    NewBatchSerial serialDetails = (NewBatchSerial) kmsg.getEntityList().get(0);
                    serialDetailsId = serialDetails.getId();
                }

                HashMap<String, Object> documentMap = new HashMap<>();
                documentMap.put("quantity", 1);
                documentMap.put("serialmapid", serialDetailsId);
                documentMap.put("documentid", documentId);
                documentMap.put("transactiontype", "28");//This is GRN Type Tranction  
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    documentMap.put("expfromdate", authHandler.getDateOnlyFormat().parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    documentMap.put("exptodate", authHandler.getDateOnlyFormat().parse(jSONObject.getString("expend")));
                }

                KwlReturnObject krObj = accCommonTablesDAO.saveSerialDocumentMapping(documentMap); 
                SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) krObj.getEntityList().get(0);
                if (jSONObject.has("customfield")) {
                    String customfield = jSONObject.getString("customfield");
                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        HashMap<String, Object> DOMap = new HashMap<>();
                        JSONArray jcustomarray = new JSONArray(customfield);

                        HashMap<String, Object> customrequestParams = new HashMap<>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "SerialDocumentMapping");
                        customrequestParams.put("moduleprimarykey", "SerialDocumentMappingId");
                        customrequestParams.put("modulerecid", serialDocumentMapping.getId());
                        customrequestParams.put("moduleid", Constants.SerialWindow_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        DOMap.put("id", serialDocumentMapping.getId());
                        customrequestParams.put("customdataclasspath", Constants.Acc_Serial_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            DOMap.put("serialcustomdataref", serialDocumentMapping.getId());
                            accCommonTablesDAO.updateserialcustomdata(DOMap);
                        }
                    }
                }
                KwlReturnObject serialObj = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), serialDetailsId);
                NewBatchSerial newBatchSerial = (NewBatchSerial) serialObj.getEntityList().get(0);
                if (newBatchSerial != null) {
                    if (isWarehouseForProduct && isLocationForProduct) {
                        smd.addSerialName(newBatchSerial.getSerialname());
                        if (sendForQAApproval && isQAEnable) {
                            StringBuilder serialNamesQC = new StringBuilder();
                            serialNamesQC.append(StringUtil.isNullOrEmpty(istDetailQC.getIssuedSerialNames()) ? "" : istDetailQC.getIssuedSerialNames());
                            if (serialNamesQC.length() > 0) {
                                serialNamesQC.append(",").append(newBatchSerial.getSerialname());
                            } else {
                                serialNamesQC.append(newBatchSerial.getSerialname());
                            }
                            istDetailQC.setDeliveredSerialNames(serialNamesQC.toString());
                            istDetailQC.setIssuedSerialNames(serialNamesQC.toString());
                        } 
                    }
                }
            } else {
                batchQty = 0;
            }
        }
        
            if (wocdObj != null) {
                
                String consumptionString = "";
                JSONObject consumeJobj = null;
                if (!StringUtil.isNullOrEmpty(wocdObj.getConsumptionDetails())) {
                    consumptionString = wocdObj.getConsumptionDetails();
                    consumeJobj = new JSONObject(consumptionString);
                    consumeJobj.put("actualquantity", totalQty);
                } else {
                    consumptionString = batchJSON;          
                }

                if (ComponentType == 2 || ComponentType == 3) {
                    /*
                     * for co-product or scrap set only producedquantity
                     * set InitialPurchasePrice for co-product and scrap
                     */
                    JSONObject job = new JSONObject();
                    String wocParentProductID = wocdObj.getParentProduct().getID();
                    job.put("productid", wocParentProductID);
                    KwlReturnObject subAssemblyProduct = accProductDaoObj.getSubAssemblyProduct(job);
                    
                    Set<WorkOrderComponentDetails> workordercomponentdetails=workOrder.getComponentDetails();
                    boolean isBlockQtyUsed=false;
                    double oldQuantity = 0;
                    double woquantity=0;
                    double blockQuantity=0;
                    double initailprice=0;
                    double tempComponetPrice=0;
                    double finalCoScrapPriceWithoutRate=0;
                    double finalCoScrapPriceWithRate=0;
                    double coScrapRate=0;
                    if (subAssemblyProduct.getEntityList() != null) {
                        List<ProductAssembly> subAssemblys = subAssemblyProduct.getEntityList();
                        
                        for (ProductAssembly subAssembly : subAssemblys) {
                            if (subAssembly.getComponentType()==1) {
                                 for(WorkOrderComponentDetails wocd:workordercomponentdetails){
                                  if(subAssembly.getSubproducts().getID().equals(wocd.getProduct().getID()))
                                  {
                                        isBlockQtyUsed=wocd.isBlockQtyUsed();
                                        if(isBlockQtyUsed){
                                            blockQuantity=wocd.getBlockQuantityUsed();
                                            initailprice=wocd.getInitialPurchasePrice();
                                            tempComponetPrice=0;
                                            tempComponetPrice=authHandler.round((blockQuantity*initailprice), companyid);
                                            finalCoScrapPriceWithoutRate+=tempComponetPrice;
                                        }else{
                                            blockQuantity=wocd.getBlockQuantity();
                                            initailprice=wocd.getInitialPurchasePrice();
                                            tempComponetPrice=0;
                                            tempComponetPrice=authHandler.round((blockQuantity*initailprice), companyid);
                                            finalCoScrapPriceWithoutRate+=tempComponetPrice;
                                        }
                                  }
                                 }
                                
                            }

                        }
                        
                        for (ProductAssembly subAssembly : subAssemblys) {
                            if (subAssembly.getSubproducts().getID().equals(wocdObj.getProduct().getID())) {
                                coScrapRate =subAssembly.getCrate()/100;     
                                finalCoScrapPriceWithRate=authHandler.round(((finalCoScrapPriceWithoutRate)*(coScrapRate)), companyid);
                                
                            }

                        }
                    }
                    if (totalQty!=0) {                        
                        wocdObj.setInitialPurchasePrice(finalCoScrapPriceWithRate);
                    }
                    wocdObj.setConsumptionDetails("");
                    wocdObj.setBlockQuantity(0);
                    wocdObj.setProducedQuantity(totalQty);
                    wocdObj.setBlockQuantityUsed(0);
                    wocdObj.setBlockQtyUsed(false);
                    wocdObj.setInventory(inventory);

                } else {
                    if (!(isWasteQuantity || isRecycleQuantity || isReturnQuantity || isRemainingBlockQuantityFree)) {
                        double totalPriceOfFinalProduct = 0;
                        double initialPurchasePrice = 0;
                        if (isFinalProductProducedOnWorkOrderClose) {
                            if (wocdObj.getInitialPurchasePrice() != 0) {
                                totalPriceOfFinalProduct = authHandler.round(wocdObj.getInitialPurchasePrice() * wocdObj.getRequiredQuantity(), companyid);
                                initialPurchasePrice = authHandler.round(totalPriceOfFinalProduct / totalQty, companyid);
                                wocdObj.setInitialPurchasePrice(initialPurchasePrice);
                            }
                            wocdObj.setConsumptionDetails(consumptionString);
                        } else {
                            wocdObj.setConsumptionDetails(consumeJobj.toString());
                        }
                        wocdObj.setBlockQuantity(totalQty);
                        wocdObj.setProducedQuantity(totalQty);
                        /**
                         * block quantity directly used because, rest of the
                         * tables in system is not properly updated for blocked
                         * quantity and in workorder quantity should not be
                         * blocked as this quantity should be available for
                         * other purposes. Making quick solution for time being
                         * to again used block quantity for that remaing
                         * unblocked for rest of the system.
                         */
                        wocdObj.setBlockQuantityUsed(totalQty);
                        wocdObj.setBlockQtyUsed(true);
                        if (jArrBlockDetails.length() > 0) {
                            wocdObj.setBlockDetails(jArrBlockDetails.toString());
                        }
                    }
                }
            }       
            
        if (isWarehouseForProduct && isLocationForProduct) {
            for (Map.Entry<Store, StockMovement> entry : storeWiseStockMovement.entrySet()) {
                stockMovementsList.add(entry.getValue());
            }
        }
        
         if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateInventoryTab() && !stockMovementsList.isEmpty()) {
             /**
              * false is passed as parameter, to restrict from removing stockmovement entry of WorkOrder(ERP-35428).
              */
             boolean removeStockMovementofWorkorder=false;
             stockMovementService.addOrUpdateBulkStockMovement(workOrder.getCompany(), workOrder.getID(), stockMovementsList,removeStockMovementofWorkorder);
             stockMovementsList.clear();
         }
         
         
          if (sendForQAApproval && isQAEnable) {
             /**
              * We have created map for storing store wise separate IST request.
              * All IST request will have same transaction number.
              */
             for (Map.Entry<Store, InterStoreTransferRequest> entrySet : storeWiseInterStoreTransferRequestForQC.entrySet()) {
                 InterStoreTransferRequest value = entrySet.getValue();
                 Logger.getLogger(AccWorkOrderServiceImplCMN.class.getName()).log(Level.INFO, "IST createdOn: " + value.getCreatedOn());
                 Map<String, Object> requestParams = new HashMap<>();
                 requestParams.put(Constants.companyid, companyid);
                 requestParams.put(Constants.CREATE_IST_FOR_QC_WORKORDER, true);
                 requestParams.put("wocdetailid", wocdObj.getID());
                 requestParams.put("actualquantity", value.getAcceptedQty());
                 requestParams.put("quantitydue", value.getAcceptedQty());
                 requestParams.put(Constants.moduleid, Constants.Acc_InterStore_ModuleId);
                 istService.addInterStoreTransferRequest(workOrder.getCreatedBy(), value, false, requestParams);
                 istService.acceptInterStoreTransferRequest(workOrder.getCreatedBy(), value);                 
             }
             
             storeWiseInterStoreTransferRequestForQC.clear();
             
         }
         
          accountingHandlerDAOobj.saveOrUpdateObject(wocdObj);   
          
           if (sendForQAApproval && isQAEnable) {
             try {
                 seqService.updateSeqNumber(interStoreTransferSeqFormat);
             } catch (SeqFormatException ex) {
                 Logger.getLogger(AccWorkOrderServiceImplCMN.class.getName()).log(Level.SEVERE, null, ex);
             }
         }       
   }
     
     public void saveNewAssemblyBatch(String batchJSON, Inventory inventory, Map<String,Object> requestMap, WorkOrder workOrder,String documentId, List<StockMovement> stockMovementsList) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {
        JSONArray jArr = new JSONArray(batchJSON);
        String purchasebatchid = "";
        KwlReturnObject kmsg = null;
        double ActbatchQty = 1;
        double batchQty = 0;
        double actualProducedQty = 0;
        double producedQty = 0;
        double lockedBatchQty=0;
        boolean isproducedQtyUpdate = false;
        boolean isBatch = false;
        boolean isserial = false;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        boolean isAssemblyProduct=false;
//        DateFormat df = authHandler.getDateFormatter(request);    //refer ticket ERP-15117
        if(requestMap.containsKey("actualProducedQty") &&  requestMap.get("actualProducedQty") != null ){
            actualProducedQty = (Double) requestMap.get("actualProducedQty");
        }
        boolean isWasteQuantity = requestMap.containsKey("isWasteQuantity") ? (Boolean) requestMap.get("isWasteQuantity") : false;
        boolean isRecycleQuantity = requestMap.containsKey("isRecycleQuantity") ? (Boolean) requestMap.get("isRecycleQuantity") : false;
        boolean isConsumeQuantity = requestMap.containsKey("isConsumeQuantity") ? (Boolean) requestMap.get("isConsumeQuantity") : false;
        boolean isRemainingBlockQuantityFree = requestMap.containsKey("isRemainingBlockQuantityFree") ? (Boolean) requestMap.get("isRemainingBlockQuantityFree") : false;
        boolean isReturnQuantity =  requestMap.containsKey("isReturnQuantity") ? (Boolean) requestMap.get("isReturnQuantity") : false;
        boolean isProducedProductIngredientOfFinalAssemblyProduct = requestMap.containsKey("isProducedProductIngredientOfFinalAssemblyProduct") ? (Boolean) requestMap.get("isProducedProductIngredientOfFinalAssemblyProduct") : false;
        Date taskTransactionDate = requestMap.containsKey("taskTransactionDate") && requestMap.get("taskTransactionDate")!=null ? (Date) requestMap.get("taskTransactionDate") : new Date();
        String remark = "";
        if(requestMap.containsKey("remark") &&  requestMap.get("remark") != null ){
            remark = requestMap.get("remark").toString();
        }
        DateFormat df = authHandler.getDateOnlyFormat();
        String companyid =  (String) requestMap.get(Constants.companyKey);
        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
        isBatch = preferences.isIsBatchCompulsory();
        isserial = preferences.isIsSerialCompulsory();
        
        ExtraCompanyPreferences extraCompanyPreferences = null;
        KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
        
         if (inventory.getProduct() != null) {
             //KwlReturnObject prodresult = accountingHandlerDAOobj.getObject(Product.class.getName(), productIdStr);
             //Product product = (Product) prodresult.getEntityList().get(0);
             Product product = inventory.getProduct();
             isLocationForProduct = product.isIslocationforproduct();
             isWarehouseForProduct = product.isIswarehouseforproduct();
             isBatchForProduct = product.isIsBatchForProduct();
             isSerialForProduct = product.isIsSerialForProduct();
             isRowForProduct = product.isIsrowforproduct();
             isRackForProduct = product.isIsrackforproduct();
             isBinForProduct = product.isIsbinforproduct();
                
             if (product.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                 isAssemblyProduct = true;

             }
         }

        StockMovementDetail smd = null;
        StockMovement stockMovement = null;
        Map<Store, StockMovement> storeWiseStockMovement = new HashMap<Store, StockMovement>();
        //Save Batch detail for both  option for serial no and batch also as if batch option is off then also we are generating batch in backend
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
            if (jSONObject.has("quantity") && !jSONObject.getString("quantity").equals("undefined") && !jSONObject.getString("quantity").isEmpty()) {
                ActbatchQty = jSONObject.getDouble("quantity");
            }
            if (batchQty == 0) {
                batchQty = jSONObject.getDouble("quantity");
                lockedBatchQty=batchQty;
                
                if (actualProducedQty > 0 && isproducedQtyUpdate == false) {
                    
                    if (actualProducedQty > 0 && actualProducedQty == (producedQty + batchQty)) {
                        isproducedQtyUpdate =true; // if the quanties required is same then no need to change batch Qty
                    } else if (actualProducedQty > 0 && actualProducedQty >  (producedQty + batchQty)) {
                        producedQty += batchQty;  // if quantity reuired is grater than batch qty then continue  
                    } else if (actualProducedQty > 0 && actualProducedQty < (producedQty + batchQty)) {
                       if(producedQty == 0){  // Batch qty is more or less than produced qty then adjust batch qty
                           batchQty = actualProducedQty;
                       }else{
                           batchQty = actualProducedQty - producedQty;
                       } 
                       isproducedQtyUpdate =true;
                    }
                }
                
                    
                 KwlReturnObject warehouseObj = accountingHandlerDAOobj.getObject(Store.class.getName(), jSONObject.getString("warehouse"));
                Store store = (Store) warehouseObj.getEntityList().get(0);
                if (isWarehouseForProduct && isLocationForProduct) {
                    if (storeWiseStockMovement.containsKey(store)) {
                        stockMovement = storeWiseStockMovement.get(store);
                        stockMovement.setQuantity(stockMovement.getQuantity() + batchQty);
                    } else {
                        stockMovement = new StockMovement();
                        if (store != null) {
                            stockMovement.setStore(store);
                        }
                        
                        WorkOrderComponentDetails wocdObj = (WorkOrderComponentDetails) kwlCommonTablesDAOObj.getClassObject(WorkOrderComponentDetails.class.getName(), documentId);
                        double wocdPricePerUnit = 1;
                        if (wocdObj != null) {
                            wocdPricePerUnit = (wocdObj.getInitialPurchasePrice() != 0) ? wocdObj.getInitialPurchasePrice() : 1;
                        }
                        Date transactionDate = new Date();
                        stockMovement.setCompany(inventory.getCompany());
                        stockMovement.setProduct(inventory.getProduct());
                        stockMovement.setStockUoM(inventory.getProduct().getUnitOfMeasure());
                        stockMovement.setPricePerUnit(wocdPricePerUnit);//workordercomponentdetails Initial Purchase price
                        stockMovement.setQuantity(batchQty);
                        /**
                         * Set Transaction date as Task End Date for particular manage quantity.
                         */
                        stockMovement.setTransactionDate(authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(taskTransactionDate)));
                        stockMovement.setModuleRefId(workOrder.getID());
                        stockMovement.setModuleRefDetailId(documentId);
                        stockMovement.setVendor(null);
                        stockMovement.setCostCenter(null);
                        stockMovement.setTransactionNo(workOrder.getWorkOrderID());
                        stockMovement.setTransactionModule(TransactionModule.Work_Order);/*Transaction module:Work Order*/            
                        stockMovement.setTransactionType(TransactionType.OUT);
                        
                        /** MRP : STOCK_MANAGEMENT_FLAG.
                          * NON MRP ACTIVATED COMPANY : DEFAULT_STOCK_MANAGEMENT_FLAG 
                          * MRP ACTIVATED COMPANY :
                                            * CONSUME / PRODUCE MANAGEMENT : MRP_CONSUME_PRODUCE_STOCK_MANAGEMENT_FLAG
                                            * WASTE MANAGEMENT : MRP_WASTE_STOCK_MANAGEMENT_FLAG
                                            * RECYCLE MANAGEMENT : MRP_RECYCLE_STOCK_MANAGEMENT_FLAG
                                            * RETURN MANAGEMENT : MRP_RETURN_STOCK_MANAGEMENT_FLAG
                         * This FLAG Used to differentiating or distinguishing between Above FOUR MANAGEMENTS.                  
                         */
                        
                        if (isConsumeQuantity) {
                            stockMovement.setStock_management_flag(Constants.MRP_CONSUME_PRODUCE_STOCK_MANAGEMENT_FLAG);
                        } else if (isWasteQuantity) {
                            stockMovement.setStock_management_flag(Constants.MRP_WASTE_STOCK_MANAGEMENT_FLAG);
                        } else if (isRecycleQuantity) {
                            stockMovement.setStock_management_flag(Constants.MRP_RECYCLE_STOCK_MANAGEMENT_FLAG);
                        } else if (isReturnQuantity) {
                            stockMovement.setStock_management_flag(Constants.MRP_RETURN_STOCK_MANAGEMENT_FLAG);
                        } else {
                            stockMovement.setStock_management_flag(Constants.DEFAULT_STOCK_MANAGEMENT_FLAG);
                        }
                        
                        if(isWasteQuantity || isRecycleQuantity || isConsumeQuantity || isReturnQuantity){
                            stockMovement.setRemark(remark);
                        }
                        if(workOrder.getCustomer()!=null){
                            stockMovement.setCustomer(workOrder.getCustomer());
                        }
                        if(!isRemainingBlockQuantityFree) {
                            storeWiseStockMovement.put(store, stockMovement);
                        }
                    }
                }
            }
            if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct || isRowForProduct || isRackForProduct || isBinForProduct) && (batchQty == ActbatchQty)) {
                HashMap<String, Object> documentMap = new HashMap<String, Object>();
               
                
                documentMap.put("quantity", Double.toString(batchQty));

                documentMap.put("documentid", documentId);
                documentMap.put("transactiontype", "27");//This is GRN Type Tranction  
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                    documentMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                    documentMap.put("expdate", df.parse(jSONObject.getString("expdate")));
                }
                documentMap.put("batchmapid", jSONObject.getString("purchasebatchid"));

                  if (isWarehouseForProduct && isLocationForProduct) {
                    smd = new StockMovementDetail();
                    
                    //as we are require Location object in stock movment so taken inventoryLocation id from that we have find out Location
                    KwlReturnObject locationUpdate = accountingHandlerDAOobj.getObject(Location.class.getName(), jSONObject.getString("location"));
                    Location locationObj = (Location) locationUpdate.getEntityList().get(0);
                    
                    if (locationObj != null) {
                        smd.setLocation(locationObj);
                    }
                    if(isRowForProduct){
                        KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), jSONObject.optString("row"));
                        StoreMaster row = (StoreMaster) krObject.getEntityList().get(0);
                        smd.setRow(row);
                    }
                    if(isRackForProduct){
                        KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), jSONObject.optString("rack"));
                        StoreMaster rack = (StoreMaster) krObject.getEntityList().get(0);
                        smd.setRack(rack);
                    }
                    if(isBinForProduct){
                        KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), jSONObject.optString("bin"));
                        StoreMaster bin = (StoreMaster) krObject.getEntityList().get(0);
                        smd.setBin(bin);
                    }
                    smd.setQuantity(batchQty);
                    smd.setBatchName("");
                    smd.setStockMovement(stockMovement);
                    stockMovement.getStockMovementDetails().add(smd);
                }

                if (!isBatchForProduct && !isSerialForProduct) {
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                    filter_names.add("company.companyID");
                    filter_params.add(companyid);

                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("warehouse"))) {
                        String warehouse = jSONObject.getString("warehouse");
                        filter_names.add("warehouse.id");
                        filter_params.add(warehouse);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("location"))) {
                        String location = jSONObject.getString("location");
                        filter_names.add("location.id");
                        filter_params.add(location);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("row"))) {
                        String row = jSONObject.getString("row");
                        filter_names.add("row.id");
                        filter_params.add(row);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("rack"))) {
                        String rack = jSONObject.getString("rack");
                        filter_names.add("rack.id");
                        filter_params.add(rack);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("bin"))) {
                        String bin = jSONObject.getString("bin");
                        filter_names.add("bin.id");
                        filter_params.add(bin);
                    }


                    // if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
                    filter_names.add("product");
                    filter_params.add(inventory.getProduct().getID());
                    // }


                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    filterRequestParams.put("order_by", order_by);
                    filterRequestParams.put("order_type", order_type);
                    KwlReturnObject result = accMasterItemsDAO.getNewBatches(filterRequestParams, false, false);
                    List listResult = result.getEntityList();
                    Iterator itrResult = listResult.iterator();
                    Double quantityToDue = ActbatchQty;
                    while (itrResult.hasNext()) {
                        if (quantityToDue > 0) {
                            NewProductBatch newProductBatch = (NewProductBatch) itrResult.next();
                            double dueQty = newProductBatch.getQuantitydue();
                            HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                            batchUpdateQtyMap.put("id", newProductBatch.getId());
                            if (dueQty > 0) {
                                if (quantityToDue > dueQty) {
                                    batchUpdateQtyMap.put("qty", String.valueOf(-(dueQty)));
                                    quantityToDue = quantityToDue - dueQty;

                                } else {
                                    batchUpdateQtyMap.put("qty", String.valueOf(-(quantityToDue)));
                                    quantityToDue = quantityToDue - quantityToDue;

                                }
                                if(newProductBatch.getLockquantity() >= ActbatchQty){
                                    batchUpdateQtyMap.put("lockquantity", String.valueOf(-(ActbatchQty)));
                                }
                                documentMap.put("batchmapid", newProductBatch.getId());
                                accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
                            }
                        }
                    }
                } else {
                    String batchname=StringUtil.DecodeText(jSONObject.optString("batch"));
                    String productBatchId = accCommonTablesDAO.getpurchaseBatchIdForLocationWarehouseRowRackBin(inventory.getProduct().getID(), jSONObject.getString("location"), jSONObject.getString("warehouse"),jSONObject.optString("row",null),jSONObject.optString("rack",null),jSONObject.optString("bin",null),batchname);
                    
                    if(isProducedProductIngredientOfFinalAssemblyProduct && !StringUtil.isNullOrEmpty(productBatchId)){
                        purchasebatchid = productBatchId;
                    }else if(isProducedProductIngredientOfFinalAssemblyProduct && StringUtil.isNullOrEmpty(productBatchId)){//Add Batch if jSONObject.getString("purchasebatchid") is empty & if isProducedProductIngredientOfFinalAssemblyProduct
                        HashMap<String, Object> consumeBatchMap = new HashMap();
                        consumeBatchMap.put("companyid", inventory.getCompany().getCompanyID());
                        consumeBatchMap.put("name", batchname);
                        if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                            consumeBatchMap.put("mfgdate", authHandler.getDateOnlyFormat().parse(jSONObject.getString("mfgdate")));
                        }
                        if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                            consumeBatchMap.put("expdate", authHandler.getDateOnlyFormat().parse(jSONObject.getString("expdate")));
                        }
                        consumeBatchMap.put("quantity", jSONObject.getString("quantity"));
                        if (jSONObject.has("balance") && !StringUtil.isNullOrEmpty(jSONObject.getString("balance"))) {
                        consumeBatchMap.put("balance", jSONObject.getString("balance"));
                        }
                        consumeBatchMap.put("location", jSONObject.optString("location",""));
                        consumeBatchMap.put("product", inventory.getProduct().getID());
                        consumeBatchMap.put("warehouse", jSONObject.optString("warehouse",""));

                        consumeBatchMap.put("row", jSONObject.optString("row",null));
                        consumeBatchMap.put("rack", jSONObject.optString("rack",null));
                        consumeBatchMap.put("bin", jSONObject.optString("bin",null));

                        consumeBatchMap.put("isopening", false);
                        consumeBatchMap.put("transactiontype", "28");//This is GRN Type Tranction  
                        consumeBatchMap.put("ispurchase", true);
                        kmsg = accCommonTablesDAO.saveNewBatchForProduct(consumeBatchMap);

                        if (kmsg != null && kmsg.getEntityList().size() != 0) {
                            NewProductBatch productBatch = (NewProductBatch) kmsg.getEntityList().get(0);
                            purchasebatchid = productBatchId = productBatch.getId();
                        }
                    }else{
                        purchasebatchid = jSONObject.getString("purchasebatchid");
                    }
                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                    batchUpdateQtyMap.put("qty", String.valueOf(-(batchQty)));
                    batchUpdateQtyMap.put("lockquantity", String.valueOf(-(batchQty)));
                    batchUpdateQtyMap.put("id", purchasebatchid);
                    accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
                    
                    //When Locked quantity is grater than used batch Qty
                    if(lockedBatchQty != batchQty){
                    batchUpdateQtyMap.clear();
                    batchUpdateQtyMap.put("lockquantity", String.valueOf(-(lockedBatchQty-batchQty)));
                    batchUpdateQtyMap.put("id", purchasebatchid);
                    accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
                        
                    }
                    
                    //Code to Send Batch
                    KwlReturnObject batchObj = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), purchasebatchid);
                    NewProductBatch newProductBatch1 = (NewProductBatch) batchObj.getEntityList().get(0);
                    if (isWarehouseForProduct && isLocationForProduct) {
                        smd.setBatchName(newProductBatch1.getBatchname());
                    }

                }
              /**
               * LocationBatchDocumentMapping.
               * transaction type is 27 and stock type is 1. pls change if requried
               */
                accCommonTablesDAO.saveBatchDocumentMapping(documentMap);
            }
            batchQty--;

            if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 

                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", 1);
                documentMap.put("serialmapid", jSONObject.getString("purchaseserialid"));
                documentMap.put("documentid", documentId);
                documentMap.put("transactiontype", "27");//This is GRN Type Tranction  
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    documentMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    documentMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }


                //   accCommonTablesDAO.saveSerialDocumentMapping(documentMap);
                KwlReturnObject krObj = accCommonTablesDAO.saveSerialDocumentMapping(documentMap);

                SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) krObj.getEntityList().get(0);
                if (jSONObject.has("customfield")) {
                    String customfield = jSONObject.getString("customfield");
                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        HashMap<String, Object> DOMap = new HashMap<String, Object>();
                        JSONArray jcustomarray = new JSONArray(customfield);

                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "SerialDocumentMapping");
                        customrequestParams.put("moduleprimarykey", "SerialDocumentMappingId");
                        customrequestParams.put("modulerecid", serialDocumentMapping.getId());
                        customrequestParams.put("moduleid", Constants.SerialWindow_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        DOMap.put("id", serialDocumentMapping.getId());
                        customrequestParams.put("customdataclasspath", Constants.Acc_Serial_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            DOMap.put("serialcustomdataref", serialDocumentMapping.getId());
                            accCommonTablesDAO.updateserialcustomdata(DOMap);
                        }
                    }
                }
                
                KwlReturnObject serialObj = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), jSONObject.getString("purchaseserialid"));
                NewBatchSerial newBatchSerial = (NewBatchSerial) serialObj.getEntityList().get(0);
                if (newBatchSerial != null) {
                    if (newBatchSerial.getQuantitydue() == 0) {
                        throw new AccountingException(newBatchSerial.getSerialname() + " is Already being used so you can not use it Again");
                    }
                }
                HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                serialUpdateQtyMap.put("qty", "-1");
                serialUpdateQtyMap.put("lockquantity", "-1");
                serialUpdateQtyMap.put("id", jSONObject.getString("purchaseserialid"));
                accCommonTablesDAO.saveSerialAmountDue(serialUpdateQtyMap);
             
                // Code to Send Serial Numbers to Inventory
                if (newBatchSerial != null) {
                    if (isWarehouseForProduct && isLocationForProduct) {
                        smd.addSerialName(newBatchSerial.getSerialname());
                    }
                }

            } else {
                batchQty = 0;
            }
        }
        if (isWarehouseForProduct && isLocationForProduct) {
            for (Map.Entry<Store, StockMovement> entry : storeWiseStockMovement.entrySet()) {
                stockMovementsList.add(entry.getValue());
            }
        }
         if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateInventoryTab() && !stockMovementsList.isEmpty()) {

             if (isAssemblyProduct) {
                 /**
                  * For Assembly product do not remove its StockMovement IN
                  * Entry Create StockMovement Out Entry for Assembly Product.
                  *
                  */
                 stockMovementService.addOrUpdateBulkStockMovementForWorkOrder(workOrder.getCompany(), documentId, stockMovementsList, false);
             } else {

                 /**
                  * For work order, in StockMovement OUT entry of Inventory item
                  * is created when stock is consumed on completion of any task.
                  * (SDP-11255)
                  */
                 stockMovementService.addOrUpdateBulkStockMovementForWorkOrder(workOrder.getCompany(), documentId, stockMovementsList,false);

             }
             stockMovementsList.clear();
         }

    }
    
    @Override
    public JSONObject getJOBWOKINProductDetails(HashMap<String,Object> requestMap) throws JSONException, ServiceException, SessionExpiredException {

        JSONArray jArr = new JSONArray();
        JSONObject jobj =new JSONObject();
        String productid =(String) requestMap.get("productid");
        String purchaseorderid =(String) requestMap.get("purchaseorderid");
     
	
	//boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
         Boolean isMRPJOBWORKIN = (requestMap.containsKey("isMRPJOBWORKIN") && requestMap.get("isMRPJOBWORKIN") != null )? false:Boolean.parseBoolean((String) requestMap.get("isMRPJOBWORKIN"));
          String companyid = (String) requestMap.get(Constants.companyKey);
                  
        
          
           KwlReturnObject result = accProductDaoObj.getProducts(requestMap);
            List list =  result.getEntityList();
            int count = result.getRecordTotalCount();
            for (Object object : list) {
                Object[] row = (Object[]) object;
                Product product = (Product) row[0];
                JSONObject obj = new JSONObject();
                String productType = "";
                productType = (product.getProducttype() != null ? product.getProducttype().getName() : "");
		
                obj.put("productid", product.getID());
                obj.put("hasAccess", product.isIsActive());
                obj.put("productname", product.getName());
                obj.put("desc", product.getDescription());
                obj.put("isAsset", product.isAsset());
                obj.put("supplierpartnumber", StringUtil.isNullOrEmpty(product.getSupplier()) ? "" : product.getSupplier());
                obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
                obj.put("minorderingquantity", product.getMinOrderingQuantity());
                obj.put("maxorderingquantity", product.getMaxOrderingQuantity());
                obj.put("reorderQuantity", product.getReorderQuantity());
                obj.put("uomschematypeid", product.getUomSchemaType() != null ? product.getUomSchemaType().getID() : "");
                UnitOfMeasure uom = product.getUnitOfMeasure();
                UnitOfMeasure purchaseuom = product.getPurchaseUOM();
                UnitOfMeasure salesuom = product.getSalesUOM();
                UnitOfMeasure orderingUoM = product.getOrderingUOM();
                UnitOfMeasure transferingUoM = product.getTransferUOM();
                obj.put("uomid", uom == null ? "" : uom.getID());
                obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
                obj.put("purchaseuom", purchaseuom == null ? "" : purchaseuom.getID());
                obj.put("salesuom", salesuom == null ? "" : salesuom.getID());
                obj.put("salesuomname", salesuom == null ? "" : salesuom.getNameEmptyforNA());
                obj.put("purchaseuomname", purchaseuom == null ? "" : purchaseuom.getNameEmptyforNA());
                obj.put("orderinguomname", orderingUoM == null ? "" : orderingUoM.getNameEmptyforNA());
                obj.put("orderinguomid", orderingUoM == null ? "" : orderingUoM.getID());
                obj.put("transferinguomname", transferingUoM == null ? "" : transferingUoM.getNameEmptyforNA());
                obj.put("transferinguomid", transferingUoM == null ? "" : transferingUoM.getID());
                obj.put("stockpurchaseuomvalue", (product.getPackaging() != null && purchaseuom != null) ? product.getPackaging().getStockUomQtyFactor(purchaseuom) : 1);
                obj.put("stocksalesuomvalue", (product.getPackaging() != null && salesuom != null) ? product.getPackaging().getStockUomQtyFactor(salesuom) : 1);
                obj.put("multiuom", product.isMultiuom());
                obj.put("isLocationForProduct", product.isIslocationforproduct());
                obj.put("isWarehouseForProduct", product.isIswarehouseforproduct());
                obj.put("isRowForProduct", product.isIsrowforproduct());
                obj.put("isRackForProduct", product.isIsrackforproduct());
                obj.put("isBinForProduct", product.isIsbinforproduct());
                obj.put("isBatchForProduct", product.isIsBatchForProduct());
                obj.put("isSerialForProduct", product.isIsSerialForProduct());
                obj.put("isSKUForProduct", product.isIsSKUForProduct());
                obj.put("isRecyclable", product.isRecyclable());
                obj.put("recycleQuantity", product.getRecycleQuantity());
                obj.put("isWastageApplicable", product.isWastageApplicable());
                obj.put("purchaseacctaxcode", ((product.getPurchaseAccount() != null && (!StringUtil.isNullOrEmpty(product.getPurchaseAccount().getTaxid()))) ? product.getPurchaseAccount().getTaxid() : ""));
                obj.put("salesacctaxcode", ((product.getSalesAccount() != null && (!StringUtil.isNullOrEmpty(product.getSalesAccount().getTaxid()))) ? product.getSalesAccount().getTaxid() : ""));
                obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : ""));
                obj.put("salesretaccountid", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getID() : ""));
                obj.put("location", (product.getLocation() != null ? product.getLocation().getId() : ""));
                obj.put("warehouse", (product.getWarehouse() != null ? product.getWarehouse().getId() : ""));
                KwlReturnObject purchase = accProductDaoObj.getProductPrice(product.getID(), true, null, "", "");
                obj.put("purchaseprice", purchase.getEntityList().get(0));
                KwlReturnObject sales = accProductDaoObj.getProductPrice(product.getID(), false, null, "", "");
                obj.put("saleprice", sales.getEntityList().get(0));
                
                KwlReturnObject companyObj1 = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), purchaseorderid);
                 PurchaseOrder purchaseOrder = (PurchaseOrder) companyObj1.getEntityList().get(0);
                 if(purchaseOrder != null){
                    obj.put("quantity",purchaseOrder.getProductquantity());
                    obj.put("baseuomquantity",purchaseOrder.getProductquantity());
                    obj.put("uomid", product.getUnitOfMeasure() == null ? "" : product.getUnitOfMeasure().getID());
                    obj.put("baseuomname", product.getUnitOfMeasure() == null ? "" : product.getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("multiuom", false); // Currently Not Handling for Multi UOM
                    obj.put("baseuomrate", 1);
                    obj.put("prdiscount", 0);
                    obj.put("discountispercent", 1);
                    obj.put("linkto", "");
                    obj.put("linkid", "");
                    obj.put("linktype", -1);
                     
                 }
                 
//                KwlReturnObject result = accProductObj.getQuantity(product.getID());
//                obj.put("quantity", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));

//                KwlReturnObject result2 = accProductObj.getAssemblyLockQuantity(product.getID());//get the lock quantity of assembly type of product locked in SO
//                Double assmblyLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));

//                KwlReturnObject result1 = accProductObj.getLockQuantity(product.getID());
//                Double SoLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));
//
//                obj.put("lockquantity", assmblyLockQuantity + SoLockQuantity);
//
//                KwlReturnObject result4 = accProductObj.getVendorConsignedQuantity(product.getID());
//                obj.put("venconsignuomquantity", (result4.getEntityList().get(0) == null ? 0 : result4.getEntityList().get(0)));
//
//                KwlReturnObject result5 = accProductObj.getConsignedQuantity(product.getID());
//                obj.put("consignquantity", (result5.getEntityList().get(0) == null ? 0 : result5.getEntityList().get(0)));

                obj.put("shelfLocation", (product.getShelfLocation() != null ? product.getShelfLocation().getShelfLocationValue() : ""));
                obj.put("location", (product.getLocation() != null ? product.getLocation().getId() : ""));
                obj.put("warehouse", (product.getWarehouse() != null ? product.getWarehouse().getId() : ""));
                String type = "";
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
                if (product.isAsset()) {   //For Fixed Asset Group, type will be "Asset"
                    obj.put("type", "Asset");
                } else {
                    obj.put("type", type);
                }
                obj.put("pid", product.getProductid());

                double orderToStockUOMFactor = 1;
                double transferToStockUOMFactor = 1;
                String packaging = "";
                Packaging prodPackaging = product.getPackaging();
                if (prodPackaging != null) {
                    orderToStockUOMFactor = prodPackaging.getStockUomQtyFactor(product.getOrderingUOM());
                    transferToStockUOMFactor = prodPackaging.getStockUomQtyFactor(product.getTransferUOM());
                    packaging = prodPackaging.toString();
                }
                obj.put("orderToStockUOMFactor", orderToStockUOMFactor);
                obj.put("transferToStockUOMFactor", transferToStockUOMFactor);
                obj.put("packaging", packaging);
                obj.put("packagingid", prodPackaging != null ? prodPackaging.getId() : "");
                obj.put("uomschematype", product.getUomSchemaType()!=null?product.getUomSchemaType().getID():"");
                obj.put("ismultipleuom", product.isMultiuom());
                obj.put("productweightperstockuom",  product.getProductWeightPerStockUom());
                obj.put("productweightincludingpakagingperstockuom", product.getProductWeightIncludingPakagingPerStockUom());
                obj.put("productvolumeperstockuom",product.getProductVolumePerStockUom());
                obj.put("productvolumeincludingpakagingperstockuom",product.getProductVolumeIncludingPakagingPerStockUom());

                jArr.put(obj);
            }
            
        jobj.put("data", jArr);
        jobj.put("totalCount", count);
        return jobj;
    }
     
//    @Override
//    public JSONObject getWorkOrderProducedQtyDetails(Map<String, Object> requestParams) throws ServiceException {
//        JSONObject Jobj = new JSONObject();
//        JSONArray jArr = new JSONArray();
//        KwlReturnObject kmsg = null;
//        try {
//            String workOrderID = "";
//            if (requestParams.containsKey(WorkOrder.WOID) && requestParams.get(WorkOrder.WOID) != null) {
//                workOrderID = requestParams.get(WorkOrder.WOID).toString();
//            }
//            if(!StringUtil.isNullOrEmpty(workOrderID)){
//                KwlReturnObject result = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), workOrderID);
//                WorkOrder workOrder = (WorkOrder) result.getEntityList().get(0);
//                  Set<WorkOrderComponentDetails> workOrderComponentDetailsSet = workOrder.getComponentDetails();
//                    for (WorkOrderComponentDetails workOrderComponentDetails : workOrderComponentDetailsSet) {
//                        String consumptionDetails = workOrderComponentDetails.getConsumptionDetails();
//                        Product product=workOrderComponentDetails.getParentProduct();
//                        JSONObject jObj=AccProductService.getProductDetailJSON(product, requestParams);
//                        jObj.put("workorderid", workOrderID);
//                        jObj.put("workorderdetailid", workOrderID);
//                        jObj.put("producedquantity", workOrderComponentDetails.getProducedQuantity());
//                        
//                        boolean parentProductCheck=(product != null && workOrder.getProductID() != null ) ?  product.getID().equalsIgnoreCase(workOrder.getProductID().getID()) : false;
//                        if (parentProductCheck && !StringUtil.isNullOrEmpty(consumptionDetails) && workOrderComponentDetails.getProduct() != null ) {
//                            JSONObject jobj = new JSONObject(consumptionDetails);
//                            String producedqtydetails = "";
//                            if (jobj.has("producedqtydetails")) {
//                                producedqtydetails = (String) jobj.getString("producedqtydetails");
//                            }
//                            if(! StringUtil.isNullOrEmpty(producedqtydetails)){
//                                jObj.put("producedqtydetails", producedqtydetails);
//                            }
//                            
//                            jArr.put(jObj);
//                        }
//                    }
//                
////                Product product=workOrder.getProductID() != null ? workOrder.getProductID() : null ;
////                if(product != null){
////                   JSONObject jObj=AccProductService.getProductDetailJSON(product, requestParams);
////                   jObj.put("quantity", workOrder.getQuantity());
////                   jArr.put(jObj);
////                }
//            }
//            Jobj.put("data", jArr);
//        } catch(Exception ex) {
//            Logger.getLogger(AccWorkOrderServiceImplCMN.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return Jobj;
//    }

    /**
     * create Work Order details JSON
     * @param request
     * @param companyid
     * @param WOID
     * @param FieldMap
     * @param replaceFieldMap
     * @param DimensionFieldMap
     * @return
     * @throws ServiceException 
     */
    @Override
    public JSONArray getWODetailsItemJSON(HttpServletRequest request, String companyid, String WOID, HashMap<String, Integer> FieldMap, HashMap<String, String> replaceFieldMap, HashMap<String, Integer> DimensionFieldMap) throws ServiceException {
        JSONArray returnJarr = new JSONArray();
        try {
            //get Work Order object
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), WOID);
            WorkOrder workOrder = (WorkOrder) objItr.getEntityList().get(0);
            //get user date formatter
            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(request);//User Date Formatter
            
            JSONObject detailsTableData = new JSONObject();
            //Details Table - Component Availability -- Starts
            JSONArray componentAvailabilityDetailsTableArr = new JSONArray();
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            requestParams.put("bills", WOID);
            requestParams.put("isFromWO", true);
            JSONArray jsonArray = accWorkOrderServiceDAOObj.getWorkOrderComponentDetails(requestParams);
            //Iterate component availiability details
            for(int componentCnt = 0; componentCnt < jsonArray.length(); componentCnt++){
                JSONObject componentAvailabilityDetailsJobj = new JSONObject();
                JSONObject componentDetailsJobj = jsonArray.getJSONObject(componentCnt);
                //put details in json object with respective keys
                componentAvailabilityDetailsJobj.put(CustomDesignerConstants.SrNo, componentCnt+1);
                componentAvailabilityDetailsJobj.put(CustomDesignerConstants.IN_ProductCode, componentDetailsJobj.optString("pid", ""));
                componentAvailabilityDetailsJobj.put(CustomDesignerConstants.ProductName, componentDetailsJobj.optString(WorkOrder.PRODUCTNAME, ""));
                componentAvailabilityDetailsJobj.put(CustomDesignerConstants.ProductDescription, componentDetailsJobj.optString(WorkOrderComponentDetails.PARAM_PRODUCT_DESC, ""));
                componentAvailabilityDetailsJobj.put(CustomDesignerConstants.PRODUCT_TYPE, componentDetailsJobj.optString(WorkOrderComponentDetails.PARAM_PRODUCT_TYPE_NAME, ""));
                componentAvailabilityDetailsJobj.put(CustomDesignerConstants.BALANCE_QUANTITY, componentDetailsJobj.optString(WorkOrderComponentDetails.PARAM_AVAILABLE_QUANTITY, ""));
                componentAvailabilityDetailsJobj.put(CustomDesignerConstants.BLOCK_QUANTITY, componentDetailsJobj.optString(WorkOrderComponentDetails.PARAM_BLOCKED_QUANTITY, ""));
                componentAvailabilityDetailsJobj.put(CustomDesignerConstants.REQUIRED_QUANTITY, componentDetailsJobj.optString(WorkOrderComponentDetails.PARAM_REQUIRED_QUANTITY, ""));
                componentAvailabilityDetailsJobj.put(CustomDesignerConstants.MIN_PERCENT_QUANTITY_REQUIRED, componentDetailsJobj.optString("minpercentquantity", ""));
                componentAvailabilityDetailsJobj.put(CustomDesignerConstants.BLOCKED_QTY_BY_OTHER_ORDERS, componentDetailsJobj.optString("outstandingquantity", ""));
                componentAvailabilityDetailsJobj.put(CustomDesignerConstants.SHORTFALL_QUANTITY, componentDetailsJobj.optString("shortfallquantity", ""));
                componentAvailabilityDetailsJobj.put(CustomDesignerConstants.ORDER_QUANTITY, componentDetailsJobj.optString("orderquantity", ""));
                //get warehouse details
                String warehouseName = "";
                if(!StringUtil.isNullOrEmpty(componentDetailsJobj.optString("warehouse", ""))){
                    KwlReturnObject warehouseObj = accountingHandlerDAOobj.getObject(Store.class.getName(), componentDetailsJobj.optString("warehouse", ""));
                    Store store = (Store) warehouseObj.getEntityList().get(0);
                    warehouseName = store.getFullName();
                }
                //get location details
                String locationName = "";
                if(!StringUtil.isNullOrEmpty(componentDetailsJobj.optString("location", ""))){
                    KwlReturnObject locationObj = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), componentDetailsJobj.optString("location", ""));
                    InventoryLocation inventoryLocation = (InventoryLocation) locationObj.getEntityList().get(0);
                    locationName = inventoryLocation.getName();
                }
                //put warehouse and location details in json object
                componentAvailabilityDetailsJobj.put(CustomDesignerConstants.WAREHOUSE, warehouseName);
                componentAvailabilityDetailsJobj.put(CustomDesignerConstants.LOCATION, locationName);
                //put component details json object in array
                componentAvailabilityDetailsTableArr.put(componentAvailabilityDetailsJobj);
            }
            //put component details json array in details table object with key
            detailsTableData.put("component_availability", componentAvailabilityDetailsTableArr);
            //Details Table - Component Availability -- Ends
            
            //Details Table - Tasks and Consumption -- Starts
            JSONArray taskDetailsTableArr = new JSONArray();
            JSONArray consumptionDetailsTableArr = new JSONArray();
            JSONArray checklistTableArr = new JSONArray();
            JSONObject checklistTableData = new JSONObject();
            requestParams.clear();
            requestParams.put("companyid", companyid);
            requestParams.put(Constants.userdf, df);
            requestParams.put("userid", workOrder.getCreatedBy().getUserID());
            requestParams.put("projectid", workOrder.getProjectId());
            requestParams.put("workorderid", WOID);
            JSONObject woTaskDetailsJobj = accWorkOrderServiceDAOObj.getTaskDetailsOfworkOrder(requestParams);
            Map<String, String> taskDetailsMap = new LinkedHashMap<String, String>();
            Map<String, String> subTaskCountMap = new LinkedHashMap<String, String>();
            JSONArray taskBreak = new JSONArray();
            JSONArray taskTableHeader = new JSONArray();
            int taskCount = 0;
            String parentTask = "", parentTaskId = "";
            //if task count is more than 0, it means tasks available for work order
            if(woTaskDetailsJobj.getInt("count") > 0){
                JSONObject taskDetailsData = woTaskDetailsJobj.getJSONObject("data");
                JSONArray taskDetailsJarr = taskDetailsData.getJSONArray("data");
                
                JSONArray checklistHeaderArr = new JSONArray();
                JSONArray checklistDetailsArr = new JSONArray();
                //Iterate task details
                for(int taskCnt = 0; taskCnt < taskDetailsJarr.length(); taskCnt++){
                    JSONObject taskDetailsJobj = new JSONObject();
                    //get task details
                    JSONObject taskDetails = taskDetailsJarr.getJSONObject(taskCnt);
                    String parent = taskDetails.optString("parent", "");
                    boolean isParent = taskDetails.optBoolean("isparent", false);
                    int level = taskDetails.optInt("level", 0);
                    
                    if(level == 0){
                        taskCount = 0;
                        parentTask = taskDetails.optString("taskname", "");
                        parentTaskId = taskDetails.optString("id", "");
                        taskDetailsMap.put(parentTaskId, parentTask);
                        subTaskCountMap.put(parentTaskId, taskCount+"");
                    } else{
                        taskCount++;
                        subTaskCountMap.put(parentTaskId, taskCount+"");
                    }
                    
                    String taskName = taskDetails.optString("taskname", "");                    
                    String duration = taskDetails.optString("duration", "");
                    String startdate = taskDetails.optString("startdate", "");
                    String enddate = taskDetails.optString("enddate", "");
                    String skills = taskDetails.optString("skills", "");
                    String notes = taskDetails.optString("notes", "");
                    notes = notes.replaceAll("!NL!","\n");
                    notes = notes.replaceAll("\n","<br>");
                    //product details
                    String productid = taskDetails.optString("productid", "");
                    String productname = taskDetails.optString("productname", "");
                    //get product details
                    KwlReturnObject productObj = accountingHandlerDAOobj.getObject(Product.class.getName(), productid);
                    Product product = (Product) productObj.getEntityList().get(0);
                    String productDescription = "", productCode = "", productType = "";
                    //if productis available then fetch details of product
                    if(product != null){
                        productDescription = product.getDescription();
                        productCode = product.getProductid();
                        productType = product.getProducttype().getName();
                    }
                    
                    String process = taskDetails.optString("process", "");
                    double progress = taskDetails.optDouble("percentcomplete", 0);
                    String progressStr = taskDetails.optString("percentcomplete", "0");
                    //get resource names
                    String resourceNames = "";
                    JSONArray resourceDetailsJarr = taskDetails.optJSONArray("resourcedetails");
                    for(int rdCnt = 0; rdCnt < resourceDetailsJarr.length(); rdCnt++){
                        JSONObject resourceDetails = resourceDetailsJarr.getJSONObject(rdCnt);
                        if(!StringUtil.isNullOrEmpty(resourceDetails.optString("resourcename", ""))){
                            resourceNames += resourceDetails.optString("resourcename", "") + ", ";
                        }
                    }
                    //remove extra comma(, ) from ending if resource name available
                    if(!StringUtil.isNullOrEmpty(resourceNames)){
                        resourceNames = resourceNames.substring(0, resourceNames.length() - 2);
                    }
                    JSONArray checklistJarr = taskDetails.optJSONArray("qcdetails");
                    
                    if(checklistJarr.length() > 0){
                        String checklistHeader = "";
                        JSONObject checklistHeaderJobj = new JSONObject();
                        JSONObject checklistDetailsJobj = new JSONObject();

                        checklistDetailsJobj.put("taskName", taskName);
                        for(int clCnt = 0; clCnt < checklistJarr.length(); clCnt++){
                            JSONObject checklistDetails = checklistJarr.getJSONObject(clCnt);
                            String qcParameterName = checklistDetails.optString("qcpname", "");
                            String qcParameterValue = checklistDetails.optString("qcactval", "0");
                            String qcParameterStatus = checklistDetails.optString("qcstatus", "");
                            String qcParameterDesc = checklistDetails.optString("qcdesc", "");
                            if(!StringUtil.isNullOrEmpty(qcParameterStatus)){
                                qcParameterStatus = (qcParameterStatus.equals("0") ? "Fail" : "Pass");
                            }
                            if(!StringUtil.isNullOrEmpty(qcParameterName)){
                                checklistHeader += qcParameterName + "!##";
                                //put checklist details in json object with respective keys
                                checklistDetailsJobj.put(qcParameterName+"_rating", qcParameterValue);
                                checklistDetailsJobj.put(qcParameterName+"_status", qcParameterStatus);
                                checklistDetailsJobj.put(qcParameterName+"_description", qcParameterDesc);
                            }
                        }
                        checklistDetailsJobj.put("headers", checklistHeader);
                        checklistDetailsArr.put(checklistDetailsJobj);
                    }
                    //put task details in json object with respective keys
                    taskDetailsJobj.put(CustomDesignerConstants.SrNo, (taskCnt + 1));
                    taskDetailsJobj.put(CustomDesignerConstants.TASK_NAME, taskName);
                    taskDetailsJobj.put(CustomDesignerConstants.IN_ProductCode, productCode);
                    taskDetailsJobj.put(CustomDesignerConstants.ProductName, productname);
                    taskDetailsJobj.put(CustomDesignerConstants.ProductDescription, productDescription);
                    taskDetailsJobj.put(CustomDesignerConstants.PRODUCT_TYPE, productType);
                    taskDetailsJobj.put(CustomDesignerConstants.NOTES, notes);
                    taskDetailsJobj.put(CustomDesignerConstants.DURATION, duration);
                    taskDetailsJobj.put(CustomDesignerConstants.START_DATE, startdate);
                    taskDetailsJobj.put(CustomDesignerConstants.END_DATE, enddate);
                    taskDetailsJobj.put(CustomDesignerConstants.PROGRESS, progressStr);
                    taskDetailsJobj.put(CustomDesignerConstants.SKILLS, skills);
                    taskDetailsJobj.put(CustomDesignerConstants.PROCESSES, process);
                    taskDetailsJobj.put(CustomDesignerConstants.RESOURCE_NAMES, resourceNames);
                    //put task details json object in array
                    if(!isParent || (isParent  && level != 0)){
                        taskDetailsTableArr.put(taskDetailsJobj);
                    }
                    /**
                     * Consumption details
                     */
                    if(product != null && Double.compare(progress, 100) == 0){
                        requestParams.clear();
                        requestParams.put("productId", product.getID());
                        requestParams.put("projectId", workOrder.getProjectId());
                        requestParams.put(Constants.companyKey, companyid);
                        JSONArray consumptionProductDetailsArr = AccProductService.getProductsForProject(requestParams);
                        if(consumptionProductDetailsArr.length() > 0){
                            JSONObject detailsObj = consumptionProductDetailsArr.getJSONObject(0);
                            JSONObject consumptionDetails = new JSONObject(detailsObj.optString("consumptiondetails", "{}"));
                            consumptionDetails.optString("");

                            JSONObject consumptionDetailsJobj = new JSONObject();
                            consumptionDetailsJobj.put(CustomDesignerConstants.SrNo, consumptionDetailsTableArr.length() + 1);
                            consumptionDetailsJobj.put(CustomDesignerConstants.IN_ProductCode, consumptionDetails.optString("pid", ""));
                            consumptionDetailsJobj.put(CustomDesignerConstants.ProductName, URLDecoder.decode(consumptionDetails.optString("productname", ""), "UTF-8"));
                            consumptionDetailsJobj.put(CustomDesignerConstants.PRODUCT_TYPE, consumptionDetails.optString("productType", ""));
                            consumptionDetailsJobj.put(CustomDesignerConstants.ProductDescription, consumptionDetails.optString("desc", ""));
                            consumptionDetailsJobj.put(CustomDesignerConstants.BALANCE_QUANTITY, consumptionDetails.optString(WorkOrderComponentDetails.PARAM_AVAILABLE_QUANTITY, ""));
                            consumptionDetailsJobj.put(CustomDesignerConstants.REQUIRED_QUANTITY, consumptionDetails.optString(WorkOrderComponentDetails.PARAM_REQUIRED_QUANTITY, ""));
                            consumptionDetailsJobj.put(CustomDesignerConstants.BLOCK_QUANTITY, consumptionDetails.optString(WorkOrderComponentDetails.PARAM_BLOCKED_QUANTITY, ""));
                            consumptionDetailsJobj.put(CustomDesignerConstants.ACTUAL_QUANTITY, consumptionDetails.optString("actualquantity", ""));
                            consumptionDetailsJobj.put(CustomDesignerConstants.REJECTED_QUANTITY, consumptionDetails.optString(WorkOrderComponentDetails.PARAM_REJECTED_QUANTITY, ""));
                            consumptionDetailsJobj.put(CustomDesignerConstants.WASTE_QUANTITY, consumptionDetails.optString(WorkOrderComponentDetails.PARAM_WASTE_QUANTITY, ""));
                            consumptionDetailsJobj.put(CustomDesignerConstants.RECYCLE_QTY, consumptionDetails.optString(WorkOrderComponentDetails.PARAM_RECYCLE_QUANTITY, ""));
                            consumptionDetailsJobj.put(CustomDesignerConstants.PRODUCED_QUANTITY, consumptionDetails.optString("producedquantity", ""));

                            JSONArray batchDetailsJArr = new JSONArray(consumptionDetails.optString("batchdetails", "[]"));

                            String warehouseName = "";
                            String locationName = "";
                            String batch = "";
                            String serial = "";
                            for(int ind = 0; ind < batchDetailsJArr.length(); ind++){
                                JSONObject batchDetailsJObj = batchDetailsJArr.getJSONObject(ind);
                                //get warehouse details
                                if(!StringUtil.isNullOrEmpty(batchDetailsJObj.optString("warehouse", ""))){
                                    KwlReturnObject warehouseObj = accountingHandlerDAOobj.getObject(Store.class.getName(), batchDetailsJObj.optString("warehouse", ""));
                                    Store store = (Store) warehouseObj.getEntityList().get(0);
                                    warehouseName += store.getFullName() + "!##";
                                }
                                //get location details
                                if(!StringUtil.isNullOrEmpty(batchDetailsJObj.optString("location", ""))){
                                    KwlReturnObject locationObj = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), batchDetailsJObj.optString("location", ""));
                                    InventoryLocation inventoryLocation = (InventoryLocation) locationObj.getEntityList().get(0);
                                    locationName += inventoryLocation.getName() + "!##";
                                }
                                if(!StringUtil.isNullOrEmpty(batchDetailsJObj.optString("batchname", ""))){
                                    batch += batchDetailsJObj.optString("batchname", "") + "!##";
                                }
                                if(!StringUtil.isNullOrEmpty(batchDetailsJObj.optString("serialno", ""))){
                                    serial += batchDetailsJObj.optString("serialno", "") + "!##";
                                }
                            }
                            if(!StringUtil.isNullOrEmpty(warehouseName)){
                                warehouseName = warehouseName.substring(0, warehouseName.length() - 3);
                            }
                            if(!StringUtil.isNullOrEmpty(locationName)){
                                locationName = locationName.substring(0, locationName.length() - 3);
                            }
                            if(!StringUtil.isNullOrEmpty(batch)){
                                batch = batch.substring(0, batch.length() - 3);
                            }
                            if(!StringUtil.isNullOrEmpty(serial)){
                                serial = serial.substring(0, serial.length() - 3);
                            }

                            consumptionDetailsJobj.put(CustomDesignerConstants.WAREHOUSE, warehouseName);
                            consumptionDetailsJobj.put(CustomDesignerConstants.LOCATION, locationName);
                            consumptionDetailsJobj.put(CustomDesignerConstants.BatchNumber, batch);
                            consumptionDetailsJobj.put(CustomDesignerConstants.SerialNumber, serial);
                            //put Consumption details json object in array
                            consumptionDetailsTableArr.put(consumptionDetailsJobj);
                        }
                    }
                }
                checklistTableData.put("isChecklistTableData", true);
                checklistTableData.put("checklistTableData", checklistDetailsArr);
                returnJarr.put(checklistTableData);
            }
            
            int singleTaskCounter = 0;
            for(String key : subTaskCountMap.keySet()){
                if(!((String) subTaskCountMap.get(key)).equals("0")){
                    if(singleTaskCounter > 0){
                        taskBreak.put(singleTaskCounter);
                        taskTableHeader.put("");
                        singleTaskCounter = 0;
                    }
                    String taskNm = (String) taskDetailsMap.get(key);
                    taskBreak.put((String) subTaskCountMap.get(key));
                    taskTableHeader.put(taskNm);
                } else{
                    singleTaskCounter++;
                }
            }
            
            //put task details json array and other details for task table breaking in details table object with key
            detailsTableData.put("tasks", taskDetailsTableArr);
            detailsTableData.put("taskBreak", taskBreak);
            detailsTableData.put("taskTableHeader", taskTableHeader);
            //put Consumption details json array in details table object with key
            detailsTableData.put("consumption", consumptionDetailsTableArr);
            //Details Table - Tasks -- Ends
            
            detailsTableData.put("isDetailsTableData", true);
            returnJarr.put(detailsTableData);
            
            /**
             * Global details
             */
            String linkTo = "", linkDocNo = "", linkDocDate = "";
            //get linking details
            if(workOrder.getFromlinktype() == WorkOrder.SALESORDER){
                linkTo = Constants.SALESORDER;
                if(workOrder.getSalesOrder() != null){
                    linkDocNo = workOrder.getSalesOrder().getSalesOrderNumber();
                    linkDocDate = df.format(workOrder.getSalesOrder().getOrderDate());
                }
            } else if(workOrder.getFromlinktype() == WorkOrder.SALESCONTRACT){
                linkTo = Constants.SALESCONTRACT;
                if(workOrder.getSalesContractID() != null){
                    linkDocNo = workOrder.getSalesContractID().getContractNumber();
                    linkDocDate = df.format(workOrder.getSalesContractID().getOrderDate());
                }
            }
            //get work center details
            String workCenterName = "";
            for(WorkOrderWorkCenterMapping wcMapping : workOrder.getWorkcentermapping()){
                workCenterName += wcMapping.getWorkcentreid().getName() + ", ";
            }
            if(!StringUtil.isNullOrEmpty(workCenterName)){
                workCenterName = workCenterName.substring(0, workCenterName.length() - 2);
            }
            //get machine details
            String woMachineName = "";
            for(WorkOrderMachineMapping womMapping : workOrder.getMachinemapping()){
                woMachineName += womMapping.getMachineid().getMachineName() + ", ";
            }
            if(!StringUtil.isNullOrEmpty(woMachineName)){
                woMachineName = woMachineName.substring(0, woMachineName.length() - 2);
            }
            //get labour details
            String woLabourName = "";
            for(WorkOrderLabourMapping wolMapping : workOrder.getLabourmapping()){
                woLabourName += wolMapping.getLabourid().getFullName() + ", ";
            }
            if(!StringUtil.isNullOrEmpty(woLabourName)){
                woLabourName = woLabourName.substring(0, woLabourName.length() - 2);
            }
            //get batch number of Finished Good
            String batchName = "";
            //get batch details by documentid and transactiontype
            KwlReturnObject batchNumbersObject = accProductDaoObj.getBatchesByDocumentid(workOrder.getID(), Constants.WORK_ORDER_TRANSACTION_TYPE_IN);
            //iterate for all batch numbers
            for(int ind = 0; ind < batchNumbersObject.getEntityList().size(); ind++){
                Object[] batchDetailsObjArr = (Object[]) batchNumbersObject.getEntityList().get(0);
                //load product batch object
                KwlReturnObject NewProductBatchObj = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), batchDetailsObjArr[0].toString());
                NewProductBatch newProductBatch = (NewProductBatch) NewProductBatchObj.getEntityList().get(0);
                //get batch number and append with comma seperator
                batchName += (StringUtil.isNullOrEmpty(newProductBatch.getBatchname()) ? "" : newProductBatch.getBatchname() + ", ");
            }
            //remove extra comma and space from ending of batch number
            if(!StringUtil.isNullOrEmpty(batchName)){
                batchName = batchName.substring(0, batchName.length() - 2);
            }
            //put global details
            JSONObject summaryData = new JSONObject();
            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.WORK_ORDER_STATUS, workOrder.getWorkOrderStatus() != null ? workOrder.getWorkOrderStatus().getValue() : "");
            summaryData.put(CustomDesignerConstants.WORK_CENTER, workCenterName);
            summaryData.put(CustomDesignerConstants.MACHINES, woMachineName);
            summaryData.put(CustomDesignerConstants.LABOURS, woLabourName);
            summaryData.put(CustomDesignerConstants.LINK_TO, linkTo);
            summaryData.put(CustomDesignerConstants.LINK_DOCUMENT_NO, linkDocNo);
            summaryData.put(CustomDesignerConstants.LINK_DOCUMENT_DATE, linkDocDate);
            summaryData.put(CustomDesignerConstants.BatchNumber, batchName);
            //put global details json object in main json array
            returnJarr.put(summaryData);
        } catch (Exception e) {
            Logger.getLogger(AccWorkOrderServiceImplCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnJarr;
    }
    
}
