package com.krawler.spring.accounting.account;

/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author krawler
 */
public class UpdateExistingRecordsWithDefaultValue extends BaseDAO implements Runnable {

    private HibernateTransactionManager txnManager;
    ArrayList processQueue = new ArrayList();
    private boolean isworking = false;
    
    @Override
    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }
    
    /*
     *added requestParams into processQueue
     */
    public void add(HashMap<String, Object> requestParams) {
        try {
            processQueue.add(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(UpdateExistingRecordsWithDefaultValue.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     *added Module array into processQueue
     */
    public void add(ArrayList moduleArr) {
        try {
            processQueue.add(moduleArr);
        } catch (Exception ex) {
            Logger.getLogger(UpdateExistingRecordsWithDefaultValue.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        while (!processQueue.isEmpty() && !isworking) {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("Custom_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            
            HashMap<String, Object> requestParams = (HashMap<String,Object>) processQueue.get(0);
            ArrayList moduleArr = (ArrayList) processQueue.get(1);
            Timestamp timestamp =  new Timestamp(System.currentTimeMillis());
            isworking = true;
            try {
                String companyId = (String) requestParams.get(Constants.companyKey);
                HashMap<Integer,Object> modulerequestParams = (HashMap<Integer,Object>) requestParams.get("modulerequestParams");
                /*
                *logged thread starting time
                */
                System.out.println("Update existing records with default value has been started for Company = " + companyId + " at : " + timestamp);
                Logger.getLogger("Update existing records with default value has been started for Company = " + companyId + " at : " + timestamp);

                for (Object moduleArr1 : moduleArr) {
                    HashMap<String, Object> moduleParams = new HashMap<>();
                    Integer moduleid = Integer.parseInt(moduleArr1.toString());
                    if (modulerequestParams.containsKey(moduleid)) {
                        moduleParams = (HashMap<String, Object>) modulerequestParams.get(moduleid);//Get Module request params
                        moduleParams.put(Constants.moduleid, moduleid);
                        moduleParams.put(Constants.companyKey, companyId);
                        storeDefaultCstmData(moduleParams);//To update table with default value
                    }
                }
                timestamp =  new Timestamp(System.currentTimeMillis());
                System.out.println("Update existing records with default value has been Ended for Company = " + companyId + " at : " + timestamp);
                Logger.getLogger("Update existing records with default value has been Ended for Company = " + companyId + " at : " + timestamp);
                txnManager.commit(status);
            } catch (Exception ex) {
                Logger.getLogger(UpdateExistingRecordsWithDefaultValue.class.getName()).log(Level.SEVERE, null, ex);
                txnManager.rollback(status);
            } finally {
                isworking = false;
                processQueue.remove(requestParams);
                processQueue.remove(moduleArr);
            }
        }
    }

    /**
     *
     * @param requestParams
     * @desc Update Existing records with default value
     */
    public void storeDefaultCstmData(HashMap<String, Object> requestParams) {
        try {
            if (requestParams.get(Constants.RES_success).toString().equals("1") && requestParams.get(Constants.defaultvalue) != null && !StringUtil.isNullOrEmpty(requestParams.get(Constants.defaultvalue).toString())) {
                String refcolumnname = null;
                if (requestParams.containsKey(Constants.Refcolnum)) {
                    refcolumnname = Constants.Custom_column_Prefix + requestParams.get(Constants.Refcolnum).toString();
                }
                String Colnum = Constants.Custom_column_Prefix + requestParams.get(Constants.Colnum).toString();
                String defaultvalue = requestParams.get(Constants.defaultvalue).toString();
                String companyid = (String) requestParams.get("companyid");
                Integer moduleid = (Integer) requestParams.get(Constants.moduleid);
                Integer iscustomcolumn = (Integer) requestParams.get(Constants.iscustomcolumn);

                Map<String, String> customDataDetails = getCustomDataDetails(moduleid);
                String modulename = iscustomcolumn == 1 ? customDataDetails.get("detailClasspath") : customDataDetails.get("globalClasspath");

                String updatequery = null;
                Colnum = Colnum.replace(Constants.C, Constants.c);
                String whereCondition = " where  company.companyID = '" + companyid + "' and moduleId='" + moduleid + "'  ";
                if (!StringUtil.isNullOrEmpty(modulename)) {
                    if (!StringUtil.isNullOrEmpty(refcolumnname)) {  //Update sort column and values column in case of multiselect drop-down whose entry present in the customdata table
                        updatequery = "update " + modulename + " set " + Colnum + "='" + defaultvalue + "' , " + refcolumnname + "='" + defaultvalue.split(",")[0] + "' ";
                    } else {//Update value column whose entry present in the customdata table
                        updatequery = "update " + modulename + " set " + Colnum + "='" + defaultvalue + "' ";
                    }
                    updatequery += whereCondition;
                    executeUpdate(updatequery);
                }else{
                    System.out.println("Existing records with default value has been not updated for Module id : "+moduleid);
                    Logger.getLogger("Existing records with default value has been not updated for Module id : "+moduleid +" POJO Entry not found in getCustomDataDetails() method");
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(UpdateExistingRecordsWithDefaultValue.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param moduleid
     * @return POJO path for Global Custom data and Line Custom data for requested Module id
     */
    public Map<String, String> getCustomDataDetails(int moduleid) {
        Map<String, String> customDataDetails = new HashMap<>();
        String globalClasspath = "", detailClasspath = "";
        try {
            switch (moduleid) {
                case 41:
                case 51:
                case 67:
                case 27:
                    globalClasspath = "com.krawler.hql.accounting.DeliveryOrderCustomData";
                    detailClasspath = "com.krawler.hql.accounting.DeliveryOrderDetailCustomData";
                    break;
                case 28:
                case 40:
                case 57:
                    globalClasspath = "com.krawler.hql.accounting.GoodsReceiptOrderCustomData";
                    detailClasspath = "com.krawler.hql.accounting.GoodsReceiptOrderDetailsCustomDate";
                    break;
                case 63:
                case 90:
                case 18:
                    globalClasspath = "com.krawler.hql.accounting.PurchaseOrderCustomData";
                    detailClasspath = "com.krawler.hql.accounting.PurchaseOrderDetailsCustomData";
                    break;
                case 36:            //LO
                case 50:
                case 20://consignment
                    globalClasspath = "com.krawler.hql.accounting.SalesOrderCustomData";
                    detailClasspath = "com.krawler.hql.accounting.SalesOrderDetailsCustomData";
                    break;
                case 23:        //VQ
                case 89:
                    globalClasspath = "com.krawler.hql.accounting.VendorQuotationCustomData";
                    detailClasspath = "com.krawler.hql.accounting.VendorQuotationDetailCustomData";
                    break;
                case 22:        //Cq
                case 65:
                    globalClasspath = "com.krawler.hql.accounting.QuotationCustomData";
                    detailClasspath = "com.krawler.hql.accounting.QuotationDetailCustomData";
                    break;
                case 29:        //SR
                case 53:
                case 68:
                case 98:
                    globalClasspath = "com.krawler.hql.accounting.SalesReturnCustomData";
                    detailClasspath = "com.krawler.hql.accounting.SalesReturnDetailCustomData";
                    break;
                case 31:        //PR
                case 59:
                case 96:
                    globalClasspath = "com.krawler.hql.accounting.PurchaseReturnCustomData";
                    detailClasspath = "com.krawler.hql.accounting.PurchaseReturnDetailCustomDate";
                    break;
                case 32:        //PRqui
                case 87:
                    globalClasspath = "com.krawler.hql.accounting.PurchaseRequisitionCustomData";
                    detailClasspath = "com.krawler.hql.accounting.PurchaseRequisitionDetailCustomData";
                    break;
                case 33:
                case 88:        //RFQ
                    globalClasspath = "com.krawler.hql.accounting.RFQCustomData";
                    detailClasspath = "com.krawler.hql.accounting.RequestForQuotationDetailCustomData";
                    break;
                case 2:     //CI
                case 6:     //VI
                case 24:
                case 38:
                case 39:   //FA -PI 
                case 52:
                case 58:
                case 93:
                case 10:       //DN
                case 12:       //CN
                case 14:       //MP
                case 16:       //RP
                    globalClasspath = "com.krawler.hql.accounting.AccJECustomData";
                    detailClasspath = "com.krawler.hql.accounting.AccJEDetailCustomData";
                    break;
                case 30:        //product
                case 42:        //group
                    globalClasspath = "com.krawler.hql.accounting.AccProductCustomData";
                    detailClasspath = "";
                    break;
                case 34:        //account
                    globalClasspath = "com.krawler.hql.accounting.AccountCustomData";
                    detailClasspath = "";
                    break;
                case 35:        //contract
                    globalClasspath = "com.krawler.hql.accounting.ContractCustomData";
                    detailClasspath = "com.krawler.hql.accounting.ContractDetailCustomData";
                    break;
                case 25:        //Customer
                    globalClasspath = "com.krawler.hql.accounting.CustomerCustomData";
                    detailClasspath = "com.krawler.hql.accounting.CustomerCustomData";
                    break;
                case 26:        //Vendor
                    globalClasspath = "com.krawler.hql.accounting.VendorCustomData";
                    detailClasspath = "com.krawler.hql.accounting.VendorCustomData";
                    break;
                case 79:        //Serial Window
                    globalClasspath = "com.krawler.common.admin.SerialCustomData";
                    detailClasspath = "";
                    break;
                case 121:
                    globalClasspath = "com.krawler.hql.accounting.AssetDetailsCustomData";
                    detailClasspath = "";
                    break;
                case 1101:      //Labour
                    globalClasspath = "com.krawler.spring.mrp.labormanagement.LabourCustomData";
                    detailClasspath = "";
                    break;
                case 1102:      //WorkCenter
                    globalClasspath = "com.krawler.spring.mrp.workcentremanagement.WorkCentreCustomData";
                    detailClasspath = "";
                    break;
                case 1103:      //MachineMaster
                    globalClasspath = "com.krawler.spring.mrp.machinemanagement.MachineCustomData";
                    detailClasspath = "";
                    break;
                case 1104:      //JobWork
                    globalClasspath = "com.krawler.spring.mrp.jobwork.JobWorkCustomData";
                    detailClasspath = "";
                    break;
                case 1105:      //WorkOrder
                    globalClasspath = "com.krawler.spring.mrp.WorkOrder.WorkOrderCustomData";
                    detailClasspath = "";
                    break;
                case 1106:      //MRP Contract
                    globalClasspath = "com.krawler.spring.mrp.contractmanagement.MRPContractCustomData";
                    detailClasspath = "com.krawler.spring.mrp.contractmanagement.MRPContractDetailsCustomData";
                    break;
                case 1107:      //Routing Template
                    globalClasspath = "com.krawler.spring.mrp.routingmanagement.RoutingTemplateCustomData";
                    detailClasspath = "";
                    break;
            }
            customDataDetails.put("globalClasspath", globalClasspath);
            customDataDetails.put("detailClasspath", detailClasspath);
        } catch (Exception ex) {
            Logger.getLogger(UpdateExistingRecordsWithDefaultValue.class.getName()).log(Level.SEVERE, null, ex);
        }
        return customDataDetails;
    }
}
