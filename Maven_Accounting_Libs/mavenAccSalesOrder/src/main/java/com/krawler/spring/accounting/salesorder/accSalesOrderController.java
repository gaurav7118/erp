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
package com.krawler.spring.accounting.salesorder;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.ServerEventManager;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignLineItemProp;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.CommonFnControllerService;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
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
public class accSalesOrderController extends MultiActionController implements MessageSourceAware {
    
    private HibernateTransactionManager txnManager;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    private fieldDataManager fieldDataManagercntrl;
    private accProductDAO accProductObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private VelocityEngine velocityEngine;
    private accSalesOrderService accSalesOrderServiceobj;
    private ImportHandler importHandler;
    String recId = "";
    String tranID = "";
    private CommonFnControllerService commonFnControllerService;
    
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    
    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }
    
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    
    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
    
    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }
    
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    
    public String getSuccessView() {
        return successView;
    }
    
    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    
    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    
    public void setvelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public void setAccSalesOrderServiceobj(accSalesOrderService accSalesOrderServiceobj) {
        this.accSalesOrderServiceobj = accSalesOrderServiceobj;
    }
    
    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }
    
    public void setCommonFnControllerService(CommonFnControllerService commonFnControllerService) {
        this.commonFnControllerService = commonFnControllerService;
    }
    
    public ModelAndView saveSalesOrder(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String channelName = "";
        try {
            
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            paramJobj.put("baseUrl", baseUrl);
            String userName = sessionHandlerImpl.getUserFullName(request);
            paramJobj.put(Constants.username,userName);
            jobj = accSalesOrderServiceobj.saveSalesOrderJSON(paramJobj);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
            channelName = jobj.optString(Constants.channelName, null);
            return new ModelAndView("jsonView", "model", jobj.toString());
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
 
    public ModelAndView saveSalesOrderLinking(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            paramJobj.put("baseUrl", baseUrl);
            String userName = sessionHandlerImpl.getUserFullName(request);
            paramJobj.put(Constants.username, userName);
            jobj = accSalesOrderServiceobj.saveSalesOrderLinkingJSON(paramJobj);
            issuccess = true;

        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = true;
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    private HashSet<SalesOrderDetail> updateSalesOrderRows(String soDetails,int moduleid, String companyid) throws ServiceException, JSONException {
        HashSet<SalesOrderDetail> rows = new HashSet<>();
        try {
            JSONArray jArr = new JSONArray(soDetails);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                SalesOrderDetail row = null;
                String linktype = jobj.getString("linktype");
                String linkid = jobj.getString("linkid");
                String linkto = jobj.getString("linkto");
                
                if (jobj.has("rowid")) {
                    KwlReturnObject invDetailsResult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), StringUtil.isNullOrEmpty(linkto)?jobj.getString("rowid"):jobj.getString("savedrowid"));
                    row = (SalesOrderDetail) invDetailsResult.getEntityList().get(0);
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
                        HashMap<String, Object> SOMap = new HashMap<>();
                        JSONArray jcustomarray = new JSONArray(customfield);

                        HashMap<String, Object> customrequestParams = new HashMap<>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "SalesorderDetail");
                        customrequestParams.put("moduleprimarykey", "SoDetailID");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put(Constants.moduleid, moduleid);
                        customrequestParams.put(Constants.companyKey, companyid);
                        SOMap.put(Constants.Acc_id, row.getID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_SalesOrderDetails_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            SOMap.put("salesordercustomdataref", row.getID());
                            accSalesOrderDAOobj.saveSalesOrderDetails(SOMap);
                        }
                    }

                    // Add Custom fields details for Product
                    if (!StringUtil.isNullOrEmpty(jobj.optString("productcustomfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("productcustomfield", "[]"));
                        HashMap<String, Object> customrequestParams = new HashMap<>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "SalesorderDetail");
                        customrequestParams.put("moduleprimarykey", "SoDetailID");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                        customrequestParams.put("recdetailId", row.getID());
                        customrequestParams.put("productId", row.getProduct().getID());
                        customrequestParams.put(Constants.companyKey, companyid);
                        customrequestParams.put("customdataclasspath", Constants.Acc_SODETAIL_Productcustom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    }
                    rows.add(row);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updateInvoiceRows : " + ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
        return rows;
    }

    public List updateSalesOrder(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {
        String id = null;
        List ll = new ArrayList();
        ArrayList discountArr = new ArrayList();
        String soid = null;
        SalesOrder so = null;
        SalesOrder salesOrder = null;
        try {
//            DateFormat userdf = authHandler.getUserDateFormatter(request);
//            DateFormat df = authHandler.getDateFormatter(request);
            DateFormat df = authHandler.getDateOnlyFormat();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            soid = request.getParameter("invoiceid");
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
            boolean isConsignment = (!StringUtil.isNullOrEmpty(request.getParameter("isConsignment"))) ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
            boolean isEdit = true;
            boolean isCopy = false;
            boolean isLinkedTransaction = !StringUtil.isNullOrEmpty(request.getParameter("isLinkedTransaction") )? StringUtil.getBoolean(request.getParameter("isLinkedTransaction")) : false;
            boolean islockQuantity = !StringUtil.isNullOrEmpty(request.getParameter("islockQuantity") ) ? StringUtil.getBoolean(request.getParameter("islockQuantity")) : false;
            int moduleid = isConsignment ? Constants.Acc_ConsignmentRequest_ModuleId : isLeaseFixedAsset?Constants.Acc_Lease_Order_ModuleId:Constants.Acc_Sales_Order_ModuleId;
            String customfield = request.getParameter("customfield");
            /*
             * To update the following items which is not affecting the amount
             * and linking of the Invoice.
             */
            HashMap<String, Object> soPrmt = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(soid)) {
                soPrmt.put(Constants.Acc_id, soid); 
            }
            soPrmt.put("orderdate", df.parse(request.getParameter("billdate")));
            soPrmt.put("memo", request.getParameter("memo") == null ? "" : request.getParameter("memo"));
            soPrmt.put("billto", request.getParameter("billto") == null ? "" : request.getParameter("billto"));
            soPrmt.put("shipaddress", request.getParameter("shipaddress") == null ? "" : request.getParameter("shipaddress"));
            if (request.getParameter("shipdate") != null && !StringUtil.isNullOrEmpty(request.getParameter("shipdate"))) {
                soPrmt.put("shipdate", df.parse(request.getParameter("shipdate")));
            }
            soPrmt.put("customerporefno", request.getParameter("customerporefno") == null ? "" : request.getParameter("customerporefno"));
            soPrmt.put(Constants.companyKey, companyid);
            soPrmt.put("salesPerson", request.getParameter("salesPerson") == null ? "" : request.getParameter("salesPerson"));
            soPrmt.put("shipvia", request.getParameter("shipvia"));
            soPrmt.put("fob", request.getParameter("fob") == null ? "" : request.getParameter("fob"));
            soPrmt.put("modifiedby", sessionHandlerImpl.getUserid(request));
            soPrmt.put("updatedon", System.currentTimeMillis());
            soPrmt.put("posttext", request.getParameter("posttext"));
            soPrmt.put("costCenterId", request.getParameter("costcenter"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("deliveryTime"))) {
                soPrmt.put("deliveryTime", request.getParameter("deliveryTime"));
            }

            KwlReturnObject invResult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soid);
            so = (SalesOrder) invResult.getEntityList().get(0);

            Map<String, Object> addressParams = new HashMap<>();
            String billingAddress = request.getParameter(Constants.BILLING_ADDRESS);
            if (!StringUtil.isNullOrEmpty(billingAddress)) {
                addressParams = AccountingAddressManager.getAddressParams(request, false);
            } else {
                addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(so.getCustomer().getID(), companyid, accountingHandlerDAOobj);// addressParams = getCustomerDefaultAddressParams(customer,companyid);
            }
            BillingShippingAddresses bsa = so.getBillingShippingAddresses();//used to update billing shipping addresses
            addressParams.put(Constants.Acc_id, bsa != null ? bsa.getID() : "");
            KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
            bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
            String addressid = bsa.getID();
            soPrmt.put("billshipAddressid", addressid);
            /*
             * Updating line item information.
             */
            String soDetails = request.getParameter("detail");
            HashSet<SalesOrderDetail> sodetails = updateSalesOrderRows(soDetails,moduleid, companyid);
            soPrmt.put("sodetails", sodetails);
            /*
             * Updating Custom field data.
             */
            
            if (!StringUtil.isNullOrEmpty(customfield)) {
                HashMap<String, Object> SOMap = new HashMap<>();
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "SalesOrder");
                customrequestParams.put("moduleprimarykey", "SoID");
                customrequestParams.put("modulerecid", so.getID());
                customrequestParams.put(Constants.moduleid, isConsignment ? Constants.Acc_ConsignmentRequest_ModuleId : isLeaseFixedAsset?Constants.Acc_Lease_Order_ModuleId:Constants.Acc_Sales_Order_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                SOMap.put(Constants.Acc_id, so.getID());
                SOMap.put("isOpeningBalanceOrder", so.isIsOpeningBalanceSO());
                SOMap.put(Constants.companyKey, companyid);
                SOMap.put("orderdate", df.parse(request.getParameter("billdate")));
                SOMap.put("isEdit", isEdit);
                SOMap.put("islockQuantity", islockQuantity);
                SOMap.put("isLinkedTransaction", isLinkedTransaction);
                SOMap.put("isCopy", isCopy);
                
                customrequestParams.put("customdataclasspath", Constants.Acc_SalesOrder_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    SOMap.put("salesordercustomdataref", so.getID());
                    accSalesOrderDAOobj.saveSalesOrder(SOMap);
                }
            }
            String disablePOForSO = request.getParameter("blockSOPO") != null ? request.getParameter("blockSOPO") : "";
            boolean isBlockDocument = false;
            if (!StringUtil.isNullOrEmpty(disablePOForSO) && disablePOForSO.equalsIgnoreCase("on")) {
                isBlockDocument = true;
            }
            soPrmt.put("isLinkedPOBlocked", isBlockDocument);
            soPrmt.put("isEdit", isEdit);
            soPrmt.put("islockQuantity", islockQuantity);
            soPrmt.put("isLinkedTransaction", isLinkedTransaction);
            soPrmt.put("isCopy", isCopy);
            KwlReturnObject result = accSalesOrderDAOobj.saveSalesOrder(soPrmt);
            salesOrder = (SalesOrder) result.getEntityList().get(0);//Create Invoice without invoice-details.
            id = salesOrder.getID();
            /*
             * Data for return information.
             */
            String personalid = salesOrder.getCustomer().getAccount().getID();
            String accname = salesOrder.getCustomer().getAccount().getName();
            String salesOrderNo = salesOrder.getSalesOrderNumber();
            String address = salesOrder.getCustomer().getBillingAddress();
            String fullShippingAddress = "";
            if (salesOrder.getBillingShippingAddresses() != null) {
                fullShippingAddress = salesOrder.getBillingShippingAddresses().getFullShippingAddress();
            }
            tranID = id;
            recId = salesOrderNo;
            ll.add(new String[]{id, ""});
            ll.add(discountArr);
            ll.add((salesOrder.getPendingapproval() == 1) ? "Pending Approval" : "Approved");
            ll.add(personalid);
            ll.add(accname);
            ll.add(salesOrderNo);
            ll.add(address);
            ll.add(salesOrder.getTotalamount());
            ll.add("");
            ll.add(fullShippingAddress);
        } catch (ParseException | JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }catch (AccountingException ex) {
            throw new AccountingException(ex.getMessage());
        }
        catch (Exception e) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
        return ll;
    }

    public ModelAndView updateLinkedSalesOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isAccountingExe = false;
        String msg = "", channelName = "", invoiceid = "", deliveryOid = "", billNo = "", doinvflag = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        status = txnManager.getTransaction(def);    //Please asked about this to sagar sir
        try {
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean isConsignment = request.getParameter("isConsignment") != null ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
            boolean isJobWorkOrderReciever = request.getParameter("isJobWorkOrderReciever") != null ? Boolean.parseBoolean(request.getParameter("isJobWorkOrderReciever")) : false;
            boolean iscash = StringUtil.isNullOrEmpty(request.getParameter("incash")) ? false : Boolean.parseBoolean(request.getParameter("incash"));
            List li = updateSalesOrder(request);
            String[] id = (String[]) li.get(0);
            String billno = (String) li.get(5);
            String jeNumber = (String) li.get(8);
            invoiceid = (String)id[0];
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
             if (isLeaseFixedAsset) {
                channelName = "/LeaseOrderReport/gridAutoRefresh";
            } else if (!(isLeaseFixedAsset || isConsignment)) {//For normal SO
                channelName = "/SalesOrderReport/gridAutoRefresh";
            } 
            /*
             * Composing the message to display after save operation.
             */
            if (isLeaseFixedAsset) {
                msg = messageSource.getMessage("acc.lso.update", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";   //"Sales order has been saved successfully";
            } else if (isConsignment) {
                msg = messageSource.getMessage("acc.consignment.order.update", null, RequestContextUtils.getLocale(request)) +"<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";   //"consignment saved successfully";
            } else if (isJobWorkOrderReciever) {
                msg = messageSource.getMessage("acc.jwo.update", null, RequestContextUtils.getLocale(request)) +"<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";   //"Job Work Order updated successfully";
            } else {
                msg = messageSource.getMessage("acc.so.update", null, RequestContextUtils.getLocale(request));
                msg += "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";
            }
            /*
             * Composing the message to insert into Audit Trail.
             */
            String action = "updated";
            if (isLeaseFixedAsset) {
                action += " Lease";
            }
            auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + (isConsignment ? " Consignment Sales Order " : " Sales Order ") + recId, request, tranID);

        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
         //   isAccountingExe = true;
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
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
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public void updateBatchDetailsForSO(String productId,HttpServletRequest request, String sodetailsId) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {

        KwlReturnObject kmsg = null;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userid = sessionHandlerImpl.getUserid(request);
        boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));        

        if (!StringUtil.isNullOrEmpty(productId)) {
            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), productId);
            Product product = (Product) prodresult.getEntityList().get(0);
            isLocationForProduct = product.isIslocationforproduct();
            isWarehouseForProduct = product.isIswarehouseforproduct();
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
        }
    int cnt = 0;
    HashMap<Integer, Object[]> BatchdetalisMap = new HashMap<Integer, Object[]>();
    if (isLocationForProduct && isWarehouseForProduct && isBatchForProduct && isSerialForProduct) {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        kmsg = accCommonTablesDAO.getBatchSerialDetailsforProduct(productId, isSerialForProduct,isEdit, paramJobj);
        List<Object[]> batchList = kmsg.getEntityList();
        for (Object[] ObjBatchrow:batchList) {
            BatchdetalisMap.put(cnt++, ObjBatchrow);
        }
    }

    int batchcnt = 0;
    int allocatedcnt = 0;
    boolean isquantityNotavl=false;  //this flag is used to check whether serial batch quantity is avilabale 
       if (!StringUtil.isNullOrEmpty(sodetailsId)) {
          KwlReturnObject sodresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), sodetailsId);
          SalesOrderDetail sodetail = (SalesOrderDetail) sodresult.getEntityList().get(0);
          int cntp = (int) sodetail.getLockquantitydue();
          for (int i = 0; i < cntp; i++) {
              Object[] objArr = BatchdetalisMap.get(batchcnt++);
                
                if(objArr!=null){
                String serialId=objArr[0] != null ? (String) objArr[0] : "";
                String batchId=objArr[1] != null ? (String) objArr[1] : "";
                String mfgdate=objArr[3] != null ? (String) objArr[3] : "";
                String expdate=objArr[4] != null ? (String) objArr[4] : "";
                
                if(!StringUtil.isNullOrEmpty(sodetailsId) && !StringUtil.isNullOrEmpty(batchId) && !StringUtil.isNullOrEmpty(serialId) ){
                    KwlReturnObject pbdresult = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), batchId);
                    NewProductBatch pbdetail = (NewProductBatch) pbdresult.getEntityList().get(0);
                    String locationid= pbdetail.getLocation()!= null ? pbdetail.getLocation().getId():"";
                    String warehouseid= pbdetail.getWarehouse()!= null ? pbdetail.getWarehouse().getId():"";
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("requestorid", userid);
                    requestParams.put(Constants.companyKey, companyid);
                    requestParams.put("warehouse", warehouseid);
                    requestParams.put("location", locationid);
                   
                    HashMap<String, Object> documentMap = new HashMap<String, Object>();
                    documentMap.put("quantity","1");
                    documentMap.put("documentid", sodetailsId);
                    documentMap.put("transactiontype", "20");//This is SO Type Tranction   sales order moduleid
                    if (!StringUtil.isNullOrEmpty(mfgdate)) {
                        documentMap.put("mfgdate", df.parse(mfgdate));
                    }
                    if (!StringUtil.isNullOrEmpty(expdate)) {
                        documentMap.put("expdate", df.parse(expdate));
                    }
                    documentMap.put("batchmapid", batchId);
                    accCommonTablesDAO.saveBatchDocumentMapping(documentMap);

                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                    batchUpdateQtyMap.put(Constants.Acc_id, batchId);
                    batchUpdateQtyMap.put("lockquantity","1");
                    accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
                    
                    HashMap<String, Object> serialdocumentMap = new HashMap<String, Object>();
                    serialdocumentMap.put("quantity", "1");
                    serialdocumentMap.put("documentid", sodetailsId);
                     // saving serial details
                    if (!StringUtil.isNullOrEmpty(mfgdate)) {
                        serialdocumentMap.put("mfgdate", df.parse(mfgdate));
                    }
                    if (!StringUtil.isNullOrEmpty(expdate)) {
                        serialdocumentMap.put("expdate", df.parse(expdate));
                    }
//                     HashMap<String, Object> documentMap = new HashMap<String, Object>();
                //code to Apply Pending Approval Rule
                KwlReturnObject ruleResult = accMasterItemsDAOobj.CheckRuleForPendingApproval(requestParams);
                Iterator itr = ruleResult.getEntityList().iterator();
                Set<User> approverSet = null;
                boolean isRequestPending = false;
                while (itr.hasNext()) {
                    ConsignmentRequestApprovalRule approvalRule = (ConsignmentRequestApprovalRule) itr.next();
                    if (approvalRule != null) { 
                        KwlReturnObject res = accSalesOrderDAOobj.getConsignmentRequestApproverList(approvalRule.getID());
                        List<User> userlist = res.getEntityList();
                        Set<User> users=new HashSet<User>();;
                        for (User  user: userlist ) {
                               users.add(user);
                        }
                        approverSet = users;
                        isRequestPending = true;
                        break;
                    }
                }
                if (isRequestPending) {
                    serialdocumentMap.put("requestpendingapproval", RequestApprovalStatus.PENDING);
                    serialdocumentMap.put("approver", approverSet);
                }
                    serialdocumentMap.put("serialmapid", serialId);
                    serialdocumentMap.put("transactiontype", "20");//This is so Type Tranction  
                    accCommonTablesDAO.saveSerialDocumentMapping(serialdocumentMap);

                    HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                    serialUpdateQtyMap.put("lockquantity", "1");
                    serialUpdateQtyMap.put(Constants.Acc_id, serialId);
                    accCommonTablesDAO.saveSerialAmountDue(serialUpdateQtyMap);

               }
                allocatedcnt++;
            }else{
                    isquantityNotavl=true;  //if quantity is not vailable then break and come out of for loop
                    break;
                }
          }
          sodetail.setLockquantitydue(sodetail.getLockquantitydue()-allocatedcnt);
        }
    }
    
    public ModelAndView updateCQScriptForSO(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject companyList = accSalesOrderDAOobj.getCompanyList();
            Iterator citr = companyList.getEntityList().iterator();
            while (citr.hasNext()) {
                String company = (String) citr.next();
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.companyKey, company);
                requestParams.put("linkflag", "2");
                KwlReturnObject result = accSalesOrderDAOobj.getQuotationsForCQScript(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    String linkNumbers = (String) itr.next();
                    if (!StringUtil.isNullOrEmpty(linkNumbers)) {
                       accSalesOrderServiceobj.updateOpenStatusFlagForSO(linkNumbers);
                    }
                }
            }
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_msg, "Script completed update the isOpen Flag in CQ");
                jobj.put(Constants.RES_success, issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView saveConsignmentApprovalRules(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String locationName = "";
        String approverName = "";
        String auditMsg = "";
        int isDuplicateflag = -1;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String id = request.getParameter(Constants.Acc_id);
            String warehouseid = request.getParameter("warehouse");
            String locations = request.getParameter("locations");
            String ruleName = request.getParameter("ruleName");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userid = sessionHandlerImpl.getUserid(request);
            String requestorid = request.getParameter("requestor");
            boolean isrequestapproval =Boolean.parseBoolean((String) request.getParameter("isrequestapproval"));
            String approverids = request.getParameter("approver");
            
            isDuplicateflag = accSalesOrderDAOobj.checkConsignmentApprovalRules(id, requestorid, warehouseid, locations, approverids, companyid);
            if (isDuplicateflag == 1 || isDuplicateflag == -1) {
                KwlReturnObject coresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) coresult.getEntityList().get(0);
                
                coresult = accountingHandlerDAOobj.getObject(User.class.getName(), requestorid);
                User requestor = (User) coresult.getEntityList().get(0);
                
              
                               
                coresult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
                User creator = (User) coresult.getEntityList().get(0);
                
                coresult = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), warehouseid);
                InventoryWarehouse warehouse = (InventoryWarehouse) coresult.getEntityList().get(0);
                
                long createdon = System.currentTimeMillis();
                long updatedon = System.currentTimeMillis();
                
                ConsignmentRequestApprovalRule approvalRule = new ConsignmentRequestApprovalRule();
                if (!StringUtil.isNullOrEmpty(id)) {
                    KwlReturnObject result1 = accountingHandlerDAOobj.getObject(ConsignmentRequestApprovalRule.class.getName(), id);
                    approvalRule = (ConsignmentRequestApprovalRule) result1.getEntityList().get(0);
                }
//                approvalRule.setApproverSet(approverSet);
                if(isrequestapproval){
                    approvalRule.setRequester(requestor);
                    approvalRule.setApprovalType(ApprovalType.REQUEST);
                }else{
                    approvalRule.setApprovalType(ApprovalType.QAAPPROVAL);
                }
                
                approvalRule.setCompany(company);
                approvalRule.setInventoryWarehouse(warehouse);
                approvalRule.setRuleName(ruleName);
                approvalRule.setCreatedon(createdon);
                approvalRule.setUpdatedon(updatedon);
                approvalRule.setCreatedby(creator);
                approvalRule.setModifiedby(creator);
                
                accSalesOrderDAOobj.saveConsignmentApprovalRules(approvalRule, id, locations); 
                //Delete Existing User set for Perticular rule if exsit        
                accSalesOrderDAOobj.deleteConsignmentRequestApproverMapping(approvalRule.getID());        
                //inserting new entries for rules
                if (!StringUtil.isNullOrEmpty(approverids)) {
                    String[] approveridArr = approverids.split(",");
                    for (String approverid : approveridArr) {
                        coresult = accountingHandlerDAOobj.getObject(User.class.getName(), approverid);
                        User approver = (User) coresult.getEntityList().get(0);
                        ConsignmentRequestApproverMapping approverMapping=new ConsignmentRequestApproverMapping();
                        approverMapping.setApprover(approver);
                        approverMapping.setConsignmentRequestRule(approvalRule);
                        accSalesOrderDAOobj.UpdateObject(approverMapping);
                        approverName += approver.getFullName() + ",";
                    }
                }
               
                String[] locationids = locations.split(",");
                if (!StringUtil.isNullOrEmpty(locations)) {
                    for (String locationid : locationids) {
                        coresult = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), locationid);
                        InventoryLocation inventoryLocation = (InventoryLocation) coresult.getEntityList().get(0);
                        locationName += inventoryLocation.getName() + ",";
                    }
                }
                locationName = locationName.substring(0, locationName.length() - 1);
                approverName = approverName.substring(0, approverName.length() - 1);
                String auditID = AuditAction.APPROVAL_RULE;
                if(!StringUtil.isNullOrEmpty(requestorid)){
                    auditMsg="User " + sessionHandlerImpl.getUserFullName(request) + " has set Consignment Approval Rule for Requestor : " + requestor.getFullName() + ",Store : " + warehouse.getName() + ", Locations : (" + locationName + "), Approver : (" + approverName + ")";
                }else{
                     auditMsg="User " + sessionHandlerImpl.getUserFullName(request) + " has set QA Approval Rule for Store : " + warehouse.getName() + ", Locations : (" + locationName + "), Approver : (" + approverName + ")";
                }
                auditTrailObj.insertAuditLog(auditID, auditMsg, request, id);
                msg = messageSource.getMessage("acc.field.acc.field.ApprovalRulehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else if (isDuplicateflag == 0) {
                msg = messageSource.getMessage("acc.field.ConsignmentRequestApprovalDuplicateMsg", null, RequestContextUtils.getLocale(request));
            } else if (isDuplicateflag == 3) {
                msg = messageSource.getMessage("acc.field.acc.field.ConsignmentRuleWarningMsg", null, RequestContextUtils.getLocale(request));
            }
            issuccess = true;
            txnManager.commit(status);
            
            
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("isDuplicateflag", isDuplicateflag);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getConsignmentApprovalRules(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            
            boolean isrequestapproval =Boolean.parseBoolean((String) request.getParameter("isrequestapproval"));
            if(isrequestapproval){
                 filter_names.add("approvalType");
                 filter_params.add(ApprovalType.REQUEST);
            }else{
                filter_names.add("approvalType");
                filter_params.add(ApprovalType.QAAPPROVAL);
            }
            filter_names.add("company.companyID");
            filter_params.add(companyid);
            requestParams.put(Constants.filterNamesKey, filter_names);
            requestParams.put(Constants.filterParamsKey, filter_params);
            
            KwlReturnObject result = accSalesOrderDAOobj.getConsignmentApprovalRules(requestParams);
            List<ConsignmentRequestApprovalRule> list = result.getEntityList();
            int count = result.getRecordTotalCount();
            
            JSONArray jSONArray = new JSONArray();
            for (ConsignmentRequestApprovalRule consignmentRequestApprovalRule : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put(Constants.Acc_id, consignmentRequestApprovalRule.getID());
                if(isrequestapproval){
                    jSONObject.put("requestorid", consignmentRequestApprovalRule.getRequester().getUserID());
                    jSONObject.put("requestor", consignmentRequestApprovalRule.getRequester().getFullName());
                }else{
                    jSONObject.put("requestorid","");
                    jSONObject.put("requestor", "");
                }
                jSONObject.put("storeid", consignmentRequestApprovalRule.getInventoryWarehouse().getId());
                KwlReturnObject kwlobject = accountingHandlerDAOobj.getObject(Store.class.getName(), consignmentRequestApprovalRule.getInventoryWarehouse().getId());
                Store store = (Store) kwlobject.getEntityList().get(0);
                if (store != null) {
                    jSONObject.put("store", store.getFullName());
                }
//                jSONObject.put("store", consignmentRequestApprovalRule.getInventoryWarehouse().getName());
                
                filter_names.clear();
                filter_names.add("consignmentrequest.ID");
                filter_params.clear();
                filter_params.add(consignmentRequestApprovalRule.getID());
                requestParams.clear();
                requestParams.put(Constants.filterNamesKey, filter_names);
                requestParams.put(Constants.filterParamsKey, filter_params);
                KwlReturnObject res = accSalesOrderDAOobj.getConsignmentRequestLocationMapping(requestParams);
                List<ConsignmentRequestLocationMapping> ll = res.getEntityList();
                String locationids = "";
                String locations = "";
                for (ConsignmentRequestLocationMapping requestLocationMapping : ll) {
                    locationids += requestLocationMapping.getInventorylocation().getId() + ",";
                    locations += requestLocationMapping.getInventorylocation().getName() + ",";
                }
                if (!StringUtil.isNullOrEmpty(locationids) && !StringUtil.isNullOrEmpty(locations)) {
                    locationids = locationids.substring(0, locationids.length() - 1);
                    locations = locations.substring(0, locations.length() - 1);
                }
                        
                 res = accSalesOrderDAOobj.getConsignmentRequestApproverList(consignmentRequestApprovalRule.getID());
                List<User> userList = res.getEntityList();
                String approverids = "";
                String approvers = "";
                for (User  user: userList ) {
                    if (StringUtil.isNullOrEmpty(approverids)) {
                        approverids = user.getUserID();
                        approvers = user.getFullName();
                    }else{
                        approverids += ","+user.getUserID();
                        approvers += ","+user.getFullName();
                    }
                }
                jSONObject.put("locationid", locationids);
                jSONObject.put("location", locations);
                jSONObject.put("approverid", approverids);
                jSONObject.put("approver", approvers);
                
                jSONArray.put(jSONObject);
            }
            jobj.put(Constants.RES_data, jSONArray);
            jobj.put(Constants.RES_count, count);
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    public ModelAndView deleteConsignmentApprovalRules(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        KwlReturnObject result = null;
        boolean issuccess = false;
        String auditMsg = "";
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String id = request.getParameter(Constants.Acc_id);
            KwlReturnObject result1 = null;
            ConsignmentRequestApprovalRule approvalRule = new ConsignmentRequestApprovalRule();
            if (!StringUtil.isNullOrEmpty(id)) {
                result1 = accountingHandlerDAOobj.getObject(ConsignmentRequestApprovalRule.class.getName(), id);
                approvalRule = (ConsignmentRequestApprovalRule) result1.getEntityList().get(0);
            }
            /*
             * Get the Approval rule data for the audit trail entry ERP-33279
             */
            if (approvalRule != null) {
                Set<InventoryLocation> ll = approvalRule.getInventoryLocationsSet();
                String locationName = "";
                for (InventoryLocation requestLocationMapping : ll) {
                    locationName += requestLocationMapping.getName() + ",";
                }
                locationName = locationName.substring(0, locationName.length() - 1);
                String requestor = (approvalRule.getRequester() != null && !StringUtil.isNullOrEmpty(approvalRule.getRequester().getFullName())) ? approvalRule.getRequester().getFullName() : "";
                String warehouse = (approvalRule.getInventoryWarehouse() != null && !StringUtil.isNullOrEmpty(approvalRule.getInventoryWarehouse().getName())) ? approvalRule.getInventoryWarehouse().getName() : "";
                String approverName = (approvalRule.getCreatedby() != null && !StringUtil.isNullOrEmpty(approvalRule.getCreatedby().getFullName())) ? approvalRule.getCreatedby().getFullName() : "";

                result = accSalesOrderDAOobj.deleteConsignmentApprovalRules(companyid, id);
                issuccess = result.isSuccessFlag();
                msg = messageSource.getMessage("delete.consignmentApprovalRules.successfully", null, RequestContextUtils.getLocale(request));
                String auditID = AuditAction.DELETE_CONSIGNMENTAPPROVALRULE;
                if (!StringUtil.isNullOrEmpty(id)) {
                    auditMsg = messageSource.getMessage("acc.field.User", null, RequestContextUtils.getLocale(request)) + requestor + " " + messageSource.getMessage("acc.consignmentApprovalRules.deletedSuccessfully", null, RequestContextUtils.getLocale(request)) + " " + requestor + ",Store : " + warehouse + ", Locations : (" + locationName + "), Approver : (" + approverName + ")";
                }
                auditTrailObj.insertAuditLog(auditID, auditMsg, request, id);
//            auditTrailObj.insertAuditLog(AuditAction.DELETE_CONSIGNMENTAPPROVALRULE, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted approval rule ", request, id);
            }
            } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    
    public ModelAndView saveRepeateSalesOrderInfo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String detail = "";
        String repeateid = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            GregorianCalendar gc = new GregorianCalendar(); //It returns actual Date object 
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();  //Map for notification mail data
            String loginUserId = sessionHandlerImpl.getUserid(request);
            requestParams.put("loginUserId", loginUserId);
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            int intervalUnit = Integer.parseInt(request.getParameter("interval"));
            dataMap.put("intervalUnit", intervalUnit);
            boolean isActivate = StringUtil.isNullOrEmpty(request.getParameter("isactivate"))?true:Boolean.parseBoolean(request.getParameter("isactivate"));
            int NoOfpost = Integer.parseInt(request.getParameter("NoOfpost"));
            dataMap.put("NoOfpost", NoOfpost);
            dataMap.put("intervalType", request.getParameter("intervalType"));
            Date startDate = df.parse(request.getParameter("startDate"));
            Date expireDate = df.parse(request.getParameter("expireDate"));
            DateFormat datef=authHandler.getDateOnlyFormat();
              //By default every recurring SO ll be considered as Pending for approval. So if user do recurring by mistake, he need not to worry about it. SO ll recur only on his approval            
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isedit")) ? false : Boolean.parseBoolean(request.getParameter("isedit"));
            if (!isEdit) {
                //boolean ispendingapproval = true;
                String approver = "";
                int notifyme = StringUtil.isNullOrEmpty(request.getParameter("notifyme")) ? 1 : Integer.parseInt(request.getParameter("notifyme"));
                if (notifyme == 1) {  // 0 : Auto Recurring, 1: Pending Recurring JE
                    dataMap.put("isactivate", false);
                    approver = !StringUtil.isNullOrEmpty(request.getParameter("approver")) ? request.getParameter("approver") : "";
                    dataMap.put("approver", approver);
                    dataMap.put("ispendingapproval", true);    //1: Pending Recurring JE
                    requestParams.put("ispendingapproval", true);
                } else {    //Auto Entry
                    dataMap.put("approver", approver);
                    dataMap.put("isactivate", isActivate);  //isActivate=true means recurring invoice is in active mode.                    
                    dataMap.put("ispendingapproval", false);
                    requestParams.put("ispendingapproval", false);
                }
            }
            
            String repeateId = request.getParameter("repeateid");
            if (StringUtil.isNullOrEmpty(repeateId)) {
                dataMap.put("startDate", startDate);
                dataMap.put("nextDate", startDate);
                requestParams.put("nextDate", startDate);
                gc.setTime(startDate);
            } else {
                dataMap.put(Constants.Acc_id, repeateId);
                Date nextDate = startDate;//RepeatedSalesOrder.calculateNextDate(startDate, intervalUnit, request.getParameter("intervalType"));//SDP-9651 on every edit the next date and start were getting incremented by one
                dataMap.put("nextDate", nextDate);
                requestParams.put("nextDate", nextDate);
                gc.setTime(nextDate);
            }
            
            gc.add(Calendar.DAY_OF_YEAR, -1);
            Date prevDate = gc.getTime();
            String date=datef.format(prevDate);
            try{
                prevDate=datef.parse(date);
            }catch(ParseException ex){
                prevDate = gc.getTime();
            }
            dataMap.put("prevDate", prevDate);
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("expireDate"))) {
                dataMap.put("expireDate", df.parse(request.getParameter("expireDate")));
                requestParams.put("prevDate", prevDate);
            }
            
            String auditMessage = "";
            boolean isEditFlag = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            /* preparing Audit trial entry for Edit Case*/
            if (!StringUtil.isNullOrEmpty(repeateId) && isEditFlag) {
                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
                HashMap<String, Object> oldsoMap = new HashMap<String, Object>();
                HashMap<String, Object> newAuditKey = new HashMap<String, Object>();
                KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(RepeatedSalesOrder.class.getName(), repeateId);
                RepeatedSalesOrder rSalesOrder = (RepeatedSalesOrder) CQObj.getEntityList().get(0);

                accSalesOrderServiceobj.setValuesForAuditTrialForRecurringSO(rSalesOrder, oldsoMap, newAuditKey);
                String startDate1=dateformat.format(startDate);
                String endDate1=dateformat.format(expireDate);
                dataMap.put("NoOfpost", NoOfpost + "[" + endDate1 + "]");
                dataMap.put("intervalUnit", intervalUnit + "[" + request.getParameter("intervalType") + "]");
                dataMap.put("startDate", startDate1);
                auditMessage = AccountingManager.BuildAuditTrialMessage(dataMap, oldsoMap, 20, newAuditKey);
                dataMap.put("intervalUnit", intervalUnit);
                dataMap.put("NoOfpost", NoOfpost);
                dataMap.put("startDate", startDate);
            }
     
            KwlReturnObject rObj = accSalesOrderDAOobj.saveRepeateSalesOrderInfo(dataMap);
            RepeatedSalesOrder rSalesOrder = (RepeatedSalesOrder) rObj.getEntityList().get(0);
            
            JSONObject SOjson = new JSONObject();
            SOjson.put("SOid", request.getParameter("invoiceid"));
            SOjson.put("repeateid", rSalesOrder.getId());
            accSalesOrderDAOobj.updateSalesOrder(SOjson, null);
            if (!StringUtil.isNullOrEmpty(request.getParameter("detail"))) {
                repeateid = rSalesOrder.getId();
                int delcount = accSalesOrderDAOobj.DelRepeateSOMemo(repeateid);
                detail = request.getParameter("detail");
                JSONArray arrMemo = new JSONArray(detail);
                for (int i = 0; i < arrMemo.length(); i++) {
                    JSONObject jsonmemo = arrMemo.getJSONObject(i);
                    HashMap<String, Object> dataMapformemo = new HashMap<String, Object>();
                    dataMapformemo.put("no", Integer.parseInt(jsonmemo.get("no").toString()));
                    dataMapformemo.put("memo", jsonmemo.get("memo"));
                    dataMapformemo.put("RepeatedSOID", rSalesOrder.getId());
                    KwlReturnObject savememo = accSalesOrderDAOobj.saveRepeateSOMemo(dataMapformemo);
                }
            }
            String billno = request.getParameter("billno");
            requestParams.put("billno", billno);
            msg = messageSource.getMessage("acc.field.RecurringSaleshasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)); //"Recurring Invoice has been saved successfully";
            issuccess = true;
            auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER, messageSource.getMessage("acc.field.User", null, RequestContextUtils.getLocale(request)) + sessionHandlerImpl.getUserFullName(request) +" " +(isEditFlag?messageSource.getMessage("acc.field.RecurringSaleshasbeenupdated", null, RequestContextUtils.getLocale(request)):messageSource.getMessage("acc.field.RecurringSaleshasbeenadded", null, RequestContextUtils.getLocale(request))) + " " +billno+" "+auditMessage,request, rSalesOrder.getId());
            SendMail(requestParams);    //Notification Mail
            txnManager.commit(status);
            
            
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getSalesOrderRepeateDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "", parentSOId = "";
        try {
            if (!StringUtil.isNullOrEmpty(request.getParameter("parentid"))) {
                parentSOId = request.getParameter("parentid");
            } else if (!StringUtil.isNullOrEmpty(request.getParameter("bills"))) {
                parentSOId = request.getParameter("bills");
            }
            JSONArray JArr = new JSONArray();
            String[] salesOrders = null;
            KwlReturnObject details = null;
            int i = 0;
            salesOrders = (salesOrders == null) ? parentSOId.split(",") : salesOrders;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            while (salesOrders != null && i < salesOrders.length) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("parentSOId", salesOrders[i]);
                requestParams.put("companyid", companyid);
                details = accSalesOrderDAOobj.getRepeateSalesOrderDetailsForExpander(requestParams);
                List detailsList = details.getEntityList();
                if (detailsList.size() > 0) {
                    Iterator itr = detailsList.iterator();
                    while (itr.hasNext()) {
                        Object[] repeatedSalesOrders = (Object[]) itr.next();
                        JSONObject obj = new JSONObject();
                        obj.put("invoiceId", repeatedSalesOrders[0].toString());
                        obj.put("invoiceNo", repeatedSalesOrders[1].toString());
                        obj.put("parentInvoiceId", salesOrders[i]);
                        obj.put("isExpander", true);
                        JArr.put(obj);
                    }
                    i++;
                } else {
                    JSONObject obj = new JSONObject();
                    obj.put("parentInvoiceId", salesOrders[i]);
                    obj.put("isExpander", false);
                    JArr.put(obj);
                    i++;
                }
            }
            jobj.put("data", JArr);
            jobj.put("count", salesOrders.length);
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView approveCustomerQuotation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String currentUser = sessionHandlerImpl.getUserid(request);
            String remark = request.getParameter("remark");
            String doID = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double totalOrderAmount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount")) ? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);
            KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(Quotation.class.getName(), doID);
            Quotation cqObj = (Quotation) CQObj.getEntityList().get(0);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            double totalProfitMargin = 0;
            double totalProfitMarginPerc = 0;
            HashMap<String, Object> qApproveMap = new HashMap<String, Object>();
            totalProfitMargin = cqObj.getTotalProfitMargin();
            totalProfitMarginPerc = cqObj.getTotalProfitMarginPercent();
            int level = cqObj.getApprovestatuslevel();
            String currencyid=cqObj.getCurrency()!=null?cqObj.getCurrency().getCurrencyID():sessionHandlerImpl.getCurrencyID(request);
            // Add Product and discounts mapping
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            JSONArray productDiscountJArr=new JSONArray();
            Set<QuotationDetail> purchaseOrderDetail = cqObj.getRows();
            for (QuotationDetail poDetail : purchaseOrderDetail) {
                String productId = poDetail.getProduct().getID();
                double discountVal = poDetail.getDiscount();
                int isDiscountPercent = poDetail.getDiscountispercent();
                if(isDiscountPercent==1){
                    discountVal = (poDetail.getQuantity()*poDetail.getRate())*(discountVal/100);
                }
                KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, cqObj.getQuotationDate(), cqObj.getExternalCurrencyRate());
                double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                discAmountinBase = authHandler.round(discAmountinBase, companyid);
                JSONObject productDiscountObj=new JSONObject();
                productDiscountObj.put("productId", productId);
                productDiscountObj.put("discountAmount", discAmountinBase);
                productDiscountJArr.put(productDiscountObj);
            }
            qApproveMap.put(Constants.companyKey, companyid);
            qApproveMap.put("level", level);
            qApproveMap.put("totalAmount", String.valueOf(totalOrderAmount));
            qApproveMap.put("totalProfitMargin", totalProfitMargin);
            qApproveMap.put("totalProfitMarginPerc", totalProfitMarginPerc);
            qApproveMap.put("currentUser", currentUser);
            qApproveMap.put("fromCreate", false);
            qApproveMap.put("productDiscountMapList", productDiscountJArr);
            qApproveMap.put(Constants.moduleid, Constants.Acc_Customer_Quotation_ModuleId);
            qApproveMap.put(Constants.PAGE_URL, baseUrl);
            List approvedLevelList = accSalesOrderServiceobj.approveCustomerQuotation(cqObj, qApproveMap, true);
            int approvedLevel = (Integer) approvedLevelList.get(0);
            
            
            /*---------Same as Sales Order-----------  */
            if (approvedLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences

                HashMap emailMap = new HashMap();
                String userName = sessionHandlerImpl.getUserFullName(request);
                emailMap.put("userName", userName);
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                emailMap.put("company", company);
                emailMap.put("customerQuotation", cqObj);
                emailMap.put("baseUrl", baseUrl);
                emailMap.put("preferences", preferences);
                emailMap.put("ApproveMap", qApproveMap);

                accSalesOrderServiceobj.sendApprovalMailForCQIfAllowedFromSystemPreferences(emailMap);

            }

            // Save Approval History
            if (approvedLevel != Constants.NoAuthorityToApprove) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("transtype", Constants.CUSTOMER_QUOTATION_APPROVAL);
                hashMap.put("transid", cqObj.getID());
                hashMap.put("approvallevel", cqObj.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
                hashMap.put("remark", remark);
                hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                hashMap.put(Constants.companyKey, companyid);
                accountingHandlerDAOobj.updateApprovalHistory(hashMap);

                // Audit log entry
                auditTrailObj.insertAuditLog(AuditAction.CUSTOMER_QUOTATION_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has Approved a Customer Quotation " + cqObj.getQuotationNumber()+ " at Level-" + cqObj.getApprovestatuslevel(), request, cqObj.getID());
                txnManager.commit(status);
                issuccess = true;
                KwlReturnObject kmsg = null;
                String roleName = "Company User";
                kmsg = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
                Iterator ite2 = kmsg.getEntityList().iterator();
                while (ite2.hasNext()) {
                    Object[] row = (Object[]) ite2.next();
                    roleName = row[1].toString();
                }
                msg = messageSource.getMessage("acc.field.CustomerQuotationhasbeenapprovedsuccessfully", null, RequestContextUtils.getLocale(request)) + " by " + roleName + " " + sessionHandlerImpl.getUserFullName(request) + " at Level " + cqObj.getApprovestatuslevel() + ".";
            } else {
                txnManager.commit(status);
                issuccess = true;
                msg = messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, RequestContextUtils.getLocale(request)) + cqObj.getApprovestatuslevel() + ".";
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView rejectPendingCustomerQuotation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String roleName = "";
            int level = 0;
            KwlReturnObject userRoleResult = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
            Iterator itr = userRoleResult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                roleName = row[1].toString();
            }
            
            String cqID = "";
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONObject jObj = jArr.getJSONObject(0);
            cqID = StringUtil.DecodeText(jObj.optString("billid"));
            KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(Quotation.class.getName(), cqID);
            Quotation cqObj = (Quotation) CQObj.getEntityList().get(0);
            level = cqObj.getApprovestatuslevel();
            
            boolean isRejected = rejectPendingCustomerQuotation(request);
            txnManager.commit(status);
            issuccess = true;
            
            if (isRejected) {
                msg = messageSource.getMessage("acc.field.CustomerQuotationhasbeenrejectedsuccessfully", null, RequestContextUtils.getLocale(request)) + " by " + roleName + " " + sessionHandlerImpl.getUserFullName(request) + " at Level " + level + ".";
            } else {
                msg = messageSource.getMessage("acc.vq.notAuthorisedToRejectThisRecord", null, RequestContextUtils.getLocale(request)) + " at Level " + level + ".";
            }
            
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public boolean rejectPendingCustomerQuotation(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException, ScriptException {
        boolean isRejected = false;
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String remark = request.getParameter("remark");
            String currentUser = sessionHandlerImpl.getUserid(request);
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), currentUser);
            User user = (User) userResult.getEntityList().get(0);
            String actionId = "66", actionMsg = "rejected";
            int level = 0;
            String amount = "";
            for (int i = 0; i < jArr.length(); i++) {
                boolean hasAuthorityToReject = false;
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String cqid = StringUtil.DecodeText(jobj.optString("billid"));
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(Quotation.class.getName(), cqid);
                    Quotation cqObj = (Quotation) cap.getEntityList().get(0);
                    double totalAmount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount")) ? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);
                    double totalProfitMargin = 0;
                    double totalProfitMarginPerc = 0;
                    HashMap<String, Object> qApproveMap = new HashMap<String, Object>();
                    totalProfitMargin = cqObj.getTotalProfitMargin();
                    totalProfitMarginPerc = cqObj.getTotalProfitMarginPercent();
                    level = cqObj.getApprovestatuslevel();
                    qApproveMap.put(Constants.companyKey, companyid);
                    qApproveMap.put("level", level);
                    qApproveMap.put("totalAmount", String.valueOf(totalAmount));
                    qApproveMap.put("totalProfitMargin", totalProfitMargin);
                    qApproveMap.put("totalProfitMarginPerc", totalProfitMarginPerc);
                    qApproveMap.put("currentUser", currentUser);
                    qApproveMap.put("fromCreate", false);
                    qApproveMap.put(Constants.moduleid, Constants.Acc_Customer_Quotation_ModuleId);
                    amount = String.valueOf(totalAmount);
                    if (AccountingManager.isCompanyAdmin(user)) {
                        hasAuthorityToReject = true;
                    } else {
                        hasAuthorityToReject = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(qApproveMap);
                    }
                    if (hasAuthorityToReject) {
                        accSalesOrderDAOobj.rejectPendingCustomerQuotation(cqObj.getID(), companyid);
                        isRejected = true;
                        // Maintain Approval History of Rejected Record
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("transtype", Constants.CUSTOMER_QUOTATION_APPROVAL);
                        hashMap.put("transid", cqObj.getID());
                        hashMap.put("approvallevel", Math.abs(cqObj.getApprovestatuslevel()));//  If approvedLevel = 11 then its final Approval
                        hashMap.put("remark", remark);
                        hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                        hashMap.put(Constants.companyKey, companyid);
                        hashMap.put("isrejected", true);
                        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                        auditTrailObj.insertAuditLog(actionId, "User " + sessionHandlerImpl.getUserFullName(request) + " " + actionMsg + " Customer Quotation " + cqObj.getQuotationNumber(), request, cqObj.getID());
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
    
    //this function has no usages
    public int approveCustomerQuotation(Quotation doObj, String companyid, int level, String amount, String currentUser, boolean fromCreate) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException {
        boolean hasAuthority = false;
        if (!fromCreate) {
            String thisUser = currentUser;
            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
            User user = (User) userclass.getEntityList().get(0);
            
            if (AccountingManager.isCompanyAdmin(user)) {
                hasAuthority = true;
            } else {
                hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRule(level, companyid, amount, thisUser, Constants.Acc_Customer_Quotation_ModuleId);
            }
        } else {
            hasAuthority = true;
        }
        if (hasAuthority) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            String requisitionApprovalSubject = "Customer Quotation: %s - Approval Notification";
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
                    + "<p>%s has created Customer Quotation %S and sent it to you for approval.</p>"
                    + "<p>Please review and approve it (Customer Quotation Number: %s).</p>"
                    + "<p></p>"
                    + "<p>Thanks</p>"
                    + "<p>This is an auto generated email. Do not reply<br>";
            String requisitionApprovalPlainMsg = "Hi All,\n\n"
                    + "%s has created Customer Quotation %S and sent it to you for approval.\n"
                    + "Please review and approve it (Customer Quotation Number: %s).\n\n"
                    + "Thanks\n\n"
                    + "This is an auto generated email. Do not reply\n";
            int approvalStatus = 11;
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String prNumber = doObj.getQuotationNumber();
            String cqID = doObj.getID();
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put(Constants.companyKey, companyid);
            qdDataMap.put("level", level + 1);
            qdDataMap.put(Constants.moduleid, Constants.Acc_Customer_Quotation_ModuleId);
            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            Iterator itr = flowresult.getEntityList().iterator();
            
            //String fromEmailId = Constants.ADMIN_EMAILID;
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) returnObject.getEntityList().get(0);
            Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
            
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
//            JSONObject obj = new JSONObject();
                String rule = "";
                if (row[2] != null) {
                    rule = row[2].toString();
                }
                rule = rule.replaceAll("[$$]+", amount);
                if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                    // send emails
                    try {
                        if (Boolean.parseBoolean(row[3].toString()) && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                            String fromName = "User";
                            String fromEmailId = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                            fromName = doObj.getCreatedby().getFirstName().concat(" ").concat(doObj.getCreatedby().getLastName());
                            String fromEmailId1 = StringUtil.isNullOrEmpty(doObj.getCompany().getEmailID()) ? authHandlerDAOObj.getSysEmailIdByCompanyID(doObj.getCompany().getCompanyID()) : doObj.getCompany().getEmailID();
                            if (!StringUtil.isNullOrEmpty(fromEmailId1)) {
                                fromEmailId = fromEmailId1;
                            }
                            String subject = String.format(requisitionApprovalSubject, prNumber);
                            String htmlMsg = String.format(requisitionApprovalHtmlMsg, fromName, prNumber, prNumber, prNumber);
                            String plainMsg = String.format(requisitionApprovalPlainMsg, fromName, prNumber, prNumber, prNumber);
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
                                SendMailHandler.postMail(emails, subject, htmlMsg, plainMsg, fromEmailId, smtpConfigMap);
                            }
                        }
                    } catch (MessagingException ex) {
                        Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    approvalStatus = level + 1;
                }
            }
            accSalesOrderDAOobj.approvePendingCustomerQuotation(cqID, companyid, approvalStatus);
            return approvalStatus;
        } else {
            return Constants.NoAuthorityToApprove; //if not have approval permission then return one fix value like 999
        }
    }
    
    public ModelAndView rejectPendingSalesOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String roleName = "";
            int level = 0;
            KwlReturnObject userRoleResult = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
            Iterator itr = userRoleResult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                roleName = row[1].toString();
            }
            
            String soID = "";
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONObject jObj = jArr.getJSONObject(0);
            soID = StringUtil.DecodeText(jObj.optString("billid"));
            KwlReturnObject SOObj = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soID);
            SalesOrder soObj = (SalesOrder) SOObj.getEntityList().get(0);
            level = soObj.getApprovestatuslevel();
            
            boolean isRejected = rejectPendingSalesOrder(request);
            txnManager.commit(status);
            issuccess = true;
            
            if (isRejected) {
                msg = messageSource.getMessage("acc.field.SalesOrderhasbeenrejectedsuccessfully", null, RequestContextUtils.getLocale(request)) + " by " + roleName + " " + sessionHandlerImpl.getUserFullName(request) + " at Level " + level + ".";
            } else {
                msg = messageSource.getMessage("acc.vq.notAuthorisedToRejectThisRecord", null, RequestContextUtils.getLocale(request)) + " at Level " + level + ".";
            }
            
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public boolean rejectPendingSalesOrder(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException, ScriptException {
        boolean isRejected = false;
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String remark = request.getParameter("remark");
            String currentUser = sessionHandlerImpl.getUserid(request);
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), currentUser);
            User user = (User) userResult.getEntityList().get(0);
            KwlReturnObject kwlcmp = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) kwlcmp.getEntityList().get(0);
            String actionId = "66", actionMsg = "rejected";
            int level = 0;
            String amount = "";
            for (int i = 0; i < jArr.length(); i++) {
                boolean hasAuthorityToReject = false;
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String soid = StringUtil.DecodeText(jobj.optString("billid"));
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soid);
                    SalesOrder soObj = (SalesOrder) cap.getEntityList().get(0);
                    double totalAmount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount")) ? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);
                    double totalProfitMargin = 0;
                    double totalProfitMarginPerc = 0;
                    HashMap<String, Object> soApproveMap = new HashMap<String, Object>();
                    // Add Product and discounts mapping
                    HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
                    String currencyid = soObj.getCurrency() != null ? soObj.getCurrency().getCurrencyID() : sessionHandlerImpl.getCurrencyID(request);
                    JSONArray productDiscountJArr = new JSONArray();
                    Set<SalesOrderDetail> salesOrderDetails = soObj.getRows();
                    for (SalesOrderDetail soDetail : salesOrderDetails) {
                        String productId = soDetail.getProduct().getID();
                        double discountVal = soDetail.getDiscount();
                        int isDiscountPercent = soDetail.getDiscountispercent();
                        if (isDiscountPercent == 1) {
                            discountVal = (soDetail.getQuantity() * soDetail.getRate()) * (discountVal / 100);
                        }
                        KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, soObj.getOrderDate(), soObj.getExternalCurrencyRate());
                        double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                        discAmountinBase = authHandler.round(discAmountinBase, companyid);
                        JSONObject productDiscountObj = new JSONObject();
                        productDiscountObj.put("productId", productId);
                        productDiscountObj.put("discountAmount", discAmountinBase);
                        productDiscountJArr.put(productDiscountObj);
                    }
                    totalProfitMargin = soObj.getTotalProfitMargin();
                    totalProfitMarginPerc = soObj.getTotalProfitMarginPercent();
                    level = soObj.getApprovestatuslevel();
                    soApproveMap.put(Constants.companyKey, companyid);
                    soApproveMap.put("level", level);
                    soApproveMap.put("totalAmount", String.valueOf(totalAmount));
                    soApproveMap.put("totalProfitMargin", totalProfitMargin);
                    soApproveMap.put("totalProfitMarginPerc", totalProfitMarginPerc);
                    soApproveMap.put("currentUser", currentUser);
                    soApproveMap.put("fromCreate", false);
                    soApproveMap.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                    soApproveMap.put("productDiscountMapList", productDiscountJArr);
                    amount = String.valueOf(totalAmount);
                    if (AccountingManager.isCompanyAdmin(user)) {
                        hasAuthorityToReject = true;
                    } else {
                        hasAuthorityToReject = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(soApproveMap);
                    }
                    if (hasAuthorityToReject) {
                        accSalesOrderDAOobj.rejectPendingSalesOrder(soObj.getID(), companyid);
                        accSalesOrderDAOobj.deleteBatchDetailsAfterRejectPendingSalesOrder(soObj.getID(), companyid);
                        sendApproveRejctionOfSalesOrderMail(request, preferences, companyid, soObj,Constants.REJECTION_EMAIL);
                        isRejected = true;
                        // Maintain Approval History of Rejected Record
                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
                        hashMap.put("transtype", Constants.SALES_ORDER_APPROVAL);
                        hashMap.put("transid", soObj.getID());
                        hashMap.put("approvallevel", Math.abs(soObj.getApprovestatuslevel()));//  If approvedLevel = 11 then its final Approval
                        hashMap.put("remark", remark);
                        hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                        hashMap.put(Constants.companyKey, companyid);
                        hashMap.put("isrejected", true);
                        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                        auditTrailObj.insertAuditLog(actionId, "User " + sessionHandlerImpl.getUserFullName(request) + " " + actionMsg + " Sales Order " + soObj.getSalesOrderNumber(), request, soObj.getID());
                    }
                }
            }
        } /*catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }catch (Exception ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }
        return isRejected;
    }
    
    public void sendApproveRejctionOfSalesOrderMail(HttpServletRequest request, CompanyAccountPreferences preferences, String companyid, SalesOrder soObj, String fieldid) throws ServiceException {
        try {
            String userName = sessionHandlerImpl.getUserFullName(request);
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) returnObject.getEntityList().get(0);
            String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
            String creatormail = company.getCreator().getEmailID();
            String documentcreatoremail = (soObj != null && soObj.getCreatedby() != null) ? soObj.getCreatedby().getEmailID() : "";
            String fname = company.getCreator().getFirstName() == null ? "" : company.getCreator().getFirstName();
            String lname = company.getCreator().getLastName() == null ? "" : company.getCreator().getLastName();
            String creatorname = fname + " " + lname;
            String approvalpendingStatusmsg = "";
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            int level = soObj.getApprovestatuslevel();
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            ArrayList<String> emailArray = new ArrayList<>();
            qdDataMap.put(Constants.companyKey, companyid);
            qdDataMap.put("level", level);
            qdDataMap.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
//            emailArray = commonFnControllerService.getUserApprovalEmail(qdDataMap);
            emailArray.add(creatormail);
            if (!StringUtil.isNullOrEmpty(documentcreatoremail) && !creatormail.equalsIgnoreCase(documentcreatoremail)) {
                emailArray.add(documentcreatoremail);
            }
            String[] emails = {};
            emails = emailArray.toArray(emails);
            if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
            }
            if (emails.length > 0) {
                String salesordernumber = soObj.getSalesOrderNumber();
                KwlReturnObject result = accountingHandlerDAOobj.getEmailTemplateTosendApprovalMail(companyid, fieldid, Constants.Acc_Sales_Order_ModuleId);
                NotificationRules dft = (NotificationRules) result.getEntityList().get(0);
                //get email ids of selected users
                String users = dft.getUsers();
                if (dft.isMailToAssignedTo() && !StringUtil.isNullOrEmpty(users) && users.split(",").length > 0) {
                    String usersArr[] = users.split(",");
                    String[] userEmailIds = new String[usersArr.length];
                    for (int j = 0; j < usersArr.length; j++) {
                        User userObj = (User) kwlCommonTablesDAOObj.getClassObject(User.class.getName(), usersArr[j]);
                        if (userObj != null && !StringUtil.isNullOrEmpty(userObj.getEmailID())) {
                            userEmailIds[j] = userObj.getEmailID();
                        }
                    }
                    if (userEmailIds.length > 0) {
                        emails = AccountingManager.getMergedMailIds(emails, userEmailIds);
                    }
                }
                //get email id of document creator
                String[] docCreatorEmailid={};
                if (dft.isMailToCreator() && soObj.getCreatedby() != null) {
                    User userObj = (User) kwlCommonTablesDAOObj.getClassObject(User.class.getName(), soObj.getCreatedby().getUserID() != null ? soObj.getCreatedby().getUserID() : "");
                    if (userObj != null && !StringUtil.isNullOrEmpty(userObj.getEmailID())) {
                        docCreatorEmailid = userObj.getEmailID().split(",");
                    }
                    if (docCreatorEmailid.length > 0) {
                        emails = AccountingManager.getMergedMailIds(emails, docCreatorEmailid);
                    }
                }
                
                //to get email ids entered in send a copy to
                String[] otherEmailIds = {};
                if (!StringUtil.isNullOrEmpty(dft.getEmailids())) {
                    otherEmailIds = dft.getEmailids().split(",");
                    if (otherEmailIds.length > 0) {
                        emails = AccountingManager.getMergedMailIds(emails, otherEmailIds);
                    }
                }
                if (soObj.getApprovestatuslevel() < 11 && fieldid.equalsIgnoreCase("22")) {
//                qdDataMap.put("level", creditNote.getApprovestatuslevel()+1);
                approvalpendingStatusmsg=commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
                String emailBody = dft.getMailcontent();
                String emailBody1 = "";
                String emailBody2 = "";
                int usernamelength=userName.length();
                int sonumberlength=soObj.getSalesOrderNumber().length();
                String subject = dft.getMailsubject();
                emailBody = emailBody.replaceAll("#UserName#", userName);
                emailBody = emailBody.replaceAll("#ModuleName#", Constants.moduleID_NameMap.get(Constants.Acc_Sales_Order_ModuleId));
                emailBody = emailBody.replaceAll("#SalesOrderNo#", soObj.getSalesOrderNumber());
                subject = subject.replaceAll("#SalesOrderNo#", soObj.getSalesOrderNumber());
                emailBody = emailBody.replaceAll("#Approvalstatuslevel#", String.valueOf(level));
                emailBody = emailBody.replaceAll("#baseurl#",baseUrl);
                emailBody =emailBody.replaceAll("#approvalpendingStatusmsg#",approvalpendingStatusmsg);
                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                try {
                    SendMailHandler.postMail(emails, subject, emailBody, emailBody, sendorInfo, smtpConfigMap);
                } catch (Exception ex) {
                    System.out.printf("Error occured while sending Approval/Rejection mail for transaction : " + soObj.getSalesOrderNumber());
                }
//                accountingHandlerDAOobj.sendApprovedEmails(soObj.getSalesOrderNumber(), userName, emails, sendorInfo, Constants.SALESORDER, "All", companyid,baseUrl);  
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)), ex);
        }
    }
    
    


    public ModelAndView approveConsignmentRequest(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean activateCRblockingWithoutStock = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
//            String remark = request.getParameter("remark");
//            String soDetailID = request.getParameter("billid");
//            String batchid = request.getParameter("batchid");
//            String newbatchserialid = request.getParameter("newbatchserialid");
            String userId = sessionHandlerImpl.getUserid(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) returnObject.getEntityList().get(0);

            KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
            activateCRblockingWithoutStock = extraCompanyPreferences.isActivateCRblockingWithoutStock();
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            HashMap<String,String> consignmentmap=new HashMap<String, String>();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject obj = jArr.getJSONObject(i);
               //Updating Lock quanity According to Approval Quanity
                String serialbatchmapid = obj.getString("serialbatchmapid"); //request.getParameter("serialbatchmapid");
                String locationbatchmapid = obj.getString("locationbatchmapid");
                String soDetailID = obj.getString("rowid");
                if(!StringUtil.isNullOrEmpty(serialbatchmapid)){
                    KwlReturnObject SodetailObj = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), soDetailID);
                    SalesOrderDetail salesOrderDetail = (SalesOrderDetail) SodetailObj.getEntityList().get(0);
                    KwlReturnObject SerialObj = accountingHandlerDAOobj.getObject(SerialDocumentMapping.class.getName(), serialbatchmapid);
                    SerialDocumentMapping documentMapping = (SerialDocumentMapping) SerialObj.getEntityList().get(0);
                    if (documentMapping != null) {
                        if(documentMapping.getRequestApprovalStatus()!=RequestApprovalStatus.APPROVED){
                            documentMapping.setRequestApprovalStatus(RequestApprovalStatus.APPROVED);
                            if (salesOrderDetail != null) {
                                if((salesOrderDetail.getApprovedQuantity()+salesOrderDetail.getRejectedQuantity())<=salesOrderDetail.getQuantity()){
                                    salesOrderDetail.setApprovedQuantity(salesOrderDetail.getApprovedQuantity()+1);
                                    if(salesOrderDetail.getSalesOrder().isLockquantityflag()){
                                        salesOrderDetail.setLockquantity(salesOrderDetail.getLockquantity()+1);
                                        salesOrderDetail.setLockquantitydue(salesOrderDetail.getLockquantitydue()+1);
                                        salesOrderDetail.setLockQuantityInSelectedUOM(salesOrderDetail.getLockQuantityInSelectedUOM()+1);
                                    }
                                    
                                    accSalesOrderDAOobj.UpdateObject(salesOrderDetail);
                                }else{
                                    throw new AccountingException(messageSource.getMessage("acc.consignment.alreadyApproved", null, RequestContextUtils.getLocale(request)));//messageSource.getMessage("acc.field.SalesOrdernumber", null, RequestContextUtils.getLocale(request))  + 
                                }
                            }
                            accSalesOrderDAOobj.UpdateObject(documentMapping);
                        }else{
                            throw new AccountingException(messageSource.getMessage("acc.consignment.alreadyApproved", null, RequestContextUtils.getLocale(request)));//messageSource.getMessage("acc.field.SalesOrdernumber", null, RequestContextUtils.getLocale(request))  + 
                        }

                    }
                }else if(!StringUtil.isNullOrEmpty(locationbatchmapid)){
                    KwlReturnObject SoObj = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), soDetailID);
                    SalesOrderDetail salesOrderDetail = (SalesOrderDetail) SoObj.getEntityList().get(0);
                    KwlReturnObject SODetailObj = accountingHandlerDAOobj.getObject(LocationBatchDocumentMapping.class.getName(), locationbatchmapid);
                    LocationBatchDocumentMapping batchDocumentMapping = (LocationBatchDocumentMapping) SODetailObj.getEntityList().get(0);
                    double approvedQuantity = obj.optDouble("finalquantity",0);
                        if (batchDocumentMapping != null) {
                            batchDocumentMapping.setApprovedQuantity(batchDocumentMapping.getApprovedQuantity()+approvedQuantity);
                            if(salesOrderDetail!=null){
                                salesOrderDetail.setApprovedQuantity(salesOrderDetail.getApprovedQuantity()+approvedQuantity);
                            }
//                            batchDocumentMapping.setRejectedby(user);
                            accSalesOrderDAOobj.UpdateObject(batchDocumentMapping);
                            

                        }
                }else{
                        KwlReturnObject SerialObj = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), soDetailID);
                        SalesOrderDetail salesOrderDetail = (SalesOrderDetail) SerialObj.getEntityList().get(0);
                        double approvedQuantity = obj.optDouble("finalquantity",0);
                        if (salesOrderDetail != null) {
                            boolean isbatchlockedinSO=false;
                            boolean isSeriallockedinSO=false;
                            if((salesOrderDetail.getApprovedQuantity()+salesOrderDetail.getRejectedQuantity())<=salesOrderDetail.getQuantity()){
                                salesOrderDetail.setApprovedQuantity(salesOrderDetail.getApprovedQuantity()+approvedQuantity);
                                if(salesOrderDetail.getSalesOrder().isLockquantityflag()){
                                    salesOrderDetail.setLockquantity(salesOrderDetail.getLockquantity()+(approvedQuantity*salesOrderDetail.getBaseuomrate()));
                                    if (!StringUtil.isNullOrEmpty(soDetailID)) {
                                        isbatchlockedinSO = accSalesOrderDAOobj.getSalesorderBatchStatus(soDetailID, companyid);   //get sales order status whether is locked or not
                                    }
                                    if (!StringUtil.isNullOrEmpty(soDetailID)) {
                                        isSeriallockedinSO = accSalesOrderDAOobj.getSalesorderSerialStatus(soDetailID, companyid);   //get sales order status whether is locked or not
                                    }
                                    //we will reduce the 
                                    if(!isbatchlockedinSO && !isSeriallockedinSO) {
                                    salesOrderDetail.setLockquantitydue(salesOrderDetail.getLockquantitydue()+(approvedQuantity*salesOrderDetail.getBaseuomrate()));
                                    }
                                    salesOrderDetail.setLockQuantityInSelectedUOM(salesOrderDetail.getLockQuantityInSelectedUOM()+(approvedQuantity*salesOrderDetail.getBaseuomrate()));
                                }
                                accSalesOrderDAOobj.UpdateObject(salesOrderDetail);
                            }else{
                                throw new AccountingException(messageSource.getMessage("acc.consignment.alreadyApproved", null, RequestContextUtils.getLocale(request)));//messageSource.getMessage("acc.field.SalesOrdernumber", null, RequestContextUtils.getLocale(request))  + 
                            }
                        }
                }

                double approvedQuantity = obj.optDouble("finalquantity",0);
                KwlReturnObject SodetailObj = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), soDetailID);
                SalesOrderDetail salesOrderDetail = (SalesOrderDetail) SodetailObj.getEntityList().get(0);
                if (salesOrderDetail != null) {
                    if(consignmentmap.containsKey(obj.getString("billno")+"_"+obj.getString("billid"))){
                        String value=consignmentmap.get(obj.getString("billno")+"_"+obj.getString("billid"));
                        value=value+", "+salesOrderDetail.getProduct().getName()+" (Qty-"+approvedQuantity+")";
                        consignmentmap.remove(obj.getString("billno")+"_"+obj.getString("billid"));
                        consignmentmap.put(obj.getString("billno")+"_"+obj.getString("billid"),value);
                    }else{
                        consignmentmap.put(obj.getString("billno")+"_"+obj.getString("billid"),salesOrderDetail.getProduct().getName()+" (Qty-"+approvedQuantity+")");
                    }
                }
            }
            JSONObject paramJobj=StringUtil.convertRequestToJsonObject(request);
            // Audit log entry
            for(Map.Entry entry:consignmentmap.entrySet()){
              String reqno[]=entry.getKey().toString().split("_");
              auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER, "User " + sessionHandlerImpl.getUserFullName(request) + " has approved consignment request "+reqno[0]+" for product: " + entry.getValue(), request,reqno[1]);
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), sessionHandlerImpl.getCompanyid(request));
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
                if (documentEmailSettings.isConsignmentRequestApproval()) {
                    KwlReturnObject sOrder = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), reqno[1]);
                    SalesOrder so = (SalesOrder) sOrder.getEntityList().get(0);
                    accSalesOrderServiceobj.sendConsignmentApprovalEmails(paramJobj,user, so, reqno[0], true,false);
               }
            }

            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.consignmentRequesthasbeenapprovedsuccessfully", null, RequestContextUtils.getLocale(request)) + ".";//+" by "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" at Level "+cqObj.getApprovestatuslevel()
            if (activateCRblockingWithoutStock) {
                TransactionStatus statusforBlockSOQty = txnManager.getTransaction(def);
                try {
                    accSalesOrderServiceobj.assignStockToPendingConsignmentRequests(paramJobj,company,user);
                    txnManager.commit(statusforBlockSOQty);
                } catch (Exception ex) {
                    txnManager.rollback(statusforBlockSOQty);
                    Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView rejectConsignmentRequest(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean activateCRblockingWithoutStock = false;
        boolean rejectblocking = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) returnObject.getEntityList().get(0);
            
            KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
            activateCRblockingWithoutStock = extraCompanyPreferences.isActivateCRblockingWithoutStock();
            
            HashMap<Integer, Object[]> rejectedBatchdetalisMap = new HashMap<Integer, Object[]>();
            int count=0;
            String prodidStrings = "";
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            HashMap<String,String> consignmentmap=new HashMap<String, String>();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject obj = jArr.getJSONObject(i);
                String serialbatchmapid = StringUtil.DecodeText(obj.optString("serialbatchmapid"));
                String locationbatchmapid = StringUtil.DecodeText(obj.optString("locationbatchmapid"));
                String sodetailid = StringUtil.DecodeText(obj.optString("rowid"));
//                KwlReturnObject SODetailObj = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), sodetailid);
//                SalesOrderDetail salesOrderDetail = (SalesOrderDetail) SODetailObj.getEntityList().get(0);
                  if(!StringUtil.isNullOrEmpty(serialbatchmapid)){
                        KwlReturnObject SOObj = accountingHandlerDAOobj.getObject(SerialDocumentMapping.class.getName(), serialbatchmapid);
                        SerialDocumentMapping documentMapping = (SerialDocumentMapping) SOObj.getEntityList().get(0);
                        String serialDetailsId="",productBatchId="",productid="";
                        Date mfgdate=null;
                        Date expdate=null;
                        if (documentMapping != null) {
                            documentMapping.setRequestApprovalStatus(RequestApprovalStatus.REJECTED);
                            if (documentMapping.getSerialid() != null) {
                                NewBatchSerial newBatchSerial = documentMapping.getSerialid();
                                serialDetailsId = newBatchSerial.getId();
                                newBatchSerial.setLockquantity(newBatchSerial.getLockquantity() - 1);
                                NewProductBatch newProductBatch = newBatchSerial.getBatch();
                                if (newProductBatch != null) {
                                    productBatchId = newProductBatch.getId(); 
                                    mfgdate=newProductBatch.getMfgdate();
                                    expdate=newProductBatch.getExpdate();
                                    productid=newProductBatch.getProduct();
                                    }

                                prodidStrings += "'" + productid + "',";
                                newProductBatch.setLockquantity(newProductBatch.getLockquantity() - 1);
                                newBatchSerial.setBatch(newProductBatch);
                                documentMapping.setSerialid(newBatchSerial);
                                documentMapping.setRejectedby(user);
                            }
                            accSalesOrderDAOobj.UpdateObject(documentMapping);
                            Object[] batchserialobjArr = null;
                            if (!StringUtil.isNullOrEmpty(serialDetailsId) && !StringUtil.isNullOrEmpty(productBatchId)) {
                                batchserialobjArr = new Object[5];
                                batchserialobjArr[0] = serialDetailsId;
                                batchserialobjArr[1] = productBatchId;
                                if (mfgdate!=null) {
                                    batchserialobjArr[2] =mfgdate;
                                }
                                if (expdate!=null) {
                                    batchserialobjArr[3] = expdate;
                                }
                                rejectedBatchdetalisMap.put(count++, batchserialobjArr);
                            }
                        }
                  }else if(!StringUtil.isNullOrEmpty(locationbatchmapid)){
                      KwlReturnObject SODetailObj = accountingHandlerDAOobj.getObject(LocationBatchDocumentMapping.class.getName(), locationbatchmapid);
                      LocationBatchDocumentMapping batchDocumentMapping = (LocationBatchDocumentMapping) SODetailObj.getEntityList().get(0);
                      double rejectedQuantity = obj.optDouble("finalquantity",0);
                        if (batchDocumentMapping != null) {
                            batchDocumentMapping.setRejectedQuantity(batchDocumentMapping.getRejectedQuantity()+rejectedQuantity);
//                            batchDocumentMapping.setRejectedby(user);
                            boolean isUserExist=false;
                            Set<LocationBatchRejectorMapping> users=batchDocumentMapping.getRejectedBy();
                            for(LocationBatchRejectorMapping rejectorMapping:users){
                                if(rejectorMapping.getRejectedby().equals(user)){
                                    rejectorMapping.setRejectedQuntity(rejectorMapping.getRejectedQuntity()+rejectedQuantity);
                                    rejectorMapping.setLocationDocumentMapping(batchDocumentMapping);
                                    rejectorMapping.setRejectedby(user);
                                    batchDocumentMapping.setRejectedBy(users);
                                    isUserExist=true;
                                }
                            }
                            if(isUserExist==false){
                                LocationBatchRejectorMapping rejectorMapping=new LocationBatchRejectorMapping();
                                rejectorMapping.setRejectedQuntity(rejectorMapping.getRejectedQuntity()+rejectedQuantity);
                                rejectorMapping.setLocationDocumentMapping(batchDocumentMapping);
                                rejectorMapping.setRejectedby(user);
                                users.add(rejectorMapping);
                                batchDocumentMapping.setRejectedBy(users);
                            }
                            accSalesOrderDAOobj.UpdateObject(batchDocumentMapping);
                            
                        }
                  }else{
                      KwlReturnObject SODetailObj = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), sodetailid);
                      SalesOrderDetail salesOrderDetail = (SalesOrderDetail) SODetailObj.getEntityList().get(0);
                      double rejectedQuantity = obj.optDouble("finalquantity",0);
                        if (salesOrderDetail != null) {
                            salesOrderDetail.setRejectedQuantity(salesOrderDetail.getRejectedQuantity()+rejectedQuantity);
                            salesOrderDetail.setRejectedby(user);
                            accSalesOrderDAOobj.UpdateObject(salesOrderDetail);
                            HashMap<String, Object> hashMap = new HashMap<String, Object>();
                            hashMap.put("documentid", salesOrderDetail.getID());
                            hashMap.put("rejectedQuantity", rejectedQuantity);
                            accSalesOrderDAOobj.releseSODBatchLockQuantity(hashMap);
                        }
                        
                  }
                  //NO Need to update Consignment Lock quanity due for row;
                  //                if (salesOrderDetail != null) {//Lock quantity updated on request reject 
//                    salesOrderDetail.setLockquantity(salesOrderDetail.getLockquantity() - 1);
//                    accSalesOrderDAOobj.UpdateObject(salesOrderDetail);
//                    
//                }
                double rejectedQuantity = obj.optDouble("finalquantity",0);
                KwlReturnObject SodetailObj = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), sodetailid);
                SalesOrderDetail salesOrderDetail = (SalesOrderDetail) SodetailObj.getEntityList().get(0);
                if (salesOrderDetail != null) {
                    if(consignmentmap.containsKey(obj.getString("billno")+"_"+obj.getString("billid"))){
                        String value=consignmentmap.get(obj.getString("billno")+"_"+obj.getString("billid"));
                        value=value+", "+salesOrderDetail.getProduct().getName()+" (Qty-"+rejectedQuantity+")";
                        consignmentmap.remove(obj.getString("billno")+"_"+obj.getString("billid"));
                        consignmentmap.put(obj.getString("billno")+"_"+obj.getString("billid"),value);
                    }else{
                        consignmentmap.put(obj.getString("billno")+"_"+obj.getString("billid"),salesOrderDetail.getProduct().getName()+" (Qty-"+rejectedQuantity+")");
                    }
                }
            }

            // Audit log entry
            for(Map.Entry entry:consignmentmap.entrySet()){
              String reqno[]=entry.getKey().toString().split("_");
              auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER, "User " + sessionHandlerImpl.getUserFullName(request) + " has rejected consignment request "+reqno[0]+" for product: " + entry.getValue(),request,reqno[1]);
            }
            
            txnManager.commit(status);
            
            if (activateCRblockingWithoutStock && rejectblocking) {
                TransactionStatus statusforBlockSOQty = txnManager.getTransaction(def);
                try {
                    JSONObject paramJobj=StringUtil.convertRequestToJsonObject(request);   
                    accSalesOrderServiceobj.assignStockToPendingConsignmentRequests(paramJobj,company,user);
                    txnManager.commit(statusforBlockSOQty);
                } catch (Exception ex) {
                    txnManager.rollback(statusforBlockSOQty);
                    Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            issuccess = true;
            msg = messageSource.getMessage("acc.field.consignmentRequesthasbeenrejectedsuccessfully", null, RequestContextUtils.getLocale(request)) + ".";//+" by "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" at Level "+level

        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //Delete SalesOrder Temporary
    public ModelAndView deleteSalesOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("servletContext", this.getServletContext());
            paramJobj.put(Constants.locale, RequestContextUtils.getLocale(request));
            jobj = accSalesOrderServiceobj.deleteSalesOrdersTemporary(paramJobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //Delete SalesOrders Permanent
    public ModelAndView deleteSalesOrdersPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("servletContext", this.getServletContext());
            paramJobj.put(Constants.locale, RequestContextUtils.getLocale(request));
            jobj = accSalesOrderServiceobj.deleteSalesOrdersPermanent(paramJobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

     public ModelAndView closeConsignmentRequest(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            closeConsignmentRequest(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.consignment.order.close", null, RequestContextUtils.getLocale(request));
            
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public void closeConsignmentRequest(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException { 
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String soid = StringUtil.DecodeText(jobj.optString("billid"));
                    KwlReturnObject res = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soid);
                    SalesOrder salesOrder = (SalesOrder) res.getEntityList().get(0);
                    String sono = salesOrder.getSalesOrderNumber();//jobj.getString("billno");
                    
                    //code for making  availble serials which are not used:
                    if (salesOrder != null) {
                        Set<SalesOrderDetail> soDetailsObj = salesOrder.getRows();
                        for (SalesOrderDetail row : soDetailsObj) {
                            Product product = row.getProduct();
                            boolean isLocationForProduct = false;
                            boolean isWarehouseForProduct = false;
                            boolean isBatchForProduct = false;
                            boolean isSerialForProduct = false;
                            boolean isRowForProduct = false;
                            boolean isRackForProduct = false;
                            boolean isBinForProduct = false;
                            double soLockQuantity = row.getLockquantity();
                            double soLockQuantitydue = row.getLockquantitydue();
                            if (!StringUtil.isNullOrEmpty(product.getID())) {
                                isBatchForProduct = product.isIsBatchForProduct();
                                isSerialForProduct = product.isIsSerialForProduct();
                                isLocationForProduct = product.isIslocationforproduct();
                                isWarehouseForProduct = product.isIswarehouseforproduct();
                                isRowForProduct = product.isIsrowforproduct();
                                isRackForProduct = product.isIsrackforproduct();
                                isBinForProduct = product.isIsbinforproduct();
                            }
                            
                            accSalesOrderDAOobj.updatebatchlockQuantity(product.getID(), row.getID(), companyid);
                            if (isSerialForProduct) {
                                accSalesOrderDAOobj.updateSerialslockQuantity(product.getID(), row.getID(), companyid);
                            }
                            if (soLockQuantity > 0) {
                                accCommonTablesDAO.updateSOLockQuantity(row.getID(), soLockQuantity, companyid);  //updte salesorder lock  quntity for all type of products
                            }
                            if (soLockQuantitydue > 0) {
                                accCommonTablesDAO.updateSOLockQuantitydue(row.getID(), soLockQuantitydue, companyid);
                            }
                        }
                    }   
                    salesOrder.setFreeze(true);
                    accSalesOrderDAOobj.UpdateObject(salesOrder);
                    String actionMsg = "closed";
                    auditTrailObj.insertAuditLog("77", " User " + sessionHandlerImpl.getUserFullName(request) + " has " + actionMsg + " a consignment request " + sono, request, soid);
                    
                }
            }
          
        } /*catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }
    
//Billing Sales order
    public ModelAndView saveBillingSalesOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            int pendingApprovalFlag = saveBillingSalesOrder(request);
            boolean pendingApproval = false;
            issuccess = true;
            if (pendingApprovalFlag == 1) {
                pendingApproval = true;
            }
            int istemplate = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("istemplate"))) {
                istemplate = Integer.parseInt(request.getParameter("istemplate"));
            }
            if (istemplate == 1) {
                msg = messageSource.getMessage("acc.field.SalesOrderandTemplatehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) + (pendingApproval ? messageSource.getMessage("acc.field.butSalesOrderispendingforApproval", null, RequestContextUtils.getLocale(request)) : ".");
            } else if (istemplate == 2) {
                msg = messageSource.getMessage("acc.field.SalesOrderTemplatehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.so.save", null, RequestContextUtils.getLocale(request)) + ((pendingApprovalFlag == 1) ? messageSource.getMessage("acc.field.butpendingforApproval", null, RequestContextUtils.getLocale(request)) : ".");   //"Sales order has been saved successfully";
            }
            
            
            jobj.put("pendingApproval", pendingApproval);
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public int saveBillingSalesOrder(HttpServletRequest request) throws ServiceException, AccountingException, JSONException {
        BillingSalesOrder bSalesOrder = null;
        int pendingApprovalFlag = 0;
        try {
            int istemplate = request.getParameter("istemplate") != null ? Integer.parseInt(request.getParameter("istemplate")) : 0;
            String taxid = null;
            taxid = request.getParameter("taxid");
            double taxamount = StringUtil.getDouble(request.getParameter("taxamount"));
            
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String sequenceformat = request.getParameter("sequenceformat");
            String entryNumber = request.getParameter("number");
            String soid = request.getParameter("invoiceid");
            String costCenterId = request.getParameter("costcenter");
            String nextAutoNumber;
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            
            HashMap<String, Object> soDataMap = new HashMap<String, Object>();
            
            KwlReturnObject socnt = accSalesOrderDAOobj.getBillingSalesOrderCount(entryNumber, companyid);
            if (socnt.getRecordTotalCount() > 0 && istemplate != 2) {
                if (StringUtil.isNullOrEmpty(soid)) {
                    throw new AccountingException(messageSource.getMessage("acc.field.SalesOrdernumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                } else {
                    nextAutoNumber = entryNumber;
                    soDataMap.put(Constants.Acc_id, soid);
                    accSalesOrderDAOobj.deleteBillingSalesOrderDetails(soid, companyid);
                }
            } else {
                boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                String nextAutoNoInt = "";
                if (seqformat_oldflag) {
                    nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGSALESORDER, sequenceformat);
                } else {
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_BILLINGSALESORDER, sequenceformat, seqformat_oldflag, new Date());
                    nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    soDataMap.put(Constants.SEQFORMAT, sequenceformat);
                    soDataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                }
            }
            
            DateFormat df = authHandler.getDateOnlyFormat(request);
            soDataMap.put("entrynumber", entryNumber);
            soDataMap.put("autogenerated", nextAutoNumber.equals(entryNumber));
            soDataMap.put("memo", request.getParameter("memo"));
            soDataMap.put("posttext", request.getParameter("posttext"));
            soDataMap.put("customerid", request.getParameter("customer"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("perdiscount"))) {
                soDataMap.put("perDiscount", StringUtil.getBoolean(request.getParameter("perdiscount")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("discount"))) {
                soDataMap.put("discount", StringUtil.getDouble(request.getParameter("discount")));
            }
            //soDataMap.put("credito", request.getParameter("creditoraccount"));
            soDataMap.put("orderdate", df.parse(request.getParameter("billdate")));
            soDataMap.put("duedate", df.parse(request.getParameter("duedate")));
            
            if (request.getParameter("shipdate") != null && !StringUtil.isNullOrEmpty(request.getParameter("shipdate"))) {
                soDataMap.put("shipdate", df.parse(request.getParameter("shipdate")));
            }
            soDataMap.put("shipvia", request.getParameter("shipvia"));
            soDataMap.put("fob", request.getParameter("fob"));
            soDataMap.put("termid", request.getParameter("termid"));
            soDataMap.put("currencyid", currencyid);
            soDataMap.put("isfavourite", request.getParameter("isfavourite"));
            soDataMap.put("shipaddress", request.getParameter("shipaddress"));
            soDataMap.put("billto", request.getParameter("billto"));
            soDataMap.put("salesPerson", request.getParameter("salesPerson"));
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                soDataMap.put("costCenterId", costCenterId);
            }
            soDataMap.put(Constants.companyKey, companyid);
            
            if (taxid != null && !taxid.isEmpty()) {
                Tax tax = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxid);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                }
                soDataMap.put("taxid", taxid);
            } else if (!taxid.isEmpty()) {
                soDataMap.put("taxid", taxid);
            }

            //save Billing Sales Order
            KwlReturnObject result = accSalesOrderDAOobj.saveBillingSalesOrder(soDataMap);
            bSalesOrder = (BillingSalesOrder) result.getEntityList().get(0);

            //save Billing Sales Order Details
            List sodetails = saveBillingSalesOrderRows(request, bSalesOrder, companyid, currencyid, GlobalParams, externalCurrencyRate);
            double soAmount = (Double) sodetails.get(1) + taxamount;
            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, soAmount, currencyid, df.parse(request.getParameter("billdate")), externalCurrencyRate);
            soAmount = (Double) bAmt.getEntityList().get(0);
            ArrayList amountApprove = (accountingHandlerDAOobj.getApprovalFlagForAmount(soAmount, Constants.SALES_ORDER, Constants.TRANS_AMOUNT, companyid));
            pendingApprovalFlag = (istemplate != 2 ? ((Boolean) (amountApprove.get(0)) ? 1 : 0) : 0);
            bSalesOrder.setPendingapproval(pendingApprovalFlag);
            bSalesOrder.setApprovallevel((Integer) (amountApprove.get(1)));
            bSalesOrder.setIstemplate(istemplate);
            
            if (pendingApprovalFlag == 1) { //this for send approval email
                String[] emails = {};
                String invoiceNumber = bSalesOrder.getSalesOrderNumber();
                String userName = sessionHandlerImpl.getUserFullName(request);
                String moduleName = "Sales Order";
                emails = accountingHandlerDAOobj.getApprovalUserList(request, moduleName, 1);
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(),companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                String fromEmailId = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
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
                hashMap.put(Constants.moduleid, Constants.Acc_BillingSales_Order_ModuleId);
                hashMap.put("modulerecordid", bSalesOrder.getID());
                hashMap.put(Constants.companyKey, companyid);
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
            throw ServiceException.PARSE_ERROR("saveBillingSalesOrder : " + ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("saveBillingSalesOrder : " + ex.getMessage(), ex);
        }
        return pendingApprovalFlag;
    }
    
    private List saveBillingSalesOrderRows(HttpServletRequest request, BillingSalesOrder salesOrder, String companyid, String currencyid, HashMap<String, Object> GlobalParams, double externalCurrencyRate) throws ServiceException, AccountingException {
        HashSet rows = new HashSet();
        List ll = new ArrayList();
        try {
            double totalAmount = 0.0;
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            for (int i = 0; i < jArr.length(); i++) {
                double rowAmount = 0;
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> sodDataMap = new HashMap<String, Object>();
                sodDataMap.put("srno", i + 1);
                sodDataMap.put(Constants.companyKey, companyid);
                sodDataMap.put("soid", salesOrder.getID());
                sodDataMap.put("rate", jobj.getDouble("rate"));//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
                sodDataMap.put("quantity", jobj.getDouble("quantity"));
                sodDataMap.put("credito", jobj.getString("creditoraccount"));
                sodDataMap.put("remark", jobj.optString("remark"));
                sodDataMap.put("productdetail", StringUtil.DecodeText(jobj.optString("productdetail")));
                if (jobj.has("prdiscount") && jobj.get("prdiscount") != null) {
                    sodDataMap.put("discount", jobj.getDouble("prdiscount"));
                }
                if (jobj.has("discountispercent") && jobj.get("discountispercent") != null) {
                    sodDataMap.put("discountispercent", jobj.getInt("discountispercent"));
                }
                String rowtaxid = jobj.getString("prtaxid");
                
                rowAmount = jobj.getDouble("rate") * jobj.getDouble("quantity");
                
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null) {
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    } else {
                        sodDataMap.put("rowtaxid", rowtaxid);
                        double rowtaxamount = StringUtil.getDouble(jobj.getString("taxamount"));
                        sodDataMap.put("rowTaxAmount", rowtaxamount);
                        rowAmount = rowAmount + rowtaxamount;
                    }
                    
                }
                //  row.setTax(rowtax);
                KwlReturnObject result = accSalesOrderDAOobj.saveBillingSalesOrderDetails(sodDataMap);
                BillingSalesOrderDetail row = (BillingSalesOrderDetail) result.getEntityList().get(0);
                rows.add(row);
                totalAmount += rowAmount;
            }
            ll.add(rows);
            ll.add(totalAmount);
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw ServiceException.FAILURE("saveBillingSalesOrderRows : " + ex.getMessage(), ex);
        }
        return ll;
    }
    
    public ModelAndView deleteBillingSalesOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteBillingSalesOrders(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.so.del", null, RequestContextUtils.getLocale(request));   //"Sales Order has been deleted successfully";
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public void deleteBillingSalesOrders(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String soid = StringUtil.DecodeText(jobj.optString("billid"));
                    accSalesOrderDAOobj.deleteBillingSalesOrder(soid, companyid);
                }
            }
        } /*catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }
    
    private HashSet<QuotationDetail> updateQuotationRows(String quoteDetails,int moduleid, String companyid) throws ServiceException, JSONException {
        HashSet<QuotationDetail> rows = new HashSet<>();
        try {
            JSONArray jArr = new JSONArray(quoteDetails);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                QuotationDetail row = null;
                if (jobj.has("rowid")) {
                    KwlReturnObject quoteDetailsResult = accountingHandlerDAOobj.getObject(QuotationDetail.class.getName(), jobj.getString("rowid"));
                    row = (QuotationDetail) quoteDetailsResult.getEntityList().get(0);
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
                        jcustomarray = fieldDataManagercntrl.getComboValueIdsForCurrentModule(jcustomarray, moduleid, companyid, 1);
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "QuotationDetail");
                        customrequestParams.put("moduleprimarykey", "QuotationDetailId");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put(Constants.moduleid, moduleid);
                        customrequestParams.put(Constants.companyKey, companyid);
                        DOMap.put(Constants.Acc_id, row.getID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_QuotationDetails_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            DOMap.put("qdetailscustomdataref", row.getID());
                            accSalesOrderDAOobj.updateQuotationDetailsCustomData(DOMap);
                        }
                    }

                    // Add Custom fields details for Product
                    if (!StringUtil.isNullOrEmpty(jobj.optString("productcustomfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("productcustomfield", "[]"));
                        HashMap<String, Object> quotationMap = new HashMap<String, Object>();
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "CqProductCustomData");
                        customrequestParams.put("moduleprimarykey", "CqDetailID");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put(Constants.moduleid, Constants.Acc_Customer_Quotation_ModuleId);
                        customrequestParams.put(Constants.companyKey, companyid);
                        quotationMap.put(Constants.Acc_id, row.getID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_CQDetail_Productcustom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            quotationMap.put("qdetailscustomdataref", row.getID());
                            accSalesOrderDAOobj.updateQuotationDetailsProductCustomData(quotationMap);
                        }
                    }
                    rows.add(row);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updateQuotationRows : " + ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
        return rows;
    }

    public List updateQuotation(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {
        String id = null;
        List ll = new ArrayList();
        ArrayList discountArr = new ArrayList();
        String soid = null;
        Quotation quote = null;
        Quotation quotation = null;
        try {
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            DateFormat df = authHandler.getDateOnlyFormat();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            soid = request.getParameter("invoiceid");
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
            int moduleid = isLeaseFixedAsset?Constants.Acc_Lease_Order_ModuleId:Constants.Acc_Customer_Quotation_ModuleId;
            String customfield = request.getParameter("customfield");
            /*
             * To update the following items which is not affecting the amount
             * and linking of the Invoice.
             */
            HashMap<String, Object> qDataMap = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(soid)) {
                qDataMap.put(Constants.Acc_id, soid); 
            }
            qDataMap.put("orderdate", df.parse(request.getParameter("billdate")));
            qDataMap.put("memo", request.getParameter("memo") == null ? "" : request.getParameter("memo"));
            qDataMap.put("billto", request.getParameter("billto") == null ? "" : request.getParameter("billto"));
            qDataMap.put("shipaddress", request.getParameter("shipaddress") == null ? "" : request.getParameter("shipaddress"));
            if (request.getParameter("shipdate") != null && !StringUtil.isNullOrEmpty(request.getParameter("shipdate"))) {
                qDataMap.put("shipdate", df.parse(request.getParameter("shipdate")));
            }
            qDataMap.put("customerporefno", request.getParameter("customerporefno") == null ? "" : request.getParameter("customerporefno"));
            qDataMap.put(Constants.companyKey, companyid);
            qDataMap.put("salesPerson", request.getParameter("salesPerson") == null ? "" : request.getParameter("salesPerson"));
            qDataMap.put("shipvia", request.getParameter("shipvia"));
            qDataMap.put("fob", request.getParameter("fob") == null ? "" : request.getParameter("fob"));
            qDataMap.put("modifiedby", sessionHandlerImpl.getUserid(request));
            qDataMap.put("updatedon", System.currentTimeMillis());
            qDataMap.put("posttext", request.getParameter("posttext"));
            qDataMap.put("shippingterm", request.getParameter("shippingterm"));
            qDataMap.put("costCenterId", request.getParameter("costcenter"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("deliveryTime"))) {
                qDataMap.put("deliveryTime", request.getParameter("deliveryTime"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("validdate"))) {
                qDataMap.put("validdate", df.parse(request.getParameter("validdate")));
            }else{
                qDataMap.put("validdate",null);
            }

            KwlReturnObject invResult = accountingHandlerDAOobj.getObject(Quotation.class.getName(), soid);
            quote = (Quotation) invResult.getEntityList().get(0);

            Map<String, Object> addressParams = new HashMap<>();
            String billingAddress = request.getParameter(Constants.BILLING_ADDRESS);
            if (!StringUtil.isNullOrEmpty(billingAddress)) {
                addressParams = AccountingAddressManager.getAddressParams(request, false);
            } else {
                addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(quote.getCustomer().getID(), companyid, accountingHandlerDAOobj);// addressParams = getCustomerDefaultAddressParams(customer,companyid);
            }
            BillingShippingAddresses bsa = quote.getBillingShippingAddresses();//used to update billing shipping addresses
            addressParams.put(Constants.Acc_id, bsa != null ? bsa.getID() : "");
            KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
            bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
            String addressid = bsa.getID();
            qDataMap.put("billshipAddressid", addressid);
            /*
             * Updating line item information.
             */
            String qDetails = request.getParameter("detail");
            HashSet<QuotationDetail> quoteDetails = updateQuotationRows(qDetails,moduleid, companyid);
            qDataMap.put("sodetails", quoteDetails);
            /*
             * Updating Custom field data.
             */
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_Quotation_modulename);
                customrequestParams.put("moduleprimarykey", "QuotationId");
                customrequestParams.put("modulerecid", quote.getID());
                customrequestParams.put(Constants.moduleid, isLeaseFixedAsset ? Constants.Acc_Lease_Quotation : Constants.Acc_Customer_Quotation_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_Quotation_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    qDataMap.put("accquotationcustomdataref", quote.getID());
                    KwlReturnObject accresult = accSalesOrderDAOobj.updateQuotationCustomData(qDataMap);
                }
            }
            KwlReturnObject result = accSalesOrderDAOobj.saveQuotation(qDataMap);
            quotation = (Quotation) result.getEntityList().get(0);
            id = quotation.getID();
            /*
             * Data for return information.
             */
            String personalid = quotation.getCustomer().getAccount().getID();
            String accname = quotation.getCustomer().getAccount().getName();
            String salesOrderNo = quotation.getQuotationNumber();
            String address = quotation.getCustomer().getBillingAddress();
            String fullShippingAddress = "";
            if (quotation.getBillingShippingAddresses() != null) {
                fullShippingAddress = quotation.getBillingShippingAddresses().getFullShippingAddress();
            }
            tranID = id;
            recId = salesOrderNo;
            ll.add(new String[]{id});
            ll.add(discountArr);
            ll.add("");
            ll.add(personalid);
            ll.add(accname);
            ll.add(salesOrderNo);
            ll.add(address);
            ll.add(quotation.getQuotationamount());
            ll.add("");
            ll.add(fullShippingAddress);
        } catch (ParseException | JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, e);
        }
        return ll;
    }

    public ModelAndView updateLinkedQuotation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isAccountingExe = false;
        String msg = "", channelName = "", invoiceid = "", deliveryOid = "", billNo = "", doinvflag = "";

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
            String billno = (String) li.get(5);
            String jeNumber = (String) li.get(8);
            invoiceid = (String)id[0];
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
            if (isLeaseFixedAsset) {
                channelName = "/LeaseQuotationReport/gridAutoRefresh";
            } else {
                channelName = "/SalesQuotationReport/gridAutoRefresh";
            }
            /*
             * Composing the message to display after save operation.
             */
            if (isLeaseFixedAsset) {
                msg = messageSource.getMessage("acc.leaseCustomerQuotation.update", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";
            } else {
                msg = messageSource.getMessage("acc.CustomerQuotation.update", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";
            }
            /*
             * Composing the message to insert into Audit Trail.
             */
            String action = "updated";
            if (isLeaseFixedAsset) {
                action += " Lease";
            }
            auditTrailObj.insertAuditLog(AuditAction.CUSTOMER_QUOTATION_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " Customer Quotation " + recId, request, tranID);

        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            isAccountingExe = true;
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
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
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView saveQuotation(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String channelName = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("locale", RequestContextUtils.getLocale(request));
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            paramJobj.put("baseUrl", baseUrl);
            String userName = sessionHandlerImpl.getUserFullName(request);
            paramJobj.put(Constants.username,userName);
            jobj = accSalesOrderServiceobj.saveQuotationJSON(paramJobj);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            channelName = jobj.optString(Constants.channelName, null);
            return new ModelAndView("jsonView", "model", jobj.toString());
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
                ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView deleteQuotations(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        String linkedQuotations = "";
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            requestJobj.put("servletContext", this.getServletContext());
            linkedQuotations = deleteQuotations(requestJobj);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedQuotations)) {
                msg = messageSource.getMessage("acc.field.CustomerQuotationhasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.CustomerQuotationsexcept", null, RequestContextUtils.getLocale(request)) + " " + linkedQuotations.substring(0, linkedQuotations.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            }
        } catch (JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteQuotations(JSONObject requestJobj) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(requestJobj.optString(Constants.RES_data));
            String companyid = requestJobj.optString(Constants.companyKey);
            String linkedQuotaions = "";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                linkedQuotaions = accSalesOrderServiceobj.deleteQuotation(jobj, requestJobj, companyid, linkedQuotaions);
            }
            return linkedQuotaions;
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
        }
    }

    public ModelAndView deleteQuotationsPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            requestJobj.put("servletContext", this.getServletContext());
            String linkedTransaction = deleteQuotationsPermanent(requestJobj);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                msg = messageSource.getMessage("acc.field.CustomerQuotationshasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.CustomerQuotationsexcept", null, RequestContextUtils.getLocale(request)) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            }
        } catch (JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteQuotationsPermanent(JSONObject requestJobj) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransaction = "";
        try {
            JSONArray jArr = new JSONArray(requestJobj.optString(Constants.RES_data));
            String companyid = requestJobj.optString(Constants.companyKey);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                linkedTransaction = accSalesOrderServiceobj.deleteQuotationPermanent(jobj, companyid, linkedTransaction, requestJobj);
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
        }
        return linkedTransaction;
    }
    
    public ModelAndView deleteQuotationVersions(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        String quotationVersions = "";
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            quotationVersions = deleteQuotationVersions(request);
            txnManager.commit(status);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(quotationVersions)) {
                msg = messageSource.getMessage("acc.field.CustomerQuotationsVersionhasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.CustomerQuotationsexcept", null, RequestContextUtils.getLocale(request)) + " " + quotationVersions.substring(0, quotationVersions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public String deleteQuotationVersions(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String quotationVersions = "";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    quotationVersions += "Quotation Version ID is null or empty" + ", ";
                    continue;
                }
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String versionID = StringUtil.DecodeText(jobj.optString("billid"));
                    auditTrailObj.insertAuditLog("74", " User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a Customer Quotation Version's " + versionID, request, versionID);
                    accSalesOrderDAOobj.deleteQuotationVersion(versionID, companyid);
                }
            }
            return quotationVersions;
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }
    
    public ModelAndView deleteQuotationVersionsPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String quotationVersion = deleteQuotationVersionsPermanent(request);
            txnManager.commit(status);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(quotationVersion)) {
                msg = messageSource.getMessage("acc.field.CustomerQuotationsVersionhasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.CustomerQuotationsexcept", null, RequestContextUtils.getLocale(request)) + " " + quotationVersion.substring(0, quotationVersion.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public String deleteQuotationVersionsPermanent(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        String quotationVersion = "";
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String versionID = "";
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                versionID = StringUtil.DecodeText(jobj.optString("billid"));
                
                if (StringUtil.isNullOrEmpty(versionID)) {
                    quotationVersion += "Quotation Version ID is null or empty" + ", ";
                    continue;
                }
                requestParams.put(Constants.companyKey, companyid);
                requestParams.put("versionid", versionID);
                if (!StringUtil.isNullOrEmpty(versionID)) {
                    accSalesOrderDAOobj.deleteQuotationVersionsPermanent(requestParams);
                    auditTrailObj.insertAuditLog("74", " User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a Customer Quotation Version Permanently " + versionID, request, versionID);
                    
                }
            }
        } /*catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return quotationVersion;
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
            archieveQuotations(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.CustomerQuotationhasbeenarchievedsuccessfully", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public void archieveQuotations(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String billid = request.getParameter("billid");
        String billno = request.getParameter("billno");
        accSalesOrderDAOobj.archieveQuotation(billid, companyid);
        auditTrailObj.insertAuditLog(AuditAction.Customer_Quotation, "User " + sessionHandlerImpl.getUserFullName(request) + " has Archive customer quotation " + billno, request, billid);
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
            unArchieveQuotations(request);
            txnManager.commit(status);
            issuccess = true;
//            msg = messageSource.getMessage("acc.so.quotacr", null, RequestContextUtils.getLocale(request));//"Quotation has been archieved successfully.";
            msg = messageSource.getMessage("acc.field.CustomerQuotationhasbeenarchievedsuccessfully", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public void unArchieveQuotations(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String billid = request.getParameter("billid");
        String billno = request.getParameter("billno");
        accSalesOrderDAOobj.unArchieveQuotation(billid, companyid);
        auditTrailObj.insertAuditLog(AuditAction.Customer_Quotation, "User " + sessionHandlerImpl.getUserFullName(request) + " has Un-Archive customer quotation " + billno, request, billid);
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
            txnManager.commit(status);
            issuccess = true;
            auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER, "User " + sessionHandlerImpl.getUserFullName(request) + " has Approved a Sales Order  " + billno, request, billno);
            msg = messageSource.getMessage("acc.field.Salesorderhasbeenupdatedsuccessfully", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
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
            KwlReturnObject billingSalesOrderObj = accountingHandlerDAOobj.getObject(BillingSalesOrder.class.getName(), billid);
            BillingSalesOrder billingSalesOrder = (BillingSalesOrder) billingSalesOrderObj.getEntityList().get(0);
            invoiceNumber = billingSalesOrder.getSalesOrderNumber();
            if (billingSalesOrder.getPendingapproval() == billingSalesOrder.getApprovallevel()) {
                isSendMailForNextLevelUsers = false;
            }
        } else {
            KwlReturnObject salesOrderObj = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), billid);
            SalesOrder salesOrder = (SalesOrder) salesOrderObj.getEntityList().get(0);
            invoiceNumber = salesOrder.getSalesOrderNumber();
            if (salesOrder.getPendingapproval() == salesOrder.getApprovallevel()) {
                isSendMailForNextLevelUsers = false;
            }
        }
        
        int approvedLevel = accSalesOrderDAOobj.approvePendingOrder(billid, isbilling, companyid, userid);
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("transtype", Constants.SALES_ORDER);
        hashMap.put("transid", billid);
        hashMap.put("approvallevel", approvedLevel);
        hashMap.put("remark", remark);
        hashMap.put("userid", userid);
        hashMap.put(Constants.companyKey, companyid);
        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(),companyid);
        Company company = (Company) returnObject.getEntityList().get(0);
        String fromEmailId = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
        if (isSendMailForNextLevelUsers && preferences.isSendapprovalmail()) { //this only for level 2. we aleady check is pending level and approve level are same or not
            String[] emails = {};
            String userName = sessionHandlerImpl.getUserFullName(request);
            String moduleName = "Sales Order";
            emails = accountingHandlerDAOobj.getApprovalUserList(request, moduleName, 2);//Leval value hard coded as 2
            if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
            }
            //String fromEmailId = Constants.ADMIN_EMAILID;
            /**
             * parameters required for sending mail
             */
            Map<String, Object> mailParameters = new HashMap();
            mailParameters.put(Constants.companyid, companyid);
            mailParameters.put(Constants.prNumber, invoiceNumber);
            mailParameters.put(Constants.modulename, moduleName);
            mailParameters.put(Constants.fromName, userName);
            mailParameters.put(Constants.fromEmailID, fromEmailId);
            mailParameters.put(Constants.PAGE_URL, baseUrl); 
            mailParameters.put(Constants.emails, emails); 
            accountingHandlerDAOobj.sendApprovalEmails(mailParameters);
        } else if (preferences.isSendapprovalmail()) {
            String[] emails = {};
            String userName = sessionHandlerImpl.getUserFullName(request);
            String moduleName = "Sales Order";
            String approvalpendingStatusmsg = "";
            emails = accountingHandlerDAOobj.getApprovalUserList(request, moduleName, 2);//Leval value hard coded as 2
            if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
            }
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put(Constants.companyKey, companyid);
            qdDataMap.put("level", approvedLevel);
            qdDataMap.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
            if (approvedLevel < 11) {
                approvalpendingStatusmsg=commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
            Map<String, Object> mailParameters = new HashMap();
            mailParameters.put("Number", invoiceNumber);
            mailParameters.put("userName", userName);
            mailParameters.put("emails", emails);
            mailParameters.put("sendorInfo", fromEmailId);
            mailParameters.put("moduleName", moduleName);
            mailParameters.put("addresseeName", "All");
            mailParameters.put("companyid", company.getCompanyID());
            mailParameters.put("baseUrl", baseUrl);
            mailParameters.put("approvalstatuslevel", approvedLevel);
            mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
            if (emails.length > 0) {
                accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
            }
        }
        
    }
    
    public ModelAndView updateFavourite(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        
        try {
            KwlReturnObject result = null;
            HashMap<String, Object> soDataMap = new HashMap<String, Object>();
            boolean withInventory = Boolean.parseBoolean(request.getParameter("withInv"));
            boolean quotationFlag = Boolean.parseBoolean(request.getParameter("quotationFlag"));
            String invoiceid = request.getParameter("invoiceid");
            
            KwlReturnObject soresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), invoiceid); // (Tax)session.get(Tax.class, taxid);
            SalesOrder salesOrder = (SalesOrder) soresult.getEntityList().get(0);
            if (salesOrder != null) {
                soDataMap.put("isOpeningBalanceOrder", salesOrder.isIsOpeningBalanceSO());
            }
            String companyid = sessionHandlerImpl.getCompanyid(request);
            soDataMap.put(Constants.companyKey, companyid);
            soDataMap.put("orderdate", new Date(request.getParameter("date")));
            soDataMap.put(Constants.Acc_id, invoiceid);
            soDataMap.put("isfavourite", request.getParameter("isfavourite"));
            if (!StringUtil.isNullOrEmpty(invoiceid)) {
                if (withInventory) {
                    result = accSalesOrderDAOobj.saveBillingSalesOrder(soDataMap);
                } else {
                    if (quotationFlag) {
                        result = accSalesOrderDAOobj.saveQuotation(soDataMap);
                    } else {
                        result = accSalesOrderDAOobj.saveSalesOrder(soDataMap);
                    }
                }
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView updatePrint(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        
        try {
            KwlReturnObject result = null;
            
            boolean withInventory = Boolean.parseBoolean(request.getParameter("withInv"));
            boolean quotationFlag = Boolean.parseBoolean(request.getParameter("quotationFlag"));
            String recordids = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("recordids"))) {
                recordids = request.getParameter("recordids");
            }
            ArrayList<String> SOIDList = CustomDesignHandler.getSelectedBillIDs(recordids);
            for (int cnt = 0; cnt < SOIDList.size(); cnt++) {
                HashMap<String, Object> soDataMap = new HashMap<String, Object>();
                soDataMap.put(Constants.Acc_id, SOIDList.get(cnt));
                soDataMap.put("isprinted", request.getParameter("isprinted"));
                if (!StringUtil.isNullOrEmpty(SOIDList.get(cnt))) {
                    if (withInventory) {
                        result = accSalesOrderDAOobj.saveBillingSalesOrder(soDataMap);
                    } else {
                        if (quotationFlag) {
                            result = accSalesOrderDAOobj.saveQuotation(soDataMap);
                        } else {
                            result = accSalesOrderDAOobj.saveSalesOrder(soDataMap);
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
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView activateDeactivateRecurringSO(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            msg = activateDeactivateRecurringSO(request);
            txnManager.commit(status);
            issuccess = true;
            //msg = messageSource.getMessage("acc.je1.updt", null, RequestContextUtils.getLocale(request));   //"Journal Entry has been updated successfully";
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, "accSalesOrderController.activateDeactivateRecurringSO", ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, "accSalesOrderController.activateDeactivateRecurringSO", ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (Exception ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, "accSalesOrderController.activateDeactivateRecurringSO", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public String activateDeactivateRecurringSO(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        String msg = "";
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("repeatedid"))) {
                    String repeateid  = jobj.getString("repeatedid");
                    boolean isactivate = jobj.optBoolean("isactivate");
                    boolean ispendingapproval = jobj.optBoolean("ispendingapproval");
                    String billno = jobj.getString("billno");
                    if(ispendingapproval){
                        accSalesOrderDAOobj.approveRecurringSO(repeateid, false);    //Invoice Approved here
                        msg = "Recurring SO has been approved successfully.";
                    } else {
                        accSalesOrderDAOobj.activateDeactivateRecurringSO(repeateid, isactivate);
                        msg = messageSource.getMessage("acc.SOList.RecurringSOhasbeenupdatedsuccessfully", null, RequestContextUtils.getLocale(request));  
                        auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER, messageSource.getMessage("acc.field.User", null, RequestContextUtils.getLocale(request)) + sessionHandlerImpl.getUserFullName(request) + " " +messageSource.getMessage("acc.field.has", null, RequestContextUtils.getLocale(request)) + " " +(isactivate? messageSource.getMessage("acc.field.Deactivated", null, RequestContextUtils.getLocale(request)):messageSource.getMessage("acc.field.Activated", null, RequestContextUtils.getLocale(request))) + " " +messageSource.getMessage("acc.repeatedSO.recInv", null, RequestContextUtils.getLocale(request)) +" " +billno, request, billno);
                    }
                }//if
            }//for            
        } catch(Exception ex){//try
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, "accSalesOrderController.activateDeactivateRecurringSO", ex);
        }//catch
        return msg;
    }//method-end
        //Send notification mail on set of Recurring Sales Order
    public void SendMail(HashMap requestParams) throws ServiceException {
        String loginUserId = (String) requestParams.get("loginUserId");
        User user = (User) accSalesOrderDAOobj.getUserObject(loginUserId);
        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), user.getCompany().getCompanyID());
        Company company = (Company) returnObject.getEntityList().get(0);
        String sendorInfo = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        boolean ispendingapproval = (Boolean) requestParams.get("ispendingapproval");
        String billno = (String) requestParams.get("billno");
        String cEmail = user.getEmailID() != null ? user.getEmailID() : "";
        if (!StringUtil.isNullOrEmpty(cEmail)) {
            try {
                String subject = "Recurring Sales Order Status Notification";
                //String sendorInfo = "admin@deskera.com";
                String htmlTextC = "";
                htmlTextC += "<br/>Hello " + user.getFirstName() + "<br/>";
                if (ispendingapproval) {
                    htmlTextC += "<br/>Sales Order <b>\"" + billno + "\"</b> has been set recurring successfully. <br/></br>";
                } else {
                    htmlTextC += "<br/>Sales Order <b>\"" + billno + "\"</b> has been set recurring successfully. <br/><br/>";
                }
                htmlTextC += "<br/>Regards,<br/>";
                htmlTextC += "<br/>ERP System<br/>";
                htmlTextC += "<br/><br/>";
                htmlTextC += "<br/>This is an auto generated email. Do not reply<br/>";

                String plainMsgC = "";
                plainMsgC += "\nHello " + user.getFirstName() + "\n";
                if (ispendingapproval) {
                    plainMsgC += "\nSales Order <b>\"" + billno + "\"</b> has been set recurring successfully. \n\n";
                } else {
                    plainMsgC += "\nSales Order <b>\"" + billno + "\"</b> has been set recurring successfully. \n\n";
                }
                plainMsgC += "\nRegards,\n";
                plainMsgC += "\nDeskera Financials\n";
                plainMsgC += "\n\n";
                plainMsgC += "\nThis is an auto generated email. Do not reply.\n";

                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                SendMailHandler.postMail(new String[]{cEmail}, subject, htmlTextC, plainMsgC, sendorInfo, smtpConfigMap);
            } catch (Exception ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        }//sendMail
    
    public void sendConsignmentApprovalEmails(HttpServletRequest request,User sender, SalesOrder so,String billno,boolean isApproved,boolean isEdit) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        String sendorInfo = sender.getEmailID()!=null?sender.getEmailID():authHandlerDAOObj.getSysEmailIdByCompanyID(so.getCompany().getCompanyID());
        Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), sender.getCompany().getCompanyID());
        
        boolean isMailForOlympus=(company.getSubDomain().equalsIgnoreCase("olympus2") || company.getSubDomain().equalsIgnoreCase("olympus3"))? true : false;
        List<String> customFieldRemarkList=new ArrayList<String>();
        String purposeOfRequestValue="";
        if (isMailForOlympus) {  // this is hardcoded check for olympus as per mentioned in ERP-22631 as this is required for olympus only
            
            String reQValue=request.getParameter("Custom_Purpose Of Request");
            if(!StringUtil.isNullOrEmpty(reQValue)){
                KwlReturnObject cusObj = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), reQValue);
                FieldComboData fieldCombo = (FieldComboData) cusObj.getEntityList().get(0);
                if(fieldCombo != null){
                    purposeOfRequestValue=fieldCombo.getValue();
                }
            }
            
            String dtl=request.getParameter("detail");
            JSONArray jArr = new JSONArray();
            if(!StringUtil.isNullOrEmpty(dtl)){
                jArr = new JSONArray(dtl);
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jCustomObj = jArr.getJSONObject(i);
                String customfield = jCustomObj.getString("customfield");
                if (!StringUtil.isNullOrEmpty(customfield)) {
                
                    JSONArray jcustomarray = new JSONArray(customfield);

                    if (jcustomarray != null) {
                        for (int x = 0; x < jcustomarray.length(); x++) {
                            JSONObject fieldObj = jcustomarray.getJSONObject(x);
                            if (fieldObj.has(Constants.Acc_custom_field)) {
                                String fieldname = fieldObj.getString(Constants.Acc_custom_field);
                                String fielddbname = fieldObj.getString(fieldname);
                                String fieldValue = fieldObj.getString(fielddbname);
                                
                                if(fieldname.equalsIgnoreCase("Custom_Remark")){
                                    customFieldRemarkList.add(fieldValue);
                                }

                            }
                        }
                    }

                }
            }
        }
        
        List ll;
        String msg = null, companyId, companyName;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        
        try {
            KwlReturnObject kwlobject = accountingHandlerDAOobj.getObject(Store.class.getName(), so.getRequestWarehouse().getId());
            Store store = (Store) kwlobject.getEntityList().get(0);
            Set<User> mgrSet=store.getStoreManagerSet();
            mgrSet.addAll(store.getStoreExecutiveSet());
            Map map = null;
            if (so != null && so.getRows() != null) {
                map = new HashMap();
                int sno = 1;
                ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
                List finalData = new ArrayList();
                List headerItems = new ArrayList();
                headerItems.add("No.");
                headerItems.add("Product ID");
                headerItems.add("Product Description");
                headerItems.add(isApproved?"Requested Quantity":"Quantity");
                if(isApproved){
                    headerItems.add("Approved Quantity");
                }
                headerItems.add("UoM");
                if (isMailForOlympus) {
                    headerItems.add("Remarks");
                }
                
                String emailIds = "";
                String mailSeparator = ",";
                String htmlText = "";

                boolean isfirst = true;

                for (SalesOrderDetail sod : so.getRows()) {
                    if(!isApproved && sod.getApproverSet() != null){
                        for (User user : sod.getApproverSet()) {
                            if (isfirst) {
                                emailIds += user.getEmailID();
                                isfirst = false;
                            } else {
                                emailIds += mailSeparator + user.getEmailID();
                            }

                        }
                    }else{
                        for (User user : mgrSet) {
                            if (isfirst) {
                                emailIds += user.getEmailID();
                                isfirst = false;
                            } else {
                                emailIds += mailSeparator + user.getEmailID();
                            }

                        }
                    }
                
                    List data = new ArrayList();
                    data.add(sno);
                    data.add(sod.getProduct().getProductid()); //product code
                    data.add(sod.getProduct().getDescription()); //product Desc
                    data.add(sod.getQuantity()); //quantity
                    if(isApproved){
                        data.add(sod.getApprovedQuantity());
                    }
                    data.add(sod.getUom()!=null?sod.getUom().getNameEmptyforNA():"");
                    if(isMailForOlympus && customFieldRemarkList != null && !customFieldRemarkList.isEmpty()){
                        data.add(customFieldRemarkList.get(sno-1));
                    }
                    finalData.add(data);
                    sno++;
                }
                
                if (sno > 1) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        if("Product Description".equalsIgnoreCase(a)){
                            headerprop.setWidth("200px");
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
                    htmlText = htmlText.concat(tablehtml);
                }
                
                String subject = "";
                subject = isApproved?"Request Approval Notification":"Request Notification";
                String htmlTextC = "";
                htmlTextC += "<br/>Hi,<br/>";
                if(!isApproved){
                   htmlTextC += "<br/>User <b>" + sender.getFullName() + "</b> has created  a new Consignment Request <b>" + billno + "</b>.<br/>";
                }else{
                   htmlTextC += "<br/>User <b>" + sender.getFullName() + "</b> has approved Consignment Request <b>" + billno + "</b>.<br/>";
                }
                
                KwlReturnObject result = accountingHandlerDAOobj.getNotifications(company.getCompanyID());
                List<NotificationRules> list = result.getEntityList();
                Iterator<NotificationRules> itr=list.iterator();
                while(itr.hasNext()){
                    NotificationRules nr=itr.next();
                    if(nr != null && nr.getModuleId() == 201){
                        if((isApproved && Integer.parseInt(nr.getFieldid()) == 27) || (!isApproved && !isEdit && Integer.parseInt(nr.getFieldid()) == 25) || (!isApproved && isEdit && Integer.parseInt(nr.getFieldid()) == 26)){
                            subject= nr.getMailsubject();
                            htmlTextC =  nr.getMailcontent();
                            
                            subject=subject.replaceAll("#Customer_Alias#", so.getCustomer().getAliasname()==null?"":so.getCustomer().getAliasname());
                            subject=subject.replaceAll("#Sales_Person#", so.getSalesperson() != null ? so.getSalesperson().getValue() : "");
                            subject=subject.replaceAll("#Document_Number#", billno);
                            htmlTextC=htmlTextC.replaceAll("#Document_Number#", billno);
                            htmlTextC=htmlTextC.replaceAll("#User_Name#", sender.getFullName());
                            
                            if (isApproved && nr.isMailToSalesPerson()) {
                                MasterItem mi = so.getSalesperson();
                                if (mi != null) {
                                    emailIds += mailSeparator + mi.getEmailID();
                                }
                            }
                            break;
                        }
                    }
                }
                
                
                htmlTextC += "<br/><b>Customer Name :</b> "+so.getCustomer().getName() + "</b>";
                htmlTextC += "<br/><b>From Date :</b>     "+df.format(so.getFromdate()) + "</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>To Date :</b>       "+df.format(so.getTodate()) + "</b>";
                htmlTextC += "<br/><b>Store :</b>         " + store.getFullName() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Location :</b>      " + so.getRequestLocation().getName() + "</b>";
                if(isMailForOlympus){
                    htmlTextC += "<br/><b>Purpose of Request :</b>      " + purposeOfRequestValue + "</b>";
                }
                htmlTextC += "<br/><b>Memo :</b>      " + so.getMemo() + "</b>";
                htmlTextC += "<br/><br/>" + htmlText;
//                htmlTextC += "<br/>This is an auto generated email. Do not reply.<br/>";
                htmlTextC += "<br/><br/>Regards,<br/>";
                htmlTextC += "<br/>ERP System<br/>";
                htmlTextC += "<br/><br/>";
                htmlTextC += "<br/>This is an auto generated email. Do not reply.<br/>";
                String plainMsgC = "";
                plainMsgC += "\nRegards,\n";
                plainMsgC += "\nDeskera Financials\n";
                plainMsgC += "\n\n";
                plainMsgC += "\nThis is an auto generated email. Do not reply.\n";
                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                SendMailHandler.postMail(emailIds.split(","), subject, htmlTextC, plainMsgC, sendorInfo, smtpConfigMap);
            }

        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
   
    public ModelAndView deleteRecurringSalesOrder(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("data", request.getParameter("data"));
            requestParams.put("locale", RequestContextUtils.getLocale(request));
            
            //Below Params Added for Audit Trial Entry
            requestParams.put("reqHeader", request.getHeader("x-real-ip"));
            requestParams.put("remoteAddress", request.getRemoteAddr());
            requestParams.put("userid", sessionHandlerImpl.getUserid(request));
            requestParams.put("userFullName", sessionHandlerImpl.getUserFullName(request));

            msg = deleteRecurringSalesOrder(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, "accSalesOrderController.deleteRecurringSalesOrder", ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (Exception ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, "accSalesOrderController.deleteRecurringSalesOrder", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteRecurringSalesOrder(HashMap<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Delete_RSO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        Locale locale = null;
        if (requestParams.containsKey("locale")) {
            locale = (Locale) requestParams.get("locale");
        }
        try {
            String nonDeletedRepeatedSOs = "";
            JSONArray dataArray = new JSONArray();
            String userFullName = (String) requestParams.get("userFullName");
            dataArray = new JSONArray((String) requestParams.get("data"));
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject obj = dataArray.getJSONObject(i);
                String invoicenumber = obj.optString("invoicenumber", "");
                String invoiceid = obj.getString("invoiceid");
                String repeateid = obj.getString("repeatedid");
                try {
                    status = txnManager.getTransaction(def);

                    //repeatSO is foreign key in Sales Order. so setting it null for removing dependency before deleting Repeated SO 
                    accSalesOrderDAOobj.updateToNullRepeatedSOOfSalesOrder(invoiceid, repeateid);

                    //Deleting entry from RepeateSOMemo as it is redundant after deleting RepeatedSO
                    accSalesOrderDAOobj.DelRepeateSOMemo(repeateid);

                    //Finally Deleting Repeated SO Record From Recurring / Pending Recurring Tab
                    accSalesOrderDAOobj.deleteRepeatedSO(repeateid);

                    auditTrailObj.insertAuditLog(AuditAction.REPEATED_SO_DELETE, "User " + userFullName + " has deleted a recurring Sales Order " + invoicenumber, requestParams, repeateid);
                    txnManager.commit(status);
                } catch (Exception ex) {
                    nonDeletedRepeatedSOs += nonDeletedRepeatedSOs.equals("") ? invoicenumber : "," + invoicenumber;
                    txnManager.rollback(status);
                    Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (!StringUtil.isNullOrEmpty(nonDeletedRepeatedSOs)) {
                msg = "Except Rerord(s) " + nonDeletedRepeatedSOs + " all selected records have been deleted successfully.";
            } else {
                msg = messageSource.getMessage("acc.commo.Allselectedrecord(s)havebeendeletedsuccessfully", null, locale);
            }

        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("deleteRecurringSORule: " + ex.getMessage(), ex);
        }
        return msg;
    }
    
    /*
     * function to save Sales Order Status for Purchase Order
     */
    public ModelAndView saveSalesOrderStatusForPO(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        String message = "";
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            String salesOrderId = (String) request.getParameter("billid");
            String status = (String) request.getParameter("status");
            String auditStatus = "";

            HashMap requestparams = new HashMap();
            requestparams.put("salesOrderID", salesOrderId);
            requestparams.put("status", status);

            if (status.equalsIgnoreCase("Open")) {
                auditStatus = " Blocked ";
            } else {
                auditStatus = " Unblocked ";
            }

            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), salesOrderId);
            SalesOrder salesorder = (SalesOrder) rdresult.getEntityList().get(0);
            String salesOrderNo = salesorder.getSalesOrderNumber();
            requestparams.put("salesOrderNo", salesOrderNo);
            requestparams.put("locale", RequestContextUtils.getLocale(request));
            KwlReturnObject result = accSalesOrderDAOobj.saveSalesOrderStatusForPO(requestparams);
            message = result.getMsg();
            issuccess = true;
            auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER_BLOCKED_UNBLOCKED, "User " + sessionHandlerImpl.getUserFullName(request) + " has been " + auditStatus + " Sales Order " + salesOrderNo + " For Purchase Order ", request, salesOrderId);
        } catch (Exception ex) {
            message = "accSalesOrderController.saveSalesOrderStatusForPO:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, message);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     
    /*
     * function to close Sales Order manually if it is no longer required
     */
    public ModelAndView closeDocument(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String message = "";
        String companyid = sessionHandlerImpl.getCompanyid(request);
        try {
            String salesOrderId = (String) request.getParameter("billid");
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), salesOrderId);
            SalesOrder salesorder = (SalesOrder) rdresult.getEntityList().get(0);
            HashMap requestparams = new HashMap();
            requestparams.put("salesOrder", salesorder);
            requestparams.put("closeFlag", true);
            Set<SalesOrderDetail> orderDetail = salesorder.getRows();
            boolean isSoAllowToClose = true;
            if (salesorder.getLinkflag() == 1) {//checking SO is used in invoice
                 /* SO is allowing to close if SO is used partially in SI & that SI is used in DO fully or partially*/
                if (salesorder.isIsopen()) {
                    for (SalesOrderDetail salesOrderDetail : orderDetail) {
                        KwlReturnObject doresult = accSalesOrderDAOobj.checkWhetherSOIsUsedInDOOrNot(salesOrderDetail.getID(), companyid);
                        List list1 = doresult.getEntityList();
                        if (list1.size() > 0) {
                            isSoAllowToClose = false;
                            break;
                        }
                        if (isSoAllowToClose) {
                            jobj.put(Constants.RES_success, false);
                            jobj.put(Constants.RES_msg, "Sales Order are used in invoice.So you cannot close it Manually");
                            return new ModelAndView("jsonView", "model", jobj.toString());
                        }
                    }
                } else {
                    /* If SO is fully used in Invoice then it is not allowing to close*/
                    jobj.put(Constants.RES_success, false);
                    jobj.put(Constants.RES_msg, "Sales Order are completely used in invoice.So you cannot close it Manually");
                     return new ModelAndView("jsonView", "model", jobj.toString());
                    

                }

            }
            requestparams.put("locale", RequestContextUtils.getLocale(request));
            KwlReturnObject result = accSalesOrderDAOobj.closeDocument(requestparams);
            message = result.getMsg();
            issuccess = true;
            auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER_CLOSED_MANUALLY, messageSource.getMessage("acc.field.User", null, RequestContextUtils.getLocale(request)) + sessionHandlerImpl.getUserFullName(request) +" " +messageSource.getMessage("acc.field.hasClosedSalesOrder", null, RequestContextUtils.getLocale(request))+ " " +salesorder.getSalesOrderNumber(),request, salesorder.getID());
            
            /* Lock Quantity is releasing at the time of closing SO mamually for remainign quantity those are not used*/
            if (salesorder.isLockquantityflag()) {
                for (SalesOrderDetail salesOrderDetail : orderDetail) {
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("salesOrderDetail", salesOrderDetail.getID());
                    hashMap.put(Constants.companyKey, companyid);
                    releaseLockQuantityOfSOAfterClosedManually(hashMap);

                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, message);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /*
     * function to close Line level product used in Sales Order manually, if it is no longer required
     */
    public ModelAndView closeLineItem(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String message = "";
        String companyid = sessionHandlerImpl.getCompanyid(request);

        try {
            String soDetailId = (String) request.getParameter("DetailId");
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), soDetailId);
            SalesOrderDetail salesorderDetail = (SalesOrderDetail) rdresult.getEntityList().get(0);
            HashMap requestparams = new HashMap();
            requestparams.put("salesorderDetail", salesorderDetail);
            SalesOrder salesOrder = salesorderDetail.getSalesOrder();
            requestparams.put("salesOrder", salesOrder);
            Set<SalesOrderDetail> orderDetail = salesOrder.getRows();
            boolean isSoAllowToClose = true;
            
            if (salesOrder.getLinkflag() == 1) {//checking SO is used in invoice
               /* SO is allowing to close if SO is used partially in SI & that SI is used in DO fully or partially*/
                if (salesOrder.isIsopen()) {
                    for (SalesOrderDetail salesOrderDetail : orderDetail) {
                        KwlReturnObject doresult = accSalesOrderDAOobj.checkWhetherSOIsUsedInDOOrNot(salesOrderDetail.getID(), companyid);
                        List list1 = doresult.getEntityList();
                        if (list1.size() > 0) {
                            isSoAllowToClose = false;
                            break;
                        }
                        if (isSoAllowToClose) {
                            jobj.put(Constants.RES_success, false);
                            jobj.put(Constants.RES_msg, "Sales Order are used in invoice.So you cannot close it Manually");
                            return new ModelAndView("jsonView", "model", jobj.toString());
                        }
                    }
                } else {
                     /* If SO is fully used in Invoice then it is not allowing to close*/
                    jobj.put(Constants.RES_success, false);
                    jobj.put(Constants.RES_msg, "Sales Order are completely used in invoice.So you cannot close it Manually");
                    return new ModelAndView("jsonView", "model", jobj.toString());

                }

            }
         

           /* Closing Line item*/
            KwlReturnObject result = accSalesOrderDAOobj.closeLineItem(requestparams);
            message = result.getMsg();
            issuccess = true;
            
            /* Closing SO if all line level item is being closed*/
            boolean isSoClosed = true;
            for (SalesOrderDetail salesOrderDetail : orderDetail) {
                if (!salesOrderDetail.isIsLineItemClosed()) {
                    /*If line level is not closed manually & it is used in transaction then it will no longer load in transaction*/
                    if (salesOrderDetail.getBalanceqty() != 0) {
                        isSoClosed = false;
                        break;
                    }
                }
            }
            
            if (isSoClosed) {
                 requestparams.put("closeFlag", true);
                result = accSalesOrderDAOobj.closeDocument(requestparams);
                auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER_CLOSED_MANUALLY, messageSource.getMessage("acc.field.User", null, RequestContextUtils.getLocale(request)) + sessionHandlerImpl.getUserFullName(request) + " " +messageSource.getMessage("acc.field.hasClosedSalesOrder", null, RequestContextUtils.getLocale(request))+ " " +salesOrder.getSalesOrderNumber(),request, salesOrder.getID());
                auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER_CLOSED_MANUALLY, messageSource.getMessage("acc.field.User", null, RequestContextUtils.getLocale(request)) + sessionHandlerImpl.getUserFullName(request) + " " +messageSource.getMessage("acc.field.has", null, RequestContextUtils.getLocale(request))+" "+messageSource.getMessage("acc.field.manually", null, RequestContextUtils.getLocale(request))+" " +messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request))+" "+salesorderDetail.getProduct().getName()+" "+messageSource.getMessage("acc.common.in", null, RequestContextUtils.getLocale(request))+" "+messageSource.getMessage("acc.wtfTrans.so", null, RequestContextUtils.getLocale(request))+" "+salesOrder.getSalesOrderNumber(),request, salesOrder.getID());
            }else{
                auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER_CLOSED_MANUALLY, messageSource.getMessage("acc.field.User", null, RequestContextUtils.getLocale(request)) + sessionHandlerImpl.getUserFullName(request) + " " +messageSource.getMessage("acc.field.has", null, RequestContextUtils.getLocale(request))+" "+ messageSource.getMessage("acc.field.manually", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request)) + " " + salesorderDetail.getProduct().getName() + " " + messageSource.getMessage("acc.common.in", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.wtfTrans.so", null, RequestContextUtils.getLocale(request)) + " " + salesOrder.getSalesOrderNumber(), request, salesOrder.getID());
            }
            
            /* Lock Quantity is releasing at the time of closing SO mamually for remainign quantity those are not used*/
            if (salesOrder.isLockquantityflag()) {

                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("salesOrderDetail", soDetailId);
                hashMap.put(Constants.companyKey, companyid);
                releaseLockQuantityOfSOAfterClosedManually(hashMap);

            }
            
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, message);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /*
     * function to reject Line level product used in Consignment request manually, if it is not issued
     */
    public ModelAndView rejectLineItem(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String message = "";
         DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Delete_RSO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        try {
            status=txnManager.getTransaction(def);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String soDetailId = (String) request.getParameter("DetailId");
            String reason = (String) request.getParameter("reason");
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), soDetailId);
            SalesOrderDetail salesorderDetail = (SalesOrderDetail) rdresult.getEntityList().get(0);
            HashMap requestparams = new HashMap();
            requestparams.put("salesorderDetail", salesorderDetail);
            requestparams.put("reason", reason);
            SalesOrder salesOrder = salesorderDetail.getSalesOrder();
            requestparams.put("salesOrder", salesOrder);
            Set<SalesOrderDetail> orderDetail = salesOrder.getRows();
            boolean isSoAllowToClose = true;
            
            if (salesOrder.getLinkflag() == 1) {//checking SO is used in invoice
               /* SO is allowing to close if SO is used partially in SI & that SI is used in DO fully or partially*/
                if (salesOrder.isIsopen()) {
                    for (SalesOrderDetail salesOrderDetail : orderDetail) {
                        KwlReturnObject doresult = accSalesOrderDAOobj.checkWhetherSOIsUsedInDOOrNot(salesOrderDetail.getID(), companyid);
                        List list1 = doresult.getEntityList();
                        if (list1.size() > 0) {
                            isSoAllowToClose = false;
                            break;
                        }
                        if (isSoAllowToClose) {
                            jobj.put(Constants.RES_success, false);
                            jobj.put(Constants.RES_msg, "Sales Order are used in invoice.So you cannot Reject Item");
                            return new ModelAndView("jsonView", "model", jobj.toString());
                        }
                    }
                } else {
                     /* If SO is fully used in Invoice then it is not allowing to close*/
                    jobj.put(Constants.RES_success, false);
                    jobj.put(Constants.RES_msg, "Sales Order are completely used in invoice.So you cannot Reject Item");
                    return new ModelAndView("jsonView", "model", jobj.toString());

                }

            }
         

           /* Closing Line item*/
            KwlReturnObject result = accSalesOrderDAOobj.rejectLineItem(requestparams);
            message = result.getMsg();
            issuccess = true;
            
            /* Closing SO if all line level item is being closed*/
            boolean isSoClosed = true;
            for (SalesOrderDetail salesOrderDetail : orderDetail) {
                if (!(salesOrderDetail.isIsLineItemClosed() || salesOrderDetail.isIsLineItemRejected())) {
                    /*If line level is not closed manually & it is used in transaction then it will no longer load in transaction*/
                    if (salesOrderDetail.getBalanceqty() != 0) {
                        isSoClosed = false;
                        break;
                    }
                }
            }
            
            if (isSoClosed) {
                 requestparams.put("closeFlag", true);
                accSalesOrderDAOobj.closeDocument(requestparams);
                auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER_CLOSED_MANUALLY, messageSource.getMessage("acc.field.User", null, RequestContextUtils.getLocale(request)) + sessionHandlerImpl.getUserFullName(request) + " " +messageSource.getMessage("acc.field.hasClosedSalesOrder", null, RequestContextUtils.getLocale(request))+ " " +salesOrder.getSalesOrderNumber(),request, salesOrder.getID());
                auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER_CLOSED_MANUALLY, messageSource.getMessage("acc.field.User", null, RequestContextUtils.getLocale(request)) + sessionHandlerImpl.getUserFullName(request) + " " +messageSource.getMessage("acc.field.has", null, RequestContextUtils.getLocale(request))+" "+messageSource.getMessage("acc.field.manually", null, RequestContextUtils.getLocale(request))+" " +messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request))+" "+salesorderDetail.getProduct().getName()+" "+messageSource.getMessage("acc.common.in", null, RequestContextUtils.getLocale(request))+" "+messageSource.getMessage("acc.wtfTrans.so", null, RequestContextUtils.getLocale(request))+" "+salesOrder.getSalesOrderNumber(),request, salesOrder.getID());
            }else{
                auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER_CLOSED_MANUALLY, messageSource.getMessage("acc.field.User", null, RequestContextUtils.getLocale(request)) + sessionHandlerImpl.getUserFullName(request) + " " +messageSource.getMessage("acc.field.has", null, RequestContextUtils.getLocale(request))+" "+ messageSource.getMessage("acc.field.rejected", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request)) + " " + salesorderDetail.getProduct().getName() + " " + messageSource.getMessage("acc.common.in", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.wtfTrans.so", null, RequestContextUtils.getLocale(request)) + " " + salesOrder.getSalesOrderNumber(), request, salesOrder.getID());
            }
            
            /* Lock Quantity is releasing at the time of closing SO mamually for remainign quantity those are not used*/
            if (salesOrder.isLockquantityflag()) {

                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("salesOrderDetail", soDetailId);
                hashMap.put(Constants.companyKey, companyid);
                releaseLockQuantityOfSOAfterClosedManually(hashMap);

            }
            
            txnManager.commit(status);
        } catch (Exception ex) {
             if (status != null) {
                txnManager.rollback(status);
            }
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, message);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public void releaseLockQuantityOfSOAfterClosedManually(HashMap<String, Object> requestParams) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String soDetailId = (String) requestParams.get("salesOrderDetail");

            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), soDetailId);
            SalesOrderDetail salesorderDetail = (SalesOrderDetail) rdresult.getEntityList().get(0);

            //code for making  availble serials which are not used:
            if (salesorderDetail != null) {

                Product product = salesorderDetail.getProduct();
                boolean isSerialForProduct = false;
                double soLockQuantity = salesorderDetail.getLockquantity();
                double soLockQuantitydue = salesorderDetail.getLockquantitydue();
                if (product != null) {
                    isSerialForProduct = product.isIsSerialForProduct();
                }

                accSalesOrderDAOobj.updatebatchlockQuantity(product.getID(), salesorderDetail.getID(), companyid);
                if (isSerialForProduct) {
                    accSalesOrderDAOobj.updateSerialslockQuantity(product.getID(), salesorderDetail.getID(), companyid);
                }
                if (soLockQuantity > 0) {
                    salesorderDetail.setLockquantity(salesorderDetail.getLockquantity() - soLockQuantity);                
                }
                if (soLockQuantitydue > 0) {
                    salesorderDetail.setLockquantitydue(salesorderDetail.getLockquantitydue() - soLockQuantitydue);
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("releaseLockQuantityOfSOAfterClosedManually: " + ex.getMessage(), ex);
        }
    }
    
    public ModelAndView importCustomerQuotations(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyId = sessionHandlerImpl.getCurrencyID(request);
            boolean typeXLSFile = (request.getParameter("typeXLSFile") != null) ? Boolean.parseBoolean(request.getParameter("typeXLSFile")) : false;
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", companyid);
            extraParams.put("Life", 10.0);
            extraParams.put("Salvage", 0.0);

            KwlReturnObject CompanyPrefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) CompanyPrefResult.getEntityList().get(0);

            KwlReturnObject extraPrefObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) extraPrefObj.getEntityList().get(0);

            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());
            requestParams.put("currencyId", currencyId);
            requestParams.put("importMethod", typeXLSFile ? "xls" : "csv");
            requestParams.put("bookbeginning", preferences.getBookBeginningFrom());
            requestParams.put("isCurrencyCode", extraPref.isCurrencyCode());
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            requestParams.put(Constants.language, RequestContextUtils.getLocale(request).getLanguage());
            requestParams.put(Constants.reqHeader, StringUtil.getIpAddress(request));
            requestParams.put(Constants.remoteIPAddress, request.getRemoteAddr());
            requestParams.put(Constants.PAGE_URL, URLUtil.getPageURL(request, loginpageFull));
            if (sessionHandlerImpl.getCompanySessionObj(request) != null) {
                requestParams.put(Constants.userfullname, sessionHandlerImpl.getCompanySessionObj(request).getUserfullname());
                requestParams.put(Constants.userdateformat, sessionHandlerImpl.getCompanySessionObj(request).getUserdateformat());
                requestParams.put(Constants.timezonedifference, sessionHandlerImpl.getCompanySessionObj(request).getTzdiff());
            }

            if (doAction.compareToIgnoreCase("import") == 0) {
//                System.out.println("A(( Import start : " + new Date());
                JSONObject datajobj = new JSONObject();
                JSONObject resjson = new JSONObject(request.getParameter("resjson").toString());
                JSONArray resjsonJArray = resjson.getJSONArray("root");

                String filename = request.getParameter("filename");
                datajobj.put("filename", filename);
                datajobj.put("isDraft", extraParams.optBoolean("isDraft", false));

                String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
                File filepath = new File(destinationDirectory + File.separator + filename);
                datajobj.put("FilePath", filepath);
                datajobj.put("resjson", resjsonJArray);

                String dateFormatId = request.getParameter("dateFormat");
                requestParams.put("dateFormat", dateFormatId);
                Date applyDate = authHandler.getDateOnlyFormatter(request).parse(authHandler.getDateOnlyFormatter(request).format(new Date()));
                requestParams.put("jobj", datajobj);
                requestParams.put("applyDate", applyDate);
                

                jobj = accSalesOrderServiceobj.importCustomerQuotationRecordsForCSV(requestParams, datajobj, AccountingManager.getGlobalParams(request));
//                System.out.println("A(( Import end : " + new Date());
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
//                System.out.println("A(( Validation start : " + new Date());
                jobj = importHandler.validateFileData(requestParams);
//                System.out.println("A(( Validation end : " + new Date());
                jobj.put(Constants.RES_success, true);
            }
        } catch (Exception ex) {
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * Description: Method for importing Sales Orders.
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView importSalesOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = getImportSalesOrderParams(request);
            jobj = accSalesOrderServiceobj.importSalesOrderJSON(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Description: Method for getting parameters of import Sales Orders.
     *
     * @param request
     * @return
     * @throws JSONException
     * @throws SessionExpiredException
     */
    public JSONObject getImportSalesOrderParams(HttpServletRequest request) throws JSONException, SessionExpiredException {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put("servletContext", this.getServletContext());
        paramJobj.put("locale", RequestContextUtils.getLocale(request));
        return paramJobj;
    }
    
    /**
     * Description: Following method imports sales orders from a file via script
     * @param subdomain (Constants.COMPANY_SUBDOMAIN)
     * @param moduleid (Constants.moduleid)
     * @param submoduleflag (Constants.submoduleflag)
    */
    public ModelAndView importSalesOrdersWithScript(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = new JSONObject();
//            String subdomain = request.getParameter(Constants.COMPANY_SUBDOMAIN);//Value of subdomain in this key is must be sent as a parameter
            String subdomain = request.getParameter(Constants.COMPANY_PARAM);//Value of subdomain in this key is picked from subdomain appended in URL
            paramJobj.put(Constants.COMPANY_SUBDOMAIN, subdomain);
            String moduleid = request.getParameter(Constants.moduleid);
            paramJobj.put(Constants.moduleid, moduleid);
            paramJobj.put(Constants.PAGE_URL, URLUtil.getPageURL(request, loginpageFull));
            /**
             * If filename is passed as a request parameter, put it into JSON
             * When this parameter is passed with request, this filename is used instead of the one stored in database
             */
            String importFileName = request.getParameter("importfilename");
            if (!StringUtil.isNullOrEmpty(importFileName)) {
                paramJobj.put("importFileName", importFileName);
            }
            int subModuleFlag = 0;//Default value is 0. Please refer to method ImportHandler.getModuleColumnConfig or javascript documentImportInterface.js for more information
            String subModuleFlagStr = request.getParameter(Constants.submoduleflag);
            if (!StringUtil.isNullOrEmpty(subModuleFlagStr)) {
                subModuleFlag = Integer.parseInt(subModuleFlagStr);
            }
            paramJobj.put(Constants.submoduleflag, subModuleFlag);
            paramJobj.put("do", "importByScript");//Flag used in service method to distinguish between 'validation', 'import', and 'importByScript'
            jobj = accSalesOrderServiceobj.importSalesOrderJSON(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
