/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.account;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Templatepnl;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.utils.json.JSONArray;
import java.util.*;
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
public class AccountMapHandler implements Runnable {

    String templateid;
    String companyid;
//    boolean isEdit;
    private kwlCommonTablesDAO KwlCommonTablesDAOObj;
    private accJournalEntryDAO accJournalEntryobj;
    private HibernateTransactionManager txnManager;
    private accAccountDAO accAccountDAOobj;

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }

    public String getTemplateid() {
        return templateid;
    }

    public void setTemplateid(String templateid) {
        this.templateid = templateid;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setKwlCommonTablesDAO(kwlCommonTablesDAO KwlCommonTablesDAOObj) {
        this.KwlCommonTablesDAOObj = KwlCommonTablesDAOObj;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    AccountMapHandler(String templateid, String companyid) {
        this.templateid = templateid;
        this.companyid = companyid;
    }

    AccountMapHandler() {
    }
    
    boolean isWorking = false;
    ArrayList processQueue = new ArrayList();

    public void setIsWorking(boolean isWorking) {
        this.isWorking = isWorking;
    }

    public boolean isIsWorking() {
        return isWorking;
    }

//    public boolean isIsEdit() {
//        return isEdit;
//    }
//
//    public void setIsEdit(boolean isEdit) {
//        this.isEdit = isEdit;
//    }
    
    public void add(HashMap<String, Object> requestParams) {
        try {
            processQueue.add(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        try {
            while (!processQueue.isEmpty()) {
                HashMap<String, Object> requestParams = (HashMap<String, Object>) processQueue.get(0);
                try {
                    this.isWorking = true;
                    this.setTemplateid((String)requestParams.get("templateid"));
                    this.setCompanyid((String)requestParams.get("companyid"));
//                    this.setIsEdit((Boolean)requestParams.get("isedit"));
                    updateJournalEntryPerms();
                } catch(Exception ex){

                } finally {
                    processQueue.remove(requestParams);
                }
            }
        } catch(Exception ex){
        } finally{
            this.isWorking = false;
        }
        
    }

    public synchronized void updateJournalEntryPerms() {
        try {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("import_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            requestParams.put("templateid", templateid);
            KwlReturnObject result = accAccountDAOobj.getPnLTemplates(requestParams);
            List list = result.getEntityList();

            Iterator itr = list.iterator();
            //Fetched templates
            while (itr.hasNext()) {
                Templatepnl listObj = (Templatepnl) itr.next();
                int templatecode = listObj.getTemplateid();
                listObj.setStatus(2);
                int templateCode = 0;
                templateCode += (Math.pow(2, templatecode));
                //Fetched all JE to check for perms
                KwlReturnObject resultJE = accJournalEntryobj.getAllJournalEntry(companyid);
                List listJE = resultJE.getEntityList();

                Iterator itrJE = listJE.iterator();
                while (itrJE.hasNext()) {
                    JournalEntry jeObj = (JournalEntry) itrJE.next();
                    
                    //Following case will check if permission is already set and delete it accordingly
                    if((jeObj.getTemplatepermcode() != null) && ((jeObj.getTemplatepermcode() & templateCode) == templateCode)){
                        jeObj.setTemplatepermcode(jeObj.getTemplatepermcode() - templateCode);
                    }
                    
                    KwlReturnObject resultJED = accJournalEntryobj.getJournalEntryDetail(jeObj.getID(), companyid);
                    List listJED = resultJED.getEntityList();
                    

                    Iterator itrJED = listJED.iterator();
                    boolean isMapJE = true;

                    while (itrJED.hasNext()) {
                        JournalEntryDetail jedObj = (JournalEntryDetail) itrJED.next();
                        String accountid = jedObj.getAccount().getID();
                        HashMap<String, Object> requestParamsMap = new HashMap<String, Object>();

                        requestParamsMap.put("templateid", templateid);
                        requestParamsMap.put("companyid", companyid);
                        requestParamsMap.put("accountid", accountid);
                        KwlReturnObject resultExists = accAccountDAOobj.getAccountsFormappedPnL(requestParamsMap);
                        if (resultExists.getRecordTotalCount() == 0) {
                            isMapJE = false;
                            break;
                        }
                    }
                    if (isMapJE) {
                        Long finalCode = 0l;
                        
                        if (jeObj.getTemplatepermcode() != null) {
                            finalCode = jeObj.getTemplatepermcode() + templateCode;
                        } else {
                            finalCode += templateCode;
                        }

                        jeObj.setTemplatepermcode(finalCode);
                    }
                }
                listObj.setStatus(0);
            }
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
            
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccountMapHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
