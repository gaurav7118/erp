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
package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignLineItemProp;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.permissionHandler.permissionHandler;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.profileHandler.profileHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.MessageSource;

/**
 *
 * @author krawler
 */
public class AccountingHandlerDAOImpl extends BaseDAO implements AccountingHandlerDAO {

    private profileHandlerDAO profileHandlerDAOObj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private VelocityEngine velocityEngine;
    private MessageSource messageSource;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
     
     public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setprofileHandlerDAO(profileHandlerDAO profileHandlerDAOObj1) {
        this.profileHandlerDAOObj = profileHandlerDAOObj1;
    }

    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }
    
    public void setvelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }
    
    public KwlReturnObject getObject(String classpath, String id) throws ServiceException {
        List list = new ArrayList();
        try {
            Class cls = Class.forName(classpath);
            Object obj = get(cls, id);
            list.add(obj);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject loadObject(String classpath, String id) throws ServiceException {
        List list = new ArrayList();
        try {
            Class cls = Class.forName(classpath);
            Object obj = load(cls, id);
            list.add(obj);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    /*
     * Check If Rule is apply on product category from multiapproverule window
     */
    public boolean checkForProductCategoryForProduct(JSONArray productCategoryMapList, int appliedUpon, String rule) {
        boolean sendForApproval = false;
        int listSize = 0;
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        if (productCategoryMapList != null) {
            try {
                for (int cnt = 0; cnt < productCategoryMapList.length(); cnt++) {
                    String productId = "";
                    List list = null;
                    JSONObject jObj = (JSONObject) productCategoryMapList.get(cnt);
                    productId = jObj.get("productId").toString();
                    String query = "select count(ID) from ProductCategoryMapping where productCategory.ID=? and productID.ID=?";
                    list = executeQuery(query, new String[]{rule, productId});
                    if (list != null && list.size() > 0) {
                        Long count = (Long) list.get(0);
                        if (count > 0) {
                            sendForApproval = true;
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return sendForApproval;
    }
    
    
    @Override
    public KwlReturnObject getInvoiceFromFirstDB(String [] subdomain,String dbName) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            String query="select invoicenumber,exchangeratedetail,porefno,duedate,porefdate,je.company,inv.currency,je.externalcurrencyrate,mastersalesperson,inv.customer,account,je.entrydate,invoiceamountdue,je.externalcurrencyrate,termid,inv.memo,journalentry,inv.seqnumber, inv.datePreffixValue, inv.dateAfterPreffixValue, inv.dateSuffixValue , inv.seqformat,inv.autogen from "+dbName+".invoice as inv, "+dbName+".journalentry as je where je.id=inv.journalentry and je.company =(select companyid from  "+dbName+".company where subdomain=?) and inv.deleteflag='F' and inv.isfixedassetinvoice='F' and inv.istemplate=0 and inv.cashtransaction = 0 and inv.isdraft = 0 ";
            params.add(subdomain[0]);
            list = executeSQLQuery( query, params.toArray());
            count = list.size();           
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getInvoiceFromFirstDB" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    public KwlReturnObject getFieldParamsFromFirstDB(HashMap<String, Object> requestParams,String dbName) {
        KwlReturnObject result = null;
         List list =null;
        try {
            ArrayList params = new ArrayList();
            String hql = "";
            hql = "select id,colnum,fieldlabel,fieldname,fieldtype,refcolnum  from  "+dbName+".fieldparams where moduleid=? and companyid=? ";
           params.add(requestParams.get("moduleid"));
           params.add(requestParams.get("companyid"));
             list = executeSQLQuery( hql, params.toArray());
           

        } catch (Exception ex) {
            ex.printStackTrace();

        }
         return new KwlReturnObject(true, "", null, list, list.size());
    }
    public String getCustomDataUsingColNum(HashMap<String, Object> requestParams,String dbName) {
        String colData = "";
        try {
            List list = null;
            ArrayList params = new ArrayList();
            String colNum = (String) requestParams.get("colNum");
            params.add(requestParams.get("journalentryId"));
            params.add(requestParams.get("companyid"));
            String query = "select " + colNum + " from  "+dbName+".accjecustomdata where journalentryId = ? and company = ?";
            list = executeSQLQuery(query, params.toArray());
            if (list != null && list.size() > 0 && list.get(0)!=null) {
                colData = list.get(0).toString();
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return colData;
    }
    
    public String getCustomDataUsingColNumInv(HashMap<String, Object> requestParams) {
        String colData = "";
        try {
            List list = null;
            ArrayList params = new ArrayList();
            String colNum = (String) requestParams.get("colNum");
            params.add(requestParams.get("productId"));
            params.add(requestParams.get("companyid"));
            String query = "select " + colNum + " from accproductcustomdata where productId = ? and company = ?";
            list = executeSQLQuery(query, params.toArray());
            if (!list.isEmpty() && list.get(0)!=null && list.size() > 0) {
                colData = list.get(0).toString();
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return colData;
    }
    
    
    public String getCustomDataUsingColNumRec(HashMap<String, Object> requestParams) {
        String colData = "";
        try {
            List list = null;
            ArrayList params = new ArrayList();
            String colNum = (String) requestParams.get("colNum");
            params.add(requestParams.get("jedId"));
            params.add(requestParams.get("companyid"));
            String query = "select " + colNum + " from accjedetailcustomdata where jedetailId= ? and company = ?";
            list = executeSQLQuery(query, params.toArray());
            if (list != null && list.size() > 0 && list.get(0) != null) {
                colData = list.get(0).toString();
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return colData;
    }
    
    public String getCustomDataUsingColNumVal(String id) {
        String colData = "";
        try {
            List list = null;
            ArrayList params = new ArrayList();
            params.add(id);
            String query = "select value from fieldcombodata where id= ? ";
            list = executeSQLQuery(query, params.toArray());
            if (list != null && list.size() > 0) {
                colData = list.get(0).toString();
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return colData;
    }
    
    
    @Override
    public KwlReturnObject getVendorInvoiceFromFirstDB(String [] subdomain,String dbName) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            String query = "select grnumber,exchangeratedetail,partyinvoicenumber,duedate,partyinvoicedate,je.company,inv.currency,je.externalcurrencyrate,masteragent,inv.vendor,account,je.entrydate,invoiceamountdue,je.externalcurrencyrate,termid,inv.memo,journalentry,inv.seqnumber, inv.datePreffixValue, inv.dateAfterPreffixValue, inv.dateSuffixValue , inv.seqformat,inv.autogen from  " + dbName + ".goodsreceipt as inv, " + dbName + ".journalentry as je where je.id=inv.journalentry and je.company =(select companyid from  " + dbName + ".company where subdomain=?) and inv.deleteflag='F' and inv.isconsignment='F' and inv.istemplate=0";
            
            params.add(subdomain[0]);
            list = executeSQLQuery( query, params.toArray());
            count = list.size();           
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getInvoiceFromFirstDB" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject getCustomerCreditNoteFromFirstDB(String [] subdomain,String dbName) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            String query="select cnnumber,cnamountdue,cn.currency,je.externalcurrencyrate,cn.memo,cn.company,narration,je.entrydate,cn.customer,cn.account,je.externalcurrencyrate,journalentry,cn.vendor,salesperson,cn.seqnumber, cn.datePreffixValue, cn.dateAfterPreffixValue, cn.dateSuffixValue , cn.seqformat,cn.autogen  from  "+dbName+".creditnote as cn, "+dbName+".journalentry as je where je.id=cn.journalentry and je.company =(select companyid from  "+dbName+".company where subdomain=?) and cn.deleteflag='F'";
            
            params.add(subdomain[0]);
            list = executeSQLQuery( query, params.toArray());
            count = list.size();           
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getInvoiceFromFirstDB" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    
    @Override
    public KwlReturnObject getCustomerDebitNoteFromFirstDB(String [] subdomain,String dbName) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            String query="select dnnumber,dnamountdue,dn.currency,je.externalcurrencyrate,dn.memo,dn.company,narration,je.entrydate,dn.customer,dn.account,je.externalcurrencyrate,journalentry,dn.vendor,salesperson,dn.seqnumber, dn.datePreffixValue, dn.dateAfterPreffixValue, dn.dateSuffixValue , dn.seqformat,dn.autogen  from  "+dbName+".debitnote as dn, "+dbName+".journalentry as je where je.id=dn.journalentry and je.company =(select companyid from  "+dbName+".company where subdomain=?) and dn.deleteflag='F'";
            
            params.add(subdomain[0]);
            list = executeSQLQuery( query, params.toArray());
            count = list.size();           
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getInvoiceFromFirstDB" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
     
    
    @Override
    public KwlReturnObject getReceiptFromFirstDB(String [] subdomain,String dbName) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();
//            String query="select receiptnumber,rad.amountdue,rec.currency,rec.externalcurrencyrate,rec.memo,rec.company,je.entrydate,rec.customer,rec.account,je.externalcurrencyrate,pm.methodname,che.chequeno,pm.account,che.description,che.bankname,che.duedate,che.BankMasterItem,pm.id from  "+dbName+".receipt as rec, "+dbName+".journalentry as je, "+dbName+".receiptadvancedetail as rad, "+dbName+".paydetail as pd, "+dbName+".paymentmethod as pm, "+dbName+".cheque as che  where je.id=rec.journalentry and rad.receipt=rec.id and pd.id=rec.paydetail and pm.id=pd.paymentMethod and che.id=pd.cheque and je.company =(select companyid from  "+dbName+".company where subdomain=?)";
            String query="select receiptnumber,rad.amountdue,rec.currency,rec.externalcurrencyrate,rec.memo,rec.company,je.entrydate,rec.customer,rec.account,je.externalcurrencyrate,pm.methodname,che.chequeno,pm.account,che.description,che.bankname,che.duedate,che.BankMasterItem,pm.id,rec.seqnumber, rec.datePreffixValue, rec.dateAfterPreffixValue, rec.dateSuffixValue , rec.seqformat,rec.autogen from  "+dbName+".receipt as rec inner join  "+dbName+".journalentry as je on je.id=rec.journalentry inner join  "+dbName+".receiptadvancedetail as rad on  rad.receipt=rec.id inner join  "+dbName+".paydetail as pd on pd.id=rec.paydetail inner join  "+dbName+".paymentmethod as pm on pm.id=pd.paymentMethod left join  "+dbName+".cheque as che on che.id=pd.cheque where  je.company =(select companyid from  "+dbName+".company where subdomain=?) and rec.deleteflag='F'";
            params.add(subdomain[0]);
            list = executeSQLQuery( query, params.toArray());
            count = list.size();           
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getInvoiceFromFirstDB" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
     @Override
    public KwlReturnObject getOpeningInvoiceFromFirstDB(String [] subdomain,String dbName) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            String query="select invoicenumber,exchangeratedetail,porefno,duedate,porefdate,company,inv.currency,exchangerateforopeningtransaction,mastersalesperson,inv.customer,account,creationdate,openingbalanceamountdue,exchangerateforopeningtransaction,termid,inv.memo,journalentry,inv.seqnumber, inv.datePreffixValue, inv.dateAfterPreffixValue, inv.dateSuffixValue , inv.seqformat,inv.autogen from  "+dbName+".invoice as inv where company =(select companyid from  "+dbName+".company where subdomain=?) and inv.deleteflag='F' and inv.isfixedassetinvoice='F' and isopeningbalenceinvoice=true and inv.istemplate=0 and inv.cashtransaction = 0 and inv.isdraft = 0 ";
            params.add(subdomain[0]);
            list = executeSQLQuery( query, params.toArray());
            count = list.size();           
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getInvoiceFromFirstDB" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
     
     
     @Override
    public KwlReturnObject getVendorOpeningInvoiceFromFirstDB(String [] subdomain,String dbName) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            String query="select grnumber,exchangeratedetail,partyinvoicenumber,duedate,partyinvoicedate,company,inv.currency,exchangerateforopeningtransaction,masteragent,inv.vendor,account,creationdate,openingbalanceamountdue,exchangerateforopeningtransaction,termid,inv.memo,journalentry,inv.seqnumber, inv.datePreffixValue, inv.dateAfterPreffixValue, inv.dateSuffixValue , inv.seqformat,inv.autogen from  "+dbName+".goodsreceipt as inv where company =(select companyid from  "+dbName+".company where subdomain=?) and inv.deleteflag='F' and inv.isfixedassetinvoice='F'  and inv.isconsignment='F' and isopeningbalenceinvoice=true and inv.istemplate=0";
            
            params.add(subdomain[0]);
            list = executeSQLQuery( query, params.toArray());
            count = list.size();           
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getInvoiceFromFirstDB" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
     
     @Override
    public KwlReturnObject getCustomerOpeningCreditNoteFromFirstDB(String [] subdomain,String dbName) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            String query="select cnnumber,openingbalanceamountdue,cn.currency,externalcurrencyrate,cn.memo,cn.company,narration,creationdate,cn.customer,cn.account,exchangerateforopeningtransaction,journalentry,cn.vendor,salesperson, cn.seqnumber, cn.datePreffixValue, cn.dateAfterPreffixValue, cn.dateSuffixValue , cn.seqformat,cn.autogen from  "+dbName+".creditnote as cn where company =(select companyid from  "+dbName+".company where subdomain=?) and cn.deleteflag='F' and isopeningbalencecn=true";
            
            params.add(subdomain[0]);
            list = executeSQLQuery( query, params.toArray());
            count = list.size();           
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getInvoiceFromFirstDB" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
     
     @Override
    public KwlReturnObject getCustomerOpeningDebitNoteFromFirstDB(String [] subdomain,String dbName) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            String query="select dnnumber,openingbalanceamountdue,dn.currency,externalcurrencyrate,dn.memo,dn.company,narration,creationdate,dn.customer,dn.account,exchangerateforopeningtransaction,journalentry,dn.vendor,salesperson,dn.seqnumber, dn.datePreffixValue, dn.dateAfterPreffixValue, dn.dateSuffixValue , dn.seqformat,dn.autogen  from  "+dbName+".debitnote as dn where company =(select companyid from  "+dbName+".company where subdomain=?) and dn.deleteflag='F' and isopeningbalencedn=true";
            
            params.add(subdomain[0]);
            list = executeSQLQuery( query, params.toArray());
            count = list.size();           
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getInvoiceFromFirstDB" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
     
     @Override
    public KwlReturnObject getOpeningReceiptFromFirstDB(String [] subdomain,String dbName) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();
//            String query="select receiptnumber,rad.amountdue,rec.currency,rec.externalcurrencyrate,rec.memo,rec.company,je.entrydate,rec.customer,rec.account,je.externalcurrencyrate,pm.methodname,che.chequeno,pm.account,che.description,che.bankname,che.duedate,che.BankMasterItem,pm.id from  "+dbName+".receipt as rec, "+dbName+".journalentry as je, "+dbName+".receiptadvancedetail as rad, "+dbName+".paydetail as pd, "+dbName+".paymentmethod as pm, "+dbName+".cheque as che  where je.id=rec.journalentry and rad.receipt=rec.id and pd.id=rec.paydetail and pm.id=pd.paymentMethod and che.id=pd.cheque and je.company =(select companyid from  "+dbName+".company where subdomain=?)";
            String query="select receiptnumber,rec.openingbalanceamountdue,rec.currency,rec.externalcurrencyrate,rec.memo,rec.company,creationdate,rec.customer,rec.account,exchangerateforopeningtransaction,pm.methodname,che.chequeno,pm.account,che.description,che.bankname,che.duedate,che.BankMasterItem,pm.id,rec.seqnumber, rec.datePreffixValue, rec.dateAfterPreffixValue, rec.dateSuffixValue , rec.seqformat,autogen from  "+dbName+".receipt as rec  left join  "+dbName+".receiptadvancedetail as rad on  rad.receipt=rec.id left join  "+dbName+".paydetail as pd on pd.id=rec.paydetail left join  "+dbName+".paymentmethod as pm on pm.id=pd.paymentMethod left join  "+dbName+".cheque as che on che.id=pd.cheque where  rec.company =(select companyid from  "+dbName+".company where subdomain=?) and rec.deleteflag='F' and isopeningbalencereceipt=true";
            params.add(subdomain[0]);
            list = executeSQLQuery( query, params.toArray());
            count = list.size();           
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getInvoiceFromFirstDB" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
     
     
     @Override
    public KwlReturnObject getOpeningPaymentFromFirstDB(String [] subdomain,String dbName) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();
//            String query="select paymentnumber,rad.amountdue,rec.currency,rec.externalcurrencyrate,rec.memo,rec.company,je.entrydate,rec.vendor,rec.account,je.externalcurrencyrate,pm.methodname,che.chequeno,pm.account,che.description,che.bankname,che.duedate,che.BankMasterItem,pm.id from  "+dbName+".payment as rec, "+dbName+".journalentry as je, "+dbName+".advancedetail as rad, "+dbName+".paydetail as pd, "+dbName+".paymentmethod as pm, "+dbName+".cheque as che  where je.id=rec.journalentry and rad.payment=rec.id and pd.id=rec.paydetail and pm.id=pd.paymentMethod and che.id=pd.cheque and je.company =(select companyid from  "+dbName+".company where subdomain=?)";
            String query="select paymentnumber,rec.openingbalanceamountdue,rec.currency,rec.externalcurrencyrate,rec.memo,rec.company,creationdate,rec.vendor,rec.account,exchangerateforopeningtransaction,pm.methodname,che.chequeno,pm.account,che.description,che.bankname,che.duedate,che.BankMasterItem,pm.id,rec.seqnumber, rec.datePreffixValue, rec.dateAfterPreffixValue, rec.dateSuffixValue , rec.seqformat,rec.autogen from  "+dbName+".payment as rec  left join  "+dbName+".advancedetail as rad on rad.payment=rec.id left join  "+dbName+".paydetail as pd on pd.id=rec.paydetail left join   "+dbName+".paymentmethod as pm on pm.id=pd.paymentMethod left join  "+dbName+".cheque as che on che.id=pd.cheque where  rec.company =(select companyid from  "+dbName+".company where subdomain=?) and rec.deleteflag='F' and isopeningbalencepayment=true";
            
            params.add(subdomain[0]);
            list = executeSQLQuery( query, params.toArray());
            count = list.size();           
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getInvoiceFromFirstDB" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject getPaymentFromFirstDB(String [] subdomain,String dbName) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();
//            String query="select paymentnumber,rad.amountdue,rec.currency,rec.externalcurrencyrate,rec.memo,rec.company,je.entrydate,rec.vendor,rec.account,je.externalcurrencyrate,pm.methodname,che.chequeno,pm.account,che.description,che.bankname,che.duedate,che.BankMasterItem,pm.id from  "+dbName+".payment as rec, "+dbName+".journalentry as je, "+dbName+".advancedetail as rad, "+dbName+".paydetail as pd, "+dbName+".paymentmethod as pm, "+dbName+".cheque as che  where je.id=rec.journalentry and rad.payment=rec.id and pd.id=rec.paydetail and pm.id=pd.paymentMethod and che.id=pd.cheque and je.company =(select companyid from  "+dbName+".company where subdomain=?)";
            String query="select paymentnumber,rad.amountdue,rec.currency,rec.externalcurrencyrate,rec.memo,rec.company,je.entrydate,rec.vendor,rec.account,je.externalcurrencyrate,pm.methodname,che.chequeno,pm.account,che.description,che.bankname,che.duedate,che.BankMasterItem,pm.id,rec.seqnumber, rec.datePreffixValue, rec.dateAfterPreffixValue, rec.dateSuffixValue , rec.seqformat,rec.autogen from  "+dbName+".payment as rec inner join  "+dbName+".journalentry as je on je.id=rec.journalentry inner join  "+dbName+".advancedetail as rad on rad.payment=rec.id inner join  "+dbName+".paydetail as pd on pd.id=rec.paydetail inner join   "+dbName+".paymentmethod as pm on pm.id=pd.paymentMethod left join  "+dbName+".cheque as che on che.id=pd.cheque where  je.company =(select companyid from  "+dbName+".company where subdomain=?) and rec.deleteflag='F'";
            
            params.add(subdomain[0]);
            list = executeSQLQuery( query, params.toArray());
            count = list.size();           
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getInvoiceFromFirstDB" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    public KwlReturnObject getObject(String classpath, Integer id) throws ServiceException {
        List list = new ArrayList();
        try {
            Class cls = Class.forName(classpath);
            Object obj = get(cls, id);
            list.add(obj);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject saveOrUpdateObject(Object object) throws ServiceException {
        List list = new ArrayList();
        try {
            if (object != null) {
                saveOrUpdate(object);
                list.add(object);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public void evictObj(Object currentSessionObject) {
        getHibernateTemplate().evict(currentSessionObject);
    }
    
    //Synchronizes hibernate session with database, i.e. saves any unsaved objects/operations in database
    @Override
    public void flushHibernateSession() {
        getHibernateTemplate().flush();
    }
    
     @Override
    public Boolean checkSecurityGateFunctionalityisusedornot(String companyid) throws ServiceException {
        boolean isCreateSecurity = false;
        List list = executeQuery("select count(id) from SecurityGateEntry where company.companyID=?", companyid);
        if (!list.isEmpty() && list.size() > 0) {
            Long count = (Long) list.get(0);
            if (count > 0) {
                isCreateSecurity = true;
            }
        }
        return isCreateSecurity;
    }
     @Override
    public Boolean checkIsVendorAsCustomer(String companyid,String customerId) throws ServiceException {
        boolean isCreateCustomer = false;
        ArrayList params = new ArrayList();
        params.add(companyid);
        params.add(customerId);
        params.add(true);
        List list = executeQuery("select count(id) from Customer where company.companyID=? and name=? and mapcustomervendor=?", params.toArray());
        if (!list.isEmpty() && list.size() > 0) {
            Long count = (Long) list.get(0);
            if (count > 0) {
                isCreateCustomer = true;
            }
        }
        return isCreateCustomer;
    }


//    public static Object getObject(HibernateTemplate hibernateTemplate, String classpath, String id) throws ServiceException {
//        Object obj = null;
//        try {
//            Class cls = Class.forName(classpath);
//            obj = hibernateTemplate.get(cls, id);
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE(ex.getMessage(), ex);
//        }
//        return obj;
//    }

    /*
     * public Object invokeMethod(String modulename, String method, Object[]
     * params) throws ServiceException { Object result = null; String beanid =
     * ""; try { // BeanFactory factory = getBeanFactory(); beanid =
     * ConfigReader.getinstance().get(modulename+"BeanId");
     *
     * Object beanobj = factory.getBean(beanid); Class cl1 = beanobj.getClass();
     * Object invoker = beanobj;
     *
     * int len = params.length; Class[] arguments = new Class[len]; for(int i=0;
     * i<len ; i++){ arguments[i] = params[i].getClass(); }
     *
     * java.lang.reflect.Method objMethod1 = cl1.getMethod(method, arguments);
     * result = objMethod1.invoke(invoker, params); } catch
     * (IllegalAccessException ex) {
     * Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE,
     * null, ex); throw
     * ServiceException.FAILURE("invokeMethod.IllegalAccessException,
     * MethodName="+method, ex); } catch (IllegalArgumentException ex) {
     * Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE,
     * null, ex); throw
     * ServiceException.FAILURE("invokeMethod.IllegalArgumentException,
     * MethodName="+method, ex); } catch (InvocationTargetException ex) {
     * Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE,
     * null, ex); throw
     * ServiceException.FAILURE("invokeMethod.TargetMethodInvocationException,
     * MethodName="+method, ex); } catch (NoSuchMethodException ex) {
     * Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE,
     * null, ex); throw
     * ServiceException.FAILURE("invokeMethod.NoSuchMethodException,
     * MethodName="+method, ex); } catch (SecurityException ex) {
     * Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE,
     * null, ex); throw
     * ServiceException.FAILURE("invokeMethod.SecurityException,
     * MethodName="+method, ex); } catch (NoSuchBeanDefinitionException ex) {
     * Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE,
     * null, ex); throw
     * ServiceException.FAILURE("invokeMethod.NoSuchBeanDefinitionException,
     * BeanId="+beanid, ex); } return result; }
     *
     * private BeanFactory getBeanFactory() { // BeanFactory factory = new
     * XmlBeanFactory(new
     * FileSystemResource("classpath:../dispatcher-servlet.xml")); //
     * ClassPathXmlApplicationContext appContext = new
     * ClassPathXmlApplicationContext("classpath:../dispatcher-servlet.xml"); //
     * ClassPathXmlApplicationContext appContext = new
     * ClassPathXmlApplicationContext(new String[]
     * {"classpath:../applicationContext.xml",
     * "classpath:../dispatcher-servlet.xml"}); // BeanFactory factory =
     * (BeanFactory) appContext; BeanFactory bfactory = new XmlBeanFactory(new
     * ClassPathResource("../dispatcher-servlet.xml")); return bfactory; }
     */
    public ArrayList getApprovalFlagForProducts(ArrayList productlist, String typeid, String fieldtype, String companyid) throws ServiceException {
        boolean approvalFlag = false;
        List list = new ArrayList();
        int approvallevel = 0;
        ArrayList returnParams = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(companyid);
        params.add(typeid);
        params.add(fieldtype);

        Iterator itProd = productlist.iterator();
        String condition = "";
        while (itProd.hasNext()) {
            String obj = (String) itProd.next();
            condition += " value like ? or";
            params.add("%" + obj + "%");
        }
        if (condition.length() > 0) {
            condition = " and (" + condition.substring(0, condition.lastIndexOf("or")) + ")";
        }

        String query = "select value, approvallevel from approvalrules where company = ? and typeid = ? and fieldtype = ? " + condition;
        list = executeSQLQuery(query, params.toArray());
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Object[] val = (Object[]) it.next();
            int approvalLevelSet = (val[1] == null) ? 1 : Integer.parseInt(val[1].toString());

            if (approvallevel < approvalLevelSet) {
                approvallevel = approvalLevelSet;
            }
            approvalFlag = true;
        }

        returnParams.add(approvalFlag);
        returnParams.add(approvallevel);
        return returnParams;
    }

     public KwlReturnObject deleteCustomizeReportColumn(String id, String companyId, int reportId) throws ServiceException {
        int numRows = 0;
        String delQuery = "delete from CustomizeReportMapping where id=? and company.companyID=? and reportId=?";
        numRows = executeUpdate(delQuery, new Object[]{id, companyId, reportId});
        return new KwlReturnObject(true, "Custom Column has been deleted successfully.", null, null, numRows);
    }
    
    public KwlReturnObject getCustomizeReportViewMappingField(HashMap hm) throws ServiceException { //function to check delivery order used in sales return
        List returnlist = new ArrayList();
        ArrayList params = new ArrayList();
        int count = 0;
        String company="";
        int reportId=0;
        int moduleId=0;
        try {
            String query = "from CustomizeReportMapping where company.companyID=? and reportId=? ";
            if (hm.containsKey("companyId")) {
                company = hm.get("companyId").toString();
                params.add(company);
            }
            if (hm.containsKey("reportId")) {
                reportId = Integer.parseInt(hm.get("reportId").toString());
                params.add(reportId);
            }
            if (hm.containsKey("moduleId")) {
                if(hm.get("moduleId").toString().contains(",")){
                    query += " and moduleId IN("+hm.get("moduleId").toString()+")";
                }else{
                    query += " and moduleId=?";
                    moduleId = Integer.parseInt(hm.get("moduleId").toString());
                    params.add(moduleId);
                }
                
            }
            returnlist = executeQuery(query,params.toArray());
            count = returnlist.size();
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accInvoiceImpl.getCustomizeReportMappingFieldForProductCategory:" + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "", null, returnlist, count);
    }
    
    
    public ArrayList getApprovalFlagForProductsDiscount(Double discamount, String productid, String typeid, String fieldtype, String companyid, boolean approvalFlag, int approvallevel) throws ServiceException {

        List list = new ArrayList();
        ArrayList returnParams = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(companyid);
        params.add(typeid);
        params.add(fieldtype);
        String condition = "";
        condition += " and value like ? and discountamount < ? ";
        params.add("%" + productid + "%");
        params.add(discamount);

        String query = "select value, approvallevel from approvalrules where company = ? and typeid = ? and fieldtype = ? " + condition;
        list = executeSQLQuery(query, params.toArray());
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Object[] val = (Object[]) it.next();
//            Double amountSet = (val[0] != null && !StringUtil.isNullOrEmpty(val[0].toString())) ? Double.parseDouble(val[0].toString()) : 0.0;
            int approvalLevelSet = (val[1] == null) ? 1 : Integer.parseInt(val[1].toString());

            if (approvallevel < approvalLevelSet) {
                approvallevel = approvalLevelSet;
            }
            if (!approvalFlag) {
                approvalFlag = true;
            }
        }

        returnParams.add(approvalFlag);
        returnParams.add(approvallevel);
        return returnParams;
    }

    public ArrayList getApprovalFlagForAmount(Double invoiceamount, String typeid, String fieldtype, String companyid) throws ServiceException {
        boolean approvalFlag = false;
        int approvallevel = 0;
        List list = new ArrayList();

        ArrayList params = new ArrayList();
        ArrayList returnParams = new ArrayList();
        params.add(companyid);
        params.add(typeid);
        params.add(fieldtype);

        String query = "select value, approvallevel from approvalrules where company = ? and typeid = ? and fieldtype = ?";
        list = executeSQLQuery(query, params.toArray());
        Iterator it = list.iterator();
        Double maxAmount = 0.0;
        while (it.hasNext()) {
            Object[] val = (Object[]) it.next();
            Double amountSet = (val[0] != null && !StringUtil.isNullOrEmpty(val[0].toString())) ? Double.parseDouble(val[0].toString()) : 0.0;
            int approvalLevelSet = (val[1] != null && !StringUtil.isNullOrEmpty(val[1].toString())) ? Integer.parseInt(val[1].toString()) : 1;


            if (!approvalFlag) {
                approvalFlag = (invoiceamount > amountSet) ? true : false;
            }

            if (approvalFlag && (invoiceamount > amountSet) && maxAmount <= amountSet) {
                approvallevel = approvalLevelSet;
            }

            if (maxAmount <= amountSet) {
                maxAmount = amountSet;
            }
        }

        returnParams.add(approvalFlag);
        returnParams.add(approvallevel);
        return returnParams;
    }

    public String getApprovalHistory(String billid, String companyid, DateFormat df,HashMap<String, Object> hm) throws ServiceException {
        Object[] docMapArr = null;
        String msg = "";
        ArrayList params = new ArrayList();
        params.add(billid);
        params.add(companyid);
        Locale locale = null;
        if(hm.containsKey("locale")){
        locale = (Locale) hm.get("locale");
        }
        String query = "from Approvalhistory where transid = ? and company.companyID = ? order by approvedon";
        List list = executeQuery(query, params.toArray());
        Iterator it = list.iterator();
        
        while (it.hasNext()) {
            Approvalhistory obj = (Approvalhistory) it.next();
            
            // for getting attachment of requisition
            ArrayList approvalMapParams = new ArrayList();
            approvalMapParams.add(obj.getID());
            approvalMapParams.add(companyid);
            String approvalMapQuery = " select docMap.document.docID, docMap.document.docName from InvoiceDocumentCompMap docMap "
                    + " where docMap.invoiceID = ? and docMap.company.companyID = ? ";
            List approvalMapList = executeQuery(approvalMapQuery, approvalMapParams.toArray());
            if (approvalMapList != null && !approvalMapList.isEmpty()) {
                docMapArr = (Object[]) approvalMapList.get(0);
            }
            
            
            Date approveDate = new Date();
            approveDate.setTime(obj.getApprovedon());
            String approveDateString = df.format(approveDate);
            msg += "<table style='width:100%;border-bottom:1px solid #bfbfbf;'>";
            msg += "<tr><td style='padding-right:5px;'><b>"+(obj.isRejected()?messageSource.getMessage("acc.common.Rejected", null, locale):messageSource.getMessage("acc.common.Approved", null, locale))+" " + messageSource.getMessage("acc.common.Level", null, locale) + "<span style='float:right'>:</span></b></td><td>Level " + Math.abs(obj.getApprovallevel()) + "</td></tr>";
            msg += "<tr><td style='padding-right:5px;'><b>"+(obj.isRejected()?messageSource.getMessage("acc.common.Rejected", null, locale):messageSource.getMessage("acc.common.Approved", null, locale))+" "+ messageSource.getMessage("acc.common.On", null, locale) +"<span style='float:right'>:</span></b></td><td>" + approveDateString + "</td></tr>";
            msg += "<tr><td style='padding-right:5px;width:100px'><b>"+messageSource.getMessage("acc.field.ConsignmentRequestApprovalApprover", null, locale)+"<span style='float:right'>:</span></b></td><td>" + obj.getApprover().getFirstName() + " " + obj.getApprover().getLastName() + "</td></tr>";
            msg += "<tr><td style='vertical-align:top;padding-right:5px;'><b>"+ messageSource.getMessage("acc.invoice.gridRemark", null, locale) + "<span style='float:right'>:</span></b></td><td>" + obj.getRemark() + "</td></tr>";
            
            if (docMapArr != null) {
                String docname = (String) docMapArr[1];
                String Ext = "";
                if (docname.contains(".")) {
                    Ext = docname.substring(docname.lastIndexOf("."));
                }
                msg += "<tr><td style='vertical-align:top;padding-right:5px;'><b>Attachment<span style='float:right'>:</span></b></td><td>"
                    + " <a class='tbar-link-text' href='#' onclick=\"setDldUrl(\'" + "../../fdownload.jsp?url=" + docMapArr[0] + Ext + "&dtype=attachment" + "&docname=" + docMapArr[1] + "\')\">" + docMapArr[1] + "</a>"
                    + "</td></tr>";
            }
            
            msg += "</table>";
        }
        if (StringUtil.isNullOrEmpty(msg)) {
            msg = messageSource.getMessage("acc.je.NoApprovalHistorytodisplay", null, locale);
        }
        return msg;
    }

    public KwlReturnObject getApprovalHistoryForExport(String billid, String companyid) throws ServiceException {
        List list = null;
        int listSize = 0;
        try {
            ArrayList params = new ArrayList();
            params.add(billid);
            params.add(companyid);

            String query = "from Approvalhistory where transid = ? and company.companyID = ? order by approvallevel";
            list = executeQuery(query, params.toArray());
            if (list != null) {
                listSize = list.size();
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }
    
    public KwlReturnObject updateApprovalHistory(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        try {
            Approvalhistory history = new Approvalhistory();

            if (hm.containsKey("transtype")) {
                history.setTranstype((String) hm.get("transtype"));
            }

            if (hm.containsKey("transid")) {
                history.setTransid((String) hm.get("transid"));
            }

            history.setApprovedon(System.currentTimeMillis());

            if (hm.containsKey("approvallevel")) {
                history.setApprovallevel((Integer) hm.get("approvallevel"));
            }

            if (hm.containsKey("remark")) {
                history.setRemark((String) hm.get("remark"));
            }

            if (hm.containsKey("userid")) {
                User user = (hm.get("userid") == null ? null : (User) get(User.class, (String) hm.get("userid")));
                history.setApprover(user);
            }

            if (hm.containsKey("companyid")) {
                Company cmp = (hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid")));
                history.setCompany(cmp);
            }
            
            if (hm.containsKey("isrejected") && hm.get("isrejected")!=null) {
                history.setRejected(Boolean.parseBoolean(hm.get("isrejected").toString()));
            }

            saveOrUpdate(history);
            list.add(history);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccountingHandlerDAOImpl.updateApprovalHistory : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Approval History updated successfully.", null, list, list.size());
    }

    public KwlReturnObject saveModuleTemplate(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        try {
            ModuleTemplate mtemp = new ModuleTemplate();

            if (hm.containsKey("moduletemplateid")) {
                String id = (String) hm.get("moduletemplateid");
                if (!StringUtil.isNullOrEmpty(id)) {
                    mtemp = (ModuleTemplate) get(ModuleTemplate.class, id);
                }
            }
            if (hm.containsKey("templatename")) {
                mtemp.setTemplateName((String) hm.get("templatename"));
            }

            if (hm.containsKey("moduleid")) {
                mtemp.setModuleId((Integer) hm.get("moduleid"));
            }

            if (hm.containsKey("modulerecordid")) {
                mtemp.setModuleRecordId((String) hm.get("modulerecordid"));
            }
            if (hm.containsKey("companyunitid")) {
                mtemp.setCompanyUnitid((String) hm.get("companyunitid"));
            }
            if (hm.containsKey("populateproducttemplate")) {
                mtemp.setPopulateproductintemp((Boolean) hm.get("populateproducttemplate"));
            }
             if (hm.containsKey("populatecustomertemplate")) {
                mtemp.setPopulatecustomerintemp((Boolean) hm.get("populatecustomertemplate"));
            }
             if (hm.containsKey("populateautodointemp")) {
                mtemp.setPopulateautodointemp((Boolean) hm.get("populateautodointemp"));
            }
            if (hm.containsKey("companyid")) {
                Company cmp = (hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid")));
                mtemp.setCompany(cmp);
            }
             if (hm.containsKey("isdefaulttemplate") && hm.get("isdefaulttemplate") != null) {
                mtemp.setIsdefaulttemplate((Boolean) hm.get("isdefaulttemplate"));
            }

            saveOrUpdate(mtemp);
            list.add(mtemp);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccountingHandlerDAOImpl.updateApprovalHistory : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Approval History updated successfully.", null, list, list.size());
    }
    public KwlReturnObject getApprovalHistory(HashMap<String, Object> approvalHisMap) throws ServiceException {
        List list = null;
        int listSize = 0;
        String companyid = "";
        String billid = "";
        String transtype = "";
        String condition="";
        try {
            
            ArrayList params = new ArrayList();
            if (approvalHisMap.containsKey(Constants.companyid)) {
                companyid = (String) approvalHisMap.get(Constants.companyid);
                params.add(companyid);
            }
            if (approvalHisMap.containsKey("billid")) {
                billid = (String) approvalHisMap.get("billid");
                condition += " and transid = ?";
                params.add(billid);
                condition += " order by approvedon desc";
            }
            String query = "from Approvalhistory where company.companyID = ? "+condition;
            list = executeQuery(query, params.toArray());
            if (list != null) {
                listSize = list.size();
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }
    @Override
    public KwlReturnObject getModuleTemplates(HashMap hm) {
        List list = null;
        int listSize = 0;
        try {
            String companyId = hm.get("companyid") + "";
            int moduleId = 0;
            boolean CheckExciseUnit = false;
            ArrayList params = new ArrayList();
            String hql = "from ModuleTemplate mt where mt.company.companyID=?";
            if (!StringUtil.isNullOrEmpty(companyId)) {
                params.add(companyId);
                if (hm.containsKey("moduleId")) {
                    if (hm.get("moduleId").toString().contains(",")) {
                        hql += " And mt.moduleId IN(" + hm.get("moduleId").toString() + ")";
                    } else {
                        moduleId = Integer.parseInt(hm.get("moduleId").toString());
                        hql += " And mt.moduleId=?";
                        params.add(moduleId);
                    }
                }
                if (hm.containsKey("CheckExciseUnit")) {
                    CheckExciseUnit = (boolean) hm.get("CheckExciseUnit");
                    if(CheckExciseUnit){
                        hql += " And mt.companyUnitid IS NOT NULL";
                        
                    }
                }
                if (hm.containsKey("modulerecordid")) {
                    String modulerecordid = hm.get("modulerecordid").toString();
                    hql += " And mt.moduleRecordId = ?";
                    params.add(modulerecordid);
                }
                if (hm.containsKey("companyunitid")) {
                    String companyunitid = hm.get("companyunitid").toString();
                    hql += " And mt.companyUnitid = ?";
                    params.add(companyunitid);
                }
                /*
                Need to return one entry for one temlate record
                Stock request template saved details wise due to which apply grp by
                */
                if(moduleId==Constants.Acc_Stock_Request_ModuleId){
                    hql += " Group by templateName";
                }
                list = executeQuery(hql, params.toArray());
            }
            if (list != null) {
                listSize = list.size();
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }
    
    @Override
    public KwlReturnObject getExciseTemplatesMap(HashMap hm) {
        List list = null;
        int listSize = 0;
        try {
            String companyId = hm.get("companyid") + "";

            ArrayList params = new ArrayList();
            String hql = "from ExciseDetailsTemplateMap edtm where edtm.companyid.companyID=?";
            if (!StringUtil.isNullOrEmpty(companyId)) {
                params.add(companyId);
//                if (hm.containsKey("templateId")) {
//                    String templateid = hm.get("templateId").toString();
//                    hql += " And edtm.templateid.templateId=?";
//                    params.add(templateid);
//                }
                
//                if (hm.containsKey("moduleRecordId")) {
//                    String moduleRecordId = hm.get("moduleRecordId").toString();
//                    hql += " And edtm.templateid.moduleRecordId=?";
//                    params.add(moduleRecordId);
//                }
                /**
                 * Get Excise template details by id if present in request, otherwise get all Excise Unit details
                 */
                if (hm.containsKey("companyunitid") && !StringUtil.isNullOrEmpty(hm.get("companyunitid").toString())) {
                    String moduleRecordId = hm.get("companyunitid").toString();
                    hql += " And id=?";
                    params.add(moduleRecordId);
                }
                list = executeQuery(hql, params.toArray());
            }
            if (list != null) {
                listSize = list.size();
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }
    

    @Override
    public KwlReturnObject getModuleTemplateForTemplatename(HashMap hm) {
        List list = null;
        int listSize = 0;
        try {
            String companyId = hm.get("companyid") + "";
            ArrayList params = new ArrayList();

            if (!StringUtil.isNullOrEmpty(companyId)) {
                String hql = "from ModuleTemplate mt where mt.company.companyID=? ";
                params.add(companyId);

                if (hm.containsKey("moduletemplateid")) {
                    String templateid = (String) hm.get("moduletemplateid");
                     hql += " And mt.templateId != ? ";
                    params.add(templateid);
                }
                if (hm.containsKey("templatename")) {
                    String templatename = hm.get("templatename").toString();
                    hql += " And mt.templateName=? ";
                    params.add(templatename);
                }

                if (hm.containsKey("moduleid")) {
                    int moduleId = Integer.parseInt(hm.get("moduleid").toString());
                    hql += " And mt.moduleId=?";
                    params.add(moduleId);
                }

                list = executeQuery(hql, params.toArray());
            }
            if (list != null) {
                listSize = list.size();
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject deleteModuleTemplates(String companyId, String templateId) throws ServiceException {
        String delQuery = "delete from ModuleTemplate mt where mt.templateId=? and mt.company.companyID=?";
        int numRows = 0;
        if (!StringUtil.isNullOrEmpty(companyId)) {
            numRows = executeUpdate(delQuery, new Object[]{templateId, companyId});
        }
        return new KwlReturnObject(true, "ModuleTemplate has been deleted successfully.", null, null, numRows);
    }

    /**
     * @param mailParameters (String Number, String fromName, String[] emails, String fromEmailId, String moduleName, String companyid, String PAGE_URL)
     */
    public void sendApprovalEmails(Map<String, Object> mailParameters) {
        try {
            String companyid = "";
            if (mailParameters.containsKey(Constants.companyid)) {
                companyid = (String) mailParameters.get(Constants.companyid);
            }
            String[] emails = null;
            if((mailParameters.containsKey(Constants.emails))){
                emails = ((String[]) mailParameters.get(Constants.emails));
            }
            String ApprovalSubject = "Approval Notification: %S: %S";
            String ApprovalHtmlMsg = "<html><head><title>Deskera Accounting - Your Deskera Account</title></head><style type='text/css'>"
                    + "a:link, a:visited, a:active {\n"
                    + " 	color: #03C;"
                    + "}\n"
                    + "body {\n"
                    + "	font-family: Arial, Helvetica, sans-serif;"
                    + "	color: #000;"
                    + "	font-size: 13px;"
                    + "}\n"
                    + "</style><body>"
                    + "Hi All,"
                    + "<p></p>"
                    + "<p>%s has created <b>\"%S Number: %S\"</b> and sent it to you for approval.</p>"
                    + "<p>Please review and approve it.</p>"
                    + "<p>Company Name:- %s</p>"
                    + "<p>Please check on Url:- %s</p>"
                    + "<p></p>"
                    + "<p>Thanks.</p>"
                    + "<p>This is an auto generated email. Do not reply<br>";
            String ApprovalPlainMsg = "Hi All,\n\n"
                    + "%S has created %S %S and sent it to you for approval.\n"
                    + "Please review and approve it.\n\n"
                    + "Company Name:- %s \n"
                    + "Please check on Url:- %s \n\n"
                    + "Thanks.\n\n"
                    + "This is an auto generated email. Do not reply\n";
            Company company = (Company) get(Company.class, companyid);
            Map<String, Object> filterMap = new HashMap();
            String companyName = company.getCompanyName();
            filterMap.put("companyID", companyid);
            String subject = "";
            String htmlMsg = "";
            String plainMsg = "";
            if (mailParameters.containsKey(Constants.prNumber) && mailParameters.containsKey(Constants.modulename)) {
                 subject = String.format(ApprovalSubject, (String) mailParameters.get(Constants.modulename), (String) mailParameters.get(Constants.prNumber));
            }
            if (mailParameters.containsKey(Constants.prNumber) && mailParameters.containsKey(Constants.modulename) && mailParameters.containsKey(Constants.fromName) && mailParameters.containsKey(Constants.PAGE_URL)) {
                 htmlMsg = String.format(ApprovalHtmlMsg, (String) mailParameters.get(Constants.fromName), (String) mailParameters.get(Constants.modulename),  (String) mailParameters.get(Constants.prNumber), companyName, (String) mailParameters.get(Constants.PAGE_URL));
                 plainMsg = String.format(ApprovalPlainMsg, (String) mailParameters.get(Constants.fromName), (String) mailParameters.get(Constants.modulename), (String) mailParameters.get(Constants.prNumber), companyName, (String) mailParameters.get(Constants.PAGE_URL));
            }
            if (emails.length > 0) {
                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                if (mailParameters.containsKey(Constants.fromEmailID)) {
                    SendMailHandler.postMail(emails, subject, htmlMsg, plainMsg, (String) mailParameters.get(Constants.fromEmailID), smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    public void sendApprovedEmails(Map<String, Object> mailParameters) {
        String Number = "";
        String companyid = "";
        String fromName = "";
        String fromEmailId = "";
        String moduleName = "";
        String addresseeName = "";
        String baseUrl = "";
        String emails[] = null;
        int level = 0;
        String approvalpendingStatusmsg="";

        if (mailParameters.containsKey("Number")) {
            Number = (String) mailParameters.get("Number");
        }

        if (mailParameters.containsKey(Constants.companyid)) {
            companyid = (String) mailParameters.get(Constants.companyid);
        }
        if (mailParameters.containsKey("userName")) {
            fromName = (String) mailParameters.get("userName");
        }

        if (mailParameters.containsKey("sendorInfo")) {
            fromEmailId = (String) mailParameters.get("sendorInfo");
        }
        if (mailParameters.containsKey("moduleName")) {
            moduleName = (String) mailParameters.get("moduleName");
        }
        if (mailParameters.containsKey("addresseeName")) {
            addresseeName = (String) mailParameters.get("addresseeName");
        }
        if (mailParameters.containsKey("baseUrl")) {
            baseUrl = (String) mailParameters.get("baseUrl");
        }
        if (mailParameters.containsKey("emails")) {
            emails = (String[]) mailParameters.get("emails");
        }
        if (mailParameters.containsKey("approvalstatuslevel")) {
            level = (int) mailParameters.get("approvalstatuslevel");
        }
        if (mailParameters.containsKey("approvalpendingStatusmsg")) {
            approvalpendingStatusmsg = (String) mailParameters.get("approvalpendingStatusmsg");
        }

        try {
            String ApprovalSubject = "Approval Transaction: %s - Approval Notification";
            String ApprovalHtmlMsg = "<html><head><title>Deskera Accounting - Your Deskera Account</title></head><style type='text/css'>"
                    + "a:link, a:visited, a:active {\n"
                    + " 	color: #03C;"
                    + "}\n"
                    + "body {\n"
                    + "	font-family: Arial, Helvetica, sans-serif;"
                    + "	color: #000;"
                    + "	font-size: 13px;"
                    + "}\n"
                    + "</style><body>"
                    + "<p>Hi %s,</p>"
                    + "<p></p>"
                    + "<p>%s has approved %S  <b>%s</b> at level " + (level) + approvalpendingStatusmsg + "</p>"
                    + "<p></p>"
                    + "<p>Please check on Url:- %s</p>"
                    + "<p>Thanks</p>"
                    + "<p>This is an auto generated email. Do not reply<br>";
            String ApprovalPlainMsg = "Hi %s,\n\n"
                    + "%s has been approved %S <b>%s</b> at level" + (level) + approvalpendingStatusmsg + "\n\n"
                    + "Please check on Url:- %s"
                    + "Thanks\n\n"
                    + "This is an auto generated email. Do not reply\n";
            String subject = String.format(ApprovalSubject, Number);
            String htmlMsg = String.format(ApprovalHtmlMsg, addresseeName, fromName, moduleName, Number, baseUrl);
            String plainMsg = String.format(ApprovalPlainMsg, addresseeName, fromName, moduleName, Number, baseUrl);
            if (emails.length > 0) {
                Company company = (Company) get(Company.class, companyid);
                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                SendMailHandler.postMail(emails, subject, htmlMsg, plainMsg, fromEmailId, smtpConfigMap);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    @Override
    public void sendReorderLevelEmails(String Sender,String[] emails,String moduleName, HashMap<String,String> data) throws ServiceException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject jeresult =getObject(User.class.getName(), Sender);
        User sender = (User) jeresult.getEntityList().get(0);
        Company company = (Company)get(Company.class, sender.getCompany().getCompanyID());
        
        List ll;
        String msg = null;


        try {
            if (data != null  && data.size()>0) {
                int sno = 1;

                String emailIds = "";
                String mailSeparator = ",";
                boolean isfirst = true;

                    String productName=data.get("productName");
                    String storeId=data.get("storeId");
                    String qty=data.get("availableQty");
                    KwlReturnObject kwlobject = getObject(Store.class.getName(), storeId);
                    Store store = (Store) kwlobject.getEntityList().get(0);
                    Set<User> mgrSet =new HashSet(); 
                    mgrSet.addAll(store.getStoreManagerSet());
                    mgrSet.addAll(store.getStoreExecutiveSet());
                    String sendorInfo = sender.getEmailID() != null ? sender.getEmailID() :store.getCompany().getEmailID();
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    
                        for (User user : mgrSet) {
                            if (isfirst) {
                                emailIds += user.getEmailID();
                                isfirst = false;
                            } else {
                                emailIds += mailSeparator + user.getEmailID();
                            }

                        String subject = "Reorder Level Notification";
                        String htmlTextC = "";
                        htmlTextC += "<br/>Hi,<br/>";
                        htmlTextC += "<br/>Quantity for product <b>" + productName + "</b>  has gone below reorder level.";
                        htmlTextC += "<br/><b> Quantity :</b> " + qty + "</b><br/><b> Store :</b>     " + store.getFullName() + "</b>";
//                htmlTextC += "<br/><b>Store :</b>     "+store + "</b>";
//                        htmlTextC += "<br/><br/>This is an auto generated email. Do not reply.<br/>";
                        htmlTextC += "<br/><br/>Regards,<br/>";
                        htmlTextC += "<br/>ERP System<br/>";
                        htmlTextC += "<br/><br/>";
                        htmlTextC += "<br/>This is an auto generated email. Do not reply.<br/>";
                        String plainMsgC = "";
                        plainMsgC += "\nRegards,\n";
                        plainMsgC += "\nDeskera Financials\n";
                        plainMsgC += "\n\n";
                        plainMsgC += "\nThis is an auto generated email. Do not reply.\n";
                        SendMailHandler.postMail(emailIds.split(","), subject, htmlTextC, plainMsgC, sendorInfo, smtpConfigMap);
                    }
                }
            
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
     @Override
    public void sendSaveTransactionEmails(String documentNumber,String moduleName, String[] toEmailIds, String userName ,boolean isEditMail, String companyid) throws ServiceException {
        try {                 
            if (toEmailIds.length > 0) {
                String fromEmailId = Constants.ADMIN_EMAILID;
                String subject = "";
                String htmlMsg = "";
                String plainMsg = "";
                
                if (isEditMail) {
                    subject = "Document Updation: %s ";
                    subject = String.format(subject, moduleName);
                    htmlMsg = "<html><head></head>"
                            + "<body>"
                            + "<p>Hi,</p>"
                            + "<p>%s  <b>%s</b> has been updated against you by %s .</p>"
                            + "<p></p>"
                            + "<p>Thanks</p>"
                            + "<p>This is an auto generated email. Do not reply.</p><br>"
                            + "</body>"
                            + "</html>";
                    htmlMsg = String.format(htmlMsg, moduleName, documentNumber, userName);
                    plainMsg = "Hi,\n\n"
                            + "%s %s has has been updated against you by %s .\n\n"
                            + "Thanks\n\n"
                            + "This is an autogenarated email. Do not reply.\n";
                    plainMsg = String.format(plainMsg, moduleName, documentNumber, userName);
                } else {
                    subject = "Document Generation: %s ";
                    subject = String.format(subject, moduleName);
                    htmlMsg = "<html><head></head>"
                            + "<body>"
                            + "<p>Hi,</p>"
                            + "<p>%s  <b>%s</b> has been generated against you by %s .</p>"
                            + "<p></p>"
                            + "<p>Thanks</p>"
                            + "<p>This is an auto generated email. Do not reply.</p><br>"
                            + "</body>"
                            + "</html>";
                    htmlMsg = String.format(htmlMsg, moduleName, documentNumber, userName);
                    plainMsg = "Hi,\n\n"
                            + "%s %s has has been generated against you by %s.\n\n"
                            + "Thanks\n\n"
                            + "This is an autogenarated email. Do not reply.\n";
                    plainMsg = String.format(plainMsg, moduleName, documentNumber, userName);
                }

                Company company = (Company) get(Company.class, companyid);
                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                fromEmailId = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:profileHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                SendMailHandler.postMail(toEmailIds, subject, htmlMsg, plainMsg, fromEmailId, smtpConfigMap);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    public void sendSaveTransactionEmails(String documentNumber, String moduleName, String[] toEmailIds,String ccmailids, String userName, boolean isEditMail, String companyid) throws ServiceException {
        try {                 
            if (toEmailIds.length > 0) {
                String fromEmailId = Constants.ADMIN_EMAILID;
                String subject = "";
                String htmlMsg = "";
                String plainMsg = "";
                
                if (isEditMail) {
                    subject = "Document Updation: %s ";
                    subject = String.format(subject, moduleName);
                    htmlMsg = "<html><head></head>"
                            + "<body>"
                            + "<p>Hi,</p>"
                            + "<p>%s  <b>%s</b> has been updated against you by %s .</p>"
                            + "<p></p>"
                            + "<p>Thanks</p>"
                            + "<p>This is an auto generated email. Do not reply.</p><br>"
                            + "</body>"
                            + "</html>";
                    htmlMsg = String.format(htmlMsg, moduleName, documentNumber, userName);
                    plainMsg = "Hi,\n\n"
                            + "%s %s has has been updated against you by %s .\n\n"
                            + "Thanks\n\n"
                            + "This is an autogenarated email. Do not reply.\n";
                    plainMsg = String.format(plainMsg, moduleName, documentNumber, userName);
                } else {
                    subject = "Document Generation: %s ";
                    subject = String.format(subject, moduleName);
                    htmlMsg = "<html><head></head>"
                            + "<body>"
                            + "<p>Hi,</p>"
                            + "<p>%s  <b>%s</b> has been generated against you by %s .</p>"
                            + "<p></p>"
                            + "<p>Thanks</p>"
                            + "<p>This is an auto generated email. Do not reply.</p><br>"
                            + "</body>"
                            + "</html>";
                    htmlMsg = String.format(htmlMsg, moduleName, documentNumber, userName);
                    plainMsg = "Hi,\n\n"
                            + "%s %s has has been generated against you by %s.\n\n"
                            + "Thanks\n\n"
                            + "This is an autogenarated email. Do not reply.\n";
                    plainMsg = String.format(plainMsg, moduleName, documentNumber, userName);
                }
                
                Company company = (Company) get(Company.class, companyid);
                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                fromEmailId = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:profileHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                SendMailHandler.postMail(ccmailids,new String[0],toEmailIds, subject, htmlMsg, plainMsg, fromEmailId,new String[0], smtpConfigMap);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public String getTabularFormatHTMLForNotificationMail(List<String> headerItemsList, List rowDetailMapList){
        String finalTableHtmlText = "";
        try {
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            List finalData = new ArrayList();
            List headerItems = new ArrayList();
            headerItems.add("No.");
            for (String header : headerItemsList) {
                headerItems.add(header);
            }
            
            int sno = 1;
            if (rowDetailMapList != null && !rowDetailMapList.isEmpty()) {
                for (int i = 0; i < rowDetailMapList.size(); i++) {
                    Map datamap = (Map) rowDetailMapList.get(i);
                    List data = new ArrayList();
                    data.add(sno);
                    for (String header : headerItemsList) {
                        data.add(datamap.get(header)); 
                    }
                    finalData.add(data);
                    sno++;
                }
            }
            if (sno > 1) {
                for (Object header : headerItems) {
                    CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                    String a = header.toString();
                    headerprop.setAlign("left");
                    headerprop.setData(a);
                    if("Product Description".equalsIgnoreCase(a)){
                        headerprop.setWidth("200px");
                    }else if("No.".equalsIgnoreCase(a)){
                        headerprop.setWidth("20px");
                    }else{
                    headerprop.setWidth("50px");
                    }
                    headerlist.add(headerprop);
                }
                List finalProductList = new ArrayList();
                for (Object headerdata : finalData) {
                    ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                    List datalist = (List) headerdata;
                    for (Object hdata : datalist) {
                        CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                        prop.setAlign("left");
                        prop.setData(hdata.toString());
                        prodlist.add(prop);
                    }
                    finalProductList.add(prodlist);
                }
                String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                StringWriter writer = new StringWriter();
                VelocityEngine ve = new VelocityEngine();
                ve.init();
                VelocityContext context = new VelocityContext();
                context.put("tableHeader", headerlist);
                context.put("prodList", finalProductList);
                context.put("top", top);
                context.put("left", left);
                context.put("width", tablewidth);
                velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                String tablehtml = writer.toString();
                finalTableHtmlText = finalTableHtmlText.concat(tablehtml);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
//            throw ex;
        }
        return finalTableHtmlText;
    }
    
    public void sendTransactionEmails(String[] toEmailIds,String ccmailids,String subject,String htmlMsg,String plainMsg,String companyid) throws ServiceException {
        try {                 
            if (toEmailIds.length > 0) {
                String fromEmailId = Constants.ADMIN_EMAILID;

                Company company = (Company) get(Company.class, companyid);
                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                SendMailHandler.postMail(ccmailids,new String[0],toEmailIds, subject, htmlMsg, plainMsg, fromEmailId,new String[0], smtpConfigMap);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
 
     @Override
    public Boolean checkForMultiLevelApprovalRule(int level, String companyid, String amount, String userid,int moduleid) throws AccountingException, ServiceException, ScriptException {
        boolean validate = false;
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        int levelToCheck = level;
        List params = new ArrayList();
        params.add(levelToCheck);
        params.add(companyid);
        params.add(moduleid);
        List<Object[]> rules = new ArrayList();
        String query = "select id,level,rule from multilevelapprovalrule where level= ? and companyid = ? and moduleid = ?";
        try {
            rules = executeSQLQuery(query, params.toArray());
        } catch (ServiceException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Object[] row : rules) {
            String rule = row[2].toString();
            String ruleExpression = rule;
            rule = rule.replaceAll("[$$]+", amount);
            //if(!StringUtil.isNullOrEmpty(rule)) {
            if ((!StringUtil.isNullOrEmpty(rule) && Boolean.parseBoolean(engine.eval(rule).toString())) || StringUtil.isNullOrEmpty(rule)) { // rule valid so check for current user as approver 
                List user = new ArrayList();
                List ParamsNew = new ArrayList();
                ParamsNew.add(levelToCheck);
                ParamsNew.add(companyid);
                ParamsNew.add(ruleExpression);
                ParamsNew.add(moduleid);
                ParamsNew.add(userid);
//                String query1 = "select * from multilevelapprovalruletargetusers where ruleid in (select id from multilevelapprovalrule where level=? and companyid=? and rule=? and moduleid = ?) and userid=?";
                String query1 = " select multilevelapprovalruletargetusers.* from multilevelapprovalruletargetusers inner join multilevelapprovalrule on multilevelapprovalruletargetusers.ruleid=multilevelapprovalrule.id where multilevelapprovalrule.level=? and multilevelapprovalrule.companyid=? and multilevelapprovalrule.rule=? and multilevelapprovalrule.moduleid=? and multilevelapprovalruletargetusers.userid=?";
                try {
                    user = executeSQLQuery(query1, ParamsNew.toArray());
                } catch (ServiceException ex) {
                    Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (user.size() != 0) {
                    validate = true;
                    break;
                }
            }

            // }
        }
        return validate;
    }
     
    @Override
    public Boolean checkForMultiLevelApprovalRules(HashMap<String, Object> requestParams) throws AccountingException, ServiceException, ScriptException{
        boolean validate = false;
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        String companyid = "";
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
            companyid = requestParams.get("companyid").toString();
        }
        String userid = "";
        if (requestParams.containsKey("currentUser") && requestParams.get("currentUser") != null) {
            userid = requestParams.get("currentUser").toString();
        }
        int level = 0;
        if (requestParams.containsKey("level") && requestParams.get("level") != null) {
            level = Integer.parseInt(requestParams.get("level").toString());
        }
        String amount = "";
        if (requestParams.containsKey("totalAmount") && requestParams.get("totalAmount") != null) {
            amount = requestParams.get("totalAmount").toString();
        }
        boolean fromCreate = false;
        if (requestParams.containsKey("fromCreate") && requestParams.get("fromCreate") != null) {
            fromCreate = Boolean.parseBoolean(requestParams.get("fromCreate").toString());
        }
        double totalProfitMargin = 0;
        if (requestParams.containsKey("totalProfitMargin") && requestParams.get("totalProfitMargin") != null) {
            totalProfitMargin = Double.parseDouble(requestParams.get("totalProfitMargin").toString());
        }
        double totalProfitMarginPerc = 0;
        if (requestParams.containsKey("totalProfitMarginPerc") && requestParams.get("totalProfitMarginPerc") != null) {
            totalProfitMarginPerc = Double.parseDouble(requestParams.get("totalProfitMarginPerc").toString());
        }
        int moduleid = 0;
        if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null) {
            moduleid = Integer.parseInt(requestParams.get("moduleid").toString());
        }
        boolean isLimitExceeding = false;                    //ERM-396
        if (requestParams.containsKey("isLimitExceeding") && requestParams.get("isLimitExceeding") != null) {
            isLimitExceeding = Boolean.parseBoolean(requestParams.get("isLimitExceeding").toString());
        }
        JSONArray productDiscountMapList = null;
        if (requestParams.containsKey("productDiscountMapList") && requestParams.get("productDiscountMapList") != null) {
            try {
                productDiscountMapList = new JSONArray(requestParams.get("productDiscountMapList").toString());
            }  catch (Exception ex) {
                Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        int levelToCheck = level;
        List params = new ArrayList();
        params.add(levelToCheck);
        params.add(companyid);
        params.add(moduleid);
        List<Object[]> rules = new ArrayList();
        String query = "select id,level,rule,appliedupon, discountrule from multilevelapprovalrule where level= ? and companyid = ? and moduleid = ?";
        try {
            rules = executeSQLQuery(query, params.toArray());
        } catch (ServiceException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Object[] row : rules) {
            String rule = "";
            String ruleId = "";
            if (row[0] != null) {
                ruleId = row[0].toString();
            }
            if(row[2]!=null){
                rule = row[2].toString();
            }
            String discountRule = "";
            if(row[4]!=null){
                discountRule = row[4].toString();
            }
            boolean sendForApproval = false;
            int appliedUpon = Integer.parseInt(row[3].toString());
            String ruleExpression = rule;
            if(appliedUpon == 3){
                rule = rule.replaceAll("[$$]+", String.valueOf(totalProfitMargin));
            } else if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount) {
                if (productDiscountMapList != null) {
                    sendForApproval=AccountingManager.checkForProductAndProductDiscountRule(productDiscountMapList,appliedUpon,rule,discountRule);
                }
            } else if (appliedUpon == Constants.Specific_Products_Category) {
                /*
                 * Check If Rule is apply on product category from
                 * multiapproverule window
                 */
                if (productDiscountMapList != null) {
                    try {
                        for (int cnt = 0; cnt < productDiscountMapList.length(); cnt++) {
                            String productId = "";
                            List list = null;
                            JSONObject jObj = (JSONObject) productDiscountMapList.get(cnt);
                            productId = jObj.get("productId").toString();
                            String query1 = "select count(ID) from ProductCategoryMapping where productCategory.ID=? and productID.ID=?";
                            list = executeQuery(query1, new String[]{rule, productId});
                            if (list != null && list.size() > 0) {
                                Long count = (Long) list.get(0);
                                if (count > 0) {
                                    sendForApproval = true;
                                    break;
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }else if (appliedUpon == Constants.SO_CREDIT_LIMIT) {
                /**
                 * Check If Rule is apply on SO Credit limit category from
                 * multiapproverule window ERM-396 if yes then we need to check weather user has permission to approve or not.
                 */
                sendForApproval = true;
            } else {
                rule = rule.replaceAll("[$$]+", amount);
            }
            //if(!StringUtil.isNullOrEmpty(rule)) {
            if ((!StringUtil.isNullOrEmpty(rule) && appliedUpon != Constants.SO_CREDIT_LIMIT && appliedUpon != Constants.Specific_Products && appliedUpon != Constants.Specific_Products_Discount && appliedUpon !=Constants.Specific_Products_Category && Boolean.parseBoolean(engine.eval(rule).toString())) || StringUtil.isNullOrEmpty(rule) || sendForApproval) { // rule valid so check for current user as approver 
                List user = new ArrayList();
                List ParamsNew = new ArrayList();
                ParamsNew.add(levelToCheck);
                ParamsNew.add(companyid);
                ParamsNew.add(ruleExpression);
                ParamsNew.add(moduleid);
                ParamsNew.add(userid);
                ParamsNew.add(ruleId);
                /*
                 * Check for approval rule for the specific user.
                 * If approval rule present for user then user will be authorized to approve the document otherwise not authorized.
                 */
//                String query1 = "select * from multilevelapprovalruletargetusers where ruleid in (select id from multilevelapprovalrule where level=? and companyid=? and rule=? and moduleid = ?) and userid=?";
                String query1 = " select multilevelapprovalruletargetusers.* from multilevelapprovalruletargetusers inner join multilevelapprovalrule on multilevelapprovalruletargetusers.ruleid=multilevelapprovalrule.id where multilevelapprovalrule.level=? and multilevelapprovalrule.companyid=? and multilevelapprovalrule.rule=? and multilevelapprovalrule.moduleid=? and multilevelapprovalruletargetusers.userid=? and multilevelapprovalruletargetusers.ruleid=?";
                try {
                    user = executeSQLQuery(query1, ParamsNew.toArray());
                } catch (ServiceException ex) {
                    Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (user.size() != 0) {
                    validate = true;
                    break;
                }
            }

            // }
        }
        return validate;
    } 
 
    public String[] getApprovalUserList(HttpServletRequest request, String moduleName, int approvalLevel) {
        String[] emails = {};
        try {
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
            KwlReturnObject kmsg = null;
            requestParams1.clear();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams1.put("companyid", companyid);
            ArrayList filter_names = new ArrayList();
            ArrayList filter_params = new ArrayList();
            filter_names.add("u.company.companyID");
            filter_params.add(companyid);
            filter_names.add("u.deleteflag");
            filter_params.add(0);

            kmsg = profileHandlerDAOObj.getUserDetails(requestParams1, filter_names, filter_params);

//            String userName = sessionHandlerImpl.getUserFullName(request);            
            ArrayList<String> emailArray = new ArrayList<String>();
            Iterator ite = kmsg.getEntityList().iterator();

            while (ite.hasNext()) {
                User user = (User) ite.next();
                String userid = user.getUserID();
                JSONObject perms = new JSONObject();
                if (!permissionHandlerDAOObj.isSuperAdmin(userid, companyid)) {

                    kmsg = permissionHandlerDAOObj.getActivityFeature();
                    perms = permissionHandler.getAllPermissionJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());

                    requestParams1 = new HashMap<String, Object>();
                    requestParams1.put("userid", userid);

                    kmsg = permissionHandlerDAOObj.getUserPermission(requestParams1);
                    perms = permissionHandler.getRolePermissionJson(kmsg.getEntityList(), perms);
                    if (approvalLevel == 1) {
                        if (StringUtil.equal(moduleName, "Purchase Order")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "purchaseorderapprovelevelone"))) {
                                emailArray.add(user.getEmailID());
                            }
                        } else if (StringUtil.equal(moduleName, "Sales Order")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "salesorderapprovelevelone"))) {
                                emailArray.add(user.getEmailID());
                            }
                        } else if (StringUtil.equal(moduleName, "Vendor Invoice")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "vendorinvoiceapprovelevelone"))) {
                                emailArray.add(user.getEmailID());
                            }
                        } else if (StringUtil.equal(moduleName, "Customer Invoice")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "customerinvoiceapprovelevelone"))) {
                                emailArray.add(user.getEmailID());
                            }
                        }
                    } else if (approvalLevel == 2) {
                        if (StringUtil.equal(moduleName, "Purchase Order")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "purchaseorderapproveleveltwo"))) {
                                emailArray.add(user.getEmailID());
                            }
                        } else if (StringUtil.equal(moduleName, "Sales Order")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "salesorderapproveleveltwo"))) {
                                emailArray.add(user.getEmailID());
                            }
                        } else if (StringUtil.equal(moduleName, "Vendor Invoice")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "vendorinvoiceapproveleveltwo"))) {
                                emailArray.add(user.getEmailID());
                            }
                        } else if (StringUtil.equal(moduleName, "Customer Invoice")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "customerinvoiceapproveleveltwo"))) {
                                emailArray.add(user.getEmailID());
                            }
                        }
                    }
                }
            }
            emails = emailArray.toArray(emails);
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return emails;
    }

    public String[] getApprovalUserListJson(JSONObject jobj, String moduleName, int approvalLevel) {
        String[] emails = {};
        try {
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
            KwlReturnObject kmsg = null;
            requestParams1.clear();
            String companyid = jobj.getString(Constants.companyKey);
            requestParams1.put("companyid", companyid);
            ArrayList filter_names = new ArrayList();
            ArrayList filter_params = new ArrayList();
            filter_names.add("u.company.companyID");
            filter_params.add(companyid);
            filter_names.add("u.deleteflag");
            filter_params.add(0);

            kmsg = profileHandlerDAOObj.getUserDetails(requestParams1, filter_names, filter_params);

//            String userName = sessionHandlerImpl.getUserFullName(request);            
            ArrayList<String> emailArray = new ArrayList<String>();
            Iterator ite = kmsg.getEntityList().iterator();

            while (ite.hasNext()) {
                User user = (User) ite.next();
                String userid = user.getUserID();
                JSONObject perms = new JSONObject();
                if (!permissionHandlerDAOObj.isSuperAdmin(userid, companyid)) {

                    kmsg = permissionHandlerDAOObj.getActivityFeature();
                    perms = permissionHandler.getAllPermissionJson(kmsg.getEntityList(), null, kmsg.getRecordTotalCount());

                    requestParams1 = new HashMap<String, Object>();
                    requestParams1.put("userid", userid);

                    kmsg = permissionHandlerDAOObj.getUserPermission(requestParams1);
                    perms = permissionHandler.getRolePermissionJson(kmsg.getEntityList(), perms);
                    if (approvalLevel == 1) {
                        if (StringUtil.equal(moduleName, "Purchase Order")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "purchaseorderapprovelevelone"))) {
                                emailArray.add(user.getEmailID());
                            }
                        } else if (StringUtil.equal(moduleName, "Sales Order")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "salesorderapprovelevelone"))) {
                                emailArray.add(user.getEmailID());
                            }
                        } else if (StringUtil.equal(moduleName, "Vendor Invoice")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "vendorinvoiceapprovelevelone"))) {
                                emailArray.add(user.getEmailID());
                            }
                        } else if (StringUtil.equal(moduleName, "Customer Invoice")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "customerinvoiceapprovelevelone"))) {
                                emailArray.add(user.getEmailID());
                            }
                        }
                    } else if (approvalLevel == 2) {
                        if (StringUtil.equal(moduleName, "Purchase Order")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "purchaseorderapproveleveltwo"))) {
                                emailArray.add(user.getEmailID());
                            }
                        } else if (StringUtil.equal(moduleName, "Sales Order")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "salesorderapproveleveltwo"))) {
                                emailArray.add(user.getEmailID());
                            }
                        } else if (StringUtil.equal(moduleName, "Vendor Invoice")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "vendorinvoiceapproveleveltwo"))) {
                                emailArray.add(user.getEmailID());
                            }
                        } else if (StringUtil.equal(moduleName, "Customer Invoice")) {
                            if ((permissionHandler.isPermitted(perms, "approvals", "customerinvoiceapproveleveltwo"))) {
                                emailArray.add(user.getEmailID());
                            }
                        }
                    }
                }
            }
            emails = emailArray.toArray(emails);
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return emails;
    }
    
    @Override
    public KwlReturnObject getDuedateCustomerInvoiceInfoList() throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String SqlQuery = "select  customer.name as `name`,customeraddresses.billingEmail1 as email,"
                    + "convert_tz(invoice.dueDate,'+00:00',timezone.difference) as duedate,invoice.invoicenumber as invoicenumber,invoice.currency as currency,invoice.id as id,'false' as withoutinventory,"
                    + "company.emailid as emailid,company.companyname as companyname,company.companyid as companyID,company.currency as basecurrency,"
                    + "users.userid as userid,users.fname as fname,users.lname as lname,"
                    + "jedetail.amount as amount,"
                    + "account.`name` as accountName "
                    + "from customer "
                    + "inner join invoice on customer.id=invoice.customer "
                    + "left join customeraddresses on customer.customeraddresses=customeraddresses.id "
                    + "inner join company on company.companyid = customer.company "
                    + "left join users on users.userid=company.creator "
                    + "left join timezone on timezone.timezoneid=users.timeZone "                    
                    + "inner join jedetail on jedetail.id=invoice.centry "
                    + "inner join account on account.id = jedetail.account "
                    + "where DATE(convert_tz(invoice.dueDate,'+00:00',timezone.difference))=DATE(NOW()) and invoice.deleteflag=false and invoice.pendingapproval=0 ";
            list = executeSQLQuery(SqlQuery, null);
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getDuedateVendorInvoiceList(HashMap<String, Object> requestParams) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        List params = new ArrayList();
        try {
            String companyid = (String) requestParams.get("companyid");
            boolean isDueDateFilter = Boolean.parseBoolean(requestParams.get("isDueDate").toString());
            String query = "select goodsreceipt.id from goodsreceipt "
                    + " left join users on users.userid=goodsreceipt.createdby"
                    + " left join timezone on timezone.timezoneid=users.timeZone";

            if (!isDueDateFilter) {
                query += " inner join journalentry on journalentry.id= goodsreceipt.journalentry";
            }
            String condition = " where goodsreceipt.deleteflag='F' and goodsreceipt.cashtransaction='F' and ((goodsreceipt.isopeningbalenceinvoice=false and goodsreceipt.invoiceamountdue>0) or (goodsreceipt.isopeningbalenceinvoice=true and goodsreceipt.openingbalanceamountdue>0)) and goodsreceipt.company=?";
            params.add(companyid);

            String dateColumn = "journalentry.entrydate";
            if (isDueDateFilter) {
                dateColumn = "goodsreceipt.duedate";
            }
            condition += getRecurringMailSQLQuery(requestParams, params, dateColumn);
            String orderBy = "";
            if (isDueDateFilter) {
                orderBy += " order by goodsreceipt.duedate";
            } else {
                orderBy += " order by journalentry.entrydate";
            }
            query = query + condition + orderBy;
            list = executeSQLQuery(query, params.toArray());            
            if (list != null) {
                listSize = list.size();
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getDuedateVendorBillingInvoiceList(String companyId, String date) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String SqlQuery = "from BillingGoodsReceipt gr where gr.company.companyID=? and gr.deleted=false and gr.dueDate like '" + date + "%'";
            list = executeQuery(SqlQuery, new String[]{companyId});
            if (list != null) {
                listSize = list.size();
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getDuedateCustomerInvoiceList(HashMap<String, Object> requestParams) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        List params = new ArrayList();
        try {
            String companyid = (String) requestParams.get("companyid");
            boolean isDueDateFilter = Boolean.parseBoolean(requestParams.get("isDueDate").toString());
            String query = "select invoice.id from invoice"
                    + " left join users on users.userid=invoice.createdby"
                    + " left join timezone on timezone.timezoneid=users.timeZone";
                    
            if (!isDueDateFilter) {
                query += " inner join journalentry on journalentry.id= invoice.journalentry";
            }
            String condition = " where invoice.deleteflag='F' and invoice.cashtransaction='F' and ((invoice.isopeningbalenceinvoice=false and invoice.invoiceamountdue>0) or (invoice.isopeningbalenceinvoice=true and invoice.openingbalanceamountdue>0)) and invoice.company=?";
            params.add(companyid);

            String dateColumn = "journalentry.entrydate";
            if (isDueDateFilter) {
                dateColumn = "invoice.duedate";
            }
            condition += getRecurringMailSQLQuery(requestParams, params, dateColumn);
            String orderBy = "";
            if (isDueDateFilter) {
                orderBy += " order by invoice.duedate";
            } else {
                orderBy += " order by journalentry.entrydate";
            }
            query = query + condition + orderBy;
            list = executeSQLQuery(query, params.toArray());
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    public String getRecurringMailSQLQuery(HashMap<String, Object> requestParams, List params, String dateColumn) {
        String date = (String) requestParams.get("dbDueDate");
        boolean isRecurring = false;
        if (requestParams.containsKey("isRecurring")) {
            isRecurring = Boolean.parseBoolean(requestParams.get("isRecurring").toString());
        }
        String condition = " and convert_tz(" + dateColumn + ",'+00:00', timezone.difference)<= now()";
        if (isRecurring) { // case only for recurring rules
            int repeatTime = Integer.parseInt(requestParams.get("repeatTime").toString());//Repeat After every time
            int repeatTimeType = Integer.parseInt(requestParams.get("repeatTimeType").toString());// combo value day/month/week
            if (repeatTimeType == Constants.RECURRING_DAY || repeatTimeType == Constants.RECURRING_WEEK) { //for days and week
                condition += " and ((convert_tz(" + dateColumn + ",'+00:00', timezone.difference) like '" + date + "%') OR "
                        + " (DATEDIFF(convert_tz(" + dateColumn + ",'+00:00', timezone.difference),now()) MOD ? = 0)) ";
                if (repeatTimeType == Constants.RECURRING_DAY) {
                    params.add(repeatTime); //days
                } else {
                    params.add(7 * repeatTime); //weeks
                }
            } else { //for months
                condition += " and ((convert_tz(" + dateColumn + ",'+00:00', timezone.difference) like '" + date + "%') OR"
                        + " (PERIOD_DIFF(EXTRACT(YEAR_MONTH FROM convert_tz(" + dateColumn + ",'+00:00', timezone.difference)),EXTRACT( YEAR_MONTH FROM NOW())) MOD ?=0 and  DAYOFMONTH(convert_tz(" + dateColumn + ",'+00:00', timezone.difference))=DAYOFMONTH(NOW())))";
                params.add(repeatTime);
            }
        } else {
            condition += " and  "+dateColumn+" like '" + date + "%'";
        }
        return condition;
    }

    @Override
    public KwlReturnObject getDuedateCustomerBillingInvoiceList(String companyId, String date) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from BillingInvoice inv where inv.company.companyID=? and inv.deleted=false and inv.dueDate like '" + date + "%'";
            list = executeQuery(query, new String[]{companyId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getSOdateSalesOrderList(String companyId, String date) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
//            String query = "from SalesOrder so where so.company.companyID=? and so.deleted=false and convert_tz(so.orderDate,'+00:00', so.createdby.timeZone.difference)  like '" + date + "%'";
            String query = "from SalesOrder so where so.company.companyID=? and so.deleted=false and so.orderDate  like '" + date + "%'";
            list = executeQuery(query, new String[]{companyId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getSOdateBillingSalesOrderList(String companyId, String date) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from BillingSalesOrder bso where bso.company.companyID=? and bso.deleted=false and bso.orderDate like '" + date + "%'";
            list = executeQuery(query, new String[]{companyId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getPOdatePurchaseOrderList(String companyId, String date) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from PurchaseOrder po where po.company.companyID=? and po.deleted=false and po.orderDate like '" + date + "%'";
            list = executeQuery(query, new String[]{companyId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getPOdateBillingPurchaseOrderList(String companyId, String date) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from BillingPurchaseOrder bpo where bpo.company.companyID=? and bpo.deleted=false and bpo.orderDate like '" + date + "%'";
            list = executeQuery(query, new String[]{companyId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getDOdateDeliveryOrderList(String companyId, String date) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from DeliveryOrder do where do.company.companyID=? and do.deleted=false and do.orderDate like '" + date + "%'";
            list = executeQuery(query, new String[]{companyId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getProductExpdateDeliveryOrderList(String companyId, String date) throws ServiceException {
        int listSize = 0;
        List list = null;
        try {
            String query = "select do.id,do.donumber,batchserial.exptodate,prod.name from deliveryorder as do "
                    + "inner join dodetails as dod on do.id=dod.deliveryorder "
                    + "inner join product as prod on dod.product=prod.id "
                    + "inner join productbatch on dod.batch=productbatch.id  "
                    + "inner join batchserial on productbatch.id= batchserial.batch and do.company=? and batchserial.exptodate like '" + date + "%'";
            list = executeSQLQuery(query, new String[]{companyId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getProductExpdateGoodsReceiptOrderList(String companyId, String date) throws ServiceException {
        int listSize = 0;
        List list = null;
        try {
            String query = "select gro.id,gro.gronumber,batchserial.exptodate,prod.name from grorder as gro "
                    + "inner join grodetails as grod on gro.id=grod.grorder "
                    + "inner join product as prod on grod.product=prod.id "
                    + "inner join productbatch on grod.batch=productbatch.id  "
                    + "inner join batchserial on productbatch.id= batchserial.batch and gro.company=? and batchserial.exptodate like '" + date + "%'";
            list = executeSQLQuery(query, new String[]{companyId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getGRODateGoodsReceiptOrderList(String companyId, String date) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from GoodsReceiptOrder gro where gro.company.companyID=? and gro.deleted=false and gro.orderDate like '" + date + "%'";
            list = executeQuery(query, new String[]{companyId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getSRDateSalesReturnList(String companyId, String date) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from SalesReturn sr where sr.company.companyID=? and sr.deleted=false and sr.orderDate like '" + date + "%'";
            list = executeQuery(query, new String[]{companyId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getPRDatePurchaseReturnList(String companyId, String date) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from PurchaseReturn pr where pr.company.companyID=? and pr.deleted=false and pr.orderDate like '" + date + "%'";
            list = executeQuery(query, new String[]{companyId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getCustomerCreationDateCustomerList(String companyId, String date) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from Customer c where c.company.companyID=? and c.createdOn like '" + date + "%'";
            list = executeQuery(query, new String[]{companyId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getVendorCreationDateVendorList(String companyId, String date) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from Vendor v where v.company.companyID=? and v.createdOn like '" + date + "%'";
            list = executeQuery(query, new String[]{companyId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getCompanyList() throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from Company";
            list = executeQuery(query, null);
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }
    
    @Override
    public List getPOSCompanyList() throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "Select c.companyid,c.subdomain from company c inner join extracompanypreferences ecf on(c.companyid=ecf.id) where ecf.isPOSIntegration='T' ";
            list = executeSQLQuery(query);

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return list;
        }
    }

    @Override
    public KwlReturnObject getFieldParamsForProject(String companyid, String projectid) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        String searchString = "";
        try {
            String mysqlQuery = " select colnum, id from fieldparams where isforproject = 1 and companyid = ?";
            //Fetched all isProject fields
            list = executeSQLQuery(mysqlQuery, new Object[]{companyid});
            if (list != null) {
                listSize = list.size();
            }
        } catch (Exception e) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }
    
    public KwlReturnObject getFieldParamsForLinkProjectToInvoice(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
        List list = null;
        try {
            ArrayList params = new ArrayList();
            String hql = "";
            if((int)requestParams.get("moduleid")!=Constants.Acc_Product_Master_ModuleId){
                hql = "select id,colnum,fieldlabel,fieldname,fieldtype,refcolnum  from fieldparams where moduleid=? and companyid=? and customcolumn=1";
            }else{
            hql = "select id,colnum,fieldlabel,fieldname,fieldtype,refcolnum  from fieldparams where moduleid=? and companyid=?";
            }
            params.add(requestParams.get("moduleid"));
            params.add(requestParams.get("companyid"));
            list = executeSQLQuery(hql, params.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    
    @Override
    public KwlReturnObject getReceiptDetails(String companyId,String receiptId) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from ReceiptAdvanceDetail nr where nr.company.companyID=? and receipt.ID=?";
            list = executeQuery(query, new String[]{companyId,receiptId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }
    
    
    @Override
    public KwlReturnObject getFieldParamsForTask(String companyid) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String mysqlQuery = " select colnum, id from fieldparams where isfortask = 1 and companyid = ?";
            //Fetched all isfortask fields
            list = executeSQLQuery(mysqlQuery, new Object[]{companyid});
            if (list != null) {
                listSize = list.size();
            }
        } catch (Exception e) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getFieldComboDataForTask(String fieldId, String projectid, String taskid) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String mysqlQueryCmbData = " select id from fieldcombodata where fieldid = ? and projectid = ? and taskid = ? ";

            list = executeSQLQuery(mysqlQueryCmbData, new Object[]{fieldId, projectid, taskid});
            if (list != null) {
                listSize = list.size();
            }
        } catch (Exception e) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getFieldComboDataForProject(String fieldId, String projectId) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        String searchString = "";
        try {
            String mysqlQuery = " select id from fieldcombodata where fieldid = ? and projectid = ?";
            //Fetched all isProject fields
            list = executeSQLQuery(mysqlQuery, new Object[]{fieldId, projectId});
            if (list != null) {
                listSize = list.size();
            }
        } catch (Exception e) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getNotifications(String companyId) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {/*
              Fieldid 20 is for email Button from report template
              Added as irrelevant mail were getting sent when crone was hit
            */
            String query = "from NotificationRules nr where nr.company.companyID=? and nr.fieldid != 20 ";
            list = executeQuery(query, new String[]{companyId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getDueJournalEntryList(String companyId, Long dbDuedate1, Long dbDuedate2, String columnname) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "SELECT journalentry.entrydate AS entrydate, journalentry.entryno as entrynumber,currency.name as currencyname from journalentry "
                    + "INNER JOIN currency ON currency.currencyid=journalentry.currency "
                    + "INNER JOIN accjecustomdata ON accjecustomdata.journalentryId=journalentry.id "
                    + "WHERE journalentry.deleteflag ='F' and journalentry.company=? and  accjecustomdata." + columnname + ">=? AND accjecustomdata." + columnname + "<?";

            list = executeSQLQuery(query, new Object[]{companyId, dbDuedate1, dbDuedate2});
            if (list != null) {
                listSize = list.size();
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, list, listSize);
    }

    @Override
    public KwlReturnObject getDuedateCustomefield(String companyId) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String SqlQuery = "SELECT fieldparams.id,fieldparams.colnum,fieldparams.refcolnum "
                    + "from fieldparams "
                    + "where fieldparams.fieldlabel LIKE '%Due%' and fieldparams.fieldtype=? and  "
                    + "fieldparams.moduleid=? and fieldparams.companyid= ?";
            list = executeSQLQuery(SqlQuery, new Object[]{"3", "24", companyId});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getUserDetailObj(String[] userids) throws ServiceException {
        List list = new ArrayList();
        try {
            int size = 0;
            for (int i = 0; i < userids.length; i++) {
                if (!StringUtil.isNullOrEmpty(userids[i])) {
                    String userid = userids[i];
                    User ur = (User) get(User.class, userid);
                    list.add(ur);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveAddressDetail(Map<String, Object> addressParams, String companyid) throws ServiceException {
        List list = new ArrayList();
        try {
            BillingShippingAddresses bsa = new BillingShippingAddresses();
            if (addressParams.containsKey("id")) {
                if (!StringUtil.isNullOrEmpty((String) addressParams.get("id"))) {
                    bsa = (BillingShippingAddresses) get(BillingShippingAddresses.class, (String) addressParams.get("id"));
                }
            }

            if (addressParams.containsKey(Constants.BILLING_ADDRESS)) {
                bsa.setBillingAddress((String) addressParams.get(Constants.BILLING_ADDRESS));
            }

            if (addressParams.containsKey(Constants.BILLING_COUNTRY)) {
                bsa.setBillingCountry((String) addressParams.get(Constants.BILLING_COUNTRY));
            }

            if (addressParams.containsKey(Constants.BILLING_STATE)) {
                bsa.setBillingState((String) addressParams.get(Constants.BILLING_STATE));
            }

            if (addressParams.containsKey(Constants.BILLING_COUNTY)) {
                bsa.setBillingCounty((String) addressParams.get(Constants.BILLING_COUNTY));
            }
            
            if (addressParams.containsKey(Constants.BILLING_CITY)) {
                bsa.setBillingCity((String) addressParams.get(Constants.BILLING_CITY));
            }

            if (addressParams.containsKey(Constants.BILLING_POSTAL)) {
                bsa.setBillingPostal((String) addressParams.get(Constants.BILLING_POSTAL));
            }

            if (addressParams.containsKey(Constants.BILLING_FAX)) {
                bsa.setBillingFax((String) addressParams.get(Constants.BILLING_FAX));
            }
            if (addressParams.containsKey(Constants.BILLING_EMAIL)) {
                bsa.setBillingEmail((String) addressParams.get(Constants.BILLING_EMAIL));
            }

            if (addressParams.containsKey(Constants.BILLING_MOBILE)) {
                bsa.setBillingMobile((String) addressParams.get(Constants.BILLING_MOBILE));
            }
            if (addressParams.containsKey(Constants.BILLING_PHONE)) {
                bsa.setBillingPhone((String) addressParams.get(Constants.BILLING_PHONE));
            }

            if (addressParams.containsKey(Constants.BILLING_CONTACT_PERSON)) {
                bsa.setBillingContactPerson((String) addressParams.get(Constants.BILLING_CONTACT_PERSON));
            }
            if (addressParams.containsKey(Constants.BILLING_RECIPIENT_NAME)) {
                bsa.setBillingRecipientName((String) addressParams.get(Constants.BILLING_RECIPIENT_NAME));
            }
            if (addressParams.containsKey(Constants.BILLING_CONTACT_PERSON_NUMBER)) {
                bsa.setBillingContactPersonNumber((String) addressParams.get(Constants.BILLING_CONTACT_PERSON_NUMBER));
            }

            if (addressParams.containsKey(Constants.BILLING_CONTACT_PERSON_DESIGNATION)) {
                bsa.setBillingContactPersonDesignation((String) addressParams.get(Constants.BILLING_CONTACT_PERSON_DESIGNATION));
            }
            if (addressParams.containsKey(Constants.BILLING_WEBSITE)) {
                bsa.setBillingWebsite((String) addressParams.get(Constants.BILLING_WEBSITE));
            }
            if (addressParams.containsKey(Constants.BILLING_ADDRESS_TYPE)) {
                bsa.setBillingAddressType((String) addressParams.get(Constants.BILLING_ADDRESS_TYPE));
            }
            
                        
            if (addressParams.containsKey(Constants.DropShip_BILLING_ADDRESS)) {
                bsa.setVendorBillingAddress((String) addressParams.get(Constants.DropShip_BILLING_ADDRESS));
            }

            if (addressParams.containsKey(Constants.DropShip_BILLING_COUNTRY)) {
                bsa.setVendorBillingCountry((String) addressParams.get(Constants.DropShip_BILLING_COUNTRY));
            }

            if (addressParams.containsKey(Constants.DropShip_BILLING_STATE)) {
                bsa.setVendorBillingState((String) addressParams.get(Constants.DropShip_BILLING_STATE));
            }

            if (addressParams.containsKey(Constants.DropShip_BILLING_COUNTY)) {
                bsa.setVendorBillingCounty((String) addressParams.get(Constants.DropShip_BILLING_COUNTY));
            }
            
            if (addressParams.containsKey(Constants.DropShip_BILLING_CITY)) {
                bsa.setVendorBillingCity((String) addressParams.get(Constants.DropShip_BILLING_CITY));
            }

            if (addressParams.containsKey(Constants.DropShip_BILLING_POSTAL)) {
                bsa.setVendorBillingPostal((String) addressParams.get(Constants.DropShip_BILLING_POSTAL));
            }

            if (addressParams.containsKey(Constants.DropShip_BILLING_FAX)) {
                bsa.setVendorBillingFax((String) addressParams.get(Constants.DropShip_BILLING_FAX));
            }
            if (addressParams.containsKey(Constants.DropShip_BILLING_EMAIL)) {
                bsa.setVendorBillingEmail((String) addressParams.get(Constants.DropShip_BILLING_EMAIL));
            }

            if (addressParams.containsKey(Constants.DropShip_BILLING_MOBILE)) {
                bsa.setVendorBillingMobile((String) addressParams.get(Constants.DropShip_BILLING_MOBILE));
            }
            if (addressParams.containsKey(Constants.DropShip_BILLING_PHONE)) {
                bsa.setVendorBillingPhone((String) addressParams.get(Constants.DropShip_BILLING_PHONE));
            }

            if (addressParams.containsKey(Constants.DropShip_BILLING_CONTACT_PERSON)) {
                bsa.setVendorBillingContactPerson((String) addressParams.get(Constants.DropShip_BILLING_CONTACT_PERSON));
            }
            if (addressParams.containsKey(Constants.DropShip_BILLING_RECIPIENT_NAME)) {
                bsa.setVendorBillingRecipientName((String) addressParams.get(Constants.DropShip_BILLING_RECIPIENT_NAME));
            }
            if (addressParams.containsKey(Constants.DropShip_BILLING_CONTACT_PERSON_NUMBER)) {
                bsa.setVendorBillingContactPersonNumber((String) addressParams.get(Constants.DropShip_BILLING_CONTACT_PERSON_NUMBER));
            }

            if (addressParams.containsKey(Constants.DropShip_BILLING_CONTACT_PERSON_DESIGNATION)) {
                bsa.setVendorBillingContactPersonDesignation((String) addressParams.get(Constants.DropShip_BILLING_CONTACT_PERSON_DESIGNATION));
            }
            if (addressParams.containsKey(Constants.DropShip_BILLING_WEBSITE)) {
                bsa.setVendorBillingWebsite((String) addressParams.get(Constants.DropShip_BILLING_WEBSITE));
            }
            if (addressParams.containsKey(Constants.DropShip_BILLING_ADDRESS_TYPE)) {
                bsa.setVendorBillingAddressType((String) addressParams.get(Constants.DropShip_BILLING_ADDRESS_TYPE));
            }
            /**
             * If "Show Vendor Address in purchase document is false and India country 
             * then save Vendor Billing Address in separate fields
             */
            if (!StringUtil.isNullOrEmpty(companyid)) {
                Company company = (Company) get(Company.class, companyid);
                if (company != null && company.getCountry() != null && !StringUtil.isNullOrEmpty(company.getCountry().getID())
                        && company.getCountry().getID().equalsIgnoreCase(Constants.INDIA_COUNTRYID)) {
                    KwlReturnObject kwlExtraCompanyPref = getObject(ExtraCompanyPreferences.class.getName(), companyid);
                    ExtraCompanyPreferences extraCompanyPref = null;
                    if (kwlExtraCompanyPref != null && kwlExtraCompanyPref.getEntityList() != null && !kwlExtraCompanyPref.getEntityList().isEmpty()) {
                        extraCompanyPref = (ExtraCompanyPreferences) kwlExtraCompanyPref.getEntityList().get(0);
                    }
                    if (extraCompanyPref != null && !extraCompanyPref.isIsAddressFromVendorMaster()) {
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_ADDRESS) && addressParams.get(Constants.VENDOR_BILLING_ADDRESS) != null) {
                            bsa.setVendorBillingAddressForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_ADDRESS));
                        }
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_COUNTRY) && addressParams.get(Constants.VENDOR_BILLING_COUNTRY) != null) {
                            bsa.setVendorBillingCountryForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_COUNTRY));
                        }
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_STATE) && addressParams.get(Constants.VENDOR_BILLING_STATE) != null) {
                            bsa.setVendorBillingStateForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_STATE));
                        }
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_COUNTY) && addressParams.get(Constants.VENDOR_BILLING_COUNTY) != null) {
                            bsa.setVendorBillingCountyForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_COUNTY));
                        }
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_CITY) && addressParams.get(Constants.VENDOR_BILLING_CITY) != null) {
                            bsa.setVendorBillingCityForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_CITY));
                        }
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_POSTAL) && addressParams.get(Constants.VENDOR_BILLING_POSTAL) != null) {
                            bsa.setVendorBillingPostalForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_POSTAL));
                        }
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_FAX) && addressParams.get(Constants.VENDOR_BILLING_FAX) != null) {
                            bsa.setVendorBillingFaxForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_FAX));
                        }
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_EMAIL) && addressParams.get(Constants.VENDOR_BILLING_EMAIL) != null) {
                            bsa.setVendorBillingEmailForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_EMAIL));
                        }
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_MOBILE) && addressParams.get(Constants.VENDOR_BILLING_MOBILE) != null) {
                            bsa.setVendorBillingMobileForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_MOBILE));
                        }
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_PHONE) && addressParams.get(Constants.VENDOR_BILLING_PHONE) != null) {
                            bsa.setVendorBillingPhoneForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_PHONE));
                        }
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_CONTACT_PERSON) && addressParams.get(Constants.VENDOR_BILLING_CONTACT_PERSON) != null) {
                            bsa.setVendorBillingContactPersonForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_CONTACT_PERSON));
                        }
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_RECIPIENT_NAME) && addressParams.get(Constants.VENDOR_BILLING_RECIPIENT_NAME) != null) {
                            bsa.setVendorBillingRecipientNameForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_RECIPIENT_NAME));
                        }
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_CONTACT_PERSON_NUMBER) && addressParams.get(Constants.VENDOR_BILLING_CONTACT_PERSON_NUMBER) != null) {
                            bsa.setVendorBillingContactPersonNumberForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_CONTACT_PERSON_NUMBER));
                        }
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_CONTACT_PERSON_DESIGNATION) && addressParams.get(Constants.VENDOR_BILLING_CONTACT_PERSON_DESIGNATION) != null) {
                            bsa.setVendorBillingContactPersonDesignationForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_CONTACT_PERSON_DESIGNATION));
                        }
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_WEBSITE) && addressParams.get(Constants.VENDOR_BILLING_WEBSITE) != null) {
                            bsa.setVendorBillingWebsiteForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_WEBSITE));
                        }
                        if (addressParams.containsKey(Constants.VENDOR_BILLING_ADDRESS_TYPE) && addressParams.get(Constants.VENDOR_BILLING_ADDRESS_TYPE) != null) {
                            bsa.setVendorBillingAddressTypeForINDIA((String) addressParams.get(Constants.VENDOR_BILLING_ADDRESS_TYPE));
                        }
                    }
                }
            }
            

            if (addressParams.containsKey(Constants.SHIPPING_ADDRESS)) {
                bsa.setShippingAddress((String) addressParams.get(Constants.SHIPPING_ADDRESS));
            }
            if (addressParams.containsKey(Constants.SHIPPING_COUNTRY)) {
                bsa.setShippingCountry((String) addressParams.get(Constants.SHIPPING_COUNTRY));
            }

            if (addressParams.containsKey(Constants.SHIPPING_STATE)) {
                bsa.setShippingState((String) addressParams.get(Constants.SHIPPING_STATE));
            }
            
            if (addressParams.containsKey(Constants.SHIPPING_COUNTY)) {
                bsa.setShippingCounty((String) addressParams.get(Constants.SHIPPING_COUNTY));
            }
            
            if (addressParams.containsKey(Constants.SHIPPING_CITY)) {
                bsa.setShippingCity((String) addressParams.get(Constants.SHIPPING_CITY));
            }

            if (addressParams.containsKey(Constants.SHIPPING_MOBILE)) {
                bsa.setShippingMobile((String) addressParams.get(Constants.SHIPPING_MOBILE));
            }
            if (addressParams.containsKey(Constants.SHIPPING_PHONE)) {
                bsa.setShippingPhone((String) addressParams.get(Constants.SHIPPING_PHONE));
            }

            if (addressParams.containsKey(Constants.SHIPPING_EMAIL)) {
                bsa.setShippingEmail((String) addressParams.get(Constants.SHIPPING_EMAIL));
            }
            if (addressParams.containsKey(Constants.SHIPPING_FAX)) {
                bsa.setShippingFax((String) addressParams.get(Constants.SHIPPING_FAX));
            }

            if (addressParams.containsKey(Constants.SHIPPING_POSTAL)) {
                bsa.setShippingPostal((String) addressParams.get(Constants.SHIPPING_POSTAL));
            }

            if (addressParams.containsKey(Constants.SHIPPING_CONTACT_PERSON)) {
                bsa.setShippingContactPerson((String) addressParams.get(Constants.SHIPPING_CONTACT_PERSON));
            }
            
            if (addressParams.containsKey(Constants.SHIPPING_RECIPIENT_NAME)) {
                bsa.setShippingRecipientName((String) addressParams.get(Constants.SHIPPING_RECIPIENT_NAME));
            }

            if (addressParams.containsKey(Constants.SHIPPING_CONTACT_PERSON_NUMBER)) {
                bsa.setShippingContactPersonNumber((String) addressParams.get(Constants.SHIPPING_CONTACT_PERSON_NUMBER));
            }
            if (addressParams.containsKey(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION)) {
                bsa.setShippingContactPersonDesignation((String) addressParams.get(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION));
            }
            if (addressParams.containsKey(Constants.SHIPPING_WEBSITE)) {
                bsa.setShippingWebsite((String) addressParams.get(Constants.SHIPPING_WEBSITE));
            }
            if (addressParams.containsKey(Constants.SHIPPING_ROUTE)) {
                bsa.setShippingRoute((String) addressParams.get(Constants.SHIPPING_ROUTE));
            }
            
            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_ADDRESS)) {
                bsa.setVendcustShippingAddress((String) addressParams.get(Constants.VENDCUST_SHIPPING_ADDRESS));
            }
            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_STATE)) {
                bsa.setVendcustShippingState((String) addressParams.get(Constants.VENDCUST_SHIPPING_STATE));
            }
            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_COUNTRY)) {
                bsa.setVendcustShippingCountry((String) addressParams.get(Constants.VENDCUST_SHIPPING_COUNTRY));
            }

            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_COUNTY)) {
                bsa.setVendcustShippingCounty((String) addressParams.get(Constants.VENDCUST_SHIPPING_COUNTY));
            }
            
            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_CITY)) {
                bsa.setVendcustShippingCity((String) addressParams.get(Constants.VENDCUST_SHIPPING_CITY));
            }
            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_EMAIL)) {
                bsa.setVendcustShippingEmail((String) addressParams.get(Constants.VENDCUST_SHIPPING_EMAIL));
            }

            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_FAX)) {
                bsa.setVendcustShippingFax((String) addressParams.get(Constants.VENDCUST_SHIPPING_FAX));
            }
            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_POSTAL)) {
                bsa.setVendcustShippingPostal((String) addressParams.get(Constants.VENDCUST_SHIPPING_POSTAL));
            }

            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_MOBILE)) {
                bsa.setVendcustShippingMobile((String) addressParams.get(Constants.VENDCUST_SHIPPING_MOBILE));
            }
            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_PHONE)) {
                bsa.setVendcustShippingPhone((String) addressParams.get(Constants.VENDCUST_SHIPPING_PHONE));
            }

            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_RECIPIENT_NAME)) {
                bsa.setVendcustShippingRecipientName((String) addressParams.get(Constants.VENDCUST_SHIPPING_RECIPIENT_NAME));
            }

            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_NUMBER)) {
                bsa.setVendcustShippingContactPersonNumber((String) addressParams.get(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_NUMBER));
            }
            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_DESIGNATION)) {
                bsa.setVendcustShippingContactPersonDesignation((String) addressParams.get(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_DESIGNATION));
            }
            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_WEBSITE)) {
                bsa.setVendcustShippingWebsite((String) addressParams.get(Constants.VENDCUST_SHIPPING_WEBSITE));
            }
            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_CONTACT_PERSON)) {
                bsa.setVendcustShippingContactPerson((String) addressParams.get(Constants.VENDCUST_SHIPPING_CONTACT_PERSON));
            }
            
            if (addressParams.containsKey(Constants.VENDCUST_SHIPPING_ADDRESS_TYPE)) {
                bsa.setVendcustShippingAddressType((String) addressParams.get(Constants.VENDCUST_SHIPPING_ADDRESS_TYPE));
            }
            
            if (addressParams.containsKey(Constants.SHIPPING_ADDRESS_TYPE)) {
                bsa.setShippingAddressType((String) addressParams.get(Constants.SHIPPING_ADDRESS_TYPE));
            }
            /*
             Put Customer Shipping Addres related parametrs in Map for save details
            */
            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_ADDRESS)) {
                bsa.setCustomerShippingAddress((String) addressParams.get(Constants.CUSTOMER_SHIPPING_ADDRESS));
            }
            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_STATE)) {
                bsa.setCustomerShippingState((String) addressParams.get(Constants.CUSTOMER_SHIPPING_STATE));
            }
            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_COUNTRY)) {
                bsa.setCustomerShippingCountry((String) addressParams.get(Constants.CUSTOMER_SHIPPING_COUNTRY));
            }

            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_COUNTY)) {
                bsa.setCustomerShippingCounty((String) addressParams.get(Constants.CUSTOMER_SHIPPING_COUNTY));
            }
            
            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_CITY)) {
                bsa.setCustomerShippingCity((String) addressParams.get(Constants.CUSTOMER_SHIPPING_CITY));
            }
            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_EMAIL)) {
                bsa.setCustomerShippingEmail((String) addressParams.get(Constants.CUSTOMER_SHIPPING_EMAIL));
            }

            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_FAX)) {
                bsa.setCustomerShippingFax((String) addressParams.get(Constants.CUSTOMER_SHIPPING_FAX));
            }
            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_POSTAL)) {
                bsa.setCustomerShippingPostal((String) addressParams.get(Constants.CUSTOMER_SHIPPING_POSTAL));
            }

            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_MOBILE)) {
                bsa.setCustomerShippingMobile((String) addressParams.get(Constants.CUSTOMER_SHIPPING_MOBILE));
            }
            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_PHONE)) {
                bsa.setCustomerShippingPhone((String) addressParams.get(Constants.CUSTOMER_SHIPPING_PHONE));
            }

            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_RECIPIENT_NAME)) {
                bsa.setCustomerShippingRecipientName((String) addressParams.get(Constants.CUSTOMER_SHIPPING_RECIPIENT_NAME));
            }

            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_NUMBER)) {
                bsa.setCustomerShippingContactPersonNumber((String) addressParams.get(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_NUMBER));
            }
            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_DESIGNATION)) {
                bsa.setCustomerShippingContactPersonDesignation((String) addressParams.get(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_DESIGNATION));
            }
            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_WEBSITE)) {
                bsa.setCustomerShippingWebsite((String) addressParams.get(Constants.CUSTOMER_SHIPPING_WEBSITE));
            }
            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON)) {
                bsa.setCustomerShippingContactPerson((String) addressParams.get(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON));
            }
            
            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_ADDRESS_TYPE)) {
                bsa.setCustomerShippingAddressType((String) addressParams.get(Constants.CUSTOMER_SHIPPING_ADDRESS_TYPE));
            }
            
//            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_ADDRESS_TYPE)) {
//                bsa.setShippingAddressType((String) addressParams.get(Constants.CUSTOMER_SHIPPING_ADDRESS_TYPE));
//            }
            if (addressParams.containsKey(Constants.CUSTOMER_SHIPPING_ROUTE)) {
                bsa.setCustomerShippingRoute((String) addressParams.get(Constants.CUSTOMER_SHIPPING_ROUTE));
            }

            if (!StringUtil.isNullOrEmpty(companyid)) {
                Company company = (Company) get(Company.class, companyid);
                bsa.setCompany(company);
            }
            saveOrUpdate(bsa);
            list.add(bsa);

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "Address field added successfully.", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveVendorAddressesDetails(HashMap<String, Object> addressParams, String companyid) throws ServiceException {
        List list = new ArrayList();
        try {
           VendorAddressDetails addresses = new VendorAddressDetails();
           if(addressParams.containsKey("addressid")){
               String addressid=(String)addressParams.get("addressid");
               if(!StringUtil.isNullOrEmpty(addressid)){
                   addresses = (VendorAddressDetails) get(VendorAddressDetails.class, addressid);
               }           
           }
           if(addressParams.containsKey("aliasName")){
               addresses.setAliasName((String)addressParams.get("aliasName"));
           }
           if(addressParams.containsKey("address")){
               addresses.setAddress((String)addressParams.get("address"));
           }
           if(addressParams.containsKey("county")){
               addresses.setCounty((String)addressParams.get("county"));
           }
           if(addressParams.containsKey("city")){
               addresses.setCity((String)addressParams.get("city"));
           }
           if(addressParams.containsKey("state")){
               addresses.setState((String)addressParams.get("state"));
           }
           if(addressParams.containsKey("stateCode")){
               addresses.setStateCode((String)addressParams.get("stateCode"));
           }
           if(addressParams.containsKey("country")){
               addresses.setCountry((String)addressParams.get("country"));
           }
           if(addressParams.containsKey("postalCode")){
               addresses.setPostalCode((String)addressParams.get("postalCode"));
           }
           if(addressParams.containsKey("phone")){
               addresses.setPhone((String)addressParams.get("phone"));
           }
           if(addressParams.containsKey("mobileNumber")){
               addresses.setMobileNumber((String)addressParams.get("mobileNumber"));
           }
           if(addressParams.containsKey("fax")){
               addresses.setFax((String)addressParams.get("fax"));
           }
           if(addressParams.containsKey("emailID")){
               addresses.setEmailID((String)addressParams.get("emailID"));
           }
           if(addressParams.containsKey("recipientName")){
               addresses.setRecipientName((String)addressParams.get("recipientName"));
           }
           if(addressParams.containsKey("contactPerson")){
               addresses.setContactPerson((String)addressParams.get("contactPerson"));
           }
           if(addressParams.containsKey("contactPersonNumber")){
               addresses.setContactPersonNumber((String)addressParams.get("contactPersonNumber"));
           }
           if (addressParams.containsKey("contactPersonDesignation")) {
                addresses.setContactPersonDesignation((String) addressParams.get("contactPersonDesignation"));
           }
           if (addressParams.containsKey("website")) {
                addresses.setWebsite((String) addressParams.get("website"));
           }
           if(addressParams.containsKey("isBillingAddress")){
               addresses.setIsBillingAddress(Boolean.parseBoolean((String)addressParams.get("isBillingAddress").toString()));
           }
           if(addressParams.containsKey("isDefaultAddress")){
               addresses.setIsDefaultAddress(Boolean.parseBoolean((String)addressParams.get("isDefaultAddress").toString()));
           }
           if(addressParams.containsKey("vendorid")){
               addresses.setVendorID((String)addressParams.get("vendorid"));
           }
           if(!StringUtil.isNullOrEmpty(companyid)){
               addresses.setCompany((Company) get(Company.class, companyid));
           }
           save(addresses);
           list.add(addresses);                        

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "Address field added successfully.", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject deleteVendorAddressDetails(String vendorID, String companyid) throws ServiceException {
        int numRows = 0;
        try {
            String delQuery = "delete from VendorAddressDetails vad where vad.vendorID=? and vad.company.companyID=?";
            numRows += executeUpdate(delQuery, new Object[]{vendorID, companyid});
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject getVendorAddressDetails(HashMap<String, Object> requestParams) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String condition="";
            boolean isdefault=false;
            List params = new ArrayList();           
            String vendorid= (String) requestParams.get("vendorid");
            params.add(vendorid);
            String query = "from VendorAddressDetails vad where vad.vendorID=? ";
            if(requestParams.containsKey("companyid")){
                params.add(requestParams.get("companyid")); 
                condition+="and vad.company.companyID=? ";
            }
            if(requestParams.containsKey("isDefaultAddress")){
                isdefault=Boolean.parseBoolean(requestParams.get("isDefaultAddress").toString());
                if(isdefault){
                    condition+="and vad.isDefaultAddress= 'T' ";
                }
            }
            if(requestParams.containsKey("isBillingAddress")){
                boolean isBillingAddress=Boolean.parseBoolean(requestParams.get("isBillingAddress").toString());
                if(isBillingAddress){
                    condition+="and vad.isBillingAddress= 'T' ";
                } else {
                    condition+="and vad.isBillingAddress= 'F' ";
                }
            }
            query+=condition;
            list = executeQuery(query, params.toArray());
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }


    @Override
    public KwlReturnObject saveCustomerAddressesDetails(HashMap<String, Object> addressParams, String companyid) throws ServiceException {
        List list = new ArrayList();
        try {            
           CustomerAddressDetails addresses=new CustomerAddressDetails();
           
           if(addressParams.containsKey("addressid")){
               String addressid=(String)addressParams.get("addressid");
               if(!StringUtil.isNullOrEmpty(addressid)){
                   addresses = (CustomerAddressDetails) get(CustomerAddressDetails.class, addressid);
               }           
           }
           if(addressParams.containsKey("aliasName")){
               addresses.setAliasName((String)addressParams.get("aliasName"));
           }
           if(addressParams.containsKey("address")){
               addresses.setAddress((String)addressParams.get("address"));
           }
           if(addressParams.containsKey("county")){
               addresses.setCounty((String)addressParams.get("county"));
           }
           if(addressParams.containsKey("city")){
               addresses.setCity((String)addressParams.get("city"));
           }
           if(addressParams.containsKey("state")){
               addresses.setState((String)addressParams.get("state"));
           }
           if(addressParams.containsKey("stateCode")){
               addresses.setStateCode((String)addressParams.get("stateCode"));
           }
           if(addressParams.containsKey("country")){
               addresses.setCountry((String)addressParams.get("country"));
           }
           if(addressParams.containsKey("postalCode")){
               addresses.setPostalCode((String)addressParams.get("postalCode"));
           }
           if(addressParams.containsKey("phone")){
               addresses.setPhone((String)addressParams.get("phone"));
           }
           if(addressParams.containsKey("mobileNumber")){
               addresses.setMobileNumber((String)addressParams.get("mobileNumber"));
           }
           if(addressParams.containsKey("fax")){
               addresses.setFax((String)addressParams.get("fax"));
           }
           if(addressParams.containsKey("emailID")){
               addresses.setEmailID((String)addressParams.get("emailID"));
           }
           if(addressParams.containsKey("contactPerson")){
               addresses.setContactPerson((String)addressParams.get("contactPerson"));
           }
           if(addressParams.containsKey("recipientName")){
               addresses.setRecipientName((String)addressParams.get("recipientName"));
           }
           if(addressParams.containsKey("contactPersonNumber")){
               addresses.setContactPersonNumber((String)addressParams.get("contactPersonNumber"));
           }
           if (addressParams.containsKey("contactPersonDesignation")) {
                addresses.setContactPersonDesignation((String) addressParams.get("contactPersonDesignation"));
           }
           if (addressParams.containsKey("website")) {
                addresses.setWebsite((String) addressParams.get("website"));
           }
           if(addressParams.containsKey("shippingRoute")){
               addresses.setShippingRoute((String)addressParams.get("shippingRoute"));
           }
           if(addressParams.containsKey("isBillingAddress")){
               addresses.setIsBillingAddress(Boolean.parseBoolean((String)addressParams.get("isBillingAddress").toString()));
           }
           if(addressParams.containsKey("isDefaultAddress")){
               addresses.setIsDefaultAddress(Boolean.parseBoolean((String)addressParams.get("isDefaultAddress").toString()));
           }
           if(addressParams.containsKey("customerid")){
               addresses.setCustomerID((String)addressParams.get("customerid"));
           }
           if(!StringUtil.isNullOrEmpty(companyid)){
               addresses.setCompany((Company) get(Company.class, companyid));
           }
            saveOrUpdate(addresses);
            list.add(addresses);
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "Address field added successfully.", null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteCustomerAddressDetails(String customerID, String companyid) throws ServiceException {
        int numRows = 0;
        try {
            String delQuery = "delete from CustomerAddressDetails cad where cad.customerID=? and cad.company.companyID=?";
            numRows += executeUpdate(delQuery, new Object[]{customerID, companyid});
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, null, numRows);
    }
    @Override
    public KwlReturnObject deleteCustomerAddressByID(String id, String companyid) throws ServiceException {
        int count = 0;
            String delQuery = "delete from CustomerAddressDetails cad where cad.ID=? and cad.company.companyID=?";
            count = executeUpdate(delQuery, new Object[]{id, companyid});
        return new KwlReturnObject(true, "", null, null, count);
    }
    
    @Override
    public KwlReturnObject deleteVendorAddressByID(String id, String companyid) throws ServiceException {
        int count = 0;
            String delQuery = "delete from VendorAddressDetails vad where vad.ID=? and vad.company.companyID=?";
            count = executeUpdate(delQuery, new Object[]{id, companyid});
        return new KwlReturnObject(true, "", null, null, count);
    }
    
    @Override
    public KwlReturnObject getCustomerAddressDetails(HashMap<String, Object> requestParams) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String condition="";
            boolean isdefault=false;
            List params = new ArrayList();           
            String customerid= (String) requestParams.get("customerid");
            params.add(customerid);
            String query = "from CustomerAddressDetails cad where cad.customerID=? ";
            if(requestParams.containsKey("companyid")){
                params.add(requestParams.get("companyid")); 
                condition+="and cad.company.companyID=? ";
            }
            if(requestParams.containsKey("isDefaultAddress")){
                isdefault=Boolean.parseBoolean(requestParams.get("isDefaultAddress").toString());
                if(isdefault){
                    condition+="and cad.isDefaultAddress= 'T' ";
                }
            }
            if(requestParams.containsKey("isBillingAddress")){
               boolean isBillingAddress=Boolean.parseBoolean(requestParams.get("isBillingAddress").toString());
                if(isBillingAddress){
                    condition+="and cad.isBillingAddress= 'T' ";
                } else {
                    condition+="and cad.isBillingAddress= 'F' ";
                }
            }
            if(requestParams.containsKey("aliasName") && requestParams.get("aliasName")!=null && !StringUtil.isNullOrEmpty(requestParams.get("aliasName").toString())){
                params.add(requestParams.get("aliasName").toString()); 
                condition+="and cad.aliasName=? ";
            }
            query+=condition;
            list = executeQuery(query, params.toArray());
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }
        
    @Override
    public KwlReturnObject getCustomerAddressDetailsMap(HashMap<String, Object> addrRequestParams) throws ServiceException {        
        List<Object[]> list = new ArrayList<>();
        try {
            String companyid = null;
            boolean isBillingAddress = false;
            boolean isDefaultAddress = false;
            
            String conditionQuery = "";
            List params = new ArrayList();
            
            if(addrRequestParams.get("companyid") != null) {
                companyid = addrRequestParams.get("companyid").toString();
            }
            
            if(addrRequestParams.get("isBillingAddress") != null) {
                isBillingAddress = Boolean.parseBoolean(addrRequestParams.get("isBillingAddress").toString());
                
                    conditionQuery += " AND cad.isbillingaddress = ? ";
                if(isBillingAddress) {
                    params.add("T");
                }else{
                    params.add("F");
                }
            }
            
            if(addrRequestParams.get("isDefaultAddress") != null) {
                isDefaultAddress = Boolean.parseBoolean(addrRequestParams.get("isDefaultAddress").toString());
                
                if(isDefaultAddress) {
                    conditionQuery += " AND cad.isdefaultaddress = ? ";
                    params.add("T");
                }
            }
            
            String query = "SELECT cad.customerid, cad.address, cad.city, cad.state, cad.country, CONCAT_WS(', ', cad.address, cad.city, cad.state, cad.country) as fulladdress , county " +
                    "FROM customeraddressdetails cad " +
                    "WHERE cad.company = ? " + conditionQuery + " GROUP BY cad.customerid";
            
            params.add(0, companyid);
            
            list = executeSQLQuery(query, params.toArray());
            
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new KwlReturnObject(true, null, null, list, list.size());
    }
        
    @Override
    public void updateCustomerAddressDefaultValueToFalse(String customerid, boolean isBillingAddress) throws ServiceException {
        String query = "update  customeraddressdetails set isdefaultaddress ='F' where customerid=? and isdefaultaddress='T' ";
        if (isBillingAddress) {
            query += "and isbillingaddress='T' ";
        } else {
            query += "and isbillingaddress='F' ";
        }
        executeSQLUpdate(query, new Object[]{customerid});
    }
    
    @Override
    public KwlReturnObject getInvoiceCustomFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "From Invoice inv";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getVendorInvoiceCustomFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "From GoodsReceipt inv";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getCustomerQuotationCustomFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "select q From Quotation q LEFT JOIN q.quotationCustomData qcustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getVendorQuotationCustomFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "select q From VendorQuotation q LEFT JOIN q.vendorQuotationCustomData qcustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getSalesOrderDateCustomFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "select so From SalesOrder so LEFT JOIN so.soCustomData socustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getPurchaseOrderCustomFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "select po From PurchaseOrder po LEFT JOIN po.poCustomData pocustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getDeliveryOrderCustomFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "select do From DeliveryOrder do LEFT JOIN do.deliveryOrderCustomData docustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getGoodsReceiptOrderCustomFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "select gro From GoodsReceiptOrder gro LEFT JOIN gro.goodsReceiptOrderCustomData grocustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getJournalEntryCustomFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "select je From JournalEntry je LEFT JOIN je.accBillInvCustomData jecustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getSalesReturnCustomFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "select sr From SalesReturn sr LEFT JOIN sr.salesReturnCustomData srcustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getPurchaseReturnCustomFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "select pr From PurchaseReturn pr LEFT JOIN pr.purchaseReturnCustomData prcustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getCustomerCustomFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "select c From Customer c LEFT JOIN c.accCustomerCustomData ccustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getVendorCustomFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "select v From Vendor v LEFT JOIN v.accVendorCustomData vcustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getCustomerInvoiceLineCustomDateFieldMails(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String selectFields = requestParams.containsKey("selectfields") && requestParams.get("selectfields").toString().length() > 0
                    ? requestParams.get("selectfields").toString() : "";
            String query = "select inv.id,inv.invoicenumber,je.entrydate, product.name, " + selectFields + " from invoice inv "
                    + "inner join journalentry je on je.id = inv.journalentry "
                    + "inner join invoicedetails invd on invd.invoice = inv.id "
                    + "inner join inventory invent on invent.id = invd.id "
                    + "inner join product on product.id = invent.product "
                    + "left join accjedetailcustomdata accjedcustom on accjedcustom.recdetailId = invd.id";
            ArrayList name = null;
            ArrayList value = null;
            if (requestParams.get("filter_names") != null && requestParams.get("filter_values") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_values"));
                query += com.krawler.common.util.StringUtil.filterQuery(name, "where");
                int ind = query.indexOf("(");

                if (ind > -1) {
                    int index = Integer.valueOf(query.substring(ind + 1, ind + 2));
                    query = query.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
                    value.remove(index);
                }
            }
            list = executeSQLQuery(query, value.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getVendorInvoiceLineCustomDateFieldMails(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String selectFields = requestParams.containsKey("selectfields") && requestParams.get("selectfields").toString().length() > 0
                    ? requestParams.get("selectfields").toString() : "";
            String query = "select gr.id,gr.grnumber,je.entrydate, product.name, " + selectFields + " from goodsreceipt gr "
                    + "inner join journalEntry je on je.id = gr.journalentry "
                    + "inner join grdetails grd on grd.goodsreceipt = gr.id "
                    + "inner join inventory invent on invent.id = grd.id "
                    + "inner join product on product.id = invent.product "
                    + "left join accjedetailcustomdata accjedcustom on accjedcustom.recdetailId = grd.id";
            ArrayList name = null;
            ArrayList value = null;
            if (requestParams.get("filter_names") != null && requestParams.get("filter_values") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_values"));
                query += com.krawler.common.util.StringUtil.filterQuery(name, "where");
                int ind = query.indexOf("(");

                if (ind > -1) {
                    int index = Integer.valueOf(query.substring(ind + 1, ind + 2));
                    query = query.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
                    value.remove(index);
                }
            }
            list = executeSQLQuery(query, value.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getCustomerQuotationDetailsLineCustomDateFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "Select qd From QuotationDetail qd LEFT JOIN qd.quotationDetailCustomData qdcustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getVendorQuotationDetailsLineCustomDateFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "Select q From VendorQuotationDetail q LEFT JOIN q.vendorQuotationDetailCustomData qcustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getSalesOrderDetailsLineCustomDateFields(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "Select sod From SalesOrderDetail sod LEFT JOIN sod.soDetailCustomData sodcustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getPurchaseOrderDetailLineCustomDateFieldMails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "Select pod From PurchaseOrderDetail pod LEFT JOIN pod.poDetailCustomData podcustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getDeliveryOrderDetailLineCustomDateFieldMails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "Select dod From DeliveryOrderDetail dod LEFT JOIN dod.deliveryOrderDetailCustomData dodcustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getGoodsReceiptOrderDetailLineCustomDateFieldMails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "Select grod From GoodsReceiptOrderDetails grod LEFT JOIN grod.goodsReceiptOrderDetailsCustomDate grodcustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getSalesReturnDetailLineCustomDateFieldMails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "Select srd From SalesReturnDetail srd LEFT JOIN srd.salesReturnDetailCustomData srdcustom";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject getPurchaseReturnDetailLineCustomDateFieldMails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "Select prd From PurchaseReturnDetail prd LEFT JOIN prd.purchaseReturnDetailCustomDate prdcustom";
        return buildNExecuteQuery(query, requestParams);
    }
    public int getNature(String grpname, String companyid) throws ServiceException {
        try {
            ArrayList params = new ArrayList();
            params.add(grpname);
            params.add(companyid);
            String query = "select nature from Group where ID=? and company.companyID=?";
            List list = executeQuery(query, params.toArray());
            int num = Integer.parseInt(list.get(0).toString());
            return num;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("removeFileTable:" + ex.getMessage(), ex);
        }
    }
    @Override
    public void updateAdvanceDetailAmountDueOnAmountReceived(String advancedetailid, double amountreceived) throws ServiceException {
        try {
            String query = "Update advancedetail set amountdue = (amountdue + ?) where id = ?";
            executeSQLUpdate(query, new Object[]{amountreceived, advancedetailid});
        } catch(Exception ex) {
            throw ServiceException.FAILURE("updateAdvanceDetailAmountDueOnAmountReceived:" + ex.getMessage(), ex);
        }
    }
    
    @Override
    public List getPaymentAdvanceDetailsInRefundCase(String advancedetailid) throws ServiceException {
        List list = new ArrayList(); 
        try {
            String query = "select p.paymentnumber, p.currency, adv.amountdue, p.depositamount from advancedetail adv inner join payment p on p.id = adv.payment where adv.id = ?";
            list = executeSQLQuery(query, new Object[]{advancedetailid});
        } catch(Exception ex) {
            throw ServiceException.FAILURE("getPaymentAdvanceDetailsInRefundCase:" + ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public KwlReturnObject getProductExpiryList(String companyId) throws ServiceException {
        
        KwlReturnObject retObj=new KwlReturnObject(false, null, null, null, 0);
        //String query = "FROM NewBatchSerial WHERE company.companyID = ? AND quantitydue = 1 AND exptodate IS NOT NULL ";
        
        String sqlqry="SELECT p.productid,p.name,s.serialname,s.exptodate from newbatchserial s "
                + " LEFT JOIN product p   ON s.product= p.id where s.quantitydue=1 AND  s.exptodate IS NOT NULL "
                + " AND  s.company=? AND p.company=?  ORDER BY p.productid ";
        
        List returnList=new ArrayList();
        List params = new ArrayList(); 
        params.add(companyId);
        params.add(companyId);
        
        returnList=executeSQLQuery(sqlqry, params.toArray());
        if(!returnList.isEmpty()){
           retObj=new KwlReturnObject(true, null, null, returnList, returnList.size());
        }
        
        return retObj;
    }
    
    @Override
    public String getCustomerAddress(HashMap<String, Object> addrRequestParams) {
        String address = "";
        try {
            KwlReturnObject addressResult = getCustomerAddressDetails(addrRequestParams);
            if (addressResult.getEntityList() != null && !addressResult.getEntityList().isEmpty()) {
                CustomerAddressDetails customerAddressDetail = (CustomerAddressDetails) addressResult.getEntityList().get(0);
                
                boolean isSeparator = false; // Separator for Document Designer
                if (addrRequestParams.containsKey("isSeparator") && addrRequestParams.get("isSeparator") != null) {
                    isSeparator = (Boolean) addrRequestParams.get("isSeparator");
                }
                String separator= isSeparator?"!## ":", ";
                
                String countryName = StringUtil.isNullOrEmpty(customerAddressDetail.getCountry())?"":customerAddressDetail.getCountry();
                countryName = countryName.trim();
                countryName = countryName.toLowerCase();
                
                if(addrRequestParams.containsKey("templateFlag") && addrRequestParams.get("templateFlag") != null && (Constants.BakerTilly_templateflag == (Integer) addrRequestParams.get("templateFlag") || Constants.BakerTilly_templateflag_pcs == (Integer) addrRequestParams.get("templateFlag"))){
                    String addr = StringUtil.isNullOrEmpty(customerAddressDetail.getAddress()) ? "" : customerAddressDetail.getAddress();
                    String postalcode = StringUtil.isNullOrEmpty(customerAddressDetail.getPostalCode()) ? "" : separator + customerAddressDetail.getPostalCode();
                    String city = StringUtil.isNullOrEmpty(customerAddressDetail.getCity()) ? "" : separator + customerAddressDetail.getCity();
                    String state = StringUtil.isNullOrEmpty(customerAddressDetail.getState()) ? "" : separator + customerAddressDetail.getState();
                    String country = StringUtil.isNullOrEmpty(customerAddressDetail.getCountry()) ? "" : separator+ customerAddressDetail.getCountry();
                    address = addr + city + state + country + postalcode;
                } else if(countryName.contains("malaysia")){ // for malaysia country address format is different SDP-2247
                    String addr = StringUtil.isNullOrEmpty(customerAddressDetail.getAddress()) ? "" : customerAddressDetail.getAddress();
                    String postalcode = StringUtil.isNullOrEmpty(customerAddressDetail.getPostalCode()) ? "" : separator + customerAddressDetail.getPostalCode();
                    String city = StringUtil.isNullOrEmpty(customerAddressDetail.getCity()) ? "" :StringUtil.isNullOrEmpty(customerAddressDetail.getPostalCode()) ? separator+ customerAddressDetail.getCity() : " " + customerAddressDetail.getCity();
                    String state = StringUtil.isNullOrEmpty(customerAddressDetail.getState()) ? "" : separator + customerAddressDetail.getState();
                    String country = StringUtil.isNullOrEmpty(customerAddressDetail.getCountry()) ? "" : separator+ customerAddressDetail.getCountry();
                    address = addr + postalcode + city + state + country;
                } else if(countryName.equals("us") || countryName.contains("usa") || countryName.contains("united states")){ // for USA country address format is different
                    String addr = StringUtil.isNullOrEmpty(customerAddressDetail.getAddress()) ? "" : customerAddressDetail.getAddress();
                    String county = StringUtil.isNullOrEmpty(customerAddressDetail.getCounty()) ? "" :separator + customerAddressDetail.getCounty();
                    String city = StringUtil.isNullOrEmpty(customerAddressDetail.getCity()) ? "" :separator + customerAddressDetail.getCity();
                    String state = StringUtil.isNullOrEmpty(customerAddressDetail.getState()) ? "" :separator + customerAddressDetail.getState();
                    String country = StringUtil.isNullOrEmpty(customerAddressDetail.getCountry()) ? "" : separator + customerAddressDetail.getCountry();
                    String postalcode = StringUtil.isNullOrEmpty(customerAddressDetail.getPostalCode()) ? "" : " " + customerAddressDetail.getPostalCode();
                    String email = StringUtil.isNullOrEmpty(customerAddressDetail.getEmailID()) ? "" : "\nEmail : " + customerAddressDetail.getEmailID();
                    String phone = StringUtil.isNullOrEmpty(customerAddressDetail.getPhone()) ? "" : "\nPhone : " + customerAddressDetail.getPhone();
                    String mobile = StringUtil.isNullOrEmpty(customerAddressDetail.getMobileNumber()) ? "" : "\nMobile : " + customerAddressDetail.getMobileNumber();
                    String fax = StringUtil.isNullOrEmpty(customerAddressDetail.getFax()) ? "" : separator +"Fax : " + customerAddressDetail.getFax();
                    address = addr + county + city + state + postalcode + country + email + phone + mobile + fax;
                } else{
                    String addr = StringUtil.isNullOrEmpty(customerAddressDetail.getAddress()) ? "" : customerAddressDetail.getAddress();
                    String city = StringUtil.isNullOrEmpty(customerAddressDetail.getCity()) ? "" :separator + customerAddressDetail.getCity();
                    String state = StringUtil.isNullOrEmpty(customerAddressDetail.getState()) ? "" :separator + customerAddressDetail.getState();
                    String country = StringUtil.isNullOrEmpty(customerAddressDetail.getCountry()) ? "" : separator + customerAddressDetail.getCountry();
                    String postalcode = StringUtil.isNullOrEmpty(customerAddressDetail.getPostalCode()) ? "" : " " + customerAddressDetail.getPostalCode();
                    String email = StringUtil.isNullOrEmpty(customerAddressDetail.getEmailID()) ? "" : "\nEmail : " + customerAddressDetail.getEmailID();
                    String phone = StringUtil.isNullOrEmpty(customerAddressDetail.getPhone()) ? "" : "\nPhone : " + customerAddressDetail.getPhone();
                    String mobile = StringUtil.isNullOrEmpty(customerAddressDetail.getMobileNumber()) ? "" : "\nMobile : " + customerAddressDetail.getMobileNumber();
                    String fax = StringUtil.isNullOrEmpty(customerAddressDetail.getFax()) ? "" : separator +"Fax : " + customerAddressDetail.getFax();
                    address = addr + city + state + country + postalcode + email + phone + mobile + fax;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return address;
    }
    
    @Override
    public  CustomerAddressDetails getCustomerAddressobj(HashMap<String, Object> addrRequestParams) {
        CustomerAddressDetails customerAddressDetail = null;
        try {
            KwlReturnObject addressResult = getCustomerAddressDetails(addrRequestParams);
            if (addressResult.getEntityList() != null && !addressResult.getEntityList().isEmpty()) {
                customerAddressDetail = (CustomerAddressDetails) addressResult.getEntityList().get(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return customerAddressDetail;
    }
    
    @Override
    public String getVendorAddress(HashMap<String, Object> addrRequestParams) {
        String address = "";
        try {
            KwlReturnObject addressResult = getVendorAddressDetails(addrRequestParams);
            if (addressResult.getEntityList() != null && !addressResult.getEntityList().isEmpty()) {
                VendorAddressDetails VendorAddressDetails = (VendorAddressDetails) addressResult.getEntityList().get(0);
                
                boolean isSeparator = false; // Separator for Document Designer
                if (addrRequestParams.containsKey("isSeparator") && addrRequestParams.get("isSeparator") != null) {
                    isSeparator = (Boolean) addrRequestParams.get("isSeparator");
                }
                String separator= isSeparator?"!## ":", ";
                String countryName = StringUtil.isNullOrEmpty(VendorAddressDetails.getCountry())?"":VendorAddressDetails.getCountry();
                countryName = countryName.trim();
                countryName = countryName.toLowerCase();
                if (addrRequestParams.containsKey("templateFlag") && addrRequestParams.get("templateFlag") != null && Constants.BakerTilly_templateflag == (Integer) addrRequestParams.get("templateFlag")) {
                    String addr = StringUtil.isNullOrEmpty(VendorAddressDetails.getAddress()) ? "" : VendorAddressDetails.getAddress();
                    String postalcode = StringUtil.isNullOrEmpty(VendorAddressDetails.getPostalCode()) ? "" : separator + VendorAddressDetails.getPostalCode();
                    String city = StringUtil.isNullOrEmpty(VendorAddressDetails.getCity()) ? "" : separator + VendorAddressDetails.getCity();
                    String state = StringUtil.isNullOrEmpty(VendorAddressDetails.getState()) ? "" : separator + VendorAddressDetails.getState();
                    String country = StringUtil.isNullOrEmpty(VendorAddressDetails.getCountry()) ? "" : separator + VendorAddressDetails.getCountry();
                    address = addr + city + state + country + postalcode;
                } else if(countryName.contains("malaysia")){ // for malaysia country address format is different SDP-2247
                    String addr = StringUtil.isNullOrEmpty(VendorAddressDetails.getAddress()) ? "" : VendorAddressDetails.getAddress();
                    String postalcode = StringUtil.isNullOrEmpty(VendorAddressDetails.getPostalCode()) ? "" : separator + VendorAddressDetails.getPostalCode();
                    String city = StringUtil.isNullOrEmpty(VendorAddressDetails.getCity()) ? "" : StringUtil.isNullOrEmpty(VendorAddressDetails.getPostalCode()) ? separator + VendorAddressDetails.getCity() : " " + VendorAddressDetails.getCity();
                    String state = StringUtil.isNullOrEmpty(VendorAddressDetails.getState()) ? "" : separator + VendorAddressDetails.getState();
                    String country = StringUtil.isNullOrEmpty(VendorAddressDetails.getCountry()) ? "" : separator + VendorAddressDetails.getCountry();
                    address = addr + postalcode + city + state + country;
                } else if(countryName.equals("us") || countryName.contains("usa") || countryName.contains("united states")){ // for USA country address format is different
                    String addr = StringUtil.isNullOrEmpty(VendorAddressDetails.getAddress()) ? "" : VendorAddressDetails.getAddress();
                    String county = StringUtil.isNullOrEmpty(VendorAddressDetails.getCounty()) ? "" : separator + VendorAddressDetails.getCounty();
                    String city = StringUtil.isNullOrEmpty(VendorAddressDetails.getCity()) ? "" : separator + VendorAddressDetails.getCity();
                    String state = StringUtil.isNullOrEmpty(VendorAddressDetails.getState()) ? "" : separator + VendorAddressDetails.getState();
                    String country = StringUtil.isNullOrEmpty(VendorAddressDetails.getCountry()) ? "" : separator + VendorAddressDetails.getCountry();
                    String postalcode = StringUtil.isNullOrEmpty(VendorAddressDetails.getPostalCode()) ? "" : " " + VendorAddressDetails.getPostalCode();
                    String email = StringUtil.isNullOrEmpty(VendorAddressDetails.getEmailID()) ? "" : "\nEmail : " + VendorAddressDetails.getEmailID();
                    String phone = StringUtil.isNullOrEmpty(VendorAddressDetails.getPhone()) ? "" : "\nPhone : " + VendorAddressDetails.getPhone();
                    String mobile = StringUtil.isNullOrEmpty(VendorAddressDetails.getMobileNumber()) ? "" : "\nMobile : " + VendorAddressDetails.getMobileNumber();
                    String fax = StringUtil.isNullOrEmpty(VendorAddressDetails.getFax()) ? "" : separator +"Fax : " + VendorAddressDetails.getFax();
                    address = addr + county + city + state + postalcode + country + email + phone + mobile + fax;
                } else{
                    String addr = StringUtil.isNullOrEmpty(VendorAddressDetails.getAddress()) ? "" : VendorAddressDetails.getAddress();
                    String city = StringUtil.isNullOrEmpty(VendorAddressDetails.getCity()) ? "" : separator + VendorAddressDetails.getCity();
                    String state = StringUtil.isNullOrEmpty(VendorAddressDetails.getState()) ? "" : separator + VendorAddressDetails.getState();
                    String country = StringUtil.isNullOrEmpty(VendorAddressDetails.getCountry()) ? "" : separator + VendorAddressDetails.getCountry();
                    String postalcode = StringUtil.isNullOrEmpty(VendorAddressDetails.getPostalCode()) ? "" : " " + VendorAddressDetails.getPostalCode();
                    String email = StringUtil.isNullOrEmpty(VendorAddressDetails.getEmailID()) ? "" : "\nEmail : " + VendorAddressDetails.getEmailID();
                    String phone = StringUtil.isNullOrEmpty(VendorAddressDetails.getPhone()) ? "" : "\nPhone : " + VendorAddressDetails.getPhone();
                    String mobile = StringUtil.isNullOrEmpty(VendorAddressDetails.getMobileNumber()) ? "" : "\nMobile : " + VendorAddressDetails.getMobileNumber();
                    String fax = StringUtil.isNullOrEmpty(VendorAddressDetails.getFax()) ? "" : separator +"Fax : " + VendorAddressDetails.getFax();
                    address = addr + city + state + country + postalcode + email + phone + mobile + fax;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return address;
    }
    
    @Override
    public VendorAddressDetails getVendorAddressObj(HashMap<String, Object> addrRequestParams) {
        VendorAddressDetails VendorAddressDetails = null;
        try {
            KwlReturnObject addressResult = getVendorAddressDetails(addrRequestParams);
            if (addressResult.getEntityList() != null && !addressResult.getEntityList().isEmpty()) {
                VendorAddressDetails = (VendorAddressDetails) addressResult.getEntityList().get(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return VendorAddressDetails;
    }
    
    @Override
    public String getVendorAddressForSenwanTec(HashMap<String, Object> addrRequestParams) {
        String address = "";
        VendorAddressDetails VendorAddressDetails = null;
        try {
            KwlReturnObject addressResult = getVendorAddressDetails(addrRequestParams);
            if (addressResult.getEntityList() != null && !addressResult.getEntityList().isEmpty()) {
                VendorAddressDetails = (VendorAddressDetails) addressResult.getEntityList().get(0);
                String addr = StringUtil.isNullOrEmpty(VendorAddressDetails.getAddress()) ? "" : VendorAddressDetails.getAddress();
                String city = StringUtil.isNullOrEmpty(VendorAddressDetails.getCity()) ? "" : ", " + VendorAddressDetails.getCity();
                String state = StringUtil.isNullOrEmpty(VendorAddressDetails.getState()) ? "" : ", " + VendorAddressDetails.getState();
                String country = StringUtil.isNullOrEmpty(VendorAddressDetails.getCountry()) ? "" : ", " + VendorAddressDetails.getCountry();
                address = addr + city + state + country;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return address;
    }

    @Override
    public String getTotalVendorAddress(HashMap<String, Object> addrRequestParams) {
        String address = "";
        VendorAddressDetails vendorAddresses = null;
        try {
            KwlReturnObject addressResult = getVendorAddressDetails(addrRequestParams);
            if (addressResult.getEntityList() != null && !addressResult.getEntityList().isEmpty()) {
                vendorAddresses = (VendorAddressDetails) addressResult.getEntityList().get(0);
                String addr = StringUtil.isNullOrEmpty(vendorAddresses.getAddress()) ? "" : vendorAddresses.getAddress();
                String city = StringUtil.isNullOrEmpty(vendorAddresses.getCity()) ? "" : ", " + vendorAddresses.getCity();
                String state = StringUtil.isNullOrEmpty(vendorAddresses.getState()) ? "" : ", " + vendorAddresses.getState();
                String country = StringUtil.isNullOrEmpty(vendorAddresses.getCountry()) ? "" : ", " + vendorAddresses.getCountry();
                String postalcode = StringUtil.isNullOrEmpty(vendorAddresses.getPostalCode()) ? "." : "-" + vendorAddresses.getPostalCode();
                address = addr + city + state + country + postalcode;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return address;
    }
    
    @Override
    public String getCustomerAddressForSenwanTec(HashMap<String, Object> addrRequestParams) {
        String address = "";
        CustomerAddressDetails customerAddresses = null;
        try {
            KwlReturnObject addressResult = getCustomerAddressDetails(addrRequestParams);
            if (addressResult.getEntityList() != null && !addressResult.getEntityList().isEmpty()) {
                customerAddresses = (CustomerAddressDetails) addressResult.getEntityList().get(0);
                String addr = StringUtil.isNullOrEmpty(customerAddresses.getAddress()) ? "" : customerAddresses.getAddress();
                String city = StringUtil.isNullOrEmpty(customerAddresses.getCity()) ? "" : ", " + customerAddresses.getCity();
                String state = StringUtil.isNullOrEmpty(customerAddresses.getState()) ? "" : ", " + customerAddresses.getState();
                String country = StringUtil.isNullOrEmpty(customerAddresses.getCountry()) ? "" : ", " + customerAddresses.getCountry();
                address = addr + city + state + country;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return address;
    }
    
    @Override
    public String getTotalCustomerAddress(HashMap<String, Object> addrRequestParams) {
        String address = "";
        CustomerAddressDetails customerAddresses = null;
        try {
            KwlReturnObject addressResult = getCustomerAddressDetails(addrRequestParams);
            if (addressResult.getEntityList() != null && !addressResult.getEntityList().isEmpty()) {
                customerAddresses = (CustomerAddressDetails) addressResult.getEntityList().get(0);
                String addr = StringUtil.isNullOrEmpty(customerAddresses.getAddress()) ? "" : customerAddresses.getAddress();
                String city = StringUtil.isNullOrEmpty(customerAddresses.getCity()) ? "" : ", " + customerAddresses.getCity();
                String state = StringUtil.isNullOrEmpty(customerAddresses.getState()) ? "" : ", " + customerAddresses.getState();
                String country = StringUtil.isNullOrEmpty(customerAddresses.getCountry()) ? "" : ", " + customerAddresses.getCountry();
                String postalcode = StringUtil.isNullOrEmpty(customerAddresses.getPostalCode()) ? "." : "-" + customerAddresses.getPostalCode();
                address = addr + city + state + country + postalcode;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return address;
    }
    
    @Override
    public List getDescriptionConfig(HashMap<String, Object> requestParams) throws ServiceException{
        List list = null;
        try {
            String companyid="";
            String condition="";
            if(requestParams.containsKey("companyid") && requestParams.get("companyid")!=null){
                ArrayList params = new ArrayList();
                companyid=(String)requestParams.get("companyid");
                int moduleid=-1;
                if(requestParams.containsKey("moduleid") && requestParams.get("moduleid")!=null){
                    moduleid=Integer.parseInt(requestParams.get("moduleid").toString());
                }
                int document=-1;
                if(requestParams.containsKey("document") && requestParams.get("document")!=null){
                    document=Integer.parseInt(requestParams.get("document").toString());
                }
                params.add(companyid);
                if(moduleid!=-1){
                    condition+=" AND moduleid=? ";
                    params.add(moduleid);
                }
                if(document!=-1){
                    condition+=" AND document=? ";
                    params.add(document);
                }
                String query = "select id,moduleid,field,document,jsondata,company from gldescriptionconfig where company=? "+condition;
                try {
                    list = executeSQLQuery(query, params.toArray());
                } catch (ServiceException ex) {
                    Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return list;
        }
    }
     @Override
    public KwlReturnObject getCompanyAddressDetails(HashMap<String, Object> requestParams) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String condition = "";
            boolean isdefault = false;
            List params = new ArrayList();
            String companyid = (String) requestParams.get(Constants.companyKey);
            params.add(companyid);
            String query = "from CompanyAddressDetails cad where cad.company.companyID=? ";
            if (requestParams.containsKey("isDefaultAddress")) {
                isdefault = Boolean.parseBoolean(requestParams.get("isDefaultAddress").toString());
                if (isdefault) {
                    condition += "and cad.isDefaultAddress= 'T' ";
                }
            }
            if (requestParams.containsKey("isBillingAddress")) {
                boolean isBillingAddress = Boolean.parseBoolean(requestParams.get("isBillingAddress").toString());
                if (isBillingAddress) {
                    condition += "and cad.isBillingAddress= 'T' ";
                } else {
                    condition += "and cad.isBillingAddress= 'F' ";
                }
            }
            query += condition;
            list = executeQuery(query, params.toArray());
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getEmailTemplateTosendApprovalMail(String companyid, String fieldid, int moduleID) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from NotificationRules where moduleId=?  and fieldid= ? and company.companyID=?";
            list = executeQuery(query, new Object[]{moduleID, fieldid, companyid});
            if (list.isEmpty()) {
                query = "from NotificationRules where moduleId=?  and fieldid= ? and company.companyID IS NULL";
                list = executeQuery(query, new Object[]{moduleID, fieldid});
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccountingHandlerDAOImpl.getEmailTemplateTosendApprovalMail : " + ex.getMessage(), ex);
        } finally {
            return new KwlReturnObject(true, "AccountingHandlerDAOImpl.getEmailTemplateTosendApprovalMail", null, list, listSize);
        }
    }

    @Override
    public void insertDocumentMailMapping(String documentid, int moduleid, String ruleid, String companyid) throws ServiceException {
        try {
            List params = new ArrayList();
            String query = "insert into documentrecurringmailrecord (id,documentid,moduleid,maildate,notificationruleid,company) values (uuid(),?,?,now(),?,?)";
            params.add(documentid);
            params.add(moduleid);
            params.add(ruleid);
            params.add(companyid);
            executeSQLUpdate(query, params.toArray());
        } catch(Exception ex ){
            throw ServiceException.FAILURE("AccountingHandlerDAOImpl.getEmailTemplateTosendApprovalMail : " + ex.getMessage(), ex);
        }
    }

    @Override
    public int getDocumentMailCount(String documentid, String companyid, String ruleid) throws ServiceException {
        List list=new ArrayList();
        List params=new ArrayList();
        String query="select * from documentrecurringmailrecord where documentid=? and notificationruleid=? and company=?";
        params.add(documentid);
        params.add(ruleid);
        params.add(companyid);
        list = executeSQLQuery(query, params.toArray());
        return list.size();
    }

    @Override
    public KwlReturnObject getProductMasterFieldsToShowAtLineLevel(Map<String, Object> ProductFieldsRequestParams) throws ServiceException {
        List list = new ArrayList();
        List params = new ArrayList();
        String companyid = "";
        String userid = "";
        String joinQuery = "";
        StringBuffer conditionSql = new StringBuffer(" where ");
        try {

            if (ProductFieldsRequestParams.containsKey("companyid")) {
                companyid = (String) ProductFieldsRequestParams.get("companyid");
                params.add((String)companyid);
                conditionSql.append(" crm.company.companyID=? ");
            }
            if (ProductFieldsRequestParams.containsKey("moduleid")) {
                int moduleid = (Integer) ProductFieldsRequestParams.get("moduleid");
                params.add(moduleid);
                conditionSql.append(" and pfm.moduleid=?  ");
            }

            joinQuery = "select df from  ProductFieldsAndModulesMapping pfm inner join pfm.fieldid as crm "
                    + " inner join crm.customizeReportHeader as crh  "
                    + "inner join crh.defaultheader as df  ";

            String finalQuery = joinQuery + conditionSql.toString();
            list = executeQuery(finalQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
     
    @Override
     public Boolean checkInventoryModuleFunctionalityIsUsedOrNot(String companyid) throws ServiceException {
        boolean isInventoryModuleUsed = false;
        Long totalCnt = 0l;
        // List<?> creatorResult = executeQuery("from InventoryWarehouse where isdefault='F' and  company.companyID=?", companyid);
        List list = executeQuery("select count(id) from InventoryWarehouse where isdefault=false and  company.companyID=?", companyid);
        if (list != null && !list.isEmpty()) {
            totalCnt = (Long) list.get(0);
            if (totalCnt > 0) {
                isInventoryModuleUsed = true;
            }
        } else {
            //creatorResult = executeQuery("from InventoryLocation where isdefault='F' and company.companyID=?", companyid);
            list = executeQuery("select count(id) from InventoryLocation where isdefault=false and company.companyID=?", companyid);
            if (list != null && !list.isEmpty()) {
                totalCnt = (Long) list.get(0);
                if (totalCnt > 0) {
                    isInventoryModuleUsed = true;
                }
            } else {
                //creatorResult = executeQuery("from StoreMaster where company.companyID=?", companyid);
                list = executeQuery("select count(id) from StoreMaster where company.companyID=?", companyid);
                if (list != null && !list.isEmpty()) {
                    totalCnt = (Long) list.get(0);
                    if (totalCnt > 0) {
                        isInventoryModuleUsed = true;
                    }
                }
            }
        }
        return isInventoryModuleUsed;
    }
     
    @Override
    public Boolean checkSerialNoFunctionalityisusedornot(String companyid,String checkColumn) throws ServiceException {
        boolean isUsedSerial = false;
        //List<?> creatorResult = executeQuery("from Product where ("+checkColumn+"='T') and company.companyID=?", companyid);
//        List<?> creatorResult = executeQuery("from Product where (islocationforproduct='T' or iswarehouseforproduct='T' or isSerialForProduct='T' or isBatchForProduct='T') and company.companyID=?", companyid);
        List list=executeQuery("select count(productid) from Product where ("+checkColumn+"='T') and company.companyID=?", companyid);        
        if (!list.isEmpty() && list.size() > 0) {
            Long count = (Long) list.get(0);
            if (count > 0) {
                isUsedSerial = true;
            }
        }
       
        return isUsedSerial;
    }

    @Override
    public KwlReturnObject getMasterItemByUserID(Map<String,Object> salesPersonParams) throws ServiceException {
          List list = new ArrayList();
        List params = new ArrayList();
        String joinQuery = "";
        StringBuffer conditionSql = new StringBuffer();
        try {

            if (salesPersonParams.containsKey("companyid")) {
                Object companyid =  salesPersonParams.get("companyid");
                params.add((String)companyid);
                conditionSql.append(" company.companyID=?  ");
            }
            
            if (salesPersonParams.containsKey("userid")) {
               Object userid = (String) salesPersonParams.get("userid");
                params.add((String)userid);
                conditionSql.append(" and user.userID=? ");
            }
             if (salesPersonParams.containsKey("grID")) {
                Object groupID =  salesPersonParams.get("grID");
                params.add((String)groupID);
                conditionSql.append(" and masterGroup.ID=?  ");
            }

            joinQuery = "select ID from MasterItem where ";
                    

            String finalQuery = joinQuery + conditionSql.toString();
            list = executeQuery(finalQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
        
    }
    
    
    public Map<String, String> getMasterItemByCompanyID(Map<String, Object> salesPersonParams) throws ServiceException {
        List<Object[]> list = new ArrayList();
        Map<String, String> ItemsMap = new HashMap<>();
        List params = new ArrayList();
        String joinQuery = "";
        StringBuffer conditionSql = new StringBuffer();
        try {

            if (salesPersonParams.containsKey("companyid")) {
                Object companyid = salesPersonParams.get("companyid");
                params.add((String) companyid);
                conditionSql.append(" company.companyID=?  ");
            }

            if (salesPersonParams.containsKey("userid")) {
                Object userid = (String) salesPersonParams.get("userid");
                params.add((String) userid);
                conditionSql.append(" and user.userID=? ");
            }
            if (salesPersonParams.containsKey("grID")) {
                Object groupID = salesPersonParams.get("grID");
                params.add((String) groupID);
                conditionSql.append(" and masterGroup.ID=?  ");
            }

            joinQuery = "select ID,value from MasterItem where ";

            String finalQuery = joinQuery + conditionSql.toString();
            list = executeQuery(finalQuery, params.toArray());

            for (Object[] objs : list) {
                String id = objs[0] != null ? (String) objs[0] : null;
                String Value = objs[1] != null ? (String) objs[1] : null;
                ItemsMap.put(id, Value);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return ItemsMap;

    }

    @Override
    public KwlReturnObject getExciseDetails(String receiptId) throws ServiceException {
        List list = new ArrayList();
        try {
            String selQuery = "from SalesInvoiceExciseDetailsMap where invoice.ID=?";
            list = executeQuery(selQuery, new Object[]{receiptId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accountingHandlerDAOobj.getExciseDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getSMTPAuthenticationDetails(HashMap<String, Object> requestParams) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            List params = new ArrayList();
            String companyid = (String) requestParams.get(Constants.companyKey);
            params.add(companyid);
            String query = "SELECT mailserveraddress,mailserverport,smtppassword,emailID from Company c where c.companyID=? ";
           
            list = executeQuery(query, params.toArray());
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accountingHandlerDAOobj.getSMTPAuthenticationDetails : " + ex.getMessage(), ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }
    
    @Override
    public KwlReturnObject getAdvancePayDetails(String paymentid) throws ServiceException {
        List list = new ArrayList();
        try {
            String selQuery = "from AdvanceDetail ad where ad.payment.ID=?";
            list = executeQuery(selQuery, new Object[]{paymentid});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accountingHandlerDAOobj.getExciseDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
   /* Get Attachment for Contract */ 
    @Override
    public KwlReturnObject getinvoiceDocuments(HashMap<String, Object> dataMap) throws ServiceException {
        List list = null;
        int count = 0;
        try {

            ArrayList params = new ArrayList();

            params.add((String) dataMap.get(Constants.companyKey));

            String conditionSQL = " where invoicedoccompmap.company=?";

            String invoiceId = (String) dataMap.get("invoiceID");
            if (!StringUtil.isNullOrEmpty(invoiceId)) {
                conditionSQL += " and invoicedoccompmap.invoiceid=?";
                params.add(invoiceId);
            }

            String mysqlQuery = "select invoicedocuments.docname  as docname,invoicedocuments.doctypeid as doctypeid,invoicedocuments.docid as docid "
                    + "from invoicedoccompmap inner join invoicedocuments on invoicedoccompmap.documentid=invoicedocuments.id " + conditionSQL;

            list = executeSQLQuery(mysqlQuery, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCustomerDAOImpl.getinvoiceDocuments:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    /**
     * @Desc : Get Address details
     * @param reqParams
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject getAddressDetailsFromCompanyId(Map<String, Object> reqParams) throws ServiceException {
        List param = new ArrayList();
        if (reqParams.containsKey("companyid") && reqParams.get("companyid") != null) {
            param.add((String) reqParams.get("companyid"));
        }

        String query = " From CompanyAddressDetails where company.companyID=? and isBillingAddress=true";
        List list = executeQuery(query, param.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject populateMasterInformation(Map<String, String> requestParams) throws ServiceException {
        List data = Collections.emptyList();
        try {

            ArrayList params = new ArrayList();
            StringBuilder hqlQuery = new StringBuilder();
            if (requestParams.containsKey("fetchColumn") && !StringUtil.isNullOrEmpty(requestParams.get("fetchColumn"))) {

                hqlQuery.append(" select " + requestParams.get("fetchColumn") + " ");

            }
            if (requestParams.containsKey("tableName") && !StringUtil.isNullOrEmpty(requestParams.get("tableName"))) {

                hqlQuery.append(" from " + requestParams.get("tableName") + " ");

            }
            if (requestParams.containsKey("companyColumn") && !StringUtil.isNullOrEmpty(requestParams.get("companyColumn"))) {

                hqlQuery.append(" where " + requestParams.get("companyColumn") + "= ? ");
                params.add(requestParams.get(Constants.companyKey));

            }

            if (requestParams.containsKey("condtionColumn") && !StringUtil.isNullOrEmpty(requestParams.get("condtionColumn"))) {

                hqlQuery.append(" and " + requestParams.get("condtionColumn") + "= ? ");
                params.add(requestParams.get("condtionColumnvalue"));

            }
            data = executeQuery(hqlQuery.toString(), params.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccountingHandlerDAOImpl.populateMasterInformation:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, data, data.size());
    }
    
    @Override
    public KwlReturnObject getAccountidforDummyAccount(HashMap<String, Object> dataMap) throws ServiceException {
        List list = null;
        int count = 0;
        try {

            ArrayList params = new ArrayList();
            params.add((String) dataMap.get(Constants.companyKey));

            String conditionSQL = " where company=?";

            String modulename = (String) dataMap.get("modulename");
            if (!StringUtil.isNullOrEmpty(modulename)) {
                conditionSQL += " and moduleid=?";
                params.add(modulename);
            }

            String mysqlQuery = "select detail from rest_client_details " + conditionSQL;

            list = executeSQLQuery(mysqlQuery, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccountingHandlerDAOImpl.getAccountidforDummyAccount:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public void setDefaultModuleTemplates(HashMap hm) throws ServiceException {
        try {
            if (hm.containsKey("templateId")) {
                String templateId = (String) hm.get("templateId");
                boolean isdefaulttemplate = (hm.containsKey("isdefaulttemplate")) ? Boolean.parseBoolean(hm.get("isdefaulttemplate").toString()) : false;
                ModuleTemplate templateObj = (ModuleTemplate) get(ModuleTemplate.class, templateId);
                templateObj.setIsdefaulttemplate(isdefaulttemplate);
                saveOrUpdate(templateObj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccountingHandlerDAOImpl.getAccountidforDummyAccount:" + ex.getMessage(), ex);
        }
    }
    
    /**
     * Get MultiEntity details
     * @param paramsMap
     * @return :KwlReturnObject of Multi Entity Details
     * @throws ServiceException
     */
    public KwlReturnObject getEntityDetails(Map<String, Object> paramsMap) throws ServiceException {
        ArrayList params = new ArrayList();
        List list = null;
        try {
            String condition = "";
            if (paramsMap.containsKey(Constants.companyid)) {
                params.add(paramsMap.get(Constants.companyid));
            }
            if (paramsMap.containsKey(Constants.multiEntityId) && paramsMap.get(Constants.multiEntityId) != null) {
                condition += " and multiEntity.id = ?";
                params.add(paramsMap.get(Constants.multiEntityId));
            }
            if (paramsMap.containsKey(Constants.multiEntityValue) && paramsMap.get(Constants.multiEntityValue) != null) {
                condition += " and multiEntity.value = ?";
                params.add(paramsMap.get(Constants.multiEntityValue));
            }
            String hql = "from MultiEntityMapping where company.companyID = ? " + condition;
            list = executeQuery(hql, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccountingHandlerDAOImpl.getEntityDetails:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getCompanyList(int offset,int limit) throws ServiceException {
        int listSize = 0;
        offset=offset-1;
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        try {
            String query = "select subdomain from company order by subdomain limit ? ,?";
            params.add(offset);
            params.add(limit);
            list = executeSQLQuery( query, params.toArray());
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }
    /*
    Get NewProductBatch object by passing Location,Warehouse,Row,Rack,Bin,Batch
    */
    @Override
    public NewProductBatch getERPProductBatch(Map<String, Object> requestParams) throws ServiceException {
        String product="";
        String store="";
        String location="";
        StoreMaster row=null;
        StoreMaster rack=null;
        StoreMaster bin=null;
        String batchName="";
        String companyId="";
        
        if(requestParams.containsKey("productid")){
            product=(String)requestParams.get("productid");
        }
        if(requestParams.containsKey("location")){
            location=(String)requestParams.get("location");
        }
        if(requestParams.containsKey("store")){
            store=(String)requestParams.get("store");
        }
        if(requestParams.containsKey("row")){
            row=(StoreMaster)requestParams.get("row");
        }
        if(requestParams.containsKey("rack")){
            rack=(StoreMaster)requestParams.get("rack");
        }
        
        if(requestParams.containsKey("bin")){
            bin=(StoreMaster)requestParams.get("bin");
        }
        
        if(requestParams.containsKey("batch")){
            batchName=(String)requestParams.get("batch");
        }
        
        if(requestParams.containsKey("companyid")){
            companyId=(String)requestParams.get("companyid");
        }
        
        if (StringUtil.isNullOrEmpty(batchName)) {
            batchName = "";
        }
        NewProductBatch productBatch = null;
        StringBuilder hql = new StringBuilder("FROM NewProductBatch WHERE company.companyID = ? AND product = ? AND warehouse.id = ? AND location.id = ? ");
        List params = new ArrayList();
        params.add(companyId);
        params.add(product);
        params.add(store);
        params.add(location);
        
        if (!StringUtil.isNullOrEmpty(batchName)) {
            hql.append(" AND batchname = ? ");
            params.add(batchName);
        } else {
            hql.append(" AND (batchname = '' OR batchname IS NULL )");
        }
        if (row != null) {
            hql.append(" AND row.id = ? ");
            params.add(row.getId());
        } else {
            hql.append(" AND row.id IS NULL ");
        }
        if (rack != null) {
            hql.append(" AND rack.id = ? ");
            params.add(rack.getId());
        } else {
            hql.append(" AND rack.id IS NULL ");
        }
        if (bin != null) {
            hql.append(" AND bin.id = ? ");
            params.add(bin.getId());
        } else {
            hql.append(" AND bin.id IS NULL ");
        }
        List list = executeQuery(hql.toString(), params.toArray());
        if (!list.isEmpty()) {
            productBatch = (NewProductBatch) list.get(0);
        }
        return productBatch;
    }
     /*
      Get Warehouse by passing Warehouse name.
     */
    @Override
    public String getStoreByTypes(Map<String, Object> requestParams) throws ServiceException{
        String storeID = "";
        String companyId="";
        String abbrev="";
        if(requestParams.containsKey("companyid")){
            companyId=(String)requestParams.get("companyid");
        }
        if(requestParams.containsKey("abbrev")){
            abbrev=(String)requestParams.get("abbrev");
        }
        StringBuilder sqlQuery = new StringBuilder("select id FROM in_storemaster s ");
        sqlQuery.append("WHERE s.company = ? and s.abbrev =? and s.type IN ('0','1','2') ");
        List params = new ArrayList();
        params.add(companyId);
        params.add(abbrev);
        List list = executeSQLQuery(sqlQuery.toString(), params.toArray());
        if (!list.isEmpty()) {
            storeID = (String) list.get(0);
        }
        return storeID;
    }
    /**
     * Format double value to remove trailing zeros. 
     * Example: 0.2500 -> 0.25, 1.0026500 -> 1.0265, 1.0 -> 1, 0.1260000 -> 0.126
     * 
     * @param double 
     * @return String
     */
    @Override
    public String formatDouble(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        } else {
            return String.format("%s", d);
        }
    }
    @Override
    public JSONObject getUserPermissionForFeature(HashMap<String, Object> params) throws ServiceException {
        JSONObject returnJobj = new JSONObject();
        try {
            JSONObject fjobj = new JSONObject();
            JSONObject ujobj = new JSONObject();

            String userId = StringUtil.isNullOrEmpty((String) params.get("userid")) ? "" : params.get("userid").toString();
            
            KwlReturnObject kmsg = permissionHandlerDAOObj.getActivityFeature();
            Iterator ite = kmsg.getEntityList().iterator();
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                String fName = ((ProjectFeature) row[0]).getFeatureName();
                ProjectActivity activity = (ProjectActivity) row[1];
                if (!fjobj.has(fName)) {
                    fjobj.put(fName, new JSONObject());
                }
                JSONObject temp = fjobj.getJSONObject(fName);
                if (activity != null) {
                    temp.put(activity.getActivityName(), (int) Math.pow(2, temp.length()));
                }
            }

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("userid", userId);
            KwlReturnObject krObj = permissionHandlerDAOObj.getUserPermission(requestParams);
            ite = krObj.getEntityList().iterator();
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                ujobj.put(row[0].toString(), row[1]);
            }
            returnJobj.put("Perm", fjobj);
            returnJobj.put("UPerm", ujobj);
        } catch (Exception e) {
            throw ServiceException.FAILURE("getUserPermissionForSpecificFeature : " + e.getMessage(), e);
        }
        return returnJobj;
    }
}
