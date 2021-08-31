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
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.JournalEntryConstants;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accRepeateInvoice extends MultiActionController implements MessageSourceAware {

    private accInvoiceDAO accInvoiceDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private MessageSource messageSource;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccJournalEntryModuleService journalEntryModuleServiceobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private authHandlerDAO authHandlerDAOObj;
    private auditTrailDAO auditTrailObj;
    private AccRepeateInvoiceService accRepeateInvoiceServiceObj;

    public AccRepeateInvoiceService getAccRepeateInvoiceServiceObj() {
        return accRepeateInvoiceServiceObj;
    }

    public void setAccRepeateInvoiceServiceObj(AccRepeateInvoiceService accRepeateInvoiceServiceObj) {
        this.accRepeateInvoiceServiceObj = accRepeateInvoiceServiceObj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setaccDiscountDAO(accDiscountDAO accDiscountobj) {
        this.accDiscountobj = accDiscountobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setJournalEntryModuleServiceobj(AccJournalEntryModuleService journalEntryModuleServiceobj) {
        this.journalEntryModuleServiceobj = journalEntryModuleServiceobj;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public ModelAndView repeateInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "", failed = "";
        String loggedUserid = "";
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("RIC_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status =null;
        try {
//            status = txnManager.getTransaction(def);
            HashMap<String, Object> mailParams = new HashMap<String, Object>();
            String croneName = Constants.Recurring_Invoice_Crone_Name;

            String croneID = Constants.Recurring_Invoice_Crone_ID;

            SimpleDateFormat df = new SimpleDateFormat(Constants.yyyyMMdd);

            Date currentDate = df.parse(df.format(new Date()));
            HashMap<String, Object> resultMap = new HashMap();
            HashMap<String, Object> requestParams = new HashMap();
            String ipAddress = "";

            boolean isCroneExecutedForCurrentDay = accCommonTablesDAO.isCroneExecutedForCurrentDay(croneID, currentDate);

            if(isCroneExecutedForCurrentDay){
                msg = "This Crone is executed for today,so it cannot be hit again";
            } else {
                boolean isPrevious = false;                         //to create previously set recurring invoices
                if (!StringUtil.isNullOrEmpty(request.getParameter("isPrevious"))) {
                    isPrevious = Boolean.parseBoolean(request.getParameter("isPrevious"));
                    requestParams.put("isPrevious", isPrevious);
                    if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.companyKey))) {
                        requestParams.put(Constants.companyKey, request.getParameter(Constants.companyKey));
                    }
                }

                if (StringUtil.isNullOrEmpty(request.getHeader("x-real-ip"))) {
                    ipAddress = request.getRemoteAddr();
                    requestParams.put("remoteAddress", ipAddress);
                } else {
                    ipAddress = request.getHeader("x-real-ip");
                    requestParams.put("reqHeader", ipAddress);
                }
                requestParams.put("currentDate", currentDate);
                resultMap = recurringSalesInvoice(requestParams);
                msg += (String) (resultMap.get("msg")!=null?resultMap.get("msg"):"");
                resultMap = recurringSalesOrder(requestParams);
                msg += (String) (resultMap.get("msg")!=null?resultMap.get("msg"):"");
                resultMap = recurringJournalEntry(requestParams);
                msg += (String) (resultMap.get("msg")!=null?resultMap.get("msg"):"");

                try {
//                    status = txnManager.getTransaction(def);
                    if (msg.length() > 0) {
                        msg = msg.substring(0, msg.length() - 1);
                        msg = messageSource.getMessage("acc.field.Generatedinvoices", null, RequestContextUtils.getLocale(request)) + " : " + msg + ".";
                    } else {
                        msg = messageSource.getMessage("acc.field.Noanyscheduledrecurringinvoicetogenerate", null, RequestContextUtils.getLocale(request));
                    }

                    // save crone hit detail
                    saveCroneDetails(croneID, croneName, currentDate);

                    HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
                    requestParams1.put("userid", loggedUserid);
                    requestParams1.put("reqHeader", request.getHeader("x-real-ip"));
                    requestParams1.put("remoteAddress", request.getRemoteAddr());
                    String auditMessage = "Cron has been executed successfully for repeateInvoices [Recurring Sales Order,Recurring Invoice,Recurring Journal Entry]";
                    if (!StringUtil.isNullOrEmpty(auditMessage) && !StringUtil.isNullOrEmpty(loggedUserid) && StringUtil.isNullOrEmpty(failed)) {
                        auditTrailObj.insertAuditLog(AuditAction.Cron_Success, auditMessage, requestParams1, "");
                    }

//                    txnManager.commit(status);
                } catch (Exception ex) {
//                    txnManager.rollback(status);
                    Logger.getLogger(accRepeateInvoice.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
//            try {
//                if(status!=null){
//                txnManager.rollback(status);
//                }
//            } catch (Exception txEx) {
//                msg += ex.getMessage();
//            }
            Logger.getLogger(accRepeateInvoice.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                //jobj.put("failed", failed);
                jobj.put("datetime", new Date());
                jobj.put("msg", msg);
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accRepeateInvoice.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public void saveCroneDetails(String croneID, String croneName, Date hittingDate) throws ServiceException {
        try {
            accCommonTablesDAO.saveCroneDetails(croneID, croneName, hittingDate);
        } catch (Exception ex) {
            Logger.getLogger(accRepeateInvoice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

        public HashMap<String, Object> recurringSalesInvoice(HashMap<String, Object> requestParams) throws ServiceException {
        System.out.println("\n***** Recurring Invoice Start  *****\n" + Constants.Recurring_Invoice_Crone_Name);
        String invoiceno = "";
        String msg = "", failed = "";
        HashMap<String, Object> mailParams = new HashMap<String, Object>();
        HashMap<String, Object> returnValue = new HashMap<>();
        String loggedUserid = "";
        Date currentDate = (Date) requestParams.get("currentDate");
        boolean isPrevious = requestParams.get("isPrevious") != null ? Boolean.parseBoolean(requestParams.get("isPrevious").toString()) : false;
        KwlReturnObject repeateInvdetails = accInvoiceDAOobj.getRepeateInvoiceNo(currentDate);
        List<Invoice> invlist = repeateInvdetails.getEntityList();
        if (!isPrevious) {
                for (Invoice invc : invlist) {
                    try {
                        RepeatedInvoices repeateInvc = invc.getRepeateInvoice();
                        Date expireDate = repeateInvc.getExpireDate();
                        Date nextDate = repeateInvc.getNextDate();
                        Calendar c = Calendar.getInstance();
                        c.setTime(nextDate);
                        c.add(Calendar.DATE, repeateInvc.getIntervalUnit());
                        SimpleDateFormat df = new SimpleDateFormat(Constants.yyyyMMdd);
                        Date nextToNextDate = df.parse(df.format(c.getTime()));
                        if ((!(expireDate.before(nextToNextDate)) && repeateInvc.getInvoiceAdvanceCreationDate().equals(currentDate) && repeateInvc.getIntervalUnit() == 1 && StringUtil.equalIgnoreCase("day", repeateInvc.getIntervalType())) || repeateInvc.getPrevDate().equals(currentDate)) {
                            invoiceno = invc != null ? (invc.getInvoiceNumber()) : "";
                            loggedUserid = invc != null ? (invc.getCreatedby().getUserID()) : "";
                            if (!StringUtil.isNullOrEmpty(loggedUserid)) {
                                String htmlMailContent = "<br/>Recurring Invoice <b>\"" + invoiceno + "\"</b> will be post tomorrow.<br/>";
                                htmlMailContent += "<br/>If you want, you can deactivate it from Recurring Invoice Report.<br/>";
                                mailParams.put("htmlMailContent", htmlMailContent);

                                String plainMailContent = "\nRecurring Invoice \n" + invoiceno + "\nwill be post tomorrow.\n";
                                plainMailContent += "\nIf you want, you can deactivate it from Recurring Invoice Report.\n";
                                mailParams.put("plainMailContent", plainMailContent);

                                mailParams.put("loginUserId", loggedUserid);
                                accRepeateInvoiceServiceObj.sendMail(mailParams);    //Notification Mail 
                            }
                        }
                    } catch (Exception ex) {
                        failed = "Recurring notification mail failed.";
                        System.out.println("\n***** Exception in Recurring Invoices :*****\n" + failed);
                    }
                }//while
            }
        // For Recurring Invoices
        String companyid = requestParams.get(Constants.companyid) != null ? requestParams.get(Constants.companyid).toString() : "";
        KwlReturnObject repeateInvcs;
        if (isPrevious) {
            repeateInvcs = accInvoiceDAOobj.getRepeatePreviousInvoices(requestParams);
        } else {
            repeateInvcs = accInvoiceDAOobj.getRepeateInvoices(requestParams);
        }
        KwlReturnObject excludedInvoicesObj = accInvoiceDAOobj.getExcludedInvoices(requestParams);
        List ExcludedIDlist = excludedInvoicesObj.getEntityList();
        List list = repeateInvcs.getEntityList();
        Iterator Excludeditr = ExcludedIDlist.iterator();
        Iterator itr = list.iterator();
        boolean excludeFlag = false;

        while (itr.hasNext()) {
            String entryno = "";
            User adminUser = null;
            Invoice invoice = (Invoice) itr.next();
            try {
                msg += accRepeateInvoiceServiceObj.processSalesInvoice(invoice, requestParams, Excludeditr, ExcludedIDlist);
            } catch (SessionExpiredException ex) {
                Logger.getLogger(accRepeateInvoice.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ServiceException ex) {
                Logger.getLogger(accRepeateInvoice.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(accRepeateInvoice.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        returnValue.put("msg", msg);
        System.out.println("\n***** Recurring Invoice End  *****\n" + Constants.Recurring_Invoice_Crone_Name);
        return returnValue;
    }
        
        public HashMap<String, Object> recurringJournalEntry(HashMap<String, Object> requestParams) throws ServiceException {
        String entryno = "";
        JournalEntry jeObj = null;
        String msg = "", failed = "";
        String loggedUserid = "";
        Date currentDate = (Date) requestParams.get("currentDate");
        KwlReturnObject repeateJEdetails = accJournalEntryobj.getRepeateJEEntryNo(currentDate);
        List jlist = repeateJEdetails.getEntityList();
        Iterator jeitr = jlist.iterator();
        HashMap<String, Object> mailParams = new HashMap<String, Object>();
        HashMap<String, Object> returnValue = new HashMap<String, Object>();
        boolean isPrevious = requestParams.get("isPrevious") != null ? Boolean.parseBoolean(requestParams.get("isPrevious").toString()) : false;
        if (!isPrevious) {
            while (jeitr.hasNext()) {
                try {
                    jeObj = (JournalEntry) jeitr.next();
                    entryno = jeObj != null ? (jeObj.getEntryNumber()) : "";
                    loggedUserid = jeObj != null ? (jeObj.getCreatedby().getUserID()) : "";
                    if (!StringUtil.isNullOrEmpty(loggedUserid)) {
                        RepeatedJE repeateJE = jeObj.getRepeateJE();
                        Date expireDate = repeateJE.getExpireDate();
                        Date nextDate = repeateJE.getNextDate();
                        Calendar c = Calendar.getInstance();
                        c.setTime(nextDate);
                        c.add(Calendar.DATE, repeateJE.getIntervalUnit()); // Adding one day to current date to get records which are set at the difference of one day
                        SimpleDateFormat df = new SimpleDateFormat(Constants.yyyyMMdd);
                        Date nextToNextDate = df.parse(df.format(c.getTime()));
                        if ((!(expireDate.before(nextToNextDate)) && repeateJE.getNextDate().equals(currentDate) && repeateJE.getIntervalUnit() == 1 && StringUtil.equalIgnoreCase("day", repeateJE.getIntervalType())) || repeateJE.getPrevDate().equals(currentDate)) {
                            String htmlMailContent = "<br/>Recurring Journal Entry <b>\"" + entryno + "\"</b> will be post tomorrow.<br/>";
                            htmlMailContent += "<br/>If you want, you can deactivate it from Recurring Journal Entry Report.<br/>";
                            mailParams.put("htmlMailContent", htmlMailContent);

                            String plainMailContent = "\nRecurring Journal Entry \n" + entryno + "\nwill be post tomorrow.\n";
                            plainMailContent += "\nIf you want, you can deactivate it from Recurring Journal Entry Report.\n";
                            mailParams.put("plainMailContent", plainMailContent);

                            mailParams.put("loginUserId", loggedUserid);
                            accRepeateInvoiceServiceObj.sendMail(mailParams);    //Notification Mail 
                        }
                    }
                } catch (Exception ex) {
                    failed = "Recurring notification mail failed.";
                    System.out.println("\n***** Exception in Recurring Journal Entry:*****\n" + failed);
                    throw ServiceException.FAILURE("" + ex.getMessage(), ex);
                }
            }//while

            KwlReturnObject repeateJE = accJournalEntryobj.getRepeateJE(requestParams);
            List JElist = repeateJE.getEntityList();
            Iterator JEitr = JElist.iterator();
            while (JEitr.hasNext()) {
                String jeentryno = "";
                User adminUser = null;
                JournalEntry JEObj = (JournalEntry) JEitr.next();
                try {
                    msg += accRepeateInvoiceServiceObj.processJournalEntry(JEObj, requestParams);
                } catch (Exception ex) {
                    Logger.getLogger(accRepeateInvoice.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            returnValue.put("msg", msg);
        }
        return returnValue;
    }
        public HashMap<String, Object> recurringSalesOrder(HashMap<String, Object> requestParams) throws ServiceException {
            String msg = "", failed = "";
            String loggedUserid = "";
            String SOno = "";
            HashMap<String, Object> returnValue = new HashMap<>();
            HashMap<String, Object> mailParams = new HashMap<String, Object>();
            Date currentDate = (Date) requestParams.get("currentDate");
            System.out.println("\n***** Recurring Sales Order Start *****\n");
            KwlReturnObject repeateSOdetails = accSalesOrderDAOobj.getRepeateSONo(currentDate);
            List<SalesOrder> solist = repeateSOdetails.getEntityList();
            boolean isPrevious = requestParams.get("isPrevious") != null ? Boolean.parseBoolean(requestParams.get("isPrevious").toString()) : false;
            if (!isPrevious) {
                for (SalesOrder so : solist) {
                    try {
                        SOno = so.getSalesOrderNumber();
                        loggedUserid = so.getCreatedby().getUserID();
                        if (!StringUtil.isNullOrEmpty(loggedUserid)) {
                            RepeatedSalesOrder repeateSO = so.getRepeateSO();
                            Date expireDate = repeateSO.getExpireDate();
                            Date nextDate = repeateSO.getNextDate();
                            Calendar c = Calendar.getInstance();
                            c.setTime(nextDate);
                            c.add(Calendar.DATE, repeateSO.getIntervalUnit()); // Adding one day to current date to get records which are set at the difference of one day
                            SimpleDateFormat df = new SimpleDateFormat(Constants.yyyyMMdd);
                            Date nextToNextDate = df.parse(df.format(c.getTime()));
                            if ((!(expireDate.before(nextToNextDate)) && repeateSO.getNextDate().equals(currentDate) && repeateSO.getIntervalUnit() == 1 && StringUtil.equalIgnoreCase("day", repeateSO.getIntervalType())) || repeateSO.getPrevDate().equals(currentDate)) {
                                String htmlMailContent = "<br/>Recurring Sales Order <b>\"" + SOno + "\"</b> will be post tomorrow.<br/>";
                                htmlMailContent += "<br/>If you want, you can deactivate it from Recurring Sales Order Report.<br/>";
                                mailParams.put("htmlMailContent", htmlMailContent);

                                String plainMailContent = "\nRecurring Sales Order \n" + SOno + "\nwill be post tomorrow.\n";
                                plainMailContent += "\nIf you want, you can deactivate it from Recurring Sales Order Report.\n";
                                mailParams.put("plainMailContent", plainMailContent);

                                mailParams.put("loginUserId", loggedUserid);
                                accRepeateInvoiceServiceObj.sendMail(mailParams);    //Notification Mail 
                            }
                        }
                    } catch (Exception ex) {
                        failed = "Recurring notification mail failed.";
                        System.out.println("\n***** Exception in Recurring Sales Order:*****\n" + failed);

                    }
                }

                KwlReturnObject repeateSO = accSalesOrderDAOobj.getRepeateSalesOrder(requestParams);
                List<SalesOrder> SOlist = repeateSO.getEntityList();
                for (SalesOrder SalesOrderObj : SOlist) {
                    try {
                        msg += accRepeateInvoiceServiceObj.processSalesOrder(SalesOrderObj, requestParams);
                    } catch (Exception ex) {
                        Logger.getLogger(AccRepeateInvoiceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                returnValue.put("msg", msg);
            }
        System.out.println("\n***** Recurring Sales Order End *****\n" + Constants.SALES_ORDER);
        return returnValue;
    }
}
