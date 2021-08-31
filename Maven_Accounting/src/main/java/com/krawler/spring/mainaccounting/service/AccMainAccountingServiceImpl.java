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


import com.itextpdf.text.DocumentException;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.DefaultHeader;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.FieldConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.account.accAccountHandler;
import com.krawler.spring.accounting.account.accVendorCustomerProductDAO;
import com.krawler.spring.accounting.currency.accCurrencyController;
import com.krawler.spring.accounting.customDesign.CustomDesignDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.tax.accTaxController;
import com.krawler.spring.accounting.term.accTermController;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.CommonFnController;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.mortbay.jetty.handler.DefaultHandler;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;



/**
 *
 * @author krawler
 */
public class AccMainAccountingServiceImpl implements AccMainAccountingService{
    private accInvoiceCMN accInvoiceCommon;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accAccountDAO accAccountDAOobj;
    private accProductDAO accProductObj;
    private accVendorCustomerProductDAO accVendorCustomerProductDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accTaxController AccTaxcontrollerObj;
    private accProductController AccProductcontrollerObj;
    private accTermController AccTermcontrollerObj;
    private accCurrencyController AccCurrencycontrollerObj;
     private CustomDesignDAO customDesignDAOObj;

      public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
      public accVendorCustomerProductDAO getAccVendorCustomerProductDAOobj() {
        return accVendorCustomerProductDAOobj;
    }

    public void setAccVendorCustomerProductDAOobj(accVendorCustomerProductDAO accVendorCustomerProductDAOobj) {
        this.accVendorCustomerProductDAOobj = accVendorCustomerProductDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccTaxcontroller(accTaxController accTaxControllerObj) {
        this.AccTaxcontrollerObj = accTaxControllerObj;
    }
     public void setaccProductcontroller(accProductController accProductControllerObj) {
        this.AccProductcontrollerObj = accProductControllerObj;
    }
      public void setaccTermcontroller(accTermController accTermControllerObj) {
        this.AccTermcontrollerObj = accTermControllerObj;
    }
    public void setaccCurrencycontroller(accCurrencyController accCurrencyControllerObj) {
        this.AccCurrencycontrollerObj = accCurrencyControllerObj;
    }
    
    public void setcustomDesignDAO(CustomDesignDAO customDesignDAOObj) {
        this.customDesignDAOObj = customDesignDAOObj;
    }
     public JSONObject getAccountsForCombo(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
         try{
            JSONObject paramJObj = new JSONObject();
            try{Enumeration<String> attributes = request.getAttributeNames();
            while(attributes.hasMoreElements()){
                String attribute = attributes.nextElement();            
                paramJObj.put(attribute, request.getAttribute(attribute));
            }
//            System.out.println("attributes ended");
            Enumeration<String> parameters = request.getParameterNames();
            while(parameters.hasMoreElements()){
                String parameter = parameters.nextElement();
                paramJObj.put(parameter, request.getParameter(parameter));
            }
            }
            catch(JSONException e){
                e.printStackTrace();
            }
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userDateFormat = sessionHandlerImpl.getUserDateFormat(request);
            paramJObj.put(Constants.companyKey, companyid);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            paramJObj.put("currencyid", currencyid);
            paramJObj.put("gcurrencyid", currencyid);
            paramJObj.put("userdateformat", userDateFormat);
            jobj = getAccountsForComboJson(paramJObj);            
        }
        catch(JSONException ex){
            Logger.getLogger(AccMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);                        
        }
          catch (SessionExpiredException ex) {
            Logger.getLogger(AccMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch(Exception ex) {
            Logger.getLogger(AccMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;         
    }
    
     
    public JSONObject getAccountsForComboJson(JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException{
        JSONObject jobj = new JSONObject();     
        JSONArray jArr=new JSONArray();
        
        boolean headerAdded=false;
        boolean consolidateAccMapFlag=false;
        boolean levelFlag=false;
        boolean isFixedAsset=false;
        boolean issuccess = false;
        
            consolidateAccMapFlag =Boolean.parseBoolean(paramJobj.optString("consolidateAccMapFlag","false"));
            levelFlag =Boolean.parseBoolean(paramJobj.optString("levelFlag","false"));
            String companyid = paramJobj.getString(Constants.companyKey);
            HashMap<String, Object> requestParams = accAccountHandler.getJsonMap(paramJobj);
            requestParams.put("templateid", paramJobj.optString("templateid",null));//  Custom Layout - filter accounts if already mapped in the selected template
            KwlReturnObject result = accAccountDAOobj.getAccountsForCombo(requestParams);
            List list = result.getEntityList();
            if(requestParams.containsKey("headerAdded")){headerAdded =Boolean.parseBoolean((String)requestParams.get("headerAdded"));}
            if(requestParams.containsKey("isFixedAsset")){isFixedAsset =Boolean.parseBoolean((String)requestParams.get("isFixedAsset"));}
            boolean ignoreCustomers=requestParams.get("ignorecustomers")!=null;
            boolean ignoreVendors=requestParams.get("ignorevendors")!=null;
            boolean ignoreTransactionFlag=paramJobj.optString("ignoreTransactionFlag",null)!=null?Boolean.parseBoolean(paramJobj.getString("ignoreTransactionFlag")):true;
                    
            String excludeaccountid = (String) requestParams.get("accountid");
            String includeaccountid = (String) requestParams.get("includeaccountid");
            String includeparentid = (String) requestParams.get("includeparentid");
//            String customerCpath = ConfigReader.getinstance().get("Customer");
//            String vendorCpath = ConfigReader.getinstance().get("Vendor");
//            
//            boolean isCustomers=requestParams.get("isCustomer")!=null?Boolean.parseBoolean(request.getParameter("isCustomer")):true;
//            boolean isVendors=requestParams.get("isVendor")!=null?Boolean.parseBoolean(request.getParameter("isVendor")):true;
//            boolean deleted =Boolean.parseBoolean((String)requestParams.get("deleted"));
//            boolean nondeleted =Boolean.parseBoolean((String)requestParams.get("nondeleted"));
            String currencyid=(String)requestParams.get("currencyid");
            KWLCurrency currency = (KWLCurrency)kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.KWLCurrency", currencyid);
            
            Iterator itr = list.iterator();
//            int level=0;
            while (itr.hasNext()) {
                Object[] listObj =(Object[]) itr.next();
                String accountid = listObj!= null ? listObj[0].toString() : "";
                Account account = (Account) kwlCommonTablesDAOObj.getClassObject(Account.class.getName(), accountid);
                if(excludeaccountid!=null&&account.getID().equals(excludeaccountid)) continue;
                if((includeparentid!=null&&(!account.getID().equals(includeparentid)||(account.getParent()!=null&&!account.getParent().getID().equals(includeparentid))))) continue;
                else if((includeaccountid!=null&&!account.getID().equals(includeaccountid))) continue;
                    
//                Object c = kwlCommonTablesDAOObj.getClassObject(customerCpath, account.getID());
//                if(ignoreCustomers&&account.getGroup().getID().equals(Group.ACCOUNTS_RECEIVABLE)){
//                    if(c!=null)continue;
//                }
//
//                Object v = kwlCommonTablesDAOObj.getClassObject(vendorCpath, account.getID());
//                if(ignoreVendors&&account.getGroup().getID().equals(Group.ACCOUNTS_PAYABLE)){
//                    if(v!=null)continue;
//                }
//                
//                if ((c != null || v != null) && isCOA) {
//                    continue;
//                }

                if (!consolidateAccMapFlag && headerAdded && !isFixedAsset && ignoreTransactionFlag) {
                    KwlReturnObject Count = accAccountDAOobj.getJEDTrasactionfromAccount(account.getID(), companyid);
                    int jedCount = Count.getRecordTotalCount();
                    // calculation of opening balance                
                    double openbalance = accInvoiceCommon.getOpeningBalanceOfAccountJson(paramJobj,account,false,null);
                    double openingBal=openbalance;//account.getOpeningBalance();
                    if (!(jedCount==0 && openingBal==0)) {
                        continue;
                    }
                }                              
                JSONObject obj = new JSONObject();
                obj.put("accid", account.getID());
                obj.put("accname", account.getName());
                obj.put("accountpersontype", 0);
                obj.put("mappedaccountid", account.getID());
                obj.put("groupid", account.getGroup().getID());
                obj.put("mastergroupid", (account.getGroup()!=null)?account.getGroup().getID():"");
                obj.put("groupname", account.getGroup().getName());
                obj.put("acccode", account.getAcccode());
                obj.put("accnamecode", (!StringUtil.isNullOrEmpty(account.getAcccode())?"["+account.getAcccode()+"] "+account.getName():account.getName()));
                obj.put("nature", account.getGroup().getNature());
                obj.put("naturename",(account.getGroup().getNature()==Constants.Liability)?"Liability":(account.getGroup().getNature()==Constants.Asset)?"Asset":(account.getGroup().getNature()==Constants.Expences)?"Expences":(account.getGroup().getNature()==Constants.Income)?"Income":"");
                obj.put("currencyid",(account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol",(account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getSymbol()));
                obj.put("currencyname",(account.getCurrency()==null?currency.getName(): account.getCurrency().getName()));
                obj.put("currencycode",(account.getCurrency()==null?currency.getCurrencyCode(): account.getCurrency().getCurrencyCode()));
                obj.put("deleted", account.isDeleted());
                obj.put("aliascode", account.getAliascode()==null?"":account.getAliascode());
                if(account.getAcccode()!=null)
                    obj.put("acccode", account.getAcccode());
                else
                    obj.put("acccode","");
//                if (c != null || v != null) {
//                    obj.put("isOnlyAccount", "false");
//                } else {
                    obj.put("isOnlyAccount", "true");
//                }
                if(levelFlag) {
                    int level = 0;                    
                    level = getAccountLevel(account, level);
                    obj.put("level", level);
                }
//                obj.put("depreciationaccount", account.getDepreciationAccont()==null?"":account.getDepreciationAccont().getID());
//                obj.put("openbalance", account.getOpeningBalance());
//                Account parentAccount = (Account) row[6];
//                if(parentAccount!=null){
//                    obj.put("parentid", parentAccount.getID());
//                    obj.put("parentname", parentAccount.getName());
//                }
//                obj.put("level", row[3]);
//                obj.put("leaf", row[4]);
//                obj.put("presentbalance", account.getPresentValue());
//                obj.put("life", account.getLife());
//                obj.put("salvage", account.getSalvage());
//                obj.put("posted", row[7]);
//                obj.put("creationDate", authHandler.getDateFormatter(request).format(account.getCreationDate()));
//                obj.put("categoryid", account.getCategory()==null?"":account.getCategory().getID());
                jArr.put(obj);                    
            } 
        
         if(!ignoreCustomers){   
            result = accAccountDAOobj.getCustomerForCombo(requestParams);
            List ls = result.getEntityList();
            Iterator<Object[]> itr1 = ls.iterator();
            while (itr1.hasNext()) {
                Object[] row = (Object[]) itr1.next();
                String customerid = (String) row[0].toString();
                String customername = (String) row[1].toString();
                String accountid = (String) row[2].toString();
                String customercode = (row[3]!=null)?(String) row[3].toString():"";
                Account account = (Account) kwlCommonTablesDAOObj.getClassObject(Account.class.getName(), accountid);

                if (excludeaccountid != null && customerid.equals(excludeaccountid)) {
                    continue;
    }

                JSONObject obj = new JSONObject();
                obj.put("accid", customerid);
                obj.put("accname", customername);
                obj.put("groupid", account.getGroup().getID());
                obj.put("accountpersontype", 1);
                obj.put("mappedaccountid", account.getID());
                obj.put("mastergroupid", (account.getGroup()!=null)?account.getGroup().getID():"");
                obj.put("groupname", account.getGroup().getName());
                obj.put("acccode", customercode);
                obj.put("accnamecode", (!StringUtil.isNullOrEmpty(account.getAcccode()) ? "[" + account.getAcccode() + "] " + account.getName() : account.getName()));
                obj.put("nature", account.getGroup().getNature());
                obj.put("naturename", (account.getGroup().getNature() == Constants.Liability) ? "Liability" : (account.getGroup().getNature() == Constants.Asset) ? "Asset" : (account.getGroup().getNature() == Constants.Expences) ? "Expences" : (account.getGroup().getNature() == Constants.Income) ? "Income" : "");
                obj.put("currencyid", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getSymbol()));
                obj.put("currencyname", (account.getCurrency() == null ? currency.getName() : account.getCurrency().getName()));
                obj.put("currencycode", (account.getCurrency() == null ? currency.getCurrencyCode() : account.getCurrency().getCurrencyCode()));
                obj.put("deleted", account.isDeleted());
                obj.put("aliascode", account.getAliascode() == null ? "" : account.getAliascode());

                jArr.put(obj);
                }
         }
          if(!ignoreVendors){  
            result = accAccountDAOobj.getVendorForCombo(requestParams);
            List ls  = result.getEntityList();
            Iterator<Object[]> itr1 = ls.iterator();
            while (itr1.hasNext()) {
                Object[] row = (Object[]) itr1.next();
                String vendorid = (String) row[0].toString();
                String vendorname = (String) row[1].toString();
                String accountid = (String) row[2].toString();
                 String vendorcode = (row[3]!=null)?(String) row[3].toString():"";
                Account account = (Account) kwlCommonTablesDAOObj.getClassObject(Account.class.getName(), accountid);
    
                if (excludeaccountid != null && vendorid.equals(excludeaccountid)) {
                    continue;
                }

                JSONObject obj = new JSONObject();
                obj.put("accid", vendorid);
                obj.put("accname", vendorname);
                obj.put("mappedaccountid", account.getID());
                obj.put("groupid", account.getGroup().getID());
                obj.put("mastergroupid", (account.getGroup()!=null)?account.getGroup().getID():"");
                obj.put("groupname", account.getGroup().getName());
                obj.put("acccode", vendorcode);
                obj.put("accountpersontype", 2);
                obj.put("accnamecode", (!StringUtil.isNullOrEmpty(account.getAcccode()) ? "[" + account.getAcccode() + "] " + account.getName() : account.getName()));
                obj.put("nature", account.getGroup().getNature());
                obj.put("naturename", (account.getGroup().getNature() == Constants.Liability) ? "Liability" : (account.getGroup().getNature() == Constants.Asset) ? "Asset" : (account.getGroup().getNature() == Constants.Expences) ? "Expences" : (account.getGroup().getNature() == Constants.Income) ? "Income" : "");
                obj.put("currencyid", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getSymbol()));
                obj.put("currencyname", (account.getCurrency() == null ? currency.getName() : account.getCurrency().getName()));
                obj.put("currencycode", (account.getCurrency() == null ? currency.getCurrencyCode() : account.getCurrency().getCurrencyCode()));
                obj.put("deleted", account.isDeleted());
                obj.put("aliascode", account.getAliascode() == null ? "" : account.getAliascode());

                jArr.put(obj);
            }
          }  
            
            jobj.put("data", jArr);
            jobj.put("totalCount", jArr.length());
            jobj.put("success", true);
        
        return jobj;
    }
    
    public JSONObject getAccountsIdNameJson(JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException{
        JSONObject jobj = new JSONObject();     
        JSONArray jArr=new JSONArray();
        HashMap<String, Object> requestParams = accAccountHandler.getJsonMap(paramJobj);
        requestParams.put("templateid", paramJobj.optString("templateid",null));//  Custom Layout - filter accounts if already mapped in the selected template
        KwlReturnObject result = accAccountDAOobj.getAccountsForCombo(requestParams);
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] row = (Object[]) itr.next();

            JSONObject obj = new JSONObject();
            obj.put("accid", (String) row[0].toString());
            obj.put("accname", row[1] != null && !StringUtil.isNullOrEmpty(row[1].toString()) ? (String) row[1].toString() : (row[2] != null ? (String) row[2].toString() : ""));
            obj.put("acccode", row[2] != null ? (String) row[2].toString() : "");
            jArr.put(obj);                    
        } 
        jobj.put("data", jArr);
        jobj.put("totalCount", jArr.length());
        jobj.put("success", true);
        
        return jobj;
    }
    
    public int getAccountLevel(Account account, int level) throws ServiceException {
        if (account.getParent() != null) {
            level++;
            level = getAccountLevel(account.getParent(), level);
        }
        return level;
    }

    public JSONObject getCustomCombodata(HttpServletRequest request, HttpServletResponse response) {
        KwlReturnObject result = null;
         JSONObject jresult = new JSONObject();
        String fieldid = request.getParameter(FieldConstants.Crm_fieldid);
        String flag = request.getParameter(FieldConstants.Crm_flag);
        boolean isFormPanel=false;
        if(!StringUtil.isNullOrEmpty(request.getParameter("isFormPanel")))
            isFormPanel =Boolean.parseBoolean(request.getParameter("isFormPanel"));
        String jsonview = flag != null ? "jsonView" : "jsonView-ex";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        try {
            Integer colcount = 1;
            requestParams.put(Constants.filter_names, Arrays.asList(FieldConstants.Crm_fieldid, FieldConstants.Crm_deleteflag));
            requestParams.put(Constants.filter_values, Arrays.asList(fieldid, 0));
            ArrayList order_by = new ArrayList();
            ArrayList order_type = new ArrayList();
            order_by.add("itemsequence");
            order_type.add("asc");
            requestParams.put("order_by", order_by);
            requestParams.put("order_type", order_type);
            result = accAccountDAOobj.getCustomCombodata(requestParams);
            List lst = result.getEntityList();
            colcount = lst.size();
            Iterator ite = lst.iterator();
            if(isFormPanel){
                JSONObject jobjTemp = new JSONObject();
                jobjTemp.put(FieldConstants.Crm_id, "1234");
                jobjTemp.put(FieldConstants.Crm_name, "None");
                jresult.append(Constants.data, jobjTemp);
            }
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                FieldComboData tmpcontyp = (FieldComboData) row[0];
                JSONObject jobjTemp = new JSONObject();
                jobjTemp.put(FieldConstants.Crm_id, tmpcontyp.getId());
                jobjTemp.put(FieldConstants.Crm_name, tmpcontyp.getValue());
                FieldComboData parentItem = (FieldComboData) row[3];
                if (parentItem != null) {
                    jobjTemp.put("parentid", parentItem.getId());
                    jobjTemp.put("parentname", parentItem.getValue());
                }
                jobjTemp.put("level", row[1]);
                jobjTemp.put("leaf", row[2]);


                jresult.append(Constants.data, jobjTemp);
            }
//            if (colcount == 0) {
//                jresult.put(Constants.data, new com.krawler.utils.json.JSONArray());
//            }
//            jresult.put("valid", true);
            jresult.put(Constants.RES_success, result.isSuccessFlag());
        } catch (JSONException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jresult;
    }      

    public JSONObject getFieldParams(HttpServletRequest request, HttpServletResponse response) {
         KwlReturnObject result = null;
        JSONObject jresult = new JSONObject();
        int lineitem = StringUtil.isNullOrEmpty(request.getParameter(Constants.customcolumn)) ? 0 : Integer.parseInt(request.getParameter(Constants.customcolumn)) ;
        String module = request.getParameter(Constants.moduleid);
        if (module.equalsIgnoreCase("" + Constants.CUSTOMER_MODULE_UUID)) {
            module = ""+Constants.Acc_Customer_ModuleId;
        }
        String jeId = request.getParameter("jeId");
        boolean requestForCRM=StringUtil.isNullOrEmpty(request.getParameter("isCRM"))?false:Boolean.parseBoolean(request.getParameter("isCRM"));
        String[] moduleidarray = request.getParameterValues(Constants.moduleidarray);
        boolean isForProductCustomFieldHistoryCombo = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isForProductCustomFieldHistoryCombo"))) {
            isForProductCustomFieldHistoryCombo = Boolean.parseBoolean(request.getParameter("isForProductCustomFieldHistoryCombo"));
        }
        String commaSepratedModuleids = "";
        if (moduleidarray != null) {
            for (int i = 0; i < moduleidarray.length; i++) {
                if (!StringUtil.isNullOrEmpty(moduleidarray[i])) {
                    commaSepratedModuleids += moduleidarray[i] + ",";
                }
            }
            if (moduleidarray.length > 1) {
                commaSepratedModuleids = commaSepratedModuleids.substring(0, commaSepratedModuleids.length() - 1);
            }
        }
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        try {
            Date currentDate = authHandler.getDateFormatter(request).parse(authHandler.getDateFormatter(request).format(new Date()));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Integer colcount = 1;
            if (StringUtil.isNullOrEmpty(commaSepratedModuleids) && StringUtil.isNullOrEmpty(module)) {
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid,Constants.customcolumn));
                requestParams.put(Constants.filter_values, Arrays.asList(companyid,lineitem));
            } else if (StringUtil.isNullOrEmpty(commaSepratedModuleids)) {
                Integer moduleid = Integer.parseInt(module);
               if(moduleid>99){  //Added module >100 for Report like ledger, Balance sheet etc
               if(moduleid==101 || moduleid == 100 || moduleid==102){    
                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid));
                        requestParams.put(Constants.filter_values, Arrays.asList(companyid));
               }else{
                   requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid,Constants.customcolumn));
                   requestParams.put(Constants.filter_values, Arrays.asList(companyid,lineitem));
                    }
               }else{
                   /**
                    * while sync Map field under Product Master from CRM. 
                    */
                   if (requestForCRM && module.equalsIgnoreCase("" + Constants.Acc_Product_Master_ModuleId)) {
                       requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                       requestParams.put(Constants.filter_values, Arrays.asList(companyid, Integer.parseInt(module)));
                   } else {
                       requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                       requestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, lineitem));
                   }
               }
            } else {
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.INmoduleid,Constants.customcolumn));
                    requestParams.put(Constants.filter_values, Arrays.asList(companyid, commaSepratedModuleids,lineitem));
                }
            if(!StringUtil.isNullOrEmpty(request.getParameter("iscustomfield"))){
                requestParams.put("customfield",1);
            }
            if (requestForCRM && module.equalsIgnoreCase("" + Constants.Acc_Customer_Quotation_ModuleId)) {
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                requestParams.put(Constants.filter_values, Arrays.asList(companyid, Integer.parseInt(module)));
                
                /* Sending extra parameter,
                
                 If Custom column added for Product module is also available 
                
                 for Customer Quotation at line level*/
                
                requestParams.put("ProductrelatedModuleid", module);
                requestParams.put("companyId", companyid);
            }
            Integer moduleidint=0;
            if(!StringUtil.isNullOrEmpty(module)){
                moduleidint = Integer.parseInt(module);
            }
            if ( moduleidint==100 || moduleidint == 101 || moduleidint==102) {//Used this query to club the same name dimension/Custom fields
                result = accAccountDAOobj.getFieldParamsUsingSql(requestParams);
            } else {
                result = accAccountDAOobj.getFieldParams(requestParams);
            }
            List lst = result.getEntityList();
            colcount = lst.size();
            AccJECustomData accBillInvCustomData = null;
            AccProductCustomData accProductCustomData = null;
            AccountCustomData accountCustomData = null;
            CustomerCustomData accCustomerCustomData = null;
            VendorCustomData accVendorCustomData = null;
            DeliveryOrderCustomData deliveryOrderCustomData= null;
            KwlReturnObject custumObjresult = null;
            Iterator ite = lst.iterator();
            while (ite.hasNext()) {
                FieldParams tmpcontyp = null;
                if (moduleidint==100 ||moduleidint == 101 || moduleidint==102) {
                    Object[] temp = (Object[]) ite.next();
                    KwlReturnObject fieldParamObj = null;
                    try {
                        fieldParamObj = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), temp[1].toString());
                        tmpcontyp = (FieldParams) fieldParamObj.getEntityList().get(0);
                    } catch (ServiceException ex) {
                        Logger.getLogger(AccMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                } else {
                    tmpcontyp = (FieldParams) ite.next();
                }
                if (isForProductCustomFieldHistoryCombo && !(tmpcontyp.getFieldtype() == 1 || tmpcontyp.getFieldtype() == 2)) {
                    continue;
                }
                JSONObject jobj = new JSONObject();
                jobj.put("fieldname", tmpcontyp.getFieldname());
                if (!StringUtil.isNullOrEmpty(jeId)) {
                    Integer moduleid = Integer.parseInt(module);
                    try {
                        if(moduleid ==34){
                            custumObjresult = accountingHandlerDAOobj.getObject(AccountCustomData.class.getName(), jeId);
                       } else if(moduleid<30) {
                            switch (moduleid) {
                               case 18 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(PurchaseOrderCustomData.class.getName(), jeId);
                                    break;
                               case 20 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(SalesOrderCustomData.class.getName(), jeId);
                                    break;
                               case 22 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(QuotationCustomData.class.getName(), jeId);
                                    break;
                               case 23 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(VendorQuotationCustomData.class.getName(), jeId);
                                    break;
                               case 24 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
                                    break;
                               case 25 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(CustomerCustomData.class.getName(), jeId);
                                    break;
                               case 26 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(VendorCustomData.class.getName(), jeId);
                                    break;
                               case 27 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(DeliveryOrderCustomData.class.getName(), jeId);
                                    break;
                               case 28 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(GoodsReceiptOrderCustomData.class.getName(), jeId);
                                    break;
                               case 29 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(SalesReturnCustomData.class.getName(), jeId);
                                    break;
                               default :     
                                    custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
                            }
                        } else if(moduleid==31){
                            custumObjresult = accountingHandlerDAOobj.getObject(PurchaseReturnCustomData.class.getName(), jeId);
                        } else if(moduleid==32){
                            custumObjresult = accountingHandlerDAOobj.getObject(PurchaseRequisitionCustomData.class.getName(), jeId);
                        } else {
                            custumObjresult = accountingHandlerDAOobj.getObject(AccProductCustomData.class.getName(), jeId);
                        }
                        /*if(moduleid==25 || moduleid==26){
                        custumObjresult = accountingHandlerDAOobj.getObject(AccountCustomData.class.getName(), jeId);
                        }  else if(moduleid == 27) {
                        custumObjresult = accountingHandlerDAOobj.getObject(DeliveryOrderCustomData.class.getName(), jeId); 
                        } else{ 
                        if(moduleid<30)
                        custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
                        else
                        custumObjresult = accountingHandlerDAOobj.getObject(AccProductCustomData.class.getName(), jeId);
                        } */
                    } catch (Exception e) {
                    }
                    if(moduleid == 25){
                     accCustomerCustomData = (CustomerCustomData) custumObjresult.getEntityList().get(0);
                        if (accCustomerCustomData != null) {
                            String coldata = accCustomerCustomData.getCol(tmpcontyp.getColnum());
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                jobj.put("fieldData", coldata);
                            }
                        }
                    } else  if(moduleid == 26){
                     accVendorCustomData = (VendorCustomData) custumObjresult.getEntityList().get(0);
                        if (accVendorCustomData != null) {
                            String coldata = accVendorCustomData.getCol(tmpcontyp.getColnum());
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                jobj.put("fieldData", coldata);
                            }
                        }
                    } else if (moduleid ==34) {
                        accountCustomData = (AccountCustomData) custumObjresult.getEntityList().get(0);
                        if (accountCustomData != null) {
                            String coldata = accountCustomData.getCol(tmpcontyp.getColnum());
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                jobj.put("fieldData", coldata);
                            }
                        }
                    } else if(moduleid == 27){
                        deliveryOrderCustomData = (DeliveryOrderCustomData) custumObjresult.getEntityList().get(0);
                        if (deliveryOrderCustomData != null) {
                            String coldata = deliveryOrderCustomData.getCol(tmpcontyp.getColnum());
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                jobj.put("fieldData", coldata);
                            }
                        }
                    } else {
                        if (moduleid < 30) {
                            if(moduleid==18) {
                                PurchaseOrderCustomData purchaseOrderCustomData = (PurchaseOrderCustomData) custumObjresult.getEntityList().get(0);
                                if (purchaseOrderCustomData != null) {
                                    String coldata = purchaseOrderCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            } else if(moduleid==20) {
                                SalesOrderCustomData salesOrderCustomData = (SalesOrderCustomData) custumObjresult.getEntityList().get(0);
                                if (salesOrderCustomData != null) {
                                    String coldata = salesOrderCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            } else if(moduleid==22) {
                                QuotationCustomData quotationCustomData = (QuotationCustomData) custumObjresult.getEntityList().get(0);
                                if (quotationCustomData != null) {
                                    String coldata = quotationCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            } else if(moduleid==23) {
                                VendorQuotationCustomData vendorQuotationCustomData = (VendorQuotationCustomData) custumObjresult.getEntityList().get(0);
                                if (vendorQuotationCustomData != null) {
                                    String coldata = vendorQuotationCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            } else if(moduleid==27) {
                                deliveryOrderCustomData = (DeliveryOrderCustomData) custumObjresult.getEntityList().get(0);
                                if (deliveryOrderCustomData != null) {
                                    String coldata = deliveryOrderCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            } else if(moduleid==28) {
                                GoodsReceiptOrderCustomData goodsReceiptOrderCustomData = (GoodsReceiptOrderCustomData) custumObjresult.getEntityList().get(0);
                                if (goodsReceiptOrderCustomData != null) {
                                    String coldata = goodsReceiptOrderCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            } else if(moduleid==29) {
                                SalesReturnCustomData salesReturnCustomData = (SalesReturnCustomData) custumObjresult.getEntityList().get(0);
                                if (salesReturnCustomData != null) {
                                    String coldata = salesReturnCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            } else {
                                accBillInvCustomData = (AccJECustomData) custumObjresult.getEntityList().get(0);
                                if (accBillInvCustomData != null) {
                                    String coldata = accBillInvCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            }
                        }else if(moduleid==32) {
                                PurchaseRequisitionCustomData purchaseRequisitionCustomData = (PurchaseRequisitionCustomData) custumObjresult.getEntityList().get(0);
                                if (purchaseRequisitionCustomData != null) {
                                    String coldata = purchaseRequisitionCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            }
                        else if (moduleid == 31) {
                            PurchaseReturnCustomData purchaseReturnCustomData = (PurchaseReturnCustomData) custumObjresult.getEntityList().get(0);
                            if (purchaseReturnCustomData != null) {
                                String coldata = purchaseReturnCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        } else if (moduleid == 30) {
                            accProductCustomData = (AccProductCustomData) custumObjresult.getEntityList().get(0);
                            if (accProductCustomData != null) {
                                String coldata = accProductCustomData.getCol(tmpcontyp.getColnum());
                                Object fieldValueObject = getProductCustomFieldValue(tmpcontyp.getId(), accProductCustomData.getProductId(), companyid, currentDate);
                                String latestValue = "";
                                if (fieldValueObject != null) {
                                    latestValue = (String) fieldValueObject;
                                }
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    if (latestValue.equalsIgnoreCase(coldata) || StringUtil.isNullOrEmpty(latestValue)) {
                                        jobj.put("fieldData", coldata);
                                    } else {
                                        jobj.put("fieldData", latestValue);
                                    }
                                }
                            }
                        } else {
                            accProductCustomData = (AccProductCustomData) custumObjresult.getEntityList().get(0);
                            if (accProductCustomData != null) {
                                String coldata = accProductCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        }
                  }
                }
                if (StringUtil.isNullOrEmpty(module)) {
                    if (lineitem == 0) {
                        if (!StringUtil.isNullOrEmpty(request.getParameter("customfieldlableflag"))) {  //[mayur B] is custom field flag
                            jobj.put("fieldlabel", tmpcontyp.getFieldlabel());
                        } else {
                            jobj.put("fieldlabel", tmpcontyp.getFieldlabel() + "(" + getModuleName(tmpcontyp.getModuleid()) + ")");
                        }
                    } else {
                        jobj.put("fieldlabel", tmpcontyp.getFieldlabel());
                    }
                } else {
                    Integer moduleid = Integer.parseInt(module);
                    if (moduleid > 99) {
                        if(moduleid==100 || moduleid==101 || moduleid==102)
                            jobj.put("fieldlabel", tmpcontyp.getFieldlabel());
                        else
                        jobj.put("fieldlabel", tmpcontyp.getFieldlabel() + "(" + getModuleName(tmpcontyp.getModuleid()) + ")");
                    } else {
                        jobj.put("fieldlabel", tmpcontyp.getFieldlabel());
                    }
                }
                if (requestForCRM && (Integer.parseInt(module) == Constants.Acc_Customer_Quotation_ModuleId || Integer.parseInt(module) == Constants.Acc_Product_Master_ModuleId) && (tmpcontyp.isFieldOfGivenGSTConfigType(Constants.IsForGSTRuleMapping) || tmpcontyp.isFieldOfGivenGSTConfigType(Constants.isformultientity) || tmpcontyp.getFieldlabel().equals(Constants.GSTProdCategory))) {
                    jobj.put("isGSTField", true);
                    jobj.put("erpDefaultHeader", tmpcontyp.getFieldlabel());
                }
                jobj.put("isessential", tmpcontyp.getIsessential());
                jobj.put("maxlength", tmpcontyp.getMaxlength());
                jobj.put("validationtype", tmpcontyp.getValidationtype());
                jobj.put("fieldid", tmpcontyp.getId());
                jobj.put("moduleid", tmpcontyp.getModuleid());
                jobj.put("fieldtype", tmpcontyp.getFieldtype());
                jobj.put("iseditable", tmpcontyp.getIseditable());
                jobj.put("comboid", tmpcontyp.getComboid());
                jobj.put("comboname", tmpcontyp.getComboname());
                jobj.put("moduleflag", tmpcontyp.getModuleflag());
                jobj.put("parentid", tmpcontyp.getParentid());
                try {
                     requestParams.put("parentid",tmpcontyp.getId());
                    jobj.put("childstr", getChildString(requestParams));
                } catch (ServiceException ex) {
                    Logger.getLogger(AccMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                jobj.put("refcolumn_number", Constants.Custom_Column_Prefix + tmpcontyp.getRefcolnum());
                jobj.put("column_number", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                jobj.put("sendnotification", tmpcontyp.getsendNotification());
                jobj.put("notificationdays", tmpcontyp.getnotificationDays());
                jobj.put("iscustomfield", tmpcontyp.getCustomfield()==1 ? true : false);
                /**
                 * if getCustomField == 0 then its Dimension field
                 */
                jobj.put("isdimentionfield", (tmpcontyp.getCustomfield()==0));
                jobj.put("iscustomcolumn", (tmpcontyp.getCustomcolumn()==0 || (tmpcontyp.getCustomcolumn()==1 && tmpcontyp.getCustomfield()==0)) ? false : true);
                
                /*If Product custom field is also available for CQ at line level
                 then we sending "iscustomcolumn" true to CRM as all other line level custom field
                 */
                if((tmpcontyp.getModuleid() == Constants.Acc_Product_Master_ModuleId && tmpcontyp.getRelatedmoduleid()!=null && tmpcontyp.getRelatedmoduleid().contains("22"))){
                 jobj.put("iscustomcolumn",  true);   
                }
                jresult.append("data", jobj);
            }
            if (!StringUtil.isNullOrEmpty(commaSepratedModuleids)) {
                requestParams.clear();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                requestParams.put(Constants.filter_values, Arrays.asList(companyid, 30));
                result = accAccountDAOobj.getFieldParams(requestParams);
                lst = result.getEntityList();
                colcount += lst.size();
                jresult = getProductFieldParams(lst, jresult);
            }
            if (colcount == 0) {
                jresult.put("data", new com.krawler.utils.json.JSONArray());
            }
            jresult.put("valid", true);
        } catch (ParseException ex) {
            Logger.getLogger(AccMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jresult.put("success", result.isSuccessFlag());
                jresult.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        return jresult;
    }

    public Map<String, Object> getCustomFieldsForExport(HashMap<String, String> customFieldMap, Map<String, Object> variableMap, HashMap<String, String> customDateFieldMap) throws ServiceException {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
            String coldata = (varEntry.getValue() != null) ? (!varEntry.getValue().toString().equals("null") ? varEntry.getValue().toString() : "") : "";
            if (customFieldMap.containsKey(varEntry.getKey()) && !StringUtil.isNullOrEmpty(coldata)) {
                String ids[] = coldata.split(",");
                coldata = "";
                for (int cnt = 0; cnt < ids.length; cnt++) {
                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), ids[cnt]);
                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                    if (fieldComboData != null) {
                        coldata = coldata + (fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : "");
                    }
                }
                if (coldata.length() > 1) {
                    coldata = coldata.substring(0, coldata.length() - 1);
                }
                returnMap.put(varEntry.getKey(), coldata);
            } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                Date dateFromDB=null;
                try {
                    dateFromDB = defaultDateFormat.parse(coldata);
                    coldata = df2.format(dateFromDB);

                } catch (Exception e) {
                }
                returnMap.put(varEntry.getKey(), coldata);
            } else {
                returnMap.put(varEntry.getKey(), coldata);
            }
        }
        return returnMap;
    }
    
    public JSONObject getProductFieldParams(List list, JSONObject jresult) throws JSONException {


        AccProductCustomData accProductCustomData = null;
        Iterator ite = list.iterator();
        while (ite.hasNext()) {
            FieldParams tmpcontyp = (FieldParams) ite.next();
            JSONObject jobj = new JSONObject();
            jobj.put("fieldname", tmpcontyp.getFieldname());
//            try {
//
//              custumObjresult = accountingHandlerDAOobj.getObject(AccProductCustomData.class.getName(), jeId);
//            } catch (Exception e) {
//            }
//
//            accProductCustomData = (AccProductCustomData) custumObjresult.getEntityList().get(0);
//            if (accProductCustomData != null) {
//                String coldata = accProductCustomData.getCol(tmpcontyp.getColnum());
//                if (!StringUtil.isNullOrEmpty(coldata)) {
//                    jobj.put("fieldData", coldata);
//                }
//
//            }


            jobj.put("fieldlabel", tmpcontyp.getFieldlabel());
            jobj.put("isessential", tmpcontyp.getIsessential());
            jobj.put("maxlength", tmpcontyp.getMaxlength());
            jobj.put("validationtype", tmpcontyp.getValidationtype());
            jobj.put("fieldid", tmpcontyp.getId());
            jobj.put("moduleid", tmpcontyp.getModuleid());
            jobj.put("fieldtype", tmpcontyp.getFieldtype());
            jobj.put("iseditable", tmpcontyp.getIseditable());
            jobj.put("comboid", tmpcontyp.getComboid());
            jobj.put("comboname", tmpcontyp.getComboname());
            jobj.put("moduleflag", tmpcontyp.getModuleflag());
            jobj.put("relatedmoduleid", tmpcontyp.getRelatedmoduleid());
            jobj.put("refcolumn_number", Constants.Custom_Column_Prefix + tmpcontyp.getRefcolnum());
            jobj.put("column_number", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
            jobj.put("sendnotification", tmpcontyp.getsendNotification());
            jobj.put("notificationdays", tmpcontyp.getnotificationDays());
            jobj.put("iscustomfield", tmpcontyp.getCustomfield() == 1 ? true : false);
            jobj.put("iscustomcolumn", tmpcontyp.getCustomcolumn() == 1 ? true : false);
            jresult.append("data", jobj);
        }



        return jresult;
    }

    public Object getProductCustomFieldValue(String fieldId, String productId, String companyId, Date transactionDate) {
        Object returnObject = null;
        try {
            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
            customrequestParams.put("fieldId", fieldId);
            customrequestParams.put("productId", productId);
            customrequestParams.put("companyId", companyId);
            customrequestParams.put("transactionDate", transactionDate);
            KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), fieldId);
            FieldParams fieldParams = (FieldParams) custumObjresult.getEntityList().get(0);
            if (fieldParams != null && fieldParams.getFieldtype() == 1 || fieldParams.getFieldtype() == 2) {
                KwlReturnObject result = accProductObj.getProductCustomFieldValue(customrequestParams);

                List list = result.getEntityList();
                Iterator itr = list.iterator();
                if (itr.hasNext()) {
                    returnObject = itr.next();
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnObject;
    }

    public String getModuleName(int moduleid) {
        String moduleName = "";
        switch (moduleid) {
            case (Constants.Acc_Invoice_ModuleId):
                moduleName = "Invoice/Cash Sales";
                break;
            case (Constants.Acc_GENERAL_LEDGER_ModuleId):
                moduleName = "Journal Entry";
                break;
            case (Constants.Acc_BillingInvoice_ModuleId):
                moduleName = "Billing Invoice";
                break;
            case (Constants.Acc_Cash_Sales_ModuleId):
                moduleName = "Cash Sales";
                break;
            case (Constants.Acc_Billing_Cash_Sales_ModuleId):
                moduleName = "Billing Cash Sales";
                break;
            case (Constants.Acc_Vendor_Invoice_ModuleId):
                moduleName = "Purchase Invoice/Cash Purchase";
                break;
            case (Constants.Acc_Debit_Note_ModuleId):
                moduleName = "Debit Note";
                break;
            case (Constants.Acc_Credit_Note_ModuleId):
                moduleName = "Credit Note";
                break;
            case (Constants.Acc_Make_Payment_ModuleId):
                moduleName = "Make Payment";
                break;
            case (Constants.Acc_Receive_Payment_ModuleId):
                moduleName = "Receive Payment";
                break;
            case (Constants.Acc_Product_Master_ModuleId):
                moduleName = "Products & Services";
                break;
            case (Constants.Acc_Sales_Order_ModuleId):
                moduleName = "Sales Order";
                break;
            case (Constants.Acc_Purchase_Order_ModuleId):
                moduleName = "Purchase Order";
                break;
            case (Constants.Acc_Customer_Quotation_ModuleId):
                moduleName = "Customer Quotation";
                break;
            case (Constants.Acc_Vendor_Quotation_ModuleId):
                moduleName = "Vendor Quotation";
                break;
            case (Constants.Acc_Customer_ModuleId):
                moduleName = "Customer";
                break;
            case (Constants.Acc_Vendor_ModuleId):
                moduleName = "Vendor";
                break;
            case (Constants.Acc_Delivery_Order_ModuleId):
                moduleName = "Delivery Order";
                break;
            case (Constants.Acc_Goods_Receipt_ModuleId):
                moduleName = "Goods Receipt Order";
                break;
            case (Constants.Acc_Purchase_Return_ModuleId):
                moduleName = "Purchase Return";
                break;
            case (Constants.Acc_Sales_Return_ModuleId):
                moduleName = "Sales Return";
                break;
            case (Constants.Account_Statement_ModuleId):
                moduleName = "GL Accounts";
                break;

        }
        return moduleName;
    }
    
       public JSONObject getInvoiceCreationJson(HttpServletRequest request, HttpServletResponse response ) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
       JSONObject jobj = new JSONObject();
        {
            ByteArrayOutputStream baos = null;
            boolean issuccess = true;
            try {
                boolean loadTaxStore= Boolean.parseBoolean((String)request.getParameter("loadtaxstore"));
                boolean loadPriceStore = Boolean.parseBoolean((String)request.getParameter("loadpricestore"));
                boolean loadCurrencyStore =  Boolean.parseBoolean((String)request.getParameter("loadcurrencystore"));
                boolean loadTermStore =  Boolean.parseBoolean((String)request.getParameter("loadtermstore"));
                ModelAndView model=null;
                Map map=null;
                String modelStr;
                JSONObject obj;
                if(loadTaxStore){
                    model=AccTaxcontrollerObj.getTax(request,response);
                    map = model.getModel();
                    modelStr = (String) map.get("model");
                    obj = new JSONObject(modelStr);
                     jobj.put("taxdata", obj);
                }
                if(loadPriceStore){
                     model=AccProductcontrollerObj.getProducts(request,response);
                     map = model.getModel();
                     modelStr = (String) map.get("model");
                     obj = new JSONObject(modelStr);
                     jobj.put("productdata", obj);
                }
                if(loadTermStore){
                     model=AccTermcontrollerObj.getTerm(request,response);
                     map = model.getModel();
                     modelStr = (String) map.get("model");
                     obj = new JSONObject(modelStr);
                     jobj.put("termdata", obj);
                }
                if(loadCurrencyStore){
                     model=AccCurrencycontrollerObj.getCurrencyExchange(request,response);
                     map = model.getModel();
                     modelStr = (String) map.get("model");
                     obj = new JSONObject(modelStr);
                     jobj.put("currencydata", obj);
                }
            }   catch (Exception e) {
                  issuccess=false;
             } finally {
             
                    jobj.put("success", issuccess);
                    jobj.put("msg", "Json Created");
            }
        }
       return jobj;
    }  
    
    public String getChildString(HashMap<String, Object> requestParams) throws JSONException, ServiceException, SessionExpiredException {
        KwlReturnObject result = accAccountDAOobj.getFieldParams(requestParams);
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        String childId = "";
        while (itr.hasNext()) {
            FieldParams tmpcontyp = (FieldParams) itr.next();
            String id = Constants.Custom_Record_Prefix + tmpcontyp.getFieldlabel();
            if (StringUtil.isNullOrEmpty(childId)) {
                childId = id;
            } else {
                childId += "," + id;
            }
        }
        return childId;
    }
     
    //to get the templatelist of document designer
  public JSONObject getDesignTemplateList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
         try {
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String companyid = AccountingManager.getCompanyidFromRequest(request);
            String  moduleidStr[] = request.getParameterValues(Constants.moduleid);
            JSONArray templateArr = new JSONArray();
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            String countryid = new String(); 
            String stateid = new String(); 
            KwlReturnObject companyresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company companyObj = (Company) companyresult.getEntityList().get(0);
            if(companyObj.getCountry() != null){
                countryid = companyObj.getCountry().getID();
            }
            if(companyObj.getState()!= null){
                stateid = companyObj.getState().getID();
            }
            for (int i = 0; i < moduleidStr.length; i++) {
                 int moduleid = Integer.parseInt(moduleidStr[i]);
                 String ss="";
                 KwlReturnObject result = customDesignDAOObj.getDesignTemplateList(companyid, moduleid,ss,start,limit,"",countryid,stateid,"","");
                 List list = result.getEntityList();                 
                 for (int cnt = 0; cnt < list.size(); cnt++) {
                     // fetch columns - id, defaultHeader, dbcolumnname,reftablename, reftablefk,reftabledatacolumn,dummyvalue
                     Object[] row = (Object[]) list.get(cnt);
                     JSONObject tempObj = new JSONObject();
                     tempObj.put("templateid", row[0]);
                     tempObj.put("templatename", row[1]);
                     tempObj.put("createdby", row[2]);
                     tempObj.put("createdon", formatter.parse(row[3].toString()).getTime());
                     tempObj.put("isdefault", row[4]);
                     tempObj.put("moduleid", row[5]);
                     templateArr.put(tempObj);
                 }
            }            
            jobj.put("count", templateArr.length());
            jobj.put(Constants.RES_data, templateArr);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(AccMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null": msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }   
    
}
