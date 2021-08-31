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
package com.krawler.spring.accounting.customer;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.hql.accounting.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import com.krawler.spring.accounting.handler.AccountingHandlerService;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import static com.krawler.esp.web.resource.Links.loginpageFull;
/**
 *
 * @author krawler
 */
public class accCustomerController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accCustomerDAO accCustomerDAOobj;
    private accAccountDAO accAccountDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private exportMPXDAOImpl exportDaoObj;
    private ImportHandler importHandler;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private MessageSource messageSource;
    private AccCommonTablesDAO accCommonTablesDAO;
    private companyDetailsDAO companyDetailsDAOObj; 
    private fieldDataManager fieldDataManagercntrl;
    private AccountingHandlerService accountingHandlerServiceObj;
    private accMasterItemsDAO accMasterItemsDAOobj;
        
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
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

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
     public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj1) {
        this.companyDetailsDAOObj = companyDetailsDAOObj1;
    }
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setAccountingHandlerServiceObj(AccountingHandlerService accountingHandlerServiceObj) {
        this.accountingHandlerServiceObj = accountingHandlerServiceObj;
    }
    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
//    public ModelAndView manageCustomer(HttpServletRequest request, HttpServletResponse response) {
//        KwlReturnObject result = new KwlReturnObject(true, null, null, null, 0);
//        JSONObject jobj = new JSONObject(), obj=new JSONObject();
//        String msg = "";
//        boolean issuccess = true;
//        try {
//            int mode = Integer.parseInt(request.getParameter("mode"));
//            String companyid = sessionHandlerImpl.getCompanyid(request);
//            DateFormat df = authHandler.getDateFormatter(request);
//            switch (mode) {
//                case 12 :
////                    jobj=getInvoices(request);
//                    break;
//            }
//            issuccess = result.isSuccessFlag();
//        } catch (Exception ex) {
//            issuccess = false;
//            msg = ex.getMessage();
//        } finally {
//            try {
//                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
//            } catch (JSONException ex) {
//                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView("jsonView", "model", jobj.toString());
//    }
    public ModelAndView getCustomers(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getCustomerRequestMap(request);
//            KwlReturnObject result = accAccountDAOobj.getAccounts(requestParams);
//            System.out.println(new Date());
//            JSONArray jArr= getCustomerJson(request, result.getEntityList());

            KwlReturnObject result = accCustomerDAOobj.getCustomer(requestParams);
            ArrayList list = accAccountDAOobj.getAccountArrayList(result.getEntityList(), requestParams, false, false);
            JSONArray jArr = getCustomerJson(request, list);

//             String start = request.getParameter("start");
//            String limit = request.getParameter("limit");
//             JSONArray pagedJson = jArr;
//            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
//                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
//            }

            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getCustomers : " + ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getCustomersForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        List<Object[]> selectedcustomerList = new ArrayList();
        JSONArray jArr = new JSONArray();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = getCustomerRequestMap(request);
            String selectedCustomerIds = request.getParameter("combovalue");
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", sessionHandlerImpl.getCompanyid(request));
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                String userId = sessionHandlerImpl.getUserid(request);
                int permCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
                String userRoleID=!StringUtil.isNullOrEmpty(sessionHandlerImpl.getRole(request)) ? sessionHandlerImpl.getRole(request) : "";
                if (!((permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE)) {
                    /*View All permission = false
                    When user has view all permission=true and if "extraPref.isEnablesalespersonAgentFlow()" is true then show only those customers who have salesperson mapping with current user
                     * when (permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE is true then user has permission to view all customers documents,so at that time there is need to filter record according to user&salesperson. 
                     */
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                    
                    requestParams.put("hasViewAllPermission", false);
                    requestParams.put("isexcludeCustomersChecked", extraPref.isViewAllExcludeCustomer());
                }else if(!StringUtil.isNullOrEmpty(userRoleID) && !userRoleID.equalsIgnoreCase(Integer.toString(Constants.ADMIN_USER_ROLEID)) && extraPref.isViewAllExcludeCustomer()){
                    /*
                    
                    View All permission = true
                    userRoleID != Constants.ADMIN_USER_ROLEID - this check added becoz when " extraPref.isViewAllExcludeCustomer()"=true at that time admin should not get affected in any case admin should have full access
                    When  any user has View all permission code assigned then instead of treating that user admin apply filter while fetching customers to show in dropdown
                    */
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                    
                    
                    requestParams.put("hasViewAllPermission", true);
                    requestParams.put("isexcludeCustomersChecked", extraPref.isViewAllExcludeCustomer());
                }
            }
            /**
             * Block used to get selected customers using their ids from SOA- customer account statement.
             */
            if (!StringUtil.isNullOrEmpty(selectedCustomerIds) && !selectedCustomerIds.equals("All")) {
                requestParams.put("multiselectcustomerids", selectedCustomerIds);
                requestParams.put("ismultiselectcustomeridsFlag", true);
                KwlReturnObject selectedcustomer =  accCustomerDAOobj.getCustomersForCombo(requestParams);
                requestParams.remove("ismultiselectcustomeridsFlag");
                selectedcustomerList = selectedcustomer.getEntityList();
            }
            requestParams.put("customervendorsortingflag", extraPref.isCustomerVendorSortingFlag());
            /*
            accountingHandlerDAOobj.getCustomerAddressDetailsOptimized(String companyid, boolean isBillingAddress, boolean isDefaultAddress);
            */
            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            Map<String, Object> customerBillingAddressDetailsMap = null;
            addrRequestParams.put("companyid", companyid);
            /**
             * For India/ US GST we need to take default address to calculate GST
             * Previously random address taken.
             */
            if (extraPref.isIsNewGST()) {
                addrRequestParams.put("isBillingAddress", true);
                addrRequestParams.put("isDefaultAddress", true);
                customerBillingAddressDetailsMap = accountingHandlerServiceObj.getCustomerAddressDetailsMap(addrRequestParams);
            }
            Map<String, Object> customerShippingAddressDetailsMap =null;
            if (extraPref.isIsNewGST()) {
                addrRequestParams.put("isBillingAddress", false); 
                addrRequestParams.put("isDefaultAddress", true);
                customerShippingAddressDetailsMap = accountingHandlerServiceObj.getCustomerAddressDetailsMap(addrRequestParams);
            } else {
                addrRequestParams.put("isBillingAddress", false);
                addrRequestParams.put("isDefaultAddress", false);
                customerShippingAddressDetailsMap = accountingHandlerServiceObj.getCustomerAddressDetailsMap(addrRequestParams);
            }
            
            KwlReturnObject result = accCustomerDAOobj.getCustomersForCombo(requestParams);
            String excludeaccountid = (String) requestParams.get("accountid");
            String includeaccountid = (String) requestParams.get("includeaccountid");
            String includeparentid = (String) requestParams.get("includeparentid");

            boolean receivableAccFlag = request.getParameter("receivableAccFlag") != null ? Boolean.parseBoolean(request.getParameter("receivableAccFlag")) : false;
            boolean loanFlag = request.getParameter("loanFlag") != null ? Boolean.parseBoolean(request.getParameter("loanFlag")) : false;

            List<Object[]> list = result.getEntityList();
            /**
             * adding list of selected customres from SOA- customer account statement.
             */
            selectedcustomerList.addAll(list);

            for (Object[] row : selectedcustomerList) {                
                if (excludeaccountid != null && row[0] != null && row[0].equals(excludeaccountid)) {
                    continue;
                }
                if ((includeparentid != null && row[0] != null && (!row[0].equals(includeparentid) || (row[1] != null && !row[1].equals(includeparentid))))) {
                    continue;
                } else if ((includeaccountid != null && row[0] != null && !row[0].equals(includeaccountid))) {
                    continue;
                }
                
                JSONObject obj = new JSONObject();
                obj.put("accid", row[0] != null ? row[0] : "");
                obj.put("acccode", row[2] != null ? row[2] : "");
                obj.put("crmAccountId", row[3] != null ? row[3] : "");
                obj.put("accountid", row[4] != null ? row[4] : "");
                obj.put("accname", row[5] != null ? row[5] : "");
                obj.put("aliasname", row[6] != null ? row[6] : "");
                obj.put("taxId", row[7] != null ? (accAccountDAOobj.isTaxActivated(companyid, (String) row[7]) ? row[7] : "") : "");
                obj.put("isPermOrOnetime", row[8] != null ? row[8] : "");
                obj.put("interstateparty", row[9] != null ? row[9] : "");//INDIAN Company for CST Tax Calculation (Interstateparty)
                obj.put("cformapplicable", row[10] != null ? row[10] : "");//INDIAN Company for CST Tax Calculation (Cformapplicable)
                obj.put("commissionerate", row[11] != null ? row[11] : "");
                obj.put("division", row[12] != null ? row[12] : "");
                obj.put("range", row[13] != null ? row[13] : "");
                obj.put("iecnumber", row[14] != null ? row[14] : "");
                obj.put("csttinno", row[15] != null ? row[15] : "");
                obj.put("vattinno", row[16] != null ? row[16] : "");
                obj.put("eccno", row[17] != null ? row[17] : "");
                if(extraPref != null && extraPref.getCompany().getCountry().getID().equals("106")){
                    // In backend, NPWP saved as PAN number.
                    obj.put("npwp", row[18] != null ? row[18] : "");
                }else{
                    obj.put("panno", row[18] != null ? row[18] : "");
                }
                
                if (loanFlag) {
                    JSONObject jObj = accCustomerDAOobj.getLoanConfirmation(row[0] != null ? row[0].toString() : "", companyid);
                    JSONArray jArr1= jObj.getJSONArray("list");
                    boolean isCustomerApplyLoan=(boolean) jObj.get("isCustomerApplyLoan");
                    if (jArr1 != null && jArr1.length() > 0 && isCustomerApplyLoan) {
                        /*
                         * If If Loan is not clear but loan is apply
                         */
                        obj.put("isLoanClear", false);
                        obj.put("isLoanApply", true);
                    }else if(!isCustomerApplyLoan){
                        /*
                         * If If Loan is not clear but loan is not apply
                         */
                        obj.put("isLoanApply", false);
                        obj.put("isLoanClear", false);
                    } else{
                        /*
                         * If If Loan is clear and loan is apply
                         */
                        obj.put("isLoanClear", true);
                        obj.put("isLoanApply", true);
                    }
                }
                
                JSONObject object = new JSONObject();
                if (extraPref.isIsNewGST()) {
                    if(row[0] != null && customerBillingAddressDetailsMap != null && customerBillingAddressDetailsMap.containsKey(row[0].toString())) {
                        Object[] addressDetails = (Object[]) customerBillingAddressDetailsMap.get(row[0].toString());
                        object.put("billingCity", addressDetails[1]);
                        object.put("billingState", addressDetails[2]);
                        object.put("billingCounty", addressDetails[5]); // Billing county in 5th location
                        obj.put("addressExciseBuyer", addressDetails[4]);
                        obj.put("billingState", addressDetails[2]);
                    }
                }
                
                if (!receivableAccFlag) {
                    obj.put("masterSalesPerson", row[19] != null ? row[19] : "");
                    //Value to set in combo box for remote store-ERP-41011
                    if (row[19] != null) {
                        KwlReturnObject catresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), row[19].toString());
                        MasterItem masterItem = (MasterItem) catresult.getEntityList().get(0);
                        if (masterItem != null) {
                            obj.put("masterSalesPersonName", masterItem.getValue());
                        }
                    } else {
                        obj.put("masterSalesPersonName", "");
                    }
                    obj.put("billto", row[20] != null ? row[20] : "");
                    obj.put("email", row[21] != null ? row[21] : "");
                    obj.put("groupname", row[36] != null ? row[36] : "");
                    obj.put("termdays", row[37] != null ? row[37] : "");
                    obj.put("termid", row[38] != null ? row[38] : "");
//                    obj.put("mappedAccountTaxId", row[39] != null ? row[39] : "");
                    obj.put("mappedAccountTaxId", !StringUtil.isNullObject(row[39]) ? (accAccountDAOobj.isTaxActivated(companyid, row[39].toString()) ? row[39].toString() : "") : "");
                    obj.put("deleted", row[40] != null ? row[40] : "");
                }
                obj.put("masterReceivedForm", row[22] != null ? row[22] : "");
                obj.put("mappedPaidToId", row[23] != null ? row[23] : "");
                obj.put("paymentmethod", row[43]!= null ? row[43] : "");
                obj.put("paymentCriteria", row[24] != null ? row[24] : "");
                obj.put("defaultnatureofpurchase", row[25] != null ? row[25] : "");
                obj.put("overseas", row[26] != null ? row[26] : "");
                obj.put("hasAccess", row[27] != null && !row[27].toString().isEmpty() ? Boolean.parseBoolean(row[27].toString()) : "");
                obj.put("isactivate", row[27] != null ? row[27] : "");
                
                if (extraPref != null && extraPref.isAutoPopulateFieldsForDeliveryPlanner()) {
                    obj.put("deliveryDate", row[28] != null ? row[28] : "");
                    obj.put("deliveryTime", row[29] != null ? row[29] : "");
                }
                
                obj.put("currencyid", row[33] != null ? row[33] : "");
                obj.put("currencysymbol", row[34] != null ? row[34] : "");
                obj.put("currencyname", row[35] != null ? row[35] : "");
                
                /**
                 * ERP-32829 code for customer address for GST
                 * Need to add check for GST
                 */
                JSONArray currentAddressDetailrec = new JSONArray();
                if(row[0] != null && customerShippingAddressDetailsMap!=null && customerShippingAddressDetailsMap.containsKey(row[0].toString())) {
                    Object[] shippingAddressDetails = (Object[]) customerShippingAddressDetailsMap.get(row[0].toString());
                    object.put("shippingCity", shippingAddressDetails[1]);
                    object.put("shippingState", shippingAddressDetails[2]);
                    object.put("shippingCounty", shippingAddressDetails[5]); // Shipping county in 5th location
                }
                currentAddressDetailrec.put(object);
                obj.put("currentAddressDetailrec", currentAddressDetailrec);
                /**
                 * ERP-32829 Address - Dimension mapping
                 */
                object.put("companyid", companyid);
                currentAddressDetailrec = fieldDataManagercntrl.getAddressDimensionMapping(object);
                obj.put("addressMappingRec", currentAddressDetailrec);
                String type = row[30] != null ? row[30].toString() : "";
                /**
                 * Get Master item default for Customer type , To handle special
                 * case
                 */
                String defaultMasterItemID= "";
                if (!StringUtil.isNullOrEmpty(type)) {
                    KwlReturnObject retObj = accMasterItemsDAOobj.getMasterItem(type);
                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                        MasterItem reasonObj = (MasterItem) retObj.getEntityList().get(0);
                        defaultMasterItemID = reasonObj.getDefaultMasterItem() != null ? reasonObj.getDefaultMasterItem().getID() : "";
                        obj.put("CustVenTypeDefaultMstrID", defaultMasterItemID);
                    }
                }
                obj.put("uniqueCase", accCustomerDAOobj.getUniqueCase(obj.put("type",defaultMasterItemID)));
                obj.put("sezfromdate", row[31] != null ? row[31] : "");
                obj.put("seztodate", row[32] != null ? row[32] : "");
                //Get Customer GST details
                obj.put("CustomerVendorTypeId", row[30] != null ? row[30] : "");
                obj.put("gstin", row[41] != null ? row[41] : "");
                obj.put("GSTINRegistrationTypeId", row[42] != null ? row[42] : "");
                //ERP-34970(ERM-534)
                if (row[42] !=null && !StringUtil.isNullOrEmpty(row[42].toString())) {
                    KwlReturnObject retObj = accMasterItemsDAOobj.getMasterItem(row[42].toString());
                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                        MasterItem reasonObj = (MasterItem) retObj.getEntityList().get(0);
                        defaultMasterItemID = reasonObj.getDefaultMasterItem() != null ? reasonObj.getDefaultMasterItem().getID() : "";
                        obj.put("GSTINRegTypeDefaultMstrID", defaultMasterItemID);
                    }
                }
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getCustomersForCombo : " + ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * @DESC : Get type of Customer
     * @param object
     * @return 
     */
    private int getUniqueCase(JSONObject object) {
        int uniqueCase = Constants.APPLYGST;
        String type = object.optString("type");
        if (!StringUtil.isNullOrEmpty(type)) {
            if (type.equalsIgnoreCase(Constants.CUSTVENTYPE.get("Export (WPAY)"))) {
                return Constants.NOGST;
            } else if (type.equalsIgnoreCase(Constants.CUSTVENTYPE.get("Export (WOPAY)"))) {
                return Constants.NOGST;
            }else if (type.equalsIgnoreCase(Constants.CUSTVENTYPE.get("Import"))) {
                return Constants.APPLYSOMEGST;
            } else if (type.equalsIgnoreCase(Constants.CUSTVENTYPE.get("SEZ (WPAY)"))) {
                return Constants.APPLY_IGST;
            }else if (type.equalsIgnoreCase(Constants.CUSTVENTYPE.get("SEZ (WOPAY)"))) {
                return Constants.NOGST;
            }else if (type.equalsIgnoreCase(Constants.CUSTVENTYPE.get("Deemed_Export"))) {
                return Constants.APPLY_IGST;
            }
        }
        return uniqueCase;
    }
    public static HashMap<String, Object> getCustomerRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        String[] groups = request.getParameterValues("group");
        String[] groupsAfterAdding = groups;
        //To do - No depedndecy on accounts in customer and vendor.
//        if (groups != null) {
//            List<String> groupsList = new ArrayList<String>(Arrays.asList(groups));
//            Set groupsSet = new HashSet(Arrays.asList(groups));
//             if (groupsSet.contains(Group.ACCOUNTS_PAYABLE)&&!groupsSet.contains(Group.BILLS_PAYABLE)) {
//                groupsList.add(Group.BILLS_PAYABLE);
//            } else if (groupsSet.contains(Group.CURRENT_ASSETS)&&!groupsSet.contains(Group.CASH)) {
//                groupsList.add(Group.CASH);
//            }
//            groupsAfterAdding = groupsList.toArray(new String[groupsList.size()]);
//        }
        requestParams.put("group", groupsAfterAdding);
//        requestParams.put("getSundryCustomer", request.getParameter("getSundryCustomer"));        
//        requestParams.put("query", request.getParameter("query"));
        requestParams.put("ignore", request.getParameter("ignore"));
        requestParams.put("ignorecustomers", request.getParameter("ignorecustomers"));
        requestParams.put("ignorevendors", request.getParameter("ignorevendors"));
        if (request.getParameter("accountid") != null && !StringUtil.isNullOrEmpty(request.getParameter("accountid"))) {
            requestParams.put("accountid", request.getParameter("accountid"));
        }

        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        if (request.getParameter("query") != null && !StringUtil.isNullOrEmpty(request.getParameter("query"))) {
            requestParams.put("ss", request.getParameter("query"));
        } else if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
            requestParams.put("ss", request.getParameter("ss"));
        }

        if (StringUtil.isNullOrEmpty(request.getParameter("filetype"))) {
            if (request.getParameter("start") != null) {
                requestParams.put("start", request.getParameter("start"));
            }
            if (request.getParameter("limit") != null) {
                requestParams.put("limit", request.getParameter("limit"));
            }
        }
        if(!StringUtil.isNullOrEmpty(request.getParameter(Constants.REQ_startdate))){
                requestParams.put(Constants.REQ_startdate ,request.getParameter(Constants.REQ_startdate));
        }
        if (request.getParameter("comboCurrencyid") != null) {
            requestParams.put("comboCurrencyid", request.getParameter("comboCurrencyid"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("isPermOrOnetime"))) {
            requestParams.put("isPermOrOnetime", Boolean.FALSE.parseBoolean(request.getParameter("isPermOrOnetime")));
        }
        if (request.getParameter("receivableAccFlag") != null && !StringUtil.isNullOrEmpty(request.getParameter("receivableAccFlag"))) {
            requestParams.put("receivableAccFlag", request.getParameter("receivableAccFlag"));
        }
        if (request.getParameter("selectedCustomerIds") != null && !StringUtil.isNullOrEmpty(request.getParameter("selectedCustomerIds"))) {
            requestParams.put("selectedCustomerIds", request.getParameter("selectedCustomerIds"));
        }
        if (request.getParameter("activeDormantFlag") != null && !StringUtil.isNullOrEmpty(request.getParameter("activeDormantFlag"))) {
            requestParams.put("activeDormantFlag", request.getParameter("activeDormantFlag"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
            requestParams.put("dir", request.getParameter("dir"));
            requestParams.put("sort", request.getParameter("sort"));
        }
        if(request.getParameter("searchstartwith") !=null)
        {
            requestParams.put("searchstartwith",request.getParameter("searchstartwith"));
        }
        if(request.getParameter("cmpRecordField") !=null)
        {
            requestParams.put("cmpRecordField",request.getParameter("cmpRecordField"));
        }
        
        requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
        requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        /*
            Customer ids for exporting selected customers
        */
        String exportcustomerids=request.getParameter("exportcustvenids");
        if(!StringUtil.isNullOrEmpty(exportcustomerids)){
            requestParams.put("exportcustomers", exportcustomerids.substring(0, exportcustomerids.length()-1));
        }
        return requestParams;
    }

    // method moved to Main Controller. it is in no more use
    public JSONArray getCustomerJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Account account = null;
                Customer customer = (Customer) row[1];
                if (customer == null) {
                    continue;
                }
                if (customer.getAccount() != null) {
                    account = customer.getAccount();
                }
                JSONObject obj = new JSONObject();
//                obj.put("acccode",(StringUtil.isNullOrEmpty(customer.getAcccode()))?"":customer.getAcccode());
                obj.put("acccode", (StringUtil.isNullOrEmpty(customer.getAcccode())) ? "" : customer.getAcccode());
                obj.put("accid", customer.getID());
                obj.put("accname", customer.getName());
                obj.put("accnamecode", (StringUtil.isNullOrEmpty(customer.getAcccode())) ? customer.getName() : "[" + customer.getAcccode() + "]" + customer.getName());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                obj.put("openbalance", account.getOpeningBalance()); //To Do - Need to change 
                if (customer.getParent() != null) {
                    obj.put("parentid", customer.getParent().getID());
                    obj.put("parentname", customer.getParent().getName());
                }
                KWLCurrency currency = (KWLCurrency) row[4];
                obj.put("currencyid", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getSymbol()));
                obj.put("currencyname", (account.getCurrency() == null ? currency.getName() : account.getCurrency().getName()));
                obj.put("level", row[2]);
                obj.put("leaf", row[3]);
                obj.put("title", customer.getTitle());
                obj.put("address", customer.getBillingAddress());
                obj.put("baddress2", customer.getBillingAddress2());
                obj.put("baddress3", customer.getBillingAddress3());
                obj.put("email", customer.getEmail());
                obj.put("contactno", customer.getContactNumber());
                obj.put("contactno2", customer.getAltContactNumber());
                obj.put("fax", customer.getFax());
                obj.put("shippingaddress", customer.getShippingAddress());
                obj.put("shippingaddress2", customer.getShippingAddress2());
                obj.put("shippingaddress3", customer.getShippingAddress3());
                obj.put("pdm", customer.getPreferedDeliveryMode());
                obj.put("termname", customer.getCreditTerm() != null ? customer.getCreditTerm().getTermname() : "");
                obj.put("termid", customer.getCreditTerm() != null ? customer.getCreditTerm().getID() : "");
                obj.put("termdays", customer.getCreditTerm() != null ? customer.getCreditTerm().getTermdays() : "");
                obj.put("mappedSalesPersonId", ((customer.getMappingSalesPerson() != null) ? customer.getMappingSalesPerson().getID() : ""));
                obj.put("nameinaccounts", customer.getAccount().getName());
                obj.put("bankaccountno", customer.getBankaccountno());
                obj.put("isPermOrOnetime", customer.isIsPermOrOnetime());
                obj.put("billto", customer.getBillingAddress());
                obj.put("other", (customer.getOther() != null) ? customer.getOther() : "");
                obj.put("deleted", customer.getAccount().isDeleted());
                obj.put("id", customer.getID());
                obj.put("taxno", customer.getTaxNo());
                obj.put("categoryid", getCustomerCategoryIDs(account.getID()));
                obj.put("intercompanytypeid", account.getIntercompanytype() == null ? "" : account.getIntercompanytype().getID());
                obj.put("intercompany", account.isIntercompanyflag());
                obj.put("creationDate", authHandler.getUserDateFormatterWithoutTimeZone(request).format(customer.getAccount().getCreationDate()));
                obj.put("country", (customer.getCountry() == null ? "" : customer.getCountry().getID()));
                obj.put("limit", customer.getCreditlimit());
                obj.put("mapcustomervendor", customer.isMapcustomervendor());
                obj.put("overseas", customer.isOverseas());
                obj.put("mappingaccid", customer.getAccount().getID());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCustomerJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public String getCustomerCategoryIDs(String customerid) {
        JSONObject jobj = new JSONObject();
        String valuesStr = "";
        boolean issuccess = false;
        try {
            KwlReturnObject result = accCustomerDAOobj.getCustomerCategoryIDs(customerid);

            List list = result.getEntityList();
            Iterator itr = list.iterator();

            while (itr.hasNext()) {
                CustomerCategoryMapping row = (CustomerCategoryMapping) itr.next();
                MasterItem masterItemObj = row.getCustomerCategory();
                if (itr.hasNext()) {
                    valuesStr += masterItemObj.getID() + ",";
                } else {
                    valuesStr += masterItemObj.getID();
                }
            }
            issuccess = true;
        } catch (Exception e) {
            try {
                throw ServiceException.FAILURE(e.getMessage(), e);
            } catch (ServiceException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return valuesStr;
    }

    public ModelAndView getAddresses(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jSONObject = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        String msg = "";
        boolean issuccess = true;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String customerid = request.getParameter("customerid");
            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            addrRequestParams.put("customerid", customerid);
            addrRequestParams.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(request.getParameter("isBillingAddress"))) {
                boolean isBillingAddress = Boolean.parseBoolean((String) request.getParameter("isBillingAddress"));
                addrRequestParams.put("isBillingAddress", isBillingAddress);
            }
            /**
             * ERM - 294 If isDefaultAddress is true we only return default
             * address else all Address will return
             */
            if (!StringUtil.isNullOrEmpty(request.getParameter("isDefaultAddress"))) {
                boolean isDefaultAddress = Boolean.parseBoolean((String) request.getParameter("isDefaultAddress"));
                addrRequestParams.put("isDefaultAddress", isDefaultAddress);
            }
            KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);            
            if(!addressResult.getEntityList().isEmpty()){                    
                    List <CustomerAddressDetails> casList=addressResult.getEntityList();
                    for(CustomerAddressDetails cas:casList){
                         JSONObject addrObject=new JSONObject();
                         addrObject.put("aliasName", cas.getAliasName()!=null?cas.getAliasName():"");             
                         addrObject.put("address", cas.getAddress()!=null?cas.getAddress():"");       
                         addrObject.put("county", cas.getCounty()!=null?cas.getCounty():"");
                         addrObject.put("city", cas.getCity()!=null?cas.getCity():"");       
                         addrObject.put("state", cas.getState()!=null?cas.getState():"");       
                         addrObject.put("country", cas.getCountry()!=null?cas.getCountry():"");       
                         addrObject.put("postalCode", cas.getPostalCode()!=null?cas.getPostalCode():"");       
                         addrObject.put("phone", cas.getPhone()!=null?cas.getPhone():"");       
                         addrObject.put("mobileNumber", cas.getMobileNumber()!=null?cas.getMobileNumber():"");       
                         addrObject.put("fax", cas.getFax()!=null?cas.getFax():"");       
                         addrObject.put("emailID", cas.getEmailID()!=null?cas.getEmailID():"");       
                         addrObject.put("recipientName", cas.getRecipientName()!=null?cas.getRecipientName():"");       
                         addrObject.put("contactPerson", cas.getContactPerson()!=null?cas.getContactPerson():"");       
                         addrObject.put("contactPersonNumber", cas.getContactPersonNumber()!=null?cas.getContactPersonNumber():"");       
                         addrObject.put("contactPersonDesignation", cas.getContactPersonDesignation()!=null?cas.getContactPersonDesignation():"");       
                         addrObject.put("website", cas.getWebsite()!=null?cas.getWebsite():"");       
                         addrObject.put("shippingRoute", (cas.getShippingRoute()!=null && !cas.isIsBillingAddress())?cas.getShippingRoute():"");       
                         addrObject.put("isDefaultAddress", cas.isIsDefaultAddress());  
                         addrObject.put("isBillingAddress", cas.isIsBillingAddress());                             
                         jSONArray.put(addrObject);
                    }                    
                }
            jSONObject.put("data", jSONArray);
            jSONObject.put("count", jSONArray.length());
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getAddresses : " + ex;
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jSONObject.put("success", issuccess);
                jSONObject.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jSONObject.toString());
    }

    public ModelAndView exportCustomer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = getCustomerRequestMap(request);
            KwlReturnObject result = accCustomerDAOobj.getCustomer(requestParams);
            ArrayList list = accAccountDAOobj.getAccountArrayList(result.getEntityList(), requestParams, false, false);
            JSONArray jArr = getCustomerJson(request, list);
            jobj.put("data", jArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put("success", true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView importCustomer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));
            String companyid = sessionHandlerImpl.getCompanyid(request);

            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", companyid);
            CompanyAccountPreferences companyAccountPref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.hql.accounting.CompanyAccountPreferences", companyid);
            String baseUrl =com.krawler.common.util.URLUtil.getPageURL(request,loginpageFull,extraPref.getCompany().getSubDomain());//To get the URL of current loaded page.
            boolean updateExistingRecordFlag = false;
            if(!StringUtil.isNullOrEmpty(request.getParameter("updateExistingRecordFlag"))){
                updateExistingRecordFlag = Boolean.FALSE.parseBoolean(request.getParameter("updateExistingRecordFlag"));
            }
            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("tzdiff", sessionHandlerImpl.getTimeZoneDifference(request));
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());
            requestParams.put("companyid", companyid);
            requestParams.put("moduleName", Constants.Acc_Customer_modulename);
            requestParams.put("moduleid", "09508488-c1d2-102d-b048-001e58a64cb6");
            requestParams.put("isActivateToDateforExchangeRates", extraPref.isActivateToDateforExchangeRates());//variable needs while fetching exchange rate
            requestParams.put("isCurrencyCode",extraPref.isCurrencyCode());
            requestParams.put("isTDSapplicable",extraPref.isTDSapplicable()); //ERP-26934   
            requestParams.put("isExciseApplicable",extraPref.isExciseApplicable()); //ERP-26934 
            requestParams.put("isEnableVatCst",extraPref.isEnableVatCst());
            requestParams.put("updateExistingRecordFlag", updateExistingRecordFlag);
            requestParams.put("baseUrl", baseUrl);//ERP-37786
            requestParams.put("bookBeginningDate", companyAccountPref.getBookBeginningFrom());//ERP-36639
            if(extraPref.getCompany().getCountry()!=null){
               requestParams.put("countryid",extraPref.getCompany().getCountry().getID());//ERP-26934 
            }
            if(extraPref.getCompany().getState()!=null){
               requestParams.put("stateid",extraPref.getCompany().getState().getID()); 
            }
            if(updateExistingRecordFlag){
                requestParams.put("allowDuplcateRecord", updateExistingRecordFlag);
            }
//            ServerEventManager.publish("/importdata/111", "{total:34}",this.getServletContext());

            if (doAction.compareToIgnoreCase("import") == 0 || doAction.compareToIgnoreCase("xlsImport") == 0) {
                requestParams.put("action",doAction);
                System.out.println("A(( Import start : " + new Date());
//                String exceededLimit = request.getParameter("exceededLimit");
                String exceededLimit = "yes";
                if (exceededLimit.equalsIgnoreCase("yes")) { //If file contains records more than 1500 then Import file in background using thread
                    String logId = importHandler.addPendingImportLog(requestParams);
                    requestParams.put("logId", logId);
                    importHandler.add(requestParams);
                    if (!importHandler.isIsWorking()) {
                        Thread t = new Thread(importHandler);
                        t.setPriority(Constants.IMPORT_THREAD_PRIORITY_HIGH);
                        t.start();
                    }
                    jobj.put("success", true);
                } else {
                    if (extraPref != null) {
                        requestParams.put("allowropagatechildcompanies", extraPref.isPropagateToChildCompanies());
                    }
                    jobj = importHandler.importFileData(requestParams);

                    if (extraPref != null && extraPref.isPropagateToChildCompanies()) {
                        try {
                            List childCompaniesList = companyDetailsDAOObj.getChildCompanies(companyid);
                            requestParams.put("childcompanylist", childCompaniesList);
                            requestParams.put("parentcompanyID", companyid);
                            importHandler.add(requestParams);
                            if (!importHandler.isIsWorking()) {
                                Thread t = new Thread(importHandler);
                                t.start();
                            }
                        } catch (Exception ex) {
                             Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
                jobj.put("exceededLimit", exceededLimit);
                System.out.println("A(( Import end : " + new Date());
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                System.out.println("A(( Validation start : " + new Date());
                jobj = importHandler.validateFileData(requestParams);
                System.out.println("A(( Validation end : " + new Date());
            }
        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getCustomersByCategory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));

//            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
//            filter_names.add("c.company");
//            filter_params.add(sessionHandlerImpl.getCompanyid(request));
//            filter_names.add("ISaccount.deleted");
//            filter_params.add(false);
//            requestParams.put("filter_names", filter_names);
//            requestParams.put("filter_params", filter_params);
//            order_by.add("cm.customercategory");
//            order_type.add("desc");
//            requestParams.put("order_by", order_by);
//            requestParams.put("order_type", order_type);
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("categoryid", request.getParameter("categoryid"));

            KwlReturnObject result = accCustomerDAOobj.getNewCustomerList(requestParams);
            JSONArray jArr = getCustomersByCategoryJson(request, result.getEntityList());

            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    // method moved to Main Controller. it is in no more use
    public JSONArray getCustomersByCategoryJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {

                Object[] row = (Object[]) itr.next();
                String Custid = row[0].toString();
                String CategoryId = row[1] != null ? row[1].toString() : "";
                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), Custid);
                Customer customer = (Customer) custresult.getEntityList().get(0);
                MasterItem masterItem = null;
                if (!StringUtil.isNullOrEmpty(CategoryId)) {
                    KwlReturnObject catresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), CategoryId);
                    masterItem = (MasterItem) catresult.getEntityList().get(0);
                }

                Account account = customer.getAccount();

                if (account.isDeleted()) {
//                    continue;
                }
                JSONObject obj = new JSONObject();
                obj.put("accid", customer.getID());
                obj.put("accname", customer.getName());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                obj.put("openbalance", account.getOpeningBalance());

                obj.put("currencyid", (account.getCurrency() == null ? "" : account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (account.getCurrency() == null ? "" : account.getCurrency().getSymbol()));
                obj.put("currencyname", (account.getCurrency() == null ? "" : account.getCurrency().getName()));

                obj.put("title", customer.getTitle());
                obj.put("address", customer.getBillingAddress());
                obj.put("email", customer.getEmail());
                obj.put("contactno", customer.getContactNumber());
                obj.put("contactno2", customer.getAltContactNumber());
                obj.put("fax", customer.getFax());
                obj.put("shippingaddress", customer.getShippingAddress());
                obj.put("pdm", customer.getPreferedDeliveryMode());
                obj.put("termname", customer.getCreditTerm().getTermname());
                obj.put("termid", customer.getCreditTerm().getID());
                obj.put("termdays", customer.getCreditTerm().getTermdays());
                obj.put("nameinaccounts", customer.getAccount().getName());
                obj.put("bankaccountno", customer.getBankaccountno());
                obj.put("billto", customer.getBillingAddress());
                obj.put("other", customer.getOther());
                obj.put("deleted", customer.getAccount().isDeleted());
                obj.put("id", customer.getID());
                obj.put("taxno", customer.getTaxNo());
                obj.put("categoryid", masterItem == null ? "" : masterItem.getID());
                obj.put("category", masterItem == null ? "" : masterItem.getValue());
                obj.put("creationDate", authHandler.getUserDateFormatterWithoutTimeZone(request).format(customer.getAccount().getCreationDate()));

                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCustomersByCategoryJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public boolean isChildorGrandChild(String customerid, String parentid) throws ServiceException {
        try {
            List Result = accAccountDAOobj.isChildorGrandChild(parentid);
            Iterator iterator = Result.iterator();
            if (iterator.hasNext()) {
                Object ResultObj = iterator.next();
                Account ResultParentac = (Account) ResultObj;
                ResultParentac = ResultParentac.getParent();
                if (ResultParentac == null) {
                    return false;
                } else {
                    String Resultparent = ResultParentac.getID();
                    if (Resultparent.equals(customerid)) {
                        return true;
                    } else {
                        return isChildorGrandChild(customerid, Resultparent);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("isChildorGrandChild : " + ex.getMessage(), ex);
        }
        return false;
    }

    public ModelAndView getCustomerTransactionDetail(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray DataJArr = new JSONArray();

        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("customerID", request.getParameter("customerID"));
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put(Constants.REQ_startdate, request.getParameter("startdate"));
            requestParams.put(Constants.REQ_enddate, request.getParameter("enddate"));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("dateType", request.getParameter("dateType"));
            requestParams.put("isPurchase", request.getParameter("isPurchase"));
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("fetchOnHandData"))) {
                boolean isFetchOnlyAvailableStock=false;
                isFetchOnlyAvailableStock = Boolean.parseBoolean((String) request.getParameter("fetchOnHandData"));
                requestParams.put("fetchOnHandData", isFetchOnlyAvailableStock);
            }
            KwlReturnObject result = accCustomerDAOobj.getCustomerTransactionDetail(requestParams);
            List list = result.getEntityList();
            DataJArr = getCustomerTransactionDetail(request, list);
            int count = result.getRecordTotalCount();
            jobj.put("data", DataJArr);
            jobj.put("totalCount", count);
            issuccess = true;

            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getCustomerTransactionDetail(HttpServletRequest request, List list) throws JSONException, ServiceException, ParseException {
        Iterator itr = list.iterator();
        JSONArray jArr = new JSONArray();

        String pid = "";
        double balance = 0.0;
        boolean ispurchase = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isPurchase"))) {
            ispurchase = Boolean.parseBoolean((String) request.getParameter("isPurchase"));
        }
        
        boolean dateType = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("dateType"))) {
            dateType = Boolean.parseBoolean((String) request.getParameter("dateType"));
        } 
        
        boolean isFetchOnlyAvailableStock = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("fetchOnHandData"))) {
            isFetchOnlyAvailableStock = Boolean.parseBoolean((String) request.getParameter("fetchOnHandData"));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map prodBatchOutQtyMap = new HashMap();
            try {
                String companyid = sessionHandlerImpl.getCompanyid(request);
                DateFormat df = authHandler.getDateOnlyFormat(request);
                SimpleDateFormat sm = new SimpleDateFormat("yyyy-mm-dd");
                HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                String gcurrencyid = sessionHandlerImpl.getCurrencyID(request);

            while (itr.hasNext()) {

                JSONArray jSONArray = new JSONArray();
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);

                Object[] row = (Object[]) itr.next();

                if (!pid.equals((String) row[2])) {
                    balance = 0.0;
                }

                String transType = (String) row[0];
                String productid = (String) row[1];
                pid = (String) row[2];
                String prodname = (String) row[3];
                Date transactionDate = (Date) row[4];
                String transactionNumber = (String) row[5];
                String transid = (String) row[6];
                String personCode = (String) row[7];
                String personName = (String) row[8];
               String personid = (String) row[9];
                
                double stockRate = (Double) row[11];
                double baseUOMRate = (Double) row[12]; // Conversion Factor
                String batch = (String) row[13];
                double quantity = (batch != null) ? (row[19] == null) ? 0 : (Double) row[19] : (row[10] == null) ? 0 : (Double) row[10];
                boolean isFixedAsset = (((String) row[14]).equals("0")) ? false : true;
                boolean isLeaseFixedAsset = (((String) row[15]).equals("0")) ? false : true;
                String assetid = (String) row[16];
                String assetName = (String) row[20];
                String documentid = (row[18] == null) ? "" : (String) row[18];              
                String contractnumber = "";
                String cid = "";
                String dodetailsid = (String) row[21];
                boolean isSerialForProduct = (("T".equals(row[22].toString()))) ? true : false;

                JSONObject obj = new JSONObject();
                obj.put("type", transType);
                obj.put("pid", pid);
                obj.put("prodname", prodname);
                obj.put("transactionDate", df.format(transactionDate));
                obj.put("transactionNumber", transactionNumber);
                obj.put("transid", transid);
                obj.put("personCode", personCode);
                obj.put("accName", personName);
                obj.put("quantity", quantity);
                obj.put("isFixedAsset", isFixedAsset);
                obj.put("isLeaseFixedAsset", isLeaseFixedAsset);
                if (!StringUtil.isNullOrEmpty(assetid)) {
                    HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
                    requestParams1.put("assetid", assetid); //transaction id 
                    KwlReturnObject resultTrans = accCustomerDAOobj.getBatchForAsset(requestParams1);
                    List list2 = resultTrans.getEntityList();
                    Iterator it = list2.iterator();
                    if (it.hasNext()) {
                        Object oj = (Object) it.next();
                        batch = oj.toString();
                    }
                    documentid = assetid;     //documentid will be the assset id
                    quantity = 1;  //as for each asset detail there should be only 1 quantity for each asset id
                    obj.put("assetid", assetName); //for fixed asset and lease do there can be assset in that case name of asset

                }

                if (transType.equalsIgnoreCase("Delivery Order")) {
                    if (isFixedAsset) {
                        obj.put("transactionType", "Fixed Asset DO");
                    } else if (isLeaseFixedAsset) {
                        obj.put("transactionType", "Lease DO");
                    } else {
                        obj.put("transactionType", "Sales DO");
                    }
                } else if (transType.equalsIgnoreCase("Goodsreceipt Order")) {
                    if (isFixedAsset) {
                        obj.put("transactionType", "Fixed Asset GRO");
                    } else {
                        obj.put("transactionType", "Normal GRO");
                    }
                    
                } else {
                    obj.put("transactionType", transType);
                }

                if (transType.equalsIgnoreCase("Sales Return")) {
                    if (!StringUtil.isNullOrEmpty(dodetailsid)) {
                        String doid = accCustomerDAOobj.getDeliveryorderId(companyid, dodetailsid);
                        if (!StringUtil.isNullOrEmpty(doid)) {
                            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
                            requestParams1.put("transid", doid); //transaction id 
                            KwlReturnObject resultTrans = accCustomerDAOobj.getContractForDO(requestParams1);
                            List list2 = resultTrans.getEntityList();
                            Iterator it = list2.iterator();
                            if (it.hasNext()) {
                                Object[] oj = (Object[]) it.next();
                                cid = oj[0].toString();
                                contractnumber = oj[1].toString();
                            }
                        }
                    }
                } else {
                    if (!StringUtil.isNullOrEmpty(transid)) {
                        HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
                        requestParams1.put("transid", transid); //transaction id 
                        KwlReturnObject resultTrans = accCustomerDAOobj.getContractForDO(requestParams1);
                        List list2 = resultTrans.getEntityList();
                        Iterator it = list2.iterator();
                        if (it.hasNext()) {
                            Object[] oj = (Object[]) it.next();
                            cid = oj[0].toString();
                            contractnumber = oj[1].toString();
                        }
                    }
                }
             
                if (!StringUtil.isNullOrEmpty(cid)) {
                    obj.put("cid", cid.toString());
                }
                if (!StringUtil.isNullOrEmpty(contractnumber)) {
                    obj.put("contractnumber", contractnumber.toString());
                }
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.SerialWindow_ModuleId, 1));
                HashMap<String, String> customFieldMap = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
                // HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
                if (!StringUtil.isNullOrEmpty(batch)) {                    
                    KwlReturnObject Batch = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), batch);
                    NewProductBatch productBatch = (NewProductBatch) Batch.getEntityList().get(0);
                    if (productBatch != null) {
                    obj.put("id", productBatch.getId());
                    obj.put("batch", productBatch.getBatchname());
                   
                    obj.put("batch", productBatch.getBatchname());   //to show dat in grid
                        if (productBatch.getLocation() != null) {
                        obj.put("location", productBatch.getLocation().getName());
                        }
                        if (productBatch.getWarehouse() != null) {
                        obj.put("warehouse", productBatch.getWarehouse().getName());
                        }
                        if (dateType && !isSerialForProduct && productBatch.getExpdate() == null) {
                         continue;
                    }
                    obj.put("mfgdate", productBatch.getMfgdate() != null ? productBatch.getMfgdate() : "");
                    obj.put("expdate", productBatch.getExpdate() != null ? productBatch.getExpdate() : "");

                    obj.put("quantity", quantity);
                    obj.put("balance", productBatch.getBalance());
                    } 
                    
                    KwlReturnObject kmsg = null;
                    
                    if (!isSerialForProduct && ispurchase && isFetchOnlyAvailableStock && productBatch != null) {
                        String prodBatchKey = productid + productBatch.getId();
                        if (prodBatchOutQtyMap.containsKey(prodBatchKey)) {
                            double batchOutQty = (double) prodBatchOutQtyMap.get(prodBatchKey);
                            if (quantity - batchOutQty < 0) {
                                batchOutQty = batchOutQty - quantity;
                                quantity = 0;
                                prodBatchOutQtyMap.put(prodBatchKey, batchOutQty);
                            } else {
                                quantity = quantity - batchOutQty;
                                prodBatchOutQtyMap.put(prodBatchKey, 0.0);
                            }
                        } else {
                            double batchOutQty = productBatch.getQuantity() - productBatch.getQuantitydue();
                            if (batchOutQty < 0) {
                                quantity = 0;
                            } else {
                                prodBatchOutQtyMap.put(prodBatchKey, (batchOutQty - quantity < 0 ? 0.0 : batchOutQty - quantity));
                                quantity = (quantity - batchOutQty < 0 ? 0 : quantity - batchOutQty);
                            }
                        }
                        obj.put("quantity", quantity);
                    }
                    
                    if (transType.equalsIgnoreCase("Stock Adjustment IN") || transType.equalsIgnoreCase("Stock Adjustment Out")) {
                        kmsg = accCommonTablesDAO.getStockAdjustmentSerialData(companyid, productBatch, documentid, isFetchOnlyAvailableStock);

                        List list1 = kmsg.getEntityList();
                        Iterator iter = list1.iterator();
                        while (iter.hasNext()) {
                            String serialnoid = (String) iter.next();
                            JSONObject bobj = new JSONObject();
                            if (StringUtil.isNullOrEmpty(serialnoid)) {
                                continue;
                            }
                            KwlReturnObject BatchSerial = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), serialnoid);
                            NewBatchSerial batchSerial = (NewBatchSerial) BatchSerial.getEntityList().get(0);
                            if (dateType && batchSerial != null && (batchSerial.getExpfromdate() != null && batchSerial.getExptodate() != null)) {
                                continue;
                            }
                            if (isFetchOnlyAvailableStock && batchSerial.getQuantitydue() == 0) {
                                quantity = quantity - 1;
                                obj.put("quantity", quantity);
                                continue;
                            }

                            Date vendorExpDate = accCommonTablesDAO.getVendorExpDateForSerial(serialnoid, ispurchase);
                            if (vendorExpDate != null) {
                                bobj.put("vendorExpDate", sdf.format(vendorExpDate));
                            }

                            bobj.put("serialnoid", batchSerial.getId());
                            bobj.put("serialno", batchSerial.getSerialname());
                            bobj.put("expstart", batchSerial.getExpfromdate() != null ? authHandler.getDateOnlyFormat(request).format(batchSerial.getExpfromdate()) : "");
                            bobj.put("expend", batchSerial.getExptodate() != null ? authHandler.getDateOnlyFormat(request).format(batchSerial.getExptodate()) : "");
                            jSONArray.put(bobj);
                        }

                    } else {
                        kmsg = accCommonTablesDAO.getBatchSerialDetails(documentid, false, false, "", false, false, "");

                        List list1 = kmsg.getEntityList();
                        Iterator iter = list1.iterator();
                        int i = 1;
                        while (iter.hasNext()) {
                            Object[] objArr = (Object[]) iter.next();
                            String serialnoid = objArr[7] != null ? (String) objArr[7] : "";
                            JSONObject bobj = new JSONObject();
                            if (StringUtil.isNullOrEmpty(serialnoid)) {
                                continue;
                            }
                            KwlReturnObject BatchSerial = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), serialnoid);
                            NewBatchSerial batchSerial = (NewBatchSerial) BatchSerial.getEntityList().get(0);
                            if (!batchSerial.getBatch().getId().equals(productBatch.getId())) {
                                continue;
                            } else if (isFetchOnlyAvailableStock && batchSerial.getQuantitydue() == 0) {
                                quantity = quantity - 1;
                                obj.put("quantity", quantity);
                                continue;
                            }
                            if (!StringUtil.isNullOrEmpty(serialnoid)) {
                                Date vendorExpDate = accCommonTablesDAO.getVendorExpDateForSerial(serialnoid, ispurchase);
                                if (vendorExpDate != null) {
                                    bobj.put("vendorExpDate", sdf.format(vendorExpDate));
                                }

                            }
                            bobj.put("serialnoid", batchSerial.getId());
                            bobj.put("serialno", batchSerial.getSerialname());
                            bobj.put("expstart", batchSerial.getExpfromdate() != null ? authHandler.getDateOnlyFormat(request).format(batchSerial.getExpfromdate()) : "");
                            bobj.put("expend", batchSerial.getExptodate() != null ? authHandler.getDateOnlyFormat(request).format(batchSerial.getExptodate()) : "");

                            if (objArr[14] != null && !objArr[14].toString().equalsIgnoreCase("")) {    //Get SerialDocumentMappingId
                                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(SerialDocumentMapping.class.getName(), objArr[14].toString());
                                SerialDocumentMapping sdm = (SerialDocumentMapping) result1.getEntityList().get(0);
                                Map<String, Object> variableMap = new HashMap<String, Object>();
                                SerialCustomData serialCustomData = (SerialCustomData) sdm.getSerialCustomData();
                                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                                AccountingManager.setCustomColumnValues(serialCustomData, fieldMap, replaceFieldMap, variableMap);
                                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                    String coldata = varEntry.getValue().toString();
                                    String valueForReport = "";
                                    if (customFieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                                        try {
                                            String[] valueData = coldata.split(",");
                                            for (String value : valueData) {
                                                FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), value);
                                                if (fieldComboData != null) {
//                                    valueForReport += fieldComboData.getValue() + ",";
                                                    valueForReport += fieldComboData.getValue() + ",";
                                                }
                                            }
                                            if (valueForReport.length() > 1) {
                                                valueForReport = valueForReport.substring(0, valueForReport.length() - 1);
                                            }
                                            bobj.put(varEntry.getKey(), valueForReport);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                                        } catch (Exception ex) {
                                            bobj.put(varEntry.getKey(), coldata);
                                        }
                                    } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                                        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                                        DateFormat defaultDateFormat = new SimpleDateFormat(Constants.MMMMdyyyy);
                                        Date dateFromDB = null;
                                        try {
                                            dateFromDB = defaultDateFormat.parse(coldata);
                                            coldata = df2.format(dateFromDB);

                                        } catch (Exception e) {
                                        }                                        
                                            bobj.put(varEntry.getKey(), coldata);                                        
                                    } else {
                                        if (!StringUtil.isNullOrEmpty(coldata)) {
                                            bobj.put(varEntry.getKey(), coldata);
                                        }
                                    }
                                }
                            }
                            jSONArray.put(bobj);

                        }
                    }
                    
                    String batchdetails = jSONArray.toString();
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                    CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                    obj.put("batchdetails", (StringUtil.isNullOrEmpty(batchdetails)) ? "" : batchdetails);
                } else {
                    
                    if (!StringUtil.isNullOrEmpty(documentid)) {
                        KwlReturnObject kmsg = null;
                        kmsg = accCommonTablesDAO.getOnlySerialDetails(documentid, false, "", false, false);
                        List batchserialdetails = kmsg.getEntityList();
                        Iterator iter = batchserialdetails.iterator();
                        while (iter.hasNext()) {

                            JSONObject bobj = new JSONObject();
                            Object[] objArr = (Object[]) iter.next();
                            String serialnoid = objArr[7] != null ? (String) objArr[7] : "";
                            
                            KwlReturnObject BatchSerial = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), serialnoid);
                            NewBatchSerial batchSerial = (NewBatchSerial) BatchSerial.getEntityList().get(0);
                            if (isFetchOnlyAvailableStock && batchSerial != null && batchSerial.getQuantitydue() == 0) {
                                quantity = quantity - 1;
                                obj.put("quantity", quantity);
                                continue;
                            }
                            if (!StringUtil.isNullOrEmpty(serialnoid)) {
                                Date vendorExpDate = accCommonTablesDAO.getVendorExpDateForSerial(serialnoid, ispurchase);
                                if (vendorExpDate != null) {
                                    bobj.put("vendorExpDate", sdf.format(vendorExpDate));
                                }

                            }
                            bobj.put("serialnoid", serialnoid);
                            bobj.put("serialno", objArr[8] != null ? (String) objArr[8] : "");
                            bobj.put("expstart", objArr[9] != null ? authHandler.getDateOnlyFormat(request).format(objArr[9]) : "");
                            bobj.put("expend", objArr[10] != null ? authHandler.getDateOnlyFormat(request).format(objArr[10]) : "");
                            if (objArr[14] != null) {    //Get SerialDocumentMappingId
                                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(SerialDocumentMapping.class.getName(), objArr[14].toString());
                                SerialDocumentMapping sdm = (SerialDocumentMapping) result1.getEntityList().get(0);
                                Map<String, Object> variableMap = new HashMap<String, Object>();
                                SerialCustomData serialCustomData = (SerialCustomData) sdm.getSerialCustomData();
                                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                                AccountingManager.setCustomColumnValues(serialCustomData, fieldMap, replaceFieldMap, variableMap);
                                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                    String coldata = varEntry.getValue().toString();
                                    String valueForReport = "";
                                    if (customFieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                                        try {
                                            String[] valueData = coldata.split(",");
                                            for (String value : valueData) {
                                                FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), value);
                                                if (fieldComboData != null) {
//                                    valueForReport += fieldComboData.getValue() + ",";
                                                    valueForReport += fieldComboData.getValue() + ",";
                                                }
                                            }
                                            if (valueForReport.length() > 1) {
                                                valueForReport = valueForReport.substring(0, valueForReport.length() - 1);
                                            }
                                            bobj.put(varEntry.getKey(), valueForReport);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                                        } catch (Exception ex) {
                                            bobj.put(varEntry.getKey(), coldata);
                                        }
                                    } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                                        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                                        DateFormat defaultDateFormat = new SimpleDateFormat(Constants.MMMMdyyyy);
                                        Date dateFromDB = null;
                                        try {
                                            dateFromDB = defaultDateFormat.parse(coldata);
                                            coldata = df2.format(dateFromDB);

                                        } catch (Exception e) {
                                        }
                                        bobj.put(varEntry.getKey(), coldata);
                                    } else {
                                        if (!StringUtil.isNullOrEmpty(coldata)) {
                                            bobj.put(varEntry.getKey(), coldata);
                                        }
                                    }
                                }
                            }
                            jSONArray.put(bobj);

                        }
                        String batchdetails = jSONArray.toString();
                        KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                        obj.put("batchdetails", (StringUtil.isNullOrEmpty(batchdetails)) ? "" : batchdetails);
                    }
                }
                jArr.put(obj);
            }
            } catch (SessionExpiredException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);

            }
        return jArr;
    }
    
    public ModelAndView exportVendorProductExpiryReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
                String companyid = sessionHandlerImpl.getCompanyid(request);
             HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("customerID", request.getParameter("customerID"));
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put(Constants.REQ_startdate, request.getParameter("startdate"));
            requestParams.put(Constants.REQ_enddate, request.getParameter("enddate"));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("dateType", request.getParameter("dateType"));
            requestParams.put("isPurchase", request.getParameter("isPurchase"));
                
                String gcurrencyid = sessionHandlerImpl.getCurrencyID(request);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
            
            
            request.setAttribute("companyid", companyid);
            request.setAttribute("gcurrencyid", gcurrencyid);
 
            
            request.setAttribute("isExport", true);
            
          
           KwlReturnObject result = accCustomerDAOobj.getCustomerTransactionDetail(requestParams);
            List list = result.getEntityList();
            JSONArray DataJArr = getCustomerTransactionDetail(request, list);
            
             
            jobj.put("data", DataJArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
     public ModelAndView getCustomersIdNameForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        JSONArray jArr = new JSONArray();
        List<Object[]> selectedcustomerList = new ArrayList();
        try {
           HashMap<String, Object> requestParams = getCustomerRequestMap(request);
           String selectedCustomerIds = request.getParameter("combovalue");
           ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", sessionHandlerImpl.getCompanyid(request));
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                String userId = sessionHandlerImpl.getUserid(request);
                int permCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
                String userRoleID=!StringUtil.isNullOrEmpty(sessionHandlerImpl.getRole(request)) ? sessionHandlerImpl.getRole(request) : "";
                if (!((permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE)) {
                    /*View All permission = false
                    When user has view all permission=true and if "extraPref.isEnablesalespersonAgentFlow()" is true then show only those customers who have salesperson mapping with current user
                     * when (permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE is true then user has permission to view all customers documents,so at that time there is need to filter record according to user&salesperson. 
                     */
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                    
                    requestParams.put("hasViewAllPermission", false);
                    requestParams.put("isexcludeCustomersChecked", extraPref.isViewAllExcludeCustomer());
                }else if(!StringUtil.isNullOrEmpty(userRoleID) && !userRoleID.equalsIgnoreCase(Integer.toString(Constants.ADMIN_USER_ROLEID)) && extraPref.isViewAllExcludeCustomer()){
                    /*
                    
                    View All permission = true
                    userRoleID != Constants.ADMIN_USER_ROLEID - this check added becoz when " extraPref.isViewAllExcludeCustomer()"=true at that time admin should not get affected in any case admin should have full access
                    When  any user has View all permission code assigned then instead of treating that user admin apply filter while fetching customers to show in dropdown
                    */
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                    
                    
                    requestParams.put("hasViewAllPermission", true);
                    requestParams.put("isexcludeCustomersChecked", extraPref.isViewAllExcludeCustomer());
}
            }
            /**
             * Block used to get selected customers using their ids from SOA- customer account statement.
             */
            if (!StringUtil.isNullOrEmpty(selectedCustomerIds) && !selectedCustomerIds.equals("All")) {
                requestParams.put("multiselectcustomerids", selectedCustomerIds);
                requestParams.put("ismultiselectcustomeridsFlag", true);
                KwlReturnObject selectedcustomer =  accCustomerDAOobj.getCustomersForCombo(requestParams);
                requestParams.remove("ismultiselectcustomeridsFlag");
                selectedcustomerList = selectedcustomer.getEntityList();
            }
            requestParams.put("customervendorsortingflag", extraPref.isCustomerVendorSortingFlag());
            KwlReturnObject result = accCustomerDAOobj.getCustomersForCombo(requestParams);
            String excludeaccountid = requestParams.get("accountid") == null ? "" : (String) requestParams.get("accountid");
            List<Object[]> custlist = result.getEntityList();
            
            /**
             * adding list of selected customres from SOA- customer account statement.
             */
            selectedcustomerList.addAll(custlist);
            
            for (Object[] row : selectedcustomerList) {
                JSONObject obj = new JSONObject();
                if (row != null) {
                    if (!StringUtil.isNullOrEmpty(excludeaccountid) && row[0] != null && row[0].equals(excludeaccountid)) {
                        continue;
                    }
                    if (row[2] != null) {
                        obj.put("acccode", row[2].toString());
                    }
                    if (row[0] != null) {
                        obj.put("accid", row[0].toString());
                    }
                    if (row[5] != null) {
                        obj.put("accname", row[5].toString());
                    }
                    if (row[7] != null) {
                        obj.put("taxId", row[7].toString());
                    }
                    if (row[27] != null) {
                         obj.put("hasAccess", Boolean.parseBoolean(row[27].toString()));
                    }
                    if (row[36] != null) {
                        obj.put("groupname",  row[36].toString());
                    }
                }
                jArr.put(obj);
            }
            
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getCustomersForCombo : " + ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
}
