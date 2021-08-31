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

import com.krawler.common.admin.*;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.account.accCusVenMapDAO;
import com.krawler.spring.accounting.account.accVendorCustomerProductDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendor.accVendorControllerServiceImpl;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;

import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import java.text.ParseException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.RequestContextUtils;
/**
 *
 * @author krawler
 */
public class accCustomerControllerCMNServiceImpl implements accCustomerControllerCMNService,MessageSourceAware{
    private accCustomerDAO accCustomerDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accVendorCustomerProductDAO accVendorCustomerProductDAOobj;
    private fieldDataManager fieldDataManagercntrl;
    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private auditTrailDAO auditTrailObj;
    private accAccountDAO accAccountDAOobj;
    private accProductDAO accProductObj;
    private accJournalEntryDAO accJournalEntryobj;
    private accPaymentDAO accPaymentDAOobj;
    private accTaxDAO accTaxObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accCusVenMapDAO accCusVenMapDAOObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accVendorDAO accVendorDAOobj;
    private companyDetailsDAO companyDetailsDAOObj;
    private ImportHandler importHandler;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    
    public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj1) {
        this.companyDetailsDAOObj = companyDetailsDAOObj1;
    }
    
    public void setAccCusVenMapDAOObj(accCusVenMapDAO accCusVenMapDAOObj) {
        this.accCusVenMapDAOObj = accCusVenMapDAOObj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    @Override
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public accVendorCustomerProductDAO getAccVendorCustomerProductDAOobj() {
        return accVendorCustomerProductDAOobj;
    }

    public void setAccVendorCustomerProductDAOobj(accVendorCustomerProductDAO accVendorCustomerProductDAOobj) {
        this.accVendorCustomerProductDAOobj = accVendorCustomerProductDAOobj;
    }

    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

     public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
        public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    
 @Override   
    public JSONObject getCustomerFromCRMAccounts(HashMap<String, Object> reqParams, JSONObject resObj) throws ServiceException {
        JSONObject job;
        JSONArray jArr = new JSONArray();
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        long failedCount = 0, duplicateCount = 0, successCount = 0;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String companyid = (String) reqParams.get("companyid");;
            JSONArray customerArr = resObj.optJSONArray("data");
            boolean syncBasedOnAccountCode = resObj.optBoolean("syncBasedOnAccountCode", false);
            if (customerArr.length() > 0) {
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

                String gcurrencyid = "";
                if(preferences!=null){
                    gcurrencyid = preferences.getCompany().getCurrency()!=null?preferences.getCompany().getCurrency().getCurrencyID():"";
                }

                for (int i = 0; i < customerArr.length(); i++) {
                    JSONObject custObj = customerArr.getJSONObject(i);
                    String seqNumber = null;
                    Customer customer = null;
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    String accountname = custObj.isNull("accountname") ? "" : custObj.getString("accountname");
                    accountname = custObj.isNull("accname") ? accountname : custObj.getString("accname");
                    String currency = custObj.isNull("currency") ? "" : custObj.getString("currency");
                    currency = custObj.isNull("currencyname") ? currency : custObj.getString("currencyname");
                    String accountid = custObj.isNull("accountid") ? "" : custObj.getString("accountid");
                    String erpcustomerid = custObj.isNull("erpcustomerid") ? "" : custObj.getString("erpcustomerid");
                    Date creationDate = custObj.isNull("creationDate") ? new Date() : sdf.parse(custObj.getString("creationDate"));
                    String sequenceformatid = custObj.isNull("sequenceformatid") ? null : custObj.getString("sequenceformatid");
                    sequenceformatid = custObj.isNull("sequenceformatid") ? sequenceformatid : custObj.getString("sequenceformatid");
                    String accountcode = custObj.isNull("accountcode") ? "" : custObj.getString("accountcode");
                    accountcode = custObj.isNull("acccode") ? accountcode: custObj.getString("acccode");
                    String currencyid = custObj.isNull("currency") ? gcurrencyid : custObj.getString("currency");
                    if (StringUtil.isNullOrEmpty(accountcode) || StringUtil.isNullOrEmpty(sequenceformatid)) {
                        failedCount++;
                        continue;
                    }
                    if (creationDate == null) {
                        creationDate = new Date();
                    }

                    KwlReturnObject resultcheck = accCustomerDAOobj.checkCustomerExist(accountid, companyid);
                    if (!resultcheck.getEntityList().isEmpty()) {
                        customer = (Customer) resultcheck.getEntityList().get(0);
                    } else if (!StringUtil.isNullOrEmpty(erpcustomerid)) {
                        KwlReturnObject cstresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), erpcustomerid);
                        customer = (Customer) cstresult.getEntityList().get(0);
                    }
                    if (customer == null && syncBasedOnAccountCode) {
                        KwlReturnObject returnObj = accCustomerDAOobj.checkCustomerExistbyCode(accountcode, companyid);
                        if (!returnObj.getEntityList().isEmpty()) {
                            customer = (Customer) returnObj.getEntityList().get(0);
                        }
                    }

                    //For sequence format common code for both add & edit record
                    SequenceFormat sequenceFormat = null;
                    boolean isAutoGenerated = false;
                    if (StringUtil.isNullOrEmpty(sequenceformatid)) { //if sequnece format is not coming from CRM in this case giving exception
                        failedCount++;
                        continue;
                    } else if (sequenceformatid.equalsIgnoreCase("NA")) {
                        /**
                         * ERP-25311 While syncing customer from CRM to ERP, if
                         * acccode matches any sequence format at ERP then
                         * sequence format is saved for such customer.
                         */
                        List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Customer_ModuleId, accountcode, companyid);
                        if (!resultList.isEmpty()) {
                            seqNumber = String.valueOf(resultList.get(2));
                            String formatid = (String) resultList.get(3);
                            if (!StringUtil.isNullOrEmpty(formatid)) {
                                KwlReturnObject seqFormatResult = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), formatid);
                                sequenceFormat = (SequenceFormat) seqFormatResult.getEntityList().get(0);
                                sequenceformatid = formatid;
                                isAutoGenerated = true;
                            }
                        }
                        if (!syncBasedOnAccountCode) {
                            String customerid = customer == null ? "" : customer.getID();
                            KwlReturnObject returnObj = accCustomerDAOobj.checkCustomerExistbyCode(accountcode, companyid, customerid);
                            List listObj = returnObj.getEntityList();
                            if (!listObj.isEmpty()) {
                                duplicateCount++;
                                continue;
                            }
                        }
                    } else if (customer == null && !StringUtil.isNullOrEmpty(sequenceformatid) && !sequenceformatid.equalsIgnoreCase("NA")) {
//                            SequenceFormat seqFormat = (SequenceFormat) session.get(SequenceFormat.class, sequenceformatid);
                        KwlReturnObject seqFormatResult = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), sequenceformatid);
                        SequenceFormat seqFormat = (SequenceFormat) seqFormatResult.getEntityList().get(0);

                        if (seqFormat == null) {
                            failedCount++;
                            continue;
                        }

                        if (!syncBasedOnAccountCode) {
                            String customerid = customer == null ? "" : customer.getID();
                            KwlReturnObject returnObj = accCustomerDAOobj.checkCustomerExistbyCode(accountcode, companyid, customerid);
                            List listObj = returnObj.getEntityList();
                            if (!listObj.isEmpty()) {
                                duplicateCount++;
                                continue;
                            }
                        }
                        sequenceFormat = seqFormat;
                        isAutoGenerated = true;
                    }

                    boolean isCustomerPreExist = false;
                    if (customer != null) {
                        requestParams.put("accname", accountname);
                        HashMap<String, Object> params = new HashMap<String, Object>();
                        params.put("accid", customer.getID());
                        params.put("accname", accountname);

                        if (sequenceformatid.equalsIgnoreCase("NA")) {
                            params.put("acccode", accountcode);
                        } else {
                            params.put(Constants.SEQFORMAT, sequenceformatid);
                            params.put(Constants.SEQNUMBER, seqNumber);
                            params.put("autogenerated", isAutoGenerated);
                        }

                        params.put("crmaccountid", accountid);
                        KwlReturnObject result = accCustomerDAOobj.updateCustomer(params);
                        isCustomerPreExist = true;
                    } else {

                        if (preferences != null) {
                            requestParams.put("accountid", preferences.getCustomerdefaultaccount().getID());
                        }

                        requestParams.put("synchedfromotherapp", true);
                        requestParams.put("crmaccountid", accountid);
                        requestParams.put("autogenerated", isAutoGenerated);
                        requestParams.put("acccode", accountcode);
                        requestParams.put(Constants.SEQFORMAT, sequenceFormat != null ? sequenceFormat.getID() : null);
                        requestParams.put(Constants.SEQNUMBER, sequenceFormat != null && !sequenceformatid.equalsIgnoreCase("NA")? seqNumber : null);
                        requestParams.put("accname", accountname);
                        requestParams.put("openbalance", 0);
                        requestParams.put("companyid", companyid);
                        requestParams.put("overseas", false);
                        requestParams.put("mapcustomervendor", false);
                        requestParams.put("creationDate", creationDate);
                        requestParams.put("currencyid", currencyid);

                        resultcheck = accCustomerDAOobj.getDefaultCreditTermForCustomer(companyid);
                        Term term = null;
                        if (!resultcheck.getEntityList().isEmpty()) {
                            term = (Term) resultcheck.getEntityList().get(0);
                            if (term != null) {
                                requestParams.put("termid", term.getID());
                            }
                        }

                        KwlReturnObject result = accCustomerDAOobj.addCustomer(requestParams);
                        customer = (Customer) result.getEntityList().get(0);

                    }

                    String customfield = custObj.optString("customfield", null);
                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        JSONArray jcustomarray = new JSONArray(customfield);
                        if (jcustomarray.length() > 0) {
                          
                            /* It is not called from CRM So last parameter is being sent "false" */
                            jcustomarray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_Customer_ModuleId, companyid, 0, false);            // 1= for line item
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_Customer_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_CustomerId);
                            customrequestParams.put("modulerecid", customer.getID());
                            customrequestParams.put("moduleid", Constants.Acc_Customer_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            customrequestParams.put("customdataclasspath", Constants.Acc_Customer_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                requestParams.put("acccustomercustomdataref", customer.getID());
                                requestParams.put("accid", customer.getID());
                                KwlReturnObject accresult = accCustomerDAOobj.updateCustomer(requestParams);;
                            }
                        }
                    }
                    if (customer != null) {
                        // For Existing Customer If Default Address Present then updating it  
                        // For New Customer inserting a new default address if address coming from CRM 

                        if (custObj.has("billingAddress") && custObj.optJSONArray("billingAddress") != null) {
                            JSONArray billingAddressArray = custObj.optJSONArray("billingAddress");
                            for (int count = 0; count < billingAddressArray.length(); count++) {
                                JSONObject billAddrObject = billingAddressArray.getJSONObject(count);
                                HashMap<String, Object> custAddrMap = new HashMap<String, Object>();
                                boolean isDefault = billAddrObject.optBoolean("isDefaultAddress", false);
                                String aliasName = billAddrObject.optString("aliasName", "");
                                if (isCustomerPreExist) {
                                    //Logic for Pre Exist Customer
                                    //here we find address of same alias name. If it is present then we update it otherwise we insert a new entry for address. 
                                    if(isDefault){//if default Address came from CRM then that become default in ERP as well so need to update previous address as default false
                                        accountingHandlerDAOobj.updateCustomerAddressDefaultValueToFalse(customer.getID(),true);
                                    }
                                    String biilingAddressID = "";
                                    List addrDetail = getCustomerAddressInfoByAliasName(aliasName, customer.getID(), companyid, true);
                                    biilingAddressID = (String) addrDetail.get(0);
                                    custAddrMap.put("addressid", biilingAddressID);
                                }

                                custAddrMap.put("customerid", customer.getID());
                                custAddrMap.put("aliasName", StringUtil.isNullOrEmpty(billAddrObject.optString("aliasName", "")) ? "Billing Address1" : billAddrObject.optString("aliasName", ""));
                                custAddrMap.put("address", billAddrObject.optString("address", ""));
                                custAddrMap.put("county", billAddrObject.optString("county", ""));
                                custAddrMap.put("city", billAddrObject.optString("city", ""));
                                custAddrMap.put("state", billAddrObject.optString("state", ""));
                                custAddrMap.put("country", billAddrObject.optString("country", ""));
                                custAddrMap.put("postalCode", billAddrObject.optString("postalCode", ""));
                                custAddrMap.put("phone", billAddrObject.optString("phone", ""));
                                custAddrMap.put("mobileNumber", billAddrObject.optString("mobileNumber", ""));
                                custAddrMap.put("fax", billAddrObject.optString("fax", ""));
                                custAddrMap.put("emailID", billAddrObject.optString("emailID", ""));
                                custAddrMap.put("contactPerson", billAddrObject.optString("contactPerson", ""));
                                custAddrMap.put("website", billAddrObject.optString("website", ""));
                                custAddrMap.put("recipientName", billAddrObject.optString("recipientName", ""));
                                custAddrMap.put("contactPersonNumber", billAddrObject.optString("contactPersonNumber", ""));
                                custAddrMap.put("contactPersonDesignation", billAddrObject.optString("contactPersonDesignation", ""));
                                custAddrMap.put("isBillingAddress", true);
                                custAddrMap.put("isDefaultAddress", isDefault);
                                accountingHandlerDAOobj.saveCustomerAddressesDetails(custAddrMap, companyid);
                            }
                        }

                        if (custObj.has("shippingAddress") && custObj.optJSONArray("shippingAddress") != null) {
                            JSONArray shippingAddressArray = custObj.optJSONArray("shippingAddress");
                            for (int count = 0; count < shippingAddressArray.length(); count++) {
                                JSONObject shipAddrObject = shippingAddressArray.getJSONObject(count);
                                boolean isDefault = shipAddrObject.optBoolean("isDefaultAddress", false);
                                String aliasName = shipAddrObject.optString("aliasName", "");
                                HashMap<String, Object> custAddrMap = new HashMap<String, Object>();
                                if (isCustomerPreExist) {
                                    //Logic for Pre Exist Customer
                                    //here we find address of same alias name. If it is present then we update it otherwise we insert a new entry for address.         
                                    if(isDefault){//if default Address came from CRM then that become default in ERP as well so need to update previous address as default false
                                        accountingHandlerDAOobj.updateCustomerAddressDefaultValueToFalse(customer.getID(),false);
                                    }
                                    String shippingAddressID = "";
                                    List addrDetail = getCustomerAddressInfoByAliasName(aliasName, customer.getID(), companyid, false);
                                    shippingAddressID = (String) addrDetail.get(0);
                                    custAddrMap.put("addressid", shippingAddressID);
                                }

                                custAddrMap.put("customerid", customer.getID());
                                custAddrMap.put("aliasName", StringUtil.isNullOrEmpty(shipAddrObject.optString("aliasName", "")) ? "Shipping Address1" : shipAddrObject.optString("aliasName", ""));
                                custAddrMap.put("address", shipAddrObject.optString("address", ""));
                                custAddrMap.put("county", shipAddrObject.optString("county", ""));
                                custAddrMap.put("city", shipAddrObject.optString("city", ""));
                                custAddrMap.put("state", shipAddrObject.optString("state", ""));
                                custAddrMap.put("country", shipAddrObject.optString("country", ""));
                                custAddrMap.put("postalCode", shipAddrObject.optString("postalCode", ""));
                                custAddrMap.put("phone", shipAddrObject.optString("phone", ""));
                                custAddrMap.put("mobileNumber", shipAddrObject.optString("mobileNumber", ""));
                                custAddrMap.put("fax", shipAddrObject.optString("fax", ""));
                                custAddrMap.put("emailID", shipAddrObject.optString("emailID", ""));
                                custAddrMap.put("contactPerson", shipAddrObject.optString("contactPerson", ""));
                                custAddrMap.put("website", shipAddrObject.optString("website", ""));
                                custAddrMap.put("recipientName", shipAddrObject.optString("recipientName", ""));
                                custAddrMap.put("contactPersonNumber", shipAddrObject.optString("contactPersonNumber", ""));
                                custAddrMap.put("contactPersonDesignation", shipAddrObject.optString("contactPersonDesignation", ""));
                                custAddrMap.put("isBillingAddress", false);
                                custAddrMap.put("isDefaultAddress", isDefault);
                                accountingHandlerDAOobj.saveCustomerAddressesDetails(custAddrMap, companyid);
                            }
                        }
                    }
                    successCount++;
                    job = new JSONObject();
                    job.put("erpCustomerid", customer.getID());
                    job.put("accountid", customer.getCrmaccountid());
                    job.put("companyid", customer.getCompany().getCompanyID());
                    jArr.put(job);
                }

            }
            issuccess = true;
            msg += successCount + " Customer(s) has been synced successfully."+"<br>";
            msg += duplicateCount + " Customer(s) has been rejected due to duplicate Customer Code."+"<br>";
            msg += failedCount + " Customer(s) has been failed due to wrong sequence format id."+"<br>";
        } catch (Exception ex) {
            Logger.getLogger(accCustomerControllerCMNService.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("data", jArr);
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMNService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
 
 @Override  
    public List getCustomerAddressInfoByAliasName(String AliasName, String customerid, String companyid, boolean isbillingAddress) throws ServiceException {
        List addressDetails = new ArrayList();
        //Logic for Pre Exist Customer
        //here we find address of same alias name. If it is present then we return it ID . 
        HashMap<String, Object> addressParams = new HashMap<String, Object>();
        addressParams.put("companyid", companyid);
        addressParams.put("customerid", customerid);
        addressParams.put("isBillingAddress", isbillingAddress); //for billing address this falg will be true
        addressParams.put("aliasName", AliasName); //for billing address this falg will be true
        KwlReturnObject returnObject = accountingHandlerDAOobj.getCustomerAddressDetails(addressParams);
        String addressID = "";
        if (returnObject != null && !returnObject.getEntityList().isEmpty()) {
            CustomerAddressDetails details = (CustomerAddressDetails) returnObject.getEntityList().get(0);
            if (details != null) {
                addressID = details.getID();
            }
        }
        addressDetails.add(addressID);
        return addressDetails;
    }
    
@Override   
    public JSONObject deleteCustomer(HashMap<String, Object> reqParams, JSONObject resObj) throws ServiceException {
        JSONObject jobj = new JSONObject();
        HashMap returnMap = new HashMap<>();
        String msg = "";
        boolean issuccess = false;
        JSONArray propagatedCustomerjarr = null;
        boolean propagateTOChildCompaniesFalg = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Customer_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = (String) reqParams.get("companyid");;
            returnMap = deleteCustomerArray(resObj, companyid, propagateTOChildCompaniesFalg, propagatedCustomerjarr);

            issuccess = true;
            msg = messageSource.getMessage("acc.cus.del", null, Locale.forLanguageTag(resObj.getString(Constants.language)));   //"Customer has been deleted successfully";
            txnManager.commit(status);

        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.data, returnMap);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

@Override  
    public HashMap deleteCustomerArray(JSONObject resObj, String companyid, boolean propagateTOChildCompaniesFalg, JSONArray propagatedCustomerjarr) throws ServiceException, AccountingException, SessionExpiredException {
        HashMap returnMap = new HashMap<>();
        KwlReturnObject result = null;
        String auditID = "";
        HashSet deletedCustomerArray = new HashSet();
        HashSet nonDeletedCustomerArray = new HashSet();
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            JSONArray jArr = null;
            if (propagateTOChildCompaniesFalg) {
                jArr = propagatedCustomerjarr;
            } else {
                requestParams.put(Constants.data, resObj.optJSONArray("customerids"));
                jArr = resObj.optJSONArray("customerids");
            }

            Map<String, Object> auditTrailMap = new HashMap<>();
            auditTrailMap.put(Constants.useridKey, resObj.getString(Constants.creatoridKey));

            String accountid = "";
            String customerid = "";
            String customerName = "";
            String cusomerCode = "";
            for (int i = 0; i < jArr.length(); i++) {
                if (StringUtil.isNullOrEmpty(jArr.getString(i)) == false) {
                    accountid = jArr.getString(i);
                    Customer customer = null;
                    KwlReturnObject resultcheck = accCustomerDAOobj.checkCustomerExist(accountid, companyid);
                    if (!resultcheck.getEntityList().isEmpty()) {
                        customer = (Customer) resultcheck.getEntityList().get(0);
                        customerid = customer.getID();
                    }
                    
                    if(customer == null){
                        nonDeletedCustomerArray.add(accountid);
                        continue;
                    }
                    
                    customerName = customer.getName();
                    cusomerCode = customer.getAcccode();
                    if (!StringUtil.isNullOrEmpty(customerid)) {//Delete the productvendor mapping
                        accVendorCustomerProductDAOobj.deleteCustomerProductMapped(customerid);
                    }

                    // Check in Journal Entry
                    result = accJournalEntryobj.getJEDfromAccount(customerid, companyid);
                    int count = result.getRecordTotalCount();
                    if (count > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");
                        nonDeletedCustomerArray.add(accountid);
                        continue;
                    }

                    // Check Product Entry
                    result = accProductObj.getProductfromAccount(customerid, companyid);
                    count = result.getRecordTotalCount();
                    if (count > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the Account Preferences. So it cannot be deleted.");
                        nonDeletedCustomerArray.add(accountid);
                        continue;
                    }

                    // Check for Preferances Entry
                    result = accCompanyPreferencesObj.getPreferencesFromAccount(customerid, companyid);
                    count = result.getRecordTotalCount();
                    if (count > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the Product(s). So it cannot be deleted.");
                        nonDeletedCustomerArray.add(accountid);
                        continue;
                    }

                    // Check fot Payment Entry
                    result = accPaymentDAOobj.getPaymentMethodFromAccount(customerid, companyid);
                    count = result.getRecordTotalCount();
                    if (count > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the Term(s). So it cannot be deleted.");
                        nonDeletedCustomerArray.add(accountid);
                        continue;
                    }

                    // Check for Tax Entry
                    result = accTaxObj.getTaxFromAccount(customerid, companyid);
                    count = result.getRecordTotalCount();
                    if (count > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the Tax(s). So it cannot be deleted.");
                        nonDeletedCustomerArray.add(accountid);
                        continue;
                    }

                    // Check for Tax Entry
                    result = accCustomerDAOobj.getQuotationFromAccount(customerid, companyid);
                    count = result.getRecordTotalCount();
                    if (count > 0) {
//                            throw new AccountingException("Selected record(s) is currently used for Customer Quotations. So it cannot be deleted.");
                        nonDeletedCustomerArray.add(accountid);
                        continue;
                    }
                    // Check for Delivery Order Entry
                    result = accCustomerDAOobj.getDeliveryOrderFromAccount(customerid, companyid);
                    count = result.getRecordTotalCount();
                    if (count > 0) {
//                            throw new AccountingException("Selected record(s) is currently used for Delivery Order. So it cannot be deleted.");
                        nonDeletedCustomerArray.add(accountid);
                        continue;
                    }
                    // Check for Delivery Order /Salesorder /Sales Return /Sales Invoice/Receipt  Entry
                    boolean isused = accCusVenMapDAOObj.isCustomerUsedInTransactions(customerid, companyid); //ERP-19783
                    if (isused) {
//                            throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");
                        nonDeletedCustomerArray.add(accountid);
                        continue;
                    }
                    result = accCompanyPreferencesObj.getCustomerFromPreferences(customerid, companyid);
                    count = result.getRecordTotalCount();
                    if (count > 0) {
//                            throw new AccountingException("Selected record(s) is currently used for Company preferences. So it cannot be deleted.");
                        nonDeletedCustomerArray.add(accountid);
                        continue;
                    }
                    List childList = new ArrayList(customer.getChildren());
                    if (childList.size() > 0) {
//                            throw new AccountingException("Selected customer(s) is having child customer(s). So it cannot be deleted.");
                        nonDeletedCustomerArray.add(accountid);
                        continue;
                    }
                    try {
                        // for delete discount rule created
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("customerID", customerid);
                        params.put("companyID", companyid);
                        accProductObj.deleteProductBrandDiscountDetails(params);

//                        Delete Account
                        result = accCustomerDAOobj.deletecustomervendormapping(customerid);
                        result = accCustomerDAOobj.deleteCustomerCategoryMappingDtails(customerid);
                        result = accCustomerDAOobj.deleteSalesPersonMappingDtails(customerid);
                        result = accCustomerDAOobj.deleteCustomer(customerid, companyid);
                        result = accAccountDAOobj.deleteAccount(customerid, companyid);
                    } catch (ServiceException ex) {
                        try {
                            result = accAccountDAOobj.deleteAccount(customerid, true);
                        } catch (ServiceException e) {
//                                throw new AccountingException("Selected record(s) is currently used in the transaction(s).");
                            nonDeletedCustomerArray.add(accountid);
                            continue;
                        }
                    }
                    auditID = AuditAction.CUSTOMER_DELETED;
                    if (!propagateTOChildCompaniesFalg) {
                        auditTrailObj.insertAuditLog(auditID, " User " + resObj.optString(Constants.creatorUserName, null) + " " + messageSource.getMessage("acc.rem.hasDeletedaCustomer", null, Locale.forLanguageTag(resObj.getString(Constants.language))) + " " + customerName + " (" + cusomerCode + ") - from CRM.", auditTrailMap, accountid);
                    }
                    deletedCustomerArray.add(accountid);
                }
            }
            returnMap.put("deleted", new JSONArray(deletedCustomerArray));
            returnMap.put("nondeleted", new JSONArray(nonDeletedCustomerArray));
            returnMap.put("companyid", companyid);

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("deleteAccount : " + ex.getMessage(), ex);
        }
        return returnMap;
    }
    

@Override  
    /*
     * Method for saving the receiving details for UOB bank for customer
     */
    public UOBReceivingDetails saveUOBReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        UOBReceivingDetails receivingBankDetails = null;
        try {
            HashMap<String, Object> requestMap = getUOBReceivingBankDetailsRequestParamsMap(request);
            
            KwlReturnObject returnObject = accCustomerDAOobj.saveUOBReceivingBankDetails(requestMap);

            if (!returnObject.getEntityList().isEmpty()) {
                receivingBankDetails = (UOBReceivingDetails) returnObject.getEntityList().get(0);
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accCustomerControllerCMNService.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("saveUOBReceivingBankDetails : " + ex.getMessage(), ex);
        }

        return receivingBankDetails;
    }

@Override  
    /*
     *  Method for getting the receiving details for UOB bank for customer
     */
    public HashMap<String, Object> getUOBReceivingBankDetailsRequestParamsMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();

        requestParams.put("receivingBankDetailId", request.getParameter("receivingBankDetailId"));
        
        requestParams.put("customerBankAccountType", request.getParameter("customerBankAccountType"));
        
        requestParams.put("UOBReceivingBankDetailId", request.getParameter("UOBReceivingBankDetailId"));

        requestParams.put("UOBBICCode", request.getParameter("UOBBICCode"));

        requestParams.put("UOBReceivingBankAccNumber", request.getParameter("UOBReceivingBankAccNumber"));
        
        requestParams.put("UOBReceivingAccName", request.getParameter("UOBReceivingAccName"));

        requestParams.put("UOBEndToEndID", request.getParameter("UOBEndToEndID"));

        requestParams.put("UOBMandateId", request.getParameter("UOBMandateId"));

        requestParams.put("UOBPurposeCode", request.getParameter("UOBPurposeCode"));
        
        requestParams.put("UOBCustomerReference", request.getParameter("UOBCustomerReference"));
        
        requestParams.put("UOBUltimatePayerBeneficiaryName", request.getParameter("UOBUltimatePayerBeneficiaryName"));
        
        requestParams.put("UOBCurrency", request.getParameter("UOBCurrency"));

        requestParams.put("customer", request.getParameter("customer"));

        requestParams.put("isForForm", request.getParameter("isForForm"));
        
        requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
        
        requestParams.put(Constants.isEdit, request.getParameter(Constants.isEdit));
        
        requestParams.put("bankNameForUOB", request.getParameter("bankNameForUOB"));
        
        requestParams.put("UOBBankCode", request.getParameter("UOBBankCode"));
        
        requestParams.put("UOBBranchCode", request.getParameter("UOBBranchCode"));
        
        return requestParams;
    }
  
@Override  
    /*
     *  Method for getting the receiving details for UOB bank for customer    
     */
    public JSONArray getUOBReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONArray returnArray = new JSONArray();
        try {
            
            HashMap<String, Object> requestParams = getUOBReceivingBankDetailsRequestParamsMap(request);
            boolean isForForm = requestParams.containsKey("isForForm")?Boolean.parseBoolean((String)requestParams.get("isForForm")):false;
            KwlReturnObject returnObject = accCustomerDAOobj.getUOBReceivingBankDetails(requestParams);

            returnArray = getUOBReceivingBankDetails(returnObject.getEntityList(),isForForm);
            
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("getUOBReceivingBankDetails : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("getUOBReceivingBankDetails : " + ex.getMessage(), ex);
        }
        return returnArray;
    }

@Override  
    /*
     *  Method for getting the receiving details for UOB bank for customer
     */
    public JSONArray getUOBReceivingBankDetails(List UOBReceivingBankDetailsList,boolean isForForm) throws JSONException, ServiceException {
        JSONArray returnArray = new JSONArray();
        if (!UOBReceivingBankDetailsList.isEmpty()) {
            Iterator it = UOBReceivingBankDetailsList.iterator();
            while (it.hasNext()) {
                UOBReceivingDetails receivingBankDetails = (UOBReceivingDetails) it.next();

                JSONObject jobj = new JSONObject();

                jobj.put("UOBReceivingBankDetailId", receivingBankDetails.getId());
                jobj.put("customerBankAccountType", receivingBankDetails.getCustomerBankAccountType().getID());
                jobj.put("customerBankAccountTypeValue", receivingBankDetails.getCustomerBankAccountType().getValue());
                jobj.put("UOBBICCode", receivingBankDetails.getReceivingBICCode());
                jobj.put("UOBReceivingBankAccNumber", receivingBankDetails.getReceivingBankAccountNumber());
                jobj.put("UOBReceivingAccName", receivingBankDetails.getReceivingAccountName());
                jobj.put("UOBEndToEndID", receivingBankDetails.getEndToEndId());
                jobj.put("UOBMandateId", receivingBankDetails.getMandateId());
                jobj.put("UOBPurposeCode", receivingBankDetails.getPurposeCode());
                jobj.put("UOBCustomerReference", receivingBankDetails.getCustomerReference());
                jobj.put("UOBUltimatePayerBeneficiaryName", receivingBankDetails.getUltimatePayerOrBeneficiaryName());
                jobj.put("UOBCurrency", receivingBankDetails.getCurrencyCode());
                jobj.put("customer", receivingBankDetails.getCustomer().getID());
                jobj.put("bankType", Constants.UOB_Bank);
                jobj.put("UOBBankName", receivingBankDetails.getBankName() != null ? receivingBankDetails.getBankName().getID() : "");
                jobj.put("UOBBankNameValue", receivingBankDetails.getBankName() != null ? receivingBankDetails.getBankName().getValue() : "");
                jobj.put("UOBBankCode", receivingBankDetails.getReceivingBankCode() != null ? receivingBankDetails.getReceivingBankCode() : "");
                jobj.put("UOBBranchcode", receivingBankDetails.getReceivingBranchCode() != null ? receivingBankDetails.getReceivingBranchCode() : "");
                jobj.put("activated", receivingBankDetails.isActivated());
                
                returnArray.put(jobj);
            }
        }

        return returnArray;
    }
   
@Override  
    /*
     *  Method for deleting the receiving details for UOB bank for customer
     */
    public void deleteUOBReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        try {
            HashMap<String, Object> deleteMap = getUOBReceivingBankDetailsRequestParamsMap(request);
            boolean isIBGDetailsUsedInTransaction = accCustomerDAOobj.isIBGDetailsUsedInTransaction(deleteMap);
            if (isIBGDetailsUsedInTransaction) {
                throw new AccountingException("Selected IBG-Receiving Bank Details is used in transaction(s) so it cannot be delete");
            }
            KwlReturnObject returnObject = accCustomerDAOobj.deleteExistingReceivingData(deleteMap);
            
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("Selected IBG-Receiving Bank Details is used in transaction(s) so it cannot be delete", ex);
        }
    }
  
@Override  
    /*
     *  Method for getting the GIRO generation history
     */
    public JSONArray getGiroFileGenerationHistory(HashMap map) throws SessionExpiredException, ServiceException {
        JSONArray returnArray = new JSONArray();
        try {
            
            
            KwlReturnObject returnObject = accCustomerDAOobj.getGiroFileGenerationHistory(map);
            DateFormat df= (DateFormat) map.get(Constants.df);
            returnArray = getGiroFileGenerationHistoryJson(returnObject.getEntityList(),df);
            
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("getGiroFileGenerationHistory : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("getGiroFileGenerationHistory : " + ex.getMessage(), ex);
        }
        return returnArray;
    }
  
@Override  
    /*
     *  Method for getting the GIRO generation history JSON
     */
    public JSONArray getGiroFileGenerationHistoryJson(List<GiroFileGenerationHistory> historyList,DateFormat df) throws JSONException, ServiceException {
        JSONArray returnArray = new JSONArray();
        if (!historyList.isEmpty()) {
            Iterator it = historyList.iterator();
            for(GiroFileGenerationHistory history : historyList) {
                JSONObject jobj = new JSONObject();
                jobj.put("id", history.getID());
                jobj.put("generationdate", history.getGenerationDate() != null ? df.format(history.getGenerationDate()) : "");
                
                String filename = history.getFileName();
                if (!StringUtil.isNullOrEmpty(filename)) {
                    int indexForSubString = filename.indexOf("_", 0);
                    filename = indexForSubString == -1 ? filename : filename.substring(0, indexForSubString);
                    jobj.put("filename", filename);
                }
                returnArray.put(jobj);
            }
        }

        return returnArray;
    }
    
@Override
    public JSONObject saveCustomer(JSONObject paramJObj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isDuplicateNoExe = false;
        boolean isTaxDeactivated = false;
        String sequenceformat = paramJObj.optString(Constants.sequenceformat, null);
        String sequenceformatVen = paramJObj.optString("sequenceformatvencus");
        String customerID = null, msg = "";
        String customerId = paramJObj.optString("accid");
        String customerNumber = paramJObj.optString("acccode");
        String vendorNumber = paramJObj.optString("custorvenacccode").trim();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Customer_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        int customervendor = 0;
        TransactionStatus status = null;
        String companyid = "";
        boolean isAlreadyVendorMappedCustomer=false;
        Map<String, String> deleteparam = null;
        try {
            companyid = paramJObj.optString(Constants.companyKey);
            if(!StringUtil.isNullOrEmpty(customerId)){                
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("ID",customerId);
            paramMap.put("company.companyID",  companyid);
            Object prefObject = kwlCommonTablesDAOObj.getRequestedObjectFields(Customer.class, new String[]{"mapcustomervendor"}, paramMap);
            isAlreadyVendorMappedCustomer = (boolean)prefObject;
         }
            boolean mapcustomervendorFlag = paramJObj.optString("mapcustomervendor",null) != null;
            boolean isEdit = StringUtil.isNullOrEmpty(paramJObj.optString("isEdit", null)) ? false : Boolean.parseBoolean(paramJObj.optString("isEdit"));
            KwlReturnObject count = null;
            CustomerVendorMapping customervendorma = accCusVenMapDAOObj.checkCustomerMappingExists(customerId);
            String vendorid = (customervendorma == null) ? "" : customervendorma.getVendoraccountid().getID();

            deleteparam = new HashMap<String, String>();
            deleteparam.put("customerno", customerNumber);
            deleteparam.put("vendorno", vendorNumber);
            deleteparam.put(Constants.companyKey, companyid);
            if (mapcustomervendorFlag) {
                deleteparam.put("isalsovendor", paramJObj.optString("mapcustomervendor", null));
            }

            if (!paramJObj.has(Constants.sequenceformat) || StringUtil.isNullOrEmpty(paramJObj.optString(Constants.sequenceformat, null))) {
                String sequenceformatid = null;
                Map<String, Object> sfrequestParams = new HashMap<String, Object>();
                sfrequestParams.put(Constants.companyKey, paramJObj.get(Constants.companyKey));
                sfrequestParams.put("modulename", "autocustomerid");
                KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
                List<SequenceFormat> ll = seqFormatResult.getEntityList();
                if (ll.get(0) != null && !ll.isEmpty()) {
                    SequenceFormat format = (SequenceFormat) ll.get(0);
                    sequenceformatid = format.getID();
                    paramJObj.put(Constants.sequenceformat, sequenceformatid);
                } else if (!StringUtil.isNullOrEmpty(customerNumber)) {
                    paramJObj.put(Constants.sequenceformat, "NA");
                }
            }//end of sequenceformat

            if (StringUtil.isNullOrEmpty(paramJObj.optString(Constants.sequenceformat, null))) {
                JSONObject response = StringUtil.getErrorResponse("acc.common.erp33", paramJObj, "Sequence Format Details are missing.", messageSource);
                throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
            }

            //Checks duplicate number in edit case
            if (isEdit) {
                if (sequenceformat.equals("NA")) {
                    count = accCustomerDAOobj.checkDuplicateCustomerForEdit(customerNumber, companyid, customerId);
                    if (count.getRecordTotalCount() > 0) {
                        customervendor = Constants.Acc_Customer_ModuleId;
                        isDuplicateNoExe = true;
                        
                        JSONObject response = StringUtil.getErrorResponse("", paramJObj,messageSource.getMessage("acc.customer.customercode", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + customerNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))), messageSource);
                        throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
//                        throw new AccountingException(messageSource.getMessage("acc.customer.customercode", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + customerNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))));
                    }
                }
                //Checks duplicate number in case customer can be vendor
                if (mapcustomervendorFlag && sequenceformatVen.equals("NA")) {
                    count = accVendorDAOobj.checkDuplicateVendorForEdit(vendorNumber, companyid, vendorid);
                    if (count.getRecordTotalCount() > 0) {
                        customervendor = Constants.Acc_Vendor_ModuleId;
                        isDuplicateNoExe = true;
                        JSONObject response = StringUtil.getErrorResponse("", paramJObj, messageSource.getMessage("acc.vendor.vendorcode", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + vendorNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))), messageSource);
                        throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
//                        throw new AccountingException(messageSource.getMessage("acc.vendor.vendorcode", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + vendorNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))));
                    }
                }
            } else {//Checks duplicate number in add case
                if (sequenceformat.equals("NA")) {
                    count = accCustomerDAOobj.getCustomerCount(customerNumber, companyid);
                    if (count.getRecordTotalCount() > 0) {
                        customervendor = Constants.Acc_Customer_ModuleId;
                        isDuplicateNoExe = true;
                        JSONObject response = StringUtil.getErrorResponse("", paramJObj,messageSource.getMessage("acc.customer.customercode", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + customerNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))), messageSource);
                        throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
//                      
//                        throw new AccountingException(messageSource.getMessage("acc.customer.customercode", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + customerNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))));
                    }
                }
                //Checks duplicate number in case customer can be vendor
                if (mapcustomervendorFlag && sequenceformatVen.equals("NA")) {
                    count = accVendorDAOobj.getVendorCount(vendorNumber, companyid);
                    if (count.getRecordTotalCount() > 0) {
                        customervendor = Constants.Acc_Vendor_ModuleId;
                        isDuplicateNoExe = true;
                        JSONObject response = StringUtil.getErrorResponse("", paramJObj, messageSource.getMessage("acc.vendor.vendorcode", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + vendorNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))), messageSource);
                        throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
//                        throw new AccountingException(messageSource.getMessage("acc.vendor.vendorcode", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + vendorNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))));
                    }
                }
                //Check Deactivate Tax in New Transaction.
                if (!StringUtil.isNullOrEmpty(paramJObj.optString("taxId", null)) && !accAccountDAOobj.isTaxActivated(companyid, paramJObj.optString("taxId"))) {
                    isTaxDeactivated=true;
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.tax.deactivated.tax.saveAlert", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))), "", false);
                }
            }

            //Checks duplicate number for simultaneous transactions
            synchronized (this) {
                status = txnManager.getTransaction(def);
                KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(customerNumber, companyid, Constants.Acc_Customer_ModuleId);
                if (resultInv.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    customervendor = Constants.Acc_Customer_ModuleId;
                    isDuplicateNoExe = true;
                    JSONObject response = StringUtil.getErrorResponse("", paramJObj,messageSource.getMessage("acc.customer.selectedcustomercode", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + customerNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))), messageSource);
                    throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
//                    throw new AccountingException(messageSource.getMessage("acc.customer.selectedcustomercode", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + customerNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))));
                } else {
                    accCommonTablesDAO.insertTransactionInTemp(customerNumber, companyid, Constants.Acc_Customer_ModuleId);
                }

                if (mapcustomervendorFlag && sequenceformatVen.equals("NA")) {
                    resultInv = accCommonTablesDAO.getTransactionInTemp(vendorNumber, companyid, Constants.Acc_Vendor_ModuleId);
                    if (resultInv.getRecordTotalCount() > 0) {
                        customervendor = Constants.Acc_Vendor_ModuleId;
                        isDuplicateNoExe = true;
                        JSONObject response = StringUtil.getErrorResponse("", paramJObj,messageSource.getMessage("acc.vendor.selectedvendorcode", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + vendorNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))), messageSource);
                        throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
//                        throw new AccountingException(messageSource.getMessage("acc.vendor.selectedvendorcode", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + vendorNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))));
                    } else {
                        accCommonTablesDAO.insertTransactionInTemp(vendorNumber, companyid, Constants.Acc_Vendor_ModuleId);
                    }
                }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);

            /*
             * Saving Customer
             */
//            Customer customer = saveCustomer(request);
            Customer customer = null;

            JSONObject customerJobj = saveCustomerJSON(paramJObj);
            if (customerJobj.has(Customer.CUSTOMER_OBJECT) && customerJobj.get(Customer.CUSTOMER_OBJECT) != null) {
                customer = (Customer) customerJobj.get(Customer.CUSTOMER_OBJECT);
                customerID = customer.getID();
                int from = paramJObj.optString("from", null) == null ? -1 : Integer.parseInt(paramJObj.optString("from"));
                int fromVen = Integer.parseInt(paramJObj.optString("fromVenCus","65"));
                String customerNo = "";
                String vendorNo = "";
                Vendor vendor = new Vendor();
                String customerid = customer.getID();
                CustomerVendorMapping customervendormapping = accCusVenMapDAOObj.checkCustomerMappingExists(customerid);
                String mappingid = (customervendormapping == null) ? "" : customervendormapping.getId();
                if (mapcustomervendorFlag) {
                    if (StringUtil.isNullOrEmpty(mappingid)) {
                        paramJObj.put("copyflag", true);

//                    vendor = saveVendor(request);
                        JSONObject vendorObj = saveVendorJSON(paramJObj);
                        if (vendorObj.has(Vendor.VENDOR_OBJECT) && vendorObj.get(Vendor.VENDOR_OBJECT) != null) {
                            vendor = (Vendor) vendorObj.get(Vendor.VENDOR_OBJECT);
                            JSONObject jobjaccount = new JSONObject();
                            jobjaccount.put("customeraccountid", customerid);
                            jobjaccount.put("vendoraccountid", vendor.getID());
                            jobjaccount.put("mappingflag", true);
                            KwlReturnObject result = accCusVenMapDAOObj.saveUpdateCustomerVendorMapping(jobjaccount);
                        }

                    } else {
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();

                        requestParams.put("accid", customervendormapping.getVendoraccountid().getID());
                        requestParams.put("mapcustomervendor", true);
                        requestParams.put("acccode", paramJObj.optString("custorvenacccode"));
                        requestParams.put("accountid", paramJObj.optString("mappingvenaccid"));
                        requestParams.put("deducteetype", !StringUtil.isNullOrEmpty(paramJObj.optString("deducteeTypeId", null)) ? paramJObj.optString("deducteeTypeId") : "");
                        accVendorDAOobj.updateVendor(requestParams);

                        JSONObject jobjaccount = new JSONObject();
                        jobjaccount.put("id", mappingid);
                        jobjaccount.put("mappingflag", true);
                        KwlReturnObject result = accCusVenMapDAOObj.saveUpdateCustomerVendorMapping(jobjaccount);
                    }
                } else {
                    if (!StringUtil.isNullOrEmpty(mappingid)) {
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("accid", customervendormapping.getVendoraccountid().getID());
                        requestParams.put("mapcustomervendor", false);
                        accVendorDAOobj.updateVendor(requestParams); 

                        JSONObject jobjaccount = new JSONObject();
                        jobjaccount.put("id", mappingid);
                        jobjaccount.put("mappingflag", false);
                        KwlReturnObject result = accCusVenMapDAOObj.saveUpdateCustomerVendorMapping(jobjaccount);
                    }
                }
                txnManager.commit(status);
                status = null;
                TransactionStatus AutoNoStatus = null;
                try {
                    synchronized (this) {
                        DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                        def1.setName("AutoNum_Tx");
                        def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                        AutoNoStatus = txnManager.getTransaction(def1);
                        if (!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equalsIgnoreCase("NA") && !isEdit) {
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, from, sequenceformat, false, customer.getCreatedOn());
                            seqNumberMap.put(Constants.DOCUMENTID, customerid);
                            seqNumberMap.put(Constants.companyKey, companyid);
                            seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                            customerNo = accCustomerDAOobj.updateCustomerNumber(seqNumberMap);
                        }
                        if (mapcustomervendorFlag) {
                            if ((!StringUtil.isNullOrEmpty(sequenceformatVen) && !sequenceformatVen.equalsIgnoreCase("NA") && !isEdit)|| (!StringUtil.isNullOrEmpty(sequenceformatVen) && !sequenceformatVen.equalsIgnoreCase("NA") && !isAlreadyVendorMappedCustomer)) {
                                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, fromVen, sequenceformatVen, false, vendor.getCreatedOn());
                                seqNumberMap.put(Constants.DOCUMENTID, vendor.getID());
                                seqNumberMap.put(Constants.companyKey, companyid);
                                seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformatVen);
                                vendorNo = accVendorDAOobj.updateVendorNumber(seqNumberMap);
                            }
                        }
                        txnManager.commit(AutoNoStatus);
                    }
                } catch (Exception ex) {
                    if (AutoNoStatus != null) {
                        txnManager.rollback(AutoNoStatus);
                    }
                    //Delete entries in temporary table
                    deleteEntryInTemp(deleteparam);
                    Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
                }
                //*****************************************Propagate customer In child companies**************************
                String auditID = "";
                boolean propagateTOChildCompaniesFalg = false;
                boolean isPropagatedPersonalDetails = false;
                if (!StringUtil.isNullOrEmpty(paramJObj.optString("ispropagatetochildcompanyflag", null))) {
                    propagateTOChildCompaniesFalg = Boolean.parseBoolean(paramJObj.optString("ispropagatetochildcompanyflag"));
                }

                Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                auditRequestParams.put(Constants.reqHeader, paramJObj.getString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJObj.getString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJObj.getString(Constants.useridKey));

                if (propagateTOChildCompaniesFalg) {
                    try {
                        String parentcompanyid = companyid;
                        Map<String, Object> parentdataMap = new HashMap<>();

                        Map<String, Object> requestMap = (Map<String, Object>) paramJObj.get(Constants.requestMap);
                        Set set = requestMap.entrySet();
                        for (Object obj : set) {
                            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) obj;
                            String[] value = (String[]) entry.getValue();
                            parentdataMap.put(entry.getKey(), value[0]);
                        }

                        String parentcompanyCustomerid = customer.getID();
                        parentdataMap.put("parentCompanyCustomerID", parentcompanyCustomerid);
                        List childCompaniesList = companyDetailsDAOObj.getChildCompanies(parentcompanyid);
                        String childCompanyName = "";

                        if (!isEdit) {
                            auditID = AuditAction.CUSTOMER_ADDED;
                            for (Object childObj : childCompaniesList) {
                                try {
                                    status = txnManager.getTransaction(def);
                                    Object[] childdataOBj = (Object[]) childObj;
                                    String childCompanyID = (String) childdataOBj[0];
                                    childCompanyName = (String) childdataOBj[1];

//                                saveCustomerInChildCompanies(request, isEdit, parentdataMap, parentcompanyid, childCompanyID);
                                    saveCustomerInChildCompanies(paramJObj, isEdit, parentdataMap, parentcompanyid, childCompanyID);

                                    isPropagatedPersonalDetails = true;
                                    txnManager.commit(status);
                                    status = null;

//                                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has propagated(added) customer " + customer.getName() + " ( " + customer.getAcccode() + " ) " + " to child company " + childCompanyName, request, customer.getID());
                                    auditTrailObj.insertAuditLog(auditID, "User " + paramJObj.optString(Constants.userfullname) + " has propagated(added) customer " + customer.getName() + " ( " + customer.getAcccode() + " ) " + " to child company " + childCompanyName, auditRequestParams, customer.getID());
                                } catch (Exception ex) {
                                    txnManager.rollback(status);
//                                auditTrailObj.insertAuditLog(auditID, "Customer " + customer.getName() + " ( " + customer.getAcccode() + " ) " + " could not be propagated(added) to child company " + childCompanyName, request, customer.getID());
                                    auditTrailObj.insertAuditLog(auditID, "Customer " + customer.getName() + " ( " + customer.getAcccode() + " ) " + " could not be propagated(added) to child company " + childCompanyName, auditRequestParams, customer.getID());
                                }
                            }
                        } else {
                            auditID = AuditAction.CUSTOMER_UPDATED;
                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put("propagatedCustomerID", parentcompanyCustomerid);
                            KwlReturnObject result = accCustomerDAOobj.getChildCustomerCount(requestParams);
                            List childCompaniesCustomerList = result.getEntityList();

                            for (Object childObj : childCompaniesCustomerList) {
                                try {
                                    Customer cust = (Customer) childObj;
                                    if (cust != null) {
                                        status = txnManager.getTransaction(def);
                                        String childcompanyscustomerid = cust.getID();
                                        String childCompanyID = cust.getCompany().getCompanyID();
                                        childCompanyName = cust.getCompany().getSubDomain();
                                        parentdataMap.put("childcustomerid", childcompanyscustomerid);

                                        // //UN
//                                    saveCustomerInChildCompanies(request, isEdit, parentdataMap, parentcompanyid, childCompanyID);
                                        saveCustomerInChildCompanies(paramJObj, isEdit, parentdataMap, parentcompanyid, childCompanyID);

                                        isPropagatedPersonalDetails = true;
                                        txnManager.commit(status);
                                        status = null;

//                                    auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has propagated(updated) customer " + customer.getName() + " ( " + customer.getAcccode() + " ) " + " to child company " + childCompanyName, request, customer.getID());
                                        auditTrailObj.insertAuditLog(auditID, "User " + paramJObj.optString(Constants.userfullname) + " has propagated(updated) customer " + customer.getName() + " ( " + customer.getAcccode() + " ) " + " to child company " + childCompanyName, auditRequestParams, customer.getID());
                                    }
                                } catch (Exception ex) {
                                    txnManager.rollback(status);

//                                auditTrailObj.insertAuditLog(auditID, "Customer " + customer.getName() + " ( " + customer.getAcccode() + " ) " + " could not be propagated(udated) to child company " + childCompanyName, request, customer.getID());
                                    auditTrailObj.insertAuditLog(auditID, "Customer " + customer.getName() + " ( " + customer.getAcccode() + " ) " + " could not be propagated(udated) to child company " + childCompanyName, auditRequestParams, customer.getID());
                                }
                            }
                        }
                        jobj.put("isPropagatedPersonalDetails", isPropagatedPersonalDetails);
                    } catch (Exception ex) {
                        if (status != null) {
                            txnManager.rollback(status);
//                        auditTrailObj.insertAuditLog(auditID, "Customer " + customer.getName() + " ( " + customer.getAcccode() + " ) " + " could not be propagated(added) to child company " + customer.getCompany().getSubDomain(), request, customer.getID());
                            auditTrailObj.insertAuditLog(auditID, "Customer " + customer.getName() + " ( " + customer.getAcccode() + " ) " + " could not be propagated(added) to child company " + customer.getCompany().getSubDomain(), auditRequestParams, customer.getID());
                        }
                    }
                }
                //*****************************************Propagate customer In child companies**************************
                issuccess = true;
                msg = messageSource.getMessage("acc.cus.save", null, Locale.forLanguageTag(paramJObj.optString(Constants.language)));   //"Customer information has been saved successfully";
                status = txnManager.getTransaction(def);
                //Delete entries in temporary table
                deleteEntryInTemp(deleteparam);
                txnManager.commit(status);

            } else {
                JSONObject response = StringUtil.getErrorResponse("acc.common.erp35", paramJObj, "Some issue occured while saving Customer.Transaction cannot be completed.", messageSource);
                throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
            }
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            //Delete entries in temporary table
            deleteEntryInTemp(deleteparam);
            msg = "" + ex.getMessage();

            Logger.getLogger(accCustomerControllerCMNServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);

        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            //Delete entries in temporary table
            deleteEntryInTemp(deleteparam);
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            //Delete entries in temporary table
            deleteEntryInTemp(deleteparam);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("perAccID", customerID);
                jobj.put(Constants.customerid, customerID);
                jobj.put("isDuplicateExe", isDuplicateNoExe);
                jobj.put("customervendor", customervendor);
                jobj.put(Constants.isTaxDeactivated, isTaxDeactivated);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public JSONObject saveCustomerJSON(JSONObject paramJObj) throws ServiceException, SessionExpiredException, AccountingException, JSONException {
        JSONObject customerJobj = new JSONObject();
        Customer customer = null;
        String auditMsg = "", auditID = "";
        try {
            boolean mapcustomervendorFlag = !StringUtil.isNullOrEmpty(paramJObj.optString("mapcustomervendor", null)); //true when copy customer checkbox is checked in vendor creation.
            String currencyid = (paramJObj.optString(Constants.currencyKey, null) == null ? paramJObj.optString(Constants.globalCurrencyKey) : paramJObj.optString(Constants.currencyKey));
            String companyid = paramJObj.optString(Constants.companyKey);
            String customerid = paramJObj.optString("accid");
            boolean isEdit = StringUtil.isNullOrEmpty(paramJObj.optString("isEdit", null)) ? false : Boolean.parseBoolean(paramJObj.optString("isEdit"));
            String accountName = paramJObj.optString("accname");
            String customerNumber = paramJObj.optString("acccode");
            String aliasname = paramJObj.optString("aliasname");
            String sequenceformat = paramJObj.optString("sequenceformat");
            String parentName = paramJObj.optString("parentname", null);
            String mappingaccid = paramJObj.optString("mappingcusaccid", null);
            String customfield = paramJObj.optString("customfield", null);
            String employmentStatus = paramJObj.optString("employmentStatus", null);
            String employerName = paramJObj.optString("employerName", null);
            String companyAddress = paramJObj.optString("companyAddress", null);
            String noofActiveCreditLoans = paramJObj.optString("noofActiveCreditLoans", null);
            String occupationAndYears = paramJObj.optString("occupationAndYears", null);
            double monthlyIncome = StringUtil.isNullOrEmpty(paramJObj.optString("monthlyIncome", null)) ? 0 : Double.parseDouble(paramJObj.optString("monthlyIncome"));
            boolean intercompanyflag = paramJObj.optString("intercompanyflag", null) != null;
            boolean isTDSapplicableoncust = !StringUtil.isNullOrEmpty(paramJObj.optString("isTDSapplicableoncust", null));
            String intercompanytype = paramJObj.optString("intercompanytype", null);
            String mappingSalesPersonId = "";
            String mappingReceivedFromId = "";
            boolean iscutomeravailableonlytosalespersons = false;
            String taxId = "";
            boolean autogen = false;
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("taxId", null))) {
                taxId = paramJObj.optString("taxId");
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("iscutomeravailableonlytosalespersons", null))) {
                iscutomeravailableonlytosalespersons = Boolean.parseBoolean(paramJObj.optString("iscutomeravailableonlytosalespersons"));
            }

            if (!StringUtil.isNullOrEmpty(paramJObj.optString("mapsalesperson", null))) {
                mappingSalesPersonId = paramJObj.optString("mapsalesperson");
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("mapreceivedfrom", null))) {
                mappingReceivedFromId = paramJObj.optString("mapreceivedfrom");
            }

            boolean copyflag = paramJObj.optString("copyflag", null) != null; //true when copy customer checkbox is checked in vendor creation.
            boolean debitType = StringUtil.getBoolean(paramJObj.optString("debitType", null));
            debitType = (copyflag) ? !debitType : debitType;
            double openBalance = StringUtil.getDouble(paramJObj.optString("openbalance", "0.0"));
            openBalance = debitType ? openBalance : -openBalance;
            String parentid = (copyflag) ? null : paramJObj.optString("parentid", null);

            //Convert New Date into User's Timezone
            String creationdate = authHandler.getDateFormatter(paramJObj).format(new Date());
//            String creationdate = authHandler.getDateOnlyFormat().format(new Date());
            Date createdate = authHandler.getDateOnlyFormat().parse(creationdate);
            Date creationDate = paramJObj.optString("creationDate", null) == null ? createdate : authHandler.getDateOnlyFormat().parse(paramJObj.optString("creationDate"));//Ajit
            if (creationDate == null) {
                creationDate = new Date();
            }
            String paymentMethod=(StringUtil.isNullOrEmpty(paramJObj.optString("paymentmethod",null))?"": paramJObj.optString("paymentmethod"));                    //ERM-735-add default payment method to customer
            int paymentCriteria = (StringUtil.isNullOrEmpty(paramJObj.optString("paymentCriteria", null)) ? 1 : Integer.parseInt(paramJObj.optString("paymentCriteria")));  // paymentCriteria = '1' - NA; '2'-LIFO; '3'-FIFo
            String pricingBand = (StringUtil.isNullOrEmpty(paramJObj.optString("pricingBand", null)) ? "" : paramJObj.optString("pricingBand"));  // to set customer specific pricingBand
            int deliveryDate = (StringUtil.isNullOrEmpty(paramJObj.optString("deliveryDate", null)) ? 0 : Integer.parseInt(paramJObj.optString("deliveryDate")));
            String deliveryTime = (StringUtil.isNullOrEmpty(paramJObj.optString("deliveryTime", null)) ? "" : paramJObj.optString("deliveryTime"));
            String vehicleNo = (StringUtil.isNullOrEmpty(paramJObj.optString("vehicleNo", null)) ? "" : paramJObj.optString("vehicleNo"));
            String driver = (StringUtil.isNullOrEmpty(paramJObj.optString("driver", null)) ? "" : paramJObj.optString("driver"));
            boolean interStateParty = !StringUtil.isNullOrEmpty(paramJObj.optString("interstateparty", null));
            boolean cFromApplicable = !StringUtil.isNullOrEmpty(paramJObj.optString("cformapplicable", null));
            Date vatRegDate = StringUtil.isNullOrEmpty(paramJObj.optString("vatregdate", null)) ? null : authHandler.getDateOnlyFormat().parse(paramJObj.optString("vatregdate"));
            Date cstRegDate = StringUtil.isNullOrEmpty(paramJObj.optString("cstregdate", null)) ? null : authHandler.getDateOnlyFormat().parse(paramJObj.optString("cstregdate"));
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("accid", customerid);
            if (sequenceformat.equals("NA") || isEdit) {
                requestParams.put("acccode", customerNumber);
            } else {
                requestParams.put("acccode", "");
            }
            requestParams.put("aliasname", aliasname);
            requestParams.put("taxId", taxId);
            requestParams.put("accname", accountName);
            requestParams.put("iscutomeravailableonlytosalespersons", iscutomeravailableonlytosalespersons);
            requestParams.put("mappingSalesPersonId", mappingSalesPersonId);
            requestParams.put("mappingReceivedFromId", mappingReceivedFromId);
            requestParams.put("mappingPaidTo", (!StringUtil.isNullOrEmpty(paramJObj.optString("mappingPaidTo", null)) ? paramJObj.optString("mappingPaidTo") : ""));
            requestParams.put("parentid", parentid);
            requestParams.put("issub", paramJObj.optString("issub", null));
            requestParams.put("debitType", paramJObj.optString("debitType", null));
            requestParams.put("openbalance", paramJObj.optString("openbalance", "0"));
            requestParams.put("title", paramJObj.optString("title", null));
            requestParams.put("uenno", paramJObj.optString("uenno", null));
            requestParams.put("vattinno", !StringUtil.isNullOrEmpty(paramJObj.optString("vattinno", null)) ? paramJObj.optString("vattinno") : "");
            requestParams.put("csttinno", !StringUtil.isNullOrEmpty(paramJObj.optString("csttinno", null)) ? paramJObj.optString("csttinno") : "");
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("companycountry", null)) && paramJObj.optString("companycountry").equals("106")) {
                //NPWP no. is only for Indonesia but in backend it is saved as PAN NO.
                requestParams.put("panno", !StringUtil.isNullOrEmpty(paramJObj.optString("npwp", null)) ? paramJObj.optString("npwp") : "");
            } else {
                requestParams.put("panno", !StringUtil.isNullOrEmpty(paramJObj.optString("panno", null)) ? paramJObj.optString("panno") : "");
            }
            requestParams.put("panstatus", !StringUtil.isNullOrEmpty(paramJObj.optString("panStatusId", null)) ? paramJObj.optString("panStatusId") : "");
            requestParams.put("deducteetype", !StringUtil.isNullOrEmpty(paramJObj.optString("deducteeTypeId", null)) ? paramJObj.optString("deducteeTypeId") : "");
            requestParams.put("residentialstatus", !StringUtil.isNullOrEmpty(paramJObj.optString("residentialstatus", null)) ? Integer.parseInt(paramJObj.optString("residentialstatus")) : 0); //By Default 0-residensial  
            requestParams.put("servicetaxno", !StringUtil.isNullOrEmpty(paramJObj.optString("servicetaxno", null)) ? paramJObj.optString("servicetaxno") : "");
            requestParams.put("tanno", !StringUtil.isNullOrEmpty(paramJObj.optString("tanno", null)) ? paramJObj.optString("tanno") : "");
            requestParams.put("eccno", !StringUtil.isNullOrEmpty(paramJObj.optString("eccno", null)) ? paramJObj.optString("eccno") : "");
            requestParams.put("bankaccountno", paramJObj.optString("bankaccountno", null));
            requestParams.put("termid", paramJObj.optString("termid", null));
            requestParams.put("other", paramJObj.optString("other", null));
            requestParams.put("taxno", paramJObj.optString("taxno", null));
            requestParams.put(Constants.companyKey, companyid);
            requestParams.put("country", paramJObj.optString("country", null));
            requestParams.put("creditLimit", paramJObj.optString("limit", null));
            requestParams.put("overseas", !StringUtil.isNullOrEmpty(paramJObj.optString("overseas", null)));
            requestParams.put("mapcustomervendor", mapcustomervendorFlag);

            requestParams.put("creationDate", creationDate);
            requestParams.put("intercompanyflag", intercompanyflag);
            requestParams.put("intercompanytype", intercompanytype);
            requestParams.put(Constants.currencyKey, currencyid);
            requestParams.put("isPermOrOnetime", paramJObj.optString("isPermOrOnetime", null));
            requestParams.put("companyRegistrationNumber", paramJObj.optString("companyRegistrationNumber", null));
            requestParams.put("gstRegistrationNumber", paramJObj.optString("gstRegistrationNumber", null));
            if (!StringUtil.isNullOrEmpty(paymentMethod)) {
                requestParams.put("paymentmethod", paymentMethod);
            }
            requestParams.put("paymentCriteria", paymentCriteria);
            requestParams.put("pricingBand", pricingBand);
            requestParams.put("deliveryDate", deliveryDate);
            requestParams.put("deliveryTime", deliveryTime);
            requestParams.put("vehicleNo", vehicleNo);
            requestParams.put("driver", driver);
            requestParams.put("employmentStatus", employmentStatus);
            requestParams.put("employerName", employerName);
            requestParams.put("companyAddress", companyAddress);
            requestParams.put("occupationAndYears", occupationAndYears);
            requestParams.put("monthlyIncome", monthlyIncome);
            requestParams.put("noofActiveCreditLoans", noofActiveCreditLoans);
            requestParams.put("interstateparty", interStateParty);
            requestParams.put("cformapplicable", cFromApplicable);
            requestParams.put("isTDSapplicableoncust", isTDSapplicableoncust);
            requestParams.put("vatregdate", vatRegDate);
            /*
             * CST Registration date in Customer Master required for Form 402
             */
            requestParams.put("cstregdate", cstRegDate);
            requestParams.put("dealertype", !StringUtil.isNullOrEmpty(paramJObj.optString("dealertype", null)) ? paramJObj.optString("dealertype") : "");
            requestParams.put("defaultnatureofpurchase", !StringUtil.isNullOrEmpty(paramJObj.optString("defaultnatureofpurchase", null)) ? paramJObj.optString("defaultnatureofpurchase") : "");
            requestParams.put("importereccno", !StringUtil.isNullOrEmpty(paramJObj.optString("importereccno", null)) ? paramJObj.optString("importereccno") : "");
            requestParams.put("iecno", !StringUtil.isNullOrEmpty(paramJObj.optString("iecno", null)) ? paramJObj.optString("iecno") : "");
            requestParams.put("range", !StringUtil.isNullOrEmpty(paramJObj.optString("range", null)) ? paramJObj.optString("range") : "");
            requestParams.put("division", !StringUtil.isNullOrEmpty(paramJObj.optString("division", null)) ? paramJObj.optString("division") : "");
            requestParams.put("commissionerate", !StringUtil.isNullOrEmpty(paramJObj.optString("commissionerate", null)) ? paramJObj.optString("commissionerate") : "");
            /*
             * For India Compliace - GST related fields - START
             */
            DateFormat df = authHandler.getDateOnlyFormat();
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("sezfromdate", null))) {
                requestParams.put("sezfromdate", df.parse(paramJObj.optString("sezfromdate")));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("seztodate", null))) {
                requestParams.put("seztodate", df.parse(paramJObj.optString("seztodate")));
            }            
            requestParams.put("gstin", !StringUtil.isNullOrEmpty(paramJObj.optString("gstin", null)) ? paramJObj.optString("gstin") : "");
            requestParams.put("GSTINRegistrationTypeId", paramJObj.optString("GSTINRegistrationTypeId", null));
            requestParams.put("CustomerVendorTypeId", paramJObj.optString("CustomerVendorTypeId", null));
            /*
             * For India Compliace - GST related fields - END
             */
            if (!StringUtil.isNullOrEmpty(sequenceformat) && sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Customer_ModuleId, customerNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + " <b>" + customerNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))));
                    }
                }
            }
            autogen = !sequenceformat.equalsIgnoreCase("NA") ? true : false;
            requestParams.put("autogenerated", autogen);

            Account account;
            KwlReturnObject result = null;
            boolean issavegstdetails=paramJObj.optBoolean("isgstdetailsupdated",false);
            if (StringUtil.isNullOrEmpty(customerid)) {
                requestParams.put("accountid", mappingaccid);
                result = accCustomerDAOobj.addCustomer(requestParams);

                customer = (Customer) result.getEntityList().get(0);

                KwlReturnObject prefRes = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                ExtraCompanyPreferences companyPreferences = (ExtraCompanyPreferences) prefRes.getEntityList().get(0);
                String warehouseId = companyPreferences.getDefaultWarehouse();
                if (!StringUtil.isNullOrEmpty(warehouseId)) {
                    accCustomerDAOobj.addCustomerWarehouseMapping(warehouseId, customer.getID(), false, null, true);
                }
                issavegstdetails=true;
                auditMsg = " added new customer ";
                auditID = AuditAction.CUSTOMER_ADDED;
            } else {
                if (isChildorGrandChildForCustomer(customerid, parentid)) {
                    throw new AccountingException("\"" + accountName + "\" is a parent of \"" + parentName + "\" so can't set \"" + parentName + "\" as a parent.");
                }

                requestParams.put("accountid", mappingaccid);
                result = accCustomerDAOobj.updateCustomer(requestParams);
                auditMsg = messageSource.getMessage("acc.rem.updatedCustomer", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))); //"updated Customer";
                auditID = AuditAction.CUSTOMER_UPDATED;
            }
            List ll = result.getEntityList();
            customer = (Customer) ll.get(0);

            /**
             * Save Customer GST history for India.
             */
            if (customer.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id) && issavegstdetails) {
                String gstapplieddate=paramJObj.optString("gstapplieddate", null);
                Date applyDate=df.parse(gstapplieddate);
                requestParams.put("applyDate", applyDate);
                requestParams.put("customerid", customer.getID());
                List histList = accCustomerDAOobj.getGstCustomerHistory(requestParams);
                if (!histList.isEmpty() && histList.get(0) != null) {
                    /**
                     * If history present for input date then need to update it.
                     */
                    requestParams.put("gstcustomerhistoryid", (String) histList.get(0));
                }
                /**
                 * Save Customer GST history Audit Trail entry
                 */
                paramJObj.put(Constants.customerid, customer.getID());
                paramJObj.put(Constants.customerName, customer.getName());
                saveCustomerGSTHistoryAuditTrail(paramJObj);
                
                accCustomerDAOobj.saveGstCustomerHistory(requestParams);
            }
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                if (paramJObj.optBoolean(Constants.isdefaultHeaderMap)==true) {// called for rest service for client
                    jcustomarray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_Customer_ModuleId, companyid, true);
                }
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_Customer_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_CustomerId);
                customrequestParams.put("modulerecid", customer.getID());
                customrequestParams.put(Constants.moduleid, Constants.Acc_Customer_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_Customer_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    requestParams.put("acccustomercustomdataref", customer.getID());
                    requestParams.put("accid", customer.getID());
                    KwlReturnObject accresult = accCustomerDAOobj.updateCustomer(requestParams);;
                }
            }

            String customerID = customer.getID();
            String[] customerCategory = null;
            if (paramJObj.optString("category", null) != null) {
                customerCategory = paramJObj.optString("category").split(",");
            }
            if (!StringUtil.isNullOrEmpty(customerID)) {
                accCustomerDAOobj.deleteCustomerCategoryMappingDtails(customerID);
            }
            if (customerCategory != null) {
                for (int j = 0; j < customerCategory.length; j++) {
                    if (!StringUtil.isNullOrEmpty(customerID) && !StringUtil.isNullOrEmpty(customerCategory[j])) {
                        accCustomerDAOobj.saveCustomerCategoryMapping(customerID, customerCategory[j]);
                    }
                }
            }
            String[] multiSalesPerson = null;// For storing masteritems selected
            if (paramJObj.optString("mapmultisalesperson", null) != null) {
                multiSalesPerson = paramJObj.optString("mapmultisalesperson").split(",");
            }
            if (!StringUtil.isNullOrEmpty(customerID)) {
                accCustomerDAOobj.deleteSalesPersonMappingDtails(customerID); //first delete mapping for selected customer
            }
            if (multiSalesPerson != null) {
                for (int j = 0; j < multiSalesPerson.length; j++) {
                    if (!StringUtil.isNullOrEmpty(customerID) && !StringUtil.isNullOrEmpty(multiSalesPerson[j])) {
                        accCustomerDAOobj.saveSalesPersonMapping(customerID, multiSalesPerson[j]); //save masteritems in salespersonmaping table. 
                    }
                }
            }
            //saving products in customerproduct mapping-Neeraj D
            if (paramJObj.optString("productmapping", null) != null) {
                if (!StringUtil.isNullOrEmpty(customerID)) {
                    accVendorCustomerProductDAOobj.deleteCustomerProductMapped(customerID);
                }
                String[] productMapping = paramJObj.optString("productmapping").split(",");
                JSONArray jArray = null;
                JSONObject job = null;

                if (!StringUtil.isNullOrEmpty(paramJObj.optString("customJSONString", null))) {
                    jArray = new JSONArray(paramJObj.optString("customJSONString", "[{}]"));
                }

                if (productMapping != null) {
                    for (int j = 0; j < productMapping.length; j++) {
                        String jsonString = "";
                        if (jArray != null && jArray.length() > 0) {
                            for (int cnt = 0; cnt < jArray.length(); cnt++) {
                                job = jArray.getJSONObject(cnt);
                                if (job.has(productMapping[j])) { //Get field JSON of product 
                                    jsonString = job.get(productMapping[j]).toString();
                                    break;
                                }
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(customerID) && !StringUtil.isNullOrEmpty(productMapping[j])) {
                            accVendorCustomerProductDAOobj.saveCustomerProductMapping(customerID, productMapping[j], jsonString);
                        }
                    }
                }
            }

            Map<String, Object> auditRequestParams = new HashMap<String, Object>();
            auditRequestParams.put(Constants.reqHeader, paramJObj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJObj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJObj.getString(Constants.useridKey));

//            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " " + customer.getName() + " ( " + customerNumber + " ) ", request, customer.getID());
            auditTrailObj.insertAuditLog(auditID, "User " + paramJObj.optString(Constants.userfullname) + " has " + auditMsg + " " + customer.getName() + " ( " + customerNumber + " ) ", auditRequestParams, customer.getID());

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveCustomer : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveCustomer : " + ex.getMessage(), ex);
        } finally {
            customerJobj.put(Customer.CUSTOMER_OBJECT, customer);
        }
        return customerJobj;
    }

    public JSONObject saveVendorJSON(JSONObject paramJobj) throws ServiceException, SessionExpiredException, AccountingException, JSONException {
        JSONObject vendorObj = new JSONObject();
        Vendor vendor = null;
        String auditMsg = "", auditID = "";
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            boolean mapcustomervendorFlag = !StringUtil.isNullOrEmpty(paramJobj.optString("mapcustomervendor", null));//true when copy vendor checkbox is checked in customer creation.

            String accountName = paramJobj.optString("accname", null);
            String aliasname = paramJobj.optString("aliasname", null);
            String vendorNumber = paramJobj.optString("custorvenacccode").trim();
            String sequenceformat = paramJobj.optString("sequenceformatvencus", null);
            String parentName = paramJobj.optString("parentname", null);
            String taxIDNumber = paramJobj.optString("taxidnumber", null);
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit", null)) ? false : Boolean.parseBoolean(paramJobj.optString("isEdit"));
            String contactperson = (paramJobj.optString("contactperson", null) == null) ? "" : paramJobj.optString("contactperson");
            String currencyid = (paramJobj.optString(Constants.currencyKey) == null ? paramJobj.optString(Constants.globalCurrencyKey) : paramJobj.optString(Constants.currencyKey));

            boolean issub = paramJobj.optString("issub", null) != null;
            boolean copyflag = paramJobj.optString("copyflag", null) != null;//true when copy vendor checkbox is checked in customer creation.
            String vendorid = copyflag ? "" : paramJobj.optString("accid", null);
            boolean debitType = StringUtil.getBoolean(paramJobj.optString("debitType", null));
            debitType = (copyflag) ? !debitType : debitType;
            boolean taxEligible = StringUtil.getBoolean(paramJobj.optString("taxeligible", null));
            double openBalance = StringUtil.getDouble(paramJobj.optString("openbalance", null));
            String mappingaccid = paramJobj.optString("mappingvenaccid", null);
            openBalance = debitType ? openBalance : -openBalance;
            String parentid = (copyflag) ? null : paramJobj.optString("parentid", null);
            if (!issub) {
                KwlReturnObject accResult = accAccountDAOobj.getSundryAccount(companyid, true);
                if (accResult.getEntityList().size() > 0 && accResult.getEntityList().get(0) != null) {
                    parentid = (String) accResult.getEntityList().get(0);
                } else {
                    parentid = null;
                }
            }

            Date creationDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("creationDate"));
            if (creationDate == null) {
                creationDate = new Date();
            }
            boolean considerExemptLimit = StringUtil.isNullOrEmpty(paramJobj.optString("considerExemptLimit", null)) ? false : Boolean.parseBoolean(paramJobj.optString("considerExemptLimit"));
            boolean interStateParty = !StringUtil.isNullOrEmpty(paramJobj.optString("interstateparty", null));
            boolean cFromApplicable = !StringUtil.isNullOrEmpty(paramJobj.optString("cformapplicable", null));
            Date vatRegDate = StringUtil.isNullOrEmpty(paramJobj.optString("vatregdate", null)) ? null : authHandler.getDateOnlyFormat().parse(paramJobj.optString("vatregdate"));
            Date cstRegDate = StringUtil.isNullOrEmpty(paramJobj.optString("cstregdate", null)) ? null : authHandler.getDateOnlyFormat().parse(paramJobj.optString("cstregdate"));
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("accid", vendorid);
            if (sequenceformat.equals("NA") || isEdit) {
                requestParams.put("acccode", vendorNumber);
            } else {
                requestParams.put("acccode", "");
            }
            requestParams.put("accname", accountName);
            requestParams.put("creationDate", creationDate);
            requestParams.put("aliasname", aliasname);
            requestParams.put("parentid", parentid);
            requestParams.put("residentialstatus", !StringUtil.isNullOrEmpty(paramJobj.optString("residentialstatus", null)) ? Integer.parseInt(paramJobj.optString("residentialstatus", "0")) : 0); //By Default 0-residensial  
            requestParams.put("issub", paramJobj.optString("issub", null));
            requestParams.put("debitType", paramJobj.optString("debitType", null));
            requestParams.put("openbalance", paramJobj.optString("openbalance", null));
            requestParams.put("title", paramJobj.optString("title", null));
            requestParams.put("address", paramJobj.optString("address", null));
            requestParams.put("baddress2", paramJobj.optString("baddress2", null));
            requestParams.put("baddress3", paramJobj.optString("baddress3", null));
            requestParams.put("bankaccountno", paramJobj.optString("bankaccountno", null));
            requestParams.put("email", paramJobj.optString("email", null));
            requestParams.put("contactno", paramJobj.optString("contactno", null));
            requestParams.put("contactno2", paramJobj.optString("contactno2", null));
            requestParams.put("fax", paramJobj.optString("fax", null));
            requestParams.put("shippingaddress", paramJobj.optString("shippingaddress", null));
            requestParams.put("termid", paramJobj.optString("termid", null));
            requestParams.put("other", paramJobj.optString("other", null));
            requestParams.put("taxidmailon", paramJobj.optString("taxidmailon", null));
            requestParams.put("companyid", companyid);
            requestParams.put("taxeligible", taxEligible);
            requestParams.put("taxidnumber", taxIDNumber);
            requestParams.put("debitLimit", paramJobj.optString("limit", null));
            requestParams.put("contactperson", contactperson);
            requestParams.put("mapcustomervendor", mapcustomervendorFlag);
            requestParams.put("createdInCustomer", true); //this will be always true, because this vendor is created in customer   
            requestParams.put("deducteetype", !StringUtil.isNullOrEmpty(paramJobj.optString("deducteeTypeId", null)) ? paramJobj.optString("deducteeTypeId") : "");
            requestParams.put("servicetaxno", !StringUtil.isNullOrEmpty(paramJobj.optString("servicetaxno", null)) ? paramJobj.optString("servicetaxno") : "");
            requestParams.put("vattinno", !StringUtil.isNullOrEmpty(paramJobj.optString("vattinno", null)) ? paramJobj.optString("vattinno") : "");
            requestParams.put("csttinno", !StringUtil.isNullOrEmpty(paramJobj.optString("csttinno", null)) ? paramJobj.optString("csttinno") : "");
            requestParams.put("eccno", !StringUtil.isNullOrEmpty(paramJobj.optString("eccno", null)) ? paramJobj.optString("eccno") : "");
            requestParams.put("interstateparty", interStateParty);
            requestParams.put("cformapplicable", cFromApplicable);
            requestParams.put("vatregdate", vatRegDate);
            requestParams.put("considerExemptLimit", considerExemptLimit);
            /*
             * CST Registration date in Vendor Master required for Form 402
             */
            requestParams.put("cstregdate", cstRegDate);
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("companycountry", null)) && paramJobj.optString("companycountry").equals("106")) {
                //NPWP no. is only for Indonesia but in backend it is saved as PAN NO.
                requestParams.put("panno", !StringUtil.isNullOrEmpty(paramJobj.optString("npwp", null)) ? paramJobj.optString("npwp") : "");
            } else {
                requestParams.put("panno", !StringUtil.isNullOrEmpty(paramJobj.optString("panno", null)) ? paramJobj.optString("panno") : "");
            }
            requestParams.put("panstatus", !StringUtil.isNullOrEmpty(paramJobj.optString("panStatusId", null)) ? paramJobj.optString("panStatusId") : "");
            requestParams.put("deducteetype", !StringUtil.isNullOrEmpty(paramJobj.optString("deducteeTypeId", null)) ? paramJobj.optString("deducteeTypeId") : "");
            requestParams.put("residentialstatus", !StringUtil.isNullOrEmpty(paramJobj.optString("residentialstatus", null)) ? Integer.parseInt(paramJobj.optString("residentialstatus", "0")) : 0); //By Default 0-residensial  
            requestParams.put("dealertype", !StringUtil.isNullOrEmpty(paramJobj.optString("dealertype", null)) ? paramJobj.optString("dealertype") : "");
            requestParams.put("defaultnatureofpurchase", !StringUtil.isNullOrEmpty(paramJobj.optString("defaultnatureofpurchase", null)) ? paramJobj.optString("defaultnatureofpurchase") : "");
            requestParams.put("manufacturerType", !StringUtil.isNullOrEmpty(paramJobj.optString("manufacturerType", null)) ? paramJobj.optString("manufacturerType") : "");
            requestParams.put("importereccno", !StringUtil.isNullOrEmpty(paramJobj.optString("importereccno", null)) ? paramJobj.optString("importereccno") : "");
            requestParams.put("iecno", !StringUtil.isNullOrEmpty(paramJobj.optString("iecno", null)) ? paramJobj.optString("iecno") : "");
            requestParams.put("range", !StringUtil.isNullOrEmpty(paramJobj.optString("range", null)) ? paramJobj.optString("range") : "");
            requestParams.put("division", !StringUtil.isNullOrEmpty(paramJobj.optString("division", null)) ? paramJobj.optString("division") : "");
            requestParams.put("commissionerate", !StringUtil.isNullOrEmpty(paramJobj.optString("commissionerate", null)) ? paramJobj.optString("commissionerate") : "");
            requestParams.put("natureOfPayment", !StringUtil.isNullOrEmpty(paramJobj.optString("natureofpayment", null)) ? paramJobj.optString("natureofpayment") : "");
            requestParams.put("companyRegistrationNumber", paramJobj.optString("companyRegistrationNumber", null));
            requestParams.put("gstRegistrationNumber", paramJobj.optString("gstRegistrationNumber", null));
            requestParams.put(Constants.currencyKey, currencyid);
            /*
             * For India Compliace - GST related fields - START
             */
            requestParams.put("gstin", !StringUtil.isNullOrEmpty(paramJobj.optString("gstin", null)) ? paramJobj.optString("gstin") : "");
            /*
             * For India Compliace - GST related fields - END
             */

            if (!StringUtil.isNullOrEmpty(sequenceformat) && sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Vendor_ModuleId, vendorNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " <b>" + vendorNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                    }
                }
            }
            requestParams.put("autogenerated", !sequenceformat.equalsIgnoreCase("NA") ? true : false);

            KwlReturnObject result;
            if (StringUtil.isNullOrEmpty(vendorid)) {
                requestParams.put("accountid", mappingaccid);
                result = accVendorDAOobj.addVendor(requestParams);
                auditMsg = " added new vendor ";
                auditID = AuditAction.VENDOR_ADDED;
            } else {
                if (isChildorGrandChild(vendorid, parentid)) {
                    throw new AccountingException("\"" + accountName + "\" is a parent of \"" + parentName + "\" so can't set \"" + parentName + "\" as a parent.");
                }
                requestParams.put("accountid", mappingaccid);
                result = accVendorDAOobj.updateVendor(requestParams);
                auditMsg = " updated vendor ";
                auditID = AuditAction.VENDOR_UPDATED;
            }

            List ll = result.getEntityList();
            vendor = (Vendor) ll.get(0);

            String vendorID = vendor.getID();
            String[] vendorCategory = paramJobj.optString("category").split(",");

            if (!StringUtil.isNullOrEmpty(vendorID)) {
                accVendorDAOobj.deleteVendorCategoryMappingDtails(vendorID);
            }

            for (int j = 0; j < vendorCategory.length; j++) {
                if (!StringUtil.isNullOrEmpty(vendorID) && !StringUtil.isNullOrEmpty(vendorCategory[j])) {
                    accVendorDAOobj.saveVendorCategoryMapping(vendorID, vendorCategory[j]);
                }
            }
            Map<String, Object> auditRequestParams = new HashMap<String, Object>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));

//            auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + auditMsg + vendor.getName(), request, vendor.getID());
            auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + auditMsg + vendor.getName(), auditRequestParams, vendor.getID());

        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveVendor : " + ex.getMessage(), ex);
        } finally {
            vendorObj.put(Vendor.VENDOR_OBJECT, vendor);
        }
        return vendorObj;
    }
    
    //Function used to delete entries in temporary table
    private void deleteEntryInTemp(Map deleteparam) {
        try {
            String customerno = deleteparam.get("customerno").toString();
            String vendorno = deleteparam.get("vendorno").toString();
            String companyid = deleteparam.get(Constants.companyKey).toString();
            accCommonTablesDAO.deleteTransactionInTemp(customerno, companyid, Constants.Acc_Customer_ModuleId);
            if (deleteparam.containsKey("isalsovendor") && deleteparam.get("isalsovendor") != null) {
                accCommonTablesDAO.deleteTransactionInTemp(vendorno, companyid, Constants.Acc_Vendor_ModuleId);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accCustomerControllerCMNServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isChildorGrandChildForCustomer(String vendorid, String parentid) throws ServiceException {
        try {
            List Result = accAccountDAOobj.isChildorGrandChildForCustomer(parentid);
            Iterator iterator = Result.iterator();
            if (iterator.hasNext()) {
                Object ResultObj = iterator.next();
                Customer ResultParentac = (Customer) ResultObj;
                ResultParentac = ResultParentac.getParent();
                if (ResultParentac == null) {
                    return false;
                } else {
                    String Resultparent = ResultParentac.getID();
                    if (Resultparent.equals(vendorid)) {
                        return true;
                    } else {
                        return isChildorGrandChildForCustomer(vendorid, Resultparent);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("isChildorGrandChild : " + ex.getMessage(), ex);
        }
        return false;
    }

    private void saveCustomerInChildCompanies(JSONObject paramJobj, boolean isEdit, Map<String, Object> parentDataMap, String parentCompanyid, String childCompanyID) throws DataInvalidateException {
        /*
         * fetchColumn - column whose value is fetched from database dataColumn
         * - column on which we apply condition
         */
        try {
            HashMap<String, Object> FinalDataMap = new HashMap<String, Object>();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            List list = null;
            //********************default fields processing*********************************************
            int subModuleFlag = 0;
            //Replaced the multiple arguments of getModuleColumnConfig() with single HashMap object
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("moduleId", Constants.CUSTOMER_MODULE_UUID);
            params.put("companyid", childCompanyID);
            params.put("isdocumentimport", "F");
            params.put("subModuleFlag", new Integer(subModuleFlag));
            JSONArray defaultColumnConfigJarray = importHandler.getModuleColumnConfig(params);
            for (int i = 0; i < defaultColumnConfigJarray.length(); i++) {

                JSONObject ColumnConfigJObj = defaultColumnConfigJarray.getJSONObject(i);
                String formfieldname = ColumnConfigJObj.getString("formfieldname");
                if (!StringUtil.isNullOrEmpty(formfieldname)) {
                    if (parentDataMap.containsKey(formfieldname)) {
                        String validateType = ColumnConfigJObj.has("validatetype") ? ColumnConfigJObj.getString("validatetype") : "";
                        Object value = parentDataMap.get(formfieldname);
                        if (validateType.equals("ref")) {
                            String data = value.toString();
                            if (!StringUtil.isNullOrEmpty(data)) {
                                try {
                                    String table = ColumnConfigJObj.getString("refModule");
                                    String fetchColumn = ColumnConfigJObj.getString("refDataColumn");
                                    String dataColumn = ColumnConfigJObj.getString("refFetchColumn");
                                    //get id from name .example - select name from account where id=?
                                    requestParams.put(Constants.companyKey, parentCompanyid);
                                    list = importHandler.getRefData(requestParams, table, dataColumn, fetchColumn, "", data);
                                    data = (String) list.get(0);
                                    //get id from name .example - select id from account where name=?
                                    requestParams.put(Constants.companyKey, childCompanyID);
                                    list = importHandler.getRefData(requestParams, table, fetchColumn, dataColumn, "", data);
                                    data = (String) list.get(0);
                                    if (!StringUtil.isNullOrEmpty(data)) {
                                        FinalDataMap.put(formfieldname, data);
                                    }
                                } catch (Exception ex) {
                                    throw new DataInvalidateException("Combo value not found in child company.");
                                }
                            }
                        } else if (validateType.equalsIgnoreCase("date")) {
                            String data = value.toString();
                            if (!StringUtil.isNullOrEmpty(data)) {
                                Date date = paramJobj.optString("creationDate", null) == null ? new Date() : authHandler.getDateOnlyFormat().parse(paramJobj.optString("creationDate"));
                                if (date == null) {
                                    date = new Date();
                                }
                                FinalDataMap.put(formfieldname, date);
                            }
                        } else if (validateType.equalsIgnoreCase("integer")) {
                            String data = value.toString();
                            int numberValue = 0;
                            if (!StringUtil.isNullOrEmpty(data)) {
                                numberValue = StringUtil.isNullOrEmpty(data) ? 0 : Integer.parseInt(data);
                                FinalDataMap.put(formfieldname, numberValue);
                            }
                        } else {
                            String dataVal = value.toString();
                            if (!StringUtil.isNullOrEmpty(dataVal)) {
                                FinalDataMap.put(formfieldname, value);
                            }
                        }

                    }

                }
            }
            //********************default fields processing Ends*********************************************

            //*********************************save singleSelect dropdowns *************************
            KwlReturnObject returnObject = null;
            String masterGroupID = "";
            String data = "";
            String fetchColumn = "name";
            String conditionColumn = "id";

            if (parentDataMap.containsKey("taxId") && !StringUtil.isNullOrEmpty((String) parentDataMap.get("taxId"))) {
                try {
                    returnObject = importHandler.getTaxbyIDorName(parentCompanyid, fetchColumn, conditionColumn, (String) parentDataMap.get("taxId"));
                    data = (String) returnObject.getEntityList().get(0);

                    returnObject = importHandler.getTaxbyIDorName(childCompanyID, conditionColumn, fetchColumn, data);
                    data = (String) returnObject.getEntityList().get(0);
                    FinalDataMap.put("taxId", data);
                } catch (Exception ex) {
                    throw new DataInvalidateException("Combo value not found in child company.");
                }
            }

            fetchColumn = "mst.value";
            conditionColumn = "mst.ID";
            if (parentDataMap.containsKey("title") && !StringUtil.isNullOrEmpty((String) parentDataMap.get("title"))) {
                try {
                    masterGroupID = String.valueOf(6);
                    returnObject = accCustomerDAOobj.getMasterItemByNameorID(parentCompanyid, (String) parentDataMap.get("title"), masterGroupID, fetchColumn, conditionColumn);
                    data = (String) returnObject.getEntityList().get(0);

                    returnObject = accCustomerDAOobj.getMasterItemByNameorID(childCompanyID, data, masterGroupID, conditionColumn, fetchColumn);
                    data = (String) returnObject.getEntityList().get(0);
                    FinalDataMap.put("title", data);
                } catch (Exception ex) {
                    throw new DataInvalidateException("Combo value not found in child company.");
                }
            }

            //*******************save singleSelect dropdowns Ends****************************************
            //****************************Add/Edit child compny's customer************************************
            String parentCompanyCustomerID = parentDataMap.containsKey("parentCompanyCustomerID") ? (String) parentDataMap.get("parentCompanyCustomerID") : "";
            FinalDataMap.put("parentCompanyCustomerID", parentCompanyCustomerID);
            FinalDataMap.put(Constants.companyKey, childCompanyID);
            KwlReturnObject result = null;
            String childCustomerID = "";
            Customer customer = null;
            try {
                if (!isEdit) {
                    result = accCustomerDAOobj.addCustomer(FinalDataMap);
                } else {

                    String childCustomerid = parentDataMap.containsKey("childcustomerid") ? (String) parentDataMap.get("childcustomerid") : "";
                    FinalDataMap.put("accid", childCustomerid);
                    result = accCustomerDAOobj.updateCustomer(FinalDataMap);
                }
                customer = (Customer) result.getEntityList().get(0);
                childCustomerID = customer.getID();
            } catch (Exception ex) {
                throw new DataInvalidateException("Customer could not be saved.");
            }
            //****************************Add/Edit child compny's customer************************************

            //*********************************save multiselect dropdowns*************************
            if (!StringUtil.isNullOrEmpty(childCustomerID)) {
                accCustomerDAOobj.deleteCustomerCategoryMappingDtails(childCustomerID);
            }
            if (parentDataMap.get("category") != null) {
                masterGroupID = "7";
                String category = parentDataMap.get("category").toString();
                String[] customerCategory = category.split(",");
                for (int i = 0; i < customerCategory.length; i++) {
                    String value = customerCategory[i];
                    if (!StringUtil.isNullOrEmpty(value)) {
                        try {
                            returnObject = accCustomerDAOobj.getMasterItemByNameorID(parentCompanyid, value, masterGroupID, fetchColumn, conditionColumn);
                            data = (String) returnObject.getEntityList().get(0);

                            returnObject = accCustomerDAOobj.getMasterItemByNameorID(childCompanyID, data, masterGroupID, conditionColumn, fetchColumn);
                            data = (String) returnObject.getEntityList().get(0);
                            accCustomerDAOobj.saveCustomerCategoryMapping(customer.getID(), data);
                        } catch (Exception ex) {
                            throw new DataInvalidateException("Combo value not found in child company.");
                        }
                    }
                }
            }

            if (!StringUtil.isNullOrEmpty(childCustomerID)) {
                accCustomerDAOobj.deleteSalesPersonMappingDtails(childCustomerID); //first delete mapping for selected customer
            }
            if (parentDataMap.get("mapmultisalesperson") != null) {
                String salesperson = parentDataMap.get("mapmultisalesperson").toString();
                masterGroupID = "15";
                String[] salespersonIDs = salesperson.split(",");
                for (int i = 0; i < salespersonIDs.length; i++) {
                    String value = salespersonIDs[i];
                    if (!StringUtil.isNullOrEmpty(value)) {
                        try {
                            returnObject = accCustomerDAOobj.getMasterItemByNameorID(parentCompanyid, value, masterGroupID, fetchColumn, conditionColumn);
                            data = (String) returnObject.getEntityList().get(0);

                            returnObject = accCustomerDAOobj.getMasterItemByNameorID(childCompanyID, data, masterGroupID, conditionColumn, fetchColumn);
                            data = (String) returnObject.getEntityList().get(0);
                            accCustomerDAOobj.saveSalesPersonMapping(childCustomerID, data);
                        } catch (Exception ex) {
                            throw new DataInvalidateException("Combo value not found in child company.");
                        }
                    }
                }
            }

            if (!StringUtil.isNullOrEmpty(childCustomerID)) {
                accVendorCustomerProductDAOobj.deleteCustomerProductMapped(childCustomerID);
            }
            if (parentDataMap.get("productmapping") != null) {
                fetchColumn = "p.name";
                conditionColumn = "p.ID";
                String productString = parentDataMap.get("productmapping").toString();
                String[] productMapping = productString.split(",");
                if (productMapping != null) {
                    for (int j = 0; j < productMapping.length; j++) {
                        String value = productMapping[j];
                        if (!StringUtil.isNullOrEmpty(value)) {
                            try {
                                returnObject = accVendorCustomerProductDAOobj.getProductByNameorID(parentCompanyid, value, masterGroupID, fetchColumn, conditionColumn);
                                data = (String) returnObject.getEntityList().get(0);

                                returnObject = accVendorCustomerProductDAOobj.getProductByNameorID(childCompanyID, data, masterGroupID, conditionColumn, fetchColumn);
                                data = (String) returnObject.getEntityList().get(0);
                                /**
                                 * Third parameter sent as a blank value as it
                                 * is required only while saving
                                 * Customer/Vendor.
                                 */
                                accVendorCustomerProductDAOobj.saveCustomerProductMapping(childCustomerID, data, "");
                            } catch (Exception ex) {
                                throw new DataInvalidateException("Combo value not found in child company.");
                            }
                        }
                    }
                }
            }
            //*********************************save multiselect dropdowns Ends*************************

            //*******************Save Custom Fields Data****************************
            JSONArray jarray = parentDataMap.containsKey("customfield") ? new JSONArray(parentDataMap.get("customfield").toString()) : new JSONArray();
            Map<String, Object> customColumnConfigMap = importHandler.getCustomModuleColumnConfigForSharingMastersData(Constants.CUSTOMER_MODULE_UUID, childCompanyID, false);

            JSONArray childFinalCustomJarray = new JSONArray();
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject customColumnJobj = jarray.getJSONObject(i);
                String parentFieldValue = customColumnJobj.getString("fieldDataVal");
                String parentFieldName = customColumnJobj.getString("fieldname");
                String parentFieldID = customColumnJobj.getString("fieldid");
                int parentXtype = Integer.parseInt(customColumnJobj.getString("xtype"));

                if (customColumnConfigMap.containsKey(parentFieldName)) {
                    JSONObject childCustomConfig = (JSONObject) customColumnConfigMap.get(parentFieldName);
                    int childXtype = childCustomConfig.getInt("xtype");
                    String childFieldID = childCustomConfig.getString("id");
                    if (parentXtype == childXtype) {
                        JSONObject cjobj = new JSONObject();

                        cjobj.put("fieldid", childCustomConfig.getString("id"));
                        cjobj.put("refcolumn_name", "Col" + childCustomConfig.get("refcolnum"));
                        cjobj.put("fieldname", "Custom_" + childCustomConfig.get("columnName"));
                        cjobj.put("xtype", childCustomConfig.getString("xtype"));

                        cjobj.put("Custom_" + childCustomConfig.get("columnName"), "Col" + childCustomConfig.get("colnum"));

                        if (childXtype == 4 || childXtype == 7 || childXtype == 12) {
                            //combo ,multiselect combo,checklist.
                            try {
                                if (parentFieldValue != null) {
                                    String[] fieldComboDataArr = parentFieldValue.toString().split(",");
                                    String fieldComboDataStr = "";

                                    for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                        String value = fieldComboDataArr[dataArrIndex];
                                        if (!StringUtil.isNullOrEmpty(value)) {

                                            String CustomFetchColumn = "value";
                                            list = importHandler.getCustomComboValue(value, parentFieldID, CustomFetchColumn);

                                            value = list.get(0).toString();
                                            CustomFetchColumn = "id";
                                            list = importHandler.getCustomComboID(value, childFieldID, CustomFetchColumn);
                                            if (list != null && !list.isEmpty()) {
                                                fieldComboDataStr += list.get(0).toString() + ",";
                                            }
                                        }
                                    }

                                    if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                        String comboids = fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1);
                                        cjobj.put("fieldDataVal", comboids);
                                        cjobj.put("Col" + childCustomConfig.get("colnum"), comboids);
                                    } else {
                                        cjobj.put("fieldDataVal", "");
                                        cjobj.put("Col" + childCustomConfig.get("colnum"), "");
                                    }
                                } else {
                                    cjobj.put("fieldDataVal", "");
                                    cjobj.put("Col" + childCustomConfig.get("colnum"), "");
                                }
                            } catch (Exception ex) {
                                throw new DataInvalidateException("Combo value not found in child company.");
                            }
                        } else {
                            cjobj.put("fieldDataVal", parentFieldValue);
                            cjobj.put("Col" + childCustomConfig.get("colnum"), parentFieldValue);
                        }

                        childFinalCustomJarray.put(cjobj);
                    }

                }

            }

            if (childFinalCustomJarray.length() > 0) {
                try {
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", childFinalCustomJarray);
                    customrequestParams.put("modulename", Constants.Acc_Customer_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_CustomerId);
                    customrequestParams.put("modulerecid", customer.getID());
                    customrequestParams.put(Constants.moduleid, Constants.Acc_Customer_ModuleId);
                    customrequestParams.put(Constants.companyKey, childCompanyID);
                    customrequestParams.put("customdataclasspath", Constants.Acc_Customer_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        requestParams.put("acccustomercustomdataref", customer.getID());
                        requestParams.put("accid", customer.getID());
                        KwlReturnObject accresult = accCustomerDAOobj.updateCustomer(requestParams);
                    }
                } catch (Exception ex) {
                    throw new DataInvalidateException("Error ocurred while saving custom fields");
                }
            }
            //**************************Save Custom Fields Data End***************************************
        } catch (Exception ex) {
            throw new DataInvalidateException("Error ocurred while saving Customer");
        }
    }
  
    public boolean isChildorGrandChild(String vendorid, String parentid) throws ServiceException {
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
                    if (Resultparent.equals(vendorid)) {
                        return true;
                    } else {
                        return isChildorGrandChild(vendorid, Resultparent);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("isChildorGrandChild : " + ex.getMessage(), ex);
        }
        return false;
    }
  
    @Override
    public JSONObject saveCustomerAddresses(JSONObject paramJObj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String customerID = null, msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Customer_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveCustomerAddressesJSON(paramJObj);
            issuccess = true;
            msg = messageSource.getMessage("acc.cus.save", null, Locale.forLanguageTag(paramJObj.getString(Constants.language)));   //"Customer information has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("perAccID", customerID);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    /**
     * function to get customer GST fields data based on date filters
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws SessionExpiredException
     * @throws ParseException 
     */
    public JSONObject getCustomerGSTHistory(JSONObject reqParams) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        Map<String, Object> reqMap = new HashMap();
        if (!StringUtil.isNullOrEmpty(reqParams.optString("returnalldata"))) {
            reqMap.put("returnalldata", reqParams.optBoolean("returnalldata"));
        }
        if (!StringUtil.isNullOrEmpty(reqParams.optString("transactiondate"))) {
            reqMap.put("transactiondate", authHandler.getDateOnlyFormat().parse(reqParams.optString("transactiondate")));
        }
        if (!StringUtil.isNullOrEmpty(reqParams.optString("customerid"))) {
            reqMap.put("customerid", reqParams.optString("customerid"));
        }
        if (!StringUtil.isNullOrEmpty(reqParams.optString("isfortransaction"))) {
            reqMap.put("isfortransaction", reqParams.optBoolean("isfortransaction"));
        }
        DateFormat df=null;
        try {
            df=authHandler.getOnlyDateFormat();
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCustomerControllerCMNServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        JSONObject data = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        JSONObject jSONObject = new JSONObject();
            /**
             * If request for  history data.
             */
            List<GstCustomerHistory> customerHistorys = accCustomerDAOobj.getGstCustomerHistory(reqMap);
            for (GstCustomerHistory gstCustomerHistory : customerHistorys) {
                jSONObject = new JSONObject();
                jSONObject.put("id", gstCustomerHistory.getCustomer().getName());
                jSONObject.put("GSTINRegistrationTypeId", gstCustomerHistory.getGSTRegistrationType() != null ? gstCustomerHistory.getGSTRegistrationType().getID() : "");
                jSONObject.put("CustomerVendorTypeId", gstCustomerHistory.getGSTCustomerType()!=null? gstCustomerHistory.getGSTCustomerType().getID():"");
                jSONObject.put("gstin", gstCustomerHistory.getGstin());
                jSONObject.put("applydate", gstCustomerHistory.getApplyDate() != null ? df.format(gstCustomerHistory.getApplyDate()) : "");
                /**
                 * Add data for transaction purpose.
                 */
                jSONObject.put("uniqueCase", accCustomerDAOobj.getUniqueCase(jSONObject.put("type", gstCustomerHistory.getGSTCustomerType()!=null && gstCustomerHistory.getGSTCustomerType().getDefaultMasterItem()!=null?gstCustomerHistory.getGSTCustomerType().getDefaultMasterItem().getID():"")));
                jSONObject.put("GSTINRegTypeDefaultMstrID", gstCustomerHistory.getGSTRegistrationType() != null ? (gstCustomerHistory.getGSTRegistrationType().getDefaultMasterItem() != null ? gstCustomerHistory.getGSTRegistrationType().getDefaultMasterItem().getID() : "") : "");
                jSONObject.put(IndiaComplianceConstants.CustVenTypeDefaultMstrID, gstCustomerHistory.getGSTCustomerType() != null ? (gstCustomerHistory.getGSTCustomerType().getDefaultMasterItem() != null ? gstCustomerHistory.getGSTCustomerType().getDefaultMasterItem().getID() : "") : "");
                jSONArray.put(jSONObject);
                if(reqParams.optBoolean("returncurrentsingledata")){
                    break;
                }
            }
            /**
             * If GST History not present then take GST data from Customer
             */
            boolean isfortransaction = reqParams.optBoolean("isfortransaction", false);
            boolean isGSTHistoryDataPresent = true;
            if (isfortransaction) {
                if (customerHistorys.isEmpty()) {
                    isGSTHistoryDataPresent = false;
                } else if (jSONArray.length() == 1) {
                    /**
                     * If GST History present but empty values GST Registration type, Customer Type and GSTIN if required
                     */
                    JSONObject historyJobj = jSONArray.getJSONObject(0);
                    if (StringUtil.isNullOrEmpty(historyJobj.optString("GSTINRegistrationTypeId", ""))) {
                        isGSTHistoryDataPresent = false;
                    } else if (StringUtil.isNullOrEmpty(historyJobj.optString("CustomerVendorTypeId", ""))) {
                        isGSTHistoryDataPresent = false;
                    } else if (!StringUtil.isNullOrEmpty(historyJobj.optString("GSTINRegTypeDefaultMstrID", "")) && !(historyJobj.optString("GSTINRegTypeDefaultMstrID", "").equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Unregistered))) && StringUtil.isNullOrEmpty(historyJobj.optString("gstin", ""))) {
                        isGSTHistoryDataPresent = false;
                    }
                }
        } else if (reqParams.optBoolean("returncurrentsingledata") && customerHistorys.isEmpty()) {
            jSONObject.put("GSTINRegistrationTypeId", "");
            jSONObject.put("CustomerVendorTypeId", "");
            jSONObject.put("gstin", "");
            jSONObject.put("applydate", "");
            jSONArray.put(jSONObject);
        }
            data.put(Constants.IS_GST_HISTORY_PRESENT, isGSTHistoryDataPresent);

        data.put("count", jSONArray.length());
        return data.put("data", jSONArray);
    }
        /**
     * Function to get Customer's Used history.
     *
     * @param reqMap
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject getCustomerGSTUsedHistory(JSONObject reqParams) throws ServiceException, JSONException, ParseException {        
        DateFormat df = null;
        df = authHandler.getGlobalDateFormat();
        Map<String, Object> reqMap = new HashMap();
        if (!StringUtil.isNullOrEmpty(reqParams.optString("custvenid"))) {
            reqMap.put("customerid", reqParams.optString("custvenid"));
        }
        if (!StringUtil.isNullOrEmpty(reqParams.optString("applydate"))) {
            reqMap.put("applyDate", df.parse(reqParams.optString("applydate")));
        }
        if (!StringUtil.isNullOrEmpty(reqParams.optString("companyid"))) {
            reqMap.put("companyid", reqParams.optString("companyid"));
        }
        JSONObject data = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        List result=accCustomerDAOobj.getGstCustomerUsedHistory(reqMap);     
        if(!result.isEmpty()){
            jSONObject.put("isUsedCustomerVendor",true);
        }else{
            jSONObject.put("isUsedCustomerVendor",false);
        }
        jSONArray.put(jSONObject);
        data.put("count", jSONArray.length());
        return data.put("data", jSONArray);
    }
    /**
     * Save GST details add/ update entry in audit trail
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws SessionExpiredException
     * @throws ParseException 
     */
    public JSONObject saveCustomerGSTHistoryAuditTrail(JSONObject paramJObj) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        JSONObject returnJSONObj = new JSONObject();
        
        Map<String, Object> auditRequestParamsForGSTHistory = new HashMap<String, Object>();
        auditRequestParamsForGSTHistory.put(Constants.reqHeader, paramJObj.getString(Constants.reqHeader));
        auditRequestParamsForGSTHistory.put(Constants.remoteIPAddress, paramJObj.getString(Constants.remoteIPAddress));
        auditRequestParamsForGSTHistory.put(Constants.useridKey, paramJObj.getString(Constants.useridKey));
        String userName = paramJObj.optString(Constants.userfullname);
        String customerID = paramJObj.optString(Constants.customerid, "");
        String customerName = paramJObj.optString(Constants.customerName, "");
        String auditMSGForGSTHistory = "";
        /**
         * Get All changed GST details as new GST details value
         */
        String newgstin = paramJObj.optString("gstin","");
        String newGSTINRegistrationType = paramJObj.optString("GSTINRegistrationTypeId","");
        String newCustomerVendorType= paramJObj.optString("CustomerVendorTypeId", "");
        if(!StringUtil.isNullOrEmpty(newGSTINRegistrationType)){
            Map<String, Object> map = new HashMap<>();
            map.put("ID", newGSTINRegistrationType);
            Object res = kwlCommonTablesDAOObj.getRequestedObjectFields(MasterItem.class, new String[]{"value"}, map);
            newGSTINRegistrationType = res != null ? (String) res : "";
        }
        if(!StringUtil.isNullOrEmpty(newCustomerVendorType)){
            Map<String, Object> map = new HashMap<>();
            map.put("ID", newCustomerVendorType);
            Object res = kwlCommonTablesDAOObj.getRequestedObjectFields(MasterItem.class, new String[]{"value"}, map);
            newCustomerVendorType = res != null ? (String) res : "";
        }
        DateFormat df = authHandler.getDateOnlyFormat();
        String gstapplieddate = paramJObj.optString("gstapplieddate", null);
        Date applyDate = df.parse(gstapplieddate);
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("applyDate", applyDate);
        requestParams.put("customerid", customerID);
        requestParams.put("returnalldata", true);
        
        String oldgstin = "";
        String oldGSTINRegistrationType = "";
        String oldCustomerVendorType = "";
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.MMMMdyyyy);
        String newAppliedDateString = sdf.format(applyDate);
        String oldAppliedDateString = "";
        /**
         * Check if same GST detail present or not if present then
         * audit trail entry as updated GST details
         */
        List<GstCustomerHistory> customerHistory = accCustomerDAOobj.getGstCustomerHistory(requestParams);
        if (customerHistory!=null && !customerHistory.isEmpty()) {
            for (GstCustomerHistory gstCustomerHistory : customerHistory) {
                if (gstCustomerHistory != null) {
                    oldgstin = gstCustomerHistory.getGstin();
                    oldGSTINRegistrationType =gstCustomerHistory.getGSTRegistrationType()!=null ? gstCustomerHistory.getGSTRegistrationType().getValue() : "";
                    oldCustomerVendorType = gstCustomerHistory.getGSTCustomerType()!=null ? gstCustomerHistory.getGSTCustomerType().getValue() : "";
                    oldAppliedDateString = gstCustomerHistory.getApplyDate()!=null ? sdf.format(gstCustomerHistory.getApplyDate()) : "";
                }
            }
            Object[] msgparams = new Object[]{userName,customerName,oldGSTINRegistrationType,oldgstin,oldCustomerVendorType,oldAppliedDateString,
                newGSTINRegistrationType,newgstin, newCustomerVendorType,newAppliedDateString};
            auditMSGForGSTHistory = messageSource.getMessage("acc.save.customer.gstdetails.update.auditTrail", msgparams, Locale.forLanguageTag(paramJObj.optString(Constants.language)));
        }else{
            /**
             * If New GST details then audit trail entry as added GST details
             */
            Object[] msgparams = new Object[]{userName,customerName,newGSTINRegistrationType,newgstin, newCustomerVendorType,newAppliedDateString};
            auditMSGForGSTHistory = messageSource.getMessage("acc.save.customer.gstdetails.add.auditTrail", msgparams, Locale.forLanguageTag(paramJObj.optString(Constants.language)));
        }
        auditTrailObj.insertAuditLog("2221", auditMSGForGSTHistory, auditRequestParamsForGSTHistory, customerID);

        return returnJSONObj;
    }
 @Override   
   public void saveCustomerAddressesJSON(JSONObject paramJObj) throws ServiceException, SessionExpiredException, AccountingException {      
        try{
            String vendorID = "";
            String customerID = "";
            String companyid = paramJObj.optString(Constants.companyKey);
            customerID = paramJObj.optString("accid",null);
            String addressDetails =  paramJObj.optString("addressDetail","[{}]");
            JSONArray jArr = new JSONArray(addressDetails);
            
            String propagationflag = paramJObj.optString("ispropagatetochildcompanyflag",null);
            boolean ispropagatetochildcompanyflag = !StringUtil.isNullOrEmpty(propagationflag) ? Boolean.parseBoolean(propagationflag) : false;
            KwlReturnObject returnObject = null;
            String masterGroupID = "";
            String data = "";
            String fetchColumn = "mst.value";
            String conditionColumn = "mst.ID";
            String parentcompanyid = companyid;
            String parentCompanyCustomerID = customerID;
            

            HashMap<String, Object> mappingParams = new HashMap<String, Object>();
            mappingParams.put("customeraccountid", customerID);
            KwlReturnObject mappingResult = accCusVenMapDAOObj.getCustomerVendorMapping(mappingParams);
            if (mappingResult != null && !mappingResult.getEntityList().isEmpty()) {
                CustomerVendorMapping mapping = (CustomerVendorMapping) mappingResult.getEntityList().get(0);
                if (mapping != null && mapping.getVendoraccountid() != null) {
                    vendorID = mapping.getVendoraccountid().getID();
                    if (!StringUtil.isNullOrEmpty(vendorID)) {
                        KwlReturnObject categoryresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorID);
                        Vendor vendor = (Vendor) categoryresult.getEntityList().get(0);
                        if (vendor.isCreatedInCustomer()) {
                            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                            addrRequestParams.put("vendorid", vendorID);
                            addrRequestParams.put(Constants.companyKey, companyid);
                            KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
                            if (addressResult.getEntityList().isEmpty()) { //if address is not given to vendor which is created with customer. in this case updating vendor address with customer address
                                for (int i = 0; i < jArr.length(); i++) {
                                    HashMap<String, Object> custAddrMap = new HashMap<String, Object>();
                                    JSONObject jobj = jArr.getJSONObject(i);
                                    custAddrMap.put("vendorid", vendorID);
                                    custAddrMap.put("aliasName", jobj.optString("aliasName", ""));
                                    custAddrMap.put("address", jobj.optString("address", ""));
                                    custAddrMap.put("county", jobj.optString("county", ""));
                                    custAddrMap.put("city", jobj.optString("city", ""));
                                    custAddrMap.put("state", jobj.optString("state", ""));
                                    custAddrMap.put("stateCode", jobj.optString("stateCode", ""));
                                    custAddrMap.put("country", jobj.optString("country", ""));
                                    custAddrMap.put("postalCode", jobj.optString("postalCode", ""));
                                    custAddrMap.put("phone", jobj.optString("phone", ""));
                                    custAddrMap.put("mobileNumber", jobj.optString("mobileNumber", ""));
                                    custAddrMap.put("fax", jobj.optString("fax", ""));
                                    String email = jobj.optString("emailID", "").replaceAll("\\s", "");
                                    custAddrMap.put("emailID", email);
                                    custAddrMap.put("recipientName", jobj.optString("recipientName", ""));
                                    custAddrMap.put("contactPerson", jobj.optString("contactPerson", ""));
                                    custAddrMap.put("contactPersonNumber", jobj.optString("contactPersonNumber", ""));
                                    custAddrMap.put("contactPersonDesignation", jobj.optString("contactPersonDesignation", ""));
                                    custAddrMap.put("website", jobj.optString("website", ""));
                                    custAddrMap.put("shippingRoute", jobj.optString("shippingRoute", ""));
                                    custAddrMap.put("isBillingAddress", jobj.getBoolean("isBillingAddress"));
                                    custAddrMap.put("isDefaultAddress", jobj.getBoolean("isDefaultAddress"));
                                    KwlReturnObject custAddrobject = accountingHandlerDAOobj.saveVendorAddressesDetails(custAddrMap, companyid);
                                }
                            }
                        }
                    }
                }

            }

            if (!StringUtil.isNullOrEmpty(customerID)) {
                KwlReturnObject deleteResult = accountingHandlerDAOobj.deleteCustomerAddressDetails(customerID, companyid);
                
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("propagatedCustomerID", parentCompanyCustomerID);
                KwlReturnObject result = accCustomerDAOobj.getChildCustomerCount(requestParams);
                List childCompaniesCustomerList = result.getEntityList();
                try {
                    for (Object childObj : childCompaniesCustomerList) {
                        Customer cust = (Customer) childObj;
                        if (cust != null) {
                            String childCompanyID = cust.getCompany().getCompanyID();
                            String childcompanyscustomerid = cust.getID();
                            KwlReturnObject deleteResult1 = accountingHandlerDAOobj.deleteCustomerAddressDetails(childcompanyscustomerid, childCompanyID);
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            for (int i = 0; i < jArr.length(); i++) {
                HashMap<String, Object> custAddrMap = new HashMap<String, Object>();
                JSONObject jobj = jArr.getJSONObject(i);
                custAddrMap.put("customerid", customerID);
                custAddrMap.put("aliasName", jobj.optString("aliasName", ""));
                custAddrMap.put("address", jobj.optString("address", ""));
                custAddrMap.put("county", jobj.optString("county", ""));
                custAddrMap.put("city", jobj.optString("city", ""));
                custAddrMap.put("state", jobj.optString("state", ""));
                custAddrMap.put("stateCode", jobj.optString("stateCode", ""));
                custAddrMap.put("country", jobj.optString("country", ""));
                custAddrMap.put("postalCode", jobj.optString("postalCode", ""));
                custAddrMap.put("phone", jobj.optString("phone", ""));
                custAddrMap.put("mobileNumber", jobj.optString("mobileNumber", ""));
                custAddrMap.put("fax", jobj.optString("fax", ""));
                String email = jobj.optString("emailID", "").replaceAll("\\s", "");
                custAddrMap.put("emailID", email);
                custAddrMap.put("recipientName", jobj.optString("recipientName", ""));
                custAddrMap.put("contactPerson", jobj.optString("contactPerson", ""));
                custAddrMap.put("contactPersonNumber", jobj.optString("contactPersonNumber", ""));
                custAddrMap.put("contactPersonDesignation", jobj.optString("contactPersonDesignation", ""));
                custAddrMap.put("website", jobj.optString("website", ""));
                custAddrMap.put("shippingRoute", jobj.optString("shippingRoute", ""));
                custAddrMap.put("isBillingAddress", jobj.getBoolean("isBillingAddress"));
                custAddrMap.put("isDefaultAddress", jobj.getBoolean("isDefaultAddress"));
                KwlReturnObject custAddrobject = accountingHandlerDAOobj.saveCustomerAddressesDetails(custAddrMap, companyid);
                
                //*************************save address details in child companies ****************************8
                
                if (ispropagatetochildcompanyflag) {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("propagatedCustomerID", parentCompanyCustomerID);
                    KwlReturnObject result = accCustomerDAOobj.getChildCustomerCount(requestParams);
                    List childCompaniesCustomerList = result.getEntityList();
                    try {
                        for (Object childObj : childCompaniesCustomerList) {

                            Customer cust = (Customer) childObj;
                            if (cust != null) {
                                String childCompanyID = cust.getCompany().getCompanyID();
                                String childcompanyscustomerid = cust.getID();
                                custAddrMap.put("customerid", childcompanyscustomerid);

                                if (custAddrMap.containsKey("shippingRoute") && !StringUtil.isNullOrEmpty((String) custAddrMap.get("shippingRoute"))) {
                                    masterGroupID = String.valueOf(28);
                                    returnObject = accCustomerDAOobj.getMasterItemByNameorID(parentcompanyid, (String) custAddrMap.get("shippingRoute"), masterGroupID, fetchColumn, conditionColumn);
                                    data = (String) returnObject.getEntityList().get(0);

                                    returnObject = accCustomerDAOobj.getMasterItemByNameorID(childCompanyID, data, masterGroupID, conditionColumn, fetchColumn);
                                    data = (String) returnObject.getEntityList().get(0);
                                    custAddrMap.put("shippingRoute", data);
                                }
                                custAddrobject = accountingHandlerDAOobj.saveCustomerAddressesDetails(custAddrMap, childCompanyID);
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //*************************save address details in child companies Ends****************************8
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveCustomer : " + ex.getMessage(), ex);
        }
    }   
    
 @Override
  public JSONObject activateDeactivateCustomers(JSONObject paramJObj){
        JSONObject jobj=new JSONObject();
        String msg="",auditMsg="";
        KwlReturnObject result = null;
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.data, paramJObj.optString(Constants.data));
            String customerActivateDeactivate = paramJObj.optString("activateDeactivateFlag");
            String companyid=paramJObj.optString(Constants.companyKey);
            boolean customerActivateDeactivateFlag = StringUtil.isNullOrEmpty(customerActivateDeactivate)?false:Boolean.parseBoolean(customerActivateDeactivate);
            requestParams.put("customerActivateDeactivateFlag", customerActivateDeactivateFlag);
            requestParams.put(Constants.companyKey, companyid);
            result = accCustomerDAOobj.activateDeactivateCustomers(requestParams);
            issuccess = true;
            msg = customerActivateDeactivateFlag? messageSource.getMessage("acc.customer.activate", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))):messageSource.getMessage("acc.customer.deactivate", null, Locale.forLanguageTag(paramJObj.getString(Constants.language)));
            txnManager.commit(status);
            auditMsg = customerActivateDeactivateFlag ? "Activated Customer " : "Deactivated Customer ";
            for (int i = 0; i < result.getRecordTotalCount(); i++) {
                Customer customer = (Customer) result.getEntityList().get(i);
                Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                auditRequestParams.put(Constants.reqHeader, paramJObj.getString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJObj.getString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJObj.getString(Constants.useridKey));
//                auditTrailObj.insertAuditLog(AuditAction.CUSTOMER_ACTIVATE_DEACTIVATE, "User " +paramJObj.optString(Constants.userfullname) + " has " + auditMsg + "<b>" + customer.getName() + "</b>" + " ( " + customer.getAcccode() + " ) ", request, customer.getID());
                auditTrailObj.insertAuditLog(AuditAction.CUSTOMER_ACTIVATE_DEACTIVATE, "User " +paramJObj.optString(Constants.userfullname) + " has " + auditMsg + "<b>" + customer.getName() + "</b>" + " ( " + customer.getAcccode() + " ) ", auditRequestParams, customer.getID());
            }
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    } 

    public void deleteCustomer(JSONObject paramJobj, JSONArray propagatedCustomerjarr, boolean propagateTOChildCompaniesFalg, String companyid) throws ServiceException, AccountingException {
        KwlReturnObject result = null;
        String auditID = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            JSONArray jArr = null;
            if (propagateTOChildCompaniesFalg) {
                jArr = propagatedCustomerjarr;
            } else {
                requestParams.put(Constants.data, paramJobj.optString(Constants.RES_data));
                jArr = new JSONArray((String) requestParams.get(Constants.data));
            }

            String accountid = "";
            String customerName = "";
            String cusomerCode = "";
            String coaId = "";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);

                if (StringUtil.isNullOrEmpty(jobj.getString("accid")) == false) {
                    accountid = jobj.getString("accid");
                    KwlReturnObject custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), accountid);
                    Customer customer = (Customer) custresult.getEntityList().get(0);
                    customerName = customer.getName();
                    cusomerCode = customer.getAcccode();
                    coaId = customer.getAccount().getID();
                    if (!StringUtil.isNullOrEmpty(accountid)) {//Delete the productvendor mapping
                        accVendorCustomerProductDAOobj.deleteCustomerProductMapped(accountid);
                    }

                    if (!propagateTOChildCompaniesFalg && jobj.getDouble("openbalance") != 0) {
                        throw new AccountingException("Selected record(s) is having the Opening Balance. So it cannot be deleted");
                    } else {
                        // Check in Journal Entry
                        result = accJournalEntryobj.getJEDfromAccount(accountid, companyid);
                        int count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");
                        }

                        // Check Product Entry
                        result = accProductObj.getProductfromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the Account Preferences. So it cannot be deleted.");
                        }

                        // Check for Preferances Entry
                        result = accCompanyPreferencesObj.getPreferencesFromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the Product(s). So it cannot be deleted.");
                        }

                        // Check fot Payment Entry
                        result = accPaymentDAOobj.getPaymentMethodFromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the Term(s). So it cannot be deleted.");
                        }

                        // Check for Tax Entry
                        result = accTaxObj.getTaxFromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the Tax(s). So it cannot be deleted.");
                        }

                        // Check for Tax Entry
                        result = accCustomerDAOobj.getQuotationFromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used for Customer Quotations. So it cannot be deleted.");
                        }
                        // Check for Delivery Order Entry
                        result = accCustomerDAOobj.getDeliveryOrderFromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used for Delivery Order. So it cannot be deleted.");
                        }
                        // Check for Delivery Order /Salesorder /Sales Return /Sales Invoice/Receipt  Entry
                        boolean isused = accCusVenMapDAOObj.isCustomerUsedInTransactions(accountid, companyid); //ERP-19783
                        if (isused) {
                            throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");
                        }
                        result = accCompanyPreferencesObj.getCustomerFromPreferences(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used for Company preferences. So it cannot be deleted.");
                        }
                        List childList = new ArrayList(customer.getChildren());
                        if (childList.size() > 0) {
                            throw new AccountingException("Selected customer(s) is having child customer(s). So it cannot be deleted.");
                        }
                        try {
                            // for delete discount rule created
                            HashMap<String, Object> params = new HashMap<>();
                            params.put("customerID", accountid);
                            params.put("companyID", companyid);
                            accProductObj.deleteProductBrandDiscountDetails(params);

//                        Delete Account
                            result = accCustomerDAOobj.deletecustomervendormapping(accountid);
                            result = accCustomerDAOobj.deleteCustomerCategoryMappingDtails(accountid);
                            result = accCustomerDAOobj.deleteSalesPersonMappingDtails(accountid);
                            accCustomerDAOobj.UpdateCustomerVendorMapping(customer.getID());
                            if (customer.getCompany().getCountry().getID().equals("" + Constants.indian_country_id)) {
                                /**
                                 * Delete customer GST fields history for India.
                                 */
                                accCustomerDAOobj.deleteGstCustomerHistory(accountid);
                            }
                            result = accCustomerDAOobj.deleteCustomer(accountid, companyid);
                            result = accAccountDAOobj.deleteAccount(accountid, companyid);
                            //change used in if this is last transaction present with mapped acocunt
                            boolean returnSuccess = accAccountDAOobj.removeEntryFromAccountUsedIn(Constants.Customer_Default_Account, companyid, coaId);
                        } catch (ServiceException ex) {
                            try {
                                result = accAccountDAOobj.deleteAccount(accountid, true);
                            } catch (ServiceException e) {
                                throw new AccountingException("Selected record(s) is currently used in the transaction(s).");
                            }
                        }
                    }
                    auditID = AuditAction.CUSTOMER_DELETED;
                    if (!propagateTOChildCompaniesFalg) {
                        Map<String, Object> auditParamsMap = new HashMap();
                        auditParamsMap.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
                        auditParamsMap.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                        auditParamsMap.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                        auditParamsMap.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                        auditTrailObj.insertAuditLog(auditID, " User " + paramJobj.optString(Constants.userfullname) + " " + messageSource.getMessage("acc.rem.hasDeletedaCustomer", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " " + customerName + " (" + cusomerCode + " )", auditParamsMap, accountid);
                    }
                }
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("deleteCustomer : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteCustomer : " + ex.getMessage(), ex);
        }
    }

    /**
     * Code is moved from accCustomerControllerCMN.
     * @param paramJobj
     * @return
     * @throws ServiceException
     * @throws AccountingException 
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, AccountingException.class})
    public JSONObject deleteCustomer(JSONObject paramJobj) throws ServiceException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        JSONArray propagatedCustomerjarr = null;
        boolean propagateToChildCompaniesFlag = false;
        String msg = "";
        boolean issuccess = false;
        try {
            String companyid = paramJobj.optString(Constants.companyid);
            deleteCustomer(paramJobj, propagatedCustomerjarr, propagateToChildCompaniesFlag, companyid);
            msg = messageSource.getMessage("acc.cus.del", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
            issuccess = true;
            returnJobj.put(Constants.RES_success, issuccess);
            returnJobj.put(Constants.RES_msg, msg);
            propagateToChildCompaniesFlag = paramJobj.optBoolean("ispropagatetochildcompanyflag", false);
            if (propagateToChildCompaniesFlag) {
                deleteCustomersInChildCompany(paramJobj, propagateToChildCompaniesFlag);
            }
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMNServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accCustomerControllerCMNServiceImpl.deleteCustomer : " + ex.getMessage(), ex);
        } finally {
            try {
                returnJobj.put(Constants.RES_success, issuccess);
                returnJobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE("accCustomerControllerCMNServiceImpl.deleteCustomer : " + ex.getMessage(), ex);
            }
        }
        return returnJobj;
    }

    public void deleteCustomersInChildCompany(JSONObject paramJobj, boolean propagateToChildCompaniesFlag) throws ServiceException, AccountingException {
        try {
            JSONArray propagatedCustomerjarr = null;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.RES_data, paramJobj.optString(Constants.RES_data));
            JSONArray jArr = new JSONArray((String) requestParams.get(Constants.RES_data));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject CustomerJobj = jArr.getJSONObject(i);
                String parentcompanyCustomerid = CustomerJobj.getString("accid");
                String childCompanyName = "";
                String auditID = AuditAction.CUSTOMER_DELETED;
                HashMap<String, Object> requestParamsPropagatedCustomer = new HashMap<String, Object>();
                requestParamsPropagatedCustomer.put("propagatedCustomerID", parentcompanyCustomerid);
                KwlReturnObject result = accCustomerDAOobj.getChildCustomerCount(requestParamsPropagatedCustomer);
                List childCompaniesCustomerList = result.getEntityList();

                JSONObject deleteObj = null;
                for (Object childObj : childCompaniesCustomerList) {
                    String childcompanyscustomerid = "";
                    String childCustomername = "";
                    String childcustomerCode = "";
                    try {
                        Customer cust = (Customer) childObj;
                        if (cust != null) {
                            childcompanyscustomerid = cust.getID();
                            childCustomername = cust.getName();
                            childcustomerCode = cust.getAcccode();
                            String childCompanyID = cust.getCompany().getCompanyID();
                            childCompanyName = cust.getCompany().getSubDomain();

                            propagatedCustomerjarr = new JSONArray();
                            deleteObj = new JSONObject();
                            deleteObj.put("accid", cust.getID());
                            propagatedCustomerjarr.put(deleteObj);
                            deleteCustomer(paramJobj, propagatedCustomerjarr, propagateToChildCompaniesFlag, childCompanyID);
                            Map<String, Object> auditParamsMap = new HashMap();
                            auditParamsMap.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
                            auditParamsMap.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                            auditParamsMap.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                            auditParamsMap.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                            auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + " has deleted customer " + childCustomername + "(" + childcustomerCode + ")" + " from child company " + childCompanyName, auditParamsMap, childcompanyscustomerid);
                        }
                    } catch (Exception ex) {
                        Map<String, Object> auditParamsMap = new HashMap();
                        auditParamsMap.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
                        auditParamsMap.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                        auditParamsMap.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                        auditParamsMap.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                        auditTrailObj.insertAuditLog(auditID, "Customer " + childCustomername + "(" + childcustomerCode + ")" + " could not be deleted  from child company " + childCompanyName, auditParamsMap, childcompanyscustomerid);
                        throw ServiceException.FAILURE("deleteCustomer : " + ex.getMessage(), ex);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteCustomer : " + ex.getMessage(), ex);
        }
    }
}
