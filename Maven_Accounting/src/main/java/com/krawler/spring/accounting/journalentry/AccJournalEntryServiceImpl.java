/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.journalentry;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.inventory.model.stockout.StockAdjustmentDAO;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteService;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteService;
import com.krawler.spring.accounting.depreciation.accDepreciationDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.writeOffInvoice.accWriteOffServiceDao;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccJournalEntryServiceImpl implements AccJournalEntryService, MessageSourceAware {

    private accInvoiceDAO accInvoiceDAOobj;
    private accReceiptDAO accReceiptDAOobj;
    private accVendorPaymentDAO accVendorPaymentobj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accProductDAO accProductObj;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accCreditNoteService accCreditNoteService;
    private accDebitNoteDAO accDebitNoteobj;
    private accDebitNoteService accDebitNoteService;
    private accWriteOffServiceDao accWriteOffServiceDao;
    private StockAdjustmentDAO stockAdjustmentDAO;
    private accBankReconciliationDAO accBankReconciliationObj;
    private accDepreciationDAO accDepreciationObj;
    private accPaymentDAO accPaymentDAOobj;
    private auditTrailDAO auditTrailObj;
    private accAccountDAO accAccountDAOobj;
    private AccJournalEntryModuleService journalEntryModuleServiceobj;
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }

    public void setaccCreditNoteService(accCreditNoteService accCreditNoteService) {
        this.accCreditNoteService = accCreditNoteService;
    }

    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }

    public void setaccDebitNoteService(accDebitNoteService accDebitNoteService) {
        this.accDebitNoteService = accDebitNoteService;
    }

    public void setAccWriteOffServiceDao(accWriteOffServiceDao accWriteOffServiceDao) {
        this.accWriteOffServiceDao = accWriteOffServiceDao;
    }

    public void setStockAdjustmentDAO(StockAdjustmentDAO stockAdjustmentDAO) {
        this.stockAdjustmentDAO = stockAdjustmentDAO;
    }

    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }

    public void setaccDepreciationDAO(accDepreciationDAO accDepreciationObj) {
        this.accDepreciationObj = accDepreciationObj;
    }

    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setJournalEntryModuleServiceobj(AccJournalEntryModuleService journalEntryModuleServiceobj) {
        this.journalEntryModuleServiceobj = journalEntryModuleServiceobj;
    }
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {SessionExpiredException.class, AccountingException.class, ServiceException.class, JSONException.class})
    public void deleteJournalEntry(JSONObject paramJobj, JSONObject jobj, int countryid, String companyid, Boolean flag) throws SessionExpiredException, AccountingException, ServiceException, JSONException {
        if (!StringUtil.isNullOrEmpty(jobj.getString("journalentryid"))) {
            String jeid = StringUtil.DecodeText(jobj.optString("journalentryid"));
            boolean isDelTemp=paramJobj.optBoolean("jeFlag"); //jeFlag comes true in case of temporary delete
            boolean isCNUsed=jobj.optBoolean("iscnused");
            boolean isDNUsed=jobj.optBoolean("isdnused");
            boolean isVatSalesJE = false;
            boolean isVatPurchaseJE = false;
            if (countryid == Constants.indian_country_id) {

                /**
                 * If JE is TDS/TCS JE then need to update amount due for
                 * invoice Also need to delete GST adjustment JE if exist
                 */
                KwlReturnObject kwlReturnObject = accReceiptDAOobj.getReceiptInvoiceJEMapping(jobj);
                List<ReceiptInvoiceJEMapping> receiptInvoiceJEMappings = kwlReturnObject.getEntityList();
                List<String> invoiList = new ArrayList();
                /**
                 * Need to delete ReceiptInvoiceJEMapping
                 */
                accReceiptDAOobj.deleteReceiptInvoiceJEMapping(jobj);
                for (ReceiptInvoiceJEMapping receiptInvoiceJEMapping : receiptInvoiceJEMappings) {

                    /**
                     * Delete GST adjustment JE
                     */
                    if (receiptInvoiceJEMapping.getGstAdjustment() != null) {
                        accJournalEntryobj.deleteJournalEntryPermanent(receiptInvoiceJEMapping.getGstAdjustment().getID(), companyid);
                    }

                    /**
                     * Update invoice amount due
                     */
                    if (invoiList.contains(receiptInvoiceJEMapping.getInvoice().getID())) {
                        continue;
                    }
                    invoiList.add(receiptInvoiceJEMapping.getInvoice().getID());
                    JSONObject invjson = new JSONObject();
                    invjson.put("invoiceid", receiptInvoiceJEMapping.getInvoice().getID());
                    invjson.put("companyid", companyid);
                    invjson.put(Constants.invoiceamountdue, receiptInvoiceJEMapping.getInvoiceamountdue());
                    invjson.put(Constants.invoiceamountdueinbase, receiptInvoiceJEMapping.getInvoiceamountdueinbase());
                    KwlReturnObject result = accInvoiceDAOobj.updateInvoice(invjson, new HashSet());
                }


                JSONObject datamap = new JSONObject();
                datamap.put(Constants.companyKey, companyid);
                datamap.put("table", "InvoiceDetailTermsMap");
                datamap.put("jeid", jeid);
                datamap.put("istaxpaid", 1);
                datamap.put("termtype", IndiaComplianceConstants.LINELEVELTERMTYPE_VAT);
                isVatSalesJE = accJournalEntryobj.checkIfJEisVatJE(datamap);
                datamap.put("table", "ReceiptDetailTermsMap");
                isVatPurchaseJE = accJournalEntryobj.checkIfJEisVatJE(datamap);
            }
            String reversejeno = (!StringUtil.isNullOrEmpty(jobj.getString("reversejeno"))) ? jobj.getString("reversejeno") : "";
            boolean BRdeleteFlag = jobj.has("isBR") || jobj.has("isBUR") ? true : false;
            String BRID = jobj.has("BRID") ? jobj.getString("BRID") : "";
            List list;
            List billingList;
            KwlReturnObject result, billingResult;
            result = accReceiptDAOobj.getBReciptFromJE(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Receipt(s). Delete <b>Receipt</b> instead.");
            }

            result = accReceiptDAOobj.getReceiptLinkedInvoiceJE(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Receipt(s). Delete <b>Receipt</b> instead.");
            }
            result = accReceiptDAOobj.getAdvanceReceiptLinkedInvoiceJE(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Receipt(s). Delete <b>Receipt</b> instead.");
            }
            result = accReceiptDAOobj.getReceiptLinkedDebitNoteJE(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Receipt(s). Delete <b>Receipt</b> instead.");
            }

            KwlReturnObject curJEObj = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeid);
            JournalEntry journalEntry = null;
            if (!curJEObj.getEntityList().isEmpty() && curJEObj.getEntityList().get(0) != null) {
                journalEntry = (JournalEntry) curJEObj.getEntityList().get(0);
            }
            result = accCreditNoteDAOobj.getCreditNoteFromJE(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                if (journalEntry != null ? journalEntry.getPartlyJeEntryWithCnDn() == 1 : false) {
                    CreditNote cn = (CreditNote) list.get(0);
                    if (cn != null) {
                        boolean isCreditNoteLinkedWithPayment = false;
                        KwlReturnObject resultOfCnUsedInPayment = accCreditNoteDAOobj.getMakePaymentIdLinkedWithCreditNote(cn.getID());
                        List listOfPaymentAssiciatedToCN = resultOfCnUsedInPayment.getEntityList();
                        if (listOfPaymentAssiciatedToCN.size() > 0) {
                            isCreditNoteLinkedWithPayment = true;
                        } else {
                            resultOfCnUsedInPayment = accVendorPaymentobj.getAdvancePaymentIdLinkedWithNote(cn.getID());   //get creditnote which is to linked Advanced Payment
                            listOfPaymentAssiciatedToCN = resultOfCnUsedInPayment.getEntityList();
                            if (listOfPaymentAssiciatedToCN.size() > 0) {
                                isCreditNoteLinkedWithPayment = true;
                            }
                        }
                        if (isCreditNoteLinkedWithPayment) {
                            throw new AccountingException(messageSource.getMessage("acc.je.CanNotDeleteJEAssociatedToCN", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                        }
                        if (isDelTemp) {
                            if (!isCNUsed) {
                                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                requestParams.put("cnid",cn.getID());
                                requestParams.put("isused", isCNUsed);
                                requestParams.put("companyid",companyid);
                                requestParams.put("CreditNote",true);
                                accCreditNoteDAOobj.deletePartyJournalCNDNTemporary(requestParams);
                            } else {
                                throw new AccountingException("Some of selected record(s) are currently associated with Credit Note(s). Delete <b>Credit Note</b> instead.");
                            }

                        }else{
                            accCreditNoteService.updateOpeningInvoiceAmountDue(cn.getID(), companyid);
                            accCreditNoteDAOobj.deletePartyJournalCN(cn.getID(), companyid);
                        }
                    }
                } else {
                    throw new AccountingException("Some of selected record(s) are currently associated with Credit Note(s). Delete <b>Credit Note</b> instead.");
                }
            }

            result = accDebitNoteobj.getDebitNoteFromJE(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                if (journalEntry != null ? journalEntry.getPartlyJeEntryWithCnDn() == 1 : false) {
                    DebitNote dn = (DebitNote) list.get(0);
                    if (dn != null) {
                        boolean isDebitNoteLinkedWithPayment = false;
                        KwlReturnObject resultOfDnUsedInReceipt = accDebitNoteobj.getReceivePaymentIdLinkedWithDebitNote(dn.getID());
                        List listOfPaymentAssiciatedToDN = resultOfDnUsedInReceipt.getEntityList();
                        if (listOfPaymentAssiciatedToDN.size() > 0) {
                            isDebitNoteLinkedWithPayment = true;
                        } else {
                            resultOfDnUsedInReceipt = accReceiptDAOobj.getAdvanceReceiptIdLinkedWithNote(dn.getID()); //get debitnote which is linked to Advanced Payment
                            listOfPaymentAssiciatedToDN = resultOfDnUsedInReceipt.getEntityList();
                            if (listOfPaymentAssiciatedToDN.size() > 0) {
                                isDebitNoteLinkedWithPayment = true;
                            }
                        }
                        if (isDebitNoteLinkedWithPayment) {
                            throw new AccountingException(messageSource.getMessage("acc.je.CanNotDeleteJEAssociatedToDN", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                        }                          
                        if (isDelTemp) {
                            if (!isDNUsed ) {
                                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                requestParams.put("dnid", dn.getID());
                                requestParams.put("isused", isDNUsed);
                                requestParams.put("companyid", companyid);
                                requestParams.put("DebitNote", true);
                                accCreditNoteDAOobj.deletePartyJournalCNDNTemporary(requestParams);
                            } else {
                                throw new AccountingException("Some of selected record(s) are currently associated with Credit Note(s). Delete <b>Credit Note</b> instead.");
                            }

                        }else{
                                accDebitNoteService.updateOpeningInvoiceAmountDue(dn.getID(), companyid);
                                accDebitNoteobj.deletePartyJournalDN(dn.getID(), companyid);
                        }
                        
                    }
                } else {
                    throw new AccountingException("Some of selected record(s) are currently associated with Debit Note(s). Delete <b>Debit Note</b> instead.");
                }
            }

            result = accInvoiceDAOobj.getSalesInvoiceFromJE(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Invoice(s). Delete <b>Invoice</b> instead.");
            }
            result = accInvoiceDAOobj.getDeliveryOrderFromJE(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Delivery Order(s). Delete <b>Delivery Order</b> instead.");
            }
            
            result = accGoodsReceiptobj.getGoodsReceiptFromJE(jeid, companyid);
            list = result.getEntityList();
            billingResult = accGoodsReceiptobj.getBGRFromJE(jeid, companyid);
            billingList = billingResult.getEntityList();
            if (!list.isEmpty() || !billingList.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Vendor Invoice(s). Delete <b>Vendor Invoice</b> instead.");
            }

            result = accVendorPaymentobj.getPaymentMadeFromJE(jeid, companyid);
            list = result.getEntityList();
            billingResult = accVendorPaymentobj.getBillingPaymentFromJE(jeid, companyid);
            billingList = billingResult.getEntityList();
            if (!list.isEmpty() || !billingList.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Payment(s). Delete <b>Payment</b> instead.");
            }

            result = accVendorPaymentobj.getPaymentLinkedInvoiceJE(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Payment(s). Delete <b>Payment</b> instead.");
            }

            result = accVendorPaymentobj.getPaymentLinkedCreditNoteJE(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Payment(s). Delete <b>Payment</b> instead.");
            }
            /*
             * Checking if JE is import service JE
             */
            result = accVendorPaymentobj.JEForPaymentOfImportServiceInvoices(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Payment(s). Delete <b>Payment</b> instead.");
            }
            /*
             * Checking If JE is Free Gift JE
             */
            result = accInvoiceDAOobj.JEForFreeGift(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Delivery Order(s). Delete <b>Delivery Order</b> instead.");
            }
//                    query = "from Receipt where journalEntry.ID in( " + qMarks + ") and deleted=false and company.companyID=?";
//                    list = HibernateUtil.executeQuery(session, query, params.toArray());
            result = accReceiptDAOobj.getReciptFromJE(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Receipt(s). Delete <b>Receipt</b> instead.");
            }

            // Check IF JE posted for the dishonoured Cheque
            result = accReceiptDAOobj.getReciptFromDisHonouredJE(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Receipt(s). Delete <b>Receipt</b> instead.");
            }

            // getting cheque id if je is created for fund transfer and delete it later
            result = accJournalEntryobj.getChequeIdLinkedToJournalEntry(jeid, companyid);
            String chequeId = "";
            if (result != null) {
                List chequeList = result.getEntityList();
                if (!chequeList.isEmpty()) {
                    chequeId = (String) chequeList.get(0);
                }
            }
            // Check Foreign gain loss je
            result = accCreditNoteDAOobj.getJEFromCNDetail(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Credit Note(s). Delete <b>Credit Note</b> instead.");
            }
            result = accDebitNoteobj.getJEFromDNDetail(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Debit Note(s). Delete <b>Debit Note</b> instead.");
            }
            
            /* checks linkdetails of exchange gain/loss je
             * ex. when RP advance linked to MP refund externally then separate JE get post for exchange gain/loss can be deleted manually
             * if linkdetails is present then prevents from getting deleted
             */
            HashMap hm=new HashMap<String, Object>();
            hm.put("exgainlossjeid", jeid); //jeid of exchange gain/loss JE
            hm.put("companyid", companyid);
            result = accReceiptDAOobj.getLinkDetailAdvanceReceiptToRefundPayment(hm);
            list= result.getEntityList();
            if(!list.isEmpty()){
                throw new AccountingException(messageSource.getMessage("acc.field.MsgForDeleteForexGainLossJEAssociatedPayment", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            }
            
            /* checks linkdetails of exchange gain/loss je
             * ex. when MP advance linked to RP refund externally then separate JE get post for exchange gain/loss getting deleted manually
             * if linkdetails is present then prevents from getting deleted
             */
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("exgainlossjeid", jeid);    // jeid of exchange gain/loss JE
            jsonObj.put("companyid", companyid);
            result = accPaymentDAOobj.getRefundReceiptLinkDetailsLinkedWithAdvance(jsonObj);
            list= result.getEntityList();
            if(!list.isEmpty()){
                throw new AccountingException(messageSource.getMessage("acc.field.MsgForDeleteForexGainLossJEAssociatedReceipt", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            }
            
            result = accWriteOffServiceDao.getJEFromInvoiceWriteOff(jeid, companyid, false);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Sales Invoice Write Off. Delete <b>Sales Invoice</b> instead.");
            }
            result = accWriteOffServiceDao.getJEFromInvoiceWriteOff(jeid, companyid, true);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Sales Invoice Write Off Recover. Delete <b>Sales Invoice</b> instead.");
            }
            result = accWriteOffServiceDao.getJEFromReceiptWriteOff(jeid, companyid, false);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Receipt Write Off. Delete <b>Sales Invoice</b> instead.");
            }
            result = accWriteOffServiceDao.getJEFromReceiptWriteOff(jeid, companyid, true);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Receipt Write Off Recover. Delete <b>Sales Invoice</b> instead.");
            }
            result = accProductObj.getJEFromDisposedOrRevertedAssetDetails(jeid, companyid, false);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException(messageSource.getMessage("acc.msg.disposedassetje", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            }
            result = accProductObj.getJEFromDisposedOrRevertedAssetDetails(jeid, companyid, true);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException(messageSource.getMessage("acc.msg.revertdisposedassetje", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            }
            result = accGoodsReceiptobj.getGROFromJE(jeid, companyid); // check for GRN
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Goods Receipt(s). Delete <b>Goods Receipt</b> instead.");
            }
            result = accGoodsReceiptobj.getPRFromJE(jeid, companyid); // Check for Purchase Return
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Purchase Return(s). Delete <b>Purchase Return</b> instead.");
            }
            result = accInvoiceDAOobj.getDOCountForInventoryJE(jeid, companyid); // Check for Delivery Order
            list = result.getEntityList();
            if (!list.isEmpty()) {
                long count = (Long) list.get(0);
                if (count > 0) {
                    throw new AccountingException("Some of selected record(s) are currently associated with Delivery Order(s). Delete <b>Delivery Order</b> instead.");
                }
            }
            result = accInvoiceDAOobj.getSRCountForInventoryJE(jeid, companyid); // Check for Sales Return
            list = result.getEntityList();
            if (!list.isEmpty()) {
                long count = (Long) list.get(0);
                if (count > 0) {
                    throw new AccountingException("Some of selected record(s) are currently associated with Sales Return(s). Delete <b>Sales Return</b> instead.");
                }
            }
            
            result = accProductObj.getPostDepreciationJE(jeid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Depreciation(s).Unpost Depreciation instead.");
            }  
              
            if (Constants.indian_country_id == countryid) {
                result = accReceiptDAOobj.getTDSJEmappingTerm(jeid, companyid);
                list = result.getEntityList();
                if (!list.isEmpty()) {
                    throw new AccountingException("Some of selected record(s) are currently associated with TDS of Purchase Invoice(s). Delete <b>Purchase Invoice</b> instead.");
                }
            }
            
            Map<String, Object> saMap = new HashMap<>();
            saMap.put(Constants.companyKey, companyid);
            saMap.put("inventoryjeid", jeid);
            result = stockAdjustmentDAO.getStockAdjustmentJEs(saMap); // Check for SA JEs
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Some of selected record(s) are currently associated with Stock Adjustment(s). Delete <b>Stock Adjustment</b> instead.");
            }
            JSONObject jobj1 = new JSONObject();
            /**
             * Method to check the payment is Reconciled or not according to its
             * JE id
             */
            HashMap<String, Object> reconcileMap = new HashMap<>();
            reconcileMap.put("jeid", jeid);
            reconcileMap.put(Constants.companyKey, companyid);
            boolean isReconciledFlag = accBankReconciliationObj.isRecordReconciled(reconcileMap);
            if (isReconciledFlag) {
                throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " " + "<b>" + journalEntry.getEntryNumber() + "</b>" + " " + messageSource.getMessage("acc.reconcilation.asitisreconciled", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            }
            if (BRdeleteFlag && !StringUtil.isNullOrEmpty(BRID)) {
                accBankReconciliationObj.permenantDeleteBankReconciliationDetailUsingJE(jeid, companyid);
                accBankReconciliationObj.permenantDeleteBankUnReconciliationDetailUsingJE(jeid, companyid);
            }
//                    deleteAssetEntries(request, jeid, companyid);
            deleteAssetEntries(paramJobj, jeid, companyid);
            if (flag == true) {
                // For India Country  reset the vat payment flag
                if (countryid == Constants.indian_country_id) {
                    HashMap paramsHM = new HashMap();
                    paramsHM.put("journalentryid", jeid);
                    accInvoiceDAOobj.resetInvoiceTaxPaidFlag(paramsHM);
                    accGoodsReceiptobj.resetGoodsRecieptTaxPaidFlag(paramsHM);
                    accGoodsReceiptobj.resetPurchaseReturnTaxPaidFlag(paramsHM);
                    accInvoiceDAOobj.resetSalesReturnTaxPaidFlag(paramsHM);
                }
                accJournalEntryobj.deleteJournalEntry(jeid, companyid);

                Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));

                if (isVatPurchaseJE || isVatSalesJE) {
                    auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + paramJobj.getString(Constants.userfullname) + " has deleted VAT Payment Journal Entry " + jobj.getString("entryno"), auditRequestParams, jeid);
                } else {
                    auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + paramJobj.getString(Constants.userfullname) + " has deleted Journal Entry " + jobj.getString("entryno"), auditRequestParams, jeid);
                }

            } else {
                // For India Country 
                if (countryid == Constants.indian_country_id) {
                    HashMap paramsHM = new HashMap();
                    paramsHM.put("journalentryid", jeid);
                    accInvoiceDAOobj.resetInvoiceTaxPaidFlag(paramsHM);
                    accGoodsReceiptobj.resetGoodsRecieptTaxPaidFlag(paramsHM);
                    accGoodsReceiptobj.resetPurchaseReturnTaxPaidFlag(paramsHM);
                    accInvoiceDAOobj.resetSalesReturnTaxPaidFlag(paramsHM);
                }

                /**
                 * delete unconciled records in case of permanent delete
                 */
                accBankReconciliationObj.deleteUnReconciliationRecords(reconcileMap);
                //Delete entry from optimized table
                accJournalEntryobj.deleteAccountJEs_optimized(jeid, true);
                accJournalEntryobj.deleteJournalEntryPermanent(jeid, companyid);
                if (!StringUtil.isNullOrEmpty(chequeId)) {
                    result = accPaymentDAOobj.deleteChequePermanently(chequeId, companyid);
                }
                Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));

                if (isVatPurchaseJE || isVatSalesJE) {
                    auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + paramJobj.getString(Constants.userfullname) + " has deleted VAT Payment Journal Entry Permanently " + jobj.getString("entryno"), auditRequestParams, jeid);
                } else {
                    auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + paramJobj.getString(Constants.userfullname) + " has deleted Journal Entry Permanently " + jobj.getString("entryno"), auditRequestParams, jeid);
                }
            }
            if (!StringUtil.isNullOrEmpty(reversejeno)) {
                accJournalEntryobj.reverseRecurringJEForOneTime(reversejeno, companyid);
            }
        }
    }

    /**
     * @author Neeraj
     * @param je
     * @param companyid
     * @throws SessionExpiredException
     * @throws AccountingException
     * @throws ServiceException
     */
    public void deleteAssetEntries(JSONObject paramJobj, String je, String companyid) throws SessionExpiredException, AccountingException, ServiceException {
        KwlReturnObject purchase, sale, dep;
        String asset = "";
        int period = 0;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();

            requestParams.put("purchaseJe", je);
            purchase = accDepreciationObj.getAsset(requestParams);


            if (purchase != null && purchase.getEntityList().size() > 0) {      //  Purchase Journal Entry Case
                Asset asset1 = (Asset) purchase.getEntityList().get(0);
                if (asset1.getDeleteJe() != null) {
                    throw new AccountingException(messageSource.getMessage("acc.rem.157", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                } else {
                    requestParams.clear();
                    requestParams.put("accountid", asset1.getAccount().getID());
                    dep = accDepreciationObj.getDepreciation(requestParams);

                    if (dep != null && dep.getEntityList().size() > 0) {
                        throw new AccountingException(messageSource.getMessage("acc.rem.155", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else {
                        accAccountDAOobj.deleteAccount(asset1.getAccount().getID(), true);
                    }
                }																//  Purchase Journal Entry Case

            } else {														        //  Sell off and Write Off Journal Entry Case
                requestParams.clear();
                requestParams.put("deleteJe", je);
                sale = accDepreciationObj.getAsset(requestParams);

                if (sale != null && sale.getEntityList().size() > 0) {
                    Asset asset1 = (Asset) sale.getEntityList().get(0);
                    asset = asset1.getAccount().getID();
                    requestParams.clear();
                    requestParams.put(Constants.Acc_id, asset);
                    requestParams.put("deleteJe", null);
                    requestParams.put("isSale", false);
                    requestParams.put("isWriteOff", false);
                    accDepreciationObj.addAssetDetail(requestParams);

                    accAccountDAOobj.deleteAccount(asset1.getAccount().getID(), false);
                    //  Sell off and Write Off Journal Entry Case
                } else {
                    dep = accDepreciationObj.getDepreciationFromJE(je, companyid);	//  Asset Depreciation Journal Entry Case
                    if (dep != null && dep.getEntityList().size() > 0) {
                        DepreciationDetail depreciationDetail = (DepreciationDetail) dep.getEntityList().get(0);
                        asset = depreciationDetail.getAccount().getID();

                        requestParams.clear();
                        requestParams.put("id", asset);
                        sale = accDepreciationObj.getAsset(requestParams);
                        if (sale != null && sale.getEntityList().size() > 0) {
                            Asset asset1 = (Asset) sale.getEntityList().get(0);
                            if (asset1.getDeleteJe() != null) {
                                throw new AccountingException(messageSource.getMessage("acc.rem.154", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                            }
                        }

                        period = depreciationDetail.getPeriod() + 1;

                        requestParams.clear();
                        requestParams.put("period", period);
                        requestParams.put("accountid", asset);
                        dep = accDepreciationObj.getDepreciation(requestParams);

                        if (dep != null && dep.getEntityList().size() > 0) {
                            throw new AccountingException(messageSource.getMessage("acc.rem.153", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                        } else {
                            accDepreciationObj.deleteDepreciationJE(je);
                        }

                    }																//  Asset Depreciation Journal Entry Case
                }
            }

        } catch (AccountingException ex) {
            throw new AccountingException(ex.getMessage());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteAssetEntries : " + ex.getMessage(), ex);
        }
    }
    /**
     * Function to post JE for TDS and TCS type of Payment-Invoice adjustment
     * @param reqParams
     * @return
     * @throws JSONException
     * @throws ServiceException
     * @throws SessionExpiredException
     * @throws AccountingException 
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {SessionExpiredException.class, AccountingException.class, ServiceException.class, JSONException.class})
    public JSONObject postJournalEntry(JSONObject reqParams) throws JSONException, ServiceException, SessionExpiredException, AccountingException {
        String invoiceids = reqParams.optString("invoiceid");
        String invoicenos = reqParams.optString("invoiceno");
        String companyid = reqParams.optString("companyid");
        double amount = reqParams.optDouble("amount");
        String customerid = reqParams.optString("customerid");
        KwlReturnObject cuKwlReturnObject = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerid);
        Customer customer = (Customer) cuKwlReturnObject.getEntityList().get(0);
        String adjustedaccount = reqParams.optString("adjustedaccount");
        if (StringUtil.isNullOrEmpty(adjustedaccount)) {
            throw new AccountingException(messageSource.getMessage("acc.gstrAccountprompt", null, Locale.forLanguageTag(reqParams.getString(Constants.language))));
        }
        String custaccountid = customer.getAccount().getID();
        JSONObject data = new JSONObject();
        JSONArray dataArr = new JSONArray();
        JSONArray detailArr = new JSONArray();
        /**
         * Put TDS adjustment
         */
        JSONObject jedata = new JSONObject();
        jedata.put("memo", reqParams.optString("reason") + " for invoices : " + invoicenos.substring(0, invoicenos.length() - 1));
        jedata.put("auditmessage", reqParams.optString("reason"));
        JSONArray detailsArr = new JSONArray();
        JSONObject detailObj = new JSONObject();
        detailObj.put("amount", amount);
        detailObj.put("accountid", adjustedaccount);
        detailObj.put("debit", true);
        detailObj.put("description", "");
        detailsArr.put(detailObj);
        detailObj = new JSONObject();
        detailObj.put("amount", amount);
        detailObj.put("accountid", custaccountid);
        detailObj.put("debit", false);
        detailObj.put("description", "");
        detailsArr.put(detailObj);
        jedata.put("details", detailsArr);
        JournalEntry journalEntry = journalEntryModuleServiceobj.saveJournalEntryRemoteApplicationJson(reqParams, jedata);
        String JENos = journalEntry.getEntryNumber() + ",";
        String tdsJEID=journalEntry.getID();
        /**
         * Update amount due of selected invoices
         */
        String array=reqParams.optString("invoiceArray");
        JSONArray invoiceArray = new JSONArray(array);
        for (int index = 0; index < invoiceArray.length(); index++) {
            JSONObject invObj = invoiceArray.optJSONObject(index);
            String invoiceid = invObj.optString("billid");
            String invoiceno = invObj.optString("billno");
            double amountdueinbase = invObj.optDouble("amountdueinbase");
            double amountdue = invObj.optDouble("amountdue");
            boolean isRCMapplicable=invObj.optBoolean("gtaapplicable",false);
            if (!StringUtil.isNullOrEmpty(invoiceid)) {
                /**
                 * Get Linked Receipt information
                 */
                JSONObject invjson = new JSONObject();
                invjson.put("invoiceid", invoiceid);
                invjson.put("companyid", companyid);
                List<Object> receiptdata = accInvoiceDAOobj.getReceiptLinkedToInvoice(invjson);
                for (Object object : receiptdata) {
                    invjson = new JSONObject();
                    invjson.put("invoiceid", invoiceid);
                    invjson.put("companyid", companyid);
                    Object[] list = (Object[]) object;
                    if (list[1] != null &&  list[1].toString().equalsIgnoreCase("1") && !isRCMapplicable) {
                        /**
                         * If Invoice link to advance then need to post GST
                         * adjustment JE
                         */
                        String receiptNo = list[2].toString();
                        KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), list[0].toString());
                        Receipt receipt = (Receipt) receiptObj.getEntityList().get(0);
                        Set<ReceiptAdvanceDetail> advanceDetails = receipt.getReceiptAdvanceDetails();
                        if (!advanceDetails.isEmpty()) {
                            jedata = new JSONObject();
                            jedata.put("memo", "GST Adjustment posted against Advance Receipt '" + receiptNo + "' linked to Invoice '" + invoiceno + "'");
                            jedata.put("auditmessage", reqParams.optString("GST Adjustment"));
                            detailsArr = new JSONArray();
                            Set<JournalEntryDetail> detail = new HashSet();
                            for (ReceiptAdvanceDetail advanceDetail : advanceDetails) {
                                String adId = advanceDetail.getId();
                                /**
                                 * Get term details from Advance
                                 */
                                JSONObject jSONObject = new JSONObject();
                                jSONObject.put("adId", adId);
                                KwlReturnObject kro = accReceiptDAOobj.getAdvanceDetailsTerm(jSONObject);
                                List<ReceiptAdvanceDetailTermMap> advanceDetailTermMaps = kro.getEntityList();
                                int srcount = 1;
                                double totalpercentage = 0d;
                                for (ReceiptAdvanceDetailTermMap receiptAdvanceDetailTermMap : advanceDetailTermMaps) {
                                    totalpercentage += receiptAdvanceDetailTermMap.getPercentage();
                                }
                                for (ReceiptAdvanceDetailTermMap receiptAdvanceDetailTermMap : advanceDetailTermMaps) {
                                    double termamount = 0d;//receiptAdvanceDetailTermMap.getTermamount();
                                    double percentage = receiptAdvanceDetailTermMap.getPercentage();
                                    String gstAdvpayableAcc = receiptAdvanceDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount().getID();
                                    String gstAcc = receiptAdvanceDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID();
                                    /**
                                     * Calculate Amount Formula is written based
                                     * on calculate gst amount by applying
                                     * percentage on amount excluding tax amount
                                     * Example : Advance amount = 1000+50 =1050
                                     * Invoice Amount = 500+25=525 Linking JE
                                     * amount = Calculate GST amount on 500 not
                                     * on 525
                                     */
                                    double amtWithoutTax = (amountdueinbase * 100 / (100 + totalpercentage));
                                    double jeamount = amtWithoutTax * percentage / 100;
                                    termamount = authHandler.round(jeamount, companyid);
                                    detailObj = new JSONObject();
                                    detailObj.put("amount", termamount);
                                    detailObj.put("accountid", gstAcc);
                                    detailObj.put("debit", true);
                                    detailObj.put("description", "");
                                    detailsArr.put(detailObj);
                                    detailObj = new JSONObject();
                                    detailObj.put("amount", termamount);
                                    detailObj.put("accountid", gstAdvpayableAcc);
                                    detailObj.put("debit", false);
                                    detailObj.put("description", "");
                                    detailsArr.put(detailObj);
                                }
                            }
                            jedata.put("details", detailsArr);
                            journalEntry = journalEntryModuleServiceobj.saveJournalEntryRemoteApplicationJson(reqParams, jedata);
                            invjson.put("gstjeid", journalEntry.getID());
                        }
                    }

                    /**
                     * Save Receipt-Invoice-TDS/TCS JE
                     */
                    invjson.put("receiptid", list[0].toString());
                    invjson.put("journalid",tdsJEID);
                    invjson.put("invoiceamountdue", amountdue);
                    invjson.put("invoiceamountdueinbase", amountdueinbase);
                    accReceiptDAOobj.saveReceiptInvoiceJEMapping(invjson);
                }

                /**
                 * Update invoice amount due
                 */
                invjson.put(Constants.invoiceamountdue, 0.0);
                invjson.put(Constants.invoiceamountdueinbase, 0.0);
                KwlReturnObject result = accInvoiceDAOobj.updateInvoice(invjson, new HashSet());
            }
        }
        return new JSONObject().put("JENos", JENos);
    }
}
