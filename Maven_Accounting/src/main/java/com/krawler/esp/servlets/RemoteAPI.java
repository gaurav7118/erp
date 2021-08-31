/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.esp.servlets;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.esp.handlers.DashboardHandler;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.web.resource.Links;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.invoice.service.AccInvoiceModuleService;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.hql.accounting.masteritems.service.AccMasterItemsService;
import com.krawler.spring.accounting.account.accAccountControllerCMN;
import com.krawler.spring.accounting.account.accAccountControllerCMNService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.accountservice.AccAccountService;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.CurrencyContants;
import com.krawler.spring.accounting.currency.accCurrencyController;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerControllerCMNService;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.*;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceHandler;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.payment.accPaymentImpl;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.receipt.AccReceiptServiceDAO;
import com.krawler.spring.accounting.salesorder.AccSalesOrderServiceDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.tax.accTaxController;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.tax.service.AccTaxService;
import com.krawler.spring.accounting.term.accTermDAO;
import com.krawler.spring.accounting.term.service.AccTermService;
import com.krawler.spring.accounting.uom.accUomDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerController;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.permissionHandler.permissionHandler;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.profileHandler.profileHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author krawler
 */
public class RemoteAPI extends MultiActionController implements MessageSourceAware, CurrencyContants {

    private MessageSource messageSource;
    
    private accPaymentImpl accPaymentDAOobj;
    
    private AccCommonTablesDAO accCommonTablesDAO;
    
    private profileHandlerDAO profileHandlerDAOObj;
    
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    
    private AccountingHandlerDAO accountingHandlerDAOobj;
    
    private permissionHandlerDAO permissionHandlerDAOObj;
    
    private accCustomerDAO accCustomerDAOobj;
    
    private accVendorDAO accVendorDAOobj;
    
    private accProductDAO accProductObj;
    
    private accTermDAO accTermObj;
    
    private accUomDAO accUomObj;
    
    private accAccountDAO accAccountDAOobj;
    
    private accMasterItemsDAO accMasterItemsDAOobj;
    
    private accTaxDAO accTaxObj;
    
    private accCurrencyDAO accCurrencyDAOobj;
    
    private accJournalEntryDAO accJournalEntryobj;
    
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    
    private accAccountControllerCMNService accAccountControllerCMNServiceObj;
    
    private accCustomerControllerCMNService accCustomerControllerCMNServiceObj;
    
//    private accVendorDAO accVendorDAOObj;
    
    private accSalesOrderDAO accSalesOrderDAOobj;
    
    private accInvoiceDAO accInvoiceDAOobj;
    
    private fieldManagerDAO fieldManagerDAOobj;
    
    private sessionHandlerImpl sessionHandlerImplObj;
    
    private authHandlerController authHandlerControllerObj;
    
//    private kwlCommonTablesDAO KwlCommonTablesDAOObj;
    
    private AccAccountService accAccountService;
    
    private AccMasterItemsService accMasterItemsService;
    
    private AccTaxService accTaxService;
    
    private AccTermService accTermService;
    
    private AccInvoiceModuleService accInvoiceModuleService;
    
    private AccReceiptServiceDAO accReceiptServiceDAO;
    
    private accCurrencyDAO accCurrencyobj;
    
    private AccInvoiceServiceDAO accInvoiceServiceDAO;
    
    private AccJournalEntryModuleService accJournalEntryModuleService;
    
    private HibernateTransactionManager txnManager;
    
    private auditTrailDAO auditTrailObj;
    
    private APICallHandlerService apiCallHandlerService;
    
    private accAccountControllerCMN accAccountCMNObj;
    
    private AccSalesOrderServiceDAO accSalesOrderServiceDAOobj;
    
    private accGoodsReceiptDAO accGoodsReceiptobj;
    
    private AccProductModuleService accProductModuleService;
    
    private fieldDataManager fieldDataManagercntrl;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setAccPaymentDAOobj(accPaymentImpl accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setProfileHandlerDAOObj(profileHandlerDAO profileHandlerDAOObj) {
        this.profileHandlerDAOObj = profileHandlerDAOObj;
    }

    public void setAccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setPermissionHandlerDAOObj(permissionHandlerDAO permissionHandlerDAOObj) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj;
    }

    public void setAccCustomerDAOobj(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public void setAccVendorDAOobj(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }

    public void setAccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setAccTermObj(accTermDAO accTermObj) {
        this.accTermObj = accTermObj;
    }

    public void setAccUomObj(accUomDAO accUomObj) {
        this.accUomObj = accUomObj;
    }

    public void setAccAccountDAOobj(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setAccMasterItemsDAOobj(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setAccTaxObj(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setAccCurrencyDAOobj(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setAccJournalEntryobj(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setKwlCommonTablesDAOObj(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }

    public void setAccAccountControllerCMNServiceObj(accAccountControllerCMNService accAccountControllerCMNServiceObj) {
        this.accAccountControllerCMNServiceObj = accAccountControllerCMNServiceObj;
    }

    public void setAccCustomerControllerCMNServiceObj(accCustomerControllerCMNService accCustomerControllerCMNServiceObj) {
        this.accCustomerControllerCMNServiceObj = accCustomerControllerCMNServiceObj;
    }

    public void setAccSalesOrderDAOobj(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }

    public void setAccInvoiceDAOobj(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setFieldManagerDAOobj(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }

    public void setSessionHandlerImplObj(sessionHandlerImpl sessionHandlerImplObj) {
        this.sessionHandlerImplObj = sessionHandlerImplObj;
    }

    public void setAuthHandlerControllerObj(authHandlerController authHandlerControllerObj) {
        this.authHandlerControllerObj = authHandlerControllerObj;
    }

    public void setAccAccountService(AccAccountService accAccountService) {
        this.accAccountService = accAccountService;
    }

    public void setAccMasterItemsService(AccMasterItemsService accMasterItemsService) {
        this.accMasterItemsService = accMasterItemsService;
    }

    public void setAccTaxService(AccTaxService accTaxService) {
        this.accTaxService = accTaxService;
    }

    public void setAccTermService(AccTermService accTermService) {
        this.accTermService = accTermService;
    }

    public void setAccInvoiceModuleService(AccInvoiceModuleService accInvoiceModuleService) {
        this.accInvoiceModuleService = accInvoiceModuleService;
    }

    public void setAccReceiptServiceDAO(AccReceiptServiceDAO accReceiptServiceDAO) {
        this.accReceiptServiceDAO = accReceiptServiceDAO;
    }

    public void setAccCurrencyobj(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyobj = accCurrencyobj;
    }

    public void setAccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }

    public void setAccJournalEntryModuleService(AccJournalEntryModuleService accJournalEntryModuleService) {
        this.accJournalEntryModuleService = accJournalEntryModuleService;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }

    public void setAccAccountCMNObj(accAccountControllerCMN accAccountCMNObj) {
        this.accAccountCMNObj = accAccountCMNObj;
    }

    public void setAccSalesOrderServiceDAOobj(AccSalesOrderServiceDAO accSalesOrderServiceDAOobj) {
        this.accSalesOrderServiceDAOobj = accSalesOrderServiceDAOobj;
    }

    public void setAccGoodsReceiptobj(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }
    public ModelAndView remoteapi(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView model;
        response.setContentType("text/html;charset=UTF-8");
//        Session session = null;
        String result = "";
        String companyid = "";
        boolean isCommit = false;
        boolean isTestMode = false;
        int action = 0;
        String validkey = storageHandlerImpl.GetRemoteAPIKey();
        String remoteapikey = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
//        status = txnManager.getTransaction(def);
        boolean newAPIflag= false;
        action = Integer.parseInt(request.getParameter("action"));
        if(action == 706||action == 707||action == 708){
            newAPIflag = true;
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("data")) || newAPIflag) {

            try {
                JSONObject jobj = newAPIflag ? new JSONObject():new JSONObject(request.getParameter("data"));
                companyid = jobj.has("companyid") ? (jobj.isNull("companyid") ? "" : jobj.getString("companyid")) : "";
                isCommit = (jobj.has("iscommit") && jobj.getBoolean("iscommit"));
                isTestMode = (jobj.has("test") && jobj.getBoolean("test"));
                if (jobj.has("remoteapikey")) {
                    remoteapikey = jobj.getString("remoteapikey");
                }
                int mode = 0;
                if (!StringUtil.isNullOrEmpty(request.getParameter("actionmode"))) {
                    mode = Integer.parseInt(request.getParameter("actionmode"));
                }
//                session = HibernateUtil.getCurrentSession();
//                session.beginTransaction();
//                action = Integer.parseInt(request.getParameter("action"));
                switch (action) {
                    case 0://check for companyid
                        result = CompanyidExits(request);
                        break;
                    case 1://Check for userid or username
                        result = UserExits(request);
                        break;
                    case 2://create user
                        result = isCompanyActivated(jobj) ? createUser(request) : getMessage(2, 99);
                        break;
                    case 3://create company
                        result = createCompany(request);
                        break;
                    case 4://delete user
                        result = isCompanyActivated(jobj) ? UserDelete(request) : getMessage(2, 99);
                        break;
                    case 5://assign Role
                        result = isCompanyActivated(jobj) ? assignRole(request) : getMessage(2, 99);
                        break;
                    case 6://Activate user
                        result = isCompanyActivated(jobj) ? ActivateDeactivateUser(request, action) : getMessage(2, 99);
                        break;
                    case 7://DeActivate user
                        result = isCompanyActivated(jobj) ? ActivateDeactivateUser(request, action) : getMessage(2, 99);
                        break;
                    case 8:
                        result = isCompanyActivated(jobj) ? updateCompany(request) : getMessage(2, 99);
                        break;
                    case 9:
                        result = isCompanyActivated(jobj) ? getUpdates(request) : getMessage(2, 99);
                        break;
                    case 10:
                        result = isCompanyActivated(jobj) ? editUser(request) : getMessage(2, 99);
                        break;
                    case 11:
                        result = isCompanyActivated(jobj) ? getAccProduct(request) : getMessage(2, 99);
                        break;
                    case 15:
                        result = deleteCompany(jobj);
                        break;
                    case 16:
                        result = deactivateCompany(request);
                        break;
                    case 301:
                        result = getYearLock(request);
                        break;
                    case 302:
                        result = hrmsSalaryJE(request);
                        break;
                    case 303:
                        result = reverseHrmsSalaryJE(request);
                        break;
                    case 197:
                        result = saveCustomerFromLMS(request);
                        break;
                    case 198:
                        result = getCustomerSequenceFormatToOtherAPPs(request);
                        ;
                        break;
                    case 199:
                        result = saveCustomerFromCRM(request);
                        ;
                        break;
                    case 200:
                        result = getCustomersTOCRM(request);
                        break;
                    case 201:
                        result = isCustomerAccountExists(request);
                        break;
                    case 202:
                        result = saveCustomer(request);
                        break;
                    case 203:
//                        result = saveQuotation(request);  Code is commented as this function is not used in CRM.
                        break;
                    case 204:
                        result = getCustomersWithPart(request);
                        break;
                    case 205:
//                        result = saveQuotationWhenOpportunityStageChanged(session, request); Code is commented as this function is not used in CRM.
                        break;
                    case 207:
                        result = getInvoiceDetailfromCrmQuotation(request);
                        break;
                    case 208:    // Request received from CRM - to get Contract  Details
                        result = getContractDetails(request);
                        break;
                    case 209:    // Request received from CRM - to get Contract Other Details
                        result = getContractOtherDetails(request);
                        break;
                    case 210:    // Request received from CRM - to get Contract Normal Invoice Details
                        result = getContractNormalInvoiceDetails(request);
                        break;
                    case 211:    // Request received from CRM - to get Contract Normal DO Item Details
                        result = getContractNormalDOItemDetails(request);
                        break;
                    case 212:    // Request received from CRM - to get Account Contract Agreement Details
                        result = getCustomerContractsAgreementDetails(request);
                        break;
                    case 213:    // Request received from CRM - to get Account Contract Cost Agreement Details
                        result = getCustomerContractsCostAgreementDetails(request);
                        break;
                    case 214:    // Request received from CRM - to get Account Contract Service Agreement Details
                        result = getCustomerContractsServiceAgreementDetails(request);
                        break;
                    case 215:    // Request received from CRM - to get Account Contract Details
                        result = getAccountContractDetails(request);
                        break;
                    case 216:    // Request received from CRM - to get Contract Replacement Invoice Details
                        result = getContractReplacementInvoiceDetails(request);
                        break;
                    case 217:    // Request received from CRM - to get Contract Maintenance Invoice Details
                        result = getContractMaintenanceInvoiceDetails(request);
                        break;
                    case 218:    // Request received from CRM - to get Contract Normal DO Item Details Row on expand function call
                        result = getContractNormalDOItemDetailsRow(request);
                        break;
                    case 219:    // Request received from CRM - to get Contract Replacement DO item Details
                        result = getContractReplacementDOItemDetails(request);
                        break;
                    case 220:    // Request received from CRM - to get Contract Replacement DO Item Details Row on expand function call
                        result = getContractReplacementDOItemDetailsRow(request);
                        break;
                    case 221:    // Request to delete Quotation from accounting 
                        result = deleteQuotationRequest(request);
                        break;
                    case 304:
                        result = getAccountListForPM(request);
                        break;
                    case 305:
                        result = pmAmountJE(request);
                        break;
                    case 306:     //Company Activated                        
                        result = isActivatedCompany(jobj);
                        break;
                    case 307:
//                        result = saveProduct(request);  This action is commented as we are integrated Inventory and ERP into single Application.
                        break;
                    case 308:
                        result = getUserList(request);
                        break;
                    case 309:
//                        result = saveRoundingDifference(session, request);  Code is mark as commented because this function is not used in Any project.
                        break;
                    case 310:
                        result = saveProductFromLMS(request);
                        break;

                    case 401:
                        result = getCustomerInvoicesReport(request);
                        break;
                    case 402:
                        result = getVendorInvoicesReport(request);
                        break;
                    case 403:
                        result = getCashAndPurchaseRevenue(request);
                        break;
                    case 404:    // Request received from Project Management - to get invoices linked to Payment Milestone tasks. Here in ERP payment milestone is treated as Dimension
                        result = getCashRevenueTaskPM(request);
                        break;
                    case 408:
                        result = saveTax(request, response);
                        break;
                    case 409:
                        result = getTaxRequest(request, response);
                        break;
                    case 501:
                        result = saveProjectDetails(request);
                        break;
                    case 502:
                        result = savePaymentMileStoneDetails(request);
                        break;
                    case 503:
                        result = deleteProjectDetails(request);
                        break;
                    case 405:    // This request used for send the product list for perticular contract
                        result = getContractProductList(request);
                        break;
                    case 406:   // This is used for save and edit product replacement request from CRM 
                        result = saveProductReplacementRequest(request);
                        break;
                    case 407:   // This is used for save and edit product maintenance request from CRM 
                        result = saveProductMaintenanceRequest(request);
                        break;
                    case 410:
//                        result = updateContractServiceDetail(session, request);   Code is mark as commented because this function is not used in Any project.
                        break;
                    case 411:   // This is used for delete product maintenance request from CRM 
                        result = deleteProductMaintenanceRequest(request);
                        break;
                    case 412:   // This is used for delete product replacement request from CRM 
                        result = deleteProductReplacementRequest(request);
                        break;
                    case 601: // This is used to get Currency Exchange Rate to E-Claim
                        result = getCurrencyExchange(jobj);
                        break;
                    case 602: // This is used to update crm account id in db if accounts are synced from CRM
                        result = updateCRMAccountIDForCustomer(request);
                        break;
                    case 701:   // This is used for asset details request from CRM 
                        result = getAssetDetails(request);
                        break;
                    case 702:   // This is used for asset information for assetids request from CRM 
                        result = getAssetInformation(request);
                        break;
                    case 705:   // This is used for asset information payment method
                        result = getPaymentmethod(request);
                        break;

                    case 706:
                        switch (mode) {
                            case 0://verify Login
                                JSONObject jobj1 = new JSONObject();
                                jobj1 = verifyLogin(request, response);
                                if (jobj1.has("success") && (jobj1.get("success").equals(true))) {
                                    String userId = jobj1.getString("lid");
                                    request.setAttribute("userId", userId);
                                    String permValues = getUserPermissions(request, response);
                                    JSONObject permJson = new JSONObject(permValues);
                                    jobj1.remove("perms");
                                    jobj1.put("perms", permJson);
                                    result = jobj1.toString();
                                }
                                result = jobj1.toString();
                                break;
                        }
                        break;
                    case 707:  //dashboard request
                        setUserSessionOnServiceCall(request, response);
                        switch (mode) {

                            case 0://getUserPermissions
                                String permValues = getUserPermissions(request, response);
                                JSONObject permJson = new JSONObject(permValues);
                                result = (new JSONObject().put("perms", permJson)).toString();
                                break;

                            case 1://get Sales order
                                model = getSalesOrdersMerged(request, response);
                                result = model.getModel().get("model").toString();
                                break;

                            case 2://get Customer Invoices
                                model = getInvoicesMerged(request, response);
                                result = model.getModel().get("model").toString();
                                break;
                        }
                        break;
                    case 708:  //saving
                        setUserSessionOnServiceCall(request, response);
                        switch (mode) {

                            case 1://SaveInvoice
                                model = saveCustomerInvoice(request, response);
                                result = model.getModel().get("model").toString();
                                break;
                            case 2://Savereceipt
                                model = saveReceipt(request, response);
                                result = model.getModel().get("model").toString();
                                break;
                            case 3://delete Invoice
                                model = deleteInvoice(request, response);
                                result = model.getModel().get("model").toString();
                                break;
                            case 4://create JE for weekly revenue
                                model = saveJournalEntry(request, response);
                                result = model.getModel().get("model").toString();
                                break;
                            case 5://save multiple invoices from LMS
                                model = saveInvoiceFromLMS(request, response);
                                result = model.getModel().get("model").toString();
                                break;
                            case 6://  Send Accounts to Remote Applications
                                model = getAccounts(request, response);
                                result = model.getModel().get("model").toString();
                                break;
                            case 7://create JE for RemoteApplications
                                model = saveJournalEntryRemoteApplication(request, response);
                                result = model.getModel().get("model").toString();
                                break;
                            case 8:// get company currencyid
                                model = sendInvoiceTermsToCRM(request, response);
                                result = model.getModel().get("model").toString();
                                result = URLEncoder.encode(result, StaticValues.ENCODING);
                                break;
                            case 9://send cost center's to eclaim
                                model = getCostCenter(request, response);
                                result = model.getModel().get("model").toString();
                                break;
//                        case 10://add/edit cost center's received from eclaim
//                            model = addOrEditCostCenter(request, response);
//                            result = model.getModel().get("model").toString();
//                            break;
                        case 11://delete cost center's received from eclaim
                            model = deleteCostCenter(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                            case 12://Add/Edit Payment Terms
                                model = saveTermFromCRM(request, response);
                                result = model.getModel().get("model").toString();
                                break;
                            case 13://Send Products Default Columns List to CRM
                                model = sendDefaultColumnsOfProduct(request, response);
                                result = model.getModel().get("model").toString();
                                break;
                        }
                        break;

                }
                if (isCommit && validkey.equals(remoteapikey)) {
                    status = txnManager.getTransaction(def);
                    txnManager.commit(status);
                }
                if (isTestMode) {
                    result = result.substring(0, (result.length() - 1));
                    result += ",\"action\": " + Integer.toString(action) + "}";
                    //                    result = "{success: true, action:" + Integer.toString(action) + ",data:" + getMessage(2, 2) + "}";
                }
            } catch (JSONException e) {
                result = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
                if (isCommit) {
                    result += ",\"action\": " + Integer.toString(action) + "}";
                    //                    result = "{success: false, action:" + Integer.toString(action) + ",data:" + getMessage(2, 2) + "}";
                }
                if (status != null) {
                    status = txnManager.getTransaction(def);
                    txnManager.rollback(status);
                }
            } catch (ServiceException e) {
                result = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
                if (isCommit) {
                    result += ",\"action\": " + Integer.toString(action) + "}";
                    //                    result = "{success: false, action:" + Integer.toString(action) + ",data:" + getMessage(2, 2) + "}";
                }
                status = txnManager.getTransaction(def);
                txnManager.rollback(status);
            } catch (Exception e) {
                result = getMessage(2, 2);
                if (status != null) {
                    status = txnManager.getTransaction(def);
                    txnManager.rollback(status);
                }
            }
        } else {
        }


        return new ModelAndView("jsonView_ex", "model", result);
    }

    public void setUserSessionOnServiceCall(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {

        String transactionDateinLong = request.getParameter("transactiondate");
        String billdateinlong = request.getParameter("billdate");
        String duedateinlong = request.getParameter("duedate");
        String shipdateinlong = request.getParameter("shipdate");
        DateFormat datef = authHandler.getDateOnlyFormat();
        //transactiondate
        if (!StringUtil.isNullOrEmpty(transactionDateinLong)) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.valueOf(transactionDateinLong).longValue());
            String Trdate = datef.format(cal.getTime());
            try {
                Date TransactionDate = datef.parse(Trdate);
                request.setAttribute(TRANSACTIONDATE, TransactionDate);
            } catch (ParseException ex) {
                request.setAttribute(TRANSACTIONDATE, cal.getTime());
            }
        }
        //Billdate
        if (!StringUtil.isNullOrEmpty(billdateinlong)) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.valueOf(billdateinlong).longValue());
            String bdate = datef.format(cal.getTime());
            try {
                Date billDate = datef.parse(bdate);
                request.setAttribute("billdate", billDate);
            } catch (ParseException ex) {
                request.setAttribute("billdate", cal.getTime());
            }
        }
        //Duedactate
        if (!StringUtil.isNullOrEmpty(duedateinlong)) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.valueOf(duedateinlong).longValue());
            String ddate = datef.format(cal.getTime());
            try {
                Date dueDate = datef.parse(ddate);
                request.setAttribute("duedate", dueDate);
            } catch (ParseException ex) {
                request.setAttribute("duedate", cal.getTime());
            }
        }
        //shipdate
        if (!StringUtil.isNullOrEmpty(shipdateinlong)) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.valueOf(shipdateinlong).longValue());
            String sdate = datef.format(cal.getTime());
            try {
                Date shipDate = datef.parse(sdate);
                request.setAttribute("shipdate", shipDate);
            } catch (ParseException ex) {
                request.setAttribute("shipdate", cal.getTime());
            }
        }

        try {
            request.getParameter("cdomain");
            if (StringUtil.isNullOrEmpty(sessionHandlerImpl.getUserid(request))) {
                setUserSession(request, response);
            } else if (!StringUtil.equal(sessionHandlerImpl.getUserid(request), request.getParameter("userid"))) {
                sessionHandlerImplObj.destroyUserSession(request, response);
                setUserSession(request, response);
            }
        } catch (SessionExpiredException ex) {
            logger.warn("Exception in RemoteAPI:setUserSessionOnServiceCall() - Session has not set. Need to create new session.");
            setUserSession(request, response);
        }
    }

    private String assignRole(HttpServletRequest request) throws SQLException, ServiceException {
        String result = "";
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String userid = jobj.getString("userid");
            String roleStr = jobj.getString("role");
            String companyid = jobj.isNull("companyid") ? "" : jobj.getString("companyid");
            KwlReturnObject kmsg = profileHandlerDAOObj.getUserWithUserName(userid, false);
            int count = kmsg.getRecordTotalCount();
            if (count > 0) {
                String roleid = roleStr.equals("a0") ? Role.COMPANY_ADMIN : (roleStr.equals("a1") ? Role.COMPANY_ADMIN : Role.COMPANY_USER);
                permissionHandlerDAOObj.assignRoles(userid, roleid);
                if (!roleid.equals(Role.COMPANY_ADMIN)) {
                HashMap<String, Object> requestParams = new HashMap<>();
                requestParams.put("userid", userid);
                requestParams.put("roleid", roleid);
                requestParams.put("companyid", companyid);
                KwlReturnObject krObj = permissionHandlerDAOObj.getUserPermission(requestParams);
                 int i = 0;
                List<Object[]> rows = krObj.getEntityList();
                String[] features = new String[krObj.getRecordTotalCount()];
                String[] permissions = new String[krObj.getRecordTotalCount()];
                for (Object[] row : rows) {
                    features[i] = row[2].toString();
                    permissions[i] = row[1].toString();
                    i++;
                }
                requestParams.put("userid", userid);
                requestParams.put("roleid", roleid);

            permissionHandlerDAOObj.setPermissions(requestParams, features, permissions);
                }
                result = getMessage(1, 8);
            } else {
                result = getMessage(2, 6);
            }
        } catch (Exception e) {
            result = "{\"success\":false}";
            throw ServiceException.FAILURE("comapanyServlet.CompanyidExits:" + e.getMessage(), e);
        }
        return result;
    }

    public String isActivatedCompany(JSONObject jobj) throws ServiceException {
        String r = getMessage(1, 2);//"{\"success\": true, \"infocode\": \"m02\"}";
        try {
            boolean flag = isCompanyActivated(jobj);
            if (flag) {
                r = getMessage(1, 1);//"{\"success\": true, \"infocode\": \"m01\"}";
            }
        } catch (ServiceException e) {
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public String CompanyidExits(HttpServletRequest request) throws SQLException, ServiceException {
        String result = "{\"success\":false}";
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid = "";
            if (!jobj.isNull("companyid")) {
                companyid = jobj.getString("companyid");
            } else {
                return getMessage(2, 1);
            }
            List<Company> companyList = new ArrayList<>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            HashMap<String, Object> companyRequestParams = new HashMap<>();
            filter_names.add("c.companyID");
            filter_params.add(companyid);
            companyRequestParams.put("filter_names", filter_names);
            companyRequestParams.put("filter_values", filter_params);
            KwlReturnObject companyListObj = accCommonTablesDAO.getCompany(companyRequestParams);
            if (companyListObj.getEntityList().size() > 0) {
                companyList = companyListObj.getEntityList();
            }
            int count = companyList.size();
            if (count > 0) {
                result = getMessage(1, 1);
            } else {
                result = getMessage(1, 2);
            }
        } catch (Exception e) {
            result = "{\"success\":false}";
            throw ServiceException.FAILURE("comapanyServlet.CompanyidExits:" + e.getMessage(), e);
        }
        return result;
    }

    public String UserExits(HttpServletRequest request) throws SQLException, ServiceException {
        String result = "{\"success\":false}";
        try {
            String userid = "";
            boolean flag = false;
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            if (jobj.has("userid")) {
                userid = jobj.getString("userid");
            } else if (jobj.has("username")) {
                userid = jobj.getString("username");
                flag = true;
            }
            if (StringUtil.isNullOrEmpty(userid)) {
                return getMessage(2, 1);
            }

            KwlReturnObject kmsg = profileHandlerDAOObj.getUserWithUserName(userid, flag);
            int count = kmsg.getRecordTotalCount();
            if (count > 0) {
                result = getMessage(1, 3);
            } else {
                result = getMessage(1, 4);
            }
        } catch (Exception e) {
            result = "{\"success\":false}";
            throw ServiceException.FAILURE("comapanyServlet.UserExits", e);
        }
        return result;
    }

    public String UserDelete(HttpServletRequest request) throws SQLException, ServiceException {
        String result = "{\"success\":false}";
        try {
            User getuser = new User();
            String userid = "";
            boolean flag = false;
            String query = "";
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            if (jobj.has("userid")) {
                userid = jobj.getString("userid");
            } else if (jobj.has("username")) {
                userid = jobj.getString("username");
                flag = true;
            }
            if (!flag) {
                String[] uArr = userid.split(",");
                for (int i = 0; i < uArr.length; i++) {
                    KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), uArr[i]);
                    User u = (User) userResult.getEntityList().get(0);
                    if (u != null) {
                        profileHandlerDAOObj.deleteUser(u.getUserID());
                        result = getMessage(1, 7);
                    } else {
                        result = getMessage(2, 6);
                    }
                }
            }
            if (StringUtil.isNullOrEmpty(userid)) {
                return getMessage(2, 1);
            }
        } catch (Exception e) {
            result = "{\"success\":false, \"errormsg\": \"Following error occurred while deleting user : \"" + e.getMessage() + "}";
            throw ServiceException.FAILURE("comapanyServlet.CompanyDelete:" + e.getMessage(), e);
        }
        return result;
    }

    public String ActivateDeactivateUser(HttpServletRequest request, int action) throws SQLException, ServiceException {
        String result = "{\"success\":false}";
        try {
            String userid = "";
            boolean flag = false;
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            if (jobj.has("userid")) {
                userid = jobj.getString("userid");
            } else if (jobj.has("username")) {
                userid = jobj.getString("username");
                flag = true;
            }
            if (flag) {
                KwlReturnObject kmsg = profileHandlerDAOObj.getUserWithUserName(userid, flag);
                Iterator ite = kmsg.getEntityList().iterator();
                if (ite.hasNext()) {
                    UserLogin userObj = (UserLogin) ite.next();
                    userid = userObj.getUserID();
                } else {
                    return getMessage(2, 6);
                }
            } else {
                String[] uids = userid.split(",");

                for (int i = 0; i < uids.length; i++) {

                    KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), uids[i]);
                    User getuser = (User) userResult.getEntityList().get(0);
                    HashMap<String, Object> userHashMap = new HashMap<>();
                    userHashMap.put("userid", userid);
                    if (getuser != null) {
                        if (action == 6) {
                            userHashMap.put("deleteflag", 0);
                            result = getMessage(1, 9);
                        } else {
                            userHashMap.put("deleteflag", 1);
                            result = getMessage(1, 10);
                        }
                        KwlReturnObject rtObj = profileHandlerDAOObj.addUser(userHashMap);
                    } else {
                        result = getMessage(2, 6);
                    }
                }

            }
            if (StringUtil.isNullOrEmpty(userid)) {
                return getMessage(2, 1);
            }
        } catch (Exception e) {
            result = "{\"success\":false, \"errormsg\": \"Following error occurred while deleting user : \"" + e.getMessage() + "}";
            throw ServiceException.FAILURE("comapanyServlet.CompanyDelete:" + e.getMessage(), e);
        }
        return result;
    }

    private String createUser(HttpServletRequest request) throws SQLException, ServiceException {
        String result = "{\"success\":false}";
        try {
            String pwdText = "";
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String userid = jobj.isNull("userid") ? "" : jobj.getString("userid");
            String companyid = jobj.isNull("companyid") ? "" : jobj.getString("companyid");
            String username = jobj.isNull("username") ? "" : jobj.getString("username");
            String pwd = jobj.isNull("password") ? "" : jobj.getString("password");
            String fname = jobj.isNull("fname") ? "" : jobj.getString("fname");
            String lname = jobj.isNull("lname") ? "" : jobj.getString("lname");
            String emailid = jobj.isNull("emailid") ? "" : jobj.getString("emailid");
            String address = jobj.isNull("address") ? "" : jobj.getString("address");
            String contactno = jobj.isNull("contactno") ? "" : jobj.getString("contactno");

            if (StringUtil.isNullOrEmpty(companyid) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(fname)
                    || StringUtil.isNullOrEmpty(lname) || StringUtil.isNullOrEmpty(emailid)) {
                return getMessage(2, 1);
            }
            List list = accCompanyPreferencesObj.isCompanyExistWithCompanyID(jobj.getString("companyid"));
            if (list.isEmpty()) {
                return getMessage(2, 4);
            }
            try {
                List usl = profileHandlerDAOObj.getUserExistWithUserID(userid);
                if (usl.size() > 0) {
                    return getMessage(2, 7);
                }
            } catch (Exception e) {
            }
            if (jobj.isNull("password")) {
                pwdText = StringUtil.generateNewPassword();
                pwd = StringUtil.getSHA1(pwdText);
            }
            boolean isUserExist = profileHandlerDAOObj.isUserExist(username, companyid);
            if (isUserExist) {
                result = getMessage(2, 3);
                return result;
            }
            KwlReturnObject roleListResult = accountingHandlerDAOobj.getObject(Rolelist.class.getName(), Role.COMPANY_USER);
            Rolelist roleList = (Rolelist) roleListResult.getEntityList().get(0);

            KwlReturnObject dateformatObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), "18");
            KWLDateFormat dateFormat = (KWLDateFormat) dateformatObj.getEntityList().get(0);

            HashMap<String, Object> userHashMap = new HashMap<>();
            userHashMap.put("userid", userid);
            userHashMap.put("company", companyid);
            userHashMap.put("firstName", fname);
            userHashMap.put("lastName", lname);
            userHashMap.put("emailID", emailid);
            userHashMap.put("address", address);
            userHashMap.put("contactno", contactno);
            userHashMap.put("dateformat", "18");
            userHashMap.put("appid", "3");// ERP Application ID
            userHashMap.put("iscommit", true);
            userHashMap.put("password", pwd);
            userHashMap.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userHashMap.put("role", Role.COMPANY_USER);
            KwlReturnObject rtObj = profileHandlerDAOObj.addUser(userHashMap);
            User user = (User) rtObj.getEntityList().get(0);

            HashMap<String, Object> userLoginHashmap = new HashMap<>();
            userLoginHashmap.put("userName", username);
            userLoginHashmap.put("password", pwd);
            userLoginHashmap.put("user", user);
            userLoginHashmap.put("userid", user.getUserID());
            userLoginHashmap.put("saveStandAloneUserLogin", true);
            String logdate = authHandler.getGlobalDateFormat().format(new Date());
            Date loginDate = authHandler.getGlobalDateFormat().parse(logdate);
            userLoginHashmap.put("lastlogindate", loginDate);
            profileHandlerDAOObj.saveUserLogin(userLoginHashmap);

            HashMap<String, Object> roleusermap = new HashMap<String, Object>();
            roleusermap.put("user", user);
            roleusermap.put("roleid", roleList.getRoleid());
            profileHandlerDAOObj.saveRoleUserMapping(roleusermap);

            String diff = null;
            updatePreferences(request, null, (jobj.isNull("formatid") ? null : jobj.getString("formatid")), (jobj.isNull("tzid") ? null : jobj.getString("tzid")), diff);
            if (jobj.has("sendmail") && jobj.getBoolean("sendmail")) {
//                Company companyObj = (Company) session.get(Company.class,companyid);
                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) result1.getEntityList().get(0);
                User creater = (User) (company.getCreator());
                String fullnameCreator = StringUtil.getFullName(creater);
                String uri = URLUtil.getPageURL(request, Links.loginpageFull);
                String passwordString = "";
                if (jobj.isNull("password")) {
                    passwordString = "\n\nUsername: " + username + " \nPassword: " + pwdText;
                }
                String msgMailInvite = "Hi %s,\n\n%s has created an account for you at Deskera Accounting.\n\nDeskera Accounting is an Account Management Tool which you'll love using." + passwordString + "\n\nYou can log in at:\n%s\n\n\nSee you on Deskera Accounting\n\n - %s and The Deskera Acconting Team";
                String pmsg = String.format(msgMailInvite, user.getFirstName(), fullnameCreator, uri, fullnameCreator);
                if (jobj.isNull("password")) {
                    passwordString = "		<p>Username: <strong>%s</strong> </p>"
                            + "               <p>Password: <strong>%s</strong></p>";
                }
                String msgMailInviteUsernamePassword = "<html><head><title>Deskera Accounting - Your Deskera Account</title></head><style type='text/css'>"
                        + "a:link, a:visited, a:active {\n"
                        + " 	color: #03C;"
                        + "}\n"
                        + "body {\n"
                        + "	font-family: Arial, Helvetica, sans-serif;"
                        + "	color: #000;"
                        + "	font-size: 13px;"
                        + "}\n"
                        + "</style><body>"
                        + "	<div>"
                        + "		<p>Hi <strong>%s</strong>,</p>"
                        + "		<p>%s has created an account for you at %s.</p>"
                        + "             <p>Deskera Accounting is an Account Management Tool which you'll love using.</p>"
                        + passwordString
                        + "		<p>You can log in to Deskera Acconting at: <a href=%s>%s</a>.</p>"
                        + "		<br/><p>See you on Deskera Accounting!</p><p> - %s and The Deskera Accounting Team</p>"
                        + "	</div></body></html>";
                String htmlmsg = String.format(msgMailInviteUsernamePassword, user.getFirstName(), fullnameCreator, company.getCompanyName(), uri, uri, fullnameCreator);
                try {
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(new String[]{user.getEmailID()}, "[Deskera] Welcome to Deskera Accounting", htmlmsg, pmsg, creater.getEmailID(), smtpConfigMap);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
            result = getMessage(1, 5);
        } catch (Exception e) {
//            result = "{\"success\":true, \"successmsg\": \"Following error occured while creating user : \""+e.getMessage()+"}";
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return result;
    }

    private String saveCustomer(HttpServletRequest request) throws SQLException, ServiceException {
        JSONObject result = new JSONObject();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid = jobj.isNull("companyid") ? "" : jobj.getString("companyid");
            String accountname = jobj.isNull("accountname") ? "" : jobj.getString("accountname");
            String address = jobj.isNull("address") ? "-" : jobj.getString("address");
            String email = jobj.isNull("email") ? "" : jobj.getString("email");
            String contactno = jobj.isNull("contactno") ? "" : jobj.getString("contactno");
            String accountid = jobj.isNull("accountid") ? "" : jobj.getString("accountid");
            String seqFormatID = jobj.isNull("sequenceformatid") ? "" : jobj.getString("sequenceformatid");
            String entryNumber = jobj.isNull("accountcode") ? "" : jobj.getString("accountcode");
            boolean isVendor = jobj.isNull("isVendor") ? false : Boolean.parseBoolean(jobj.getString("isVendor"));

            List ll = accCompanyPreferencesObj.isCompanyExistWithCompanyID(companyid);
            if (ll.isEmpty()) {
                return getMessage(2, 4);
            }

            Date date1 = sdf.parse(jobj.getString("accountcreationdateStr"));
            Date creationDateDt = date1;//sdf.parse(creationDateDtr);
            KwlReturnObject cmpResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cmpResult.getEntityList().get(0);
            String currencyid = null;
            if (preferences != null && preferences.getCompany() != null && preferences.getCompany().getCurrency() != null) {
                currencyid = preferences.getCompany().getCurrency().getCurrencyID();
            }
            if (isVendor) {
                Vendor vendor = null;
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("synchedfromotherapp", true);
                requestParams.put("creationDate", creationDateDt);
                if (preferences != null) {
                    requestParams.put("accountid", preferences.getVendordefaultaccount().getID());
                }
                requestParams.put("accname", accountname);
                requestParams.put("crmaccountid", accountid);
                if (!StringUtil.isNullOrEmpty(currencyid)) {
                    requestParams.put("currencyid", currencyid);
                }

                boolean isAutoGenerated = false;
                String accountcode = "";
                SequenceFormat sequenceFormat = null;
                if (StringUtil.isNullOrEmpty(seqFormatID)) { //if sequnece format is not coming from CRM in this case giving exception
                    throw ServiceException.FAILURE("Sequnece Format not found for customer", null);
                } else {
                    if (seqFormatID.equalsIgnoreCase("NA")) {
                        KwlReturnObject returnObj = accCustomerDAOobj.checkCustomerExistbyCode(entryNumber, companyid, "");
                        List listObj = returnObj.getEntityList();
                        if (listObj.isEmpty()) {
                            accountcode = entryNumber;
                            isAutoGenerated = false;
                        } else {
                            throw ServiceException.FAILURE("Duplicate Customer Code", null);
                        }
                    } else {
                        KwlReturnObject sfResult = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), seqFormatID);
                        sequenceFormat = (SequenceFormat) sfResult.getEntityList().get(0);
                        if (sequenceFormat == null) {
                            throw ServiceException.FAILURE("Invalid Sequence Format", null);
                        }
                        KwlReturnObject returnObj = accCustomerDAOobj.checkCustomerExistbyCode(entryNumber, companyid, "");
                        List listObj = returnObj.getEntityList();
                        if (listObj.isEmpty()) {
                            accountcode = entryNumber;
                            isAutoGenerated = false;
                        } else {
                            throw ServiceException.FAILURE("Duplicate Customer Code", null);
                        }
                    }
                }

                requestParams.put("autogenerated", isAutoGenerated);
                requestParams.put("acccode", accountcode);
                requestParams.put(Constants.SEQFORMAT, sequenceFormat != null ? sequenceFormat.getID() : null);
                KwlReturnObject resultcheck = accCustomerDAOobj.getDefaultCreditTermForCustomer(companyid);
                Term term = null;
                if (!resultcheck.getEntityList().isEmpty()) {
                    term = (Term) resultcheck.getEntityList().get(0);
                    if (term != null) {
                        requestParams.put("termid", term.getID());
                    }
                }
                requestParams.put("companyid", companyid);

                KwlReturnObject custResult = accVendorDAOobj.addVendor(requestParams);
                vendor = (Vendor) custResult.getEntityList().get(0);

                if (vendor != null) {
                    HashMap<String, Object> venAddrMap = new HashMap<String, Object>();
                    venAddrMap.put("aliasName", "Billing Address1");
                    venAddrMap.put("address", address);
                    venAddrMap.put("phone", contactno);
                    venAddrMap.put("emailID", email);
                    venAddrMap.put("isBillingAddress", true);
                    venAddrMap.put("isDefaultAddress", true);
                    venAddrMap.put("customerid", vendor.getID());
                    accountingHandlerDAOobj.saveVendorAddressesDetails(venAddrMap, companyid);

                    venAddrMap.put("aliasName", "Shipping Address1");
                    venAddrMap.put("isBillingAddress", false);
                    accountingHandlerDAOobj.saveVendorAddressesDetails(venAddrMap, companyid);
                }
                result.put("sucess", true);
                result.put("erpCustomerId", vendor.getID());
                result.put("message", getMessage(1, 5));
            } else {
                Customer customer = null;
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("synchedfromotherapp", true);
                requestParams.put("creationDate", creationDateDt);
                if (preferences != null) {
                    requestParams.put("accountid", preferences.getCustomerdefaultaccount().getID());
                }
                requestParams.put("accname", accountname);
                requestParams.put("crmaccountid", accountid);
                boolean isAutoGenerated = false;
                String accountcode = "";
                SequenceFormat sequenceFormat = null;
                if (!StringUtil.isNullOrEmpty(currencyid)) {
                    requestParams.put("currencyid", currencyid);
                }
                // getting sequence number for customer 
                if (StringUtil.isNullOrEmpty(seqFormatID)) { //if sequnece format is not coming from CRM in this case giving exception
                    throw ServiceException.FAILURE("Sequnece Format not found for customer", null);
                } else {
                    if (seqFormatID.equalsIgnoreCase("NA")) {
                        KwlReturnObject returnObj = accCustomerDAOobj.checkCustomerExistbyCode(entryNumber, companyid, "");
                        List listObj = returnObj.getEntityList();
                        if (listObj.isEmpty()) {
                            accountcode = entryNumber;
                            isAutoGenerated = false;
                        } else {
                            throw ServiceException.FAILURE("Duplicate Customer Code", null);
                        }
                    } else {
                        KwlReturnObject sfResult = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), seqFormatID);
                        sequenceFormat = (SequenceFormat) sfResult.getEntityList().get(0);
                        if (sequenceFormat == null) {
                            throw ServiceException.FAILURE("Invalid Sequence Format", null);
                        }
                        KwlReturnObject returnObj = accCustomerDAOobj.checkCustomerExistbyCode(entryNumber, companyid, "");
                        List listObj = returnObj.getEntityList();
                        if (listObj.isEmpty()) {
                            accountcode = entryNumber;
                            isAutoGenerated = false;
                        } else {
                            throw ServiceException.FAILURE("Duplicate Customer Code", null);
                        }
                    }
                }

                requestParams.put("autogenerated", isAutoGenerated);
                requestParams.put("acccode", accountcode);
                requestParams.put(Constants.SEQFORMAT, sequenceFormat != null ? sequenceFormat.getID() : null);
                KwlReturnObject resultcheck = accCustomerDAOobj.getDefaultCreditTermForCustomer(companyid);
                Term term = null;
                if (!resultcheck.getEntityList().isEmpty()) {
                    term = (Term) resultcheck.getEntityList().get(0);
                    if (term != null) {
                        requestParams.put("termid", term.getID());
                    }
                }
                requestParams.put("companyid", companyid);

                KwlReturnObject custResult = accCustomerDAOobj.addCustomer(requestParams);
                customer = (Customer) custResult.getEntityList().get(0);

                if (customer != null) {

                    HashMap<String, Object> custAddrMap = new HashMap<String, Object>();
                    custAddrMap.put("aliasName", "Billing Address1");
                    custAddrMap.put("address", address);
                    custAddrMap.put("phone", contactno);
                    custAddrMap.put("emailID", email);
                    custAddrMap.put("isBillingAddress", true);
                    custAddrMap.put("isDefaultAddress", true);
                    custAddrMap.put("customerid", customer.getID());
                    accountingHandlerDAOobj.saveCustomerAddressesDetails(custAddrMap, companyid);

                    custAddrMap.put("aliasName", "Shipping Address1");
                    custAddrMap.put("isBillingAddress", false);
                    accountingHandlerDAOobj.saveCustomerAddressesDetails(custAddrMap, companyid);
                }

                result.put("success", true);
                result.put("erpCustomerId", customer.getID());
                result.put("message", getMessage(1, 5));
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return result.toString();
    }

    public String getCustomerSequenceFormatToOtherAPPs(HttpServletRequest request) throws SQLException, ServiceException {
        String result = "{\"success\":false}";
        JSONArray jArr = new JSONArray();
        JSONObject Jobj = new JSONObject();
        try {
            String companyid = "";
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            if (jobj.has("companyid")) {
                companyid = jobj.getString("companyid");
            }
            List ll = accCompanyPreferencesObj.isCompanyExistWithCompanyID(companyid);
            if (ll.isEmpty()) {
                return getMessage(2, 4);
            }
            JSONObject naSeqObj = new JSONObject();
            naSeqObj.put("id", "NA");
            naSeqObj.put("value", "NA");
            naSeqObj.put("oldflag", false);
            jArr.put(naSeqObj);
            KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getActivatedSequenceFormat(companyid);
            Iterator itr = seqFormatResult.getEntityList().iterator();
            while (itr.hasNext()) {
                SequenceFormat seqFormat = (SequenceFormat) itr.next();
                JSONObject j = new JSONObject();
                j.put("id", seqFormat.getID());
                j.put("value", seqFormat.isDateBeforePrefix() ? seqFormat.getDateformatinprefix() + seqFormat.getName() : seqFormat.getName());
                j.put("dateFormat", seqFormat.getDateformatinprefix() == null ? "" : seqFormat.getDateformatinprefix().equals("empty") ? "" : seqFormat.getDateformatinprefix());
                j.put("prefix", seqFormat.getPrefix());
                j.put("suffix", seqFormat.getSuffix());
                j.put("numberofdigit", seqFormat.getNumberofdigit());
                j.put("startfrom", seqFormat.getStartfrom());
                j.put("showleadingzero", seqFormat.isShowleadingzero() ? "Yes" : "No");
                j.put("oldflag", false);
                jArr.put(j);
            }
            Jobj.put("data", jArr);
            Jobj.put("success", true);
            result = Jobj.toString();
        } catch (Exception e) {
            result = "{\"success\":false}";
            throw ServiceException.FAILURE("comapanyServlet.UserExits", e);
        }
        return result;
    }

    private String updateCRMAccountIDForCustomer(HttpServletRequest request) throws SQLException, ServiceException {
        String result = "{\"success\":false}";
        try {
            JSONObject customerobj = new JSONObject(request.getParameter("data"));
            JSONArray customerArr = customerobj.getJSONArray("data");
            if (customerArr.length() > 0) {
                for (int i = 0; i < customerArr.length(); i++) {
                    JSONObject custObj = customerArr.getJSONObject(i);
                    String erpcustomerid = custObj.isNull("erpcustomerid") ? "" : custObj.getString("erpcustomerid");
                    String crmaccountid = custObj.isNull("crmaccountid") ? "" : custObj.getString("crmaccountid");
                    KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), erpcustomerid);
                    Customer customer = (Customer) customerResult.getEntityList().get(0);

                    if (customer != null) {
                        HashMap<String, Object> customrequestParams = new HashMap<>();
                        customrequestParams.put("accid", customer.getID());
                        customrequestParams.put("crmaccountid", crmaccountid);
                        KwlReturnObject accresult = accCustomerDAOobj.updateCustomer(customrequestParams);
                    }
                }
            }

            result = getMessage(1, 5);

        } catch (JSONException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private String saveCustomerFromCRM(HttpServletRequest request) throws SQLException, ServiceException {
        JSONObject jObj = new JSONObject();
        try {
            JSONObject customerobj = new JSONObject(request.getParameter("data"));
            String companyid = StringUtil.isNullOrEmpty(customerobj.getString("companyid")) ? "" : (String) customerobj.getString("companyid");
            HashMap<String, Object> params = new HashMap<>();
            params.put("companyid", companyid);
            jObj = accCustomerControllerCMNServiceObj.getCustomerFromCRMAccounts(params, customerobj);
        } catch (JSONException | ServiceException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jObj.toString();
    }

    private String saveCustomerFromLMS(HttpServletRequest request) throws SQLException, ServiceException {
        JSONObject result = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean flag = false;
        try {
            JSONObject customerobj = new JSONObject(request.getParameter("data"));
            String companyid = StringUtil.isNullOrEmpty(customerobj.getString("companyid")) ? "" : (String) customerobj.getString("companyid");
            HashMap<String, Object> params = new HashMap<>();
            params.put("companyid", companyid);
            params.put("gcurrencyid", "");
            jArr = accAccountControllerCMNServiceObj.saveCustomerDataFromLMS(params, customerobj);
            flag = true;
        } catch (JSONException | ServiceException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            try {
                result.put("data", jArr);
                result.put("success", flag);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    private String isCustomerAccountExists(HttpServletRequest request) throws SQLException, ServiceException {
        String result = "{\"success\":false}";
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));

            String companyid = jobj.isNull("companyid") ? "" : jobj.getString("companyid");
            String customerName = jobj.isNull("accountname") ? "" : jobj.getString("accountname");
            boolean isVendor = jobj.isNull("isVendor") ? false : Boolean.parseBoolean(jobj.getString("isVendor"));
            List ll = accCompanyPreferencesObj.isCompanyExistWithCompanyID(companyid);
            if (ll.isEmpty()) {
                return getMessage(2, 4);
            }
            if (isVendor) {
                KwlReturnObject retObj = accVendorDAOobj.getVendorByName(customerName, companyid);
                if (retObj.getEntityList().isEmpty()) {
                    result = "{\"success\": true, \"msg\": \"Vendor with same name doesn't exist.\", \"duplicateAccount\":false}";
                } else {
                    result = "{\"success\": true, \"msg\": \"Vendor with same name already exists.\", \"duplicateAccount\":true}";
                }
                retObj = accAccountDAOobj.getAllAccountsFromName(companyid, result);
                if (retObj.getEntityList().isEmpty()) {
                    result = "{\"success\": true, \"msg\": \"Vendor account with same name doesn't exist.\", \"duplicateAccount\":false}";
                } else {
                    result = "{\"success\": true, \"msg\": \"Vendor account with same name already exists.\", \"duplicateAccount\":true}";
                }

                if (jobj.has("entryNumber") && !StringUtil.isNullOrEmpty(jobj.optString("entryNumber", ""))) {
                    String entryNumber = jobj.getString("entryNumber");
                    retObj = accVendorDAOobj.getVendorByCode(entryNumber, companyid);
                    if (retObj.getEntityList().isEmpty()) {
                        result = "{\"success\": true, \"msg\": \"Customer with same code doesn't exist.\", \"duplicateCode\":false}";
                    } else {
                        result = "{\"success\": true, \"msg\": \"Customer with same code already exists.\", \"duplicateCode\":true}";
                    }
                }
            } else {
                //Check if customer with same name already exists.
                KwlReturnObject retObj = accCustomerDAOobj.getCustomerByName(customerName, companyid);
                if (retObj.getEntityList().isEmpty()) {
                    result = "{\"success\": true, \"msg\": \"Customer with same name doesn't exist.\", \"duplicateAccount\":false}";
                } else {
                    result = "{\"success\": true, \"msg\": \"Customer with same name already exists.\", \"duplicateAccount\":true}";
                }

                retObj = accAccountDAOobj.getAllAccountsFromName(companyid, result);
                if (retObj.getEntityList().isEmpty()) {
                    result = "{\"success\": true, \"msg\": \"Customer account with same name doesn't exist.\", \"duplicateAccount\":false}";
                } else {
                    result = "{\"success\": true, \"msg\": \"Customer account with same name already exists.\", \"duplicateAccount\":true}";
                }

                if (jobj.has("entryNumber") && !StringUtil.isNullOrEmpty(jobj.optString("entryNumber", ""))) { //this case for converting lead in CRM
                    String entryNumber = jobj.getString("entryNumber");
                    retObj = accCustomerDAOobj.getCustomerByCode(customerName, companyid);
                    if (retObj.getEntityList().isEmpty()) {
                        result = "{\"success\": true, \"msg\": \"Customer with same code doesn't exist.\", \"duplicateCode\":false}";
                    } else {
                        result = "{\"success\": true, \"msg\": \"Customer with same code already exists.\", \"duplicateCode\":true}";
                    }
                }
            }
        } catch (JSONException | ServiceException e) {
            result = "{\"success\":false, \"msg\": \"Following error occurred while validating customer : \"" + e.getMessage() + "}";
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return result;
    }

    /*
     * Code is commented as this function is not used in CRM.
     */
//
//    private String saveQuotation(HttpServletRequest request) throws SQLException, ServiceException {
//        String result = "{\"success\":false}";
//        try {
//            JSONObject jobj = new JSONObject(request.getParameter("data"));
//            JSONObject quotationdata = new JSONObject();
//            JSONArray jArray = jobj.getJSONArray("quotationdata");
//            for (int i = 0; i < jArray.length(); i++) {
//                JSONObject jobjQuote = jArray.getJSONObject(i);
//
//                JSONObject retObjQuote = new JSONObject();
//                String companyid = (String) jobjQuote.get("companyid");
//                HashMap<String, Object> qDataMap = new HashMap<>();
//                qDataMap.put("entrynumber", (String) jobjQuote.get("quotationnumber"));
//                qDataMap.put("crmquoatationid", (String) jobjQuote.get("quotationid"));
//                qDataMap.put("autogenerated", false);
//                qDataMap.put("perDiscount", (Boolean) jobjQuote.get("discounttype"));
//                if (jobjQuote.has("discount")) {
//                    qDataMap.put("discount", Double.parseDouble(jobjQuote.get("discount").toString()));
//                }
//                qDataMap.put("memo", (String) jobjQuote.get("memo"));
//                qDataMap.put("shipvia", (String) jobjQuote.get("shipvia"));
//                qDataMap.put("shippingterm", (String) jobjQuote.get("shippingterm"));
//                qDataMap.put("fob", (String) jobjQuote.get("fob"));
//                qDataMap.put("createdby", (String) jobjQuote.get("createdbyid"));
//                String paymentterm = "";
//                if (jobjQuote.has("paymentterm") && jobjQuote.get("paymentterm") != null) {
//                    paymentterm = (String) jobjQuote.get("paymentterm");
//                }
//
//                HashMap<String, Object> termMap = new HashMap<>();
//                termMap.put("termname", paymentterm);
//                termMap.put("companyid", companyid);
//                KwlReturnObject termsResult = accTermObj.getTerm(termMap);
//                Term term = (Term) termsResult.getEntityList().get(0);
//                qDataMap.put("termid", term.getID());
//
//                if (jobjQuote.has("shipdate")) {
//                    Date date = new Date();
//                    date.setTime(jobjQuote.getLong("shipdate"));
//                    Date shipdate = date;
//                    qDataMap.put("shipdate", shipdate);
//                }
//
//                KwlReturnObject retObj = accCustomerDAOobj.getCustomerByName(jobjQuote.get("customer").toString(), companyid);
//                List list = retObj.getEntityList();
//
//                Iterator itr = list.iterator();
//                Customer customer = null;
//                while (itr.hasNext()) {
//                    customer = (Customer) itr.next();
//                }
//                qDataMap.put("customerid", customer.getID());
//                Company company;
//                if (jobjQuote.get("companyid") == null) {
//                    company = null;
//                } else {
//                    KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), (String) jobjQuote.get("companyid"));
//                    company = (Company) companyResult.getEntityList().get(0);
//                }
//                qDataMap.put("companyid", companyid);
//
//                KWLCurrency currency = null;
//                if (jobjQuote.has("quotationcurrency") && jobjQuote.get("quotationcurrency") != null) {
//                    KwlReturnObject currencyResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), (String) jobjQuote.get("quotationcurrency"));
//                    currency = (KWLCurrency) currencyResult.getEntityList().get(0);
//                } else {
//                    if (customer.getAccount().getCurrency() != null) {
//                        currency = customer.getAccount().getCurrency();
//                    } else {
//                        currency = company.getCurrency();
//                    }
//                }
//                qDataMap.put("currencyid", currency.getCurrencyID());
//                Map<String, Object> addressParams = new HashMap<>();
//                addressParams.put(Constants.SHIPPING_ADDRESS, jobjQuote.optString("shippingaddress", ""));
//                addressParams.put(Constants.BILLING_ADDRESS, jobjQuote.optString("street", ""));
//                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
//                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
//                qDataMap.put("billshipAddressid", bsa.getID());
//                Date date1 = new Date();
//                date1.setTime(jobjQuote.getLong("quotationdate"));
//                Date creationDateDt = date1;//sdf.parse(creationDateDtr);
//                qDataMap.put("orderdate", creationDateDt);
//                qDataMap.put("approvestatuslevel", 11);
//                Date date2 = new Date();
//                date2.setTime(jobjQuote.getLong("quotationduedate"));
//                qDataMap.put("duedate", date2);
//                creationDateDt = date1;//sdf.parse(creationDateDtr);
//                if (term != null) {
//                    Calendar calendarDueDate = Calendar.getInstance();
//                    long quotationDatelong = jobjQuote.getLong("quotationdate");
//                    calendarDueDate.setTimeInMillis(quotationDatelong);
//                    calendarDueDate.add(Calendar.DATE, term.getTermdays());
//                    qDataMap.put("duedate", calendarDueDate.getTime());
//                }
//                creationDateDt = date1;//sdf.parse(creationDateDtr);
//                if (jobjQuote.has("taxid")) {
//                    qDataMap.put("taxid", jobjQuote.has("taxid"));
//                }
//                if (jobjQuote.has("contractid")) {
//                    qDataMap.put("contractid", jobjQuote.has("contractid"));
//                }
//                if (jobjQuote.has("replacementno")) {
//                    KwlReturnObject contractResult = accountingHandlerDAOobj.getObject(Contract.class.getName(), (String) jobjQuote.get("contractid"));
//                    Contract contract = (Contract) contractResult.getEntityList().get(0);
//                    if (!contract.isNormalContract()) {
//                        qDataMap.put("isLeaseFixedAsset", true);
//                    }
//                    qDataMap.put("quotationtype", 1);
//                }
//                if (jobjQuote.has("maintenanceno")) {
//                    qDataMap.put("quotationtype", 2);
//                    qDataMap.put("maintenanceid", jobjQuote.get("maintenanceid"));
//                }
//
//                KwlReturnObject soresult = accSalesOrderDAOobj.saveQuotation(qDataMap);
//                Quotation quotation = (Quotation) soresult.getEntityList().get(0);
//
//                String gcurrencyid = company.getCurrency().getCurrencyID();
//                HashMap<String, Object> requestParams = new HashMap<>();
//                requestParams.put("companyid", companyid);
//                requestParams.put("gcurrencyid", gcurrencyid);
//
//                JSONArray jArrQProds = jobjQuote.getJSONArray("quotationproducts");
//                for (int j = 0; j < jArrQProds.length(); j++) {
//                    JSONObject jobj1 = jArrQProds.getJSONObject(j);
//
//                    if (jobj1.has("productid")) {
//                        Product product;
//                        if (jobj1.get("productid") == null) {
//                            product = null;
//                        } else {
//                            KwlReturnObject productResult = accountingHandlerDAOobj.getObject(Product.class.getName(), (String) jobj1.get("productid"));
//                            product = (Product) productResult.getEntityList().get(0);
//                        }
//                        if (product != null) {
//                            HashMap<String, Object> qdDataMap = new HashMap<>();
//                            qdDataMap.put("soid", quotation.getID());
////                                     
//                            if (jobjQuote.has("replacementid")) {
//                                String replacementid = (String) jobjQuote.get("replacementid");
//                                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(ProductReplacementDetail.class.getName(), replacementid);
//                                if (!rdresult.getEntityList().isEmpty()) {
//                                    ProductReplacementDetail pr = (ProductReplacementDetail) rdresult.getEntityList().get(0);
//                                    qdDataMap.put("productreplacementDetailId", pr.getId());
//                                }
//                            }
//                            if (jobj1.has("srno")) {
//                                qdDataMap.put("srno", jobj.getInt("srno"));
//                            }
//                            qdDataMap.put("baseuomquantity", 1);
//                            qdDataMap.put("baseuomrate", 1);
//
//                            if (jobj1.has("unitprice")) {
//                                KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, Double.parseDouble(jobj1.get("unitprice").toString()), currency.getCurrencyID(), creationDateDt, 0);
//                                qdDataMap.put("rate", Double.parseDouble(jobj1.get("unitprice").toString()));
//                            }
//                            if (jobj1.has("quantity") && jobj1.get("quantity") != null) {
//                                qdDataMap.put("quantity", Double.parseDouble(jobj1.get("quantity").toString()));
//                            }
//                            if (jobj1.has("remark")) {
//                                qdDataMap.put("remark", (String) jobj1.get("remark"));
//                            }
//                            if (jobj1.has("description")) {
//                                qdDataMap.put("desc", (String) jobj1.get("description"));
//                            }
//                            if (jobj1.has("discount")) {
//                                qdDataMap.put("discount", Double.parseDouble(jobj1.get("discount").toString()));
//                            }
//                            if (jobj1.has("discountispercent")) {
//                                int ispercent = 0;
//                                if (!StringUtil.isNullOrEmpty(jobj1.get("discountispercent").toString())) {
//                                    if (Boolean.parseBoolean(jobj1.get("discountispercent").toString())) {
//                                        ispercent = 1;
//                                    } else if (StringUtil.equal(jobj1.get("discountispercent").toString(), "1")) {
//                                        ispercent = Integer.parseInt(jobj1.get("discountispercent").toString());
//                                    }
//                                }
//                                qdDataMap.put("discountispercent", ispercent);
//                            }
//                            JSONObject retObjDetails = new JSONObject();
//                            retObjQuote.append("quotationproducts", retObjDetails);
//                            qdDataMap.put("productid", product.getID());
//                            qdDataMap.put("companyid", company.getCompanyID());
//                            if (jobj1.has("rowtaxid")) {
//                                KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), jobj1.get("rowtaxid").toString());
//                                Tax rowtax = (Tax) txresult.getEntityList().get(0);
//                                if (rowtax != null) {
//                                    qdDataMap.put("rowtaxid", rowtax.getID());
//                                    if (jobj1.has("rowtaxamount") && jobj1.get("rowtaxamount") != null) {
//                                        qdDataMap.put("rowTaxAmount", Double.parseDouble(jobj1.get("rowtaxamount").toString()));
//                                    }
//                                }
//                            }
//                            KwlReturnObject quoteDetailsResult = accSalesOrderDAOobj.saveQuotationDetails(qdDataMap);
//                            QuotationDetail row = (QuotationDetail) quoteDetailsResult.getEntityList().get(0);
//                        }
//                    }
//                }
//                quotationdata.append("quotationdata", retObjQuote);
//            }
//
//            result = getMessage(1, 5);
//        } catch (JSONException | NumberFormatException | ServiceException e) {
//            throw ServiceException.FAILURE(e.getMessage(), e);
//        }
//        return result;
//    }
    private String deleteQuotationRequest(HttpServletRequest request) throws AccountingException {
        JSONObject result = new JSONObject();
        boolean successFlag = true;
        String msg = "";
        String deletedTransaction = "";
        String linkedQuotaions = "";
        String linkQuotationIds = "";
        try {
            JSONObject jobject = new JSONObject(request.getParameter("data"));
            String companyID = jobject.getString("companyid");
            JSONArray quotationIds = jobject.getJSONArray("quotationids");

            for (int i = 0; i < quotationIds.length(); i++) {
                String crmqid = (String) quotationIds.get(i);
                String qid = "";
                if (!StringUtil.isNullOrEmpty(crmqid)) {
                    HashMap<String, Object> requestParamsNew = new HashMap<>();
                    requestParamsNew.put("crmquoatationid", crmqid);
                    requestParamsNew.put("companyid", companyID);
                    requestParamsNew.put("archieve", 0);   // Ask about this to sagar sir.
                    KwlReturnObject quoteResult = accSalesOrderDAOobj.getQuotations(requestParamsNew);
                    List<String> qlist = quoteResult.getEntityList();
                    if (qlist != null && qlist.size() > 0) {
                        for (String quoteid : qlist) {
                            KwlReturnObject quotationResult = accountingHandlerDAOobj.getObject(Quotation.class.getName(), quoteid);
                            Quotation quo = (Quotation) quotationResult.getEntityList().get(0);
                            String qno = "";
                            qno = quo.getQuotationNumber();
                            qid = quo.getID();
                            KwlReturnObject invResult = accSalesOrderDAOobj.getQTforinvoice(qid, companyID);  //for checking Customer Quotation used in invoice or not
                            int count1 = invResult.getRecordTotalCount();
                            if (count1 > 0) {
                                linkedQuotaions += qno + ", ";
                                linkQuotationIds += crmqid + ", ";
                                continue;
                            }
                            KwlReturnObject soResults = accSalesOrderDAOobj.getSOforQT(qid, companyID);  //for checking Customer Quotation used in sales order or not
                            int count2 = soResults.getRecordTotalCount();
                            if (count2 > 0) {
                                linkedQuotaions += qno + ", ";
                                linkQuotationIds += crmqid + ", ";
                                continue;
                            }
                            if (count1 == 0 && count2 == 0) {
                                HashMap<String, Object> requestParams = new HashMap<>();
                                requestParams.put("qid", qid);
                                requestParams.put("companyid", companyID);
                                accSalesOrderDAOobj.deleteQuotationsPermanent(requestParams);
                                deletedTransaction += qno + ", ";
                            }
                        }
                    }
                }
            }
            if (StringUtil.isNullOrEmpty(linkedQuotaions)) {
                msg = "Quotation(s) has been deleted successfully";
            } else {
                msg = "Quotation(s) except " + linkedQuotaions.substring(0, linkedQuotaions.length() - 2) + " has been deleted successfully.";
            }

        } catch (ServiceException | JSONException ex) {
            successFlag = false;
            msg = "Failed to make Connection with web server";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            JSONObject retObj = new JSONObject();
            try {
                retObj.put("successFlag", successFlag);
                retObj.put("msg", msg);
                retObj.put("deletedTransaction", deletedTransaction);
                retObj.put("linkedQuotaions", linkedQuotaions);
                retObj.put("linkedQuotaionIds", linkQuotationIds);
                JSONArray returnArr = new JSONArray();
                returnArr.put(retObj);
                result.put("data", returnArr);
                result.put("success", successFlag);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }
    /*
     * Code is commented as this function is not used in CRM.
     */
//    private String saveQuotationWhenOpportunityStageChanged(Session session, HttpServletRequest request) throws SQLException, ServiceException {
//        String result = "{\"success\":false}";
//        try {
//            JSONArray jArr = new JSONArray();
//            JSONObject jobj = new JSONObject(request.getParameter("data"));
//
//            JSONObject quotationdata = new JSONObject();
//            JSONArray jArray = jobj.getJSONArray("quotationdata");
//            for (int i = 0; i < jArray.length(); i++) {
//                JSONObject jobjQuote = jArray.getJSONObject(i);
//                String companyid = (String) jobjQuote.get("companyid");
//                String hql = " from Customer where name = ? and company.companyID = ? ";
//                List list = HibernateUtil.executeQuery(session, hql, new Object[]{jobjQuote.get("customer"), companyid});
//                if (list.size() > 0) {
//
//                    JSONObject retObjQuote = new JSONObject();
//
//
//                    Quotation quotation = new Quotation();
//
//                    hql = " from Quotation where crmquoteid = ? and company.companyID = ? ";
//                    List Quotationlist = HibernateUtil.executeQuery(session, hql, new Object[]{jobjQuote.get("quotationid"), companyid});
//                    if (Quotationlist.size() > 0) {
//                        Iterator Quotationitr = Quotationlist.iterator();
//                        if (Quotationitr.hasNext()) {
//                            quotation = null;
//                            quotation = (Quotation) Quotationitr.next();
//                        }
//                    }
//                    try {
//                        hql = "delete from QuotationDetail where quotation.ID = ?";
//                        int QuotationDetailsDeletedCount = HibernateUtil.executeUpdate(session, hql, new Object[]{quotation.getID()});
//
//
//                        quotation.setDeleted(false);
//
//                        quotation.setquotationNumber((String) jobjQuote.get("quotationnumber"));
//                        quotation.setCrmquoteid((String) jobjQuote.get("quotationid"));
//
//                        quotation.setPerDiscount((Boolean) jobjQuote.get("discounttype"));
//                        if (jobjQuote.has("discount")) {
//                            quotation.setDiscount(Double.parseDouble(jobjQuote.get("discount").toString()));
//                        }
//
//                        quotation.setMemo((String) jobjQuote.get("memo"));
//
//                        quotation.setShipvia((String) jobjQuote.get("shipvia"));
//
//                        quotation.setFob((String) jobjQuote.get("fob"));
//
//                        if (jobjQuote.has("shipdate")) {
//                            Date date = new Date();
//                            date.setTime(jobjQuote.getLong("shipdate"));
//                            Date shipdate = date;
//                            quotation.setShipdate(shipdate);
//                        }
//
//
//                        Iterator itr = list.iterator();
//                        Customer customer = null;
//                        while (itr.hasNext()) {
//                            customer = (Customer) itr.next();
//                        }
//
//                        quotation.setCustomer(customer);
//
//                        Company company = jobjQuote.get("companyid") == null ? null : (Company) session.get(Company.class, (String) jobjQuote.get("companyid"));
//                        quotation.setCompany(company);
//                        KWLCurrency currency = null;
//                        if (jobjQuote.has("quotationcurrency") && jobjQuote.get("quotationcurrency") != null) {
//                            currency = (KWLCurrency) session.get(KWLCurrency.class, (String) jobjQuote.get("quotationcurrency"));
//                        } else {
//                            if (customer.getAccount().getCurrency() != null) {
//                                currency = customer.getAccount().getCurrency();
//                            } else {
//                                currency = company.getCurrency();
//                            }
//                        }
//                        quotation.setCurrency(currency);
//
//                        Date date1 = new Date();
//
//                        //Creation date
//                        date1.setTime(jobjQuote.getLong("quotationdate"));
////                String creationDateDtr = sdf1.format(date1);
//
//                        Date creationDateDt = date1;//sdf.parse(creationDateDtr);
////                quoteDate.setTimeInMillis(jobjQuote.getLong("quotationdate"));
//                        quotation.setQuotationDate(creationDateDt);
//
//
//                        //Due Date
//                        date1.setTime(jobjQuote.getLong("quotationduedate"));
//
////                creationDateDtr = sdf1.format(date1);
//
//                        creationDateDt = date1;//sdf.parse(creationDateDtr);
//
////                quoteDate.setTimeInMillis();
//                        quotation.setDueDate(creationDateDt);
//                        /*
//                         * Tax part will update later
//                         */
//                        if (jobjQuote.has("taxid")) {
//                            Tax tax = jobjQuote.get("taxid") == null ? null : (Tax) session.get(Tax.class, (String) jobjQuote.get("taxid"));
//                            quotation.setTax(tax);
//                        }
//
//
//
//                        session.saveOrUpdate(quotation);
////                if (jobj.get("sodetails") != null) {
////                    quotation.setRows((Set<QuotationDetail>) jobj.get("sodetails"));
////                }
//                        String gcurrencyid = company.getCurrency().getCurrencyID();
//                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
//                        requestParams.put("companyid", companyid);
//                        requestParams.put("gcurrencyid", gcurrencyid);
//
//                        JSONArray jArrQProds = jobjQuote.getJSONArray("quotationproducts");
//
//                        for (int j = 0; j < jArrQProds.length(); j++) {
//                            JSONObject jobj1 = jArrQProds.getJSONObject(j);
//                            if (jobj1.has("productid")) {
//                                Product product = jobj1.get("productid") == null ? null : (Product) session.get(Product.class, (String) jobj1.get("productid"));
//                                if (product != null) {
//                                    QuotationDetail quotationDetail = new QuotationDetail();
//
////                                hql = " from QuotationDetail where quotation.ID = ? and product.ID";
//////                                List QuotationDetailsList = HibernateUtil.executeery(session, hql, new Object[]{quotation.getID()});
////                                List QuotationDetailsList = HibernateUtil.executeQuery(session, hql, new Object[]{quotation.getID(),jobj1.get("productid")});
////                                if (QuotationDetailsList.size() > 0) {
////                                    Iterator QuotationDetailsListitr = QuotationDetailsList.iterator();
////                                    if (QuotationDetailsListitr.hasNext()) {
////                                        quotationDetail = null;
////                                        quotationDetail = (QuotationDetail) QuotationDetailsListitr.next();
////                                    }
////                                }
//
//                                    quotationDetail.setQuotation(quotation);
////                }
//                                    if (jobj1.has("srno")) {
//                                        quotationDetail.setSrno(i);
//                                    }
//                                    if (jobj1.has("unitprice")) {
//                                        KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, Double.parseDouble(jobj1.get("unitprice").toString()), currency.getCurrencyID(), creationDateDt, 0);
//                                        quotationDetail.setRate((Double) bAmt.getEntityList().get(0));
//                                    }
//                                    if (jobj1.has("quantity") && jobj1.get("quantity") != null) {
//                                        quotationDetail.setQuantity(Double.parseDouble(jobj1.get("quantity").toString()));
//                                    }
//                                    if (jobj1.has("remark")) {
//                                        quotationDetail.setRemark((String) jobj1.get("remark"));
//                                    }
//                                    if (jobj1.has("description")) {
//                                        quotationDetail.setDescription((String) jobj1.get("description"));
//                                    }
//
//                                    if (jobj1.has("discount")) {
//                                        quotationDetail.setDiscount(Double.parseDouble(jobj1.get("discount").toString()));
//                                    }
//
//                                    JSONObject retObjDetails = new JSONObject();
//
//                                    retObjQuote.append("quotationproducts", retObjDetails);
//                                    quotationDetail.setProduct(product);
//                                    quotationDetail.setCompany(company);
////                    }
//                                    if (jobj1.has("rowtaxid")) {
//                                        Tax rowtax = (jobj1.get("rowtaxid") == null ? null : (Tax) session.get(Tax.class, (String) jobj1.get("rowtaxid")));
//                                        quotationDetail.setTax(rowtax);
//                                    }
//                                    session.saveOrUpdate(quotationDetail);
//                                }
//                            }
//                        }
//                        quotationdata.append("quotationdata", retObjQuote);
//                    } catch (ServiceException ex) {
//                        JSONObject jObj = new JSONObject();
//                        jObj.put("quotationnumber", jobjQuote.get("quotationnumber"));
//                        jObj.put("quotationid", jobjQuote.get("quotationid"));
//                        jArr.put(jObj);
//                    }
//                }
//            }
//
////            result = getMessage(1, 5);
//            String temp = "m" + String.format("%02d", 5);
//            result = "{\"success\": true, \"infocode\": \"" + temp + "\", \"unchanged\":" + jArr.toString() + "}";
//        } catch (Exception e) {
////            result = "{\"success\":true, \"successmsg\": \"Following error occured while creating user : \""+e.getMessage()+"}";
//            throw ServiceException.FAILURE(e.getMessage(), e);
//        }
//        return result;
//    }

    public String saveTax(HttpServletRequest request, HttpServletResponse response) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {
        String result = "{\"success\":false}";
        String alreadyexist = " But Tax(s) ";
        String added = "Tax(s) ";
        boolean duplicate = false;
        String taxname1 = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String s1 = request.getParameter("data");
            JSONObject jobj2 = new JSONObject(s1);
            JSONArray jobj3 = jobj2.getJSONArray("taxdetials");
            String companyID = jobj2.optString("companyid", "");
            KwlReturnObject taxResult = accTaxObj.getAllTaxOfCompany(companyID);
            List<Tax> list = taxResult.getEntityList();
            for (int i = 0; i < jobj3.length(); i++) {
                JSONObject jobj = jobj3.getJSONObject(i);
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
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
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
            alreadyexist += " are already exist on ERP side. If You Want to Sync then please update these taxes and Sync.";
        }
        result = "{\"success\":true, 'msg':'" + added + alreadyexist + "' ,'syncaccounting' : true,\"companyexist\":true}";;
        return URLEncoder.encode(result, "UTF-8");
    }

    public String getAccProduct(HttpServletRequest request) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        JSONObject obj = new JSONObject();
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid = jobj.getString("companyid");  
            List ll = accCompanyPreferencesObj.isCompanyExistWithCompanyID(companyid);
            int companyCount = ll.size();
            if (companyCount == 0) {
                return "{\"success\": false, \"msg\": \"Company does not exist.\"}";
            }
            obj = getProductTypes(request, obj);
            obj = getProducts(request, companyid, obj);//,start,limit);
            String encodeobj = encodeStringData(obj);
            r = "{\"valid\": true, \"success\": true, \"data\":" + encodeobj + "}";
        } catch (Exception e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public static String encodeStringData(Object obj) {
        String encodeobj = obj.toString();
        String finaldata = "";
        try {
            finaldata = encodeobj.replaceAll("%", "%25");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return finaldata;
    }

    public JSONObject getCustomerInvoicesReportData(HttpServletRequest request, JSONObject obj, String companyid, Date startDate, Date endDate, String projectid, String currencyid) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        SimpleDateFormat sdf = (SimpleDateFormat) authHandler.getGlobalDateFormat();

        try {
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyResult.getEntityList().get(0);
            String gcurrencyid = company.getCurrency().getCurrencyID();

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", gcurrencyid);
            endDate.setHours(24);
            endDate.setMinutes(0);
            String searchString = getSearchColumns(companyid, projectid);

            HashMap<String, Object> reqParams = new HashMap<>();
            reqParams.put("companyid", companyid);
            reqParams.put("startdate", startDate);
            reqParams.put("enddate", endDate);
            reqParams.put("searchstring", searchString);
            KwlReturnObject invResult = accInvoiceDAOobj.getInvoicesWithSearchColumn(reqParams);
            List list = invResult.getEntityList();

            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();

            while (itr.hasNext()) {
                Double amount = 0.0;
                String invoicenum = "";
                String jenum = "";
                String date = "";
                String invid = itr.next().toString();

                KwlReturnObject invoiceResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                Invoice invoice = (Invoice) invoiceResult.getEntityList().get(0);
                JournalEntryDetail d = invoice.getCustomerEntry();
                amount = d.getAmount();
//                KwlReturnObject bAmt = getOneCurrencyToOther(requestParams, amount, invoice.getCurrency().getCurrencyID(), currencyid, invoice.getJournalEntry().getEntryDate(), invoice.getExternalCurrencyRate());
                KwlReturnObject bAmt = getOneCurrencyToOther(requestParams, amount, invoice.getCurrency().getCurrencyID(), currencyid, invoice.getCreationDate(), invoice.getExternalCurrencyRate());
                amount = (Double) bAmt.getEntityList().get(0);
                amount = authHandler.round(amount, companyid);

                invoicenum = invoice.getInvoiceNumber();
                jenum = invoice.getJournalEntry().getEntryNumber();
//                date = sdf.format(invoice.getJournalEntry().getEntryDate());
                date = sdf.format(invoice.getCreationDate());

                JSONObject jobj = new JSONObject();
                jobj.put("invoiceno", invoicenum);
                jobj.put("cost", amount);
                jobj.put("jenum", jenum);
                jobj.put("date", date);
                jArr.put(jobj);

            }
            obj.put("invoicedata", jArr);
            obj.put("count", list.size());
        } catch (ServiceException | JSONException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return obj;
    }

    public String getCustomerInvoicesReport(HttpServletRequest request) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        JSONObject obj = new JSONObject();
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid = jobj.getString("companyid");
            String projectid = jobj.getString("projectid");
            String currencyid = jobj.getString("currencyid");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            Date startDate = authHandler.getDateWithTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            Date endDate = authHandler.getDateWithTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            if (!StringUtil.isNullOrEmpty(jobj.getString("startdate"))) {
                startDate = sdf.parse(jobj.getString("startdate"));
            }
            if (!StringUtil.isNullOrEmpty(jobj.getString("enddate"))) {
                endDate = sdf.parse(jobj.getString("enddate"));
            }
            List ll = accCompanyPreferencesObj.isCompanyExistWithCompanyID(companyid);
            int companyCount = ll.size();
            if (companyCount == 0) {
                return "{\"success\": false, \"msg\": \"Company does not exist.\"}";
            }
            obj = getCustomerInvoicesReportData(request, obj, companyid, startDate, endDate, projectid, currencyid);

            r = "{\"valid\": true, \"success\": true, \"data\":" + obj.toString() + "}";
        } catch (JSONException | SessionExpiredException | ParseException | ServiceException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public JSONObject getVendorInvoicesReportData(HttpServletRequest request, JSONObject obj, String companyid, Date startDate, Date endDate, String projectid, String currencyid) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        SimpleDateFormat sdf = (SimpleDateFormat) authHandler.getGlobalDateFormat();

        try {
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyResult.getEntityList().get(0);
            String gcurrencyid = company.getCurrency().getCurrencyID();

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", gcurrencyid);
            endDate.setHours(24);
            endDate.setMinutes(0);

            String searchString = getSearchColumns(companyid, projectid);

            HashMap<String, Object> reqParams = new HashMap<>();
            reqParams.put("companyid", companyid);
            reqParams.put("startdate", startDate);
            reqParams.put("enddate", endDate);
            reqParams.put("searchstring", searchString);
            KwlReturnObject invResult = accGoodsReceiptobj.getGoodsReceiptsWithSearchColumn(reqParams);
            List list = invResult.getEntityList();

            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();

            while (itr.hasNext()) {
                Double amount = 0.0;
                String invoicenum = "";
                String jenum = "";
                String date = "";
                String invid = itr.next().toString();

                KwlReturnObject grResult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invid);
                GoodsReceipt invoice = (GoodsReceipt) grResult.getEntityList().get(0);
                JournalEntryDetail d = invoice.getVendorEntry();
                amount = d.getAmount();
//                KwlReturnObject bAmt = getOneCurrencyToOther(requestParams, amount, invoice.getCurrency().getCurrencyID(), currencyid, invoice.getJournalEntry().getEntryDate(), invoice.getExternalCurrencyRate());
                KwlReturnObject bAmt = getOneCurrencyToOther(requestParams, amount, invoice.getCurrency().getCurrencyID(), currencyid, invoice.getCreationDate(), invoice.getExternalCurrencyRate());
                amount = (Double) bAmt.getEntityList().get(0);
                amount = authHandler.round(amount, companyid);

                invoicenum = invoice.getGoodsReceiptNumber();
                jenum = invoice.getJournalEntry().getEntryNumber();
//                date = sdf.format(invoice.getJournalEntry().getEntryDate());
                date = sdf.format(invoice.getCreationDate());

                JSONObject jobj = new JSONObject();
                jobj.put("invoiceno", invoicenum);
                jobj.put("cost", amount);
                jobj.put("jenum", jenum);
                jobj.put("date", date);
                jArr.put(jobj);

            }
            obj.put("invoicedata", jArr);
            obj.put("count", list.size());

        } catch (ServiceException | JSONException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return obj;
    }

    public String getVendorInvoicesReport(HttpServletRequest request) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        JSONObject obj = new JSONObject();
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid = jobj.getString("companyid");
            String projectid = jobj.getString("projectid");
            String currencyid = jobj.getString("currencyid");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            Date startDate = authHandler.getDateWithTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            Date endDate = authHandler.getDateWithTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            if (!StringUtil.isNullOrEmpty(jobj.getString("startdate"))) {
                startDate = sdf.parse(jobj.getString("startdate"));
            }
            if (!StringUtil.isNullOrEmpty(jobj.getString("enddate"))) {
                endDate = sdf.parse(jobj.getString("enddate"));
            }
            List ll = accCompanyPreferencesObj.isCompanyExistWithCompanyID(companyid);
            int companyCount = ll.size();
            if (companyCount == 0) {
                return "{\"success\": false, \"msg\": \"Company does not exist.\"}";
            }
            obj = getVendorInvoicesReportData(request, obj, companyid, startDate, endDate, projectid, currencyid);

            r = "{\"valid\": true, \"success\": true, \"data\":" + obj.toString() + "}";
        } catch (Exception e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public String getCustomersWithPart(HttpServletRequest request) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        JSONObject obj = new JSONObject();
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid = jobj.getString("companyid");
            String partno = jobj.getString("partno");
            List ll = accCompanyPreferencesObj.isCompanyExistWithCompanyID(companyid);
            int companyCount = ll.size();
            if (companyCount == 0) {
                return "{\"success\": false, \"msg\": \"Company does not exist.\"}";
            }
            obj = getCustomersWithPart(companyid, partno, obj);
            r = "{\"valid\": true, \"success\": true, \"data\":" + obj.toString() + "}";
        } catch (JSONException | ServiceException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public String getSearchColumns(String companyid, String projectid) {

        String searchString = "";
        try {
            KwlReturnObject returnObject = accountingHandlerDAOobj.getFieldParamsForProject(companyid, projectid);
            List list = returnObject.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] oj = (Object[]) itr.next();
                String columnno = (oj[0]).toString();
                String id = (oj[1]).toString();

                KwlReturnObject cmbReturnObject = accountingHandlerDAOobj.getFieldComboDataForProject(id, projectid);
                List listCmbData = cmbReturnObject.getEntityList();
                Iterator itrCmbData = listCmbData.iterator();
                //If project id is present that searchfield is added
                if (itrCmbData.hasNext()) {
                    String comboid = (String) itrCmbData.next();

                    searchString += "col" + columnno + " = '" + comboid + "' or ";
                }
            }
            if (searchString.length() > 0) {
                searchString = searchString.substring(0, searchString.lastIndexOf("or"));
                searchString = "and (" + searchString + ")";
            }

        } catch (ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return searchString;
    }

    public String getSearchColumnsForTask(String companyid, String projectid, String taskid) {

        String searchString = "";
        try {
            KwlReturnObject returnObject = accountingHandlerDAOobj.getFieldParamsForTask(companyid);
            List list = returnObject.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] oj = (Object[]) itr.next();
                String columnno = (oj[0]).toString();
                String id = (oj[1]).toString();

                //Checked if provided isfortask is present for combo data
                KwlReturnObject cmbReturnObject = accountingHandlerDAOobj.getFieldComboDataForTask(id, projectid, taskid);
                List listCmbData = cmbReturnObject.getEntityList();
                Iterator itrCmbData = listCmbData.iterator();
                //If Task Id is present that searchfield is added
                if (itrCmbData.hasNext()) {
                    String comboid = (String) itrCmbData.next();
                    searchString += "col" + columnno + " = '" + comboid + "' or ";
                }
            }
            if (searchString.length() > 0) {
                searchString = searchString.substring(0, searchString.lastIndexOf("or"));
                searchString = "and (" + searchString + ")";
            }

        } catch (ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return searchString;
    }

    public JSONObject getCustomerInvoicesRevenueData(HttpServletRequest request, JSONObject obj, String companyid, Date startDate, Date endDate, String projectid, String currencyid) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";


        try {

            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyResult.getEntityList().get(0);
            String gcurrencyid = company.getCurrency().getCurrencyID();

            String costCategoryCustomFieldId = "";
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", gcurrencyid);

            SimpleDateFormat sdf = (SimpleDateFormat) authHandler.getGlobalDateFormat();
            endDate.setHours(24);
            endDate.setMinutes(0);

            HashMap<String, String> customFieldMap = new HashMap<>();
            HashMap<String, String> customDateFieldMap = new HashMap<>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Invoice_ModuleId, 0));
            HashMap<String, String> replaceFieldMap = new HashMap<>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            replaceFieldMap = new HashMap<>();
            JSONArray salesCustomFieldJsonArray = new JSONArray();

            String searchString = getSearchColumns(companyid, projectid);

            HashMap<String, Object> reqParams = new HashMap<>();
            reqParams.put("companyid", companyid);
            reqParams.put("startdate", startDate);
            reqParams.put("enddate", endDate);
            reqParams.put("searchstring", searchString);
            KwlReturnObject invResult = accInvoiceDAOobj.getInvoicesWithSearchColumn(reqParams);
            List list = invResult.getEntityList();

            Iterator itr = list.iterator();
            Double totalAmount = 0.0;
            while (itr.hasNext()) {
                Double amount = 0.0;
                JSONObject costCategoryFieldJsonObj = new JSONObject();

                String invid = itr.next().toString();
                KwlReturnObject invoiceResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                Invoice invoice = (Invoice) invoiceResult.getEntityList().get(0);
                JournalEntryDetail d = invoice.getCustomerEntry();
                amount = d.getAmount();
//                KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(), invoice.getJournalEntry().getEntryDate(), invoice.getExternalCurrencyRate());
                KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(), invoice.getCreationDate(), invoice.getExternalCurrencyRate());
                amount = (Double) bAmt.getEntityList().get(0);
                amount = authHandler.round(amount, companyid);

                String jeId = invoice.getJournalEntry().getID();

                Map<String, Object> variableMap = new HashMap<>();
                KwlReturnObject jeCustomDataResult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
                AccJECustomData jeDetailCustom = (AccJECustomData) jeCustomDataResult.getEntityList().get(0);
                replaceFieldMap = new HashMap<>();
                if (jeDetailCustom != null) {
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        String coldata = varEntry.getValue().toString();
                        if (customFieldMap.containsKey(varEntry.getKey())) {
                            KwlReturnObject fieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                            FieldComboData fieldComboData = (FieldComboData) fieldComboDataResult.getEntityList().get(0);
                            if (fieldComboData != null && varEntry.getKey().equalsIgnoreCase("Custom_Cost Category")) {
                                costCategoryCustomFieldId = fieldComboData.getField().getId();
                                KwlReturnObject bAmtInRequiredCurrency = getBaseToCurrencyAmount(requestParams, amount, currencyid, new Date(), 0);
                                double amountInRequiredCurrency = (Double) bAmtInRequiredCurrency.getEntityList().get(0);
                                amountInRequiredCurrency = authHandler.round(amountInRequiredCurrency, companyid);
                                getDimensionJSONForPM(costCategoryFieldJsonObj, fieldComboData, amountInRequiredCurrency);
                                salesCustomFieldJsonArray.put(costCategoryFieldJsonObj);
                            }
                        }
                    }
                }
                totalAmount += amount;
            }

            KwlReturnObject bAmt = getBaseToCurrencyAmount(requestParams, totalAmount, currencyid, new Date(), 0);
            totalAmount = (Double) bAmt.getEntityList().get(0);
            totalAmount = authHandler.round(totalAmount, companyid);

            HashMap<String, Object> params = new HashMap<>();
            params.put("companyid", companyid);
            params.put("fieldname", "Custom_Cost Category");
            params.put("moduleid", Constants.Acc_Invoice_ModuleId);
            KwlReturnObject fcdResult = accMasterItemsDAOobj.getFieldComboDataByFieldName(params);
            List fieldComboDataList = fcdResult.getEntityList();

            JSONArray newJArray = new JSONArray();
            Set<String> fieldComboDataInInvoicesSET = new HashSet<String>();
            for (int i = 0; i < salesCustomFieldJsonArray.length(); i++) {
                if (salesCustomFieldJsonArray.getJSONObject(i) != null) {
                    String fieldComboDataId = salesCustomFieldJsonArray.getJSONObject(i).optString("id", "");
                    if (!StringUtil.isNullOrEmpty(fieldComboDataId)) {
                        fieldComboDataInInvoicesSET.add(fieldComboDataId);
                    }
                }
            }

            Iterator it = fieldComboDataList.iterator();
            while (it.hasNext()) {
                JSONObject jobj = new JSONObject();
                String id = it.next().toString();
                if (!fieldComboDataInInvoicesSET.contains(id)) {
                    KwlReturnObject fieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), id);
                    FieldComboData fieldComboData = (FieldComboData) fieldComboDataResult.getEntityList().get(0);
                    getDimensionJSONForPM(jobj, fieldComboData, 0.0);
                    newJArray.put(jobj);
                }
            }

            for (int i = 0; i < newJArray.length(); i++) {
                JSONObject jSONObject = newJArray.getJSONObject(i);
                salesCustomFieldJsonArray.put(jSONObject);
            }

            salesCustomFieldJsonArray = getHierarachicalJsonArray(request, salesCustomFieldJsonArray, costCategoryCustomFieldId);

            obj.put("salesrevenue", totalAmount);
            obj.put("salesCustomFieldData", salesCustomFieldJsonArray);
        } catch (Exception e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return obj;
    }

    public JSONObject getCustomerInvoicesRevenueDataForTask(HttpServletRequest request, JSONObject obj, String companyid, String projectid, String currencyid, String taskid) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        List list = Collections.EMPTY_LIST;

        try {
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyResult.getEntityList().get(0);
            String gcurrencyid = company.getCurrency().getCurrencyID();

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", gcurrencyid);

            String searchString = getSearchColumnsForTask(companyid, projectid, taskid);
            HashMap<String, Object> reqParams = new HashMap<>();
            reqParams.put("companyid", companyid);
            reqParams.put("searchstring", searchString);
            KwlReturnObject invResult = accInvoiceDAOobj.getInvoicesWithSearchColumn(reqParams);
            list = invResult.getEntityList();

            Iterator itr = list.iterator();
            Double totalAmount = 0.0;

            while (itr.hasNext()) {
                Double amount = 0.0;
                Object[] oj = (Object[]) itr.next();
                String invid = oj[0].toString();
                KwlReturnObject invoiceResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                Invoice invoice = (Invoice) invoiceResult.getEntityList().get(0);
                JournalEntryDetail d = invoice.getCustomerEntry();
                amount = d.getAmount();
//                KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(), invoice.getJournalEntry().getEntryDate(), invoice.getExternalCurrencyRate());
                KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(), invoice.getCreationDate(), invoice.getExternalCurrencyRate());
                amount = (Double) bAmt.getEntityList().get(0);
                amount = authHandler.round(amount, companyid);
                totalAmount += amount;
            }

            KwlReturnObject bAmt = getBaseToCurrencyAmount(requestParams, totalAmount, currencyid, new Date(), 0);
            totalAmount = (Double) bAmt.getEntityList().get(0);
            totalAmount = authHandler.round(totalAmount, companyid);

            obj.put("projectid", projectid);
            obj.put("taskid", taskid);
            obj.put("salesrevenue", totalAmount);
        } catch (ServiceException | JSONException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return obj;
    }

    public JSONArray getContractProductList(HttpServletRequest request, String companyid, String contractid) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        List list = Collections.EMPTY_LIST;
        JSONArray returnArray = new JSONArray();

        try {
            KwlReturnObject result = accSalesOrderDAOobj.getContractProductList(contractid, companyid);
            list = result.getEntityList();
            Iterator itr = list.iterator();

            while (itr.hasNext()) {
                Object[] oj = (Object[]) itr.next();
                JSONObject obj = new JSONObject();
                KwlReturnObject productResult = accountingHandlerDAOobj.getObject(Product.class.getName(), oj[1].toString());
                Product product = (Product) productResult.getEntityList().get(0);

                obj.put("productid", oj[1].toString());
                obj.put("productname", oj[0].toString());
                obj.put("quantity", oj[2].toString());
                obj.put("isSerialNumberAvailable", product.isIsSerialForProduct());
                obj.put("serials", accSalesOrderDAOobj.getBatchSerialByProductID(oj[1].toString(), contractid));
                obj.put("isAsset", oj[3].toString());
                returnArray.put(obj);
            }
        } catch (ServiceException | JSONException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return returnArray;
    }

    public JSONArray getHierarachicalJsonArray(HttpServletRequest request, JSONArray salesCustomFieldJsonArray, String costCategoryCustomFieldId) {
        JSONArray returnArray = new JSONArray();

        try {
            HashMap<String, Object> filterRequestParams = new HashMap<>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("field.id");
            filter_params.add(costCategoryCustomFieldId);
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
//            KwlReturnObject result = getMasterItemsForCustomHire(session, filterRequestParams);
            KwlReturnObject result = accMasterItemsDAOobj.getMasterItemsForCustomHire(filterRequestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();

            JSONArray jArr = new JSONArray();

            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                FieldComboData fieldComboData = (FieldComboData) row[0];
                String fieldComboDataId = fieldComboData.getId();
                double categoryCost = 0d;
                for (int i = 0; i < salesCustomFieldJsonArray.length(); i++) {
                    JSONObject jobj = salesCustomFieldJsonArray.getJSONObject(i);
                    if (fieldComboDataId.equalsIgnoreCase(jobj.optString("id", ""))) {
                        categoryCost += jobj.optDouble("categoryCost", 0);
                    }
                }
                JSONObject obj = new JSONObject();

                obj.put("id", fieldComboData.getId());
                obj.put("name", fieldComboData.getValue());
                FieldComboData parentItem = (FieldComboData) row[3];
                if (parentItem != null) {
                    obj.put("parentid", parentItem.getId());
                    obj.put("parentname", parentItem.getValue());
                }
                obj.put("level", row[1]);
                obj.put("leaf", row[2]);
                obj.put("categoryCost", categoryCost);
                jArr.put(obj);

            }
            returnArray = jArr;

        } catch (ServiceException ex) {
            returnArray = salesCustomFieldJsonArray;
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            returnArray = salesCustomFieldJsonArray;
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return returnArray;
    }

    public boolean getChildGroups(FieldComboData fieldComboData, List resultlist, int level) {
        boolean leaf = true;
        Set<FieldComboData> childrenSet = fieldComboData.getChildren();

        TreeSet<FieldComboData> sortedChildrenSet = new TreeSet<FieldComboData>(childrenSet);

        Iterator<FieldComboData> itr = sortedChildrenSet.iterator();
        level++;
        while (itr.hasNext()) {
            Object listObj = itr.next();
            FieldComboData child = (FieldComboData) listObj;
            leaf = false;

            Object tmplist[] = new Object[4]; //[0:List, 1:level, 2:leaf, 3:parent group]
            tmplist[0] = listObj;
            tmplist[1] = level;
            resultlist.add(tmplist);
            tmplist[2] = getChildGroups(child, resultlist, level);
            tmplist[3] = fieldComboData;//parent group
        }
        return leaf;
    }

    public JSONObject getVendorInvoicesRevenueData(HttpServletRequest request, JSONObject obj, String companyid, Date startDate, Date endDate, String projectid, String currencyid) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";

        try {
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyResult.getEntityList().get(0);
            String gcurrencyid = company.getCurrency().getCurrencyID();

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", gcurrencyid);

            endDate.setHours(24);
            endDate.setMinutes(0);

            String costCategoryCustomFieldId = "";
            HashMap<String, String> customFieldMap = new HashMap<>();
            HashMap<String, String> customDateFieldMap = new HashMap<>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Vendor_Invoice_ModuleId, 0));
//            fieldrequestParams.put("Session", session);
            HashMap<String, String> replaceFieldMap = new HashMap<>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            replaceFieldMap = new HashMap<>();
            JSONArray purchaseCustomFieldJsonArray = new JSONArray();

            String searchString = getSearchColumns(companyid, projectid);
            HashMap<String, Object> reqParams = new HashMap<>();
            reqParams.put("companyid", companyid);
            reqParams.put("startdate", startDate);
            reqParams.put("enddate", endDate);
            reqParams.put("searchstring", searchString);
            KwlReturnObject invResult = accGoodsReceiptobj.getGoodsReceiptsWithSearchColumn(reqParams);
            List list = invResult.getEntityList();

            Iterator itr = list.iterator();
            Double totalAmount = 0.0;
            while (itr.hasNext()) {
                Double amount = 0.0;
                JSONObject costCategoryFieldJsonObj = new JSONObject();
                String invid = itr.next().toString();
                KwlReturnObject grResult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invid);
                GoodsReceipt invoice = (GoodsReceipt) grResult.getEntityList().get(0);
                JournalEntryDetail d = invoice.getVendorEntry();
                amount = d.getAmount();

//                KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(), invoice.getJournalEntry().getEntryDate(), invoice.getExternalCurrencyRate());
                KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(), invoice.getCreationDate(), invoice.getExternalCurrencyRate());
                amount = (Double) bAmt.getEntityList().get(0);
                amount = authHandler.round(amount, companyid);
                String jeId = invoice.getJournalEntry().getID();

                Map<String, Object> variableMap = new HashMap<String, Object>();
                KwlReturnObject accJECustomDataResult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
                AccJECustomData jeDetailCustom = (AccJECustomData) accJECustomDataResult.getEntityList().get(0);
                replaceFieldMap = new HashMap<>();
                if (jeDetailCustom != null) {
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        String coldata = varEntry.getValue().toString();
                        if (customFieldMap.containsKey(varEntry.getKey())) {
                            KwlReturnObject fieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                            FieldComboData fieldComboData = (FieldComboData) fieldComboDataResult.getEntityList().get(0);
                            if (fieldComboData != null && varEntry.getKey().equalsIgnoreCase("Custom_Cost Category")) {
                                costCategoryCustomFieldId = fieldComboData.getField().getId();
                                KwlReturnObject bAmtInRequiredCurrency = getBaseToCurrencyAmount(requestParams, amount, currencyid, new Date(), 0);
                                double amountInRequiredCurrency = (Double) bAmtInRequiredCurrency.getEntityList().get(0);
                                amountInRequiredCurrency = authHandler.round(amountInRequiredCurrency, companyid);
                                getDimensionJSONForPM(costCategoryFieldJsonObj, fieldComboData, amountInRequiredCurrency);
                                purchaseCustomFieldJsonArray.put(costCategoryFieldJsonObj);
                            }
                        }
                    }
                }
                totalAmount += amount;
            }

            KwlReturnObject bAmt = getBaseToCurrencyAmount(requestParams, totalAmount, currencyid, new Date(), 0);
            totalAmount = (Double) bAmt.getEntityList().get(0);
            totalAmount = authHandler.round(totalAmount, companyid);

            HashMap<String, Object> params = new HashMap<>();
            params.put("companyid", companyid);
            params.put("fieldname", "Custom_Cost Category");
            params.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
            KwlReturnObject fcdResult = accMasterItemsDAOobj.getFieldComboDataByFieldName(params);
            List fieldComboDataList = fcdResult.getEntityList();

            JSONArray newJArray = new JSONArray();

            Set<String> fieldComboDataInInvoicesSET = new HashSet<>();

            for (int i = 0; i < purchaseCustomFieldJsonArray.length(); i++) {
                if (purchaseCustomFieldJsonArray.getJSONObject(i) != null) {
                    String fieldComboDataId = purchaseCustomFieldJsonArray.getJSONObject(i).optString("id", "");
                    if (!StringUtil.isNullOrEmpty(fieldComboDataId)) {
                        fieldComboDataInInvoicesSET.add(fieldComboDataId);
                    }
                }
            }

            Iterator it = fieldComboDataList.iterator();
            while (it.hasNext()) {
                JSONObject jobj = new JSONObject();
                String id = it.next().toString();
                if (!fieldComboDataInInvoicesSET.contains(id)) {
                    KwlReturnObject fieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), id);
                    FieldComboData fieldComboData = (FieldComboData) fieldComboDataResult.getEntityList().get(0);
                    getDimensionJSONForPM(jobj, fieldComboData, 0.0);
                    newJArray.put(jobj);
                }
            }

            for (int i = 0; i < newJArray.length(); i++) {
                JSONObject jSONObject = newJArray.getJSONObject(i);
                purchaseCustomFieldJsonArray.put(jSONObject);
            }

            purchaseCustomFieldJsonArray = getHierarachicalJsonArray(request, purchaseCustomFieldJsonArray, costCategoryCustomFieldId);

            obj.put("purchaserevenue", totalAmount);
            obj.put("purchaseCustomFieldData", purchaseCustomFieldJsonArray);
        } catch (Exception e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return obj;
    }

    public void getDimensionJSONForPM(JSONObject costCategoryFieldJsonObj, FieldComboData fieldComboData, double amount) {
        try {
            costCategoryFieldJsonObj.put("id", fieldComboData.getId());
            costCategoryFieldJsonObj.put("fieldId", fieldComboData.getField().getId());
            costCategoryFieldJsonObj.put("costCategoryName", fieldComboData.getValue());
            costCategoryFieldJsonObj.put("categoryCost", amount);

            boolean leaf = false;
            String parentId = "";
            String parentName = "";
            Integer level = new Integer(0);
            HashMap<String, Integer> levelMap = new HashMap<String, Integer>();
            levelMap.put("level", level);

            if (fieldComboData.getParent() != null) {
                parentId = fieldComboData.getParent().getId();
                parentName = fieldComboData.getParent().getValue();
                costCategoryFieldJsonObj.put("parentid", parentId);
                costCategoryFieldJsonObj.put("parentname", parentName);
                getLevel(fieldComboData.getParent(), levelMap);
            }

            if (fieldComboData.getChildren().isEmpty()) {
                leaf = true;
            }

            costCategoryFieldJsonObj.put("leaf", leaf);
            level = levelMap.get("level");
            costCategoryFieldJsonObj.put("level", level);

        } catch (JSONException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void getLevel(FieldComboData fieldComboData, HashMap<String, Integer> levelMap) {
        int level = levelMap.get("level");
        level++;
        levelMap.put("level", level);
        if (fieldComboData.getParent() != null) {
            getLevel(fieldComboData.getParent(), levelMap);

        }
    }

    public String getCashAndPurchaseRevenue(HttpServletRequest request) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        JSONObject obj = new JSONObject();
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid = jobj.getString("companyid");
            String projectid = jobj.getString("projectid");
            String currencyid = jobj.getString("currencyid");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            Date startDate = sdf.parse(jobj.getString("startdate"));
            Date endDate = sdf.parse(jobj.getString("enddate"));
            List ll = accCompanyPreferencesObj.isCompanyExistWithCompanyID(companyid);
            int companyCount = ll.size();
            if (companyCount == 0) {
                return "{\"success\": false, \"msg\": \"Company does not exist.\"}";
            }
            obj = getVendorInvoicesRevenueData(request, obj, companyid, startDate, endDate, projectid, currencyid);
            obj = getCustomerInvoicesRevenueData(request, obj, companyid, startDate, endDate, projectid, currencyid);

            r = "{\"valid\": true, \"success\": true, \"data\":" + obj.toString() + "}";
        } catch (JSONException | ParseException | ServiceException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public String getContractProductList(HttpServletRequest request) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        JSONArray jarr = new JSONArray();
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid = jobj.getString("companyid");
            String contractid = jobj.getString("contractid");
            List ll = accCompanyPreferencesObj.isCompanyExistWithCompanyID(companyid);
            int companyCount = ll.size();
            if (companyCount == 0) {
                return "{\"success\": false, \"msg\": \"Company does not exist.\"}";
            }

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyid);
            requestParams.put("contractid", contractid);
            KwlReturnObject contractResult = accSalesOrderDAOobj.getContractDetails(requestParams);
            int contractCount = contractResult.getRecordTotalCount();
            if (contractCount == 0) {
                return "{\"success\": false, \"msg\": \"Contract does not exist.\"}";
            }
            jarr = getContractProductList(request, companyid, contractid);
            r = "{\"valid\": true, \"success\": true, \"data\":" + jarr.toString() + "}";
        } catch (JSONException | ServiceException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While getting Contract product", e);
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public String getCashRevenueTaskPM(HttpServletRequest request) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        JSONArray jarr = new JSONArray();
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid = jobj.getString("companyid");
            String projectid = jobj.getString("projectid");
            String currencyid = jobj.getString("currencyid");
            String taskids = jobj.getString("taskid");
            String[] taskid = taskids.split(",");
            List ll = accCompanyPreferencesObj.isCompanyExistWithCompanyID(companyid);
            int companyCount = ll.size();
            if (companyCount == 0) {
                return "{\"success\": false, \"msg\": \"Company does not exist.\"}";
            }
            for (String task : taskid) {
                JSONObject obj = new JSONObject();
                obj = getCustomerInvoicesRevenueDataForTask(request, obj, companyid, projectid, currencyid, task);
                jarr.put(obj);
            }
            r = "{\"valid\": true, \"success\": true, \"data\":" + jarr.toString() + "}";
        } catch (JSONException | ServiceException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public JSONObject getCustomersWithPart(String companyid, String partno, JSONObject obj) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        try {

            if (StringUtil.isNullOrEmpty(partno)) {
                return obj;
            } else {
                partno = "%" + partno + "%";
            }
            KwlReturnObject custResult = accCustomerDAOobj.getCustomerWithPartNumber(partno, companyid);
            Iterator itr = custResult.getEntityList().iterator();
            JSONArray jArr = new JSONArray();
            ArrayList<String> listA = new ArrayList<>();
            while (itr.hasNext()) {
                DeliveryOrderDetail doDetail = (DeliveryOrderDetail) itr.next();
                JSONObject jobj = new JSONObject();
                String id = doDetail.getDeliveryOrder().getCustomer().getAccount().getCrmaccountid();
                if (!listA.contains(id)) {
                    listA.add(id);
                    jobj.put("name", doDetail.getDeliveryOrder().getCustomer().getName());
                    jobj.put("id", id);
                    jArr.put(jobj);
                }
            }
            obj.put("custdata", jArr);
        } catch (ServiceException | JSONException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return obj;
    }

    public String getCustomersTOCRM(HttpServletRequest request) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        JSONObject obj = new JSONObject();
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid = jobj.getString("companyid");
            String start = jobj.optString("start", "" + 0);
            String limit = jobj.optString("limit", "" + 500);
            List ll = accCompanyPreferencesObj.isCompanyExistWithCompanyID(companyid);
            int companyCount = ll.size();
            if (companyCount == 0) {
                return "{\"success\": false, \"msg\": \"Company does not exist.\"}";
            }
            obj = getCustomersTOCRM(companyid, obj, start, limit);
            r = "{\"valid\": true, \"success\": true, \"data\":" + obj.toString() + "}";
        } catch (JSONException | ServiceException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public JSONObject getCustomersTOCRM(String companyid, JSONObject jobj, String start, String limit) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyid);
            KwlReturnObject result = accCustomerDAOobj.getCustomer(requestParams);
            int count = result.getRecordTotalCount();

            requestParams.put("start", start);
            requestParams.put("limit", limit);
            result = accCustomerDAOobj.getCustomer(requestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                Customer customer = (Customer) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("customerid", customer.getID());
                obj.put("customername", StringUtil.isNullOrEmpty(customer.getName()) ? "" : customer.getName());
                obj.put("currencyid", (customer.getAccount().getCurrency() == null ? "" : customer.getAccount().getCurrency().getCurrencyID()));
                obj.put("creationdate", (customer.getCreatedOn()) != null ? customer.getCreatedOn().getTime() : null); // CRM Needs date value in Long                                      
                obj.put("crmaccountid", StringUtil.isNullOrEmpty(customer.getCrmaccountid()) ? "" : customer.getCrmaccountid());
                obj.put("sequenceformatid", customer.getSeqformat() != null ? customer.getSeqformat().getID() : "NA");
                obj.put("customercode", customer.getAcccode() != null ? customer.getAcccode() : null);
                obj.put("isAutoGenerated", customer.isAutoGenerated());

                //Below code for customer default address sending
                CustomerAddressDetails billCustAddrDetails = null;
                CustomerAddressDetails shipCustAddrDetails = null;

                HashMap<String, Object> addressParams = new HashMap<>();
                addressParams.put("companyid", companyid);
                addressParams.put("customerid", customer.getID());
                addressParams.put("isDefaultAddress", true);
                KwlReturnObject returnObject = accountingHandlerDAOobj.getCustomerAddressDetails(addressParams);
                list = returnObject.getEntityList();

                Iterator addrItr = list.iterator();
                while (addrItr.hasNext()) {
                    CustomerAddressDetails cad = (CustomerAddressDetails) addrItr.next();
                    if (cad != null) {
                        if (cad.isIsBillingAddress()) {
                            billCustAddrDetails = cad;
                        } else {
                            shipCustAddrDetails = cad;
                        }
                    }
                }
                if (billCustAddrDetails != null) {
                    JSONObject billingAddrObj = AccountingManager.getAddressJsonObject(billCustAddrDetails);
                    obj.put("billingAddress", billingAddrObj);
                }
                if (shipCustAddrDetails != null) {
                    JSONObject shippingAddrObj = AccountingManager.getAddressJsonObject(shipCustAddrDetails);
                    obj.put("shippingAddress", shippingAddrObj);
                }
                jArr.put(obj);
            }
            jobj.put("count", count);
            jobj.put("data", jArr);
        } catch (ServiceException | JSONException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return jobj;
    }

    public JSONObject getProducts(HttpServletRequest request, String companyID, JSONObject jobj) throws ServiceException {
        try {
            ArrayList params = new ArrayList();
            params.add(companyID);
            List<Product> list = accProductObj.getSyncableProduct(companyID);
            int count = list.size();
            JSONArray jArr = new JSONArray();

            //To send product custom data
            Date currentDate = authHandler.getDateOnlyFormat(request).parse(authHandler.getDateOnlyFormat(request).format(new Date()));
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            requestParams.put(Constants.filter_values, Arrays.asList(companyID, Constants.Acc_Product_Master_ModuleId));
            requestParams.put("isActivated", 1);
            requestParams.put("order_by", Arrays.asList("sequence"));
            requestParams.put("order_type", Arrays.asList("asc"));
            KwlReturnObject result = fieldManagerDAOobj.getFieldParams(requestParams);
            List lst = result.getEntityList();

            for (Product product : list) {
                JSONObject obj = new JSONObject();
                obj.put("id", product.getID());
                obj.put("productname", product.getName());

                //======refer ticket ERP-11075 & ERP-11606============
                Date creationDate = new Date(product.getCreatedon());
                Calendar cal = Calendar.getInstance(); // locale-specific
                cal.setTime(creationDate);
                
                //sent GMT date if fails
                try {
                    cal.setTimeZone(TimeZone.getTimeZone("GMT" + sessionHandlerImpl.getTimeZoneDifference(request)));        // need to add Application TimeZone
                } catch (Exception e) {
                    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
                }
                
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long time = cal.getTimeInMillis();
                obj.put("createdon", time);

                obj.put("desc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));//desc
                obj.put("vendor", (product.getVendor() != null ? product.getVendor().getID() : ""));
                obj.put("vendornameid", (product.getVendor() != null ? product.getVendor().getName() : ""));
                obj.put("vendorphoneno", (product.getVendor() != null ? product.getVendor().getContactNumber() : ""));
                obj.put("vendoremail", (product.getVendor() != null ? product.getVendor().getEmail() : ""));
                obj.put("type", (product.getProducttype() != null ? product.getProducttype().getName(): ""));
                obj.put("pid", !StringUtil.isNullOrEmpty(product.getProductid()) ? product.getProductid() : "");
                if (product.getWarrantyperiod() != 0) {
                    obj.put("warrantyperiod", product.getWarrantyperiod());
                }
                if (product.getWarrantyperiodsal() != 0) {
                    obj.put("warrantyperiodsal", product.getWarrantyperiodsal());
                }

                String currencyId = "";
                if (product.getCurrency() != null) {
                    currencyId = product.getCurrency().getCurrencyID();
                }
               
                KwlReturnObject priceResult = accProductObj.getProductPrice(product.getID(), true, null, "", currencyId);
                double purchasePrice = priceResult.getEntityList().get(0) != null ? (Double) priceResult.getEntityList().get(0) : 0;
               
                priceResult = accProductObj.getProductPrice(product.getID(), false, null, "", currencyId);
                double salesPrice = priceResult.getEntityList().get(0) != null ? (Double) priceResult.getEntityList().get(0) : 0;
                obj.put("purchaseprice", purchasePrice);
                obj.put("saleprice", salesPrice);
                
                //====extra fields to be send to map the columns data====
                
                obj.put("uomname", (product.getPackaging()!=null && product.getPackaging().getStockUoM()!=null) ? product.getPackaging().getStockUoM().getNameEmptyforNA() : "");
                obj.put("reorderlevel", product.getReorderLevel());
                obj.put("reorderquantity", product.getReorderQuantity());
                obj.put("leadtime", product.getLeadTimeInDays());
                obj.put("parentid", product.getParent()!=null ? product.getParent().getProductName(): "");
                obj.put("salesaccountname", product.getSalesAccount()!=null ? product.getSalesAccount().getAccountName(): "");
                obj.put("salesretaccountname", product.getSalesReturnAccount()!=null ? product.getSalesReturnAccount().getAccountName(): "");
                obj.put("purchaseaccountname", product.getPurchaseAccount()!=null ? product.getPurchaseAccount().getAccountName(): "");
                obj.put("purchaseretaccountname", product.getPurchaseReturnAccount()!=null ? product.getPurchaseReturnAccount().getAccountName(): "");
                obj.put("quantity", product.getAvailableQuantity());
                obj.put("locationName", product.getLocation()!=null ? product.getLocation().getName(): "");
                obj.put("warehouseName", product.getWarehouse()!=null ? product.getWarehouse().getName(): "");
                obj.put("currencyName", product.getCurrency()!=null ? product.getCurrency().getCurrencyID() : "");
                obj.put("purchaseuom", product.getPurchaseUOM()!=null ? product.getPurchaseUOM().getNameEmptyforNA(): "");
                obj.put("salesuom", product.getSalesUOM()!=null ? product.getSalesUOM().getNameEmptyforNA(): "");
                obj.put("casinguom", (product.getPackaging()!=null && product.getPackaging().getCasingUoM()!=null) ? product.getPackaging().getCasingUoM().getNameEmptyforNA() : "");
                obj.put("inneruom", (product.getPackaging()!=null && product.getPackaging().getInnerUoM()!=null) ? product.getPackaging().getInnerUoM().getNameEmptyforNA() : "");
                obj.put("casinguom_value", product.getPackaging()!=null ? product.getPackaging().getCasingUomValue() : 0);
                obj.put("inneruom_value", product.getPackaging()!=null ? product.getPackaging().getInnerUomValue() : 0);
                obj.put("stockuom_value", product.getPackaging()!=null ? product.getPackaging().getStockUomValue() : 0);
                obj.put("supplier", product.getSupplier());
                obj.put("coilcraft", product.getCoilcraft());
                obj.put("interplant", product.getInterplant());
                obj.put("wipoffset", product.getWIPOffset());
                obj.put("inventoryoffset", product.getInventoryOffset());
                obj.put("hscode", product.getHSCode());
                obj.put("additionalfreetext", product.getAdditionalFreeText());
                obj.put("itemcolor", product.getItemColor());
                obj.put("alternateproduct", product.getAlternateProduct());
                obj.put("purchasemfg", product.getPurchaseMfg());
                obj.put("catalogno", product.getCatalogNo());
                obj.put("barcode", product.getBarcode());
                obj.put("additionaldesc", product.getAdditionalDesc());
                obj.put("descinforeign", product.getDescInForeign());
                obj.put("licensecode", product.getLicenseCode());
                obj.put("itemgroup", product.getItemGroup());
                obj.put("pricelist", product.getPriceList());
                obj.put("shippingtype", product.getShippingType());
                obj.put("itemsalesvolume", product.getItemSalesVolume());
                obj.put("productweight", product.getProductweight());
                obj.put("productweightperstockuom",  product.getProductWeightPerStockUom());
                obj.put("productweightincludingpakagingperstockuom", product.getProductWeightIncludingPakagingPerStockUom());
                obj.put("productvolumeperstockuom",product.getProductVolumePerStockUom());
                obj.put("productvolumeincludingpakagingperstockuom",product.getProductVolumeIncludingPakagingPerStockUom());
                obj.put("itemsaleswidth", product.getItemSalesWidth());
                obj.put("itemsalesheight", product.getItemSalesHeight());
                obj.put("itemwidth", product.getItemWidth());
                obj.put("itemvolume", product.getItemVolume());
                obj.put("itempurchasewidth", product.getItemPurchaseWidth());
                obj.put("itempurchaselength", product.getItemPurchaseLength());
                obj.put("qaleadtimeindays", product.getQALeadTimeInDays());
                obj.put("reusabilitycount", product.getReusabilityCount());
                obj.put("orderinguom", product.getOrderingUOM()!=null ? product.getOrderingUOM().getNameEmptyforNA(): "");
                obj.put("transferuom", product.getTransferUOM()!=null ? product.getTransferUOM().getNameEmptyforNA(): "");
                obj.put("itempurchaseheight", product.getItemPurchaseHeight());
                obj.put("itempurchasevolume", product.getItemPurchaseVolume());
                obj.put("itemsaleslength", product.getItemSalesLength());
                obj.put("itemlength", product.getItemLength());
                obj.put("itemheight", product.getItemHeight());
                obj.put("asofdate", product.getAsOfDate().getTime());
                
                //=======================================================
                
                Map<String, Object> customParams = new HashMap<String, Object>();
                customParams.put("fieldList", lst);
                customParams.put("companyid", companyID);
                customParams.put("currentDate", currentDate);
                customParams.put("productObj", product);
                
                JSONArray customJobj = getProductCutomDataJson(customParams);
                obj.put("customdata", customJobj);

                jArr.put(obj);
            }
            jobj.put("productdata", jArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    public JSONArray getProductCutomDataJson(Map<String, Object> customParams) {
        JSONArray customData = new JSONArray();
        List fieldList = (List) customParams.get("fieldList");
        String companyID = (String) customParams.get("companyid");
        Date currentDate = (Date) customParams.get("currentDate");
        Product product = (Product) customParams.get("productObj");

        try {
            Iterator itr = fieldList.iterator();
            while (itr.hasNext()) {
                FieldParams tmpcontyp = (FieldParams) itr.next();
                JSONObject customJobj = new JSONObject();
                customJobj.put("fieldname", tmpcontyp.getFieldname());
                customJobj.put("sequence", tmpcontyp.getSequence());

                customJobj.put("isessential", tmpcontyp.getIsessential());
                customJobj.put("maxlength", tmpcontyp.getMaxlength());
                customJobj.put("validationtype", tmpcontyp.getValidationtype());
                customJobj.put("fieldid", tmpcontyp.getId());
                customJobj.put("moduleid", tmpcontyp.getModuleid());
                customJobj.put("modulename", "\"Products & Services\"");
                customJobj.put("fieldtype", tmpcontyp.getFieldtype());
                customJobj.put("iseditable", tmpcontyp.getIseditable());
                customJobj.put("comboid", tmpcontyp.getComboid());
                customJobj.put("comboname", tmpcontyp.getComboname());
                customJobj.put("refcolumn_number", Constants.Custom_Column_Prefix + tmpcontyp.getRefcolnum());
                customJobj.put("column_number", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                customJobj.put("fieldlabel", tmpcontyp.getFieldlabel());

                AccProductCustomData accProductCustomData = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), product.getID());
                if (accProductCustomData != null) {
                    String coldata = accProductCustomData.getCol(tmpcontyp.getColnum());
                    Object fieldValueObject = accAccountCMNObj.getProductCustomFieldValue(tmpcontyp.getId(), accProductCustomData.getProductId(), companyID, currentDate);
                    String latestValue = "";
                    if (fieldValueObject != null) {
                        latestValue = (String) fieldValueObject;
                    }
                    if (!StringUtil.isNullOrEmpty(coldata)) {
                        String value = "";
                        if (latestValue.equalsIgnoreCase(coldata) || StringUtil.isNullOrEmpty(latestValue)) {
                            value = coldata;
                        } else {
                            value = latestValue;
                        }

                        if (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7) {//for combo or multi-select sent display values of selected items
                            String[] array = value.split(",", -1);
                            value = "";
                            for (String id : array) {
                                FieldComboData field = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), id);
                                value += field.getValue() + ", ";
                            }
                            customJobj.put("fieldData", value.substring(0, Math.max(0, value.length() - 2)));
                        } else {
                            customJobj.put("fieldData", value);
                        }
                    }
                }
                customData.put(customJobj);
            }
        } catch (Exception e) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While fetching product custom data", e);
        }

        return customData;
    }

    public JSONObject getProductTypes(HttpServletRequest request, JSONObject obj) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        try {
            HashMap<String, Object> requestParams = new HashMap<>();;
            KwlReturnObject result = accProductObj.getProductTypes(requestParams);
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                Producttype ptype = (Producttype) itr.next();
                JSONObject jobj = new JSONObject();
                jobj.put("id", ptype.getID());
                jobj.put("name", ptype.getName());
                jArr.put(jobj);
            }
            obj.put("typedata", jArr);
        } catch (Exception e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return obj;
    }

    static void updatePreferences(HttpServletRequest request, String currencyid, String dateformatid, String timezoneid, String tzdiff) {
        //To do - need to modify this
        if (currencyid != null) {
            request.getSession().setAttribute("currencyid", currencyid);
        }
        if (timezoneid != null) {
            request.getSession().setAttribute("timezoneid", timezoneid);
            request.getSession().setAttribute("tzdiff", tzdiff);
        }
        if (dateformatid != null) {
            request.getSession().setAttribute("dateformatid", dateformatid);
        }
    }

    private String createCompany(HttpServletRequest request) throws SQLException, ServiceException {
        String result = "{\"success\":false}";
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));

            String companyid = jobj.isNull("companyid") ? "" : jobj.getString("companyid");
            String lname = jobj.isNull("lname") ? "" : jobj.getString("lname");
            String userid = jobj.isNull("userid") ? "" : jobj.getString("userid");
            String subdomain = jobj.isNull("subdomain") ? "" : jobj.getString("subdomain");
            String userid2 = jobj.isNull("username") ? "" : jobj.getString("username");
            String emailid2 = jobj.isNull("emailid") ? "" : jobj.getString("emailid");
            String password = jobj.isNull("password") ? "" : jobj.getString("password");
            String companyname = jobj.isNull("companyname") ? "" : jobj.getString("companyname");
            String fname = jobj.isNull("fname") ? "" : jobj.getString("fname");
            int referralkey = jobj.isNull(Constants.REFERRALKEY) ? 0 : jobj.getInt(Constants.REFERRALKEY);
            String currency = jobj.isNull("currency") ? StorageHandler.getDefaultCurrencyID() : jobj.getString("currency");
            String country = jobj.isNull("country") ? "244" : jobj.getString("country");
            String timezone = jobj.isNull("timezone") ? "23" : jobj.getString("timezone");
            if (StringUtil.isNullOrEmpty(companyname) || StringUtil.isNullOrEmpty(userid2)
                    || StringUtil.isNullOrEmpty(fname) || StringUtil.isNullOrEmpty(emailid2)) {
                return getMessage(2, 1);
            }
            String pwdtext = "";
            if (jobj.isNull("password")) {
                pwdtext = StringUtil.generateNewPassword();
                password = StringUtil.getSHA1(pwdtext);
            }
            if (!(StringUtil.isNullOrEmpty(userid2) || StringUtil.isNullOrEmpty(emailid2))) {
                emailid2 = emailid2.replace(" ", "+");
                result = signupCompany(request, companyid, userid, userid2, password, emailid2, companyname, fname, subdomain, lname, currency, country, timezone, referralkey);
                if (result.equals("success")) {
                    result = getMessage(1, 6);
                } else {
                    if (result.equals("failure")) {
                        result = getMessage(2, 8);
                    }
                }
            }
        } catch (Exception e) {
            result = "{\"success\":false}";
            throw ServiceException.FAILURE("comapanyServlet.createCompany:" + e.getMessage(), e);
        }
        return result;
    }

    public String signupCompany(HttpServletRequest request, String companyid, String userid, String id, String password, String emailid, String companyname,
            String fname, String subdomain, String lname, String currencyid, String countryid, String timezoneid, int referralkey)
            throws ServiceException {
        String result = "failure";

        try {
            Company company = null;
            UserLogin userLogin = null;
            User user = null;
            List<Company> companyList = new ArrayList<>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            HashMap<String, Object> companyRequestParams = new HashMap<>();
            filter_names.add("c.subDomain");
            filter_params.add(subdomain);
            companyRequestParams.put("filter_names", filter_names);
            companyRequestParams.put("filter_values", filter_params);
            KwlReturnObject companyListObj = accCommonTablesDAO.getCompany(companyRequestParams);
            if (companyListObj.getEntityList().size() > 0) {
                companyList = companyListObj.getEntityList();
            }

            Iterator itr11 = companyList.iterator();
            if (itr11.hasNext()) {
                Company oldcompany = (Company) itr11.next();
                HashMap<String, Object> companyHashMap = new HashMap<>();
                companyHashMap.put("companyid", oldcompany.getCompanyID());
                companyHashMap.put("subdomain", "old_" + oldcompany.getSubDomain());
                KwlReturnObject cmprtObj = profileHandlerDAOObj.updateCompany(companyHashMap);
            }
            if (!StringUtil.isNullOrEmpty(userid)) {
                KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
                user = (User) userResult.getEntityList().get(0);
                if (user != null) {
                    return getMessage(2, 7);
                }
            }
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            company = (Company) companyResult.getEntityList().get(0);
            if (company != null) {
                return getMessage(2, 8);
            }

            KwlReturnObject countryResult = accountingHandlerDAOobj.getObject(Country.class.getName(), countryid);
            Country country = (Country) countryResult.getEntityList().get(0);

            KwlReturnObject currencyResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) currencyResult.getEntityList().get(0);

            KwlReturnObject roleListResult = accountingHandlerDAOobj.getObject(Rolelist.class.getName(), Role.COMPANY_ADMIN);
            Rolelist roleList = (Rolelist) roleListResult.getEntityList().get(0);

            company = new Company();
            user = new User();
            userLogin = new UserLogin();
            user.setUserID(userid);

            company.setCompanyID(companyid);
            company.setCreator(user);
            company.setAddress("");
            company.setDeleted(0);
            Date curdate = authHandler.getDateOnlyFormat().parse(authHandler.getConstantDateFormatter(request).format(new Date()));
            company.setCreatedOn(curdate);
            company.setModifiedOn(curdate);
            company.setSubDomain(subdomain);
            company.setCompanyName(companyname);
            company.setCountry(country);
            KwlReturnObject tzResult = accountingHandlerDAOobj.getObject(KWLTimeZone.class.getName(), timezoneid);
            KWLTimeZone tz = (KWLTimeZone) tzResult.getEntityList().get(0);
            company.setTimeZone(tz);
            company.setEmailID(emailid);
            company.setCurrency(currency);
            company.setActivated(true);
            company.setSwitchpref(1);
            company.setStoreinvoiceamountdue(true);
            company.setReferralkey(referralkey);
            userLogin.setUser(user);
            user.setRoleID(Role.COMPANY_ADMIN);

            RoleUserMapping rmapping = new RoleUserMapping();
            rmapping.setRoleId(roleList);
            rmapping.setUserId(user);
            kwlCommonTablesDAOObj.saveObj(rmapping);

            userLogin.setUserName(id);
            userLogin.setPassword(password);
            user.setFirstName(fname);
            user.setTimeZone(tz);
            user.setLastName(lname);
            user.setEmailID(emailid);
            user.setAddress("");

            user.setCompany(company);
            KwlReturnObject dfResult = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), "2");
            KWLDateFormat df = (KWLDateFormat) dfResult.getEntityList().get(0);
            user.setDateFormat(df);//yyyy-mm-dd
            kwlCommonTablesDAOObj.saveObj(company);
            kwlCommonTablesDAOObj.saveObj(user);
            kwlCommonTablesDAOObj.saveObj(userLogin);

            request.setAttribute("currencyid", currencyid);
            setupNewCompany(request, company, user, currencyid);
            result = "success";
        } catch (Exception e) {
            result = "failure";
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return result;
    }

    public void setupNewCompany(HttpServletRequest request, Company company, User companyAdmin, String currencyid) throws SessionExpiredException {
        try {
            String companyid = company.getCompanyID();
            accTermObj.copyTerms(companyid);
            accUomObj.copyUOM(companyid, new HashMap<String, Object>());
            KwlReturnObject kresult = accAccountDAOobj.copyAccounts(companyid, currencyid, null, null, null, null, false);
            HashMap hmAcc = (HashMap) kresult.getEntityList().get(0);
            accPaymentDAOobj.copyPaymentMethods(companyid, hmAcc);
            accMasterItemsDAOobj.copyMasterItems(companyid, hmAcc);
            accCompanyPreferencesObj.setAccountPreferences(companyid, hmAcc, getCurrentDate());
            accCompanyPreferencesObj.saveDefaultSequenceFormat(company);
            accTaxObj.copyTax1099Category(companyid);
        } catch (ServiceException e) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.WARNING, e.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    public Date getCurrentDate() throws ServiceException {
        DateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
        String timezoneid = StorageHandler.getDefaultTimeZoneID();
        KwlReturnObject kresult = accountingHandlerDAOobj.getObject(KWLTimeZone.class.getName(), timezoneid);
        KWLTimeZone tz = (KWLTimeZone) kresult.getEntityList().get(0);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + tz.getDifference()));
        Date curDate = new Date();
        try {
            curDate = sdf.parse(sdf.format(curDate));
        } catch (ParseException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return curDate;
    }

    public String updateCompany(HttpServletRequest request) throws JSONException, ServiceException {
        String result = "{\"success\":false}";
        boolean isSetUpDone = false;
        Map<String, Object> requestParams = new HashMap<String, Object>();
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid = jobj.isNull("companyid") ? "" : jobj.getString("companyid");
            requestParams.put("companyid", companyid);
            KwlReturnObject compAccPrefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) compAccPrefResult.getEntityList().get(0);
            if (companyAccountPreferences != null && companyAccountPreferences.isSetupDone()) {
                isSetUpDone = true;
            }
            String subdomain = jobj.isNull("subdomain") ? "" : jobj.getString("subdomain");
            String companyname = jobj.isNull("companyname") ? "" : jobj.getString("companyname");
            String address = jobj.isNull("address") ? "" : jobj.getString("address");
            String city = jobj.isNull("city") ? "" : jobj.getString("city");
            String state = jobj.isNull("state") ? "" : jobj.getString("state");
            String phone = jobj.isNull("phone") ? "" : jobj.getString("phone");
            String fax = jobj.isNull("fax") ? "" : jobj.getString("fax");
            String zip = jobj.isNull("zip") ? "" : jobj.getString("zip");
            String website = jobj.isNull("website") ? "" : jobj.getString("website");
            String emailid = jobj.isNull("emailid") ? "" : jobj.getString("emailid");
            String currency = jobj.isNull("currency") ? "" : jobj.getString("currency");
            String country = jobj.isNull("country") ? "" : jobj.getString("country");
            String timezone = jobj.isNull("timezone") ? "" : jobj.getString("timezone");
            String image = jobj.isNull("image") ? "" : jobj.getString("image");
            String smtpflow = StringUtil.isNullOrEmpty(jobj.optString("smtpflow")) ? "0" : jobj.optString("smtpflow");
            String smtppassword = jobj.optString("smtppassword");
            String mailserveraddress = jobj.optString("smtppath");
            String mailserverport = jobj.optString("smtppport");

            if (StringUtil.isNullOrEmpty(companyid) || StringUtil.isNullOrEmpty(subdomain) || StringUtil.isNullOrEmpty(currency)
                    || StringUtil.isNullOrEmpty(timezone) || StringUtil.isNullOrEmpty(country) || StringUtil.isNullOrEmpty(companyname)) {
                return getMessage(2, 1);
            }
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyResult.getEntityList().get(0);
            if (company == null) {
                return getMessage(2, 4);
            }

            if (isSetUpDone && !isCompanyCurrencyandCountrySame(request) && !profileHandlerDAOObj.isTransactionCreated(companyid)) {
                /*
                 * Rollback company setup data if setup is already is done
                 */
                profileHandlerDAOObj.deleteCompanySetUpData(requestParams);

                /*
                 * Update default data as per Country and Currency
                 */
                request.setAttribute("currencyid", currency);
                KwlReturnObject kresult = accAccountDAOobj.copyAccounts(companyid, currency, null, null, null, null, false);
                HashMap hmAcc = (HashMap) kresult.getEntityList().get(0);
                accPaymentDAOobj.copyPaymentMethods(companyid, hmAcc);
                accMasterItemsDAOobj.copyMasterItems(companyid,hmAcc);
                kwlCommonTablesDAOObj.evictObject(companyAccountPreferences);
                accCompanyPreferencesObj.setAccountPreferences(companyid, hmAcc, getCurrentDate());
                accTaxObj.copyTax1099Category(companyid);
            }
            List list = accCompanyPreferencesObj.isAnotherCompanyExistWithSameSubDomain(subdomain, company.getCompanyID());
            if (!list.isEmpty()) {
                return getMessage(2, 10);
            }

            HashMap<String, Object> companyHashMap = new HashMap<>();
            companyHashMap.put("companyid", companyid);
            companyHashMap.put("companyname", companyname);
            companyHashMap.put("subdomain", subdomain);

            if (!isCompanyCurrencyandCountrySame(request) && !profileHandlerDAOObj.isTransactionCreated(companyid)) {
                if (!companyAccountPreferences.isCountryChange()) {
                    companyHashMap.put("country", country);
                }
                if (!companyAccountPreferences.isCurrencyChange()) {
                    companyHashMap.put("currency", currency);
                }
            }

            companyHashMap.put("timezone", timezone);
            companyHashMap.put("companylogo", image);
            companyHashMap.put("address", address);
            companyHashMap.put("city", city);

            companyHashMap.put("state", state);
            companyHashMap.put("phone", phone);
            companyHashMap.put("fax", fax);
            companyHashMap.put("zip", zip);
            companyHashMap.put("website", website);
            companyHashMap.put("emailid", emailid);

            Date modifydate = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            companyHashMap.put("modifiedon", modifydate);

            //SMTP Configs
            companyHashMap.put("smtpflow", smtpflow);
            companyHashMap.put("smtppassword", smtppassword);
            companyHashMap.put("mailserveraddress", mailserveraddress);
            companyHashMap.put("mailserverport", mailserverport);

            KwlReturnObject cmprtObj = profileHandlerDAOObj.updateCompany(companyHashMap);

            result = getMessage(1, 11);
        } catch (NullPointerException e) {
            ServiceException.FAILURE("RemoteAPI.updateCompany", e);
        } catch (ParseException pe) {
            ServiceException.FAILURE("RemoteAPI.updateCompany : problem in modification date", pe);
        } catch (SessionExpiredException se) {
            ServiceException.FAILURE("RemoteAPI.updateCompany", se);
        }
        return result;
    }

    public String getUpdates(HttpServletRequest request) throws ServiceException {
        String result = "{\"success\":false}";
        try {
            JSONObject jOutput = new JSONObject();
            JSONObject jData = new JSONObject();
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid = jobj.isNull("companyid") ? "" : jobj.getString("companyid");
            String userid = jobj.isNull("userid") ? "" : jobj.getString("userid");
            int offset = jobj.isNull("offset") ? 0 : jobj.getInt("offset");
            int limit = jobj.isNull("limit") ? 5 : jobj.getInt("limit");
            if (StringUtil.isNullOrEmpty(companyid) || StringUtil.isNullOrEmpty(userid)) {
                return getMessage(2, 1);
            }

            KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) result1.getEntityList().get(0);
            if (company == null) {
                return getMessage(2, 4);
            }

            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
            User user = (User) userResult.getEntityList().get(0);
            if (user == null) {
                return getMessage(2, 6);
            }

            JSONArray jArr = getUpdatesArray(companyid, userid);
            jData.put("head", "<div style='padding:10px 0 10px 0;font-size:13px;font-weight:bold;color:#10559a;border-bottom:solid 1px #EEEEEE;'>Updates</div>");

            jOutput.append("data", jData);
            for (int i = offset; i < offset + limit && i < jArr.length(); i++) {
                JSONObject temp = new JSONObject();
                temp.put("update", jArr.getString(i));
                jOutput.append("data", temp);
            }
            jOutput.append("count", jArr.length());

            result = "{\"valid\": true, \"success\": true, \"data\":" + jOutput.toString() + "}";
        } catch (HibernateException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException je) {
            ServiceException.FAILURE("RemoteAPI.updateCompany", je);
        } finally {
        }
        return result;
    }

    private JSONArray getUpdatesArray(String companyid, String userid) throws ServiceException, JSONException {
        JSONArray jArray = new JSONArray();
        ArrayList temp;

        KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
        User user = (User) userResult.getEntityList().get(0);
        JSONObject perms = getPermissions(user.getUserID());

        temp = getVendorsUpdationInfo(companyid, perms, false);
        for (int i = 0; i < temp.size(); i++) {
            jArray.put(temp.get(i));
        }
        temp = getCustomersUpdationInfo(companyid, perms, false);
        for (int i = 0; i < temp.size(); i++) {
            jArray.put(temp.get(i));
        }
        temp = getProductsBelowROLInfo(companyid, perms, false);
        for (int i = 0; i < temp.size(); i++) {
            jArray.put(temp.get(i));
        }
        ArrayList props = new ArrayList();
        props.add("color=#10559A");
        replaceTag(jArray, "a", "font", props);
        return jArray;
    }

    public ArrayList getProductsBelowROLInfo(String companyID, JSONObject perms, Boolean isDashboard) throws ServiceException {
        ArrayList jArray = new ArrayList();
        try {
            JSONArray jArr = getProducts(null, companyID).getJSONArray("data");
            String link;
            String productID;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject obj = jArr.getJSONObject(i);
                if (obj.getInt("quantity") > obj.getInt("reorderlevel")) {
                    continue;
                }
                link = obj.getString("productname");
                productID = obj.getString("productid");
                if (DashboardHandler.isPermitted(perms, "product", "view")) {
                    link = DashboardHandler.getLink(link, "callProductDetails(\"" + productID + "\")");
                }
                jArray.add(DashboardHandler.getFormatedAlert("The Product " + link + " is below reorder level (Available quantity:" + obj.getInt("quantity") + " " + obj.getString("uomname") + ")", "accountingbase updatemsg-product", isDashboard));
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jArray;
    }

    public JSONObject getProducts(String productid, String companyID) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
            Company company = (Company) result1.getEntityList().get(0);
            String currencyid = company.getCurrency().getCurrencyID();

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("currencyid", currencyid);
            requestParams.put("companyid", companyID);
            KwlReturnObject result = accProductObj.getProductsFoRemoteAPI(requestParams);
            Iterator itr = result.getEntityList().iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Product product = (Product) row[0];
                if (product.getID().equals(productid)) {
                    continue;
                }
                JSONObject obj = new JSONObject();
                obj.put("productid", product.getID());
                obj.put("productname", product.getName());
                obj.put("desc", product.getDescription());
                UnitOfMeasure uom = product.getUnitOfMeasure();
                obj.put("uomid", uom == null ? "" : uom.getID());
                obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
                obj.put("precision", uom == null ? 0 : (Integer) uom.getAllowedPrecision());
                obj.put("leadtime", product.getLeadTimeInDays());
                obj.put("reorderlevel", product.getReorderLevel());
                obj.put("reorderquantity", product.getReorderQuantity());
                obj.put("purchaseaccountid", (product.getPurchaseAccount() != null ? product.getPurchaseAccount().getID() : ""));
                obj.put("salesaccountid", (product.getSalesAccount() != null ? product.getSalesAccount().getID() : ""));
                obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : ""));
                obj.put("salesretaccountid", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getID() : ""));
                obj.put("level", 0);
                obj.put("purchaseprice", row[1]);
                obj.put("saleprice", row[2]);
                obj.put("quantity", (row[3] == null ? 0 : row[3]));
                obj.put("initialquantity", (row[4] == null ? 0 : row[4]));
                obj.put("initialprice", (row[5] == null ? 0 : row[5]));
                jArr.put(obj);
                obj.put("leaf", getChildProducts(product, jArr, 0, productid));
            }
            jobj.put("data", jArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    private boolean getChildProducts(Product product, JSONArray jArr, int level, String productid) throws JSONException, ServiceException {
        boolean leaf = true;
        Iterator<Product> itr = new TreeSet(product.getChildren()).iterator();
        level++;
        String currencyid = product.getCompany().getCurrency().getCurrencyID();
        while (itr.hasNext()) {
            Product child = itr.next();
            if (child.getID().equals(productid) || child.isDeleted()) {
                continue;
            }
            leaf = false;
            JSONObject obj = new JSONObject();
            obj.put("productid", child.getID());
            obj.put("productname", child.getName());
            obj.put("desc", child.getDescription());
            obj.put("uomid", child.getUnitOfMeasure().getID());
            obj.put("uomname", child.getUnitOfMeasure().getNameEmptyforNA());
            obj.put("leadtime", child.getLeadTimeInDays());
            obj.put("reorderlevel", child.getReorderLevel());
            obj.put("reorderquantity", child.getReorderQuantity());
            obj.put("purchaseaccountid", (child.getPurchaseAccount() != null ? child.getPurchaseAccount().getID() : ""));
            obj.put("salesaccountid", (child.getSalesAccount() != null ? child.getSalesAccount().getID() : ""));
            obj.put("purchaseretaccountid", (child.getPurchaseReturnAccount() != null ? child.getPurchaseReturnAccount().getID() : ""));
            obj.put("salesretaccountid", (child.getSalesReturnAccount() != null ? child.getSalesReturnAccount().getID() : ""));
            obj.put("parentid", product.getID());
            obj.put("parentname", product.getName());
            obj.put("level", level);

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("currencyid", currencyid);
            KwlReturnObject result = accProductObj.getChildProductsFoRemoteAPI(requestParams);
            List list = result.getEntityList();

            if (!list.isEmpty()) {
                Object[] row = (Object[]) list.get(0);
                obj.put("purchaseprice", row[0]);
                obj.put("saleprice", row[1]);
                obj.put("quantity", (row[2] == null ? 0 : row[2]));
            }
            jArr.put(obj);
            obj.put("leaf", getChildProducts(child, jArr, level, productid));
        }
        return leaf;
    }

    public ArrayList getVendorsUpdationInfo(String companyID, JSONObject perms, Boolean isDashboard) throws ServiceException {
        ArrayList jArray = new ArrayList();
        try {
            KwlReturnObject result = accVendorDAOobj.getVendor_Dashboard(companyID, true, "createdOn", 0, 2);
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            String link;
            String vendorID = "";
            while (itr.hasNext()) {
                Vendor vendor = (Vendor) itr.next();
                link = vendor.getAccount().getName();
                vendorID = vendor.getAccount().getID();
                if (DashboardHandler.isPermitted(perms, "vendor", "view")) {
                    link = DashboardHandler.getLink(link, "callVendorDetails(\"" + vendorID + "\")");
                }
                jArray.add(DashboardHandler.getFormatedAlert("New vendor " + link + " created", "accountingbase updatemsg-vendor", isDashboard));
            }
            KwlReturnObject result1 = accVendorDAOobj.getVendor_Dashboard(companyID, true, "modifiedOn", 0, 2);
            list = result1.getEntityList();
            itr = list.iterator();
            while (itr.hasNext()) {
                Vendor vendor = (Vendor) itr.next();
                link = vendor.getAccount().getName();
                vendorID = vendor.getAccount().getID();
                if (DashboardHandler.isPermitted(perms, "vendor", "view")) {
                    link = DashboardHandler.getLink(link, "callVendorDetails(\"" + vendorID + "\")");
                }
                jArray.add(DashboardHandler.getFormatedAlert("Vendor " + link + " modified", "accountingbase updatemsg-vendor", isDashboard));
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jArray;
    }

    public ArrayList getCustomersUpdationInfo(String companyID, JSONObject perms, Boolean isDashboard) throws ServiceException {
        ArrayList jArray = new ArrayList();
        try {
            KwlReturnObject result = accCustomerDAOobj.getCustomer_Dashboard(companyID, true, "createdOn", 0, 2);
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            String link;
            String customerID;
            while (itr.hasNext()) {
                Customer customer = (Customer) itr.next();
                customerID = customer.getID();
                link = customer.getAccount().getName();
                if (DashboardHandler.isPermitted(perms, "customer", "view")) {
                    link = DashboardHandler.getLink(link, "callCustomerDetails(\"" + customerID + "\")");
                }

                jArray.add(DashboardHandler.getFormatedAlert("New customer " + link + " created", "accountingbase updatemsg-customer", isDashboard));
            }
            KwlReturnObject result1 = accCustomerDAOobj.getCustomer_Dashboard(companyID, false, "modifiedOn", 0, 2);
            list = result1.getEntityList();
            itr = list.iterator();
            while (itr.hasNext()) {
                Customer customer = (Customer) itr.next();
                customerID = customer.getID();
                link = customer.getAccount().getName();
                if (DashboardHandler.isPermitted(perms, "customer", "view")) {
                    link = DashboardHandler.getLink(link, "callCustomerDetails(\"" + customerID + "\")");
                }
                jArray.add(DashboardHandler.getFormatedAlert("Customer " + link + " modified", "accountingbase updatemsg-customer", isDashboard));
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jArray;
    }

    public JSONObject getPermissions(String userid) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONObject fjobj = new JSONObject();
        JSONObject ujobj = new JSONObject();
        try {
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
            requestParams.put("userid", userid);
            KwlReturnObject krObj = permissionHandlerDAOObj.getUserPermission(requestParams);
            ite = krObj.getEntityList().iterator();
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                ujobj.put(row[0].toString(), row[1]);
            }
            jobj.put("Perm", fjobj);
            jobj.put("UPerm", ujobj);
        } catch (Exception e) {
            throw ServiceException.FAILURE("PermissionHandler.getPermissions", e);
        }
        return jobj;
    }

    private static void replaceTag(JSONArray jArr, String oldTag, String newTag, ArrayList properties) throws JSONException {
        for (int i = 0; i < jArr.length(); i++) {
            String str = jArr.getString(i);
            str = str.replaceAll("<" + oldTag + " [^>]*>", "<" + newTag + (properties != null ? " " + DashboardHandler.joinArrayList(properties, " ") : "") + ">");
            str = str.replaceAll("</" + oldTag + ">", "</" + newTag + ">");
            jArr.put(i, str);
        }
    }

    public static String getMessage(int type, int mode) {
        String r = "";
        String temp = "";
        switch (type) {
            case 1:     // success messages
                temp = "m" + String.format("%02d", mode);
                r = "{\"success\": true, \"infocode\": \"" + temp + "\"}";
                break;
            case 2:     // error messages
                temp = "e" + String.format("%02d", mode);

                r = "{\"success\": false, \"errorcode\": \"" + temp + "\"}";
                break;
        }
        return r;
    }

    public String editUser(HttpServletRequest request) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String userid = "";
            boolean flag = false;
            if (jobj.has("userid")) {
                userid = StringUtil.serverHTMLStripper(jobj.get("userid").toString());
            } else {
                flag = true;
                r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }
            if (!flag) {
                String emailid = jobj.has("emailid") ? jobj.getString("emailid").trim().replace(" ", "+") : "";
                String fname = jobj.has("fname") ? StringUtil.serverHTMLStripper(jobj.get("fname").toString()) : "";
                String lname = jobj.has("lname") ? StringUtil.serverHTMLStripper(jobj.get("lname").toString()) : "";
                emailid = jobj.has("emailid") ? StringUtil.serverHTMLStripper(emailid) : "";
                String contactno = jobj.has("contactno") ? StringUtil.serverHTMLStripper(jobj.get("contactno").toString()) : "";
                String address = jobj.has("address") ? StringUtil.serverHTMLStripper(jobj.get("address").toString()) : "";

                String timezone = jobj.has("timezone") ? StringUtil.serverHTMLStripper(jobj.get("timezone").toString()) : "";

                KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
                User u = (User) userResult.getEntityList().get(0);
                if (u != null) {
                    HashMap<String, Object> userHashMap = new HashMap<>();
                    userHashMap.put("userid", u.getUserID());
                    userHashMap.put("firstName", fname);
                    userHashMap.put("address", address);
                    userHashMap.put("lastName", lname);
                    userHashMap.put("emailID", emailid);
                    userHashMap.put("contactno", contactno);
                    userHashMap.put("timeZone", timezone);

                    KwlReturnObject rtObj = profileHandlerDAOObj.addUser(userHashMap);
                    u = (User) rtObj.getEntityList().get(0);
                } else {
                    r = getMessage(2, 6);
                }
            }
        } catch (JSONException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "JSON Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        } catch (Exception e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    public String deactivateCompany(HttpServletRequest request) throws ServiceException {
        String result = getMessage(1, 16);
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String comp = isCompanyExists(jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {

                String companyID = jobj.getString("companyid");
                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
                Company company = (Company) result1.getEntityList().get(0);
                if (company == null) {
                    return getMessage(2, 16);
                } else {
                    HashMap<String, Object> companyHashMap = new HashMap<>();
                    companyHashMap.put("companyid", company.getCompanyID());
                    companyHashMap.put("isactivated", false);
                    KwlReturnObject cmprtObj = profileHandlerDAOObj.updateCompany(companyHashMap);
                }
            } else {
                // Company doesn't exists or Insufficient Data
                result = cj.toString();
            }

        } catch (ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Service Exception while deactivating company", ex);
            throw ServiceException.FAILURE(result, ex);
        } catch (JSONException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "JSON Exception while deactivating company", ex);
            throw ServiceException.FAILURE(result, ex);
        }

        return result;
    }

    public String isCompanyExists(JSONObject jobj) throws ServiceException {
        String r = getMessage(1, 2);//"{\"success\": true, \"infocode\": \"m02\"}";
        List list = new ArrayList();
        try {
            String sql = "";
            boolean flag = false;
            String param = "";
            if (jobj.has("companyid")) {
                list = accCompanyPreferencesObj.isCompanyExistWithCompanyID(jobj.getString("companyid"));
            } else if (jobj.has("subdomain")) {
                list = accCompanyPreferencesObj.isCompanyExistWithSubDomain(jobj.getString("subdomain"));
            } else {
                flag = true;
                r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }
            if (!flag) {
                if (list.size() > 0) {
                    r = getMessage(1, 1);//"{\"success\": true, \"message\": \"m01\"}";
                }
            }
        } catch (JSONException e) {
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(r, e);
        } catch (ServiceException e) {
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public boolean isCompanyActivated(JSONObject jobj) throws ServiceException {
        boolean result = false;
        try {
            String comp = isCompanyExists(jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {
                result = accCompanyPreferencesObj.isCompanyActivated(jobj.getString("companyid"));
            }

        } catch (JSONException e) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("JSON exception in isCompanyActivated()", e);
        } catch (ServiceException e) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("Service exception in isCompanyActivated()", e);
        }
        return result;
    }

    public String deleteCompany(JSONObject jobj) throws ServiceException, SQLException {
        String result = getMessage(1, 15);
        try {
            String comp = isCompanyExists(jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {
                String subdomain = jobj.getString("subdomain");
                accCompanyPreferencesObj.deleteCompanyData(subdomain);
            }
        } catch (JSONException e) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("JSON exception in deleteCompany()", e);
        } catch (ServiceException e) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("Service exception in deleteCompany()", e);
        } catch (Exception e) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("Exception in deleteCompany()", e);
        }
        return result;
    }

    public String hrmsSalaryJE(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject result = new JSONObject();
        try {
            result.put("success", true);
            result.put("infocode", "m302");
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String comp = isCompanyExists(jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {

                String companyID = jobj.getString("companyid");
                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
                Company company = (Company) result1.getEntityList().get(0);

                KwlReturnObject compAccPrefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
                CompanyAccountPreferences cap = (CompanyAccountPreferences) compAccPrefResult.getEntityList().get(0);

                KwlReturnObject cpfEEReturnObject = accAccountDAOobj.getAccountFromName(companyID, "CPF Employer Expense");
                List acclist = cpfEEReturnObject.getEntityList();
                Iterator itr = acclist.iterator();
                Account cpfEmployerExpenseAccount = null;
                while (itr.hasNext()) {
                    cpfEmployerExpenseAccount = (Account) itr.next();
                }

                KwlReturnObject cpfPReturnObject = accAccountDAOobj.getAccountFromName(companyID, "CPF Payable");
                acclist = cpfPReturnObject.getEntityList();
                itr = acclist.iterator();
                Account cpfPayableAccount = null;
                while (itr.hasNext()) {
                    cpfPayableAccount = (Account) itr.next();
                }

                int maxnumber = 0;
                SequenceFormat jeSeqFormat = null;
                String modulename = "autojournalentry";
                String jeDatePrefix = "";
                String jeDateAfterPrefix = "";
                String jeDateSuffix = "";

                Map<String, Object> sfrequestParams = new HashMap<String, Object>();
                sfrequestParams.put("companyid", companyID);
                sfrequestParams.put("modulename", modulename);
                KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
                List<SequenceFormat> ll = seqFormatResult.getEntityList();
                if (ll.isEmpty()) {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("modulename", modulename);
                    requestParams.put("numberofdigit", 6);
                    requestParams.put("showleadingzero", true);
                    requestParams.put("prefix", Constants.JE_DEFAULT_PREFIX);
                    requestParams.put("sufix", "");
                    requestParams.put("startfrom", 0);
                    requestParams.put("name", Constants.JE_DEFAULT_FORMAT);
                    requestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    requestParams.put("companyid", companyID);
                    jeSeqFormat = accCompanyPreferencesObj.saveSequenceFormat(requestParams);

                } else if (ll.get(0) != null) {
                    SequenceFormat format = (SequenceFormat) ll.get(0);
                    String sequenceformatid = format.getID();
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyID, StaticValues.AUTONUM_JOURNALENTRY, sequenceformatid, false , new Date());
                    if (!seqNumberMap.isEmpty()) {
                        maxnumber = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                        jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part 
                    } else {
                        maxnumber = 0;
                    }
                    jeSeqFormat = format;
                    jeSeqFormat.setStartfrom(maxnumber);
                }

                if (cap.getLiabilityAccount() != null && cap.getExpenseAccount() != null && jobj.getString("currencyid") != null && jobj.getString("jarr") != null && cpfEmployerExpenseAccount != null && cpfPayableAccount != null) {

                    KwlReturnObject currencyResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), jobj.getString("currencyid"));
                    KWLCurrency currency = (KWLCurrency) currencyResult.getEntityList().get(0);
                    JSONArray jArr = jobj.getJSONArray("jarr");

                    for (int i = 0; i < jArr.length(); i++) {

                        JSONObject salary = jArr.getJSONObject(i);
                        String month = (salary.has("month") && salary.getString("month") != null) ? salary.getString("month") : "";
                        String name = (salary.has("name") && salary.getString("name") != null) ? salary.getString("name") : "";
                        Double salaryPayable = Double.parseDouble(salary.getString("salaryPayable"));
                        Double salaryexpense = Double.parseDouble(salary.getString("salaryExpense"));
                        Double cpfEmployerExpense = Double.parseDouble(salary.getString("cpfEmployerExpense"));
                        Double cpfPayable = Double.parseDouble(salary.getString("cpfPayable"));

                        jeSeqFormat.setStartfrom(jeSeqFormat.getStartfrom() + 1);
                        int nextNumber = jeSeqFormat.getStartfrom();
                        int numberofdigit = jeSeqFormat.getNumberofdigit();
                        boolean showleadingzero = jeSeqFormat.isShowleadingzero();
                        String prefix = jeSeqFormat.getPrefix() != null ? jeSeqFormat.getPrefix() : "";
                        String suffix = jeSeqFormat.getSuffix() != null ? jeSeqFormat.getSuffix() : "";
                        String nextNumTemp = nextNumber + "";
                        if (showleadingzero) {
                            while (nextNumTemp.length() < numberofdigit) {
                                nextNumTemp = "0" + nextNumTemp;
                            }
                        }
                        String autoNumber = jeDatePrefix + prefix + jeDateAfterPrefix + nextNumTemp + suffix + jeDateSuffix;

                        String jeuuid = UUID.randomUUID().toString();
                        JournalEntry je = new JournalEntry();
                        je.setCompany(company);
                        je.setAutoGenerated(true);
                        je.setCurrency(currency);
                        je.setDeleted(false);
                        je.setEntryDate(new Date());
                        je.setMemo(name + " : Salary JE for the month of " + month);
                        je.setID(jeuuid);
                        je.setEntryNumber(autoNumber);
                        je.setSeqnumber(nextNumber);
                        je.setDatePreffixValue(jeDatePrefix);
                        je.setDateAfterPreffixValue(jeDateAfterPrefix);
                        je.setDateSuffixValue(jeDateSuffix);
                        je.setSeqformat(jeSeqFormat);
//	                    	session.save(je);
                        kwlCommonTablesDAOObj.saveObj(je);

                        String debitje = UUID.randomUUID().toString();
                        JournalEntryDetail jed = new JournalEntryDetail();
                        jed.setAccount(cap.getExpenseAccount());
                        jed.setAmount(salaryexpense);
                        jed.setDebit(true);
                        jed.setCompany(company);
                        jed.setDescription("Salary Expense from HRMS");
                        jed.setID(debitje);
                        jed.setSrno(1);
                        jed.setJournalEntry(je);
//	                    	session.save(jed);
                        kwlCommonTablesDAOObj.saveObj(jed);

                        String cpfPayableJed = UUID.randomUUID().toString();
                        JournalEntryDetail jed2 = new JournalEntryDetail();
                        jed2.setAccount(cpfEmployerExpenseAccount);
                        jed2.setAmount(cpfEmployerExpense);
                        jed2.setDebit(true);
                        jed2.setCompany(company);
                        jed2.setDescription("CPF Employer Expense from HRMS");
                        jed2.setID(cpfPayableJed);
                        jed2.setSrno(2);
                        jed2.setJournalEntry(je);
//	                    	session.save(jed2);
                        kwlCommonTablesDAOObj.saveObj(jed2);

                        String creditje = UUID.randomUUID().toString();
                        JournalEntryDetail jed1 = new JournalEntryDetail();
                        jed1.setAccount(cap.getLiabilityAccount());
                        jed1.setAmount(salaryPayable);
                        jed1.setDebit(false);
                        jed1.setCompany(company);
                        jed1.setDescription("Salary Payable from HRMS");
                        jed1.setID(creditje);
                        jed1.setSrno(3);
                        jed1.setJournalEntry(je);
//	                    	session.save(jed1);
                        kwlCommonTablesDAOObj.saveObj(jed1);

                        String cpfEmployerExpenseJed = UUID.randomUUID().toString();
                        JournalEntryDetail jed3 = new JournalEntryDetail();
                        jed3.setAccount(cpfPayableAccount);
                        jed3.setAmount(cpfPayable);
                        jed3.setDebit(false);
                        jed3.setCompany(company);
                        jed3.setDescription("CPF Payable from HRMS");
                        jed3.setID(cpfEmployerExpenseJed);
                        jed3.setSrno(4);
                        jed3.setJournalEntry(je);
//	                    	session.save(jed3);
                        kwlCommonTablesDAOObj.saveObj(jed3);

                        Set<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
                        details.add(jed);
                        details.add(jed1);
                        details.add(jed2);
                        details.add(jed3);
                        je.setDetails(details);
                        accJournalEntryobj.saveJournalEntryDetailsSet(details);
//                                session.save(je);
                        kwlCommonTablesDAOObj.saveObj(je);

                        //add account's amounts from newly added JE in jedetails_optimized table
                        saveAccountJEs_optimized(je.getID());

                        jArr.getJSONObject(i).put("jeid", je.getID());
                    }
                    result.put("jarr", jArr);

                }
            } else {
                result.put("success", false);
                result.put("infocode", "e01");
            }

        } catch (ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Service Exception while creating hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "JSON Exception while creating hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        }

        return result.toString();
    }

    public String getYearLock(HttpServletRequest request) throws ServiceException {
//    	String result = "{\"success\": true, \"infocode\": \"m301\" ";
        JSONObject result = new JSONObject();
        JSONArray jArr = new JSONArray();
        JSONObject res = new JSONObject();
        try {
            result.put("success", true);
            result.put("infocode", "m301");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyID = jobj.getString("companyid");
            String comp = isCompanyExists(jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {
                List list = accCompanyPreferencesObj.getYearID(companyID);
                if (list.size() > 0) {

                    KwlReturnObject compAccPrefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
                    CompanyAccountPreferences cap = (CompanyAccountPreferences) compAccPrefResult.getEntityList().get(0);
                    Calendar fyFrom = Calendar.getInstance();
                    fyFrom.setTime(cap.getFinancialYearFrom());
                    Calendar startdate = Calendar.getInstance();
                    Calendar enddate = Calendar.getInstance();
                    for (int i = 0; i < list.size(); i++) {
                        startdate.setTime(cap.getFinancialYearFrom());
                        enddate.setTime(cap.getFinancialYearFrom());
                        startdate.set(Calendar.YEAR, (Integer) list.get(i));
                        enddate.set(Calendar.YEAR, ((Integer) list.get(i)) + 1);
                        enddate.add(Calendar.DATE, -1);
                        res = new JSONObject();
                        res.put("startdate", sdf.format(startdate.getTime()));
                        res.put("enddate", sdf.format(enddate.getTime()));
                        jArr.put(res);
                    }
                }
                result.put("jarr", jArr);
            } else {
                result.put("success", false);
                result.put("infocode", "e01");
            }
        } catch (JSONException e) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("JSON exception in getYearLock()", e);
        } catch (ServiceException e) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("Service exception in getYearLock()", e);
        }

        return result.toString();

    }

    public String reverseHrmsSalaryJE(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject result = new JSONObject();
        try {
            result.put("success", true);
            result.put("infocode", "m303");
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String comp = isCompanyExists(jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {

                String companyID = jobj.getString("companyid");
                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
                Company company = (Company) result1.getEntityList().get(0);
                KwlReturnObject compAccPrefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
                CompanyAccountPreferences cap = (CompanyAccountPreferences) compAccPrefResult.getEntityList().get(0);

                int maxnumber = 0;
                SequenceFormat jeSeqFormat = null;
                String modulename = "autojournalentry";
                String jeDatePrefix = "";
                String jeDateAfterPrefix = "";
                String jeDateSuffix = "";
                Map<String, Object> sfrequestParams = new HashMap<String, Object>();
                sfrequestParams.put("companyid", companyID);
                sfrequestParams.put("modulename", modulename);
                KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
                List<SequenceFormat> ll = seqFormatResult.getEntityList();

                if (ll.isEmpty()) {

                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("modulename", modulename);
                    requestParams.put("numberofdigit", 6);
                    requestParams.put("showleadingzero", true);
                    requestParams.put("prefix", Constants.JE_DEFAULT_PREFIX);
                    requestParams.put("sufix", "");
                    requestParams.put("startfrom", 0);
                    requestParams.put("name", Constants.JE_DEFAULT_FORMAT);
                    requestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    requestParams.put("companyid", companyID);
                    jeSeqFormat = accCompanyPreferencesObj.saveSequenceFormat(requestParams);

                } else if (ll.get(0) != null) {
                    SequenceFormat format = (SequenceFormat) ll.get(0);
                    String sequenceformatid = format.getID();
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyID, StaticValues.AUTONUM_JOURNALENTRY, sequenceformatid, false, new Date());
                    if (!seqNumberMap.isEmpty()) {
                        maxnumber = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                        jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    } else {
                        maxnumber = 0;
                    }
                    jeSeqFormat = format;
                    jeSeqFormat.setStartfrom(maxnumber);
                }

                if (cap.getLiabilityAccount() != null && cap.getExpenseAccount() != null && jobj.getString("currencyid") != null && jobj.getString("jarr") != null) {

                    KwlReturnObject currencyResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), jobj.getString("currencyid"));
                    KWLCurrency currency = (KWLCurrency) currencyResult.getEntityList().get(0);

                    JSONArray jArr = jobj.getJSONArray("jarr");
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject salary = jArr.getJSONObject(i);
                        Double salaryAmount = Double.parseDouble(salary.getString("salary"));
                        jeSeqFormat.setStartfrom(jeSeqFormat.getStartfrom() + 1);
                        int nextNumber = jeSeqFormat.getStartfrom();
                        int numberofdigit = jeSeqFormat.getNumberofdigit();
                        boolean showleadingzero = jeSeqFormat.isShowleadingzero();
                        String prefix = jeSeqFormat.getPrefix() != null ? jeSeqFormat.getPrefix() : "";
                        String suffix = jeSeqFormat.getSuffix() != null ? jeSeqFormat.getSuffix() : "";
                        String nextNumTemp = nextNumber + "";
                        if (showleadingzero) {
                            while (nextNumTemp.length() < numberofdigit) {
                                nextNumTemp = "0" + nextNumTemp;
                            }
                        }
                        String autoNumber = jeDatePrefix + prefix + jeDateAfterPrefix + nextNumTemp + suffix + jeDateSuffix;

                        KwlReturnObject jeResult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), salary.getString("jeid"));
                        JournalEntry journalEntry = (JournalEntry) jeResult.getEntityList().get(0);

                        String jeuuid = UUID.randomUUID().toString();
                        JournalEntry je = new JournalEntry();
                        je.setCompany(company);
                        je.setAutoGenerated(true);
                        je.setCurrency(currency);
                        je.setDeleted(false);
                        je.setEntryDate(new Date());
                        je.setMemo("Reverse Entry for Salary JE from HRMS for JE no " + journalEntry.getEntryNumber());
                        je.setID(jeuuid);
                        je.setEntryNumber(autoNumber);
                        je.setSeqnumber(nextNumber);
                        je.setDatePreffixValue(jeDatePrefix);
                        je.setDateAfterPreffixValue(jeDateAfterPrefix);
                        je.setDateSuffixValue(jeDateSuffix);
                        je.setSeqformat(jeSeqFormat);
                        kwlCommonTablesDAOObj.saveObj(je);

                        String debitje = UUID.randomUUID().toString();
                        JournalEntryDetail jed = new JournalEntryDetail();
                        jed.setAccount(cap.getExpenseAccount());
                        jed.setAmount(salaryAmount);
                        jed.setDebit(false);
                        jed.setCompany(company);
                        jed.setDescription("Reverse Entry for Salary JE from HRMS");
                        jed.setID(debitje);
                        jed.setSrno(1);
                        jed.setJournalEntry(je);
                        kwlCommonTablesDAOObj.saveObj(jed);

                        String creditje = UUID.randomUUID().toString();
                        JournalEntryDetail jed1 = new JournalEntryDetail();
                        jed1.setAccount(cap.getLiabilityAccount());
                        jed1.setAmount(salaryAmount);
                        jed1.setDebit(true);
                        jed1.setCompany(company);
                        jed1.setDescription("Reverse Entry for Salary JE from HRMS");
                        jed1.setID(creditje);
                        jed1.setSrno(2);
                        jed1.setJournalEntry(je);
                        kwlCommonTablesDAOObj.saveObj(jed1);

                        Set<JournalEntryDetail> details = new HashSet<>();
                        details.add(jed);
                        details.add(jed1);
                        je.setDetails(details);
                        accJournalEntryobj.saveJournalEntryDetailsSet(details);
                        kwlCommonTablesDAOObj.saveObj(je);

                        //add account's amounts from newly added JE in jedetails_optimized table
                        saveAccountJEs_optimized(je.getID());
                    }
                }
            } else {
                result.put("success", false);
                result.put("infocode", "e01");
            }

        } catch (ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Service Exception while reverse hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "JSON Exception while reverse hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        }

        return result.toString();
    }

    public String getAccountListForPM(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        try {
            result.put("success", true);
            result.put("infocode", "m302");
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String comp = isCompanyExists(jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {
                JSONArray jArr = new JSONArray();
                String companyID = jobj.getString("companyid");
                String nature = jobj.has("nature") ? jobj.getString("nature") : "";
                String mastertype = jobj.has("mastertype") ? jobj.getString("mastertype") : "";

                HashMap<String, Object> reqParams = new HashMap<>();
                reqParams.put("companyid", companyID);
                reqParams.put("nature", nature);
                reqParams.put("mastertype", mastertype);
                KwlReturnObject accResult = accAccountDAOobj.getAccountsForPM(reqParams);
                List list = accResult.getEntityList();
                int totalCount = accResult.getRecordTotalCount();
                Iterator itr = list.iterator();

                while (itr.hasNext()) {
                    Object[] oj = (Object[]) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("accountID", oj[0].toString());
                    obj.put("accountName", oj[1].toString());
                    if (oj[2] != null) {
                        obj.put("accountCode", oj[2].toString());
                    } else {
                        obj.put("accountCode", "");
                    }
                    if (oj[3] != null) {
                        obj.put("mastertype", oj[3].toString());
                    }
                    jArr.put(obj);
                }
                result.put("data", jArr);
                result.put("totalCount", totalCount);
            } else {
                result.put("success", false);
                result.put("infocode", "e01");
            }
        } catch (ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Service Exception while creating hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "JSON Exception while creating hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        }
        return result.toString();
    }

    public String pmAmountJE(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        try {
            String journalEntryId="";
            result.put("success", true);
            result.put("infocode", "m302");
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String comp = isCompanyExists(jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {

                String companyID = jobj.getString("companyid");
                KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
                Company company = (Company) companyResult.getEntityList().get(0);
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date newdate = new Date();
                String userdiff = company.getCreator() == null ? "" : (company.getCreator().getTimeZone() != null ? company.getCreator().getTimeZone().getDifference() : company.getTimeZone().getDifference());
                sdf1.setTimeZone(TimeZone.getTimeZone("GMT" + userdiff));
                Date newcreatedate = authHandler.getDateWithTimeFormat().parse(sdf1.format(newdate));
                SequenceFormat jeSeqFormat = null;
                String modulename = "autojournalentry";
                String jeDatePrefix = "";
                String jeDateAfterPrefix = "";
                String jeDateSuffix = "";
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                String sequenceformatid="";
                int maxnumber=0;
                
                Map<String, Object> sfrequestParams = new HashMap<>();
                sfrequestParams.put("companyid", companyID);
                sfrequestParams.put("modulename", modulename);
                KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
                List<SequenceFormat> ll = seqFormatResult.getEntityList();
                if (ll.isEmpty()) {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("modulename", modulename);
                    requestParams.put("numberofdigit", 6);
                    requestParams.put("showleadingzero", true);
                    requestParams.put("prefix", Constants.JE_DEFAULT_PREFIX);
                    requestParams.put("sufix", "");
                    requestParams.put("startfrom", 0);
                    requestParams.put("name", Constants.JE_DEFAULT_FORMAT);
                    requestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    requestParams.put("companyid", companyID);
                    jeSeqFormat = accCompanyPreferencesObj.saveSequenceFormat(requestParams);
              
                } else if (ll.get(0) != null) {
                    SequenceFormat format = (SequenceFormat) ll.get(0);
                    sequenceformatid = format.getID();                 
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyID, StaticValues.AUTONUM_JOURNALENTRY, sequenceformatid, false, newcreatedate);
                    if (!seqNumberMap.isEmpty()) {
                        maxnumber = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                        jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    } 
                    jeSeqFormat = format;
                    jeSeqFormat.setStartfrom(maxnumber);
                }

                if (jobj.getString("currencyid") != null && jobj.getString("data") != null) {
                    KwlReturnObject currencyResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), jobj.getString("currencyid"));
                    KWLCurrency currency = (KWLCurrency) currencyResult.getEntityList().get(0);
                    JSONArray jArr = jobj.getJSONArray("data");
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject GLObject = jArr.getJSONObject(i);

                        if (!StringUtil.isNullOrEmpty(GLObject.getString("glDebitAccountId")) && !StringUtil.isNullOrEmpty(GLObject.getString("glCreditAccountId"))) {
                            String glDebitAccountId = GLObject.getString("glDebitAccountId");
                            String glCreditAccountId = GLObject.getString("glCreditAccountId");
                            Double glAmount = (GLObject.has("glAmount") && GLObject.getString("glAmount") != null) ? Double.parseDouble(GLObject.getString("glAmount")) : 0;
                            Double TaxAmount = (GLObject.has("TaxAmount") && GLObject.getString("TaxAmount") != null) ? Double.parseDouble(GLObject.getString("TaxAmount")) : 0;

                            KwlReturnObject glDrAccResult = accountingHandlerDAOobj.getObject(Account.class.getName(), glDebitAccountId);
                            Account glDebitAccount = (Account) glDrAccResult.getEntityList().get(0);

                            KwlReturnObject glCrAccResult = accountingHandlerDAOobj.getObject(Account.class.getName(), glCreditAccountId);
                            Account glCreditAccount = (Account) glCrAccResult.getEntityList().get(0);

                            String JEMemo = GLObject.has("JEMemo") ? StringUtil.decodeString(GLObject.getString("JEMemo")) : glDebitAccount.getName() + " Dr to " + glCreditAccount.getName() + " Cr for Category: \"" + GLObject.getString("categoryName") + "\" for Project: \"" + GLObject.getString("projectName") + "\"";
                            String JEDescription = GLObject.has("JEDescription") ? GLObject.getString("JEDescription") : "Amount from Project Management";
                            String taxID = GLObject.has("taxid") ? GLObject.getString("taxid") : "";
                            boolean isFromEclaim = GLObject.has("isFromEclaim") ? GLObject.getBoolean("isFromEclaim"): false;
                            boolean includeInGSTReport = GLObject.has("includeingstreport") ? GLObject.getBoolean("includeingstreport"): false;
                            
                            /*If Tax is not present in ERP side then we simply retrun with info code   */
                            Tax tax = null;
                            if (!StringUtil.isNullOrEmpty(taxID)) {
                                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxID);
                                tax = (Tax) result1.getEntityList().get(0);
                                if (StringUtil.isNullObject(tax)) {
                                    result.put("success", false);
                                    result.put("infocode", "e350");
                                    break;
                                }

                            }
                            
                            int nextNumber = jeSeqFormat.getStartfrom();
                            int numberofdigit = jeSeqFormat.getNumberofdigit();
                            boolean showleadingzero = jeSeqFormat.isShowleadingzero();
                            String prefix = jeSeqFormat.getPrefix() != null ? jeSeqFormat.getPrefix() : "";
                            String suffix = jeSeqFormat.getSuffix() != null ? jeSeqFormat.getSuffix() : "";
                            String nextNumTemp = nextNumber + "";
                            if (showleadingzero) {
                                while (nextNumTemp.length() < numberofdigit) {
                                    nextNumTemp = "0" + nextNumTemp;
                                }
                            }
                            String autoNumber = jeDatePrefix + prefix + nextNumTemp + suffix + jeDateSuffix;
                            jeSeqFormat.setStartfrom(jeSeqFormat.getStartfrom() + 1);
                                                        
                            String jeid = (GLObject.has("jeId")) ? GLObject.getString("jeId") : "";
                            if (StringUtil.isNullOrEmpty(jeid)) {  //New JE
                             
                                Map<String, Object> jeDataMap = new HashMap<>();
                                jeDataMap.put("companyid", company.getCompanyID());
                                jeDataMap.put("autogenerated", true);
                                jeDataMap.put("currencyid", currency.getCurrencyID());
                                jeDataMap.put("entrydate", newcreatedate);
                                jeDataMap.put("memo", JEMemo);
                                jeDataMap.put("isFromEclaim", isFromEclaim);
                                jeDataMap.put("entrynumber", autoNumber);
                                jeDataMap.put(Constants.SEQNUMBER, nextNumber);
                                jeDataMap.put(Constants.SEQFORMAT, sequenceformatid);
                                jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                                jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                                jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                                jeDataMap.put("includeInGSTReport", includeInGSTReport);
                                jeDataMap.put("typevalue", includeInGSTReport ? 1:0);
                                
                                JournalEntry journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                                accJournalEntryobj.saveJournalEntryByObject(journalEntry);
                                
                                journalEntryId=journalEntry.getID();

                                JSONObject jedjson = new JSONObject();
                                jedjson.put("accountid", glDebitAccount.getID());
                                jedjson.put("amount", glAmount);
                                jedjson.put("debit", true);
                                jedjson.put("companyid", company.getCompanyID());
                                jedjson.put("description", JEDescription);
                                jedjson.put("srno", 1);
                                jedjson.put("jeid", journalEntry.getID());
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                Set<JournalEntryDetail> detail = new HashSet();
                                detail.add(jed);

                                jedjson = new JSONObject();
                                jedjson.put("accountid", glCreditAccount.getID());
                                jedjson.put("amount", glAmount+TaxAmount);//total Credit Amount
                                jedjson.put("debit", false);
                                jedjson.put("companyid", company.getCompanyID());
                                jedjson.put("description", JEDescription);
                                jedjson.put("srno", TaxAmount!=0 ? 3 : 2);
                                jedjson.put("jeid", journalEntry.getID());
                                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                detail.add(jed);
                                
                                /* Save JeDetail for tax applied in eclaim*/
                                if (!StringUtil.isNullOrEmpty(taxID)) {
                                    jedjson = new JSONObject();
                                    jedjson.put("accountid", tax.getAccount().getID());
                                    jedjson.put("amount", TaxAmount);
                                    jedjson.put("debit", true);
                                    jedjson.put("companyid", company.getCompanyID());
                                    jedjson.put("description", JEDescription);
                                    jedjson.put("srno", 2);
                                    jedjson.put("jeid", journalEntry.getID());                                    
                                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                    jed.setGstapplied(tax);
                                    detail.add(jed);
                                }
                        
                                   
                        
                                                               
                                journalEntry.setDetails(detail);
                                accJournalEntryobj.saveJournalEntryDetailsSet(detail);

                                //add account's amounts from newly added JE in jedetails_optimized table
                                saveAccountJEs_optimized(journalEntry.getID());

                                jArr.getJSONObject(i).put("jeId", journalEntry.getID());
                                
                                
                                /* Adding custom field value on Line level coming from Eclaim*/
                                String customfield = GLObject.optString("customfieldmap", null);
                                for (Iterator<JournalEntryDetail> jEDIterator = detail.iterator(); jEDIterator.hasNext();) {
                                    JournalEntryDetail jedetail = jEDIterator.next();

                                    if (!StringUtil.isNullOrEmpty(customfield)) {
                                        JSONArray jcustomarray = new JSONArray(customfield);
                                        if (jcustomarray.length() > 0) {
                                            jcustomarray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_GENERAL_LEDGER_ModuleId, companyID,false);
                                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                                            customrequestParams.put("customarray", jcustomarray);
                                            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                                            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                                            customrequestParams.put("modulerecid", jedetail.getID());
                                            customrequestParams.put("recdetailId", jedetail.getID());
                                            customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                            customrequestParams.put("companyid", companyID);
                                            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {

                                                JSONObject jeDetailJson = new JSONObject();
                                                jeDetailJson.put("accjedetailcustomdata", jedetail.getID());
                                                jeDetailJson.put("jedid", jed.getID());
                                                jedresult = accJournalEntryobj.updateJournalEntryDetails(jeDetailJson);
                                            }
                                        }

                                    }

                                }

                                /* Adding custom field value on Global level coming from Eclaim*/
                                if (!StringUtil.isNullOrEmpty(customfield)) {
                                    JSONArray jcustomarray = new JSONArray(customfield);
                                    if (jcustomarray.length() > 0) {
                                        if (jcustomarray.length() > 0) {
                                            jcustomarray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_GENERAL_LEDGER_ModuleId, companyID,true);
                                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                                            customrequestParams.put("customarray", jcustomarray);
                                            customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                                            customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                                            customrequestParams.put("modulerecid", journalEntryId);
                                            customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                            customrequestParams.put("companyid", companyID);
                                            customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                                Map<String, Object> customjeDataMap = new HashMap<String, Object>();
                                                customjeDataMap.put("accjecustomdataref", journalEntryId);
                                                customjeDataMap.put("jeid", journalEntryId);
                                                customjeDataMap.put("istemplate", 1);
                                                jedresult = accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                                            }
                                        }
                                    }
                                }
                                
                                
                                
                            } else {   //update JE
                                KwlReturnObject jeResult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeid);
                                JournalEntry journalEntry = (JournalEntry) jeResult.getEntityList().get(0);
                                if (journalEntry != null) {
                                    //add account's amounts from newly added JE in jedetails_optimized table
                                    accJournalEntryobj.deleteOnEditAccountJEs_optimized(jeid);

                                    Set<JournalEntryDetail> journalEntryDetails = journalEntry.getDetails();
                                    for (JournalEntryDetail journalEntryDetail : journalEntryDetails) {
                                        if (journalEntryDetail.isDebit()) {
                                            JSONObject jedjson = new JSONObject();
                                            jedjson.put("jedid", journalEntryDetail.getID());
                                            jedjson.put("accountid", glDebitAccount.getID());
                                            jedjson.put("amount", glAmount);
                                            jedjson.put("jeid", journalEntry.getID());
                                            jedjson.put("description", JEDescription);
                                            jedjson.put("debit", true);
                                            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                                        } else {
                                            JSONObject jedjson = new JSONObject();
                                            jedjson.put("jedid", journalEntryDetail.getID());
                                            jedjson.put("accountid", glCreditAccount.getID());
                                            jedjson.put("amount", glAmount);
                                            jedjson.put("jeid", journalEntry.getID());
                                            jedjson.put("description", JEDescription);
                                            jedjson.put("debit", false);
                                            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                                        }
                                    }

                                    JSONObject jeDataMap = new JSONObject();
                                    jeDataMap.put("jeid", journalEntry.getID());
                                    jeDataMap.put("entrydate", newcreatedate);
                                    jeDataMap.put("memo", JEMemo);
                                    KwlReturnObject journalEntryResult = accJournalEntryobj.updateJournalEntry(jeDataMap, new HashSet<JournalEntryDetail>());
                                    journalEntry = (JournalEntry) journalEntryResult.getEntityList().get(0);

                                    jArr.getJSONObject(i).put("successTRflag", true);
                                    jArr.getJSONObject(i).put("jeId", journalEntry.getID());

                                    //add account's amounts from newly added JE in jedetails_optimized table
                                    saveAccountJEs_optimized(journalEntry.getID());
                                }
                            }
                        } else {
                            jArr.getJSONObject(i).put("successTRflag", false);
                        }
                                }
                    result.put("data", jArr);
                }
            } else {
                result.put("success", false);
                result.put("infocode", "e01");
            }
        } catch (ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Service Exception while creating hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "JSON Exception while creating hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result.toString();
    }

    public String saveProjectDetails(HttpServletRequest request) throws ServiceException {
        JSONObject jsonResult = new JSONObject();
        KwlReturnObject result = null;
        try {
            jsonResult.put("success", true);
            jsonResult.put("infocode", "m302");
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String comp = isCompanyExists(jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {

                String companyID = jobj.getString("companyid");
                JSONArray jArray = jobj.getJSONArray("projects");

                //Fetched Project field params     
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("filter_names", Arrays.asList("companyid", "isforproject"));
                requestParams.put("filter_values", Arrays.asList(companyID, 1));

                result = fieldManagerDAOobj.getFieldParams(requestParams);

                List list = result.getEntityList();
                Iterator itr = list.iterator();
                //Loop on fields params having isProject true
                while (itr.hasNext()) {
                    FieldParams fieldParams = (FieldParams) itr.next();
                    //Loop to set combo values 
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject jobjData = jArray.getJSONObject(i);

                        String projectid = (String) jobjData.get("projectid");
                        String projectname = (String) jobjData.get("projectname");

                        //Check for duplicate. If not present then insert.
                        ArrayList filter_params = new ArrayList();
                        filter_params.add(projectid);
                        filter_params.add(fieldParams.getId());
                        List listItems = accMasterItemsDAOobj.getMasterItemsForRemoteAPI(filter_params, false);

                        Iterator itrItems = listItems.iterator();
                        HashMap requestParam = new HashMap();

                        requestParam.put("name", projectname);
                        requestParam.put("groupid", fieldParams.getId());
                        requestParam.put("projectid", projectid);
                        if (itrItems.hasNext()) {
                            FieldComboData item = (FieldComboData) itrItems.next();
                            requestParam.put("id", item.getId());
                        }
                        boolean isEdit = Boolean.parseBoolean(jobjData.get("isEdit").toString());
                        result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, isEdit);
                    }
                }
            } else {
                jsonResult.put("success", false);
                jsonResult.put("infocode", "e01");
            }
        } catch (ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Service Exception while creating Project", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jsonResult.toString();
    }

    public String deleteProjectDetails(HttpServletRequest request) throws ServiceException {
        JSONObject jsonResult = new JSONObject();
        KwlReturnObject result = null;
        boolean isUsed = false;
        try {
            jsonResult.put("success", true);
            jsonResult.put("infocode", "m302");
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String comp = isCompanyExists(jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {
                String companyID = jobj.getString("companyid");
                JSONArray jArray = jobj.getJSONArray("projects");
                //Fetched Project field params     
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("filter_names", Arrays.asList("companyid", "isforproject"));
                requestParams.put("filter_values", Arrays.asList(companyID, 1));
                result = fieldManagerDAOobj.getFieldParams(requestParams); // get custom field for module

                List list = result.getEntityList();
                Iterator itr = list.iterator();
                //Loop on fields params having isProject true
                while (itr.hasNext()) {
                    FieldParams fieldParams = (FieldParams) itr.next();
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject jobjData = jArray.getJSONObject(i);
                        String projectid = (String) jobjData.get("projectid");
                        //Check  If  present or not
                        ArrayList filter_params = new ArrayList();
                        filter_params.add(projectid);
                        filter_params.add(fieldParams.getId());
                        List listItems = accMasterItemsDAOobj.getMasterItemsForRemoteAPI(filter_params, false);
                        Iterator itrItems = listItems.iterator();
                        if (itrItems.hasNext()) {
                            FieldComboData item = (FieldComboData) itrItems.next();
                            isUsed = accMasterItemsDAOobj.isUsedMasterCustomItem(item.getId(), fieldParams.getId());
                            if (!isUsed) {
                                accMasterItemsDAOobj.deleteDimension(item.getId());
                            }
                        }
                    }
                }
                jsonResult.put("isUsed", isUsed);
            } else {
                jsonResult.put("success", false);
                jsonResult.put("infocode", "e01");
            }
        } catch (ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Service Exception while creating Project", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jsonResult.toString();
    }

    public String savePaymentMileStoneDetails(HttpServletRequest request) throws ServiceException {
        JSONObject jsonobj = new JSONObject();
        String msg = "";
        String feildLables = "";
        String Projectids = "";
        boolean NoProject = false; // Use to check if projectid from PM is not exitst in parent combo
        boolean NoParent = false; // Use to Check if Combo is having parent or not
        boolean flag = false;
        boolean NoParentmsg = false;
        JSONObject jsonResult = new JSONObject();
        KwlReturnObject result = null;
        boolean CheckForExist = true;
        try {
            jsonResult.put("success", true);
            jsonResult.put("infocode", "m302");
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String comp = isCompanyExists(jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {

                String companyID = jobj.getString("companyid");
                JSONArray jArray = jobj.getJSONArray("data");

                //Fetched Project field params     
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("filter_names", Arrays.asList("companyid", "isfortask"));
                requestParams.put("filter_values", Arrays.asList(companyID, 1));
                result = fieldManagerDAOobj.getFieldParams(requestParams);

                List list = result.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    FieldParams fieldParams = (FieldParams) itr.next();
                    if (fieldParams.getParent() == null) {  //Check if Combo doesn't having parent
                        feildLables += fieldParams.getFieldlabel() + ",";
                        jsonobj.put("Noparent", true);
                        NoParent = true;
                        continue;
                    }
                    //Loop to set combo values 
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject jobjData = jArray.getJSONObject(i);
                        CheckForExist = true;
                        String projectid = (String) jobjData.get("projectid");
                        String taskid = (String) jobjData.get("taskid");
                        String taskName = (String) jobjData.get("taskName");

                        //Check for duplicate. If not present then insert.
                        ArrayList filter_params = new ArrayList();

                        filter_params.add(projectid);
                        filter_params.add(fieldParams.getId());
                        filter_params.add(taskid);

                        List listItems = accMasterItemsDAOobj.getMasterItemsForRemoteAPI(filter_params, true);
                        Iterator itrItems = listItems.iterator();
                        HashMap requestParam = new HashMap();

                        requestParam.put("name", taskName);
                        requestParam.put("groupid", fieldParams.getId());
                        requestParam.put("projectid", projectid);
                        requestParam.put("taskid", taskid);
                        if (itrItems.hasNext()) {
                            FieldComboData item = (FieldComboData) itrItems.next();
                            requestParam.put("id", item.getId());
                            CheckForExist = false;
                        }
                        result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, false);
                        if (CheckForExist) {
                            FieldComboData FCD = (FieldComboData) result.getEntityList().get(0);
                            HashMap<String, String> ProjectTask = new HashMap<String, String>();
                            ProjectTask.put("projectid", projectid);
                            ProjectTask.put("fieldid", fieldParams.getParentid());
                            ProjectTask.put("taskFeildComboId", FCD.getId());
                            NoProject = accMasterItemsDAOobj.SaveProjectTaskMapping(ProjectTask);
                            if (NoProject) {
                                NoParentmsg = true;
                                if (!flag) {
                                    Projectids += projectid + "- Task : " + taskName + ",";
                                }
                            }
                        }
                    }
                    flag = true;
                }
            } else {
                jsonResult.put("success", false);
                jsonResult.put("infocode", "e01");
            }
            if (NoParentmsg) {
                msg += "There is No Projects with id " + Projectids;
                msg = msg.substring(0, msg.length() - 1);
            }
            if (NoParent) {
                msg += " Please Set Parent For Combo " + feildLables;
            }
            if (!StringUtil.isNullOrEmpty(msg)) {
                msg = msg.substring(0, msg.length() - 1);
            }
            jsonobj.put("msg", msg);
        } catch (ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Service Exception while creating Payment MileStone", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jsonResult.toString();
    }

    /*
     * Product Replacement Request is coming from CRM side
     */
    private String saveProductReplacementRequest(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        boolean successFlag = true;
        String msg = "";
        try {
            result.put("success", true);

            JSONObject jobject = new JSONObject(request.getParameter("data"));
            String comp = isCompanyExists(jobject);

            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {
                boolean flag = isCompanyActivated(jobject);
                if (flag) {
                    String companyID = jobject.getString("companyid");
                    KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
                    Company company = (Company) companyResult.getEntityList().get(0);

                    String replacementNumber = jobject.getString("replacemenetno");
                    String replacementid = jobject.getString("replacementid");

                    // For deleting Replacement entry
                    if (!StringUtil.isNullOrEmpty(replacementid)) {
                        List list = accSalesOrderDAOobj.deleteProductReplacement(companyID, replacementid, replacementNumber);
                        if (!list.isEmpty()) {
                            String linkedTransaction = (list.get(0) != null) ? (String) list.get(0) : "";
                            if (!StringUtil.isNullOrEmpty(linkedTransaction)) {
                                throw new AccountingException("Replacement number " + replacementNumber + " is linked with transaction. So cannot be edited.");
                            }
                        }
                    }

                    // Check whether Replacement Number exist or not
                    List ll = accSalesOrderDAOobj.getProductReplacementByReplacementNumber(replacementNumber, companyID);

                    if (!ll.isEmpty()) {
                        throw new AccountingException("Repacement Number you have entered is already available in our database, please enter another number");
                    }

                    String contractId = jobject.getString("contractid");
                    KwlReturnObject contractResult = accountingHandlerDAOobj.getObject(Contract.class.getName(), contractId);
                    Contract contract = (Contract) contractResult.getEntityList().get(0);

                    String customertId = jobject.getString("accountid");
                    ArrayList custParams = new ArrayList();
                    custParams.add(companyID);
                    custParams.add(customertId);
                    KwlReturnObject resultcheck = accCustomerDAOobj.checkCustomerExist(customertId, companyID);
                    List custList = resultcheck.getEntityList();
                    Iterator custItr = custList.iterator();
                    Customer customer = null;
                    while (custItr.hasNext()) {
                        customer = (Customer) custItr.next();
                    }

                    HashMap<String, Object> replacementMap = new HashMap<>();
                    replacementMap.put("replacementid", replacementid);
                    replacementMap.put("replacementRequestNumber", replacementNumber);
                    replacementMap.put("customerId", customer.getID());
                    replacementMap.put("contractId", contract.getID());
                    replacementMap.put("isSalesContractReplacement", contract.isNormalContract());
                    replacementMap.put("companyId", company.getCompanyID());

                    ProductReplacement productReplacement = new ProductReplacement();

                    productReplacement = accSalesOrderDAOobj.buildProductReplacement(productReplacement, replacementMap);

                    JSONArray jArr = jobject.getJSONArray("productData");

                    Set<ProductReplacementDetail> productReplacementDetails = new HashSet<>();

                    // For Checking serial numbers available or not
                    String srNumbers = "";
                    int count = 0;
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject productDataObject = jArr.getJSONObject(i);
                        String productId = productDataObject.getString("productid");
                        JSONArray serialNumberJsonArray = productDataObject.getJSONArray("serialNoData");

                        KwlReturnObject productResult = accountingHandlerDAOobj.getObject(Product.class.getName(), productId);
                        Product product = (Product) productResult.getEntityList().get(0);

                        if (product.isIsSerialForProduct()) {// if serial number  enable for product then bellow code will be process

                            for (int j = 0; j < serialNumberJsonArray.length(); j++) {
                                JSONObject serialNoDataObject = serialNumberJsonArray.getJSONObject(j);
                                String serialNumber = serialNoDataObject.getString("serialno");

                                NewBatchSerial batchSerial = accSalesOrderDAOobj.getBatchSerialByName(productId, serialNumber);
                                if (batchSerial == null) {
                                    srNumbers += serialNumber + ", ";
                                    count++;
                                }
                            }
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(srNumbers)) {
                        if (count == 1) {
                            throw new AccountingException("Serial number <b>" + srNumbers.substring(0, srNumbers.length() - 2) + "</b> is not available in our records");
                        } else {
                            throw new AccountingException("Serial number's <b>" + srNumbers.substring(0, srNumbers.length() - 2) + "</b> are not available in our records");
                        }

                    }
//                    }

                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject productDataObject = jArr.getJSONObject(i);
                        double replacementQuantity = productDataObject.getDouble("qty");
                        String productId = productDataObject.getString("productid");

                        KwlReturnObject prodResult = accountingHandlerDAOobj.getObject(Product.class.getName(), productId);
                        Product product = (Product) prodResult.getEntityList().get(0);

                        // Create Set For ProductReplacementDetail
                        ProductReplacementDetail productReplacementDetail = new ProductReplacementDetail();
                        productReplacementDetail.setIsAsset(product.isAsset());
                        productReplacementDetail.setCompany(company);
                        productReplacementDetail.setContract(contract);
                        productReplacementDetail.setProduct(product);
                        productReplacementDetail.setProductReplacement(productReplacement);
                        productReplacementDetail.setReplacementQuantity(replacementQuantity);

                        Set<ReplacementProductBatchDetailsMapping> batchDetailsMappings = new HashSet<ReplacementProductBatchDetailsMapping>();
                        if (product.isIsSerialForProduct()) {// if serial number  enable for product then bellow code will be process
                            JSONArray serialNumberJsonArray = productDataObject.getJSONArray("serialNoData");
                            for (int j = 0; j < serialNumberJsonArray.length(); j++) {
                                JSONObject serialNoDataObject = serialNumberJsonArray.getJSONObject(j);
                                String serialNumber = serialNoDataObject.getString("serialno");
                                // Creating set for saving serialmapping
                                ReplacementProductBatchDetailsMapping batchDetailsMapping = new ReplacementProductBatchDetailsMapping();
                                NewBatchSerial batchSerial = accSalesOrderDAOobj.getBatchSerialByName(productId, serialNumber);
                                if (batchSerial == null) {
                                    throw new AccountingException("Serial number " + serialNumber + " is not available in our records");
                                }
                                batchDetailsMapping.setBatchSerial(batchSerial);
                                batchDetailsMapping.setCompany(company);
                                batchDetailsMapping.setProductReplacement(productReplacement);
                                batchDetailsMapping.setProductReplacementDetail(productReplacementDetail);
                                batchDetailsMappings.add(batchDetailsMapping);
                            }
                            productReplacementDetail.setReplacementProductBatchDetailsMappings(batchDetailsMappings);
                        }
                        productReplacementDetails.add(productReplacementDetail);
                    }
                    HashMap<String, Object> productReplacementMap = new HashMap<>();
                    productReplacementMap.put("productReplacementId", productReplacement.getId());
                    productReplacementMap.put("productReplacementDetails", productReplacementDetails);
                    accSalesOrderDAOobj.updateProductReplacement(productReplacementMap);

                    msg = "Replacement Request " + replacementNumber + " has been submitted successfully.";

                } else {
                    result.put("success", false);
                    result.put("infocode", "m02");
                }
            } else {
                result.put("success", false);
                result.put("infocode", "e01");
            }
        } catch (ServiceException ex) {
            successFlag = false;
            msg = "Failed to make Connection with web server";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            successFlag = false;
            msg = "Failed to make Connection with web server";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (AccountingException exception) {
            successFlag = false;
            msg = exception.getMessage();
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, exception.getMessage(), exception);
        } finally {
            JSONObject retObj = new JSONObject();
            try {
                retObj.put("successFlag", successFlag);
                retObj.put("msg", msg);
                JSONArray returnArr = new JSONArray();
                returnArr.put(retObj);
                result.put("data", returnArr);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    private String deleteProductReplacementRequest(HttpServletRequest request) throws AccountingException {
        JSONObject result = new JSONObject();
        boolean successFlag = true;
        String msg = "";
        String deletedTransaction = "";
        try {
            result.put("success", true);

            JSONObject jobject = new JSONObject(request.getParameter("data"));
            String comp = isCompanyExists(jobject);

            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {
                boolean flag = isCompanyActivated(jobject);
                if (flag) {
                    String companyID = jobject.getString("companyid");
                    JSONArray replacementids = jobject.getJSONArray("replacementids");
                    String linkedTransaction = "";

                    for (int i = 0; i < replacementids.length(); i++) {
                        String replacementid = (String) replacementids.get(i);
                        if (!StringUtil.isNullOrEmpty(replacementid)) {
                            Object obj = accountingHandlerDAOobj.getObject(ProductReplacement.class.getName(), replacementid);
                            if (obj != null) {
                                ProductReplacement prodReplace = (ProductReplacement) obj;
                                List list = accSalesOrderDAOobj.deleteProductReplacement(companyID, replacementid, prodReplace.getReplacementRequestNumber());

                                if (list.get(0) != null && list.get(0) != "") {
                                    linkedTransaction += (String) list.get(0) + ", ";
                                }

                                if (list.get(1) != null && list.get(1) != "") {
                                    deletedTransaction += (String) list.get(1) + ",";
                                }
                            } else {
                                linkedTransaction += replacementid + ", ";
                            }
                        }
                    }

                    if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                        msg = "Replacement(s) has been deleted successfully";
                    } else {
                        msg = "Replacement(s) except " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " has been deleted successfully.";
                    }
                } else {
                    result.put("success", false);
                    result.put("infocode", "m02");
                }
            } else {
                result.put("success", false);
                result.put("infocode", "e01");
            }
        } catch (ServiceException ex) {
            successFlag = false;
            msg = "Failed to make Connection with web server";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            successFlag = false;
            msg = "Failed to make Connection with web server";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            JSONObject retObj = new JSONObject();
            try {
                retObj.put("successFlag", successFlag);
                retObj.put("msg", msg);
                retObj.put("deletedTransaction", deletedTransaction);
                JSONArray returnArr = new JSONArray();
                returnArr.put(retObj);
                result.put("data", returnArr);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    public String getAssetDetails(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        boolean issuccess = false;
//        boolean isSerialForProduct = false;
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            JSONArray jArr = new JSONArray();
            JSONObject obj1;
            String companyID = jobj.getString("companyid");

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyID);
            requestParams.put("isFixedAsset", true);
            KwlReturnObject productResult = accProductObj.getProducts(requestParams);
            List list3 = productResult.getEntityList();
            Iterator itr1 = list3.iterator();
            JSONArray jArr1;
            while (itr1.hasNext()) {
                Product p = (Product) itr1.next();
                obj1 = new JSONObject();
                obj1.put("assetgroupname", p.getName());
                obj1.put("assetgroupid", p.getID());

                HashMap<String, Object> assetParams = new HashMap<>();
                assetParams.put("companyId", companyID);
                assetParams.put("invrecord", true);
                assetParams.put("productId", p.getID());
                KwlReturnObject assetDetailsResult = accProductObj.getAssetDetails(assetParams);
                List list = assetDetailsResult.getEntityList();
                Iterator itr = list.iterator();
                JSONObject obj;
                jArr1 = new JSONArray();
                while (itr.hasNext()) {
                    AssetDetails ad = (AssetDetails) itr.next();
                    obj = new JSONObject();
                    obj.put("assetid", ad.getId());
                    obj.put("assetname", ad.getAssetId());
                    obj.put("assetquantity", 1);
                    String batchId = ad.getBatch() != null ? ad.getBatch().getId() : "";
                    if (p.isIsSerialForProduct()) {
                        ArrayList param3 = new ArrayList();
                        param3.add(companyID);
                        param3.add(batchId);
                        /*
                         * String query1 = "from BatchSerial bs Where
                         * bs.company.companyID=? and bs.batch.id=?"; // get
                         * serial no's for asset id's List list1 =
                         * HibernateUtil.executeQuery(session, query1,
                         * param3.toArray()); Iterator iter = list1.iterator();
                         *
                         * Iterator iter = batchserialdetails.iterator();
                         */
                        List list1 = new ArrayList();
                        if (!p.isIsBatchForProduct() && !p.isIslocationforproduct() && !p.isIswarehouseforproduct() && p.isIsSerialForProduct()) {
//                            list1 = getOnlySerialDetails(ad.getId(), session);
                            list1 = accCommonTablesDAO.getOnlySerialDetailsForRemoteAPI(ad.getId());
                        } else {
//                            list1 = getBatchSerialDetails(ad.getId(), !p.isIsSerialForProduct(), session);
                            list1 = accCommonTablesDAO.getBatchSerialDetailsForRemoteAPI(ad.getId(), !p.isIsSerialForProduct());
                        }

                        Iterator iter = list1.iterator();
                        while (iter.hasNext()) {
                            Object[] objArr = (Object[]) iter.next();
                            obj.put("assetserialnoid", objArr[7] != null ? (String) objArr[7] : "");
                            obj.put("assetserialno", objArr[8] != null ? (String) objArr[8] : "");
                        }
                        /*
                         * while (iter.hasNext()) { BatchSerial batchSerial =
                         * (BatchSerial) iter.next(); obj.put("assetserialnoid",
                         * batchSerial.getId()); obj.put("assetserialno",
                         * batchSerial.getName()); }
                         */
                        if (list1.isEmpty()) {
                            obj.put("assetserialnoid", "");
                            obj.put("assetserialno", "N/A");
                        }

                    } else {
                        obj.put("assetserialnoid", "");
                        obj.put("assetserialno", "N/A");
                    }
                    jArr1.put(obj);
                }
                obj1.put("assetgroupmembers", jArr1);

                jArr.put(obj1);
            }
            result.put("data", jArr);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of asset details ", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    public String getAssetInformation(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        boolean issuccess = false;

        try {
            JSONArray jArr = new JSONArray();;
            JSONObject jobject = new JSONObject(request.getParameter("data"));

            String companyID = jobject.getString("companyid");
            JSONArray assetIds = jobject.getJSONArray("assetids");
            JSONObject jobj;

            for (int i = 0; i < assetIds.length(); i++) {
                String aid = (String) assetIds.get(i);
                if (!StringUtil.isNullOrEmpty(aid)) {
                    String qno = "";
//                    Object obj = session.get(AssetDetails.class, aid);
                    Object obj = accountingHandlerDAOobj.getObject(AssetDetails.class.getName(), aid);
                    jobj = new JSONObject();
                    if (obj != null) {
                        AssetDetails ad = (AssetDetails) obj;
                        Product p = ad.getProduct();
                        jobj.put("assetid", ad.getId());
                        jobj.put("assetname", ad.getAssetId());
                        jobj.put("assetquantity", 1);
                        jobj.put("assetgroupname", ad.getProduct().getName());
                        jobj.put("assetgroupid", ad.getProduct().getID());
                        String batchId = ad.getBatch() != null ? ad.getBatch().getId() : "";
                        if (!StringUtil.isNullOrEmpty(batchId)) {
                            ArrayList param3 = new ArrayList();
                            param3.add(companyID);
                            param3.add(batchId);
                            /*
                             * String query1 = "from BatchSerial bs Where
                             * bs.company.companyID=? and bs.batch.id=?"; // get
                             * serial no's for asset id's List list1 =
                             * HibernateUtil.executeQuery(session, query1,
                             * param3.toArray()); Iterator iter =
                             * list1.iterator(); while (iter.hasNext()) {
                             * BatchSerial batchSerial = (BatchSerial)
                             * iter.next(); jobj.put("assetserialnoid",
                             * batchSerial.getId()); jobj.put("assetserialno",
                             * batchSerial.getName()); }
                             */

                            List list1 = new ArrayList();
                            if (!p.isIsBatchForProduct() && !p.isIslocationforproduct() && !p.isIswarehouseforproduct() && p.isIsSerialForProduct()) {
//                                list1 = getOnlySerialDetails(ad.getId(), session);
                                list1 = accCommonTablesDAO.getOnlySerialDetailsForRemoteAPI(ad.getId());
                            } else {
//                                list1 = getBatchSerialDetails(ad.getId(), !p.isIsSerialForProduct(), session);
                                list1 = accCommonTablesDAO.getBatchSerialDetailsForRemoteAPI(ad.getId(), !p.isIsSerialForProduct());
                            }

                            Iterator iter = list1.iterator();
                            while (iter.hasNext()) {
                                Object[] objArr = (Object[]) iter.next();
                                jobj.put("assetserialnoid", objArr[7] != null ? (String) objArr[7] : "");
                                jobj.put("assetserialno", objArr[8] != null ? (String) objArr[8] : "");
                            }

                            if (list1.isEmpty()) {
                                jobj.put("assetserialnoid", "");
                                jobj.put("assetserialno", "N/A");
                            }

                        } else {
                            jobj.put("assetserialnoid", "");
                            jobj.put("assetserialno", "N/A");
                        }
                    }
                    jArr.put(jobj);
                }
            }

            result.put("data", jArr);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of asset details ", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    /*
     * Product Maintenance Request is coming from CRM side
     */
    private String saveProductMaintenanceRequest(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        boolean successFlag = true;
        String msg = "";
        try {
            result.put("success", true);

            JSONObject jobject = new JSONObject(request.getParameter("data"));
            String comp = isCompanyExists(jobject);

            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {
                boolean flag = isCompanyActivated(jobject);
                if (flag) {
                    String companyID = jobject.getString("companyid");
                    KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
                    Company company = (Company) companyResult.getEntityList().get(0);

                    String maintenanceNumber = jobject.getString("maintenanceNumber");
                    String maintainanceid = jobject.getString("maintainanceid");
                    double maintainanceamt = jobject.getDouble("maintainanceamt");

                    if (!StringUtil.isNullOrEmpty(maintainanceid)) {
                        List list = accSalesOrderDAOobj.deleteProductMaintenence(companyID, maintainanceid, maintenanceNumber);
                        if (!list.isEmpty()) {
                            String linkedTransaction = (list.get(0) != null) ? (String) list.get(0) : "";

                            if (!StringUtil.isNullOrEmpty(linkedTransaction)) {
                                throw new AccountingException("Maintenanace number " + maintenanceNumber + " is linked with transaction. So cannot be edited.");
                            }
                        }
                    }

                    // Check whether Replacement Number exist or not

                    List ll = accSalesOrderDAOobj.getProductMaintenanceByReplacementNumber(maintenanceNumber, companyID);

                    if (!ll.isEmpty()) {
                        throw new AccountingException("Maintenance Number you have entered is already available in our database, please enter another number");
                    }

                    String contractId = jobject.getString("contractid");

                    KwlReturnObject contractResult = accountingHandlerDAOobj.getObject(Contract.class.getName(), contractId);
                    Contract contract = (Contract) contractResult.getEntityList().get(0);

                    String customertId = jobject.getString("accountid");

                    ArrayList custParams = new ArrayList();
                    custParams.add(companyID);
                    custParams.add(customertId);

                    KwlReturnObject resultcheck = accCustomerDAOobj.checkCustomerExist(customertId, companyID);
                    List custList = resultcheck.getEntityList();
                    Iterator custItr = custList.iterator();
                    Customer customer = null;
                    while (custItr.hasNext()) {
                        customer = (Customer) custItr.next();
                    }

                    HashMap<String, Object> replacementMap = new HashMap<String, Object>();
                    replacementMap.put("maintainanceid", maintainanceid);
                    replacementMap.put("maintenanceNumber", maintenanceNumber);
                    replacementMap.put("maintainanceamt", maintainanceamt);
                    replacementMap.put("customerId", customer.getID());
                    replacementMap.put("contractId", contract.getID());
                    replacementMap.put("isSalesContractMaintenance", contract.isNormalContract());
                    replacementMap.put("companyId", company.getCompanyID());

                    KwlReturnObject maintainanceResult = accSalesOrderDAOobj.saveProductMaintenance(replacementMap);
                    Maintenance maintenance = (Maintenance) maintainanceResult.getEntityList().get(0);

                    msg = "Maintenance Request " + maintenance.getMaintenanceNumber() + " has been saved successfully.";

                } else {
                    result.put("success", false);
                    result.put("infocode", "m02");
                }
            } else {
                result.put("success", false);
                result.put("infocode", "e01");
            }
        } catch (ServiceException ex) {
            successFlag = false;
            msg = "Failed to make Connection with web server";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            successFlag = false;
            msg = "Failed to make Connection with web server";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (AccountingException exception) {
            successFlag = false;
            msg = exception.getMessage();
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, exception.getMessage(), exception);
        } finally {
            JSONObject retObj = new JSONObject();
            try {
                retObj.put("successFlag", successFlag);
                retObj.put("msg", msg);
                JSONArray returnArr = new JSONArray();
                returnArr.put(retObj);
                result.put("data", returnArr);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    private String deleteProductMaintenanceRequest(HttpServletRequest request) throws AccountingException {
        JSONObject result = new JSONObject();
        boolean successFlag = true;
        String msg = "";
        String deletedTransaction = "";
        try {
            result.put("success", true);

            JSONObject jobject = new JSONObject(request.getParameter("data"));
            String comp = isCompanyExists(jobject);

            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {
                boolean flag = isCompanyActivated(jobject);
                if (flag) {
                    String companyID = jobject.getString("companyid");
                    JSONArray maintainanceids = jobject.getJSONArray("maintainanceids");
                    String linkedTransaction = "";

                    for (int i = 0; i < maintainanceids.length(); i++) {
                        String maintainanceid = (String) maintainanceids.get(i);
                        if (!StringUtil.isNullOrEmpty(maintainanceid)) {
//                            Object obj = session.get(Maintenance.class, maintainanceid);
                            Object obj = accountingHandlerDAOobj.getObject(Maintenance.class.getName(), maintainanceid);
                            if (obj != null) {
                                Maintenance maintenance = (Maintenance) obj;
                                List list = accSalesOrderDAOobj.deleteProductMaintenence(companyID, maintainanceid, maintenance.getMaintenanceNumber());
                                if (list.get(0) != null && list.get(0) != "") {
                                    linkedTransaction += (String) list.get(0) + ", ";
                                }

                                if (list.get(1) != null && list.get(1) != "") {
                                    deletedTransaction += (String) list.get(1) + ",";
                                }
                            } else {
                                linkedTransaction += maintainanceid + ", ";
                            }
                        }
                    }

                    if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                        msg = "Maintenance(s) has been deleted successfully";
                    } else {
                        msg = "Maintenance(s) except " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " has been deleted successfully.";
                    }
                } else {
                    result.put("success", false);
                    result.put("infocode", "m02");
                }
            } else {
                result.put("success", false);
                result.put("infocode", "e01");
            }
        } catch (ServiceException ex) {
            successFlag = false;
            msg = "Failed to make Connection with web server";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            successFlag = false;
            msg = "Failed to make Connection with web server";
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            JSONObject retObj = new JSONObject();
            try {
                retObj.put("successFlag", successFlag);
                retObj.put("msg", msg);
                retObj.put("deletedTransaction", deletedTransaction);
                JSONArray returnArr = new JSONArray();
                returnArr.put(retObj);
                result.put("data", returnArr);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

//    private String saveProduct(HttpServletRequest request) throws SQLException, ServiceException {
//        JSONObject result = new JSONObject();
//        try {
//            result.put("success", true);
//            result.put("infocode", "m302");
//            JSONObject jobject = new JSONObject(request.getParameter("data"));
//            Date newUserDate = new Date();
//            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), jobject.getString("companyid"));
//            Company company1 = (Company) companyResult.getEntityList().get(0);
//            if (company1.getCreator() != null) {
//                newUserDate = authHandler.getUserNewDate(null, company1.getCreator().getTimeZone()!=null?company1.getCreator().getTimeZone().getDifference() : company1.getTimeZone().getDifference());
//            }
//            String comp = isCompanyExists(jobject);
//            JSONObject cj = new JSONObject(comp);
//            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {
//                boolean flag = isCompanyActivated(jobject);
//                if (flag) {
//                    String companyID = jobject.getString("companyid");
//                    KwlReturnObject comResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
//                    Company company = (Company) comResult.getEntityList().get(0);
//                    KWLCurrency baseCurrency = company.getCurrency();
//                    SequenceFormat invSeqFormat = null;
//                    SequenceFormat jeSeqFormat = null;
//                    int maxnumber = 0;
////                    String modulename = "autoinvoice";
////                    Map<String, Object> sfrequestParams = new HashMap<>();
////                    sfrequestParams.put("companyid", companyID);
////                    sfrequestParams.put("modulename", modulename);
////                    KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
////                    List<SequenceFormat> ll = seqFormatResult.getEntityList();
////                    if (ll.isEmpty()) {
////                        HashMap<String, Object> requestParams = new HashMap<>();
////                        requestParams.put("modulename", modulename);
////                        requestParams.put("numberofdigit", 6);
////                        requestParams.put("showleadingzero", true);
////                        requestParams.put("prefix", Constants.CI_DEFAULT_PREFIX);
////                        requestParams.put("sufix", "");
////                        requestParams.put("startfrom", 0);
////                        requestParams.put("name", Constants.CI_DEFAULT_FORMAT);
////                        requestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
////                        requestParams.put("companyid", companyID);
////                        invSeqFormat = accCompanyPreferencesObj.saveSequenceFormat(requestParams);
////                        
////                    } else if (ll.get(0) != null) {
////                        SequenceFormat format = (SequenceFormat) ll.get(0);
////                        String sequenceformatid = format.getID();
////                        String[] nextAutoNumberTemp = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyID, StaticValues.AUTONUM_INVOICE, sequenceformatid, false);
////                        if (nextAutoNumberTemp != null) {
////                            maxnumber = Integer.parseInt(nextAutoNumberTemp[1].toString());
////                        } else {
////                            maxnumber = 0;
////                        }
////                        invSeqFormat = format;
////                        invSeqFormat.setStartfrom(maxnumber);
////                    }
//                    
//                    String modulename = "autojournalentry";
//                    Map<String, Object> sfrequestParams = new HashMap<>();
//                    sfrequestParams.put("companyid", companyID);
//                    sfrequestParams.put("modulename", modulename);
//                    KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
//                    List ll = seqFormatResult.getEntityList();
//                    if (ll.isEmpty()) {
//                        HashMap<String, Object> requestParams = new HashMap<>();
//                        requestParams.put("modulename", modulename);
//                        requestParams.put("numberofdigit", 6);
//                        requestParams.put("showleadingzero", true);
//                        requestParams.put("prefix", Constants.JE_DEFAULT_PREFIX);
//                        requestParams.put("sufix", "");
//                        requestParams.put("startfrom", 0);
//                        requestParams.put("name", Constants.JE_DEFAULT_FORMAT);
//                        requestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
//                        requestParams.put("companyid", companyID);
//                        jeSeqFormat = accCompanyPreferencesObj.saveSequenceFormat(requestParams);
//
//                    } else if (ll.get(0) != null) {
//                        SequenceFormat format = (SequenceFormat) ll.get(0);
//                        String sequenceformatid = format.getID();
//                        String[] nextAutoNumberTemp = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyID, StaticValues.AUTONUM_JOURNALENTRY, sequenceformatid, false);
//                        if (nextAutoNumberTemp != null) {
//                            maxnumber = Integer.parseInt(nextAutoNumberTemp[1].toString());
//                        } else {
//                            maxnumber = 0;
//                        }
//                        jeSeqFormat = format;
//                        jeSeqFormat.setStartfrom(maxnumber);
//                    }
//
//                    if (jobject.getString("data") != null) {
//                        KwlReturnObject currencyResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), jobject.getString("currencyid"));
//                        KWLCurrency currency = (KWLCurrency) currencyResult.getEntityList().get(0);
//                        String currencyid = currency.getCurrencyID();
//                        HashMap<String, List<InvoiceProductData>> checkNumberWiseProductGroup = new HashMap<String, List<InvoiceProductData>>();
//                        String customerName = jobject.isNull("customerName") ? "" : jobject.getString("customerName");
//                        String vendorName = "";
//                        if (!StringUtil.isNullOrEmpty(customerName) && customerName.contains("Customer")) {
//                            vendorName = customerName.replace("Customer", "Vendor");
//                        }
//                        int fromInventory = jobject.isNull("fromInventory") ? 0 : Integer.parseInt(jobject.getString("fromInventory"));
//                        JSONArray jArr = jobject.getJSONArray("data");
//                        for (int i = 0; i < jArr.length(); i++) {
//                            try {
//                                JSONObject jobj = jArr.getJSONObject(i);
//                                String productid = jobj.isNull("ItemCode") ? "" : jobj.getString("ItemCode");
//                                String productname = jobj.isNull("productName") ? "" : jobj.getString("productName");
//                                double salePrice = jobj.isNull("salePrice") ? 0.0 : Double.parseDouble(jobj.getString("salePrice"));
//                                double purchasePrice = jobj.isNull("purchasePrice") ? 0.0 : Double.parseDouble(jobj.getString("purchasePrice"));
//                                double totalTax = jobj.isNull("totalTax") ? 0.0 : Double.parseDouble(jobj.getString("totalTax"));
//                                double totalDiscount = jobj.isNull("totalDiscount") ? 0.0 : Double.parseDouble(jobj.getString("totalDiscount"));
//                                double productquantity = jobj.isNull("productQuantity") ? 0 : Double.parseDouble(jobj.getString("productQuantity"));
//                                double actualquantity = jobj.isNull("actualQuantity") ? 0 : Double.parseDouble(jobj.getString("actualQuantity"));
//                                String checknumber = jobj.isNull("checkNumber") ? "" : jobj.getString("checkNumber");
//                                Date invoiceCreationDate = null;
//                                try {
//                                    DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
//                                    if (jobj.isNull("BusinessDate")) {
//                                        invoiceCreationDate = new Date();
//                                    } else {
//                                        invoiceCreationDate = df.parse(jobj.getString("BusinessDate"));
//                                    }
//                                } catch (ParseException parseException) {
//                                    invoiceCreationDate = new Date();
//                                } catch (Exception exception) {
//                                    invoiceCreationDate = new Date();
//                                }
//                          
//                                Product product = null;
//
//                                KwlReturnObject results = accProductObj.getProduct(productid, companyID);
//                                List list = results.getEntityList();
//                                if(list.isEmpty()){
//                                    
//                                    HashMap<String, Object> requestParams = new HashMap<>();
//                                    requestParams.put("companyid", companyID);
//                                    requestParams.put("productType", Producttype.INVENTORY_PART_Name);
//
//                                    DefaultsForProduct defaultsForProduct = accProductObj.getDefaultAccountsForProduct(requestParams);
//                                    UnitOfMeasure uom = defaultsForProduct.getUnitOfMeasure();
//                                    Producttype ptype = defaultsForProduct.getProducttype();
//                                    Account paccount = defaultsForProduct.getPaccount();
//                                    Account saccount = defaultsForProduct.getSaccount();
//
//                                    requestParams.clear();//cleared all data to reuse the variable
//                                    requestParams.put("producttype", ptype.getID());
//                                    requestParams.put("name", productname);
//                                    requestParams.put("productid", productid);
//                                    requestParams.put("syncable", false);
//                                    requestParams.put("uomid", uom.getID());
//                                    requestParams.put("purchaseaccountid", paccount.getID());
//                                    requestParams.put("purchaseretaccountid", paccount.getID());
//                                    requestParams.put("salesaccountid", saccount.getID());
//                                    requestParams.put("salesretaccountid", saccount.getID());
//                                    requestParams.put("leadtime", 0);
//                                    requestParams.put("warrantyperiod", 0);
//                                    requestParams.put("warrantyperiodsal", 0);
//                                    requestParams.put("reorderlevel", 0d);
//                                    requestParams.put("reorderquantity", 0d);
//                                    requestParams.put("deletedflag", false);
//                                    requestParams.put("companyid", companyID);
//                                    requestParams.put("currencyid", baseCurrency.getCurrencyID());
//                                    results = accProductObj.addProduct(requestParams);
//                                    product = (Product) results.getEntityList().get(0);
//                                    String productId = product.getID();
//                                    
//                                    JSONObject inventoryjson = new JSONObject();
//                                    inventoryjson.put("productid", product.getID());
//                                    inventoryjson.put("quantity", 0);
//                                    inventoryjson.put("baseuomquantity", 0);
//                                    inventoryjson.put("baseuomrate", 1);
//                                    inventoryjson.put("carryin", true);
//                                    inventoryjson.put("defective", false);
//                                    inventoryjson.put("newinventory", true);
//                                    inventoryjson.put("companyid", company.getCompanyID());
//                                    inventoryjson.put("updatedate", newUserDate);
//                                    accProductObj.addInventory(inventoryjson);
//                                    
//                                    requestParams.clear();//cleared all data to reuse the variable
//                                    requestParams.put("productid", productId);
//                                    requestParams.put("carryin", true);
//                                    requestParams.put("applydate", new Date());
//                                    requestParams.put("price", purchasePrice);
//                                    requestParams.put("affecteduser", "-1");
//                                    requestParams.put("companyid", companyID);
//                                    requestParams.put("currencyid", baseCurrency.getCurrencyID());
//                                    accProductObj.addPriceList(requestParams);
//
//                                    requestParams.clear();//cleared all data to reuse the variable
//                                    requestParams.put("productid", productId);
//                                    requestParams.put("carryin", false);
//                                    requestParams.put("applydate", new Date());
//                                    requestParams.put("price", 0d);
//                                    requestParams.put("affecteduser", "-1");
//                                    requestParams.put("companyid", companyID);
//                                    requestParams.put("currencyid", baseCurrency.getCurrencyID());
//                                    accProductObj.addPriceList(requestParams);
//
//                                } else {
//                                    Iterator itr = list.iterator();
//                                    while (itr.hasNext()) {
//                                        product = (Product) itr.next();
//                                    }
//
//                                    JSONObject inventoryjson = new JSONObject();
//                                    inventoryjson.put("productid", product.getID());
//                                    inventoryjson.put("quantity", productquantity);
//                                    inventoryjson.put("baseuomquantity", productquantity);
//                                    inventoryjson.put("carryin", true);
//                                    inventoryjson.put("defective", false);
//                                    inventoryjson.put("newinventory", true);
//                                    inventoryjson.put("companyid", company.getCompanyID());
//                                    inventoryjson.put("updatedate", newUserDate);
//                                    accProductObj.addInventory(inventoryjson);
//
//                                    HashMap<String, Object> requestParams = new HashMap<>();
//                                    requestParams.put("productid", product.getID());
//                                    requestParams.put("carryin", true);
//                                    requestParams.put("applydate", new Date());
//                                    requestParams.put("price", purchasePrice);
//                                    requestParams.put("affecteduser", "-1");
//                                    requestParams.put("companyid", companyID);
//                                    requestParams.put("currencyid", baseCurrency.getCurrencyID());
//                                    accProductObj.addPriceList(requestParams);
//
//                                    requestParams.clear();//cleared all data to reuse the variable
//                                    requestParams.put("productid", product.getID());
//                                    requestParams.put("carryin", false);
//                                    requestParams.put("applydate", new Date());
//                                    requestParams.put("price", 0d);
//                                    requestParams.put("affecteduser", "-1");
//                                    requestParams.put("companyid", companyID);
//                                    requestParams.put("currencyid", baseCurrency.getCurrencyID());
//                                    accProductObj.addPriceList(requestParams);
//                                }
//
//                                if (checkNumberWiseProductGroup.containsKey(checknumber)) {
//                                    List<InvoiceProductData> invoiceProductsData = checkNumberWiseProductGroup.get(checknumber);
//                                    InvoiceProductData invoiceProductData = new InvoiceProductData();
//                                    invoiceProductData.setProductID(productid);
//                                    invoiceProductData.setProductName(productname);
//                                    invoiceProductData.setProductPrice(salePrice);
//                                    invoiceProductData.setFromInventory(fromInventory);
//                                    invoiceProductData.setProductPurchasePrice(purchasePrice);
//                                    invoiceProductData.setProductQuantity(productquantity);
//                                    invoiceProductData.setActualQuantity(actualquantity);
//                                    invoiceProductData.setChecknumber(checknumber);
//                                    invoiceProductData.setCustomerName(customerName);
//                                    invoiceProductData.setBusinessDate(invoiceCreationDate);
//                                    invoiceProductData.setTotalTax(totalTax);
//                                    invoiceProductData.setTotalDiscount(totalDiscount);
//                                    invoiceProductData.setProduct(product);
//                                    invoiceProductsData.add(invoiceProductData);
//                                } else {
//                                    List<InvoiceProductData> invoiceProductsData = new ArrayList<InvoiceProductData>();
//                                    InvoiceProductData invoiceProductData = new InvoiceProductData();
//                                    invoiceProductData.setProductID(productid);
//                                    invoiceProductData.setProductName(productname);
//                                    invoiceProductData.setProductPrice(salePrice);
//                                    invoiceProductData.setProductPurchasePrice(purchasePrice);
//                                    invoiceProductData.setFromInventory(fromInventory);
//                                    invoiceProductData.setBusinessDate(invoiceCreationDate);
//                                    invoiceProductData.setTotalTax(totalTax);
//                                    invoiceProductData.setTotalDiscount(totalDiscount);
//                                    invoiceProductData.setProductQuantity(productquantity);
//                                    invoiceProductData.setActualQuantity(actualquantity);
//                                    invoiceProductData.setChecknumber(checknumber);
//                                    invoiceProductData.setCustomerName(customerName);
//                                    invoiceProductData.setProduct(product);
//                                    invoiceProductsData.add(invoiceProductData);
//                                    checkNumberWiseProductGroup.put(checknumber, invoiceProductsData);
//                                }
//
//                            } catch (JSONException | NumberFormatException | ServiceException ex) {
//                                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Product creation in Accounting", ex.getMessage());
//                                jArr.getJSONObject(i).put("successFlag", false);
//                            }
//                            jArr.getJSONObject(i).put("successFlag", true);
//
//                        }
////                        saveInvoice(companyID, company, currencyid, currency, cap, customerName, vendorName, checkNumberWiseProductGroup, jeSeqFormat, invSeqFormat);
//                        result.put("data", jArr);
//
//                    }
//                } else {
//                    result.put("success", false);
//                    result.put("infocode", "m02");
//                }
//            } else {
//                result.put("success", false);
//                result.put("infocode", "e01");
//            }
//
//        } catch (ServiceException ex) {
//            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Service Exception while creating hrms Salary JE", ex);
//            throw ServiceException.FAILURE(result.toString(), ex);
//        } catch (Exception ex) {
//            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "JSON Exception while creating hrms Salary JE", ex);
//            throw ServiceException.FAILURE(result.toString(), ex);
//        }
//        return result.toString();
//    }
    private String saveProductFromLMS(HttpServletRequest request) throws SQLException, ServiceException {
        JSONObject result = new JSONObject();
        try {
            result.put("success", true);
            result.put("infocode", "m302");
            JSONObject jobject = new JSONObject(request.getParameter("data"));
            String comp = isCompanyExists(jobject);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {
                boolean flag = isCompanyActivated(jobject);
                if (flag) {
                    String companyID = jobject.getString("companyid");
                    Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyID);
                    KWLCurrency baseCurrency = company.getCurrency();

                    Date productDate = authHandler.getConstantDateFormatter().parse(authHandler.getConstantDateFormatter().format(new Date()));
                    if (jobject.getString("data") != null) {
                        JSONArray jArr = jobject.getJSONArray("data");
                        for (int i = 0; i < jArr.length(); i++) {
                            try {
                                JSONObject jobj = jArr.getJSONObject(i);
                                String productid = jobj.isNull("productCode") ? "" : jobj.getString("productCode");
                                String productname = jobj.isNull("productName") ? "" : jobj.getString("productName");
                                String productdescription = jobj.isNull("description") ? "" : jobj.getString("description");
                                double salePrice = jobj.isNull("salePrice") ? 0.0 : Double.parseDouble(jobj.getString("salePrice"));
                                double purchasePrice = jobj.isNull("purchasePrice") ? 0.0 : Double.parseDouble(jobj.getString("purchasePrice"));

                                HashMap<String, Object> requestParams = new HashMap<>();
                                KwlReturnObject results = accProductObj.getProduct(productid, companyID);
                                List list = results.getEntityList();
                                Product product = null;
                                if (list.isEmpty()) {
                                    requestParams.put("companyid", companyID);
                                    requestParams.put("productType", Producttype.SERVICE_Name);

                                    DefaultsForProduct defaultsForProduct = accProductObj.getDefaultAccountsForProduct(requestParams);
                                    Producttype ptype = defaultsForProduct.getProducttype();
                                    Account paccount = defaultsForProduct.getPaccount();
                                    Account saccount = defaultsForProduct.getSaccount();

                                    requestParams.clear();//cleared all data to reuse the variable
                                    requestParams.put("producttype", ptype.getID());
                                    requestParams.put("name", productname);
                                    requestParams.put("desc", productdescription);
                                    requestParams.put("productid", productid);
                                    requestParams.put("syncable", true);
                                    requestParams.put("purchaseaccountid", paccount.getID());
                                    requestParams.put("purchaseretaccountid", paccount.getID());
                                    requestParams.put("salesaccountid", saccount.getID());
                                    requestParams.put("salesretaccountid", saccount.getID());
                                    requestParams.put("leadtime", 0);
                                    requestParams.put("warrantyperiod", -1);
                                    requestParams.put("warrantyperiodsal", -1);
                                    requestParams.put("reorderlevel", 0d);
                                    requestParams.put("reorderquantity", 0d);
                                    requestParams.put("deletedflag", false);
                                    requestParams.put("companyid", companyID);
                                    requestParams.put("asOfDate", productDate);
                                    UnitOfMeasure uom = defaultsForProduct.getUnitOfMeasure();
                                    requestParams.put("uomid", uom.getID());
//                                    product.setID(productid);
                                    results = accProductObj.addProduct(requestParams);
                                    product = (Product) results.getEntityList().get(0);//get the inserted product
                                    String productId = product.getID();

                                    requestParams.clear();//cleared all data to reuse the variable
                                    requestParams.put("productid", productId);
                                    requestParams.put("carryin", true);
                                    requestParams.put("applydate", productDate);
                                    requestParams.put("price", purchasePrice);
                                    requestParams.put("affecteduser", "-1");
                                    requestParams.put("companyid", companyID);
                                    requestParams.put("currencyid", baseCurrency.getCurrencyID());
                                    requestParams.put("uomid", product.getUnitOfMeasure().getID());
                                    accProductObj.addPriceList(requestParams);

                                    requestParams.clear();//cleared all data to reuse the variable
                                    requestParams.put("productid", productId);
                                    requestParams.put("carryin", false);
                                    requestParams.put("applydate", productDate);
                                    requestParams.put("price", 0d);
                                    requestParams.put("affecteduser", "-1");
                                    requestParams.put("companyid", companyID);
                                    requestParams.put("currencyid", baseCurrency.getCurrencyID());
                                    requestParams.put("uomid", product.getUnitOfMeasure().getID());
                                    accProductObj.addPriceList(requestParams);
                                } else {
                                    product = (Product) list.get(0);//get the first product
                                    String productId = product.getID();

                                    requestParams.put("productid", productId);
                                    requestParams.put("carryin", true);
                                    requestParams.put("applydate", productDate);
                                    requestParams.put("price", purchasePrice);
                                    requestParams.put("affecteduser", "-1");
                                    requestParams.put("companyid", companyID);
                                    requestParams.put("currencyid", baseCurrency.getCurrencyID());
                                    requestParams.put("uomid", product.getUnitOfMeasure().getID());
                                    accProductObj.addPriceList(requestParams);

                                    requestParams.clear();//cleared all data to reuse the variable
                                    requestParams.put("productid", productId);
                                    requestParams.put("carryin", false);
                                    requestParams.put("applydate", productDate);
                                    requestParams.put("price", salePrice);
                                    requestParams.put("affecteduser", "-1");
                                    requestParams.put("companyid", companyID);
                                    requestParams.put("currencyid", baseCurrency.getCurrencyID());
                                    requestParams.put("uomid", product.getUnitOfMeasure().getID());
                                    accProductObj.addPriceList(requestParams);
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Product creation in Accounting", ex.getMessage());
                                jArr.getJSONObject(i).put("successFlag", false);
                            }
                            jArr.getJSONObject(i).put("successFlag", true);
                        }
                    }
                } else {
                    result.put("success", false);
                    result.put("infocode", "m02");
                }
            } else {
                result.put("success", false);
                result.put("infocode", "e01");
            }
        } catch (ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Service Exception while creating hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } catch (Exception ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "JSON Exception while creating hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        }
        return result.toString();
    }

//    private void saveInvoice(Session session, String companyid, Company company, String currencyid, KWLCurrency currency, CompanyAccountPreferences cap, String customerName, String vendorName, HashMap<String, List<InvoiceProductData>> checkNumberWiseProductGroup, SequenceFormat jeSeqFormat, SequenceFormat invSeqFormat) throws SQLException, ServiceException {
//        try {
//            Date newUserDate = new Date();
//            if (company.getCreator() != null) {
//                newUserDate = authHandler.getUserNewDate(null, company.getCreator().getTimeZone()!=null?company.getCreator().getTimeZone().getDifference() : company.getTimeZone().getDifference());
//            }
//            for (Map.Entry<String, List<InvoiceProductData>> entry : checkNumberWiseProductGroup.entrySet()) {
//                try {
//                    String key = entry.getKey();
//                    double totalSaleAmount = 0.0, totalPurchaseAmount = 0.0, totalDiscountAmount = 0.0, totalSaleRowAmount = 0.0;
//                    Invoice invoice = new Invoice();
//                    String hql = " from Customer where name = ? and company.companyID = ? ";
//                    List list = HibernateUtil.executeQuery(session, hql, new Object[]{customerName, companyid});
//
//                    Iterator itr = list.iterator();
//                    Customer customer = null;
//                    while (itr.hasNext()) {
//                        customer = (Customer) itr.next();
//                    }
//
//                    hql = " from Vendor where name = ? and company.companyID = ? ";
//                    list = HibernateUtil.executeQuery(session, hql, new Object[]{vendorName, companyid});
//                    itr = list.iterator();
//                    Vendor vendor = null;
//                    while (itr.hasNext()) {
//                        vendor = (Vendor) itr.next();
//                    }
//                    hql = " from Projreport_Template where tempname = 'Basic Template'";
//                    list = HibernateUtil.executeQuery(session, hql, new Object[]{});
//                    itr = list.iterator();
//                    Projreport_Template projreport_Template = null;
//                    while (itr.hasNext()) {
//                        projreport_Template = (Projreport_Template) itr.next();
//                    }
//
//                    hql = " from UnitOfMeasure where company.companyID=? and  name = 'Unit'"; //By Default 'Unit'  
//                    list = HibernateUtil.executeQuery(session, hql, new Object[]{companyid});
//                    itr = list.iterator();
//                    UnitOfMeasure uomObj = null;
//                    while (itr.hasNext()) {
//                        uomObj = (UnitOfMeasure) itr.next();
//                    }
//
//
//                    jeSeqFormat.setStartfrom(jeSeqFormat.getStartfrom() + 1);//here need to increment startfrom because it gives next number
//                    invSeqFormat.setStartfrom(invSeqFormat.getStartfrom() + 1);
//
//                    int nextNumber = invSeqFormat.getStartfrom();
//                    int numberofdigit = invSeqFormat.getNumberofdigit();
//                    boolean showleadingzero = invSeqFormat.isShowleadingzero();
//                    String prefix = invSeqFormat.getPrefix() != null ? invSeqFormat.getPrefix() : "";
//                    String suffix = invSeqFormat.getSuffix() != null ? invSeqFormat.getSuffix() : "";
//                    String nextNumTemp = nextNumber + "";
//                    if (showleadingzero) {
//                        while (nextNumTemp.length() < numberofdigit) {
//                            nextNumTemp = "0" + nextNumTemp;
//                        }
//                    }
//                    String autoNumber = prefix + nextNumTemp + suffix;
//
//                    invoice.setDeleted(false);
//                    invoice.setInvoiceNumber(autoNumber);
//                    invoice.setSeqnumber(nextNumber);
//                    invoice.setSeqformat(invSeqFormat);
//                    invoice.setAutoGenerated(true);
//                    invoice.setNormalInvoice(true);
////                    invoice.setBillTo((customer != null) ? customer.getBillingAddress() : "");
////                    invoice.setShipTo((customer != null) ? customer.getShippingAddress() : "");    
//
//                    if (entry.getValue().isEmpty()) {
//                        invoice.setMemo("Customer invoice from inventory data");
//                    } else if (entry.getValue().get(0).getFromInventory() == 1) {
//                        invoice.setMemo("Customer invoice from Sales file for " + key);
//                    } else if (entry.getValue().get(0).getFromInventory() == 2) {
//                        invoice.setMemo("Customer invoice from POSLavu for " + key);
//                    }
//
//                    //Projreport_Template projreport_Template = (Projreport_Template) session.get(Projreport_Template.class, "ff8080813ff0605a013ff07200670003");  //By Default Basic Template      
//                    invoice.setTemplateid(projreport_Template);
//                    invoice.setCompany(company);
//                    invoice.setCustomer(customer);
//                    invoice.setCurrency(currency);
//                    invoice.setExternalCurrencyRate(0);
//                    invoice.setInventoryorderid(key);
//                    invoice.setPartialinv(false);
//                    JournalEntry salesJE = postJournalEntry(session, company, currency, cap, "Generated Customer Invoice from Inventory data for " + key, customer.getAccount(), jeSeqFormat);
//                    invoice.setJournalEntry(salesJE);
//                    invoice.setCustomer(customer);
//                    if (entry.getValue().isEmpty()) {
//                        salesJE.setEntryDate(new Date());
//                        invoice.setDueDate(new Date());
//                        invoice.setShipDate(new Date());
//                    } else {
//                        salesJE.setEntryDate(entry.getValue().get(0).getBusinessDate());
//                        invoice.setDueDate(entry.getValue().get(0).getBusinessDate());
//                        invoice.setShipDate(entry.getValue().get(0).getBusinessDate());
//                    }
//                    hql = "select ID from ExchangeRate where fromCurrency.currencyID=? and toCurrency.currencyID=? ";
//                    list = HibernateUtil.executeQuery(session, hql, new Object[]{cap.getCompany().getCurrency().getCurrencyID(), currencyid});
//                    itr = list.iterator();
//                    Iterator erIDitr = list.iterator();
//                    String erid = "";
//                    erid = (String) erIDitr.next();
//
//                    hql = "select max(erd.applyDate) from ExchangeRateDetails erd where erd.company.companyID=? and  erd.exchangeratelink.ID = ? and applyDate <= ?";
//                    list = HibernateUtil.executeQuery(session, hql, new Object[]{companyid, erid, new Date()});
//                    itr = list.iterator();
//                    Date maxDate = (Date) itr.next();
//
//                    hql = "from ExchangeRateDetails erd where erd.applyDate=? and erd.exchangeratelink.ID=? and erd.company.companyID=?";
//                    list = HibernateUtil.executeQuery(session, hql, new Object[]{maxDate, erid, companyid});
//                    itr = list.iterator();
//                    ExchangeRateDetails exchangeRateDetails = (ExchangeRateDetails) itr.next();
//                    invoice.setExchangeRateDetail(exchangeRateDetails);
//                    Set<JournalEntryDetail> salentries = salesJE.getDetails();
//                    for (JournalEntryDetail journalEntryDetail : salentries) {
//                        if (journalEntryDetail.isDebit()) {
//                            invoice.setCustomerEntry(journalEntryDetail);
//                        }
//                    }
//                    session.save(invoice);
//
//                    int i = 0;
//                    Account salesAccount = null, purchaseAccount = null;
//                    HashSet<InvoiceDetail> invdetails = new HashSet<InvoiceDetail>();
//                    List<InvoiceProductData> invoiceProductsData = entry.getValue();
//                    for (InvoiceProductData invoiceProductData : invoiceProductsData) {
//                        if (key.equalsIgnoreCase(invoiceProductData.getChecknumber())) {
//                            try {
//                                Discount discount = new Discount();
//                                InvoiceDetail detail = new InvoiceDetail();
//
//                                Inventory inventory = new Inventory();
//                                inventory.setProduct(invoiceProductData.getProduct());
//                                inventory.setQuantity(invoiceProductData.getProductQuantity());
//                                inventory.setBaseuomquantity(invoiceProductData.getProductQuantity());
//                                inventory.setBaseuomrate(1);
//                                inventory.setUom(uomObj);
//                                inventory.setActquantity(0);
//                                inventory.setInvrecord(true);
//                                inventory.setCarryIn(false);
//                                inventory.setDefective(false);
//                                inventory.setNewInv(false);
//                                inventory.setCompany(company);
//                                inventory.setUpdateDate(newUserDate);
//                                session.save(inventory);
//
//                                // Creating Discount Object for POSLavu
//                                if (invoiceProductData.getTotalDiscount() != 0.0) {
//                                    discount.setDiscount(invoiceProductData.getTotalDiscount());
//                                    discount.setInPercent(false);
//                                    discount.setOriginalAmount(invoiceProductData.getProductPrice() * invoiceProductData.getProductQuantity());
//                                    discount.setCompany(company);
//
//                                    session.save(discount);
//                                    detail.setDiscount(discount);
//                                    totalDiscountAmount = totalDiscountAmount + invoiceProductData.getTotalDiscount();
//                                }
//
//                                detail.setSrno(i++);
//                                detail.setPartamount(0);
//                                detail.setCompany(company);
//                                detail.setInvoice(invoice);
//                                detail.setRate(invoiceProductData.getProductPrice());
//                                detail.setInventory(inventory);
//                                invdetails.add(detail);
//
//                                double saleAmount = (invoiceProductData.getProductPrice() * invoiceProductData.getProductQuantity()) - invoiceProductData.getTotalDiscount();
//                                totalSaleAmount += saleAmount;
//
//                                double saleRowAmount = (invoiceProductData.getProductPrice() * invoiceProductData.getProductQuantity());
//                                totalSaleRowAmount += saleRowAmount;
//
//                                double purchaseAmount = invoiceProductData.getProductPurchasePrice() * invoiceProductData.getProductQuantity();
//                                totalPurchaseAmount += purchaseAmount;
//
//                                salesAccount = invoiceProductData.getProduct().getSalesAccount();
//                                purchaseAccount = invoiceProductData.getProduct().getPurchaseAccount();
//                            } catch (Exception ex) {
//                                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//                            }
//                        }
//                    }
//
////                    JournalEntry purchaseJE= postJournalEntry(session,company,currency,cap,"Generated Purchase JE from Inventory data",vendor.getAccount());                    
////                    Set<JournalEntryDetail> purentryDetails = purchaseJE.getDetails();
////                    for (JournalEntryDetail journalEntryDetail : purentryDetails) {
////                        if (journalEntryDetail.isDebit()) {
////                            journalEntryDetail.setAccount(purchaseAccount);
////                            journalEntryDetail.setAmount(totalPurchaseAmount);                            
////                            session.save(journalEntryDetail);
////                        } else {
////                            journalEntryDetail.setAccount(vendor.getAccount());
////                            journalEntryDetail.setAmount(totalPurchaseAmount);                            
////                            session.save(journalEntryDetail);
////                        }
////                    }
//
//
//                    Set<JournalEntryDetail> salentryDetails = salesJE.getDetails();
//                    for (JournalEntryDetail journalEntryDetail : salentryDetails) {
//                        if (journalEntryDetail.isDebit()) {
//                            journalEntryDetail.setAccount(customer.getAccount());
//                            journalEntryDetail.setAmount(totalSaleAmount);
//                            session.save(journalEntryDetail);
//                        } else {
//                            journalEntryDetail.setAccount(salesAccount);
//                            journalEntryDetail.setAmount(totalSaleRowAmount);
//                            session.save(journalEntryDetail);
//                        }
//                    }
//
//
//                    if (totalDiscountAmount > 0.0) {
//                        String discountJE = UUID.randomUUID().toString();
//                        JournalEntryDetail discountJed = new JournalEntryDetail();
//                        discountJed.setAccount(cap.getDiscountGiven());
//                        discountJed.setAmount(totalDiscountAmount);
//                        discountJed.setDebit(true);
//                        discountJed.setCompany(company);
//                        discountJed.setID(discountJE);
//                        discountJed.setSrno(salesJE.getDetails().size() + 1);
//                        discountJed.setJournalEntry(salesJE);
//                        session.save(discountJed);
//                    }
//
//                    invoice.setRows(invdetails);
//                    session.saveOrUpdate(invoice);
//
//                    //add account's amounts from newly added JE in jedetails_optimized table
//                    saveAccountJEs_optimized(salesJE.getID());
////                    saveAccountJEs_optimized(session, purchaseJE.getID());
//
//                } catch (Exception ex) {
//                    Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//                }
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//            throw ServiceException.FAILURE(ex.getMessage(), ex);
//        }
//    }
//    private JournalEntry postJournalEntry(Session session, Company company, KWLCurrency currency, CompanyAccountPreferences cap, String memo, Account account, SequenceFormat jeSeqFormat) throws SQLException, ServiceException {
//        JournalEntry je = new JournalEntry();
//        try {
//            String jeuuid = UUID.randomUUID().toString();
//            int nextNumber = jeSeqFormat.getStartfrom();
//            int numberofdigit = jeSeqFormat.getNumberofdigit();
//            boolean showleadingzero = jeSeqFormat.isShowleadingzero();
//            String prefix = jeSeqFormat.getPrefix() != null ? jeSeqFormat.getPrefix() : "";
//            String suffix = jeSeqFormat.getSuffix() != null ? jeSeqFormat.getSuffix() : "";
//            String nextNumTemp = nextNumber + "";
//            if (showleadingzero) {
//                while (nextNumTemp.length() < numberofdigit) {
//                    nextNumTemp = "0" + nextNumTemp;
//                }
//            }
//            String autoNumber = prefix + nextNumTemp + suffix;
//
//            je.setCompany(company);
//            je.setAutoGenerated(true);
//            je.setCurrency(currency);
//            je.setDeleted(false);
//            je.setEntryDate(new Date());
//            je.setMemo(memo);
//            je.setID(jeuuid);
//            je.setEntryNumber(autoNumber);
//            je.setSeqnumber(nextNumber);
//            je.setSeqformat(jeSeqFormat);
//            je.setCreatedOn(new Date().getTime());
//            session.save(je);
//
//            String debitje = UUID.randomUUID().toString();
//            JournalEntryDetail debitjed = new JournalEntryDetail();
//            debitjed.setAccount(account);
//            debitjed.setAmount(0);
//            debitjed.setDebit(true);
//            debitjed.setCompany(company);
//            debitjed.setID(debitje);
//            debitjed.setSrno(1);
//            debitjed.setJournalEntry(je);
//            session.save(debitjed);
//
//            String creditje = UUID.randomUUID().toString();
//            JournalEntryDetail creditjed = new JournalEntryDetail();
//            creditjed.setAccount(account);
//            creditjed.setAmount(0);
//            creditjed.setDebit(false);
//            creditjed.setCompany(company);
//            creditjed.setID(creditje);
//            creditjed.setSrno(2);
//            creditjed.setJournalEntry(je);
//            session.save(creditjed);
//
//            Set<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
//            details.add(debitjed);
//            details.add(creditjed);
//            je.setDetails(details);
//        } catch (Exception ex) {
//            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//        }
//        return je;
//    }
    public String getUserList(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
//        ArrayList params = new ArrayList();
        try {
            result.put("success", true);
            result.put("infocode", "m302");
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String comp = isCompanyExists(jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {
                boolean flag = isCompanyActivated(jobj);
                if (flag) {
                    JSONArray jArr = new JSONArray();
                    String companyID = jobj.getString("companyid");

                    KwlReturnObject venResult = accVendorDAOobj.getAllVendorsOfCompany(companyID);
                    int totalCount = venResult.getRecordTotalCount();
                    Iterator itr = venResult.getEntityList().iterator();

                    while (itr.hasNext()) {
                        KwlReturnObject vendorResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), itr.next().toString());
                        Vendor vendor = (Vendor) vendorResult.getEntityList().get(0);
                        
                        JSONObject obj = new JSONObject();
                        obj.put("vendorID", vendor.getID());
                        obj.put("vendorName", vendor.getName());
                        obj.put("vendorAddress", vendor.getAddress() == null ? "" : vendor.getAddress());
                        obj.put("vendorEmail", vendor.getEmail() == null ? "" : vendor.getEmail());
                        obj.put("vendorContactNo", vendor.getContactNumber() == null ? "" : vendor.getContactNumber());
                        obj.put("vendorDebitTerm", vendor.getDebitTerm() == null ? "" : vendor.getDebitTerm());
                        obj.put("vendorFax", vendor.getFax() == null ? "" : vendor.getFax());
                        jArr.put(obj);
                    }
                    result.put("data", jArr);
                    result.put("totalCount", totalCount);
                } else {
                    result.put("success", false);
                    result.put("infocode", "m02");
                }
            } else {
                result.put("success", false);
                result.put("infocode", "e01");
            }


        } catch (ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Service Exception while giving list of users", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "JSON Exception while giving list of users", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        }
        return result.toString();
    }

//    public String saveRoundingDifference(Session session, HttpServletRequest request) throws ServiceException {
//        JSONObject result = new JSONObject();
//        try {
//            result.put("success", true);
//            result.put("infocode", "m302");
//            JSONObject jobj = new JSONObject(request.getParameter("data"));
//            String comp = isCompanyExists(jobj);
//            JSONObject cj = new JSONObject(comp);
//            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {
//                boolean flag = isCompanyActivated(jobj);
//                if (flag) {
//                    String companyID = jobj.getString("companyid");
//                    Company company = (Company) session.get(Company.class, companyID);
//                    CompanyAccountPreferences cap = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, companyID);
//                    if (jobj.getString("data") != null) {
//                        JSONArray jArr = jobj.getJSONArray("data");
//                        for (int i = 0; i < jArr.length(); i++) {
//                            JSONObject roundingObject = jArr.getJSONObject(i);
//                            String orderId = roundingObject.isNull("orderId") ? "" : roundingObject.getString("orderId");
//                            double roundingDifference = roundingObject.isNull("roundingAmount") ? 0.0 : Double.parseDouble(roundingObject.getString("roundingAmount"));
//                            Date startDate = null, endDate = null;
//                            try {
//                                DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                                if (!(roundingObject.isNull("closedDate"))) {
//                                    startDate = dateformat.parse(roundingObject.getString("closedDate"));
//                                    startDate.setHours(0);
//                                    startDate.setMinutes(0);
//                                    startDate.setSeconds(0);
//                                    endDate = dateformat.parse(roundingObject.getString("closedDate"));
//                                    endDate.setHours(23);
//                                    endDate.setMinutes(59);
//                                    endDate.setSeconds(59);
//                                }
//                            } catch (ParseException parseEx) {
//                            } catch (Exception exce) {
//                            }
//
//                            if (!StringUtil.isNullOrEmpty(orderId) && startDate != null && endDate != null) {
//                                ArrayList params = new ArrayList();
//                                params.add(companyID);
//                                params.add(orderId);
//                                params.add(startDate);
//                                params.add(endDate);
//                                String query = "select invoice.id from invoice inner join journalentry on invoice.journalentry=journalentry.id where invoice.company=? and inventoryorderid=? and (journalentry.entrydate >=? and journalentry.entrydate <=?)";
//                                List list = HibernateUtil.executeSQLQuery(session, query, params.toArray());
//
//                                if (!list.isEmpty()) {
//
//                                    String invoiceId = (String) list.get(0);
//                                    Invoice invoice = (Invoice) session.get(Invoice.class, invoiceId);
//                                    if (invoice != null) {
//
//                                        double roundingAmount = 0.0;
//                                        if (roundingDifference > 0) {
//                                            roundingAmount = (-1) * roundingDifference;
//                                        } else {
//                                            roundingAmount = (-1) * roundingDifference;
//                                        }
//
//                                        Set<JournalEntryDetail> salentryDetails = invoice.getJournalEntry().getDetails();
//                                        for (JournalEntryDetail journalEntryDetail : salentryDetails) {
//                                            if (journalEntryDetail.isDebit() && journalEntryDetail.getAccount().equals(invoice.getCustomer().getAccount())) {
//                                                journalEntryDetail.setAmount(journalEntryDetail.getAmount() + roundingDifference);
//                                                session.save(journalEntryDetail);
//                                                jArr.getJSONObject(i).put("successTRflag", true);
//                                            }
//                                        }
//
//                                        if (roundingDifference != 0.0) {
//                                            String roundingJE = UUID.randomUUID().toString();
//                                            JournalEntryDetail roundingJed = new JournalEntryDetail();
//                                            roundingJed.setAccount(cap.getRoundingDifferenceAccount());
//                                            roundingJed.setAmount(roundingAmount);
//                                            roundingJed.setDebit(true);
//                                            roundingJed.setCompany(company);
//                                            roundingJed.setID(roundingJE);
//                                            roundingJed.setSrno(invoice.getJournalEntry().getDetails().size() + 1);
//                                            roundingJed.setJournalEntry(invoice.getJournalEntry());
//                                            session.save(roundingJed);
//                                        }
//
//                                    } else {
//                                        jArr.getJSONObject(i).put("successTRflag", false);
//                                    }
//
//                                } else {
//                                    jArr.getJSONObject(i).put("successTRflag", false);
//                                }
//                            } else {
//                                jArr.getJSONObject(i).put("successTRflag", false);
//                            }
//                        }
//                    }
//                } else {
//                    result.put("success", false);
//                    result.put("infocode", "m02");
//                }
//            } else {
//                result.put("success", false);
//                result.put("infocode", "e01");
//            }
//
//        } catch (ServiceException ex) {
//            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Service Exception while giving list of users", ex);
//            throw ServiceException.FAILURE(result.toString(), ex);
//        } catch (JSONException ex) {
//            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "JSON Exception while giving list of users", ex);
//            throw ServiceException.FAILURE(result.toString(), ex);
//        }
//        return result.toString();
//    }
    public boolean saveAccountJEs_optimized(String jeid) throws ServiceException {
        boolean successflag = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        JournalEntry je = (JournalEntry) session.get(JournalEntry.class, jeid); 

        KwlReturnObject jeResult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeid);
        JournalEntry je = (JournalEntry) jeResult.getEntityList().get(0);

        String gcurrencyid = je.getCompany().getCurrency().getCurrencyID();
        String companyid = je.getCompany().getCompanyID();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyid", companyid);
        requestParams.put("gcurrencyid", gcurrencyid);
        try {
            if (!je.isOptimizedflag() && !je.isDeleted() && je.getPendingapproval() == 0) {
                Set<JournalEntryDetail> jedetail = (Set<JournalEntryDetail>) je.getDetails();
                Iterator itr = jedetail.iterator();
                while (itr.hasNext()) {
                    JournalEntryDetail jed = (JournalEntryDetail) itr.next();

                    double amount = jed.isDebit() ? jed.getAmount() : -jed.getAmount();
                    String accountid = jed.getAccount().getID();
                    String entrydate = sdf.format(je.getEntryDate());
                    String costCenterID = je.getCostcenter() != null ? je.getCostcenter().getID() : "";

                    String fromcurrencyid = (je.getCurrency() == null ? gcurrencyid : je.getCurrency().getCurrencyID());
                    KwlReturnObject crresult = getCurrencyToBaseAmount(requestParams, amount, fromcurrencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                    amount = (Double) crresult.getEntityList().get(0);
                    if (amount != 0) {
                        accJournalEntryobj.saveAccountJEs_optimized(accountid, companyid, entrydate, costCenterID, amount);
                    }
                }
                successflag = accJournalEntryobj.setJEs_optimizedflag(jeid);
//                successflag = true;
//                je.setOptimizedflag(true);
//                session.saveOrUpdate(je);
            }
        } catch (Exception e) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("accJournalEntryImpl.saveAccountJEs_optimized : " + e.getMessage(), e);
        }
        return successflag;
    }

    public KwlReturnObject getBaseToCurrencyAmount(Map request, Double Amount, String newcurrencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        try {
            if (Amount != 0) {
                if (rate == 0) {
                    KwlReturnObject result = accCurrencyDAOobj.getExcDetailID(request, newcurrencyid, transactiondate, null);
                    List li = result.getEntityList();
                    if (!li.isEmpty()) {
                        Iterator itr = li.iterator();
                        ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                        rate = erd.getExchangeRate();
                    }
                    Amount = Amount * rate;
                } else if (request.containsKey("gcurrencyid") && !request.get("gcurrencyid").toString().equalsIgnoreCase(newcurrencyid)) {
                    Amount = Amount * rate;
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getBaseToCurrencyAmount : " + ex.getMessage(), ex);
        } finally {
            list.add(Amount);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }

    public KwlReturnObject getOneCurrencyToOther(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        Double currencyAmount = 0.0;
        try {
            if (Amount != 0) {
                KwlReturnObject bAmt = getCurrencyToBaseAmount(request, Amount, oldcurrencyid, transactiondate, rate);
                Double baseAmount = (Double) bAmt.getEntityList().get(0);
                bAmt = getBaseToCurrencyAmount(request, baseAmount, newcurrencyid, transactiondate, rate);
                currencyAmount = (Double) bAmt.getEntityList().get(0);
            }
            list.add(currencyAmount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getOneCurrencyToOther : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getCurrencyToBaseAmount(Map request, Double Amount, String currencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        try {
            if (Amount != 0) {
                if (rate == 0) {
                    KwlReturnObject result = accCurrencyDAOobj.getExcDetailID(request, currencyid, transactiondate, null);
                    List li = result.getEntityList();
                    if (!li.isEmpty()) {
                        Iterator itr = li.iterator();
                        ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                        rate = erd.getExchangeRate();
                    }
                }
                Amount = Amount / rate;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.getCurrencyToBaseAmount : " + ex.getMessage(), ex);
        } finally {
            list.add(Amount);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }

    private String getInvoiceDetailfromCrmQuotation(HttpServletRequest request) throws SQLException, ServiceException, JSONException {
        String result = "{\"success\":false}";
        try {
            JSONArray jArr = new JSONArray();
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String Crmquoteid = (String) jobj.get("quotationid");

            KwlReturnObject invResult = accInvoiceDAOobj.getInvoiceDetailfromCrmQuotation(Crmquoteid);
            List invoiceList = invResult.getEntityList();
            if (invoiceList.size() > 0) {
                for (int count = 0; count < invoiceList.size(); count++) {
                    JSONObject obj = new JSONObject();
                    Object[] invoicelistobj = (Object[]) invoiceList.get(count);
                    String invoicenumber = (String) invoicelistobj[0];
                    obj.put("InvoiceNumber", invoicenumber);
                    String invoiceid = (String) invoicelistobj[1];
                    KwlReturnObject invoiceResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
                    Invoice inv = (Invoice) invoiceResult.getEntityList().get(0);
                    Double invoicetotalamount = inv.getCustomerEntry().getAmount();
                    obj.put("InvoiceTotalAmount", invoicetotalamount);
                    obj.put("success", true);
                    jArr.put(obj);
                }
            }
            result = jArr.toString();
            String temp = "m" + String.format("%02d", 5);
            result = "{\"success\": true, \"infocode\": \"" + temp + "\", \"data\":" + jArr.toString() + "}";
        } catch (JSONException | ServiceException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return result;
    }

    public String getContractDetails(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        boolean issuccess = false;
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));

            JSONArray jArr = new JSONArray();
            String companyID = jobj.getString("companyid");
            String contractID = jobj.getString("contractid");
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyID);
            requestParams.put("contractid", contractID);
            KwlReturnObject contractResult = accSalesOrderDAOobj.getContractDetails(requestParams);
            List list = contractResult.getEntityList();

            int totalCount = list.size();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Contract contract = (Contract) itr.next();

                JSONObject obj = new JSONObject();
                obj.put("id", contract.getID());
                obj.put("accountname", (contract.getCustomer() != null) ? contract.getCustomer().getName() : "");
                obj.put("aggreement", contract.getContractNumber());
                obj.put("currencysymbol", contract.getCurrency().getSymbol());

                // for contract expiry date
                KwlReturnObject contractendate = accSalesOrderDAOobj.getContractStrtendDates(contract.getID());
                List<Object[]> contractDateList = contractendate.getEntityList();
                for (Object[] row : contractDateList) {
                    obj.put("agreementExpireyDate", row[1] != null ? row[1] : null);
                }

                obj.put("renewAgreement", ""); // not added in pojo
                obj.put("terminateAgreement", ""); // not added in pojo
                obj.put("tenureDetails", ""); // not added in pojo
                obj.put("totalAmount", contract.getAmount());

                jArr.put(obj);
            }

            result.put("data", jArr);
            result.put("totalCount", totalCount);
            issuccess = true;
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of contract details ", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    public String getContractOtherDetails(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        boolean issuccess = false;
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));

            JSONArray jArr = new JSONArray();
            String companyID = jobj.getString("companyid");
            String contractID = jobj.getString("contractid");
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyID);
            requestParams.put("contractid", contractID);
            KwlReturnObject contractResult = accSalesOrderDAOobj.getContractDetails(requestParams);
            List list = contractResult.getEntityList();
            int totalCount = list.size();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Contract contract = (Contract) itr.next();

                JSONObject obj = new JSONObject();
                obj.put("id", contract.getID());
                obj.put("aggreement", contract.getContractNumber());
                KwlReturnObject contractendate = accSalesOrderDAOobj.getContractStrtendDates(contract.getID());
                List<Object[]> contractDateList = contractendate.getEntityList();
                for (Object[] row : contractDateList) {
                    obj.put("from", row[0] != null ? row[0] : null);
                    obj.put("to", row[1] != null ? row[1] : null);
                }

                int status = contract.getCstatus();
                String statusName = "";
                if (status == 1) {
                    statusName = "Active";
                } else if (status == 2) {
                    statusName = "Terminated";
                    continue;                   // dont add terminated contract in the array
                } else if (status == 3) {
                    statusName = "Expire";
                } else if (status == 4) {
                    statusName = "Renew";
                }
                obj.put("status", statusName);

                String termType = contract.getTermType();
                String termTypeName = "";
                if (!StringUtil.isNullOrEmpty(termType)) {
                    if (termType.equals("1")) {
                        termTypeName = "Day";
                    } else if (termType.equals("2")) {
                        termTypeName = "Week";
                    } else if (termType.equals("3")) {
                        termTypeName = "Month";
                    } else if (termType.equals("4")) {
                        termTypeName = "Year";
                    }
                }
                obj.put("leaseTerm", contract.getTermValue() + " " + termTypeName);

                obj.put("lastRenewedDate", ""); // not added in pojo
                obj.put("originalEndDate", contract.getOriginalEndDate());
                obj.put("signInDate", contract.getSignDate());
                obj.put("moveInDate", contract.getMoveDate());
                obj.put("moveOutDate", contract.getMoveOutDate());

                jArr.put(obj);
            }

            result.put("data", jArr);
            result.put("totalCount", totalCount);
            issuccess = true;
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of contract other details ", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    public String getContractNormalInvoiceDetails(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        boolean issuccess = false;
        try {
            double amount = 0;
            JSONObject jobj = new JSONObject(request.getParameter("data"));

            JSONArray jArr = new JSONArray();
            String companyID = jobj.getString("companyid");
            String contractID = jobj.getString("contractid");

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyID);
            requestParams.put("contractid", contractID);
            KwlReturnObject invResult = accSalesOrderDAOobj.getContractNormalInvoiceDetails(requestParams);
            List<String> list = invResult.getEntityList();
            int totalCount = invResult.getRecordTotalCount();
            Set<String> invoiceSet = new HashSet<>();
            for (String invContrMapID : list) {
                KwlReturnObject invConMapResult = accountingHandlerDAOobj.getObject(InvoiceContractMapping.class.getName(), invContrMapID);
                InvoiceContractMapping invMap = (InvoiceContractMapping) invConMapResult.getEntityList().get(0);
                Invoice inv = (Invoice) invMap.getInvoice();
                amount = 0;
                if (!invoiceSet.contains(inv.getID())) {

                    invoiceSet.add(inv.getID());

                    JSONObject obj = new JSONObject();
                    obj.put("document", inv.getInvoiceNumber());
                    obj.put("description", inv.getMemo());
//                    obj.put("date", inv.getJournalEntry().getEntryDate());
                    obj.put("date", inv.getCreationDate());
                    JournalEntryDetail d = inv.getCustomerEntry();
                    amount += d.getAmount();
                    obj.put("amount", authHandler.round(amount, companyID));
                    obj.put("currencysymbol", inv.getCurrency().getSymbol());

                    jArr.put(obj);
                }
            }

            result.put("data", jArr);
            result.put("totalCount", totalCount);
            issuccess = true;
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of Contract Normal Invoice details ", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    public String getContractReplacementInvoiceDetails(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        boolean issuccess = false;
        try {
            int amount;
            double quantity = 0;
            JSONObject jobj = new JSONObject(request.getParameter("data"));

            JSONArray jArr = new JSONArray();
            String companyID = jobj.getString("companyid");
            String contractID = jobj.getString("contractid");

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyID);
            requestParams.put("contractid", contractID);
            KwlReturnObject invResult = accSalesOrderDAOobj.getContractReplacementInvoiceDetails(requestParams);
            List<String> list = invResult.getEntityList();
            int totalCount = invResult.getRecordTotalCount();

            Set<String> invoiceSet = new HashSet<>();

            for (String invContrMapID : list) {
                KwlReturnObject invConMapResult = accountingHandlerDAOobj.getObject(InvoiceContractMapping.class.getName(), invContrMapID);
                InvoiceContractMapping invMap = (InvoiceContractMapping) invConMapResult.getEntityList().get(0);
                Invoice inv = (Invoice) invMap.getInvoice();

                if (!invoiceSet.contains(inv.getID())) {

                    invoiceSet.add(inv.getID());

                    JSONObject obj = new JSONObject();
                    obj.put("document", inv.getInvoiceNumber());
                    obj.put("description", inv.getMemo());
//                    obj.put("date", inv.getJournalEntry().getEntryDate());
                    obj.put("date", inv.getCreationDate());

                    Set<InvoiceDetail> invRows = inv.getRows();
                    amount = 0;
                    if (invRows != null && !invRows.isEmpty()) {
                        for (InvoiceDetail temp : invRows) {
                            quantity = temp.getInventory().getQuantity();
                            amount += authHandler.round(temp.getRate() * quantity, companyID);
                        }
                    }
                    obj.put("amount", authHandler.round(amount, companyID));
                    obj.put("currencysymbol", inv.getCurrency().getSymbol());

                    jArr.put(obj);
                }
            }

            result.put("data", jArr);
            result.put("totalCount", totalCount);
            issuccess = true;
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of Contract Replacement Invoice details ", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    public String getContractMaintenanceInvoiceDetails(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        boolean issuccess = false;
        try {
            int amount;
            double quantity = 0;
            JSONObject jobj = new JSONObject(request.getParameter("data"));

            JSONArray jArr = new JSONArray();
            String companyID = jobj.getString("companyid");
            String contractID = jobj.getString("contractid");

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyID);
            requestParams.put("contractid", contractID);

            KwlReturnObject invResult = accSalesOrderDAOobj.getContractMaintenanceInvoiceDetails(requestParams);
            List<String> list = invResult.getEntityList();
            int totalCount = invResult.getRecordTotalCount();

            for (String invID : list) {
                KwlReturnObject invConMapResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invID);
                Invoice inv = (Invoice) invConMapResult.getEntityList().get(0);

                JSONObject obj = new JSONObject();
                obj.put("document", inv.getInvoiceNumber());
                obj.put("description", inv.getMemo());
//                obj.put("date", inv.getJournalEntry().getEntryDate());
                obj.put("date", inv.getCreationDate());

                Set<InvoiceDetail> invRows = inv.getRows();
                amount = 0;
                if (invRows != null && !invRows.isEmpty()) {
                    for (InvoiceDetail temp : invRows) {
                        quantity = temp.getInventory().getQuantity();
                        amount += authHandler.round(temp.getRate() * quantity, companyID);
                    }
                }
                obj.put("amount", authHandler.round(amount, companyID));
                obj.put("currencysymbol", inv.getCurrency().getSymbol());

                jArr.put(obj);
            }

            result.put("data", jArr);
            result.put("totalCount", totalCount);
            issuccess = true;
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of Contract Maintenance Invoice details ", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    public String getContractNormalDOItemDetails(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        ArrayList params = new ArrayList();
        boolean issuccess = false;
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));

            JSONArray jArr = new JSONArray();
            String companyID = jobj.getString("companyid");
            String contractID = jobj.getString("contractid");

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyID);
            requestParams.put("contractid", contractID);

            KwlReturnObject doResult = accSalesOrderDAOobj.getContractNormalDOItemDetails(requestParams);
            List<String> list = doResult.getEntityList();
            int totalCount = doResult.getRecordTotalCount();

            for (String dodid : list) {
                KwlReturnObject dodResult = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), dodid);
                DeliveryOrderDetail doDetail = (DeliveryOrderDetail) dodResult.getEntityList().get(0);

                JSONObject obj = new JSONObject();
                obj.put("pid", (doDetail.getProduct() != null) ? doDetail.getProduct().getID() : "");
                obj.put("itemName", (doDetail.getProduct() != null) ? doDetail.getProduct().getName() : "");
                obj.put("doid", (doDetail.getDeliveryOrder() != null) ? doDetail.getDeliveryOrder().getID() : "");
                obj.put("itemCode", (doDetail.getProduct() != null) ? doDetail.getProduct().getProductid() : "");
                obj.put("itemDescription", (doDetail.getProduct() != null) ? doDetail.getProduct().getDescription() : "");
                obj.put("quantity", doDetail.getActualQuantity());
                String uom = doDetail.getUom() != null ? doDetail.getUom().getNameEmptyforNA() : doDetail.getProduct().getUnitOfMeasure() == null ? "" : doDetail.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                obj.put("unitname", uom);

                jArr.put(obj);
            }

            result.put("data", jArr);
            result.put("totalCount", totalCount);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of contract normal DO item details", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    public String getContractNormalDOItemDetailsRow(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        boolean issuccess = false;
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            JSONArray jArr = new JSONArray();
            String companyID = jobj.getString("companyid");
            String contractID = jobj.getString("contractid");
            String pid = jobj.getString("pid");
            String doid = jobj.getString("doid");

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyID);
            requestParams.put("contractid", contractID);
            requestParams.put("pid", pid);
            requestParams.put("doid", doid);

            KwlReturnObject doRowResult = accSalesOrderDAOobj.getContractNormalDOItemDetailsRow(requestParams);
            List<Object[]> list = doRowResult.getEntityList();
            int totalCount = doRowResult.getRecordTotalCount();

            for (Object[] row : list) {

                JSONObject obj = new JSONObject();
                String serialnoid = (row[0] != null) ? (String) row[0] : "";
                obj.put("srid", serialnoid);
                obj.put("srname", (row[1] != null) ? (String) row[1] : "");
                obj.put("batchname", (row[2] != null) ? (String) row[2] : "");
                obj.put("warrentyExpireyDate", (row[3] != null) ? df.format((Date) row[3]) : "");

                if (!StringUtil.isNullOrEmpty(serialnoid)) {
                    obj.put("vendorWarrentyDate", (row[3] != null) ? df.format((Date) row[3]) : "");
                }

                jArr.put(obj);
            }

            result.put("data", jArr);
            result.put("totalCount", totalCount);
            issuccess = true;
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of contract normal DO item details row", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    public String getContractReplacementDOItemDetails(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        boolean issuccess = false;
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));

            JSONArray jArr = new JSONArray();
            String companyID = jobj.getString("companyid");
            String contractID = jobj.getString("contractid");

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyID);
            requestParams.put("contractid", contractID);

            KwlReturnObject doResult = accSalesOrderDAOobj.getContractReplacementDOItemDetails(requestParams);
            List<Object[]> list = doResult.getEntityList();
            int totalCount = doResult.getRecordTotalCount();

            for (Object[] objRow : list) {
                String dodid = (String) objRow[1];
                KwlReturnObject dodResult = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), dodid);
                DeliveryOrderDetail doDetail = (DeliveryOrderDetail) dodResult.getEntityList().get(0);

                JSONObject obj = new JSONObject();
                obj.put("productReplacementID", (objRow[0] != null) ? objRow[0] : "");
                obj.put("pid", (doDetail.getProduct() != null) ? doDetail.getProduct().getID() : "");
                obj.put("itemName", (doDetail.getProduct() != null) ? doDetail.getProduct().getName() : "");
                obj.put("dateOfReplacement", (doDetail.getDeliveryOrder() != null) ? doDetail.getDeliveryOrder().getOrderDate() : "");
                obj.put("itemCode", (doDetail.getProduct() != null) ? doDetail.getProduct().getProductid() : "");
                obj.put("itemDescription", (doDetail.getProduct() != null) ? doDetail.getProduct().getDescription() : "");
                obj.put("quantity", doDetail.getActualQuantity());
                String uom = doDetail.getUom() != null ? doDetail.getUom().getNameEmptyforNA() : doDetail.getProduct().getUnitOfMeasure() == null ? "" : doDetail.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                obj.put("unitname", uom);

                jArr.put(obj);
            }

            result.put("data", jArr);
            result.put("totalCount", totalCount);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of contract replacement DO item details", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    public String getContractReplacementDOItemDetailsRow(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        boolean issuccess = false;
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));

            JSONArray jArr = new JSONArray();
            String companyID = jobj.getString("companyid");
            String contractID = jobj.getString("contractid");
            String pid = jobj.getString("pid");
            String productReplacementID = jobj.getString("productReplacementID");

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyID);
            requestParams.put("contractid", contractID);
            requestParams.put("pid", pid);
            requestParams.put("productReplacementID", productReplacementID);

            KwlReturnObject codresult = accSalesOrderDAOobj.getContractReplacementDOItemDetailsRow(requestParams);
            List<Object[]> list = codresult.getEntityList();
            int totalCount = codresult.getRecordTotalCount();

            for (Object[] row : list) {

                JSONObject obj = new JSONObject();
                String serialnoid = (row[0] != null) ? (String) row[0] : "";
                obj.put("srid", serialnoid);
                obj.put("srname", (row[1] != null) ? (String) row[1] : "");
                obj.put("batchname", (row[2] != null) ? (String) row[2] : "");
                obj.put("warrentyExpireyDate", (row[3] != null) ? (Date) row[3] : "");

                if (!StringUtil.isNullOrEmpty(serialnoid)) {
                    Date vendorExpDate = accCommonTablesDAO.getVendorExpDateForSerial(serialnoid, false);
                    if (vendorExpDate != null) {
                        obj.put("vendorWarrentyDate", vendorExpDate);
                    }
                }
                jArr.put(obj);
            }

            result.put("data", jArr);
            result.put("totalCount", totalCount);
            issuccess = true;
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of contract replacement DO item details row", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    public String getCustomerContractsAgreementDetails(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        ArrayList params = new ArrayList();
        boolean issuccess = false;
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));

            JSONArray jArr = new JSONArray();
            String companyID = jobj.getString("companyid");
            String customerID = jobj.getString("accountid");
            String contractID = jobj.optString("contractid", "");

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyID);
            requestParams.put("crmaccountid", customerID);
            requestParams.put("contractid", contractID);

            KwlReturnObject contractResult = accSalesOrderDAOobj.getCustomerContractsFromCRMAccountID(requestParams);
            List list = contractResult.getEntityList();
            int totalCount = contractResult.getRecordTotalCount();

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Contract contract = (Contract) itr.next();

                JSONObject obj = new JSONObject();
                obj.put("contractrecid", contract.getID());
                if (!contract.isNormalContract()) {
                    HashMap<String, Object> requestParams1 = new HashMap<>();
                    requestParams1.put("companyid", companyID);
                    requestParams1.put("contractid", contract.getID());
                    KwlReturnObject contractDOMappingResult = accSalesOrderDAOobj.getContractFromDOContractMapping(requestParams1);

                    if (contractDOMappingResult.getEntityList().isEmpty()) {
                        continue;
                    }
                } else {
                    //checked if DO is generated for that Contract or not & if not then excluded those contracts from sending to CRM
                    Map<String, Object> params1 = new HashMap<>();
                    params1.put("contractid", contract.getID());
                    KwlReturnObject results1 = accSalesOrderDAOobj.getContractsDO(params1);
                    if(results1.getEntityList().isEmpty()) {
                        continue;
                    }
                }
                obj.put("contractid", contract.getContractNumber());
                obj.put("contactperson", contract.getContactPerson());
                obj.put("agreementtype", ""); // not added in pojo

                ArrayList contractDateParams = new ArrayList();
                contractDateParams.add(contract.getID());

                KwlReturnObject contractendate = accSalesOrderDAOobj.getContractStrtendDates(contract.getID());
                List<Object[]> contractDateList = contractendate.getEntityList();
                for (Object[] row : contractDateList) {
                    if (!StringUtil.isNullOrEmpty(contractID)) {//changed date format for maintanince date to shown in add maintanince service form in CRM
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        obj.put("fromdate", row[0] != null ? sdf.format(row[0]) : null);
                        obj.put("todate", row[1] != null ? sdf.format(row[1]) : null);
                    } else {
                        obj.put("fromdate", row[0] != null ? row[0] : null);
                        obj.put("todate", row[1] != null ? row[1] : null);
                    }

                }

                int status = contract.getCstatus();
                String statusName = "";
                if (status == 1) {
                    statusName = "Active";
                } else if (status == 2) {
                    statusName = "Terminated";
                    continue;
                } else if (status == 3) {
                    statusName = "Expire";
                } else if (status == 4) {
                    statusName = "Renew";
                }
                obj.put("statusname", statusName);

                String termType = contract.getTermType();
                String termTypeName = "";
                if (!StringUtil.isNullOrEmpty(termType)) {
                    if (termType.equals("1")) {
                        termTypeName = "Day";
                    } else if (termType.equals("2")) {
                        termTypeName = "Week";
                    } else if (termType.equals("3")) {
                        termTypeName = "Month";
                    } else if (termType.equals("4")) {
                        termTypeName = "Year";
                    }
                }
                obj.put("leasetermname", contract.getTermValue() + " " + termTypeName);
                obj.put("lastrenewdate", ""); // not added in pojo
                obj.put("orgenddate", contract.getOriginalEndDate());
                obj.put("signindate", contract.getSignDate());
                obj.put("moveindate", contract.getMoveDate());
                obj.put("moveoutdate", contract.getMoveOutDate());
                obj.put("isNormalSalesContract", contract.isNormalContract());

                jArr.put(obj);
            }

            result.put("data", jArr);
            result.put("totalCount", totalCount);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of Customer Contract details ", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    public String getCustomerContractsCostAgreementDetails(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        ArrayList params = new ArrayList();
        boolean issuccess = false;
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));

            JSONArray jArr = new JSONArray();
            String companyID = jobj.getString("companyid");
            String customerID = jobj.getString("accountid");

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyID);
            requestParams.put("crmaccountid", customerID);

            KwlReturnObject contractResult = accSalesOrderDAOobj.getCustomerContractsFromCRMAccountID(requestParams);
            List list = contractResult.getEntityList();
            int totalCount = contractResult.getRecordTotalCount();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Contract contract = (Contract) itr.next();

                JSONObject obj = new JSONObject();
                obj.put("contractid", contract.getContractNumber());
                obj.put("leaseamount", contract.getAmount());
                obj.put("currencysymbol", (contract.getCurrency() != null) ? contract.getCurrency().getSymbol() : "");
                obj.put("securitydepos", ""); // not added in pojo

                HashMap<String, Object> requestParams1 = new HashMap<>();
                requestParams1.put("companyid", companyID);
                requestParams1.put("contractid", contract.getID());
                KwlReturnObject invoiceResult = accSalesOrderDAOobj.getContractInvoiceDetails(requestParams);
                List invoiceList = invoiceResult.getEntityList();

                Iterator invoiceListItr = invoiceList.iterator();
                double amount = 0;
                double quantity = 0;

                while (invoiceListItr.hasNext()) {
                    InvoiceContractMapping InvMap = (InvoiceContractMapping) invoiceListItr.next();
                    Invoice inv = (Invoice) InvMap.getInvoice();

                    Set<InvoiceDetail> invRows = inv.getRows();
                    if (invRows != null && !invRows.isEmpty()) {
                        for (InvoiceDetail temp : invRows) {
                            quantity = temp.getInventory().getQuantity();
                            amount += authHandler.round(temp.getRate() * quantity, companyID);
                        }
                    }
                }

                double contractAmount = contract.getAmount();
                double outstandingAmount = contractAmount - amount;
                obj.put("outstandings", outstandingAmount);

                obj.put("monthlyrent", ""); // not added in pojo

                jArr.put(obj);
            }

            result.put("data", jArr);
            result.put("totalCount", totalCount);
            issuccess = true;
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of Customer Contract Cost Agreement details ", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    public String getCustomerContractsServiceAgreementDetails(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        ArrayList params = new ArrayList();
        String query = "";
        boolean issuccess = false;
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            JSONArray jArr = new JSONArray();
            String companyID = jobj.getString("companyid");
            String accountid = jobj.getString("accountid");
            String contractid = jobj.getString("contractid");

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyID);
            requestParams.put("crmaccountid", accountid);
            requestParams.put("contractid", contractid);

            KwlReturnObject contractResult = accSalesOrderDAOobj.getCustomerContractsFromCRMAccountID(requestParams);
            List list = contractResult.getEntityList();
            int totalCount = contractResult.getRecordTotalCount();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Contract contract = (Contract) itr.next();

                JSONObject obj = new JSONObject();
                obj.put("contractid", contract.getContractNumber());
                obj.put("agreedservices", contract.getAgreedServices());

                KwlReturnObject resultOfNextServiceDate = accSalesOrderDAOobj.getNextServiceDateOfContract(requestParams);
                if (!resultOfNextServiceDate.getEntityList().isEmpty()) {
                    Date nextServiceDate = (Date) resultOfNextServiceDate.getEntityList().get(0);
                    obj.put("nextservicedate", nextServiceDate);
                }

                KwlReturnObject resultOfPreviousServiceDate = accSalesOrderDAOobj.getPreviousServiceDateOfContract(requestParams);
                if (!resultOfPreviousServiceDate.getEntityList().isEmpty()) {
                    Date previousServiceDate = (Date) resultOfPreviousServiceDate.getEntityList().get(0);
                    obj.put("lastservicedate", previousServiceDate);
                }

                obj.put("oncallservices", ""); // not added in pojo
                obj.put("ongoingservices", ""); // not added in pojo

                jArr.put(obj);
            }

            result.put("data", jArr);
            result.put("totalCount", totalCount);
            issuccess = true;
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of Customer Contract Service Agreement details ", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    public String getAccountContractDetails(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        boolean issuccess = false;
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            JSONArray jArr = new JSONArray();
            String companyID = jobj.getString("companyid");
            String accountid = jobj.getString("accountid");
            int limit = 25;
            int start = 0;
            boolean ispaging = jobj.has("start") && jobj.has("limit") && !StringUtil.isNullOrEmpty(jobj.get("start").toString()) && !StringUtil.isNullOrEmpty(jobj.get("limit").toString());
            if (ispaging) {
                start = Integer.parseInt(jobj.get("start").toString());
                limit = Integer.parseInt(jobj.get("limit").toString());
            }

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyID);
            requestParams.put("crmaccountid", accountid);
            requestParams.put("ss", jobj.getString("ss"));
            requestParams.put("dir", jobj.getString("dir"));
            requestParams.put("sort", jobj.getString("sort"));
            KwlReturnObject contractResult = accSalesOrderDAOobj.getAccountContractDetails(requestParams);
            int totalCount = contractResult.getRecordTotalCount();
            requestParams.put("start", start);
            requestParams.put("limit", limit);
            contractResult = accSalesOrderDAOobj.getAccountContractDetails(requestParams);

            Iterator itr = contractResult.getEntityList().iterator();
            while (itr.hasNext()) {
                Contract contract = (Contract) itr.next();

                JSONObject obj = new JSONObject();
                obj.put("contractrecid", contract.getID());
                obj.put("contractid", contract.getContractNumber());
                obj.put("isNormalSalesContract", contract.isNormalContract());
                obj.put("accountname", (contract.getCustomer() != null) ? contract.getCustomer().getName() : "");
                obj.put("contactperson", contract.getContactPerson());
                obj.put("email", contract.getEmailID());
                obj.put("noofagreedservices", contract.getAgreedServices());
                obj.put("contractamt", contract.getAmount());
                obj.put("accountid", (contract.getCustomer() != null) ? contract.getCustomer().getCrmaccountid() : "");
                obj.put("id", contract.getContractNumber()); // addded for contract combo
                obj.put("name", contract.getContractNumber());
                obj.put("contractstatus", contract.getCstatus() == 1 ? "Active" : (contract.getCstatus() == 2 ? "Terminate" : (contract.getCstatus() == 3 ? "Expire" : "Renew")));
                obj.put("hasAccess", true);

                jArr.put(obj);
            }

            result.put("data", jArr);
            result.put("totalCount", totalCount);
            issuccess = true;
        } catch (JSONException | NumberFormatException | ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of Account Contract details ", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    public String getTaxRequest(HttpServletRequest request, HttpServletResponse response) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {
        String result = "{\"success\":false}";
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String s1 = request.getParameter("data");
            JSONObject jobj2 = new JSONObject(s1);
            String companyID = jobj2.getString("companyid");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            if (jobj2.has("userid")) {
                userData.put("userid", jobj2.getString("userid"));
            }
            userData.put("companyid", companyID);
            boolean isSales = jobj2.optBoolean("isforlms", false);
            if (!isSales) {
                isSales = jobj2.optBoolean("isSales", false);
            }
            JSONArray tjobj = new JSONArray();
            KwlReturnObject taxResult = accTaxObj.getAllTaxOfCompany(companyID);
            List<Tax> list = taxResult.getEntityList();
            for (Tax tax : list) {
                KwlReturnObject taxListResult = accountingHandlerDAOobj.getObject(TaxList.class.getName(), tax.getID());
                TaxList taxlist = (TaxList) taxListResult.getEntityList().get(0);
                JSONObject jobj1 = new JSONObject();
                jobj1.put("applydate", taxlist.getApplyDate());
                jobj1.put("percent", taxlist.getPercent());
                jobj1.put("companyid", tax.getCompany().getCompanyID());
                jobj1.put("taxname", tax.getName());
                jobj1.put("taxcode", tax.getTaxCode());
                jobj1.put("taxid", tax.getID());
                if (isSales && tax.getTaxtype() == 2) {
                    tjobj.put(jobj1);
                } else if (!isSales) {
                    tjobj.put(jobj1);
                }

            }
            userData.put("data", tjobj);
            userData.put("success", true);
            result = encodeStringData(userData);
            //  result = userData.toString();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("RemoteAPI.getTaxRequest", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }
    //Get Exchange rate for input currency

    public String getCurrencyExchange(JSONObject jobj) {
        JSONObject jobjData = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            String erpcompanyid = jobj.getString("companyid");
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), erpcompanyid);
            Company companyObj = (Company) companyResult.getEntityList().get(0);
            DateFormat datef = authHandler.getDateOnlyFormat();
            String erpcurrency = companyObj.getCurrency().getCurrencyID();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            requestParams.put("fromcurrencyid", jobj.getString("fromcurrencyid"));

            if ((jobj.getString("fromcurrencyid")).equalsIgnoreCase(erpcurrency)) {
                requestParams.put("companyid", erpcompanyid);
                String transactionDateinString = jobj.getString("transacationdateStr");
                if (!StringUtil.isNullOrEmpty(transactionDateinString)) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(sdf.parse(transactionDateinString));
                    String tdate = datef.format(cal.getTime());
                    try {
                        Date tradate = datef.parse(tdate);
                        requestParams.put("transacationdate", tradate);
                    } catch (ParseException ex) {
                        requestParams.put("transacationdate", cal.getTime());
                    }
                };
                KwlReturnObject result = accCurrencyDAOobj.getCurrencyExchange(requestParams);
                List list = result.getEntityList();
                JSONArray jArr = getCurrencyExchangeJson(jobj, list, requestParams);
                jobjData.put("data", jArr);
                jobjData.put("count", jArr.length());
                issuccess = true;
                jobjData.put("result", issuccess);
                jobjData.put("msg", msg);
            } else {
                jobjData.put("result", issuccess);
                jobjData.put("msg", "Eclaim Basecurrency is different than ERP Basecurrency. Please check it !!");
            }
        } catch (JSONException | ServiceException | ParseException | SessionExpiredException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobjData.toString();
    }

    public JSONArray getCurrencyExchangeJson(JSONObject jobj, List<ExchangeRate> list, Map<String, Object> requestParams) throws SessionExpiredException, ServiceException, java.text.ParseException, com.krawler.utils.json.base.JSONException {
        JSONArray jArr = new JSONArray();
        try {
            Map<String, Object> mapParams = new HashMap<>();
            Date transactiondate = null;
            mapParams.put("gcurrencyid", jobj.getString("fromcurrencyid"));
            mapParams.put("companyid", jobj.getString("companyid"));
            try {
                if ((requestParams.get("transacationdate")) != null) {
                    transactiondate = (Date) requestParams.get("transacationdate");
                }
            } catch (Exception exception) {
                transactiondate = new Date();
            }

            JSONObject obj = new JSONObject();
            if (list != null && !list.isEmpty()) {
                for (ExchangeRate ER : list) {
                    String erID = ER.getID();
                    KwlReturnObject erdresult = accCurrencyDAOobj.getExcDetailID(mapParams, null, transactiondate, erID);
                    ExchangeRateDetails erd = (ExchangeRateDetails) erdresult.getEntityList().get(0);
                    obj = new JSONObject();
                    if (erd != null) {
                        obj.put("exchangerate", erd.getExchangeRate());
                        obj.put("newexchangerate", erd.getExchangeRate());
                        obj.put("fromcurrency", erd.getExchangeratelink().getFromCurrency().getName());
                        obj.put("currencycode", erd.getExchangeratelink().getToCurrency().getCurrencyCode());
                        obj.put("tocurrency", erd.getExchangeratelink().getToCurrency().getName());
                        obj.put("tocurrencyid", erd.getExchangeratelink().getToCurrency().getCurrencyID());
                        obj.put("fromcurrencyid", erd.getExchangeratelink().getFromCurrency().getCurrencyID());
                        obj.put("companyid", erd.getCompany().getCompanyID());
                        jArr.put(obj);
                    }
                }
            }
        } catch (com.krawler.utils.json.JSONException ex) {
            throw ServiceException.FAILURE("getCurrencyExchangeJson : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getCurrencyExchangeJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public static Object getCompanyObject(Session session, String classpath, String id) throws ServiceException {
        Object obj = null;
        try {
            Class cls = Class.forName(classpath);
            obj = session.get(cls, id);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return obj;
    }

    public String getPaymentmethod(HttpServletRequest request) throws ServiceException {
        JSONObject result = new JSONObject();
        boolean issuccess = false;

        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            JSONArray jArr = new JSONArray();
            JSONObject obj1;
            String companyID = jobj.getString("companyid");

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyID);
            requestParams.put("paymentAccountType", "0");

            KwlReturnObject paymentMethodResult = accPaymentDAOobj.getPaymentMethod(requestParams);
            List list3 = paymentMethodResult.getEntityList();

            Iterator itr1 = list3.iterator();
            while (itr1.hasNext()) {
                PaymentMethod p = (PaymentMethod) itr1.next();
                obj1 = new JSONObject();
                obj1.put("methodid", p.getID());
                obj1.put("methodname", p.getMethodName());
                obj1.put("methodaccountid", p.getAccount().getID());
                jArr.put(obj1);
            }
            result.put("data", jArr);
            issuccess = true;
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception while giving list of payment details ", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } finally {
            try {
                result.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result.toString();
    }

    /**
     * Description : This Method is used to check the existing currency or
     * country is same or not
     *
     * @param <request> used to get request parameters
     * @param <session> used to get Company Object
     * @return :boolean
     */
    public boolean isCompanyCurrencyandCountrySame(HttpServletRequest request) throws ServiceException {

        String companyid = "", currency = "", country = "";
        boolean isCurrencyandCountrySame = false;
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            companyid = jobj.isNull("companyid") ? "" : jobj.getString("companyid");
            currency = jobj.isNull("currency") ? "" : jobj.getString("currency");
            country = jobj.isNull("country") ? "" : jobj.getString("country");

            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyResult.getEntityList().get(0);
            if (company != null && company.getCurrency().getCurrencyID().equalsIgnoreCase(currency) && company.getCountry().getID().equalsIgnoreCase(country)) {
                isCurrencyandCountrySame = true;
            }

        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception when checking the transcation is created or not  ", ex);
        }
        return isCurrencyandCountrySame;
    }

    private JSONObject verifyLogin(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        try {
            ModelAndView model = authHandlerControllerObj.verifyLogin(request, response);
            jobj = new JSONObject(model.getModel().get("model").toString());
            jobj = jobj.getJSONObject("data");
            if (jobj.has("success") && (jobj.get("success").equals(true))) {
                String userid = jobj.getString("lid");
                HashMap<String, Object> requestParams = new HashMap<>();
                requestParams.put("userid", StringUtil.checkForNull(userid));
            } else {
                jobj.put("success", false);
                jobj.put("error", "Authentication failed");
            }
        } catch (ServletException | JSONException e) {
            logger.warn(e.getMessage(), e);
            jobj.put("success", false);
            jobj.put("error", "Error occurred while authentication " + e.toString());
            logger.warn(e.getMessage(), e);
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while authentication in RemoteAPI.verifyLogin()", e.toString());
        }
        return jobj;
    }

    public String getUserPermissions(HttpServletRequest request, HttpServletResponse response) {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        try {
            String userid = (request.getParameter("userid") != null ? request.getParameter("userid") : (request.getAttribute("userId") != null ? request.getAttribute("userId").toString() : ""));
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("userid", userid);
            JSONObject permJobj = new JSONObject();
            kmsg = permissionHandlerDAOObj.getActivityFeature();
            permJobj = permissionHandler.getAllPermissionJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());

            requestParams = new HashMap<>();
            requestParams.put("userid", userid);
            kmsg = permissionHandlerDAOObj.getUserPermission(requestParams);
            permJobj = permissionHandler.getRolePermissionJson(kmsg.getEntityList(), permJobj);

            kmsg = permissionHandlerDAOObj.getUserPermission(requestParams);
            List<Object[]> rows = kmsg.getEntityList();
            ArrayList jo = new ArrayList();
            JSONObject Perm = permJobj.getJSONObject("Perm");
            for (Object[] row : rows) {
                String keyName = row[0].toString();
                String value = row[1].toString();
                JSONObject keyPerm = Perm.getJSONObject(keyName);
                long perm = Long.parseLong(value);
                JSONObject temp = doOperation(keyPerm, perm);
                jo.add(new JSONObject().put(keyName, temp));
            }
            jobj.put("permValues", jo);
        } catch (ServiceException | JSONException | NumberFormatException e) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while authentication in RemoteAPI.verifyLogin()", e.toString());
        }
        return jobj.toString();
    }

    public List permissionsValues(Long val) {
        ArrayList values = new ArrayList();
        if (val == 0) {
            values.add(new Boolean("false"));
        } else {
            while (val > 0) {
                if (val % 2 == 0) {
                    values.add(new Boolean("false"));
                } else {
                    values.add(new Boolean("true"));
                }
                val /= 2;
            }
        }
        return values;
    }

    public int countValue(int val) {
        int cnt = 0;
        while (val != 1) {
            cnt++;
            val /= 2;
        }
        return cnt;
    }

    public JSONObject doOperation(JSONObject keys, long value) {
        JSONObject newValues = new JSONObject();

        List list = permissionsValues(value);

        int listLen = list.size();
        List<String> strings = new ArrayList<>();
        Iterator iterator = keys.keys();
        while (iterator.hasNext()) {
            strings.add((String) iterator.next());
        }
        String[] keysNames = new String[strings.size()];
        keysNames = strings.toArray(keysNames);
        for (String key : keysNames) {
            int x = 0;
            try {
                x = (Integer) keys.get(key);
                int p = countValue(x);
                if (p >= listLen) {
                    newValues.put(key, new Boolean("false"));
                } else {
                    newValues.put(key, list.get(p));
                }
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while authentication in RemoteAPI.doOperation()", ex);
            }
        }
        return newValues;
    }

    private void setUserSession(HttpServletRequest request, HttpServletResponse response) {
        try {
            String userID = request.getParameter("u");
            User userObj = null;
            if (userID == null) {
                userID = request.getParameter("userid");
                if (!StringUtil.isNullOrEmpty(userID)) {
                    userObj = (User) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.User", userID);
                }
            }
            if (userObj == null) {
                String companyid = request.getParameter("companyid");
                Company userObj1 = (Company) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.Company", companyid);
                userID = userObj1.getCreator().getUserID();
                userObj = (User) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.User", userID);
            }
            String user = userObj.getUserLogin().getUserName();
            String pwd = userObj.getUserLogin().getPassword();
            if (userID != null && userObj != null) {
                request.setAttribute("user", user);
                request.setAttribute("pwd", pwd);
            }
            ModelAndView modelAndView = authHandlerControllerObj.verifyLogin(request, response);
            JSONObject jobj = new JSONObject(modelAndView.getModel().get("model").toString());
            jobj = jobj.getJSONObject("data");
            if (jobj.has("success") && (jobj.get("success").equals(true))) {
                request.getSession().setAttribute("iPhoneCRM", true);
            } else {
                return;
            }
        } catch (ServiceException | ServletException | JSONException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while setting User Session in RemoteAPI.setUserSession()", ex);
        }

    }

    //saving the invoice     
    public ModelAndView saveCustomerInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accInvoiceModuleService.saveInvoice(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accReceiptServiceDAO.saveReceipt(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accInvoiceModuleService.deleteInvoice(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveJournalEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accJournalEntryModuleService.saveJournalEntry(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveJournalEntryRemoteApplication(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accJournalEntryModuleService.saveJournalEntryRemoteApplication(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveInvoiceFromLMS(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accInvoiceModuleService.saveInvoiceFromLMS(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView sendInvoiceTermsToCRM(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            request.setAttribute("isCRMCall", true);
            JSONObject jobj1 = accTaxService.getTax(request, response);
            jobj.put("taxdata", jobj1.getJSONArray("data"));
            JSONArray jarr = accAccountService.getInvoiceTerms(request);
            jobj.put("termdata", jarr);
            jobj.put("success", true);
        } catch (ServiceException | SessionExpiredException | JSONException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while sending Invoice Terms To CRM in RemoteAPI.sendInvoiceTermsToCRM()", ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAccounts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray DataJArr = new JSONArray();
        try {

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String ignorecustomers = "true";
            String ignorevendors = "true";
            if (StringUtil.isNullOrEmpty(request.getParameter("ignorecustomers"))) {
                ignorecustomers = request.getParameter("ignorecustomers");
            }
            if (StringUtil.isNullOrEmpty(request.getParameter("ignorevendors"))) {
                ignorevendors = request.getParameter("ignorevendors");
            }
            requestParams.put("ignorecustomers", ignorecustomers);
            requestParams.put("ignorevendors", ignorevendors);
            requestParams.put("nondeleted", "true");
            String currencyid = (String) sessionHandlerImpl.getCurrencyID(request);
            KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.KWLCurrency", currencyid);
            KwlReturnObject result = accAccountDAOobj.getAccountsForCombo(requestParams);
            List<Account> list = result.getEntityList();
            if (list != null && list.size() > 0) {
                for (Account account : list) {
                    JSONObject obj = new JSONObject();
                    if (account.isActivate()) {
                        obj.put("accid", account.getID());
                        obj.put("accname", (!StringUtil.isNullOrEmpty(account.getName())) ? account.getName() : (!StringUtil.isNullOrEmpty(account.getAcccode()) ? account.getAcccode() : ""));
                        obj.put("accdesc", StringUtil.isNullOrEmpty(account.getDescription()) ? "" : account.getDescription());
                        obj.put("mappedaccountid", account.getID());
                        obj.put("groupid", account.getGroup().getID());
                        obj.put("acccode", account.getAcccode());
                        obj.put("accnamecode", (!StringUtil.isNullOrEmpty(account.getAcccode()) ? "[" + account.getAcccode() + "] " + account.getName() : account.getName()));
                        obj.put("nature", account.getGroup().getNature());
                        obj.put("naturename", (account.getGroup().getNature() == Constants.Liability) ? "Liability" : (account.getGroup().getNature() == Constants.Asset) ? "Asset" : (account.getGroup().getNature() == Constants.Expences) ? "Expences" : (account.getGroup().getNature() == Constants.Income) ? "Income" : "");
                        obj.put("currencyid", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID()));
                        obj.put("accounttype", account.getAccounttype());
                        DataJArr.put(obj);
                    }
                }
            }

            issuccess = true;
        } catch (SessionExpiredException | ServiceException | JSONException ex) {
            msg = "RemoteAPI.getAccounts:" + ex.getMessage();
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while getting Accounts in RemoteAPI.getAccounts()", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", DataJArr);

            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while getting Accounts in RemoteAPI.getAccounts()", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString().replaceAll("%", "%25"));
    }

    public ModelAndView getSalesOrdersMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        KwlReturnObject result = null;
        JSONArray DataJArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = accSalesOrderServiceDAOobj.getSalesOrdersMap(request);
            result = accSalesOrderDAOobj.getSalesOrdersMerged(requestParams);
            DataJArr = getSalesOrdersJsonMerged(request, result.getEntityList(), DataJArr);
            issuccess = true;
        } catch (ServiceException ex) {
            msg = "RemoteAPI.getSalesOrdersMerged:" + ex.getMessage();
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while retrieving Sales Orders in RemoteAPI.getSalesOrdersMerged()", ex);
        } catch (Exception ex) {
            msg = "RemoteAPI.getSalesOrdersMerged:" + ex.getMessage();
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while retrieving Sales Orders in RemoteAPI.getSalesOrdersMerged()", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", DataJArr);

            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while retrieving Sales Orders in RemoteAPI.getSalesOrdersMerged()", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString().replaceAll("%", "%25"));
    }

    public JSONArray getSalesOrdersJsonMerged(HttpServletRequest request, List list, JSONArray jArr) throws ServiceException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = accSalesOrderServiceDAOobj.getSalesOrdersMap(request);
            boolean closeflag = request.getParameter("closeflag") != null ? true : false;
            boolean isLeaseSO = Boolean.FALSE.parseBoolean(request.getParameter("isLeaseFixedAsset"));
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, isLeaseSO ? Constants.Acc_Lease_Order_ModuleId : Constants.Acc_Sales_Order_ModuleId, 0));

            boolean isOutstanding = request.getParameter("isOutstanding") != null ? Boolean.parseBoolean(request.getParameter("isOutstanding")) : false;
            boolean iscustomeridpresent = !StringUtil.isNullOrEmpty(request.getParameter("erpcustomerid"));
            String custid = "";
            if (iscustomeridpresent) {
                custid = request.getParameter("erpcustomerid");
            }
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] oj = (Object[]) itr.next();
                String orderid = oj[0].toString();
                {
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), orderid);
                    SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
                    KWLCurrency currency = null;
                    if (salesOrder.getCurrency() != null) {
                        currency = salesOrder.getCurrency();
                    } else {
                        currency = salesOrder.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : salesOrder.getCustomer().getAccount().getCurrency();
                    }
                    Customer customer = salesOrder.getCustomer();
                    JSONObject obj = new JSONObject();
                    if (!customer.getID().equalsIgnoreCase(custid)) {
                        continue;
                    }
                    obj.put("billid", salesOrder.getID());
                    obj.put("companyid", salesOrder.getCompany().getCompanyID());
                    obj.put("companyname", salesOrder.getCompany().getCompanyName());
                    obj.put("externalcurrencyrate", salesOrder.getExternalCurrencyRate());
                    obj.put("withoutinventory", false);
                    obj.put("personid", customer.getID());
                    obj.put("billno", salesOrder.getSalesOrderNumber());
                    obj.put("duedate", authHandler.getDateFormatter(request).format(salesOrder.getDueDate()));
                    obj.put("shipdate", salesOrder.getShipdate() == null ? "" : authHandler.getDateFormatter(request).format(salesOrder.getShipdate()));
                    obj.put("shipvia", salesOrder.getShipvia());
                    obj.put("fob", salesOrder.getFob());
                    obj.put("isOpeningBalanceTransaction", salesOrder.isIsOpeningBalanceSO());
                    obj.put("isConsignment", salesOrder.isIsconsignment());
                    if (salesOrder.getCustWarehouse() != null) {
                        obj.put("custWarehouse", salesOrder.getCustWarehouse().getId());
                    }
                    obj.put("date", authHandler.getDateFormatter(request).format(salesOrder.getOrderDate()));
                    obj.put("isfavourite", salesOrder.isFavourite());
                    obj.put("isprinted", salesOrder.isPrinted());
                    obj.put("billto", salesOrder.getBillTo());
                    obj.put("shipto", salesOrder.getShipTo());
                    obj.put("salesPerson", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getID());
                    obj.put("salespersonname", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getValue());
                    obj.put("createdby", StringUtil.getFullName(salesOrder.getCreatedby()));
                    obj.put("createdbyid", salesOrder.getCreatedby().getUserID());
                    obj.put("deleted", salesOrder.isDeleted());
                    obj.put("gstIncluded", salesOrder.isGstIncluded());
                    obj.put("islockQuantityflag", salesOrder.isLockquantityflag());  //for getting locked flag of indivisual so
                    obj.put("leaseOrMaintenanceSo", salesOrder.getLeaseOrMaintenanceSO());
                    obj.put("maintenanceId", salesOrder.getMaintenance() == null ? "" : salesOrder.getMaintenance().getId());
                    BillingShippingAddresses addresses = salesOrder.getBillingShippingAddresses();
                    AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                    obj.put(Constants.SEQUENCEFORMATID, salesOrder.getSeqformat() == null ? "" : salesOrder.getSeqformat().getID());

                    int pendingApprovalInt = salesOrder.getPendingapproval();
                    obj.put("approvalstatusint", pendingApprovalInt);
                    if (pendingApprovalInt == Constants.LEVEL_ONE) {
                        obj.put("approvalstatus", "Pending level 1 approval");
                    } else if (pendingApprovalInt == Constants.LEVEL_TWO) {
                        obj.put("approvalstatus", "Pending level 2 approval");
                    } else {
                        obj.put("approvalstatus", "");
                    }

                    Iterator itrRow = salesOrder.getRows().iterator();
                    double amount = 0, totalDiscount = 0, discountPrice = 0;
                    double rowTaxAmt = 0d, rowDiscountAmt = 0d;
                    System.out.println(salesOrder.getSalesOrderNumber());
                    while (itrRow.hasNext()) {
                        SalesOrderDetail sod = (SalesOrderDetail) itrRow.next();
                        if (sod.getTax() != null) {
                            requestParams.put("transactiondate", salesOrder.getOrderDate());
                            requestParams.put("taxid", sod.getTax().getID());
                        }
                        double sorate = authHandler.roundUnitPrice(sod.getRate(), companyid);
                        double quantity = authHandler.roundQuantity(sod.getQuantity(), companyid);

                        double quotationPrice = authHandler.round(quantity * sorate, companyid);
                        double discountSOD = authHandler.round(sod.getDiscount(), companyid);

                        if (sod.getDiscountispercent() == 1) {
                            discountPrice = (quotationPrice) - authHandler.round((quotationPrice * discountSOD / 100), companyid);
                            rowDiscountAmt += authHandler.round((quotationPrice * discountSOD / 100), companyid);
                        } else {
                            discountPrice = quotationPrice - discountSOD;
                            rowDiscountAmt += discountSOD;
                        }

                        rowTaxAmt += sod.getRowTaxAmount();
                        amount += discountPrice + authHandler.round(sod.getRowTaxAmount(), companyid);//amount += discountPrice + (discountPrice * rowTaxPercent/100);
                    }
                    double discountSO = authHandler.round(salesOrder.getDiscount(), companyid);
                    if (discountSO != 0) {
                        if (salesOrder.isPerDiscount()) {
                            totalDiscount = authHandler.round(amount * discountSO / 100, companyid);
                            amount = amount - totalDiscount;
                        } else {
                            amount = amount - discountSO;
                            totalDiscount = discountSO;
                        }
                        obj.put("discounttotal", discountSO);
                    } else {
                        obj.put("discounttotal", 0);
                    }
                    obj.put("discount", rowDiscountAmt);
                    obj.put("discountispertotal", salesOrder.isPerDiscount());
                    obj.put("amount", amount);
                    if (salesOrder.isPerDiscount()) {
                        obj.put("ispercentdiscount", salesOrder.isPerDiscount());
                        obj.put("discountval", discountSO);
                    } else {
                        obj.put("discountval", totalDiscount);    //obj.put("discountval", salesOrder.getDiscount());
                    }
                    try {
                        obj.put("creditDays", salesOrder.getTerm().getTermdays());
                    } catch (Exception ex) {
                        obj.put("creditDays", 0);
                    }
                    RepeatedSalesOrder repeatedSO = salesOrder.getRepeateSO();
                    obj.put("isRepeated", repeatedSO == null ? false : true);
                    if (repeatedSO != null) {
                        obj.put("repeateid", repeatedSO.getId());
                        obj.put("interval", repeatedSO.getIntervalUnit());
                        obj.put("intervalType", repeatedSO.getIntervalType());
                        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
                        obj.put("startDate", sdf.format(repeatedSO.getStartDate()));
                        obj.put("NoOfpost", repeatedSO.getNoOfSOpost());
                        obj.put("NoOfRemainpost", repeatedSO.getNoOfRemainSOpost());
                        obj.put("nextDate", sdf.format(repeatedSO.getNextDate()));
                        obj.put("expireDate", repeatedSO.getExpireDate() == null ? "" : sdf.format(repeatedSO.getExpireDate()));
                        requestParams.put("parentSOId", salesOrder.getID());
                        KwlReturnObject details = accSalesOrderDAOobj.getRepeateSalesOrderDetails(requestParams);
                        List detailsList = details.getEntityList();
                        obj.put("childCount", detailsList.size());
                    }
                    double totalTermAmount = 0;
                    HashMap<String, Object> requestParam = new HashMap();
                    requestParam.put("salesOrder", salesOrder.getID());
                    KwlReturnObject salesOrderResult = null;
                    salesOrderResult = accSalesOrderDAOobj.getSalesOrderTermMap(requestParam);
                    List<SalesOrderTermMap> termMap = salesOrderResult.getEntityList();
                    for (SalesOrderTermMap salesOrderTermMap : termMap) {
                        double termAmnt = salesOrderTermMap.getTermamount();
                        totalTermAmount += authHandler.round(termAmnt, companyid);
                    }
                    totalTermAmount = authHandler.round(totalTermAmount, companyid);
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("basecurrencysymbol", salesOrder.getCustomer().getAccount().getCurrency().getSymbol());
                    obj.put("taxid", salesOrder.getTax() == null ? "" : salesOrder.getTax().getID());
                    obj.put("taxname", salesOrder.getTax() == null ? "" : salesOrder.getTax().getName());
                    obj.put("shiplengthval", salesOrder.getShiplength());
                    obj.put("invoicetype", salesOrder.getInvoicetype());
                    double taxPercent = 0;
                    if (salesOrder.getTax() != null) {
                        requestParams.put("transactiondate", salesOrder.getOrderDate());
                        requestParams.put("taxid", salesOrder.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];

                    }
                    double orderAmount = amount;//(Double) bAmt.getEntityList().get(0);
                    double ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round((orderAmount * taxPercent / 100), companyid));
                    ordertaxamount += rowTaxAmt;
                    obj.put("taxpercent", taxPercent);
                    obj.put("taxamount", ordertaxamount);// Tax Amount
                    amount = amount + totalTermAmount + ordertaxamount;
                    orderAmount += totalTermAmount;
                    obj.put("orderamount", orderAmount);
                    obj.put("orderamountwithTax", amount);// Total Amount

                    KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amount, salesOrder.getCurrency().getCurrencyID(), salesOrder.getOrderDate(), salesOrder.getExternalCurrencyRate());
                    double totalAmountinBase = (Double) bAmt.getEntityList().get(0);
                    obj.put("amountinbase", authHandler.round(totalAmountinBase, companyid)); //Total Amount in base
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("personname", customer.getName());
                    HashMap<String, Object> addressParams = new HashMap<String, Object>();
                    addressParams.put("companyid", companyid);
                    addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                    addressParams.put("isBillingAddress", true);    //true to get billing address
                    addressParams.put("customerid", customer.getID());
                    CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                    obj.put("personemail", customerAddressDetails != null ? customerAddressDetails.getEmailID() : "");
                    obj.put("memo", salesOrder.getMemo());
                    obj.put("posttext", salesOrder.getPostText());
                    obj.put("costcenterid", salesOrder.getCostcenter() == null ? "" : salesOrder.getCostcenter().getID());
                    obj.put("costcenterName", salesOrder.getCostcenter() == null ? "" : salesOrder.getCostcenter().getName());
                    obj.put("archieve", 0);
                    boolean includeprotax = false;
                    Set<SalesOrderDetail> salesOrderDetails = salesOrder.getRows();
                    for (SalesOrderDetail salesOrderDetail : salesOrderDetails) {
                        if (salesOrderDetail.getTax() != null) {
                            includeprotax = true;
                            break;
                        }
                    }
                    obj.put("includeprotax", includeprotax);
                    if (salesOrder.getModifiedby() != null) {
                        obj.put("lasteditedby", StringUtil.getFullName(salesOrder.getModifiedby()));
                    }
                    obj.put("termdetails", getTermDetails(salesOrder.getID(), true));
                    obj.put("termamount", CommonFunctions.getTotalTermsAmount(getTermDetails(salesOrder.getID(), true)));
                    String status = "open";//getSalesOrderStatus(salesOrder);
                    obj.put("status", status);

                    if (isOutstanding && status.equalsIgnoreCase("open")) {
                        jArr.put(obj);
                    } else if (!isOutstanding) {
                        if (!closeflag || (closeflag && status.equalsIgnoreCase("open"))) {
                            jArr.put(obj);
                        }
                    }
                }

            }
        } catch (SessionExpiredException | ServiceException | JSONException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while Creating Sales Orders JSON in RemoteAPI.getSalesOrdersJsonMerged()", ex);
            throw ServiceException.FAILURE("getSalesOrdersJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public JSONArray getTermDetails(String id, boolean isOrder) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParam = new HashMap();
            if (isOrder) {
                requestParam.put("salesOrder", id);
                KwlReturnObject curresult = accSalesOrderDAOobj.getSalesOrderTermMap(requestParam);
                List<SalesOrderTermMap> termMap = curresult.getEntityList();
                for (SalesOrderTermMap SalesOrderTermMap : termMap) {
                    InvoiceTermsSales mt = SalesOrderTermMap.getTerm();
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("id", mt.getId());
                    jsonobj.put("term", mt.getTerm());
                    jsonobj.put("glaccount", mt.getAccount().getID());
                    jsonobj.put("sign", mt.getSign());
                    jsonobj.put("formula", mt.getFormula());
                    jsonobj.put("termpercentage", SalesOrderTermMap.getPercentage());
                    jsonobj.put("termamount", SalesOrderTermMap.getTermamount());
                    jArr.put(jsonobj);
                }
            } else {
                requestParam.put("quotation", id);
                KwlReturnObject curresult = accSalesOrderDAOobj.getQuotationTermMap(requestParam);
                List<QuotationTermMap> termMap = curresult.getEntityList();
                for (QuotationTermMap quotationTermMap : termMap) {
                    InvoiceTermsSales mt = quotationTermMap.getTerm();
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("id", mt.getId());
                    jsonobj.put("term", mt.getTerm());
                    jsonobj.put("glaccount", mt.getAccount().getID());
                    jsonobj.put("sign", mt.getSign());
                    jsonobj.put("formula", mt.getFormula());
                    jsonobj.put("termpercentage", quotationTermMap.getPercentage());
                    jsonobj.put("termamount", quotationTermMap.getTermamount());
                    jArr.put(jsonobj);
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while retrieving Term Details in RemoteAPI.getTermDetails()", ex);
        }
        return jArr;
    }

    public ModelAndView getInvoicesMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray DataJArr = new JSONArray();
        try {
            HashMap requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            requestParams.put("companyid", request.getParameter("companyid"));
            KwlReturnObject result = accInvoiceDAOobj.getInvoicesMerged(requestParams);
            List list = result.getEntityList();
            request.setAttribute("isRemoteRequest", true);
            DataJArr = accInvoiceServiceDAO.getInvoiceJsonMerged(request, list, DataJArr);
            DataJArr = filterJsonusingCustomer(DataJArr, request);
            issuccess = true;
        } catch (ServiceException ex) {
            msg = "RemoteAPI.getInvoicesMerged:" + ex.getMessage();
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while retrieving Invoices in RemoteAPI.getInvoicesMerged()", ex);
        } catch (Exception ex) {
            msg = "RemoteAPI.getInvoicesMerged:" + ex.getMessage();
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while retrieving Invoices in RemoteAPI.getInvoicesMerged()", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", DataJArr);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while retrieving Invoices in RemoteAPI.getInvoicesMerged()", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString().replaceAll("%", "%25"));
    }

    public JSONArray filterJsonusingCustomer(JSONArray array, HttpServletRequest request) throws JSONException {
        JSONArray DataJArr = new JSONArray();
        boolean iscustomeridpresent = !StringUtil.isNullOrEmpty(request.getParameter("erpcustomerid"));
        String custid = "";
        if (iscustomeridpresent) {
            custid = request.getParameter("erpcustomerid");
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject jSONObjecta = array.getJSONObject(i);
            String personid = jSONObjecta.optString("personid", "");
            if (custid.equalsIgnoreCase(personid)) {
                DataJArr.put(jSONObjecta);
            }
        }
        return DataJArr;
    }

    public ModelAndView getInvoiceFromLMS(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONObject resObj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isused = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        TransactionStatus status = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            String lmsURL = this.getServletContext().getInitParameter("lmsURL");
            String lmsURL = URLUtil.buildRestURL("lmsURL");
            lmsURL = lmsURL + "financials/invoice";              
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
//            String action = "36";
            try {
                resObj = apiCallHandlerService.restGetMethod(lmsURL, userData.toString());
//                resObj = apiCallHandlerService.callApp(lmsURL, userData, companyid, action);
            } catch (Exception ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while retrieving Invoices from LMS in RemoteAPI.getInvoiceFromLMS()", ex);
            }
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                int counter = 0;
                JSONArray jarr = new JSONArray(resObj.optString("invoicedata"));
                JSONArray array = new JSONArray();
                for (int i = 0; i < jarr.length(); i++) {
                    JSONObject dataMap = jarr.getJSONObject(i);
                    try {
                        status = txnManager.getTransaction(def);
                        Invoice invoice = accInvoiceModuleService.saveInvoiceFromLMS(request, dataMap, counter);
                        invoice.setApprovestatuslevel(11);
                        JSONObject jobj1 = new JSONObject();
                        jobj1.put("invoiceid", invoice.getID());
                        jobj1.put("jeno", invoice.getJournalEntry().getEntryNumber());
                        jobj1.put("jeid", invoice.getJournalEntry().getID());
                        jobj1.put("number", dataMap.optString("number"));
                        jobj1.put("itemid", dataMap.optString("itemid"));
                        array.put(jobj1);
                        txnManager.commit(status);
                    } catch (TransactionException | ServiceException | AccountingException | SessionExpiredException | UnsupportedEncodingException | JSONException ex) {
                        Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while retrieving Invoices from LMS in RemoteAPI.getInvoiceFromLMS()", ex);
                    }
                }

                // Call API To update invoice table at LMS side
                if (jarr.length() > 0) {
                    try {
//                        action = "37";
                        userData.put("invoicedata", array);
                        JSONObject resObj1 = apiCallHandlerService.restPostMethod(lmsURL, userData.toString());
//                        JSONObject resObj1 = apiCallHandlerService.callApp(lmsURL, userData, companyid, action);
                    } catch (Exception ex) {
                        Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while retrieving Invoices from LMS in RemoteAPI.getInvoiceFromLMS()", ex);
                    }

                    if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                        issuccess = true;
                        status = txnManager.getTransaction(def);
                        auditTrailObj.insertAuditLog(AuditAction.INVOICE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has sync " + jarr.length() + " Invoices from LMS ", request, companyid);
                        txnManager.commit(status);
                    }
                }
            }
            msg = "Invoices Sync Successfully";
            issuccess = true;
        } catch (SessionExpiredException | IllegalStateException | JSONException | TransactionException | ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while retrieving Invoices from LMS in RemoteAPI.getInvoiceFromLMS()", ex);
            txnManager.rollback(status);
            isused = true;
            issuccess = false;
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("isused", isused);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while retrieving Invoices from LMS in RemoteAPI.getInvoiceFromLMS()", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getReceiptFromLMS(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        HashMap<String, Object> requestParams = AccountingManager.getSyncAllRequestParams(request);
        requestParams.put("lmsURL", this.getServletContext().getInitParameter("lmsURL"));
        JSONObject jobj = new JSONObject();
        jobj = accReceiptServiceDAO.getReceiptFromLMS(requestParams);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getCostCenter(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        jobj = accMasterItemsService.getMasterItemsForEclaim(request);
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

//    public ModelAndView addOrEditCostCenter(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
//        JSONObject jobj = new JSONObject();
//        jobj = accMasterItemsService.addEditMasterItemsForEclaim(request);
//        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
//    }
//    
    public ModelAndView deleteCostCenter(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        jobj = accMasterItemsService.deleteMasterItemsForEclaim(request);
        try{
        JSONArray jarr = jobj.getJSONArray("data");
        for(int i=0; i<jarr.length();i++){
            JSONObject jb = jarr.getJSONObject(i);
            if(jb.has("isdeleted") && (Boolean)jb.get("isdeleted")){
                String deletemsg = jb.getString("msg");
                auditTrailObj.insertAuditLog(AuditAction.COST_CENTER_DELETED, deletemsg, request, sessionHandlerImpl.getCompanyid(request));
            }
        }
        } catch(JSONException jx){
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while deleting Cost Centers from Accounting in RemoteAPI.deleteCostCenter()", jx);
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    public ModelAndView saveTermFromCRM(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        TransactionStatus status = null;
        JSONObject job = new JSONObject();
        boolean issuccess = true;
        try {
            status = txnManager.getTransaction(def);
            JSONArray jArr = new JSONArray(request.getParameter("termdetails"));
            String companyid = request.getParameter("companyid");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("termdetails", jArr);
            params.put("companyid", companyid);
            job = accTermService.saveTerm(params);
            txnManager.commit(status);
        } catch (TransactionException | JSONException | ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while saving Terms from CRM in RemoteAPI.saveTermFromCRM()", ex);
            issuccess = false;
            txnManager.rollback(status);
            throw ServiceException.FAILURE("saveTermFromCRM.saveTermFromCRM", ex);
        } finally {
            try {
                job.put("success", issuccess);
                job.put("companyexist", true);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Error occurred while saving Terms from CRM in RemoteAPI.saveTermFromCRM()", ex);
            }
        }
        return new ModelAndView("jsonView", "model", job.toString());
    }
    public ModelAndView sendDefaultColumnsOfProduct(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        TransactionStatus status = null;
        JSONObject job = new JSONObject();
        boolean issuccess = true;
        try {
            status = txnManager.getTransaction(def);
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put(Constants.moduleid, request.getParameter("moduleid"));
            job = accProductModuleService.getDefaultColumns(params);
            txnManager.commit(status);
        } catch (Exception ex) {
            issuccess = false;
            txnManager.rollback(status);
            throw ServiceException.FAILURE("RemoteAPI.sendDefaultColumnsOfProduct", ex);
        } finally {
            try {
                job.put("success", issuccess);
                job.put("companyexist", true);
            } catch (JSONException ex) {
                Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", job.toString());
    }
}
