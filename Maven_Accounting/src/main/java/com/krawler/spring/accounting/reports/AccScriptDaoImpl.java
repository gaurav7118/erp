/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.googlecode.cqengine.query.simple.Has;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.CreditNote;
import com.krawler.hql.accounting.CreditNoteDetail;
import com.krawler.hql.accounting.CreditNotePaymentDetails;
import com.krawler.hql.accounting.CreditNoteTaxEntry;
import com.krawler.hql.accounting.DebitNote;
import com.krawler.hql.accounting.DebitNoteDetail;
import com.krawler.hql.accounting.DebitNotePaymentDetails;
import com.krawler.hql.accounting.DebitNoteTaxEntry;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.LinkDetailPaymentToCreditNote;
import com.krawler.hql.accounting.Payment;
import com.krawler.hql.accounting.Receipt;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class AccScriptDaoImpl extends BaseDAO implements AccScriptDao {

    public Map getDebitNoteAndDntaxEntry(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        List list1 = new ArrayList();
        Map map = new HashMap();
        Writer writer = null;
        int count = 0;
        boolean success = false;
        try {
            if (request.containsKey("filename")) {
                writer = new FileWriter((String) request.get("filename"));
            } else {
                writer = new FileWriter("/home/krawler/ScriptData/corruptCNDN.csv");
            }
            writer.write("Module,Entry No,Date,DN/DN Amount,DN/CN Amount Due,LinkedAmt\n");
            String companyid = request.get("companyid").toString();
            String query = "from DebitNote dn where dn.company.companyID=?";
            List<DebitNote> dnlist = executeQuery(query, new Object[]{companyid});
            if (dnlist.size() > 0) {
                for (DebitNote debitNote : dnlist) {
                    String dnid = debitNote.getID();
                    query = "from DebitNoteTaxEntry dnd where dnd.debitNote.ID=?";
                    List<DebitNoteTaxEntry> dntaxlist = executeQuery(query, new Object[]{dnid});
                    if (dntaxlist.size() > 0) {
                        double taxdrsum = 0d;
                        for (DebitNoteTaxEntry debitNoteTaxEntry : dntaxlist) {
                            taxdrsum += debitNoteTaxEntry.getAmount();
                            taxdrsum += debitNoteTaxEntry.getTaxamount();
                        }
                        double dnAmount = debitNote.getDnamount();
                        taxdrsum = authHandler.round(taxdrsum, companyid);
                        if (taxdrsum != dnAmount) {
                            list.add(debitNote.getDebitNoteNumber() + "=" + (dnAmount - taxdrsum));
                        }
                    } else {
                        list.add(debitNote.getDebitNoteNumber());
                    }
                }
                for (DebitNote debitNote : dnlist) {
                    String dnid = debitNote.getID();
                    double discountAmt = 0d;
                    double amountpaidindncurrency = 0d;
                    double dnAmount = authHandler.round(debitNote.getDnamount(), companyid);
                    double dnAmountdue = authHandler.round(debitNote.getDnamountdue(), companyid);
                    query = "from DebitNoteDetail dnd where dnd.debitNote.ID=?";
                    List<DebitNoteDetail> dndetailslist = executeQuery(query, new Object[]{dnid});
                    if (dndetailslist.size() > 0) {
                        for (DebitNoteDetail debitNoteDetail : dndetailslist) {
                            if (debitNoteDetail.getDiscount() != null) {
                                discountAmt += debitNoteDetail.getDiscount().getDiscount();
                            }
                        }
                    }
                    query = "from DebitNotePaymentDetails dnd where dnd.debitnote.ID=?";
                    List<DebitNotePaymentDetails> debitNotePaymentDetails = executeQuery(query, new Object[]{dnid});
                    if (debitNotePaymentDetails.size() > 0) {
                        for (DebitNotePaymentDetails notePaymentDetails : debitNotePaymentDetails) {
                            amountpaidindncurrency += notePaymentDetails.getAmountPaid();
                        }
                    }
                    amountpaidindncurrency = authHandler.round(amountpaidindncurrency, companyid);
                    discountAmt = authHandler.round(discountAmt, companyid);
                    double sum = authHandler.round(dnAmountdue + amountpaidindncurrency + discountAmt, companyid);
//                    Date date = debitNote.getJournalEntry() != null ? debitNote.getJournalEntry().getEntryDate() : debitNote.getCreationDate();
                    Date date = debitNote.getCreationDate();
                    if (sum != dnAmount) {
                        writer.write("Debit Note," + debitNote.getDebitNoteNumber() + "," + date + "," + dnAmount + "," + dnAmountdue + "," + amountpaidindncurrency + discountAmt + "\n");
                        list1.add(debitNote.getDebitNoteNumber());
                    }
                }
            }
            request.put("writer", writer);
            Map m = getCreditNoteAndCNtaxEntry(request);
            writer.flush();
            writer.close();
            success = true;
            map.put("dntaxentrydata", list);
            map.put("corruptdn", list1);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccScriptDaoImpl.getDebitNoteAndDntaxEntry:" + ex.getMessage(), ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    map = new HashMap();
                    map.put("success", success);
                } catch (IOException ex) {
                    Logger.getLogger(AccScriptDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return map;
    }

    public Map getCreditNoteAndCNtaxEntry(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        List list1 = new ArrayList();
        Map map = new HashMap();
//        Writer writer = null;
        int count = 0;
        Writer writer = request.containsKey("writer") ? (Writer) request.get("writer") : null;
        try {

//            if (request.containsKey("filename")) {
//                writer = new FileWriter((String) request.get("filename"));
//            } else {
//                writer = new FileWriter("/home/krawler/corruptCN.csv");
//            }
//            writer.write("Module,Entry No,CN Amount,CN Amount Due,LinkedAmt\n");
            String companyid = request.get("companyid").toString();
            String query = "from CreditNote dn where dn.company.companyID=?";
            List<CreditNote> dnlist = executeQuery(query, new Object[]{companyid});
            if (dnlist.size() > 0) {
                for (CreditNote debitNote : dnlist) {
                    String dnid = debitNote.getID();
                    query = "from CreditNoteTaxEntry dnd where dnd.creditNote.ID=?";
                    List<CreditNoteTaxEntry> dntaxlist = executeQuery(query, new Object[]{dnid});
                    if (dntaxlist.size() > 0) {
                        double taxdrsum = 0d;
                        for (CreditNoteTaxEntry debitNoteTaxEntry : dntaxlist) {
                            taxdrsum += debitNoteTaxEntry.getAmount();
                            taxdrsum += debitNoteTaxEntry.getTaxamount();
                        }
                        double dnAmount = debitNote.getCnamount();
                        taxdrsum = authHandler.round(taxdrsum, companyid);
                        if (taxdrsum != dnAmount) {
                            list.add(debitNote.getCreditNoteNumber() + "=" + (dnAmount - taxdrsum));
                        }
                    } else {
                        list.add(debitNote.getCreditNoteNumber());
                    }
                }
                for (CreditNote debitNote : dnlist) {
                    String dnid = debitNote.getID();
                    double discountAmt = 0d;
                    double amountpaidindncurrency = 0d;
                    double dnAmount = authHandler.round(debitNote.getCnamount(), companyid);
                    double dnAmountdue = authHandler.round(debitNote.getCnamountdue(), companyid);
                    query = "from CreditNoteDetail dnd where dnd.creditNote.ID=?";
                    List<CreditNoteDetail> dndetailslist = executeQuery(query, new Object[]{dnid});
                    if (dndetailslist.size() > 0) {
                        for (CreditNoteDetail debitNoteDetail : dndetailslist) {
                            if (debitNoteDetail.getDiscount() != null) {
                                discountAmt += debitNoteDetail.getDiscount().getDiscount();
                            }
                        }
                    }
                    query = "from CreditNotePaymentDetails dnd where dnd.creditnote.ID=?";
                    List<CreditNotePaymentDetails> debitNotePaymentDetails = executeQuery(query, new Object[]{dnid});
                    if (debitNotePaymentDetails.size() > 0) {
                        for (CreditNotePaymentDetails notePaymentDetails : debitNotePaymentDetails) {
                            amountpaidindncurrency += notePaymentDetails.getAmountPaid();
                        }
                    }
                    query = "from LinkDetailPaymentToCreditNote dnd where dnd.creditnote.ID=?";
                    List<LinkDetailPaymentToCreditNote> linkDetailPaymentToCreditNotes = executeQuery(query, new Object[]{dnid});
                    if (linkDetailPaymentToCreditNotes.size() > 0) {
                        for (LinkDetailPaymentToCreditNote linkDetailPaymentToCreditNote : linkDetailPaymentToCreditNotes) {
                            amountpaidindncurrency += linkDetailPaymentToCreditNote.getAmount();
                        }
                    }
                    amountpaidindncurrency = authHandler.round(amountpaidindncurrency, companyid);
                    discountAmt = authHandler.round(discountAmt, companyid);
                    double sum = authHandler.round(dnAmountdue + amountpaidindncurrency + discountAmt, companyid);
//                    Date date = debitNote.getJournalEntry() != null ? debitNote.getJournalEntry().getEntryDate() : debitNote.getCreationDate();
                    Date date = debitNote.getCreationDate();
                    if (sum != dnAmount) {
                        writer.write("Credit Note," + debitNote.getCreditNoteNumber() + "," + date + "," + dnAmount + "," + dnAmountdue + "," + amountpaidindncurrency + discountAmt + "\n");
                        list1.add(debitNote.getCreditNoteNumber());
                    }
                }
            }
            map.put("cntaxentrydata", list);
            map.put("corruptcn", list1);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccScriptDaoImpl.getDebitNoteAndDntaxEntry:" + ex.getMessage(), ex);
        }
        return map;
    }

    public Map getTrnsactionsOtherThanControlAccountForVendor(Map<String, Object> request) throws ServiceException {
        Map map = new HashMap();
        List result = new ArrayList();
        List JeList = new ArrayList();
        List GoodsreceiptList = new ArrayList();
        List paymentList = new ArrayList();
        List receiptList = new ArrayList();
        List DNList = new ArrayList();
        List CNList = new ArrayList();
        try {
            String companyid = request.get("companyid").toString();
            String query = "from GoodsReceipt gr where gr.company.companyID=? and gr.normalInvoice=true";
            List<GoodsReceipt> jelist = executeQuery(query, new Object[]{companyid});
            if (jelist.size() > 0) {
                for (GoodsReceipt goodsReceipt : jelist) {
                    String je = goodsReceipt.getJournalEntry().getID();
                    String jeNumber = goodsReceipt.getJournalEntry().getEntryNumber();
                    String vendorAccId = goodsReceipt.getVendor().getAccount().getID();
                    query = "select id from jedetail jed where jed.company=? and jed.journalentry=? and "
                            + "jed.account in (select distinct account from vendor where company=?)";
                    List jedlist = executeSQLQuery(query, new Object[]{companyid, je, companyid});
                    if (jedlist.size() == 0) {
                        GoodsreceiptList.add(goodsReceipt.getGoodsReceiptNumber());
                    }
                }
            }
            query = "from Payment gr where gr.company.companyID=? and gr.normalPayment=true and gr.vendor.ID is NOT NULL";
            List<Payment> jelist1 = executeQuery(query, new Object[]{companyid});
            if (jelist1.size() > 0) {
                for (Payment goodsReceipt : jelist1) {
                    String je = goodsReceipt.getJournalEntry().getID();
                    String jeNumber = goodsReceipt.getJournalEntry().getEntryNumber();
                    String vendorAccId = goodsReceipt.getVendor().getAccount().getID();
                    query = "select id from jedetail jed where jed.company=? and jed.journalentry=? and "
                            + "jed.account in (select distinct account from vendor where company=?)";
                    List jedlist = executeSQLQuery(query, new Object[]{companyid, je, companyid});
                    if (jedlist.size() == 0) {
                        paymentList.add(goodsReceipt.getPaymentNumber());
                    }
                }
            }
            query = "from Receipt gr where gr.company.companyID=? and gr.normalReceipt=true and gr.vendor is NOT NULL";
            List<Receipt> jelist4 = executeQuery(query, new Object[]{companyid});
            if (jelist4.size() > 0) {
                for (Receipt goodsReceipt : jelist4) {
                    String je = goodsReceipt.getJournalEntry().getID();
                    String jeNumber = goodsReceipt.getJournalEntry().getEntryNumber();
                    String vendorAccId = goodsReceipt.getVendor();
                    query = "select id from jedetail jed where jed.company=? and jed.journalentry=? and "
                            + "jed.account in (select distinct account from vendor where company=?)";
                    List jedlist = executeSQLQuery(query, new Object[]{companyid, je, companyid});
                    if (jedlist.size() == 0) {
                        receiptList.add(goodsReceipt.getReceiptNumber());
                    }
                }
            }
            query = "from DebitNote gr where gr.company.companyID=? and gr.normalDN=true and gr.vendor.ID is NOT NULL";
            List<DebitNote> jelist2 = executeQuery(query, new Object[]{companyid});
            if (jelist2.size() > 0) {
                for (DebitNote goodsReceipt : jelist2) {
                    String je = goodsReceipt.getJournalEntry().getID();
                    String jeNumber = goodsReceipt.getJournalEntry().getEntryNumber();
                    String vendorAccId = goodsReceipt.getVendor().getAccount().getID();
                    query = "select id from jedetail jed where jed.company=? and jed.journalentry=? and "
                            + "jed.account in (select distinct account from vendor where company=?)";
                    List jedlist = executeSQLQuery(query, new Object[]{companyid, je, companyid});
                    if (jedlist.size() == 0) {
                        DNList.add(goodsReceipt.getDebitNoteNumber());
                    }
                }
            }
            query = "from CreditNote gr where gr.company.companyID=? and gr.normalCN=true and gr.vendor.ID is NOT NULL";
            List<CreditNote> jelist3 = executeQuery(query, new Object[]{companyid});
            if (jelist3.size() > 0) {
                for (CreditNote goodsReceipt : jelist3) {
                    String je = goodsReceipt.getJournalEntry().getID();
                    String jeNumber = goodsReceipt.getJournalEntry().getEntryNumber();
                    String vendorAccId = goodsReceipt.getVendor().getAccount().getID();
                    query = "select id from jedetail jed where jed.company=? and jed.journalentry=? and "
                            + "jed.account in (select distinct account from vendor where company=?)";
                    List jedlist = executeSQLQuery(query, new Object[]{companyid, je, companyid});
                    if (jedlist.size() == 0) {
                        CNList.add(goodsReceipt.getCreditNoteNumber());
                    }
                }
            }
            map.put("goodsreceipt", GoodsreceiptList);
            map.put("payment", paymentList);
            map.put("receipt", receiptList);
            map.put("DN", DNList);
            map.put("CN", CNList);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPurchaseOrderImpl.getTrnsactionsOtherThanControlAccount:" + ex.getMessage(), ex);
        }
        return map;
    }

    public Map getOpeningTrnsactionsOtherThanControlAccountForVendor(Map<String, Object> request) throws ServiceException {
        Map map = new HashMap();
        List result = new ArrayList();
        List JeList = new ArrayList();
        List GoodsreceiptList = new ArrayList();
        List paymentList = new ArrayList();
        List receiptList = new ArrayList();
        List DNList = new ArrayList();
        List CNList = new ArrayList();
        try {
            String companyid = request.get("companyid").toString();
            String query = "select grnumber from goodsreceipt gr where gr.company=? and gr.isnormalinvoice=0 and gr.isOpeningBalenceInvoice=1 and gr.vendor IS NOT NULL and "
                    + "gr.account not in (select distinct v.account from vendor v where v.company=?) ";
            List jelist = executeSQLQuery(query, new Object[]{companyid, companyid});
            Iterator iterator = jelist.iterator();
            if (iterator.hasNext()) {
                String grNumber = (String) iterator.next();
                GoodsreceiptList.add(grNumber);
            }
            query = "select paymentnumber from payment gr where gr.company=? and gr.isnormalpayment=0 and gr.isopeningbalencepayment=1 and gr.vendor IS NOT NULL and "
                    + "gr.account not in (select distinct v.account from vendor v where v.company=?) ";
            jelist = executeSQLQuery(query, new Object[]{companyid, companyid});
            iterator = jelist.iterator();
            if (iterator.hasNext()) {
                String grNumber = (String) iterator.next();
                paymentList.add(grNumber);
            }
            query = "select receiptnumber from receipt gr where gr.company=? and gr.isnormalreceipt=0 and gr.isopeningbalencereceipt=1 and gr.vendor IS NOT NULL and "
                    + "gr.account not in (select distinct v.account from vendor v where v.company=?) ";
            jelist = executeSQLQuery(query, new Object[]{companyid, companyid});
            iterator = jelist.iterator();
            if (iterator.hasNext()) {
                String grNumber = (String) iterator.next();
                receiptList.add(grNumber);
            }
            query = "select dnnumber from debitnote gr where gr.company=? and gr.isnormaldn=0 and gr.isopeningbalencedn=1 and gr.vendor IS NOT NULL and "
                    + "gr.account not in (select distinct v.account from vendor v where v.company=?) ";
            jelist = executeSQLQuery(query, new Object[]{companyid, companyid});
            iterator = jelist.iterator();
            if (iterator.hasNext()) {
                String grNumber = (String) iterator.next();
                DNList.add(grNumber);
            }
            query = "select cnnumber from creditnote gr where gr.company=? and gr.isnormalcn=0 and gr.isopeningbalencecn=1 and gr.vendor IS NOT NULL and "
                    + "gr.account not in (select distinct v.account from vendor v where v.company=?) ";
            jelist = executeSQLQuery(query, new Object[]{companyid, companyid});
            iterator = jelist.iterator();
            if (iterator.hasNext()) {
                String grNumber = (String) iterator.next();
                CNList.add(grNumber);
            }
            map.put("goodsreceipt", GoodsreceiptList);
            map.put("payment", paymentList);
            map.put("receipt", receiptList);
            map.put("DN", DNList);
            map.put("CN", CNList);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPurchaseOrderImpl.getTrnsactionsOtherThanControlAccount:" + ex.getMessage(), ex);
        }
        return map;
    }

    public Map getTrnsactionsOtherThanControlAccountForCustomer(Map<String, Object> request) throws ServiceException {
        Map map = new HashMap();
        List result = new ArrayList();
        List JeList = new ArrayList();
        List invoiceList = new ArrayList();
        List paymentList = new ArrayList();
        List receiptList = new ArrayList();
        List DNList = new ArrayList();
        List CNList = new ArrayList();
        try {
            String companyid = request.get("companyid").toString();
            String query = "from Invoice gr where gr.company.companyID=? and gr.normalInvoice=true";
            List<Invoice> jelist = executeQuery(query, new Object[]{companyid});
            if (jelist.size() > 0) {
                for (Invoice goodsReceipt : jelist) {
                    String je = goodsReceipt.getJournalEntry().getID();
                    String jeNumber = goodsReceipt.getJournalEntry().getEntryNumber();
                    String customerAccId = goodsReceipt.getCustomer().getAccount().getID();
                    query = "select id from jedetail jed where jed.company=? and jed.journalentry=? and "
                            + "jed.account in (select distinct account from customer where company=?)";
                    List jedlist = executeSQLQuery(query, new Object[]{companyid, je, companyid});
                    if (jedlist.size() == 0) {
                        invoiceList.add(goodsReceipt.getInvoiceNumber());
                    }
                }
            }
            query = "from Payment gr where gr.company.companyID=? and gr.normalPayment=true and gr.customer is NOT NULL";
            List<Payment> jelist1 = executeQuery(query, new Object[]{companyid});
            if (jelist1.size() > 0) {
                for (Payment goodsReceipt : jelist1) {
                    String je = goodsReceipt.getJournalEntry().getID();
                    String jeNumber = goodsReceipt.getJournalEntry().getEntryNumber();
                    String customerAccId = goodsReceipt.getCustomer();
                    query = "select id from jedetail jed where jed.company=? and jed.journalentry=? and "
                            + "jed.account in  (select distinct account from customer where company=?)";
                    List jedlist = executeSQLQuery(query, new Object[]{companyid, je, companyid});
                    if (jedlist.size() == 0) {
                        paymentList.add(goodsReceipt.getPaymentNumber());
                    }
                }
            }
            query = "from Receipt gr where gr.company.companyID=? and gr.normalReceipt=true and gr.customer is NOT NULL";
            List<Receipt> jelist4 = executeQuery(query, new Object[]{companyid});
            if (jelist4.size() > 0) {
                for (Receipt goodsReceipt : jelist4) {
                    String je = goodsReceipt.getJournalEntry().getID();
                    String jeNumber = goodsReceipt.getJournalEntry().getEntryNumber();
                    String customerAccId = goodsReceipt.getCustomer().getAccount().getID();
                    query = "select id from jedetail jed where jed.company=? and jed.journalentry=? and "
                            + "jed.account in (select distinct account from customer where company=?)";
                    List jedlist = executeSQLQuery(query, new Object[]{companyid, je, companyid});
                    if (jedlist.size() == 0) {
                        receiptList.add(goodsReceipt.getReceiptNumber());
                    }
                }
            }
            query = "from DebitNote gr where gr.company.companyID=? and gr.normalDN=true and gr.customer.ID is NOT NULL";
            List<DebitNote> jelist2 = executeQuery(query, new Object[]{companyid});
            if (jelist2.size() > 0) {
                for (DebitNote goodsReceipt : jelist2) {
                    String je = goodsReceipt.getJournalEntry().getID();
                    String jeNumber = goodsReceipt.getJournalEntry().getEntryNumber();
                    String customerAccId = goodsReceipt.getCustomer().getAccount().getID();
                    query = "select id from jedetail jed where jed.company=? and jed.journalentry=? and "
                            + "jed.account in (select distinct account from customer where company=?)";
                    List jedlist = executeSQLQuery(query, new Object[]{companyid, je, companyid});
                    if (jedlist.size() == 0) {
                        DNList.add(goodsReceipt.getDebitNoteNumber());
                    }
                }
            }
            query = "from CreditNote gr where gr.company.companyID=? and gr.normalCN=true and gr.customer.ID is NOT NULL";
            List<CreditNote> jelist3 = executeQuery(query, new Object[]{companyid});
            if (jelist3.size() > 0) {
                for (CreditNote goodsReceipt : jelist3) {
                    String je = goodsReceipt.getJournalEntry().getID();
                    String jeNumber = goodsReceipt.getJournalEntry().getEntryNumber();
                    String customerAccId = goodsReceipt.getCustomer().getAccount().getID();
                    query = "select id from jedetail jed where jed.company=? and jed.journalentry=? and "
                            + "jed.account in (select distinct account from customer where company=?)";
                    List jedlist = executeSQLQuery(query, new Object[]{companyid, je, companyid});
                    if (jedlist.size() == 0) {
                        CNList.add(goodsReceipt.getCreditNoteNumber());
                    }
                }
            }
            map.put("invoice", invoiceList);
            map.put("payment", paymentList);
            map.put("receipt", receiptList);
            map.put("DN", DNList);
            map.put("CN", CNList);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPurchaseOrderImpl.getTrnsactionsOtherThanControlAccount:" + ex.getMessage(), ex);
        }
        return map;
    }

    public Map getOpeningTrnsactionsOtherThanControlAccountForCustomer(Map<String, Object> request) throws ServiceException {
        Map map = new HashMap();
        List result = new ArrayList();
        List JeList = new ArrayList();
        List invoiceList = new ArrayList();
        List paymentList = new ArrayList();
        List receiptList = new ArrayList();
        List DNList = new ArrayList();
        List CNList = new ArrayList();
        try {
            String companyid = request.get("companyid").toString();
            String query = "select invoicenumber from invoice gr where gr.company=? and gr.isnormalinvoice=0 and gr.isOpeningBalenceInvoice=1 and gr.customer IS NOT NULL and "
                    + "gr.account not in (select distinct v.account from customer v where v.company=?) ";
            List jelist = executeSQLQuery(query, new Object[]{companyid, companyid});
            Iterator iterator = jelist.iterator();
            if (iterator.hasNext()) {
                String grNumber = (String) iterator.next();
                invoiceList.add(grNumber);
            }
            query = "select paymentnumber from payment gr where gr.company=? and gr.isnormalpayment=0 and gr.isopeningbalencepayment=1 and gr.customer IS NOT NULL and "
                    + "gr.account not in (select distinct v.account from customer v where v.company=?) ";
            jelist = executeSQLQuery(query, new Object[]{companyid, companyid});
            iterator = jelist.iterator();
            if (iterator.hasNext()) {
                String grNumber = (String) iterator.next();
                paymentList.add(grNumber);
            }
            query = "select receiptnumber from receipt gr where gr.company=? and gr.isnormalreceipt=0 and gr.isopeningbalencereceipt=1 and gr.customer IS NOT NULL and "
                    + "gr.account not in (select distinct v.account from customer v where v.company=?) ";
            jelist = executeSQLQuery(query, new Object[]{companyid, companyid});
            iterator = jelist.iterator();
            if (iterator.hasNext()) {
                String grNumber = (String) iterator.next();
                receiptList.add(grNumber);
            }
            query = "select dnnumber from debitnote gr where gr.company=? and gr.isnormaldn=0 and gr.isopeningbalencedn=1 and gr.customer IS NOT NULL and "
                    + "gr.account not in (select distinct v.account from customer v where v.company=?) ";
            jelist = executeSQLQuery(query, new Object[]{companyid, companyid});
            iterator = jelist.iterator();
            if (iterator.hasNext()) {
                String grNumber = (String) iterator.next();
                DNList.add(grNumber);
            }
            query = "select cnnumber from creditnote gr where gr.company=? and gr.isnormalcn=0 and gr.isopeningbalencecn=1 and gr.customer IS NOT NULL and "
                    + "gr.account not in (select distinct v.account from customer v where v.company=?) ";
            jelist = executeSQLQuery(query, new Object[]{companyid, companyid});
            iterator = jelist.iterator();
            if (iterator.hasNext()) {
                String grNumber = (String) iterator.next();
                CNList.add(grNumber);
            }
            map.put("invoice", invoiceList);
            map.put("payment", paymentList);
            map.put("receipt", receiptList);
            map.put("DN", DNList);
            map.put("CN", CNList);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPurchaseOrderImpl.getTrnsactionsOtherThanControlAccount:" + ex.getMessage(), ex);
        }
        return map;
    }

    public Map getOpeningDocumentListForVendor(HashMap<String, Object> request) throws ServiceException {
        Writer writer = null;
        Map map = new HashMap();
        boolean success = false;
        List params = new ArrayList();
        try {
            String companyid = request.containsKey("companyid") ? request.get("companyid").toString() : "";
            if (request.containsKey("filename")) {
                writer = new FileWriter((String) request.get("filename"));
            } else {
                writer = new FileWriter("/home/krawler/ScriptData/openingDocListForVendor.csv");
            }
            params.add(companyid);
            String condition = "";
            String accname = request.containsKey("accountname") ? request.get("accountname").toString() : "All";
            writer.write("Account name,Vendor name,Type,Transaction Number,Amount,AmountInBase \n");
            for (StringTokenizer stringTokenizer1 = new StringTokenizer(accname, ","); stringTokenizer1.hasMoreTokens();) {
                String name = stringTokenizer1.nextToken();
                if (!name.equalsIgnoreCase("All")) {
                    params.add(accname);
                    condition += " and gr.account.name=?";
                }
                String query = "from GoodsReceipt gr where gr.company.companyID=? and gr.normalInvoice=false" + condition;
                List<GoodsReceipt> jelist = executeQuery(query, params.toArray());
                if (jelist.size() > 0) {
                    for (GoodsReceipt goodsReceipt : jelist) {
                        writer.write(goodsReceipt.getAccount().getAccountName() + "," + goodsReceipt.getVendor().getName() + ",Goodsreceipt," + goodsReceipt.getGoodsReceiptNumber() + "," + goodsReceipt.getOriginalOpeningBalanceAmount()
                                + "," + goodsReceipt.getOriginalOpeningBalanceBaseAmount() + "\n");
                    }
                }
                query = "from Payment gr where gr.company.companyID=? and gr.normalPayment=false" + condition;
                List<Payment> jelist1 = executeQuery(query, params.toArray());
                if (jelist1.size() > 0) {
                    for (Payment goodsReceipt : jelist1) {
                        writer.write(goodsReceipt.getAccount().getAccountName() + "," + goodsReceipt.getVendor().getName() + ",Payment," + goodsReceipt.getPaymentNumber() + "," + goodsReceipt.getDepositAmount()
                                + "," + goodsReceipt.getOriginalOpeningBalanceBaseAmount() + "\n");
                    }
                }
                query = "from DebitNote gr where gr.company.companyID=? and gr.normalDN=false and gr.vendor.ID is NOT NULL" + condition;
                List<DebitNote> jelist2 = executeQuery(query, params.toArray());
                if (jelist2.size() > 0) {
                    for (DebitNote goodsReceipt : jelist2) {
                        writer.write(goodsReceipt.getAccount().getAccountName() + "," + goodsReceipt.getVendor().getName() + ",DebitNote," + goodsReceipt.getDebitNoteNumber() + "," + goodsReceipt.getDnamount()
                                + "," + goodsReceipt.getOriginalOpeningBalanceBaseAmount() + "\n");
                    }
                }
                query = "from CreditNote gr where gr.company.companyID=? and gr.normalCN=false and gr.vendor.ID is NOT NULL" + condition;
                List<CreditNote> jelist3 = executeQuery(query, params.toArray());
                if (jelist3.size() > 0) {
                    for (CreditNote goodsReceipt : jelist3) {
                        writer.write(goodsReceipt.getAccount().getAccountName() + "," + goodsReceipt.getVendor().getName() + ",CreditNote," + goodsReceipt.getCreditNoteNumber() + "," + goodsReceipt.getCnamount()
                                + "," + goodsReceipt.getOriginalOpeningBalanceBaseAmount() + "\n");
                    }
                }
            }
            writer.flush();
            writer.close();
            success = true;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPurchaseOrderImpl.getOpeningDocumentList:" + ex.getMessage(), ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    map = new HashMap();
                    map.put("success", success);
                } catch (IOException ex) {
                    Logger.getLogger(AccScriptDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return map;
    }

    public Map getOpeningDocumentListForCustomer(HashMap<String, Object> request) throws ServiceException {
        Writer writer = null;
        Map map = new HashMap();
        boolean success = false;
        List params = new ArrayList();
        try {
            String companyid = request.containsKey("companyid") ? request.get("companyid").toString() : "";
            if (request.containsKey("filename")) {
                writer = new FileWriter((String) request.get("filename"));
            } else {
                writer = new FileWriter("/home/krawler/ScriptData/openingDocListForCustomer.csv");
            }
            params.add(companyid);
            String condition = "";
            String accname = request.containsKey("accountname") ? request.get("accountname").toString() : "All";
            writer.write("Account name,Customer name,Type,Transaction Number,Amount,AmountInBase \n");
            for (StringTokenizer stringTokenizer1 = new StringTokenizer(accname, ","); stringTokenizer1.hasMoreTokens();) {
                String name = stringTokenizer1.nextToken();
                if (!name.equalsIgnoreCase("All")) {
                    params.add(accname);
                    condition += " and gr.account.name=?";
                }
                String query = "from Invoice gr where gr.company.companyID=? and gr.normalInvoice=false " + condition;
                List<Invoice> jelist = executeQuery(query, params.toArray());
                if (jelist.size() > 0) {
                    for (Invoice goodsReceipt : jelist) {
                        writer.write(goodsReceipt.getAccount().getAccountName() + "," + goodsReceipt.getCustomer().getName() + ",Goodsreceipt," + goodsReceipt.getInvoiceNumber() + "," + goodsReceipt.getOriginalOpeningBalanceAmount()
                                + "," + goodsReceipt.getOriginalOpeningBalanceBaseAmount() + "\n");
                    }
                }
                query = "from Receipt gr where gr.company.companyID=? and gr.normalReceipt=false " + condition;
                List<Receipt> jelist1 = executeQuery(query, params.toArray());
                if (jelist1.size() > 0) {
                    for (Receipt goodsReceipt : jelist1) {
                        writer.write(goodsReceipt.getAccount().getAccountName() + "," + goodsReceipt.getCustomer().getName() + ",Receipt," + goodsReceipt.getReceiptNumber() + "," + goodsReceipt.getDepositAmount()
                                + "," + goodsReceipt.getOriginalOpeningBalanceBaseAmount() + "\n");
                    }
                }
                query = "from DebitNote gr where gr.company.companyID=? and gr.normalDN=false and gr.customer.ID is NOT NULL " + condition;
                List<DebitNote> jelist2 = executeQuery(query, params.toArray());
                if (jelist2.size() > 0) {
                    for (DebitNote goodsReceipt : jelist2) {
                        writer.write(goodsReceipt.getAccount().getAccountName() + "," + goodsReceipt.getCustomer().getName() + ",DebitNote," + goodsReceipt.getDebitNoteNumber() + "," + goodsReceipt.getDnamount()
                                + "," + goodsReceipt.getOriginalOpeningBalanceBaseAmount() + "\n");
                    }
                }
                query = "from CreditNote gr where gr.company.companyID=? and gr.normalCN=false and gr.customer.ID is NOT NULL " + condition;
                List<CreditNote> jelist3 = executeQuery(query, params.toArray());
                if (jelist3.size() > 0) {
                    for (CreditNote goodsReceipt : jelist3) {
                        writer.write(goodsReceipt.getAccount().getAccountName() + "," + goodsReceipt.getCustomer().getName() + ",CreditNote," + goodsReceipt.getCreditNoteNumber() + "," + goodsReceipt.getCnamount()
                                + "," + goodsReceipt.getOriginalOpeningBalanceBaseAmount() + "\n");
                    }
                }
            }
            writer.flush();
            writer.close();
            success = true;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPurchaseOrderImpl.getOpeningDocumentList:" + ex.getMessage(), ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    map = new HashMap();
                    map.put("success", success);
                } catch (IOException ex) {
                    Logger.getLogger(AccScriptDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return new HashMap();
    }

    public List getDNForGainLossNotPosted(Map<String, Object> request) throws ServiceException {
        ArrayList arrayList = new ArrayList();
        String companyid = request.containsKey("companyid") ? (String) request.get("companyid") : "";
        String query = "select dn.id,dn.dnnumber,dnje.entrydate ,gr.grnumber,gje.entrydate ,dn.currency from  debitnote dn inner join dndetails dd on dd.debitNote= dn.id "
                + "inner join goodsreceipt gr on gr.id=dd.goodsreceipt inner join journalentry dnje on dnje.id=dn.journalentry inner join journalentry gje on gje.id=gr.journalentry "
                + "where dn.company=? and gje.externalcurrencyrate != dnje.externalcurrencyrate and dd.linkedgainlossje='' and dn.currency=gr.currency order by dn.currency";
        List list = executeSQLQuery(query, new Object[]{companyid});
        arrayList.addAll(list);
        String openingquery = "select dn.id,dn.dnnumber,dnje.entrydate ,gr.grnumber,gr.creationdate ,dn.currency from  debitnote dn inner join dndetails dd on dd.debitNote= dn.id "
                + "inner join goodsreceipt gr on gr.id=dd.goodsreceipt inner join journalentry dnje on dnje.id=dn.journalentry "
                + "where dn.company=? and gr.exchangerateforopeningtransaction != dnje.externalcurrencyrate and (dd.linkedgainlossje='' or dd.linkedgainlossje is NULL) "
                + "and dn.currency=gr.currency and gr.isopeningbalenceinvoice=1 order by dn.currency";
        List list2 = executeSQLQuery(openingquery, new Object[]{companyid});
        arrayList.addAll(list2);
        return arrayList;
    }

    public List getCNForGainLossNotPosted(Map<String, Object> request) throws ServiceException {
        String companyid = request.containsKey("companyid") ? (String) request.get("companyid") : "";
        String query = "select cn.id,cn.cnnumber,cnje.entrydate,gr.invoicenumber,gje.entrydate,cn.currency from  creditnote cn inner join cndetails cd on cd.creditNote= cn.id "
                + "inner join invoice gr on gr.id=cd.invoice inner join journalentry cnje on cnje.id=cn.journalentry inner join journalentry gje on gje.id=gr.journalentry "
                + "where cn.company=? and gje.externalcurrencyrate != cnje.externalcurrencyrate and cd.linkedgainlossje='' and cn.currency=gr.currency order by cn.currency";
        List list = executeSQLQuery(query, new Object[]{companyid});
        return list;
    }

    public List getPaymentForGainLossNotPosted(Map<String, Object> request) throws ServiceException {
        ArrayList arrayList = new ArrayList();
        String companyid = request.containsKey("companyid") ? (String) request.get("companyid") : "";
        String query = "select p.id,p.paymentnumber,pje.entrydate ,gr.grnumber,gje.entrydate ,p.currency from  payment p inner join linkdetailpayment ld on ld.payment= p.id "
                + "inner join goodsreceipt gr on gr.id=ld.goodsreceipt inner join journalentry pje on pje.id=p.journalentry inner join journalentry gje on gje.id=gr.journalentry "
                + "where p.company=? and gje.externalcurrencyrate != pje.externalcurrencyrate and (ld.linkedgainlossje='' or ld.linkedgainlossje is NULL) and p.currency=gr.currency order by p.currency";
        List list = executeSQLQuery(query, new Object[]{companyid});
        arrayList.addAll(list);
        query = "select p.id,p.paymentnumber,pje.entrydate ,gr.cnnumber,gje.entrydate ,p.currency from  payment p inner join linkdetailpaymenttocreditnote ld on ld.payment= p.id "
                + "inner join creditnote gr on gr.id=ld.creditnote inner join journalentry pje on pje.id=p.journalentry inner join journalentry gje on gje.id=gr.journalentry "
                + "where p.company=? and gje.externalcurrencyrate != pje.externalcurrencyrate and (ld.linkedgainlossje='' or ld.linkedgainlossje is NULL) and p.currency=gr.currency order by p.currency";
        list = executeSQLQuery(query, new Object[]{companyid});
        arrayList.addAll(list);

        String openingquery = "select p.id,p.paymentnumber,pje.entrydate ,gr.grnumber,gr.creationdate ,p.currency from  payment p inner join linkdetailpayment ld on ld.payment= p.id "
                + "inner join goodsreceipt gr on gr.id=ld.goodsreceipt inner join journalentry pje on pje.id=p.journalentry "
                + "where p.company=? and gr.exchangerateforopeningtransaction != pje.externalcurrencyrate and (ld.linkedgainlossje='' or ld.linkedgainlossje is NULL) "
                + "and p.currency=gr.currency and gr.isopeningbalenceinvoice=1 order by p.currency";
        List list2 = executeSQLQuery(openingquery, new Object[]{companyid});
        arrayList.addAll(list2);
        openingquery = "select p.id,p.paymentnumber,pje.entrydate ,gr.cnnumber,gr.creationdate ,p.currency from  payment p inner join linkdetailpaymenttocreditnote ld on ld.payment= p.id "
                + "inner join creditnote gr on gr.id=ld.creditnote inner join journalentry pje on pje.id=p.journalentry "
                + "where p.company=? and gr.exchangerateforopeningtransaction != pje.externalcurrencyrate and (ld.linkedgainlossje='' or ld.linkedgainlossje is NULL) "
                + "and p.currency=gr.currency and gr.isopeningbalencecn=1 order by p.currency";
        list2 = executeSQLQuery(openingquery, new Object[]{companyid});
        arrayList.addAll(list2);
        return arrayList;
    }

    public List getReceiptForGainLossNotPosted(Map<String, Object> request) throws ServiceException {
        ArrayList arrayList = new ArrayList();
        String companyid = request.containsKey("companyid") ? (String) request.get("companyid") : "";
        String query = "select p.id,p.receiptnumber,pje.entrydate ,gr.invoicenumber,gje.entrydate ,p.currency from  receipt p inner join linkdetailreceipt ld on ld.receipt= p.id "
                + "inner join invoice gr on gr.id=ld.invoice inner join journalentry pje on pje.id=p.journalentry inner join journalentry gje on gje.id=gr.journalentry "
                + "where p.company=? and gje.externalcurrencyrate != pje.externalcurrencyrate and (ld.linkedgainlossje='' or ld.linkedgainlossje is NULL) and p.currency=gr.currency order by p.currency";
        List list = executeSQLQuery(query, new Object[]{companyid});
        arrayList.addAll(list);
        query = "select p.id,p.receiptnumber,pje.entrydate ,gr.dnnumber,gje.entrydate ,p.currency from  receipt p inner join linkdetailreceipttodebitnote ld on ld.receipt= p.id "
                + "inner join debitnote gr on gr.id=ld.debitnote inner join journalentry pje on pje.id=p.journalentry inner join journalentry gje on gje.id=gr.journalentry "
                + "where p.company=? and gje.externalcurrencyrate != pje.externalcurrencyrate and (ld.linkedgainlossje='' or ld.linkedgainlossje is NULL) and p.currency=gr.currency order by p.currency";
        list = executeSQLQuery(query, new Object[]{companyid});
        arrayList.addAll(list);

        String openingquery = "select p.id,p.receiptnumber,pje.entrydate ,gr.invoicenumber,gr.creationdate ,p.currency from  receipt p inner join linkdetailreceipt ld on ld.receipt= p.id "
                + "inner join invoice gr on gr.id=ld.invoice inner join journalentry pje on pje.id=p.journalentry "
                + "where p.company=? and gr.exchangerateforopeningtransaction != pje.externalcurrencyrate and (ld.linkedgainlossje='' or ld.linkedgainlossje is NULL) "
                + "and p.currency=gr.currency and gr.isopeningbalenceinvoice=1 order by p.currency";
        List list2 = executeSQLQuery(openingquery, new Object[]{companyid});
        arrayList.addAll(list2);
        openingquery = "select p.id,p.receiptnumber,pje.entrydate ,gr.dnnumber,gr.creationdate ,p.currency from  receipt p inner join linkdetailreceipttodebitnote ld on ld.receipt= p.id "
                + "inner join debitnote gr on gr.id=ld.debitnote inner join journalentry pje on pje.id=p.journalentry "
                + "where p.company=? and gr.exchangerateforopeningtransaction != pje.externalcurrencyrate and (ld.linkedgainlossje='' or ld.linkedgainlossje is NULL) "
                + "and p.currency=gr.currency and gr.isopeningbalencedn=1 order by p.currency";
        list2 = executeSQLQuery(openingquery, new Object[]{companyid});
        arrayList.addAll(list2);
        return arrayList;
    }

    public Map getManualJEForControlAccount(Map<String, Object> request) throws ServiceException {
        Map map = new HashMap();
        String companyid = request.containsKey("companyid") ? (String) request.get("companyid") : "";
        String query = "select je.entryno,je.id from journalentry je inner join jedetail jed on jed.journalentry = je.id where (je.typevalue = 1 or je.typevalue=3)"
                + " and je.company=? and jed.account in (select distinct account from vendor where company=?) and je.deleteflag='F'  group by je.id";
        List list = executeSQLQuery(query, new Object[]{companyid, companyid});
        ArrayList FinalList = new ArrayList();
        Iterator itr = list.iterator();
        double diffamt = 0d;
        while (itr.hasNext()) {
            Object[] oj = (Object[]) itr.next();
            String jeno = oj[0].toString();
            String jeid = oj[1].toString();
            String q = "select sum(amount) from jedetail where debit='T' and journalentry =? and account in (select distinct account from vendor where company=?)";
            List list1 = executeSQLQuery(q, new Object[]{jeid, companyid});
            String debit = list1.get(0) != null ? list1.get(0).toString() : "0";
            double db = Double.valueOf(debit);
            q = "select sum(amount) from jedetail where debit='F' and journalentry =? and account in (select distinct account from vendor where company=?)";
            list1 = executeSQLQuery(q, new Object[]{jeid, companyid});
            String credit = list1.get(0) != null ? list1.get(0).toString() : "0";
            double dr = Double.valueOf(credit);
            if (db - dr != 0) {
                FinalList.add(jeno);
            }
        }
        map.put("ManualJE", FinalList);

//        # Query to check if any party JE(with CN/DN) is present without any CN/DN linked to it.
        query = "select entryno from journalentry je where je.id not in "
                + "(select je.id from journalentry je  inner join creditnote cn on cn.journalentry = je.id inner join debitnote dn on dn.journalentry = je.id where je.typevalue = 2 and je.company = ?)"
                + " and je.typevalue = 2 and je.company = ? and je.deleteflag='F' and je.partlyjeentrywithcndn = 1";
        list = executeSQLQuery(query, new Object[]{companyid, companyid});
        map.put("PartyJEWithoutCNDN", list);

//        # Query to check if any party JE(with CN/DN) is present with customerVendorID as NULL or EMPTY
        query = "select entryno,entrydate,je.deleteflag,je.partlyjeentrywithcndn,jed.customervendorid from journalentry je inner join "
                + "jedetail jed on jed.journalentry = je.id  where je.id in (select je.id from journalentry je "
                + " inner join creditnote cn on cn.journalentry = je.id inner join debitnote dn on dn.journalentry = je.id where je.typevalue = 2 and je.company = ?)"
                + " and je.typevalue = 2 and je.company = ? and je.partlyjeentrywithcndn = 1 and (jed.customervendorid is null or jed.customervendorid ='')";
        list = executeSQLQuery(query, new Object[]{companyid, companyid});
        map.put("PartyJEWithVenCustNull", list);
        return map;
    }

    public List getInvoicesAmountDiffThanJEAmount(Map<String, Object> request) throws ServiceException {
        String companyid = request.containsKey("companyid") ? (String) request.get("companyid") : "";
        String query = "select gr.invoicenumber,gr.invoiceamountinbase, je.entryno,jed.amountinbase,(jed.amountinbase - gr.invoiceamountinbase) as diff from invoice gr inner join "
                + "customer v on v.id = gr.customer inner join journalentry je on je.id = gr.journalentry inner join "
                + " jedetail jed on jed.journalentry = je.id inner join account a on a.id = v.account where gr.company=? and jed.debit = 'F'  group by gr.id having diff > 0";
        List list = executeSQLQuery(query, new Object[]{companyid});
        return list;
    }

    public List getGoodsReceiptAmountDiffThanJEAmount(Map<String, Object> request) throws ServiceException {
        String companyid = request.containsKey("companyid") ? (String) request.get("companyid") : "";
        String query = "select gr.grnumber,gr.invoiceamountinbase, je.entryno,jed.amountinbase,(jed.amountinbase - gr.invoiceamountinbase) as diff from goodsreceipt gr inner join "
                + "vendor v on v.id = gr.vendor inner join journalentry je on je.id = gr.journalentry inner join "
                + " jedetail jed on jed.journalentry = je.id inner join account a on a.id = v.account where gr.company=? and jed.debit = 'F'  group by gr.id having diff > 0";
        List list = executeSQLQuery(query, new Object[]{companyid});
        return list;
    }

    public List getDifferentPaymentAndGainLossJEAccount(Map<String, Object> request) throws ServiceException {
        String companyid = request.containsKey("companyid") ? (String) request.get("companyid") : "";
        List l = new ArrayList();
        String query = "select jed.account,lje.id,p.paymentnumber from payment p inner join advancedetail adp on adp.payment=p.id "
                + "inner join jedetail jed on jed.id=adp.totaljedid inner join linkdetailpayment ldp on ldp.payment=p.id "
                + "inner join journalentry lje on lje.id=ldp.linkedgainlossje inner join jedetail ljed on ljed.journalentry=lje.id "
                + "where  p.company=? group by p.id;";
        List list = executeSQLQuery(query, new Object[]{companyid});
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] oj = (Object[]) itr.next();
            String account = oj[0].toString();
            String jeid = oj[1].toString();
            String payment = oj[2].toString();
            String a = "select id from jedetail where journalentry=? and account=?";
            List list1 = executeSQLQuery(a, new Object[]{jeid, account});
            if (list1.size() == 0) {
                l.add(payment);
            }
        }
        return l;
    }

    public List getDifferentReceiptAndGainLossJEAccount(Map<String, Object> request) throws ServiceException {
        String companyid = request.containsKey("companyid") ? (String) request.get("companyid") : "";
        List l = new ArrayList();
        String query = "select jed.account,lje.id,p.receiptnumber from receipt p inner join receiptadvancedetail adp on adp.receipt=p.id "
                + "inner join jedetail jed on jed.id=adp.totaljedid inner join linkdetailreceipt ldp on ldp.receipt=p.id "
                + "inner join journalentry lje on lje.id=ldp.linkedgainlossje inner join jedetail ljed on ljed.journalentry=lje.id "
                + "where  p.company=? group by p.id;";
        List list = executeSQLQuery(query, new Object[]{companyid});
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] oj = (Object[]) itr.next();
            String account = oj[0].toString();
            String jeid = oj[1].toString();
            String payment = oj[2].toString();
            String a = "select id from jedetail where journalentry=? and account=?";
            List list1 = executeSQLQuery(a, new Object[]{jeid, account});
            if (list1.size() == 0) {
                l.add(payment);
            }
        }
        return l;
    }
    
    @Override
    public Map updateMailidsLocally(Map<String, Object> request) throws ServiceException {
        Map map = new HashMap();
        String companyid = request.containsKey("companyid") ? (String) request.get("companyid") : "";
        String EmailID = request.containsKey("EmailID")? (String) request.get("EmailID") : "";
        
        String query = "update users us, compaccpreferences cp set us.emailid=? , cp.sendimportmailidsforolympus=? , cp.approvalemails = ? where us.company=? and cp.id=?";
        int count = executeSQLUpdate(query, new Object[]{EmailID,EmailID,EmailID,companyid,companyid});
        map.put("count", count);
        return map;
    }
/**
 * 
 * @param param
 * @return
 * @throws ServiceException 
 * @Desc : List of company for which discount data available
 */
    public KwlReturnObject getAllCompanyOfBrandDiscount(JSONObject param) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            String subdomain = param.optString("subdomain", "");
            String condition = "";
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                condition += " and c.subdomain=?";
                params.add(subdomain);
            }
            String query = "select c.companyid from productbranddiscountdetails p inner join company c where c.companyid=p.company " + condition + " group by c.companyid";
            list = executeSQLQuery(query, params.toArray());

            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getQuotations:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
        public KwlReturnObject getAllCompanyIndiaUS(JSONObject param) throws ServiceException {
        List list = null;
        int count = 0;
        String query="";
        try {
            ArrayList localParams = new ArrayList();
             String subdomain=param.optString("subdomain");
             if (!StringUtil.isNullOrEmpty(subdomain)) {
                localParams.add(subdomain);                
                query = "select c.companyid,c.country,c.subdomain,cp.setupdone from company c INNER JOIN compaccpreferences cp ON  c.companyid=cp.id  where subdomain=?";
             }else{
//                query = "select c.companyid,c.country,c.subdomain from company c inner join country cr on cr.id=c.country where cr.id in ('105','244')";
                  query = "select c.companyid,c.country,c.subdomain,cp.setupdone from company c inner join country cr on cr.id=c.country INNER JOIN compaccpreferences cp on c.companyid=cp.id  where cr.id in ('105','244')";  
             }                            
            list = executeSQLQuery(query, localParams.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Error While getting Companies for India and US" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
        public KwlReturnObject getAllCompanisOfIndiaOnly(JSONObject param) throws ServiceException {
        List list = null;
        int count = 0;
        String query="";
        try {
            ArrayList localParams = new ArrayList();
             String subdomain=param.optString("subdomain");
             if (!StringUtil.isNullOrEmpty(subdomain)) {
                localParams.add(subdomain);                
                query = "select c.companyid,c.country,c.subdomain,cp.setupdone from company c INNER JOIN compaccpreferences cp ON  c.companyid=cp.id  where subdomain=?";
             }else{
//                query = "select c.companyid,c.country,c.subdomain from company c inner join country cr on cr.id=c.country where cr.id in ('105','244')";
                  query = "select c.companyid,c.country,c.subdomain,cp.setupdone from company c inner join country cr on cr.id=c.country INNER JOIN compaccpreferences cp on c.companyid=cp.id  where cr.id in ('105')";  
             }                            
            list = executeSQLQuery(query, localParams.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Error While getting Companies for India" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
/**
 * 
 * @param requestMap
 * @return
 * @throws ServiceException 
 * @Desc  : Update product custom data 
 */
    public List updateProductCustomData(Map requestMap,List returnlist) throws ServiceException {
        String productid = (String) requestMap.get("productid");
        String companyid = (String) requestMap.get("companyid");
        String brandvalue = (String) requestMap.get("brandvalue");
        String moduleid = "30";
        int colnum = (Integer) requestMap.get("colnum");

        /**
         * Check whether custom data exist for product
         */
        String query = " select productId  from accproductcustomdata where productId=? and col" + colnum + " is NULL";
        List list = executeSQLQuery(query, new String[]{productid});
        if (list.size() > 0 && list.get(0) != null && !StringUtil.isNullOrEmpty(list.get(0).toString())) {
            /**
             * Need to update data
             */
            String updatequery = " update accproductcustomdata set col" + colnum + "=? where productId=?";
            int a = executeSQLUpdate(updatequery, new String[]{brandvalue, productid});
            returnlist.add(" Updated For :" + productid);
        } else {
            /**
             * Need to insert data
             */

            String query1 = " select productId  from accproductcustomdata where productId=? ";
            List list1 = executeSQLQuery(query1, new String[]{productid});
            if (list1.isEmpty()) {
            String hqlquery = "insert into accproductcustomdata (productId,company,moduleId,col" + colnum + ") values(?,?,?,?)";
            int a = executeSQLUpdate(hqlquery, new String[]{productid, companyid, moduleid, brandvalue});

            String updatequery = " update product set accproductcustomdataref=? where id=?";
            a = executeSQLUpdate(updatequery, new String[]{productid, productid});
            returnlist.add(" Inserted For : " + productid);
        }

        }
        return returnlist;
    }

/**
 * 
 * @param dataobj
 * @throws ServiceException 
 * @Desc : Update Brand discount table with dimension values
 */
    public void updateBrandDiscount(JSONObject dataobj) throws ServiceException {
        for (Iterator it = dataobj.keys(); it.hasNext();) {
            String masterid = (String) it.next();
            String fcdid = dataobj.optString(masterid);
            String updatequery = " update productbranddiscountdetails set productbrand=? where productbrand=?";
            int a = executeSQLUpdate(updatequery, new String[]{fcdid, masterid});
        }
    }
/**
 * 
 * @param jSONObject
 * @return
 * @throws ServiceException 
 * @Desc  : List of products for which brand is tagged
 */
    public KwlReturnObject getProductsHavingBrand(JSONObject jSONObject) throws ServiceException {
        String company = jSONObject.optString("companyid");
        boolean isdefaultbom = false;
        List returnList = new ArrayList();
        String query = "select p.id,p.productbrand from product p where p.productbrand is NOT NULL and p.company=?";
        returnList = executeSQLQuery(query, new String[]{company});
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    /**
     * 
     * @param param
     * @param query
     * @return
     * @throws ServiceException 
     */
    @Override
     public List getCommonQueryResultForDimensionValueScript(JSONObject reqParams ,String query, List queryParams, boolean isDelete) throws ServiceException {
        List list = null;
        try {
            if (isDelete) {
                executeSQLUpdate(query, queryParams.toArray());
            } else {
                list = executeSQLQuery(query, queryParams.toArray());
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Error While getting query Result for India script - deleteEmptyAndNoneValuesFromCustomDimension" + ex.getMessage(), ex);
        }
        return list;
    }
    @Override
    public Map cleanUpCompanyData(HashMap<String, Object> request) throws ServiceException {
        int count = 0;
        boolean success = false;
        String subdomain = "";
        String dbname = "";
        Map map = new HashMap();
        ArrayList params = new ArrayList();

        try {

            if (request.containsKey("dbname")) {
                dbname = request.get("dbname").toString();
            }
            if (request.containsKey("subdomain")) {
                subdomain = request.get("subdomain").toString();
            }

            String query = "call cleanupcompanydata('" + dbname + "','" + subdomain + "')";
            executeSQLUpdate(query);


        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getQuotations:" + ex.getMessage(), ex);


        }
        return map;



    }
    /**
     * Function to get company id for specific country
     *
     * @param params
     * @return
     * @throws ServiceException
     */
    public List getCompanyData(JSONObject params) throws ServiceException {
        ArrayList list = new ArrayList();
        String condition = "";
        if (!StringUtil.isNullOrEmpty(params.optString("subdomain"))) {
            condition += " and subdomain=?";
            list.add(params.optString("subdomain"));
        }
        String query = "select c.companyid,c.creator,ca.firstfyfrom,c.subdomain from company c inner join compaccpreferences ca on ca.id=c.companyid"
                + " where country=105" + condition;
        List returnlist = executeSQLQuery(query, list.toArray());
        return returnlist;
    }
    public List getForegnReferencesForTax(JSONObject params) throws ServiceException {
        ArrayList list = new ArrayList();
        String condition = "";
        String foreignkeyresult = "SELECT TABLE_NAME,COLUMN_NAME,CONSTRAINT_NAME,REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME "
                + "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE REFERENCED_TABLE_NAME = 'tax'";
        List returnlist = executeSQLQuery(foreignkeyresult, list.toArray());
        return returnlist;
    }

    public List getDocumentDetailsForTax(JSONObject params) throws ServiceException {
        ArrayList list = new ArrayList();
        String condition = "";
        String tablename = params.optString("tablename");
        String columnname = params.optString("columnname");
        String companyid = params.optString("companyid");

        String query = " select id from " + tablename + " where " + columnname + " in "
                + "( select id from tax where company= '" + companyid + "')";
        List returnlist = executeSQLQuery(query, list.toArray());
        return returnlist;
    }
    /**
     * Function to get column no and Field id for tax class
     *
     * @param params
     * @return
     * @throws ServiceException
     */
    public List getFieldParamsData(JSONObject params) throws ServiceException {
        ArrayList list = new ArrayList();
        String condition = "";
        list.add(params.optString("companyid"));
        if (!StringUtil.isNullOrEmpty(params.optString("fieldname"))) {
            condition += " and fieldname=?";
            list.add("Custom_" + params.optString("fieldname"));
        }
        if (!StringUtil.isNullOrEmpty(params.optString("moduleid"))) {
            condition += " and moduleid=?";
            list.add(params.optInt("moduleid"));
        }
        String query = "select id,colnum from fieldparams where companyid=?" + condition;
        List returnlist = executeSQLQuery(query, list.toArray());
        return returnlist;
    }

    /**
     * Function to get products for which history data not present in history
     * table
     *
     * @param params
     * @return
     * @throws ServiceException
     */
    public List getProductsForTaxClassHistory(JSONObject params) throws ServiceException {
        ArrayList list = new ArrayList();
        String condition = "";
        int isAsset=params.optInt("moduleid")==Constants.Acc_FixedAssets_AssetsGroups_ModuleId?1:0;
        int colnum = params.optInt("taxclasscolnum");
        list.add(params.optString("companyid"));
        list.add(isAsset);
        list.add(params.optString("companyid"));
        list.add(params.optString("taxclassfieldid"));
        String query = "select p.id,pcd.col" + colnum + ",p.asofdate from product p inner join accproductcustomdata pcd on pcd.productId=p.id "
                + "where p.company=? and p.isasset=? and "
                + "p.id not in (select ph.product from productcustomfieldhistory ph where ph.company=? and ph.fieldparams=?)"
                + "" + condition;
        List returnlist = executeSQLQuery(query, list.toArray());
        return returnlist;
    }

    /**
     * Function to insert Product tax class history for product
     *
     * @param params
     * @throws ServiceException
     */
    public void insertProductTaxClassInHistory(JSONObject params) throws ServiceException {
        List list = new ArrayList();
        Date creationdate = new Date();
//        try {
            creationdate=(Date)params.opt("creationdate");
//            creationdate = authHandler.getDateOnlyFormat().parse(params.optString("creationdate"));
//        } catch (SessionExpiredException ex) {
//            Logger.getLogger(AccScriptDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ParseException ex) {
//            Logger.getLogger(AccScriptDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
//        }
        list.add(params.optInt("moduleid"));
        list.add(creationdate);
        list.add(creationdate);
        list.add(params.optString("taxclassvalue"));
        list.add(params.optString("productid"));
        list.add(params.optString("companyid"));
        list.add(params.optString("userid"));
        list.add(params.optString("taxclassfieldid"));
        String hqlquery = "insert into productcustomfieldhistory (id,moduleid,applydate,creationdate,value,product,company,user,fieldparams) "
                + "values(UUID(),?,?,?,?,?,?,?,?)";
        int a = executeSQLUpdate(hqlquery, list.toArray());
    }

    /**
     * Function to insert GST field data for customer
     *
     * @param params
     * @throws ServiceException
     */
    public void insertCustomerGSTHistory(JSONObject params) throws ServiceException {
        String companyid = params.optString("companyid");
        ArrayList list = new ArrayList();
        list.add(companyid);
        list.add(companyid);
        String query = "INSERT INTO gstcustomerhistory (id,gstin,applydate,customer,gstregistrationtype,gstcustomertype) "
                + "SELECT UUID(),gstin,createdon,id,gstregistrationtype,gstcustomertype FROM customer WHERE "
                + "  company = ? and id \n"
                + "not in (select customer from gstcustomerhistory where company = ?)";
        int a = executeSQLUpdate(query,list.toArray());
    }

    /**
     * Function to insert GST field data for vendor
     *
     * @param params
     * @throws ServiceException
     */
    public void insertVendorGSTHistory(JSONObject params) throws ServiceException {
        String companyid = params.optString("companyid");
        ArrayList list = new ArrayList();
        list.add(companyid);
        list.add(companyid);
        String query = "INSERT INTO gstvendorhistory (id,gstin,applydate,vendor,gstregistrationtype,gstvendortype) "
                + "SELECT UUID(),gstin,createdon,id,gstregistrationtype,gstvendortype FROM vendor WHERE company = ? and id \n"
                + "not in (select vendor from gstvendorhistory where company = ?)";
        executeSQLUpdate(query,list.toArray());
    }

    /**
     * Function to insert sales transaction GST fields data
     *
     * @param params
     * @throws ServiceException
     */
    public void insertSalesTransactionHistoryData(JSONObject params) throws ServiceException {
        String companyid = params.optString("companyid");
        String maintable = params.optString("maintable");
        int moduleid = params.optInt("moduleid");
        ArrayList list = new ArrayList();
        list.add(companyid);
        String isOpeningInvoiceQuery = " ";
        if(moduleid==Constants.Acc_Invoice_ModuleId){
            isOpeningInvoiceQuery = " and inv.isopeningbalenceinvoice = 0 ";
        }
        String dateField=params.optString("datecolumn");
        String query = "INSERT INTO gstdocumenthistory (id,gstrtype,custventypeid,gstin,refdocid,moduleid) "
                + "SELECT UUID(),ch.gstregistrationtype,ch.gstcustomertype,ch.gstin,inv.id," + moduleid + " FROM " + maintable + " inv "
                + " inner join customer c on c.id=inv.customer inner join gstcustomerhistory ch on ch.customer=c.id "
                + "  WHERE inv.company = ? and inv."+ dateField+">='2017-07-01' and inv.id \n"
                + " not in (select dh1.refdocid from gstdocumenthistory dh1 where dh1.moduleid = " + moduleid + ") " + isOpeningInvoiceQuery
                + " and ch.applydate = (select max(gch1.applydate) from gstcustomerhistory gch1 where "
                + " gch1.customer=c.id and DATE(gch1.applydate)<=DATE(inv." + dateField + ")) " ;
        int count=executeSQLUpdate(query,list.toArray());

        insertLineDataForTransaction(params);
    }

    /**
     * Function to insert purchase transaction GST fields data
     *
     * @param params
     * @throws ServiceException
     */
    public void insertPurcaseTransactionHistoryData(JSONObject params) throws ServiceException {
        String companyid = params.optString("companyid");
        String maintable = params.optString("maintable");
        int moduleid = params.optInt("moduleid");
        ArrayList list = new ArrayList();
        list.add(companyid);
        String isOpeningInvoiceQuery = " ";
        if(moduleid==Constants.Acc_Vendor_Invoice_ModuleId){
            isOpeningInvoiceQuery = " and inv.isopeningbalenceinvoice = 0 ";
        }
        String dateField=params.optString("datecolumn");
        String query = "INSERT INTO gstdocumenthistory (id,gstrtype,custventypeid,gstin,refdocid,moduleid) "
                + "SELECT UUID(),ch.gstregistrationtype,ch.gstvendortype,ch.gstin,inv.id," + moduleid + " FROM " + maintable + " inv "
                + "inner join vendor c on c.id=inv.vendor inner join gstvendorhistory ch on ch.vendor=c.id WHERE inv.company = ? and inv."+ dateField+">='2017-07-01' and inv.id \n"
                + "not in (select dh1.refdocid from gstdocumenthistory dh1 where dh1.moduleid = " + moduleid + ") " + isOpeningInvoiceQuery
                + " and ch.applydate = (select max(gch1.applydate) from gstvendorhistory gch1 "
                + " where gch1.vendor=c.id and DATE(gch1.applydate)<=DATE(inv."+ dateField+")) ";
        int count=executeSQLUpdate(query,list.toArray());
        insertLineDataForTransaction(params);
    }

    /**
     * Function to insert purchase and sales transaction GST tax class data
     *
     * @param params
     * @throws ServiceException
     */
    public void insertLineDataForTransaction(JSONObject params) throws ServiceException {
        String query = "";
        String companyid = params.optString("companyid");
        int moduleid = params.optInt("moduleid");
        String linetable = params.optString("linetable");
        String maintable = params.optString("maintable");
        String dateField=params.optString("datecolumn");
        int colnum = params.optInt("taxclasscolnum");
        ArrayList list = new ArrayList();
        String assetcondition=params.optString("assetcondition","");
        list.add(companyid);
        if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Vendor_Invoice_ModuleId || 
                moduleid==Constants.Acc_FixedAssets_DisposalInvoice_ModuleId || moduleid==Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId) {
            query = "INSERT INTO gsttaxclasshistory (id,producttaxclass,refdocid,moduleid) "
                    + "SELECT UUID(),pch.value,invd.id," + moduleid + " FROM " + linetable + " invd "
                    + " inner join "+ maintable+" mt on mt.id=invd."+maintable
                    + " inner join inventory inv on inv.id=invd.id "
                    + " inner join product p on p.id=inv.product "
                    + " inner join productcustomfieldhistory pch on p.id = pch.product "
                    + " inner join accproductcustomdata pcd on pcd.productId=p.id WHERE p.company = ? "+assetcondition+" and invd.id \n"
                    + " not in (select dh1.refdocid from gsttaxclasshistory dh1 where dh1.moduleid = " + moduleid + ")"
                    + " and pch.applydate=(select max(pch1.applydate) from productcustomfieldhistory pch1 "
                    + " inner join fieldparams fp1 on fp1.id=pch1.fieldparams where fp1.fieldlabel='" + Constants.GSTProdCategory + "' and fp1.moduleid=pch.moduleid and fp1.companyid=pch.company "
                    + " and pch1.product=pch.product and pch1.applydate<=mt."+ dateField+") ";

        } else if (moduleid == Constants.Acc_Make_Payment_ModuleId || moduleid == Constants.Acc_Debit_Note_ModuleId || moduleid == Constants.Acc_Credit_Note_ModuleId) {
            query = "INSERT INTO gsttaxclasshistory (id,producttaxclass,refdocid,moduleid) "
                    + " SELECT UUID(),pch.value,invd.id," + moduleid + " FROM " + linetable + " invd "
                    + " inner join "+ maintable+" mt on mt.id=invd."+maintable
                    + " inner join product p on p.id=invd.productid "
                    + " inner join productcustomfieldhistory pch on p.id = pch.product "
                    + " inner join accproductcustomdata pcd on pcd.productId=p.id WHERE p.company = ? "+assetcondition+" and invd.id \n"
                    + " not in (select dh1.refdocid from gsttaxclasshistory dh1 where dh1.moduleid = " + moduleid + ")"
                    + " and pch.applydate=(select max(pch1.applydate) from productcustomfieldhistory pch1 "
                    + " inner join fieldparams fp1 on fp1.id=pch1.fieldparams where fp1.fieldlabel='" + Constants.GSTProdCategory + "' and fp1.moduleid=pch.moduleid and fp1.companyid=pch.company "
                    + " and pch1.product=pch.product and pch1.applydate<=mt."+ dateField+") ";
        } else {
            query = "INSERT INTO gsttaxclasshistory (id,producttaxclass,refdocid,moduleid) "
                    + "SELECT UUID(),pch.value,invd.id," + moduleid + " FROM " + linetable + " invd "
                    + " inner join "+ maintable+" mt on mt.id=invd."+maintable
                    + " inner join product p on p.id=invd.product "
                    + " inner join productcustomfieldhistory pch on p.id = pch.product "
                    + " inner join accproductcustomdata pcd on pcd.productId=p.id WHERE p.company = ? "+assetcondition+" and invd.id \n"
                    + " not in (select dh1.refdocid from gsttaxclasshistory dh1 where dh1.moduleid = " + moduleid + ")"
                    + " and pch.applydate=(select max(pch1.applydate) from productcustomfieldhistory pch1 "
                    + " inner join fieldparams fp1 on fp1.id=pch1.fieldparams where fp1.fieldlabel='" + Constants.GSTProdCategory + "' and fp1.moduleid=pch.moduleid and fp1.companyid=pch.company "
                    + " and pch1.product=pch.product and pch1.applydate<=mt."+ dateField+") ";;
        }
        int count=executeSQLUpdate(query,list.toArray());
        //System.out.println("Product Tax Class: Moduleid = "+moduleid + ": Count="+count);
        //System.out.println("Query = "+query); 
    }
    /**
     * Function to get state value of invoice using its transaction address/
     * Customer address.
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getStateValueForSalesDocument(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        String documenttable = reqParams.optString("documenttable");
        String documentnumbercolumn = reqParams.optString("documentnumbercolumn");
        String documentnumber = reqParams.optString("documentNumber");
        String companyid = reqParams.optString("companyid");
        params.add(documentnumber);
        params.add(companyid);
        String query = " select (case when dadrs.billingstate='' then cadrs.state when dadrs.billingstate=NULL then cadrs.state else dadrs.billingstate end ) from " + documenttable + " dt inner join customer c on c.id=dt.customer "
                + " left join customeraddressdetails cadrs on cadrs.customerid=c.id and cadrs.isdefaultaddress='T' and cadrs.isbillingaddress='T'  "
                + " left join billingshippingaddresses dadrs on dadrs.id=dt.billingshippingaddresses "
                + " where dt." + documentnumbercolumn + " =? and dt.company=? ";
        List list = executeSQLQuery(query, params.toArray());
        return list;
    }

    /**
     * Function to get state value from address tables
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getStateValueForPurchaseDocument(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        String documenttable = reqParams.optString("documenttable");
        String documentnumbercolumn = reqParams.optString("documentnumbercolumn");
        String documentnumber = reqParams.optString("documentNumber");
        String companyid = reqParams.optString("companyid");
        params.add(documentnumber);
        params.add(companyid);
        String query = " select (case when dadrs.billingstate='' then cadrs.state when dadrs.billingstate=NULL then cadrs.state else dadrs.billingstate end ) from " + documenttable + " dt inner join vendor c on c.id=dt.vendor "
                + " left join vendoraddressdetails cadrs on cadrs.vendorid=c.id and cadrs.isdefaultaddress='T' and cadrs.isbillingaddress='T' "
                + " left join billingshippingaddresses dadrs on dadrs.id=dt.billingshippingaddresses "
                + " where dt." + documentnumbercolumn + " =? and dt.company=? ";
        List list = executeSQLQuery(query, params.toArray());
        return list;
}

    /**
     * Function to set State value in custom table of invoice
     * @param reqParams
     * @throws ServiceException
     * @throws JSONException
     */
    public void setStateValueToInvoice(JSONObject reqParams) throws ServiceException, JSONException {
        int colnum = reqParams.optInt("statecolnum");
        String fieldid = reqParams.optString("statefiedid");
        String statevalue = reqParams.optString("statevalue");
        String jeid = reqParams.optString("jeid");
        List params = new ArrayList();
        params.add(fieldid);
        params.add(statevalue);
        params.add(jeid);
        String query = " update accjecustomdata set col" + colnum + "=(select id from fieldcombodata where fieldid=? and value=?) where journalentryId=?";
        executeSQLUpdate(query, params.toArray());

    }

    /**
     * Function to update tax amount for invoice details
     * @param reqParams
     * @throws ServiceException
     */
    public void setInvoiceDetailsTermAmount(JSONObject reqParams) throws ServiceException {
        double rowttermamount = reqParams.optDouble("rowttermamount");
        String invoicedetailid = reqParams.optString("invd");
        String tablename = reqParams.optString("tablename");
        String query = " update " + tablename + " set rowtermamount=? where id=?";
        executeSQLUpdate(query, new Object[]{rowttermamount, invoicedetailid});
    }

    /**
     * Function to update JEDETAILS for customer account
     * @param reqParams
     * @throws ServiceException
     */
    public void updateAmountInCustomerJEDetails(JSONObject reqParams) throws ServiceException {
        double amount = reqParams.optDouble("amount");
        double amountinbase = reqParams.optDouble("amountinbase");
        String jedid = reqParams.optString("jedid");
        String query = " update jedetail set amount=?,amountinbase=? where id=?";
        executeSQLUpdate(query, new Object[]{amount, amountinbase, jedid});
    }

    /**
     * Function to delete JEDETAILS for GST term accounts.
     * @param reqParams
     * @throws ServiceException
     */
    public void deleteGSTJEDetails(JSONObject reqParams) throws ServiceException {
        String jeid = reqParams.optString("jeid");
        String companyid = reqParams.optString("companyid");
//        String query = " delete jed from jedetail  jed inner join linelevelterms lt on (lt.account=jed.account or lt.payableaccount=jed.account) and lt.company=? "
//                + "where jed.journalEntry=? and jed.company=? and lt.termtype=7";
        String query = " delete from jedetail  where account in (select id from account where defaultaccountid in "
                + "('31ce34bc-5f2b-11e7-907b-a6006ad3dba0','31ce37a0-5f2b-11e7-907b-a6006ad3dba0','31ce38f4-5f2b-11e7-907b-a6006ad3dba0','31ce39e4-5f2b-11e7-907b-a6006ad3dba0'"
                + ",'31ce3aca-5f2b-11e7-907b-a6006ad3dba0','31ce3ba6-5f2b-11e7-907b-a6006ad3dba0','31ce3c78-5f2b-11e7-907b-a6006ad3dba0','31ce3d54-5f2b-11e7-907b-a6006ad3dba0','31ce3e30-5f2b-11e7-907b-a6006ad3dba0',"
                + "'1d941156-c2db-11e7-96e8-c03fd5658531','8bca5f4e-c2dc-11e7-96e8-c03fd5658531','4068e07e-6ba3-11e7-907b-a6006ad3dba0','4068e42a-6ba3-11e7-907b-a6006ad3dba0','4068e948-6ba3-11e7-907b-a6006ad3dba0',"
                + "'4068eaba-6ba3-11e7-907b-a6006ad3dba0')) and journalEntry=? and company=?";
        int count = executeSQLUpdate(query, new Object[]{jeid, companyid});
    }

    /**
     * function to check whether document link further or not
     * @param reqParams
     * @return
     * @throws ServiceException
     */
    public List getLinkedDocument(JSONObject reqParams) throws ServiceException {
        String linkingtable = reqParams.optString("linkingtable");
        String query = " select linkeddocno from " + linkingtable + " where docid=? and sourceflag=0 ";
        List list = executeSQLQuery(query, new Object[]{reqParams.optString("documentid")});
        return list;
    }
}
