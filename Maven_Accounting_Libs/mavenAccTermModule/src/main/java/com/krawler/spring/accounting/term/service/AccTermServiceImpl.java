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
package com.krawler.spring.accounting.term.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Term;
import com.krawler.spring.accounting.term.accTermDAO;
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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public class AccTermServiceImpl implements AccTermService {

    private accTermDAO accTermObj;

    public void setaccTermDAO(accTermDAO accTermObj) {
        this.accTermObj = accTermObj;
    }

    public JSONObject getTerm(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String cash = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            cash = request.getParameter("cash_Invoice") != null ? request.getParameter("cash_Invoice").toString() : "false";
            requestParams.put("cash_Invoice", cash);


            KwlReturnObject result = accTermObj.getTerm(requestParams);
            List list = result.getEntityList();

            JSONArray jArr = getTermJson(list);
            jobj.put("data", jArr);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccTermServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccTermServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccTermServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
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
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTermJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public JSONObject saveTerm(Map<String, Object> params) throws ServiceException, JSONException {
        KwlReturnObject termresult;
        String failedTerms = "";
        JSONObject jSONObject = new JSONObject();
        String msg = "";
        String companyid = (String) params.get("companyid");
        JSONArray array = (JSONArray) params.get("termdetails");
        for (int i = 0; i < array.length(); i++) {
           // try {
                JSONObject jobj = array.getJSONObject(i);
                HashMap<String, Object> termMap = new HashMap<String, Object>();
                String crmTermID = jobj.getString("crmtermid");
                String termName = StringUtil.DecodeText(jobj.optString("termname"));
                int termDays = Integer.parseInt(StringUtil.DecodeText(jobj.optString("termdays")));

                // Check duplicate is present or not
                termMap.put("companyid", companyid);
                termMap.put("termname", termName);

                KwlReturnObject terms = accTermObj.getTerm(termMap);
                if (terms.getEntityList() != null && terms.getEntityList().size() > 0) {
                    Term term = (Term) terms.getEntityList().get(0);
                    if (!StringUtil.equal(term.getCrmtermid(), crmTermID)) {
                        termName += " - From CRM";
                    }
                }

                // check add/ edit case
                termMap.remove("termname");
                termMap.put("crmtermid", jobj.getString("crmtermid"));
                terms = accTermObj.getTerm(termMap);

                termMap.put("termdays", termDays);
                termMap.put("termname", termName);

                if (terms.getEntityList() != null && terms.getEntityList().isEmpty()) {
                    termresult = accTermObj.addTerm(termMap);
                } else {
                    Term term = (Term) terms.getEntityList().get(0);
                    termMap.put("term", term);
                    String checkUse = "";
                    if (term.getTermdays() != termDays) {
                        checkUse = checkTermUsage(termMap);
                        failedTerms += StringUtil.isNullOrEmpty(checkUse) ? "" : (checkUse + ", ");
                    }
                    if (StringUtil.isNullOrEmpty(checkUse)) {
                        termMap.put("termid", term.getID());
                        termresult = accTermObj.updateTerm(termMap);
                    }
                }
           /* } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(AccTermServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        }
        msg = "All Payment Terms have been synced successfully.";
        if (!StringUtil.isNullOrEmpty(failedTerms)) {
            msg = "All Payment Terms have been synced successfully except below:<br/><br/><b>" + failedTerms.substring(0, failedTerms.length()-2) + "</b><br/><br/>As they are used in transactions of ERP application.";
            jSONObject.put("usedintransaction", true);
        }
        return jSONObject.put("msg", msg);
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

//        if (count1 > 0 || count2 > 0 || count3 > 0 || count4 > 0 || count5 > 0 || count6 > 0 || count7 > 0 || count8 > 0) {
//            linkedTransaction = termName;
//        }
        return linkedTransaction;
    }
}
