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
package com.krawler.spring.accounting.purchaseorder;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.ServerEventManager;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.purchaseorder.service.AccPurchaseOrderModuleService;
import com.krawler.spring.accounting.purchaseorder.service.AccPurchaseOrderModuleServiceImpl;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.CommonFnControllerService;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
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
import javax.mail.MessagingException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accPurchaseOrderController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private String successView;
    private authHandlerDAO authHandlerDAOObj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    private fieldDataManager fieldDataManagercntrl;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private AccPurchaseOrderModuleService accPurchaseOrderModuleServiceObj;
    String recId = "";
    String tranID = "";
    private CommonFnControllerService commonFnControllerService;
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }
    
    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }
    
    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    
    public void setAccPurchaseOrderModuleServiceObj(AccPurchaseOrderModuleService accPurchaseOrderModuleServiceObj) {
        this.accPurchaseOrderModuleServiceObj = accPurchaseOrderModuleServiceObj;
    }
    
    public void setCommonFnControllerService(CommonFnControllerService commonFnControllerService) {
        this.commonFnControllerService = commonFnControllerService;
    }
    public ModelAndView savePurchaseOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj =null;
        boolean issuccess = false;
        String channelName = "";
        try {

            paramJobj = StringUtil.convertRequestToJsonObject(request);
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            paramJobj.put("baseUrl", baseUrl);
            paramJobj.put("locale", RequestContextUtils.getLocale(request));
            String userName = sessionHandlerImpl.getUserFullName(request);
            paramJobj.put(Constants.username,userName);
            jobj = accPurchaseOrderModuleServiceObj.savePurchaseOrderJSON(paramJobj);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
            channelName = jobj.optString(Constants.channelName, null);
        } catch (AccountingException ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", ex.getMessage());
                if(!jobj.has("isAccountingExe")){
                    if (paramJobj.has("isAccountingExe")) {
                        jobj.put("isAccountingExe", paramJobj.optBoolean("isAccountingExe",false));
                    } 
                }
            } catch (JSONException e1x) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e1x);
            }
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * To save security gate entry form 
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView saveSecurityGateEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String channelName = "";
        try {

            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("locale", RequestContextUtils.getLocale(request));
            jobj = accPurchaseOrderModuleServiceObj.saveSecurityGateEntryJSON(paramJobj);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
            channelName = jobj.optString(Constants.channelName, null);
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    
    private HashSet<PurchaseOrderDetail> updatePurchaseOrderRows(String soDetails, int moduleid, String companyid) throws ServiceException, JSONException {
        HashSet<PurchaseOrderDetail> rows = new HashSet<>();
        try {
            JSONArray jArr = new JSONArray(soDetails);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                PurchaseOrderDetail row = null;
                String linkto = jobj.getString("linkto");
                if (jobj.has("rowid")) {
                    KwlReturnObject invDetailsResult = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(), StringUtil.isNullOrEmpty(linkto)?jobj.getString("rowid"):jobj.getString("savedrowid"));
                    row = (PurchaseOrderDetail) invDetailsResult.getEntityList().get(0);
                }

                /*
                 * To change the sequence of product
                 */
                if (row != null) {
                    if (jobj.has("srno")) {
                        row.setSrno(jobj.getInt("srno"));
                    }

                    /*
                     * We can update the descritpion of line item.
                     */
                    if (!StringUtil.isNullOrEmpty(jobj.optString("desc"))) {
                        try {
                            row.setDescription(StringUtil.DecodeText(jobj.optString("desc")));
                        } catch (Exception ex) {
                            row.setDescription(jobj.optString("desc"));
                        }
                    }

                    /*
                     * To update the custom field data of line items.
                     */
                    String customfield = jobj.getString("customfield");

                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        HashMap<String, Object> POMap = new HashMap<>();
                        JSONArray jcustomarray = new JSONArray(customfield);

                        HashMap<String, Object> customrequestParams = new HashMap<>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "PurchaseorderDetail");
                        customrequestParams.put("moduleprimarykey", "PoDetailID");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put("moduleid", moduleid);
                        customrequestParams.put("companyid", companyid);
                        POMap.put("id", row.getID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_PurchaseOrderDetails_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            POMap.put("purchaseordercustomdataref", row.getID());
                            accPurchaseOrderobj.savePurchaseOrderDetails(POMap);
                        }
                    }

                    // Add Custom fields details for Product
                    if (!StringUtil.isNullOrEmpty(jobj.optString("productcustomfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("productcustomfield", "[]"));
                        HashMap<String, Object> customrequestParams = new HashMap<>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "PurchaseorderDetail");
                        customrequestParams.put("moduleprimarykey", "PoDetailID");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
                        customrequestParams.put("recdetailId", row.getID());
                        customrequestParams.put("productId", row.getProduct().getID());
                        customrequestParams.put("companyid", companyid);
                        customrequestParams.put("customdataclasspath", Constants.Acc_PODETAIL_Productcustom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    }
                    rows.add(row);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updatePurchaseOrderRows : " + ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
        return rows;
    }
    private HashSet<ExpensePODetail> updateExpensePurchaseOrderRows(String poDetails, String companyid) throws ServiceException, JSONException {
        HashSet<ExpensePODetail> rows = new HashSet<>();
        try {
            JSONArray jArr = new JSONArray(poDetails);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                ExpensePODetail row = null;
                String linkto = jobj.getString("linkto");
                if (jobj.has("rowid") || jobj.has("savedrowid")) {
                    KwlReturnObject invDetailsResult = accountingHandlerDAOobj.getObject(ExpensePODetail.class.getName(), StringUtil.isNullOrEmpty(linkto)?jobj.getString("rowid"):jobj.getString("savedrowid"));
                    row = (ExpensePODetail) invDetailsResult.getEntityList().get(0);
                }

                /*
                 * To change the sequence of product
                 */
                if (row != null) {
                    if (jobj.has("srno")) {
                        row.setSrno(jobj.getInt("srno"));
                    }

                    /*
                     * We can update the descritpion of line item.
                     */
                    if (!StringUtil.isNullOrEmpty(jobj.optString("desc"))) {
                        try {
                            row.setDescription(StringUtil.DecodeText(jobj.optString("desc")));
                        } catch (Exception ex) {
                            row.setDescription(jobj.optString("desc"));
                        }
                    }

                    /*
                     * To update the custom field data of line items.c
                     */

                    //Saving custom field Data If any
                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "ExpensePODetail");//this is getter/setter part of pojo class method name  
                        customrequestParams.put("moduleprimarykey", "ExpensePODetailID");
                        customrequestParams.put("modulerecid", row.getID());  
                        customrequestParams.put("recdetailId", row.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Purchase_Order_ModuleId ); // Check
                        customrequestParams.put("companyid", companyid);
                        customrequestParams.put("customdataclasspath", Constants.Acc_ExpensePODetail_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    }
                    rows.add(row);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updatePurchaseOrderRows : " + ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
        return rows;
    }

    public List updatePurchaseOrder(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {
        String id = null;
        List ll = new ArrayList();
        ArrayList discountArr = new ArrayList();
        String poid = null;
        PurchaseOrder po = null;
        PurchaseOrder purchaseOrder = null;
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            poid = request.getParameter("invoiceid");
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean isConsignment = (!StringUtil.isNullOrEmpty(request.getParameter("isConsignment"))) ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
            boolean isEdit = true;
            boolean isCopy = false;
            boolean isLinkedTransaction = request.getParameter("isLinkedTransaction") != null ? StringUtil.getBoolean(request.getParameter("isLinkedTransaction")) : false;
            boolean islockQuantity = request.getParameter("islockQuantity") != null ? StringUtil.getBoolean(request.getParameter("islockQuantity")) : false;
            int moduleid = isConsignment?Constants.Acc_ConsignmentVendorRequest_ModuleId:(!isFixedAsset?Constants.Acc_Purchase_Order_ModuleId:Constants.Acc_FixedAssets_Purchase_Order_ModuleId);
            String customfield = request.getParameter("customfield");
            /*
             * To update the following items which is not affecting the amount
             * and linking of the Invoice.
             */
            HashMap<String, Object> poPrmt = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(poid)) {
                poPrmt.put("id", poid); 
            }
            poPrmt.put("orderdate", df.parse(request.getParameter("billdate")));
            poPrmt.put("memo", request.getParameter("memo") == null ? "" : request.getParameter("memo"));
            poPrmt.put("billto", request.getParameter("billto") == null ? "" : request.getParameter("billto"));
            poPrmt.put("shipaddress", request.getParameter("shipaddress") == null ? "" : request.getParameter("shipaddress"));
            if (request.getParameter("shipdate") != null && !StringUtil.isNullOrEmpty(request.getParameter("shipdate"))) {
                poPrmt.put("shipdate", df.parse(request.getParameter("shipdate")));
            }
            poPrmt.put("customerporefno", request.getParameter("customerporefno") == null ? "" : request.getParameter("customerporefno"));
            poPrmt.put("companyid", companyid);
            poPrmt.put("salesPerson", request.getParameter("salesPerson") == null ? "" : request.getParameter("salesPerson"));
            poPrmt.put("shipvia", request.getParameter("shipvia"));
            poPrmt.put("fob", request.getParameter("fob") == null ? "" : request.getParameter("fob"));
            poPrmt.put("modifiedby", sessionHandlerImpl.getUserid(request));
            poPrmt.put("updatedon", System.currentTimeMillis());
            poPrmt.put("posttext", request.getParameter("posttext"));
            poPrmt.put("costCenterId", request.getParameter("costcenter"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("deliveryTime"))) {
                poPrmt.put("deliveryTime", request.getParameter("deliveryTime"));
            }

            KwlReturnObject invResult = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), poid);
           po = (PurchaseOrder) invResult.getEntityList().get(0);

            Map<String, Object> addressParams = new HashMap<>();
            String billingAddress = request.getParameter(Constants.BILLING_ADDRESS);
            if (!StringUtil.isNullOrEmpty(billingAddress)) {
                addressParams = AccountingAddressManager.getAddressParams(request, false);
            } else {
                addressParams = AccountingAddressManager.getDefaultVendorAddressParams(po.getVendor().getID(), companyid, accountingHandlerDAOobj);// addressParams = getCustomerDefaultAddressParams(customer,companyid);
            }
            BillingShippingAddresses bsa = po.getBillingShippingAddresses();//used to update billing shipping addresses
            addressParams.put("id", bsa != null ? bsa.getID() : "");
            KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
            bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
            String addressid = bsa.getID();
            poPrmt.put("billshipAddressid", addressid);
            /*
             * Updating line item information.
             */
            if (po.isIsExpenseType()) {
                String expensedetail = request.getParameter("expensedetail");
                HashSet<ExpensePODetail> podetails = updateExpensePurchaseOrderRows(expensedetail, companyid);
                poPrmt.put("expensedetail", podetails);
            } else {
                String soDetails = request.getParameter("detail");
                HashSet<PurchaseOrderDetail> podetails = updatePurchaseOrderRows(soDetails, moduleid, companyid);
                poPrmt.put("podetails", podetails);
            }
            
            /*
             * Updating Custom field data.
             */
            
            if (!StringUtil.isNullOrEmpty(customfield)) {
                HashMap<String, Object> POMap = new HashMap<>();
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "PurchaseOrder");
                customrequestParams.put("moduleprimarykey", "PoID");
                customrequestParams.put("modulerecid", po.getID());
                customrequestParams.put("moduleid", moduleid);
                customrequestParams.put("companyid", companyid);
                POMap.put("poid", po.getID());
                customrequestParams.put("customdataclasspath", Constants.Acc_PurchaseOrder_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    if (customDataresult.getEntityList().get(0) != null) {
                        POMap.put("purchaseordercustomdataref", po.getID());
                        accPurchaseOrderobj.updatePurchaseOrder(POMap);
                    }
                }
            }
            
            String disableSOForPO = request.getParameter("blockSOPO") != null ? request.getParameter("blockSOPO") : "";
            boolean isBlockDocument = false;
            if (!StringUtil.isNullOrEmpty(disableSOForPO) && disableSOForPO.equalsIgnoreCase("on")) {
                isBlockDocument = true;
            }
            poPrmt.put("isLinkedSOBlocked", isBlockDocument);
            
            poPrmt.put("isEdit", isEdit);
            poPrmt.put("isLinkedTransaction", isLinkedTransaction);
            poPrmt.put("isCopy", isCopy);
            KwlReturnObject result = accPurchaseOrderobj.savePurchaseOrder(poPrmt);
            purchaseOrder = (PurchaseOrder) result.getEntityList().get(0);//Create Invoice without invoice-details.
            id = purchaseOrder.getID();
            /*
             * Data for return information.
             */
            String personalid = purchaseOrder.getVendor().getAccount().getID();
            String accname = purchaseOrder.getVendor().getAccount().getName();
            String purchaseOrderNo = purchaseOrder.getPurchaseOrderNumber();
            String address = purchaseOrder.getVendor().getAddress();
            String fullShippingAddress = "";
            if (purchaseOrder.getBillingShippingAddresses() != null) {
                fullShippingAddress = purchaseOrder.getBillingShippingAddresses().getFullShippingAddress();
            }
            tranID = id;
            recId = purchaseOrderNo;
            ll.add(new String[]{id});
            ll.add(discountArr);
            ll.add((purchaseOrder.getPendingapproval() == 1) ? "Pending Approval" : "Approved");
            ll.add(personalid);
            ll.add(accname);
            ll.add(purchaseOrderNo);
            ll.add(address);
            ll.add(purchaseOrder.getTotalamount());
            ll.add("");
            ll.add(fullShippingAddress);
        } catch (ParseException | JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }catch (AccountingException ex) {
            throw new AccountingException(ex.getMessage());
        }
        catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
        return ll;
    }

    public ModelAndView updateLinkedPurchaseOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isAccountingExe = false;
        String msg = "", channelName = "", invoiceid = "", deliveryOid = "", billNo = "", doinvflag = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        status = txnManager.getTransaction(def);    
        try {
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean isConsignment = request.getParameter("isConsignment") != null ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
            List li = updatePurchaseOrder(request);
            String[] id = (String[]) li.get(0);
            String billno = (String) li.get(5);
            invoiceid = (String)id[0];
            if(!StringUtil.isNullOrEmpty(invoiceid)){
               KwlReturnObject res = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), invoiceid);
               PurchaseOrder purchaseOrder = (PurchaseOrder) res.getEntityList().get(0);
               jobj.put("isExpenseInv", purchaseOrder != null ? purchaseOrder.isIsExpenseType() : false);
            }
            jobj.put("invoiceid", li.get(0));
            jobj.put("accountid", li.get(3));
            jobj.put("accountName", li.get(4));
            jobj.put("invoiceNo", li.get(5));
            jobj.put("address", li.get(6));
            jobj.put("amount", li.get(7));
            jobj.put("fullShippingAddress", li.get(9));

            txnManager.commit(status);
            issuccess = true;
            /*
             * To refresh a Invoice List.
             */
            if (isFixedAsset) {
                channelName = "/FixedAssetPurchaseOrderList/gridAutoRefresh";
            } else if (!(isConsignment)) {//For normal PO
                channelName = "/PurchaseOrderReport/gridAutoRefresh";
            }
            /*
             * Composing the message to display after save operation.
             */
            if (isLeaseFixedAsset) {
                msg = messageSource.getMessage("acc.lso.update", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";   //"Sales order has been saved successfully";
            } else if (isConsignment) {
                msg = messageSource.getMessage("acc.consignment.order.update", null, RequestContextUtils.getLocale(request)) +"<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";   //"consignment saved successfully";
            } else {
                msg = messageSource.getMessage("acc.po.update", null, RequestContextUtils.getLocale(request));
                msg += "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";
            }
            
            /*
             * Composing the message to insert into Audit Trail.
             */
            String action = "updated";
            if (isLeaseFixedAsset) {
                action += " Lease";
            }
            auditTrailObj.insertAuditLog(AuditAction.PURCHASE_ORDER, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + (isConsignment ? " Consignment Purchase Order " : " Purchase Order ") + recId, request, tranID);

        } catch (SessionExpiredException | ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            //isAccountingExe = true;
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("isAccountingExe", isAccountingExe);
                jobj.put("invid", invoiceid);
                jobj.put("doid", deliveryOid);
                jobj.put("dono", billNo);
                jobj.put("doinvflag", doinvflag);

                if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                    jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
                    ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
                }
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
         
     public ModelAndView updateVQScript(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String channelName = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                requestParams.put("linkFlag", "2");
                KwlReturnObject result = null;
                result = accPurchaseOrderobj.getQuotationsForScript(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    String linkNumbers = (String) itr.next();
                    if (!StringUtil.isNullOrEmpty(linkNumbers)) {
                        accPurchaseOrderModuleServiceObj.updateVQisOpenAndLinking(linkNumbers);
                    }
                }
            }
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed update the isOpen Flag in VQ");

            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
        
    public ModelAndView deletePurchaseOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isConsignment = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isConsignment))) {
            isConsignment = Boolean.parseBoolean(request.getParameter(Constants.isConsignment));
        }
        boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isFixedAsset))) ? Boolean.parseBoolean(request.getParameter(Constants.isFixedAsset)) : false;
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            String linkedTransaction = deletePurchaseOrders(requestJobj);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                if (isFixedAsset) {
                    msg = (messageSource.getMessage("acc.field.assetPurchaseOrdersHasBeenDeletedSuccessfully", null, RequestContextUtils.getLocale(request)));
                } else {
                    msg = isConsignment ? messageSource.getMessage("acc.consignment.order.del", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.po.del", null, RequestContextUtils.getLocale(request));   //"Purchase Order(s) has been deleted successfully";
                }
            } else {
                if (isFixedAsset) {
                    msg = (messageSource.getMessage("acc.field.assetPurchaseOrderssExcept", null, RequestContextUtils.getLocale(request))) + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsucessfully", null, RequestContextUtils.getLocale(request));
                } else {
                    msg = (isConsignment ? messageSource.getMessage("acc.field.consignmentOexcept", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.field.Prchaseorderssexcept", null, RequestContextUtils.getLocale(request))) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsucessfully", null, RequestContextUtils.getLocale(request));   //"Purchase Order(s) has been deleted successfully";
                }
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deletePurchaseOrders(JSONObject requestJobj) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(requestJobj.optString(Constants.RES_data));
            String companyid = requestJobj.optString(Constants.companyKey);
            String linkedTransaction = "";
            boolean isConsignment = false;
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.isConsignment))) {
                isConsignment = Boolean.parseBoolean(requestJobj.optString(Constants.isConsignment));
            }
            boolean isFixedAsset = false;
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.isFixedAsset))) {
                isFixedAsset = Boolean.parseBoolean(requestJobj.optString(Constants.isFixedAsset));
            }
            String modulename = "";
            if (isConsignment) {
                modulename = " " + messageSource.getMessage("acc.venconsignment.order", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))) + " ";
            } else if (isFixedAsset) {
                modulename = " " + messageSource.getMessage("acc.field.assetPurchaseOrder", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))) + " ";
            } else {
                modulename = " " + messageSource.getMessage("acc.dimension.module.10", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))) + " ";
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString(Constants.billid))) {
                    linkedTransaction = accPurchaseOrderModuleServiceObj.deletePurchaseOrder(jobj, requestJobj, linkedTransaction, companyid, modulename);
                }
            }
            return linkedTransaction;
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
        }
    }
    
    /**
     * To delete security gate entry record from report
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView deleteSecuriyGateEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String linkedTransaction = deleteSecurityGate(request);
            txnManager.commit(status);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                 msg =  messageSource.getMessage("acc.security.del", null, RequestContextUtils.getLocale(request));   //"Purchase Order(s) has been deleted successfully";
            } else {
                 msg =  messageSource.getMessage("acc.security.securityexcept", null, RequestContextUtils.getLocale(request)) +" "+ linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsucessfully", null, RequestContextUtils.getLocale(request));   //"Purchase Order(s) has been deleted successfully";
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * To  delete security gate entry from report
     * @param request
     * @return
     * @throws SessionExpiredException
     * @throws AccountingException
     * @throws ServiceException 
     */
    public String deleteSecurityGate(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String linkedTransaction = "";
            String modulename = "";
            modulename = " Security Gate Entry ";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String securityId = StringUtil.DecodeText(jobj.optString("billid"));
                    
                    KwlReturnObject res = accountingHandlerDAOobj.getObject(SecurityGateEntry.class.getName(), securityId);
                    SecurityGateEntry securityGateEntry = (SecurityGateEntry) res.getEntityList().get(0);

                    String pono = securityGateEntry.getSecurityNumber();//jobj.getString("billno");
                    /*
                     * To check SGE link to GR
                     */ 
                    KwlReturnObject resultd = accPurchaseOrderobj.getGROforSGE(securityId, companyid);  //for cheching SO is used in DO or not
                    int count = resultd.getRecordTotalCount();
                    if (count > 0) {
                        linkedTransaction += pono + ", ";
                        continue;
                    }
                    String actionMsg = "deleted";
                    boolean isReject = StringUtil.isNullOrEmpty(request.getParameter("isReject")) ? false : Boolean.parseBoolean(request.getParameter("isReject"));

                    if (isReject == true) {
                        actionMsg = "rejected";
                    }
                    auditTrailObj.insertAuditLog("86", " User " + sessionHandlerImpl.getUserFullName(request) + " has " + actionMsg + modulename + pono, request, securityId);
                    accPurchaseOrderobj.deleteSecurityGate(securityId, companyid);

                }
            }
            return linkedTransaction;
        }/*
         * catch (UnsupportedEncodingException ex) { throw
         * ServiceException.FAILURE(messageSource.getMessage("acc.common.excp",
         * null, RequestContextUtils.getLocale(request)), ex);
        }
         */ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

    public ModelAndView deletePurchaseOrdersPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isConsignment = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isConsignment))) {
            isConsignment = Boolean.parseBoolean(request.getParameter(Constants.isConsignment));
        }
        boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isFixedAsset))) ? Boolean.parseBoolean(request.getParameter(Constants.isFixedAsset)) : false;
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            requestJobj.put("servletContext", this.getServletContext());
            String linkedTransaction = accPurchaseOrderModuleServiceObj.deletePurchaseOrdersPermanent(requestJobj);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                if (isFixedAsset) {
                    msg = messageSource.getMessage("acc.field.assetPurchaseOrdersHasBeenDeletedSuccessfully" + linkedTransaction, null, RequestContextUtils.getLocale(request));
                } else {
                    msg = isConsignment ? messageSource.getMessage("acc.venconsignment.order.del", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.po.del" + linkedTransaction, null, RequestContextUtils.getLocale(request));   //"Purchase Order(s) has been deleted successfully";
                }
            } else {
                if (isFixedAsset) {
                    msg = messageSource.getMessage("acc.field.assetPurchaseOrderssExcept", null, RequestContextUtils.getLocale(request)) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsucessfully", null, RequestContextUtils.getLocale(request));
                } else {
                    msg = isConsignment ? (messageSource.getMessage("acc.venfield.consignmentOexcept", null, RequestContextUtils.getLocale(request)) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsucessfully", null, RequestContextUtils.getLocale(request))) : (messageSource.getMessage("acc.field.Prchaseorderssexcept", null, RequestContextUtils.getLocale(request)) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsucessfully", null, RequestContextUtils.getLocale(request)));   //"Purchase Order(s) has been deleted successfully";
                }
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    
    
    /**
     * to delete security gate entry record from report
     * @param request
     * @param response
     * @return 
     */
     public ModelAndView deleteSecurityGateEntryPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String linkedTransaction = deleteSecurityGateEntryPermanent(request);
            txnManager.commit(status);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                    msg =messageSource.getMessage("acc.security.delp" + linkedTransaction, null, RequestContextUtils.getLocale(request));   //"Purchase Order(s) has been deleted successfully";
            } else {
                    msg =(messageSource.getMessage("acc.security.securityexcept", null, RequestContextUtils.getLocale(request)) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsucessfully", null, RequestContextUtils.getLocale(request)));   //"Purchase Order(s) has been deleted successfully";
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteSecurityGateEntryPermanent(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransaction = "";
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String securityId = "", securityNo = "";
            String modulename = "";
            boolean isopen = false;
            modulename = " Security Gate Entry";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                securityId = StringUtil.DecodeText(jobj.optString("billid"));

                KwlReturnObject res = accountingHandlerDAOobj.getObject(SecurityGateEntry.class.getName(), securityId);
                SecurityGateEntry securityGateEntry = (SecurityGateEntry) res.getEntityList().get(0);

                securityNo = securityGateEntry.getSecurityNumber();//jobj.getString("billno");

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("securityId", securityId);
                requestParams.put("companyid", companyid);
                requestParams.put("securityNo", securityNo);
                if (!StringUtil.isNullOrEmpty(securityId)) {
                    KwlReturnObject resultd = accPurchaseOrderobj.getGROforSGE(securityId, companyid);  //for cheching SO is used in DO or not
                    int count = resultd.getRecordTotalCount();
                    if (count > 0) {
                        linkedTransaction += securityNo + ", ";
                        continue;
                    }
                    double quantity_used_in_sge = 0d;
                    
                    HashMap<String, Object> doRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                    filter_names.add("securityGateEntry.ID");
                    filter_params.add(securityGateEntry.getID());
                    doRequestParams.put("filter_names", filter_names);
                    doRequestParams.put("filter_params", filter_params);
                    
                    KwlReturnObject podresult = accPurchaseOrderobj.getSecurityGateDetails(doRequestParams);
                    Iterator itr = podresult.getEntityList().iterator();
                    while (itr.hasNext()) {
                        SecurityGateDetails row = (SecurityGateDetails) itr.next();
                        if (row.getPodetail() != null) {
                            String linkid = row.getPodetail().getPurchaseOrder().getID();
                            if (!StringUtil.isNullOrEmpty(linkid)) {
                                
                                
                                KwlReturnObject idresult = accPurchaseOrderobj.getSGIDFromPOD(row.getPodetail().getID(), securityGateEntry.getID());
                                List list = idresult.getEntityList();
                                Iterator iteGRD = list.iterator();
                                double qua = 0.0;
                                while (iteGRD.hasNext()) {
                                    SecurityGateDetails sge = (SecurityGateDetails) iteGRD.next();
                                    if(sge.getSecurityGateEntry().getID().equals(securityGateEntry.getID())){//Get quantity of current deleting SGE only
                                        qua += sge.getQuantity();
                                    }else{
                                        quantity_used_in_sge += sge.getQuantity();
                                    }
                                }
                                /*
                                *ERM-1099
                                *As balance quantity is not changed when creating SGE it also will not change when deleting SGE
                                */
//                                if (row != null) {
//                                    if (row != null && !StringUtil.isNullOrEmpty(row.getID()) && qua > 0) {
//                                        HashMap poMap = new HashMap();
//                                        poMap.put("podetails", row.getPodetail().getID());
//                                        poMap.put("companyid", row.getCompany().getCompanyID());
//                                        poMap.put("balanceqty", qua);
//                                        poMap.put("add", true);
//                                        accCommonTablesDAO.updatePurchaseOrderStatus(poMap);
//                                    }
//                                }
                                
                                double addobj = row.getPodetail().getBalanceqty() - qua;
                                if (addobj > 0) {
                                    isopen = true;
                                }
                                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), linkid);
                                PurchaseOrder purchaseOrder = (PurchaseOrder) rdresult.getEntityList().get(0);

                                HashMap hMap = new HashMap();
                                hMap.put("purchaseOrder", purchaseOrder);
                                hMap.put("isopen", isopen);
                                if (quantity_used_in_sge == 0.0) {
                                    hMap.put("value", "0");
                                    purchaseOrder.setIsPoUsed(false);
                                }
                                accPurchaseOrderobj.updatePOLinkflag(hMap);
                                
                            }
                        }
                    }
                    if (quantity_used_in_sge == 0.0) {//If PO is not used in SGE then set isPOUsed flag false to load PO in all transaction
                        accPurchaseOrderobj.updatePoIsUsedDeleteSGE(securityId, companyid, false);
                    }
                    accPurchaseOrderobj.deleteSecurityGateEntryPermanent(requestParams);
                    auditTrailObj.insertAuditLog("86", " User " + sessionHandlerImpl.getUserFullName(request) + " has deleted" + modulename + " Permanently " + securityNo, request, securityId);
                }
            }
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } */catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return linkedTransaction;
    }
    
    public ModelAndView saveBillingPurchaseOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            int pendingApprovalFlag = saveBillingPurchaseOrder(request);
            boolean pendingApproval = false;
            issuccess = true;
            int istemplate = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("istemplate"))) {
                istemplate = Integer.parseInt(request.getParameter("istemplate"));
            }
            if (istemplate == 1) {
                msg = messageSource.getMessage("acc.field.PurchaseOrderandTemplatehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) + ((pendingApprovalFlag == 1) ? messageSource.getMessage("acc.field.butPurchaseOrderispendingforApproval", null, RequestContextUtils.getLocale(request)) : ".");
            } else if (istemplate == 2) {
                msg = messageSource.getMessage("acc.field.PurchaseOrderTemplatehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.po.save1", null, RequestContextUtils.getLocale(request)) + " " + ((pendingApprovalFlag == 1) ? messageSource.getMessage("acc.field.butpendingforApproval", null, RequestContextUtils.getLocale(request)) : ".");
            }
            if (pendingApprovalFlag == 1) {
                pendingApproval = true;
            }
            jobj.put("pendingApproval", pendingApproval);
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int saveBillingPurchaseOrder(HttpServletRequest request) throws ServiceException, AccountingException, JSONException {
        BillingPurchaseOrder bPurchaseOrder = null;
        int pendingApprovalFlag = 0;
        try {
            int istemplate = request.getParameter("istemplate") != null ? Integer.parseInt(request.getParameter("istemplate")) : 0;
            String taxid = null;
            taxid = request.getParameter("taxid");
            double taxamount = StringUtil.getDouble(request.getParameter("taxamount"));
            String sequenceformat = request.getParameter("sequenceformat");

            String companyid = sessionHandlerImpl.getCompanyid(request);
            String entryNumber = request.getParameter("number");
            String costCenterId = request.getParameter("costcenter");
            String poid = request.getParameter("invoiceid");
            String nextAutoNumber;

            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            HashMap<String, Object> poDataMap = new HashMap<String, Object>();

//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
//            BillingPurchaseOrder purchaseOrder = new BillingPurchaseOrder();
//            String entryNumber = request.getParameter("number");
//            String q = "from BillingPurchaseOrder where purchaseOrderNumber=? and company.companyID=?";
//            if (!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty()) {
//                throw new AccountingException("Purchase Order number '" + entryNumber + "' already exists.");
//            }
            KwlReturnObject pocnt = accPurchaseOrderobj.getBPOCount(entryNumber, companyid);
            if (pocnt.getRecordTotalCount() > 0 && istemplate != 2) {
                if (StringUtil.isNullOrEmpty(poid)) {
                    throw new AccountingException(messageSource.getMessage("acc.field.PurchaseOrdernumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                } else {
                    nextAutoNumber = entryNumber;
                    poDataMap.put("id", poid);
                    accPurchaseOrderobj.deleteBillingPurchaseOrderDetails(poid, companyid);
                }
            }

//            purchaseOrder.setPurchaseOrderNumber(entryNumber);
//            purchaseOrder.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_BILLINGPURCHASEORDER).equals(entryNumber));

//            purchaseOrder.setMemo(request.getParameter("memo"));
//            purchaseOrder.setVendor((Vendor) session.get(Vendor.class, request.getParameter("vendor")));
//            if (!StringUtil.isNullOrEmpty(request.getParameter("creditoraccount"))) {
//                purchaseOrder.setDebitFrom((Account) session.get(Account.class, request.getParameter("creditoraccount")));
//            }
//            purchaseOrder.setOrderDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")));
//            purchaseOrder.setDueDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("duedate")));
//            purchaseOrder.setCompany(company);

            DateFormat df = authHandler.getDateOnlyFormat(request);
            boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
            String nextAutoNoInt = "";
            String datePrefix = "";
            String dateafterPrefix = "";
            String dateSuffix = "";
            if (seqformat_oldflag) {
                nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGSALESORDER, sequenceformat);
            } else {
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_BILLINGSALESORDER, sequenceformat, seqformat_oldflag, new Date());
                nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part

                poDataMap.put(Constants.SEQFORMAT, sequenceformat);
                poDataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                poDataMap.put(Constants.DATEPREFIX, datePrefix);
                poDataMap.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                poDataMap.put(Constants.DATESUFFIX, dateSuffix);
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter("perdiscount"))) {
                poDataMap.put("perDiscount", StringUtil.getBoolean(request.getParameter("perdiscount")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("discount"))) {
                poDataMap.put("discount", StringUtil.getDouble(request.getParameter("discount")));
            }
            poDataMap.put("entrynumber", entryNumber);
            poDataMap.put("autogenerated", nextAutoNumber.equals(entryNumber));
            poDataMap.put("memo", request.getParameter("memo"));
            poDataMap.put("posttext", request.getParameter("posttext"));
            poDataMap.put("vendorid", request.getParameter("vendor"));
            //poDataMap.put("debitfrom", request.getParameter("creditoraccount"));
            poDataMap.put("orderdate", df.parse(request.getParameter("billdate")));
            poDataMap.put("duedate", df.parse(request.getParameter("duedate")));
            if (request.getParameter("shipdate") != null && !StringUtil.isNullOrEmpty(request.getParameter("shipdate"))) {
                poDataMap.put("shipdate", df.parse(request.getParameter("shipdate")));
            }
            poDataMap.put("shipvia", request.getParameter("shipvia"));
            poDataMap.put("fob", request.getParameter("fob"));
            poDataMap.put("termid", request.getParameter("termid"));
            poDataMap.put("currencyid", currencyid);
            poDataMap.put("venbilladdress", request.getParameter("venbilladdress"));
            poDataMap.put("venshipaddress", request.getParameter("venshipaddress"));
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                poDataMap.put("costCenterId", costCenterId);
            }
            poDataMap.put("companyid", companyid);

            if (taxid != null && !taxid.isEmpty()) {
                Tax tax = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxid);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                }
//                purchaseOrder.setTax(tax);
                poDataMap.put("taxid", taxid);
            } else if (taxid.isEmpty()) {
                poDataMap.put("taxid", taxid);
            }

            //save Billing Sales Order
            KwlReturnObject result = accPurchaseOrderobj.saveBillingPurchaseOrder(poDataMap);
            bPurchaseOrder = (BillingPurchaseOrder) result.getEntityList().get(0);

            //save Billing Sales Order Details
            List podetails = saveBillingPurchaseOrderRows(request, bPurchaseOrder, companyid, currencyid, GlobalParams, externalCurrencyRate);
            accPurchaseOrderobj.deletePurchaseOrderOtherDetails(bPurchaseOrder.getID(), companyid);
            
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            accPurchaseOrderModuleServiceObj.savePurchaseOrderOtherDetails(paramJobj, bPurchaseOrder.getID(), companyid);

            double poAmount = (Double) podetails.get(1) + taxamount;
            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, poAmount, currencyid, df.parse(request.getParameter("billdate")), externalCurrencyRate);
            poAmount = (Double) bAmt.getEntityList().get(0);
            ArrayList amountApprove = (accountingHandlerDAOobj.getApprovalFlagForAmount(poAmount, Constants.PURCHASE_ORDER, Constants.TRANS_AMOUNT, companyid));
            pendingApprovalFlag = (istemplate != 2 ? ((Boolean) (amountApprove.get(0)) ? 1 : 0) : 0);//No need of approval if transaction is saved as only template
            poDataMap.put("id", bPurchaseOrder.getID());
            poDataMap.put("podetails", (HashSet) podetails.get(0));
            result = accPurchaseOrderobj.saveBillingPurchaseOrder(poDataMap);
            bPurchaseOrder = (BillingPurchaseOrder) result.getEntityList().get(0);
            bPurchaseOrder.setPendingapproval(pendingApprovalFlag);
            bPurchaseOrder.setIstemplate(istemplate);
            bPurchaseOrder.setApprovallevel((Integer) (amountApprove.get(1)));

            if (pendingApprovalFlag == 1) { //this for send approval email
                String[] emails = {};
                String invoiceNumber = bPurchaseOrder.getPurchaseOrderNumber();
                String userName = sessionHandlerImpl.getUserFullName(request);
                String moduleName = "Purchase Order";
                emails = accountingHandlerDAOobj.getApprovalUserList(request, moduleName, 1);
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                String fromEmailId = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                //String fromEmailId = "admin@deskera.com";
                /**
                 * parameters required for sending mail
                 */
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put(Constants.companyid, companyid);
                mailParameters.put(Constants.prNumber, invoiceNumber);
                mailParameters.put(Constants.modulename, moduleName);
                mailParameters.put(Constants.fromName, userName);
                mailParameters.put(Constants.fromEmailID, fromEmailId);
                mailParameters.put(Constants.PAGE_URL, URLUtil.getPageURL(request, loginpageFull));
                mailParameters.put(Constants.emails, emails); 
                accountingHandlerDAOobj.sendApprovalEmails(mailParameters);
            }
            //Save record as template
            if (!StringUtil.isNullOrEmpty(request.getParameter("templatename")) && (istemplate == 1 || istemplate == 2)) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                String moduletemplateid = request.getParameter("moduletemplateid");
                hashMap.put("templatename", request.getParameter("templatename"));
                if (!StringUtil.isNullOrEmpty(moduletemplateid)) {
                    hashMap.put("moduletemplateid", moduletemplateid);
                }
                hashMap.put("moduleid", Constants.Acc_BillingPurchase_Order_ModuleId);
                hashMap.put("modulerecordid", bPurchaseOrder.getID());
                hashMap.put("companyid", companyid);
                if(!StringUtil.isNullOrEmpty(request.getParameter("companyunitid"))){
                    hashMap.put("companyunitid", request.getParameter("companyunitid")); // Added Unit ID if it is present in request
                }
                /**
                 * checks the template name is already exist in create and edit template case
                 */
                KwlReturnObject templateResult = accountingHandlerDAOobj.getModuleTemplateForTemplatename(hashMap);
                int nocount = templateResult.getRecordTotalCount();
                if (nocount > 0) {
                    throw new AccountingException(messageSource.getMessage("acc.tmp.templateNameAlreadyExists", null, RequestContextUtils.getLocale(request)));
                }
                accountingHandlerDAOobj.saveModuleTemplate(hashMap);
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveBillingPurchaseOrder : " + ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("saveBillingPurchaseOrder : " + ex.getMessage(), ex);
        }
        return pendingApprovalFlag;
    }

    private List saveBillingPurchaseOrderRows(HttpServletRequest request, BillingPurchaseOrder purchaseOrder, String companyid, String currencyid, HashMap<String, Object> GlobalParams, double externalCurrencyRate) throws ServiceException, AccountingException {
        HashSet rows = new HashSet();
        List ll = new ArrayList();
        try {
            double totalAmount = 0.0;
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            for (int i = 0; i < jArr.length(); i++) {
                double rowAmount = 0;
                JSONObject jobj = jArr.getJSONObject(i);
//                BillingPurchaseOrderDetail row = new BillingPurchaseOrderDetail();
//                row.setCompany(company);
//                row.setPurchaseOrder(purchaseOrder);
//                row.setProductDetail(StringUtil.DecodeText(jobj.optString("productdetail")));
//                row.setRate(jobj.getDouble("rate"));
//                row.setQuantity(jobj.getInt("quantity"));
//                row.setRemark(jobj.optString("remark"));
                HashMap<String, Object> podDataMap = new HashMap<String, Object>();
                podDataMap.put("srno", i + 1);
                podDataMap.put("companyid", companyid);
                podDataMap.put("poid", purchaseOrder.getID());
                podDataMap.put("rate", jobj.getDouble("rate"));
                podDataMap.put("debitfrom", jobj.getString("creditoraccount"));
                podDataMap.put("quantity", jobj.getDouble("quantity"));
                podDataMap.put("remark", jobj.optString("remark"));
                podDataMap.put("productdetail", StringUtil.DecodeText(jobj.optString("productdetail")));
                String rowtaxid = jobj.getString("prtaxid");
                if (jobj.has("prdiscount") && jobj.get("prdiscount") != null) {
                    podDataMap.put("discount", jobj.getDouble("prdiscount"));
                }
                if (jobj.has("discountispercent") && jobj.get("discountispercent") != null) {
                    podDataMap.put("discountispercent", jobj.getInt("discountispercent"));
                }

                rowAmount = jobj.getDouble("rate") * jobj.getDouble("quantity");

                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null) {
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    } else {
                        podDataMap.put("rowtaxid", rowtaxid);
                        double rowtaxamount = StringUtil.getDouble(jobj.getString("taxamount"));
                        podDataMap.put("rowTaxAmount", rowtaxamount);
                        rowAmount = rowAmount + rowtaxamount;
                    }

                }
                //  row.setTax(rowtax);
                KwlReturnObject result = accPurchaseOrderobj.saveBillingPurchaseOrderDetails(podDataMap);
                BillingPurchaseOrderDetail row = (BillingPurchaseOrderDetail) result.getEntityList().get(0);
                rows.add(row);
                totalAmount += rowAmount;
            }
            ll.add(rows);
            ll.add(totalAmount);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveBillingPurchaseOrderRows : " + ex.getMessage(), ex);
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        }*/
        return ll;
    }

    public ModelAndView deleteBillingPurchaseOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteBillingPurchaseOrders(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.so.del", null, RequestContextUtils.getLocale(request));   //"Purchase Order has been deleted successfully";
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteBillingPurchaseOrders(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String poid = StringUtil.DecodeText(jobj.optString("billid"));
                    accPurchaseOrderobj.deleteBillingPurchaseOrder(poid, companyid);
                }
            }
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }
    }

    public ModelAndView approvePendingOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        String billno = request.getParameter("billno");
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            approvePendingOrders(request);
            issuccess = true;
            auditTrailObj.insertAuditLog(AuditAction.PURCHASEORDERAPPROVED, "User " + sessionHandlerImpl.getUserFullName(request) + " has Approved a Purcahse Order  " + billno, request, billno);
            txnManager.commit(status);
            msg = messageSource.getMessage("acc.field.Purchaseorderhasbeenupdatedsuccessfully", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void approvePendingOrders(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {

        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userid = sessionHandlerImpl.getUserid(request);
        String billid = request.getParameter("billid");
        Boolean isbilling = Boolean.parseBoolean(request.getParameter("isbilling"));
        String remark = request.getParameter("remark");

        boolean isSendMailForNextLevelUsers = true;
        String invoiceNumber = "";

        KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
        String baseUrl = URLUtil.getPageURL(request, loginpageFull);

        if (isbilling) {
            KwlReturnObject BillingPurchaseOrderObj = accountingHandlerDAOobj.getObject(BillingPurchaseOrder.class.getName(), billid);
            BillingPurchaseOrder billingPurchaseOrder = (BillingPurchaseOrder) BillingPurchaseOrderObj.getEntityList().get(0);
            invoiceNumber = billingPurchaseOrder.getPurchaseOrderNumber();
            if (billingPurchaseOrder.getPendingapproval() == billingPurchaseOrder.getApprovallevel()) {
                isSendMailForNextLevelUsers = false;
            }
        } else {
            KwlReturnObject purchaseOrderObj = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), billid);
            PurchaseOrder purchaseOrder = (PurchaseOrder) purchaseOrderObj.getEntityList().get(0);
            invoiceNumber = purchaseOrder.getPurchaseOrderNumber();
            if (purchaseOrder.getPendingapproval() == purchaseOrder.getApprovallevel()) {
                isSendMailForNextLevelUsers = false;
            }
        }
        int approvedLevel = accPurchaseOrderobj.approvePendingOrder(billid, isbilling, companyid, userid);
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("transtype", Constants.PURCHASE_ORDER);
        hashMap.put("transid", billid);
        hashMap.put("approvallevel", approvedLevel);
        hashMap.put("remark", remark);
        hashMap.put("userid", userid);
        hashMap.put("companyid", companyid);
        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
        if (preferences.isSendapprovalmail()) { //If allow in Company Account Preferences
          KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                String sendorInfo = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
            if (isSendMailForNextLevelUsers) {   //send mail if level is not a final level
                String[] emails = {};
                String userName = sessionHandlerImpl.getUserFullName(request);
                emails = accountingHandlerDAOobj.getApprovalUserList(request, Constants.ACC_PURCHASE_ORDER, 2);
                if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                    String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                    emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                }
                /**
                 * parameters required for sending mail
                 */
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put(Constants.companyid, companyid);
                mailParameters.put(Constants.prNumber, invoiceNumber);
                mailParameters.put(Constants.modulename,  Constants.ACC_PURCHASE_ORDER);
                mailParameters.put(Constants.fromName, userName);
                mailParameters.put(Constants.fromEmailID, sendorInfo);
                mailParameters.put(Constants.PAGE_URL, baseUrl);
                mailParameters.put(Constants.emails, emails);
                accountingHandlerDAOobj.sendApprovalEmails(mailParameters);
            } else {    //send mail to company creator after final approval
                
                String creatoremail = company.getCreator().getEmailID();
                String fname = company.getCreator().getFirstName() == null ? "" : company.getCreator().getFirstName();
                String lname = company.getCreator().getLastName() == null ? "" : company.getCreator().getLastName();
                String creatorname = fname + " " + lname;
                String approvalpendingStatusmsg = "";
                String[] emails = {creatoremail};
                String userName = sessionHandlerImpl.getUserFullName(request);
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put(Constants.companyKey, companyid);
                qdDataMap.put("level", approvedLevel);
                qdDataMap.put(Constants.moduleid, Constants.Acc_Purchase_Order_ModuleId);
                if (approvedLevel < 11) {
                approvalpendingStatusmsg=commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put("Number", invoiceNumber);
                mailParameters.put("userName", userName);
                mailParameters.put("emails", emails);
                mailParameters.put("moduleName", Constants.ACC_PURCHASE_ORDER);
                mailParameters.put("sendorInfo", sendorInfo);
                mailParameters.put("addresseeName", creatorname);
                mailParameters.put("companyid", company.getCompanyID());
                mailParameters.put("baseUrl", baseUrl);
                mailParameters.put("approvalstatuslevel", approvedLevel);
                mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
                if (emails.length > 0) {
                    accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
                }
            }
        }
    }
    
    public ModelAndView approvePurchaseOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String currentUser = sessionHandlerImpl.getUserid(request);
            String remark = request.getParameter("remark");
            String doID = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            double totalOrderAmount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount"))? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);
            KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), doID);
            PurchaseOrder cqObj = (PurchaseOrder) CQObj.getEntityList().get(0);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            
            HashMap<String, Object> poApproveMap = new HashMap<String, Object>();            
            int level = cqObj.getApprovestatuslevel();
            String currencyid=cqObj.getCurrency()!=null?cqObj.getCurrency().getCurrencyID():sessionHandlerImpl.getCurrencyID(request);
            // Add Product and discounts mapping
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            JSONArray productDiscountJArr=new JSONArray();
            Set<PurchaseOrderDetail> purchaseOrderDetail = cqObj.getRows();
            for (PurchaseOrderDetail poDetail : purchaseOrderDetail) {
                String productId = poDetail.getProduct().getID();
                double discountVal = poDetail.getDiscount();
                int isDiscountPercent = poDetail.getDiscountispercent();
                if(isDiscountPercent==1){
                    discountVal = (poDetail.getQuantity()*poDetail.getRate())*(discountVal/100);
                }
                KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, cqObj.getOrderDate(), cqObj.getExternalCurrencyRate());
                double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                discAmountinBase = authHandler.round(discAmountinBase, companyid);
                JSONObject productDiscountObj=new JSONObject();
                productDiscountObj.put("productId", productId);
                productDiscountObj.put("discountAmount", discAmountinBase);
                productDiscountJArr.put(productDiscountObj);
            }
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            poApproveMap.put("companyid", companyid);
            poApproveMap.put("level", level);
            poApproveMap.put("totalAmount", String.valueOf(totalOrderAmount));
            poApproveMap.put("currentUser", currentUser);
            poApproveMap.put("fromCreate", false);
            poApproveMap.put("productDiscountMapList", productDiscountJArr);
            poApproveMap.put("moduleid", isFixedAsset ? Constants.Acc_FixedAssets_Purchase_Order_ModuleId : Constants.Acc_Purchase_Order_ModuleId);
            poApproveMap.put(Constants.PAGE_URL, baseUrl);
            
            List approvedLevelList = accPurchaseOrderModuleServiceObj.approvePurchaseOrder(cqObj, poApproveMap, true);
            int approvedLevel = (Integer) approvedLevelList.get(0);
            
            if (approvedLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences

                HashMap emailMap = new HashMap();
                String userName = sessionHandlerImpl.getUserFullName(request);
                emailMap.put("userName", userName);
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                emailMap.put("company", company);
                emailMap.put("purchaseOrder", cqObj);
                emailMap.put("baseUrl", baseUrl);
                emailMap.put("preferences", preferences);
                emailMap.put("isFixedAsset", isFixedAsset);
                emailMap.put("ApproveMap", poApproveMap);
                
               accPurchaseOrderModuleServiceObj.sendApprovalMailIfAllowedFromSystemPreferences(emailMap);
               
            }
          

            // Save Approval History
            if (approvedLevel != Constants.NoAuthorityToApprove) {
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("transtype", Constants.PURCHASE_ORDER_APPROVAL);
            hashMap.put("transid", cqObj.getID());
            hashMap.put("approvallevel", cqObj.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
            hashMap.put("remark", remark);
            hashMap.put("userid", sessionHandlerImpl.getUserid(request));
            hashMap.put("companyid", companyid);
            accountingHandlerDAOobj.updateApprovalHistory(hashMap);
            accPurchaseOrderobj.setApproverForPurchaseOrder(cqObj.getID(), companyid,sessionHandlerImpl.getUserid(request));//to save current approver for the transaction approval level
            if(isFixedAsset)
                auditTrailObj.insertAuditLog(AuditAction.PURCHASE_ORDER, "User " + sessionHandlerImpl.getUserFullName(request) + " has Approved a Asset Purchase Order " + cqObj.getPurchaseOrderNumber()+" at Level-"+cqObj.getApprovestatuslevel(), request, cqObj.getID());
            else
                auditTrailObj.insertAuditLog(AuditAction.PURCHASE_ORDER, "User " + sessionHandlerImpl.getUserFullName(request) + " has Approved a Purchase Order " + cqObj.getPurchaseOrderNumber()+" at Level-"+cqObj.getApprovestatuslevel(), request, cqObj.getID());
            txnManager.commit(status);
            issuccess = true;
            KwlReturnObject kmsg = null;
            String roleName="Company User";
            kmsg = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
            Iterator ite2 = kmsg.getEntityList().iterator();
            while (ite2.hasNext()) {
                Object[] row = (Object[]) ite2.next();
                roleName = row[1].toString();
            }
            if(isFixedAsset)
                msg = messageSource.getMessage("acc.field.AssetPurchaseOrderhasbeenapprovedsuccessfully", null, RequestContextUtils.getLocale(request))+" by "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" at Level "+cqObj.getApprovestatuslevel()+".";
            else
                msg = messageSource.getMessage("acc.field.PurchaseOrderhasbeenapprovedsuccessfully", null, RequestContextUtils.getLocale(request))+" by "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" at Level "+cqObj.getApprovestatuslevel()+".";
            } else {
                txnManager.commit(status);
                issuccess = true;
                msg = messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, RequestContextUtils.getLocale(request)) + cqObj.getApprovestatuslevel()+".";
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView saveQuotation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String channelName = "";
        try {
            /*Get request parameters */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            paramJobj.put("baseUrl", baseUrl);
            String userName = sessionHandlerImpl.getUserFullName(request);
            paramJobj.put(Constants.username,userName);
            /*Call to Save Vendor Quotation Details*/
            jobj = accPurchaseOrderModuleServiceObj.saveVendorQuotationJSON(paramJobj);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
            channelName = jobj.optString(Constants.channelName, null);
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Description : Update line level records 
     * @param <quoteDetails> used to get Quotation details
     * @param <moduleid> used to get Module Id
     * @param <companyid> used to get Company Id
     * @return :Hash Set
     */
    private HashSet<VendorQuotationDetail> updateQuotationRows(String quoteDetails, int moduleid, String companyid) throws ServiceException, JSONException {
        HashSet<VendorQuotationDetail> rows = new HashSet<>();
        try {
            JSONArray jArr = new JSONArray(quoteDetails);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                VendorQuotationDetail row = null;
                if (jobj.has("rowid")) {
                    KwlReturnObject quoteDetailsResult = accountingHandlerDAOobj.getObject(VendorQuotationDetail.class.getName(), jobj.getString("rowid"));
                    row = (VendorQuotationDetail) quoteDetailsResult.getEntityList().get(0);
                }

                /*
                 * To change the sequence of product
                 */
                if (row != null) {
                    if (jobj.has("srno")) {
                        row.setSrno(jobj.getInt("srno"));
                    }

                    /*
                     * We can update the descritpion of line item.
                     */
                    if (!StringUtil.isNullOrEmpty(jobj.optString("desc"))) {
                        try {
                            row.setDescription(StringUtil.DecodeText(jobj.optString("desc")));
                        } catch (Exception ex) {
                            row.setDescription(jobj.optString("desc"));
                        }
                    }

                    /*
                     * To update the custom field data of line items.
                     */

                    String customfield = jobj.getString("customfield");
                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        HashMap<String, Object> DOMap = new HashMap<String, Object>();
                        JSONArray jcustomarray = new JSONArray(customfield);

                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "VendorQuotationDetail");
                        customrequestParams.put("moduleprimarykey", "VendorQuotationDetailId");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put("moduleid", moduleid);
                        customrequestParams.put("companyid", companyid);
                        DOMap.put("id", row.getID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_VendorQuotationDetails_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            DOMap.put("vqdetailscustomdataref", row.getID());
                            accPurchaseOrderobj.updateVQuotationDetailsCustomData(DOMap);
                        }
                    }

                    /*
                     * Add Custom fields details for Product
                     */
                    if (!StringUtil.isNullOrEmpty(jobj.optString("productcustomfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("productcustomfield", "[]"));
                        HashMap<String, Object> quotationMap = new HashMap<String, Object>();
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "VqProductCustomData");
                        customrequestParams.put("moduleprimarykey", "VqDetailID");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        quotationMap.put("id", row.getID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_VQDetail_Productcustom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            quotationMap.put("vqdetailscustomdataref", row.getID());
                            accPurchaseOrderobj.updateVQuotationDetailsProductCustomData(quotationMap);
                        }
                    }
                    rows.add(row);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updateQuotationRows : " + ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
        return rows;
    }
    
    /**
     * Description : Update vendor quotation when You are link in other module.  
     * @param <request> used to get default company setup parameters
     * @param <response> used to send response
     * @return :JSONObject
     */
    public ModelAndView updateLinkedQuotation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isAccountingExe = false;
        String msg = "", channelName = "", QuotaionId = "", deliveryOid = "", billNo = "", doinvflag = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        status = txnManager.getTransaction(def);    //Please asked about this to sagar sir
        try {
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean isConsignment = request.getParameter("isConsignment") != null ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
            boolean iscash = StringUtil.isNullOrEmpty(request.getParameter("incash")) ? false : Boolean.parseBoolean(request.getParameter("incash"));
            List li = updateQuotation(request);
            String[] id = (String[]) li.get(0);
            String billno = (String) li.get(3);
            QuotaionId = (String) id[0];

            jobj.put("invoiceid", li.get(0));
            jobj.put("accountid", li.get(1));
            jobj.put("accountName", li.get(2));
            jobj.put("invoiceNo", li.get(3));
            jobj.put("address", li.get(4));
            jobj.put("amount", li.get(5));
            jobj.put("fullShippingAddress", li.get(6));

            txnManager.commit(status);
            issuccess = true;
            /*
             * To refresh a Invoice List.
             */
            if (!isFixedAsset) {
                channelName = "/PurchaseQuotationReport/gridAutoRefresh";;
            }
            /*
             * Composing the message to display after save operation.
             */
            if (isFixedAsset) {
                msg = messageSource.getMessage("acc.fixedassetCustomerQuotation.update", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";
            } else {
                msg = messageSource.getMessage("acc.VendorQuotation.update", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";
            }
            /*
             * Composing the message to insert into Audit Trail.
             */
            String action = "updated";
            if (isLeaseFixedAsset) {
                action += " Asset";
            }
            auditTrailObj.insertAuditLog(AuditAction.Vendor_Quotation, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " Vendor Quotation " + billno, request, QuotaionId);

        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            isAccountingExe = true;
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("invid", QuotaionId);
                jobj.put("doid", deliveryOid);
                jobj.put("dono", billNo);
                jobj.put("doinvflag", doinvflag);

                if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                    jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
                    ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
                }
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Description : Update vendor quotation when You are link in other module.  
     * @param <request> used to get default company setup parameters
     * @return :List
     */
    public List updateQuotation(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {
        String id = null;
        List ll = new ArrayList();
        ArrayList discountArr = new ArrayList();
        String soid = null;
        VendorQuotation quote = null;
        VendorQuotation quotation = null;
        try {
            DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            soid = request.getParameter("invoiceid");
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
            int moduleid = isFixedAsset ? Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId : Constants.Acc_Vendor_Quotation_ModuleId;
            String customfield = request.getParameter("customfield");
            /*
             * To update the following items which is not affecting the amount
             * and linking of the Invoice.
             */
            HashMap<String, Object> qDataMap = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(soid)) {
                qDataMap.put("id", soid);
            }
            qDataMap.put("orderdate", df.parse(request.getParameter("billdate")));
            qDataMap.put("memo", request.getParameter("memo") == null ? "" : request.getParameter("memo"));
            if (request.getParameter("shipdate") != null && !StringUtil.isNullOrEmpty(request.getParameter("shipdate"))) {
                qDataMap.put("shipdate", df.parse(request.getParameter("shipdate")));
            }
            qDataMap.put("companyid", companyid);
            qDataMap.put("shipvia", request.getParameter("shipvia"));
            qDataMap.put("fob", request.getParameter("fob") == null ? "" : request.getParameter("fob"));
            qDataMap.put("agent", request.getParameter("agent"));
            qDataMap.put("modifiedby", sessionHandlerImpl.getUserid(request));
            qDataMap.put("updatedon", System.currentTimeMillis());
            if (!StringUtil.isNullOrEmpty(request.getParameter("validdate"))) {
                qDataMap.put("validdate", df.parse(request.getParameter("validdate")));
            } else {
                qDataMap.put("validdate", null);
            }

            KwlReturnObject invResult = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), soid);
            quote = (VendorQuotation) invResult.getEntityList().get(0);

            Map<String, Object> addressParams = new HashMap<>();
            String billingAddress = request.getParameter(Constants.BILLING_ADDRESS);
            if (!StringUtil.isNullOrEmpty(billingAddress)) {
                addressParams = AccountingAddressManager.getAddressParams(request, false);
            } else {
                addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(quote.getVendor().getID(), companyid, accountingHandlerDAOobj);// addressParams = getCustomerDefaultAddressParams(customer,companyid);
            }
            BillingShippingAddresses bsa = quote.getBillingShippingAddresses();//used to update billing shipping addresses
            addressParams.put("id", bsa != null ? bsa.getID() : "");
            KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
            bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
            String addressid = bsa.getID();
            qDataMap.put("billshipAddressid", addressid);
            /*
             * Updating line item information.
             */
            String qDetails = request.getParameter("detail");
            HashSet<VendorQuotationDetail> quoteDetails = updateQuotationRows(qDetails, moduleid, companyid);
            qDataMap.put("sodetails", quoteDetails);
            /*
             * Updating Custom field data.
             */
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_VendorQuotation_modulename);
                customrequestParams.put("moduleprimarykey", "VendorQuotationId");
                customrequestParams.put("modulerecid", quote.getID());
                customrequestParams.put("moduleid", !isFixedAsset ? Constants.Acc_Vendor_Quotation_ModuleId : Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_VendorQuotation_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    qDataMap.put("accvendorquotationcustomdataref", quote.getID());
                    KwlReturnObject accresult = accPurchaseOrderobj.updateVendorQuotationCustomData(qDataMap);
                }
            }
            KwlReturnObject result = accPurchaseOrderobj.saveVendorQuotation(qDataMap);
            quotation = (VendorQuotation) result.getEntityList().get(0);
            id = quotation.getID();
            /*
             * Data for return information.
             */
            String personalid = quotation.getVendor().getAccount().getID();
            String accname = quotation.getVendor().getAccount().getName();
            String quotationNo = quotation.getQuotationNumber();
            String address = quotation.getVendor().getAddress();
            String fullShippingAddress = "";
            if (quotation.getBillingShippingAddresses() != null) {
                fullShippingAddress = quotation.getBillingShippingAddresses().getFullShippingAddress();
            }
            ll.add(new String[]{id, ""});
            ll.add(personalid);
            ll.add(accname);
            ll.add(quotationNo);
            ll.add(address);
            ll.add(quotation.getQuotationamount());
            ll.add(fullShippingAddress);
        } catch (ParseException | JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
        return ll;
    }
    public List<String> approveVendorQuotation(VendorQuotation doObj, HashMap<String, Object> qApproveMap, boolean isMailApplicable) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException, JSONException {
        boolean hasAuthority = false;
        String companyid = "";
        List returnList = new ArrayList();
        List mailParamList = new ArrayList();
        int returnStatus;
        if (qApproveMap.containsKey("companyid") && qApproveMap.get("companyid") != null) {
            companyid = qApproveMap.get("companyid").toString();
        }
        String currentUser = "";
        if (qApproveMap.containsKey("currentUser") && qApproveMap.get("currentUser") != null) {
            currentUser = qApproveMap.get("currentUser").toString();
        }
        int level = 0;
        if (qApproveMap.containsKey("level") && qApproveMap.get("level") != null) {
            level = Integer.parseInt(qApproveMap.get("level").toString());
        }
        String amount = "";
        if (qApproveMap.containsKey("totalAmount") && qApproveMap.get("totalAmount") != null) {
            amount = qApproveMap.get("totalAmount").toString();
        }
        boolean fromCreate = false;
        if (qApproveMap.containsKey("fromCreate") && qApproveMap.get("fromCreate") != null) {
            fromCreate = Boolean.parseBoolean(qApproveMap.get("fromCreate").toString());
        }
        boolean isFixedAsset = false;
        if (qApproveMap.containsKey("isFixedAsset") && qApproveMap.get("isFixedAsset") != null) {
            isFixedAsset = Boolean.parseBoolean(qApproveMap.get("isFixedAsset").toString());
        }
        JSONArray productDiscountMapList = null;
        if (qApproveMap.containsKey("productDiscountMapList") && qApproveMap.get("productDiscountMapList") != null) {
            productDiscountMapList = new JSONArray(qApproveMap.get("productDiscountMapList").toString());
        }
        boolean isEdit = false;
        if (qApproveMap.containsKey("isEdit") && qApproveMap.get("isEdit") != null) {
            isEdit = Boolean.parseBoolean(qApproveMap.get("isEdit").toString());
        }
        if (!fromCreate) {
            String thisUser = currentUser;
            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
            User user = (User) userclass.getEntityList().get(0);

            if (AccountingManager.isCompanyAdmin(user)) {
                hasAuthority = true;
            } else {
                if (isFixedAsset) {
                    hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRule(level, companyid, amount, thisUser, Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
                } else {
                    hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRule(level, companyid, amount, thisUser, Constants.Acc_Vendor_Quotation_ModuleId);
                }
            }
        } else {
            hasAuthority = true;
        }
        if (hasAuthority) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            int approvalStatus = 11;
            String prNumber = doObj.getQuotationNumber();
            String vqID = doObj.getID();
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put("companyid", companyid);
            qdDataMap.put("level", level + 1);

            if (isFixedAsset) {
                qdDataMap.put("moduleid", Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
            } else {
                qdDataMap.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
            }

            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            String fromName = "User";
            fromName = doObj.getCreatedby().getFirstName().concat(" ").concat(doObj.getCreatedby().getLastName());
            /**
             * parameters required for sending mail
             */
            Map<String, Object> mailParameters = new HashMap();
            mailParameters.put(Constants.companyid, companyid);
            mailParameters.put(Constants.prNumber, prNumber);
            mailParameters.put(Constants.fromName, fromName);
            mailParameters.put(Constants.isCash, false);
            mailParameters.put(Constants.isEdit, isEdit);
            mailParameters.put(Constants.createdBy, doObj.getCreatedby().getUserID());
            if (qApproveMap.containsKey(Constants.PAGE_URL)) {
                mailParameters.put(Constants.PAGE_URL, (String) qApproveMap.get(Constants.PAGE_URL));
            }
            Iterator itr = flowresult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                mailParameters.put(Constants.ruleid, row[0].toString());
                //            JSONObject obj = new JSONObject();
                String rule = "";
                HashMap<String, Object> recMap = new HashMap();
                if (row[2] != null) {
                    rule = row[2].toString();
                }
                String discountRule = "";
                if (row[7] != null) {
                    discountRule = row[7].toString();
                }

                boolean sendForApproval = false;
                int appliedUpon = Integer.parseInt(row[5].toString());
                if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount) {
                    if (productDiscountMapList != null) {
                        sendForApproval = AccountingManager.checkForProductAndProductDiscountRule(productDiscountMapList, appliedUpon, rule, discountRule);
                    }
                }else if(appliedUpon ==Constants.Specific_Products_Category){
                    /*
                     * Check If Rule is apply on product
                     * category from multiapproverule window
                     */
                    sendForApproval = accountingHandlerDAOobj.checkForProductCategoryForProduct(productDiscountMapList, appliedUpon, rule);
                } else {
                    rule = rule.replaceAll("[$$]+", amount);
                }
                if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && appliedUpon != Constants.Specific_Products && appliedUpon != Constants.Specific_Products_Discount &&appliedUpon !=Constants.Specific_Products_Category && Boolean.parseBoolean(engine.eval(rule).toString())) || sendForApproval) {
                    // send emails
                    boolean hasApprover = Boolean.parseBoolean(row[3].toString());
                    mailParameters.put(Constants.hasApprover, hasApprover);
                    mailParameters.put("level", doObj.getApprovestatuslevel());
                    if (isFixedAsset) {
                        if (isMailApplicable) {
                            mailParameters.put(Constants.moduleid, Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
                            accPurchaseOrderModuleServiceObj.sendMailToApprover(mailParameters);

                        } else {
                            
                          /* This block will be executed 
                         * if any Asset VQ will go for pending approval*/
                            
                            recMap.put("ruleid", row[0].toString());
                            recMap.put("fromName", fromName);
                            recMap.put("hasApprover", hasApprover);
                            
                            mailParamList.add(recMap);
                        }
                        approvalStatus = level + 1;
                    } else {
                        if (isMailApplicable) {
                            mailParameters.put(Constants.moduleid, Constants.Acc_Vendor_Quotation_ModuleId);
                            accPurchaseOrderModuleServiceObj.sendMailToApprover(mailParameters);
                        } else {
                            
                        /* This block will be executed 
                         * if any VQ will go for pending approval*/
                            
                            recMap.put("ruleid", row[0].toString());
                            recMap.put("fromName", fromName);
                            recMap.put("hasApprover", hasApprover);

                            mailParamList.add(recMap);
                        }
                        approvalStatus = level + 1;
                    }

                }
            }
            accPurchaseOrderobj.approvePendingVendorQuotation(vqID, companyid, approvalStatus);
            returnStatus = approvalStatus;
        } else {
            returnStatus = Constants.NoAuthorityToApprove; //if not have approval permission then return one fix value like 999
        }
        returnList.add(returnStatus);
        returnList.add(mailParamList);

        return returnList;

    }
    public ModelAndView approveVendorQuotation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String remark = request.getParameter("remark");
            String doID = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currentUser = sessionHandlerImpl.getUserid(request);
            double totalOrderAmount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount")) ? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
//            String companyCurrency = sessionHandlerImpl.getCurrencyID(request);
//            HashMap<String, Object> requestParams = new HashMap();
//            requestParams.put("companyid", companyid);
//            requestParams.put("ID", prID);
//            requestParams.put("archieve", 0);
            KwlReturnObject VQObj = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), doID);
            VendorQuotation vqObj = (VendorQuotation) VQObj.getEntityList().get(0);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

//            Iterator itrRow = requisition.getRows().iterator();
//            Set<VendorQuotationDetail> vqRows = vqObj.getRows();
//            double totalAmount = 0;
//            double quantity = 0;
//            if (vqRows != null && !vqRows.isEmpty()) {
//                for (VendorQuotationDetail cnt : vqRows) {
//                    quantity = cnt.getQuantity();
//                    totalAmount += cnt.getRate() * quantity;
//                }
//            }
//            amount = getAmountBasetoCurrency(requisition.getRows(), companyid, companyCurrency, requisition.getCurrency().getCurrencyID(), requisition.getRequisitionDate());
//            while(itrRow.hasNext()) {
//                PurchaseRequisitionDetail ItemDetail = (PurchaseRequisitionDetail) itrRow.next();
//                amount +=(ItemDetail.getQuantity() * ItemDetail.getRate());
//            }
            int level = vqObj.getApprovestatuslevel();
            String currencyid=vqObj.getCurrency()!=null?vqObj.getCurrency().getCurrencyID():sessionHandlerImpl.getCurrencyID(request);
            // Add Product and discounts mapping
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            HashMap<String, Object> qApproveMap = new HashMap<String, Object>();
            JSONArray productDiscountJArr=new JSONArray();
            Set<VendorQuotationDetail> purchaseOrderDetail = vqObj.getRows();
            for (VendorQuotationDetail poDetail : purchaseOrderDetail) {
                String productId = poDetail.getProduct().getID();
                double discountVal = poDetail.getDiscount();
                int isDiscountPercent = poDetail.getDiscountispercent();
                if(isDiscountPercent==1){
                    discountVal = (poDetail.getQuantity()*poDetail.getRate())*(discountVal/100);
                }
                KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, vqObj.getQuotationDate(), vqObj.getExternalCurrencyRate());
                double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                discAmountinBase = authHandler.round(discAmountinBase, companyid);
                JSONObject productDiscountObj=new JSONObject();
                productDiscountObj.put("productId", productId);
                productDiscountObj.put("discountAmount", discAmountinBase);
                productDiscountJArr.put(productDiscountObj);
            }
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            qApproveMap.put("companyid", companyid);
            qApproveMap.put("level", level);
            qApproveMap.put("totalAmount", String.valueOf(totalOrderAmount));
            qApproveMap.put("currentUser", currentUser);
            qApproveMap.put("fromCreate", false);
            qApproveMap.put("isFixedAsset", isFixedAsset);
            qApproveMap.put("productDiscountMapList", productDiscountJArr);
            qApproveMap.put(Constants.PAGE_URL, baseUrl);
            if(isFixedAsset){
                qApproveMap.put("moduleid", Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
            }else{
                qApproveMap.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
            }
            List approvedLevelList = approveVendorQuotation(vqObj, qApproveMap, true);
            int approvedLevel = (Integer) approvedLevelList.get(0);
            if (approvedLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                String userName = sessionHandlerImpl.getUserFullName(request);
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                String approvalpendingStatusmsg = "";
                String sendorInfo = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                String creatormail = company.getCreator().getEmailID();
                String documentcreatoremail = (vqObj != null && vqObj.getCreatedby() != null) ? vqObj.getCreatedby().getEmailID() : "";
                String fname = company.getCreator().getFirstName() == null ? "" : company.getCreator().getFirstName();
                String lname = company.getCreator().getLastName() == null ? "" : company.getCreator().getLastName();
                String creatorname = fname + " " + lname;
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                ArrayList<String> emailArray = new ArrayList<>();
                qdDataMap.put(Constants.companyKey, companyid);
                qdDataMap.put("level", level);
                if (isFixedAsset) {
                    qdDataMap.put(Constants.moduleid, Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
                } else {
                    qdDataMap.put(Constants.moduleid, Constants.Acc_Vendor_Quotation_ModuleId);
                }
//                emailArray =commonFnControllerService.getUserApprovalEmail(qdDataMap);
                emailArray.add(creatormail);
                if (!StringUtil.isNullOrEmpty(documentcreatoremail) && !creatormail.equalsIgnoreCase(documentcreatoremail)) {
                    emailArray.add(documentcreatoremail);
                }
                String[] emails = {};
                emails = emailArray.toArray(emails);
//                String[] emails = {creatormail};
                if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                    String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                    emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                }
                if (vqObj.getApprovestatuslevel() < 11) {
                    qdDataMap.put("ApproveMap", qApproveMap);
                approvalpendingStatusmsg=commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put("Number", vqObj.getQuotationNumber());
                mailParameters.put("userName", userName);
                mailParameters.put("emails", emails);
                mailParameters.put("sendorInfo", sendorInfo);
                mailParameters.put("addresseeName", "All");
                mailParameters.put("companyid", company.getCompanyID());
                mailParameters.put("baseUrl", baseUrl);
                mailParameters.put("approvalstatuslevel", vqObj.getApprovestatuslevel());
                mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
                if (emails.length > 0) {
                    if (isFixedAsset) {
                        mailParameters.put("moduleName", Constants.ASSET_VENDOR_QUOTATION);
                        accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
                    } else {
                        mailParameters.put("moduleName", Constants.VENDOR_QUOTATION);
                        accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
                    }
                }
            }

            // Save Approval History
            if (approvedLevel != Constants.NoAuthorityToApprove) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("transtype", Constants.VENDOR_QUOTATION_APPROVAL);
                hashMap.put("transid", vqObj.getID());
                hashMap.put("approvallevel", vqObj.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
                hashMap.put("remark", remark);
                hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                hashMap.put("companyid", companyid);
                accountingHandlerDAOobj.updateApprovalHistory(hashMap);

                String moduleName = Constants.VENDOR_QUOTATION;
                if (isFixedAsset) {
                    moduleName = Constants.ASSET_VENDOR_QUOTATION;
                }
                // Audit log entry
                auditTrailObj.insertAuditLog(AuditAction.DELIVERY_ORDER, "User " + sessionHandlerImpl.getUserFullName(request) + " has Approved a "+moduleName+" " + vqObj.getQuotationNumber()+" at Level-"+vqObj.getApprovestatuslevel(), request, vqObj.getID());
                txnManager.commit(status);
                issuccess = true;
                KwlReturnObject kmsg = null;
                String roleName="Company User";
                kmsg = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
                Iterator ite2 = kmsg.getEntityList().iterator();
                while (ite2.hasNext()) {
                    Object[] row = (Object[]) ite2.next();
                    roleName = row[1].toString();
                }
                
                if (isFixedAsset) {
                    msg = messageSource.getMessage("acc.field.assetVendorQuotationhasbeenapprovedsuccessfully", null, RequestContextUtils.getLocale(request))+" by "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" at Level "+vqObj.getApprovestatuslevel()+".";
                } else {
                    msg = messageSource.getMessage("acc.field.VendorQuotationhasbeenapprovedsuccessfully", null, RequestContextUtils.getLocale(request))+" by "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" at Level "+vqObj.getApprovestatuslevel()+".";
                }
                
            } else {
                txnManager.commit(status);
                issuccess = true;
                msg = messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, RequestContextUtils.getLocale(request))+ vqObj.getApprovestatuslevel()+".";
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView rejectPendingPurchaseOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String roleName="";
            int level=0;
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            KwlReturnObject userRoleResult = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
            Iterator itr = userRoleResult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                roleName = row[1].toString();
            }
            
            String soID="";
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONObject jObj= jArr.getJSONObject(0);
            soID= StringUtil.DecodeText(jObj.optString("billid"));
            KwlReturnObject SOObj = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), soID);
            PurchaseOrder soObj = (PurchaseOrder) SOObj.getEntityList().get(0);
            level=soObj.getApprovestatuslevel();
            
            boolean isRejected=rejectPendingPurchaseOrder(request);
            txnManager.commit(status);
            issuccess = true;
            
            if(isRejected){
                msg = messageSource.getMessage("acc.field.PurchaseOrderhasbeenrejectedsuccessfully", null, RequestContextUtils.getLocale(request))+" by "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" at Level "+level+".";
            } else {
                msg = messageSource.getMessage("acc.vq.notAuthorisedToRejectThisRecord", null, RequestContextUtils.getLocale(request))+" at Level "+level+".";
            }
            
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public boolean rejectPendingPurchaseOrder(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException, ScriptException {
        boolean isRejected=false;
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currentUser = sessionHandlerImpl.getUserid(request);
            String remark = request.getParameter("remark");
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), currentUser);
            User user = (User) userResult.getEntityList().get(0);
            String actionId = "66", actionMsg = "rejected";
            int level=0;
            String amount="";
            for (int i = 0; i < jArr.length(); i++) {
                 boolean hasAuthorityToReject=false;
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String soid = StringUtil.DecodeText(jobj.optString("billid"));
                    boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
                    boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), soid);
                    PurchaseOrder soObj = (PurchaseOrder) cap.getEntityList().get(0);
                    double totalAmount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount")) ? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);
                    HashMap<String, Object> soApproveMap = new HashMap<String, Object>();            
                    level = soObj.getApprovestatuslevel();
                    soApproveMap.put("companyid", companyid);
                    soApproveMap.put("level", level);
                    soApproveMap.put("totalAmount", String.valueOf(totalAmount));
                    soApproveMap.put("currentUser", currentUser);
                    soApproveMap.put("fromCreate", false);
                    soApproveMap.put("moduleid", isFixedAsset?Constants.Acc_FixedAssets_Purchase_Order_ModuleId:Constants.Acc_Purchase_Order_ModuleId);
                    amount=String.valueOf(totalAmount);
                    if (AccountingManager.isCompanyAdmin(user)) {
                        hasAuthorityToReject = true;
                    } else {
                        hasAuthorityToReject = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(soApproveMap);
                    }
                    if(hasAuthorityToReject){ 
                        accPurchaseOrderobj.rejectPendingPurchaseOrder(soObj.getID(), companyid);
                        isRejected=true;
                        // Maintain Approval History of Rejected Record
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("transtype", Constants.PURCHASE_ORDER_APPROVAL);
                        hashMap.put("transid", soObj.getID());
                        hashMap.put("approvallevel", Math.abs(soObj.getApprovestatuslevel()));//  If approvedLevel = 11 then its final Approval
                        hashMap.put("remark", remark);
                        hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                        hashMap.put("companyid", companyid);
                        hashMap.put("isrejected", true);
                        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                        auditTrailObj.insertAuditLog(actionId, "User " + sessionHandlerImpl.getUserFullName(request) + " " + actionMsg + " Purchase Order " + soObj.getPurchaseOrderNumber(), request, soObj.getID());
                    }    
                 }
            }
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }
        return isRejected;
    }
    
    public ModelAndView rejectPendingVendorQuotation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String roleName="";
            int level=0;
            KwlReturnObject userRoleResult = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
            Iterator itr = userRoleResult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                roleName = row[1].toString();
            }
            
            String vqID="";
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONObject jObj= jArr.getJSONObject(0);
            vqID= StringUtil.DecodeText(jObj.optString("billid"));
            KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), vqID);
            VendorQuotation cqObj = (VendorQuotation) CQObj.getEntityList().get(0);
            level=cqObj.getApprovestatuslevel();
            
            boolean isRejected=rejectPendingVendorQuotation(request);
            txnManager.commit(status);
            issuccess = true;
            
            if (isRejected) {
                msg = messageSource.getMessage("acc.field.VendorQuotationhasbeenrejectedsuccessfully", null, RequestContextUtils.getLocale(request))+" by "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" at Level "+level+".";
            } else {
                msg = messageSource.getMessage("acc.vq.notAuthorisedToRejectThisRecord", null, RequestContextUtils.getLocale(request))+" "+level+".";
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public boolean rejectPendingVendorQuotation(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException, ScriptException {
        boolean isRejected=false;
        try {            
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currentUser = sessionHandlerImpl.getUserid(request);
            String remark = request.getParameter("remark");
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), currentUser);
            User user = (User) userResult.getEntityList().get(0);
            String actionId = "66", actionMsg = "rejected";
            int level=0;
            String amount="";
            for (int i = 0; i < jArr.length(); i++) {
                boolean hasAuthorityToReject=false;
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String vqid = StringUtil.DecodeText(jobj.optString("billid"));
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), vqid);
                    VendorQuotation vqObj = (VendorQuotation) cap.getEntityList().get(0);
                    double totalAmount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount")) ? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);
                    amount=String.valueOf(totalAmount);
                    level= vqObj.getApprovestatuslevel();
                    if (AccountingManager.isCompanyAdmin(user)) {
                        hasAuthorityToReject = true;
                    } else {
                        hasAuthorityToReject = accountingHandlerDAOobj.checkForMultiLevelApprovalRule(level, companyid, amount, currentUser, Constants.Acc_Vendor_Quotation_ModuleId);
                    }
                    if(hasAuthorityToReject){
                        accPurchaseOrderobj.rejectPendingVendorQuotation(vqObj.getID(), companyid);
                        isRejected=true;
                        // Maintain Approval History of Rejected Record
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("transtype", Constants.VENDOR_QUOTATION_APPROVAL);
                        hashMap.put("transid", vqObj.getID());
                        hashMap.put("approvallevel", Math.abs(vqObj.getApprovestatuslevel()));//  If approvedLevel = 11 then its final Approval
                        hashMap.put("remark", remark);
                        hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                        hashMap.put("companyid", companyid);
                        hashMap.put("isrejected", true);
                        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                        auditTrailObj.insertAuditLog(actionId, "User " + sessionHandlerImpl.getUserFullName(request) + " " + actionMsg + " Vendor Quotation " + vqObj.getQuotationNumber(), request, vqObj.getID());
                    }
                }
            }            
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }
        return isRejected;
    }
    
    public ModelAndView deleteQuotations(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            boolean isFixedAsset = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isFixedAsset))) {
                isFixedAsset = Boolean.parseBoolean(request.getParameter(Constants.isFixedAsset));
            }
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            requestJobj.put("servletContext", this.getServletContext());
            String linkedTransaction = deleteQuotations(requestJobj);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                if (isFixedAsset) {
                    msg = messageSource.getMessage("acc.field.assetVendorQuotationshasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                } else {
                    msg = messageSource.getMessage("acc.field.VendorQuotationshasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                }
            } else {
                if (isFixedAsset) {
                    msg = messageSource.getMessage("acc.field.assetVendorQuotationsexcept", null, RequestContextUtils.getLocale(request)) + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                } else {
                    msg = messageSource.getMessage("acc.field.VendorQuotationsexcept", null, RequestContextUtils.getLocale(request)) + linkedTransaction.substring(0, linkedTransaction.length() - 2)  + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                }
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteQuotations(JSONObject requestJobj) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransaction = "";
        try {
            JSONArray jArr = new JSONArray(requestJobj.optString(Constants.RES_data));
            String companyid = requestJobj.optString(Constants.companyKey);
            boolean isFixedAsset = false;
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.isFixedAsset))) {
                isFixedAsset = Boolean.parseBoolean(requestJobj.optString(Constants.isFixedAsset));
            }
            String audtmsg = "";
            if (isFixedAsset) {
                audtmsg = " " + messageSource.getMessage("acc.up.10", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))) + " ";
            } else {
                audtmsg = " ";
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString(Constants.billid))) {
                    linkedTransaction = accPurchaseOrderModuleServiceObj.deleteQuotation(jobj, requestJobj, linkedTransaction, companyid, audtmsg);
                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
        }
        return linkedTransaction;
    }
    
    public ModelAndView deleteQuotationsPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            boolean isFixedAsset = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isFixedAsset))) {
                isFixedAsset = Boolean.parseBoolean(request.getParameter(Constants.isFixedAsset));
            }
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            requestJobj.put("servletContext", this.getServletContext());
            String linkedTransaction = deleteQuotationsPermanent(requestJobj);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                if (isFixedAsset) {
                    msg = messageSource.getMessage("acc.field.assetVendorQuotationshasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                } else {
                    msg = messageSource.getMessage("acc.field.VendorQuotationshasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                }
            } else {
                if (isFixedAsset) {
                    msg = messageSource.getMessage("acc.field.assetVendorQuotationsexcept", null, RequestContextUtils.getLocale(request)) + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                } else {
                    msg = messageSource.getMessage("acc.field.VendorQuotationsexcept", null, RequestContextUtils.getLocale(request)) + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                }
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteQuotationsPermanent(JSONObject requestJobj) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransaction = "";
        try {
            JSONArray jArr = new JSONArray(requestJobj.optString(Constants.RES_data));
            
            boolean isFixedAsset = false;
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.isFixedAsset))) {
                isFixedAsset = Boolean.parseBoolean(requestJobj.optString(Constants.isFixedAsset));
            }
            
            String companyid = requestJobj.optString(Constants.companyKey);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.billid))) {
                    linkedTransaction = accPurchaseOrderModuleServiceObj.deleteQuotationPermanent(jobj, requestJobj, linkedTransaction, companyid, isFixedAsset);
                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
        }
        return linkedTransaction;
    }
    
    public ModelAndView archieveQuotations(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            archieveQuotations(request);
            txnManager.commit(status);
            issuccess = true;
//            msg = messageSource.getMessage("acc.so.quotacr", null, RequestContextUtils.getLocale(request));//"Quotation has been archieved successfully.";
            if (isFixedAsset) {
                msg = messageSource.getMessage("acc.field.assetVendorQuotationhasbeenarchievedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.VendorQuotationhasbeenarchievedsuccessfully", null, RequestContextUtils.getLocale(request));
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void archieveQuotations(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {

        String companyid = sessionHandlerImpl.getCompanyid(request);
        String billid = request.getParameter("billid");
        String billno = request.getParameter("billno");
        String auditMsg=messageSource.getMessage("acc.vendorquotation.archive", null, RequestContextUtils.getLocale(request));
        boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
        if(isFixedAsset){
            auditMsg=messageSource.getMessage("acc.assetvendorquotation.archive", null, RequestContextUtils.getLocale(request));
        }
        accPurchaseOrderobj.archieveQuotation(billid, companyid);
        auditTrailObj.insertAuditLog(AuditAction.Vendor_Quotation, "User " + sessionHandlerImpl.getUserFullName(request) +" "+ auditMsg +" " + billno, request, billid);

    }

    public ModelAndView unArchieveQuotations(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            unArchieveQuotations(request);
            txnManager.commit(status);
            issuccess = true;
//            msg = messageSource.getMessage("acc.so.quotacr", null, RequestContextUtils.getLocale(request));//"Quotation has been archieved successfully.";
            if(isFixedAsset){
             msg = messageSource.getMessage("acc.field.assetVendorQuotationhasbeenunarchievedsuccessfully", null, RequestContextUtils.getLocale(request));
            }else{
             msg = messageSource.getMessage("acc.field.VendorQuotationhasbeenunarchievedsuccessfully", null, RequestContextUtils.getLocale(request));
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void unArchieveQuotations(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {

        String companyid = sessionHandlerImpl.getCompanyid(request);
        String billid = request.getParameter("billid");
        String billno = request.getParameter("billno");
        String auditMsg=messageSource.getMessage("acc.vendorquotation.unarchive", null, RequestContextUtils.getLocale(request));
        boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
        if(isFixedAsset){
            auditMsg=messageSource.getMessage("acc.assetvendorquotation.unarchive", null, RequestContextUtils.getLocale(request));
        }
        accPurchaseOrderobj.unArchieveQuotation(billid, companyid);
        auditTrailObj.insertAuditLog(AuditAction.Vendor_Quotation, "User " + sessionHandlerImpl.getUserFullName(request) +" "+ auditMsg +" " +  billno, request, billid);
    }

    public ModelAndView updateFavourite(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);

        try {
            KwlReturnObject result = null;
            HashMap<String, Object> poDataMap = new HashMap<String, Object>();
            boolean withInventory = Boolean.parseBoolean(request.getParameter("withInv"));
            boolean quotationFlag = Boolean.parseBoolean(request.getParameter("quotationFlag"));
            boolean requisitionflag=false;
            if(request.getParameter("requisitionflag")!=null){
                    requisitionflag = Boolean.parseBoolean(request.getParameter("requisitionflag"));
            }
            String poid = request.getParameter("invoiceid");
            KwlReturnObject poresult = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), poid); // (Tax)session.get(Tax.class, taxid);
            PurchaseOrder purchaseOrder = (PurchaseOrder) poresult.getEntityList().get(0);
            if (purchaseOrder != null) {
                poDataMap.put("isOpeningBalanceOrder", purchaseOrder.isIsOpeningBalancePO());
            }
            String companyid = sessionHandlerImpl.getCompanyid(request);
            poDataMap.put("companyid", companyid);
            poDataMap.put("orderdate", new Date(request.getParameter("date")));
            poDataMap.put("id", poid);
            poDataMap.put("isfavourite", request.getParameter("isfavourite"));

            if (!StringUtil.isNullOrEmpty(poid)) {
                if (withInventory) {
                    result = accPurchaseOrderobj.saveBillingPurchaseOrder(poDataMap);
                } else {
                    if (quotationFlag) {
                        result = accPurchaseOrderobj.saveVendorQuotation(poDataMap);
                    } else {
                        if(requisitionflag){
                        result = accPurchaseOrderobj.savePurchaseRequisition(poDataMap);
                        } else{
                        result = accPurchaseOrderobj.savePurchaseOrder(poDataMap);
                    }
                    }
                }
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updatePrint(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);

        try {
            KwlReturnObject result = null;
            HashMap<String, Object> poDataMap = new HashMap<String, Object>();
            boolean withInventory = Boolean.parseBoolean(request.getParameter("withInv"));
            boolean quotationFlag = Boolean.parseBoolean(request.getParameter("quotationFlag"));
            String recordids = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("recordids"))) {
                recordids = request.getParameter("recordids");
            }
            ArrayList<String> SOIDList = CustomDesignHandler.getSelectedBillIDs(recordids);
            for (int cnt = 0; cnt < SOIDList.size(); cnt++) {
                poDataMap.put("id", SOIDList.get(cnt));
                poDataMap.put("isprinted", request.getParameter("isprinted"));

                if (!StringUtil.isNullOrEmpty(SOIDList.get(cnt))) {
                    if (withInventory) {
                        result = accPurchaseOrderobj.saveBillingPurchaseOrder(poDataMap);
                    } else {
                        if (quotationFlag) {
                            result = accPurchaseOrderobj.saveVendorQuotation(poDataMap);
                        } else {
                            result = accPurchaseOrderobj.savePurchaseOrder(poDataMap);
                        }
                    }
                }
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (AccountingException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

     /**
     * Description : Update line level records
     * @param <requisition> used to get PurchaseRequisition object
     * @param <companyid> used to get Company Id
     * @return :Hash Set
     */
    public HashSet<PurchaseRequisitionDetail> updateRequisitionRows(HttpServletRequest request, PurchaseRequisition requisition, String companyid) throws ServiceException, AccountingException, UnsupportedEncodingException, SessionExpiredException {
        HashSet rows = new HashSet();
        try {
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;

            /**
             * @param <details> used to Get details of line level records
             */
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            for (int i = 0; i < jArr.length(); i++) {
                PurchaseRequisitionDetail row = null;
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                if (jobj.has("rowid")) {
                    KwlReturnObject quoteDetailsResult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), jobj.getString("rowid"));
                    row = (PurchaseRequisitionDetail) quoteDetailsResult.getEntityList().get(0);
                }
                if (jobj.has("srno")) {
                    row.setSrno(jobj.getInt("srno"));
                }
                if (jobj.has("desc")) {
                    row.setProductdescription(URLDecoder.decode(jobj.optString("desc"), StaticValues.ENCODING));
                }
                /*
                 * To Update Line Level Records
                 */
                String customfield = jobj.getString("customfield");
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    HashMap<String, Object> DOMap = new HashMap<String, Object>();
                    JSONArray jcustomarray = new JSONArray(customfield);

                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "PurchaseRequisitionDetail");
                    customrequestParams.put("moduleprimarykey", "PurchaseRequisitionDetailId");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put("moduleid", !isFixedAsset ? Constants.Acc_Purchase_Requisition_ModuleId : Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId);
                    customrequestParams.put("companyid", companyid);
                    DOMap.put("id", row.getID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_PurchaseRequisitionDetail_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        DOMap.put("accpurchaserequisitiondetailcustomdataref", row.getID());
                        accPurchaseOrderobj.updatePurchaseRequisitionDetailCustomData(DOMap);
                    }
                }

                rows.add(row);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveRequisitionRows : " + ex.getMessage(), ex);
        }
        return rows;
    }

    /**
     * Description : Update Purchase Requisition when You are link in other module.
     * @param <request> used to get default company setup parameters
     * @return :List
     */
    public List updateRequisition(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {
        List ll = new ArrayList();
        String id = "";
        String reuisitionId = "";
        PurchaseRequisition requisition = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            reuisitionId = request.getParameter("invoiceid");
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            int moduleid = isFixedAsset ? Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId : Constants.Acc_Purchase_Requisition_ModuleId;
            String customfield = request.getParameter("customfield");

            HashMap<String, Object> qDataMap = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(reuisitionId)) {
                qDataMap.put("id", reuisitionId);
            }
            qDataMap.put("memo", request.getParameter("memo") == null ? "" : request.getParameter("memo"));
            qDataMap.put("companyid", companyid);
            qDataMap.put("userid", sessionHandlerImpl.getUserid(request));
            qDataMap.put(Constants.Checklocktransactiondate, request.getParameter("billdate"));
            KwlReturnObject soresult = accPurchaseOrderobj.savePurchaseRequisition(qDataMap);
            requisition = (PurchaseRequisition) soresult.getEntityList().get(0);
            String requisitionNo = requisition.getPrNumber();

            qDataMap.put("id", requisition.getID());
            HashSet<PurchaseRequisitionDetail> sodetails = updateRequisitionRows(request, requisition, companyid);

            /*
             * Update global level custom fields of purchsase requisition
             */
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_Purchase_Requisition_modulename);
                customrequestParams.put("moduleprimarykey", "PurchaseRequisitionId");
                customrequestParams.put("modulerecid", requisition.getID());
                customrequestParams.put("moduleid", !isFixedAsset ? Constants.Acc_Purchase_Requisition_ModuleId : Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_PurchaseRequisition_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    qDataMap.put("accpurchaserequisitioncustomdataref", requisition.getID());
                    KwlReturnObject accresult = accPurchaseOrderobj.updatePurchaseRequisitionCustomData(qDataMap);
                }
            }
            id = requisition.getID();
            ll.add(new String[]{id, ""});
            ll.add(requisitionNo);
        } catch (Exception e) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
        return ll;
    }

    /**
     * Description : Update Purchase Requisition when You are link in other module.
     * @param <request> used to get default company setup parameters
     * @param <response> used to send response
     * @return :JSONObject
     */
    public ModelAndView updateRequisition(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "",requisitionId = "", billNo = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        status = txnManager.getTransaction(def);    //Please asked about this to sagar sir
        try {
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            List li = updateRequisition(request);
            String[] id = (String[]) li.get(0);
            requisitionId = (String) id[0];
            billNo = (String) li.get(1);
            issuccess = true;
            msg = messageSource.getMessage("acc.purchaseRequsition.update", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billNo + "</b>";
            /*
             * Composing the message to insert into Audit Trail.
             */
            String action = "updated";
            auditTrailObj.insertAuditLog(AuditAction.PURCHASE_REQUISITION_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " Purchase Requisition " + billNo, request, requisitionId);
            txnManager.commit(status);

        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView saveRequisition(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String billno = "";
        boolean issuccess = false;
        boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
        boolean isDraft = (!StringUtil.isNullOrEmpty(request.getParameter("isdraft"))) ? Boolean.parseBoolean(request.getParameter("isdraft")) : false; //SDP-13487
        boolean isSaveDraftRecord = (!StringUtil.isNullOrEmpty(request.getParameter("isSaveDraftRecord"))) ? Boolean.parseBoolean(request.getParameter("isSaveDraftRecord")) : false; //SDP-13487            
        boolean isAutoSeqForEmptyDraft = (!StringUtil.isNullOrEmpty(request.getParameter("isAutoSeqForEmptyDraft"))) ? Boolean.parseBoolean(request.getParameter("isAutoSeqForEmptyDraft")) : false; //SDP-13927 : If Draft already having sequence no. then do not update it
        boolean isDuplicateNoExe = false;
        boolean isAccountingExe = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;//txnManager.getTransaction(def);
        String entryNumber = request.getParameter("number");
        String companyid = "";
        try {
            String sequenceformat = request.getParameter("sequenceformat");
            companyid = sessionHandlerImpl.getCompanyid(request);
            String poid = request.getParameter("invoiceid");
            KwlReturnObject socnt =null;
            //Checks duplicate number in edit case
            if(!StringUtil.isNullOrEmpty(poid) && sequenceformat.equals("NA")){
                socnt = accPurchaseOrderobj.getEditPurchaseRequisitionCount(entryNumber, companyid, poid);
                if (socnt.getRecordTotalCount() > 0) {
                    isDuplicateNoExe = true;
                    throw new AccountingException(messageSource.getMessage("acc.field.PurchaseRequisitionnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
            }else if(sequenceformat.equals("NA")){//Checks duplicate number in add case
                socnt = accPurchaseOrderobj.getPurchaseRequisitionCount(entryNumber, companyid);
                if (socnt.getRecordTotalCount() > 0) {
                    isDuplicateNoExe = true;
                    throw new AccountingException(messageSource.getMessage("acc.field.PurchaseRequisitionnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
            }
            //Checks duplicate number in simultaneous transactions
            synchronized (this) {
                status = txnManager.getTransaction(def);
                if (sequenceformat.equals("NA")) {
                    //Check entry in temporary table
                    KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_Purchase_Requisition_ModuleId);
                    if (resultInv.getRecordTotalCount() > 0) {
                        isDuplicateNoExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.requisition.selectedrequisitionno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, RequestContextUtils.getLocale(request)));
                    } else {
                        //Insert entry into temporary table
                        accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_Purchase_Requisition_ModuleId);
                    }
                }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
            PurchaseRequisition requisition = saveRequisition(request);
            int approvalLevel = requisition.getApprovestatuslevel();        //To get count for Approval level. If there is no level then default count will be 11.
            billno = requisition.getPrNumber();
            String moduleName =Constants.PURCHASE_REQUISITION;
            if(isFixedAsset){
                moduleName = Constants.ASSET_PURCHASE_REQUISITION;
            }
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            String auditID = "62";
            String action = "added new";
            if (isEdit) {
                action = "updated";
                auditID = "63";
            }
            txnManager.commit(status);
            status=null;
            TransactionStatus AutoNoStatus = null;
            try {
                synchronized (this) {
                    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                    def1.setName("AutoNum_Tx");
                    def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    AutoNoStatus = txnManager.getTransaction(def1);
                    if (StringUtil.isNullOrEmpty(poid) && (!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA"))) {
                        boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                        int nextAutoNoInt=0;
                        String nextAutoNumber ="";
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        if (seqformat_oldflag) {
                            nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_REQUISITION, sequenceformat);
                            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextAutoNumber);
                        } else {
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_REQUISITION, sequenceformat, seqformat_oldflag, requisition.getRequisitionDate());
                        }
                        billno = seqNumberMap.get("autoentrynumber").toString();
                        seqNumberMap.put(Constants.DOCUMENTID, requisition.getID());
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        //SDP-13487 : When user save the transaction at very first time then transaction no. & sequence no.will be saved as empty.
                        seqNumberMap.put(Constants.isDraft, isDraft);
                        accPurchaseOrderobj.updateRequisitionEntryNumber(seqNumberMap);
                    }  else if(isSaveDraftRecord && !sequenceformat.equals("NA") && isAutoSeqForEmptyDraft){  //SDP-13487 : Do not update Invoice No. in case of Sequence Format as "NA",   //SDP-13927 : If Draft already having sequence no. then do not update it
                        /*
                        Below piece of code has written to handle Auto-Sequence no.in edit mode.
                        When user open the draft in edit mode, he can save it as a draft or a transaction. If it save as draft again then this code will not be execute.
                        But, if he saves it as a transaction then this code will be execute to get the Auto-Sequence No and set it to transaction no.
                        */
                        String nextAutoNumber = "";
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_REQUISITION, sequenceformat, false, requisition.getRequisitionDate());
                        billno = seqNumberMap.get("autoentrynumber").toString();
                        seqNumberMap.put(Constants.DOCUMENTID, requisition.getID());
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        seqNumberMap.put(Constants.isDraft, isDraft);
                        accPurchaseOrderobj.updateRequisitionEntryNumber(seqNumberMap);
                    } else if(isDraft && !sequenceformat.equals("NA") && isAutoSeqForEmptyDraft){
                        /* SDP-13923
                        This piece of code has been written to fix below case.
                        1)Draft has been made with NA. 2)Draft has opened in edit mode and saved as a draft again with Auto-Sequence Format.
                        3)Again draft opened in edit mode then sequence format should be Auto-Sequence Format.
                        */
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_REQUISITION, sequenceformat, false, requisition.getRequisitionDate());
                        billno = seqNumberMap.get("autoentrynumber").toString();
                        seqNumberMap.put(Constants.DOCUMENTID, requisition.getID());
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        seqNumberMap.put(Constants.isDraft, isDraft);
                        accPurchaseOrderobj.updateRequisitionEntryNumber(seqNumberMap);
                    }
                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " " +moduleName+" "+ billno +(approvalLevel == 1 ? " "+messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : ""), request, requisition.getID());
                txnManager.commit(AutoNoStatus);    
                }
            } catch (Exception ex) {
                if (AutoNoStatus != null) {
                    txnManager.rollback(AutoNoStatus);
                }
                //Delete entry from temporary table
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid,Constants.Acc_Purchase_Requisition_ModuleId);
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            issuccess = true;
            if (approvalLevel==1) {
//        	msg = messageSource.getMessage("acc.so.save1", null, RequestContextUtils.getLocale(request));   //"Quotation has been saved successfully";
                if (isFixedAsset) {
                    msg = messageSource.getMessage("acc.field.assetPurchaseRequisitionhasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) + ", " + messageSource.getMessage("acc.field.butpendingforApproval", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";
                } else {
                    msg = messageSource.getMessage("acc.field.PurchaseRequisitionhasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.field.butpendingforApproval", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";
                }
            } else {
                if(isDraft) {
                    //Below code snippet has written to show difference message when entry has been saved as a transaction or a draft.
                  if(isFixedAsset){
                      msg = messageSource.getMessage("acc.field.AssetPurchaseRequisitionDraft", null, RequestContextUtils.getLocale(request))+" "+messageSource.getMessage("acc.draft.success.msg.hasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
                  } else {
                      msg = messageSource.getMessage("acc.field.PurchaseRequisitionDraft", null, RequestContextUtils.getLocale(request))+" "+messageSource.getMessage("acc.draft.success.msg.hasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
                  }
                    //msg = (isFixedAsset ? messageSource.getMessage("acc.field.AssetPurchaseRequisitionDrafthasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.field.PurchaseRequisitionDrafthasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)));   //SDP-13487
                } else if (isFixedAsset) {
                    msg = messageSource.getMessage("acc.field.assetPurchaseRequisitionhasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";
                } else {
                    msg = messageSource.getMessage("acc.field.PurchaseRequisitionhasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";
                }
            }
            
            status = txnManager.getTransaction(def);
            //Delete entry from temporary table
            accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid,Constants.Acc_Purchase_Requisition_ModuleId);
            txnManager.commit(status);
        } catch (AccountingException ex) {
            if(status!=null){
            txnManager.rollback(status);
            }
            try {
                //Delete entry from temporary table
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid,Constants.Acc_Purchase_Requisition_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {//For check lock period
                msg = ex.getCause().getMessage();
            }
            isAccountingExe=true;
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if(status!=null){
            txnManager.rollback(status);
            }
            try {
                //Delete entry from temporary table
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid,Constants.Acc_Purchase_Requisition_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {//For check lock period
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("isDuplicateExe",isDuplicateNoExe);
                jobj.put("isAccountingExe", isAccountingExe);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public PurchaseRequisition saveRequisition(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        PurchaseRequisition requisition = null;
        try {
            String currentUser = sessionHandlerImpl.getUserid(request);
            String taxid = null;
            PurchaseRequisition pr=null;
            taxid = request.getParameter("taxid");
            double taxamount = StringUtil.getDouble(request.getParameter("taxamount"));
            String sequenceformat = request.getParameter("sequenceformat");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String entryNumber = request.getParameter("number");
            String costCenterId = request.getParameter("costcenter");
            String duedate = request.getParameter("duedate");
            String poid = request.getParameter("invoiceid");
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = createdby;
            String nextAutoNumber = "";
            String auditmsg = "added", auditID = "62";
            HashMap<String, Object> qDataMap = new HashMap<String, Object>();
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean isDraft = (!StringUtil.isNullOrEmpty(request.getParameter("isdraft"))) ? Boolean.parseBoolean(request.getParameter("isdraft")) : false; //SDP-13487
            boolean isSaveDraftRecord = (!StringUtil.isNullOrEmpty(request.getParameter("isSaveDraftRecord"))) ? Boolean.parseBoolean(request.getParameter("isSaveDraftRecord")) : false; //SDP-13487            
            boolean isAutoSeqForEmptyDraft = (!StringUtil.isNullOrEmpty(request.getParameter("isAutoSeqForEmptyDraft"))) ? Boolean.parseBoolean(request.getParameter("isAutoSeqForEmptyDraft")) : false; //SDP-13927 : If Draft already having sequence no. then do not update it
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            synchronized (this) {
                //If Purchase Requisition Linked in Vendor Quotation, then throw an exception.
                KwlReturnObject cnresult = accPurchaseOrderobj.getVendorQuotationLinkedWithPR(poid, companyid);
                List listc = cnresult.getEntityList();
                if(listc.size() >= 1){
//                   throw new AccountingException("Cannot Edit Purcahse Requisition as it is or was already used in Other Transactions"); 
                   throw new AccountingException(messageSource.getMessage("acc.field.PurchaseRequisitionCannotEdit", null, RequestContextUtils.getLocale(request))); 
                }
                
                SequenceFormat prevSeqFormat = null;
                if (!StringUtil.isNullOrEmpty(poid)) {//Edit case
                    KwlReturnObject socnt = accPurchaseOrderobj.getEditPurchaseRequisitionCount(entryNumber, companyid, poid);
                    if (socnt.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.PurchaseRequisitionnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    } else {
                        auditmsg = "updated";
                        auditID = "63";
                        qDataMap.put("id", poid);
                        KwlReturnObject rst = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), poid);
                        pr = (PurchaseRequisition) rst.getEntityList().get(0);
                        prevSeqFormat = pr.getSeqformat();
                        if (!sequenceformat.equals("NA")) {
                            nextAutoNumber = entryNumber;
                        }
                        if (isFixedAsset) {
                            HashMap<String, Object> requestMap = new HashMap<String, Object>();
                            requestMap.put("companyid", companyid);
                            requestMap.put("purchaserequisitionid", poid);
                            accPurchaseOrderobj.deletePurchaseRequisitionAssetDetails(requestMap);
                        }
                        accPurchaseOrderobj.deletePurchaseRequisitionDetails(poid, companyid);
                    }
                } else {//Create new case
                    KwlReturnObject socnt = accPurchaseOrderobj.getPurchaseRequisitionCount(entryNumber, companyid);
                    if (socnt.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.PurchaseRequisitionnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                }

                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Purchase_Requisition_ModuleId, entryNumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                }
                
                /*if (!sequenceformat.equals("NA") && prevSeqFormat == null) {//generate sequence number
                    boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                    String nextAutoNoInt = "";
                    if (seqformat_oldflag) {
                        nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_REQUISITION, sequenceformat);
                    } else {
                        String[] nextAutoNoTemp = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_REQUISITION, sequenceformat, seqformat_oldflag);
                        nextAutoNumber = nextAutoNoTemp[0];
                        nextAutoNoInt = nextAutoNoTemp[1];
                        qDataMap.put(Constants.SEQFORMAT, sequenceformat);
                        qDataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                    }
                    entryNumber = nextAutoNumber;
                }*/
                }

            DateFormat df = authHandler.getDateOnlyFormat(request);
            /*
                In edit case of draft where auto-sequence no.is enable and not a save action then we have set entryno as empty to avoid duplication.
            */
            if(isDraft && isAutoSeqForEmptyDraft && !StringUtil.isNullOrEmpty(entryNumber)){
                entryNumber = "";
            }
            if(isDraft && !isSaveDraftRecord && !StringUtil.isNullOrEmpty(poid) && !sequenceformat.equals("NA") && StringUtil.isNullOrEmpty(entryNumber)){ 
                qDataMap.put("entrynumber", "");
            } else if(sequenceformat.equals("NA") || !StringUtil.isNullOrEmpty(poid)){
                qDataMap.put("entrynumber", entryNumber);
            }else{
                qDataMap.put("entrynumber", "");
            }
            qDataMap.put("autogenerated", sequenceformat.equals("NA")?false:true);
            qDataMap.put("isFixedAsset", isFixedAsset);
            qDataMap.put("memo", request.getParameter("memo"));
            qDataMap.put("vendorid", request.getParameter("vendor"));
            qDataMap.put("orderdate", df.parse(request.getParameter("billdate")));
            qDataMap.put("perDiscount", StringUtil.getBoolean(request.getParameter("perdiscount")));
            qDataMap.put("discount", StringUtil.getDouble(request.getParameter("discount")));
            qDataMap.put("createdby", createdby);
            qDataMap.put("modifiedby", modifiedby);
            long createdon = System.currentTimeMillis();
            qDataMap.put("createdon", createdon);
            //boolean isDraft = Boolean.TRUE.equals(Boolean.parseBoolean(request.getParameter("isdraft")));
            qDataMap.put(Constants.Checklocktransactiondate, request.getParameter("billdate"));//ERP-16800-Without parsing date
            qDataMap.put(Constants.isSaveAsDraft, isDraft);
            if (StringUtil.isNullOrEmpty(poid) || !isDraft) {
                qDataMap.put("approvestatuslevel", 0);
            }
            if (isDraft) {
                qDataMap.put("approvestatuslevel", Constants.DraftedPurchaseRequisitions); // return int -99  and 1 to 10 are pending approval levels and  level<0 means rejected records
            }

            qDataMap.put("currencyid", currencyid);

            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                qDataMap.put("costCenterId", costCenterId);
            }
            if (!StringUtil.isNullOrEmpty(duedate)) {
                qDataMap.put("duedate", df.parse(duedate));
            }
            qDataMap.put("companyid", companyid);
            qDataMap.put("userid", sessionHandlerImpl.getUserid(request));
            if (taxid != null && !taxid.isEmpty()) {
                Tax tax = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxid);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                }
                qDataMap.put("taxid", taxid);
            }

            KwlReturnObject soresult = accPurchaseOrderobj.savePurchaseRequisition(qDataMap);
            requisition = (PurchaseRequisition) soresult.getEntityList().get(0);

            qDataMap.put("id", requisition.getID());
            HashSet<PurchaseRequisitionDetail> sodetails = saveRequisitionRows(request, requisition, companyid);

            //Save global-level CUSTOMFIELDS for PurchaseRequisition
            String customfield = request.getParameter("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_Purchase_Requisition_modulename);
                customrequestParams.put("moduleprimarykey", "PurchaseRequisitionId");
                customrequestParams.put("modulerecid", requisition.getID());
                customrequestParams.put("moduleid", !isFixedAsset?Constants.Acc_Purchase_Requisition_ModuleId:Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_PurchaseRequisition_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    qDataMap.put("accpurchaserequisitioncustomdataref", requisition.getID());
                    KwlReturnObject accresult = accPurchaseOrderobj.updatePurchaseRequisitionCustomData(qDataMap);
                }
            }
            
            // Update Requisition Apprival Status
            if (!isDraft) {
                double amount = 0;
                amount = getAmountBasetoCurrency(sodetails, companyid, sessionHandlerImpl.getCurrencyID(request), requisition.getCurrency().getCurrencyID(), requisition.getRequisitionDate());
                /**
                 * create product details JSON array for product category rule
                 */
                JSONArray productDiscountJArr=new JSONArray();
                for (PurchaseRequisitionDetail prDetail : sodetails) {
                    String productId = prDetail.getProduct().getID();
                    double discountVal = prDetail.getDiscount();
                    int isDiscountPercent = prDetail.getDiscountispercent();
                    if(isDiscountPercent==1){
                        discountVal = (prDetail.getQuantity()*prDetail.getRate())*(discountVal/100);
                    }
                    KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, requisition.getRequisitionDate(), 0.0);
                    double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                    discAmountinBase = authHandler.round(discAmountinBase, companyid);
                    JSONObject productDiscountObj=new JSONObject();
                    productDiscountObj.put("productId", productId);
                    productDiscountObj.put("discountAmount", discAmountinBase);
                    productDiscountJArr.put(productDiscountObj);
                }
                
                /**
                 * parameters required for sending mail
                 */
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put(Constants.companyid, companyid);
                mailParameters.put(Constants.requisiton, requisition);
                mailParameters.put(Constants.Acc_level, 0);
                mailParameters.put(Constants.amount, String.valueOf(amount));
                mailParameters.put(Constants.fromCreate, true);
                mailParameters.put(Constants.isFixedAsset, isFixedAsset);
                mailParameters.put(Constants.createdBy, currentUser);
                mailParameters.put(Constants.productArray, productDiscountJArr);
                mailParameters.put(Constants.PAGE_URL, URLUtil.getPageURL(request, loginpageFull));                
                int level = approvePendingRequisition(mailParameters);
                if (level == 11) {       //PR is not pending for Approval
                    requisition.setApprovestatuslevel(11);
                } else {                  //PR is pending for Approval with Rule : level-x    ('x' be an integer)
                    requisition.setApprovestatuslevel(level);
                }
            }
             String moduleName =Constants.PURCHASE_REQUISITION;
            if(isFixedAsset){
                moduleName = Constants.ASSET_PURCHASE_REQUISITION;
            }
            //Send Mail when Purchase Requisition is generated or modified.
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), sessionHandlerImpl.getCompanyid(request));
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (documentEmailSettings != null) {
                boolean sendmail = false;
                boolean isEditMail = false;
                if (StringUtil.isNullOrEmpty(poid)) {
                    if (isFixedAsset && documentEmailSettings.isAssetPurchaseReqGenerationMail()) {
                        sendmail = true;
                    } else if (documentEmailSettings.isPurchaseReqGenerationMail()) {
                        sendmail = true;
                    }

                } else {
                    isEditMail = true;
                    if (isFixedAsset && documentEmailSettings.isAssetPurchaseReqUpdationMail()) {
                        sendmail = true;
                    } else if (documentEmailSettings.isPurchaseReqUpdationMail()) {
                        sendmail = true;
                    }

                }
                if (sendmail) {
                    String userMailId="",userName="",currentUserid="";
                    String createdByEmail = "";
                    String createdById = "";
                    HashMap<String, Object> requestParams= AccountingManager.getEmailNotificationParams(request);
                    if(requestParams.containsKey("userfullName")&& requestParams.get("userfullName")!=null){
                        userName=(String)requestParams.get("userfullName");
                    }
                    if(requestParams.containsKey("usermailid")&& requestParams.get("usermailid")!=null){
                        userMailId=(String)requestParams.get("usermailid");
                    }
                    if(requestParams.containsKey(Constants.useridKey)&& requestParams.get(Constants.useridKey)!=null){
                        currentUserid=(String)requestParams.get(Constants.useridKey);
                    }
                    List<String> mailIds = new ArrayList();
                    if (!StringUtil.isNullOrEmpty(userMailId)) {
                        mailIds.add(userMailId);
                    }
                    /*
                     if Edit mail option is true then get userid and Email id of document creator.
                    */
                     if (isEditMail) {
                         if (pr != null && pr.getCreatedby() != null) {
                             createdByEmail = pr.getCreatedby().getEmailID();
                             createdById = pr.getCreatedby().getUserID();
                         }
                         /*
                          if current user userid == document creator userid then don't add creator email ID in List.
                          */
                         if (!StringUtil.isNullOrEmpty(createdByEmail) && !(currentUserid.equalsIgnoreCase(createdById))) {
                             mailIds.add(createdByEmail);
                         }
                     }
                    String[] temp = new String[mailIds.size()];
                    String[] tomailids = mailIds.toArray(temp);
                    String prNumber = entryNumber;
                    accountingHandlerDAOobj.sendSaveTransactionEmails(prNumber, moduleName, tomailids, userName, isEditMail, companyid);
                }
            }
           /*
            int approvalLevel = requisition.getApprovestatuslevel();
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            String action = "added new";
            if (isEdit) {
                action = "updated";
            }
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " " +moduleName+" "+ requisition.getPrNumber() +(approvalLevel == 1 ? " "+messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : ""), request, requisition.getID());*/
        } catch (JSONException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ScriptException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveRequisition : " + ex.getMessage(), ex);
        }
        return requisition;
    }

    public HashSet saveRequisitionRows(HttpServletRequest request, PurchaseRequisition requisition, String companyid) throws ServiceException, AccountingException, UnsupportedEncodingException, SessionExpiredException {
        HashSet rows = new HashSet();
        try {
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                
                if(jobj.has("srno")) {
                   qdDataMap.put("srno",jobj.getInt("srno"));
                }
                qdDataMap.put("companyid", companyid);
                qdDataMap.put("vendorquotationid", requisition.getID());
                qdDataMap.put("productid", jobj.getString("productid"));
                qdDataMap.put("rate", jobj.optDouble("rate",0));//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
                if (jobj.has("priceSource")) {
                    qdDataMap.put("priceSource", !StringUtil.isNullOrEmpty(jobj.optString("priceSource")) ? StringUtil.DecodeText(jobj.optString("priceSource")) : "");
                }
                if (jobj.has("pricingbandmasterid")) {
                    qdDataMap.put("pricingbandmasterid", !StringUtil.isNullOrEmpty(jobj.optString("pricingbandmasterid")) ? StringUtil.DecodeText(jobj.optString("pricingbandmasterid")) : "");
                }
                qdDataMap.put("quantity", jobj.getDouble("quantity"));
                if (jobj.has("uomid")) {
                    qdDataMap.put("uomid", jobj.getString("uomid"));
                }
                qdDataMap.put("balanceqty", jobj.getDouble("quantity"));
                if (jobj.has("baseuomquantity") && jobj.get("baseuomquantity") != null) {
                    qdDataMap.put("baseuomquantity", jobj.getDouble("baseuomquantity"));
                    qdDataMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                } else {
                    qdDataMap.put("baseuomquantity", jobj.getDouble("quantity"));
                    qdDataMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                }
                qdDataMap.put("remark", jobj.optString("remark"));
                qdDataMap.put("desc", StringUtil.DecodeText(jobj.optString("desc")));
                qdDataMap.put("discount", jobj.optDouble("prdiscount",0));
                qdDataMap.put("discountispercent", jobj.optInt("discountispercent",0));
                qdDataMap.put("approverremark", jobj.getString("approverremark"));
                if (!StringUtil.isNullOrEmpty(jobj.optString("rowid",""))) {
                    qdDataMap.put("workorderdetailid", jobj.getString("rowid"));
                }
                String rowtaxid = jobj.getString("prtaxid");
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null) {
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    } else {
                        qdDataMap.put("rowtaxid", rowtaxid);
                    }
                }
                //  row.setTax(rowtax);

                KwlReturnObject result = accPurchaseOrderobj.savePurchaseRequisitionDetails(qdDataMap);
                PurchaseRequisitionDetail row = (PurchaseRequisitionDetail) result.getEntityList().get(0);
                
                // Save Line-level CUSTOMFIELDS for PurchaseRequisition
                String customfield = jobj.getString("customfield");
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    HashMap<String, Object> DOMap = new HashMap<String, Object>();
                    JSONArray jcustomarray = new JSONArray(customfield);

                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "PurchaseRequisitionDetail");
                    customrequestParams.put("moduleprimarykey","PurchaseRequisitionDetailId");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put("moduleid", !isFixedAsset?Constants.Acc_Purchase_Requisition_ModuleId:Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId);
                    customrequestParams.put("companyid", companyid);
                    DOMap.put("id", row.getID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_PurchaseRequisitionDetail_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        DOMap.put("accpurchaserequisitiondetailcustomdataref", row.getID());
                        accPurchaseOrderobj.updatePurchaseRequisitionDetailCustomData(DOMap);
                    }
                }
                
                // save assets
                if (isFixedAsset) {
                    JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
                    paramJobj.put("locale", RequestContextUtils.getLocale(request));
                    Set<PurchaseRequisitionAssetDetails> assetDetailsSet = accPurchaseOrderModuleServiceObj.savePurchaseRequisitionAssetDetails(paramJobj, jobj.getString("productid"), jobj.getString("assetDetails"), false, false, false);
                    Set<AssetPurchaseRequisitionDetailMapping> assetInvoiceDetailMappings = accPurchaseOrderModuleServiceObj.saveAssetPurchaseRequisitionDetailMapping(row.getID(), assetDetailsSet, companyid, Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId);
                }
                
                rows.add(row);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveRequisitionRows : " + ex.getMessage(), ex);
        }
        return rows;
    }
        
    public ModelAndView deletePurchaseRequisition(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isFixedAsset))) ? Boolean.parseBoolean(request.getParameter(Constants.isFixedAsset)) : false;
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            requestJobj.put("servletContext", this.getServletContext());
            String linkedTransactions = deletePurchaseRequisitions(requestJobj);
            String[] linkedTransactionsArray = linkedTransactions.split(",");
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransactions)) {
                if (isFixedAsset) {
                    msg = messageSource.getMessage("acc.field.assetPurchaseRequisitionhasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                } else {
                    msg = messageSource.getMessage("acc.field.PurchaseRequisitionhasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                }
            } else {
                if (isFixedAsset) {
                    if (linkedTransactionsArray.length == 1) {
                        msg = messageSource.getMessage("acc.pr.assetPurchaseRequisitionSingleExcept", null, RequestContextUtils.getLocale(request));
                    } else {
                        msg = messageSource.getMessage("acc.pr.assetPurchaseRequisitionExcept", null, RequestContextUtils.getLocale(request)) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                    }
                } else {
                    if (linkedTransactionsArray.length == 1) {
                        msg = messageSource.getMessage("acc.pr.purchaseRequisitionSingleExcept", null, RequestContextUtils.getLocale(request));
                    } else {
                        msg = messageSource.getMessage("acc.pr.purchaseRequisitionExcept", null, RequestContextUtils.getLocale(request)) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                    }
                }
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deletePurchaseRequisitions(JSONObject requestJobj) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransactions = "";
        try {
            JSONArray jArr = new JSONArray(requestJobj.optString(Constants.RES_data));
            String companyid = requestJobj.optString(Constants.companyKey);
            boolean isFixedAsset = false;
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.isFixedAsset))) {
                isFixedAsset = Boolean.parseBoolean(requestJobj.optString(Constants.isFixedAsset));
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString(Constants.billid))) {
                    linkedTransactions = accPurchaseOrderModuleServiceObj.deletePurchaseRequisition(jobj, requestJobj, linkedTransactions, companyid, isFixedAsset);
                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
        }
        return linkedTransactions;
    }

    public ModelAndView deletePurchaseRequisitionPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isFixedAsset))) ? Boolean.parseBoolean(request.getParameter(Constants.isFixedAsset)) : false;
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            requestJobj.put("servletContext", this.getServletContext());
            String linkedTransactions = deletePurchaseRequisitionsPermanent(requestJobj);
            String[] linkedTransactionsArray = linkedTransactions.split(",");
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransactions)) {
                if (isFixedAsset) {
                    msg = messageSource.getMessage("acc.field.assetPurchaseRequisitionhasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                } else {
                    msg = messageSource.getMessage("acc.field.PurchaseRequisitionhasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                }
            } else {
                if (isFixedAsset) {
                    if (linkedTransactionsArray.length == 1) {
                        msg = messageSource.getMessage("acc.pr.assetPurchaseRequisitionSingleExcept", null, RequestContextUtils.getLocale(request));
                    } else {
                        msg = messageSource.getMessage("acc.pr.assetPurchaseRequisitionExcept", null, RequestContextUtils.getLocale(request)) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                    }
                } else {
                    if (linkedTransactionsArray.length == 1) {
                        msg = messageSource.getMessage("acc.pr.purchaseRequisitionSingleExcept", null, RequestContextUtils.getLocale(request));
                    } else {
                        msg = messageSource.getMessage("acc.pr.purchaseRequisitionExcept", null, RequestContextUtils.getLocale(request)) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                    }
                }
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deletePurchaseRequisitionsPermanent(JSONObject requestJobj) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransactions = "";
        try {
            JSONArray jArr = new JSONArray(requestJobj.optString(Constants.RES_data));
            String companyid = requestJobj.optString(Constants.companyKey);
            boolean isFixedAsset = false;
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.isFixedAsset))) {
                isFixedAsset = Boolean.parseBoolean(requestJobj.optString(Constants.isFixedAsset));
            }

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.billid))) {
                    linkedTransactions = accPurchaseOrderModuleServiceObj.deletePurchaseRequisitionPermanent(jobj, requestJobj, linkedTransactions, companyid, isFixedAsset);
                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
        }
        return linkedTransactions;
    }
    
    public ModelAndView rejectPurchaseRequisition(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String roleName="";
            int level=0;
            KwlReturnObject userRoleResult = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
            Iterator itr = userRoleResult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                roleName = row[1].toString();
            }            
            String pRID="";
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONObject jObj= jArr.getJSONObject(0);
            pRID= StringUtil.DecodeText(jObj.optString("billid"));
            KwlReturnObject PRObj = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), pRID);
            PurchaseRequisition prObj = (PurchaseRequisition) PRObj.getEntityList().get(0);
            level=prObj.getApprovestatuslevel();
            
            boolean isRejected=rejectPurchaseRequisition(request);
            txnManager.commit(status);
            issuccess = true;
            
            if (isRejected) {
                msg = messageSource.getMessage("acc.field.PurchaseRequisitionhasbeenrejectedsuccessfully", null, RequestContextUtils.getLocale(request))+" by "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" at Level "+level+".";
            } else {
                msg = messageSource.getMessage("acc.vq.notAuthorisedToRejectThisRecord", null, RequestContextUtils.getLocale(request))+" at level "+level+".";
            }            
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public boolean rejectPurchaseRequisition(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException, ScriptException {
        boolean isRejected=false;
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            boolean isFixedAsset = StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))?false : Boolean.parseBoolean(request.getParameter("isFixedAsset"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currentUser = sessionHandlerImpl.getUserid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String remark = request.getParameter("remark");
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), currentUser);
            User user = (User) userResult.getEntityList().get(0);
            String actionId = "66", actionMsg = "rejected";
            int level=0;
            String amount="";   
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            for (int i = 0; i < jArr.length(); i++) {
                boolean hasAuthorityToReject=false;
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String poid = StringUtil.DecodeText(jobj.optString("billid"));
                    PurchaseRequisition requisition = (PurchaseRequisition) kwlCommonTablesDAOObj.getClassObject(PurchaseRequisition.class.getName(), poid);
                    double totalAmount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount")) ? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);
                    amount=String.valueOf(totalAmount);
                    level= requisition.getApprovestatuslevel();
                    JSONArray productJArr = new JSONArray();
                    HashMap<String, Object> prApproveMap = new HashMap<String, Object>();
                    for (PurchaseRequisitionDetail prDetail : requisition.getRows()) {
                        String productId = prDetail.getProduct().getID();
                        /*
                        Added To handle product discount rule in  pending approval
                        Calculated Discount and added in Json array for further calculation
                        */
                        double discountVal = prDetail.getDiscount();
                        int isDiscountPercent = prDetail.getDiscountispercent();
                        if (isDiscountPercent == 1) {
                            discountVal = (prDetail.getQuantity() * prDetail.getRate()) * (discountVal / 100);
                        }
                        KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, requisition.getRequisitionDate(), 0.0);
                        double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                        discAmountinBase = authHandler.round(discAmountinBase, companyid);
                        JSONObject productObj = new JSONObject();
                        productObj.put("productId", productId);
                        productObj.put("discountAmount", discAmountinBase);
                        productJArr.put(productObj);
                    }
                    prApproveMap.put("productDiscountMapList", productJArr);
                    prApproveMap.put("level", level);
                    prApproveMap.put("companyid", companyid);
                    prApproveMap.put("totalAmount", amount);
                    prApproveMap.put("currentUser", currentUser);
                    if (isFixedAsset) {
                        prApproveMap.put("moduleid", Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId);
                    } else {
                        prApproveMap.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                    }
                    if (AccountingManager.isCompanyAdmin(user)) {
                        hasAuthorityToReject = true;
                    } else {
                        hasAuthorityToReject = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(prApproveMap);
                    }
                    if(hasAuthorityToReject){
                        accPurchaseOrderobj.rejectPurchaseRequisition(poid, companyid);
                        isRejected=true;
                        // Maintain Approval History of Rejected Record
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("transtype", isFixedAsset?Constants.FIXEDASSETS_PURCHASE_REQUISITION_APPROVAL: Constants.PURCHASE_REQUISITION_APPROVAL);
                        hashMap.put("transid", requisition.getID());
                        hashMap.put("approvallevel", Math.abs(requisition.getApprovestatuslevel()));//  If approvedLevel = 11 then its final Approval
                        hashMap.put("remark", remark);
                        hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                        hashMap.put("companyid", companyid);
                        hashMap.put("isrejected", true);
                        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                        auditTrailObj.insertAuditLog(actionId, "User " + sessionHandlerImpl.getUserFullName(request) + " " + actionMsg + " Purchase Requisition " + requisition.getPrNumber(), request, requisition.getID());
                    }
                }
            }
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }
        return isRejected;
    }

    public ModelAndView getRequisitionFlowData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONArray jArr = new JSONArray();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put("companyid", companyid);
            KwlReturnObject flowresult = accPurchaseOrderobj.getRequisitionFlowData(qdDataMap);
            Iterator itr = flowresult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", row[0]);
                obj.put("level", row[1]);
                obj.put("rule", row[2]);
                String userName = "", userId = "";
                KwlReturnObject userResult = accPurchaseOrderobj.getRequisitionFlowTargetUsers(row[0].toString());
                Iterator useritr = userResult.getEntityList().iterator();
                while (useritr.hasNext()) {
                    Object[] userrow = (Object[]) useritr.next();
                    userId += userrow[0] + ",";
                    userName += userrow[1] + ",";
                }
                if (!StringUtil.isNullOrEmpty(userName)) {
                    userName = userName.substring(0, userName.length() - 1);
                    userId = userId.substring(0, userId.length() - 1);
                }
                obj.put("users", userName);
                obj.put("userids", userId);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accPurchaseOrderController.getRequisitionFlowData:" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accPurchaseOrderController.getRequisitionFlowData:" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView addrequisitionflowlevel(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String rule = "";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String approver = request.getParameter("approver");
            String isrule = request.getParameter("isrule");
            String rulelevel = request.getParameter("level");
            if (!StringUtil.isNullOrEmpty(isrule) && isrule.equals("on")) {
                String limitvalue = request.getParameter("limitvalue");
                int ruleType = Integer.parseInt(request.getParameter("rule"));// 1 - Greaer Than Equal To , 2 - Less Than Equal To, 3 - Equal to
                switch (ruleType) {
                    case 1:
                        rule = "$$>" + limitvalue;
                        break;
                    case 2:
                        rule = "$$<" + limitvalue;
                        break;
                    case 3:
                        rule = "$$==" + limitvalue;
                        break;
                    case 4:
                        rule = limitvalue + "<=$$" + " && $$<=" + request.getParameter("ulimitvalue");
                        break;
                }
            }
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put("level", rulelevel);
            qdDataMap.put("rule", rule);
            qdDataMap.put("companyid", companyid);
            qdDataMap.put("approver", approver);
            qdDataMap.put("hasapprover", !StringUtil.isNullOrEmpty(approver));

            KwlReturnObject result = accPurchaseOrderobj.savePurchaseRequisitionFlow(qdDataMap);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.RequisitionApprovalRulehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            auditTrailObj.insertAuditLog("70", "User " + sessionHandlerImpl.getUserFullName(request) + " added Purchase Requisition Flow at level-" + rulelevel, request, result.getEntityList().get(0).toString());
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleterequisitionflowlevel(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            String id = request.getParameter("id");
            qdDataMap.put("id", id);
            KwlReturnObject result = accPurchaseOrderobj.deletePurchaseRequisitionFlow(qdDataMap);
            auditTrailObj.insertAuditLog("71", "User " + sessionHandlerImpl.getUserFullName(request) + " deleted Purchase Requisition Flow", request, result.getEntityList().get(0).toString());
            issuccess = true;
            msg = messageSource.getMessage("acc.field.RequisitionApprovalRulehasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private double getAmountBasetoCurrency(Set<PurchaseRequisitionDetail> rowDetails, String companyid, String companyCurrencyId, String transactionCurrencyId, java.util.Date transactionDate) {
        double convertedAmount = 0;
        try {
            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", companyCurrencyId);
            for (PurchaseRequisitionDetail row : rowDetails) {
                double quotationPrice = authHandler.round(row.getQuantity() * row.getRate(), companyid);
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, quotationPrice, transactionCurrencyId, transactionDate, 0);
                convertedAmount += (Double) bAmt.getEntityList().get(0);
            }
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return convertedAmount;
        }
    }

    public ModelAndView approvePendingRequisition(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        String billno = request.getParameter("billno");
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String currentUser = sessionHandlerImpl.getUserid(request);
            String remark = request.getParameter("remark");
            String prID = request.getParameter("billid");
            String docID = request.getParameter("docID");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String companyCurrency = sessionHandlerImpl.getCurrencyID(request);
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            KwlReturnObject PRObj = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), prID);
            PurchaseRequisition requisition = (PurchaseRequisition) PRObj.getEntityList().get(0);
            int level = requisition.getApprovestatuslevel();
            String approvalpendingStatusmsg = "";
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

//            Iterator itrRow = requisition.getRows().iterator();
            double amount = 0;
            amount = getAmountBasetoCurrency(requisition.getRows(), companyid, companyCurrency, requisition.getCurrency().getCurrencyID(), requisition.getRequisitionDate());
//            while(itrRow.hasNext()) {
//                PurchaseRequisitionDetail ItemDetail = (PurchaseRequisitionDetail) itrRow.next();
//                amount +=(ItemDetail.getQuantity() * ItemDetail.getRate());
//            }
            /**
             * create product details JSON array for product category rule
             */
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            JSONArray productJArr = new JSONArray();
            for (PurchaseRequisitionDetail prDetail : requisition.getRows()) {
                String productId = prDetail.getProduct().getID();
                /*
                 Added To handle product discount rule in  pending approval
                 Calculated Discount and added in Json array for further calculation
                 */
                double discountVal = prDetail.getDiscount();
                int isDiscountPercent = prDetail.getDiscountispercent();
                if (isDiscountPercent == 1) {
                    discountVal = (prDetail.getQuantity() * prDetail.getRate()) * (discountVal / 100);
                }
                KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, companyCurrency, requisition.getRequisitionDate(), 0.0);
                double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                discAmountinBase = authHandler.round(discAmountinBase, companyid);

                JSONObject productObj = new JSONObject();
                productObj.put("productId", productId);
                productObj.put("discountAmount", discAmountinBase);
                productJArr.put(productObj);
            }
            /**
             * parameters required for sending mail
             */
            Map<String, Object> mailParameters = new HashMap();
            mailParameters.put(Constants.companyid, companyid);
            mailParameters.put(Constants.requisiton, requisition);
            mailParameters.put(Constants.Acc_level, requisition.getApprovestatuslevel());
            mailParameters.put(Constants.amount, String.valueOf(amount));
            mailParameters.put(Constants.fromCreate, false);
            mailParameters.put(Constants.isFixedAsset, isFixedAsset);
            mailParameters.put(Constants.createdBy, currentUser);
            mailParameters.put(Constants.productArray, productJArr);
            mailParameters.put(Constants.PAGE_URL, URLUtil.getPageURL(request, loginpageFull));
            int approvedLevel = approvePendingRequisition(mailParameters);
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            if (approvedLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                String userName = sessionHandlerImpl.getUserFullName(request);
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                String sendorInfo = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                String creatormail = company.getCreator().getEmailID();
                String documentcreatoremail = (requisition != null && requisition.getCreatedby() != null) ? requisition.getCreatedby().getEmailID() : "";
                String fname = company.getCreator().getFirstName() == null ? "" : company.getCreator().getFirstName();
                String lname = company.getCreator().getLastName() == null ? "" : company.getCreator().getLastName();
                String creatorname = fname + " " + lname;
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                ArrayList<String> emailArray = new ArrayList<>();
                qdDataMap.put(Constants.companyKey, companyid);
                qdDataMap.put("level", level);
                qdDataMap.put(Constants.moduleid, isFixedAsset ? Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId : Constants.Acc_Purchase_Requisition_ModuleId);
//                emailArray = commonFnControllerService.getUserApprovalEmail(qdDataMap);
                emailArray.add(creatormail);
                emailArray.add(creatormail);
                if (!StringUtil.isNullOrEmpty(documentcreatoremail) && !creatormail.equalsIgnoreCase(documentcreatoremail)) {
                    emailArray.add(documentcreatoremail);
                }
                String[] emails = {};
                emails = emailArray.toArray(emails);
//                String[] emails = {creatormail};
                if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                    String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                    emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                }
                if (requisition.getApprovestatuslevel() < 11) {
                    qdDataMap.put("totalAmount", String.valueOf(amount));
                    qdDataMap.put("productDiscountMapList", productJArr);
                approvalpendingStatusmsg=commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
                Map<String, Object> mailparameters = new HashMap();
                mailparameters.put("Number", requisition.getPrNumber());
                mailparameters.put("userName", userName);
                mailparameters.put("emails", emails);
                mailparameters.put("sendorInfo", sendorInfo);
                mailparameters.put("addresseeName", "All");
                mailparameters.put("companyid", company.getCompanyID());
                mailparameters.put("baseUrl", baseUrl);
                mailparameters.put("approvalstatuslevel", requisition.getApprovestatuslevel());
                mailparameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
                if (emails.length > 0) {
                    if (isFixedAsset) {
                        mailparameters.put("moduleName",  Constants.ASSET_PURCHASE_REQUISITION);
                        accountingHandlerDAOobj.sendApprovedEmails(mailparameters);
                    } else {
                        mailparameters.put("moduleName", Constants.PURCHASE_REQUISITION);
                        accountingHandlerDAOobj.sendApprovedEmails(mailparameters);
                    }
                }
            }
            
            // Save Approval History
            if (approvedLevel != Constants.NoAuthorityToApprove) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("transtype", isFixedAsset?Constants.FIXEDASSETS_PURCHASE_REQUISITION_APPROVAL: Constants.PURCHASE_REQUISITION_APPROVAL);
                hashMap.put("transid", requisition.getID());
                hashMap.put("approvallevel", requisition.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
                hashMap.put("remark", remark);
                hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                hashMap.put("companyid", companyid);
                KwlReturnObject approvalHResult = accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                Approvalhistory approvalHistory = (Approvalhistory) approvalHResult.getEntityList().get(0);
                
                if (!StringUtil.isNullOrEmpty(docID)) {
                    HashMap<String, Object> approvalDocMap = new HashMap<String, Object>();
                    approvalDocMap.put("docID", docID);
                    approvalDocMap.put("companyID", companyid);
                    approvalDocMap.put("approvalHistoryID", approvalHistory.getID());
                    
                    accPurchaseOrderobj.saveApprovalDocMap(approvalDocMap);
                }

                // Audit log entry
                txnManager.commit(status);
                issuccess = true;
                KwlReturnObject kmsg = null;
                String roleName="Company User";
                kmsg = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
                Iterator ite2 = kmsg.getEntityList().iterator();
                while (ite2.hasNext()) {
                Object[] row = (Object[]) ite2.next();
                roleName = row[1].toString();
               }
                String moduleName = Constants.PURCHASE_REQUISITION;
                if (isFixedAsset) {
                    moduleName = Constants.ASSET_PURCHASE_REQUISITION;
                }

                auditTrailObj.insertAuditLog(AuditAction.PURCHASE_REQUISITION_APPROVED, "User " + sessionHandlerImpl.getUserFullName(request) + " has Approved a "+moduleName+"  " + billno+" at Level-"+requisition.getApprovestatuslevel(), request, billno);
                if (isFixedAsset) {
                   msg = messageSource.getMessage("acc.field.assetPurchaseRequisitionhasbeenapprovedsuccessfully", null, RequestContextUtils.getLocale(request))+" by "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" at Level "+requisition.getApprovestatuslevel()+".";

                } else {
                    msg = messageSource.getMessage("acc.field.PurchaseRequisitionhasbeenapprovedsuccessfully", null, RequestContextUtils.getLocale(request))+" "+messageSource.getMessage("acc.common.by", null, RequestContextUtils.getLocale(request))+" "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" "+messageSource.getMessage("acc.common.atLevel", null, RequestContextUtils.getLocale(request))+" "+requisition.getApprovestatuslevel()+".";
                }
            } else {
                txnManager.commit(status);
                issuccess = true;
                msg = messageSource.getMessage("acc.field.youHaveNotBeenSetApproverForThisRecord", null, RequestContextUtils.getLocale(request));
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * 
     * @param mailParameters(PurchaseRequisition requisition, String companyid, int level, String amount, String currentUser, boolean fromCreate, boolean isFixedAsset, JSONArray productJArr, String PAGE_URL)
     * @return
     * @throws SessionExpiredException
     * @throws AccountingException
     * @throws ServiceException
     * @throws ScriptException
     * @throws MessagingException
     * @throws JSONException 
     */
    public int approvePendingRequisition(Map<String, Object> mailParameters) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException, JSONException {
        boolean hasAuthority = false;
        PurchaseRequisition requisition = null;
        String companyid = "";
        boolean fromCreate = false;
        boolean isFixedAsset = false;
        int level = 0;
        String amount = "0.0";
        String currentUser = "";
        JSONArray productJArr = null;
        if(mailParameters.containsKey(Constants.requisiton)){
            requisition = (PurchaseRequisition) mailParameters.get(Constants.requisiton);
        }
        if(mailParameters.containsKey(Constants.companyid)){
            companyid = (String) mailParameters.get(Constants.companyid);
        }
        if(mailParameters.containsKey(Constants.Acc_level)){
            level = (int) mailParameters.get(Constants.Acc_level);
        }
        if(mailParameters.containsKey(Constants.amount)){
            amount = (String) mailParameters.get(Constants.amount);
        }
        if(mailParameters.containsKey(Constants.createdBy)){
            currentUser = (String) mailParameters.get(Constants.createdBy);
        }
        if(mailParameters.containsKey(Constants.fromCreate)){
            fromCreate = (boolean) mailParameters.get(Constants.fromCreate);
        }
        if(mailParameters.containsKey(Constants.isFixedAsset)){
            isFixedAsset = (boolean) mailParameters.get(Constants.isFixedAsset);
        }
        if(mailParameters.containsKey(Constants.productArray)){
            productJArr = (JSONArray) mailParameters.get(Constants.productArray);
        }
        /**
         * create purchase requisition details map for multi level approval rule and authority.
         */
        HashMap<String, Object> prApproveMap = new HashMap<String, Object>();
        prApproveMap.put("companyid", companyid);
        prApproveMap.put("level", level);
        prApproveMap.put("totalAmount", amount);
        prApproveMap.put("currentUser", currentUser);
        prApproveMap.put("fromCreate", fromCreate);
        prApproveMap.put("moduleid", isFixedAsset ? Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId : Constants.Acc_Purchase_Requisition_ModuleId);
        prApproveMap.put("productDiscountMapList", productJArr);
        
        if (!fromCreate) {
            String thisUser = currentUser;
//            KwlReturnObject isAdmin=accPurchaseOrderobj.checkForAdmin(thisUser,companyid);
//            Object[] userrow1 = (Object[])isAdmin.getEntityList().get(0);
//            String roleId=(userrow1[1].toString());

            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
            User user = (User) userclass.getEntityList().get(0);

            if (AccountingManager.isCompanyAdmin(user)) {
                hasAuthority = true;
            } else {
                if (isFixedAsset) {
//                    hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRule(level, companyid, amount, thisUser, Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId);
                    hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(prApproveMap);
                } else {
//                    hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRule(level, companyid, amount, thisUser, Constants.Acc_Purchase_Requisition_ModuleId);
                    hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(prApproveMap);
                }
            }
        } else {
            hasAuthority = true;
        }
        if (hasAuthority) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            String requisitionApprovalSubject = isFixedAsset ? "Asset Purchase Requisition: %s - Approval Notification" : "Purchase Requisition: %s - Approval Notification";
            String requisitionApprovalHtmlMsg = "<html><head><title>Deskera Accounting - Your Deskera Account</title></head><style type='text/css'>"
                    + "a:link, a:visited, a:active {\n"
                    + " 	color: #03C;"
                    + "}\n"
                    + "body {\n"
                    + "	font-family: Arial, Helvetica, sans-serif;"
                    + "	color: #000;"
                    + "	font-size: 13px;"
                    + "}\n"
                    + "</style><body>"
                    + "<p>Hi All,</p>"
                    + "<p></p>"
                    + "<p>%s has created requisition %s and sent it to you for approval. at level "+(level+1)+"</p>"
                    + "<p>Please review and approve it (Purchase Requisition Number: %s).</p>"
                    + "<p>Company Name:- %s</p>"
                    + "<p>Please check on Url:- %s</p>"
                    + "<p></p>"
                    + "<p>Thanks</p>"
                    + "<p>This is an auto generated email. Do not reply<br>";
            String requisitionApprovalPlainMsg = "Hi All,\n\n"
                    + "%s has created requisition %s and sent it to you for approval. at level "+(level +1)+"\n"
                    + "Please review and approve it (Purchase Requisition Number: %s).\n\n"
                    + "Company Name:- %s \n"
                    + "Please check on Url:- %s \n\n"
                    + "Thanks\n\n"
                    + "This is an auto generated email. Do not reply\n";
            int approvalStatus = 11;
            KwlReturnObject cap = null;
            if (mailParameters.containsKey(Constants.companyid)) {
                cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            }
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String prNumber = requisition.getPrNumber();
            String prID = requisition.getID();
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put("companyid", companyid);
            qdDataMap.put("level", level + 1);
            if (isFixedAsset) {
                qdDataMap.put("moduleid", Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId);
            } else {
                qdDataMap.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
            }
            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            Iterator itr = flowresult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                //            JSONObject obj = new JSONObject();
                String rule = "";
                if (row[2] != null) {
                    rule = row[2].toString();
                }
                String discountRule = "";
                if (row[7] != null) {
                    discountRule = row[7].toString();
                }
                int appliedUpon = Integer.parseInt(row[5].toString());
                boolean sendForApproval = false;
                if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount) {
                    if (productJArr != null) {
                        sendForApproval = AccountingManager.checkForProductAndProductDiscountRule(productJArr, appliedUpon, rule, discountRule);
                    }
                }else if(appliedUpon == Constants.Specific_Products_Category){
                    /*
                     * Check If Rule is apply on product
                     * category from multiapproverule window
                     */
                    sendForApproval = accountingHandlerDAOobj.checkForProductCategoryForProduct(productJArr, appliedUpon, rule);
                } else {
                    rule = rule.replaceAll("[$$]+", amount);
                }
                if (StringUtil.isNullOrEmpty(rule) || sendForApproval || (!StringUtil.isNullOrEmpty(rule) && appliedUpon == Constants.Total_Amount && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                    // send emails
                    try {
                        if (Boolean.parseBoolean(row[3].toString()) && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                            HashMap<String, Object> requestParams = new HashMap();
                            requestParams.put("companyid", companyid);
                            requestParams.put("prNumber", prNumber);
                            requestParams.put("archieve", 0);

                            String fromName = "User";
                            //String fromEmailId = Constants.ADMIN_EMAILID;
                            KwlReturnObject returnObject = null;
                            if(mailParameters.containsKey(Constants.companyid)){
                                returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                            }
                            Company company = (Company) returnObject.getEntityList().get(0);
                            String fromEmailId = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                            fromName = requisition.getUsers().getFirstName().concat(" ").concat(requisition.getUsers().getLastName());
//                            String fromEmailId1 = StringUtil.isNullOrEmpty(requisition.getCompany().getEmailID()) ? authHandlerDAOObj.getSysEmailIdByCompanyID(requisition.getCompany().getCompanyID()) : requisition.getCompany().getEmailID();
//                                if (!StringUtil.isNullOrEmpty(fromEmailId1)) {
//                                    fromEmailId = fromEmailId1;
//                                }
                            String companyName = company.getCompanyName();
                            String subject = "";
                            String htmlMsg = "";
                            String plainMsg = "";
                            subject = String.format(requisitionApprovalSubject, prNumber);
                            htmlMsg = String.format(requisitionApprovalHtmlMsg, fromName, prNumber, prNumber, companyName, (String) mailParameters.get(Constants.PAGE_URL));
                            plainMsg = String.format(requisitionApprovalPlainMsg, fromName, prNumber, prNumber, companyName, (String) mailParameters.get(Constants.PAGE_URL));
                            ArrayList<String> emailArray = new ArrayList<String>();
                            String[] emails = {};
                            qdDataMap.put("ruleid", row[0].toString());
                            KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(qdDataMap);
                            Iterator useritr = userResult.getEntityList().iterator();
                            while (useritr.hasNext()) {
                                Object[] userrow = (Object[]) useritr.next();
                                emailArray.add(userrow[3].toString());
                                //                        userId +=userrow[0]+",";
                                //                        userName +=userrow[1]+",";
                            }
                            emails = emailArray.toArray(emails);
                            if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                                String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                                emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                            }
                            if (emails.length > 0) {
                                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                                SendMailHandler.postMail(emails, subject, htmlMsg, plainMsg, fromEmailId, smtpConfigMap);
                            }
                            //                        userName = userName.substring(0,userName.length()-1);
                            //                        userId = userId.substring(0,userId.length()-1);
                        }
                    } catch (MessagingException ex) {
                        Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    approvalStatus = level + 1;
                }
                //            obj.put("id", row[0]);
                //            obj.put("level", row[1]);
                //            obj.put("rule",row[2]);
            }
            accPurchaseOrderobj.approvePendingRequisition(prID, companyid, approvalStatus);
            return approvalStatus;
        } else {
            return Constants.NoAuthorityToApprove; //if not have approval permission then return one fix value like 999
        }
    }

    // RFQ 
    public ModelAndView saveRFQ(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isDuplicateNoExe = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
	DefaultTransactionDefinition def2 = new DefaultTransactionDefinition();
        def2.setName("Quotation_Tx2");
        def2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null, status2 = null;
        String entryNumber = request.getParameter("number");
        String companyid = "";
        try {
            boolean isEdit = (!StringUtil.isNullOrEmpty(request.getParameter("isEdit"))) ? Boolean.parseBoolean(request.getParameter("isEdit")) : false;
            String sequenceformat = request.getParameter("sequenceformat");
            String RFQid = request.getParameter("invoiceid");
            companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject socnt =null;
            String auditmsg = "added";
            /*
                Checks duplicate number in edit case
            */
            if(!StringUtil.isNullOrEmpty(RFQid) && sequenceformat.equals("NA")){
                socnt = accPurchaseOrderobj.getEditRFQCount(entryNumber, companyid, RFQid);
                if (socnt.getRecordTotalCount() > 0) {
                    isDuplicateNoExe = true;
                    throw new AccountingException(messageSource.getMessage("acc.field.RequestForQuotationnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
            }else if(sequenceformat.equals("NA")){//Checks duplicate number in add case
                socnt = accPurchaseOrderobj.getRFQCount(entryNumber, companyid);
                if (socnt.getRecordTotalCount() > 0) {
                    isDuplicateNoExe = true;
                    throw new AccountingException(messageSource.getMessage("acc.field.RequestForQuotationnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
            }
            /*
                Checks duplicate number in simultaneous transactions
            */
            synchronized (this) {
                status = txnManager.getTransaction(def);
                if (sequenceformat.equals("NA")) {
                    /*
                        Check entry in temporary table
                    */
                    KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_RFQ_ModuleId);
                    if (resultInv.getRecordTotalCount() > 0) {
                        isDuplicateNoExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.RFQ.selectedrfq", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, RequestContextUtils.getLocale(request)));
                    } else {
                        /*
                            Insert entry into temporary table
                        */
                        accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_RFQ_ModuleId);
                    }
                }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
            RequestForQuotation requisition = saveRFQ(request);
            txnManager.commit(status);
            String billno = requisition.getRfqNumber();
            status = null;
            TransactionStatus AutoNoStatus = null;
            try {
                synchronized (this) {
                    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                    def1.setName("AutoNum_Tx");
                    def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    AutoNoStatus = txnManager.getTransaction(def1);
                    if (!isEdit && (!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA"))) {
                        boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                        String nextAutoNumber ="";
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        if (seqformat_oldflag) {
                            nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_RFQ, sequenceformat);
                            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextAutoNumber);
                        } else {
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_RFQ, sequenceformat, seqformat_oldflag, requisition.getRfqDate());
                        }
                        billno = seqNumberMap.get(Constants.AUTO_ENTRYNUMBER).toString();
                        seqNumberMap.put(Constants.DOCUMENTID, requisition.getID());
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        accPurchaseOrderobj.updateRFQEntryNumber(seqNumberMap);
                    }
                if(isEdit){
                    auditmsg="updated";
                }    
                auditTrailObj.insertAuditLog(AuditAction.RFQ_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has "+auditmsg+" RFQ " + billno, request, requisition.getID());
                txnManager.commit(AutoNoStatus);    
                }
            } catch (Exception ex) {
                if (AutoNoStatus != null) {
                    txnManager.rollback(AutoNoStatus);
                }
                /*
                    Delete entry from temporary table
                */
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid,Constants.Acc_RFQ_ModuleId);
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
            issuccess = true;
            msg = messageSource.getMessage("acc.field.RequestForQuotationRFQhasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";
            status = txnManager.getTransaction(def);
            /*
                Delete entry from temporary table
            */
            accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid,Constants.Acc_RFQ_ModuleId);
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {    //ERP-28190
                try {
                    status2 = txnManager.getTransaction(def2);
                    accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_RFQ_ModuleId);
                    txnManager.commit(status2);
                } catch (ServiceException ex1) {
                    Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex1);
                }
                txnManager.rollback(status);
            }
            /*
                Delete entry from temporary table
            */
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid,Constants.Acc_RFQ_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("isDuplicateExe",isDuplicateNoExe);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public RequestForQuotation saveRFQ(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        RequestForQuotation RFQ = null;
        try {
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            String taxid = null;
            taxid = request.getParameter("taxid");
            double taxamount = StringUtil.getDouble(request.getParameter("taxamount"));
            String sequenceformat = request.getParameter("sequenceformat");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String entryNumber = request.getParameter("number");
            String costCenterId = request.getParameter("costcenter");
            String duedate = request.getParameter("duedate");
            String RFQid = request.getParameter("invoiceid");
            boolean isEdit = (!StringUtil.isNullOrEmpty(request.getParameter("isEdit"))) ? Boolean.parseBoolean(request.getParameter("isEdit")) : false;
            boolean copyInv = (!StringUtil.isNullOrEmpty(request.getParameter("copyInv"))) ? Boolean.parseBoolean(request.getParameter("copyInv")) : false;
            String deletedLinkedDocumentID = request.getParameter("deletedLinkedDocumentId");
            String unlinkMessage="";
            String auditmsg = "added", auditID = "62";
            String nextAutoNumber = "";
            KwlReturnObject socnt = null;
            SequenceFormat prevSeqFormat = null;
            HashMap<String, Object> qDataMap = new HashMap<String, Object>();
            if ( isEdit && (!StringUtil.isNullOrEmpty(RFQid))) {//Edit case
                    socnt = accPurchaseOrderobj.getEditRFQCount(entryNumber, companyid, RFQid);
                     HashMap<String, Object> requestMap = new HashMap<String, Object>();
                    if (socnt.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.RequestForQuotationnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    } else {
                        auditmsg = "updated";
                        auditID = "63";
                        qDataMap.put("id", RFQid);
                        KwlReturnObject rst = accountingHandlerDAOobj.getObject(RequestForQuotation.class.getName(), RFQid);
                        RequestForQuotation pr = (RequestForQuotation) rst.getEntityList().get(0);
                        prevSeqFormat = pr.getSeqformat();
                       
                        requestMap.put("companyid", companyid);
                        requestMap.put("rfqid", RFQid);
                        if (!sequenceformat.equals("NA")) {
                            qDataMap.put(Constants.SEQFORMAT, pr.getSeqformat().getID());
                            qDataMap.put(Constants.SEQNUMBER, pr.getSeqnumber());
                            nextAutoNumber = entryNumber;
                        }
                        if (isFixedAsset) {
                            accPurchaseOrderobj.deleteRequestForQuotationAssetDetails(requestMap);
                        }
                        requestMap.put("isEditRfq", isEdit);
                        accPurchaseOrderobj.deleteRFQPermanent(requestMap);
                    }
                    
                    /*Deleting linking information of RFQ & Purchase Requisition during edit of RFQ */   
                    accPurchaseOrderobj.deleteLinkingInformationOfRFQ(requestMap);
                    
                   
                    if (!StringUtil.isNullOrEmpty(deletedLinkedDocumentID)) {
                        String[] deletedLinkedDocumentIDArr = deletedLinkedDocumentID.split(",");
                        for (int i = 0; i < deletedLinkedDocumentIDArr.length; i++) {
                            KwlReturnObject venresult = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), deletedLinkedDocumentIDArr[i]);
                            PurchaseRequisition purchaseRequisition = (PurchaseRequisition) venresult.getEntityList().get(0);
                            if (purchaseRequisition != null) {
                               
                                 /* Preparing audit trial message while unlinking document through Edit*/
                                if (i == 0) {
                                    unlinkMessage += " from the Purchase Requisition(s) ";
                                }
                                if (unlinkMessage.indexOf(purchaseRequisition.getPrNumber()) == -1) {
                                    unlinkMessage += purchaseRequisition.getPrNumber() + ", ";
                                }
                            } 
                        }
                    }
                  if (!StringUtil.isNullOrEmpty(unlinkMessage) && unlinkMessage.endsWith(", ")) {
                    unlinkMessage = unlinkMessage.substring(0, unlinkMessage.length() - 2);
                }
            } else {
                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_RFQ_ModuleId, entryNumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                }
            }
            DateFormat df = authHandler.getDateOnlyFormat(request);
            if(sequenceformat.equals("NA") || !StringUtil.isNullOrEmpty(RFQid)){
                qDataMap.put("entrynumber", entryNumber);
            }else{
                qDataMap.put("entrynumber", "");
            }
            qDataMap.put("autogenerated", sequenceformat.equals("NA")?false:true);
            qDataMap.put("isFixedAsset", isFixedAsset);
            qDataMap.put("memo", request.getParameter("memo"));
            qDataMap.put("vendorid", request.getParameter("vendor"));
            qDataMap.put("othervendoremails", request.getParameter("othervendoremails"));
            qDataMap.put("orderdate", df.parse(request.getParameter("billdate")));
            qDataMap.put("perDiscount", StringUtil.getBoolean(request.getParameter("perdiscount")));
            qDataMap.put("discount", StringUtil.getDouble(request.getParameter("discount")));
            qDataMap.put("approvestatuslevel", 0);
//            qDataMap.put("currencyid", currencyid);

            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                qDataMap.put("costCenterId", costCenterId);
            }
            if (!StringUtil.isNullOrEmpty(duedate)) {
                qDataMap.put("duedate", df.parse(duedate));
            }
            qDataMap.put("companyid", companyid);
            qDataMap.put("userid", sessionHandlerImpl.getUserid(request));
//            if (taxid != null && !taxid.isEmpty()) {
//                Tax tax = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxid);
//                if (tax == null) {
//                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
//                }
//                qDataMap.put("taxid", taxid);
//            }

            KwlReturnObject soresult = accPurchaseOrderobj.saveRFQ(qDataMap);
            RFQ = (RequestForQuotation) soresult.getEntityList().get(0);

            qDataMap.put("id", RFQ.getID());
            HashSet<RequestForQuotationDetail> sodetails = saveRFQRows(request, RFQ, companyid);
            RFQ.setRows(sodetails);

            String customfield = request.getParameter("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_RFQ_modulename);
                customrequestParams.put("moduleprimarykey","rfqId");
                customrequestParams.put("modulerecid", RFQ.getID());
                customrequestParams.put("moduleid", isFixedAsset?Constants.Acc_FixedAssets_RFQ_ModuleId:Constants.Acc_RFQ_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_RFQ_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    qDataMap.put("accrfqcustomdataref", RFQ.getID());
                    KwlReturnObject accresult = accPurchaseOrderobj.updateRFQCustomData(qDataMap);
                }
            }
//            auditTrailObj.insertAuditLog(AuditAction.RFQ_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has "+auditmsg+" RFQ " + RFQ.getRfqNumber(), request, RFQ.getID());
            //Send Mail when RFQ  is generated or modified.
            String moduleName=Constants.Acc_RFQ_modulename;
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), sessionHandlerImpl.getCompanyid(request));
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
             if (documentEmailSettings != null) {
                boolean sendmail = false;
                if (!isEdit) { 
                    if(documentEmailSettings.isRFQGenerationMail()){
                         sendmail = true;
                    }
                } else {
                     if(documentEmailSettings.isRFQUpdationMail()){
                         sendmail = true;
                    }
                }
                 if (sendmail) {
                     String userName = "", userMailId = "", mailIDs="";
                     String[] tomailids = {""};
                     HashMap<String, Object> requestParams = AccountingManager.getEmailNotificationParams(request);
                     if (requestParams.containsKey("userfullName") && requestParams.get("userfullName") != null) {
                         userName = (String) requestParams.get("userfullName");
                     }
                     if (requestParams.containsKey("usermailid") && requestParams.get("usermailid") != null) {
                         userMailId = (String) requestParams.get("usermailid");
                     }
                     mailIDs = request.getParameter("othervendoremails");
                     if (!StringUtil.isNullOrEmpty(mailIDs)) {
                         tomailids = mailIDs.split(",");
                     }
                     String rfqNumber = entryNumber;
                     accountingHandlerDAOobj.sendSaveTransactionEmails(rfqNumber, moduleName, tomailids,userMailId, userName, isEdit, companyid);
                 }
            }
           /* Updating entry in audit trial when RFQ is unlinking from Purchase Requisition*/
             if (!StringUtil.isNullOrEmpty(unlinkMessage)) {
                auditTrailObj.insertAuditLog(AuditAction.RFQ_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "RFQ " + entryNumber + unlinkMessage + ".", request, entryNumber);
            }
             
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveRFQ : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveRFQ : " + ex.getMessage(), ex);
        }
        return RFQ;
    }

    public HashSet saveRFQRows(HttpServletRequest request, RequestForQuotation requisition, String companyid) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {
        HashSet rows = new HashSet();
        try {
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean isEdit = false;
            boolean copyInv=false;
            String docid = "";
            if(request.getParameter("isEdit") != null && !StringUtil.isNullOrEmpty(request.getParameter("isEdit").toString())){
                isEdit= Boolean.parseBoolean(request.getParameter("isEdit").toString());
            }
            if (request.getParameter("copyInv") != null && !StringUtil.isNullOrEmpty(request.getParameter("copyInv").toString())) {
                copyInv = Boolean.parseBoolean(request.getParameter("copyInv").toString());
            }
            String billid = request.getParameter("invoiceid");
            String entryNumber = request.getParameter("number");
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put("srno", i + 1);
                qdDataMap.put("companyid", companyid);
                qdDataMap.put("vendorquotationid", requisition.getID());
                qdDataMap.put("productid", jobj.getString("productid"));
                if (StringUtil.isNullOrEmpty(jobj.get("rate").toString())) {
                    qdDataMap.put("rate", 0.0);
                } else {
                    qdDataMap.put("rate", jobj.getDouble("rate"));//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
                }
                qdDataMap.put("quantity", jobj.getDouble("quantity"));
                if (jobj.has("uomid")) {
                    qdDataMap.put("uomid", jobj.getString("uomid"));
                }
                if (jobj.has("baseuomquantity") && jobj.get("baseuomquantity") != null) {
                    qdDataMap.put("baseuomquantity", jobj.getDouble("baseuomquantity"));
                    qdDataMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                } else {
                    qdDataMap.put("baseuomquantity", jobj.getDouble("quantity"));
                    qdDataMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                }
                qdDataMap.put("remark", jobj.optString("desc"));
                qdDataMap.put("pricingbandmasterid", jobj.optString("pricingbandmasterid"));
                if (jobj.has("priceSource")) {
                    qdDataMap.put("priceSource", !StringUtil.isNullOrEmpty(jobj.optString("priceSource")) ? StringUtil.DecodeText(jobj.optString("priceSource")) : "");
                }
//                qdDataMap.put("discount", jobj.getDouble("prdiscount"));
//                qdDataMap.put("discountispercent", jobj.getInt("discountispercent"));                
                qdDataMap.put("prid", !jobj.getString("billid").isEmpty()?jobj.getString("billid"):billid);
                if(isEdit || copyInv){ 
                    // While creating new RFQ, billid is purchase requisition ID
                    // While editing RFQ, linkid is purchase requisition ID
                    qdDataMap.put("prid", jobj.getString("linkid"));
                }
//                String rowtaxid = jobj.getString("prtaxid");
//                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
//                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(),rowtaxid); // (Tax)session.get(Tax.class, taxid);
//                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
//                    if (rowtax == null)
//                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
//                    else
//                        qdDataMap.put("rowtaxid", rowtaxid);
//                 }
                //  row.setTax(rowtax);
                
                     
                /* In Edit & Copy case, linkid is Purchase Requisition ID
                 While at the time of Creating RFQ, billid is Purchase Requisition ID
                 */
                if (isEdit || copyInv) {
                    if (docid.indexOf(jobj.getString("linkid")) == -1) {
                        docid += jobj.getString("linkid") + ",";
                    }

                } else {
                    if (docid.indexOf(jobj.getString("billid")) == -1) {
                        docid += jobj.getString("billid") + ",";
                    }

                }
                    

                KwlReturnObject result = accPurchaseOrderobj.saveRFQDetails(qdDataMap);
                RequestForQuotationDetail row = (RequestForQuotationDetail) result.getEntityList().get(0);
                 
                String customfield = jobj.getString("customfield");
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    HashMap<String, Object> DOMap = new HashMap<String, Object>();
                    JSONArray jcustomarray = new JSONArray(customfield);

                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "RequestForQuotationDetail");
                    customrequestParams.put("moduleprimarykey","RequestForQuotationDetailId");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put("moduleid", isFixedAsset?Constants.Acc_FixedAssets_RFQ_ModuleId:Constants.Acc_RFQ_ModuleId);
                    customrequestParams.put("companyid", companyid);
                    DOMap.put("id", row.getID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_RequestForQuotationDetail_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        DOMap.put("accrequestforquotationdetailcustomdataref", row.getID());
                        accPurchaseOrderobj.updateRFQDetailCustomData(DOMap);
                    }
                }
                
                // save assets
                if (isFixedAsset) {
                    JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
                    Set<PurchaseRequisitionAssetDetails> assetDetailsSet = accPurchaseOrderModuleServiceObj.savePurchaseRequisitionAssetDetails(paramJobj, jobj.getString("productid"), jobj.getString("assetDetails"), false, false, false);
                    Set<AssetPurchaseRequisitionDetailMapping> assetInvoiceDetailMappings = accPurchaseOrderModuleServiceObj.saveAssetPurchaseRequisitionDetailMapping(row.getID(), assetDetailsSet, companyid, Constants.Acc_FixedAssets_RFQ_ModuleId);
                }
                
                rows.add(row);
            }
            /*
             *   Linking information not to be used in fixed asset case. 
             */
            if (!isFixedAsset) {
                if (!StringUtil.isNullOrEmpty(docid) && docid.endsWith(", ")) {
                    docid = docid.substring(0, docid.length() - 1);
                }

                if(!StringUtil.isNullOrEmpty(docid)){
                    String[] docidArr = docid.split(",");

                    for (int i = 0; i < docidArr.length; i++) {
                        /* Saving linking information of Purchase requisition while linking with RFQ*/
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("linkeddocid", requisition.getID());
                        requestParams.put("docid", docidArr[i]);
                        requestParams.put("moduleid", Constants.Acc_RFQ_ModuleId);
                        requestParams.put("linkeddocno", entryNumber);
                        requestParams.put("sourceflag", 0);
                        KwlReturnObject result1 = accPurchaseOrderobj.savePurchaseRequisitionLinking(requestParams);

                        /* Saving linking information of RFQ while linking with Purchase requisition*/
                        requestParams.put("sourceflag", 1);
                        requestParams.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), docidArr[i]);
                        PurchaseRequisition purchaserequisition = (PurchaseRequisition) rdresult.getEntityList().get(0);
                        String prno = purchaserequisition.getPrNumber();
                        requestParams.put("linkeddocno", prno);
                        requestParams.put("docid", requisition.getID());
                        requestParams.put("linkeddocid", docidArr[i]);
                        result1 = accPurchaseOrderobj.saveRFQLinking(requestParams);
                    }
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveRFQRows : " + ex.getMessage(), ex);
        }
        return rows;
    }
    
    /**
     * Description : Below Method is used to update the some fields in Request For Quotation i.e Memo,Custom fields and
     * Line level description and Custom fields. 
     * @param <request> used to get request Parameters
       @param <response> used to send success message and success flag
     * @return :JSONObject
     */
    public ModelAndView updateRequestForQuotation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false, isEdit=false, isFixedAsset=false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        String companyid = "", RFQid = "", customfield = "";
        Map<String, Object> commonMap = new HashMap<String, Object>();

        try {
            status = txnManager.getTransaction(def);
             /* Get required value from request Object*/
            isEdit = (!StringUtil.isNullOrEmpty(request.getParameter("isEdit"))) ? Boolean.parseBoolean(request.getParameter("isEdit")) : false;
            isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            RFQid = request.getParameter("invoiceid");
            companyid = sessionHandlerImpl.getCompanyid(request);
            customfield = request.getParameter("customfield");
            
            /* Put required value in Map*/
            commonMap.put("companyid", companyid);
            commonMap.put("isEdit", isEdit);
            commonMap.put("isFixedAsset", isFixedAsset);
            commonMap.put("rfqid", RFQid);
            commonMap.put("customfield", customfield);
            commonMap.put("memo", request.getParameter("memo"));
            commonMap.put("detail", request.getParameter("detail"));

            
            
            /* Update Request For Quotation */
            RequestForQuotation requisition = updateRequestForQuotation(commonMap);
            String billno = requisition.getRfqNumber();
            msg = messageSource.getMessage("acc.field.RequestForQuotationRFQhasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";
            txnManager.commit(status);
            issuccess = true;
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     /**
     * Description : Below Method is used to update the Global level data of  Request For Quotation i.e Memo,Custom fields
     * @param <commonMap> used to get common required parameters from map
     * @return :RequestForQuotation
     */
    public RequestForQuotation updateRequestForQuotation(Map<String, Object> commonMap) throws SessionExpiredException, AccountingException, ServiceException {
        RequestForQuotation RFQ = null;
        String companyid = "", memo = "", customfield = "", RFQid = "";
        HashMap<String, Object> qDataMap = new HashMap<String, Object>();
        boolean isFixedAsset = false;
        try {
            if (commonMap.containsKey("companyid") && commonMap.get("companyid") != null) {
                companyid = (String) commonMap.get("companyid");
            }
            if (commonMap.containsKey("isFixedAsset") && commonMap.get("isFixedAsset") != null) {
                isFixedAsset = (Boolean) commonMap.get("isFixedAsset");
            }
            if (commonMap.containsKey("rfqid") && commonMap.get("rfqid") != null) {
                RFQid = (String) commonMap.get("rfqid");
            }
            if (commonMap.containsKey("memo") && commonMap.get("memo") != null) {
                memo = (String) commonMap.get("memo");
            }
            if (commonMap.containsKey("customfield") && commonMap.get("customfield") != null) {
                customfield = (String) commonMap.get("customfield");
            }

            if (!StringUtil.isNullOrEmpty(RFQid)) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(RequestForQuotation.class.getName(), RFQid);
                RFQ = (RequestForQuotation) result.getEntityList().get(0);
            }
            if (RFQ != null) {
                qDataMap.put("id", RFQ.getID());
                RFQ.setMemo(memo);
                
                 /* Update Request For Quotation line level Details*/
                HashSet<RequestForQuotationDetail> sodetails = updateRequestForQuotationRows(commonMap);

                if (!StringUtil.isNullOrEmpty(customfield)) {
                    JSONArray jcustomarray = new JSONArray(customfield);
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_RFQ_modulename);
                    customrequestParams.put("moduleprimarykey", "rfqId");
                    customrequestParams.put("modulerecid", RFQ.getID());
                    customrequestParams.put("moduleid", isFixedAsset ? Constants.Acc_FixedAssets_RFQ_ModuleId : Constants.Acc_RFQ_ModuleId);
                    customrequestParams.put("companyid", companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_RFQ_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        qDataMap.put("accrfqcustomdataref", RFQ.getID());
                        KwlReturnObject accresult = accPurchaseOrderobj.updateRFQCustomData(qDataMap);
                    }
                }
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updateRequestForQuotation : " + ex.getMessage(), ex);
        }
        return RFQ;
    }
     /**
     * Description : Below Method is used to update the line level data of  Request For Quotation i.e Description,Custom fields
     * @param <commonMap> used to get common required parameters from map
     * @return :HashSet
     */
    public HashSet updateRequestForQuotationRows(Map<String, Object> commonMap) throws ServiceException, SessionExpiredException {
        HashSet rows = new HashSet();
        String companyid = "", memo = "", customfield = "", detail = "";
        HashMap<String, Object> qDataMap = new HashMap<String, Object>();
        boolean isFixedAsset = false;
        try {
            if (commonMap.containsKey("companyid") && commonMap.get("companyid") != null) {
                companyid = (String) commonMap.get("companyid");
            }
            if (commonMap.containsKey("isFixedAsset") && commonMap.get("isFixedAsset") != null) {
                isFixedAsset = (Boolean) commonMap.get("isFixedAsset");
            }
            if (commonMap.containsKey("detail") && commonMap.get("detail") != null) {
                detail = (String) commonMap.get("detail");
            }

            JSONArray jArr = new JSONArray(detail);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                RequestForQuotationDetail row = null;
                if (jobj.has("rowid")) {
                    KwlReturnObject paymentAdvanceDetail = accountingHandlerDAOobj.getObject(RequestForQuotationDetail.class.getName(), jobj.getString("rowid"));
                    row = (RequestForQuotationDetail) paymentAdvanceDetail.getEntityList().get(0);
                }

                if (row != null) {

                    if (!StringUtil.isNullOrEmpty(jobj.optString("desc"))) {
                        try {
                            row.setRemark(StringUtil.DecodeText(jobj.optString("desc")));
                        } catch (Exception ex) {
                            row.setRemark(jobj.optString("desc"));
                        }
                    }
                    if (jobj.has("srno")) {
                        row.setSrno(jobj.getInt("srno"));
                    }

                    customfield = jobj.getString("customfield");
                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        HashMap<String, Object> DOMap = new HashMap<String, Object>();
                        JSONArray jcustomarray = new JSONArray(customfield);

                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "RequestForQuotationDetail");
                        customrequestParams.put("moduleprimarykey", "RequestForQuotationDetailId");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put("moduleid", isFixedAsset ? Constants.Acc_FixedAssets_RFQ_ModuleId : Constants.Acc_RFQ_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        DOMap.put("id", row.getID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_RequestForQuotationDetail_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            DOMap.put("accrequestforquotationdetailcustomdataref", row.getID());
                            accPurchaseOrderobj.updateRFQDetailCustomData(DOMap);
                        }
                    }
                    rows.add(row);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveRFQRows : " + ex.getMessage(), ex);
        }
        return rows;
    }
    
    public ModelAndView deleteRFQ(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            requestJobj.put("servletContext", this.getServletContext());
            String linkedTransactions = deleteRFQs(requestJobj);
            String[] linkedTransactionsArray = linkedTransactions.split(",");
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransactions)) {
                msg = messageSource.getMessage("acc.field.RFQhasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else if (linkedTransactionsArray.length > 0) {
                msg = messageSource.getMessage("acc.field.RFQhasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request)) + " Execept " + linkedTransactions.substring(0, linkedTransactions.length() - 2);
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteRFQs(JSONObject requestJobj) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransactions = "";
        try {
            JSONArray jArr = new JSONArray(requestJobj.optString(Constants.RES_data));
            String companyid = requestJobj.optString(Constants.companyKey);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString(Constants.billid))) {
                    linkedTransactions = accPurchaseOrderModuleServiceObj.deleteRFQ(jobj, requestJobj, linkedTransactions, companyid);
                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
        }
        return linkedTransactions;
    }

    public ModelAndView deleteRFQPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            requestJobj.put("servletContext", this.getServletContext());
            String linkedTransactions = deleteRFQsPermanent(requestJobj);
            String[] linkedTransactionsArray = linkedTransactions.split(",");
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransactions)) {
                msg = messageSource.getMessage("acc.field.RFQhasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else {
                if (linkedTransactionsArray.length > 0) {
                    msg = messageSource.getMessage("acc.field.RFQhasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request)) + " Execept " + linkedTransactions.substring(0, linkedTransactions.length() - 1);
                }
            }

        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteRFQsPermanent(JSONObject requestJobj) throws SessionExpiredException, AccountingException, ServiceException, JSONException {
        String linkedTransactions = "";
        try {
            JSONArray jArr = new JSONArray(requestJobj.optString(Constants.RES_data));

            boolean isFixedAsset = false;
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.isFixedAsset))) {
                isFixedAsset = Boolean.parseBoolean(requestJobj.optString(Constants.isFixedAsset));
            }

            String companyid = requestJobj.optString(Constants.companyKey);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                linkedTransactions = accPurchaseOrderModuleServiceObj.deleteRFQPermanent(jobj, requestJobj, linkedTransactions, companyid, isFixedAsset);
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
        }
        return linkedTransactions;
    }
    
//    public Map<String, Object> getVendorDefaultAddressParams(Vendor vendor,String companyid) {
//        Map<String, Object> addressMap = new HashMap<String, Object>();
//        try {
//            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
//            addrRequestParams.put("vendorid", vendor.getID());
//            addrRequestParams.put("companyid", companyid);
//            addrRequestParams.put("isDefaultAddress", true);
//            KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
//            if (!addressResult.getEntityList().isEmpty()) {
//                List<VendorAddressDetails> vendAddrList = addressResult.getEntityList();
//                for (VendorAddressDetails vendAddr : vendAddrList) {
//                    if (vendAddr.isIsBillingAddress()) {
//                        addressMap.put(Constants.BILLING_ADDRESS_TYPE, vendAddr.getAliasName()==null?"":vendAddr.getAliasName());
//                        addressMap.put(Constants.BILLING_ADDRESS, vendAddr.getAddress()==null?"":vendAddr.getAddress());
//                        addressMap.put(Constants.BILLING_COUNTRY, vendAddr.getCountry()==null?"":vendAddr.getCountry());
//                        addressMap.put(Constants.BILLING_STATE, vendAddr.getState()==null?"":vendAddr.getState());
//                        addressMap.put(Constants.BILLING_CITY, vendAddr.getCity()==null?"":vendAddr.getCity());
//                        addressMap.put(Constants.BILLING_POSTAL, vendAddr.getPostalCode()==null?"":vendAddr.getPostalCode());
//                        addressMap.put(Constants.BILLING_EMAIL, vendAddr.getEmailID()==null?"":vendAddr.getEmailID());
//                        addressMap.put(Constants.BILLING_FAX, vendAddr.getFax()==null?"":vendAddr.getFax());
//                        addressMap.put(Constants.BILLING_MOBILE, vendAddr.getMobileNumber()==null?"":vendAddr.getMobileNumber());
//                        addressMap.put(Constants.BILLING_PHONE, vendAddr.getPhone()==null?"":vendAddr.getPhone());
//                        addressMap.put(Constants.BILLING_RECIPIENT_NAME, vendAddr.getRecipientName()==null?"":vendAddr.getRecipientName());
//                        addressMap.put(Constants.BILLING_CONTACT_PERSON, vendAddr.getContactPerson()==null?"":vendAddr.getContactPerson());
//                        addressMap.put(Constants.BILLING_CONTACT_PERSON_NUMBER, vendAddr.getContactPersonNumber()==null?"":vendAddr.getContactPersonNumber());
//                    } else {
//                        addressMap.put(Constants.SHIPPING_ADDRESS_TYPE, vendAddr.getAliasName()==null?"":vendAddr.getAliasName());
//                        addressMap.put(Constants.SHIPPING_ADDRESS, vendAddr.getAddress()==null?"":vendAddr.getAddress());
//                        addressMap.put(Constants.SHIPPING_COUNTRY, vendAddr.getCountry()==null?"":vendAddr.getCountry());
//                        addressMap.put(Constants.SHIPPING_STATE, vendAddr.getState()==null?"":vendAddr.getState());
//                        addressMap.put(Constants.SHIPPING_CITY, vendAddr.getCity()==null?"":vendAddr.getCity());
//                        addressMap.put(Constants.SHIPPING_EMAIL, vendAddr.getEmailID()==null?"":vendAddr.getEmailID());
//                        addressMap.put(Constants.SHIPPING_FAX, vendAddr.getFax()==null?"":vendAddr.getFax());
//                        addressMap.put(Constants.SHIPPING_MOBILE, vendAddr.getMobileNumber()==null?"":vendAddr.getMobileNumber());
//                        addressMap.put(Constants.SHIPPING_PHONE, vendAddr.getPhone()==null?"":vendAddr.getPhone());
//                        addressMap.put(Constants.SHIPPING_RECIPIENT_NAME, vendAddr.getPhone()==null?"":vendAddr.getRecipientName());
//                        addressMap.put(Constants.SHIPPING_POSTAL, vendAddr.getPostalCode()==null?"":vendAddr.getPostalCode());
//                        addressMap.put(Constants.SHIPPING_CONTACT_PERSON_NUMBER, vendAddr.getContactPersonNumber()==null?"":vendAddr.getContactPersonNumber());
//                        addressMap.put(Constants.SHIPPING_CONTACT_PERSON, vendAddr.getContactPerson()==null?"":vendAddr.getContactPerson());
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return addressMap;
//    }
    
    public ModelAndView getBudgeting(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = getBudgeting(paramJobj);
            issuccess = true;
        } catch (SessionExpiredException | ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accPurchaseOrderController.getBudgeting : " + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getBudgeting(JSONObject paramJobj) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String companyID = paramJobj.getString(Constants.companyKey);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;

            String dimensionValueStr = !StringUtil.isNullOrEmpty(paramJobj.optString("dimensionValue", null)) ? paramJobj.getString("dimensionValue") : "";
            String[] dimensionValueArr = dimensionValueStr.split(",");
            String productStr = !StringUtil.isNullOrEmpty(paramJobj.optString("product", null)) ? paramJobj.getString("product") : "";
            String[] productArr = productStr.split(",");
            String productCategoryStr = !StringUtil.isNullOrEmpty(paramJobj.optString("productCategory", null)) ? paramJobj.getString("productCategory") : "";
            String[] productCategoryArr = productCategoryStr.split(",");
            String frequencyType = !StringUtil.isNullOrEmpty(paramJobj.optString("frequencyType", null)) ? paramJobj.getString("frequencyType") : "";

            if (extraCompanyPreferences != null && extraCompanyPreferences.getBudgetType() == 0) {
                for (int i = 0; i < dimensionValueArr.length; i++) {
                    KwlReturnObject deptObj = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), dimensionValueArr[i]);
                    FieldComboData department = (FieldComboData) deptObj.getEntityList().get(0);

                    JSONObject obj = new JSONObject();
                    obj.put("dimensionValueUUID", department.getId());
                    obj.put("dimensionValue", department.getValue());

                    HashMap<String, Object> dataMap = new HashMap<>();
                    dataMap.put("dimensionValue", dimensionValueArr[i]);
                    dataMap.put("frequencyType", frequencyType);
                    dataMap.put("companyID", companyID);

                    KwlReturnObject result = accPurchaseOrderobj.getBudgeting(dataMap);
                    List<Budgeting> resultList = result.getEntityList();
                    for (Budgeting budgeting : resultList) {
                        if (frequencyType.equalsIgnoreCase("4")) {
                            obj.put(budgeting.getYear(), budgeting.getAmount());
                        } else {
                            obj.put(budgeting.getFrequencyColumn(), budgeting.getAmount());
                        }
                    }
                    jArr.put(obj);
                }
            } else if (extraCompanyPreferences != null && extraCompanyPreferences.getBudgetType() == 1) {
                for (int i = 0; i < dimensionValueArr.length; i++) {
                    for (int j = 0; j < productArr.length; j++) {
                        KwlReturnObject deptObj = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), dimensionValueArr[i]);
                        FieldComboData department = (FieldComboData) deptObj.getEntityList().get(0);

                        KwlReturnObject productObj = accountingHandlerDAOobj.getObject(Product.class.getName(), productArr[j]);
                        Product product = (Product) productObj.getEntityList().get(0);

                        JSONObject obj = new JSONObject();
                        obj.put("productUUID", product.getID());
                        obj.put("productName", product.getName());
                        obj.put("dimensionValueUUID", department.getId());
                        obj.put("dimensionValue", department.getValue());

                        HashMap<String, Object> dataMap = new HashMap<>();
                        dataMap.put("dimensionValue", dimensionValueArr[i]);
                        dataMap.put("product", productArr[j]);
                        dataMap.put("frequencyType", frequencyType);
                        dataMap.put("companyID", companyID);

                        KwlReturnObject result = accPurchaseOrderobj.getBudgeting(dataMap);
                        List<Budgeting> resultList = result.getEntityList();
                        for (Budgeting budgeting : resultList) {
                            if (frequencyType.equalsIgnoreCase("4")) {
                                obj.put(budgeting.getYear(), budgeting.getAmount());
                            } else {
                                obj.put(budgeting.getFrequencyColumn(), budgeting.getAmount());
                            }
                        }
                        jArr.put(obj);
                    }
                }
            } else if (extraCompanyPreferences != null && extraCompanyPreferences.getBudgetType() == 2) {
                for (int i = 0; i < dimensionValueArr.length; i++) {
                    for (int j = 0; j < productCategoryArr.length; j++) {
                        KwlReturnObject deptObj = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), dimensionValueArr[i]);
                        FieldComboData department = (FieldComboData) deptObj.getEntityList().get(0);

                        KwlReturnObject productObj = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), productCategoryArr[j]);
                        MasterItem productCategory = (MasterItem) productObj.getEntityList().get(0);

                        JSONObject obj = new JSONObject();
                        obj.put("productCategoryUUID", productCategory.getID());
                        obj.put("productCategory", productCategory.getValue());
                        obj.put("dimensionValueUUID", department.getId());
                        obj.put("dimensionValue", department.getValue());

                        HashMap<String, Object> dataMap = new HashMap<>();
                        dataMap.put("dimensionValue", dimensionValueArr[i]);
                        dataMap.put("productCategory", productCategoryArr[j]);
                        dataMap.put("frequencyType", frequencyType);
                        dataMap.put("companyID", companyID);

                        KwlReturnObject result = accPurchaseOrderobj.getBudgeting(dataMap);
                        List<Budgeting> resultList = result.getEntityList();
                        for (Budgeting budgeting : resultList) {
                            if (frequencyType.equalsIgnoreCase("4")) {
                                obj.put(budgeting.getYear(), budgeting.getAmount());
                            } else {
                                obj.put(budgeting.getFrequencyColumn(), budgeting.getAmount());
                            }
                        }
                        jArr.put(obj);
                    }
                }
            }

            jobj.put("data", jArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    
    public ModelAndView saveBudgeting(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true, isCommitEx = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyID = sessionHandlerImpl.getCompanyid(request);
            String dimensionValue = request.getParameter("dimensionValue") == null ? "" : request.getParameter("dimensionValue");
            String product = request.getParameter("product") == null ? "" : request.getParameter("product");
            String productCategory = request.getParameter("productCategory") == null ? "" : request.getParameter("productCategory");
            String frequencyType = request.getParameter("frequencyType") == null ? "" : request.getParameter("frequencyType");
            String frequencyColumn = request.getParameter("frequencyColumn") == null ? "" : request.getParameter("frequencyColumn");
            String year = request.getParameter("year") == null ? "" : request.getParameter("year");
            double amount = request.getParameter("amount") == null ? 0 : Double.parseDouble(request.getParameter("amount"));
            
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyID", companyID);
            requestParams.put("dimensionValue", dimensionValue);
            if (!StringUtil.isNullOrEmpty(product)) {
                requestParams.put("product", product);
            }
            if (!StringUtil.isNullOrEmpty(productCategory)) {
                requestParams.put("productCategory", productCategory);
            }
            requestParams.put("frequencyType", frequencyType);
            if (!StringUtil.isNullOrEmpty(frequencyColumn)) {
                requestParams.put("frequencyColumn", frequencyColumn);
            }
            if (!StringUtil.isNullOrEmpty(year)) {
                requestParams.put("year", year);
            }
            requestParams.put("amount", amount);
            
            KwlReturnObject result = accPurchaseOrderobj.getBudgeting(requestParams);
            
            if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                Budgeting budgeting = (Budgeting) result.getEntityList().get(0);
                requestParams.put("budgetingID", budgeting.getID());
            }
            
            accPurchaseOrderobj.saveBudgeting(requestParams);

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
            }
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
        
    public ModelAndView deleteQuotationVersions(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        String quotationVersions = "";
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            requestJobj.put("servletContext", this.getServletContext());
            quotationVersions = deleteQuotationVersions(requestJobj);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(quotationVersions)) {
                msg = messageSource.getMessage("acc.field.VendorQuotationsVersionhasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.VendorQuotationsversionexcept", null, RequestContextUtils.getLocale(request)) + " " + quotationVersions.substring(0, quotationVersions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteQuotationVersions(JSONObject requestJobj) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(requestJobj.optString(Constants.RES_data));
            String companyid = requestJobj.optString(Constants.companyKey);
            String quotationVersions = "";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                quotationVersions = accPurchaseOrderModuleServiceObj.deleteQuotationVersion(jobj, requestJobj, quotationVersions, companyid);
            }
            return quotationVersions;
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
        }
    }

    public ModelAndView deleteQuotationVersionsPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            requestJobj.put("servletContext", this.getServletContext());
            String quotationVersion = deleteQuotationVersionsPermanent(requestJobj);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(quotationVersion)) {
                msg = messageSource.getMessage("acc.field.VendorQuotationsVersionhasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.VendorQuotationsversionexcept", null, RequestContextUtils.getLocale(request)) + " " + quotationVersion.substring(0, quotationVersion.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteQuotationVersionsPermanent(JSONObject requestJobj) throws SessionExpiredException, AccountingException, ServiceException {
        String quotationVersion = "";
        try {
            JSONArray jArr = new JSONArray(requestJobj.optString(Constants.RES_data));
            String companyid = requestJobj.optString(Constants.companyKey);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                quotationVersion = accPurchaseOrderModuleServiceObj.deleteQuotationVersionPermanent(jobj, requestJobj, quotationVersion, companyid);
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
        }
        return quotationVersion;
    }
    
    /**
     * Description : Method is used to Update Entry in linking information table
     * for Vendor Quotation & Purchase Requisition If any Purchase Requisition
     * linked with Vendor Quotation
     *
     * @param <request> :-used to get sub domain for which you want to update
     * entry in linking information table
     * @param <response>:- used to send response
     * @return :JSONObject(contains success & Message whether script is
     * completed or not)
     */
    public ModelAndView updatePurchaseRequisitionLinkingScript(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();

            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                KwlReturnObject result = accPurchaseOrderobj.getLinkedVQWithPurchaseRequisition(requestParams);
                Iterator itr2 = result.getEntityList().iterator();
                while (itr2.hasNext()) {
                    String vendorQuotationID = (String) itr2.next();
                    if (!StringUtil.isNullOrEmpty(vendorQuotationID)) {
                        
    /*
                         * Method is used for updating linking information of
                         *
                         * Vendor Quotation in linking table linked with
                         * Purchase Requisition
     */
    
                        updateLinkingInformationOfPR(vendorQuotationID,false);
                    }
                }
                
                result = accPurchaseOrderobj.getLinkedRFQWithPurchaseRequisition(requestParams);
                itr2 = result.getEntityList().iterator();

                while (itr2.hasNext()) {
                    String rfqID = (String) itr2.next();
                    if (!StringUtil.isNullOrEmpty(rfqID)) {

                        /*
                         * Method is used for updating linking information of
                         *
                         * RFQ in linking table linked with
                         * Purchase Requisition
                         */
                        updateLinkingInformationOfPR(rfqID,true);
                    }
                }

            }

            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for Updating Linking Information for Purchase Invoice linked with Debit Note");

            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    
    private void updateLinkingInformationOfPR(String linkNumbers, boolean isRFQ) throws ServiceException {
        List list = null;
        try {
            if (!isRFQ) {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(VendorQuotationDetail.class.getName(), linkNumbers);
                VendorQuotationDetail vqdetail = (VendorQuotationDetail) rdresult.getEntityList().get(0);

                String vqno = vqdetail.getVendorquotation().getQuotationNumber();
                String vqid = vqdetail.getVendorquotation().getID();
                String purchaseRequisitionDetailId = vqdetail.getPurchaseRequisitionDetailsId();

                rdresult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), purchaseRequisitionDetailId);
                PurchaseRequisitionDetail prdetail = (PurchaseRequisitionDetail) rdresult.getEntityList().get(0);

                String prno = prdetail.getPurchaserequisition().getPrNumber();
                String prid = prdetail.getPurchaserequisition().getID();

                KwlReturnObject result = accPurchaseOrderobj.checkEntryForVendorQuotationLinkingTable(vqid, prid);
                list = result.getEntityList();
                if (list == null || list.isEmpty()) {

                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("sourceflag", 0);
                    requestParams.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
                    requestParams.put("linkeddocno", vqno);
                    requestParams.put("docid", prid);
                    requestParams.put("linkeddocid", vqid);
                    result = accPurchaseOrderobj.savePurchaseRequisitionLinking(requestParams);

                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                    requestParams.put("linkeddocno", prno);
                    requestParams.put("docid", vqid);
                    requestParams.put("linkeddocid", prid);
                    result = accPurchaseOrderobj.saveVQLinking(requestParams);
                }
            } else {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(RequestForQuotationDetail.class.getName(), linkNumbers);
                RequestForQuotationDetail rfqqdetail = (RequestForQuotationDetail) rdresult.getEntityList().get(0);

                String rfqno = rfqqdetail.getRequestforquotation().getRfqNumber();
                String rfqid = rfqqdetail.getRequestforquotation().getID();
                String prid = "";
                if(rfqqdetail.getPrid() != null){
                    prid = rfqqdetail.getPrid().getID();
                    rdresult = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), prid);
                    PurchaseRequisition purchaserequisition = (PurchaseRequisition) rdresult.getEntityList().get(0);

                    String prno = purchaserequisition.getPrNumber();


                    KwlReturnObject result = accPurchaseOrderobj.checkEntryForRFQLinkingTable(rfqid, prid);
                    list = result.getEntityList();
                    if (list == null || list.isEmpty()) {

                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        /* Saving linking information of Purchase requisition while linking with RFQ*/
                        requestParams.put("sourceflag", 0);
                        requestParams.put("moduleid", Constants.Acc_RFQ_ModuleId);
                        requestParams.put("linkeddocno", rfqno);
                        requestParams.put("docid", prid);
                        requestParams.put("linkeddocid", rfqid);
                        result = accPurchaseOrderobj.savePurchaseRequisitionLinking(requestParams);

                        /* Saving linking information of RFQ while linking with Purchase requisition*/
                        requestParams.put("sourceflag", 1);
                        requestParams.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                        requestParams.put("linkeddocno", prno);
                        requestParams.put("docid", rfqid);
                        requestParams.put("linkeddocid", prid);
                        result = accPurchaseOrderobj.saveRFQLinking(requestParams);
                    }
                } 
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfPR : " + ex.getMessage(), ex);
        }

    }
    
    /**
     * Description : Method is used to Update Entry in linking information table
     * for Purchase Order & Sales Order If any Purchase Order linked with Sales
     * Order
     *
     * @param <request> :-used to get sub domain for which you want to update
     * entry in linking information table
     * @param <response>:- used to send response
     * @return :JSONObject(contains success & Message whether script is
     * completed or not)
     */
    
    public ModelAndView updatePOLinkingWithSOScript(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();

            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                KwlReturnObject result = accPurchaseOrderobj.getLinkedSOWithPO(requestParams);
                Iterator itr2 = result.getEntityList().iterator();
                while (itr2.hasNext()) {
                    String sodetailID = (String) itr2.next();
                    if (!StringUtil.isNullOrEmpty(sodetailID)) {

                        /*
                         * Method is used for updating linking information of
                         *
                         * Purchase Order & Sales Order in linking table
                         */

                        updateLinkingInformationOfPO(sodetailID);
                    }
                }

            }

            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for Updating Linking Information for Purchase Order linked with Sales Order");

            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private void updateLinkingInformationOfPO(String linkNumbers) throws ServiceException {
        List list = null;
        try {

            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), linkNumbers);
            SalesOrderDetail sodetail = (SalesOrderDetail) rdresult.getEntityList().get(0);

            String salesOrderNo = sodetail.getSalesOrder().getSalesOrderNumber();
            String salesOrderID = sodetail.getSalesOrder().getID();
            String podetailID = sodetail.getPurchaseorderdetailid();

            rdresult = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(), podetailID);
            PurchaseOrderDetail podetail = (PurchaseOrderDetail) rdresult.getEntityList().get(0);

            String purchaseOrderNo = podetail.getPurchaseOrder().getPurchaseOrderNumber();
            String purchaseOrderID = podetail.getPurchaseOrder().getID();



            KwlReturnObject result = accPurchaseOrderobj.checkPOIsPresentInLinkingTable(purchaseOrderID, salesOrderID);
            list = result.getEntityList();
            if (list == null || list.isEmpty()) {

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("sourceflag", 0);
                requestParams.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                requestParams.put("linkeddocno", salesOrderNo);
                requestParams.put("docid", purchaseOrderID);
                requestParams.put("linkeddocid", salesOrderID);
                result = accPurchaseOrderobj.savePOLinking(requestParams);


                requestParams.put("sourceflag", 1);
                requestParams.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
                requestParams.put("linkeddocno", purchaseOrderNo);
                requestParams.put("docid", salesOrderID);
                requestParams.put("linkeddocid", purchaseOrderID);
                result = accSalesOrderDAOobj.saveSalesOrderLinking(requestParams);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfPO : " + ex.getMessage(), ex);
        }

    }
    
    /**
     * Description : Method is used to Update Entry in linking information table
     * for Vendor Quotation & Customer Quotation If any Vendor Quotation linked
     * with Customer Quotation
     *
     * @param <request> :-used to get sub domain for which you want to update
     * entry in linking information table
     * @param <response>:- used to send response
     * @return :JSONObject(contains success & Message whether script is
     * completed or not
     *
     */
    
    public ModelAndView updateVQLinkingWithCQScript(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();

            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                KwlReturnObject result = accPurchaseOrderobj.getLinkedCQWithVQ(requestParams);
                Iterator itr2 = result.getEntityList().iterator();
                while (itr2.hasNext()) {
                    String quotationDetailID = (String) itr2.next();
                    if (!StringUtil.isNullOrEmpty(quotationDetailID)) {

                        /*
                         * Method is used for updating linking information of
                         *
                         * Vendor Quotation & Customer Quotation in linking
                         * table
                         */

                        updateLinkingInformationOfCQ(quotationDetailID);
                    }
                }

            }

            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for Updating Linking Information for Vendor Quotation linked with Customer Quoation");

            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     
    private void updateLinkingInformationOfCQ(String linkNumbers) throws ServiceException {
        List list = null;
        try {

            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(QuotationDetail.class.getName(), linkNumbers);
            QuotationDetail cqdetail = (QuotationDetail) rdresult.getEntityList().get(0);

            String customerQuotationNo = cqdetail.getQuotation().getquotationNumber();
            String customerQuotationID = cqdetail.getQuotation().getID();
            String vqDetailID = cqdetail.getVendorquotationdetails();

            rdresult = accountingHandlerDAOobj.getObject(VendorQuotationDetail.class.getName(), vqDetailID);
            VendorQuotationDetail vqdetail = (VendorQuotationDetail) rdresult.getEntityList().get(0);

            String vendorQuotationNo = vqdetail.getVendorquotation().getQuotationNumber();
            String vendorQuoatationID = vqdetail.getVendorquotation().getID();


            /*
             * checking Entry of VQ in linking table whether it is present or
             * not
             */
            
            KwlReturnObject result = accPurchaseOrderobj.checkEntryForVendorQuotationLinkingTable(vendorQuoatationID, customerQuotationID);
            list = result.getEntityList();
            if (list == null || list.isEmpty()) {

                /*
                 * Insering Entry in Vendor Quotation Linking table
                 */
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("sourceflag", 0);
                requestParams.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                requestParams.put("linkeddocno", customerQuotationNo);
                requestParams.put("docid", vendorQuoatationID);
                requestParams.put("linkeddocid", customerQuotationID);
                result = accPurchaseOrderobj.saveVQLinking(requestParams);


                /*
                 * Insering Entry in Customer Quotation Linking table
                 */
                requestParams.put("sourceflag", 1);
                requestParams.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
                requestParams.put("linkeddocno", vendorQuotationNo);
                requestParams.put("docid", customerQuotationID);
                requestParams.put("linkeddocid", vendorQuoatationID);
                result = accSalesOrderDAOobj.saveQuotationLinking(requestParams);
}

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfCQ : " + ex.getMessage(), ex);
        }

    }
    
        /*
     * function to save Purchase Order Status for Sales Order
     */
    
    public ModelAndView savePurchaseOrderStatusForSO(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        String message = "";
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            String purchaseOrderID = (String) request.getParameter("billid");
            String status = (String) request.getParameter("status");
            String auditStatus = "";

            HashMap requestparams = new HashMap();
            requestparams.put("purchaseOrderID", purchaseOrderID);
            requestparams.put("status", status);

            if (status.equalsIgnoreCase("Open")) {
                auditStatus = " Blocked ";
            } else {
                auditStatus = " Unblocked ";
            }
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), purchaseOrderID);
            PurchaseOrder purchaseorder = (PurchaseOrder) rdresult.getEntityList().get(0);
            String purchaseOrderNo = purchaseorder.getPurchaseOrderNumber();
            requestparams.put("purchaseOrderNo", purchaseOrderNo);
            requestparams.put("locale", RequestContextUtils.getLocale(request));
            KwlReturnObject result = accPurchaseOrderobj.savePurchaseOrderStatusForSO(requestparams);
            message = result.getMsg();
            issuccess = true;
            auditTrailObj.insertAuditLog(AuditAction.PURCHASE_ORDER_BLOCKED_UNBLOCKED, "User " + sessionHandlerImpl.getUserFullName(request) + " has been " + auditStatus + " Purchase Order " + purchaseOrderNo + " For Sales Order ", request, purchaseOrderID);
        } catch (Exception ex) {
            message = "accPurchaseOrderController.savePurchaseOrderStatusForSO:" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", message);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /*
     * function to close Purchase Order manually if it is no longer required
     */
    public ModelAndView closeDocument(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String message = "";
        String companyid = sessionHandlerImpl.getCompanyid(request);
        try {
            String purchaseOrderId = (String) request.getParameter("billid");
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), purchaseOrderId);
            PurchaseOrder purchaseOrder = (PurchaseOrder) rdresult.getEntityList().get(0);
            HashMap requestparams = new HashMap();
            requestparams.put("purchaseOrder", purchaseOrder);
            requestparams.put("closeFlag", true);
            Set<PurchaseOrderDetail> orderDetail = purchaseOrder.getRows();
            boolean isPoAllowToClose = true;

            if (purchaseOrder.getLinkflag() == 1) {//checking PO is used in invoice
                  /* PO is allowing to close if PO is used partially in PI & that PI is used in GR fully or partially*/
                if (purchaseOrder.isIsOpen()) {
                    for (PurchaseOrderDetail purchaseOrderDetail : orderDetail) {
                        KwlReturnObject doresult = accPurchaseOrderobj.checkWhetherPOIsUsedInGROrNot(purchaseOrderDetail.getID(), companyid);
                        List list1 = doresult.getEntityList();
                        if (list1.size() > 0) {
                            isPoAllowToClose = false;
                            break;
                        }
                    }
                    if (isPoAllowToClose) {
                        jobj.put("success", false);
                        jobj.put("msg", messageSource.getMessage("acc.po.pousedonso", null, RequestContextUtils.getLocale(request)));
                        return new ModelAndView("jsonView", "model", jobj.toString());
                    }
                } else {
                      /* If PO is fully used in Invoice then it is not allowing to close*/
                    jobj.put("success", false);
                    jobj.put("msg", messageSource.getMessage("acc.po.pocompletelyusedonso", null, RequestContextUtils.getLocale(request)));
                    return new ModelAndView("jsonView", "model", jobj.toString());
                }
            }
             /* Closing PO*/
            requestparams.put("locale", RequestContextUtils.getLocale(request));
            KwlReturnObject result = accPurchaseOrderobj.closeDocument(requestparams);
            message = result.getMsg();
            issuccess = true;
            auditTrailObj.insertAuditLog(AuditAction.PURCHASE_ORDER_CLOSED_MANUALLY, messageSource.getMessage("acc.field.User", null, RequestContextUtils.getLocale(request)) + sessionHandlerImpl.getUserFullName(request) +" "+messageSource.getMessage("acc.field.hasClosedPurchaseOrder", null, RequestContextUtils.getLocale(request))+" "+purchaseOrder.getPurchaseOrderNumber(),request, purchaseOrder.getID());
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", message);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
     /*
     * function to close Line level product used in Purchase Order manually, if it is no longer required
     */
    public ModelAndView closeLineItem(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String message = "";
        String companyid = sessionHandlerImpl.getCompanyid(request);
        try {
            String poDetailId = (String) request.getParameter("DetailId");
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(), poDetailId);
            PurchaseOrderDetail purchaseOrderDetail = (PurchaseOrderDetail) rdresult.getEntityList().get(0);
            HashMap requestparams = new HashMap();
            requestparams.put("purchaseOrderDetail", purchaseOrderDetail);
            PurchaseOrder purchaseOrder = purchaseOrderDetail.getPurchaseOrder();
            requestparams.put("purchaseOrder", purchaseOrder);
            Set<PurchaseOrderDetail> orderDetail = purchaseOrder.getRows();
            boolean isPoAllowToClose = true;

            if (purchaseOrder.getLinkflag() == 1) {//checking PO is used in invoice
                  /* PO is allowing to close if PO is used partially in PI & that PI is used in GR fully or partially*/
                if (purchaseOrder.isIsOpen()) {
                    for (PurchaseOrderDetail poDetail : orderDetail) {
                        KwlReturnObject doresult = accPurchaseOrderobj.checkWhetherPOIsUsedInGROrNot(poDetail.getID(), companyid);
                        List list1 = doresult.getEntityList();
                        if (list1.size() > 0) {
                            isPoAllowToClose = false;
                            break;
                        }
                    }
                    if (isPoAllowToClose) {
                        jobj.put("success", false);
                        jobj.put("msg", "Purchase Order are used in invoice.So you cannot close it Manually.");
                        return new ModelAndView("jsonView", "model", jobj.toString());
                    }
                } else {
                    /* If PO is fully used in Invoice then it is not allowing to close*/
                    jobj.put("success", false);
                    jobj.put("msg", "Purchase Order are completely used in invoice.So you cannot close it Manually.");
                    return new ModelAndView("jsonView", "model", jobj.toString());
                }
            }
            /* Closing Line item*/
            KwlReturnObject result = accPurchaseOrderobj.closeLineItem(requestparams);
            message = result.getMsg();
            issuccess = true;

            /* Closing PO if all line level item is being closed*/
            boolean isPoClosed = true;
            for (PurchaseOrderDetail poDetail : orderDetail) {

                if (!poDetail.isIsLineItemClosed()) {
                     /*If line level is not closed manually & it is used in transaction then it will no longer load in transaction*/
                    if (poDetail.getBalanceqty() != 0) {
                        isPoClosed = false;
                        break;
                    }

                }
            }
            if (isPoClosed) {
                requestparams.put("closeFlag", true);
                result = accPurchaseOrderobj.closeDocument(requestparams);
               auditTrailObj.insertAuditLog(AuditAction.PURCHASE_ORDER_CLOSED_MANUALLY, messageSource.getMessage("acc.field.User", null, RequestContextUtils.getLocale(request)) + sessionHandlerImpl.getUserFullName(request) +" "+messageSource.getMessage("acc.field.hasClosedPurchaseOrder", null, RequestContextUtils.getLocale(request))+" " +purchaseOrder.getPurchaseOrderNumber(),request, purchaseOrder.getID());
               auditTrailObj.insertAuditLog(AuditAction.PURCHASE_ORDER_CLOSED_MANUALLY, messageSource.getMessage("acc.field.User", null, RequestContextUtils.getLocale(request)) + sessionHandlerImpl.getUserFullName(request)+" "+messageSource.getMessage("acc.field.has", null, RequestContextUtils.getLocale(request))+" "+messageSource.getMessage("acc.field.manually", null, RequestContextUtils.getLocale(request))+" "+messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request))+" "+purchaseOrderDetail.getProduct().getName()+" "+messageSource.getMessage("acc.common.in", null, RequestContextUtils.getLocale(request))+" "+messageSource.getMessage("acc.accPref.autoPO", null, RequestContextUtils.getLocale(request))+" "+purchaseOrder.getPurchaseOrderNumber() ,request, purchaseOrder.getID());
            }else{
                auditTrailObj.insertAuditLog(AuditAction.PURCHASE_ORDER_CLOSED_MANUALLY, messageSource.getMessage("acc.field.User", null, RequestContextUtils.getLocale(request)) + sessionHandlerImpl.getUserFullName(request)+" "+messageSource.getMessage("acc.field.has", null, RequestContextUtils.getLocale(request))+" "+messageSource.getMessage("acc.field.manually", null, RequestContextUtils.getLocale(request))+" "+messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request))+" "+purchaseOrderDetail.getProduct().getName()+" "+messageSource.getMessage("acc.common.in", null, RequestContextUtils.getLocale(request))+" "+messageSource.getMessage("acc.accPref.autoPO", null, RequestContextUtils.getLocale(request))+" "+purchaseOrder.getPurchaseOrderNumber() ,request, purchaseOrder.getID());
            }
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", message);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView importPurchaseOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = getImportPurchaseOrderParams(request);
            jobj = accPurchaseOrderModuleServiceObj.importPurchaseOrderJSON(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject getImportPurchaseOrderParams(HttpServletRequest request) throws JSONException, SessionExpiredException {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put("servletContext", this.getServletContext());
        return paramJobj;
    }
    public ModelAndView importVendorQuotations(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            /* Get Import related global parameters */
            JSONObject paramJobj = getVendorQuotationsParams(request);
            /* Call validate and import data of VQ. */
            jobj = accPurchaseOrderModuleServiceObj.importVendorQuotationJSON(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
     public JSONObject getVendorQuotationsParams(HttpServletRequest request) throws JSONException, SessionExpiredException {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put("servletContext", this.getServletContext());
        return paramJobj;
    }
}
