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
package com.krawler.spring.mainaccounting.service;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.costCenter.AccCostCenterDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.goodsreceipt.AccGoodsReceiptServiceDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFuctionality.ExportRecordHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.app.VelocityEngine;

/**
 *
 * @author krawler
 */
public class AccInvoiceServiceImpl implements AccInvoiceService {
    
    private accInvoiceDAO accInvoiceDAOobj;
    private accDebitNoteDAO accDebitNoteobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accReceiptDAO accReceiptDAOobj;
    private accAccountDAO accAccountDAOobj;
    private authHandlerDAO authHandlerDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private AccCostCenterDAO accCostCenterObj;
    private accInvoiceCMN accInvoiceCommon;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accTaxDAO accTaxObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accProductDAO accProductObj;
    private VelocityEngine velocityEngine;
    private CustomDesignDAO customDesignDAOObj;
    private AccInvoiceServiceDAO accInvoiceServiceDAOObj;
    private AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj;

    public void setAccInvoiceServiceDAOObj(AccInvoiceServiceDAO accInvoiceServiceDAOObj) {
        this.accInvoiceServiceDAOObj = accInvoiceServiceDAOObj;
    }

    public void setAccGoodsReceiptServiceDAOObj(AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj) {
        this.accGoodsReceiptServiceDAOObj = accGoodsReceiptServiceDAOObj;
    }
            
            
    
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
     public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
       public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }  
        public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }
    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
     public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
      public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
     public void setaccCostCenterDAO (AccCostCenterDAO accCostCenterDAOObj) {
        this.accCostCenterObj = accCostCenterDAOObj;
    }
    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
     public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
      public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    public void setvelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public void setcustomDesignDAO(CustomDesignDAO customDesignDAOObj) {
        this.customDesignDAOObj = customDesignDAOObj;
    }
    public JSONObject getInvoicesMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try{
            boolean consolidateFlag = request.getParameter("consolidateFlag")!=null?Boolean.parseBoolean(request.getParameter("consolidateFlag")):false;            
            String[] companyids = (consolidateFlag && request.getParameter("companyids")!=null)?request.getParameter("companyids").split(","):sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid")!=null)?request.getParameter("gcurrencyid"):sessionHandlerImpl.getCurrencyID(request);            
            boolean isAged = request.getParameter("isAged")!=null?Boolean.parseBoolean(request.getParameter("isAged")):false;
            boolean isForTemplate = false;  
            boolean onlyOutstanding =false;
            boolean report=false;
            int totalCount = 0;
            if(!StringUtil.isNullOrEmpty(request.getParameter("isForTemplate"))){
                isForTemplate = Boolean.parseBoolean(request.getParameter("isForTemplate"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("onlyOutsatnding"))){
                onlyOutstanding = Boolean.parseBoolean(request.getParameter("onlyOutsatnding"));
            }           
            if(!StringUtil.isNullOrEmpty(request.getParameter("report"))){
                report = Boolean.parseBoolean(request.getParameter("report"));
            }           
            boolean eliminateflag = consolidateFlag;
            HashMap requestParams = getInvoiceRequestMap(request);
            String dir = "";
            String sort = "";
            if(!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))){
                dir = request.getParameter("dir");
                 sort = request.getParameter("sort");
                   requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            String companyid = "";
            for(int cnt=0; cnt<companyids.length; cnt++) {
                companyid = companyids[cnt];                
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                request.setAttribute("onlyOutstanding", onlyOutstanding);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                requestParams.put("consolidateFlag", consolidateFlag);
                requestParams.put("isForTemplate", isForTemplate);
                requestParams.put("datefilter", request.getParameter("datefilter"));
                requestParams.put("custVendorID", request.getParameter("custVendorID")); 
                requestParams.put("start", request.getParameter("start"));
                requestParams.put("limit", request.getParameter("limit"));
                KwlReturnObject result = accInvoiceDAOobj.getInvoicesMerged(requestParams);
                totalCount=result!=null?result.getRecordTotalCount():0;
                List list = result.getEntityList();
                DataJArr = getInvoiceJsonMerged(request, list, DataJArr);
            }
             
            companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyid", companyid);
            if (isAged) {              
                requestParams.put("agedReport", true);
                
                JSONArray OBJArryInvoice = new JSONArray();
                KwlReturnObject result = accInvoiceDAOobj.getOpeningBalanceInvoices(requestParams);
                OBJArryInvoice = getAgedOpeningBalanceInvoiceJson(requestParams, result.getEntityList(), OBJArryInvoice);
                for (int i = 0; i < OBJArryInvoice.length(); i++) {
                    DataJArr.put(OBJArryInvoice.get(i));
                }

                JSONArray OBJArryDebitNote = new JSONArray();
                result  = accDebitNoteobj.getOpeningBalanceCustomerDNs(requestParams);
                OBJArryDebitNote = getAgedOpeningBalanceDebitNoteJson(requestParams, result.getEntityList(), OBJArryDebitNote);
                for (int i = 0; i < OBJArryDebitNote.length(); i++) {
                    DataJArr.put(OBJArryDebitNote.get(i));
                }
                
                JSONArray OBJArryCreditNote = new JSONArray();
                result =accCreditNoteDAOobj.getOpeningBalanceCNs(requestParams);
                OBJArryCreditNote = getAgedOpeningBalanceCreditNoteJson(requestParams, result.getEntityList(), OBJArryCreditNote);
                for (int i = 0; i < OBJArryCreditNote.length(); i++) {
                    DataJArr.put(OBJArryCreditNote.get(i));
                }
                
                JSONArray OBJArryPayment = new JSONArray();
                result =accReceiptDAOobj.getOpeningBalanceReceipts(requestParams);
                OBJArryPayment = getAgedOpeningBalanceReceiptJson(requestParams, result.getEntityList(), OBJArryPayment);
                for (int i = 0; i < OBJArryPayment.length(); i++) {
                    DataJArr.put(OBJArryPayment.get(i));
                }
                
                JSONArray CreditNotejArr = new JSONArray();
                result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                totalCount+=result.getRecordTotalCount();
                CreditNotejArr = getCreditNotesMergedJson(requestParams, result.getEntityList(), CreditNotejArr);
                for (int i = 0; i < CreditNotejArr.length(); i++) {
                    DataJArr.put(CreditNotejArr.get(i));
                }
                
                requestParams.put("cntype", 4);
                JSONArray DebitNotejArr = new JSONArray();//This is used for getting DN gainst vendor and otherwise 
                result = accDebitNoteobj.getDebitNoteMerged(requestParams);
                totalCount+=result.getRecordTotalCount();
                DebitNotejArr = getDebitNotesMergedJson(requestParams, result.getEntityList(), DebitNotejArr);   
                for(int i=0; i<DebitNotejArr.length();i++){
                  DataJArr.put(DebitNotejArr.get(i));    
                }
                
                requestParams.put("isadvancepayment", true);//Only Advance payment type make payments required. so it is true 
                JSONArray receivePaymentJArr = new JSONArray();
                result = accReceiptDAOobj.getReceipts(requestParams);   
                totalCount+=result.getRecordTotalCount();
                receivePaymentJArr = getReceiptsJson(requestParams, result.getEntityList(),receivePaymentJArr);
                for (int i = 0; i < receivePaymentJArr.length(); i++) {
                    DataJArr.put(receivePaymentJArr.get(i));
                }
            }
            
            if(request.getParameter("minimumAmountDue")!=null){
               JSONArray temp = new JSONArray();
               double minimumAmountDue = Double.parseDouble(request.getParameter("minimumAmountDue").toString());
               for(int i = 0; i < DataJArr.length(); i++) {
                   if(DataJArr.getJSONObject(i).getDouble("amountdue") >= minimumAmountDue)
                       temp.put(DataJArr.getJSONObject(i));
               }
               DataJArr = temp;
            }
            if(onlyOutstanding){
                JSONArray temp = new JSONArray();
               for(int i = 0; i < DataJArr.length(); i++) {
                   if(DataJArr.getJSONObject(i).getDouble("amountdue") >= 1)
                       temp.put(DataJArr.getJSONObject(i));
               }
               DataJArr = temp;
            }    
            int count = DataJArr.length();
            JSONArray pagedJson = DataJArr;
            if(consolidateFlag){
                String start = request.getParameter("start");
                String limit = request.getParameter("limit");
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                }
            }        
            if(isAged){
                //show multigrouping JSONArray required to sort    
               String sortKey = "type";
               JSONArray array = AccountingManager.sortJsonArrayOnStringValues(pagedJson,sortKey,true);
               sortKey = "personname";
               array = AccountingManager.sortJsonArrayOnStringValues(array,sortKey,true);
               jobj.put("data", array);  
            } else{
               jobj.put("data", pagedJson); 
            }  
            if(report || isAged){
                jobj.put("count", totalCount);
            }else{
                jobj.put("count", count);
            }
            issuccess = true;
        } catch (Exception ex){
            msg = ""+ex.getMessage();
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       return jobj;
    }
 
    
        public static HashMap<String, Object> getInvoiceRequestMap (HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(CCConstants.REQ_costCenterId,request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        requestParams.put(InvoiceConstants.accid, request.getParameter(InvoiceConstants.accid));
        requestParams.put(InvoiceConstants.cashonly, request.getParameter(InvoiceConstants.cashonly));
        requestParams.put(InvoiceConstants.creditonly, request.getParameter(InvoiceConstants.creditonly));
        boolean fullPaidFlag = StringUtil.getBoolean(request.getParameter("fullPaidFlag"));
        requestParams.put(InvoiceConstants.ignorezero, fullPaidFlag?"false":request.getParameter(InvoiceConstants.ignorezero));
        requestParams.put(InvoiceConstants.persongroup, request.getParameter(InvoiceConstants.persongroup));
        requestParams.put(InvoiceConstants.isagedgraph, request.getParameter(InvoiceConstants.isagedgraph));
        requestParams.put(InvoiceConstants.curdate, request.getParameter(InvoiceConstants.curdate));
        requestParams.put(InvoiceConstants.customerid, request.getParameter(InvoiceConstants.customerid));
        requestParams.put(InvoiceConstants.newcustomerid, request.getParameter(InvoiceConstants.newcustomerid));
        requestParams.put(InvoiceConstants.customerCategoryid,request.getParameter(InvoiceConstants.customerCategoryid));
        requestParams.put(InvoiceConstants.deleted, request.getParameter(InvoiceConstants.deleted));
        requestParams.put(InvoiceConstants.nondeleted, request.getParameter(InvoiceConstants.nondeleted));
        requestParams.put(InvoiceConstants.billid, request.getParameter(InvoiceConstants.billid));
        requestParams.put(InvoiceConstants.getRepeateInvoice, request.getParameter(InvoiceConstants.getRepeateInvoice));
        requestParams.put(InvoiceConstants.isSalesCommissionStmt, request.getParameter(InvoiceConstants.isSalesCommissionStmt));
        requestParams.put(InvoiceConstants.userid, request.getParameter(InvoiceConstants.userid));
        requestParams.put(InvoiceConstants.onlyamountdue,request.getParameter(InvoiceConstants.REQ_onlyAmountDue));
        requestParams.put(Constants.REQ_startdate ,request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate ,request.getParameter(Constants.REQ_enddate));
        requestParams.put("pendingapproval" ,(request.getParameter("pendingapproval") != null)? Boolean.parseBoolean(request.getParameter("pendingapproval")): false);
        requestParams.put("istemplate" ,(request.getParameter("istemplate") != null)? Integer.parseInt(request.getParameter("istemplate")): 0);
        requestParams.put(InvoiceConstants.productid ,(request.getParameter(InvoiceConstants.productid)== null)? "" :  request.getParameter(InvoiceConstants.productid));
        requestParams.put(InvoiceConstants.productCategoryid, request.getParameter(InvoiceConstants.productCategoryid));
        requestParams.put(InvoiceConstants.termid ,(request.getParameter(InvoiceConstants.termid)== null)? "" :  request.getParameter(InvoiceConstants.termid));
        requestParams.put(InvoiceConstants.prodfiltercustid ,(request.getParameter(InvoiceConstants.prodfiltercustid)== null)? "" :  request.getParameter(InvoiceConstants.prodfiltercustid));
       requestParams.put("currencyfilterfortrans", (request.getParameter("currencyfilterfortrans") == null)? "" : request.getParameter("currencyfilterfortrans"));
        requestParams.put(Constants.Acc_Search_Json ,request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria ,request.getParameter(InvoiceConstants.Filter_Criteria));
        requestParams.put(Constants.moduleid ,request.getParameter(Constants.moduleid));
        requestParams.put(InvoiceConstants.MARKED_FAVOURITE, request.getParameter(InvoiceConstants.MARKED_FAVOURITE));
        requestParams.put("isOpeningBalanceInvoices", request.getParameter("isOpeningBalanceInvoices"));
        requestParams.put("direction", (request.getParameter("direction") == null)? "" : request.getParameter("direction"));
        requestParams.put("isLifoFifo", (request.getParameter("isLifoFifo") == null)? "" : request.getParameter("isLifoFifo"));
        requestParams.put(InvoiceConstants.salesPersonid, (request.getParameter(InvoiceConstants.salesPersonid) == null)? "" : request.getParameter(InvoiceConstants.salesPersonid));
        requestParams.put("custVendorID", request.getParameter("custVendorID"));
        requestParams.put("datefilter", request.getParameter("datefilter"));
        requestParams.put(InvoiceConstants.duration ,(request.getParameter(InvoiceConstants.duration) != null)? Integer.parseInt(request.getParameter(InvoiceConstants.duration)): 0);
        if(request.getParameter("isReceipt")!=null){
            requestParams.put("isReceipt" ,request.getParameter("isReceipt"));
        }
        return requestParams;
    }
        
          public JSONArray getInvoiceJsonMerged(HttpServletRequest request, List list, JSONArray jArr) throws SessionExpiredException, ServiceException {
//        JSONObject jobj=new JSONObject();        
//        JSONArray jArr=new JSONArray();
        try{
            HashMap requestParams = getInvoiceRequestMap(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = AccountingManager.getCompanyidFromRequest(request);
            boolean ignoreZero = request.getParameter("ignorezero") != null ? Boolean.parseBoolean(request.getParameter("ignorezero")) : false;
            boolean onlyAmountDue = requestParams.get("onlyamountdue") != null? Boolean.parseBoolean(request.getParameter("onlyamountdue")) : false;
            boolean onlyOutstanding=request.getAttribute("onlyOutstanding")!=null ? Boolean.parseBoolean(request.getParameter("onlyOutstanding")) : false;
            boolean invoiceReport=false; // ((Boolean) request.getAttribute("report")) : false;
             if(!StringUtil.isNullOrEmpty(request.getParameter("report"))){
                invoiceReport = Boolean.parseBoolean(request.getParameter("report"));
            }
            boolean isSOA=request.getAttribute("isSOA")!=null ?(Boolean)request.getAttribute("isSOA") : false;
            int datefilter = StringUtil.getInteger(request.getParameter("datefilter"));// 0 = Invoice Due date OR 1 = Invoice date
//            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
//            String cashAccount = pref.getCashAccount().getID();
            boolean isSalesCommissionStmt=(requestParams.containsKey(InvoiceConstants.isSalesCommissionStmt))?Boolean.parseBoolean((String) requestParams.get(InvoiceConstants.isSalesCommissionStmt)):false;
            boolean isProduct = (requestParams.containsKey(InvoiceConstants.productid) && !StringUtil.isNullOrEmpty((String)requestParams.get(InvoiceConstants.productid)))? true : false;
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), AccountingManager.getGlobalCurrencyidFromRequest(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            int duration = 30;
            double commission=0;
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap=null;
            if(onlyOutstanding){
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn,"customfield"));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Invoice_ModuleId,0,1));                 
                     FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }
            if(isSalesCommissionStmt){                

                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(requestParams.get(Constants.companyKey));
                requestParams.put(Constants.filterNamesKey, filter_names);
                requestParams.put(Constants.filterParamsKey, filter_params);

                KwlReturnObject result = accCostCenterObj.getSalesCommission(requestParams);
                List<SalesCommission> salesCommissions = result.getEntityList();
                commission=salesCommissions.get(0).getCommission();               
                
            }
            String curDateString = "";
            Date curDate = null;
            boolean booleanAged = false;//Added for aged payable/receivable
            
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            
            if(requestParams.get("curdate") != null){//Added for aged payable/receivable
                curDateString = (String) requestParams.get("curdate");
                curDate = df.parse(curDateString);
                booleanAged = true;
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            }
            
           
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;

     
            String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
            cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);

            String cal2String = authHandler.getDateOnlyFormat().format(cal2.getTime());
            cal2Date = authHandler.getDateOnlyFormat().parse(cal2String);

            String cal3String = authHandler.getDateOnlyFormat().format(cal3.getTime());
            cal3Date = authHandler.getDateOnlyFormat().parse(cal3String);

            String cal4String = authHandler.getDateOnlyFormat().format(cal4.getTime());
            cal4Date = authHandler.getDateOnlyFormat().parse(cal4String);

            String cal5String = authHandler.getDateOnlyFormat().format(cal5.getTime());
            cal5Date = authHandler.getDateOnlyFormat().parse(cal5String);

            String cal6String = authHandler.getDateOnlyFormat().format(cal6.getTime());
            cal6Date = authHandler.getDateOnlyFormat().parse(cal6String);

            String cal7String = authHandler.getDateOnlyFormat().format(cal7.getTime());
            cal7Date = authHandler.getDateOnlyFormat().parse(cal7String);

            
            double amountdue1 = 0;
            double amountdue2 = 0;
            double amountdue3 = 0;
            double amountdue4 = 0;
            double amountdue5 = 0;
            double amountdue6 = 0;
            double amountdue7 = 0;
            double amountdue8 = 0;
            double amountWD = 0;
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                
                //Invoice invoice = (Invoice) itr.next();

                Object[] oj = (Object[])itr.next();                
                String invid = oj[0].toString();
                //Withoutinventory 0 for normal, 1 for billing
                boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());
                {
                    amountdue1 = amountdue2 = amountdue3 = amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountWD=0;
                    double taxPercent = 0;
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                    Invoice invoice = (Invoice) objItr.getEntityList().get(0);
                    
                    Date invoiceCreationDate = invoice.getCreationDate();
                    Double externalCurrencyRate = 0d;
                    Double invoiceOriginalAmount = 0d;
                    if(invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()){
                        ExchangeRateDetails erd = invoice.getExchangeRateDetail();
                        externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                        invoiceOriginalAmount = invoice.getOriginalOpeningBalanceAmount();
                    }
                    
                    JournalEntry je = null;
                    if(invoice.isNormalInvoice()){
                        je = invoice.getJournalEntry();
//                        invoiceCreationDate = je.getEntryDate();
                        externalCurrencyRate = je.getExternalCurrencyRate();
                    }
                    
                    JournalEntryDetail d = null;
                    if(invoice.isNormalInvoice()){
                        d = invoice.getCustomerEntry();
                        invoiceOriginalAmount = d.getAmount();
                    }
                    
                    Account account = null;
                    if(invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()){
                        account = invoice.getCustomer().getAccount();
//                        KwlReturnObject accObjItr = accountingHandlerDAOobj.getObject(Account.class.getName(), invoice.getCustomer().getID());
//                        account = (Account) accObjItr.getEntityList().get(0);
                    }else{
                        account = d.getAccount();
                    }
                    
                    String currencyid=(invoice.getCurrency()==null?currency.getCurrencyID(): invoice.getCurrency().getCurrencyID());
                    List ll = null;
                    if(invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()){
                        ll = new ArrayList();
                        ll.add(invoice.getOpeningBalanceAmountDue());
                        ll.add(0.0);
                        ll.add(0.0);
                    }else{
                        if(Constants.InvoiceAmountDueFlag) {
                            ll = accInvoiceCommon.getInvoiceDiscountAmountInfo(requestParams, invoice);
                        } else {
                            ll = accInvoiceCommon.getAmountDue_Discount(requestParams,invoice);
                        }
                    }
                    
                    double amountdue= (Double) ll.get(0);
                    double discountDeduct= (Double) ll.get(1);
                    amountWD= (Double) ll.get(2);
                    amountWD= amountWD - getInvDisountOnAmt(invoice.getID().toString(), amountWD,withoutinventory);
                    if(onlyAmountDue&&authHandler.round(amountdue,companyid)==0)
                        continue;
                   int isReval=0; 
                    if(invoiceReport && !invoice.isIsOpeningBalenceInvoice()){
                        KwlReturnObject brdAmt = accInvoiceDAOobj.getRevalFlag(invoice.getID());
                        List reval = brdAmt.getEntityList();
                        if(!reval.isEmpty() && (Long)reval.get(0) >0){
                            isReval=1;
                        }
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("billid", invoice.getID());
                    obj.put("isOpeningBalanceTransaction", invoice.isIsOpeningBalenceInvoice());
                    obj.put("isNormalTransaction", invoice.isNormalInvoice());
                    obj.put("companyid", invoice.getCompany().getCompanyID());
                    obj.put("companyname", invoice.getCompany().getCompanyName());
                    obj.put("withoutinventory", withoutinventory);
                    obj.put("partialinv", invoice.isPartialinv());
                    obj.put("personid", invoice.getCustomer() == null ? account.getID() : invoice.getCustomer().getID());
                    obj.put("personemail", invoice.getCustomer() == null ? "" : invoice.getCustomer().getEmail());
                    obj.put("customername", invoice.getCustomer() == null ? "" : invoice.getCustomer().getName());
                    obj.put("accid", account.getID());
                    obj.put("billno", invoice.getInvoiceNumber()); 
                    obj.put("currencyid",currencyid);
                    obj.put("currencyidval",authHandlerDAOObj.getCurrency(sessionHandlerImpl.getCurrencyID(request)));
                    obj.put("currencysymbol",(invoice.getCurrency()==null?currency.getSymbol(): invoice.getCurrency().getSymbol()));
                    obj.put("currencycode",(invoice.getCurrency()==null?currency.getCurrencyCode(): invoice.getCurrency().getCurrencyCode()));
                    obj.put("currencyname",(invoice.getCurrency()==null?currency.getName(): invoice.getCurrency().getName()));
                    obj.put("companyaddress", invoice.getCompany().getAddress());
                    obj.put("companyname", invoice.getCompany().getCompanyName());
                    obj.put("isfavourite", invoice.isFavourite());
                    obj.put("isprinted", invoice.isPrinted());
    //                obj.put("oldcurrencyrate", CompanyHandler.getBaseToCurrencyAmount(session,request,1.0,currencyid,je.getEntryDate()));
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, invoiceCreationDate, 0);
                    obj.put("oldcurrencyrate", (Double) bAmt.getEntityList().get(0));
                    obj.put("billto", invoice.getBillTo());
                    obj.put("shipto", invoice.getShipTo());
                    obj.put("journalentryid", (je != null?je.getID():""));
                    obj.put("porefno", invoice.getPoRefNumber());
                    obj.put("externalcurrencyrate", (je != null?je.getExternalCurrencyRate():externalCurrencyRate));
                    obj.put("entryno", (je != null?je.getEntryNumber():""));
                    obj.put("date", df.format(invoiceCreationDate));
                    obj.put("shipdate", invoice.getShipDate()==null? "" : df.format(invoice.getShipDate()));
                    obj.put("duedate", df.format(invoice.getDueDate()));
                    obj.put("personname", invoice.getCustomer()==null?account.getName():invoice.getCustomer().getName());
                    obj.put("salesPerson", invoice.getMasterSalesPerson()==null?"":invoice.getMasterSalesPerson().getID());
                    obj.put("memo", invoice.getMemo());
                    obj.put("termname",invoice.getCustomer()==null?"":((invoice.getCustomer().getCreditTerm()== null)?"":invoice.getCustomer().getCreditTerm().getTermname()));
                    obj.put("deleted", invoice.isDeleted());
                    obj.put("taxincluded", invoice.getTax() == null ? false : true);
                    obj.put("taxid", invoice.getTax() == null ? "" : invoice.getTax().getID());
                    obj.put("taxname", invoice.getTax() == null ? "" : invoice.getTax().getName());
                    obj.put("taxamount", invoice.getTaxEntry() == null ? 0 : invoice.getTaxEntry().getAmount());
                    obj.put("discount", invoice.getDiscount() == null ? 0 : invoice.getDiscount().getDiscountValue());
                    obj.put("ispercentdiscount", invoice.getDiscount()==null?false:invoice.getDiscount().isInPercent());
                    obj.put("discountval", invoice.getDiscount()==null?0:invoice.getDiscount().getDiscount());  
                    obj.put("shipvia", invoice.getShipvia()== null? "" : invoice.getShipvia());
                    obj.put("posttext", invoice.getPostText()== null? "" : invoice.getPostText());
                    obj.put("fob", invoice.getFob() == null?"":invoice.getFob());
                    obj.put("termdetails", getTermDetails(invoice.getID()));
                    obj.put("termdays", (invoice.getTermid()==null)? 0 : invoice.getTermid().getTermdays());
                    obj.put("isGIROFileGeneratedForUOBBank", invoice.isIsGIROFileGeneratedForUOBBank()?"Yes":"No");
                    obj.put("isGIROFileGeneratedForUOBBankForReport", invoice.isIsGIROFileGeneratedForUOBBank());
                    if(invoice.isIsGIROFileGeneratedForUOBBank()){
                        obj.put("paymentMethodUsedForUOB", invoice.getPaymentMethodUsedForUOB()!=null?invoice.getPaymentMethodUsedForUOB().getMethodName():"");
                    }
                    
                    BillingShippingAddresses addresses=invoice.getBillingShippingAddresses();
                    obj.put(Constants.BILLING_ADDRESS,addresses==null?(invoice.getBillTo()==null?"":invoice.getBillTo()):addresses.getBillingAddress());
                    obj.put(Constants.BILLING_CITY,addresses==null?"":addresses.getBillingCity());
                    obj.put(Constants.BILLING_CONTACT_PERSON,addresses==null?"":addresses.getBillingContactPerson());
                    obj.put(Constants.BILLING_CONTACT_PERSON_NUMBER,addresses==null?"":addresses.getBillingContactPersonNumber());
                    obj.put(Constants.BILLING_CONTACT_PERSON_DESIGNATION,addresses==null?"":addresses.getBillingContactPersonDesignation());
                    obj.put(Constants.BILLING_COUNTRY,addresses==null?"":addresses.getBillingCountry());
                    obj.put(Constants.BILLING_EMAIL,addresses==null?"":addresses.getBillingEmail());
                    obj.put(Constants.BILLING_FAX,addresses==null?"":addresses.getBillingFax());
                    obj.put(Constants.BILLING_MOBILE,addresses==null?"":addresses.getBillingMobile());
                    obj.put(Constants.BILLING_PHONE,addresses==null?"":addresses.getBillingPhone());
                    obj.put(Constants.BILLING_POSTAL,addresses==null?"":addresses.getBillingPostal());
                    obj.put(Constants.BILLING_STATE,addresses==null?"":addresses.getBillingState());
                    obj.put(Constants.BILLING_ADDRESS_TYPE,addresses==null?"":addresses.getBillingAddressType());
                    obj.put(Constants.SHIPPING_ADDRESS,addresses==null?(invoice.getShipTo()==null?"":invoice.getShipTo()):addresses.getShippingAddress());
                    obj.put(Constants.SHIPPING_CITY,addresses==null?"":addresses.getShippingCity());
                    obj.put(Constants.SHIPPING_CONTACT_PERSON,addresses==null?"":addresses.getShippingContactPerson());
                    obj.put(Constants.SHIPPING_CONTACT_PERSON_NUMBER,addresses==null?"":addresses.getShippingContactPersonNumber());
                    obj.put(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION,addresses==null?"":addresses.getShippingContactPersonDesignation());
                    obj.put(Constants.SHIPPING_COUNTRY,addresses==null?"":addresses.getShippingCountry());
                    obj.put(Constants.SHIPPING_EMAIL,addresses==null?"":addresses.getShippingEmail());
                    obj.put(Constants.SHIPPING_FAX,addresses==null?"":addresses.getShippingFax());
                    obj.put(Constants.SHIPPING_MOBILE,addresses==null?"":addresses.getShippingMobile());
                    obj.put(Constants.SHIPPING_PHONE,addresses==null?"":addresses.getShippingPhone());
                    obj.put(Constants.SHIPPING_POSTAL,addresses==null?"":addresses.getShippingPostal());
                    obj.put(Constants.SHIPPING_STATE,addresses==null?"":addresses.getShippingState());
                    obj.put(Constants.SHIPPING_ADDRESS_TYPE,addresses==null?"":addresses.getShippingAddressType());
                        
                    if(invoiceReport){ obj.put("isreval", isReval);}
                    int pendingApprovalInt = invoice.getPendingapproval();
                    obj.put("approvalstatusint", pendingApprovalInt);
                    if (pendingApprovalInt == Constants.LEVEL_ONE) {
                        obj.put("approvalstatus", "Pending level 1 approval");
                    } else if (pendingApprovalInt == Constants.LEVEL_TWO) {
                        obj.put("approvalstatus", "Pending level 2 approval");
                    } else {
                        obj.put("approvalstatus", "");
                    }
                    
                    if (invoice.getTemplateid() == null) {
                          obj.put("templateid", "");
                          obj.put("templatename", "");
                      } else {
                          obj.put("templateid", invoice.getTemplateid().getTempid());
                          obj.put("templatename", invoice.getTemplateid().getTempname());
                    }
                    obj.put("costcenterid", (je != null?je.getCostcenter()==null?"":je.getCostcenter().getID():""));
                    obj.put("costcenterName", (je != null?je.getCostcenter()==null?"":je.getCostcenter().getName():""));
                    obj.put("archieve", 0);
                    obj.put("cashtransaction", invoice.isCashtransaction());
                     boolean includeprotax = false;
                    Set<InvoiceDetail> invoiceDetails = invoice.getRows();
                    for (InvoiceDetail invoiceDetail : invoiceDetails) {
                         if (invoiceDetail.getTax() != null) {
                              includeprotax = true;
                              break;
                          }
                    }
                    obj.put("includeprotax", includeprotax);
                    if(invoice.getModifiedby()!=null){
                        obj.put("lasteditedby",invoice.getModifiedby().getFirstName()+" "+invoice.getModifiedby().getLastName());
                    }

                    /*For Product search, add Products details from Invoice details*/

                    if(isProduct && invoice.isNormalInvoice()) {
                        String idvString = isProduct ? oj[3].toString() : "";
                        KwlReturnObject objItrID = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), idvString);
                        InvoiceDetail idvObj = (InvoiceDetail) objItrID.getEntityList().get(0);

                        if(idvObj != null) {
                            obj.put("rowproductname", idvObj.getInventory().getProduct().getName());
//                            obj.put("rowquantity", idvObj.getInventory().isInvrecord() ? idvObj.getInventory().getQuantity() : idvObj.getInventory().getActquantity());
                            obj.put("rowquantity", idvObj.getInventory().getQuantity());
                            obj.put("rowrate", idvObj.getRate()); 

                            Discount disc = idvObj.getDiscount();
                            if (disc != null && disc.isInPercent()) {
                                obj.put("rowprdiscount", disc.getDiscount()); //product discount in percent
                            } else {
                                obj.put("rowprdiscount", 0);
                            }
                            double rowTaxPercent = 0;
                            if (idvObj.getTax() != null) {
//                                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), idvObj.getTax().getID());
                                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getCreationDate(), idvObj.getTax().getID());
                                rowTaxPercent = (Double) perresult.getEntityList().get(0);
                            }
                            obj.put("rowprtaxpercent", rowTaxPercent);


                        }
                    }



                    if (invoice.isCashtransaction()) {
                        obj.put("amountdue", 0);
                        obj.put("amountdueinbase",0);
                        obj.put("incash", true);
                    } else {
    //                    obj.put("amountdueinbase", CompanyHandler.getCurrencyToBaseAmount(session,request,amount - ramount,currencyid,je.getEntryDate()));  //amount left after apllying receipt and CN
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, currencyid, invoiceCreationDate,externalCurrencyRate);
                         obj.put("amountdueinbase", authHandler.round((Double) bAmt.getEntityList().get(0),companyid));
                         obj.put("amountdue", authHandler.round(amountdue,companyid));
                        if(booleanAged){
                            Date dueDate = null;
                                if (!StringUtil.isNullOrEmpty(df.format(invoice.getDueDate()))) {
                                    dueDate = df.parse(df.format(invoice.getDueDate()));
                                }
                            if(isSOA){
                                amountdue=authHandler.round((Double) bAmt.getEntityList().get(0),companyid);
                            }
                            if(datefilter==0)
                                 dueDate = df.parse(df.format(invoice.getDueDate()));
                            else 
                                 dueDate = df.parse(df.format(invoiceCreationDate)); 

                            if ((dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                                amountdue1 = authHandler.round(amountdue, companyid);
                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                                amountdue2 = authHandler.round(amountdue, companyid);
                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                                amountdue3 = authHandler.round(amountdue, companyid);
                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                                amountdue4 = authHandler.round(amountdue, companyid);
                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                                amountdue5 = authHandler.round(amountdue, companyid);
                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                                amountdue6 = authHandler.round(amountdue, companyid);
                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                                amountdue7 = authHandler.round(amountdue, companyid);
                            } else {
                                amountdue8 = authHandler.round(amountdue, companyid);
                            }
                        }

                       // obj.put("amountdue", amountdue);
                    }
                    obj.put("deductDiscount", discountDeduct);
                    obj.put("amountduenonnegative", (amountdue <= 0) ? 0 : authHandler.round(amountdue,companyid));
                    obj.put("amount", invoiceOriginalAmount);   //actual invoice amount
                    if(!invoiceReport){
                        obj.put("amountdue1", amountdue1);
                        obj.put("amountdue2",amountdue2);
                        obj.put("amountdue3", amountdue3);
                        obj.put("amountdue4", amountdue4);
                        obj.put("amountdue5", amountdue5);
                        obj.put("amountdue6", amountdue6);
                        obj.put("amountdue7", amountdue7);
                        obj.put("amountdue8", amountdue8);
                        obj.put("type", "Sales Invoice");
                    }
    //                obj.put("amountinbase", CompanyHandler.getCurrencyToBaseAmount(session,request,d.getAmount(),currencyid,je.getEntryDate()));
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoiceOriginalAmount, currencyid, invoiceCreationDate,externalCurrencyRate);
                    double amountinbase=(Double)bAmt.getEntityList().get(0);
                    obj.put("amountinbase",authHandler.round(amountinbase,companyid) );

                    if (invoice.getTax() != null) {
    //                    taxPercent = CompanyHandler.getTaxPercent(session, request, je.getEntryDate(), invoice.getTax().getID());
//                        KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, je.getEntryDate(), invoice.getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getCreationDate(), invoice.getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("taxpercent", taxPercent);  //tax in percent applyind on invoice
                    try {
                        obj.put("creditDays", invoice.getTermid().getTermdays());
                    } catch(Exception ex) {
                        obj.put("creditDays", 0);
                    }
                    RepeatedInvoices repeatedInvoice = invoice.getRepeateInvoice();
                    obj.put("isRepeated", repeatedInvoice==null?false:true);
                    if(repeatedInvoice!=null){
                        obj.put("repeateid",repeatedInvoice.getId());
                        obj.put("interval",repeatedInvoice.getIntervalUnit());
                        obj.put("intervalType",repeatedInvoice.getIntervalType());
                        SimpleDateFormat sdf=new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
    //                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"+sessionHandlerImpl.getTimeZoneDifference(request)));
                        obj.put("startDate",sdf.format(repeatedInvoice.getStartDate()));
                        obj.put("nextDate",sdf.format(repeatedInvoice.getNextDate()));
                        obj.put("expireDate",repeatedInvoice.getExpireDate()==null?"":sdf.format(repeatedInvoice.getExpireDate()));
                        requestParams.put("parentInvoiceId", invoice.getID());
                        KwlReturnObject details = accInvoiceDAOobj.getRepeateInvoicesDetails(requestParams);
                        List detailsList = details.getEntityList();
                        obj.put("childCount", detailsList.size());
                    }
                    if(onlyOutstanding && invoice.isNormalInvoice()){
                         Map<String, Object> variableMap = new HashMap<String, Object>();
                  HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                    ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
                    Detailfilter_names.add("companyid");
                    Detailfilter_params.add(invoice.getCompany().getCompanyID());
                    Detailfilter_names.add("journalentryId");
                    Detailfilter_params.add(invoice.getJournalEntry().getID());
                    Detailfilter_names.add("moduleId");
                    Detailfilter_params.add(Constants.Acc_Invoice_ModuleId+"");
                    invDetailRequestParams.put("filter_names", Detailfilter_names);
                    invDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresult = accJournalEntryobj.getJournalEntryCustomData(invDetailRequestParams);
                    if(idcustresult.getEntityList().size()>0) {
                        AccJECustomData jeCustom = (AccJECustomData) idcustresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap,variableMap);
                        DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateFromDB=null;
                        for (Entry<String, Object> varEntry : variableMap.entrySet()) {
                                 String coldata = varEntry.getValue().toString();
                                 if(customFieldMap.containsKey(varEntry.getKey())){
                                    FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), coldata);
                                   if(fieldComboData != null){
                                       obj.put(varEntry.getKey(), fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                                   }
                              }else if(customDateFieldMap.containsKey(varEntry.getKey())){
                                  DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                     try {
                                         dateFromDB = defaultDateFormat.parse(coldata);
                                         coldata = sdf.format(dateFromDB);
                                     } catch (Exception e) {
                                     }
                                  obj.put(varEntry.getKey(),coldata);
                              }else{
                                       if (!StringUtil.isNullOrEmpty(coldata)) {
                                        obj.put(varEntry.getKey(), coldata);
                                        }
                                   }
                            }
                    }
                    }
                if(isSalesCommissionStmt && !invoice.isIsOpeningBalenceInvoice()){
                        double remainingAmount=obj.getDouble("amountdue");
                        double invoiceAmount=obj.getDouble("amount");
                        double paidAmount=invoiceAmount-remainingAmount;
                        double difference=amountWD-paidAmount;
                        if(paidAmount==0){
                            obj.put("amountDueStatus","UnPaid");
                            obj.put("amountwithouttax",difference);
                            double commissionamount = difference*commission/100;
                            obj.put("commission",commissionamount);
                            
//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, difference, currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, difference, currencyid, invoiceCreationDate,je.getExternalCurrencyRate());
                            double differenceinbase=(Double)bAmt.getEntityList().get(0);
                            obj.put("amountwithouttaxinbase", authHandler.round(differenceinbase ,companyid));
                            
//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, commissionamount, currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, commissionamount, currencyid, invoiceCreationDate,je.getExternalCurrencyRate());
                            commissionamount=(Double)bAmt.getEntityList().get(0);
                            obj.put("commissioninbase",commissionamount);
                            
                        }else if(difference>0){                            
                            JSONObject ab1 = new JSONObject(obj.toString());
                            ab1.put("amountDueStatus","UnPaid");                            
                            ab1.put("amountwithouttax",difference);                            
                            double commissionamount = difference*commission/100;
                            ab1.put("commission",commissionamount);
                            
//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, difference, currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, difference, currencyid, invoiceCreationDate,je.getExternalCurrencyRate());
                            double differenceinbase=(Double)bAmt.getEntityList().get(0);
                            ab1.put("amountwithouttaxinbase", authHandler.round(differenceinbase ,companyid));
                            
//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, commissionamount, currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, commissionamount, currencyid, invoiceCreationDate,je.getExternalCurrencyRate());
                            commissionamount=(Double)bAmt.getEntityList().get(0);
                            ab1.put("commissioninbase",commissionamount);
                            jArr.put(ab1);                                                        
                            obj.put("amountDueStatus","Paid");                            
                            obj.put("amountwithouttax",paidAmount);                            
                            obj.put("commission",(paidAmount*commission/100));
                        }else{
                            obj.put("amountDueStatus","Paid");
                            obj.put("amountwithouttax",amountWD);
                            double commissionamount = amountWD*commission/100;
                            obj.put("commission",commissionamount);
                            
//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountWD, currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountWD, currencyid, invoiceCreationDate,je.getExternalCurrencyRate());
                            double amountWDinbase=(Double)bAmt.getEntityList().get(0);
                            obj.put("amountwithouttaxinbase", authHandler.round(amountWDinbase ,companyid));
                            
//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, commissionamount, currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, commissionamount, currencyid, invoiceCreationDate,je.getExternalCurrencyRate());
                            commissionamount=(Double)bAmt.getEntityList().get(0);
                            obj.put("commissioninbase",commissionamount);
                        }                                                                                            
                    }  
                    if (!(ignoreZero && authHandler.round(amountdue,companyid) <= 0)) {
                        jArr.put(obj);
                    }
                }
                
                
            }
            if(request.getParameter("filename") != null){
            	if(request.getParameter("filename").equals("Aged Receivable")){
		            if(request.getParameter("filetype") != null){
			            if(request.getParameter("filetype").equals("print")){
			            	if(!request.getParameter("mode").equals("18")){
				            	double total = 0;
				            	for(int i = 0; i < jArr.length(); i++)
				            		total = total + (Double)jArr.getJSONObject(i).get("amountdueinbase");
				            	JSONObject obj1 = new JSONObject();
				            	obj1.put("amountdueinbase", total);
				            	obj1.put("billno", "Total Amount Due");
				            	jArr.put(obj1);
			            	}
			            }
		            }
            	}
            }
//            jobj.put("data", jArr);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getInvoiceJson : "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getInvoiceJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }
          
    public JSONArray getAgedOpeningBalanceInvoiceJson(HashMap<String, Object> requestParams, List list, JSONArray dataArray) {
        try {
            DateFormat df = (DateFormat) requestParams.get("df");
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration")!=null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            String companyid = (String) requestParams.get("companyid");
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            
            if (requestParams.get(InvoiceConstants.curdate) != null) {
                String curDateString = (String) requestParams.get(InvoiceConstants.curdate);
                Date curDate = df.parse(curDateString);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            }
            
     
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;


            String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
            cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);

            String cal2String = authHandler.getDateOnlyFormat().format(cal2.getTime());
            cal2Date = authHandler.getDateOnlyFormat().parse(cal2String);

            String cal3String = authHandler.getDateOnlyFormat().format(cal3.getTime());
            cal3Date = authHandler.getDateOnlyFormat().parse(cal3String);

            String cal4String = authHandler.getDateOnlyFormat().format(cal4.getTime());
            cal4Date = authHandler.getDateOnlyFormat().parse(cal4String);

            String cal5String = authHandler.getDateOnlyFormat().format(cal5.getTime());
            cal5Date = authHandler.getDateOnlyFormat().parse(cal5String);

            String cal6String = authHandler.getDateOnlyFormat().format(cal6.getTime());
            cal6Date = authHandler.getDateOnlyFormat().parse(cal6String);

            String cal7String = authHandler.getDateOnlyFormat().format(cal7.getTime());
            cal7Date = authHandler.getDateOnlyFormat().parse(cal7String);

            
            
            Iterator it = null;
            if (list != null) {
                it = list.iterator();
                while (it.hasNext()) {
                    
                    double amountdue1 = 0;
                    double amountdue2 = 0;
                    double amountdue3 = 0;
                    double amountdue4 = 0;
                    double amountdue5 = 0;
                    double amountdue6 = 0; 
                    double amountdue7 = 0;
                    double amountdue8 = 0;
                    
                    Invoice invoice = (Invoice) it.next();                    
                    if (invoice != null && invoice.getOpeningBalanceAmountDue() > 0) {
                        JSONObject invoiceJson = new JSONObject();
                        Date invoiceCreationDate=invoice.getCreationDate();
                        double amountdue = invoice.getOpeningBalanceAmountDue();
                        double exchangeRateForOtherCurrency = invoice.getExchangeRateForOpeningTransaction();
                        
                        invoiceJson.put(InvoiceConstants.billid, invoice.getID());
                        invoiceJson.put(InvoiceConstants.billno, invoice.getInvoiceNumber());
                        invoiceJson.put(InvoiceConstants.journalentryid, invoice.getJournalEntry() == null ? "" : invoice.getJournalEntry().getID());
                        invoiceJson.put(InvoiceConstants.withoutinventory, false);
                        invoiceJson.put(InvoiceConstants.currencysymbol, invoice.getCurrency() == null ? "" : invoice.getCurrency().getSymbol());
                        invoiceJson.put(InvoiceConstants.currencyid, (invoice.getCurrency() == null ? "" : invoice.getCurrency().getCurrencyID()));
                        invoiceJson.put(InvoiceConstants.currencyname, (invoice.getCurrency() == null ? "" : invoice.getCurrency().getName()));
                        invoiceJson.put(InvoiceConstants.entryno, "");
                        invoiceJson.put(InvoiceConstants.personid, invoice.getCustomer() == null ? "" : invoice.getCustomer().getID());
                        invoiceJson.put(InvoiceConstants.personname, invoice.getCustomer() == null ? "" : invoice.getCustomer().getName());
                        invoiceJson.put(InvoiceConstants.personinfo, invoice.getCustomer() == null ? "" : invoice.getCustomer().getName()+"("+invoice.getCustomer().getAcccode()+")");
                        invoiceJson.put(InvoiceConstants.duedate, df.format(invoice.getDueDate()));
                        invoiceJson.put(InvoiceConstants.date, df.format(invoiceCreationDate));
                        invoiceJson.put(InvoiceConstants.memo, invoice.getMemo() == null ? "" : invoice.getMemo());
                        invoiceJson.put(InvoiceConstants.termname, invoice.getTermid() == null ? "" : invoice.getTermid().getTermname());
                        invoiceJson.put(InvoiceConstants.deleted, invoice.isDeleted());
                        invoiceJson.put(InvoiceConstants.externalcurrencyrate, invoice.getExternalCurrencyRate());
                        double amountdueinbase =0d;
                        if (Constants.OpeningBalanceBaseAmountFlag) {
                            amountdueinbase = invoice.getOpeningBalanceBaseAmountDue();
                        } else {
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, invoice.getCurrency().getCurrencyID(), invoiceCreationDate, exchangeRateForOtherCurrency);
                            amountdueinbase = (Double) bAmt.getEntityList().get(0);
                        }
                        invoiceJson.put(InvoiceConstants.amountdue, amountdue);
                        invoiceJson.put(InvoiceConstants.amountdueinbase, amountdueinbase);
                        invoiceJson.put("type", "Sales Invoice");
                        
                        Date dueDate = df.parse(df.format(invoice.getDueDate()));       
                        
                        if ((dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                            amountdue1 = authHandler.round(amountdue, companyid);
                        } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                            amountdue2 = authHandler.round(amountdue, companyid);
                        } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                            amountdue3 = authHandler.round(amountdue, companyid);
                        } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                            amountdue4 = authHandler.round(amountdue, companyid);
                        } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                            amountdue5 = authHandler.round(amountdue, companyid);
                        } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                            amountdue6 = authHandler.round(amountdue, companyid);
                        } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                            amountdue7 = authHandler.round(amountdue, companyid);
                        } else {
                            amountdue8 = authHandler.round(amountdue, companyid);
                        }
                        
                        invoiceJson.put(InvoiceConstants.amountdue1, amountdue1);
                        invoiceJson.put(InvoiceConstants.amountdue2, amountdue2);
                        invoiceJson.put(InvoiceConstants.amountdue3, amountdue3);
                        invoiceJson.put(InvoiceConstants.amountdue4, amountdue4);
                        invoiceJson.put(InvoiceConstants.amountdue5, amountdue5);
                        invoiceJson.put(InvoiceConstants.amountdue6, amountdue6);
                        invoiceJson.put(InvoiceConstants.amountdue7, amountdue7);
                        invoiceJson.put(InvoiceConstants.amountdue8, amountdue8);
                        dataArray.put(invoiceJson);
                    }
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }   
    public JSONArray getAgedOpeningBalanceDebitNoteJson(HashMap<String, Object> requestParams, List list, JSONArray dataArray) {
        try {
            DateFormat df = (DateFormat) requestParams.get("df");
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration")!=null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            String companyid = (String) requestParams.get("companyid");
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            if (requestParams.get(InvoiceConstants.curdate) != null) {
                String curDateString = (String) requestParams.get(InvoiceConstants.curdate);
                Date curDate = df.parse(curDateString);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            }
            
            
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;

            String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
            cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);

            String cal2String = authHandler.getDateOnlyFormat().format(cal2.getTime());
            cal2Date = authHandler.getDateOnlyFormat().parse(cal2String);

            String cal3String = authHandler.getDateOnlyFormat().format(cal3.getTime());
            cal3Date = authHandler.getDateOnlyFormat().parse(cal3String);

            String cal4String = authHandler.getDateOnlyFormat().format(cal4.getTime());
            cal4Date = authHandler.getDateOnlyFormat().parse(cal4String);

            String cal5String = authHandler.getDateOnlyFormat().format(cal5.getTime());
            cal5Date = authHandler.getDateOnlyFormat().parse(cal5String);

            String cal6String = authHandler.getDateOnlyFormat().format(cal6.getTime());
            cal6Date = authHandler.getDateOnlyFormat().parse(cal6String);

            String cal7String = authHandler.getDateOnlyFormat().format(cal7.getTime());
            cal7Date = authHandler.getDateOnlyFormat().parse(cal7String);

            
            
            
            Iterator it = null;
            if (list != null) {
                it = list.iterator();
                while (it.hasNext()) {
                    
                    double amountdue1 = 0;
                    double amountdue2 = 0;
                    double amountdue3 = 0;
                    double amountdue4 = 0;
                    double amountdue5 = 0;
                    double amountdue6 = 0; 
                    double amountdue7 = 0;
                    double amountdue8 = 0;
                    
                    DebitNote dn = (DebitNote) it.next();
                    
                    if (dn != null && dn.getOpeningBalanceAmountDue() > 0) {
                        JSONObject invoiceJson = new JSONObject();
                        Date creationDate=dn.getCreationDate();
                        double amountdue = dn.getOpeningBalanceAmountDue();
                        double exchangeRateForOtherCurrency = dn.getExchangeRateForOpeningTransaction();
                        
                        invoiceJson.put(InvoiceConstants.billid, dn.getID());
                        invoiceJson.put(InvoiceConstants.billno, dn.getDebitNoteNumber());
                        invoiceJson.put(InvoiceConstants.journalentryid, dn.getJournalEntry() == null ? "" : dn.getJournalEntry().getID());
                        invoiceJson.put(InvoiceConstants.withoutinventory, false);
                        invoiceJson.put(InvoiceConstants.currencysymbol, dn.getCurrency() == null ? "" : dn.getCurrency().getSymbol());
                        invoiceJson.put(InvoiceConstants.currencyid, (dn.getCurrency() == null ? "" : dn.getCurrency().getCurrencyID()));
                        invoiceJson.put(InvoiceConstants.currencyname, (dn.getCurrency() == null ? "" : dn.getCurrency().getName()));
                        invoiceJson.put(InvoiceConstants.entryno, "");
                        invoiceJson.put(InvoiceConstants.personid, dn.getCustomer() == null ? "" : dn.getCustomer().getID());
                        invoiceJson.put(InvoiceConstants.personname, dn.getCustomer() == null ? "" : dn.getCustomer().getName());
                        invoiceJson.put(InvoiceConstants.personinfo, dn.getCustomer() == null ? "" : dn.getCustomer().getName()+"("+dn.getCustomer().getAcccode()+")");
                        invoiceJson.put(InvoiceConstants.duedate, df.format(creationDate));
                        invoiceJson.put(InvoiceConstants.date, df.format(creationDate));
                        invoiceJson.put(InvoiceConstants.memo, dn.getMemo() == null ? "" : dn.getMemo());
                        invoiceJson.put(InvoiceConstants.deleted, dn.isDeleted());
                        invoiceJson.put(InvoiceConstants.externalcurrencyrate, dn.getExternalCurrencyRate());
                        double openingBalanceAmountDueInBase = 0d;
                        if (Constants.OpeningBalanceBaseAmountFlag) {
                            openingBalanceAmountDueInBase = dn.getOpeningBalanceBaseAmountDue();
                        } else {
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, dn.getCurrency().getCurrencyID(), creationDate, exchangeRateForOtherCurrency);
                            openingBalanceAmountDueInBase = (Double) bAmt.getEntityList().get(0);
                        }
                        invoiceJson.put(InvoiceConstants.amountdue, amountdue);
                        invoiceJson.put(InvoiceConstants.amountdueinbase, openingBalanceAmountDueInBase);
                        invoiceJson.put("type", "Debit Note");
                        
                        Date dueDate = df.parse(df.format(creationDate));
                        
                        
                        if ((dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                            amountdue1 = authHandler.round(amountdue, companyid);
                        } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                            amountdue2 = authHandler.round(amountdue, companyid);
                        } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                            amountdue3 = authHandler.round(amountdue, companyid);
                        } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                            amountdue4 = authHandler.round(amountdue, companyid);
                        } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                            amountdue5 = authHandler.round(amountdue, companyid);
                        } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                            amountdue6 = authHandler.round(amountdue, companyid);
                        } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                            amountdue7 = authHandler.round(amountdue, companyid);
                        } else {
                            amountdue8 = authHandler.round(amountdue, companyid);
                        }
                        
                        invoiceJson.put(InvoiceConstants.amountdue1, amountdue1);
                        invoiceJson.put(InvoiceConstants.amountdue2, amountdue2);
                        invoiceJson.put(InvoiceConstants.amountdue3, amountdue3);
                        invoiceJson.put(InvoiceConstants.amountdue4, amountdue4);
                        invoiceJson.put(InvoiceConstants.amountdue5, amountdue5);
                        invoiceJson.put(InvoiceConstants.amountdue6, amountdue6);
                        invoiceJson.put(InvoiceConstants.amountdue7, amountdue7);
                        invoiceJson.put(InvoiceConstants.amountdue8, amountdue8);
                        dataArray.put(invoiceJson);
                    }
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }
   public JSONArray getAgedOpeningBalanceCreditNoteJson(HashMap<String, Object> requestParams, List list, JSONArray dataArray) {
        try {
            DateFormat df = (DateFormat) requestParams.get("df");
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration")!=null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            String companyid = (String) requestParams.get("companyid");
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            if (requestParams.get(InvoiceConstants.curdate) != null) {
                String curDateString = (String) requestParams.get(InvoiceConstants.curdate);
                Date curDate = df.parse(curDateString);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            }
            
         
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;

            String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
            cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);

            String cal2String = authHandler.getDateOnlyFormat().format(cal2.getTime());
            cal2Date = authHandler.getDateOnlyFormat().parse(cal2String);

            String cal3String = authHandler.getDateOnlyFormat().format(cal3.getTime());
            cal3Date = authHandler.getDateOnlyFormat().parse(cal3String);

            String cal4String = authHandler.getDateOnlyFormat().format(cal4.getTime());
            cal4Date = authHandler.getDateOnlyFormat().parse(cal4String);

            String cal5String = authHandler.getDateOnlyFormat().format(cal5.getTime());
            cal5Date = authHandler.getDateOnlyFormat().parse(cal5String);

            String cal6String = authHandler.getDateOnlyFormat().format(cal6.getTime());
            cal6Date = authHandler.getDateOnlyFormat().parse(cal6String);

            String cal7String = authHandler.getDateOnlyFormat().format(cal7.getTime());
            cal7Date = authHandler.getDateOnlyFormat().parse(cal7String);
            
            Iterator it = null;
            if (list != null) {
                it = list.iterator();
                while (it.hasNext()) {
                    
                    double amountdue1 = 0;
                    double amountdue2 = 0;
                    double amountdue3 = 0;
                    double amountdue4 = 0;
                    double amountdue5 = 0;
                    double amountdue6 = 0; 
                    double amountdue7 = 0;
                    double amountdue8 = 0;
                    
                    CreditNote cn = (CreditNote) it.next();                    
                    
                    if (cn != null && cn.getOpeningBalanceAmountDue() > 0) {
                        JSONObject invoiceJson = new JSONObject();
                        Date creationDate=cn.getCreationDate();
                        double amountdue = -cn.getOpeningBalanceAmountDue();//amount due will be negative for credit note
                        double exchangeRateForOtherCurrency = cn.getExchangeRateForOpeningTransaction();
                        
                        invoiceJson.put(InvoiceConstants.billid, cn.getID());
                        invoiceJson.put(InvoiceConstants.billno, cn.getCreditNoteNumber());
                        invoiceJson.put(InvoiceConstants.journalentryid, cn.getJournalEntry() == null ? "" : cn.getJournalEntry().getID());
                        invoiceJson.put(InvoiceConstants.withoutinventory, false);
                        invoiceJson.put(InvoiceConstants.currencysymbol, cn.getCurrency() == null ? "" : cn.getCurrency().getSymbol());
                        invoiceJson.put(InvoiceConstants.currencyid, (cn.getCurrency() == null ? "" : cn.getCurrency().getCurrencyID()));
                        invoiceJson.put(InvoiceConstants.currencyname, (cn.getCurrency() == null ? "" : cn.getCurrency().getName()));
                        invoiceJson.put(InvoiceConstants.entryno, "");
                        invoiceJson.put(InvoiceConstants.personid, cn.getCustomer() == null ? "" : cn.getCustomer().getID());
                        invoiceJson.put(InvoiceConstants.personname, cn.getCustomer() == null ? "" : cn.getCustomer().getName());
                        invoiceJson.put(InvoiceConstants.personinfo, cn.getCustomer() == null ? "" : cn.getCustomer().getName()+"("+cn.getCustomer().getAcccode()+")");
                        invoiceJson.put(InvoiceConstants.duedate, df.format(creationDate));
                        invoiceJson.put(InvoiceConstants.date, df.format(creationDate));
                        invoiceJson.put(InvoiceConstants.memo, cn.getMemo() == null ? "" : cn.getMemo());
                        invoiceJson.put(InvoiceConstants.deleted, cn.isDeleted());
                        invoiceJson.put(InvoiceConstants.externalcurrencyrate, cn.getExternalCurrencyRate());
                        double amountdueinbase = 0d;
                        if (Constants.OpeningBalanceBaseAmountFlag) {
                            amountdueinbase = -cn.getOpeningBalanceBaseAmountDue();
                        } else {
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, cn.getCurrency().getCurrencyID(), creationDate, exchangeRateForOtherCurrency);
                            amountdueinbase = (Double) bAmt.getEntityList().get(0);
                        }
                        invoiceJson.put(InvoiceConstants.amountdue, amountdue);
                        invoiceJson.put(InvoiceConstants.amountdueinbase, amountdueinbase);
                        invoiceJson.put("type", "Credit Note");
                        
                        Date dueDate = df.parse(df.format(creationDate));                    
                                  
                        if ((dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                            amountdue1 = authHandler.round(amountdue, companyid);
                        } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                            amountdue2 = authHandler.round(amountdue, companyid);
                        } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                            amountdue3 = authHandler.round(amountdue, companyid);
                        } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                            amountdue4 = authHandler.round(amountdue, companyid);
                        } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                            amountdue5 = authHandler.round(amountdue, companyid);
                        } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                            amountdue6 = authHandler.round(amountdue, companyid);
                        } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                            amountdue7 = authHandler.round(amountdue, companyid);
                        } else {
                            amountdue8 = authHandler.round(amountdue, companyid);
                        }

                        invoiceJson.put(InvoiceConstants.amountdue1, amountdue1);
                        invoiceJson.put(InvoiceConstants.amountdue2, amountdue2);
                        invoiceJson.put(InvoiceConstants.amountdue3, amountdue3);
                        invoiceJson.put(InvoiceConstants.amountdue4, amountdue4);
                        invoiceJson.put(InvoiceConstants.amountdue5, amountdue5);
                        invoiceJson.put(InvoiceConstants.amountdue6, amountdue6);
                        invoiceJson.put(InvoiceConstants.amountdue7, amountdue7);
                        invoiceJson.put(InvoiceConstants.amountdue8, amountdue8);
                        dataArray.put(invoiceJson);
                    }
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    }  
      public JSONArray getAgedOpeningBalanceReceiptJson(HashMap<String, Object> requestParams, List list, JSONArray dataArray) {
        try {
            DateFormat df = (DateFormat) requestParams.get("df");
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration")!=null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            String companyid = (String) requestParams.get("companyid");
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            if (requestParams.get(InvoiceConstants.curdate) != null) {
                String curDateString = (String) requestParams.get(InvoiceConstants.curdate);
                Date curDate = df.parse(curDateString);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            }
            
         
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;

            String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
            cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);

            String cal2String = authHandler.getDateOnlyFormat().format(cal2.getTime());
            cal2Date = authHandler.getDateOnlyFormat().parse(cal2String);

            String cal3String = authHandler.getDateOnlyFormat().format(cal3.getTime());
            cal3Date = authHandler.getDateOnlyFormat().parse(cal3String);

            String cal4String = authHandler.getDateOnlyFormat().format(cal4.getTime());
            cal4Date = authHandler.getDateOnlyFormat().parse(cal4String);

            String cal5String = authHandler.getDateOnlyFormat().format(cal5.getTime());
            cal5Date = authHandler.getDateOnlyFormat().parse(cal5String);

            String cal6String = authHandler.getDateOnlyFormat().format(cal6.getTime());
            cal6Date = authHandler.getDateOnlyFormat().parse(cal6String);

            String cal7String = authHandler.getDateOnlyFormat().format(cal7.getTime());
            cal7Date = authHandler.getDateOnlyFormat().parse(cal7String);
                        
            Iterator it = null;
            if (list != null) {
                it = list.iterator();
                while (it.hasNext()) {
                    
                    double amountdue1 = 0;
                    double amountdue2 = 0;
                    double amountdue3 = 0;
                    double amountdue4 = 0;
                    double amountdue5 = 0;
                    double amountdue6 = 0; 
                    double amountdue7 = 0;
                    double amountdue8 = 0;
                    
                    Receipt receipt = (Receipt) it.next();
                    if (receipt != null && receipt.getOpeningBalanceAmountDue() > 0) {
                        JSONObject invoiceJson = new JSONObject();
                        Date creationDate=receipt.getCreationDate();
                        double amountdue = -receipt.getOpeningBalanceAmountDue();//amount due will be negative for receipt payment
                        double exchangeRateForOtherCurrency = receipt.getExchangeRateForOpeningTransaction();
                        
                        invoiceJson.put(InvoiceConstants.billid, receipt.getID());
                        invoiceJson.put(InvoiceConstants.billno, receipt.getReceiptNumber());
                        invoiceJson.put(InvoiceConstants.journalentryid, receipt.getJournalEntry() == null ? "" : receipt.getJournalEntry().getID());
                        invoiceJson.put(InvoiceConstants.withoutinventory, false);
                        invoiceJson.put(InvoiceConstants.currencysymbol, receipt.getCurrency() == null ? "" : receipt.getCurrency().getSymbol());
                        invoiceJson.put(InvoiceConstants.currencyid, (receipt.getCurrency() == null ? "" : receipt.getCurrency().getCurrencyID()));
                        invoiceJson.put(InvoiceConstants.currencyname, (receipt.getCurrency() == null ? "" : receipt.getCurrency().getName()));
                        invoiceJson.put(InvoiceConstants.entryno, "");
                        invoiceJson.put(InvoiceConstants.personid, receipt.getCustomer() == null ? "" : receipt.getCustomer().getID());
                        invoiceJson.put(InvoiceConstants.personname, receipt.getCustomer() == null ? "" : receipt.getCustomer().getName());
                        invoiceJson.put(InvoiceConstants.personinfo, receipt.getCustomer() == null ? "" : receipt.getCustomer().getName()+"("+receipt.getCustomer().getAcccode()+")");
                        invoiceJson.put(InvoiceConstants.duedate, df.format(creationDate));
                        invoiceJson.put(InvoiceConstants.date, df.format(creationDate));
                        invoiceJson.put(InvoiceConstants.memo, receipt.getMemo() == null ? "" : receipt.getMemo());
                        invoiceJson.put(InvoiceConstants.deleted, receipt.isDeleted());
                        invoiceJson.put(InvoiceConstants.externalcurrencyrate, receipt.getExternalCurrencyRate());
                        double openingBalanceAmountDueInBase = 0d;
                        if (Constants.OpeningBalanceBaseAmountFlag) {
                            openingBalanceAmountDueInBase = -receipt.getOpeningBalanceBaseAmountDue();
                        } else {
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, receipt.getCurrency().getCurrencyID(), creationDate, exchangeRateForOtherCurrency);
                            openingBalanceAmountDueInBase = (Double) bAmt.getEntityList().get(0);
                        }
                        invoiceJson.put(InvoiceConstants.amountdue, amountdue);
                        invoiceJson.put(InvoiceConstants.amountdueinbase, openingBalanceAmountDueInBase);
                        invoiceJson.put("type", "Payment Received");
                        
                        Date dueDate = df.parse(df.format(creationDate));
                        
                        if ((dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                            amountdue1 = authHandler.round(amountdue, companyid);
                        } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                            amountdue2 = authHandler.round(amountdue, companyid);
                        } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                            amountdue3 = authHandler.round(amountdue, companyid);
                        } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                            amountdue4 = authHandler.round(amountdue, companyid);
                        } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                            amountdue5 = authHandler.round(amountdue, companyid);
                        } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                            amountdue6 = authHandler.round(amountdue, companyid);
                        } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                            amountdue7 = authHandler.round(amountdue, companyid);
                        } else {
                            amountdue8 = authHandler.round(amountdue, companyid);
                        }
                        
                        invoiceJson.put(InvoiceConstants.amountdue1, amountdue1);
                        invoiceJson.put(InvoiceConstants.amountdue2, amountdue2);
                        invoiceJson.put(InvoiceConstants.amountdue3, amountdue3);
                        invoiceJson.put(InvoiceConstants.amountdue4, amountdue4);
                        invoiceJson.put(InvoiceConstants.amountdue5, amountdue5);
                        invoiceJson.put(InvoiceConstants.amountdue6, amountdue6);
                        invoiceJson.put(InvoiceConstants.amountdue7, amountdue7);
                        invoiceJson.put(InvoiceConstants.amountdue8, amountdue8);
                        dataArray.put(invoiceJson);
                    }
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return dataArray;
    } 
      
    public JSONArray getCreditNotesMergedJson(HashMap<String, Object> requestParams, List list, JSONArray JArr) throws ServiceException {
        try {
            String companyid = (String) requestParams.get("companyid");
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            int duration = (requestParams.containsKey(InvoiceConstants.duration) && requestParams.containsKey(InvoiceConstants.duration)) ? Integer.parseInt(requestParams.get(InvoiceConstants.duration).toString()) : 30;
            boolean agedReport=(requestParams.containsKey("agedReport") && requestParams.get("agedReport")!=null)?Boolean.parseBoolean(requestParams.get("agedReport").toString()):false;
            boolean isSOA=(requestParams.containsKey("isSOA") && requestParams.get("isSOA")!=null)?Boolean.parseBoolean(requestParams.get("isSOA").toString()):false;
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            if (requestParams.get(InvoiceConstants.curdate) != null) {
                String curDateString = (String) requestParams.get(InvoiceConstants.curdate);
                Date curDate = df.parse(curDateString);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            }
            
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;

            String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
            cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);

            String cal2String = authHandler.getDateOnlyFormat().format(cal2.getTime());
            cal2Date = authHandler.getDateOnlyFormat().parse(cal2String);

            String cal3String = authHandler.getDateOnlyFormat().format(cal3.getTime());
            cal3Date = authHandler.getDateOnlyFormat().parse(cal3String);

            String cal4String = authHandler.getDateOnlyFormat().format(cal4.getTime());
            cal4Date = authHandler.getDateOnlyFormat().parse(cal4String);

            String cal5String = authHandler.getDateOnlyFormat().format(cal5.getTime());
            cal5Date = authHandler.getDateOnlyFormat().parse(cal5String);

            String cal6String = authHandler.getDateOnlyFormat().format(cal6.getTime());
            cal6Date = authHandler.getDateOnlyFormat().parse(cal6String);

            String cal7String = authHandler.getDateOnlyFormat().format(cal7.getTime());
            cal7Date = authHandler.getDateOnlyFormat().parse(cal7String);
            
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                
                double amountdue1 = 0;
                double amountdue2 = 0;
                double amountdue3 = 0;
                double amountdue4 = 0;
                double amountdue5 = 0;
                double amountdue6 = 0;
                double amountdue7 = 0;
                double amountdue8 = 0;

                Object[] row = (Object[]) itr.next();
                boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                String personid = "";
                String personname = "";
                String billto = "";
                String cncurrencyid="";
                KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) row[2]);
                Customer customer = (Customer) resultObject.getEntityList().get(0);
                personid = customer!=null?customer.getID():"";
                personname = customer!=null?customer.getName():"";
                billto=customer.getBillingAddress()==null?"":customer.getBillingAddress();

                resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);

                JSONObject obj = new JSONObject();
                if (!withoutinventory) {
                    resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) row[1]);
                    CreditNote creditNote = (CreditNote) resultObject.getEntityList().get(0);
                    if (creditNote.isOtherwise() && creditNote.getCnamountdue()>0) {
                        JournalEntry je = creditNote.getJournalEntry();
                        cncurrencyid=creditNote.getCurrency() == null ? currency.getCurrencyID() : creditNote.getCurrency().getCurrencyID();
                        obj.put(InvoiceConstants.billid, creditNote.getID());
                        obj.put(InvoiceConstants.noteid, creditNote.getID());
                        obj.put(InvoiceConstants.noteno, creditNote.getCreditNoteNumber());
                        obj.put(InvoiceConstants.companyid, creditNote.getCompany().getCompanyID());
                        obj.put(InvoiceConstants.companyname, creditNote.getCompany().getCompanyName());
                        obj.put(InvoiceConstants.billno, creditNote.getCreditNoteNumber());
                        obj.put(InvoiceConstants.journalentryid, je.getID());
                        obj.put(InvoiceConstants.withoutinventory, withoutinventory);
                        obj.put(InvoiceConstants.currencysymbol, (creditNote.getCurrency() == null ? currency.getSymbol() : creditNote.getCurrency().getSymbol()));
                        obj.put(InvoiceConstants.currencyid, (creditNote.getCurrency() == null ? currency.getCurrencyID() : creditNote.getCurrency().getCurrencyID()));
                        obj.put(InvoiceConstants.currencyname, (creditNote.getCurrency() == null ? currency.getName() : creditNote.getCurrency().getName()));
                        obj.put(InvoiceConstants.entryno, je.getEntryNumber());
                        obj.put(InvoiceConstants.personid, personid);
                        obj.put(InvoiceConstants.personname, personname);
                        obj.put(InvoiceConstants.personinfo, creditNote.getCustomer() == null ? "" : creditNote.getCustomer().getName()+"("+creditNote.getCustomer().getAcccode()+")");
                        obj.put(InvoiceConstants.billto, billto);   
                        if (agedReport || isSOA) {
                            obj.put(InvoiceConstants.amount, creditNote.isOtherwise() ? -creditNote.getCnamount() : details.getAmount());
                            obj.put(InvoiceConstants.amountdue, creditNote.isOtherwise() ? -creditNote.getCnamountdue() : 0);
//                            obj.put(InvoiceConstants.amountdueinbase, -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, creditNote.getCnamountdue(), cncurrencyid, je.getEntryDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid));
                            obj.put(InvoiceConstants.amountdueinbase, -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, creditNote.getCnamountdue(), cncurrencyid, creditNote.getCreationDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid));
                        } else {
                            obj.put(InvoiceConstants.amount, creditNote.isOtherwise() ? creditNote.getCnamount() : details.getAmount());
                            obj.put(InvoiceConstants.amountdue, creditNote.isOtherwise() ? creditNote.getCnamountdue() : 0);
//                            obj.put(InvoiceConstants.amountdueinbase, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, creditNote.getCnamountdue(), cncurrencyid, je.getEntryDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid));
                            obj.put(InvoiceConstants.amountdueinbase, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, creditNote.getCnamountdue(), cncurrencyid, creditNote.getCreationDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid));
                        }
                        obj.put("currencyidval",authHandlerDAOObj.getCurrency(currencyid));
//                        obj.put(InvoiceConstants.duedate, df.format(je.getEntryDate()));
//                        obj.put(InvoiceConstants.date, df.format(je.getEntryDate()));
                        obj.put(InvoiceConstants.duedate, df.format(creditNote.getCreationDate()));
                        obj.put(InvoiceConstants.date, df.format(creditNote.getCreationDate()));
                        obj.put(InvoiceConstants.memo, creditNote.getMemo());
                        obj.put(InvoiceConstants.deleted, creditNote.isDeleted());
                        obj.put(InvoiceConstants.externalcurrencyrate, creditNote.getExternalCurrencyRate());
                        obj.put("type","Credit Note");
                        obj.put("isCN",true);

//                        Date dueDate = df.parse(df.format(je.getEntryDate()));
                        Date dueDate = df.parse(df.format(creditNote.getCreationDate()));
                        Double amountdue = 0.0;
                        if (agedReport) {//aged report view case
                            amountdue = -creditNote.getCnamountdue();
                        } else if(isSOA){ //export SOA report case
                            amountdue=- authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, creditNote.getCnamountdue(), cncurrencyid, creditNote.getCreationDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid);
                        } else {
                            amountdue = creditNote.getCnamountdue();
                        }
                        
                        if ((dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                            amountdue1 = authHandler.round(amountdue, companyid);
                        } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                            amountdue2 = authHandler.round(amountdue, companyid);
                        } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                            amountdue3 = authHandler.round(amountdue, companyid);
                        } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                            amountdue4 = authHandler.round(amountdue, companyid);
                        } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                            amountdue5 = authHandler.round(amountdue, companyid);
                        } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                            amountdue6 = authHandler.round(amountdue, companyid);
                        } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                            amountdue7 = authHandler.round(amountdue, companyid);
                        } else {
                            amountdue8 = authHandler.round(amountdue, companyid);
                        }

                        obj.put("amountdue1", amountdue1);
                        obj.put("amountdue2", amountdue2);
                        obj.put("amountdue3", amountdue3);
                        obj.put("amountdue4", amountdue4);
                        obj.put("amountdue5", amountdue5);
                        obj.put("amountdue6", amountdue6);
                        obj.put("amountdue7", amountdue7);
                        obj.put("amountdue8", amountdue8);
                        JArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getDebitNotesJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }   
        
    public JSONArray getDebitNotesMergedJson(HashMap<String, Object> requestParams, List list, JSONArray JArr) throws ServiceException {
        try {
            String companyid = (String) requestParams.get("companyid");
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            int duration = (requestParams.containsKey(InvoiceConstants.duration) && requestParams.containsKey(InvoiceConstants.duration)) ? Integer.parseInt(requestParams.get(InvoiceConstants.duration).toString()) : 30;
            boolean isSOA=(requestParams.containsKey("isSOA") && requestParams.get("isSOA")!=null)?Boolean.parseBoolean(requestParams.get("isSOA").toString()):false;
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            if (requestParams.get(InvoiceConstants.curdate) != null) {
                String curDateString = (String) requestParams.get(InvoiceConstants.curdate);
                Date curDate = df.parse(curDateString);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            }
            
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;

            String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
            cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);

            String cal2String = authHandler.getDateOnlyFormat().format(cal2.getTime());
            cal2Date = authHandler.getDateOnlyFormat().parse(cal2String);

            String cal3String = authHandler.getDateOnlyFormat().format(cal3.getTime());
            cal3Date = authHandler.getDateOnlyFormat().parse(cal3String);

            String cal4String = authHandler.getDateOnlyFormat().format(cal4.getTime());
            cal4Date = authHandler.getDateOnlyFormat().parse(cal4String);

            String cal5String = authHandler.getDateOnlyFormat().format(cal5.getTime());
            cal5Date = authHandler.getDateOnlyFormat().parse(cal5String);

            String cal6String = authHandler.getDateOnlyFormat().format(cal6.getTime());
            cal6Date = authHandler.getDateOnlyFormat().parse(cal6String);

            String cal7String = authHandler.getDateOnlyFormat().format(cal7.getTime());
            cal7Date = authHandler.getDateOnlyFormat().parse(cal7String);
            
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                
                double amountdue1 = 0;
                double amountdue2 = 0;
                double amountdue3 = 0;
                double amountdue4 = 0;
                double amountdue5 = 0;
                double amountdue6 = 0;
                double amountdue7 = 0;
                double amountdue8 = 0;

                Object[] row = (Object[]) itr.next();
                boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                String personid = "";
                String personname = "";
                String billto = "";
                String dncurrencyid="";

                KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) row[2]);
                Customer customer = (Customer) resultObject.getEntityList().get(0);
                personid = customer!=null?customer.getID():"";
                personname = customer!=null?customer.getName():"";
                billto=customer.getBillingAddress()==null?"":customer.getBillingAddress();

                resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);

                JSONObject obj = new JSONObject();
                if (!withoutinventory) {
                    resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), (String) row[1]);
                    DebitNote debitNote = (DebitNote) resultObject.getEntityList().get(0);
                    if (debitNote.isOtherwise() && debitNote.getDnamountdue()>0) {
                        JournalEntry je = debitNote.getJournalEntry();
                        dncurrencyid=debitNote.getCurrency() == null ? currency.getCurrencyID() : debitNote.getCurrency().getCurrencyID();
                        obj.put(InvoiceConstants.billid, debitNote.getID());
                        obj.put(InvoiceConstants.noteid, debitNote.getID());
                        obj.put(InvoiceConstants.noteno, debitNote.getDebitNoteNumber());
                        obj.put(InvoiceConstants.companyid, debitNote.getCompany().getCompanyID());
                        obj.put(InvoiceConstants.companyname, debitNote.getCompany().getCompanyName());
                        obj.put(InvoiceConstants.billno, debitNote.getDebitNoteNumber());
                        obj.put(InvoiceConstants.journalentryid, je.getID());
                        obj.put(InvoiceConstants.withoutinventory, withoutinventory);
                        obj.put(InvoiceConstants.currencysymbol, (debitNote.getCurrency() == null ? currency.getSymbol() : debitNote.getCurrency().getSymbol()));
                        obj.put(InvoiceConstants.currencyid, (debitNote.getCurrency() == null ? currency.getCurrencyID() : debitNote.getCurrency().getCurrencyID()));
                        obj.put(InvoiceConstants.currencyname, (debitNote.getCurrency() == null ? currency.getName() : debitNote.getCurrency().getName()));
                        obj.put(InvoiceConstants.entryno, je.getEntryNumber());
                        obj.put(InvoiceConstants.personid, personid);
                        obj.put(InvoiceConstants.personname, personname); 
                        obj.put(InvoiceConstants.personinfo, debitNote.getCustomer() == null ? "" : debitNote.getCustomer().getName()+"("+debitNote.getCustomer().getAcccode()+")");
                        obj.put(InvoiceConstants.billto, billto);
                        obj.put(InvoiceConstants.amount, debitNote.isOtherwise() ? debitNote.getDnamount() : details.getAmount());
                        obj.put(InvoiceConstants.amountdue, debitNote.isOtherwise() ? debitNote.getDnamountdue() : 0);
                        obj.put(InvoiceConstants.amountduenonnegative, debitNote.isOtherwise() ? debitNote.getDnamountdue() : 0);
//                        obj.put(InvoiceConstants.amountdueinbase, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, debitNote.getDnamountdue(), dncurrencyid, je.getEntryDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid));
                        obj.put(InvoiceConstants.amountdueinbase, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, debitNote.getDnamountdue(), dncurrencyid, debitNote.getCreationDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid));
//                        obj.put(InvoiceConstants.duedate, df.format(je.getEntryDate()));
//                        obj.put(InvoiceConstants.date, df.format(je.getEntryDate()));
                        obj.put(InvoiceConstants.duedate, df.format(debitNote.getCreationDate()));
                        obj.put(InvoiceConstants.date, df.format(debitNote.getCreationDate()));
                        obj.put(InvoiceConstants.memo, debitNote.getMemo());
                        obj.put(InvoiceConstants.deleted, debitNote.isDeleted());
                        obj.put(InvoiceConstants.externalcurrencyrate, debitNote.getExternalCurrencyRate());
                        obj.put("type","Debit Note");
                        obj.put("isDN",true);
                        obj.put("currencyidval",authHandlerDAOObj.getCurrency(currencyid));
//                        Date dueDate = df.parse(df.format(je.getEntryDate()));
                        Date dueDate = df.parse(df.format(debitNote.getCreationDate()));
                        Double amountdue = 0.0;
                        if(isSOA){ //export SOA report case
//                            amountdue= authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, debitNote.getDnamountdue(), dncurrencyid, je.getEntryDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid);
                            amountdue= authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, debitNote.getDnamountdue(), dncurrencyid, debitNote.getCreationDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid);
                        }else{
                             amountdue = debitNote.getDnamountdue();
                        }
                        if ((dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                            amountdue1 = authHandler.round(amountdue, companyid);
                        } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                            amountdue2 = authHandler.round(amountdue, companyid);
                        } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                            amountdue3 = authHandler.round(amountdue, companyid);
                        } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                            amountdue4 = authHandler.round(amountdue, companyid);
                        } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                            amountdue5 = authHandler.round(amountdue, companyid);
                        } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                            amountdue6 = authHandler.round(amountdue, companyid);
                        } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                            amountdue7 = authHandler.round(amountdue, companyid);
                        } else {
                            amountdue8 = authHandler.round(amountdue, companyid);
                        }

                        obj.put("amountdue1", amountdue1);
                        obj.put("amountdue2", amountdue2);
                        obj.put("amountdue3", amountdue3);
                        obj.put("amountdue4", amountdue4);
                        obj.put("amountdue5", amountdue5);
                        obj.put("amountdue6", amountdue6);
                        obj.put("amountdue7", amountdue7);
                        obj.put("amountdue8", amountdue8);
                        JArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getDebitNotesJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }   
        
    private JSONArray getReceiptsJson(HashMap requestParams, List entityList, JSONArray JArr) throws ServiceException {
       try {
            String companyid = (String) requestParams.get("companyid");
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList();
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            int duration = (requestParams.containsKey("duration") && requestParams.get("duration")!=null) ? Integer.parseInt(requestParams.get("duration").toString()) : 30;
            boolean agedReport=(requestParams.containsKey("agedReport") && requestParams.get("agedReport")!=null)?Boolean.parseBoolean(requestParams.get("agedReport").toString()):false;
            boolean isSOA=(requestParams.containsKey("isSOA") && requestParams.get("isSOA")!=null)?Boolean.parseBoolean(requestParams.get("isSOA").toString()):false;
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            if (requestParams.get(InvoiceConstants.curdate) != null) {
                String curDateString = (String) requestParams.get(InvoiceConstants.curdate);
                Date curDate = df.parse(curDateString);
                cal1.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            }
            
           Date cal1Date = null;
           Date cal2Date = null;
           Date cal3Date = null;
           Date cal4Date = null;
           Date cal5Date = null;
           Date cal6Date = null;
           Date cal7Date = null;

           String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
           cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);

           String cal2String = authHandler.getDateOnlyFormat().format(cal2.getTime());
           cal2Date = authHandler.getDateOnlyFormat().parse(cal2String);

           String cal3String = authHandler.getDateOnlyFormat().format(cal3.getTime());
           cal3Date = authHandler.getDateOnlyFormat().parse(cal3String);

           String cal4String = authHandler.getDateOnlyFormat().format(cal4.getTime());
           cal4Date = authHandler.getDateOnlyFormat().parse(cal4String);

           String cal5String = authHandler.getDateOnlyFormat().format(cal5.getTime());
           cal5Date = authHandler.getDateOnlyFormat().parse(cal5String);

           String cal6String = authHandler.getDateOnlyFormat().format(cal6.getTime());
           cal6Date = authHandler.getDateOnlyFormat().parse(cal6String);

           String cal7String = authHandler.getDateOnlyFormat().format(cal7.getTime());
           cal7Date = authHandler.getDateOnlyFormat().parse(cal7String);
            
            Iterator itr = entityList.iterator();
            while (itr.hasNext()) {
                
                double amountdue1 = 0;
                double amountdue2 = 0;
                double amountdue3 = 0;
                double amountdue4 = 0;
                double amountdue5 = 0;
                double amountdue6 = 0;
                double amountdue7 = 0;
                double amountdue8 = 0;

                String personid = "";
                String personname = "";
                String billto="";
                boolean advanceUsed = false;
                String rpcurrencyid="";
                
                Object[] row = (Object[]) itr.next();
                Receipt receipt = (Receipt) row[0];
                Account acc = (Account) row[1];
                
                rRequestParams.clear(); filter_names.clear(); filter_params.clear();
                filter_names.add("receipt.ID");
                filter_params.add(receipt.getID());
                rRequestParams.put("filter_names", filter_names);
                rRequestParams.put("filter_params", filter_params);
                KwlReturnObject grdresult = accReceiptDAOobj.getReceiptDetails(rRequestParams);
                advanceUsed = grdresult.getEntityList().size()>0?true:false;
                
                JSONObject obj = new JSONObject();
                       
                if (receipt.isIsadvancepayment() && receipt.getDepositAmount()>0 && !advanceUsed) {
                    JournalEntry je = receipt.getJournalEntry();
                    rpcurrencyid=receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID();
                    obj.put(InvoiceConstants.billid, receipt.getID());
                    obj.put(InvoiceConstants.companyid, receipt.getCompany().getCompanyID());
                    obj.put(InvoiceConstants.companyname, receipt.getCompany().getCompanyName());
                    obj.put(InvoiceConstants.billno, receipt.getReceiptNumber());
                    obj.put(InvoiceConstants.journalentryid, je.getID());
                    obj.put(InvoiceConstants.withoutinventory, false);
                    obj.put(InvoiceConstants.currencysymbol, (receipt.getCurrency() == null ? currency.getSymbol() : receipt.getCurrency().getSymbol()));
                    obj.put(InvoiceConstants.currencyid, (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID()));
                    obj.put(InvoiceConstants.currencyname, (receipt.getCurrency() == null ? currency.getName() : receipt.getCurrency().getName()));
                    obj.put(InvoiceConstants.entryno, je.getEntryNumber());
                    obj.put(InvoiceConstants.personid,receipt.getCustomer()==null?"" : receipt.getCustomer().getID());
                    obj.put(InvoiceConstants.personname, receipt.getCustomer()==null?"" : receipt.getCustomer().getName());
                    obj.put(InvoiceConstants.personinfo, receipt.getCustomer() == null ? "" : receipt.getCustomer().getName()+"("+receipt.getCustomer().getAcccode()+")");
                    obj.put(InvoiceConstants.aliasname, receipt.getCustomer()==null?"" : receipt.getCustomer().getAliasname());
                    obj.put(InvoiceConstants.billto, receipt.getCustomer()==null?"" : receipt.getCustomer().getBillingAddress()==null?"":receipt.getCustomer().getBillingAddress());
                    
                    if (agedReport || isSOA) {
                        obj.put(InvoiceConstants.amountdue, -receipt.getDepositAmount());
//                        obj.put(InvoiceConstants.amountdueinbase, -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, receipt.getDepositAmount(), rpcurrencyid, je.getEntryDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid));
                        obj.put(InvoiceConstants.amountdueinbase, -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, receipt.getDepositAmount(), rpcurrencyid, receipt.getCreationDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid));
                    } else {
                        obj.put(InvoiceConstants.amountdue, receipt.getDepositAmount());
//                        obj.put(InvoiceConstants.amountdueinbase, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, receipt.getDepositAmount(), rpcurrencyid, je.getEntryDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid));
                        obj.put(InvoiceConstants.amountdueinbase, authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, receipt.getDepositAmount(), rpcurrencyid, receipt.getCreationDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid));
                    }
//                    obj.put(InvoiceConstants.duedate, df.format(je.getEntryDate()));
//                    obj.put(InvoiceConstants.date, df.format(je.getEntryDate()));
                    obj.put(InvoiceConstants.duedate, df.format(receipt.getCreationDate()));
                    obj.put(InvoiceConstants.date, df.format(receipt.getCreationDate()));
                    obj.put(InvoiceConstants.memo, receipt.getMemo());
                    obj.put(InvoiceConstants.deleted, receipt.isDeleted());
                    obj.put(InvoiceConstants.externalcurrencyrate, receipt.getExternalCurrencyRate());
                    obj.put("type","Payment Received");
                    obj.put("isRP", true);
                    obj.put("currencyidval",authHandlerDAOObj.getCurrency(currencyid));
                    
//                    Date dueDate = df.parse(df.format(je.getEntryDate()));
                    Date dueDate = df.parse(df.format(receipt.getCreationDate()));
                    Double amountdue = 0.0;
                    if (agedReport) { //Aged report viev case
                        amountdue = -receipt.getDepositAmount();
                    } else if(isSOA){ //export SOA report case
//                        amountdue= -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, receipt.getDepositAmount(), rpcurrencyid, je.getEntryDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid);
                        amountdue= -authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, receipt.getDepositAmount(), rpcurrencyid, receipt.getCreationDate(), je.getExternalCurrencyRate()).getEntityList().get(0), companyid);
                    } else {
                        amountdue = receipt.getDepositAmount();
                    }

                    if ((dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                        amountdue1 = authHandler.round(amountdue, companyid);
                    } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                        amountdue2 = authHandler.round(amountdue, companyid);
                    } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                        amountdue3 = authHandler.round(amountdue, companyid);
                    } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                        amountdue4 = authHandler.round(amountdue, companyid);
                    } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                        amountdue5 = authHandler.round(amountdue, companyid);
                    } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                        amountdue6 = authHandler.round(amountdue, companyid);
                    } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                        amountdue7 = authHandler.round(amountdue, companyid);
                    } else {
                        amountdue8 = authHandler.round(amountdue, companyid);
                    }

                    obj.put("amountdue1", amountdue1);
                    obj.put("amountdue2", amountdue2);
                    obj.put("amountdue3", amountdue3);
                    obj.put("amountdue4", amountdue4);
                    obj.put("amountdue5", amountdue5);
                    obj.put("amountdue6", amountdue6);
                    obj.put("amountdue7", amountdue7);
                    obj.put("amountdue8", amountdue8);
                    JArr.put(obj);
                }

            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceControllerCMN.getReceiptsJson: " + ex.getMessage(), ex);
        }
        return JArr;
    }  
    
    public double getInvDisountOnAmt(String id, double withoutTAmt, boolean isWithoutInventory) throws ServiceException {
        Discount discountObj = null;
        if (isWithoutInventory) {
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), id);
            BillingInvoice billingInvoice = (BillingInvoice) objItr.getEntityList().get(0);
            if (billingInvoice.getDiscount() == null) {
                return 0;
            }
            discountObj = billingInvoice.getDiscount();
        } else {
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), id);
            Invoice invoice = (Invoice) objItr.getEntityList().get(0);
            if (invoice.getDiscount() == null) {
                return 0;
            }
            discountObj = invoice.getDiscount();
        }
        double disc = 0;
        if (discountObj != null) {
            if (discountObj.isInPercent() && discountObj.getDiscount() > 0 && discountObj.getDiscount() <= 100.0) {
                disc = withoutTAmt * discountObj.getDiscount() / 100;
            } else {
                disc = discountObj.getDiscount();
            }
        } else {
            return 0;
        }
        return disc;
    }
        public JSONArray getTermDetails(String invoiceid) {
       JSONArray jArr=new JSONArray();
        try {
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("invoiceid", invoiceid);
            KwlReturnObject curresult = accInvoiceDAOobj.getInvoiceTermMap(requestParam);
            List<InvoiceTermsMap> termMap = curresult.getEntityList();
            for(InvoiceTermsMap invoiceTerMap : termMap) {
                InvoiceTermsSales mt = invoiceTerMap.getTerm();
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("id", mt.getId());
                jsonobj.put("term", mt.getTerm());
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("sign", mt.getSign());
                jsonobj.put("formula", mt.getFormula());
                jsonobj.put("termpercentage", invoiceTerMap.getPercentage());
                jsonobj.put("termamount", invoiceTerMap.getTermamount());
                jArr.put(jsonobj);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
        
 //getDeliveryOrderMerged    
public JSONObject getDeliveryOrdersMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getDeliveryOrdersMap(request);
            boolean pendingapproval = (request.getParameter("pendingapproval") != null)? Boolean.parseBoolean(request.getParameter("pendingapproval")): false;
            boolean consolidateFlag = request.getParameter("consolidateFlag")!=null?Boolean.parseBoolean(request.getParameter("consolidateFlag")):false;            
            String companyid = (consolidateFlag && request.getParameter("companyids")!=null)?request.getParameter("companyids"):sessionHandlerImpl.getCompanyid(request);
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid")!=null)?request.getParameter("gcurrencyid"):sessionHandlerImpl.getCurrencyID(request);                        
            request.setAttribute("companyid", companyid);
            request.setAttribute("gcurrencyid", gcurrencyid);
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", gcurrencyid);
            if(!StringUtil.isNullOrEmpty(request.getParameter("searchJson")))
            {
                requestParams.put("searchJson", request.getParameter("searchJson"));
                requestParams.put("moduleid", request.getParameter("moduleid"));
                requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
            }
            String dir = "";
            String sort = "";
             if(!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))){
                dir = request.getParameter("dir");
                 sort = request.getParameter("sort");
                   requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }   
            boolean isUnInvoiced = request.getParameter("isUnInvoiced")!=null?Boolean.parseBoolean(request.getParameter("isUnInvoiced")):false;
//          System.out.println("getDeliveryOrdersMerged -- isUnInvoiced:"+isUnInvoiced);                         
            KwlReturnObject result = null;          
            if (!isUnInvoiced) {
                if(pendingapproval) {
                    requestParams.put("userid", sessionHandlerImpl.getUserid(request));
                    result = accInvoiceDAOobj.getPendingDO(requestParams);
                } else {
                    result = accInvoiceDAOobj.getDeliveryOrdersMerged(requestParams);
                }
            } else {
            	result = accInvoiceDAOobj.getUnInvoicedDeliveryOrders(requestParams);
            }       
            
            JSONArray jarr = getDeliveryOrdersJsonMerged(request, result.getEntityList());            
            jobj.put("data", jarr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }       
     
public JSONArray getDeliveryOrdersJsonMerged(HttpServletRequest request, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            double quantity = 0;
            double amount = 0;
            boolean closeflag = request.getParameter("closeflag")!=null?true:false;
            boolean srflag = request.getParameter("srflag")!=null?true:false;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Delivery_Order_ModuleId));
            HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
           // HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
            
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                //SalesOrder salesOrder=(SalesOrder)itr.next();
                Object[] oj = (Object[])itr.next();                
                String orderid = oj[0].toString();
                //Withoutinventory 0 for normal, 1 for billing
                boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());

                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), orderid);
                    DeliveryOrder deliveryOrder = (DeliveryOrder) objItr.getEntityList().get(0);

                    Customer customer=deliveryOrder.getCustomer();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", deliveryOrder.getID());
                    obj.put("companyid", deliveryOrder.getCompany().getCompanyID());
                    obj.put("companyname", deliveryOrder.getCompany().getCompanyName());
                    obj.put("withoutinventory", withoutinventory);
                    obj.put("personid", customer.getID());
                    obj.put("billno", deliveryOrder.getDeliveryOrderNumber());
                    //obj.put("duedate", authHandler.getDateFormatter(request).format(salesOrder.getDueDate()));
                    obj.put("date", authHandler.getDateOnlyFormat(request).format(deliveryOrder.getOrderDate()));

                    
                    obj.put("personname", customer.getName());
                    obj.put("personemail", customer.getEmail());
                    obj.put("memo", deliveryOrder.getMemo());
                    obj.put("posttext", deliveryOrder.getPostText()==null?"":deliveryOrder.getPostText());
                    obj.put("costcenterid", deliveryOrder.getCostcenter()==null?"":deliveryOrder.getCostcenter().getID());
                    obj.put("costcenterName", deliveryOrder.getCostcenter()==null?"":deliveryOrder.getCostcenter().getName());
                    obj.put("statusID", deliveryOrder.getStatus()==null?"":deliveryOrder.getStatus().getID());
                    obj.put("status", deliveryOrder.getStatus()==null?"":deliveryOrder.getStatus().getValue());
                    obj.put("shipdate", deliveryOrder.getShipdate()==null? "" : authHandler.getDateOnlyFormat(request).format(deliveryOrder.getShipdate()));
                    obj.put("shipvia", deliveryOrder.getShipvia()==null?"":deliveryOrder.getShipvia());
                    obj.put("fob", deliveryOrder.getFob()==null?"":deliveryOrder.getFob());
                    obj.put("isfavourite", deliveryOrder.isFavourite());
                    obj.put("isprinted", deliveryOrder.isPrinted());
                    obj.put("deleted", deliveryOrder.isDeleted());
                    obj.put("currencyid", (deliveryOrder.getCurrency() == null ? "" : deliveryOrder.getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (deliveryOrder.getCurrency() == null ? "" : deliveryOrder.getCurrency().getSymbol()));
                    obj.put(Constants.SEQUENCEFORMATID,deliveryOrder.getSeqformat()!=null?deliveryOrder.getSeqformat().getID():"");
                    if(deliveryOrder.getModifiedby()!=null){
                        obj.put("lasteditedby",deliveryOrder.getModifiedby().getFirstName()+" "+deliveryOrder.getModifiedby().getLastName());
                    }
                    Set<DeliveryOrderDetail> doRows = deliveryOrder.getRows();
                    amount = 0;
                    if (doRows != null && !doRows.isEmpty()){
                        for ( DeliveryOrderDetail temp: doRows){
                            quantity = temp.getInventory().getQuantity();
                            amount += temp.getRate() * quantity;
                        }
                    }
                    obj.put("amount", authHandler.round(amount,companyid));
                    if(deliveryOrder.getCurrency()!=null){
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, deliveryOrder.getCurrency().getCurrencyID(), deliveryOrder.getOrderDate(), 0);
                        obj.put("amountinbase", authHandler.round((Double) bAmt.getEntityList().get(0),companyid));
                    }
                    
                    String status = "Open";
                    if(srflag) {
                        status = getDeliveryReturnStatus(deliveryOrder);
                    } else {
                        status = getDeliverySalesOrderStatus(deliveryOrder);
                    }
                    
                    boolean addflag = true;
                    if (closeflag && deliveryOrder.isDeleted()) {
                        addflag = false;
                    } else if (closeflag && (status.equalsIgnoreCase("Closed"))) {
                        addflag = false;
                    }
                    obj.put("approvalstatusinfo",deliveryOrder.getApprovestatuslevel()==-1 ? "Rejected" : deliveryOrder.getApprovestatuslevel()<11 ? "Waiting for Approval at Level - "+deliveryOrder.getApprovestatuslevel(): "Approved");
                    obj.put("approvalstatus",deliveryOrder.getApprovestatuslevel());
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    DeliveryOrderCustomData deliveryOrderCustomData = (DeliveryOrderCustomData)deliveryOrder.getDeliveryOrderCustomData();
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    AccountingManager.setCustomColumnValues(deliveryOrderCustomData, fieldMap, replaceFieldMap,variableMap);
                    DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateFromDB=null;
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        String coldata = varEntry.getValue().toString();
                        if(customFieldMap.containsKey(varEntry.getKey())){
                                   FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), coldata);
                                   if(fieldComboData != null){
                                       obj.put(varEntry.getKey(),coldata!=null?coldata:"");// fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                                   
                                   }
                              }else if(customDateFieldMap.containsKey(varEntry.getKey())){
                                  DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                                  try {
                                        dateFromDB = defaultDateFormat.parse(coldata);
                                        coldata = df2.format(dateFromDB);

                                       } catch (Exception e) {
                                       }
                                    obj.put(varEntry.getKey(),coldata);
                              } else{
                                       if (!StringUtil.isNullOrEmpty(coldata)) {
                                        obj.put(varEntry.getKey(), coldata);
                                        }
                               }
                    }
                    if(addflag){
                        jArr.put(obj);
                    }
            
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getDeliveryOrdersJsonMerged : "+ex.getMessage(), ex);
            }
        return jArr;
    }

    public HashMap<String, Object> getDeliveryOrdersMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        requestParams.put(Constants.start, request.getParameter(Constants.start));
        requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        requestParams.put(CCConstants.REQ_costCenterId, request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(CCConstants.REQ_customerId, request.getParameter(CCConstants.REQ_customerId));
        requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
        requestParams.put(Constants.MARKED_FAVOURITE, request.getParameter(Constants.MARKED_FAVOURITE));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("currencyfilterfortrans", (request.getParameter("currencyfilterfortrans") == null) ? "" : request.getParameter("currencyfilterfortrans"));
        return requestParams;
    }

    public String getDeliveryReturnStatus(DeliveryOrder so) throws ServiceException {
        Set<DeliveryOrderDetail> orderDetail = so.getRows();
        Iterator ite = orderDetail.iterator();

        String result = "Closed";
        while (ite.hasNext()) {
            DeliveryOrderDetail soDetail = (DeliveryOrderDetail) ite.next();
            KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSRD(soDetail.getID());
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            double qua = 0;
            while (ite1.hasNext()) {
                SalesReturnDetail ge = (SalesReturnDetail) ite1.next();
                qua += ge.getInventory().getQuantity();
            }
            if (qua < soDetail.getActualQuantity()) {
                result = "Open";
                break;
            }
        }
        return result;
    }

    public String getDeliverySalesOrderStatus(DeliveryOrder so) throws ServiceException {
        Set<DeliveryOrderDetail> orderDetail = so.getRows();
        Iterator ite = orderDetail.iterator();

        String result = "Closed";
        while (ite.hasNext()) {
            DeliveryOrderDetail soDetail = (DeliveryOrderDetail) ite.next();
            KwlReturnObject idresult = accInvoiceDAOobj.getIDFromDOD(soDetail.getID());
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            double qua = 0;
            while (ite1.hasNext()) {
                InvoiceDetail ge = (InvoiceDetail) ite1.next();
//                qua += ge.getInventory().isInvrecord()? ge.getInventory().getQuantity() : ge.getInventory().getActquantity() ;
                qua += ge.getInventory().getQuantity();
            }
            if (qua < soDetail.getActualQuantity()) {
                result = "Open";
                break;
            }
        }
        return result;
    }

  public JSONObject getDeliveryOrderRows(HttpServletRequest request) throws SessionExpiredException, ServiceException {  //, HashMap<String, Integer> fieldMap
   JSONObject jobj=new JSONObject();
        try {
            HashMap<String,Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
           String companyid=sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            boolean closeflag = request.getParameter("closeflag")!=null?true:false;
            boolean srflag = request.getParameter("srflag")!=null?true:false;
            boolean linkingFlag = (StringUtil.isNullOrEmpty(request.getParameter("linkingFlag")))?false:Boolean.parseBoolean(request.getParameter("linkingFlag"));
            boolean isFixedAssetDO=false;  
            boolean isBatchSerial=false;  
            boolean isBatch=false;
            boolean isSerial=false;
            boolean isBatchForProduct=false;
            boolean isSerialForProduct=false;
            String description="";
            String[] sos=(String[])request.getParameter("bills").split(",");
            int i=0;
            JSONArray jArr=new JSONArray();
            int addobj = 1;
            DateFormat userDateFormat=new SimpleDateFormat(sessionHandlerImpl.getUserDateFormat(request));

            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Delivery_Order_ModuleId,1));
            HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
           // HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
            HashMap<String, Object> doRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("deliveryOrder.ID");
            order_by.add("srno");
            order_type.add("asc");
            doRequestParams.put("filter_names", filter_names);
            doRequestParams.put("filter_params", filter_params);
            doRequestParams.put("order_by", order_by);
            doRequestParams.put("order_type", order_type);
        
            
            while(sos!=null&&i<sos.length){
//                SalesOrder so=(SalesOrder)session.get(SalesOrder.class, sos[i]);
                KwlReturnObject result = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), sos[i]);
                DeliveryOrder dorder = (DeliveryOrder) result.getEntityList().get(0);
                filter_params.clear();
                filter_params.add(dorder.getID());
                KwlReturnObject podresult = accInvoiceDAOobj.getDeliveryOrderDetails(doRequestParams);
                Iterator itr = podresult.getEntityList().iterator();
                
                while(itr.hasNext()) {
                    DeliveryOrderDetail row=(DeliveryOrderDetail)itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", dorder.getID());
                    obj.put("billno", dorder.getDeliveryOrderNumber());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", row.getProduct().getID());
                    obj.put("productname",row.getProduct().getName());
                    obj.put("invstore",(StringUtil.isNullOrEmpty(row.getInvstoreid()))?"":row.getInvstoreid());                    
                    obj.put("invlocation",(StringUtil.isNullOrEmpty(row.getInvlocid()))?"":row.getInvlocid());
                    obj.put("isAsset", row.getProduct().isAsset());
                    //int warranty = -1;
//                    int warranty = row.getProduct().getWarrantyperiod();
//                    if(warranty != -1){
//                      Calendar cal = Calendar.getInstance();
//                      Calendar cal1 = Calendar.getInstance();
//            
//                      cal.setTime(row.getDeliveryOrder().getOrderDate());
//                      cal.add(Calendar.DAY_OF_MONTH, warranty);
//
//                      if(cal.after(cal1) || cal.equals(cal1)){
//                          obj.put("warrantystat", 0);//Under Warranty
//                      } else {
//                          obj.put("warrantystat", 1);//Over warranty
//                      }
//                    }
//                    obj.put("unitname", row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getName());
                    obj.put("unitname", row.getInventory().getUom()!=null?row.getInventory().getUom().getNameEmptyforNA():row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                        description = row.getDescription();
                    } else if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                        description = row.getProduct().getDescription();
                    } else {
                        description = "";
                    }
                    obj.put("desc", description);
                    obj.put("type",row.getProduct().getProducttype()==null?"":row.getProduct().getProducttype().getName());
                    obj.put("pid",row.getProduct().getProductid());
                    obj.put("memo", row.getRemark());
                    obj.put("quantity", row.getActualQuantity());    
                     KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                    CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                    
                    if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
                        KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), row.getProduct().getID());
                        Product product = (Product) prodresult.getEntityList().get(0);
                        isBatchForProduct = product.isIsBatchForProduct();
                        isSerialForProduct = product.isIsSerialForProduct();
                    }
                    if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory()) {  //check if company level option is on then only we will check productt level
                    if (isBatchForProduct || isSerialForProduct) {
                        obj.put("batchdetails", (row.getBatch() == null) ? "" :  getBatchJson(row.getBatch(), isFixedAssetDO, request, preferences.isIsBatchCompulsory(), isBatchForProduct, preferences.isIsSerialCompulsory(), isSerialForProduct));
                    }
                    }
                    obj.put("dquantity", row.getDeliveredQuantity());
                    double baseuomrate = row.getInventory().getBaseuomrate();
                    if(row.getInventory().getUom()!=null) {
                        obj.put("uomid", row.getInventory().getUom().getID());                        
                    } else {
                        obj.put("uomid", row.getInventory().getProduct().getUnitOfMeasure()!=null?row.getInventory().getProduct().getUnitOfMeasure().getID():"");                        
                    }
                    obj.put("baseuomquantity", row.getActualQuantity()*baseuomrate);
                    obj.put("baseuomrate", baseuomrate);
                    
                    obj.put("copyquantity", row.getDeliveredQuantity());
                    obj.put("description", description);
                    obj.put("partno", (row.getPartno()!=null) ? row.getPartno() : "" );
                    obj.put("remark", row.getRemark());
                    obj.put("discountispercent", row.getDiscountispercent());
                    obj.put("prdiscount", row.getDiscount());
                    //Set rate to default product price when invoice is created using DO.
//                    KwlReturnObject rateResult=accProductObj.getProductPrice(row.getInventory().getProduct().getID(), false, row.getDeliveryOrder().getOrderDate(),"");
//                    Object temp = rateResult.getEntityList().get(0);
//                    if (temp != null) {
//                        obj.put("rate", temp.toString());
//                    } else {
//                        obj.put("rate", 0);
//                    }
                    obj.put("rate", row.getRate());
                    HashMap<String, Object> fieldrequestParamsProduct = new HashMap();
                    Map<String, Object> variableMapProduct = new HashMap<String, Object>();
                    fieldrequestParamsProduct.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParamsProduct.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Product_Master_ModuleId, 0));
                    HashMap<String, String> replaceFieldMapProduct = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMapProduct = accAccountDAOobj.getFieldParamsMap(fieldrequestParamsProduct, replaceFieldMapProduct);
                    KwlReturnObject resultProduct = accountingHandlerDAOobj.getObject(AccProductCustomData.class.getName(), row.getProduct().getID());
                    AccProductCustomData objProduct = (AccProductCustomData) resultProduct.getEntityList().get(0);
                    if (objProduct != null) {
                        productHandler.setCustomColumnValuesForProduct(objProduct, FieldMapProduct, replaceFieldMapProduct, variableMapProduct);
                        for (Map.Entry<String, Object> varEntry : variableMapProduct.entrySet()) {
                            String coldata = varEntry.getValue().toString();
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                        obj.put(varEntry.getKey(), coldata);
                                        obj.put("key", varEntry.getKey());
//                                jobj.append("data", jsonObj);
                                        }
                                   }
                            }

                     if(!linkingFlag){
                        if (row.getCidetails() != null) {
                            obj.put("linkto", row.getCidetails().getInvoice().getInvoiceNumber());
                            obj.put("linkid", row.getCidetails().getInvoice().getID());
                            obj.put("rowid", row.getCidetails().getID());                            
                            obj.put("savedrowid", row.getCidetails().getID());                            
                            obj.put("linktype", 1);
                        } else if (row.getSodetails() != null) {
                            obj.put("linkto", row.getSodetails().getSalesOrder().getSalesOrderNumber());                            
                            obj.put("linkid", row.getSodetails().getSalesOrder().getID());
                            obj.put("rowid", row.getSodetails().getID());
                            obj.put("savedrowid", row.getSodetails().getID());
                            obj.put("linktype", 0);
                        } else {
                            obj.put("linkto", "");
                            obj.put("linkid", "");
                            obj.put("linktype", -1);
                        }
                    }


                    if (dorder.isFixedAssetDO() || dorder.isLeaseDO()) {
                        isBatchSerial = preferences.isShowprodserial();  //to get the optioof serial no and batch is on or not
                         getASsetDetailsJson(row, companyid, obj, df,preferences, request);
                    }
                     
                     Map<String, Object> variableMap = new HashMap<String, Object>();
                    DeliveryOrderDetailCustomData deliveryOrderDetailCustomData = (DeliveryOrderDetailCustomData) row.getDeliveryOrderDetailCustomData();
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    AccountingManager.setCustomColumnValues(deliveryOrderDetailCustomData, fieldMap, replaceFieldMap,variableMap);
                    DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateFromDB=null;
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        String coldata = varEntry.getValue().toString();
                        if(customFieldMap.containsKey(varEntry.getKey())){
                                   FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), coldata);
                                   if(fieldComboData != null){
                                       obj.put(varEntry.getKey(),coldata!=null?coldata:"");// fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                                   
                                   }
                              }else if(customDateFieldMap.containsKey(varEntry.getKey())){
                                  DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                                  try {
                                    dateFromDB = defaultDateFormat.parse(coldata);
                                    coldata = df2.format(dateFromDB);

                                  } catch (Exception e) {
                                  }                                 
                                  obj.put(varEntry.getKey(),coldata);
                              } else{
                                       if (!StringUtil.isNullOrEmpty(coldata)) {
                                        obj.put(varEntry.getKey(), coldata);
                                        }
                               }
                    }
                    
                     
                    if (closeflag) {
                        double quantity = 0;
                        if(srflag) {
                            quantity = getDeliveryOrderDetailStatusFORSR(row);
                        } else {
                            quantity = getDeliveryOrderDetailStatus(row);
                        }
                        
                        obj.put("quantity", quantity);
                        obj.put("dquantity", quantity);
                        obj.put("baseuomquantity", quantity*baseuomrate);
                        obj.put("baseuomrate", baseuomrate);
                    } else {
                        obj.put("quantity", row.getActualQuantity());
                        obj.put("baseuomquantity", row.getActualQuantity()*baseuomrate);
                        obj.put("baseuomrate", baseuomrate);
                    }
//                    obj.put("rate", row.getProduct().);
                    jArr.put(obj);
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }

    public double getDeliveryOrderDetailStatusFORSR(DeliveryOrderDetail sod) throws ServiceException {
        double result = sod.getDeliveredQuantity();

        KwlReturnObject idresult = accInvoiceDAOobj.getIDFromDODFORSR(sod.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0;
        while (ite1.hasNext()) {
            SalesReturnDetail ge = (SalesReturnDetail) ite1.next();
            qua += ge.getInventory().getQuantity();
        }
        result = result - qua;
        return result;
    }

    public double getDeliveryOrderDetailStatus(DeliveryOrderDetail sod) throws ServiceException {
        double result = sod.getDeliveredQuantity();

        KwlReturnObject idresult = accInvoiceDAOobj.getIDFromDOD(sod.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0;
        while (ite1.hasNext()) {
            InvoiceDetail ge = (InvoiceDetail) ite1.next();
//            qua += ge.getInventory().isInvrecord() ? ge.getInventory().getQuantity() : ge.getInventory().getActquantity() ;
            qua += ge.getInventory().getQuantity();
        }
        result = result - qua;
        return result;
    }

   public String getBatchJson(ProductBatch productBatch, boolean isFixedAssetDO, HttpServletRequest request,boolean isbatch,boolean isBatchForProduct,boolean isserial,boolean isSerialForProduct) throws ServiceException, SessionExpiredException, JSONException {
        JSONArray jSONArray = new JSONArray();
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String purchasebatchid="";
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
        filter_names.add("batch.id");
        filter_params.add(productBatch.getId());
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        KwlReturnObject kmsg = accCommonTablesDAO.getSerialForBatch(filterRequestParams);

        List list = kmsg.getEntityList();
        Iterator iter = list.iterator();
        int i = 1;
        while (iter.hasNext()) {
            BatchSerial batchSerial = (BatchSerial) iter.next();
            JSONObject obj = new JSONObject();
            if (i == 1) {
                obj.put("id", productBatch.getId());
                obj.put("batch", productBatch.getName());
                obj.put("batch", productBatch.getName());
                obj.put("batchname", productBatch.getName());
                obj.put("location", productBatch.getLocation().getId());
                obj.put("warehouse", productBatch.getWarehouse().getId());
                obj.put("mfgdate", productBatch.getMfgdate()!=null?authHandler.getDateOnlyFormat(request).format(productBatch.getMfgdate()):"");
                obj.put("expdate",productBatch.getExpdate()!=null?authHandler.getDateOnlyFormat(request).format(productBatch.getExpdate()):"");
                obj.put("quantity", productBatch.getQuantity());
                obj.put("balance", productBatch.getBalance());
                obj.put("balance", productBatch.getBalance());
                obj.put("asset", productBatch.getAsset());
                if (isFixedAssetDO) {
                    obj.put("purchasebatchid", productBatch.getId());
                } else {
                    obj.put("purchasebatchid", getPurchaseBatchid(productBatch.getId()));
                }
            } else {
                obj.put("id", "");
                obj.put("batch", "");
                obj.put("batchname", "");
                obj.put("location", "");
                obj.put("warehouse", "");
                obj.put("mfgdate", "");
                obj.put("expdate", "");
                obj.put("quantity", "");
                obj.put("balance", "");
                obj.put("purchasebatchid", "");
            }
            i++;
            obj.put("serialnoid", batchSerial.getId());
            obj.put("serialno", batchSerial.getName());
           obj.put("expstart",batchSerial.getExpfromdate()!=null?authHandler.getDateOnlyFormat(request).format(batchSerial.getExpfromdate()):"");
           obj.put("expend", batchSerial.getExptodate()!=null?authHandler.getDateOnlyFormat(request).format(batchSerial.getExptodate()):"");
            if (isFixedAssetDO) {
                obj.put("purchaseserialid", batchSerial.getId());
            } else {
                obj.put("purchaseserialid", getPurchaseSerialid(batchSerial.getId()));
            }
            jSONArray.put(obj);

        }
     if (isBatchForProduct && !isSerialForProduct) //only in batch case
          {
              JSONObject Jobj = new JSONObject();
              Jobj = getOnlyBatchDetail(productBatch, request);
             if (isFixedAssetDO) {
                    purchasebatchid=productBatch.getId();
                } else {
                    purchasebatchid=getPurchaseBatchid(productBatch.getId());
                }
              if (!StringUtil.isNullOrEmpty(purchasebatchid)) {
                  Jobj.put("purchasebatchid", purchasebatchid);
              }
              jSONArray.put(Jobj);
          }

          return jSONArray.toString();
    }
         public JSONObject getOnlyBatchDetail(ProductBatch productBatch, HttpServletRequest request) throws JSONException, SessionExpiredException {

        JSONObject obj = new JSONObject();
        obj.put("id", productBatch.getId());
        obj.put("batch", productBatch.getName());
        obj.put("batchname", productBatch.getName());
        obj.put("location", productBatch.getLocation().getId());
        obj.put("warehouse", productBatch.getWarehouse().getId());
        obj.put("mfgdate", productBatch.getMfgdate() != null ? authHandler.getDateFormatter(request).format(productBatch.getMfgdate()) : "");
        obj.put("expdate", productBatch.getExpdate() != null ? authHandler.getDateFormatter(request).format(productBatch.getExpdate()) : "");
        obj.put("quantity", productBatch.getQuantity());
        obj.put("balance", productBatch.getBalance());
        obj.put("asset", productBatch.getAsset());
        obj.put("expstart", "");
        obj.put("expend","");
        return obj;
    }
    public String getPurchaseBatchid(String salesid) throws ServiceException {
        String purchaseMapId = "";
        SalesPurchaseBatchMapping productBatch = null;
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        filter_names.add("salesBatch.id");
        filter_params.add(salesid);
        order_by.add("id");
        order_type.add("asc");
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        filterRequestParams.put("order_by", order_by);
        filterRequestParams.put("order_type", order_type);
        KwlReturnObject result = accCommonTablesDAO.getBatchMappingDetails(filterRequestParams);

        List list = result.getEntityList();
        Iterator iter = list.iterator();
        int i = 1;
        while (iter.hasNext()) {
            productBatch = (SalesPurchaseBatchMapping) iter.next();
        }
        if (productBatch != null) {
            purchaseMapId = productBatch.getPurchaseBatch().getId();
        }
        return purchaseMapId;

    }

    public String getPurchaseSerialid(String salesid) throws ServiceException {
        String purchaseMapId = "";
        BatchSerialMapping batchSerialMapping = null;
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        filter_names.add("salesSerial.id");
        filter_params.add(salesid);
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        KwlReturnObject result = accCommonTablesDAO.getSerialMappingDetails(filterRequestParams);

        List list = result.getEntityList();
        Iterator iter = list.iterator();
        int i = 1;
        while (iter.hasNext()) {
            batchSerialMapping = (BatchSerialMapping) iter.next();
        }
        if (batchSerialMapping != null) {
            purchaseMapId = batchSerialMapping.getPurchaseSerial().getId();
        }
        return purchaseMapId;

    }
 
    
    
    public void getASsetDetailsJson(DeliveryOrderDetail row, String companyid, JSONObject obj, DateFormat df,CompanyAccountPreferences preferences,HttpServletRequest request) throws JSONException, ServiceException, SessionExpiredException {
        boolean isFixedAssetDO=true;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        JSONArray assetDetailsJArr = new JSONArray();
        HashMap<String, Object> assetDetailsParams = new HashMap<String, Object>();
        assetDetailsParams.put("companyid", companyid);
        assetDetailsParams.put("invoiceDetailId", row.getID());
        assetDetailsParams.put("moduleId", Constants.Acc_Delivery_Order_ModuleId);

        KwlReturnObject assetInvMapObj = accProductObj.getAssetInvoiceDetailMapping(assetDetailsParams);
        List assetInvMapList = assetInvMapObj.getEntityList();
        Iterator assetInvMapListIt = assetInvMapList.iterator();
        while (assetInvMapListIt.hasNext()) {
            AssetInvoiceDetailMapping invoiceDetailMapping = (AssetInvoiceDetailMapping) assetInvMapListIt.next();
            AssetDetails assetDetails = invoiceDetailMapping.getAssetDetails();
            JSONObject assetDetailsJOBJ = new JSONObject();

            assetDetailsJOBJ.put("assetdetailId", assetDetails.getId());
            assetDetailsJOBJ.put("assetId", assetDetails.getId());
            assetDetailsJOBJ.put("location", (assetDetails.getLocation() != null) ? assetDetails.getLocation().getId() : "");
            assetDetailsJOBJ.put("department", (assetDetails.getDepartment() != null) ? assetDetails.getDepartment().getId() : "");
            assetDetailsJOBJ.put("assetdescription", (assetDetails.getAssetDescription() != null) ? assetDetails.getAssetDescription() : "");
            assetDetailsJOBJ.put("assetUser", (assetDetails.getAssetUser() != null) ? assetDetails.getAssetUser().getUserID() : "");
            assetDetailsJOBJ.put("cost", assetDetails.getCost());
            assetDetailsJOBJ.put("salvageRate", assetDetails.getSalvageRate());
            assetDetailsJOBJ.put("salvageValue", assetDetails.getSalvageValue());
            assetDetailsJOBJ.put("accumulatedDepreciation", assetDetails.getAccumulatedDepreciation());
//            assetDetailsJOBJ.put("wdv", assetDetails.getWdv());
            assetDetailsJOBJ.put("assetLife", assetDetails.getAssetLife());
            assetDetailsJOBJ.put("elapsedLife", assetDetails.getElapsedLife());
            assetDetailsJOBJ.put("nominalValue", assetDetails.getElapsedLife());
            assetDetailsJOBJ.put("installationDate", df.format(assetDetails.getInstallationDate()));
            assetDetailsJOBJ.put("purchaseDate", df.format(assetDetails.getPurchaseDate()));
             if (!StringUtil.isNullOrEmpty(assetDetails.getProduct().getID())) {
                KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), assetDetails.getProduct().getID());
                Product product = (Product) prodresult.getEntityList().get(0);
                isBatchForProduct = product.isIsBatchForProduct();
                isSerialForProduct = product.isIsSerialForProduct();
            }
            if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory()) {  //check if company level option is on then only we will check productt level
                if (isBatchForProduct || isSerialForProduct) {
                    assetDetailsJOBJ.put("batchdetails", (assetDetails.getBatch() == null) ? "" : getBatchJson(assetDetails.getBatch(), isFixedAssetDO, request, preferences.isIsBatchCompulsory(), isBatchForProduct, preferences.isIsSerialCompulsory(), isSerialForProduct));
                }
            }
            assetDetailsJArr.put(assetDetailsJOBJ);
        }
        obj.put("assetDetails", assetDetailsJArr.toString());
    }
  
  //export single invoice for Android API--Neeraj D 
    public JSONObject exportSingleInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView-empty";
        try {
            HashMap<String, Object>otherconfigrequestParams = new HashMap();
            int moduleid=2;
            String invoiceID = request.getParameter("bills");
            String companyid = AccountingManager.getCompanyidFromRequest(request);
            String recordids = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("recordids"))) {
                recordids = request.getParameter("recordids");
            }
            ArrayList<String> invoiceIDList = CustomDesignHandler.getSelectedBillIDs(recordids);
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceID);
            Invoice invoice = (Invoice) objItr.getEntityList().get(0);
            AccCustomData accCustomData = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Invoice_ModuleId, 1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
            replaceFieldMap = new HashMap<String, String>();
            
            /*
             * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
             */
            fieldrequestParams.clear();
            HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
            HashMap<String, Integer> DimensionFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, dimensionFieldMap);
            
            fieldrequestParams.clear();
            HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
            HashMap<String, Integer> LineLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

            //For product custom field
            fieldrequestParams.clear();
            HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
            HashMap<String, Integer> ProductLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);
            
            HashMap<String, JSONArray> itemDataAgainstInvoice = new HashMap<String, JSONArray>();
            
            HashMap<String, Object> paramMap = new HashMap();
            paramMap.put(Constants.fieldMap, FieldMap);
            paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
            paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
            paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
            paramMap.put(Constants.productLevelCustomFieldMap, ProductLevelCustomFieldMap);
            JSONObject requestObj = StringUtil.convertRequestToJsonObject(request);
            
            for (int count = 0; count < invoiceIDList.size(); count++) {
                JSONArray lineItemsArr = accInvoiceCommon.getInvoiceDetailsItemJSON(requestObj, invoiceIDList.get(count), paramMap);
                itemDataAgainstInvoice.put(invoiceIDList.get(count), lineItemsArr);
            }
            otherconfigrequestParams.put(Constants.moduleid, moduleid);
            String invoicePostText = invoice.getPostText() == null ? "" : invoice.getPostText();
            ExportRecordHandler.exportSingleGeneric(request, response, itemDataAgainstInvoice, accCustomData, customDesignDAOObj,accCommonTablesDAO, accAccountDAOobj, accountingHandlerDAOobj,
                    velocityEngine, invoicePostText,otherconfigrequestParams,accInvoiceServiceDAOObj,accGoodsReceiptServiceDAOObj);

        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
}
