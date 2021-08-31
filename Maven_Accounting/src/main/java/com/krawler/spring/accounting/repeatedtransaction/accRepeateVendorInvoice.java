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
package com.krawler.spring.accounting.repeatedtransaction;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.entitygst.AccEntityGstDao;
import com.krawler.spring.accounting.entitygst.AccEntityGstService;
import com.krawler.spring.accounting.goodsreceipt.GoodsReceiptConstants;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptController;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.goodsreceipt.service.accGoodsReceiptModuleService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.JournalEntryConstants;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.vendor.accVendorControllerCMNService;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
//import java.util.GregorianCalendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accRepeateVendorInvoice extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accGoodsReceiptDAO accGoodsReceiptDAO;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private MessageSource messageSource;
    private accJournalEntryDAO accJournalEntryobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private authHandlerDAO authHandlerDAOObj;
    private auditTrailDAO auditTrailObj;
    private accGoodsReceiptModuleService accGoodsReceiptModuleServiceObj;
    private fieldDataManager fieldDataManagercntrl;
    private AccEntityGstService accEntityGstService;
    private accVendorControllerCMNService accVendorcontrollerCMNService;
    private AccEntityGstDao accEntityGstDao;
    private accCurrencyDAO accCurrencyDAOobj;
    
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
     public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
    public void setaccDiscountDAO(accDiscountDAO accDiscountobj) {
        this.accDiscountobj = accDiscountobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setAccGoodsReceiptDAO(com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO accGoodsReceiptDAO) {
        this.accGoodsReceiptDAO = accGoodsReceiptDAO;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setAccGoodsReceiptModuleServiceObj(accGoodsReceiptModuleService accGoodsReceiptModuleServiceObj) {
        this.accGoodsReceiptModuleServiceObj = accGoodsReceiptModuleServiceObj;
    }
    public void setAccEntityGstService(AccEntityGstService accEntityGstService) {
        this.accEntityGstService = accEntityGstService;
    }

    public void setaccVendorcontrollerCMNService(accVendorControllerCMNService accVendorcontrollerCMNService) {
        this.accVendorcontrollerCMNService = accVendorcontrollerCMNService;
    }

    public void setAccEntityGstDao(AccEntityGstDao accEntityGstDao) {
        this.accEntityGstDao = accEntityGstDao;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public ModelAndView repeateVendorInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        GoodsReceipt goodsReceipt = null;
        String msg = "", failed = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;

        try {
            HashMap<String, Object> mailParams = new HashMap<String, Object>();
            String croneName = Constants.Recurring_Vendor_Invoice_Crone_Name;

            String croneID = Constants.Recurring_Vendor_Invoice_Crone_ID;


            SimpleDateFormat df = new SimpleDateFormat(Constants.yyyyMMdd);

            Date currentDate = df.parse(df.format(new Date()));
            
            //CODE TO SEND NOTIFICATION MAIL FOR REPEATE VENDOR INVOICE
//            GregorianCalendar gc = new GregorianCalendar(); //It returns actual Date objetc
//            gc.setTime(currentDate);
//            gc.add(Calendar.DAY_OF_YEAR, -1);
//            Date prevDate = gc.getTime();
//            prevDate = df.parse(df.format(prevDate));   //Previous Date before the recurring.
            
            boolean isCroneExecutedForCurrentDay = accCommonTablesDAO.isCroneExecutedForCurrentDay(croneID, currentDate);
            
            if (isCroneExecutedForCurrentDay) {            
                msg = "This Crone is executed for today,so it cannot be hit again";
            } else {
//                status = txnManager.getTransaction(def);
                HashMap<String, Object> requestParams = new HashMap<String, Object>();//AccountingManager.getGlobalParams(request);
                
                String grnumber = "";
                String loggedUserId = "";
                GoodsReceipt gr = null;
                KwlReturnObject repeategrdetails = accGoodsReceiptDAO.getRepeatePurchaseInvoiceNo(currentDate);
                List grlist = repeategrdetails.getEntityList();
                Iterator gritr = grlist.iterator();
                while (gritr.hasNext()) {
                    try {
                        gr = (GoodsReceipt) gritr.next();
                        grnumber = gr != null ? (gr.getGoodsReceiptNumber()) : "";
                        loggedUserId = gr != null ? (gr.getCreatedby().getUserID()) : "";
                        if (!StringUtil.isNullOrEmpty(loggedUserId)) {
                            RepeatedInvoices repeateGR=gr.getRepeateInvoice();
                            Date expireDate = repeateGR.getExpireDate();
                            Date nextDate = repeateGR.getNextDate();
                            Calendar c = Calendar.getInstance();
                            c.setTime(nextDate);
                            c.add(Calendar.DATE, repeateGR.getIntervalUnit()); 
                            Date nextToNextDate = df.parse(df.format(c.getTime()));
                            if ((!(expireDate.before(nextToNextDate)) && repeateGR.getInvoiceAdvanceCreationDate().equals(currentDate) && repeateGR.getIntervalUnit()==1 && StringUtil.equalIgnoreCase("day", repeateGR.getIntervalType())) || repeateGR.getPrevDate().equals(currentDate)) {
                            String htmlMailContent = "<br/>Recurring Purchase Invoice <b>\"" + grnumber + "\"</b> will be post tomorrow.<br/>";
                            htmlMailContent += "<br/>If you want, you can deactivate it from Recurring Purchase Invoice Report.<br/>";
                            mailParams.put("htmlMailContent", htmlMailContent);

                            String plainMailContent = "\nRecurring Purchase Invoice \n" + grnumber + "\nwill be post tomorrow.\n";
                            plainMailContent += "\nIf you want, you can deactivate it from Recurring Purchase Invoice Report.\n";
                            mailParams.put("plainMailContent", plainMailContent);

                            mailParams.put("loginUserId", loggedUserId);
                            SendMail(mailParams);    //Notification Mail 
                        }
                }
                    } catch (Exception ex) {
                        failed = "Recurring notification mail failed.";
                    }
                }//while
                
                // For Recurring Invoices
                KwlReturnObject repeateInvcs = accGoodsReceiptDAO.getRepeateVendorInvoices(requestParams);
                KwlReturnObject excludedInvoicesObj = accGoodsReceiptDAO.getExcludedInvoices(requestParams);
                List ExcludedIDlist = excludedInvoicesObj.getEntityList();
                List list = repeateInvcs.getEntityList();
                Iterator Excludeditr = ExcludedIDlist.iterator();
                Iterator itr = list.iterator();
                boolean excludeFlag = false;
                while (itr.hasNext()) {
                      String entryno = "";
                      User adminUser = null;
                      status = txnManager.getTransaction(def);
                      goodsReceipt = (GoodsReceipt) itr.next();
                      int contractstatus = 0;
                      excludeFlag = false;
                      try {
                          GoodsReceipt repeatedInvoice = repeateInvoice(goodsReceipt);
                          adminUser = repeatedInvoice.getCompany().getCreator()!=null ? repeatedInvoice.getCompany().getCreator() : repeatedInvoice.getCreatedby();
                          entryno = repeatedInvoice.getGoodsReceiptNumber();
                          msg += repeatedInvoice.getGoodsReceiptNumber() + ",";
                          if (goodsReceipt.getRepeateInvoice() != null) {
                              updateRepeateInfo(goodsReceipt.getRepeateInvoice());
                          }
                      } catch (Exception e) {
                          failed += goodsReceipt.getGoodsReceiptNumber() + "[" + goodsReceipt.getID() + "]: " + e.getMessage() + ";";
                          System.out.println("\n***** Exception in Recurring Invoices Entry:*****\n" + failed);
                          txnManager.rollback(status); 
                      }
                      //Add Audit Trail Entry for PI
                      auditTrailObj.insertRecurringAuditLog(AuditAction.ADD_RECURRING_PURCHASE_INVOICE_ENTRY, "System has generated a recurring Purchase Invoice - " + entryno, request, entryno, adminUser);
                      txnManager.commit(status);
                  }
                
                 status = txnManager.getTransaction(def);
                if (msg.length() > 0) {
                    msg = msg.substring(0, msg.length() - 1);
                    msg = messageSource.getMessage("acc.field.Generatedinvoices", null, RequestContextUtils.getLocale(request)) + " : " + msg + ".";
                } else {
                    msg = messageSource.getMessage("acc.field.Noanyscheduledrecurringinvoicetogenerate", null, RequestContextUtils.getLocale(request));
                }
                
                saveCroneDetails(croneID, croneName, currentDate);
                txnManager.commit(status);
            }
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            try {
                 if (status != null) {
                    txnManager.rollback(status);
                }
            } catch (Exception txEx) {
                msg += ex.getMessage();
            }
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                //jobj.put("failed", failed);
                jobj.put("datetime", new Date());
                jobj.put("msg", msg);
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    public void saveCroneDetails(String croneID, String croneName, Date hittingDate) throws ServiceException {
        try {
            accCommonTablesDAO.saveCroneDetails(croneID, croneName, hittingDate);
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateRepeateInfo(RepeatedInvoices rinfo) throws ServiceException, SessionExpiredException {
//        Date nextDate = rinfo.getNextDate();
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(nextDate);
//        int interval = rinfo.getIntervalUnit();
//        String intervalType = rinfo.getIntervalType();
//        if (intervalType.equalsIgnoreCase("Days")) {
//            cal.add(Calendar.DATE, interval);
//        } else if (intervalType.equalsIgnoreCase("Week")) {
//            cal.add(Calendar.DATE, interval * 7);
//        } else if (intervalType.equalsIgnoreCase("Month")) {
//            cal.add(Calendar.MONTH, interval);
//        }
        Date nextDate = RepeatedInvoices.calculateNextDate(rinfo.getNextDate(), rinfo.getIntervalUnit(), rinfo.getIntervalType());
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("NoOfRemainpost", rinfo.getNoOfRemainInvoicespost() + 1);
        dataMap.put("id", rinfo.getId());
        dataMap.put("nextDate", nextDate);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        c.setTime(nextDate);
        c.add(Calendar.DATE, (-rinfo.getAdvanceNoofdays())); // Subtracting advance no of days to next date
        String output = sdf.format(c.getTime());
        System.out.println(output);
        dataMap.put("advanceDate", c.getTime());
        c.add(Calendar.DATE, -1); // Subtracting one day and saving it in prev date for mail
        output = sdf.format(c.getTime());
        System.out.println("Prev day "+output);
        dataMap.put("prevDate", c.getTime());        
        accGoodsReceiptDAO.saveRepeateInvoiceInfo(dataMap);
    }

    
    public GoodsReceipt repeateInvoice(GoodsReceipt invoice) throws ServiceException {
        GoodsReceipt repeatedInvoice = null;
        Map<String, Object> invjson = new HashMap<String, Object>();
        Map<Inventory, List<HashMap>> FinalTerm = new HashMap<Inventory, List<HashMap>>();
        try {
            String companyid = invoice.getCompany().getCompanyID();
            String currencyid = invoice.getCurrency().getCurrencyID();
            String Memo = "";
            Date creationDate = null;
            String companyTZDiff = invoice.getCompany().getTimeZone()!=null?invoice.getCompany().getTimeZone().getDifference():"+00:00";
            DateFormat comdf = authHandler.getCompanyTimezoneDiffFormat(companyTZDiff);
            SimpleDateFormat sd = new SimpleDateFormat("MMMM d, yyyy");
            Calendar cal = Calendar.getInstance();
            String calString = authHandler.getDateOnlyFormat().format(invoice.getRepeateInvoice().getNextDate());
            Date calDate = authHandler.getDateOnlyFormat().parse(calString);
            Date BillDate = calDate;
            creationDate = sd.parse(comdf.format(BillDate));
            
            invjson.put(GoodsReceiptConstants.VENDORID, invoice.getVendor() == null ? invoice.getVendorEntry().getAccount().getID() : invoice.getVendor().getID());    //vendorid 
            String nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_GOODSRECEIPT);
            boolean nextInv = false;
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            
            SequenceFormat prevSeqFormat = null;
            String nextAutoNoInt = "";
            String datePrefix = "";
            String dateafterPrefix = "";
            String dateSuffix = "";
            String entryNumber = "";//nextAutoNo;
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) returnObject.getEntityList().get(0);
            if (extraPref != null && extraPref.isDefaultsequenceformatforrecinv() && invoice.getSeqformat() != null && invoice.getSeqformat().isIsactivate() ) {
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_GOODSRECEIPT, invoice.getSeqformat().getID(), false, creationDate);
                nextAutoNo = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                
                String invoiceSeqFormatId = invoice.getSeqformat().getID();
                invjson.put(Constants.SEQFORMAT, invoiceSeqFormatId);
                invjson.put(Constants.SEQNUMBER, nextAutoNoInt);
                invjson.put(Constants.DATEPREFIX, datePrefix);
                invjson.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                invjson.put(Constants.DATESUFFIX, dateSuffix);
                entryNumber = nextAutoNo;
            } else {

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("parentInvoiceId", invoice.getID());
                KwlReturnObject details = accGoodsReceiptDAO.getRepeateVendorInvoicesDetails(requestParams);
                List detailsList = details.getEntityList();
                int repInvoiceCount = detailsList.size() + Constants.RECURRING_INVOICE_01_APPEND_START_FROM;
                while (nextInv == false) {
                    entryNumber = invoice.getGoodsReceiptNumber() + "-" + repInvoiceCount;
                    details = accGoodsReceiptDAO.getGoodsReceiptCount(entryNumber, companyid);
                    int nocount = details.getRecordTotalCount();
                    if (nocount > 0) {
                        repInvoiceCount++;
                        continue;
                    } else {
                        nextInv = true;
                    }
                }
            }
            int noOfRemainpost = invoice.getRepeateInvoice().getNoOfRemainInvoicespost();
            noOfRemainpost++;
            try {
                HashMap<String, Object> requestParamsMemo = new HashMap<String, Object>();
                requestParamsMemo.put("repeatedJEMemoID", invoice.getRepeateInvoice().getId());
                requestParamsMemo.put("noOfJERemainpost", noOfRemainpost);
                requestParamsMemo.put("columnName", "RepeatedInvoiceID");
                KwlReturnObject RepeatedJEMemo = accJournalEntryobj.getRepeateJEMemo(requestParamsMemo);
                RepeatedJEMemo RM = (RepeatedJEMemo) RepeatedJEMemo.getEntityList().get(0);
                Memo = RM.getMemo();
                if (StringUtil.isNullOrEmpty(Memo)) {
                    Memo = invoice.getMemo();
                }
            } catch (Exception ex) {
                Memo = invoice.getMemo();
            }
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            invjson.put(GoodsReceiptConstants.ENTRYNUMBER, entryNumber);
            invjson.put("autogenerated", nextAutoNo.equals(entryNumber));
            invjson.put("memo", Memo);
            invjson.put("billto", invoice.getBillTo());
            invjson.put("shipaddress", invoice.getShipTo());
            invjson.put("createdby", (invoice.getCreatedby() != null) ? invoice.getCreatedby().getUserID() : "");
            invjson.put("modifiedby", (invoice.getCreatedby() != null) ? invoice.getCreatedby().getUserID() : "");
            invjson.put("createdon", createdon);
            invjson.put("updatedon", updatedon);
            invjson.put("companyid", companyid);
            invjson.put("currencyid", currencyid);
            invjson.put("externalCurrencyRate", invoice.getExternalCurrencyRate());
            if (invoice.getExchangeRateDetail() != null) {
                invjson.put("erdid", invoice.getExchangeRateDetail().getID());
            }
//            if (invoice.getSalesperson() != null) {
//                invjson.put("salesPerson", invoice.getSalesperson().getUserID());
//            }
            invjson.put("fob", invoice.getFob());
            invjson.put("isOpeningBalenceInvoice", invoice.isIsOpeningBalenceInvoice());
            invjson.put("conversionRateFromCurrencyToBase", true);
            invjson.put("originalOpeningBalanceAmount", invoice.getOriginalOpeningBalanceAmount());
            invjson.put("openingBalanceAmountDue", invoice.getOpeningBalanceAmountDue());
            // Store Invoice amount in base currency
            invjson.put(Constants.originalOpeningBalanceBaseAmount, invoice.getOriginalOpeningBalanceBaseAmount());
            invjson.put(Constants.openingBalanceBaseAmountDue, invoice.getOpeningBalanceBaseAmountDue());

            invjson.put("exchangeRateForOpeningTransaction", invoice.getExchangeRateForOpeningTransaction());
            invjson.put("posttext", invoice.getPostText());
            invjson.put("isfavourite", invoice.isFavourite());
            invjson.put("isFixedAsset", invoice.isFixedAssetInvoice());
            invjson.put("incash", invoice.isCashtransaction());
            invjson.put(Constants.isApplyTaxToTerms, invoice.isApplyTaxToTerms());
//            invjson.put("isLeaseFixedAsset", invoice.isFixedAssetLeaseInvoice());
            //No need to handle Calendar Instance for bill date & due date. Because it is handled later.
            
            //We need to add 1 day in bill date to avoid timezone issue. 
            //Timezone problem getting B'cz we are not maintainig Time. ERP-8708
//            Calendar caldr = Calendar.getInstance();
//            caldr.setTime(cal.getTime());
//            caldr.add(Calendar.DAY_OF_YEAR, 1);
//            Date BillDate = caldr.getTime();
            
            Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(BillDate);
            if (invoice.getVendor() != null) {
                if (invoice.getVendor().getDebitTerm() != null) {
                    int termDays = invoice.getVendor().getDebitTerm().getTermdays();
                    dueDate.add(Calendar.DATE, termDays);
                }
            }
            if(invoice.getShipDate()!=null){
                invjson.put("shipdate", invoice.getShipDate());
            }
            invjson.put("duedate", sd.parse(comdf.format(dueDate.getTime())));


            Discount DSC = invoice.getDiscount();
            if (DSC != null) {
                JSONObject discjson = new JSONObject();
                discjson.put("discount", DSC.getDiscount());
                discjson.put("inpercent", DSC.isInPercent());
                discjson.put("originalamount", DSC.getOriginalAmount());
                discjson.put("companyid", companyid);
                KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                Discount discount = (Discount) dscresult.getEntityList().get(0);
                invjson.put("discountid", discount.getID());
            }
            
            /*
            For recurring invoice, provided discount amount and discount amount in base as of orgional invoice 
            to the invoice that will be generated from recurring process. 
            */
            invjson.put(Constants.discountAmount, invoice.getDiscountAmount());
            invjson.put(Constants.discountAmountInBase, invoice.getDiscountAmountInBase());

            // Create Journal Entry
            String jeentryNumber = "";
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            boolean jeautogenflag = true;
            synchronized (this) {
                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", "autojournalentry");
                JEFormatParams.put("companyid", companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, creationDate);
                jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date Prefix Part
                jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                jeSeqFormatId = format.getID();
            }
            JournalEntry OLD_JE = invoice.getJournalEntry();
            Map<String, Object> jeDataMap = new HashMap<String, Object>();
            jeDataMap.put("DontCheckYearLock", true);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
            jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
            jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
            jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
            jeDataMap.put("entrydate", creationDate);
            jeDataMap.put("companyid", companyid);
            jeDataMap.put("memo", OLD_JE.getMemo());
            if (OLD_JE.getCostcenter() != null) {
                jeDataMap.put("costcenterid", OLD_JE.getCostcenter().getID());
            }
            jeDataMap.put("currencyid", currencyid);
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            String jeid = journalEntry.getID();
            invjson.put("journalentryid", jeid);
            jeDataMap.put("jeid", jeid);
            if (OLD_JE.getAccBillInvCustomData() != null) {
                int NoOFRecords = accJournalEntryobj.saveCustomDataForRecurringJE(jeid, OLD_JE.getID(), true);
            }

//            Set JE_DETAILS = invoice.getJournalEntry().getDetails();
//            HashSet<JournalEntryDetail> jeDetails = new HashSet<JournalEntryDetail>();
//            HashMap<String, String> oldNnewJEDid = new HashMap<String, String>();
//            Iterator jeditr = JE_DETAILS.iterator();
//            while (jeditr.hasNext()) {
//                JournalEntryDetail OLD_JED = (JournalEntryDetail) jeditr.next();
//                JSONObject jedjson = new JSONObject();
//                jedjson.put("srno", OLD_JED.getSrno());
//                jedjson.put("companyid", OLD_JED.getCompany().getCompanyID());
//                jedjson.put("amount", OLD_JED.getAmount());
//                jedjson.put("accountid", OLD_JED.getAccount().getID());
//                jedjson.put("debit", OLD_JED.isDebit());
//                jedjson.put("jeid", jeid);
//                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                if (OLD_JED.getAccJEDetailCustomData() != null) {
//                    int NoOFRecords = accJournalEntryobj.saveCustomDataForRecurringJE(jed.getID(), OLD_JED.getID(), false);
//                }
//                JSONObject jedjson1 = new JSONObject();
//                jedjson1.put("jedid", jed.getID());
//                jedjson1.put("accjedetailcustomdata", jed.getID());
//                KwlReturnObject jedresult1 = accJournalEntryobj.updateJournalEntryDetails(jedjson1);
//                JournalEntryDetail jed1 = (JournalEntryDetail) jedresult1.getEntityList().get(0);
//                jeDetails.add(jed1);
//                oldNnewJEDid.put(OLD_JED.getID(), jed.getID());
//            }
//            jeDataMap.put("accjecustomdataref", jeid);
//            jeDataMap.put("jedetails", jeDetails);
//            jeDataMap.put("externalCurrencyRate", OLD_JE.getExternalCurrencyRate());
//            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
//            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
//
//            JournalEntryDetail CUST_JED = invoice.getVendorEntry();
//            if (CUST_JED != null) {
//                invjson.put(GoodsReceiptConstants.VENDORENTRYID, oldNnewJEDid.get(CUST_JED.getID()));    //
//            }
//
//            JournalEntryDetail SHIP_JED = invoice.getShipEntry();
//            if (SHIP_JED != null) {
//                invjson.put("shipentryid", oldNnewJEDid.get(SHIP_JED.getID()));
//            }
//
//            JournalEntryDetail OTHER_JED = invoice.getOtherEntry();
//            if (OTHER_JED != null) {
//                invjson.put("otherentryid", oldNnewJEDid.get(OTHER_JED.getID()));
//            }
//
//            Tax TAX = invoice.getTax();
//            if (TAX != null) {
//                invjson.put("taxid", TAX.getID());
//            }
//
//            JournalEntryDetail TAX_JED = invoice.getTaxEntry();
//            if (TAX_JED != null) {
//                invjson.put("taxentryid", oldNnewJEDid.get(TAX_JED.getID()));
//            }
//            Term termid = invoice.getTermid();
//            if (termid != null) {
//                invjson.put("termid", termid.getID());
//            }
//            Account accountid = invoice.getAccount();
//            if (accountid != null) {
//                invjson.put("accountid", accountid.getID());
//            }
//            BillingShippingAddresses billshipAddressid = invoice.getBillingShippingAddresses();
//            if (billshipAddressid != null) {
//                invjson.put("billshipAddressid", billshipAddressid.getID());
//            }
//
//            invjson.put("approvallevel", invoice.getApprovallevel());
//            invjson.put("shipvia", invoice.getShipvia());
////            invjson.put("partialinv", invoice.isPartialinv());
//             invjson.put(Constants.Checklocktransactiondate, calDate);
//            KwlReturnObject result = accGoodsReceiptDAO.addGoodsReceipt(invjson);
//            repeatedInvoice = (GoodsReceipt) result.getEntityList().get(0);//Create Invoice without invoice-details.
//            
//            jeDataMap.put("transactionModuleid", Constants.Acc_Vendor_Invoice_ModuleId);
//            jeDataMap.put("transactionId", repeatedInvoice.getID());
//            jeDataMap.put(JournalEntryConstants.JEID,journalEntry.getID());
//            KwlReturnObject updatejeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//update journalentry
//            JournalEntry updatejournalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            //Invoice Contract Mapping
//            Set INVOICE_CONTRACT_MAPPING = invoice.getContractMappings();
//            HashSet<InvoiceContractMapping> invoiceContract = new HashSet<InvoiceContractMapping>();
//            Iterator itrInvoiceContract = INVOICE_CONTRACT_MAPPING.iterator();
//            while (itrInvoiceContract.hasNext()) {
//                InvoiceContractMapping oldInvoiceComMapobj = (InvoiceContractMapping) itrInvoiceContract.next();
//                InvoiceContractMapping newInvoiceComMapobj = new InvoiceContractMapping();
//                newInvoiceComMapobj.setCompany(oldInvoiceComMapobj.getCompany());
//                newInvoiceComMapobj.setContract(oldInvoiceComMapobj.getContract());
//                newInvoiceComMapobj.setDeliveryOrder(oldInvoiceComMapobj.getDeliveryOrder());
//                newInvoiceComMapobj.setInvoice(repeatedInvoice);
//                invoiceContract.add(newInvoiceComMapobj);
//            }
            
            Tax TAX = invoice.getTax();
            if (TAX != null) {
                invjson.put("taxid", TAX.getID());
            }

          
            Term termid = invoice.getTermid();
            if (termid != null) {
                invjson.put("termid", termid.getID());
            }
            Account accountid = invoice.getAccount();
            if (accountid != null) {
                invjson.put("accountid", accountid.getID());
            }
            String addressID = "";
            if (extraPref!=null && extraPref.isPickAddressFromMaster()) {
                Map<String, Object> addressParams = new HashMap<String, Object>();
                addressParams = AccountingAddressManager.getDefaultVendorAddressParams(invoice.getVendor().getID(), companyid, accountingHandlerDAOobj);
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                if (bsa != null) {
                    addressID = bsa.getID();
                }
            } else {
                addressID = invoice.getBillingShippingAddresses() != null ? invoice.getBillingShippingAddresses().getID() : "";
            }
            if (!StringUtil.isNullOrEmpty(addressID)) {
                invjson.put("billshipAddressid", addressID);
            }

            invjson.put("approvallevel", invoice.getApprovallevel());
            invjson.put("shipvia", invoice.getShipvia());
            invjson.put("creationDate", creationDate);
//            invjson.put("partialinv", invoice.isPartialinv());
            invjson.put(Constants.Checklocktransactiondate, calDate);
            invjson.put(Constants.SUPPLIERINVOICENO, invoice.getSupplierInvoiceNo());
            if (invoice.getMasterAgent() != null) {
                invjson.put("agent", invoice.getMasterAgent().getID());
            }
            if (invoice.getMasterSalesPerson() != null) {
                invjson.put("salesPerson", invoice.getMasterSalesPerson());
            }
            invjson.put(Constants.termsincludegst,invoice.getTermsincludegst());
            invjson.put(Constants.isCreditable,invoice.isIsCreditable());//ERP-41548
            KwlReturnObject result = accGoodsReceiptDAO.addGoodsReceipt(invjson);
            repeatedInvoice = (GoodsReceipt) result.getEntityList().get(0);//Create Invoice without invoice-details.
            Set INVOICE_DETAILS = invoice.getRows();
            HashSet<GoodsReceiptDetail> invcdetails = new HashSet<GoodsReceiptDetail>();
            Iterator itr = INVOICE_DETAILS.iterator();

            /**
             * Check GST history for customer or product tax class changed nor
             * not This case apply for India only.
             */
            boolean isGSThisChanged = false;
            int uniqueCase = 0;
            JSONArray dimArr = new JSONArray();
            /**
             * Indian GST related code which includes logic for history.
             */
            if (repeatedInvoice.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                String productids = "";
                while (itr.hasNext()) {
                    GoodsReceiptDetail ivd = (GoodsReceiptDetail) itr.next();
                    productids += "'" + ivd.getInventory().getProduct().getID() + "',";
                }
                /**
                 * Check whether GST history changed or not.
                 */
                JSONObject tempParams = new JSONObject();
                tempParams.put("masterid", invoice.getVendor().getID());
                tempParams.put("isCustomer", false);
                tempParams.put("productids", productids);
                tempParams.put("applydate", authHandler.getDateOnlyFormat().format(invoice.getCreationDate()));
                tempParams.put("transactiondate", authHandler.getDateOnlyFormat().format(new Date()));
                JSONObject temp = accEntityGstService.getGSTFieldsChangedStatus(tempParams);
                isGSThisChanged = temp.optBoolean("isdatachanged", false);

                JSONObject jSONObject = new JSONObject();
                if (isGSThisChanged) {
                    /**
                     * If History changed then get customer GST details as per
                     * current date.
                     */
                    tempParams.put("vendorid", invoice.getVendor().getID());
                    tempParams.put("returnalldata", true);
                    tempParams.put("isfortransaction", true);
                    tempParams.put("transactiondate", authHandler.getDateOnlyFormat().format(new Date()));
                    jSONObject = accVendorcontrollerCMNService.getVendorGSTHistory(tempParams);
                    jSONObject = jSONObject.optJSONArray("data").optJSONObject(0);
                    uniqueCase = jSONObject.optInt("uniqueCase");
                } else {
                    /**
                     * IF history does not changed then get customer details
                     * from parent document.
                     */
                    jSONObject.put("refdocid", invoice.getID());
                    fieldDataManagercntrl.getGSTDocumentHistory(jSONObject);
                }
                /**
                 * Save Customer GST details at document level.
                 */
                jSONObject.remove("gstdochistoryid");
                jSONObject.put("docid", repeatedInvoice.getID());
                jSONObject.put("moduleid", repeatedInvoice.isFixedAssetInvoice() ? Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId : Constants.Acc_Vendor_Invoice_ModuleId);
                fieldDataManagercntrl.createRequestMapToSaveDocHistory(jSONObject);
                if (isGSThisChanged) {
                    /**
                     * Create dimension array to calculate GST for each product
                     * i.e Using State and Entity value.
                     */
                    dimArr=createDimensionArrayToCalculateGSTForInvoice(invoice, companyid);
                }
            }
            String vendorAccountID= repeatedInvoice.getVendor().getAccount().getID();
            //Invoice Details
            double finaltaxamt=0d;
            HashSet<JournalEntryDetail> jeDetails = new HashSet<JournalEntryDetail>();
            itr = INVOICE_DETAILS.iterator();
            while (itr.hasNext()) {
                GoodsReceiptDetail ivd = (GoodsReceiptDetail) itr.next();
                GoodsReceiptDetail row = new GoodsReceiptDetail();
                row.setSrno(ivd.getSrno());
                row.setPurchaseorderdetail(ivd.getPurchaseorderdetail());
                row.setCompany(ivd.getCompany());
                row.setRate(ivd.getRate());
                row.setGoodsReceipt(repeatedInvoice);
//                row.setPartamount(ivd.getPartamount());
                row.setDescription(ivd.getDescription());
//                row.setDeferredJeDetailId(ivd.getDeferredJeDetailId());
                row.setPurchaseorderdetail(ivd.getPurchaseorderdetail());
                row.setVendorQuotationDetail(ivd.getVendorQuotationDetail());
                row.setRowTaxAmount(ivd.getRowTaxAmount());
                row.setRowTermTaxAmount(ivd.getRowTermTaxAmount());
                row.setRowTermAmount(ivd.getRowTermAmount());   //ERP-38046 - Set Term amount
                row.setWasRowTaxFieldEditable(ivd.isWasRowTaxFieldEditable());
                row.setInvstoreid(ivd.getInvstoreid());
                row.setInvlocid(ivd.getInvlocid());
                row.setPriceSource(ivd.getPriceSource());
                
                if (ivd.getTax() != null) {
                    row.setTax(ivd.getTax());
                }
                if (ivd.getRateincludegst() > 0) {
                    row.setRateincludegst(ivd.getRateincludegst());
                }
                Inventory ID_INVENTORY = ivd.getInventory();
                if (ID_INVENTORY != null) {
                    JSONObject inventoryjson = new JSONObject();
                    inventoryjson.put("productid", ID_INVENTORY.getProduct().getID());
                    inventoryjson.put("quantity", ID_INVENTORY.getQuantity());
                    inventoryjson.put("baseuomquantity", ID_INVENTORY.getBaseuomquantity());
                    inventoryjson.put("baseuomrate", ID_INVENTORY.getBaseuomrate());
                    if (ID_INVENTORY.getUom() != null) {
                        inventoryjson.put("uomid", ID_INVENTORY.getUom().getID());
                    }
                    inventoryjson.put("description", ID_INVENTORY.getDescription());
                    inventoryjson.put("carryin", false);
                    inventoryjson.put("defective", false);
                    inventoryjson.put("newinventory", false);
                    inventoryjson.put("companyid", companyid);
                    inventoryjson.put("updatedate", ID_INVENTORY.getUpdateDate());
                    KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                    Inventory inventory = (Inventory) invresult.getEntityList().get(0);
                    row.setInventory(inventory);
                }

                Discount ID_DISCOUNT = ivd.getDiscount();
                if (ID_DISCOUNT != null) {
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", ID_DISCOUNT.getDiscount());
                    discjson.put("inpercent", ID_DISCOUNT.isInPercent());
                    discjson.put("originalamount", ID_DISCOUNT.getOriginalAmount());
                    discjson.put("companyid", companyid);
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                }
                if (ivd.getPurchaseJED() != null) {
                    JournalEntryDetail OLD_JED = ivd.getPurchaseJED();
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jeDetails.size() + 1);
                    jedjson.put("companyid", OLD_JED.getCompany().getCompanyID());
                    jedjson.put("amount", OLD_JED.getAmount());
                    jedjson.put("accountid", OLD_JED.getAccount().getID());
                    jedjson.put("debit", OLD_JED.isDebit());
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    row.setPurchaseJED(jed);
                    jeDetails.add(jed);
                    if (OLD_JED.getAccJEDetailCustomData() != null) {
                        int NoOFRecords = accJournalEntryobj.saveCustomDataForRecurringJE(jed.getID(), OLD_JED.getID(), false);
                    }
                }
                
                 if (ivd.getTax() != null) {
                    JSONObject jedjson = new JSONObject();
                    jedjson = new JSONObject();
                    jedjson.put("srno", jeDetails.size() + 1);
                    jedjson.put("companyid", ivd.getCompany().getCompanyID());
                    jedjson.put("amount", ivd.getRowTaxAmount());
                    jedjson.put("accountid", ivd.getTax().getAccount().getID());
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);
                    
                    if(ivd.getRowTermTaxAmount()!=0) {
                        jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put("companyid", ivd.getCompany().getCompanyID());
                        jedjson.put("amount", ivd.getRowTermTaxAmount());
                        jedjson.put("accountid", ivd.getTax().getAccount().getID());
                        jedjson.put("debit", true);
                        jedjson.put("jeid", jeid);
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jeDetails.add(jed);
                    }
                }
                /*
                  Below piece of code has written to get the list of Map Invoice Details Term Map.
                 */
                HashSet<ReceiptDetailTermsMap> invcdetailTermMap = new HashSet<ReceiptDetailTermsMap>();
                if (extraPref.getLineLevelTermFlag() == 1) {//If line level term is applicable then we will consider GST Account
                    JSONObject jSONObject = new JSONObject();
                    List termlist = new ArrayList();
                    if (isGSThisChanged) {
                        /**
                         * If history changed then calculate Term for each
                         * product Save in the term table.
                         */
                        double quantity = ivd.getInventory().getQuantity();
                        double rate = ivd.getRate();
                        double discount = ivd.getDiscount() != null ? ivd.getDiscount().getDiscount() : 0d;
                        double subtotal = quantity * rate;
                        if (ivd.getDiscount() != null && ivd.getDiscount().isInPercent()) {
                            discount = (subtotal * ivd.getDiscount().getDiscount()) / 100;
                        }
                        subtotal = subtotal - discount;
                        HashMap<String, Object> requestParams = new HashMap();
                        requestParams.put("df", authHandler.getOnlyDateFormat());
                        jSONObject.put("productids", ivd.getInventory().getProduct().getID());
                        jSONObject.put("transactiondate", authHandler.getOnlyDateFormat().format(new Date()));
                        jSONObject.put("termSalesOrPurchaseCheck", false);
                        jSONObject.put("isFixedAsset", invoice.isFixedAssetInvoice());
                        jSONObject.put("uniqueCase", uniqueCase);
                        jSONObject.put("dimArr", dimArr);
                        jSONObject.put("companyid", companyid);
                        jSONObject = accEntityGstService.getGSTForProduct(jSONObject, requestParams);
                        if (jSONObject.optBoolean("success")) {
                            String prodTermArray = jSONObject.optJSONArray("prodTermArray").getJSONObject(0).optString("LineTermdetails");
                            String taxclass = jSONObject.optJSONArray("prodTermArray").getJSONObject(0).optString("taxclass");
                            JSONArray termarr = new JSONArray(prodTermArray);
                            double rowttermamount = 0d;
                            for (int i = 0; i < termarr.length(); i++) {
                                JSONObject termObj = termarr.optJSONObject(i);
                                JSONObject jedTermjson = new JSONObject();
                                double termamt = subtotal * termObj.optDouble("taxvalue") / 100;
                                termamt = authHandler.round(termamt, companyid);
                                rowttermamount += termamt;
                                jedTermjson.put("srno", jeDetails.size() + 1);
                                jedTermjson.put("companyid", ivd.getCompany().getCompanyID());
                                jedTermjson.put("amount", subtotal * termObj.optDouble("taxvalue") / 100);
                                jedTermjson.put("accountid", termObj.optString("accountid")); //GST Account ID
                                jedTermjson.put("debit", true);
                                jedTermjson.put("jeid", jeid);
                                KwlReturnObject jedresultobj = accJournalEntryobj.addJournalEntryDetails(jedTermjson);
                                JournalEntryDetail jedobj = (JournalEntryDetail) jedresultobj.getEntityList().get(0);
                                jeDetails.add(jedobj);
                                termObj.put("assessablevalue", subtotal);
                                termObj.put("termamount", termamt);
                                List list = mapInvoiceDetailTerms(termObj, ivd.getGoodsReceipt().getCreatedby().getUserID());
                                termlist.addAll(list);
                            }
                            FinalTerm.put(row.getInventory(), termlist);
                            jSONObject = new JSONObject();
                            jSONObject.put("taxclass", taxclass);
                            row.setRowTermAmount(rowttermamount);
                            finaltaxamt += rowttermamount;
                        }
                    } else {
                        /**
                         * Save GST details in Term table by coping from parent
                         * table.
                         */
                        HashMap<String, Object> hm = new HashMap<>();
                        hm.put("GoodsReceiptDetailid", ivd.getID());
                        KwlReturnObject rslist = accGoodsReceiptDAO.getGoodsReceiptdetailTermMap(hm);
                        if (!rslist.getEntityList().isEmpty()) {
                            Iterator rsitr = rslist.getEntityList().iterator();
                            while (rsitr.hasNext()) {
                                ReceiptDetailTermsMap idt = (ReceiptDetailTermsMap) rsitr.next();
                                JSONObject jedTermjson = new JSONObject();
                                jedTermjson.put("srno", jeDetails.size() + 1);
                                jedTermjson.put("companyid", ivd.getCompany().getCompanyID());
                                jedTermjson.put("amount", idt.getTermamount());
                                jedTermjson.put("accountid", idt.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID()); //GST Account ID
                                jedTermjson.put("debit", true);
                                jedTermjson.put("jeid", jeid);
                                KwlReturnObject jedresultobj = accJournalEntryobj.addJournalEntryDetails(jedTermjson);
                                JournalEntryDetail jedobj = (JournalEntryDetail) jedresultobj.getEntityList().get(0);
                                jeDetails.add(jedobj);

                                //Get Invoice Detail Term Map
                                List list = mapInvoiceDetailTerms(idt, row.getInventory(), idt.getCreator().getUserID(), extraPref.isAvalaraIntegration());
                                termlist.addAll(list);
                            }
                            FinalTerm.put(row.getInventory(), termlist);
                        }
                        /**
                         * Get tax class details from parent document.
                         */

                        jSONObject.put("refdocid", ivd.getID());
                        fieldDataManagercntrl.getGSTTaxClassHistory(jSONObject);
                    }
                    /**
                     * Save GST History Tax Class.
                     */
                    jSONObject.remove("taxclasshistoryid");
                    jSONObject.put("detaildocid", row.getInventory().getID());
                    jSONObject.put("moduleid", repeatedInvoice.isFixedAssetInvoice() ? Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId : Constants.Acc_Vendor_Invoice_ModuleId);
                    fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jSONObject);
                }
                invcdetails.add(row);
            }

            invjson.put(GoodsReceiptConstants.ISEXPENSETYPE, invoice.isIsExpenseType());
            HashSet<ExpenseGRDetail> expensedetails = new HashSet<ExpenseGRDetail>();
            if (invoice.isIsExpenseType()) {
                isGSThisChanged=false;
                Set expenseDetails = invoice.getExpenserows();
                Iterator expenseItr = expenseDetails.iterator();
                while (expenseItr.hasNext()) {
                    ExpenseGRDetail exp = (ExpenseGRDetail) expenseItr.next();
                    ExpenseGRDetail row = new ExpenseGRDetail();
                    row.setID(StringUtil.generateUUID());
                    row.setSrno(exp.getSrno());
                    if (exp.getAccount() != null) {
                        row.setAccount(exp.getAccount());
                    }
                    row.setRate(exp.getRate());
                    row.setAmount(exp.getAmount());
                    row.setGoodsReceipt(repeatedInvoice);
                    if (exp.getTax() != null) {
                        row.setTax(exp.getTax());
                    }
                    row.setRowTaxAmount(exp.getRowTaxAmount());

                    row.setWasRowTaxFieldEditable(exp.isWasRowTaxFieldEditable());
                    row.setCompany(exp.getCompany());
                    row.setDescription(exp.getDescription());
                    row.setIsdebit(exp.isIsdebit());
                    row.setRateExcludingGst(exp.getRateExcludingGst());

                    Discount ID_DISCOUNT = exp.getDiscount();
                    if (ID_DISCOUNT != null) {
                        JSONObject discjson = new JSONObject();
                        discjson.put("discount", ID_DISCOUNT.getDiscount());
                        discjson.put("inpercent", ID_DISCOUNT.isInPercent());
                        discjson.put("originalamount", ID_DISCOUNT.getOriginalAmount());
                        discjson.put("companyid", companyid);
                        KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                        Discount discount = (Discount) dscresult.getEntityList().get(0);
                        row.setDiscount(discount);
                    }
                    
                    if(exp.getPurchaseJED()!=null){
                        JournalEntryDetail jed=exp.getPurchaseJED();
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put("companyid", jed.getCompany()!=null?jed.getCompany().getCompanyID():companyid);
                        jedjson.put("amount", jed.getAmount());
                        jedjson.put("accountid", jed.getAccount()!=null?jed.getAccount().getID():"");
                        jedjson.put("debit", true);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description",jed.getDescription());
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JournalEntryDetail purchaseJED = (JournalEntryDetail) jedresult.getEntityList().get(0);    
                        row.setPurchaseJED(purchaseJED);
                        jeDetails.add(purchaseJED);
                    }
                    if (exp.getTax() != null) {
                        JSONObject jedjson = new JSONObject();
                        jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put("companyid", exp.getCompany().getCompanyID());
                        jedjson.put("amount", exp.getRowTaxAmount());
                        jedjson.put("accountid", exp.getTax().getAccount().getID());
                        jedjson.put("debit", true);
                        jedjson.put("jeid", jeid);
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jeDetails.add(jed);
                    }
                    expensedetails.add(row);
                }
            }
            
            JSONObject vendorjedjson = new JSONObject();
            vendorjedjson.put("srno", jeDetails.size() + 1);
            vendorjedjson.put("companyid", OLD_JE.getCompany().getCompanyID());
            if (isGSThisChanged) {
                /**
                 * Recalculate amount if GST history changed.
                 */
                vendorjedjson.put("amount", invoice.getExcludingGstAmount() + finaltaxamt);
            } else {
                vendorjedjson.put("amount", invoice.getInvoiceAmount());
            }
            vendorjedjson.put("accountid", vendorAccountID);
            vendorjedjson.put("debit", false);
            vendorjedjson.put("jeid", jeid);
            KwlReturnObject res = accJournalEntryobj.addJournalEntryDetails(vendorjedjson);
            JournalEntryDetail vendorJED = (JournalEntryDetail) res.getEntityList().get(0);
            jeDetails.add(vendorJED);
            invjson.put(GoodsReceiptConstants.VENDORENTRYID, vendorJED.getID());
            
            if (invoice.getDiscountAmount() > 0) {
                JSONObject discjedjson = new JSONObject();
                discjedjson.put("srno", jeDetails.size() + 1);
                discjedjson.put("companyid", OLD_JE.getCompany().getCompanyID());
                discjedjson.put("amount", invoice.getDiscountAmount());
                discjedjson.put("accountid", preferences.getDiscountReceived().getID());
                discjedjson.put("debit", false);
                discjedjson.put("jeid", jeid);
                KwlReturnObject descJEDres = accJournalEntryobj.addJournalEntryDetails(discjedjson);
                JournalEntryDetail descJED = (JournalEntryDetail) descJEDres.getEntityList().get(0);
                jeDetails.add(descJED);
            }
            
            HashMap<String, Object> termParam = new HashMap();
            termParam.put("invoiceid", invoice.getID());
            KwlReturnObject curresult = accGoodsReceiptDAO.getInvoiceTermMap(termParam);
            
            JSONArray invoiceTermsMapJarr = new JSONArray();
            List<ReceiptTermsMap> termMap = curresult.getEntityList();
            for (ReceiptTermsMap receiptTermsMap : termMap) {
                double termAmnt =0d;
                double termTaxAmnt =0d;
                if(invoice.isGstIncluded()){
                    termAmnt = receiptTermsMap.getTermAmountExcludingTax();
                }else{
                    termAmnt = receiptTermsMap.getTermamount();
                }
                JSONObject termjedjson = new JSONObject();
                termjedjson.put("srno", jeDetails.size() + 1);
                termjedjson.put("companyid", OLD_JE.getCompany().getCompanyID());
                termjedjson.put("amount", Math.abs(termAmnt));
                termjedjson.put("accountid", receiptTermsMap.getTerm().getAccount().getID());
                termjedjson.put("debit",  termAmnt > 0 ? true : false);
                termjedjson.put("jeid", jeid);
                KwlReturnObject termJEDres = accJournalEntryobj.addJournalEntryDetails(termjedjson);
                JournalEntryDetail termJED = (JournalEntryDetail) termJEDres.getEntityList().get(0);
                jeDetails.add(termJED);
                
                termTaxAmnt = receiptTermsMap.getTermtaxamount();
                if (receiptTermsMap.getTermtax() != null) {
                    termjedjson = new JSONObject();
                    termjedjson.put("srno", jeDetails.size() + 1);
                    termjedjson.put(Constants.companyKey, companyid);
                    termjedjson.put("amount", Math.abs(termTaxAmnt));
                    termjedjson.put("accountid", receiptTermsMap.getTermtax().getAccount().getID());
                    termjedjson.put("debit", termTaxAmnt > 0 ? true : false);
                    termjedjson.put("jeid", jeid);
                    termJEDres = accJournalEntryobj.addJournalEntryDetails(termjedjson);
                    termJED = (JournalEntryDetail) termJEDres.getEntityList().get(0);
                    jeDetails.add(termJED);
                }
                JSONObject invoiceTermsMapJson = new JSONObject();
                InvoiceTermsSales invoiceTermsSalesObj = receiptTermsMap.getTerm();
                invoiceTermsMapJson.put("id", invoiceTermsSalesObj.getId());
                invoiceTermsMapJson.put("termamount", receiptTermsMap.getTermamount());
                invoiceTermsMapJson.put("termpercentage", receiptTermsMap.getPercentage());                
                invoiceTermsMapJson.put("termtax", receiptTermsMap.getTermtax()!= null ? receiptTermsMap.getTermtax().getID() : "");
                invoiceTermsMapJson.put("termamountinbase", receiptTermsMap.getTermamountinbase());
                invoiceTermsMapJson.put("termtaxamount", receiptTermsMap.getTermtaxamount());
                invoiceTermsMapJson.put("termtaxamountinbase", receiptTermsMap.getTermtaxamountinbase());
                invoiceTermsMapJson.put("termAmountExcludingTax", receiptTermsMap.getTermAmountExcludingTax());
                invoiceTermsMapJson.put("termAmountExcludingTaxInBase", receiptTermsMap.getTermAmountExcludingTaxInBase());
                invoiceTermsMapJarr.put(invoiceTermsMapJson);
            }
            accGoodsReceiptModuleServiceObj.mapInvoiceTerms(invoiceTermsMapJarr.toString(), repeatedInvoice.getID(), invoice.getCreatedby().getUserID(), false);
                    
            if (invoice.getTaxEntry()!=null) {
                JSONObject discjedjson = new JSONObject();
                discjedjson.put("srno", jeDetails.size() + 1);
                discjedjson.put("companyid", OLD_JE.getCompany().getCompanyID());
                discjedjson.put("amount", invoice.getTaxEntry().getAmount());
                discjedjson.put("accountid", invoice.getTaxEntry().getAccount().getID());
                discjedjson.put("debit", true);
                discjedjson.put("jeid", jeid);
                KwlReturnObject taxJEDres = accJournalEntryobj.addJournalEntryDetails(discjedjson);
                JournalEntryDetail taxJED = (JournalEntryDetail) taxJEDres.getEntityList().get(0);
                jeDetails.add(taxJED);
                invjson.put("taxentryid", taxJED.getID()); 
            }
            
            jeDataMap.put("accjecustomdataref", jeid);
            jeDataMap.put("jedetails", jeDetails);
            jeDataMap.put("externalCurrencyRate", OLD_JE.getExternalCurrencyRate());
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            Set newJEDetails = journalEntry.getDetails();
            Set JE_DETAILS = invoice.getJournalEntry().getDetails();
            HashMap<String, String> oldNnewJEDid = new HashMap<String, String>();
            Iterator jeditr = JE_DETAILS.iterator();
            Iterator newjeditr = newJEDetails.iterator();
            while (jeditr.hasNext()) {
                JournalEntryDetail newJEDetail = null;
                JournalEntryDetail oldJEDetail = (JournalEntryDetail) jeditr.next();
                if (newjeditr.hasNext()) {
                    newJEDetail = (JournalEntryDetail) newjeditr.next();
                    JSONObject jedjson1 = new JSONObject();
                    jedjson1.put("jedid", newJEDetail.getID());
                    jedjson1.put("accjedetailcustomdata", newJEDetail.getID());
                    KwlReturnObject jedresult1 = accJournalEntryobj.updateJournalEntryDetails(jedjson1);
                    JournalEntryDetail jed1 = (JournalEntryDetail) jedresult1.getEntityList().get(0);
                    oldNnewJEDid.put(oldJEDetail.getID(), jed1.getID());
                }

            }
//            JournalEntryDetail CUST_JED = invoice.getVendorEntry();
//            if (CUST_JED != null) {
//                invjson.put(GoodsReceiptConstants.VENDORENTRYID, oldNnewJEDid.get(CUST_JED.getID()));    //
//            }

            JournalEntryDetail SHIP_JED = invoice.getShipEntry();
            if (SHIP_JED != null) {
                invjson.put("shipentryid", oldNnewJEDid.get(SHIP_JED.getID()));
            }

            JournalEntryDetail OTHER_JED = invoice.getOtherEntry();
            if (OTHER_JED != null) {
                invjson.put("otherentryid", oldNnewJEDid.get(OTHER_JED.getID()));
            }

//            JournalEntryDetail TAX_JED = invoice.getTaxEntry();
//            if (TAX_JED != null) {
//                invjson.put("taxentryid", oldNnewJEDid.get(TAX_JED.getID()));
//            }
            
            invjson.put("approvallevel", invoice.getApprovallevel());
            invjson.put("shipvia", invoice.getShipvia());
            invjson.put(Constants.Checklocktransactiondate, calDate);
            
            jeDataMap.put("transactionModuleid", Constants.Acc_Vendor_Invoice_ModuleId);
            jeDataMap.put("transactionId", repeatedInvoice.getID());
            jeDataMap.put(JournalEntryConstants.JEID,journalEntry.getID());
            KwlReturnObject updatejeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//update journalentry
            JournalEntry updatejournalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            
            /*
            invjson.put(GoodsReceiptConstants.ISEXPENSETYPE, invoice.isIsExpenseType());
            HashSet<ExpenseGRDetail> expensedetails = new HashSet<ExpenseGRDetail>();
            if (invoice.isIsExpenseType()) {
                Set expenseDetails = invoice.getExpenserows();
                Iterator expenseItr = expenseDetails.iterator();
                while (expenseItr.hasNext()) {
                    ExpenseGRDetail exp = (ExpenseGRDetail) expenseItr.next();
                    ExpenseGRDetail row = new ExpenseGRDetail();
                    row.setID(StringUtil.generateUUID());
                    row.setSrno(exp.getSrno());
                    if (exp.getAccount() != null) {
                        row.setAccount(exp.getAccount());
                    }
                    row.setRate(exp.getRate());
                    row.setAmount(exp.getAmount());
                    row.setGoodsReceipt(repeatedInvoice);
                    if (exp.getTax() != null) {
                        row.setTax(exp.getTax());
                    }
                    row.setRowTaxAmount(exp.getRowTaxAmount());

                    row.setWasRowTaxFieldEditable(exp.isWasRowTaxFieldEditable());
                    row.setCompany(exp.getCompany());
                    row.setDescription(exp.getDescription());

                    Discount ID_DISCOUNT = exp.getDiscount();
                    if (ID_DISCOUNT != null) {
                        JSONObject discjson = new JSONObject();
                        discjson.put("discount", ID_DISCOUNT.getDiscount());
                        discjson.put("inpercent", true);
                        discjson.put("originalamount", ID_DISCOUNT.getOriginalAmount());
                        discjson.put("companyid", companyid);
                        KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                        Discount discount = (Discount) dscresult.getEntityList().get(0);
                        row.setDiscount(discount);
                    }
                    if(exp.getPurchaseJED()!=null){
                        JournalEntryDetail jed=exp.getPurchaseJED();
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put("companyid", jed.getCompany()!=null?jed.getCompany().getCompanyID():companyid);
                        jedjson.put("amount", jed.getAmount());
                        jedjson.put("accountid", jed.getAccount()!=null?jed.getAccount().getID():"");
                        jedjson.put("debit", true);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description",jed.getDescription());
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JournalEntryDetail purchaseJED = (JournalEntryDetail) jedresult.getEntityList().get(0);    
                        row.setPurchaseJED(purchaseJED);
                    }
                    expensedetails.add(row);
                }
            }
            */
//            if (!invoiceContract.isEmpty()) {
//                HashMap<String, Object> invoiceContractSet = new HashMap<String, Object>();
//                invoiceContractSet.put("id", repeatedInvoice.getID());
//                invoiceContractSet.put("contractMappings", invoiceContract);
//                accInvoiceDAOobj.updateInvoiceUsingSet(invoiceContractSet);
//            }

            invjson.put(Constants.invoiceamountdue, invoice.getInvoiceAmount());
            invjson.put(Constants.invoiceamountdueinbase, invoice.getInvoiceAmountInBase());
            invjson.put(Constants.invoiceamount, invoice.getInvoiceAmount());
            invjson.put(Constants.invoiceamountinbase, invoice.getInvoiceAmountInBase());
            invjson.put("gstIncluded", invoice.isGstIncluded());
            invjson.put("excludingGstAmount", invoice.getExcludingGstAmount());
            invjson.put("excludingGstAmountInBase", invoice.getExcludingGstAmountInBase());
            invjson.put("taxAmount", invoice.getTaxamount());
            invjson.put("taxAmountInBase", invoice.getTaxamountinbase());//
            invjson.put("conversionRateFromCurrencyToBase", invoice.isConversionRateFromCurrencyToBase());
            invjson.put(GoodsReceiptConstants.GRID, repeatedInvoice.getID());
            invjson.put("parentid", invoice.getID());
            invjson.put(GoodsReceiptConstants.GRDETAILS, invcdetails);
            invjson.put(GoodsReceiptConstants.EXPENSEGRDETAILS, expensedetails);
            if (isGSThisChanged) {
                /**
                 * Recalculate amount if GST history changed.
                 */
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, invoice.getCompany().getCompanyID());
                requestParams.put("gcurrencyid", invoice.getCompany().getCurrency().getCurrencyID());
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoice.getExcludingGstAmount() + finaltaxamt, invoice.getCurrency().getCurrencyID(), creationDate, invoice.getJournalEntry().getExternalCurrencyRate());
                double invoiceamountdueinbase = (Double) bAmt.getEntityList().get(0);
                invjson.put(Constants.invoiceamountdue, invoice.getExcludingGstAmount() + finaltaxamt);
                invjson.put(Constants.invoiceamount, invoice.getExcludingGstAmount() + finaltaxamt);
                invjson.put(Constants.invoiceamountdueinbase, invoiceamountdueinbase);
                invjson.put(Constants.invoiceamountinbase, invoiceamountdueinbase);
            }
            result = accGoodsReceiptDAO.updateGoodsReceipt(invjson);
            repeatedInvoice = (GoodsReceipt) result.getEntityList().get(0);//Add invoice details
            accGoodsReceiptDAO.updateRecDetailId(repeatedInvoice);

            /**
             * When Avalara Integration is enabled, we save tax details in
             * Avalara tax mapping table 'TransactionDetailAvalaraTaxMapping' So
             * there is no need to save InvoiceDetailTermMap in case Avalara
             * Integration is enabled. : Below piece of code has
             * written to get the list of Map Invoice Details Term Map.
             */
            if (extraPref.getLineLevelTermFlag() == 1 && !extraPref.isAvalaraIntegration()) {
                Set<GoodsReceiptDetail> invoiceDetailsSet = repeatedInvoice.getRows();
                for (GoodsReceiptDetail invoiceDetail : invoiceDetailsSet) {
                    if (invoiceDetail.getInventory() != null && FinalTerm != null && ((List) FinalTerm.get(invoiceDetail.getInventory()) != null)) {
                        List ll2 = (List) FinalTerm.get(invoiceDetail.getInventory());
                        Iterator itr2 = ll2.iterator();
                        while (itr2.hasNext()) {
                            HashMap<String, Object> termHashMap = (HashMap<String, Object>) itr2.next();
                            termHashMap.put("goodsReceiptDetail", invoiceDetail);
                            accGoodsReceiptDAO.saveInvoiceDetailTermMap(termHashMap);
                        }
                    }
                }
            }
            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        } catch (AccountingException ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        }

        return repeatedInvoice;
    }
    /**
     * Function to create dimension array
     * @param invoice
     * @param companyid
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    public JSONArray createDimensionArrayToCalculateGSTForInvoice(GoodsReceipt invoice, String companyid) throws JSONException, ServiceException {
        JSONArray dimArr = new JSONArray();
        JSONObject tempParams = new JSONObject();
        JSONObject jSONObject = new JSONObject();
        tempParams = new JSONObject();
        tempParams.put("companyid", companyid);
        tempParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
        List returnList = accEntityGstDao.getGSTDimensionsDetailsForGSTCalculations(tempParams);
        Iterator iterator = returnList.iterator();
        String selectstate = "";
        String selectentity = "";
        while (iterator.hasNext()) {
            jSONObject = new JSONObject();
            Object[] object = (Object[]) iterator.next();
            int gstconfigtype = Integer.parseInt(object[2].toString());
            jSONObject.put("fieldname", (String) object[0]);
            jSONObject.put("gstmappingcolnum", Integer.parseInt(object[3].toString()));
            jSONObject.put("gstconfigtype", Integer.parseInt(object[2].toString()));
            dimArr.put(jSONObject);
            if (gstconfigtype == 1) {
                selectentity = "Col" + Integer.parseInt(object[1].toString());
            } else {
                selectstate = "Col" + Integer.parseInt(object[1].toString());
            }
        }
        /**
         * Get data from custom tables.
         */
        tempParams.put("selectstate", selectstate);
        tempParams.put("selectentity", selectentity);
        tempParams.put("customtable", "accjecustomdata");
        tempParams.put("primarykey", "journalentryId");
        tempParams.put("primaryid", invoice.getJournalEntry().getID());
        returnList = accEntityGstDao.getGSTDimensionDataFromCustomTableForGSTCalculations(tempParams);
        iterator = returnList.iterator();
        while (iterator.hasNext()) {
            Object[] object = (Object[]) iterator.next();
            for (int i = 0; i < dimArr.length(); i++) {
                jSONObject = dimArr.optJSONObject(i);
                if (jSONObject.optInt("gstconfigtype") == 1) {
                    dimArr.getJSONObject(i).put("dimvalue", (String) object[0]);
                } else {
                    dimArr.getJSONObject(i).put("dimvalue", (String) object[1]);
                }
            }
        }
        return dimArr;
    }
    /**
     * Create Json to save GST term details 
     * @param idt
     * @param invObj
     * @param userid
     * @param isAvalaraIntegration
     * @return
     * @throws ServiceException 
     */
    public List mapInvoiceDetailTerms(ReceiptDetailTermsMap idt, Inventory invObj, String userid, boolean isAvalaraIntegration) throws ServiceException {
        List ll = new ArrayList();
        try {
            HashMap<String, Object> termMap = new HashMap<String, Object>();
            termMap.put("term", idt.getEntitybasedLineLevelTermRate().getLineLevelTerms().getId());
            termMap.put("termamount", idt.getTermamount());
            termMap.put("termpercentage", idt.getPercentage());
            termMap.put("assessablevalue", idt.getAssessablevalue());
            termMap.put("creationdate", new Date());
            termMap.put("accountid", idt.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
            termMap.put("userid", userid);
            termMap.put("purchasevalueorsalevalue", idt.getPurchaseValueOrSaleValue());
            termMap.put("deductionorabatementpercent", idt.getDeductionOrAbatementPercent());
            termMap.put("isDefault", idt.getEntitybasedLineLevelTermRate().getLineLevelTerms().isIsDefault());
            termMap.put("productentitytermid", idt.getEntitybasedLineLevelTermRate().getId());
            termMap.put("taxtype", idt.getTaxType());
            termMap.put("termamount", idt.getTermamount());
            termMap.put("termpercentage", idt.getPercentage());
            ll.add(termMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return ll;
    }
    /**
     * Function to create require Map from json object
     * @param termObj
     * @param userid
     * @return
     * @throws ServiceException 
     */
    public List mapInvoiceDetailTerms(JSONObject termObj, String userid) throws ServiceException {
        List ll = new ArrayList();
        try {
            HashMap<String, Object> termMap = new HashMap<String, Object>();
            termMap.put("term", termObj.optString("termid"));
            termMap.put("termamount", termObj.optDouble("termamount"));
            termMap.put("termpercentage", termObj.optDouble("taxvalue"));
            termMap.put("assessablevalue", termObj.optDouble("assessablevalue"));
            termMap.put("creationdate", new Date());
            termMap.put("accountid", termObj.optString("accountid"));
            termMap.put("userid", userid);
            termMap.put("productentitytermid", termObj.optString("productentitytermid"));
            termMap.put("taxtype", termObj.optInt("taxtype"));
            ll.add(termMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return ll;
    }
    //Send notification mail 
    public void SendMail(HashMap requestParams) throws ServiceException {
        String loginUserId = (String) requestParams.get("loginUserId");
        User user = (User) accJournalEntryobj.getUserObject(loginUserId);
        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), user.getCompany().getCompanyID());
        Company company = (Company) returnObject.getEntityList().get(0);
        String sendorInfo = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        String htmlMailContent = (String) requestParams.get("htmlMailContent");
        String plainMailContent = (String) requestParams.get("plainMailContent");
        SimpleDateFormat sdf = new SimpleDateFormat();
        String cEmail = user.getEmailID() != null ? user.getEmailID() : "";
        if (!StringUtil.isNullOrEmpty(cEmail)) {
            try {
                String subject = "Recurring Alert Notification";
                //String sendorInfo = "admin@deskera.com";
                String htmlTextC = "";
                htmlTextC += "<br/>Hello " + user.getFirstName() + "<br/>";
                htmlTextC = htmlTextC + htmlMailContent;

                htmlTextC += "<br/>Regards,<br/>";
                htmlTextC += "<br/>ERP System<br/>";
                htmlTextC += "<br/><br/>";
                htmlTextC += "<br/>This is an auto generated email. Do not reply<br/>";

                String plainMsgC = "";
                plainMsgC += "\nHello " + user.getFirstName() + "\n";
                plainMsgC = plainMsgC + plainMailContent;

                plainMsgC += "\nRegards,\n";
                plainMsgC += "\nERP System\n";
                plainMsgC += "\n\n";
                plainMsgC += "\nThis is an auto generated email. Do not reply.\n";

                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                SendMailHandler.postMail(new String[]{cEmail}, subject, htmlTextC, plainMsgC, sendorInfo, smtpConfigMap);
            } catch (Exception ex) {
                throw ServiceException.FAILURE("" + ex.getMessage(), ex);
            }
        }
    }//sendMail

}
