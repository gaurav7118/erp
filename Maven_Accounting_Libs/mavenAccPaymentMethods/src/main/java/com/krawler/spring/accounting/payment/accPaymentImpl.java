/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.spring.accounting.payment;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLDateFormat;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class accPaymentImpl extends BaseDAO implements accPaymentDAO {
private MessageSource messageSource;

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    @Override
    public KwlReturnObject addPayDetail(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        try {
            PayDetail pdetail;
            if (hm.containsKey("paydetailid")) {
                String paydetailid = (String) hm.get("paydetailid");
                pdetail = (PayDetail) get(PayDetail.class, paydetailid);
            } else {
                pdetail = new PayDetail();
            }
            if (hm.containsKey("paymethodid")) {
                PaymentMethod paymentMethod = (hm.get("paymethodid") == null ? null : (PaymentMethod) get(PaymentMethod.class, (String) hm.get("paymethodid")));
                pdetail.setPaymentMethod(paymentMethod);
            }
            if (hm.containsKey("companyid")) {
                Company cmp = (hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid")));
                pdetail.setCompany(cmp);
            }
            if (hm.containsKey("chequeid")) {
                Cheque chq = (hm.get("chequeid") == null ? null : (Cheque) get(Cheque.class, (String) hm.get("chequeid")));
                pdetail.setCheque(chq);
            }
            if (hm.containsKey("cardid")) {
                Card card = (hm.get("cardid") == null ? null : (Card) get(Card.class, (String) hm.get("cardid")));
                pdetail.setCard(card);
            }
            saveOrUpdate(pdetail);
            list.add(pdetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.addPayDetail : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Pay Details has been added successfully", null, list, list.size());
    }
    @Override
    public KwlReturnObject savePayDetail(PayDetail payDetail) throws ServiceException {
        List list = new ArrayList();
        try {
            saveOrUpdate(payDetail);
            list.add(payDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.addPayDetail : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Pay Details has been added successfully", null, list, list.size());
    }
    @Override
    public PayDetail saveOrUpdatePayDetail(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        PayDetail pdetail;
        try {
            if (hm.containsKey("paydetailid")) {
                String paydetailid = (String) hm.get("paydetailid");
                pdetail = (PayDetail) get(PayDetail.class, paydetailid);
            } else {
                pdetail = new PayDetail();
            }
            if (hm.containsKey("paymethodid")) {
                PaymentMethod paymentMethod = (hm.get("paymethodid") == null ? null : (PaymentMethod) get(PaymentMethod.class, (String) hm.get("paymethodid")));
                pdetail.setPaymentMethod(paymentMethod);
            }
            if (hm.containsKey("companyid")) {
                Company cmp = (hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid")));
                pdetail.setCompany(cmp);
            }
            if (hm.containsKey("chequeid")) {
                Cheque chq = (hm.get("chequeid") == null ? null : (Cheque) get(Cheque.class, (String) hm.get("chequeid")));
                pdetail.setCheque(chq);
            }
            if (hm.containsKey("cardid")) {
                Card card = (hm.get("cardid") == null ? null : (Card) get(Card.class, (String) hm.get("cardid")));
                pdetail.setCard(card);
            }
            saveOrUpdate(pdetail);
            list.add(pdetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.addPayDetail : " + ex.getMessage(), ex);
        }
        return pdetail;
    }

    public KwlReturnObject updatePayDetail(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String paydetailid = (String) hm.get("paydetailid");
            PayDetail pdetail = (PayDetail) get(PayDetail.class, paydetailid);
            if (pdetail != null) {
                if (hm.containsKey("paymethodid")) {
                    PaymentMethod paymentMethod = (hm.get("paymethodid") == null ? null : (PaymentMethod) get(PaymentMethod.class, (String) hm.get("paymethodid")));
                    pdetail.setPaymentMethod(paymentMethod);
                }
                if (hm.containsKey("companyid")) {
                    Company cmp = (hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid")));
                    pdetail.setCompany(cmp);
                }
                if (hm.containsKey("chequeid")) {
                    Cheque chq = (hm.get("chequeid") == null ? null : (Cheque) get(Cheque.class, (String) hm.get("chequeid")));
                    pdetail.setCheque(chq);
                }
                if (hm.containsKey("cardid")) {
                    Card card = (hm.get("cardid") == null ? null : (Card) get(Card.class, (String) hm.get("cardid")));
                    pdetail.setCard(card);
                }
            }
            saveOrUpdate(pdetail);
            list.add(pdetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.updatePayDetail : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Pay Details has been updated successfully", null, list, list.size());
    }

    @Override
    public KwlReturnObject deletePayDetail(String padetailId, String companyid) throws ServiceException {
        String delQuery = "delete from PayDetail p where p.ID=? and p.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{padetailId, companyid});
        return new KwlReturnObject(true, "Pay Detail has been deleted successfully.", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject addCheque(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        try {
            Cheque cheque;
            if (hm.containsKey("chequeID") && hm.get("chequeID")!=null && hm.get("chequeID")!="") {
                String chequeID = (String) hm.get("chequeID");
                cheque = (Cheque) get(Cheque.class, chequeID);
            } else {
                cheque = new Cheque();
            }
            if (hm.containsKey("chequeno")) {
                cheque.setChequeNo((String) hm.get("chequeno"));
            }
            if (hm.containsKey("createdFrom")) {
                cheque.setCreatedFrom((Integer) hm.get("createdFrom"));
            }
            if (hm.containsKey("companyId")) {
                Company company = (Company) get(Company.class, (String) hm.get("companyId"));
                cheque.setCompany(company);
            }
            if (hm.containsKey("sequenceNumber") && hm.get("sequenceNumber")!=null) {
                BigInteger  sequenceNumber = new BigInteger(hm.get("sequenceNumber").toString());
                cheque.setSequenceNumber(sequenceNumber);
            }
            if (hm.containsKey(Constants.DATEPREFIX)) {
                cheque.setDatePreffixValue((String) hm.get(Constants.DATEPREFIX));
            }
             if (hm.containsKey(Constants.DATEAFTERPREFIX)) {
                cheque.setDateAfterPreffixValue((String)hm.get(Constants.DATEAFTERPREFIX));
            }
            if (hm.containsKey(Constants.DATESUFFIX)) {
                cheque.setDateSuffixValue((String) hm.get(Constants.DATESUFFIX));
            }
            if (hm.containsKey("isAutoGeneratedChequeNumber")) {
                cheque.setChequeNoAutoGenetated((Boolean) hm.get("isAutoGeneratedChequeNumber"));
            }
            if (hm.containsKey("bankAccount")) {
                Account bankAccount = (Account) get(Account.class, (String) hm.get("bankAccount"));
                cheque.setBankAccount(bankAccount);
            }
            if (hm.containsKey("description")) {
                cheque.setDescription((String) hm.get("description"));
            }
            if (hm.containsKey("bankname")) {
                cheque.setBankName((String) hm.get("bankname"));
            }
            if (hm.containsKey("duedate")) {
                cheque.setDueDate((Date) hm.get("duedate"));
            }
            if (hm.containsKey("bankmasteritemid")) {
                String bankid = (String) hm.get("bankmasteritemid");
                MasterItem bank = (MasterItem) get(MasterItem.class, bankid);
                cheque.setBankMasterItem(bank);
            }
            if (hm.containsKey(Constants.SEQFORMAT) && hm.get(Constants.SEQFORMAT)!=null) {
                cheque.setSeqformat((ChequeSequenceFormat) get(ChequeSequenceFormat.class, (String) hm.get(Constants.SEQFORMAT)));
            }
            saveOrUpdate(cheque);
            list.add(cheque);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.addCheque : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Cheque has been added successfully", null, list, list.size());
    }


    @Override
    public KwlReturnObject approvePendingMakePayment(String cnID, String companyid, int status) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(status);
        params.add(cnID);
        params.add(companyid);
        String query = "update Payment set approvestatuslevel = ? where ID=? and company.companyID=?";
        int numRows = executeUpdate(query, params.toArray());
        return new KwlReturnObject(true, "Payment has been updated successfully.", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject rejectPendingmakePayment(String cnid, String companyid) throws ServiceException{
         try {
            String query = "update Payment set deleted=true,approvestatuslevel = (-approvestatuslevel) where ID=? and company.companyID=?";
            int numRows = executeUpdate( query, new Object[]{cnid, companyid});
            return new KwlReturnObject(true, "Make Payment has been rejected successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.rejectPendingCreditNote : " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean isChequeSequenceNumberAvailable(HashMap hm) throws ServiceException {
        boolean isChequeNumberAvailable = false;

        String condition = "";
        ArrayList params = new ArrayList();
        if (hm.containsKey("companyId")) {
            String companyId = (String) hm.get("companyId");
            condition += " and ch.company=? ";
            params.add(companyId);
        }
        if (hm.containsKey("bankAccountId")) {
            String bankAccountId = (String) hm.get("bankAccountId");
            condition += " and ch.bankaccount=? ";
            params.add(bankAccountId);
        }
        if (hm.containsKey("sequenceNumber")) {
            BigInteger sequenceNumber = (BigInteger) hm.get("sequenceNumber");
            params.add(sequenceNumber);
            condition += " and ch.sequencenumber=? ";
        }
        
        if (hm.containsKey("sequenceformatid") && hm.get("sequenceformatid") != null && !StringUtil.isNullOrEmpty(hm.get("sequenceformatid").toString())) {
            String sequenceformatid = (String) hm.get("sequenceformatid");
            params.add(sequenceformatid);
            condition += " and ch.seqformat=? ";
        }

        String query = "select chequeno from cheque ch "
                + "WHERE (ch.createdfrom=1 or ch.createdfrom=3) and ch.deleteflag=false" + condition;

        List list = executeSQLQuery( query, params.toArray());

        if (!list.isEmpty()) {
            isChequeNumberAvailable = true;
        }

        return isChequeNumberAvailable;
    }

    @Override
    public KwlReturnObject deleteChequePermanently(String chequeid, String companyid) throws ServiceException {
        String delQuery = "delete from Cheque c where c.ID=? ";
        int numRows = executeUpdate( delQuery, new Object[]{chequeid});
        return new KwlReturnObject(true, "Cheque has been deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject deleteCheque(String chequeid, String companyid) throws ServiceException {
        String delQuery = "update cheque set deleteflag=true where id=? ";
        int numRows = executeSQLUpdate( delQuery, new Object[]{chequeid});
        return new KwlReturnObject(true, "Cheque has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject updateCheque(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String chequeid = (String) hm.get("chequeid");
            Cheque cheque = (Cheque) get(Cheque.class, chequeid);
            if (cheque != null) {
                if (hm.containsKey("chequeno")) {
                    cheque.setChequeNo((String) hm.get("chequeno"));
                }
                if (hm.containsKey("description")) {
                    cheque.setDescription((String) hm.get("description"));
                }
                if (hm.containsKey("bankname")) {
                    cheque.setBankName((String) hm.get("bankname"));
                }
            }
            saveOrUpdate(cheque);
            list.add(cheque);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.updateCheque : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Cheque has been updated successfully", null, list, list.size());
    }

    public KwlReturnObject addCard(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        try {
            Card card = new Card();
            if (hm.containsKey("cardno")) {
                card.setCardNo((String) hm.get("cardno"));
            }
            if (hm.containsKey("nameoncard")) {
                card.setCardHolder((String) hm.get("nameoncard"));
            }
            if (hm.containsKey("expirydate")) {
                card.setExpiryDate((Date) hm.get("expirydate"));
            }
            if (hm.containsKey("cardtype")) {
                card.setCardType((String) hm.get("cardtype"));
            }
            if (hm.containsKey("refno")) {
                card.setRefNo((String) hm.get("refno"));
            }
            saveOrUpdate(card);
            list.add(card);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.addCard : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Card has been added successfully", null, list, list.size());
    }

    public KwlReturnObject deleteCard(String cardid, String companyid) throws ServiceException {
        String delQuery = "delete from Card c where c.ID=? ";
        int numRows = executeUpdate( delQuery, new Object[]{cardid});
        return new KwlReturnObject(true, "Card has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject updateCard(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String cardid = (String) hm.get("cardid");
            Card card = (Card) get(Card.class, cardid);
            if (card != null) {
                if (hm.containsKey("cardno")) {
                    card.setCardNo((String) hm.get("cardno"));
                }
                if (hm.containsKey("nameoncard")) {
                    card.setCardHolder((String) hm.get("nameoncard"));
                }
                if (hm.containsKey("expirydate")) {
                    card.setExpiryDate((Date) hm.get("expirydate"));
                }
                if (hm.containsKey("cardtype")) {
                    card.setCardType((String) hm.get("cardtype"));
                }
                if (hm.containsKey("refno")) {
                    card.setRefNo((String) hm.get("refno"));
                }
                saveOrUpdate(card);
            }
            list.add(card);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.updateCard : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Card has been updated successfully", null, list, list.size());
    }

    public KwlReturnObject getPaymentMethodFromAccount(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from PaymentMethod pm where account.ID=? and pm.company.companyID=?";
        list = executeQuery( q, new Object[]{accountid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getPaymentAmountofBadDebtGoodsReceipt(String invoiceid, boolean isBeforeClaimed) throws ServiceException {
        List list = new ArrayList();
        String q = "select sum(amount) from PaymentDetail rd where rd.goodsReceipt.ID=? and rd.payment.linkedToClaimedInvoice=? group by rd.goodsReceipt";
        List l = executeQuery( q, new Object[]{invoiceid, isBeforeClaimed});
        double amount = (l.isEmpty() ? 0 : (Double) l.get(0));
        list.add(amount);
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getPaymentFromBadDebtClaimedInvoice(String invoiceid, boolean isBeforeClaimed, Date badDebtCalculationToDate) throws ServiceException {
        List list = new ArrayList();
        list.add(invoiceid);
        list.add(isBeforeClaimed);
        String addDate = "";
        if (badDebtCalculationToDate != null) {
            list.add(badDebtCalculationToDate);
            addDate = " and rd.payment.journalEntry.entryDate<=?";
        }
        String q = "from PaymentDetail rd where rd.goodsReceipt.ID=? and rd.payment.linkedToClaimedInvoice=? " + addDate;
        List l = executeQuery( q, list.toArray());
//        double amount = (l.isEmpty() ? 0 : (Double) l.get(0));
//        list.add(amount);
        return new KwlReturnObject(true, "", null, l, l.size());
    }
    
    @Override
    public KwlReturnObject getPaymentAmountFromGoodsReceipt(HashMap<String, Object> requestMap) throws ServiceException {
        
        List params = new ArrayList();
        
        params.add((String) requestMap.get("companyid"));
        
        String condition = "";
        if(requestMap.containsKey("invoiceid") && requestMap.get("invoiceid")!=null){
            condition+=" and rd.goodsReceipt.ID=? ";
            params.add((String) requestMap.get("invoiceid"));
        }
        
        if (requestMap.containsKey("startDate") && requestMap.get("startDate") != null && requestMap.containsKey("endDate") && requestMap.get("endDate") != null) {
//            condition += " and (rd.payment.journalEntry.entryDate>=? and rd.payment.journalEntry.entryDate<=?) ";
            condition += " and (rd.payment.creationDate>=? and rd.payment.creationDate<=?) ";
            params.add((Date) requestMap.get("startDate"));
            params.add((Date) requestMap.get("endDate"));
        }
        
        String groupByQuery = " group by rd.goodsReceipt";
                
        List list = new ArrayList();
        String q = "select sum(amount) from PaymentDetail rd where rd.payment.company.companyID=? "+condition+groupByQuery;
        List l = executeQuery( q, params.toArray());
        double amount = (l.isEmpty() ? 0 : (Double) l.get(0));
        list.add(amount);
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getPaymentDetailsLinkedWithGoodsReceipt(HashMap<String, Object> requestMap) throws ServiceException {
        
        List params = new ArrayList();
        
        params.add((String) requestMap.get("companyid"));
        
        String condition = "";
        if(requestMap.containsKey("invoiceid") && requestMap.get("invoiceid")!=null){
            condition+=" and rd.goodsReceipt.ID=? ";
            params.add((String) requestMap.get("invoiceid"));
        }
        
        if (requestMap.containsKey("startDate") && requestMap.get("startDate") != null && requestMap.containsKey("endDate") && requestMap.get("endDate") != null) {//All PaymentDetails between start date and end date 
//            condition += " and (rd.payment.journalEntry.entryDate>=? and rd.payment.journalEntry.entryDate<=?) ";
            condition += " and (rd.payment.creationDate>=? and rd.payment.creationDate<=?) ";
            params.add((Date) requestMap.get("startDate"));
            params.add((Date) requestMap.get("endDate"));
        } else if (requestMap.containsKey("endDate") && requestMap.get("endDate") != null) { //All PaymentDetails before end date  
//            condition += " and (rd.payment.journalEntry.entryDate <?) ";
            condition += " and (rd.payment.creationDate <?) ";
            params.add((Date) requestMap.get("endDate"));
        }
        
        String q = "from PaymentDetail rd where rd.payment.company.companyID=? and rd.payment.approvestatuslevel = 11 and rd.payment.deleted = 'F' "+condition;
        List list = executeQuery( q, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getChequeLayoutPaymentMethod(String bankid) throws ServiceException {
        List list = new ArrayList();
        String q = "select ID from ChequeLayout where paymentmethod.ID= ?";
        list = executeQuery( q, new Object[]{bankid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject addPaymentMethod(Map<String, Object> pmMap) throws ServiceException {
        List list = new ArrayList();
        try {
            PaymentMethod pm = new PaymentMethod();
            pm = buildPaymentMethod(pm, pmMap);
            save(pm);
            list.add(pm);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addPaymentMethod : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Payment Method has been added successfully", null, list, list.size());
    }

    public KwlReturnObject addChequeLayout(HashMap<String, Object> clMap) throws ServiceException {
        List list = new ArrayList();
        Locale locale =null;
        if(clMap.containsKey("locale")){
         locale = (Locale)clMap.get("locale");
        }
        try {
            ChequeLayout cl = new ChequeLayout();
            cl = buildChequeLayout(cl, clMap);
            save(cl);
            list.add(cl);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addChequeLayout : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, messageSource.getMessage("acc.checklayoutsetup.ChequeLayouthasbeenaddedsuccessfully", null, locale), null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getInvoiceInTemp(String document, String companyId,int moduleId) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String query = "select document from invoiceinused where document=? and company=? and moduleid=? ";
        list = executeSQLQuery( query, new Object[]{document, companyId,moduleId});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject insertInvoiceOrCheque(String document, String companyId,int moduleId,String bankId) throws ServiceException {
        List list = new ArrayList();

        try {
//            String uuid = UUID.randomUUID().toString();
            String query = "insert into invoiceinused (document,company,moduleid,bankid) values(?,?,?,?)";
            executeSQLUpdate( query, new Object[]{document, companyId,moduleId,bankId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.insertInvoice:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getSearchChequeNoTemp(String document, String companyid,int moduleId,String bankId) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String query = "select document from invoiceinused where document=? and company=? and moduleid=? and bankid=?";
        list = executeSQLQuery( query, new Object[]{document, companyid,moduleId,bankId});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    
    @Override
    public KwlReturnObject deleteUsedInvoiceOrCheque(String document, String companyId) throws ServiceException {
        List list = new ArrayList();

        try {
            String query = "delete from invoiceinused where document=? and company=? ";
            executeSQLUpdate( query, new Object[]{document, companyId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.deleteUsedInvoice:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
//    @Override
//    public KwlReturnObject deleteUsedCheque(String document, String companyId) throws ServiceException {
//        List list = new ArrayList();
//
//        try {
//            String uuid = UUID.randomUUID().toString();
//            String query = "delete from document where document=? and company=? ";
//            executeSQLUpdate( query, new Object[]{document, companyId});
//        } catch (Exception ex) {
//            throw ServiceException.FAILURE("accPaymentImpl.deleteUsedInvoice:" + ex.getMessage(), ex);
//        }
//        return new KwlReturnObject(true, "", null, list, list.size());
//    }
    
    public KwlReturnObject updateChequeLayout(HashMap<String, Object> clMap) throws ServiceException {
        List list = new ArrayList();
        Locale locale =null;
         if(clMap.containsKey("locale")){
         locale = (Locale)clMap.get("locale");
        }
        try {
            ChequeLayout cl= (ChequeLayout) get(ChequeLayout.class, (String) clMap.get("id"));
            cl = buildChequeLayout(cl, clMap);
            saveOrUpdate(cl);
            list.add(cl);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateChequeLayout : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, messageSource.getMessage("acc.checklayout.update", null, locale), null, list, list.size());
    }

    public ChequeLayout buildChequeLayout(ChequeLayout cl, HashMap<String, Object> clMap) {
//        if (clMap.containsKey("id")) {
//            cl.setID((String) clMap.get("id"));
//        }
        if (clMap.containsKey("coordinates")) {
            cl.setCoordinateinfo((String) clMap.get("coordinates"));
        }
        if (clMap.containsKey("bankid")) {
            PaymentMethod paymentMethod = (clMap.get("bankid") == null ? null : (PaymentMethod) get(PaymentMethod.class, (String) clMap.get("bankid")));
            cl.setPaymentmethod(paymentMethod);
        }
        if (clMap.containsKey("dateformat") && clMap.get("dateformat") != null) {
            String dateid = clMap.get("dateformat").toString();
            cl.setDateFormat((KWLDateFormat) load(KWLDateFormat.class, dateid));
        }
        if (clMap.containsKey("appendcharacter")) {
            cl.setAppendcharacter((String)clMap.get("appendcharacter"));
        }
        if (clMap.containsKey("isnewlayout")) {
            cl.setIsnewlayout((Boolean)clMap.get("isnewlayout"));
        }
        if (clMap.containsKey("activateExtraFields")) {
            cl.setActivateExtraFields((Boolean)clMap.get("activateExtraFields"));
        }
        if (clMap.containsKey("addCharacterInCheckDate")) {
            cl.setAddCharacterInCheckDate((Boolean)clMap.get("addCharacterInCheckDate"));
        }
        return cl;
    }

    public KwlReturnObject getChequeLayout(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from ChequeLayout ";

        if (filterParams.containsKey("bankid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "paymentmethod.ID=?";
            params.add((String) filterParams.get("bankid"));
        }
        query += condition;
//        query="from PaymentMethod where company.companyID=?";
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject updatePaymentMethod(Map<String, Object> pmMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String methodid = (String) pmMap.get("methodid");
            PaymentMethod pm = (PaymentMethod) get(PaymentMethod.class, methodid);
            if (pm != null) {
                pm = buildPaymentMethod(pm, pmMap);
                saveOrUpdate(pm);
            }
            list.add(pm);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updatePaymentMethod : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Payment Method has been updated successfully", null, list, list.size());
    }

    public PaymentMethod buildPaymentMethod(PaymentMethod pm, Map<String, Object> pmMap) {
        if (pmMap.containsKey("srno") && pmMap.get("srno") != null) {
            pm.setSrno((Integer) pmMap.get("srno"));
        }
        if (pmMap.containsKey("methodname")) {
            pm.setMethodName((String) pmMap.get("methodname"));
        }
        if (pmMap.containsKey("detailtype")) {
            pm.setDetailType((Integer) pmMap.get("detailtype"));
        }
        if (pmMap.containsKey("autopopulate")) {
            pm.setAutoPopulate((Boolean) pmMap.get("autopopulate"));
        }
        if (pmMap.containsKey("autopopulateincpcs")) {
            pm.setAutoPopulateInCPCS((Boolean) pmMap.get("autopopulateincpcs"));
        }
        if (pmMap.containsKey("autopopulateinloan")) {
            pm.setAutoPopulateInLoan((Boolean) pmMap.get("autopopulateinloan"));
        }
        if (pmMap.containsKey("autoPopulateInIBGGeneration") && pmMap.get("autoPopulateInIBGGeneration") != null) {
            pm.setAutoPopulateInIBGGeneration((Boolean) pmMap.get("autoPopulateInIBGGeneration"));
        }
        if (pmMap.containsKey("accountid")) {
            Account account = (pmMap.get("accountid") == null ? null : (Account) get(Account.class, (String) pmMap.get("accountid")));
            pm.setAccount(account);
            if (account != null) {
                String usedin = account.getUsedIn();
                account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Payment_Method));
            }
        }
        if (pmMap.containsKey("companyid")) {
            Company cmp = (pmMap.get("companyid") == null ? null : (Company) get(Company.class, (String) pmMap.get("companyid")));
            pm.setCompany(cmp);
        }
        return pm;
    }

    public KwlReturnObject deletePaymentMethod(String methodid, String companyid) throws ServiceException {
        String delQuery = "delete from PaymentMethod p where p.ID=? and p.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{methodid, companyid});
        return new KwlReturnObject(true, "Payment Method has been deleted successfully.", null, null, numRows);
    }
    /*
     * Search uom which is used for any product.
     */
    public KwlReturnObject searchPaymentMethod(String paymentMethod, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String searchQuery = "select id from  paydetail where company=? and paymentMethod=?";
        list = executeSQLQuery(searchQuery, new Object[]{companyid, paymentMethod});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject updateCnAmount(String noteid, double amount) throws ServiceException {
        String delQuery = "update CreditNote set cnamountdue=(cnamountdue-?) where ID=?";
        int numRows = executeUpdate( delQuery, new Object[]{amount, noteid});
        return new KwlReturnObject(true, "Payment Method has been updated successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject updateCnOpeningAmountDue(String noteid, double amount) throws ServiceException {
        String query = "update CreditNote set openingBalanceAmountDue=(openingBalanceAmountDue-?) where ID=?";
        int numRows = executeUpdate( query, new Object[]{amount, noteid});
        return new KwlReturnObject(true, "Amount has benn updated successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject updateCnOpeningBaseAmountDue(String noteid, double  amount) throws ServiceException{
        String query = "update CreditNote set openingbalancebaseamountdue=(openingbalancebaseamountdue-?) where ID=?";
        int numRows = executeUpdate( query, new Object[]{amount, noteid});
        return new KwlReturnObject(true, "Amount has been updated successfully.", null, null, numRows);
    }
    @Override
    public KwlReturnObject getPayMtdMappedToCustomer(HashMap<String, Object> requestparams) throws ServiceException{
        List list = new ArrayList<String>();
        ArrayList params = new ArrayList();
        String companyid = "";
        String Query = "";
        if (requestparams.containsKey("companyid")) {
            companyid= StringUtil.isNullOrEmpty(requestparams.get("companyid").toString())?"":requestparams.get("companyid").toString();
            params.add(companyid);
        }
        Query = "select distinct (pm.id) from paymentmethod  pm inner join customer c on pm.id=c.paymentmethod where c.company= ?";
        try {
            list = executeSQLQuery(Query, params.toArray());
        } catch (Exception ex) {
             throw ServiceException.FAILURE("MappedPaymentMethod :" + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getPaymentMethod(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from PaymentMethod ";

        if (filterParams.containsKey("companyid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        
        if (filterParams.containsKey("accountid") && filterParams.get("accountid") != null && filterParams.containsKey("isforEclaim") && filterParams.get("isforEclaim") != null
                && ((Boolean)filterParams.get("isforEclaim"))==true) {
               String selectedAccountIds = AccountingManager.getFilterInString((String)filterParams.get("accountid"));
               condition += (condition.length() == 0 ? " where " : " and ") + "account.ID in " + selectedAccountIds  ;
        } else {
            if (filterParams.containsKey("accountid") && filterParams.get("accountid") != null) {
                condition += (condition.length() == 0 ? " where " : " and ") + "account.ID=?";
                params.add(filterParams.get("accountid"));
            }
        }
        
        if (filterParams.containsKey("paymentAccountType")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "detailType=?";
            params.add(Integer.parseInt((String) filterParams.get("paymentAccountType")));
        }
        if(filterParams.containsKey("populateincpcs")){
            condition += (condition.length() == 0 ? " where " : " and ") + "autoPopulateInCPCS=?";
            params.add(filterParams.get("populateincpcs"));
        }
        if(filterParams.containsKey("loanFlag")){
            condition += (condition.length() == 0 ? " where " : " and ") + "autoPopulateInLoan=?";
            params.add(filterParams.get("loanFlag"));
        }
        if (filterParams.containsKey("methodName")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "methodName=?";
            params.add(filterParams.get("methodName"));
        }
        /*
         * Parameters for filtering the IBG related payment methods
        */
        if(filterParams.containsKey("onlyIBGAccounts")){
            condition += (condition.length() == 0 ? " where ": " and ") + " account.IBGBank = ?";
            params.add(filterParams.get("onlyIBGAccounts"));
        }
        if(filterParams.containsKey("IBGBankType")){
            condition += (condition.length() == 0 ? " where ": " and ") + " account.ibgBankType = ?";
            params.add(filterParams.get("IBGBankType"));
        }
        if(filterParams.containsKey("populateInIBGGeneration") && filterParams.get("populateInIBGGeneration") != null){
            condition += (condition.length() == 0 ? " where ": " and ") + " autoPopulateInIBGGeneration = ?";
            params.add(filterParams.get("populateInIBGGeneration"));
        }
        query += condition+ " order by srno";
//        query="from PaymentMethod where company.companyID=?";
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

     //@Transactional(propagation = Propagation.REQUIRED)
    public KwlReturnObject copyPaymentMethods(String companyid, HashMap hm) throws ServiceException {
        List returnList = new ArrayList();
        try {
            String query = "from DefaultPaymentMethod";
            HashMap paymentHM = new HashMap();
            List list = executeQuery( query);
            Iterator iter = list.iterator();
            Company company = (Company) get(Company.class, companyid);
            while (iter.hasNext()) {
                DefaultPaymentMethod defaultPM = (DefaultPaymentMethod) iter.next();
                if(!hm.containsKey(defaultPM.getAccount().getID())){
                    continue;
                }
                PaymentMethod pm = new PaymentMethod();
                /**
                 * Check If PaymentMethod is present or not if Present then update same PaymentMethod
                 * Ticket - ERP-35391
                 */
                HashMap<String, Object> pymtParams = new HashMap<String, Object>();
                pymtParams.put(Constants.companyKey, companyid);
                pymtParams.put("methodName", defaultPM.getMethodName());
                KwlReturnObject resultList = getPaymentMethod(pymtParams);
                if (resultList != null && resultList.getEntityList()!=null && !resultList.getEntityList().isEmpty()) {
                    pm = (PaymentMethod) resultList.getEntityList().get(0);
                }
                pm.setCompany(company);
                pm.setDetailType(defaultPM.getDetailType());
                pm.setMethodName(defaultPM.getMethodName());
                pm.setAutoPopulate(true);
                pm.setAutoPopulateInCPCS(true);
                pm.setAutoPopulateInLoan(true);
                String accountid = (String)hm.get(defaultPM.getAccount().getID());
                Account account = (Account)get(Account.class, accountid);
//                Account account = (Account) hm.get(defaultPM.getAccount());
                pm.setAccount(account);
                if (account != null) {
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Payment_Method));
                }else{
                    continue;
                }
                save(pm);
                if(!StringUtil.isNullOrEmpty(account.getDefaultaccountID())){
                    paymentHM.put(account.getDefaultaccountID(), pm);
                }
            }
            returnList.add(paymentHM);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("copyPaymentMethods : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    public void copyIndiaComplianceData(String companyid, HashMap hmPaymentMethod) throws ServiceException {
        try {
            if (!hmPaymentMethod.isEmpty()) {

                String query = "from DefaultChequeLayout";
                List list = null;
                list = executeQuery(query);

                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    String chequeLayoutId = UUID.randomUUID().toString();
                    DefaultChequeLayout defaultchequelayout = (DefaultChequeLayout) iter.next();
                    ChequeLayout chequelayout = new ChequeLayout();
                    chequelayout.setID(chequeLayoutId);
                    PaymentMethod paymentmethod = (PaymentMethod) hmPaymentMethod.get(defaultchequelayout.getDefaultaccount().getID());
                    chequelayout.setPaymentmethod(paymentmethod);
                    chequelayout.setCoordinateinfo(defaultchequelayout.getDefaultcoordinateinfo());
                    save(chequelayout);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("copyIndiaComplianceData : " + ex.getMessage(), ex);
        }
    }

    public KwlReturnObject updateDnAmount(String noteid, double amount) throws ServiceException {
        String delQuery = "update DebitNote set dnamountdue=(dnamountdue-?) where ID=?";
        int numRows = executeUpdate( delQuery, new Object[]{amount, noteid});
        return new KwlReturnObject(true, "Payment Method has been deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject updateDnOpeningAmountDue(String noteid, double amount) throws ServiceException {
        String query = "update DebitNote set openingBalanceAmountDue=(openingBalanceAmountDue-?) where ID=?";
        int numRows = executeUpdate( query, new Object[]{amount, noteid});
        return new KwlReturnObject(true, "Amount Due has been updated successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject getTransactionCountForPayment(String id, String companyid) throws ServiceException {
        List list = new ArrayList();
        String query = "from PayDetail pd where pd.paymentMethod.ID=?  and pd.company.companyID=? ";
        list = executeQuery( query, new Object[]{id, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
        public KwlReturnObject updateChequePrint(String paymentid,String companyid ) throws ServiceException {        
        String query = "update Payment set chequeprinted=true where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{paymentid, companyid});
        return new KwlReturnObject(true, "Amount Due has been updated successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject getDefaultPaymentMethod(String companyid,String paymentMethodAccountName, String paymentMethodName,int PaymentMethodDetailType) throws ServiceException{
        List list = new ArrayList();
        String query = "from PaymentMethod p where p.company.companyID= ? and p.account.name=? and p.methodName=? and p.detailType=? ";
        list= executeQuery(query,new Object[]{companyid,paymentMethodAccountName,paymentMethodName,PaymentMethodDetailType});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getCreditNotePaymentDetails(HashMap<String, Object> reqMap) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        try {
            DateFormat df = (DateFormat) reqMap.get(Constants.df);
            String condition = "";
            if (reqMap.containsKey("creditnoteid")) {
                condition += " WHERE cnp.cnid=? ";
                params.add((String) reqMap.get("creditnoteid"));
            }
            if (reqMap.containsKey("asofdate") && reqMap.get("asofdate") != null) {
//                condition += condition.isEmpty() ? " WHERE journalentry.entrydate<=? " : " AND journalentry.entrydate<=? ";
                condition += condition.isEmpty() ? " WHERE payment.creationDate<=? " : " AND payment.creationDate<=? ";
                String asOfDate = (String) reqMap.get("asofdate");
                params.add(df.parse(asOfDate));
            }
            String query = "SELECT cnp.exchangeratefortransaction,cnp.paidamountinpaymentcurrency,cnp.amountpaid FROM creditnotpayment AS cnp "
                    + " INNER JOIN payment ON payment.id = cnp.paymentid "
                    + " INNER JOIN journalentry ON journalentry.id=payment.journalentry "
                    + condition;
            list = executeSQLQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.getrDnPaymentDetails:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getPaymentsForCustomer(String personId, String companyId) throws ServiceException{
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(personId);
        params.add(companyId);
        String query="";
        query= "from Payment p where  p.customer = ? and p.company.companyID = ? and p.deleted = false ";
        list = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getRepeatedPaymentChequeDetailsForPaymentMethod(HashMap hm) throws ServiceException{
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        String bankAccountId = hm.containsKey("bankAccountId")?(String)hm.get("bankAccountId") :"";
        String chequeNumber = hm.containsKey("chequeNumber")?(String)hm.get("chequeNumber"):"";
        params.add(bankAccountId);
        String query = "select RPCD.id, P.paymentnumber, RPCD.chequenumber, RPCD.repeatedpaymentid from repeatedpaymentchequedetail RPCD inner join repeatedpayment RP on RP.id = RPCD.repeatedpaymentid "
                + "inner join payment P on P.repeatpayment = RP.id "
                + "inner join paydetail PD on PD.id = P.paydetail "
                + "inner join paymentmethod PM on PM.id = PD.paymentMethod "
                + "inner join account A on A.id = PM.account "
                + "where A.id = ? and RPCD.chequenumber in ( "+chequeNumber+" )";
        list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getPaymentMadeForJE(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        try {
            params.add(requestParams.get("companyid"));
//            String hqlQuery = "select payment,payment.journalEntry.ID from Payment payment where payment.company.companyID=? and payment.journalEntry is not null";
            String hqlQuery = "select id,journalentry,journalentryforbankcharges,journalentryforbankinterest from payment where company=? and journalentry is not null";
//            list = list = executeQuery( hqlQuery, params.toArray());
            list = list = executeSQLQuery( hqlQuery, params.toArray());
        } catch (Exception ex) {
//            System.out.println("Exception in getPaymentMadeForJE: " + ex.getMessage());
        }
         return new KwlReturnObject(true, "", null, list, list.size());
    }
    //
    public KwlReturnObject updateDisHonouredJEFromPayment(String paymentid,String company) throws ServiceException {
        List list = new ArrayList();
        String query = "update Payment set disHonouredChequeJe=null where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{paymentid,company});
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject searchPaymentMethodInFundTransferJE(String methodid, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String searchQuery = "select id from journalentry where company=? and paymentmethod=?";
        list = executeSQLQuery(searchQuery, new Object[]{companyid, methodid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getChequeSequenceFormatFromAccount(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from ChequeSequenceFormat chq where bankAccount.ID=? and chq.company.companyID=?";
        list = executeQuery(q, new Object[]{accountid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    /**
     *
     * @param requestParams
     * @return KwlReturnObject of getUOBBankDetails 
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getUOBBankDetails(Map<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String condition = "";
            ArrayList paramList = new ArrayList();
            if (requestParams.containsKey(Constants.companyKey) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.companyKey))) {
                paramList.add(requestParams.get(Constants.companyKey));
            }
            if (requestParams.containsKey(Constants.Acc_Accountid) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Accountid))) {
                condition += " and uob.account.ID = ? ";
                paramList.add(requestParams.get(Constants.Acc_Accountid));
            }
            String query = " from UOBBankDetails uob where uob.company.companyID = ? " + condition;
            list = executeQuery(query, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accReceiptImpl.getUOBBankDetails:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
          
     /*
     * Get CURRENCY as per currency Name
     * 
     */    
    @Override
    public KwlReturnObject getCurrency(String currencyName) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = " from KWLCurrency where name=?";
        list = executeQuery( q, new Object[]{currencyName});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }  
    
     /*
     * Get ACCOUNT NAME as per accountName
     * Additional check - get Non deleted accounts only.
     */        
    @Override
    public KwlReturnObject getAccountNameCount(String accountName, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "from Account where name=? and company.companyID=? and deleted='F'";
        list = executeQuery( q, new Object[]{accountName, companyid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject getRefundNameCount(String refundNo, String companyid, String customerID) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "SELECT id, receipt.currency.currencyCode, amount,amountDue, receipt.creationDate FROM ReceiptAdvanceDetail WHERE receipt.receiptNumber=? AND receipt.company.companyID =? AND receipt.customer.ID =? AND amountDue > 0 AND receipt.approvestatuslevel=11 and receipt.deleted=false";
        list = executeQuery(q, new Object[]{refundNo, companyid, customerID});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    /*
     * Get PAYMENT METHOD as per method Name
     * 
     */    
    @Override
    public KwlReturnObject getPaymentMethodCount(String paymentMethodStr, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "from PaymentMethod where methodName=? and company.companyID=?";
        list = executeQuery( q, new Object[]{paymentMethodStr,companyid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    public KwlReturnObject getPaymentFromBillNo(String billno, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from Payment where paymentNumber=? and company.companyID=?";
        list = executeQuery( q, new Object[]{billno, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }  

    public List getSalesPaymentKnockOffTransactions(Map<String, Object> requestParams) throws ServiceException{
        List ll = null;
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String customerid = (String) requestParams.get("custVendorID");
            boolean isAgedDetailsReport = requestParams.containsKey("isAgedDetailsReport") ? (Boolean)requestParams.get("isAgedDetailsReport"): false;
            boolean isSalesPersonAgedReport = requestParams.containsKey("isSalesPersonAgedReport") ? (Boolean)requestParams.get("isSalesPersonAgedReport"): false;
            
            int datefilter = requestParams.containsKey("datefilter") && requestParams.get("datefilter") != null ? Integer.parseInt(requestParams.get("datefilter").toString()) : 0;
            String custQuery = "";
            boolean includeExcludeChildCmb=false;
            if (requestParams.containsKey("includeExcludeChildCmb") && requestParams.get("includeExcludeChildCmb") != null) {
                includeExcludeChildCmb = (Boolean) requestParams.get("includeExcludeChildCmb");
            }
            if (!StringUtil.isNullOrEmpty(customerid) && !customerid.equals("All")) {
                String[] customers = customerid.split(",");
                StringBuilder custValues = new StringBuilder();
                for (String customer : customers) {
                    custValues.append("'").append(customer).append("',");
                }
                String custStr = custValues.substring(0, custValues.lastIndexOf(","));
                if (includeExcludeChildCmb) {
                    custQuery += " and (cust.id IN (" + custStr + ") or cust.parent IN (" + custStr + "))";
                } else {
                    custQuery += " and cust.id IN (" + custStr + ")";
                }
            }else if(!includeExcludeChildCmb){
                custQuery += " and cust.parent is  null";
            }
            DateFormat origdf = authHandler.getDateOnlyFormat();
            String duedateStr = (String)requestParams.get("enddate");
            String asofdateStr= (requestParams.containsKey("asofdate") && requestParams.get("asofdate") != null) ? (String) requestParams.get("asofdate") : duedateStr;
            Date duedate = origdf.parse(duedateStr);
            Date asofdate = origdf.parse(asofdateStr);
            DateFormat mysqldf = new SimpleDateFormat("yyyy-MM-dd");
            duedateStr = mysqldf.format(duedate);
            ArrayList paramsAdvSearch = new ArrayList();
            ArrayList paramsAdvSearch1= new ArrayList();
            requestParams.remove("isOpeningBalancePayment");
            JSONObject advSearchQueryObj = getAdvanceSearchForCustomQuery(requestParams, paramsAdvSearch1, paramsAdvSearch, "");            
            String jeid = " jedetail.id = adv.totaljedid";
            if(advSearchQueryObj.has("jeid") && !StringUtil.isNullOrEmpty(advSearchQueryObj.getString("jeid"))){
                jeid= advSearchQueryObj.getString("jeid");
            }
            String joinString1 = advSearchQueryObj.getString("joinString1");
            String mySearchFilterString = custQuery +advSearchQueryObj.getString("mySearchFilterString");
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(duedate);
            params.addAll(paramsAdvSearch);
            
            params.add(asofdate);
            params.add(companyid);
            params.add(duedate);
            
            String conditionSQL="";
            if (requestParams.containsKey("groupcombo") && requestParams.get("groupcombo") != null && requestParams.containsKey(Constants.globalCurrencyKey) && requestParams.get(Constants.globalCurrencyKey) != null) {
                int groupcombo = (Integer) requestParams.get("groupcombo");
                if (groupcombo == Constants.AgedPayableBaseCurrency) {
                    conditionSQL += " where pt.doccurrency=" + Integer.parseInt((String) requestParams.get(Constants.globalCurrencyKey));
                } else if (groupcombo == Constants.AgedPayableOtherthanBaseCurrency) {
                    conditionSQL += " where pt.doccurrency!=" + Integer.parseInt((String) requestParams.get(Constants.globalCurrencyKey));
                }
            }
            //global search
            String ss = (requestParams.containsKey("ss") && requestParams.get("ss") != null) ? (String) requestParams.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                try {
                    String[] searchcol = new String[]{"pt.customername","pt.custaliasname","pt.custcode", "pt.docnumber", "pt.accountname"};
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 5); 
                    StringUtil.insertParamSearchString(map);
                    String queryStart = "and";
                    if(StringUtil.isNullOrEmpty(conditionSQL)){
                        queryStart = "where";
                    }
                    String searchQuery = StringUtil.getSearchString(ss, queryStart, searchcol);
                    conditionSQL += searchQuery + " AND pt.custcode IS NOT NULL ";
                } catch (SQLException ex) {
                    Logger.getLogger(accPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            String sql = "select pt.docid, pt.docnumber, SUM(pt.amount), SUM(pt.amountinbase), SUM(pt.koamt), SUM(pt.koamtbase), pt.doctype, pt.docterm, pt.creationdate, pt.duedate, pt.salespersonname, pt.salespersoncode, pt.salespersonid, pt.entryno, pt.entrydate, pt.isOpeningBalanceTransaction, pt.customerid, pt.customername, pt.custaliasname, pt.custcode, pt.customeptermname, pt.customeptermid, pt.customercreditlimit, pt.memo, pt.exchangerate,pt.doccurrency, pt.doccurrencyname, pt.doccurrencycode, pt.doccurrencysymbol,pt.companyname,pt.shipdate, pt.basecurrencysymbol, pt.accountname from (\n"
                    + "SELECT  payment.id as docid, payment.paymentnumber as docnumber, jedetail.amount as amount, jedetail.amountinbase,0 as koamtbase,0 as koamt, 'Receipt' as doctype, ' ' as docterm, payment.creationdate, payment.creationdate as duedate, ' ' as salespersonname, ' ' as salespersoncode, ' ' as salespersonid, je.entryno, je.entrydate, '0' as isOpeningBalanceTransaction, cust.id as customerid, cust.name as customername, cust.aliasname as custaliasname, cust.acccode as custcode, custcredit.termname as customeptermname, custcredit.termid as customeptermid, cust.creditlimit as customercreditlimit, payment.memo,if(je.externalcurrencyrate=0,exchangerate_calc(payment.company,payment.creationdate,payment.currency,company.currency),je.externalcurrencyrate) as exchangerate,payment.currency as doccurrency, ptcurr.name as doccurrencyname, ptcurr.currencycode as doccurrencycode, ptcurr.symbol as doccurrencysymbol,company.companyname, ' ' as shipdate, compcurr.symbol as basecurrencysymbol, account.name as accountname\n"
                    + "from payment \n"
                    + "INNER JOIN advancedetail adv on payment.id=adv.payment and adv.receiptadvancedetail is null\n"
                    + "INNER JOIN journalentry je ON payment.journalentry=je.id \n"
                    + "inner join jedetail on "+jeid+"\n"
                    + "INNER JOIN customer cust ON cust.id=payment.customer \n"
                    + "LEFT JOIN account ON account.id=payment.account \n"
                    + "inner join creditterm custcredit on cust.creditterm = custcredit.termid\n"
                    + "INNER JOIN company on payment.company = company.companyid \n"
                    + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                    + "INNER JOIN currency ptcurr on payment.currency = ptcurr.currencyid \n"
                    +joinString1
                    + "where payment.company = ? and payment.creationdate <= ? and payment.isopeningbalencepayment=0 and payment.deleteflag='F' and payment.contraentry='F' and payment.isdishonouredcheque='F' and payment.paymentwindowtype = '2' and payment.approvestatuslevel = '11'  \n"
                    + mySearchFilterString
                    + " \n"
                    + "group by payment.paymentnumber \n"
                    + " UNION \n"
                    + "SELECT  payment.id as docid, payment.paymentnumber as docnumber, 0 as amount, 0 as amountinbase,SUM(ifnull(lp.amountinpaymentcurrency,0)/COALESCE(if(je.externalcurrencyrate =0, exchangerate_calc(payment.company,payment.creationdate,payment.currency,company.currency), je.externalcurrencyrate ),1)) as koamtbase,SUM(ifnull(lp.amountinpaymentcurrency,0)) as koamt, null as doctype, null as docterm, null as creationdate, null as duedate, null as salespersonname, null as salespersoncode, null as salespersonid, null as entryno, null as entrydate, null as isOpeningBalanceTransaction, null as customerid, null as customername, null as custaliasname, null as custcode, null as customertermname, null as customertermid, null as customercreditlimit, null as memo,null as exchangerate,null as doccurrency, null as doccurrencyname, null as doccurrencycode, null as doccurrencysymbol,null as companyname, null as shipdate, null as basecurrencysymbol, null as accountname\n"
                    + "from payment \n"
                    + "INNER JOIN journalentry je ON payment.journalentry=je.id \n"
                    + "INNER JOIN customer cust ON cust.id=payment.customer \n"
                    + "INNER JOIN company on payment.company = company.companyid \n"
                    + "INNER join linkdetailpaymenttoadvancepayment lp on lp.payment=payment.id and lp.paymentlinkdate <= ? and lp.company=payment.company \n"
                    + "where payment.company = ? and payment.creationdate <= ? and payment.isopeningbalencepayment=0 and payment.deleteflag='F' and payment.contraentry='F' and payment.isdishonouredcheque='F' and payment.paymentwindowtype = '2' and payment.approvestatuslevel = '11'  \n"
                    + " \n"
                    + "group by payment.paymentnumber \n"
                    + ") pt " + conditionSQL +" group by pt.docnumber order by pt.creationdate desc";
            
            ll = executeSQLQuery(sql, params.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("error", ex);
        }
        return ll;
    }
    
    /**
     * This method is used to update the amount due of refund receipt which user
     * links to advance payment from receipt report.
     * @param paramJobj
     * @return
     * @throws ServiceException 
     */
    @Override
    public int updateRefundReceiptExternallyLinkedWithAdvance(JSONObject paramJobj) throws ServiceException {
        int updateCount = 0;
        List params = new ArrayList();
        String companyid = paramJobj.optString(Constants.companyid, "");
        String paymentId = paramJobj.optString("paymentId", "");
        double amountDue = paramJobj.optDouble("amountDue");
        String updateQuery = "UPDATE receiptadvancedetail rad INNER JOIN linkdetailreceipttoadvancepayment ldr on rad.receipt = ldr.receipt "
                + "set rad.amountdue = ? where rad.advancedetailid is null and  ldr.paymentid = ?  and ldr.company = ? ";
        params.add(amountDue);
        params.add(paymentId);
        params.add(companyid);
        updateCount = executeSQLUpdate(updateQuery, params.toArray());
        return updateCount;
    }
    /**
     * This method is used to get Linking information of refund receipt linked
     * with advance on the basis of payment.
     * @param paramJobj
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject getRefundReceiptLinkDetailsLinkedWithAdvance(JSONObject paramJobj) throws ServiceException {
        List params = new ArrayList();
        List list = new ArrayList();
        String companyid = paramJobj.optString(Constants.companyid, "");
        String paymentId = paramJobj.optString("paymentId", "");
        String exgainlossjeid = paramJobj.optString("exgainlossjeid", "");
        String selectQuery = "";
        /* ERP-30246
         *  checks for linkdetails of foreign exchange gain/loss je at time of delete operation from journalentry report 
         */
        if(!StringUtil.isNullOrEmpty(exgainlossjeid)){
            selectQuery = "from LinkDetailReceiptToAdvancePayment where linkedGainLossJE = ? and company.companyID = ? ";
            params.add(exgainlossjeid);
            params.add(companyid);
        } else{
            selectQuery = "from LinkDetailReceiptToAdvancePayment where paymentId = ? and company.companyID = ? ";
            params.add(paymentId);
            params.add(companyid);
        }
        list = executeQuery(selectQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * This method is used to update amount due of refund receipt linked with
     * advance payment internally i.e while creating refund receipt and linking advance payment at line
     * level.
     * @param paramJobj
     * @return
     * @throws ServiceException 
     */
    @Override
    public int updateRefundReceiptLinkedWithAdvance(JSONObject paramJobj) throws ServiceException {
        List params = new ArrayList();
        String companyid = paramJobj.optString(Constants.companyid, "");
        String paymentadvancedetail = paramJobj.optString("paymentadvancedetail", "");
        /**
         * isToRevertAmtDue flag is used to set the amountdue of refund receipt to 0.
         */
        boolean isToRevertAmtDue = paramJobj.optBoolean("isToRevertAmtDue", false);
        String condition = " amountdue = amount ";
        if (isToRevertAmtDue) {
            condition = " amountdue = 0 ";
        }
        String updateQuery = "update receiptadvancedetail set " + condition + " where advancedetailid = ? and company = ? ";
        params.add(paymentadvancedetail);
        params.add(companyid);
        int count = executeSQLUpdate(updateQuery, params.toArray());
        return count;
    }
    /**
     * This function is used to get details of refund receipt linked to advance
     * payment internally i.e when the refund receipt is created by linking the
     * payment at line level.
     * @param paramJobj
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject getRefundReceiptDetailsLinkedToAdvance(JSONObject paramJobj) throws ServiceException {
        List list = new ArrayList();
        List params = new ArrayList();
        String companyid = paramJobj.optString(Constants.companyid, "");
        String paymentadvancedetail = paramJobj.optString("paymentadvancedetail", "");
        String q = "from ReceiptAdvanceDetail where advancedetailid=? and company.companyID=?";
        params.add(paymentadvancedetail);
        params.add(companyid);
        list = executeQuery(q, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    private JSONObject getAdvanceSearchForCustomQuery(Map<String, Object> request, ArrayList params, ArrayList paramsSQLWithoutInv, String searchDefaultFieldSQL) throws JSONException, ServiceException {
        JSONObject returnObj = new JSONObject();
        boolean isOpeningBalancePayment = false;
        if (request.get("isOpeningBalancePayment") != null) {
            isOpeningBalancePayment = (Boolean) request.get("isOpeningBalancePayment");
        }
        String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
        if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
            if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                filterConjuctionCriteria = com.krawler.common.util.Constants.or;
            }
        }
        String mySearchFilterString = "";
        String joinString1 = "";
        String jeid = "";
        String Searchjson = "";
       try{
        if (request.containsKey("searchJson") && request.get("searchJson") != null) {
            if(request.containsKey("searchJsonMakePayment") && request.get("searchJsonMakePayment") != null)
            {
                Searchjson = StringUtil.DecodeText(request.get("searchJsonMakePayment").toString());
            } 
            else
            {
                Searchjson = StringUtil.DecodeText(request.get("searchJson").toString());
            }

            if (!StringUtil.isNullOrEmpty(Searchjson)) {
                JSONObject serachJobj = new JSONObject(Searchjson);
                JSONArray customSearchFieldArray = new JSONArray();
                JSONArray defaultSearchFieldArray = new JSONArray();
                StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);

                if (customSearchFieldArray.length() > 0) {
                        /*
                         Advance Search For Custom fields
                         */
                        request.put(Constants.Searchjson, Searchjson);
                    request.put(Constants.appendCase, "and");
                        request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                            request.put("isOpeningBalance", isOpeningBalancePayment);
                            request.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                        if (isOpeningBalancePayment) {
//                            mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
//                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "openingbalancepaymentcustomdata");//    
////                        mySearchFilterStringforOpeningTransaction = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
//                            joinString1 = " inner join openingbalancepaymentcustomdata on openingbalancepaymentcustomdata.openingbalancepayment=payment.id ";                            
                        } else {
                            mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
//                            mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                            if (mySearchFilterString.contains("accjecustomdata") || mySearchFilterString.contains("AccJECustomData")) {
                                joinString1 = " inner join accjecustomdata on accjecustomdata.journalentryId=payment.journalentry ";
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "accjecustomdata");//    
                            }
                            StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                            if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accjedetailcustomdata");//    
                                joinString1 += " left join accjedetailcustomdata  on accjedetailcustomdata.jedetailId=jedetail.id ";
                                jeid = " jedetail.journalentry = payment.journalentry ";
                            }
                            if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "accjedetailproductcustomdata");//    
                                joinString1 += " left join accjedetailproductcustomdata  on accjedetailproductcustomdata.jedetailId=jedetail.id ";
                                jeid = " jedetail.journalentry = payment.journalentry ";
                            }
                            if (mySearchFilterString.contains("CustomerCustomData")) {
                                joinString1 += " left join customercustomdata  on customercustomdata.customerId=customer.id ";
                                mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "customercustomdata");
                            }
                        }
                        StringUtil.insertParamAdvanceSearchString1(paramsSQLWithoutInv, Searchjson);
                    }
                mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
            }
        }
        returnObj.put("jeid", jeid);
        returnObj.put("joinString1", joinString1);
        returnObj.put("mySearchFilterString", mySearchFilterString);
         } catch (Exception ex) {
            Logger.getLogger(accPaymentImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accPaymentr mpl.getAdvanceSearchForCustomQuery:" + ex.getMessage(), ex);
        }
        return returnObj;
    }
}
