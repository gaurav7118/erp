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
package com.krawler.spring.accounting.tax.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.InvoiceTermsSales;
import com.krawler.hql.accounting.Tax;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.tax.TaxConstants;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
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
public class AccTaxServiceImpl implements AccTaxService, TaxConstants {

    private accTaxDAO accTaxObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public JSONObject getTax(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            DateFormat df = authHandler.getDateFormatter(request);
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String transDate = request.getAttribute("transactiondate") != null ? authHandler.getDateFormatter(request).format(request.getAttribute("transactiondate")) : request.getParameter(TRANSACTIONDATE);
            String taxtypeid = request.getParameter("taxtypeid");
            if (transDate != null) {
                requestParams.put("transactiondate", df.parse(transDate));
            }
            if (!StringUtil.isNullOrEmpty(taxtypeid)) {
                requestParams.put("taxtypeid", taxtypeid);
            }
            String module = request.getParameter(Constants.moduleid);
            if (module != null) {
                int moduleid = Integer.parseInt(module);
                if (moduleid == Constants.Acc_Purchase_Order_ModuleId || moduleid == Constants.Acc_BillingPurchase_Order_ModuleId
                        || moduleid == Constants.Acc_Vendor_Invoice_ModuleId || moduleid == Constants.Acc_Vendor_BillingInvoice_ModuleId
                        || moduleid == Constants.Acc_Vendor_Quotation_ModuleId || moduleid == Constants.Acc_Make_Payment_ModuleId) {
                    requestParams.put("taxtype", 1);
                } else {
                    requestParams.put("taxtype", 2);
                }
            }
            KwlReturnObject result = accTaxObj.getTax(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getTaxJson(request, list);
            jobj.put(DATA, DataJArr);
            jobj.put(COUNT, count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccTaxServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccTaxServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccTaxServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public JSONArray getTaxJson(HttpServletRequest request, List<Object[]> list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
//            Iterator itr = list.iterator();
//            while (itr.hasNext()) {
//               Object[] row = (Object[]) itr.next();
            if (list != null && !list.isEmpty()) {
                for (Object[] row : list) {
                    if (row[2] == null) {
                        continue;
                    }
                    Tax tax = (Tax) row[0];
                    JSONObject obj = new JSONObject();
                    obj.put(TAXID, tax.getID());
                    obj.put(TAXNAME, tax.getName());
                    obj.put(TAXDESCRIPTION, tax.getDescription());
                    obj.put(PERCENT, row[1]);
                    obj.put(TAXCODE, tax.getTaxCode());
                    obj.put(ACCOUNTID, tax.getAccount().getID());
                    obj.put(ACCOUNTNAME, tax.getAccount().getName());
                    obj.put(TAXTYPEID, tax.getTaxtype());
                    obj.put("taxTypeName", tax.getTaxtype() == 2 ? "Sales" : "Purchase");
                    obj.put(APPLYDATE, authHandler.getDateFormatter(request).format(row[2]));
                    
                    if (request.getAttribute("isCRMCall") != null && (Boolean) request.getAttribute("isCRMCall")) {
                        List l = accTaxObj.getTerms(tax.getID());
                        String termid = "";
                        String termname = "";
                        Iterator itr = l.iterator();
                        while (itr.hasNext()) {
                            InvoiceTermsSales invoiceTermsSales = (InvoiceTermsSales) kwlCommonTablesDAOObj.getClassObject(InvoiceTermsSales.class.getName(), itr.next().toString());
                            //   InvoiceTermsSales invoiceTermsSales=(InvoiceTermsSales)itr.next();
                            if (invoiceTermsSales != null) {
                                termid += invoiceTermsSales.getId() + ",";
                                termname += invoiceTermsSales.getTerm() + ",";;
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(termid)) {
                            termid = termid.substring(0, termid.length() - 1);
                        }
                        if (!StringUtil.isNullOrEmpty(termname)) {
                            termname = termname.substring(0, termname.length() - 1);
                        }
                        obj.put("termid", termid);
                        obj.put("termname", termname);
                    }
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTaxJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
 
 @Override   
  /* Used in Group Company Mapping*/  
     public JSONObject getTax(JSONObject paramJObj) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            DateFormat df = authHandler.getDateFormatter(paramJObj);
            Map<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJObj);
            String transDate = paramJObj.optString("transactiondate", null) != null ? authHandler.getDateFormatter(paramJObj).format(paramJObj.optString("transactiondate")) : paramJObj.optString(TRANSACTIONDATE);
            String taxtypeid = paramJObj.optString("taxtypeid", null);
            if (!StringUtil.isNullOrEmpty(transDate)){
                requestParams.put("transactiondate", df.parse(transDate));
            }
            if (!StringUtil.isNullOrEmpty(taxtypeid)) {
                requestParams.put("taxtypeid", taxtypeid);
            }
            String module = paramJObj.optString(Constants.moduleid);
             if (!StringUtil.isNullOrEmpty(module)){
                int moduleid = Integer.parseInt(module);
                if (moduleid == Constants.Acc_Purchase_Order_ModuleId || moduleid == Constants.Acc_BillingPurchase_Order_ModuleId
                        || moduleid == Constants.Acc_Vendor_Invoice_ModuleId || moduleid == Constants.Acc_Vendor_BillingInvoice_ModuleId
                        || moduleid == Constants.Acc_Vendor_Quotation_ModuleId || moduleid == Constants.Acc_Make_Payment_ModuleId) {
                    requestParams.put("taxtype", 1);
                } else {
                    requestParams.put("taxtype", 2);
                }
            }

            requestParams.put(Constants.ss, paramJObj.optString(Constants.ss));
            requestParams.put("notinquery", paramJObj.optString("notinquery",null));
            KwlReturnObject result = accTaxObj.getTax(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getTaxJson(paramJObj, list);
            jobj.put(DATA, DataJArr);
            jobj.put(COUNT, count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccTaxServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccTaxServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccTaxServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public JSONArray getTaxJson(JSONObject paramJObj, List<Object[]> list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            if (list != null && !list.isEmpty()) {
                for (Object[] row : list) {
                    if (row[2] == null) {
                        continue;
                    }
                    Tax tax = (Tax) row[0];
                    JSONObject obj = new JSONObject();
                    obj.put(TAXID, tax.getID());
                    obj.put(TAXNAME, tax.getName());
                    obj.put(TAXDESCRIPTION, tax.getDescription());
                    obj.put(PERCENT, row[1]);
                    obj.put(TAXCODE, tax.getTaxCode());
                    obj.put(ACCOUNTID, tax.getAccount().getID());
                    obj.put(ACCOUNTNAME, tax.getAccount().getName());
                    obj.put(TAXTYPEID, tax.getTaxtype());
                    obj.put("taxTypeName", tax.getTaxtype() == 2 ? "Sales" : "Purchase");
                    obj.put(APPLYDATE, authHandler.getDateFormatter(paramJObj).format(row[2]));
                    obj.put("isSalesFlag", tax.getTaxtype() == 2 ? true : false);
                    

                    if (paramJObj.optString("isCRMCall", null) != null && (Boolean) paramJObj.get("isCRMCall")) {
                        List l = accTaxObj.getTerms(tax.getID());
                        String termid = "";
                        String termname = "";
                        Iterator itr = l.iterator();
                        while (itr.hasNext()) {
                            InvoiceTermsSales invoiceTermsSales = (InvoiceTermsSales) kwlCommonTablesDAOObj.getClassObject(InvoiceTermsSales.class.getName(), itr.next().toString());
                            if (invoiceTermsSales != null) {
                                termid += invoiceTermsSales.getId() + ",";
                                termname += invoiceTermsSales.getTerm() + ",";;
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(termid)) {
                            termid = termid.substring(0, termid.length() - 1);
                        }
                        if (!StringUtil.isNullOrEmpty(termname)) {
                            termname = termname.substring(0, termname.length() - 1);
                        }
                        obj.put("termid", termid);
                        obj.put("termname", termname);
                    }
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTaxJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
}
