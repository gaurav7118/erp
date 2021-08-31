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
package com.krawler.spring.accounting.term;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.DiscountMaster;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Term;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
public class accTermController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accTermDAO accTermObj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;

    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccTermDAO(accTermDAO accTermObj) {
        this.accTermObj = accTermObj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public ModelAndView getTerm(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String cash = "";
        boolean issuccess = false;
        try {
            String start=request.getParameter("start");   //ERP-13661 [SJ]
            String limit=request.getParameter("limit");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            cash = request.getParameter("cash_Invoice") != null ? request.getParameter("cash_Invoice").toString() : "false";
            requestParams.put("cash_Invoice", cash);
            if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
                String ss = (String) request.getParameter("ss");
                requestParams.put("ss", ss);
                requestParams.put("ss_names", new String[]{"value"});
            } else if (request.getParameter("query") != null && !StringUtil.isNullOrEmpty(request.getParameter("query"))) {
                String ss = (String) request.getParameter("query");
                requestParams.put("ss", ss);
                requestParams.put("ss_names", new String[]{"value"});
            }

            KwlReturnObject result = accTermObj.getTerm(requestParams);
            List list = result.getEntityList();

            JSONArray jArr = getTermJson(list);
             JSONArray pagedJson = jArr;//ERP-13661 [SJ]                
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(jArr, Integer.parseInt(start), Integer.parseInt(limit));
                }
            jobj.put("data", pagedJson);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTermController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accTermController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTermController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getTermJson(List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Term ct = (Term) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("termid", ct.getID());
                obj.put("termname", ct.getTermname());
                obj.put("termdays", ct.getTermdays());
                obj.put("isdefaultcreditterm", ct.isIsdefault());
                obj.put("discountname", ct.getDiscountName()!=null?ct.getDiscountName().getId():"");
                obj.put("applicabledays", ct.getApplicableDays());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTermJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView saveTerm(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Term_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String linkedTransaction = saveTerm(request);
            issuccess = true;
            if(!StringUtil.isNullOrEmpty(linkedTransaction)){
                msg = messageSource.getMessage("acc.term.updateExcept", null, RequestContextUtils.getLocale(request))+ linkedTransaction.substring(0, linkedTransaction.length() - 2) + ". " + messageSource.getMessage("acc.field.usedintransaction", null, RequestContextUtils.getLocale(request))+""+messageSource.getMessage("acc.paymentterms.sequencesave", null, RequestContextUtils.getLocale(request));   //"All Term(s) has been updated successfully except #TermNames. These term(s) are already used in tansaction(s).";
            }else{
                msg = messageSource.getMessage("acc.term.update", null, RequestContextUtils.getLocale(request));   //"Term has been Updated successfully";
            }
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accTermController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accTermController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTermController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String saveTerm(HttpServletRequest request) throws AccountingException, ServiceException, SessionExpiredException {
        String linkedTransaction = "";
        try {
            int delCount = 0;
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String termid = "";
            KwlReturnObject termresult,termSrresult;
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                HashMap<String, Object> termMap = new HashMap<String, Object>();
                if (StringUtil.isNullOrEmpty(jobj.getString("termid")) == false) {
                    termid = jobj.getString("termid");
                    String methodname = jobj.getString("termname");
                    KwlReturnObject res = accountingHandlerDAOobj.getObject(Term.class.getName(), termid);
                    Term term = (Term) res.getEntityList().get(0);
                    
                    if (term != null) {
                        String termName = term.getTermname();
                        termMap.put("term", term);  //term object put
                        termMap.put("companyid", companyid);  //term object put
                        String checkUse = checkTermUsage(termMap);
                        termMap.remove("term");  //term object removed

                        if (!StringUtil.isNullOrEmpty(checkUse)) {
                            linkedTransaction += termName + ", ";
                            continue;
                        }

                        try {
                            termresult = accTermObj.deleteTerm(termid, companyid);
                            delCount += termresult.getRecordTotalCount();
                            auditTrailObj.insertAuditLog(AuditAction.PAYMENT_METHOD_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Payment Term " + methodname, request, "0");
                        } catch (ServiceException ex) {
                            throw new AccountingException(messageSource.getMessage("acc.uom.excp1", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                }
            }
//            params.add(company.getCompanyID());
//            try {
//                delQuery = "delete from Term c where c.ID in(" + qMarks + ") and c.company.companyID=?";
//                delCount = HibernateUtil.executeUpdate(session, delQuery, params.toArray());
//            } catch (ServiceException ex) {
//                int type = 0;
//                String tablename = "in Transaction (s)";
//                String query = "from Customer c where c.creditTerm.ID not in(" + qMarks + ") and c.company.companyID=?";
//                list = HibernateUtil.executeQuery(session, query, params.toArray());
//                Iterator itr = list.iterator();
//                if (itr.hasNext()) {
//                    tablename = "by Customer(s)";
//                    type = 1;
//                }
//                query = "from Vendor v where v.debitTerm.ID not in(" + qMarks + ") and v.company.companyID=?";
//                list = HibernateUtil.executeQuery(session, query, params.toArray());
//                itr = list.iterator();
//                if (itr.hasNext()) {
//                    if (type == 1) {
//                        tablename = "by Vendor(s) and Customer(s)";
//                    } else {
//                        tablename = "by Vendor(s)";
//                    }
//                }
//                throw new AccountingException("Selected record(s) are currently used " + tablename);
//            }
            Term term = null;
            String failedTerms = "";
            String auditMsg;
            String auditID;
//            String fullName = AuthHandler.getFullName(session, AuthHandler.getUserid(request));
//            if (delCount > 0) {
//                auditTrailObj.insertAuditLog(AuditAction.CREDIT_TERM_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " deleted " + delCount + " Credit Term", request, "0");
//            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> termMap = new HashMap<String, Object>();
                String termName = StringUtil.DecodeText(jobj.optString("termname"));
                int termDays = Integer.parseInt(StringUtil.DecodeText(jobj.optString("termdays")));
                String termId = StringUtil.DecodeText(jobj.optString("termid"));
                termMap.put("termname", termName);
                termMap.put("companyid", companyid);
                termMap.put("termid", termId);
//                int applicableDays = StringUtil.isNullOrEmpty(jobj.optString("applicabledays")) ? -1 : jobj.optInt("applicabledays");
//                String discountName = StringUtil.isNullOrEmpty(jobj.optString("discountname")) ? "" : jobj.optString("discountname");
                if (!StringUtil.isNullOrEmpty(jobj.optString("discountname"))) {
                    KwlReturnObject discountMasterReturn = accountingHandlerDAOobj.getObject(DiscountMaster.class.getName(), jobj.optString("discountname"));
                    DiscountMaster discountMaster = (DiscountMaster) discountMasterReturn.getEntityList().get(0);
                    termMap.put("discountname", discountMaster);
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("applicabledays"))) {
                    termMap.put("applicabledays", jobj.optInt("applicabledays"));
                }
                if (jobj.has("srno")) {
                    termMap.put("srno", jobj.getInt("srno"));
                }if (!StringUtil.isNullOrEmpty(termId)) {
                    termSrresult = accTermObj.updateSrNoTerm(termMap);
                }
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }
                
                KwlReturnObject terms = accTermObj.getTerm(termMap);
                if (terms.getEntityList() != null && terms.getEntityList().size() > 0) {
                    term = (Term) terms.getEntityList().get(0);
                    if (!StringUtil.equal(term.getID(), termId)) {
                        failedTerms += termName + ", ";
                        continue;
                    }
                }
                
                termMap.put("termdays", termDays);
                termMap.put("isdefaultcreditterm", Boolean.parseBoolean(StringUtil.DecodeText(jobj.optString("isdefaultcreditterm"))));

                if (StringUtil.isNullOrEmpty(termId)) {
                    auditMsg = "added";
                    auditID = AuditAction.PAYMENT_METHOD_ADDED;
                    termresult = accTermObj.addTerm(termMap);
                    term = (Term) termresult.getEntityList().get(0);
                } else {
                    KwlReturnObject res = accountingHandlerDAOobj.getObject(Term.class.getName(), termId);
                    term = (Term) res.getEntityList().get(0);
                    auditMsg = "updated";
                    auditID = AuditAction.PAYMENT_METHOD_CHANGED;
                    termMap.put("termid", jobj.getString("termid"));
                    termMap.put("term", term);  //term object put
                    String checkUse = checkTermUsage(termMap);
                    termMap.remove("term");  //term object removed
                    linkedTransaction += StringUtil.isNullOrEmpty(checkUse) ? "" : (checkUse + ", ");
                    if (StringUtil.isNullOrEmpty(checkUse)) {
                        termresult = accTermObj.updateTerm(termMap);
                        term = (Term) termresult.getEntityList().get(0);
                    }
                }
                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " Payment Term " + term.getTermname(), request, term.getID());
            }
            if(!StringUtil.isNullOrEmpty(failedTerms)) {
                throw new AccountingException(messageSource.getMessage("acc.term.updateExcept", null, RequestContextUtils.getLocale(request)) + " " +
                        messageSource.getMessage("acc.alert.paymenttermalreadyexists", new Object[]{failedTerms.substring(0, failedTerms.length()-2)}, RequestContextUtils.getLocale(request)));
            }
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.uom.excp2", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw ServiceException.FAILURE("saveTerm : " + ex.getMessage(), ex);
        }
        return linkedTransaction;
    }

    public String checkTermUsage(Map<String, Object> details) throws ServiceException {
        String linkedTransaction = "";
        Term term = (Term) details.get("term");
        String termid = term.getID();
        String companyid = (String) details.get("companyid");

        String termName = term.getTermname();
        //Check PO Transactions
        KwlReturnObject result = accTermObj.getPOTerm(termid, companyid); //Is used in Purchase Order ?
        List list1 = result.getEntityList();
        int count1 = list1.size();
        if(count1 > 0) {
            linkedTransaction = termName;
            return linkedTransaction;
        }
        

        //Check SO Transactions
        result = accTermObj.getSOTerm(termid, companyid);  // Is used in Sales Order ?
        List list2 = result.getEntityList();
        int count2 = list2.size();
        if(count2 > 0) {
            linkedTransaction = termName;
            return linkedTransaction;
        }

        //Check PI Transactions
        result = accTermObj.getPITerm(termid, companyid); // Is Used in Vendor Invoice?
        List list3 = result.getEntityList();
        int count3 = list3.size();
        if(count3 > 0) {
            linkedTransaction = termName;
            return linkedTransaction;
        }

        //Check SI Transactions
        result = accTermObj.getSITerm(termid, companyid);  // Is used in Customer Invoice?
        List list4 = result.getEntityList();
        int count4 = list4.size();
        if(count4 > 0) {
            linkedTransaction = termName;
            return linkedTransaction;
        }

        //Check PQ Transactions
        result = accTermObj.getVQTerm(termid, companyid); // Is used in Vendor Quotation?
        List list5 = result.getEntityList();
        int count5 = list5.size();
        if(count5 > 0) {
            linkedTransaction = termName;
            return linkedTransaction;
        }

        //Check SQ Transactions
        result = accTermObj.getCQTerm(termid, companyid); //Is used in Customer Quotation?
        List list6 = result.getEntityList();
        int count6 = list6.size();
        if(count6 > 0) {
            linkedTransaction = termName;
            return linkedTransaction;
        }

        //Check Customer Transactions
        result = accTermObj.getCustomerTerm(termid, companyid); //Is used in Customer?
        List list7 = result.getEntityList();
        int count7 = list7.size();
        if(count7 > 0) {
            linkedTransaction = termName;
            return linkedTransaction;
        }

        //Check Vendor Transactions
        result = accTermObj.getVendorTerm(termid, companyid); //Is used in Vendor?
        List list8 = result.getEntityList();
        int count8 = list8.size();
        if(count8 > 0) {
            linkedTransaction = termName;
            return linkedTransaction;
        }
        return linkedTransaction;
    }
}
