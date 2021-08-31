/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.ProductExportDetail;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.journalentry.JournalEntryConstants;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class JournalEntryExport implements Runnable {

    ArrayList processQueue = new ArrayList();
    private boolean isworking = false;
    private HibernateTransactionManager txnManager;
    private exportMPXDAOImpl exportDaoObj;
    private accProductDAO accProductObj;
    private accJournalEntryDAO accJournalEntryobj;
    private AccFinancialReportsService accFinancialReportsService;
    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setAccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setAccFinancialReportsService(AccFinancialReportsService accFinancialReportsService) {
        this.accFinancialReportsService = accFinancialReportsService;
    }

    /*
     add request params into queue using Json Object
     */
    public void add(JSONObject requestJobj) {
        try {
            processQueue.add(requestJobj);
        } catch (Exception ex) {
            Logger.getLogger(ExportGroupDetailReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     add request params into queue using Map
     */
    public void add(HashMap map) {
        try {
            processQueue.add(map);
        } catch (Exception ex) {
            Logger.getLogger(ExportGroupDetailReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {

        /*
         Exceute code for JE Export
         */
        while (!processQueue.isEmpty() && !isworking) {

            /*
             Declaration of transaction manager
             */
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("Account_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            isworking = true;
            HashMap<String, Object> requestParams = (HashMap) processQueue.get(1);
            JSONObject requestJobj = (JSONObject) processQueue.get(0);
            JSONArray DataJArr = new JSONArray();
            try {
                Date requestTime = new Date();
                SimpleDateFormat sdfTemp = new SimpleDateFormat("ddMMyyyy_hhmmssaa");
                String filename = "Journal Entry" + (sdfTemp.format(requestTime)).toString();
                String append = (String) requestParams.get("type");
                String type = "xls";
                type = append.contains("csv") ? "csv" : "xls";
                append = append.contains("detailed") ? "(Detail)" : "(Summary)";
                filename = filename + append;

                /*
                 Map to create entry for export log
                 */
                HashMap<String, Object> exportDetails = new HashMap<String, Object>();
                exportDetails.put("fileName", filename + "." + type);
                exportDetails.put("requestTime", requestTime);
                exportDetails.put("status", 1);
                exportDetails.put("companyId", (String) requestParams.get("companyid"));
                exportDetails.put("fileType", type);

                /*
                
                 Function to generate entry in export log
                 */
                KwlReturnObject resultExportObj = accProductObj.saveProductExportDetails(exportDetails);
                ProductExportDetail productExportDetail = (ProductExportDetail) resultExportObj.getEntityList().get(0);
                txnManager.commit(status);

                status = null;
                status = txnManager.getTransaction(def);
                String companyid = "";
                String gcurrencyid = (String) requestParams.get("gcurrencyid");
                String reporttype = (String) requestParams.get("cashtype");
                int templateflag = Integer.parseInt(requestParams.get("templateflag").toString());
                String[] companyids = (String[]) requestParams.get("companyids");

                /*
                 Export company wise data. 
                 Multiple companyid will not occur.
                
                 */
                for (int cnt = 0; cnt < companyids.length; cnt++) {
                    companyid = companyids[cnt];
                    requestParams.put(Constants.companyKey, companyid);
                    requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
                    KwlReturnObject result = null;
                    KwlReturnObject result1 = null;
                    KwlReturnObject result2 = null;

                    /*
                     Pass control to Fetch data from DB
                     */
                    if (!StringUtil.isNullOrEmpty(reporttype)) {    //ERP-41849
                        if (StringUtil.equal(JournalEntryConstants.CashReceiptJournal, reporttype)) {
                            requestParams.put(JournalEntryConstants.ReportClass, JournalEntryConstants.Receipt);
                            result1 = accJournalEntryobj.getJournalEntryForReports(requestParams);
                            requestParams.put(JournalEntryConstants.ReportClass, JournalEntryConstants.ASSET);
                            requestParams.put(JournalEntryConstants.ReportType, reporttype);
                            result2 = accJournalEntryobj.getJournalEntryForReports(requestParams);
                            List jeList1 = result1.getEntityList();
                            List jeList2 = result2.getEntityList();
                            List jeList3 = new ArrayList();
                            jeList3.addAll(jeList1);
                            jeList3.addAll(jeList2);
                            Collections.sort(jeList3, new JournalEntryExport.ExportJEComparator());
                            result = new KwlReturnObject(true, "", null, jeList3, result1.getRecordTotalCount() + result2.getRecordTotalCount());
                        } else if (StringUtil.equal(JournalEntryConstants.CashDisbursementJournal, reporttype)) {
                            requestParams.put(JournalEntryConstants.ReportClass, JournalEntryConstants.Payment);
                            result1 = accJournalEntryobj.getJournalEntryForReports(requestParams);
                            requestParams.put(JournalEntryConstants.ReportClass, JournalEntryConstants.ASSET);
                            requestParams.put(JournalEntryConstants.ReportType, reporttype);
                            result2 = accJournalEntryobj.getJournalEntryForReports(requestParams);
                            List jeList1 = result1.getEntityList();
                            List jeList2 = result2.getEntityList();
                            List jeList3 = new ArrayList();
                            jeList3.addAll(jeList1);
                            jeList3.addAll(jeList2);
                            Collections.sort(jeList3, new JournalEntryExport.ExportJEComparator());
                            result = new KwlReturnObject(true, "", null, jeList3, result1.getRecordTotalCount() + result2.getRecordTotalCount());
                        } else if (StringUtil.equal(JournalEntryConstants.SalesReceivableJournal, reporttype)) {
                            requestParams.put(JournalEntryConstants.ReportClass, JournalEntryConstants.Invoice);
                            result = accJournalEntryobj.getJournalEntryForReports(requestParams);
                        } else if (StringUtil.equal(JournalEntryConstants.PurchasePayableJournal, reporttype)) {
                            requestParams.put(JournalEntryConstants.ReportClass, JournalEntryConstants.GoodsReceipt);
                            result = accJournalEntryobj.getJournalEntryForReports(requestParams);
                        } else if (StringUtil.equal(JournalEntryConstants.PartyJournal, reporttype)) {
                            requestParams.put(CCConstants.isPartyJE, CCConstants.isPartyJE);
                            requestParams.put(Constants.Journal_Entry_Type, Constants.Party_Journal_Entry);     //ERP-41849
                            result = accJournalEntryobj.getJournalEntry(requestParams);
                        } else if (StringUtil.equal(JournalEntryConstants.fundTransfer, reporttype)) {      //ERP-41849
                            requestParams.put(Constants.Journal_Entry_Type,Constants.FundTransfer_Journal_Entry);   
                            result = accJournalEntryobj.getJournalEntry(requestParams);
                        } else if(StringUtil.equal(JournalEntryConstants.NormalJournal, reporttype)){       //ERP-41849
                            requestParams.put(Constants.Journal_Entry_Type, Constants.Normal_Journal_Entry);
                            result = accJournalEntryobj.getJournalEntry(requestParams);
                        }else {
                            result = accJournalEntryobj.getJournalEntry(requestParams);
                        }
                    } else {
                        result = accJournalEntryobj.getJournalEntry(requestParams);
                    }
                    JSONObject jobj = accFinancialReportsService.getJournalEntryJsonForExportMerged(requestParams, result.getEntityList(), DataJArr, templateflag);
                }

                JSONObject dataObj = new JSONObject();
                dataObj.put(Constants.RES_data, DataJArr);
                dataObj.put("filename", filename);
                String filePath = exportDaoObj.processRequest(requestJobj, dataObj);
                /*
                 Update Progress flag into log
                 */
                exportDetails.clear();
                exportDetails.put("id", productExportDetail.getId());
                exportDetails.put("status", 2);
                exportDetails.put("fileType", type);
                accProductObj.saveProductExportDetails(exportDetails);
                txnManager.commit(status);

            } catch (Exception ex) {
                Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
                txnManager.rollback(status);
            } finally {
                processQueue.remove(requestJobj);
                processQueue.remove(requestParams);
                isworking = false;
                System.out.println("Done.Journal Entry Report Exported Successfully.");
            }
        }
    }

    /*
     Sort JE records date wise
     */
    private class ExportJEComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {

            int result = 0;
            Object[] row1 = (Object[]) o1;
            JournalEntry entry1 = (JournalEntry) row1[0];
            Object[] row2 = (Object[]) o2;
            JournalEntry entry2 = (JournalEntry) row2[0];

            if (entry1.getEntryDate().getTime() > entry2.getEntryDate().getTime()) {
                result = 1;
            } else if (entry1.getEntryDate().getTime() < entry2.getEntryDate().getTime()) {
                result = -1;
            } else {
                result = 0;
            }

            return result;
        }
    }
}
