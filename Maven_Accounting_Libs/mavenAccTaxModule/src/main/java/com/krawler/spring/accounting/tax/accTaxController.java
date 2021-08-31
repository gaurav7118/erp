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
package com.krawler.spring.accounting.tax;

import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.*;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.Tax1099Accounts;
import com.krawler.hql.accounting.Tax1099Category;
import com.krawler.hql.accounting.TaxList;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
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
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import static com.krawler.spring.accounting.tax.TaxConstants.MSG;
import static com.krawler.spring.accounting.tax.TaxConstants.SUCCESS;
import com.krawler.spring.common.kwlCommonTablesDAO;
import java.text.SimpleDateFormat;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 *
 * @author krawler
 */
public class accTaxController extends MultiActionController implements TaxConstants, MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accTaxDAO accTaxObj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    private accVendorDAO accVendorDao;
    public accCustomerDAO accCustomerDao;
    public accAccountDAO accAccountDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private APICallHandlerService apiCallHandlerService;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;

    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }//getTax1099Category

    public void setAccVendorDAO(accVendorDAO accVendorDao) {
        this.accVendorDao = accVendorDao;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setAccCustomerDAO(accCustomerDAO accCustomerDao) {
        this.accCustomerDao = accCustomerDao;
    }

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }
    
    public ModelAndView getTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean exportflag=false;
        String msg = "";
        try {
            DateFormat df = authHandler.getDateFormatter(request);
            String start = request.getParameter(Constants.start);
            String limit = request.getParameter(Constants.limit);
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String transDate = request.getParameter("transactiondate");
            String taxtypeid = request.getParameter("taxtypeid");
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            if (transDate != null) {
                requestParams.put("transactiondate", df.parse(transDate));
            }
            if (!StringUtil.isNullOrEmpty(taxtypeid)) {
                requestParams.put("taxtypeid", taxtypeid);
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("query"))){   //SDP-12753
                requestParams.put("ss", request.getParameter("query"));
            }
            /*
             *ERP-40242 Show only activated taxes in create and copy case and all taxes in edit cases
             */
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.includeDeactivatedTax))) {
                requestParams.put(Constants.includeDeactivatedTax, Boolean.parseBoolean(request.getParameter(Constants.includeDeactivatedTax)));
            }

            String module = request.getParameter(Constants.moduleid);
            if (!StringUtil.isNullOrEmpty(module)) {
                int moduleid = Integer.parseInt(module);
                if (moduleid == Constants.Acc_Purchase_Order_ModuleId || moduleid == Constants.Acc_BillingPurchase_Order_ModuleId
                        || moduleid == Constants.Acc_Vendor_Invoice_ModuleId || moduleid == Constants.Acc_Vendor_BillingInvoice_ModuleId
                        || moduleid == Constants.Acc_Vendor_Quotation_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId
                        || moduleid == Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId
                        || moduleid == Constants.Acc_Goods_Receipt_ModuleId || moduleid == Constants.Acc_Purchase_Return_ModuleId) {
                    requestParams.put("taxtype", 1);
                } else if ( moduleid == Constants.Acc_Receive_Payment_ModuleId || moduleid == Constants.Acc_Make_Payment_ModuleId){
                    // Include both type of taxes for MP and RP
                } else {
                    requestParams.put("taxtype", 2);
                }
            }
            KwlReturnObject result = accTaxObj.getTax(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getTaxJson(request, list,exportflag);
            JSONArray pagedJson = DataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put(DATA, pagedJson);
            jobj.put(COUNT, count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getTaxJson(HttpServletRequest request, List<Object[]> list,boolean exportflag) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
//            Iterator itr = list.iterator();
//            while (itr.hasNext()) {
//               Object[] row = (Object[]) itr.next();
            String taxType = "";
            if (list != null && !list.isEmpty()) {
                for (Object[] row : list) {
                    if (row[2] == null) {
                        continue;
                    }
                    Tax tax = (Tax) row[0];
                    taxType = "";
                    if (tax.getTaxtype() == 0) {
                        taxType = "Both";
                    } else if (tax.getTaxtype() == 1) {
                        taxType = "Purchase";
                    }else if (tax.getTaxtype() == 2) {
                        taxType = "Sales";
                    }

                    JSONObject obj = new JSONObject();
                    accTaxObj.getTerms(tax.getID(), obj);
                    obj.put(TAXID, tax.getID());
                    obj.put(TAXNAME, tax.getName());
                    obj.put(TAXDESCRIPTION, tax.getDescription());
                    obj.put(PERCENT, row[1]);
                    obj.put(TAXCODE, tax.getTaxCode());
                    obj.put(ACCOUNTID,exportflag==true?tax.getAccount().getName(): tax.getAccount().getID());
                    obj.put(ACCOUNTNAME, tax.getAccount().getName());
                    obj.put(TAXTYPEID,exportflag==true? taxType : tax.getTaxtype());
                    obj.put(COMPANYID, tax.getCompany().getCompanyID());
                    obj.put("taxTypeName", taxType);
                    obj.put(APPLYDATE, authHandler.getDateOnlyFormat(request).format(row[2]));
                    obj.put("extrataxtypeid",tax.getExtrataxtype());
                    obj.put(ACTIVATED,tax.isActivated());
                    obj.put(Constants.HAS_ACCESS, tax.isActivated());
                    obj.put(ISINPUTCREDITFORTAX, tax.isInputCredit());
                    
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTaxJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    public ModelAndView ExportTAx(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        boolean exportflag=true;
        try {

            DateFormat df = authHandler.getDateFormatter(request);

            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String transDate = request.getParameter("transactiondate");
            String taxtypeid = request.getParameter("taxtypeid");
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            if (transDate != null) {
                requestParams.put("transactiondate", df.parse(transDate));
            }
            if (!StringUtil.isNullOrEmpty(taxtypeid)) {
                requestParams.put("taxtypeid", taxtypeid);
            }
            String module = request.getParameter(Constants.moduleid);
            if (!StringUtil.isNullOrEmpty(module)) {
                int moduleid = Integer.parseInt(module);
                if (moduleid == Constants.Acc_Purchase_Order_ModuleId || moduleid == Constants.Acc_BillingPurchase_Order_ModuleId
                        || moduleid == Constants.Acc_Vendor_Invoice_ModuleId || moduleid == Constants.Acc_Vendor_BillingInvoice_ModuleId
                        || moduleid == Constants.Acc_Vendor_Quotation_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId
                        || moduleid == Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId
                        || moduleid == Constants.Acc_Goods_Receipt_ModuleId || moduleid == Constants.Acc_Make_Payment_ModuleId) {
                    requestParams.put("taxtype", 1);
                } else {
                    requestParams.put("taxtype", 2);
                }
            }
            KwlReturnObject result = accTaxObj.getTax(requestParams);
            List list = result.getEntityList();
            JSONArray jArr = getTaxJson(request, list,exportflag);

            String fileType = request.getParameter("filetype");

            if (StringUtil.isNullOrEmpty(fileType)) {
                fileType = "csv";
            }
            jobj.put(Constants.RES_data, jArr);
            jobj.put(Constants.RES_count, jArr.length());
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put("success", true);

        } catch (Exception ex) {
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    public ModelAndView getTax1099Category(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);

            KwlReturnObject result = accTaxObj.getTax1099Category(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getTax1099CategoryJson(requestParams, list);
            jobj.put(DATA, DataJArr);
            jobj.put(COUNT, count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getTax1099CategoryJson(Map<String, Object> requestParams, List<Tax1099Category> list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String companyid = (String) requestParams.get(COMPANYID);
//            Iterator itr = list.iterator();
//            while (itr.hasNext()) {
//              //  Object[] row = (Object[]) itr.next();
//                Tax1099Category taxcategory =(Tax1099Category)itr.next();
            if (list != null && !list.isEmpty()) {
                for (Tax1099Category taxcategory : list) {
                    JSONObject obj = new JSONObject();
                    obj.put(CATEGORYID, taxcategory.getID());
                    obj.put(CATEGORYNAME, taxcategory.getCategory());
                    obj.put(THRESHOLDVALUE, taxcategory.getThresholdValue());
                    obj.put(ISDELETED, taxcategory.isDeleted());
                    obj.put(SRNO, taxcategory.getSrno());
                    KwlReturnObject result = accTaxObj.getTaxCategoryAccount(companyid, taxcategory.getID());
                    List<Account> accList = result.getEntityList();
//                    Iterator accItr = accList.iterator();
                    String accNames = "";
//                    while (accItr.hasNext()) {
//                        Account acc= (Account) accItr.next();
                    if (accList != null && !accList.isEmpty()) {
                        for (Account acc : accList) {
                            accNames += acc.getID();
                            accNames += ",";
                        }
                    }
                    accNames = accNames.substring(0, Math.max(0, accNames.length() - 1));
                    //              obj.put("accountid", taxcategory.getAccount().getID());
                    obj.put(ACCOUNTID, accNames);
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTaxJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView checkApplyTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean flag;
        String msg = "";
        try {
            flag = checkApplyTax(request);
            jobj.put(MSG, flag);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                if (msg.length() > 0) {
                    jobj.put(MSG, msg);
                }
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public boolean checkApplyTax(HttpServletRequest request) throws JSONException, SessionExpiredException, ParseException, ServiceException {
        boolean flag = true;
        JSONArray jArr = new JSONArray(request.getParameter(DATA));
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);

            Date appDate = authHandler.getDateFormatter(request).parse(jobj.getString(APPLYDATE));
            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(TAXID, jobj.getString(TAXID));
            filterParams.put(APPDATE, appDate);
            filterParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));

            KwlReturnObject result = accTaxObj.getTaxList(filterParams);
            List list = result.getEntityList();
            if (!list.isEmpty()) {
                flag = false;
                return false;
            }
        }
        return flag;
    }

    public ModelAndView saveTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Tax_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveTax(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.tax.update2", null, RequestContextUtils.getLocale(request));  //"Tax details has been updated successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(ConstraintViolationException | DataIntegrityViolationException ex){
            txnManager.rollback(status);
            msg = messageSource.getMessage("acc.alert.taxCanNotDeleted", null, RequestContextUtils.getLocale(request));  //"Tax details has been updated successfully";
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(ServiceException ex){
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveTax(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter(DATA));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            Map<String, Object> auditRequestParams = new HashMap<>();
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
            
            String taxid = "";
            KwlReturnObject result;
            int delCount = 0;
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString(TAXID)) == false) {
                    taxid = jobj.getString(TAXID);
                    String taxname = jobj.getString("taxname");
                    try {
                        KwlReturnObject result1 = accVendorDao.getVendorForTax(taxid, companyid);
                        int count1 = (int) result1.getRecordTotalCount();
                        if (count1 > 0) {
                            throw new AccountingException(messageSource.getMessage("acc.tax.excptax", null, RequestContextUtils.getLocale(request)));//Selected Tax is currently associted with Vendor(s). So it cannot be deleted.
                        }
                        KwlReturnObject result2 = accCustomerDao.getCustomerForTax(taxid, companyid);
                        int count2 = (int) result2.getRecordTotalCount();
                        if (count2 > 0) {
                            throw new AccountingException(messageSource.getMessage("acc.tax.excptax", null, RequestContextUtils.getLocale(request)));//Selected Tax is currently associted with Customer(s). So it cannot be deleted.
                        }
                        result = accTaxObj.deleteTaxList(taxid, companyid);
                        result = accTaxObj.deleteTax(taxid, companyid);
                        delCount = result.getRecordTotalCount();
                        if (delCount > 0) {
                            /*
                             * To post delete tax audit trail entry for every tax with tax name.
                             */
                            auditTrailObj.insertAuditLog(AuditAction.TAX_DETAIL_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has deleted Tax \"" + taxname + "\"", auditRequestParams, "0");
                        }
                    } catch (ServiceException ex) {
                        throw new AccountingException(messageSource.getMessage("acc.tax.excp1", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            String auditMsg;
            String auditID;
            Tax tax = null;
            TaxList taxlist;
            KwlReturnObject taxresult;
            Map<String, Object> taxMap;
            String duplicateTaxName = "";
            boolean isMalaysiaOrSingaporeCompany = false;
            Set<Object> landedcostaxes = null;
            String countrycode = extraCompanyPreferences!=null?extraCompanyPreferences.getCompany().getCountry().getID():"";
            if (countrycode.equalsIgnoreCase(String.valueOf(Constants.malaysian_country_id)) || countrycode.equalsIgnoreCase(String.valueOf(Constants.SINGAPOREID))) {
                isMalaysiaOrSingaporeCompany = true;
            }
            //to check if landed cost tax transactions are available in the system
            if(extraCompanyPreferences!=null && extraCompanyPreferences.isActivelandingcostofitem() && isMalaysiaOrSingaporeCompany){
                landedcostaxes = accTaxObj.getTaxIdsWithLandedCost(companyid);
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;       // Updated modified tax only.
                }

                taxMap = new HashMap<>();
                taxMap.put(COMPANYID, companyid);
                boolean isEdit = false;
                boolean isAccountNameChanged = false;
                boolean isTaxCodeChanged = false;
                String oldAccountId = "";
                String oldTaxCodeId = "";
                String newAccountId = jobj.getString(ACCOUNTID);
                String oldAccountName = "";
                String newTaxCodeId = jobj.getString(TAXCODE);
                boolean isTaxStatusChanged = false;
                boolean newTaxStatus = jobj.optBoolean(ACTIVATED, true);//By default tax should be activated.
                /*
                 *If TaxName or TaxCode already exist then deny to update or insert duplicate records.
                 * Ticket--> ERP-35905
                 */
                String taxName = StringUtil.DecodeText(jobj.optString(TAXNAME));
                Map<String, Object> requestParams = AccountingManager.getGlobalParams(paramJobj);
                requestParams.put("chkDuplicateTax", true);
                requestParams.put(TAXNAME, taxName);
//                requestParams.put(TAXCODE, StringUtil.DecodeText(newTaxCodeId));
                KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                List duplicateList = result1.getEntityList();
                int count=StringUtil.isNullOrEmpty(jobj.getString(TAXID))?0:1;
                    if (duplicateList != null && duplicateList.size() > count) {
                        duplicateTaxName += "[" + taxName + "," + StringUtil.DecodeText(newTaxCodeId) + "]";
                        continue;
                    }
                if (StringUtil.isNullOrEmpty(jobj.getString(TAXID))) {
                    auditMsg = "added";
                    auditID = AuditAction.TAX_DETAIL_CREATED;
                    taxid = StringUtil.generateUUID();
                    taxMap.put(TAXID, taxid);
                    taxMap.put(TAXNAME, StringUtil.DecodeText(jobj.optString(TAXNAME)));
                    taxMap.put(TAXDESCRIPTION, StringUtil.DecodeText(jobj.optString(TAXDESCRIPTION)));
                    taxMap.put(TAXCODE, StringUtil.DecodeText(jobj.optString(TAXCODE)));
                    taxMap.put(TAXCODEWITHOUTPERCENTAGE, StringUtil.DecodeText(jobj.optString(TAXCODE)));//ERP-10979
                    taxMap.put(ACCOUNTID, jobj.getString(ACCOUNTID));
                    taxMap.put(COMPANYID, companyid);
                    taxMap.put(TAXTYPEID, jobj.getString(TAXTYPEID));
                    taxMap.put(ISINPUTCREDITFORTAX, jobj.optString(ISINPUTCREDITFORTAX,"F")); // ERM-971 taxes for landed cost
                    taxMap.put("extrataxtypeid", StringUtil.isNullOrEmpty(jobj.getString("extrataxtypeid"))?"0":jobj.getString("extrataxtypeid"));
                    taxMap.put(ACTIVATED, newTaxStatus);
                    taxresult = accTaxObj.addTax(taxMap);
                } else {
                    auditMsg = "updated";
                    auditID = AuditAction.TAX_DETAIL_UPDATED;
                    taxMap.put(TAXID, jobj.getString(TAXID));
                    KwlReturnObject taxKwlObj = accTaxObj.getTax(taxMap);
                    /*
                     * To post account name change audit Trail Entry.
                     */
                    if (!taxKwlObj.getEntityList().isEmpty()) {
                        List list = taxKwlObj.getEntityList();
                        Object[] taxObjArr = (Object[]) list.get(0);
                        tax = (Tax)taxObjArr[0];
                        oldAccountId = tax.getAccount().getID();
                        oldAccountName = tax.getAccount().getAccountName();
                        if (!StringUtil.equalIgnoreCase(oldAccountId, newAccountId)) {
                            isAccountNameChanged = true;
                            taxMap.put(ACCOUNTID, newAccountId);//ERP-33514
                        }
                        
                        oldTaxCodeId = tax.getTaxCode();
                        if (!StringUtil.equalIgnoreCase(oldTaxCodeId, newTaxCodeId)) {
                            isTaxCodeChanged = true;
                            taxMap.put(TAXCODE, StringUtil.DecodeText(newTaxCodeId));//ERP-33514
                        }
                        
                        boolean taxStatusBeforeUpdate = tax.isActivated();//ERP-38656
                        if (taxStatusBeforeUpdate != newTaxStatus) {
                            isTaxStatusChanged = true;
                            taxMap.put(ACTIVATED, newTaxStatus);
                        }
                    }
                    /**
                     * ERM-971 do not allow updating the isinputcredit flag of a tax if it is used in landed cost transactions
                     * currently only for Singapore/Malaysian countries.
                     */
                    if (tax != null && extraCompanyPreferences.isActivelandingcostofitem() && isMalaysiaOrSingaporeCompany && landedcostaxes != null && landedcostaxes.contains(jobj.getString(TAXID))) {
                        boolean oldinputcreditflag = tax.isInputCredit();
                        boolean newinputcreditflag = jobj.optBoolean(ISINPUTCREDITFORTAX, false);
                        if (oldinputcreditflag != newinputcreditflag && !newinputcreditflag) {
                            Object[] msgparams = new Object[]{tax.getName()};
                            throw new AccountingException(messageSource.getMessage("acc.masterConfig.taxes.taxusedinlandedcost", msgparams, RequestContextUtils.getLocale(request)));
                        }
                    }
                    taxMap.put(ISINPUTCREDITFORTAX, jobj.optString(ISINPUTCREDITFORTAX,"F")); // ERM-971 taxes for landed cost
                    taxMap.put(TAXCODE, StringUtil.DecodeText(jobj.optString(TAXCODE)));
                    taxresult = accTaxObj.updateTax(taxMap);
                    isEdit = true;
                }
                tax = (Tax) taxresult.getEntityList().get(0);
                if(!isEdit){// make entry in tax list table only in new case
                    taxlist = setNewTax(request, jobj, tax, taxid);
                }
                if (isAccountNameChanged && isTaxCodeChanged) {//Account Name change audit entry.
                    auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + " has updated account name to \"" + tax.getAccount().getAccountName() + "\" from \"" + oldAccountName + " for tax \"" + tax.getName() + "\"", auditRequestParams, tax.getID());
                    auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + " has updated tax code to \"" + newTaxCodeId + "\" from \"" + oldTaxCodeId + "\" for tax \"" + tax.getName() + "\"", auditRequestParams, tax.getID());
                } else if (isAccountNameChanged) {
                    auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + " has updated account name to \"" + tax.getAccount().getAccountName() + "\" from \"" + oldAccountName + " for tax \"" + tax.getName() + "\"", auditRequestParams, tax.getID());
                } else if (isTaxCodeChanged) {
                    auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + " has updated tax code to \"" + newTaxCodeId + "\" from \"" + oldTaxCodeId + "\" for tax \"" + tax.getName() + "\"", auditRequestParams, tax.getID());
                } else {
                    auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + " has " + auditMsg + " Tax \"" + tax.getName() + "\"", auditRequestParams, tax.getID());
                }
                if (isTaxStatusChanged) {//Activate/Deactivate tax audit trail entry.
                    auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + " has " + (newTaxStatus ? "activated" : "deactivated") + " tax code \"" + tax.getName(), auditRequestParams, tax.getID());
                }
            }
            if(!StringUtil.isNullOrEmpty(duplicateTaxName)){
                throw new AccountingException("Cannot create duplicate entry for Tax Code or Tax Name\n" + duplicateTaxName);
            }
        }/* catch (UnsupportedEncodingException ex) {
         throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } */catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public ModelAndView saveTax1099Category(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Tax_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveTax1099Category(request);
            issuccess = true;
            msg = "Tax Category details has been updated successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveTax1099Category(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException, JSONException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter(DATA));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Tax1099Category taxCategory;
            KwlReturnObject taxresult = null;
            Map<String, Object> taxCategoryMap;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                taxCategoryMap = new HashMap<String, Object>();
                taxCategoryMap.put(THRESHOLDVALUE, Double.parseDouble(jobj.getString(THRESHOLDVALUE)));// StringUtil.DecodeText(jobj.optString("taxname")));
                taxCategoryMap.put(CATEGORYID, jobj.getString(CATEGORYID));
                taxCategoryMap.put(COMPANYID, companyid);
                taxresult = accTaxObj.updateTax1099Category(taxCategoryMap);
                taxCategory = (Tax1099Category) taxresult.getEntityList().get(0);
                accTaxObj.deleteTax1099AccountList(jobj.getString(CATEGORYID), companyid);
                Tax1099Accounts taxcatacclist = setTaxCategoryAccount(request, jobj, taxCategory);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public Tax1099Accounts setTaxCategoryAccount(HttpServletRequest request, JSONObject jobj, Tax1099Category taxCategory) throws ServiceException, SessionExpiredException {
        Tax1099Accounts taxcatacclist = null;
        try {
            String taxcategoryid = taxCategory.getID();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] accountarr = jobj.getString(ACCOUNTID).split(",");
            for (int i = 0; i < accountarr.length; i++) {
                String accountid = accountarr[i];
                if (!StringUtil.isNullOrEmpty(accountid)) {
                    Map<String, Object> taxListMap = new HashMap<String, Object>();
                    taxListMap.put(COMPANYID, companyid);
                    taxListMap.put(CATEGORYID, taxcategoryid);
                    taxListMap.put(ACCOUNTID, accountid);
                    KwlReturnObject taxlistresult;
                    taxlistresult = accTaxObj.updateTax1099Account(taxListMap);
                    taxcatacclist = (Tax1099Accounts) taxlistresult.getEntityList().get(0);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("setNewTax : " + ex.getMessage(), ex);
        }
        return taxcatacclist;
    }

    public TaxList setNewTax(HttpServletRequest request, JSONObject jobj, Tax tax, String taxlistid) throws ServiceException, SessionExpiredException {
        TaxList taxlist = null;
        try {
//            List list = null;
//            ArrayList params=new ArrayList();
//            Company company=(Company)session.get(Company.class,AuthHandler.getCompanyid(request));
//                String query="from TaxList where applyDate=? and tax.ID=?  and company.companyID=?";
//                params.add(appDate);
//                params.add(tax.getID());
//                params.add(company.getCompanyID());
//                list = HibernateUtil.executeQuery(session, query, params.toArray());

            String taxid = taxlistid;
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            Date appDate = authHandler.getDateOnlyFormat(request).parse(jobj.getString(APPLYDATE));
            
            Date appDate = new Date();
            Map paramsMap = new HashMap<>();
            paramsMap.put("ID", companyid);
            Date financialYearStartDate = (Date) kwlCommonTablesDAOObj.getRequestedObjectFields(CompanyAccountPreferences.class, new String[]{"firstFinancialYearFrom"}, paramsMap);
            if (financialYearStartDate != null) {
                /**
                 * Apply Date column removed from Tax Report so by default apply
                 * date is first financial year from date as per ERP-41069.
                 */
                appDate = financialYearStartDate;
            }

            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(APPLYDATE, appDate);
            filterParams.put(COMPANYID, companyid);
            filterParams.put(TAXID, taxid);

            KwlReturnObject result = accTaxObj.getTaxList(filterParams);
            List list = result.getEntityList();
//            taxlist.setApplyDate(appDate);
//            taxlist.setTax(tax);
//            taxlist.setCompany(company);
//            taxlist.setPercent(Double.parseDouble(jobj.getString("percent")));
//            session.saveOrUpdate(taxlist);
            Map<String, Object> taxListMap = new HashMap<String, Object>();
            taxListMap.put(TAXID, taxid);
            taxListMap.put(APPLYDATE, appDate);
            taxListMap.put(COMPANYID, companyid);
            taxListMap.put(PERCENT, Double.parseDouble(jobj.getString(PERCENT)));

            KwlReturnObject taxlistresult;
            if (list != null && !list.isEmpty()) {
                taxlist = (TaxList) list.get(0);
                taxListMap.put(TAXLISTID, taxlist.getID());
                taxlistresult = accTaxObj.updateTaxList(taxListMap);
            } else {
//                taxlist = new TaxList();
                taxlistresult = accTaxObj.addTaxList(taxListMap);
            }

            taxlist = (TaxList) taxlistresult.getEntityList().get(0);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("setNewTax : " + ex.getMessage(), ex);
        }
        return taxlist;
    }

    /*
     * This function get default tax for singapur country only.
     */
    public ModelAndView getDefaultGSTTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {

            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            boolean isMalasianCountry = false;
            String countryid = request.getParameter("countryid");
            String stateid = request.getParameter("stateid");
            if (countryid != null) {
                dataMap.put("countryid", countryid);
                if(countryid.equals("137")){
                    isMalasianCountry = true;
                } else if(countryid.equals("105") && !StringUtil.isNullOrEmpty(stateid)) {//For Indian Company("105")
                    dataMap.put("stateid", stateid);
                }
            }
            KwlReturnObject result = accTaxObj.getDefaultGSTList(dataMap);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getDefaultGSTTaxJson(request, list);
            jobj.put(DATA, DataJArr);
            jobj.put(COUNT, count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getDefaultGSTTaxJson(HttpServletRequest request, List<Object[]> list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            if (list != null && !list.isEmpty()) {

                String companyId = sessionHandlerImpl.getCompanyid(request);
                String gstOutputAccountId = "";
                String gstInputAccountId = "";
                String gstOutputAccountName = "";
                String gstInputAccountName = "";
                String salesTaxPayableAccountId = "";
                String salesTaxPayableAccountName = "";

                Date financialStartDate = null;

                boolean isMalasianCountry = false;
                boolean isUSCountry = false;
                String countryid = request.getParameter("countryid");
                if (countryid != null && countryid.equals("137")) {
                    isMalasianCountry = true;
                }
                /*
                 countryid 244 is set for US companies so setting isUSCompany flag to true
                 */
                if (countryid != null && countryid.equals("244")) {
                    isUSCountry = true;
                }

                if (isMalasianCountry) {
                    if (!StringUtil.isNullOrEmpty(request.getParameter("financialYrStartDate"))) {
                        financialStartDate = authHandler.getDateFormatter(request).parse(request.getParameter("financialYrStartDate"));
                    }

                    // 
                    
                    KwlReturnObject accountReturnObject = accAccountDAOobj.getAccountFromName(companyId, Constants.MALAYSIAN_GST_OUTPUT_TAX);
                    List accountResultList = accountReturnObject.getEntityList();
                    if(!accountResultList.isEmpty()){
                        gstOutputAccountId =  ((Account)accountResultList.get(0)).getID();
                        gstOutputAccountName =  ((Account)accountResultList.get(0)).getName();
                    }

                    accountReturnObject = accAccountDAOobj.getAccountFromName(companyId, Constants.MALAYSIAN_GST_INPUT_TAX);
                    accountResultList = accountReturnObject.getEntityList();
                    if(!accountResultList.isEmpty()){
                        gstInputAccountId = ((Account)accountResultList.get(0)).getID();;
                        gstInputAccountName = ((Account)accountResultList.get(0)).getName();;
                    }
                }
                /*
                 For US companies mapping only one account(Sales Tax Payable) to all default taxes.
                 */
                if (isUSCountry) {
                    KwlReturnObject accountReturnObject = accAccountDAOobj.getAccountFromName(companyId, Constants.SALES_TAX_PAYABLE);
                    List accountResultList = accountReturnObject.getEntityList();
                    if(!accountResultList.isEmpty()){
                        salesTaxPayableAccountId =  ((Account)accountResultList.get(0)).getID();
                        salesTaxPayableAccountName =  ((Account)accountResultList.get(0)).getName();
                    }
                }

                for (Object[] row : list) {
                    JSONObject obj = new JSONObject();
                    obj.put(TAXNAME, row[0]);
                    obj.put(TAXDESCRIPTION, row[1]);
                    obj.put(PERCENT, row[3]);
                    obj.put(TAXCODE, row[2]);
                    obj.put(TAXTYPE, row[5]);
                    obj.put(TAXID,row.length>7 && row[7]!=null?row[7].toString():""); //defaultgst table id
                    obj.put(APPLYDATE, (isMalasianCountry)?authHandler.getDateFormatter(request).format(financialStartDate):authHandler.getDateFormatter(request).format(new Date()));
                    obj.put(MASTERTYPEVALUE, Group.ACCOUNTTYPE_GST);
                    if(isMalasianCountry){
                        boolean isPurchase=false;
                        int taxType=2; //For sales tax type
                        obj.put(GST_ACCOUNT_ID, gstOutputAccountId);
                        obj.put(GST_ACCOUNT_NAME, gstOutputAccountName);
                        String taxName = row[0].toString();
                        /*
                         * TX-E43 renamed as TX-IES 
                         * TX-N43 renamed as TX-ES
                         * Added new purchase tax RP,TX-FRS,TX-NC & NP
                         */
                        if(StringUtil.isMalaysianPurchaseTax(taxName)){
                            taxType=1; //For Purchase tax type
                            obj.put(GST_ACCOUNT_ID, gstInputAccountId);
                            obj.put(GST_ACCOUNT_NAME, gstInputAccountName);
                        }
                    }else{
                        obj.put(GST_ACCOUNT_ID, "");
                        obj.put(GST_ACCOUNT_NAME, "");
                    }
                    /*
                     Adding sales tax payable accountid in JSON for US companies
                     */
                    if(isUSCountry){
                        obj.put(SALESTAX_ACCOUNT_ID, salesTaxPayableAccountId);
                        obj.put(SALESTAX_ACCOUNT_NAME, salesTaxPayableAccountName);
                    }else{
                        obj.put(SALESTAX_ACCOUNT_ID, "");
                        obj.put(SALESTAX_ACCOUNT_NAME, "");
                    }
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTaxJson : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getTaxJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public JSONArray getTaxes(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = null;
        boolean issuccess = false;
        boolean exportflag=false;
        String msg = "";
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String transDate = request.getParameter("transactiondate");
            String taxtypeid = request.getParameter("taxtypeid");
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
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

            DataJArr = getTaxJson(request, list,exportflag);
            //jobj.put( DATA,DataJArr);
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return DataJArr;
    }

    public ModelAndView sendTax(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        //Session session=null;
        boolean companyExist=true;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            String crmURL = this.getServletContext().getInitParameter("crmURL");
            String crmURL = URLUtil.buildRestURL(Constants.crmURL);
            crmURL = crmURL + "master/tax";
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);

            //session = HibernateUtil.getCurrentSession();
//            String action = "206";
            JSONArray tjobj = getTaxes(request, response);
            JSONArray taxes = new JSONArray();
            for (int i = 0; i < tjobj.length(); i++) {
                JSONObject jb = tjobj.getJSONObject(i);
                String taxType = jb.get("taxTypeName").toString();
                if (taxType.equalsIgnoreCase("Sales")||taxType.equalsIgnoreCase("Both")) {            // Sync  sales and both  taxtype to CRM
                    taxes.put(jb);
                }
            }
            userData.put("data", taxes);
            JSONObject resObj = apiCallHandlerService.restPostMethod(crmURL, userData.toString());
//            JSONObject resObj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                issuccess = resObj.getBoolean("success");
                msg = resObj.getString(Constants.RES_MESSAGE);
                jobj.put("msg", msg);
                companyExist=resObj.optBoolean("companyexist");
            }
        } catch (Exception ex) {
            issuccess=false;
            throw ServiceException.FAILURE("crmManager.insertAccProduct", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("companyexist", companyExist);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
            //HibernateUtil.closeSession(session);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView sendTaxToPOS(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        //Session session=null;
        boolean companyExist=true;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String subdomain = sessionHandlerImpl.getCompanySessionObj(request).getCdomain();
            String posURL = this.getServletContext().getInitParameter("posURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
            userData.put("subdomain", subdomain);
            //session = HibernateUtil.getCurrentSession();
            String action = "31";       //key to save tax at pos side
            JSONArray tjobj = getTaxes(request, response);
            JSONArray taxes = new JSONArray();
            for (int i = 0; i < tjobj.length(); i++) {
                JSONObject jb = tjobj.getJSONObject(i);
                String taxType = jb.get("taxTypeName").toString();
                if (taxType.equalsIgnoreCase("Sales")) {            // Sync only sales type tax to POS
                    taxes.put(jb);
                }
            }
            userData.put("taxes", taxes);
            JSONObject resObj = apiCallHandlerService.callApp(posURL, userData, companyid, action);
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                issuccess = resObj.getBoolean("success");
                msg = resObj.getString("msg");
                companyExist=resObj.optBoolean("companyexist");
            }
        } catch (Exception ex) {
            issuccess=false;
            throw ServiceException.FAILURE("crmManager.insertAccProduct", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("companyexist", companyExist);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
//            HibernateUtil.closeSession(session);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    public ModelAndView sendTaxRequest(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String taxname1 = "";
        String alreadyexist = " But Tax(s) ";
        String added = "Tax(s) ";
        boolean issuccess = true;
        boolean duplicate = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            String crmURL = this.getServletContext().getInitParameter("crmURL");
            String crmURL = URLUtil.buildRestURL(Constants.crmURL);
            crmURL = crmURL + "master/tax";
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
//            String action = "207";
            JSONObject resObj = apiCallHandlerService.restGetMethod(crmURL, userData.toString());
//            JSONObject resObj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                issuccess = resObj.getBoolean("success");
                JSONArray jobj3 = resObj.getJSONArray("taxdetials");
                String companyID = resObj.getString("companyid");
                KwlReturnObject taxResult = accTaxObj.getAllTaxOfCompany(companyID);
                List<Tax> list = taxResult.getEntityList();
                for (int i = 0; i < jobj3.length(); i++) {
                    jobj = jobj3.getJSONObject(i);
                    if (!StringUtil.isNullOrEmpty(jobj.optString("id", ""))) {
                        String taxid = jobj.getString("id");
                        String taxname = jobj.getString("taxname");
                        KwlReturnObject txResult = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxid);
                        Tax taxObj = (Tax) txResult.getEntityList().get(0);
                        if (taxObj != null) {
                            taxname1 = taxObj.getName();
                        } else {
                            for (Tax obj : list) {
                                if (obj.getName().equals(taxname)) {
                                    duplicate = true;
                                }
                            }
                        }
                        if ((taxname1 == null ? taxname != null : !taxname.equals(taxname1)) && !duplicate) {
                            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
                            Company company = (Company) companyResult.getEntityList().get(0);
                            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                            Date newdate = new Date();
                            String userdiff = company.getCreator().getTimeZone() != null ? company.getCreator().getTimeZone().getDifference() : company.getTimeZone().getDifference();
                            sdf1.setTimeZone(TimeZone.getTimeZone("GMT" + userdiff));
                            Date newcreatedate = authHandler.getDateWithTimeFormat().parse(sdf1.format(newdate));

                            KWLCurrency currid = company.getCurrency();
                            Account account = null;
                            JSONObject accjson = new JSONObject();
                            accjson.put("currencyid", currid.getCurrencyID());
                            accjson.put("name", "Tax");
                            accjson.put("balance", 0.0);
                            accjson.put("budget", 0.0);
                            accjson.put("minbudget", 0.0);
                            accjson.put("eliminateflag", false);
                            accjson.put("companyid", company.getCompanyID());
                            accjson.put("groupid", "3");
                            accjson.put("creationdate", newcreatedate);
                            accjson.put("life", 10);
                            accjson.put("salvage", 0);
                            
                            /**
                             * ERP-31364
                             * accounttype // 0 - for PnL, 1 - Balance sheet 
                             * Account Type of Tax From CRM is Balance sheet so we set Constants.ACC_TYPE_BALANCESHEET
                             * mastertypevalue //1 - GL, 2 - Cash, 3 - Bank, 4 - Normal GST
                             * mastertypevalue for Tax From CRM is is Normal GST so we set Constants.ACCOUNT_MASTERTYPE_GST
                             */
                            accjson.put("accounttype", Group.ACC_TYPE_BALANCESHEET);
                            accjson.put("mastertypevalue", Group.ACCOUNTTYPE_GST);
                            
                            KwlReturnObject accresult = accAccountDAOobj.addAccount(accjson);
                            account = (Account) accresult.getEntityList().get(0);

                            HashMap<String, Object> taxMap = new HashMap<>();
                            taxMap.put("taxid", jobj.getString("id"));
                            taxMap.put("taxcode", StringUtil.DecodeText(jobj.optString("taxcode").replaceAll("%", "%25")));
                            taxMap.put("taxname", StringUtil.DecodeText(jobj.optString("taxname").replaceAll("%", "%25")));
                            taxMap.put("companyid", company.getCompanyID());
                            taxMap.put("accountid", account.getID());
                            taxMap.put("taxCodeWithoutPercentage", StringUtil.DecodeText(jobj.optString("taxcode").replaceAll("%", "%25")));
                            taxMap.put("taxdescription", "Sales Tax");
                            taxMap.put("taxtypeid", 2);
                            KwlReturnObject taxresult = accTaxObj.addTax(taxMap);
                            Tax tax = (Tax) taxresult.getEntityList().get(0);

                            Date date = sdf.parse(jobj.getString("applydateStr"));
                            //Create taxList
                            HashMap<String, Object> taxListMap = new HashMap<>();
                            taxListMap.put("applydate", date);
                            taxListMap.put("taxid", tax.getID());
                            taxListMap.put("companyid", company.getCompanyID());
                            taxListMap.put("percent", Double.parseDouble(jobj.getString("percent")));
                            KwlReturnObject taxlistresult = accTaxObj.addTaxList(taxListMap);
                            TaxList taxlist = (TaxList) taxlistresult.getEntityList().get(0);

                            added += "<b>" + taxname + "</b>" + ", ";
                        } else {
                            alreadyexist += "<b>" + taxname + "</b>" + ", ";
                        }
                    }
                }
                if (added.equals("Tax(s) ")) {
                    added = " No Tax(s) are synced with Accounting";
                } else {
                    added = added.substring(0, added.length() - 2);
                    added += " are successfully synced with Accounting";
                }
                if (alreadyexist.equals(" But Tax(s) ")) {
                    alreadyexist = ""; // are already exists on ERP side. If You Want to Sync then please update these taxes and Sync.";
                } else {
                    alreadyexist = alreadyexist.substring(0, alreadyexist.length() - 2);
                    alreadyexist += " are already exists on ERP side. If You Want to Sync then please update these taxes and Sync.";
                }
                msg = added + alreadyexist;
            }
        } catch (Exception ex) {
            issuccess = false;
            throw ServiceException.FAILURE("crmManager.insertAccProduct", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("companyexist", true);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    //Update apply date of taxes
    public ModelAndView updateApplyDateForTaxes(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, AccountingException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> filterParams = new HashMap<String, Object>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Date appDate = null;
            if (StringUtil.isNullOrEmpty(request.getParameter("applydate"))) {
                throw new AccountingException(messageSource.getMessage("acc.curex.excp1", null, RequestContextUtils.getLocale(request)));
            } else {
                appDate = authHandler.getGlobalDateFormat().parse(request.getParameter("applydate"));
                filterParams.put("applyDate", appDate);
            }
            filterParams.put("companyid", companyid);
            accTaxObj.updateApplyDateForTaxes(filterParams);
        } catch (ParseException ex) {
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("updateApplyDateForTaxes : " + ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView checkTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Tax_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            checkTax(request);
            issuccess = true;
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConstraintViolationException | DataIntegrityViolationException ex) {
            txnManager.rollback(status);
            msg = messageSource.getMessage("acc.alert.taxCanNotDeleted", null, RequestContextUtils.getLocale(request));  //"Tax details has been updated successfully";
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
}
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void checkTax(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {

        String companyid = sessionHandlerImpl.getCompanyid(request);
        String taxid = request.getParameter("id");

        try {
            KwlReturnObject result1 = accVendorDao.getVendorForTax(taxid, companyid);
//            String msg1 = "The Tax code(s) is or had been associated with " + " " + result1.getEntityList().toString() + " " + "Vendor(s). So it cannot be deleted";
            String msg1=messageSource.getMessage("acc.tax.excptaxven",null, RequestContextUtils.getLocale(request))+result1.getEntityList().toString()+messageSource.getMessage("acc.tax.excptax2",null, RequestContextUtils.getLocale(request));
            int count1 = (int) result1.getRecordTotalCount();
            if (count1 > 0) {
                throw new AccountingException(msg1);//Selected Tax is currently associted with Vendor(s). So it cannot be deleted.
            }
            KwlReturnObject result2 = accCustomerDao.getCustomerForTax(taxid, companyid);
//            String msg = "The Tax code(s) is or had been associated with " + " " + result2.getEntityList().toString() + " " + "Customer(s). So it cannot be deleted";
            String msg=messageSource.getMessage("acc.tax.excptaxcust",null, RequestContextUtils.getLocale(request))+result2.getEntityList().toString()+messageSource.getMessage("acc.tax.excptax2",null, RequestContextUtils.getLocale(request));
            int count2 = (int) result2.getRecordTotalCount();
            if (count2 > 0) {
                throw new AccountingException(msg);//Selected Tax is currently associted with Customer(s). So it cannot be deleted.
            }

        } catch (ServiceException ex) {
            throw new AccountingException(messageSource.getMessage("acc.tax.excp1", null, RequestContextUtils.getLocale(request)));
        }

    }
    
}
