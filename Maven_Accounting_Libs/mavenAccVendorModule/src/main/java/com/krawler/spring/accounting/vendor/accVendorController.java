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
package com.krawler.spring.accounting.vendor;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import static com.krawler.common.util.Constants.GSTRegType;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.lucene.search.ConstantScoreQuery;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
import static com.krawler.esp.web.resource.Links.loginpageFull;
/**
 *
 * @author krawler
 */
public class accVendorController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accVendorDAO accVendorDAOobj;
    private accAccountDAO accAccountDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private exportMPXDAOImpl exportDaoObj;
    private ImportHandler importHandler;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accVendorControllerService accVendorControllerServiceObj;
    private MessageSource messageSource;
    private companyDetailsDAO companyDetailsDAOObj;
    private fieldDataManager fieldDataManagercntrl;
    private accMasterItemsDAO accMasterItemsDAOobj;
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;

    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setaccVendorControllerService(accVendorControllerService accVendorControllerServiceObj) {
        this.accVendorControllerServiceObj = accVendorControllerServiceObj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
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
    
    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj1) {
        this.companyDetailsDAOObj = companyDetailsDAOObj1;
    }
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    public ModelAndView getVendors(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getVendorRequestMap(request);
//            KwlReturnObject result = accAccountDAOobj.getAccounts(requestParams);
//            JSONArray jArr = getVendorJson(request, result.getEntityList());

            KwlReturnObject result = accVendorDAOobj.getVendor(requestParams);
            ArrayList list = accAccountDAOobj.getAccountArrayList(result.getEntityList(), requestParams, false, false);
            JSONArray jArr = getVendorJson(request, list);

            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accVendorController.getVendors : " + ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getVendorsForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        String vendorEmailId="";
        JSONArray jArr = new JSONArray();
        List<Object[]> selectedvendorList = new ArrayList();
        try {
            HashMap<String, Object> requestParams = getVendorRequestMap(request);
            String companyId = (String) requestParams.get(Constants.companyKey);
            String selectedvendorIds = request.getParameter("combovalue"); 
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", sessionHandlerImpl.getCompanyid(request));
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                int permCode = sessionHandlerImpl.getPerms(request, Constants.VENDOR_PERMCODE);
                if (!((permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE is true then user has permission to view all vendors documents,so at that time there is need to filter record according to user&agent. 
                     */
                    String userId = sessionHandlerImpl.getUserid(request);
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                }
            }
            /**
             * Block used to get selected customers using their ids from SOA- customer account statement.
             */
            if (!StringUtil.isNullOrEmpty(selectedvendorIds) && !selectedvendorIds.equals("All")) {
                requestParams.put("multiselectedvendorIds", selectedvendorIds);
                requestParams.put("ismultiselectvendoridsFlag", true);
                KwlReturnObject selectedvendor =  accVendorDAOobj.getVendorsForCombo(requestParams);
                requestParams.remove("ismultiselectvendoridsFlag");
                selectedvendorList = selectedvendor.getEntityList();
            }
            requestParams.put("customervendorsortingflag", extraPref.isCustomerVendorSortingFlag());
            KwlReturnObject result = accVendorDAOobj.getVendorsForCombo(requestParams);
//            ArrayList list = accAccountDAOobj.getAccountArrayList(result.getEntityList(), requestParams);
//            ArrayList resultlist = new ArrayList();
//            boolean ignoreCustomers=requestParams.get("ignorecustomers")!=null;
//            boolean ignoreVendors=requestParams.get("ignorevendors")!=null;
//            boolean deleted =Boolean.parseBoolean((String)requestParams.get("deleted"));
//            boolean nondeleted =Boolean.parseBoolean((String)requestParams.get("nondeleted"));
            String excludeaccountid = (String) requestParams.get("accountid");
            String includeaccountid = (String) requestParams.get("includeaccountid");
            String includeparentid = (String) requestParams.get("includeparentid");

            boolean receivableAccFlag = request.getParameter("receivableAccFlag") != null ? Boolean.parseBoolean(request.getParameter("receivableAccFlag")) : false;
            List list = result.getEntityList();
            selectedvendorList.addAll(list);
            HashMap<String, Object> addressParams = new HashMap<String, Object>();
            addressParams.put("isDefaultAddress", true);
            addressParams.put("isBillingAddress", true); //true to get billing address
                
            for(Object vendor : selectedvendorList) {
                Object[] row = (Object[]) vendor;
                
                if (excludeaccountid != null && row[0] != null && row[0].equals(excludeaccountid)) {
                    continue;
                }
                if ((includeparentid != null && row[0] != null && (!row[0].equals(includeparentid) || (row[1] != null && !row[1].equals(includeparentid))))) {
                    continue;
                } else if ((includeaccountid != null && row[0] != null && !row[0].equals(includeaccountid))) {
                    continue;
                }

                JSONObject obj = new JSONObject();
                obj.put("accid", StringUtil.isNullObject(row[0]) ? "" : row[0].toString());
                obj.put("acccode", StringUtil.isNullObject(row[2]) ? "" : row[2].toString());
                obj.put("vattinno", StringUtil.isNullObject(row[3]) ? "" : row[3].toString());
                obj.put("csttinno", StringUtil.isNullObject(row[4]) ? "" : row[4].toString());
                if(extraPref != null && extraPref.getCompany().getCountry().getID().equals("106")){
                    // In backend, NPWP saved as PAN number.
                    obj.put("npwp", StringUtil.isNullObject(row[5]) ? "" : row[5].toString());
                }else{
                    obj.put("panno", StringUtil.isNullObject(row[5]) ? "" : row[5].toString());
                }
                obj.put("vendorbranch", StringUtil.isNullObject(row[6]) ? "" : row[6].toString());
                obj.put("servicetaxno", StringUtil.isNullObject(row[7]) ? "" : row[7].toString());
                obj.put("tanno", StringUtil.isNullObject(row[8]) ? "" : row[8].toString());
                obj.put("eccno", StringUtil.isNullObject(row[9]) ? "" : row[9].toString());
                obj.put("residentialstatus", StringUtil.isNullObject(row[10]) ? "" : row[10].toString());
                obj.put("natureOfPayment", StringUtil.isNullObject(row[11]) ? "" : row[11].toString());
                obj.put("deductionReason", StringUtil.isNullObject(row[12]) ? "" : row[12].toString());
                MasterItem masterItem2 = null;
                if (row[11] != null && !StringUtil.isNullOrEmpty(row[11].toString())) {
                    KwlReturnObject catresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), row[11].toString());
                    if(!catresult.getEntityList().isEmpty()) {
                        masterItem2 = (MasterItem) catresult.getEntityList().get(0);
                    }
                    obj.put("natureOfPaymentname", masterItem2.getCode() +" - "+ masterItem2.getValue());//INDIAN Company for TDS Calculation
                } else {
                    obj.put("natureOfPaymentname", "");
                }
                obj.put("tdsInterestPayableAccount", StringUtil.isNullObject(row[14]) ? "" : row[14].toString());
                obj.put("accname", StringUtil.isNullObject(row[15]) ? "" : row[15].toString());
                obj.put("aliasname", StringUtil.isNullObject(row[16]) ? "" : row[16].toString());
                obj.put("rmcdApprovalNumber", StringUtil.isNullObject(row[17]) ? "" : row[17].toString());// For Malasian company
                obj.put("accountid", StringUtil.isNullObject(row[18]) ? "" : row[18].toString());
                obj.put("currencyid", StringUtil.isNullObject(row[19]) ? "" : row[19].toString());
                obj.put("taxId", !StringUtil.isNullObject(row[20]) ? (accAccountDAOobj.isTaxActivated(companyId, row[20].toString()) ? row[20].toString() : "") : "");
                obj.put("selfBilledFromDate", row[21] !=null ? authHandler.getUserDateFormatterWithoutTimeZone(request).format(row[21]) : "");
                obj.put("selfBilledToDate",row[22] !=null ? authHandler.getUserDateFormatterWithoutTimeZone(request).format(row[22]) : "");
                obj.put("hasAccess", StringUtil.isNullObject(row[23]) && !row[23].toString().isEmpty() ? "" : Boolean.parseBoolean(row[23].toString()));
                obj.put("isactivate", StringUtil.isNullObject(row[23]) ? "" : row[23].toString());
                obj.put("masteragent", StringUtil.isNullObject(row[24]) ? "" : row[24].toString());
                
                //Value to set in combo box for remote store-ERP-41011.When Vendor is selected while creating transaction
                if (row[24] != null) {
                    KwlReturnObject catresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), row[24].toString());
                    MasterItem masterItem = (MasterItem) catresult.getEntityList().get(0);
                    if (masterItem != null) {
                        obj.put("masteragentname", masterItem.getValue());
                    }
                } else {
                    obj.put("masteragentname", "");
                }
                
                obj.put("deducteetype", StringUtil.isNullObject(row[25]) ? "" : row[25].toString());//INDIAN Company for TDS Calculation in Make Payment
                MasterItem masterItem = null;
                if (row[25] != null && !StringUtil.isNullOrEmpty(row[25].toString())) {
                    KwlReturnObject catresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), row[25].toString());
                    if(!catresult.getEntityList().isEmpty()) {
                    masterItem = (MasterItem) catresult.getEntityList().get(0);
                    obj.put("deducteetypename", masterItem!=null?masterItem.getValue():"");//INDIAN Company for TDS Calculation in Make Payment
                    }
                } else {
                    obj.put("deducteetypename", "");
                }
                obj.put("interstateparty", StringUtil.isNullObject(row[26]) ? "" : row[26].toString());//INDIAN Company for TDS Calculation in Make Payment
                obj.put("cformapplicable", StringUtil.isNullObject(row[27]) ? "" : row[27].toString());//INDIAN Company for TDS Calculation in Make Payment
                obj.put("isTDSapplicableonvendor", row[28] != null ? row[28] : "");//INDIAN Company for TDS Calculation in Make Payment
                obj.put("gtaapplicable", StringUtil.isNullObject(row[29]) ? "" : row[29].toString());
                obj.put("commissionerate", StringUtil.isNullObject(row[30]) ? "" : row[30].toString());
                obj.put("division", StringUtil.isNullObject(row[31]) ? "" : row[31].toString());
                obj.put("range", StringUtil.isNullObject(row[32]) ? "" : row[32].toString());
                obj.put("iecnumber", StringUtil.isNullObject(row[33]) ? "" : row[33].toString());
                obj.put("minPriceValueForVendor", StringUtil.isNullObject(row[34]) ? "" : row[34].toString());
                obj.put("mappedPaidToId", StringUtil.isNullObject(row[35]) ? "" : row[35].toString());
                
                String companyid = sessionHandlerImpl.getCompanyid(request);
                JSONObject object=new JSONObject();
                if (extraPref.isIsNewGST()) {
                    HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                    addrRequestParams.put("vendorid", StringUtil.isNullObject(row[0]) ? "" : row[0].toString());
                    addrRequestParams.put("companyid", companyid);
                    addrRequestParams.put("isBillingAddress", true);//only billing address   
                    addrRequestParams.put("isDefaultAddress", true); // Only Default Address  
                    KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
                    if (!addressResult.getEntityList().isEmpty()) {
                        List<VendorAddressDetails> vasList = addressResult.getEntityList();
                        if (vasList.size() > 0) {
                            VendorAddressDetails vas = (VendorAddressDetails) vasList.get(0);
                            String fullAddress = "";
                            if (!StringUtil.isNullOrEmpty(vas.getAddress())) {
                                fullAddress += vas.getAddress() + ", ";
                            }
                            if (!StringUtil.isNullOrEmpty(vas.getCity())) {
                                fullAddress += vas.getCity() + ", ";
                            }
                            if (!StringUtil.isNullOrEmpty(vas.getState())) {
                                fullAddress += vas.getState() + ", ";
                            }
                            if (!StringUtil.isNullOrEmpty(vas.getCountry())) {
                                fullAddress += vas.getCountry() + ", ";
                            }
                            if (!StringUtil.isNullOrEmpty(fullAddress)) {
                                fullAddress = fullAddress.substring(0, fullAddress.length() - 2);
                            }
                            obj.put("addressExciseBuyer", fullAddress);
                            obj.put("billingState", vas.getState() != null ? vas.getState() : "");
                            object.put("billingState", vas.getState());
                            object.put("billingCity", vas.getCity());
                            object.put("billingCounty", vas.getCounty());
                            /**
                             * Below Key used only for INDIA country if *Show vendors address in purchase document* flag is OFF
                             */
                            obj.put("vendorbillingStateForINDIA", vas.getState() != null ? vas.getState() : "");
                            object.put("vendorbillingStateForINDIA", vas.getState());
                        }
                    }
                }
                
                obj.put("masterReceivedForm", StringUtil.isNullObject(row[38]) ? "" : row[38].toString());
                obj.put("paymentCriteria", StringUtil.isNullObject(row[39]) ? "" : row[39].toString());
                obj.put("defaultnatureofpurchase", StringUtil.isNullObject(row[40]) ? "" : row[40].toString());
                obj.put("manufacturertype",StringUtil.isNullObject(row[41]) ? "" : row[41].toString());
                obj.put("currencysymbol", StringUtil.isNullObject(row[42]) ? "" : row[42].toString());
                obj.put("currencyname", StringUtil.isNullObject(row[43]) ? "" : row[43].toString());
                
                if (!receivableAccFlag) {                    
                    obj.put("billto", StringUtil.isNullObject(row[36]) ? "" : row[36].toString());
                    obj.put("email", StringUtil.isNullObject(row[37]) ? "" : row[37].toString());
                    obj.put("termdays", StringUtil.isNullObject(row[44]) ? "" : row[44].toString());
                    obj.put("termid", StringUtil.isNullObject(row[45]) ? "" : row[45].toString());
                    obj.put("groupname", StringUtil.isNullObject(row[46]) ? "" : row[46].toString());
                    obj.put("deleted", StringUtil.isNullObject(row[47]) ? "" : row[47].toString());
//                    obj.put("mappedAccountTaxId", StringUtil.isNullObject(row[48]) ? "" : row[48].toString());
                    obj.put("mappedAccountTaxId", !StringUtil.isNullObject(row[48]) ? (accAccountDAOobj.isTaxActivated(companyId, row[48].toString()) ? row[48].toString() : "") : "");
                }
                addressParams.put("companyid", companyid);
                addressParams.put("vendorid", StringUtil.isNullObject(row[0]) ? "" : row[0].toString());
                VendorAddressDetails vendorAddressDetail = accountingHandlerDAOobj.getVendorAddressObj(addressParams);
                vendorEmailId = vendorAddressDetail != null ? vendorAddressDetail.getEmailID() : "";
                obj.put("billingEmail",vendorEmailId);
                
                /**
                 * ERP-32829 
                 * code for vendor address for GST
                 */
                addressParams.put("isBillingAddress", false);//only billing address   
                vendorAddressDetail = accountingHandlerDAOobj.getVendorAddressObj(addressParams);
                JSONArray currentAddressDetailrec=new JSONArray();
                if(vendorAddressDetail!=null){
                  object.put("vendorShippingState", vendorAddressDetail.getState());
                  object.put("vendorShippingCity", vendorAddressDetail.getCity());
                  object.put("vendorShippingCounty", vendorAddressDetail.getCounty());
                  /**
                   * For GST below key used to set dimension value while selecting Customer and Vendor
                   * ERP-38084
                   * Check GSTCalculation.js - "populateGSTDimensionValues"  function 
                   */
                    if (extraPref.isIsNewGST()) {
                        object.put("shippingState", vendorAddressDetail.getState());
                        object.put("shippingCity", vendorAddressDetail.getCity());
                        object.put("shippingCounty", vendorAddressDetail.getCounty());
                        /**
                         * For US country below Shipping address key used if Show vendor Address check OFF from company pref.
                         */
                        object.put("vendcustShippingState", vendorAddressDetail.getState());
                        object.put("vendcustShippingCity", vendorAddressDetail.getCity());
                        object.put("vendcustShippingCounty", vendorAddressDetail.getCounty());
                    }
                }
                currentAddressDetailrec.put(object);
                obj.put("currentAddressDetailrec", currentAddressDetailrec);
                /**
                 * Address - Dimension mapping
                 */
                object.put("companyid", companyid);
                currentAddressDetailrec = fieldDataManagercntrl.getAddressDimensionMapping(object);
                obj.put("addressMappingRec", currentAddressDetailrec);
                String type = row[49] != null ? row[49].toString() : "";
                String defaultMasterItemID= "";
                /**
                 * Get Master item default for Vendor type , To handle special case
                 */
                if (!StringUtil.isNullOrEmpty(type)) {
                    KwlReturnObject retObj = accMasterItemsDAOobj.getMasterItem(type);
                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                        MasterItem reasonObj = (MasterItem) retObj.getEntityList().get(0);
                        defaultMasterItemID = reasonObj.getDefaultMasterItem()!=null ? reasonObj.getDefaultMasterItem().getID() : "";
                        obj.put("CustVenTypeDefaultMstrID", defaultMasterItemID);
                    }
                }
                obj.put("uniqueCase", getUniqueCase(obj.put("type",defaultMasterItemID)));
                obj.put("sezfromdate", StringUtil.isNullObject(row[50]) ? "" : row[50].toString());
                obj.put("seztodate", StringUtil.isNullObject(row[51]) ? "" : row[51].toString());
                //Get Vendor GST details
                obj.put("gstin", row[52]==null ? "" : row[52]);
                obj.put("GSTINRegistrationTypeId", row[53]==null ? "" : row[53]);
                /**
                 * Get Master item default for Vendor type , GST Registration Type
                 */
                if (row.length>=53 && row[53] !=null && !StringUtil.isNullOrEmpty(row[53].toString())) {
                    KwlReturnObject retObj = accMasterItemsDAOobj.getMasterItem(row[53].toString());
                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                        MasterItem reasonObj = (MasterItem) retObj.getEntityList().get(0);
                        defaultMasterItemID = reasonObj.getDefaultMasterItem() != null ? reasonObj.getDefaultMasterItem().getID() : "";
                        obj.put("GSTINRegTypeDefaultMstrID", defaultMasterItemID);
                        if (defaultMasterItemID.equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Composition)) || defaultMasterItemID.equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Composition_ECommerce))) {
                            obj.put("uniqueCase", Constants.NOGST);
                        }
                    }
                    }
                obj.put("CustomerVendorTypeId", type);
                obj.put("considerExemptLimit", row[54]==null ?false:row[54]);  
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accVendorController.getVendorsForCombo : " + ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * * @DESC : Get type of Vendor
     * @param object
     * @return
     */
    private int getUniqueCase(JSONObject object) {
        int uniqueCase = Constants.APPLYGST;
        String type = object.optString("type");
        if (!StringUtil.isNullOrEmpty(type)) {
            if (type.equalsIgnoreCase(Constants.CUSTVENTYPE.get("Export (WPAY)"))) {
                return Constants.APPLY_IGST;
            } else if (type.equalsIgnoreCase(Constants.CUSTVENTYPE.get("Export (WOPAY)"))) {
                return Constants.NOGST;
            } else if (type.equalsIgnoreCase(Constants.CUSTVENTYPE.get("Import"))) {
                return Constants.NOGST;
            }else if (type.equalsIgnoreCase(Constants.CUSTVENTYPE.get("SEZ (WPAY)"))) {
                return Constants.APPLY_IGST;
            }else if (type.equalsIgnoreCase(Constants.CUSTVENTYPE.get("SEZ (WOPAY)"))) {
                return Constants.NOGST;
            }else if (type.equalsIgnoreCase(Constants.CUSTVENTYPE.get("Tax Exempt"))) {
                return Constants.NOGST;
            }
        }
        return uniqueCase;
    }
    public static HashMap<String, Object> getVendorRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        String[] groups = request.getParameterValues("group");
        String[] groupsAfterAdding = groups;
        //To do - Need to check this.
//        if (groups != null) {
//            List<String> groupsList = new ArrayList<String>(Arrays.asList(groups));
//            Set groupsSet = new HashSet(Arrays.asList(groups));
//            if (groupsSet.contains(Group.ACCOUNTS_PAYABLE)&&!groupsSet.contains(Group.BILLS_PAYABLE)) {
//                groupsList.add(Group.BILLS_PAYABLE);
//            }
//            groupsAfterAdding=groupsList.toArray(new String[groupsList.size()]);
//        }
        requestParams.put("group", groupsAfterAdding);
        requestParams.put("ignore", request.getParameter("ignore"));
        requestParams.put("ignorecustomers", request.getParameter("ignorecustomers"));
        requestParams.put("ignorevendors", request.getParameter("ignorevendors"));
        if (request.getParameter("accountid") != null && !StringUtil.isNullOrEmpty(request.getParameter("accountid"))) {
            requestParams.put("accountid", request.getParameter("accountid"));
        }
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("getSundryVendor", request.getParameter("getSundryVendor"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        if (request.getParameter("query") != null && !StringUtil.isNullOrEmpty(request.getParameter("query"))) {
            requestParams.put("ss", request.getParameter("query"));
        } else if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
            requestParams.put("ss", request.getParameter("ss"));
        }
        if(request.getParameter("searchstartwith") !=null)//Search text starting with
        {
            requestParams.put("searchstartwith",request.getParameter("searchstartwith"));
        }
        if(request.getParameter("cmpRecordField") !=null)
        {
            requestParams.put("cmpRecordField",request.getParameter("cmpRecordField"));
        }

        if (StringUtil.isNullOrEmpty(request.getParameter("filetype"))) {
            if (request.getParameter("start") != null) {
                requestParams.put("start", request.getParameter("start"));
            }
            if (request.getParameter("limit") != null) {
                requestParams.put("limit", request.getParameter("limit"));
            }
        }

        if (request.getParameter("comboCurrencyid") != null) {
            requestParams.put("comboCurrencyid", request.getParameter("comboCurrencyid"));
        }
        if (request.getParameter("receivableAccFlag") != null && !StringUtil.isNullOrEmpty(request.getParameter("receivableAccFlag"))) {
            requestParams.put("receivableAccFlag", request.getParameter("receivableAccFlag"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
            requestParams.put("dir", request.getParameter("dir"));
            requestParams.put("sort", request.getParameter("sort"));
        }
        requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
        requestParams.put(Constants.vendorid, request.getParameter(Constants.vendorid));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        if (request.getParameter("isIBGVendors") != null && !StringUtil.isNullOrEmpty(request.getParameter("isIBGVendors"))) {
            requestParams.put("isIBGVendors", request.getParameter("isIBGVendors"));
            requestParams.put("bankType", request.getParameter("bankType"));
            
        }
        if (request.getParameter("activeDormantFlag") != null && !StringUtil.isNullOrEmpty(request.getParameter("activeDormantFlag"))) {
            requestParams.put("activeDormantFlag", request.getParameter("activeDormantFlag"));
        }
        /*
            Vendor ids for exporting selected Vendors
        */
        String exportvendorids=request.getParameter("exportcustvenids");
        if(!StringUtil.isNullOrEmpty(exportvendorids)){
            requestParams.put("exportvendors", exportvendorids.substring(0, exportvendorids.length()-1));
        }
        return requestParams;
    }

    // method moved to Main Controller. it is in no more use
    public JSONArray getVendorJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Vendor vendor = (Vendor) row[1];
                Account account = vendor.getAccount();
                if (vendor == null) {
                    continue;
                }
                JSONObject obj = new JSONObject();
                obj.put("acccode", (StringUtil.isNullOrEmpty(vendor.getAcccode())) ? "" : vendor.getAcccode());
//                obj.put("acccode",(StringUtil.isNullOrEmpty(vendor.getAcccode()))?"":vendor.getAcccode());
                obj.put("accid", vendor.getID());
                obj.put("accname", vendor.getName());
                obj.put("accnamecode", (StringUtil.isNullOrEmpty(vendor.getAcccode())) ? vendor.getName() : "[" + vendor.getAcccode() + "]" + vendor.getName());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                obj.put("openbalance", account.getOpeningBalance());
                Vendor parentVendor = (Vendor) row[5];
                if (parentVendor != null) {
                    obj.put("parentid", parentVendor.getID());
                    obj.put("parentname", parentVendor.getName());
                } else if (vendor.getParent() != null) {
                    obj.put("parentid", vendor.getParent().getID());
                    obj.put("parentname", vendor.getParent().getName());
                }
                KWLCurrency currency = (KWLCurrency) row[4];
                obj.put("currencyid", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getSymbol()));
                obj.put("currencyname", (account.getCurrency() == null ? currency.getName() : account.getCurrency().getName()));
                obj.put("level", row[2]);
                obj.put("leaf", row[3]);
                obj.put("id", vendor.getID());
                obj.put("title", vendor.getTitle());
                obj.put("address", vendor.getAddress());
                obj.put("baddress2", vendor.getAddress2());
                obj.put("baddress3", vendor.getAddress3());
                obj.put("email", vendor.getEmail());
                obj.put("contactno", vendor.getContactNumber());
                obj.put("contactno2", vendor.getAltContactNumber());
                obj.put("istaxeligible", vendor.isTaxEligible() ? "Yes" : "No");
                obj.put("fax", vendor.getFax());
                obj.put("pdm", vendor.getPreferedDeliveryMode());
                obj.put("termname", vendor.getDebitTerm() == null ? "" : vendor.getDebitTerm().getTermname());
                obj.put("termdays", vendor.getDebitTerm() == null ? "" : vendor.getDebitTerm().getTermdays());
                obj.put("termid", vendor.getDebitTerm() == null ? "" : vendor.getDebitTerm().getID());
                obj.put("bankaccountno", vendor.getBankaccountno());
                obj.put("other", (vendor.getOther() != null) ? vendor.getOther() : "");
                obj.put("billto", vendor.getAddress());
                obj.put("categoryid", getVendorCategoryIDs(account.getID()));
                obj.put("intercompanytypeid", account.getIntercompanytype() == null ? "" : account.getIntercompanytype().getID());
                obj.put("intercompany", account.isIntercompanyflag());
                obj.put("taxeligible", vendor.isTaxEligible());
                obj.put("taxidnumber", vendor.getTaxIDNumber());
                obj.put("creationDate", authHandler.getUserDateFormatterWithoutTimeZone(request).format(vendor.getAccount().getCreationDate()));
                obj.put("deleted", vendor.getAccount().isDeleted());
                obj.put("limit", vendor.getDebitlimit());
                obj.put("mapcustomervendor", vendor.isMapcustomervendor());
                obj.put("contactperson", (vendor.getContactperson() == null) ? "" : vendor.getContactperson());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getVendorJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public String getVendorCategoryIDs(String vendorid) {
        JSONObject jobj = new JSONObject();
        String valuesStr = "";
        boolean issuccess = false;
        try {
            KwlReturnObject result = accVendorDAOobj.getVendorCategoryIDs(vendorid);

            List list = result.getEntityList();
            Iterator itr = list.iterator();

            while (itr.hasNext()) {
                VendorCategoryMapping row = (VendorCategoryMapping) itr.next();
                MasterItem masterItemObj = row.getVendorCategory();
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
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return valuesStr;
    }

    public ModelAndView saveIBGReceivingBankDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String vendorID = null, msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Vendor_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {

            IBGReceivingBankDetails receivingBankDetails = accVendorControllerServiceObj.saveIBGReceivingBankDetails(request);
            
            issuccess = true;

            msg = messageSource.getMessage("acc.comman.receiving.saved", null, RequestContextUtils.getLocale(request));   //IBG-Receiving Bank Details has been saved successfully.

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("perAccID", vendorID);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    public ModelAndView saveVendorMailingDate(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String vendorID = null, msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Vendor_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            Vendor vendor = saveVendorMailingDate(request);
            vendorID = vendor.getAccount().getID();

            issuccess = true;
            msg = messageSource.getMessage("acc.ven.save", null, RequestContextUtils.getLocale(request));   //"Vendor information has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("perAccID", vendorID);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public Vendor saveVendorMailingDate(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        Vendor vendor = null;
        try {
            Date mailedOn = request.getParameter("taxidmailon") == null ? null : authHandler.getDateOnlyFormat(request).parse(request.getParameter("taxidmailon"));
            if (mailedOn == null) {
                mailedOn = new Date();
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("accid", request.getParameter("accid"));
            requestParams.put("taxidmailon", mailedOn);
            KwlReturnObject result;
            result = accVendorDAOobj.updateVendor(requestParams);
            List ll = result.getEntityList();
            vendor = (Vendor) ll.get(0);
        } catch (ParseException ex) {
//            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveVendor : " + ex.getMessage(), ex);
        }
        return vendor;
    }

    public ModelAndView getAddresses(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jSONObject = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        String msg = "";
        boolean issuccess = true;
        try {

            String vendorid = request.getParameter("customerid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            addrRequestParams.put("vendorid", vendorid);
            addrRequestParams.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(request.getParameter("isBillingAddress"))) {
                boolean isBillingAddress = Boolean.parseBoolean((String) request.getParameter("isBillingAddress"));
                addrRequestParams.put("isBillingAddress", isBillingAddress);
            }
                KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
                if(!addressResult.getEntityList().isEmpty()){
                    List <VendorAddressDetails> vendAddrList=addressResult.getEntityList();
                    for(VendorAddressDetails vendAddr:vendAddrList){
                        JSONObject addrObject=new JSONObject();
                         addrObject.put("aliasName", vendAddr.getAliasName()!=null?vendAddr.getAliasName():"");             
                         addrObject.put("address", vendAddr.getAddress()!=null?vendAddr.getAddress():"");       
                         addrObject.put("county", vendAddr.getCounty()!=null?vendAddr.getCounty():"");       
                         addrObject.put("city", vendAddr.getCity()!=null?vendAddr.getCity():"");       
                         addrObject.put("state", vendAddr.getState()!=null?vendAddr.getState():"");       
                         addrObject.put("country", vendAddr.getCountry()!=null?vendAddr.getCountry():"");       
                         addrObject.put("postalCode", vendAddr.getPostalCode()!=null?vendAddr.getPostalCode():"");       
                         addrObject.put("phone", vendAddr.getPhone()!=null?vendAddr.getPhone():"");       
                         addrObject.put("mobileNumber", vendAddr.getMobileNumber()!=null?vendAddr.getMobileNumber():"");       
                         addrObject.put("fax", vendAddr.getFax()!=null?vendAddr.getFax():"");       
                         addrObject.put("emailID", vendAddr.getEmailID()!=null?vendAddr.getEmailID():"");       
                         addrObject.put("recipientName", vendAddr.getRecipientName()!=null?vendAddr.getRecipientName():"");       
                         addrObject.put("contactPerson", vendAddr.getContactPerson()!=null?vendAddr.getContactPerson():"");       
                         addrObject.put("contactPersonNumber", vendAddr.getContactPersonNumber()!=null?vendAddr.getContactPersonNumber():"");       
                         addrObject.put("contactPersonDesignation", vendAddr.getContactPersonDesignation()!=null?vendAddr.getContactPersonDesignation():"");       
                         addrObject.put("website", vendAddr.getWebsite()!=null?vendAddr.getWebsite():"");       
                         addrObject.put("isDefaultAddress", vendAddr.isIsDefaultAddress());  
                         addrObject.put("isBillingAddress", vendAddr.isIsBillingAddress());  
                         jSONArray.put(addrObject);
                    }                    
                }
            jSONObject.put("data", jSONArray);
            jSONObject.put("count", jSONArray.length());
        } catch (Exception ex) {
            issuccess = false;
            msg = "accVendorController.getAddresses : " + ex;
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jSONObject.put("success", issuccess);
                jSONObject.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jSONObject.toString());
    }

    public ModelAndView exportVendor(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = getVendorRequestMap(request);
            KwlReturnObject result = accVendorDAOobj.getVendor(requestParams);
            ArrayList list = accAccountDAOobj.getAccountArrayList(result.getEntityList(), requestParams, false, false);
            JSONArray jArr = getVendorJson(request, list);
            jobj.put("data", jArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView importVendor(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", companyid);
            CompanyAccountPreferences companyAccountPref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.hql.accounting.CompanyAccountPreferences", companyid);
            String baseUrl =com.krawler.common.util.URLUtil.getPageURL(request,loginpageFull,extraPref.getCompany().getSubDomain());
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
            requestParams.put("moduleName", Constants.Acc_Vendor_modulename);
            requestParams.put("moduleid", "b8bd81b0-c500-102d-bb0b-001e58a64cb6");
            requestParams.put("isActivateToDateforExchangeRates", extraPref.isActivateToDateforExchangeRates());//variable needs while fetching exchange rate
            requestParams.put("isCurrencyCode",extraPref.isCurrencyCode());
            requestParams.put("isTDSapplicable",extraPref.isTDSapplicable());       //ERP-26934     
            requestParams.put("isExciseApplicable",extraPref.isExciseApplicable()); //ERP-26934     
            requestParams.put("isEnableVatCst",extraPref.isEnableVatCst()); 
            requestParams.put("updateExistingRecordFlag", updateExistingRecordFlag);
            requestParams.put("baseUrl", baseUrl);
            requestParams.put("bookBeginningDate", companyAccountPref.getBookBeginningFrom());//ERP-38015
            if(extraPref.getCompany().getCountry()!=null){
               requestParams.put("countryid",extraPref.getCompany().getCountry().getID());//ERP-26934 
            }
            if(extraPref.getCompany().getState()!=null){
               requestParams.put("stateid",extraPref.getCompany().getState().getID()); 
            }
            if(updateExistingRecordFlag){
                requestParams.put("allowDuplcateRecord", updateExistingRecordFlag);
            }
            
            if (doAction.compareToIgnoreCase("import") == 0 || doAction.compareToIgnoreCase("xlsImport") == 0) {
                requestParams.put("action",doAction);
//                requestParams.put("updateExistingRecordFlag", updateExistingRecordFlag);
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
                    requestParams.put("locale", RequestContextUtils.getLocale(request));
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
                            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
                jobj.put("exceededLimit", exceededLimit);
                System.out.println("A(( Import end : " + new Date());
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                System.out.println("A(( Validation start : " + new Date());
                jobj = importHandler.validateFileData(requestParams);
                System.out.println("A(( Validation end : " + new Date());
                jobj.put("success", true);
            }
        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getVendorsByCategory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));

//            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
//            filter_names.add("company.companyID");
//            filter_params.add(sessionHandlerImpl.getCompanyid(request));
//            filter_names.add("ISaccount.deleted");
//            filter_params.add(false);
//            requestParams.put("filter_names", filter_names);
//            requestParams.put("filter_params", filter_params);
//            order_by.add("account.category");
//            order_type.add("desc");
//            requestParams.put("order_by", order_by);
//            requestParams.put("order_type", order_type);
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("categoryid", request.getParameter("categoryid"));

            KwlReturnObject result = accVendorDAOobj.getNewVendorList(requestParams);
            JSONArray jArr = getCustomersByCategoryJson(request, result.getEntityList());

            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
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
                String Vendid = row[0].toString();
                String CategoryId = row[1] != null ? row[1].toString() : "";
                KwlReturnObject vendresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), Vendid);
                Vendor vendor = (Vendor) vendresult.getEntityList().get(0);
                MasterItem masterItem = null;
                if (!StringUtil.isNullOrEmpty(CategoryId)) {
                    KwlReturnObject catresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), CategoryId);
                    masterItem = (MasterItem) catresult.getEntityList().get(0);
                }

                Account account = vendor.getAccount();

                if (account.isDeleted()) {
//                    continue;
                }
                JSONObject obj = new JSONObject();
                obj.put("accid", vendor.getID());
                obj.put("accname", vendor.getName());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                obj.put("openbalance", account.getOpeningBalance());

                obj.put("currencyid", (account.getCurrency() == null ? "" : account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (account.getCurrency() == null ? "" : account.getCurrency().getSymbol()));
                obj.put("currencyname", (account.getCurrency() == null ? "" : account.getCurrency().getName()));

                obj.put("id", vendor.getID());
                obj.put("title", vendor.getTitle());
                obj.put("address", vendor.getAddress());
                obj.put("email", vendor.getEmail());
                obj.put("contactno", vendor.getContactNumber());
                obj.put("contactno2", vendor.getAltContactNumber());
                obj.put("fax", vendor.getFax());
                obj.put("pdm", vendor.getPreferedDeliveryMode());
                obj.put("termname", vendor.getDebitTerm().getTermname());
                obj.put("termdays", vendor.getDebitTerm().getTermdays());
                obj.put("termid", vendor.getDebitTerm().getID());
                obj.put("bankaccountno", vendor.getBankaccountno());
                obj.put("other", vendor.getOther());
                obj.put("billto", vendor.getAddress());
                obj.put("creationDate", authHandler.getUserDateFormatterWithoutTimeZone(request).format(vendor.getAccount().getCreationDate()));
                obj.put("deleted", vendor.getAccount().isDeleted());
                obj.put("categoryid", masterItem == null ? "" : masterItem.getID());
                obj.put("category", masterItem == null ? "" : masterItem.getValue());

                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCustomersByCategoryJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView saveCIMBReceivingBankDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String vendorID = null, msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Vendor_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {

            CIMBReceivingDetails receivingBankDetails = accVendorControllerServiceObj.saveCIMBReceivingBankDetails(request);
            
            issuccess = true;

            msg = messageSource.getMessage("acc.comman.receiving.saved", null, RequestContextUtils.getLocale(request));   //IBG-Receiving Bank Details has been saved successfully.

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("perAccID", vendorID);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());

    }
    
    public ModelAndView getVendorsIdNameForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        JSONArray jArr = new JSONArray();
        List<Object[]> selectedvendorList = new ArrayList();
        try {
            HashMap<String, Object> requestParams = getVendorRequestMap(request);
            String selectedvendorIds = request.getParameter("combovalue");
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", sessionHandlerImpl.getCompanyid(request));
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                int permCode = sessionHandlerImpl.getPerms(request, Constants.VENDOR_PERMCODE);
                if (!((permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.VENDOR_VIEWALL_PERMCODE) ==
                     * Constants.VENDOR_VIEWALL_PERMCODE is true then user has
                     * permission to view all vendors documents,so at that time
                     * there is need to filter record according to user&agent.
                     */
                    String userId = sessionHandlerImpl.getUserid(request);
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                }
            }
            
            /**
             * Block used to get selected customers using their ids from SOA- customer account statement.
             */
            if (!StringUtil.isNullOrEmpty(selectedvendorIds) && !selectedvendorIds.equals("All")) {
                requestParams.put("multiselectedvendorIds", selectedvendorIds);
                requestParams.put("ismultiselectvendoridsFlag", true);
                KwlReturnObject selectedvendor =  accVendorDAOobj.getVendorsForCombo(requestParams);
                requestParams.remove("ismultiselectvendoridsFlag");
                selectedvendorList = selectedvendor.getEntityList();
            }
            
            requestParams.put("customervendorsortingflag", extraPref.isCustomerVendorSortingFlag());
            KwlReturnObject result = accVendorDAOobj.getVendorsForCombo(requestParams);
            String excludeaccountid = (String) requestParams.get("accountid");
            String includeaccountid = (String) requestParams.get("includeaccountid");
            String includeparentid = (String) requestParams.get("includeparentid");

            List list = result.getEntityList();
            selectedvendorList.addAll(list);
            HashMap<String, Object> addressParams = new HashMap<String, Object>();
            addressParams.put("isDefaultAddress", true);
            addressParams.put("isBillingAddress", true); //true to get billing address

            for (Object vendor : selectedvendorList) {
                Object[] row = (Object[]) vendor;

                if (excludeaccountid != null && row[0] != null && row[0].equals(excludeaccountid)) {
                    continue;
                }
                if ((includeparentid != null && row[0] != null && (!row[0].equals(includeparentid) || (row[1] != null && !row[1].equals(includeparentid))))) {
                    continue;
                } else if ((includeaccountid != null && row[0] != null && !row[0].equals(includeaccountid))) {
                    continue;
                }

                JSONObject obj = new JSONObject();
                obj.put("accid", StringUtil.isNullObject(row[0]) ? "" : row[0].toString());
                obj.put("acccode", StringUtil.isNullObject(row[2]) ? "" : row[2].toString());
                obj.put("accname", StringUtil.isNullObject(row[15]) ? "" : row[15].toString());
                if (row[23] != null) {
                         obj.put("hasAccess", Boolean.parseBoolean(row[23].toString()));
                }
                obj.put("groupname", StringUtil.isNullObject(row[46]) ? "" : row[46].toString());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accVendorController.getVendorsForCombo : " + ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
