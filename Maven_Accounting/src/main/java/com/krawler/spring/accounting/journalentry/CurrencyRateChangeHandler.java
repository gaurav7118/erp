/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.journalentry;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.importFunctionality.ImportHandler;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author sagar
 */
public class CurrencyRateChangeHandler implements Runnable {
    String currencyid;
    String gcurrencyid;
    String companyid;
    Date startDate;
    Date endDate;
    double oldRate;
    double newRate;
//    boolean isEdit;
    private accJournalEntryDAO accJournalEntryobj;
    private HibernateTransactionManager txnManager;

    public String getCurrencyid() {
        return currencyid;
    }

    public void setCurrencyid(String currencyid) {
        this.currencyid = currencyid;
    }

    public String getGcurrencyid() {
        return gcurrencyid;
    }

    public void setGcurrencyid(String gcurrencyid) {
        this.gcurrencyid = gcurrencyid;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public double getNewRate() {
        return newRate;
    }

    public void setNewRate(double newRate) {
        this.newRate = newRate;
    }

    public double getOldRate() {
        return oldRate;
    }

    public void setOldRate(double oldRate) {
        this.oldRate = oldRate;
    }
    
    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }    

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    CurrencyRateChangeHandler(String gcurrencyid, String currencyid, Date startDate, Date endDate, double oldRate, double newRate, String companyid) {
        this.gcurrencyid = gcurrencyid;
        this.currencyid = currencyid;
        this.companyid = companyid;
        this.startDate = startDate;
        this.endDate = endDate;
        this.oldRate = oldRate;
        this.newRate = newRate;
    }

    CurrencyRateChangeHandler() {
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
//                    this.setGcurrencyid((String)requestParams.get("gcurrencyid"));
                    this.setCurrencyid((String)requestParams.get("currencyid"));
                    this.setCompanyid((String)requestParams.get("companyid"));
                    this.setStartDate((Date)requestParams.get("startDate"));
                    this.setEndDate((Date)requestParams.get("endDate"));
                    this.setOldRate((Double)requestParams.get("oldRate"));
                    this.setNewRate((Double)requestParams.get("newRate"));
//                    this.setIsEdit((Boolean)requestParams.get("isedit"));
                    updateJournalEntryOptimzedEntries();
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

    public synchronized void updateJournalEntryOptimzedEntries() {
        TransactionStatus status=null;
        try {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("import_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            status = txnManager.getTransaction(def);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            double oldamount = 0;
            double newamount = 0;
            
            KwlReturnObject abresult1 = accJournalEntryobj.getCurrencyJEs(currencyid, startDate, endDate);
            List list1 = abresult1.getEntityList();
            Iterator itr1 = list1.iterator();
            while (itr1.hasNext()) {
                Object[] row = (Object[]) itr1.next();
                JournalEntryDetail jed = (JournalEntryDetail) row[1];
                JournalEntry je = jed.getJournalEntry();
                double amount = jed.isDebit() ? jed.getAmount() : -jed.getAmount();
                String accountid = jed.getAccount().getID();
                
//                String jeid = je.getID();
                String entryDate = sdf.format(je.getEntryDate());
                String costCenterId = (je.getCostcenter() != null ? je.getCostcenter().getID() : "");
//                String fromcurrencyid = (je.getCurrency() == null ? gcurrencyid : je.getCurrency().getCurrencyID());
                oldamount = getCurrencyToBaseAmount(amount, oldRate);
                newamount = getCurrencyToBaseAmount(amount, newRate);

                if(oldamount!=0) {
                    //Revert back old amount from jedetail_optimzied table
                    accJournalEntryobj.saveAccountJEs_optimized(accountid, companyid, entryDate, costCenterId, -oldamount);
                }
                if(newamount!=0) {
                    //Add new amount in jedetail_optimzied table
                    accJournalEntryobj.saveAccountJEs_optimized(accountid, companyid, entryDate, costCenterId, newamount);
                }
            }
//            try {
                txnManager.commit(status);
//            } catch (Exception ex) {
//                txnManager.rollback(status);
//            }
        } catch (ServiceException ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            Logger.getLogger(CurrencyRateChangeHandler.class.getName()).log(Level.SEVERE, null, ex);
        }catch (Exception ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            Logger.getLogger(CurrencyRateChangeHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public double getCurrencyToBaseAmount(double Amount, double rate) throws ServiceException {        
        try{
            if (Amount != 0 && rate != 0) {
//                if (rate == 0) {
//                    KwlReturnObject result = getExcDetailID(request, currencyid, transactiondate, null);
//                    List li = result.getEntityList();
//                    if (!li.isEmpty()) {
//                        Iterator itr = li.iterator();
//                        ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
//                        rate = erd.getExchangeRate();
//                    }
//                }
                Amount = Amount / rate;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("CurrencyRateChangeHandler.getCurrencyToBaseAmount : "+ex.getMessage(), ex);
        } finally {
            return Amount;
        }
    }
}
