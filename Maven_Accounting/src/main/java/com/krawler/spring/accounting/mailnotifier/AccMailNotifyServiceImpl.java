/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.mailnotifier;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.AccCustomData;
import com.krawler.common.admin.AccountingAddressManager;
import com.krawler.common.admin.BillingShippingAddresses;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.CustomerAddressDetails;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.NotificationRules;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.FieldConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.documentdesigner.AccDocumentDesignService;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.servlets.ProfileImageServlet;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.DeliveryOrder;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.GoodsReceiptOrder;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.Payment;
import com.krawler.hql.accounting.PurchaseOrder;
import com.krawler.hql.accounting.PurchaseRequisition;
import com.krawler.hql.accounting.PurchaseReturn;
import com.krawler.hql.accounting.Quotation;
import com.krawler.hql.accounting.Receipt;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.SalesReturn;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Vendor;
import com.krawler.hql.accounting.VendorQuotation;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.customDesign.LineItemColumnModuleMapping;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptCMN;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.handler.AopAdvisor;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.mailNotification.AccMailNotificationDAO;
import com.krawler.spring.accounting.purchaseorder.AccPurchaseOrderServiceDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderControllerCMN;
import com.krawler.spring.accounting.receipt.AccReceiptServiceDAO;
import com.krawler.spring.accounting.salesorder.AccSalesOrderServiceDAO;
import com.krawler.spring.accounting.vendorpayment.AccVendorPaymentServiceDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.CommonFnController;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFuctionality.AccExportReportsServiceDAO;
import com.krawler.spring.exportFuctionality.CreatePDF;
import com.krawler.spring.exportFuctionality.ExportRecordHandler;
import com.krawler.spring.exportFuctionality.ExportRecord_VRNet;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.lowagie.text.DocumentException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class AccMailNotifyServiceImpl implements AccMailNotifyService {

    private AccMailNotificationDAO accMailNotificationDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccPurchaseOrderServiceDAO accPurchaseOrderServiceDAOobj;
    private accAccountDAO accAccountDAOobj;
    private CustomDesignDAO customDesignDAOObj;
    private AccSalesOrderServiceDAO accSalesOrderServiceDAOobj;
    private AccVendorPaymentServiceDAO accVendorPaymentServiceDAOobj;
    private AccReceiptServiceDAO accReceiptServiceDAOobj;
    private accInvoiceCMN accInvoiceCommon;
    private accPurchaseOrderControllerCMN accPurchaseorderCommon;
    private accGoodsReceiptCMN accGoodsReceiptCommon;
    private authHandlerDAO authHandlerDAOObj;
    private accInvoiceDAO accInvoiceDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private MessageSource messageSource;
    private ExportRecord_VRNet exportrecordVRnetObj;
    private CreatePDF createPDFObj;
    private auditTrailDAO auditTrailObj;
    private AccExportReportsServiceDAO accExportOtherReportsServiceDAOobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccDocumentDesignService accDocumentDesignService;
        
    public AccMailNotificationDAO getAccMailNotificationDAOObj() {
        return accMailNotificationDAOObj;
    }

    public void setAccMailNotificationDAOObj(AccMailNotificationDAO accMailNotificationDAOObj) {
        this.accMailNotificationDAOObj = accMailNotificationDAOObj;
    }

    public AccountingHandlerDAO getAccountingHandlerDAOobj() {
        return accountingHandlerDAOobj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public AccPurchaseOrderServiceDAO getAccPurchaseOrderServiceDAOobj() {
        return accPurchaseOrderServiceDAOobj;
    }

    public void setAccPurchaseOrderServiceDAOobj(AccPurchaseOrderServiceDAO accPurchaseOrderServiceDAOobj) {
        this.accPurchaseOrderServiceDAOobj = accPurchaseOrderServiceDAOobj;
    }

    public accAccountDAO getAccAccountDAOobj() {
        return accAccountDAOobj;
    }

    public void setAccAccountDAOobj(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public CustomDesignDAO getCustomDesignDAOObj() {
        return customDesignDAOObj;
    }

    public void setCustomDesignDAOObj(CustomDesignDAO customDesignDAOObj) {
        this.customDesignDAOObj = customDesignDAOObj;
    }

    public AccSalesOrderServiceDAO getAccSalesOrderServiceDAOobj() {
        return accSalesOrderServiceDAOobj;
    }

    public void setAccSalesOrderServiceDAOobj(AccSalesOrderServiceDAO accSalesOrderServiceDAOobj) {
        this.accSalesOrderServiceDAOobj = accSalesOrderServiceDAOobj;
    }

    public AccVendorPaymentServiceDAO getAccVendorPaymentServiceDAOobj() {
        return accVendorPaymentServiceDAOobj;
    }

    public void setAccVendorPaymentServiceDAOobj(AccVendorPaymentServiceDAO accVendorPaymentServiceDAOobj) {
        this.accVendorPaymentServiceDAOobj = accVendorPaymentServiceDAOobj;
    }

    public void setAccReceiptServiceDAOobj(AccReceiptServiceDAO accReceiptServiceDAOobj) {
        this.accReceiptServiceDAOobj = accReceiptServiceDAOobj;
    }

    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }

    public void setAccGoodsReceiptCommon(accGoodsReceiptCMN accGoodsReceiptCommon) {
        this.accGoodsReceiptCommon = accGoodsReceiptCommon;
    }

    public void setAccPurchaseorderCommon(accPurchaseOrderControllerCMN accPurchaseorderCommon) {
        this.accPurchaseorderCommon = accPurchaseorderCommon;
    }

    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }

    public void setAccInvoiceDAOobj(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public kwlCommonTablesDAO getKwlCommonTablesDAOObj() {
        return kwlCommonTablesDAOObj;
    }

    public void setKwlCommonTablesDAOObj(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setExportrecordVRnetObj(ExportRecord_VRNet exportrecordVRnetObj) {
        this.exportrecordVRnetObj = exportrecordVRnetObj;
    }

    public void setCreatePDFObj(CreatePDF createPDFObj) {
        this.createPDFObj = createPDFObj;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }

    public void setAccExportOtherReportsServiceDAOobj(AccExportReportsServiceDAO accExportOtherReportsServiceDAOobj) {
        this.accExportOtherReportsServiceDAOobj = accExportOtherReportsServiceDAOobj;
    }

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setAccDocumentDesignService(AccDocumentDesignService accDocumentDesignService) {
        this.accDocumentDesignService = accDocumentDesignService;
    }

    public JSONObject replacePlaceholdersofEmailContent(JSONObject jsonObj) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject result = null;
        try {
            String companyid = jsonObj.optString(Constants.companyKey);
            String useidid = jsonObj.optString(Constants.userid);
            String fieldid = jsonObj.optString("fieldid");
            Integer moduleid = Integer.parseInt(jsonObj.optString("moduleid"));

            KwlReturnObject user = getAccountingHandlerDAOobj().getObject("com.krawler.common.admin.User", useidid);
            User userobj = (User) user.getEntityList().get(0);

            KwlReturnObject kwl = getAccountingHandlerDAOobj().getObject("com.krawler.common.admin.Company", companyid);
            Company company = (Company) kwl.getEntityList().get(0);
            result = getAccMailNotificationDAOObj().getEmailTemplateToEdit(companyid, moduleid, fieldid);
            // JSONObject jsonObj=StringUtil.convertRequestToJsonObject(request);
            jobj = replacePlaceHolders(jsonObj, company, result, moduleid);

            jobj.put("emailid", !StringUtil.isNullOrEmpty(userobj.getEmailID()) ? userobj.getEmailID() : (!StringUtil.isNullOrEmpty(company.getEmailID()) ? company.getEmailID() : ""));
        } catch (Exception ex) {
            jobj.put("msg", ex.getMessage());
            Logger.getLogger(AccMailNotifyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    public JSONObject replacePlaceHolders(JSONObject jsonObj, Company company, KwlReturnObject result, int moduleid) throws ServiceException, JSONException {
        JSONObject emailBodyjobj = new JSONObject();
        try {
            Iterator<NotificationRules> ite = result.getEntityList().iterator();
            NotificationRules dft = ite.next();
            emailBodyjobj = replaceCommonPlaceHoldersforEmailSubjectBody(jsonObj, company, dft, moduleid, result);
            if (emailBodyjobj.has("emailSubject")) {
                emailBodyjobj.put("subject", emailBodyjobj.optString("emailSubject", ""));
            }
            if (emailBodyjobj.has("emailBody")) {
                emailBodyjobj.put("message", emailBodyjobj.optString("emailBody", ""));
            }
            boolean isMailToShippingEmail = dft.isMailtoshippingemail();
            String templateId = !StringUtil.isNullOrEmpty(dft.getTemplateid()) ? dft.getTemplateid() : "";

            emailBodyjobj.put("templateid", templateId);
            emailBodyjobj.put("isMailToShippingEmail", isMailToShippingEmail);
            emailBodyjobj.put("success", true);

//            jobj.put("subject", subject);
//            jobj.put("message", emailBody);
//            jobj.put("templateid", templateId);
//            jobj.put("isMailToShippingEmail", isMailToShippingEmail);
//            jobj.put("success", true);
        } catch (Exception ex) {
            Logger.getLogger(AccMailNotifyServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return emailBodyjobj;
    }

    public JSONObject replaceCommonPlaceHoldersforEmailSubjectBody(JSONObject jsonObj, Company company, NotificationRules dft, int moduleid, KwlReturnObject result) {
        String emailBody = "", emailSubject = "";
        JSONObject emailsubjectbodyjobj = new JSONObject();
        String mailsubjectsqlquery = "", mailsubjectjson = "", mailbodysqlquery = "", mailbodyjson = "";
        try {

            emailBody = dft.getMailcontent();
            emailSubject = dft.getMailsubject();

            if (!StringUtil.isNullOrEmpty(dft.getMailbodysqlquery())) {
                mailbodysqlquery = dft.getMailbodysqlquery();
            }
            if (!StringUtil.isNullOrEmpty(dft.getMailbodyjson())) {
                mailbodyjson = dft.getMailbodyjson();
            }
            if (!StringUtil.isNullOrEmpty(dft.getMailsubjectsqlquery())) {
                mailsubjectsqlquery = dft.getMailsubjectsqlquery();
            }
            if (!StringUtil.isNullOrEmpty(dft.getMailsubjectjson())) {
                mailsubjectjson = dft.getMailsubjectjson();
            }

//            String accountname = !StringUtil.isNullOrEmpty(jsonObj.optString("personname")) ? jsonObj.optString("personname") : "";
//            String companyPhoneNo = !StringUtil.isNullOrEmpty(company.getPhoneNumber()) ? company.getPhoneNumber() : "";
//            String CompanyEmailId = !StringUtil.isNullOrEmpty(company.getEmailID()) ? company.getEmailID() : "";
            String billid = !StringUtil.isNullOrEmpty(jsonObj.optString("billid")) ? jsonObj.optString("billid") : "";
            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(jsonObj);//User Date Formatter

            if (moduleid == Constants.Acc_Purchase_Order_ModuleId && !StringUtil.isNullOrEmpty(billid)) {//For Purchase Order

                String companyid = jsonObj.optString(Constants.companyKey);
                KwlReturnObject objItr = getAccountingHandlerDAOobj().getObject(PurchaseOrder.class.getName(), billid);
                PurchaseOrder purchaseOrder = (PurchaseOrder) objItr.getEntityList().get(0);
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Purchase_Order_ModuleId, 1));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                replaceFieldMap = new HashMap<String, String>();
                /*
                 * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
                 */
                fieldrequestParams.clear();
                HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Purchase_Order_ModuleId, 1, 0));
                HashMap<String, Integer> DimensionFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

                fieldrequestParams.clear();
                HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Purchase_Order_ModuleId, 1, 1));
                HashMap<String, Integer> LineLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

                //For product custom field
                fieldrequestParams.clear();
                HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                HashMap<String, Integer> ProductLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);

                JSONObject paramJobj = jsonObj;
                
                HashMap<String, Object> paramMap = new HashMap();
                paramMap.put(Constants.fieldMap, FieldMap);
                paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
                paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
                paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
                paramMap.put(Constants.productLevelCustomFieldMap, ProductLevelCustomFieldMap);

                JSONArray lineItemsArr = getAccPurchaseOrderServiceDAOobj().getPODetailsItemJSON(paramJobj, billid, paramMap);
                emailBody = emailBody.replaceAll("#Purchase Order No#", purchaseOrder.getPurchaseOrderNumber());
                emailBody = emailBody.replaceAll("#Purchase Order Date#", purchaseOrder.getOrderDate()!=null?df.format(purchaseOrder.getOrderDate()):"");
                emailBody = emailBody.replaceAll("#Vendor#", purchaseOrder.getVendor() != null ? purchaseOrder.getVendor().getName() : "");
                emailSubject = emailSubject.replaceAll("#Purchase Order No#", purchaseOrder.getPurchaseOrderNumber());
                emailSubject = emailSubject.replaceAll("#Purchase Order Date#",purchaseOrder.getOrderDate()!=null?df.format(purchaseOrder.getOrderDate()):"");
                emailSubject = emailSubject.replaceAll("#Vendor#", purchaseOrder.getVendor() != null ? purchaseOrder.getVendor().getName() : "");
                HashMap<String, Object> customParams = new HashMap<String, Object>();
                /*Email Body*/
                customParams.put(Constants.moduleid, moduleid);
                customParams.put("billid", billid);
                customParams.put("replacehtml", emailBody);
                customParams.put("sqlquery", mailbodysqlquery);
                customParams.put("json", mailbodyjson);
                emailBody = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailBody", emailBody);

                /*Email Subject*/
                customParams.put("replacehtml", emailSubject);
                customParams.put("sqlquery", mailsubjectsqlquery);
                customParams.put("json", mailsubjectjson);
                emailSubject = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailSubject", emailSubject);
            }

            if (moduleid == Constants.Acc_Sales_Order_ModuleId && !StringUtil.isNullOrEmpty(billid)) {//For Sales Order

                //String companyid = AccountingManager.getCompanyidFromRequest(request);
                String companyid = jsonObj.optString(Constants.companyKey);
                KwlReturnObject objItr = getAccountingHandlerDAOobj().getObject(SalesOrder.class.getName(), billid);
                SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                replaceFieldMap = new HashMap<String, String>();

                /*
                 * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
                 */
                fieldrequestParams.clear();
                HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
                HashMap<String, Integer> DimensionFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

                fieldrequestParams.clear();
                HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
                HashMap<String, Integer> LineLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

                //For product custom field
                fieldrequestParams.clear();
                HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                HashMap<String, Integer> ProductLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);
                
                HashMap<String, Object> paramMap = new HashMap();
                paramMap.put(Constants.fieldMap, FieldMap);
                paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
                paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
                paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
                paramMap.put(Constants.productLevelCustomFieldMap, ProductLevelCustomFieldMap);
                
                JSONArray lineItemsArr = getAccSalesOrderServiceDAOobj().getSODetailsItemJSON(jsonObj, billid, paramMap);
                emailBody = emailBody.replaceAll("#Sales Order No#", salesOrder.getSalesOrderNumber());
                emailBody = emailBody.replaceAll("#Sales Order Number#", salesOrder.getSalesOrderNumber());
                emailBody = emailBody.replaceAll("#Sales Order Date#", salesOrder.getOrderDate()!=null?df.format(salesOrder.getOrderDate()):"");
                emailBody = emailBody.replaceAll("#Customer#", salesOrder.getCustomer() != null ? salesOrder.getCustomer().getName() : "");
                emailSubject = emailSubject.replaceAll("#Sales Order No#", salesOrder.getSalesOrderNumber());
                emailSubject = emailSubject.replaceAll("#Sales Order Number#", salesOrder.getSalesOrderNumber());
                emailSubject = emailSubject.replaceAll("#Sales Order Date#", salesOrder.getOrderDate()!=null?df.format(salesOrder.getOrderDate()):"");
                emailSubject = emailSubject.replaceAll("#Customer#", salesOrder.getCustomer() != null ? salesOrder.getCustomer().getName() : "");
                HashMap<String, Object> customParams = new HashMap<String, Object>();
                /*
                 * Email Body
                 */
                customParams.put(Constants.moduleid, moduleid);
                customParams.put("billid", billid);
                customParams.put("replacehtml", emailBody);
                customParams.put("sqlquery", mailbodysqlquery);
                customParams.put("json", mailbodyjson);
                emailBody = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailBody", emailBody);

                /*
                 * Email Subject
                 */
                customParams.put("replacehtml", emailSubject);
                customParams.put("sqlquery", mailsubjectsqlquery);
                customParams.put("json", mailsubjectjson);
                emailSubject = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailSubject", emailSubject);
            }

            if (moduleid == Constants.Acc_Make_Payment_ModuleId && !StringUtil.isNullOrEmpty(billid)) {//For Make Payment

                // String companyid = AccountingManager.getCompanyidFromRequest(request);
                String companyid = jsonObj.optString(Constants.companyKey);
                KwlReturnObject objItr = getAccountingHandlerDAOobj().getObject(Payment.class.getName(), billid);
                Payment payment = (Payment) objItr.getEntityList().get(0);
                HashMap<String, Object> fieldrequestParams = new HashMap();
                HashMap<String, String> customFieldMap = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId, 1));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = getAccAccountDAOobj().getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                replaceFieldMap = new HashMap<String, String>();
                /*
                 * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
                 */
                fieldrequestParams.clear();
                HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId, 1, 0));
                HashMap<String, Integer> DimensionFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

                fieldrequestParams.clear();
                HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId, 1, 1));
                HashMap<String, Integer> LineLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, customfieldFieldMap);
                
                HashMap<String, Object> paramMap = new HashMap();
                paramMap.put(Constants.fieldMap, FieldMap);
                paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
                paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
                paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
                
                JSONArray lineItemsArr = getAccVendorPaymentServiceDAOobj().getMPDetailsItemJSONNew(jsonObj, billid, paramMap);
                String Customer_vendorName = "";
                if (payment.getPaymentWindowType() == 1) {
                    Customer_vendorName = payment.getVendor().getName();
                } else if (payment.getPaymentWindowType() == 2) {
                    KwlReturnObject custresult = getAccountingHandlerDAOobj().getObject(Customer.class.getName(), payment.getCustomer());
                    Customer customer = (Customer) custresult.getEntityList().get(0);
                    Customer_vendorName = customer.getName();
                } else {
                    Customer_vendorName = "";
                }
                emailBody = emailBody.replaceAll("#Payment No#", payment.getPaymentNumber());
                emailBody = emailBody.replaceAll("#Customer/ Vendor Name#", Customer_vendorName);
                emailSubject = emailSubject.replaceAll("#Payment No#", payment.getPaymentNumber());
                emailSubject = emailSubject.replaceAll("#Customer/ Vendor Name#", payment.getVendor() != null ? payment.getVendor().getName() : "");
                HashMap<String, Object> customParams = new HashMap<String, Object>();
                /*
                 * Email Body
                 */
                customParams.put(Constants.moduleid, moduleid);
                customParams.put("billid", billid);
                customParams.put("replacehtml", emailBody);
                customParams.put("sqlquery", mailbodysqlquery);
                customParams.put("json", mailbodyjson);
                emailBody = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailBody", emailBody);

                /*
                 * Email Subject
                 */
                customParams.put("replacehtml", emailSubject);
                customParams.put("sqlquery", mailsubjectsqlquery);
                customParams.put("json", mailsubjectjson);
                emailSubject = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailSubject", emailSubject);
            }
            if (moduleid == Constants.Acc_Receive_Payment_ModuleId && !StringUtil.isNullOrEmpty(billid)) {//For Receipt Payment

                //String companyid = AccountingManager.getCompanyidFromRequest(request);
                String companyid = jsonObj.optString(Constants.companyKey);
                KwlReturnObject objItr = getAccountingHandlerDAOobj().getObject(Receipt.class.getName(), billid);
                Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                HashMap<String, Object> fieldrequestParams = new HashMap();
                HashMap<String, String> customFieldMap = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId, 1));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = getAccAccountDAOobj().getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                replaceFieldMap = new HashMap<String, String>();

                /*
                 * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
                 */
                fieldrequestParams.clear();
                HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId, 1, 0));
                HashMap<String, Integer> DimensionFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

                fieldrequestParams.clear();
                HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId, 1, 1));
                HashMap<String, Integer> LineLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, customfieldFieldMap);
                
                HashMap<String, Object> paramMap = new HashMap();
                paramMap.put(Constants.fieldMap, FieldMap);
                paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
                paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
                paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
                
                JSONArray lineItemsArr = accReceiptServiceDAOobj.getRPDetailsItemJSONNew(jsonObj, billid, paramMap);
                String Customer_vendorName = "";
                if (receipt.getPaymentWindowType() == 1) {
                    Customer_vendorName = receipt.getCustomer().getName();
                } else if (receipt.getPaymentWindowType() == 2) {
                    KwlReturnObject custresult = getAccountingHandlerDAOobj().getObject(Vendor.class.getName(), receipt.getVendor());
                    Vendor vendor = (Vendor) custresult.getEntityList().get(0);
                    Customer_vendorName = vendor.getName();
                } else {
                    Customer_vendorName = "";
                }
                emailBody = emailBody.replaceAll("#Receipt No#", receipt.getReceiptNumber());
                emailBody = emailBody.replaceAll("#Customer/ Vendor Name#", Customer_vendorName);
                emailSubject = emailSubject.replaceAll("#Receipt No#", receipt.getReceiptNumber());
                emailSubject = emailSubject.replaceAll("#Customer/ Vendor Name#", Customer_vendorName);
                HashMap<String, Object> customParams = new HashMap<String, Object>();
                /*
                 * Email Body
                 */
                customParams.put(Constants.moduleid, moduleid);
                customParams.put("billid", billid);
                customParams.put("replacehtml", emailBody);
                customParams.put("sqlquery", mailbodysqlquery);
                customParams.put("json", mailbodyjson);
                emailBody = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailBody", emailBody);

                /*
                 * Email Subject
                 */
                customParams.put("replacehtml", emailSubject);
                customParams.put("sqlquery", mailsubjectsqlquery);
                customParams.put("json", mailsubjectjson);
                emailSubject = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailSubject", emailSubject);
            }
            if (moduleid == Constants.Acc_Invoice_ModuleId && !StringUtil.isNullOrEmpty(billid)) {//For Sales Invoice

                //String companyid = AccountingManager.getCompanyidFromRequest(request);
                String companyid = jsonObj.optString(Constants.companyKey);
                KwlReturnObject objItr = getAccountingHandlerDAOobj().getObject(Invoice.class.getName(), billid);
                Invoice invoice = (Invoice) objItr.getEntityList().get(0);

                Customer customer = invoice.getCustomer();
                emailsubjectbodyjobj.put("entryno", invoice.getInvoiceNumber());
                emailsubjectbodyjobj.put("personName", invoice.getCustomer().getName());
                BillingShippingAddresses addresses = invoice.getBillingShippingAddresses();
                emailsubjectbodyjobj.put(Constants.BILLING_EMAIL, addresses == null ? "" : addresses.getBillingEmail());
                emailsubjectbodyjobj.put(Constants.SHIPPING_EMAIL, addresses == null ? "" : addresses.getShippingEmail());
//                HashMap<String, Object> addressParams = new HashMap<String, Object>();
//                addressParams.put(Constants.companyKey, companyid);
//                addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
//                addressParams.put("isBillingAddress", true);    //true to get billing address
//                addressParams.put(Constants.customerid, customer != null ? customer.getID() : "");
//                CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
//                emailsubjectbodyjobj.put(BillingShippingAddresses.personemail, customerAddressDetails != null ? customerAddressDetails.getEmailID() : "");
//                emailsubjectbodyjobj.put(BillingShippingAddresses.billtoaddress, invoice.getBillingShippingAddresses() == null ? "" : CommonFunctions.getBillingShippingAddress(invoice.getBillingShippingAddresses(), true));
//                emailsubjectbodyjobj.put(BillingShippingAddresses.shiptoaddress, invoice.getBillingShippingAddresses() == null ? "" : CommonFunctions.getBillingShippingAddress(invoice.getBillingShippingAddresses(), false));

                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                replaceFieldMap = new HashMap<String, String>();
                /*
                 * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
                 */
                fieldrequestParams.clear();
                HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
                HashMap<String, Integer> DimensionFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

                fieldrequestParams.clear();
                HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
                HashMap<String, Integer> LineLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

                //For product custom field
                fieldrequestParams.clear();
                HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                HashMap<String, Integer> ProductLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);

                HashMap<String, Object> paramMap = new HashMap();
                paramMap.put(Constants.fieldMap, FieldMap);
                paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
                paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
                paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
                paramMap.put(Constants.productLevelCustomFieldMap, ProductLevelCustomFieldMap);
                
                JSONArray lineItemsArr = accInvoiceCommon.getInvoiceDetailsItemJSON(jsonObj, billid, paramMap);

                emailBody = emailBody.replaceAll("#Invoice Number#", invoice.getInvoiceNumber());
                emailBody = emailBody.replaceAll("#Invoice Date#", invoice.getCreationDate() != null ? df.format(invoice.getCreationDate()) : "");
                emailBody = emailBody.replaceAll("#Customer#", invoice.getCustomer() != null ? invoice.getCustomer().getName() : "");
                emailSubject = emailSubject.replaceAll("#Invoice Number#", invoice.getInvoiceNumber());
                emailSubject = emailSubject.replaceAll("#Invoice Date#", invoice.getCreationDate() != null ? df.format(invoice.getCreationDate()) : "");
                emailSubject = emailSubject.replaceAll("#Customer#", invoice.getCustomer() != null ? invoice.getCustomer().getName() : "");
                HashMap<String, Object> customParams = new HashMap<String, Object>();
                /*
                 * Email Body
                 */
                customParams.put(Constants.moduleid, moduleid);
                customParams.put("billid", billid);
                customParams.put("replacehtml", emailBody);
                customParams.put("sqlquery", mailbodysqlquery);
                customParams.put("json", mailbodyjson);
                emailBody = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailBody", emailBody);

                /*
                 * Email Subject
                 */
                customParams.put("replacehtml", emailSubject);
                customParams.put("sqlquery", mailsubjectsqlquery);
                customParams.put("json", mailsubjectjson);
                emailSubject = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailSubject", emailSubject);
            }
            if (moduleid == Constants.Acc_Vendor_Invoice_ModuleId && !StringUtil.isNullOrEmpty(billid)) {//For Purchase Invoice

                //String companyid = AccountingManager.getCompanyidFromRequest(request);
                String companyid = jsonObj.optString(Constants.companyKey);
                KwlReturnObject objItr = getAccountingHandlerDAOobj().getObject(GoodsReceipt.class.getName(), billid);
                GoodsReceipt goodsReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                replaceFieldMap = new HashMap<String, String>();
                /*
                 * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
                 */
                fieldrequestParams.clear();
                HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
                HashMap<String, Integer> DimensionFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

                fieldrequestParams.clear();
                HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
                HashMap<String, Integer> LineLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

                //For product custom field
                fieldrequestParams.clear();
                HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                HashMap<String, Integer> ProductLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);

                HashMap<String, Object> paramMap = new HashMap();
                paramMap.put(Constants.fieldMap, FieldMap);
                paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
                paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
                paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
                paramMap.put(Constants.productLevelCustomFieldMap, ProductLevelCustomFieldMap);
                
                JSONArray lineItemsArr = accGoodsReceiptCommon.getGoodsReceiptDetailsItemJSON(jsonObj, billid, paramMap);

                emailBody = emailBody.replaceAll("#Vendor Invoice Number#", goodsReceipt.getGoodsReceiptNumber());
                emailBody = emailBody.replaceAll("#Vendor Invoice Date#", goodsReceipt.getCreationDate() != null ? df.format(goodsReceipt.getCreationDate()) : "");
                emailBody = emailBody.replaceAll("#Vendor#", goodsReceipt.getVendor() != null ? goodsReceipt.getVendor().getName() : "");
                emailSubject = emailSubject.replaceAll("#Vendor Invoice Number#", goodsReceipt.getGoodsReceiptNumber());
                emailSubject = emailSubject.replaceAll("#Vendor Invoice Date#", goodsReceipt.getCreationDate() != null ? df.format(goodsReceipt.getCreationDate()) : "");
                emailSubject = emailSubject.replaceAll("#Vendor#", goodsReceipt.getVendor() != null ? goodsReceipt.getVendor().getName() : "");
                HashMap<String, Object> customParams = new HashMap<String, Object>();
                /*
                 * Email Body
                 */
                customParams.put(Constants.moduleid, moduleid);
                customParams.put("billid", billid);
                customParams.put("replacehtml", emailBody);
                customParams.put("sqlquery", mailbodysqlquery);
                customParams.put("json", mailbodyjson);
                emailBody = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailBody", emailBody);

                /*
                 * Email Subject
                 */
                customParams.put("replacehtml", emailSubject);
                customParams.put("sqlquery", mailsubjectsqlquery);
                customParams.put("json", mailsubjectjson);
                emailSubject = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailSubject", emailSubject);
            }
            if (moduleid == Constants.Acc_Delivery_Order_ModuleId && !StringUtil.isNullOrEmpty(billid)) {//For Delivery Order

                //String companyid = AccountingManager.getCompanyidFromRequest(request);
                String companyid = jsonObj.optString(Constants.companyKey);
                KwlReturnObject objItr = getAccountingHandlerDAOobj().getObject(DeliveryOrder.class.getName(), billid);
                DeliveryOrder deliveryOrder = (DeliveryOrder) objItr.getEntityList().get(0);
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                replaceFieldMap = new HashMap<String, String>();
                /*
                 * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
                 */
                fieldrequestParams.clear();
                HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
                HashMap<String, Integer> DimensionFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

                fieldrequestParams.clear();
                HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
                HashMap<String, Integer> LineLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

                //For product custom field
                fieldrequestParams.clear();
                HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                HashMap<String, Integer> ProductLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);
                
                HashMap<String, Object> paramMap = new HashMap();
                paramMap.put(Constants.fieldMap, FieldMap);
                paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
                paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
                paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
                paramMap.put(Constants.productLevelCustomFieldMap, ProductLevelCustomFieldMap);
                
                JSONArray lineItemsArr = accInvoiceCommon.getDODetailsItemJSON(jsonObj, billid, paramMap);

                emailBody = emailBody.replaceAll("#Delivery Order Number#", deliveryOrder.getDeliveryOrderNumber());
                emailBody = emailBody.replaceAll("#Delivery Order Date#", deliveryOrder.getOrderDate() != null ? df.format(deliveryOrder.getOrderDate()) : "");
                emailBody = emailBody.replaceAll("#Customer#", deliveryOrder.getCustomer() != null ? deliveryOrder.getCustomer().getName() : "");
                emailSubject = emailSubject.replaceAll("#Delivery Order Number#", deliveryOrder.getDeliveryOrderNumber());
                emailSubject = emailSubject.replaceAll("#Delivery Order Date#", deliveryOrder.getOrderDate() != null ? df.format(deliveryOrder.getOrderDate()) : "");
                emailSubject = emailSubject.replaceAll("#Customer#", deliveryOrder.getCustomer() != null ? deliveryOrder.getCustomer().getName() : "");
                HashMap<String, Object> customParams = new HashMap<String, Object>();
                /*
                 * Email Body
                 */
                customParams.put(Constants.moduleid, moduleid);
                customParams.put("billid", billid);
                customParams.put("replacehtml", emailBody);
                customParams.put("sqlquery", mailbodysqlquery);
                customParams.put("json", mailbodyjson);
                emailBody = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailBody", emailBody);

                /*
                 * Email Subject
                 */
                customParams.put("replacehtml", emailSubject);
                customParams.put("sqlquery", mailsubjectsqlquery);
                customParams.put("json", mailsubjectjson);
                emailSubject = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailSubject", emailSubject);
            }
            if (moduleid == Constants.Acc_Goods_Receipt_ModuleId && !StringUtil.isNullOrEmpty(billid)) {//For Goods Receipt Order

                //String companyid = AccountingManager.getCompanyidFromRequest(request);
                String companyid = jsonObj.optString(Constants.companyKey);
                KwlReturnObject objItr = getAccountingHandlerDAOobj().getObject(GoodsReceiptOrder.class.getName(), billid);
                GoodsReceiptOrder goodsReceiptOrder = (GoodsReceiptOrder) objItr.getEntityList().get(0);
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                replaceFieldMap = new HashMap<String, String>();
                /*
                 * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
                 */
                fieldrequestParams.clear();
                HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
                HashMap<String, Integer> DimensionFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

                fieldrequestParams.clear();
                HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
                HashMap<String, Integer> LineLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

                //For product custom field
                fieldrequestParams.clear();
                HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                
                HashMap<String, Object> paramMap = new HashMap();
                paramMap.put(Constants.fieldMap, FieldMap);
                paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
                paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
                paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
                
                JSONArray lineItemsArr = accGoodsReceiptCommon.getGRODetailsItemJSON(jsonObj, billid, paramMap);

                emailBody = emailBody.replaceAll("#Goods Receipt Number#", goodsReceiptOrder.getGoodsReceiptOrderNumber());
                emailBody = emailBody.replaceAll("#Goods Receipt Date#", goodsReceiptOrder.getOrderDate() != null ? df.format(goodsReceiptOrder.getOrderDate()) : "");
                emailBody = emailBody.replaceAll("#Vendor#", goodsReceiptOrder.getVendor() != null ? goodsReceiptOrder.getVendor().getName() : "");
                emailSubject = emailSubject.replaceAll("#Goods Receipt Number#", goodsReceiptOrder.getGoodsReceiptOrderNumber());
                emailSubject = emailSubject.replaceAll("#Goods Receipt Date#", goodsReceiptOrder.getOrderDate() != null ? df.format(goodsReceiptOrder.getOrderDate()) : "");
                emailSubject = emailSubject.replaceAll("#Vendor#", goodsReceiptOrder.getVendor() != null ? goodsReceiptOrder.getVendor().getName() : "");
                HashMap<String, Object> customParams = new HashMap<String, Object>();
                /*
                 * Email Body
                 */
                customParams.put(Constants.moduleid, moduleid);
                customParams.put("billid", billid);
                customParams.put("replacehtml", emailBody);
                customParams.put("sqlquery", mailbodysqlquery);
                customParams.put("json", mailbodyjson);
                emailBody = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailBody", emailBody);

                /*
                 * Email Subject
                 */
                customParams.put("replacehtml", emailSubject);
                customParams.put("sqlquery", mailsubjectsqlquery);
                customParams.put("json", mailsubjectjson);
                emailSubject = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailSubject", emailSubject);
            }
            if (moduleid == Constants.Acc_Sales_Return_ModuleId && !StringUtil.isNullOrEmpty(billid)) {//For Sales Return

                //String companyid = AccountingManager.getCompanyidFromRequest(request);
                String companyid = jsonObj.optString(Constants.companyKey);
                KwlReturnObject objItr = getAccountingHandlerDAOobj().getObject(SalesReturn.class.getName(), billid);
                SalesReturn salesReturn = (SalesReturn) objItr.getEntityList().get(0);
                JSONArray lineItemsArr = accInvoiceCommon.getSalesReturnDetailsItemJSON(jsonObj, billid, moduleid);

                emailBody = emailBody.replaceAll("#Sales Return Number#", salesReturn.getSalesReturnNumber());
                emailBody = emailBody.replaceAll("#Sales Return Date#", salesReturn.getOrderDate() != null ? df.format(salesReturn.getOrderDate()): "");
                emailBody = emailBody.replaceAll("#Customer#", salesReturn.getCustomer() != null ? salesReturn.getCustomer().getName() : "");
                emailSubject = emailSubject.replaceAll("#Sales Return Number#", salesReturn.getSalesReturnNumber());
                emailSubject = emailSubject.replaceAll("#Sales Return Date#", salesReturn.getOrderDate() != null ? df.format(salesReturn.getOrderDate()): "");
                emailSubject = emailSubject.replaceAll("#Customer#", salesReturn.getCustomer() != null ? salesReturn.getCustomer().getName() : "");
                HashMap<String, Object> customParams = new HashMap<String, Object>();
                /*
                 * Email Body
                 */
                customParams.put(Constants.moduleid, moduleid);
                customParams.put("billid", billid);
                customParams.put("replacehtml", emailBody);
                customParams.put("sqlquery", mailbodysqlquery);
                customParams.put("json", mailbodyjson);
                emailBody = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailBody", emailBody);

                /*
                 * Email Subject
                 */
                customParams.put("replacehtml", emailSubject);
                customParams.put("sqlquery", mailsubjectsqlquery);
                customParams.put("json", mailsubjectjson);
                emailSubject = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailSubject", emailSubject);
            }
            if (moduleid == Constants.Acc_Purchase_Return_ModuleId && !StringUtil.isNullOrEmpty(billid)) {//For Purchase Return

                //String companyid = AccountingManager.getCompanyidFromRequest(request);
                String companyid = jsonObj.optString(Constants.companyKey);
                KwlReturnObject objItr = getAccountingHandlerDAOobj().getObject(PurchaseReturn.class.getName(), billid);
                PurchaseReturn purchaseReturn = (PurchaseReturn) objItr.getEntityList().get(0);
                JSONArray lineItemsArr = accGoodsReceiptCommon.getPurchaseReturnDetailsItemJSON(jsonObj, billid, moduleid);

                emailBody = emailBody.replaceAll("#Purchase Return Number#", purchaseReturn.getPurchaseReturnNumber());
                emailBody = emailBody.replaceAll("#Purchase Return Date#", purchaseReturn.getOrderDate() != null ? df.format(purchaseReturn.getOrderDate()) : "");
                emailBody = emailBody.replaceAll("#Vendor#", purchaseReturn.getVendor() != null ? purchaseReturn.getVendor().getName() : "");
                emailSubject = emailSubject.replaceAll("#Purchase Return Number#", purchaseReturn.getPurchaseReturnNumber());
                emailSubject = emailSubject.replaceAll("#Purchase Return Date#", purchaseReturn.getOrderDate() != null ? df.format(purchaseReturn.getOrderDate()) : "");
                emailSubject = emailSubject.replaceAll("#Vendor#", purchaseReturn.getVendor() != null ? purchaseReturn.getVendor().getName() : "");
                HashMap<String, Object> customParams = new HashMap<String, Object>();
                /*
                 * Email Body
                 */
                customParams.put(Constants.moduleid, moduleid);
                customParams.put("billid", billid);
                customParams.put("replacehtml", emailBody);
                customParams.put("sqlquery", mailbodysqlquery);
                customParams.put("json", mailbodyjson);
                emailBody = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailBody", emailBody);

                /*
                 * Email Subject
                 */
                customParams.put("replacehtml", emailSubject);
                customParams.put("sqlquery", mailsubjectsqlquery);
                customParams.put("json", mailsubjectjson);
                emailSubject = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailSubject", emailSubject);
            }
            if (moduleid == Constants.Acc_Customer_Quotation_ModuleId && !StringUtil.isNullOrEmpty(billid)) {//For Customer Quotation

                //String companyid = AccountingManager.getCompanyidFromRequest(request);
                String companyid = jsonObj.optString(Constants.companyKey);
                KwlReturnObject objItr = getAccountingHandlerDAOobj().getObject(Quotation.class.getName(), billid);
                Quotation quotation = (Quotation) objItr.getEntityList().get(0);
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                replaceFieldMap = new HashMap<String, String>();
                /*
                 * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
                 */
                fieldrequestParams.clear();
                HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
                HashMap<String, Integer> DimensionFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

                fieldrequestParams.clear();
                HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
                HashMap<String, Integer> LineLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

                //For product custom field
                fieldrequestParams.clear();
                HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                HashMap<String, Integer> ProductLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);
                
                HashMap<String, Object> paramMap = new HashMap();
                paramMap.put(Constants.fieldMap, FieldMap);
                paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
                paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
                paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
                paramMap.put(Constants.productLevelCustomFieldMap, ProductLevelCustomFieldMap);
                
                JSONArray lineItemsArr = accInvoiceCommon.getCustomerQuotationDetailsItemJSON(jsonObj, billid, paramMap);

                emailBody = emailBody.replaceAll("#Customer Quotation Number#", quotation.getQuotationNumber());
                emailBody = emailBody.replaceAll("#Customer Quotation Date#", quotation.getQuotationDate() != null ? df.format(quotation.getQuotationDate()) : "");
                emailBody = emailBody.replaceAll("#Customer#", quotation.getCustomer() != null ? quotation.getCustomer().getName() : "");
                emailSubject = emailSubject.replaceAll("#Customer Quotation Number#", quotation.getQuotationNumber());
                emailSubject = emailSubject.replaceAll("#Customer Quotation Date#", quotation.getQuotationDate() != null ? df.format(quotation.getQuotationDate()) : "");
                emailSubject = emailSubject.replaceAll("#Customer#", quotation.getCustomer() != null ? quotation.getCustomer().getName() : "");
                HashMap<String, Object> customParams = new HashMap<String, Object>();
                /*
                 * Email Body
                 */
                customParams.put(Constants.moduleid, moduleid);
                customParams.put("billid", billid);
                customParams.put("replacehtml", emailBody);
                customParams.put("sqlquery", mailbodysqlquery);
                customParams.put("json", mailbodyjson);
                emailBody = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailBody", emailBody);

                /*
                 * Email Subject
                 */
                customParams.put("replacehtml", emailSubject);
                customParams.put("sqlquery", mailsubjectsqlquery);
                customParams.put("json", mailsubjectjson);
                emailSubject = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailSubject", emailSubject);
            }
            /* Added to replace Common Place Holders for Email Subject and Body  */
            if (moduleid == Constants.Acc_Purchase_Requisition_ModuleId && !StringUtil.isNullOrEmpty(billid)) {//For Purchase Requisition

                //String companyid = AccountingManager.getCompanyidFromRequest(request);
                String companyid = jsonObj.optString(Constants.companyKey);
                KwlReturnObject objItr = getAccountingHandlerDAOobj().getObject(PurchaseRequisition.class.getName(), billid);
                PurchaseRequisition requisition = (PurchaseRequisition) objItr.getEntityList().get(0);
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                replaceFieldMap = new HashMap<String, String>();
                /*
                 * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
                 */
                fieldrequestParams.clear();
                HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
                HashMap<String, Integer> DimensionFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

                fieldrequestParams.clear();
                HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
                HashMap<String, Integer> LineLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

                //For product custom field
                fieldrequestParams.clear();
                HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                HashMap<String, Integer> ProductLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);
                
                HashMap<String, Object> paramMap = new HashMap();
                paramMap.put(Constants.fieldMap, FieldMap);
                paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
                paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
                paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
                paramMap.put(Constants.productLevelCustomFieldMap, ProductLevelCustomFieldMap);
                // Will Return Document Designer Template Json
                JSONArray lineItemsArr = accPurchaseorderCommon.getPRDetailsItemJSON(jsonObj, billid, paramMap);

                emailBody = emailBody.replaceAll("#Purchase Requisition Number#", requisition.getPrNumber());
                emailBody = emailBody.replaceAll("#Company PhoneNo#", requisition.getCompany().getPhoneNumber() != null ? requisition.getCompany().getPhoneNumber(): "");
                emailBody = emailBody.replaceAll("#Company Email#",requisition.getCompany().getEmailID() != null ? requisition.getCompany().getEmailID(): "" );
                emailSubject = emailSubject.replaceAll("#Purchase Requisition Number#", requisition.getPrNumber());
                emailSubject = emailSubject.replaceAll("#Company Name#", requisition.getCompany().getCompanyName());
                HashMap<String, Object> customParams = new HashMap<String, Object>();
                /*
                 * Email Body
                 */
                customParams.put(Constants.moduleid, moduleid);
                customParams.put("billid", billid);
                customParams.put("replacehtml", emailBody);
                customParams.put("sqlquery", mailbodysqlquery);
                customParams.put("json", mailbodyjson);
                emailBody = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailBody", emailBody);

                /*
                 * Email Subject
                 */
                customParams.put("replacehtml", emailSubject);
                customParams.put("sqlquery", mailsubjectsqlquery);
                customParams.put("json", mailsubjectjson);
                emailSubject = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailSubject", emailSubject);
            }
            if (moduleid == Constants.Acc_Vendor_Quotation_ModuleId && !StringUtil.isNullOrEmpty(billid)) {//For Vendor Quotation

                //String companyid = AccountingManager.getCompanyidFromRequest(jsonObj);
                String companyid = jsonObj.optString(Constants.companyKey);
                KwlReturnObject objItr = getAccountingHandlerDAOobj().getObject(VendorQuotation.class.getName(), billid);
                VendorQuotation vendorQuotation = (VendorQuotation) objItr.getEntityList().get(0);
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                replaceFieldMap = new HashMap<String, String>();
                /*
                 * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
                 */
                fieldrequestParams.clear();
                HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
                HashMap<String, Integer> DimensionFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

                fieldrequestParams.clear();
                HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
                HashMap<String, Integer> LineLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

                //For product custom field
                fieldrequestParams.clear();
                HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                HashMap<String, Integer> ProductLevelCustomFieldMap = getAccAccountDAOobj().getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);
                
                HashMap<String, Object> paramMap = new HashMap();
                paramMap.put(Constants.fieldMap, FieldMap);
                paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
                paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
                paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
                paramMap.put(Constants.productLevelCustomFieldMap, ProductLevelCustomFieldMap);
                
                JSONArray lineItemsArr = accGoodsReceiptCommon.getVendorQuotationDetailsItemJSON(jsonObj, billid, paramMap);

                emailBody = emailBody.replaceAll("#Vendor Quotation Number#", vendorQuotation.getQuotationNumber());
                emailBody = emailBody.replaceAll("#Vendor Quotation Date#", vendorQuotation.getQuotationDate() != null ? df.format(vendorQuotation.getQuotationDate()): "");
                emailBody = emailBody.replaceAll("#Vendor#", vendorQuotation.getVendor() != null ? vendorQuotation.getVendor().getName() : "");
                emailSubject = emailSubject.replaceAll("#Vendor Quotation Number#", vendorQuotation.getQuotationNumber());
                emailSubject = emailSubject.replaceAll("#Vendor Quotation Date#", vendorQuotation.getQuotationDate() != null ? df.format(vendorQuotation.getQuotationDate()): "");
                emailSubject = emailSubject.replaceAll("#Vendor#", vendorQuotation.getVendor() != null ? vendorQuotation.getVendor().getName() : "");
                HashMap<String, Object> customParams = new HashMap<String, Object>();
                /*
                 * Email Body
                 */
                customParams.put(Constants.moduleid, moduleid);
                customParams.put("billid", billid);
                customParams.put("replacehtml", emailBody);
                customParams.put("sqlquery", mailbodysqlquery);
                customParams.put("json", mailbodyjson);
                emailBody = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailBody);
                emailBody = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailBody", emailBody);

                /*
                 * Email Subject
                 */
                customParams.put("replacehtml", emailSubject);
                customParams.put("sqlquery", mailsubjectsqlquery);
                customParams.put("json", mailsubjectjson);
                emailSubject = replaceSqlQueryLevelFields(jsonObj, customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceSummaryLevelFields(jsonObj, customParams, lineItemsArr);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), customParams);
                customParams.put("replacehtml", emailSubject);
                emailSubject = replaceCustomFieldsforOtherModule(jsonObj, customParams);
                emailsubjectbodyjobj.put("emailSubject", emailSubject);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccMailNotifyServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return emailsubjectbodyjobj;
    }

    public String replaceSummaryLevelFields(JSONObject jsonObj, HashMap<String, Object> customParams, JSONArray lineItemsArr) throws JSONException, ServiceException {
        String emailsubjectbodyhtml = "";
        int moduleid = 0;
        try {
            if (customParams.containsKey(Constants.moduleid)) {
                moduleid = (Integer) customParams.get(Constants.moduleid);
            }
            if (customParams.containsKey("replacehtml")) {
                emailsubjectbodyhtml = (String) customParams.get("replacehtml");
            }

            for (int cnt = 0; cnt < lineItemsArr.length(); cnt++) {
                JSONObject itemData = null;
                if (moduleid == 14 || moduleid == 16) {
                    itemData = lineItemsArr.getJSONObject(1);
                    cnt = lineItemsArr.length();
                } else {
                    itemData = lineItemsArr.getJSONObject(cnt);
                }
                if (itemData.has("summarydata")) {
                    /*
                     * Summary Items like Total Amount,Total Tax and SubTotal
                     */
                    HashMap<String, String> summaryFields = new HashMap<String, String>();
                    summaryFields = LineItemColumnModuleMapping.InvoiceProductSummaryItems;
                    for (Map.Entry<String, String> entry : summaryFields.entrySet()) {
                        JSONObject staticColInfo = new JSONObject(summaryFields.get(entry.getKey()));
                        if (itemData.has(entry.getKey())) {
                            String value = itemData.get(entry.getKey()).toString();
                            value = value.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
                            emailsubjectbodyhtml = emailsubjectbodyhtml.replaceAll("#" + staticColInfo.getString("label") + "#", value);
                        }
                    }

                    //Multiple Approver Levels Calculation.
                    for (int level = 1; level <= 10; level++) {
                        if (itemData.has(Constants.ApproverLevel + level)) {
                            String value = itemData.get(Constants.ApproverLevel + level).toString();
                            emailsubjectbodyhtml = emailsubjectbodyhtml.replaceAll("#" + Constants.ApproverLevel + level + "#", value);
                        } else {
                            emailsubjectbodyhtml = emailsubjectbodyhtml.replaceAll("#" + Constants.ApproverLevel + level + "#", "");
                        }
                    }

                    /*
                     * Extra Fields
                     */
                    HashMap<String, String> extraFields = new HashMap<String, String>();
                    extraFields = AccountingManager.getExtraFieldsForModule(moduleid);
                    if (extraFields != null) {
                        for (Map.Entry<String, String> entry : extraFields.entrySet()) {
                            JSONObject staticColInfo = new JSONObject(extraFields.get(entry.getKey()));
                            if (itemData.has(entry.getKey())) {
                                String value = itemData.get(entry.getKey()).toString();
                                value = value.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
                                emailsubjectbodyhtml = emailsubjectbodyhtml.replaceAll("#" + staticColInfo.getString("label") + "#", value.replaceAll("!##", ","));
                            }
                        }
                    }
                    /*User Details*/
                    extraFields = CustomDesignerConstants.CurrentUserDetailsMap;
                    if (extraFields != null) {
                        for (Map.Entry<String, String> entry : extraFields.entrySet()) {
                            JSONObject staticColInfo = new JSONObject(extraFields.get(entry.getKey()));
                            if (itemData.has(entry.getKey())) {
                                String value = itemData.get(entry.getKey()).toString();
                                value = value.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
                                emailsubjectbodyhtml = emailsubjectbodyhtml.replaceAll("#" + staticColInfo.getString("label") + "#", value);
                            }
                        }
                    }
                }
            }//end of lineitemsarray

        } catch (Exception ex) {
            Logger.getLogger(AccMailNotifyServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return emailsubjectbodyhtml;
    }

    public String replaceSqlQueryLevelFields(JSONObject jsonObj, HashMap<String, Object> customParams) throws JSONException, ServiceException {
        KwlReturnObject result = null;
        String billid = "", emailsubjectbodyhtml = "", sqlquery = "", json = "";
        int moduleid = 0;
        List list = new ArrayList<>();
        try {
            //String companyid = sessionHandlerImpl.getCompanyid(request);
            String companyid = jsonObj.optString(Constants.companyKey);
            if (customParams.containsKey(Constants.moduleid)) {
                moduleid = (Integer) customParams.get(Constants.moduleid);
            }
            if (customParams.containsKey("billid")) {
                billid = (String) customParams.get("billid");
            }

            if (customParams.containsKey("replacehtml")) {
                emailsubjectbodyhtml = (String) customParams.get("replacehtml");
            }

            if (customParams.containsKey("sqlquery")) {
                sqlquery = (String) customParams.get("sqlquery");
            }

            if (customParams.containsKey("json")) {
                json = (String) customParams.get("json");
            }

            Object[] rows = null;
            if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_ConsignmentInvoice_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where invoice.id = ? ");
            } else if (moduleid == Constants.Acc_Sales_Order_ModuleId || moduleid == Constants.Acc_ConsignmentRequest_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where salesorder.id = ? ");
            } else if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where purchaseorder.id = ? ");
            } else if (moduleid == Constants.Acc_Credit_Note_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where creditnote.id = ? ");
            } else if (moduleid == Constants.Acc_Debit_Note_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where debitnote.id = ? ");
            } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId || moduleid == Constants.Acc_ConsignmentDeliveryOrder_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where deliveryorder.id = ? ");
            } else if (moduleid == Constants.Acc_Goods_Receipt_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where grorder.id = ? ");
            } else if (moduleid == Constants.Acc_Make_Payment_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where payment.id = ? ");
            } else if (moduleid == Constants.Acc_Receive_Payment_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where receipt.id = ? ");
            } else if (moduleid == Constants.Acc_Vendor_Invoice_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where goodsreceipt.id = ? ");
            } else if (moduleid == Constants.Acc_Customer_Quotation_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where quotation.id = ?  ");
            } else if (moduleid == Constants.Acc_Vendor_Quotation_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where vendorquotation.id = ? ");
            } else if (moduleid == Constants.Acc_Sales_Return_ModuleId || moduleid == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where salesreturn.id = ? ");
            } else if (moduleid == Constants.Acc_Purchase_Return_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where purchasereturn.id = ? ");
            } else if (moduleid == Constants.Acc_Stock_Request_ModuleId || moduleid == Constants.Inventory_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where in_goodsrequest.id = ? ");
            } else if (moduleid == Constants.Acc_Stock_Adjustment_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where in_stockadjustment.id = ? ");
            } else if (moduleid == Constants.Acc_InterStore_ModuleId || moduleid == Constants.Acc_InterLocation_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where in_interstoretransfer.id = ? ");
            } else if (moduleid == Constants.Acc_RFQ_ModuleId) {
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where requestforquotation.id = ? ");
            } else if (moduleid == Constants.Acc_Purchase_Requisition_ModuleId) { //ERP-19851
                result = getCustomDesignDAOObj().getSQLNativeQueryResult(billid, sqlquery + " where purchaserequisition.id = ? ");
            }

            list = result.getEntityList();
            List<String> invoiceCols = new ArrayList();

            DateFormat df = new SimpleDateFormat(jsonObj.optString(Constants.userdateformat));
            // DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(request);//User Date Formatter
            Matcher m = Pattern.compile("#(.+?)#").matcher(sqlquery);
            while (m.find()) {
                invoiceCols.add(m.group(1));
            }

            JSONArray jArr = new JSONArray(json);
            HashMap<String, Integer> default_headers = AccountingManager.getDefaultHeaderName_XtypeNew(getCustomDesignDAOObj(), jArr);
            for (int cnt = 0; cnt < list.size(); cnt++) {
                if (CustomDesignHandler.isArray(list.get(cnt))) {
                    rows = (Object[]) list.get(cnt);
                } else {
                    List objectArr = new ArrayList();
                    objectArr.add(list.get(cnt));
                    rows = objectArr.toArray();
                }
                for (int colCnt = 0; colCnt < rows.length; colCnt++) {
                    Object col = rows[colCnt];
                    if (default_headers.containsKey(invoiceCols.get(colCnt))) {
                        int xtype = default_headers.get(invoiceCols.get(colCnt));
                        switch (xtype) {
                            case 3:
                                try {
                                    java.sql.Timestamp sqlDate = (java.sql.Timestamp) col;
                                    if (sqlDate != null) {
                                        Date date = new Date(sqlDate.getTime());
                                        col = df.format(date);
                                    } else {
                                        col = "";
                                    }
                                } catch (ClassCastException e) { //To handle java.sql.Date cannot cast to java.sql.Timestamp exception.
                                    try {
                                        Date date = (Date) col;
                                        col = df.format(date);
                                    } catch (ClassCastException e1) {
                                        long colparse = Long.parseLong(col.toString());//for Created on Updated On
                                        Date date = new Date(colparse);
                                        col = df.format(date);
                                    }
                                }
                                break;
                            case 2:
                                col = authHandler.formattedAmount(Double.parseDouble(col.toString()), companyid);
                                break;
                            case 1:
                                col = col != null ? col.toString().replaceAll("(\r\n|\n\r|\r|\n)", "<br />") : "";
                                break;
                        }
                    }
                    emailsubjectbodyhtml = emailsubjectbodyhtml.replaceAll("#" + invoiceCols.get(colCnt) + "#", col != null ? col.toString() : "");
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AccMailNotifyServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return emailsubjectbodyhtml;
    }

    public String replaceGlobalLevelCustomFields(JSONObject jsonObj, AccountingHandlerDAO accountingHandlerDAOobj, accAccountDAO accAccountDAOobj, HashMap<String, Object> customParams) throws JSONException, SessionExpiredException, ServiceException {

        AccCustomData accCustomData = null;
        String billid = "", emailsubjectbodyhtml = "";
        int moduleid = 0;
        try {

            // String companyid = AccountingManager.getCompanyidFromRequest(request);
            String companyid = jsonObj.optString(Constants.companyKey);
            if (customParams.containsKey(Constants.moduleid)) {
                moduleid = (Integer) customParams.get(Constants.moduleid);
            }
            if (customParams.containsKey("billid")) {
                billid = (String) customParams.get("billid");
            }

            if (customParams.containsKey("replacehtml")) {
                emailsubjectbodyhtml = (String) customParams.get("replacehtml");
            }

            accCustomData = ExportRecordHandler.getCustomDataObject(moduleid, billid.trim(), accountingHandlerDAOobj);
            HashMap<String, Object> CustomRequestParams = new HashMap<String, Object>();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            //DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(request);//User Date Formatter
            DateFormat df = new SimpleDateFormat(jsonObj.optString(Constants.userdateformat));
            requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            requestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid));
            KwlReturnObject result = accAccountDAOobj.getFieldParams(requestParams);
            List lst = result.getEntityList();
            Iterator ite = lst.iterator();

            while (ite.hasNext()) {
                String field = "", data = "";
                FieldParams tmpcontyp = (FieldParams) ite.next();
                field = tmpcontyp.getFieldlabel();
                if (accCustomData != null) {
                    String coldata = accCustomData.getCol(tmpcontyp.getColnum());
                    if (!StringUtil.isNullOrEmpty(coldata)) {
                        int fieldType = tmpcontyp.getFieldtype();
                        switch (fieldType) {
                            case 3:
                                //String dateColData = accAccountDAOobj.getBrowserSpecificCustomDateLongValue(coldata, sessionHandlerImpl.getBrowserTZ(request));
                                String dateColData = accAccountDAOobj.getBrowserSpecificCustomDateLongValue(coldata, jsonObj.optString(Constants.browsertz));
                                long milliSeconds = Long.parseLong(dateColData);
                                data = df.format(milliSeconds);//User Date Formatter
                                break;
                            case 4:
                                CustomRequestParams.clear();
                                CustomRequestParams.put(Constants.filter_names, Arrays.asList(FieldConstants.Crm_id, FieldConstants.Crm_deleteflag));
                                CustomRequestParams.put(Constants.filter_values, Arrays.asList(coldata, 0));
                                KwlReturnObject customresult = accAccountDAOobj.getCustomCombodata(CustomRequestParams);
                                if (customresult != null) {
                                    List customDataList = customresult.getEntityList();
                                    Iterator cite = customDataList.iterator();
                                    while (cite.hasNext()) {
                                        Object[] row = (Object[]) cite.next();
                                        FieldComboData combodata = (FieldComboData) row[0];
                                        data = combodata.getValue();
                                    }
                                }
                                break;
                            case 7: //Multiple select drop down
                                String[] valueData = coldata.split(",");
                                for (String value : valueData) {
                                    CustomRequestParams.clear();
                                    CustomRequestParams.put(Constants.filter_names, Arrays.asList(FieldConstants.Crm_id, FieldConstants.Crm_deleteflag));
                                    CustomRequestParams.put(Constants.filter_values, Arrays.asList(value, 0));
                                    KwlReturnObject customresult1 = accAccountDAOobj.getCustomCombodata(CustomRequestParams);
                                    if (customresult1 != null) {
                                        List customDataList = customresult1.getEntityList();
                                        Iterator cite = customDataList.iterator();
                                        while (cite.hasNext()) {
                                            Object[] row = (Object[]) cite.next();
                                            FieldComboData combodata = (FieldComboData) row[0];
                                            data += combodata.getValue() + ",";
                                        }
                                    }
                                }
                                data = data.substring(0, data.length() - 1);
                                break;
                            case 12:
                                String fieldid = tmpcontyp.getId();
                                HashMap<String, Object> checkListRequestParams = new HashMap<String, Object>();
                                String Colsplit[] = coldata.split(",");
                                for (int i = 0; i < Colsplit.length; i++) {
                                    coldata = Colsplit[i];
                                    checkListRequestParams.put(Constants.filter_names, Arrays.asList(FieldConstants.Crm_id, FieldConstants.Crm_deleteflag));
                                    checkListRequestParams.put(Constants.filter_values, Arrays.asList(coldata, 0));
                                    ArrayList order_by = new ArrayList();
                                    ArrayList order_type = new ArrayList();
                                    order_by.add("itemsequence");
                                    order_type.add("asc");
                                    checkListRequestParams.put("order_by", order_by);
                                    checkListRequestParams.put("order_type", order_type);
                                    KwlReturnObject checkListresult = accAccountDAOobj.getCustomCombodata(checkListRequestParams);
                                    List checklst = checkListresult.getEntityList();
                                    Iterator checkite = checklst.iterator();
                                    while (checkite.hasNext()) {
                                        Object[] row = (Object[]) checkite.next();
                                        FieldComboData checkfield = (FieldComboData) row[0];
                                        data += checkfield.getValue() + ",";
                                    }
                                }
                                data = data.substring(0, data.length() - 1);
                                break;
                            case 13:
                                data = coldata.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
                                break;
                            default:
                                data = coldata;
                                break;
                        }
                    } else {
                        data = "";
                    }
                }
                emailsubjectbodyhtml = emailsubjectbodyhtml.replaceAll("#" + field + "#", data);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccMailNotifyServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return emailsubjectbodyhtml;
    }

    public JSONObject getSelectFieldPlaceholderswithCategories(JSONObject jsonObj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        try {
            //String companyid = AccountingManager.getCompanyidFromRequest(request);
            String companyid = jsonObj.optString(Constants.companyKey);
            int moduleid = Integer.parseInt(jsonObj.optString(Constants.moduleid));
            JSONObject jresult = new JSONObject();
            jresult = AccountingManager.fetchDefaultHeaderFieldsWithCategories(getCustomDesignDAOObj(), jresult, moduleid, companyid);
            jresult = AccountingManager.fetchCustomFieldsWithModule(getCustomDesignDAOObj(), jresult, moduleid, companyid);//fetching customfields for original moduleid
            TreeMap<String, String> baseModuletoOtherMap = null;
            switch (moduleid) {
                case Constants.Acc_Purchase_Order_ModuleId: // Purchase Order
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignPurchaseOrdertoOtherMap;
                    break;
                case Constants.Acc_Sales_Order_ModuleId: // Sales Order
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignSalesOrdertoOtherMap;
                    break;
                case Constants.Acc_Make_Payment_ModuleId: // Make Payment
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignPaymenttoOtherMap;
                    break;
                case Constants.Acc_Receive_Payment_ModuleId: // Receipt Payment
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignReceipttoOtherMap;
                    break;
                case Constants.Acc_Invoice_ModuleId: // Sales Invoice
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignInvoicetoOtherMap;
                    break;
                case Constants.Acc_Vendor_Invoice_ModuleId: // Purchase Invoice
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignVendorInvoicetoOtherMap;
                    break;
                case Constants.Acc_Delivery_Order_ModuleId: // Delivery order
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignVendorInvoicetoOtherMap;
                    break;
                case Constants.Acc_Goods_Receipt_ModuleId: // Goods Receipt Order
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignVendorInvoicetoOtherMap;
                    break;
                case Constants.Acc_Sales_Return_ModuleId: // Sales Return
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignVendorInvoicetoOtherMap;
                    break;
                case Constants.Acc_Purchase_Return_ModuleId: // Purchase Return
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignVendorInvoicetoOtherMap;
                    break;
                case Constants.Acc_Customer_Quotation_ModuleId: // Purchase Return
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignVendorInvoicetoOtherMap;
                    break;
                case Constants.Acc_Vendor_Quotation_ModuleId: // Purchase Return
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignVendorInvoicetoOtherMap;
                    break;
            }

            if (baseModuletoOtherMap != null) {
                TreeMap<String, String> moduletoOtherMap = (TreeMap<String, String>) baseModuletoOtherMap.clone();
                if (moduletoOtherMap != null) {
                    for (Map.Entry<String, String> mapModule : moduletoOtherMap.entrySet()) {
                        int mapModuleId = StringUtil.getInteger(mapModule.getValue());
                        jresult = AccountingManager.fetchCurrentUserandCompanyDetails(getCustomDesignDAOObj(), jresult, mapModuleId, false, companyid);
                        jresult = AccountingManager.fetchCustomFieldsWithModule(getCustomDesignDAOObj(), jresult, mapModuleId, companyid);
                    }
                }
            }
            if (moduleid == Constants.Acc_Purchase_Order_ModuleId || moduleid == Constants.Acc_Sales_Order_ModuleId || moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Vendor_Invoice_ModuleId
                    || moduleid == Constants.Acc_Delivery_Order_ModuleId || moduleid == Constants.Acc_Goods_Receipt_ModuleId
                    || moduleid == Constants.Acc_Sales_Return_ModuleId || moduleid == Constants.Acc_Purchase_Return_ModuleId
                    || moduleid == Constants.Acc_Customer_Quotation_ModuleId || moduleid == Constants.Acc_Vendor_Quotation_ModuleId ||  moduleid == Constants.Acc_Purchase_Requisition_ModuleId ) {
                jresult = AccountingManager.fetchCurrentUserandCompanyDetails(getCustomDesignDAOObj(), jresult, moduleid, true, companyid);
                JSONObject jSONObjectapprover = new JSONObject();
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("fieldid", "NA");
                jSONObject.put("id", "NA");
                jSONObject.put("label", "----------------[Approver levels]------------");
                jSONObject.put("xtype", "");
                jSONObjectapprover.append("data", jSONObject);

                for (int level = 1; level <= 10; level++) {
                    JSONObject staticamountInfo = new JSONObject();
                    staticamountInfo.put("fieldid", Constants.ApproverLevel + level);
                    staticamountInfo.put("label", Constants.ApproverLevel + level);
                    staticamountInfo.put("xtype", 1);
                    staticamountInfo.put("id", Constants.ApproverLevel + level);
                    jSONObjectapprover.append("data", staticamountInfo);
                }
                jresult.append("data", jSONObjectapprover);
            }
            if (moduleid == Constants.Acc_Make_Payment_ModuleId || moduleid == Constants.Acc_Receive_Payment_ModuleId) {
                jresult = AccountingManager.fetchCurrentUserandCompanyDetails(getCustomDesignDAOObj(), jresult, moduleid, true, companyid);
            }
            //For total Amount,Subtotal,Total Tax,Total Discount,Amount in words entry in Default Header
            HashMap<String, String> amountcols = null;
            List<JSONObject> amountcolList = new ArrayList();
            if (moduleid == Constants.Acc_Purchase_Order_ModuleId || moduleid == Constants.Acc_Sales_Order_ModuleId || moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Vendor_Invoice_ModuleId
                    || moduleid == Constants.Acc_Delivery_Order_ModuleId || moduleid == Constants.Acc_Goods_Receipt_ModuleId || moduleid == Constants.Acc_Sales_Return_ModuleId || moduleid == Constants.Acc_Purchase_Return_ModuleId
                    || moduleid == Constants.Acc_Customer_Quotation_ModuleId || moduleid == Constants.Acc_Vendor_Quotation_ModuleId) {
                amountcols = LineItemColumnModuleMapping.InvoiceProductSummaryItems;
            } else if (moduleid == Constants.Acc_Make_Payment_ModuleId || moduleid == Constants.Acc_Receive_Payment_ModuleId) {
                amountcols = LineItemColumnModuleMapping.PaymentProductSummaryItems;
            }
            JSONObject jSONObjectamountsection = new JSONObject();
            if (amountcols != null && amountcols.size() > 0) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("fieldid", "NA");
                jSONObject.put("id", "NA");
                jSONObject.put("label", "----------------[Amount Section]------------");
                jSONObject.put("xtype", "");
                jSONObjectamountsection.append("data", jSONObject);
            }

            if (amountcols != null) {
                for (Map.Entry<String, String> amountentry : amountcols.entrySet()) {
                    JSONObject staticamountInfo = new JSONObject(amountentry.getValue());
                    staticamountInfo.put("id", amountentry.getKey());
                    staticamountInfo.put("fieldid", amountentry.getKey());
                    staticamountInfo.put("label", staticamountInfo.get("label"));
                    staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                    amountcolList.add(staticamountInfo);
                    jSONObjectamountsection.append("data", staticamountInfo);
                }
                jresult.append("data", jSONObjectamountsection);
            }

            //Email extra fields
            JSONObject jsonObjectEmailSection = new JSONObject();
            JSONObject titleJobj = new JSONObject();
            titleJobj.put("fieldid", "NA");
            titleJobj.put("id", "NA");
            titleJobj.put("label", "----------------[Email Section]------------");
            titleJobj.put("xtype", "");
            jsonObjectEmailSection.append("data", titleJobj);

            HashMap<String, String> emailNotificationExtraFieldsMap = CustomDesignerConstants.CustomDesign_Email_Notification_ExtraFieldsMap;
            if (emailNotificationExtraFieldsMap != null) {
                for (Map.Entry<String, String> extraField : emailNotificationExtraFieldsMap.entrySet()) {
                    JSONObject staticamountInfo = new JSONObject(extraField.getValue());
                    staticamountInfo.put("fieldid", extraField.getKey());
                    staticamountInfo.put("id", extraField.getKey());
                    staticamountInfo.put("label", staticamountInfo.get("label"));
                    staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                    jsonObjectEmailSection.append("data", staticamountInfo);
                }
                jresult.append("data", jsonObjectEmailSection);
            }
            jobj.put("defaultfield", jresult);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(AccMailNotifyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccMailNotifyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public String replaceCustomFieldsforOtherModule(JSONObject jsonObj, HashMap<String, Object> customParams) throws JSONException, ServiceException {
        String emailsubjectbodyhtml = "", billid = "", vendororcustomerid = "";
        int moduleid = 0;
        TreeMap<String, String> baseModuletoOtherMap = null;
        HashMap<String, Object> replaceparams = (HashMap<String, Object>) customParams.clone();
        try {
            if (replaceparams.containsKey(Constants.moduleid)) {
                moduleid = (Integer) replaceparams.get(Constants.moduleid);
            }
            if (replaceparams.containsKey("replacehtml")) {
                emailsubjectbodyhtml = (String) replaceparams.get("replacehtml");
            }
            if (replaceparams.containsKey("billid")) {
                billid = (String) replaceparams.get("billid");
            }
            switch (moduleid) {
                case Constants.Acc_Purchase_Order_ModuleId: // Purchase Order
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignPurchaseOrdertoOtherMap;
                    break;
                case Constants.Acc_Sales_Order_ModuleId: // Sales Order
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignSalesOrdertoOtherMap;
                    break;
            }

            if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                KwlReturnObject objItr = getAccountingHandlerDAOobj().getObject(PurchaseOrder.class.getName(), billid);
                PurchaseOrder purchaseOrder = (PurchaseOrder) objItr.getEntityList().get(0);
                vendororcustomerid = purchaseOrder.getVendor() != null ? purchaseOrder.getVendor().getID() : "";
            } else if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
                KwlReturnObject objItr = getAccountingHandlerDAOobj().getObject(SalesOrder.class.getName(), billid);
                SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
                vendororcustomerid = salesOrder.getCustomer() != null ? salesOrder.getCustomer().getID() : "";
            }
            replaceparams.put("billid", vendororcustomerid);

            if (baseModuletoOtherMap != null) {
                TreeMap<String, String> moduletoOtherMap = (TreeMap<String, String>) baseModuletoOtherMap.clone();
                if (moduletoOtherMap != null) {
                    for (Map.Entry<String, String> mapModule : moduletoOtherMap.entrySet()) {
                        int mapModuleId = StringUtil.getInteger(mapModule.getValue());
                        replaceparams.put(Constants.moduleid, mapModuleId);
                        emailsubjectbodyhtml = replaceGlobalLevelCustomFields(jsonObj, getAccountingHandlerDAOobj(), getAccAccountDAOobj(), replaceparams);
                        replaceparams.put("replacehtml", emailsubjectbodyhtml);
                    }
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AccMailNotifyServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return emailsubjectbodyhtml;
    }

    public JSONObject sendMailNotification(JSONObject jsonObj) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {

        java.io.OutputStream os = null;
        JSONObject jobj = new JSONObject();
        List<String> list = new ArrayList<String>();
        List<Object> jasperList = new ArrayList<Object>();
        {
            String fileName = "";
            ByteArrayOutputStream baos = null;
            ByteArrayInputStream bais = null;
            JasperPrint jasperPrint = null;
            byte[] pdfByteArray = null;
            FileInputStream fis = null;
            FileOutputStream fos = null;
            boolean issuccess = false;
            boolean iscontraentryflag = false;
            boolean otherwiseFlag = false;
            boolean advanceFlag = false;
            double advanceAmount = 0;
            HashMap<String, Object> requestmap = null;
            try {
                requestmap = new HashMap<String, Object>();
                File tempDir = null;
                String entryno = jsonObj.optString("entryno");
                Map<String, Object> requestParams = new HashMap<String, Object>();
                Iterator<String> keys = jsonObj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = jsonObj.getString(key);
                    requestParams.put(key, value);
                }
                if(!StringUtil.isNullOrEmpty(jsonObj.optString("emailid",""))) {
                    String[] emails = jsonObj.optString("emailid").split(";");
                    String personid = jsonObj.optString("personid");
                    String plainMsg = jsonObj.optString("message");

                    String subject = jsonObj.optString("emailSubject");
                    boolean sendPdf = Boolean.parseBoolean((String) jsonObj.optString("sendpdf"));
                    int mode = Integer.parseInt(jsonObj.optString("mode"));
                    int moduleid = StringUtil.isNullOrEmpty(jsonObj.optString("moduleid")) ? 0 : Integer.parseInt(jsonObj.optString("moduleid"));
                    String billid = jsonObj.optString("billid");
                    String attachmentSelection = jsonObj.optString("attachmentSelection");
                    //String username = sessionHandlerImpl.getUserName(request);
                    String username = jsonObj.optString(Constants.username);

                    //String baseUrl = URLUtil.getPageURL(request, loginpageFull);
                    String baseUrl = URLUtil.getPageURLJson(requestParams, loginpageFull);
                    if (!StringUtil.isNullOrEmpty(jsonObj.optString("advanceAmount"))) {
                        advanceAmount = Double.parseDouble(jsonObj.optString("advanceAmount"));
                    }

                    if (!StringUtil.isNullOrEmpty(jsonObj.optString("otherwise"))) {
                        otherwiseFlag = Boolean.parseBoolean(jsonObj.optString("otherwise"));
                    }

                    iscontraentryflag = Boolean.parseBoolean(jsonObj.optString("contraentryflag"));
                    //Locale loc = RequestContextUtils.getLocale(request);
                    Locale loc = StringUtil.getLocale(jsonObj.optString(Constants.language));
                    requestmap = new HashMap<String, Object>();
                    requestmap.put("loc", loc);
                    requestmap.put("locale", loc);
                    requestmap.put("baseUrl", baseUrl);
                    requestmap.put("otherwiseFlag", otherwiseFlag);
                    requestmap.put("advanceAmount", advanceAmount);
                    requestmap.put("iscontraentryflag", iscontraentryflag);
                    requestmap.put("username", username);
                    requestmap.put(Constants.companyKey, jsonObj.optString(Constants.companyKey));
                    requestmap.put(Constants.globalCurrencyKey, jsonObj.optString(Constants.globalCurrencyKey));
                    //requestmap.put(Constants.df, authHandler.getDateFormatter(request));
                    requestmap.put(Constants.df, jsonObj.optString(Constants.userdateformat));
                    requestmap.put("emails", (String[]) emails);
                    requestmap.put("personid", personid.toString());
                    requestmap.put("plainMsg", plainMsg.toString());
                    requestmap.put("subject", subject.toString());
                    requestmap.put("sendPdf", (boolean) sendPdf);
                    requestmap.put("mode", (int) mode);
                    requestmap.put("billid", billid.toString());
                    requestmap.put("attachmentSelection", attachmentSelection.toString());
                    String[] attachmentSelectionArray = attachmentSelection.split(",");
                    CompanyAccountPreferences preferences = (CompanyAccountPreferences) getKwlCommonTablesDAOObj().getClassObject(CompanyAccountPreferences.class.getName(), jsonObj.optString(Constants.companyKey));
                    ExtraCompanyPreferences extrapreferences = (ExtraCompanyPreferences) getKwlCommonTablesDAOObj().getClassObject(ExtraCompanyPreferences.class.getName(), jsonObj.optString(Constants.companyKey));
                    Company company = preferences.getCompany();
                    KWLCurrency currency = (KWLCurrency) getKwlCommonTablesDAOObj().getClassObject(KWLCurrency.class.getName(), jsonObj.optString(Constants.globalCurrencyKey));
                    // double amount = 0;
                    Date invDate = new Date();
                    String fromID = authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                    File destDir = new File("");
                    JSONArray attachments = new JSONArray();
                    if (!StringUtil.isNullOrEmpty(jsonObj.optString("attachments"))) {
                        attachments = new JSONArray(jsonObj.optString("attachments"));
                    }
                    String[] path = new String[]{};
                    String[] Names = new String[]{};
                    String userId = jsonObj.optString(Constants.userid);
                    User user = (User) kwlCommonTablesDAOObj.getClassObject(User.class.getName(), userId);
                    if (extrapreferences!=null && extrapreferences.getDefaultmailsenderFlag() == Constants.UserMail) {
                          if (user != null) {
                            fromID = user.getEmailID();
                        }
                    }
                    if (sendPdf) {
                        double amount = 0.0;
                        if (!StringUtil.isNullOrEmpty(jsonObj.optString("amount"))) {
                            amount = Double.parseDouble((String) jsonObj.optString("amount"));
                        }
                        DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(jsonObj.optString(Constants.dateformatid), jsonObj.optString(Constants.timezonedifference), true);
                        String logoPath = ProfileImageServlet.getProfileImagePath(jsonObj, true, null);
                        String currencyid = jsonObj.optString("currencyid") == null ? currency.getCurrencyID() : jsonObj.optString("currencyid");
                        String dateStr = "";
                        try {
                            DateFormat df = authHandler.getDateOnlyFormat();
                            // DateFormat df = authHandler.getDateFormatter(request);

                            dateStr = df.format(invDate);
                        } catch (Exception ex) {
                        }
                        String accname = null;
                        if (!StringUtil.isNullOrEmpty(jsonObj.optString("accname"))) {
                            accname = jsonObj.optString("accname");
                        }
                        String companyId = jsonObj.optString(Constants.companyKey);
                        boolean isexpenseinv = false;
                        String cust = "", address = "";
                        if (!StringUtil.isNullOrEmpty(jsonObj.optString("isexpenseinv"))) {
                            isexpenseinv = Boolean.parseBoolean((String) jsonObj.optString("isexpenseinv"));
                        }
                        if (!StringUtil.isNullOrEmpty(jsonObj.optString("customer"))) {
                            cust = jsonObj.optString("customer");
                        }
                        if (!StringUtil.isNullOrEmpty(jsonObj.optString("address"))) {
                            address = jsonObj.optString("address");
                        }
                        if (storageHandlerImpl.GetVRnetCompanyId().contains(companyId)
                                && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_QUOTATION
                                || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_DELIVERYORDER
                                || mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE || mode == StaticValues.AUTONUM_PURCHASEORDER)) {
                            baos = exportrecordVRnetObj.createVRNetPdf(jsonObj, currencyid, billid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv, companyId, userId, jsonObj.optString(Constants.globalCurrencyKey));
                        }
                        if (mode == 8 || mode == 4) {   //For Receive Payment & Make Payment Email
                            jasperList = accExportOtherReportsServiceDAOobj.exportDefaultPaymentVoucher(jsonObj);
                            jasperPrint = (JasperPrint) jasperList.get(0);
                            pdfByteArray = JasperExportManager.exportReportToPdf(jasperPrint);
                            bais = new ByteArrayInputStream(pdfByteArray);
                            fileName = "Transaction" + dateStr + ".pdf";
                        } else if (mode == StaticValues.AUTONUM_RFQ) {   //RFQ Template Jasper
                            jasperList = accExportOtherReportsServiceDAOobj.exportDefaultRFQ(jsonObj);
                            jasperPrint = (JasperPrint) jasperList.get(0);
                            pdfByteArray = JasperExportManager.exportReportToPdf(jasperPrint);
                            bais = new ByteArrayInputStream(pdfByteArray);
                            fileName = "Transaction" + dateStr + ".pdf";
                        } else {
                            baos = createPDFObj.createPdf(requestmap, currencyid, billid, formatter, mode, amount, logoPath, null, accname, null, isexpenseinv, companyId, userId, jsonObj.optString(Constants.globalCurrencyKey));
                        }
                        if (mode != 8 && mode != 4 && mode != StaticValues.AUTONUM_RFQ) { //Temporary Check : When Transaction is of Non-Receive Payment & Non-Make Payment
                            destDir = new File(storageHandlerImpl.GetProfileImgStorePath(), "Transaction" + dateStr + ".pdf");
                            FileOutputStream oss = new FileOutputStream(destDir);
                            baos.writeTo(oss);
                            list.add(destDir.getAbsolutePath());
                            oss.close();
                            path = list.toArray(new String[attachments.length() + 1]);
                            Names = new String[attachments.length() + 1];
                            Names[0] = "Transaction" + dateStr + ".pdf";
                            for (int i = 0; i < attachments.length(); i++) {
                                path[i + 1] = StorageHandler.GetDocStorePath() + attachments.getJSONObject(i).get("id").toString();
                                Names[i + 1] = attachments.getJSONObject(i).get("name").toString();
                            }
//                        path = new String[]{destDir.getAbsolutePath()};
//                        baos.close();
                        }
                    } else if (mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION || mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_GOODSRECEIPT || mode == StaticValues.AUTONUM_DELIVERYORDER || mode == StaticValues.AUTONUM_GOODSRECEIPTORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_SALESRETURN || mode == StaticValues.AUTONUM_PURCHASERETURN) {
                        path = new String[attachments.length()];
                        Names = new String[attachments.length()];
                        for (int i = 0; i < attachments.length(); i++) {
                            path[i] = StorageHandler.GetDocStorePath() + attachments.getJSONObject(i).get("id").toString();
                            Names[i] = attachments.getJSONObject(i).get("name").toString();
                        }
                    }
                    if (attachmentSelectionArray.length > 0) {
                        /*
                         * mode =2 ->Customer Invoice Without Inventory Mode
                         * mode =11 ->Customer Invoice With Inventory Mode mode
                         * =15 ->Vendor Invoice With Inventory Mode mode =6
                         * ->Vendor Invoice Without Inventory Mode
                         */
                        if (mode == 2 || mode == 11 || mode == 15 || mode == 6) {  //only for customer invoice (Without Inventory) 
                            HashMap<String, Object> hashMap = new HashMap<String, Object>();
                            hashMap.put("invoiceID", billid);
                            hashMap.put("companyid", company.getCompanyID());
                            KwlReturnObject object = accInvoiceDAOobj.getinvoiceDocuments(hashMap);

                            tempDir = new File(storageHandlerImpl.GetDocStorePath() + "Temp");
                            if (!tempDir.exists()) {
                                tempDir.mkdir();
                            }

                            Iterator iterator = object.getEntityList().iterator();
                            while (iterator.hasNext()) {
                                Object[] obj = (Object[]) iterator.next();
                                String storeID = (String) obj[2];
                                for (int selectCount = 0; selectCount < attachmentSelectionArray.length; selectCount++) {
                                    if (storeID.equalsIgnoreCase(attachmentSelectionArray[selectCount])) {
                                        String docName = (String) obj[0];
                                        String Ext = docName.substring(docName.lastIndexOf("."));

                                        try {
                                            File fp = new File(StorageHandler.GetDocStorePath() + storeID + Ext);
                                            File op = new File(storageHandlerImpl.GetDocStorePath() + "Temp" + "/", docName);
                                            if (fp.exists()) {
                                                byte buff[] = new byte[(int) fp.length()];
                                                fis = new FileInputStream(fp);
                                                int read = fis.read(buff);
                                                if (!op.exists()) {
                                                    op.createNewFile();
                                                }
                                                fos = new FileOutputStream(op);
                                                fos.write(buff);
                                                fos.flush();
                                                list.add(storageHandlerImpl.GetDocStorePath() + "Temp" + "/" + docName);
                                            }
                                        } catch (FileNotFoundException ex) {
                                            Logger.getLogger(AccMailNotifyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    try {
                        
                        if (emails.length > 0) {
                            Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                            // get notification rule for email
                            KwlReturnObject notificationRuleResult = getAccountingHandlerDAOobj().getEmailTemplateTosendApprovalMail(company.getCompanyID(), Constants.Email_Button_From_Report_fieldid, moduleid);
                            String hyperlinkText = "";
                            String ccemails="";
                            if (notificationRuleResult.getEntityList().size() > 0) {
                                NotificationRules notificationRule = (NotificationRules) notificationRuleResult.getEntityList().get(0);
                                hyperlinkText = notificationRule.getHyperlinkText();
                                ccemails=notificationRule.getEmailids();
                            }
                            // get hyperlink field from constants
                            JSONObject jObj = new JSONObject(CustomDesignerConstants.CustomDesign_Email_Notification_ExtraFieldsMap.get(CustomDesignerConstants.TEMPLATE_HYPERLINK_IN_EMAIL));
                            String hyperlinkFieldLabel = jObj.getString("label");

                            if (mode == 8 || mode == 4 || mode == StaticValues.AUTONUM_RFQ) { //For Receive Payment & Make Payment Email
                                String mainHtml = "";
                                String templateid = StringUtil.isNullOrEmpty(jsonObj.optString("templateid")) ? "" : jsonObj.optString("templateid");
                                String templateName = StringUtil.isNullOrEmpty(jsonObj.optString("templateName")) ? "" : jsonObj.optString("templateName");
                                if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                                    //JSONObject requestParams = jsonObj;

                                    if (extrapreferences.isActivateDDTemplateFlow()) {
                                        mainHtml = accDocumentDesignService.getHTMLContentForEmailWithDDTemplate(jsonObj);
                                    }

                                    if (extrapreferences.isActivateDDInsertTemplateLink()) {
                                        StringBuilder appendString = new StringBuilder();
                                        jsonObj.put(Constants.isdefaultHeaderMap, true);
                                        String resturl = accDocumentDesignService.PrintTemplateRestUrl(jsonObj);
                                        if (!StringUtil.isNullOrEmpty(hyperlinkText)) {
                                            templateName = hyperlinkText;
                                        }
                                        String templateurl = "<a href='" + resturl + "' target='_blank'>" + templateName + "</a>";

                                        if (plainMsg.contains(hyperlinkFieldLabel)) {
                                            plainMsg = plainMsg.replaceAll("#" + hyperlinkFieldLabel + "#", templateurl);
                                        } else {
                                            appendString.append(mainHtml);
                                            appendString.append("<br/><b>Please click on link below to preview template: </b> <br/>" + templateurl);
                                            mainHtml = appendString.toString();
                                        }
                                    }
                                }
                                // Replace placeholder with empty text
                                plainMsg = plainMsg.replaceAll("#" + hyperlinkFieldLabel + "#", "");
                                SendMailHandler.attachPDFToMail(fileName, emails, subject, plainMsg + mainHtml, plainMsg + mainHtml, fromID, bais, mode, smtpConfigMap, "");
                                issuccess = true;
                            } else {
                                if (mode == StaticValues.AUTONUM_PURCHASEREQUISITION) {
                                    SendMailHandler.postMail("", emails, new String[0], subject, plainMsg, plainMsg, fromID, path, smtpConfigMap);
                                    issuccess = true;

                                } else if (mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION || mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_GOODSRECEIPT || mode == StaticValues.AUTONUM_DELIVERYORDER || mode == StaticValues.AUTONUM_GOODSRECEIPTORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_SALESRETURN || mode == StaticValues.AUTONUM_PURCHASERETURN) {
                                    String mainHtml = "";
                                    String templateid = StringUtil.isNullOrEmpty(jsonObj.optString("templateid")) ? "" : jsonObj.optString("templateid");
                                    String templateName = StringUtil.isNullOrEmpty(jsonObj.optString("templateName")) ? "" : jsonObj.optString("templateName");
                                    if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                                        //JSONObject requestParams = jsonObj;

                                        if (extrapreferences.isActivateDDTemplateFlow()) {
                                            mainHtml = accDocumentDesignService.getHTMLContentForEmailWithDDTemplate(jsonObj);
                                        }

                                        if (extrapreferences.isActivateDDInsertTemplateLink()) {
                                            StringBuilder appendString = new StringBuilder();
                                            jsonObj.put(Constants.isdefaultHeaderMap, true);
                                            String resturl = accDocumentDesignService.PrintTemplateRestUrl(jsonObj);
                                            if (!StringUtil.isNullOrEmpty(hyperlinkText)) {
                                                templateName = hyperlinkText;
                                            }
                                            String templateurl = "<a href='" + resturl + "' target='_blank'>" + templateName + "</a>";

                                            if (plainMsg.contains(hyperlinkFieldLabel)) {
                                                plainMsg = plainMsg.replaceAll("#" + hyperlinkFieldLabel + "#", templateurl);
                                            } else {
                                                appendString.append(mainHtml);
                                                appendString.append("<br/><b>Please click on link below to preview template: </b> <br/>" + templateurl);
                                                mainHtml = appendString.toString();
                                            }
                                        }
                                    }
                                    // Replace placeholder with empty text
                                    plainMsg = plainMsg.replaceAll("#" + hyperlinkFieldLabel + "#", "");
//                                    SendMailHandler.postMail(emails, subject, plainMsg + mainHtml, plainMsg + mainHtml, fromID, path, Names, smtpConfigMap);
                                    SendMailHandler.postMail(ccemails,new String[0],emails, subject, plainMsg+mainHtml, plainMsg+mainHtml, fromID, path,smtpConfigMap);
                                    issuccess = true;
                                } else {
                                    String mainHtml = "";
                                    String templateid = StringUtil.isNullOrEmpty(jsonObj.optString("templateid")) ? "" : jsonObj.optString("templateid");
                                    String templateName = StringUtil.isNullOrEmpty(jsonObj.optString("templateName")) ? "" : jsonObj.optString("templateName");
                                    if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                                        //  JSONObject requestParams = jsonObj;
                                        if (extrapreferences.isActivateDDTemplateFlow()) {
                                            mainHtml = accDocumentDesignService.getHTMLContentForEmailWithDDTemplate(jsonObj);
                                        }
                                        if (extrapreferences.isActivateDDInsertTemplateLink()) {
                                            StringBuilder appendString = new StringBuilder();
                                            jsonObj.put(Constants.isdefaultHeaderMap, true);
                                            String resturl = accDocumentDesignService.PrintTemplateRestUrl(jsonObj);
                                            if (!StringUtil.isNullOrEmpty(hyperlinkText)) {
                                                templateName = hyperlinkText;
                                            }
                                            String templateurl = "<a href='" + resturl + "' target='_blank'>" + templateName + "</a>";

                                            if (plainMsg.contains(hyperlinkFieldLabel)) {
                                                plainMsg = plainMsg.replaceAll("#" + hyperlinkFieldLabel + "#", templateurl);
                                            } else {
                                                appendString.append(mainHtml);
                                                appendString.append("<b>Please click on link below to preview template: </b> <br/>" + templateurl);
                                                mainHtml = appendString.toString();
                                            }
                                        }

                                    }
                                    // Replace placeholder with empty text
                                    plainMsg = plainMsg.replaceAll("#" + hyperlinkFieldLabel + "#", "");
                                    SendMailHandler.postMail(emails, subject, plainMsg + mainHtml, plainMsg + mainHtml, fromID, path, smtpConfigMap);
                                    issuccess = true;
                                }

                            }
                            auditTrailObj.insertAuditLog(AuditAction.SENT_EMAIL, "User " + jsonObj.optString(Constants.userfullname) + " has sent Email of " + AccountingManager.getModuleName(moduleid)+" "+ entryno, requestParams, "12");
                        } else {
                            auditTrailObj.insertAuditLog(AuditAction.SENT_EMAIL, "Failed to send email for Invoice - " + entryno + " as receiver emailid is not found", requestParams, "12");
                        }
                    } catch (MessagingException e) {
                        try {
                            throw new MessageSizeExceedingException(e.getMessage());
                        } catch (MessageSizeExceedingException exception) {
                            if (StringUtil.isNullOrEmpty(exception.toString())) {
                                Logger.getLogger(AccMailNotifyServiceImpl.class.getName()).log(Level.SEVERE, null, e);
                            } else {
                                issuccess = false;
                                jobj.put("success", issuccess);
                                jobj.put("isMsgSizeException", true);
                                jobj.put("msg", exception.toString());
                            }
                        }
                    }


                    // Below Function is called to update sent Email flag 
                    if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Vendor_Invoice_ModuleId
                            || moduleid == Constants.Acc_Purchase_Order_ModuleId || moduleid == Constants.Acc_Sales_Order_ModuleId
                            || moduleid == Constants.Acc_Make_Payment_ModuleId || moduleid == Constants.Acc_Receive_Payment_ModuleId
                            || moduleid == Constants.Acc_Customer_Quotation_ModuleId) {
                        accCommonTablesDAO.updateSentEmailFlag(moduleid, billid, jsonObj.optString(Constants.companyKey));
                    } 
                } else {
                    auditTrailObj.insertAuditLog(AuditAction.SENT_EMAIL, "Failed to send email for Invoice - " + entryno + " as receiver emailid is not found", requestParams, "12");
                }

            } catch (SessionExpiredException ex) {
                Logger.getLogger(AccMailNotifyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception e) {
                issuccess = false;
            } finally {
                try {
                    if (baos != null) {
                        baos.close();
                    }
                    if (bais != null) {
                        bais.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                    if (fis != null) {
                        fis.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(AccMailNotifyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (jobj.length() == 0) {
                    jobj.put("success", issuccess);
                    jobj.put("msg", getMessageSource().getMessage("acc.rem.165", null, StringUtil.getLocale(Constants.language)));
                }
            }
        }
        return jobj;
    }
    
    @Transactional
    public void sendInvoicesonMail(JSONObject requestParam) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        JSONObject jsonObj = new JSONObject();
        String path = requestParam.optString("path");
        String servPath = requestParam.optString("servPath");
        KwlReturnObject result = null;
        try {
            String companyId = requestParam.optString(Constants.companyKey);
            String useidId = requestParam.optString(Constants.useridKey);
            KwlReturnObject user = getAccountingHandlerDAOobj().getObject("com.krawler.common.admin.User", useidId);
            User userobj = (User) user.getEntityList().get(0);
            KwlReturnObject kwl = getAccountingHandlerDAOobj().getObject("com.krawler.common.admin.Company", companyId);
            Company company = (Company) kwl.getEntityList().get(0);
            String toEmailId[] = requestParam.optString("to").split(";");
            List<String> toEmailList = Arrays.asList(toEmailId);
            String fieldid = requestParam.optString("fieldid", "");
            String mode = requestParam.optString("mode");
            int moduleid = Integer.parseInt(requestParam.optString("moduleid"));
            String templateid =requestParam.optString("templateid");
            result = getAccMailNotificationDAOObj().getEmailTemplateToEdit(companyId, moduleid, fieldid);
            String[] billIDs = requestParam.optString("billids", "").split(",");
            
            HashMap<String,Object> emailDataMap=new HashMap<>();
            String invoicesWithEmailIDs="";
            /*
             Created map with billid and email(set at the time of email sent) which is sent by JS side.
            */
            if (requestParam.has("billidswithemail")) {
                invoicesWithEmailIDs = requestParam.optString("billidswithemail");
                if (!StringUtil.isNullOrEmpty(invoicesWithEmailIDs)) {
                    JSONArray invoiceWithEmailArr = new JSONArray(invoicesWithEmailIDs);
                    for (int i = 0; i < invoiceWithEmailArr.length(); i++) {
                        JSONObject jSONObject = new JSONObject(invoiceWithEmailArr.get(i).toString());
                        if (jSONObject.has("email")) {
                            emailDataMap.put(jSONObject.optString("id"), jSONObject.optString("email"));
                        }
                    }
                }
            }

            if (billIDs.length > 0) {
                for (int index = 0; index < billIDs.length; index++) {
                    List<String> emailList = new ArrayList<String>();
                    String billid = billIDs[index];
                    requestParam.put("billid", billid);
                    boolean sendCopyChecked = requestParam.optBoolean("sendCopyChecked", false);
                    if (sendCopyChecked) {
                        String emailid = !StringUtil.isNullOrEmpty(userobj.getEmailID()) ? userobj.getEmailID() : (!StringUtil.isNullOrEmpty(company.getEmailID()) ? company.getEmailID() : "");
                        emailList.add(emailid);
                    }
                    jsonObj = replacePlaceHolders(requestParam, company, result, moduleid);
                    jsonObj.put("mode", mode);
                    jsonObj.put("billid", billid);
                    jsonObj.put("bills", billid);
                    jsonObj.put("moduleid", moduleid);
                    jsonObj.put("fieldid", fieldid);
                    jsonObj.put("path", path);
                    jsonObj.put("servPath", servPath);
                    jsonObj.put("templateid", templateid);
                    
                    boolean isMailToShippingEmail = jsonObj.optBoolean("isMailToShippingEmail", false);
                    if(billIDs.length >1){ //on selecting single invoice mail shoold not go to Billing or shipping address Ref SDP-11949
                    if (isMailToShippingEmail && !StringUtil.isNullOrEmpty(jsonObj.optString(Constants.SHIPPING_EMAIL, ""))) {
                        String[] shippingEmaild = jsonObj.optString(Constants.SHIPPING_EMAIL, "").replace("/,/g", ";").split(";");
                        List<String> shippingEmailList = Arrays.asList(shippingEmaild);
                        emailList.addAll(shippingEmailList);
                    } else if (!StringUtil.isNullOrEmpty(jsonObj.optString(Constants.BILLING_EMAIL, ""))) {
                        String[] billingEmaild = jsonObj.optString(Constants.BILLING_EMAIL, "").replace("/,/g", ";").split(";");
                        List<String> billingEmailList = Arrays.asList(billingEmaild);
                        emailList.addAll(billingEmailList);
                    }
                    }
                    
                    if (!toEmailList.isEmpty() && billid.equals(billIDs[0]) && billIDs.length==1) {
                        emailList.addAll(toEmailList);
                    }
                    /*
                    IF Invoice has no emailid then take email id from manually enterd by user
                    */
                    if(!emailDataMap.isEmpty() && emailDataMap.containsKey(billid)){
                        String data=(String)emailDataMap.get(billid);
                        data=data.replace(",", ";");
                        if (!StringUtil.isNullOrEmpty(data)) {
                            String[] billingEmaild = data.split(";");
                            List<String> billingEmailList = Arrays.asList(billingEmaild);
                            emailList.addAll(billingEmailList);
                        }
                    }
                        
//                    if (!emailList.isEmpty()) {
//                        jsonObj = replacePlaceHolders(requestParam, company, result, moduleid);
//                        jsonObj.put("mode", mode);
//                        jsonObj.put("billid", billid);
//                        jsonObj.put("bills", billid);
//                        jsonObj.put("moduleid", moduleid);
//                        jsonObj.put("fieldid", fieldid);
//                        jsonObj.put("path", path);
//                        jsonObj.put("servPath", servPath);
//                        jsonObj.put("templateid", templateid);
//                    }
//                        
                    
                    Set<String> hashSet = new HashSet<String>();
                    hashSet.addAll(emailList);
                    emailList.clear();
                    emailList.addAll(hashSet);
                    jsonObj.put("emailid", org.apache.commons.lang.StringUtils.join(emailList, ";"));

                    Iterator<String> keys = jsonObj.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        String value = jsonObj.getString(key);
                        requestParam.put(key, value);
                    }
                    sendMailNotification(requestParam);

                }

            }
        } catch (Exception ex) {
            Logger.getLogger(AccMailNotifyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    
    class MessageSizeExceedingException extends Exception {

        String msg = "";

        public MessageSizeExceedingException(String message) {
            this.msg = message.trim();
        }

        @Override
        public String toString() {
            String sizeExceedingMsg = "552 4.3.1 Message size exceeds fixed maximum message size";

            if (sizeExceedingMsg.equalsIgnoreCase(this.msg)) {
                return ("Attached file(s) size is exceeding message size limit!");
            } else {
                return ("");
            }

        }
    }
}