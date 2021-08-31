/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.WorkOrder;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.inspection.InspectionForm;
import com.krawler.inventory.model.inspection.InspectionFormDetails;
import com.krawler.inventory.model.ist.InterStoreTransferStatus;
import com.krawler.inventory.model.ist.WOCDetailISTMapping;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.StockMovementService;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.accProductImpl;
import com.krawler.spring.accounting.salesorder.accSalesOrderService;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.mrp.jobwork.AccJobWorkServiceImpl;
import com.krawler.spring.mrp.jobwork.JobWork;
import com.krawler.spring.mrp.labormanagement.LabourWorkCentreMapping;
import com.krawler.spring.mrp.machinemanagement.AccMachineManagementServiceImpl;
import com.krawler.spring.mrp.machinemanagement.Machine;
import com.krawler.spring.mrp.machinemanagement.MachineWorkCenterMapping;
import com.krawler.spring.mrp.machinemanagement.SubstituteMachineMapping;
import com.krawler.spring.mrp.routingmanagement.AccRoutingManagementService;
import com.krawler.spring.mrp.routingmanagement.RoutingTemplate;
import com.krawler.spring.mrp.workcentremanagement.AccWorkCentreServiceImpl;
import com.krawler.spring.mrp.workcentremanagement.MaterialWorkCentreMapping;
import com.krawler.spring.mrp.workcentremanagement.ProductWorkCentreMapping;
import com.krawler.spring.mrp.workcentremanagement.WorkCentre;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.mrp.labormanagement.AccLabourServiceDAO;
import com.krawler.spring.mrp.labormanagement.Labour;
import com.krawler.spring.mrp.labormanagement.ResourceCost;
import com.krawler.spring.mrp.labormanagement.accLabourDAO;
import com.krawler.spring.mrp.machinemanagement.*;
import com.krawler.utils.json.JSONException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

/**
 *
 * @author krawler
 */
public class AccWorkOrderServiceImpl implements AccWorkOrderServiceDAO {
    private MessageSource messageSource;
    private WorkOrderDAO workOrderDAOObj;
     private AccRoutingManagementService accRoutingManagementService;
     private accCompanyPreferencesDAO accCompanyPreferencesObj;
     private exportMPXDAOImpl exportDaoObj;
     private accProductDAO accProductDaoObj;
     private APICallHandlerService apiCallHandlerService;
     private AccountingHandlerDAO accountingHandlerDAOobj;
     private accAccountDAO accAccountDAOobj;
     private fieldDataManager fieldDataManagercntrl;
     private kwlCommonTablesDAO kwlCommonTablesDAOObj;
     private HibernateTransactionManager txnManager;
     private accJournalEntryDAO accJournalEntryobj;
     private accMasterItemsDAO accMasterItemsDAOObj;
     private AccCommonTablesDAO accCommonTablesDAOObj;
     private accSalesOrderService accSalesOrderServiceObj;
     private auditTrailDAO auditTrailObj;
     private accInvoiceDAO accInvoiceDAOobj;
     private accLabourDAO accLabourDAOObj;
     private AccCommonTablesDAO accCommonTablesDAO;
     private StockMovementService stockMovementService;
     private StockService stockService;
     private AccMachineManagementDAO accMachineManagementDAOObj;
    
    public AccCommonTablesDAO getAccCommonTablesDAO() {
        return accCommonTablesDAO;
    }

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public StockMovementService getStockMovementService() {
        return stockMovementService;
    }

    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public void setAccLabourDAOObj(accLabourDAO accLabourDAOObj) {
        this.accLabourDAOObj = accLabourDAOObj;
    }
 

  
    public void setAccInvoiceDAOobj(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setAccCommonTablesDAOObj(AccCommonTablesDAO accCommonTablesDAOObj) {
        this.accCommonTablesDAOObj = accCommonTablesDAOObj;
    }

    public void setAccMasterItemsDAOObj(accMasterItemsDAO accMasterItemsDAOObj) {
        this.accMasterItemsDAOObj = accMasterItemsDAOObj;
    }

    public void setAccSalesOrderServiceObj(accSalesOrderService accSalesOrderServiceObj) {
        this.accSalesOrderServiceObj = accSalesOrderServiceObj;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
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
    
    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }
    
    public AccMachineManagementDAO getAccMachineManagementDAOObj() {
        return accMachineManagementDAOObj;
    }

    public void setAccMachineManagementDAOObj(AccMachineManagementDAO accMachineManagementDAOObj) {
        this.accMachineManagementDAOObj = accMachineManagementDAOObj;
    }
    
    @Override
    public KwlReturnObject saveWorkOrder(Map<String, Object> requestParams) throws AccountingException,ServiceException {
        KwlReturnObject kwlRetObj = null;
        KwlReturnObject kmsg = null;
        KwlReturnObject kmsg1 = null;
        Locale requestcontextutilsobj=null;
        JSONObject jObj = null;
        String msg= "", auditMsg = "";
        boolean genpocheck=false;
        WorkOrder woObj = null;
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            if(requestParams.containsKey("requestcontextutilsobj")){
                requestcontextutilsobj=(Locale)requestParams.get("requestcontextutilsobj");
            }
            String companyid = requestParams.get("companyid").toString();
            String userid = requestParams.get("userid").toString();
            boolean isEdit = Boolean.parseBoolean(requestParams.get("isEdit").toString());
            boolean isBOMChanged = Boolean.parseBoolean(requestParams.get("isBOMChanged").toString());
            ArrayList<String> WOCodes = new ArrayList<>();
            ArrayList<String> WONames = new ArrayList<>();
            ArrayList<String> WOids = new ArrayList<>();
            boolean isWorkOrderIDAlreadyPresent = false;
             String sequenceformat = "", nextAutoNumber = "";
            String codeStr = "";
            JSONObject dataJobj = new JSONObject(StringUtil.DecodeText(requestParams.get("data").toString()));
            JSONArray dataArr = dataJobj.getJSONArray("data");
//            JSONArray jobWorkdataArr = dataJobj.getJSONArray("joborderdata");
            for(int recCount = 0; recCount < dataArr.length(); recCount++) {
                JSONObject dataObj = dataArr.getJSONObject(recCount);
                dataObj.put("companyid", companyid);
                dataObj.put("createdby", userid);
                dataObj.put("modifiedby", userid);
                String entrynumber = dataObj.getString(WorkOrder.WORKORDERID);
                sequenceformat = dataObj.getString(WorkOrder.SEQUENCEFORMAT);
                String woid = dataObj.optString("id","");
                synchronized (this) {

                    HashMap<String, Object> requestParams1 = new HashMap();
                    requestParams1.put("companyid", companyid);
                    requestParams1.put(WorkOrder.WORKORDERID, dataObj.getString(WorkOrder.WORKORDERID));
                    if (isEdit && !StringUtil.isNullOrEmpty(woid) && sequenceformat.equals("NA")) {
                        requestParams1.put("woid", woid);
                    }
                    kmsg1 = workOrderDAOObj.getWorkOrders(requestParams1);
                    if (kmsg1.getEntityList().size() > 0) {
                        isWorkOrderIDAlreadyPresent = true;
                    }
                    Date workOrderDate = null;
                    if (dataObj.has("workorderdate") && !StringUtil.isNullOrEmpty(dataObj.getString("workorderdate"))) {
                        workOrderDate = df.parse(dataObj.get("workorderdate").toString());
                    }
                    if (isWorkOrderIDAlreadyPresent) {
                        if (isEdit) {
                            nextAutoNumber = entrynumber;
                        }else{
//                            if (sequenceformat.equals("NA")) {
                                throw new AccountingException(messageSource.getMessage("mrp.qcreport.gridheader.workordername", null, requestcontextutilsobj) + " '<b>" + entrynumber + "</b> " + messageSource.getMessage("acc.field.alreadyexists.", null, requestcontextutilsobj));
//                            }
                            }

                        } else {
                        boolean seqformat_oldflag = StringUtil.getBoolean(dataObj.optString("seqformat_oldflag", "false"));
                        String nextAutoNoInt = "";
                        String datePrefix = "";
                        String dateafterPrefix = "";
                        String dateSuffix = "";
                        if (!sequenceformat.equals("NA")) {
                            if (seqformat_oldflag) {
                                nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_MRP_WORKORDER, sequenceformat);
                            } else {
                                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_MRP_WORKORDER, sequenceformat, seqformat_oldflag, workOrderDate);
                                nextAutoNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                                nextAutoNoInt = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                                datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                                dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                                dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part

                                dataObj.put(Constants.SEQFORMAT, sequenceformat);
                                dataObj.put(Constants.SEQNUMBER, nextAutoNoInt);
                                dataObj.put(Constants.DATEPREFIX, datePrefix);
                                dataObj.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                                dataObj.put(Constants.DATESUFFIX, dateSuffix);
                            }
                            entrynumber = nextAutoNumber;
                        }

                    }

                    if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                        List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.MRP_WORK_ORDER_MODULEID, entrynumber, companyid);
                        if (!list.isEmpty()) {
                            boolean isvalidEntryNumber = (Boolean) list.get(0);
                            String formatName = (String) list.get(1);
                            if (!isvalidEntryNumber) {
                                throw new AccountingException("#DuplicateException#"+messageSource.getMessage("acc.common.enterdocumentnumber", null, requestcontextutilsobj) + " <b>" + entrynumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, requestcontextutilsobj) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, requestcontextutilsobj) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, requestcontextutilsobj));
                            }
                        }
                    }

                }
//                dataObj.put(WorkOrder.WORKORDERID, entrynumber);
                dataObj.put("autogenerated", nextAutoNumber.equals(entrynumber));
                kmsg = workOrderDAOObj.saveWorkOrder(dataObj);
                WorkOrder workorderObj=(WorkOrder)kmsg.getEntityList().get(0);
                
                KwlReturnObject kwl = null;
                Map<String, Object> deleteParams = new HashMap();
                deleteParams.put(WorkOrder.WOID, workorderObj.getID());
                
//                ****************************Save one to many labour mapping *************************
                if (isEdit) {
                    deleteParams.put(WorkOrder.POJO, WorkOrderLabourMapping.POJONAME);
                    deleteParams.put(WorkOrder.ATTRIBUTE, WorkOrderLabourMapping.ATTRIBUTENAME);
                    kwl = workOrderDAOObj.deleteWorkOrderMappings(deleteParams);
                }
                if (dataObj.has(WorkOrder.LABOURID) && !StringUtil.isNullOrEmpty(dataObj.getString(WorkOrder.LABOURID))) {
                    Map<String, Object> LabourMappingDataMap = new HashMap();
                    LabourMappingDataMap.put(WorkOrder.LABOURID, dataObj.getString(WorkOrder.LABOURID));
                    LabourMappingDataMap.put("workorderObj", workorderObj);
                    kwl = workOrderDAOObj.saveWorkOrderLabourMapping(LabourMappingDataMap);
                }
//                ****************************Save one to many labour mapping *************************
//                ****************************Save one to many machine mapping *************************
                if (isEdit) {
                    deleteParams.put(WorkOrder.POJO, WorkOrderMachineMapping.POJONAME);
                    deleteParams.put(WorkOrder.ATTRIBUTE, WorkOrderMachineMapping.ATTRIBUTENAME);
                    kwl = workOrderDAOObj.deleteWorkOrderMappings(deleteParams);
                }
                if (dataObj.has(WorkOrder.MACHINEID) && !StringUtil.isNullOrEmpty(dataObj.getString(WorkOrder.MACHINEID))) {
                    Map<String, Object> machineMappingDataMap = new HashMap();
                    machineMappingDataMap.put(WorkOrder.MACHINEID, dataObj.getString(WorkOrder.MACHINEID));
                    machineMappingDataMap.put("workorderObj", workorderObj);
                    kwl = workOrderDAOObj.saveWorkOrderMachineMapping(machineMappingDataMap);
                }
//                ****************************Save one to many machine mapping *************************
                 //****************************Save one to many WorkCenter mapping *************************
                if (isEdit) {
                    deleteParams.put(WorkOrder.POJO, WorkOrderWorkCenterMapping.POJONAME);
                    deleteParams.put(WorkOrder.ATTRIBUTE, WorkOrderWorkCenterMapping.ATTRIBUTENAME);
                    kwl = workOrderDAOObj.deleteWorkOrderMappings(deleteParams);
                }
                if (dataObj.has(WorkOrder.WORKCENTREID) && !StringUtil.isNullOrEmpty(dataObj.getString(WorkOrder.WORKCENTREID))) {
                    Map<String, Object> workcenterMappingDataMap = new HashMap();
                    workcenterMappingDataMap.put(WorkOrder.WORKCENTREID, dataObj.getString(WorkOrder.WORKCENTREID));
                    workcenterMappingDataMap.put("workorderObj", workorderObj);
                    kwl = workOrderDAOObj.saveWorkOrderWorkCenterMapping(workcenterMappingDataMap);
                }
                woObj = (WorkOrder) kmsg.getEntityList().get(0);
                if (isEdit) {
                    Map<String, Object> requestParams1 = new HashMap<String, Object>();
                    requestParams1.put("workorderid", woObj.getID());
                    requestParams1.put(Constants.companyKey, companyid);
                    requestParams1.put("isEdit", isEdit);
                    String workorderDefaultstatus =((woObj.getWorkOrderStatus() != null && woObj.getWorkOrderStatus().getDefaultMasterItem() != null) ?  woObj.getWorkOrderStatus().getDefaultMasterItem().getID() : "");
                    
                    /*
                     * at EDIT Work order Save time; 
                     * if WORK ORDER status is not PLANNED then Work order is STARTED.
                     */
                    if(!StringUtil.isNullOrEmpty(workorderDefaultstatus))
                    {
                        if(!workorderDefaultstatus.equals(Constants.defaultWOstatus_PLANNED))
                        {
                                requestParams1.put("isWOStart",true);
                        }else{
                                requestParams1.put("isWOStart",false);                        
                        }                    
                    }
                    workOrderDAOObj.deleteWorkOrderBatchSerialDetails(requestParams1);
                    if (isBOMChanged) {
                        deleteParams.put(WorkOrder.POJO, WorkOrderComponentDetails.POJONAME);
                        deleteParams.put(WorkOrder.ATTRIBUTE, WorkOrderComponentDetails.DB_WORKORDERID);
                        kwl = workOrderDAOObj.deleteWorkOrderMappings(deleteParams);
                    }
                }
                /*
                 * SAVE WORK ORDER COMPONENT DETAILS
                 */
                if (dataObj.has("details") && dataObj.get("details") != null) {
                    
//                    JSONArray detailArr = new JSONArray(dataObj.getString("details"));
//                    for (int detIndex = 0 ; detIndex < detailArr.length(); detIndex++) {
//                        JSONObject detJObj = detailArr.getJSONObject(detIndex);
//                        String batchStr =  detJObj.optString("batchdetails","");
//                        if (!StringUtil.isNullOrEmpty(batchStr)){
//                            HashMap detmap = new HashMap();
//                            detmap.put("df", null);
//                            detmap.put("companyid", requestParams.get("companyid"));
//                            detmap.put("userid", requestParams.get("userid"));
//                            saveWONewBatch(batchStr,detJObj.getString("productid"),detmap,);
//                        }
//                    }
                    Map<String,Object> workOrderComponentDetailsDataMap=new HashMap<>();
                    workOrderComponentDetailsDataMap.put("workorderid", woObj.getWorkOrderID());
                    workOrderComponentDetailsDataMap.put("workorderObj", woObj);
                    workOrderComponentDetailsDataMap.put("isEdit", isEdit);
                    /**
                     * Remove avlquantity as it shows up in batch serial
                     * window as balance quantity.
                     */
                    JSONArray detailArr = new JSONArray(dataObj.getString("details"));
                    for (int detIndex = 0; detIndex < detailArr.length(); detIndex++) {
                        JSONObject detJObj = detailArr.getJSONObject(detIndex);
                        String batchStr = detJObj.optString("batchdetails", "");
                        if (!StringUtil.isNullOrEmpty(batchStr)) {
                            JSONArray jArrBatchDetails = new JSONArray(batchStr);
                            for (int i = 0; i < jArrBatchDetails.length(); i++) {
                                JSONObject jSONObject = (JSONObject) jArrBatchDetails.get(i);
                                jSONObject.remove(Constants.avlQuantity);
                                if (StringUtil.isNullOrEmpty(jSONObject.optString(Constants.batchName))) {
                                    String productBatchId = accCommonTablesDAO.getpurchaseBatchIdForLocationWarehouseRowRackBin(jSONObject.getString("productid"), jSONObject.getString("location"), jSONObject.getString("warehouse"), jSONObject.optString("row", null), jSONObject.optString("rack", null), jSONObject.optString("bin", null), jSONObject.optString("batch", null));
                                    jSONObject.put(Constants.purchaseBatchId, productBatchId);
                                    jSONObject.put(Constants.documentBatchId, productBatchId);
                                }
                            }
                            detJObj.put(WorkOrderComponentDetails.BATCH_DETAILS, jArrBatchDetails.toString());
                        }
                    }
                    workOrderComponentDetailsDataMap.put("details", detailArr.toString());
                    KwlReturnObject woDetMsg = workOrderDAOObj.saveWorkOrderComponentDetails(workOrderComponentDetailsDataMap);
                    Set<WorkOrderComponentDetails> woDetSet = (Set<WorkOrderComponentDetails>) woDetMsg.getEntityList().get(0);
                    detailArr = new JSONArray(dataObj.getString("details"));
                    for(WorkOrderComponentDetails row : woDetSet) {
                        for (int detIndex = 0 ; detIndex < detailArr.length(); detIndex++) {
                            JSONObject detJObj = detailArr.getJSONObject(detIndex); 
                            String savedProductId = row.getProduct()!= null ? row.getProduct().getID() : "";
                            String jsonProductId = detJObj.optString("productid","");
                            String savedParentProductId = row.getParentProduct() != null ? row.getParentProduct().getID() : "";
                            String jsonParentProductId = detJObj.optString(Constants.parentProductId, "");
                            boolean genpocheckIndividual = detJObj.optBoolean("genpocheck", false);
                            if (genpocheckIndividual) {
                                genpocheck = genpocheckIndividual;
                            }
                            if (jsonProductId.equalsIgnoreCase(savedProductId) && jsonParentProductId.equals(savedParentProductId)) {
                                String batchStr = detJObj.optString("batchdetails", "");
                                if (!StringUtil.isNullOrEmpty(batchStr)) {
                                    HashMap detmap = new HashMap();
                                    JSONObject detJobj = new JSONObject();
                                    detJobj.put("df", "");
                                    detJobj.put("companyid", requestParams.get("companyid"));
                                    detJobj.put("userid", requestParams.get("userid"));
                                    detJobj.put("isEdit",isEdit);
//                                    saveWONewBatch(batchStr, detJObj.getString("productid"), detmap, row.getID());
                                    lockWorkOrderDeatilsBatch(batchStr, detJObj.getString("productid"), detJobj, row.getID());
                                }
                            }
                        }
                    }
                }
                //****************************Save one to many WorkCenter mapping *************************
                if (dataObj.has(WorkOrder.PROJECTID) && !StringUtil.isNullOrEmpty((String) dataObj.get(WorkOrder.PROJECTID))) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("companyid", woObj.getCompany().getCompanyID());
                    userData.put("userid", userid);
                    userData.put("projectname", woObj.getWorkOrderName());// Sending Work order name as Project Name
                    userData.put("isNewProject", "false");// As Updating the Existing Project
                    userData.put("isMasterProject", "false");// False for Work Order
                    userData.put("projectId", (String) dataObj.get(WorkOrder.PROJECTID));
                    userData.put("projectcode",woObj.getWorkOrderID());
                    userData.put("MRPWOStatus",(woObj.getWorkOrderStatus() != null && woObj.getWorkOrderStatus().getDefaultMasterItem() != null) ?  woObj.getWorkOrderStatus().getDefaultMasterItem().getID() : "");
                    JSONObject pmjsonObj = accRoutingManagementService.createOrUpdateProjectRest(userData);
                    /*
                      ERP-40195 : While creating WO, we create PM Project too. Due to some reason, system fails to create PM Project
                      we will rollback Work Order operation also to avoid data corruption.
                      We update project code and name after creation of PM Project to avoid mismatch in WO ID and Project Code.
                    */
                    if(pmjsonObj.has("success") && pmjsonObj.getBoolean("success") && pmjsonObj.has("isprojectcodeupdated") && pmjsonObj.getBoolean("isprojectcodeupdated")){
                        dataObj.put("id", workorderObj.getID());
                        dataObj.put("workorderid", pmjsonObj.getString("projectcode"));
                        dataObj.put("workordername", pmjsonObj.getString("projectname"));
                        dataObj.put("isEdit", true);   
                        kmsg = workOrderDAOObj.saveWorkOrder(dataObj);
                    } else if(pmjsonObj.has("success") && !pmjsonObj.getBoolean("success")){
                        throw new AccountingException(messageSource.getMessage("acc.groupCompany.consolidate.error", null, requestcontextutilsobj));    //ERP-40173 : Some error occurred while saving the transaction.                          
                    }

                }
                
                /*
                 Save Custom Field Data
                 */
                String customfield = dataObj.optString("customfield","");
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    JSONArray jcustomarray = new JSONArray(customfield);
                    HashMap<String, Object> customrequestParams = new HashMap<>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_MRPWorkOrder_Modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_MRPWorkOrder_Id);
                    customrequestParams.put("modulerecid", workorderObj.getID());
                    customrequestParams.put("moduleid", Constants.MRP_WORK_ORDER_MODULEID);
                    customrequestParams.put("companyid", companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_MRPWorkOrder_CustomData_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        dataObj.put("accworkordercustomdataref", workorderObj.getID());
                        dataObj.put("id", workorderObj.getID());
                        dataObj.put("isEdit", true);            //Update Work Order when Custom Field
                        kmsg = workOrderDAOObj.saveWorkOrder(dataObj);
                    }

                }
                String WOCode = woObj.getWorkOrderID();
                WOCodes.add(WOCode);
                codeStr += "<br>" + (recCount + 1) + "  <b>" + WOCode + "</b>";
            }
            if (dataArr.length() == 1 ) {
                if (isEdit) {
                    msg = messageSource.getMessage("mrp.workorder.form.successfullyupdatemsg", new Object[]{woObj.getWorkOrderID()}, requestcontextutilsobj);
                } else {
                    msg = messageSource.getMessage("mrp.workorder.form.successfullysavedmsg", new Object[]{woObj.getWorkOrderID()}, requestcontextutilsobj);
                }
            } else {
                if (isEdit) {
                    msg = "Following Work Order(s) have been updated";
                } else {
                    msg = "Following Work Order(s) have been created";
                }
                msg += codeStr;
            }
            List list = new ArrayList();
            if (isEdit) {
                auditMsg = " User " + requestParams.get("userfullname") + " has updated Work Order  <b>" + woObj.getWorkOrderID()+ "</b>";
                auditTrailObj.insertAuditLog(AuditAction.WORK_ORDER_EDIT, auditMsg, requestParams, userid);
            } else {
                auditMsg = " User " + requestParams.get("userfullname") + " has added new Work Order  <b>" + woObj.getWorkOrderID() + "</b>";
                auditTrailObj.insertAuditLog(AuditAction.WORK_ORDER_CREATE, auditMsg, requestParams, userid);
            }
            list.add(msg);
            list.add(kmsg.getEntityList().get(0));
            list.add(genpocheck);
            kwlRetObj = new KwlReturnObject(true, null, null, list, 0);
        } catch (JSONException  | ServiceException | UnsupportedEncodingException | SessionExpiredException |com.krawler.utils.json.base.JSONException |ParseException  jex) {
            throw ServiceException.FAILURE("", jex);
        } 
        return  kwlRetObj;
    }
    public KwlReturnObject updateMassStatus(Map<String, Object> requestParams) throws AccountingException,ServiceException {
        KwlReturnObject kwlRetObj = null;
        KwlReturnObject kmsg = null;
        KwlReturnObject kmsg1 = null;
        Locale requestcontextutilsobj=null;
        JSONObject jObj = null;
        String msg= "", auditMsg = "";
        WorkOrder woObj = null;
        try {
            HttpServletRequest request = (HttpServletRequest) requestParams.get(Constants.RES_REQUEST);
            if(requestParams.containsKey("requestcontextutilsobj")){
                requestcontextutilsobj=(Locale)requestParams.get("requestcontextutilsobj");
            }
            String companyid = requestParams.get("companyid").toString();
            String userid = requestParams.get("userid").toString();
            boolean isEdit = Boolean.parseBoolean(requestParams.get("isEdit").toString());
            boolean isBOMChanged = Boolean.parseBoolean(requestParams.get("isBOMChanged").toString());
            boolean isWorkOrderIDAlreadyPresent = false;
            String sequenceformat = "", nextAutoNumber = "";
            String codeStr = "";
            JSONObject dataJobj = new JSONObject(StringUtil.DecodeText(requestParams.get("data").toString()));
            JSONArray dataArr = dataJobj.getJSONArray("data");
            for(int recCount = 0; recCount < dataArr.length(); recCount++) {
                JSONObject dataObj = dataArr.getJSONObject(recCount);
                dataObj.put("isEdit", true);
                kmsg = workOrderDAOObj.saveWorkOrder(dataObj);
                WorkOrder workorderObj=(WorkOrder)kmsg.getEntityList().get(0);
            }
            msg = "Status Changed Successfully.";
            msg += codeStr;
            List list = new ArrayList();
//            if (isEdit) {
//                auditMsg = " User " + sessionHandlerImpl.getUserFullName(request) + " has updated Work Order  <b>" + woObj.getWorkOrderID()+ "</b>";
//                auditTrailObj.insertAuditLog(com.krawler.accounting.utils.AuditAction.WORK_ORDER_EDIT, auditMsg, request, woObj.getWorkOrderID());
//            } else {
//                auditMsg = " User " + sessionHandlerImpl.getUserFullName(request) + " has added new Work Order  <b>" + woObj.getWorkOrderID() + "</b>";
//                auditTrailObj.insertAuditLog(com.krawler.accounting.utils.AuditAction.WORK_ORDER_CREATE, auditMsg, request, woObj.getWorkOrderID());
//            }
            list.add(msg);
            list.add(kmsg.getEntityList().get(0));
            kwlRetObj = new KwlReturnObject(true, null, null, list, 0);
        } catch (JSONException  | ServiceException  | com.krawler.utils.json.base.JSONException jex) {
            throw ServiceException.FAILURE("", jex);
        } 
        return  kwlRetObj;
    }
    
    
    @Override
    public JSONObject getWorkOrderDataandColumnModel(Map<String, Object> requestParams) {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        JSONObject commData = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        int width=150; //Column Default WIDTH in Work Order Report and Export case 
        Locale requestcontextutilsobj=null;
        try {   

            Boolean isExport = false;
            Boolean isWOStockDetailsReport=false;
            if(requestParams.containsKey("requestcontextutilsobj")){
                requestcontextutilsobj=(Locale)requestParams.get("requestcontextutilsobj");
            }
            if (requestParams.containsKey("isExport") && requestParams.get("isExport") != null) {
                isExport = (Boolean) requestParams.get("isExport");
            }
            if (requestParams.containsKey("isWOStockDetailsReport") && requestParams.get("isWOStockDetailsReport") != null) {
                isWOStockDetailsReport = Boolean.parseBoolean((String) requestParams.get("isWOStockDetailsReport"));
            }
            if (isWOStockDetailsReport) {
                width = 200; //Column Default WIDTH in Work Order Stock Details Report and Export case 
            }


            //***************************Data**********************************************
          
            
            JSONObject dataobj = createWorkOrderJSONArr(requestParams);
            dataJArr=dataobj.getJSONArray("data");

                
            //***************************Data**********************************************
            //*******************Record****************************
            
            String batchSerialDetails ="isWarehouseForProduct,isLocationForProduct,isBatchForProduct,isSerialForProduct,isRowForProduct,isRackForProduct,isBinForProduct"
                    + "warehouse,warehousename,location,locationname";
            for(String dataIndex: batchSerialDetails.split(",")){
                    JSONObject rec = new JSONObject();
                    rec.put(WorkOrder.KEY, dataIndex);
                    jarrRecords.put(rec);
            }
            
            JSONObject rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.WOID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.WORKORDERNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.WORKORDERID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.SEQUENCEFORMAT);
            jarrRecords.put(rec);       
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.JOURNALENTRYID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.JOURNALENTRYNUMBER);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.DATEOFDELIVERY);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.SALESCONTRACTID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.SALESCONTRACTNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.SALESORDERID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.SALESORDERNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.PRODUCTID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY,"pid");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkOrder.PROJECTID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.PRODUCTNAME);
            jarrRecords.put(rec); 
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.ROUTECODEID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.ROUTECODE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.WORKORDERSTATUSID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.WORKORDERSTATUS);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.WORKORDERTYPEID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.WORKORDERTYPE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.WORKORDERWAREHOUSE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.WORKORDERLOCATION);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.CUSTOMERID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.CUSTOMERNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.CREATEDBYID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.CREATEDBYNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.MODIFIEDBYID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.MODIFIEDBYNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.LABOURID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.LABOURNAME);
            jarrRecords.put(rec);
            
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.MACHINEID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.MACHINENAME);
            jarrRecords.put(rec);
            
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.WORKCENTREID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.WORKCENTRENAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, "deleted");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, "quantity");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, "producedquantity");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, "materialid");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrderComponentDetails.PARAM_PRODUCT_TYPE_ID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrderComponentDetails.PARAM_PRODUCT_TYPE_NAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, WorkOrder.PID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, "routetemplateid");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, "routetemplatename");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkOrder.KEY, "workorderdefstatusid");
            jarrRecords.put(rec);
             rec = new JSONObject();
            rec.put(WorkOrder.KEY, "routingtype");
            jarrRecords.put(rec);
            
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.RTCODE);
            jarrRecords.put(rec);
            
            rec = new JSONObject();
            rec.put(JobWork.KEY, "routingmastertypename");
            jarrRecords.put(rec);
            
            
            rec = new JSONObject();
            rec.put(JobWork.KEY, "fromLinkCombo");
            jarrRecords.put(rec);
            
            rec = new JSONObject();
            rec.put(JobWork.KEY, "linkdocid");
            jarrRecords.put(rec);
            
             rec = new JSONObject();
            rec.put(JobWork.KEY, "linkdocid");
            jarrRecords.put(rec);
            
            rec = new JSONObject();
            rec.put(JobWork.KEY, "workorderdate");
            jarrRecords.put(rec);
            
            rec = new JSONObject();
            rec.put(JobWork.KEY, WorkOrder.MATERIALID);
            jarrRecords.put(rec);
            
            rec = new JSONObject();
            rec.put(JobWork.KEY, WorkOrder.MATERIALNAME);
            jarrRecords.put(rec);
            
            rec = new JSONObject();
            rec.put(JobWork.KEY, WorkOrder.ATTACH_DOCUMENTS);
            jarrRecords.put(rec);

             rec = new JSONObject();
            rec.put(JobWork.KEY, WorkOrder.ATTACHMENT);
            jarrRecords.put(rec);
            //billid for document designer
             rec = new JSONObject();
            rec.put(JobWork.KEY, Constants.billid);
            jarrRecords.put(rec);
            
            //*******************Record****************************

            //*****************ColumnModel******************************
             JSONObject tmp = new JSONObject();
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.header1", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkOrder.WORKORDERNAME);
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.header2", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkOrder.WORKORDERID);
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
            if(!isWOStockDetailsReport)
            {
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.invoiceList.jeno", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkOrder.JOURNALENTRYNUMBER);
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            }

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.header3", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkOrder.CUSTOMERNAME);
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            
            if(!isWOStockDetailsReport)
            {
                
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.header4", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkOrder.SALESORDERNAME);
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.header5", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex",  WorkOrder.SALESCONTRACTNAME);
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
            }
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.header6", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkOrder.PRODUCTNAME);
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
                    
            
            /* 
             Added Product ID Column to Grid 
            */
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.header15", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex",  WorkOrder.PID);
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
                    
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.field.bomName", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkOrder.MATERIALNAME);
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.actualquantity", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "quantity");
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

             jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.producedquantity", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "producedquantity");
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
             jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.field.workorder.workorderdate", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "workorderdate");
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.header7", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkOrder.DATEOFDELIVERY);
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
//          
            
            if(!isWOStockDetailsReport)
            {

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.fieldlabel.workorder.routingmaster.type", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "routingmastertypename");
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.fields.workorder.routingtemplateroutingcode.title", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "routetemplatename");
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.header9", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkOrder.WORKORDERTYPE);
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.header10", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkOrder.WORKORDERSTATUS);
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.header14", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkOrder.LABOURNAME);
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.header12", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkOrder.MACHINENAME);
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workcentre.report.header2", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkOrder.WORKCENTRENAME);
            jobjTemp.put("width", width);
            jobjTemp.put("pdfwidth", width);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
            }
            
            /*
             Add Custom Fields in Column Model
             */
            String companyId = (String) requestParams.get("companyid");
            requestParams.put("companyId", companyId);
            requestParams.put("reportId", Constants.MRP_WORK_ORDER_MODULEID);
            putCustomColumnForWorkOrder(jarrColumns, jarrRecords, requestParams);
//*****************ColumnModel******************************
            
            commData.put("success", true);
            commData.put("coldata", dataJArr);
            commData.put("columns", jarrColumns);
            commData.put("totalCount", dataobj.optInt("TotalCount",0));
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

            jobj.put("valid", true);
            if (isExport) {
                jobj.put("data", dataJArr);
            } else {
                jobj.put("data", commData);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccWorkOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    public void putCustomColumnForWorkOrder(JSONArray jarrColumns, JSONArray jarrRecords, Map<String, Object> requestParams) throws ServiceException {
        try {
            HashMap<String, Object> requestParams1 = new HashMap<>(requestParams);
            KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(requestParams1);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            List arrayList = new ArrayList();
            for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
                String column = "Custom_" + customizeReportMapping.getDataIndex();
                if (!arrayList.contains(customizeReportMapping.getDataIndex())) {
                    JSONObject jobjTemp = new JSONObject();
                    jobjTemp.put("name", column);
                    jarrRecords.put(jobjTemp);
                    jobjTemp = new JSONObject();
                    jobjTemp.put("header", customizeReportMapping.getDataHeader());
                    jobjTemp.put("dataIndex", column);
                    jobjTemp.put("width", 150);
                    jobjTemp.put("pdfwidth", 150);
                    jobjTemp.put("custom", "true");
                    jarrColumns.put(jobjTemp);
                    arrayList.add(customizeReportMapping.getDataIndex());
                }
            }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    @Override
    public KwlReturnObject deleteWorkOrders(Map<String, Object> dataMap) throws ServiceException {
        List list=Collections.EMPTY_LIST;
        try{
            JSONObject jobj=new JSONObject((String)dataMap.get("data"));
            JSONArray jArr=jobj.getJSONArray("root");
            
            Map<String,Object>requestParams=new HashMap();
            requestParams.put(WorkOrder.COMPANYID,dataMap.get(WorkOrder.COMPANYID));
            for (int i = 0; i < jArr.length(); i++) {
                String id = (String) jArr.get(i);
                if (!StringUtil.isNullOrEmpty(id)) {
                    requestParams.put(WorkOrder.WOID, id);
                    KwlReturnObject result = workOrderDAOObj.deleteWorkOrders(requestParams);
                }

            }
            
            
        }catch(Exception ex){
         throw ServiceException.FAILURE("AccJobWorkServiceImpl.deleteJobWork", ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public JSONObject getSOSCCombo(Map<String, Object> map) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        KwlReturnObject kmsg = null;
        boolean isEdit=false;
        String productType = "";
        HashMap<String, Object> hashmap = new HashMap();
        try {

            KwlReturnObject result = workOrderDAOObj.getSOSCCombo(map);
             if (map.containsKey("isEdit")) {
                isEdit =Boolean.parseBoolean(map.get("isEdit").toString());
            }
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                try {
                    jSONObject = new JSONObject();
                    Object obj[] = (Object[]) itr.next();
                    jSONObject.put("id", (String) obj[0]);
                    jSONObject.put("name", (String) obj[1]);
                    
                    if(!isEdit)
                    {
                    hashmap.put(WorkOrder.SALESORDERID, (String) obj[0]);
                    kmsg = workOrderDAOObj.getProductsForCombo(hashmap, true);

                    List productlist = kmsg.getEntityList();
                    if (!productlist.isEmpty() && productlist != null) {

                        Iterator productiterator = productlist.iterator();
                        Product product = null;
                        double soquantity = 0;
                        while (productiterator.hasNext()) {
                            try {

                                Object obj1[] = (Object[]) productiterator.next();
                                product = (Product) obj1[0];
                                soquantity = (double) obj1[1];

                                map.put("id", (String) obj[0]);//sales order id.
                                map.put("productid", product.getID());//product id.

                                KwlReturnObject workorderresult = workOrderDAOObj.getWorkorderFromSOandProduct(map);
                                List wolost = workorderresult.getEntityList();

                                if (!wolost.isEmpty() && wolost != null) {
                                    Iterator woiterator = wolost.iterator();
                                    int woquantity = 0;
                                    double totalwoquantity = 0;
                                    while (woiterator.hasNext()) {
                                        Object obj2[] = (Object[]) woiterator.next();
                                        woquantity = (int) obj2[1];
                                        totalwoquantity += woquantity;
                                    }
                                    if (totalwoquantity < soquantity) {
                                        jArr.put(jSONObject);
                                        break;
                                    }
                                } else {
                                    productType = (product.getProducttype() != null ? product.getProducttype().getID() : "");
                                    if (productType.equals(Producttype.ASSEMBLY)) {
                                        jArr.put(jSONObject);
                                        break;
                                    }
                                }
                            } catch (Exception ex) {
                                throw ServiceException.FAILURE(ex.getMessage(), ex);
                            }
                        }
                    }
                }else{
                        jArr.put(jSONObject);
                    }

                } catch (com.krawler.utils.json.base.JSONException ex) {
                    throw ServiceException.FAILURE(ex.getMessage(), ex);
                }
            }
            jobj.put("data", jArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkServiceImpl.deleteJobWork", ex);
        }
        return jobj;
    }
    
    @Override
    public JSONObject deleteWorkOrder(Map<String, Object> requestParams) throws AccountingException, ServiceException {

        JSONObject jobj = new JSONObject();
        KwlReturnObject returnResult=null;
        String [] arrayOfID=null;
        String workOrderID = "";
        boolean isAllWODelete = true;
        Locale requestcontextutilsobj=null;
        String woNotDelIds = "";
        String message = "";
        try {
            HttpServletRequest request = (HttpServletRequest) requestParams.get(Constants.RES_REQUEST);
            if(requestParams.containsKey("requestcontextutilsobj")){
                requestcontextutilsobj=(Locale)requestParams.get("requestcontextutilsobj");
            }
            if(requestParams.containsKey("idsfordelete")&& requestParams.get("idsfordelete")!=null){
                
                arrayOfID=(String[])requestParams.get("idsfordelete");
                for (int count = 0; count < arrayOfID.length; count++) {
                    requestParams.put(WorkCentre.WCID, arrayOfID[count]);
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), arrayOfID[count]);
                    WorkOrder workOrder = (WorkOrder) result.getEntityList().get(0);
                    workOrderID = "<b>" + workOrder.getWorkOrderID()+ "</b>";
                    /*
                     * if WO status is Planned
                     * Then only that WO is Allowed to delete.
                     */ 
                    if (Constants.defaultWOstatus_PLANNED.equals(workOrder.getWorkOrderStatus().getDefaultMasterItem().getID())) {
                        returnResult = workOrderDAOObj.deleteWorkOrder(requestParams);
                        auditTrailObj.insertAuditLog(AuditAction.WORK_ORDER_DELETE, " User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Work Order " + workOrderID, request, arrayOfID[count]);
                    } else {
                        woNotDelIds += "<b>" + workOrderID + "</b> <br>";
                        isAllWODelete = false;
                        
                    }
                }
            }
            
            /*
             * If All WO(s) are not deleted 
             * then send list of Work Orders that are not deleted.
             */ 
            if (!isAllWODelete) {
                message += messageSource.getMessage("mrp.workorder.delete.cannotdeletewo", null, requestcontextutilsobj) + "<br>";
                message += woNotDelIds;
                message += messageSource.getMessage("mrp.workorder.delete.cannotdeleteworeason", null, requestcontextutilsobj);
                jobj.put("msg", message);
            } else {
                jobj.put("msg", returnResult.getMsg());
            }
        } catch (Exception ex) {
            Logger.getLogger(AccWorkOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    @Override
    public JSONObject deleteWorkOrderPermanently(Map<String, Object> requestParams) throws ServiceException {

        JSONObject jobj = new JSONObject();
        Machine machineObj = null;
        List list = null;
        String[] arrayOfID = null;
        String linkedTransaction = "";
        Locale requestcontextutilsobj = null;
        KwlReturnObject returnResult = null;
        Set<SubstituteMachineMapping> activeMachineMappingDetails = null;
        String workOrderID = "", woProductName = "", wodetailInfo="";   //ERP-41236
        double woQuantity = 0;
        boolean isAllWODelete = true;
        boolean isProductUsed = true;
        String woNotDelIds = "";
        String woNotDelIdsDueToQAAccepted="";
        String woNotDelIdsDueToQAApproved="";
        String woNotDelIdsDueToQARejected="";
        String woNotDelIdsDueToQAApprovedorRejected="";
        String productsIdUsed = "";
        String message = "";
        boolean isDocumentInQA=false;
        StringBuilder tempInventoryID = new StringBuilder();
        try {
            HttpServletRequest request = (HttpServletRequest) requestParams.get(Constants.RES_REQUEST);
            String companyid = requestParams.get("companyid").toString();
            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }

            if (requestParams.containsKey("idsfordelete") && requestParams.get("idsfordelete") != null) {
                arrayOfID = (String[]) requestParams.get("idsfordelete");
                
                skipWorkOrder:   
                for (int count = 0; count < arrayOfID.length; count++) {
                    requestParams.put("id", arrayOfID[count]);
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), arrayOfID[count]);
                    WorkOrder workOrder = (WorkOrder) result.getEntityList().get(0);
                    workOrderID = "<b>" + workOrder.getWorkOrderID()+ "</b>";
                    woProductName = workOrder.getProductID()!=null ? workOrder.getProductID().getProductid() : "";
                    woQuantity = workOrder.getQuantity();
                    wodetailInfo = workOrderID + "<b>  [ Product: " + woProductName+ ",  Quantity: "+woQuantity+" ]</b>";   //ERP-41236 : Audit Trail Message while deleting the WO.
                    /*
                     * if WO status is Planned
                     * Then only that WO is Allowed to delete.
                     */ 
                    if (!Constants.defaultWOstatus_CLOSED.equals(workOrder.getWorkOrderStatus().getDefaultMasterItem().getID())) { 
                        Map<String, Object> requestParams1 = new HashMap<String, Object>();
                        requestParams1.put(Constants.companyKey, companyid);
                        
                        requestParams.put(Constants.projectId, workOrder.getProjectId());
                        KwlReturnObject result1 = workOrderDAOObj.getWorkOrderComponentDetails(requestParams);
                        List<WorkOrderComponentDetails> detailsesList = result1.getEntityList();
                        /**
                         * Check if any produced quantity of WO is deleted.
                         */
                        for (WorkOrderComponentDetails workOrderComponentDetails : detailsesList) {
                            if (!StringUtil.isNullOrEmpty(workOrderComponentDetails.getConsumptionDetails())) {
                                
                                /**
                                 * check whether product stock is send to QA store.
                                 */
                                JSONObject QAmappingPara = new JSONObject();
                                QAmappingPara.put("wocdid", workOrderComponentDetails.getID());
                                KwlReturnObject wocdistmappingkwlObj = stockService.getWOCDetailISTMapping(QAmappingPara);
                                List<WOCDetailISTMapping> wocdistmappings = wocdistmappingkwlObj.getEntityList();
                                for (WOCDetailISTMapping wocdistmapping : wocdistmappings) {
                                    isDocumentInQA = false;
                                    if (wocdistmapping.getRejectedInterStoreTransferRequests() != null && !wocdistmapping.getRejectedInterStoreTransferRequests().isEmpty()) {
                                       /**
                                        * if document is already Rejected from QA store.
                                        */
                                        isDocumentInQA = true;
                                        woNotDelIdsDueToQARejected += workOrderID + " , ";
                                    } else if (wocdistmapping.getApprovedInterStoreTransferRequests() != null && !wocdistmapping.getApprovedInterStoreTransferRequests().isEmpty()) {
                                       /**
                                        * if document is already Approved from QA store.
                                        */
                                        isDocumentInQA = true;
                                        woNotDelIdsDueToQAApproved += workOrderID + " , ";
                                    } else if (wocdistmapping.getInterStoreTransferRequest() != null) {
                                       /**
                                        * if stock is already accepted by QA store.
                                        */
                                        if (wocdistmapping.getInterStoreTransferRequest().getStatus() != InterStoreTransferStatus.INTRANSIT) {
                                            isDocumentInQA = true;
                                            woNotDelIdsDueToQAAccepted += workOrderID + " , ";
                                        }
                                    }
                                    if (isDocumentInQA) {
                                        woNotDelIds += "<b>" + workOrderID + "</b> <br>";
                                        isAllWODelete = false;
                                        continue skipWorkOrder;
                                    }
                                }
                                JSONObject jObjConsumptionDetails = new JSONObject(workOrderComponentDetails.getConsumptionDetails());
                                String producedQtyDetail = jObjConsumptionDetails.optString(WorkOrderComponentDetails.PRODUCE_DETAILS);
                                String batchDetails = jObjConsumptionDetails.optString(WorkOrderComponentDetails.BATCH_DETAILS);
                                /**
                                 * if any product produced and not consumed
                                 * other than final product, describes that
                                 * produced product is used some where else.
                                 */
                                if (!StringUtil.isNullOrEmpty(producedQtyDetail) && StringUtil.isNullOrEmpty(batchDetails)) {
                                    /**
                                     * Check if quantity in produceQtyDetail and
                                     * in Batch Matches i.e. quantity mentioned
                                     * in WorkOrder is still present or have
                                     * been consumed by some other means.
                                     */
                                    try {
                                        JSONArray jArr = new JSONArray(producedQtyDetail);
                                        for (int i = 0; i < jArr.length(); i++) {
                                            JSONObject jSONObject = (JSONObject) jArr.get(i);
                                            String productBatchId = accCommonTablesDAO.getpurchaseBatchIdForLocationWarehouseRowRackBin(jSONObject.getString("productid"), jSONObject.getString("location"), jSONObject.getString("warehouse"), jSONObject.optString("row", null), jSONObject.optString("rack", null), jSONObject.optString("bin", null), jSONObject.optString("batch", null));
                                            Map<String, Object> filterMap = new HashMap();
                                            filterMap.put(Constants.Acc_id, productBatchId);
                                            Double quantityDue = (Double) kwlCommonTablesDAOObj.getRequestedObjectFields(NewProductBatch.class, new String[]{Constants.quantityDue}, filterMap);
                                            if (quantityDue != jSONObject.optDouble(Constants.quantity)) {
                                                woNotDelIds += "<b>" + workOrderID + "</b> <br>";
                                                productsIdUsed += workOrderComponentDetails.getProduct().getProductid() + ", ";
                                                isAllWODelete = false;
                                                continue skipWorkOrder;
                                            }
                                        }
                                    } catch (com.krawler.utils.json.base.JSONException ex) {
                                        System.out.println(ex);
                                    }
                                }
                            }
                        }
                        
                        for (WorkOrderComponentDetails workOrderComponentDetails : detailsesList) {
                            /**
                             * batches and LBDM
                             */
                            requestParams1.put("workorderdetailid", workOrderComponentDetails.getID());
                            requestParams1.put("isDeletePermanent", true);
                            requestParams1.put("productId", workOrderComponentDetails.getProduct()!= null ? workOrderComponentDetails.getProduct().getID() : "");
                            workOrderDAOObj.deleteWorkOrderBatchSerialDetails(requestParams1);
                            /**
                             * revert SM and IN Stock
                             */
                            stockMovementService.removeStockMovementByReferenceIdForWorkOrder(workOrderComponentDetails.getWorkOrder().getCompany(), workOrderComponentDetails.getID());
                            /**
                             * revert Product and Inventory table
                             */
                            if (workOrderComponentDetails.getInventory() != null) {
                                tempInventoryID.append(workOrderComponentDetails.getInventory().getID());
                                workOrderComponentDetails.setInventory(null);
                                accProductDaoObj.deleteInventory(tempInventoryID.toString(), companyid);
                                tempInventoryID.setLength(0);
                            }
                            if (workOrderComponentDetails.getInventoryProduced() != null) {
                                tempInventoryID.append(workOrderComponentDetails.getInventoryProduced().getID());
                                workOrderComponentDetails.setInventoryProduced(null);
                                accProductDaoObj.deleteInventory(tempInventoryID.toString(), companyid);
                                tempInventoryID.setLength(0);
                            }
                            if (workOrderComponentDetails.getInventoryReturnedOut() != null) {
                                tempInventoryID.append(workOrderComponentDetails.getInventoryReturnedOut().getID());
                                workOrderComponentDetails.setInventoryReturnedOut(null);
                                accProductDaoObj.deleteInventory(tempInventoryID.toString(), companyid);
                                tempInventoryID.setLength(0);
                            }
                            if (workOrderComponentDetails.getInventoryReturnedIn() != null) {
                                tempInventoryID.append(workOrderComponentDetails.getInventoryReturnedIn().getID());
                                workOrderComponentDetails.setInventoryReturnedIn(null);
                                accProductDaoObj.deleteInventory(tempInventoryID.toString(), companyid);
                                tempInventoryID.setLength(0);
                            }
                            if (workOrderComponentDetails.getInventoryWasteOut() != null) {
                                tempInventoryID.append(workOrderComponentDetails.getInventoryWasteOut().getID());
                                workOrderComponentDetails.setInventoryWasteOut(null);
                                accProductDaoObj.deleteInventory(tempInventoryID.toString(), companyid);
                                tempInventoryID.setLength(0);
                            }
                            if (workOrderComponentDetails.getInventoryWasteIn() != null) {
                                tempInventoryID.append(workOrderComponentDetails.getInventoryWasteIn().getID());
                                workOrderComponentDetails.setInventoryWasteIn(null);
                                accProductDaoObj.deleteInventory(tempInventoryID.toString(), companyid);
                                tempInventoryID.setLength(0);
                            }
                            if (workOrderComponentDetails.getInventoryRecycleIn() != null) {
                                tempInventoryID.append(workOrderComponentDetails.getInventoryRecycleIn().getID());
                                workOrderComponentDetails.setInventoryRecycleIn(null);
                                accProductDaoObj.deleteInventory(tempInventoryID.toString(), companyid);
                                tempInventoryID.setLength(0);
                            }
                            if (workOrderComponentDetails.getInventoryRecycleOut() != null) {
                                tempInventoryID.append(workOrderComponentDetails.getInventoryRecycleOut().getID());
                                workOrderComponentDetails.setInventoryRecycleOut(null);
                                accProductDaoObj.deleteInventory(tempInventoryID.toString(), companyid);
                                tempInventoryID.setLength(0);
                            }
                            /**
                             * delete Batch if Produced.
                             */
                            String oldProduceQtyDetail = "";
                            if(!StringUtil.isNullOrEmpty(workOrderComponentDetails.getConsumptionDetails())){
                                JSONObject jObjConsumptionDetails = new JSONObject(workOrderComponentDetails.getConsumptionDetails());
                                oldProduceQtyDetail = jObjConsumptionDetails.optString(WorkOrderComponentDetails.PRODUCE_DETAILS);
                                if (!StringUtil.isNullOrEmpty(oldProduceQtyDetail)) {
                                    try {
                                        JSONArray jArr = new JSONArray(oldProduceQtyDetail);
                                        for (int i = 0; i < jArr.length(); i++) {
                                            JSONObject jSONObject = (JSONObject) jArr.get(i);
                                            String productBatchId = accCommonTablesDAO.getpurchaseBatchIdForLocationWarehouseRowRackBin( jSONObject.getString("productid"), jSONObject.getString("location"), jSONObject.getString("warehouse"), jSONObject.optString("row", null), jSONObject.optString("rack", null), jSONObject.optString("bin", null), jSONObject.optString("batch", null));
                                            accCommonTablesDAO.deleteBatches(productBatchId, companyid);
                                        }
                                    } catch (com.krawler.utils.json.base.JSONException ex) {
                                        System.out.println(ex);
                                    }
                                }
                            }
                        }
                        /**
                         * Delete respective PM project for this WO.
                         */
                        requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
                        requestParams.put(Constants.useridKey, sessionHandlerImpl.getUserid(request));
                        requestParams.put(Constants.MRP_isDirtyProject, false);
                        JSONObject jSONObject = accRoutingManagementService.deleteDirtyProjectRest(requestParams);

                        requestParams.put(WorkOrder.POJO, JobWork.POJONAME);
                        requestParams.put(WorkOrder.ATTRIBUTE, JobWork.DB_WORKORDERID);
                        returnResult = workOrderDAOObj.deleteWorkOrderMappings(requestParams);

                        requestParams.put(WorkOrder.POJO, WorkOrderLabourMapping.POJONAME);
                        requestParams.put(WorkOrder.ATTRIBUTE, WorkOrderLabourMapping.ATTRIBUTENAME);
                        returnResult = workOrderDAOObj.deleteWorkOrderMappings(requestParams);

                        requestParams.put(WorkOrder.POJO, WorkOrderMachineMapping.POJONAME);
                        requestParams.put(WorkOrder.ATTRIBUTE, WorkOrderMachineMapping.ATTRIBUTENAME);
                        returnResult = workOrderDAOObj.deleteWorkOrderMappings(requestParams);

                        requestParams.put(WorkOrder.POJO, WorkOrderWorkCenterMapping.POJONAME);
                        requestParams.put(WorkOrder.ATTRIBUTE, WorkOrderWorkCenterMapping.ATTRIBUTENAME);
                        returnResult = workOrderDAOObj.deleteWorkOrderMappings(requestParams);


                        requestParams.put(WorkOrder.POJO, WorkOrderComponentDetails.POJONAME);
                        requestParams.put(WorkOrder.ATTRIBUTE, WorkOrderComponentDetails.DB_WORKORDERID);
                        returnResult = workOrderDAOObj.deleteWorkOrderMappings(requestParams);

                        requestParams.put(WorkOrder.POJO, WorkOrderCustomData.POJONAME);
                        requestParams.put(WorkOrder.ATTRIBUTE, WorkOrderCustomData.DB_WORKORDERID);
                        returnResult = workOrderDAOObj.deleteWorkOrderCustomData(requestParams);

                        returnResult = workOrderDAOObj.deleteWorkOrderPermanently(requestParams);

                        auditTrailObj.insertAuditLog(AuditAction.WORK_ORDER_DELETE, " User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Work Order Permanently " + wodetailInfo, request, arrayOfID[count]);     //ERP-41236
                    } else {
                        woNotDelIds += "<b>" + workOrderID + "</b> <br>";
                        isAllWODelete = false;
                        isProductUsed = false;
                    }
                }
            }
            /*
             * If All WO(s) are not deleted 
             * then send list of Work Orders that are not deleted.
             */ 
            if (!isAllWODelete) { 
                message += messageSource.getMessage("mrp.workorder.delete.cannotdeletewo", null, requestcontextutilsobj) + "<br>";
                message += woNotDelIds;
                message += messageSource.getMessage("mrp.workorder.delete.cannotdeleteworeason", null, requestcontextutilsobj);
                if (!StringUtil.isNullOrEmpty(productsIdUsed)) {
                    /**
                     * Remove ,(space) appended in last.
                     */
                    productsIdUsed = productsIdUsed.substring(0, productsIdUsed.length() - 2);
                    message += messageSource.getMessage("mrp.workorder.delete.cannotdeleteworeason.productareadyused", new Object[]{productsIdUsed}, requestcontextutilsobj);
                }
                if(!StringUtil.isNullOrEmpty(woNotDelIdsDueToQAAccepted)){
                     woNotDelIdsDueToQAAccepted = woNotDelIdsDueToQAAccepted.substring(0, woNotDelIdsDueToQAAccepted.length() - 2);
                     message += messageSource.getMessage("acc.workorder.cannot.be.deleted.as.stockisaccepted.byQAstore", new Object[] {"delete",woNotDelIdsDueToQAAccepted}, requestcontextutilsobj);
                }
                if(!StringUtil.isNullOrEmpty(woNotDelIdsDueToQAApproved) || !StringUtil.isNullOrEmpty(woNotDelIdsDueToQARejected)){
                     woNotDelIdsDueToQAApprovedorRejected = woNotDelIdsDueToQAApproved + woNotDelIdsDueToQARejected;
                     woNotDelIdsDueToQAApprovedorRejected = woNotDelIdsDueToQAApprovedorRejected.substring(0, woNotDelIdsDueToQAApprovedorRejected.length() - 2);
                     message += messageSource.getMessage("acc.workorder.cannot.be.deleted.as.approvedOrRejected.byQAstore", new Object[] {"delete",woNotDelIdsDueToQAApprovedorRejected,"approved / rejected"}, requestcontextutilsobj);
                }
                if (!isProductUsed) {
                    message += messageSource.getMessage("mrp.workorder.delete.cannotdeleteworeason.woclosed", null, requestcontextutilsobj);
                }
                jobj.put("msg", message);
            } else {
                if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                    jobj.put("msg", returnResult.getMsg());
                } else {
                    jobj.put("msg", messageSource.getMessage("acc.machineMaster.machineExcept", null, requestcontextutilsobj)+ "<B>" +linkedTransaction.substring(0, linkedTransaction.length() - 2)+"</B>"+" "+messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, requestcontextutilsobj));
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    
    @Override
    public JSONObject exportWorkOrder(Map<String, Object> requestParams) throws ServiceException {
        
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        HttpServletRequest request=null;
        HttpServletResponse response=null;
        try {
            JSONObject tmpObj=createWorkOrderJSONArr(requestParams);
            jobj.put("data", tmpObj.getJSONArray("data"));
            request=(HttpServletRequest)requestParams.get("request");
            response=(HttpServletResponse)requestParams.get("response");
            exportDaoObj.processRequest(request, response, jobj);
            
        } catch (Exception ex) {
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    
    public JSONObject createWorkOrderJSONArr(Map<String, Object> requestParams) throws  ServiceException {
        JSONArray dataJArr = new  JSONArray();
        JSONObject jobj=new JSONObject();
        try{
            KwlReturnObject result = workOrderDAOObj.getWorkOrders(requestParams);
            List dataList = result.getEntityList();
            WorkOrder workOrder = null;
            DateFormat userdf = requestParams.containsKey(Constants.userdf) ? (DateFormat) requestParams.get(Constants.userdf) : null;
            JSONObject tmpObj = new JSONObject();
            for (Object obj : dataList) {
                workOrder = (WorkOrder) obj;
                tmpObj = new JSONObject();

                tmpObj.put(workOrder.WOID, workOrder.getID());
                tmpObj.put(Constants.billid, workOrder.getID());//billid for document designer print
                tmpObj.put(workOrder.WORKORDERNAME, workOrder.getWorkOrderName());
                tmpObj.put(workOrder.WORKORDERID, workOrder.getWorkOrderID());
                if(workOrder.getCloseWOJE()!=null && !StringUtil.isNullOrEmpty(workOrder.getCloseWOJE().getID())){
                    tmpObj.put(workOrder.JOURNALENTRYID, workOrder.getCloseWOJE().getID());
                    tmpObj.put(workOrder.JOURNALENTRYNUMBER, workOrder.getCloseWOJE().getEntryNumber());
                }else{
                    tmpObj.put(workOrder.JOURNALENTRYID, "");
                    tmpObj.put(workOrder.JOURNALENTRYNUMBER, "");
                }
                tmpObj.put(workOrder.SEQUENCEFORMAT, workOrder.getSeqformat() != null ? workOrder.getSeqformat().getID() : "");
                tmpObj.put(workOrder.DATEOFDELIVERY, ((workOrder.getDateOfDelivery() != null) && (userdf != null)) ? userdf.format(workOrder.getDateOfDelivery()) : "");
                tmpObj.put(workOrder.SALESCONTRACTID, workOrder.getSalesContractID() != null ? workOrder.getSalesContractID().getID() : "");
                tmpObj.put(workOrder.SALESCONTRACTNAME, workOrder.getSalesContractID() != null ? workOrder.getSalesContractID().getContractNumber() : "");
                if (workOrder.getFromlinktype() == WorkOrder.SALESORDER) {
                    tmpObj.put("fromLinkCombo", WorkOrder.SALESORDER);
                    tmpObj.put("linkdocid", workOrder.getSalesOrder() != null ? workOrder.getSalesOrder().getID() : "");

                } else if (workOrder.getFromlinktype() == WorkOrder.SALESCONTRACT) {
                    tmpObj.put("fromLinkCombo", WorkOrder.SALESCONTRACT);
                    tmpObj.put("linkdocid", workOrder.getSalesContractID() != null ? workOrder.getSalesContractID().getID() : "");

                } else {
                    tmpObj.put("fromLinkCombo", 0);
                    tmpObj.put("linkdocid", "");

                }
                
                tmpObj.put(workOrder.SALESORDERNAME, workOrder.getSalesOrder() != null ? workOrder.getSalesOrder().getSalesOrderNumber() : "");
                tmpObj.put(workOrder.PRODUCTID, workOrder.getProductID() != null ? workOrder.getProductID().getID(): "");
                 tmpObj.put("pid", workOrder.getProductID() != null ? workOrder.getProductID().getProductid(): "");
                tmpObj.put(workOrder.PRODUCTNAME, workOrder.getProductID() != null ? workOrder.getProductID().getProductName() : "");
//                tmpObj.put(workOrder.PRODUCTID, "");
                tmpObj.put("workorderdate", ((workOrder.getWorkOrderDate() != null) && (userdf != null)) ? userdf.format(workOrder.getWorkOrderDate()) : "");
                tmpObj.put(workOrder.ROUTECODEID, workOrder.getRouteCode() != null ? workOrder.getRouteCode():"");
                tmpObj.put("routetemplateid", workOrder.getRouteTemplate() != null ? workOrder.getRouteTemplate().getId():"");
                tmpObj.put("routetemplatename", workOrder.getRouteTemplate() != null ? workOrder.getRouteTemplate().getName():"");
                tmpObj.put(workOrder.ROUTECODE, workOrder.getRouteCode() != null ? workOrder.getRouteCode(): "");
                tmpObj.put(workOrder.WORKORDERSTATUSID, workOrder.getWorkOrderStatus()!= null ? workOrder.getWorkOrderStatus().getID(): "");
                tmpObj.put(workOrder.WORKORDERSTATUS, workOrder.getWorkOrderStatus() != null ? workOrder.getWorkOrderStatus().getValue(): "");
                tmpObj.put("workorderdefstatusid", (workOrder.getWorkOrderStatus() != null && workOrder.getWorkOrderStatus().getDefaultMasterItem()!= null) ? workOrder.getWorkOrderStatus().getDefaultMasterItem().getID(): "");
                tmpObj.put(workOrder.WORKORDERTYPEID, workOrder.getWorkOrderType()!= null ? workOrder.getWorkOrderType().getID(): "");
                tmpObj.put(workOrder.WORKORDERTYPE, workOrder.getWorkOrderType() != null ? workOrder.getWorkOrderType().getValue(): "");
                tmpObj.put(workOrder.WORKORDERWAREHOUSE, workOrder.getOrderWarehouse() != null ? workOrder.getOrderWarehouse().getId(): "");
                tmpObj.put(workOrder.WORKORDERLOCATION, workOrder.getOrderLocation() != null ? workOrder.getOrderLocation().getId(): "");
                tmpObj.put(workOrder.CUSTOMERID, workOrder.getCustomer() != null ? workOrder.getCustomer().getID(): "");
                tmpObj.put(workOrder.CUSTOMERNAME, workOrder.getCustomer() != null ? workOrder.getCustomer().getCVName(): "");
                tmpObj.put(workOrder.CREATEDBYID, workOrder.getCreatedBy() != null ? workOrder.getCreatedBy().getUserID(): "");
                tmpObj.put(workOrder.CREATEDBYNAME, workOrder.getCreatedBy() != null ? workOrder.getCreatedBy().getFullName(): "");
                tmpObj.put(workOrder.MODIFIEDBYID, workOrder.getModifiedBy() != null ? workOrder.getModifiedBy().getUserID(): "");
                tmpObj.put(workOrder.MODIFIEDBYNAME, workOrder.getModifiedBy() != null ? workOrder.getModifiedBy().getFullName(): "");
                tmpObj.put(workOrder.PROJECTID, workOrder.getProjectId() != null ? workOrder.getProjectId(): "");
                tmpObj.put("routingtype", workOrder.getRoutingMasterType());
                
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("invoiceID", workOrder.getID());
                hashMap.put("companyid", workOrder.getCompany().getCompanyID());
                KwlReturnObject object = accInvoiceDAOobj.getinvoiceDocuments(hashMap);
                int attachemntcount = object.getRecordTotalCount();
                tmpObj.put("attachment", attachemntcount);
                
                if (workOrder.getRoutingMasterType() == RoutingTemplate.MASTERTYPE_ROUTING_TEMPLATE) {
                    tmpObj.put("routingmastertypename", WorkOrder.ROUTINGMASTER_TEMPLATE);
                } else if (workOrder.getRoutingMasterType() == RoutingTemplate.MASTERTYPE_ROUTING_CODE) {
                    tmpObj.put("routingmastertypename", WorkOrder.ROUTINGMASTER_CODE);
                } else {
                    tmpObj.put("routingmastertypename", WorkOrder.ROUTINGMASTER_NONE);
                }
                if(workOrder.getRoutingMasterType() == RoutingTemplate.MASTERTYPE_ROUTING_CODE){
                    /* In edit case of work order if routing code was selected previously then in that case routingtemplate name is null so in this case use routingcode */
                    tmpObj.put("routetemplatename", workOrder.getRouteTemplate() != null ? workOrder.getRouteTemplate().getRoutecode():"");
                }
                tmpObj.put("quantity", workOrder.getQuantity() );
                tmpObj.put("producedquantity", workOrder.getProducedquantity());
                tmpObj.put("deleted", workOrder.isDeleted());
                tmpObj.put(WorkOrder.MATERIALID, workOrder.getBomid()!=null?workOrder.getBomid().getID():"");
                tmpObj.put(WorkOrder.MATERIALNAME, workOrder.getBomid()!=null?workOrder.getBomid().getBomName():"");
                tmpObj.put(workOrder.PID, workOrder.getProductID() != null ? workOrder.getProductID().getProductid() : "");
                tmpObj.put(WorkOrderComponentDetails.PARAM_PRODUCT_TYPE_ID, workOrder.getProductID().getProducttype().getID());
                tmpObj.put(WorkOrderComponentDetails.PARAM_PRODUCT_TYPE_NAME, workOrder.getProductID().getProducttype().getName());
                
                Product product =workOrder.getProductID();
                if (product != null) {
                    tmpObj.put("isLocationForProduct", product.isIslocationforproduct());
                    tmpObj.put("isWarehouseForProduct" , product.isIswarehouseforproduct());
                    tmpObj.put("isBatchForProduct", product.isIsBatchForProduct());
                    tmpObj.put("isSerialForProduct", product.isIsSerialForProduct());
                    tmpObj.put("isRowForProduct", product.isIsrowforproduct());  
                    tmpObj.put("isRackForProduct", product.isIsrackforproduct());
                    tmpObj.put("isBinForProduct", product.isIsbinforproduct());
                    tmpObj.put("warehouse", workOrder.getOrderWarehouse()!= null ? workOrder.getOrderWarehouse().getId() :"");
                    tmpObj.put("warehousename", workOrder.getOrderWarehouse()!= null ? workOrder.getOrderWarehouse().getName() :"");
                    tmpObj.put("location",  workOrder.getOrderLocation()!= null ? workOrder.getOrderLocation().getId() :"");
                    tmpObj.put("locationname",  workOrder.getOrderLocation()!= null ? workOrder.getOrderLocation().getName() :"");
                }
                Set<WorkOrderLabourMapping> labourMapping = workOrder.getLabourmapping();
                List<WorkOrderLabourMapping> sortedList=new ArrayList<>(labourMapping);
                sortedList.size();
                
                String labourids = "";
                String labournames = "";
                List <String> labourIDList=new ArrayList<>();
                List <String> labourNamelist=new ArrayList<>();
                for (WorkOrderLabourMapping Obj : labourMapping) {
                    labourids = Obj.getLabourid().getID() ;
                    labournames = Obj.getLabourid().getFullName();
                    labourIDList.add(labourids);
                    labourNamelist.add(labournames);
                }
                Collections.sort(labourIDList);
                Collections.sort(labourNamelist);
              
                if (!StringUtil.isNullOrEmpty(labourids) && !StringUtil.isNullOrEmpty(labournames)) {
//                    labourids = labourids.substring(0, (labourids.length()) - 1);
                    labourids = labourIDList.toString().replaceAll("\\[", "").replaceAll("\\]","");
                   // labournames = labournames.substring(0, (labournames.length()) - 1);
                    labournames = labourNamelist.toString().replaceAll("\\[", "").replaceAll("\\]","");
                    tmpObj.put(workOrder.LABOURID, labourids);
                    tmpObj.put(workOrder.LABOURNAME,labournames);
                }
                
                //*************************WorkOrder Labour mapppings***************************
                //*************************WorkOrder Machine mapppings***************************
                Set<WorkOrderMachineMapping> machineMapping = workOrder.getMachinemapping();
                List<WorkOrderMachineMapping> sortedMachineList=new ArrayList<>(machineMapping);
                sortedMachineList.size();
                String machineids = "";
                String machinenames = "";
                
                List <String> machineIDList=new ArrayList<>();
                List <String> machineNamelist=new ArrayList<>();
                for (WorkOrderMachineMapping Obj : machineMapping) {
                    machineids = Obj.getMachineid().getID();
                    machinenames = Obj.getMachineid().getMachineName();
                    machineIDList.add(machineids);
                    machineNamelist.add(machinenames);
                }
                
                Collections.sort(machineIDList);
                Collections.sort(machineNamelist);
                
                if (!StringUtil.isNullOrEmpty(machineids) && !StringUtil.isNullOrEmpty(machinenames)) {
                    //machineids = machineids.substring(0, (machineids.length()) - 1);
                    machineids = machineIDList.toString().replaceAll("\\[", "").replaceAll("\\]","");
                    //machinenames = machinenames.substring(0, (machinenames.length()) - 1);
                    machinenames = machineNamelist.toString().replaceAll("\\[", "").replaceAll("\\]","");
                    tmpObj.put(workOrder.MACHINEID, machineids);
                    tmpObj.put(workOrder.MACHINENAME, machinenames);
                }
                //*************************WorkOrder Machine mapppings***************************
                //*************************WorkOrder WorkCenter mapppings***************************
                Set<WorkOrderWorkCenterMapping> workcenterMapping = workOrder.getWorkcentermapping();
                List<WorkOrderWorkCenterMapping> sortedWorkCentreList=new ArrayList<>(workcenterMapping);
                sortedWorkCentreList.size();
                String workcenterids = "";
                String workcenterenames = "";
                List <String> workCentreIDList=new ArrayList<>();
                List <String> workCentreNamelist=new ArrayList<>();
                for (WorkOrderWorkCenterMapping Obj : workcenterMapping) {
                    workcenterids = Obj.getWorkcentreid().getID();
                    workcenterenames = Obj.getWorkcentreid().getName();
                    workCentreIDList.add(workcenterids);
                    workCentreNamelist.add(workcenterenames);
                }
                Collections.sort(workCentreIDList);
                Collections.sort(workCentreNamelist);
                if (!StringUtil.isNullOrEmpty(workcenterids) && !StringUtil.isNullOrEmpty(workcenterenames)) {
                    //workcenterids = workcenterids.substring(0, (workcenterids.length()) - 1);
                    workcenterids = workCentreIDList.toString().replaceAll("\\[", "").replaceAll("\\]","");
                    //workcenterenames = workcenterenames.substring(0, (workcenterenames.length()) - 1);
                    workcenterenames = workCentreNamelist.toString().replaceAll("\\[", "").replaceAll("\\]","");
                    tmpObj.put(workOrder.WORKCENTREID, workcenterids);
                    tmpObj.put(workOrder.WORKCENTRENAME, workcenterenames);
                }
                /*
                 Add Global Custom data for document
                 */
                String companyId = (String)requestParams.get("companyid");
                Map globalMap = new HashMap();
                globalMap.put("moduleid", Constants.MRP_WORK_ORDER_MODULEID);
                globalMap.put("companyid", companyId);
                globalMap.put("workorderid", workOrder.getID());
                globalMap.put(Constants.userdf, userdf);
                putGlobalCustomDetailsForWorkOrder(tmpObj, globalMap);
                dataJArr.put(tmpObj);
            }
            jobj.put("TotalCount",result.getRecordTotalCount());
            jobj.put("data",dataJArr);
        }catch(Exception ex) {
            throw ServiceException.FAILURE("", ex);
        }
        return  jobj;
    }
    
    public void putGlobalCustomDetailsForWorkOrder(JSONObject jSONObject, Map<String, Object> map) throws ServiceException, com.krawler.utils.json.base.JSONException {

        String companyId = "";
        int moduleid = 0;
        String workOrderId = "";
        if (map.containsKey("companyid")) {
            companyId = map.get("companyid").toString();
        }
        if (map.containsKey("moduleid")) {
            moduleid = Integer.parseInt(map.get("moduleid").toString());
        }
        if (map.containsKey("workorderid")) {
            workOrderId = map.get("workorderid").toString();
        }
        // ## Get Custom Field Data 
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, moduleid));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        Map<String, Object> variableMap = new HashMap<String, Object>();

        KwlReturnObject customObjresult = null;
        customObjresult = accountingHandlerDAOobj.getObject(WorkOrderCustomData.class.getName(), workOrderId);
        replaceFieldMap = new HashMap<String, String>();
        if (customObjresult != null && customObjresult.getEntityList().size() > 0) {
            WorkOrderCustomData workOrderCustomData = (WorkOrderCustomData) customObjresult.getEntityList().get(0);
            if (workOrderCustomData != null) {
                AccountingManager.setCustomColumnValues(workOrderCustomData, FieldMap, replaceFieldMap, variableMap);
                JSONObject params = new JSONObject();
                params.put("companyid", companyId);
                params.put("isExport", true);
                params.put(Constants.userdf, map.get(Constants.userdf));
                fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
            }
        }
    }
    
    @Override
    public JSONObject getProductsForCombo(HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        KwlReturnObject kmsg = null;
        try {
            String companyid = requestParams.get(WorkOrder.COMPANYID).toString();
            KwlReturnObject result = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences preferences = (ExtraCompanyPreferences) result.getEntityList().get(0);
            String salesOrderid = "";
            if (requestParams.get(WorkOrder.SALESORDERID) != null) {
                salesOrderid = requestParams.get(WorkOrder.SALESORDERID).toString();
            }
            String salesContractid = "";
            if (requestParams.get(WorkOrder.SALESCONTRACTID) != null) {
                salesContractid = requestParams.get(WorkOrder.SALESCONTRACTID).toString();
            }
            if ( !StringUtil.isNullOrEmpty(salesOrderid)) {
                kmsg = workOrderDAOObj.getProductsForCombo(requestParams,true);
            } else if (!StringUtil.isNullOrEmpty(salesContractid)) {
                kmsg = workOrderDAOObj.getProductsForCombo(requestParams,false);
            } else {
                requestParams.remove("start");
                requestParams.remove("limit");
                requestParams.put(Constants.PRODUCT_SEARCH_FLAG,preferences.getProductSearchingFlag());
                kmsg = accProductDaoObj.getProductsForCombo(requestParams);
            }
            List prodList = kmsg.getEntityList();
            Iterator ite = prodList.iterator();
            for(Object object: prodList) {
               Product product=null;
               double quantity=1;
                JSONObject obj = new JSONObject();
                if (!StringUtil.isNullOrEmpty(salesOrderid) || !StringUtil.isNullOrEmpty(salesContractid)) {
                    Object tmpObj[] = (Object[]) object;
                    product = (Product) tmpObj[0];
                    quantity = (double) tmpObj[1];
                } else {
                    product = (Product) object;
                }


                if (!StringUtil.isNullOrEmpty(salesOrderid)) {
                    requestParams.put("id", salesOrderid);//sales order id.
                    requestParams.put("productid", product.getID());//product id.
                    requestParams.put("linkfrom", "1");
                    KwlReturnObject workorderresult = workOrderDAOObj.getWorkorderFromSOandProduct(requestParams);
                    List wolost = workorderresult.getEntityList();

                    if (!wolost.isEmpty() && wolost != null) {
                        Iterator woiterator = wolost.iterator();
                        int woquantity = 0;
                        double totalwoquantity=0; 
                        while (woiterator.hasNext()) {
                            Object obj2[] = (Object[]) woiterator.next();
                            woquantity = (int) obj2[1];
                            totalwoquantity+=woquantity;
                        }
                        if (totalwoquantity < quantity) {
                            obj.put("quantity", quantity - totalwoquantity);
                        }else{
                        continue;
                        }
                        } else {
                        obj.put("quantity", quantity);
                        }

                } else {

                    obj.put("quantity", quantity);

                }
               
//                if (product.getID().equals(productid)) {
//                    continue;
//                }
                String productType = "";
                //String companyid = sessionHandlerImpl.getCompanyid(request);
                productType = (product.getProducttype() != null ? product.getProducttype().getID(): "");
                if (productType.equals(Producttype.ASSEMBLY)) {
                    obj.put("productid", product.getID());
                    obj.put("productname", product.getName());
                    obj.put("desc", product.getDescription());
                    obj.put("isAsset", product.isAsset());
                    obj.put("supplierpartnumber", StringUtil.isNullOrEmpty(product.getSupplier()) ? "" : product.getSupplier());
                    obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
                    obj.put("minorderingquantity", product.getMinOrderingQuantity());
                    obj.put("maxorderingquantity", product.getMaxOrderingQuantity());
                    obj.put("reorderQuantity", product.getReorderQuantity());
                    obj.put("uomschematypeid", product.getUomSchemaType() != null ? product.getUomSchemaType().getID() : "");
//                    if (!onlyProduct) {
//                        UnitOfMeasure uom = product.getUnitOfMeasure();
//                        UnitOfMeasure purchaseuom = product.getPurchaseUOM();
//                        UnitOfMeasure salesuom = product.getSalesUOM();
//                        UnitOfMeasure orderingUoM = product.getOrderingUOM();
//                        UnitOfMeasure transferingUoM = product.getTransferUOM();
//                        obj.put("uomid", uom == null ? "" : uom.getID());
//                        obj.put("uomname", uom == null ? "" : uom.getName());
//                        obj.put("purchaseuom", purchaseuom == null ? "" : purchaseuom.getID());
//                        obj.put("salesuom", salesuom == null ? "" : salesuom.getID());
//                        obj.put("salesuomname", salesuom == null ? "" : salesuom.getName());
//                        obj.put("purchaseuomname", purchaseuom == null ? "" : purchaseuom.getName());
//                        obj.put("orderinguomname", orderingUoM == null ? "" : orderingUoM.getName());
//                        obj.put("orderinguomid", orderingUoM == null ? "" : orderingUoM.getID());
//                        obj.put("transferinguomname", transferingUoM == null ? "" : transferingUoM.getName());
//                        obj.put("transferinguomid", transferingUoM == null ? "" : transferingUoM.getID());
//                        obj.put("stockpurchaseuomvalue", (product.getPackaging() != null && purchaseuom != null) ? product.getPackaging().getStockUomQtyFactor(purchaseuom) : 1);
//                        obj.put("stocksalesuomvalue", (product.getPackaging() != null && salesuom != null) ? product.getPackaging().getStockUomQtyFactor(salesuom) : 1);
//                        obj.put("multiuom", product.isMultiuom());
                        obj.put("isLocationForProduct", product.isIslocationforproduct());
                        obj.put("isWarehouseForProduct", product.isIswarehouseforproduct());
                        obj.put("isRowForProduct", product.isIsrowforproduct());
                        obj.put("isRackForProduct", product.isIsrackforproduct());
                        obj.put("isBinForProduct", product.isIsbinforproduct());
                        obj.put("isBatchForProduct", product.isIsBatchForProduct());
                        obj.put("isSerialForProduct", product.isIsSerialForProduct());
                        obj.put("isSKUForProduct", product.isIsSKUForProduct());
//                        obj.put("isRecyclable", product.isRecyclable());
//                        obj.put("recycleQuantity", product.getRecycleQuantity());
                        obj.put("isWastageApplicable", product.isWastageApplicable());
//                        obj.put("purchaseacctaxcode", ((product.getPurchaseAccount() != null && (!StringUtil.isNullOrEmpty(product.getPurchaseAccount().getTaxid()))) ? product.getPurchaseAccount().getTaxid() : ""));
//                        obj.put("salesacctaxcode", ((product.getSalesAccount() != null && (!StringUtil.isNullOrEmpty(product.getSalesAccount().getTaxid()))) ? product.getSalesAccount().getTaxid() : ""));
//                        obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : ""));
//                        obj.put("salesretaccountid", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getID() : ""));
//                        obj.put("location", (product.getLocation() != null ? product.getLocation().getId() : ""));
//                        obj.put("warehouse", (product.getWarehouse() != null ? product.getWarehouse().getId() : ""));
//                        KwlReturnObject purchase = accProductDaoObj.getProductPrice(product.getID(), true, null, "", "");
//                        obj.put("purchaseprice", purchase.getEntityList().get(0));
//                        KwlReturnObject sales = accProductDaoObj.getProductPrice(product.getID(), false, null, "", "");
//                        obj.put("saleprice", sales.getEntityList().get(0));
//
//                        KwlReturnObject result = accProductDaoObj.getQuantity(product.getID());
//                        obj.put("quantity", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));
//
//                        KwlReturnObject result2 = accProductDaoObj.getAssemblyLockQuantity(product.getID());//get the lock quantity of assembly type of product locked in SO
//                        Double assmblyLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));
//
//                        KwlReturnObject result1 = accProductDaoObj.getLockQuantity(product.getID());
//                        Double SoLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));
//
//                        obj.put("lockquantity", assmblyLockQuantity + SoLockQuantity);
//
//                        KwlReturnObject result4 = accProductDaoObj.getVendorConsignedQuantity(product.getID());
//                        obj.put("venconsignuomquantity", (result4.getEntityList().get(0) == null ? 0 : result4.getEntityList().get(0)));
//
//                        KwlReturnObject result5 = accProductDaoObj.getConsignedQuantity(product.getID());
//                        obj.put("consignquantity", (result5.getEntityList().get(0) == null ? 0 : result5.getEntityList().get(0)));
//
//                    }
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
                    obj.put("uomschematype", product.getUomSchemaType() != null ? product.getUomSchemaType().getID() : "");
                    obj.put("ismultipleuom", product.isMultiuom());

                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
        } catch(Exception ex) {
            Logger.getLogger(AccWorkOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    @Override
    public JSONObject getExpanderWOStockDetails(HashMap<String, Object> requestParam) throws ServiceException {
        JSONObject stockDetailsJSONObjet = new JSONObject();
        JSONObject FinishedProductIDObj = new JSONObject();
        JSONArray stockDetailsJSONArray = new JSONArray();
        try {
            
            String workorderid = "",FinishedProductID = "",productid = "", productname = "", producttype = "", uom = "",companyID;
            int ComponentType;
            double requiredQuantity, blockedQuantity, consumptionQuantity, wastageQuantity, recycleQuantity, returnedQuantity, produceQuantity, unitPrice, totalAmount, productAssemblyQuantity, WOQuantity, QuantityBasedOnWOQuantity , initialPurchaseprice;
           
            WorkOrder WorkOrderObj = null;
            Product product = null;
            
            workorderid = requestParam.get(WorkOrder.WOID).toString();
            KwlReturnObject kwrl = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), workorderid);
            WorkOrderObj = (WorkOrder) kwrl.getEntityList().get(0);
            
            companyID=requestParam.get(WorkOrder.COMPANYID).toString();
                    
            
           
            if (WorkOrderObj != null) {
               
                WOQuantity = 0;
                WOQuantity = WorkOrderObj.getQuantity();
                
                FinishedProductID = WorkOrderObj.getProductID().getID();
                FinishedProductIDObj.put("productid", FinishedProductID);
                KwlReturnObject kwl = accProductDaoObj.getSubAssemblyProduct(FinishedProductIDObj);
                List<ProductAssembly> productAssemblyList = kwl.getEntityList();
               
                ArrayList<String> componentTypeList = new ArrayList<String>();
                componentTypeList.add(Constants.MRP_DEFAULT_TYPE_NO,Constants.MRP_DEFAULT_TYPE_NAME);
                componentTypeList.add(Constants.MRP_COMPONENT_TYPE_NO,Constants.MRP_COMPONENT_TYPE_NAME);
                componentTypeList.add(Constants.MRP_COPRODUCT_TYPE_NO,Constants.MRP_COPRODUCT_TYPE_NAME);
                componentTypeList.add(Constants.MRP_SCRAP_TYPE_NO,Constants.MRP_SCRAP_TYPE_NAME);
                
                
                Map<String,Object> workorderParams=new HashMap();  
                workorderParams.put("companyid", companyID); //Company ID
                workorderParams.put("bills", workorderid); //Work order ID
                
                KwlReturnObject result = workOrderDAOObj.getWorkOrderComponentDetails(workorderParams); //This Method is used to get WOCD in Sequence format which was Define at BOM level. 
                List WorkOrderComponentDetailsList=result.getEntityList();
                Iterator itr=WorkOrderComponentDetailsList.iterator();
         
                    while (itr.hasNext()) {
                    WorkOrderComponentDetails WorkOrderComponentDetailsObj = (WorkOrderComponentDetails) itr.next();
                    if (!(WorkOrderObj.getProductID().getID().equals(WorkOrderComponentDetailsObj.getProduct().getID()))) {

                        JSONObject productDetailObj = new JSONObject();
                        product = WorkOrderComponentDetailsObj.getProduct();

                        ComponentType = 0;
                        requiredQuantity = blockedQuantity = consumptionQuantity = wastageQuantity = recycleQuantity = returnedQuantity = produceQuantity = unitPrice = totalAmount = productAssemblyQuantity = QuantityBasedOnWOQuantity = initialPurchaseprice = 0;

                        /**
                         * Product Related Information: ID Name
                         * ProductType(Inventory part,Assembly Product) UOM(Unit
                         * of Measure).
                         */
                        productid = product.getProductid();
                        productname = product.getName();
                        producttype = product.getProducttype().getName();
                        uom = product.getUnitOfMeasure().getName();


                        /**
                         * Stock Detail Related Information: Required quantity
                         * Blocked quantity Consumption Quantity Wastage
                         * Quantity Recycle Quantity Returned Quantity produce
                         * Quantity ComponentType (BOM Type) 0:---------
                         * 1:Component 2:Co-Product 3:Scrap UnitPrice Total
                         * Amount.
                         */
                        requiredQuantity = authHandler.round(WorkOrderComponentDetailsObj.getRequiredQuantity(), companyID);
                        blockedQuantity = authHandler.round(WorkOrderComponentDetailsObj.getBlockQuantity(), companyID);
                        consumptionQuantity = authHandler.round(WorkOrderComponentDetailsObj.getBlockQuantityUsed(), companyID);
                        wastageQuantity = authHandler.round(WorkOrderComponentDetailsObj.getWastedQuantity(), companyID);
                        recycleQuantity = authHandler.round(WorkOrderComponentDetailsObj.getRecycledQuantity(), companyID);
                        returnedQuantity = authHandler.round(WorkOrderComponentDetailsObj.getReturnQuantity(), companyID);
                        produceQuantity = authHandler.round(WorkOrderComponentDetailsObj.getProducedQuantity(), companyID);
                        initialPurchaseprice = authHandler.round(WorkOrderComponentDetailsObj.getInitialPurchasePrice(), companyID);


                        for (ProductAssembly assembly : productAssemblyList) {
                            if ((assembly.getSubproducts().getID().equals(WorkOrderComponentDetailsObj.getProduct().getID()))) {
                                ComponentType = assembly.getComponentType();
                                productAssemblyQuantity = authHandler.round(assembly.getQuantity(), companyID);
                            }
                        }

                        if (ComponentType == 2 || ComponentType == 3) {
                            if (produceQuantity == 0) {
                                QuantityBasedOnWOQuantity = authHandler.round(WOQuantity * productAssemblyQuantity, companyID);
                                unitPrice = authHandler.round(((initialPurchaseprice) / (QuantityBasedOnWOQuantity)), companyID);
                                totalAmount = initialPurchaseprice;
                            } else {
                                unitPrice = authHandler.round((initialPurchaseprice) / (produceQuantity), companyID);
                                totalAmount = initialPurchaseprice;
                            }
                        } else {
                            unitPrice = initialPurchaseprice;
                            if (consumptionQuantity == 0) {
                                totalAmount = authHandler.round((unitPrice) * (blockedQuantity), companyID);
                            } else {
                                totalAmount = authHandler.round((unitPrice) * (consumptionQuantity), companyID);
                            }
                        }


                        productDetailObj.put("productid", productid);
                        productDetailObj.put("productname", productname);
                        productDetailObj.put("producttype", producttype);
                        productDetailObj.put("requiredquantity", requiredQuantity);
                        productDetailObj.put("blockedquantity", blockedQuantity);
                        productDetailObj.put("consumptionquantity", consumptionQuantity);
                        productDetailObj.put("wastagequantity", wastageQuantity);
                        productDetailObj.put("recyclequantity", recycleQuantity);
                        productDetailObj.put("returnedquantity", returnedQuantity);
                        productDetailObj.put("bomtype", componentTypeList.get(ComponentType));
                        productDetailObj.put("producedquantity", produceQuantity);
                        productDetailObj.put("uom", uom);
                        productDetailObj.put("unitprice", unitPrice);
                        productDetailObj.put("totalamount", totalAmount);
                        productDetailObj.put("id", workorderid);
                        stockDetailsJSONArray.put(productDetailObj);
                    }
                }
            }
            
            stockDetailsJSONObjet.put("data", stockDetailsJSONArray);


        } catch (Exception ex) {
            Logger.getLogger(AccWorkOrderServiceImpl.class.getName()).log(Level.SEVERE, "Erroe occurred while showing expander Stock details in Workorder Stock Details Report", ex);
            throw ServiceException.FAILURE("Erroe occurred while showing expander Stock details in Workorder Stock Details Report", ex);

        }

        return stockDetailsJSONObjet;
    }

    @Override
    public JSONObject getExpanderDetails(HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        KwlReturnObject kmsgM = null;
        KwlReturnObject kmsgL = null;
        try {
            String woid = requestParams.get(WorkOrder.WOID).toString();
            boolean isExpanderDetails = true;
            requestParams.put("isExpanderDetails",isExpanderDetails);
            kmsgL = workOrderDAOObj.getLabourForWorkOrder(requestParams);
            kmsgM = workOrderDAOObj.getMachineForWorkOrder(requestParams);
           
            List lList = kmsgL.getEntityList();
            List mList = kmsgM.getEntityList();
                       
            WorkOrder word = (WorkOrder) accountingHandlerDAOobj.getObject("com.krawler.spring.mrp.WorkOrder.WorkOrder", woid).getEntityList().get(0);

            Set<WorkOrderWorkCenterMapping> wcList = word.getWorkcentermapping();
            List<String> sortedList=new ArrayList<>();
            JSONObject tempJobj = new JSONObject();

            //***********************************************Get Labour Details************************************
            for (Object Obj : lList) {

                Object ObjArr[] = (Object[]) Obj;
                StringBuilder wcnamesLabour = new StringBuilder();            
                StringBuilder sbf = new StringBuilder(ObjArr[0].toString());
                tempJobj = new JSONObject();
                tempJobj.put(WorkOrder.WOID, woid);
                tempJobj.put("labourid", sbf.toString());
                tempJobj.put("labourname", ObjArr[1]);
                tempJobj.put("islabourOrmachine", 0);

                for (WorkOrderWorkCenterMapping wcm : wcList) {
                    WorkCentre wc = wcm.getWorkcentreid();
                    Set<LabourWorkCentreMapping> wclbrm = wc.getLabourworkcentremappings();
                    for (LabourWorkCentreMapping lbrMap : wclbrm) {
                        Labour lb = lbrMap.getLabour();
                        if (sbf.toString().equals(lb.getID())) {
                            sortedList.add(wc.getName());
                        }
                    }
                }
                Collections.sort(sortedList);
                StringBuilder fnString = new StringBuilder();
                if(!StringUtil.isNullOrEmpty(sortedList.toString())) {
                    fnString = new StringBuilder(sortedList.toString().replaceAll("\\[", "").replaceAll("\\]",""));
                }
                sortedList.clear();
                tempJobj.put("workcentrename", fnString.toString());

                jarr.put(tempJobj);
            }
            //***********************************************Get Machine Details************************************
            for (Object Obj : mList) {

                Object ObjArr[] = (Object[]) Obj;
                StringBuilder wcnamesMachine = new StringBuilder();
                StringBuilder sbString = new StringBuilder(ObjArr[0].toString());
                tempJobj = new JSONObject();
                tempJobj.put(WorkOrder.WOID, woid);
                tempJobj.put("machineid", ObjArr[0]);
                tempJobj.put("machinename", ObjArr[1]);
                tempJobj.put("islabourOrmachine", 1); //0 - labour, 1- Machine

                for (WorkOrderWorkCenterMapping wcm : wcList) {
                    WorkCentre wc = wcm.getWorkcentreid();
                    Set<MachineWorkCenterMapping> wcMch = wc.getMachineworkcentremappings();
                    for (MachineWorkCenterMapping McMap : wcMch) {
                        Machine mc = McMap.getMachineID();
                        if (sbString.toString().equals(mc.getID())) {
                           // wcnamesMachine.append(wc.getName() + ", ");
                            sortedList.add(wc.getName());
                        }
                    }
                }
                Collections.sort(sortedList);
                StringBuilder fnString = new StringBuilder();
                if(!StringUtil.isNullOrEmpty(sortedList.toString())) {
                   // fnString = new StringBuilder(wcnamesMachine.toString().substring(0, wcnamesMachine.toString().length() - 2));
                    fnString = new StringBuilder(sortedList.toString().replaceAll("\\[", "").replaceAll("\\]",""));
                }
                sortedList.clear();
                tempJobj.put("workcentrename", fnString.toString());
                
                jarr.put(tempJobj);
            }
            jobj.put("data", jarr);
        } catch (Exception ex) {
            Logger.getLogger(AccWorkOrderServiceImpl.class.getName()).log(Level.SEVERE, "Erroe occurred while showing expander details in workorder", ex);
            throw  ServiceException.FAILURE("Erroe occurred while showing expander details in workorder", ex);
        }
        return jobj;
    }
    
     public JSONArray getWorkOrderComponentDetails(Map<String, Object> requestParams) throws ServiceException {
        JSONArray dataJArr = new JSONArray();
        try {
            KwlReturnObject result = workOrderDAOObj.getWorkOrderComponentDetails(requestParams);
            String companyid = requestParams.get("companyid").toString();
            CompanyAccountPreferences preferences=(CompanyAccountPreferences)requestParams.get("preferences");
            String productidstr = "";
            boolean isFromWO = false;
            boolean genpocheck = false;
            boolean isForCompAvailablity=false;
            if (requestParams.get("productidstr") != null) {
                productidstr = requestParams.get("productidstr").toString();
            }
            if (requestParams.get("isFromWO") != null) {
                isFromWO = Boolean.parseBoolean(requestParams.get("isFromWO").toString());
            }
             if (requestParams.containsKey("isForCompAvailablity") && requestParams.get("isForCompAvailablity") != null && !StringUtil.isNullOrEmpty(requestParams.get("isForCompAvailablity").toString())) {
                isForCompAvailablity = Boolean.parseBoolean(requestParams.get("isForCompAvailablity").toString());
            }
            List dataList = result.getEntityList();
            JSONObject tmpObj = new JSONObject();
            for (Object obj : dataList) {
                WorkOrderComponentDetails componentDetails = (WorkOrderComponentDetails) obj;
                tmpObj = new JSONObject();
                Product product = componentDetails.getProduct();
                String prodcutid = product.getID();
                PurchaseOrder POobj = null;
                PurchaseRequisition PReqobj = null;
                HashMap<String, Object> params =  new HashMap<>();
                KwlReturnObject kmsg1 = null;
                List dataList1 = null;
                if (productidstr.contains(prodcutid) || isFromWO) {
                    params.put("wodetailid", componentDetails.getID());
                    kmsg1 = workOrderDAOObj.getPOforWOdetail(params);
                    dataList1 = kmsg1.getEntityList();
                    if (dataList1.size() > 0) {
                        String POid = dataList1.get(0).toString();
                        POobj = (PurchaseOrder) accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.PurchaseOrder", POid).getEntityList().get(0);
                        tmpObj.put("POCode", POobj.getPurchaseOrderNumber());
                    }
                    kmsg1 = workOrderDAOObj.getPReqforWOdetail(params);
                    dataList1 = kmsg1.getEntityList();
                    if (dataList1.size() > 0) {
                        String POid = dataList1.get(0).toString();
                        PReqobj = (PurchaseRequisition) accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.PurchaseRequisition", POid).getEntityList().get(0);
                        tmpObj.put("POCode", PReqobj.getPrNumber());
                    }
                    KwlReturnObject result2 = accProductDaoObj.getAssemblyLockQuantity(product.getID());
                    Double assmblyLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));

                    KwlReturnObject result1 = accProductDaoObj.getLockQuantity(product.getID());
                    Double SoLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));

                    KwlReturnObject woresult = accProductDaoObj.getWOLockQuantity(product.getID());
                    Double WOLockQuantity = (Double) (woresult.getEntityList().get(0) == null ? 0.0 : woresult.getEntityList().get(0));
                    double totalLockQuantity = assmblyLockQuantity + SoLockQuantity + WOLockQuantity;
                    double availableQuantity = product!= null ? product.getAvailableQuantity() : 0.0;
                    double availableQuantityForUse = availableQuantity - totalLockQuantity;
                    if (availableQuantityForUse < 0) {
                        availableQuantityForUse = 0.0;
                    }
                    /** (this is implemented to distinguish blocked from Component Availability window and blocked from manage quantity window)
                     * if product quantity is blocked from Component Availability window then only show block quantity.
                     * otherwise show 0.
                     */
                    
                    double blockedFromCA = componentDetails.isBlockedFromCA() ? componentDetails.getBlockQuantity() : 0;
                    tmpObj.put(WorkOrderComponentDetails.PARAM_ID, componentDetails.getID());
                    tmpObj.put(WorkOrderComponentDetails.PARAM_AVAILABLE_QUANTITY, availableQuantityForUse);
                    tmpObj.put(WorkOrderComponentDetails.PARAM_BLOCKED_QUANTITY, blockedFromCA);            
                    tmpObj.put(WorkOrderComponentDetails.PARAM_REQUIRED_QUANTITY, componentDetails.getRequiredQuantity());
                    double reqQuantity = componentDetails.getRequiredQuantity();
                    tmpObj.put(WorkOrderComponentDetails.PARAM_PURCHASE_PRICE, componentDetails.getInitialPurchasePrice());
                    tmpObj.put("outstandingquantity", totalLockQuantity - (componentDetails.getBlockQuantity() - componentDetails.getBlockQuantityUsed()));        // subtracting self blocked quantities from total blocked quantities
                    tmpObj.put("minpercentquantity", componentDetails.getMinpercent());
                    tmpObj.put("parentproductid", componentDetails.getParentProduct()!= null? componentDetails.getParentProduct().getID():"");
                    
                    //NEED to change logic
                    // String workorderDefaultstatus =((woObj.getWorkOrderStatus() != null && woObj.getWorkOrderStatus().getDefaultMasterItem() != null) ?  woObj.getWorkOrderStatus().getDefaultMasterItem().getID() : "");
                    String woStatus_planned="";
                    KwlReturnObject kmsg = null;
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("defaultStatusId", Constants.defaultWOstatus_PLANNED);
                    map.put("companyId", companyid);
                    kmsg = workOrderDAOObj.getWOStatusidFromDefaultID(map);         // fetching company's In process id based on default WO In process id
                    if (kmsg.getEntityList().size() > 0) {
                         MasterItem miObj = (MasterItem) kmsg.getEntityList().get(0);
                         woStatus_planned=miObj.getID();
                    }                    
                    if (isForCompAvailablity && !(componentDetails.getWorkOrder().getWorkOrderStatus().getID().equals(woStatus_planned)) && ((product.getProducttype().getID().equals(Producttype.ASSEMBLY)) || (componentDetails.getBlockQuantity() > 0))) {
                        tmpObj.put("Disabled", true);
                    } else {
                        tmpObj.put("Disabled", false);
                    }
                    
                    double shortfallQuantity = reqQuantity - availableQuantityForUse;
                    double reorderQuantity = product != null ? product.getReorderQuantity() : 0.0;
                    if (shortfallQuantity < 0) {
                        shortfallQuantity = shortfallQuantity < 0 ? 0.0 : shortfallQuantity;
                    } else {
                        shortfallQuantity = shortfallQuantity < 0 ? 0.0 : shortfallQuantity;
                    }
                    tmpObj.put("shortfallquantity", shortfallQuantity);
                    tmpObj.put("orderquantity", reorderQuantity);
                    if (shortfallQuantity > 0) {
                        tmpObj.put("genpo", "yes");
                        tmpObj.put("genpocheck", true);
                        if (isForCompAvailablity) {
                            tmpObj.put("genpo", "yes");
                            tmpObj.put("genpocheck", false);
                        }
                    } else {
                        tmpObj.put("genpo", "no");
                        tmpObj.put("genpocheck", false);
                    }
                    tmpObj.put(WorkOrder.PRODUCTID, product != null ? product.getID() : "");
                    tmpObj.put(WorkOrder.PRODUCTNAME, product != null ? product.getProductName() : "");
                    tmpObj.put(WorkOrderComponentDetails.PARAM_PRODUCT_DESC, product.getDescription() != null ? product.getDescription() : "");
                    tmpObj.put(WorkOrderComponentDetails.PARAM_PRODUCT_TYPE_ID, product.getProducttype().getID());
                    tmpObj.put(WorkOrderComponentDetails.PARAM_PRODUCT_TYPE_NAME, product.getProducttype().getName());
                    tmpObj.put("unitname", product.getUnitOfMeasure() != null? product.getUnitOfMeasure().getNameEmptyforNA(): "");
                    tmpObj.put("location", product.getLocation() != null? product.getLocation().getId(): "");
                    tmpObj.put("warehouse", product.getWarehouse() != null? product.getWarehouse().getId(): "");
                    tmpObj.put("israteIncludingGst", false);
                    tmpObj.put("wodetailid", componentDetails.getID());
                    tmpObj.put("isLocationForProduct", product.isIslocationforproduct());
                    tmpObj.put("isWarehouseForProduct", product.isIswarehouseforproduct());
                    tmpObj.put("isBatchForProduct", product.isIsBatchForProduct());
                    tmpObj.put("isSerialForProduct", product.isIsSerialForProduct());
                    tmpObj.put("isRowForProduct", product.isIsrowforproduct());
                    tmpObj.put("isRackForProduct", product.isIsrackforproduct());
                    tmpObj.put("isBinForProduct", product.isIsbinforproduct());
//                    if ((preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory())) {  //check if company level option is on then only we will check productt level
                        if (product.isIsBatchForProduct() || product.isIsSerialForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct()) {  //product level batch and serial no on or not
                            JSONObject paramJobj = new JSONObject();
                            paramJobj.put("linkingFlag", false);
                            paramJobj.put("isEdit", true);
                            paramJobj.put("isConsignment", false);
                            paramJobj.put("companyid",companyid );
                            if(isForCompAvailablity && !(product.getProducttype().getID().equals(Producttype.ASSEMBLY))){
                             paramJobj.put("moduleid",""+Constants.MRP_WORK_ORDER_MODULEID);//Workorder Moduleid                                                          
                            }
                            String batchdetails = getNewBatchJson(product, paramJobj, componentDetails.getID());
                            if (!StringUtil.isNullOrEmpty(batchdetails)) {
                                tmpObj.put("batchdetails", batchdetails);
                            }
                        }
//                    }
                    tmpObj.put("isAsset", false);
                    tmpObj.put("baseuomquantity", 1);
                    tmpObj.put("balanceQuantity", 1);
                    tmpObj.put("type", product.getProducttype() != null ? product.getProducttype().getName() : "");
                    tmpObj.put("salesPerson", "");
                    tmpObj.put("productid", product.getID());
                    tmpObj.put("copyquantity", 1);
                    tmpObj.put("description", product.getDescription());
                    tmpObj.put("taxamount", 0);
                    tmpObj.put("linkid", "");
                    double qty  = 0.0 ;
                    qty =  componentDetails.getRequiredQuantity()- componentDetails.getAvailableQuantity();
                    if (qty < product.getReorderQuantity()) {
                        qty  = product.getReorderQuantity();
                    }
                    tmpObj.put("quantity", qty);
                    tmpObj.put("prdiscount", 0);
                    tmpObj.put("uomid", product.getUnitOfMeasure().getID());
                    tmpObj.put("amountForExcelFile", "11.00");
                    tmpObj.put("dorowid", "");
                    tmpObj.put("lockquantity", 0);
                    tmpObj.put("rateIncludingGst", 0);
                    tmpObj.put("priceSource", "");
                    tmpObj.put("rowTaxPercent", 0);
                    tmpObj.put("status", "N/A");
                    tmpObj.put("rowTaxAmount", 0);
                    tmpObj.put("currencysymbol", "SGD");
                    tmpObj.put("islockQuantityflag", false);
                    tmpObj.put("baseuomrate", 1);
                    tmpObj.put("srno", 1);
                    tmpObj.put("availableQtyInSelectedUOM", 1);
                    tmpObj.put("multiuom", false);
                    tmpObj.put("orderrate", 11);
                    tmpObj.put("desc", product.getDescription());
                    tmpObj.put("memo", "");
                    tmpObj.put("originalTransactionRowid", "");
                    tmpObj.put("recTermAmount", 0);
                    tmpObj.put("OtherTermNonTaxableAmount", 0);
                    tmpObj.put("uomname", "Unit");
                    tmpObj.put("invlocation", "");
                    tmpObj.put("LineTermdetails", "[]");
                    tmpObj.put("rate", componentDetails.getInitialPurchasePrice());
                    tmpObj.put("dquantity", 1);
                    tmpObj.put("prtaxid", "");
                    tmpObj.put("productname", product.getProductName());
                    tmpObj.put("linktype", -1);
                    tmpObj.put("prtaxpercent", 0);
                    tmpObj.put("baseuomname", "Unit");
                    tmpObj.put("invstore", "");
                    tmpObj.put("lockquantitydue", 0);
                    tmpObj.put("pid", product.getProductid());
                    tmpObj.put("linkto", "");
                    tmpObj.put("discountvalue", 0);
                    tmpObj.put("billid", componentDetails.getWorkOrder().getID());
                    tmpObj.put("isConsignment", false);
                    tmpObj.put("hasAccess", true);
                    tmpObj.put("baseuomid", "");
                    tmpObj.put("discountispercent", 1);
                    tmpObj.put("billno", componentDetails.getWorkOrder().getWorkOrderID());
                    tmpObj.put("rowid", componentDetails.getID());
                    dataJArr.put(tmpObj);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccWorkCentreServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dataJArr;
    }
     
     public String getNewBatchJson(Product product,JSONObject paramJobj, String documentid) throws ServiceException, SessionExpiredException, com.krawler.utils.json.base.JSONException {
        JSONArray jSONArray = new JSONArray();
        DateFormat df = authHandler.getDateFormatter(paramJobj);
        KwlReturnObject kmsg = null;
        boolean linkingFlag = paramJobj.optBoolean("linkingFlag",false);
        boolean isEdit=paramJobj.optBoolean("isEdit",false);
        boolean isConsignment=paramJobj.optBoolean("isConsignment",false);
        String moduleID = paramJobj.optString("moduleid",null);
          boolean isBatch=false;
        if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && !product.isIsrowforproduct() && !product.isIsrackforproduct() && !product.isIsbinforproduct() && product.isIsSerialForProduct()) {
            kmsg = accCommonTablesDAOObj.getOnlySerialDetails(documentid, linkingFlag, moduleID,isConsignment,isEdit);
        } else {
              isBatch=true;
            kmsg = accCommonTablesDAOObj.getBatchSerialDetails(documentid, !product.isIsSerialForProduct(), linkingFlag, moduleID,isConsignment,isEdit,"");
        }
        
        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList((String)paramJobj.get(Constants.companyKey), Constants.SerialWindow_ModuleId, 1));
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
        // HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
        HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
//        product.getName()
         double ActbatchQty = 1;
        double approvedSerialQty = 0;
        double batchQty = 0;
        List batchserialdetails = kmsg.getEntityList();
        Iterator iter = batchserialdetails.iterator();
        while (iter.hasNext()) {
            Object[] objArr = (Object[]) iter.next();
            JSONObject obj = new JSONObject();
            obj.put("id", objArr[0] != null ? (String) objArr[0] : "");
            obj.put("batch", objArr[1] != null ? (String) objArr[1] : "");
            obj.put("batchname", objArr[1] != null ? (String) objArr[1] : "");
            obj.put("location", objArr[2] != null ? (String) objArr[2] : "");
            obj.put("warehouse", objArr[3] != null ? (String) objArr[3] : "");
           if (isBatch){
                obj.put("row", objArr[15] != null ? (String) objArr[15] : "");
                obj.put("rack", objArr[16] != null ? (String) objArr[16] : "");
                obj.put("bin", objArr[17] != null ? (String) objArr[17] : "");
         }
            if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct()) && product.isIsSerialForProduct()) {
                if (isConsignment && linkingFlag) {
                      ActbatchQty = accCommonTablesDAOObj.getApprovedSerialQty(documentid, (String) objArr[0],isEdit);
                } else {
                     ActbatchQty = accCommonTablesDAOObj.getBatchQuantity(documentid, (String) objArr[0]);
                }

                if (batchQty == 0) {
                    batchQty =  ActbatchQty;
                }
                if (batchQty == ActbatchQty) {
                    obj.put("isreadyonly", false);
                    obj.put("quantity", ActbatchQty);
                } else {
                    obj.put("isreadyonly", true);
                    obj.put("quantity", "");
                }

            } else {
                obj.put("isreadyonly", false);
                obj.put("quantity", ActbatchQty);
            }
            if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && !product.isIsrowforproduct() && !product.isIsrackforproduct() && !product.isIsbinforproduct() && product.isIsSerialForProduct()) {
                obj.put("mfgdate", "");
                obj.put("expdate", "");
            } else {
                obj.put("mfgdate", objArr[4] != null ? df.format(objArr[4]) : "");
                obj.put("expdate", objArr[5] != null ? df.format(objArr[5]) : "");
            }

//            obj.put("quantity", objArr[6] != null ? objArr[6] : "");
//            obj.put("quantity",ActbatchQty);
            obj.put("lockquantity", objArr[12] != null ? objArr[12] : "");
            
            if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct()) && !product.isIsSerialForProduct()) {
                obj.put("quantity", objArr[11] != null ? objArr[11] : "");
            }

            if (!StringUtil.isNullOrEmpty(product.getID())) {
                obj.put("productid", product.getID());
            }
            obj.put("balance", 0);
            obj.put("asset", "");
            obj.put("serialnoid", objArr[7] != null ? (String) objArr[7] : "");
            obj.put("serialno", objArr[8] != null ? (String) objArr[8] : "");
            obj.put("skufield", objArr[13] != null ? (String) objArr[13] : "");
            obj.put("purchasebatchid", objArr[0] != null ? (String) objArr[0] : "");
            obj.put("purchaseserialid", objArr[7] != null ? (String) objArr[7] : "");
            obj.put("expstart", (objArr[9] != null && !objArr[9].toString().equalsIgnoreCase("")) ? df.format(objArr[9]) : "");
            obj.put("expend", (objArr[10] != null && !objArr[10].toString().equalsIgnoreCase(""))  ? df.format(objArr[10]) : "");
            obj.put("documentid", documentid != null ? documentid : "");
            if(linkingFlag && isConsignment && !isEdit ){ //For geting only unused Serial batch details in DO
                if(product.isIsSerialForProduct() && objArr[7] != null ){
                    KwlReturnObject result1 = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), objArr[7].toString());
                    NewBatchSerial newBatchSerial = (NewBatchSerial) result1.getEntityList().get(0);
                    if(newBatchSerial != null && newBatchSerial.getQuantitydue()==0){
    //                    batchQty--;
                        continue;
                    }
                }else if(product.isIsBatchForProduct() && ! product.isIsSerialForProduct() && objArr[0] != null){
                    KwlReturnObject result1 = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), objArr[0].toString());
                    NewProductBatch newProductBatch = (NewProductBatch) result1.getEntityList().get(0);
                    if(newProductBatch !=null && newProductBatch.getQuantitydue()==0){
                        continue;
                    }
                }
            }
            if (objArr[14] != null && !objArr[14].toString().equalsIgnoreCase("")) {
                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(SerialDocumentMapping.class.getName(), objArr[14].toString());
                SerialDocumentMapping sdm = (SerialDocumentMapping) result1.getEntityList().get(0);
                Map<String, Object> variableMap = new HashMap<String, Object>();
                SerialCustomData serialCustomData = (SerialCustomData) sdm.getSerialCustomData();
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                AccountingManager.setCustomColumnValues(serialCustomData, fieldMap, replaceFieldMap, variableMap);
                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                    String coldata = varEntry.getValue().toString();
                    String valueForReport = "";
                    if (customFieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                        try {
                            String[] valueData = coldata.split(",");
                            for (String value : valueData) {
                                FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), value);
                                if (fieldComboData != null) {
//                                    valueForReport += fieldComboData.getValue() + ",";
                                      valueForReport += value + ",";
                                }
                            }
                            if (valueForReport.length() > 1) {
                                valueForReport = valueForReport.substring(0, valueForReport.length() - 1);
                            }
                            obj.put(varEntry.getKey(), valueForReport);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                        } catch (Exception ex) {
                            obj.put(varEntry.getKey(), coldata);
                        }
                    } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                        DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateFromDB=null;
                        try {
                            dateFromDB = defaultDateFormat.parse(coldata);
                            coldata = df2.format(dateFromDB);

                        } catch (Exception e) {
                        }                        
                            obj.put(varEntry.getKey(), coldata);
                    } else {
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            obj.put(varEntry.getKey(), coldata);
                        }
                    }
                }
            }
            String serialNoId = objArr[7] != null ? (String) objArr[7] : "";
            int transType = Constants.Acc_ConsignmentSalesReturn_ModuleId;
            String docId="";
           KwlReturnObject reusablecountobj = accCommonTablesDAOObj.getSerialsReusableCount(product.getID(), objArr[8] != null ? (String) objArr[8] : "",product.getCompany().getCompanyID(),transType, false, docId, objArr[0] != null ? (String) objArr[0] :"");
            if (reusablecountobj.getEntityList() != null && !reusablecountobj.getEntityList().isEmpty()) {
                if (reusablecountobj.getEntityList().get(0) != null) {
                    double sumCount = Double.parseDouble(reusablecountobj.getEntityList().get(0).toString());
                    obj.put("reusablecount",  sumCount);
                } else {
                    obj.put("reusablecount", 0);
                }
            } else {
                obj.put("reusablecount", 0);
            }
            jSONArray.put(obj);
             batchQty--;

        }


        return jSONArray.toString();
    }
    
    public JSONArray getWOCombo(Map<String, Object> map) throws ServiceException {
        JSONArray jArr = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        KwlReturnObject result = workOrderDAOObj.getWorkOrderCombo(map);
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            try {
                jSONObject = new JSONObject();
                Object obj[] = (Object[]) itr.next();
                jSONObject.put("id", (String) obj[0]);
                jSONObject.put("billid", (String) obj[0]);
                jSONObject.put("name", (String) obj[1]);
                jSONObject.put("number", (String) obj[2]);
                jSONObject.put("billno", (String) obj[2]);
                jArr.put(jSONObject);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                throw ServiceException.FAILURE(ex.getMessage(), ex);
            }
        }
        return jArr;
    }
    public JSONObject changeStatustoInProcess(HashMap<String, Object> map) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String message1 = "",message2 = "",message3 = "";
        String msg = "";
        try {
            Locale requestcontextutilsobj = null;
            if (map.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) map.get("requestcontextutilsobj");
            }
            String userid = map.get("userid").toString();
            boolean isvalid = true;
            String companyid = map.get("companyId").toString();
            String woid = "";
            JSONArray woidJarr = null;
            if (map.get("woidArr") != null) {
                woidJarr = new JSONArray(map.get("woidArr").toString());
            }
            for (int index = 0; index < woidJarr.length(); index++) {
                
                woid = woidJarr.getString(index);
                map.put("woid", woid);
                JSONObject retJobj = checkComponentAvailability(map);   // calling component avialability check function
                /**
                 * If there is even one single sub assembly product that can be
                 * produced then WO can be started.
                 */
                if (!retJobj.optBoolean(Constants.isValidToStart)) {
                    Iterator ite = retJobj.keys();
                    while (ite.hasNext()) {                            // checking if all products are fullfilled 
                        String key = ite.next().toString();
                        boolean isFullfilled = retJobj.getBoolean(key);
                        if (!isFullfilled) {
                            isvalid = false;
                            msg += "&emsp; " + key;
                        }
                    }
                }
                KwlReturnObject result = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), woid);
                WorkOrder woObj = (WorkOrder) result.getEntityList().get(0);

                if (isvalid) {             // generation of message and calling wo status change function
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("id", woid);
                    params.put("defaultStatusId", Constants.defaultWOstatus_INPROCESS);
                    params.put("companyId", companyid);
                    JSONObject temJobj = changeWOStatus(params);
                    
                    /*
                     * Code to update WO Status in PM
                     */ 
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("companyid", woObj.getCompany().getCompanyID());
                    userData.put("userid", userid);
                    userData.put("projectname", woObj.getWorkOrderName());// Sending Work order name as Project Name
                    userData.put("isNewProject", "false");// As Updating the Existing Project
                    userData.put("isMasterProject", "false");// False for Work Order
                    userData.put("projectId", woObj.getProjectId());
                    userData.put("projectcode",woObj.getWorkOrderID());
                    userData.put("MRPWOStatus", Constants.defaultWOstatus_INPROCESS);
                    accRoutingManagementService.createOrUpdateProjectRest(userData);
                    
                    /**
                     * Following code is used to save Audit Trail entry along with descriptive information about user action.
                     */
                    String auditMsg = " User " + map.get("userfullname") + " has started Work Order  <b>" + woObj.getWorkOrderID() + "</b>";
                    auditTrailObj.insertAuditLog(AuditAction.WORK_ORDER_STARTED, auditMsg, map, woObj.getWorkOrderID());
                    message1 +=  "<b>" +woObj.getWorkOrderID() + "</b>&emsp;" ;
                } else {
                    message2 += "<br/><b>" +woObj.getWorkOrderID() + "</b> " + msg;
                }
            }
            if (StringUtil.isNullOrEmpty(message1)) {
                message3  = messageSource.getMessage("acc.mrp.workorder.massstatuschange.message2", null, requestcontextutilsobj) + message2;
            } else if (StringUtil.isNullOrEmpty(message2)) {
                message3 = messageSource.getMessage("acc.mrp.workorder.massstatuschange.message1", null, requestcontextutilsobj);
            } else {
                message3 = messageSource.getMessage("acc.mrp.workorder.massstatuschange.message3", null, requestcontextutilsobj)+ message1 +messageSource.getMessage("acc.mrp.workorder.massstatuschange.message4", null, requestcontextutilsobj)+ message2 +messageSource.getMessage("acc.mrp.workorder.massstatuschange.message1", null, requestcontextutilsobj);
            }
            
            jobj.put("msg", message3);
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return  jobj;
    }
    
    @Override
    public JSONObject changeWOStatus(HashMap<String, Object> map) throws ServiceException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("WOStatus_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONObject paramObj = new JSONObject();
            paramObj.put("isEdit", true);
            String woid = map.get("id").toString();
            paramObj.put("id", woid);
            kmsg = workOrderDAOObj.getWOStatusidFromDefaultID(map);         // fetching company's In process id based on default WO In process id
            if (kmsg.getEntityList().size() > 0) {
                MasterItem miObj = (MasterItem) kmsg.getEntityList().get(0);
                paramObj.put("workorderstatus", miObj.getID());
            }
            if((map.containsKey("closewoje") && map.get("closewoje")!=null) && (map.containsKey("assemblyJedid") && map.get("assemblyJedid")!=null)){
                String jeid=map.get("closewoje").toString();
                String assemblyJedid=map.get("assemblyJedid").toString();
                paramObj.put("closewoje", jeid);
                paramObj.put("assemblyJedid",assemblyJedid);
            }
            kmsg = workOrderDAOObj.saveWorkOrder(paramObj); // saving WO
            txnManager.commit(status);
            jobj.put("isSuccess", true);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
     
    public JSONObject checkComponentAvailability(HashMap<String, Object> map) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            String productid ="";
            if (map.get("productid") != null ) {
                productid = map.get("productid").toString();
            }
            String woid = map.get("woid").toString();
            
            int flag =Integer.parseInt(map.get("flag").toString());  // if flag = 1 then req quantity to be taken from wo
            double reqQty = 0;
            String bomid = "";
            WorkOrderComponentDetails WOdetailObj = null; // to fetch wodetail on productid and wo id
            WorkOrder woObj = null;
            Product product = null;
            
            
            
            woObj = (WorkOrder) accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), woid).getEntityList().get(0);
            
            if (flag == 1) {  // excuted when product is, what saved  in Work order
                 reqQty = woObj.getQuantity();
                 bomid = woObj.getBomid() != null ?woObj.getBomid().getID() : "";
                 productid = woObj.getProductID().getID();
            }  else {      // excuted when product is from BOM on recursive call
                reqQty = Double.parseDouble(map.get("reqQty").toString());
                bomid = map.get("bomid").toString();
            }
            
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("productid", productid);
            if (!StringUtil.isNullOrEmpty(bomid)) {
                requestParams.put("bomdetailid", bomid);
            }
            KwlReturnObject result = accProductDaoObj.getAssemblyItems(requestParams);
            List list = result.getEntityList();
            Iterator ite = list.iterator();
            boolean isAssembly=false;
            boolean isInventory=false;
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                ProductAssembly passembly = (ProductAssembly) row[0];
                Product subProduct = passembly.getSubproducts();
                String productname=passembly.getProduct().getProductName();
                if (subProduct.getProducttype().getID().equalsIgnoreCase(Producttype.ASSEMBLY)) { // If Product is of type Inventory Assembly
                    isAssembly=true;
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("productid", subProduct.getID());
                    params.put("woid", woid);
                    params.put("parentproductid", productid);
                    KwlReturnObject detailResult = workOrderDAOObj.getWODetailfromProductandWO(params);
                    if (detailResult.getEntityList().size() > 0) {
                        // checking quantities
                        WOdetailObj = (WorkOrderComponentDetails) detailResult.getEntityList().get(0);
                        double percent = WOdetailObj.getMinpercent();
                        double blockQty = WOdetailObj.getBlockQuantity();
                        double requiredQty = passembly.getQuantity() * reqQty;
                        double actualreqqty = authHandler.roundQuantity(((percent / 100) * requiredQty), passembly.getProduct().getCompany().getCompanyID());//ERP-36904
                        if (blockQty < actualreqqty) {
                            double stilReqQty = actualreqqty - blockQty;
                            HashMap<String, Object> params2 = new HashMap<>();
                            params2.put("productid", subProduct.getID());
                            params2.put("woid", woid);
                            params2.put("flag", 2);
                            params2.put("bomid", passembly.getSubbom() != null ? passembly.getSubbom().getID() : "");
                            params2.put("reqQty", stilReqQty);
                            JSONObject tempJobj = checkComponentAvailability(params2);  // recursive call for multi BOM 
                            Iterator ite1 = tempJobj.keys();
                            boolean isvalid = true;
                            while (ite1.hasNext()) {
                                String key = ite1.next().toString();
                                boolean isFullfilled = tempJobj.getBoolean(key);
                                    if (!isFullfilled) {
                                        isvalid = false;
                                    }
                                jobj.put(key, tempJobj.getString(key));
                            }
                            if (isvalid) {
                                jobj.put(subProduct.getProductid(), true);  // If the quantities are fullfilled
                                /**
                                 * If there is a single sub assembly product
                                 * that can be produced then WO can be started.
                                 */
                                jobj.put(Constants.isValidToStart, true);
                            } else {
                                jobj.put(subProduct.getProductid(), false); // If the quantities are not fullfilled
                            }

                        } else {
                            jobj.put(subProduct.getProductid(), true);  // If the quantities are fullfilled
                        }
                    }
                } else {                                               // If Product is of another type other than Inventory Assembly
                    isInventory=true;                     
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("productid", subProduct.getID());
                    params.put("woid", woid);
                    params.put("parentproductid", productid);
                    KwlReturnObject detailResult = workOrderDAOObj.getWODetailfromProductandWO(params);
                    if (detailResult.getEntityList().size() > 0) {
                        WOdetailObj = (WorkOrderComponentDetails) detailResult.getEntityList().get(0);
                        if(!WOdetailObj.getProduct().getProducttype().getID().equals(Producttype.SERVICE)){
                            double percent = WOdetailObj.getMinpercent();
                            double blockQty = authHandler.roundQuantity((WOdetailObj.getBlockQuantity()), passembly.getProduct().getCompany().getCompanyID());
                           // double requiredQty = passembly.getQuantity() * reqQty;
                            double requiredQty = authHandler.roundQuantity((WOdetailObj.getRequiredQuantity()), passembly.getProduct().getCompany().getCompanyID());
                            double actualreqqty = authHandler.roundQuantity(((percent / 100) * requiredQty), passembly.getProduct().getCompany().getCompanyID());//ERP-36904                                                                              
                            if (blockQty < actualreqqty) {
                                jobj.put(subProduct.getProductid(), false); // If the quantities are not fullfilled
                            } else {
                                jobj.put(subProduct.getProductid(), true);  // If the quantities are fullfilled
                            }
                        }
                    }
                }
            }                           
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }        
        return jobj;
    }
    @Override
    public JSONObject syncBOmWithChecklistToPM(Map<String, Object> requestParams) throws ServiceException {
        JSONObject resObj = new JSONObject();
        String resourceId = "";
        String projectId = "";
        String bomid = "";
        try {
            JSONArray dataJArr = new JSONArray();
            String productid = "";
            String inspectiontemplateid="";
            String inspectionformdetail="";
            String inspectionformid="";
            String inspectionDate="";
            String modelName="";
            String customerName="";
            SimpleDateFormat frmtr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            
            JSONArray bomWiseProductsArr=(JSONArray) requestParams.get("bomwiseproducts");
            
            JSONObject rootProd = new JSONObject();
            rootProd.put("productid", (String)requestParams.get("productid"));
            rootProd.put("isFinishedProduct", true);
            String salesorderid="";
            int seqNo=0;
            
            /*
             * get inspection form details which are stored at SO line level
             */
            if (requestParams.containsKey("salesorderid")  && !StringUtil.isNullOrEmpty((String) requestParams.get("salesorderid"))) {
                salesorderid = (String) requestParams.get("salesorderid");
                KwlReturnObject SOObj = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), salesorderid);
                SalesOrder soObj = (SalesOrder) SOObj.getEntityList().get(0);

                if (soObj != null) {
                    Set<SalesOrderDetail> salesorderdetails = soObj.getRows();
                    for (SalesOrderDetail salesorderdetail : salesorderdetails) {
                        seqNo=0;
                        if (salesorderdetail.getProduct().getID().equals((String) requestParams.get("productid"))) {
                            inspectiontemplateid = salesorderdetail.getInspectionTemplate() != null ? salesorderdetail.getInspectionTemplate().getId() : "";
                             InspectionForm inspectionForm = salesorderdetail.getInspectionForm();
                            if (inspectionForm != null) {
                                JSONArray detailsJArr = new JSONArray();
                                inspectionformid=inspectionForm.getId();
                                inspectionDate = frmtr.format(inspectionForm.getInspectionDate());
                                modelName = inspectionForm.getModelName();
                                customerName = inspectionForm.getCustomerName();
                                                                                           
                                Set<InspectionFormDetails> insFormDetailSet = inspectionForm.getRows();
                                
                                for (InspectionFormDetails insFormDetail : insFormDetailSet) {
                                    JSONObject insFormDetailJobj = new JSONObject();
                                    insFormDetailJobj.put("templateId", (salesorderdetail.getInspectionTemplate() != null ? salesorderdetail.getInspectionTemplate().getId() : ""));
                                    insFormDetailJobj.put("areaId", insFormDetail.getInspectionArea() != null ? insFormDetail.getInspectionArea().getId():"");
                                    insFormDetailJobj.put("areaName", insFormDetail.getInspectionAreaValue() !=null ? insFormDetail.getInspectionAreaValue():"");
                                    insFormDetailJobj.put("status", insFormDetail.getInspectionStatus() !=null ? insFormDetail.getInspectionStatus():"");
                                    insFormDetailJobj.put("faults", insFormDetail.getFaults() !=null ? insFormDetail.getFaults():"");
                                    insFormDetailJobj.put("passingValue", insFormDetail.getPassingValue() != null ? insFormDetail.getPassingValue():"");
                                    insFormDetailJobj.put("seqNo", seqNo);
                                    seqNo++;
                                    detailsJArr.put(insFormDetailJobj);
                                }
                                inspectionformdetail= detailsJArr.toString();
                            }
                        }
                    }
                }
            }
            rootProd.put("inspectiontemplateid", inspectiontemplateid);
            rootProd.put("inspectionformid", inspectionformid);
            rootProd.put("inspectionDate", inspectionDate);        
            rootProd.put("modelName",modelName);
            rootProd.put("customerName",customerName);
            rootProd.put("inspectionAreaDetails", inspectionformdetail);
            
            
            bomWiseProductsArr.put(rootProd);
            //**********Get CheckList data********************
            
           int groupCount = CreateDataArrOfCehckListDetails(dataJArr,bomWiseProductsArr,requestParams);

            
             //**********Get CheckList data********************
            String accRestURL = URLUtil.buildRestURL("pmURL");
            JSONObject userData = new JSONObject();
            String userId = "";
            String companyId = "";
            if (requestParams.containsKey("companyId")) {
                companyId = (String) requestParams.get("companyId");
            }
            if (requestParams.containsKey("userId")) {
                userId = (String) requestParams.get("userId");
            }
            if (requestParams.containsKey("projectId")) {
                projectId = (String) requestParams.get("projectId");
            }
//        
            userData.put("iscommit", true);
            userData.put("userid", userId);
            userData.put("companyid", companyId);
            userData.put("projectid", projectId);
            userData.put("groupcount", groupCount);

            userData.put("woproducts", dataJArr);
            String endpoint = accRestURL + "transaction/projectchecklist";
            resObj = apiCallHandlerService.restPostMethod(endpoint, userData.toString());
        } catch (com.krawler.utils.json.base.JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return resObj;
    }
    
    public JSONObject syncWorkOrderDateToPM(Map<String, Object> requestParam)throws ServiceException{
        JSONObject syncwodateresobj = new JSONObject();
        JSONObject syncparameters = new JSONObject();
        try {
            if ((requestParam.containsKey(Constants.companyKey) && !StringUtil.isNullOrEmpty(requestParam.get(Constants.companyKey).toString()))
                    && (requestParam.containsKey(Constants.useridKey) && !StringUtil.isNullOrEmpty(requestParam.get(Constants.useridKey).toString()))
                    && (requestParam.containsKey(Constants.projectid) && !StringUtil.isNullOrEmpty(requestParam.get(Constants.projectid).toString()))
                    && (requestParam.containsKey(Constants.workorderdate) && !StringUtil.isNullOrEmpty(requestParam.get(Constants.workorderdate).toString()))
                    && (requestParam.containsKey(Constants.isShiftProjectStartDate) && !StringUtil.isNullOrEmpty(requestParam.get(Constants.isShiftProjectStartDate).toString()))) {
                              
                syncparameters.put(Constants.companyKey,requestParam.get(Constants.companyKey).toString());
                syncparameters.put(Constants.useridKey,requestParam.get(Constants.useridKey).toString());
                syncparameters.put(Constants.projectid,requestParam.get(Constants.projectid).toString());
                syncparameters.put(Constants.workorderdate,requestParam.get(Constants.workorderdate).toString());
                syncparameters.put(Constants.isShiftProjectStartDate,requestParam.get(Constants.isShiftProjectStartDate).toString());
                
                String accRestURL= URLUtil.buildRestURL(Constants.pmURL);
                String endpoint = accRestURL + "transaction/syncworkorderdate";
                syncwodateresobj  = apiCallHandlerService.restPostMethod(endpoint,syncparameters.toString());                                
            }
        }catch (Exception ex)  {
            throw ServiceException.FAILURE(ex.getMessage(), ex);                     
        }                        
        return syncwodateresobj;
    }
    public int CreateDataArrOfCehckListDetails(JSONArray dataJArr, JSONArray bomWiseProductsArr, Map<String, Object> requestParams) throws ServiceException {
        int groupCount=0;
        try {

            String productid = "";
            String inspectiontemplateid="";
            String inspectionformid="";
            String inspectionformdetail="";
            String inspectionDate="";
            String modelName="";
            String customerName="";
                    
            KwlReturnObject kwl = null;
            for (int i = 0; i < bomWiseProductsArr.length(); i++) {
                JSONObject jObj = bomWiseProductsArr.getJSONObject(i);
                productid = jObj.getString("productid");
                requestParams.put("productid", productid);
                requestParams.put("isGroup", true);
                requestParams.put("groupid", null);
                JSONObject productJobj = new JSONObject();
                
                inspectiontemplateid=jObj.optString("inspectiontemplateid", "");
                inspectionformid=jObj.optString("inspectionformid", "");
                inspectionDate=jObj.optString("inspectionDate", "");
                modelName = jObj.optString("modelName","");
                customerName = jObj.optString("customerName","");
                inspectionformdetail=jObj.optString("inspectionAreaDetails", "");
                                
                kwl = workOrderDAOObj.getCheckListDetails(requestParams);
                List<Object[]> dataList = kwl.getEntityList();

                JSONObject groupObj = null;
                JSONArray groupJArr = new JSONArray();
                for (Object[] obj : dataList) {
//                    QualityControl qcg = (QualityControl) obj;
                    groupCount++;
                    groupObj = new JSONObject();
                    groupObj.put("qcgroupid", obj[0]!=null ? obj[0] : "");
                    groupObj.put("qcgroup", obj[1]!=null ? obj[1] : "");

                    requestParams.put("groupid", obj[0]!=null ? obj[0] : "");
                    requestParams.put("isGroup", false);
                    JSONArray parameterJarr = new JSONArray();
                    kwl = workOrderDAOObj.getCheckListDetails(requestParams);
                    JSONObject paramObj = new JSONObject();
                    List paramList = kwl.getEntityList();
                    for (Object paraObj : paramList) {
                        QualityControl qcp = (QualityControl) paraObj;
                        paramObj = new JSONObject();
                        paramObj.put("qcparamid", qcp.getQcparameter()!=null ? qcp.getQcparameter().getID() : "");
                        paramObj.put("qcparam", qcp.getQcparameter()!=null ? qcp.getQcparameter().getValue() : "");
                        paramObj.put("qcvalue", qcp.getQcvalue());
                        paramObj.put("qcdescription", qcp.getQcdescription());
                        parameterJarr.put(paramObj);
                    }
                    groupObj.put("qcparameter", parameterJarr);
                    groupJArr.put(groupObj);
                }

                productJobj.put("productid", productid);
                productJobj.put("QC", groupJArr);
                productJobj.put("inspectiontemplateid", inspectiontemplateid);
                productJobj.put("inspectionformid", inspectionformid);
                productJobj.put("inspectionDate", inspectionDate);
                productJobj.put("modelName",modelName);
                productJobj.put("customerName",customerName);
                productJobj.put("inspectionAreaDetails", inspectionformdetail);
                
                
                if (jObj.optBoolean("isFinishedProduct", false)) {
                    
                    productJobj.put("isFinishedProduct", true);
                    
                }
                dataJArr.put(productJobj);
            }
//           
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return groupCount;
    }

    @Override
    public JSONObject getRejectedItemListReport(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        JSONObject commData = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        try {

            Boolean isExport = false;
            Locale requestcontextutilsobj = null;
            if (requestParams.containsKey("isExport") && requestParams.get("isExport") != null) {
                isExport = (Boolean) requestParams.get("isExport");
            }
            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }
            /*
             Write method to dao layer to fetch rejected item list
             */ 
             dataJArr=getRejectedItemsDetails(requestParams);
            //*******************Record****************************
            JSONObject rec = new JSONObject();
            rec.put("name", "productid");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put("name", "productcode");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put("name", "productname");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put("name", "actionid");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put("name", "actionname");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put("name", "workordername");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put("name", "workordercode");
            jarrRecords.put(rec);
            rec = new JSONObject(); //ERP-35176 : Show customer in Rejected WO Register
            rec.put("name", "customername");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put("name", "workcenter");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put("name", "rejectedqty");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put("name", "wastedqty");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put("name", "recycledqty");
            jarrRecords.put(rec);

            //*******************Record****************************
            //*****************ColumnModel*************************
               jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.entry.workordername", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "workordername");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.entry.workorderid", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "workordercode");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.saleByItem.gridProduct", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "productname");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
//         
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.product.gridProductID", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "productcode");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();  //ERP-35176 : Customer Name Column.
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.header3", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "customername");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);


            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.workcenter", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "workcenter");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.rejecteditemlistreport.gridheader.rejectedquantity", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "rejectedqty");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.rejecteditemlistreport.gridheader.wasteQuantity", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "wastedqty");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.rejecteditemlistreport.gridheader.recycleQuantity", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "recycledqty");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

//*****************ColumnModel******************************
            commData.put("success", true);
            commData.put("coldata", dataJArr);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

            jobj.put("valid", true);
            if (isExport) {
                jobj.put("data", dataJArr);
            } else {
                jobj.put("data", commData);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccWorkCentreServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
      public JSONArray getRejectedItemsDetails(Map<String, Object> requestParams) throws ServiceException {
        JSONArray dataJArr = new JSONArray();
        try {
            requestParams.put("isFromRejectedItemListReport", true);
            KwlReturnObject result = workOrderDAOObj.getWorkOrderComponentDetails(requestParams);

            List dataList = result.getEntityList();
            JSONObject tmpObj = new JSONObject();
            for (Object obj : dataList) {
                WorkOrderComponentDetails componentDetails = (WorkOrderComponentDetails) obj;
                tmpObj = new JSONObject();
                Product product = componentDetails.getProduct();
                String prodcutid = product.getID();
                tmpObj.put("productcode", componentDetails.getProduct() != null ? componentDetails.getProduct().getProductid() : "");
                tmpObj.put("productname", componentDetails.getProduct() != null ? componentDetails.getProduct().getProductName() : "");
                tmpObj.put("workordername", componentDetails.getWorkOrder() != null ? componentDetails.getWorkOrder().getWorkOrderName() : "");
                tmpObj.put("workordercode", componentDetails.getWorkOrder() != null ? componentDetails.getWorkOrder().getWorkOrderID() : "");
                tmpObj.put("customername", (componentDetails.getWorkOrder() != null && componentDetails.getWorkOrder().getCustomer()!=null) ? componentDetails.getWorkOrder().getCustomer().getName(): "");//ERP-35176 : Customer Name
                
                
                tmpObj.put("workcenter", "");
                if (componentDetails.getWorkOrder() != null && componentDetails.getWorkOrder().getWorkcentermapping() != null) {
                    Set<WorkOrderWorkCenterMapping> wcm=(Set<WorkOrderWorkCenterMapping>)componentDetails.getWorkOrder().getWorkcentermapping();
                    for(WorkOrderWorkCenterMapping wcObj: wcm){
                        WorkCentre wc=(WorkCentre) wcObj.getWorkcentreid();
                        tmpObj.put("workcenter",wc.getName());
                    }
                } 
                tmpObj.put("rejectedqty", componentDetails.getRejectedQuantity());
                tmpObj.put("wastedqty", componentDetails.getWastedQuantity());
                tmpObj.put("recycledqty", componentDetails.getRecycledQuantity());
                dataJArr.put(tmpObj);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccWorkCentreServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dataJArr;
    }

    @Override
    public JSONObject exportRejectedItemsList(Map<String, Object> requestParams) throws ServiceException {
          
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        HttpServletRequest request=null;
        HttpServletResponse response=null;
        try {
            
            dataJArr=getRejectedItemsDetails(requestParams);
            jobj.put("data", dataJArr);
            request=(HttpServletRequest)requestParams.get("request");
            response=(HttpServletResponse)requestParams.get("response");
            exportDaoObj.processRequest(request, response, jobj);
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    
    @Override
    public void lockWorkOrderDeatilsBatch(String batchJSON, String productId, JSONObject paramJobj, String documentId) throws com.krawler.utils.json.base.JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {

        JSONArray jArr = new JSONArray(batchJSON);
        double ActbatchQty = 1;
        double batchQty = 0;

        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        boolean isEdit=false;
        int serialsequence = 1 , batchsequence = 1; // for user selected sequence of batch and serial while creating WO.
        DateFormat df = authHandler.getDateOnlyFormat();
        String companyid = paramJobj.getString(Constants.companyKey);
        String userid = paramJobj.getString(Constants.useridKey);
        isEdit=(paramJobj.has("isEdit"))?paramJobj.getBoolean("isEdit"):false;
        if (!StringUtil.isNullOrEmpty(productId)) {
            KwlReturnObject prodresult = accountingHandlerDAOobj.getObject(Product.class.getName(), productId);
            Product product = (Product) prodresult.getEntityList().get(0);
            isLocationForProduct = product.isIslocationforproduct();
            isWarehouseForProduct = product.isIswarehouseforproduct();
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
            isRowForProduct = product.isIsrowforproduct();
            isRackForProduct = product.isIsrackforproduct();
            isBinForProduct = product.isIsbinforproduct();
        }

        //Save Batch detail for both  option for serial no and batch also as if batch option is off then also we are generating batch in backend
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
            if (jSONObject.has("quantity") && !jSONObject.getString("quantity").equals("undefined") && !jSONObject.getString("quantity").isEmpty()) {
                ActbatchQty = jSONObject.getDouble("quantity");
            }
            if (batchQty == 0) {
                batchQty = jSONObject.getDouble("quantity");
            }
            if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct || isRowForProduct || isRackForProduct || isBinForProduct) && (batchQty == ActbatchQty)) {
                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", jSONObject.getString("quantity"));

                documentMap.put("documentid", documentId);
                documentMap.put("transactiontype", "20");//change 100 to 20 for work order moduleid
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                    documentMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                    documentMap.put("expdate", df.parse(jSONObject.getString("expdate")));
                }
                documentMap.put("batchmapid", jSONObject.getString("purchasebatchid"));
                //for checking Consignment request approval rule
                requestParams.clear();
                requestParams.put("requestorid", userid);
                requestParams.put(Constants.companyKey, companyid);
                if (!StringUtil.isNullOrEmpty(jSONObject.getString("warehouse"))) {
                    String warehouse = jSONObject.getString("warehouse");
                    requestParams.put("warehouse", warehouse);
                }
                if (!StringUtil.isNullOrEmpty(jSONObject.getString("location"))) {
                    String location = jSONObject.getString("location");
                    requestParams.put("location", location);
                }
                if (!isBatchForProduct && !isSerialForProduct) {
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                    filter_names.add("company.companyID");
                    filter_params.add(paramJobj.getString(Constants.companyKey));

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

                    filter_names.add("product");
                    filter_params.add(productId);

                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    filterRequestParams.put("order_by", order_by);
                    filterRequestParams.put("order_type", order_type);
                    KwlReturnObject result = accMasterItemsDAOObj.getNewBatches(filterRequestParams, false, false);
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
                                    batchUpdateQtyMap.put("lockquantity", String.valueOf(dueQty));
                                    quantityToDue = quantityToDue - dueQty;

                                } else {
//                                    if (isEdit) {
//                                        batchUpdateQtyMap.put("qty", String.valueOf((quantityToDue)));//not need to pass this
//                                    }
                                    batchUpdateQtyMap.put("lockquantity", String.valueOf(quantityToDue));
                                    quantityToDue = quantityToDue - quantityToDue;

                                }
                                documentMap.put("batchmapid", newProductBatch.getId());
                                accCommonTablesDAOObj.saveBatchAmountDue(batchUpdateQtyMap);

                            }
                        }

                    }
                } else {

                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                    batchUpdateQtyMap.put("lockquantity", String.valueOf((Double.parseDouble(jSONObject.getString("quantity")))));
                    batchUpdateQtyMap.put("id", jSONObject.getString("purchasebatchid"));
                    accCommonTablesDAOObj.saveBatchAmountDue(batchUpdateQtyMap);

                }
                /**
                 * added selected sequence for batch selected by user while
                 * creating WO.
                 */
                documentMap.put("batchsequence", batchsequence++);
                accCommonTablesDAOObj.saveBatchDocumentMapping(documentMap); //this line uncomment to store batchserial data into locationbatchdocumentmapping table
            }
            batchQty--;//need to decrease for product having serial number 
            if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 
                HashMap<String, Object> documentMap = new HashMap<String, Object>();

                documentMap.put("quantity", 1);
                documentMap.put("lockquantity", 1);
                documentMap.put("serialmapid", jSONObject.getString("purchaseserialid"));
                documentMap.put("documentid", documentId);
                documentMap.put("transactiontype", "27");//This is GRN Type Tranction  
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    documentMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    documentMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }
                
                /**
                 * added selected sequence for serial selected by user while
                 * creating WO.
                 */
                documentMap.put("serialsequence", serialsequence++);
                KwlReturnObject krObj = accCommonTablesDAOObj.saveSerialDocumentMapping(documentMap);

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
                            accCommonTablesDAOObj.updateserialcustomdata(DOMap);
                        }
                    }
                }
                HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                serialUpdateQtyMap.put("lockquantity", "1");
                serialUpdateQtyMap.put("id", jSONObject.getString("purchaseserialid"));
                accCommonTablesDAOObj.saveSerialAmountDue(serialUpdateQtyMap);

            } else {
                batchQty = 0; //for without serial
            }
        }

    }

    @Override
    public JSONObject getQualityControlParameters(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        try {
            dataJArr = getQualityControParameterDetailsFromPM(requestParams);
            jobj.put("success", true);
            jobj.put("valid", true);
            jobj.put("count", dataJArr.length());
            JSONObject dataObj = new JSONObject();
            dataObj.put("data", dataJArr);
            jobj.put("data", dataObj);
//           
        } catch (Exception ex) {
            Logger.getLogger(AccWorkCentreServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    public JSONArray getQualityControParameterDetailsFromPM(Map<String, Object> requestParams) throws ServiceException {
        JSONObject resObj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        try {
            
            String accRestURL = URLUtil.buildRestURL("pmURL");
            JSONObject userData = new JSONObject();
            String userId = "",companyId = "",projectid="",statustype="",ss="";
            if (requestParams.containsKey("companyid")) {
                companyId = (String) requestParams.get("companyid");
            }
            if (requestParams.containsKey("userid")) {
                userId = (String) requestParams.get("userid");
            }
            if (requestParams.containsKey("projectid")) {
                projectid = (String) requestParams.get("projectid");
            }
            if (requestParams.containsKey("statustype")) {
                statustype = (String) requestParams.get("statustype");
            }
            if (requestParams.containsKey(Constants.ss)) {
                ss = (String) requestParams.get(Constants.ss);
            }
            
            userData.put("userid", userId);
            userData.put("companyid", companyId);
            userData.put("projectid", projectid);
            userData.put("statustype", statustype);
            userData.put(Constants.ss, ss);

            String endpoint = accRestURL + "transaction/getQualityControlParameters";
            resObj = apiCallHandlerService.restPostMethod(endpoint, userData.toString());
            dataJArr= resObj.getJSONArray("data");
            System.out.println("sp");
        } catch (com.krawler.utils.json.base.JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return dataJArr;
    }

    @Override
    public JSONObject exportMRPQCReportList(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        try {

            dataJArr = getQualityControParameterDetailsFromPM(requestParams);
            jobj.put("data", dataJArr);
            request = (HttpServletRequest) requestParams.get("request");
            response = (HttpServletResponse) requestParams.get("response");
            exportDaoObj.processRequest(request, response, jobj);

        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    @Override
    public JSONObject getWorkOrderShortFallReport(Map<String, Object> requestParams) throws ServiceException {
          JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        JSONObject dataJobj = new JSONObject();
        JSONObject commData = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        String start = "", limit = "";
        try {
            if (requestParams.containsKey("start") && !StringUtil.isNullObject(requestParams.get("start")) ) {
                start = requestParams.get("start").toString();
            }
            if (requestParams.containsKey("limit") && !StringUtil.isNullObject(requestParams.get("limit"))) {
                limit = requestParams.get("limit").toString();
            }
            Boolean isExport = false;
            Boolean isForExpander = false;
            Locale requestcontextutilsobj = null;
            if (requestParams.containsKey("isExport") && requestParams.get("isExport") != null) {
                isExport = (Boolean) requestParams.get("isExport");
            }
            if (requestParams.containsKey("isForExpander") && requestParams.get("isForExpander") != null) {
                isForExpander = Boolean.parseBoolean(requestParams.get("isForExpander").toString());
            }
            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }
            /*
             Write method to dao layer to fetch rejected item list
             */ 
             dataJobj= getShortFallProducts(requestParams);
             dataJArr = dataJobj.getJSONArray("data");
           
            //*******************Record****************************
            JSONObject rec = new JSONObject();
            rec.put("name", "id");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put("name", "productId");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put("name", "productName");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put("name", "workOrders");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put("name", "shortfallQuantity");
            jarrRecords.put(rec);
            
            //*******************Record****************************
            //*****************ColumnModel*************************
             jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.product.gridProductID", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "productId");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
             jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.header6", null, requestcontextutilsobj)); // Product Name
            jobjTemp.put("dataIndex", "productName");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
             jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.wo.ca.grid.header7", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "shortfallQuantity");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
             jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.workorder", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "workOrders");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            

//*****************ColumnModel******************************
            JSONArray pagedJson = new JSONArray();
            pagedJson = dataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            
            
            commData.put("success", true);
            commData.put("totalCount", dataJArr.length());
            commData.put("coldata", pagedJson);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);
            if (!isForExpander) {
                jobj.put("valid", true);
            }
            if (isExport || isForExpander) {
                jobj.put("data", dataJArr);
            } else {
                jobj.put("data", commData);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccWorkOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    public JSONObject getShortFallProducts(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        KwlReturnObject result = null;
        boolean isForExpander=false;
        String productid="";
        String ss="";
        
        try {
            Product product = null;
            
            HashMap<String,Object> paramsMap =  new HashMap<>();
            String companyid = "";
            if (requestParams.containsKey("companyid")) {
                companyid = requestParams.get("companyid").toString();
                paramsMap.put("companyid", companyid);
            }
            if (requestParams.containsKey("productid")) {
                productid = requestParams.get("productid").toString();
                if (!StringUtil.isNullOrEmpty(productid)) {
                    paramsMap.put("productid", productid);
                }
            }
            if (requestParams.containsKey("isForExpander")) {
                isForExpander = Boolean.parseBoolean(requestParams.get("isForExpander").toString());
            }
            /**
             * Quick Search on Product ID, Product Name.
             */
            if(requestParams.containsKey("ss")){
                ss = requestParams.get("ss")!=null?requestParams.get("ss").toString():"";
                if(!StringUtil.isNullOrEmpty(ss)){
                    paramsMap.put("ss",ss);
                }                
            }
            
            result = workOrderDAOObj.getWODetailfromProductandWO(paramsMap);
            
            List<WorkOrderComponentDetails> resultList = result.getEntityList();
            Set<String> productIdSet = new HashSet<>();
            
            // Extracting all distinct product ids in a set.
            for( WorkOrderComponentDetails woComDet : resultList ) {
                String productId = woComDet.getProduct() != null ? woComDet.getProduct().getID(): "";
                if (!productIdSet.contains(productId)) {
                    productIdSet.add(productId);
                }
            }
            HashMap<String,Object> woidQuantityMap =  null;
            HashMap<String,Object> woidWONameMap =  null;
            
            if (isForExpander) {
                woidQuantityMap = new HashMap<>();
                woidWONameMap = new HashMap<>();
            }
            
            for (String productId : productIdSet) {
                JSONObject tempObj = new JSONObject();
                double shortfallQuantity = 0.0;
                String WOIdStr = "";
                product = (Product) accountingHandlerDAOobj.getObject(Product.class.getName(), productId).getEntityList().get(0);
                for (WorkOrderComponentDetails woComDet : resultList) {
                    String woProductId = woComDet.getProduct() != null ? woComDet.getProduct().getID() : "";
                    if (woProductId.equalsIgnoreCase(productId)) {
                        JSONObject paramJobj = new JSONObject();
                        paramJobj.put("productId", productId);
                        paramJobj.put("wocdid", woComDet.getID());
                        JSONObject WoCdProdQuantityJobj = getWOCompDetProdQuantities(paramJobj);
                        if (WoCdProdQuantityJobj.getDouble("shortfallQuantity") > 0) {
                            shortfallQuantity += WoCdProdQuantityJobj.getDouble("shortfallQuantity");
                            String woid =  woComDet.getWorkOrder() != null ? woComDet.getWorkOrder().getWorkOrderID() : "";
                            String woName =  woComDet.getWorkOrder() != null ? woComDet.getWorkOrder().getWorkOrderName() : "";
                            if (!WOIdStr.contains(woid)) {
                                WOIdStr += woid + "<br>";
                            }
                            if (isForExpander){
                                if (woidQuantityMap.containsKey(woid)) {
                                    woidQuantityMap.put(woid, authHandler.roundQuantity(Double.parseDouble(woidQuantityMap.get(woid).toString()) + WoCdProdQuantityJobj.getDouble("shortfallQuantity"), companyid));
                                } else {
                                    woidQuantityMap.put(woid, authHandler.roundQuantity(WoCdProdQuantityJobj.getDouble("shortfallQuantity"), companyid));
                                }
                                woidWONameMap.put(woid, woName);
                            }
                        }
                    }
                }
                if (!isForExpander) {
                    if (shortfallQuantity > 0) {
                        tempObj.put("id", product.getID());
                        tempObj.put("productId", product.getProductid());
                        tempObj.put("productName", product.getProductName());
                        tempObj.put("workOrders", WOIdStr);
                        tempObj.put("shortfallQuantity", authHandler.roundQuantity(shortfallQuantity, companyid));
                        jArr.put(tempObj);
                    }
                }
            }
            if (isForExpander) {
                for (String woid:woidQuantityMap.keySet()) {
                    JSONObject tempJobj = new JSONObject();
                    tempJobj.put("id", productid);
                    tempJobj.put("woid", woid);
                    tempJobj.put("woname", woidWONameMap.get(woid));
                    tempJobj.put("quantity", woidQuantityMap.get(woid));
                    jArr.put(tempJobj);
                }
            }
            jobj.put("data", jArr);
            
        } catch (Exception ex) {
            Logger.getLogger(AccWorkOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    //Get Details of shortfall product
    public JSONArray getShortFallProductsDetails(Map<String, Object> requestParams) throws ServiceException {
        JSONArray dataJArr = new JSONArray();
        KwlReturnObject result = null;
        String productid = "";
        String productidstr = "";
        if (requestParams.get("productidstr") != null) {
            productidstr = requestParams.get("productidstr").toString();
        }
        try {
            Product product = null;
            HashMap<String, Object> paramsMap = new HashMap<>();
            String companyid = "";
            if (requestParams.containsKey("companyid")) {
                companyid = requestParams.get("companyid").toString();
                paramsMap.put("companyid", companyid);
            }
            if (requestParams.containsKey("productid")) {
                productid = requestParams.get("productid").toString();
                if (!StringUtil.isNullOrEmpty(productid)) {
                    paramsMap.put("productid", productid);
                }
            }

            result = workOrderDAOObj.getWODetailfromProductandWO(paramsMap);
            List<WorkOrderComponentDetails> resultList = result.getEntityList();
            Set<String> productIdSet = new HashSet<>();

            // Extracting all distinct product ids in a set.
            for (WorkOrderComponentDetails woComDet : resultList) {
                String productId = woComDet.getProduct() != null ? woComDet.getProduct().getID() : "";
                if (!productIdSet.contains(productId)) {
                    productIdSet.add(productId);
                }
            }
            for (String productId : productIdSet) {
                JSONObject tempObj = new JSONObject();
                double shortfallQuantity = 0.0;
                String WOIdStr = "";
                product = (Product) accountingHandlerDAOobj.getObject(Product.class.getName(), productId).getEntityList().get(0);
                for (WorkOrderComponentDetails woComDet : resultList) {
                    String woProductId = woComDet.getProduct() != null ? woComDet.getProduct().getID() : "";
                    if (woProductId.equalsIgnoreCase(productId)) {
                        JSONObject paramJobj = new JSONObject();
                        paramJobj.put("productId", productId);
                        paramJobj.put("wocdid", woComDet.getID());
                        JSONObject WoCdProdQuantityJobj = getWOCompDetProdQuantities(paramJobj);
                        shortfallQuantity += WoCdProdQuantityJobj.getDouble("shortfallQuantity");
                    }
                }
                    String prodcutid = product.getID();
                    if (shortfallQuantity > 0 && productidstr.contains(prodcutid)) {
                        tempObj.put("id", product.getID());
                        tempObj.put("productId", product.getProductid());
                        tempObj.put("productName", product.getProductName());
                        tempObj.put("workOrders", WOIdStr);
                        tempObj.put("shortfallQuantity", shortfallQuantity);
                        tempObj.put("isAsset", false);
                        tempObj.put("baseuomquantity", shortfallQuantity);
                        tempObj.put("balanceQuantity", 1);
                        tempObj.put("type", product.getProducttype() != null ? product.getProducttype().getName() : "");
                        tempObj.put("salesPerson", "");
                        tempObj.put("productid", product.getID());
                        tempObj.put("copyquantity", 1);
                        tempObj.put("description", product.getDescription());
                        tempObj.put("taxamount", 0);
                        tempObj.put("linkid", "");
                        tempObj.put("quantity", shortfallQuantity);
                        tempObj.put("prdiscount", 0);
                        tempObj.put("uomid",product.getUnitOfMeasure().getID());
                        tempObj.put("amountForExcelFile", "11.00");
                        tempObj.put("dorowid", "");
                        tempObj.put("lockquantity", 0);
                        tempObj.put("rateIncludingGst", 0);
                        tempObj.put("priceSource", "");
                        tempObj.put("rowTaxPercent", 0);
                        tempObj.put("status", "N/A");
                        tempObj.put("rowTaxAmount", 0);
                        tempObj.put("currencysymbol", "SGD");
                        tempObj.put("islockQuantityflag", false);
                        tempObj.put("baseuomrate", 1);
                        tempObj.put("srno", 1);
                        tempObj.put("availableQtyInSelectedUOM", 1);
                        tempObj.put("multiuom", false);
                        tempObj.put("orderrate", 11);
                        tempObj.put("desc", product.getDescription());
                        tempObj.put("memo", "");
                        tempObj.put("originalTransactionRowid", "");
                        tempObj.put("recTermAmount", 0);
                        tempObj.put("OtherTermNonTaxableAmount", 0);
                        tempObj.put("uomname", product.getUnitOfMeasure().getNameEmptyforNA());
                        tempObj.put("invlocation", "");
                        tempObj.put("LineTermdetails", "[]");
                        tempObj.put("rate", 0);
                        tempObj.put("dquantity", 1);
                        tempObj.put("prtaxid", "");
                        tempObj.put("productname", product.getProductName());
                        tempObj.put("linktype", -1);
                        tempObj.put("prtaxpercent", 0);
                        tempObj.put("baseuomname", product.getUnitOfMeasure().getNameEmptyforNA());
                        tempObj.put("invstore", "");
                        tempObj.put("lockquantitydue", 0);
                        tempObj.put("pid", product.getProductid());
                        tempObj.put("linkto", "");
                        tempObj.put("discountvalue", 0);
                        tempObj.put("billid", "");
                        tempObj.put("isConsignment", false);
                        tempObj.put("hasAccess", true);
                        tempObj.put("baseuomid", product.getUnitOfMeasure().getID());
                        tempObj.put("discountispercent", 1);
                        tempObj.put("billno", "");
                        tempObj.put("rowid", 1);
                        dataJArr.put(tempObj);
                    }
//                }
            }


        } catch (Exception ex) {
            Logger.getLogger(AccWorkCentreServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dataJArr;
    }
    private JSONObject getWOCompDetProdQuantities(JSONObject jobj) {
        JSONObject retJobj = new JSONObject();
        try {
            String productId = "";
            String wocdId = "";
            
            Double assmblyLockQuantity = 0.0;
            Double SoLockQuantity = 0.0;
            Double WOLockQuantity = 0.0;
            Double totalLockQuantity = 0.0;
            Double availableQuantity = 0.0;
            Double availableQuantityForUse = 0.0;
            Double reqQuantity = 0.0;
            Double shortfallQuantity = 0.0;
            
            Product product = null;
            WorkOrderComponentDetails wocd = null;
            
            if (jobj.has("productId") && !StringUtil.isNullOrEmpty(jobj.optString("productId", ""))) {
                productId = jobj.getString("productId");
            }
            if (jobj.has("wocdid") && !StringUtil.isNullOrEmpty(jobj.optString("wocdid", ""))) {
                wocdId = jobj.getString("wocdid");
                wocd = (WorkOrderComponentDetails) accountingHandlerDAOobj.getObject(WorkOrderComponentDetails.class.getName(), wocdId).getEntityList().get(0);
            }
            
            if (!StringUtil.isNullOrEmpty(productId)) {
                
                product = (Product) accountingHandlerDAOobj.getObject(Product.class.getName(), productId).getEntityList().get(0);
                
                // Fetching Block Quantity locked in assembly
                KwlReturnObject result1 = accProductDaoObj.getAssemblyLockQuantity(productId);
                assmblyLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));
                
                // Fetching Block Quantity locked in Sales Orders
                KwlReturnObject result2 = accProductDaoObj.getLockQuantity(productId);
                SoLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));
                
                // Fetching Block Quantity locked in Work Orders
                KwlReturnObject result3 = accProductDaoObj.getWOLockQuantity(productId);
                WOLockQuantity = (Double) (result3.getEntityList().get(0) == null ? 0.0 : result3.getEntityList().get(0));
            }
            
            totalLockQuantity = assmblyLockQuantity + SoLockQuantity + WOLockQuantity;
            availableQuantity = product != null ? product.getAvailableQuantity() : 0.0;
            availableQuantityForUse = availableQuantity - totalLockQuantity;
            if (availableQuantityForUse < 0) {
                availableQuantityForUse = 0.0;
            }
            reqQuantity = wocd.getRequiredQuantity();
            shortfallQuantity = reqQuantity - availableQuantityForUse;
            if (shortfallQuantity < 0) {
                shortfallQuantity = shortfallQuantity < 0 ? 0.0 : shortfallQuantity;
            } else {
                shortfallQuantity = shortfallQuantity < 0 ? 0.0 : shortfallQuantity;
            }
            
            retJobj.put("shortfallQuantity",shortfallQuantity);
        } catch (JSONException ex) {
            Logger.getLogger(AccWorkOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccWorkOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retJobj;
    }

    
    @Override
    public JSONObject getTaskDetailsOfworkOrder(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        try {
            dataJArr = getWorkOrdersTaskWithTheirDetails(requestParams);
            jobj.put("success", true);
            jobj.put("valid", true);
            jobj.put("count", dataJArr.length());
            JSONObject dataObj = new JSONObject();
            dataObj.put("data", dataJArr);
            jobj.put("data", dataObj);
//           
        } catch (Exception ex) {
            Logger.getLogger(AccWorkCentreServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    public JSONArray getWorkOrdersTaskWithTheirDetails(Map<String, Object> requestParams) throws ServiceException {
        JSONObject resObj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        JSONArray tempJarr=new JSONArray();
        try {
          
            String accRestURL = URLUtil.buildRestURL("pmURL");
            JSONObject userData = new JSONObject();
            String userId = "",companyId = "",projectid="",statustype="",ss="",workorderid="";
            DateFormat userdf=(DateFormat)requestParams.get(Constants.userdf);
            if (requestParams.containsKey("companyid")) {
                companyId = (String) requestParams.get("companyid");
            }
            if (requestParams.containsKey("userid")) {
                userId = (String) requestParams.get("userid");
            }
            if (requestParams.containsKey("projectid")) {
                projectid = (String) requestParams.get("projectid");
            }
            if (requestParams.containsKey("workorderid")) {
                workorderid = (String) requestParams.get("workorderid");
            }
            
            if (requestParams.containsKey(Constants.ss)) {
                ss = (String) requestParams.get(Constants.ss);
            }
            
            userData.put("userid", userId);
            userData.put("companyid", companyId);
            userData.put("projectid", projectid);
            userData.put("statustype", statustype);
            userData.put(Constants.ss, ss);
       
            String endpoint = accRestURL + "transaction/getTasksInProjectWithDetails";
            resObj = apiCallHandlerService.restGetMethod(endpoint,userData.toString());
            tempJarr= resObj.getJSONArray("data");
            
            //***************Get Inventory Consumpttion Details from workordercomponentdetails table*********************
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy"); 
            JSONObject finalJobj=null;
            JSONArray finalJArray=new JSONArray();
            Map<String,Object> consumptionParams=new HashMap();
            consumptionParams.put("projectId", projectid);
            consumptionParams.put("companyid", companyId);
            for(int i=0; i<tempJarr.length();i++){
                finalJobj=tempJarr.getJSONObject(i);
                     finalJArray=new JSONArray();
                
                if (finalJobj.has("productid")) {
                    consumptionParams.put("productId", finalJobj.getString("productid"));

                    KwlReturnObject result = workOrderDAOObj.getWorkOrderComponentDetails(consumptionParams);
                    List dataList = result.getEntityList();
                    JSONObject tmpObj = new JSONObject();
                    for (Object obj : dataList) {
                        WorkOrderComponentDetails componentDetails = (WorkOrderComponentDetails) obj;
                        tmpObj = new JSONObject();
                        Product product = componentDetails.getProduct();
                        String prodcutid = product.getID();
                        tmpObj.put("productcode", componentDetails.getProduct() != null ? componentDetails.getProduct().getProductid() : "");
                        tmpObj.put("productname", componentDetails.getProduct() != null ? componentDetails.getProduct().getProductName() : "");
                        
                        if (componentDetails.getConsumptionDetails() != null) {
                            JSONObject consObj = new JSONObject(componentDetails.getConsumptionDetails());
                            tmpObj.put("requiredqty", consObj.getString("requiredquantity"));
                            tmpObj.put("blockedqty", consObj.getString("blockquantity"));
                        }else{
                            tmpObj.put("requiredqty", 0);
                            tmpObj.put("blockedqty", 0);
                        }
                        tmpObj.put("rejectedqty", componentDetails.getRejectedQuantity());
                        tmpObj.put("wastedqty", componentDetails.getWastedQuantity());
                        tmpObj.put("recycledqty", componentDetails.getRecycledQuantity());
                        tmpObj.put("id", finalJobj.has("id") ? finalJobj.getString("id") : "");
                        finalJArray.put(tmpObj);
                    }
                }
                finalJobj.put("consumptionDetails", finalJArray);
                
                
                String startdateStr=finalJobj.getString("startdate");
                Date startDate = (Date)formatter.parse(startdateStr); 
                
                 String enddateStr=finalJobj.getString("enddate");
                Date endDate = (Date)formatter.parse(enddateStr); 
                
                finalJobj.put("startdate", userdf.format(startDate));
                finalJobj.put("enddate", userdf.format(endDate));
                
                dataJArr.put(finalJobj);
            }
            
            System.out.println("sp");
        } catch (com.krawler.utils.json.base.JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return dataJArr;
    }

    @Override
    public JSONObject exportWorkOrdersTask(Map<String, Object> requestParams) throws ServiceException {
         JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        try {

            jobj = getTaskDetailsOfworkOrder(requestParams);
            
            JSONObject TempObj=jobj.getJSONObject("data");
            jobj.put("data", TempObj.get("data"));
            request = (HttpServletRequest) requestParams.get("request");
            response = (HttpServletResponse) requestParams.get("response");
            exportDaoObj.processRequest(request, response, jobj);

        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    
    
    JSONObject getRestParameters(Map<String, Object> requestParams) throws ServiceException {
        JSONObject userData = new JSONObject();
        try {

            String userId = "";
            String companyId = "";
            String cdomain = "";
            if (requestParams.containsKey("companyId")) {
                companyId = (String) requestParams.get("companyId");
            }
            if (requestParams.containsKey("cdomain")) {
                cdomain = (String) requestParams.get("cdomain");
            }
            if (requestParams.containsKey("userId")) {
                userId = (String) requestParams.get("userId");
            }
            userData.put("companyid", companyId);
            userData.put("cdomain", cdomain);
            userData.put("withUserDetails", true);

        } catch (Exception ex) {
            Logger.getLogger(AccWorkOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccLabourServiceImpl.getRestParameters", ex);
        }

        return userData;
    }


    @Override
    public JSONObject getWorkOrderCosting(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        JSONObject resObj = new JSONObject();
        double totalCostOfProject = 0.0;
        try {

            /*
             Get all the work orders in which he is currently working
             */
            boolean isMRPProfitablityReport= requestParams.containsKey("isMRPProfitablityReport")? (Boolean)requestParams.get("isMRPProfitablityReport") : false;
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            boolean isExport = requestParams.containsKey("isExport") ? (boolean) requestParams.get("isExport") : false;
            int costingType = requestParams.containsKey("costingType") ? (Integer) requestParams.get("costingType") : 0;

            String woid = requestParams.containsKey("woid") ? requestParams.get("woid").toString() : "";
            if (!StringUtil.isNullOrEmpty(woid)) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), woid);
                WorkOrder work = (WorkOrder) result.getEntityList().get(0);
                String woName = work.getWorkOrderName();

                Product product = work.getProductID();
                //**********************************sales orders*************************************
                double SoTotalCost = 0.0;
                if (isMRPProfitablityReport) {
                    JSONObject soDetailObj = getSalesOrderDetails(work, product, dataJArr);
                    SoTotalCost = soDetailObj.getDouble("SoTotalCost");
                }

                String accRestURL = URLUtil.buildRestURL("pmURL");
                JSONObject userData = getRestParameters(requestParams);
                userData.put("projectid", work.getProjectId());
                String endpoint = accRestURL + "task/projecttasks";
                resObj = apiCallHandlerService.restGetMethod(endpoint, userData.toString());

                if (costingType == Constants.MRPCOSTTYPENUMERIC_ALL || costingType == Constants.MRPCOSTTYPENUMERIC_LABOUR) {

                    if (resObj.has("success")) {
                        //**********************Labour Costing******************************************************
                        try {
                            JSONObject labourData = getlaoburCostJsonArray(woName, df, resObj, dataJArr);
                            totalCostOfProject += labourData.getDouble("labourTotalCost");
                        } catch (Exception ex) {
                            Logger.getLogger(AccWorkOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
                
                if (costingType == Constants.MRPCOSTTYPENUMERIC_ALL || costingType == Constants.MRPCOSTTYPENUMERIC_MACHINE) {

                    if (resObj.has("success")) {
                        //**********************Machine Costing******************************************************
                        try {
                            JSONObject machineData = getMachineCostJsonArray(woName, df, resObj, dataJArr);
                            totalCostOfProject += machineData.getDouble("machineTotalCost");
                        } catch (ServiceException | com.krawler.utils.json.base.JSONException ex) {
                            Logger.getLogger(AccWorkOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
                
                //**********************Material Costing******************************************************
                if (costingType == Constants.MRPCOSTTYPENUMERIC_ALL || costingType == Constants.MRPCOSTTYPENUMERIC_MATERIAL) {
                    try {
                        JSONObject materialData = getMaterialCostDataArr(woName, work, dataJArr);
                        totalCostOfProject += materialData.getDouble("totalMaterialCost");
                    } catch (Exception ex) {
                        Logger.getLogger(AccWorkOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                //********************Total cost Object***********************************
                if (isExport) {
                    //Blank record to add blank row in exported file
                    JSONObject blankObj = new JSONObject();
                    blankObj.put("costtype", Constants.MRPCOSTTYPE_TOTAL);
                    blankObj.put("name", "");
                    blankObj.put("workorder", "");
                    blankObj.put("cost", "");
                    dataJArr.put(blankObj);
                }

                //record to displaye total as a last record
                JSONObject totalCost = new JSONObject();
                totalCost.put("costtype", Constants.MRPCOSTTYPE_TOTAL);
                totalCost.put("name", Constants.MRPCOSTTYPE_TOTAL);
                if (!isExport) {
                    totalCost.put("name", "");
                }
                totalCost.put("workorder", woName);
                totalCost.put("cost", totalCostOfProject);
                dataJArr.put(totalCost);

                //********************Work order profitablility report*************************
                if (isMRPProfitablityReport) {
                    double pnlAmt = SoTotalCost - totalCostOfProject;
                    JSONObject pnlObj = new JSONObject();
                    pnlObj.put("costtype", pnlAmt > 0 ? Constants.MRPCOSTTYPE_PROFIT : Constants.MRPCOSTTYPE_LOSS);
                    pnlObj.put("name", "");
                    pnlObj.put("workorder", "");
                    pnlObj.put("cost", pnlAmt);
                    dataJArr.put(pnlObj);
                }

        }
                JSONObject dataObj = new JSONObject();
                if (isExport) {
                    jobj.put("data", dataJArr);
                } else {
                    jobj.put("success", true);
                    jobj.put("valid", true);
                    jobj.put("count", dataJArr.length());
                    dataObj.put("data", dataJArr);
                    jobj.put("data", dataObj);
                }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccLabourServiceImpl.getLabourCosting", ex);
        }
        return jobj;
    }
    
    
    private JSONObject getSalesOrderDetails(WorkOrder work, Product product, JSONArray dataJArr) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            String productID = product.getID();
            //**********************************sales orders*************************************
            SalesOrder so = work.getSalesOrder();
            Set<SalesOrderDetail> sodSet = null;
            double quantity = 0.0;
            double price = 0.0;
            double SoTotalCost=0.0;
            if (so != null) {
                sodSet = so.getRows();

                for (SalesOrderDetail sod : sodSet) {
                    quantity = 0.0;
                    price = 0.0;
                    if (productID.equals(sod.getProduct().getID())) {
                        quantity = sod.getQuantity();
                        price = sod.getRate();
                        SoTotalCost=quantity * price;
                        JSONObject sodObj = new JSONObject();
                        sodObj.put("costtype", Constants.MRPCOSTTYPE_REVENUE);
                        sodObj.put("sonumber", sod.getSalesOrder().getSalesOrderNumber());
                        sodObj.put("workorder", work.getWorkOrderName());
                        sodObj.put("cost", SoTotalCost);

                        dataJArr.put(sodObj);
                    }

                }
            }
        jobj.put("SoTotalCost", SoTotalCost);
        } catch (Exception ex) {
            Logger.getLogger(AccWorkOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccLabourServiceImpl.getRestParameters", ex);
        }
        return jobj;
    }
     /**
     * description- This method calculate total cost of all labour and return
     * jsonarray of labour and its cost
     *
     * @param resObj
     * @param dataJArr
     * @return jsonObject
     */
    public JSONObject getlaoburCostJsonArray(String woName, DateFormat df, JSONObject resObj, JSONArray dataJArr) throws ServiceException {
        JSONObject jobj = new JSONObject();
        double labourTotalCost = 0.0;
        try {
            JSONArray resArr = resObj.getJSONArray("data");
            //Iterating tasks
            for (int i = 0; i < resArr.length(); i++) {
                JSONObject taskJobj = resArr.getJSONObject(i);

                String taskName = taskJobj.getString("taskName");
                JSONArray resourceArr = taskJobj.getJSONArray("resourceDetails");
                //Iterating laoburs working on task 
                for (int j = 0; j < resourceArr.length(); j++) {
                    JSONObject ResObj = resourceArr.getJSONObject(j);

                    String reosurcid = ResObj.getString("resourceId");
                    String reosurcTypeId = ResObj.getString("resourceTypeId");
                    if (!reosurcTypeId.equals(Constants.MRP_RESOURCETYPE_LABOUR)) {
                        continue;
                    }
                    KwlReturnObject resourceResult = accountingHandlerDAOobj.getObject(Labour.class.getName(), reosurcid);
                    Labour labour = (Labour) resourceResult.getEntityList().get(0);
                    String labourName = labour.getFullName();   
                   
                    Date shifttime = null;
                    Calendar calendar = Calendar.getInstance();

                    double minToHr = 0.0;

                    /*
                     * If shifttime is not empty or null then convert string to
                     * date(Calender)
                     */
                    if (!StringUtil.isNullOrEmpty(labour.getShifttiming())) {

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
                        shifttime = sdf.parse(labour.getShifttiming());
                        calendar.setTime(shifttime);

                        double minute = calendar.get(Calendar.MINUTE);
                        /*
                         * If Minute is greater than zero then it can be converted to minute to hour.       
                         */
                        if(minute>0){
                            minToHr = minute / Constants.MINUTE_PER_HOUR;
                        }
                    }

                    JSONArray hrsDetailsArr = ResObj.getJSONArray("resourceHoursDetails");
                    double labourCost = 0.0;
                    double totalHrs = 0;
                    double dayWiseCostOflabour = 0.0;
                    double effectiveCost = 0.0;  
                    //Iterating daywise working hours of each labour
                    for (int k = 0; k < hrsDetailsArr.length(); k++) {
                        JSONObject hrsObj = hrsDetailsArr.getJSONObject(k);
                        Iterator ite = hrsObj.keys();
                        String date = "";
                        String hours = "";
                        while (ite.hasNext()) {
                            /*
                             Get date and horus of each working day of laobur
                             */
                            date = (String) ite.next();
                            hours = hrsObj.getString(date);    
                        }
                        Date workDate = df.parse(date);
                        double hrs = 0.0;
                        /*
                         * If shifttime is not empty or null then get hours from
                         * labour form(i.e Shift Timing) otherwise it take hours
                         * from PM side(i.e Working Hours))
                         */
                        if (shifttime != null) {

                            hrs = calendar.get(Calendar.HOUR_OF_DAY) + minToHr;
                        } else {
                            hrs = Integer.parseInt(hours);
                        }
      
                        //Get cost of a particular day for the labour
                        Map<String, Object> effectiveCostParams = new HashMap<>();
                        effectiveCostParams.put("labourId", reosurcid);
                        effectiveCostParams.put("workDate", workDate);                        
                        KwlReturnObject costResult = accLabourDAOObj.getResourceCostSQL(effectiveCostParams);
                       // ResourceCost resourceCost = (ResourceCost) costResult.getEntityList().get(0);
                        List<Object[]> list = costResult.getEntityList(); 
                        for (Object[] resourceCost : list) {
                            effectiveCost =resourceCost[2]!= null ? Double.parseDouble((String) resourceCost[2]) : 0.0;
                            break;//Getting on zeroth element
                        }                     
                        dayWiseCostOflabour = hrs * effectiveCost;
                        labourCost += dayWiseCostOflabour;
                        totalHrs += hrs;
                    }
                    labourTotalCost += labourCost;
                    JSONObject labourObj = new JSONObject();
                    labourObj.put("costtype", Constants.MRPCOSTTYPE_LAOBUR);
                    labourObj.put("name", labourName);
                    labourObj.put("workorder", woName);
                    labourObj.put("taskname", taskName);
                    labourObj.put("hours", totalHrs);
                    labourObj.put("cost", labourTotalCost);

                    dataJArr.put(labourObj);

                }

            }
            jobj.put("labourTotalCost", labourTotalCost);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AcclabourServiceImpl.getlaoburCostJsonArray", ex);
        }
        return jobj;
    }
    
     /**
     * description- This method calculate total cost of all machine and return
     * json array of machine and its cost
     *
     * @param resObj
     * @param dataJArr
     * @return jsonObject
     */
    public JSONObject getMachineCostJsonArray(String woName, DateFormat df, JSONObject resObj, JSONArray dataJArr) throws ServiceException {
        JSONObject jobj = new JSONObject();
        double machineTotalCost = 0.0;
        try {
            JSONArray resArr = resObj.getJSONArray("data");
            //Iterating tasks
            for (int i = 0; i < resArr.length(); i++) {
                JSONObject taskJobj = resArr.getJSONObject(i);

                String taskName = taskJobj.getString("taskName");
                JSONArray resourceArr = taskJobj.getJSONArray("resourceDetails");
                //Iterating machine working on task 
                for (int j = 0; j < resourceArr.length(); j++) {
                    JSONObject ResObj = resourceArr.getJSONObject(j);

                    String reosurcid = ResObj.getString("resourceId");
                    String reosurcTypeId = ResObj.getString("resourceTypeId");
                    if (!reosurcTypeId.equals(Constants.MRP_RESOURCETYPE_MACHINE)) {
                        continue;
                    }
                    KwlReturnObject resourceResult = accountingHandlerDAOobj.getObject(Machine.class.getName(), reosurcid);
                    Machine machine = (Machine) resourceResult.getEntityList().get(0);
                    String labourName = machine.getMachineName();
                   
                    Date shifttime = null;
                    Calendar calendar = Calendar.getInstance();

                    double minToHr = 0.0;

                    /*
                     * If shifttime is not empty or null then convert string to
                     * date(Calender)
                     */
                    if (!StringUtil.isNullOrEmpty(machine.getShifttiming())) {

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
                        shifttime = sdf.parse(machine.getShifttiming());
                        calendar.setTime(shifttime);

                        double minute = calendar.get(Calendar.MINUTE);
                        /*
                         * If Minute is greater than zero then it can be converted to minute to hour.       
                         */
                        if(minute>0){
                            minToHr = minute / Constants.MINUTE_PER_HOUR;
                        }
                    }

                    JSONArray hrsDetailsArr = ResObj.getJSONArray("resourceHoursDetails");
                    double machineCost = 0.0;
                    double totalHrs = 0;
                    double dayWiseCostOflabour = 0.0;
                    double effectiveCost = 0.0;  
                    //Iterating daywise working hours of each machine
                    for (int k = 0; k < hrsDetailsArr.length(); k++) {
                        JSONObject hrsObj = hrsDetailsArr.getJSONObject(k);
                        Iterator ite = hrsObj.keys();
                        String date = "";
                        String hours = "";
                        while (ite.hasNext()) {
                            /*
                             Get date and horus of each working day of machine
                             */
                            date = (String) ite.next();
                            hours = hrsObj.getString(date);    
                        }
                        Date workDate = df.parse(date);
                        double hrs = 0.0d;
                        /*
                         * If shifttime is not empty or null then get hours from
                         * machine form(i.e Shift Timing) otherwise it take hours
                         * from PM side(i.e Working Hours))
                         */
                        if (shifttime != null) {

                            hrs = calendar.get(Calendar.HOUR_OF_DAY) + minToHr;
                        } else {
                            hrs = Integer.parseInt(hours);
                        }
      
                        //Get cost of a particular day for the machine
                        Map<String, Object> effectiveCostParams = new HashMap<>();
                        effectiveCostParams.put("labourId", reosurcid);
                        effectiveCostParams.put("workDate", workDate);                        
                        KwlReturnObject costResult = accMachineManagementDAOObj.getMachineCostSQL(effectiveCostParams);
                       // ResourceCost resourceCost = (ResourceCost) costResult.getEntityList().get(0);
                        List<Object[]> list = costResult.getEntityList(); 
                        for (Object[] resourceCost : list) {
                            effectiveCost =resourceCost[2]!= null ? Double.parseDouble((String) resourceCost[2]) : 0.0;
                            break;//Getting on zeroth element
                        }                     
                        dayWiseCostOflabour = hrs * effectiveCost;
                        machineCost += dayWiseCostOflabour;
                        totalHrs += hrs;
                    }
                    machineTotalCost += machineCost;
                    JSONObject labourObj = new JSONObject();
                    labourObj.put("costtype", Constants.MRPCOSTTYPE_MACHINE);
                    labourObj.put("name", labourName);
                    labourObj.put("workorder", woName);
                    labourObj.put("taskname", taskName);
                    labourObj.put("hours", totalHrs);
                    labourObj.put("cost", machineCost);

                    dataJArr.put(labourObj);

                }

            }
            jobj.put("machineTotalCost", machineTotalCost);
        } catch (com.krawler.utils.json.base.JSONException | ServiceException | ParseException | NumberFormatException ex) {
            throw ServiceException.FAILURE("AccWorkOrderServiceImpl.getMachineCostJsonArray", ex);
        }
        return jobj;
    }
    
    /**
    * description- This method total cost of material used in workorder at task level
     * @param woName
     * @param work
     * @param dataJArr
     * @return
     * @throws ServiceException 
     */
    public JSONObject getMaterialCostDataArr(String woName, WorkOrder work, JSONArray dataJArr) throws ServiceException {
        JSONObject jobj = new JSONObject();
        double totalMaterialCost = 0.0;
        try {
            Set<WorkOrderComponentDetails> woComp = work.getComponentDetails();

            for (WorkOrderComponentDetails wocd : woComp) {

                String consumption = wocd.getConsumptionDetails();
                if (!StringUtil.isNullOrEmpty(consumption)) {
                    JSONObject qtyConsumptionOBj = new JSONObject(consumption);
                    double materialCost = 0.0;
                    int actQtyUsed = 0;
                    String actQty = qtyConsumptionOBj.getString("actualquantity");
                    String productID = qtyConsumptionOBj.getString("productid");
                    KwlReturnObject prResult = accountingHandlerDAOobj.getObject(Product.class.getName(), productID);
                    Product productObj = (Product) prResult.getEntityList().get(0);

                    String unitOfmeasure = productObj.getUnitOfMeasure() != null ? productObj.getUnitOfMeasure().getNameEmptyforNA() : "";
                    //get product purchase price
                    KwlReturnObject purchase = accProductDaoObj.getProductPrice(productID, true, null, "", "");
                    Double productPurchasePrice = purchase.getEntityList().get(0) != null ? (Double) purchase.getEntityList().get(0) : 0.0;
                    if (!StringUtil.isNullOrEmpty(actQty)) {
                        actQtyUsed = Integer.parseInt(actQty);
                        materialCost = actQtyUsed * productPurchasePrice;
                    }
                    totalMaterialCost += materialCost;
                    JSONObject matrialOBj = new JSONObject();
                    matrialOBj.put("costtype", Constants.MRPCOSTTYPE_MATERIAL);
                    matrialOBj.put("taskname", wocd.getTaskName());
                    matrialOBj.put("workorder", woName);
                    matrialOBj.put("cost", materialCost);
                    matrialOBj.put("materialname", productObj.getName());
                    matrialOBj.put("Description", productObj.getDescription());
                    matrialOBj.put("uom", unitOfmeasure);
                    matrialOBj.put("quantity", actQtyUsed);
                    dataJArr.put(matrialOBj);

                }

            }
            jobj.put("totalMaterialCost", totalMaterialCost);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AcclabourServiceImpl.getMaterialCostDataArr", ex);
        }
        return jobj;
    }
}
