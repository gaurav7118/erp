/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.salesorder;

import com.krawler.accounting.integration.common.IntegrationCommonService;
import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import static com.krawler.common.util.Constants.Acc_ConsignmentRequest_ModuleId;
import static com.krawler.common.util.Constants.Acc_Lease_Order_ModuleId;
import com.krawler.common.util.CsvReader;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.inspection.InspectionForm;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.AccCostCenterDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignLineItemProp;
import com.krawler.spring.accounting.customDesign.LineItemColumnModuleMapping;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.term.accTermDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.CommonFnControllerService;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.hibernate.TransactionException;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accSalesOrderServiceImpl implements accSalesOrderService,MessageSourceAware{
    
      
    private HibernateTransactionManager txnManager;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    private fieldDataManager fieldDataManagercntrl;
    private String salesOrderId;
    private accProductDAO accProductObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private VelocityEngine velocityEngine;
    private APICallHandlerService apiCallHandlerService;
    private accAccountDAO accAccountDAOobj;
    private ImportHandler importHandler;
    private ImportDAO importDao;
    private accTermDAO accTermObj;
    private accCustomerDAO accCustomerDAOobj;
    private AccCostCenterDAO accCostCenterObj;
    private accTaxDAO accTaxObj;
    private accVendorDAO accVendorDAOObj;
    private AccProductModuleService accProductModuleService;
    private IntegrationCommonService integrationCommonService;
    private companyDetailsDAO companyDetailsDAOObj;
    private CommonFnControllerService commonFnControllerService;
    private permissionHandlerDAO permissionHandlerDAOObj;
    String recId = "";
    String tranID = "";

    public void setCompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj) {
        this.companyDetailsDAOObj = companyDetailsDAOObj;
    }
    
    public void setIntegrationCommonService(IntegrationCommonService integrationCommonService) {
        this.integrationCommonService = integrationCommonService;
    }
    
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
 
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }
    
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    
    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }
    
    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }
    
    public void setaccTermDAO(accTermDAO accTermObj) {
        this.accTermObj = accTermObj;
    }
    
    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }
    
    public void setaccCostCenterDAO(AccCostCenterDAO accCostCenterDAOObj) {
        this.accCostCenterObj = accCostCenterDAOObj;
    }
    
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    
    public void setAccVendorDAO(accVendorDAO accVendorDAOObj) {
        this.accVendorDAOObj = accVendorDAOObj;
    }
    public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }
    
    public void setCommonFnControllerService(CommonFnControllerService commonFnControllerService) {
        this.commonFnControllerService = commonFnControllerService;
    }

    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }

    /**
     * Create JSON from SalesOrder Object for Avalara integration
     * Used only in Avalara integration
     * @param requestJobj
     * @param salesOrder
     * @param salesOrderDate
     * @param companyid
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws AccountingException
     * @throws SessionExpiredException 
     */
    @Override
    public JSONObject createJsonFromSalesOrderObject(JSONObject requestJobj, SalesOrder salesOrder, Date salesOrderDate, String companyid) throws ServiceException, JSONException, AccountingException, SessionExpiredException {
        JSONObject returnJobj = new JSONObject();
        
        JSONObject lineDetailsJobj = createRowsJsonFromSalesOrderObject(salesOrder);
        JSONArray lineDetailJarr = lineDetailsJobj.optJSONArray(Constants.detail);

        BillingShippingAddresses billingShippingAddresses = salesOrder.getBillingShippingAddresses();
        JSONObject addrObject = new JSONObject();
        if (billingShippingAddresses != null) {
            addrObject.put("address", billingShippingAddresses.getShippingAddress() != null ? billingShippingAddresses.getShippingAddress() : "");
            addrObject.put("city", billingShippingAddresses.getShippingCity() != null ? billingShippingAddresses.getShippingCity() : "");
            addrObject.put("state", billingShippingAddresses.getShippingState() != null ? billingShippingAddresses.getShippingState() : "");
            addrObject.put("country", billingShippingAddresses.getShippingCountry() != null ? billingShippingAddresses.getShippingCountry() : "");
            addrObject.put("postalCode", billingShippingAddresses.getShippingPostal() != null ? billingShippingAddresses.getShippingPostal() : "");
            addrObject.put("recipientName", billingShippingAddresses.getShippingRecipientName() != null ? billingShippingAddresses.getShippingRecipientName() : "");
        }

        returnJobj.put(IntegrationConstants.shipToAddressForAvalara, addrObject.toString());
        returnJobj.put(Constants.detail, lineDetailJarr != null ? lineDetailJarr.toString() : "[]");
        
        //avalaraExemptionCode --> Value of 'AvaTax Exemption Code' dimension
        String avalaraExemptionCode = null;
        if (requestJobj.has(IntegrationConstants.avalaraExemptionCode)) {
            avalaraExemptionCode = requestJobj.getString(IntegrationConstants.avalaraExemptionCode);
        } else if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.customfield, null))) {
            avalaraExemptionCode = integrationCommonService.getExemptionCodeFromCustomFieldsJson(requestJobj);
        } else {
            avalaraExemptionCode = integrationCommonService.getExemptionCodeFromRefModule(companyid, null, Constants.Acc_Sales_Order_ModuleId, salesOrder.getID());
        }
        returnJobj.put(IntegrationConstants.avalaraExemptionCode, avalaraExemptionCode);
        returnJobj.put(Constants.billid, salesOrder.getID());
        returnJobj.put(Constants.billno, salesOrder.getSalesOrderNumber());
        returnJobj.put(Constants.BillDate, authHandler.getDateOnlyFormat().format(salesOrderDate));
        returnJobj.put(Constants.currencyKey, salesOrder.getCurrency() != null ? salesOrder.getCurrency().getCurrencyID() : null);
        returnJobj.put("currencyCode", salesOrder.getCurrency() != null ? salesOrder.getCurrency().getCurrencyCode() : null);
        double exchangeRate = salesOrder.getExternalCurrencyRate() != 0 ? (1.0d / salesOrder.getExternalCurrencyRate()) :  0;
        returnJobj.put("exchangeRate", exchangeRate != 0 ? exchangeRate : null);
        returnJobj.put(Constants.customerid, salesOrder.getCustomer().getID());
        returnJobj.put("customerCode", salesOrder.getCustomer().getAcccode());
        returnJobj.put("salesPersonCode", salesOrder.getSalesperson() != null ? salesOrder.getSalesperson().getCode() : "");
        returnJobj.put("salespersonid", salesOrder.getSalesperson() != null ? salesOrder.getSalesperson() : "");
        returnJobj.put(Constants.moduleid, String.valueOf(Constants.Acc_Sales_Order_ModuleId));
        returnJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
        returnJobj.put(Constants.companyKey, companyid);
        return returnJobj;
    }
    
    private JSONObject createRowsJsonFromSalesOrderObject(SalesOrder salesOrder) throws ServiceException, JSONException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        JSONArray lineDetailJarr = new JSONArray();
        Set<SalesOrderDetail> salesOrderDetailsSet = salesOrder.getRows();
        for (SalesOrderDetail row : salesOrderDetailsSet) {
            JSONObject lineDetailjobj = new JSONObject();
            lineDetailjobj.put(Constants.productid, row.getProduct().getID());
            lineDetailjobj.put("pid", row.getProduct().getProductid());
            lineDetailjobj.put("desc", row.getDescription());
            double quantity = row.getQuantity();
            lineDetailjobj.put(Constants.quantity, quantity);
            double rate = row.getRate();
            int isDiscountPercent = row.getDiscountispercent();
            double discount = row.getDiscount();
            double amountwithouttax = 0d;
            if (isDiscountPercent == 0) {//flat discount
                amountwithouttax = (rate * quantity) - discount;
            } else {//Percent discount
                amountwithouttax = ((rate * quantity) * (100d - discount)) / 100d;
            }
            lineDetailjobj.put("amountwithouttax", amountwithouttax);
            lineDetailjobj.put(IntegrationConstants.parentRecordID, row.getID());
            lineDetailJarr.put(lineDetailjobj);
        }
        returnJobj.put(Constants.detail, lineDetailJarr);
        return returnJobj;
    }

    @Override
    public JSONObject saveSalesOrderJSON(JSONObject paramJobj) throws SessionExpiredException, ServiceException, JSONException {
        JSONObject jobj=new JSONObject();
        
        String userId = paramJobj.getString(Constants.useridKey);
        String companyid = paramJobj.getString(Constants.companyKey);
        String msg = "";
        String billid = "";
        String billno = "";
        int linkflag;
        int istemplate = paramJobj.optInt("istemplate",0);
        String channelName = "", auditMsg = "", auditID = "", entryNumber = "";
        boolean issuccess = false;
        boolean accexception = false;
        boolean isTaxDeactivated = false;
        String butPendingForApproval = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        try {
            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            
            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                entryNumber = paramJobj.optString("salesOrderNumber", null);
            } else {
                entryNumber = paramJobj.optString("number", null);
            }
            boolean isDraft = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isDraft, null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isDraft)) : false; //SDP-13487
            boolean isSaveDraftRecord = (!StringUtil.isNullOrEmpty(paramJobj.optString("isSaveDraftRecord", null))) ? Boolean.parseBoolean(paramJobj.getString("isSaveDraftRecord")) : false; //SDP-13487
            boolean isAutoSeqForEmptyDraft = (!StringUtil.isNullOrEmpty(paramJobj.optString("isAutoSeqForEmptyDraft", null))) ? Boolean.parseBoolean(paramJobj.getString("isAutoSeqForEmptyDraft")) : false; //SDP-13927 : If Draft already having sequence no. then do not update it
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) returnObject.getEntityList().get(0);

            if (!paramJobj.has(Constants.sequenceformat) || StringUtil.isNullOrEmpty(paramJobj.optString(Constants.sequenceformat, null))) {
                String sequenceformatid = null;
                Map<String, Object> sfrequestParams = new HashMap<String, Object>();
                sfrequestParams.put(Constants.companyKey, paramJobj.get(Constants.companyKey));
                sfrequestParams.put("modulename", "autoso");
                KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
                List<SequenceFormat> ll = seqFormatResult.getEntityList();
                if (ll.size() > 0) {
                    SequenceFormat format = (SequenceFormat) ll.get(0);
                    sequenceformatid = format.getID();
                    paramJobj.put(Constants.sequenceformat, sequenceformatid);
                } else if (!StringUtil.isNullOrEmpty(entryNumber)) {
                    paramJobj.put(Constants.sequenceformat, "NA");
                }
            }//end of sequenceformat
            
             /**
             * creating template for sales order.
             * istemplate=2 //creating only template
             * istemplate=0 //creating only transaction
             */
            if (istemplate != 2 && StringUtil.isNullOrEmpty(paramJobj.optString(Constants.sequenceformat, null))) {
                JSONObject response = StringUtil.getErrorResponse("acc.common.erp33", paramJobj, "Sequence Format Details are missing. <br>Please set the Sequence Format from system controls.", messageSource);
                throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
            }
            /**
             * createAsTransactionChkboxwithTemplate- used to create template along with transaction.
             */
            boolean createAsTransactionChkboxwithTemplate = paramJobj.optString("createAsTransactionChkbox").equalsIgnoreCase("on") ? true : false;
            String sequenceformat = paramJobj.optString(Constants.sequenceformat, null);
            boolean isOpeningBalanceOrder = paramJobj.optString("isOpeningBalanceOrder", null) != null ? Boolean.parseBoolean(paramJobj.getString("isOpeningBalanceOrder")) : false;
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(paramJobj.optString("copyInv", null)) ? false : Boolean.parseBoolean(paramJobj.getString("copyInv"));
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString("isLeaseFixedAsset", null))) ? Boolean.parseBoolean(paramJobj.getString("isLeaseFixedAsset")) : false;
            boolean isConsignment = paramJobj.optString("isConsignment", null) != null ? Boolean.parseBoolean(paramJobj.getString("isConsignment")) : false;
            boolean isJobWorkOrderReciever = paramJobj.optString("isJobWorkOrderReciever", null) != null ? Boolean.parseBoolean(paramJobj.getString("isJobWorkOrderReciever")) : false;
            boolean isEditedPendingDocument = StringUtil.isNullOrEmpty(paramJobj.optString("isEditedPendingDocument", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEditedPendingDocument"));
            boolean editRejectedPendingSalesOrder = false;
            String soid=null;
            
            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                soid = paramJobj.optString(Constants.billid, null);
            } else {
                soid = paramJobj.optString("invoiceid", null);
            }

            String fromLinkCombo = paramJobj.optString("fromLinkCombo", null) != null ? paramJobj.getString("fromLinkCombo") : "";
            String additionalsauditmessage = "";
            if (!StringUtil.isNullOrEmpty(soid)) {
                //to check rejected sales orders deleteflag nad aprrovalstatuslevel to create autiString. 
                KwlReturnObject salesorderObj = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soid);
                SalesOrder salesorder = (SalesOrder) salesorderObj.getEntityList().get(0);
                if (salesorder.isDeleted() && salesorder.getApprovestatuslevel() < 0) {
                    editRejectedPendingSalesOrder = true;
                }
            }
            if (!StringUtil.isNullOrEmpty(soid)) {
                auditMsg = "updated";
                if (isOpeningBalanceOrder) {
                    auditID = AuditAction.OPENING_BALANCE_UPDATED;
                } else {
                    auditID = AuditAction.SALES_ORDER;
                }
            } else {
                auditMsg = "added new";
                if (isOpeningBalanceOrder) {
                    auditID = AuditAction.OPENING_BALANCE_CREATED;
                } else {
                    auditID = AuditAction.SALES_ORDER;
                }
            }
            if (isLeaseFixedAsset) {
                auditMsg += " Lease";
            }
            KwlReturnObject socnt = null;
            if (!StringUtil.isNullOrEmpty(soid)) {//In edit case checks duplicate number
                socnt = accSalesOrderDAOobj.getSalesOrderEditCount(entryNumber, companyid, soid);
                if (socnt.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    accexception = true;
                    if (isJobWorkOrderReciever) {
                        throw new AccountingException(messageSource.getMessage("acc.field.JobWorkOrdernumber", null, Locale.forLanguageTag(paramJobj.getString("language"))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                    } else {
                        throw new AccountingException(messageSource.getMessage("acc.field.SalesOrdernumber", null, Locale.forLanguageTag(paramJobj.getString("language"))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                    }
                }
            } else {//In add case checks duplicate number
               if (!StringUtil.isNullObject(entryNumber) && entryNumber!="") {
                    socnt = accSalesOrderDAOobj.getSalesOrderCount(entryNumber, companyid);
                }
                if (socnt !=null && socnt.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    accexception = true;
                    if (isJobWorkOrderReciever) {
                        throw new AccountingException(messageSource.getMessage("acc.field.JobWorkOrdernumber", null, Locale.forLanguageTag(paramJobj.getString("language"))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                    } else {
                        throw new AccountingException(messageSource.getMessage("acc.field.SalesOrdernumber", null, Locale.forLanguageTag(paramJobj.getString("language"))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                    }
                }
                                
                //Check Deactivate Tax in New Transaction.
                if (!fieldDataManagercntrl.isTaxActivated(paramJobj)) {
                    isTaxDeactivated = true;
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.tax.deactivated.tax.saveAlert", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                }
            }

            synchronized (this) {//Checks duplicate number for simultaneous transactions
                status = txnManager.getTransaction(def);
                if (sequenceformat.equals("NA")) {
                    KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_Sales_Order_ModuleId);
                    if (resultInv.getRecordTotalCount() > 0) {
                        accexception = true;
                        throw new AccountingException(messageSource.getMessage("acc.SO.selectedsalesorderno", null, Locale.forLanguageTag(paramJobj.getString("language"))) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                    } else {
                        accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_Sales_Order_ModuleId);
                    }
                }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
             /**
             * creating template for sales order.
             * istemplate=2 //creating only template
             * istemplate=0 //creating only transaction
             */
            if (createAsTransactionChkboxwithTemplate) {
                paramJobj.put("istemplate", 2);
                saveSalesOrder(paramJobj);
                paramJobj.remove("istemplate");
            }
             /**
             * creating sales order.
             */
            List soList = saveSalesOrder(paramJobj);
            List mailParams = (List) soList.get(5);
            SalesOrder salesOrder = (SalesOrder) soList.get(0);
            billid = salesOrder.getID();
            linkflag = salesOrder.getLinkflag();
            billno = salesOrder.getSalesOrderNumber();
            if (soList.get(1) != null) {//fields updated
                additionalsauditmessage = soList.get(1).toString();
            }            
            
            String roleName = soList.get(8) != null ? (String) soList.get(8) : "";
            boolean isAuthorityToApprove = soList.get(9) != null ? (Boolean) soList.get(9) : false;
            boolean sendPendingDocumentsToNextLevel = soList.get(10) != null ? (Boolean) soList.get(10) : false;
            
            issuccess = true;
            double totalAmount = 0;
            int approvalStatusLevel = 11;

            if (soList.get(2) != null) { // totalAmount
                totalAmount = Double.parseDouble(soList.get(2).toString());
            }
            if (soList.get(3) != null) {// approvalStatusLevel 
                approvalStatusLevel = Integer.parseInt(soList.get(3).toString());
            }
            if (soList.get(4) != null) {//butPendingForApproval 
                butPendingForApproval = soList.get(4).toString();
            }
            
            //Get mapping details id of invoice documents
            String savedFilesMappingId = paramJobj.optString("savedFilesMappingId", "");
            if(!StringUtil.isNullOrEmpty(savedFilesMappingId)){
                /**
                * Save temporary saved attachment files mapping in permanent table
                */
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("savedFilesMappingId", savedFilesMappingId);
                jsonObj.put("docId", billid);
                jsonObj.put("companyid", companyid);
                //
                saveDocuments(jsonObj);
            }
            txnManager.commit(status);
            status = null;
            TransactionStatus AutoNoStatus = null;
            try {
                synchronized (this) {
                    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                        def1.setName("AutoNum_Tx");
                        def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                        AutoNoStatus = txnManager.getTransaction(def1);
                    if (StringUtil.isNullOrEmpty(soid) && (!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA"))) {
                        boolean seqformat_oldflag = false;//    old flag was used when sequence format not implemented.StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                        String nextAutoNumber = "";
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        if (seqformat_oldflag) {
                            nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_SALESORDER, sequenceformat);
                            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextAutoNumber);
                        } else {
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_SALESORDER, sequenceformat, seqformat_oldflag, salesOrder.getOrderDate());
                        }

                        seqNumberMap.put(Constants.DOCUMENTID, billid);
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        //SDP-13487 : When user save the transaction at very first time then transaction no. & sequence no.will be saved as empty.
                        seqNumberMap.put(Constants.isDraft, isDraft);
                        billno = accSalesOrderDAOobj.updateSOEntryNumberForNewSO(seqNumberMap);
                    } else if(isSaveDraftRecord && !sequenceformat.equals("NA") && isAutoSeqForEmptyDraft){  //SDP-13487 : Do not update Draft No. in case of Sequence Format as "NA",  //SDP-13927 : If Draft already having sequence no. then do not update it
                        /*
                        Below piece of code has written to handle Auto-Sequence no.in edit mode.
                        When user open the draft in edit mode, he can save it as a draft or a transaction. If it save as draft again then this code will not be execute.
                        But, if he saves it as a transaction then this code will be execute to get the Auto-Sequence No and set it to transaction no.
                        */
                        String nextAutoNumber = "";
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_SALESORDER, sequenceformat, false, salesOrder.getOrderDate());
                        seqNumberMap.put(Constants.DOCUMENTID, billid);
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        seqNumberMap.put(Constants.isDraft, isDraft);
                        billno = accSalesOrderDAOobj.updateSOEntryNumberForNewSO(seqNumberMap);
                    } else if(isDraft && !sequenceformat.equals("NA") && isAutoSeqForEmptyDraft){
                        /* SDP-13923
                        This piece of code has been written to fix below case.
                        1)Draft has been made with NA. 2)Draft has opened in edit mode and saved as a draft again with Auto-Sequence Format.
                        3)Again draft opened in edit mode then sequence format should be Auto-Sequence Format.
                        */
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_SALESORDER, sequenceformat, false, salesOrder.getOrderDate());
                        seqNumberMap.put(Constants.DOCUMENTID, billid);
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        seqNumberMap.put(Constants.isDraft, isDraft);
                        billno = accSalesOrderDAOobj.updateSOEntryNumberForNewSO(seqNumberMap);
                    }
                    
                    DocumentEmailSettings documentEmailSettings = null;
                    KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), companyid);
                    documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
                    if (documentEmailSettings.isConsignmentReqGenerationMail()) {
                        Set<SalesOrderDetail> soDetails = salesOrder.getRows();
                        boolean sendMail = false;
                        for (SalesOrderDetail sodetail : soDetails) {
                            double pendingQty = sodetail.getQuantity() - (sodetail.getApprovedQuantity() + sodetail.getRejectedQuantity());
                            if (pendingQty > 0) {
                                sendMail = true;
                            }
                        }
                        if (sendMail && !isCopy && isConsignment) {
                            sendConsignmentApprovalEmails(paramJobj, user, salesOrder, billno, false, isEdit);
                        }

                    }
                    txnManager.commit(AutoNoStatus);
                }

            } catch (Exception ex) {
                if (AutoNoStatus != null) {
                    txnManager.rollback(AutoNoStatus);
                }
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Sales_Order_ModuleId);//Delete entry in temporary table
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (istemplate == 1) {
                msg = messageSource.getMessage("acc.field.SalesOrderandTemplatehasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " " + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString("language"))) + ": <b>" + billno + "</b>";
            } else if (istemplate == 2) {
                msg = messageSource.getMessage("acc.field.SalesOrderTemplatehasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language")));
            } else if (isLeaseFixedAsset) {
                msg = messageSource.getMessage("acc.lso.save", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " " + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString("language"))) + ": <b>" + billno + "</b>";   //"Sales order has been saved successfully";
            } else if (isConsignment) {
                msg = messageSource.getMessage("acc.consignment.order.save", null, Locale.forLanguageTag(paramJobj.getString("language"))) + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString("language"))) + ": <b>" + billno + "</b>";   //"consignment saved successfully";
            } else if (isJobWorkOrderReciever) {
                msg = messageSource.getMessage("acc.jobwork.order.save", null, Locale.forLanguageTag(paramJobj.getString("language"))) + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString("language"))) + ": <b>" + billno + "</b>";   //"consignment saved successfully";
            } else if(isDraft){   
                //SDP-13487 : Below code snippet has written to show difference message when entry has been saved as a transaction or a draft.
                if(StringUtil.isNullOrEmpty(billno)){
                    msg = messageSource.getMessage("acc.field.SalesOrderDraft", null, Locale.forLanguageTag(paramJobj.getString("language"))) +" "+messageSource.getMessage("acc.draft.success.msg.hasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language")));                    
                } else {
                    msg = messageSource.getMessage("acc.field.SalesOrderDraft", null, Locale.forLanguageTag(paramJobj.getString("language"))) +" <b>"+billno+"</b> "+messageSource.getMessage("acc.draft.success.msg.hasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language")));                    
                }
            } else {
                msg = messageSource.getMessage("acc.field.SalesOrderhasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language")));
                msg += (StringUtil.isNullOrEmpty(butPendingForApproval) ? "." : " ") + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString("language"))) + ": <b>" + billno + "</b>";
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("createAsTransactionChkbox", "")) && paramJobj.optString("createAsTransactionChkbox").equalsIgnoreCase("on") && !StringUtil.isNullOrEmpty(paramJobj.optString("templatename", ""))) {
                    msg += " Template Name: <b>" + paramJobj.optString("templatename", "") + "</b>";
                }
            }
            if (mailParams != null && !mailParams.isEmpty()) {
                /**
                 * parameters required for sending mail
                 */
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put(Constants.companyid, companyid);
                mailParameters.put(Constants.prNumber, billno);
                mailParameters.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                mailParameters.put(Constants.createdBy, userId);
                mailParameters.put(Constants.PAGE_URL, paramJobj.optString(Constants.PAGE_URL));

                Iterator itr = mailParams.iterator();

                while (itr.hasNext()) {
                    HashMap<String, Object> paramsMap = (HashMap<String, Object>) itr.next();

                    mailParameters.put(Constants.ruleid, (String) paramsMap.get("ruleid"));
                    mailParameters.put(Constants.fromName, (String) paramsMap.get("fromName"));
                    mailParameters.put(Constants.hasApprover, (Boolean) paramsMap.get("hasApprover"));
                    mailParameters.put("level",salesOrder.getApprovallevel());
                    sendMailToApprover(mailParameters);
                }
            }
            jobj.put("SOID", salesOrderId);
            jobj.put("billid", billid);
            jobj.put("billno", billno);
            jobj.put("amount", totalAmount);
            jobj.put("repeatedid", "");
            jobj.put("intervalUnit", 0);
            jobj.put("linkflag", linkflag);
            jobj.put("nextdate", "");
            jobj.put("pendingApproval", approvalStatusLevel != 11);
            String template = " Document template for record ";
            String sms = "";
            if (istemplate == 0) {
                template = "";
            }

            String modulename = "";
            if (isConsignment) {
                modulename = " Consignment Request ";
            } else if (isLeaseFixedAsset) {
                modulename = " Order ";
            } else if (isJobWorkOrderReciever) {
                modulename = " "+Constants.JOBWORK_IN_ORDER+" ";
            }else {
                modulename = " Sales Order ";
            }
            String linkedDocuments = (String) soList.get(6);
            /*
             * Preparing Audit trial message if document is linking at the time
             * of creating
             */
            String linkingMessages = "";
            if (!StringUtil.isNullOrEmpty(linkedDocuments) && !StringUtil.isNullOrEmpty(fromLinkCombo)) {
                linkingMessages = " by Linking to " + fromLinkCombo + " " + linkedDocuments;
            }
            
            Map<String, Object> auditRequestParams=new HashMap<String, Object>();
            auditRequestParams.put(sms, status);
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
            
            if (isOpeningBalanceOrder) {
                sms = "User " + paramJobj.getString(Constants.userfullname) + " has  " + auditMsg + " a Opening Balance Order " + billno;
//                auditTrailObj.insertAuditLog(auditID, sms + additionalsauditmessage, request, billno);
                auditTrailObj.insertAuditLog(auditID, sms + additionalsauditmessage, auditRequestParams, billno);
            } else if (editRejectedPendingSalesOrder) {
                sms = " Rejected Sales Order " + billno + " has been edited by " + paramJobj.getString(Constants.userfullname);
//                auditTrailObj.insertAuditLog(auditID, sms + additionalsauditmessage, request, billno);
                auditTrailObj.insertAuditLog(auditID, sms + additionalsauditmessage, auditRequestParams, billno);
            } else {
                sms = "User " + paramJobj.getString(Constants.userfullname) + " has  " + auditMsg + template + modulename + billno + linkingMessages + (approvalStatusLevel != 11 ? " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "");
//                auditTrailObj.insertAuditLog(auditID, sms + additionalsauditmessage, request, billno);
                auditTrailObj.insertAuditLog(auditID, sms + additionalsauditmessage, auditRequestParams, billno);
            }
            String unlinkMessage = (String) soList.get(7);
            /*
             * Inserting Entry in Audit trial when any document is unlinking
             * through Edit
             */
            if (!StringUtil.isNullOrEmpty(unlinkMessage)) {
//                auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.getString(Constants.userfullname) + " has unlinked " + "Sales Order " + billno + unlinkMessage + ".", request, billno);
                auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.getString(Constants.userfullname) + " has unlinked " + "Sales Order " + billno + unlinkMessage + ".", auditRequestParams, billno);
            }
            if (isLeaseFixedAsset) {
                channelName = "/LeaseOrderReport/gridAutoRefresh";
            } else if (!(isLeaseFixedAsset || isConsignment)) {//For normal SO
                channelName = "/SalesOrderReport/gridAutoRefresh";
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("createAsTransactionChkbox", "")) && paramJobj.optString("createAsTransactionChkbox").equalsIgnoreCase("on") && !StringUtil.isNullOrEmpty(paramJobj.optString("templatename", ""))) {
                auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has added Document Template "+paramJobj.optString("templatename", "")+ " for record Sales Order" , auditRequestParams, tranID);
            }
            //Allocating quantity to Blocked SO Quantity 
            boolean activateCRblockingWithoutStock = false;
            KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), salesOrder.getCompany().getCompanyID());
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
            activateCRblockingWithoutStock = extraCompanyPreferences.isActivateCRblockingWithoutStock();
            if (activateCRblockingWithoutStock) {
                TransactionStatus statusforBlockSOQty = txnManager.getTransaction(def);
                try {
                    assignStockToPendingConsignmentRequests(paramJobj, company, user);
                    txnManager.commit(statusforBlockSOQty);
                } catch (Exception ex) {
                    txnManager.rollback(statusforBlockSOQty);
                    Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            /*------Code if we edit pending document---------  */
            if (isEditedPendingDocument) {

                /*--If check "Send pending documents to next level" is activated from system preferences---------  */
                if (sendPendingDocumentsToNextLevel) {

                    if (roleName != "" && isAuthorityToApprove) {

                        /*----Prepare Messages and inset AuditLog for approval document------  */
                        msg += "<br>";
                        msg += messageSource.getMessage("acc.field.SalesOrderhasbeenapprovedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " by " + roleName + " " + paramJobj.getString(Constants.userfullname) + " at Level " + salesOrder.getApprovestatuslevel() + ".";

                        auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER, "User " + paramJobj.getString(Constants.userfullname) + " has Approved a Sales Order " + salesOrder.getSalesOrderNumber() + " at Level-" + salesOrder.getApprovestatuslevel(), auditRequestParams, salesOrder.getID());
                    } else {//If User have no authority to approve the document
                        msg += "<br>";
                        msg += messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, Locale.forLanguageTag(paramJobj.getString("language"))) + salesOrder.getApprovestatuslevel() + ".";
                    }
                } else if (!isAuthorityToApprove && butPendingForApproval == "") {//If user have no authority to approve document
                    msg += "<br>";
                    msg += messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, Locale.forLanguageTag(paramJobj.getString("language"))) + salesOrder.getApprovestatuslevel() + " and record will be available at this level for approval" + ".";
                }
            }
            
            status = txnManager.getTransaction(def);
            accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Sales_Order_ModuleId);//Delete entry in temporary table
            txnManager.commit(status);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Sales_Order_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex1);
            }
                msg = "" + ex.getMessage();
                if (ex.getMessage() == null) {
                    msg = ex.getCause().getMessage();
                }
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Sales_Order_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex1);
            }
                msg = "" + ex.getMessage();
                if (ex.getMessage() == null) {
                    msg = ex.getCause().getMessage();
                }
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("isAccountingExe", accexception);
                jobj.put(Constants.channelName, channelName);
                jobj.put(Constants.isTaxDeactivated, isTaxDeactivated);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
 
    @Override
    public JSONObject saveSalesOrderLinkingJSON(JSONObject paramJobj) throws SessionExpiredException, ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean accexception = false;
        String msg = "";
        String sonumber="";
        KwlReturnObject unlinkobj=null;
        String newreceiptno="";
        try {
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("linkedAdvancePaymentId", null))) {
                ArrayList params = new ArrayList();
                String docid = paramJobj.optString("docid", "");
                HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                requestParamsLinking.put("soid", docid);
                requestParamsLinking.put("unlinkflag", true);
                requestParamsLinking.put("type", 5);
                unlinkobj=accSalesOrderDAOobj.deleteLinkingInformationOfSO(requestParamsLinking);
                String receiptNo =unlinkobj.getEntityList()!=null&&unlinkobj.getEntityList().size() > 0 ? (String) unlinkobj.getEntityList().get(0) : "";
                
                KwlReturnObject soresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), docid);
                SalesOrder salesOrderobj = (SalesOrder) soresult.getEntityList().get(0);
                sonumber = salesOrderobj.getSalesOrderNumber();
                String[] linkNumbers = paramJobj.getString("linkedAdvancePaymentId").split(",");
                String[] linkDocNos = paramJobj.optString("linkedAdvancePaymentNo", "").split(",");
                docid = paramJobj.optString("docid", "");
                int linkDocNosLength = linkDocNos.length;
                if (linkNumbers.length > 0) {
                    for (int i = 0; i < linkNumbers.length; i++) {
                        if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {
                            requestParamsLinking = new HashMap<String, Object>();
                            /*
                             * saving linking informaion of Sales Order while
                             * linking with Customer Quotation
                             */
                            requestParamsLinking.put("linkeddocid", linkNumbers[i]);
                            requestParamsLinking.put("docid", docid);
                            requestParamsLinking.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                            requestParamsLinking.put("linkeddocno", i < linkDocNosLength ? linkDocNos[i] : "");
                            requestParamsLinking.put("sourceflag", 1);
                            accSalesOrderDAOobj.saveSalesOrderLinking(requestParamsLinking);
                           newreceiptno+=" "+linkDocNos[i];
                        }
                    }
                }
                Map<String, Object> auditRequestParams = new HashMap<>();
                auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                if(!receiptNo.isEmpty()){
                    auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER, "User " + paramJobj.getString(Constants.userfullname) + " has unlinked  Advance Receive Payment "+ receiptNo  + " and linked Advance Receive Payment "+ newreceiptno+ " with Sales Order" +  sonumber, auditRequestParams, docid);
                }
                else{
                    auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER, "User " + paramJobj.getString(Constants.userfullname) + " has linked Advance Receive Payment "+ newreceiptno+ " with Sales Order" +  sonumber, auditRequestParams, docid);
                }     
                msg = messageSource.getMessage("acc.paymentSelection.window.linkedSuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language")));
             
            } else if (StringUtil.isNullOrEmpty(paramJobj.getString("linkedAdvancePaymentId"))) {
                ArrayList params = new ArrayList();
                String docid = paramJobj.optString("docid", "");
                HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                requestParamsLinking.put("soid", docid);
                requestParamsLinking.put("unlinkflag", true);
                requestParamsLinking.put("type", 5);
                unlinkobj=accSalesOrderDAOobj.deleteLinkingInformationOfSO(requestParamsLinking);
                String receiptNo = !StringUtil.isNullObject(unlinkobj.getEntityList())&&unlinkobj.getEntityList().size() > 0 ? (String) unlinkobj.getEntityList().get(0) : "";
               
               
                KwlReturnObject soresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), docid);
                SalesOrder salesOrderobj = (SalesOrder) soresult.getEntityList().get(0);
                sonumber = salesOrderobj.getSalesOrderNumber();
                msg = messageSource.getMessage("acc.paymentSelection.window.unlinkedSuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language")));

                Map<String, Object> auditRequestParams = new HashMap<>();
                auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER, "User " + paramJobj.getString(Constants.userfullname) + " has unlinked Advance Receive Payment " + receiptNo  + " from Sales Order  " + sonumber + " ", auditRequestParams, docid);

            }
                             
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put("isAccountingExe", accexception);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
   public List saveSalesOrder(JSONObject paramJobj) throws SessionExpiredException, ServiceException, AccountingException, JSONException, ScriptException, MessagingException, ParseException {
        SalesOrder salesOrder = null;
        List newList = new ArrayList();
        List newSOList = new ArrayList();
        int pendingApprovalFlag = 0;
        List mailParams=null;
        String unlinkMessage="";
        String CustomerId=null;
        String currencyid=null;
        String entryNumber = null;
        String billdate=null;
        String customerpoRefNo=null;
        String salesPerson=null;
        String soid = null;
        String credittermid=null;
        boolean gstIncluded =false ;
        try {
            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                CustomerId = paramJobj.optString(Constants.customerName, null);
                currencyid = (paramJobj.optString(Constants.currencyName, null) == null ? paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.getString(Constants.currencyName));
                entryNumber = paramJobj.optString("salesOrderNumber", null);
                billdate = paramJobj.optString("OrderDate", null);
                customerpoRefNo = paramJobj.optString("customerPORefNo", null);
                salesPerson = paramJobj.optString("salesperson", null);
                soid=paramJobj.optString(Constants.billid,null);
                credittermid=paramJobj.optString("term",null);
                gstIncluded = (StringUtil.isNullOrEmpty(paramJobj.optString("gstIncluded", null))) ? false : Boolean.parseBoolean(paramJobj.getString("gstIncluded"));
            } else {
                CustomerId = paramJobj.optString("customer", null);
                currencyid = (paramJobj.optString(Constants.currencyKey, null) == null ? paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.getString(Constants.currencyKey));
                entryNumber = paramJobj.optString("number", null);
                billdate = paramJobj.optString("billdate", null);
                customerpoRefNo = paramJobj.optString("customerporefno", null);
                salesPerson = paramJobj.optString("salesPerson", null);
                soid=paramJobj.optString("invoiceid",null);
                credittermid=paramJobj.optString("termid",null);
                gstIncluded =paramJobj.optString("includingGST", null) == null ? false : Boolean.parseBoolean(paramJobj.getString("includingGST"));
            }
            boolean isRoundingAdjustmentApplied = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.IsRoundingAdjustmentApplied, null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.IsRoundingAdjustmentApplied)) : false;                  
            int istemplate = paramJobj.optString("istemplate",null) != null ? Integer.parseInt(paramJobj.getString("istemplate")) : 0;
            boolean isCopy = StringUtil.isNullOrEmpty(paramJobj.optString("copyInv",null)) ? false : Boolean.parseBoolean(paramJobj.getString("copyInv"));
            String currentUser =paramJobj.getString(Constants.useridKey);
            String taxid = null;
//            taxid = paramJobj.optString("taxid",null);
            if (paramJobj.optString("taxid").equalsIgnoreCase("None")) {
                taxid = null;
            } else {
                taxid = paramJobj.optString("taxid",null);
            }
            String sequenceformat = paramJobj.optString(Constants.sequenceformat,null);
            String companyid =  paramJobj.getString(Constants.companyKey);
            String createdby = paramJobj.getString(Constants.useridKey);
            String modifiedby = paramJobj.getString(Constants.useridKey);
            
            KwlReturnObject extraPrefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCMPPreferences = (ExtraCompanyPreferences) extraPrefresult.getEntityList().get(0);
            /**
             * If Avalara Integration is enabled and tax calculation in Avalara Integration is enabled
             * then we make below method call which creates and adds tax details in requestJson
             * This is used when transaction is created by means other than UI; for example import, REST etc
             */
            if (extraCMPPreferences != null && extraCMPPreferences.isAvalaraIntegration()) {
                JSONObject paramsJobj = new JSONObject();
                paramsJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
                paramsJobj.put(Constants.companyKey, companyid);
                if (integrationCommonService.isTaxCalculationEnabled(paramsJobj)) {
                    paramJobj = integrationCommonService.createAvalaraTaxDetails(paramJobj, Constants.Acc_Sales_Order_ModuleId);
                }
            }
            String customfield = paramJobj.optString(Constants.customfield,null);
            
            long createdon = System.currentTimeMillis();
            long updatedon = createdon;
            
            boolean isOpeningBalanceOrder = paramJobj.optString("isOpeningBalanceOrder",null) != null ? Boolean.parseBoolean(paramJobj.getString("isOpeningBalanceOrder")) : false;
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit",null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEdit"));
            boolean isLinkedTransaction = StringUtil.isNullOrEmpty(paramJobj.optString("isLinkedTransaction",null)) ? false : Boolean.parseBoolean(paramJobj.getString("isLinkedTransaction"));
            boolean islockQuantity = !StringUtil.isNullOrEmpty(paramJobj.optString("islockQuantity",null))?Boolean.parseBoolean(paramJobj.getString("islockQuantity")):false;
            boolean isDraft = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isDraft,null))) ? Boolean.parseBoolean(paramJobj.optString(Constants.isDraft)) : false;
            boolean isSaveDraftRecord = (!StringUtil.isNullOrEmpty(paramJobj.optString("isSaveDraftRecord", null))) ? Boolean.parseBoolean(paramJobj.getString("isSaveDraftRecord")) : false; //SDP-13487
            boolean isAutoSeqForEmptyDraft = (!StringUtil.isNullOrEmpty(paramJobj.optString("isAutoSeqForEmptyDraft", null))) ? Boolean.parseBoolean(paramJobj.getString("isAutoSeqForEmptyDraft")) : false; //SDP-13927 : If Draft already having sequence no. then do not update it
            boolean isMRPSalesOrder = StringUtil.isNullOrEmpty(paramJobj.optString("isMRPSalesOrder",null)) ? false : Boolean.parseBoolean(paramJobj.getString("isMRPSalesOrder"));
            boolean isJobWorkOrderReciever = StringUtil.isNullOrEmpty(paramJobj.optString("isJobWorkOrderReciever",null)) ? false : Boolean.parseBoolean(paramJobj.getString("isJobWorkOrderReciever"));
            boolean RCMApplicable = paramJobj.optBoolean("GTAApplicable",false);
            boolean isdropshipchecked = paramJobj.optString("isdropshipchecked",null) != null ? Boolean.parseBoolean(paramJobj.getString("isdropshipchecked")) : false;
            boolean isEditedPendingDocument = StringUtil.isNullOrEmpty(paramJobj.optString("isEditedPendingDocument", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEditedPendingDocument"));
            String costCenterId = paramJobj.optString(Constants.costcenter,null);
            boolean isLeaseFixedAsset = false;
            boolean isConsignment = false;
            String isConsignmentStr = paramJobj.optString("isConsignment",null);
            String isLeaseFixedAssetStr = paramJobj.optString("isLeaseFixedAsset",null);
            String deletedLinkedDocumentID = paramJobj.optString("deletedLinkedDocumentId",null);
            
            if (!StringUtil.isNullOrEmpty(isLeaseFixedAssetStr)) {
                isLeaseFixedAsset = Boolean.parseBoolean(isLeaseFixedAssetStr);
            }
            if (!StringUtil.isNullOrEmpty(isConsignmentStr)) {
                isConsignment = Boolean.parseBoolean(isConsignmentStr);
            }
            
            boolean isLinkedFromMaintenanceNumber = (!StringUtil.isNullOrEmpty(paramJobj.optString("isLinkedFromMaintenanceNumber",null))) ? Boolean.parseBoolean(paramJobj.getString("isLinkedFromMaintenanceNumber")) : false;
            
            String maintenanceId = "";
            
            if (isLinkedFromMaintenanceNumber && !StringUtil.isNullOrEmpty(paramJobj.optString("maintenanceId",null))) {
                maintenanceId = paramJobj.getString("maintenanceId");
            }
            
            boolean isLinkedFromCustomerQuotation = (!StringUtil.isNullOrEmpty(paramJobj.optString("isLinkedFromCustomerQuotation"))) ? Boolean.parseBoolean(paramJobj.getString("isLinkedFromCustomerQuotation")) : false;
            boolean isLinkedFromReplacementNumber = (!StringUtil.isNullOrEmpty(paramJobj.optString("isLinkedFromReplacementNumber"))) ? Boolean.parseBoolean(paramJobj.getString("isLinkedFromReplacementNumber")) : false;
            
            String replacementId = "";
            
            if (isLinkedFromReplacementNumber && !StringUtil.isNullOrEmpty(paramJobj.optString("replacementId",null))) {
                String[] replacementIdArray = paramJobj.optString("replacementId","").split(",");
                replacementId = replacementIdArray[0];// only single select option will be true in linking combo
            }
            
            String customerQuotationId = "";
            
            if (isLinkedFromCustomerQuotation && !StringUtil.isNullOrEmpty(paramJobj.optString("linkNumber",null))) {
                String[] customerQuotationIdArray = paramJobj.optString("linkNumber","").split(",");
                customerQuotationId = customerQuotationIdArray[0];// all quotation will be of same contract, so only one quotation is sufficient to get contract number.
            }
            
            String shipLength = paramJobj.optString("shipLength",null);
            String invoicetype = paramJobj.optString("invoicetype",null);
            String custWarehouse = paramJobj.optString("custWarehouse",null);
            String movementtype = paramJobj.optString("movementtype",null);
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate","1"));
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParamsJson(paramJobj);
            Boolean islockQuantityflag = Boolean.parseBoolean(paramJobj.optString("islockQuantity",""));   //get the value of lock quantity         
            
            HashMap<String, Object> soDataMap = new HashMap<String, Object>();
            Map<String, Object> oldsoMap = new HashMap<String, Object>();
            Map<String, Object> newAuditKey = new HashMap<String, Object>();
            String auditMessage="";
           
            synchronized (this) {
                if (!StringUtil.isNullOrEmpty(soid)) { //For edit case
                    // delete asset detail if it is fixed asset lease sales order
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soid);
                    SalesOrder so = (SalesOrder) result.getEntityList().get(0);
                    isRoundingAdjustmentApplied=so.isIsRoundingAdjustmentApplied();
                    if (so.getLeaseOrMaintenanceSO() == 1) {// if it is a lease SO
                        deleteAssetDetails(so, companyid);
                    }
                    
                    KwlReturnObject socnt = accSalesOrderDAOobj.getSalesOrderEditCount(entryNumber, companyid, soid);
                    if (socnt.getRecordTotalCount() > 0 && istemplate != 2 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.SalesOrdernumber", null,Locale.forLanguageTag(paramJobj.getString("language"))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null,Locale.forLanguageTag(paramJobj.getString("language"))));
                    } else {
                        if (isEdit == true) {
                            setValuesForAuditTrialForSO(so, paramJobj, oldsoMap, soDataMap, newAuditKey);
                        }

                        if (so.getContract() != null) {
                            throw new AccountingException(messageSource.getMessage("acc.field.LeaseOrdernumberEdit", null,Locale.forLanguageTag(paramJobj.getString("language"))));
                        }
                        soDataMap.put("id", soid);
                        
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("soid", soid);
                        requestParams.put(Constants.companyKey, companyid);
                        accSalesOrderDAOobj.deleteSalesOrdersBatchSerialDetails(requestParams); //dlete serial no and mapping
                        
                        if (extraCMPPreferences.isAvalaraIntegration()) {//In case of edit, if Avalara Integration is enabled, delete tax mapping from table 'TransactionDetailAvalaraTaxMapping'
                            deleteAvalaraTaxMappingForSO(so.getRows());
                        }
                        
                        accSalesOrderDAOobj.deleteSalesOrderDetails(soid, companyid, isLeaseFixedAsset,isConsignment,isJobWorkOrderReciever);
                        
                        KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
                        if(!extraCompanyPreferences.getCompany().getCountry().getID().equals(Constants.indian_country_id)){
                            //Delete Sales Order Term Map
                            HashMap<String, Object> termReqMap = new HashMap<String, Object>();
                            termReqMap.put("soid", soid);
                            accSalesOrderDAOobj.deleteSOTermMap(termReqMap);
                        }
                        
                        if (so.getMaintenance() != null && !so.getMaintenance().getId().equals(maintenanceId)) {  // if sales order which is being edited has maintenance number, then status of that maintenance number should be updated as open now
                            String maintenanceIdOfSelectedRecord = (so.getMaintenance() != null) ? so.getMaintenance().getId() : "";
                            accSalesOrderDAOobj.openMaintenance(soid, companyid, maintenanceIdOfSelectedRecord);
//                            String crmURL = paramJobj.optString(Constants.crmURL, "");
                            String crmURL = URLUtil.buildRestURL(Constants.crmURL);
                            crmURL = crmURL + "master/maintainceformstatus";
                            JSONObject newmaintenanceJObj = new JSONObject();
                            newmaintenanceJObj.put("maintainanceid", maintenanceIdOfSelectedRecord);
                            newmaintenanceJObj.put("maintainanceno", so.getMaintenance().getMaintenanceNumber());
                            newmaintenanceJObj.put("customerid", (so.getMaintenance().getCustomer() != null) ? so.getMaintenance().getCustomer().getID() : "");
                            newmaintenanceJObj.put("isClosed", false);
                            JSONArray newmaintenanceJObjArr = new JSONArray();
                            newmaintenanceJObjArr.put(newmaintenanceJObj);
                            JSONObject userData = new JSONObject();
                            userData.put("iscommit", true);
                            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
                            userData.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                            userData.put(Constants.companyKey, companyid);
                            userData.put("maintenanceStatusDetails", newmaintenanceJObjArr);
                            apiCallHandlerService.restPostMethod(crmURL, userData.toString());
//                            apiCallHandlerService.callApp(crmURL, userData, companyid, "" + Constants.Update_Maintenance_Status_To_CRM);
                        }
                    }

                    HashMap<String, Object> linkrequestParams = new HashMap<String, Object>();

                    /*
                     * Deleting Linking information of SO during Editing
                     */
                    linkrequestParams.put("soid", soid);
                    accSalesOrderDAOobj.deleteLinkingInformationOfSO(linkrequestParams);//Deleting linking information of PO 

                    /*
                     * Updating Isopen Flag=0 & linkflag=0 of CQ during Editing SO
                     */
                    if (!StringUtil.isNullOrEmpty(deletedLinkedDocumentID)) {
                        String[] deletedLinkedDocumentIDArr = deletedLinkedDocumentID.split(",");
                        for (int i = 0; i < deletedLinkedDocumentIDArr.length; i++) {
                            KwlReturnObject venresult = accountingHandlerDAOobj.getObject(Quotation.class.getName(), deletedLinkedDocumentIDArr[i]);
                            Quotation quotation = (Quotation) venresult.getEntityList().get(0);
                            if (quotation != null) {
                                linkrequestParams.put("isOpen", true);
                                linkrequestParams.put("quotation", quotation);
                                linkrequestParams.put("value", "0");
                                accSalesOrderDAOobj.updateCQLinkflag(linkrequestParams);
                                /*
                                 * Preparing audit trial message while unlinking
                                 * document through Edit
                                 */
                                if (i == 0) {
                                    unlinkMessage += " from the Customer Quotation(s) ";
                                }
                                if (unlinkMessage.indexOf(quotation.getQuotationNumber()) == -1) {
                                    unlinkMessage += quotation.getQuotationNumber() + ", ";
                                }
                            } else {
                                venresult = accSalesOrderDAOobj.getPurchaseOrderNumber(deletedLinkedDocumentIDArr[i]);
                                if (venresult != null) {
                                    String purchaseOrderNo = venresult.getEntityList().get(0).toString();
                                    /*
                                     * Preparing audit trial message while
                                     * unlinking document through Edit
                                     */
                                    if (i == 0) {
                                        unlinkMessage += " from the Purchase Order(s) ";
                                    }
                                    if (unlinkMessage.indexOf(purchaseOrderNo) == -1) {
                                        unlinkMessage += purchaseOrderNo + ", ";
                                    }
                                }
                            }
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(unlinkMessage) && unlinkMessage.endsWith(", ")) {
                        unlinkMessage = unlinkMessage.substring(0, unlinkMessage.length() - 2);
                    }
                }
                
                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this entry number can also be generated by a sequence format or not
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Sales_Order_ModuleId, entryNumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null,Locale.forLanguageTag(paramJobj.getString("language"))) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null,Locale.forLanguageTag(paramJobj.getString("language"))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null,Locale.forLanguageTag(paramJobj.getString("language"))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null,Locale.forLanguageTag(paramJobj.getString("language"))));
                        }
                    }
                }
            }
            
            DateFormat df = authHandler.getDateOnlyFormat();
            KwlReturnObject custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), CustomerId);
            Customer customer = (Customer) custresult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(soid)) {//Edit PO Case for updating address detail
                Map<String, Object> addressParams = new HashMap<String, Object>();
                String billingAddress = paramJobj.optString(Constants.BILLING_ADDRESS,null);
                if (!StringUtil.isNullOrEmpty(billingAddress)) {  //handling the cases when no address coming in edit case 
                    addressParams = AccountingAddressManager.getAddressParamsJson(paramJobj,false);
                } else {
                    addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(customer.getID(), companyid, accountingHandlerDAOobj);// addressParams = getCustomerDefaultAddressParams(customer,companyid);
                }
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soid);
                SalesOrder so = (SalesOrder) returnObject.getEntityList().get(0);
                addressParams.put("id", so.getBillingShippingAddresses() == null ? "" : so.getBillingShippingAddresses().getID());
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                soDataMap.put("billshipAddressid", bsa.getID());
            } else { //Other Cases for saving address detail
                boolean isDefaultAddress = paramJobj.optString("defaultAdress",null) != null ? Boolean.parseBoolean(paramJobj.getString("defaultAdress")) : false;
                Map<String, Object> addressParams = new HashMap<String, Object>();
                if (isDefaultAddress) {
                    addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(customer.getID(), companyid, accountingHandlerDAOobj);// addressParams = getCustomerDefaultAddressParams(customer,companyid);
                } else {
                    addressParams = AccountingAddressManager.getAddressParamsJson(paramJobj,false);
                }
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                soDataMap.put("billshipAddressid", bsa.getID());
            }
            /*
                In edit case of draft where auto-sequence no.is enable and not a save action then we have set entryno as empty to avoid duplication.
            */
            if(isDraft && isAutoSeqForEmptyDraft && !StringUtil.isNullOrEmpty(entryNumber)){
                entryNumber = "";
            }
            if(isDraft && !isSaveDraftRecord && !StringUtil.isNullOrEmpty(soid) && !sequenceformat.equals("NA") && StringUtil.isNullOrEmpty(entryNumber)){ //SDP-13927 : If Draft already having sequence no. then do not update it
                soDataMap.put("entrynumber", "");
            } else if (sequenceformat.equals("NA") || !StringUtil.isNullOrEmpty(soid)) {
                soDataMap.put("entrynumber", entryNumber);
            } else {
                soDataMap.put("entrynumber", "");
            }
            soDataMap.put("autogenerated", sequenceformat.equals("NA") ?  false : true);
            soDataMap.put(Constants.memo, paramJobj.optString("memo",null));
            soDataMap.put(Constants.posttext, paramJobj.optString("posttext",null));
            soDataMap.put("isOpeningBalanceOrder", isOpeningBalanceOrder);
            soDataMap.put("customerid", CustomerId);
            soDataMap.put("orderdate", df.parse(billdate));
            soDataMap.put(Constants.Checklocktransactiondate, billdate);//ERP-16800-Without parsing date
            soDataMap.put(Constants.duedate, StringUtil.isNullOrEmpty(paramJobj.optString("duedate",null))?null:df.parse(paramJobj.getString("duedate")));
            soDataMap.put(Constants.currencyKey, currencyid);
            soDataMap.put("isLeaseFixedAsset", isLeaseFixedAsset);
            soDataMap.put("isConsignment", isConsignment);
            soDataMap.put(Constants.isDraft, isDraft);
            soDataMap.put("isMRPSalesOrder", isMRPSalesOrder);
            if (isdropshipchecked) {
                soDataMap.put("isdropshipchecked", isdropshipchecked);
            }
            soDataMap.put("isJobWorkOrderReciever", isJobWorkOrderReciever);
            soDataMap.put("customerporefno", customerpoRefNo);
            soDataMap.put("profitMargin", paramJobj.optString("profitMargin",null));
            soDataMap.put("profitMarginPercent", paramJobj.optString("profitMarginPercent",null));
            soDataMap.put("formtypeid", paramJobj.optString("formtypeid","0"));
            soDataMap.put(Constants.isApplyTaxToTerms, paramJobj.optBoolean(Constants.isApplyTaxToTerms,false));
            
            if (isConsignment) {
                if (paramJobj.optString("todate",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("todate",null))) {
                    soDataMap.put("todate", df.parse(paramJobj.getString("todate")));
                }
                if (paramJobj.optString("fromdate",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("fromdate",null))) {
                    soDataMap.put("fromdate", df.parse(paramJobj.getString("fromdate")));
                }
                if (paramJobj.optString("requestWarehouse",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("requestWarehouse",null))) {
                    soDataMap.put("requestWarehouse", paramJobj.getString("requestWarehouse"));
                }
                if (paramJobj.optString("requestLocation",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("requestLocation",null))) {
                    soDataMap.put("requestLocation", paramJobj.getString("requestLocation"));
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("autoapproveflag",null))) {
                    soDataMap.put("autoapproveflag", StringUtil.getBoolean(paramJobj.getString("autoapproveflag")));
                }
                
            }
            soDataMap.put("externalCurrencyRate", externalCurrencyRate);
            
            soDataMap.put("gstIncluded", gstIncluded);
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("perdiscount",null))) {
                soDataMap.put("perDiscount", StringUtil.getBoolean(paramJobj.getString("perdiscount")));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("discount",null))) {
                soDataMap.put("discount", StringUtil.getDouble(paramJobj.getString("discount")));
            }
            if (paramJobj.optString("shipdate",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("shipdate",null))) {
                soDataMap.put("shipdate", df.parse(paramJobj.getString("shipdate")));
            }
            soDataMap.put(Constants.shipvia, paramJobj.optString("shipvia",null));
            soDataMap.put(Constants.fob, paramJobj.optString("fob",null));
            soDataMap.put("termid", credittermid);
            soDataMap.put("shipaddress", paramJobj.optString("shipaddress",null));
            soDataMap.put("billto", paramJobj.optString("billto",null));
            soDataMap.put("isfavourite", paramJobj.optString("isfavourite",null));
            soDataMap.put("salesPerson",salesPerson);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                soDataMap.put("costCenterId", costCenterId);
            }
            if (!StringUtil.isNullOrEmpty(shipLength)) {
                soDataMap.put("shipLength", shipLength);
            }
            if (!StringUtil.isNullOrEmpty(invoicetype)) {
                soDataMap.put("invoicetype", invoicetype);
            }
            if (!StringUtil.isNullOrEmpty(custWarehouse)) {
                soDataMap.put("custWarehouse", custWarehouse);
            }
            if (!StringUtil.isNullOrEmpty(movementtype)) {
                soDataMap.put("movementtype", movementtype);
            }
            soDataMap.put(Constants.companyKey, companyid);
            
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("maintenanceId",null))) {
                soDataMap.put("maintenanceId", maintenanceId);
            }
            
            soDataMap.put("isLinkedFromMaintenanceNumber", isLinkedFromMaintenanceNumber);
            
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("replacementId",null))) {
                soDataMap.put("IsReplacementSO", isLinkedFromReplacementNumber);
                
                ProductReplacement productReplacement = (ProductReplacement) kwlCommonTablesDAOObj.getClassObject(ProductReplacement.class.getName(), replacementId);
                if (productReplacement != null) {
                    Contract contract = productReplacement.getContract();
                    
                    if (contract != null) {
                        soDataMap.put("contractid", contract.getID());
                    }
                }
            } else if (!StringUtil.isNullOrEmpty(customerQuotationId)) {
                
                Quotation quotation = (Quotation) kwlCommonTablesDAOObj.getClassObject(Quotation.class.getName(), customerQuotationId);
                if (quotation != null && quotation.getContract() != null) {
                    Contract contract = quotation.getContract();
                    soDataMap.put("contractid", contract.getID());
                    soDataMap.put("IsReplacementSO", true);// contract will be in quotation only and only if quotation is created by linking with Product Replacement
                }
            }
            
            soDataMap.put("createdby", createdby);
            soDataMap.put("modifiedby", modifiedby);
            soDataMap.put("createdon", createdon);
            soDataMap.put("updatedon", updatedon);
            soDataMap.put("islockQuantityflag", islockQuantityflag);// lock quantity true or not

            double subTotal = 0, taxAmt = 0, totalTermAmt = 0, totalAmt = 0, totalRowDiscount = 0, totalAmountinbase = 0;
            JSONArray jArr = new JSONArray(paramJobj.getString(Constants.detail));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);

                double qrate = authHandler.roundUnitPrice(jobj.getDouble("rate"), companyid);
                double quantity = authHandler.roundQuantity(jobj.getDouble("quantity"), companyid);
                if (gstIncluded) {
                    qrate = authHandler.roundUnitPrice(jobj.optDouble("rateIncludingGst",0.0), companyid);
                }

                double quotationPrice = authHandler.round(quantity * qrate, companyid);
                double discountQD = authHandler.round(jobj.optDouble("prdiscount", 0), companyid);
                double discountPerRow = 0;

                if (jobj.optInt("discountispercent", 0) == 1) {
                    discountPerRow = authHandler.round((quotationPrice * discountQD / 100), companyid);
                } else {
                    discountPerRow = discountQD;
                }

                totalRowDiscount += discountPerRow;

            }
            totalRowDiscount = authHandler.round(totalRowDiscount, companyid);
            soDataMap.put("totallineleveldiscount",totalRowDiscount);
            
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("subTotal",null))) {
                subTotal = Double.parseDouble(paramJobj.getString("subTotal"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxamount",null))) {
                taxAmt = Double.parseDouble(paramJobj.getString("taxamount"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("invoicetermsmap",null))) {
                JSONArray termDetailsJArr = new JSONArray(paramJobj.getString("invoicetermsmap"));
                for (int cnt = 0; cnt < termDetailsJArr.length(); cnt++) {
                    JSONObject temp = termDetailsJArr.getJSONObject(cnt);
                    if (temp != null) {
                        double termAmount = Double.parseDouble(temp.getString("termamount"));
                        totalTermAmt += termAmount;
                    }
                }
            }
            totalAmt = subTotal + taxAmt + totalTermAmt;
                      
            double roundingadjustmentAmount = 0.0, roundingadjustmentAmountinbase = 0.0;
            String roundingAdjustmentAccountID = "";
            String columnPref = extraCMPPreferences.getColumnPref();
            if (!StringUtil.isNullOrEmpty(columnPref)) {
                JSONObject prefObj = new JSONObject(columnPref);
                roundingAdjustmentAccountID = prefObj.optString(Constants.RoundingAdjustmentAccountID, "");
            }

            if (isRoundingAdjustmentApplied && !StringUtil.isNullOrEmpty(roundingAdjustmentAccountID)) {
                double totalInvAmountAfterRound = Math.round(totalAmt);
                roundingadjustmentAmount = authHandler.round(totalInvAmountAfterRound - totalAmt, companyid);
                if (roundingadjustmentAmount != 0) {
                    totalAmt = totalInvAmountAfterRound;//Now rounded value becomes total invoice amount
                    soDataMap.put(Constants.roundingadjustmentamount, roundingadjustmentAmount);
                    soDataMap.put(Constants.roundingadjustmentamountinbase, roundingadjustmentAmount);

                    String globalcurrency = paramJobj.getString(Constants.globalCurrencyKey);
                    if (!globalcurrency.equalsIgnoreCase(currencyid)) {
                        HashMap<String, Object> roundingRequestParams = new HashMap<String, Object>();
                        roundingRequestParams.put("companyid", companyid);
                        roundingRequestParams.put("gcurrencyid", (paramJobj.optString(Constants.currencyKey, null) == null ? paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.getString(Constants.currencyKey)));
                        KwlReturnObject baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(roundingRequestParams, roundingadjustmentAmount, currencyid, df.parse(paramJobj.optString("billdate")), externalCurrencyRate);
                        roundingadjustmentAmountinbase = authHandler.round((Double) baseAmt.getEntityList().get(0), companyid);
                        soDataMap.put(Constants.roundingadjustmentamountinbase, roundingadjustmentAmountinbase);
                    }
                }
            }
            soDataMap.put(Constants.IsRoundingAdjustmentApplied, isRoundingAdjustmentApplied);
            soDataMap.put("totalamount", totalAmt);
            
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            filterRequestParams.put(Constants.companyKey, companyid);
            filterRequestParams.put(Constants.globalCurrencyKey,paramJobj.getString(Constants.globalCurrencyKey));
            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalAmt, currencyid, df.parse(billdate), externalCurrencyRate);
            totalAmountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
            soDataMap.put("totalamountinbase", totalAmountinbase);

            KwlReturnObject descbAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalRowDiscount, currencyid, df.parse(billdate), externalCurrencyRate);
            double descountinBase = authHandler.round((Double) descbAmtTax.getEntityList().get(0), companyid);

            soDataMap.put("discountinbase", descountinBase);
            
            if (taxid != null && !taxid.isEmpty()) {
                Tax tax = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxid);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                }
                soDataMap.put("taxid", taxid);
            } else {
                soDataMap.put("taxid", taxid);     // Put taxid as null if the SO doesnt have any total tax included. (To avoid problem while editing PO)
            }
            if(extraCMPPreferences.isIsNewGST()){
                if(!StringUtil.isNullOrEmpty(paramJobj.optString("formtypeid"))){
                    soDataMap.put("formtypeid", paramJobj.optString("formtypeid"));
                }
                /**
                 * ERP-32829 
                 */
                soDataMap.put("gstapplicable", paramJobj.optBoolean("GSTApplicable"));
                soDataMap.put(Constants.RCMApplicable, RCMApplicable);
                soDataMap.put(Constants.isMerchantExporter, paramJobj.optBoolean(Constants.isMerchantExporter, false));
            }
            String maintenanceid = "";
            String salesOrderTerms = paramJobj.optString("invoicetermsmap",null);
            if (StringUtil.isAsciiString(salesOrderTerms)) {
                if (new JSONArray(salesOrderTerms).length() > 0) {
                    soDataMap.put(Constants.termsincludegst, Boolean.parseBoolean(paramJobj.optString(Constants.termsincludegst)));
                    }
            }
            soDataMap.put(Constants.generatedSource,  (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.generatedSource, null))) ? Integer.parseInt(paramJobj.optString(Constants.generatedSource,Constants.RECORD_WEB_Application)) :null);

            String disablePOForSO = paramJobj.optString("blockSOPO", "");
            boolean isBlockDocument = false;
            if (!StringUtil.isNullOrEmpty(disablePOForSO) && disablePOForSO.equalsIgnoreCase("on")) {
                isBlockDocument = true;
            }
            
            soDataMap.put("isLinkedPOBlocked", isBlockDocument);
            
            soDataMap.put("isEdit", isEdit);
            soDataMap.put("islockQuantity", islockQuantity);
            soDataMap.put("isLinkedTransaction", isLinkedTransaction);
            soDataMap.put("isCopy", isCopy);
            KwlReturnObject soresult = accSalesOrderDAOobj.saveSalesOrder(soDataMap);
            salesOrder = (SalesOrder) soresult.getEntityList().get(0);
            // Save PO Custom Data
            /**
             * Save GST History Customer/Vendor data.
             */
            if (salesOrder.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                paramJobj.put("docid", salesOrder.getID());
                paramJobj.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                fieldDataManagercntrl.createRequestMapToSaveDocHistory(paramJobj);
            }
            if (!StringUtil.isNullOrEmpty(customfield)) {
                HashMap<String, Object> SOMap = new HashMap<String, Object>();
                JSONArray jcustomarray = new JSONArray(customfield);

                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "SalesOrder");
                customrequestParams.put("moduleprimarykey", "SoID");
                customrequestParams.put("modulerecid", salesOrder.getID());
                customrequestParams.put(Constants.moduleid, isConsignment ? Constants.Acc_ConsignmentRequest_ModuleId : isLeaseFixedAsset?Constants.Acc_Lease_Order_ModuleId:isJobWorkOrderReciever?Constants.VENDOR_JOB_WORKORDER_MODULEID:Constants.Acc_Sales_Order_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                SOMap.put("id", salesOrder.getID());
                SOMap.put("isOpeningBalanceOrder", salesOrder.isIsOpeningBalanceSO());
                SOMap.put(Constants.companyKey, companyid);
                SOMap.put("orderdate", df.parse(billdate));
                customrequestParams.put("customdataclasspath", Constants.Acc_SalesOrder_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (!StringUtil.isNullOrEmpty(costCenterId)) {
                    SOMap.put("costCenterId", costCenterId);
                }
                SOMap.put("isEdit", isEdit);
                SOMap.put("islockQuantity", islockQuantity);
                SOMap.put("isLinkedTransaction", isLinkedTransaction);
                SOMap.put("isCopy", isCopy);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    SOMap.put("salesordercustomdataref", salesOrder.getID());
                    accSalesOrderDAOobj.saveSalesOrder(SOMap);
                }
            }
            
            salesOrderId = salesOrder.getID();
            soDataMap.put("id", salesOrder.getID());
            //Saving Line Item Details
            List rowDetails = saveSalesOrderRows(paramJobj, salesOrder, companyid, currencyid, GlobalParams, externalCurrencyRate);
            String billid = salesOrder.getID();
            String pendingApprovalFlagnew = String.valueOf(pendingApprovalFlag);
            String billno = salesOrder.getSalesOrderNumber();
            newList.add(pendingApprovalFlagnew);
            newList.add(billid);
            newList.add(billno);

            Set<SalesOrderDetail> sodetails = null;
            sodetails = (HashSet<SalesOrderDetail>) rowDetails.get(0);
            JSONArray productDiscountJArr=new JSONArray();
            for (SalesOrderDetail soDetail : sodetails) {
                String productId = soDetail.getProduct().getID();
                double discountVal = soDetail.getDiscount();
                int isDiscountPercent = soDetail.getDiscountispercent();
                if(isDiscountPercent==1){
                    discountVal = (soDetail.getQuantity()*soDetail.getRate())*(discountVal/100);
                }
                KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, df.parse(billdate), externalCurrencyRate);
                double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                discAmountinBase = authHandler.round(discAmountinBase, companyid);

                JSONObject productDiscountObj=new JSONObject();
                productDiscountObj.put("productId", productId);
                productDiscountObj.put("discountAmount", discAmountinBase);
                productDiscountJArr.put(productDiscountObj);
            }
            salesOrder.setIstemplate(istemplate);

             String linkedDocuments="";
             String linkMode = paramJobj.optString("fromLinkCombo",null);
             if (!StringUtil.isNullOrEmpty(linkMode) && (linkMode.equalsIgnoreCase("Customer Quotation") || linkMode.equalsIgnoreCase("Lease Quotation"))) {
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("linkNumber",null))) { //When adding Quotation link for Sales Order update link flag(2) in Quotation.
                    String[] linkNumbers =paramJobj.getString("linkNumber").split(",");
                    if (linkNumbers.length > 0 && !isLinkedFromReplacementNumber) {
                        for (int i = 0; i < linkNumbers.length; i++) {
                            if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {
                                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(Quotation.class.getName(), linkNumbers[i]);
                                Quotation quotation = (Quotation) rdresult.getEntityList().get(0);
                                updateOpenStatusFlagForSO(linkNumbers[i]);
                                if (quotation.getMaintenance() != null) {   //get maintenance quotation
                                    maintenanceid = quotation.getMaintenance().toString();
                                }
                                
                                /*
                                 * saving linking informaion of Customer
                                 * Quotation while linking with Sales Order
                                 */

                                HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                                requestParamsLinking.put("linkeddocid", salesOrder.getID());
                                requestParamsLinking.put("docid", linkNumbers[i]);
                                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                                requestParamsLinking.put("linkeddocno", entryNumber);
                                requestParamsLinking.put("sourceflag", 0);
                                KwlReturnObject result3 = accSalesOrderDAOobj.saveQuotationLinking(requestParamsLinking);


                                /*
                                 * saving linking informaion of Sales Order
                                 * while linking with Customer Quotation
                                 */
                                requestParamsLinking.put("linkeddocid", linkNumbers[i]);
                                requestParamsLinking.put("docid", salesOrder.getID());
                                requestParamsLinking.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                                requestParamsLinking.put("linkeddocno", quotation.getquotationNumber());
                                requestParamsLinking.put("sourceflag", 1);
                                result3 = accSalesOrderDAOobj.saveSalesOrderLinking(requestParamsLinking);
                                linkedDocuments +=quotation.getquotationNumber()+ " ,";

                            }
                        }
                        
                    }
                }
                 if (linkedDocuments.endsWith(",")) {
                     linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                 }
            } else if (!StringUtil.isNullOrEmpty(linkMode) && (linkMode.equalsIgnoreCase(Constants.ACC_PURCHASE_ORDER))) {
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("linkNumber",null))) {
                    String[] linkNumbers =paramJobj.getString("linkNumber").split(",");
                    if (linkNumbers.length > 0 && !isLinkedFromReplacementNumber) {
                        for (int i = 0; i < linkNumbers.length; i++) {
                            if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {


                                /*
                                 * saving linking informaion of Purchase Order
                                 * Quotation while linking with Sales Order
                                 */

                                HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                                requestParamsLinking.put("linkeddocid", salesOrder.getID());
                                requestParamsLinking.put("docid", linkNumbers[i]);
                                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                                requestParamsLinking.put("linkeddocno", entryNumber);
                                requestParamsLinking.put("sourceflag", 0);
                                KwlReturnObject result3 = accSalesOrderDAOobj.updateEntryInPurchaseOrderLinkingTable(requestParamsLinking);


                                /*
                                 * saving linking informaion of Sales Order
                                 * while linking with Purchase Order
                                 */
                                
                                result3 = accSalesOrderDAOobj.getPurchaseOrderNumber(linkNumbers[i]);
                                String purchaseOrderNo = result3.getEntityList().get(0).toString();
                                requestParamsLinking.put("linkeddocid", linkNumbers[i]);
                                requestParamsLinking.put("docid", salesOrder.getID());
                                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Purchase_Order_ModuleId);
                                requestParamsLinking.put("linkeddocno", purchaseOrderNo);
                                requestParamsLinking.put("sourceflag", 1);
                                result3 = accSalesOrderDAOobj.saveSalesOrderLinking(requestParamsLinking);
                               linkedDocuments +=purchaseOrderNo+ " ,"; 
                               
                                /**
                                 * if the blockSOPO check is enabled on UI side
                                 * then updating the value of disabledpoforso = 'T' of linked PO ERP-35541
                                 */
                                String purchaseOrderID = linkNumbers[i];
                                String message = "";
                                String auditStatus = "";
                                JSONObject requestparams = new JSONObject();
                                if (isBlockDocument) {
                                    requestparams.put("status", "open");
                                    auditStatus = " Blocked ";
                                    message = messageSource.getMessage("acc.wtfTrans.po", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " " + purchaseOrderNo + " " + messageSource.getMessage("acc.po.hasbeenblockedforSalesOrder", null, Locale.forLanguageTag(paramJobj.getString("language")));
                                } else {
                                    requestparams.put("status", "closed");
                                    auditStatus = "Unblocked";
                                    message = messageSource.getMessage("acc.wtfTrans.po", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " " + purchaseOrderNo + " " + messageSource.getMessage("acc.po.hasbeenunblockedforSalesOrder", null, Locale.forLanguageTag(paramJobj.getString("language")));
                                }
                                requestparams.put("purchaseOrderID", purchaseOrderID);
                                requestparams.put("purchaseOrderNo", purchaseOrderNo);
                                int updatedRecordCnt = accSalesOrderDAOobj.savePurchaseOrderStatusForSO(requestparams);
                                if (updatedRecordCnt > 0) {
                                    Map<String, Object> auditRequestParams = new HashMap<>();
                                    auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                                    auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                                    auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                                    auditTrailObj.insertAuditLog(AuditAction.PURCHASE_ORDER_BLOCKED_UNBLOCKED, "User " + paramJobj.getString(Constants.userfullname) + " has " + auditStatus + " Purchase Order " + purchaseOrderNo + " For Sales Order ", auditRequestParams, purchaseOrderID);
                                }
                            }
                        }

                    }
                }
                 if (linkedDocuments.endsWith(",")) {
                     linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                 }
            }
             
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("linkedAdvancePaymentId", null))) { //When adding Quotation link for Sales Order update link flag(2) in Quotation.
                String[] linkNumbers = paramJobj.getString("linkedAdvancePaymentId").split(",");
                String[] linkDocNos = paramJobj.optString("linkedAdvancePaymentNo", "").split(",");
                int linkDocNosLength = linkDocNos.length;
                if (linkNumbers.length > 0) {
                    for (int i = 0; i < linkNumbers.length; i++) {
                        if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {
//                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(Quotation.class.getName(), linkNumbers[i]);
//                            Quotation quotation = (Quotation) rdresult.getEntityList().get(0);
//                            updateOpenStatusFlagForSO(linkNumbers[i]);
//                            if (quotation.getMaintenance() != null) {   //get maintenance quotation
//                                maintenanceid = quotation.getMaintenance().toString();
//                            }
//
//                            /*
//                             * saving linking informaion of Customer Quotation
//                             * while linking with Sales Order
//                             */
//
                            HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
//                            requestParamsLinking.put("linkeddocid", salesOrder.getID());
//                            requestParamsLinking.put("docid", linkNumbers[i]);
//                            requestParamsLinking.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
//                            requestParamsLinking.put("linkeddocno", entryNumber);
//                            requestParamsLinking.put("sourceflag", 0);
//                            KwlReturnObject result3 = accSalesOrderDAOobj.saveQuotationLinking(requestParamsLinking);


                            /*
                             * saving linking informaion of Sales Order while
                             * linking with Customer Quotation
                             */
                            requestParamsLinking.put("linkeddocid", linkNumbers[i]);
                            requestParamsLinking.put("docid", salesOrder.getID());
                            requestParamsLinking.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
//                            requestParamsLinking.put("linkeddocno", quotation.getquotationNumber());
                            requestParamsLinking.put("linkeddocno", i < linkDocNosLength ? linkDocNos[i] : "");
                            requestParamsLinking.put("sourceflag", 1);
                            accSalesOrderDAOobj.saveSalesOrderLinking(requestParamsLinking);
//                            linkedDocuments += quotation.getquotationNumber() + " ,";
                            linkedDocuments += (i < linkDocNosLength ? linkDocNos[i] : "") + ",";
                        }
                    }
                    if (linkedDocuments.endsWith(",")) {
                        linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                    }
                }
            }
   
             if (isEdit == true) { //For Audit Trial-ERP-14034
                //ERP-14034 
                DateFormat sdf = authHandler.getUserDateFormatterJson(paramJobj);
                int moduleid = Constants.Acc_Sales_Order_ModuleId;
                if (salesOrder.getShipdate() != null) {
                    soDataMap.put("AuditShipDate", sdf.format(salesOrder.getShipdate()));  //New Ship Date
                } else {
                    soDataMap.put("AuditShipDate", "");
                }
                if (salesOrder.getDueDate() != null) {
                    soDataMap.put("AuditDueDate", sdf.format(salesOrder.getDueDate()));  //New Due Date
                } else {
                    soDataMap.put("AuditDueDate", "");
                }
                if (salesOrder.getOrderDate() != null) {
                    soDataMap.put("AuditOrderdate", sdf.format(salesOrder.getOrderDate()));  //New Order Date
                } else {
                    soDataMap.put("AuditOrderdate", "");
                }

                auditMessage = AccountingManager.BuildAuditTrialMessage(soDataMap, oldsoMap, moduleid, newAuditKey);
            }
             
            // Check for approval rules and apply rules if available
            String butPendingForApproval = "";
            double subTotalAmount = 0;
            double taxAmount = 0;
            double totalTermAmount = 0;
            double totalAmount = 0;
            double totalProfitMargin = 0;
            double totalProfitMarginPerc = 0;
            HashMap<String, Object> soApproveMap = new HashMap<String, Object>();
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("subTotal",null))) {
                subTotalAmount = Double.parseDouble(paramJobj.getString("subTotal"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxamount",null))) {
                taxAmount = Double.parseDouble(paramJobj.getString("taxamount"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("invoicetermsmap",null))) {
                JSONArray termDetailsJArr = new JSONArray(paramJobj.getString("invoicetermsmap"));
                for (int cnt = 0; cnt < termDetailsJArr.length(); cnt++) {
                    JSONObject temp = termDetailsJArr.getJSONObject(cnt);
                    if (temp != null) {
                        double termAmount = Double.parseDouble(temp.getString("termamount"));
                        totalTermAmount += termAmount;
                    }
                }
            }
            totalAmount = subTotalAmount + taxAmount + totalTermAmount;
            KwlReturnObject tAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, totalAmount, currencyid, df.parse(billdate), externalCurrencyRate);
            double totalAmountinBase = 0;
            totalAmountinBase = (Double) tAmount.getEntityList().get(0);
            totalProfitMargin = salesOrder.getTotalProfitMargin();
            totalProfitMarginPerc = salesOrder.getTotalProfitMarginPercent();
            int approvalStatusLevel = 11;
            int level = (isEdit && !isCopy) ? 0 : salesOrder.getApprovestatuslevel();
            
            /**
             * Checking whether 'Include Current Transaction Amount for Credit
             * Limit Check' of Customer So Credit Control is enabled in Company
             * Preferences. If yes then Checking amount due of customer is
             * greater than credit limit of customer if yes then sending SO for
             * approval.
             */
            double customerCreditLimit = customer.getCreditlimit();
            double salesOrderAmount=salesOrder.getTotalamountinbase();
            boolean isLimitExceeding=false;
            double amountDueOfCustomer = paramJobj.optDouble("amountDueOfCustomer", 0.0);
            if(extraCMPPreferences.isIncludeAmountInLimitSO()){
                amountDueOfCustomer=paramJobj.optDouble("totalAmountDueOfCustomer", 0.0);
            }
            if (paramJobj.has("amountDueOfCustomer") && (amountDueOfCustomer > customerCreditLimit) && extraCMPPreferences.isIncludeAmountInLimitSO()) {
                isLimitExceeding = true;
            }
            soApproveMap.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
            soApproveMap.put("level", level);
            soApproveMap.put("totalAmount", String.valueOf(authHandler.round(totalAmountinBase, companyid)));
            soApproveMap.put("totalProfitMargin", totalProfitMargin);
            soApproveMap.put("totalProfitMarginPerc", totalProfitMarginPerc);
            soApproveMap.put("currentUser", currentUser);
            soApproveMap.put("fromCreate", true);
            soApproveMap.put("productDiscountMapList", productDiscountJArr);
            soApproveMap.put("isLimitExceeding", isLimitExceeding);             //ERM-396
            soApproveMap.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
            soApproveMap.put(Constants.PAGE_URL, paramJobj.optString(Constants.PAGE_URL));
            List approvedlevel = null;
            boolean ismailApplicable=false;
            
                            
            boolean sendPendingDocumentsToNextLevel = false;
            boolean pendingMessage = true;
                                    
             JSONObject columnprefObj = new JSONObject();
            if (!StringUtil.isNullOrEmpty(columnPref)) {
                 columnprefObj = new JSONObject(columnPref);
            }

            if (columnprefObj.has("sendPendingDocumentsToNextLevel") && columnprefObj.get("sendPendingDocumentsToNextLevel") != null && (Boolean) columnprefObj.get("sendPendingDocumentsToNextLevel") != false) {
                sendPendingDocumentsToNextLevel = true;
            }
            
            /*------If pending document is Edited & Check is activated from system preferences
             *---Then Edit will work same as while approving document
             */
            if (isEditedPendingDocument) {
                level = salesOrder.getApprovestatuslevel();
                soApproveMap.put("fromCreate", false);
                soApproveMap.put("documentLevel", level);
                if (sendPendingDocumentsToNextLevel) {

                    ismailApplicable = true;
                    soApproveMap.put("level", level);
                    pendingMessage = false;

                } else {//Sending Parameter in approve function if "Send approval documents to next level" check is disabled from system preferences
                    soApproveMap.put("isEditedPendingDocumentWithCheckOff", true);
                }
            }
            
            if (!(isLeaseFixedAsset || isConsignment || isDraft)) { // !isDraft = if you are saving sales order as draft then no need to apply aprroval rule because draft means it does not exist in the system.
                approvedlevel = approveSalesOrder(salesOrder, soApproveMap, ismailApplicable);
                     
              approvalStatusLevel = (Integer)approvedlevel.get(0);
              mailParams = (List)approvedlevel.get(1);
            } else {
                salesOrder.setApprovestatuslevel(11);
            }
            
                                    
            List approvalHistoryList = null;
            String roleName = "";
            boolean isAuthorityToApprove = false;


            /*-----Block is executed when Edited pending Document & Check "Send pending documents to next level" is activated-------*/
            /*------If pending document is Edited & Check is activated from system preferences
             *---Then Edit will work same as while approving document
             */
            if (isEditedPendingDocument && sendPendingDocumentsToNextLevel) {

                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

                /*---Document will approve as approval level -----  */
                if (approvalStatusLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {

                    HashMap emailMap = new HashMap();

                    String userName = paramJobj.optString(Constants.username, null);
                    emailMap.put("userName", userName);
                    KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                    Company company = (Company) returnObject.getEntityList().get(0);
                    emailMap.put("company", company);
                    emailMap.put("salesOrder", salesOrder);
                    emailMap.put("baseUrl", paramJobj.optString("baseUrl", null));
                    emailMap.put("preferences", preferences);
                    emailMap.put("ApproveMap", soApproveMap);

                    sendApprovalMailIfAllowedFromSystemPreferences(emailMap);

                }

                /*--------Save Approval history Code--------  */
                if (approvalStatusLevel != Constants.NoAuthorityToApprove) {

                    HashMap approvalHistoryMap = new HashMap();

                    KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                    Company company = (Company) returnObject.getEntityList().get(0);
                    approvalHistoryMap.put("company", company);
                    approvalHistoryMap.put("salesOrder", salesOrder);
                    String userid = paramJobj.optString(Constants.userid, null);
                    approvalHistoryMap.put("userid", userid);

                    approvalHistoryList = saveApprovalHistory(approvalHistoryMap);
                    roleName = approvalHistoryList != null ? approvalHistoryList.get(0).toString() : "";
                    isAuthorityToApprove = true;

                } else {
                    /*----If User have no authority to approve------  */
                    isAuthorityToApprove = false;
                }
            }
            
            
            if (approvalStatusLevel != 11 && pendingMessage) {
                butPendingForApproval = messageSource.getMessage("acc.field.butpendingforApproval", null,Locale.forLanguageTag(paramJobj.getString("language")));
            }
            
            /*-------If  "Send pending documents to next level" check is OFF and User have no authority to approve--------- */
            if (isEditedPendingDocument && !sendPendingDocumentsToNextLevel && approvalStatusLevel == Constants.NoAuthorityToApprove) {
                isAuthorityToApprove = false;
                butPendingForApproval = "";
            }
            
            newSOList.add(salesOrder);
            newSOList.add(auditMessage);
            newSOList.add(totalAmount);
            newSOList.add(approvalStatusLevel);
            newSOList.add(butPendingForApproval);
            newSOList.add(mailParams);
            newSOList.add(linkedDocuments);
            newSOList.add(unlinkMessage);
            newSOList.add(roleName);
            newSOList.add(isAuthorityToApprove);
            newSOList.add(sendPendingDocumentsToNextLevel);
             
            //Save record as template
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("templatename",null)) && (istemplate == 1 || istemplate == 2)) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                String moduletemplateid = paramJobj.optString("moduletemplateid");
                hashMap.put("templatename", paramJobj.optString("templatename",null));
                 if (!StringUtil.isNullOrEmpty(moduletemplateid)) {
                    hashMap.put("moduletemplateid", moduletemplateid);
                }  
                hashMap.put("companyunitid", paramJobj.optString("companyunitid",null));
                hashMap.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                hashMap.put("modulerecordid", salesOrder.getID());
                hashMap.put(Constants.companyKey, companyid);
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("populateproducttemplate", null)) && paramJobj.optString("populateproducttemplate").equalsIgnoreCase("on")) {
                    hashMap.put("populateproducttemplate", paramJobj.optBoolean("populateproducttemplate", true));
                } else {
                    hashMap.put("populateproducttemplate", false);
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("populatecustomertemplate", null)) && paramJobj.optString("populatecustomertemplate").equalsIgnoreCase("on")) {
                    hashMap.put("populatecustomertemplate", paramJobj.optBoolean("populatecustomertemplate", true));
                } else {
                    hashMap.put("populatecustomertemplate", false);
                }
                if(!StringUtil.isNullOrEmpty(paramJobj.optString("companyunitid",null))){
                    hashMap.put("companyunitid", paramJobj.getString("companyunitid")); // Added Unit ID if it is present in request
                }
                 /**
                 * checks the template name is already exist in create and edit template case
                 */
                KwlReturnObject result = accountingHandlerDAOobj.getModuleTemplateForTemplatename(hashMap);
                int nocount = result.getRecordTotalCount();
                if (nocount > 0) {
                    throw new AccountingException(messageSource.getMessage("acc.tmp.templateNameAlreadyExists", null,Locale.forLanguageTag(paramJobj.getString("language"))));
                }
                
                accountingHandlerDAOobj.saveModuleTemplate(hashMap);
            }
            
            if (StringUtil.isAsciiString(salesOrderTerms)) {
                mapInvoiceTerms(salesOrderTerms, salesOrder.getID(), paramJobj.getString(Constants.useridKey), false);
            }
            // Update Maintenance Status and call CRM APP for updating status of maintenance Number
            if (!StringUtil.isNullOrEmpty(maintenanceid)) {
                maintenanceId = maintenanceid;
            }
            if ((isLinkedFromMaintenanceNumber && !StringUtil.isNullOrEmpty(maintenanceId)) || !StringUtil.isNullOrEmpty(maintenanceid)) {
                HashMap<String, Object> maintenanceMap = new HashMap<String, Object>();
                maintenanceMap.put("companyId", companyid);
                maintenanceMap.put("maintenanceId", maintenanceId);
                maintenanceMap.put("isClosed", true);
                maintenanceMap.put("customerId",CustomerId);
                KwlReturnObject maintenanceObj = accSalesOrderDAOobj.updateProductMaintenance(maintenanceMap);
                
                Maintenance maintenance = (Maintenance) maintenanceObj.getEntityList().get(0);
                
                JSONObject maintenanceJObj = new JSONObject();
                maintenanceJObj.put("maintainanceid", maintenance.getId());
                maintenanceJObj.put("maintainanceno", maintenance.getMaintenanceNumber());
                maintenanceJObj.put("customerid", (maintenance.getCustomer() != null) ? maintenance.getCustomer().getID() : "");
                maintenanceJObj.put("isClosed", maintenance.isClosed());
                
                JSONArray maintenanceJObjArr = new JSONArray();
                maintenanceJObjArr.put(maintenanceJObj);

                JSONObject userData = new JSONObject();
                userData.put("iscommit", true);
                userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
                userData.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                userData.put(Constants.companyKey, companyid);
                userData.put("maintenanceStatusDetails", maintenanceJObjArr);

                String crmURL = URLUtil.buildRestURL(Constants.crmURL);
                crmURL = crmURL + "master/maintainceformstatus";                
                apiCallHandlerService.restPostMethod(crmURL, userData.toString());
            }
             String moduleName = Constants.SALESORDER;
            if (isLeaseFixedAsset) {
                moduleName = Constants.moduleID_NameMap.get(Acc_Lease_Order_ModuleId);
            }
            if (isConsignment) {
                moduleName = Constants.moduleID_NameMap.get(Acc_ConsignmentRequest_ModuleId);
            }
            //Send Mail when Sales Order  is generated or modified.
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), paramJobj.getString(Constants.companyKey));
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (documentEmailSettings != null) {
                boolean sendmail = false;
                boolean isEditMail = false;
                if (StringUtil.isNullOrEmpty(soid)) {
                    if (isLeaseFixedAsset && documentEmailSettings.isLeaseOrderGenerationMail()) {
                        sendmail = true;
                    } else if (documentEmailSettings.isSalesOrderGenerationMail()) {
                        sendmail = true;
                    }
                } else {
                    isEditMail = true;
                    if (isLeaseFixedAsset && documentEmailSettings.isLeaseOrderUpdationMail()) {
                        sendmail = true;
                    } else if (documentEmailSettings.isSalesOrderUpdationMail()) {
                        sendmail = true;
                    }
                }
                if (sendmail) {//if allow to send alert mail when option selected in companypreferences
                    String userMailId="",userName="",currentUserid="";
                    String createdByEmail = "";
                    String createdById = "";
                    HashMap<String, Object> requestParams= AccountingManager.getEmailNotificationParamsJson(paramJobj);
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
                        if (salesOrder != null && salesOrder.getCreatedby() != null) {
                            createdByEmail = salesOrder.getCreatedby().getEmailID();
                            createdById = salesOrder.getCreatedby().getUserID();
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
                    String vqNumber = entryNumber;
                    accountingHandlerDAOobj.sendSaveTransactionEmails(vqNumber, moduleName, tomailids, userName, isEditMail, companyid);
                }
            }
           
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveSalesOrder : " + ex.getMessage(), ex);
        }
        return newSOList;
    }
   
    private void deleteAvalaraTaxMappingForSO(Set<SalesOrderDetail> soDetailSet) throws JSONException, ServiceException {
        List soDetailIDsList = new ArrayList<String>();
        for (SalesOrderDetail sod : soDetailSet) {
            soDetailIDsList.add(sod.getID());
        }
        if (!soDetailIDsList.isEmpty()) {
            //to create a comma separated string of SalesOrderDetail IDs for 'IN' subquery
            String soDetailIDsStr = org.springframework.util.StringUtils.collectionToDelimitedString(soDetailIDsList, ",", "'", "'");
            JSONObject avalaraTaxDeleteJobj = new JSONObject();
            avalaraTaxDeleteJobj.put(IntegrationConstants.parentRecordID, soDetailIDsStr);
            integrationCommonService.deleteTransactionDetailTaxMapping(avalaraTaxDeleteJobj);
        }
    }
    
     /**
     * Save temporary saved attachment files mapping in permanent table
     * @param jsonObj
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject saveDocuments(JSONObject jsonObj) throws ServiceException{
        List list = new ArrayList();
        try{
            String savedFilesMappingId = jsonObj.optString("savedFilesMappingId", "");
            String docId = jsonObj.optString("docId", "");
            String companyid = jsonObj.optString("companyid", "");
            
            Map<String,Object> fileMap = new HashMap<>();
            fileMap.put("id", savedFilesMappingId);
            fileMap.put("companyid", companyid);
            //Get temporary save attachments mapping
            KwlReturnObject mappedFilesResult = accSalesOrderDAOobj.getMappedFilesResult(fileMap);
            List mappedFiles = mappedFilesResult.getEntityList();
            Iterator itr = mappedFiles.iterator();
            KwlReturnObject objectResult = null;
            //Get company object
            objectResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) objectResult.getEntityList().get(0);
            //Save all temporary saved documents related to invoice in permanent table
            while(itr.hasNext()){
                Object[] row = (Object[])itr.next();
                String recordId = (String) row[0];
                String documentId = (String) row[1];
                //Get Invoice Document
                objectResult = accountingHandlerDAOobj.getObject(InvoiceDocuments.class.getName(), documentId);
                InvoiceDocuments document = (InvoiceDocuments) objectResult.getEntityList().get(0);
                //Create Invoice document mapping object for linking document to invoice
                InvoiceDocumentCompMap invoiceDocumentmapping = new InvoiceDocumentCompMap();
                //Set documentid, document and company to Invoice Document Mapping table
                invoiceDocumentmapping.setInvoiceID(docId);
                invoiceDocumentmapping.setCompany(company);
                invoiceDocumentmapping.setDocument(document);
                accSalesOrderDAOobj.SaveUpdateObject(invoiceDocumentmapping);
                list.add(invoiceDocumentmapping);
            }
            //Delete temporary saved document details from table
            accSalesOrderDAOobj.deleteTemporaryMappedFiles(savedFilesMappingId, companyid);
        } catch(Exception ex){
            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accInvoiceImpl.saveInvoiceDocuments:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    /**
     * Check GST Rule Present or not FOR Import Sales Order for INDIA 
     * @param paramsObj
     * @param customJArr
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    public JSONObject getGSTRuleImportSalesOrder(JSONObject paramsObj, JSONArray customJArr) throws JSONException, ServiceException {
        JSONObject returnObj = new JSONObject();
        HashMap<String, Object> requestParams = new HashMap<>();
        String failureMSG = "";
        String companyID = paramsObj.optString(Constants.companyKey);
        requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
        requestParams.put(Constants.filter_values, Arrays.asList(paramsObj.getString(Constants.companyKey), Constants.Acc_Product_Master_ModuleId, Constants.GSTProdCategory));
        KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
        Product product = (Product) paramsObj.opt("product");
        Date salesOrderDate = (Date) paramsObj.opt("salesOrderDate");
        int countryid = paramsObj.optInt(Constants.COUNTRY_ID);
        /*
         * Check For empty Field params
         */
        FieldParams params = null;
        if (fieldParamsResult.getEntityList() != null && fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty() && fieldParamsResult.getEntityList().size() > 0) {
            params = (FieldParams) fieldParamsResult.getEntityList().get(0);
        }
        AccProductCustomData accProductCustomData = null;
        String coldata = "";
        if (product != null && params != null) {
            accProductCustomData = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), product.getID());
            coldata = accProductCustomData != null ? accProductCustomData.getCol(params.getColnum()) : "";
        }
        if (StringUtil.isNullOrEmpty(coldata)) {
            failureMSG += "Product Tax Class not defined for Product  " + (product != null ? product.getProductName() : "");
        }
        String todimension1 = "";
        String todimension2 = "";
        String todimension3 = "";
        String entity = "";
        HashMap<String, Object> gstRulerequestParams = new HashMap<String, Object>();
        for (int i = 0; i < customJArr.length(); i++) {
            JSONObject Customjobj = customJArr.getJSONObject(i);
            if (Customjobj.has("Custom_Entity")) {
                entity = Customjobj.optString(Customjobj.optString("Custom_Entity"), "");
            }
            if (Customjobj.has("Custom_State")) {
                todimension1 = Customjobj.optString(Customjobj.optString("Custom_State"), "");
            }
            if (countryid == Constants.USA_country_id) {
                if (Customjobj.has("Custom_City")) {
                    todimension2 = Customjobj.optString(Customjobj.optString("Custom_City"), "");
                }
                if (Customjobj.has("Custom_County")) {
                    todimension3 = Customjobj.optString(Customjobj.optString("Custom_County"), "");
                }
            }
        }
        if (StringUtil.isNullOrEmpty(entity)) {
            failureMSG += "Entity dimension value not presnet.";
        }
        if (StringUtil.isNullOrEmpty(todimension1)) {
            failureMSG += " State dimension value not present";
        } else {
            gstRulerequestParams.put("todimension1", todimension1);
        }
        if (countryid == Constants.USA_country_id) {
            if (StringUtil.isNullOrEmpty(todimension2)) {
                failureMSG += " City dimension value not present";
            } else {
                gstRulerequestParams.put("todimension2", todimension2);
            }
            if (StringUtil.isNullOrEmpty(todimension3)) {
                failureMSG += " County dimension value not present";
            } else {
                gstRulerequestParams.put("todimension3", todimension3);
            }
        }
        gstRulerequestParams.put("productcategory", coldata);
        gstRulerequestParams.put("isProdCategoryPresent", true);
        gstRulerequestParams.put("salesOrPurchase", true);
        gstRulerequestParams.put("entity", entity);
        gstRulerequestParams.put("applieddate", salesOrderDate != null ? salesOrderDate : "");
        gstRulerequestParams.put("companyid", companyID);
        gstRulerequestParams.put("linelevelterms", paramsObj.optString("linelevelterms", ""));
        KwlReturnObject GSTRulekwlReturnObject = accSalesOrderDAOobj.getGSTRuleSetupForImportSalesOrder(gstRulerequestParams);
        String productentitytermid = "";
        if (GSTRulekwlReturnObject != null && GSTRulekwlReturnObject.getEntityList().size() > 0) {
            EntitybasedLineLevelTermRate entitybasedLineLevelTermRate = (EntitybasedLineLevelTermRate) GSTRulekwlReturnObject.getEntityList().get(0);
            productentitytermid = entitybasedLineLevelTermRate.getId();
            if (entitybasedLineLevelTermRate.getPercentage() != paramsObj.optDouble("termPercent", 0)) {
                failureMSG += "GST Rule Not present for " + paramsObj.optString("lineleveltermsName", "") + ". ";
            }
        } else {
            failureMSG += "GST Rule Not present for " + paramsObj.optString("lineleveltermsName", "") + ". ";
        }
        returnObj.put("productentitytermid", productentitytermid);
        returnObj.put("failureMSG", failureMSG);
        return returnObj;
    }
        
   @Override
   public void sendConsignmentApprovalEmails(JSONObject paramJobj,User sender, SalesOrder so,String billno,boolean isApproved,boolean isEdit) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        String sendorInfo = sender.getEmailID()!=null?sender.getEmailID():authHandlerDAOObj.getSysEmailIdByCompanyID(so.getCompany().getCompanyID());
        Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), sender.getCompany().getCompanyID());
        
        boolean isMailForOlympus=(company.getSubDomain().equalsIgnoreCase("olympus2") || company.getSubDomain().equalsIgnoreCase("olympus3"))? true : false;
        List<String> customFieldRemarkList=new ArrayList<String>();
        String purposeOfRequestValue="";
        if (isMailForOlympus && !isApproved) {  // this is hardcoded check for olympus as per mentioned in ERP-22631 as this is required for olympus only
            
            String reQValue=paramJobj.optString("Custom_Purpose Of Request",null);
            if(!StringUtil.isNullOrEmpty(reQValue)){
                KwlReturnObject cusObj = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), reQValue);
                FieldComboData fieldCombo = (FieldComboData) cusObj.getEntityList().get(0);
                if(fieldCombo != null){
                    purposeOfRequestValue=fieldCombo.getValue();
                }
            }
            
            JSONArray jArr = new JSONArray(paramJobj.getString(Constants.detail));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jCustomObj = jArr.getJSONObject(i);
                String customfield = jCustomObj.getString(Constants.customfield);
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
        
        String msg = null;
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
                if (isMailForOlympus && !isApproved) {
                    headerItems.add("Remarks");
                }
                
                String emailIds = "";
                String mailSeparator = ",";
                String htmlText = "";

                boolean isfirst = true;

                for (SalesOrderDetail sod : so.getRows()) {
                    List data = new ArrayList();
                    data.add(sno);
                    data.add(sod.getProduct().getProductid()); //product code
                    data.add(sod.getProduct().getDescription()); //product Desc
                    data.add(sod.getQuantity()); //quantity
                    if(isApproved){
                        data.add(sod.getApprovedQuantity());
                    }
                    data.add(sod.getUom()!=null?sod.getUom().getNameEmptyforNA():"");
                    if(isMailForOlympus && customFieldRemarkList != null && !customFieldRemarkList.isEmpty() && !isApproved){
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
                            subject=subject.replaceAll("#Sales_Person#", so.getSalesperson()!=null?so.getSalesperson().getValue():"");
                            subject=subject.replaceAll("#Document_Number#", billno);
                            htmlTextC=htmlTextC.replaceAll("#Document_Number#", billno);
                            htmlTextC=htmlTextC.replaceAll("#User_Name#", sender.getFullName());
                            
                            if(nr.isMailToStoreManager()){
                                for (User user : mgrSet) {
                                    if (isfirst) {
                                        emailIds += user.getEmailID();
                                        isfirst = false;
                                    } else {
                                        emailIds += mailSeparator + user.getEmailID();
                                    }

                                }
                            }
                            
                            if (nr.isMailToSalesPerson()) {
                                MasterItem mi = so.getSalesperson();
                                if (mi != null) {
                                    if(isfirst){
                                        emailIds += mi.getEmailID();
                                        isfirst = false;
                                    } else {
                                        emailIds += mailSeparator + mi.getEmailID();
                                    }
                                }
                            }
                            if(!StringUtil.isNullOrEmpty(nr.getEmailids())){ //copy to emails address
                                if(isfirst){
                                    emailIds += nr.getEmailids();
                                    isfirst = false;
                                }else{
                                    emailIds += mailSeparator + nr.getEmailids();
                                }
                            }
                            if (!StringUtil.isNullOrEmpty(nr.getUsers())) {
                                String[] addresseeUserIdArray = nr.getUsers().split(",");
                                for (String usr : addresseeUserIdArray) {
                                    KwlReturnObject usrObj = accountingHandlerDAOobj.getObject(User.class.getName(), usr);
                                    User user = (User) usrObj.getEntityList().get(0);
                                    if(user != null && !StringUtil.isNullOrEmpty(user.getEmailID())){
                                        if(isfirst){
                                            emailIds += user.getEmailID();
                                            isfirst = false;
                                        } else {
                                            emailIds += mailSeparator + user.getEmailID();
                                        }
                                    }
                                }

                            }
                            break;
                        }
                    }
                }
                
                
                htmlTextC += "<br/><b>Customer Name :</b> "+so.getCustomer().getName() + "</b>";
                htmlTextC += "<br/><b>From Date :</b>     "+df.format(so.getFromdate()) + "</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>To Date :</b>       "+df.format(so.getTodate()) + "</b>";
                htmlTextC += "<br/><b>Store :</b>         " + store.getFullName() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Location :</b>      " + so.getRequestLocation().getName() + "</b>";
                
                if(isMailForOlympus && !isApproved){
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
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    } 
   
   @Override
    /**
     * @param mailParameters(String companyid, String ruleId, String prNumber, String fromName, boolean hasApprover, int moduleid, String createdby, String PAGE_URL)
     * @throws ServiceException 
     */
    public void sendMailToApprover(Map<String, Object> mailParameters) throws ServiceException {
        KwlReturnObject cap = null;
        int level=0;
        if (mailParameters.containsKey(Constants.companyid)) {
            cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), (String) mailParameters.get(Constants.companyid));
        }
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
        boolean hasApprover = false;
        int moduleid = 0;
        String createdby = "";
        if(mailParameters.containsKey(Constants.createdBy)){
            createdby = (String) mailParameters.get(Constants.createdBy);
        }
        if(mailParameters.containsKey("level")){
            level = (int) mailParameters.get("level");
        }
        if(mailParameters.containsKey(Constants.moduleid)){
            moduleid = (int) mailParameters.get(Constants.moduleid);
        }
        if(mailParameters.containsKey(Constants.hasApprover)){
            hasApprover = (boolean) mailParameters.get(Constants.hasApprover);
        }
        String transactionName="";
        String transactionNo="";
        switch(moduleid){
            case Constants.Acc_Sales_Order_ModuleId :
                transactionName="Sales Order";
                transactionNo="Sales Order Number";
                break;
            case Constants.Acc_Customer_Quotation_ModuleId :
                transactionName="Customer Quotation";
                transactionNo="Customer Quotation Number";
                break;
        }
        String requisitionApprovalSubject = transactionName+": %s - Approval Notification";
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
                + "<p>%s has created "+transactionName+" %s and sent it to you for approval. at level "+(level+1)+"</p>"
                + "<p>Please review and approve it ("+transactionNo+": %s).</p>"
                + "<p>Company Name:- %s</p>"
                + "<p>Please check on Url:- %s</p>"
                + "<p></p>"
                + "<p>Thanks</p>"
                + "<p>This is an auto generated email. Do not reply<br>";
        String requisitionApprovalPlainMsg = "Hi All,\n\n"
                + "%s has created "+transactionName+" %s and sent it to you for approval. at level "+(level+1)+"\n"
                + "Please review and approve it ("+transactionNo+": %s).\n\n"
                + "Company Name:- %s \n"
                + "Please check on Url:- %s \n\n"
                + "Thanks\n\n"
                + "This is an auto generated email. Do not reply\n";
        try {
            if (hasApprover && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                KwlReturnObject returnObject = null;
                if(mailParameters.containsKey(Constants.companyid)){
                    returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), (String) mailParameters.get(Constants.companyid));
                }
                Company company = (Company) returnObject.getEntityList().get(0);
                String fromEmailId = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                String companyName = company.getCompanyName();
                String subject = "";
                String htmlMsg = "";
                String plainMsg = "";
                if (mailParameters.containsKey(Constants.prNumber) ) {
                     subject = String.format(requisitionApprovalSubject, (String) mailParameters.get(Constants.prNumber));
                }
                if (mailParameters.containsKey(Constants.prNumber) && mailParameters.containsKey(Constants.fromName) && mailParameters.containsKey(Constants.PAGE_URL)) {
                     htmlMsg = String.format(requisitionApprovalHtmlMsg, (String) mailParameters.get(Constants.fromName), (String) mailParameters.get(Constants.prNumber),  (String) mailParameters.get(Constants.prNumber), companyName, (String) mailParameters.get(Constants.PAGE_URL));
                     plainMsg = String.format(requisitionApprovalPlainMsg, (String) mailParameters.get(Constants.fromName), (String) mailParameters.get(Constants.prNumber), (String) mailParameters.get(Constants.prNumber), companyName, (String) mailParameters.get(Constants.PAGE_URL));
                }
                ArrayList<String> emailArray = new ArrayList<String>();
                String[] emails = {};
                String userDepartment=null;
                KwlReturnObject returnObjectRes=null; 
                
                HashMap<String, Object> dataMap=new HashMap<String,Object>();
                if (mailParameters.containsKey(Constants.ruleid)) {
                    dataMap.put(Constants.ruleid, (String) mailParameters.get(Constants.ruleid));
                }
                if(mailParameters.containsKey(Constants.companyid)){
                    dataMap.put(Constants.companyKey, (String) mailParameters.get(Constants.companyid));
                }
                dataMap.put("checkdeptwiseapprover", true);
                
                KwlReturnObject userResult1 = accMultiLevelApprovalDAOObj.checkDepartmentWiseApprover(dataMap);
                if (userResult1 != null && userResult1.getEntityList() != null && userResult1.getEntityList().size() > 0) {
                    User user = null;
                    if (!StringUtil.isNullObject(createdby)) {
                        returnObjectRes = accountingHandlerDAOobj.getObject(User.class.getName(), createdby);
                        user = (User) returnObjectRes.getEntityList().get(0);
                    }
                     if(user!=null && !StringUtil.isNullObject(user.getDepartment())){
                       userDepartment= user.getDepartment();
                       dataMap.put("userdepartment", userDepartment);
                    }
                }
                
                KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(dataMap);
                
                if(userResult.getEntityList()!=null && userResult.getEntityList().size()<=0 && !StringUtil.isNullOrEmpty(userDepartment )){
                    dataMap.remove("userdepartment");
                    userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(dataMap);
                }
                Iterator useritr = userResult.getEntityList().iterator();
                while (useritr.hasNext()) {
                    Object[] userrow = (Object[]) useritr.next();
                    emailArray.add(userrow[3].toString());
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
            }
        } catch (MessagingException ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
   @Override
    public Set<AssetDetails> saveAssetDetails(JSONObject paramJobj, String productId, String assetDetails) throws SessionExpiredException, AccountingException, UnsupportedEncodingException {
        Set<AssetDetails> assetDetailsSet = new HashSet<AssetDetails>();
        try {
            JSONArray jArr = new JSONArray(assetDetails);
            String companyId =paramJobj.getString(Constants.companyKey);
            
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                String assetId = StringUtil.DecodeText(jobj.optString("assetId"));
                double sellAmt = jobj.optDouble("sellAmount", 0);
                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                KwlReturnObject DOObj = accountingHandlerDAOobj.getObject(AssetDetails.class.getName(), assetId);
                AssetDetails assetDetail = (AssetDetails) DOObj.getEntityList().get(0);
                
                dataMap.put("assetDetailId", assetId);
                dataMap.put("sellAmount", sellAmt);
                dataMap.put("productId", productId);
                dataMap.put("isLinkedToLeaseSO", true);
                dataMap.put("companyId", companyId);
                
                KwlReturnObject result = accProductObj.updateAssetDetails(dataMap);
                
                assetDetail = (AssetDetails) result.getEntityList().get(0);
                
                assetDetailsSet.add(assetDetail);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Processing Data");
        } catch (JSONException ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Processing Data");
        }
        return assetDetailsSet;
    }
   
    public List saveSalesOrderRows(JSONObject paramJobj, SalesOrder SalesOrder, String companyid, String currencyid, HashMap<String, Object> GlobalParams, double externalCurrencyRate) throws ServiceException, AccountingException, JSONException {
        HashSet rows = new HashSet();
        List ll = new ArrayList();
        boolean isConsignment = false;
        String billdate=null;
        ArrayList<String> prodList = new ArrayList<String>();
        boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString("isLeaseFixedAsset",null))) ? Boolean.parseBoolean(paramJobj.getString("isLeaseFixedAsset")) : false;
        boolean isLinkedFromReplacementNumber = (!StringUtil.isNullOrEmpty(paramJobj.optString("isLinkedFromReplacementNumber",null))) ? Boolean.parseBoolean(paramJobj.getString("isLinkedFromReplacementNumber")) : false;
        try {
            String isConsignmentStr = paramJobj.optString("isConsignment",null);
            if (!StringUtil.isNullOrEmpty(isConsignmentStr)) {
                isConsignment = Boolean.parseBoolean(isConsignmentStr);
            }
            boolean isJobWorkOrderReciever = StringUtil.isNullOrEmpty(paramJobj.optString("isJobWorkOrderReciever",null)) ? false : Boolean.parseBoolean(paramJobj.getString("isJobWorkOrderReciever"));
            //Rest Services
            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                billdate = paramJobj.optString("OrderDate", null);
            } else {
                billdate = paramJobj.optString("billdate", null);
            }
            
            double totalAmount = 0.0;
            double lineleveltaxtermamount = 0d;
            JSONArray jArr = new JSONArray(paramJobj.getString(Constants.detail));
            DateFormat df = authHandler.getDateOnlyFormat();
            String userid = paramJobj.getString(Constants.useridKey);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            boolean includeProductTax = StringUtil.isNullOrEmpty(paramJobj.optString("includeprotax",null)) ? false : Boolean.parseBoolean(paramJobj.getString("includeprotax"));
            Boolean lockquantityflag = SalesOrder.isLockquantityflag();
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            String countryid = extraCompanyPreferences.getCompany().getCountry().getID();
            boolean RCMApplicable = paramJobj.optBoolean("GTAApplicable",false);
            Set<String> productNameRCMNotActivate = new HashSet<String>();
            for (int i = 0; i < jArr.length(); i++) {
                double rowAmount = 0;
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> sodDataMap = new HashMap<String, Object>();
                
                if(jobj.has("srno")) {
                    sodDataMap.put("srno", jobj.getInt("srno"));
                }
                if(SalesOrder.isGstIncluded()){
                    if(jobj.has("lineleveltermamount")){
                        sodDataMap.put("lineleveltermamount", jobj.optDouble("lineleveltermamount",0));
                    }
                }
                sodDataMap.put(Constants.companyKey, companyid);
                sodDataMap.put("soid", SalesOrder.getID());
                String productId="";

                productId = jobj.getString(Constants.productid);
                sodDataMap.put("productid", productId);
                 
                if (lockquantityflag) {//if assembly type of product is locked in SO then take its sub products also and store mapping of it
                    KwlReturnObject res = accountingHandlerDAOobj.getObject(Product.class.getName(),productId);
                    Product product = (Product) res.getEntityList().get(0);
                    if (product.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                        KwlReturnObject assprodlist = accSalesOrderDAOobj.getAssemblySubProductlist(productId, companyid);
                        List assprodlistdetails = assprodlist.getEntityList();
                        Iterator itr = assprodlistdetails.iterator();
                        
                        while (itr.hasNext()) {
                            ProductAssembly assemblyProduct = (ProductAssembly) itr.next();
                            HashMap<String, Object> assemblyMap = new HashMap<String, Object>();
                            assemblyMap.put("productid", assemblyProduct.getProduct().getID());
                            assemblyMap.put("subproductid", assemblyProduct.getSubproducts().getID());
                            assemblyMap.put("quantity", (Double) (assemblyProduct.getQuantity()) * (jobj.getDouble("quantity")));
                            accSalesOrderDAOobj.saveAssemblySubProdmapping(assemblyMap);
                        }
                    }
                }
                Product product = (Product) kwlCommonTablesDAOObj.getClassObject(Product.class.getName(), productId);
                prodList.add(productId);
                /**
                 * IF Invoice is RCM Applicable and Product is not RCM //ERP-34970(ERM-534)
                 * Applicable
                 */
                if (Integer.parseInt(countryid) == Constants.indian_country_id && RCMApplicable) {
                    if (product != null && !product.isRcmApplicable()) {
                        productNameRCMNotActivate.add(product.getName());
                       // throw new AccountingException(messageSource.getMessage("acc.common.rcmforproductnotactivated.SO.text", new Object[]{product.getName()}, null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
                sodDataMap.put("rate", jobj.getDouble("rate"));//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
                if (jobj.has("priceSource")) {
                    sodDataMap.put("priceSource", !StringUtil.isNullOrEmpty(jobj.optString("priceSource",null)) ? StringUtil.DecodeText(jobj.optString("priceSource")) : "");
                }
                if (jobj.has("pricingbandmasterid")) {
                    sodDataMap.put("pricingbandmasterid", !StringUtil.isNullOrEmpty(jobj.optString("pricingbandmasterid",null)) ? StringUtil.DecodeText(jobj.optString("pricingbandmasterid")) : "");
                }
                if (jobj.has("rateIncludingGst")) {
                    sodDataMap.put("rateIncludingGst", jobj.optDouble("rateIncludingGst", 0));
                }
                sodDataMap.put("quantity", jobj.getDouble("quantity"));
                if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    if (jobj.has("uomname")) {
                        sodDataMap.put("uomid", jobj.getString("uomname"));
                    }
                } else {
                    if (jobj.has("uomid")) {
                        sodDataMap.put("uomid", jobj.getString("uomid"));
                    }
                }
                sodDataMap.put("balanceqty", jobj.getDouble("quantity"));
                if (jobj.has("baseuomquantity") && jobj.get("baseuomquantity") != null) {
                    sodDataMap.put("baseuomquantity", jobj.getDouble("baseuomquantity"));
                    sodDataMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                } else {
                    sodDataMap.put("baseuomquantity", jobj.getDouble("quantity"));
                    sodDataMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                }
                if (jobj.has("baseuomquantity") && jobj.get("baseuomquantity") != null && lockquantityflag) {   //if lock checkbox is true then lockquantity is our SO quantity
                        sodDataMap.put("lockquantity", jobj.getDouble("baseuomquantity"));
                    if (jobj.has("batchdetails") && !StringUtil.isNullOrEmpty(jobj.getString("batchdetails")) && !jobj.getString("batchdetails").equals("[]")) {
                        sodDataMap.put("lockquantitydue", 0.0);//Quantity Lock assigned serial while creating Request
                    } else {
                            sodDataMap.put("lockquantitydue", jobj.getDouble("baseuomquantity"));
                    }
                } else {
                    sodDataMap.put("lockquantity", 0.0);//if lock checkbox is mot true then lockquantity is 0.0
                    sodDataMap.put("lockquantitydue", 0.0);//if lock checkbox is mot true then lockquantitydue is 0.0
                }

                // save Lock Quantity in Selected UOM
                if (lockquantityflag) {
                    sodDataMap.put("lockQuantityInSelectedUOM", jobj.getDouble("quantity"));
                }
                
                sodDataMap.put("remark", jobj.optString("remark",""));
                if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {  //This is sats specific code  
                    if (jobj.has("dependentType")) {
                        sodDataMap.put("dependentType", StringUtil.isNullOrEmpty(jobj.getString("dependentType")) ? jobj.getString("dependentTypeNo") : jobj.getString("dependentType"));
                    }
                    if (jobj.has("inouttime")) {
                        sodDataMap.put("inouttime", !StringUtil.isNullOrEmpty(jobj.getString("inouttime")) ? jobj.getString("inouttime") : "");
                    }
                    if (jobj.has("showquantity")) {
                        sodDataMap.put("showquantity", !StringUtil.isNullOrEmpty(jobj.getString("showquantity")) ? jobj.getString("showquantity") : "");
                    }
                }
                String rowtaxid = "";
                if (!StringUtil.isNullOrEmpty(jobj.optString("prtaxid", null)) && jobj.optString("prtaxid", null).equalsIgnoreCase("None")) {
                    rowtaxid = null;
                } else {
                    rowtaxid = jobj.optString("prtaxid",null);
                }
                    sodDataMap.put("desc", StringUtil.DecodeText(jobj.optString("desc")));
                
                //Default Header-Rest Service
                if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true && jobj.has("discount") && jobj.get("discount") != null) {
                    sodDataMap.put("discount", jobj.optDouble("discount", 0));
                } else if (jobj.has("prdiscount") && jobj.get("prdiscount") != null) {
                    sodDataMap.put("discount", jobj.optDouble("prdiscount", 0));
                }
                
                //Default Header Check-Rest Service
                if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true && jobj.has("discountType") && jobj.get("discountType") != null) {
                    sodDataMap.put("discountispercent", jobj.optInt("discountType", 1));
                } else if (jobj.has("discountispercent") && jobj.get("discountispercent") != null) {
                    sodDataMap.put("discountispercent", jobj.optInt("discountispercent", 1));
                }
                
                if (!StringUtil.isNullOrEmpty(jobj.optString("invstore",null))) {
                    sodDataMap.put("invstoreid", jobj.getString("invstore"));
                } else {
                    sodDataMap.put("invstoreid", "");
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invlocation",null))) {
                    sodDataMap.put("invlocationid", jobj.getString("invlocation"));
                } else {
                    sodDataMap.put("invlocationid", "");
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("recTermAmount",null))) {
                    sodDataMap.put("recTermAmount", jobj.getString("recTermAmount"));
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("bomid",null))) {
                    sodDataMap.put("bomid", jobj.getString("bomid"));
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("OtherTermNonTaxableAmount",null))) {
                    sodDataMap.put("OtherTermNonTaxableAmount", jobj.getString("OtherTermNonTaxableAmount"));
                }
                //get and put inspection template id
                if (!StringUtil.isNullOrEmpty(jobj.optString("inspectionTemplate",null))) {
                    sodDataMap.put("inspectionTemplate", jobj.getString("inspectionTemplate"));
                }
                //get and put inspection form id
                if (!StringUtil.isNullOrEmpty(jobj.optString("inspectionForm",null))) {
                    sodDataMap.put("inspectionForm", jobj.getString("inspectionForm"));
                }
                
                String linkmode = paramJobj.optString("fromLinkCombo",null);
                String linkNumber = paramJobj.optString("linkNumber",null);
                boolean isEdit =(!StringUtil.isNullOrEmpty(paramJobj.optString("isEdit",null))) ? Boolean.parseBoolean(paramJobj.getString("isEdit")) : false;
                String linkto = jobj.optString("linkto",null);
                if (isEdit) {
         
                    /* If we linking document (that was already linked with another document) in Edit mode 
                     i.e linking CQ->SO(CQ already linked with VQ)
                     then linkto is setting same as while creating document
                     because it is same as while creating new document by linking
                    
                     */
                     if ((!StringUtil.isNullOrEmpty(jobj.optString("savedrowid",null)))) {
                        if (linkmode.equalsIgnoreCase("Purchase Order")) {
                            List list = accSalesOrderDAOobj.getPOdetails(jobj.getString("savedrowid"), companyid);
                            if (list == null || list.isEmpty()) {
                                linkto = "";
                            }
                        } else if (linkmode.equalsIgnoreCase("Master Contract")) {
                                linkto = "";
                        } else if (linkmode.equalsIgnoreCase("Customer Quotation") || linkmode.equalsIgnoreCase("Lease Quotation")) {
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(QuotationDetail.class.getName(), jobj.getString("savedrowid"));
                            QuotationDetail quotationDetails = (QuotationDetail) rdresult.getEntityList().get(0);
                            if (quotationDetails == null || StringUtil.isNullObject(quotationDetails)) {
                                linkto = "";
                            }
                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(linkmode) && !StringUtil.isNullOrEmpty(linkNumber) && (!StringUtil.isNullOrEmpty(jobj.getString("rowid"))) && (!linkmode.equalsIgnoreCase(Constants.SALESORDER))) {
                    if (linkmode.equalsIgnoreCase(Constants.ACC_PURCHASE_ORDER) && !StringUtil.isNullOrEmpty(jobj.getString("rowid"))) {
                        sodDataMap.put("PurchaseOrderDetailID", (StringUtil.isNullOrEmpty(linkto)) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
                    } else if (linkmode.equalsIgnoreCase("Master Contract") && !StringUtil.isNullOrEmpty(jobj.getString("rowid"))) {
                        sodDataMap.put("MRPContractDetailsID", (StringUtil.isNullOrEmpty(linkto)) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
                    } else if (isLinkedFromReplacementNumber) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(ProductReplacementDetail.class.getName(), (StringUtil.isNullOrEmpty(jobj.getString("linkto"))) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
                        if (!rdresult.getEntityList().isEmpty()) {
                            ProductReplacementDetail pr = (ProductReplacementDetail) rdresult.getEntityList().get(0);
                            sodDataMap.put("productreplacementDetailId", pr.getId());
                        }
                    } else {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(QuotationDetail.class.getName(), (StringUtil.isNullOrEmpty(linkto)) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
                        QuotationDetail qod = (QuotationDetail) rdresult.getEntityList().get(0);
                        //qod.getID();
                            sodDataMap.put("quotationdetailid", qod != null ? qod.getID() : "");
                        }
                    }
                    //For multigroup company case only. Saving only podetailid in SO.
                    if (jobj.has("sourcepurchaseorderdetailid") && !StringUtil.isNullOrEmpty(jobj.optString("sourcepurchaseorderdetailid", null))) {
                        sodDataMap.put("sourcepurchaseorderdetailid", jobj.optString("sourcepurchaseorderdetailid"));
                    }
                
                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("rate"), currencyid, df.parse(billdate), externalCurrencyRate);

                rowAmount = (Double) jobj.optDouble("rate", 0.0) * jobj.getDouble("quantity");
                rowAmount = authHandler.round(rowAmount, companyid);
                if (!StringUtil.isNullOrEmpty(rowtaxid) && includeProductTax) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null) {
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null,Locale.forLanguageTag(paramJobj.getString("language"))));
                    } else {
                        sodDataMap.put("rowtaxid", rowtaxid);
                        double rowtaxamount = StringUtil.getDouble(jobj.getString("taxamount"));
                        sodDataMap.put("rowTaxAmount", rowtaxamount);
                        bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, rowtaxamount, currencyid, df.parse(billdate), externalCurrencyRate);
                        rowtaxamount = (Double) bAmt.getEntityList().get(0);
                        
                        rowAmount = rowAmount + rowtaxamount;
                        sodDataMap.put(Constants.isUserModifiedTaxAmount, jobj.optBoolean(Constants.isUserModifiedTaxAmount, false));
                    }
                    
//                    
//                    double lineleveltaxtermamountInBase = 0d;
                    lineleveltaxtermamount += StringUtil.getDouble(jobj.optString("lineleveltaxtermamount","0"));
//                    sodDataMap.put("rowtermtaxamount", lineleveltaxtermamount);
                }
                
                if(SalesOrder.isIsconsignment() && SalesOrder.isAutoapproveflag()){
                    String warehouse=SalesOrder.getRequestWarehouse()!=null?SalesOrder.getRequestWarehouse().getId():"";
                    String location=SalesOrder.getRequestLocation()!=null?SalesOrder.getRequestLocation().getId():"";
                     //code to Apply Pending Approval Rule and checking Consignment request approval rule
                    requestParams.clear();
                    requestParams.put("requestorid", userid);
                    requestParams.put("companyid", companyid);
                    requestParams.put("warehouse", warehouse);
                    requestParams.put("location", location);
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
                        sodDataMap.put("requestpendingapproval", RequestApprovalStatus.PENDING);
                        sodDataMap.put("approver", approverSet);
                    }else if(isConsignment&&!isRequestPending){
                         sodDataMap.put("requestpendingapproval", RequestApprovalStatus.APPROVED);
                         sodDataMap.put("approvalquantity", sodDataMap.get("baseuomquantity"));
                    }
                }
                /**
                 * Put job order item data
                 */
                sodDataMap.put("jobOrderItem", jobj.optBoolean("joborderitem", false));
                if(jobj.optBoolean("joborderitem", false)){
                    sodDataMap.put("jobOrderItemNumber", SalesOrder.getSalesOrderNumber()+"-"+jobj.optInt("srno"));
                }
                if (jobj.has("discountjson")) {
                    String discountjson=jobj.optString("discountjson", "");
                    discountjson=!StringUtil.isNullOrEmpty(discountjson)?StringUtil.decodeString(discountjson):"";
                    sodDataMap.put("discountjson",discountjson);
                }
                if (jobj.has("sourcepurchaseorderdetailid") && jobj.get("sourcepurchaseorderdetailid")!=null) {
                    sodDataMap.put("sourcepurchaseorderdetailid",jobj.get("sourcepurchaseorderdetailid"));
                }
                
                KwlReturnObject result = accSalesOrderDAOobj.saveSalesOrderDetails(sodDataMap);
                SalesOrderDetail row = (SalesOrderDetail) result.getEntityList().get(0);

                // Save SO Details Inspection template and form details
                String inspectionAreaDetailsStr = jobj.optString("inspectionAreaDetails", null);
                if (!StringUtil.isNullOrEmpty(inspectionAreaDetailsStr)) {
                    inspectionAreaDetailsStr = URLDecoder.decode(inspectionAreaDetailsStr, Constants.ENCODING);
                                        
                    //Save inspection form details
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    JSONObject params = new JSONObject();
                    params.put("inspectionDate", sdf.format(row.getSalesOrder().getOrderDate()));
                    params.put("modelName", row.getProduct().getProductid());
                    params.put("consignmentReturnNo", "");
                    params.put("department", "");
                    params.put("customerName", row.getSalesOrder().getCustomer().getName());
                    String inspectionFormId = "";
                    if (row != null) {
                        if (row.getInspectionForm() != null) {
                            inspectionFormId = row.getInspectionForm().getId();
                        }
                    }
                    params.put("inspectionFormId", inspectionFormId);
                    
                    //save inspection form
                    KwlReturnObject inspectionFormResult = accCommonTablesDAO.saveOrUpdateInspectionForm(params);
                    List list = inspectionFormResult.getEntityList();
                    InspectionForm insForm = (InspectionForm) list.get(0);
                    if(StringUtil.isNullOrEmpty(inspectionFormId)){
                        if(row != null){
                            row.setInspectionForm(insForm);
                            inspectionFormId = insForm.getId();
                        }
                        params.put("inspectionFormId", inspectionFormId);
                    }
                    params.put(Constants.detail, inspectionAreaDetailsStr);
                    //delete existing inspection form details
                    accCommonTablesDAO.deleteInspectionFormDetails(inspectionFormId);
                    JSONArray inspectionAreaJarr = new JSONArray(inspectionAreaDetailsStr);
                    //save inspection form details
                    for(int ind = 0; ind < inspectionAreaJarr.length(); ind++){
                        JSONObject inspectionAreaObj = inspectionAreaJarr.optJSONObject(ind);

                        HashMap<String, Object> inspectionFormDetailsMap = new HashMap<String, Object>();
                        inspectionFormDetailsMap.put("inspectionFormId", inspectionFormId);
                        inspectionFormDetailsMap.put("areaId", inspectionAreaObj.optString("areaId", ""));
                        inspectionFormDetailsMap.put("areaName", inspectionAreaObj.optString("areaName", ""));
                        inspectionFormDetailsMap.put("status", inspectionAreaObj.optString("status", ""));
                        inspectionFormDetailsMap.put("faults", inspectionAreaObj.optString("faults", ""));
                        inspectionFormDetailsMap.put("passingValue", inspectionAreaObj.optString("passingValue", ""));

                        accCommonTablesDAO.saveInspectionFormDetails(inspectionFormDetailsMap);
                    }
                    /**
                     * Update sales order details.
                     * Save inspection form at SO line level
                     */
                    HashMap<String, Object> SOMap = new HashMap<String, Object>();
                    SOMap.put("id", row.getID());
                    SOMap.put("inspectionFormId", inspectionFormId);
                    accSalesOrderDAOobj.saveSalesOrderDetails(SOMap);
                }
                
                // Save SO Details Custom Data
                String customfield = jobj.optString(Constants.customfield,null);
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    HashMap<String, Object> SOMap = new HashMap<String, Object>();
                    JSONArray jcustomarray = new JSONArray(customfield);

                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "SalesorderDetail");
                    customrequestParams.put("moduleprimarykey", "SoDetailID");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put("moduleid", isConsignment ? Constants.Acc_ConsignmentRequest_ModuleId : isLeaseFixedAsset?Constants.Acc_Lease_Order_ModuleId: isJobWorkOrderReciever?Constants.VENDOR_JOB_WORKORDER_MODULEID : Constants.Acc_Sales_Order_ModuleId);
                    customrequestParams.put("companyid", companyid);
                    SOMap.put("id", row.getID());
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
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "SalesorderDetail");
                    customrequestParams.put("moduleprimarykey", "SoDetailID");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                    customrequestParams.put("recdetailId", row.getID());
                    customrequestParams.put("productId", row.getProduct().getID());
                    customrequestParams.put("companyid", companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_SODETAIL_Productcustom_data_classpath);
                    /*
                     * Rich Text Area is put in json if User have not selected any data for this field. ERP-ERP-37624
                     */
                    customrequestParams.put("productIdForRichRext", row.getProduct().getID());                    
                    fieldDataManagercntrl.setRichTextAreaForProduct(customrequestParams);
                    fieldDataManagercntrl.setCustomData(customrequestParams);
                }

                HashMap<String, Object> sodVendDataMap = new HashMap<String, Object>();
                String vendorid = jobj.optString("vendorid", "");
                double vendorunitcost = jobj.optDouble("vendorunitcost", 0);
                double vendorcurrexchangerate = jobj.optDouble("vendorcurrexchangerate", 0);
                double totalcost = jobj.optDouble("totalcost", 0);
                KwlReturnObject resSODetailsVendorMap = null;
                if (!StringUtil.isNullOrEmpty(vendorid) && extraCompanyPreferences.isActivateProfitMargin()) {
                    sodVendDataMap.put("id", row.getID());
                    sodVendDataMap.put("vendorid", vendorid);
                    sodVendDataMap.put("vendorunitcost", vendorunitcost);
                    sodVendDataMap.put("vendorcurrexchangerate", vendorcurrexchangerate);
                    sodVendDataMap.put("totalcost", totalcost);
                    resSODetailsVendorMap = accSalesOrderDAOobj.saveSalesOrderDetailsVendorMapping(sodVendDataMap);
                }
                if (lockquantityflag && jobj.has("batchdetails") && jobj.getString("batchdetails") != null) {
                    String batchDetails = jobj.getString("batchdetails");
                    if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true && product.isIsBatchForProduct() && product.isIsSerialForProduct()) {
                       JSONObject jsobj= accProductModuleService.manipulateBatchDetailsforMobileApps(batchDetails, productId, paramJobj);
                        if (jsobj.get("batchdetails") != null && !StringUtil.isNullOrEmpty(jsobj.optString("batchdetails", null))) {
                            batchDetails = jsobj.getString("batchdetails");
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(batchDetails)) {
                        if (extraCompanyPreferences.isAutoFillBatchDetails() && !isConsignment) {
                            saveSONewBatchAutoFill(batchDetails, productId, paramJobj, row.getID());
                        } else {
                                saveSONewBatch(batchDetails, productId, paramJobj, row.getID());
                            }
                        }
                    }
                
                if (extraCompanyPreferences.getLineLevelTermFlag()==1 && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails"))) {
                    if (extraCompanyPreferences.isAvalaraIntegration()) {//If Avalara Integration is enabled, then save tax details in Avalara tax mapping table 'TransactionDetailAvalaraTaxMapping'
                        JSONObject paramsJobj = new JSONObject();
                        paramsJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
                        paramsJobj.put(Constants.companyKey, companyid);
                        if (integrationCommonService.isTaxCalculationEnabled(paramsJobj)) {
                            JSONObject saveTaxParamsJobj = new JSONObject();
                            saveTaxParamsJobj.put(IntegrationConstants.parentRecordID, row.getID());
                            saveTaxParamsJobj.put(IntegrationConstants.avalaraTaxDetails, StringUtil.DecodeText(jobj.optString("LineTermdetails")));
                            integrationCommonService.saveTransactionDetailTaxMapping(saveTaxParamsJobj);
                        }
                    } else {
                    JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                    for (int j = 0; j < termsArray.length(); j++) {
                        HashMap<String, Object> salesOrderDetailsTermsMap = new HashMap<String, Object>();
                        JSONObject termObject = termsArray.getJSONObject(j);

                        if (termObject.has("termid")) {
                            salesOrderDetailsTermsMap.put("term", termObject.get("termid"));
                        }
                        if (termObject.has("termamount")) {
                            salesOrderDetailsTermsMap.put("termamount", termObject.get("termamount"));
                        }
                        if (termObject.has("termpercentage")) {
                            salesOrderDetailsTermsMap.put("termpercentage", termObject.get("termpercentage"));
                        }
                        if (termObject.has("purchasevalueorsalevalue")) {
                            salesOrderDetailsTermsMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                        }
                        if (termObject.has("deductionorabatementpercent")) {
                            salesOrderDetailsTermsMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                        }
                        if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.getString("taxtype"))) {
                            salesOrderDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                            if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.getString("taxvalue"))) {
                                if(termObject.getInt("taxtype")==0){ // If Flat
                                    salesOrderDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                } else { // Else Percentage
                                    salesOrderDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                }
                            }
                        }
                        salesOrderDetailsTermsMap.put("salesOrderDetailID", row.getID());
                        /**
                         * ERP-32829 
                         */
                        salesOrderDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                        salesOrderDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                        salesOrderDetailsTermsMap.put("product", jobj.get("productid"));
                        salesOrderDetailsTermsMap.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                        
                        accSalesOrderDAOobj.saveSalesOrderDetailsTermMap(salesOrderDetailsTermsMap);
                    }
                }
                if (extraCompanyPreferences.getLineLevelTermFlag()==1) {
                    /**
                     * Save GST History Customer/Vendor data.
                     */
                    jobj.put("detaildocid", row.getID());
                    jobj.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                    fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jobj);
                }     
                }
                
                //  Indian Details Valuation Type -- start   
            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company)companyObj.getEntityList().get(0);
                if(company.getCountry()!=null &&  (Constants.indian_country_id) == Integer.parseInt(company.getCountry().getID())){
                    if(jobj.has("productMRP") && !StringUtil.isNullOrEmpty(jobj.getString("productMRP"))){
                            row.setMrpIndia(jobj.getDouble("productMRP"));
                    }
                    if(jobj.has("valuationType") && !StringUtil.isNullOrEmpty(jobj.getString("valuationType"))){ // Excise Details
                       row.setExciseValuationType(jobj.getString("valuationType"));
                       if((Constants.QUENTITY).equals(jobj.getString("valuationType"))){
                           if(jobj.has("reortingUOMExcise") && !StringUtil.isNullOrEmpty(jobj.getString("reortingUOMExcise"))){
                               UnitOfMeasure reportingUom=null;
                               KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(),jobj.getString("reortingUOMExcise"));
                               reportingUom = (UnitOfMeasure) custresult.getEntityList().get(0);
                               row.setReportingUOMExcise(reportingUom);
                           }
                           if(jobj.has("reortingUOMSchemaExcise") && !StringUtil.isNullOrEmpty(jobj.getString("reortingUOMSchemaExcise"))){
                               UOMschemaType reportingUom=null;
                               KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UOMschemaType.class.getName(),jobj.getString("reortingUOMSchemaExcise"));
                               reportingUom = (UOMschemaType) custresult.getEntityList().get(0);
                               row.setReportingSchemaTypeExcise(reportingUom);
                           }
                       }
                    }
                    if(jobj.has("valuationTypeVAT") && !StringUtil.isNullOrEmpty(jobj.getString("valuationTypeVAT"))){ // VAT Details
                       row.setVatValuationType(jobj.getString("valuationTypeVAT"));
                       if( (Constants.QUENTITY).equals(jobj.getString("valuationTypeVAT"))){
                           if(jobj.has("reportingUOMVAT") &&!StringUtil.isNullOrEmpty(jobj.getString("reportingUOMVAT"))){
                               UnitOfMeasure reportingUom=null;
                               KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), jobj.getString("reportingUOMVAT"));
                                 reportingUom = (UnitOfMeasure) custresult.getEntityList().get(0);
                                 row.setReportingUOMVAT(reportingUom);
                           }
                           if(jobj.has("reportingUOMSchemaVAT") && !StringUtil.isNullOrEmpty(jobj.getString("reportingUOMSchemaVAT"))){
                               UOMschemaType reportingUom=null;
                               KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UOMschemaType.class.getName(),jobj.getString("reportingUOMSchemaVAT"));
                               reportingUom = (UOMschemaType) custresult.getEntityList().get(0);
                               row.setReportingSchemaVAT(reportingUom);
                           }
                       }
                    }
                }
                // Indian Details Valuation Type -- End                 
                
                rows.add(row);
                totalAmount += rowAmount;
                // add asset Details 
                if (isLeaseFixedAsset && product.isAsset()) {
                    Set<AssetDetails> assetDetailsSet = saveAssetDetails(paramJobj, productId, jobj.getString("assetDetails"));
                    
                    Set<AssetInvoiceDetailMapping> assetInvoiceDetailMappings = saveAssetInvoiceDetailMapping(row.getID(), assetDetailsSet, companyid, Constants.Acc_Sales_Order_ModuleId);
            }
                
                
            }
            if (Integer.parseInt(countryid) == Constants.indian_country_id && RCMApplicable && !productNameRCMNotActivate.isEmpty()) {
                throw new AccountingException(messageSource.getMessage("acc.common.rcmforproductnotactivated.SO.text", new Object[]{StringUtils.join(productNameRCMNotActivate, ", ")}, null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            }
            SalesOrder.setRows(rows);
            ll.add(rows);
            ll.add(totalAmount);
            ll.add(prodList);
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE("saveSalesOrderRows : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveSalesOrderRows : " + ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("saveSalesOrderRows : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveSalesOrderRows : " + ex.getMessage(), ex);
        }
        return ll;
    } 
    
    @Override
    public KwlReturnObject assignStockToPendingConsignmentRequests(JSONObject paramJobj, Company company, User user) throws ServiceException {
          KwlReturnObject retObj = new KwlReturnObject(false, null, null, null, 0);
            boolean activateCRblockingWithoutStock = false;
            KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), company.getCompanyID());
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
            activateCRblockingWithoutStock = extraCompanyPreferences.isActivateCRblockingWithoutStock();

            // if CRBlockingWithoutStock feature is activated then procceed further
            if (activateCRblockingWithoutStock) {

                // get Pending consignment requests 
                KwlReturnObject pendingReqList = accCommonTablesDAO.getPendingConsignmentRequests(company);

                if (pendingReqList != null && pendingReqList.isSuccessFlag() && pendingReqList.getRecordTotalCount() > 0) {
                    
                    List<SalesOrder> consReqList = new ArrayList<SalesOrder>();
                    List listSerial = pendingReqList.getEntityList();
                    Iterator itrSerial = listSerial.iterator();
                    while (itrSerial.hasNext()) {
                           Object obj = (Object) itrSerial.next();
                        if (obj != null) {
                            String salesOrderId = (String) obj.toString();
                            if (!StringUtil.isNullOrEmpty(salesOrderId)) {
                                KwlReturnObject solist = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), salesOrderId);
                                SalesOrder salesOrder = (SalesOrder) solist.getEntityList().get(0);
                                consReqList.add(salesOrder);
                            }
                        }
                    }

                    /*
                 * this set is used to check whether serial is locked already or
                 * not.this has to be used bcoz somewhere in code sql query is
                 * used and somewhere hql is used (so hibernatetemplates session
                 * will be different) so changes made in Objects will not get
                 * reflected due to different hibernate session.So for this ,
                 * map is used to save locked serial until commit operation is
                 * performed.
                     */
                    Set usedProductBatchSerialSet = new HashSet();
                    Set usedProductBatchSet = new HashSet();

                    // Sales Order for loop
                    for (int i = 0; i < consReqList.size(); i++) {

                        SalesOrder so = consReqList.get(i);
                        Set<SalesOrderDetail> rows = so.getRows();
                        boolean autoapproveflag=false;
                        autoapproveflag=so.isAutoapproveflag();
                        MasterItem requestType = so.getMovementType();
                        String requestTypeId = null,requestWarehouse="",requestLocation="";;
                        if (requestType != null) {
                            requestTypeId = requestType.getID();
                        }
                        if (autoapproveflag) {
                            if (so.getRequestWarehouse() != null) {
                                requestWarehouse = so.getRequestWarehouse().getId();
                            }
                            if (so.getRequestLocation() != null) {
                                requestLocation = so.getRequestLocation().getId();
                            }
                        }
                        //Sales Order Detail for loop
                        for (SalesOrderDetail soDetail : rows) {

                            try {
                                Product product = soDetail.getProduct();
                                HashMap<Integer, Object[]> BatchdetalisMap = new HashMap<Integer, Object[]>();
                                KwlReturnObject kmsg = null;
                                String companyid = company.getCompanyID();

                                int batchcnt = 0;
                                int cnt = 0;
                                boolean isquantityNotavl = false;  //this flag is used to check whether serial batch quantity is avilabale 

                                  //get products batch serial list that is available (ie. non-locked)
                                
                                //location and warehouse should be enable for product while assigning the stock
                                if (product.isIslocationforproduct() && product.isIswarehouseforproduct()) {
                                    kmsg = accCommonTablesDAO.getBatchSerialDetailsforProduct(company, product.getID(), product.isIsSerialForProduct(), requestTypeId, false, requestWarehouse, requestLocation);
                                    List batchList = kmsg.getEntityList();
                                    Iterator bitr = batchList.iterator();
                                    while (bitr.hasNext()) {
                                        Object[] ObjBatchrow = (Object[]) bitr.next();
                                        BatchdetalisMap.put(cnt++, ObjBatchrow);
                                    }

                                String sodetailsid = soDetail.getID(); 
                                
                                double lockquantitydue = 0.0, ActbatchQty = 0.0, approvedquantity = 0.0,batchQty=0.0;
                                if (autoapproveflag) {
                                    approvedquantity = soDetail.getApprovedQuantity();
                                    if (!StringUtil.isNullOrEmpty(sodetailsid) && product.isIsSerialForProduct()) {
                                        ActbatchQty = accCommonTablesDAO.getserialAssignedQty(sodetailsid);
                                    }else{
                                        ActbatchQty = accCommonTablesDAO.getbatchAssignedQty(sodetailsid);
                                    }
                                    lockquantitydue = approvedquantity - ActbatchQty;
                                } else {
                                    lockquantitydue = soDetail.getLockquantitydue();
                                }
                                int cntp = (int) lockquantitydue;
                                batchQty=cntp;
                                if(product.isIsSerialForProduct()){ // for serial no case we will save the serial details and as location and warehouse are madnatory so it will genrate batchses also

                                    for (int j = 0; j < cntp; j++) {

                                    for (int serialCnt = 0; serialCnt < cnt; serialCnt++) {
                                         Object[] objArr = BatchdetalisMap.get(serialCnt);

                                        if (objArr != null) {

                                            String serialId = objArr[0] != null ? (String) objArr[0] : "";
                                            String batchId = objArr[1] != null ? (String) objArr[1] : "";
                                            String warehouse = objArr[10] != null ? (String) objArr[10] : "";
                                            String location = objArr[11] != null ? (String) objArr[11] : "";

                                            Date mfgDateObj = null;
                                            Date expDateObj = null;

                                            String checkInSet = product.getID() + batchId + serialId;

                                            if (!usedProductBatchSerialSet.contains(checkInSet)) {

                                                if (objArr[3] != null) { //ie mfgdate is not null
                                                    try {
                                                        java.sql.Timestamp mfgdatets = (java.sql.Timestamp) objArr[3];
                                                        mfgDateObj = new Date(mfgdatets.getTime());
                                                    } catch (Exception ex) {
                                                        try {
                                                            java.sql.Timestamp mfgdatets = (java.sql.Timestamp) objArr[3];
                                                            mfgDateObj = new Date(mfgdatets.getTime());
                                                        } catch (Exception e) {
                                                            java.sql.Date mfgdatets = (java.sql.Date) objArr[3];
                                                            mfgDateObj = new Date(mfgdatets.getTime());
                                                        }
                                                    }
                                                }
                                                if (objArr[4] != null) { //ie expdate is not null
                                                    java.sql.Date expdatets = (java.sql.Date) objArr[4];
                                                    expDateObj = new Date(expdatets.getTime());
                                                }
                                                if (!StringUtil.isNullOrEmpty(sodetailsid) && !StringUtil.isNullOrEmpty(batchId) && !StringUtil.isNullOrEmpty(serialId)) {
                                                    HashMap<String, Object> documentMap = new HashMap<String, Object>();
                                                    documentMap.put("quantity", "1");
                                                    documentMap.put("documentid", sodetailsid);
                                                    documentMap.put("transactiontype", "20");//This is SO Type Tranction   sales order moduleid
                                                    if (mfgDateObj != null) {
                                                        documentMap.put("mfgdate", mfgDateObj);
                                                    }
                                                    if (expDateObj != null) {
                                                        documentMap.put("expdate", expDateObj);
                                                    }
                                                    documentMap.put("batchmapid", batchId);
                                                    accCommonTablesDAO.saveBatchDocumentMapping(documentMap);

                                                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                                                    batchUpdateQtyMap.put("id", batchId);
                                                    batchUpdateQtyMap.put("lockquantity", "1");
                                                    accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
 
                                                    HashMap<String, Object> serialdocumentMap = new HashMap<String, Object>();
                                                    serialdocumentMap.put("quantity", "1");
                                                    serialdocumentMap.put("documentid", sodetailsid);
                                                    if (mfgDateObj != null) {
                                                        serialdocumentMap.put("mfgdate", mfgDateObj);
                                                    }
                                                    if (expDateObj != null) {
                                                        serialdocumentMap.put("expdate", expDateObj);
                                                    }
                                                    serialdocumentMap.put("serialmapid", serialId);
                                                    serialdocumentMap.put("transactiontype", "20");//This is so Type Tranction  

                                                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                                    requestParams.put("companyid", companyid);
                                                    if (!StringUtil.isNullOrEmpty(user.getUserID())) {
                                                        requestParams.put("requestorid", user.getUserID());
                                                    }
                                                    if (!StringUtil.isNullOrEmpty(warehouse)) {
                                                        requestParams.put("warehouse", warehouse);
                                                    }
                                                    if (!StringUtil.isNullOrEmpty(location)) {
                                                        requestParams.put("location", location);
                                                    }
                                                    //code to Apply Pending Approval Rule
                                                    if(!autoapproveflag){
                                                    KwlReturnObject ruleResult = accMasterItemsDAOobj.CheckRuleForPendingApproval(requestParams);
                                                    Iterator itr = ruleResult.getEntityList().iterator();
                                                    Set<User> approverSet = null;
                                                    boolean isRequestPending = false;
                                                    while (itr.hasNext()) {
                                                        ConsignmentRequestApprovalRule approvalRule = (ConsignmentRequestApprovalRule) itr.next();
                                                        if (approvalRule != null) {
                                                            KwlReturnObject res = accSalesOrderDAOobj.getConsignmentRequestApproverList(approvalRule.getID());
                                                            List<User> userlist = res.getEntityList();
                                                            Set<User> users = new HashSet<User>();;
                                                            for (User us : userlist) {
                                                                users.add(us);
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
                                                    }else{
                                                        Set<User> approverSet = null;
                                                        Set<User> users = new HashSet<User>();
                                                        if (soDetail != null) {
                                                            approverSet = soDetail.getApproverSet();
                                                        }
                                                        Iterator iterator = approverSet.iterator();
                                                        while (iterator.hasNext()) {
                                                            users.add((User) iterator.next());
                                                        }
                                                        serialdocumentMap.put("requestpendingapproval", RequestApprovalStatus.APPROVED);
                                                        serialdocumentMap.put("approver", users);
                                                    }

                                                    accCommonTablesDAO.saveSerialDocumentMapping(serialdocumentMap);

                                                    HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                                                    serialUpdateQtyMap.put("lockquantity", "1");
                                                    serialUpdateQtyMap.put("id", serialId);
                                                    accCommonTablesDAO.saveSerialAmountDue(serialUpdateQtyMap);

                                                    String setName = product.getID() + batchId + serialId;
                                                    usedProductBatchSerialSet.add(setName);

                                                    batchcnt += 1;
                                                    break;
                                                }
                                            }

                                        } else {
                                            isquantityNotavl = true;  //if quantity is not available then break and come out of for loop
                                            break;
                                        }
                                     }
                                   }
                                    } else {// for without serial case assign data to those
                                        while (batchQty != 0 ) {

                                            for (int batchCount = 0; batchCount < cnt; batchCount++) {
                                                Object[] objArr = BatchdetalisMap.get(batchCount);

                                                if (objArr != null && batchQty!=0) {
//                                                    String serialId = objArr[0] != null ? (String) objArr[0] : "";
                                                    String batchId = objArr[1] != null ? (String) objArr[1] : "";
                                                    String warehouse = objArr[10] != null ? (String) objArr[10] : "";
                                                    String location = objArr[11] != null ? (String) objArr[11] : "";

                                                    Date mfgDateObj = null;
                                                    Date expDateObj = null;
                                                    double batchavlqty=0;
                                                    if(!StringUtil.isNullOrEmpty(batchId)){
                                                    KwlReturnObject pbdresult = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), batchId);
                                                    NewProductBatch pbdetail = (NewProductBatch) pbdresult.getEntityList().get(0);
                                                    batchavlqty=pbdetail.getQuantitydue()-pbdetail.getLockquantity();
                                                    }
                                                    String checkInSet = product.getID() + batchId ;

                                                 if (!usedProductBatchSet.contains(checkInSet)) {

                                                    if (objArr[3] != null) { //ie mfgdate is not null
                                                        try{
                                                        java.sql.Timestamp mfgdatets = (java.sql.Timestamp) objArr[3];
                                                        mfgDateObj = new Date(mfgdatets.getTime());
                                                        } catch (Exception e) {
                                                            try {
                                                                java.sql.Date mfgdatets = (java.sql.Date) objArr[3];
                                                                mfgDateObj = new Date(mfgdatets.getTime());
                                                            } catch(Exception ex){
                                                                
                                                            }
                                                        }
                                                    }
                                                     if (objArr[4] != null) { //ie expdate is not null
                                                         try {
                                                             java.sql.Timestamp expdatets = (java.sql.Timestamp) objArr[4];
                                                             expDateObj = new Date(expdatets.getTime());
                                                         } catch (Exception e) {
                                                             try {
                                                                 java.sql.Date expdatets = (java.sql.Date) objArr[4];
                                                                 expDateObj = new Date(expdatets.getTime());
                                                             } catch (Exception ex) {
                                                             }
                                                         }
                                                     }
                                                    HashMap<String, Object> documentMap = new HashMap<String, Object>();
                                                     if (!StringUtil.isNullOrEmpty(sodetailsid) && !StringUtil.isNullOrEmpty(batchId)) {
                                                         HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                                                         if (batchavlqty > 0) {
                                                             if (batchQty > batchavlqty) {
                                                                 batchUpdateQtyMap.put("lockquantity", String.valueOf(batchavlqty));
                                                                 documentMap.put("quantity", String.valueOf(batchavlqty));
                                                                 documentMap.put("approvedqty", String.valueOf(batchavlqty));

                                                                 batchQty = batchQty - batchavlqty;
                                                             } else {
                                                                 batchUpdateQtyMap.put("lockquantity", String.valueOf((batchQty)));
                                                                 documentMap.put("quantity", String.valueOf(batchQty));
                                                                 documentMap.put("approvedqty", String.valueOf(batchQty));

                                                                 batchQty = batchQty - batchQty;
                                                             }
                                                             batchUpdateQtyMap.put("id", batchId);
                                                             accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
                                                         }
    
                                                        
                                                        documentMap.put("documentid", sodetailsid);
                                                         documentMap.put("transactiontype", "20");//This is SO Type Tranction   sales order moduleid
                                                         if (mfgDateObj != null) {
                                                             documentMap.put("mfgdate", mfgDateObj);
                                                         }
                                                         if (expDateObj != null) {
                                                             documentMap.put("expdate", expDateObj);
                                                         }
                                                         documentMap.put("batchmapid", batchId);
                                                         accCommonTablesDAO.saveBatchDocumentMapping(documentMap);
                                                     
                                                  }
                                                    
                                                    String setName = product.getID() + batchId;
                                                    usedProductBatchSerialSet.add(setName);
                                                 }

                                                } else {
                                                    isquantityNotavl = true;  //if quantity is not available then break and come out of for loop
                                                    break;
                                                }

                                            }
                                            if (isquantityNotavl) {
                                                break;
                                            }
                                             batchQty=0;
                                        }
                                    }
                                    if (!autoapproveflag) {
                                        accCommonTablesDAO.updateSOLockQuantitydue(sodetailsid, batchcnt, companyid);
                                    }
                                    if (isquantityNotavl) {
                                        break;
                                    }
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }

                    }

                }

            }
            return retObj;
    }
       
    private void deleteAssetDetails(SalesOrder so, String companyId) throws ServiceException {
        HashMap<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("companyid", companyId);
        requestMap.put("invoiceid", so.getID());
        requestMap.put("deleteMappingAlso", true);
        accSalesOrderDAOobj.deleteAssetDetailsLinkedWithSO(requestMap);
    }

    public void setValuesForAuditTrialForSO(SalesOrder oldso, JSONObject paramJobj, Map<String, Object> oldsoMap, Map<String, Object> soDataMap, Map<String, Object> newAuditKey) throws SessionExpiredException, JSONException {
        DateFormat df = authHandler.getUserDateFormatterJson(paramJobj);
        try {
            //Setting values in map for oldgreceipt
            if (oldso != null) {
                if (oldso.getTerm() != null) {
                    KwlReturnObject oldcredittermresult = accountingHandlerDAOobj.getObject(Term.class.getName(), oldso.getTerm().getID());
                    Term term = (Term) oldcredittermresult.getEntityList().get(0);
                    oldsoMap.put(Constants.CreditTermName, term.getTermname());
                    newAuditKey.put(Constants.CreditTermName, "Credit Term");
                }
                KwlReturnObject currobretrurnlist = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), oldso.getCurrency().getCurrencyID());
                KWLCurrency oldcurrencyobj = (KWLCurrency) currobretrurnlist.getEntityList().get(0);
                KwlReturnObject custobretrurnlist = accountingHandlerDAOobj.getObject(Customer.class.getName(), oldso.getCustomer().getID());
                Customer oldcustomer = (Customer) custobretrurnlist.getEntityList().get(0);
                if (oldso.getSalesperson() != null) {
                    KwlReturnObject oldmasteritemobretrurnlist = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), oldso.getSalesperson().getID());
                    MasterItem oldsalesPerson = (MasterItem) oldmasteritemobretrurnlist.getEntityList().get(0);
                    oldsoMap.put("auditSalesPerson", oldsalesPerson != null ? oldsalesPerson.getValue() : "");
                } else {
                    oldsoMap.put("auditSalesPerson", "");
                }
                newAuditKey.put("auditSalesPerson", "Sales Person");
                oldsoMap.put(Constants.CustomerName, oldcustomer.getName());
                newAuditKey.put(Constants.CustomerName, "Customer");
                oldsoMap.put("entrynumber", oldso.getSalesOrderNumber());
                newAuditKey.put("entrynumber", "Entry Number");
                oldsoMap.put(Constants.CurrencyName, oldcurrencyobj.getName());//Currency name
                newAuditKey.put(Constants.CurrencyName, "Currency");
                oldsoMap.put("memo", StringUtil.isNullOrEmpty(oldso.getMemo()) ? "" : oldso.getMemo());
                newAuditKey.put("memo", "Memo");
                oldsoMap.put("shipvia", StringUtil.isNullOrEmpty(oldso.getShipvia()) ? "" : oldso.getShipvia());
                newAuditKey.put("shipvia", "Ship Via");
                oldsoMap.put("fob", StringUtil.isNullOrEmpty(oldso.getFob()) ? "" : oldso.getFob());
                newAuditKey.put("fob", "FOB");
                oldsoMap.put("AuditShipDate", oldso.getShipdate() != null ? df.format(oldso.getShipdate()) : "");
                newAuditKey.put("AuditShipDate", "Ship Date");
                oldsoMap.put("AuditOrderdate", oldso.getOrderDate() != null ? df.format(oldso.getOrderDate()) : "");
                newAuditKey.put("AuditOrderdate", "Sales Order Date");
                oldsoMap.put("AuditDueDate", oldso.getDueDate() != null ? df.format(oldso.getDueDate()) : "");
                newAuditKey.put("AuditDueDate", "Due Date");
            }

            KwlReturnObject debittermresult = accountingHandlerDAOobj.getObject(Term.class.getName(), paramJobj.optString("termid", ""));
            Term term = (Term) debittermresult.getEntityList().get(0);
            soDataMap.put(Constants.CreditTermName, term.getTermname());//Credit Term Name
            String currencyid = (paramJobj.optString("currencyid", null) == null ? paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.getString(Constants.currencyKey));
            KwlReturnObject newcurrencyreturnobj = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency newcurrencyobj = (KWLCurrency) newcurrencyreturnobj.getEntityList().get(0);
            soDataMap.put(Constants.CurrencyName, newcurrencyobj.getName());//Currencey name
            KwlReturnObject custobretrurnlist = accountingHandlerDAOobj.getObject(Customer.class.getName(), paramJobj.getString("customer"));
            Customer newcustomer = (Customer) custobretrurnlist.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("salesPerson", null))) {
                KwlReturnObject newmasteritemobretrurnlist = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), paramJobj.getString("salesPerson"));
                MasterItem salesPerson = (MasterItem) newmasteritemobretrurnlist.getEntityList().get(0);
                soDataMap.put("auditSalesPerson", salesPerson.getValue());//SalesPerson Name
            } else {
                soDataMap.put("auditSalesPerson", "");//SalesPerson Name
            }
            soDataMap.put(Constants.CustomerName, newcustomer.getName());//Customer Name

        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
    
    @Override
    public void updateOpenStatusFlagForSO(String linkNumbers) throws ServiceException {
        HashMap hMap = new HashMap();
        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(Quotation.class.getName(), linkNumbers);
        Quotation quotation = (Quotation) rdresult.getEntityList().get(0);
        Set<QuotationDetail> rows = quotation.getRows();
        Iterator itrCQD = rows.iterator();
        boolean isOpen = false;
        while (itrCQD.hasNext()) {
            QuotationDetail row = (QuotationDetail) itrCQD.next();
            KwlReturnObject idresult = accSalesOrderDAOobj.getSODFromQD(row.getID());
            List list = idresult.getEntityList();
            Iterator itePOD = list.iterator();
            double qua = 0.0;
            while (itePOD.hasNext()) {
                SalesOrderDetail pod = (SalesOrderDetail) itePOD.next();
                qua += pod.getQuantity();
            }
            double addobj = row.getQuantity() - qua;
            if (addobj > 0) {
                hMap.put("isOpen", true);
                isOpen = true;
                break;
            }
        }
        hMap.put("isOpen", isOpen);
        hMap.put("quotation", quotation);
        hMap.put("value", "2");
        accSalesOrderDAOobj.updateCQLinkflag(hMap);
    } 
    
  @Override
    public List<String> approveSalesOrder(SalesOrder soObj, HashMap<String, Object> soApproveMap, boolean isMailApplicable) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException, JSONException {
        boolean hasAuthority = false;
        String companyid = "";

        List returnList = new ArrayList();
        List mailParamList = new ArrayList();
        int returnStatus;
        if (soApproveMap.containsKey("companyid") && soApproveMap.get("companyid") != null) {
            companyid = soApproveMap.get("companyid").toString();
        }
        String currentUser = "";
        if (soApproveMap.containsKey("currentUser") && soApproveMap.get("currentUser") != null) {
            currentUser = soApproveMap.get("currentUser").toString();
        }
        int level = 0;
        if (soApproveMap.containsKey("level") && soApproveMap.get("level") != null) {
            level = Integer.parseInt(soApproveMap.get("level").toString());
        }
        String amount = "";
        if (soApproveMap.containsKey("totalAmount") && soApproveMap.get("totalAmount") != null) {
            amount = soApproveMap.get("totalAmount").toString();
        }
        boolean fromCreate = false;
        if (soApproveMap.containsKey("fromCreate") && soApproveMap.get("fromCreate") != null) {
            fromCreate = Boolean.parseBoolean(soApproveMap.get("fromCreate").toString());
        }
        double totalProfitMargin = 0;
        if (soApproveMap.containsKey("totalProfitMargin") && soApproveMap.get("totalProfitMargin") != null) {
            totalProfitMargin = Double.parseDouble(soApproveMap.get("totalProfitMargin").toString());
        }
        double totalProfitMarginPerc = 0;
        if (soApproveMap.containsKey("totalProfitMarginPerc") && soApproveMap.get("totalProfitMarginPerc") != null) {
            totalProfitMarginPerc = Double.parseDouble(soApproveMap.get("totalProfitMarginPerc").toString());
        }
        JSONArray productDiscountMapList = null;
        if (soApproveMap.containsKey("productDiscountMapList") && soApproveMap.get("productDiscountMapList") != null) {
            productDiscountMapList = new JSONArray(soApproveMap.get("productDiscountMapList").toString());
        }
        boolean isLimitExceeding = false;   //ERM-396
        if (soApproveMap.containsKey("isLimitExceeding") && soApproveMap.get("isLimitExceeding") != null) {
            isLimitExceeding = Boolean.parseBoolean(soApproveMap.get("isLimitExceeding").toString());
        }
        if (!fromCreate) {
            String thisUser = currentUser;
            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
            User user = (User) userclass.getEntityList().get(0);

            if (AccountingManager.isCompanyAdmin(user)) {
                hasAuthority = true;
            } else {
                /*
                 If "Send approval documents to next level" is disabled from system preferences & pending document is edited then
                 1. When user is authorised then document is always goes at first level
                 2. When user is not authorised then document remains at same level
                 
                 */
                boolean isEditedPendingDocumentWithCheckOff = false;
                if (soApproveMap.containsKey("isEditedPendingDocumentWithCheckOff") && soApproveMap.get("isEditedPendingDocumentWithCheckOff") != null) {
                    level = Integer.parseInt(soApproveMap.get("documentLevel").toString());//Actual level of document for fetching rule at that level for the user
                    soApproveMap.put("level", level);
                    isEditedPendingDocumentWithCheckOff = true;
                }
                hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(soApproveMap);
                /*---If User is authorised at this level then sending document to first level that's why assigning "level=0" ------ */
                if (isEditedPendingDocumentWithCheckOff && hasAuthority) {
                    level = 0;
                }
            }
        } else {
            hasAuthority = true;
        }
        if (hasAuthority) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            int approvalStatus = 11;
            String prNumber = soObj.getSalesOrderNumber();
            String cqID = soObj.getID();
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put("companyid", companyid);
            qdDataMap.put("level", level + 1);
            qdDataMap.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            Iterator itr = flowresult.getEntityList().iterator();
            String fromName = "User";
            fromName = soObj.getCreatedby().getFirstName().concat(" ").concat(soObj.getCreatedby().getLastName());
            /**
             * parameters required for sending mail
             */
            Map<String, Object> mailParameters = new HashMap();
            mailParameters.put(Constants.companyid, companyid);
            mailParameters.put(Constants.prNumber, prNumber);
            mailParameters.put(Constants.fromName, fromName);
            mailParameters.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
            mailParameters.put(Constants.createdBy, soObj.getCreatedby().getUserID());
            if (soApproveMap.containsKey(Constants.PAGE_URL)) {
                mailParameters.put(Constants.PAGE_URL, (String) soApproveMap.get(Constants.PAGE_URL));
            }
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                HashMap<String, Object> recMap = new HashMap();
                mailParameters.put(Constants.ruleid, row[0].toString());
//            JSONObject obj = new JSONObject();
                String rule = "";
                if (row[2] != null) {
                    rule = row[2].toString();
                }
                String discountRule = "";
                if (row[7] != null) {
                    discountRule = row[7].toString();
                }
                boolean sendForApproval = false;
                int appliedUpon = Integer.parseInt(row[5].toString());
                if (appliedUpon == 3) {
                    rule = rule.replaceAll("[$$]+", String.valueOf(totalProfitMargin));
                } else if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount) {
                    if (productDiscountMapList != null) {
                        sendForApproval = AccountingManager.checkForProductAndProductDiscountRule(productDiscountMapList, appliedUpon, rule, discountRule);
                    }
                }else if(appliedUpon ==Constants.Specific_Products_Category){
                    /*
                     * Check If Rule is apply on product
                     * category from multiapproverule window
                     */
                    sendForApproval = accountingHandlerDAOobj.checkForProductCategoryForProduct(productDiscountMapList, appliedUpon, rule);
                }else if (appliedUpon == Constants.SO_CREDIT_LIMIT && isLimitExceeding) {
                    /*
                     * Check If Rule is apply on SO Credit limit
                     * category from multiapproverule window ERM-396
                     */
                    sendForApproval = true;
                } else {
                    rule = rule.replaceAll("[$$]+", amount);
                }
                if ((StringUtil.isNullOrEmpty(rule) && appliedUpon != Constants.SO_CREDIT_LIMIT)  || (!StringUtil.isNullOrEmpty(rule) && appliedUpon != Constants.Specific_Products && appliedUpon != Constants.Specific_Products_Discount && appliedUpon !=Constants.Specific_Products_Category && Boolean.parseBoolean(engine.eval(rule).toString())) || sendForApproval) {
                    // send emails
                    boolean hasApprover = Boolean.parseBoolean(row[3].toString());
                    mailParameters.put(Constants.hasApprover, hasApprover);
                    if (isMailApplicable) {
                        
                        mailParameters.put("level",level);
                        sendMailToApprover(mailParameters);
                        approvalStatus = level + 1;

                    } else {
                        approvalStatus = level + 1;
                        recMap.put("ruleid", row[0].toString());
                        recMap.put("fromName", fromName);
                        recMap.put("hasApprover", hasApprover);

                        mailParamList.add(recMap);
                    }
                }
            }
            accSalesOrderDAOobj.approvePendingSalesOrder(cqID, companyid, approvalStatus);
                returnStatus = approvalStatus;
        } else {
            returnStatus = Constants.NoAuthorityToApprove; //if not have approval permission then return one fix value like 999
        }
        returnList.add(returnStatus);
        returnList.add(mailParamList);

        return returnList;
    }   

    @Override
    public List mapInvoiceTerms(String InvoiceTerms, String id, String userid, boolean isQuotation) throws ServiceException {
        List ll = new ArrayList();
        try {
            JSONArray termsArr = new JSONArray(InvoiceTerms);
            for (int cnt = 0; cnt < termsArr.length(); cnt++) {
                JSONObject temp = termsArr.getJSONObject(cnt);
                HashMap<String, Object> termMap = new HashMap<String, Object>();
                termMap.put("term", temp.getString("id"));
                termMap.put("termamount", Double.parseDouble(temp.getString("termamount")));
                termMap.put("termtaxamount", temp.optDouble("termtaxamount",0));
                termMap.put("termtaxamountinbase", temp.optDouble("termtaxamountinbase",0));
                termMap.put("termtax", temp.optString("termtax",null));
                termMap.put("termAmountExcludingTax", temp.optDouble("termAmountExcludingTax",0));
                termMap.put("termAmountExcludingTaxInBase", temp.optDouble("termAmountExcludingTaxInBase",0));
                termMap.put("termamountinbase", temp.optDouble("termamountinbase",0));
                double percentage = 0;
                if (!StringUtil.isNullOrEmpty(temp.getString("termpercentage"))) {
                    percentage = Double.parseDouble(temp.getString("termpercentage"));
                }
                termMap.put("termpercentage", percentage);
                termMap.put("creationdate", new Date());
                termMap.put("userid", userid);
                if (isQuotation) {
                    termMap.put("quotationID", id);
                    accSalesOrderDAOobj.saveQuotationTermMap(termMap);
                } else {
                    termMap.put("salesOrderID", id);
                    accSalesOrderDAOobj.saveSalesOrderTermMap(termMap);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return ll;
    }

    public void saveSONewBatch(String batchJSON, String productId, JSONObject paramJobj, String documentId) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {

        JSONArray jArr = new JSONArray(batchJSON);
        double ActbatchQty = 1;
        double batchQty = 0;

        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        int serialsequence = 1 , batchsequence = 1; // for user selected sequence of batch and serial while creating SO.
        DateFormat df = authHandler.getDateOnlyFormat();
        String companyid = paramJobj.getString(Constants.companyKey);
        String userid = paramJobj.getString(Constants.useridKey);
        ExtraCompanyPreferences extraCompanyPreferences = null;
        KwlReturnObject kmsg = null;
        NewProductBatch productBatch = null;
        String productBatchId = "";
        KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), paramJobj.getString(Constants.companyKey));
        extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
        boolean isnegativestockforlocwar = extraCompanyPreferences.isIsnegativestockforlocwar();
        CompanyAccountPreferences companyPreferences = null;
        KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), paramJobj.getString(Constants.companyKey));
        companyPreferences = prefresult != null ? (CompanyAccountPreferences) prefresult.getEntityList().get(0) : null;
        boolean isnegativestockforso = companyPreferences.getNegativeStockSO()!=1;

        if (!StringUtil.isNullOrEmpty(productId)) {
            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), productId);
            Product product = (Product) prodresult.getEntityList().get(0);
            isLocationForProduct = product.isIslocationforproduct();
            isWarehouseForProduct = product.isIswarehouseforproduct();
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
            isRowForProduct = product.isIsrowforproduct();
            isRackForProduct = product.isIsrackforproduct();
            isBinForProduct = product.isIsbinforproduct();
        }

        //Save Batch detail for both  option for serial no and batch also as if batch option is off then also we are generating batch in backend
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
            if (jSONObject.has("quantity") && !jSONObject.optString("quantity").equals("undefined") && !jSONObject.optString("quantity").isEmpty()) {
                ActbatchQty = authHandler.roundQuantity(jSONObject.optDouble("quantity",0.0), companyid);
            }
            if (batchQty == 0) {
                batchQty = jSONObject.optDouble("quantity",0.0);
            }
            if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct || isRowForProduct || isRackForProduct || isBinForProduct) && (batchQty == ActbatchQty)) {
                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity",authHandler.roundQuantity(jSONObject.optDouble("quantity",0.0), companyid) );

                documentMap.put("documentid", documentId);
                documentMap.put("transactiontype", "20");//This is SO Type Tranction   sales order moduleid
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.optString("mfgdate",null))) {
                    documentMap.put("mfgdate", df.parse(jSONObject.optString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.optString("expdate",null))) {
                    documentMap.put("expdate", df.parse(jSONObject.optString("expdate")));
                }
                documentMap.put("batchmapid", jSONObject.optString("purchasebatchid",""));
                //for checking Consignment request approval rule
                requestParams.clear();
                requestParams.put("requestorid", userid);
                requestParams.put(Constants.companyKey, companyid);
                if (jSONObject.has("warehouse") && !StringUtil.isNullOrEmpty(jSONObject.optString("warehouse",null))) {
                    String warehouse = jSONObject.optString("warehouse");
                    requestParams.put("warehouse", warehouse);
                }
                if (jSONObject.has("location") && !StringUtil.isNullOrEmpty(jSONObject.optString("location",null))) {
                    String location = jSONObject.optString("location");
                    requestParams.put("location", location);
                }
                if (!isBatchForProduct && !isSerialForProduct) {
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                    filter_names.add("company.companyID");
                    filter_params.add(paramJobj.getString(Constants.companyKey));

                    if (jSONObject.has("warehouse") && !StringUtil.isNullOrEmpty(jSONObject.optString("warehouse",null))) {
                        String warehouse = jSONObject.optString("warehouse");
                        filter_names.add("warehouse.id");
                        filter_params.add(warehouse);
                    }
                    if (jSONObject.has("location") && !StringUtil.isNullOrEmpty(jSONObject.optString("location",null))) {
                        String location = jSONObject.optString("location");
                        filter_names.add("location.id");
                        filter_params.add(location);
                    }
                    if (jSONObject.has("row") && !StringUtil.isNullOrEmpty(jSONObject.optString("row",null))) {
                        String row = jSONObject.optString("row");
                        filter_names.add("row.id");
                        filter_params.add(row);
                    }
                    if (jSONObject.has("rack") && !StringUtil.isNullOrEmpty(jSONObject.optString("rack",null))) {
                        String rack = jSONObject.optString("rack");
                        filter_names.add("rack.id");
                        filter_params.add(rack);
                    }
                    if (jSONObject.has("bin") && !StringUtil.isNullOrEmpty(jSONObject.optString("bin",null))) {
                        String bin = jSONObject.optString("bin");
                        filter_names.add("bin.id");
                        filter_params.add(bin);
                    }

                    filter_names.add("product");
                    filter_params.add(productId);

                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    filterRequestParams.put("order_by", order_by);
                    filterRequestParams.put("order_type", order_type);
                    KwlReturnObject result = accMasterItemsDAOobj.getNewBatches(filterRequestParams, false, false);
                    List listResult = result.getEntityList();
                    Iterator itrResult = listResult.iterator();
                    Double quantityToDue = ActbatchQty;
                    double bcount = 0.0;
                    /**
                     * In case of Activate Negative Stock For Location Warehouse is ON or Negative Stock For SO in ON and 
                     * if there is no entry present in newproductbatch against that warehouse the below will be executed. 
                     */
                    if (((isnegativestockforlocwar || isnegativestockforso) && !(isBatchForProduct || isSerialForProduct)) && listResult.isEmpty()) {
                        HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                        pdfTemplateMap.put(Constants.companyKey, companyid);
                        pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("batch")));
//                        if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.optString("mfgdate", null))) {
//                            pdfTemplateMap.put("mfgdate", authHandler.getDateOnlyFormat().parse(jSONObject.optString("mfgdate")));
//                        }
//                        if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.optString("expdate", null))) {
//                            pdfTemplateMap.put("expdate", authHandler.getDateOnlyFormat().parse(jSONObject.optString("expdate")));
//                        }
                        String batchQuantity = jSONObject.optString("quantity");

                        if (!StringUtil.isNullOrEmpty(batchQuantity)) {
                            bcount = Double.parseDouble(batchQuantity);
                        }
                        pdfTemplateMap.put("quantity", String.valueOf(-(authHandler.roundQuantity(bcount, companyid))));
                        if (jSONObject.has("balance") && !StringUtil.isNullOrEmpty(jSONObject.getString("balance"))) {
                            pdfTemplateMap.put("balance", jSONObject.getString("balance"));
                        }
                        pdfTemplateMap.put("location", jSONObject.optString("location", ""));
                        pdfTemplateMap.put("product", productId);
                        pdfTemplateMap.put("warehouse", jSONObject.optString("warehouse", ""));
                        pdfTemplateMap.put("row", jSONObject.optString("row", ""));
                        pdfTemplateMap.put("rack", jSONObject.optString("rack", ""));
                        pdfTemplateMap.put("bin", jSONObject.optString("bin", ""));
                        pdfTemplateMap.put("isopening", false);
                        pdfTemplateMap.put("transactiontype", "20");//This is SO Type Tranction  
                        pdfTemplateMap.put("ispurchase", false);
                        kmsg = accCommonTablesDAO.saveNewBatchForProduct(pdfTemplateMap);
                        if (kmsg != null && kmsg.getEntityList().size() != 0) {
                            productBatch = (NewProductBatch) kmsg.getEntityList().get(0);
                            productBatchId = productBatch.getId();
                        }
                        documentMap.put("batchmapid", productBatchId);
                    }
                    while (itrResult.hasNext()) {
                        NewProductBatch newProductBatch = (NewProductBatch) itrResult.next();
                        documentMap.put("batchmapid", newProductBatch.getId());
                        if (quantityToDue > 0) {
                            double dueQty = authHandler.roundQuantity(newProductBatch.getQuantitydue(), companyid);
                            HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                            batchUpdateQtyMap.put("id", newProductBatch.getId());
                            if (dueQty > 0) {
                                if (quantityToDue > dueQty) {
                                    batchUpdateQtyMap.put("lockquantity", String.valueOf(dueQty));
                                    quantityToDue = quantityToDue - dueQty;

                                } else {
//                                    batchUpdateQtyMap.put("qty", String.valueOf(-(quantityToDue)));
                                    batchUpdateQtyMap.put("lockquantity", String.valueOf(quantityToDue));
                                    quantityToDue = quantityToDue - quantityToDue;

                                }
                                
                                accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);

                            }
                        }

                    }
                } else {

                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                    batchUpdateQtyMap.put("lockquantity", String.valueOf((Double.parseDouble(jSONObject.optString("quantity","0")))));
                    batchUpdateQtyMap.put("id", jSONObject.optString("purchasebatchid"));
                    accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);

                }
                /**
                 * added selected sequence for batch selected by user while
                 * creating SO.
                 */
                documentMap.put("batchsequence", batchsequence++);
                    accCommonTablesDAO.saveBatchDocumentMapping(documentMap);
            }
            batchQty--;//need to decrease for product having serial number 
            if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 
                HashMap<String, Object> documentMap = new HashMap<String, Object>();

                documentMap.put("quantity", 1);
                documentMap.put("lockquantity", 1);
                documentMap.put("serialmapid", jSONObject.optString("purchaseserialid"));
                documentMap.put("documentid", documentId);
                documentMap.put("transactiontype", "27");//This is GRN Type Tranction  
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.optString("expstart",null))) {
                    documentMap.put("expfromdate", df.parse(jSONObject.optString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.optString("expend",null))) {
                    documentMap.put("exptodate", df.parse(jSONObject.optString("expend")));
                }
                
                /**
                 * added selected sequence for serial selected by user while
                 * creating SO.
                 */
                documentMap.put("serialsequence", serialsequence++);
                KwlReturnObject krObj = accCommonTablesDAO.saveSerialDocumentMapping(documentMap);

                SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) krObj.getEntityList().get(0);
                if (jSONObject.has("customfield")) {
                    String customfield = jSONObject.optString("customfield","[]");
                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        HashMap<String, Object> DOMap = new HashMap<String, Object>();
                        JSONArray jcustomarray = new JSONArray(customfield);

                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "SerialDocumentMapping");
                        customrequestParams.put("moduleprimarykey", "SerialDocumentMappingId");
                        customrequestParams.put("modulerecid", serialDocumentMapping.getId());
                        customrequestParams.put("moduleid", Constants.SerialWindow_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        DOMap.put("id", serialDocumentMapping.getId());
                        customrequestParams.put("customdataclasspath", Constants.Acc_Serial_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            DOMap.put("serialcustomdataref", serialDocumentMapping.getId());
                            accCommonTablesDAO.updateserialcustomdata(DOMap);
                        }
                    }
                }
                HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                serialUpdateQtyMap.put("lockquantity", "1");
                serialUpdateQtyMap.put("id", jSONObject.optString("purchaseserialid"));
                accCommonTablesDAO.saveSerialAmountDue(serialUpdateQtyMap);

            } else {
                batchQty = 0; //for without serial
            }
        }

    }
    
       public void saveSONewBatchAutoFill(String batchJSON, String productId, JSONObject paramJobj, String documentId) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {

       JSONArray jArr = new JSONArray(batchJSON);
        double ActbatchQty = 1;
        double batchQty = 0;

        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        DateFormat df = authHandler.getDateOnlyFormat();
        String companyid = paramJobj.getString(Constants.companyKey);
        String userid = paramJobj.getString(Constants.useridKey);

        if (!StringUtil.isNullOrEmpty(productId)) {
            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), productId);
            Product product = (Product) prodresult.getEntityList().get(0);
            isLocationForProduct = product.isIslocationforproduct();
            isWarehouseForProduct = product.isIswarehouseforproduct();
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
            isRowForProduct = product.isIsrowforproduct();
            isRackForProduct = product.isIsrackforproduct();
            isBinForProduct = product.isIsbinforproduct();
        }

        //Save Batch detail for both  option for serial no and batch also as if batch option is off then also we are generating batch in backend
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
            if (jSONObject.has("quantity") && !jSONObject.optString("quantity").equals("undefined") && !jSONObject.optString("quantity").isEmpty()) {
                ActbatchQty = jSONObject.optDouble("quantity",0.0);
            }
            if (batchQty == 0) {
                batchQty = jSONObject.optDouble("quantity",0.0);
            }
            if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct || isRowForProduct || isRackForProduct || isBinForProduct) && (batchQty == ActbatchQty)) {
                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", jSONObject.optString("quantity","0"));

                documentMap.put("documentid", documentId);
                documentMap.put("transactiontype", "20");//This is SO Type Tranction   sales order moduleid
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.optString("mfgdate",null))) {
                    documentMap.put("mfgdate", df.parse(jSONObject.optString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.optString("expdate",null))) {
                    documentMap.put("expdate", df.parse(jSONObject.optString("expdate")));
                }
                documentMap.put("batchmapid", jSONObject.optString("purchasebatchid",""));
                //for checking Consignment request approval rule
                requestParams.clear();
                requestParams.put("requestorid", userid);
                requestParams.put(Constants.companyKey, companyid);
                if (jSONObject.has("warehouse") && !StringUtil.isNullOrEmpty(jSONObject.optString("warehouse",null))) {
                    String warehouse = jSONObject.optString("warehouse");
                    requestParams.put("warehouse", warehouse);
                }
                if (jSONObject.has("location") && !StringUtil.isNullOrEmpty(jSONObject.optString("location",null))) {
                    String location = jSONObject.optString("location");
                    requestParams.put("location", location);
                }
                if (!isBatchForProduct && !isSerialForProduct) {
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                    filter_names.add("company.companyID");
                    filter_params.add(paramJobj.getString(Constants.companyKey));

                    if (jSONObject.has("warehouse") && !StringUtil.isNullOrEmpty(jSONObject.optString("warehouse",null))) {
                        String warehouse = jSONObject.optString("warehouse");
                        filter_names.add("warehouse.id");
                        filter_params.add(warehouse);
                    }
                    if (jSONObject.has("location") && !StringUtil.isNullOrEmpty(jSONObject.optString("location",null))) {
                        String location = jSONObject.optString("location");
                        filter_names.add("location.id");
                        filter_params.add(location);
                    }
                    if (jSONObject.has("row") && !StringUtil.isNullOrEmpty(jSONObject.optString("row",null))) {
                        String row = jSONObject.optString("row");
                        filter_names.add("row.id");
                        filter_params.add(row);
                    }
                    if (jSONObject.has("rack") && !StringUtil.isNullOrEmpty(jSONObject.optString("rack",null))) {
                        String rack = jSONObject.optString("rack");
                        filter_names.add("rack.id");
                        filter_params.add(rack);
                    }
                    if (jSONObject.has("bin") && !StringUtil.isNullOrEmpty(jSONObject.optString("bin",null))) {
                        String bin = jSONObject.optString("bin");
                        filter_names.add("bin.id");
                        filter_params.add(bin);
                    }

                    filter_names.add("product");
                    filter_params.add(productId);

                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    filterRequestParams.put("order_by", order_by);
                    filterRequestParams.put("order_type", order_type);
                    KwlReturnObject result = accMasterItemsDAOobj.getNewBatches(filterRequestParams, false, false);
                    List listResult = result.getEntityList();
                    Iterator itrResult = listResult.iterator();
                    Double quantityToDue = ActbatchQty;
                    while (itrResult.hasNext()) {
                        NewProductBatch newProductBatch = (NewProductBatch) itrResult.next();
                        if (quantityToDue > 0) {
                            double dueQty = newProductBatch.getQuantitydue();
                            HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                            batchUpdateQtyMap.put("id", newProductBatch.getId());
                            if (dueQty > 0) {
                                if (quantityToDue > dueQty) {
                                    batchUpdateQtyMap.put("lockquantity", String.valueOf(dueQty));
                                    quantityToDue = quantityToDue - dueQty;

                                } else {
//                                    batchUpdateQtyMap.put("qty", String.valueOf(-(quantityToDue)));
                                    batchUpdateQtyMap.put("lockquantity", String.valueOf(quantityToDue));
                                    quantityToDue = quantityToDue - quantityToDue;

                                }
                                documentMap.put("batchmapid", newProductBatch.getId());
                                accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);

                            }
                        }

                    }
                } else {

                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                    batchUpdateQtyMap.put("lockquantity", String.valueOf((Double.parseDouble(jSONObject.optString("quantity","0")))));
                    batchUpdateQtyMap.put("id", jSONObject.optString("purchasebatchid"));
                    accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);

                }
                accCommonTablesDAO.saveBatchDocumentMapping(documentMap);
            }
            batchQty--;//need to decrease for product having serial number 
            String serialDetails=jSONObject.optString("serialDetails", "");
            if (isSerialForProduct && !StringUtil.isNullOrEmpty(serialDetails)) {  //if serial no option is on then only save the serial no details 

                JSONArray serialArr = new JSONArray(serialDetails);
                for (int count = 0; count < serialArr.length(); count++) {
                    JSONObject serialObject = new JSONObject(serialArr.get(count).toString());

                    HashMap<String, Object> documentMap = new HashMap<String, Object>();

                    documentMap.put("quantity", 1);
                    documentMap.put("lockquantity", 1);
                    documentMap.put("serialmapid", serialObject.optString("purchaseserialid"));
                    documentMap.put("documentid", documentId);
                    documentMap.put("transactiontype", "27");//This is GRN Type Tranction  
                    if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(serialObject.optString("expstart", null))) {
                        documentMap.put("expfromdate", df.parse(serialObject.optString("expstart")));
                    }
                    if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(serialObject.optString("expend", null))) {
                        documentMap.put("exptodate", df.parse(serialObject.optString("expend")));
                    }

                    KwlReturnObject krObj = accCommonTablesDAO.saveSerialDocumentMapping(documentMap);

                    SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) krObj.getEntityList().get(0);
                    if (serialObject.has("customfield")) {
                        String customfield = serialObject.optString("customfield", "[]");
                        if (!StringUtil.isNullOrEmpty(customfield)) {
                            HashMap<String, Object> DOMap = new HashMap<String, Object>();
                            JSONArray jcustomarray = new JSONArray(customfield);

                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", "SerialDocumentMapping");
                            customrequestParams.put("moduleprimarykey", "SerialDocumentMappingId");
                            customrequestParams.put("modulerecid", serialDocumentMapping.getId());
                            customrequestParams.put("moduleid", Constants.SerialWindow_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            DOMap.put("id", serialDocumentMapping.getId());
                            customrequestParams.put("customdataclasspath", Constants.Acc_Serial_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                DOMap.put("serialcustomdataref", serialDocumentMapping.getId());
                                accCommonTablesDAO.updateserialcustomdata(DOMap);
                            }
                        }
                    }
                    HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                    serialUpdateQtyMap.put("lockquantity", "1");
                    serialUpdateQtyMap.put("id", serialObject.optString("purchaseserialid"));
                    accCommonTablesDAO.saveSerialAmountDue(serialUpdateQtyMap);

                }
            } else {
                batchQty = 0; //for without serial
            }
        }

    }


    public Set<AssetInvoiceDetailMapping> saveAssetInvoiceDetailMapping(String invoiceDetailId, Set<AssetDetails> assetDetailsSet, String companyId, int moduleId) throws AccountingException {
        Set<AssetInvoiceDetailMapping> assetInvoiceDetailMappings = new HashSet<AssetInvoiceDetailMapping>();
        try {
            for (AssetDetails assetDetails : assetDetailsSet) {
                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                dataMap.put("invoiceDetail", invoiceDetailId);
                dataMap.put("moduleId", moduleId);
                dataMap.put("assetDetails", assetDetails.getId());
                dataMap.put("company", companyId);
                KwlReturnObject object = accProductObj.saveAssetInvoiceDetailMapping(dataMap);

                AssetInvoiceDetailMapping detailMapping = (AssetInvoiceDetailMapping) object.getEntityList().get(0);
                assetInvoiceDetailMappings.add(detailMapping);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            throw new AccountingException("Error while processing data.");
        }
        return assetInvoiceDetailMappings;
    }
    
    public JSONObject deleteSalesOrdersPermanentJson(JSONObject paramJobj) throws JSONException {
        JSONObject responseJobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isLeaseFixedAsset = false;
        boolean isConsignment = false;
        boolean isVendorJobWorkOrder = false;
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isLeaseFixedAsset, null))) {
            isLeaseFixedAsset = Boolean.parseBoolean(paramJobj.getString(Constants.isLeaseFixedAsset));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isConsignment, null))) {
            isConsignment = Boolean.parseBoolean(paramJobj.getString(Constants.isConsignment));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("isVendorJobWorkOrder", null))) {
            isVendorJobWorkOrder = Boolean.parseBoolean(paramJobj.getString("isVendorJobWorkOrder"));
        }
        Locale locale = (Locale) paramJobj.get(Constants.locale);
        try {
            String linkedTransactions = "";
            JSONArray jArr = new JSONArray(paramJobj.getString(Constants.RES_data));
            String companyid = paramJobj.getString(Constants.companyKey);
            String modulename = "";
            if (isConsignment) {
                modulename = " " + messageSource.getMessage("acc.consignment.order", null, locale) + " ";
            } else {
                modulename = Constants.SALESORDER;
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                linkedTransactions = deleteSalesOrderPermanent(linkedTransactions, jobj, companyid, isLeaseFixedAsset, paramJobj, modulename);
            }
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransactions)) {
                if (isLeaseFixedAsset) {
                    msg = messageSource.getMessage("acc.lso.del", null, locale);
                } else if (isConsignment) {
                    msg = messageSource.getMessage("acc.consignment.order.del", null, locale);
                } else if (isVendorJobWorkOrder) {
                    msg = messageSource.getMessage("acc.vendorjob.order.del", null, locale);
                } else {
                    msg = messageSource.getMessage("acc.so.del", null, locale);   //"Sales Order has been deleted successfully;
                }
            } else {
                if (isLeaseFixedAsset) {
                    msg = messageSource.getMessage("acc.field.LSOexcept", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);
                } else if (isVendorJobWorkOrder) {
                    msg = messageSource.getMessage("acc.field.vendorjob", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);
                } else if (isConsignment) {
                    msg = messageSource.getMessage("acc.field.consignmentOexcept", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);
                } else {
                    msg = messageSource.getMessage("acc.field.SalesOrdersexcept", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);   //"Sales Order has been deleted successfully;
                }
            }
        } catch (JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                responseJobj.put(Constants.RES_success, issuccess);
                responseJobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return responseJobj;
    }
    
    //Delete single sales order permanently
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class})
    public String deleteSalesOrderPermanent(String linkedTransactions, JSONObject jobj, String companyid, boolean isLeaseFixedAsset, JSONObject paramJobj, String modulename) throws JSONException, ServiceException, AccountingException {
        if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.billid))) {
            String soid = StringUtil.DecodeText(jobj.optString(Constants.billid));
            KwlReturnObject res = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soid);
            SalesOrder salesOrder = (SalesOrder) res.getEntityList().get(0);
            String sono = salesOrder.getSalesOrderNumber();//jobj.getString("billno");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("soid", soid);
            requestParams.put(Constants.companyKey, companyid);
            requestParams.put("sono", sono);
            requestParams.put(Constants.isLeaseFixedAsset, isLeaseFixedAsset);
            if (!StringUtil.isNullOrEmpty(soid)) {
                KwlReturnObject result = accSalesOrderDAOobj.getSOforinvoice(soid, companyid, true);  //for cheching SO is used in invoice or not
                int count1 = result.getRecordTotalCount();
                if (count1 > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the Invoices(s). So it cannot be deleted.");
                    linkedTransactions += sono + ", ";
                    return linkedTransactions;
                }
                KwlReturnObject resultd = accSalesOrderDAOobj.getDOforinvoice(soid, companyid, true);  //for cheching SO is used in DO or not
                int count2 = resultd.getRecordTotalCount();
                if (count2 > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the Delivery Order(s). So it cannot be deleted.");
                    linkedTransactions += sono + ", ";
                    return linkedTransactions;
                }
                KwlReturnObject resultp = accSalesOrderDAOobj.getPOforSO(soid, companyid);  //for checking SO is used in PO or not
                int count3 = resultp.getRecordTotalCount();
                if (count3 > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the purchase Order(s). So it cannot be deleted.");
                    linkedTransactions += sono + ", ";
                    return linkedTransactions;
                }
                boolean isSOLinked = accSalesOrderDAOobj.checkSoLinkedInContract(companyid, soid);
                if (isSOLinked) {
                    linkedTransactions += sono + " ,";
                    return linkedTransactions;
                }
                
                JSONObject reqParam = new JSONObject();
                reqParam.put("docid", soid);
                KwlReturnObject poReturnObj = accSalesOrderDAOobj.getLinkedPO(reqParam);
                List poList = poReturnObj.getEntityList();
                if (poReturnObj.getRecordTotalCount() > 0 && poReturnObj.getEntityList().get(0) != null) {
                    for (int poCount = 0; poCount < poReturnObj.getRecordTotalCount(); poCount++) {
                        reqParam = new JSONObject();
                        reqParam.put("purchaseOrderID", (String) poList.get(poCount));
                        accSalesOrderDAOobj.savePurchaseOrderStatusForSO(reqParam);
                    }
                }
                
                boolean isAvalaraIntegration = false;
                ExtraCompanyPreferences extraCompanyPreferences = null;
                result = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    extraCompanyPreferences = (ExtraCompanyPreferences) result.getEntityList().get(0);
                    isAvalaraIntegration = extraCompanyPreferences.isAvalaraIntegration();
                }
                
                if (isAvalaraIntegration) {//When Avalara Integration is enabled, also delete tax details from table 'TransactionDetailAvalaraTaxMapping'
                    deleteAvalaraTaxMappingForSO(salesOrder.getRows());
                }
                
                if (!salesOrder.isDeleted()) {
                accSalesOrderDAOobj.deleteSalesOrdersBatchSerialDetails(requestParams);
                }
                accSalesOrderDAOobj.deleteLinkingInformationOfSO(requestParams);
                accSalesOrderDAOobj.deleteSalesOrdersPermanent(requestParams);

                Map<String, Object> insertLogParams = new HashMap<String, Object>();
                insertLogParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                insertLogParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                insertLogParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                auditTrailObj.insertAuditLog(AuditAction.SALES_ORDER, "User " + paramJobj.getString(Constants.userfullname) + " has deleted a " + (isLeaseFixedAsset ? "Lease " : "") + modulename + " Permanently " + sono, insertLogParams, soid);
            }
        }
        return linkedTransactions;
    }

    public JSONObject deleteSalesOrdersJSON(JSONObject paramJobj) throws JSONException {
        JSONObject responseJobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isLeaseFixedAsset = false;
        boolean isConsignment = false;
        Locale locale = (Locale) paramJobj.get(Constants.locale);
        boolean isVendorJobWorkOrder = false;
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isLeaseFixedAsset, null))) {
            isLeaseFixedAsset = Boolean.parseBoolean(paramJobj.getString(Constants.isLeaseFixedAsset));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isConsignment, null))) {
            isConsignment = Boolean.parseBoolean(paramJobj.getString(Constants.isConsignment));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("isVendorJobWorkOrder", null))) {
            isVendorJobWorkOrder = Boolean.parseBoolean(paramJobj.getString("isVendorJobWorkOrder"));
        }
        try {
            String linkedTransactions = "";
            JSONArray jArr = new JSONArray(paramJobj.getString(Constants.RES_data));
            String companyid = paramJobj.getString(Constants.companyKey);
            String modulename = "";
            if (isConsignment) {
                modulename = " " + messageSource.getMessage("acc.consignment.order", null, locale) + " ";
            } else {
                modulename = Constants.SALESORDER;
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                linkedTransactions = deleteSalesOrder(jobj, linkedTransactions, companyid, paramJobj, modulename, isLeaseFixedAsset);
            }
            if (linkedTransactions != "" || linkedTransactions.length() > 0) {
                issuccess = false;
            } else {
                issuccess = true;
            }
            if (StringUtil.isNullOrEmpty(linkedTransactions)) {
                if (isLeaseFixedAsset) {
                    msg = messageSource.getMessage("acc.lso.del", null, locale);
                } else if (isConsignment) {
                    msg = messageSource.getMessage("acc.consignment.order.del", null, locale);
                } else if (isVendorJobWorkOrder) {
                    msg = messageSource.getMessage("acc.vendorjob.order.del", null, locale);
                } else {
                    msg = messageSource.getMessage("acc.so.del", null, locale);   //"Sales Order has been deleted successfully;
                }
            } else {
                if (isLeaseFixedAsset) {
                    msg = messageSource.getMessage("acc.field.LSOexcept", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);
                } else if (isConsignment) {
                    msg = messageSource.getMessage("acc.field.consignmentOexcept", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);
                } else if (isVendorJobWorkOrder) {
                    msg = messageSource.getMessage("acc.field.vendorjob", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);
                } else {
                    msg = messageSource.getMessage("acc.field.SalesOrdersexcept", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);   //"Sales Order has been deleted successfully;
                }
            }
        } catch (JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                responseJobj.put(Constants.RES_success, issuccess);
                responseJobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return responseJobj;
    }
    
    //Delete single sales order temporarily
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class})
    public String deleteSalesOrder(JSONObject jobj, String linkedTransactions, String companyid, JSONObject paramJobj, String modulename, boolean isLeaseFixedAsset) throws ServiceException, JSONException, AccountingException {
        if (!StringUtil.isNullOrEmpty(jobj.getString(Constants.billid))) {
            String soid = StringUtil.DecodeText(jobj.optString(Constants.billid));

            KwlReturnObject res = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soid);
            SalesOrder salesOrder = (SalesOrder) res.getEntityList().get(0);

            String sono = salesOrder.getSalesOrderNumber();//jobj.getString("billno");
            boolean isSOLinkedInContract = accSalesOrderDAOobj.checkSoLinkedInContract(companyid, soid);
            if (isSOLinkedInContract) {
                linkedTransactions += sono + " ,";
                return linkedTransactions;
            }
            KwlReturnObject result = accSalesOrderDAOobj.getSOforinvoice(soid, companyid, false);  //for cheching SO is used in invoice or not
            int count1 = result.getRecordTotalCount();
            if (count1 > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the Invoices(s). So it cannot be deleted.");
                linkedTransactions += sono + ", ";
                return linkedTransactions;
            }
            KwlReturnObject resultd = accSalesOrderDAOobj.getDOforinvoice(soid, companyid,false);  //for cheching SO is used in DO or not
            int count2 = resultd.getRecordTotalCount();
            if (count2 > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the Delivery Order(s). So it cannot be deleted.");
                linkedTransactions += sono + ", ";
                return linkedTransactions;
            }
            KwlReturnObject resultp = accSalesOrderDAOobj.getPOforSO(soid, companyid);  //for checking SO is used in PO or not
            int count3 = resultp.getRecordTotalCount();
            if (count3 > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the purchase Order(s). So it cannot be deleted.");
                linkedTransactions += sono + ", ";
                return linkedTransactions;
            }
            
            JSONObject reqParam = new JSONObject();
                reqParam.put("docid", soid);
                KwlReturnObject poReturnObj = accSalesOrderDAOobj.getLinkedPO(reqParam);
                List poList = poReturnObj.getEntityList();
                if (poReturnObj.getRecordTotalCount() > 0 && poReturnObj.getEntityList().get(0) != null) {
                    for (int poCount = 0; poCount < poReturnObj.getRecordTotalCount(); poCount++) {
                        reqParam = new JSONObject();
                        reqParam.put("purchaseOrderID", (String) poList.get(poCount));
                        accSalesOrderDAOobj.savePurchaseOrderStatusForSO(reqParam);
                    }
                }
            
            if (isLeaseFixedAsset) {
                HashMap<String, Object> requestMap = new HashMap<String, Object>();
                requestMap.put(Constants.companyKey, companyid);
                requestMap.put("invoiceid", soid);
                requestMap.put("deleteMappingAlso", true);
                accSalesOrderDAOobj.deleteAssetDetailsLinkedWithSO(requestMap);
            }

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("soid", soid);
            requestParams.put(Constants.companyKey, companyid);
            requestParams.put(Constants.isLeaseFixedAsset, isLeaseFixedAsset);
            
            accSalesOrderDAOobj.deleteSalesOrdersBatchSerialDetails(requestParams);
            
            accSalesOrderDAOobj.deleteSalesOrder(soid, companyid);
            String actionMsg = Constants.deleted;
            boolean isReject = StringUtil.isNullOrEmpty(paramJobj.optString("isReject", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isReject"));

            if (isReject == true) {
                actionMsg = "rejected";
            }
            Map<String, Object> insertLogParams = new HashMap<String, Object>();
            insertLogParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            insertLogParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            insertLogParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));

            auditTrailObj.insertAuditLog("77", "User " + paramJobj.getString(Constants.userfullname) + " has " + actionMsg + (isLeaseFixedAsset ? " a Lease " : " a ") + modulename + " " + sono, insertLogParams, soid);
        }
        return linkedTransactions;
    }
    
    @Override
    public JSONObject saveQuotationJSON(JSONObject paramJobj) throws SessionExpiredException, ServiceException, JSONException {
        JSONObject jobj = new JSONObject();

        String companyid = paramJobj.getString(Constants.companyKey);
        String msg = "", billid = "", billno = "", channelName = "", amount = "", auditMsg = "", auditID = "", additionalsauditmessage = "", entryNumber = "";
        String sequenceFormatName = "";
        String addAuditMessage = "";
        boolean issuccess = false;
        boolean accexception = false;
        boolean isDraft = false;
        boolean isTaxDeactivated = false;
        int approvalStatusLevel = 11;
        String butPendingForApproval = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        try {
            entryNumber = paramJobj.optString("number", null);
            String sequenceformat = paramJobj.optString("sequenceformat", null);
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(paramJobj.optString("copyInv", null)) ? false : Boolean.parseBoolean(paramJobj.getString("copyInv"));
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString("isLeaseFixedAsset", null))) ? Boolean.parseBoolean(paramJobj.getString("isLeaseFixedAsset")) : false;
            boolean isEditedPendingDocument = StringUtil.isNullOrEmpty(paramJobj.optString("isEditedPendingDocument", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEditedPendingDocument"));
            isDraft = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isDraft, null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isDraft)) : false; //SDP-13487
            boolean isSaveDraftRecord = (!StringUtil.isNullOrEmpty(paramJobj.optString("isSaveDraftRecord", null))) ? Boolean.parseBoolean(paramJobj.getString("isSaveDraftRecord")) : false; //SDP-13487
            boolean isAutoSeqForEmptyDraft = (!StringUtil.isNullOrEmpty(paramJobj.optString("isAutoSeqForEmptyDraft", null))) ? Boolean.parseBoolean(paramJobj.getString("isAutoSeqForEmptyDraft")) : false; //SDP-13927 : If Draft already having sequence no. then do not update it
            int istemplate = 0;
            /**
             * createAsTransactionChkboxwithTemplate- used to create template along with transaction.
             */
            boolean createAsTransactionChkboxwithTemplate = paramJobj.optString("createAsTransactionChkbox").equalsIgnoreCase("on") ? true : false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("istemplate", null))) {
                istemplate = Integer.parseInt((String) paramJobj.get("istemplate"));
            }
            String currentUser = paramJobj.getString(Constants.useridKey);
            String quotationId = paramJobj.optString("invoiceid", null);
            if (!StringUtil.isNullOrEmpty(quotationId)) { // Edit PO Case for updating address detail
                if (isSaveDraftRecord) { //edited draft and save it i.e.it get approved and add new CQ.
                    auditMsg = "added new";
                    auditID = AuditAction.CUSTOMER_QUOTATION_ADDED;
                } else { //edit case
                    auditMsg = "updated";
                    auditID = AuditAction.CUSTOMER_QUOTATION_UPDATED;
                    if (isDraft) { //edit CQ draft and again save as draft
                        addAuditMessage = "and saved as draft"; 
                    }
                }
            } else {
                    auditMsg = "added new";
                    auditID = AuditAction.CUSTOMER_QUOTATION_ADDED;
                }

            KwlReturnObject socnt = null;
            if (!StringUtil.isNullOrEmpty(quotationId)) { // In edit case checks duplicate number
                socnt = accSalesOrderDAOobj.getEditQuotationCount(entryNumber, companyid, quotationId);
                int count = socnt.getRecordTotalCount();
                if (count > 0 && istemplate != 2 && sequenceformat.equals("NA")) {
                    accexception = true;
                    throw new AccountingException(messageSource.getMessage("acc.QUO.customerquono", null, Locale.forLanguageTag(paramJobj.getString("language"))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                }
            } else { // In add case checks duplicate number
                socnt = accSalesOrderDAOobj.getQuotationCount(entryNumber, companyid);
                if (socnt.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    accexception = true;
                    throw new AccountingException(messageSource.getMessage("acc.QUO.customerquono", null, Locale.forLanguageTag(paramJobj.getString("language"))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                }
                
                //Check Deactivate Tax in New Transaction.
                if (!fieldDataManagercntrl.isTaxActivated(paramJobj)) {
                    isTaxDeactivated = true;
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.tax.deactivated.tax.saveAlert", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                }
            }
            synchronized (this) { // Checks duplicate number for simultaneous transactions
                status = txnManager.getTransaction(def);
                if (sequenceformat.equals("NA")) {
                    KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_Customer_Quotation_ModuleId);
                    if (resultInv.getRecordTotalCount() > 0) {
                        accexception = true;
                        throw new AccountingException(messageSource.getMessage("acc.QUO.selectedcustomerquono", null, Locale.forLanguageTag(paramJobj.getString("language"))) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                    } else {
                        accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_Customer_Quotation_ModuleId);
                    }
                }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
             /**
             * creating template for customer quotation.
             * istemplate=2 //creating only template
             * istemplate=0 //creating only transaction
             */
            if (createAsTransactionChkboxwithTemplate) {
                paramJobj.put("istemplate", 2);
                saveQuotation(paramJobj);
                paramJobj.remove("istemplate");
            }
             /**
             * creating customer quotation.
             */
            List li = saveQuotation(paramJobj);
            List mailParams = (List) li.get(5);
            String unlinkMessage = (String) li.get(6);

            Quotation quotation = (Quotation) li.get(0);
            if (li.get(1) != null) {
                additionalsauditmessage = li.get(1).toString();
            }
            billid = quotation.getID();
            billno = quotation.getquotationNumber();
            /*
             *get sequence format name for audit trail entry 
             */            
            KwlReturnObject result = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), sequenceformat);
            SequenceFormat seqFormat = (SequenceFormat) result.getEntityList().get(0);
            if (!sequenceformat.equals("NA")) {
                sequenceFormatName = seqFormat.getName();
            }
            double totalAmount = 0;

            if (li.get(2) != null) { // totalAmount
                totalAmount = Double.parseDouble(li.get(2).toString());
            }
            if (li.get(3) != null) { // approvalStatusLevel 
                approvalStatusLevel = Integer.parseInt(li.get(3).toString());
            }
            if (li.get(4) != null) { // butPendingForApproval 
                butPendingForApproval = li.get(4).toString();
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

                    if (((isCopy) ? true : !isEdit) && (!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA"))) {
                        boolean seqformat_oldflag = false; // old flag was used when sequence format not implemented.
                        String nextAutoNumber = "";
                        Map<String, Object> seqNumberMap = new HashMap<>();
                        if (seqformat_oldflag) {
                            nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_QUOTATION, sequenceformat);
                            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextAutoNumber);
                        } else {
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_QUOTATION, sequenceformat, seqformat_oldflag, quotation.getQuotationDate());
                        }
                        seqNumberMap.put(Constants.DOCUMENTID, billid);
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        //SDP-13487 : When user save the transaction at very first time then transaction no. & sequence no.will be saved as empty.
                        seqNumberMap.put(Constants.isDraft, isDraft);
                        billno = accSalesOrderDAOobj.updateCQEntryNumberForNewCQ(seqNumberMap);
                    } else if(isSaveDraftRecord && !sequenceformat.equals("NA") && isAutoSeqForEmptyDraft){  //SDP-13487 : Do not update Transaction No. in case of Sequence Format as "NA", //SDP-13927 : If Draft already having sequence no. then do not update it
                        /*
                        Below piece of code has written to handle Auto-Sequence no.in edit mode.
                        When user open the draft in edit mode, he can save it as a draft or a transaction. If it save as draft again then this code will not be execute.
                        But, if he saves it as a transaction then this code will be execute to get the Auto-Sequence No and set it to transaction no.
                        */
                        String nextAutoNumber = "";
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_QUOTATION, sequenceformat, false, quotation.getQuotationDate());
                        seqNumberMap.put(Constants.DOCUMENTID, billid);
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        seqNumberMap.put(Constants.isDraft, isDraft);
                        billno = accSalesOrderDAOobj.updateCQEntryNumberForNewCQ(seqNumberMap);
                    } else if(isDraft && !sequenceformat.equals("NA") && isAutoSeqForEmptyDraft){
                        /* SDP-13923
                        This piece of code has been written to fix below case.
                        1)Draft has been made with NA. 2)Draft has opened in edit mode and saved as a draft again with Auto-Sequence Format.
                        3)Again draft opened in edit mode then sequence format should be Auto-Sequence Format.
                        */
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_QUOTATION, sequenceformat, false, quotation.getQuotationDate());
                        seqNumberMap.put(Constants.DOCUMENTID, billid);
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        seqNumberMap.put(Constants.isDraft, isDraft);
                        billno = accSalesOrderDAOobj.updateCQEntryNumberForNewCQ(seqNumberMap);
                    }
                    txnManager.commit(AutoNoStatus);
                }

                /*
                 * This block is executed if any CQ will go for pending approval
                 * & mail wil be sent to admin
                 */
                if (mailParams != null && !mailParams.isEmpty()) {
                    /**
                     * parameters required for sending mail
                     */
                    Map<String, Object> mailParameters = new HashMap();
                    mailParameters.put(Constants.companyid, companyid);
                    mailParameters.put(Constants.prNumber, billno);
                    mailParameters.put(Constants.moduleid, Constants.Acc_Customer_Quotation_ModuleId);
                    mailParameters.put(Constants.createdBy, currentUser);
                    mailParameters.put(Constants.PAGE_URL, paramJobj.optString(Constants.PAGE_URL));
                    Iterator itr = mailParams.iterator();
                    while (itr.hasNext()) {
                        HashMap<String, Object> paramsMap = (HashMap<String, Object>) itr.next();

                        mailParameters.put(Constants.ruleid, (String) paramsMap.get("ruleid"));
                        mailParameters.put(Constants.fromName, (String) paramsMap.get("fromName"));
                        mailParameters.put(Constants.hasApprover, (Boolean) paramsMap.get("hasApprover"));
                        /*
                         * Method is used for sending mail to admin
                         */
                        if(quotation.getApprovestatuslevel() > 11){
                        mailParameters.put("level",quotation.getApprovestatuslevel());
                        }
                        sendMailToApprover(mailParameters);
                    }
                }

            } catch (Exception ex) {
                if (AutoNoStatus != null) {
                    txnManager.rollback(AutoNoStatus);
                }
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Customer_Quotation_ModuleId);
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
            /*
             * Inserting Entry in Audit trial when any document is unlinking
             * through Edit
             */
            Map<String, Object> insertLogParams = new HashMap<>();
            insertLogParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            insertLogParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            insertLogParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
            if (!StringUtil.isNullOrEmpty(unlinkMessage)) {
                auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.getString(Constants.userfullname) + " has unlinked " + "Customer Quotation " + billno + unlinkMessage + ".", insertLogParams, billno);
            }
            String linkedDocuments = (String) li.get(7);
            /*
             * Preparing Audit trial message if document is linking at the time
             * of creating
             */
            String linkingMessages = "";
            String linkFrom = paramJobj.optString("linkFrom", null);
            if (!StringUtil.isNullOrEmpty(linkedDocuments) && !StringUtil.isNullOrEmpty(linkFrom)) {
                linkingMessages = " by Linking to " + linkFrom + " " + linkedDocuments;
                auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.getString(Constants.userfullname) + " has " + auditMsg + " " + (isLeaseFixedAsset ? Constants.LEASE_CUSTOMER_QUOTATION : Constants.CUSTOMER_QUOTATION) + " " + billno + additionalsauditmessage + "  " + linkingMessages + " " + (approvalStatusLevel != 11 ? " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : ""), insertLogParams, quotation.getID());
            } else if (!isDraft && !sequenceformat.equals("NA") && !StringUtil.isNullOrEmpty(billno)) { //create CQ with sequence format
                auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.getString(Constants.userfullname) + " has " + auditMsg + " " + (isLeaseFixedAsset ? Constants.LEASE_CUSTOMER_QUOTATION : Constants.CUSTOMER_QUOTATION) + " " + billno + " with Sequence Format" + " " + sequenceFormatName, insertLogParams, quotation.getID());
            } else if (!isDraft && sequenceformat.equals("NA") && !StringUtil.isNullOrEmpty(billno)) { //create CQ with sequence format as NA
                auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.getString(Constants.userfullname) + " has " + auditMsg + " " + (isLeaseFixedAsset ? Constants.LEASE_CUSTOMER_QUOTATION : Constants.CUSTOMER_QUOTATION) + " " + billno + " with Sequence Format" + " NA", insertLogParams, quotation.getID());
            } else if (isDraft && sequenceformat.equals("NA")) {// CQ Draft with sequence format as NA
                if (StringUtil.isNullOrEmpty(addAuditMessage)) {//Created CQ Draft and save as draft
                    auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.getString(Constants.userfullname) + " has " + auditMsg + " " + (isLeaseFixedAsset ? Constants.LEASE_CUSTOMER_QUOTATION : Constants.CUSTOMER_QUOTATION) + " with Sequence Format" + " NA" + " as draft.", insertLogParams, quotation.getID());
                } else {//Edited CQ Draft and save as draft
                    auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.getString(Constants.userfullname) + " has " + auditMsg + " " + (isLeaseFixedAsset ? Constants.LEASE_CUSTOMER_QUOTATION : Constants.CUSTOMER_QUOTATION) + " with Sequence Format" + " NA" + " " + addAuditMessage, insertLogParams, quotation.getID());
                }
            } else if (isDraft && !sequenceformat.equals("NA") && !StringUtil.isNullOrEmpty(sequenceFormatName)) {// CQ Draft with sequence format
                if (StringUtil.isNullOrEmpty(addAuditMessage)) {//Created CQ Draft and save as draft
                    auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.getString(Constants.userfullname) + " has " + auditMsg + " " + (isLeaseFixedAsset ? Constants.LEASE_CUSTOMER_QUOTATION : Constants.CUSTOMER_QUOTATION) + " with Sequence Format" + " " + sequenceFormatName + " as draft.", insertLogParams, quotation.getID());
                } else {//Edited CQ Draft and save as draft
                    auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.getString(Constants.userfullname) + " has " + auditMsg + " " + (isLeaseFixedAsset ? Constants.LEASE_CUSTOMER_QUOTATION : Constants.CUSTOMER_QUOTATION) + " with Sequence Format" + " " + sequenceFormatName + " " + addAuditMessage, insertLogParams, quotation.getID());
                }
            }else{
                   auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.getString(Constants.userfullname) + " has " + auditMsg + " " + (isLeaseFixedAsset ? Constants.LEASE_CUSTOMER_QUOTATION : Constants.CUSTOMER_QUOTATION) + " " + billno + additionalsauditmessage +"  "+linkingMessages + " " + (approvalStatusLevel != 11 ? " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : ""), insertLogParams, quotation.getID());
            }
            
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("createAsTransactionChkbox", "")) && paramJobj.optString("createAsTransactionChkbox").equalsIgnoreCase("on") && !StringUtil.isNullOrEmpty(paramJobj.optString("templatename", ""))) {
                auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has added Document Template "+paramJobj.optString("templatename", "")+ " for record Customer Quotation" , insertLogParams, quotation.getID());
            }
            issuccess = true;
            if (isLeaseFixedAsset) {
                msg = messageSource.getMessage("acc.field.leaseCustomerQuotationhasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " " + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString("language"))) + ": <b>" + billno + "</b>";
            } else if(isDraft){
                //Below code snippet has written to show difference message when entry has been saved as a transaction or a draft.
                if(StringUtil.isNullOrEmpty(billno)){
                    msg = messageSource.getMessage("acc.field.CustomerQuotationDraft", null, Locale.forLanguageTag(paramJobj.getString("language"))) +" "+messageSource.getMessage("acc.draft.success.msg.hasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language")));                    
                } else {
                    msg = messageSource.getMessage("acc.field.CustomerQuotationDraft", null, Locale.forLanguageTag(paramJobj.getString("language"))) +" <b>"+billno+"</b> "+messageSource.getMessage("acc.draft.success.msg.hasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language")));
                }
            } else {
                msg = messageSource.getMessage("acc.field.CustomerQuotationhasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " " + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString("language"))) + ": <b>" + billno + "</b>";
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("createAsTransactionChkbox", "")) && paramJobj.optString("createAsTransactionChkbox").equalsIgnoreCase("on") && !StringUtil.isNullOrEmpty(paramJobj.optString("templatename", ""))) {
                    msg += " Template Name: <b>" + paramJobj.optString("templatename", "") + "</b>";
                }
            }
            if (istemplate == 2) { // message in case of document template saved
                msg = messageSource.getMessage("acc.field.CustomerQuotationTemplatehasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language")));
            }

            if (isLeaseFixedAsset) {
                channelName = "/LeaseQuotationReport/gridAutoRefresh";
            } else {
                channelName = "/SalesQuotationReport/gridAutoRefresh";
            }
                        
            /*------Code if we edit pending document---------  */
            if (isEditedPendingDocument) {

                String roleName = li.get(8) != null ? (String) li.get(8) : "";
                boolean isAuthorityToApprove = li.get(9) != null ? (Boolean) li.get(9) : false;
                boolean sendPendingDocumentsToNextLevel = li.get(10) != null ? (Boolean) li.get(10) : false;

                /*--If check "Send pending documents to next level" is activated from system preferences---------  */
                if (sendPendingDocumentsToNextLevel) {

                    if (roleName != "" && isAuthorityToApprove) {

                        /*----Prepare Messages and inset AuditLog for approval document------  */
                        msg += "<br>";
                        msg += messageSource.getMessage("acc.field.CustomerQuotationhasbeenapprovedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " by " + roleName + " " + paramJobj.getString(Constants.userfullname) + " at Level " + quotation.getApprovestatuslevel() + ".";

                        auditTrailObj.insertAuditLog(AuditAction.CUSTOMER_QUOTATION_UPDATED, "User " + paramJobj.getString(Constants.userfullname) + " has Approved a Customer Quotation " + quotation.getQuotationNumber() + " at Level-" + quotation.getApprovestatuslevel(), insertLogParams, quotation.getID());
                    } else {//If User have no authority to approve the document
                        msg += "<br>";
                        msg += messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, Locale.forLanguageTag(paramJobj.getString("language"))) + quotation.getApprovestatuslevel() + ".";
                    }
                } else if (!isAuthorityToApprove && butPendingForApproval == "") {//If user have no authority to approve document
                    msg += "<br>";
                    msg += messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, Locale.forLanguageTag(paramJobj.getString("language"))) + quotation.getApprovestatuslevel() + " and record will be available at this level for approval" + ".";
                }
            }
            
            status = txnManager.getTransaction(def);
            accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Customer_Quotation_ModuleId);
            txnManager.commit(status);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Customer_Quotation_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Customer_Quotation_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("billid", billid);
                jobj.put("billno", billno);
                jobj.put("amount", "");
                jobj.put("isAccountingExe", accexception);
                jobj.put("pendingApproval", approvalStatusLevel != 11);
                jobj.put(Constants.channelName, channelName);
                jobj.put(Constants.isTaxDeactivated, isTaxDeactivated);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    public List saveQuotation(JSONObject paramJobj) throws SessionExpiredException, ServiceException, AccountingException, ScriptException, MessagingException {
        Quotation quotation = null;
        String auditMsg = "", auditID = "", additionalsauditmessage = "";
        List returnList = new ArrayList();
        List mailParams = null;
        String unlinkMessage = "";
        try {
            boolean isCopy = StringUtil.isNullOrEmpty(paramJobj.optString("copyInv", null)) ? false : Boolean.parseBoolean(paramJobj.getString("copyInv"));
            boolean isRoundingAdjustmentApplied = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.IsRoundingAdjustmentApplied, null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.IsRoundingAdjustmentApplied)) : false;
            String currentUser = paramJobj.getString(Constants.useridKey);
            KwlReturnObject templateResult = null;
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParamsJson(paramJobj);
            int istemplate = paramJobj.optString("istemplate", null) != null ? Integer.parseInt(paramJobj.getString("istemplate")) : 0;
//            String taxid = paramJobj.optString("taxid", null);
            String taxid = "";
            if (paramJobj.optString("taxid").equalsIgnoreCase("None")) {
                taxid = null;
            } else {
                taxid = paramJobj.optString("taxid", null);
            }
            String sequenceformat = paramJobj.optString(Constants.sequenceformat, null);
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate", "1"));
            String companyid = paramJobj.getString(Constants.companyKey);

            boolean isLinkedFromReplacementNumber = (!StringUtil.isNullOrEmpty(paramJobj.optString("isLinkedFromReplacementNumber"))) ? Boolean.parseBoolean(paramJobj.getString("isLinkedFromReplacementNumber")) : false;

            String createdby = paramJobj.getString(Constants.useridKey);
            String modifiedby = paramJobj.getString(Constants.useridKey);
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString("isLeaseFixedAsset", null))) ? Boolean.parseBoolean(paramJobj.getString("isLeaseFixedAsset")) : false;
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEdit"));
            boolean isDraft = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isDraft, null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isDraft)) : false;
            boolean isSaveDraftRecord = (!StringUtil.isNullOrEmpty(paramJobj.optString("isSaveDraftRecord", null))) ? Boolean.parseBoolean(paramJobj.getString("isSaveDraftRecord")) : false; //SDP-13487
            boolean isAutoSeqForEmptyDraft = (!StringUtil.isNullOrEmpty(paramJobj.optString("isAutoSeqForEmptyDraft", null))) ? Boolean.parseBoolean(paramJobj.getString("isAutoSeqForEmptyDraft")) : false; //SDP-13927 : If Draft already having sequence no. then do not update it
            
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();

            String entryNumber = paramJobj.optString("number", null);
            String costCenterId = paramJobj.optString(Constants.costcenter, null);
            String quotationId = paramJobj.optString("invoiceid", null);
            String shipLength = paramJobj.optString("shipLength", null);
            String invoicetype = paramJobj.optString("invoicetype", null);
            String nextAutoNumber = "";
            boolean RCMApplicable = paramJobj.optBoolean("GTAApplicable",false);
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);

            /**
             * If Avalara Integration is enabled and tax calculation in Avalara Integration is enabled
             * then we make below method call which creates and adds tax details in requestJson
             * This is used when transaction is created by means other than UI; for example import, REST etc
             */
            if (extraCompanyPreferences != null && extraCompanyPreferences.isAvalaraIntegration()) {
                JSONObject paramsJobj = new JSONObject();
                paramsJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
                paramsJobj.put(Constants.companyKey, companyid);
                if (integrationCommonService.isTaxCalculationEnabled(paramsJobj)) {
                    paramJobj = integrationCommonService.createAvalaraTaxDetails(paramJobj, Constants.Acc_Customer_Quotation_ModuleId);
                }
            }
            
            HashMap<String, Object> qDataMap = new HashMap<>();
            int countryid = (extraCompanyPreferences.getCompany() != null && extraCompanyPreferences.getCompany().getCountry() != null) ? Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID()) : 0;
            Map<String, Object> oldquotationMap = new HashMap<>();
            Map<String, Object> newAuditKey = new HashMap<>();
            String currencyid = (paramJobj.optString(Constants.currencyKey, null) == null ? paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.getString(Constants.currencyKey));
            String deletedLinkedDocumentID = paramJobj.optString("deletedLinkedDocumentId", null);
            boolean isEditedPendingDocument = StringUtil.isNullOrEmpty(paramJobj.optString("isEditedPendingDocument", null)) ? false : Boolean.parseBoolean(paramJobj.getString("isEditedPendingDocument"));
            synchronized (this) {
                SequenceFormat prevSeqFormat = null;
                if (!StringUtil.isNullOrEmpty(quotationId)) { // For edit case check duplicate
                    HashMap<String, Object> termReqMap = new HashMap<>();
                    KwlReturnObject socnt = accSalesOrderDAOobj.getEditQuotationCount(entryNumber, companyid, quotationId);
                    int count = socnt.getRecordTotalCount();
                    if (count > 0 && istemplate != 2 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Quotationnumber", null, Locale.forLanguageTag(paramJobj.getString("language"))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                    } else {
                        KwlReturnObject rst = accountingHandlerDAOobj.getObject(Quotation.class.getName(), quotationId);
                        Quotation q = (Quotation) rst.getEntityList().get(0);
                        prevSeqFormat = q.getSeqformat();
                        if (!sequenceformat.equals("NA")) {
                            nextAutoNumber = entryNumber;
                        }
                        if (isEdit == true) { // For Audit Trial-ERP-12922
                            setValuesForAuditTrialMessage(q, paramJobj, oldquotationMap, qDataMap, newAuditKey);
                        }

                        qDataMap.put("id", quotationId);
                        if (!isLeaseFixedAsset && extraCompanyPreferences.isVersionslist() && !(q.isDraft())) { // Not Lease Record and Activated Version History
                            saveQuotationVersion(paramJobj, quotationId);
                        }
                        
                        if (extraCompanyPreferences.isAvalaraIntegration()) {//In case of edit, if Avalara Integration is enabled, delete tax mapping from table 'TransactionDetailAvalaraTaxMapping'
                            deleteAvalaraTaxMappingForQuotation(q.getRows());
                        }
                        
                        accSalesOrderDAOobj.deleteQuotationDetails(quotationId, companyid);

                        if (!extraCompanyPreferences.getCompany().getCountry().getID().equals(Constants.indian_country_id)) {
                            // Delete Quotation Term Map
                            termReqMap.put("quotationid", quotationId);
                            accSalesOrderDAOobj.deleteQuotationTermMap(termReqMap);
                        }
                    }
                    /*
                     * Deleting Linking information of CQ while editing
                     */
                    termReqMap.put("qid", quotationId);
                    accSalesOrderDAOobj.deleteLinkingInformationOfCQ(termReqMap);
                    /*
                     * Preparing Audit trial message while Editing CQ &
                     * unlinking document VQ
                     */
                    if (!StringUtil.isNullOrEmpty(deletedLinkedDocumentID)) {
                        String[] deletedLinkedDocumentIDArr = deletedLinkedDocumentID.split(",");
                        for (int i = 0; i < deletedLinkedDocumentIDArr.length; i++) {
                            /*
                             * Method used to get Vendor Quotation Number
                             */
                            KwlReturnObject result = accSalesOrderDAOobj.getVendorQuotationNumber(deletedLinkedDocumentIDArr[i]);

                            if (result != null) {
                                String VendorQuotatioNo = result.getEntityList().get(0).toString();
                                if (i == 0) {
                                    unlinkMessage += " from the Vendor Quotation(s) ";
                                }

                                if (unlinkMessage.indexOf(VendorQuotatioNo) == -1) {
                                    unlinkMessage += VendorQuotatioNo + ", ";
                                }
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(unlinkMessage) && unlinkMessage.endsWith(", ")) {
                        unlinkMessage = unlinkMessage.substring(0, unlinkMessage.length() - 2);
                    }
                }

                if (sequenceformat.equals("NA")) { // In case of NA checks wheather this entry number can also be generated by a sequence format or not
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Customer_Quotation_ModuleId, entryNumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                        }
                    }
                }
            }

            DateFormat df = authHandler.getDateOnlyFormat();
            String CustomerId = paramJobj.optString("customer", null);
            KwlReturnObject custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), CustomerId);
            Customer customer = (Customer) custresult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(quotationId)) { // Edit PO Case for updating address detail
                auditMsg = "updated";
                auditID = AuditAction.CUSTOMER_QUOTATION_UPDATED;
                Map<String, Object> addressParams = new HashMap<>();
                String billingAddress = paramJobj.optString(Constants.BILLING_ADDRESS, null);
                if (!StringUtil.isNullOrEmpty(billingAddress)) { // handling the cases when no address coming in edit case 
                    addressParams = AccountingAddressManager.getAddressParamsJson(paramJobj, false);
                } else {
                    addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(customer.getID(), companyid, accountingHandlerDAOobj);
                }
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Quotation.class.getName(), quotationId);
                Quotation quotation1 = (Quotation) returnObject.getEntityList().get(0);
                addressParams.put("id", quotation1.getBillingShippingAddresses() == null ? "" : quotation1.getBillingShippingAddresses().getID());
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                qDataMap.put("billshipAddressid", bsa.getID());
            } else { // Other Cases for saving address detail
                auditMsg = "added";
                auditID = AuditAction.CUSTOMER_QUOTATION_ADDED;
                boolean isDefaultAddress = paramJobj.optString("defaultAdress", null) != null ? Boolean.parseBoolean(paramJobj.getString("defaultAdress")) : false;
                Map<String, Object> addressParams = new HashMap<>();
                if (isDefaultAddress) {
                    addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(customer.getID(), companyid, accountingHandlerDAOobj);
                } else {
                    addressParams = AccountingAddressManager.getAddressParamsJson(paramJobj, false);
                }
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                qDataMap.put("billshipAddressid", bsa.getID());
            }
            qDataMap.put("externalCurrencyRate", externalCurrencyRate);
            /*
                In edit case of draft where auto-sequence no.is enable and not a save action then we have set entryno as empty to avoid duplication.
            */
            if(isDraft && isAutoSeqForEmptyDraft && !StringUtil.isNullOrEmpty(entryNumber)){
                entryNumber = "";
            }
            if(isDraft && !isSaveDraftRecord && !StringUtil.isNullOrEmpty(quotationId) && !sequenceformat.equals("NA") && StringUtil.isNullOrEmpty(entryNumber)){ //SDP-13927 : If Draft already having sequence no. then do not update it
                qDataMap.put("entrynumber", "");
            } else if (sequenceformat.equals("NA") || !StringUtil.isNullOrEmpty(quotationId)) {
                qDataMap.put("entrynumber", entryNumber);
            } else {
                qDataMap.put("entrynumber", "");
            }
            qDataMap.put(Constants.isDraft, isDraft);
            qDataMap.put("autogenerated", sequenceformat.equals("NA") ? false : true);
            qDataMap.put("memo", paramJobj.optString("memo", null));
            qDataMap.put("posttext", (paramJobj.optString("posttext", null) == null) ? "" : authHandler.removeStyleFromString(paramJobj.getString("posttext")));
            qDataMap.put("customerid", paramJobj.optString("customer", null));
            qDataMap.put("termid", paramJobj.optString("termid", null));
            qDataMap.put("orderdate", df.parse(paramJobj.optString("billdate", null)));
            qDataMap.put(Constants.Checklocktransactiondate, paramJobj.optString("billdate", null)); // ERP-16800-Without parsing date
            qDataMap.put(Constants.isSaveAsDraft, isDraft);
            qDataMap.put("duedate", df.parse(paramJobj.optString("duedate", null)));
            qDataMap.put("perDiscount", paramJobj.optBoolean("perdiscount", false));
            qDataMap.put("discount", paramJobj.optDouble("discount", 0));
            qDataMap.put("gstIncluded", (paramJobj.optString("includingGST", null) == null) ? false : Boolean.parseBoolean(paramJobj.getString("includingGST")));
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("shipdate", null))) {
                qDataMap.put("shipdate", df.parse(paramJobj.getString("shipdate")));
            } else {
                qDataMap.put("shipdate", null);
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("validdate", null))) {
                qDataMap.put("validdate", df.parse(paramJobj.getString("validdate")));
            } else {
                qDataMap.put("validdate", null);
            }

            qDataMap.put("shipvia", paramJobj.optString("shipvia", null));
            qDataMap.put("shippingterm", paramJobj.optString("shippingterm", null));
            qDataMap.put("fob", paramJobj.optString("fob", null));
            qDataMap.put("currencyid", currencyid);
            qDataMap.put("isfavourite", paramJobj.optString("isfavourite", null));
            qDataMap.put("shipaddress", paramJobj.optString("shipaddress", null));
            qDataMap.put("billto", paramJobj.optString("billto", null));
            qDataMap.put("istemplate", istemplate);
            qDataMap.put("isLeaseFixedAsset", isLeaseFixedAsset);
            qDataMap.put("salesPerson", paramJobj.optString("salesPerson", null));
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                qDataMap.put("costCenterId", costCenterId);
            }
            qDataMap.put("companyid", companyid);
            qDataMap.put("createdby", createdby);
            qDataMap.put("modifiedby", modifiedby);
            qDataMap.put("createdon", createdon);
            qDataMap.put("updatedon", updatedon);
            qDataMap.put("customerporefno", paramJobj.optString("customerporefno", null));
            qDataMap.put("profitMargin", paramJobj.optString("profitMargin", null));
            qDataMap.put("profitMarginPercent", paramJobj.optString("profitMarginPercent", null));
            double subTotalAmount = 0, taxAmount = 0, totalTermAmount = 0, totalAmount = 0, totalAmountinbase = 0, totalRowDiscount = 0;
            JSONArray jArr = new JSONArray(paramJobj.getString(Constants.detail));

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);

                double qrate = authHandler.roundUnitPrice(jobj.getDouble("rate"), companyid);
                double quantity = authHandler.roundQuantity(jobj.getDouble("quantity"), companyid);
                boolean gstIncluded = paramJobj.optString("includingGST", null) == null ? false : Boolean.parseBoolean(paramJobj.optString("includingGST"));
                if (gstIncluded) {
                    qrate = authHandler.roundUnitPrice(jobj.getDouble("rateIncludingGst"), companyid);
                }

                double quotationPrice = authHandler.round(quantity * qrate, companyid);
                double discountQD = authHandler.round(jobj.optDouble("prdiscount", 0), companyid);
                double discountPerRow = 0;

                if (jobj.optInt("discountispercent", 0) == 1) {
                    discountPerRow = authHandler.round((quotationPrice * discountQD / 100), companyid);
                } else {
                    discountPerRow = discountQD;
                }

                totalRowDiscount += discountPerRow;

            }
            totalRowDiscount = authHandler.round(totalRowDiscount, companyid);
            qDataMap.put("totallineleveldiscount", totalRowDiscount);
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("subTotal", null))) {
                subTotalAmount = Double.parseDouble(paramJobj.getString("subTotal"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxamount", null))) {
                taxAmount = Double.parseDouble(paramJobj.getString("taxamount"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("invoicetermsmap", null))) {
                JSONArray termDetailsJArr = new JSONArray(paramJobj.getString("invoicetermsmap"));
                for (int cnt = 0; cnt < termDetailsJArr.length(); cnt++) {
                    JSONObject temp = termDetailsJArr.getJSONObject(cnt);
                    if (temp != null) {
                        double termAmount = Double.parseDouble(temp.getString("termamount"));
                        totalTermAmount += termAmount;
                    }
                }
            }
            totalAmount = subTotalAmount + taxAmount + totalTermAmount;
            
            //Rounding Adjustment will always calculated after calculation of totalInvAmount
            double roundingadjustmentAmount = 0.0, roundingadjustmentAmountinbase = 0.0;
            String roundingAdjustmentAccountID = "";
            String columnPref = extraCompanyPreferences.getColumnPref();
            if (!StringUtil.isNullOrEmpty(columnPref)) {
                JSONObject prefObj = new JSONObject(columnPref);
                roundingAdjustmentAccountID = prefObj.optString(Constants.RoundingAdjustmentAccountID, "");
            }
           
            
            if (isRoundingAdjustmentApplied && !StringUtil.isNullOrEmpty(roundingAdjustmentAccountID)) {
                double totalInvAmountAfterRound = Math.round(totalAmount);
                roundingadjustmentAmount = authHandler.round(totalInvAmountAfterRound - totalAmount, companyid);
                if (roundingadjustmentAmount != 0) {
                    totalAmount = totalInvAmountAfterRound;//Now rounded value becomes total quotation amount
                    qDataMap.put(Constants.roundingadjustmentamount, roundingadjustmentAmount);
                    qDataMap.put(Constants.roundingadjustmentamountinbase, roundingadjustmentAmount);

                    String globalcurrency = paramJobj.getString(Constants.globalCurrencyKey);
                    if (!globalcurrency.equalsIgnoreCase(currencyid)) {
                        HashMap<String, Object> roundingRequestParams = new HashMap<String, Object>();
                        roundingRequestParams.put("companyid", companyid);
                        roundingRequestParams.put("gcurrencyid", (paramJobj.optString(Constants.currencyKey, null) == null ? paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.getString(Constants.currencyKey)));
                        KwlReturnObject baseAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(roundingRequestParams, roundingadjustmentAmount, currencyid, df.parse(paramJobj.optString("billdate")), externalCurrencyRate);
                        roundingadjustmentAmountinbase = authHandler.round((Double) baseAmt.getEntityList().get(0), companyid);
                        qDataMap.put(Constants.roundingadjustmentamountinbase, roundingadjustmentAmountinbase);
                    }
                }
            }
            qDataMap.put(Constants.IsRoundingAdjustmentApplied, isRoundingAdjustmentApplied);
            
            
            qDataMap.put("quotationamount", totalAmount);

            HashMap<String, Object> filterRequestParams = new HashMap<>();
            filterRequestParams.put("companyid", companyid);
            filterRequestParams.put("gcurrencyid", paramJobj.getString(Constants.globalCurrencyKey));
            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalAmount, currencyid, df.parse(paramJobj.optString("billdate", null)), externalCurrencyRate);
            totalAmountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
            qDataMap.put("quotationamountinbase", totalAmountinbase);

            KwlReturnObject descbAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalRowDiscount, currencyid, df.parse(paramJobj.optString("billdate", null)), externalCurrencyRate);
            double descountinBase = authHandler.round((Double) descbAmtTax.getEntityList().get(0), companyid);

            qDataMap.put("discountinbase", descountinBase);

            if (!StringUtil.isNullOrEmpty(shipLength)) {
                qDataMap.put("shipLength", shipLength);
            }
            if (!StringUtil.isNullOrEmpty(invoicetype)) {
                qDataMap.put("invoicetype", invoicetype);
            }
            String replacementId = "";

            if (isLinkedFromReplacementNumber && !StringUtil.isNullOrEmpty(paramJobj.optString("replacementId", null))) {
                String[] replacementIdArray = paramJobj.optString("replacementId", "").split(",");
                replacementId = replacementIdArray[0]; // only single select option will be true in linking combo
            }

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("replacementId", null))) {
                qDataMap.put("isLinkedFromReplacementNumber", isLinkedFromReplacementNumber);

                ProductReplacement productReplacement = (ProductReplacement) kwlCommonTablesDAOObj.getClassObject(ProductReplacement.class.getName(), replacementId);
                if (productReplacement != null) {
                    Contract contract = productReplacement.getContract();

                    if (contract != null) {
                        qDataMap.put("contractid", contract.getID());
                    }
                }
            }

            if (taxid != null && !taxid.isEmpty()) {
                Tax tax = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxid);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                }
                qDataMap.put("taxid", taxid);
            } else {
                qDataMap.put("taxid", taxid); // Put taxid as null if the CQ doesnt have any total tax included. (To avoid problem while editing CQ)
            }

            String quotationTerms = paramJobj.optString("invoicetermsmap", null);
            if (StringUtil.isAsciiString(quotationTerms)) {
                if (new JSONArray(quotationTerms).length() > 0) {
                    qDataMap.put(Constants.termsincludegst, Boolean.parseBoolean(paramJobj.getString(Constants.termsincludegst)));
                }
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("formtypeid")) && Constants.indian_country_id == Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID())) {
                qDataMap.put("formtypeid", paramJobj.optString("formtypeid"));
                qDataMap.put("gstapplicable", paramJobj.optBoolean("GSTApplicable"));
                qDataMap.put(Constants.RCMApplicable, RCMApplicable);
                qDataMap.put(Constants.isMerchantExporter, paramJobj.optBoolean(Constants.isMerchantExporter, false));
            } else if (extraCompanyPreferences.isIsNewGST()) {
                /**
                 * ERP-32829 
                 */
                qDataMap.put("gstapplicable", paramJobj.optBoolean("GSTApplicable"));
                qDataMap.put(Constants.RCMApplicable, RCMApplicable);
                qDataMap.put(Constants.isMerchantExporter, paramJobj.optBoolean(Constants.isMerchantExporter, false));
            }
            qDataMap.put(Constants.isApplyTaxToTerms, paramJobj.optBoolean(Constants.isApplyTaxToTerms,false));
            Boolean isreserveStockQuantity = Boolean.parseBoolean(paramJobj.optString("isreserveStockQuantity", ""));   //get the value of lock quantity 
            qDataMap.put("isreserveStockQuantity", isreserveStockQuantity);// lock quantity true or not
            KwlReturnObject soresult = accSalesOrderDAOobj.saveQuotation(qDataMap);
            quotation = (Quotation) soresult.getEntityList().get(0);
            /**
             * Save GST History Customer/Vendor data.
             */
            if (quotation.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                paramJobj.put("docid", quotation.getID());
                paramJobj.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                fieldDataManagercntrl.createRequestMapToSaveDocHistory(paramJobj);
            }
            qDataMap.put("id", quotation.getID());
//            if (countryid == Constants.indian_country_id && customer != null && customer.getGSTRegistrationType() != null) {
//                MasterItem gstRegistrationType = customer.getGSTRegistrationType();
//                if (gstRegistrationType != null && gstRegistrationType.getDefaultMasterItem() != null && !StringUtil.isNullOrEmpty(gstRegistrationType.getDefaultMasterItem().getID())) {
//                    paramJobj.put("isUnRegisteredDealer", gstRegistrationType.getDefaultMasterItem().getID().equals(Constants.GSTRegType.get(Constants.GSTRegType_Unregistered)));;
//                }
//            }
            HashSet sodetails = saveQuotationRows(paramJobj, quotation, companyid);
            quotation.setRows(sodetails);

            String linkedDocuments = "";
            String linkFrom = paramJobj.optString("linkFrom", null);
            HashMap<String, Object> requestParams1 = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(linkFrom) && linkFrom.equalsIgnoreCase("Vendor Quotation")) {
                /*
                 * ID of VQ while linking with CQ at the time of creating CQ
                 */
                String[] linkNumbers = paramJobj.optString("linkNumber", "").split(",");
                if (linkNumbers.length > 0) {
                    for (int i = 0; i < linkNumbers.length; i++) {
                        if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {

                            /*
                             * Saving linking information in Vendor Quotation
                             * linking table
                             */
                            requestParams1.put("linkeddocid", quotation.getID());
                            requestParams1.put("docid", linkNumbers[i]);
                            requestParams1.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                            requestParams1.put("linkeddocno", entryNumber);
                            requestParams1.put("sourceflag", 0);
                            KwlReturnObject result = accSalesOrderDAOobj.updateEntryInVendorQuotationLinkingTable(requestParams1);

                            /*
                             * Saving linking information in Customer Quotation
                             * linking table
                             */
                            requestParams1.clear();
                            result = accSalesOrderDAOobj.getVendorQuotationNumber(linkNumbers[i]);
                            String VendorQuotatioNo = result.getEntityList().get(0).toString();
                            requestParams1.put("linkeddocid", linkNumbers[i]);
                            requestParams1.put("docid", quotation.getID());
                            requestParams1.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
                            requestParams1.put("linkeddocno", VendorQuotatioNo);
                            requestParams1.put("sourceflag", 1);
                            result = accSalesOrderDAOobj.saveQuotationLinking(requestParams1);
                            linkedDocuments += VendorQuotatioNo + " ,";

                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(linkedDocuments)) {
                    linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                }
            }

            // Save record as template
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("templatename", null)) && (istemplate == 1 || istemplate == 2)) {
                HashMap<String, Object> hashMap = new HashMap<>();
                int nocount = 0;
                String moduletemplateid = paramJobj.getString("moduletemplateid");
                hashMap.put("templatename", paramJobj.getString("templatename"));
                 if (!StringUtil.isNullOrEmpty(moduletemplateid)) {
                    hashMap.put("moduletemplateid", moduletemplateid);
                }
                hashMap.put("companyunitid", paramJobj.optString("companyunitid", ""));
                hashMap.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                hashMap.put("modulerecordid", quotation.getID());
                hashMap.put("companyid", companyid);
                if(!StringUtil.isNullOrEmpty(paramJobj.optString("companyunitid",null))){
                    hashMap.put("companyunitid", paramJobj.getString("companyunitid")); // Added Unit ID if it is present in request
                }
                 /**
                 * checks the template name is already exist in create and edit template case
                 */
                templateResult = accountingHandlerDAOobj.getModuleTemplateForTemplatename(hashMap);
                nocount = templateResult.getRecordTotalCount();
                if (nocount > 0) {
                    throw new AccountingException(messageSource.getMessage("acc.tmp.templateNameAlreadyExists", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
                accountingHandlerDAOobj.saveModuleTemplate(hashMap);
            }

            if (isEdit == true) { // For Audit Trial-ERP-14034
                // ERP-14034 
                DateFormat sdf = authHandler.getUserDateFormatterJson(paramJobj);
                if (quotation.getShipdate() != null) {
                    qDataMap.put("AuditShipDate", sdf.format(quotation.getShipdate())); // New Ship Date
                } else {
                    qDataMap.put("AuditShipDate", "");
                }
                if (quotation.getQuotationDate() != null) {
                    qDataMap.put("AuditOrderdate", sdf.format(quotation.getQuotationDate())); // New Ship Date
                } else {
                    qDataMap.put("AuditOrderdate", "");
                }
                if (quotation.getValiddate() != null) {
                    qDataMap.put("AuditValiddate", sdf.format(quotation.getValiddate())); // New Ship Date
                } else {
                    qDataMap.put("AuditValiddate", "");
                }
            }

            if (StringUtil.isAsciiString(quotationTerms) && !extraCompanyPreferences.getCompany().getCountry().getID().equals(Constants.indian_country_id)) {
                mapInvoiceTerms(quotationTerms, quotation.getID(), paramJobj.getString(Constants.useridKey), true);
            }
            // Product to Discount Mapping
            Set<QuotationDetail> qdetails = (Set<QuotationDetail>) sodetails;
            JSONArray productDiscountJArr = new JSONArray();
            for (QuotationDetail qDetail : qdetails) {
                String productId = qDetail.getProduct().getID();
                double discountVal = qDetail.getDiscount();
                int isDiscountPercent = qDetail.getDiscountispercent();
                if (isDiscountPercent == 1) {
                    discountVal = (qDetail.getQuantity() * qDetail.getRate()) * (discountVal / 100);
                }
                KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, df.parse(paramJobj.optString("billdate", null)), externalCurrencyRate);
                double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                discAmountinBase = authHandler.round(discAmountinBase, companyid);
                JSONObject productDiscountObj = new JSONObject();
                productDiscountObj.put("productId", productId);
                productDiscountObj.put("discountAmount", discAmountinBase);
                productDiscountJArr.put(productDiscountObj);
            }


            String customfield = paramJobj.optString("customfield", null);
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_Quotation_modulename);
                customrequestParams.put("moduleprimarykey", "QuotationId");
                customrequestParams.put("modulerecid", quotation.getID());
                customrequestParams.put("moduleid", isLeaseFixedAsset ? Constants.Acc_Lease_Quotation : Constants.Acc_Customer_Quotation_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_Quotation_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    qDataMap.put("accquotationcustomdataref", quotation.getID());
                    KwlReturnObject accresult = accSalesOrderDAOobj.updateQuotationCustomData(qDataMap);
                }
            }

            // Check for approval rules and apply rules if available
            String butPendingForApproval = "";
            int approvalStatusLevel = 11;
            List approvedlevel = null;
            double totalProfitMargin = 0;
            double totalProfitMarginPerc = 0;
            HashMap<String, Object> qApproveMap = new HashMap<>();
            totalProfitMargin = quotation.getTotalProfitMargin();
            totalProfitMarginPerc = quotation.getTotalProfitMarginPercent();
            int level = (isEdit && !isCopy) ? 0 : quotation.getApprovestatuslevel();
            qApproveMap.put("companyid", paramJobj.getString(Constants.companyKey));
            qApproveMap.put("level", level);
            qApproveMap.put("totalAmount", String.valueOf(authHandler.round(totalAmountinbase, companyid)));
            qApproveMap.put("totalProfitMargin", totalProfitMargin);
            qApproveMap.put("totalProfitMarginPerc", totalProfitMarginPerc);
            qApproveMap.put("currentUser", currentUser);
            qApproveMap.put("fromCreate", true);
            qApproveMap.put("productDiscountMapList", productDiscountJArr);
            qApproveMap.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);

                                  
            boolean ismailApplicable = false;

            boolean sendPendingDocumentsToNextLevel = false;
            boolean pendingMessage = true;

            JSONObject columnprefObj = new JSONObject();
            if (!StringUtil.isNullOrEmpty(columnPref)) {
                columnprefObj = new JSONObject(columnPref);
            }

            if (columnprefObj.has("sendPendingDocumentsToNextLevel") && columnprefObj.get("sendPendingDocumentsToNextLevel") != null && (Boolean) columnprefObj.get("sendPendingDocumentsToNextLevel") != false) {
                sendPendingDocumentsToNextLevel = true;
            }

            /*------If pending document is Edited & Check is activated from system preferences
             *---Then Edit will work same as while approving document
             */
            if (isEditedPendingDocument) {
                level = quotation.getApprovestatuslevel();
                qApproveMap.put("fromCreate", false);
                qApproveMap.put("documentLevel", level);
                if (sendPendingDocumentsToNextLevel) {

                    ismailApplicable = true;
                    qApproveMap.put("level", level);
                    pendingMessage = false;

                } else {//Sending Parameter in approve function if "Send approval documents to next level" check is disabled from system preferences
                    qApproveMap.put("isEditedPendingDocumentWithCheckOff", true);
                }
            }    
            qApproveMap.put(Constants.PAGE_URL, paramJobj.optString(Constants.PAGE_URL));
                                                           
            if (!(isLeaseFixedAsset || isDraft) && istemplate != 1 && istemplate != 2) { // !isDraft = if customer you are saving quotation as draft then no need to apply aprroval rule because draft means it does not exist in the system.
                approvedlevel = approveCustomerQuotation(quotation, qApproveMap, ismailApplicable);
                approvalStatusLevel = (Integer) approvedlevel.get(0);
                mailParams = (List) approvedlevel.get(1);
            } else {
                quotation.setApprovestatuslevel(11);
            }
            
            List approvalHistoryList = null;
            String roleName = "";
            boolean isAuthorityToApprove = false;


            /*-----Block is executed when Edited pending Document & Check "Send pending documents to next level" is activated-------*/
            /*------If pending document is Edited & Check is activated from system preferences
             *---Then Edit will work same as while approving document
             */
            if (isEditedPendingDocument && sendPendingDocumentsToNextLevel) {

                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

                /*---Document will approve as approval level -----  */
                if (approvalStatusLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {

                    HashMap emailMap = new HashMap();

                    String userName = paramJobj.optString(Constants.username, null);
                    emailMap.put("userName", userName);
                    KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                    Company company = (Company) returnObject.getEntityList().get(0);
                    emailMap.put("company", company);
                    emailMap.put("customerQuotation", quotation);
                    emailMap.put("baseUrl", paramJobj.optString("baseUrl", null));
                    emailMap.put("preferences", preferences);

                    sendApprovalMailForCQIfAllowedFromSystemPreferences(emailMap);

                }

                /*--------Save Approval history Code--------  */
                if (approvalStatusLevel != Constants.NoAuthorityToApprove) {

                    HashMap approvalHistoryMap = new HashMap();

                    KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                    Company company = (Company) returnObject.getEntityList().get(0);
                    approvalHistoryMap.put("company", company);
                    approvalHistoryMap.put("customerQuotation", quotation);
                    String userid = paramJobj.optString(Constants.userid, null);
                    approvalHistoryMap.put("userid", userid);

                    approvalHistoryList = saveApprovalHistoryForCQ(approvalHistoryMap);
                    roleName = approvalHistoryList != null ? approvalHistoryList.get(0).toString() : "";
                    isAuthorityToApprove = true;

                } else {
                    /*----If User have no authority to approve------  */
                    isAuthorityToApprove = false;
                }
            }
            
            
            if (approvalStatusLevel != 11 && pendingMessage) {
                butPendingForApproval = messageSource.getMessage("acc.field.butpendingforApproval", null, Locale.forLanguageTag(paramJobj.getString("language")));
            }
            
            /*-------If  "Send pending documents to next level" check is OFF and User have no authority to approve--------- */
            if (isEditedPendingDocument && !sendPendingDocumentsToNextLevel && approvalStatusLevel == Constants.NoAuthorityToApprove) {
                isAuthorityToApprove = false;
                butPendingForApproval = "";
            }

            if (isEdit == true) {
                int moduleid = Constants.Acc_Customer_Quotation_ModuleId;
                additionalsauditmessage = AccountingManager.BuildAuditTrialMessage(qDataMap, oldquotationMap, moduleid, newAuditKey);
            }

            returnList.add(quotation);
            returnList.add(additionalsauditmessage); // Audit Trial Message
            returnList.add(totalAmount);
            returnList.add(approvalStatusLevel);
            returnList.add(butPendingForApproval);
            returnList.add(mailParams);
            returnList.add(unlinkMessage);
            returnList.add(linkedDocuments);
            returnList.add(roleName);
            returnList.add(isAuthorityToApprove);
            returnList.add(sendPendingDocumentsToNextLevel);
            String moduleName = Constants.moduleID_NameMap.get(Constants.Acc_Customer_Quotation_ModuleId);
            if (isLeaseFixedAsset) {
                moduleName = Constants.moduleID_NameMap.get(Constants.Acc_Lease_Quotation);
            }
            // Send Mail when Customer Quotation is generated or modified.
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), paramJobj.getString(Constants.companyKey));
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (documentEmailSettings != null) {
                boolean sendmail = false;
                boolean isEditMail = false;
                if (StringUtil.isNullOrEmpty(quotationId)) {
                    if (isLeaseFixedAsset && documentEmailSettings.isLeaseQuotationGenerationMail()) {
                        sendmail = true;
                    } else if (documentEmailSettings.isCustomerQuotationGenerationMail()) {
                        sendmail = true;
                    }
                } else {
                    isEditMail = true;
                    if (isLeaseFixedAsset && documentEmailSettings.isLeaseQuotationUpdationMail()) {
                        sendmail = true;
                    } else if (documentEmailSettings.isCustomerQuotationUpdationMail()) {
                        sendmail = true;
                    }
                }
                if (sendmail) { // if allow to send alert mail when option selected in companypreferences
                    String userMailId = "", userName = "",currentUserid="";
                    String createdByEmail = "";
                    String createdById = "";
                    HashMap<String, Object> requestParams = AccountingManager.getEmailNotificationParamsJson(paramJobj);
                    if (requestParams.containsKey("userfullName") && requestParams.get("userfullName") != null) {
                        userName = (String) requestParams.get("userfullName");
                    }
                    if (requestParams.containsKey("usermailid") && requestParams.get("usermailid") != null) {
                        userMailId = (String) requestParams.get("usermailid");
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
                        if (quotation != null && quotation.getCreatedby() != null) {
                            createdByEmail = quotation.getCreatedby().getEmailID();
                            createdById = quotation.getCreatedby().getUserID();
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
                    String vqNumber = entryNumber;
                    accountingHandlerDAOobj.sendSaveTransactionEmails(vqNumber, moduleName, tomailids, userName, isEditMail, companyid);
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveQuotation : " + ex.getMessage(), ex);
        }
        return returnList;
    }
    
    private void deleteAvalaraTaxMappingForQuotation(Set<QuotationDetail> quotationDetailSet) throws JSONException, ServiceException {
        List quotationDetailIDsList = new ArrayList<String>();
        for (QuotationDetail qd : quotationDetailSet) {
            quotationDetailIDsList.add(qd.getID());
        }
        if (!quotationDetailIDsList.isEmpty()) {
            //to create a comma separated string of QuotationDetail IDs for 'IN' subquery
            String quotationDetailIDsStr = org.springframework.util.StringUtils.collectionToDelimitedString(quotationDetailIDsList, ",", "'", "'");
            JSONObject avalaraTaxDeleteJobj = new JSONObject();
            avalaraTaxDeleteJobj.put(IntegrationConstants.parentRecordID, quotationDetailIDsStr);
            integrationCommonService.deleteTransactionDetailTaxMapping(avalaraTaxDeleteJobj);
        }
    }

    // Appending values in Map to build Audit Trial
    public void setValuesForAuditTrialMessage(Quotation oldquotation, JSONObject paramJobj, Map<String, Object> oldquotationMap, Map<String, Object> qDataMap, Map<String, Object> newAuditKey) throws SessionExpiredException, JSONException {
        DateFormat df = authHandler.getUserDateFormatterJson(paramJobj);
        try {
            // Setting values in map for oldgreceipt
            if (oldquotation != null) {
                if (oldquotation.getTerm() != null) {
                    KwlReturnObject oldcredittermresult = accountingHandlerDAOobj.getObject(Term.class.getName(), oldquotation.getTerm().getID());
                    Term term = (Term) oldcredittermresult.getEntityList().get(0);
                    oldquotationMap.put(Constants.CreditTermName, term.getTermname());
                    newAuditKey.put(Constants.CreditTermName, "Credit Term");
                }
                KwlReturnObject currobretrurnlist = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), oldquotation.getCurrency().getCurrencyID());
                KWLCurrency oldcurrencyobj = (KWLCurrency) currobretrurnlist.getEntityList().get(0);
                KwlReturnObject custobretrurnlist = accountingHandlerDAOobj.getObject(Customer.class.getName(), oldquotation.getCustomer().getID());
                Customer oldcustomer = (Customer) custobretrurnlist.getEntityList().get(0);
                if (oldquotation.getSalesperson() != null) {
                    KwlReturnObject oldmasteritemobretrurnlist = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), oldquotation.getSalesperson().getID());
                    MasterItem oldsalesPerson = (MasterItem) oldmasteritemobretrurnlist.getEntityList().get(0);
                    oldquotationMap.put("SalesPerson", oldsalesPerson != null ? oldsalesPerson.getValue() : "");

                } else {
                    oldquotationMap.put("SalesPerson", "");
                }
                newAuditKey.put("SalesPerson", "Sales Person");
                oldquotationMap.put(Constants.CustomerName, oldcustomer.getName());
                newAuditKey.put(Constants.CustomerName, "Customer");
                oldquotationMap.put("entrynumber", oldquotation.getQuotationNumber());
                newAuditKey.put("entrynumber", "Entry Number");
                oldquotationMap.put(Constants.CurrencyName, oldcurrencyobj.getName()); // Currency name
                newAuditKey.put(Constants.CurrencyName, "Currency");
                oldquotationMap.put("memo", StringUtil.isNullOrEmpty(oldquotation.getMemo()) ? "" : oldquotation.getMemo());
                newAuditKey.put("memo", "Memo");
                oldquotationMap.put("shipvia", StringUtil.isNullOrEmpty(oldquotation.getShipvia()) ? "" : oldquotation.getShipvia());
                newAuditKey.put("shipvia", "Ship Via");
                oldquotationMap.put("fob", StringUtil.isNullOrEmpty(oldquotation.getFob()) ? "" : oldquotation.getFob());
                newAuditKey.put("fob", "FOB");
                oldquotationMap.put("AuditShipDate", oldquotation.getShipdate() != null ? df.format(oldquotation.getShipdate()) : "");
                newAuditKey.put("AuditShipDate", "Ship Date");
                oldquotationMap.put("AuditOrderdate", oldquotation.getQuotationDate() != null ? df.format(oldquotation.getQuotationDate()) : "");
                newAuditKey.put("AuditOrderdate", "Entry Date");
                oldquotationMap.put("AuditValiddate", oldquotation.getValiddate() != null ? df.format(oldquotation.getValiddate()) : "");
                newAuditKey.put("AuditValiddate", "Valid Till");
            }

            // Setting values in map for qDataMap-newquotation
            KwlReturnObject debittermresult = accountingHandlerDAOobj.getObject(Term.class.getName(), paramJobj.optString("termid", null));
            Term term = (Term) debittermresult.getEntityList().get(0);
            qDataMap.put(Constants.CreditTermName, term.getTermname()); // Debit Term Name
            String currencyid = (paramJobj.optString(Constants.currencyKey, null) == null) ? paramJobj.getString(Constants.globalCurrencyKey) : paramJobj.getString(Constants.currencyKey);
            KwlReturnObject newcurrencyreturnobj = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency newcurrencyobj = (KWLCurrency) newcurrencyreturnobj.getEntityList().get(0);
            qDataMap.put(Constants.CurrencyName, newcurrencyobj.getName()); // Currencey name
            KwlReturnObject custobretrurnlist = accountingHandlerDAOobj.getObject(Customer.class.getName(), paramJobj.optString("customer", null));
            Customer newcustomer = (Customer) custobretrurnlist.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("salesperson", null))) {
                KwlReturnObject newmasteritemobretrurnlist = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), paramJobj.getString("salesperson"));
                MasterItem salesPerson = (MasterItem) newmasteritemobretrurnlist.getEntityList().get(0);
                qDataMap.put("SalesPerson", salesPerson.getValue()); // SalesPerson Name
            } else {
                qDataMap.put("SalesPerson", ""); // SalesPerson Name
            }
            qDataMap.put(Constants.CustomerName, newcustomer.getName()); // Customer Name

        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveQuotationVersion(JSONObject paramJobj, String quotationid) throws ServiceException, AccountingException {
        try {
            HashMap<String, Object> qDataMap = new HashMap<>();
            KwlReturnObject quotaionres = accountingHandlerDAOobj.getObject(Quotation.class.getName(), quotationid);
            Quotation quotation = (Quotation) quotaionres.getEntityList().get(0);
            boolean isLeaseFixedAsset=quotation.isLeaseQuotation();
            if (quotation.getSeqformat() != null) {
                qDataMap.put(Constants.SEQFORMAT, quotation.getSeqformat().getID());
            }
            qDataMap.put(Constants.SEQNUMBER, quotation.getSeqnumber());
            qDataMap.put(Constants.DATEPREFIX, quotation.getDatePreffixValue());
            qDataMap.put(Constants.DATEAFTERPREFIX, quotation.getDateAfterPreffixValue());
            qDataMap.put(Constants.DATESUFFIX, quotation.getDateSuffixValue());
            if (quotation.getBillingShippingAddresses() != null) {
                qDataMap.put("billshipAddressid", quotation.getBillingShippingAddresses().getID());
            }
            qDataMap.put("externalCurrencyRate", quotation.getExternalCurrencyRate());
            qDataMap.put("entrynumber", quotation.getquotationNumber());
            qDataMap.put("autogenerated", quotation.isAutoGenerated());
            qDataMap.put("memo", quotation.getMemo());
            qDataMap.put("posttext", quotation.getPostText());
            if (quotation.getCustomer() != null) {
                qDataMap.put("customerid", quotation.getCustomer().getID());
            }
            qDataMap.put("orderdate", quotation.getQuotationDate());
            qDataMap.put("duedate", quotation.getDueDate());
            qDataMap.put("perDiscount", quotation.isPerDiscount());
            qDataMap.put("discount", quotation.getDiscount());
            qDataMap.put("gstIncluded", quotation.isGstIncluded());
            qDataMap.put("shipdate", quotation.getShipdate());
            qDataMap.put("validdate", quotation.getValiddate());
            qDataMap.put("shipvia", quotation.getShipvia());
            qDataMap.put("fob", quotation.getFob());
            if (quotation.getCurrency() != null) {
                qDataMap.put("currencyid", quotation.getCurrency().getCurrencyID());
            }
            qDataMap.put("isfavourite", quotation.isFavourite());
            qDataMap.put("shipaddress", quotation.getShipTo());
            qDataMap.put("billto", quotation.getBillTo());
            qDataMap.put("istemplate", quotation.getIstemplate());
            qDataMap.put("isLeaseFixedAsset", quotation.isLeaseQuotation());
            qDataMap.put("salesPerson", quotation.getSalesperson() != null ? quotation.getSalesperson().getID() : null);
            qDataMap.put("companyid", quotation.getCompany().getCompanyID());
            qDataMap.put("createdby", quotation.getCreatedby().getUserID());
            qDataMap.put("modifiedby", quotation.getModifiedby().getUserID());
            qDataMap.put("createdon", quotation.getCreatedon());
            qDataMap.put("updatedon", quotation.getUpdatedon());
            qDataMap.put("shipLength", quotation.getShiplength());
            qDataMap.put("invoicetype", quotation.getInvoicetype());
            qDataMap.put("quotationID", quotation.getID());
            qDataMap.put("isLinkedFromReplacementNumber", quotation.getLinkflag());
            qDataMap.put("contractid", quotation.getContract() == null ? null : quotation.getContract().getID());
            qDataMap.put("taxid", quotation.getTax() == null ? null : quotation.getTax().getID()); // Put taxid as null if the CQ doesnt have any total tax included. (To avoid problem while editing CQ)
            String version = "VN00000";
            KwlReturnObject socnt = accSalesOrderDAOobj.getQuotationVersionCount(quotation.getID(), quotation.getCompany().getCompanyID());
            int count = socnt.getRecordTotalCount();
            qDataMap.put("version", version + (count + 1));
            KwlReturnObject soresult = accSalesOrderDAOobj.saveQuotationVersion(qDataMap);
            QuotationVersion quotationVersion = (QuotationVersion) soresult.getEntityList().get(0);
            qDataMap.put("id", quotationVersion.getID());
            HashSet qoversiondetails = saveQuotationVersionRows(paramJobj, quotation, quotation.getCompany().getCompanyID(), quotationVersion);
            quotationVersion.setRows(qoversiondetails);

            HashMap<String, String> customFieldMap = new HashMap<>();
            HashMap<String, String> customDateFieldMap = new HashMap<>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(quotationVersion.getCompany().getCompanyID(), Constants.Acc_Customer_Quotation_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            JSONArray jcustarray = new JSONArray();
            KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(QuotationCustomData.class.getName(), quotation.getID());
            if (custumObjresult.getEntityList().size() > 0) {
                Map<String, Object> variableMap = new HashMap<>();
                QuotationCustomData quotationCustomData = (QuotationCustomData) custumObjresult.getEntityList().get(0);
                AccountingManager.setCustomColumnValues(quotationCustomData, FieldMap, replaceFieldMap, variableMap);
                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                    JSONObject obj = new JSONObject();
                    String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                    if (customFieldMap.containsKey(varEntry.getKey())) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                        FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                        if (fieldComboData != null) {
                            obj.put(varEntry.getKey(), fieldComboData.getValue() != null ? fieldComboData.getValue() : "");
                        }
                    } else {
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            String[] coldataArray = coldata.split(",");
                            String Coldata = "";
                            for (int countArray = 0; countArray < coldataArray.length; countArray++) {
                                Coldata += "'" + coldataArray[countArray] + "',";
                            }
                            Coldata = Coldata.substring(0, Coldata.length() - 1);
                            String ColValue = accAccountDAOobj.getfieldcombodatabyids(Coldata);
                            obj.put(varEntry.getKey(), coldata);
                            obj.put(varEntry.getKey(), ColValue);
                        }
                    }
                    jcustarray.put(obj);
                }
            }
            String customfield = paramJobj.optString(Constants.customfield, null);
            if (!StringUtil.isNullOrEmpty(customfield)) {
            JSONArray jcustomarray = new JSONArray(customfield);
            for (int j = 0; j < jcustomarray.length(); j++) {
                JSONObject replaceObj = jcustomarray.optJSONObject(j);
                JSONObject tempObj = jcustarray.optJSONObject(j);
                if (tempObj != null && replaceObj != null) {
                    Iterator keyitr = replaceObj.keys();
                    Iterator tempIter = tempObj.keys();
                    while (tempIter.hasNext()) {
                        if (keyitr.next().equals(tempIter.next())) {
                            replaceObj.put(keyitr.next().toString(), tempObj.opt(tempIter.next().toString()));
                        }
                    }
                }
            }
            HashMap<String, Object> customrequestParams = new HashMap<>();
            customrequestParams.put("customarray", jcustomarray);
            customrequestParams.put("modulename", Constants.Acc_Quotation_modulename);
            customrequestParams.put("moduleprimarykey", "QuotationId");
            customrequestParams.put("modulerecid", quotationVersion.getID());
                customrequestParams.put("moduleid", isLeaseFixedAsset ? Constants.Acc_Lease_Quotation : Constants.Acc_Customer_Quotation_ModuleId);
            customrequestParams.put("companyid", quotationVersion.getCompany().getCompanyID());
            customrequestParams.put("customdataclasspath", Constants.Acc_QuotationVersion_custom_data_classpath);
            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                qDataMap.put("accquotationcustomdataref", quotationVersion.getID());
                KwlReturnObject accresult = accSalesOrderDAOobj.updateQuotationVersionCustomData(qDataMap);
            }
            }

        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public HashSet saveQuotationVersionRows(JSONObject paramJobj, Quotation quotation, String companyid, QuotationVersion quotationVersion) throws ServiceException, AccountingException, SessionExpiredException {
        HashSet rows = new HashSet();
        try {
            HashMap<String, Object> soRequestParams = new HashMap<>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("quotation.ID");
            order_by.add("srno");
            order_type.add("asc");
            soRequestParams.put("filter_names", filter_names);
            soRequestParams.put("filter_params", filter_params);
            soRequestParams.put("order_by", order_by);
            soRequestParams.put("order_type", order_type);
            filter_params.clear();
            filter_params.add(quotation.getID());
            KwlReturnObject podresult = accSalesOrderDAOobj.getQuotationDetails(soRequestParams);
            List<QuotationDetail> quotationDetailList = podresult.getEntityList();
            int i = 0;
            for (QuotationDetail row : quotationDetailList) {
                HashMap<String, Object> qdDataMap = new HashMap<>();
                qdDataMap.put("srno", i + 1);
                qdDataMap.put("companyid", companyid);
                qdDataMap.put("soid", quotationVersion.getID());
                qdDataMap.put("productid", row.getProduct().getID());
                qdDataMap.put("rate", row.getRate());
                qdDataMap.put("quantity", row.getQuantity());
                if (row.getUom() != null) {
                    qdDataMap.put("uomid", row.getUom().getID());
                }
                qdDataMap.put("baseuomquantity", row.getBaseuomquantity());
                qdDataMap.put("baseuomrate", row.getBaseuomrate());
                qdDataMap.put("remark", row.getRemark());
                qdDataMap.put("dependentType", row.getDependentType());
                qdDataMap.put("inouttime", row.getInouttime());
                qdDataMap.put("showquantity", row.getShowquantity());
                qdDataMap.put("desc", row.getDescription());
                qdDataMap.put("invstoreid", row.getInvstoreid());
                qdDataMap.put("invlocationid", row.getInvlocid());
                if (row.getProductReplacementDetail() != null) {
                    qdDataMap.put("productreplacementDetailId", row.getProductReplacementDetail().getId());
                }
                qdDataMap.put("discount", row.getDiscount());
                qdDataMap.put("discountispercent", row.getDiscountispercent());
                if (row.getTax() != null) {
                    qdDataMap.put("rowtaxid", row.getTax().getID());
                }
                qdDataMap.put("rowTaxAmount", row.getRowTaxAmount());
                qdDataMap.put("vendorquotationdetails", row.getVendorquotationdetails());
                KwlReturnObject result = accSalesOrderDAOobj.saveQuotationVersionDetails(qdDataMap);
                QuotationVersionDetail qvd = (QuotationVersionDetail) result.getEntityList().get(0);

                HashMap<String, Object> fieldrequestParams = new HashMap();
                HashMap<String, String> customFieldMap = new HashMap<>();
                HashMap<String, String> customDateFieldMap = new HashMap<>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(paramJobj.getString(Constants.companyKey), Constants.Acc_Customer_Quotation_ModuleId, 1));
                HashMap<String, String> replaceFieldMap = new HashMap<>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                Map<String, Object> variableMap = new HashMap<>();
                QuotationDetailCustomData quotationDetailCustomData = (QuotationDetailCustomData) row.getQuotationDetailCustomData();
                AccountingManager.setCustomColumnValues(quotationDetailCustomData, FieldMap, replaceFieldMap, variableMap);
                JSONArray costomArray = new JSONArray();
                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                    JSONObject obj = new JSONObject();
                    String coldata = varEntry.getValue().toString();
                    String valueForReport = "";
                    if (customFieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                        try {
                            String[] valueData = coldata.split(",");
                            for (String value : valueData) {
                                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                                FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                if (fieldComboData != null) {
                                    valueForReport += fieldComboData.getValue() + ",";
                                }
                            }
                            if (valueForReport.length() > 1) {
                                valueForReport = valueForReport.substring(0, valueForReport.length() - 1);
                            }
                            obj.put(varEntry.getKey(), valueForReport);
                        } catch (Exception ex) {
                            obj.put(varEntry.getKey(), coldata);
                        }
                    } else {
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            obj.put(varEntry.getKey(), coldata);
                        }
                    }
                    costomArray.put(costomArray);
                }
                JSONArray jArr = new JSONArray(paramJobj.getString(Constants.detail));
                JSONObject jobj = jArr.optJSONObject(0);
                String customfield = jobj.getString("customfield");
                JSONArray jcustomarray = new JSONArray(customfield);
                for (int j = 0; j < jcustomarray.length(); j++) {
                    JSONObject replaceObj = jcustomarray.optJSONObject(j);
                    JSONObject tempObj = costomArray.optJSONObject(j);
                    if (tempObj != null && replaceObj != null) {
                        Iterator tempIter = tempObj.keys();
                        Iterator keyitr = replaceObj.keys();
                        while (tempIter.hasNext()) {
                            if (keyitr.next().equals(tempIter.next())) {
                                replaceObj.put(keyitr.next().toString(), tempObj.opt(tempIter.next().toString()));
                            }
                        }
                    }
                }
                HashMap<String, Object> DOMap = new HashMap<>();
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "QuotationVersionDetail");
                customrequestParams.put("moduleprimarykey", "QuotationDetailId");
                customrequestParams.put("modulerecid", qvd.getID());
                customrequestParams.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                customrequestParams.put("companyid", companyid);
                DOMap.put("id", qvd.getID());
                customrequestParams.put("customdataclasspath", Constants.Acc_QuotationVersionDetails_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    DOMap.put("qdetailscustomdataref", qvd.getID());
                    accSalesOrderDAOobj.updateQuotationVersionDetailsCustomData(DOMap);
                }
                rows.add(qvd);
                i++;
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveQuotationVersionRows : " + ex.getMessage(), ex);
        }
        return rows;
    }

    @Override
    public List<String> approveCustomerQuotation(Quotation doObj, HashMap<String, Object> qApproveMap, boolean isMailApplicable) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException, JSONException {
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
        double totalProfitMargin = 0;
        if (qApproveMap.containsKey("totalProfitMargin") && qApproveMap.get("totalProfitMargin") != null) {
            totalProfitMargin = Double.parseDouble(qApproveMap.get("totalProfitMargin").toString());
        }
        double totalProfitMarginPerc = 0;
        if (qApproveMap.containsKey("totalProfitMarginPerc") && qApproveMap.get("totalProfitMarginPerc") != null) {
            totalProfitMarginPerc = Double.parseDouble(qApproveMap.get("totalProfitMarginPerc").toString());
        }
        JSONArray productDiscountMapList = null;
        if (qApproveMap.containsKey("productDiscountMapList") && qApproveMap.get("productDiscountMapList") != null) {
            productDiscountMapList = new JSONArray(qApproveMap.get("productDiscountMapList").toString());
        }
        if (!fromCreate) {
            String thisUser = currentUser;
            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
            User user = (User) userclass.getEntityList().get(0);

            if (AccountingManager.isCompanyAdmin(user)) {
                hasAuthority = true;
            } else {
                
                /*
                 If "Send approval documents to next level" is disabled from system preferences & pending document is edited then
                 1. When user is authorised then document is always goes at first level
                 2. When user is not authorised then document remains at same level
                 
                 */
                boolean isEditedPendingDocumentWithCheckOff = false;
                if (qApproveMap.containsKey("isEditedPendingDocumentWithCheckOff") && qApproveMap.get("isEditedPendingDocumentWithCheckOff") != null) {
                    level = Integer.parseInt(qApproveMap.get("documentLevel").toString());//Actual level of document for fetching rule at that level for the user
                    qApproveMap.put("level", level);
                    isEditedPendingDocumentWithCheckOff = true;
                }
                
                hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(qApproveMap);
                
                /*---If User is authorised at this level then sending document to first level that's why assigning "level=0" ------ */
                if (isEditedPendingDocumentWithCheckOff && hasAuthority) {
                    level = 0;
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
            String cqID = doObj.getID();
            HashMap<String, Object> qdDataMap = new HashMap<>();
            qdDataMap.put("companyid", companyid);
            qdDataMap.put("level", level + 1);
            qdDataMap.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
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
            mailParameters.put(Constants.moduleid, Constants.Acc_Customer_Quotation_ModuleId);
            mailParameters.put(Constants.createdBy, doObj.getCreatedby().getUserID());
            if (qApproveMap.containsKey(Constants.PAGE_URL)) {
                mailParameters.put(Constants.PAGE_URL, (String) qApproveMap.get(Constants.PAGE_URL));
            }
            Iterator itr = flowresult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                mailParameters.put(Constants.ruleid, row[0].toString());
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
                if (appliedUpon == Constants.Profit_Margin_Amount) {
                    rule = rule.replaceAll("[$$]+", String.valueOf(totalProfitMargin));
                } else if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount) {
                    if (productDiscountMapList != null) {
                        sendForApproval = AccountingManager.checkForProductAndProductDiscountRule(productDiscountMapList, appliedUpon, rule, discountRule);
                    }
                }else if(appliedUpon ==Constants.Specific_Products_Category){
                    /*
                     * Check If Rule is apply on product
                     * category from multiapproverule window
                     */
                    sendForApproval = accountingHandlerDAOobj.checkForProductCategoryForProduct(productDiscountMapList, appliedUpon, rule);
                }else {
                    rule = rule.replaceAll("[$$]+", amount);
                }
                if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && appliedUpon != Constants.Specific_Products && appliedUpon != Constants.Specific_Products_Discount && appliedUpon != Constants.Specific_Products_Category && Boolean.parseBoolean(engine.eval(rule).toString())) || sendForApproval) {
                    // send emails
                    boolean hasApprover = Boolean.parseBoolean(row[3].toString());
                    mailParameters.put(Constants.hasApprover, hasApprover);
                    if (isMailApplicable) {
//                        approvalStatus = level + 1;
                        mailParameters.put("level",level);
                        sendMailToApprover(mailParameters);
                        approvalStatus = level + 1;
                    } else {
                        /*
                         * This block will be executed if any CQ will go for
                         * pending approval
                         */
                        approvalStatus = level + 1;
                        recMap.put("ruleid", row[0].toString());
                        recMap.put("fromName", fromName);
                        recMap.put("hasApprover", hasApprover);

                        mailParamList.add(recMap);
                    }
                }
            }
            accSalesOrderDAOobj.approvePendingCustomerQuotation(cqID, companyid, approvalStatus);
            returnStatus = approvalStatus;
        } else {
            returnStatus = Constants.NoAuthorityToApprove; // if not have approval permission then return one fix value like 999
        }
        returnList.add(returnStatus);
        returnList.add(mailParamList);

        return returnList;
    }

    public HashSet saveQuotationRows(JSONObject paramJobj, Quotation quotation, String companyid) throws ServiceException, AccountingException {
        HashSet rows = new HashSet();
        try {
            JSONArray jArr = new JSONArray(paramJobj.getString(Constants.detail));
            boolean isLinkedFromReplacementNumber = (!StringUtil.isNullOrEmpty(paramJobj.optString("isLinkedFromReplacementNumber", null))) ? Boolean.parseBoolean(paramJobj.getString("isLinkedFromReplacementNumber")) : false;
            boolean includeProductTax = StringUtil.isNullOrEmpty(paramJobj.optString("includeprotax", null)) ? false : Boolean.parseBoolean(paramJobj.getString("includeprotax"));
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString("isLeaseFixedAsset", null))) ? Boolean.parseBoolean(paramJobj.getString("isLeaseFixedAsset")) : false;
            boolean RCMApplicable = paramJobj.optBoolean("GTAApplicable",false);
            String countryid = extraCompanyPreferences.getCompany().getCountry().getID();
            Set<String> productNameRCMNotActivate = new HashSet<String>();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> qdDataMap = new HashMap<>();

                if (jobj.has("srno")) {
                    qdDataMap.put("srno", jobj.getInt("srno"));
                }
                /**
                 * IF RCM Applicable and Product is not RCM
                 * Applicable
                 */
                Product product = (Product) kwlCommonTablesDAOObj.getClassObject(Product.class.getName(), jobj.getString("productid"));
                if (Integer.parseInt(countryid) == Constants.indian_country_id && RCMApplicable) {
                    if (product != null && !product.isRcmApplicable()) {
                        productNameRCMNotActivate.add(product.getName());
                        //throw new AccountingException(messageSource.getMessage("acc.common.rcmforproductnotactivated.CQ.text", new Object[]{product.getName()}, null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
                qdDataMap.put("companyid", companyid);
                qdDataMap.put("soid", quotation.getID());
                qdDataMap.put("productid", jobj.getString("productid"));
                qdDataMap.put("rate", jobj.getDouble("rate"));
                if (jobj.has("priceSource")) {
                    qdDataMap.put("priceSource", !StringUtil.isNullOrEmpty(jobj.optString("priceSource")) ? StringUtil.DecodeText(jobj.optString("priceSource")) : "");
                }
                if (jobj.has("pricingbandmasterid")) {
                    qdDataMap.put("pricingbandmasterid", !StringUtil.isNullOrEmpty(jobj.optString("pricingbandmasterid")) ? StringUtil.DecodeText(jobj.optString("pricingbandmasterid")) : "");
                }
                if (jobj.has("rateIncludingGst")) {
                    qdDataMap.put("rateIncludingGst", jobj.optDouble("rateIncludingGst", 0));
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
                qdDataMap.put("remark", jobj.optString("remark"));
                if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) { // This is sats specific code  
                    if (jobj.has("dependentType")) {
                        qdDataMap.put("dependentType", StringUtil.isNullOrEmpty(jobj.getString("dependentType")) ? jobj.getString("dependentTypeNo") : jobj.getString("dependentType"));
                    }
                    if (jobj.has("inouttime")) {
                        qdDataMap.put("inouttime", !StringUtil.isNullOrEmpty(jobj.getString("inouttime")) ? jobj.getString("inouttime") : "");
                    }
                    if (jobj.has("showquantity")) {
                        qdDataMap.put("showquantity", !StringUtil.isNullOrEmpty(jobj.getString("showquantity")) ? jobj.getString("showquantity") : "");
                    }
                }
//                try {
                    qdDataMap.put("desc", StringUtil.DecodeText(jobj.optString("desc")));
//                } catch (Exception ex) {
//                    qdDataMap.put("desc", jobj.optString("desc"));
//                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invstore"))) {
                    qdDataMap.put("invstoreid", jobj.optString("invstore"));
                } else {
                    qdDataMap.put("invstoreid", "");
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invlocation"))) {
                    qdDataMap.put("invlocationid", jobj.optString("invlocation"));
                } else {
                    qdDataMap.put("invlocationid", "");
                }

                String linkMode = paramJobj.optString("fromLinkCombo", null);

                if (!StringUtil.isNullOrEmpty(linkMode)) {
                    if (isLinkedFromReplacementNumber) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(ProductReplacementDetail.class.getName(), jobj.getString("rowid"));
                        if (!rdresult.getEntityList().isEmpty()) {
                            ProductReplacementDetail pr = (ProductReplacementDetail) rdresult.getEntityList().get(0);
                            qdDataMap.put("productreplacementDetailId", pr.getId());
                        }
                    }
                }

                qdDataMap.put("discount", jobj.optDouble("prdiscount", 0));
                qdDataMap.put("discountispercent", jobj.optInt("discountispercent", 0));
                if (jobj.has("discountjson")) {
                    String discountjson = jobj.optString("discountjson", "");
                    discountjson = !StringUtil.isNullOrEmpty(discountjson) ? StringUtil.decodeString(discountjson) : "";
                    qdDataMap.put("discountjson", discountjson);
                }
                String rowtaxid = "";
                if (!StringUtil.isNullOrEmpty(jobj.optString("prtaxid", null)) && jobj.optString("prtaxid").equalsIgnoreCase("None")) {
                    rowtaxid = null;    
                } else {
                    rowtaxid = jobj.optString("prtaxid",null);
                }
                if (!StringUtil.isNullOrEmpty(rowtaxid) && includeProductTax) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    double rowtaxamount = StringUtil.getDouble(jobj.getString("taxamount"));
                    if (rowtax == null) {
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                    } else {
                        qdDataMap.put("rowtaxid", rowtaxid);
                        qdDataMap.put("rowTaxAmount", rowtaxamount);
                        qdDataMap.put(Constants.isUserModifiedTaxAmount, jobj.optBoolean(Constants.isUserModifiedTaxAmount, false));
                    }
                    
//                    double lineleveltaxtermamount = 0d;
//                    double lineleveltaxtermamountInBase = 0d;
//                    lineleveltaxtermamount = StringUtil.getDouble(jobj.optString("lineleveltaxtermamount","0"));
//                    qdDataMap.put("rowtermtaxamount", lineleveltaxtermamount);
                }

                String linkmode = paramJobj.optString("linkNumber", null);
                String linkFrom = paramJobj.optString("linkFrom", null);
                String linkto = jobj.getString("linkto");
                boolean isEdit = paramJobj.optBoolean("isEdit", false);

                if (isEdit) {
                    /*
                     * If we linking document (that was already linked with
                     * another document) in Edit mode i.e linking VQ->CQ(VQ
                     * already linked with PR or RFQ) then linkto is setting
                     * same as while creating document because it is same as
                     * while creating new document by linking
                     *
                     */
                    if ((!StringUtil.isNullOrEmpty(jobj.optString("savedrowid",null)))) {
                        List list = accSalesOrderDAOobj.getVQdetails(jobj.getString("savedrowid"), companyid);
                        if (list == null || list.isEmpty()) {
                            linkto = "";
                        }
                    }
                }
                /*
                 * linkto flag is not empty if Vendor Quotation is * linked with
                 * PR otherwise it is empty
                 */
                if ((!StringUtil.isNullOrEmpty(linkmode)) && (!StringUtil.isNullOrEmpty(jobj.getString("rowid"))) && linkFrom.equalsIgnoreCase("Vendor Quotation")) {
                    qdDataMap.put("vendorquotationdetails", StringUtil.isNullOrEmpty(linkto) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("recTermAmount"))) {
                    qdDataMap.put("recTermAmount", jobj.optString("recTermAmount"));
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("OtherTermNonTaxableAmount"))) {
                    qdDataMap.put("OtherTermNonTaxableAmount", jobj.optString("OtherTermNonTaxableAmount"));
                }

                KwlReturnObject result = accSalesOrderDAOobj.saveQuotationDetails(qdDataMap);
                QuotationDetail row = (QuotationDetail) result.getEntityList().get(0);
                String customfield = jobj.getString("customfield");
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    HashMap<String, Object> DOMap = new HashMap<>();
                    JSONArray jcustomarray = new JSONArray(customfield);
                    jcustomarray = fieldDataManagercntrl.getComboValueIdsForCurrentModule(jcustomarray, isLeaseFixedAsset ? Constants.Acc_Lease_Quotation : Constants.Acc_Customer_Quotation_ModuleId, companyid, 1);
                    HashMap<String, Object> customrequestParams = new HashMap<>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "QuotationDetail");
                    customrequestParams.put("moduleprimarykey", "QuotationDetailId");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put("moduleid", isLeaseFixedAsset ? Constants.Acc_Lease_Quotation : Constants.Acc_Customer_Quotation_ModuleId);
                    customrequestParams.put("companyid", companyid);
                    DOMap.put("id", row.getID());
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
                    HashMap<String, Object> quotationMap = new HashMap<>();
                    HashMap<String, Object> customrequestParams = new HashMap<>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "CqProductCustomData");
                    customrequestParams.put("moduleprimarykey", "CqDetailID");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                    customrequestParams.put("companyid", companyid);
                    quotationMap.put("id", row.getID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_CQDetail_Productcustom_data_classpath);
                    customrequestParams.put("productIdForRichRext", row.getProduct().getID());                    
                    fieldDataManagercntrl.setRichTextAreaForProduct(customrequestParams);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        quotationMap.put("qdetailscustomdataref", row.getID());
                        accSalesOrderDAOobj.updateQuotationDetailsProductCustomData(quotationMap);
                    }
                }

                HashMap<String, Object> qdVendDataMap = new HashMap<>();
                String vendorid = jobj.optString("vendorid", "");
                double vendorunitcost = jobj.optDouble("vendorunitcost", 0);
                double vendorcurrexchangerate = jobj.optDouble("vendorcurrexchangerate", 0);
                double totalcost = jobj.optDouble("totalcost", 0);
                KwlReturnObject resquoDetailsVendorMap = null;
                if (!StringUtil.isNullOrEmpty(vendorid) && extraCompanyPreferences.isActivateProfitMargin()) {
                    qdVendDataMap.put("id", row.getID());
                    qdVendDataMap.put("vendorid", vendorid);
                    qdVendDataMap.put("vendorunitcost", vendorunitcost);
                    qdVendDataMap.put("vendorcurrexchangerate", vendorcurrexchangerate);
                    qdVendDataMap.put("totalcost", totalcost);
                    resquoDetailsVendorMap = accSalesOrderDAOobj.saveQuotationDetailsVendorMapping(qdVendDataMap);
                }
                if (extraCompanyPreferences.getLineLevelTermFlag()==1 && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty(jobj.optString("LineTermdetails"))) {
                    JSONArray termsArray = new JSONArray(StringUtil.DecodeText(jobj.optString("LineTermdetails")));
                    if (extraCompanyPreferences.isAvalaraIntegration()) {//When Avalara Integration is enabled, we save tax details in table 'TransactionDetailAvalaraTaxMapping'
                        JSONObject paramsJobj = new JSONObject();
                        paramsJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
                        paramsJobj.put(Constants.companyKey, companyid);
                        if (integrationCommonService.isTaxCalculationEnabled(paramsJobj)) {
                            JSONObject saveTaxParamsJobj = new JSONObject();
                            saveTaxParamsJobj.put(IntegrationConstants.parentRecordID, row.getID());
                            saveTaxParamsJobj.put(IntegrationConstants.avalaraTaxDetails, StringUtil.DecodeText(jobj.optString("LineTermdetails")));
                            integrationCommonService.saveTransactionDetailTaxMapping(saveTaxParamsJobj);
                        }
                    } else {
                        for (int j = 0; j < termsArray.length(); j++) {
                            HashMap<String, Object> quotationDetailsTermsMap = new HashMap<>();
                            JSONObject termObject = termsArray.getJSONObject(j);

                            if (termObject.has("termid")) {
                                quotationDetailsTermsMap.put("term", termObject.get("termid"));
                            }
                            if (termObject.has("termamount")) {
                                quotationDetailsTermsMap.put("termamount", termObject.get("termamount"));
                            }
                            if (termObject.has("termpercentage")) {
                                quotationDetailsTermsMap.put("termpercentage", termObject.get("termpercentage"));
                            }
                            if (termObject.has("assessablevalue")) {
                                quotationDetailsTermsMap.put("assessablevalue", termObject.get("assessablevalue"));
                            }
                            if (termObject.has("purchasevalueorsalevalue")) {
                                quotationDetailsTermsMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                            }
                            if (termObject.has("deductionorabatementpercent")) {
                                quotationDetailsTermsMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                            }
                            if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.getString("taxtype"))) {
                                quotationDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                                if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.getString("taxvalue"))) {
                                    if (termObject.getInt("taxtype") == 0) { // If Flat
                                        quotationDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                    } else { // Else Percentage
                                        quotationDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                    }
                                }
                            }
                            quotationDetailsTermsMap.put("quotationDetailID", row.getID());
                            /**
                             * ERP-32829 
                             */
                            quotationDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                            quotationDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                            quotationDetailsTermsMap.put("product", jobj.get("productid"));
                            quotationDetailsTermsMap.put("userid", paramJobj.getString(Constants.useridKey));
                            accSalesOrderDAOobj.saveQuotationDetailsTermMap(quotationDetailsTermsMap);
                        }
                    }
                }
                if (extraCompanyPreferences.getLineLevelTermFlag()==1) {
                    /**
                     * Save GST History Customer/Vendor data.
                     */
                    jobj.put("detaildocid", row.getID());
                    jobj.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                    fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jobj);
                }
                //  Indian Details Valuation Type -- start   
                KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) companyObj.getEntityList().get(0);
                if (company.getCountry() != null && (Constants.indian_country_id) == Integer.parseInt(company.getCountry().getID())) {
                    if (jobj.has("productMRP") && !StringUtil.isNullOrEmpty(jobj.getString("productMRP"))) {
                        row.setMrpIndia(jobj.getDouble("productMRP"));
                    }
                    if (jobj.has("valuationType") && !StringUtil.isNullOrEmpty(jobj.getString("valuationType"))) { // Excise Details
                        row.setExciseValuationType(jobj.getString("valuationType"));
                        if ((Constants.QUENTITY).equals(jobj.getString("valuationType"))) {
                            if (jobj.has("reortingUOMExcise") && !StringUtil.isNullOrEmpty(jobj.getString("reortingUOMExcise"))) {
                                UnitOfMeasure reportingUom = null;
                                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), jobj.getString("reortingUOMExcise"));
                                reportingUom = (UnitOfMeasure) custresult.getEntityList().get(0);
                                row.setReportingUOMExcise(reportingUom);
                            }
                            if (jobj.has("reortingUOMSchemaExcise") && !StringUtil.isNullOrEmpty(jobj.getString("reortingUOMSchemaExcise"))) {
                                UOMschemaType reportingUom = null;
                                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UOMschemaType.class.getName(), jobj.getString("reortingUOMSchemaExcise"));
                                reportingUom = (UOMschemaType) custresult.getEntityList().get(0);
                                row.setReportingSchemaTypeExcise(reportingUom);
                            }
                        }
                    }
                    if (jobj.has("valuationTypeVAT") && !StringUtil.isNullOrEmpty(jobj.getString("valuationTypeVAT"))) { // VAT Details
                        row.setVatValuationType(jobj.getString("valuationTypeVAT"));
                        if ((Constants.QUENTITY).equals(jobj.getString("valuationTypeVAT"))) {
                            if (jobj.has("reportingUOMVAT") && !StringUtil.isNullOrEmpty(jobj.getString("reportingUOMVAT"))) {
                                UnitOfMeasure reportingUom = null;
                                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), jobj.getString("reportingUOMVAT"));
                                reportingUom = (UnitOfMeasure) custresult.getEntityList().get(0);
                                row.setReportingUOMVAT(reportingUom);
                            }
                            if (jobj.has("reportingUOMSchemaVAT") && !StringUtil.isNullOrEmpty(jobj.getString("reportingUOMSchemaVAT"))) {
                                UOMschemaType reportingUom = null;
                                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(UOMschemaType.class.getName(), jobj.getString("reportingUOMSchemaVAT"));
                                reportingUom = (UOMschemaType) custresult.getEntityList().get(0);
                                row.setReportingSchemaVAT(reportingUom);
                            }
                        }
                    }
                }
                // Indian Details Valuation Type -- End
                rows.add(row);
            }
            if (Integer.parseInt(countryid) == Constants.indian_country_id && RCMApplicable && !productNameRCMNotActivate.isEmpty()) {
                throw new AccountingException(messageSource.getMessage("acc.common.rcmforproductnotactivated.CQ.text", new Object[]{StringUtils.join(productNameRCMNotActivate, ", ")}, null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveQuotationRows : " + ex.getMessage(), ex);
        }
        return rows;
    }
    
    @Override
    public JSONObject importCustomerQuotationRecordsForCSV(HashMap<String, Object> request, JSONObject jobj, HashMap<String, Object> globalParams) throws AccountingException, IOException, SessionExpiredException, JSONException {
        boolean issuccess = true;
        String msg = "";
        String fileName = jobj.getString("filename");
        int total = 0, failed = 0;
        String companyID = request.get("companyid").toString();
        String userID = request.get("userid").toString();
        String masterPreference = request.get("masterPreference").toString();
        String delimiterType = request.get("delimiterType").toString();
        String gcurrencyId = request.get("currencyId").toString();
        boolean isAlreadyExist = false;
        boolean isRecordFailed = false;
        String customfield = "";
        FileInputStream fileInputStream = null;
        CsvReader csvReader = null;
        String language = (String) request.get(Constants.language);
        JSONObject returnObj = new JSONObject();
        JSONObject paramJobj = new JSONObject();
        JSONArray rows = new JSONArray();
        String prevInvNo = "";

        try {
            String dateFormat = null, dateFormatId = request.get("dateFormat").toString();
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            df.setLenient(false);

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
            DateFormat dateOnlydf = null;
            if (request.containsKey(Constants.df) && request.get(Constants.df) != null) {
                dateOnlydf = (DateFormat) request.get(Constants.df);
            }

            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            Boolean isCurrencyCode = extrareferences.isCurrencyCode();
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);
            int cnt = 0;

            StringBuilder failedRecords = new StringBuilder();

            HashMap<String, Integer> columnConfig = new HashMap<>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }

            HashMap currencyMap = getCurrencyMap(isCurrencyCode);

            while (csvReader.readRecord()) {
                String failureMsg = "";
                String[] recarr = csvReader.getValues();
                if (cnt == 0) {
                    failedRecords.append(createCSVrecord(recarr)).append("\" \"");
                }
                if (cnt == 1) {
                    failedRecords.append("\n").append(createCSVrecord(recarr)).append("\"Error Message\"");
                }

                if (cnt != 0 && cnt != 1) {
                    try {
                        String currencyID = request.get("currencyId").toString();

                        String customerQutationNumber = "";
                        if (columnConfig.containsKey("number")) {
                            customerQutationNumber = recarr[(Integer) columnConfig.get("number")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(customerQutationNumber)) {
                                failureMsg += "Customer Quotation Number is not available. ";
                            }
                        } else {
                            failureMsg += "Customer Quotation Number column is not found. ";
                        }

                        Date customerQutationDate = null;
                        if (columnConfig.containsKey("billdate")) {
                            String customerQutationDateStr = recarr[(Integer) columnConfig.get("billdate")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(customerQutationDateStr)) {
                                failureMsg += "Customer Quotation Date is not available. ";
                            } else {
                                try {
                                    customerQutationDate = df.parse(customerQutationDateStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Customer Quotation Date, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        } else {
                            failureMsg += "Customer Quotation Date column is not found. ";
                        }
                        
                        String customerPORefNo = "";
                        if (columnConfig.containsKey("customerPORefNo")) {
                            customerPORefNo = recarr[(Integer) columnConfig.get("customerPORefNo")].replaceAll("\"", "").trim();
                        }

                        String shippingTerm = "";
                        if (columnConfig.containsKey("shippingterm")) {
                            shippingTerm = recarr[(Integer) columnConfig.get("shippingterm")].replaceAll("\"", "").trim();
                        }
                        
                        Date shipDate = null;
                        if (columnConfig.containsKey("shipdate")) {
                            String shipDateStr = recarr[(Integer) columnConfig.get("shipdate")].replaceAll("\"", "").trim();

                            if (!StringUtil.isNullOrEmpty(shipDateStr)) {
                                try {
                                    shipDate = df.parse(shipDateStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Ship Date, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        }

                        String customerID = "";
                        String accountID = "";

                        /*
                         * 1. Customer Code
                         */
                        if (columnConfig.containsKey("customerCode")) {
                            String customerCode = recarr[(Integer) columnConfig.get("customerCode")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(customerCode)) {
                                Customer customer = getCustomerByCode(customerCode, companyID);
                                if (customer != null) {
                                    accountID = customer.getAccount().getID();
                                    customerID = customer.getID();
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.CustomerCodeisnotavailable", null, Locale.forLanguageTag(language)) + customerCode + ". ";
                                }
                            }
                        }

                        /*
                         * 2. Customer Name if customerID is empty it means
                         * customer is not found for given code. so need to
                         * search data on name
                         */
                        if (StringUtil.isNullOrEmpty(customerID)) {
                            if (columnConfig.containsKey("customer")) {
                                String customerName = recarr[(Integer) columnConfig.get("customer")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(customerName)) {
                                    Customer customer = null;
                                    KwlReturnObject retObj = accCustomerDAOobj.getCustomerByName(customerName, companyID);
                                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                        customer = (Customer) retObj.getEntityList().get(0);
                                    }
                                    if (customer != null) {
                                        accountID = customer.getAccount().getID();
                                        customerID = customer.getID();
                                    } else {
                                        failureMsg += messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, Locale.forLanguageTag(language)) + ". ";
                                    }
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, Locale.forLanguageTag(language)) + ".";
                                }
                            } else {
                                failureMsg += messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, Locale.forLanguageTag(language)) + ".";
                            }
                        }

                        String termID = "";
                        if (columnConfig.containsKey("termid")) {
                            String termName = recarr[(Integer) columnConfig.get("termid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(termName)) {
                                termID = getTermIDByName(termName, companyID);
                                if (StringUtil.isNullOrEmpty(termID)) {
                                    failureMsg += "Credit Term is not found for name " + termName + ". ";
                                }
                            } else {
                                failureMsg += "Credit Term is not available. ";
                            }
                        } else {
                            failureMsg += "Credit Term column is not found. ";
                        }

                        String memo = "";
                        if (columnConfig.containsKey("memo")) {
                            memo = recarr[(Integer) columnConfig.get("memo")].replaceAll("\"", "").trim();
                        }

                        String shipVia = "";
                        if (columnConfig.containsKey("shipvia")) {
                            shipVia = recarr[(Integer) columnConfig.get("shipvia")].replaceAll("\"", "").trim();
                        }

                        String fob = "";
                        if (columnConfig.containsKey("fob")) {
                            fob = recarr[(Integer) columnConfig.get("fob")].replaceAll("\"", "").trim();
                        }

                        String salesPersonID = "";
                        if (columnConfig.containsKey("salesperson")) {
                            String salesPersonName = recarr[(Integer) columnConfig.get("salesperson")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(salesPersonName)) {
                                salesPersonID = getSalesPersonIDByName(salesPersonName, companyID);
                                if (StringUtil.isNullOrEmpty(salesPersonID)) {
                                    failureMsg += "Sales Person is not found for name " + salesPersonName + ". ";
                                }
                            }
                        }
                        
                        Date validTill = null;
                        if (columnConfig.containsKey("validdate")) {
                            String validTillStr = recarr[(Integer) columnConfig.get("validdate")].replaceAll("\"", "").trim();

                            if (!StringUtil.isNullOrEmpty(validTillStr)) {
                                try {
                                    validTill = df.parse(validTillStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Valid Till, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        }

                        if (isCurrencyCode ? columnConfig.containsKey("currencyCode") : columnConfig.containsKey("currencyid")) {
                            String currencyStr = isCurrencyCode ? recarr[(Integer) columnConfig.get("currencyCode")].replaceAll("\"", "").trim() : recarr[(Integer) columnConfig.get("currencyid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(currencyStr)) {
                                currencyID = getCurrencyId(currencyStr, currencyMap);

                                if (StringUtil.isNullOrEmpty(currencyID)) {
                                    failureMsg += messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, Locale.forLanguageTag(language)) + ". ";
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg += "Currency is not available. ";
                                }
                            }
                        }

                        Product product = null;
                        String productUUID = "";
                        if (columnConfig.containsKey("productid")) {
                            String productID = recarr[(Integer) columnConfig.get("productid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productID)) {
                                product = getProductByProductID(productID, companyID);
                                if (product != null) {
                                    productUUID = product.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productUUID = null;
                                    } else {
                                        failureMsg += "Product is not found for " + productID + ". ";
                                    }
                                }
                            } else {
                                failureMsg += "Product is not available. ";
                            }
                        } else {
                            failureMsg += "Product column is not found. ";
                        }

                        double quantity = 0;
                        if (columnConfig.containsKey("quantity")) {
                            String quantityStr = recarr[(Integer) columnConfig.get("quantity")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(quantityStr)) {
                                failureMsg += "Quantity is not available. ";
                            } else {
                                try {
                                    quantity = authHandler.roundQuantity(Double.parseDouble(quantityStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Quantity, Please ensure that value type of Quantity matches with the Quantity. ";
                                }
                            }
                        } else {
                            failureMsg += "Quantity column is not found. ";
                        }

                        double unitPrice = 0;
                        if (columnConfig.containsKey("rate")) {
                            String unitPriceStr = recarr[(Integer) columnConfig.get("rate")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(unitPriceStr)) {
                                failureMsg += "Unit Price is not available. ";
                            } else {
                                try {
                                    unitPrice = authHandler.roundQuantity(Double.parseDouble(unitPriceStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Unit Price, Please ensure that value type of Unit Price matches with the Unit Price. ";
                                }
                            }
                        } else {
                            failureMsg += "Unit Price column is not found. ";
                        }

                        UnitOfMeasure uom = null;
                        String productUOMID = "";
                        if (columnConfig.containsKey("uomid")) {
                            String productUOMName = recarr[(Integer) columnConfig.get("uomid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                uom = getUOMByName(productUOMName, companyID);
                                if (uom != null) {
                                    productUOMID = uom.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productUOMID = "";
                                    } else {
                                        failureMsg += "Product Unit Of Measure is not found for " + productUOMName + ". ";
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productUOMID = "";
                                } else {
                                    failureMsg += "Product Unit Of Measure is not available. ";
                                }
                            }
                        } else {
                            productUOMID = "";
                        }

                        int discountType = 1;
                        if (columnConfig.containsKey("discountispercent")) {
                            String discountTypeStr = recarr[(Integer) columnConfig.get("discountispercent")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(discountTypeStr)) {
                                if (discountTypeStr.equalsIgnoreCase("Percentage")) {
                                    discountType = 1;
                                } else if (discountTypeStr.equalsIgnoreCase("Flat")) {
                                    discountType = 0;
                                } else {
                                    failureMsg += "Format you entered is not correct. It should be like \"Percentage\" or \"Flat\". ";
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg += "Discount Type is not available. ";
                                }
                            }
                        }

                        double discount = 0;
                        if (columnConfig.containsKey("prdiscount")) {
                            String discountStr = recarr[(Integer) columnConfig.get("prdiscount")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(discountStr)) {
                                failureMsg += "Dicount is not available. ";
                            } else {
                                try {
                                    discount = authHandler.roundQuantity(Double.parseDouble(discountStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Discount, Please ensure that value type of Discount matches with the Discount. ";
                                }
                            }
                        }

                        double exchangeRateForTransaction = 1;
                        Map<String, Object> currMap = new HashMap<>();
                        Date applyDate = customerQutationDate;

                        currMap.put("applydate", applyDate);
                        currMap.put("gcurrencyid", gcurrencyId);
                        currMap.put("companyid", companyID);
                        KwlReturnObject retObj = accCurrencyDAOobj.getExcDetailID(currMap, currencyID, applyDate, null);
                        if (retObj != null) {
                            List li = retObj.getEntityList();
                            if (!li.isEmpty()) {
                                Iterator itr = li.iterator();
                                ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                                if (erd != null) {
                                    exchangeRateForTransaction = erd.getExchangeRate();
                                }
                            }
                        }

                        // creating CQ json
                        if (!prevInvNo.equalsIgnoreCase(customerQutationNumber) || customerQutationNumber.equalsIgnoreCase("")) {
                            prevInvNo = customerQutationNumber;

                            if (rows.length() > 0 && !isRecordFailed) {
                                paramJobj.put(Constants.detail, rows.toString());
                                if (request.containsKey(Constants.PAGE_URL)) {
                                    paramJobj.put(Constants.PAGE_URL, (String) request.get(Constants.PAGE_URL));
                                }
                                // for save CQ
                                saveQuotationJSON(paramJobj);

                                // reset variables
                                paramJobj = new JSONObject();
                                rows = new JSONArray();
                            }
                            isRecordFailed = false;
                            isAlreadyExist = false;

                            KwlReturnObject result = accSalesOrderDAOobj.getQuotationCount(customerQutationNumber, companyID);
                            int nocount = result.getRecordTotalCount();
                            if (nocount > 0) {
                                isAlreadyExist = true;
                                throw new AccountingException("Vendor Invoice number'" + customerQutationNumber + "' already exists.");
                            }

                            if (!StringUtil.isNullOrEmpty(failureMsg)) {
                                throw new AccountingException(failureMsg);
                            }

                            // For create custom field array
                            JSONArray customJArr = new JSONArray();
                            for (int i = 0; i < jSONArray.length(); i++) {
                                JSONObject jSONObject = jSONArray.getJSONObject(i);

                                if (jSONObject.optBoolean("customflag", false) && !jSONObject.optBoolean("isLineItem", false)) {
                                    HashMap<String, Object> requestParams = new HashMap<>();
                                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                    requestParams.put(Constants.filter_values, Arrays.asList(companyID, Constants.Acc_Customer_Quotation_ModuleId, jSONObject.getString("columnname")));

                                    KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
                                    FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);

                                    if (!StringUtil.isNullOrEmpty(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim())) {
                                        JSONObject customJObj = new JSONObject();
                                        customJObj.put("fieldid", params.getId());
                                        customJObj.put("filedid", params.getId());
                                        customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                        customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                        customJObj.put("xtype", params.getFieldtype());

                                        String fieldComboDataStr = "";
                                        if (params.getFieldtype() == 3) { // if field of date type
                                            String dateStr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim();
                                            customJObj.put("Col" + params.getColnum(), df.parse(dateStr).getTime());
                                            customJObj.put("fieldDataVal", df.parse(dateStr).getTime());
                                        } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                            String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                                            for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                                requestParams = new HashMap<>();
                                                requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                                requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), fieldComboDataArr[dataArrIndex], 0));


                                                fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                                if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                    FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                    fieldComboDataStr += fieldComboData.getId() + ",";
                                                }
                                            }

                                            if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                                customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                                customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            } else if (StringUtil.equalIgnoreCase(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim(), Constants.NONE)) {
                                                //To handle 'None' value of non-mandatory fields
                                                customJObj.put("Col" + params.getColnum(), "");
                                                customJObj.put("fieldDataVal", "");
                                            } else {
                                                continue;
                                            }
                                        } else if (params.getFieldtype() == 11) { // if field of check box type 
                                            customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                            customJObj.put("fieldDataVal", Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                        } else if (params.getFieldtype() == 12) { // if field of check list type
                                            requestParams = new HashMap<>();
                                            requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                            requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), 0));


                                            fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                            List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();

                                            String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                                            int dataArrIndex = 0;

                                            for (FieldComboData fieldComboData : fieldComboDataList) {
                                                if (fieldComboDataArr.length > dataArrIndex && fieldComboDataArr[dataArrIndex] != null && fieldComboDataArr[dataArrIndex].replaceAll("\"", "").trim().equalsIgnoreCase("true")) {
                                                    fieldComboDataStr += fieldComboData.getId() + ",";
                                                }
                                                dataArrIndex++;
                                            }

                                            if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                                customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                                customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            } else {
                                                continue;
                                            }
                                        } else {
                                            customJObj.put("Col" + params.getColnum(), recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                            customJObj.put("fieldDataVal", recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                        }

                                        customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

                                        customJArr.put(customJObj);
                                    }
                                }
                            }
                            customfield = customJArr.toString();

                            // for adding due date
                            KwlReturnObject termObj = accountingHandlerDAOobj.getObject(Term.class.getName(), termID);
                            Term term = (Term) termObj.getEntityList().get(0);

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(customerQutationDate);
                            cal.add(Calendar.DAY_OF_MONTH, term.getTermdays());
                            
                            String sequenceFormatID = "NA";
                            boolean autogenerated = false;
                            if (!StringUtil.isNullOrEmpty(customerQutationNumber)) {
                                Map<String, String> sequenceNumberDataMap = new HashMap<String, String>();
                                sequenceNumberDataMap.put("moduleID", String.valueOf(Constants.Acc_Customer_Quotation_ModuleId));
                                sequenceNumberDataMap.put("entryNumber", customerQutationNumber);
                                sequenceNumberDataMap.put("companyID", companyID);
                                List list = importHandler.checksEntryNumberForSequenceNumber(sequenceNumberDataMap);
                                if (!list.isEmpty()) {
                                    boolean isvalidEntryNumber = (Boolean) list.get(0);
                                    if (!isvalidEntryNumber) {
                                        String formatID = (String) list.get(2);
                                        int intSeq = (Integer) list.get(3);
                                        paramJobj.put(Constants.SEQNUMBER, intSeq);
                                        paramJobj.put(Constants.SEQFORMAT, formatID);
                                        autogenerated = true;
                                        sequenceFormatID = formatID;
                                    }
                                }
                            }
                            
                            // request map for save goods receipt  
                            paramJobj.put(Constants.companyKey, companyID);
                            paramJobj.put(Constants.globalCurrencyKey, request.get(Constants.globalCurrencyKey));
                            paramJobj.put(Constants.useridKey, userID);
                            paramJobj.put(Constants.userfullname, request.get(Constants.userfullname));
                            paramJobj.put(Constants.reqHeader, request.get(Constants.reqHeader));
                            paramJobj.put(Constants.remoteIPAddress, request.get(Constants.remoteIPAddress));
                            paramJobj.put(Constants.language, language);
                            paramJobj.put(Constants.currencyKey, currencyID);
                            paramJobj.put("number", customerQutationNumber);
                            paramJobj.put("sequenceformat", sequenceFormatID);
                            paramJobj.put("autogenerated", autogenerated);
                            paramJobj.put("customer", customerID);
                            paramJobj.put("defaultAdress", "true");
                            paramJobj.put("memo", memo);
                            paramJobj.put("posttext", "");
                            paramJobj.put("termid", termID);
                            paramJobj.put("billdate", sdf.format(customerQutationDate));
                            paramJobj.put("duedate", sdf.format(cal.getTime()));
                            paramJobj.put("perdiscount", "false");
                            paramJobj.put("discount", "0");
                            paramJobj.put("includingGST", "false");
                            if (shipDate != null) {
                                paramJobj.put("shipdate", sdf.format(shipDate));
                            }
                            if (validTill != null) {
                                paramJobj.put("validdate", sdf.format(validTill));
                            }
                            paramJobj.put("shipvia", shipVia);
                            paramJobj.put("shippingterm", shippingTerm);
                            paramJobj.put("fob", fob);
                            paramJobj.put("isfavourite", "false");
                            paramJobj.put("salesperson", salesPersonID);
                            paramJobj.put("customerPORefNo", customerPORefNo);
                            paramJobj.put("externalcurrencyrate", String.valueOf(exchangeRateForTransaction));
                            paramJobj.put("istemplate", "0");
                            paramJobj.put("taxamount", "0");
                            paramJobj.put("invoicetermsmap", "[]");
                            paramJobj.put("termsincludegst", "false");
                            paramJobj.put("fromLinkCombo", "");
                            paramJobj.put("linkFrom", "");
                            paramJobj.put("linkNumber", "");
                            paramJobj.put("templatename", "");
                            paramJobj.put("customfield", customfield);
                            paramJobj.put("isEdit", "false");
                            paramJobj.put("copyInv", "false");
                            paramJobj.put(Constants.isDraft, "false");
                            paramJobj.put("includeprotax", "false");
                            paramJobj.put("shipLength", "1");
                            paramJobj.put("taxid", "");
                            paramJobj.put("deletedLinkedDocumentId", "");
                            paramJobj.put("invoicetype", "");
                            paramJobj.put("seqformat_oldflag", "false");

                            Map<String, Object> requestParams = new HashMap<>();
                            requestParams.put(Constants.companyKey, companyID);
                            CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, customerQutationDate, false);
                        } // end global details

                        double conversionFactor = 1;

                        JSONObject detailData = new JSONObject();
                        detailData.put("productid", (product != null) ? product.getID() : "");
                        detailData.put("rate", String.valueOf(unitPrice));
                        detailData.put("priceSource", "");
//                        detailData.put("rateIncludingGst", String.valueOf(unitPrice));
                        detailData.put("quantity", String.valueOf(quantity));
                        detailData.put("uomid", (uom != null) ? uom.getID() : "");
                        detailData.put("baseuomquantity", String.valueOf(quantity * conversionFactor));
                        detailData.put("baseuomrate", String.valueOf(conversionFactor));
                        detailData.put("dependentType", "");
                        detailData.put("inouttime", "");
                        detailData.put("showquantity", "");
                        detailData.put("desc", (product != null) ? product.getDescription() : "");
                        detailData.put("invstore", "");
                        detailData.put("invlocation", "");
                        detailData.put("rowid", "");
                        detailData.put("prdiscount", String.valueOf(discount));
                        detailData.put("discountispercent", String.valueOf(discountType));
                        detailData.put("prtaxid", "");
                        detailData.put("taxamount", "0");
                        detailData.put("linkto", "");
                        detailData.put("savedrowid", "");
                        detailData.put("recTermAmount", "");
                        detailData.put("OtherTermNonTaxableAmount", "");
                        detailData.put("productcustomfield", "[{}]");
                        detailData.put("LineTermdetails", "");
                        detailData.put("productMRP", "");
                        detailData.put("valuationType", "");
                        detailData.put("reortingUOMExcise", "");
                        detailData.put("reortingUOMSchemaExcise", "");
                        detailData.put("valuationTypeVAT", "");
                        detailData.put("reportingUOMVAT", "");
                        detailData.put("reportingUOMSchemaVAT", "");

                        // Add Custom fields details of line items
                        // For create custom field array
                        JSONArray lineCustomJArr = new JSONArray();
                        for (int i = 0; i < jSONArray.length(); i++) {
                            JSONObject jSONObject = jSONArray.getJSONObject(i);

                            if (jSONObject.optBoolean("customflag", false) && jSONObject.optBoolean("isLineItem", false)) {
                                HashMap<String, Object> requestParams = new HashMap<>();
                                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                requestParams.put(Constants.filter_values, Arrays.asList(companyID, Constants.Acc_Customer_Quotation_ModuleId, jSONObject.getString("columnname")));

                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
                                FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);

                                if (!StringUtil.isNullOrEmpty(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim())) {
                                    JSONObject customJObj = new JSONObject();
                                    customJObj.put("fieldid", params.getId());
                                    customJObj.put("filedid", params.getId());
                                    customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                    customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                    customJObj.put("xtype", params.getFieldtype());

                                    String fieldComboDataStr = "";
                                    if (params.getFieldtype() == 3) { // if field of date type
                                        String dateStr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim();
                                        customJObj.put("Col" + params.getColnum(), df.parse(dateStr).getTime());
                                        customJObj.put("fieldDataVal", df.parse(dateStr).getTime());
                                    } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                        String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                                        for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                            requestParams = new HashMap<>();
                                            requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                            requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), fieldComboDataArr[dataArrIndex], 0));


                                            fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                            if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                fieldComboDataStr += fieldComboData.getId() + ",";
                                            }
                                        }

                                        if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                            customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                        } else {
                                            continue;
                                        }
                                    } else if (params.getFieldtype() == 11) { // if field of check box type 
                                        customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                        customJObj.put("fieldDataVal", Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                    } else if (params.getFieldtype() == 12) { // if field of check list type
                                        requestParams = new HashMap<>();
                                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                        requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), 0));

                                        fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                        List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();

                                        String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");
                                        int dataArrIndex = 0;

                                        for (FieldComboData fieldComboData : fieldComboDataList) {
                                            if (fieldComboDataArr.length > dataArrIndex && fieldComboDataArr[dataArrIndex] != null && fieldComboDataArr[dataArrIndex].replaceAll("\"", "").trim().equalsIgnoreCase("true")) {
                                                fieldComboDataStr += fieldComboData.getId() + ",";
                                            }
                                            dataArrIndex++;
                                        }

                                        if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                            customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                        } else {
                                            continue;
                                        }
                                    } else {
                                        customJObj.put("Col" + params.getColnum(), recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                        customJObj.put("fieldDataVal", recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                    }

                                    customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

                                    lineCustomJArr.put(customJObj);
                                }
                            }
                        }
                        String lineCustomfield = lineCustomJArr.toString();
                        detailData.put("customfield", lineCustomfield);

                        rows.put(detailData);

                    } catch (Exception ex) {
                        failed++;
                        isRecordFailed = true;
                        String errorMsg = ex.getMessage();
                        if (ex.getMessage() != null) {
                            errorMsg = ex.getMessage();
                        } else if (ex.getCause() != null) {
                            errorMsg = ex.getCause().getMessage();
                        }

                        failedRecords.append("\n").append(createCSVrecord(recarr)).append("\"").append(errorMsg.replaceAll("\"", "")).append("\"");
                    }
                    total++;
                }
                cnt++;
            }

            // save CQ for last record
            if (!isAlreadyExist && !isRecordFailed) {
                paramJobj.put(Constants.detail, rows.toString());
                if (request.containsKey(Constants.PAGE_URL)) {
                    paramJobj.put(Constants.PAGE_URL, (String) request.get(Constants.PAGE_URL));
                }
                saveQuotationJSON(paramJobj);
            }

            if (failed > 0) {
                importHandler.createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = "Empty file.";
            } else if (success == 0) {
                msg = "Failed to import all the records.";
            } else if (success == total) {
                msg = "All records are imported successfully.";
            } else {
                msg = "Imported " + success + " record" + (success > 1 ? "s" : "") + " successfully";
                msg += (failed == 0 ? "." : " and failed to import " + failed + " record" + (failed > 1 ? "s" : "") + ".");
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            csvReader.close();

            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                // Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<>();
                logDataMap.put("FileName", ImportLog.getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed > 0 ? "csv" : "");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_Customer_Quotation_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userID);
                logDataMap.put("Company", companyID);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", ImportLog.getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_Customer_Quotation_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
    
    public HashMap getCurrencyMap(boolean isCurrencyCode) throws ServiceException {
        HashMap currencyMap = new HashMap();
        KwlReturnObject returnObject = accCurrencyDAOobj.getCurrencies(currencyMap);
        List currencyList = returnObject.getEntityList();

        if (currencyList != null && !currencyList.isEmpty()) {
            Iterator iterator = currencyList.iterator();
            while (iterator.hasNext()) {
                KWLCurrency currency = (KWLCurrency) iterator.next();
                if (isCurrencyCode) {
                    currencyMap.put(currency.getCurrencyCode(), currency.getCurrencyID());
                } else {
                    currencyMap.put(currency.getName(), currency.getCurrencyID());
                }
            }
        }
        return currencyMap;
    }
    
    public String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 0; i < listArray.length; i++) {
            rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString().replaceAll("\"", "")) + "\",";
        }
        return rec;
    }
    
    public String getTermIDByName(String termName, String companyID) throws AccountingException {
        String termID = "";
        try {
            if (!StringUtil.isNullOrEmpty(termName) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> requestParams = new HashMap<>();
                requestParams.put("companyid", companyID);
                requestParams.put("termname", termName);


                KwlReturnObject retObj = accTermObj.getTerm(requestParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    Term term = (Term) retObj.getEntityList().get(0);
                    termID = term.getID();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Term.");
        }
        return termID;
    }
    
    public String getCurrencyId(String currencyName, HashMap currencyMap) {
        String currencyId = "";
        if (currencyMap != null && currencyMap.containsKey(currencyName)) {
            currencyId = currencyMap.get(currencyName).toString();
        }
        return currencyId;
    }
    
    public Product getProductByProductID(String productID, String companyID) throws AccountingException {
        Product product = null;
        try {
            if (!StringUtil.isNullOrEmpty(productID) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getProductByProductID(productID, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    product = (Product) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product");
        }
        return product;
    }
    
    public UnitOfMeasure getUOMByName(String productUOMName, String companyID) throws AccountingException {
        UnitOfMeasure uom = null;
        try {
            if (!StringUtil.isNullOrEmpty(productUOMName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getUOMByName(productUOMName, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    uom = (UnitOfMeasure) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Unit of Measure");
        }
        return uom;
    }
    
    @Override
    public Customer getCustomerByCode(String customerCode, String companyID) throws AccountingException {
        Customer customer = null;
        try {
            if (!StringUtil.isNullOrEmpty(customerCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accCustomerDAOobj.getCustomerByCode(customerCode, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    customer = (Customer) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching customer");
        }
        return customer;
    }
    
    @Override
    public String getSalesPersonIDByName(String salesPersonName, String companyID) throws AccountingException {
        String salesPersonID = "";
        try {
            if (!StringUtil.isNullOrEmpty(salesPersonName) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> filterRequestParams = new HashMap<>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(companyID);
                filter_names.add("masterGroup.ID");
                filter_params.add("15"); // For Geting Sales Person
                filter_names.add("value");
                filter_params.add(salesPersonName);
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);

                KwlReturnObject retObj = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    MasterItem salesPerson = (MasterItem) retObj.getEntityList().get(0);
                    salesPersonID = salesPerson.getID();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Sales Person.");
        }
        return salesPersonID;
    }

    @Override
    public void setValuesForAuditTrialForRecurringSO(RepeatedSalesOrder rSalesOrder, HashMap<String, Object> oldsoMap, HashMap<String, Object> newAuditKey) throws ServiceException {

        try {
            if (rSalesOrder != null) {

                newAuditKey.put("NoOfpost", "Number Recurring Sales Order");
                oldsoMap.put("NoOfpost", rSalesOrder.getNoOfSOpost() + "[" + rSalesOrder.getExpireDate().toString() + "]");
                newAuditKey.put("intervalUnit", "Repeat this Sales Order every");
                oldsoMap.put("intervalUnit", rSalesOrder.getIntervalUnit() + "[" + rSalesOrder.getIntervalType() + "]");
                newAuditKey.put("startDate", "Next Sales Order Generation Date");
                oldsoMap.put("startDate", rSalesOrder.getStartDate().toString());
            }

        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public String getCostCenterIDByName(String costCenterName, String companyID) throws AccountingException {
        String costCenterID = "";
        try {
            if (!StringUtil.isNullOrEmpty(costCenterName) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> requestParams = new HashMap<>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(companyID);
                filter_names.add("name");
                filter_params.add(costCenterName);
                requestParams.put(Constants.filterNamesKey, filter_names);
                requestParams.put(Constants.filterParamsKey, filter_params);
    
                KwlReturnObject retObj = accCostCenterObj.getCostCenter(requestParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    CostCenter costCenter = (CostCenter) retObj.getEntityList().get(0);
                    costCenterID = costCenter.getID();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Cost Center.");
        }
        return costCenterID;
    }
    
     @Override
    public double getExchangeRateForTransaction(JSONObject requestJobj, Date billDate, String currencyID) throws JSONException, ServiceException {
        double exchangeRateForTransaction = 1;

        Map<String, Object> currMap = new HashMap<>();
        currMap.put("applydate", billDate);
        currMap.put("gcurrencyid", requestJobj.getString(Constants.globalCurrencyKey));
        currMap.put("companyid", requestJobj.getString(Constants.companyKey));
        KwlReturnObject retObj = accCurrencyDAOobj.getExcDetailID(currMap, currencyID, billDate, null);
        if (retObj != null && !retObj.getEntityList().isEmpty()) {
            List li = retObj.getEntityList();
            Iterator itr = li.iterator();
            ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
            if (erd != null) {
                exchangeRateForTransaction = erd.getExchangeRate();
            }
        }
        return exchangeRateForTransaction;
    }
    
    @Override
    public JSONArray createLineLevelCustomFieldArrayForImport(JSONObject requestJobj, JSONArray jSONArray, String[] recarr, DateFormat df, int moduleID) throws JSONException, ParseException,AccountingException {
        JSONArray customJArr = new JSONArray();
        for (int i = 0; i < jSONArray.length(); i++) {
            JSONObject jSONObject = jSONArray.getJSONObject(i);

            if (jSONObject.optBoolean("customflag", false) && jSONObject.optBoolean("isLineItem", false)) {
                createCustomFieldArrayForImport(requestJobj, recarr, df, jSONObject, customJArr, moduleID);
            }
        }
        return customJArr;
    }
    @Override
    public JSONArray createGlobalCustomFieldArrayForImport(JSONObject requestJobj, JSONArray jSONArray, String[] recarr, DateFormat df, int moduleID) throws JSONException, ParseException,AccountingException {
        JSONArray customJArr = new JSONArray();

        for (int i = 0; i < jSONArray.length(); i++) {
            JSONObject jSONObject = jSONArray.getJSONObject(i);

            if (jSONObject.optBoolean("customflag", false) && !jSONObject.optBoolean("isLineItem", false)) {
                createCustomFieldArrayForImport(requestJobj, recarr, df, jSONObject, customJArr, moduleID);
            }
        }

        return customJArr;
    }
    public void createCustomFieldArrayForImport(JSONObject requestJobj, String[] recarr, DateFormat df, JSONObject jSONObject, JSONArray customJArr, int moduleID) throws JSONException, ParseException,AccountingException {
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
        requestParams.put(Constants.filter_values, Arrays.asList(requestJobj.getString(Constants.companyKey), moduleID, jSONObject.getString("columnname")));

        KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
        /*
          * Check For empty Field params
          */
        FieldParams params = null;
        if(fieldParamsResult.getEntityList() != null && fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty() && fieldParamsResult.getEntityList().size() > 0){
            params = (FieldParams) fieldParamsResult.getEntityList().get(0);
        }        
        boolean isMandatory = jSONObject.optBoolean("isMandatory", false);
        String columnHeader = jSONObject.optString("csvheader", "");
        String columnValue = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim();
        if (params != null && !StringUtil.isNullOrEmpty(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim())) {
            JSONObject customJObj = new JSONObject();
            customJObj.put("fieldid", params.getId());
            customJObj.put("filedid", params.getId());
            customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
            customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
            customJObj.put("xtype", params.getFieldtype());
            customJObj.put("isMandatory", false);
            if(params.getIsessential()==1){
             customJObj.put("isMandatory", true);
            }

            String fieldComboDataStr = "";
            if (params.getFieldtype() == 3) { // if field of date type
                String dateStr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim();
                customJObj.put("Col" + params.getColnum(), df.parse(dateStr).getTime());
                customJObj.put("fieldDataVal", df.parse(dateStr).getTime());
            } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                    requestParams = new HashMap<>();
                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                    requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), fieldComboDataArr[dataArrIndex], 0));


                    fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                    if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                        FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                        fieldComboDataStr += fieldComboData.getId() + ",";
                    }
                }

                if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                    customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                    customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                } else if (StringUtil.equalIgnoreCase(columnValue, Constants.NONE)) {//TO handle none value of non-mandatory combo-type custom fields and dimensions
                    customJObj.put("Col" + params.getColnum(), "");
                    customJObj.put("fieldDataVal", "");
                } else if (!columnValue.equalsIgnoreCase("none") && (moduleID == Constants.Acc_Receive_Payment_ModuleId || moduleID == Constants.Acc_Make_Payment_ModuleId || moduleID == Constants.Acc_Invoice_ModuleId)) {
                    throw new AccountingException(columnValue + " not found in drop down of " + columnHeader);
                } else {
                    return;
                }
            } else if (params.getFieldtype() == 11) { // if field of check box type 
                customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                customJObj.put("fieldDataVal", Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
            } else if (params.getFieldtype() == 12) { // if field of check list type
                requestParams = new HashMap<>();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), 0));


                fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();

                String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                for (FieldComboData fieldComboData : fieldComboDataList) {
                    for (String fieldComboArrData : fieldComboDataArr) {
                        if (fieldComboArrData != null && fieldComboArrData.replaceAll("\"", "").trim().equalsIgnoreCase(fieldComboData.getValue())) {
                            fieldComboDataStr += fieldComboData.getId() + ",";
                        }
                    }
                }

                if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                    customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                    customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                } else {
                    return;
                }
            } else {
                customJObj.put("Col" + params.getColnum(), recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                customJObj.put("fieldDataVal", recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
            }

            customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

            customJArr.put(customJObj);
        }else if (params != null && (params.getFieldtype() == 4 || params.getFieldtype() == 7 || params.getFieldtype() == 2) && isMandatory && (moduleID == Constants.Acc_Receive_Payment_ModuleId || moduleID == Constants.Acc_Make_Payment_ModuleId || moduleID == Constants.Acc_Credit_Note_ModuleId)) { // if field of MultiSelect Drop Down OR Drop Down-(ERP-26702-if Numeric field is empty and it is mandatory then through exception)
            throw new AccountingException("Empty value for " + columnHeader + " column,Please enter correct data.");
        }
    }
    
    @Override
    public String isValidCustomFieldData(JSONArray customArray) {
        String exceptionMSg = "";
        String data = "";
        String columnName = "";
        boolean isMandatory=false;
        try {
            if (customArray.length() > 0) {
                for (int i = 0; i < customArray.length(); i++) {
                    JSONObject JsonObject = customArray.getJSONObject(i);
                    if (JsonObject.has("xtype")) {
                        String type = JsonObject.optString("xtype", "");
                        isMandatory=JsonObject.optBoolean("isMandatory", false);
                        data=JsonObject.optString("fieldDataVal", "");
                        columnName=JsonObject.optString("fieldname", "");
                        if (type.equalsIgnoreCase("2")) {
                            try {
                                if (StringUtil.isNullOrEmpty(data)) {
                                    if (isMandatory) {      //most resticted or data is mandatory
                                        exceptionMSg = "Empty value for " + columnName + " column,Please enter correct data.";
                                    }
                                } else {
                                    Double.parseDouble(JsonObject.optString("fieldDataVal", ""));
                                }
                            } catch (NumberFormatException ex) {
                                exceptionMSg = "Incorrect numeric value for "+JsonObject.optString("fieldname", "")+" column,Please enter numeric data.";
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return exceptionMSg;
    }
    
    /**
     * Description: Method for saving import log.
     *
     * @param requestJobj
     * @param msg
     * @param total
     * @param failed
     * @param moduleID
     */
    @Override
    public void saveImportLog(JSONObject requestJobj, String msg, int total, int failed, int moduleID) {
        DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
        ldef.setName("import_Tx");
        ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus lstatus = txnManager.getTransaction(ldef);

        try {
            HashMap<String, Object> logDataMap = new HashMap<>();
            logDataMap.put("FileName", ImportLog.getActualFileName(requestJobj.getString("filename")));
            logDataMap.put("StorageName", requestJobj.getString("filename"));
            logDataMap.put("Log", msg);
            logDataMap.put("Type", "csv");
            logDataMap.put("FailureFileType", failed > 0 ? "csv" : "");
            logDataMap.put("TotalRecs", total);
            logDataMap.put("Rejected", failed);
            logDataMap.put("Module", moduleID);
            logDataMap.put("ImportDate", new Date());
            logDataMap.put("User", requestJobj.getString(Constants.useridKey));
            logDataMap.put("Company", requestJobj.getString(Constants.companyKey));
            logDataMap.put("Id", requestJobj.optString("logId"));
            importDao.saveImportLog(logDataMap);
            txnManager.commit(lstatus);
        } catch (JSONException | ServiceException | DataInvalidateException | TransactionException ex) {
            txnManager.rollback(lstatus);
            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Date getDueDateFromTermAndBillDate(String termID, Date billDate) throws ServiceException {
        Date dueDate;
        KwlReturnObject termObj = accountingHandlerDAOobj.getObject(Term.class.getName(), termID);
        Term term = (Term) termObj.getEntityList().get(0);

        Calendar cal = Calendar.getInstance();
        cal.setTime(billDate);
        cal.add(Calendar.DAY_OF_MONTH, term.getTermdays());
        dueDate = cal.getTime();

        return dueDate;
    }
    
    /**
     * Description: Method for importing and validating Sales Orders.
     *
     * @param paramJobj
     * @return
     */
    @Override
    public JSONObject importSalesOrderJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        try {
            String doAction = paramJobj.getString("do");

            if (doAction.compareToIgnoreCase("import") == 0) {
                jobj = importSalesOrderRecordsForCSV(paramJobj);
            } else if (doAction.compareToIgnoreCase("importByScript") == 0){
                paramJobj = addRequestParamsForScriptImport(paramJobj);
                paramJobj = getImportFileDetailsAndCopyFileToServer(paramJobj);
                paramJobj = getColumnMappingJsonForScriptImport(paramJobj);
                /*
                    If file exists for import and is successfully copied from remote server to deskera server,
                    then 'isFileExistsForImport' flag is true, otherwise it's false.
                    This flag is put into paramJobj, by method 'getImportFileDetailsAndCopyFileToServer'
                */
                if (paramJobj.optBoolean("isFileExistsForImport", false)) {
                    jobj = importSalesOrderRecordsForCSV(paramJobj);
                } else {
                    throw new AccountingException("Could not read data from file. Please make sure that file exists at location and file name is correct.");
                }
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                String eParams = paramJobj.getString("extraParams");
                JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);

                HashMap<String, Object> requestParams = importHandler.getImportRequestParams(paramJobj);
                requestParams.put("extraParams", extraParams);
                requestParams.put("extraObj", null);
                requestParams.put("servletContext", paramJobj.get("servletContext"));
                /*
                 While importing opening PO need some parameters that's why below if block is used.
                */
                if (paramJobj.has("isOpeningOrder") && paramJobj.optBoolean("isOpeningOrder")) {
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), paramJobj.getString(Constants.companyKey));
                    CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                    requestParams.put("bookbeginning", preferences.getBookBeginningFrom());
                    requestParams.put("isOpeningOrder", true);
                }

                jobj = importHandler.validateFileData(requestParams);
                jobj.put("success", true);
            }
        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return jobj;
    }
    /**
     * Description: This function gets import File Details from Database or from paramJobj and
     * calls copy function to copy file to deskera server from FTP location
     *
     * @param paramJobj
     * @return paramJobj (with some additional params)
     * @throws JSONException
     * @throws ServiceException
     * @throws AccountingException
     */
    public JSONObject getImportFileDetailsAndCopyFileToServer(JSONObject paramJobj) throws JSONException, ServiceException, AccountingException {
        boolean isFileExistsForImport = false;
        JSONObject paramsForFileDetails = new JSONObject();
        paramsForFileDetails.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
        paramsForFileDetails.put(Constants.moduleid, paramJobj.optString(Constants.moduleid));
        KwlReturnObject importFileDetailsKwlObj = importDao.getImportFileDetails(paramsForFileDetails);
        if (importFileDetailsKwlObj != null && importFileDetailsKwlObj.getEntityList() != null && !importFileDetailsKwlObj.getEntityList().isEmpty()) {
            ImportFileDetails importFileDetails = (ImportFileDetails) importFileDetailsKwlObj.getEntityList().get(0);
            if (importFileDetails != null) {
                JSONObject fileDetailsJobj = new JSONObject();
                String fileName = importFileDetails.getFileName();
                String fileNameSuffix = "";
                if (!StringUtil.isNullOrEmpty(importFileDetails.getFileNameSuffixDateFormat())) {//In case file name has a date suffix, get DateFormat for suffix and create suffix for file-name
                    Date date = new Date();
                    DateFormat df = new SimpleDateFormat(importFileDetails.getFileNameSuffixDateFormat());
                    fileNameSuffix = df.format(date);
                }
                if (paramJobj.has("importFileName")) {//If file name exists in paramJobj, use the name from paramJobj and set suffix as empty
                    fileName = paramJobj.optString("importFileName");
                    fileNameSuffix = "";
                }
                String fileNameWithUUID = fileName + fileNameSuffix + "_" + StringUtil.generateUUID() + ".csv";//append a UUID to fileName to make the name unique
                fileDetailsJobj.put("fileName", fileName);
                fileDetailsJobj.put("fileNameSuffix", fileNameSuffix);
                fileDetailsJobj.put("serverUrl", importFileDetails.getServerUrl());
                fileDetailsJobj.put("serverPort", importFileDetails.getServerPort());
                fileDetailsJobj.put("subDirectory", importFileDetails.getSubDirectory());
                fileDetailsJobj.put("userName", importFileDetails.getUserName());
                fileDetailsJobj.put("passKey", importFileDetails.getPassKey());
                fileDetailsJobj.put("fileNameWithUUID", fileNameWithUUID);
                isFileExistsForImport = copyImportFileFromRemoteServer(fileDetailsJobj);
                paramJobj.put("filename", fileNameWithUUID);
            }
        } else {
            throw new AccountingException("File details could not be fetched from database. Please make sure that file details are set in database.");
        }

        paramJobj.put("isFileExistsForImport", isFileExistsForImport);
        return paramJobj;
    }

    /**
     * Description: Copy import file from FTP location to Deskera server
     *
     * @param fileDetailsJobj
     * @return success -> Boolean flag which indicates whether the file is
     * copied successfully or not.
     * @throws AccountingException
     */
    public boolean copyImportFileFromRemoteServer(JSONObject fileDetailsJobj) throws AccountingException {
        boolean success = false;
        String fileName = fileDetailsJobj.optString("fileName");
        String fileNameSuffix = fileDetailsJobj.optString("fileNameSuffix");
        String fileNameWithUUID = fileDetailsJobj.optString("fileNameWithUUID");
        String serverUrl = fileDetailsJobj.optString("serverUrl");
        int serverPort = fileDetailsJobj.optInt("serverPort");
        String subDirectory = fileDetailsJobj.optString("subDirectory");
        String userName = fileDetailsJobj.optString("userName");
        String passKey = fileDetailsJobj.optString("passKey");
        String remoteFilePath = "";
        FTPClient ftpClient = new FTPClient();
        OutputStream outputStream = null;
        try {
            ftpClient.connect(serverUrl, serverPort);
            ftpClient.login(userName, passKey);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            remoteFilePath = subDirectory + File.separator + fileName + fileNameSuffix + ".csv";
            String serverDirectoryPath = storageHandlerImpl.GetDocStorePath() + "importplans";//Location of directory at server where file is to be uploaded
            String serverFilePath = serverDirectoryPath + File.separator + fileNameWithUUID;
            File serverFile = new File(serverFilePath);
            outputStream = new BufferedOutputStream(new FileOutputStream(serverFile));
            success = ftpClient.retrieveFile(remoteFilePath, outputStream);//Copies file from 'remoteFilePath' to 'serverFilePath'
        } catch (IOException ex) {
            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new AccountingException("Could not read data from file at path '" + remoteFilePath + "'. Please make sure that file exists at location and file name is correct.");
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return success;
    }

    /**
     * Description: Adds required parameters to paramJobj when call is coming
     * from Cron scheduler
     *
     * @param paramJobj
     * @return paramJobj (with some additional params)
     * @throws JSONException
     * @throws AccountingException
     * @throws ServiceException
     * @throws IOException
     */
    public JSONObject addRequestParamsForScriptImport(JSONObject paramJobj) throws JSONException, AccountingException, ServiceException, IOException {
        String subdomain = paramJobj.optString(Constants.COMPANY_SUBDOMAIN);
        String companyid = companyDetailsDAOObj.getCompanyid(subdomain);
        paramJobj.put(Constants.companyKey, companyid);
        paramJobj.put("isImportByScript", true);//Flag to indicate that the import is being done with script (not from UI)
        paramJobj.put("delimiterType", "Comma");
        paramJobj.put("masterPreference", "0");//Default Value

        String language = Constants.RES_DEF_LANGUAGE;//en-US default language for US clients
        Locale locale = Locale.forLanguageTag(language);
        paramJobj.put(Constants.locale, locale);
        paramJobj.put(Constants.language, language);
        paramJobj.put(Constants.reqHeader, Constants.defaultIp);
        paramJobj.put(Constants.remoteIPAddress, Constants.defaultIp);

        String globalCurrencyId = "", userId = "", userFullName = "", userName = "", userDateFormatId = Constants.yyyyMMdd_formatid, userDateFormat = Constants.yyyyMMdd;
        KwlReturnObject companyKwlObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        if (companyKwlObj != null && companyKwlObj.getEntityList() != null && !companyKwlObj.getEntityList().isEmpty()) {
            Company company = (Company) companyKwlObj.getEntityList().get(0);
            if (company != null) {
                if (company.getCurrency() != null) {
                    globalCurrencyId = company.getCurrency().getCurrencyID();
                }
                if (company.getCreator() != null) {
                    User user = company.getCreator();
                    userId = user.getUserID();
                    userFullName = StringUtil.getFullName(user);
                    userName = user.getFirstName();
                    userDateFormatId = (user.getDateFormat() == null) ? Constants.yyyyMMdd_formatid : user.getDateFormat().getFormatID();//"2" is DateFormatID for "yyyy-MM-dd"
                    userDateFormat = (user.getDateFormat() == null) ? Constants.yyyyMMdd : user.getDateFormat().getJavaForm();//Constants.yyyyMMdd is constant for DateFormat "yyyy-MM-dd"
                }
            }
        }
        paramJobj.put(Constants.globalCurrencyKey, globalCurrencyId);
        paramJobj.put(Constants.useridKey, userId);
        paramJobj.put(Constants.userfullname, userFullName);
        paramJobj.put(Constants.userid, userId);
        paramJobj.put(Constants.username, userName);
        paramJobj.put(Constants.dateformatid, userDateFormatId);
        paramJobj.put(Constants.userdateformat, userDateFormat);

        return paramJobj;
    }

    /**
     * Description: Gets columns mapping from database and puts into paramJobj
     * for import via Cron scheduler
     *
     * @param paramJobj
     * @return paramJobj (with additional mapping JSON)
     * @throws JSONException
     * @throws AccountingException
     * @throws ServiceException
     * @throws IOException
     */
    public JSONObject getColumnMappingJsonForScriptImport(JSONObject paramJobj) throws JSONException, AccountingException, ServiceException, IOException {
        JSONObject resJson = new JSONObject();
        JSONArray rootJarr = new JSONArray();
        FileInputStream fileInputStream = null;
        CsvReader csvReader = null;
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            String moduleid = paramJobj.optString(Constants.moduleid);
            int subModuleFlag = paramJobj.optInt(Constants.submoduleflag);
            String fileName = paramJobj.optString("filename");

            /**
             * ****************Open Import File and Read first Line to get Array
             * of Column Headers*******************
             */
            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            File filePath = new File(destinationDirectory + File.separator + fileName);
            fileInputStream = new FileInputStream(filePath);
            String delimiterType = paramJobj.getString("delimiterType");
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);
            csvReader.readRecord();
            String[] recarr = csvReader.getValues();
            /**
             * ****************Open Import File and Read first Line to get Array
             * of Column Headers*******************
             */

            /**
             * ****************Get Mapping details for column headers from file*******************
             */
            for (int index = 0; index < recarr.length; index++) {
                String fileHeader = recarr[index];
                paramJobj.put("fileHeader", fileHeader);//Add fileHeader for below call to importDao.getImportFileColumnMapping
                KwlReturnObject columnMappingKwlObj = importDao.getImportFileColumnMapping(paramJobj);
                List<ImportFileColumnMapping> columnMappingList = columnMappingKwlObj != null ? columnMappingKwlObj.getEntityList() : null;
                if (columnMappingList != null && !columnMappingList.isEmpty()) {
                    ImportFileColumnMapping importFileColumnMapping = (ImportFileColumnMapping) columnMappingList.get(0);
                    JSONObject mappingJobj = getColumnMappingForScriptImport(importFileColumnMapping, new JSONObject(), fileHeader, moduleid, subModuleFlag, companyid, index);
                    if (mappingJobj.has("dataindex")) {//put mappingJobj into Mapping JSONArray only if it is non-empty
                        rootJarr.put(mappingJobj);
                    }
                }
            }
            resJson.put("root", rootJarr);
            paramJobj.put("resjson", resJson);//resJson contains columns' mapping data. This key is used because it is used for other import calls.
            /**
             * ****************Get Mapping details for column headers from file*******************
             */

            /**
             * ****************Get details of columns for which default values
             * are to be used. Date format for SO date is also fetched from
             * database here. *******************
             */
            JSONObject defaultValuesMappingJson = new JSONObject();
            paramJobj.remove("fileHeader");//remove fileHeader from paramJobj as it is used in getImportFileColumnMapping method, but not required here
            paramJobj.put("isForDefaultValues", true);//required by getImportFileColumnMapping method
            paramJobj.put("isForDateFormat", true);//required by getImportFileColumnMapping method
            KwlReturnObject columnMappingKwlObj = importDao.getImportFileColumnMapping(paramJobj);
            List<ImportFileColumnMapping> columnMappingList = columnMappingKwlObj != null ? columnMappingKwlObj.getEntityList() : null;
            if (columnMappingList != null && !columnMappingList.isEmpty()) {
                for (ImportFileColumnMapping importFileColumnMapping : columnMappingList) {
                    if (importFileColumnMapping.getFieldMappingType() == 4) {
                        paramJobj.put("dateFormatForScriptImport", importFileColumnMapping.getDefaultValue());//Date format for SO date
                    } else {
                        defaultValuesMappingJson = getColumnMappingForScriptImport(importFileColumnMapping, defaultValuesMappingJson, null, moduleid, subModuleFlag, companyid, 0);
                    }
                }
            }
            paramJobj.put("defaultValuesMappingJson", defaultValuesMappingJson);//defaultValuesJobj contains columns' default values mapping data
            /**
             * ****************Get details of columns for which default values
             * are to be used. Date format for SO date is also fetched from
             * database here. *******************
             */

        } catch (FileNotFoundException ex) {
            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Could not read data from file. Please make sure that file exists at location and file name is correct.");
        } finally {
            if (csvReader != null) {
                csvReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
        return paramJobj;
    }

    /**
     * Description: Gets columns mapping from database and puts into paramJobj
     * for import via Cron scheduler
     *
     * @param importFileColumnMapping -> Class object containing mapping details
     * @param returnJobj -> JSONObject in which mapping details are to be put
     * @param fileHeader -> Header of column in csv file
     * @param moduleid
     * @param subModuleFlag -> required for custom field's mapping (value
     * depends on module)
     * @param companyid
     * @param index -> index of column in csv file
     * @return returnJobj (with additional mapping Json)
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject getColumnMappingForScriptImport(ImportFileColumnMapping importFileColumnMapping, JSONObject returnJobj, String fileHeader, String moduleid, int subModuleFlag, String companyid, int index) throws ServiceException, JSONException {
        String systemHeaderId = importFileColumnMapping.getSystemHeaderID();
        boolean customFlag = false, isLineItem = false;
        String detaIndex = "", columnName = "";
        boolean isFieldExists = false;//This flag is true only if a field's entry exists in database corresponding to importFileColumnMapping object
        int fieldMappingtype = importFileColumnMapping.getFieldMappingType();
        if (fieldMappingtype == 1 || fieldMappingtype == 3) {//Mapping for Custom Field
            FieldParams fieldParam = (FieldParams) kwlCommonTablesDAOObj.getClassObject(FieldParams.class.getName(), systemHeaderId);
            if (fieldParam != null) {
                customFlag = true;
                isLineItem = (fieldParam.getCustomcolumn() != 0);
                columnName = fieldParam.getFieldlabel() != null ? fieldParam.getFieldlabel() : "";
                if (StringUtil.equal(moduleid, String.valueOf(Constants.GSTModule))) {
                    detaIndex = fieldParam.getFieldname() != null ? fieldParam.getFieldname() : "";
                }
                isFieldExists = true;
            }
        } else if (fieldMappingtype == 0 || fieldMappingtype == 2) {//Mapping for Default Field
            DefaultHeader defaultHeader = (DefaultHeader) kwlCommonTablesDAOObj.getClassObject(DefaultHeader.class.getName(), systemHeaderId);
            if (defaultHeader != null) {
                customFlag = defaultHeader.isCustomflag();
                detaIndex = defaultHeader.getDataIndex() == null ? "" : defaultHeader.getDataIndex();
                if (customFlag) {
                    Map<String,Object> params=new HashMap<>();
                    params.put("moduleId", moduleid);
                    params.put("subModuleFlag", subModuleFlag);
                    params.put("defaultheader", defaultHeader);
                    columnName = importHandler.getColumnNameForModuleColumnConfig(params);
                } else {
                    columnName = defaultHeader.getPojoMethodName();
                }
                boolean isCurrencyCode = false;
                KwlReturnObject kobj = kwlCommonTablesDAOObj.getIsCurrenyCodeAndIsActivatedTodate(companyid);
                List listobj = kobj.getEntityList();
                if (listobj.size() > 0) {
                    Object[] row = (Object[]) listobj.get(0);
                    if (row != null) {
                        isCurrencyCode = Boolean.parseBoolean(row[0].toString());
                    }
                }
                if (!StringUtil.isNullOrEmpty(defaultHeader.getPojoMethodName()) && defaultHeader.getPojoMethodName().equals("Currency") && isCurrencyCode) {
                    columnName = "currencyCode";
                    detaIndex = defaultHeader.getDataIndex() == null ? "" : "currencyCode";
                }
                isFieldExists = true;
            }
        }
        if (isFieldExists) {
            if (fieldMappingtype == 2 || fieldMappingtype == 3) {//Default Value mapping
                returnJobj.put(detaIndex, importFileColumnMapping.getDefaultValue());
            } else {//Mapping of a column from import file with a system column
                returnJobj.put("csvindex", index);
                returnJobj.put("csvheader", fileHeader);
                returnJobj.put("columnname", columnName);
                returnJobj.put("dataindex", detaIndex);
                returnJobj.put("customflag", String.valueOf(customFlag));
                returnJobj.put("isLineItem", String.valueOf(isLineItem));
            }
        }
        return returnJobj;
    }

    /**
     * Description: Fetches values of Custom Fields for which values from
     * Customer Master are to be used and adds to custom fields' JSONArray
     *
     * @param requestJobj -> request parameters Jobj
     * @param customJArr -> JSONArray containing Custom Fields data for import
     * record
     * @param customerID -> ID of Customer corresponding to import record
     * @param moduleID
     * @return customJArr (with some additional custom fields' data)
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONArray createCustomFieldArrayForScriptImport(JSONObject requestJobj, JSONArray customJArr, String customerID, int moduleID) throws JSONException, ServiceException {
        List<String> fieldLabelList = new ArrayList<>();

        /**
         Get List of custom fields which have already been added to customJarr
         */
        for (int i = 0; i < customJArr.length(); i++) {
            fieldLabelList.add(customJArr.getJSONObject(i).optString("fieldname"));
        }
        /**
         Get List of custom fields which have already been added to customJarr
         */

        /*
         Below code fetches all custom fields for a company corresponding to SO module.
         Then it iterates over the list of fields and checks if a field is already in 'fieldLabelList'.
         If a column is not in 'fieldLabelList', it checks if the field has a value in for corresponding customer in Customer Master.
         In case field exists in Customer Master, the value corresponding to customer is assigned to the field.
         */
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        requestParams.put(Constants.filter_values, Arrays.asList(requestJobj.optString(Constants.companyKey), moduleID));
        KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module SO
        if (fieldParamsResult.getEntityList() != null && !fieldParamsResult.getEntityList().isEmpty()) {
            List<FieldParams> paramsList = fieldParamsResult.getEntityList();
            for (FieldParams fieldParam : paramsList) {
                if (fieldParam != null && !fieldLabelList.contains(fieldParam.getFieldlabel())) {
                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                    requestParams.put(Constants.filter_values, Arrays.asList(requestJobj.optString(Constants.companyKey), Constants.Acc_Customer_ModuleId, fieldParam.getFieldlabel()));
                    fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module Customer
                    if (fieldParamsResult.getEntityList() != null && !fieldParamsResult.getEntityList().isEmpty()) {
                        FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                        if (params != null) {
                            List<String> colNamesList = new ArrayList<>();
                            colNamesList.add("col" + params.getColnum());
                            KwlReturnObject customFieldKwlObj = accCustomerDAOobj.getCustomFieldsValuesForCustomer(customerID, requestJobj.optString(Constants.companyKey), colNamesList);
                            if (customFieldKwlObj.getEntityList() != null && !customFieldKwlObj.getEntityList().isEmpty()) {
                                Object value = customFieldKwlObj.getEntityList().get(0);
                                JSONObject customJObj = new JSONObject();
                                customJObj.put("fieldid", fieldParam.getId());
                                customJObj.put("filedid", fieldParam.getId());
                                customJObj.put("refcolumn_name", "Col" + fieldParam.getRefcolnum());
                                customJObj.put("fieldname", "Custom_" + fieldParam.getFieldlabel());
                                customJObj.put("xtype", fieldParam.getFieldtype());
                                customJObj.put("isMandatory", false);
                                customJObj.put("Custom_" + fieldParam.getFieldlabel(), "Col" + fieldParam.getColnum());
                                if (fieldParam.getIsessential() == 1) {
                                    customJObj.put("isMandatory", true);
                                }
                                String fieldComboDataStr = "";
                                if (fieldParam.getFieldtype() == 3) { // if field of date type
                                    customJObj.put("Col" + fieldParam.getColnum(), (Date) value);
                                    customJObj.put("fieldDataVal", (Date) value);
                                } else if (fieldParam.getFieldtype() == 4 || fieldParam.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                    String valueString = StringUtil.isNullObject(value) ? "" : (String) value;
                                    String[] fieldComboDataArr = valueString.split(";");
                                    for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                        fieldParamsResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), fieldComboDataArr[dataArrIndex]);
                                        FieldComboData fieldComboDataForCustomer = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                        if (fieldComboDataForCustomer != null) {
                                            requestParams = new HashMap<>();
                                            requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                            requestParams.put(Constants.filter_values, Arrays.asList(fieldParam.getId(), fieldComboDataForCustomer.getValue(), 0));
                                            fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                            if (fieldParamsResult.getEntityList() != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                FieldComboData fieldComboDataForSO = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                fieldComboDataStr += fieldComboDataForSO.getId() + ",";
                                            }
                                        }
                                    }
                                    if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                        customJObj.put("Col" + fieldParam.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                        customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                    }
                                } else if (fieldParam.getFieldtype() == 11) { // if field of check box type 
                                    customJObj.put("Col" + fieldParam.getColnum(), (Boolean) value);
                                    customJObj.put("fieldDataVal", (Boolean) value);
                                } else if (fieldParam.getFieldtype() == 12) { // if field of check list type
                                    requestParams = new HashMap<>();
                                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                    requestParams.put(Constants.filter_values, Arrays.asList(fieldParam.getId(), 0));
                                    fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                    List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();
                                    String[] fieldComboDataArr = ((String) value).split(";");
                                    for (FieldComboData fieldComboData : fieldComboDataList) {
                                        for (String fieldComboArrData : fieldComboDataArr) {
                                            if (fieldComboArrData != null && fieldComboArrData.replaceAll("\"", "").trim().equalsIgnoreCase(fieldComboData.getValue())) {
                                                fieldComboDataStr += fieldComboData.getId() + ",";
                                            }
                                        }
                                    }
                                    if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                        customJObj.put("Col" + fieldParam.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                        customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                    }
                                } else {
                                    customJObj.put("Col" + fieldParam.getColnum(), (String) value);
                                    customJObj.put("fieldDataVal", (String) value);
                                }

                                if (customJObj.has("fieldDataVal")) {//Field is to be added only when "fieldDataVal" key exists. This is because "fieldDataVal" key contains value fo custom field
                                    customJArr.put(customJObj);
                                }
                            }
                        }
                    }
                }
            }
        }
        return customJArr;
    }
    
    /**
     * Description: Method for business logic of import Sales Orders.
     *
     * @param requestJobj
     * @return
     * @throws AccountingException
     * @throws IOException
     * @throws JSONException
     */
    public JSONObject importSalesOrderRecordsForCSV(JSONObject requestJobj) throws AccountingException, IOException, JSONException {
        JSONObject returnObj = new JSONObject();
        String msg = "";
        int total = 0, failed = 0;
        String fileName = requestJobj.getString("filename");
        String companyID = requestJobj.getString(Constants.companyKey);
        String masterPreference = requestJobj.getString("masterPreference");
        Locale locale = (Locale) requestJobj.get("locale");
        boolean issuccess = true;
        boolean isAlreadyExist = false;
        boolean isRecordFailed = false;
        boolean isOpeningDocImport = false;
        FileInputStream fileInputStream = null;
        CsvReader csvReader = null;
        JSONObject paramJobj = new JSONObject();
        JSONArray rows = new JSONArray();
        String prevInvNo = "";
        Date prevSalesOrderDate = null; //Added to put check on previous SO-Date instead of previous SO-Number, for ERM-33
        String prevCustomerID = null; //Added to put check on previous SO-Date instead of previous SO-Number, for ERM-33
        boolean isImportByScript = requestJobj.optBoolean("isImportByScript", false);//Flag to indicate that the import is being done by script (ERM-33)
        double totaldiscount = 0, totalamount = 0;

        try {
            String dateFormat = null, dateFormatId = requestJobj.optString("dateFormat");
            JSONObject defaultValuesMappingJson = new JSONObject();//contains default values of fields for which fixed default values are to be used (ERM-33)
            if (isImportByScript) {
                dateFormat = requestJobj.optString("dateFormatForScriptImport");//Date format as per file to be imported by script
                defaultValuesMappingJson = requestJobj.optJSONObject("defaultValuesMappingJson") != null ? requestJobj.optJSONObject("defaultValuesMappingJson") : new JSONObject();
            } else if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            df.setLenient(false);

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");

            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            /**
             * Get country ID
             */
            KwlReturnObject kwlCompany = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
            Company company = (Company) kwlCompany.getEntityList().get(0);
            int companyCountryId = company.getCountry().getID() != null ? Integer.parseInt(company.getCountry().getID()) : 0;
            boolean isNewGST = extrareferences.isIsNewGST();
            Boolean isCurrencyCode = extrareferences.isCurrencyCode();
            
            if (requestJobj.has("isOpeningOrder") && requestJobj.optBoolean("isOpeningOrder")) {
               isOpeningDocImport=true;
            }

            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            File filePath = new File(destinationDirectory + File.separator + fileName);
            fileInputStream = new FileInputStream(filePath);
            String delimiterType = requestJobj.getString("delimiterType");
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);

            JSONObject resjson = new JSONObject(requestJobj.getString("resjson"));
            JSONArray jSONArray = resjson.getJSONArray("root");
            HashMap<String, Integer> columnConfig = new HashMap<>();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }
            
            int cnt = 0;
            StringBuilder failedRecords = new StringBuilder();
            HashMap currencyMap = getCurrencyMap(isCurrencyCode);

            while (csvReader.readRecord()) {
                String failureMsg = "";
                String[] recarr = csvReader.getValues();

                if (cnt == 0 && !isImportByScript) {//In case of import by script first row containing 'T' and 'L' is not there in import file
                    failedRecords.append(createCSVrecord(recarr)).append("\" \"");
                } else if ((cnt == 1 && !isImportByScript) || (cnt == 0 && isImportByScript)) {//In case of import by script first row containing 'T' and 'L' is not there in import file
                    failedRecords.append("\n").append(createCSVrecord(recarr)).append("\"Error Message\"");
                } else {
                    try {
                        String currencyID = requestJobj.getString(Constants.globalCurrencyKey);

                        String salesOrderNumber = "";
                        if (columnConfig.containsKey("salesOrderNumber")) {
                            salesOrderNumber = recarr[(Integer) columnConfig.get("salesOrderNumber")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(salesOrderNumber)) {
                                failureMsg += "Sales Order Number is not available. ";
                            }
                        } else if (isImportByScript) {
                            Map<String, Object> filterParams = new HashMap<String, Object>();
                            filterParams.put(Constants.companyKey, companyID);
                            filterParams.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                            filterParams.put("isdefaultFormat", true);
                            KwlReturnObject sequenceFormatKwlObj = accCompanyPreferencesObj.getSequenceFormat(filterParams);
                            if (sequenceFormatKwlObj.getEntityList() == null || sequenceFormatKwlObj.getEntityList().isEmpty()) {
                                failureMsg += "Default Sequence Format for Sales Orders is not set. ";
                            }
                        } else {
                            failureMsg += "Sales Order Number column is not found. ";
                        }

                        Date salesOrderDate = null,bookbeginningdate = null;
                        if (columnConfig.containsKey("OrderDate")) {
                            String salesOrderDateStr = recarr[(Integer) columnConfig.get("OrderDate")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(salesOrderDateStr)) {
                                failureMsg += "Sales Order Date is not available. ";
                            } else {
                                try {
                                    salesOrderDate = df.parse(salesOrderDateStr);
                                    /* In UI we are not allowing user to give transaction date  on or after book beginning date
                                     below code is for the same purpose */
                                    if (isOpeningDocImport) {
                                        KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
                                        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                                        salesOrderDate = CompanyPreferencesCMN.removeTimefromDate(salesOrderDate);
                                        bookbeginningdate = CompanyPreferencesCMN.removeTimefromDate(preferences.getBookBeginningFrom());
                                        if (salesOrderDate.after(bookbeginningdate)) {
                                            failureMsg += messageSource.getMessage("acc.transactiondate.beforebbdate", null, locale);
                                        }
                                    }
                                    
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Sales Order Date, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        } else {
                            failureMsg += "Sales Order Date column is not found. ";
                        }

                        String customerPORefNo = "";
                        if (columnConfig.containsKey("customerPORefNo")) {
                            customerPORefNo = recarr[(Integer) columnConfig.get("customerPORefNo")].replaceAll("\"", "").trim();
                        }

                        String costCenterID = "";
                        if (columnConfig.containsKey("costcenter")) {
                            String costCenterName = recarr[(Integer) columnConfig.get("costcenter")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(costCenterName)) {
                                costCenterID = getCostCenterIDByName(costCenterName, companyID);
                                if (StringUtil.isNullOrEmpty(costCenterID)) {
                                    failureMsg += "Cost Center is not found for name " + costCenterName + ". ";
                                }
                            }
                        }

                        Date shipDate = null;
                        if (columnConfig.containsKey("shipdate")) {
                            String shipDateStr = recarr[(Integer) columnConfig.get("shipdate")].replaceAll("\"", "").trim();

                            if (!StringUtil.isNullOrEmpty(shipDateStr)) {
                                try {
                                    shipDate = df.parse(shipDateStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Ship Date, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        }

                        String customerID = "";

                        /*
                         * 1. Customer Code
                         */
                        if (columnConfig.containsKey("customerCode")) {
                            String customerCode = recarr[(Integer) columnConfig.get("customerCode")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(customerCode)) {
                                Customer customer = getCustomerByCode(customerCode, companyID);
                                if (customer != null) {
                                    customerID = customer.getID();
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.CustomerCodeisnotavailable", null, locale) + customerCode + ". ";
                                }
                            }
                        }

                        /*
                         * 2. Customer Name if customerID is empty it means
                         * customer is not found for given code. so need to
                         * search data on name
                         */
                        if (StringUtil.isNullOrEmpty(customerID)) {
                            if (columnConfig.containsKey("customerName")) {
                                String customerName = recarr[(Integer) columnConfig.get("customerName")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(customerName)) {
                                    Customer customer = null;
                                    KwlReturnObject retObj = accCustomerDAOobj.getCustomerByName(customerName, companyID);
                                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                        customer = (Customer) retObj.getEntityList().get(0);
                                    }
                                    if (customer != null) {
                                        customerID = customer.getID();
                                    } else {
                                        failureMsg += messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, locale) + ". ";
                                    }
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, locale) + ".";
                                }
                            } else if (!isImportByScript) {//Customer Name error message is not to be shown for script import as customer name column is not present in file (ERM-33)
                                failureMsg += messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, locale) + ".";
                            }
                        }

                        String termID = "";
                        if (columnConfig.containsKey("term")) {
                            String termName = recarr[(Integer) columnConfig.get("term")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(termName)) {
                                termID = getTermIDByName(termName, companyID);
                                if (StringUtil.isNullOrEmpty(termID)) {
                                    failureMsg += "Credit Term is not found for name " + termName + ". ";
                                }
                            } else {
                                failureMsg += "Credit Term is not available. ";
                            }
                        } else if (isImportByScript && !StringUtil.isNullOrEmpty(customerID)) {
                            KwlReturnObject customerKwlObj = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerID);
                            Customer customer = (Customer) customerKwlObj.getEntityList().get(0);
                            if (customer.getCreditTerm() != null) {
                                termID = customer.getCreditTerm().getID();
                            } else {
                                failureMsg += "Credit Term column is not set for respective Customer. ";
                            }
                        } else {
                            failureMsg += "Credit Term column is not found. ";
                        }

                        String memo = "";
                        if (columnConfig.containsKey("memo")) {
                            memo = recarr[(Integer) columnConfig.get("memo")].replaceAll("\"", "").trim();
                        }

                        String shipVia = "";
                        if (columnConfig.containsKey("shipvia")) {
                            shipVia = recarr[(Integer) columnConfig.get("shipvia")].replaceAll("\"", "").trim();
                        }

                        String fob = "";
                        if (columnConfig.containsKey("fob")) {
                            fob = recarr[(Integer) columnConfig.get("fob")].replaceAll("\"", "").trim();
                        }

                        String salesPersonID = "";
                        if (columnConfig.containsKey("salesperson")) {
                            String salesPersonName = recarr[(Integer) columnConfig.get("salesperson")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(salesPersonName)) {
                                salesPersonID = getSalesPersonIDByName(salesPersonName, companyID);
                                if (StringUtil.isNullOrEmpty(salesPersonID)) {
                                    failureMsg += "Sales Person is not found for name " + salesPersonName + ". ";
                                }
                            }
                        }

                        if (isCurrencyCode ? columnConfig.containsKey("currencyCode") : columnConfig.containsKey("currencyName")) {
                            String currencyStr = isCurrencyCode ? recarr[(Integer) columnConfig.get("currencyCode")].replaceAll("\"", "").trim() : recarr[(Integer) columnConfig.get("currencyName")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(currencyStr)) {
                                currencyID = getCurrencyId(currencyStr, currencyMap);

                                if (StringUtil.isNullOrEmpty(currencyID)) {
                                    failureMsg += messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, locale) + ". ";
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg += "Currency is not available. ";
                                }
                            }
                        } else if (isImportByScript && !StringUtil.isNullOrEmpty(customerID)) {
                            KwlReturnObject customerKwlObj = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerID);
                            Customer customer = (Customer) customerKwlObj.getEntityList().get(0);
                            if (customer.getCurrency() != null) {
                                currencyID = customer.getCurrency().getCurrencyID();
                            } else {
                                failureMsg += "Currency is not set for respective Customer. ";
                            }
                        }

                        boolean isIncludingGST = false;
                        /**
                         * Handle Check For IMport sales order GST Records
                         */
                        String taxID = "";
                        boolean isIncludeProductTax = false;
                        boolean isIncludeTotalTax = false;
                        if (!isNewGST) { // Not For US And INDIA - (GST)
                        /**
                         * If Avalara Integration is enabled, then includingGST flag is always false
                         */
                        if (columnConfig.containsKey("gstIncluded") && !extrareferences.isAvalaraIntegration()) {
                            String isIncludingGSTStr = recarr[(Integer) columnConfig.get("gstIncluded")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(isIncludingGSTStr)) {
                                if (isIncludingGSTStr.equalsIgnoreCase("TRUE")) {
                                    isIncludingGST = true;
                                } else if (isIncludingGSTStr.equalsIgnoreCase("FALSE")) {
                                    isIncludingGST = false;
                                } else {
                                    failureMsg += "Format you entered for Include GST is not correct. It should be like \"TRUE\" or \"FALSE\". ";
                                }
                            }
                        }

                        if (columnConfig.containsKey("includeprotax")) {
                            String isIncludeProductTaxStr = recarr[(Integer) columnConfig.get("includeprotax")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(isIncludeProductTaxStr)) {
                                if (isIncludeProductTaxStr.equalsIgnoreCase("Yes")) {
                                    isIncludeProductTax = true;
                                } else if (isIncludeProductTaxStr.equalsIgnoreCase("No")) {
                                    isIncludeProductTax = false;
                                } else {
                                    failureMsg += "Format you entered for Include Product Tax is not correct. It should be like \"Yes\" or \"No\". ";
                                }
                            }
                        }

                        if (isIncludingGST && !isIncludeProductTax) {
                            failureMsg += "If value Including GST is \"TRUE\" then value of Include Product Tax should be \"Yes\". ";
                        }

                        if (columnConfig.containsKey("taxincluded")) {
                            String isIncludeTotalTaxStr = recarr[(Integer) columnConfig.get("taxincluded")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(isIncludeTotalTaxStr)) {
                                if (isIncludeTotalTaxStr.equalsIgnoreCase("Yes")) {
                                    isIncludeTotalTax = true;
                                } else if (isIncludeTotalTaxStr.equalsIgnoreCase("No")) {
                                    isIncludeTotalTax = false;
                                } else {
                                    failureMsg += "Format you entered for Include Total Tax is not correct. It should be like \"Yes\" or \"No\". ";
                                }
                            }
                        }

                        if (isIncludeProductTax && isIncludeTotalTax) {
                            failureMsg += "If value of Include Product Tax is \"Yes\" then value of Include Total Tax should be \"No\".";
                        }

                        if (columnConfig.containsKey("taxid")) {
                            String taxCode = recarr[(Integer) columnConfig.get("taxid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(taxCode)) {
//                                Tax tax = getGSTByCode(taxCode, companyID);
                                Map taxMap = new HashMap<>();
                                taxMap.put(Constants.companyKey, companyID);
                                taxMap.put(Constants.TAXCODE, taxCode);
                                ArrayList taxList = importHandler.getTax(taxMap);
                                if (taxList.get(0) != null) {
                                    Tax tax = (Tax) taxList.get(0);
                                    if (tax.getTaxtype() == Constants.PURCHASE_TYPE_TAX && isIncludeTotalTax) {
                                        failureMsg += "Tax Code is not Sales Type TAX for code " + taxCode;
                                    } else {
                                        taxID = tax.getID();
                                    }
                                } else if (!StringUtil.isNullOrEmpty((String) taxList.get(2))) {
                                    failureMsg += (String) taxList.get(2) + taxCode;
                                }
                            } else {
                                if (!isIncludingGST && !isIncludeProductTax && isIncludeTotalTax) {
                                    failureMsg += "Tax Code is not available. ";
                                }
                            }
                        } else {
                            if (!isIncludingGST && !isIncludeProductTax && isIncludeTotalTax) {
                                failureMsg += "Tax Code column is not found. ";
                            }
                        }
                        }
                        
                        boolean islockQuantity = false;
                        if (columnConfig.containsKey("islockQuantity")) {
                            String islockQuantityStr = recarr[(Integer) columnConfig.get("islockQuantity")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(islockQuantityStr)) {
                                if (islockQuantityStr.equalsIgnoreCase("TRUE")) {
                                    islockQuantity = true;
                                } else if (islockQuantityStr.equalsIgnoreCase("FALSE")) {
                                    islockQuantity = false;
                                } else {
                                    failureMsg += "Format you entered for Block Quantity is not correct. It should be like \"TRUE\" or \"FALSE\". ";
                                }
                            }
                        }
                        
                        Product product = null;
                        if (columnConfig.containsKey("productID")) {
                            String productID = recarr[(Integer) columnConfig.get("productID")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productID)) {
                                product = getProductByProductID(productID, companyID);
                                if (product == null) {
                                    failureMsg += "Product ID is not found for " + productID + ". ";
                                }
                            } else {
                                failureMsg += "Product ID is not available. ";
                            }
                        } else {
                            failureMsg += "Product ID column is not found. ";
                        }
                        
                        String desc = "";
                        if (columnConfig.containsKey("desc")) {
                            desc = recarr[(Integer) columnConfig.get("desc")].replaceAll("\"", "").trim();
                        }

                        double quantity = 0;
                        if (columnConfig.containsKey("quantity")) {
                            String quantityStr = recarr[(Integer) columnConfig.get("quantity")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(quantityStr)) {
                                failureMsg += "Quantity is not available. ";
                            } else {
                                try {
                                    quantity = authHandler.roundQuantity(Double.parseDouble(quantityStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Quantity, Please ensure that value type of Quantity matches with the Quantity. ";
                                }
                            }
                        } else if (defaultValuesMappingJson.has("quantity")) {
                            quantity = defaultValuesMappingJson.optDouble("quantity");//Quantity is 1 by default for proprietary file import
                        } else {
                            failureMsg += "Quantity column is not found. ";
                        }

                        double unitPriceIncludingGST = 0;
                        /**
                         * Include GST not Considered
                         */
                        if (!isNewGST) { // Not For US and INDIA (GST)
                        if (columnConfig.containsKey("rateIncludingGst")) {
                            String unitPriceIncludingGSTStr = recarr[(Integer) columnConfig.get("rateIncludingGst")].replaceAll("\"", "").trim();
                            if (isIncludingGST && StringUtil.isNullOrEmpty(unitPriceIncludingGSTStr)) {
                                throw new AccountingException("Unit Price Including GST is not available. ");
                            } else if (!StringUtil.isNullOrEmpty(unitPriceIncludingGSTStr)) {
                                try {
                                    unitPriceIncludingGST = authHandler.roundUnitPrice(Double.parseDouble(unitPriceIncludingGSTStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Unit Price Including GST, Please ensure that value type of Unit Price matches with the Unit Price Including GST. ";
                                }
                            }
                        } else {
                            if (isIncludingGST) {
                                throw new AccountingException("Unit Price Including GST column is not found. ");
                            }
                        }
                        }

                        double unitPrice = 0;
                        if (columnConfig.containsKey("rate")) {
                            String unitPriceStr = recarr[(Integer) columnConfig.get("rate")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(unitPriceStr)) {
                                failureMsg += "Unit Price is not available. ";
                            } else {
                                try {
                                    unitPrice = authHandler.roundUnitPrice(Double.parseDouble(unitPriceStr),companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Unit Price, Please ensure that value type of Unit Price matches with the Unit Price. ";
                                }
                            }
                        } else if (isImportByScript) {
                            if (!StringUtil.isNullOrEmpty(customerID) && product != null) {
                                KwlReturnObject customerKwlObj = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerID);
                                Customer customer = (Customer) customerKwlObj.getEntityList().get(0);
                                if (customer.getPricingBandMaster() != null) {
                                    String pricingBandMasterID = customer.getPricingBandMaster().getID();
                                    HashMap<String, Object> filterRequestParams = new HashMap<>();
                                    List<String> filter_names = new ArrayList<String>();
                                    filter_names.add("pricingBandMaster.ID");
                                    filter_names.add("company.companyID");
                                    filter_names.add("product");
                                    List<String> filter_params = new ArrayList<String>();
                                    filter_params.add(pricingBandMasterID);
                                    filter_params.add(companyID);
                                    filter_params.add(product.getID());
                                    filterRequestParams.put(Constants.filter_names, filter_names);
                                    filterRequestParams.put(Constants.filter_params, filter_params);
                                    KwlReturnObject detailObj = accMasterItemsDAOobj.getPricingBandMasterDetailsList(filterRequestParams);
                                    List<PricingBandMasterDetail> detailList = detailObj.getEntityList();
                                    if (detailList != null && !detailList.isEmpty()) {
                                        PricingBandMasterDetail pricingBandMasterDetail = detailList.get(0);
                                        double salesPrice = pricingBandMasterDetail.getSalesPrice();
                                        unitPrice = authHandler.roundUnitPrice(salesPrice,companyID);
                                    } else {
                                        failureMsg += "Price Band is not set for respective Product and Customer. ";
                                    }
                                } else {
                                    failureMsg += "Price Band is not set for respective Customer. ";
                                }
                            }
                        } else {
                            failureMsg += "Unit Price column is not found. ";
                        }

                        UnitOfMeasure uom = null;
                        if (columnConfig.containsKey("uomname")) {
                            String productUOMName = recarr[(Integer) columnConfig.get("uomname")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                uom = getUOMByName(productUOMName, companyID);
                                if (uom == null) {
                                    if (!masterPreference.equalsIgnoreCase("1")) {
                                        failureMsg += "Product Unit Of Measure is not found for " + productUOMName + ". ";
                                    }
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg += "Product Unit Of Measure is not available. ";
                                }
                            }
                        } else if (defaultValuesMappingJson.has("uomname")) {
                            String productUOMName = defaultValuesMappingJson.optString("uomname");
                            uom = getUOMByName(productUOMName, companyID);
                            if (uom == null) {
                                failureMsg += "Product Unit Of Measure is not found for '" + productUOMName + "'. ";
                            }
                        }

                        int discountType = 1;
                        if (columnConfig.containsKey("discountType")) {
                            String discountTypeStr = recarr[(Integer) columnConfig.get("discountType")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(discountTypeStr)) {
                                if (discountTypeStr.equalsIgnoreCase("Percentage")) {
                                    discountType = 1;
                                } else if (discountTypeStr.equalsIgnoreCase("Flat")) {
                                    discountType = 0;
                                } else {
                                    failureMsg += "Format you entered is not correct. It should be like \"Percentage\" or \"Flat\". ";
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg += "Discount Type is not available. ";
                                }
                            }
                        }

                        double discount = 0;
                        if (columnConfig.containsKey("discount")) {
                            String discountStr = recarr[(Integer) columnConfig.get("discount")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(discountStr)) {
                                try {
                                    discount = authHandler.roundQuantity(Double.parseDouble(discountStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Discount, Please ensure that value type of Discount matches with the Discount. ";
                                }
                            }
                        }

                        Tax rowtax = null;
                        /**
                         * Product tax ID Not used for INDIA
                         */
                        if (!isNewGST) { // Not For US and INDIA (GST)
                        if (columnConfig.containsKey("prtaxid")) {
                            String taxCode = recarr[(Integer) columnConfig.get("prtaxid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(taxCode)) {
//                                rowtax = getGSTByCode(taxCode, companyID);
                                Map taxMap = new HashMap<>();
                                taxMap.put(Constants.companyKey, companyID);
                                taxMap.put(Constants.TAXCODE, taxCode);
                                ArrayList taxList = importHandler.getTax(taxMap);
                                if (taxList.get(0) != null) {
                                    rowtax = (Tax) taxList.get(0);
                                    if (rowtax.getTaxtype() == Constants.PURCHASE_TYPE_TAX && isIncludeProductTax) {
                                        failureMsg += "Tax Code is not Sales Type TAX for code " + taxCode;
                                    }
                                } else if (!StringUtil.isNullOrEmpty((String) taxList.get(2))) {
                                    failureMsg += (String) taxList.get(2) + taxCode;
                                }
                            } else {
                                if (isIncludeProductTax) {
                                    failureMsg += "Product Tax is not available. ";
                                }
                            }
                        } else {
                            if (isIncludeProductTax) {
                                failureMsg += "Product Tax column is not found. ";
                            }
                        }
                        }

                        String rowtaxamount = "0";
                        if (columnConfig.containsKey("taxamount")) {
                            rowtaxamount = recarr[(Integer) columnConfig.get("taxamount")].replaceAll("\"", "").trim();
                        }
                        
                        /*
                         * Vendor Code
                         */
                        String vendorID = "";
                        if (columnConfig.containsKey("vendorid")) {
                            String vendorCode = recarr[(Integer) columnConfig.get("vendorid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(vendorCode)) {
                                Vendor vendor = getVendorByCode(vendorCode, companyID);
                                if (vendor != null) {
                                    vendorID = vendor.getID();
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.VendorCodeisnotavailable", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + vendorCode + ". ";
                                }
                            }
                        }
                        
                        double vendorUnitCost = 1;
                        if (columnConfig.containsKey("vendorunitcost")) {
                            String vendorUnitCostStr = recarr[(Integer) columnConfig.get("vendorunitcost")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(vendorUnitCostStr)) {
                                try {
                                    vendorUnitCost = authHandler.roundQuantity(Double.parseDouble(vendorUnitCostStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Unit Cost, Please ensure that value type of Unit Cost matches with the Unit Cost. ";
                                }
                            }
                        }
                        
                        double vendorCurrencyExchangeRate = 1;
                        if (columnConfig.containsKey("vendorcurrexchangerate")) {
                            String vendorCurrencyExchangeRateStr = recarr[(Integer) columnConfig.get("vendorcurrexchangerate")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(vendorCurrencyExchangeRateStr)) {
                                try {
                                    vendorCurrencyExchangeRate = authHandler.roundQuantity(Double.parseDouble(vendorCurrencyExchangeRateStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Vendor Currency Exchange Rate, Please ensure that value type of Vendor Currency Exchange Rate matches with the Vendor Currency Exchange Rate. ";
                                }
                            }
                        }
                        /**
                         * Check All Validation For Import Sales order with INDIA GST Rule
                         */
                        double gstTaxAmt=0.0;
                        JSONArray gstTermTaxJArr=new JSONArray();
                        JSONObject gstTermTaxJObj=new JSONObject();
                        if (isNewGST) {
                            if (columnConfig.containsKey("termtax")) {
                                String notAvailable = "";
                                JSONObject paramObj = new JSONObject();
                                paramObj.put("companyid", companyID);
                                paramObj.put("termType", 7);
                                paramObj.put("isInput", true);
                                paramObj.put("onlyTermTaxName", true);
                                KwlReturnObject kwlReturnObject = accTermObj.getGSTTermDetails(paramObj);
                                List<String> lineLevelTerms = kwlReturnObject.getEntityList();
                                String termTax = recarr[(Integer) columnConfig.get("termtax")].replaceAll("\"", "").trim();
                                String termTaxes[] = StringUtil.isNullOrEmpty(termTax)? new String[0] : termTax.split(",");
                                for (int taxCount = 0; taxCount < termTaxes.length; taxCount++) {
                                    if (!lineLevelTerms.contains(termTaxes[taxCount])) {
                                        notAvailable += termTaxes[taxCount] + ",";
                                    }
                                }
                                if (!StringUtil.isNullOrEmpty(notAvailable)) {
                                    notAvailable = notAvailable.substring(0, notAvailable.length() - 1);
                                    failureMsg += "Term Tax is not found for " + notAvailable+". ";
                                }
                                /*
                                Calculate Discount and after discount price
                                */
                                double rate = authHandler.round(unitPrice, companyID);
                                double rowAmount = authHandler.round(rate * authHandler.round(quantity, companyID), companyID);
                                rowAmount = authHandler.round(rowAmount, companyID);

                                double rowdiscount = discount;
                                if (discountType == 1) { // for percent disc
                                    rowdiscount = (rowAmount * discount) / 100;
                                }
                                rowdiscount = authHandler.round(rowdiscount, companyID);
                                double finalPrice = rowAmount - rowdiscount;
                                //*********************End Discount Calculation*******************/
                                
                                String termPercentages[] = new String[0];
                                if (columnConfig.containsKey("termpercentage")) {
                                    String percentage = recarr[(Integer) columnConfig.get("termpercentage")].replaceAll("\"", "").trim();
                                    if (!StringUtil.isNullOrEmpty(percentage)) {
                                        termPercentages = percentage.split(",");
                                        for (int percentCnt = 0; percentCnt < termPercentages.length; percentCnt++) {
                                            try {
                                                String valueWithoutPercentSymbol = termPercentages[percentCnt].replace('%', ' ').trim();
                                                double termPercent = Double.parseDouble(valueWithoutPercentSymbol);
                                                if (termPercent > 100) {
                                                    failureMsg += "Term Amount cannot be greater than 100 percentage. ";
                                                }else{
                                                    gstTaxAmt += authHandler.round((finalPrice) *  (termPercent / 100), companyID);
                                                }
                                            } catch (Exception ex) {
                                                failureMsg += "Incorrect numeric value for Term Amount. ";
                                            }
                                        }
                                        if (termPercentages.length != termTaxes.length) {
                                            failureMsg += "Number of Term Tax and number of Term Amount should match. ";
                                        }
                                    } else if(!StringUtil.isNullOrEmpty(termTax)){
                                        failureMsg += "Term Amount is not available. ";
                                    }
                                }
                                if(StringUtil.isNullOrEmpty(failureMsg)){
                                    paramObj=new JSONObject();
                                    int count=0;
                                    paramObj.put("companyid", companyID);
                                    paramObj.put("termType", 7);
                                    paramObj.put("isInput", true);
                                    paramObj.put("termname", "'" + StringUtil.join("','", termTax.split(",")) + "'");
                                    Map<String,String> taxTermAmount = new HashMap<String,String>();
                                    if (termTaxes.length == termPercentages.length) {
                                        for (int i = 0; i < termTaxes.length; i++) {
                                            String termname = termTaxes[i];
                                            taxTermAmount.put(termname.trim(), termPercentages[i]);
                                        }
                                    }else{
                                        failureMsg += "Term Amount and Term Tax not matched.";
                                    }

                                    KwlReturnObject kwlLineLevelTermsReturnObject = accTermObj.getGSTTermDetails(paramObj);
                                    List<LineLevelTerms> lineLevelTermsListObj = kwlLineLevelTermsReturnObject.getEntityList();
                                    for(LineLevelTerms lineLevelTermsObj:lineLevelTermsListObj){
                                        
                                        String valueWithoutPercentSymbol = taxTermAmount.containsKey(lineLevelTermsObj.getTerm()) ? (taxTermAmount.get(lineLevelTermsObj.getTerm()).replace('%', ' ').trim()) : "0";
                                        double termPercent = Double.parseDouble(valueWithoutPercentSymbol);
                                        
                                        JSONArray customJArr = createGlobalCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.GSTModule);
                                        JSONObject jobjRuleParams = new JSONObject();
                                        jobjRuleParams.put("product", product);
                                        jobjRuleParams.put("salesOrderDate", salesOrderDate);
                                        jobjRuleParams.put(Constants.companyKey, companyID);
                                        jobjRuleParams.put("linelevelterms", lineLevelTermsObj.getId());
                                        jobjRuleParams.put("lineleveltermsName", lineLevelTermsObj.getTerm());
                                        jobjRuleParams.put("termPercent", termPercent);
                                        jobjRuleParams.put("customerID", customerID);
                                        jobjRuleParams.put(Constants.COUNTRY_ID, companyCountryId);
                                        JSONObject GSTRulejsonObj = getGSTRuleImportSalesOrder(jobjRuleParams, customJArr);
                                        if(!StringUtil.isNullOrEmpty(GSTRulejsonObj.optString("failureMSG", ""))){
                                            failureMsg += GSTRulejsonObj.optString("failureMSG", "");
                                        }
                                        String productentitytermid = GSTRulejsonObj.optString("productentitytermid", "");
                                        gstTermTaxJObj.put("termid",lineLevelTermsObj.getId());
                                        gstTermTaxJObj.put("termamount",authHandler.round((finalPrice) * (termPercent / 100), companyID));
                                        gstTermTaxJObj.put("termpercentage",valueWithoutPercentSymbol);
                                        gstTermTaxJObj.put("purchasevalueorsalevalue",lineLevelTermsObj.getPurchaseValueOrSaleValue());
                                        gstTermTaxJObj.put("deductionorabatementpercent",lineLevelTermsObj.getDeductionOrAbatementPercent());
                                        gstTermTaxJObj.put("taxtype",lineLevelTermsObj.getTaxType());
                                        gstTermTaxJObj.put("taxvalue",termPercent);
                                        gstTermTaxJObj.put("isDefault",true);
                                        gstTermTaxJObj.put("productentitytermid",productentitytermid);       
                                        gstTermTaxJObj.put("salesOrPurchase",lineLevelTermsObj.isSalesOrPurchase());
                                        gstTermTaxJObj.put("productid", product != null ? product.getID() : ""); 
                                        gstTermTaxJObj.put("IsOtherTermTaxable",lineLevelTermsObj.isOtherTermTaxable());
                                        gstTermTaxJObj.put("glaccount", lineLevelTermsObj.getAccount().getID());
                                        gstTermTaxJObj.put(Constants.useridKey, requestJobj.optString(Constants.useridKey, ""));
                                        gstTermTaxJArr.put(gstTermTaxJObj);
                                        gstTermTaxJObj=new JSONObject();
                                    }
                                }
                            }
                        }
                        // creating invoice json
                        if ((!isImportByScript && (!prevInvNo.equalsIgnoreCase(salesOrderNumber) || salesOrderNumber.equalsIgnoreCase(""))) || (isImportByScript && (!(salesOrderDate.equals(prevSalesOrderDate)) || (salesOrderDate.equals(prevSalesOrderDate) && !StringUtil.equal(customerID, prevCustomerID))))) {
                            prevInvNo = salesOrderNumber;
                            prevSalesOrderDate = salesOrderDate;
                            prevCustomerID = customerID;

                            if (rows.length() > 0 && !isRecordFailed) {
                                double taxamount = 0.0;
                                if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxid", null))) {
                                    HashMap<String, Object> taxParams = new HashMap<>();
                                    taxParams.put("transactiondate", sdf.parse(paramJobj.getString("billdate")));
                                    taxParams.put("taxid", paramJobj.getString("taxid"));
                                    taxParams.put("companyid", companyID);
                                    KwlReturnObject taxResult = accTaxObj.getTax(taxParams);
                                    Object[] taxRow = (Object[]) taxResult.getEntityList().get(0);
                                    double taxPercentage = (double) taxRow[1];
                                    taxamount = ((totalamount - totaldiscount) * taxPercentage) / 100;
                                    taxamount = authHandler.round(taxamount, companyID);
                                }
                                paramJobj.put("taxamount", String.valueOf(taxamount));
                                paramJobj.put(Constants.detail, rows.toString());
                                paramJobj.put(Constants.PAGE_URL, requestJobj.optString(Constants.PAGE_URL));
                                // for save PO
                                saveSalesOrderJSON(paramJobj);
                            }
                            
                            // reset variables
                            totaldiscount = 0;
                            totalamount = 0;
                            paramJobj = new JSONObject();
                            rows = new JSONArray();
                            isRecordFailed = false;
                            isAlreadyExist = false;
                            
                            KwlReturnObject result = accSalesOrderDAOobj.getSalesOrderCount(salesOrderNumber, companyID);
                            int nocount = result.getRecordTotalCount();
                            if (nocount > 0) {
                                isAlreadyExist = true;
                                throw new AccountingException("Sales Order number'" + salesOrderNumber + "' already exists.");
                            }

                            if (isImportByScript) {    //Generate SO Number for import by script
                                Map<String, Object> filterParams = new HashMap<String, Object>();
                                filterParams.put(Constants.companyKey, companyID);
                                filterParams.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                                filterParams.put("isdefaultFormat", true);
                                result = accCompanyPreferencesObj.getSequenceFormat(filterParams);
                                List sequenceFormatList = result.getEntityList();
                                if (sequenceFormatList != null && !sequenceFormatList.isEmpty()) {
                                    SequenceFormat sequenceFormat = (SequenceFormat) sequenceFormatList.get(0);
                                    Map<String, Object> seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyID, StaticValues.AUTONUM_SALESORDER, sequenceFormat.getID(), false, salesOrderDate);
                                    salesOrderNumber = seqNumberMap.containsKey(Constants.AUTO_ENTRYNUMBER) ? (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER) : "";
                                }
                            }
                            
                            // For create custom field array
                            JSONArray customJArr = createGlobalCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Sales_Order_ModuleId);
                            if (isImportByScript) {
                                customJArr = createCustomFieldArrayForScriptImport(requestJobj, customJArr, customerID, Constants.Acc_Sales_Order_ModuleId);
                            }
                            
                            // For getting exchange rate
                            double exchangeRateForTransaction = getExchangeRateForTransaction(requestJobj, salesOrderDate, currencyID);

                            String sequenceFormatID = "NA";
                            boolean autogenerated = false;
                            if (!StringUtil.isNullOrEmpty(salesOrderNumber)) {
                                Map<String, String> sequenceNumberDataMap = new HashMap<String, String>();
                                sequenceNumberDataMap.put("moduleID", String.valueOf(Constants.Acc_Sales_Order_ModuleId));
                                sequenceNumberDataMap.put("entryNumber", salesOrderNumber);
                                sequenceNumberDataMap.put("companyID", companyID);
                                List list = importHandler.checksEntryNumberForSequenceNumber(sequenceNumberDataMap);
                                if (!list.isEmpty()) {
                                    boolean isvalidEntryNumber = (Boolean) list.get(0);
                                    if (!isvalidEntryNumber) {
                                        String formatID = (String) list.get(2);
                                        int intSeq = (Integer) list.get(3);
                                        paramJobj.put(Constants.SEQNUMBER, intSeq);
                                        paramJobj.put(Constants.SEQFORMAT, formatID);
                                        autogenerated = true;
                                        sequenceFormatID = formatID;
                                    }
                                }
                            }
                            
                            // param obj
                            paramJobj.put(Constants.companyKey, companyID);
                            paramJobj.put(Constants.globalCurrencyKey, requestJobj.getString(Constants.globalCurrencyKey));
                            paramJobj.put(Constants.useridKey, requestJobj.getString(Constants.useridKey));
                            paramJobj.put(Constants.userfullname, requestJobj.getString(Constants.userfullname));
                            paramJobj.put(Constants.reqHeader, requestJobj.getString(Constants.reqHeader));
                            paramJobj.put(Constants.remoteIPAddress, requestJobj.getString(Constants.remoteIPAddress));
                            paramJobj.put(Constants.language, requestJobj.getString(Constants.language));
                            paramJobj.put(Constants.currencyKey, currencyID);
                            paramJobj.put("number", salesOrderNumber);
                            paramJobj.put("sequenceformat", sequenceFormatID);
                            paramJobj.put("autogenerated", autogenerated);
                            paramJobj.put("customer", customerID);
                            paramJobj.put("customerporefno", customerPORefNo);
                            paramJobj.put("defaultAdress", "true");
                            paramJobj.put("memo", memo);
                            paramJobj.put("posttext", "");
                            paramJobj.put("termid", termID);
                            paramJobj.put("billdate", sdf.format(salesOrderDate));
                            paramJobj.put("perdiscount", "false");
                            paramJobj.put("discount", "0");
                            paramJobj.put("includingGST", String.valueOf(isIncludingGST));
                            if (shipDate != null) {
                                paramJobj.put("shipdate", sdf.format(shipDate));
                            }
                            paramJobj.put("shipvia", shipVia);
                            paramJobj.put("fob", fob);
                            paramJobj.put("isfavourite", "false");
                            paramJobj.put("salesperson", salesPersonID);
                            paramJobj.put("externalcurrencyrate", String.valueOf(exchangeRateForTransaction));
                            paramJobj.put("istemplate", "0");
                            /**
                             * If INDIA/ US GST Applicable Flag Needed 
                             */
                            if (isNewGST) {
                                paramJobj.put("GSTApplicable", true);
                                paramJobj.put("taxamount", "0");
                            } else {
                                paramJobj.put("taxamount", "0");
                            }
                            paramJobj.put("invoicetermsmap", "[]");
                            paramJobj.put("termsincludegst", "false");
                            paramJobj.put("fromLinkCombo", "");
                            paramJobj.put("linkFrom", "");
                            paramJobj.put("linkNumber", "");
                            paramJobj.put("templatename", "");
                            paramJobj.put("customfield", customJArr.toString());
                            paramJobj.put("isEdit", "false");
                            paramJobj.put("copyInv", "false");
                            paramJobj.put("isDraft", "false");
                            paramJobj.put("includeprotax", "false");
                            paramJobj.put("shipLength", "1");
                            
                            if (isIncludeTotalTax) {
                                paramJobj.put("taxid", taxID);
                            } else {
                                paramJobj.put("taxid", "");
                            }
                            
                            paramJobj.put("deletedLinkedDocumentId", "");
                            paramJobj.put("invoicetype", "");
                            paramJobj.put("seqformat_oldflag", "false");
                            paramJobj.put("includeprotax", String.valueOf(isIncludeProductTax));
                            paramJobj.put("islockQuantity", String.valueOf(islockQuantity));
                            
                            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getGlobalDateFormat();
                            paramJobj.put(Constants.Checklocktransactiondate, formatter.format(salesOrderDate));
                            /*
                             Put 'isOpeningBalanceOrder' key to identify the Opening Transaction 
                            */
                            paramJobj.put("isOpeningBalanceOrder",isOpeningDocImport);

                            // for adding due date
                            KwlReturnObject termObj = accountingHandlerDAOobj.getObject(Term.class.getName(), termID);
                            Term term = (Term) termObj.getEntityList().get(0);

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(salesOrderDate);
                            cal.add(Calendar.DAY_OF_MONTH, term.getTermdays());
                            paramJobj.put("duedate", sdf.format(cal.getTime()));
                            Map<String, Object> requestParams = new HashMap<>();
                            requestParams.put(Constants.companyKey, companyID);
                            requestParams.put("isOpeningBalanceOrder", isOpeningDocImport);
                            CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, requestJobj, salesOrderDate);
                            CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, salesOrderDate, false);
                        } // end global details

                        if (!StringUtil.isNullOrEmpty(failureMsg)) {
                            throw new AccountingException(failureMsg);
                        }

                        // For Line level details
                        double conversionFactor = 1;
                        // Add Custom fields details of line items
                        JSONArray lineCustomJArr = createLineLevelCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Sales_Order_ModuleId);
                        if (isImportByScript) {
                            lineCustomJArr = createCustomFieldArrayForScriptImport(requestJobj, lineCustomJArr, customerID, Constants.Acc_Sales_Order_ModuleId);
                        }
                        /*
                         * Getting Custom Fields of product which are marked as sales order custom fields  
                         */
                        JSONArray productcustomfield = createGlobalCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Product_Master_ModuleId);
                        JSONObject detailData = new JSONObject();
                        detailData.put("productid", (product != null) ? product.getID() : "");
                        detailData.put("rate", String.valueOf(authHandler.round(unitPrice, companyID)));
                        if (isIncludingGST) {
                            detailData.put("rateIncludingGst", String.valueOf(authHandler.round(unitPriceIncludingGST, companyID)));
                        }else if(isNewGST){ // For US and INDIA
                            detailData.put("rateIncludingGst", String.valueOf(authHandler.round(unitPrice, companyID)));
                        }
                        detailData.put("priceSource", "");
                        detailData.put("quantity", String.valueOf(quantity));
                        detailData.put("uomid", (uom != null) ? uom.getID() : "");
                        detailData.put("baseuomquantity", String.valueOf(authHandler.round(quantity * conversionFactor, companyID)));
                        detailData.put("baseuomrate", String.valueOf(conversionFactor));
                        detailData.put("dependentType", "");
                        detailData.put("inouttime", "");
                        detailData.put("showquantity", "");
                        if (!StringUtil.isNullOrEmpty(desc)) {
                            detailData.put("desc", desc);
                        } else {
                            detailData.put("desc", (product != null) ? product.getDescription() : "");
                        }
                        
                        detailData.put("invstore", "");
                        detailData.put("invlocation", "");
                        detailData.put("rowid", "");
                        detailData.put("prdiscount", String.valueOf(discount));
                        detailData.put("discountispercent", String.valueOf(discountType));
                        if (!StringUtil.isNullOrEmpty(paramJobj.optString("includeprotax", "")) && paramJobj.getString("includeprotax").equalsIgnoreCase("true") && rowtax != null) {
                            detailData.put("prtaxid", rowtax.getID());
                            detailData.put("taxamount", rowtaxamount);
                        } else {
                            detailData.put("prtaxid", "");
                            detailData.put("taxamount", "0");
                        }
                        detailData.put("linkto", "");
                        detailData.put("savedrowid", "");
                        /**
                         * For INDIA/ US Import Sales order LineTerms Details Send 
                         */
                        if (isNewGST) {
                            detailData.put("recTermAmount",gstTaxAmt);
                            detailData.put("LineTermdetails", gstTermTaxJArr);
                        } else {
                            detailData.put("recTermAmount", "");
                            detailData.put("LineTermdetails", "");
                        }
                        detailData.put("OtherTermNonTaxableAmount", "");
                        detailData.put("productcustomfield", productcustomfield.toString());
                        detailData.put("productMRP", "");
                        detailData.put("valuationType", "");
                        detailData.put("reortingUOMExcise", "");
                        detailData.put("reortingUOMSchemaExcise", "");
                        detailData.put("valuationTypeVAT", "");
                        detailData.put("reportingUOMVAT", "");
                        detailData.put("reportingUOMSchemaVAT", "");
                        detailData.put("customfield", lineCustomJArr.toString());
                        
                        if (!StringUtil.isNullOrEmpty(vendorID) && extrareferences.isActivateProfitMargin()) {
                            detailData.put("vendorid", vendorID);
                            detailData.put("vendorunitcost", vendorUnitCost);
                            detailData.put("vendorcurrexchangerate", vendorCurrencyExchangeRate);
                            
                            double totalCost = authHandler.roundQuantity((vendorUnitCost * vendorCurrencyExchangeRate), companyID);
                            detailData.put("totalcost", totalCost);
                        }
                        
                        double rate = authHandler.round(unitPrice, companyID);
                        if (paramJobj.optString("includingGST", "").equalsIgnoreCase("true")) {
                            rate = authHandler.round(unitPriceIncludingGST, companyID);
                        }
                        double rowAmount = authHandler.round(rate * authHandler.round(quantity, companyID), companyID);
                        rowAmount = authHandler.round(rowAmount, companyID);
                        totalamount += rowAmount;

                        double rowdiscount = discount;
                        if (discountType == 1) { // for percent disc
                            rowdiscount = (rowAmount * discount) / 100;
                        }
                        rowdiscount = authHandler.round(rowdiscount, companyID);
                        totaldiscount += rowdiscount;

                        rows.put(detailData);
                    } catch (Exception ex) {
                        failed++;
                        isRecordFailed = true;
                        String errorMsg = "";
                        if (ex.getMessage() != null) {
                            errorMsg = ex.getMessage();
                        } else if (ex.getCause() != null) {
                            errorMsg = ex.getCause().getMessage();
                        }

                        failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }
                cnt++;
            }

            // save for last record
            if (!isAlreadyExist && !isRecordFailed) {
                double taxamount = 0.0;
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxid", null))) {
                    HashMap<String, Object> taxParams = new HashMap<>();
                    taxParams.put("transactiondate", sdf.parse(paramJobj.getString("billdate")));
                    taxParams.put("taxid", paramJobj.getString("taxid"));
                    taxParams.put("companyid", companyID);
                    KwlReturnObject taxResult = accTaxObj.getTax(taxParams);
                    Object[] taxRow = (Object[]) taxResult.getEntityList().get(0);
                    double taxPercentage = (double) taxRow[1];
                    taxamount = ((totalamount - totaldiscount) * taxPercentage) / 100;
                    taxamount = authHandler.round(taxamount, companyID);
                }
                paramJobj.put("taxamount", String.valueOf(taxamount));
                paramJobj.put(Constants.detail, rows.toString());
                paramJobj.put(Constants.PAGE_URL, requestJobj.optString(Constants.PAGE_URL));
                saveSalesOrderJSON(paramJobj);
            }

            if (failed > 0) {
                importHandler.createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = "Empty file.";
            } else if (success == 0) {
                msg = "Failed to import all the records.";
            } else if (success == total) {
                msg = "All records are imported successfully.";
            } else {
                msg = "Imported " + success + " record" + (success > 1 ? "s" : "") + " successfully";
                msg += (failed == 0 ? "." : " and failed to import " + failed + " record" + (failed > 1 ? "s" : "") + ".");
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            csvReader.close();

            // For saving import log
            saveImportLog(requestJobj, msg, total, failed, Constants.Acc_Sales_Order_ModuleId);

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", ImportLog.getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_Sales_Order_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
    
    @Override
    public Tax getGSTByCode(String accountCode, String companyID){
        Tax tax = null;
        try {
            if (!StringUtil.isNullOrEmpty(accountCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accAccountDAOobj.getTaxFromCode(companyID, accountCode);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    tax = (Tax) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return tax;
    }
    
    public Vendor getVendorByCode(String vendorCode, String companyID) throws AccountingException {
        Vendor vendor = null;
        try {
            if (!StringUtil.isNullOrEmpty(vendorCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accVendorDAOObj.getVendorByCode(vendorCode, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    vendor = (Vendor) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Vendor");
        }
        return vendor;
    }
    
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class})
    public String deleteQuotation (JSONObject jobj, JSONObject requestJobj, String companyid, String linkedQuotaions) throws JSONException, ServiceException, AccountingException {
        if (!StringUtil.isNullOrEmpty(jobj.getString(Constants.billid))) {
            String qid = StringUtil.DecodeText(jobj.optString(Constants.billid));

            KwlReturnObject res = accountingHandlerDAOobj.getObject(Quotation.class.getName(), qid);
            Quotation quotation = (Quotation) res.getEntityList().get(0);
            String qno = quotation.getQuotationNumber();//jobj.getString("billno");
            KwlReturnObject result = accSalesOrderDAOobj.getQTforinvoice(qid, companyid);  //for cheching Customer Quotation used in invoice or not
            int count1 = result.getRecordTotalCount();
            if (count1 > 0) {
//                        throw new AccountingException("Selected record(s) is currently used in the Invoices(s). So it cannot be deleted.");
                linkedQuotaions += qno + ", ";
                return linkedQuotaions;
            }
            KwlReturnObject results = accSalesOrderDAOobj.getSOforQT(qid, companyid);  //for cheching Customer Quotation used in sales order or not
            int count2 = results.getRecordTotalCount();
            if (count2 > 0) {
                //throw new AccountingException("Selected record(s) is currently used in the Customer Invoices(s). So it cannot be deleted.");
                linkedQuotaions += qno + ", ";
                return linkedQuotaions;
            }
            boolean isLeaseFixedAsset = requestJobj.optString("isLeaseFixedAsset") != null ? Boolean.parseBoolean(requestJobj.optString("isLeaseFixedAsset")) : false;
            Map<String, Object> map = new HashMap();
            map.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
            map.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
            map.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
            map.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
            auditTrailObj.insertAuditLog("74", " User " + requestJobj.optString(Constants.userfullname) + " has deleted a " + (isLeaseFixedAsset ? "Lease " : "") + "Customer Quotation " + qno, map, qid);
            accSalesOrderDAOobj.deleteQuotation(qid, companyid);
        }
        return linkedQuotaions;
    }
    
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class})
    public String deleteQuotationPermanent(JSONObject jobj, String companyid, String linkedTransaction, JSONObject requestJobj) throws ServiceException, JSONException, AccountingException {
        String qid = StringUtil.DecodeText(jobj.optString(Constants.billid));

        KwlReturnObject res = accountingHandlerDAOobj.getObject(Quotation.class.getName(), qid);
        Quotation quotation = (Quotation) res.getEntityList().get(0);
        String qno = quotation.getQuotationNumber();//jobj.getString("billno");

        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("qid", qid);
        requestParams.put(Constants.companyKey, companyid);
        requestParams.put("qno", qno);
        requestParams.put("versionid", qid);
        if (!StringUtil.isNullOrEmpty(qid)) {
            KwlReturnObject result = accSalesOrderDAOobj.getQTforinvoice(qid, companyid);  //for cheching Customer Quotation used in invoice or not
            int count2 = result.getRecordTotalCount();
            if (count2 > 0) {
                //throw new AccountingException("Selected record(s) is currently used in the Customer Invoices(s). So it cannot be deleted.");
                linkedTransaction += qno + ", ";
                return linkedTransaction;
            }
            KwlReturnObject results = accSalesOrderDAOobj.getSOforQT(qid, companyid);  //for cheching Customer Quotation used in sales order or not
            int count3 = results.getRecordTotalCount();
            if (count3 > 0) {
                //throw new AccountingException("Selected record(s) is currently used in the Customer Invoices(s). So it cannot be deleted.");
                linkedTransaction += qno + ", ";
                return linkedTransaction;
            }
            boolean isLeaseFixedAsset = requestJobj.optString("isLeaseFixedAsset") != null ? Boolean.parseBoolean(requestJobj.optString("isLeaseFixedAsset")) : false;
            if (!isLeaseFixedAsset) {
                KwlReturnObject result2 = accSalesOrderDAOobj.getVersionQuotations(requestParams);  //for checking Customer Quotation has any Version or not
                int count4 = result2.getRecordTotalCount();
                if (count4 > 0) {
                    throw new AccountingException("Selected quotation(s) is having Version History. So it cannot be deleted.");
                }
            }
            
            boolean isAvalaraIntegration = false;
            ExtraCompanyPreferences extraCompanyPreferences = null;
            result = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                extraCompanyPreferences = (ExtraCompanyPreferences) result.getEntityList().get(0);
                isAvalaraIntegration = extraCompanyPreferences.isAvalaraIntegration();
            }

            if (isAvalaraIntegration) {//When Avalara Integration is enabled, also delete tax details from table 'TransactionDetailAvalaraTaxMapping'
                deleteAvalaraTaxMappingForQuotation(quotation.getRows());
            }
            
            accSalesOrderDAOobj.deleteLinkingInformationOfCQ(requestParams);
            accSalesOrderDAOobj.deleteQuotationsPermanent(requestParams);
            Map<String, Object> map = new HashMap();
            map.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
            map.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
            map.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
            map.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
            auditTrailObj.insertAuditLog("74", " User " + requestJobj.optString(Constants.userfullname) + " has deleted a " + (isLeaseFixedAsset ? "Lease " : "") + "Customer Quotation Permanently " + qno, map, qid);
        }
        return linkedTransaction;
    }
    
    @Override
    public JSONObject deleteSalesOrdersPermanent(JSONObject paramJobj) throws JSONException {
        JSONObject responseJobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isLeaseFixedAsset = false;
        boolean isConsignment = false;
        boolean isVendorJobWorkOrder = false;
        boolean isMultiGroupCompanyFlag = false;
        boolean isCallFromSourceDomainFlag = false;//flag to recognize when po is deleted so will be deleted
        Locale locale = (Locale) paramJobj.get(Constants.locale);
        
        JSONArray jArr = new JSONArray(paramJobj.getString(Constants.RES_data));
        String companyid = paramJobj.getString(Constants.companyKey);
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isMultiGroupCompanyFlag, null))) {
            isMultiGroupCompanyFlag = Boolean.parseBoolean(paramJobj.optString(Constants.isMultiGroupCompanyFlag));
            if (isMultiGroupCompanyFlag) {
                if (paramJobj.has("sourceTransactionId") && !StringUtil.isNullOrEmpty(paramJobj.optString("sourceTransactionId", null))) {
                    isCallFromSourceDomainFlag = true;
                }

                if (!isCallFromSourceDomainFlag) {
                    StringBuilder billidsBuilder = new StringBuilder();
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject jobj = jArr.getJSONObject(i);
                        billidsBuilder.append(jobj.optString(Constants.billid) + ",");
                    }
                    String billids = billidsBuilder.toString().substring(0, billidsBuilder.toString().length() - 1);
                    JSONObject returnJObj = checklinkingofTransactions(paramJobj, billids);
                    isCallFromSourceDomainFlag = returnJObj.optBoolean(Constants.RES_success);
                    if (!isCallFromSourceDomainFlag) {
                        msg = messageSource.getMessage("acc.multiGroupSOCannotDeleteOperation", null, locale);
                    }
                }
            }
        }

        String linkedTransactions = "";
        try {
            //IF MULTIgroupCompany is not activated or if multigroupflag is activated and is call to delete grn
            if ((!isMultiGroupCompanyFlag) || (isCallFromSourceDomainFlag)) {

                if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isLeaseFixedAsset, null))) {
                    isLeaseFixedAsset = Boolean.parseBoolean(paramJobj.getString(Constants.isLeaseFixedAsset));
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isConsignment, null))) {
                    isConsignment = Boolean.parseBoolean(paramJobj.getString(Constants.isConsignment));
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("isVendorJobWorkOrder", null))) {
                    isVendorJobWorkOrder = Boolean.parseBoolean(paramJobj.getString("isVendorJobWorkOrder"));
                }

                
                String modulename = "";
                if (isConsignment) {
                    modulename = " " + messageSource.getMessage("acc.consignment.order", null, locale) + " ";
                } else if (isVendorJobWorkOrder) {
                    modulename = Constants.JOBWORK_IN_ORDER;
                } else {
                    modulename = Constants.SALESORDER;
                }
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    linkedTransactions = deleteSalesOrderPermanent(linkedTransactions, jobj, companyid, isLeaseFixedAsset, paramJobj, modulename);
                }
                issuccess = true;
                if (StringUtil.isNullOrEmpty(linkedTransactions)) {
                    if (isLeaseFixedAsset) {
                        msg = messageSource.getMessage("acc.lso.del", null, locale);
                    } else if (isConsignment) {
                        msg = messageSource.getMessage("acc.consignment.order.del", null, locale);
                    } else if (isVendorJobWorkOrder) {
                        msg = messageSource.getMessage("acc.vendorjob.order.del", null, locale);
                    } else {
                        msg = messageSource.getMessage("acc.so.del", null, locale);   //"Sales Order has been deleted successfully;
                    }
                } else {
                    if (isLeaseFixedAsset) {
                        msg = messageSource.getMessage("acc.field.LSOexcept", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);
                    } else if (isVendorJobWorkOrder) {
                        msg = messageSource.getMessage("acc.field.vendorjob", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);
                    } else if (isConsignment) {
                        msg = messageSource.getMessage("acc.field.consignmentOexcept", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);
                    } else {
                        msg = messageSource.getMessage("acc.field.SalesOrdersexcept", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);   //"Sales Order has been deleted successfully;
                    }
                }
            }
        } catch (JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                responseJobj.put(Constants.RES_success, issuccess);
                responseJobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return responseJobj;
    }
    
    @Override
    //Delete Temporary
    public JSONObject deleteSalesOrdersTemporary(JSONObject paramJobj) throws JSONException {
        JSONObject responseJobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isLeaseFixedAsset = false;
        boolean isConsignment = false;
        Locale locale = (Locale) paramJobj.get(Constants.locale);
        boolean isVendorJobWorkOrder = false;

        boolean isMultiGroupCompanyFlag = false;
        boolean isCallFromSourceDomainFlag = false;//flag to recognize when po is deleted so will be deleted
        String linkedTransactions = "";
        JSONArray jArr = new JSONArray(paramJobj.getString(Constants.RES_data));
        
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isMultiGroupCompanyFlag, null))) {
            isMultiGroupCompanyFlag = Boolean.parseBoolean(paramJobj.optString(Constants.isMultiGroupCompanyFlag));
            if (isMultiGroupCompanyFlag) {
                if (paramJobj.has("sourceTransactionId") && !StringUtil.isNullOrEmpty(paramJobj.optString("sourceTransactionId", null))) {
                    isCallFromSourceDomainFlag = true;
                }

                if (!isCallFromSourceDomainFlag) {
                    StringBuilder billidsBuilder = new StringBuilder();
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject jobj = jArr.getJSONObject(i);
                        billidsBuilder.append(jobj.optString(Constants.billid) + ",");
                    }
                    String billids = billidsBuilder.toString().substring(0, billidsBuilder.toString().length() - 1);
                    JSONObject returnJObj = checklinkingofTransactions(paramJobj, billids);
                    isCallFromSourceDomainFlag = returnJObj.optBoolean(Constants.RES_success);
                    if (!isCallFromSourceDomainFlag) {
                        msg = messageSource.getMessage("acc.multiGroupSOCannotDeleteOperation", null, locale);
                    }
                }
            }
        }

        String companyid = paramJobj.getString(Constants.companyKey);
        try {
            //IF MULTIgroupCompany is not activated or if multigroupflag is activated and is call to delete po
            if ((!isMultiGroupCompanyFlag) || (isCallFromSourceDomainFlag)) {

                if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isLeaseFixedAsset, null))) {
                    isLeaseFixedAsset = Boolean.parseBoolean(paramJobj.getString(Constants.isLeaseFixedAsset));
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isConsignment, null))) {
                    isConsignment = Boolean.parseBoolean(paramJobj.getString(Constants.isConsignment));
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("isVendorJobWorkOrder", null))) {
                    isVendorJobWorkOrder = Boolean.parseBoolean(paramJobj.getString("isVendorJobWorkOrder"));
                }

            
                String modulename = "";
                if (isConsignment) {
                    modulename = " " + messageSource.getMessage("acc.consignment.order", null, locale) + " ";
                } else if (isVendorJobWorkOrder) {
                    modulename = Constants.JOBWORK_IN_ORDER;
                } else {
                    modulename = Constants.SALESORDER;
                }
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    linkedTransactions = deleteSalesOrder(jobj, linkedTransactions, companyid, paramJobj, modulename, isLeaseFixedAsset);
                }
                if (linkedTransactions != "" || linkedTransactions.length() > 0) {
                    issuccess = false;
                } else {
                    issuccess = true;
                }
                if (StringUtil.isNullOrEmpty(linkedTransactions)) {
                    if (isLeaseFixedAsset) {
                        msg = messageSource.getMessage("acc.lso.del", null, locale);
                    } else if (isConsignment) {
                        msg = messageSource.getMessage("acc.consignment.order.del", null, locale);
                    } else if (isVendorJobWorkOrder) {
                        msg = messageSource.getMessage("acc.vendorjob.order.del", null, locale);
                    } else {
                        msg = messageSource.getMessage("acc.so.del", null, locale);   //"Sales Order has been deleted successfully;
                    }
                } else {
                    if (isLeaseFixedAsset) {
                        msg = messageSource.getMessage("acc.field.LSOexcept", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);
                    } else if (isConsignment) {
                        msg = messageSource.getMessage("acc.field.consignmentOexcept", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);
                    } else if (isVendorJobWorkOrder) {
                        msg = messageSource.getMessage("acc.field.vendorjob", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);
                    } else {
                        msg = messageSource.getMessage("acc.field.SalesOrdersexcept", null, locale) + " " + linkedTransactions.substring(0, linkedTransactions.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, locale);   //"Sales Order has been deleted successfully;
                    }
                }
            }
        } catch (JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                responseJobj.put(Constants.RES_success, issuccess);
                responseJobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return responseJobj;
    }
    
    public JSONObject checklinkingofTransactions(JSONObject paramJobj, String billids) throws JSONException {
        JSONObject returnJObj = new JSONObject();
        boolean issuccess=true;
        try {
            KwlReturnObject bankresult = accSalesOrderDAOobj.checklinkingofTransactions(String.valueOf(Constants.Acc_Purchase_Order_ModuleId), billids);
            if (bankresult != null && bankresult.getRecordTotalCount() > 0) {
                issuccess = false;
            }

        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
               returnJObj.put(Constants.RES_success, issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnJObj;
    }
    
    
    /*---Function to save approval history , If any document is approved at some level------   */
    public List saveApprovalHistory(HashMap approvalHistoryMap) throws ServiceException {

        List approvalHistoryList = new ArrayList();

        SalesOrder salesOrderObj = null;
        Company companyObj = null;
        String userid = "";

        if (approvalHistoryMap.containsKey("salesOrder") && approvalHistoryMap.get("salesOrder") != null) {
            salesOrderObj = (SalesOrder) approvalHistoryMap.get("salesOrder");
        }

        if (approvalHistoryMap.containsKey("company") && approvalHistoryMap.get("company") != null) {
            companyObj = (Company) approvalHistoryMap.get("company");
        }

        if (approvalHistoryMap.containsKey("userid") && approvalHistoryMap.get("userid") != null) {
            userid = (String) approvalHistoryMap.get("userid");
        }

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("transtype", Constants.SALES_ORDER_APPROVAL);
        hashMap.put("transid", salesOrderObj.getID());
        hashMap.put("approvallevel", salesOrderObj.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
        hashMap.put("remark", "");//I think , it should be blank in edit mode
        hashMap.put("userid", userid);
        hashMap.put("companyid", companyObj.getCompanyID());
        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
        KwlReturnObject kmsg = null;
        String roleName = "Company User";
        kmsg = permissionHandlerDAOObj.getRoleofUser(userid);
        Iterator ite2 = kmsg.getEntityList().iterator();
        while (ite2.hasNext()) {
            Object[] row = (Object[]) ite2.next();
            roleName = row[1].toString();
        }

        approvalHistoryList.add(roleName);

        return approvalHistoryList;

    }
    
            
    /*-------Function to send approval mail if check "Allow Sending Approval Mail" is activated from system preferences---------*/
    public void sendApprovalMailIfAllowedFromSystemPreferences(HashMap emailMap) throws ServiceException {

        String userName = "";
        Company company = null;
        SalesOrder salesOrderObj = null;
        String baseUrl = "";
        CompanyAccountPreferences preferences = null;
        HashMap<String, Object> ApproveMap =null;

        if (emailMap.containsKey("userName") && emailMap.get("userName") != null) {
            userName = (String) emailMap.get("userName");
        }
        if (emailMap.containsKey("company") && emailMap.get("company") != null) {
            company = (Company) emailMap.get("company");
        }

        if (emailMap.containsKey("salesOrder") && emailMap.get("salesOrder") != null) {
            salesOrderObj = (SalesOrder) emailMap.get("salesOrder");
        }

        if (emailMap.containsKey("baseUrl") && emailMap.get("baseUrl") != null) {
            baseUrl = (String) emailMap.get("baseUrl");
        }

        if (emailMap.containsKey("preferences") && emailMap.get("preferences") != null) {
            preferences = (CompanyAccountPreferences) emailMap.get("preferences");
        }
        if (emailMap.containsKey("ApproveMap") && emailMap.get("ApproveMap") != null) {
            ApproveMap = (HashMap<String, Object>) emailMap.get("ApproveMap");
        }
        int level = salesOrderObj.getApprovestatuslevel();
        String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        String creatormail = company.getCreator().getEmailID();
        String documentcreatoremail = (salesOrderObj != null && salesOrderObj.getCreatedby() != null) ? salesOrderObj.getCreatedby().getEmailID() : "";
        String fname = company.getCreator().getFirstName() == null ? "" : company.getCreator().getFirstName();
        String lname = company.getCreator().getLastName() == null ? "" : company.getCreator().getLastName();
        String creatorname = fname + " " + lname;
        String approvalpendingStatusmsg = "";
        HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
        ArrayList<String> emailArray = new ArrayList<>();
        qdDataMap.put(Constants.companyKey, company.getCompanyID());
        qdDataMap.put("level", level);
        qdDataMap.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
//        emailArray = commonFnControllerService.getUserApprovalEmail(qdDataMap);
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
        if (salesOrderObj.getApprovestatuslevel() < 11) {
                qdDataMap.put("ApproveMap", ApproveMap);
                approvalpendingStatusmsg=commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
        Map<String, Object> mailParameters = new HashMap();
            mailParameters.put("Number", salesOrderObj.getSalesOrderNumber());
            mailParameters.put("userName", userName);
            mailParameters.put("emails", emails);
            mailParameters.put("sendorInfo", sendorInfo);
            mailParameters.put("moduleName", Constants.SALESORDER);
            mailParameters.put("addresseeName", "All");
            mailParameters.put("companyid", company.getCompanyID());
            mailParameters.put("baseUrl", baseUrl);
            mailParameters.put("approvalstatuslevel", salesOrderObj.getApprovestatuslevel());
            mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
        if (emails.length > 0) {

            accountingHandlerDAOobj.sendApprovedEmails(mailParameters);

        }
    }
    
    /*---------Send approval mail for Customer Quotation, if allowed from System Preferences--------------*/
    public void sendApprovalMailForCQIfAllowedFromSystemPreferences(HashMap emailMap) throws ServiceException {

        String userName = "";
        Company company = null;
        Quotation customerQuotationObj = null;
        String baseUrl = "";
        CompanyAccountPreferences preferences = null;
        HashMap<String, Object> ApproveMap =new HashMap();

        if (emailMap.containsKey("userName") && emailMap.get("userName") != null) {
            userName = (String) emailMap.get("userName");
        }
        if (emailMap.containsKey("company") && emailMap.get("company") != null) {
            company = (Company) emailMap.get("company");
        }

        if (emailMap.containsKey("customerQuotation") && emailMap.get("customerQuotation") != null) {
            customerQuotationObj = (Quotation) emailMap.get("customerQuotation");
        }

        if (emailMap.containsKey("baseUrl") && emailMap.get("baseUrl") != null) {
            baseUrl = (String) emailMap.get("baseUrl");
        }

        if (emailMap.containsKey("preferences") && emailMap.get("preferences") != null) {
            preferences = (CompanyAccountPreferences) emailMap.get("preferences");
        }
        if (emailMap.containsKey("ApproveMap") && emailMap.get("ApproveMap") != null) {
                ApproveMap = (HashMap<String, Object>) emailMap.get("ApproveMap");
        }

        String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        String creatormail = company.getCreator().getEmailID();
        String documentcreatoremail = (customerQuotationObj != null && customerQuotationObj.getCreatedby() != null) ? customerQuotationObj.getCreatedby().getEmailID() : "";
        String fname = company.getCreator().getFirstName() == null ? "" : company.getCreator().getFirstName();
        String lname = company.getCreator().getLastName() == null ? "" : company.getCreator().getLastName();
        String creatorname = fname + " " + lname;
        String approvalpendingStatusmsg = "";
        HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
        ArrayList<String> emailArray = new ArrayList<>();
        qdDataMap.put(Constants.companyKey, company.getCompanyID());
        qdDataMap.put("level", customerQuotationObj.getApprovestatuslevel());
        qdDataMap.put(Constants.moduleid, Constants.Acc_Customer_Quotation_ModuleId);
//        emailArray = commonFnControllerService.getUserApprovalEmail(qdDataMap);
        emailArray.add(creatormail);
        String[] emails = {};
        emails = emailArray.toArray(emails);
        if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
            String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
            emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
        }
        if (!StringUtil.isNullOrEmpty(documentcreatoremail) && !creatormail.equalsIgnoreCase(documentcreatoremail)) {
            emailArray.add(documentcreatoremail);
        }
        qdDataMap.put("ApproveMap", ApproveMap);
        if (customerQuotationObj.getApprovestatuslevel() < 11) {
                approvalpendingStatusmsg=commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
        Map<String, Object> mailParameters = new HashMap();
        mailParameters.put("Number", customerQuotationObj.getQuotationNumber());
        mailParameters.put("userName", userName);
        mailParameters.put("emails", emails);
        mailParameters.put("sendorInfo", sendorInfo);
        mailParameters.put("moduleName", Constants.CUSTOMER_QUOTATION);
        mailParameters.put("addresseeName", "All");
        mailParameters.put("companyid", company.getCompanyID());
        mailParameters.put("baseUrl", baseUrl);
        mailParameters.put("approvalstatuslevel", customerQuotationObj.getApprovestatuslevel());
        mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
        if (emails.length > 0) {
            accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
        }

    }
    
    
    
    
    /*---Function to save approval history , If any document is approved at some level------   */
    public List saveApprovalHistoryForCQ(HashMap approvalHistoryMap) throws ServiceException {

        List approvalHistoryList = new ArrayList();

        Quotation customerQuotationObj = null;
        Company companyObj = null;
        String userid = "";

        if (approvalHistoryMap.containsKey("customerQuotation") && approvalHistoryMap.get("customerQuotation") != null) {
            customerQuotationObj = (Quotation) approvalHistoryMap.get("customerQuotation");
        }

        if (approvalHistoryMap.containsKey("company") && approvalHistoryMap.get("company") != null) {
            companyObj = (Company) approvalHistoryMap.get("company");
        }

        if (approvalHistoryMap.containsKey("userid") && approvalHistoryMap.get("userid") != null) {
            userid = (String) approvalHistoryMap.get("userid");
        }

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("transtype", Constants.CUSTOMER_QUOTATION_APPROVAL);
        hashMap.put("transid", customerQuotationObj.getID());
        hashMap.put("approvallevel", customerQuotationObj.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
        hashMap.put("remark", "");// It should be blank in edit mode
        hashMap.put("userid", userid);
        hashMap.put("companyid", companyObj.getCompanyID());
        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
        KwlReturnObject kmsg = null;
        String roleName = "Company User";
        kmsg = permissionHandlerDAOObj.getRoleofUser(userid);
        Iterator ite2 = kmsg.getEntityList().iterator();
        while (ite2.hasNext()) {
            Object[] row = (Object[]) ite2.next();
            roleName = row[1].toString();
        }

        approvalHistoryList.add(roleName);

        return approvalHistoryList;

    }

    /**
     * Below method is executed only when Avalara Integration is enabled
     * and saves the tax details in database table TransactionDetailAvalaraTaxMapping
     * @param paramsJobj
     * @param repeatedSO
     * @param salesOrderNumber
     * @param companyid
     * @param msg
     * @return
     * @throws JSONException 
     */
    @Override
    public JSONObject saveTaxToAvalara(JSONObject paramsJobj, SalesOrder repeatedSO, String salesOrderNumber, String companyid, String msg) throws JSONException {
        JSONObject returnJobj = new JSONObject();
        boolean success = false;
        try {
            JSONObject avalaraSaveTaxJobj = createJsonFromSalesOrderObject(paramsJobj, repeatedSO, repeatedSO.getOrderDate(), companyid);
            /**
             * When Avalara Integration is On then we do not commit 
             * here in Sales Order.
             * Here method name is commitAndSaveTax 
             * But it only save Tax in database as isCommit flag is false by default.
             */
            JSONObject taxSaveResponseJobj = integrationCommonService.commitAndSaveTax(avalaraSaveTaxJobj);
            msg += "<br><br><b>NOTE:</b> " + taxSaveResponseJobj.optString(Constants.RES_msg);
            success = true;

        } catch (AccountingException ex) {
            msg += "<br><br><b>NOTE:</b> " + (StringUtil.isNullOrEmpty(ex.getMessage()) ? messageSource.getMessage("acc.integration.taxSaveFailureMsg", null, Locale.forLanguageTag(paramsJobj.getString(Constants.language))) : ex.getMessage());
            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg += "<br><br><b>NOTE:</b> " + messageSource.getMessage("acc.integration.taxSaveFailureMsg", null, Locale.forLanguageTag(paramsJobj.getString(Constants.language)));
            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnJobj.put(Constants.RES_msg, msg);
            returnJobj.put(Constants.RES_success, success);
        }
        return returnJobj;
    }

}
