/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author krawler
 */
package com.krawler.spring.accounting.companypreferences;

import com.krawler.common.dao.BaseDAO;
import com.krawler.common.util.Constants;
import com.krawler.hql.accounting.SequenceFormat;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UpdateExistingTransactionsWithMatchedSequenceformate extends BaseDAO implements Runnable {

    private HibernateTransactionManager txnManager;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    ArrayList processQueue = new ArrayList();
    private List<HashMap<String, Object>> list = new ArrayList();
    private boolean isworking = false;
    private sessionHandlerImpl sessionHandlerImplObj;
    
    public void setSessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj) {
        this.sessionHandlerImplObj = sessionHandlerImplObj;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }


    public void setAccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void add(HashMap<String, Object> requestParams) {
        try {
            list.clear();
            list.add(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(UpdateExistingTransactionsWithMatchedSequenceformate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /*
     * added Json object containing sequence format id and company id into processQueue
     */

    public void add(JSONObject jObj) {
        try {
            processQueue.add(jObj);
        } catch (Exception ex) {
            Logger.getLogger(UpdateExistingTransactionsWithMatchedSequenceformate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run() {
        while (!processQueue.isEmpty() && !isworking) {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("Custom_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);

            HashMap<String, Object> requestParams = list.get(0);
            JSONObject JsonObj = (JSONObject) processQueue.get(0);
            String sequenceformatid = JsonObj.optString(Constants.SEQUENCEFORMATID);
            String companyId = JsonObj.optString(Constants.companyid);
            int moduleid = JsonObj.optInt(Constants.moduleid);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            isworking = true;
            try {
                /*
                 * logged thread starting time
                 */
                System.out.println("Update existing transactions with matched sequence formate has been started for Company = " + companyId + " at : " + timestamp);
                Logger.getLogger("Update existing transactions with matched sequence formate has been started for Company = " + companyId + " at : " + timestamp);

                int startfrom = 1;
                if (requestParams.containsKey("startfrom") && requestParams.get("startfrom") != null) {
                    startfrom = (int) requestParams.get("startfrom");
                }
                JSONArray matchedtransactionArray = accCompanyPreferencesObj.checkTransactionmatchedwithSequenceFormat(requestParams);
                HashMap<String, Object> requestHashMap = new HashMap<String, Object>();
                requestHashMap.put("Seqformat", sequenceformatid);
                requestHashMap.put(Constants.moduleid, moduleid);
                requestHashMap.put("startfrom", startfrom);
                UpdateTransactions(requestHashMap, matchedtransactionArray);
                timestamp = new Timestamp(System.currentTimeMillis());
                System.out.println("Update existing transactions with matched sequence formate has been Ended for Company = " + companyId + " at : " + timestamp);
                Logger.getLogger("Update existing transactions with matched sequence formate has been Ended for Company = " + companyId + " at : " + timestamp);
                txnManager.commit(status);
            } catch (Exception ex) {
                Logger.getLogger(UpdateExistingTransactionsWithMatchedSequenceformate.class.getName()).log(Level.SEVERE, null, ex);
                txnManager.rollback(status);
            } finally {
                isworking = false;
                processQueue.remove(requestParams);
                processQueue.remove(JsonObj);
            }
        }
    }

    /**
     *
     * @param requestParams
     * @param JArray: containing the transaction which matched with sequence
     * format pattern. updating the matched transactions.
     *
     */
    public void UpdateTransactions(HashMap requestParams, JSONArray JArray) {

        try {
            int moduleid = 0, startfrom = 1;
            String sequenceformateid = "";
            if (requestParams.containsKey(Constants.moduleid) && requestParams.get(Constants.moduleid) != null) {
                moduleid = (int) requestParams.get(Constants.moduleid);
            }
            if (requestParams.containsKey(Constants.moduleid) && requestParams.get(Constants.moduleid) != null) {
                sequenceformateid = (String) requestParams.get("Seqformat");
            }
            if (requestParams.containsKey("startfrom") && requestParams.get("startfrom") != null) {
                startfrom = (int) requestParams.get("startfrom");
            }
            String TransactionClassPath = "";
            switch (moduleid) {
                case Constants.Acc_GENERAL_LEDGER_ModuleId:
                    TransactionClassPath = Constants.JournalEntry_ClassPath;
                    break;
                case Constants.Acc_Sales_Order_ModuleId:
                    TransactionClassPath = Constants.SalesOrder_ClassPath;
                    break;
                case Constants.Acc_Invoice_ModuleId:
                    TransactionClassPath = Constants.SalesInvoice_ClassPath;
                    break;
                case Constants.Acc_Cash_Sales_ModuleId:
                    TransactionClassPath = Constants.SalesInvoice_ClassPath;
                    break;
                case Constants.Acc_Credit_Note_ModuleId:
                    TransactionClassPath = Constants.CreditNote_ClassPath;
                    break;
                case Constants.Acc_Receive_Payment_ModuleId:
                    TransactionClassPath = Constants.ReceivePayment_ClassPath;
                    break;
                case Constants.Acc_Purchase_Order_ModuleId:
                    TransactionClassPath = Constants.PurchaseOrder_ClassPath;
                    break;
                case Constants.Acc_Vendor_Invoice_ModuleId:
                    TransactionClassPath = Constants.VendorInvoice_ClassPath;
                    break;
                case Constants.Acc_Cash_Purchase_ModuleId:
                    TransactionClassPath = Constants.VendorInvoice_ClassPath;
                    break;
                case Constants.Acc_Debit_Note_ModuleId:
                    TransactionClassPath = Constants.DebitNote_ClassPath;
                    break;
                case Constants.Acc_Make_Payment_ModuleId:
                    TransactionClassPath = Constants.MakePayment_ClassPath;
                    break;
                case Constants.Acc_Contract_Order_ModuleId:
                    TransactionClassPath = Constants.Contract_ClassPath;
                    break;
                case Constants.Acc_Customer_Quotation_ModuleId:
                    TransactionClassPath = Constants.CustomerQuotation_ClassPath;
                    break;
                case Constants.Acc_Vendor_Quotation_ModuleId:
                    TransactionClassPath = Constants.VendorQuotation_ClassPath;
                    break;
                case Constants.Acc_Purchase_Requisition_ModuleId:
                    TransactionClassPath = Constants.PurchaseRequisition_ClassPath;
                    break;
                case Constants.Acc_RFQ_ModuleId:
                    TransactionClassPath = Constants.RFQ_ClassPath;
                    break;
                case Constants.Acc_Product_Master_ModuleId:
                    TransactionClassPath = Constants.Product_ClassPath;
                    break;
                case Constants.Acc_Delivery_Order_ModuleId:
                    TransactionClassPath = Constants.DeliveryOrder_ClassPath;
                    break;
                case Constants.Acc_Goods_Receipt_ModuleId:
                    TransactionClassPath = Constants.GoodsReceiptOrder_ClassPath;
                    break;
                case Constants.Acc_Sales_Return_ModuleId:
                    TransactionClassPath = Constants.SalesReturn_ClassPath;
                    break;
                case Constants.Acc_Purchase_Return_ModuleId:
                    TransactionClassPath = Constants.PurchaseReturn_ClassPath;
                    break;
                case Constants.Acc_Customer_ModuleId:
                    TransactionClassPath = Constants.Customer_ClassPath;
                    break;
                case Constants.Acc_Vendor_ModuleId:
                    TransactionClassPath = Constants.Vendor_ClassPath;
                    break;
                case Constants.Acc_Build_Assembly_Product_ModuleId:

                    break;
                case Constants.Acc_FixedAssets_AssetsGroups_ModuleId:

                    break;
                case Constants.Labour_Master:
                    TransactionClassPath = Constants.Labour_ClassPath;
                    break;
                case Constants.MRP_Contract:
                    TransactionClassPath = Constants.MRPContract_ClassPath;
                    break;
                case Constants.MRP_JOB_WORK_MODULEID:
                    TransactionClassPath = Constants.JobWork_ClassPath;
                    break;
                case Constants.MRP_RouteCode:

                    break;
                case Constants.MRP_WORK_CENTRE_MODULEID:

                    break;
                case Constants.MRP_WORK_ORDER_MODULEID:
                    TransactionClassPath = Constants.WorkOrder_ClassPath;
                    break;
                case Constants.Acc_PackingDO_ModuleId:

                    break;
                case Constants.Acc_ShippingDO_ModuleId:

                    break;
                case Constants.Acc_SecurityGateEntry_ModuleId:

                    break;
            }

            requestParams.remove("moduleid");
            requestParams.remove("startfrom");
//            requestParams.put("AutoGenerated", "TRUE");//NO need to update AutoGenerated as it is not present in all modules.SDP-13227
            SequenceFormat sequenceformat = (SequenceFormat) get(SequenceFormat.class, sequenceformateid);

            for (int i = 0; i < JArray.length(); i++) {
                JSONObject jobj = JArray.getJSONObject(i);
                String transactionid = jobj.optString("transactionid");
                requestParams.put("ID", transactionid);
                /**
                 * SDP-16487 -Existing sequence format is not updating when we
                 * save sequence format 
                 * parameters in the requestParams is used to set sequence format by calling methods. In
                 * the MRPContract model the method names are different. hence
                 * passing value as per mrp module.
                 */
                if (moduleid == Constants.MRP_Contract) {
                    requestParams.put("Autogen", true);
                    requestParams.put("Datepreffixvalue", jobj.optString("selecteddateformatBeforePrefix"));
                    requestParams.put("Datesuffixvalue", jobj.optString("selecteddateformatSuffix"));
                } else if (moduleid == Constants.Acc_Product_Master_ModuleId) {
                    requestParams.put("DatePreffixValue", jobj.optString("selecteddateformatBeforePrefix"));
                    requestParams.put("DateSuffixValue", jobj.optString("selecteddateformatSuffix"));
                } else {
                    requestParams.put("AutoGenerated", true);
                    requestParams.put("DatePreffixValue", jobj.optString("selecteddateformatBeforePrefix"));
                    requestParams.put("DateSuffixValue", jobj.optString("selecteddateformatSuffix"));
                }
                requestParams.put("DateAfterPreffixValue", jobj.optString("dateFormatAfterPrefix"));
                requestParams.put("Seqnumber", jobj.optInt("intPartValue"));
                if (transactionid != "" && TransactionClassPath != "" && sequenceformat != null) {
                    /**
                     * Set the following columns : sequenceformat, Seqnumber,
                     * AutoGenerated, DatePreffixValue, DateSuffixValue,
                     * DateAfterPreffixValue.
                     */
                    setterMethod(requestParams, TransactionClassPath, "ID");
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(UpdateExistingTransactionsWithMatchedSequenceformate.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
